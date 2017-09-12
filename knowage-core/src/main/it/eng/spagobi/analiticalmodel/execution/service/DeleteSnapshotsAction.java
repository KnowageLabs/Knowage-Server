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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
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
 * @author Zerbetto Davide
 */
public class DeleteSnapshotsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "DELETE_SNAPSHOTS_ACTION";
	public static final String SNAPSHOT_ID = "id";
	
	// logger component
	private static Logger logger = Logger.getLogger(DeleteSnapshotsAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			// retrieving execution instance from session, no need to check if user is able to execute the current document
			ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			BIObject obj = executionInstance.getBIObject();
			UserProfile userProfile = (UserProfile) this.getUserProfile();
			ISnapshotDAO dao = null;
			try {
				dao = DAOFactory.getSnapshotDAO();
			} catch (EMFUserError e) {
				logger.error("Error while istantiating DAO", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
			}
			String ids = this.getAttributeAsString(SNAPSHOT_ID);
			// ids contains the id of the snapshots to be deleted separated by ,
			String[] idArray = ids.split(",");
			for (int i = 0; i < idArray.length; i++) {
				Integer id = new Integer(idArray[i]);
				Snapshot snapshot = null;
				try {
					snapshot = dao.loadSnapshot(id);
				} catch (EMFUserError e) {
					logger.error("Snapshot with id = " + id + " not found", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Scheduled execution not found", e);
				}
				if (snapshot.getBiobjId().equals(obj.getId())) {
					logger.info("User [id: " + userProfile.getUserUniqueIdentifier() + ", userId: " + userProfile.getUserId() + ", name: " + userProfile.getUserName() + "] " +
							"is deleting scheduled execution [id: " + snapshot.getId() + ", name: " + snapshot.getName() + "] ...");
					try {
						dao.deleteSnapshot(id);
					} catch (EMFUserError e) {
						throw new SpagoBIServiceException(SERVICE_NAME, "Error while deleting scheduled execution", e);
					}
					logger.debug("Scheduled execution [id: " + snapshot.getId() + ", name: " + snapshot.getName() + "] deleted.");
				} else {
					logger.error("Cannot delete scheduled execution with id = " + snapshot.getBiobjId() + ": " +
							"it is not relevant to the current document [id: " + obj.getId() + ", label: " + obj.getLabel() + ", name: " + obj.getName() + "]");
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot delete scheduled execution: it is not relevant to the current document");
				}
				
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
