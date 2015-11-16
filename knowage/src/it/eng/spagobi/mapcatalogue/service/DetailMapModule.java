/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spago.validation.coordinator.ValidationCoordinator;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.mapcatalogue.bo.GeoFeature;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;
import it.eng.spagobi.mapcatalogue.bo.GeoMapFeature;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoFeaturesDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoMapFeaturesDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoMapsDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

/**
 * Spago Module which executes the map producing request  
 */
public class DetailMapModule extends AbstractHttpModule {
	public static final String MODULE_PAGE = "DetailMapPage";
	public static final String MOD_SAVE = "SAVE";
	public static final String MOD_SAVEBACK = "SAVEBACK";
	public static final String MOD_NO_SAVE = "NO_SAVE";
	public static final String MOD_GET_TAB_DETAIL = "GET_TAB_DETAIL";
	public static final String MOD_DEL_MAP_FEATURE = "DEL_MAP_FEATURE";
	public static final String MOD_RETURN_FROM_LOOKUP = "RETURN_FROM_LOOKUP";
	public static final String MOD_DOWNLOAD_MAP = "DOWNLOAD_MAP";
	
	static private Logger logger = Logger.getLogger(DetailMapModule.class);
	
	private String modalita = "";
	private byte[] content = null;
	
	
	/**
	 * Method called automatically by Spago framework when the action is invoked.
	 * The method search into the request two parameters
	 * <ul>
	 * <li>message: a message which contains the type of the request</li>
	 * </ul>
	 * 
	 * @param serviceRequest the Spago request SourceBean
	 * @param serviceResponse the Spago response SourceBean
	 * 
	 * @throws Exception the exception
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) throws Exception {		
		
		EMFErrorHandler errorHandler = getErrorHandler();
		
//		if(ChannelUtilities.isPortletRunning()){
//			if(PortletUtilities.isMultipartRequest()) {
//				serviceRequest = ChannelUtilities.getSpagoRequestFromMultipart();
//				fillRequestContainer(serviceRequest, errorHandler);		
//			}
//		}
		String message = (String) serviceRequest.getAttribute("MESSAGEDET");
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG,  "begin of detail Map modify/visualization service with message =" + message);
		
		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);				
				TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG,  "The message parameter is null");
				throw userError;
			}
			if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INS) || 
				message.trim().equalsIgnoreCase( SpagoBIConstants.DETAIL_MOD)){	
				ValidationCoordinator.validate("PAGE", "DetailMapPost", this);
			}
			if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_SELECT)) {
				getDetailMap(serviceRequest, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {
				modDetailMap(serviceRequest, SpagoBIConstants.DETAIL_MOD, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_NEW)) {
				newDetailMap(serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
				modDetailMap(serviceRequest, SpagoBIConstants.DETAIL_INS, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_DEL)) {
				delDetailMap(serviceRequest, SpagoBIConstants.DETAIL_DEL, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(MOD_DEL_MAP_FEATURE)) {
				delRelMapFeature(serviceRequest, serviceResponse);				
			} else if (message.trim().equalsIgnoreCase(MOD_RETURN_FROM_LOOKUP)) {
				insRelMapFeature(serviceRequest, serviceResponse);				
			}else if (message.trim().equalsIgnoreCase(MOD_DOWNLOAD_MAP)) {
				downloadFile(serviceRequest);				
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
	}
	
	
	/**
	 * sends an error message to the client
	 * @param out The servlet output stream
	 */
	private void sendError(ServletOutputStream out)  {
		try{
			out.write("<html>".getBytes());
			out.write("<body>".getBytes());
			out.write("<br/><br/><center><h2><span style=\"color:red;\">Unable to produce map</span></h2></center>".getBytes());
			out.write("</body>".getBytes());
			out.write("</html>".getBytes());
		} catch (Exception e) {
			TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, 
								"GeoAction :: sendError : " +
								"Unable to write into output stream ", e);
		}
	}
	

/**
 * Gets the detail of an map choosed by the user from the 
 * maps list. It reaches the key from the request and asks to the DB all detail
 * map information, by calling the method <code>loadMapByID</code>.
 *   
 * @param key The choosed map id key
 * @param response The response Source Bean
 * @throws EMFUserError If an exception occurs
 */   
