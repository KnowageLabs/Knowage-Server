/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.generic;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ParameterValuesEncoder;
import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.engines.drivers.AbstractDriver;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;



/**
 * Driver Implementation (IEngineDriver Interface) for Qbe External Engine. 
 */
public class GenericDriver extends AbstractDriver implements IEngineDriver {
	
	private final static String PARAM_NEW_SESSION = "NEW_SESSION";
    public static final String DOCUMENT_ID = "document";
    public static final String DOCUMENT_LABEL = "DOCUMENT_LABEL";
    public static final String DOCUMENT_VERSION = "DOCUMENT_VERSION";
    public static final String DOCUMENT_AUTHOR = "DOCUMENT_AUTHOR";
    public static final String DOCUMENT_NAME = "DOCUMENT_NAME";
    public static final String DOCUMENT_DESCRIPTION = "DOCUMENT_DESCRIPTION";
    public static final String DOCUMENT_IS_PUBLIC = "DOCUMENT_IS_PUBLIC";
    public static final String DOCUMENT_IS_VISIBLE = "DOCUMENT_IS_VISIBLE";
    public static final String DOCUMENT_PREVIEW_FILE = "DOCUMENT_PREVIEW_FILE";
    public static final String DOCUMENT_COMMUNITIES = "DOCUMENT_COMMUNITIES";
    public static final String DOCUMENT_FUNCTIONALITIES = "DOCUMENT_FUNCTIONALITIES";
    public static final String IS_TECHNICAL_USER = "IS_TECHNICAL_USER";

    public static final String COUNTRY = "country";
    public static final String LANGUAGE = "language";
	
