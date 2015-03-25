/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.services.DetailMenuModule;
import it.eng.spagobi.wapp.util.MenuUtilities;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Monica Franceschini
 */
public class MenuListJSONSerializer implements Serializer {

	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String TITLE_ALIGN = "titleAlign";
	public static final String COLUMNS = "columns";
	public static final String ICON_CLS = "iconCls";
	public static final String ICON_ALIGN = "iconAlign";
	public static final String SCALE = "scale";
	public static final String TOOLTIP = "tooltip";
	public static final String SRC = "src";
	public static final String XTYPE = "xtype";
	public static final String PATH = "path";
	public static final String HREF = "href";
	public static final String FIRST_URL = "firstUrl";

	public static final String MENU = "menu";

	public static final String NAME = "name";
	public static final String TEXT = "text";
	public static final String ITEMS = "items";
	public static final String LABEL = "itemLabel";
	public static final String INFO = "INFO";
	public static final String ROLE = "ROLE";
	public static final String LANG = "LANG";
	public static final String HOME = "HOME";
	public static final String TARGET = "hrefTarget";
	public static final String HELP = "HELP";

	private static final String HREF_DOC_BROWSER = "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_BOOKMARK = "/servlet/AdapterHTTP?PAGE=HOT_LINK_PAGE&OPERATION=GET_HOT_LINK_LIST&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";
	private static final String HREF_PENCIL = "/servlet/AdapterHTTP?ACTION_NAME=CREATE_DOCUMENT_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYANALYSIS=TRUE";
	private static final String HREF_MYDATA = "/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=true";
	private static final String HREF_MYDATA_ADMIN = "/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=false";
	private static final String HREF_LOGIN = "/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE";
	private static final String HREF_LOGOUT = "/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE";
	private static final String HREF_SOCIAL_ANALYSIS = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_URL");
	private static final String HREF_HIERARCHIES_MANAGEMENT = "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/hierarchieseditor/hierarchiesEditor.jsp";
	private static final String HREF_USERS = "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_USER_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE";

	public String contextName = "";
	public String defaultThemePath = "/themes/sbi_default";

	private IEngUserProfile userProfile;
	private HttpSession httpSession;

