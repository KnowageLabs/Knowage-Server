/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.commons.serializer.v3;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.Serializer;
import it.eng.spagobi.commons.utilities.DocumentUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.security.InternalSecurityServiceSupplierImpl;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.services.DetailMenuModule;
import it.eng.spagobi.wapp.util.MenuUtilities;

/**
 * @author Alberto Nale
 */
public class MenuListJSONSerializerForREST implements Serializer {

	static private Logger logger = Logger.getLogger(MenuListJSONSerializerForREST.class);

	private static final String ID = "id";
	private static final String TO_BE_LICENSED = "toBeLicensed";
	private static final String STATIC_MENU = "STATIC_MENU";
	private static final String URL = "url";

	private static final String PLACEHOLDER_SPAGO_ADAPTER_HTTP = "${SPAGO_ADAPTER_HTTP}";
	private static final String PLACEHOLDER_SPAGOBI_CONTEXT = "${SPAGOBI_CONTEXT}";
	private static final String PLACEHOLDER_KNOWAGE_VUE_CONTEXT = "${KNOWAGE_VUE_CONTEXT}";
	private static final String PLACEHOLDER_KNOWAGE_THEME = "${KNOWAGE_THEME}";

	private static final String CONDITION = "condition";

	private static final String REQUIRED_FUNCTIONALITY = "requiredFunctionality";
	private static final String GROUP_ITEM = "GROUP_ITEM";
	private static final String ITEM = "ITEM";
	private static final String ALLOWED_USER_FUNCTIONALITIES = "ALLOWED_USER_FUNCTIONALITIES";
	private static final String COMMON_USER_FUNCTIONALITIES = "COMMON_USER_FUNCTIONALITIES";
	private static final String TECHNICAL_USER_FUNCTIONALITIES = "TECHNICAL_USER_FUNCTIONALITIES";

	private static final String LABEL = "label";
	private static final String ITEMS = "items";
	private static final String TO = "to";

	public static final String CUST_ICON = "custIcon";
	public static final String ICON_CLS = "iconCls";
	public static final String PATH = "path";

	public static final String DESCR = "descr";

	private static final String HREF_DOC_BROWSER_ANGULAR = "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ANGULAR_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_DOC_BROWSER_WORKSPACE = "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_WORKSPACE&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";

	public String contextName = "";
	public String vueContextName = "knowage-vue";
	public String defaultThemePath = "/themes/sbi_default";

	private IEngUserProfile userProfile;
	private HttpSession httpSession;
	private String currentTheme = null;

	private Set<Integer> technicalUserMenuIds = new HashSet<Integer>();

