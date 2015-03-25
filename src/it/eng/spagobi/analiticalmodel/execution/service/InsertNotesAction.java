/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class InsertNotesAction extends AbstractHttpAction {
	
	private static transient Logger logger = Logger.getLogger(InsertNotesAction.class);
	private Map execIdMap = new HashMap(); 
	 
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
			String parName = (String) p.getKey();
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
				try {
					oldNotes =new String(new BASE64Decoder().decodeBuffer(oldNotest));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
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
						e2.printStackTrace();
					} 
					if (correctRoles == null || correctRoles.size() == 0) {
						logger.warn("Object cannot be executed by no role of the user");
						throw new EMFUserError(EMFErrorSeverity.ERROR, 1006);
					}
		    		
		    		String tempOldNotest = getNotes(execIdentifier, objId );
		    		String tempOldNotes = "" ;
		    		try {
		    			tempOldNotes =new String(new BASE64Decoder().decodeBuffer(tempOldNotest));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
		    		
		    		if (tempOldNotes.equals(oldNotes)){
		    			saveNotes(execIdentifier, objId, notes,profile2);
		    		} else {
		    			conflict = "true" ;
		    			notes = oldNotes ;
		    		}
		       }
		     }
		    
		    
		    String notesEnc = new BASE64Encoder().encode(notes.getBytes());
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
					notes = new BASE64Encoder().encode(notestemp);
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
