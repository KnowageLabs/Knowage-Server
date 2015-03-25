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
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.scheduler.Formula;
import it.eng.spagobi.tools.scheduler.FormulaParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.RuntimeLoadingParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class JobManagementModule extends AbstractHttpModule {
	
	private RequestContainer reqCont = null;
	private SessionContainer sessionContainer = null;
	private IEngUserProfile profile = null;
	
	public static final String MODULE_PAGE = "SchedulerGUIPage";
	public static final String JOB_GROUP = "BIObjectExecutions";
	public static final String JOB_NAME_PREFIX = "Execute_";
	
	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(JobManagementModule.class);

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {	
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception { 
		String message = (String) request.getAttribute("MESSAGEDET");
		logger.debug("begin of scheuling service =" +message);
		reqCont = getRequestContainer();
		sessionContainer = reqCont.getSessionContainer();
		profile = (IEngUserProfile) sessionContainer.getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		EMFErrorHandler errorHandler = getErrorHandler();
		try {
			if(message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				logger.error("The message is null");
				throw userError;
			}
			if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_GET_ALL_JOBS) ||
			   message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_ORDER_LIST)) {
				getAllJobs(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_NEW_JOB)) {
				newJob(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_DOCUMENTS_SELECTED)) {
				documentSelected(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_SAVE_JOB)) {
				saveJob(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_DELETE_JOB)) {
				deleteJob(request, response);
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.MESSAGE_GET_JOB_DETAIL)) {
				getJobDetail(request, response);	
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.RETURN_TO_ACTIVITY_DETAIL)) {
				returnToJobDetail(request, response);	
			} else if(message.trim().equalsIgnoreCase(SpagoBIConstants.IGNORE_WARNING)) {
				ignoreWarning(request, response);	
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			logger.error("Error while executing schedule service", ex);
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
		logger.debug("end of scheuling service =" +message);
	}
	
	
	
	
	private void getAllJobs(SourceBean request, SourceBean response) throws EMFUserError {
		try {
			// create the sourcebean of the list
			SourceBean pageListSB  = new SourceBean("PAGED_LIST");
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String xmlList = schedulerService.getJobList();
			//SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(wsresp);
			SourceBean rowsSB = SourceBean.fromXMLString(xmlList);
			if(rowsSB==null){
				throw new Exception("Web service response incomplete");
			}
			// recover all jobs
			List jobSBs = rowsSB.getAttributeAsList("ROW");
			
			Iterator jobSBiter = jobSBs.iterator();
			while(jobSBiter.hasNext()) {
				SourceBean jobSB = (SourceBean)jobSBiter.next();
				String jobname = (String)jobSB.getAttribute("jobName");
				String jobgroupname = (String)jobSB.getAttribute("jobGroupName");
				String xmlSchedList = schedulerService.getJobSchedulationList(jobname, jobgroupname);
				int numSchedulation = 0;
				SourceBean rowsSB_JSL = SourceBean.fromXMLString(xmlSchedList);
				if(rowsSB_JSL!=null) {
					List schedulations = rowsSB_JSL.getAttributeAsList("ROW");
					if(schedulations!=null){
						numSchedulation = schedulations.size();
					}
				}
				jobSB.setAttribute("numSchedule", new Integer(numSchedulation));
			}
			// fill the list sourcebean
			pageListSB.setAttribute(rowsSB);

			//ordering of list
			String typeOrder = (request.getAttribute("TYPE_ORDER")==null)?" ASC":(String)request.getAttribute("TYPE_ORDER");
			String fieldOrder = (request.getAttribute("FIELD_ORDER")==null)?" jobDescription":(String)request.getAttribute("FIELD_ORDER");
			pageListSB = orderJobList(pageListSB, typeOrder, fieldOrder);

			// populate response with the right values			
			response.setAttribute(pageListSB);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ListJobs");
		} catch (Exception ex) {
			logger.error("Error while recovering all job definition", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1000", "component_scheduler_messages");
		}
	}
	
	
	private void newJob(SourceBean request, SourceBean response) throws EMFUserError {
		try {
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
			JobInfo jobInfo = new JobInfo();
			jobInfo.setSchedulerAdminstratorIdentifier(profile.getUserUniqueIdentifier().toString());
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			sessionContainer.setAttribute(SpagoBIConstants.JOB_INFO, jobInfo);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "JobDetail");
		} catch (Exception ex) {
			logger.error("Error while recovering objects for scheduling", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1001", "component_scheduler_messages");
		}
	}
	
	private void returnToJobDetail(SourceBean request, SourceBean response) throws EMFUserError {
		logger.debug("IN");
		try {
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "JobDetail");
		} catch (Exception ex) {
			logger.error("Error while recovering objects for scheduling", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1001", "component_scheduler_messages");
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void ignoreWarning(SourceBean request, SourceBean response) throws EMFUserError {
		logger.debug("IN");
		try {
			JobInfo jobInfo = (JobInfo) sessionContainer.getAttribute(SpagoBIConstants.JOB_INFO);
			saveJob(jobInfo);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ReturnToJobList");	
		} catch (Exception ex) {
			logger.error("Error while recovering objects for scheduling", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1001", "component_scheduler_messages");
		} finally {
			logger.debug("OUT");
		}
	}
	
	private void deleteJob(SourceBean request, SourceBean response) throws EMFUserError {
		HashMap<String, String> logParam = new HashMap();
		try {
		    ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String jobName = (String)request.getAttribute("jobName");
			String jobGroupName = (String)request.getAttribute("jobGroupName");
			String xmlSchedList = schedulerService.getJobSchedulationList(jobName, jobGroupName);
			SourceBean rowsSB_JSL = SourceBean.fromXMLString(xmlSchedList);
			if(rowsSB_JSL==null) {
				throw new Exception("List of job triggers not returned by Web service ");
			}
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP NAME", jobGroupName);
			// delete each schedulation
			List schedules = rowsSB_JSL.getAttributeAsList("ROW");
			Iterator iterSchedules = schedules.iterator();
			while(iterSchedules.hasNext()) {
			   	SourceBean scheduleSB = (SourceBean)iterSchedules.next();
			   	String triggerName = (String)scheduleSB.getAttribute("triggerName");
			   	String triggerGroup = (String)scheduleSB.getAttribute("triggerGroup");
			   	DAOFactory.getDistributionListDAO().eraseAllRelatedDistributionListObjects(triggerName);
			   	String delResp = schedulerService.deleteSchedulation(triggerName, triggerGroup);
				SourceBean schedModRespSB_DS = SchedulerUtilities.getSBFromWebServiceResponse(delResp);
				if(schedModRespSB_DS==null) {
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.DELETE",logParam , "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Imcomplete response returned by the Web service " +
							            "during schedule "+triggerName+" deletion");
				}	
				if(!SchedulerUtilities.checkResultOfWSCall(schedModRespSB_DS)){
					try {
						AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.DELETE",logParam , "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Schedule "+triggerName+" not deleted by the Web Service");
				}
			}			
			// delete job	
			String resp_DJ = schedulerService.deleteJob(jobName, jobGroupName);
			SourceBean schedModRespSB_DJ = SchedulerUtilities.getSBFromWebServiceResponse(resp_DJ);
			if(schedModRespSB_DJ==null) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.DELETE",logParam , "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				throw new Exception("Imcomplete response returned by the Web service " +
						            "during job "+jobName+" deletion");
			}	
			if(!SchedulerUtilities.checkResultOfWSCall(schedModRespSB_DJ)){
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.DELETE",logParam , "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				throw new Exception("JOb "+jobName+" not deleted by the Web Service");
			}
			// fill response
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ReturnToJobList");
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.DELETE",logParam , "KO");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.error("Error while deleting job", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1002", "component_scheduler_messages");
		}
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.DELETE",logParam , "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void documentSelected(SourceBean request, SourceBean response) throws EMFUserError {
		try {
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
			
			
			JobInfo jobInfo = (JobInfo)sessionContainer.getAttribute(SpagoBIConstants.JOB_INFO);
			getJobGenericDataFromRequest(request, jobInfo);
			getDocParValuesFromRequest(request, jobInfo);
		
			List<BIObject> previouslySelectedDocuments = jobInfo.getDocuments();
			List<Integer> previouslySelectedDocumentIds = jobInfo.getDocumentIds();
			logger.debug("Previouly selected documents [" + previouslySelectedDocumentIds.size() + "]");
			
			List<BIObject> newlySelectedDocuments = new ArrayList<BIObject>();
			String newlySelectedDocumentIdsString = (String)request.getAttribute("selected_biobject_ids");
			logger.debug("Newly selected document ids string is equal to [" + newlySelectedDocumentIdsString + "]");
			
			if (newlySelectedDocumentIdsString.equals(""))
				newlySelectedDocuments = new ArrayList<BIObject>();
			else{
				String[] newlySelectedDocumentIdsArray = newlySelectedDocumentIdsString.split(",");
				List<String> newlySelectedDocumentIds = Arrays.asList(newlySelectedDocumentIdsArray);
				logger.debug("Newly selected documents [" + newlySelectedDocumentIds.size() + "]");
				
				for(String newlySelectedDocumentId : newlySelectedDocumentIds) {
					logger.debug("Processing newly selected document [" + newlySelectedDocumentId + "]");
					Integer documentId = Integer.valueOf(newlySelectedDocumentId.substring(0, newlySelectedDocumentId.lastIndexOf("__")));
					logger.debug("The id of newly selected document [" + newlySelectedDocumentId + "] is equal to [" + documentId + "]");
					
					if(!previouslySelectedDocumentIds.contains(documentId)) {
						logger.debug("Document whose id is equal to [" + documentId + "] is not contained in previously selected documents");
						IBIObjectDAO documentDAO = DAOFactory.getBIObjectDAO();
						IBIObjectParameterDAO documentParameterDAO = DAOFactory.getBIObjectParameterDAO();
						BIObject document = documentDAO.loadBIObjectById( documentId );
						List documentParameters = documentParameterDAO.loadBIObjectParametersById( documentId );
						document.setBiObjectParameters(documentParameters);
						newlySelectedDocuments.add(document);
					} else {
						logger.debug("Document whose id is equal to [" + documentId + "] is contained in previously selected documents");
						//Iterator iter_prev_biobj = previouslySelectedDocuments.iterator();
						int index = 0;
						boolean flgExists = false;
						//preserves documents already existing
						for(BIObject previouslySelectedDocument : previouslySelectedDocuments){
							index ++;
							String tmpID = previouslySelectedDocument.getId().toString()+"__"+index;
							if(tmpID.equals(newlySelectedDocumentId)) {
								newlySelectedDocuments.add(previouslySelectedDocument);
								flgExists = true;
								continue;
							}
						}
						//adds new copy of document already existing
						if (!flgExists){
							IBIObjectDAO documentDAO = DAOFactory.getBIObjectDAO();
							IBIObjectParameterDAO documentParameterDAO = DAOFactory.getBIObjectParameterDAO();
							BIObject document = documentDAO.loadBIObjectById( documentId );
							List documentParameters = documentParameterDAO.loadBIObjectParametersById( documentId );
							document.setBiObjectParameters(documentParameters);
							newlySelectedDocuments.add(document);
						}							
					}
				}
			}
			logger.debug("Selected documents [" + newlySelectedDocuments.size() + "]");
			jobInfo.setDocuments(newlySelectedDocuments);
			sessionContainer.setAttribute(SpagoBIConstants.JOB_INFO, jobInfo);
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "JobDetail");
		} catch (Exception ex) {
			logger.error("Error while selecting documents", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1006", "component_scheduler_messages");
		}
	}
	
	
	
	
	private void saveJob(SourceBean request, SourceBean response) throws EMFUserError {
		try {			
			// get job information from session
			JobInfo jobInfo = (JobInfo)sessionContainer.getAttribute(SpagoBIConstants.JOB_INFO);
			// recover generic data
			getJobGenericDataFromRequest(request, jobInfo);
			// recover parameter values
			getDocParValuesFromRequest(request, jobInfo);
			// check for input validation errors 
			if(!this.getErrorHandler().isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)) {
				List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
				response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "JobDetail");
				return;
			}
			
			boolean warningNeeded = false;
			Map documents = new HashMap();
			List biobjs = jobInfo.getDocuments();
			Iterator iterbiobj = biobjs.iterator();
			float totalCombinations = 0;
			while(iterbiobj.hasNext()) {
				BIObject biobj = (BIObject)iterbiobj.next();
				float combinations = calculateCombinations(biobj);
				totalCombinations += combinations;
				float previous = 0;
				if (documents.containsKey(biobj.getName())) {
					Float previousFloat = (Float) documents.get(biobj.getName());
					previous = previousFloat.floatValue();
				}
				// adds to previous combinations number
				combinations += previous;
				// documents map will contain all documents with execution combinations number
				documents.put(biobj.getName(), new Float(combinations));
			}
			
			if (totalCombinations > 10) {
				// if combination of parameters exceeds 10, a warning is needed
				warningNeeded = true;
			}

			if (warningNeeded) {
				response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "JobIterationWarning");
				response.setAttribute("EXCEEDING_CONFIGURATIONS", documents);
				return;
			}
			saveJob(jobInfo);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ReturnToJobList");	
		} catch (Exception ex) {
			logger.error("Error while saving job", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1004", "component_scheduler_messages");
		}
	}

	
	private void saveJob(JobInfo jobInfo) throws EMFUserError {
		HashMap<String, String> logParam = new HashMap();
		try {			
			logParam.put("JOB NAME", jobInfo.getJobName());
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			// create message to define the new job (for the web service)
			String jobGroupName = JOB_GROUP;
			StringBuffer message = new StringBuffer();
			message.append("<SERVICE_REQUEST ");
			message.append(" jobName=\""+jobInfo.getJobName()+"\" ");
			message.append(" jobDescription=\""+jobInfo.getJobDescription()+"\" ");
			message.append(" jobGroupName=\""+jobGroupName+"\" ");
			message.append(" jobRequestRecovery=\"false\" ");
			message.append(" jobClass=\"" + ExecuteBIDocumentJob.class.getName() + "\" ");
			message.append(">");
			message.append("   <PARAMETERS>");
			List biobjs = jobInfo.getDocuments();
			Iterator iterbiobj = biobjs.iterator();
			String doclabels = "";
			int index = 0;
			while(iterbiobj.hasNext()) {
				index ++;
				BIObject biobj = (BIObject)iterbiobj.next();
				List pars = biobj.getBiObjectParameters();
				Iterator iterPars = pars.iterator();
				StringBuffer fixedParameters = new StringBuffer("");
				StringBuffer iterativeParameters = new StringBuffer("");
				StringBuffer loadAtRuntimeParameters = new StringBuffer("");
				StringBuffer useFormulaParameters = new StringBuffer("");
				while(iterPars.hasNext()) {
					BIObjectParameter biobjpar = (BIObjectParameter)iterPars.next();
					if (biobjpar.isIterative()) {
						iterativeParameters.append(biobjpar.getParameterUrlName() + ";");
					}
					Object strategyObj = biobjpar.getParameterValuesRetriever();
					if (strategyObj != null && strategyObj instanceof RuntimeLoadingParameterValuesRetriever) {
						RuntimeLoadingParameterValuesRetriever strategy = (RuntimeLoadingParameterValuesRetriever) strategyObj;
						String user = strategy.getUserIndentifierToBeUsed();
						String role = strategy.getRoleToBeUsed();
						loadAtRuntimeParameters.append(biobjpar.getParameterUrlName() + "(" + user + "|" + role + ");");
					} else if (strategyObj != null && strategyObj instanceof FormulaParameterValuesRetriever) { 
						FormulaParameterValuesRetriever strategy = (FormulaParameterValuesRetriever) strategyObj;
						String fName = strategy.getFormula().getName();
						useFormulaParameters.append(biobjpar.getParameterUrlName() + "(" + fName + ");");
					} else {
						String concatenatedValue = "";
						List values = biobjpar.getParameterValues();
						if(values != null && !values.isEmpty()) {
							Iterator itervalues = values.iterator();
							while(itervalues.hasNext()) {
								String value = (String)itervalues.next();
								concatenatedValue += value + ";";
							}
							if(concatenatedValue.length()>0) {
								concatenatedValue = concatenatedValue.substring(0, concatenatedValue.length() - 1);
							}
	
						}
						if(concatenatedValue.length()>0) {
							fixedParameters.append(biobjpar.getParameterUrlName() + "=" + concatenatedValue + "%26");
						}
					}

				}
				if (fixedParameters.length() > 0) {
					fixedParameters = fixedParameters.delete(fixedParameters.length() -1, fixedParameters.length() - 1);
				}
				message.append("<PARAMETER name=\""+biobj.getLabel()+"__"+index+"\" value=\""+fixedParameters.toString()+"\" />");
				if (iterativeParameters.length() > 0) {
					iterativeParameters.deleteCharAt(iterativeParameters.length() - 1);
					message.append("<PARAMETER name=\""+biobj.getLabel()+"__"+index+"_iterative\" value=\""+iterativeParameters.toString()+"\" />");
				}
				if (loadAtRuntimeParameters.length() > 0) {
					loadAtRuntimeParameters.deleteCharAt(loadAtRuntimeParameters.length() - 1);
					message.append("<PARAMETER name=\""+biobj.getLabel()+"__"+index+"_loadAtRuntime\" value=\""+loadAtRuntimeParameters.toString()+"\" />");
				}
				if (useFormulaParameters.length() > 0) {
					useFormulaParameters.deleteCharAt(useFormulaParameters.length() - 1);
					message.append("<PARAMETER name=\""+biobj.getLabel()+"__"+index+"_useFormula\" value=\""+useFormulaParameters.toString()+"\" />");
				}
				doclabels += biobj.getLabel() +"__"+index+ ",";
				
			}
			if(doclabels.length()>0) {
				doclabels = doclabels.substring(0, doclabels.length()-1);
			}
			logParam.put("DOC LABELS", doclabels);
			message.append("   	   <PARAMETER name=\"documentLabels\" value=\""+doclabels+"\" />");
			message.append("   </PARAMETERS>");
			message.append("</SERVICE_REQUEST>");
			// call the web service
			String servoutStr = schedulerService.defineJob(message.toString());
			SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
			if(schedModRespSB==null) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.SAVE",logParam , "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				throw new Exception("Imcomplete response returned by the Web service " +
						            "during job "+jobInfo.getJobName()+" creation");
			}	
			if(!SchedulerUtilities.checkResultOfWSCall(schedModRespSB)){
				throw new Exception("Job "+jobInfo.getJobName()+" not created by the web service");
			}
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.SAVE",logParam , "OK");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "SCHEDULER.SAVE",logParam , "KO");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.error("Error while saving job", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1004", "component_scheduler_messages");
		}
	}
		

	
	private float calculateCombinations(BIObject biobj) {
		logger.debug("IN");
		float toReturn = 1;
		List parameters = biobj.getBiObjectParameters();
		Iterator it = parameters.iterator();
		while (it.hasNext()) {
			BIObjectParameter parameter = (BIObjectParameter) it.next();
			if (parameter.isIterative()) {
				List values = parameter.getParameterValues();
				if (values != null && values.size() > 1) 
					toReturn *= values.size();
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private void getJobDetail(SourceBean request, SourceBean response) throws EMFUserError {
		try {
		    ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
			String jobName = (String)request.getAttribute("jobName");
			String jobGroupName = (String)request.getAttribute("jobGroupName");
	        // call we service
			String respStr = schedulerService.getJobDefinition(jobName, jobGroupName);
            SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr);
			if(jobDetailSB!=null) {
				JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobDetailSB);
				sessionContainer.setAttribute(SpagoBIConstants.JOB_INFO, jobInfo);
			} else {
				throw new Exception("Detail not recovered for job " + jobName);
			}
			// fill response
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "JobDetail");
		} catch (Exception ex) {
			logger.error("Error while getting detail of the job", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1005", "component_scheduler_messages");
		}
	}
			
	
	
	
	private void getJobGenericDataFromRequest(SourceBean request, JobInfo ji) {
		String jobname = (String)request.getAttribute("jobname");
		String jobdescription = (String)request.getAttribute("jobdescription");
		if(jobname!=null) {
			ji.setJobName(jobname);
		}
		if(jobdescription!=null) {
			ji.setJobDescription(jobdescription);
		}
	}
	
	
	private void getDocParValuesFromRequest(SourceBean request, JobInfo jobInfo) throws Exception {
		// get the splitter character
		String splitter = (String)request.getAttribute("splitter");
		// get the list of biobject previously setted
		List biobjects = jobInfo.getDocuments();
		List newBiObjects = new ArrayList();
		// iter over biobjects
		Iterator iterbiobjs = biobjects.iterator();
		int index = 0;
		while(iterbiobjs.hasNext()) {
			index ++;
			BIObject biobj = (BIObject)iterbiobjs.next();
			List biobjpars = biobj.getBiObjectParameters();
			List newBiobjpars = new ArrayList();
			// iter over parameters
			Iterator iterbiobjpars = biobjpars.iterator();
			while(iterbiobjpars.hasNext()) {
				BIObjectParameter biobjpar = (BIObjectParameter)iterbiobjpars.next();
				String nameParInRequest = "par_" + biobj.getId() +"_" + index + "_" + biobjpar.getParameterUrlName();
				
				String strategyToBeUsed = (String) request.getAttribute(nameParInRequest + "_strategy");
				boolean useFixedValues = false;
				boolean loadAtRuntime = false;
				boolean useFormula = false;
				if (strategyToBeUsed == null || strategyToBeUsed.equalsIgnoreCase("fixedValues")) {
					useFixedValues = true;
				} else if (strategyToBeUsed.equalsIgnoreCase("loadAtRuntime")) {
					loadAtRuntime = true;
				} else if (strategyToBeUsed.equalsIgnoreCase("useFormula")) {
					useFormula = true;
				}
				
				String isIterativeStr = (String) request.getAttribute(nameParInRequest + "_Iterative");
				boolean isIterative = isIterativeStr != null && isIterativeStr.equalsIgnoreCase("true");
				biobjpar.setIterative(isIterative);
				if (useFormula) {
					String fName = (String) request.getAttribute(nameParInRequest + "_formula");
					FormulaParameterValuesRetriever strategy = new FormulaParameterValuesRetriever();
					Formula f = Formula.getFormula(fName);
					strategy.setFormula(f);
					biobjpar.setParameterValuesRetriever(strategy);
				} else if (loadAtRuntime) {
					RuntimeLoadingParameterValuesRetriever strategy = new RuntimeLoadingParameterValuesRetriever();
					strategy.setUserIndentifierToBeUsed(profile.getUserUniqueIdentifier().toString());
					String roleToBeUsed = (String) request.getAttribute(nameParInRequest + "_loadWithRole");
					strategy.setRoleToBeUsed(roleToBeUsed);
					biobjpar.setParameterValuesRetriever(strategy);
				} else if (useFixedValues) {
					biobjpar.setParameterValuesRetriever(null);
					String valueParConcat = (String)request.getAttribute(nameParInRequest);
					if(valueParConcat!=null){
						if(valueParConcat.trim().equals("")) {
							biobjpar.setParameterValues(new ArrayList());
							continue;
						} else {
							String[] valueParArr = valueParConcat.split(splitter);
							List valuePar = Arrays.asList(valueParArr);
							biobjpar.setParameterValues(valuePar);
						}
					}
				}
				newBiobjpars.add(biobjpar);
			}
			biobj.setBiObjectParameters(newBiobjpars);
			newBiObjects.add(biobj);
		}
		jobInfo.setDocuments(newBiObjects);
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
	
}	
	
	