private void getDetailMap(SourceBean request, SourceBean response) throws EMFUserError {
	try {		 									
		GeoMap map = DAOFactory.getSbiGeoMapsDAO().loadMapByID(new Integer((String)request.getAttribute("ID")));		
		getTabDetails(request, response);		
		this.modalita = SpagoBIConstants.DETAIL_MOD;
		if (request.getAttribute("SUBMESSAGEDET") != null &&
			((String)request.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK))
		{
			response.setAttribute("loopback", "true");
			return;
		}
		response.setAttribute("modality", modalita);
		response.setAttribute("mapObj", map);
	} catch (Exception ex) {
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());	
		HashMap params = new HashMap();
		params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
		throw new EMFUserError(EMFErrorSeverity.ERROR, 5011, new Vector(), params);
	}
}
 /**
 * Inserts/Modifies the detail of an map according to the user request. 
 * When a map is modified, the <code>modifyMap</code> method is called; when a new
 * map is added, the <code>insertMap</code>method is called. These two cases are 
 * differentiated by the <code>mod</code> String input value .
 * 
 * @param request The request information contained in a SourceBean Object
 * @param mod A request string used to differentiate insert/modify operations
 * @param response The response SourceBean 
 * @throws EMFUserError If an exception occurs
 * @throws SourceBeanException If a SourceBean exception occurs
 */
private void modDetailMap(SourceBean serviceRequest, String mod, SourceBean serviceResponse)
	throws EMFUserError, SourceBeanException {
	RequestContainer requestContainer = this.getRequestContainer();	
	ResponseContainer responseContainer = this.getResponseContainer();	
	SessionContainer session = requestContainer.getSessionContainer();
	SessionContainer permanentSession = session.getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	try {
		
		
		ISbiGeoMapsDAO daoGeoMaps=DAOFactory.getSbiGeoMapsDAO();
		daoGeoMaps.setUserProfile(profile);
		
		GeoMap mapNew = recoverMapDetails(serviceRequest);
		HashMap<String, String> logParam = new HashMap();
		logParam.put("MAP_NAME",mapNew.getName());
		
		EMFErrorHandler errorHandler = getErrorHandler();
		 
		// if there are some validation errors into the errorHandler does not write into DB
		Collection errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {
			Iterator iterator = errors.iterator();
			
			while (iterator.hasNext()) {
				Object error = iterator.next();
				if (error instanceof EMFValidationError) {
					serviceResponse.setAttribute("mapObj", mapNew);
					serviceResponse.setAttribute("modality", mod);
					if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD", logParam, "KO");
					} else {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", logParam, "KO");
					}
					return;
				}
			}
		}
		
		if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {			
			//if a map with the same name not exists on db ok else error
			if (daoGeoMaps.loadMapByName(mapNew.getName()) != null){
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 5005, new Vector(), params );
				getErrorHandler().addError(error);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD", logParam, "ERR");
				return;
			}	 		
			/* The activity INSERT consists in:
			 * - insert a map (SBI_GEO_MAPS), 
			 * - insert of the features (SBI_GEO_FEATURES) through the 	method 'loadUpdateMapFeatures'
			 * - insert of the relations (SBI_GEO_MAP_FEATURES) through the method 'loadUpdateMapFeatures'
			 * (all objects are had taken from the template file)
			 */
			//DAOFactory.getSbiGeoMapsDAO().insertMap(mapNew);
			daoGeoMaps.insertMap(mapNew, content);
			loadUpdateMapFeatures(mapNew,profile);
			GeoMap tmpMap = daoGeoMaps.loadMapByName(mapNew.getName());
			mapNew.setMapId(tmpMap.getMapId());
			mapNew.setBinId(tmpMap.getBinId());
			serviceResponse.setAttribute("mapObj", mapNew);
			serviceResponse.setAttribute("modality", SpagoBIConstants.DETAIL_MOD);
			
			getTabDetails(serviceRequest, serviceResponse);
			
			if (((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK))
			{
				serviceResponse.setAttribute("loopback", "true");
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD", logParam, "OK");
				return;
			}
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD", logParam, "OK");
			return;

		} else {
			/* The activity UPDATE consists in:
			 * - update of a map (SBI_GEO_MAPS), 
			 * - update of the relations (SBI_GEO_MAP_FEATURES) through the method 'loadUpdateMapFeatures', eventually if 
			 *   there are new features those will be inserted. 
			 * (all objects had taken from the template file)
			 */
			//if content is null is because the user has modified the detail of map but not the content file (not upload)
			if (content == null){
				content = DAOFactory.getBinContentDAO().getBinContent(new Integer(mapNew.getBinId()));
			}
			List lstOldFeatures = DAOFactory.getSbiGeoMapFeaturesDAO().loadFeaturesByMapId(new Integer(mapNew.getMapId()));
			//update map
			daoGeoMaps.modifyMap(mapNew, content);			
			//update features
			List lstNewFeatures = loadUpdateMapFeatures(mapNew,profile);
			logger.debug("Loaded " +lstNewFeatures.size()+ " features form svg file." );
			 // If in the new file svg there aren't more some feature, the user can choose if erase theme relations or not.
			List lstFeaturesDel = new ArrayList();
			
			for (int i=0; i<lstOldFeatures.size(); i++){					
				if (!(lstNewFeatures.contains(((GeoFeature)lstOldFeatures.get(i)).getName())))
					lstFeaturesDel.add(((GeoFeature)lstOldFeatures.get(i)).getName());
			}
			if (lstFeaturesDel.size() > 0){
				serviceResponse.setAttribute("lstFeaturesOld",lstFeaturesDel);
				serviceResponse.setAttribute("SUBMESSAGEDET", ((String)serviceRequest.getAttribute("SUBMESSAGEDET")));
				getTabDetails(serviceRequest, serviceResponse);			
				serviceResponse.setAttribute("modality", mod);
				serviceResponse.setAttribute("mapObj", mapNew);	
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", logParam, "OK");
				return;										
			}				
		}  
		if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
			((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVE)) {			
			getTabDetails(serviceRequest, serviceResponse);			
			serviceResponse.setAttribute("modality", mod);
			serviceResponse.setAttribute("mapObj", mapNew);		
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD/MODIFY", logParam, "OK");
			return;
		}
		else if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
				((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK)){
				serviceResponse.setAttribute("loopback", "true");
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD/MODIFY", logParam, "OK");
			    return;
		}					     
	} catch (EMFUserError e){
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD/MODIFY", null, "ERR");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.error("Error while saving catalogue map: " +  e.getMessage());
		HashMap params = new HashMap();
		params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
		throw new EMFUserError(EMFErrorSeverity.ERROR, e.getDescription(), new Vector(), params);
		
	}
	
	catch (Exception ex) {	
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", null, "KO");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());		
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}	
}

