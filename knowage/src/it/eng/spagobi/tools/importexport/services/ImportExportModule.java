/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.importexport.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UploadedFile;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.tools.importexport.ExportUtilities;
import it.eng.spagobi.tools.importexport.IExportManager;
import it.eng.spagobi.tools.importexport.IImportManager;
import it.eng.spagobi.tools.importexport.ImportExportConstants;
import it.eng.spagobi.tools.importexport.ImportManager;
import it.eng.spagobi.tools.importexport.ImportResultInfo;
import it.eng.spagobi.tools.importexport.ImportUtilities;
import it.eng.spagobi.tools.importexport.ImporterMetadata;
import it.eng.spagobi.tools.importexport.MetadataAssociations;
import it.eng.spagobi.tools.importexport.TransformManager;
import it.eng.spagobi.tools.importexport.UserAssociationsKeeper;
import it.eng.spagobi.tools.importexport.bo.AssociationFile;
import it.eng.spagobi.tools.importexport.dao.AssociationFileDAO;
import it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

/**
 * This class implements a module which handles the import / export operations
 */
public class ImportExportModule extends AbstractHttpModule {

    static private Logger logger = Logger.getLogger(ImportExportModule.class);

    /**
     * Initialize the module.
     * 
     * @param config Configuration sourcebean of the module
     */
    public void init(SourceBean config) {
    }

