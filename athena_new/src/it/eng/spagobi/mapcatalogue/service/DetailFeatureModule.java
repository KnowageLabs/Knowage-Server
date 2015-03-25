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
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.mapcatalogue.bo.GeoFeature;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;
import it.eng.spagobi.mapcatalogue.bo.GeoMapFeature;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoFeaturesDAO;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletOutputStream;


/**
 * Spago Action which executes the map producing request  
 */
public class DetailFeatureModule extends AbstractHttpModule {
	
	private String modalita = "";
	
	
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

		String message = (String) serviceRequest.getAttribute("MESSAGEDET");
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG,  "begin of detail Map modify/visualization service with message =" + message);		

		EMFErrorHandler errorHandler = getErrorHandler();
		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);				
				TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG,  "The message parameter is null");
				throw userError;
			}
			if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_SELECT)) {
				String id = (String) serviceRequest.getAttribute("ID");
				getDetailFeature(id, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {
				modDetailFeature(serviceRequest, SpagoBIConstants.DETAIL_MOD, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_NEW)) {
				newDetailFeature(serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
				modDetailFeature(serviceRequest, SpagoBIConstants.DETAIL_INS, serviceResponse);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_DEL)) {
				delDetailFeature(serviceRequest, SpagoBIConstants.DETAIL_DEL, serviceResponse);
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
								"DetailFeatureAction :: sendError : " +
								"Unable to write into output stream ", e);
		}
	}
	

/**
 * Gets the detail of a feature choosed by the user from the 
 * features list. It reaches the key from the request and asks to the DB all detail
 * feature information, by calling the method <code>loadFeatureByID</code>.
 *   
 * @param key The choosed feature id key
 * @param response The response Source Bean
 * @throws EMFUserError If an exception occurs
 */
