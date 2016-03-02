/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.documentsbrowser.utils;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.DocumentsJSONDecorator;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FolderContentUtil {

	// REQUEST PARAMETERS
	public static final String FOLDER_ID = "folderId";

	public static final String ROOT_NODE_ID = "rootNode";

	// logger component
	private static Logger logger = Logger.getLogger(FolderContentUtil.class);

	public JSONObject getDocuments() {
		return documents;
	}

	public JSONObject getFolders() {
		return folders;
	}

	private JSONObject documents;
	private JSONObject folders;

	public JSONObject getFolderContent(LowFunctionality folder, SourceBean request, SourceBean response, HttpServletRequest httpRequest,
			SessionContainer sessCont) throws Exception {

		List functionalities;
		List objects;
		boolean isHome = false;
		// Check if there is folder specified as home for the document browser (Property in SBI_CONFIG with label SPAGOBI.DOCUMENTBROWSER.HOME)
		if (folder == null) {
			Config documentBrowserHomeConfig = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("SPAGOBI.DOCUMENTBROWSER.HOME");
			if (documentBrowserHomeConfig != null) {
				if (documentBrowserHomeConfig.isActive()) {

					String folderLabel = documentBrowserHomeConfig.getValueCheck();

					if (!StringUtils.isEmpty(folderLabel)) {
						folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(folderLabel, false);
					}

				}
			}

		}
		// ------------------

		// getting default folder (root)
		// LowFunctionality rootFunct = DAOFactory.getLowFunctionalityDAO().loadRootLowFunctionality(false);
		if (folder == null || String.valueOf(folder.getId()).equalsIgnoreCase(ROOT_NODE_ID)) {
			folder = DAOFactory.getLowFunctionalityDAO().loadRootLowFunctionality(false);
			// folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(rootFunct.getId(), false);
		}

		SessionContainer permCont = sessCont.getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permCont.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		if (UserUtilities.isAdministrator(profile)) {
			isHome = UserUtilities.isAPersonalFolder(folder);
		} else {
			isHome = UserUtilities.isPersonalFolder(folder, (UserProfile) profile);
		}

		// Recursive view Management: Get all the documents inside a folder and his subfolders with a recursive visit
		List allSubDocuments = null;
		Config documentBrowserRecursiveConfig = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("SPAGOBI.DOCUMENTBROWSER.RECURSIVE");
		if (documentBrowserRecursiveConfig.isActive()) {
			String propertyValue = documentBrowserRecursiveConfig.getValueCheck();
			if ((!StringUtils.isEmpty(propertyValue)) && (propertyValue.equalsIgnoreCase("true"))) {
				allSubDocuments = getAllSubDocuments(String.valueOf(folder.getId()), profile, isHome);
			}
		}
		// ------------

		// getting children documents
		// LowFunctionality lowFunct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(functID, true);
		// objects = lowFunct.getBiObjects();
		if (allSubDocuments == null) {
			List tmpObjects = DAOFactory.getBIObjectDAO().loadBIObjects(folder.getId(), profile, isHome);
			objects = new ArrayList();
			if (tmpObjects != null) {
				for (Iterator it = tmpObjects.iterator(); it.hasNext();) {
					BIObject obj = (BIObject) it.next();
					if (ObjectsAccessVerifier.canSee(obj, profile)) {
						objects.add(obj);
					}
				}
			}
		} else {
			objects = allSubDocuments;
		}

		MessageBuilder m = new MessageBuilder();
		Locale locale = m.getLocale(httpRequest);
		JSONArray documentsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(objects, locale);
		DocumentsJSONDecorator.decorateDocuments(documentsJSON, profile, folder);

		JSONObject documentsResponseJSON = createJSONResponseDocuments(documentsJSON);

		// getting children folders
		/*
		 * if (isRoot) functionalities = DAOFactory.getLowFunctionalityDAO().loadUserFunctionalities(true, false, profile); else functionalities =
		 * DAOFactory.getLowFunctionalityDAO().loadChildFunctionalities(Integer.valueOf(functID), false);
		 */
		boolean recoverBiObjects = false;
		// for massive export must also get the objects to check if there are worksheets
		Collection userFunctionalities = profile.getFunctionalities();
		if (userFunctionalities.contains("DoMassiveExportFunctionality")) {
			recoverBiObjects = true;
		}

		functionalities = DAOFactory.getLowFunctionalityDAO().loadUserFunctionalities(folder.getId(), recoverBiObjects, profile);

		JSONArray foldersJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(functionalities, locale);

		JSONObject exportAction = new JSONObject();
		exportAction.put("name", "export");
		exportAction.put("description", "Export");

		JSONObject scheduleAction = new JSONObject();
		scheduleAction.put("name", "schedule");
		scheduleAction.put("description", "Schedule");

		// call check for worksheet presence only if user can eexecute massive export, otherwise jump over control
		if (userFunctionalities.contains("DoMassiveExportFunctionality")) {
			Map<String, Boolean> folderToWorksheet = checkIfWorksheetContained(functionalities);

			for (int i = 0; i < foldersJSON.length(); i++) {
				JSONObject folderJSON = foldersJSON.getJSONObject(i);
				String code = folderJSON.getString("code");
				Boolean isWorksheet = folderToWorksheet.get(code);
				if (isWorksheet) {
					folderJSON.getJSONArray("actions").put(exportAction);
					folderJSON.getJSONArray("actions").put(scheduleAction);
				}
			}
		}

		// Flat View Management: show only documents inside a folder and no subfolders
		JSONObject foldersResponseJSON;
		Config documentBrowserFlatConfig = DAOFactory.getSbiConfigDAO().loadConfigParametersByLabel("SPAGOBI.DOCUMENTBROWSER.FLAT");
		if (documentBrowserFlatConfig.isActive()) {
			String propertyValue = documentBrowserFlatConfig.getValueCheck();
			if ((!StringUtils.isEmpty(propertyValue)) && (propertyValue.equalsIgnoreCase("true"))) {
				foldersJSON = new JSONArray(); // set an empty array for hiding subfolders
			}
		}

		foldersResponseJSON = createJSONResponseFolders(foldersJSON);

		// version 4.0--------------------//
		// find add into folder grants

		JSONObject canAddResponseJSON = null;

		return createJSONResponse(foldersResponseJSON, documentsResponseJSON, canAddResponseJSON);
	}

	// Get All Documents inside a folder and his sub-folders with recursive visit
	private List getAllSubDocuments(String functID, IEngUserProfile profile, Boolean isHome) throws NumberFormatException, EMFUserError, EMFInternalError {
		List allDocuments = new ArrayList();

		List tmpObjects;

		tmpObjects = DAOFactory.getBIObjectDAO().loadBIObjects(Integer.valueOf(functID), profile, isHome);
		List objects = new ArrayList();
		if (tmpObjects != null) {
			for (Iterator it = tmpObjects.iterator(); it.hasNext();) {
				BIObject obj = (BIObject) it.next();
				if (ObjectsAccessVerifier.checkProfileVisibility(obj, profile))
					objects.add(obj);
			}
		}

		allDocuments.addAll(objects);

		List<LowFunctionality> functionalities = DAOFactory.getLowFunctionalityDAO().loadUserFunctionalities(Integer.valueOf(functID), true, profile);
		for (LowFunctionality functionality : functionalities) {
			Set folderDocuments = new HashSet();
			Set subDocuments = visitFolder(functionality.getId(), folderDocuments, profile);
			allDocuments.addAll(subDocuments);
		}

		return allDocuments;

	}

	public Set visitFolder(Integer functID, Set allDocuments, IEngUserProfile profile) throws EMFUserError, EMFInternalError, NumberFormatException {
		List tmpObjects;

		tmpObjects = DAOFactory.getBIObjectDAO().loadBIObjects(Integer.valueOf(functID), profile, false);
		List objects = new ArrayList();
		if (tmpObjects != null) {
			for (Iterator it = tmpObjects.iterator(); it.hasNext();) {
				BIObject obj = (BIObject) it.next();
				if (ObjectsAccessVerifier.checkProfileVisibility(obj, profile))
					objects.add(obj);
			}
		}

		allDocuments.addAll(objects);

		List<LowFunctionality> functionalities = DAOFactory.getLowFunctionalityDAO().loadUserFunctionalities(Integer.valueOf(functID), true, profile);
		for (LowFunctionality functionality : functionalities) {
			Set subDocuments = visitFolder(functionality.getId(), allDocuments, profile);
			allDocuments.addAll(subDocuments);
		}

		return allDocuments;

	}

	/**
	 * Creates a json array to display add button or not
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseForAdd(boolean canCreate) throws JSONException {
		JSONObject results;
		results = new JSONObject();
		results.put("can-add", Boolean.valueOf(canCreate));
		return results;
	}

	/**
	 * Creates a json array with children document informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createJSONResponseDocuments(JSONArray rows) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("title", "Documents");
		results.put("icon", "document.png");
		results.put("samples", rows);
		this.documents = results;
		return results;
	}

	/**
	 * Creates a json array with children folders informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createJSONResponseFolders(JSONArray rows) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("title", "Folders");
		results.put("icon", "folder.png");
		results.put("samples", rows);
		this.folders = results;
		return results;
	}

	/**
	 * Creates a json array with children document informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	public JSONObject createJSONResponse(JSONObject folders, JSONObject documents, JSONObject canAdd) throws JSONException {
		JSONObject results = new JSONObject();
		JSONArray folderContent = new JSONArray();

		folderContent.put(folders);
		folderContent.put(documents);
		if (canAdd != null) {
			folderContent.put(canAdd);
		}
		results.put("folderContent", folderContent);

		return results;
	}

	private Map checkIfWorksheetContained(List functionalities) throws SpagoBIException {
		logger.debug("IN");
		// link each functionality to bo0olean indicating if containing worksheets
		Domain worksheetDomain;
		try {
			worksheetDomain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue(SpagoBIConstants.BIOBJ_TYPE, SpagoBIConstants.WORKSHEET_TYPE_CODE);
		} catch (EMFUserError e) {
			logger.error("Could not recover Worksheet domain type", e);
			throw new SpagoBIException("Could not recover Worksheet domain type", e);
		}

		Map<String, Boolean> functWorksheet = new HashMap<String, Boolean>();
		for (Iterator iterator = functionalities.iterator(); iterator.hasNext();) {
			LowFunctionality lowFunc = (LowFunctionality) iterator.next();
			boolean isThereWorksheet = false;
			if (lowFunc.getBiObjects() != null) {
				for (Iterator iterator2 = lowFunc.getBiObjects().iterator(); iterator2.hasNext() && !isThereWorksheet;) {
					BIObject biObj = (BIObject) iterator2.next();
					Integer typeId = biObj.getBiObjectTypeID();
					if (typeId.equals(worksheetDomain.getValueId())) {
						isThereWorksheet = true;
					}
				}
			}
			logger.debug("functionality " + lowFunc.getCode() + " has worksheets inside? " + isThereWorksheet);
			functWorksheet.put(lowFunc.getCode(), isThereWorksheet);

		}
		logger.debug("OUT");
		return functWorksheet;
	}

	/**
	 * Returns true if the folder specified by folderIdStr exists and the user can see it, false otherwise
	 * 
	 * @param folderIdStr
	 *            The string representing the folder id
	 * @param profile
	 *            The user profile object
	 * @return true if the folder specified by folderIdStr exists and the user can see it, false otherwise
	 */
	public boolean checkRequiredFolder(String folderIdStr, IEngUserProfile profile) {
		try {
			int folderId = new Integer(folderIdStr);
			logger.debug("Folder id is " + folderId);
			LowFunctionality folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(folderId, false);
			logger.debug("Folder is " + folder);
			if (folder == null || !ObjectsAccessVerifier.canSee(folder, profile)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve folder informations", e);
		}

	}

	/**
	 * Returns true if the folder specified by folderIdStr exists and the user can see it, false otherwise
	 *
	 * @param folderIdStr
	 *            The string representing the folder id
	 * @param profile
	 *            The user profile object
	 * @return true if the folder specified by folderIdStr exists and the user can see it, false otherwise
	 */
	public boolean checkRequiredFolder(LowFunctionality folder, IEngUserProfile profile) {
		try {
			if (folder == null || !ObjectsAccessVerifier.canSee(folder, profile)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve folder informations", e);
		}
	}
}
