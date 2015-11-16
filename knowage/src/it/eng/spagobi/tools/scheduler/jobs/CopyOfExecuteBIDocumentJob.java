/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.jobs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionController;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterValuesRetriever;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.events.EventsManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.bo.Email;
import it.eng.spagobi.tools.scheduler.Formula;
import it.eng.spagobi.tools.scheduler.FormulaParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.RuntimeLoadingParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.utils.BIObjectParametersIterator;
import it.eng.spagobi.tools.scheduler.utils.JavaClassDestination;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;


public class CopyOfExecuteBIDocumentJob implements Job {

	static private Logger logger = Logger.getLogger(CopyOfExecuteBIDocumentJob.class);	

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		IEngUserProfile profile;
		JobDataMap jobDataMap;
		
		// documentLabel__num this is necessary because the same document can be added to one scheduled activity more than one time
		String documentInstanceName;
		String documentLabel;
		
		// par1=val1&par2=val2... for parameters already set in scheduled activity's configuration
		String inputParametersQueryString;
		
		IBIObjectDAO biobjdao;
		BIObject biobj;
		ExecutionController executionController;
		ExecutionProxy executionProxy;
		EventsManager eventManager;
		
		logger.debug("IN");
		
		try {
			profile = UserProfile.createSchedulerUserProfile();
			jobDataMap = jobExecutionContext.getMergedJobDataMap();
			biobjdao = DAOFactory.getBIObjectDAO();
			
			String doclabelsConcat = jobDataMap.getString("documentLabels");
			String[] docLabels = doclabelsConcat.split(",");
			Iterator itr = jobDataMap.keySet().iterator();
			while(itr.hasNext()) {
				Object key = itr.next();
				Object value = jobDataMap.get(key);
				logger.debug("jobDataMap parameter [" + key + "] is equal to [" + value + "]");
			}
			
			long startSchedule = System.currentTimeMillis();
			logger.debug("Scheduled activity contains [" + docLabels.length + "] documnt(s)");

			for(int ind = 0; ind < docLabels.length; ind++) {
				documentInstanceName = docLabels[ind];
				documentLabel = documentInstanceName.substring(0, documentInstanceName.lastIndexOf("__"));
				logger.debug("Processing document [" + (ind+1) + "] with label [" + documentLabel + "] ...");
				
				inputParametersQueryString = jobDataMap.getString(documentInstanceName);
				logger.debug("Input parameters query string for documet [" + documentLabel + "] is equal to [" + inputParametersQueryString + "]");
				
				// load bidocument
				biobj = biobjdao.loadBIObjectByLabel(documentLabel);
				
				// get the save options
				String saveOptString = jobDataMap.getString("biobject_id_" + biobj.getId() + "__"+ (ind+1));
				DispatchContext saveInfo = SchedulerUtilities.decodeDispatchContext(saveOptString);
				
				// create the execution controller 
				executionController = new ExecutionController();
				executionController.setBiObject(biobj);
				
				// fill parameters 
				executionController.refreshParameters(biobj, inputParametersQueryString);

				String iterativeParametersString = jobDataMap.getString(documentInstanceName + "_iterative");
				logger.debug("Iterative parameter configuration for documet [" + documentLabel + "] is equal to [" + iterativeParametersString + "]");
				setIterativeParameters(biobj, iterativeParametersString);
				
				String loadAtRuntimeParametersString = jobDataMap.getString(documentInstanceName + "_loadAtRuntime");
				logger.debug("Runtime parameter configuration for documet [" + documentLabel + "] is equal to [" + loadAtRuntimeParametersString + "]");
				setLoadAtRuntimeParameters(biobj, loadAtRuntimeParametersString);
				
				String useFormulaParametersString = jobDataMap.getString(documentInstanceName + "_useFormula");
				logger.debug("Formuula based parameter configuration for documet [" + documentLabel + "] is equal to [" + useFormulaParametersString + "]");
				setUseFormulaParameters(biobj, useFormulaParametersString);

				retrieveParametersValues(biobj);

				//gets the dataset data about the email address
				IDataStore emailDispatchDataStore = null;
				if (saveInfo.isUseDataSet()) {
					IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(saveInfo.getDataSetLabel());
					dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
					dataSet.loadData();
					emailDispatchDataStore = dataSet.getDataStore();
				}
				//gets the dataset data about the folder for the document save
				IDataStore folderDispatchDataSotre = null;
				if (saveInfo.isUseFolderDataSet()) {
					IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(saveInfo.getDataSetFolderLabel());
				  	dataSet.setUserProfileAttributes(UserProfileUtils.getProfileAttributes(profile));
					dataSet.loadData();
					 folderDispatchDataSotre = dataSet.getDataStore();
				}
				
				eventManager = EventsManager.getInstance();
				List roles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(biobj.getId());
				
				String startExecMsg = "${scheduler.startexecsched} " + biobj.getName();	
				Integer idEvent = eventManager.registerEvent("Scheduler", startExecMsg, "", roles);

				
				Map tempParMap = new HashMap();
				BIObjectParametersIterator objectParametersIterator = new BIObjectParametersIterator(biobj.getBiObjectParameters());
				while (objectParametersIterator.hasNext()) {
					List parameters = (List) objectParametersIterator.next();
					biobj.setBiObjectParameters(parameters);
				

					StringBuffer toBeAppendedToName = new StringBuffer();
					StringBuffer toBeAppendedToDescription = new StringBuffer(" [");
					Iterator parametersIt = parameters.iterator();
					while (parametersIt.hasNext()) {
						
						BIObjectParameter aParameter = (BIObjectParameter) parametersIt.next();
						
						tempParMap.put(aParameter.getParameterUrlName(), aParameter.getParameterValuesAsString());
						if (aParameter.isIterative()) {
							toBeAppendedToName.append("_" + aParameter.getParameterValuesAsString());
							toBeAppendedToDescription.append(aParameter.getLabel() + ":" + aParameter.getParameterValuesAsString() + "; ");
						}
					}
					// if there are no iterative parameters, toBeAppendedToDescription is " [" and must be cleaned
					if (toBeAppendedToDescription.length() == 2) {
						toBeAppendedToDescription.delete(0, 2);
					} else {
						// toBeAppendedToDescription ends with "; " and must be cleaned
						toBeAppendedToDescription.delete(toBeAppendedToDescription.length() - 2, toBeAppendedToDescription.length());
						toBeAppendedToDescription.append("]");
					}

					// appending the current date
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat();
					sdf.applyPattern("dd:MM:yyyy");
					String dateStr = sdf.format(date);
					toBeAppendedToName.append("_" + dateStr);
					
					

					//check parameters value: if a parameter hasn't value but isn't mandatory the process 
					//must go on and so hasValidValue is set to true
					List tmpBIObjectParameters = biobj.getBiObjectParameters();
					Iterator it = tmpBIObjectParameters.iterator();
					while (it.hasNext()){
						boolean isMandatory = false;
						BIObjectParameter aBIObjectParameter = (BIObjectParameter)it.next();
						List checks = aBIObjectParameter.getParameter().getChecks();
						if (checks != null && !checks.isEmpty()) {
							Iterator checksIt = checks.iterator();
							while (checksIt.hasNext()) {
								Check check = (Check) checksIt.next();
								if (check.getValueTypeCd().equalsIgnoreCase("MANDATORY")) {
									isMandatory = true;
									break;
								}
							}
						}
						if (!isMandatory && 
								(aBIObjectParameter.getParameterValues() == null  || aBIObjectParameter.getParameterValues().size() == 0)) {
							aBIObjectParameter.setParameterValues(new ArrayList());
							aBIObjectParameter.setHasValidValues(true);
						}
					}


					// exec the document only if all its parameter are filled
					if(executionController.directExecution()) {
						
						logger.debug("Save as snapshot is eual to [" + saveInfo.isSnapshootDispatchChannelEnabled() + "]");
						logger.debug("Dispatch to a distribution list is eual to [" + saveInfo.isDistributionListDispatchChannelEnabled() + "]");
						logger.debug("Dispatch to a java class is eual to [" + saveInfo.isJavaClassDispatchChannelEnabled() + "]");
						logger.debug("Dispatch by mail-list is eual to [" + saveInfo.isMailDispatchChannelEnabled() + "]");
						logger.debug("Dispatch by folder-list is eual to [" + saveInfo.isFunctionalityTreeDispatchChannelEnabled() + "]");
						
						if(!saveInfo.isSnapshootDispatchChannelEnabled() && !saveInfo.isDistributionListDispatchChannelEnabled() && !saveInfo.isJavaClassDispatchChannelEnabled()) {
							boolean noValidDispatchTarget = false;
							if(saveInfo.isMailDispatchChannelEnabled()) {
								String[] recipients = findRecipients(saveInfo, biobj, emailDispatchDataStore);
								if (recipients != null && recipients.length > 0) {
									noValidDispatchTarget = false;
									logger.debug("Found at least one target of type mail");
								}else{
									noValidDispatchTarget = true;
								}
							} 
							
							if(saveInfo.isFunctionalityTreeDispatchChannelEnabled()) {
								List storeInFunctionalities = findFolders(saveInfo, biobj, folderDispatchDataSotre);
								if(storeInFunctionalities != null && !storeInFunctionalities.isEmpty()) {
									noValidDispatchTarget = false;
									logger.debug("Found at least one target of type folder");
								}else{
									noValidDispatchTarget = true;
								}
							}
							
							if(noValidDispatchTarget) {
								logger.debug("No valid dispatch target for document [" + (ind+1) + "] with label [" + documentInstanceName + "] and parameters [" + toBeAppendedToDescription +"]");
								logger.info("Document [" + (ind+1) + "] with label [" + documentInstanceName + "] and parameters " + toBeAppendedToDescription + " not executed: no valid dispatch target");
								continue;
							} else if(!saveInfo.isFunctionalityTreeDispatchChannelEnabled() && !saveInfo.isMailDispatchChannelEnabled()){
								logger.debug("There are no dispatch targets for document with label [" + documentInstanceName + "] - if not an ETL, WEKA or KPI document a dispatch target should be added");
							}else{
								logger.debug("There is at list one dispatch target for document with label [" + documentInstanceName + "]");
							}
						}

						executionProxy = new ExecutionProxy();
						executionProxy.setBiObject(biobj);
						
						
						
						
						logger.info("Executing document [" + (ind+1) + "] with label [" + documentInstanceName + "] and parameters " + toBeAppendedToDescription +" ...");
						long start = System.currentTimeMillis();
						byte[] response = executionProxy.exec(profile, "SCHEDULATION", null);
						if (response == null || response.length == 0) {
							logger.debug("Document executed without any response");
						}
						String retCT = executionProxy.getReturnedContentType();
						String fileextension = executionProxy.getFileExtensionFromContType(retCT);
						long end = System.currentTimeMillis();			
						long elapsed = (end - start)/1000;
						logger.info("Document [" + (ind+1) + "] with label [" + documentInstanceName + "] and parameters " + toBeAppendedToDescription +" executed in [" + elapsed + "]");
						
						
						if(saveInfo.isSnapshootDispatchChannelEnabled()) {
							saveAsSnap(saveInfo, biobj, response, toBeAppendedToName.toString(), toBeAppendedToDescription.toString(),profile);
						}

						if(saveInfo.isFunctionalityTreeDispatchChannelEnabled()) {
							saveAsDocument(saveInfo, biobj,jobExecutionContext, response, fileextension, folderDispatchDataSotre, toBeAppendedToName.toString(), toBeAppendedToDescription.toString());
						}

						if(saveInfo.isMailDispatchChannelEnabled()) {
							sendMail(saveInfo, biobj, tempParMap, response, retCT, fileextension, emailDispatchDataStore, toBeAppendedToName.toString(), toBeAppendedToDescription.toString());
						}
						if(saveInfo.isDistributionListDispatchChannelEnabled()) {
							sendToDl(saveInfo, biobj, response, retCT, fileextension, toBeAppendedToName.toString(), toBeAppendedToDescription.toString());
							if(jobExecutionContext.getNextFireTime()== null){
								String triggername = jobExecutionContext.getTrigger().getName();
								List dlIds = saveInfo.getDlIds();
								it = dlIds.iterator();
								while(it.hasNext()){
									Integer dlId = (Integer)it.next();
									DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(dlId);
									DAOFactory.getDistributionListDAO().eraseDistributionListObjects(dl, (biobj.getId()).intValue(), triggername);
								}
							}
						}

						if(saveInfo.isJavaClassDispatchChannelEnabled()) {
							sendToJavaClass(saveInfo, biobj, response);
						}


					} else {
						logger.warn("The document with label "+documentInstanceName+" cannot be executed directly, " +
						"maybe some prameters are not filled ");
						throw new Exception("The document with label "+documentInstanceName+" cannot be executed directly, " +
						"maybe some prameters are not filled ");
					}
				}
				
				String endExecMsg = "${scheduler.endexecsched} " + biobj.getName();
				eventManager.registerEvent("Scheduler", endExecMsg, "", roles);

			}

			
			long endSchedule = System.currentTimeMillis();
			long elapsedSchedule = (endSchedule-startSchedule)/1000;
			logger.info("Scheduled activity succesfully ended in [" + elapsedSchedule +"] sec.");
		} catch (Exception e) {
			logger.error("Error while executiong job ", e);
		} finally {
			logger.debug("OUT");
		}
	}




	private void retrieveParametersValues(BIObject biobj) throws Exception {
		logger.debug("IN");
		try {
			List parameters = biobj.getBiObjectParameters();
			if (parameters == null || parameters.isEmpty()) {
				logger.debug("Document has no parameters");
				return;
			}
			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				ParameterValuesRetriever retriever = parameter.getParameterValuesRetriever();
				if (retriever != null) {
					logger.debug("Document parameter with url name [" + parameter.getParameterUrlName() + "] has a parameter values retriever: " + retriever);
					logger.debug("Retrieving values...");
					List<String> values = null;
					try {
						values = retriever.retrieveValues(parameter);
					} catch (Exception e) {
						logger.error("Error while retrieving values for parameter with url name [" + parameter.getParameterUrlName() + "] of document [" + biobj.getLabel() + "].", e);
						throw e;
					}
					logger.debug("Values retrieved.");
					parameter.setParameterValues(values);
					parameter.setTransientParmeters(true);
				}
			}
		} finally {
			logger.debug("OUT");
		}
	}




	private void setLoadAtRuntimeParameters(BIObject biobj, String loadAtRuntimeParametersString) {
		logger.debug("IN");
		try {
			List parameters = biobj.getBiObjectParameters();
			if (parameters == null || parameters.isEmpty()) {
				logger.debug("Document has no parameters");
				return;
			}
			if (loadAtRuntimeParametersString == null || loadAtRuntimeParametersString.trim().trim().equals("")) {
				logger.debug("No load-at-runtime parameters found");
				return;
			}
			String[] loadAtRuntimeParameters = loadAtRuntimeParametersString.split(";");

			Map<String, String> loadAtRuntimeParametersMap = new HashMap<String, String>();
			for (int count = 0; count < loadAtRuntimeParameters.length; count++) {
				String loadAtRuntime = loadAtRuntimeParameters[count];
				int parameterUrlNameIndex = loadAtRuntime.lastIndexOf("(");
				String parameterUrlName = loadAtRuntime.substring(0, parameterUrlNameIndex);
				String userAndRole = loadAtRuntime.substring(parameterUrlNameIndex + 1, loadAtRuntime.length() - 1);
				loadAtRuntimeParametersMap.put(parameterUrlName, userAndRole);
			}

			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				if (loadAtRuntimeParametersMap.containsKey(parameter.getParameterUrlName())) {
					logger.debug("Document parameter with url name [" + parameter.getParameterUrlName() + "] was configured to be calculated at runtime.");
					RuntimeLoadingParameterValuesRetriever strategy = new RuntimeLoadingParameterValuesRetriever();
					String userRoleStr = loadAtRuntimeParametersMap.get(parameter.getParameterUrlName());
					String[] userRole = userRoleStr.split("\\|");
					strategy.setUserIndentifierToBeUsed(userRole[0]);
					strategy.setRoleToBeUsed(userRole[1]);
					parameter.setParameterValuesRetriever(strategy);
				}
			}
		} finally {
			logger.debug("OUT");
		}
	}




	private void setIterativeParameters(BIObject biobj, String iterativeParametersString) {
		logger.debug("IN");
		try {
			List parameters = biobj.getBiObjectParameters();
			if (parameters == null || parameters.isEmpty()) {
				logger.debug("Document has no parameters");
				return;
			}
			if (iterativeParametersString == null || iterativeParametersString.trim().trim().equals("")) {
				logger.debug("No iterative parameters found");
				return;
			}
			String[] iterativeParameters = iterativeParametersString.split(";");
			List iterativeParametersList = Arrays.asList(iterativeParameters);
			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				parameter.setIterative(false);
				if (iterativeParametersList.contains(parameter.getParameterUrlName())) {
					logger.debug("Document parameter with url name [" + parameter.getParameterUrlName() + "] was configured to be iterative.");
					parameter.setIterative(true);
				}
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private void setUseFormulaParameters(BIObject biobj, String useFormulaParametersString) {
		logger.debug("IN");
		try {
			List parameters = biobj.getBiObjectParameters();
			if (parameters == null || parameters.isEmpty()) {
				logger.debug("Document has no parameters");
				return;
			}
			if (useFormulaParametersString == null || useFormulaParametersString.trim().trim().equals("")) {
				logger.debug("No parameters using formula found");
				return;
			}

			String[] useFormulaParameters = useFormulaParametersString.split(";");

			Map<String, String> useFormulaParametersMap = new HashMap<String, String>();
			for (int count = 0; count < useFormulaParameters.length; count++) {
				String useFormula = useFormulaParameters[count];
				int parameterUrlNameIndex = useFormula.lastIndexOf("(");
				String parameterUrlName = useFormula.substring(0, parameterUrlNameIndex);
				String userAndRole = useFormula.substring(parameterUrlNameIndex + 1, useFormula.length() - 1);
				useFormulaParametersMap.put(parameterUrlName, userAndRole);
			}

			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				if (useFormulaParametersMap.containsKey(parameter.getParameterUrlName())) {
					logger.debug("Document parameter with url name [" + parameter.getParameterUrlName() + "] was configured to use a formula.");
					FormulaParameterValuesRetriever strategy = new FormulaParameterValuesRetriever();
					String fName = useFormulaParametersMap.get(parameter.getParameterUrlName());
					Formula f = Formula.getFormula(fName);
					strategy.setFormula(f);
					parameter.setParameterValuesRetriever(strategy);
				}
			}
		} finally {
			logger.debug("OUT");
		}
	}


	private void sendToJavaClass(DispatchContext sInfo,BIObject biobj, byte[] response) throws Exception {
		logger.debug("IN");

		String javaClass = sInfo.getJavaClassPath();
		if( (javaClass==null) || javaClass.trim().equals("")) {
			logger.error("Classe java nons specificata");
			return;
		}
		// try to get new Instance
		JavaClassDestination jcDest=null;
		try{
			jcDest=(JavaClassDestination)Class.forName(javaClass).newInstance();
		}
		catch (ClassCastException e) {
			logger.error("Class "+javaClass+" does not extend JavaClassDestination class as expected");
			return;
		}
		catch (Exception e) {
			logger.error("Error while instantiating the class "+javaClass);
			return;
			}

		logger.debug("Sucessfull instantiation of "+javaClass);

		jcDest.setBiObj(biobj);
		jcDest.setDocumentByte(response);

		try{
			jcDest.execute();
		}
		catch (Exception e) {
			logger.error("Error during execution",e);
			return;
		}


		logger.debug("OUT");


	}


	private void saveAsSnap(DispatchContext sInfo,BIObject biobj, byte[] response, String toBeAppendedToName, String toBeAppendedToDescription,IEngUserProfile profile) {
		logger.debug("IN");
		try {
			String snapName = sInfo.getSnapshotName();
			if( (snapName==null) || snapName.trim().equals("")) {
				throw new Exception("Document name not specified");
			}
			//snapName += toBeAppendedToName;
			if (snapName.length() > 100) {
				logger.warn("Snapshot name [" + snapName + "] exceeds maximum length that is 100, it will be truncated");
				snapName = snapName.substring(0, 100);
			}

			String snapDesc = sInfo.getSnapshotDescription() != null ? sInfo.getSnapshotDescription() : "";
			snapDesc += toBeAppendedToDescription;
			if (snapDesc.length() > 1000) {
				logger.warn("Snapshot description [" + snapDesc + "] exceeds maximum length that is 1000, it will be truncated");
				snapDesc = snapDesc.substring(0, 1000);
			}

			String historylengthStr = sInfo.getSnapshotHistoryLength();
			// store document as snapshot
			ISnapshotDAO snapDao = DAOFactory.getSnapshotDAO();
			snapDao.setUserProfile(profile);
			// get the list of snapshots
			List allsnapshots = snapDao.getSnapshots(biobj.getId());
			// get the list of the snapshot with the store name
			List snapshots = SchedulerUtilities.getSnapshotsByName(allsnapshots, snapName);
			// get the number of previous snapshot saved
			int numSnap = snapshots.size();
			// if the number of snapshot is greater or equal to the history length then
			// delete the unecessary snapshots
			if((historylengthStr!=null) && !historylengthStr.trim().equals("")){
				try{
					Integer histLenInt = new Integer(historylengthStr);
					int histLen = histLenInt.intValue();
					if(numSnap>=histLen){
						int delta = numSnap - histLen;
						for(int i=0; i<=delta; i++) {
							Snapshot snap = SchedulerUtilities.getNamedHistorySnapshot(allsnapshots, snapName, histLen-1);
							Integer snapId = snap.getId();
							snapDao.deleteSnapshot(snapId);
						}
					}
				} catch(Exception e) {
					logger.error("Error while deleting object snapshots", e);
				}
			}
			snapDao.saveSnapshot(response, biobj.getId(), snapName, snapDesc, null);	
		} catch (Exception e) {
			logger.error("Error while saving schedule result as new snapshot", e);
		}finally{
			logger.debug("OUT");
		}
	}





	private void saveAsDocument(DispatchContext sInfo,BIObject biobj, JobExecutionContext jex, byte[] response, String fileExt, IDataStore dataStore, String toBeAppendedToName, String toBeAppendedToDescription) {
		logger.debug("IN");
		try{
			String docName = sInfo.getDocumentName();
			if( (docName==null) || docName.trim().equals("")) {
				throw new Exception(" Document name not specified");
			}
			docName += toBeAppendedToName;
			String docDesc = sInfo.getDocumentDescription() + toBeAppendedToDescription;

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
			objTemp.setContent(response);
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
			List storeInFunctionalities = findFolders(sInfo, biobj, dataStore);
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
			newbiobj.setDataSourceId(biobj.getDataSourceId());
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
		}finally{
			logger.debug("OUT");
		}
	}




	private void sendMail(DispatchContext sInfo, BIObject biobj,Map parMap, byte[] response, String retCT, String fileExt, IDataStore dataStore, String toBeAppendedToName, String toBeAppendedToDescription) {
		logger.debug("IN");
		try{

			String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtphost");
		    String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtpport");
		    int smptPort=25;
		    
			if( (smtphost==null) || smtphost.trim().equals(""))
				throw new Exception("Smtp host not configured");
			if( (smtpport==null) || smtpport.trim().equals("")){
				throw new Exception("Smtp host not configured");
			}else{
				smptPort=Integer.parseInt(smtpport);
			}
				
		    
			String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.from");
			if( (from==null) || from.trim().equals(""))
				from = "spagobi.scheduler@eng.it";
			String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.user");
			if( (user==null) || user.trim().equals("")){
				logger.debug("Smtp user not configured");	
				user=null;
			}
			//	throw new Exception("Smtp user not configured");
			String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.password");
			if( (pass==null) || pass.trim().equals("")){
			logger.debug("Smtp password not configured");	
			}
			//	throw new Exception("Smtp password not configured");
			
			String mailSubj = sInfo.getMailSubj();
			mailSubj = StringUtilities.substituteParametersInString(mailSubj, parMap, null, false);

			String mailTxt = sInfo.getMailTxt();

			String[] recipients = findRecipients(sInfo, biobj, dataStore);
			if (recipients == null || recipients.length == 0) {
				logger.error("No recipients found for email sending!!!");
				return;
			}

			//Set the host smtp address
			Properties props = new Properties();
			props.put("mail.smtp.host", smtphost);
			props.put("mail.smtp.port", smptPort);
			
			// open session
			Session session=null;
			
			// create autheticator object
			Authenticator auth = null;
			if (user!=null) {
				auth = new SMTPAuthenticator(user, pass);
				props.put("mail.smtp.auth", "true");
				session = Session.getDefaultInstance(props, auth);
				logger.error("Session.getDefaultInstance(props, auth)");
			}else{
				session = Session.getDefaultInstance(props);
				logger.error("Session.getDefaultInstance(props)");
			}
			
			// create a message
			Message msg = new MimeMessage(session);
			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++)  {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			// Setting the Subject and Content Type
			String subject = mailSubj + " " + biobj.getName() + toBeAppendedToName;
			msg.setSubject(subject);
			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(mailTxt + "\n" + toBeAppendedToDescription);
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message

			SchedulerDataSource sds = new SchedulerDataSource(response, retCT, biobj.getName() + toBeAppendedToName + fileExt);
			mbp2.setDataHandler(new DataHandler(sds));
			mbp2.setFileName(sds.getName());
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);
			// send message
			Transport.send(msg);
		} catch (Exception e) {
			logger.error("Error while sending schedule result mail",e);
		}finally{
			logger.debug("OUT");
		}
	}

	private String[] findRecipients(DispatchContext info, BIObject biobj,
			IDataStore dataStore) {
		logger.debug("IN");
		String[] toReturn = null;
		List<String> recipients = new ArrayList();
		try {
			recipients.addAll(findRecipientsFromFixedList(info));
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			recipients.addAll(findRecipientsFromDataSet(info, biobj, dataStore));
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			recipients.addAll(findRecipientsFromExpression(info, biobj));
		} catch (Exception e) {
			logger.error(e);
		}
		// validates addresses
		List<String> validRecipients = new ArrayList();
		Iterator it = recipients.iterator();
		while (it.hasNext()) {
			String recipient = (String) it.next();
			if (GenericValidator.isBlankOrNull(recipient) || !GenericValidator.isEmail(recipient)) {
				logger.error("[" + recipient + "] is not a valid email address.");
				continue;
			}
			if (validRecipients.contains(recipient))
				continue;
			validRecipients.add(recipient);
		}
		toReturn = validRecipients.toArray(new String[0]);
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private List<String> findRecipientsFromFixedList(DispatchContext info) throws Exception {
		logger.debug("IN");
		List<String> recipients = new ArrayList();
		if (info.isUseFixedRecipients()) {
			logger.debug("Trigger is configured to send mail to fixed recipients: " + info.getMailTos());
			if (info.getMailTos() == null || info.getMailTos().trim().equals("")) {
				throw new Exception("Missing fixed recipients list!!!");
			}
			// in this case recipients are fixed and separated by ","
			String[] fixedRecipients = info.getMailTos().split(",");
			logger.debug("Fixed recipients found: " + fixedRecipients);
			recipients.addAll(Arrays.asList(fixedRecipients));
		}
		logger.debug("OUT");
		return recipients;
	}

	private List<String> findRecipientsFromExpression(DispatchContext info, BIObject biobj) throws Exception {
		logger.debug("IN");
		List<String> recipients = new ArrayList();
		if (info.isUseExpression()) {
			logger.debug("Trigger is configured to send mail using an expression: " + info.getExpression());
			String expression = info.getExpression();
			if (expression == null || expression.trim().equals("")) {
				throw new Exception("Missing recipients expression!!!");
			}
			// building a map for parameters value substitution
			Map parametersMap = new HashMap();
			List parameters = biobj.getBiObjectParameters();
			Iterator it = parameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter parameter = (BIObjectParameter) it.next();
				List values = parameter.getParameterValues();
				if (values != null && !values.isEmpty()) {
					parametersMap.put(parameter.getLabel(), values.get(0));
				} else {
					parametersMap.put(parameter.getLabel(), "");
				}
			}
			// we must substitute parameter values on the expression
			String recipientStr = StringUtilities.substituteParametersInString(expression, parametersMap,null, false);
			logger.debug("The expression, after substitution, now is [" + recipientStr + "].");
			String[] recipientsArray = recipientStr.split(",");
			logger.debug("Recipients found with expression: " + recipientsArray);
			recipients.addAll(Arrays.asList(recipientsArray));
		}
		logger.debug("OUT");
		return recipients;
	}

	private List<String> findRecipientsFromDataSet(DispatchContext info, BIObject biobj,
			IDataStore dataStore) throws Exception {
		logger.debug("IN");
		List<String> recipients = new ArrayList();
		if (info.isUseDataSet()) {
			logger.debug("Trigger is configured to send mail to recipients retrieved by a dataset");
			if (dataStore == null || dataStore.isEmpty()) {
				throw new Exception("The dataset in input is empty!! Cannot retrieve recipients from it.");
			}
			// in this case recipients must be retrieved by the dataset (which the datastore in input belongs to)
			// we must find the parameter value in order to filter the dataset
			String dsParameterLabel = info.getDataSetParameterLabel();
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
				String recipient = null;
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
					IField recipientField = (IField) record.getFieldAt(1);
					Object recipientFieldObj = recipientField.getValue();
					if (recipientFieldObj != null) {
						recipient = recipientFieldObj.toString();
						logger.debug("Found recipient [" + recipient + "] on the second field of the record.");
					} else {
						logger.warn("The second field of the record is null.");
					}
				}
				if (recipient != null) {
					recipients.add(recipient);
				}
			}
			logger.debug("Recipients found from dataset: " + recipients.toArray());
		}
		logger.debug("OUT");
		return recipients;
	}

	private List findFolders(DispatchContext info, BIObject biobj, IDataStore dataStore) {
		logger.debug("IN");
		List toReturn = null;
		List<String> folders = new ArrayList();
		try {
			folders.addAll(findFoldersFromFixedList(info));
		} catch (Exception e) {
			logger.error(e);
		}
		try {
			folders.addAll(findFoldersFromDataSet(info, biobj, dataStore));
		} catch (NullPointerException en) {
			logger.error("Folders defined into dataset " + info.getDataSetFolderLabel()+ "  not found.");
		} catch (Exception e) {
			logger.error(e);
		}
		
		toReturn = folders;
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	private List findFoldersFromFixedList(DispatchContext info) throws Exception {
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

	private List findFoldersFromDataSet(DispatchContext info, BIObject biobj,IDataStore dataStore) throws Exception {
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
	
	private void sendToDl(DispatchContext sInfo, BIObject biobj, byte[] response, String retCT, String fileExt, String toBeAppendedToName, String toBeAppendedToDescription) {
		logger.debug("IN");
		try{

			String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtphost");
			if( (smtphost==null) || smtphost.trim().equals(""))
				throw new Exception("Smtp host not configured");
			String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.from");
			if( (from==null) || from.trim().equals(""))
				from = "spagobi.scheduler@eng.it";
			String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.user");
			if( (user==null) || user.trim().equals(""))
				throw new Exception("Smtp user not configured");
			String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.password");
			if( (pass==null) || pass.trim().equals(""))
				throw new Exception("Smtp password not configured");

			String mailTos = "";
			List dlIds = sInfo.getDlIds();
			Iterator it = dlIds.iterator();
			while(it.hasNext()){

				Integer dlId = (Integer)it.next();
				DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(dlId);

				List emails = new ArrayList();
				emails = dl.getEmails();
				Iterator j = emails.iterator();
				while(j.hasNext()){
					Email e = (Email) j.next();
					String email = e.getEmail();
					String userTemp = e.getUserId();
					IEngUserProfile userProfile = GeneralUtilities.createNewUserProfile(userTemp);				
					if(ObjectsAccessVerifier.canSee(biobj, userProfile))	{				
						if (j.hasNext()) {mailTos = mailTos+email+",";}
						else {mailTos = mailTos+email;}
					}

				}
			}


			if( (mailTos==null) || mailTos.trim().equals("")) {	
				throw new Exception("No recipient address found");
			}

			String[] recipients = mailTos.split(",");
			//Set the host smtp address
			Properties props = new Properties();
			props.put("mail.smtp.host", smtphost);
			props.put("mail.smtp.auth", "true");
			// create autheticator object
			Authenticator auth = new SMTPAuthenticator(user, pass);
			// open session
			Session session = Session.getDefaultInstance(props, auth);
			// create a message
			Message msg = new MimeMessage(session);
			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++)  {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			// Setting the Subject and Content Type
			IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
			String subject = biobj.getName() + toBeAppendedToName;
			msg.setSubject(subject);
			// create and fill the first message part
			//MimeBodyPart mbp1 = new MimeBodyPart();
			//mbp1.setText(mailTxt);
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message
			SchedulerDataSource sds = new SchedulerDataSource(response, retCT, biobj.getName() + toBeAppendedToName + fileExt);
			mbp2.setDataHandler(new DataHandler(sds));
			mbp2.setFileName(sds.getName());
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			//mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);
			// send message
			Transport.send(msg);
		} catch (Exception e) {
			logger.error("Error while sending schedule result mail",e);
		}finally{
			logger.debug("OUT");
		}
	}


	private class SMTPAuthenticator extends javax.mail.Authenticator
	{
		private String username = "";
		private String password = "";

		public PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(username, password);
		}

		public SMTPAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
	}


	private class SchedulerDataSource implements DataSource {

		byte[] content = null;
		String name = null;
		String contentType = null;

		public String getContentType() {
			return contentType;
		}

		public InputStream getInputStream() throws IOException {
			ByteArrayInputStream bais = new ByteArrayInputStream(content);
			return bais;
		}

		public String getName() {
			return name;
		}

		public OutputStream getOutputStream() throws IOException {
			return null;
		}

		public SchedulerDataSource(byte[] content, String contentType, String name) {
			this.content = content;
			this.contentType = contentType;
			this.name = name;
		}
	}

}
