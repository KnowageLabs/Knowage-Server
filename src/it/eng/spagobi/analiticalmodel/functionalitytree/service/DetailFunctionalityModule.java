/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.functionalitytree.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.service.BIObjectsModule;
import it.eng.spagobi.analiticalmodel.document.utils.DetBIObjModHelper;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Implements a module which handles all low functionalities management: has
 * methods for low functionalities load, detail, modify/insertion and deleting
 * operations. The <code>service</code> method has a switch for all these
 * operations, differentiated the ones from the others by a <code>message</code>
 * String.
 * 
 * @author sulis
 */
public class DetailFunctionalityModule extends AbstractHttpModule{

	private String modality = "";
	public final static String MODULE_PAGE = "DetailFunctionalityPage";
	public final static String FUNCTIONALITY_OBJ = "FUNCTIONALITY_OBJ";
	public final static String PATH = "PATH";
	private String typeFunct = null;
	private IEngUserProfile profile;
	SessionContainer session = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base
	 * .SourceBean)
	 */
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insertion, modify,
	 * detail and deletion methods.
	 * 
	 * @param request
	 *            The Source Bean containing all request parameters
	 * @param response
	 *            The Source Bean containing all response parameters
	 * 
	 * @throws exception
	 *             If an exception occurs
	 * @throws Exception
	 *             the exception
	 */
	public void service(SourceBean request, SourceBean response)
			throws Exception {
		String message = (String) request
				.getAttribute(AdmintoolsConstants.MESSAGE_DETAIL);
		typeFunct = (String) request
				.getAttribute(AdmintoolsConstants.FUNCTIONALITY_TYPE);

		SpagoBITracer.debug(AdmintoolsConstants.NAME_MODULE,
				"DetailFunctionalityModule", "service",
				"begin of detail functionality modify/visualization service with message ="
						+ message);

		try {
			if (message == null) {
				EMFUserError userError = new EMFUserError(
						EMFErrorSeverity.ERROR, 101);
				SpagoBITracer.debug(AdmintoolsConstants.NAME_MODULE,
						"DetailFunctionalityModule", "service",
						"The message parameter is null");
				throw userError;
			}
			if (message.trim().equalsIgnoreCase(
					AdmintoolsConstants.DETAIL_SELECT)) {
				getDetailFunctionality(request, response);
			} else if (message.trim().equalsIgnoreCase(
					AdmintoolsConstants.DETAIL_MOD)) {
				modDettaglioFunctionality(request,
						AdmintoolsConstants.DETAIL_MOD, response);
			} else if (message.trim().equalsIgnoreCase(
					AdmintoolsConstants.DETAIL_NEW)) {
				newDettaglioFunctionality(request, response);
			} else if (message.trim().equalsIgnoreCase(
					AdmintoolsConstants.DETAIL_INS)) {
				modDettaglioFunctionality(request,
						AdmintoolsConstants.DETAIL_INS, response);
			} else if (message.trim().equalsIgnoreCase(
					AdmintoolsConstants.DETAIL_DEL)) {
				delFunctionality(request, AdmintoolsConstants.DETAIL_DEL,
						response);
			}

		} catch (EMFUserError eex) {
			EMFErrorHandler errorHandler = getErrorHandler();
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(
					EMFErrorSeverity.ERROR, ex);
			EMFErrorHandler errorHandler = getErrorHandler();
			errorHandler.addError(internalError);
			return;
		}
	}

	/**
	 * Gets the detail of a low functionality choosed by the user from the low
	 * functionalities list. It reaches the key from the request and asks to the
	 * DB all detail parameter use mode information, by calling the method
	 * <code>loadLowFunctionalityByPath</code>.
	 * 
	 * @param key
	 *            The choosed low functionality id key
	 * @param response
	 *            The response Source Bean
	 * @throws EMFUserError
	 *             If an exception occurs
	 */
	private void getDetailFunctionality(SourceBean request, SourceBean response)
			throws EMFUserError {
		try {
			this.modality = AdmintoolsConstants.DETAIL_MOD;
			String path = (String) request
					.getAttribute(DetailFunctionalityModule.PATH);
			int index = path.lastIndexOf("/");
			String parentPath = path.substring(0, index);
			response.setAttribute(AdmintoolsConstants.PATH_PARENT, parentPath);
			response.setAttribute(AdmintoolsConstants.MODALITY, modality);
			if (typeFunct.equals("LOW_FUNCT")) {
				LowFunctionality funct = DAOFactory.getLowFunctionalityDAO()
						.loadLowFunctionalityByPath(path, false);
				response.setAttribute(FUNCTIONALITY_OBJ, funct);
			}
		} catch (EMFUserError eex) {
			EMFErrorHandler errorHandler = getErrorHandler();
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE,
					"DetailFunctionalityModule", "getDetailFunctionality",
					"Cannot fill response container", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	/**
	 * Inserts/Modifies the detail of a low functionality according to the user
	 * request. When a parameter use mode is modified, the
	 * <code>modifyLowFunctionality</code> method is called; when a new
	 * parameter use mode is added, the <code>insertLowFunctionality</code>
	 * method is called. These two cases are differentiated by the
	 * <code>mod</code> String input value .
	 * 
	 * @param request
	 *            The request information contained in a SourceBean Object
	 * @param mod
	 *            A request string used to differentiate insert/modify
	 *            operations
	 * @param response
	 *            The response SourceBean
	 * @throws EMFUserError
	 *             If an exception occurs
	 * @throws SourceBeanException
	 *             If a SourceBean exception occurs
	 */
	private void modDettaglioFunctionality(SourceBean request, String mod,
			SourceBean response) throws EMFUserError, SourceBeanException {
		HashMap<String, String> logParam = new HashMap();
		try {
			// **********************************************************************
			RequestContainer requestContainer = this.getRequestContainer();	
			ResponseContainer responseContainer = this.getResponseContainer();	
			session = requestContainer.getSessionContainer();
			SessionContainer permanentSession = session.getPermanentContainer();
			profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			LowFunctionality lowFunct = recoverLowFunctionalityDetails(request,
					mod);
			logParam.put("Functionality_Name", lowFunct.getName());
			response.setAttribute(FUNCTIONALITY_OBJ, lowFunct);
			response.setAttribute(AdmintoolsConstants.MODALITY, mod);
			EMFErrorHandler errorHandler = getErrorHandler();
			// if(mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
			// String pathParent =
			// (String)request.getAttribute(AdmintoolsConstants.PATH_PARENT);
			// response.setAttribute(AdmintoolsConstants.PATH_PARENT,
			// pathParent);
			// }

			// if there are some validation errors into the errorHandler does
			// not write into DB
			Collection errors = errorHandler.getErrors();
			if (errors != null && errors.size() > 0) {
				Iterator iterator = errors.iterator();
				while (iterator.hasNext()) {
					Object error = iterator.next();
					if (error instanceof EMFValidationError) {
						Integer parentFolderId = lowFunct.getParentId();
						LowFunctionality parentFolder = null;
						if (parentFolderId != null) {
							parentFolder = DAOFactory.getLowFunctionalityDAO()
									.loadLowFunctionalityByID(parentFolderId,
											false);
						}
						if (parentFolder == null) {
							
							
							AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.ADD",logParam , "KO");		 
							
							throw new Exception("Parent folder not available.");
						} else {
							response.setAttribute(
									AdmintoolsConstants.PATH_PARENT,
									parentFolder.getPath());
						}
						return;
					}
				}
			}

			if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				//SessionContainer permSess = getRequestContainer().getSessionContainer().getPermanentContainer();
				//IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
				DAOFactory.getLowFunctionalityDAO().insertLowFunctionality(lowFunct, profile);
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.ADD",logParam , "OK");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			} else if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
				DAOFactory.getLowFunctionalityDAO().modifyLowFunctionality(
						lowFunct);
				
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.MODIFY",logParam , "OK");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// at this point erase inconsistent child roles that have been
				// deleted from parents
				// prova debug
				// Set set1 = new HashSet();
				// loadRolesToErase(lowFunct,set1);
				Set set = new HashSet();
				loadRolesToErase(lowFunct, set);
				DAOFactory.getLowFunctionalityDAO()
						.deleteInconsistentRoles(set);
			}

		} catch (EMFUserError eex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.ADD/MODIFY",logParam , "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			EMFErrorHandler errorHandler = getErrorHandler();
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.ADD/MODIFY",logParam , "ERR");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE,
					"DetailFunctionalityModule", "modDettaglioFunctionality",
					"Cannot fill response container", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute(AdmintoolsConstants.LOOPBACK, "true");

	}

	/**
	 * Deletes a low functionality choosed by user from the low functionality
	 * list.
	 * 
	 * @param request
	 *            The request SourceBean
	 * @param mod
	 *            A request string used to differentiate delete operation
	 * @param response
	 *            The response SourceBean
	 * @throws EMFUserError
	 *             If an Exception occurs
	 * @throws SourceBeanException
	 *             If a SourceBean Exception occurs
	 */
	private void delFunctionality(SourceBean request, String mod,
			SourceBean response) throws EMFUserError, SourceBeanException {
		HashMap<String, String> logParam = new HashMap();
		SessionContainer permSess = getRequestContainer().getSessionContainer().getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		
		try {

			if (typeFunct.equals("LOW_FUNCT")) {
				String path = (String) request.getAttribute(PATH);
				ILowFunctionalityDAO funcdao = DAOFactory
						.getLowFunctionalityDAO();
				LowFunctionality funct = funcdao.loadLowFunctionalityByPath(
						path, false);
				if (funct!=null) logParam.put("Functionlity_Name",funct.getName());

				funcdao.eraseLowFunctionality(funct, profile);
			}
		} catch (EMFUserError eex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.DELETE",logParam , "KO");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			EMFErrorHandler errorHandler = getErrorHandler();
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.DELETE",logParam , "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE,
					"DetailFunctionalityModule", "delFunctionality",
					"Cannot fill response container", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute("loopback", "true");
		try {
			AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.DELETE",logParam , "OK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		 
	}

	/**
	 * Instantiates a new
	 * <code>LowFunctionalitye<code> object when a new low functionality
	 * insertion is required, in order to prepare the page for the insertion.
	 * 
	 * @param response
	 *            The response SourceBean
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	private void newDettaglioFunctionality(SourceBean request,
			SourceBean response) throws EMFUserError {
		
		
		try {
			this.modality = AdmintoolsConstants.DETAIL_INS;
			String pathParent = (String) request
					.getAttribute(AdmintoolsConstants.PATH_PARENT);
			response.setAttribute(AdmintoolsConstants.MODALITY, modality);
			response.setAttribute(AdmintoolsConstants.PATH_PARENT, pathParent);

			if (typeFunct.equals("LOW_FUNCT")) {
				LowFunctionality funct = new LowFunctionality();
				funct.setDescription("");
				funct.setId(new Integer(0));
				funct.setCode("");
				funct.setName("");
				LowFunctionality parentFunct = DAOFactory
						.getLowFunctionalityDAO().loadLowFunctionalityByPath(
								pathParent, false);
				Role[] execRoles = new Role[0];
				Role[] devRoles = new Role[0];
				Role[] testRoles = new Role[0];
				Role[] createRoles = new Role[0];
				if (parentFunct != null) {
					execRoles = parentFunct.getExecRoles();
					devRoles = parentFunct.getDevRoles();
					testRoles = parentFunct.getTestRoles();
					createRoles = parentFunct.getCreateRoles();
				}
				funct.setTestRoles(testRoles);
				funct.setDevRoles(devRoles);
				funct.setExecRoles(execRoles);
				funct.setCreateRoles(createRoles);
				response.setAttribute(FUNCTIONALITY_OBJ, funct);
				
				
			}
			

			
		} catch (EMFUserError eex) {

			EMFErrorHandler errorHandler = getErrorHandler();
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {

			SpagoBITracer.major(AdmintoolsConstants.NAME_MODULE,
					"DetailFunctionalityModule", "newDettaglioFunctionality",
					"Cannot prepare page for the insertion", ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	private LowFunctionality recoverLowFunctionalityDetails(SourceBean request,
			String mod) throws EMFUserError, SourceBeanException {
		String name = (String) request.getAttribute("name");
		name = name.trim();
		String description = (String) request.getAttribute("description");
		description = description.trim();
		String code = (String) request.getAttribute("code");
		code = code.trim();

		List testAttrsList = request.getAttributeAsList("test");
		Role[] testRoles = new Role[testAttrsList.size()];
		for (int i = 0; i < testRoles.length; i++) {
			String idRoleStr = (String) testAttrsList.get(i);
			testRoles[i] = DAOFactory.getRoleDAO().loadByID(
					new Integer(idRoleStr));
		}
		List devAttrsList = request.getAttributeAsList("development");
		Role[] devRoles = new Role[devAttrsList.size()];
		for (int i = 0; i < devRoles.length; i++) {
			String idRoleStr = (String) devAttrsList.get(i);
			devRoles[i] = DAOFactory.getRoleDAO().loadByID(
					new Integer(idRoleStr));
		}
		List execAttrsList = request.getAttributeAsList("execution");
		Role[] execRoles = new Role[execAttrsList.size()];
		for (int i = 0; i < execRoles.length; i++) {
			String idRoleStr = (String) execAttrsList.get(i);
			execRoles[i] = DAOFactory.getRoleDAO().loadByID(
					new Integer(idRoleStr));
		}
		List createAttrsList = request.getAttributeAsList("creation");
		Role[] createRoles = new Role[createAttrsList.size()];
		for (int i = 0; i < createRoles.length; i++) {
			String idRoleStr = (String) createAttrsList.get(i);
			createRoles[i] = DAOFactory.getRoleDAO().loadByID(
					new Integer(idRoleStr));
		}

		LowFunctionality lowFunct = null;

		if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
			String pathParent = (String) request
					.getAttribute(AdmintoolsConstants.PATH_PARENT);
			LowFunctionality parentFunct = DAOFactory.getLowFunctionalityDAO()
					.loadLowFunctionalityByPath(pathParent, false);
			if (parentFunct == null) {
				HashMap<String, String> logParam = new HashMap();
				logParam.put("Funtionality_Name", parentFunct.getName());
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "parentFunct",logParam , "OK");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				EMFValidationError error = new EMFValidationError(
						EMFErrorSeverity.ERROR,
						AdmintoolsConstants.PATH_PARENT, "1002", new Vector());
				getErrorHandler().addError(error);
			}
			String newPath = pathParent + "/" + code;
			// SourceBean dataLoad = new SourceBean("dataLoad");
			LowFunctionality funct = DAOFactory.getLowFunctionalityDAO()
					.loadLowFunctionalityByPath(newPath, false);
			if (funct != null) {
				HashMap<String, String> params = new HashMap();
				params.put(AdmintoolsConstants.PAGE.toString(),
						BIObjectsModule.MODULE_PAGE.toString());
				// params.put(SpagoBIConstants.ACTOR,
				// SpagoBIConstants.ADMIN_ACTOR);
				params.put(SpagoBIConstants.OPERATION.toString(),
						SpagoBIConstants.FUNCTIONALITIES_OPERATION.toString());
				EMFValidationError error = new EMFValidationError(
						EMFErrorSeverity.ERROR, "code", "1005", new Vector(),
						params);
				getErrorHandler().addError(error);
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.ADD",null , "ERR");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (DAOFactory.getLowFunctionalityDAO().existByCode(code) != null) {
				EMFValidationError error = new EMFValidationError(
						EMFErrorSeverity.ERROR, "code", "1027");
				getErrorHandler().addError(error);
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.ADD",null , "ERR");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			lowFunct = new LowFunctionality();
			lowFunct.setCode(code);
			lowFunct.setDescription(description);
			lowFunct.setName(name);
			lowFunct.setPath(newPath);
			lowFunct.setDevRoles(devRoles);
			lowFunct.setExecRoles(execRoles);
			lowFunct.setTestRoles(testRoles);
			lowFunct.setCreateRoles(createRoles);

			if (parentFunct != null)
				lowFunct.setParentId(parentFunct.getId());
		} else if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
			String idFunct = (String) request
					.getAttribute(AdmintoolsConstants.FUNCTIONALITY_ID);
			Integer idFunctWithSameCode = DAOFactory.getLowFunctionalityDAO()
					.existByCode(code);
			if ((idFunctWithSameCode != null)
					&& !(idFunctWithSameCode.equals(new Integer(idFunct)))) {
				try {
					AuditLogUtilities.updateAudit(getHttpRequest(),  profile, "FUNCTIONALITY.MODIFY",null , "ERR");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				EMFValidationError error = new EMFValidationError(
						EMFErrorSeverity.ERROR, 1027);
				getErrorHandler().addError(error);
			}
			lowFunct = DAOFactory.getLowFunctionalityDAO()
					.loadLowFunctionalityByID(new Integer(idFunct), false);

			// finds the new path
			String oldPath = lowFunct.getPath();
			String parentPath = oldPath.substring(0, oldPath.lastIndexOf("/"));
			String newPath = parentPath + "/" + code;

			lowFunct.setPath(newPath);
			lowFunct.setCode(code);
			lowFunct.setDescription(description);
			lowFunct.setName(name);
			lowFunct.setDevRoles(devRoles);
			lowFunct.setExecRoles(execRoles);
			lowFunct.setTestRoles(testRoles);
			lowFunct.setCreateRoles(createRoles);
		}

		return lowFunct;
	}

	/**
	 * Controls if a particular role belongs to the parent functionality. It is
	 * called inside functionalities Jsp in ordet to identify those roles that a
	 * child functionality is able to select.
	 * 
	 * @param rule
	 *            The role id string identifying the role
	 * @param parentLowFunct
	 *            the parent low functionality object
	 * @param permission
	 *            The role's permission
	 * 
	 * @return True if the role belongs to the parent funct, else false
	 */
	public boolean isParentRule(String rule, LowFunctionality parentLowFunct,
			String permission) {
		boolean isParent = false;
		if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)) {
			Role[] devRolesObj = parentLowFunct.getDevRoles();
			String[] devRules = new String[devRolesObj.length];
			for (int i = 0; i < devRolesObj.length; i++) {
				devRules[i] = devRolesObj[i].getId().toString();
				if (rule.equals(devRules[i])) {
					isParent = true;
				}
			}
		} else if (permission
				.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
			Role[] execRolesObj = parentLowFunct.getExecRoles();
			String[] execRules = new String[execRolesObj.length];
			for (int i = 0; i < execRolesObj.length; i++) {
				execRules[i] = execRolesObj[i].getId().toString();
				if (rule.equals(execRules[i])) {
					isParent = true;
				}
			}
		} else if (permission
				.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
			Role[] testRolesObj = parentLowFunct.getTestRoles();
			String[] testRules = new String[testRolesObj.length];
			for (int i = 0; i < testRolesObj.length; i++) {
				testRules[i] = testRolesObj[i].getId().toString();
				if (rule.equals(testRules[i])) {
					isParent = true;
				}
			}
		} else if (permission
				.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
			Role[] createRolesObj = parentLowFunct.getCreateRoles();
			String[] createRules = new String[createRolesObj.length];
			for (int i = 0; i < createRolesObj.length; i++) {
				createRules[i] = createRolesObj[i].getId().toString();
				if (rule.equals(createRules[i])) {
					isParent = true;
				}
			}
		}
		return isParent;
	}

	/**
	 * Defines all roles that have to be erased in order to keep functionalities
	 * tree consistence. When we leave some permissions to a functionality,
	 * those permissions will not be assignable to all the children
	 * functionality. If any child has a permission that his parent anymore has,
	 * this permission mus be deleted for all father's children and descendants.
	 * This metod recusively scans all father's descendants and saves inside a
	 * Set all roles that must be erased from the Database.
	 * 
	 * @param lowFuncParent
	 *            the parent Functionality
	 * @param rolesToErase
	 *            the set containing all roles to erase
	 * 
	 * @throws EMFUserError
	 *             if any EMFUserError exception occurs
	 * @throws BuildOperationException
	 *             if any BuildOperationException exception occurs
	 * @throws OperationExecutionException
	 *             if any OperationExecutionException exception occurs
	 */
	public void loadRolesToErase(LowFunctionality lowFuncParent,
			Set rolesToErase) throws EMFUserError {
		String parentPath = lowFuncParent.getPath();
		// ArrayList childs =
		// DAOFactory.getFunctionalityCMSDAO().recoverChilds(parentPath);
		List childs = DAOFactory.getLowFunctionalityDAO()
				.loadSubLowFunctionalities(parentPath, false);
		if (childs.size() != 0) {
			Iterator i = childs.iterator();
			while (i.hasNext()) {
				LowFunctionality childNode = (LowFunctionality) i.next();
				String childPath = childNode.getPath();
				// LowFunctionality lowFuncParent =
				// DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(parentPath);
				LowFunctionality lowFuncChild = DAOFactory
						.getLowFunctionalityDAO().loadLowFunctionalityByPath(
								childPath, false);
				if (lowFuncChild != null) {
					// control childs permissions and fathers permissions
					// remove from childs those persmissions that are not
					// present in the fathers
					// control for test Roles
					Role[] testChildRoles = lowFuncChild.getTestRoles();
					// Role[] testParentRoles = lowFuncParent.getTestRoles();
					// ArrayList newTestChildRoles = new ArrayList();
					// HashMap rolesToErase = new HashMap();
					for (int j = 0; j < testChildRoles.length; j++) {
						String rule = testChildRoles[j].getId().toString();
						if (!isParentRule(rule, lowFuncParent,
								SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, testChildRoles[j].getId());
							roles.add(
									2,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(
									lowFuncChild,
									rule,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST);
							// rolesToErase.put(lowFuncChild.getId(),testChildRoles[j].getId());
							// DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,testChildRoles[j].getId());
						}
					}
					// control for development roles
					Role[] devChildRoles = lowFuncChild.getDevRoles();
					// Role[] devParentRoles = lowFuncParent.getDevRoles();
					// ArrayList newDevChildRoles = new ArrayList();
					for (int j = 0; j < devChildRoles.length; j++) {
						String rule = devChildRoles[j].getId().toString();
						if (!isParentRule(
								rule,
								lowFuncParent,
								SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, devChildRoles[j].getId());
							roles.add(
									2,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(
									lowFuncChild,
									rule,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP);
							// rolesToErase.put(lowFuncChild.getId(),devChildRoles[j].getId());
							// DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,devChildRoles[j].getId());
						}
					}
					// control for execution roles
					Role[] execChildRoles = lowFuncChild.getExecRoles();
					// Role[] execParentRoles = lowFuncParent.getExecRoles();
					// ArrayList newExecChildRoles = new ArrayList();
					for (int j = 0; j < execChildRoles.length; j++) {
						String rule = execChildRoles[j].getId().toString();
						if (!isParentRule(
								rule,
								lowFuncParent,
								SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, execChildRoles[j].getId());
							roles.add(
									2,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(
									lowFuncChild,
									rule,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE);
							// rolesToErase.put(lowFuncChild.getId(),execChildRoles[j].getId());
							// DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,execChildRoles[j].getId());
						}
					}
					// control for development roles
					Role[] createChildRoles = lowFuncChild.getCreateRoles();
					for (int j = 0; j < createChildRoles.length; j++) {
						String rule = createChildRoles[j].getId().toString();
						if (!isParentRule(rule, lowFuncParent,
								SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, createChildRoles[j].getId());
							roles.add(
									2,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(
									lowFuncChild,
									rule,
									SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE);
							// rolesToErase.put(lowFuncChild.getId(),devChildRoles[j].getId());
							// DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,devChildRoles[j].getId());
						}
					}

					// loadRolesToErase(lowFuncChild,rolesToErase);
				}

				// loadRolesToErase(childPath,rolesToErase);
			}

		}

	}

	/**
	 * Erases the defined input role from a functionality object, if this one
	 * has the role.The updated functionality object is returned.
	 * 
	 * @param func
	 *            the input functionality object
	 * @param roleId
	 *            the role id for the role to erase
	 * @param permission
	 *            the permission of the role to erase
	 * 
	 * @return the updated functionality
	 */
	public LowFunctionality eraseRolesFromFunctionality(LowFunctionality func,
			String roleId, String permission) {
		if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)) {
			Role[] roles = func.getDevRoles();
			Set devRolesSet = new HashSet();
			for (int i = 0; i < roles.length; i++) {
				if (!roles[i].getId().toString().equals(roleId)) {
					devRolesSet.add(roles[i]);
				}

			}
			func.setDevRoles((Role[]) devRolesSet.toArray(new Role[0]));

		}
		if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
			Role[] roles = func.getTestRoles();
			Set testRolesSet = new HashSet();
			for (int i = 0; i < roles.length; i++) {
				if (!roles[i].getId().toString().equals(roleId)) {
					testRolesSet.add(roles[i]);
				}

			}
			func.setTestRoles((Role[]) testRolesSet.toArray(new Role[0]));

		}
		if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
			Role[] roles = func.getExecRoles();
			Set execRolesSet = new HashSet();
			for (int i = 0; i < roles.length; i++) {
				if (!roles[i].getId().toString().equals(roleId)) {
					execRolesSet.add(roles[i]);
				}

			}
			func.setExecRoles((Role[]) execRolesSet.toArray(new Role[0]));

		}
		if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
			Role[] roles = func.getCreateRoles();
			Set createRolesSet = new HashSet();
			for (int i = 0; i < roles.length; i++) {
				if (!roles[i].getId().toString().equals(roleId)) {
					createRolesSet.add(roles[i]);
				}

			}
			func.setCreateRoles((Role[]) createRolesSet.toArray(new Role[0]));

		}
		return func;
	}
}
