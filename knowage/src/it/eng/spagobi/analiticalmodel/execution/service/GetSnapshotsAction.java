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
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide
 */
public class GetSnapshotsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "GET_SNAPSHOTS_ACTION";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetSnapshotsAction.class);
	
	public void doService() {
		logger.debug("IN");
		ExecutionInstance executionInstance;
		
		try {
			// retrieving execution instance from session, no need to check if user is able to execute the required document
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			Integer biobjectId = executionInstance.getBIObject().getId();
			List snapshotsList = null;
			try {
				snapshotsList = DAOFactory.getSnapshotDAO().getSnapshots(biobjectId);
			} catch (EMFUserError e) {
				logger.error("Error while recovering snapshots list for document with id = " + biobjectId, e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load scheduled executions", e);
			}
			
			try {
				JSONArray snapshotsListJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( snapshotsList ,null);
				JSONObject results = new JSONObject();
				results.put("results", snapshotsListJSON);
				writeBackToClient( new JSONSuccess( results ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects", e);
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects into a JSON object", e);
			}

		} finally {
			logger.debug("OUT");
		}
	}

}
