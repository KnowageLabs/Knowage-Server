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
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
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

	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String TITLE_ALIGN = "titleAlign";
	public static final String COLUMNS = "columns";
	public static final String ICON = "icon";
	public static final String CUST_ICON = "custIcon";
	public static final String ICON_CLS = "iconCls";
//	public static final String ICON_ALIGN = "iconAlign";
//	public static final String SCALE = "scale";
	public static final String TOOLTIP = "tooltip";
	public static final String SRC = "src";
	public static final String XTYPE = "xtype";
	public static final String PATH = "path";
	public static final String HREF = "href";
	public static final String FIRST_URL = "firstUrl";
	public static final String LINK_TYPE = "linkType";

	public static final String MENU = "menu";

	public static final String NAME = "name";
	public static final String TEXT = "text";
	public static final String DESCR = "descr";
	public static final String ITEMS = "items";
	public static final String LABEL = "itemLabel";
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
			List filteredMenuList = (List) o;
			JSONArray tempFirstLevelMenuList = new JSONArray();
			JSONArray userMenu = new JSONArray();
			if (filteredMenuList != null && !filteredMenuList.isEmpty()) {
				result = new JSONObject();

				JSONArray menuUserList = new JSONArray();
				MessageBuilder msgBuild = new MessageBuilder();

				JSONObject personal = new JSONObject(); // MB

//				String userMenuMessage = msgBuild.getMessage("menu.UserMenu", locale);
//				personal.put(ICON_CLS, "spagobi");
//				personal.put(TOOLTIP, userMenuMessage);
//				personal.put(PATH, userMenuMessage);
//
//				tempFirstLevelMenuList.put(personal);
//				boolean isAdmin = false;
//				for (int i = 0; i < filteredMenuList.size(); i++) {
//					Menu menuElem = (Menu) filteredMenuList.get(i);
//					String path = MenuUtilities.getMenuPath(filteredMenuList, menuElem, locale);
//
//					if (menuElem.getLevel().intValue() == 1) {
//
//						JSONObject temp = new JSONObject();
//
//						if (!menuElem.isAdminsMenu()) {
//							// Create custom Menu elements (menu defined by the
//							// users)
//
//							menuUserList = createUserMenuElement(filteredMenuList, menuElem, locale, 1, menuUserList);
//							personal.put(MENU, menuUserList);
//
//							if (menuElem.getHasChildren()) {
//
//								List lstChildrenLev2 = menuElem.getLstChildren();
//								JSONArray tempMenuList2 = (JSONArray) getChildren(filteredMenuList, lstChildrenLev2, 1, locale);
//								temp.put(MENU, tempMenuList2);
//							}
//						} else {
//							// This part create the elements for the admin menu
//							isAdmin = true;
//
//							temp.put(ICON_CLS, menuElem.getIconCls());
//
//							String text = "";
//							if (!menuElem.isAdminsMenu() || !menuElem.getName().startsWith("#"))
//
//								// text = msgBuild.getI18nMessage(locale, menuElem.getName());
//								text = menuElem.getName();
//							else {
//								if (menuElem.getName().startsWith("#")) {
//									String titleCode = menuElem.getName().substring(1);
//									text = msgBuild.getMessage(titleCode, locale);
//								} else {
//									text = menuElem.getName();
//								}
//							}
//							temp.put(TOOLTIP, text);
////							temp.put(ICON_ALIGN, "top");
////							temp.put(SCALE, "large");
//							temp.put(PATH, path);
////							temp.put(TARGET, "_self");
//
//							// if (menuElem.getCode() != null && menuElem.getCode().equals("doc_admin_angular")) {
//							if (menuElem.getCode() != null
//									&& (menuElem.getCode().equals("doc_admin_angular") || menuElem.getCode().equals("doc_test_angular"))) {
//								temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER_ANGULAR + "', '" + text + "')");
//								temp.put(FIRST_URL, contextName + HREF_DOC_BROWSER_ANGULAR);
//								temp.put(LINK_TYPE, "execDirectUrl");
//							}
//
//							/**
//							 * The URL for the Workspace web page.
//							 *
//							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
//							 */
//							if (menuElem.getCode() != null && menuElem.getCode().equals("workspace")) {
//
//								temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER_WORKSPACE + "', '" + text + "')");
//								temp.put(FIRST_URL, contextName + HREF_DOC_BROWSER_WORKSPACE);
//								temp.put(LINK_TYPE, "execDirectUrl");
//
//							}
//
//							if (menuElem.getHasChildren()) {
//								List lstChildrenLev2 = menuElem.getLstChildren();
//								JSONArray tempMenuList = (JSONArray) getChildren(filteredMenuList, lstChildrenLev2, 1, locale);
//								temp.put(MENU, tempMenuList);
//							}
//							if (menuElem.getCode().equals("doc_test_angular") && UserUtilities.isAdministrator(this.getUserProfile())) {
//								continue;
//							}
//
//							userMenu.put(temp);
//						}
//					}
//				}
			}
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
			JSONArray adminMenuJSONArray = createAdminMenu(menuDefinitionFile, locale);
			JSONArray userMenuJSONArray = createUserMenu(menuDefinitionFile, locale);
			JSONArray customMenuJSONArray = createCustomMenu(menuDefinitionFile, locale);

			JSONObject wholeMenu = new JSONObject();
			wholeMenu.put("fixedMenu", fixedMenuJSONArray);
			wholeMenu.put("adminMenu", adminMenuJSONArray);
			wholeMenu.put("userMenu", userMenuJSONArray);
			wholeMenu.put("customMenu", customMenuJSONArray);

			result = wholeMenu;

