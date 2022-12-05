/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.engines.qbe.services.initializers;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.api.AbstractQbeEngineResource;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.services.proxy.MetamodelServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

@Path("/start-qbe")
public class QbeEngineStartResource extends AbstractQbeEngineResource {

	public static final String ENGINE_NAME = "SpagoBIQbeEngine";
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;

	public static final String ENGINE_DATASOURCE_LABEL = "ENGINE_DATASOURCE_LABEL";
	private static final String DATA_SOURCE_LABEL = "DATA_SOURCE_LABEL";

	private DataSourceServiceProxy datasourceProxy;
	private ContentServiceProxy contentProxy;
	private DataSetServiceProxy datasetProxy;
	private MetamodelServiceProxy metamodelProxy;

	protected static final Base64.Decoder DECODER = Base64.getDecoder();
	private Content template;
	public static final String DOCUMENT_ID = "document";
	public static final String REGISTRY_CONFIGURATION = "REGISTRY_CONFIGURATION";
	private String documentId;

	@Context
	protected HttpServletRequest request;

	@Context
	protected HttpServletResponse response;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startQbe(@QueryParam("datamart") String datamart, @QueryParam("drivers") String drivers, @QueryParam("registryId") String registryId,
			@QueryParam("sourceDatasetLabel") String sourceDatasetLabel) {

		logger.debug("IN");

		QbeEngineInstance qbeEngineInstance = null;

		try {

			if (StringUtilities.isNotEmpty(datamart)) {
				logger.debug("Starting qbe from datamart [" + datamart + "]");
				qbeEngineInstance = getEngineInstancefromDatamart(datamart, drivers);
			} else if (StringUtilities.isNotEmpty(sourceDatasetLabel)) {
				logger.debug("Starting qbe from dataset [" + sourceDatasetLabel + "]");
				qbeEngineInstance = getEngineInstancefromDataset(sourceDatasetLabel);
			} else {
				throw new SpagoBIEngineStartupException(ENGINE_NAME, "Cannot start QbE: neither datamart nor dataset are specified");
			}
		} catch (Exception e) {
			SpagoBIEngineStartupException serviceException = null;

			if (e instanceof SpagoBIEngineStartupException) {
				serviceException = (SpagoBIEngineStartupException) e;
			} else {
				Throwable rootException = e;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				String message = "An unpredicted error occurred while executing " + ENGINE_NAME + " service." + "\nThe root cause of the error is: " + str;

				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, message, e);
			}

			throw serviceException;

		} finally {
			logger.debug("OUT");
		}

		return Response.ok().build();
	}

	protected QbeEngineInstance getEngineInstancefromDataset(String sourceDatasetLabel) {

		QbeEngineInstance qbeEngineInstance = null;

		IDataSet dataset = this.getDataSetServiceProxy().getDataSetByLabel(sourceDatasetLabel);
		if (!dataset.isPersisted() && !dataset.isFlatDataset() && !dataset.getDsType().equals("SbiQueryDataSet")
				&& !dataset.getDsType().equals("SbiFileDataSet")) {
			logger.error("Dataset [" + sourceDatasetLabel + "] is not persisted. Cannot start qbe.");
			throw new SpagoBIEngineStartupException(ENGINE_NAME, "Cannot start QbE from a non-persisted dataset");
		}

		Map env = getEnv();

		env.put(EngineConstants.ENV_DATASETS, Collections.singletonList(dataset));

		logger.debug("Creating engine instance ...");
		try {
			qbeEngineInstance = QbeEngine.createInstance(null, env);
		} catch (Exception t) {
			SpagoBIEngineStartupException serviceException;
			Throwable rootException = t;
			while (rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
			serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, str, t);

			throw serviceException;
		}
		logger.debug("Engine instance succesfully created");
		return qbeEngineInstance;
	}

	protected QbeEngineInstance getEngineInstancefromDatamart(String datamart, String drivers) {

		QbeEngineInstance qbeEngineInstance = null;

		SourceBean templateBean = getTemplateAsSourceBean(datamart);
		logger.debug("Template: " + templateBean);
		logger.debug("Creating engine instance ...");
		Map env = getEnv();
		env.put("DRIVERS", decodeParameterValue(drivers));
		try {
			qbeEngineInstance = QbeEngine.createInstance(templateBean, env);
		} catch (Throwable t) {
			SpagoBIEngineStartupException serviceException;
			Throwable rootException = t;
			while (rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
			serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, str, t);

			if (rootException instanceof QbeTemplateParseException) {
				QbeTemplateParseException e = (QbeTemplateParseException) rootException;
				serviceException.setDescription(e.getDescription());
				serviceException.setHints(e.getHints());
			}

			throw serviceException;
		}
		logger.debug("Engine instance succesfully created");
		return qbeEngineInstance;
	}

	private SourceBean getTemplateAsSourceBean(String modelName) {
		try {
			SourceBean qbeSB = new SourceBean("QBE");
			SourceBean datamartSB = new SourceBean("DATAMART");
			datamartSB.setAttribute("name", modelName);
			qbeSB.setAttribute(datamartSB);
			return qbeSB;
		} catch (SourceBeanException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(ENGINE_NAME,
					"Impossible to create a new template for the model " + modelName, e);
			engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			engineException.addHint("Check if the document's template is a well formed xml file");
			throw engineException;
		}

	}

	@Override
	public Map getEnv() {
		Map env = new HashMap();

		IDataSource dataSource = getEnvFromSuperTODO(env);

		if (dataSource == null || dataSource.checkIsReadOnly()) {
			logger.debug("Getting datasource for writing, since the datasource is not defined or it is read-only");
			IDataSource datasourceForWriting = this.getDataSourceForWriting();
			env.put(EngineConstants.DATASOURCE_FOR_WRITING, datasourceForWriting);
		} else {
			env.put(EngineConstants.DATASOURCE_FOR_WRITING, dataSource);
		}

		return env;
	}

	private IDataSource getEnvFromSuperTODO(Map env) {
		IDataSource dataSource = getDataSource();
		try {
			env.put(EngineConstants.ENV_DATASOURCE, dataSource);
		} catch (Exception e) {
			logger.debug("Error loading the datasource in the getEnv", e);
		}

		// document id can be null (when using QbE for dataset definition)
//		if (getDocumentId() != null) {
//			env.put(EngineConstants.ENV_DOCUMENT_ID, getDocumentId());
//		}
		env.put(EngineConstants.ENV_USER_PROFILE, getUserProfile());
		env.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, getContentServiceProxy());
