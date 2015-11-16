/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.massiveExport.services;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.execution.service.GetParametersForExecutionAction;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.tools.massiveExport.utils.Utilities;
import it.eng.spagobi.tools.massiveExport.work.MassiveExportWork;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import commonj.work.WorkEvent;
import commonj.work.WorkItem;

import de.myfoo.commonj.work.FooRemoteWorkItem;

public class StartMassiveExportThreadAction extends AbstractSpagoBIAction {

	private static final long serialVersionUID = 1L;

	private final String SERVICE_NAME = "START_MASSIVE_EXPORT_THREAD_ACTION";

	// Objects recieved
	private final String PARAMETERS_PAGE = "Sbi.browser.mexport.MassiveExportWizardParametersPage";
	private final String OPTIONS_PAGE = "Sbi.browser.mexport.MassiveExportWizardOptionsPage";
	private final String TRIGGER_PAGE = "Sbi.browser.mexport.MassiveExportWizardTriggerPage";


	private final String FUNCTIONALITY_ID = "functId";
	private final String ROLE = "selectedRole";
	private final String MIME_TYPE = "mimeType";
	private final String TYPE = "type";  
	private final String SPLITTING_FILTER = "splittingFilter"; 




	// logger component
	private static Logger logger = Logger.getLogger(StartMassiveExportThreadAction.class); 