	public MenuListJSONSerializer(IEngUserProfile userProfile, HttpSession session) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		this.setUserProfile(userProfile);
		this.setHttpSession(session);
	}

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONArray result = null;

		contextName = GeneralUtilities.getSpagoBiContext();
		if (!(o instanceof List)) {
			throw new SerializationException("MenuListJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			List filteredMenuList = (List) o;
			JSONArray tempFirstLevelMenuList = new JSONArray();
			if (filteredMenuList != null && !filteredMenuList.isEmpty()) {
				result = new JSONArray();

				JSONArray menuUserList = new JSONArray();
				MessageBuilder msgBuild = new MessageBuilder();
				// build home
				JSONObject home = new JSONObject();
				JSONObject personal = new JSONObject();

				home.put(ICON_CLS, "home");
				home.put(TOOLTIP, "Home");
				home.put(ICON_ALIGN, "top");
				home.put(SCALE, "large");
				home.put(PATH, "Home");
				home.put(LABEL, HOME);
				home.put(TARGET, "_self");
				home.put(HREF, "javascript:goHome(null, 'spagobi');");

				String userMenu = msgBuild.getMessage("menu.UserMenu", locale);
				personal.put(ICON_CLS, "spagobi");
				personal.put(TOOLTIP, userMenu);
				personal.put(ICON_ALIGN, "top");
				personal.put(SCALE, "large");
				personal.put(PATH, userMenu);
				personal.put(TARGET, "_self");

				tempFirstLevelMenuList.put(home);
				tempFirstLevelMenuList.put(personal);
				boolean isAdmin = false;
				for (int i = 0; i < filteredMenuList.size(); i++) {
					Menu menuElem = (Menu) filteredMenuList.get(i);
					String path = MenuUtilities.getMenuPath(menuElem, locale);

					if (menuElem.getLevel().intValue() == 1) {

						JSONObject temp = new JSONObject();

						if (!menuElem.isAdminsMenu()) {

							menuUserList = createUserMenuElement(menuElem, locale, 1, menuUserList);
							personal.put(MENU, menuUserList);

							if (menuElem.getHasChildren()) {

								List lstChildrenLev2 = menuElem.getLstChildren();
								JSONArray tempMenuList2 = (JSONArray) getChildren(lstChildrenLev2, 1, locale);
								temp.put(MENU, tempMenuList2);
							}
						} else {
							isAdmin = true;

							temp.put(ICON_CLS, menuElem.getIconCls());

							String text = "";
							if (!menuElem.isAdminsMenu() || !menuElem.getName().startsWith("#"))

								text = msgBuild.getI18nMessage(locale, menuElem.getName());
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
							if (menuElem.getCode() != null && menuElem.getCode().equals("doc_admin")) {
								temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_DOC_BROWSER + "', '" + text + "')");
							}
							if (menuElem.getCode() != null && menuElem.getCode().equals("my_data_admin")) {
								// admins and devs can see ONLY models tab,
								// while tester can see datasets and models
								if (UserUtilities.isTechDsManager(this.getUserProfile())) {
									temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_MYDATA_ADMIN + "', '" + text + "')");
								} else if (UserUtilities.isTester(this.getUserProfile())) {
									temp.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + HREF_MYDATA + "', '" + text + "')");
								}
							}

							if (menuElem.getHasChildren()) {

								List lstChildrenLev2 = menuElem.getLstChildren();
								JSONArray tempMenuList = (JSONArray) getChildren(lstChildrenLev2, 1, locale);
								temp.put(MENU, tempMenuList);
							}
							tempFirstLevelMenuList.put(temp);
						}

					}
				}

			}

			if (!UserUtilities.isTechnicalUser(this.getUserProfile())) {
				tempFirstLevelMenuList = createEndUserMenu(locale, 1, tempFirstLevelMenuList);
			}

			tempFirstLevelMenuList = createFixedMenu(locale, 1, tempFirstLevelMenuList);
			result = tempFirstLevelMenuList;
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
		boolean activeSignup = (strActiveSignup.equalsIgnoreCase("true") ? true : false);
		if (activeSignup && !userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID)) {
			// build myAccount
			JSONObject myAccount = new JSONObject();

			myAccount.put(ICON_CLS, "myAccount");
			myAccount.put(TOOLTIP, "My Account");
			myAccount.put(ICON_ALIGN, "top");
			myAccount.put(SCALE, "large");
			myAccount.put(TARGET, "_self");
			myAccount.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + "/restful-services/signup/prepareUpdate', \'Modify user\')");
			tempMenuList.put(myAccount);
		}
		if (isAbleTo(SpagoBIConstants.SEE_DOCUMENT_BROWSER, funcs)) {
			JSONObject browser = createMenuItem("folder_open", HREF_DOC_BROWSER, messageBuilder.getMessage("menu.Browser", locale), true, null);
			tempMenuList.put(browser);
		}
		if (isAbleTo(SpagoBIConstants.SEE_FAVOURITES, funcs)) {
			JSONObject favourites = createMenuItem("bookmark", HREF_BOOKMARK, messageBuilder.getMessage("menu.MyFavorites", locale), true, null);
			tempMenuList.put(favourites);
		}
		if (isAbleTo(SpagoBIConstants.FINAL_USERS_MANAGEMENT, funcs)) {
			JSONObject createDoc = createMenuItem("users", HREF_USERS, messageBuilder.getMessage("menu.Users", locale), true, null);
			tempMenuList.put(createDoc);
		}

		if (isAbleTo(SpagoBIConstants.CREATE_DOCUMENT, funcs)) {
			JSONObject createDoc = createMenuItem("pencil", HREF_PENCIL, messageBuilder.getMessage("menu.MyAnalysis", locale), true, null);
			tempMenuList.put(createDoc);
		}

		if (isAbleTo(SpagoBIConstants.SEE_MY_DATA, funcs)) {
			JSONObject myData = createMenuItem("my_data", HREF_MYDATA, messageBuilder.getMessage("menu.MyData", locale), true, null);
			tempMenuList.put(myData);
		}

		String strSbiSocialAnalysisStatus = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_IS_ACTIVE");
		boolean sbiSocialAnalysisStatus = "TRUE".equalsIgnoreCase(strSbiSocialAnalysisStatus);
		if (sbiSocialAnalysisStatus && (isAbleTo(SpagoBIConstants.CREATE_SOCIAL_ANALYSIS, funcs) || isAbleTo(SpagoBIConstants.VIEW_SOCIAL_ANALYSIS, funcs))) {
			JSONObject socialAnalysis = new JSONObject();
			socialAnalysis.put(ICON_CLS, "social_analysis");
			socialAnalysis.put(TOOLTIP, messageBuilder.getMessage("menu.SocialAnalysis", locale));
			socialAnalysis.put(ICON_ALIGN, "top");
			socialAnalysis.put(SCALE, "large");
			socialAnalysis.put(TARGET, "_self");
			// if (!GeneralUtilities.isSSOEnabled()) {
			socialAnalysis.put(HREF, "javascript:execDirectUrl('" + HREF_SOCIAL_ANALYSIS + "?" + SsoServiceInterface.USER_ID + "="
					+ userProfile.getUserUniqueIdentifier().toString() + "&" + SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&"
					+ SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry() + "');");
			/*
			 * } else { socialAnalysis.put(HREF, "javascript:execDirectUrl('" + HREF_SOCIAL_ANALYSIS + "?" + SpagoBIConstants.SBI_LANGUAGE + "=" +
			 * locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry() + "');"); }
			 */
			tempMenuList.put(socialAnalysis);
		}

		if (isAbleTo(SpagoBIConstants.HIERARCHIES_MANAGEMENT, funcs)) {
			JSONObject hierarchiesManagement = new JSONObject();
			hierarchiesManagement.put(ICON_CLS, "hierarchies_management");
			hierarchiesManagement.put(TOOLTIP, messageBuilder.getMessage("menu.HierarchiesManagement", locale));
			hierarchiesManagement.put(ICON_ALIGN, "top");
			hierarchiesManagement.put(SCALE, "large");
			hierarchiesManagement.put(TARGET, "_self");
			hierarchiesManagement.put(HREF, "javascript:execDirectUrl('" + contextName + HREF_HIERARCHIES_MANAGEMENT + "');");
			tempMenuList.put(hierarchiesManagement);
		}

		LowFunctionality personalFolder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode("USER_FUNCT", false);
		JSONObject myFolder = new JSONObject();
		if (personalFolder != null) {
			Integer persFoldId = personalFolder.getId();
			myFolder = createMenuItem("my_folder", "/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION&node=" + persFoldId,
					messageBuilder.getMessage("menu.MyFolder", locale), true, null);
			tempMenuList.put(myFolder);
		}

		return tempMenuList;
	}

	private JSONObject createMenuItem(String icon, String href, String tooltip, boolean idDirectLink, String label) throws JSONException {
		JSONObject menuItem = new JSONObject();
		menuItem.put(ICON_ALIGN, "top");
		menuItem.put(SCALE, "large");
		menuItem.put(TOOLTIP, "Info");
		menuItem.put(ICON_CLS, icon);
		menuItem.put(TOOLTIP, tooltip);
		menuItem.put(TARGET, "_self");
		if (label != null) {
			menuItem.put(LABEL, label);
		}
		if (idDirectLink) {
			menuItem.put(HREF, "javascript:javascript:execDirectUrl('" + contextName + href + "', '" + tooltip + "')");
		} else {
			if (label != null && label.equals(INFO)) {
				menuItem.put(HREF, "javascript:info()");
			} else if (label != null && label.equals(ROLE)) {
				menuItem.put(HREF, "javascript:roleSelection()");
			} else if (label != null && label.equals(HELP)) {
				menuItem.put(HREF, "http://wiki.spagobi.org/xwiki/bin/view/Main/");
				menuItem.remove(TARGET);
				menuItem.put(TARGET, "_blank");
			} else if (href != null && href.length() > 0) {
				menuItem.put(HREF, "javascript:execUrl('" + contextName + href + "')");
			}
		}
		menuItem.put(FIRST_URL, contextName + href);
		return menuItem;
	}

	private JSONArray createFixedMenu(Locale locale, int level, JSONArray tempMenuList) throws JSONException {

		MessageBuilder messageBuilder = new MessageBuilder();

		tempMenuList.put("->");
		JSONObject logo = new JSONObject();
		logo.put("xtype", "tbtext");
		logo.put("text", "<DIV style=\"WIDTH: 25px; HEIGHT: 100px; margin-left: 10px; background-image: url(" + contextName
				+ "/themes/sbi_default/img/logo_toolbar.png);\" align=\"center\"> &nbsp; </DIV>");
		tempMenuList.put(logo);

		JSONObject spacer = new JSONObject();
		JSONObject lang = createMenuItem("flag", "", messageBuilder.getMessage("menu.Languages", locale), false, "LANG");

		JSONObject roles = createMenuItem("roles", "", messageBuilder.getMessage("menu.RoleSelection", locale), false, "ROLE");

		JSONObject info = createMenuItem("info", "", messageBuilder.getMessage("menu.info", locale), false, "INFO");

		JSONObject help = createMenuItem("help", "", messageBuilder.getMessage("menu.help", locale), false, "HELP");

		spacer.put("xtype", "spacer");
		tempMenuList.put("->");

		tempMenuList.put(roles);

		tempMenuList.put(lang);

		tempMenuList.put(help);

		tempMenuList.put(info);

		if (userProfile.getUserUniqueIdentifier().toString().equalsIgnoreCase(SpagoBIConstants.PUBLIC_USER_ID)) {
			JSONObject login = createMenuItem("login", HREF_LOGIN, messageBuilder.getMessage("menu.login", locale), false, null);
			tempMenuList.put(login);
		} else {

			HttpSession session = this.getHttpSession();

			boolean showLogoutOnSilentLogin = Boolean.valueOf(SingletonConfig.getInstance().getConfigValue("SPAGOBI.HOME.SHOW_LOGOUT_ON_SILENT_LOGIN"));
			boolean silentLogin = Boolean.TRUE.equals(session.getAttribute(SsoServiceInterface.SILENT_LOGIN));
			// we show/don't show the logout button in case of a silent login,
			// according to configuration
			if (!silentLogin || showLogoutOnSilentLogin) {
				JSONObject power = createMenuItem("power", HREF_LOGOUT, messageBuilder.getMessage("menu.logout", locale), false, null);
				tempMenuList.put(power);
			}
		}

		return tempMenuList;
	}

	private Object getChildren(List children, int level, Locale locale) throws JSONException {
		JSONArray tempMenuList = new JSONArray();
		for (int j = 0; j < children.size(); j++) {
			Menu childElem = (Menu) children.get(j);
			tempMenuList = createUserMenuElement(childElem, locale, level, tempMenuList);
		}
		return tempMenuList;
	}

	private JSONArray createUserMenuElement(Menu childElem, Locale locale, int level, JSONArray tempMenuList) throws JSONException {
		JSONObject temp2 = new JSONObject();

		String path = MenuUtilities.getMenuPath(childElem, locale);

		MessageBuilder msgBuild = new MessageBuilder();
		String text = "";
		if (!childElem.isAdminsMenu() || !childElem.getName().startsWith("#"))
			text = msgBuild.getI18nMessage(locale, childElem.getName());
		else {
			if (childElem.getName().startsWith("#")) {
				String titleCode = childElem.getName().substring(1);
				text = msgBuild.getMessage(titleCode, locale);
			} else {
				text = childElem.getName();
			}
		}
		/*
		 * Cannot set a static ID as a random number!!!! See https://www.spagoworld.org/jira/browse/SPAGOBI-1268 See
		 * https://www.spagoworld.org/jira/browse/SPAGOBI-1269 The following line was the cause of the above issues!!
		 */
		// temp2.put(ID, new Double(Math.random()).toString());

		level++;
		if (childElem.getGroupingMenu() != null && childElem.getGroupingMenu().equals("true")) {
			temp2.put(TITLE, text);
			temp2.put(TITLE_ALIGN, "left");
			temp2.put(COLUMNS, 1);
			temp2.put(XTYPE, "buttongroup");
		} else {

			temp2.put(TEXT, text);
			temp2.put("style", "text-align: left;");
			temp2.put(SRC, childElem.getUrl());
			temp2.put(TARGET, "_self");
			temp2.put(ICON_CLS, "bullet");

			if (childElem.getObjId() != null) {
				temp2.put(HREF,
						"javascript:execDirectUrl('" + contextName + "/servlet/AdapterHTTP?ACTION_NAME=MENU_BEFORE_EXEC&MENU_ID=" + childElem.getMenuId()
								+ "', '" + path + "' )");
			} else if (childElem.getStaticPage() != null) {
				temp2.put(HREF, "javascript:execDirectUrl('" + contextName + "/servlet/AdapterHTTP?ACTION_NAME=READ_HTML_FILE&MENU_ID=" + childElem.getMenuId()
						+ "', '" + path + "' )");
			} else if (childElem.getFunctionality() != null) {
				String finalUrl = "javascript:execDirectUrl('" + DetailMenuModule.findFunctionalityUrl(childElem, contextName) + "', '" + path + "')";
				temp2.put(HREF, finalUrl);
			} else if (childElem.getExternalApplicationUrl() != null) {
				temp2.put(HREF, "javascript:callExternalApp('" + StringEscapeUtils.escapeJavaScript(childElem.getExternalApplicationUrl()) + "', '" + path
						+ "')");
			} else if (childElem.isAdminsMenu() && childElem.getUrl() != null) {
				String url = "javascript:execDirectUrl('" + childElem.getUrl() + "'";
				url = url.replace("${SPAGOBI_CONTEXT}", contextName);
				url = url.replace("${SPAGO_ADAPTER_HTTP}", GeneralUtilities.getSpagoAdapterHttpUrl());
				path = path.replace("#", "");

				// code to manage SpagoBISocialAnalysis link in admin menu
				if (url.contains("${SPAGOBI_SOCIAL_ANALYSIS_URL}")) {
					url = url.substring(0, url.length() - 1);
					url = url.replace("${SPAGOBI_SOCIAL_ANALYSIS_URL}", SingletonConfig.getInstance().getConfigValue("SPAGOBI.SOCIAL_ANALYSIS_URL"));
					// if (!GeneralUtilities.isSSOEnabled()) {
					url = url + "?" + SsoServiceInterface.USER_ID + "=" + userProfile.getUserUniqueIdentifier().toString() + "&"
							+ SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" + locale.getCountry() + "'";
					/*
					 * } else { url = url + "?" + SpagoBIConstants.SBI_LANGUAGE + "=" + locale.getLanguage() + "&" + SpagoBIConstants.SBI_COUNTRY + "=" +
					 * locale.getCountry() + "'"; }
					 */
				}

				temp2.put(HREF, url + ", '" + path + "')");
			}

		}
		if (childElem.getHasChildren()) {
			List childrenBis = childElem.getLstChildren();
			JSONArray tempMenuList2 = (JSONArray) getChildren(childrenBis, level, locale);
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
		boolean toReturn = false;
		for (int i = 0; i < funcs.size(); i++) {
			if (func.equals(funcs.get(i))) {
				toReturn = true;
				break;
			}
		}
		return toReturn;
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