	static private Logger logger = Logger.getLogger(GenericDriver.class);
	
		
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param biobject the biobject
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName) {
		logger.debug("IN");
		
		Map map = new Hashtable();
		try{
			BIObject biobj = (BIObject)biobject;
			map = getMap(biobj, profile);
			// This parameter is not required
			//map.put("query", "#");
		} catch (ClassCastException cce) {
			logger.error("The parameter is not a BIObject type", cce);
		} 
		map.put(PARAM_NEW_SESSION, "TRUE");
		map = applySecurity(map, profile);
		map = applyLocale(map);
		logger.debug("OUT");
		return map;
	}
	
	/**
	 * Returns a map of parameters which will be send in the request to the
	 * engine application.
	 * 
	 * @param subObject SubObject to execute
	 * @param profile Profile of the user
	 * @param roleName the name of the execution role
	 * @param object the object
	 * 
	 * @return Map The map of the execution call parameters
	 */
	public Map getParameterMap(Object object, Object subObject, IEngUserProfile profile, String roleName) {
	
		logger.debug("IN");
		
		if(subObject == null) {
			return getParameterMap(object, profile, roleName);
		}
		
		Map map = new Hashtable();
		try{
			BIObject biobj = (BIObject)object;
			map = getMap(biobj, profile);
			SubObject subObjectDetail = (SubObject) subObject;
			
			Integer id = subObjectDetail.getId();
			
			map.put("nameSubObject",  subObjectDetail.getName() != null? subObjectDetail.getName(): "" );
			map.put("descriptionSubObject", subObjectDetail.getDescription() != null? subObjectDetail.getDescription(): "");
			map.put("visibilitySubObject", subObjectDetail.getIsPublic().booleanValue()?"Public":"Private" );
	        map.put("subobjectId", subObjectDetail.getId());
	        //adds the format date for specific parameters date type
	        SingletonConfig config = SingletonConfig.getInstance();
			String formatSB = config.getConfigValue("SPAGOBI.DATE-FORMAT.format");
			String format = (formatSB == null) ? "dd/MM/yyyy" : formatSB;
			map.put("dateformat", format);
		} catch (ClassCastException cce) {
		    logger.error("The second parameter is not a SubObjectDetail type", cce);
		}
		
		
		map = applySecurity(map, profile);
		map = applyLocale(map);
		
		logger.debug("OUT");		
		
		return map;
		
	}
	
	
	
	     
    /**
     * Starting from a BIObject extracts from it the map of the paramaeters for the
     * execution call
     * @param biobj BIObject to execute
     * @return Map The map of the execution call parameters
     */    
	private Map getMap(BIObject biobj, IEngUserProfile profile) {
		logger.debug("IN");
		
		Map pars;
		ObjTemplate objtemplate;
		byte[] template;
		String documentId;
		String documentlabel;
		
		pars = new Hashtable();
		try {
		
			if (biobj.getDocVersion() != null){
				objtemplate = DAOFactory.getObjTemplateDAO().loadBIObjectTemplate(biobj.getDocVersion()); //specific template version (not active version)
				logger.info("Used template version id " + biobj.getDocVersion());
			}else{
				objtemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobj.getId()); //default
				logger.info("Used active template (default) ");
			}
			
			if (objtemplate == null) {
		    	throw new Exception("Template null");
		    }
			
			template = DAOFactory.getBinContentDAO().getBinContent(objtemplate.getBinId());		    
			if (template == null) {
				throw new Exception("Content of the Active template null");
			}
			
			documentId = biobj.getId().toString();
			pars.put(DOCUMENT_ID, documentId);
			logger.debug("Add " + DOCUMENT_ID + " parameter:" + documentId);
			
			documentlabel = biobj.getLabel().toString();
		    pars.put(DOCUMENT_LABEL, documentlabel);
		    logger.debug("Add " + DOCUMENT_LABEL + " parameter: " + documentlabel);
		    pars.put(DOCUMENT_VERSION, objtemplate.getId());
		    logger.debug("Add " + DOCUMENT_VERSION + " parameter: " + objtemplate.getId());
		    pars.put(DOCUMENT_AUTHOR, biobj.getCreationUser());
		    logger.debug("Add " + DOCUMENT_AUTHOR + " parameter: " + biobj.getCreationUser());
		    pars.put(DOCUMENT_NAME, biobj.getName());
		    logger.debug("Add " + DOCUMENT_NAME + " parameter: " + biobj.getName());
		    pars.put(DOCUMENT_DESCRIPTION, biobj.getDescription());
		    logger.debug("Add " + DOCUMENT_DESCRIPTION + " parameter: " + biobj.getDescription());
		    pars.put(DOCUMENT_IS_PUBLIC, biobj.isPublicDoc());
		    logger.debug("Add " + DOCUMENT_IS_PUBLIC + " parameter: " + biobj.isPublicDoc());
		    pars.put(DOCUMENT_IS_VISIBLE, biobj.isVisible());
		    logger.debug("Add " + DOCUMENT_IS_VISIBLE + " parameter: " + biobj.isVisible());
		    if (biobj.getPreviewFile() != null) 	
		    	pars.put(DOCUMENT_PREVIEW_FILE, biobj.getPreviewFile());
		    logger.debug("Add " + DOCUMENT_PREVIEW_FILE + " parameter: " + biobj.getPreviewFile());
		    List<String> communities = getCommunities(profile);
		    if (communities != null) 	{		    	
		    	 pars.put(DOCUMENT_COMMUNITIES, communities);
		    	 logger.debug("Add " + DOCUMENT_COMMUNITIES + " parameter: " +communities);
		    }		    
		    List funcs =  biobj.getFunctionalities();
		    if (funcs != null) 	{		    	
		    	pars.put(DOCUMENT_FUNCTIONALITIES,funcs);
		    	logger.debug("Add " + DOCUMENT_FUNCTIONALITIES + " parameter: " +funcs);
		    }		
		    pars.put(IS_TECHNICAL_USER, UserUtilities.isTechnicalUser(profile));
		    logger.debug("Add " + IS_TECHNICAL_USER + " parameter: " + UserUtilities.isTechnicalUser(profile));
		    
			pars = addBIParameters(biobj, pars);
			pars = addBIParameterDescriptions(biobj, pars);
        
		} catch (Exception e) {
		    logger.error("Error while recovering execution parameter map: \n" + e);
		}
		

		logger.debug("OUT");
		
		return pars;
	} 
	
	 
	 
    /**
     * Add into the parameters map the BIObject's BIParameter names and values
     * @param biobj BIOBject to execute
     * @param pars Map of the parameters for the execution call  
     * @return Map The map of the execution call parameters
     */
	private Map addBIParameters(BIObject biobj, Map pars) {
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
          if (biobjPar!=null && biobjPar.getParameterUrlName()!=null && value!=null) {
					 pars.put(biobjPar.getParameterUrlName(), value);
					 logger.debug("Add parameter:"+biobjPar.getParameterUrlName()+"/"+value);
          }  else {
             logger.warn("NO parameter are added... something is null");
          }
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

    
    private Map applyLocale(Map map) {
    	logger.debug("IN");
    	
		ConfigSingleton config = ConfigSingleton.getInstance();
		Locale portalLocale = null;
		try {
			portalLocale =  PortletUtilities.getPortalLocale();
			logger.debug("Portal locale: " + portalLocale);
		} catch (Exception e) {
		    logger.warn("Error while getting portal locale.");
		    portalLocale = MessageBuilder.getBrowserLocaleFromSpago();
		    logger.debug("Spago locale: " + portalLocale);
		}
		
		SourceBean languageSB = null;
		if(portalLocale != null && portalLocale.getLanguage() != null) {
			languageSB = (SourceBean)config.getFilteredSourceBeanAttribute("SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGE", 
					"language", portalLocale.getLanguage());
		}
		
		if(languageSB != null) {
			map.put(COUNTRY, (String)languageSB.getAttribute("country"));
			map.put(LANGUAGE, (String)languageSB.getAttribute("language"));
			logger.debug("Added parameter: country/" + (String)languageSB.getAttribute("country"));
			logger.debug("Added parameter: language/" + (String)languageSB.getAttribute("language"));
		} else {
			logger.warn("Language " + portalLocale.getLanguage() + " is not supported by SpagoBI");
			logger.warn("Portal locale will be replaced with the default lacale (country: US; language: en).");
			map.put(COUNTRY, "US");
			map.put(LANGUAGE, "en");
			logger.debug("Added parameter: country/US");
			logger.debug("Added parameter: language/en");
		}			
		
		logger.debug("OUT");
		return map;
	}
    
    private List<String> getCommunities(IEngUserProfile profile)  throws EMFUserError {    	
    	List<String> toReturn = new ArrayList();
    	List<SbiCommunity> communities = DAOFactory.getCommunityDAO().loadSbiCommunityByUser(profile.getUserUniqueIdentifier().toString());
	    if(communities != null){
			for(int i=0; i<communities.size(); i++){
				SbiCommunity community = communities.get(i);
				String functCode = community.getFunctCode();
				ILowFunctionalityDAO functDao = DAOFactory.getLowFunctionalityDAO();
				LowFunctionality funct= functDao.loadLowFunctionalityByCode(functCode, false);
				String name = community.getName();
				toReturn.add("\"" + funct.getId().toString() + "||" + funct.getCode() + "__" + name + "\"");
			}
		}
	    return toReturn;
    }

}

