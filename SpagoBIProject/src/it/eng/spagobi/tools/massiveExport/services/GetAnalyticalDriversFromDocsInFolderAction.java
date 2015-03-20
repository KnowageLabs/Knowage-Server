/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.massiveExport.services;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.analiticalmodel.execution.service.GetParametersForExecutionAction;
import it.eng.spagobi.analiticalmodel.execution.service.GetParametersForExecutionAction.ParameterForExecution.ParameterDependency;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;

public class GetAnalyticalDriversFromDocsInFolderAction extends GetParametersForExecutionAction {

	private final String SERVICE_NAME = "GET_ANALYTICAL_DRIVER_FROM_DOCS_IN_FOLDER_ACTION";

	// logger component
	private static Logger logger = Logger.getLogger(GetAnalyticalDriversFromDocsInFolderAction.class);

	// type of document to search: all if null
	private final String TYPE = "type";   // for example WORKSHEET

	private final String FUNCTIONALITY_ID = "functId";
	private final String ROLE = "selectedRole";

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
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred");
		}

		Integer folderId = this.getAttributeAsInteger(FUNCTIONALITY_ID);
		String documentType = this.getAttributeAsString(TYPE);
		String execRole = this.getAttributeAsString(ROLE);
		logger.debug("Search folder "+folderId+ " for documents of type "+TYPE);
		logger.debug(ROLE+":"+execRole);

		if(folderId == null){
			logger.error("Functionality id cannot be null");
			throw new SpagoBIServiceException(SERVICE_NAME,"Functionality id cannot be null");
		}

		try {
			LowFunctionality funct = funcDao.loadLowFunctionalityByID(folderId, true);
			Assert.assertNotNull(funct, "functionality with id "+folderId);

			List selObjects = getContainedObjFilteredbyType(funct, documentType );

			Assert.assertNotNull(getContext().getExecutionInstancesAsMap( ExecutionInstance.class.getName() ), "Execution instance cannot be null");
			// for each object I want to execute I must create an executionInstance

			instances = getContext().getExecutionInstancesAsMap( ExecutionInstance.class.getName() );

			// retrieve all parameters
			List<ParameterForExecution> parsToInsert = getParametersInformation(selObjects);

			logger.debug("there are "+parsToInsert.size()+" pars to pass to panel");

			// eliminate dependencies
			Map<String, Boolean> usedIds = new HashMap<String, Boolean>();
			logger.debug("eliminate dependencies for "+parsToInsert+" analytical drivers");
			for (Iterator iterator = parsToInsert.iterator(); iterator.hasNext();) {
				ParameterForExecution parameterForExecution = (ParameterForExecution) iterator
						.next();
				parameterForExecution.setDependencies(new HashMap<String, List<ParameterDependency>>());
				parameterForExecution.setDataDependencies(new ArrayList());
				parameterForExecution.setVisualDependencies(new ArrayList());
				parameterForExecution.setVisible(true);
				
				//mandatory if one of represented pars is mandatory: parameterForExecution.setMandatory(false);
				
				if(usedIds.get(parameterForExecution.getId())== null){
						usedIds.put(parameterForExecution.getId(), true);
				}
				else{
					throw new SpagoBIServiceException(SERVICE_NAME, "Error while retrieving parameters; two analytical driver with different label and different driver are referring to the same url name '"+parameterForExecution.getId()+"'; plese change url name or contact system administrator");
				}
				
			}
			
			
			
			JSONArray parametersJSON = null;
			try {
				parametersJSON = (JSONArray)SerializerFactory.getSerializer("application/json").serialize( parsToInsert, getLocale() );
			} catch (SerializationException e) {
				logger.error("error in serializing in JSOn the parameters for execution");
				throw e;
			}
			writeBackToClient(new JSONSuccess(parametersJSON));


		}
		catch (SpagoBIServiceException e) {
			throw e;
		}
		catch (Throwable e) {
			logger.error("Exception while retrieving analytical drivers in folder with id "+folderId, e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception while retrieving analytical drivers", e);
		}

		logger.debug("OUT");
	}






	public List getContainedObjFilteredbyType(LowFunctionality funct, String docType){
		logger.debug("IN");
		List objList = funct.getBiObjects();
		// filteronly selected type
		List<BIObject> selectedObjects = new ArrayList<BIObject>();
		if(docType == null){
			selectedObjects = objList;
		}
		else {
			for (Iterator iterator = objList.iterator(); iterator.hasNext();) {
				BIObject biObject = (BIObject) iterator.next();
				if(biObject.getBiObjectTypeCode().equals(docType)){
					selectedObjects.add(biObject);
				}

			}
		}
		logger.debug("OUT");
		return selectedObjects;
	}




	/** retrieve allparameters contained in objectsList, two are considered queals if labels and adriver are equals
	 *  
	 * @param objList
	 * @param docType
	 * @return
	 */

	//public List<DriverInfos> getParametersInformation(List<BIObject> objList){
	public List<ParameterForExecution> getParametersInformation(List<BIObject> objList){
		logger.debug("IN");

		List<ParameterForExecution> parsToReturn = new ArrayList<ParameterForExecution>();
		// cycle on objects
		for (Iterator iterator = objList.iterator(); iterator.hasNext();) {
			BIObject biObject = (BIObject) iterator.next();
			logger.debug("analyze document "+biObject.getLabel());

			BIObject objFromExec = instances.get(biObject.getId()).getBIObject();
			// cycle on pars
			List objPars = objFromExec.getBiObjectParameters();

			for (Iterator iterator2 = objPars.iterator(); iterator2.hasNext();) {
				BIObjectParameter par = (BIObjectParameter) iterator2.next();

				if(par.getParameter()==null) continue;

				logger.debug("analyze parameter with label "+par.getLabel()+" and referring to driver "+par.getParameter().getLabel());			

				// check if already inserted
				ParameterForExecution found = contains(parsToReturn, par);
				if(found != null){
					// already found, add the anchor to the object par
					logger.debug("to object parameter with id "+found.getId()+" is associated also object parameter with id "+par.getId());	
					found.getObjParameterIds().add(par.getId());
					// if new parameter found is amndatory also the general becomes mandatory
					ParameterForExecution checkmandatoryPar = new ParameterForExecution(par);
					if(checkmandatoryPar.isMandatory()){
						found.setMandatory(true);
					}
				}
				else{
					// not found, add a new one
					ParameterForExecution toAdd = new ParameterForExecution(par);
					toAdd.getObjParameterIds().add(par.getId());
					parsToReturn.add(toAdd);
				}

			}
		}

		logger.debug("OUT");
		return parsToReturn;
	}

	private ParameterForExecution contains(List<ParameterForExecution> parsToReturn, BIObjectParameter par){
		logger.debug("IN");
		ParameterForExecution toReturn = null;
		for (Iterator iterator = parsToReturn.iterator(); iterator.hasNext() && toReturn == null;) {
			ParameterForExecution parameterForExecution = (ParameterForExecution) iterator.next();
			if(parameterForExecution.getLabel().equals(par.getLabel())
					&&
					parameterForExecution.getPar().getId().equals(par.getParID())
			){
				toReturn = parameterForExecution;
			}
		}
		logger.debug("OUT");
		return toReturn;
	}




}