//			createEndUserMenu(locale, 5, new JSONArray());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		return result;
	}

	private JSONArray createEndUserMenu(Locale locale, int level, JSONArray tempMenuList) throws JSONException, EMFUserError, EMFInternalError {

		MessageBuilder messageBuilder = new MessageBuilder();

		List funcs = (List) userProfile.getFunctionalities();

		String strActiveSignup = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
		boolean activeSignup = strActiveSignup.equalsIgnoreCase("true");

		String strMyAccountMenu = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.MY_ACCOUNT_MENU");
		boolean myAccountMenu = !"false".equalsIgnoreCase(strMyAccountMenu); // default value is true, for backward compatibility

		String securityServiceSupplier = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
		boolean isInternalSecurityServiceSupplier = securityServiceSupplier.equalsIgnoreCase("it.eng.spagobi.security.InternalSecurityServiceSupplierImpl");
		boolean isPublicUser = userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID);
		if (isInternalSecurityServiceSupplier && !isPublicUser && myAccountMenu) {
			// build myAccount
			JSONObject myAccount = new JSONObject();

			myAccount.put(ICON_CLS, "fa fa-2x fa-address-card");
			myAccount.put(TOOLTIP, "My Account");
			myAccount.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + "/restful-services/signup/prepareUpdate', \'Modify user\')");
			myAccount.put(FIRST_URL, contextName + "/restful-services/signup/prepareUpdate");
			myAccount.put(LINK_TYPE, "execDirectUrl");

			tempMenuList.put(myAccount);
		}

		// workspace is added unconditionally

		String strSbiSocialAnalysisStatus = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_IS_ACTIVE");
		boolean sbiSocialAnalysisStatus = "TRUE".equalsIgnoreCase(strSbiSocialAnalysisStatus);
		JSONObject socialAnalysis = new JSONObject();
		socialAnalysis.put(ICON_CLS, "public");
		socialAnalysis.put(TOOLTIP, messageBuilder.getMessage("menu.SocialAnalysis", locale));
		socialAnalysis.put(HREF,
				"javascript:execDirectUrl('" + HREF_SOCIAL_ANALYSIS + "?" + SsoServiceInterface.USER_ID + "=" + userProfile.getUserUniqueIdentifier().toString()
						+ "&" + SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry()
						+ "');");
		socialAnalysis.put(FIRST_URL, HREF_SOCIAL_ANALYSIS + "?" + SsoServiceInterface.USER_ID + "=" + userProfile.getUserUniqueIdentifier().toString() + "&"
				+ SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry());
		socialAnalysis.put(LINK_TYPE, "execDirectUrl");

		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.SocialAnalysis",
				SpagoBIConstants.CREATE_SOCIAL_ANALYSIS, socialAnalysis.get(FIRST_URL), socialAnalysis.get(ICON_CLS)));
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.SocialAnalysis",
				SpagoBIConstants.VIEW_SOCIAL_ANALYSIS, socialAnalysis.get(FIRST_URL), socialAnalysis.get(ICON_CLS)));
		tempMenuList.put(socialAnalysis);

		JSONObject glossaryManagementTechnical = new JSONObject();
		glossaryManagementTechnical.put(ICON_CLS, "fa fa-2x fa-book");
		glossaryManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.glossary.technical", locale));
		glossaryManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_GLOSSARY_TECHNICAL + "');");
		glossaryManagementTechnical.put(FIRST_URL, contextName + HREF_MANAGE_GLOSSARY_TECHNICAL);
		glossaryManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(glossaryManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.glossary.technical",
				SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL, glossaryManagementTechnical.get(FIRST_URL), glossaryManagementTechnical.get(ICON_CLS)));

		glossaryManagementTechnical = new JSONObject();
		glossaryManagementTechnical.put(ICON_CLS, "fa fa-2x fa-building");
		glossaryManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.glossary.business", locale));
		glossaryManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_GLOSSARY_BUSINESS + "');");
		glossaryManagementTechnical.put(FIRST_URL, contextName + HREF_MANAGE_GLOSSARY_BUSINESS);
		glossaryManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(glossaryManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.glossary.business",
				SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS, glossaryManagementTechnical.get(FIRST_URL), glossaryManagementTechnical.get(ICON_CLS)));

