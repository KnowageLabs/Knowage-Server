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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
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
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
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

	/*
	 * Menus that are available to non-administrator users but have the required functionality. The map contains the pair (label, id of the technical menu of
	 * which it is a duplicate).
	 */
	private Map<String, Integer> allowedMenuToNotDuplicate = new HashMap<String, Integer>();

	/*
	 * Technical menus that will be available for Community OR Enterprise edition key: Community menu ID Value: Enterprise menu ID
	 */
	private Map<String, String> technicalMenuCommunityOrEnterprise = new HashMap<String, String>();

	public MenuListJSONSerializerForREST(IEngUserProfile userProfile, HttpSession session, String currentTheme) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		this.setUserProfile(userProfile);
		this.setHttpSession(session);
		this.currentTheme = currentTheme;

		allowedMenuToNotDuplicate.put("menu.Users", 2003);
		allowedMenuToNotDuplicate.put("menu.HierarchiesEditor", 5003);
		allowedMenuToNotDuplicate.put("menu.glossary.technical", 4007);
		allowedMenuToNotDuplicate.put("menu.glossary.business", 4008);
		allowedMenuToNotDuplicate.put("menu.cross.definition", 5005);
		allowedMenuToNotDuplicate.put("menu.calendar", 4010);
		allowedMenuToNotDuplicate.put("menu.lovs.management", 3002);
		allowedMenuToNotDuplicate.put("menu.template.management", 8002);
		allowedMenuToNotDuplicate.put("menu.importexport.document", 10002);
		allowedMenuToNotDuplicate.put("menu.importexport.resources", null);
		allowedMenuToNotDuplicate.put("menu.importexport.users", 10004);
		allowedMenuToNotDuplicate.put("menu.importexport.glossary", 10008);
		allowedMenuToNotDuplicate.put("menu.importexport.catalog", 10006);
		allowedMenuToNotDuplicate.put("menu.i18n", 9001);
		allowedMenuToNotDuplicate.put("menu.news", 5007);

		technicalMenuCommunityOrEnterprise.put("5008", "5009");
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

		JSONArray userMenu = createMenu(menuDefinitionFile, locale, technicalUserMenuJSONArray, MenuType.ALLOWED_USER_FUNCTIONALITIES);

		logger.debug("OUT");

		return userMenu;
	}

	private JSONArray createCommonUserFunctionalitiesMenu(SourceBean menuDefinitionFile, JSONArray technicalUserMenuJSONArray, Locale locale)
			throws JSONException, EMFInternalError {

		logger.debug("IN");

		JSONArray commonUserFunctionalitiesMenu = createMenu(menuDefinitionFile, locale, technicalUserMenuJSONArray, MenuType.COMMON_USER_FUNCTIONALITIES);

		logger.debug("OUT");

		return commonUserFunctionalitiesMenu;
	}

	private JSONArray createTechnicalUserMenu(SourceBean menuDefinitionFile, JSONArray technicalUserMenuJSONArray, Locale locale)
			throws JSONException, EMFInternalError {

		logger.debug("IN");

		JSONArray technicalUserMenu = createMenu(menuDefinitionFile, locale, technicalUserMenuJSONArray, MenuType.TECHNICAL_USER_FUNCTIONALITIES);

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

	private JSONArray createMenu(SourceBean menuDefinitionFile, Locale locale, JSONArray technicalUserMenuJSONArray, MenuType menuType)
			throws JSONException, EMFInternalError {
		MessageBuilder messageBuilder = new MessageBuilder();
		List attributeList = menuDefinitionFile.getAttributeAsList(menuType.name());
		return buildMenuTreeBranch(locale, messageBuilder, attributeList, technicalUserMenuJSONArray, menuType);
	}

	private JSONArray buildMenuTreeBranch(Locale locale, MessageBuilder messageBuilder, List attributeList, JSONArray technicalUserMenuJSONArray,
			MenuType menuType) throws JSONException, EMFInternalError {
		JSONArray tempMenuList = new JSONArray();
		List funcs = (List) userProfile.getFunctionalities();

		if (menuType == MenuType.TECHNICAL_USER_FUNCTIONALITIES) {
			for (Object domain : attributeList) {

				List menuCategory = ((SourceBean) domain).getAttributeAsList(GROUP_ITEM);

				for (Object groupItem : menuCategory) {

					SourceBean groupItemSB = (SourceBean) groupItem;

					boolean isGroupItemToAdd = isGroupItemToAdd(groupItemSB);

					if (isGroupItemToAdd) {

						List itemsSBList = groupItemSB.getAttributeAsList(ITEM);

						JSONArray children = createItemsArray(locale, messageBuilder, funcs, technicalUserMenuJSONArray, itemsSBList, menuType);

						if (children.length() > 0) {
							JSONObject groupItemJSON = createMenuNodeAndRecordGroupMenu(locale, messageBuilder, groupItemSB, menuType);
							groupItemJSON.put(ITEMS, children);

							tempMenuList.put(groupItemJSON);
						}
					} else if (isEnterpriseEdition() && groupItemSB.getAttribute("id").equals("8000")) {

						List itemsSBList = groupItemSB.getAttributeAsList(ITEM);
						JSONObject groupItemJSON = createMenuNodeAndRecordGroupMenu(locale, messageBuilder, groupItemSB, menuType);

						for (Object object : itemsSBList) {
							SourceBean objectSB = (SourceBean) object;

							if (objectSB.getAttribute("id").equals("8012")) {
								JSONArray children = new JSONArray();
								JSONObject licenseMenu = createMenuNodeAndRecordGroupMenu(locale, messageBuilder, objectSB, menuType);
								children.put(licenseMenu);
								groupItemJSON.put(ITEMS, children);
								tempMenuList.put(groupItemJSON);
								break;
							}
						}

					}

				}
			}

		} else {
			for (Object domain : attributeList) {
				List itemsSBList = ((SourceBean) domain).getAttributeAsList(ITEM);
				JSONArray children = createItemsArray(locale, messageBuilder, funcs, technicalUserMenuJSONArray, itemsSBList, menuType);
				tempMenuList = children;
			}
		}

		return tempMenuList;
	}

	private JSONArray createItemsArray(Locale locale, MessageBuilder messageBuilder, List funcs, JSONArray technicalUserMenuJSONArray, List itemsSBList,
			MenuType menuType) throws JSONException, EMFInternalError {
		JSONArray items = new JSONArray();
		for (Object item : itemsSBList) {

			SourceBean itemSB = (SourceBean) item;

			if (!isInTechnicalUserMenu(technicalUserMenuJSONArray, itemSB, messageBuilder, locale)) {

				String condition = (String) itemSB.getAttribute(CONDITION);

				boolean addElement = true;

				String requiredFunctionality = (String) itemSB.getAttribute(REQUIRED_FUNCTIONALITY);

				/* ALL_USERS or ALLOWED_USER_FUNCTIONALITIES */
				if (condition != null && !condition.isEmpty()) {
					addElement = menuConditionIsSatisfied(itemSB);
				} else if (StringUtils.isNotBlank(requiredFunctionality)) {
					addElement = false;

					String[] reqFunc = requiredFunctionality.split(",", -1);
					for (int i = 0; i < reqFunc.length; i++) {
						if (isAbleTo(reqFunc[i], funcs)) {
							addElement = isGroupItemToAdd(itemSB);
						}
						if (addElement)
							break;
					}
				}

				if (addElement)
					addElement &= isUserMenuForNotAdmin(menuType, itemSB);

				if (addElement)
					addElement &= isMenuForKnowageCurrentType(menuType, itemSB);

				if (addElement) {
					JSONObject menu = createMenuNode(locale, messageBuilder, itemSB, menuType);
					items.put(menu);
					if (menuType == MenuType.TECHNICAL_USER_FUNCTIONALITIES)
						technicalUserMenuIds.add(Integer.valueOf((String) itemSB.getAttribute(ID)));
				}
			}

		}

		return items;
	}

	/**
	 *
	 * @param menuType
	 * @param itemSB
	 *
	 *                 Method to know if the menu is part of the allowed user functionality that must be added even if the user is not an administrator
	 */
	private boolean isUserMenuForNotAdmin(MenuType menuType, SourceBean itemSB) {
		boolean isToAdd = true;
		if (menuType == MenuType.ALLOWED_USER_FUNCTIONALITIES || menuType == MenuType.TECHNICAL_USER_FUNCTIONALITIES) {
			String menuLabel = (String) itemSB.getAttribute(LABEL);

			if (menuType == MenuType.ALLOWED_USER_FUNCTIONALITIES) {
				// allowed user menu to add only if it is not admin and functionality is permitted in any case
				Integer technicalMenuId = allowedMenuToNotDuplicate.get(menuLabel);
				if (technicalMenuId != null && technicalUserMenuIds.contains(technicalMenuId)) {
					return false;
				}

				try {
					if ("menu.news".equals(menuLabel) && !UserUtilities.hasUserRole(this.getUserProfile())) {
						return false;
					}
				} catch (Exception e) {
					String message = "Error while retrieving user profile";
					logger.debug(message);
					throw new SpagoBIRuntimeException(message, e);
				}
			}

		}

		return isToAdd;

	}

	/**
	 *
	 * @param menuType
	 * @param itemSB
	 *
	 *                 Method handle menus that can be switched from Community to Enterprise
	 */
	private boolean isMenuForKnowageCurrentType(MenuType menuType, SourceBean itemSB) {
		boolean isToAdd = true;
		if (menuType == MenuType.ALLOWED_USER_FUNCTIONALITIES || menuType == MenuType.TECHNICAL_USER_FUNCTIONALITIES) {

			String menuId = (String) itemSB.getAttribute(ID);
			if (menuType == MenuType.TECHNICAL_USER_FUNCTIONALITIES) {
				if (technicalMenuCommunityOrEnterprise.containsKey(menuId)) {
					isToAdd = !isEnterpriseEdition();
					if (!isToAdd)
						return false;
				}

				if (technicalMenuCommunityOrEnterprise.containsValue(menuId)) {
					isToAdd = isEnterpriseEdition();
					if (!isToAdd)
						return false;
				}
			}
		}

		return isToAdd;

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

	private boolean isGroupItemToAdd(SourceBean itemSB) {
		Boolean isLicensed = true;

		String requiredLicensesString = (String) itemSB.getAttribute(TO_BE_LICENSED);
		if (requiredLicensesString != null) {

			if (isEnterpriseEdition()) {
				if (requiredLicensesString.isEmpty()) {
					try {
						Class.forName("it.eng.knowage.tools.servermanager.importexport.ExporterMetadata", false, this.getClass().getClassLoader());

						isLicensed = !DocumentUtilities.getValidLicenses().isEmpty();
					} catch (ClassNotFoundException e) {
						isLicensed = false;
					}
				} else {
					try {
						String[] requiredLicenses = requiredLicensesString.split(",", -1);
						Class productProfilerEE = Class.forName("it.eng.knowage.enterprise.security.ProductProfiler");
						Method getActiveProductsMethod = productProfilerEE.getMethod("getActiveProducts");
						List<String> activeProducts = (List<String>) getActiveProductsMethod.invoke(productProfilerEE);
						for (String lic : requiredLicenses) {
							isLicensed = activeProducts.contains(lic);
							if (isLicensed)
								break;
						}
					} catch (Exception e) {
						isLicensed = false;
					}
				}
			} else {
				isLicensed = false;
			}
		}

		return isLicensed;
	}

	private boolean isEnterpriseEdition() {
		try {
			Class.forName("it.eng.knowage.tools.servermanager.utils.LicenseManager");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
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

	private JSONObject createMenuNodeAndRecordGroupMenu(Locale locale, MessageBuilder messageBuilder, SourceBean itemSB, MenuType menuType)
			throws JSONException {
		JSONObject menu = createMenuNode(locale, messageBuilder, itemSB, menuType);

//		if (menuType == MenuType.TECHNICAL_USER_FUNCTIONALITIES) {
//			String strId = (String) itemSB.getAttribute(ID);
//			if (strId != null) {
//				Integer id = Integer.valueOf(strId);
//				technicalUserMenuIds.add(id);
//			}
//		}

		return menu;
	}

	private JSONObject createMenuNode(Locale locale, MessageBuilder messageBuilder, SourceBean itemSB, MenuType menuType) throws JSONException {
		JSONObject menu = new JSONObject();

		List containedAttributes = itemSB.getContainedAttributes();

		for (Object objAttribute : containedAttributes) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) objAttribute;

			if (isAttributeToIgnore(attribute))
				continue;
			String value = String.valueOf(attribute.getValue());
			String key = attribute.getKey();
			if (!key.equals(ITEM) && StringUtils.isNotBlank(value)) {

				if (key.equals(LABEL)) {
					String menuLabel = (String) attribute.getValue();
					value = messageBuilder.getMessage(menuLabel, locale);
				} else if (key.equals(TO)) {
					value = value.replace(PLACEHOLDER_SPAGOBI_CONTEXT, contextName);
					value = value.replace(PLACEHOLDER_KNOWAGE_VUE_CONTEXT, vueContextName);

					value = value.replace(PLACEHOLDER_SPAGO_ADAPTER_HTTP, GeneralUtilities.getSpagoAdapterHttpUrl());

					value = value.replace(PLACEHOLDER_KNOWAGE_THEME, currentTheme);
				}

				menu.put(key, value);

			}
		}

		return menu;
	}

	private boolean isAttributeToIgnore(SourceBeanAttribute attribute) {
		return attribute.getKey().equals(REQUIRED_FUNCTIONALITY) || attribute.getKey().equals(CONDITION) || attribute.getKey().equals(ID);
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
					case "menu.group.ServerManager":
					case "menu.CacheManagement":
					case "menu.group.ImportExport":
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
			temp2.put("prog", childElem.getProg());

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

		String[] roleNames = new String[childElem.getRoles().length];
		for (int i = 0; i < childElem.getRoles().length; i++) {
			roleNames[i] = childElem.getRoles()[i].getName();
		}
		temp2.put("roles", roleNames);
		tempMenuList.put(temp2);

		return tempMenuList;
	}

	private void setPropertiesForObjectMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
		try {
			BIObject document = dao.loadBIObjectById(childElem.getObjId());
			String documentLink = getDocumentLink(document);
			if (childElem.isClickable() == true) {
				temp2.put(TO, documentLink);
			} else {
				temp2.put("isClickable", "false");
			}
		} catch (Exception e) {
			logger.error("Cannot load menu item for document: " + childElem.getObjId(), e);
		}

	}

	private String getDocumentLink(BIObject document) {
		String documentLabel = document.getLabel();
		String engineLabel = document.getEngineLabel();
		String enginePath;
		switch (engineLabel) {
		case "knowagedossierengine":
			enginePath = "dossier";
			break;
		case "knowagegisengine":
			enginePath = "map";
			break;
		case "knowagekpiengine":
			enginePath = "kpi";
			break;
		case "knowageofficeengine":
			enginePath = "office-doc";
			break;
		default:
			enginePath = "document-composite";
			break;
		}
		return String.format("/%s/%s", enginePath, documentLabel);
	}

	private void setPropertiesForAdminWithUrlMenu(Menu childElem, Locale locale, JSONObject temp2, String path) throws JSONException {
		String url = childElem.getUrl();
		url = url.replace(PLACEHOLDER_SPAGOBI_CONTEXT, contextName);
		url = url.replace(PLACEHOLDER_SPAGO_ADAPTER_HTTP, GeneralUtilities.getSpagoAdapterHttpUrl());

		temp2.put(TO, contextName + url);
	}

	private void setPropertiesForExternalAppMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		String externalAppUrl = childElem.getExternalApplicationUrl();
		temp2.put(URL, StringEscapeUtils.escapeJava(externalAppUrl));
	}

	private void setPropertiesForFunctionalityMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put(TO, StringEscapeUtils.escapeJavaScript(DetailMenuModule.findFunctionalityUrl(childElem, contextName)));
	}

	private void setPropertiesForStaticMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put(TO, contextName + "/servlet/AdapterHTTP?ACTION_NAME=READ_HTML_FILE&MENU_ID=" + childElem.getMenuId());
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

	private boolean isAbleTo(String func, List funcs) {
		boolean toReturn = false;
		for (int i = 0; i < funcs.size(); i++) {
			if (func.equals(funcs.get(i))) {
				toReturn = true;
				break;
			}
		}
		return toReturn;
	}

}
