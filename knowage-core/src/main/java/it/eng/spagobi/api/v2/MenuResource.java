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

import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.toJsonTreeLowFunctionality;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.bo.MenuIcon;
import it.eng.spagobi.wapp.dao.IMenuDAO;

/**
 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
 */
@Path("/2.0/menu")
@ManageAuthorization
public class MenuResource extends AbstractSpagoBIResource {
	private final String charset = "; charset=UTF-8";

	/**
	 * Getting list of all menus. Arrays of Roles that belong to one menu are implemented to be like: One Role only with id and name
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@SuppressWarnings("unchecked")
	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getMenues() {
		logger.debug("IN");

		try {
			UserProfile profile = getUserProfile();
			IMenuDAO dao = DAOFactory.getMenuDAO();
			dao.setUserProfile(profile);

			List<Menu> allMenus = new ArrayList<>();

			allMenus = dao.loadAllMenues();

			return Response.ok(allMenus).build();
		} catch (Exception e) {
			String errorString = "sbi.menu.load.menus.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/functionalities")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getFunctionalities() {
		logger.debug("IN");
		JSONObject resp = new JSONObject();
		try {
			ILowFunctionalityDAO lowfuncdao = DAOFactory.getLowFunctionalityDAO();
			IEngUserProfile profile = getUserProfile();
			lowfuncdao.setUserProfile(profile);

			@SuppressWarnings("unchecked")
			List<LowFunctionality> functionalities = lowfuncdao.loadAllLowFunctionalities(false);

			JSONArray array = toJsonTreeLowFunctionality(functionalities);
			resp.put("functionality", array);
			return resp.toString();

		} catch (Exception e) {
			String errorString = "sbi.menu.load.menus.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/htmls")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getHTMLs() {
		logger.debug("IN");

		try {
			UserProfile profile = getUserProfile();
			IMenuDAO dao = DAOFactory.getMenuDAO();

			String resourcePath = SpagoBIUtilities.getResourcePath() + File.separatorChar + "static_menu";
			File dir = new File(resourcePath);
			String[] files = null;
			String[] filesHtmls = null;
			ArrayList<String> filesHtmlsArrayList = new ArrayList<>();
			int count = 0;
			if (!dir.isDirectory()) {
				FileUtils.forceMkdir(new File(resourcePath));
			}
			if (dir != null) {
				// get all avalaible files
				files = dir.list();

				for (int i = 0; i < files.length; i++) {
					String fileName = files[i];
					String ext = fileName.substring(fileName.indexOf(".") + 1);

					if (ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")) {
						count++;
					}
				}
				filesHtmls = new String[count];
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i];
					String ext = fileName.substring(fileName.indexOf(".") + 1);

					if (ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")) {
						filesHtmlsArrayList.add(fileName);
					}

				}
				filesHtmls = filesHtmlsArrayList.toArray(filesHtmls);
			}
			JSONArray njo = new JSONArray();

			for (int i = 0; i < filesHtmls.length; i++) {

				JSONObject file = new JSONObject();
				try {
					file.put("id", i + 1);
					file.put("name", filesHtmls[i]);
				} catch (JSONException e) {
					throw new SerializationException("An error occurred while serializing html file: " + filesHtmls[i], e);
				}
				njo.put(file);
			}

			return Response.ok(njo.toString()).build();

		} catch (SpagoBIRuntimeException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRestServiceException(e.getMessage(), buildLocaleFromSession(), e);

		} catch (Exception e) {
			String errorString = "sbi.menu.load.htmls.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Service that moves menu up
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@Path("moveUp/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getMenuMoveUp(@PathParam("id") Integer id) {

		try {
			DAOFactory.getMenuDAO().moveUpMenu(id);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.menu.load.menu.moveUp.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that moves menu down
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@GET
	@Path("moveDown/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getMenuMoveDown(@PathParam("id") Integer id) {

		try {
			DAOFactory.getMenuDAO().moveDownMenu(id);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.menu.load.menu.moveDown.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	@GET
	@Path("changeWithFather/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getMenuChangeWithFather(@PathParam("id") Integer id) {

		try {
			DAOFactory.getMenuDAO().createMasterMenu(id);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.menu.load.menu.changeWithFather.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that creates new menu
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertMenu(@javax.ws.rs.core.Context HttpServletRequest req) {

		IMenuDAO objDao = null;
		IRoleDAO roleDao = null;
		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			objDao = DAOFactory.getMenuDAO();
			roleDao = DAOFactory.getRoleDAO();
			Menu menu = new Menu();
			menu.setDescr(paramsObj.getString("descr"));
			objDao.setUserProfile(getUserProfile());

			ArrayList<Role> rolesArrayList = new ArrayList<>();
			if (paramsObj.getJSONArray("roles") != null) {
				JSONArray roles = paramsObj.getJSONArray("roles");
				Role[] rolesArray = new Role[roles.length()];
				for (int i = 0; i < roles.length(); i++) {
					int roleID = roles.getJSONObject(i).getInt("id");
					Role r = roleDao.loadByID(roleID);
					rolesArrayList.add(r);

				}

				menu.setRoles(rolesArrayList.toArray(rolesArray));
			}
			if (paramsObj.getString("externalApplicationUrl").equals("null")) {
				menu.setExternalApplicationUrl(null);
			} else {
				menu.setExternalApplicationUrl(paramsObj.getString("externalApplicationUrl"));
			}

			if (paramsObj.getString("functionality").equals("null")) {
				menu.setFunctionality(null);
			} else {
				menu.setFunctionality(paramsObj.getString("functionality"));
			}

			menu.setName(paramsObj.getString("name"));
			menu.setHideSliders(paramsObj.getBoolean("hideSliders"));
			menu.setHideToolbar(paramsObj.getBoolean("hideToolbar"));

			menu.setLevel(paramsObj.getInt("level"));
			if (paramsObj.getString("objId").equals("null")) {
				menu.setObjId(null);
			} else {

				menu.setObjId(paramsObj.getInt("objId"));
			}

			if (paramsObj.getString("initialPath").equals("null")) {
				menu.setInitialPath(null);
			} else {

				menu.setInitialPath(paramsObj.getString("initialPath"));
			}

			if (paramsObj.getString("objParameters").equals("null")) {
				menu.setObjParameters(null);
			} else {

				menu.setObjParameters(paramsObj.getString("objParameters"));
			}
			if (paramsObj.getString("parentId").equals("null")) {
				menu.setParentId(null);
			} else {
				menu.setParentId(paramsObj.getInt("parentId"));
			}
			if (paramsObj.getString("staticPage").equals("null")) {
				menu.setStaticPage(null);
			} else {

				menu.setStaticPage(paramsObj.getString("staticPage"));
			}

			menu.setViewIcons(paramsObj.getBoolean("viewIcons"));

			menu = objDao.insertMenu(menu);
			return Response.ok(menu).build();
		} catch (Exception e) {
			String errorString = "sbi.menu.save.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}

	/**
	 * Service that updates menu
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@PUT
	@Path("/{menuId}")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateMenu(@PathParam("menuId") Integer id, @javax.ws.rs.core.Context HttpServletRequest req) {
		IMenuDAO objDao = null;
		IRoleDAO roleDao = null;
		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);
			JSONArray roles = paramsObj.getJSONArray("roles");

			roleDao = DAOFactory.getRoleDAO();

			objDao = DAOFactory.getMenuDAO();
			ArrayList<Role> rolesArrayList = new ArrayList<>();

			Role[] rolesArray = new Role[roles.length()];
			for (int i = 0; i < roles.length(); i++) {
				int roleID = roles.getJSONObject(i).getInt("id");
				Role r = roleDao.loadByID(roleID);
				rolesArrayList.add(r);

			}

			objDao.setUserProfile(getUserProfile());
			Menu menu = objDao.loadMenuByID(paramsObj.getInt("menuId"));

			menu.setDescr(paramsObj.getString("descr"));

			menu.setRoles(rolesArrayList.toArray(rolesArray));
			if (paramsObj.getString("externalApplicationUrl").equals("null")) {
				menu.setExternalApplicationUrl(null);
			} else {
				menu.setExternalApplicationUrl(paramsObj.getString("externalApplicationUrl"));
			}

			if (paramsObj.has("functionality") == false || paramsObj.getString("functionality").equals("null")) {
				menu.setFunctionality(null);
			} else {
				menu.setFunctionality(paramsObj.getString("functionality"));
			}

			menu.setName(paramsObj.getString("name"));

			menu.setHasChildren(paramsObj.getBoolean("hasChildren"));
			menu.setHideSliders(paramsObj.getBoolean("hideSliders"));
			menu.setHideToolbar(paramsObj.getBoolean("hideToolbar"));
			menu.setLevel(paramsObj.getInt("level"));
			if (paramsObj.getString("objId").equals("null")) {
				menu.setObjId(null);
			} else {

				menu.setObjId(paramsObj.getInt("objId"));
			}

			if (paramsObj.getString("objParameters").equals("null")) {
				menu.setObjParameters(null);
			} else {

				menu.setObjParameters(paramsObj.getString("objParameters"));
			}

			if (paramsObj.getString("parentId").equals("null")) {
				menu.setParentId(null);
			} else {
				menu.setParentId(paramsObj.getInt("parentId"));
			}

			if (paramsObj.getString("staticPage").equals("null")) {
				menu.setStaticPage(null);
			} else {

				menu.setStaticPage(paramsObj.getString("staticPage"));
			}

			if (paramsObj.getString("initialPath").equals("null")) {
				menu.setInitialPath(null);
			} else {

				menu.setInitialPath(paramsObj.getString("initialPath"));
			}

			if (paramsObj.getString("icon").equals("null") || paramsObj.getString("icon").equals("")) {
				menu.setIcon(null);
			} else {
				MenuIcon menuIcon = new MenuIcon();
				JSONObject jsonObject = paramsObj.getJSONObject("icon");
				menuIcon.setId(jsonObject.getInt("id"));
				menuIcon.setCategory(jsonObject.getString("category"));
				menuIcon.setLabel(jsonObject.getString("label"));
				menuIcon.setClassName(jsonObject.getString("className"));

//				menuIcon.setUnicode(jsonObject.getString("unicode"));
				// Set to null because of problems to read
				menuIcon.setUnicode(null);
				menuIcon.setVisible(jsonObject.getBoolean("visible"));

				menu.setIcon(menuIcon);
			}

			if (paramsObj.getString("custIcon").equals("null") || paramsObj.getString("custIcon").equals("")) {
				menu.setCustIcon(null);
			} else {
				MenuIcon menuIcon = new MenuIcon();
				JSONObject jsonObject = paramsObj.getJSONObject("custIcon");
				menuIcon.setId(null);
				menuIcon.setCategory(jsonObject.getString("category"));
				menuIcon.setLabel(jsonObject.getString("label"));
				menuIcon.setClassName(jsonObject.getString("className"));
				menuIcon.setSrc(jsonObject.getString("src"));
				// unicode value not used. Set to null because of problems to read
				menuIcon.setUnicode(null);
				menuIcon.setVisible(jsonObject.getBoolean("visible"));

				menu.setCustIcon(menuIcon);
			}

			menu.setViewIcons(paramsObj.getBoolean("viewIcons"));
			menu.setProg(paramsObj.getInt("prog"));

			objDao.modifyMenu(menu);
			return Response.ok().build();

		} catch (Exception e) {
			String errorString = "sbi.menu.modify.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}

	/**
	 * Service that deletes menu
	 *
	 * @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net
	 */

	@DELETE
	@Path("/{id}")
	public Response deleteMenu(@PathParam("id") Integer id) {

		IMenuDAO iMenuDao = null;

		try {
			Menu menu = new Menu();
			menu.setMenuId(id);
			iMenuDao = DAOFactory.getMenuDAO();
			iMenuDao.setUserProfile(getUserProfile());
			iMenuDao.eraseMenu(menu);
			return Response.ok().build();
		} catch (Exception e) {
			String errorString = "sbi.menu.delete.error";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}
	}

	@GET
	@Path("getParent/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.MENU_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON + charset)
	public Response getParent(@PathParam("id") Integer id) {

		try {
			Menu m = DAOFactory.getMenuDAO().loadMenuByID(id);
			return Response.ok(m).build();

		} catch (Exception e) {
			String errorString = "sbi.menu.load.parent.menu";
			logger.error(errorString, e);
			throw new SpagoBIRestServiceException(errorString, buildLocaleFromSession(), e);
		}

	}
}
