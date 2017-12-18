/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;

public class DocumentRatingAction extends AbstractHttpAction{
	
	 private static transient Logger logger = Logger.getLogger(DocumentRatingAction.class);
	 
	 /* (non-Javadoc)
 	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
 	 */
 	public void service(SourceBean request, SourceBean response) throws Exception {
			logger.debug("IN");
			
			String message = (String) request.getAttribute("MESSAGEDET");
			
			EMFErrorHandler errorHandler = getErrorHandler();
			try {
				if (message == null) {
					EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
					logger.debug("The message parameter is null");
					throw userError;
				}
				logger.debug("The message parameter is: " + message.trim());
				if (message.trim().equalsIgnoreCase("GOTO_DOCUMENT_RATE")) {
					goToDocumentRating(request, "GOTO_DOCUMENT_RATE", response);
				} 
				else if (message.trim().equalsIgnoreCase("DOCUMENT_RATE")) {
					documentRating(request, "DOCUMENT_RATE", response);
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
	 
		private void goToDocumentRating(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException  {
			
			RequestContainer requestContainer = this.getRequestContainer();		
    		SessionContainer session = requestContainer.getSessionContainer();
    		SessionContainer permanentSession = session.getPermanentContainer();
			String objId= (String)request.getAttribute("OBJECT_ID");

			response.setAttribute("OBJECT_ID", objId);
			response.setAttribute("MESSAGEDET", mod);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ratingBIObjectPubJ");
		
		}
		
		private void documentRating(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException  {
			
			String objId = "";
			String rating = "";
			RequestContainer requestContainer = this.getRequestContainer();		
    		SessionContainer session = requestContainer.getSessionContainer();
    		SessionContainer permanentSession = session.getPermanentContainer();
    		UserProfile profile = (UserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
    		IEngUserProfile profile2 = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
    		String userId= (profile.getUserUniqueIdentifier()!=null ? profile.getUserUniqueIdentifier().toString():"");
			List params = request.getContainedAttributes();
		    ListIterator it = params.listIterator();

		    while (it.hasNext()) {

			Object par = it.next();
			SourceBeanAttribute p = (SourceBeanAttribute) par;
			String parName = (String) p.getKey();
			logger.debug("got parName=" + parName);
			if (parName.equals("OBJECT_ID")) {
			    objId = (String) request.getAttribute("OBJECT_ID");
			    logger.debug("got OBJECT_ID from Request=" + objId);
				} 
			else if(parName.equals("RATING")){
				rating = (String)request.getAttribute("RATING");
			}
		    }
		    boolean canSee = false;
    		
    		
    		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(new Integer(objId));
    		try {
				canSee = ObjectsAccessVerifier.canSee(obj, profile);
			} catch (EMFInternalError e1) {
				e1.printStackTrace();
			}
			if (!canSee) {
				logger.error("Object with label = '" + obj.getLabel()
						+ "' cannot be executed by the user!!");
				Vector v = new Vector();
				v.add(obj.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "1075", v, null);
			}
			// get all correct execution roles
			List correctRoles = new ArrayList();
			try {
				correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(new Integer(objId), profile2);
			} catch (NumberFormatException e2) {
				e2.printStackTrace();
			} 
			if (correctRoles == null || correctRoles.size() == 0) {
				logger.warn("Object cannot be executed by no role of the user");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1006);
			}
		    
		    if (objId != null && !objId.equals("")){
		    	if (rating != null && !rating.equals("")){
					//VOTE!
					DAOFactory.getBIObjectRatingDAO().voteBIObject(obj, userId, rating);
		       }
		     }
		   
		    response.setAttribute("MESSAGEDET", mod);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ratingBIObjectPubJ");
			response.setAttribute("OBJECT_ID",objId);
			
		 }
}
