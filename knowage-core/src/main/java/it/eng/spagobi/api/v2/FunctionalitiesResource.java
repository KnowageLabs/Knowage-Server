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
package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
 */
@Path("/2.0/functionalities")
@ManageAuthorization
public class FunctionalitiesResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	/**
	 * Getting list of all functionalities. Arrays of Roles that belong to one functionality are implemented to be like: One Role only with id and name
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getFolders(@DefaultValue("false") @QueryParam("includeDocs") Boolean recoverBIObjects, @QueryParam("perm") String permissionOnFolder,
			@QueryParam("dateFilter") String dateFilter) {
		logger.debug("IN");

		try {
			UserProfile profile = getUserProfile();
			ILowFunctionalityDAO dao = DAOFactory.getLowFunctionalityDAO();
			dao.setUserProfile(profile);
			List<LowFunctionality> allFolders = new ArrayList<>();
			if (dateFilter != null) {
				allFolders = dao.loadAllLowFunctionalities(dateFilter);
			} else {
				allFolders = dao.loadAllLowFunctionalities(recoverBIObjects);
			}
			List<LowFunctionality> folders = new ArrayList<LowFunctionality>();

			if (permissionOnFolder != null && !permissionOnFolder.isEmpty()) {
				for (LowFunctionality lf : allFolders) {
					if (ObjectsAccessVerifier.canSee(lf, profile) && checkPermissionOnFolder(permissionOnFolder, lf, profile)) {
						folders.add(lf);
					}
				}
			} else {
				for (LowFunctionality lf : allFolders) {
					if (ObjectsAccessVerifier.canSee(lf, profile)) {
						folders.add(lf);
					}
				}
			}
			List<LowFunctionality> newListOfFolders = new ArrayList<LowFunctionality>();
			JSONArray arrayDev = null;
			JSONArray arrayTest = null;
			JSONArray arrayExec = null;
			JSONArray arrayCreat = null;
			JSONArray filteredListArray = new JSONArray();
			for (LowFunctionality lowFunctionality : folders) {
				arrayDev = makeShortDevRolesOfFolder(lowFunctionality);
				arrayTest = makeShortTestRolesOfFolder(lowFunctionality);
				arrayExec = makeShortExecRolesOfFolder(lowFunctionality);
				arrayCreat = makeShortCreatRolesOfFolder(lowFunctionality);
				JSONObject folderJson = new JSONObject();
				folderJson.put("biObjects", lowFunctionality.getBiObjects());
				folderJson.put("codType", lowFunctionality.getCodType());
				folderJson.put("description", lowFunctionality.getDescription());
				folderJson.put("id", lowFunctionality.getId());
				folderJson.put("name", lowFunctionality.getName());

				if (lowFunctionality.getParentId() == null) {
					folderJson.put("parentId", JSONObject.NULL);
				} else {
					folderJson.put("parentId", lowFunctionality.getParentId());
				}

				folderJson.put("path", lowFunctionality.getPath());

				folderJson.put("code", lowFunctionality.getCode());
				folderJson.put("prog", lowFunctionality.getProg());
				folderJson.put("testRoles", arrayTest);
				folderJson.put("devRoles", arrayDev);
				folderJson.put("execRoles", arrayExec);
				folderJson.put("createRoles", arrayCreat);
				filteredListArray.put(folderJson);

			}

			return Response.ok(filteredListArray.toString()).build();
		} catch (Exception e) {
			String errorString = "sbi.folder.load.folders.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	private JSONArray makeShortCreatRolesOfFolder(LowFunctionality lowFunctionality) throws SerializationException {
		Role[] arrayCreat = lowFunctionality.getCreateRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayCreat.length; i++) {
			Role aRole = arrayCreat[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				throw new SerializationException("An error occurred while serializing role: " + aRole.getName(), e);
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private JSONArray makeShortExecRolesOfFolder(LowFunctionality lowFunctionality) throws SerializationException {
		Role[] arrayExec = lowFunctionality.getExecRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayExec.length; i++) {
			Role aRole = arrayExec[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				throw new SerializationException("An error occurred while serializing role: " + aRole.getName(), e);
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private JSONArray makeShortTestRolesOfFolder(LowFunctionality lowFunctionality) throws SerializationException {
		Role[] arrayTest = lowFunctionality.getTestRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayTest.length; i++) {
			Role aRole = arrayTest[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				throw new SerializationException("An error occurred while serializing role: " + aRole.getName(), e);
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private JSONArray makeShortDevRolesOfFolder(LowFunctionality lowFunctionality) throws SerializationException {
		Role[] arrayDev = lowFunctionality.getDevRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayDev.length; i++) {
			Role aRole = arrayDev[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				throw new SerializationException("An error occurred while serializing role: " + aRole.getName(), e);
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private boolean checkPermissionOnFolder(String permission, LowFunctionality lf, UserProfile profile) {
		boolean result = false;

		switch (permission.toUpperCase()) {
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP:
			result = ObjectsAccessVerifier.canDev(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST:
			result = ObjectsAccessVerifier.canTest(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE:
			result = ObjectsAccessVerifier.canExec(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE:
			result = ObjectsAccessVerifier.canCreate(lf, profile);
			break;
		}

		return result;
	}

	@GET
	@Path("forsharing/{objectId}")
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getFunctionalitiesForSharing(@PathParam("objectId") Integer objectId) {
		logger.debug("IN");
		List<LowFunctionality> folders = new ArrayList<>();
		try {
			UserProfile profile = getUserProfile();
			ILowFunctionalityDAO dao = DAOFactory.getLowFunctionalityDAO();
			dao.setUserProfile(profile);
			List<LowFunctionality> foldersForSharing = dao.loadFunctionalitiesForSharing(objectId);
			for (LowFunctionality folder : foldersForSharing) {
				if (ObjectsAccessVerifier.canSee(folder, profile))
					folders.add(folder);
			}
			return Response.ok(folders).build();
		} catch (Exception e) {
			String errorString = "Cannot get available folders for sharing";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Service that moves functionality up
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@Path("moveUp/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONALITIES_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getLowFunctMoveUp(@PathParam("id") Integer id) {

		try {
			DAOFactory.getLowFunctionalityDAO().moveUpLowFunctionality(id);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.folder.load.folder.moveUp";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that get parent folder of selected folder
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@Path("getParent/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONALITIES_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getParent(@PathParam("id") Integer id) {

		try {
			LowFunctionality l = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(id, false);
			return Response.ok(l).build();

		} catch (Exception e) {
			String errorString = "sbi.folder.load.parent.folder";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that moves functionality down
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@Path("moveDown/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONALITIES_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getLowFunctMoveDown(@PathParam("id") Integer id) {

		try {
			DAOFactory.getLowFunctionalityDAO().moveDownLowFunctionality(id);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.folder.load.folder.moveDown";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that creates new functionality
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONALITIES_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertLowFunctionality(@javax.ws.rs.core.Context HttpServletRequest req) {

		ILowFunctionalityDAO objDao = null;

		IRoleDAO roleDao = null;
		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);
			JSONArray devRoles = paramsObj.getJSONArray("devRoles");
			JSONArray testRoles = paramsObj.getJSONArray("testRoles");
			JSONArray execRoles = paramsObj.getJSONArray("execRoles");
			JSONArray creatRoles = paramsObj.getJSONArray("createRoles");
			roleDao = DAOFactory.getRoleDAO();
			ArrayList<Role> devRolesArrayList = new ArrayList<Role>();
			ArrayList<Role> testRolesArrayList = new ArrayList<Role>();
			ArrayList<Role> execRolesArrayList = new ArrayList<Role>();
			ArrayList<Role> creatRolesArrayList = new ArrayList<Role>();
			Role[] devRolesArray = new Role[devRoles.length()];
			for (int i = 0; i < devRoles.length(); i++) {
				int roleID = devRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				devRolesArrayList.add(r);

			}
			Role[] testRolesArray = new Role[testRoles.length()];
			for (int i = 0; i < testRoles.length(); i++) {
				int roleID = testRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				testRolesArrayList.add(r);

			}
			Role[] execRolesArray = new Role[execRoles.length()];
			for (int i = 0; i < execRoles.length(); i++) {
				int roleID = execRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				execRolesArrayList.add(r);

			}
			Role[] creatRolesArray = new Role[creatRoles.length()];
			for (int i = 0; i < creatRoles.length(); i++) {
				int roleID = creatRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				creatRolesArrayList.add(r);

			}
			objDao = DAOFactory.getLowFunctionalityDAO();

			objDao.setUserProfile(getUserProfile());
			LowFunctionality lowFunctionality = new LowFunctionality();

			lowFunctionality.setCode(paramsObj.getString("code"));
			lowFunctionality.setCodType(paramsObj.getString("codeType"));
			lowFunctionality.setCreateRoles(creatRolesArrayList.toArray(creatRolesArray));
			lowFunctionality.setTestRoles(testRolesArrayList.toArray(testRolesArray));
			lowFunctionality.setExecRoles(execRolesArrayList.toArray(execRolesArray));
			lowFunctionality.setDevRoles(devRolesArrayList.toArray(devRolesArray));
			lowFunctionality.setDescription(paramsObj.getString("description"));
			lowFunctionality.setName(paramsObj.getString("name"));
			lowFunctionality.setPath(paramsObj.getString("path"));
			lowFunctionality.setParentId(paramsObj.getInt("parentId"));

			lowFunctionality = objDao.insertLowFunctionality(lowFunctionality, getUserProfile());
			return Response.ok(lowFunctionality).build();
		} catch (Exception e) {
			String errorString = "sbi.folder.save.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that updates functionality
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@PUT
	@Path("/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.FUNCTIONALITIES_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateLowFunctionality(@PathParam("id") Integer id, @javax.ws.rs.core.Context HttpServletRequest req) {
		ILowFunctionalityDAO objDao = null;
		IRoleDAO roleDao = null;
		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);
			JSONArray devRoles = paramsObj.getJSONArray("devRoles");
			JSONArray testRoles = paramsObj.getJSONArray("testRoles");
			JSONArray execRoles = paramsObj.getJSONArray("execRoles");
			JSONArray creatRoles = paramsObj.getJSONArray("createRoles");
			roleDao = DAOFactory.getRoleDAO();
			ArrayList<Role> devRolesArrayList = new ArrayList<Role>();
			ArrayList<Role> testRolesArrayList = new ArrayList<Role>();
			ArrayList<Role> execRolesArrayList = new ArrayList<Role>();
			ArrayList<Role> creatRolesArrayList = new ArrayList<Role>();
			Role[] devRolesArray = new Role[devRoles.length()];
			for (int i = 0; i < devRoles.length(); i++) {
				int roleID = devRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				devRolesArrayList.add(r);

			}
			Role[] testRolesArray = new Role[testRoles.length()];
			for (int i = 0; i < testRoles.length(); i++) {
				int roleID = testRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				testRolesArrayList.add(r);

			}
			Role[] execRolesArray = new Role[execRoles.length()];
			for (int i = 0; i < execRoles.length(); i++) {
				int roleID = execRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				execRolesArrayList.add(r);

			}
			Role[] creatRolesArray = new Role[creatRoles.length()];
			for (int i = 0; i < creatRoles.length(); i++) {
				int roleID = creatRoles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				creatRolesArrayList.add(r);

			}
			objDao = DAOFactory.getLowFunctionalityDAO();

			objDao.setUserProfile(getUserProfile());
			LowFunctionality lowFunctionality = objDao.loadLowFunctionalityByID(paramsObj.getInt("id"), false);

			lowFunctionality.setCode(paramsObj.getString("code"));
			lowFunctionality.setCodType(paramsObj.getString("codeType"));
			lowFunctionality.setCreateRoles(creatRolesArrayList.toArray(creatRolesArray));
			lowFunctionality.setTestRoles(testRolesArrayList.toArray(testRolesArray));
			lowFunctionality.setExecRoles(execRolesArrayList.toArray(execRolesArray));
			lowFunctionality.setDevRoles(devRolesArrayList.toArray(devRolesArray));
			lowFunctionality.setDescription(paramsObj.optString("description"));
			lowFunctionality.setName(paramsObj.getString("name"));
			lowFunctionality.setPath(paramsObj.getString("path"));
			lowFunctionality.setParentId(paramsObj.getInt("parentId"));
			lowFunctionality.setProg(paramsObj.getInt("prog"));

			objDao.modifyLowFunctionality(lowFunctionality);
			Set set = new HashSet();
			loadRolesToErase(lowFunctionality, set);
			DAOFactory.getLowFunctionalityDAO().deleteInconsistentRoles(set);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.folder.modify.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}

	/**
	 * Defines all roles that have to be erased in order to keep functionalities tree consistence. When we leave some permissions to a functionality, those
	 * permissions will not be assignable to all the children functionality. If any child has a permission that his parent anymore has, this permission mus be
	 * deleted for all father's children and descendants. This metod recusively scans all father's descendants and saves inside a Set all roles that must be
	 * erased from the Database.
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
	public void loadRolesToErase(LowFunctionality lowFuncParent, Set rolesToErase) throws EMFUserError {
		String parentPath = lowFuncParent.getPath();
		// ArrayList childs =
		// DAOFactory.getFunctionalityCMSDAO().recoverChilds(parentPath);
		List childs = DAOFactory.getLowFunctionalityDAO().loadSubLowFunctionalities(parentPath, false);
		if (childs.size() != 0) {
			Iterator i = childs.iterator();
			while (i.hasNext()) {
				LowFunctionality childNode = (LowFunctionality) i.next();
				String childPath = childNode.getPath();
				// LowFunctionality lowFuncParent =
				// DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(parentPath);
				LowFunctionality lowFuncChild = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(childPath, false);
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
						if (!isParentRule(rule, lowFuncParent, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, testChildRoles[j].getId());
							roles.add(2, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(lowFuncChild, rule, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST);
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
						if (!isParentRule(rule, lowFuncParent, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, devChildRoles[j].getId());
							roles.add(2, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(lowFuncChild, rule, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP);
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
						if (!isParentRule(rule, lowFuncParent, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, execChildRoles[j].getId());
							roles.add(2, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(lowFuncChild, rule, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE);
							// rolesToErase.put(lowFuncChild.getId(),execChildRoles[j].getId());
							// DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,execChildRoles[j].getId());
						}
					}
					// control for development roles
					Role[] createChildRoles = lowFuncChild.getCreateRoles();
					for (int j = 0; j < createChildRoles.length; j++) {
						String rule = createChildRoles[j].getId().toString();
						if (!isParentRule(rule, lowFuncParent, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
							ArrayList roles = new ArrayList();
							roles.add(0, lowFuncChild.getId());
							roles.add(1, createChildRoles[j].getId());
							roles.add(2, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE);
							rolesToErase.add(roles);
							lowFuncChild = eraseRolesFromFunctionality(lowFuncChild, rule, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE);
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
	 * Erases the defined input role from a functionality object, if this one has the role.The updated functionality object is returned.
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
	public LowFunctionality eraseRolesFromFunctionality(LowFunctionality func, String roleId, String permission) {
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

	/**
	 * Controls if a particular role belongs to the parent functionality. It is called inside functionalities Jsp in ordet to identify those roles that a child
	 * functionality is able to select.
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
	public boolean isParentRule(String rule, LowFunctionality parentLowFunct, String permission) {
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
		} else if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
			Role[] execRolesObj = parentLowFunct.getExecRoles();
			String[] execRules = new String[execRolesObj.length];
			for (int i = 0; i < execRolesObj.length; i++) {
				execRules[i] = execRolesObj[i].getId().toString();
				if (rule.equals(execRules[i])) {
					isParent = true;
				}
			}
		} else if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
			Role[] testRolesObj = parentLowFunct.getTestRoles();
			String[] testRules = new String[testRolesObj.length];
			for (int i = 0; i < testRolesObj.length; i++) {
				testRules[i] = testRolesObj[i].getId().toString();
				if (rule.equals(testRules[i])) {
					isParent = true;
				}
			}
		} else if (permission.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
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
	 * Service that deletes functionality
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@DELETE
	@Path("/{id}")
	public Response deleteFunctionality(@PathParam("id") Integer id) {

		ILowFunctionalityDAO iLowFunctionalityDAO = null;

		try {
			LowFunctionality lowFunctionality = new LowFunctionality();
			lowFunctionality.setId(id);
			iLowFunctionalityDAO = DAOFactory.getLowFunctionalityDAO();
			iLowFunctionalityDAO.setUserProfile(getUserProfile());
			iLowFunctionalityDAO.eraseLowFunctionality(lowFunctionality, getUserProfile());

			return Response.ok().build();
		} catch (EMFUserError eMFUserError) {
			String errorString = eMFUserError.getDescription();
			logger.error(errorString, eMFUserError);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), eMFUserError);
		} catch (Exception e) {
			String errorString = "sbi.folder.delete.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}
}
