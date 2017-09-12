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

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetViewpointsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "GET_VIEWPOINTS_SERVICE";
	
	// request parameters
	public static String DOCUMENT_ID = ObjectsTreeConstants.OBJECT_ID;
	public static String DOCUMENT_LABEL = ObjectsTreeConstants.OBJECT_LABEL;
	
	// logger component
	private static Logger logger = Logger.getLogger(GetViewpointsAction.class);
	
	
	public void doService() {
		
		List viewpoints;
		ExecutionInstance executionInstance;
		IEngUserProfile userProfile;
		Integer biobjectId;
		IViewpointDAO viewpointDAO;
		
		
		
		logger.debug("IN");
		
		try{
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null");
			
			userProfile = this.getUserProfile();
			Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
			
			biobjectId = executionInstance.getBIObject().getId();
			Assert.assertNotNull(executionInstance, "Impossible to retrive analytical document id");
			
			logger.debug("User: [" + userProfile.getUserUniqueIdentifier() + "]");
			logger.debug("Document Id:  [" + biobjectId + "]");
			
			try {
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpoints = viewpointDAO.loadAccessibleViewpointsByObjId(biobjectId, getUserProfile());
			} catch (EMFUserError e) {
				logger.error("Cannot load viewpoints for document [" + biobjectId + "]", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot load viewpoints for document [" + biobjectId + "]", e);
			}
					
			logger.debug("Document [" + biobjectId + "] have " + (viewpoints==null?"0":""+viewpoints.size() ) + " valid viewpoints for user [" + userProfile.getUserUniqueIdentifier() + "]");
			
			try {
				JSONArray viewpointsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize( viewpoints ,null);
				JSONObject results = new JSONObject();
				results.put("results", viewpointsJSON);
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
