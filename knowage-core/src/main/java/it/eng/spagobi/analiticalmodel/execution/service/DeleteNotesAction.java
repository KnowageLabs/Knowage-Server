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

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.BIObjectNotesManager;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Antonella Giachino
 */
public class DeleteNotesAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "DELETE_NOTES_ACTION"; 
	// REQUEST PARAMETERS
	public static final String OWNER = "OWNER";
	
	// logger component
	private static Logger logger = Logger.getLogger(DeleteNotesAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			
			// retrieving execution instance from session, no need to check if user is able to execute the current document
			ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			BIObject obj = executionInstance.getBIObject();
			BIObjectNotesManager objectNManager = new BIObjectNotesManager();
			String execIdentifier = objectNManager.getExecutionIdentifier(obj);
			
			String owner = this.getAttributeAsString(OWNER);
			logger.debug("Parameter [" + OWNER + "] is equal to [" + owner + "]");
			
			logger.debug("Deleting notes...");
			String resultStr = null;
			try {
				ObjNote objnote = null;
				try {
					objnote = DAOFactory.getObjNoteDAO().getExecutionNotesByOwner(obj.getId(), execIdentifier, owner);
				} catch (Exception e) {
					logger.error("Cannot load notes for document [id: " + obj.getId() + ", label: " + obj.getLabel() + ", name: " + obj.getName() + "]", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot erase notes", e);
				}
				
				DAOFactory.getObjNoteDAO().eraseNotesByOwner(obj.getId(), execIdentifier, owner);
			
				logger.debug("Notes deleted");
				resultStr = "ok";
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error while deleting notes", e);
			}

			try {
				JSONObject results = new JSONObject();
				results.put("result", "OK");
				writeBackToClient( new JSONSuccess( results ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			}

		} finally {
			logger.debug("OUT");
		}
	}

}
