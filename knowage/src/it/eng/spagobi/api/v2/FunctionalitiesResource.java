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

import java.util.ArrayList;
import java.util.List;

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

/**
 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
 */
@Path("/2.0/functionalities")
@ManageAuthorization
public class FunctionalitiesResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	/**
	 * Getting list of all functionalities. Arrays of Roles that belong to one
	 * functionality are implemented to be like: One Role only with id and name
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

	private JSONArray makeShortCreatRolesOfFolder(LowFunctionality lowFunctionality) {
		Role[] arrayCreat = lowFunctionality.getCreateRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayCreat.length; i++) {
			Role aRole = arrayCreat[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private JSONArray makeShortExecRolesOfFolder(LowFunctionality lowFunctionality) {
		Role[] arrayExec = lowFunctionality.getExecRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayExec.length; i++) {
			Role aRole = arrayExec[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private JSONArray makeShortTestRolesOfFolder(LowFunctionality lowFunctionality) {
		Role[] arrayTest = lowFunctionality.getTestRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayTest.length; i++) {
			Role aRole = arrayTest[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filteredListArray.put(aRoleJson);
		}
		return filteredListArray;
	}

	private JSONArray makeShortDevRolesOfFolder(LowFunctionality lowFunctionality) {
		Role[] arrayDev = lowFunctionality.getDevRoles();
		JSONArray filteredListArray = new JSONArray();
		for (int i = 0; i < arrayDev.length; i++) {
			Role aRole = arrayDev[i];
			JSONObject aRoleJson = new JSONObject();
			try {
				aRoleJson.put("id", aRole.getId());
				aRoleJson.put("name", aRole.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			String errorString = "sbi.folder.load.folder.moveUp";
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
			lowFunctionality.setDescription(paramsObj.getString("code"));
			lowFunctionality.setName(paramsObj.getString("name"));
			lowFunctionality.setPath(paramsObj.getString("path"));
			lowFunctionality.setParentId(paramsObj.getInt("parentId"));

			objDao.insertLowFunctionality(lowFunctionality, getUserProfile());
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
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
			lowFunctionality.setDescription(paramsObj.getString("code"));
			lowFunctionality.setName(paramsObj.getString("name"));
			lowFunctionality.setPath(paramsObj.getString("path"));
			lowFunctionality.setParentId(paramsObj.getInt("parentId"));
			lowFunctionality.setProg(paramsObj.getInt("prog"));

			objDao.modifyLowFunctionality(lowFunctionality);
			return Response.ok().build();

		} catch (Exception e) {
			e.printStackTrace();
			String errorString = "sbi.folder.modify.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
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
			e.printStackTrace();
			String errorString = "sbi.folder.delete.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}
}
