/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.network;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.execution.service.GetMetadataAction;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Domain;
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
import it.eng.spagobi.utilities.assertion.Assert;

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
public class NetworkDriver extends AbstractDriver implements IEngineDriver {

	static private Logger logger = Logger.getLogger(NetworkDriver.class);

	public final static String PARAM_SERVICE_NAME = "ACTION_NAME";
	public final static String PARAM_NEW_SESSION = "NEW_SESSION";
	public final static String PARAM_ACTION_NAME = "NETWORK_ENGINE_START_ACTION";

	
	public final static String METADATA_AND_METACONTENT = "METADATA_AND_METACONTENT";
	public final static String PARAMETERS = "PARAMETERS";


	public final static String FORM_VALUES = "FORM_VALUES";

	public final static String CURRENT_VERSION = "1";
	public final static String ATTRIBUTE_VERSION = "version";
	public final static String TAG_WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public final static String TAG_WORKSHEET = "WORKSHEET";


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
			//parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
		} finally {
			logger.debug("OUT");
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
			//parameters = addDocumentParametersInfo(parameters, biObject);
			parameters = applyService(parameters, biObject);
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

	private Map applyService(Map parameters, BIObject biObject) {
		logger.debug("IN");

		try {
			Assert.assertNotNull(parameters, "Input [parameters] cannot be null");

			parameters.put(PARAM_SERVICE_NAME, PARAM_ACTION_NAME);
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
			RequestContainer requestContainer = RequestContainer.getRequestContainer();
			SessionContainer permanentSession = requestContainer.getSessionContainer().getPermanentContainer();
			String language = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String country = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			logger.debug("Language retrieved: [" + language + "]; country retrieved: [" + country + "]");
			locale = new Locale(language, country);
			return locale;
		} catch (Exception e) {
			logger.error("Error while getting locale; using default one", e);
			return GeneralUtilities.getDefaultLocale();
		} finally  {
			logger.debug("OUT");
		}	
	}


}

