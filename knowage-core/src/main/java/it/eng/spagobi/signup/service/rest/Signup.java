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
package it.eng.spagobi.signup.service.rest;

import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.hazelcast.core.IMap;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.community.bo.CommunityManager;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.rest.publishers.PublisherService;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.services.ChangeTheme;
import nl.captcha.Captcha;

@Path("/signup")
public class Signup {

	@Context
	private HttpServletResponse servletResponse;

	private static final String defaultPassword = "Password";
	private static final String defaultTenant = "DEFAULT_TENANT";
	private static final String MAX_DELTA_CONFIG_NAME = "maxDeltaMsToken";
	private final static long MAX_TIME_DELTA_DEFAULT_MS = 30000;

	private static Logger logger = Logger.getLogger(PublisherService.class);

	@GET
	@Path("/prepareUpdate")
	public void prepareUpdate(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId((String) profile.getUserId());
			Map<String, String> data = profile.getUserAttributes();
			if (user.getFullName() != null) {
				int i = user.getFullName().indexOf(" ");
				if (i >= 0) {
					data.put("name", user.getFullName().substring(0, i));
					data.put("surname", user.getFullName().substring(i + 1));
				} else {
					data.put("name", user.getFullName());
				}
			}
			data.put("username", user.getUserId());
			data.put("userIn", user.getCommonInfo().getUserIn());

			req.setAttribute("data", data);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", t);
		}
		try {
			String theme_name = (String) req.getAttribute(ChangeTheme.THEME_NAME);
			logger.debug("theme selected: " + theme_name);

			String currTheme = (String) req.getAttribute("currTheme");
			if (currTheme == null)
				currTheme = ThemesManager.getDefaultTheme();
			logger.debug("currTheme: " + currTheme);

			String url = "/themes/" + currTheme + "/jsp/signup/modify.jsp";
			logger.debug("url for modify: " + url);
			List communities = DAOFactory.getCommunityDAO().loadAllSbiCommunities();
			req.setAttribute("communities", communities);
			req.getRequestDispatcher(url).forward(req, servletResponse);
			// req.getRequestDispatcher("/WEB-INF/jsp/signup/modify.jsp").forward(req, servletResponse);
		} catch (ServletException e) {
			logger.error("Error dispatching request");
		} catch (IOException e) {
			logger.error("Error writing content");
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", t);
		}
		logger.debug("OUT");
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public String delete(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId((String) profile.getUserId());

			// CommunityManager cm = new CommunityManager();
			// cm.mngUserCommunityAfterDelete(user);
			// logger.debug("User-community membership deleted");

			userDao.deleteSbiUserById(user.getId());

			// String host = req.getServerName();
			// logger.debug("Activation url host is equal to [" + host + "]");
			// int port = req.getServerPort();
			// logger.debug("Activation url port is equal to [" + port + "]");

			// URL url = new URL(req.getScheme(), host, port,
			// req.getContextPath() + "/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE" );
			//
			// servletResponse.sendRedirect(url.toString());

		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", t);
		}
		logger.debug("OUT");
		return new JSONObject().toString();

	}

	private void updAttribute(ISbiUserDAO userDao, ISbiAttributeDAO dao, String attributeValue, String userId, int id, int attributeId) throws EMFUserError {
		logger.debug("IN");
		if (attributeValue != null) {
			SbiUserAttributes userAttribute = dao.loadSbiAttributesByUserAndId(id, attributeId);
			if (userAttribute != null) {
				userAttribute.getCommonInfo().setTimeUp(new Date(System.currentTimeMillis()));
				userAttribute.getCommonInfo().setUserUp(userId);
				userAttribute.getCommonInfo().setSbiVersionUp(SbiCommonInfo.SBI_VERSION);
			} else {
				userAttribute = new SbiUserAttributes();
				userAttribute.getCommonInfo().setOrganization(defaultTenant);
				userAttribute.getCommonInfo().setTimeIn(new Date(System.currentTimeMillis()));
				userAttribute.getCommonInfo().setUserIn(userId);
				userAttribute.getCommonInfo().setSbiVersionIn(SbiCommonInfo.SBI_VERSION);

				SbiUserAttributesId pk = new SbiUserAttributesId();
				pk.setAttributeId(attributeId);
				pk.setId(id);
				userAttribute.setId(pk);
			}
			userAttribute.setAttributeValue(attributeValue);
			userDao.updateSbiUserAttributes(userAttribute);
		} else {
			try {
				if (dao.loadSbiAttributesByUserAndId(id, attributeId) != null) {
					userDao.deleteSbiUserAttributeById(id, attributeId);
				}
			} catch (EMFUserError err) {
				logger.error("Error while deleting user", err);
				throw new SpagoBIServiceException("Error while deleting user", err);
			}
		}
		logger.debug("OUT");
	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public String update(@Context HttpServletRequest req) {
		logger.debug("IN");

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(req);

		JSONObject requestJSON = null;
		try {
			requestJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Throwable t) {
			logger.error("Error during body read", t);
			throw new SpagoBIServiceException(msgBuilder.getMessage("signup.check.error", "messages", locale), t);
		}
		String name = GeneralUtilities.trim(requestJSON.optString("name"));
		String surname = GeneralUtilities.trim(requestJSON.optString("surname"));
		String password = GeneralUtilities.trim(requestJSON.optString("password"));
		String email = GeneralUtilities.trim(requestJSON.optString("email"));
		String birthDate = GeneralUtilities.trim(requestJSON.optString("birthDate"));
		String address = GeneralUtilities.trim(requestJSON.optString("address"));
		// String biography = GeneralUtilities.trim(requestJSON.optString("biography"));
		// String language = GeneralUtilities.trim(requestJSON.optString("language"));

		try {

			UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			ISbiAttributeDAO attrDao = DAOFactory.getSbiAttributeDAO();

			SbiUser user = userDao.loadSbiUserByUserId((String) profile.getUserId());
			int userId = user.getId();

			user.setFullName(name + " " + surname);
			if (password != null && !password.equals(defaultPassword))
				user.setPassword(Password.encriptPassword(password));
			userDao.updateSbiUser(user, userId);

			updAttribute(userDao, attrDao, email, user.getUserId(), userId, attrDao.loadSbiAttributeByName("email").getAttributeId());
			updAttribute(userDao, attrDao, birthDate, user.getUserId(), userId, attrDao.loadSbiAttributeByName("birth_date").getAttributeId());
			updAttribute(userDao, attrDao, address, user.getUserId(), userId, attrDao.loadSbiAttributeByName("address").getAttributeId());
			// updAttribute(userDao, attrDao, biography, user.getUserId(), userId, attrDao.loadSbiAttributeByName("short_bio").getAttributeId());
			// updAttribute(userDao, attrDao, language, user.getUserId(), userId, attrDao.loadSbiAttributeByName("language").getAttributeId());

			profile.setAttributeValue("name", name);
			profile.setAttributeValue("surname", surname);
			profile.setAttributeValue("birth_date", birthDate);
			profile.setAttributeValue("email", email);
			// profile.setAttributeValue("language", language);
			// profile.setAttributeValue("short_bio", biography);
			profile.setAttributeValue("location", address);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", t);
		}
		logger.debug("OUT");
		return new JSONObject().toString();
	}

	private String validateRequest(String accountId, String uuid, IMap mapLocks) {
		logger.debug("IN");

		// verify request authenticity
		if (uuid == null) {
			logger.error("missing uuid key, request not valid, user cannot be activated");
			return "signup.msg.invalidRequestKO";
		}

		Object obj = mapLocks.get(accountId);
		String UUIDinMap = null;
		try {

			UUIDinMap = (String) obj;

			if (!UUIDinMap.equals(uuid)) {
				logger.error("Request seems not be valid, uuid check failed");
				return "signup.msg.invalidRequestKO";
			}
		} catch (Exception e) {
			logger.error("Could not verify uuid or timeout passed, user cannot be activated");
			return "signup.msg.invalidRequestKO";
		}

		logger.debug("request is valid ");
		logger.debug("OUT");
		return null;
	}

	@GET
	@Path("/prepareActive")
	@PublicService
	public void prepareActive(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {

			String theme_name = (String) req.getAttribute(ChangeTheme.THEME_NAME);
			logger.debug("theme selected: " + theme_name);

			String currTheme = (String) req.getAttribute("currTheme");
			if (currTheme == null)
				currTheme = ThemesManager.getDefaultTheme();
			logger.debug("currTheme: " + currTheme);

			String url = "/themes/" + currTheme + "/jsp/signup/active.jsp";
			logger.debug("url for active: " + url);
			req.setAttribute("currTheme", currTheme);
			req.getRequestDispatcher(url).forward(req, servletResponse);
		} catch (ServletException e) {
			logger.error("Error dispatching request");
		} catch (IOException e) {
			logger.error("Error writing content");
		}

		logger.debug("OUT");
	}

	@POST
	@Path("/active")
	@PublicService
	public String active(@Context HttpServletRequest req) {
		logger.debug("IN");

		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		String id = req.getParameter("accountId");
		String strLocale = GeneralUtilities.trim(req.getParameter("locale"));
		Locale locale = new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_") + 1));
		String expired_time = SingletonConfig.getInstance().getConfigValue("MAIL.SIGNUP.expired_time");

		try {
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = null;
			try {
				user = userDao.loadSbiUserById(Integer.parseInt(id));
			} catch (EMFUserError emferr) {
			}
			if (user == null) {
				return new JSONObject("{message: '" + msgBuilder.getMessage("signup.msg.unknownUser", "messages", locale) + "'}").toString();

			}

			if (!user.getFlgPwdBlocked()) {
				String msg = msgBuilder.getMessage("signup.msg.userActiveKO", "messages", locale);
				return new JSONObject("{message: '" + msg + "'}").toString();
			}

			long now = System.currentTimeMillis();
			if (now > user.getCommonInfo().getTimeIn().getTime() + Long.parseLong(expired_time) * 24 * 60 * 60 * 1000)
				return new JSONObject("{message: '" + msgBuilder.getMessage("signup.msg.userActivationExpired", "messages", locale) + "'}").toString();

			user.setFlgPwdBlocked(false);
			userDao.updateSbiUser(user, null);

			logger.debug("OUT");
			return new JSONObject("{message: '" + msgBuilder.getMessage("signup.msg.userActivationOK", "messages", locale) + "'}").toString();
		} catch (Throwable t) {
			logger.error("An unexpected error occured while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", t);
		}
	}

	private JSONObject buildErrorMessage(MessageBuilder msgBuilder, Locale locale, String errorString) {
		logger.debug("IN");
		JSONObject errorMsg = new JSONObject();
		JSONArray errors = new JSONArray();
		try {
			errors.put(new JSONObject("{message: '" + msgBuilder.getMessage(errorString, "messages", locale) + "'}"));
			errorMsg.put("errors", errors);
			errorMsg.put("message", "validation-error");
		} catch (JSONException e) {
			throw new SpagoBIServiceException(msgBuilder.getMessage("signup.check.error", "messages", locale), e);
		}
		logger.debug("OUT");
		return errorMsg;
	}

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@PublicService
	public Response create(@Context HttpServletRequest req) {
		logger.debug("IN");

		// String strLocale = GeneralUtilities.trim(req.getParameter("locale"));
		// Locale locale = new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_")+1));
		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(req);

		JSONObject requestJSON = null;
		try {
			requestJSON = RestUtilities.readBodyAsJSONObject(req);
		} catch (Throwable t) {
			logger.error("Error during body read", t);
			throw new SpagoBIServiceException(msgBuilder.getMessage("signup.check.error", "messages", locale), t);
		}

		String name = GeneralUtilities.trim(requestJSON.optString("name"));
		String surname = GeneralUtilities.trim(requestJSON.optString("surname"));
		String username = GeneralUtilities.trim(requestJSON.optString("username"));
		if (username == null || username.equals("")) {
			logger.error("Username is mandatory");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.usernameMandatory");
			return Response.ok(errObj.toString()).build();
		}

		String password = GeneralUtilities.trim(requestJSON.optString("password"));
		String confirmPassword = GeneralUtilities.trim(requestJSON.optString("confirmPassword"));
		if (password == null || password.equals("") || confirmPassword == null || !password.equals(confirmPassword)) {
			logger.error("Passwortd and confirm password are different");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.pwdNotEqual");
			return Response.ok(errObj.toString()).build();
		}

		String email = GeneralUtilities.trim(requestJSON.optString("email"));
		if (email == null || email.equals("")) {
			logger.error("email is mandatory");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.emailMandatory");
			return Response.ok(errObj.toString()).build();
		}

		String sex = GeneralUtilities.trim(requestJSON.optString("sex"));
		String birthDate = GeneralUtilities.trim(requestJSON.optString("birthDate"));
		String address = GeneralUtilities.trim(requestJSON.optString("address"));
		String enterprise = GeneralUtilities.trim(requestJSON.optString("enterprise"));
		String biography = GeneralUtilities.trim(requestJSON.optString("biography"));
		String language = GeneralUtilities.trim(requestJSON.optString("language"));
		String captcha = GeneralUtilities.trim(requestJSON.optString("captcha"));

		String strUseCaptcha = (requestJSON.optString("useCaptcha") == null || requestJSON.optString("useCaptcha").equals("")) ? "true"
				: requestJSON.optString("useCaptcha");
		boolean useCaptcha = Boolean.valueOf(strUseCaptcha);

		try {
			Captcha c = (Captcha) req.getSession().getAttribute(Captcha.NAME);

			if (useCaptcha && captcha == null) {
				logger.error("empty captcha");
				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.captchEmpty");
				return Response.ok(errObj.toString()).build();
			} else if (useCaptcha && !c.isCorrect(captcha)) {
				logger.error("Invalid captcha");
				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.captchWrong");
				return Response.ok(errObj.toString()).build();
			}

			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			if (userDao.isUserIdAlreadyInUse(username) != null) {
				logger.error("Username already in use");
				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.userInUse");
				return Response.ok(errObj.toString()).build();
			}

			SbiUser user = new SbiUser();
			user.setUserId(username);
			user.setPassword(Password.encriptPassword(password));
			user.setFullName(name + " " + surname);
			user.getCommonInfo().setOrganization(defaultTenant);
			user.getCommonInfo().setUserIn(username);
			user.setFlgPwdBlocked(true);

			String defaultTenant = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_TENANT_ON_SIGNUP");
			// if config is not defined, because it is a new configurationm do not therow error and put a default value
			if (defaultTenant == null) {
				defaultTenant = "DEFAULT_TENANT";
			}

			Set<SbiExtRoles> roles = new HashSet<SbiExtRoles>();
			SbiExtRoles r = new SbiExtRoles();
			String defaultRole = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_ROLE_ON_SIGNUP");

			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			roleDAO.setTenant(defaultTenant);
			Role signupRole = roleDAO.loadByName(defaultRole);
			if (signupRole == null) {
				logger.error("Invalid role " + defaultRole + " for the new user. "
						+ " Check the attibute SPAGOBI.SECURITY.DEFAULT_ROLE_ON_SIGNUP configuration and set a valid role name ! ");

				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.invalidRole");
				return Response.ok(errObj.toString()).build();
			}

			r.setExtRoleId(signupRole.getId());
			r.getCommonInfo().setOrganization(defaultTenant);
			roles.add(r);
			user.setSbiExtUserRoleses(roles);

			Set<SbiUserAttributes> attributes = new HashSet<SbiUserAttributes>();

			ISbiAttributeDAO attrDao = DAOFactory.getSbiAttributeDAO();

			addAttribute(attributes, attrDao.loadSbiAttributeByName("email").getAttributeId(), email);
			if (attrDao.loadSbiAttributeByName("gender") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("gender").getAttributeId(), sex);
			if (attrDao.loadSbiAttributeByName("birth_date") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("birth_date").getAttributeId(), birthDate);
			if (attrDao.loadSbiAttributeByName("location") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("location").getAttributeId(), address);
			if (attrDao.loadSbiAttributeByName("community") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("community").getAttributeId(), enterprise);
			if (attrDao.loadSbiAttributeByName("short_bio") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("short_bio").getAttributeId(), biography);
			if (attrDao.loadSbiAttributeByName("language") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("language").getAttributeId(), language);

			user.setSbiUserAttributeses(attributes);
			int id = userDao.fullSaveOrUpdateSbiUser(user);

			logger.debug("User [" + username + "] succesfuly created with id [" + id + "]");

			if (StringUtilities.isNotEmpty(enterprise)) {
				logger.debug("User [" + username + "] would be part of community [" + enterprise + "]");
				SbiCommunity community = DAOFactory.getCommunityDAO().loadSbiCommunityByName(enterprise);
				CommunityManager communityManager = new CommunityManager();
				communityManager.saveCommunity(community, enterprise, user.getUserId(), req);
			}
			StringBuffer sb = new StringBuffer();
			sb.append("<HTML>");
			sb.append("	<HEAD>");
			sb.append("		<TITLE>Activation user</TITLE>");
			sb.append("	</HEAD>");
			sb.append("	<BODY>");

			logger.debug("Preparing activation mail for user [" + username + "]");
			// String subject = SingletonConfig.getInstance().getConfigValue("MAIL.SIGNUP.subject");
			String subject = msgBuilder.getMessage("signup.active.msg.1", "messages", locale);
			logger.debug("Activation mail's subject set to [" + subject + "]");
			// String body = SingletonConfig.getInstance().getConfigValue("MAIL.SIGNUP.body");
			String body = msgBuilder.getMessage("signup.active.msg.2", "messages", locale) + " ";
			logger.debug("Activation mail's body set to [" + body + "]");

			String host = req.getServerName();
			logger.debug("Activation url host is equal to [" + host + "]");
			int port = req.getServerPort();
			logger.debug("Activation url port is equal to [" + port + "]");

			// get uuid to authenticat request
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuid = uuidGen.generateRandomBasedUUID();

			String urlString = req.getContextPath() + "/restful-services/signup/prepareActive?accountId=" + id + "&locale=" + locale + "&uuid=" + uuid;

			URL url = new URL(req.getScheme(), host, port, urlString);

			logger.debug("Activation url is equal to [" + url.toExternalForm() + "]");
			body += " <a href=\"" + url.toString() + "\">" + msgBuilder.getMessage("signup.active.labelUrl", "messages", locale) + "</a>";
			sb.append(body);
			logger.debug("Activation mail for user [" + username + "] succesfully prepared");

			sb.append("	</BODY>");
			sb.append("</HTML>");
			String mailTxt = sb.toString();

			// put on hazelcast map uuid
			IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_SIGNUP);

			mapLocks.put(Integer.valueOf(id).toString(), uuid.toString());

			sendMail(email, subject, mailTxt);

			String okMsg = msgBuilder.getMessage("signup.ok.message", "messages", locale);
			logger.debug("OUT");
			return Response.ok(new JSONObject().put("message", okMsg).toString()).build();

		} catch (Throwable t) {
			logger.error("Error during user creation", t);
			throw new SpagoBIServiceException(msgBuilder.getMessage("signup.check.error", "messages", locale), t);
		}

	}

	private void addAttribute(Set<SbiUserAttributes> attributes, int attrId, String attrValue) {

		if (attrValue != null) {
			SbiUserAttributes a = new SbiUserAttributes();
			a.getCommonInfo().setOrganization(defaultTenant);
			SbiUserAttributesId id = new SbiUserAttributesId();
			id.setAttributeId(attrId);
			a.setId(id);
			a.setAttributeValue(attrValue);
			attributes.add(a);
		}
	}

	@POST
	@Path("/prepare")
	@PublicService
	public void prepare(@Context HttpServletRequest req) {
		String theme_name = (String) req.getAttribute(ChangeTheme.THEME_NAME);
		logger.debug("theme selected: " + theme_name);

		String currTheme = (String) req.getAttribute("currTheme");
		if (currTheme == null)
			currTheme = ThemesManager.getDefaultTheme();
		logger.debug("currTheme: " + currTheme);

		String url = "/themes/" + currTheme + "/jsp/signup/signup.jsp";
		logger.debug("url for signup: " + url);

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(req);
		logger.debug("locale for signup: " + locale);
		try {
			List communities = DAOFactory.getCommunityDAO().loadAllSbiCommunities();
			req.setAttribute("communities", communities);
			req.setAttribute("currTheme", currTheme);
			req.setAttribute("locale", locale);
			req.getRequestDispatcher(url).forward(req, servletResponse);
			// req.getRequestDispatcher("/WEB-INF/jsp/signup/signup.jsp").forward(req, servletResponse);
		} catch (ServletException e) {
			logger.error("Error dispatching request");
		} catch (IOException e) {
			logger.error("Error writing content");
		} catch (Exception e) {
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", e);
		}
	}

	private void sendMail(String emailAddress, String subject, String emailContent) throws Exception {

		final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

		String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.smtphost");
		String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.smtpport");
		String smtpssl = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.useSSL");
		logger.debug(smtphost + " " + smtpport + " use SSL: " + smtpssl);

		// Custom Trusted Store Certificate Options
		String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file");
		String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password");

		int smptPort = 25;

		if ((smtphost == null) || smtphost.trim().equals(""))
			throw new Exception("Smtp host not configured");
		if ((smtpport == null) || smtpport.trim().equals("")) {
			throw new Exception("Smtp host not configured");
		} else {
			smptPort = Integer.parseInt(smtpport);
		}

		String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.from");
		if ((from == null) || from.trim().equals(""))
			from = "spagobi@eng.it";
		String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.user");
		if ((user == null) || user.trim().equals("")) {
			logger.debug("Smtp user not configured");
			user = null;
		}
		String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.password");
		if ((pass == null) || pass.trim().equals("")) {
			logger.debug("Smtp password not configured");
		}

		// Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", Integer.toString(smptPort));
		// Set timeout limit for mail server to respond
		props.put("mail.smtp.timeout", "5000");
		props.put("mail.smtp.connectiontimeout", "5000");

		// open session
		Session session = null;
		// create autheticator object
		Authenticator auth = null;
		if (user != null) {
			auth = new SMTPAuthenticator(user, pass);
			props.put("mail.smtp.auth", "true");
			// SSL Connection
			if (smtpssl.equals("true")) {
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
				props.put("mail.smtps.auth", "true");
				props.put("mail.smtps.socketFactory.port", Integer.toString(smptPort));
				if ((!StringUtilities.isEmpty(trustedStorePath))) {
					/*
					 * Dynamic configuration of trustedstore for CA Using Custom SSLSocketFactory to inject certificates directly from specified files
					 */

					props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

				} else {

					props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
				}
				props.put("mail.smtp.socketFactory.fallback", "false");
			}

			session = Session.getInstance(props, auth);
			logger.info("Session.getInstance(props, auth)");

		} else {
			session = Session.getInstance(props);
			logger.info("Session.getInstance(props)");
		}

		// create a message
		Message msg = new MimeMessage(session);
		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);
		InternetAddress addressTo = new InternetAddress(emailAddress);

		msg.setRecipient(Message.RecipientType.TO, addressTo);

		// Setting the Subject and Content Type
		msg.setSubject(subject);
		// create and fill the first message part
		// MimeBodyPart mbp1 = new MimeBodyPart();
		// mbp1.setText(emailContent);
		// // create the Multipart and add its parts to it
		// Multipart mp = new MimeMultipart();
		// mp.addBodyPart(mbp1);
		// // add the Multipart to the message
		// msg.setContent(mp);
		msg.setContent(emailContent, "text/html");
		// send message
		if ((smtpssl.equals("true")) && (!StringUtilities.isEmpty(user)) && (!StringUtilities.isEmpty(pass))) {
			// USE SSL Transport comunication with SMTPS
			Transport transport = session.getTransport("smtps");
			transport.connect(smtphost, smptPort, user, pass);
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
		} else {
			// Use normal SMTP
			Transport.send(msg);
		}

	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {
		private String username = "";
		private String password = "";

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}

		public SMTPAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
	}

}
