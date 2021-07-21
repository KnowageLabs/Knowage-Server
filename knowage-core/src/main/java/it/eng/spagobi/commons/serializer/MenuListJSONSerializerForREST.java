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
package it.eng.spagobi.commons.serializer;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.services.DetailMenuModule;
import it.eng.spagobi.wapp.util.MenuUtilities;

/**
 * @author Marco Cortella
 */
public class MenuListJSONSerializerForREST implements Serializer {

	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String TITLE_ALIGN = "titleAlign";
	public static final String COLUMNS = "columns";
	public static final String ICON = "icon";
	public static final String CUST_ICON = "custIcon";
	public static final String ICON_CLS = "iconCls";
	public static final String ICON_ALIGN = "iconAlign";
	public static final String SCALE = "scale";
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
	private static final String HREF_DOC_BROWSER = "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
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
	private static final String HREF_HIERARCHIES_MANAGEMENT = "/restful-services/publish?PUBLISHER=hierarchiesEditor";
	private static final String HREF_MANAGE_GLOSSARY_TECHNICAL = "/restful-services/publish?PUBLISHER=glossaryTechnical";
	private static final String HREF_MANAGE_GLOSSARY_BUSINESS = "/restful-services/publish?PUBLISHER=glossaryBusiness";
	private static final String HREF_MANAGE_CROSS_DEFINITION = "/restful-services/publish?PUBLISHER=crossDefinition";

	private static final String HREF_CALENDAR = "/restful-services/publish?PUBLISHER=calendarTemplate";

	private static final String HREF_MANAGE_DOMAIN = "/restful-services/publish?PUBLISHER=domainManagement";
	private static final String HREF_MANAGE_CONFIG = "/restful-services/publish?PUBLISHER=configManagement";
	private static final String HREF_MANAGE_TENANT = "/restful-services/publish?PUBLISHER=multitenantManagementAngular";
	private static final String HREF_MANAGE_UDP = "/restful-services/publish?PUBLISHER=manageUdpAngular";

	// private static final String HREF_USERS =
	// "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_USER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_USERS = "/restful-services/publish?PUBLISHER=usersManagement";

	private static final String HREF_MANAGE_LOVS = "/restful-services/publish?PUBLISHER=lovsManagement";
	private static final String HREF_FUNCTIONS_CATALOG = "/restful-services/publish?PUBLISHER=functionsCatalog";

	private static final String HREF_TEMPLATE_MANAGEMENT = "/restful-services/publish?PUBLISHER=templateManagement";
	private static final String HREF_IMPEXP_DOCUMENT = "/restful-services/publish?PUBLISHER=importExportDocuments";
	private static final String HREF_IMPEXP_RESOURCE = "/restful-services/publish?PUBLISHER=importExportResources";
	private static final String HREF_IMPEXP_USER = "/restful-services/publish?PUBLISHER=importExportUsers";
	private static final String HREF_IMPEXP_CATALOG = "/restful-services/publish?PUBLISHER=importExportCatalog";

	private static final String HREF_WORKSPACE = "/restful-services/publish?PUBLISHER=workspaceManagement";

	private static final String HREF_I18N = "/restful-services/publish?PUBLISHER=internationalization";

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

				String userMenuMessage = msgBuild.getMessage("menu.UserMenu", locale);
				personal.put(ICON_CLS, "spagobi");
				personal.put(TOOLTIP, userMenuMessage);
				personal.put(ICON_ALIGN, "top");
				personal.put(SCALE, "large");
				personal.put(PATH, userMenuMessage);
				personal.put(TARGET, "_self");

