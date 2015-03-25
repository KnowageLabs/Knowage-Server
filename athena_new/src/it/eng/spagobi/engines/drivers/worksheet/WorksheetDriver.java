/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.worksheet;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.execution.service.GetMetadataAction;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.serializer.MetadataJSONSerializer;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;



/**
 * Driver Implementation (IEngineDriver Interface) for Worksheet External Engine.
 */
public class WorksheetDriver extends AbstractDriver implements IEngineDriver {

	static private Logger logger = Logger.getLogger(WorksheetDriver.class);

	public final static String PARAM_SERVICE_NAME = "ACTION_NAME";
	public final static String PARAM_NEW_SESSION = "NEW_SESSION";
	public final static String QUERY = "QUERY";
	public final static String MASSIVE_EXPORT_PARAM_ACTION_NAME = "MASSIVE_EXPORT_WORKSHEET_ENGINE_START_ACTION";
	public final static String PARAM_ACTION_NAME = "WORKSHEET_ENGINE_START_ACTION";


	public final static String METADATA_AND_METACONTENT = "METADATA_AND_METACONTENT";
	public final static String PARAMETERS = "PARAMETERS";


	public final static String FORM_VALUES = "FORM_VALUES";

	public final static String CURRENT_VERSION = "1";
	public final static String ATTRIBUTE_VERSION = "version";
	public final static String TAG_WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public final static String TAG_WORKSHEET = "WORKSHEET";
	public final static String DATAMART = "DATAMART";
	public final static String TAG_QBE = "QBE";
	public final static String TAG_QBE_COMPOSITE = "COMPOSITE-QBE";
	public final static String TAG_SMART_FILTER = EngineConstants.SMART_FILTER_TAG;

