/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.objmetadata.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This class implements a module which  handles data source management. 
 */
public class DetailObjMetadataModule extends AbstractModule {
	static private Logger logger = Logger.getLogger(DetailObjMetadataModule.class);
	public static final String MOD_SAVE = "SAVE";
	public static final String MOD_SAVEBACK = "SAVEBACK";
	public static final String OBJMETA_DATA_TYPE = "dataTypes";

	private String modalita = "";
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insertion, updation or deletion methods.
	 * 
	 * @param request The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 * 
	 * @throws exception If an exception occurs
	 * @throws Exception the exception
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		String message = (String) request.getAttribute("MESSAGEDET");
		logger.debug("begin of detail Data Source service with message =" +message);
		EMFErrorHandler errorHandler = getErrorHandler();
		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.debug("The message parameter is null");
				throw userError;
			}
			logger.debug("The message parameter is: " + message.trim());
			if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_SELECT)) {
				getObjMetadata(request, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {
				modifyObjMetadata(request, SpagoBIConstants.DETAIL_MOD, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_NEW)) {
				newObjMetadata(response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
				modifyObjMetadata(request, SpagoBIConstants.DETAIL_INS, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_DEL)) {
				deleteObjMetadata(request, SpagoBIConstants.DETAIL_DEL, response);
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
	 * Gets the detail of a obj metadata choosed by the user from the 
	 * obj metadata list. It reaches the key from the request and asks to the DB all detail
	 * objmetadata information, by calling the method <code>loadObjMetadataByID</code>.
	 *   
	 * @param key The choosed metadata id key
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	private void getObjMetadata(SourceBean request, SourceBean response) throws EMFUserError {		
		try {		 									
			ObjMetadata meta = DAOFactory.getObjMetadataDAO().loadObjMetaDataByID(new Integer((String)request.getAttribute("ID")));		
			this.modalita = SpagoBIConstants.DETAIL_MOD;
			if (request.getAttribute("SUBMESSAGEDET") != null &&
				((String)request.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK))
			{
				response.setAttribute("loopback", "true");
				return;
			}
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dataTypes = domaindao.loadListDomainsByType("OBJMETA_DATA_TYPE");
			response.setAttribute(OBJMETA_DATA_TYPE, dataTypes);
			response.setAttribute("modality", modalita);
			response.setAttribute("metaObj", meta);
		} catch (Exception ex) {
			logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListObjMetadataModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 13003, new Vector(), params);
		}
		
	}
	 /**
	 * Inserts/Modifies the detail of a object metadta according to the user request. 
	 * When a metadata is modified, the <code>modifyObjMetadata</code> method is called; when a new
	 * metadata is added, the <code>insertObjMetadata</code>method is called. These two cases are 
	 * differentiated by the <code>mod</code> String input value .
	 * 
	 * @param request The request information contained in a SourceBean Object
	 * @param mod A request string used to differentiate insert/modify operations
	 * @param response The response SourceBean 
	 * @throws EMFUserError If an exception occurs
	 * @throws SourceBeanException If a SourceBean exception occurs
	 */
	private void modifyObjMetadata(SourceBean serviceRequest, String mod, SourceBean serviceResponse)
		throws EMFUserError, SourceBeanException {
		
		try {
			RequestContainer reqCont = getRequestContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			SessionContainer permSess = sessCont.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			 
			IObjMetadataDAO dao=DAOFactory.getObjMetadataDAO();
			dao.setUserProfile(profile);
			
			ObjMetadata metaNew = recoverObjMetadataDetails(serviceRequest);
			
			EMFErrorHandler errorHandler = getErrorHandler();
			 
			// if there are some validation errors into the errorHandler does not write into DB
			Collection errors = errorHandler.getErrors();
			if (errors != null && errors.size() > 0) {
				Iterator iterator = errors.iterator();
				while (iterator.hasNext()) {
					Object error = iterator.next();
					if (error instanceof EMFValidationError) {
						serviceResponse.setAttribute("metaObj", metaNew);
						serviceResponse.setAttribute("modality", mod);
						return;
					}
				}
			}
			
			if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {			
				//if a ds with the same label not exists on db ok else error
				if (dao.loadObjMetadataByLabel(metaNew.getLabel()) != null){
					HashMap params = new HashMap();
					params.put(AdmintoolsConstants.PAGE, ListObjMetadataModule.MODULE_PAGE);
					EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 13004, new Vector(), params );
					getErrorHandler().addError(error);
					return;
				}	 		

				dao.insertObjMetadata(metaNew);
				
				ObjMetadata tmpMeta = dao.loadObjMetadataByLabel(metaNew.getLabel());
				metaNew.setObjMetaId(tmpMeta.getObjMetaId());
				mod = SpagoBIConstants.DETAIL_MOD; 
			} else {				
				//update metadata
				dao.modifyObjMetadata(metaNew);			
			}  
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dataTypes = domaindao.loadListDomainsByType("OBJMETA_DATA_TYPE");
			serviceResponse.setAttribute(OBJMETA_DATA_TYPE, dataTypes);
			
			if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
				((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVE)) {	
				serviceResponse.setAttribute("modality", mod);
				serviceResponse.setAttribute("metaObj", metaNew);				
				return;
			}
			else if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
					((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK)){
					serviceResponse.setAttribute("loopback", "true");
				    return;
			}					     
		} catch (EMFUserError e){
			logger.error("Cannot fill response container" + e.getLocalizedMessage());
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListObjMetadataModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 13005, new Vector(), params);
			
		}
		
		catch (Exception ex) {		
			logger.error("Cannot fill response container" , ex);		
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}			
	}

	/**
	 * Deletes an obj metadata choosed by user from the metadata list.
	 * 
	 * @param request	The request SourceBean
	 * @param mod	A request string used to differentiate delete operation
	 * @param response	The response SourceBean
	 * @throws EMFUserError	If an Exception occurs
	 * @throws SourceBeanException If a SourceBean Exception occurs
	 */
	private void deleteObjMetadata(SourceBean request, String mod, SourceBean response)
		throws EMFUserError, SourceBeanException {
		
		try {
			String id = (String) request.getAttribute("ID");
//			if the metadata is associated with any BIObjects or BISuobjets, creates an error
			/*boolean bObjects =  DAOFactory.getObjMetadataDAO().hasBIObjAssociated(id);
			boolean bSubobjects =  DAOFactory.getObjMetadataDAO().hasSubObjAssociated(id);
			if (bObjects || bSubobjects){
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, ListObjMetadataModule.MODULE_PAGE);
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 13007, new Vector(), params );
				getErrorHandler().addError(error);
				return;
			}*/
			
			//delete the metadata
			ObjMetadata meta = DAOFactory.getObjMetadataDAO().loadObjMetaDataByID(new Integer(id));
			DAOFactory.getObjMetadataDAO().eraseObjMetadata(meta);
		}
		catch (EMFUserError e){
			  logger.error("Cannot fill response container" + e.getLocalizedMessage());
			  HashMap params = new HashMap();		  
			  params.put(AdmintoolsConstants.PAGE, ListObjMetadataModule.MODULE_PAGE);
			  throw new EMFUserError(EMFErrorSeverity.ERROR, 13006, new Vector(), params);
				
		}
	    catch (Exception ex) {		
		    ex.printStackTrace();
			logger.error("Cannot fill response container" ,ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	    }
	    response.setAttribute("loopback", "true");			
	}



	/**
	 * Instantiates a new <code>objmetadata<code> object when a new metadata insertion is required, in order
	 * to prepare the page for the insertion.
	 * 
	 * @param response The response SourceBean
	 * @throws EMFUserError If an Exception occurred
	 */

	private void newObjMetadata(SourceBean response) throws EMFUserError {
		
		try {
			
			ObjMetadata meta = null;
			this.modalita = SpagoBIConstants.DETAIL_INS;
			response.setAttribute("modality", modalita);
			meta = new ObjMetadata();
			meta.setObjMetaId(-1);
			meta.setDescription("");
			meta.setLabel("");
			meta.setDataType(new Integer("-1"));
			meta.setName("");
			meta.setCreationDate(null);
			response.setAttribute("metaObj", meta);
			
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dataTypes = domaindao.loadListDomainsByType("OBJMETA_DATA_TYPE");
			response.setAttribute(OBJMETA_DATA_TYPE, dataTypes);
			
		} catch (Exception ex) {
			logger.error("Cannot prepare page for the insertion" , ex);		
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		
	}


	private ObjMetadata recoverObjMetadataDetails (SourceBean serviceRequest) throws EMFUserError, SourceBeanException, IOException  {
		ObjMetadata meta  = new ObjMetadata();
		
		String idStr = (String)serviceRequest.getAttribute("ID");
		Integer id = new Integer(idStr);
		String description = (String)serviceRequest.getAttribute("DESCR");	
		String label = (String)serviceRequest.getAttribute("LABEL");
		String name = (String)serviceRequest.getAttribute("NAME");
		String dataType = (String)serviceRequest.getAttribute("DATA_TYPE");
		//String creationDate = (String)serviceRequest.getAttribute("USER");
		
		meta.setObjMetaId(id.intValue());
		meta.setDescription(description);
		meta.setLabel(label);
		meta.setDataType(Integer.valueOf(dataType));
		meta.setName(name);
		//meta.setCreationDate(creationDate);
		
				
		return meta;
	}

}