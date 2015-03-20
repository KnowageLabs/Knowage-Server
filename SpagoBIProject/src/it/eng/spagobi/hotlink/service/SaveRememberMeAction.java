/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.hotlink.service;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import sun.security.action.GetLongAction;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class SaveRememberMeAction extends AbstractHttpAction {

	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger(SaveRememberMeAction.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)
			throws Exception {
		
		logger.debug("IN");
		String message = null;
		freezeHttpResponse();
		HttpServletResponse httResponse = getHttpResponse();
		try {
			if (!this.getErrorHandler().isOKBySeverity(EMFErrorSeverity.ERROR)) {
				throw new Exception("Error handler contains errors, cannot save remember me!!");
			}
			SessionContainer permSession = this.getRequestContainer().getSessionContainer().getPermanentContainer();
			UserProfile profile = (UserProfile) permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			String userId = profile.getUserId().toString();
			String name = (String) serviceRequest.getAttribute("name");
			String description = (String) serviceRequest.getAttribute("description");
			String docIdStr = (String) serviceRequest.getAttribute(SpagoBIConstants.OBJECT_ID);
			Integer docId = new Integer(docIdStr);
			
			// check is user is able to execute the required document
			List correctRoles = ObjectsAccessVerifier.getCorrectRolesForExecution(docId, profile);
			// if no roles are suitable for execution, throws an exception
			if (correctRoles == null || correctRoles.size() == 0) {
				logger.error("No correct roles for execution for user [" + userId + "] and document with id = " + docId + "!!!!");
				throw new Exception("No correct roles for execution for user [" + userId + "] and document with id = " + docId + "!!!!");
			}
			
			String executionRole = (String) serviceRequest.getAttribute(SpagoBIConstants.EXECUTION_ROLE);
			if (!correctRoles.contains(executionRole)) {
				logger.error("Role [" + executionRole + "] is not a valid role for execution!!");
				throw new Exception("Specified role is not a valid role for execution!!");
			}
			
			// check if user is able to save RememberMe
			boolean canSaveRememberMe = false;
			Iterator it = correctRoles.iterator();
			while (it.hasNext()) {
				String roleName = (String) it.next();
				Role role = DAOFactory.getRoleDAO().loadByName(roleName);
				if (role.isAbleToSaveRememberMe()) {
					canSaveRememberMe = true;
					break;
				}
			}
			// if no roles are suitable for saving RememberMe, throws an exception
			if (!canSaveRememberMe) {
				logger.error("Current user [" + userId + "] is not able to save remember me");
				throw new Exception("Current user [" + userId + "] is not able to save remember me");
			}
			 
			String subobjectIdStr = (String) serviceRequest.getAttribute("subobject_id");
			Integer subobjectId = null;
			if (subobjectIdStr != null && !subobjectIdStr.trim().equals("")) {
				subobjectId = new Integer(subobjectIdStr);
				// check if user is able to see the required subobject
				SubObject subobject = DAOFactory.getSubObjectDAO().getSubObject(subobjectId);
				if (!subobject.getIsPublic() && !subobject.getOwner().equals(userId)) {
					logger.error("Current user [" + userId + "] CANNOT execute subobject with id = " + subobjectId + " of document with id = " + docId + "!!!!");
					throw new Exception("Current user [" + userId + "] CANNOT execute subobject with id = " + subobjectId + " of document with id = " + docId + "!!!!");
				}
			}
			
			String parameters = null;
			if (subobjectId == null) {
				// if the remember me is pointing a subobject, parameters are not considered;
				// if the remember me is pointing a main document, parameters are considered instead.
				parameters = (String) serviceRequest.getAttribute("parameters");
				ExecutionInstance instance = new ExecutionInstance(profile, "", "", docId, executionRole, "", true, true, null);
				instance.setParameterValues(parameters, true); 
				
				List errors = instance.getParametersErrors();
				if (errors != null && errors.size() > 0) {
					logger.error("Current user [" + userId + "] CANNOT execute document with id = " + docId + " with parameters = [" + parameters + "]!!!!");
					throw new Exception("Current user [" + userId + "] CANNOT execute document with id = " + docId + " with specified parameters!!!!");
				}
			}
			IRememberMeDAO dao=DAOFactory.getRememberMeDAO();
			dao.setUserProfile(profile);
			boolean inserted = dao.saveRememberMe(name, description, docId, subobjectId, userId, parameters);
			if (inserted) {
				message = "sbi.rememberme.saveOk";
			} else {
				message = "sbi.rememberme.alreadyExisting";
			}
		} catch (Exception e) {
			logger.debug("Error while saving remember me: " + e);
			message = "sbi.rememberme.errorWhileSaving";
		} finally {
			httResponse.getOutputStream().write(message.getBytes());
			httResponse.getOutputStream().flush();
			logger.debug("OUT");
		}
	}
	
}