				tempFirstLevelMenuList.put(personal);
				boolean isAdmin = false;
				for (int i = 0; i < filteredMenuList.size(); i++) {
					Menu menuElem = (Menu) filteredMenuList.get(i);
					String path = MenuUtilities.getMenuPath(filteredMenuList, menuElem, locale);

					if (menuElem.getLevel().intValue() == 1) {

						JSONObject temp = new JSONObject();

						if (!menuElem.isAdminsMenu()) {
							// Create custom Menu elements (menu defined by the
							// users)

							menuUserList = createUserMenuElement(filteredMenuList, menuElem, locale, 1, menuUserList);
							personal.put(MENU, menuUserList);

							if (menuElem.getHasChildren()) {

								List lstChildrenLev2 = menuElem.getLstChildren();
								JSONArray tempMenuList2 = (JSONArray) getChildren(filteredMenuList, lstChildrenLev2, 1, locale);
								temp.put(MENU, tempMenuList2);
							}
						} else {
							// This part create the elements for the admin menu
							isAdmin = true;

							temp.put(ICON_CLS, menuElem.getIconCls());

							String text = "";
							if (!menuElem.isAdminsMenu() || !menuElem.getName().startsWith("#"))

								// text = msgBuild.getI18nMessage(locale, menuElem.getName());
								text = menuElem.getName();
							else {
								if (menuElem.getName().startsWith("#")) {
									String titleCode = menuElem.getName().substring(1);
									text = msgBuild.getMessage(titleCode, locale);
								} else {
									text = menuElem.getName();
								}
							}
							temp.put(TOOLTIP, text);
							temp.put(ICON_ALIGN, "top");
							temp.put(SCALE, "large");
							temp.put(PATH, path);
							temp.put(TARGET, "_self");

							// if (menuElem.getCode() != null && menuElem.getCode().equals("doc_admin_angular")) {
							if (menuElem.getCode() != null
									&& (menuElem.getCode().equals("doc_admin_angular") || menuElem.getCode().equals("doc_test_angular"))) {
								temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER_ANGULAR + "', '" + text + "')");
								temp.put(FIRST_URL, contextName + HREF_DOC_BROWSER_ANGULAR);
								temp.put(LINK_TYPE, "execDirectUrl");
							}

							/**
							 * The URL for the Workspace web page.
							 *
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							if (menuElem.getCode() != null && menuElem.getCode().equals("workspace")) {

								temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER_WORKSPACE + "', '" + text + "')");
								temp.put(FIRST_URL, contextName + HREF_DOC_BROWSER_WORKSPACE);
								temp.put(LINK_TYPE, "execDirectUrl");

							}

							if (menuElem.getHasChildren()) {
								List lstChildrenLev2 = menuElem.getLstChildren();
								JSONArray tempMenuList = (JSONArray) getChildren(filteredMenuList, lstChildrenLev2, 1, locale);
								temp.put(MENU, tempMenuList);
							}
							if (menuElem.getCode().equals("doc_test_angular") && UserUtilities.isAdministrator(this.getUserProfile())) {
								continue;
							}

							userMenu.put(temp);
						}
					}
				}
			}

			if (!UserUtilities.isTechnicalUser(this.getUserProfile())) {
				userMenu = createEndUserMenu(locale, 1, new JSONArray());
			}

			JSONArray fixedMenuPart = createFixedMenu(locale, 1, new JSONArray());

			JSONObject wholeMenu = new JSONObject();
			wholeMenu.put("fixedMenu", fixedMenuPart);
			wholeMenu.put("userMenu", userMenu);
			wholeMenu.put("customMenu", tempFirstLevelMenuList);

			result = wholeMenu;
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
			myAccount.put(ICON_ALIGN, "top");
			myAccount.put(SCALE, "large");
			myAccount.put(TARGET, "_self");
			myAccount.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + "/restful-services/signup/prepareUpdate', \'Modify user\')");
			myAccount.put(FIRST_URL, contextName + "/restful-services/signup/prepareUpdate");
			myAccount.put(LINK_TYPE, "execDirectUrl");

			tempMenuList.put(myAccount);
		}
		if (isAbleTo(SpagoBIConstants.SEE_DOCUMENT_BROWSER, funcs)) {
			JSONObject browserAngular = createMenuItem("fa fa-2x fa-folder-o", HREF_DOC_BROWSER_ANGULAR, messageBuilder.getMessage("menu.Browser", locale),
					true, null);
			tempMenuList.put(browserAngular);
		}
		if (isAbleTo(SpagoBIConstants.SEE_FAVOURITES, funcs)) {
			JSONObject favourites = createMenuItem("fa fa-2x fa-bookmark", HREF_BOOKMARK, messageBuilder.getMessage("menu.MyFavorites", locale), true, null);
			tempMenuList.put(favourites);
		}
		if (isAbleTo(SpagoBIConstants.SEE_SUBSCRIPTIONS, funcs)) {
			JSONObject subscriptions = createMenuItem("fa fa-2x fa-envelope", HREF_SUBSCRIPTIONS, messageBuilder.getMessage("menu.Subscriptions", locale), true,
					null);
			tempMenuList.put(subscriptions);
		}
		if (isAbleTo(SpagoBIConstants.FINAL_USERS_MANAGEMENT, funcs)) {
			JSONObject createDoc = createMenuItem("fa fa-2x fa-users", HREF_USERS, messageBuilder.getMessage("menu.Users", locale), true, null);
			tempMenuList.put(createDoc);
		}

		// workspace is added unconditionally
		if (isAbleTo(SpagoBIConstants.SEE_MY_WORKSPACE, funcs)) {
			JSONObject workspace = new JSONObject();
			workspace.put(ICON_CLS, "fa fa-2x fa-briefcase");
			workspace.put(TOOLTIP, messageBuilder.getMessage("menu.workspace", locale));
			workspace.put(ICON_ALIGN, "top");
			workspace.put(SCALE, "large");
			workspace.put(TARGET, "_self");
			workspace.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_WORKSPACE + "');");
			workspace.put(LINK_TYPE, "execDirectUrl");
			workspace.put(FIRST_URL, contextName + HREF_WORKSPACE);
			tempMenuList.put(workspace);
		}

		String strSbiSocialAnalysisStatus = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_IS_ACTIVE");
		boolean sbiSocialAnalysisStatus = "TRUE".equalsIgnoreCase(strSbiSocialAnalysisStatus);
		if (sbiSocialAnalysisStatus && (isAbleTo(SpagoBIConstants.CREATE_SOCIAL_ANALYSIS, funcs) || isAbleTo(SpagoBIConstants.VIEW_SOCIAL_ANALYSIS, funcs))) {
			JSONObject socialAnalysis = new JSONObject();
			socialAnalysis.put(ICON_CLS, "public");
			socialAnalysis.put(TOOLTIP, messageBuilder.getMessage("menu.SocialAnalysis", locale));
			socialAnalysis.put(ICON_ALIGN, "top");
			socialAnalysis.put(SCALE, "large");
			socialAnalysis.put(TARGET, "_self");
			// if (!GeneralUtilities.isSSOEnabled()) {
			socialAnalysis.put(HREF,
					"javascript:execDirectUrl('" + HREF_SOCIAL_ANALYSIS + "?" + SsoServiceInterface.USER_ID + "="
							+ userProfile.getUserUniqueIdentifier().toString() + "&" + SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&"
							+ SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry() + "');");
			socialAnalysis.put(FIRST_URL, HREF_SOCIAL_ANALYSIS + "?" + SsoServiceInterface.USER_ID + "=" + userProfile.getUserUniqueIdentifier().toString()
					+ "&" + SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry());
			socialAnalysis.put(LINK_TYPE, "execDirectUrl");

			tempMenuList.put(socialAnalysis);
		}

		if (isAbleTo(SpagoBIConstants.HIERARCHIES_MANAGEMENT, funcs)) {
			JSONObject hierarchiesManagement = new JSONObject();
			hierarchiesManagement.put(ICON_CLS, "fa fa-2x fa-sitemap");
			hierarchiesManagement.put(TOOLTIP, messageBuilder.getMessage("menu.HierarchiesManagement", locale));
			hierarchiesManagement.put(ICON_ALIGN, "top");
			hierarchiesManagement.put(SCALE, "large");
			hierarchiesManagement.put(TARGET, "_self");
			hierarchiesManagement.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_HIERARCHIES_MANAGEMENT + "');");
			hierarchiesManagement.put(LINK_TYPE, "execDirectUrl");
			hierarchiesManagement.put(FIRST_URL, contextName + HREF_HIERARCHIES_MANAGEMENT);
			tempMenuList.put(hierarchiesManagement);
		}

		if (isAbleTo(SpagoBIConstants.FUNCTIONS_CATALOG_USAGE, funcs)) {
			JSONObject functionsCatalog = new JSONObject();
			functionsCatalog.put(ICON_CLS, "fa fa-2x fa-cubes");
			functionsCatalog.put(TOOLTIP, messageBuilder.getMessage("menu.FunctionsCatalog", locale));
			functionsCatalog.put(ICON_ALIGN, "top");
			functionsCatalog.put(SCALE, "large");
			functionsCatalog.put(TARGET, "_self");
			functionsCatalog.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_FUNCTIONS_CATALOG + "');");
			functionsCatalog.put(LINK_TYPE, "execDirectUrl");
			functionsCatalog.put(FIRST_URL, contextName + HREF_FUNCTIONS_CATALOG);
			tempMenuList.put(functionsCatalog);
		}

		if (isAbleTo(SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL, funcs)) {
			JSONObject glossaryManagementTechnical = new JSONObject();
			glossaryManagementTechnical.put(ICON_CLS, "fa fa-2x fa-book");
			glossaryManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.glossary.technical", locale));
			glossaryManagementTechnical.put(ICON_ALIGN, "top");
			glossaryManagementTechnical.put(SCALE, "large");
			glossaryManagementTechnical.put(TARGET, "_self");
			glossaryManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_GLOSSARY_TECHNICAL + "');");
			glossaryManagementTechnical.put(FIRST_URL, contextName + HREF_MANAGE_GLOSSARY_TECHNICAL);
			glossaryManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(glossaryManagementTechnical);
		}

		if (isAbleTo(SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS, funcs)) {
			JSONObject glossaryManagementTechnical = new JSONObject();
			glossaryManagementTechnical.put(ICON_CLS, "fa fa-2x fa-building");
			glossaryManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.glossary.business", locale));
			glossaryManagementTechnical.put(ICON_ALIGN, "top");
			glossaryManagementTechnical.put(SCALE, "large");
			glossaryManagementTechnical.put(TARGET, "_self");
			glossaryManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_GLOSSARY_BUSINESS + "');");
			glossaryManagementTechnical.put(FIRST_URL, contextName + HREF_MANAGE_GLOSSARY_BUSINESS);
			glossaryManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(glossaryManagementTechnical);
		}

		if (isAbleTo(SpagoBIConstants.MANAGE_CROSS_NAVIGATION, funcs)) {
			JSONObject o = new JSONObject();
			o.put(ICON_CLS, "fa fa-2x fa-exchange");
			o.put(TOOLTIP, messageBuilder.getMessage("menu.cross.definition", locale));
			o.put(ICON_ALIGN, "top");
			o.put(SCALE, "large");
			o.put(TARGET, "_self");
			o.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_CROSS_DEFINITION + "');");
			o.put(FIRST_URL, contextName + HREF_MANAGE_CROSS_DEFINITION);
			o.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(o);
		}

		if (isAbleTo(SpagoBIConstants.MANAGE_CALENDAR, funcs)) {
			JSONObject calendar = new JSONObject();
			calendar.put(ICON_CLS, "fa fa-2x fa-calendar");
			calendar.put(TOOLTIP, messageBuilder.getMessage("menu.calendar", locale));
			calendar.put(ICON_ALIGN, "top");
			calendar.put(SCALE, "large");
			calendar.put(TARGET, "_self");
			calendar.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_CALENDAR + "');");
			calendar.put(FIRST_URL, contextName + HREF_CALENDAR);
			calendar.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(calendar);
		}

		if (isAbleTo(SpagoBIConstants.LOVS_MANAGEMENT, funcs)) {
			JSONObject lovsManagementTechnical = new JSONObject();
			lovsManagementTechnical.put(ICON_CLS, "fa fa-2x fa-list");
			lovsManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.lovs.management", locale)); // TODO
			lovsManagementTechnical.put(ICON_ALIGN, "top");
			lovsManagementTechnical.put(SCALE, "large");
			lovsManagementTechnical.put(TARGET, "_self");
			lovsManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_MANAGE_LOVS + "');");
			lovsManagementTechnical.put(FIRST_URL, contextName + HREF_MANAGE_LOVS);
			lovsManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(lovsManagementTechnical);
		}
		// add

		if (isAbleTo(SpagoBIConstants.TEMPLATE_MANAGEMENT, funcs)) {
			JSONObject tenantManagementTechnical = new JSONObject();
			tenantManagementTechnical.put(ICON_CLS, "insert_drive_file");
			tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.template.management", locale)); // TODO
			tenantManagementTechnical.put(ICON_ALIGN, "top");
			tenantManagementTechnical.put(SCALE, "large");
			tenantManagementTechnical.put(TARGET, "_self");
			tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_TEMPLATE_MANAGEMENT + "');");
			tenantManagementTechnical.put(FIRST_URL, contextName + HREF_TEMPLATE_MANAGEMENT);
			tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(tenantManagementTechnical);
		}
		if (isAbleTo(SpagoBIConstants.IMP_EXP_DOCUMENT, funcs)) {
			JSONObject tenantManagementTechnical = new JSONObject();
			tenantManagementTechnical.put(ICON_CLS, "description");
			tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.document", locale)); // TODO
			tenantManagementTechnical.put(ICON_ALIGN, "top");
			tenantManagementTechnical.put(SCALE, "large");
			tenantManagementTechnical.put(TARGET, "_self");
			tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_DOCUMENT + "');");
			tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_DOCUMENT);
			tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(tenantManagementTechnical);
		}
		if (isAbleTo(SpagoBIConstants.IMP_EXP_RESOURCES, funcs)) {
			JSONObject tenantManagementTechnical = new JSONObject();
			tenantManagementTechnical.put(ICON_CLS, "rotate_90_degrees_ccw");
			tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.resources", locale)); // TODO
			tenantManagementTechnical.put(ICON_ALIGN, "top");
			tenantManagementTechnical.put(SCALE, "large");
			tenantManagementTechnical.put(TARGET, "_self");
			tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_RESOURCE + "');");
			tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_RESOURCE);
			tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(tenantManagementTechnical);
		}
		if (isAbleTo(SpagoBIConstants.IMP_EXP_USERS, funcs)) {
			JSONObject tenantManagementTechnical = new JSONObject();
			tenantManagementTechnical.put(ICON_CLS, "portrait");
			tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.users", locale)); // TODO
			tenantManagementTechnical.put(ICON_ALIGN, "top");
			tenantManagementTechnical.put(SCALE, "large");
			tenantManagementTechnical.put(TARGET, "_self");
			tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_USER + "');");
			tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_USER);
			tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(tenantManagementTechnical);
		}
		if (isAbleTo(SpagoBIConstants.IMP_EXP_GLOSSARY, funcs)) {
			JSONObject tenantManagementTechnical = new JSONObject();
			tenantManagementTechnical.put(ICON_CLS, "portrait");
			tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.glossary", locale)); // TODO
			tenantManagementTechnical.put(ICON_ALIGN, "top");
			tenantManagementTechnical.put(SCALE, "large");
			tenantManagementTechnical.put(TARGET, "_self");
			tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_USER + "');");
			tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_USER);
			tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(tenantManagementTechnical);
		}
		if (isAbleTo(SpagoBIConstants.IMP_EXP_CATALOG, funcs)) {
			JSONObject tenantManagementTechnical = new JSONObject();
			tenantManagementTechnical.put(ICON_CLS, "style"); // TODO: change
																// icon
			tenantManagementTechnical.put(TOOLTIP, messageBuilder.getMessage("menu.importexport.catalog", locale)); // TODO
			tenantManagementTechnical.put(ICON_ALIGN, "top");
			tenantManagementTechnical.put(SCALE, "large");
			tenantManagementTechnical.put(TARGET, "_self");
			tenantManagementTechnical.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_IMPEXP_CATALOG + "');");
			tenantManagementTechnical.put(FIRST_URL, contextName + HREF_IMPEXP_CATALOG);
			tenantManagementTechnical.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(tenantManagementTechnical);
		}

		if (isAbleTo(SpagoBIConstants.MANAGE_INTERNATIONALIZATION, funcs)) {
			JSONObject i18n = new JSONObject();
			i18n.put(ICON_CLS, "fa fa-2x fa-flag");
			i18n.put(TOOLTIP, messageBuilder.getMessage("menu.i18n", locale));
			i18n.put(ICON_ALIGN, "top");
			i18n.put(SCALE, "large");
			i18n.put(TARGET, "_self");
			i18n.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_I18N + "');");
			i18n.put(FIRST_URL, contextName + HREF_I18N);
			i18n.put(LINK_TYPE, "execDirectUrl");
			tempMenuList.put(i18n);
		}

		// if (isAbleTo(SpagoBIConstants.SEE_NEWS, funcs)) {
		JSONObject news = new JSONObject();
		news.put(ICON_CLS, "fa fa-2x fa-newspaper-o");
		news.put(TOOLTIP, messageBuilder.getMessage("menu.news", locale));
		news.put(ICON_ALIGN, "top");
		news.put(SCALE, "large");
		news.put(TARGET, "_self");
		news.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_I18N + "');");
		news.put(FIRST_URL, contextName + HREF_I18N);
		news.put(LINK_TYPE, "news");
		tempMenuList.put(news);
		// }

		JSONObject download = new JSONObject();
		download.put(ICON_CLS, "fa fa-2x fa-download");
		download.put(TOOLTIP, messageBuilder.getMessage("menu.Download", locale));
		download.put(ICON_ALIGN, "top");
		download.put(SCALE, "large");
		download.put(TARGET, "_self");
		download.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_I18N + "');");
		download.put(FIRST_URL, contextName + HREF_I18N);
		download.put(LINK_TYPE, "downloads");
		tempMenuList.put(download);

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
		menuItem.put(ICON_ALIGN, "top"); // TODO: check if used
		menuItem.put(SCALE, "large"); // TODO: check if used
		menuItem.put(TOOLTIP, "Info");
		menuItem.put(ICON_CLS, icon);
		menuItem.put(TOOLTIP, tooltip);
		menuItem.put(TARGET, "_self"); // TODO: check if used
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
			} else if (label != null && label.equals("ACCESS")) {
				menuItem.put(LINK_TYPE, "accessibilitySettings");
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

	private JSONArray createFixedMenu(Locale locale, int level, JSONArray tempMenuList) throws JSONException {

		MessageBuilder messageBuilder = new MessageBuilder();

		JSONObject lang = createMenuItem("flag", "", messageBuilder.getMessage("menu.Languages", locale), false, "LANG");

		JSONObject accessibility = createMenuItem("accessibility", "", messageBuilder.getMessage("menu.Accessibility", locale), false, "ACCESS");

		JSONObject roles = createMenuItem("assignment_ind", "", messageBuilder.getMessage("menu.RoleSelection", locale), false, "ROLE");

		JSONObject info = createMenuItem("info", "", messageBuilder.getMessage("menu.info", locale), false, "INFO");

		// JSONObject help = createMenuItem("help", "",
		// messageBuilder.getMessage("menu.help", locale), false, "HELP");

		tempMenuList.put(roles);

		tempMenuList.put(lang);

		tempMenuList.put(accessibility);

		// tempMenuList.put(help);

		tempMenuList.put(info);

		if (PublicProfile.isPublicUser(userProfile.getUserUniqueIdentifier().toString())
				|| userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID)) {
			JSONObject login = createMenuItem("input", HREF_LOGIN, messageBuilder.getMessage("menu.login", locale), false, null);
			tempMenuList.put(login);
		} else {

			HttpSession session = this.getHttpSession();

			boolean showLogoutOnSilentLogin = Boolean.valueOf(SingletonConfig.getInstance().getConfigValue("SPAGOBI.HOME.SHOW_LOGOUT_ON_SILENT_LOGIN"));
			boolean silentLogin = Boolean.TRUE.equals(session.getAttribute(SsoServiceInterface.SILENT_LOGIN));
			// we show/don't show the logout button in case of a silent login,
			// according to configuration
			if (!silentLogin || showLogoutOnSilentLogin) {
				JSONObject power = createMenuItem("power_settings_new", HREF_LOGOUT, messageBuilder.getMessage("menu.logout", locale), false, null);
				tempMenuList.put(power);
			}
		}

		return tempMenuList;
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
							+ "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry();
					url += "&" + SpagoBIConstants.SBI_SCRIPT + "=" + locale.getScript() + "'";
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
