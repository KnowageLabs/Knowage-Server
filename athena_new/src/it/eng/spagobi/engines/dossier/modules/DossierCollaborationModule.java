/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.modules;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UploadedFile;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.container.CoreContextManager;
import it.eng.spagobi.container.IBeanContainer;
import it.eng.spagobi.container.SpagoBIRequestContainer;
import it.eng.spagobi.container.SpagoBISessionContainer;
import it.eng.spagobi.container.strategy.ExecutionContextRetrieverStrategy;
import it.eng.spagobi.container.strategy.IContextRetrieverStrategy;
import it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.engines.dossier.bo.DossierPresentation;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.engines.dossier.dao.DossierDAOHibImpl;
import it.eng.spagobi.engines.dossier.dao.IDossierDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO;
import it.eng.spagobi.engines.dossier.exceptions.OpenOfficeConnectionException;
import it.eng.spagobi.engines.dossier.utils.DossierUtilities;
import it.eng.spagobi.monitoring.dao.AuditManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * This class implements a module which  handles pamphlets collaboration.
 */
public class DossierCollaborationModule extends AbstractModule {
	
	static private Logger logger = Logger.getLogger(DossierCollaborationModule.class);

	private IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the right operation handler.
	 * 
	 * @param request The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 * 
	 * @throws exception If an exception occurs
	 * @throws Exception the exception
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		EMFErrorHandler errorHandler = getErrorHandler();
		String operation = (String) request.getAttribute(SpagoBIConstants.OPERATION);
		try{
			if((operation==null)||(operation.trim().equals(""))) {
				logger.error("The operation parameter is null");
				throw new Exception("The operation parameter is null");
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_OPEN_NOTE_EDITOR)) {
				openNoteEditorHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_SAVE_NOTE)) {
				saveNoteHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_APPROVE_PRESENTATION)) {
				approveHandler(request, response);
			} else if(operation.equalsIgnoreCase(DossierConstants.OPERATION_RUN_NEW_COLLABORATION)) {
				runCollaborationHandler(request, response);
			} else if(operation.equalsIgnoreCase(DossierConstants.OPERATION_DELETE_PRESENTATION_VERSION)) {
				deletePresVerHandler(request, response);
			} else if(operation.equalsIgnoreCase(DossierConstants.OPERATION_PREPARE_PUBLISH_PRESENTATION_PAGE)) {
				preparePublishPageHandler(request, response);
			} else if(operation.equalsIgnoreCase(DossierConstants.OPERATION_PUBLISH_PRESENTATION)) {
				publishHandler(request, response);
			} 
			
		} catch (EMFUserError emfue) {
			errorHandler.addError(emfue);
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		} finally {
			logger.debug("OUT");
		}
	}

	
	
	private void publishHandler(SourceBean request, SourceBean response) {
		try{
			SessionContainer session = this.getRequestContainer().getSessionContainer();
			SessionContainer permanentSession = session.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			String userId = (String) ((UserProfile)profile).getUserId();
			
			String label = (String)request.getAttribute("label");
			if(label==null) label = "";
			String description = (String)request.getAttribute("description");
			if(description==null) description = "";
			String name = (String)request.getAttribute("name");
			if(name==null) name = "";
			String state = (String)request.getAttribute("state");
			Integer stateId = new Integer(57);
			String stateCode = "REL";
			if(state==null) {
				state = "";			
			} else {
				StringTokenizer tokenState = new StringTokenizer(state, ",");
				String stateIdStr = tokenState.nextToken();
				stateId = new Integer(stateIdStr);
				stateCode = tokenState.nextToken();
			}
			
			
			
			boolean visible = true;
			String visibleStr = (String)request.getAttribute("visible");
			if(visibleStr != null && visibleStr.equalsIgnoreCase("0")) visible = false;
			
			String dossierIdStr = (String) request.getAttribute(DossierConstants.DOSSIER_ID);
			String versionIdStr = (String)request.getAttribute(DossierConstants.VERSION_ID);
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(false);
			EMFErrorHandler errorHandler = getResponseContainer().getErrorHandler();
			if(!errorHandler.isOK()){
				if(GeneralUtilities.isErrorHandlerContainingOnlyValidationError(errorHandler)) {
					response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
					response.setAttribute(DossierConstants.PUBLISHER_NAME, "publishPresentation");
					response.setAttribute("label", label);
					response.setAttribute("name", name);
					response.setAttribute("description", description);
					response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
					response.setAttribute(DossierConstants.VERSION_ID, versionIdStr);
					return;
				}
			} else {
				// recover office document sbidomains
				IDomainDAO domainDAO = DAOFactory.getDomainDAO();
				Domain officeDocDom = domainDAO.loadDomainByCodeAndValue("BIOBJ_TYPE", "OFFICE_DOC");
				// recover development sbidomains
				Domain devDom = domainDAO.loadDomainByCodeAndValue("STATE", "DEV");
				// recover engine
				IEngineDAO engineDAO = DAOFactory.getEngineDAO();
				List engines = engineDAO.loadAllEnginesForBIObjectType(officeDocDom.getValueCd());
				Engine engine = (Engine)engines.get(0);
				// load the template
				UploadedFile uploadedFile = new UploadedFile();
				IDossierPresentationsDAO dpDAO = DAOFactory.getDossierPresentationDAO();
				Integer dossierId = new Integer(dossierIdStr);
				Integer versionId = new Integer(versionIdStr);
				byte[] tempCont = dpDAO.getPresentationVersionContent(dossierId, versionId);
				BIObject dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
				String bookName = dossier.getName();
				ObjTemplate templ = new ObjTemplate();
				templ.setActive(new Boolean(true));
		        templ.setName(bookName + ".ppt");
		        templ.setContent(tempCont);
				// load all functionality
				List storeInFunctionalities = new ArrayList();
				List functIds = request.getAttributeAsList("FUNCT_ID");
				Iterator iterFunctIds = functIds.iterator();
				while(iterFunctIds.hasNext()) {
					String functIdStr = (String)iterFunctIds.next();
					Integer functId = new Integer(functIdStr);
					storeInFunctionalities.add(functId);
				}
				// create biobject
				BIObject biobj = new BIObject();
				biobj.setCreationUser(userId);
				biobj.setDescription(description);
				biobj.setLabel(label);
				biobj.setName(name);
				biobj.setEncrypt(new Integer(0));
				biobj.setEngine(engine);
				biobj.setDataSourceId(null);
				biobj.setRelName("");
				biobj.setBiObjectTypeCode(officeDocDom.getValueCd());
				biobj.setBiObjectTypeID(officeDocDom.getValueId());
				biobj.setStateCode(devDom.getValueCd());
				biobj.setStateID(devDom.getValueId());
				biobj.setVisible(new Integer(0));
				biobj.setFunctionalities(storeInFunctionalities);
				biobj.setStateCode(stateCode);
				biobj.setStateID(stateId);
				biobj.setVisible((visible? new Integer(1): new Integer(0)));
				
				IBIObjectDAO objectDAO = DAOFactory.getBIObjectDAO();
				objectDAO.setUserProfile(profile);
				objectDAO.insertBIObject(biobj, templ);
				// put data into response
				response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
				response.setAttribute(DossierConstants.PUBLISHER_NAME, "publishPresentation");
				response.setAttribute("label", "");
				response.setAttribute("name", "");
				response.setAttribute("description", "");
				
				// load list of states and engines
				IDomainDAO domaindao = DAOFactory.getDomainDAO();
				List states = domaindao.loadListDomainsByType("STATE");
			    response.setAttribute(DossierConstants.DOSSIER_PRESENTATION_LIST_STATES, states);
				
				response.setAttribute("PublishMessage", msgBuilder.getMessage("dossier.presPublished", "component_dossier_messages"));
				response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
				response.setAttribute(DossierConstants.VERSION_ID, versionIdStr);
				
			}
		} catch(Exception e){
			logger.error("Error while publishing presentation", e);
		}
	    
	}
	
	
	
	
	private void deletePresVerHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		String dossierIdStr = null;
		List presVersions = null;
		IDossierPresentationsDAO dpDAO = null;
		try{
			dossierIdStr = (String) request.getAttribute(DossierConstants.DOSSIER_ID);
			String versionIdStr = (String) request.getAttribute(DossierConstants.VERSION_ID);
			dpDAO = DAOFactory.getDossierPresentationDAO();
			Integer dossierId = new Integer(dossierIdStr);
			Integer versionId = new Integer(versionIdStr);
			dpDAO.deletePresentationVersion(dossierId, versionId);
			presVersions = dpDAO.getPresentationVersions(dossierId);
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierPresentationVersion");
			response.setAttribute(DossierConstants.DOSSIER_PRESENTATION_VERSIONS, presVersions);
			response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
		} catch (Exception e) {
			logger.error("error while setting response attribute " + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
		}
	}
	
	
	
	
	private void preparePublishPageHandler(SourceBean request, SourceBean response) {
		logger.debug("IN");
		try {
			String dossierIdStr = (String) request.getAttribute(DossierConstants.DOSSIER_ID);
			String versionIdStr = (String)request.getAttribute(DossierConstants.VERSION_ID);
			List functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(false);
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "publishPresentation");
			response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
			response.setAttribute(DossierConstants.VERSION_ID, versionIdStr);
			 // load list of states and engines
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List states = domaindao.loadListDomainsByType("STATE");
		    response.setAttribute(DossierConstants.DOSSIER_PRESENTATION_LIST_STATES, states);
		} catch(Exception e){
			logger.error("Error while preparing page for publishing", e);
		} finally {
			logger.debug("OUT");
		}
	    
	}
	
	
	

	private void runCollaborationHandler(SourceBean request, SourceBean response) throws EMFUserError {
		logger.debug("IN");
//		String dossierIdStr = (String) request.getAttribute(DossierConstants.DOSSIER_ID);
//		Integer dossierId = new Integer(dossierIdStr);
		IDossierDAO dossierDAO = DAOFactory.getDossierDAO();
		BIObject dossier;
		String pathTempFolder = null;
		JbpmContext jbpmContext = null;
		InputStream procDefIS = null;
		String executionMsg = null;
		AuditManager auditManager = AuditManager.getInstance();
		Integer auditId = null;
		try {
//			try {
//				dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(dossierId);
				RequestContainer requestContainer = this.getRequestContainer();
				SessionContainer session = requestContainer.getSessionContainer();
				CoreContextManager contextManager = new CoreContextManager(new SpagoBISessionContainer(session), 
					new LightNavigatorContextRetrieverStrategy(request));
				ExecutionInstance executionInstance = contextManager.getExecutionInstance( ExecutionInstance.class.getName() );
				dossier = executionInstance.getBIObject();
				pathTempFolder = dossierDAO.init(dossier);
//			} catch (EMFUserError e) {
//				logger.error("Error while recovering dossier information: " + e);
//				throw e;
//			}
			IEngUserProfile profile = UserProfile.createWorkFlowUserProfile();
		    // AUDIT
			if (dossier != null) {
				auditId = auditManager.insertAudit(dossier, null, profile, "", "WORKFLOW");
			}
			try {
				procDefIS = dossierDAO.getProcessDefinitionContent(pathTempFolder);
			} catch (Exception e) {
				logger.error("Error while reading process definition file content from dossier template: " + e);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}
			
			// parse process definition
			ProcessDefinition processDefinition = null;
			try{
				processDefinition = ProcessDefinition.parseXmlInputStream(procDefIS);
			} catch(Exception e) {
				executionMsg = msgBuilder.getMessage("dossier.processDefNotCorrect", "component_dossier_messages"); 
				logger.error("Process definition xml file not correct", e);
				throw e;
			}	
		    // get name of the process
			String nameProcess = processDefinition.getName();
			// get jbpm context
			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
			jbpmContext = jbpmConfiguration.createJbpmContext();
			// deploy process   
			try{
				jbpmContext.deployProcessDefinition(processDefinition);  
			} catch (Exception e) {
				executionMsg = msgBuilder.getMessage("dossier.workProcessStartError", "component_dossier_messages");
				logger.error("Error while deploying process definition", e);
				throw e;
			}
			// create process instance
			ProcessInstance processInstance = new ProcessInstance(processDefinition);
			// get context instance and set the dossier id variable
			ContextInstance contextInstance = processInstance.getContextInstance();
			contextInstance.createVariable(DossierConstants.DOSSIER_ID, dossier.getId().toString());
			contextInstance.createVariable(DossierConstants.DOSSIER_PARAMETERS, getDossierParameters(dossier));
			
			// adding parameters for AUDIT updating
			if (auditId != null) {
				contextInstance.createVariable(AuditManager.AUDIT_ID, auditId);
			}
			
			// start workflow
			Token token = processInstance.getRootToken();
			GraphSession graphSess = jbpmContext.getGraphSession();
			try {
				token.signal();
			} catch (Exception e) {
				if(e.getCause() instanceof OpenOfficeConnectionException ) {
					executionMsg = msgBuilder.getMessage("dossier.errorConnectionOO", "component_dossier_messages");
				}
			    throw e;
			}
			// save workflow data
			jbpmContext.save(processInstance); 
			
	    } catch (Exception e) {
	    	if (executionMsg == null) {
	    		executionMsg = msgBuilder.getMessage("dossier.workProcessStartError", "component_dossier_messages");
	    	}
	    	logger.error("Error while starting workflow", e);
			// AUDIT UPDATE
			auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), 
					"STARTUP_FAILED", e.getMessage(), null);
	    } finally {
	    	if (executionMsg == null) {
	    		executionMsg = msgBuilder.getMessage("dossier.workProcessStartCorrectly", "component_dossier_messages");
	    		// AUDIT UPDATE
	    		auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, 
						"EXECUTION_STARTED", null, null);
	    	}
	    	if (jbpmContext != null) {
	    		jbpmContext.close();
	    	}
	    	if (procDefIS != null)
				try {
					procDefIS.close();
				} catch (IOException e) {
					logger.error(e);
				}
		    // cleans dossier temp folder
		    if (dossierDAO != null && pathTempFolder != null) {
		    	dossierDAO.clean(pathTempFolder);
			    logger.debug("Deleted folder " + pathTempFolder);
		    }
	    	logger.debug("OUT");
	    }
	    
	    try {
	    	response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierExecution");
	    	response.setAttribute(DossierConstants.EXECUTION_MESSAGE, executionMsg);
	    } catch (Exception e) {
	    	logger.error("Error while setting attributes into response", e);
	    }

	}

	private Map getDossierParameters(BIObject dossier) {
		Map parameters = new HashMap<String, String>();
		logger.debug("IN");
		List biParameters = dossier.getBiObjectParameters();
		if (biParameters != null && biParameters.size() > 0) {
			Iterator it = biParameters.iterator();
			while (it.hasNext()) {
				BIObjectParameter biParameter = (BIObjectParameter) it.next();
				parameters.put(biParameter.getParameterUrlName(), biParameter.getParameterValues());
			}
		}
		logger.debug("OUT");
		return parameters;
	}

	private void approveHandler(SourceBean request, SourceBean response) {
		logger.debug("IN");
		ContextInstance contextInstance = null;
		JbpmContext jbpmContext = null;
		try {
			// recover task instance and variables
			String activityKey = (String) request.getAttribute(SpagoBIConstants.ACTIVITYKEY);
			String approved = (String) request.getAttribute("approved");
			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
			jbpmContext = jbpmConfiguration.createJbpmContext();
			TaskInstance taskInstance = jbpmContext.getTaskInstance(new Long(activityKey).longValue());
			contextInstance = taskInstance.getContextInstance();
			ProcessInstance processInstance = contextInstance.getProcessInstance();
			Long workflowProcessId = new Long(processInstance.getId());
			String dossierIdStr = (String) contextInstance.getVariable(DossierConstants.DOSSIER_ID);
			Integer dossierId = new Integer(dossierIdStr);
			// store presentation
			SessionContainer session = this.getRequestContainer().getSessionContainer();
			SessionContainer permanentSession = session.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			IDossierPresentationsDAO dpDAO = DAOFactory.getDossierPresentationDAO();
			dpDAO.setUserProfile(profile);
			DossierPresentation currPresentation = dpDAO.getCurrentPresentation(dossierId, workflowProcessId);
			boolean approvedBool = false;
			if (approved.equalsIgnoreCase("true")) {
				approvedBool = true;
			}
			currPresentation.setApproved(new Boolean(approvedBool));
			Integer nextProg = dpDAO.getNextProg(dossierId);
			currPresentation.setProg(nextProg);
			dpDAO.updatePresentation(currPresentation);
			// put attributes into response
			if (approved.equalsIgnoreCase("true")) {
				response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierCompleteActivityLoopback");
			} else {
				response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierRejecrActivityLoopback");
			}
			response.setAttribute(SpagoBIConstants.ACTIVITYKEY, activityKey);
			
			// AUDIT UPDATE
			if (contextInstance != null) {
				Object auditIdObj = contextInstance.getVariable(AuditManager.AUDIT_ID);
				Integer auditId = convertIdType(auditIdObj);
				if(auditId!=null) {
					AuditManager auditManager = AuditManager.getInstance();
					auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), 
							"EXECUTION_PERFORMED", null, null);
				}
			}
			
		} catch(Exception e){
			logger.error("Error while versioning presentation", e);
			// AUDIT UPDATE
			if (contextInstance != null) {
				Object auditIdObj = contextInstance.getVariable(AuditManager.AUDIT_ID);
				Integer auditId = convertIdType(auditIdObj);
				if(auditId!=null) {
					AuditManager auditManager = AuditManager.getInstance();
					auditManager.updateAudit(auditId, null, new Long(System.currentTimeMillis()), 
							"EXECUTION_FAILED", e.getMessage(), null);
				}
			}
		} finally {
	    	if (jbpmContext != null) {
	    		jbpmContext.close();
	    	}
	    	logger.debug("OUT");
		}
	    
	}
	
	
	private Integer convertIdType(Object auditIdObj) {
		Integer auditId = null;
		if(auditIdObj instanceof Long) {
			Long auditLong = (Long)auditIdObj;
			auditId = new Integer(auditLong.intValue());
		} else if (auditIdObj instanceof Integer) {
			auditId = (Integer)auditIdObj;
		} else {
			try {
				auditId = new Integer(auditIdObj.toString());
			} catch (Exception e ) {
				logger.error("Error while converting audit id type, " +
						            "the audit log row will not be recorded", e);
			}
		}
		return auditId;
	}
	
	
	
	private void openNoteEditorHandler(SourceBean request, SourceBean response) {
		JbpmContext jbpmContext = null;
		logger.debug("IN");
		try {
			// recover task instance and variables
			String activityKey = (String)request.getAttribute("activityKey");
			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
			jbpmContext = jbpmConfiguration.createJbpmContext();
			TaskInstance taskInstance = jbpmContext.getTaskInstance(new Long(activityKey).longValue());
			String index = (String)taskInstance.getVariable(DossierConstants.DOSSIER_PART_INDEX);
			int pageNum = Integer.parseInt(index);
			ContextInstance contextInstance = taskInstance.getContextInstance();
			ProcessInstance processInstance = contextInstance.getProcessInstance();
			Long workflowProcessId = new Long(processInstance.getId());
			String dossierIdStr = (String)contextInstance.getVariable(DossierConstants.DOSSIER_ID);
			Integer dossierId = new Integer(dossierIdStr);
			
			// recovers images and notes
		    String notes = recoverNotes(dossierId, pageNum, workflowProcessId);
		    Map imageurl = recoverImageUrls(dossierId, pageNum, workflowProcessId);
		    
			// put attributes into response
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierEditNotesTemplatePart");
			response.setAttribute(DossierConstants.DOSSIER_PART_INDEX, index);
			response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
			response.setAttribute(SpagoBIConstants.ACTIVITYKEY, activityKey);
			response.setAttribute("mapImageUrls", imageurl);
			response.setAttribute("notes", notes);
			
		} catch(Exception e){
			logger.error("Error while saving notes", e);
		} finally {
	    	if (jbpmContext != null) {
	    		jbpmContext.close();
	    	}
	    	logger.debug("OUT");
		}
	    
	}
	
	
	
	private String recoverNotes(Integer dossierId, int pageNum, Long workflowProcessId) throws Exception {
		IDossierPartsTempDAO dptDAO = DAOFactory.getDossierPartsTempDAO();
	    byte[] notesByte = 	dptDAO.getNotesOfDossierPart(dossierId, pageNum, workflowProcessId);
	    String notes = notesByte != null ? new String(notesByte) : "";
	    return notes;
	}
	
	
	
	private Map recoverImageUrls(Integer dossierId, int pageNum, Long workflowProcessId) throws Exception {
		IDossierPartsTempDAO dptDAO = DAOFactory.getDossierPartsTempDAO();
		Map images = dptDAO.getImagesOfDossierPart(dossierId, pageNum, workflowProcessId);
		File tempDir = DossierDAOHibImpl.tempBaseFolder; 
		tempDir.mkdirs();
		// for each image store into the temp directory and save the url useful to recover it into the map
	    Map imageurl = new HashMap();
	    Iterator iterImgs = images.keySet().iterator();
	    while(iterImgs.hasNext()){
	    	String logicalName = (String) iterImgs.next();
	    	//String logicalNameForStoring = pathConfBook.replace('/', '_') + logicalName + ".jpg";
			UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGenerator.generateTimeBasedUUID();
			String uuid = uuidObj.toString();
			// TODO perche salvare su file system? tanto vale tenere in memoria le immagini, oppure scriverli sul file system e far puntare l'immagine al file
	    	String logicalNameForStoring = uuid + logicalName;
	    	byte[] content = (byte[])images.get(logicalName);
	    	File img = new File(tempDir, logicalNameForStoring + ".jpg");
	    	FileOutputStream fos = new FileOutputStream(img);
	    	fos.write(content);
	    	fos.flush();
	    	fos.close();
	    	// the url to recover the image is a spagobi servlet url
	    	
	    	String recoverUrl = DossierUtilities.getDossierServiceUrl() + "&" + 
	    						DossierConstants.DOSSIER_SERVICE_TASK + "=" +
	    						DossierConstants.DOSSIER_SERVICE_TASK_GET_TEMPLATE_IMAGE + "&" +
	    						DossierConstants.DOSSIER_SERVICE_IMAGE + "=" +
	    						logicalNameForStoring;
	    	imageurl.put(logicalName, recoverUrl); 
	    }
	   return imageurl;
	}
	
			
	
	private void saveNoteHandler(SourceBean request, SourceBean response) {
		JbpmContext jbpmContext = null;
		try {
			String indexPart = (String)request.getAttribute(DossierConstants.DOSSIER_PART_INDEX);
			int pageNum = Integer.parseInt(indexPart);
			String dossierIdStr = (String)request.getAttribute(DossierConstants.DOSSIER_ID);
			Integer dossierId = new Integer(dossierIdStr);
			String noteSent = (String)request.getAttribute("notes");
			String activityKey = (String)request.getAttribute(SpagoBIConstants.ACTIVITYKEY);
			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
			jbpmContext = jbpmConfiguration.createJbpmContext();
			TaskInstance taskInstance = jbpmContext.getTaskInstance(new Long(activityKey).longValue());
			ContextInstance contextInstance = taskInstance.getContextInstance();
			ProcessInstance processInstance = contextInstance.getProcessInstance();
			Long workflowProcessId = new Long(processInstance.getId());
			SessionContainer session = this.getRequestContainer().getSessionContainer();
			SessionContainer permanentSession = session.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			IDossierPartsTempDAO dptDAO = DAOFactory.getDossierPartsTempDAO();
			dptDAO.setUserProfile(profile);
			dptDAO.storeNote(dossierId, pageNum, noteSent.getBytes(), workflowProcessId);
			// recover from cms images and notes
		    String notes = recoverNotes(dossierId, pageNum, workflowProcessId);
		    Map imageurl = recoverImageUrls(dossierId, pageNum, workflowProcessId);
			// put attributes into response
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierEditNotesTemplatePart");
			response.setAttribute(DossierConstants.DOSSIER_PART_INDEX, indexPart);
			response.setAttribute(DossierConstants.DOSSIER_ID, dossierIdStr);
			response.setAttribute(SpagoBIConstants.ACTIVITYKEY, activityKey);
			response.setAttribute("mapImageUrls", imageurl);
			response.setAttribute("notes", notes);
			
		} catch(Exception e){
			logger.error("Error while saving notes", e);
		} finally {
	    	if (jbpmContext != null) {
	    		jbpmContext.close();
	    	}
		}
	}

}