//			JSONObject o = new JSONObject();
//			o.put(ICON_CLS, "fa fa-2x fa-exchange");
//			o.put(label, messageBuilder.getMessage("menu.cross.definition", locale));
////			o.put(ICON_ALIGN, "top");
////			o.put(SCALE, "large");
////			o.put(TARGET, "_self");
//			o.put(to, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_CROSS_DEFINITION + "');");
//			o.put(FIRST_URL, contextName + HREF_MANAGE_CROSS_DEFINITION);
//			o.put(LINK_TYPE, "execDirectUrl");
//			o.put(command, "function()")
//			tempMenuList.put(o);

		JSONObject calendar = new JSONObject();
		calendar.put(ICON_CLS, "fa fa-2x fa-calendar");
		calendar.put(TOOLTIP, messageBuilder.getMessage("menu.calendar", locale));
		calendar.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_CALENDAR + "');");
		calendar.put(FIRST_URL, contextName + HREF_CALENDAR);
		calendar.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(calendar);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.calendar",
				SpagoBIConstants.MANAGE_CALENDAR, calendar.get(FIRST_URL), calendar.get(ICON_CLS)));

		JSONObject lovsManagementTechnical = new JSONObject();
		lovsManagementTechnical.put(ICON_CLS, "fa fa-2x fa-list");
		lovsManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.lovs.management", locale)); // TODO
		lovsManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_LOVS + "');");
		lovsManagementTechnical.put(FIRST_URL, contextName + HREF_MANAGE_LOVS);
		lovsManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(lovsManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.lovs.management",
				SpagoBIConstants.LOVS_MANAGEMENT, lovsManagementTechnical.get(FIRST_URL), lovsManagementTechnical.get(ICON_CLS)));
		// add

		JSONObject tenantManagementTechnical = new JSONObject();
		tenantManagementTechnical.put(ICON_CLS, "insert_drive_file");
		tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.template.management", locale)); // TODO
		tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_TEMPLATE_MANAGEMENT + "');");
		tenantManagementTechnical.put(FIRST_URL, contextName + HREF_TEMPLATE_MANAGEMENT);
		tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(tenantManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.template.management",
				SpagoBIConstants.TEMPLATE_MANAGEMENT, tenantManagementTechnical.get(FIRST_URL), tenantManagementTechnical.get(ICON_CLS)));

		tenantManagementTechnical = new JSONObject();
		tenantManagementTechnical.put(ICON_CLS, "description");
		tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.document", locale)); // TODO
		tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_DOCUMENT + "');");
		tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_DOCUMENT);
		tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(tenantManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.importexport.document",
				SpagoBIConstants.IMP_EXP_DOCUMENT, tenantManagementTechnical.get(FIRST_URL), tenantManagementTechnical.get(ICON_CLS)));

		tenantManagementTechnical = new JSONObject();
		tenantManagementTechnical.put(ICON_CLS, "rotate_90_degrees_ccw");
		tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.resources", locale)); // TODO
		tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_RESOURCE + "');");
		tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_RESOURCE);
		tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(tenantManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.importexport.resources",
				SpagoBIConstants.IMP_EXP_RESOURCES, tenantManagementTechnical.get(FIRST_URL), tenantManagementTechnical.get(ICON_CLS)));

		tenantManagementTechnical = new JSONObject();
		tenantManagementTechnical.put(ICON_CLS, "portrait");
		tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.users", locale)); // TODO
		tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_USER + "');");
		tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_USER);
		tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(tenantManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.importexport.users",
				SpagoBIConstants.IMP_EXP_USERS, tenantManagementTechnical.get(FIRST_URL), tenantManagementTechnical.get(ICON_CLS)));

		tenantManagementTechnical = new JSONObject();
		tenantManagementTechnical.put(ICON_CLS, "portrait");
		tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.glossary", locale)); // TODO
		tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_USER + "');");
		tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_USER);
		tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(tenantManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.importexport.glossary",
				SpagoBIConstants.IMP_EXP_GLOSSARY, tenantManagementTechnical.get(FIRST_URL), tenantManagementTechnical.get(ICON_CLS)));

		tenantManagementTechnical = new JSONObject();
		tenantManagementTechnical.put(ICON_CLS, "style"); // TODO: change
															// icon
		tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.catalog", locale)); // TODO
		tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_CATALOG + "');");
		tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_CATALOG);
		tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(tenantManagementTechnical);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.importexport.catalog",
				SpagoBIConstants.IMP_EXP_CATALOG, tenantManagementTechnical.get(FIRST_URL), tenantManagementTechnical.get(ICON_CLS)));

		JSONObject i18n = new JSONObject();
		i18n.put(ICON_CLS, "fa fa-2x fa-flag");
		i18n.put(TOOLTIP, messageBuilder.getMessage("menu.i18n", locale));
		i18n.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_I18N + "');");
		i18n.put(FIRST_URL, contextName + HREF_I18N);
		i18n.put(LINK_TYPE, "execDirectUrl");
		tempMenuList.put(i18n);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.i18n",
				SpagoBIConstants.MANAGE_INTERNATIONALIZATION, i18n.get(FIRST_URL), i18n.get(ICON_CLS)));

		// if (isAbleTo(SpagoBIConstants.SEE_NEWS, funcs)) {
		JSONObject news = new JSONObject();
		news.put(ICON_CLS, "fa fa-2x fa-newspaper-o");
		news.put(TOOLTIP, messageBuilder.getMessage("menu.news", locale));
		news.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_I18N + "');");
		news.put(FIRST_URL, contextName + HREF_I18N);
		news.put(LINK_TYPE, "news");
		tempMenuList.put(news);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.news", SpagoBIConstants.SEE_NEWS,
				news.get(FIRST_URL), news.get(ICON_CLS)));
		// }

		JSONObject download = new JSONObject();
		download.put(ICON_CLS, "fa fa-2x fa-download");
		download.put(TOOLTIP, messageBuilder.getMessage("menu.Download", locale));
		download.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_I18N + "');");
		download.put(FIRST_URL, contextName + HREF_I18N);
		download.put(LINK_TYPE, "downloads");
		tempMenuList.put(download);
		System.out.println(String.format("<ITEM label=\"%s\" functionality=\"%s\" to=\"%s\" iconCls=\"%s\" />", "menu.Download", "", download.get(FIRST_URL),
				download.get(ICON_CLS)));

		// end
		LowFunctionality personalFolder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode("USER_FUNCT", false);
		JSONObject myFolder = new JSONObject();
		if (personalFolder != null) {
			Integer persFoldId = personalFolder.getId();
			myFolder = createMenuItem("folder_special", "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION&node=" + persFoldId,
					messageBuilder.getMessage("menu.MyFolder", locale), true, null);
			tempMenuList.put(myFolder);
		}

		return tempMenuList;
	}

	private JSONObject createMenuItem(String icon, String href, String tooltip, boolean idDirectLink, String label) throws JSONException {
		JSONObject menuItem = new JSONObject();
		menuItem.put(ICON_CLS, icon);
		if (label != null) {
			menuItem.put(LABEL, label);
		}
		if (idDirectLink) {
			menuItem.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + href + "', '" + tooltip + "')");
			menuItem.put(LINK_TYPE, "execDirectUrl");
			menuItem.put(FIRST_URL, contextName + href);
		} else {
			if (label != null && label.equals(INFO)) {
				menuItem.put(HREF, "javascript:info()");
				menuItem.put(LINK_TYPE, "info");
			} else if (label != null && label.equals(ROLE)) {
				menuItem.put(HREF, "javascript:roleSelection()");
				menuItem.put(LINK_TYPE, "roleSelection");
			} else if (label != null && label.equals(LANG)) {
				menuItem.put(LINK_TYPE, "languageSelection");
			} else if (label != null && label.equals(HELP)) {
				menuItem.put(HREF, "http://wiki.spagobi.org/xwiki/bin/view/Main/");
				menuItem.put(FIRST_URL, "http://wiki.spagobi.org/xwiki/bin/view/Main/");
				menuItem.remove(TARGET);
				menuItem.put(TARGET, "_blank");
			} else if (href != null && href.length() > 0) {
				menuItem.put(HREF, "javascript:execUrl('" + contextName + href + "')");
				menuItem.put(LINK_TYPE, "execUrl");
			}
		}

		if (label != null && label.equals(HELP)) {
			menuItem.put(LINK_TYPE, "externalUrl");
		} else {
			menuItem.put(FIRST_URL, contextName + href);
		}
		return menuItem;
	}

	private JSONArray createUserMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		logger.debug("IN");

		return createMenu(menuDefinitionFile, locale, "USER", false);
	}

	private JSONArray createFixedMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		logger.debug("IN");

		return createMenu(menuDefinitionFile, locale, "FIXED", false);
	}

	private JSONArray createAdminMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		logger.debug("IN");

		return createMenu(menuDefinitionFile, locale, "ADMIN", true);
	}

	private JSONArray createCustomMenu(SourceBean menuDefinitionFile, Locale locale) throws JSONException {

		return new JSONArray();
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

				List menuCategory = ((SourceBean) domain).getAttributeAsList("GROUP_ITEM");

				for (Object groupItem : menuCategory) {

					List itemsSBList = ((SourceBean) groupItem).getAttributeAsList("ITEM");

					JSONArray children = createItemsArray(locale, messageBuilder, funcs, itemsSBList);

					if (children.length() > 0) {

						SourceBean objSB = (SourceBean) groupItem;
						JSONObject groupItemJSON = createMenuNode(locale, messageBuilder, objSB);
						groupItemJSON.put("items", children);

						tempMenuList.put(groupItemJSON);
					}
				}

			}
		} else {
			for (Object domain : attributeList) {
				List itemsSBList = ((SourceBean) domain).getAttributeAsList("ITEM");
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
			String type = String.valueOf(itemSB.getAttribute("type"));
			if (itemSB.getAttribute("type") == null || isAbleTo(type, funcs)) {

				JSONObject menu = createMenuNode(locale, messageBuilder, itemSB);

				items.put(menu);
			}
		}

		return items;
	}

	private JSONObject createMenuNode(Locale locale, MessageBuilder messageBuilder, SourceBean itemSB) throws JSONException {
		JSONObject menu = new JSONObject();

		List containedAttributes = itemSB.getContainedAttributes();

		for (Object objAttribute : containedAttributes) {
			SourceBeanAttribute attribute = (SourceBeanAttribute) objAttribute;

			if (!attribute.getKey().equals("ITEM") && attribute.getValue() != null && !String.valueOf(attribute.getValue()).isEmpty()) {

				String value = (String) attribute.getValue();
				if (attribute.getKey().equals("label")) {
					value = messageBuilder.getMessage((String) attribute.getValue(), locale);
				} else if (attribute.getKey().equals("to")) {
					value = value.replace("user_id=", "user_id=" + userProfile.getUserUniqueIdentifier());
				}
				menu.put(attribute.getKey(), value);
			}
		}

		return menu;
	}

