/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.utilities.engines.rest;

import java.util.HashMap;
import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ArtifactServiceProxy;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.services.proxy.MetamodelServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.IEngineInstance;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public abstract class AbstractEngineRestService extends AbstractRestService {

	public static transient Logger logger = Logger.getLogger(AbstractEngineRestService.class);

	private String userId;
	private String userUniqueIdentifier;
	private String auditId;
	private String documentId;
	private IDataSource dataSource;
	private IDataSet dataSet;
	private Locale locale;
	protected EngineAnalysisMetadata analysisMetadata;
	protected byte[] analysisStateRowData;

	private Content template;

	private ContentServiceProxy contentProxy;
	private AuditServiceProxy auditProxy;
	private DataSourceServiceProxy datasourceProxy;
	private DataSetServiceProxy datasetProxy;
	private MetamodelServiceProxy metamodelProxy;
	private ArtifactServiceProxy artifactProxy;

	public static final String AUDIT_ID = "SPAGOBI_AUDIT_ID";
	public static final String DOCUMENT_ID = "document";
	public static final String SBI_EXECUTION_ID = "SBI_EXECUTION_ID";
	public static final String ENV_DOCUMENT_LABEL = "DOCUMENT_LABEL";

	public static final String COUNTRY = "SBI_COUNTRY";
	public static final String LANGUAGE = "SBI_LANGUAGE";
	public static final String SCRIPT = "SBI_SCIRPT";

	public static final String SUBOBJ_ID = "subobjectId";
	public static final String SUBOBJ_NAME = "nameSubObject";
	public static final String SUBOBJ_DESCRIPTION = "descriptionSubObject";
	public static final String SUBOBJ_VISIBILITY = "visibilitySubObject";

	public abstract String getEngineName();

	private String successString;

	public SourceBean getTemplateAsSourceBean() {
		SourceBean templateSB = null;
		try {
			templateSB = SourceBean.fromXMLString(getTemplateAsString());
		} catch (SourceBeanException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to parse template's content", e);
			engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			engineException.addHint("Check if the document's template is a well formed xml file");
			throw engineException;
		}

		return templateSB;
	}

	public String getTemplateAsString() {
		byte[] temp = getTemplate();
		if (temp != null)
			return new String(temp);
		else
			return new String("");
	}

	private byte[] getTemplate() {
		byte[] templateContent = null;
		HashMap requestParameters;

		if (template == null) {
			contentProxy = getContentServiceProxy();
			if (contentProxy == null) {
				throw new SpagoBIEngineStartupException("SpagoBIQbeEngine", "Impossible to instatiate proxy class [" + ContentServiceProxy.class.getName()
						+ "] " + "in order to retrive the template of document [" + documentId + "]");
			}

			requestParameters = ParametersDecoder.getDecodedRequestParameters(this.getServletRequest());
			template = contentProxy.readTemplate(getDocumentId(), requestParameters);
		}
		try {
			if (template == null)
				throw new SpagoBIEngineRuntimeException("There are no template associated to document [" + documentId + "]");
			templateContent = DatatypeConverter.parseBase64Binary(template.getContent());
		} catch (Throwable e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to get template's content", e);
			engineException.setDescription("Impossible to get template's content:  " + e.getMessage());
			engineException.addHint("Check the document's template");
			throw engineException;
		}

		return templateContent;
	}

	protected ContentServiceProxy getContentServiceProxy() {
		if (contentProxy == null) {
			contentProxy = new ContentServiceProxy(getUserIdentifier(), getHttpSession());
		}
		return contentProxy;
	}

	public AuditServiceProxy getAuditServiceProxy() {
		if (auditProxy == null && getAuditId() != null) {
			auditProxy = new AuditServiceProxy(getAuditId(), getUserIdentifier(), getHttpSession());
		}

		return auditProxy;
	}

	public DataSourceServiceProxy getDataSourceServiceProxy() {
		if (datasourceProxy == null) {
			datasourceProxy = new DataSourceServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return datasourceProxy;
	}

	public ArtifactServiceProxy getArtifactServiceProxy() {
		if (artifactProxy == null) {
			artifactProxy = new ArtifactServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return artifactProxy;
	}

	public DataSetServiceProxy getDataSetServiceProxy() {
		if (datasetProxy == null) {
			datasetProxy = new DataSetServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return datasetProxy;
	}

	public UserProfile getUserProfile() {
		UserProfile profile = (UserProfile) getAttributeFromHttpSession(IEngUserProfile.ENG_USER_PROFILE);
		return profile != null ? profile : UserProfileManager.getProfile();
	}

	public String getUserId() {
		if (userId == null) {
			userId = (String) getUserProfile().getUserId();
		}
		return userId;
	}

	public String getUserIdentifier() {
		if (userUniqueIdentifier == null) {
			userUniqueIdentifier = (String) getUserProfile().getUserUniqueIdentifier();
		}
		return userUniqueIdentifier;
	}

	/**
	 * Gets the audit id.
	 *
	 * @return the audit id
	 */
	public String getAuditId() {

		logger.debug("IN");

		try {
			if (auditId == null) {
				auditId = getServletRequest().getParameter(AUDIT_ID);
			}
		} finally {
			logger.debug("OUT");
		}

		return auditId;
	}

	/**
	 * Gets the document id.
	 *
	 * @return the document id
	 */
	public String getDocumentId() {
		String documentIdInSection = null;

		logger.debug("IN");

		try {
			if (documentId == null) {
				documentIdInSection = getAttributeFromSessionAsString(DOCUMENT_ID);
				logger.debug("documentId in Session:" + documentIdInSection);

				if (requestContainsAttribute(DOCUMENT_ID)) {
					documentId = getAttributeAsString(DOCUMENT_ID);
				} else {
					documentId = documentIdInSection;
					logger.debug("documentId has been taken from session");
				}
			}

			if (documentId == null) {
				SpagoBIEngineStartupException e = new SpagoBIEngineStartupException(getEngineName(), "Impossible to retrive document id");
				e.setDescription("The engine is unable to retrive the id of the document to execute from request");
				e.addHint(
						"Check on SpagoBI Server if the analytical document you want to execute have a valid template associated. Maybe you have saved the analytical document without "
								+ "uploading a valid template file");
				throw e;
			}
		} finally {
			logger.debug("OUT");
		}

		return documentId;
	}

	public String getDocumentLabel() {
		String documentLabelInSection = null;
		String documentLabel = null;

		logger.debug("IN");

		try {
			if (documentLabel == null) {
				documentLabelInSection = getAttributeFromSessionAsString(ENV_DOCUMENT_LABEL);
				logger.debug("documentLabel in Session:" + documentLabelInSection);

				if (requestContainsAttribute(ENV_DOCUMENT_LABEL)) {
					documentLabel = getAttributeAsString(ENV_DOCUMENT_LABEL);
				} else {
					documentLabel = documentLabelInSection;
					logger.debug("documentLabel has been taken from session");
				}
			}

			if (documentLabel == null) {
				SpagoBIEngineStartupException e = new SpagoBIEngineStartupException(getEngineName(), "Impossible to retrive document label");
				e.setDescription("The engine is unable to retrive the label of the document to execute from request");
				e.addHint(
						"Check on SpagoBI Server if the analytical document you want to execute have a valid template associated. Maybe you have saved the analytical document without "
								+ "uploading a valid template file");
				throw e;
			}
		} finally {
			logger.debug("OUT");
		}

		return documentLabel;
	}

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 */

	public IDataSource getDataSource() {
		String schema = null;
		String attrname = null;

		if (dataSource == null) {
			dataSource = getDataSourceServiceProxy().getDataSource(getDocumentId());
			if (dataSource == null) {
				logger.warn("Datasource is not defined.");
				if (!this.tolerateMissingDatasource()) {
					logger.error("The datasource is mandatory but it is not defined");
					throw new SpagoBIEngineRuntimeException("Datasource is not defined.");
				}

			} else {
				if (dataSource.checkIsMultiSchema()) {
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
			}
		}

		return dataSource;
	}

	/**
	 * A datasource is generally required, therefore default is false, but engines can override this method and accept missing datasource
	 *
	 * @return true if this engine tolerates the case when the datasource is missing
	 */
	protected boolean tolerateMissingDatasource() {
		return false;
	}

	/**
	 * Builds a simple success json {result: ok}
	 *
	 * @return
	 */
	public String getJsonSuccess() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}

	/**
	 * Builds a simple success json {success: true}
	 *
	 * @return
	 */
	public String getJsonSuccessTrue() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("success", true);
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}

	/**
	 * Builds a simple fail json {result: ko}
	 *
	 * @return
	 */
	public String getJsonKo() {
		if (successString == null) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ko");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			successString = obj.toString();
		}
		return successString;
	}

	/**
	 * Saves the analysis state
	 *
	 * @param name        of the subobject
	 * @param description of the subobject
	 * @param scope       of the subobject
	 * @return
	 */
	public String saveAnalysisState(String name, String description, String scope) {
		EngineAnalysisMetadata analysisMetadata = null;

		logger.debug("IN");

		logger.debug("Subobject Name: " + name);
		logger.debug("Subobject description: " + description);
		logger.debug("Subobject scope: " + scope);

		analysisMetadata = getEngineInstance().getAnalysisMetadata();
		analysisMetadata.setName(name);
		analysisMetadata.setDescription(description);

		if (EngineAnalysisMetadata.PUBLIC_SCOPE.equalsIgnoreCase(scope)) {
			analysisMetadata.setScope(EngineAnalysisMetadata.PUBLIC_SCOPE);
		} else if (EngineAnalysisMetadata.PRIVATE_SCOPE.equalsIgnoreCase(scope)) {
			analysisMetadata.setScope(EngineAnalysisMetadata.PRIVATE_SCOPE);
		} else {
			Assert.assertUnreachable("Value [" + scope + "] is not valid for the input parameter scope");
		}

		String result = null;
		try {
			result = saveAnalysisState();
		} catch (SpagoBIEngineException e) {
			logger.error("Error saving the subobject", e);
			throw new SpagoBIRestServiceException("sbi.olap.save.analysis.error", getLocale(), "Error saving the subobject", e);
		}
		if (!result.trim().toLowerCase().startsWith("ok")) {
			logger.error("Error saving the subobject " + result);
			throw new SpagoBIRestServiceException("sbi.olap.save.analysis.error", getLocale(), "Error saving the subobject");
		}

		return getJsonSuccess();
	}

	/**
	 * Saves the analysis state. It gets the name, description, ... from the engine instance
	 *
	 * @return
	 * @throws SpagoBIEngineException
	 */
	public String saveAnalysisState() throws SpagoBIEngineException {
		IEngineInstance engineInstance = null;
		String documentId = null;
		EngineAnalysisMetadata analysisMetadata = null;
		IEngineAnalysisState analysisState = null;
		ContentServiceProxy contentServiceProxy = null;
		String serviceResponse = null;

		engineInstance = getEngineInstance();
		analysisMetadata = engineInstance.getAnalysisMetadata();
		analysisState = engineInstance.getAnalysisState();

		if (getEnv() == null) {
			return "KO - Missing environment";
		}

		contentServiceProxy = (ContentServiceProxy) getEnv().get(EngineConstants.ENV_CONTENT_SERVICE_PROXY);
		if (contentServiceProxy == null) {
			return "KO - Missing content service proxy";
		}

		documentId = (String) getEnv().get(EngineConstants.ENV_DOCUMENT_ID);
		if (documentId == null) {
			return "KO - Missing document id";
		}

		String isPublic = "false";
		if (AbstractEngineAction.PUBLIC_SCOPE.equalsIgnoreCase(analysisMetadata.getScope()))
			isPublic = "true";

		serviceResponse = contentServiceProxy.saveSubObject(documentId, analysisMetadata.getName(), analysisMetadata.getDescription(), isPublic,
				new String(analysisState.store()));

		return serviceResponse;
	}
}
