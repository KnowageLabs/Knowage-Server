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

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

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
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;

public class InsertNotesAction extends AbstractHttpAction {

	private static transient Logger logger = Logger.getLogger(InsertNotesAction.class);
	private Map execIdMap = new HashMap();

	 /* (non-Javadoc)
 	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
 	 */
 	@Override
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
				if (message.trim().equalsIgnoreCase("OPEN_NOTES_EDITOR")) {
					goToInsertNotes(request, "OPEN_NOTES_EDITOR", response);
				}
				else if (message.trim().equalsIgnoreCase("INSERT_NOTES")) {
					insertNotes(request, "INSERT_NOTES", response);
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

		private void goToInsertNotes(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException  {

			RequestContainer requestContainer = this.getRequestContainer();
    		SessionContainer session = requestContainer.getSessionContainer();
    		SessionContainer permanentSession = session.getPermanentContainer();
			String objId= (String)request.getAttribute("OBJECT_ID");
			String execIdentifier = (String)request.getAttribute("execIdentifier");
			String notes = getNotes(execIdentifier, objId);
			String conflict = "false";

			response.setAttribute("OBJECT_ID", objId);
			response.setAttribute("MESSAGEDET", mod);
			response.setAttribute("execIdentifier", execIdentifier);
			response.setAttribute("NOTES_CONFLICT", conflict);
			response.setAttribute("notes", notes);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "insertNotesBIObjectPubJ");

		}

		private void insertNotes(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException  {

			String objId = "";
			String notes = "";
			String oldNotes = "";
			String conflict = "false" ;
			String execIdentifier = "";
			String userId = "";
			RequestContainer requestContainer = this.getRequestContainer();
    		SessionContainer session = requestContainer.getSessionContainer();
    		SessionContainer permanentSession = session.getPermanentContainer();
    		UserProfile profile = (UserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
    		IEngUserProfile profile2 = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
    		userId= (String) profile.getUserId();
			List params = request.getContainedAttributes();
		    ListIterator it = params.listIterator();

		    while (it.hasNext()) {

			Object par = it.next();
			SourceBeanAttribute p = (SourceBeanAttribute) par;
			String parName = p.getKey();
			logger.debug("got parName=" + parName);
			if (parName.equals("OBJECT_ID")) {
			    objId = (String) request.getAttribute("OBJECT_ID");
			    logger.debug("got OBJECT_ID from Request=" + objId);
				}
			else if(parName.equals("execIdentifier")){
				execIdentifier = (String)request.getAttribute("execIdentifier");
			}
			else if(parName.equals("OLD_NOTES")){

				String oldNotest = (String)request.getAttribute("OLD_NOTES");
				oldNotes =new String(Base64.getDecoder().decode(oldNotest));
			}
			else if(parName.equals("notes")){
				notes = (String)request.getAttribute("notes");
			}
		    }

		    if (objId != null && !objId.equals("")){
		    	if (userId != null && !userId.equals("")){

		    		boolean canSee = false;

		    		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(new Integer(objId));
		    		try {
						canSee = ObjectsAccessVerifier.canSee(obj, profile2);
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
						logger.error("getCorrectRolesForExecution",e2);
					}
					if (correctRoles == null || correctRoles.size() == 0) {
						logger.warn("Object cannot be executed by no role of the user");
						throw new EMFUserError(EMFErrorSeverity.ERROR, 1006);
					}

					String tempOldNotest = getNotes(execIdentifier, objId );
					String tempOldNotes = "" ;
					tempOldNotes = new String(Base64.getDecoder().decode(tempOldNotest));

		    		if (tempOldNotes.equals(oldNotes)){
		    			saveNotes(execIdentifier, objId, notes,profile2);
		    		} else {
		    			conflict = "true" ;
		    			notes = oldNotes ;
		    		}
		       }
		     }


		    String notesEnc = Base64.getEncoder().encodeToString(notes.getBytes());
			response.setAttribute("OBJECT_ID", objId);
		    response.setAttribute("NOTES_CONFLICT", conflict);
		    response.setAttribute("execIdentifier", execIdentifier);
		    response.setAttribute("MESSAGEDET", mod);
		    response.setAttribute("notes", notesEnc);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "insertNotesBIObjectPubJ");

		 }


	 private String getNotes(String execIdentifier, String objectid ) {


		   String notes = "";
			try{

				IObjNoteDAO objNoteDAO = DAOFactory.getObjNoteDAO();
				ObjNote objnotes = objNoteDAO.getExecutionNotes(new Integer(objectid), execIdentifier);

				if(objnotes!=null){
					byte[] notestemp = objnotes.getContent();
					notes = Base64.getEncoder().encodeToString(notestemp);
					//notes = new String(objnotes.getContent());
				}

			} catch (Exception e) {
				logger.warn("Error while getting notes", e);
				notes = "SpagoBIError:Error";
			}

			return notes ;
		}

	 private void saveNotes(String execIdentifier,String objectid , String notes,IEngUserProfile profile) {
			try{
					IBIObjectDAO objectDAO = DAOFactory.getBIObjectDAO();
					BIObject biobject = objectDAO.loadBIObjectById(new Integer(objectid));
					EMFErrorHandler errorHandler = getErrorHandler();

					IObjNoteDAO objNoteDAO = DAOFactory.getObjNoteDAO();
					objNoteDAO.setUserProfile(profile);
					ObjNote objNote = objNoteDAO.getExecutionNotes(new Integer(objectid), execIdentifier);
					if(objNote!=null) {
						objNote.setContent(notes.getBytes());
						objNote.setExecReq(execIdentifier);
						  if(errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
							  objNoteDAO.modifyExecutionNotes(objNote);
							  }

					} else {
						objNote = new ObjNote();
						objNote.setContent(notes.getBytes());
						objNote.setExecReq(execIdentifier);
						if(errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
							objNoteDAO.saveExecutionNotes(biobject.getId(), objNote);
							  }
					}
			} catch (Exception e) {
				logger.warn("Error while saving notes", e);
			}
		}


}
