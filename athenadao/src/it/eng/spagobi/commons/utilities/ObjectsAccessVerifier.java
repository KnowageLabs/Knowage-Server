/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Contains some methods to control user exec/dev/test rights.
 * 
 * @author sulis
 */
public class ObjectsAccessVerifier {

	static private Logger logger = Logger.getLogger(ObjectsAccessVerifier.class);

	/**
	 * Controls if the current user can develop the object relative to the input folder id.
	 * 
	 * @param state
	 *            state of the object
	 * @param folderId
	 *            The id of the folder containing te object
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canDev(String state, Integer folderId, IEngUserProfile profile) {
		if (!state.equals("DEV")) {
			return false;
		}
		return canDevInternal(folderId, profile);
	}

	/**
	 * Controls if current user can exec the object relative to the input folder id.
	 * 
	 * @param state
	 *            state of the object
	 * @param folderId
	 *            The id of the folder containing te object
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canExec(String state, Integer folderId, IEngUserProfile profile) {
		logger.debug("IN.state=" + state);
		if (isAbleToExec(state, profile)) {
			/*
			 * if (!state.equals("REL")) { return false; }
			 */
			LowFunctionality folder = null;
			try {
				folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
			} catch (Exception e) {
				logger.error("Exception in loadLowFunctionalityByID", e);
				return false;
			} finally {
				logger.debug("OUT");
			}
			return canExecInternal(folder, profile);
		} else {
			logger.debug("OUT.return false");
			return false;
		}
	}

	/**
	 * Metodo che verifica se nell'elenco delle funzionalità ne esiste almeno una con diritto di esecuzione
	 * 
	 * @param state
	 * @param profile
	 * @return
	 */
	public static boolean canExec(String state, List folders, IEngUserProfile profile) {

		logger.debug("IN.state=" + state);
		boolean canExec = false;
		if (isAbleToExec(state, profile)) {

			Iterator folderIt = folders.iterator();
			while (folderIt.hasNext()) {
				LowFunctionality folder = (LowFunctionality) folderIt.next();
				canExec = canExecInternal(folder, profile);
				if (canExec) {
					logger.debug("OUT.return true");
					return true;
				}
			}
			logger.debug("OUT.return false");
			return false;

		} else {
			logger.debug("OUT.return false");
			return false;
		}
	}

	/**
	 * Metodo che verifica se nell'elenco delle funzionalità ne esiste almeno una con diritto di esecuzione
	 * 
	 * @param state
	 * @param profile
	 * @return
	 */
	public static boolean canDev(String state, List folders, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canDev");
		logger.debug("IN.state=" + state);
		boolean canDev = false;
		if (isAbleToExec(state, profile)) {

			Iterator folderIt = folders.iterator();
			while (folderIt.hasNext()) {
				LowFunctionality folder = (LowFunctionality) folderIt.next();
				canDev = canDevInternal(folder, profile);
				if (canDev) {
					logger.debug("OUT.return true");
					monitor.stop();
					return true;
				}
			}
			logger.debug("OUT.return false");
			monitor.stop();
			return false;

		} else {
			logger.debug("OUT.return false");
			monitor.stop();
			return false;
		}
	}

	/**
	 * Metodo che verifica se nell'elenco delle funzionalità ne esiste almeno una con diritto di esecuzione
	 * 
	 * @param state
	 * @param profile
	 * @return
	 */
	public static boolean canTest(String state, List folders, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canTest");
		logger.debug("IN.state=" + state);
		boolean canTest = false;
		if (isAbleToExec(state, profile)) {

			Iterator folderIt = folders.iterator();
			while (folderIt.hasNext()) {
				LowFunctionality folder = (LowFunctionality) folderIt.next();
				canTest = canTestInternal(folder, profile);
				if (canTest) {
					logger.debug("OUT.return true");
					monitor.stop();
					return true;
				}
			}
			logger.debug("OUT.return false");
			monitor.stop();
			return false;

		} else {
			logger.debug("OUT.return false");
			monitor.stop();
			return false;
		}
	}

	/**
	 * Metodo che verifica il numero di istanze visibili del documento
	 * 
	 * @param state
	 * @param userProfile
	 * @return
	 */
	public static int getVisibleInstances(String initialPath, List folders) {

		logger.debug("IN");

		int visibleInstances = 0;
		if (initialPath != null && !initialPath.trim().equals("")) {
			Iterator folderIt = folders.iterator();
			while (folderIt.hasNext()) {
				LowFunctionality folder = (LowFunctionality) folderIt.next();
				String folderPath = folder.getPath();
				if (folderPath.equalsIgnoreCase(initialPath) || folderPath.startsWith(initialPath + "/")) {
					visibleInstances++;
				}
			}
		} else {
			visibleInstances = folders.size();
		}
		logger.debug("OUT");
		return visibleInstances;

	}

	public static boolean isAbleToExec(String state, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.isAbleToExec");
		logger.debug("IN.state=" + state);
		if (state.equals("REL")) {
			logger.debug("OUT.return true");
			monitor.stop();
			return true;
		} else if (state.equals("DEV")) {
			try {
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
					logger.debug("OUT.return true");
					return true;
				} else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST)) {
					logger.debug("OUT.return false");
					return false;
				}
			} catch (EMFInternalError e) {
				logger.error(e);
			}
		} else if (state.equals("TEST")) {
			try {
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST)) {
					logger.debug("OUT.return true");
					return true;
				} else if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
					logger.debug("OUT.return false");
					return false;
				}
			} catch (EMFInternalError e) {
				logger.error(e);
			}
		}
		logger.debug("OUT");
		monitor.stop();
		return false;
	}

	public static boolean isAbleToSave(JSONArray documentfolders, IEngUserProfile profile) throws EMFInternalError, JSONException {
		if (documentfolders != null) {
			for (int it = 0; it < documentfolders.length(); it++) {
				if (canCreateInternal(documentfolders.getInt(it), profile)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Control if current user can test the object relative to the folder id.
	 * 
	 * @param state
	 *            state of the object
	 * @param folderId
	 *            The id of the folder containing the object
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canTest(String state, Integer folderId, IEngUserProfile profile) {
		logger.debug("IN.state=" + state);
		if (!state.equals("TEST")) {
			return false;
		}
		return canTestInternal(folderId, profile);

	}

	/**
	 * Control if the user can develop the document specified by the input id
	 * 
	 * @param documentId
	 *            The id of the document
	 * @param profile
	 *            The user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canDevBIObject(Integer biObjectID, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canDevBIObject(Integer biObjectID, IEngUserProfile profile)");
		boolean toReturn = false;
		try {
			logger.debug("IN: obj id = [" + biObjectID + "]; user id = [" + ((UserProfile) profile).getUserId() + "]");
			// if user is administrator, he can develop, no need to make any query to database
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				logger.debug("User [" + ((UserProfile) profile).getUserId() + "] is administrator. He can develop every document");
				monitor.stop();
				return true;
			}
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(biObjectID);
			toReturn = canDevBIObject(obj, profile);
		} catch (Exception e) {
			logger.error(e);
			monitor.stop();
			return false;
		}
		logger.debug("OUT: returning " + toReturn);
		monitor.stop();
		return toReturn;
	}

	/**
	 * Control if the user can develop the input document
	 * 
	 * @param documentId
	 *            The id of the document
	 * @param profile
	 *            The user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canDevBIObject(BIObject obj, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canDevBIObject(BIObject obj, IEngUserProfile profile)");
		boolean toReturn = false;
		try {
			logger.debug("IN: obj label = [" + obj.getLabel() + "]; user id = [" + ((UserProfile) profile).getUserId() + "]");
			// if user is administrator, he can develop, no need to make any query to database
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				logger.debug("User [" + ((UserProfile) profile).getUserId() + "] is administrator. He can develop every document");
				monitor.stop();
				return true;
			}
			// if user is not an administrator and document is not in DEV state, document cannot be developed
			if (!"DEV".equals(obj.getStateCode())) {
				logger.debug("User [" + ((UserProfile) profile).getUserId()
						+ "] is not an administrator and document is not in DEV state, so it cannot be developed");
				monitor.stop();
				return false;
			}
			// if user is not an administrator and document is in DEV state, we must see if he has development permission
			List folders = obj.getFunctionalities();
			Iterator it = folders.iterator();
			while (it.hasNext()) {
				Integer folderId = (Integer) it.next();
				boolean canDevInFolder = canDev(folderId, profile);
				if (canDevInFolder) {
					logger.debug("User can develop in functionality with id = " + folderId);
					toReturn = true;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error while loading BIObject", e);
			monitor.stop();
			return false;
		}
		logger.debug("OUT: returning " + toReturn);
		monitor.stop();
		return toReturn;
	}

	/**
	 * Control if the current user can develop new object into the functionality identified by its id.
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canDev(Integer folderId, IEngUserProfile profile) {
		return canDevInternal(folderId, profile);
	}

	/**
	 * Control if the current user can develop new object into the functionality identified by its id.
	 *
	 * @param folder
	 *            The lowFunctionality
	 * @param profile
	 *            user profile
	 *
	 * @return A boolean control value
	 */
	public static boolean canDev(LowFunctionality folder, IEngUserProfile profile) {
		return canDevInternal(folder, profile);
	}

	/**
	 * Control if the current user can test new object into the functionality.
	 *
	 * @param folder
	 *            The lowFunctionality
	 * @param profile
	 *            user profile
	 *
	 * @return A boolean control value
	 */
	public static boolean canTest(LowFunctionality folder, IEngUserProfile profile) {
		return canTestInternal(folder, profile);

	}

	/**
	 * Control if the current user can test new object into the functionality identified by its id.
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canTest(Integer folderId, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canTest");
		logger.debug("IN");
		LowFunctionality folder = null;
		try {
			folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
		} catch (Exception e) {
			logger.error("Exception in loadLowFunctionalityByID", e);

			return false;
		} finally {
			monitor.stop();
			logger.debug("OUT");
		}
		return canTestInternal(folder, profile);
	}

	/**
	 * Control if the current user can execute objects into the input functionality.
	 * 
	 * @param folder
	 *            The lowFunctionality
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canExec(LowFunctionality folder, IEngUserProfile profile) {
		return canExecInternal(folder, profile);
	}

	/**
	 * Control if the current user can execute new object into the functionality identified by its id.
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 */
	public static boolean canExec(Integer folderId, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canExec");
		logger.debug("IN");
		LowFunctionality folder = null;
		try {
			folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
		} catch (Exception e) {
			logger.error("Exception in loadLowFunctionalityByID", e);

			return false;
		} finally {
			monitor.stop();
			logger.debug("OUT");
		}
		return canExecInternal(folder, profile);
	}

	/**
	 * Private method called by the corrispondent public method canExec. Executes roles functionalities control .
	 * 
	 * @param folder
	 *            The lowFunctionality
	 * @param profile
	 *            user profile
	 * @return A boolean control value
	 */
	private static boolean canExecInternal(LowFunctionality folder, IEngUserProfile profile) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canExecInternal");

		Collection roles = null;

		try {
			roles = ((UserProfile) profile).getRolesForUse();
		} catch (EMFInternalError emfie) {
			logger.error("EMFInternalError in profile.getRoles");
			logger.debug("OUT.return false");
			monitor.stop();
			return false;
		}

		if (folder.getCodType().equalsIgnoreCase("USER_FUNCT")) {
			monitor.stop();
			return true;
		}

		Role[] execRoles = folder.getExecRoles();
		List execRoleNames = new ArrayList();
		for (int i = 0; i < execRoles.length; i++) {
			Role role = execRoles[i];
			execRoleNames.add(role.getName());
		}

		Iterator iterRoles = roles.iterator();
		String roleName = "";
		while (iterRoles.hasNext()) {
			roleName = (String) iterRoles.next();
			if (execRoleNames.contains(roleName)) {
				logger.debug("OUT.return true");
				monitor.stop();
				return true;
			}
		}
		logger.debug("OUT.return false");
		monitor.stop();
		return false;

	}

	/**
	 * Private method called by the corrispondent public method canTest. Executes roles functionalities control .
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * @return A boolean control value
	 */
	private static boolean canTestInternal(LowFunctionality folder, IEngUserProfile profile) {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canTestInternal");
		logger.debug("IN");
		Collection roles = null;

		try {
			roles = ((UserProfile) profile).getRolesForUse();
		} catch (EMFInternalError emfie) {
			logger.error("EMFInternalError in profile.getRoles", emfie);
			monitor.stop();
			return false;
		}

		Role[] testRoles = folder.getTestRoles();
		List testRoleNames = new ArrayList();
		for (int i = 0; i < testRoles.length; i++) {
			Role role = testRoles[i];
			testRoleNames.add(role.getName());
		}

		Iterator iterRoles = roles.iterator();
		String roleName = "";
		while (iterRoles.hasNext()) {
			roleName = (String) iterRoles.next();
			if (testRoleNames.contains(roleName)) {
				logger.debug("OUT. return true");
				monitor.stop();
				return true;
			}
		}
		logger.debug("OUT. return false");
		monitor.stop();
		return false;

	}

	/**
	 * Private method called by the corrispondent public method canDev. Executes roles functionalities control .
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * @return A boolean control value
	 */
	private static boolean canDevInternal(LowFunctionality folder, IEngUserProfile profile) {
		logger.debug("IN");
		Collection roles = null;
		try {
			roles = ((UserProfile) profile).getRolesForUse();

		} catch (EMFInternalError emfie) {
			logger.error("EMFInternalError in profile.getRoles", emfie);
			logger.debug("OUT. return false");
			return false;
		}

		Role[] devRoles = folder.getDevRoles();
		List devRoleNames = new ArrayList();
		for (int i = 0; i < devRoles.length; i++) {
			Role role = devRoles[i];
			devRoleNames.add(role.getName());
		}

		Iterator iterRoles = roles.iterator();
		String roleName = "";
		while (iterRoles.hasNext()) {
			roleName = (String) iterRoles.next();
			if (devRoleNames.contains(roleName)) {

				logger.debug("OUT. return true");
				return true;
			}
		}
		logger.debug("OUT. return false");
		return false;

	}

	/**
	 * Private method called by the corrispondent public method canTest. Executes roles functionalities control .
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * @return A boolean control value
	 */
	private static boolean canTestInternal(Integer folderId, IEngUserProfile profile) {
		logger.debug("IN");
		Collection roles = null;

		try {
			roles = ((UserProfile) profile).getRolesForUse();

		} catch (EMFInternalError emfie) {
			logger.error("EMFInternalError in profile.getRoles", emfie);
			return false;
		}

		LowFunctionality funct = null;
		try {
			funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
		} catch (Exception e) {
			logger.error("Exception in loadLowFunctionalityByID", e);
			logger.debug("OUT. return false");
			return false;
		}
		Role[] testRoles = funct.getTestRoles();
		List testRoleNames = new ArrayList();
		for (int i = 0; i < testRoles.length; i++) {
			Role role = testRoles[i];
			testRoleNames.add(role.getName());
		}

		Iterator iterRoles = roles.iterator();
		String roleName = "";
		while (iterRoles.hasNext()) {
			roleName = (String) iterRoles.next();
			if (testRoleNames.contains(roleName)) {
				logger.debug("OUT. return true");
				return true;
			}
		}
		logger.debug("OUT. return false");
		return false;

	}

	/**
	 * Private method called by the corrispondent public method isAbleToSave. Executes roles functionalities control .
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 */
	private static boolean canCreateInternal(Integer folderId, IEngUserProfile profile) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canSaveInternal");
		Collection roles = null;
		try {
			roles = ((UserProfile) profile).getRolesForUse();

		} catch (EMFInternalError emfie) {
			logger.error("EMFInternalError in profile.getRoles", emfie);
			logger.debug("OUT. return false");
			monitor.stop();
			return false;
		}

		LowFunctionality funct = null;
		try {
			funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
		} catch (Exception e) {
			logger.error("EMFInternalError in loadLowFunctionalityByID", e);
			logger.debug("OUT. return false");
			monitor.stop();
			return false;
		}
		Role[] createRoles = funct.getCreateRoles();
		List createRoleNames = new ArrayList();
		for (int i = 0; i < createRoles.length; i++) {
			Role role = createRoles[i];
			createRoleNames.add(role.getName());
		}

		Iterator iterRoles = roles.iterator();
		String roleName = "";
		while (iterRoles.hasNext()) {
			roleName = (String) iterRoles.next();
			if (createRoleNames.contains(roleName)) {

				logger.debug("OUT. return true");
				monitor.stop();
				return true;
			}
		}

		if (profile != null) {
			LowFunctionality personalFolder = UserUtilities.loadUserFunctionalityRoot((UserProfile) profile, false);
			// if (personalFolder != null && personalFolder.getId() == folderId) {
			if (personalFolder == null) {
				try {
					UserUtilities.createUserFunctionalityRoot(profile);
					return true;
				} catch (Exception e) {
					logger.error("Error while createUserFunctionalityRoot", e);
				}
			} else {
				return true;
			}
		}

		logger.debug("OUT. return false");
		monitor.stop();
		return false;

	}

	/**
	 * Private method called by the corrispondent public method canDev. Executes roles functionalities control .
	 * 
	 * @param folderId
	 *            The id of the lowFunctionality
	 * @param profile
	 *            user profile
	 * @return A boolean control value
	 */
	private static boolean canDevInternal(Integer folderId, IEngUserProfile profile) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canDevInternal");
		;
		boolean toReturn = false;
		try {

			Assert.assertNotNull(folderId, "Input folder id not specified");
			Assert.assertNotNull(profile, "Input profile object not specified");

			// initializing DAO objects
			ILowFunctionalityDAO foldersDAO = DAOFactory.getLowFunctionalityDAO();
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			foldersDAO.setUserProfile(profile);
			roleDAO.setUserProfile(profile);

			// getting roles with DEV permission on folder
			Collection roles = ((UserProfile) profile).getRolesForUse();
			LowFunctionality funct = foldersDAO.loadLowFunctionalityByID(folderId, false);
			Assert.assertNotNull(funct, "Folder with id [" + folderId + "] not found");
			Role[] devRoles = funct.getDevRoles();
			List devRoleNames = new ArrayList();
			for (int i = 0; i < devRoles.length; i++) {
				Role role = devRoles[i];
				devRoleNames.add(role.getName());
			}

			// iterating on user's roles
			Iterator iterRoles = roles.iterator();
			String roleName = "";
			while (iterRoles.hasNext()) {
				roleName = (String) iterRoles.next();
				Role role = roleDAO.loadByName(roleName);
				// if the role is DEV_ROLE role type and has development permission on folder, the user is able to develop in folder
				if ((role.getRoleTypeCD().equals("DEV_ROLE") || role.getRoleTypeCD().equals("ADMIN"))

				&& devRoleNames.contains(roleName)

				) {
					toReturn = true;
				}
			}

			logger.debug("Returning " + toReturn);
			return toReturn;
		} catch (Exception e) {
			logger.error("Error while evaluating development permission on folder with id [" + folderId + "] for user [" + profile + "]", e);
			throw new SpagoBIRuntimeException("Error while evaluating development permission on folder with id [" + folderId + "] for user [" + profile + "]",
					e);
		} finally {
			logger.debug("OUT");
			monitor.stop();
		}

	}

	/**
	 * Controls if the current user can see the document: - if the document is in DEV state the user must have the development permission in a folder containing
	 * it; - if the document is in TEST state the user must have the test permission in a folder containing it; - if the document is in REL state the user must
	 * have the execution permission in a folder containing it.
	 * 
	 * @param obj
	 *            The BIObject
	 * @param profile
	 *            user profile
	 * 
	 * @return A boolean control value
	 * 
	 * @throws EMFInternalError
	 *             the EMF internal error
	 */
	public static boolean canSee(BIObject obj, IEngUserProfile profile) throws EMFInternalError {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canSee(BIObject obj, IEngUserProfile profile)");
		boolean canSee = false;
		if (obj == null) {
			logger.warn("BIObject in input is null!!");
			monitor.stop();
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "BIObject in input is null!!");
		}
		if (profile == null) {
			logger.warn("User profile in input is null!!");
			monitor.stop();
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "User profile in input is null!!");
		}
		String state = obj.getStateCode();
		if ("SUSP".equalsIgnoreCase(state)) {
			if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				monitor.stop();
				return false;
			} else {
				monitor.stop();
				return true;
			}
		}

		List foldersId = obj.getFunctionalities();
		if (foldersId == null || foldersId.size() == 0) {
			logger.warn("BIObject does not belong to any functionality!!");
			monitor.stop();
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "BIObject does not belong to any functionality!!");
		}
		Iterator foldersIdIt = foldersId.iterator();
		while (foldersIdIt.hasNext()) {
			Integer folderId = (Integer) foldersIdIt.next();
			boolean canDev = canDev(state, folderId, profile);
			if (canDev) {
				canSee = true;
				break;
			}
			boolean canTest = canTest(state, folderId, profile);
			if (canTest) {
				canSee = true;
				break;
			}
			boolean canExec = canExec(state, folderId, profile);
			if (canExec) {
				// administrators, developers, testers, behavioural model administrators can see that document
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) // for administrators
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV) // for developers
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST) // for testers
						|| profile.isAbleToExecuteAction(SpagoBIConstants.PARAMETER_MANAGEMENT)) { // for behavioral model administrators
					canSee = true;
				} else {
					if (obj.isPublicDoc()
							|| (!obj.isPublicDoc() && ((UserProfile) profile).getUserId().equals(obj.getCreationUser()) || isUserPersonalFolder(folderId,
									profile))) {
						canSee = checkProfileVisibility(obj, profile);
					}
				}
				break;
			}
		}
		monitor.stop();
		logger.debug("OUT.canSee=" + canSee);
		return canSee;
	}

	/**
	 * Controls if the user can see the LowFunctionality. The root LowFunctionality is visible by everybody. The administrator can see all LowFunctionalities.
	 * Other users can see the LowFunctionality only if they have at least one of the following permission: - they can develop on that folder; - they can test
	 * on that folder; - they can execute on that folder.
	 * 
	 * @param lowFunctionality
	 *            The LowFunctionality
	 * @param profile
	 *            user profile
	 * 
	 * @return true if the user can see the specified lowFunctionality, false otherwise
	 * 
	 * @throws EMFInternalError
	 *             the EMF internal error
	 */
	public static boolean canSee(LowFunctionality lowFunctionality, IEngUserProfile profile) throws EMFInternalError {
		boolean canSee = false;
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canSee(LowFunctionality lowFunctionality, IEngUserProfile profile)");
		logger.debug("IN: lowFunctionality path = [" + lowFunctionality.getPath() + "]; userId = [" + ((UserProfile) profile).getUserId() + "]");
		// if it is root folder, anybody can see it
		if (lowFunctionality.getParentId() == null) {
			canSee = true;
		} else {
			// if user is administrator, he can see all functionalities
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				canSee = true;
			} else {
				// if user can exec or dev or test on functionality, he can see it, otherwise he cannot see it
				if (ObjectsAccessVerifier.canExec(lowFunctionality, profile) || ObjectsAccessVerifier.canTest(lowFunctionality, profile)
						|| ObjectsAccessVerifier.canDev(lowFunctionality, profile)) {
					canSee = true;
				} else {
					canSee = false;
				}
			}
		}
		logger.debug("OUT.canSee=" + canSee);
		monitor.stop();
		return canSee;
	}

	/**
	 * Checks if the document in input has profiled visibility constraints. If it is the case, checks if the user in input has suitable profile attributes.
	 * 
	 * @param obj
	 * @param profile
	 * @return true if document profiled visibility constraints are satisfied by the user
	 * @throws EMFInternalError
	 */
	public static boolean checkProfileVisibility(BIObject obj, IEngUserProfile profile) throws EMFInternalError {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.checkProfileVisibility");

		logger.debug("IN: obj label is [" + obj.getLabel() + "]; user is [" + ((UserProfile) profile).getUserId().toString() + "]");
		boolean toReturn = true;
		String profVisibility = obj.getProfiledVisibility();
		if (profVisibility == null || profVisibility.trim().equals("")) {
			logger.debug("Biobject with label [" + obj.getLabel() + "] has no profile visibility contraints.");
			monitor.stop();
			return true;
		}
		logger.debug("Biobject with label [" + obj.getLabel() + "] has profile visibility contraints = [" + profVisibility + "]");
		String[] constraints = profVisibility.split(" AND ");
		for (int i = 0; i < constraints.length; i++) {
			String constraint = constraints[i];
			logger.debug("Examining constraint [" + constraint + "] ...");
			int index = constraint.indexOf("=");
			if (index == -1) {
				logger.error("Constraint [" + constraint + "] is not correct!! It should have the syntax PROFILE_ATTRIBUTE_NAME=VALUE. It will be ignored.");
				continue;
			}
			String profileAttrName = constraint.substring(0, index).trim();
			String value = constraint.substring(index + 1).trim();
			if (!profile.getUserAttributeNames().contains(profileAttrName)) {
				logger.debug("User profile hasn't the required profile attribute [" + profileAttrName + "], it does not satisfy constraint");
				toReturn = false;
				break;
			}
			Object profileAttr = profile.getUserAttribute(profileAttrName);
			if (profileAttr == null) {
				logger.debug("User profile attribute [" + profileAttrName + "] is null, it does not satisfy constraint");
				toReturn = false;
				break;
			}
			String profileAttrStr = profileAttr.toString();
			if (profileAttrStr.startsWith("{")) {
				// the profile attribute is multi-value
				String[] values = null;
				try {
					values = GeneralUtilities.findAttributeValues(profileAttrStr);
				} catch (Exception e) {
					logger.error("Error while reading profile attribute", e);
					logger.debug("User profile attribute [" + profileAttrName + "] does not satisfy constraint");
					toReturn = false;
					break;
				}
				if (!Arrays.asList(values).contains(value)) {
					logger.debug("User profile attribute [" + profileAttrName + "] does not contain [" + value + "] value, it does not satisfy constraint");
					toReturn = false;
					break;
				}
			} else {
				// the profile attribute is single-value
				if (!profileAttrStr.equals(value)) {
					logger.debug("User profile attribute [" + profileAttrName + "] is not equal to [" + value + "], it does not satisfy constraint");
					toReturn = false;
					break;
				}
			}
		}
		logger.debug("OUT.canSee=" + toReturn);
		monitor.stop();
		return toReturn;
	}

	/**
	 * returns the list of correct roles of the input profile for the execution of the document with the specified input
	 * 
	 * @param objectId
	 *            the document id
	 * @param profile
	 *            the user profile
	 * @return the list of correct roles of the input profile for the execution of the document with the specified input
	 * @throws EMFUserError
	 * @throws EMFInternalError
	 */
	public static List getCorrectRolesForExecution(Integer objectId, IEngUserProfile profile) throws EMFInternalError, EMFUserError {
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.getCorrectRolesForExecution");
		logger.debug("IN");
		List correctRoles = null;
		if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV) || profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
				|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
			logger.debug("User is able to execute action");
			correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(objectId, profile);
		} else {
			logger.debug("User is NOT able to execute action");
			correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(objectId);
		}
		logger.debug("OUT");
		monitor.stop();
		return correctRoles;
	}

	/**
	 * Retrieves the correct permission on folder that the user must have in order to execute the document: eg: document state = REL --> permission to EXECUTION
	 * document state = DEV --> permission to DEVELOPMENT document state = TEST --> permission to TEST
	 * 
	 * @param documentState
	 *            The document state
	 * @return the permission required to execute the document
	 */
	public static String getPermissionFromDocumentState(String documentState) {
		if (SpagoBIConstants.DOC_STATE_REL.equals(documentState)) {
			return SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE;
		}
		if (SpagoBIConstants.DOC_STATE_DEV.equals(documentState)) {
			return SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP;
		}
		if (SpagoBIConstants.DOC_STATE_TEST.equals(documentState)) {
			return SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST;
		}
		throw new SpagoBIRuntimeException("Document state [" + documentState + "] not valid!!");
	}

	/**
	 * Returns true if the user in input is able to delete the input object in the specified position (folder)
	 * 
	 * @param biobjectId
	 *            The id of the document to be deleted
	 * @param profile
	 *            The user profile object
	 * @param lowFunctionality
	 *            The folder
	 * @return true if the user in input is able to delete the input object in the specified position (folder)
	 */
	public static boolean canDeleteBIObject(int biobjectId, IEngUserProfile profile, LowFunctionality lowFunctionality) {
		logger.debug("IN");
		boolean canDelete = false;
		try {
			Assert.assertNotNull(profile, "User profile object in input is null");
			Assert.assertNotNull(lowFunctionality, "LowFunctionality object in input is null");
			logger.debug("Evaulating deletion permission for user [" + ((UserProfile) profile).getUserId() + "] on folder [" + lowFunctionality.getPath()
					+ "] for document with id [" + biobjectId + "] ...");
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				logger.debug("User is administrator, therefore he can delete it");
				canDelete = true;
			} else {
				logger.debug("User isn't an administrator");
				// if user can dev the document and in folder, he can delete it
				if (canDev(lowFunctionality.getId(), profile) && canDevBIObject(biobjectId, profile)) {
					logger.debug("User can develop document, therefore he can delete it");
					canDelete = true;
				} else {
					BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobjectId);
					String userId = ((UserProfile) profile).getUserId().toString();
					if (userId.equals(obj.getCreationUser())) {
						logger.debug("User is the creator of the document, therefore he can delete it");
						canDelete = true;
					} else {
						boolean isInPersonalFolder = UserUtilities.isPersonalFolder(lowFunctionality, (UserProfile) profile);
						if (isInPersonalFolder) {
							logger.debug("Folder is personal folder, therefore user can delete it");
							canDelete = true;
						}
					}
				}
			}
			logger.debug("OUT : returning " + canDelete);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while evaluating deletion permission", e);
		}
		return canDelete;
	}

	/**
	 * Returns true if the user in input is able to delete the input object everywhere
	 * 
	 * @param biobjectId
	 *            The id of the document to be deleted
	 * @param profile
	 *            The user profile object
	 * @return true if the user in input is able to delete the input object everywhere
	 */
	public static boolean canDeleteBIObject(int biobjectId, IEngUserProfile profile) {
		logger.debug("IN");
		boolean canDelete = false;
		try {
			Assert.assertNotNull(profile, "User profile object in input is null");
			logger.debug("Evaulating deletion permission for user [" + ((UserProfile) profile).getUserId() + "] for document with id [" + biobjectId + "] ...");
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				logger.debug("User is administrator, therefore he can delete it");
				canDelete = true;
			} else {
				logger.debug("User isn't an administrator");
				// if user can dev the document and in folder, he can delete it
				if (canDevBIObject(biobjectId, profile)) {
					logger.debug("User can develop document, therefore he can delete it");
					canDelete = true;
				} else {
					BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(biobjectId);
					String userId = ((UserProfile) profile).getUserId().toString();
					if (userId.equals(obj.getCreationUser())) {
						logger.debug("User is the creator of the document, therefore he can delete it");
						canDelete = true;
					} else {
						// if the document is ONLY inside the personal folder, the user can delete it
						List folders = obj.getFunctionalities();
						if (folders.size() == 1) {
							Integer folderId = (Integer) folders.get(0);
							LowFunctionality lowFunctionality = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
							boolean isInPersonalFolder = UserUtilities.isPersonalFolder(lowFunctionality, (UserProfile) profile);
							if (isInPersonalFolder) {
								logger.debug("Folder is personal folder, therefore user can delete it");
								canDelete = true;
							}
						}
					}
				}
			}
			logger.debug("OUT : returning " + canDelete);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while evaluating deletion permission", e);
		}
		return canDelete;
	}

	/**
	 * Returns true if the user in input is able to clone the input object everywhere
	 * 
	 * It consider the same conditions of the delete operation
	 * 
	 * @param biobjectId
	 *            The id of the document to be cloned
	 * @param profile
	 *            The user profile object
	 * @return true if the user in input is able to clone the input object everywhere
	 */
	public static boolean canCloneBIObject(int biobjectId, IEngUserProfile profile) {
		logger.debug("IN");
		return canDeleteBIObject(biobjectId, profile);
	}

	/**
	 * Returns true if the user in input is able to clone the input object in the specified position (folder)
	 * 
	 * It consider the same conditions of the delete operation
	 * 
	 * @param biobjectId
	 *            The id of the document to be cloned
	 * @param profile
	 *            The user profile object
	 * @param lowFunctionality
	 *            The folder
	 * @return true if the user in input is able to clone the input object in the specified position (folder)
	 */
	public static boolean canCloneBIObject(int biobjectId, IEngUserProfile profile, LowFunctionality lowFunctionality) {
		logger.debug("IN");
		return canDeleteBIObject(biobjectId, profile, lowFunctionality);
	}

	/**
	 * Check if the user can execute the required document. It checks the state of the document and its position on folders, and look for user permissions. It
	 * also checks if behavioural model is set properly (i.e. the user has valid roles for execution).
	 * 
	 * @param obj
	 *            The document to be executed
	 * @param profile
	 *            The user profile object
	 * @return true if the user can execute the required document, false otherwise
	 * @throws EMFInternalError
	 * @throws EMFUserError
	 */
	public static boolean canExec(BIObject obj, IEngUserProfile profile) throws EMFInternalError, EMFUserError {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.core.ObjectAccessVerifier.canExec(BIObject obj, IEngUserProfile profile)");
		boolean canExec = false;
		String state = obj.getStateCode();

		List foldersId = obj.getFunctionalities();
		if (foldersId == null || foldersId.size() == 0) {
			logger.warn("BIObject does not belong to any functionality!!");
			monitor.stop();
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "BIObject does not belong to any functionality!!");
		}

		boolean canExecByStateAndFolders = false;
		if ("SUSP".equalsIgnoreCase(state)) {
			// only admin can exec suspended document
			canExecByStateAndFolders = profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN);
		}

		Iterator foldersIdIt = foldersId.iterator();
		while (foldersIdIt.hasNext()) {
			Integer folderId = (Integer) foldersIdIt.next();
			boolean canDev = canDev(state, folderId, profile);
			if (canDev) {
				canExecByStateAndFolders = true;
				break;
			}
			boolean canTest = canTest(state, folderId, profile);
			if (canTest) {
				canExecByStateAndFolders = true;
				break;
			}
			boolean canExecOnFolder = canExec(state, folderId, profile);
			if (canExecOnFolder) {
				// administrators, developers, testers, behavioural model
				// administrators can see that document
				if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) // for administrators
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV) // for developers
						|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST) // for testers
						|| profile.isAbleToExecuteAction(SpagoBIConstants.PARAMETER_MANAGEMENT)) { // for behavioral model administrators
					canExecByStateAndFolders = true;
				} else {
					canExecByStateAndFolders = checkProfileVisibility(obj, profile);
				}
				break;
			}
		}

		if (canExecByStateAndFolders) {
			Integer id = obj.getId();
			// get the correct roles for execution
			List correctRoles = null;
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN))
				correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id, profile);
			else
				correctRoles = DAOFactory.getBIObjectDAO().getCorrectRolesForExecution(id);
			logger.debug("correct roles for execution retrived " + correctRoles);
			if (correctRoles == null || correctRoles.size() == 0) {
				logger.error("Document [" + obj.getLabel() + "] cannot be executed by no role of the user [" + ((UserProfile) profile).getUserId() + "]");
				canExec = false;
			} else {
				logger.debug("Document [" + obj.getLabel() + "] can be executed by the user [" + ((UserProfile) profile).getUserId() + "]");
				canExec = true;
			}
		} else {
			logger.error("User [" + ((UserProfile) profile).getUserId() + "] cannot execute the document [" + obj.getLabel()
					+ "] according to document's state and his permission on folders");
			canExec = false;
		}

		monitor.stop();
		logger.debug("OUT.canExec=" + canExec);
		return canExec;
	}

	static boolean isUserPersonalFolder(Integer folderId, IEngUserProfile profile) {
		logger.debug("IN");
		boolean toReturn = false;
		try {
			LowFunctionality folder = null;
			folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
			if (folder.getCodType().equalsIgnoreCase("USER_FUNCT") && folder.getName().equalsIgnoreCase(((UserProfile) profile).getUserId().toString())) {
				toReturn = true;
				logger.debug("User " + profile.getUserUniqueIdentifier() + " is in its personal folder");
			}

		} catch (Exception e) {
			logger.error("Exception in loadLowFunctionalityByID", e);
			return false;
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

}
