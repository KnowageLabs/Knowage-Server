/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.massiveExport.services;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.execution.service.GetParametersForExecutionAction;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.tools.massiveExport.utils.Utilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class StartMassiveExportExecutionProcessAction extends GetParametersForExecutionAction {

	private final String SERVICE_NAME = "START_MASSIVE_EXPORT_EXECUTION_PROCESS_ACTION";

	// logger component
	private static Logger logger = Logger.getLogger(StartMassiveExportExecutionProcessAction.class);

	// type of document to search: all if null
	private final String TYPE = "type";   // for wxample WORKSHEET

	private final String MODALITY = "MODALITY";
	private final String FUNCTIONALITY_ID = "functId";
	private final String FUNCTIONALITY_CD = "functCd";
	private final String ROLE = "selectedRole";

	private final String RETRIEVE_DOCUMENTS_MODALITY = "RETRIEVE_DOCUMENTS_MODALITY";
	private final String CREATE_EXEC_CONTEST_ID_MODALITY = "CREATE_EXEC_CONTEST_ID_MODALITY";


	Map<Integer, ExecutionInstance> instances; 

	ExecutionInstance executionInstance;

	@Override
	public void doService() {
		logger.debug("IN");

		IBIObjectDAO biObjDao;
		ILowFunctionalityDAO funcDao;


		try {
			biObjDao = DAOFactory.getBIObjectDAO();
			funcDao = DAOFactory.getLowFunctionalityDAO();
		} catch (EMFUserError e1) {
			logger.error(e1.getMessage(), e1);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred", e1);
		}

		Integer folderId = this.getAttributeAsInteger(FUNCTIONALITY_ID);
		String modality = this.getAttributeAsString(MODALITY);
		Assert.assertNotNull(modality, "modality cannot be null");
		logger.debug("MODALITY "+modality );
		logger.debug("FolderId "+folderId);


		String documentType = this.getAttributeAsString(TYPE);
		logger.debug("FolderdocumetnType "+documentType);

		if(folderId == null){
			logger.error("Functionality id cannot be null");
			throw new SpagoBIServiceException(SERVICE_NAME,"Functionality id cannot be null");
		}		


		JSONObject responseJSON = null;
		IEngUserProfile profile = getUserProfile();
		logger.debug("user is "+profile.getUserUniqueIdentifier());
		try {

			LowFunctionality funct = funcDao.loadLowFunctionalityByID(folderId, true);
			Assert.assertNotNull(funct, "functionality with id "+folderId+" cannot be null");
			List selObjects = Utilities.getContainedObjFilteredbyType(funct, documentType );

			if(modality.equals(RETRIEVE_DOCUMENTS_MODALITY)){
				JSONArray docsArray = new JSONArray();
				for (int i =0; i < selObjects.size() ;i++) {
					BIObject obj = (BIObject) selObjects.get(i);

					boolean canSee = ObjectsAccessVerifier.canSee(obj, profile);
					if(canSee){
						boolean canExec = ObjectsAccessVerifier.isAbleToExec(obj.getStateCode(), profile);
						if(canExec){
							String label = obj.getName();
							docsArray.put(i, label);
							logger.debug("retrieve document "+label);
						}
						else{
							logger.debug(profile + " user cannot exec document "+obj.getName());
						}
					}
					else{
						logger.debug(profile + " user cannot see document "+obj.getName());
					}
				}
				logger.debug("retrieved "+docsArray.length()+" documents of type "+documentType);

				responseJSON = new JSONObject();
				responseJSON.put("selectedDocuments", docsArray);

			}
			else if(modality.equals(CREATE_EXEC_CONTEST_ID_MODALITY)){

				String execRole = this.getAttributeAsString(ROLE);
				logger.debug("Search folder "+folderId+ " for documents of type "+TYPE);

				// for each object I want to execute I must create an executionInstance
				String executionContextId = createExecutionInstances(selObjects, execRole);
				logger.debug("execution context id is "+executionContextId);

				// ExecutionInstance has been created it's time to prepare the response with the instance unique id and flush it to the client
				responseJSON = new JSONObject();
				responseJSON.put("execContextId", executionContextId);
			}



			writeBackToClient( new JSONSuccess( responseJSON ) );

		} 
		catch (EMFUserError e) {
			logger.error("EMFUserError happened during action "+SERVICE_NAME+" called with modality "+modality, e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error happened while retrieving documents: \n"+e.getDescription(), e);
		}
		catch (Throwable e) {
			logger.error("generic error happened during action "+SERVICE_NAME+" called with modality "+modality, e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error happened while retrieving documents", e);
		}

		logger.debug("OUT");
	}











	/**create context with a map associating biObject id with its executionInstance
	 * 
	 * @param selObjects
	 * @param execRole
	 * @throws Exception 
	 */

	private String createExecutionInstances(List selObjects, String execRole) throws Exception{
		logger.debug("IN");

		// create execution id
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		String executionId = uuidObj.toString();
		String executionContextId = executionId.replaceAll("-", "");
		logger.debug("created random execution id "+executionId);

		CoreContextManager ccm = createContext( executionContextId );

		instances = new HashMap<Integer, ExecutionInstance>();
		for (Iterator iterator = selObjects.iterator(); iterator.hasNext();) {
			BIObject biObj = (BIObject) iterator.next();
			Integer biObjectid = biObj.getId();
			ExecutionInstance executionInstance = createExecutionInstance(biObjectid, execRole, executionContextId);
			instances.put(biObjectid, executionInstance);
		}
		ccm.set(ExecutionInstance.class.getName(), instances);
		logger.debug("OUT");
		return executionContextId;

	}

	private ExecutionInstance createExecutionInstance(Integer biobjectId, String aRoleName, String executionId) throws Exception {
		logger.debug("IN");
		String executionFlowId = getAttributeAsString("EXECUTION_FLOW_ID");
		Boolean displayToolbar = false;
		Boolean displaySlider = false;
		String modality = SpagoBIConstants.MASSIVE_EXPORT_MODALITY;


		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuidObj2 = uuidGen.generateTimeBasedUUID();
		String executionContextId = uuidObj2.toString();
		executionContextId = executionContextId.replaceAll("-", "");

		if (executionFlowId == null) executionFlowId = executionId;

		// create new execution instance
		ExecutionInstance instance = null;
		try {
			instance = new ExecutionInstance(getUserProfile(), executionFlowId, executionContextId, biobjectId, aRoleName, modality, 
					displayToolbar.booleanValue(), displaySlider.booleanValue(), getLocale());
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
		logger.debug("OUT");
		return instance;
	}




}