    /**
     * Reads the operation asked by the user and calls the export or import
     * methods.
     * 
     * @param request The Source Bean containing all request parameters
     * @param response The Source Bean containing all response parameters
     * 
     * @throws exception If an exception occurs
     * @throws Exception the exception
     */
    public void service(SourceBean request, SourceBean response) throws Exception {
	logger.debug("IN");

	String message = (String) request.getAttribute("MESSAGEDET");
	message.toLowerCase();
	logger.debug("begin of import / export service with message =" + message);
	EMFErrorHandler errorHandler = getErrorHandler();
	try {
	    if (message == null) {
		EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, "101", ImportManager.messageBundle);
		logger.warn("The message parameter is null");
		errorHandler.addError(userError);
		throw userError;
	    }
	    if (message.trim().equalsIgnoreCase(ImportExportConstants.EXPORT)) {
		exportConf(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPORT)) {
		importConf(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_ROLE_ASSOCIATION)) {
		associateRoles(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_ENGINE_ASSOCIATION)) {
		associateEngines(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_DATA_SOURCE_ASSOCIATION)) {
		associateDataSources(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_METADATA_ASS)) {
		associateMetadata(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_EXIT)) {
		exitImport(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_BACK_ENGINE_ASS)) {
		backEngineAssociation(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_BACK_DS_ASS)) {
	    backDataSourceAssociation(request, response);
	    } else if (message.trim().equalsIgnoreCase(ImportExportConstants.IMPEXP_BACK_METADATA_ASS)) {
		backMetadataAssociation(request, response);
	    }
	} catch (EMFUserError emfu) {
		logger.error("Error during the service execution", emfu);
	    errorHandler.addError(emfu);
	} catch (Exception ex) {
	    logger.error("Error during the service execution", ex);
	    EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, "100", ImportManager.messageBundle);
	    errorHandler.addError(error);
	    return;
	} finally {
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to export some selected objects
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void exportConf(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	HashMap<String, String> logParam = new HashMap();
	IExportManager expManager = null;
	String exportFileName = (String) request.getAttribute("exportFileName");
	logParam.put("ExportFileName", exportFileName);

	SessionContainer permSess = getRequestContainer().getSessionContainer().getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	
	if ((exportFileName == null) || (exportFileName.trim().equals(""))) {
	    logger.error("Missing name of the exported file");
	    throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportFileName", "8006", ImportManager.messageBundle);

	}
	try {
	    String exportSubObject = (String) request.getAttribute("exportSubObj");
	    boolean expSubObj = false;
	    if (exportSubObject != null) {
		expSubObj = true;
	    }
	    String exportSnapshots = (String) request.getAttribute("exportSnapshots");
	    boolean exportSnaps = false;
	    if (exportSnapshots != null) {
		exportSnaps = true;
	    }
//	    String exportResourcesStr = (String) request.getAttribute("exportResources");
//	    boolean exportResources = false;
//	    if (exportResourcesStr != null) {
//	    	exportResources = true;
//	    }
	    
	    String pathExportFolder = ExportUtilities.getExportTempFolderPath();
	    String idListStr = (String) request.getAttribute(ImportExportConstants.OBJECT_ID);
	    String[] idListArray = idListStr.split(";");
	    List ids = Arrays.asList(idListArray);
	    expManager = ExportUtilities.getExportManagerInstance();
	    expManager.prepareExport(pathExportFolder, exportFileName, expSubObj, exportSnaps);
	    expManager.exportObjects(ids);
//	    if (exportResources) {
//	    	logger.error("Exporting resources");
//	    	expManager.exportResources();
//	    }
	    expManager.createExportArchive();
	    response.setAttribute(ImportExportConstants.EXPORT_FILE_PATH, exportFileName);
	} catch (EMFUserError emfue) {
	    logger.error("Error while exporting ", emfue);
	    expManager.cleanExportEnvironment();
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "EXPORT",logParam , "ERR");
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	    throw emfue;
	} catch (Exception e) {
	    expManager.cleanExportEnvironment();
	    logger.error("Error while exporting ", e);
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "EXPORT",logParam , "ERR");
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
	} finally {
	    logger.debug("OUT");
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "EXPORT",logParam , "OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    }

    /*
    private List extractObjId(List requests) {
	logger.debug("IN");
	List toReturn = new ArrayList();
	Iterator iter = requests.iterator();
	while (iter.hasNext()) {
	    String id_path = (String) iter.next();
	    String id = id_path.substring(0, id_path.indexOf('_'));
	    toReturn.add(id);
	}
	logger.debug("OUT");
	return toReturn;
    }
    */

    /**
     * Manages the request of the user to import contents of an exported archive
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void importConf(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	HashMap<String, String> logParam = new HashMap();
	
	SessionContainer permanentSession = this.getRequestContainer().getSessionContainer().getPermanentContainer();
	IEngUserProfile profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	
	IImportManager impManager = null;
	// get exported file and eventually the associations file

	
	FileItem archive = null;
	FileItem associationsFileItem = null;
	UploadedFile associationsFile = null;
	AssociationFile assFile = null;
	try {
		String assKindFromReq = (String) request.getAttribute("importAssociationKind");
		boolean isNoAssociationModality = assKindFromReq != null && assKindFromReq.equalsIgnoreCase("noassociations");
		List uplFiles = request.getAttributeAsList("UPLOADED_FILE");
		if(uplFiles != null) {
			logger.debug("Uploded files [" + uplFiles.size() + "]");
		}
		Iterator uplFilesIter = uplFiles.iterator();
		while (uplFilesIter.hasNext()) {
			FileItem uplFile = (FileItem) uplFilesIter.next();
		   
			logger.debug("Uploded file name [" + uplFile + "]");
			logParam.put("ImportFileName", uplFile.getName());
			
			String nameInForm = uplFile.getFieldName();
		    if (nameInForm.equals("exportedArchive")) {
		    	archive = uplFile;
		    } else if (nameInForm.equals("associationsFile")) {
		    	associationsFileItem = uplFile;
		    }
		}
		// check that the name of the uploaded archive is not empty
		//String archiveName = archive.getFileName();
		String archiveName = GeneralUtilities.getRelativeFileNames(archive.getName());
		logger.debug("Archive file name [" + archiveName + "]");
		if (archiveName.trim().equals("")) {
			logger.error("Missing exported file");
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportLoopbackStopImport");
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "KO");
			} catch (Exception e) {
				e.printStackTrace();
			}
		    throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportedArchive", "8007", ImportManager.messageBundle);
		}
		
		int maxSize = ImportUtilities.getImportFileMaxSize();
		if (archive.getSize() > maxSize) {
		    logger.error("File is too large!!!");
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportLoopbackStopImport");
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "KO");
			} catch (Exception e) {
				e.printStackTrace();
			}			
		    throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportedArchive", "202");
		}
		
		// checks if the association file is bigger than 1 MB, that is more than enough!!
		if (associationsFileItem != null) {
			if (associationsFileItem.getSize() > 1048576) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "KO");
				} catch (Exception e) {
					e.printStackTrace();
				}				
				throw new EMFValidationError(EMFErrorSeverity.ERROR, "associationsFile", "202");
			}
			// loads association file
			associationsFile = new UploadedFile();
			associationsFile.setFileContent(associationsFileItem.get());
			associationsFile.setFieldNameInForm(associationsFileItem.getFieldName());
			associationsFile.setSizeInBytes(associationsFileItem.getSize());
			associationsFile.setFileName(GeneralUtilities.getRelativeFileNames(associationsFileItem.getName()));
		}
		
		// if the user choose to have no associations, checks the form, otherwise set the variable associationsFile = null
		if (!isNoAssociationModality) {
			// check if the name of associations file is empty (in this case set
			// null to the variable)
			if (associationsFile != null) {
				String associationsFileName = associationsFile.getFileName();
				if (associationsFileName.trim().equals("")) {
				    associationsFile = null;
				}
			}
			// if the association file is empty then check if there is an
			// association id
			// rebuild the uploaded file and assign it to associationsFile variable
			if (associationsFile == null) {
			    String assId = (String) request.getAttribute("hidAssId");
			    if ((assId != null) && !assId.trim().equals("")) {
				IAssociationFileDAO assfiledao = new AssociationFileDAO();
				assFile = assfiledao.loadFromID(assId);
				byte[] content = assfiledao.getContent(assFile);
				UploadedFile uplFile = new UploadedFile();
				uplFile.setSizeInBytes(content.length);
				uplFile.setFileContent(content);
				uplFile.setFileName("association.xml");
				uplFile.setFieldNameInForm("");
				associationsFile = uplFile;
			    }
			}
		} else {
			associationsFile = null;
		}
		
		// get the association mode
		String assMode = IImportManager.IMPORT_ASS_DEFAULT_MODE;
		if (assKindFromReq.equalsIgnoreCase("predefinedassociations")) {
		    assMode = IImportManager.IMPORT_ASS_PREDEFINED_MODE;
		}
		// get bytes of the archive
		byte[] archiveBytes = archive.get();
	
	    // get path of the import tmp directory
	    String pathImpTmpFolder = ImportUtilities.getImportTempFolderPath();
	   
	    // apply transformation
	    TransformManager transManager = new TransformManager();
	    archiveBytes = transManager.applyTransformations(archiveBytes, archiveName, pathImpTmpFolder);
	    logger.debug("Transformation applied succesfully");

	    // prepare import environment

		impManager = ImportUtilities.getImportManagerInstance();
	    impManager.setUserProfile(profile);
	    impManager.init(pathImpTmpFolder, archiveName, archiveBytes);
	    impManager.openSession();
	    impManager.setAssociationFile(assFile);
	    
		// if the associations file has been uploaded fill the association keeper
		if(associationsFile!=null) {
			byte[] assFilebys = associationsFile.getFileContent();
			String assFileStr = new String(assFilebys);
			try {
				impManager.getUserAssociation().fillFromXml(assFileStr);
			} catch (Exception e) {
				logger.error("Error while loading association file content:\n " + e);
				response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportLoopbackStopImport");
				throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportedArchive", "8009", ImportManager.messageBundle);
			}
		}

	    // set into import manager the association import mode
	    impManager.setImpAssMode(assMode);

		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		session.setAttribute(ImportExportConstants.IMPORT_MANAGER, impManager);
		
		// start import operations
		if (impManager.getImpAssMode().equals(IImportManager.IMPORT_ASS_PREDEFINED_MODE) && !impManager.associateAllExportedRolesByUserAssociation()) {
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportSkipRoleAssociation");
		} else {
			// move to jsp
			List exportedRoles = impManager.getExportedRoles();
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			List currentRoles = roleDAO.loadAllRoles();
			response.setAttribute(ImportExportConstants.LIST_EXPORTED_ROLES, exportedRoles);
			response.setAttribute(ImportExportConstants.LIST_CURRENT_ROLES, currentRoles);
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportRoleAssociation");
		}
	} catch (EMFUserError emfue) {
	    logger.error("Error inr etrieving import configuration ", emfue);
	    if (impManager != null) {
	    	impManager.stopImport();
	    }
	    throw emfue;
	} catch (ClassNotFoundException cnde) {
	    logger.error("Importer class not found", cnde);
	    if (impManager != null)
		impManager.stopImport();
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "ERR");
		} catch (Exception e) {
			e.printStackTrace();
		}
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (InstantiationException ie) {
	    logger.error("Cannot create an instance of importer class ", ie);
	    if (impManager != null)
		impManager.stopImport();
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "ERR");
		} catch (Exception e) {
			e.printStackTrace();
		}	    
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (IllegalAccessException iae) {
	    logger.error("Cannot create an instance of importer class ", iae);
	    if (impManager != null)
		impManager.stopImport();
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "ERR");
		} catch (Exception e) {
			e.printStackTrace();
		}	    
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (SourceBeanException sbe) {
		logger.error("Error: " + sbe);
	    if (impManager != null)
			impManager.stopImport();
		throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (Exception e) {
		logger.error("An unexpected error occured while performing import", e);
	    if (impManager != null) {
			impManager.stopImport();
	    }
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "ERR");
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
	    if (impManager != null)
			impManager.closeSession();
	    logger.debug("OUT");
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "IMPORT",logParam , "OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    }

    /**
     * Manages the request of the user to associate some exported roles to the
     * roles of the portal in use
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void associateRoles(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
	    RequestContainer requestContainer = this.getRequestContainer();
	    SessionContainer session = requestContainer.getSessionContainer();
	    impManager = (IImportManager) session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
	    impManager.openSession();
	    MetadataAssociations metaAss = impManager.getMetadataAssociation();
	    if (!request.containsAttribute("ROLES_ASSOCIATIONS_SKIPPED")) {
			// the roles associations form was submitted
			List expRoleIds = request.getAttributeAsList("expRole");
			Iterator iterExpRoles = expRoleIds.iterator();
			while(iterExpRoles.hasNext()){
				String expRoleId = (String)iterExpRoles.next();
				String roleAssociateId = (String)request.getAttribute("roleAssociated"+expRoleId);
				if(!roleAssociateId.trim().equals("")) {
					metaAss.insertCoupleRole(new Integer(expRoleId), new Integer(roleAssociateId));
					// insert into user associations
					try{
						Object existingRoleObj = impManager.getExistingObject(new Integer(roleAssociateId), SbiExtRoles.class); 
						Object exportedRoleObj = impManager.getExportedObject(new Integer(expRoleId), SbiExtRoles.class);
						if( (existingRoleObj!=null) && (exportedRoleObj!=null) ) {
							SbiExtRoles existingRole = (SbiExtRoles)existingRoleObj;
							SbiExtRoles exportedRole = (SbiExtRoles)exportedRoleObj;
							UserAssociationsKeeper usrAssKeep = impManager.getUserAssociation();
							String expRoleName = exportedRole.getName();
							String exiRoleName = existingRole.getName();
							usrAssKeep.recordRoleAssociation(expRoleName, exiRoleName);
						} else {
							throw new Exception("hibernate object of existing or exported role not recovered");
						}
					} catch (Exception e) {
						logger.error("Error while recording user role association", e);
					}
				}
			}
	    }
	    // check role associations
	    impManager.checkRoleReferences(metaAss.getRoleIDAssociation());
		
	    if (impManager.getImpAssMode().equals(IImportManager.IMPORT_ASS_PREDEFINED_MODE) && !impManager.associateAllExportedEnginesByUserAssociation()) {
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportSkipEngineAssociation");
		} else {
			// get the existing and exported engines
			// move to jsp
            List exportedEngines = impManager.getExportedEngines();
			IEngineDAO engineDAO = DAOFactory.getEngineDAO();
			List currentEngines = engineDAO.loadAllEngines();
			response.setAttribute(ImportExportConstants.LIST_EXPORTED_ENGINES, exportedEngines);
			response.setAttribute(ImportExportConstants.LIST_CURRENT_ENGINES, currentEngines);
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportEngineAssociation");
		}
	} catch (EMFUserError emfue) {
		logger.error("Error in associating roles: " + emfue);
	    if (impManager != null)
		impManager.stopImport();
	    throw emfue;
	} catch (SourceBeanException sbe) {
		logger.error("Error: " + sbe);
	    if (impManager != null)
			impManager.stopImport();
		throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (Exception e) {
	    logger.error("Error while getting role association ", e);
	    if (impManager != null)
		impManager.stopImport();
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
	    if (impManager != null)
			impManager.closeSession();
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to associate some exported engines to the
     * engines of the portal in use
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void associateEngines(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		impManager = (IImportManager)session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
		impManager.openSession();
		MetadataAssociations metaAss = impManager.getMetadataAssociation();
		if (!request.containsAttribute("ENGINES_ASSOCIATIONS_SKIPPED")) {
			List expEngineIds = request.getAttributeAsList("expEngine");
			Iterator iterExpEngines = expEngineIds.iterator();
			while(iterExpEngines.hasNext()){
				String expEngineId = (String)iterExpEngines.next();
				String engineAssociateId = (String)request.getAttribute("engineAssociated"+expEngineId);
				if(!engineAssociateId.trim().equals("")) {
					metaAss.insertCoupleEngine(new Integer(expEngineId), new Integer(engineAssociateId));
					// insert into user associations
					try{
						Object existingEngineObj = impManager.getExistingObject(new Integer(engineAssociateId), SbiEngines.class); 
						Object exportedEngineObj = impManager.getExportedObject(new Integer(expEngineId), SbiEngines.class);
						if( (existingEngineObj!=null) && (exportedEngineObj!=null) ) {
							SbiEngines existingEngine = (SbiEngines)existingEngineObj;
							SbiEngines exportedEngine = (SbiEngines)exportedEngineObj;
							impManager.getUserAssociation().recordEngineAssociation(exportedEngine.getLabel(), existingEngine.getLabel());
						} else {
							throw new Exception("hibernate object of existing or exported engine not recovered");
						}
					} catch (Exception e) {
						logger.warn("Error while recording user engine association", e);
					}
				}
			}
		}
		
		if (impManager.getImpAssMode().equals(IImportManager.IMPORT_ASS_PREDEFINED_MODE) && !impManager.associateAllExportedDataSourcesByUserAssociation()) {
		    impManager.checkExistingMetadata();
		    if (metaAss.isEmpty()) {
		    	response.setAttribute(ImportExportConstants.PUBLISHER_NAME,
			    	"ImportExportSkipExistingMetadataAssociation");
		    } else {
			    response.setAttribute(ImportExportConstants.PUBLISHER_NAME,
				    "ImportExportExistingMetadataAssociation");
		    }
		} else {
			// move to jsp
			List exportedDatasources = impManager.getExportedDataSources();
			IDataSourceDAO dsDao=DAOFactory.getDataSourceDAO();
			List currentDatasources = dsDao.loadAllDataSources();
			response.setAttribute(ImportExportConstants.LIST_EXPORTED_DATA_SOURCES, exportedDatasources);
			response.setAttribute(ImportExportConstants.LIST_CURRENT_DATA_SOURCES, currentDatasources);
			response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportDataSourceAssociation");
		}

	} catch (EMFUserError emfue) {
		logger.error("Error in associating engines: " + emfue);
	    if (impManager != null)
		impManager.stopImport();
	    throw emfue;
	} catch (SourceBeanException sbe) {
		logger.error("Error: " + sbe);
	    if (impManager != null)
			impManager.stopImport();
		throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (Exception e) {
	    logger.error("Error while getting engine association ", e);
	    if (impManager != null)
		impManager.stopImport();
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
	    if (impManager != null)
			impManager.closeSession();
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to associate some exported data sources to
     * the data sources of the portal in use
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void associateDataSources(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		impManager = (IImportManager)session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
		impManager.openSession();
		MetadataAssociations metaAss = impManager.getMetadataAssociation();
		List expDsIds = request.getAttributeAsList("expConn");
		Iterator iterExpConn = expDsIds.iterator();
		while(iterExpConn.hasNext()){
			String expDsIdStr= (String)iterExpConn.next();
			String assDsIds = (String)request.getAttribute("connAssociated"+ expDsIdStr);
			if(!assDsIds.equals("")) {
				metaAss.insertCoupleDataSources(new Integer(expDsIdStr), new Integer(assDsIds));
				Object existingDSObj = impManager.getExistingObject(new Integer(assDsIds), SbiDataSource.class); 
				Object exportedDSObj = impManager.getExportedObject(new Integer(expDsIdStr), SbiDataSource.class);
				if (existingDSObj != null && exportedDSObj != null) {
					SbiDataSource existingDataSource = (SbiDataSource)existingDSObj;
					SbiDataSource exportedDataSource = (SbiDataSource)exportedDSObj;
					impManager.getUserAssociation().recordDataSourceAssociation(exportedDataSource.getLabel(), existingDataSource.getLabel());
					impManager.getUserAssociation().recordDataSourceAssociation(Integer.valueOf(exportedDataSource.getDsId()), Integer.valueOf(existingDataSource.getDsId()));
				}
			} 
//			else {
//				logger.error("Exported data source " +expConnName+" is not associated to a current " +
//                        			"system data source");
//				List exportedDataSources = impManager.getExportedDataSources();
//				Map currentDataSources = getCurrentDataSourcesInfo();
//				response.setAttribute(ImportExportConstants.LIST_EXPORTED_DATA_SOURCES, exportedDataSources);
//				response.setAttribute(ImportExportConstants.MAP_CURRENT_DATA_SOURCES, currentDataSources);
//				response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportDataSourceAssociation");
//				throw new EMFValidationError(EMFErrorSeverity.ERROR, "connAssociated"+ expConnName, "sbi.impexp.datasourceNotAssociated");
//			}
		}
		
	    impManager.checkExistingMetadata();
	    if (metaAss.isEmpty()) {
	    	response.setAttribute(ImportExportConstants.PUBLISHER_NAME,
		    	"ImportExportSkipExistingMetadataAssociation");
	    } else {
		    response.setAttribute(ImportExportConstants.PUBLISHER_NAME,
			    "ImportExportExistingMetadataAssociation");
	    }
	} catch (EMFValidationError emfve) {
		logger.error("Error ina ssocia6ting daytsource: " + emfve);
	    throw emfve;
	} catch (EMFUserError emfue) {
		logger.error("Error in associating engines: " + emfue);
	    if (impManager != null)
		impManager.stopImport();
	    throw emfue;
	} catch (SourceBeanException sbe) {
	    logger.error("Cannot populate response ", sbe);
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (Exception e) {
	    logger.error("Error while getting  association ", e);
	    if (impManager != null)
		impManager.stopImport();
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8003", ImportManager.messageBundle);
	} finally {
	    if (impManager != null)
			impManager.closeSession();
	    logger.debug("OUT");
	}

    }

    /**
     * Manages the associations between the exported metadata and the one of the
     * portal in use
     * 
     * @param request  Spago SourceBean request
     * @param response Spago SourceBean response
     * @throws EMFUserError
     */
    private void associateMetadata(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
		String overwriteStr = (String) request.getAttribute("overwrite");
		boolean overwrite = false;
		try {
			overwrite = Boolean.parseBoolean(overwriteStr);
		} catch (Exception e) {
			logger.warn("Overwrite parameter is not a valid boolean; default value (that is false) will be considered.");
		}
	    RequestContainer requestContainer = this.getRequestContainer();
	    SessionContainer session = requestContainer.getSessionContainer();
	    impManager = (IImportManager) session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
	    impManager.openSession();
	    impManager.importObjects(overwrite);
	    ImportResultInfo iri = impManager.commitAllChanges();
	    response.setAttribute(ImportExportConstants.IMPORT_RESULT_INFO, iri);
		AssociationFile assFile = impManager.getAssociationFile();
		if (assFile != null) response.setAttribute(ImportExportConstants.IMPORT_ASSOCIATION_FILE, assFile);
	} catch (EMFUserError emfue) {
		logger.error("Error in associating engines: " + emfue);
	    if (impManager != null)
		impManager.stopImport();
	    throw emfue;
	} catch (SourceBeanException sbe) {
	    logger.error("Cannot populate response ", sbe);
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} catch (Exception e) {
	    if (impManager != null)
		impManager.stopImport();
	    logger.error("error after data source association ", e);
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
	    if (impManager != null)
			impManager.closeSession();
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to exit from the import procedure
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void exitImport(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	RequestContainer requestContainer = this.getRequestContainer();
	SessionContainer session = requestContainer.getSessionContainer();
	IImportManager impManager = (IImportManager) session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
	impManager.stopImport();
	try {
	    response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportLoopbackStopImport");
	} catch (SourceBeanException sbe) {
	    logger.error("Error while populating response source bean ", sbe);
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to go back from the engines association
     * to the roles association
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void backEngineAssociation(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		impManager = (IImportManager) session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
		impManager.openSession();
		List exportedRoles = impManager.getExportedRoles();
		IRoleDAO roleDAO = DAOFactory.getRoleDAO();
		List currentRoles = roleDAO.loadAllRoles();
	    response.setAttribute(ImportExportConstants.LIST_EXPORTED_ROLES, exportedRoles);
	    response.setAttribute(ImportExportConstants.LIST_CURRENT_ROLES, currentRoles);
	    response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportRoleAssociation");
	} catch (SourceBeanException sbe) {
	    logger.error("Error while populating response source bean ", sbe);
	    impManager.stopImport();
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
		if (impManager != null) 
			impManager.closeSession();
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to go back from the data sources
     * association to the engines association
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void backDataSourceAssociation(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		impManager = (IImportManager) session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
		impManager.openSession();
		List exportedEngines = impManager.getExportedEngines();
		IEngineDAO engineDAO = DAOFactory.getEngineDAO();
		List currentEngines = engineDAO.loadAllEngines();
	    response.setAttribute(ImportExportConstants.LIST_EXPORTED_ENGINES, exportedEngines);
	    response.setAttribute(ImportExportConstants.LIST_CURRENT_ENGINES, currentEngines);
	    response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportEngineAssociation");
	} catch (SourceBeanException sbe) {
	    logger.error("Error while populating response source bean ", sbe);
	    impManager.stopImport();
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
		if (impManager != null) 
			impManager.closeSession();
	    logger.debug("OUT");
	}
    }

    /**
     * Manages the request of the user to go back from the metadata association
     * to the data sources association
     * 
     * @param request
     *                Spago SourceBean request
     * @param response
     *                Spago SourceBean response
     * @throws EMFUserError
     */
    private void backMetadataAssociation(SourceBean request, SourceBean response) throws EMFUserError {
	logger.debug("IN");
	IImportManager impManager = null;
	try {
		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		impManager = (IImportManager) session.getAttribute(ImportExportConstants.IMPORT_MANAGER);
		impManager.openSession();
		List exportedDataSources = impManager.getExportedDataSources();
		IDataSourceDAO dsDao=DAOFactory.getDataSourceDAO();
		List currentDatasources = dsDao.loadAllDataSources();
	    response.setAttribute(ImportExportConstants.LIST_EXPORTED_DATA_SOURCES, exportedDataSources);
	    response.setAttribute(ImportExportConstants.LIST_CURRENT_DATA_SOURCES, currentDatasources);
	    response.setAttribute(ImportExportConstants.PUBLISHER_NAME, "ImportExportDataSourceAssociation");
	} catch (SourceBeanException sbe) {
	    logger.error("Error while populating response source bean ", sbe);
	    throw new EMFUserError(EMFErrorSeverity.ERROR, "8004", ImportManager.messageBundle);
	} finally {
		if (impManager != null) 
			impManager.closeSession();
	    logger.debug("OUT");
	}
    }

}
