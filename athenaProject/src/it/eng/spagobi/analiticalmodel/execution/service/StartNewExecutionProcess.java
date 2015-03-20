/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class StartNewExecutionProcess extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "START_NEW_EXECUTION";
	
	// request parameters
	public static String DOCUMENT_ID = ObjectsTreeConstants.OBJECT_ID;
	public static String DOCUMENT_VERSION = ObjectsTreeConstants.OBJECT_VERSION;	
	public static String DOCUMENT_LABEL = ObjectsTreeConstants.OBJECT_LABEL;
	public static String EXECUTION_ROLE = SpagoBIConstants.ROLE;
	
	// logger component
	private static Logger logger = Logger.getLogger(StartNewExecutionProcess.class);
	
	
	public void doService() {
		ExecutionInstance instance;
		
		Integer documentId;
		String documentLabel;
		Integer documentVersion;
		String executionRole;
		String userProvidedParametersStr;
		
		BIObject obj;
		IEngUserProfile profile;
		List roles;
		
		logger.debug("IN");
		
		try {
			
			profile = getUserProfile();
			documentId = requestContainsAttribute( DOCUMENT_ID )? getAttributeAsInteger( DOCUMENT_ID ): null;
			documentVersion = requestContainsAttribute( DOCUMENT_VERSION )? getAttributeAsInteger( DOCUMENT_VERSION ): null;
			documentLabel = getAttributeAsString( DOCUMENT_LABEL );			
			executionRole = getAttributeAsString( EXECUTION_ROLE );
			userProvidedParametersStr = getAttributeAsString(ObjectsTreeConstants.PARAMETERS);
			
			logger.debug("Parameter [" + DOCUMENT_ID + "] is equals to [" + documentId + "]");
			logger.debug("Parameter [" + DOCUMENT_LABEL + "] is equals to [" + documentLabel + "]");
			logger.debug("Parameter [" + DOCUMENT_VERSION + "] is equals to [" + documentVersion + "]");			
			logger.debug("Parameter [" + EXECUTION_ROLE + "] is equals to [" + executionRole + "]");
			
			Assert.assertTrue(!StringUtilities.isEmpty( documentLabel ) || documentId != null, 
					"At least one between [" + DOCUMENT_ID + "] and [" + DOCUMENT_LABEL + "] parameter must be specified on request");
			
			Assert.assertTrue(!StringUtilities.isEmpty( executionRole ), "Parameter [" + EXECUTION_ROLE + "] cannot be null");
			
			// load object to chek if it exists
			obj = null;
			if ( !StringUtilities.isEmpty( documentLabel ) ) {
				logger.debug("Loading document with label = [" + documentLabel + "] ...");
				try {
					obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(documentLabel);
				} catch (EMFUserError error) {
					logger.error("Object with label equals to [" + documentLabel + "] not found");
					throw new SpagoBIServiceException(SERVICE_NAME, "Object with label equals to [" + documentId + "] not found", error);
				}		
			} else if ( documentId != null ) {
				logger.info("Loading biobject with id = [" + documentId + "] ...");
				try {
					obj = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
				} catch (EMFUserError error) {
					logger.error("Object with id equals to [" + documentId + "] not found");
					throw new SpagoBIServiceException(SERVICE_NAME, "Object with id equals to [" + documentId + "] not found", error);
				}
			} else {
				Assert.assertUnreachable("At least one between [" + DOCUMENT_ID + "] and [" + DOCUMENT_LABEL + "] parameter must be specified on request");
			}
			Assert.assertNotNull(obj, "Impossible to load document");
			logger.debug("... docuemnt loaded succesfully");
			//if into the request is specified a version of the template to use it's signed into the object.  
			if (documentVersion!=null) obj.setDocVersion(documentVersion);
			
			// retrive roles for execution
			try {
				roles = ObjectsAccessVerifier.getCorrectRolesForExecution(obj.getId(), profile);
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, t);			
			} 
			
			if (roles != null && !roles.contains(executionRole)) {
				logger.error("Document [id: " + obj.getId() +"; label: " + obj.getLabel() + " ] cannot be executed by any role of the user [" + profile.getUserUniqueIdentifier() + "]");
				throw new SpagoBIServiceException(SERVICE_NAME, "Document [id: " + obj.getId() +"; label: " + obj.getLabel() + " ] cannot be executed by any role of the user [" + profile.getUserUniqueIdentifier() + "]");
			}
			
		
			
			// so far so good: everything has been validated successfully. Let's create a new ExecutionInstance.
			//instance = createExecutionInstance(obj.getId(), executionRole);
			
			UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			String executionContextId = uuidObj.toString();
			executionContextId = executionContextId.replaceAll("-", "");
			
			CoreContextManager ccm = createContext( executionContextId );
			   // so far so good: everything has been validated successfully. Let's create a new ExecutionInstance.
			instance = createExecutionInstance(obj.getId(), obj.getDocVersion(), executionRole, executionContextId, getLocale());
			   
			createContext( executionContextId ).set(ExecutionInstance.class.getName(), instance);
			

			   
			
			
			//instance.refreshParametersValues(getSpagoBIRequestContainer().getRequest(), true);
			//instance.setParameterValues(userProvidedParametersStr, true);
			
			// refresh obj variable because createExecutionInstance load the BIObject in a different way
			//obj = instance.getBIObject();	
			
			
			// ExecutionInstance has been created it's time to prepare the response with the instance unique id and flush it to the client
			JSONObject responseJSON = null;
			responseJSON = new JSONObject();
			try {
				responseJSON.put("execContextId", executionContextId);				
			} catch (JSONException e) {
				throw new SpagoBIServiceException("Impossible to serialize response", e);
			}
			
			try {
				writeBackToClient( new JSONSuccess( responseJSON ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
			}
			
		} finally {
			logger.debug("OUT");
		}
	}
	
	private ExecutionInstance createExecutionInstance(Integer biobjectId,Integer biobjectVersion,  String aRoleName, String execId, Locale locale) {
		String executionFlowId = getAttributeAsString("EXECUTION_FLOW_ID");
		Boolean displayToolbar = getAttributeAsBoolean(SpagoBIConstants.TOOLBAR_VISIBLE, true);
		Boolean displaySlider = getAttributeAsBoolean(SpagoBIConstants.SLIDERS_VISIBLE, true);
		String modality = requestContainsAttribute(ObjectsTreeConstants.MODALITY)
							? getAttributeAsString(ObjectsTreeConstants.MODALITY)
							: SpagoBIConstants.NORMAL_EXECUTION_MODALITY;
		
		// create execution id
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		String executionId = uuidObj.toString();
		executionId = executionId.replaceAll("-", "");
		
		if (executionFlowId == null) executionFlowId = executionId;
				
		// create new execution instance
		ExecutionInstance instance = null;
		try {
			instance = new ExecutionInstance(getUserProfile(), executionFlowId, execId, biobjectId, biobjectVersion, aRoleName, modality, 
					displayToolbar.booleanValue(), displaySlider.booleanValue(), locale);
		} catch (Exception e) {
			logger.error(e);
		}
		return instance;
	}

}
