/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.modules;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorCategory;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.engines.dossier.bo.ConfiguredBIDocument;
import it.eng.spagobi.engines.dossier.bo.WorkflowConfiguration;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.engines.dossier.dao.DossierDAOHibImpl;
import it.eng.spagobi.engines.dossier.dao.IDossierDAO;
import it.eng.spagobi.engines.dossier.utils.DossierAnalyticalDriversManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

/**
 * This class implements a module which  handles pamphlets management.
 */
public class DossierManagementModule extends AbstractModule {
	
	static private Logger logger = Logger.getLogger(DossierManagementModule.class);

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
		logger.debug("Begin of detail Engine modify/visualization service with operation =" +operation);
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		if (tempFolder == null) {
			tempFolder = initDossier(request, response);
			request.setAttribute(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolder);
		}
		response.setAttribute(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolder);
		try{
			if((operation==null)||(operation.trim().equals(""))) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, "100", "component_dossier_messages");
				logger.error("The operation parameter is null");
				throw userError;
			} else if(operation.equalsIgnoreCase(SpagoBIConstants.NEW_DOCUMENT_TEMPLATE)) {
				newTemplateHandler(request, response);
			} else if(operation.equalsIgnoreCase(SpagoBIConstants.EDIT_DOCUMENT_TEMPLATE)) {
				dossierDetailHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_NEW_CONFIGURED_DOCUMENT)) {
				newConfiguredDocumentHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_SAVE_CONFIGURED_DOCUMENT)) {
				saveConfiguredDocumentHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_DETAIL_DOSSIER)) {
				dossierDetailHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_DELETE_CONFIGURED_DOCUMENT)) {
				deleteConfiguredDocumentHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_DETAIL_CONFIGURED_DOCUMENT)) {
				detailConfiguredDocumentHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_SAVE_DETAIL_DOSSIER)) {
				saveDossierDetailHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_LOAD_PRESENTATION_TEMPLATE)) {
				loadPresentationTemplateHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_LOAD_PROCESS_DEFINITION_FILE)) {
				loadProcessDefinitionFileHandler(request, response);
			} else if (operation.equalsIgnoreCase(DossierConstants.OPERATION_EXIT_FROM_DETAIL)) {
				exitFromDetailHandler(request, response);
			}
		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			SpagoBITracer.major(DossierConstants.NAME_MODULE, this.getClass().getName(),
					            "service", "Error while processin request", ex);
			EMFUserError emfue = new EMFUserError(EMFErrorSeverity.ERROR, 100);
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		} finally {
			logger.debug("OUT");
		}
	}

	private void exitFromDetailHandler(SourceBean request, SourceBean response) throws EMFUserError, SourceBeanException {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		IDossierDAO dossierDao = DAOFactory.getDossierDAO();
		// cleans temp folder
		dossierDao.clean(tempFolder);
		// propagates dossier id (usefull to return to document main detail page if light navigator is disabled)
		Integer dossierId = dossierDao.getDossierId(tempFolder);
		response.setAttribute(ObjectsTreeConstants.OBJECT_ID, dossierId.toString());
		response.setAttribute(DossierConstants.PUBLISHER_NAME, "ExitFromDossierDetailLoop");
		logger.debug("OUT");
	}

	private String initDossier(SourceBean request, SourceBean response) throws EMFUserError, SourceBeanException {
		logger.debug("IN");
		String objIdStr = (String) request.getAttribute(SpagoBIConstants.OBJECT_ID);
		Integer objId = new Integer(objIdStr);
		BIObject dossier = DAOFactory.getBIObjectDAO().loadBIObjectById(objId);
		IDossierDAO dossierDao = DAOFactory.getDossierDAO();
		String tempFolder = dossierDao.init(dossier);
		logger.debug("OUT");
		return tempFolder;
	}

	private void loadProcessDefinitionFileHandler(SourceBean request,
			SourceBean response) throws SourceBeanException, EMFUserError {
		logger.debug("IN");
		try {
			String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
			IDossierDAO dossierDao = new DossierDAOHibImpl();
			FileItem upFile = (FileItem) request.getAttribute("UPLOADED_FILE");
			if (upFile != null) {
				String fileName = GeneralUtilities.getRelativeFileNames(upFile.getName());
				if (upFile.getSize() == 0) {
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "201");
					getErrorHandler().addError(error);
					return;
				}
				int maxSize = GeneralUtilities.getTemplateMaxSize();
				if (upFile.getSize() > maxSize) {
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "202");
					getErrorHandler().addError(error);
					return;
				}
				if (!fileName.toUpperCase().endsWith(".XML")) {
					List params = new ArrayList();
					params.add("xml");
					EMFUserError error = new EMFValidationError(EMFErrorSeverity.ERROR, "UPLOADED_FILE", "107", params, null, "component_dossier_messages");
					getErrorHandler().addError(error);
				} else {
					byte[] fileContent = upFile.get();
					dossierDao.storeProcessDefinitionFile(fileName, fileContent, tempFolder);
				}
			} else {
				logger.warn("Upload file was null!!!");
			}
			
			
