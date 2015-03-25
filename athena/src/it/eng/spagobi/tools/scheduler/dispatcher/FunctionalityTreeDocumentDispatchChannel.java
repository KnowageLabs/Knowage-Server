/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.massiveExport.services.StartMassiveScheduleAction;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FunctionalityTreeDocumentDispatchChannel implements IDocumentDispatchChannel {

	private DispatchContext dispatchContext;
	
	// logger component
	private static Logger logger = Logger.getLogger(FunctionalityTreeDocumentDispatchChannel.class); 
	
	public FunctionalityTreeDocumentDispatchChannel(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
		try {
			IEngUserProfile userProfile = this.dispatchContext.getUserProfile();
			//gets the dataset data about the folder for the document save
			IDataStore folderDispatchDataSotre = null;
			if (dispatchContext.isUseFolderDataSet()) {
				IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(dispatchContext.getDataSetFolderLabel());
			  	dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(userProfile));
				dataSet.loadData();
				folderDispatchDataSotre = dataSet.getDataStore();
			}
			this.dispatchContext.setFolderDispatchDataSotre(folderDispatchDataSotre);
		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to instatiate DocumentDispatchChannel class", t);
		}
	}
	
	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	public void close() {
		
	}
	
	public boolean canDispatch(BIObject document)  {
		return canDispatch(dispatchContext, document, dispatchContext.getFolderDispatchDataSotre() );
	}
	
	public boolean dispatch(BIObject document, byte[] executionOutput) {
		
		JobExecutionContext jex;
		String fileExt;
		IDataStore folderDispatchDataStore;
		String nameSuffix;
		String descriptionSuffix;
		
		logger.debug("IN");
		
		try {
			
			jex = dispatchContext.getJobExecutionContext();
			fileExt = dispatchContext.getFileExtension();
			folderDispatchDataStore = dispatchContext.getFolderDispatchDataSotre();
			nameSuffix = dispatchContext.getNameSuffix();
			descriptionSuffix = dispatchContext.getDescriptionSuffix();
			
			String docName = dispatchContext.getDocumentName();
			if( (docName==null) || docName.trim().equals("")) {
				throw new Exception(" Document name not specified");
			}
			docName += nameSuffix;
			String docDesc = dispatchContext.getDocumentDescription() + descriptionSuffix;

			// recover office document sbidomains
			IDomainDAO domainDAO = DAOFactory.getDomainDAO();
			Domain officeDocDom = domainDAO.loadDomainByCodeAndValue("BIOBJ_TYPE", "OFFICE_DOC");
			// recover development sbidomains
			Domain relDom = domainDAO.loadDomainByCodeAndValue("STATE", "REL");
			// recover engine
			IEngineDAO engineDAO = DAOFactory.getEngineDAO();
			List engines = engineDAO.loadAllEnginesForBIObjectType(officeDocDom.getValueCd());
			if(engines.isEmpty()) {
				throw new Exception(" No suitable engines for the new document");
			}
			Engine engine = (Engine)engines.get(0);		
			// load the template
			ObjTemplate objTemp = new ObjTemplate();
			objTemp.setActive(new Boolean(true));
			objTemp.setContent(executionOutput);
			objTemp.setName(docName + fileExt);
			// load all functionality
			/*orig
			List storeInFunctionalities = new ArrayList();
			String functIdsConcat = sInfo.getFunctionalityIds();
			String[] functIds =  functIdsConcat.split(",");
			for(int i=0; i<functIds.length; i++) {
				String functIdStr = functIds[i];
				if(functIdStr.trim().equals(""))
					continue;
				Integer functId = Integer.valueOf(functIdStr);
				storeInFunctionalities.add(functId);
			}*/
			List storeInFunctionalities = findFolders(dispatchContext, document, folderDispatchDataStore);
			if(storeInFunctionalities.isEmpty()) {
				throw new Exception(" No functionality specified where store the new document");
			}
			// create biobject

			String jobName = jex.getJobDetail().getName();
			String completeLabel = "scheduler_" + jobName + "_" + docName;
			String label = "sched_" + String.valueOf(Math.abs(completeLabel.hashCode()));

			BIObject newbiobj = new BIObject();
			newbiobj.setDescription(docDesc);
			newbiobj.setCreationUser("scheduler");
			newbiobj.setLabel(label);
			newbiobj.setName(docName);
			newbiobj.setEncrypt(new Integer(0));
			newbiobj.setEngine(engine);
			newbiobj.setDataSourceId(document.getDataSourceId());
			newbiobj.setRelName("");
			newbiobj.setBiObjectTypeCode(officeDocDom.getValueCd());
			newbiobj.setBiObjectTypeID(officeDocDom.getValueId());
			newbiobj.setStateCode(relDom.getValueCd());
			newbiobj.setStateID(relDom.getValueId());
			newbiobj.setVisible(new Integer(1));
			newbiobj.setFunctionalities(storeInFunctionalities);
			IBIObjectDAO objectDAO = DAOFactory.getBIObjectDAO();
			 Timestamp aoModRecDate;
			BIObject biobjexist = objectDAO.loadBIObjectByLabel(label);
			if(biobjexist==null){
				objectDAO.insertBIObject(newbiobj, objTemp);
			} else {
				newbiobj.setId(biobjexist.getId());
				objectDAO.modifyBIObject(newbiobj, objTemp);
			}
		} catch (Throwable t) {
			logger.error("Error while saving schedule result as new document", t );
			return false;
		}finally{
			logger.debug("OUT");
		}
		
		return true;
	}
	
	
	public static boolean canDispatch(DispatchContext dispatchContext, BIObject document, IDataStore folderDispatchDataStore) {
		List storeInFunctionalities = findFolders(dispatchContext, document, folderDispatchDataStore);
		return (storeInFunctionalities != null && !storeInFunctionalities.isEmpty()) ;
	}
	
	private static List findFolders(DispatchContext dispatchContext, BIObject document, IDataStore folderDispatchDataStore) {
		logger.debug("IN");
		List toReturn = null;
		List<String> folders = new ArrayList();
		try {
			folders.addAll(findFoldersFromFixedList(dispatchContext));
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			folders.addAll(findFoldersFromDataSet(dispatchContext, document, folderDispatchDataStore));
		} catch (NullPointerException en) {
			logger.error("Folders defined into dataset " + dispatchContext.getDataSetFolderLabel()+ "  not found.");
		} catch (Exception e) {
			logger.error(e);
		}
		
		toReturn = folders;
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private static List findFoldersFromFixedList(DispatchContext info) throws Exception {
		logger.debug("IN");
		List folders = new ArrayList();
		String functIdsConcat = info.getFunctionalityIds();
		String[] functIds =  functIdsConcat.split(",");
		for(int i=0; i<functIds.length; i++) {
			String functIdStr = functIds[i];
			if(functIdStr.trim().equals(""))
				continue;
			Integer functId = Integer.valueOf(functIdStr);
			folders.add(functId);
		}
		logger.debug("OUT");
		return folders;
	}


	private static List findFoldersFromDataSet(DispatchContext info, BIObject biobj,IDataStore dataStore) throws Exception {
		logger.debug("IN");
		List folders = new ArrayList();
		if (info.isUseFolderDataSet()) {
			logger.debug("Trigger is configured to save documents to folders retrieved by a dataset");
			if (dataStore == null || dataStore.isEmpty()) {
				throw new Exception("The dataset in input is empty!! Cannot retrieve folders from it.");
			}
			// in this case folders must be retrieved by the dataset (which the datastore in input belongs to)
			// we must find the parameter value in order to filter the dataset
			String dsParameterLabel = info.getDataSetFolderParameterLabel();
			logger.debug("The dataset will be filtered using the value of the parameter " + dsParameterLabel);
			// looking for the parameter
			List parameters = biobj.getBiObjectParameters();
			BIObjectParameter parameter = null;
			String codeValue = null;
			Iterator parameterIt = parameters.iterator();
			while (parameterIt.hasNext()) {
				BIObjectParameter aParameter = (BIObjectParameter) parameterIt.next();
				if (aParameter.getLabel().equalsIgnoreCase(dsParameterLabel)) {
					parameter = aParameter;
					break;
				}
			}
			if (parameter == null) {
				throw new Exception("The document parameter with label [" + dsParameterLabel + "] was not found. Cannot filter the dataset.");
			}

			// considering the first value of the parameter
			List values = parameter.getParameterValues();
			if (values == null || values.isEmpty()) {
				throw new Exception("The document parameter with label [" + dsParameterLabel + "] has no values. Cannot filter the dataset.");
			}

			codeValue = (String) values.get(0);
			logger.debug("Using value [" + codeValue + "] for dataset filtering...");
			
			Iterator it = dataStore.iterator();
			while (it.hasNext()) {
				String folder = null;
				IRecord record = (IRecord)it.next();
				// the parameter value is used to filter on the first dataset field
				IField valueField = (IField) record.getFieldAt(0);
				Object valueObj = valueField.getValue();
				String value = null;
				if (valueObj != null) 
					value = valueObj.toString();
				if (codeValue.equals(value)) {
					logger.debug("Found value [" + codeValue + "] on the first field of a record of the dataset.");
					// recipient address is on the second dataset field
					IField folderField = (IField) record.getFieldAt(1);
					Object folderFieldObj = folderField.getValue();
					if (folderFieldObj != null) {
						folder = folderFieldObj.toString();
						logger.debug("Found folder [" + folder + "] on the second field of the record.");
					} else {
						logger.warn("The second field of the record is null.");
					}
				}
				if (folder != null) {
					//get the folder Id corresponding to the label folder and add it to the return list
					try{
						LowFunctionality func = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(folder, false);
						folders.add(func.getId());
					}catch (EMFUserError emf){
						logger.debug("Folder with code: " + folder + " not exists.");
					}
				}
			}
			logger.debug("Folders found from dataset: " + folders.toArray());
		}
		logger.debug("OUT");
		return folders;
	}

}
