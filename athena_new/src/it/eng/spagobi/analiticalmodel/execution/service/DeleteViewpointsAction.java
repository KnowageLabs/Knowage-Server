/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DeleteViewpointsAction extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "DELETE_VIEWPOINTS_SERVICE";

	
	
	// request parameters
	private static final String VIEWPOINT_IDS = "viewpoint_ids";
	
	// logger component
	private static Logger logger = Logger.getLogger(DeleteViewpointsAction.class);
	
	
	public void doService() {
		
		ExecutionInstance executionInstance;
		IEngUserProfile userProfile;
		Integer biobjectId;
		
		String viewpointIds;
		String[] ids;
		
		IViewpointDAO viewpointDAO;
		Viewpoint viewpoint;
		
		logger.debug("IN");
		
		try {
			
			viewpointIds = this.getAttributeAsString(VIEWPOINT_IDS);
			
			logger.debug("Parameter [" + VIEWPOINT_IDS + "] is equals to [" + viewpointIds + "]");			
			Assert.assertTrue(!StringUtilities.isEmpty(viewpointIds), "Viewpoint's ids cannot be null or empty");
			
			ids = viewpointIds.split(",");
			
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			Assert.assertNotNull(executionInstance, "Execution instance cannot be null");
			
			userProfile = this.getUserProfile();
			Assert.assertNotNull(userProfile, "Impossible to retrive user profile");
			
			biobjectId = executionInstance.getBIObject().getId();
			Assert.assertNotNull(executionInstance, "Impossible to retrive analytical document id");
			
			logger.debug("User: [" + userProfile.getUserUniqueIdentifier() + "]");
			logger.debug("Document Id:  [" + biobjectId + "]");
			
			/*
			Assert.assertTrue(userProfile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN), 
					"User [" + userProfile.getUserUniqueIdentifier()+ "] have not the rights to delete viewpoints");
			*/
			for(int i = 0; i < ids.length; i++) {
				try {
					viewpointDAO = DAOFactory.getViewpointDAO();
					viewpoint =  viewpointDAO.loadViewpointByID( Integer.valueOf(ids[i]));					
					Assert.assertNotNull(viewpoint, "Viewpoint [" + ids[i] + "] does not exist on the database");
					
					
					viewpointDAO.eraseViewpoint( viewpoint.getVpId() );
					logger.error("Viewpoint [" + ids[i] + "] succesfully deleted");
					
				} catch (EMFUserError e) {
					logger.error("Impossible to delete viewpoint with name [" + ids[i] + "] already exists", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to delete viewpoint with name [" + ids[i] + "] already exists", e);
				}
			}
			
			try {
				JSONObject results = new JSONObject();
				writeBackToClient( new JSONAcknowledge() );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} 

		} finally {
			logger.debug("OUT");
		}
		
	}

}