	@Override
	public void doService() {

		logger.debug("IN");

		LowFunctionality folder = null;

		List<BIObject> documentsToExport = null;
		Integer progressThreadId = null;

		try{
			Integer folderId = null;
			String documentType = null;
			String role = null; 
			String output = null;
			boolean splittingFilter = false;
			JSONObject parametersJSON = null;
			
			try{
				folderId = this.getAttributeAsInteger(FUNCTIONALITY_ID);
				logger.debug("Input parameter [" + FUNCTIONALITY_ID + "] is equal to [" + folderId + "]");
				Assert.assertNotNull(folderId, "Input parameter [" + FUNCTIONALITY_ID + "] cannot be null");

				documentType = this.getAttributeAsString(TYPE);
				logger.debug("Input parameter [" + TYPE + "] is equal to [" + documentType + "]");

				// get infos from option wizard page
				JSONObject optionsObject = this.getAttributeAsJSONObject(OPTIONS_PAGE);
				logger.debug("Input parameter [" + OPTIONS_PAGE + "] is equal to [" + optionsObject + "]");
				Assert.assertNotNull(optionsObject, "Input parameter [" + OPTIONS_PAGE + "] cannot be null");

				role = optionsObject.getString(ROLE);
				logger.debug("Input parameter [" + ROLE + "] is equal to [" + role + "]");
				Assert.assertNotNull(role, "Input parameter [" + ROLE + "] cannot be null");

				output = optionsObject.getString(MIME_TYPE);
				logger.debug("Input parameter [" + MIME_TYPE + "] is equal to [" + output + "]");
				Assert.assertNotNull(output, "Input parameter [" + MIME_TYPE + "] cannot be null");

				splittingFilter = optionsObject.getBoolean(SPLITTING_FILTER);
				logger.debug("Input parameter [" + SPLITTING_FILTER + "] is equal to [" + splittingFilter + "]");

				parametersJSON = this.getAttributeAsJSONObject(PARAMETERS_PAGE);
				logger.debug("Input parameter [" + PARAMETERS_PAGE + "] is equal to [" + parametersJSON + "]");
				Assert.assertNotNull(parametersJSON, "Input parameter [" + PARAMETERS_PAGE + "] cannot be null");

			} catch (Throwable t) {
				throw new SpagoBIServiceException("Error in retrieving parameters: ", t);
			} 


			ILowFunctionalityDAO functionalityTreeDao = DAOFactory.getLowFunctionalityDAO();
			IProgressThreadDAO progressThreadDAO = DAOFactory.getProgressThreadDAO();
			IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();

			// Get all the documents
			logger.debug("Search folder " + folderId + " for documents of type " + documentType);		
			folder = functionalityTreeDao.loadLowFunctionalityByID(folderId, true);
			Assert.assertNotNull(folder, "Folder [" + folderId + "] cannot be loaded");
			documentsToExport = Utilities.getContainedObjFilteredbyType(folder, documentType );
			fillDriverValues(documentsToExport, parametersJSON);

			logger.debug("Check if userid "+getUserProfile().getUserUniqueIdentifier()+ " and functionality "+folder.getCode()+ " has already a work in execution");
			// search if already exists
			ProgressThread t = progressThreadDAO.loadActiveProgressThreadByUserIdAndFuncCd(getUserProfile().getUserUniqueIdentifier().toString(), folder.getCode());
			if(t != null){
				logger.warn("A massive export process is still opened for userId "+getUserProfile().getUserUniqueIdentifier()+" on functionality "+folder.getCode());
				throw new SpagoBIServiceException(SERVICE_NAME, "A massive export process is still opened for userId "+getUserProfile().getUserUniqueIdentifier()+" on functionality "+folder.getCode());
			}

			String randomName = getRandomName();
			ProgressThread progressThread = new ProgressThread(getUserProfile().getUserUniqueIdentifier().toString(), documentsToExport.size(), folder.getCode(), null, randomName, ProgressThread.TYPE_MASSIVE_EXPORT);
			progressThreadId = progressThreadDAO.insertProgressThread(progressThread);


			Config config = configDAO.loadConfigParametersByLabel(SpagoBIConstants.JNDI_THREAD_MANAGER);
			if(config == null) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to retrive from the configuration the property [" + SpagoBIConstants.JNDI_THREAD_MANAGER + "]");
			}

			WorkManager workManager = new WorkManager(config.getValueCheck());
			MassiveExportWork massiveExportWork = new MassiveExportWork(documentsToExport, getUserProfile(), folder , progressThreadId, randomName, splittingFilter, output);
			FooRemoteWorkItem remoteWorkItem = workManager.buildFooRemoteWorkItem(massiveExportWork, null);

			// Check if work was accepted
			if(remoteWorkItem.getStatus() != WorkEvent.WORK_ACCEPTED){
				int statusWI = remoteWorkItem.getStatus();
				throw new SpagoBIServiceException(SERVICE_NAME, "Massive export Work thread was rejected with status "+statusWI);
			} else {
				logger.debug("run work item");
				//WorkItem workItem=(WorkItem)wm.runWithReturnWI(mew, mewListener);
				WorkItem workItem=(WorkItem)workManager.runWithReturnWI(massiveExportWork, null);
				int statusWI=workItem.getStatus();
			}

		} catch (SpagoBIServiceException t) {
			if(progressThreadId != null){
				deleteDBRowInCaseOfError(progressThreadId);
			}
			throw (t);		
		} catch (Throwable t) {
			if(progressThreadId != null){
				deleteDBRowInCaseOfError(progressThreadId);
			}
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occuerd while executing service ["+ SERVICE_NAME + "]", t);
		} finally {
			logger.debug("OUT");
		}

	}

	private void deleteDBRowInCaseOfError(Integer progressThreadId){
		IProgressThreadDAO threadDAO ;

		logger.debug("IN");
		try {
			threadDAO = DAOFactory.getProgressThreadDAO();
			threadDAO.deleteProgressThread(progressThreadId);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occuerd while edeleting the row with progress id equal to [" + progressThreadId + "]", t);
		} finally {
			logger.debug("OUT");
		}

	}

	void fillDriverValues(List<BIObject> documents, JSONObject parametersJSON) throws JSONException {
		logger.debug("IN");

		for (BIObject document : documents) {
			logger.debug("fill values of object "+document.getLabel());
			List<BIObjectParameter> documentParameters = document.getBiObjectParameters();
			for (BIObjectParameter documentParameter : documentParameters) {
				logger.debug("search value for obj par with id  "+documentParameter.getId());
				String documentParameterLabel = parametersJSON.getString(documentParameter.getId().toString()+"_objParameterId");

				List<String> documentParameterValues = new ArrayList<String>();
				List<String> documentParameterValuesDescription = new ArrayList<String>();
				boolean isMultivalueParameter = false;
				
				if(documentParameterLabel != null){
					
					JSONArray values = parametersJSON.optJSONArray(documentParameterLabel);
					boolean isValued = false;
					if(values != null && values.length()>0){
						isMultivalueParameter = true;	
						for (int i = 0; i < values.length(); i++) {
							String ob = values.getString(i);
							documentParameterValues.add(ob);
							isValued = true;
							logger.debug("multivalue, value is "+ob);
						}
					}
					if(isMultivalueParameter == false){
						String value = parametersJSON.getString(documentParameterLabel);
						if(value != null){
							documentParameterValues.add(value);
							isValued = true;
							logger.debug("single value, value is "+value);
						}

					}

					 // get also descriptions
					String valuesDescr = parametersJSON.optString(documentParameterLabel+"_field_visible_description");
					if(valuesDescr != null){
							documentParameterValuesDescription.add(valuesDescr);
							logger.debug("multivalue, description value is "+valuesDescr);				
						}							


				} else{
					logger.warn("parameter value not defined  "+documentParameter.getLabel());
				}

				// check for mandatory violation
				isMandatoryViolation(documentParameter, parametersJSON, documentParameterValues, isMultivalueParameter);
				logger.debug("insert for "+documentParameter.getLabel()+" value"+ documentParameterValues.toString());
				documentParameter.setParameterValues(documentParameterValues);
				documentParameter.setParameterValuesDescription(documentParameterValuesDescription);

			}


		}


		logger.debug("OUT");
	}


	private String getRandomName(){
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yy hh:mm:ss.SSS");
		String randomName = formatter.format(new Date());			
		randomName=randomName.replaceAll(" ", "_");
		randomName=randomName.replaceAll(":", "-");
		//randomName = "Massive_Export_"+randomName;
		return randomName;

	}

	private void isMandatoryViolation(BIObjectParameter parameter
			, JSONObject parametersJSON, List<String> values, boolean isMultivalueParameter) throws JSONException{
		logger.debug("IN");
		boolean mandatory = false;
		Boolean mandatoryString = parametersJSON.optBoolean(parameter.getLabel()+"_isMandatory");
		if(mandatoryString != null && mandatoryString.equals(true) ){
			mandatory = true;
		}
		logger.debug("parameter with label "+parameter.getLabel()+" is mandatory: "+mandatory);
		// check if mandatory and value is empty throw exception
		if(mandatory){
			logger.debug("value of parameter "+parameter.getLabel()+" is "+values);
			if(values == null || values.size()==0 || values.get(0).equals("") || values.get(0).equals("[]")){
				logger.error("Mandatory parameter "+parameter.getLabel()+" must be filled");
				throw new SpagoBIServiceException(SERVICE_NAME, "Mandatory parameter "+parameter.getLabel()+" must be filled", null);
				}
		}

		logger.debug("OUT");
	}
	
}
