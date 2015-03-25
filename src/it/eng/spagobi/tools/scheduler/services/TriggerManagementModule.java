/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.to.TriggerInfo;
import it.eng.spagobi.tools.scheduler.utils.JavaClassDestination;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


public class TriggerManagementModule extends AbstractHttpModule {
	
	private RequestContainer requestContainer = null;
	private SessionContainer sessionContainer = null;
	private EMFErrorHandler errorHandler=null; 
	private IEngUserProfile profile = null;
	
	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger(TriggerManagementModule.class);
	
	
	public void init(SourceBean config) {	
		
	}
	
	public void service(SourceBean request, SourceBean response) throws Exception { 
		
		String message;
		
		requestContainer = getRequestContainer();
		sessionContainer = requestContainer.getSessionContainer();
		errorHandler = getErrorHandler();
		profile = (IEngUserProfile) sessionContainer.getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		message = null;
		try {
			
			message = (String) request.getAttribute("MESSAGEDET");
			logger.debug("Invoked operation [" + message + "] of trigger management service");
			if(message == null) {
				throw new EMFUserError(EMFErrorSeverity.ERROR, 101);
			}
			
			if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_GET_JOB_SCHEDULES) ||
			   message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_ORDER_LIST)) {
				getTriggersForJob(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_NEW_SCHEDULE)) {
				newScheduleForJob(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_SAVE_SCHEDULE)) {
				saveScheduleForJob(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_DELETE_SCHEDULE)) {
				deleteSchedule(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_GET_SCHEDULE_DETAIL)) {
				getSchedule(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_RUN_SCHEDULE)) {
				runSchedule(request, response);
			} 
			
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			logger.error("Error while executing trigger management service", ex);
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
		logger.debug("end of trigger management service =" +message);
	}
	
	
	
	private void runSchedule(SourceBean request, SourceBean response) throws EMFUserError {
		HashMap<String, String> logParam = new HashMap();
		try {
			RequestContainer reqCont = getRequestContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			SessionContainer permSess = sessCont.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		
		    ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String jobName = (String)request.getAttribute("jobName");
			String jobGroupName = (String)request.getAttribute("jobGroupName");
			getSchedule(request, response);
			TriggerInfo triggerInfo = (TriggerInfo)sessCont.getAttribute(SpagoBIConstants.TRIGGER_INFO);
			StringBuffer message = createMessageSaveSchedulation(triggerInfo, true, profile);
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP NAME", jobGroupName);
			// call the web service to create the schedule
			String resp = schedulerService.scheduleJob(message.toString());
			SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(resp);
			if(schedModRespSB!=null) {
				String outcome = (String)schedModRespSB.getAttribute("outcome");
				if(outcome.equalsIgnoreCase("fault")){
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.RUN",logParam , "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Immediate Trigger not created by the web service");
				}
			}
			// fill spago response
			response.updAttribute(SpagoBIConstants.PUBLISHER_NAME, "ReturnToTriggerList");
			response.setAttribute(SpagoBIConstants.JOB_GROUP_NAME, jobGroupName);
			response.setAttribute(SpagoBIConstants.JOB_NAME, jobName);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.RUN",logParam , "OK");
			} catch (Exception e) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.RUN",logParam , "KO");
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.RUN",logParam , "KO");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			logger.error("Error while create immediate trigger ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}
	 
	 
	private void deleteSchedule(SourceBean request, SourceBean response) throws EMFUserError {
		HashMap<String, String> logParam = new HashMap();
		String jobName = (String)request.getAttribute("jobName");
		String jobGroupName = (String)request.getAttribute("jobGroupName");
		String triggerName = (String) request.getAttribute("triggerName");
		String triggerGroup = (String) request.getAttribute("triggerGroup");
		logParam.put("JOB NAME", jobName);
		logParam.put("JOB GROUP", jobGroupName);
		logParam.put("TRIGGER NAME", triggerName);
		logParam.put("TRIGGER GROUP", triggerGroup);
		try {
				DAOFactory.getDistributionListDAO().eraseAllRelatedDistributionListObjects(triggerName);
		        ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String resp = schedulerService.deleteSchedulation(triggerName, triggerGroup);
			SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(resp);
			if(schedModRespSB!=null) {
				String outcome = (String)schedModRespSB.getAttribute("outcome");
				if(outcome.equalsIgnoreCase("fault")){
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.DELETE",logParam , "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Trigger not deleted by the service");
				}
			}
			// fill spago response
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ReturnToTriggerList");
			response.setAttribute(SpagoBIConstants.JOB_GROUP_NAME, jobGroupName);
			response.setAttribute(SpagoBIConstants.JOB_NAME, jobName);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.DELETE",logParam , "OK");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.DELETE",logParam , "KO");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			logger.error("Error while deleting schedule (trigger) ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	private void getSchedule(SourceBean request, SourceBean response) throws EMFUserError {
		try {
		    ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String jobName = (String)request.getAttribute("jobName");
			String jobGroupName = (String)request.getAttribute("jobGroupName");
			String triggerName = (String) request.getAttribute("triggerName");
			String triggerGroup = (String) request.getAttribute("triggerGroup");
			String respStr_gt = schedulerService.getJobSchedulationDefinition(triggerName, triggerGroup);
	        SourceBean triggerDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr_gt);			
			String respStr_gj = schedulerService.getJobDefinition(jobName, jobGroupName);
            SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr_gj);						
			if(triggerDetailSB!=null) {
				if(jobDetailSB!=null){
					TriggerInfo tInfo = SchedulerUtilities.getTriggerInfoFromTriggerSourceBean(triggerDetailSB, jobDetailSB);
					sessionContainer.setAttribute(SpagoBIConstants.TRIGGER_INFO, tInfo);
				} else {
					throw new Exception("Detail not recovered for job " + jobName + 
							            "associated to trigger " + triggerName);
				}
			} else {
				throw new Exception("Detail not recovered for trigger " + triggerName);
			}
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(false);
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			List allDatasets = DAOFactory.getDataSetDAO().loadDataSets();
			response.setAttribute(SpagoBIConstants.DATASETS_LIST, allDatasets);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "TriggerDetail");
		} catch (Exception ex) {
			logger.error("Error while getting detail of the schedule(trigger)", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	} 
	
	
	// ==========================================================================================================
	// METHOD Save schedule
	// ==========================================================================================================
	
	private void saveScheduleForJob(SourceBean request, SourceBean response) throws EMFUserError {
		HashMap<String, String> logParam = new HashMap();
		try{
		
			TriggerInfo triggerInfo = getTriggerInfoFromRequest(request) ;		
			logParam.put("TRIGGER NAME", triggerInfo.getTriggerName());
			logParam.put("JOB GROUP", triggerInfo.getJobInfo().getJobGroupName());
			logParam.put("JOB NAME", triggerInfo.getJobInfo().getJobName());
			// check for input validation errors 
			if(!this.getErrorHandler().isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
				List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(false);
				response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
				List allDatasets = DAOFactory.getDataSetDAO().loadDataSets();
				response.setAttribute(SpagoBIConstants.DATASETS_LIST, allDatasets);
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "TriggerDetail");
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.SAVE",logParam , "OK");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			
			//SessionContainer permanentContainer = sessionContainer.getPermanentContainer();
			//IEngUserProfile profile = (IEngUserProfile)permanentContainer.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		    StringBuffer message = createMessageSaveSchedulation(triggerInfo, false, profile);
			
		    // call the web service to create the schedule
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String servoutStr = schedulerService.scheduleJob(message.toString());
			SourceBean execOutSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
			if(execOutSB!=null) {
				String outcome = (String)execOutSB.getAttribute("outcome");
				if(outcome.equalsIgnoreCase("fault")){
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.SAVE",logParam , "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Trigger "+ triggerInfo.getTriggerName() +" not created by the web service");
				}
			}
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ReturnToTriggerList");
			response.setAttribute(SpagoBIConstants.JOB_GROUP_NAME, triggerInfo.getJobInfo().getJobGroupName());
			response.setAttribute(SpagoBIConstants.JOB_NAME, triggerInfo.getJobInfo().getJobName());
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.SAVE",logParam , "OK");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHED_TRIGGER.SAVE",logParam , "KO");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.error("Error while saving schedule for job", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}
	
	private TriggerInfo getTriggerInfoFromRequest(SourceBean request) throws EMFUserError {
		TriggerInfo triggerInfo = (TriggerInfo)sessionContainer.getAttribute(SpagoBIConstants.TRIGGER_INFO);
	
		String triggername = (String)request.getAttribute("triggername");	
		triggerInfo.setTriggerName(triggername);
		
		String triggerDescription  = (String)request.getAttribute("triggerdescription");	
		triggerInfo.setTriggerDescription(triggerDescription);
		
		String startdate  = (String)request.getAttribute("startdate");	
		triggerInfo.setStartDate(startdate);
		
		String starttime = (String)request.getAttribute("starttime");	
		triggerInfo.setStartTime(starttime);
		
		String chronstr = (String)request.getAttribute("chronstring");
		triggerInfo.setChronString(chronstr);
		
		String enddate = (String)request.getAttribute("enddate");	
		triggerInfo.setEndDate(enddate);
		
		String endtime = (String)request.getAttribute("endtime");
		triggerInfo.setEndTime(endtime);
		
		String repeatinterval = (String)request.getAttribute("repeatInterval");
		triggerInfo.setRepeatInterval(repeatinterval);
		
		Map<String, DispatchContext> saveOptions = getSaveOptionsFromRequest(request);
		triggerInfo.setSaveOptions(saveOptions);
		
		return triggerInfo;
	}
	
	private Map<String, DispatchContext> getSaveOptionsFromRequest(SourceBean request) throws EMFUserError {
		TriggerInfo triggerInfo = (TriggerInfo)sessionContainer.getAttribute(SpagoBIConstants.TRIGGER_INFO);
		JobInfo jobInfo = triggerInfo.getJobInfo();
		List<Integer> biobjIds = jobInfo.getDocumentIds();
		int index = 0;
		Map<String, DispatchContext> saveOptions = new HashMap<String, DispatchContext>();
		for(Integer biobId : biobjIds){
			index ++;
			DispatchContext dispatchContext = new DispatchContext();
			
			getSaveAsSnapshotOptions(request, dispatchContext, biobId, index);
			getSaveAsFileOptions(request, dispatchContext, biobId, index);
			getSaveAsJavaClassOptions(request, dispatchContext, biobId, index);
			getSaveAsDocumentOptions(request, dispatchContext, biobId, index);
			getSaveAsMailOptions(request, dispatchContext, biobId, index);
			getSaveAsDistributionListOptions(request, dispatchContext, biobId, index);

			saveOptions.put(biobId+"__"+index, dispatchContext);
		}
		return saveOptions;
	}
	
	private void getSaveAsSnapshotOptions(SourceBean request, DispatchContext dispatchContext, int biobId, int index) {
		String saveassnap = (String)request.getAttribute("saveassnapshot_" + biobId + "__" + index);	
		if(saveassnap != null) {
			dispatchContext.setSnapshootDispatchChannelEnabled(true);
			
			String snapshotName = (String)request.getAttribute("snapshotname_" +biobId + "__" + index);	
			dispatchContext.setSnapshotName(snapshotName);
			
			String snapshotDescription = (String)request.getAttribute("snapshotdescription_" + biobId + "__" + index);
			dispatchContext.setSnapshotDescription(snapshotDescription);
			
			String snapshotHistoryLength = (String)request.getAttribute("snapshothistorylength_" + biobId + "__" + index);
			dispatchContext.setSnapshotHistoryLength(snapshotHistoryLength);
		}  
	}
	
	private void getSaveAsFileOptions(SourceBean request, DispatchContext dispatchContext, int biobId, int index) {
		String saveasfile = (String)request.getAttribute("saveasfile_" + biobId + "__" + index);	
		if(saveasfile != null) {
			dispatchContext.setFileSystemDisptachChannelEnabled(true);
			dispatchContext.setProcessMonitoringEnabled(false);
			dispatchContext.setDestinationFolderRelativeToResourceFolder(true);
			
			String destinationFolder = (String)request.getAttribute("destinationfolder_" +biobId + "__" + index);	
			dispatchContext.setDestinationFolder(destinationFolder);			

			boolean zipFileDocument = "true".equalsIgnoreCase((String) request.getAttribute("zipFileDocument_"+biobId+"__"+index));
			dispatchContext.setZipFileDocument(zipFileDocument);
			
			String fileName = (String)request.getAttribute("fileName_"+biobId+"__"+index);	
			if(fileName != null && !fileName.equals("")){
				dispatchContext.setFileName(fileName);
			}
			// set Zip File Name if chosen
			String zipFileName = (String)request.getAttribute("zipFileName_"+biobId+"__"+index);	
			if(zipFileName != null && !zipFileName.equals("")){
				dispatchContext.setZipFileName(zipFileName);
			}

		
		}
	}
	
	private void getSaveAsJavaClassOptions(SourceBean request, DispatchContext dispatchContext, int biobId, int index) {
		String sendToJavaClass = (String)request.getAttribute("sendtojavaclass_"+biobId+"__"+index);	
		if(sendToJavaClass!=null) {
			dispatchContext.setJavaClassDispatchChannelEnabled(true);
			String javaClassPath = (String)request.getAttribute("javaclasspath_"+biobId+"__"+index);	
			JavaClassDestination tryClass=null;
			try{
			tryClass=(JavaClassDestination)Class.forName(javaClassPath).newInstance();
			}
			catch (ClassCastException e) {
				logger.error("Error in istantiating class");
				EMFValidationError emfError=new EMFValidationError(EMFErrorSeverity.ERROR, "sendtojavaclass_"+biobId+"__"+index, "12200");
				errorHandler.addError(emfError);
			
			}				
			catch (Exception e) {
				logger.error("Error in istantiating class");
				EMFValidationError emfError=new EMFValidationError(EMFErrorSeverity.ERROR, "sendtojavaclass_"+biobId+"__"+index, "12100");
				errorHandler.addError(emfError);
			}					
			dispatchContext.setJavaClassPath(javaClassPath);
		}  
	}
	
	private void getSaveAsDocumentOptions(SourceBean request, DispatchContext dispatchContext, int biobId, int index) throws EMFUserError {
		String saveasdoc = (String)request.getAttribute("saveasdocument_"+biobId+"__"+index);	
		if(saveasdoc!=null) {
			dispatchContext.setFunctionalityTreeDispatchChannelEnabled(true);
			String docname = (String)request.getAttribute("documentname_"+biobId+"__"+index);	
			dispatchContext.setDocumentName(docname);
			String docdescr = (String)request.getAttribute("documentdescription_"+biobId+"__"+index);	
			dispatchContext.setDocumentDescription(docdescr);
			boolean useFixedFolder = "true".equalsIgnoreCase((String) request.getAttribute("useFixedFolder_"+biobId+"__"+index));
			dispatchContext.setUseFixedFolder(useFixedFolder);
			if (useFixedFolder) {
				String functIdsConcat = "";
				String tmpValReq = "tree_"+biobId+"__"+index+"_funct_id";
				List functIds = request.getAttributeAsList(tmpValReq);	
				Iterator iterFunctIds = functIds.iterator();
				while(iterFunctIds.hasNext()) {
					String idFunct = (String)iterFunctIds.next();
					functIdsConcat += idFunct;
					if(iterFunctIds.hasNext()){
						functIdsConcat += ",";
					}
				}
				dispatchContext.setFunctionalityIds(functIdsConcat);
			}
			//gestire acquisizione folder 
			boolean useFolderDataset = "true".equalsIgnoreCase((String) request.getAttribute("useFolderDataset_"+biobId+"__"+index));
			dispatchContext.setUseFolderDataSet(useFolderDataset);
			if (useFolderDataset) {
				String dsLabel = (String)request.getAttribute("datasetFolderLabel_"+biobId+"__"+index);	
				dispatchContext.setDataSetFolderLabel(dsLabel);
				String datasetParameterLabel = (String)request.getAttribute("datasetFolderParameter_"+biobId+"__"+index);	
				dispatchContext.setDataSetFolderParameterLabel(datasetParameterLabel);
				if (dsLabel == null || dsLabel.trim().equals("")) {
					BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					List params = new ArrayList();
					params.add(biobj.getName());
					this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSet", params, "component_scheduler_messages"));
				}
				if (datasetParameterLabel == null || datasetParameterLabel.trim().equals("")) {
					BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					List params = new ArrayList();
					params.add(biobj.getName());
					this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSetParameter", params, "component_scheduler_messages"));
				}
			//	sInfo.setFunctionalityIds(functIdsConcat);
			}
		}
	}
	
	private void getSaveAsMailOptions(SourceBean request, DispatchContext dispatchContext, int biobId, int index) throws EMFUserError {
		String sendmail = (String)request.getAttribute("sendmail_"+biobId+"__"+index);	
		if(sendmail!=null) {
			dispatchContext.setMailDispatchChannelEnabled(true);
			boolean useFixedRecipients = "true".equalsIgnoreCase((String) request.getAttribute("useFixedRecipients_"+biobId+"__"+index));
			dispatchContext.setUseFixedRecipients(useFixedRecipients);
			if (useFixedRecipients) {
				String mailtos = (String)request.getAttribute("mailtos_"+biobId+"__"+index);
				dispatchContext.setMailTos(mailtos);
				if (mailtos == null || mailtos.trim().equals("")) {
					BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					List params = new ArrayList();
					params.add(biobj.getName());
					this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingFixedRecipients", params, "component_scheduler_messages"));
				}
			}
			boolean useDataset = "true".equalsIgnoreCase((String) request.getAttribute("useDataset_"+biobId+"__"+index));
			dispatchContext.setUseDataSet(useDataset);
			if (useDataset) {
				String dsLabel = (String)request.getAttribute("datasetLabel_"+biobId+"__"+index);	
				dispatchContext.setDataSetLabel(dsLabel);
				String datasetParameterLabel = (String)request.getAttribute("datasetParameter_"+biobId+"__"+index);	
				dispatchContext.setDataSetParameterLabel(datasetParameterLabel);
				if (dsLabel == null || dsLabel.trim().equals("")) {
					BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					List params = new ArrayList();
					params.add(biobj.getName());
					this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSet", params, "component_scheduler_messages"));
				}
				if (datasetParameterLabel == null || datasetParameterLabel.trim().equals("")) {
					BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					List params = new ArrayList();
					params.add(biobj.getName());
					this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingDataSetParameter", params, "component_scheduler_messages"));
				}
			}
			boolean useExpression = "true".equalsIgnoreCase((String) request.getAttribute("useExpression_"+biobId+"__"+index));
			dispatchContext.setUseExpression(useExpression);
			if (useExpression) {
				String expression = (String)request.getAttribute("expression_"+biobId+"__"+index);	
				dispatchContext.setExpression(expression);
				if (expression == null || expression.trim().equals("")) {
					BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
					List params = new ArrayList();
					params.add(biobj.getName());
					this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingExpression", params, "component_scheduler_messages"));
				}
			}
			
			if (!useFixedRecipients && !useDataset && !useExpression) {
				BIObject biobj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobId);
				List params = new ArrayList();
				params.add(biobj.getName());
				this.getErrorHandler().addError(new EMFValidationError(EMFErrorSeverity.ERROR, null, "errors.trigger.missingRecipients", params, "component_scheduler_messages"));	
			}
			
			String mailsubj = (String)request.getAttribute("mailsubj_"+biobId+"__"+index);	
			dispatchContext.setMailSubj(mailsubj);
			String mailtxt = (String)request.getAttribute("mailtxt_"+biobId+"__"+index);	
			dispatchContext.setMailTxt(mailtxt);
			
			
			//Mail
			boolean zipMailDocument = "true".equalsIgnoreCase((String) request.getAttribute("zipMailDocument_"+biobId+"__"+index));
			dispatchContext.setZipMailDocument(zipMailDocument);
			
			boolean reportNameInSubject = "true".equalsIgnoreCase((String) request.getAttribute("reportNameInSubject_"+biobId+"__"+index));
			dispatchContext.setReportNameInSubject(reportNameInSubject);
			
			boolean uniqueMail = "true".equalsIgnoreCase((String) request.getAttribute("uniqueMail_"+biobId+"__"+index));
			dispatchContext.setUniqueMail(uniqueMail);
			
			// set File Name if chosen
			String containedFileName = (String)request.getAttribute("containedFileName_"+biobId+"__"+index);	
			if(containedFileName != null && !containedFileName.equals("")){
				dispatchContext.setContainedFileName(containedFileName);
			}
			
			// set Zip File Name if chosen
			String zipMailName = (String)request.getAttribute("zipMailName_"+biobId+"__"+index);	
			if(zipMailName != null && !zipMailName.equals("")){
				dispatchContext.setZipMailName(zipMailName);
			}
		}
	}
	
	private void getSaveAsDistributionListOptions(SourceBean request, DispatchContext dispatchContext, int biobId, int index) throws EMFUserError {
		String sendtodl = (String)request.getAttribute("saveasdl_"+biobId+"__"+index);	
		if(sendtodl!=null) {
			dispatchContext.setDistributionListDispatchChannelEnabled(true);
			dispatchContext.setBiobjId(biobId);
			List dlist = DAOFactory.getDistributionListDAO().loadAllDistributionLists();	
			Iterator it = dlist.iterator();
			while(it.hasNext()){
				DistributionList dl = (DistributionList)it.next();
				int dlId = dl.getId();
				String listID = (String)request.getAttribute("sendtodl_"+dlId+"_"+biobId+"__"+index);
				if(listID!=null){
					dispatchContext.addDlId(new Integer(listID));
				}
				else{
					String triggername = (String)request.getAttribute("triggername");	
					DAOFactory.getDistributionListDAO().eraseDistributionListObjects(dl,biobId,triggername);
				}
									
			}
			
		}
	}
	
	
	
	
	
	// ==========================================================================================================
	// METHOD New schedule
	// ==========================================================================================================
	private void newScheduleForJob(SourceBean request, SourceBean response) throws EMFUserError {
		String jobName;
		String jobGroupName;
		
		logger.debug("IN");
		
		jobName = null;
		jobGroupName = null;
		try{
		   
		    jobName = (String)request.getAttribute("jobName");
			jobGroupName = (String)request.getAttribute("jobGroupName");
			
			
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String jobDetail = schedulerService.getJobDefinition(jobName, jobGroupName);
            SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(jobDetail);			
			if(jobDetailSB==null) {
				throw new Exception("Cannot recover job " + jobName);
			}		
			
			JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobDetailSB);
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.setJobInfo(jobInfo);
			
			Map<String, DispatchContext> saveOptions = new HashMap<String, DispatchContext>();
			List<Integer> biobjids = jobInfo.getDocumentIds();
			int index = 0;
			for(Integer idobj : biobjids) {
				index ++;
				saveOptions.put(idobj+"__" + index, new DispatchContext());
			}
			triggerInfo.setSaveOptions(saveOptions);
			
			
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(false);
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			List allDatasets = DAOFactory.getDataSetDAO().loadDataSets();
			response.setAttribute(SpagoBIConstants.DATASETS_LIST, allDatasets);
			sessionContainer.setAttribute(SpagoBIConstants.TRIGGER_INFO, triggerInfo);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "TriggerDetail");
		} catch (Exception ex) {
			logger.error("Error while creating a new schedule for job " + jobName, ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}
	
	
	private void getTriggersForJob(SourceBean request, SourceBean response) throws EMFUserError {
		String jobName = "";
		try{
		    ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			// create the sourcebean of the list
			SourceBean pageListSB  = new SourceBean("PAGED_LIST");
			jobName = (String)request.getAttribute("jobName");
			String jobGroupName = (String)request.getAttribute("jobGroupName");
			String serviceResp = schedulerService.getJobSchedulationList(jobName, jobGroupName);
			SourceBean rowsSB = SourceBean.fromXMLString(serviceResp);
			if(rowsSB==null) {
				rowsSB = new SourceBean("ROWS");
			}
			// fill the list sourcebean			
			pageListSB.setAttribute(rowsSB);
			
			//ordering of list
			String typeOrder = (request.getAttribute("TYPE_ORDER")==null)?" ASC":(String)request.getAttribute("TYPE_ORDER");
			String fieldOrder = (request.getAttribute("FIELD_ORDER")==null)?" triggerDescription":(String)request.getAttribute("FIELD_ORDER");
			pageListSB = orderJobList(pageListSB, typeOrder, fieldOrder);

			// populate response with the right values
			response.setAttribute(pageListSB);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ListTriggers");	
		} catch (Exception ex) {
			logger.error("Error while recovering triggers of the job " + jobName, ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}
	
	private SourceBean orderJobList(SourceBean pageListSB, String typeOrder, String fieldOrder) throws EMFUserError {
		try {
			List tmpAllList = pageListSB.getAttributeAsList("ROWS.ROW");
			List tmpFieldList = new ArrayList();
			
			if (tmpAllList != null){
				for (int i=0; i < tmpAllList.size(); i++){
					SourceBean tmpSB = (SourceBean)tmpAllList.get(i);
					tmpFieldList.add(tmpSB.getAttribute(fieldOrder.trim()));
				}
			}
			Object[] orderList = tmpFieldList.toArray();
			Arrays.sort(orderList);
			//create a source bean with the list ordered
			SourceBean orderedPageListSB  = new SourceBean("PAGED_LIST");
			SourceBean rows = new SourceBean("ROWS");
			int i = 0;
			if (typeOrder.trim().equals("DESC"))				 
					i = tmpFieldList.size()-1;
			
			while (tmpFieldList != null && tmpFieldList.size() > 0){	
					SourceBean newSB = (SourceBean)tmpAllList.get(tmpFieldList.indexOf(orderList[i]));					
					rows.setAttribute(newSB);
					//remove elements from temporary lists
					tmpAllList.remove(tmpFieldList.indexOf(orderList[i]));
					tmpFieldList.remove(tmpFieldList.indexOf(orderList[i]));
					if (typeOrder.trim().equals("DESC"))
						i--;
					else
						i++;
			}
			orderedPageListSB.setAttribute(rows);
			return orderedPageListSB;
		} catch (Exception ex) {
			logger.error("Error while recovering all job definition", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1000", "component_scheduler_messages");
		}
	}

	// ==========================================================================================================
	// SERIALIZER
	// ==========================================================================================================
	private StringBuffer createMessageSaveSchedulation(TriggerInfo triggerInfo, boolean runImmediately,IEngUserProfile profile) throws EMFUserError {
		
		StringBuffer message = new StringBuffer();
		JobInfo jobInfo = triggerInfo.getJobInfo();
		
		message.append("<SERVICE_REQUEST ");
		
		message.append(" jobName=\""+jobInfo.getJobName()+"\" ");
		
		message.append(" jobGroup=\""+jobInfo.getJobGroupName()+"\" ");
		if(runImmediately) {
			message.append(" runImmediately=\"true\" ");
		} else {
			message.append(" triggerName=\""+triggerInfo.getTriggerName()+"\" ");
			
			message.append(" triggerDescription=\""+triggerInfo.getTriggerDescription()+"\" ");
			message.append(" startDate=\""+triggerInfo.getStartDate()+"\" ");
			
			message.append(" startTime=\""+triggerInfo.getStartTime()+"\" ");
			
			message.append(" chronString=\""+triggerInfo.getChronString()+"\" ");
			
			String enddate = triggerInfo.getEndDate();
			String endtime = triggerInfo.getEndTime();
			if(!enddate.trim().equals("")){
				message.append(" endDate=\""+enddate+"\" ");
				
				if(!endtime.trim().equals("")){
					message.append(" endTime=\""+endtime+"\" ");
					
				}
			}
		}
		String repeatinterval = triggerInfo.getRepeatInterval();
		if(!repeatinterval.trim().equals("")){
			message.append(" repeatInterval=\""+repeatinterval+"\" ");
			
		}	
		message.append(">");
		
		serializeSaveParameterOptions(message, triggerInfo, runImmediately, profile);
		
		message.append("</SERVICE_REQUEST>");
		
		return message;
	}
	
	private void serializeSaveParameterOptions(StringBuffer message, TriggerInfo triggerInfo, boolean runImmediately, IEngUserProfile profile) throws EMFUserError {
		
		message.append("   <PARAMETERS>");		
		
		Map<String, DispatchContext> saveOptions = triggerInfo.getSaveOptions();
		Set<String> uniqueDispatchContextNames =  saveOptions.keySet();
		
		for(String uniqueDispatchContextName : uniqueDispatchContextNames) {
			DispatchContext dispatchContext = (DispatchContext)saveOptions.get(uniqueDispatchContextName);
			
			String saveOptString = "";
			
			saveOptString += serializeSaveAsSnapshotOptions(dispatchContext);
			saveOptString += serializeSaveAsFileOptions(dispatchContext);
			saveOptString += serializeSaveAsJavaClassOptions(dispatchContext);
			saveOptString += serializeSaveAsDocumentOptions(dispatchContext);
			saveOptString += serializeSaveAsMailOptions(dispatchContext);
			saveOptString += serializeSaveAsDistributionListOptions(dispatchContext, uniqueDispatchContextName, triggerInfo, runImmediately, profile);
						
			message.append("   	   <PARAMETER name=\"biobject_id_"+uniqueDispatchContextName+"\" value=\""+saveOptString+"\" />");
		}
		
		message.append("   </PARAMETERS>");
	}
	
	
	
	private String serializeSaveAsSnapshotOptions(DispatchContext dispatchContext) {
		String saveOptString = "";
		
		if(dispatchContext.isSnapshootDispatchChannelEnabled()) {
			saveOptString += "saveassnapshot=true%26";
			if( (dispatchContext.getSnapshotName()!=null) && !dispatchContext.getSnapshotName().trim().equals("") ) {
				saveOptString += "snapshotname="+dispatchContext.getSnapshotName()+"%26";
			}
			if( (dispatchContext.getSnapshotDescription()!=null) && !dispatchContext.getSnapshotDescription().trim().equals("") ) {
				saveOptString += "snapshotdescription="+dispatchContext.getSnapshotDescription()+"%26";
			}
			if( (dispatchContext.getSnapshotHistoryLength()!=null) && !dispatchContext.getSnapshotHistoryLength().trim().equals("") ) {
				saveOptString += "snapshothistorylength="+dispatchContext.getSnapshotHistoryLength()+"%26";
			}
		}
		
		return saveOptString;
	}
	
	private String serializeSaveAsJavaClassOptions(DispatchContext dispatchContext) {
		String saveOptString = "";
		
		if(dispatchContext.isJavaClassDispatchChannelEnabled()) {
			saveOptString += "sendtojavaclass=true%26";
			if( (dispatchContext.getJavaClassPath()!=null) && !dispatchContext.getJavaClassPath().trim().equals("") ) {
				saveOptString += "javaclasspath="+dispatchContext.getJavaClassPath()+"%26";
			}
		}	
		
		return saveOptString;
	}
	
	
	
	private String  serializeSaveAsFileOptions(DispatchContext dispatchContext) {
		String saveOptString = "";
		
		if(dispatchContext.isFileSystemDispatchChannelEnabled()) {
			saveOptString += "saveasfile=true%26";
			if( StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder()) ) {
				saveOptString += "destinationfolder="+dispatchContext.getDestinationFolder()+"%26";
			}
			if( StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder()) ) {
				saveOptString += "destinationfolder="+dispatchContext.getDestinationFolder()+"%26";
			}
			if(dispatchContext.isDestinationFolderRelativeToResourceFolder()) {
				saveOptString += "isrelativetoresourcefolder=true%26";
			} else {
				saveOptString += "isrelativetoresourcefolder=false%26";
			}
			
			if(dispatchContext.isProcessMonitoringEnabled()) {
				saveOptString += "isprocessmonitoringenabled=true%26";
			} else {
				saveOptString += "isprocessmonitoringenabled=false%26";
			}
		}	

		if(dispatchContext.isZipFileDocument()) {
			saveOptString += "zipFileDocument=true%26";
		}
		if(dispatchContext.getFileName() != null) {
			saveOptString += "fileName="+dispatchContext.getFileName()+"%26";
		}

		if(dispatchContext.getZipFileName() != null) {
			saveOptString += "zipFileName="+dispatchContext.getZipFileName()+"%26";
		}
		
		return saveOptString;
	}

	
	
	private String serializeSaveAsDocumentOptions(DispatchContext dispatchContext) {
		String saveOptString = "";
		
		if(dispatchContext.isFunctionalityTreeDispatchChannelEnabled()) {
			saveOptString += "saveasdocument=true%26";
			if( (dispatchContext.getDocumentName()!=null) && !dispatchContext.getDocumentName().trim().equals("") ) {
				saveOptString += "documentname="+dispatchContext.getDocumentName()+"%26";
			}
			if( (dispatchContext.getDocumentDescription()!=null) && !dispatchContext.getDocumentDescription().trim().equals("") ) {
				saveOptString += "documentdescription="+dispatchContext.getDocumentDescription()+"%26";
			}
			if(dispatchContext.isUseFixedFolder() && dispatchContext.getFoldersTo() != null && !dispatchContext.getFoldersTo().trim().equals("")) {
				saveOptString += "foldersTo="+dispatchContext.getFoldersTo()+"%26";
			}
			if(dispatchContext.isUseFolderDataSet() && dispatchContext.getDataSetFolderLabel() != null && !dispatchContext.getDataSetFolderLabel().trim().equals("")) {
				saveOptString += "datasetFolderLabel="+dispatchContext.getDataSetFolderLabel()+"%26";
				if (dispatchContext.getDataSetFolderParameterLabel() != null && !dispatchContext.getDataSetFolderParameterLabel().trim().equals("")) {
					saveOptString += "datasetFolderParameterLabel="+dispatchContext.getDataSetFolderParameterLabel()+"%26";
				}
			}
			if( (dispatchContext.getDocumentHistoryLength()!=null) && !dispatchContext.getDocumentHistoryLength().trim().equals("") ) {
				saveOptString += "documenthistorylength="+dispatchContext.getDocumentHistoryLength()+"%26";
			}
			if( (dispatchContext.getFunctionalityIds()!=null) && !dispatchContext.getFunctionalityIds().trim().equals("") ) {
				saveOptString += "functionalityids="+dispatchContext.getFunctionalityIds()+"%26";
			}
		}
		
		return saveOptString;
	}
	
	private String serializeSaveAsMailOptions(DispatchContext dispatchContext) {
		String saveOptString = "";
		
		if(dispatchContext.isMailDispatchChannelEnabled()) {
			saveOptString += "sendmail=true%26";
			if(dispatchContext.isUseFixedRecipients() && dispatchContext.getMailTos() != null && !dispatchContext.getMailTos().trim().equals("")) {
				saveOptString += "mailtos="+dispatchContext.getMailTos()+"%26";
			}
			if(dispatchContext.isUseDataSet() && dispatchContext.getDataSetLabel() != null && !dispatchContext.getDataSetLabel().trim().equals("")) {
				saveOptString += "datasetLabel="+dispatchContext.getDataSetLabel()+"%26";
				if (dispatchContext.getDataSetParameterLabel() != null && !dispatchContext.getDataSetParameterLabel().trim().equals("")) {
					saveOptString += "datasetParameterLabel="+dispatchContext.getDataSetParameterLabel()+"%26";
				}
			}
			if(dispatchContext.isUseExpression() && dispatchContext.getExpression() != null && !dispatchContext.getExpression().trim().equals("")) {
				saveOptString += "expression="+dispatchContext.getExpression()+"%26";
			}
			if( (dispatchContext.getMailSubj()!=null) && !dispatchContext.getMailSubj().trim().equals("") ) {
				saveOptString += "mailsubj="+dispatchContext.getMailSubj()+"%26";
			}
			if( (dispatchContext.getMailTxt()!=null) && !dispatchContext.getMailTxt().trim().equals("") ) {
				saveOptString += "mailtxt="+dispatchContext.getMailTxt()+"%26";
			}

			
			
			// Mail			
			if(dispatchContext.isZipMailDocument()) {
				saveOptString += "zipMailDocument=true%26";
			}
			if(dispatchContext.isReportNameInSubject()) {
				saveOptString += "reportNameInSubject=true%26";
			}
			
			if(dispatchContext.isUniqueMail()) {
				saveOptString += "uniqueMail=true%26";
			}

			if(dispatchContext.getContainedFileName() != null) {
				saveOptString += "containedFileName="+dispatchContext.getContainedFileName()+"%26";
			}
			if(dispatchContext.getZipMailName() != null) {
				saveOptString += "zipMailName="+dispatchContext.getZipMailName()+"%26";
			}

			
		}
		
		return saveOptString;
	}
	
	private String serializeSaveAsDistributionListOptions(DispatchContext dispatchContext, String uniqueDispatchContextName, TriggerInfo triggerInfo, boolean runImmediately, IEngUserProfile profile) throws EMFUserError {
		String saveOptString = "";
		
		JobInfo jobInfo = triggerInfo.getJobInfo();
		
		if(dispatchContext.isDistributionListDispatchChannelEnabled()) {
			String xml = "";
			if(!runImmediately){
				xml += "<SCHEDULE ";
				xml += " jobName=\""+jobInfo.getJobName()+"\" ";					
				xml += " triggerName=\""+triggerInfo.getTriggerName()+"\" ";					
				xml += " startDate=\""+triggerInfo.getStartDate()+"\" ";					
				xml += " startTime=\""+triggerInfo.getStartTime()+"\" ";					
				xml += " chronString=\""+triggerInfo.getChronString()+"\" ";
				String enddate = triggerInfo.getEndDate();
				String endtime = triggerInfo.getEndTime();
				if(!enddate.trim().equals("")){
					xml += " endDate=\""+enddate+"\" ";
					if(!endtime.trim().equals("")){
						xml += " endTime=\""+endtime+"\" ";
					}
				}			
				
				String repeatinterval = triggerInfo.getRepeatInterval();
				if(!repeatinterval.trim().equals("")){
					xml += " repeatInterval=\""+repeatinterval+"\" ";
				}	
				xml += ">";
				
				String params = "<PARAMETERS>";
				
				
				List biObjects = jobInfo.getDocuments();
				Iterator iterbiobj = biObjects.iterator();
				int index = 0;
				while (iterbiobj.hasNext()){
					index ++;
					BIObject biobj = (BIObject)iterbiobj.next();
					String objpref = biobj.getId().toString()+"__" + new Integer(index).toString();
					if(uniqueDispatchContextName.equals(objpref)){
					
					List pars = biobj.getBiObjectParameters();
					Iterator iterPars = pars.iterator();
					String queryString= "";
					while(iterPars.hasNext()) {
						BIObjectParameter biobjpar = (BIObjectParameter)iterPars.next();
						String concatenatedValue = "";
						List values = biobjpar.getParameterValues();
						if(values!=null) {
							Iterator itervalues = values.iterator();
							while(itervalues.hasNext()) {
								String value = (String)itervalues.next();
								concatenatedValue += value + ",";
							}
							if(concatenatedValue.length()>0) {
								concatenatedValue = concatenatedValue.substring(0, concatenatedValue.length() - 1);
								queryString += biobjpar.getParameterUrlName() + "=" + concatenatedValue + "%26";
							}
						}
					}
					if(queryString.length()>0) {
						queryString = queryString.substring(0, queryString.length()-3);
					}
					params += "<PARAMETER name=\""+biobj.getLabel()+"__"+index+"\" value=\""+queryString+"\" />";
					}else{  
						continue;
					}
				}
				params += "</PARAMETERS>";
				
				xml += params ;
				xml += "</SCHEDULE>";
			}
			
			saveOptString += "sendtodl=true%26";
			
			List l= dispatchContext.getDlIds();
			if(!l.isEmpty()){
				
				String dlIds = "dlId=";
				int objId = dispatchContext.getBiobjId();
				Iterator iter = l.iterator();
				while (iter.hasNext()){
					
					Integer dlId = (Integer)iter.next();
					try {if(!runImmediately){
						IDistributionListDAO dao=DAOFactory.getDistributionListDAO();
						dao.setUserProfile(profile);
						DistributionList dl = dao.loadDistributionListById(dlId);
						dao.insertDLforDocument(dl, objId, xml);
					}
					} catch (Exception ex) {
						logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
						throw new EMFUserError(EMFErrorSeverity.ERROR, 100);			
					}
					
					if (iter.hasNext()) {dlIds += dlId.intValue()+"," ;}
					else {dlIds += dlId.intValue();}
					
				}
				saveOptString += dlIds+"%26";
			
			}	
		}	
		
		return saveOptString;
	}
	

	
}	
	
	