//	private JSONArray createFixedMenu(Locale locale, int level, JSONArray tempMenuList) throws JSONException {
//
//		MessageBuilder messageBuilder = new MessageBuilder();
//
//		JSONObject lang = createMenuItem("flag", "", messageBuilder.getMessage("menu.Languages", locale), false, "LANG");
//
//		JSONObject roles = createMenuItem("assignment_ind", "", messageBuilder.getMessage("menu.RoleSelection", locale), false, "ROLE");
//
//		JSONObject info = createMenuItem("info", "", messageBuilder.getMessage("menu.info", locale), false, "INFO");
//
//		// JSONObject help = createMenuItem("help", "",
//		// messageBuilder.getMessage("menu.help", locale), false, "HELP");
//
//		tempMenuList.put(roles);
//
//		tempMenuList.put(lang);
//
//		// tempMenuList.put(help);
//
//		tempMenuList.put(info);
//
//		if (PublicProfile.isPublicUser(userProfile.getUserUniqueIdentifier().toString())
//				|| userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID)) {
//			JSONObject login = createMenuItem("input", HREF_LOGIN, messageBuilder.getMessage("menu.login", locale), false, null);
//			tempMenuList.put(login);
//		} else {
//
//			HttpSession session = this.getHttpSession();
//
//			boolean showLogoutOnSilentLogin = Boolean.valueOf(SingletonConfig.getInstance().getConfigValue("SPAGOBI.HOME.SHOW_LOGOUT_ON_SILENT_LOGIN"));
//			boolean silentLogin = Boolean.TRUE.equals(session.getAttribute(SsoServiceInterface.SILENT_LOGIN));
//			// we show/don't show the logout button in case of a silent login,
//			// according to configuration
//			if (!silentLogin || showLogoutOnSilentLogin) {
//				JSONObject power = createMenuItem("power_settings_new", HREF_LOGOUT, messageBuilder.getMessage("menu.logout", locale), false, null);
//				tempMenuList.put(power);
//			}
//		}
//
//		return tempMenuList;		logger.debug("OUT");
//	}

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
		if (!childElem.isAdminsMenu() || !childElem.getName().startsWith("#"))
			// text = msgBuild.getI18nMessage(locale, childElem.getName());
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
			temp2.put(TITLE, text);
			temp2.put(TITLE_ALIGN, "left");
			temp2.put(COLUMNS, 1);
			temp2.put(XTYPE, "buttongroup");
			temp2.put(ICON_CLS, childElem.getIconCls());
		} else {
			if (childElem.getIcon() != null) {
				temp2.put(ICON, new JSONObject(childElem.getIcon()));
			}
			if (childElem.getCustIcon() != null) {
				temp2.put(CUST_ICON, new JSONObject(childElem.getCustIcon()));
			}

			temp2.put(TEXT, text);

			String descr = "";
			if (!childElem.isAdminsMenu() || !childElem.getName().startsWith("#"))
				// text = msgBuild.getI18nMessage(locale, childElem.getName());
				descr = childElem.getDescr();
			else {
				if (childElem.getName().startsWith("#")) {
					String titleCode = childElem.getDescr().substring(1);

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

					descr = msgBuild.getMessage(titleCode, locale);
				} else {
					descr = childElem.getDescr();
				}
			}
			temp2.put(DESCR, descr);
			temp2.put("style", "text-align: left;");
			temp2.put(TARGET, "_self");
			temp2.put(ICON_CLS, "bullet");

			String src = childElem.getUrl();

			if (childElem.getObjId() != null) {
				if (childElem.isClickable() == true) {
					temp2.put(HREF, "javascript:execDirectUrl('" + contextName + "/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID="
							+ childElem.getMenuId() + "', '" + path + "' )");
					temp2.put(LINK_TYPE, "execDirectUrl");
					temp2.put(SRC, contextName + "/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=" + childElem.getMenuId());
				} else {
					temp2.put("isClickable", "false");
				}
			} else if (childElem.getStaticPage() != null && !childElem.getStaticPage().equals("")) {
				temp2.put(HREF, "javascript:execDirectUrl('" + contextName + "/servlet/AdapterHTTP?ACTION_NAME=READ_HTML_FILE&MENU_ID=" + childElem.getMenuId()
						+ "', '" + path + "' )");
				temp2.put(LINK_TYPE, "execDirectUrl");
				temp2.put(SRC, contextName + "/servlet/AdapterHTTP?ACTION_NAME=READ_HTML_FILE&MENU_ID=" + childElem.getMenuId());
			} else if (StringUtilities.isNotEmpty(childElem.getFunctionality())) {
				String finalUrl = "javascript:execDirectUrl('" + DetailMenuModule.findFunctionalityUrl(childElem, contextName) + "', '" + path + "')";
				temp2.put(HREF, finalUrl);
				temp2.put(LINK_TYPE, "execDirectUrl");
				temp2.put(SRC, DetailMenuModule.findFunctionalityUrl(childElem, contextName));
			} else if (childElem.getExternalApplicationUrl() != null && !childElem.getExternalApplicationUrl().isEmpty()) {
				temp2.put(HREF,
						"javascript:callExternalApp('" + StringEscapeUtils.escapeJavaScript(childElem.getExternalApplicationUrl()) + "', '" + path + "')");
				temp2.put(LINK_TYPE, "callExternalApp");
				temp2.put(SRC, childElem.getExternalApplicationUrl());
			} else if (childElem.isAdminsMenu() && childElem.getUrl() != null) {
				String url = "javascript:execDirectUrl('" + childElem.getUrl() + "'";
				url = url.replace("${SPAGOBI_CONTEXT}", contextName);
				url = url.replace("${SPAGO_ADAPTER_HTTP}", GeneralUtilities.getSpagoAdapterHttpUrl());
				src = src.replace("${SPAGOBI_CONTEXT}", contextName);
				src = src.replace("${SPAGO_ADAPTER_HTTP}", GeneralUtilities.getSpagoAdapterHttpUrl());
				path = path.replace("#", "");

				// code to manage SpagoBISocialAnalysis link in admin menu
				if (url.contains("${SPAGOBI_SOCIAL_ANALYSIS_URL}")) {
					url = url.substring(0, url.length() - 1);
					url = url.replace("${SPAGOBI_SOCIAL_ANALYSIS_URL}", SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_URL"));
					src = src.replace("${SPAGOBI_SOCIAL_ANALYSIS_URL}", SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_URL"));
					// if (!GeneralUtilities.isSSOEnabled()) {
					url = url + "?" + SsoServiceInterface.USER_ID + "=" + userProfile.getUserUniqueIdentifier().toString() + "&" + SpagoBIConstants.SBI_LANGUAGE
							+ "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry() + "'";
				}
				temp2.put(SRC, src);
				temp2.put(HREF, url + ", '" + path + "')");
				String linkType = childElem.getLinkType() == null ? "execDirectUrl" : childElem.getLinkType();
				temp2.put(LINK_TYPE, linkType);
			}

		}
		if (childElem.getHasChildren()) {
			List childrenBis = childElem.getLstChildren();
			JSONArray tempMenuList2 = (JSONArray) getChildren(filteredMenuList, childrenBis, level, locale);
			if (childElem.getGroupingMenu() != null && childElem.getGroupingMenu().equals("true")) {
				temp2.put(ITEMS, tempMenuList2);
			} else {
				temp2.put(MENU, tempMenuList2);
			}
		}

		tempMenuList.put(temp2);
		return tempMenuList;
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
