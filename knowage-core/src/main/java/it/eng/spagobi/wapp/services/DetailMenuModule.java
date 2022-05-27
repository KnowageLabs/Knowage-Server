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
package it.eng.spagobi.wapp.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.module.AbstractHttpModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.dao.IMenuDAO;

/**
 * @author Gavardi Giulio
 */
public class DetailMenuModule extends AbstractHttpModule {

	static private Logger logger = Logger.getLogger(DetailMenuModule.class);

	private String modality = "";
	public final static String MODULE_PAGE = "DetailMenuPage";
	public final static String MENU_OBJ = "MENU_OBJ";
	public final static String MENU_ID = "MENU_ID";
	public final static String PARENT_ID = "PARENT_ID";
	public final static String MENU = "MENU";
	public final static String ROLES = "ROLES";
	public final static String LOOKUP = "lookupLoopback";
	public static final String messageBundle = "MessageFiles.messages";

	public final static String PATH = "PATH";
	public final static String EXT_APP_URL = "EXT_APP_URL";
	public final static String PATH_THEME = "PATH_THEME";
	private String typeFunct = null;
	EMFErrorHandler errorHandler = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean config) {
	}

	/**
	 * Reads the operation asked by the user and calls the insertion, modify, detail and deletion methods.
	 *
	 * @param request  The Source Bean containing all request parameters
	 * @param response The Source Bean containing all response parameters
	 *
	 * @throws exception If an exception occurs
	 * @throws Exception the exception
	 */
	@Override
	public void service(SourceBean request, SourceBean response) throws Exception {
		String message = (String) request.getAttribute(AdmintoolsConstants.MESSAGE_DETAIL);
		Object documentLookup = request.getAttribute("loadDocumentLookup");
		errorHandler = getErrorHandler();

		try {
			// get theme for images
			HttpServletRequest httpRequest = getHttpRequest();
			RequestContainer reqCont = ChannelUtilities.getRequestContainer(httpRequest);
			String currTheme = ThemesManager.getCurrentTheme(reqCont);
			if (currTheme == null)
				currTheme = ThemesManager.getDefaultTheme();
			String contextTheme = httpRequest.getContextPath();
			if (!(contextTheme.charAt(contextTheme.length() - 1) == '/')) {
				contextTheme += '/';
			}
			contextTheme += "themes/" + currTheme + "/";
			response.setAttribute(PATH_THEME, contextTheme);

			if (message == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 101);
				SpagoBITracer.debug(AdmintoolsConstants.NAME_MODULE, "DetailFunctionalityModule", "service", "The message parameter is null");
				throw userError;
			}
			if (documentLookup != null) {
				lookupLoadHandler(request, message, response);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_SELECT)) {
				getDetailMenu(request, response);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
				modDetailMenu(request, AdmintoolsConstants.DETAIL_MOD, response);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_NEW)) {
				newDetailMenu(request, response);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
				modDetailMenu(request, AdmintoolsConstants.DETAIL_INS, response);
			} else if (message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_DEL)) {
				delMenu(request, AdmintoolsConstants.DETAIL_DEL, response);
			}

		} catch (EMFUserError eex) {
			errorHandler.addError(eex);
			return;
		} catch (Exception ex) {
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			errorHandler.addError(internalError);
			return;
		}
	}

	/**
	 * Gets the detail of a menu choosed by the user from the low functionalities list. It reaches the key from the request and asks to the DB all detail
	 * parameter use mode information, by calling the method <code>loadLowFunctionalityByPath</code>.
	 *
	 * @param key      The choosed low functionality id key
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */
	private void getDetailMenu(SourceBean request, SourceBean response) throws EMFUserError {
		try {
			this.modality = AdmintoolsConstants.DETAIL_MOD;

			String menuId = (String) request.getAttribute(MENU_ID);

			// String parentId = (String) request.getAttribute(PARENT_ID);
			response.setAttribute(AdmintoolsConstants.MODALITY, modality);

			Menu menu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(menuId));
			response.setAttribute(MENU, menu);
			Integer parentIdInt = menu.getParentId();

			if (parentIdInt != null) {

				String parentId = parentIdInt.toString();

				response.setAttribute(PARENT_ID, parentId);

			}
		} catch (Exception ex) {
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	/**
	 * Inserts/Modifies the detail of a menu according to the user request. When a parameter use mode is modified, the <code>modifyLowFunctionality</code>
	 * method is called; when a new parameter use mode is added, the <code>insertLowFunctionality</code> method is called. These two cases are differentiated by
	 * the <code>mod</code> String input value .
	 *
	 * @param request  The request information contained in a SourceBean Object
	 * @param mod      A request string used to differentiate insert/modify operations
	 * @param response The response SourceBean
	 * @throws EMFUserError        If an exception occurs
	 * @throws SourceBeanException If a SourceBean exception occurs
	 */
	private void modDetailMenu(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException {
		HashMap<String, String> logParam = new HashMap();

		// **********************************************************************
		RequestContainer reqCont = getRequestContainer();
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		Menu menu = recoverMenuDetails(request, mod);
		logParam.put("Name", menu.getName());
		logParam.put("Code", menu.getCode());

		response.setAttribute(MENU, menu);
		response.setAttribute(AdmintoolsConstants.MODALITY, mod);
		IMenuDAO menuDao = DAOFactory.getMenuDAO();
		menuDao.setUserProfile(profile);
		// if there are some validation errors into the errorHandler does not write into DB
		Collection errors = errorHandler.getErrors();
		if (errors != null && errors.size() > 0) {
			Iterator iterator = errors.iterator();
			while (iterator.hasNext()) {
				Object error = iterator.next();
				if (error instanceof EMFValidationError) {
					Integer parentMenuId = menu.getParentId();
					Menu parentMenu = null;
					if (parentMenuId != null) {
						parentMenu = menuDao.loadMenuByID(parentMenuId);
					}
					if (parentMenu == null) {
						throw new EMFUserError(EMFErrorSeverity.ERROR, "10001", messageBundle);
					} else {
						response.setAttribute(PARENT_ID, parentMenu.getMenuId());
					}
					return;
				}
			}
		}

		if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
			menuDao.insertMenu(menu);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.ADD", logParam, "OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
			menuDao.modifyMenu(menu);
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.MODIFY", logParam, "OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		response.setAttribute(AdmintoolsConstants.LOOPBACK, "true");
	}

	/**
	 * Deletes a Menu choosed by user .
	 *
	 * @param request  The request SourceBean
	 * @param mod      A request string used to differentiate delete operation
	 * @param response The response SourceBean
	 * @throws EMFUserError        If an Exception occurs
	 * @throws SourceBeanException If a SourceBean Exception occurs
	 */
	private void delMenu(SourceBean request, String mod, SourceBean response) throws EMFUserError, SourceBeanException {
		String id = (String) request.getAttribute(MENU_ID);
		IMenuDAO menudao = DAOFactory.getMenuDAO();
		RequestContainer reqCont = getRequestContainer();
		Menu menu = menudao.loadMenuByID(Integer.valueOf(id));
		SessionContainer permSess = getRequestContainer().getSessionContainer().getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();
		logParam.put("Name", menu.getName());
		logParam.put("Code", menu.getCode());
		try {
			menudao.eraseMenu(menu);
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.DELETE", logParam, "OK");
		} catch (EMFUserError eex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.DELETE", logParam, "ERR");
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, "10002", messageBundle);
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.DELETE", logParam, "ERR");
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		response.setAttribute("loopback", "true");
	}

	/**
	 * Instantiates a new <code>Menu<code> object when a new menu insertion is required, in order to prepare the page for the insertion.
	 *
	 * @param response The response SourceBean
	 * @throws EMFUserError If an Exception occurred
	 */
	private void newDetailMenu(SourceBean request, SourceBean response) throws EMFUserError {
		SessionContainer permSess = getRequestContainer().getSessionContainer().getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			this.modality = AdmintoolsConstants.DETAIL_INS;
			String idParent = (String) request.getAttribute(DetailMenuModule.PARENT_ID);
			response.setAttribute(AdmintoolsConstants.MODALITY, modality);
			if (idParent != null) // if it is null it is a root menu
				response.setAttribute(DetailMenuModule.PARENT_ID, idParent);

			Menu menu = new Menu();
			menu.setDescr("");
			menu.setName("");
			menu.setHasChildren(false);

			Menu parentMenu = null;
			if (idParent != null) {
				parentMenu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(idParent));
			}
			Role[] roles = new Role[0];

			if (parentMenu != null) {
				roles = parentMenu.getRoles();
			} else { // if no parent all roles enabled
				List allRoles = DAOFactory.getRoleDAO().loadAllRoles();
				roles = new Role[allRoles.size()];
				for (int i = 0; i < allRoles.size(); i++) {
					Role role = (Role) allRoles.get(i);
					roles[i] = role;

				}

			}
			menu.setRoles(roles);

			response.setAttribute(MENU, menu);
		} catch (Exception ex) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.ADD", null, "KO");
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}

	private Menu recoverMenuDetails(SourceBean request, String mod) throws EMFUserError, SourceBeanException {
		String name = (String) request.getAttribute("name");
		name = name.trim();

		String description = (String) request.getAttribute("description");
		description = description.trim();

		List attrsList = request.getAttributeAsList(DetailMenuModule.ROLES);
		Role[] roles = new Role[attrsList.size()];
		for (int i = 0; i < roles.length; i++) {
			String idRoleStr = (String) attrsList.get(i);
			roles[i] = DAOFactory.getRoleDAO().loadByID(new Integer(idRoleStr));
		}

		Menu menu = null;
		if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_INS)) {
			String idParent = (String) request.getAttribute(DetailMenuModule.PARENT_ID);
			menu = new Menu();
			menu.setHasChildren(false);
			if (idParent != null)
				menu.setParentId(Integer.valueOf(idParent));
			else
				menu.setParentId(null);
		} else if (mod.equalsIgnoreCase(AdmintoolsConstants.DETAIL_MOD)) {
			String idMenu = (String) request.getAttribute(DetailMenuModule.MENU_ID);
			menu = DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(idMenu));
		}

		menu.setName(name);
		menu.setDescr(description);
		menu.setRoles(roles);

		HashMap<String, String> logParam = new HashMap();
		logParam.put("NAME", name);

		SessionContainer permSess = getRequestContainer().getSessionContainer().getPermanentContainer();
		IEngUserProfile profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		if (name.equalsIgnoreCase("")) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "MENU.ADD/MODIFY", logParam, "OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, "10003", messageBundle);
		}

		String nodeContent = (String) request.getAttribute("nodeContent");
		if ("nodeDocument".equals(nodeContent)) {
			// menu node with document
			String objectId = (String) request.getAttribute(DetailMenuModule.MENU_OBJ);
			menu.setObjId(Integer.valueOf(objectId));
			String objParameters = (String) request.getAttribute("objParameters");
			if (objParameters != null && !objParameters.trim().equals("")) {
				menu.setObjParameters(objParameters);
			} else {
				menu.setObjParameters(null);
			}
			String subobjectName = (String) request.getAttribute("subobjectName");
			if (subobjectName != null && !subobjectName.trim().equals("")) {
				menu.setSubObjName(subobjectName);
			} else {
				menu.setSubObjName(null);
			}
			String snapshotName = (String) request.getAttribute("snapshotName");
			if (snapshotName != null && !snapshotName.trim().equals("")) {
				menu.setSnapshotName(snapshotName);
			} else {
				menu.setSnapshotName(null);
			}
			String snapshotHistoryStr = (String) request.getAttribute("snapshotHistory");
			if (snapshotHistoryStr != null && !snapshotHistoryStr.trim().equals("")) {
				Integer snapshotHistory = null;
				try {
					snapshotHistory = new Integer(Integer.parseInt(snapshotHistoryStr));
				} catch (Exception e) {
					logger.error("Error while parsing [" + snapshotHistoryStr + "] into an integer", e);
					snapshotHistory = new Integer(0);
				}
				menu.setSnapshotHistory(snapshotHistory);
			} else {
				menu.setSnapshotHistory(null);
			}
			menu.setStaticPage(null);
			menu.setExternalApplicationUrl(null);
			menu.setFunctionality(null);
			menu.setInitialPath(null);
			String hideToolbarB = (String) request.getAttribute("hideToolbar");
			String hideSlidersB = (String) request.getAttribute("hideSliders");
			if (hideToolbarB != null)
				menu.setHideToolbar(Boolean.valueOf(hideToolbarB).booleanValue());
			else
				menu.setHideToolbar(false);
			if (hideSlidersB != null)
				menu.setHideSliders(Boolean.valueOf(hideSlidersB).booleanValue());
			else
				menu.setHideSliders(false);
		} else if ("nodeStaticPage".equals(nodeContent)) {
			// menu node with static page
			menu.setExternalApplicationUrl(null);
			menu.setObjId(null);
			menu.setSubObjName(null);
			menu.setObjParameters(null);
			menu.setSnapshotName(null);
			menu.setSnapshotHistory(null);
			menu.setFunctionality(null);
			menu.setInitialPath(null);
			menu.setHideToolbar(false);
			menu.setHideSliders(false);
			String staticPage = (String) request.getAttribute("staticpage");
			menu.setStaticPage(staticPage);
		} else if ("nodeFunctionality".equals(nodeContent)) {
			// menu node with static page
			menu.setObjId(null);
			menu.setSubObjName(null);
			menu.setObjParameters(null);
			menu.setSnapshotName(null);
			menu.setSnapshotHistory(null);
			menu.setStaticPage(null);
			menu.setExternalApplicationUrl(null);
			menu.setHideToolbar(false);
			menu.setHideSliders(false);
			String functionality = (String) request.getAttribute("functionality");
			menu.setFunctionality(functionality);
			if (functionality.equals(SpagoBIConstants.DOCUMENT_BROWSER_USER)) {
				String initialPath = (String) request.getAttribute("initialPath");
				menu.setInitialPath(initialPath);
			} else {
				menu.setInitialPath(null);
			}
		} else if ("nodeExternalApp".equals(nodeContent)) {
			// url for external application
			String url = (String) request.getAttribute(DetailMenuModule.EXT_APP_URL);
			menu.setExternalApplicationUrl(url);

			menu.setObjId(null);
			menu.setSubObjName(null);
			menu.setObjParameters(null);
			menu.setSnapshotName(null);
			menu.setSnapshotHistory(null);
			menu.setStaticPage(null);
			menu.setFunctionality(null);
			menu.setInitialPath(null);
			menu.setHideToolbar(false);
			menu.setHideSliders(false);
		} else {
			// empty menu node
			menu.setObjId(null);
			menu.setSubObjName(null);
			menu.setObjParameters(null);
			menu.setSnapshotName(null);
			menu.setSnapshotHistory(null);
			menu.setStaticPage(null);
			menu.setExternalApplicationUrl(null);
			menu.setFunctionality(null);
			menu.setInitialPath(null);
			menu.setHideToolbar(false);
			menu.setHideSliders(false);
		}

		String viewIconsB = (String) request.getAttribute("viewicons");
		if (viewIconsB != null)
			menu.setViewIcons(Boolean.valueOf(viewIconsB).booleanValue());
		else
			menu.setViewIcons(false);
		return menu;
	}

	/**
	 * Defines all roles that have to be erased in order to keep functionalities tree consistence. When we leave some permissions to a functionality, those
	 * permissions will not be assignable to all the children functionality. If any child has a permission that his parent anymore has, this permission mus be
	 * deleted for all father's children and descendants. This metod recusively scans all father's descendants and saves inside a Set all roles that must be
	 * erased from the Database.
	 *
	 * @param lowFuncParent the parent Functionality
	 * @param rolesToErase  the set containing all roles to erase
	 *
	 * @throws EMFUserError                if any EMFUserError exception occurs
	 * @throws BuildOperationException     if any BuildOperationException exception occurs
	 * @throws OperationExecutionException if any OperationExecutionException exception occurs
	 */
	/*
	 * public void loadRolesToErase(Menu menuParent, Set rolesToErase) throws EMFUserError{ String parentPath = lowFuncParent.getPath(); //ArrayList childs =
	 * DAOFactory.getFunctionalityCMSDAO().recoverChilds(parentPath); List childs = DAOFactory.getLowFunctionalityDAO().loadSubLowFunctionalities(parentPath,
	 * false); if(childs.size()!= 0) { Iterator i = childs.iterator(); while (i.hasNext()){ LowFunctionality childNode = (LowFunctionality) i.next(); String
	 * childPath = childNode.getPath(); //LowFunctionality lowFuncParent = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(parentPath);
	 * LowFunctionality lowFuncChild = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(childPath, false); if(lowFuncChild != null){ //control
	 * childs permissions and fathers permissions //remove from childs those persmissions that are not present in the fathers //control for test Roles Role[]
	 * testChildRoles = lowFuncChild.getTestRoles(); //Role[] testParentRoles = lowFuncParent.getTestRoles(); //ArrayList newTestChildRoles = new ArrayList();
	 * //HashMap rolesToErase = new HashMap(); for(int j = 0; j < testChildRoles.length; j++) { String rule = testChildRoles[j].getId().toString();
	 * if(!isParentRule(rule, lowFuncParent,"TEST")){ ArrayList roles = new ArrayList(); roles.add(0,lowFuncChild.getId());
	 * roles.add(1,testChildRoles[j].getId()); roles.add(2,"TEST"); rolesToErase.add(roles); lowFuncChild =
	 * eraseRolesFromFunctionality(lowFuncChild,rule,"TEST"); //rolesToErase.put(lowFuncChild.getId(),testChildRoles[j].getId());
	 * //DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,testChildRoles[j].getId()); } } //control for development roles Role[]
	 * devChildRoles = lowFuncChild.getDevRoles(); //Role[] devParentRoles = lowFuncParent.getDevRoles(); //ArrayList newDevChildRoles = new ArrayList();
	 * for(int j = 0; j < devChildRoles.length; j++) { String rule = devChildRoles[j].getId().toString(); if(!isParentRule(rule, lowFuncParent,"DEV")){
	 * ArrayList roles = new ArrayList(); roles.add(0,lowFuncChild.getId()); roles.add(1,devChildRoles[j].getId()); roles.add(2,"DEV"); rolesToErase.add(roles);
	 * lowFuncChild = eraseRolesFromFunctionality(lowFuncChild,rule,"DEV"); //rolesToErase.put(lowFuncChild.getId(),devChildRoles[j].getId());
	 * //DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,devChildRoles[j].getId()); } } //control for execution roles Role[]
	 * execChildRoles = lowFuncChild.getExecRoles(); //Role[] execParentRoles = lowFuncParent.getExecRoles(); //ArrayList newExecChildRoles = new ArrayList();
	 * for(int j = 0; j < execChildRoles.length; j++) { String rule = execChildRoles[j].getId().toString(); if(!isParentRule(rule, lowFuncParent,"EXEC")){
	 * ArrayList roles = new ArrayList(); roles.add(0,lowFuncChild.getId()); roles.add(1,execChildRoles[j].getId()); roles.add(2,"REL");
	 * rolesToErase.add(roles); lowFuncChild = eraseRolesFromFunctionality(lowFuncChild,rule,"EXEC");
	 * //rolesToErase.put(lowFuncChild.getId(),execChildRoles[j].getId());
	 * //DAOFactory.getLowFunctionalityDAO().deleteFunctionalityRole(lowFuncChild,execChildRoles[j].getId()); } } //loadRolesToErase(lowFuncChild,rolesToErase);
	 * }
	 *
	 * //loadRolesToErase(childPath,rolesToErase); }
	 *
	 * } }
	 */

	/**
	 * Erases the defined input role from a functionality object, if this one has the role.The updated functionality object is returned.
	 *
	 * @param func     the input functionality object
	 * @param roleId   the role id for the role to erase
	 * @param roleType the type of the role to erase
	 *
	 * @return the updated functionality
	 */
	public Menu eraseRolesFromMenu(Menu menu, String roleId) {
		Role[] roles = menu.getRoles();
		Set rolesSet = new HashSet();
		for (int i = 0; i < roles.length; i++) {
			if (!roles[i].getId().toString().equals(roleId)) {
				rolesSet.add(roles[i]);
			}

		}
		menu.setRoles((Role[]) rolesSet.toArray(new Role[0]));

		return menu;
	}

	private void lookupLoadHandler(SourceBean request, String modality, SourceBean response) throws EMFUserError, SourceBeanException {

		RequestContainer requestContainer = this.getRequestContainer();
		SessionContainer session = requestContainer.getSessionContainer();
		session.setAttribute("modality", modality);
		response.setAttribute(DetailMenuModule.LOOKUP, "true");

	}

	public static String assignImage(Menu menu) {
		String url = "";
		if (menu.getObjId() != null) {
			try {
				BIObject object = DAOFactory.getBIObjectDAO().loadBIObjectById(menu.getObjId());
				String biObjType = object.getBiObjectTypeCode();
				url = "/img/objecticon_" + biObjType + ".png";

			} catch (EMFUserError e) {
				return "";
			}
			return url;
		} else if (menu.getStaticPage() != null) {
			url = "/img/wapp/static_page.png";
			return url;
		} else if (menu.getExternalApplicationUrl() != null) {
			url = "/img/wapp/application_link16.png";
			return url;
		} else if (menu.getFunctionality() != null && !menu.getFunctionality().equals("")) {
			SourceBean config = (SourceBean) ConfigSingleton.getInstance().getFilteredSourceBeanAttribute("FINAL_USER_FUNCTIONALITIES.APPLICATION",
					"functionality", menu.getFunctionality());
			if (config != null) {
				String iconUrl = (String) config.getAttribute("iconUrl");
				iconUrl = iconUrl.replaceAll("\\$\\{SPAGOBI_CONTEXT\\}", "");
				iconUrl = iconUrl.replaceAll("\\$\\{THEME\\}", "");
				return iconUrl;
			} else
				return "";
		} else if (menu.getIconPath() != null) {
			String iconUrl = menu.getIconPath();
			iconUrl = iconUrl.replaceAll("\\$\\{SPAGOBI_CONTEXT\\}", "");
			iconUrl = iconUrl.replaceAll("/themes/", "");
			iconUrl = iconUrl.replaceAll("\\$\\{THEME\\}", "");
			return iconUrl;
		} else
			return "";
	}

	public static String findFunctionalityUrl(Menu menu, String contextPath) {
		logger.debug("IN");
		String url = null;
		try {
			String functionality = menu.getFunctionality();
			if (functionality == null || functionality.trim().equals("")) {
				logger.error("Input menu is not associated to a SpagoBI functionality");
			} else {
				SourceBean config = (SourceBean) ConfigSingleton.getInstance().getFilteredSourceBeanAttribute("FINAL_USER_FUNCTIONALITIES.APPLICATION",
						"functionality", functionality);
				if (config != null) {
					url = (String) config.getAttribute("link");
					url = url.replaceAll("\\$\\{SPAGOBI_CONTEXT\\}", contextPath);
					url = url.replaceAll("\\$\\{SPAGO_ADAPTER_HTTP\\}", GeneralUtilities.getSpagoAdapterHttpUrl());
					if (functionality.equals(SpagoBIConstants.DOCUMENT_BROWSER_USER)) {
						String initialPath = menu.getInitialPath();
						if (initialPath != null && !initialPath.trim().equals("")) {
							// url += "&" + BIObjectsModule.MODALITY + "=" + BIObjectsModule.FILTER_TREE + "&" + TreeObjectsModule.PATH_SUBTREE + "="
							// + initialPath;

							String idsPath = convertPathInIds(initialPath);
//							url += "&" + BIObjectsModule.MODALITY + "=" + BIObjectsModule.FILTER_TREE + "&" + TreeObjectsModule.PATH_SUBTREE + "=" + initialPath
//									+ idsPath;

						}
					} else if (functionality.equals(SpagoBIConstants.WORKSPACE_MANAGEMENT)) {
						String initialPath = menu.getInitialPath();
						if (initialPath != null && initialPath.equals("documents")) {
							url += "/recent";
						} else if (initialPath != null && initialPath.equals("datasets")) {
							url += "/data";
						} else if (initialPath != null && initialPath.equals("models")) {
							url += "/models";
						}
					}

				} else {
					logger.warn("No configuration found for SpagoBI functionality [" + menu.getFunctionality() + "]");
				}
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug("OUT: url = [" + url + "]");
		}
		return url;
	}

	public static String convertPathInIds(String subTree) throws EMFUserError {
		// If this is a "custom" Document Browser we have a subtree path as parameter
		List<Integer> functIds = new LinkedList<>();
		logger.debug("IN");
		if (subTree != null) {

			if (!StringUtils.isEmpty(subTree)) {
				ArrayList<LowFunctionality> toMerge = new ArrayList<>();
				LowFunctionality funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(subTree, false);
				if (funct != null) {
					toMerge.add(funct);
					Integer parentId = funct.getParentId();
					while (parentId != null) {
						LowFunctionality more = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(parentId, false);
						if (more != null) {
							toMerge.add(more);
							parentId = more.getParentId();
						}
					}

					// toMerge contains all Id to be read in reverse order
					for (int i = toMerge.size() - 1; i >= 0; i--) {
						LowFunctionality lf = toMerge.get(i);
						functIds.add(lf.getId());
					}

				}
			}

		}
		logger.debug("OUT");

		String value = functIds.stream().map(x -> x.toString()).collect(Collectors.joining("&defaultFoldersId="));

		return value;
	}

}
