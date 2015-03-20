/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
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
public class DeleteSubObjectsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "DELETE_SUBOBJECTS_ACTION";
	public static final String SUBOBJECT_ID = "id";
	
	// logger component
	private static Logger logger = Logger.getLogger(DeleteSubObjectsAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			// retrieving execution instance from session, no need to check if user is able to execute the current document
			ExecutionInstance executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			BIObject obj = executionInstance.getBIObject();
			UserProfile userProfile = (UserProfile) this.getUserProfile();
			ISubObjectDAO dao = null;
			try {
				dao = DAOFactory.getSubObjectDAO();
			} catch (EMFUserError e) {
				logger.error("Error while istantiating DAO", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot access database", e);
			}
			String ids = this.getAttributeAsString(SUBOBJECT_ID);
			// ids contains the id of the subobjects to be deleted separated by ,
			String[] idArray = ids.split(",");
			for (int i = 0; i < idArray.length; i++) {
				Integer id = new Integer(idArray[i]);
				SubObject subObject = null;
				try {
					subObject = dao.getSubObject(id);
				} catch (EMFUserError e) {
					logger.error("SubObject with id = " + id + " not found", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Customized view not found", e);
				}
				if (subObject.getBiobjId().equals(obj.getId())) {
					boolean canDeleteSubObject = false;
					if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) 
							|| subObject.getOwner().equals(userProfile.getUserId().toString())) {
						canDeleteSubObject = true;
					}
					if (canDeleteSubObject) {
						logger.info("User [id: " + userProfile.getUserUniqueIdentifier() + ", userId: " + userProfile.getUserId() + ", name: " + userProfile.getUserName() + "] " +
								"is deleting customized view [id: " + subObject.getId() + ", name: " + subObject.getName() + "] ...");
						try {
							dao.deleteSubObject(id);
						} catch (EMFUserError e) {
							throw new SpagoBIServiceException(SERVICE_NAME, "Error while deleting customized view", e);
						}
						logger.debug("Customized view [id: " + subObject.getId() + ", name: " + subObject.getName() + "] deleted.");
					} else {
						logger.error("User [id: " + userProfile.getUserUniqueIdentifier() + ", userId: " + userProfile.getUserId() + ", name: " + userProfile.getUserName() + "] cannot delete customized view");
						throw new SpagoBIServiceException(SERVICE_NAME, "User cannot delete customized view");
					}
				} else {
					logger.error("Cannot delete customized view with id = " + subObject.getBiobjId() + ": " +
							"it is not relevant to the current document [id: " + obj.getId() + ", label: " + obj.getLabel() + ", name: " + obj.getName() + "]");
					throw new SpagoBIServiceException(SERVICE_NAME, "Cannot delete customized view: it is not relevant to the current document");
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

		} catch (EMFInternalError e) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An internal error has occured", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