/**
 * Deletes a map choosed by user from the maps list.
 * 
 * @param request	The request SourceBean
 * @param mod	A request string used to differentiate delete operation
 * @param response	The response SourceBean
 * @throws EMFUserError	If an Exception occurs
 * @throws SourceBeanException If a SourceBean Exception occurs
 */
private void delDetailMap(SourceBean request, String mod, SourceBean response)
	throws EMFUserError, SourceBeanException {
	RequestContainer reqCont = getRequestContainer();
	SessionContainer sessCont = reqCont.getSessionContainer();
	SessionContainer permSess = sessCont.getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	HashMap<String, String> logParam = new HashMap();
	
	try {
		String id = (String) request.getAttribute("ID");
//		if the map is associated with any BIFeautures, delete before this associations and then delete the map
		List lstMapFeatures =  DAOFactory.getSbiGeoMapFeaturesDAO().loadFeaturesByMapId(new Integer(id));
		if (lstMapFeatures != null){
			for (int i=0; i<lstMapFeatures.size();i++){
				int featureId = ((GeoFeature)lstMapFeatures.get(i)).getFeatureId();
				GeoMapFeature tmpMapFeature = DAOFactory.getSbiGeoMapFeaturesDAO().loadMapFeatures(new Integer(id), new Integer(featureId));				
				DAOFactory.getSbiGeoMapFeaturesDAO().eraseMapFeatures(tmpMapFeature);
			}
		}
		//delete the map
		GeoMap map = DAOFactory.getSbiGeoMapsDAO().loadMapByID(new Integer(id));
		logParam.put("MAP_NAME",map.getName());
		DAOFactory.getSbiGeoMapsDAO().eraseMap(map);
		
	}   catch (EMFUserError e){
		  try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.DELETE", logParam, "ERR");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  HashMap params = new HashMap();		  
		  params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
		  throw new EMFUserError(EMFErrorSeverity.ERROR, 5010, new Vector(), params);
			
		}
	    catch (Exception ex) {	
	    	try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.DELETE", logParam, "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    ex.printStackTrace();
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}
	response.setAttribute("loopback", "true");	
	try {
		AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.DELETE", logParam, "OK");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

/**
 * Inserts a relation between the map and the feature selected
 * 
 * @param request	The request SourceBean
 * @param mod	A request string used to differentiate delete operation
 * @param response	The response SourceBean
 * @throws EMFUserError	If an Exception occurs
 * @throws SourceBeanException If a SourceBean Exception occurs
 */
private void insRelMapFeature(SourceBean request, SourceBean response)
	throws EMFUserError, SourceBeanException {
	RequestContainer reqCont = getRequestContainer();
	SessionContainer sessCont = reqCont.getSessionContainer();
	SessionContainer permSess = sessCont.getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	HashMap<String, String> logParam = new HashMap();
	try {		
		ISbiGeoMapFeaturesDAO dao=DAOFactory.getSbiGeoMapFeaturesDAO();
		dao.setUserProfile(profile);
		
		String mapId = (String) request.getAttribute("MAP_ID");
		String featureId = (String)request.getAttribute("FEATURE_ID");	
		GeoMap map = DAOFactory.getSbiGeoMapsDAO().loadMapByID(new Integer(mapId));
		EMFErrorHandler errorHandler = getErrorHandler();
		
		logParam.put("MAP_NAME",map.getName());
		// if there are some validation errors into the errorHandler does not write into DB
		Collection errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {
			Iterator iterator = errors.iterator();
			while (iterator.hasNext()) {
				Object error = iterator.next();
				if (error instanceof EMFValidationError) {
					response.setAttribute("mapObj", map);
					response.setAttribute("modality", SpagoBIConstants.DETAIL_MOD);
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", logParam, "KO");
					return;
				}
			}
		}

		//inserts the relation
		GeoMapFeature mapFeature = dao.loadMapFeatures(new Integer(mapId), new Integer(featureId));
		if (mapFeature == null){
			mapFeature = new  GeoMapFeature();
			mapFeature.setMapId(new Integer(mapId).intValue());
			mapFeature.setFeatureId(new Integer(featureId).intValue());
			mapFeature.setSvgGroup(null);
			mapFeature.setVisibleFlag(null);		
			dao.insertMapFeatures(mapFeature);
		}
		//create a List of features
		List lstAllFeatures = dao.loadFeatureNamesByMapId(new Integer(map.getMapId()));
		List lstMapFeatures = new ArrayList();

		for (int i=0; i < lstAllFeatures.size(); i ++){			
			GeoFeature aFeature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByName((String)lstAllFeatures.get(i));
			lstMapFeatures.add(aFeature);
			//for the first time sets selectedFeatureId with the first feature
			if (i==0)
				featureId = String.valueOf(aFeature.getFeatureId());
		
		}

	    response.setAttribute("lstMapFeatures",lstMapFeatures);
	    response.setAttribute("selectedFeatureId",featureId);	
	    response.setAttribute("mapObj", map);
	    response.setAttribute("modality", SpagoBIConstants.DETAIL_MOD);
	    AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", logParam, "OK");
	    
	}   catch (EMFUserError e){
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", logParam, "ERR");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  HashMap params = new HashMap();		  
		  params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
		  throw new EMFUserError(EMFErrorSeverity.ERROR, 5027, new Vector(), params);
			
		}
	    catch (Exception ex) {
	    	try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.MODIFY", null, "KO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    ex.printStackTrace();
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}	
}

/**
 * Instantiates a new <code>map<code> object when a new map insertion is required, in order
 * to prepare the page for the insertion.
 * 
 * @param response The response SourceBean
 * @throws EMFUserError If an Exception occurred
 */

private void newDetailMap(SourceBean response) throws EMFUserError {
	
	try {
		
		GeoMap map = null;
		this.modalita = SpagoBIConstants.DETAIL_INS;
		response.setAttribute("modality", modalita);
		map = new GeoMap();
		map.setMapId(-1);
		map.setDescr("");
		map.setName("");
		map.setFormat("");
		map.setBinId(-1);
		response.setAttribute("mapObj", map);
	} catch (Exception ex) {
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot prepare page for the insertion" + ex.getLocalizedMessage());		
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}
	
}


private GeoMap recoverMapDetails (SourceBean serviceRequest) throws EMFUserError, SourceBeanException, IOException  {
	GeoMap map  = new GeoMap();
	
	String idStr = (String)serviceRequest.getAttribute("ID");
	Integer id = new Integer(idStr);
	String description = (String)serviceRequest.getAttribute("DESCR");	
	String name = (String)serviceRequest.getAttribute("NAME");
	String format = (String)serviceRequest.getAttribute("FORMAT");
	Integer binId = new Integer((String)serviceRequest.getAttribute("BIN_ID"));
	
	map.setMapId(id.intValue());
	map.setName(name);
	map.setDescr(description);
	map.setFormat(format);
	map.setBinId(binId);
	
	//gets the file eventually uploaded and sets the content variable
	FileItem uploaded = (FileItem) serviceRequest.getAttribute("UPLOADED_FILE");
    String fileName = null;
    if(uploaded!=null) {
    	fileName = GeneralUtilities.getRelativeFileNames(uploaded.getName());
		if (uploaded.getSize() == 0 && ((String)serviceRequest.getAttribute("MESSAGEDET")).equals("DETAIL_INS")) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "201");
			getErrorHandler().addError(error);
			return map;
		}
		int maxSize = GeneralUtilities.getTemplateMaxSize();
		if (uploaded.getSize() > maxSize && ((String)serviceRequest.getAttribute("MESSAGEDET")).equals("DETAIL_INS")) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "202");
			getErrorHandler().addError(error);
			return map;
		}
		if (uploaded.getSize()  > 0){
	        try {
		    	content = uploaded.get();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
    }
	
	return map;
	}
	
	private List loadUpdateMapFeatures(GeoMap mapNew,IEngUserProfile profile) throws EMFUserError, Exception {
		try {
	//		through the content of a map, gets and opens the svg file and inserts a feature for every tag <g>
			GeoFeature feature = null;
			List lstHashFeatures = DAOFactory.getSbiGeoMapsDAO().getFeaturesFromSVG(content);	
			List lstFeatures = new ArrayList();
			int mapId;
			int featureId;
			for (int i=0; i < lstHashFeatures.size(); i++){				
				HashMap hFeature = (HashMap)lstHashFeatures.get(i);
				//checks if a feature with the same name yet exists in db 
				//NB: the field "id" of hashmap [hFeature] contains the name of the feature)
				feature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByName((String)hFeature.get("id"));
				 
				if (feature == null || feature.getFeatureId() == 0) {
					feature = new GeoFeature();
					feature.setName((String)hFeature.get("id"));
					feature.setDescr((String)hFeature.get("descr"));
					feature.setType((String)hFeature.get("type"));
					ISbiGeoFeaturesDAO dao=DAOFactory.getSbiGeoFeaturesDAO();
					dao.setUserProfile(profile);
					dao.insertFeature(feature);					 				
				}
				lstFeatures.add(feature.getName());
	//			for every map/feature inserts a row in join table (SBI_GEO_MAP_FEATURES)							
				//gets map_id
				mapId = mapNew.getMapId();
				if (mapId == -1 )
					mapId = ((GeoMap)DAOFactory.getSbiGeoMapsDAO().loadMapByName(mapNew.getName())).getMapId();
				
				//gets feature id				
				if (feature.getFeatureId() == 0)
					feature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByName((String)hFeature.get("id"));
				
				featureId = feature.getFeatureId();
				//gets relation
				GeoMapFeature mapFeature = DAOFactory.getSbiGeoMapFeaturesDAO().loadMapFeatures(new Integer(mapId), new Integer(featureId));
				if (mapFeature == null){	
					mapFeature = new GeoMapFeature();
					mapFeature.setMapId(mapId);
					mapFeature.setFeatureId(featureId);
					mapFeature.setSvgGroup(null);
					mapFeature.setVisibleFlag(null);
					ISbiGeoMapFeaturesDAO dao=DAOFactory.getSbiGeoMapFeaturesDAO();
					dao.setUserProfile(profile);
					dao.insertMapFeatures(mapFeature);			
				}
			}//for
			return lstFeatures;
		} catch(EMFUserError eu){
			throw new EMFUserError(eu);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 5009);	
		}
	}
	
	/**
	 * Gets and create a list of feature associated at the map. 
	 * (for tab visualization) and puts them into response
	 * @param request The response Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */
	private void getTabDetails(SourceBean request, SourceBean response) throws EMFUserError {
		RequestContainer reqCont = getRequestContainer();
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			 
			//create a List of features for tabs features
			String mapId = (String)request.getAttribute("ID");
			if (mapId == null || mapId.equals("-1"))
				mapId = String.valueOf(((GeoMap)response.getAttribute("mapObj")).getMapId());
			String selectedFeatureId  = (request.getAttribute("selectedFeatureId")==null)?"-1":(String)request.getAttribute("selectedFeatureId");
			List lstAllFeatures = new ArrayList();
			lstAllFeatures = DAOFactory.getSbiGeoMapFeaturesDAO().loadFeatureNamesByMapId(new Integer(mapId));				
		
			List lstMapFeatures = new ArrayList();			
			for (int i=0; i < lstAllFeatures.size(); i ++){
				GeoFeature aFeature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByName((String)lstAllFeatures.get(i));
				lstMapFeatures.add(aFeature);
			}
			if ((selectedFeatureId == null || selectedFeatureId.equals("-1")) && lstMapFeatures.size()>0)
				selectedFeatureId = String.valueOf(((GeoFeature)lstMapFeatures.get(0)).getFeatureId());

							 
			response.setAttribute("lstMapFeatures",lstMapFeatures);
			response.setAttribute("selectedFeatureId",selectedFeatureId);								
		} catch (Exception ex) {			
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG.ADD", null, "ERR");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 5011, new Vector(), params);
		}
	}
	
	/**
	 * Deletes relations between maps and features 
	 * @param request The response Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */
	private void delRelMapFeature(SourceBean request, SourceBean response) throws EMFUserError {
		try {
			//gets list of features for delete
			String oldFeatures =  (String)request.getAttribute("lstFeaturesOld");
			if (oldFeatures == null || oldFeatures.equals(""))			 
				oldFeatures = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByID(new Integer((String)request.getAttribute("selectedFeatureId"))).getName(); 
 
			oldFeatures = oldFeatures.replace("[","");
			oldFeatures = oldFeatures.replace("]","");
	        String[] lstOldFeatures = oldFeatures.split(",");	        		       
			if (lstOldFeatures != null){ 
				String mapId = (String)request.getAttribute("id");
				for (int i=0; i<lstOldFeatures.length; i++){
					GeoFeature aFeature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByName(((String)lstOldFeatures[i]).trim());
					GeoMapFeature aMapFeature = (GeoMapFeature)DAOFactory.getSbiGeoMapFeaturesDAO().loadMapFeatures(new Integer(mapId), new Integer(aFeature.getFeatureId()));								 
					DAOFactory.getSbiGeoMapFeaturesDAO().eraseMapFeatures(aMapFeature);
				}					
			}  
			
			if (((String)request.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK))
			{
				response.setAttribute("loopback", "true");
				return;
			}			
			else{
				request.delAttribute("selectedFeatureId");
				request.setAttribute("selectedFeatureId", "-1");
				getDetailMap(request, response);
			}
		} catch (Exception ex) {
			TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 5011, new Vector(), params);
		}
	}	
	
	/**
	 * Handle a download request of a map file. Reads the file, sends it as an http response attachment.
	 * and in the end deletes the file.
	 * @param request the http request
	 * @param response the http response
	 */
	private void downloadFile (SourceBean request) throws EMFUserError, EMFInternalError {			
			String binId =(String) request.getAttribute("BIN_ID");
			
 			//download file
 			try{	
			if (binId == null || binId.equals("0") ){
				logger.error("Cannot get content file. The identifier is null.");
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, DetailMapModule.MODULE_PAGE);
				throw new EMFUserError(EMFErrorSeverity.ERROR, "5030", new Vector(), params, "component_mapcatalogue_messages");
			}
			else{
				freezeHttpResponse();
			    //HttpServletRequest request = getHttpRequest();
			    HttpServletResponse response = getHttpResponse();
				if (content == null){
					content = DAOFactory.getBinContentDAO().getBinContent(new Integer(binId));
				}
				String fileName = "map.svg";
				response.setHeader("Content-Disposition","attachment; filename=\"" + fileName + "\";");
				response.setContentLength(content.length);
				response.getOutputStream().write(content);
				response.getOutputStream().flush();
			}
		
		} catch (IOException ioe) {
			logger.error("Cannot flush response" + ioe);
		}
	}
}