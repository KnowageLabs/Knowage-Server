/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.container.ContextManager;
import it.eng.spagobi.container.IBeanContainer;
import it.eng.spagobi.container.IContainer;
import it.eng.spagobi.container.SpagoBIRequestContainer;
import it.eng.spagobi.container.strategy.ExecutionContextRetrieverStrategy;
import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.services.proxy.MetamodelServiceProxy;
import it.eng.spagobi.tools.dataset.bo.AbstractDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTable;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableRecorder;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sun.misc.BASE64Decoder;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class AbstractEngineStartAction extends AbstractBaseHttpAction {

	private String engineName;

	private ContextManager conetxtManager;

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

	protected static final BASE64Decoder DECODER = new BASE64Decoder();

	public static final String AUDIT_ID = "SPAGOBI_AUDIT_ID";
	public static final String DOCUMENT_ID = "document";
	public static final String SBI_EXECUTION_ID = "SBI_EXECUTION_ID";

	public static final String COUNTRY = "SBI_COUNTRY";
	public static final String LANGUAGE = "SBI_LANGUAGE";

	public static final String SUBOBJ_ID = "subobjectId";
	public static final String SUBOBJ_NAME = "nameSubObject";
	public static final String SUBOBJ_DESCRIPTION = "descriptionSubObject";
	public static final String SUBOBJ_VISIBILITY = "visibilitySubObject";

	/**
	 * Logger component
	 */
	public static transient Logger logger = Logger.getLogger(AbstractEngineStartAction.class);

	@Override
	public void init(SourceBean config) {
		super.init(config);
	}

	public void service(SourceBean request, SourceBean response) throws SpagoBIEngineException {
		setSpagoBIRequestContainer(request);
		setSpagoBIResponseContainer(response);
	}

	public String getEngineName() {
		return engineName;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	// all accesses to session into the engine's scope refer to HttpSession and not to Spago's SessionContainer

	public ContextManager getConetxtManager() {
		if (conetxtManager == null) {
			IContextRetrieverStrategy contextRetriveStrategy;
			contextRetriveStrategy = new ExecutionContextRetrieverStrategy(getSpagoBIRequestContainer());
			conetxtManager = new ContextManager(super.getSpagoBIHttpSessionContainer(), contextRetriveStrategy);
		}

		List list = conetxtManager.getKeys();

		return conetxtManager;
	}

	@Override
	public IBeanContainer getSpagoBISessionContainer() {
		return getSpagoBIHttpSessionContainer();
	}

	@Override
	public IBeanContainer getSpagoBIHttpSessionContainer() {
		return getConetxtManager();
	}

	public UserProfile getUserProfile() {
		return (UserProfile) getAttributeFromSession(IEngUserProfile.ENG_USER_PROFILE);
	}

	public String getUserId() {
		UserProfile profile = null;

		if (userId == null) {
			userId = (String) getUserProfile().getUserId();
		}

		return userId;
	}

	public String getUserIdentifier() {
		IEngUserProfile profile = null;

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
				auditId = getHttpRequest().getParameter(AUDIT_ID);
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
				e.addHint("Check on SpagoBI Server if the analytical document you want to execute have a valid template associated. Maybe you have saved the analytical document without "
						+ "uploading a valid template file");
				throw e;
			}
		} finally {
			logger.debug("OUT");
		}

		return documentId;
	}

	public JSONObject getTemplateAsJSONObject() {
		JSONObject templateSB = null;
		try {
			templateSB = new JSONObject(getTemplateAsString());
		} catch (JSONException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to parse template's content", e);
			engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			engineException.addHint("Check if the document's template is a well formed json file");
			throw engineException;
		}

		return templateSB;
	}

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

			requestParameters = ParametersDecoder.getDecodedRequestParameters(this.getHttpRequest());
			template = contentProxy.readTemplate(getDocumentId(), requestParameters);
		}
		try {
			if (template == null)
				throw new SpagoBIEngineRuntimeException("There are no template associated to document [" + documentId + "]");
			templateContent = DECODER.decodeBuffer(template.getContent());
		} catch (Throwable e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Impossible to get template's content", e);
			engineException.setDescription("Impossible to get template's content:  " + e.getMessage());
			engineException.addHint("Check the document's template");
			throw engineException;
		}

		return templateContent;
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
	 * A datasource is generally required, therefore default is false, but engines can override this method and accept missing datasource (example: Worksheet
	 * created on a file dataset)
	 * 
	 * @return true if this engine tolerates the case when the datasource is missing
	 */
	protected boolean tolerateMissingDatasource() {
		return false;
	}

	public IDataSet getDataSet() {
		if (dataSet == null) {
			dataSet = getDataSetServiceProxy().getDataSet(getDocumentId());
		}
		return dataSet;
	}

	/**
	 * Gets the locale.
	 * 
	 * @return the locale
	 */
	public Locale getLocale() {
		String language;
		String country;

		if (locale == null) {

			logger.debug("IN");

			language = getAttributeAsString(LANGUAGE);
			country = getAttributeAsString(COUNTRY);
			logger.debug("Locale parameters received: language = [" + language + "] ; country = [" + country + "]");

			try {
				locale = new Locale(language, country);
			} catch (Exception e) {
				logger.debug("Error while creating Locale object from input parameters: language = [" + language + "] ; country = [" + country + "]");
				logger.debug("Creating default locale [en,US].");
				locale = new Locale("en", "US");
			}

			logger.debug("IN");
		}

		return locale;
	}

	/**
	 * Gets the analysis metadata.
	 * 
	 * @return the analysis metadata
	 */
	public EngineAnalysisMetadata getAnalysisMetadata() {
		if (analysisMetadata != null) {
			return analysisMetadata;
		}

		logger.debug("IN");

		analysisMetadata = new EngineAnalysisMetadata();

		if (requestContainsAttribute(SUBOBJ_ID)) {

			Integer id = getAttributeAsInteger(SUBOBJ_ID);
			if (id == null) {
				logger.warn("Value [" + getAttribute(SUBOBJ_ID).toString() + "] is not a valid subobject id");
			}
			analysisMetadata.setId(id);

			if (requestContainsAttribute(SUBOBJ_NAME)) {
				analysisMetadata.setName(getAttributeAsString(SUBOBJ_NAME));
			} else {
				logger.warn("No name attribute available in request for subobject [" + getAttributeAsString(SUBOBJ_ID) + "]");
				analysisMetadata.setName(getAttributeAsString(SUBOBJ_ID));
			}

			if (requestContainsAttribute(SUBOBJ_DESCRIPTION)) {
				analysisMetadata.setDescription(getAttributeAsString(SUBOBJ_DESCRIPTION));
			} else {
				logger.warn("No description attribute available in request for subobject [" + getAttributeAsString(SUBOBJ_ID) + "]");
				analysisMetadata.setDescription("");
			}

			if (requestContainsAttribute(SUBOBJ_VISIBILITY)) {
				if (requestContainsAttribute(SUBOBJ_VISIBILITY, "Public")) {
					analysisMetadata.setScope(EngineAnalysisMetadata.PUBLIC_SCOPE);
				} else {
					logger.warn("No visibility attribute available in request for subobject [" + getAttributeAsString(SUBOBJ_ID) + "]");
					analysisMetadata.setScope(EngineAnalysisMetadata.PRIVATE_SCOPE);
				}
			}
		}

		logger.debug("OUT");

		return analysisMetadata;
	}

	/**
	 * Gets the analysis state row data.
	 * 
	 * @return the analysis state row data
	 */
	public byte[] getAnalysisStateRowData() {
		Content spagoBISubObject;
		byte[] rowData;

		if (analysisStateRowData == null && getAnalysisMetadata().getId() != null) {

			logger.debug("IN");

			spagoBISubObject = getContentServiceProxy().readSubObjectContent(getAnalysisMetadata().getId().toString());
			try {
				rowData = DECODER.decodeBuffer(spagoBISubObject.getContent());
				analysisStateRowData = rowData;
			} catch (IOException e) {
				logger.warn("Impossible to decode the content of " + getAnalysisMetadata().getId().toString() + " subobject");
				return null;
			}

			logger.debug("OUT");
		}

		return analysisStateRowData;
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

	public DataSetServiceProxy getDataSetServiceProxy() {
		if (datasetProxy == null) {
			datasetProxy = new DataSetServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return datasetProxy;
	}

	public MetamodelServiceProxy getMetamodelServiceProxy() {
		if (metamodelProxy == null) {
			metamodelProxy = new MetamodelServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return metamodelProxy;
	}

	public Map getEnv() {
		Map env = new HashMap();

		copyRequestParametersIntoEnv(env, getSpagoBIRequestContainer());
		try {
			env.put(EngineConstants.ENV_DATASOURCE, getDataSource());
		} catch (Exception e) {
			logger.debug("Error loading the datasource in the getEnv", e);
		}

		// document id can be null (when using QbE for dataset definition)
		if (getDocumentId() != null) {
			env.put(EngineConstants.ENV_DOCUMENT_ID, getDocumentId());
		}
		env.put(EngineConstants.ENV_USER_PROFILE, getUserProfile());
		env.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, getContentServiceProxy());
		env.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, getAuditServiceProxy());
		env.put(EngineConstants.ENV_DATASET_PROXY, getDataSetServiceProxy());
		env.put(EngineConstants.ENV_DATASOURCE_PROXY, getDataSourceServiceProxy());
		try {
			env.put(EngineConstants.ENV_METAMODEL_PROXY, getMetamodelServiceProxy());
		} catch (Throwable t) {
			logger.warn("Impossible to instatiate the metamodel proxy", t);
		}
		env.put(EngineConstants.ENV_LOCALE, getLocale());

		return env;
	}

	/**
	 * Copy request parameters into env.
	 * 
	 * @param env
	 *            the env
	 * @param serviceRequest
	 *            the service request
	 */
	public void copyRequestParametersIntoEnv(Map env, IContainer request) {
		Set parameterStopList = null;
		List requestParameters = null;

		logger.debug("IN");

		parameterStopList = new HashSet();
		parameterStopList.add("template");
		parameterStopList.add("ACTION_NAME");
		parameterStopList.add("NEW_SESSION");
		parameterStopList.add("document");
		parameterStopList.add("spagobicontext");
		parameterStopList.add("BACK_END_SPAGOBI_CONTEXT");
		parameterStopList.add("userId");
		parameterStopList.add("auditId");

		requestParameters = ((SpagoBIRequestContainer) request).getRequest().getContainedAttributes();
		for (int i = 0; i < requestParameters.size(); i++) {
			SourceBeanAttribute attrSB = (SourceBeanAttribute) requestParameters.get(i);
			logger.debug("Parameter [" + attrSB.getKey() + "] has been read from request");
			logger.debug("Parameter [" + attrSB.getKey() + "] is of type  " + attrSB.getValue().getClass().getName());
			logger.debug("Parameter [" + attrSB.getKey() + "] is equal to " + attrSB.getValue().toString());

			if (parameterStopList.contains(attrSB.getKey())) {
				logger.debug("Parameter [" + attrSB.getKey() + "] copyed into environment parameters list: FALSE");
				continue;
			}

			env.put(attrSB.getKey(), decodeParameterValue(attrSB.getValue().toString()));
			logger.debug("Parameter [" + attrSB.getKey() + "] copyed into environment parameters list: TRUE");
		}

		logger.debug("OUT");
	}

	/**
	 * Decode parameter value.
	 * 
	 * @param parValue
	 *            the par value
	 * 
	 * @return the string
	 */
	private String decodeParameterValue(String parValue) {
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

	protected IDataSetTableDescriptor persistDataset(IDataSet dataset, Map env) {
		IDataSetTableDescriptor descriptor = null;
		if (!dataset.isPersisted() && !dataset.isFlatDataset()) {
			logger.debug("Dataset is neither persisted nor flat. Persisting dataset into a temporary table...");
			IDataSource dataSource = (IDataSource) env.get(EngineConstants.DATASOURCE_FOR_WRITING);
			if (dataSource == null) {
				logger.error("Datasource for persistence not set, cannot proceed!!");
				throw new SpagoBIEngineStartupException(getEngineName(), "Datasource for persistence not set");
			}
			String tableName = this.getPersistenceTableName();
			descriptor = this.persistDataSetWithTemporaryTable(dataset, tableName, dataSource);
			logger.debug("Dataset persisted.");

			// since this a start action, we can consider that the dataset can
			// be considered to be persistent during all the user session, so we
			// can change the state of the dataset. Compare with
			// it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction
			// where we cannot
			dataset.setPersisted(true);
			dataset.setPersistTableName(descriptor.getTableName());
			dataset.setDataSourceForReading(dataSource);

		} else {
			try {
				List<String> fields = ((AbstractDataSet) dataset).getFieldsList();
				descriptor = TemporaryTableManager.getTableDescriptor(fields,
						dataset.isPersisted() ? dataset.getPersistTableName() : dataset.getFlatTableName(), dataset.getDataSourceForReading());
			} catch (Exception e) {
				throw new SpagoBIEngineRuntimeException("Error while getting persistence table's descriptor", e);
			}
		}
		return descriptor;
	}

	protected String getPersistenceTableName() {
		logger.debug("IN");
		String temporaryTableNameRoot = (String) this.getEnv().get(SpagoBIConstants.TEMPORARY_TABLE_ROOT_NAME);
		logger.debug("Temporary table name root specified on the environment : [" + temporaryTableNameRoot + "]");
		// if temporaryTableNameRadix is not specified on the environment,
		// create a new name using the user profile
		if (temporaryTableNameRoot == null) {
			logger.debug("Temporary table name root not specified on the environment, creating a new one using user identifier ...");
			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			temporaryTableNameRoot = userProfile.getUserId().toString();
		}
		temporaryTableNameRoot = "qbe_" + temporaryTableNameRoot;
		logger.debug("Temporary table root name : [" + temporaryTableNameRoot + "]");
		String temporaryTableNameComplete = TemporaryTableManager.getTableName(temporaryTableNameRoot);
		logger.debug("Temporary table name : [" + temporaryTableNameComplete + "]. Putting it into the environment");
		this.getEnv().put(SpagoBIConstants.TEMPORARY_TABLE_NAME, temporaryTableNameComplete);
		logger.debug("OUT : temporaryTableName = [" + temporaryTableNameComplete + "]");
		return temporaryTableNameComplete;
	}

	protected IDataSetTableDescriptor persistDataSetWithTemporaryTable(IDataSet dataset, String tableName, IDataSource dataSource) {

		HttpSession session = this.getHttpSession();
		synchronized (session) { // we synchronize this block in order to avoid concurrent requests

			String signature = dataset.getSignature();
			logger.debug("Dataset signature : " + signature);
			if (signature.equals(TemporaryTableManager.getLastDataSetSignature(tableName))) {
				// signature matches: no need to create a TemporaryTable
				logger.debug("Signature matches: no need to create a TemporaryTable");
				return TemporaryTableManager.getLastDataSetTableDescriptor(tableName);
			}

			// drop the temporary table if one exists
			try {
				logger.debug("Signature does not match: dropping TemporaryTable " + tableName + " if it exists...");
				TemporaryTableManager.dropTableIfExists(tableName, dataSource);
			} catch (Exception e) {
				logger.error("Impossible to drop the temporary table with name " + tableName, e);
				throw new SpagoBIEngineRuntimeException("Impossible to drop the temporary table with name " + tableName, e);
			}

			IDataSetTableDescriptor td = null;

			try {
				logger.debug("Persisting dataset ...");

				td = dataset.persist(tableName, dataSource);
				this.recordTemporaryTable(tableName, dataSource);

				logger.debug("Dataset persisted");
			} catch (Throwable t) {
				logger.error("Error while persisting dataset", t);
				throw new SpagoBIRuntimeException("Error while persisting dataset", t);
			}

			logger.debug("Dataset persisted successfully. Table descriptor : " + td);
			TemporaryTableManager.setLastDataSetSignature(tableName, signature);
			TemporaryTableManager.setLastDataSetTableDescriptor(tableName, td);
			return td;

		}
	}

	protected void recordTemporaryTable(String tableName, IDataSource dataSource) {
		String attributeName = TemporaryTableRecorder.class.getName();
		TemporaryTableRecorder recorder = (TemporaryTableRecorder) this.getHttpSession().getAttribute(attributeName);
		if (recorder == null) {
			recorder = new TemporaryTableRecorder();
		}
		recorder.addTemporaryTable(new TemporaryTable(tableName, dataSource));
		this.getHttpSession().setAttribute(attributeName, recorder);
	}

	public IDataSource getDataSourceForWriting() {
		String schema = null;
		String attrname = null;

		String datasourceLabel = this.getAttributeAsString(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL);

		if (datasourceLabel != null) {
			IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel(datasourceLabel);
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
			return dataSource;
		}

		return null;
	}
}