	public static final String MIME_TYPE = "MIME_TYPE";
	public final static String EXPORT_MIME_TYPE_XLS = 	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public final static String EXPORT_MIME_TYPE_PDF = 	"application/pdf";

	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 *
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param analyticalDocument the biobject
	 *
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object analyticalDocument, IEngUserProfile profile, String roleName) {
		Map parameters;
		BIObject biObject;

		logger.debug("IN");

		try {
			Assert.assertNotNull(analyticalDocument, "Input parameter [analyticalDocument] cannot be null");
			Assert.assertTrue((analyticalDocument instanceof BIObject), "Input parameter [analyticalDocument] cannot be an instance of [" + analyticalDocument.getClass().getName()+ "]");

			biObject = (BIObject)analyticalDocument;

			parameters = new Hashtable();
			parameters = getRequestParameters(biObject);
			parameters = applySecurity(parameters, profile);
			parameters = applyService(parameters, biObject, profile);
			parameters = applyDatasourceForWriting(parameters, biObject);
		} finally {
			logger.debug("OUT");
		}

		return parameters;
	}

	private Map applyDatasourceForWriting(Map parameters, BIObject biObject) {
		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException(
					"Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parameters.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}
		return parameters;
	}

	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 *
	 * @param analyticalDocumentSubObject SubObject to execute
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param analyticalDocument the object
	 *
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object analyticalDocument, Object analyticalDocumentSubObject, IEngUserProfile profile, String roleName) {

		Map parameters;
		BIObject biObject;
		SubObject subObject;

		logger.debug("IN");

		try{
			Assert.assertNotNull(analyticalDocument, "Input parameter [analyticalDocument] cannot be null");
			Assert.assertTrue((analyticalDocument instanceof BIObject), "Input parameter [analyticalDocument] cannot be an instance of [" + analyticalDocument.getClass().getName()+ "]");
			biObject = (BIObject)analyticalDocument;

			if(analyticalDocumentSubObject == null) {
				logger.warn("Input parameter [subObject] is null");
				return getParameterMap(analyticalDocument, profile, roleName);
			}
			Assert.assertTrue((analyticalDocumentSubObject instanceof SubObject), "Input parameter [subObjectDetail] cannot be an instance of [" + analyticalDocumentSubObject.getClass().getName()+ "]");
			subObject = (SubObject) analyticalDocumentSubObject;

			parameters = getRequestParameters(biObject);

			parameters.put("nameSubObject",  subObject.getName() != null? subObject.getName(): "" );
			parameters.put("descriptionSubObject", subObject.getDescription() != null? subObject.getDescription(): "");
			parameters.put("visibilitySubObject", subObject.getIsPublic().booleanValue()?"Public":"Private" );
			parameters.put("subobjectId", subObject.getId());

			parameters = applySecurity(parameters, profile);
			parameters = applyService(parameters, biObject, profile);
			parameters = applyDatasourceForWriting(parameters, biObject);
			parameters.put("isFromCross", "false");

		} finally {
			logger.debug("OUT");
		}
		return parameters;

	}

	/**
	 * Starting from a BIObject extracts from it the map of the paramaeters for the
	 * execution call
	 * @param biObject BIObject to execute
	 * @return Map The map of the execution call parameters
	 */
	private Map getRequestParameters(BIObject biObject) {
		logger.debug("IN");

		Map parameters;
		ObjTemplate template;
		IBinContentDAO contentDAO;
		byte[] content;

		logger.debug("IN");

		parameters = null;

		try {
			parameters = new Hashtable();
			template = this.getTemplate(biObject);

			try {
				contentDAO = DAOFactory.getBinContentDAO();
				Assert.assertNotNull(contentDAO, "Impossible to instantiate contentDAO");

				content = contentDAO.getBinContent(template.getBinId());
				Assert.assertNotNull(content, "Template content cannot be null");
			} catch (Throwable t){
				throw new RuntimeException("Impossible to load template content for document [" + biObject.getLabel()+ "]", t);
			}

			appendRequestParameter(parameters, "document", biObject.getId().toString());
			appendAnalyticalDriversToRequestParameters(biObject, parameters);
			addBIParameterDescriptions(biObject, parameters);

			addMetadataAndContent(biObject, parameters);

		} finally {
			logger.debug("OUT");
		}

		return parameters;
	}



	/**
	 * Add into the parameters map the BIObject's BIParameter names and values
	 * @param biobj BIOBject to execute
	 * @param pars Map of the parameters for the execution call
	 * @return Map The map of the execution call parameters
	 */
	private Map appendAnalyticalDriversToRequestParameters(BIObject biobj, Map pars) {
		logger.debug("IN");

		if(biobj==null) {
			logger.warn("BIObject parameter null");
			return pars;
		}

		ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
		if(biobj.getBiObjectParameters() != null){
			BIObjectParameter biobjPar = null;
			for(Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();){
				try {
					biobjPar = (BIObjectParameter)it.next();
					String value = parValuesEncoder.encode(biobjPar);
					pars.put(biobjPar.getParameterUrlName(), value);
					logger.debug("Add parameter:"+biobjPar.getParameterUrlName()+"/"+value);
				} catch (Exception e) {
					logger.error("Error while processing a BIParameter",e);
				}
			}
		}

		logger.debug("OUT");
		return pars;
	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param biobject The BIOBject to edit
	 * @param profile the profile
	 *
	 * @return the edits the document template build url
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param biobject  The BIOBject to edit
	 * @param profile the profile
	 *
	 * @return the new document template build url
	 *
	 * @throws InvalidOperationRequest the invalid operation request
	 */
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile)
	throws InvalidOperationRequest {
		logger.warn("Function not implemented");
		throw new InvalidOperationRequest();
	}



	public String updateWorksheetTemplate(String workSheetDef, String workSheetQuery, String smartFilterValues, String originalWorksheetTempl) throws SourceBeanException{
		SourceBean templateSB = new SourceBean(TAG_WORKSHEET);
		templateSB.setAttribute(ATTRIBUTE_VERSION, CURRENT_VERSION);
		SourceBean previous = SourceBean.fromXMLString( originalWorksheetTempl );

		// from version 0 to version 1 worksheet change compensation: on version 0 the
		// worksheet definition was inside QBE tag; on version 1 the QBE tag is inside
		// WORKSHEET tag
		if (previous.getName().equalsIgnoreCase(TAG_QBE)
				|| previous.getName().equalsIgnoreCase(TAG_QBE_COMPOSITE)
				|| previous.getName().equalsIgnoreCase(TAG_SMART_FILTER)) {

			if (previous.containsAttribute(TAG_WORKSHEET_DEFINITION)) {
				previous.delAttribute(TAG_WORKSHEET_DEFINITION);
			}
			templateSB.setAttribute(previous);
			SourceBean wk_def_sb = new SourceBean(TAG_WORKSHEET_DEFINITION);
			wk_def_sb.setCharacters(workSheetDef);
			templateSB.setAttribute(wk_def_sb);

			if(workSheetQuery!=null && !workSheetQuery.equals("") ){
				SourceBean query_sb = new SourceBean(QUERY);
				query_sb.setCharacters(workSheetQuery);
				previous.updAttribute(query_sb);
			}

			if(smartFilterValues!=null && !smartFilterValues.equals("")){
				SourceBean smartFilterValuesSB = new SourceBean(FORM_VALUES);
				smartFilterValuesSB.setCharacters(smartFilterValues);
				previous.updAttribute(smartFilterValuesSB);
			}

		} else {

			SourceBean qbeSB = null;

			if (previous.containsAttribute(TAG_QBE)) {
				qbeSB = (SourceBean) previous.getAttribute(TAG_QBE);
			} else if (previous.containsAttribute(TAG_QBE_COMPOSITE)) {
				qbeSB = (SourceBean) previous.getAttribute(TAG_QBE_COMPOSITE);
			} else if (previous.containsAttribute(TAG_SMART_FILTER)) {
				qbeSB = (SourceBean) previous.getAttribute(TAG_SMART_FILTER);
			}

			if (qbeSB != null) {
				templateSB.setAttribute(qbeSB);
			}

			SourceBean wk_def_sb = new SourceBean(TAG_WORKSHEET_DEFINITION);
			wk_def_sb.setCharacters(workSheetDef);
			templateSB.setAttribute(wk_def_sb);

			if(qbeSB != null && workSheetQuery!=null && !workSheetQuery.equals("") ){
				SourceBean query_sb = new SourceBean(QUERY);
				query_sb.setCharacters(workSheetQuery);
				qbeSB.updAttribute(query_sb);
			}

			if(qbeSB != null && smartFilterValues!=null && !smartFilterValues.equals("")){
				SourceBean smartFilterValuesSB = new SourceBean(FORM_VALUES);
				smartFilterValuesSB.setCharacters(smartFilterValues);
				qbeSB.updAttribute(smartFilterValuesSB);
			}

		}



		String template = templateSB.toXML(false);
		return template;
	}

	public String composeWorksheetTemplate(String workSheetDef, String workSheetQuery, String smartFilterValues, String originalQbeTempl) throws SourceBeanException{
		SourceBean templateSB = new SourceBean(TAG_WORKSHEET);
		templateSB.setAttribute(ATTRIBUTE_VERSION, CURRENT_VERSION);
		SourceBean confSB = SourceBean.fromXMLString( originalQbeTempl );
		// from version 0 to version 1 worksheet change compensation: on version 0 the
		// worksheet definition was inside QBE tag; on version 1 the QBE tag is inside
		// WORKSHEET tag
		if (confSB.getName().equalsIgnoreCase(TAG_QBE)
				|| confSB.getName().equalsIgnoreCase(TAG_QBE_COMPOSITE)
				|| confSB.getName().equalsIgnoreCase(TAG_SMART_FILTER)) {

			if (confSB.containsAttribute(TAG_WORKSHEET_DEFINITION)) {
				confSB.delAttribute(TAG_WORKSHEET_DEFINITION);
			}
			templateSB.setAttribute(confSB);
			SourceBean wk_def_sb = new SourceBean(TAG_WORKSHEET_DEFINITION);
			wk_def_sb.setCharacters(workSheetDef);
			templateSB.setAttribute(wk_def_sb);

			if(workSheetQuery!=null && !workSheetQuery.equals("") ){
				SourceBean query_sb = new SourceBean(QUERY);
				query_sb.setCharacters(workSheetQuery);
				confSB.updAttribute(query_sb);
			}

			if(smartFilterValues!=null && !smartFilterValues.equals("")){
				SourceBean smartFilterValuesSB = new SourceBean(FORM_VALUES);
				smartFilterValuesSB.setCharacters(smartFilterValues);
				confSB.updAttribute(smartFilterValuesSB);
			}

		} else {

			SourceBean qbeSB = null;

			if (confSB.containsAttribute(TAG_QBE)) {
				qbeSB = (SourceBean) confSB.getAttribute(TAG_QBE);
			} else if (confSB.containsAttribute(TAG_QBE_COMPOSITE)) {
				qbeSB = (SourceBean) confSB.getAttribute(TAG_QBE_COMPOSITE);
			} else if (confSB.containsAttribute(TAG_SMART_FILTER)) {
				qbeSB = (SourceBean) confSB.getAttribute(TAG_SMART_FILTER);
			}

			if (qbeSB != null) {
				templateSB.setAttribute(qbeSB);
				if(workSheetQuery!=null && !workSheetQuery.equals("") ){
					SourceBean query_sb = new SourceBean(QUERY);
					query_sb.setCharacters(workSheetQuery);
					qbeSB.updAttribute(query_sb);
				}

				if(smartFilterValues!=null && !smartFilterValues.equals("")){
					SourceBean smartFilterValuesSB = new SourceBean(FORM_VALUES);
					smartFilterValuesSB.setCharacters(smartFilterValues);
					qbeSB.updAttribute(smartFilterValuesSB);
				}
			}

			SourceBean wk_def_sb = new SourceBean(TAG_WORKSHEET_DEFINITION);
			wk_def_sb.setCharacters(workSheetDef);
			templateSB.setAttribute(wk_def_sb);
		}

		String template = templateSB.toXML(false);
		return template;
	}

	public String createNewWorksheetTemplate(String worksheetDefinition, String modelName, String query) throws SourceBeanException {
		SourceBean templateSB = new SourceBean(TAG_WORKSHEET);
		templateSB.setAttribute(ATTRIBUTE_VERSION, CURRENT_VERSION);
		SourceBean worksheetDefinitionSB = new SourceBean(TAG_WORKSHEET_DEFINITION);
		worksheetDefinitionSB.setCharacters(worksheetDefinition);
		templateSB.setAttribute(worksheetDefinitionSB);
		if(modelName!=null && !modelName.equals("")){
			// case when starting from a model
			SourceBean templateQBE = new SourceBean(TAG_QBE);
			SourceBean templateDatamart = new SourceBean(DATAMART);
			templateDatamart.setAttribute("name", modelName);
			templateQBE.setAttribute(templateDatamart);
			SourceBean templateQuery =  new SourceBean(QUERY);
			templateQuery.setCharacters(query);
			templateQBE.setAttribute(templateQuery);
			templateSB.setAttribute(templateQBE);
		} else 	if (query != null && !query.trim().equals("")) {
			// case when starting from a dataset
			SourceBean qbeSB = new SourceBean(TAG_QBE);
			SourceBean queryDefinitionSB = new SourceBean(QUERY);
			queryDefinitionSB.setCharacters(query);
			qbeSB.setAttribute(queryDefinitionSB);
			templateSB.setAttribute(qbeSB);
		}
		String template = templateSB.toXML(false);
		return template;
	}


	private Map applyService(Map parameters, BIObject biObject, IEngUserProfile profile) {

		logger.debug("IN");

		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");

			String userId=(String)profile.getUserUniqueIdentifier();
			if(((UserProfile)profile).isSchedulerUser(userId)){


				// if among parameters there is outputType parameter set MIME type that is required by export Action
				if(parameters.get("outputType") != null){
					String mimeType = EXPORT_MIME_TYPE_XLS;
					String outputType = parameters.get("outputType").toString();
					logger.debug("Export in "+outputType);
					if(outputType.equalsIgnoreCase("PDF")) mimeType = EXPORT_MIME_TYPE_PDF;
					else if(outputType.equalsIgnoreCase("XLS")) mimeType = EXPORT_MIME_TYPE_XLS;
					else { // default is XLS
					}
					logger.debug("Mime type to export is "+mimeType);
					parameters.put(MIME_TYPE, mimeType);
				}
				else{
					logger.debug("Mime type to export is defalt application/xls");
					parameters.put(MIME_TYPE, EXPORT_MIME_TYPE_XLS);
				}

				parameters.put(PARAM_SERVICE_NAME, MASSIVE_EXPORT_PARAM_ACTION_NAME);
			}
			else{
				parameters.put(PARAM_SERVICE_NAME, PARAM_ACTION_NAME);
			}
			parameters.put(PARAM_NEW_SESSION, "TRUE");

		} catch(Throwable t) {
			throw new RuntimeException("Impossible to guess from template extension the engine startup service to call");
		} finally {
			logger.debug("OUT");
		}

		return parameters;


	}


	private ObjTemplate getTemplate(BIObject biObject) {
		ObjTemplate template;
		IObjTemplateDAO templateDAO;

		logger.debug("IN");

		try {
			Assert.assertNotNull(biObject, "Input [biObject] cannot be null");

			templateDAO = DAOFactory.getObjTemplateDAO();
			Assert.assertNotNull(templateDAO, "Impossible to instantiate templateDAO");

			template = templateDAO.getBIObjectActiveTemplate( biObject.getId() );
			Assert.assertNotNull(template, "Loaded template cannot be null");

			logger.debug("Active template [" + template.getName() + "] of document [" + biObject.getLabel() + "] loaded succesfully");
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to load template for document [" + biObject.getLabel()+ "]", t);
		} finally {
			logger.debug("OUT");
		}

		return template;
	}

	private void appendRequestParameter(Map parameters, String pname, String pvalue) {
		parameters.put(pname, pvalue);
		logger.debug("Added parameter [" + pname + "] with value [" + pvalue + "] to request parameters list");
	}


