/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SaveViewpointAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "SAVE_VIEWPOINTS_SERVICE";

	

	
	// request parameters
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String SCOPE = "scope";
	private static final String VIEWPOINT = "viewpoint";
	
	// logger component
	private static Logger logger = Logger.getLogger(SaveViewpointAction.class);
	
	
	public void doService() {
		
		ExecutionInstance executionInstance;
		IEngUserProfile userProfile;
		Integer biobjectId;
		
		String viewpointName;
		String viewpointDescription;
		String viewpointScope;
		String viewpointOwner;
		JSONObject viewpointJSON;
		String viewpointString;
		
		IViewpointDAO viewpointDAO;
		Viewpoint viewpoint;
		
		logger.debug("IN");
		
		try{
			
			viewpointName = getAttributeAsString(NAME);
			viewpointDescription = getAttributeAsString(DESCRIPTION);
			viewpointScope = getAttributeAsString(SCOPE);
			viewpointJSON = getAttributeAsJSONObject( VIEWPOINT );
			
			logger.debug("Parameter [" + NAME + "] is equals to [" + viewpointName + "]");
			logger.debug("Parameter [" + DESCRIPTION + "] is equals to [" + viewpointDescription + "]");
			logger.debug("Parameter [" + SCOPE + "] is equals to [" + viewpointScope + "]");
			logger.debug("Parameter [" + viewpointScope + "] is equals to [" + viewpointJSON + "]");
			
			Assert.assertTrue(!StringUtilities.isEmpty(viewpointScope), "Viewpoint's name cannot be null or empty");
			Assert.assertNotNull(!StringUtilities.isEmpty(viewpointDescription), "Viewpoint's description cannot be null or empty");
			Assert.assertNotNull(!StringUtilities.isEmpty(viewpointScope), "Viewpoint's scope cannot be null or empty");
			Assert.assertNotNull(viewpointJSON, "Viewpoint's content cannot be null");
			
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null");
			
			userProfile = this.getUserProfile();
			Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
			
			biobjectId = executionInstance.getBIObject().getId();
			Assert.assertNotNull(executionInstance, "Impossible to retrive analytical document id");
			
			logger.debug("User: [" + userProfile.getUserUniqueIdentifier() + "]");
			logger.debug("Document Id:  [" + biobjectId + "]");
			
			viewpointOwner = (String) ((UserProfile)userProfile).getUserId();
			
			Iterator it = viewpointJSON.keys();
			Assert.assertTrue(it.hasNext(), "Viewpoint's content cannot be empty");
			viewpointString = "";
			while (it.hasNext()) {
				String parameterName = (String) it.next();
				String parameterValue;
				try {
					parameterValue = viewpointJSON.getString( parameterName );
				} catch (JSONException e) {
					logger.error("Impossible read value for the parameter [" + parameterName + "] into viewpoint's content", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible read value for the parameter [" + parameterName + "] into viewpoint's content", e);
				}
				
				// defines the string of parameters to save into db
				if(!StringUtilities.isEmpty(parameterValue)) {
					viewpointString += parameterName + "%3D" + parameterValue + "%26";
				}
			}
			
			if (viewpointString.endsWith("%26")) {
				viewpointString = viewpointString.substring(0, viewpointString.length() - 3);
			}
			
			logger.debug("Viewpoint's content will be saved on database as: [" + viewpointString + "]");
			
			try {
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpoint = viewpointDAO.loadViewpointByNameAndBIObjectId(viewpointName, biobjectId);
				if (viewpoint != null) throw new SpagoBIServiceException(SERVICE_NAME, "A viewpoint with the name [" + viewpointName + "] alredy exist");
				//Assert.assertTrue(viewpoint == null, "A viewpoint with the name [" + viewpointName + "] alredy exist");
			} catch (EMFUserError e) {
				logger.error("Impossible to check if a viewpoint with name [" + viewpointName + "] already exists", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to check if a viewpoint with name [" + viewpointName + "] already exists", e);
			}	
			
			try {
				viewpointDAO = DAOFactory.getViewpointDAO();
				viewpointDAO.setUserProfile(userProfile);
				viewpoint = new Viewpoint();
				viewpoint.setBiobjId( biobjectId );
				viewpoint.setVpName( viewpointName );
				viewpoint.setVpOwner( viewpointOwner );
				viewpoint.setVpDesc( viewpointDescription );
				viewpoint.setVpScope( viewpointScope );
				viewpoint.setVpValueParams( viewpointString );
				viewpoint.setVpCreationDate(new Timestamp(System.currentTimeMillis()));
				viewpointDAO.insertViewpoint(viewpoint);
				
				//reload viewpoint with new ID
				viewpoint = viewpointDAO.loadViewpointByNameAndBIObjectId(viewpointName, biobjectId);
				
				
			} catch (EMFUserError e) {
				logger.error("Impossible to save viewpoint [" + viewpointName + "]", e);
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to check if a viewpoint with name [" + viewpointName + "] already exists", e);
			}
				
			try {
				JSONObject results = (JSONObject) SerializerFactory.getSerializer("application/json").serialize( viewpoint,null );
				writeBackToClient( new JSONSuccess( results ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} catch (SerializationException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot serialize objects", e);
			} 
			
		} finally {
			logger.debug("OUT");
		}
		
	}

}
