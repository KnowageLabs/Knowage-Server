/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.distributionlist.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.bo.Email;
import it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
/**
* @author Chiarelli Chiara (chiara.chiarelli@eng.it)
*/

public class DetailDistributionListUserModule extends AbstractHttpModule {
	
	static private Logger logger = Logger.getLogger(DetailDistributionListUserModule.class);
	public final static String NAME_ATTR_LIST_DIALECTS = "dialects";

	private String modalita = "";
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractHttpModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insert, update or delete methods.
	 * 
	 * @param request The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 * 
	 * @throws exception If an exception occurs
	 * @throws Exception the exception
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
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
			} 
			else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_INSERTEMAIL)) {
				insertEmail(request, SpagoBIConstants.DETAIL_INSERTEMAIL, response);
				} 
			else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_SUBSC)) {
				subscribeToDistributionList(request, SpagoBIConstants.DETAIL_SUBSC, response);
				} 
		    else if (message.trim().equalsIgnoreCase(SpagoBIConstants.DETAIL_UNSUBSC)) {
		    	unsubscribeFromDistributionList(request, SpagoBIConstants.DETAIL_UNSUBSC, response);
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
	 * Gets a form to insert an email address
	 *   
	 * @param request The request Source Bean
	 * @param mod The modality String
	 * @param response The response Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */  
	private void insertEmail(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException  {
		
		DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(new Integer((String)request.getAttribute("DL_ID")));		
		response.setAttribute("dlObj", dl);
		String id = (String) request.getAttribute("DL_ID");
		response.setAttribute("DL_ID", id);

		response.setAttribute("modality", mod);
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "insertEmailPubJ");
	    
		
	}
	
	/**
	 * Subscribes the user to the distribution list.
	 *   
	 * @param request The request Source Bean
	 * @param mod The modality String
	 * @param response The response Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	private void subscribeToDistributionList(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException  {		
		
		String id = "";
		String email = "";
		String userId ="";
		String submessagedet = "";
		DistributionList dl = null;
		try {
						
			this.modalita = SpagoBIConstants.DETAIL_SUBSC;
			
			id = (String) request.getAttribute("DL_ID");
			email = (String)request.getAttribute("EMAIL");
			submessagedet = (String)request.getAttribute("SUBMESSAGEDET");
			
			IEngUserProfile profile=UserUtilities.getUserProfile();

			if (profile!=null) userId=(String)((UserProfile)profile).getUserId();
			//load the dl
			dl = DAOFactory.getDistributionListDAO().loadDistributionListById(new Integer(id));
			//load the user
			Email user = new Email();
			user.setEmail(email);
			user.setUserId(userId);
			//subscribe to the dl
			IDistributionListDAO dao=DAOFactory.getDistributionListDAO();
			dao.setUserProfile(profile);
			dao.subscribeToDistributionList(dl,user);
		}
		catch (EMFUserError e){
			  logger.error("Cannot fill response container" + e.getLocalizedMessage());
			  HashMap params = new HashMap();		  
			  params.put(AdmintoolsConstants.PAGE, ListDistributionListUserModule.MODULE_PAGE);
			  throw new EMFUserError(EMFErrorSeverity.ERROR, 9102, new Vector(), params);
				
		}
	    catch (Exception ex) {		
		    ex.printStackTrace();
			logger.error("Cannot fill response container" ,ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	    }
	    response.setAttribute("modality", mod);
	    response.setAttribute("DL_ID", id);
	    response.setAttribute("EMAIL", email);
	    response.setAttribute("dlObj", dl);
	    response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "insertEmailPubJ");
	    if (submessagedet != null && submessagedet.equals("SAVEBACK")){
	    response.setAttribute("loopback", "true");
	    }
		
	}
	
	/**
	 * Unsubscribes the user from the distribution list.
	 *   
	 * @param request The request Source Bean
	 * @param mod The modality String
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	private void unsubscribeFromDistributionList(SourceBean request, String mod,SourceBean response)  throws EMFUserError, SourceBeanException {		
		try {
			String id = (String) request.getAttribute("DL_ID");
			this.modalita = SpagoBIConstants.DETAIL_UNSUBSC;
			IEngUserProfile userProfile = UserUtilities.getUserProfile();
			String userId = (String)((UserProfile)userProfile).getUserId();
			//load the dl
			DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(new Integer(id));

			DAOFactory.getDistributionListDAO().unsubscribeFromDistributionList(dl, userId);
		}
		catch (EMFUserError e){
			  logger.error("Cannot fill response container" + e.getLocalizedMessage());
			  HashMap params = new HashMap();		  
			  params.put(AdmintoolsConstants.PAGE, ListDistributionListUserModule.MODULE_PAGE);
			  throw new EMFUserError(EMFErrorSeverity.ERROR, 9103, new Vector(), params);
				
		}
	    catch (Exception ex) {		
		    ex.printStackTrace();
			logger.error("Cannot fill response container" ,ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
	    }
	    response.setAttribute("loopback", "true");
		
	}
	 
	
	/**
	 * Gets the detail of a Distribution List choosen by the user from the 
	 * Distribution Lists list. It reaches the key from the request and asks to the DB all detail
	 * Distribution List information, by calling the method <code>loadDistributionListByID</code>.
	 *   
	 * @param request The request Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	private void getDistributionList(SourceBean request, SourceBean response) throws EMFUserError {		
		try {		 									
			DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(new Integer((String)request.getAttribute("DL_ID")));		
			this.modalita = SpagoBIConstants.DETAIL_SELECT;
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List dialects = domaindao.loadListDomainsByType("DIALECT_HIB");
			response.setAttribute(NAME_ATTR_LIST_DIALECTS, dialects);
			response.setAttribute("modality", modalita);
			response.setAttribute("dlObj", dl);
		} catch (Exception ex) {
			logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, ListDistributionListUserModule.MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9107, new Vector(), params);
		}
		
	}


}
