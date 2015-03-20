/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
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
public class GetSubObjectsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "GET_SUBOBJECTS_ACTION";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetSubObjectsAction.class);
	
	public void doService() {
		logger.debug("IN");
		ExecutionInstance executionInstance;
		
		try {
			// retrieving execution instance from session, no need to check if user is able to execute the required document
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			Integer biobjectId = executionInstance.getBIObject().getId();
			List subObjectsList = null;
			IEngUserProfile userProfile = this.getUserProfile();
			try {
				if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
					subObjectsList = DAOFactory.getSubObjectDAO().getSubObjects(biobjectId);
				} else {
					subObjectsList = DAOFactory.getSubObjectDAO().getAccessibleSubObjects(biobjectId, userProfile);
				}
			} catch (EMFUserError e) {
				logger.error("Error while recovering subobjects list for document with id = " + biobjectId, e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load customized views", e);
			} catch (EMFInternalError e) {
				logger.error("Error while recovering information about user", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Error while recovering information about user", e);
			}
			
			try {
				JSONArray subObjectsListJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( subObjectsList,null );
				JSONObject results = new JSONObject();
				results.put("results", subObjectsListJSON);
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