private void getDetailFeature(String key, SourceBean response) throws EMFUserError {
	try {
		this.modalita = SpagoBIConstants.DETAIL_MOD;
		response.setAttribute("modality", modalita);
		GeoFeature feature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByID(new Integer(key));
		response.setAttribute("featureObj", feature);
	} catch (Exception ex) {
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());	
		HashMap params = new HashMap();
		params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
		throw new EMFUserError(EMFErrorSeverity.ERROR, 5023, new Vector(), params);
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
private void modDetailFeature(SourceBean request, String mod, SourceBean response)
	throws EMFUserError, SourceBeanException {
	GeoFeature feature = recoverFeatureDetails(request);
	HashMap<String, String> logParam = new HashMap();
	logParam.put("FEAUTURE_NAME",feature.getName());
	
	RequestContainer requestContainer = this.getRequestContainer();	
	ResponseContainer responseContainer = this.getResponseContainer();	
	SessionContainer session = requestContainer.getSessionContainer();
	SessionContainer permanentSession = session.getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	
	try {
		
		
		ISbiGeoFeaturesDAO dao=DAOFactory.getSbiGeoFeaturesDAO();
		dao.setUserProfile(profile);
		if (feature.getName() == null) {
			response.setAttribute("mapObj", feature);
			response.setAttribute("modality", mod);
			return;
		}

		EMFErrorHandler errorHandler = getErrorHandler();
		
		// if there are some validation errors into the errorHandler does not write into DB
		Collection errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {
			Iterator iterator = errors.iterator();
			while (iterator.hasNext()) {
				Object error = iterator.next();
				if (error instanceof EMFValidationError) {
					response.setAttribute("featureObj", feature);
					response.setAttribute("modality", mod);
					if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.ADD", logParam, "KO");
					} else {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.MODIFY", logParam, "KO");
					}
					return;
				}
			}
		}
		
		if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
			//if a feature with the same name yet exists on db: error
			if (dao.loadFeatureByName(feature.getName()) != null){
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, ListFeaturesModule.MODULE_PAGE);
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 5018, new Vector(), params );
				getErrorHandler().addError(error);
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.ADD", logParam, "ERR");
				return;
			}			
			dao.insertFeature(feature);
		} else {
			dao.modifyFeature(feature);
		}
        
	} catch (EMFUserError e){
		if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.ADD", logParam, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.MODIFY", logParam, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		HashMap params = new HashMap();
		params.put(AdmintoolsConstants.PAGE, ListMapsModule.MODULE_PAGE);
		throw new EMFUserError(EMFErrorSeverity.ERROR, 5016, new Vector(), params);
		
	}
	
	catch (Exception ex) {
		if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.ADD", logParam, "KO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.MODIFY", logParam, "KO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());		
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}
	response.setAttribute("loopback", "true");
	if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.ADD", logParam, "OK");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	} else {
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.MODIFY", logParam, "OK");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
private void delDetailFeature(SourceBean request, String mod, SourceBean response)
	throws EMFUserError, SourceBeanException {
	RequestContainer reqCont = getRequestContainer();
	SessionContainer sessCont = reqCont.getSessionContainer();
	SessionContainer permSess = sessCont.getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	try {
		String id = (String) request.getAttribute("ID");
        GeoFeature feature = DAOFactory.getSbiGeoFeaturesDAO().loadFeatureByID(new Integer(id));
        List lstMapFeature =DAOFactory.getSbiGeoMapFeaturesDAO().loadMapsByFeatureId(new Integer(id));
        //deletes relations between feature and maps
        for (int i=0; i < lstMapFeature.size(); i++){
        	GeoMap map = (GeoMap)lstMapFeature.get(i);
        	GeoMapFeature mapFeature = DAOFactory.getSbiGeoMapFeaturesDAO().loadMapFeatures(new Integer(map.getMapId()), new Integer(id));
        	DAOFactory.getSbiGeoMapFeaturesDAO().eraseMapFeatures(mapFeature);
        }
        //deletes the feature
        DAOFactory.getSbiGeoFeaturesDAO().eraseFeature(feature);
	}   catch (EMFUserError e){
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.DELETE", null, "ERR");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  HashMap params = new HashMap();
		  params.put(AdmintoolsConstants.PAGE, ListFeaturesModule.MODULE_PAGE);
		  throw new EMFUserError(EMFErrorSeverity.ERROR, 5022, new Vector(), params);
			
		}
	    catch (Exception ex) {	
	    	try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.DELETE", null, "KO");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot fill response container" + ex.getLocalizedMessage());
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}
	response.setAttribute("loopback", "true");
	try {
		AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "MAP_CATALOG_FEATURE.DELETE", null, "OK");
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
}


/**
 * Instantiates a new <code>map<code> object when a new map insertion is required, in order
 * to prepare the page for the insertion.
 * 
 * @param response The response SourceBean
 * @throws EMFUserError If an Exception occurred
 */

private void newDetailFeature(SourceBean response) throws EMFUserError {
	
	try {				
		this.modalita = SpagoBIConstants.DETAIL_INS;
		response.setAttribute("modality", modalita);
		GeoFeature feature = new GeoFeature();
		feature.setFeatureId(-1);
		feature.setDescr("");
		feature.setName("");
		feature.setType("");
		response.setAttribute("featureObj", feature);
	} catch (Exception ex) {
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.MAJOR, "Cannot prepare page for the insertion" + ex.getLocalizedMessage());		
		throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	}
	
}


private GeoFeature recoverFeatureDetails (SourceBean request) throws EMFUserError {
	GeoFeature feature  = new GeoFeature();
	String idStr = (String)request.getAttribute("ID");
	Integer id = new Integer(idStr);
	String description = (String)request.getAttribute("DESCR");	
	String name = (String)request.getAttribute("NAME");
	String type = (String)request.getAttribute("TYPE");

	feature.setFeatureId(id.intValue());
	feature.setName(name);
	feature.setDescr(description);
	feature.setType(type);
	
	return feature;
	}
		
}