//			UploadedFile upFile = (UploadedFile) request.getAttribute("UPLOADED_FILE");
//			if (upFile != null) {
//				String fileName = upFile.getFileName();
//				if (!fileName.toUpperCase().endsWith(".XML")) {
//					List params = new ArrayList();
//					params.add("xml");
//					EMFUserError error = new EMFValidationError(EMFErrorSeverity.ERROR, "UPLOADED_FILE", "107", params, null, "component_dossier_messages");
//					getErrorHandler().addError(error);
//				} else {
//					byte[] fileContent = upFile.getFileContent();
//					dossierDao.storeProcessDefinitionFile(fileName, fileContent, tempFolder);
//				}
//			} else {
//				logger.warn("Upload file was null!!!");
//			}
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierLoopbackDossierDetail");
		} finally {
			logger.debug("OUT");
		}
	}

	private void loadPresentationTemplateHandler(SourceBean request,
			SourceBean response) throws SourceBeanException, EMFUserError {
		logger.debug("IN");
		try {
			String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
			IDossierDAO dossierDao = new DossierDAOHibImpl();
			FileItem upFile = (FileItem) request.getAttribute("UPLOADED_FILE");
			if (upFile != null) {
				String fileName = GeneralUtilities.getRelativeFileNames(upFile.getName());
				if (upFile.getSize() == 0) {
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "201");
					getErrorHandler().addError(error);
					return;
				}
				int maxSize = GeneralUtilities.getTemplateMaxSize();
				if (upFile.getSize() > maxSize) {
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "202");
					getErrorHandler().addError(error);
					return;
				}
				if (!fileName.toUpperCase().endsWith(".PPT")) {
					List params = new ArrayList();
					params.add("ppt");
					EMFUserError error = new EMFValidationError(EMFErrorSeverity.ERROR, "UPLOADED_FILE", "107", params, null, "component_dossier_messages");
					getErrorHandler().addError(error);
				} else {
					byte[] fileContent = upFile.get();
					dossierDao.storePresentationTemplateFile(fileName, fileContent, tempFolder);
				}
			} else {
				logger.warn("Upload file was null!!!");
			}
			
			