//		env.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, getAuditServiceProxy());
		env.put(EngineConstants.ENV_DATASET_PROXY, getDataSetServiceProxy());
		env.put(EngineConstants.ENV_DATASOURCE_PROXY, getDataSourceServiceProxy());
		try {
			env.put(EngineConstants.ENV_METAMODEL_PROXY, getMetamodelServiceProxy());
		} catch (Throwable t) {
			logger.warn("Impossible to instatiate the metamodel proxy", t);
		}
		env.put(EngineConstants.ENV_LOCALE, Locale.getDefault());
		return dataSource;
	}

	protected String decodeParameterValue(String parValue) {
		String newParValue;

		ParametersDecoder decoder = new ParametersDecoder();
		if (decoder.isMultiValues(parValue)) {
			List values = decoder.decode(parValue);
			newParValue = "";
			for (int i = 0; i < values.size(); i++) {
				newParValue += (i > 0 ? "," : "");
				newParValue += values.get(i);
			}
		} else {
			newParValue = parValue;
		}

		return newParValue;
	}

	public IDataSource getDataSource() {
		IDataSource dataSource = null;
		String dataSourceLabel = getAttributeAsString(DATA_SOURCE_LABEL);
		if (dataSourceLabel == null) {
			dataSourceLabel = this.getAttributeAsString(ENGINE_DATASOURCE_LABEL);
		}
		if (dataSourceLabel != null) {
			dataSource = getDataSourceServiceProxy().getDataSourceByLabel(dataSourceLabel);
		}
		if (dataSource == null && documentId != null) {
			dataSource = getDataSourceServiceProxy().getDataSource(documentId);

			if (dataSource.checkIsMultiSchema()) {
				logger.debug("Datasource [" + dataSource.getLabel() + "] is defined on multi schema");
				try {
					logger.debug("Retriving target schema for datasource [" + dataSource.getLabel() + "]");
					String attrname = dataSource.getSchemaAttribute();
					logger.debug("Datasource's schema attribute name is equals to [" + attrname + "]");
					Assert.assertNotNull(attrname, "Datasource's schema attribute name cannot be null in order to retrive the target schema");
					String schema = (String) getUserProfile().getUserAttribute(attrname);
					Assert.assertNotNull(schema, "Impossible to retrive the value of attribute [" + attrname + "] form user profile");
					dataSource.setJndi(dataSource.getJndi() + schema);
					logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] is [" + dataSource.getJndi() + "]");
				} catch (Throwable t) {
					throw new SpagoBIEngineRuntimeException("Impossible to retrive target schema for datasource [" + dataSource.getLabel() + "]", t);
				}
				logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] retrieved succesfully");
			}

		}
		if (dataSource == null) {
			dataSource = getDataSourceServiceProxy().getDataSourceForCache();
		}
		return dataSource;
	}

	public DataSourceServiceProxy getDataSourceServiceProxy() {
		if (datasourceProxy == null) {
			datasourceProxy = new DataSourceServiceProxy((String) getUserProfile().getUserUniqueIdentifier(), getHttpSession());
		}

		return datasourceProxy;
	}

	protected ContentServiceProxy getContentServiceProxy() {
		if (contentProxy == null) {
			contentProxy = new ContentServiceProxy((String) getUserProfile().getUserUniqueIdentifier(), getHttpSession());
		}

		return contentProxy;
	}

	public DataSetServiceProxy getDataSetServiceProxy() {
		if (datasetProxy == null) {
			datasetProxy = new DataSetServiceProxy((String) getUserProfile().getUserUniqueIdentifier(), getHttpSession());
		}

		return datasetProxy;
	}

	public MetamodelServiceProxy getMetamodelServiceProxy() {
		if (metamodelProxy == null) {
			metamodelProxy = new MetamodelServiceProxy((String) getUserProfile().getUserUniqueIdentifier(), getHttpSession());
		}

		return metamodelProxy;
	}

	public IDataSource getDataSourceForWriting() {
		String schema = null;
		String attrname = null;

		String datasourceLabel = this.getAttributeAsString(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL);

		if (datasourceLabel != null) {
			IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel(datasourceLabel);
			if (dataSource != null && dataSource.checkIsMultiSchema()) {
				logger.debug("Datasource [" + dataSource.getLabel() + "] is defined on multi schema");
				try {
					logger.debug("Retriving target schema for datasource [" + dataSource.getLabel() + "]");
					attrname = dataSource.getSchemaAttribute();
					logger.debug("Datasource's schema attribute name is equals to [" + attrname + "]");
					Assert.assertNotNull(attrname, "Datasource's schema attribute name cannot be null in order to retrive the target schema");
					schema = (String) getUserProfile().getUserAttribute(attrname);
					Assert.assertNotNull(schema, "Impossible to retrive the value of attribute [" + attrname + "] form user profile");
					dataSource.setJndi(dataSource.getJndi() + schema);
					logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] is [" + dataSource.getJndi() + "]");
				} catch (Throwable t) {
					throw new SpagoBIEngineRuntimeException("Impossible to retrive target schema for datasource [" + dataSource.getLabel() + "]", t);
				}
				logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] retrieved succesfully");
			}
			return dataSource;
		}

		return null;
	}

	protected String getUserIdentifier() {
		return String.valueOf(getUserProfile().getUserUniqueIdentifier());
	}

	@Override
	protected UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

	/**** REGISTRY START ****/
	public SourceBean getRegistryTemplateAsSourceBean(String registryId) {
		SourceBean templateSB = null;
		try {
			templateSB = SourceBean.fromXMLString(getTemplateAsString(registryId));
		} catch (SourceBeanException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to parse template's content", e);
			engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			engineException.addHint("Check if the document's template is a well formed xml file");
			throw engineException;
		}

		return templateSB;
	}

	public String getTemplateAsString(String registryId) {

		byte[] temp = getTemplate(registryId);
		if (temp != null)
			return new String(temp);
		else
			return new String("");
	}

	private byte[] getTemplate(String registryId) {
		byte[] templateContent = null;
		HashMap requestParameters;

		if (template == null) {
			contentProxy = getContentServiceProxy();
			if (contentProxy == null) {
				throw new SpagoBIEngineStartupException("SpagoBIQbeEngine", "Impossible to instatiate proxy class [" + ContentServiceProxy.class.getName()
						+ "] " + "in order to retrive the template of document [" + registryId + "]");
			}

			requestParameters = ParametersDecoder.getDecodedRequestParameters(request);
			template = contentProxy.readTemplate(registryId, requestParameters);
		}

		try {
			if (template == null)
				throw new SpagoBIEngineRuntimeException("There are no template associated to document [" + registryId + "]");
			templateContent = DECODER.decode(template.getContent());
		} catch (Throwable e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to get template's content", e);
			engineException.setDescription("Impossible to get template's content:  " + e.getMessage());
			engineException.addHint("Check the document's template");
			throw engineException;
		}

		return templateContent;
	}

	private String getEngineName() {

		return SpagoBIConstants.QBE_ENGINE_LABEL;
	}

	/**** REGISTRY END ****/
}