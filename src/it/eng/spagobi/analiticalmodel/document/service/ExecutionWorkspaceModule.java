/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class ExecutionWorkspaceModule extends AbstractModule {
	private static transient Logger logger = Logger.getLogger(ExecutionWorkspaceModule.class);
	public static final String MODULE_PAGE = "ExecutionWorkspacePage";
	
	protected List firtsLevelExecutableFolders = new ArrayList();
	protected List subTree = new ArrayList();
	protected String basePath = null;
	protected ILowFunctionalityDAO functionDAO = null;
	protected IEngUserProfile profile = null;
	protected String executionObjectLabel = null; 
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		debug("service", "Enter service method");
		// finds the id of the document to be executed+
		executionObjectLabel = (String) request.getAttribute(ObjectsTreeConstants.OBJECT_LABEL);
		// finds the user profile
		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		SessionContainer permanentSession = session.getPermanentContainer();
		profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		// loads first level functionalities
		functionDAO = DAOFactory.getLowFunctionalityDAO();
		LowFunctionality root = functionDAO.loadRootLowFunctionality(false);
		if (root == null) {
			logger.error("Missing root functionality!");
			throw new Exception("Missing root functionality!");
		}
		List firstLevelFunctions = functionDAO.loadChildFunctionalities(root.getId(), false);
		// filters the first level functionalities by the profile execution permission
		Iterator it = firstLevelFunctions.iterator();
		while (it.hasNext()) {
			LowFunctionality aFolder = (LowFunctionality) it.next();
			if (ObjectsAccessVerifier.canExec(aFolder, profile)) firtsLevelExecutableFolders.add(aFolder);
		}
		if (firtsLevelExecutableFolders.size() == 0) {
			logger.warn("The user has no executable folders");
			exit(response);
		}
		
		// finds the base folder between the filtered folders specified by request
		String folderPath = (String) request.getAttribute(TreeObjectsModule.PATH_SUBTREE);
		LowFunctionality baseFolder = null;
		if (folderPath != null) {
			it = firtsLevelExecutableFolders.iterator();
			while (it.hasNext()) {
				LowFunctionality aFolder = (LowFunctionality) it.next();
				if (aFolder.getPath().equals(folderPath)) {
					baseFolder = aFolder;
					break;
				}
			}
		}
		// if the folder is not specified by request, it is considered the firts of the executable folders
		if (baseFolder == null) {
			baseFolder = (LowFunctionality) firtsLevelExecutableFolders.get(0);
		}
		basePath = baseFolder.getPath();
		findsExecutionTree(baseFolder);
		
		exit(response);
	}

	private void findsExecutionTree(LowFunctionality folder) throws EMFUserError {
		if (!ObjectsAccessVerifier.canExec(folder, profile)) return;
		// reloads folder including biobjects
		folder = functionDAO.loadLowFunctionalityByID(folder.getId(), true);
		subTree.add(folder);
		// object not in REL state are not considered
		List objects = folder.getBiObjects();
		List releasedObjects = new ArrayList();
		Iterator it = objects.iterator();
		while (it.hasNext()) {
			BIObject obj = (BIObject) it.next();
			Integer visible = obj.getVisible();
			if (obj.getStateCode().equalsIgnoreCase("REL") && (visible == null || visible.intValue() != 0)) 
					releasedObjects.add(obj);
		}
		folder.setBiObjects(releasedObjects);
		// iterator on the child folders
		List subFolders = functionDAO.loadChildFunctionalities(folder.getId(), false);
		it = subFolders.iterator();
		while (it.hasNext()) {
			LowFunctionality aSubFolder = (LowFunctionality) it.next();
			findsExecutionTree(aSubFolder);
		}
		
	}
	
	private void exit(SourceBean response) throws SourceBeanException {
		response.setAttribute("FIRST_LEVEL_FOLDERS", firtsLevelExecutableFolders);
		response.setAttribute("SUB_TREE", subTree);
		response.setAttribute(TreeObjectsModule.PATH_SUBTREE, basePath);
		if (executionObjectLabel != null) {
			// a document was selected, document execution can start
			response.setAttribute(ObjectsTreeConstants.OBJECT_LABEL, executionObjectLabel);
		    // identity string for object execution
		    UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		    UUID uuid = uuidGen.generateTimeBasedUUID();
		    String requestIdentity = uuid.toString();
		    requestIdentity = requestIdentity.replaceAll("-", "");
		    response.setAttribute("spagobi_execution_id", requestIdentity);
		}
		debug("exit", "Exit from module");
	}
	
	/**
	 * Trace a debug message into the log
	 * @param method Name of the method to store into the log
	 * @param message Message to store into the log
	 */
	private void debug(String method, String message) {
		logger.debug(message);
	}
	
}