//			UploadedFile upFile = (UploadedFile) request.getAttribute("UPLOADED_FILE");
//			if (upFile != null) {
//				String fileName = upFile.getFileName();
//				if (!fileName.toUpperCase().endsWith(".PPT")) {
//					List params = new ArrayList();
//					params.add("ppt");
//					EMFUserError error = new EMFValidationError(EMFErrorSeverity.ERROR, "UPLOADED_FILE", "107", params, null, "component_dossier_messages");
//					getErrorHandler().addError(error);
//				} else {
//					byte[] fileContent = upFile.getFileContent();
//					dossierDao.storePresentationTemplateFile(fileName, fileContent, tempFolder);
//				}
//			} else {
//				logger.warn("Upload file was null!!!");
//			}
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierLoopbackDossierDetail");
		} finally {
			logger.debug("OUT");
		}
		
	}

	private void newTemplateHandler(SourceBean request, SourceBean response) throws SourceBeanException, EMFUserError {
		logger.debug("IN");
		String tempOOFileName = "";
		List confDoc = new ArrayList();
		WorkflowConfiguration workConf = new WorkflowConfiguration();
		List functionalities;
		try {
			functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
		} catch (EMFUserError e) {
			logger.error("Error while loading documents tree", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
        response.setAttribute(DossierConstants.CONFIGURED_DOCUMENT_LIST, confDoc);
		response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierDetail");
		response.setAttribute(DossierConstants.OO_TEMPLATE_FILENAME, tempOOFileName);
		response.setAttribute(DossierConstants.WORKFLOW_CONFIGURATION, workConf);
		logger.debug("OUT");
	}
	
	
	
	
	
	private void newConfiguredDocumentHandler(SourceBean request, SourceBean response) 
											  throws SourceBeanException, EMFUserError {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		Object objIdObj = request.getAttribute(DossierConstants.DOSSIER_CONFIGURED_BIOBJECT_ID);
		if(!(objIdObj instanceof String)){
			Map errBackPars = new HashMap();
			errBackPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
			errBackPars.put(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolder);
			errBackPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
			errBackPars.put(SpagoBIConstants.OPERATION, DossierConstants.OPERATION_DETAIL_DOSSIER);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "102", null, errBackPars, "component_dossier_messages");
		}
		String objIdStr = (String) objIdObj;
		Integer objId = new Integer(objIdStr);
		BIObject obj = null;
		List params = null;
		List roleList = null;
		try{
			IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
			obj = biobjdao.loadBIObjectById(objId);
			Integer id = obj.getId();
			IBIObjectParameterDAO biobjpardao = DAOFactory.getBIObjectParameterDAO();
			params = biobjpardao.loadBIObjectParametersById(id);
			IRoleDAO roleDao = DAOFactory.getRoleDAO();
			roleList = roleDao.loadAllRoles();
		} catch (Exception e) {
			SpagoBITracer.major(DossierConstants.NAME_MODULE, this.getClass().getName(),
                                 "newConfiguredDocumentHandler", 
                                 "Error while loading biobje parameters and roles", e);
		}
		Integer id = obj.getId();
		String descr = obj.getDescription();
		String label = obj.getLabel();
		String name = obj.getName();
		Iterator iterParams = params.iterator();
		HashMap parNamesMap = new HashMap();
		HashMap parValueMap = new HashMap();
		while(iterParams.hasNext()) {
			BIObjectParameter par = (BIObjectParameter)iterParams.next();
			String parLabel = par.getLabel();
			String parUrlName = par.getParameterUrlName();
			parNamesMap.put(parLabel, parUrlName);
			parValueMap.put(parUrlName, "");
		}
		response.setAttribute("parnamemap", parNamesMap);
		response.setAttribute("parvaluemap", parValueMap);
		response.setAttribute("idobj", id);
		response.setAttribute("description", descr);
		response.setAttribute("label", label);
		response.setAttribute("name", name);
		response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierConfiguredDocumentDetail"); 
		logger.debug("OUT");
	}
	
	
	
	
	private void saveConfiguredDocumentHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		String label = (String)request.getAttribute("biobject_label");
		// get logical name assigned to the configured document
		String logicalName = (String)request.getAttribute("logicalname");
		if( (logicalName==null) || logicalName.trim().equalsIgnoreCase("") ) {
			logicalName = "";
			//throw new EMFUserError(EMFErrorSeverity.ERROR, 103, "component_dossier_messages");
		}
		// load biobject using label
//		Integer id = new Integer(idobj);
		IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
		BIObject obj = biobjdao.loadBIObjectByLabel(label);
		IBIObjectParameterDAO biobjpardao = DAOFactory.getBIObjectParameterDAO();
		// gets parameters of the biobject
		List params = biobjpardao.loadBIObjectParametersById(obj.getId());
		Iterator iterParams = params.iterator();
		// get map of the param url name and value assigned
		boolean findOutFormat = false;
		Map paramValueMap = new HashMap();
		Map paramNameMap = new HashMap();
		while(iterParams.hasNext()) {
			BIObjectParameter par = (BIObjectParameter)iterParams.next();
			String parUrlName = par.getParameterUrlName();
			if(parUrlName.equalsIgnoreCase("outputType"))
				findOutFormat = true;
			String value = (String)request.getAttribute(parUrlName);
			paramValueMap.put(parUrlName, value);
			paramNameMap.put(par.getLabel(), par.getParameterUrlName());
		}
		if(!findOutFormat){
			paramValueMap.put("outputType", "JPGBASE64");
		}
		// fill a configured document bo with data retrived
		ConfiguredBIDocument confDoc = new ConfiguredBIDocument();
		confDoc.setDescription(obj.getDescription());
//		confDoc.setId(obj.getId());
		confDoc.setLabel(obj.getLabel());
		confDoc.setParameters(paramValueMap);
		confDoc.setName(obj.getName());
		confDoc.setLogicalName(logicalName);
		
		// check if the error handler contains validation errors
		EMFErrorHandler errorHandler = getResponseContainer().getErrorHandler();
		if(errorHandler.isOKByCategory(EMFErrorCategory.VALIDATION_ERROR)){
			// store the configured document
			IDossierDAO dossierDao = new DossierDAOHibImpl();
			dossierDao.addConfiguredDocument(confDoc, tempFolder);
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierLoopbackDossierDetail");
		} else {
			// set attribute into response
			response.setAttribute("parnamemap", paramNameMap);
			response.setAttribute("parvaluemap", paramValueMap);
//			response.setAttribute("idobj", confDoc.getId());
			response.setAttribute("description", confDoc.getDescription());
			response.setAttribute("label", confDoc.getLabel());
			response.setAttribute("name", confDoc.getName());
			response.setAttribute("logicalname", confDoc.getLogicalName());
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierConfiguredDocumentDetail"); 
		}
		logger.debug("OUT");
	}
	
	
	
	private void dossierDetailHandler(SourceBean request, SourceBean response) throws SourceBeanException, EMFUserError {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		IDossierDAO dossierDao = new DossierDAOHibImpl();
//		List roleList = null;
//		try{
//			IRoleDAO roleDao = DAOFactory.getRoleDAO();
//			roleList = roleDao.loadAllRoles();
//		} catch(Exception e) {
//			logger.error("Error while loading all roles", e);
//		}
		// get the current template file name
		String tempFileName = dossierDao.getPresentationTemplateFileName(tempFolder);
		if (tempFileName == null) tempFileName = "";
		// get list of the configured document
		List confDoc = dossierDao.getConfiguredDocumentList(tempFolder);
		// get the current process definition file name
		String procDefFileName = dossierDao.getProcessDefinitionFileName(tempFolder);
		if (procDefFileName == null) procDefFileName = "";
		//WorkflowConfiguration workConf = bookDao.getWorkflowConfiguration(pathConfBook);
		List functionalities;
		try {
			functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(true);
		} catch (EMFUserError e) {
			logger.error("Error while loading documents tree", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
        response.setAttribute(DossierConstants.CONFIGURED_DOCUMENT_LIST, confDoc);
		response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierDetail");
		response.setAttribute(DossierConstants.OO_TEMPLATE_FILENAME, tempFileName);
		response.setAttribute(DossierConstants.WF_PROCESS_DEFINTIION_FILENAME, procDefFileName);
		logger.debug("OUT");
	}
	
	
	private void deleteConfiguredDocumentHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		String confDocIdent = (String)request.getAttribute("configureddocumentidentifier");
		IDossierDAO dossierDao = new DossierDAOHibImpl();
		// delete the configured document
		dossierDao.deleteConfiguredDocument(confDocIdent, tempFolder);
		response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierLoopbackDossierDetail");
		logger.debug("OUT");
	}
	
	
	
	private void detailConfiguredDocumentHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		String confDocIdent = (String)request.getAttribute("configureddocumentidentifier");
		// get configured document
		IDossierDAO dossierDao = new DossierDAOHibImpl();
		ConfiguredBIDocument confDoc = dossierDao.getConfiguredDocument(confDocIdent, tempFolder);
		// get parameter value map
		Map paramValueMap = confDoc.getParameters();
		// create parameter name map
//		Integer idobj = confDoc.getId();
		String label = confDoc.getLabel();
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
		Integer idobj = obj.getId();
		IBIObjectParameterDAO biobjpardao = DAOFactory.getBIObjectParameterDAO();
		List params = biobjpardao.loadBIObjectParametersById(idobj);
		Iterator iterParams = params.iterator();
		Map paramNameMap = new HashMap();
		while(iterParams.hasNext()) {
			BIObjectParameter par = (BIObjectParameter)iterParams.next();
			String parLabel = par.getLabel();
			String parUrlName = par.getParameterUrlName();
			paramNameMap.put(parLabel, parUrlName);	
		}
		// set attribute into response
		response.setAttribute("parnamemap", paramNameMap);
		response.setAttribute("parvaluemap", paramValueMap);
//		response.setAttribute("idobj", confDoc.getId());
		response.setAttribute("description", confDoc.getDescription());
		response.setAttribute("label", confDoc.getLabel());
		response.setAttribute("name", confDoc.getName());
		response.setAttribute("logicalname", confDoc.getLogicalName());
		response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierConfiguredDocumentDetail"); 
		logger.debug("OUT");
	}
	
	
	
	private void saveDossierDetailHandler(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		String tempFolder = (String) request.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
		IDossierDAO dossierDao = new DossierDAOHibImpl();
		List docs = dossierDao.getConfiguredDocumentList(tempFolder);
		EMFErrorHandler errorHandler = getErrorHandler();
		if (dossierDao.getPresentationTemplateFileName(tempFolder) == null) {
			logger.error("Presentation template not loaded");
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "", "104", "component_dossier_messages");
			errorHandler.addError(error);
		}
		if (dossierDao.getProcessDefinitionFileName(tempFolder) == null) {
			logger.error("Process definition file not loaded");
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "", "105", "component_dossier_messages");
			errorHandler.addError(error);
		}
		if (docs == null || docs.size() == 0) {
			logger.error("No documents configured in dossier");
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "", "106", "component_dossier_messages");
			errorHandler.addError(error);
		}
		
		Integer dossierId = dossierDao.getDossierId(tempFolder);
		adjustRequiredAnalyticalDrivers(dossierId, docs);
		if (errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
			dossierDao.storeTemplate(dossierId, tempFolder);
		}
		
		String saveAndGoBackStr = (String) request.getAttribute("SAVE_AND_GO_BACK");
		boolean saveAndGoBack = saveAndGoBackStr != null && saveAndGoBackStr.trim().equalsIgnoreCase("TRUE");
		if (saveAndGoBack) {
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierSaveAndGoBackLoop");
		} else {
			response.setAttribute(DossierConstants.DOSSIER_SAVED_MSG_CODE_ATTR_NAME, "dossier.savedOk");
			response.setAttribute(DossierConstants.PUBLISHER_NAME, "DossierLoopbackDossierDetail");
		}
		
		logger.debug("OUT");
	}

	private void adjustRequiredAnalyticalDrivers(Integer dossierId, List docs) throws Exception {
		logger.debug("IN");
		DossierAnalyticalDriversManager manager = new DossierAnalyticalDriversManager();
		List<EMFValidationError> errors = manager.adjustRequiredAnalyticalDrivers(dossierId, docs);
		if (errors != null && errors.size() > 0) {
			Iterator<EMFValidationError> it = errors.iterator();
			while (it.hasNext()) {
				this.getErrorHandler().addError(it.next());
			}
		}
		
		logger.debug("OUT");
	}

}
