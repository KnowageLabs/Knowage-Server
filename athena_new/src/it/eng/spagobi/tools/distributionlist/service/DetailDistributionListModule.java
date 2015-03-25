/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.distributionlist.service;

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
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
* @author Chiarelli Chiara (chiara.chiarelli@eng.it)
*/

public class DetailDistributionListModule extends AbstractModule {

	static private Logger logger = Logger.getLogger(DetailDistributionListModule.class);
	public static final String MOD_SAVE = "SAVE";
	public static final String MOD_SAVEBACK = "SAVEBACK";
	public final static String NAME_ATTR_LIST_DIALECTS = "dialects";

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
		logger.debug("IN");
		String message = (String) request.getAttribute("MESSAGEDET");
		logger.debug("begin of detail Distribution List service with message =" +message);
		EMFErrorHandler errorHandler = getErrorHandler();
		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.debug("The message parameter is null");
				throw userError;
			}
			logger.debug("The message parameter is: " + message.trim());
			if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_SELECT)) {
				getDistributionList(request, response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_MOD)) {
			  modifyDistributionList(request, SpagoBIConstants.DETAIL_MOD, response);
			} 
		else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_NEW)) {
				newDistributionList(response);
			} else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {
			 modifyDistributionList(request, SpagoBIConstants.DETAIL_INS, response);
		    } 
		else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_DEL)) {
				deleteDistributionList(request, SpagoBIConstants.DETAIL_DEL, response);
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
		logger.debug("OUT");
	}
	
	 
	
	/**
	 * Gets the detail of a Distribution List choosed by the user from the 
	 * Distribution Lists list. It reaches the key from the request and asks to the DB all detail
	 * Distribution List information, by calling the method <code>loadDistributionListByID</code>.
	 *   
	 * @param key The choosed Distribution List id key
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	private void getDistributionList(SourceBean request, SourceBean response) throws EMFUserError {	
		logger.debug("IN");
		try {		 									
			DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(new Integer((String)request.getAttribute("DL_ID")));		
			this.modalita = SpagoBIConstants.DETAIL_MOD;
			if (request.getAttribute("SUBMESSAGEDET") != null &&
				((String)request.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVEBACK))
			{
				response.setAttribute("loopback", "true");
				return;
			}
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			response.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
			response.setAttribute("modality", modalita);
			response.setAttribute("dlObj", dl);
		} catch (Exception ex) {
			logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListDistributionListModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9107, new Vector(), params);
		}
		logger.debug("OUT");
		
	}
	 /**
	 * Inserts/Modifies the detail of a Distribution List according to the user request. 
	 * When a Distribution List is modified, the <code>modifyDistributionList</code> method is called; when a new
	 * Distribution List is added, the <code>insertDistributionList</code>method is called. These two cases are 
	 * differentiated by the <code>mod</code> String input value .
	 * 
	 * @param request The request information contained in a SourceBean Object
	 * @param mod A request string used to differentiate insert/modify operations
	 * @param response The response SourceBean 
	 * @throws EMFUserError If an exception occurs
	 * @throws SourceBeanException If a SourceBean exception occurs
	 */
	
	private void modifyDistributionList(SourceBean serviceRequest, String mod, SourceBean serviceResponse)
		throws EMFUserError, SourceBeanException {
		logger.debug("IN");
		try {
			RequestContainer reqCont = getRequestContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			SessionContainer permSess = sessCont.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			IDistributionListDAO dao=DAOFactory.getDistributionListDAO();
			dao.setUserProfile(profile);
			DistributionList dlNew = recoverDistributionListDetails(serviceRequest);
			
			EMFErrorHandler errorHandler = getErrorHandler();
			 
			// if there are some validation errors into the errorHandler it does not write into DB
			Collection errors = errorHandler.getErrors();
			if (errors != null && errors.size() > 0) {
				Iterator iterator = errors.iterator();
				while (iterator.hasNext()) {
					Object error = iterator.next();
					if (error instanceof EMFValidationError) {
						serviceResponse.setAttribute("dlObj", dlNew);
						serviceResponse.setAttribute("modality", mod);
						return;
					}
				}
			}
			
			if (mod.equalsIgnoreCase(SpagoBIConstants.DETAIL_INS)) {			
				//if a dl with the same name does not exist in the db ok, else error
				if (dao.loadDistributionListByName(dlNew.getName()) != null){
					HashMap params = new HashMap();
					params.put(AdmintoolsConstants.PAGE, ListDistributionListModule.MODULE_PAGE);
					EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 9100, new Vector(), params );
					getErrorHandler().addError(error);
					return;
				}	 		
				 
				dao.insertDistributionList(dlNew);
				
				//gets the new setted Id from the DL just inserted and puts it into dlNew
				DistributionList tmpDL = dao.loadDistributionListByName(dlNew.getName());
				dlNew.setId(tmpDL.getId());
				mod = SpagoBIConstants.DETAIL_MOD; 
			} else {				
				//updates dl
				dao.modifyDistributionList(dlNew);			
			}  
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			serviceResponse.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
			
			if (serviceRequest.getAttribute("SUBMESSAGEDET") != null && 
				((String)serviceRequest.getAttribute("SUBMESSAGEDET")).equalsIgnoreCase(MOD_SAVE)) {	
				serviceResponse.setAttribute("modality", mod);
				serviceResponse.setAttribute("dlObj", dlNew);				
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
			params.put(AdmintoolsConstants.PAGE, ListDistributionListModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9105, new Vector(), params);
			
		}
		
		catch (Exception ex) {		
			logger.error("Cannot fill response container" , ex);		
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}	
		logger.debug("OUT");
	}

	/**
	 * Deletes a Distribution List choosen by the user from the Distribution Lists list.
	 * 
	 * @param request	The request SourceBean
	 * @param mod	A request string used to differentiate delete operation
	 * @param response	The response SourceBean
	 * @throws EMFUserError	If an Exception occurs
	 * @throws SourceBeanException If a SourceBean Exception occurs
	 */
	private void deleteDistributionList(SourceBean request, String mod, SourceBean response)
		throws EMFUserError, SourceBeanException {
		logger.debug("IN");
		try {
			String id = (String) request.getAttribute("DL_ID");
			
			//deletes the dl
			DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(new Integer(id));
			DAOFactory.getDistributionListDAO().eraseDistributionList(dl);
		}
		catch (EMFUserError e){
			  logger.error("Cannot fill response container" + e.getLocalizedMessage());
			  HashMap params = new HashMap();		  
			  params.put(AdmintoolsConstants.PAGE, ListDistributionListModule.MODULE_PAGE);
			  throw new EMFUserError(EMFErrorSeverity.ERROR, 9101, new Vector(), params);
				
		}
	    catch (Exception ex) {		
		    ex.printStackTrace();
			logger.error("Cannot fill response container" ,ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	    }
	    response.setAttribute("loopback", "true");		
	    logger.debug("OUT");
	}



	/**
	 * Instantiates a new <code>DistributionList<code> object when a new Distribution List insertion is required, in order
	 * to prepare the page for the insertion.
	 * 
	 * @param response The response SourceBean
	 * @throws EMFUserError If an Exception occurred
	 */

	private void newDistributionList(SourceBean response) throws EMFUserError {
		logger.debug("IN");
		try {
			
			DistributionList dl = null;
			this.modalita = SpagoBIConstants.DETAIL_INS;
			response.setAttribute("modality", modalita);
			dl = new DistributionList();
			dl.setId(-1);
			dl.setDescr("");
			dl.setName("");
			response.setAttribute("dlObj", dl);
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			response.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
		} catch (Exception ex) {
			logger.error("Cannot prepare page for the insertion" , ex);		
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		logger.debug("OUT");
		
	}


	private DistributionList recoverDistributionListDetails (SourceBean serviceRequest) throws EMFUserError, SourceBeanException, IOException  {
		logger.debug("IN");
		DistributionList dl  = new DistributionList();
		
		String id = (String)serviceRequest.getAttribute("id");
		String name = (String)serviceRequest.getAttribute("NAME");
		String description = (String)serviceRequest.getAttribute("DESCR");	
		
		dl.setId((new Integer(id)).intValue());
		dl.setName(name);
		dl.setDescr(description);
		logger.debug("OUT");		
		return dl;
	}

}
