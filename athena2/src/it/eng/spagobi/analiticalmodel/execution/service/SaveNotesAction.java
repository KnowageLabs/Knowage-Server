/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.BIObjectNotesManager;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Zerbetto Davide
 *
 */
public class SaveNotesAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "SAVE_NOTES_ACTION";
	public static final String MESSAGE = "MESSAGE";
	public static final String NOTES = "NOTES";
	public static final String PREVIOUS_NOTES = "PREVIOUS_NOTES";
	public static final String VISIBILITY = "VISIBILITY";
	
	// logger component
	private static Logger logger = Logger.getLogger(SaveNotesAction.class);
	
	public void doService() {
		logger.debug("IN");
		try {
			// retrieving execution instance from session, no need to check if user is able to execute the current document
			ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			BIObject obj = executionInstance.getBIObject();
			BIObjectNotesManager objectNManager = new BIObjectNotesManager();
			String execIdentifier = objectNManager.getExecutionIdentifier(obj);
			
			String previousNotes = this.getAttributeAsString("PREVIOUS_NOTES");
			logger.debug("Parameter [" + PREVIOUS_NOTES + "] is equal to [" + previousNotes + "]");
			
			String notes = this.getAttributeAsString("NOTES");
			logger.debug("Parameter [" + NOTES + "] is equal to [" + notes + "]");
			
			String message = this.getAttributeAsString(MESSAGE);
			logger.debug("Parameter [" + MESSAGE + "] is equal to [" + message + "]");
			
			String visibility = this.getAttributeAsString(VISIBILITY);
			logger.debug("Parameter [" + VISIBILITY + "] is equal to [" + visibility + "]");
			
			String resultStr = null;
			
			ObjNote objnote = null;
			
			SessionContainer sessCont = getSessionContainer();
			SessionContainer permCont = sessCont.getPermanentContainer();
			IEngUserProfile	profile = (IEngUserProfile)permCont.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			
			String owner = (String)((UserProfile)profile).getUserId();
			try {
				objnote = DAOFactory.getObjNoteDAO().getExecutionNotesByOwner(obj.getId(), execIdentifier, owner);
			} catch (Exception e) {
				logger.error("Cannot load notes for document [id: " + obj.getId() + ", label: " + obj.getLabel() + ", name: " + obj.getName() + "]", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load notes", e);
			}
			String currentNotes = "";
			if (objnote != null) {
				logger.debug("Existing notes found with the same execution identifier");
				byte[] content = objnote.getContent();
				currentNotes = new String(content);
			}
			if (!"INSERT_NOTE".equalsIgnoreCase(MESSAGE) && !currentNotes.equals(previousNotes)) {
				logger.debug("Notes have been created by another user");
				resultStr = "conflict";
			} else {
				logger.debug("Saving notes...");
				try {
					saveNotes(execIdentifier, obj.getId(), notes, objnote, owner, visibility,profile);
					logger.debug("Notes saved");
					resultStr = "ok";
				} catch (Exception e) {
					throw new SpagoBIServiceException(SERVICE_NAME, "Error while saving notes", e);
				}
			}
			
			try {
				JSONObject result = new JSONObject();
				result.put("result", resultStr);
				writeBackToClient( new JSONSuccess( result ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			}
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void saveNotes(String execIdentifier, Integer objectId, String notes, ObjNote objnote, String owner, String visibility,IEngUserProfile profile) 
			throws Exception {
		logger.debug("IN");
		try {
			IObjNoteDAO objNoteDAO = DAOFactory.getObjNoteDAO();
			objNoteDAO.setUserProfile(profile);
			if (objnote != null) {
				//Modify note
				objnote.setContent(notes.getBytes());
				objnote.setIsPublic((visibility.equalsIgnoreCase("PUBLIC")?true:false));
				objNoteDAO.modifyExecutionNotes(objnote);
			} else {
				//Insert new note
				objnote = new ObjNote();
				objnote.setContent(notes.getBytes());
				objnote.setExecReq(execIdentifier);
				objnote.setIsPublic((visibility.equalsIgnoreCase("PUBLIC")?true:false));
				objnote.setOwner(owner);
				objNoteDAO.saveExecutionNotes(objectId, objnote);
			}
		} finally {
			logger.debug("OUT");
		}
	}

}