	public MenuListJSONSerializerForREST(IEngUserProfile userProfile, HttpSession session, String currentTheme) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		this.setUserProfile(userProfile);
		this.setHttpSession(session);
		this.currentTheme = currentTheme;
	}

	@Override
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		contextName = KnowageSystemConfiguration.getKnowageContext();
		if (!(o instanceof List)) {
			throw new SerializationException(
					MenuListJSONSerializerForREST.class.getSimpleName() + " is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			SourceBean menuDefinitionFile = (SourceBean) ConfigSingleton.getInstance().getAttribute(STATIC_MENU);

			JSONArray technicalUserMenuJSONArray = new JSONArray();
			technicalUserMenuIds = new HashSet<Integer>();
			if (UserUtilities.isTechnicalUser(this.getUserProfile())) {
				technicalUserMenuJSONArray = createTechnicalUserMenu(menuDefinitionFile, new JSONArray(), locale);
			}

			JSONArray commonUserFunctionalitiesMenuJSONArray = createCommonUserFunctionalitiesMenu(menuDefinitionFile, technicalUserMenuJSONArray, locale);

			JSONArray allowedUserFunctionalitiesMenuJSONArray = new JSONArray();
			allowedUserFunctionalitiesMenuJSONArray = createAllowedUserFunctionalitiesMenu(menuDefinitionFile, technicalUserMenuJSONArray, locale);

			List filteredMenuList = (List) o;
			JSONArray dynamicUserFunctionalitiesMenuJSONArray = createDynamicUserFunctionalitiesMenu(filteredMenuList, locale);

			JSONObject wholeMenu = new JSONObject();
			/* STATIC */
			wholeMenu.put("technicalUserFunctionalities", technicalUserMenuJSONArray);
			wholeMenu.put("commonUserFunctionalities", commonUserFunctionalitiesMenuJSONArray);
			wholeMenu.put("allowedUserFunctionalities", allowedUserFunctionalitiesMenuJSONArray);

			/* DYNAMIC */
			wholeMenu.put("dynamicUserFunctionalities", dynamicUserFunctionalitiesMenuJSONArray);

			result = wholeMenu;

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		return result;
	}

	private JSONArray createAllowedUserFunctionalitiesMenu(SourceBean menuDefinitionFile, JSONArray technicalUserMenuJSONArray, Locale locale)
			throws JSONException, EMFInternalError {

		logger.debug("IN");

		JSONArray userMenu = createMenu(menuDefinitionFile, locale, technicalUserMenuJSONArray, ALLOWED_USER_FUNCTIONALITIES, false);

		logger.debug("OUT");

		return userMenu;
	}

	private JSONArray createCommonUserFunctionalitiesMenu(SourceBean menuDefinitionFile, JSONArray technicalUserMenuJSONArray, Locale locale)
			throws JSONException, EMFInternalError {

		logger.debug("IN");

		JSONArray commonUserFunctionalitiesMenu = createMenu(menuDefinitionFile, locale, technicalUserMenuJSONArray, COMMON_USER_FUNCTIONALITIES, false);

		logger.debug("OUT");

		return commonUserFunctionalitiesMenu;
	}

	private JSONArray createTechnicalUserMenu(SourceBean menuDefinitionFile, JSONArray technicalUserMenuJSONArray, Locale locale)
			throws JSONException, EMFInternalError {

		logger.debug("IN");

		JSONArray technicalUserMenu = createMenu(menuDefinitionFile, locale, technicalUserMenuJSONArray, TECHNICAL_USER_FUNCTIONALITIES, true);

		logger.debug("OUT");

		return technicalUserMenu;
	}

	private JSONArray createDynamicUserFunctionalitiesMenu(List filteredMenuList, Locale locale) throws JSONException {

		JSONArray tempFirstLevelMenuList = new JSONArray();
		JSONArray userMenu = new JSONArray();
		JSONArray menuUserList = new JSONArray();

		if (filteredMenuList != null && !filteredMenuList.isEmpty()) {

			menuUserList = new JSONArray();
			MessageBuilder msgBuild = new MessageBuilder();

			for (int i = 0; i < filteredMenuList.size(); i++) {
				Menu menuElem = (Menu) filteredMenuList.get(i);
				String path = MenuUtilities.getMenuPath(filteredMenuList, menuElem, locale);

				if (menuElem.getLevel().intValue() == 1) {

					JSONObject temp = new JSONObject();

					if (!menuElem.isAdminsMenu()) {
						// Create custom Menu elements (menu defined by the users)

						menuUserList = createUserMenuElement(filteredMenuList, menuElem, locale, 1, menuUserList);
						if (menuUserList.length() > 0)
							tempFirstLevelMenuList.put(menuUserList.get(0));

						if (menuElem.getHasChildren()) {
							List lstChildrenLev2 = menuElem.getLstChildren();
							JSONArray tempMenuList2 = (JSONArray) getChildren(filteredMenuList, lstChildrenLev2, 1, locale);
							temp.put(ITEMS, tempMenuList2);
						}

					} else {
						// This part create the elements for the admin menu

						temp.put(ICON_CLS, menuElem.getIconCls());

						String text = "";
						if (!menuElem.isAdminsMenu() || !menuElem.getName().startsWith("#"))
							text = menuElem.getName();
						else {
							if (menuElem.getName().startsWith("#")) {
								String titleCode = menuElem.getName().substring(1);
								text = msgBuild.getMessage(titleCode, locale);
							} else {
								text = menuElem.getName();
							}
						}
						temp.put(LABEL, text);

						if (menuElem.getCode() != null && (menuElem.getCode().equals("doc_admin_angular") || menuElem.getCode().equals("doc_test_angular"))) {
							temp.put(TO, contextName + HREF_DOC_BROWSER_ANGULAR);
						}

						/**
						 * The URL for the Workspace web page.
						 *
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						if (menuElem.getCode() != null && menuElem.getCode().equals("workspace")) {
							temp.put(TO, contextName + HREF_DOC_BROWSER_WORKSPACE);
						}

						if (menuElem.getHasChildren()) {
							List lstChildrenLev2 = menuElem.getLstChildren();
							JSONArray tempMenuList = (JSONArray) getChildren(filteredMenuList, lstChildrenLev2, 1, locale);
							temp.put(ITEMS, tempMenuList);
						}
						if (menuElem.getCode().equals("doc_test_angular") && UserUtilities.isAdministrator(this.getUserProfile())) {
							continue;
						}

						userMenu.put(temp);
					}
				}
			}
		}
		return menuUserList;
	}

	private JSONArray createMenu(SourceBean menuDefinitionFile, Locale locale, JSONArray technicalUserMenuJSONArray, String attribute,
			boolean isTechnicalUserMenu) throws JSONException, EMFInternalError {
		MessageBuilder messageBuilder = new MessageBuilder();
		List attributeList = menuDefinitionFile.getAttributeAsList(attribute);
		return buildMenuTreeBranch(locale, messageBuilder, attributeList, technicalUserMenuJSONArray, isTechnicalUserMenu);
	}

	private JSONArray buildMenuTreeBranch(Locale locale, MessageBuilder messageBuilder, List attributeList, JSONArray technicalUserMenuJSONArray,
			boolean isTechnicalUserMenu) throws JSONException, EMFInternalError {
		JSONArray tempMenuList = new JSONArray();
		List funcs = (List) userProfile.getFunctionalities();

		if (isTechnicalUserMenu) {
			for (Object domain : attributeList) {

				List menuCategory = ((SourceBean) domain).getAttributeAsList(GROUP_ITEM);

				for (Object groupItem : menuCategory) {

					if (isLicensedMenu(((SourceBean) groupItem))) {

						List itemsSBList = ((SourceBean) groupItem).getAttributeAsList(ITEM);

						JSONArray children = createItemsArray(locale, messageBuilder, funcs, technicalUserMenuJSONArray, itemsSBList, isTechnicalUserMenu);

						if (children.length() > 0) {

							SourceBean objSB = (SourceBean) groupItem;
							JSONObject groupItemJSON = createMenuNode(locale, messageBuilder, objSB, isTechnicalUserMenu);
							groupItemJSON.put(ITEMS, children);

							tempMenuList.put(groupItemJSON);
						}
					}
				}

			}
		} else {
			for (Object domain : attributeList) {
				List itemsSBList = ((SourceBean) domain).getAttributeAsList(ITEM);
				JSONArray children = createItemsArray(locale, messageBuilder, funcs, technicalUserMenuJSONArray, itemsSBList, isTechnicalUserMenu);
				tempMenuList = children;
			}
		}

		return tempMenuList;
	}

	private JSONArray createItemsArray(Locale locale, MessageBuilder messageBuilder, List funcs, JSONArray technicalUserMenuJSONArray, List itemsSBList,
			boolean isTechnicalUserMenu) throws JSONException, EMFInternalError {
		JSONArray items = new JSONArray();
		for (Object item : itemsSBList) {

			SourceBean itemSB = (SourceBean) item;
			if (!isInTechnicalUserMenu(technicalUserMenuJSONArray, itemSB, messageBuilder, locale)) {

				boolean addElement = true;

				String condition = (String) itemSB.getAttribute(CONDITION);
				String requiredFunctionality = (String) itemSB.getAttribute(REQUIRED_FUNCTIONALITY);

				/* ALL_USERS or ALLOWED_USER_FUNCTIONALITIES */
				if (condition != null && !condition.isEmpty()) {
					addElement = menuConditionIsSatisfied(itemSB);
				} else if (requiredFunctionality != null) {
					if (isAbleTo(requiredFunctionality, funcs)) {
						addElement = isLicensedMenu(itemSB);
					} else
						addElement = false;
				}

				if (addElement) {
					JSONObject menu = createMenuNode(locale, messageBuilder, itemSB, isTechnicalUserMenu);
					items.put(menu);
				}
			}
		}

		return items;
	}

	private boolean isInTechnicalUserMenu(JSONArray technicalUserMenuJSONArray, SourceBean itemSB, MessageBuilder messageBuilder, Locale locale)
			throws JSONException {

		String strId = (String) itemSB.getAttribute(ID);
		if (strId != null) {
			Integer id = Integer.valueOf(strId);

			return technicalUserMenuIds.contains(id);
		}

		return false;
	}

	private boolean isLicensedMenu(SourceBean itemSB) {
		Boolean isLicensed = true;

		boolean toBeLicensed = "true".equals(itemSB.getAttribute(TO_BE_LICENSED));
		if (toBeLicensed) {
			try {
				Class.forName("it.eng.knowage.tools.servermanager.importexport.ExporterMetadata", false, this.getClass().getClassLoader());

				isLicensed = DocumentUtilities.getValidLicenses().size() > 0;
			} catch (ClassNotFoundException e) {
				isLicensed = false;
			}
		}

		return isLicensed;
	}

	private boolean menuConditionIsSatisfied(SourceBean itemSB) throws EMFInternalError {
		boolean isSatisfied = false;

		String condition = (String) itemSB.getAttribute(CONDITION);
		if (condition != null && !condition.isEmpty()) {
			switch (condition) {

			case "my_account":
				if (!UserUtilities.isTechnicalUser(this.getUserProfile())) {
					String strMyAccountMenu = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.MY_ACCOUNT_MENU");
					boolean myAccountMenu = !"false".equalsIgnoreCase(strMyAccountMenu); // default value is true, for backward compatibility

					String securityServiceSupplier = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
					boolean isInternalSecurityServiceSupplier = securityServiceSupplier.equalsIgnoreCase(InternalSecurityServiceSupplierImpl.class.getName());
					boolean isPublicUser = userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID);
					if (isInternalSecurityServiceSupplier && !isPublicUser && myAccountMenu) {
						isSatisfied = true;
					}
				}
				break;

			case "multiple_roles":
				isSatisfied = userProfile.getRoles().size() > 1;
				break;

			case "public_user":
				if (PublicProfile.isPublicUser(userProfile.getUserUniqueIdentifier().toString())
						|| userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID)) {
					isSatisfied = true;
				}
				break;

			case "silent_login":
				boolean showLogoutOnSilentLogin = Boolean.valueOf(SingletonConfig.getInstance().getConfigValue("SPAGOBI.HOME.SHOW_LOGOUT_ON_SILENT_LOGIN"));
				boolean silentLogin = Boolean.TRUE.equals(this.getHttpSession().getAttribute(SsoServiceInterface.SILENT_LOGIN));
				// we show/don't show the logout button in case of a silent login,
				// according to configuration
				if (!silentLogin || showLogoutOnSilentLogin) {
					isSatisfied = true;
				}
				break;

			default:
				break;
			}
		} else {
			isSatisfied = true;
		}

		return isSatisfied;
	}

	private JSONObject createMenuNode(Locale locale, MessageBuilder messageBuilder, SourceBean itemSB, boolean isTechnicalUserMenu) throws JSONException {
		JSONObject menu = new JSONObject();

		List containedAttributes = itemSB.getContainedAttributes();

		for (Object objAttribute : containedAttributes) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) objAttribute;

			if (!attribute.getKey().equals(ITEM) && attribute.getValue() != null && !String.valueOf(attribute.getValue()).isEmpty()) {

				String value = (String) attribute.getValue();
				if (!attribute.getKey().equals(REQUIRED_FUNCTIONALITY) && !attribute.getKey().equals(CONDITION) && !attribute.getKey().equals(TO_BE_LICENSED)
						&& !attribute.getKey().equals(ID)) {
					if (attribute.getKey().equals(LABEL)) {
						value = messageBuilder.getMessage((String) attribute.getValue(), locale);
					} else if (attribute.getKey().equals(TO)) {
						value = value.replace(PLACEHOLDER_SPAGOBI_CONTEXT, contextName);
						value = value.replace(PLACEHOLDER_KNOWAGE_VUE_CONTEXT, vueContextName);
						
						value = value.replace(PLACEHOLDER_SPAGO_ADAPTER_HTTP, GeneralUtilities.getSpagoAdapterHttpUrl());

						value = value.replace(PLACEHOLDER_KNOWAGE_THEME, currentTheme);
					}

					menu.put(attribute.getKey(), value);
				}

			}
			if (isTechnicalUserMenu) {
				String strId = (String) itemSB.getAttribute(ID);
				if (strId != null) {
					Integer id = Integer.valueOf(strId);
					technicalUserMenuIds.add(id);
				}
			}
		}

		return menu;
	}

	private Object getChildren(List filteredMenuList, List children, int level, Locale locale) throws JSONException {
		JSONArray tempMenuList = new JSONArray();
		for (int j = 0; j < children.size(); j++) {
			Menu childElem = (Menu) children.get(j);
			tempMenuList = createUserMenuElement(filteredMenuList, childElem, locale, level, tempMenuList);
		}
		return tempMenuList;
	}

	private JSONArray createUserMenuElement(List filteredMenuList, Menu childElem, Locale locale, int level, JSONArray tempMenuList) throws JSONException {
		JSONObject temp2 = new JSONObject();

		String path = MenuUtilities.getMenuPath(filteredMenuList, childElem, locale);

		MessageBuilder msgBuild = new MessageBuilder();
		String text = "";

		if (childElem.getParentId() != null && childElem.getParentId().equals(childElem.getMenuId())) {
			// Incorrect menu configuration handling. Nodes with parent_id equals to menu_id will be attached to root.
			childElem.setParentId(null);
		}

		/* TEXT PROPERTY HANDLING */
		if (!childElem.isAdminsMenu() || !childElem.getName().startsWith("#"))
			text = childElem.getName();
		else {
			if (childElem.getName().startsWith("#")) {
				String titleCode = childElem.getName().substring(1);

				try {
					switch (titleCode) {
					case "menu.ServerManager":
					case "menu.CacheManagement":
						Class.forName("it.eng.knowage.tools.servermanager.importexport.ExporterMetadata", false, this.getClass().getClassLoader());
						break;
					}
				} catch (ClassNotFoundException e) {
					return tempMenuList;
				}

				text = msgBuild.getMessage(titleCode, locale);
			} else {
				text = childElem.getName();
			}
		}

		level++;
		if (childElem.getGroupingMenu() != null && childElem.getGroupingMenu().equals("true")) {
			temp2.put(LABEL, text);
			temp2.put(ICON_CLS, childElem.getIconCls());
		} else {
			if (childElem.getIcon() != null) {
				temp2.put(ICON_CLS, childElem.getIcon().getClassName());
			}
			if (childElem.getCustIcon() != null) {
				temp2.put(CUST_ICON, childElem.getCustIcon().getSrc());
			}

			temp2.put(LABEL, text);

			/* DESCR PROPERTY HANDLING */
			String descr = "";
			if (!childElem.isAdminsMenu() || !childElem.getName().startsWith("#"))
				descr = childElem.getDescr();
			else if (childElem.getName().startsWith("#")) {
//				String titleCode = childElem.getDescr().substring(1);
//
//				try {
//					switch (titleCode) {
//					case "menu.ServerManager":
//					case "menu.CacheManagement":
//						Class.forName("it.eng.knowage.tools.servermanager.importexport.ExporterMetadata", false, this.getClass().getClassLoader());
//						break;
//					}
//				} catch (ClassNotFoundException e) {
//					return tempMenuList;
//				}
//
//				descr = msgBuild.getMessage(titleCode, locale);
			} else {
				descr = childElem.getDescr();
			}

			temp2.put(DESCR, descr);

			if (childElem.getObjId() != null) {
				setPropertiesForObjectMenu(childElem, temp2, path);
			} else if (childElem.getStaticPage() != null && !childElem.getStaticPage().equals("")) {
				setPropertiesForStaticMenu(childElem, temp2, path);
			} else if (StringUtilities.isNotEmpty(childElem.getFunctionality())) {
				setPropertiesForFunctionalityMenu(childElem, temp2, path);
			} else if (childElem.getExternalApplicationUrl() != null && !childElem.getExternalApplicationUrl().isEmpty()) {
				setPropertiesForExternalAppMenu(childElem, temp2, path);
			} else if (childElem.isAdminsMenu() && childElem.getUrl() != null) {
				setPropertiesForAdminWithUrlMenu(childElem, locale, temp2, path);
			}

		}

		if (childElem.getHasChildren()) {
			List childrenBis = childElem.getLstChildren();
			JSONArray tempMenuList2 = (JSONArray) getChildren(filteredMenuList, childrenBis, level, locale);
			if (childElem.getGroupingMenu() != null && childElem.getGroupingMenu().equals("true")) {
				temp2.put(ITEMS, tempMenuList2);
			} else {
				temp2.put(ITEMS, tempMenuList2);
			}
		}

		tempMenuList.put(temp2);

		return tempMenuList;
	}

	private void setPropertiesForObjectMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		if (childElem.isClickable() == true) {
			temp2.put(TO, contextName + "/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=" + childElem.getMenuId());
		} else {
			temp2.put("isClickable", "false");
		}
	}

	private void setPropertiesForAdminWithUrlMenu(Menu childElem, Locale locale, JSONObject temp2, String path) throws JSONException {
		String url = childElem.getUrl();
		url = url.replace(PLACEHOLDER_SPAGOBI_CONTEXT, contextName);
		url = url.replace(PLACEHOLDER_SPAGO_ADAPTER_HTTP, GeneralUtilities.getSpagoAdapterHttpUrl());

		temp2.put(TO, contextName + url);
	}

	private void setPropertiesForExternalAppMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put(URL, StringEscapeUtils.escapeJavaScript(childElem.getExternalApplicationUrl()));
	}

	private void setPropertiesForFunctionalityMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put(URL, StringEscapeUtils.escapeJavaScript(DetailMenuModule.findFunctionalityUrl(childElem, contextName)));
	}

	private void setPropertiesForStaticMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put(TO, contextName + "/servlet/AdapterHTTP?ACTION_NAME=READ_HTML_FILE&MENU_ID=" + childElem.getMenuId());
	}

	private boolean isAbleTo(String func, List funcs) {
		return funcs.contains(func);
	}

	public IEngUserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(IEngUserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

}
