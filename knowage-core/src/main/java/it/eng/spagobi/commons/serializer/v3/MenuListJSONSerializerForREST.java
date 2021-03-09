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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.Serializer;
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

	private static final String CONDITION = "condition";

	static private Logger logger = Logger.getLogger(MenuListJSONSerializerForREST.class);

	private static final String TYPE = "type";
	private static final String GROUP_ITEM = "GROUP_ITEM";
	private static final String ITEM = "ITEM";
	private static final String USER = "USER";
	private static final String FIXED = "FIXED";
	private static final String ADMIN = "ADMIN";
	private static final String LABEL = "label";
	private static final String ITEMS = "items";
	private static final String TO = "to";

	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String TITLE_ALIGN = "titleAlign";
	public static final String COLUMNS = "columns";
	public static final String ICON = "icon";
	public static final String CUST_ICON = "custIcon";
	public static final String ICON_CLS = "iconCls";
	public static final String SRC = "src";
	public static final String XTYPE = "xtype";
	public static final String PATH = "path";
	public static final String HREF = "href";
	public static final String FIRST_URL = "firstUrl";

	public static final String MENU = "menu";

	public static final String NAME = "name";
	public static final String TEXT = "text";
	public static final String DESCR = "descr";
	public static final String INFO = "INFO";
	public static final String ROLE = "ROLE";
	public static final String LANG = "LANG";
	public static final String HOME = "HOME";
	public static final String TARGET = "hrefTarget";
	public static final String HELP = "HELP";

	// OLD DOC MANAGER
	private static final String HREF_DOC_BROWSER_ANGULAR = "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ANGULAR_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";

	/**
	 * The URL for the Workspace web page.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	private static final String HREF_DOC_BROWSER_WORKSPACE = "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_WORKSPACE&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";

	private static final String HREF_BOOKMARK = "/servlet/AdapterHTTP?PAGE=HOT_LINK_PAGE&OPERATION=GET_HOT_LINK_LIST&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_SUBSCRIPTIONS = "/servlet/AdapterHTTP?PAGE=ListDistributionListUserPage&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_PENCIL = "/servlet/AdapterHTTP?ACTION_NAME=CREATE_DOCUMENT_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYANALYSIS=TRUE";
	private static final String HREF_MYDATA = "/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=true";
	private static final String HREF_MYDATA_ADMIN = "/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=false";
	private static final String HREF_LOGIN = "/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE";
	private static final String HREF_LOGOUT = "/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE";
	private static final String HREF_SOCIAL_ANALYSIS = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_URL");
	private static final String HREF_HIERARCHIES_MANAGEMENT = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierarchiesEditor.jsp";
	private static final String HREF_MANAGE_GLOSSARY_TECHNICAL = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/technicaluser/glossaryTechnical.jsp";
	private static final String HREF_MANAGE_GLOSSARY_BUSINESS = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/glossary/businessuser/glossaryBusiness.jsp";
	private static final String HREF_MANAGE_CROSS_DEFINITION = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/cross/definition/crossDefinition.jsp";

	private static final String HREF_CALENDAR = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/calendar/calendarTemplate.jsp";

	private static final String HREF_MANAGE_DOMAIN = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/domain/domainManagement.jsp";
	private static final String HREF_MANAGE_CONFIG = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/config/configManagement.jsp";
	private static final String HREF_MANAGE_TENANT = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/multitenant/multitenantManagementAngular.jsp";
	private static final String HREF_MANAGE_UDP = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/udp/manageUdpAngular.jsp";

	// private static final String HREF_USERS =
	// "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_USER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_USERS = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/catalogue/usersManagement.jsp";

	private static final String HREF_MANAGE_LOVS = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/catalogue/lovsManagement.jsp";
	private static final String HREF_FUNCTIONS_CATALOG = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/functionsCatalog/functionsCatalog.jsp";

	private static final String HREF_TEMPLATE_MANAGEMENT = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/servermanager/templateManagement.jsp";
	private static final String HREF_IMPEXP_DOCUMENT = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/servermanager/importExportDocuments/importExportDocuments.jsp";
	private static final String HREF_IMPEXP_RESOURCE = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/servermanager/importExportResources.jsp";
	private static final String HREF_IMPEXP_USER = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/servermanager/importExportUsers/importExportUsers.jsp";
	private static final String HREF_IMPEXP_CATALOG = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/servermanager/importExportCatalog/importExportCatalog.jsp";

	private static final String HREF_WORKSPACE = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/workspace/workspaceManagement.jsp";

	private static final String HREF_I18N = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/internationalization/internationalization.jsp";

	public String contextName = "";
	public String defaultThemePath = "/themes/sbi_default";

	private IEngUserProfile userProfile;
	private HttpSession httpSession;

	public MenuListJSONSerializerForREST(IEngUserProfile userProfile, HttpSession session) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		this.setUserProfile(userProfile);
		this.setHttpSession(session);
	}

	@Override
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		contextName = KnowageSystemConfiguration.getKnowageContext();
		if (!(o instanceof List)) {
			throw new SerializationException("MenuListJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
//
//			if (!UserUtilities.isTechnicalUser(this.getUserProfile())) {
//				userMenu = createEndUserMenu(locale, 1, new JSONArray());
//			}

			InputStream is = null;
			SourceBean menuDefinitionFile = null;
			String configurationFileName = "it/eng/knowage/menu/menu.xml";

			try {
				Thread curThread = Thread.currentThread();
				ClassLoader classLoad = curThread.getContextClassLoader();
				is = classLoad.getResourceAsStream(configurationFileName);
				InputSource source = new InputSource(is);
				menuDefinitionFile = SourceBean.fromXMLStream(source);
				logger.debug("Configuration successfully read from resource " + configurationFileName);
			} catch (Exception e) {
				logger.error("Error while reading configuration from resource " + configurationFileName, e);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
						logger.error(e);
					}
				logger.debug("OUT");
			}

			JSONArray fixedMenuJSONArray = createFixedMenu(menuDefinitionFile, locale);

			JSONArray adminMenuJSONArray = new JSONArray();
			if (UserUtilities.isTechnicalUser(this.getUserProfile())) {
				adminMenuJSONArray = createAdminMenu(menuDefinitionFile, locale);
			}

			JSONArray userMenuJSONArray = new JSONArray();
			userMenuJSONArray = createUserMenu(menuDefinitionFile, locale);

			List filteredMenuList = (List) o;
			JSONArray customMenuJSONArray = createCustomMenu(filteredMenuList, locale);

			JSONObject wholeMenu = new JSONObject();
			wholeMenu.put("fixedMenu", fixedMenuJSONArray);
			wholeMenu.put("adminMenu", adminMenuJSONArray);
			wholeMenu.put("userMenu", userMenuJSONArray);
			wholeMenu.put("customMenu", customMenuJSONArray);

			result = wholeMenu;

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		return result;
	}

//	private JSONObject createMenuItem(String icon, String href, String tooltip, boolean idDirectLink, String label) throws JSONException {
//		JSONObject menuItem = new JSONObject();
//		menuItem.put(ICON_CLS, icon);
//		if (label != null) {
//			menuItem.put(LABEL, label);
//		}
//		if (idDirectLink) {
//			menuItem.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + href + "', '" + tooltip + "')");
//			menuItem.put(FIRST_URL, contextName + href);
//		} else {
//			if (label != null && label.equals(INFO)) {
//				menuItem.put(HREF, "javascript:info()");
//			} else if (label != null && label.equals(ROLE)) {
//				menuItem.put(HREF, "javascript:roleSelection()");
//			} else if (label != null && label.equals(LANG)) {
//			} else if (label != null && label.equals(HELP)) {
//				menuItem.put(HREF, "http://wiki.spagobi.org/xwiki/bin/view/Main/");
//				menuItem.put(FIRST_URL, "http://wiki.spagobi.org/xwiki/bin/view/Main/");
//				menuItem.remove(TARGET);
//				menuItem.put(TARGET, "_blank");
//			} else if (href != null && href.length() > 0) {
//				menuItem.put(HREF, "javascript:execUrl('" + contextName + href + "')");
//			}
//		}
//
//		if (label != null && label.equals(HELP)) {
//		} else {
//			menuItem.put(FIRST_URL, contextName + href);
//		}
//		return menuItem;
//	}

	private JSONArray createUserMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		logger.debug("IN");

		JSONArray userMenu = createMenu(menuDefinitionFile, locale, USER, false);

		logger.debug("OUT");

		return userMenu;
	}

	private JSONArray createFixedMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		logger.debug("IN");

		JSONArray fixedMenu = createMenu(menuDefinitionFile, locale, FIXED, false);

		logger.debug("OUT");

		return fixedMenu;
	}

	private JSONArray createAdminMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		logger.debug("IN");

		JSONArray adminMenu = createMenu(menuDefinitionFile, locale, ADMIN, true);

		logger.debug("OUT");

		return adminMenu;
	}

	private JSONArray createCustomMenu(List filteredMenuList, Locale locale) throws JSONException {

		JSONArray tempFirstLevelMenuList = new JSONArray();
		JSONArray userMenu = new JSONArray();

		if (filteredMenuList != null && !filteredMenuList.isEmpty()) {

			JSONArray menuUserList = new JSONArray();
			MessageBuilder msgBuild = new MessageBuilder();

			JSONObject personal = new JSONObject(); // MB
			String userMenuMessage = msgBuild.getMessage("menu.UserMenu", locale);
			personal.put(ICON_CLS, "spagobi");
			personal.put(LABEL, userMenuMessage);

			tempFirstLevelMenuList.put(personal);
			for (int i = 0; i < filteredMenuList.size(); i++) {
				Menu menuElem = (Menu) filteredMenuList.get(i);
				String path = MenuUtilities.getMenuPath(filteredMenuList, menuElem, locale);

				if (menuElem.getLevel().intValue() == 1) {

					JSONObject temp = new JSONObject();

					if (!menuElem.isAdminsMenu()) {
						// Create custom Menu elements (menu defined by the users)

						menuUserList = createUserMenuElement(filteredMenuList, menuElem, locale, 1, menuUserList);
						personal.put(ITEMS, menuUserList);

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
						temp.put(PATH, path);

						if (menuElem.getCode() != null && (menuElem.getCode().equals("doc_admin_angular") || menuElem.getCode().equals("doc_test_angular"))) {
							temp.put(TO, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER_ANGULAR + "', '" + text + "')");
							temp.put(FIRST_URL, contextName + HREF_DOC_BROWSER_ANGULAR);
						}

						/**
						 * The URL for the Workspace web page.
						 *
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						if (menuElem.getCode() != null && menuElem.getCode().equals("workspace")) {
							temp.put(TO, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER_WORKSPACE + "', '" + text + "')");
							temp.put(FIRST_URL, contextName + HREF_DOC_BROWSER_WORKSPACE);
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
		return tempFirstLevelMenuList;
	}

	private JSONArray createMenu(SourceBean menuDefinitionFile, Locale locale, String attribute, boolean isAdminMenu) throws JSONException {
		MessageBuilder messageBuilder = new MessageBuilder();
		List attributeList = menuDefinitionFile.getAttributeAsList(attribute);
		return buildMenuTreeBranch(locale, messageBuilder, attributeList, isAdminMenu);
	}

	private JSONArray buildMenuTreeBranch(Locale locale, MessageBuilder messageBuilder, List attributeList, boolean isAdminMenu) throws JSONException {
		JSONArray tempMenuList = new JSONArray();
		List funcs = null;
		try {
			funcs = (List) userProfile.getFunctionalities();
		} catch (EMFInternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isAdminMenu) {
			for (Object domain : attributeList) {

				List menuCategory = ((SourceBean) domain).getAttributeAsList(GROUP_ITEM);

				for (Object groupItem : menuCategory) {

					List itemsSBList = ((SourceBean) groupItem).getAttributeAsList(ITEM);

					JSONArray children = createItemsArray(locale, messageBuilder, funcs, itemsSBList);

					if (children.length() > 0) {

						SourceBean objSB = (SourceBean) groupItem;
						JSONObject groupItemJSON = createMenuNode(locale, messageBuilder, objSB);
						groupItemJSON.put(ITEMS, children);

						tempMenuList.put(groupItemJSON);
					}
				}

			}
		} else {
			for (Object domain : attributeList) {
				List itemsSBList = ((SourceBean) domain).getAttributeAsList(ITEM);
				JSONArray children = createItemsArray(locale, messageBuilder, funcs, itemsSBList);
				tempMenuList = children;
			}
		}

		return tempMenuList;
	}

	private JSONArray createItemsArray(Locale locale, MessageBuilder messageBuilder, List funcs, List itemsSBList) throws JSONException {
		JSONArray items = new JSONArray();
		for (Object item : itemsSBList) {

			SourceBean itemSB = (SourceBean) item;
			String type = (String) itemSB.getAttribute(TYPE);
			String condition = (String) itemSB.getAttribute(CONDITION);

			if (type == null || isAbleTo(type, funcs) || (condition != null && menuConditionIsSatisfied(condition))) {

				JSONObject menu = createMenuNode(locale, messageBuilder, itemSB);
				items.put(menu);
			}
		}

		return items;
	}

	private boolean menuConditionIsSatisfied(String condition) {
		boolean isSatisfied = false;
		if (condition != null && !condition.isEmpty()) {
			switch (condition) {

			case "my_account":
				String strMyAccountMenu = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.MY_ACCOUNT_MENU");
				boolean myAccountMenu = !"false".equalsIgnoreCase(strMyAccountMenu); // default value is true, for backward compatibility

				String securityServiceSupplier = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
				boolean isInternalSecurityServiceSupplier = securityServiceSupplier.equalsIgnoreCase(InternalSecurityServiceSupplierImpl.class.getName());
				boolean isPublicUser = userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID);
				if (isInternalSecurityServiceSupplier && !isPublicUser && myAccountMenu) {
					isSatisfied = true;
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

	private JSONObject createMenuNode(Locale locale, MessageBuilder messageBuilder, SourceBean itemSB) throws JSONException {
		JSONObject menu = new JSONObject();

		List containedAttributes = itemSB.getContainedAttributes();

		for (Object objAttribute : containedAttributes) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) objAttribute;

			if (!attribute.getKey().equals(ITEM) && attribute.getValue() != null && !String.valueOf(attribute.getValue()).isEmpty()) {

				String value = (String) attribute.getValue();
				if (!attribute.getKey().equals(TYPE) && !attribute.getKey().equals(CONDITION)) {
					if (attribute.getKey().equals(LABEL)) {
						value = messageBuilder.getMessage((String) attribute.getValue(), locale);
					} else if (attribute.getKey().equals(TO)) {

						value = value.replace("${SPAGOBI_CONTEXT}", contextName);
						value = value.replace("${SPAGO_ADAPTER_HTTP}", GeneralUtilities.getSpagoAdapterHttpUrl());
						value = value.replace("user_id=", "user_id=" + userProfile.getUserUniqueIdentifier());

						if (value.contains("node=")) {
							LowFunctionality personalFolder;
							try {
								personalFolder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode("USER_FUNCT", false);
								if (personalFolder != null) {
									value = value.replace("node=", "node=" + personalFolder.getId());
								}
							} catch (EMFUserError e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					menu.put(attribute.getKey(), value);
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
			// HANDLE wrong menu cases
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
				temp2.put(ICON, childElem.getIcon().getClassName());
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
//			temp2.put(ICON_CLS, "bullet");

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
		url = url.replace("${SPAGOBI_CONTEXT}", contextName);
		url = url.replace("${SPAGO_ADAPTER_HTTP}", GeneralUtilities.getSpagoAdapterHttpUrl());

		temp2.put(TO, contextName + url);
	}

	private void setPropertiesForExternalAppMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put("url", StringEscapeUtils.escapeJavaScript(childElem.getExternalApplicationUrl()));
	}

	private void setPropertiesForFunctionalityMenu(Menu childElem, JSONObject temp2, String path) throws JSONException {
		temp2.put("url", StringEscapeUtils.escapeJavaScript(DetailMenuModule.findFunctionalityUrl(childElem, contextName)));
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
