/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.UserProfile;
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
 * @author Andrea Gioia
 */
public class GetFavouritesAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "GET_FAVOURITES_ACTION";
	
	// logger component
	private static Logger logger = Logger.getLogger(GetFavouritesAction.class);
	
	public void doService() {
		logger.debug("IN");
		
		try {
			UserProfile userProfile = (UserProfile)this.getUserProfile();
			String userId = userProfile.getUserId().toString();
			
			List favouriteList = null;
			try {
				favouriteList = DAOFactory.getRememberMeDAO().getMyRememberMe(userId); 
			} catch (Throwable e) {
				logger.error("Error while recovering favourites of user [" + userId + "]", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load scheduled executions", e);
			}
			
			try {
				JSONArray snapshotsListJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( favouriteList ,null);
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