//	private void addParametersContent(BIObject biobj, Map pars) {
//		logger.debug("IN");
//		JSONArray parsArray = null;
//		if(biobj.getBiObjectParameters() != null){
//			ParameterValuesEncoder parValuesEncoder = new ParameterValuesEncoder();
//			for(Iterator it = biobj.getBiObjectParameters().iterator(); it.hasNext();){
//				BIObjectParameter biobjPar = (BIObjectParameter)it.next();
//				try {
//					String name = biobjPar.getLabel();
//					String value = parValuesEncoder.encode(biobjPar);
//					JSONObject objectJSON = new JSONObject();
//					objectJSON.put("name", name);
//					objectJSON.put("value", value);
//					if(parsArray == null){
//						parsArray = new JSONArray();
//					}
//					parsArray.put(objectJSON);
//					logger.debug("Add to metadata parameter:"+biobjPar.getParameterUrlName()+"/"+value);
//				} catch (Exception e) {
//					logger.error("Error while processing metadata for BIParameter "+biobjPar.getLabel(),e);
//				}
//			}
//			pars.put(PARAMETERS, parsArray);
//
//		}
//
//		logger.debug("OUT");
//	}


	private void addMetadataAndContent(BIObject biObject, Map pars){
		logger.debug("IN");
		try{
			if(biObject.getObjMetaDataAndContents() != null){
				MetadataJSONSerializer jsonSerializer = new MetadataJSONSerializer();
				JSONArray metaArray = new JSONArray();
				Locale locale = getLocale();

				Domain typeDom = DAOFactory.getDomainDAO().loadDomainById(biObject.getBiObjectTypeID());
				MessageBuilder msgBuild = new MessageBuilder();
				// fill thecnical metadata

				JSONObject labelJSON = new JSONObject();
				String label = msgBuild.getMessage(GetMetadataAction.LABEL, locale);
				labelJSON.put("meta_name", label);
				labelJSON.put("meta_content", biObject.getLabel());
				labelJSON.put("meta_type", "GENERAL_META");

				JSONObject nameJSON = new JSONObject();
				String name = msgBuild.getMessage(GetMetadataAction.NAME, locale);
				nameJSON.put("meta_name", name);
				nameJSON.put("meta_content", biObject.getName());
				nameJSON.put("meta_type", "GENERAL_META");

				JSONObject typeJSON = new JSONObject();
				String typeL = msgBuild.getMessage(GetMetadataAction.TYPE, locale);
				String valueType = msgBuild.getMessage(typeDom.getValueName(), locale);
				typeJSON.put("meta_name", typeL);
				typeJSON.put("meta_content", valueType);
				typeJSON.put("meta_type", "GENERAL_META");

				JSONObject engineJSON = new JSONObject();
				String engine = msgBuild.getMessage(GetMetadataAction.ENG_NAME, locale);
				engineJSON.put("meta_name", engine);
				engineJSON.put("meta_content", biObject.getEngine().getName());
				engineJSON.put("meta_type", "GENERAL_META");

				metaArray.put(labelJSON);
				metaArray.put(nameJSON);
				metaArray.put(typeJSON);
				metaArray.put(engineJSON);


				for (Iterator iterator = biObject.getObjMetaDataAndContents().iterator(); iterator.hasNext();) {
					DocumentMetadataProperty type = (DocumentMetadataProperty) iterator.next();
					Object o = jsonSerializer.serialize(type, locale);
					metaArray.put(o);
					logger.debug("Metadata serialzied "+o);
				}
				logger.debug("Metadata array serialzied "+metaArray);
				pars.put(METADATA_AND_METACONTENT, metaArray);
			}
			else{
				logger.debug("no meta and metacontent defined");
			}



		}
		catch (Exception e) {
			logger.error("Impossibile to serialize metadata and metacontent for object with label "+biObject.getLabel(), e);
			throw new RuntimeException("Impossibile to serialize metadata and metacontent for object with label "+biObject.getLabel(), e);
		}

		logger.debug("OUT");
	}





	private Locale getLocale() {
	logger.debug("IN");
	try {
		Locale locale = null;

		if(RequestContainer.getRequestContainer() != null){
			RequestContainer requestContainer = RequestContainer.getRequestContainer();
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
			String language = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
			locale = new Locale(language, country);
		}
		else{
			locale = GeneralUtilities.getDefaultLocale();
		}
		return locale;
	} catch (Exception e) {
		logger.error("Error while getting locale; using default one", e);
		return GeneralUtilities.getDefaultLocale();
	} finally  {
		logger.debug("OUT");
	}
}



}

