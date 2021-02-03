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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hazelcast.core.IMap;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.validation.PasswordChecker;
import it.eng.spagobi.community.bo.CommunityManager;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bo.ProfileAttributesValueTypes;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.rest.publishers.PublisherService;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.signup.service.rest.dto.SignupDTO;
import it.eng.spagobi.signup.validation.SignupJWTTokenManager;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.services.ChangeTheme;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;

@Path("/signup")
public class Signup {

	@Context
	private HttpServletResponse servletResponse;

	private static final String defaultPassword = "Password";
	private static final String defaultTenant = "DEFAULT_TENANT";
	private static final String MAX_DELTA_CONFIG_NAME = "maxDeltaMsToken";
	private final static long MAX_TIME_DELTA_DEFAULT_MS = 30000;

	private static Logger logger = Logger.getLogger(PublisherService.class);

	@Context
	private HttpServletRequest request;

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
			logger.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
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

			String strActiveSignup = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
			boolean activeSignup = strActiveSignup.equalsIgnoreCase("true");
			req.setAttribute("activeSignup", activeSignup);

			req.getRequestDispatcher(url).forward(req, servletResponse);
			// req.getRequestDispatcher("/WEB-INF/jsp/signup/modify.jsp").forward(req, servletResponse);
		} catch (ServletException e) {
			logger.error("Error dispatching request");
		} catch (IOException e) {
			logger.error("Error writing content");
		} catch (Throwable t) {
			logger.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
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
			logger.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
		}
		logger.debug("OUT");
		return new JSONObject().toString();

	}

	private void updAttribute(ISbiUserDAO userDao, ISbiAttributeDAO dao, String attributeValue, String userId, int id, SbiAttribute attribute)
			throws EMFUserError {
		logger.debug("IN");
		try {
			Integer attributeId = null;
			SbiUserAttributes userAttribute = null;
			if (attribute != null) {
				attributeId = attribute.getAttributeId();
				userAttribute = dao.loadSbiAttributesByUserAndId(id, attributeId);
			}
			if (attributeValue != null) {
				if (userAttribute != null) {
					userAttribute.getCommonInfo().setTimeUp(new Date(System.currentTimeMillis()));
					userAttribute.getCommonInfo().setUserUp(userId);
					userAttribute.getCommonInfo().setSbiVersionUp(SbiCommonInfo.getVersion());
				} else {
					userAttribute = new SbiUserAttributes();
					userAttribute.getCommonInfo().setOrganization(defaultTenant);
					userAttribute.getCommonInfo().setTimeIn(new Date(System.currentTimeMillis()));
					userAttribute.getCommonInfo().setUserIn(userId);
					userAttribute.getCommonInfo().setSbiVersionIn(SbiCommonInfo.getVersion());

					SbiUserAttributesId pk = new SbiUserAttributesId();
					if (attributeId != null) {
						pk.setAttributeId(attributeId);
					}
					pk.setId(id);
					userAttribute.setId(pk);
				}
				userAttribute.setAttributeValue(attributeValue);
				userDao.updateSbiUserAttributes(userAttribute);
			} else {
				if (userAttribute != null) {
					userDao.deleteSbiUserAttributeById(id, attributeId);
				}
			}
		} catch (EMFUserError err) {
			logger.error("Error while deleting user", err);
			throw new SpagoBIServiceException("Error while deleting user", err);
		}
		logger.debug("OUT");
	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public String update(@Valid SignupDTO signupDTO) {
		logger.debug("IN");

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(request);

		String name = signupDTO.getName();
		String surname = signupDTO.getSurname();
		String password = signupDTO.getPassword();
		String email = signupDTO.getEmail();

		try {
			UserProfile profile = getUserProfile();
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			ISbiAttributeDAO attrDao = DAOFactory.getSbiAttributeDAO();
			attrDao.setUserProfile(profile);

			SbiUser user = userDao.loadSbiUserByUserId((String) profile.getUserId());
			try {
				PasswordChecker.getInstance().isValid(user, user.getPassword(), true, password, password);
			} catch (Exception e) {
				logger.error("Password is not valid", e);
				String message = msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale);
				if (e instanceof EMFUserError) {
					throw new SpagoBIServiceException(message, ((EMFUserError) e).getDescription());
				} else {
					throw new SpagoBIServiceException(message, e);
				}
			}

			int userId = user.getId();

			user.setFullName(name + " " + surname);
			if (password != null && !password.equals(defaultPassword))
				user.setPassword(Password.encriptPassword(password));

			userDao.updateSbiUser(user, userId);

			SbiAttribute currEmail = attrDao.loadSbiAttributeByName("email");
			/* email user attribute is mandatory */
			if (email != null && currEmail == null) {
				SbiAttribute emailSbiAttribute = new SbiAttribute();
				emailSbiAttribute.setAttributeName("email");
				emailSbiAttribute.setDescription("AUTO GENERATED email profile attribute");
				emailSbiAttribute.setValue(ProfileAttributesValueTypes.STRING);
				SbiCommonInfo sbiCommonInfo = new SbiCommonInfo();
				sbiCommonInfo.setOrganization(TenantManager.getTenant().getName());
				sbiCommonInfo.setUserIn("server");
				sbiCommonInfo.setTimeIn(new Date());
				sbiCommonInfo.setSbiVersionIn(SbiCommonInfo.getVersion());
				emailSbiAttribute.setCommonInfo(sbiCommonInfo);

				Integer newId = attrDao.saveSbiAttribute(emailSbiAttribute);
				currEmail = attrDao.loadSbiAttributeById(newId);
			}

			updAttribute(userDao, attrDao, email, user.getUserId(), userId, currEmail);

			profile.setAttributeValue("name", name);
			profile.setAttributeValue("surname", surname);
			profile.setAttributeValue("email", email);

		} catch (Throwable t) {
			logger.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
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

	@GET
	@Path("/active")
	@PublicService
	public String active(@Context HttpServletRequest request) throws JSONException {
		logger.debug("IN");

		String token = request.getParameter("token");
		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		String strLocale = GeneralUtilities.trim(request.getParameter("locale"));
		Locale locale = new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_") + 1));

		try {
			String userId = SignupJWTTokenManager.verifyJWTToken(token);

			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId(userId);

			if (user == null) {
				return new JSONObject("{message: '" + msgBuilder.getMessage("signup.msg.unknownUser", "messages", locale) + "'}").toString();
			}

			if (!user.getFlgPwdBlocked()) {
				String msg = msgBuilder.getMessage("signup.msg.userActiveKO", "messages", locale);
				return new JSONObject("{message: '" + msg + "'}").toString();
			}

			user.setFlgPwdBlocked(false);
			userDao.updateSbiUser(user, null);

			logger.debug("OUT");
			return new JSONObject("{message: '" + msgBuilder.getMessage("signup.msg.userActivationOK", "messages", locale) + "'}").toString();
		} catch (TokenExpiredException te) {
			logger.error("Expired Token [" + token + "]", te);
			return new JSONObject("{errors: '" + msgBuilder.getMessage("signup.msg.userActiveKO", "messages", locale) + "',expired:true}").toString();
		} catch (Exception e) {
			logger.error("Generic token validation error [" + token + "]", e);
			return new JSONObject("{errors: '" + msgBuilder.getMessage("signup.msg.userActiveKO", "messages", locale) + "'}").toString();
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
	public Response create(@Valid SignupDTO signupDTO) {
		logger.debug("IN");

		// String strLocale = GeneralUtilities.trim(req.getParameter("locale"));
		// Locale locale = new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_")+1));
		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(request);

		String name = signupDTO.getName();
		String surname = signupDTO.getSurname();
		String username = signupDTO.getUsername();
		if (username == null || username.equals("")) {
			logger.error("Username is mandatory");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.usernameMandatory");
			return Response.ok(errObj.toString()).build();
		}

		String password = signupDTO.getPassword();
		String confirmPassword = signupDTO.getConfirmPassword();
		if (password == null || password.equals("") || confirmPassword == null || !password.equals(confirmPassword)) {
			logger.error("Passwortd and confirm password are different");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.pwdNotEqual");
			return Response.ok(errObj.toString()).build();
		}

		try {
			PasswordChecker.getInstance().isValid(password, password);
		} catch (Exception e) {
			logger.error("Password is not valid", e);
			String message = msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale);
			if (e instanceof EMFUserError) {
				throw new SpagoBIServiceException(message, ((EMFUserError) e).getDescription());
			} else {
				throw new SpagoBIServiceException(message, e);
			}
		}

		String email = signupDTO.getEmail();
		if (email == null || email.equals("")) {
			logger.error("email is mandatory");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.emailMandatory");
			return Response.ok(errObj.toString()).build();
		}

		String gender = signupDTO.getGender();
		String birthDate = signupDTO.getBirthDate();
		String address = signupDTO.getAddress();
		String enterprise = signupDTO.getEnterprise();
		String biography = signupDTO.getBiography();
		String language = signupDTO.getLanguage();
		String captcha = signupDTO.getCaptcha();

		String strUseCaptcha = (signupDTO.getUseCaptcha() == null || signupDTO.getUseCaptcha().equals("")) ? "true" : signupDTO.getUseCaptcha();
		boolean useCaptcha = Boolean.valueOf(strUseCaptcha);

		try {
			ImageCaptcha c = (ImageCaptcha) request.getSession().getAttribute("simpleCaptcha");

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
			Integer existingUserId = userDao.isUserIdAlreadyInUse(username);
			boolean userRegistrationFromExpiredToken = false;
			/* Match with the username */
			if (existingUserId != null) {

				SbiUser sbiUser = userDao.loadSbiUserById(existingUserId);
				/* Check if sbiUser has */
				boolean matchingEmailAddress = false;
				if (sbiUser != null) {
					Set<SbiUserAttributes> userAttributes = sbiUser.getSbiUserAttributeses();
					for (SbiUserAttributes sbiUserAttributes : userAttributes) {
						if (sbiUserAttributes.getSbiAttribute().getAttributeName().equals("email") && sbiUserAttributes.getAttributeValue().equals(email)) {
							matchingEmailAddress = true;
							break;
						}
					}
				}

				/* Match with the email address and date from last access */
				userRegistrationFromExpiredToken = matchingEmailAddress && sbiUser.getDtLastAccess() == null;

				if (!userRegistrationFromExpiredToken) {
					logger.error("Username already in use");
					JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.userInUse");
					return Response.ok(errObj.toString()).build();
				}
			}

			SbiUser user = new SbiUser();
			user.setUserId(username);
			user.setPassword(Password.encriptPassword(password));
			user.setFullName(name + " " + surname);
			user.getCommonInfo().setOrganization(defaultTenant);
			user.getCommonInfo().setUserIn(username);
			user.setFlgPwdBlocked(true);

			String defaultTenant = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_TENANT_ON_SIGNUP");
			// if config is not defined, because it is a new configuration do not throw error and put a default value
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
			attrDao.setTenant(defaultTenant);
			addAttribute(attributes, attrDao.loadSbiAttributeByName("email").getAttributeId(), email);
			if (attrDao.loadSbiAttributeByName("gender") != null)
				addAttribute(attributes, attrDao.loadSbiAttributeByName("gender").getAttributeId(), gender);
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
			if (userRegistrationFromExpiredToken)
				user.setId(existingUserId);

			int id = userDao.fullSaveOrUpdateSbiUser(user);

			logger.debug("User [" + username + "] succesfuly created with id [" + id + "]");

			if (StringUtilities.isNotEmpty(enterprise)) {
				logger.debug("User [" + username + "] would be part of community [" + enterprise + "]");
				SbiCommunity community = DAOFactory.getCommunityDAO().loadSbiCommunityByName(enterprise);
				CommunityManager communityManager = new CommunityManager();
				communityManager.saveCommunity(community, enterprise, user.getUserId(), request);
			}

			String host = request.getServerName();
			logger.debug("Activation url host is equal to [" + host + "]");
			int port = request.getServerPort();
			logger.debug("Activation url port is equal to [" + port + "]");

			// Get confirmation mail template
			String mailText = Resources.toString(getClass().getResource("/templates/confirmationMailTemplate.html"), Charsets.UTF_8);

			logger.debug("Preparing activation mail for user [" + username + "]");
			String subject = msgBuilder.getMessage("signup.active.subject", "messages", locale);
			logger.debug("Activation mail's subject set to [" + subject + "]");

			String token = SignupJWTTokenManager.createJWTToken(user.getUserId());
			String version = SbiCommonInfo.getVersion().substring(0, SbiCommonInfo.getVersion().lastIndexOf("."));

			String urlString = request.getContextPath() + "/restful-services/signup/prepareActive?token=" + token + "&locale=" + locale + "&version=" + version;
			URL url = new URL(request.getScheme(), host, port, urlString);

			// Replacing all placeholder occurencies in template with dynamic user values
			mailText = mailText.replaceAll("%%WELCOME%%", msgBuilder.getMessage("signup.active.welcome", "messages", locale));
			mailText = mailText.replaceAll("%%USERNAME%%", username);
			mailText = mailText.replaceAll("%%THANKS_MESSAGE%%", msgBuilder.getMessage("signup.active.thanks", "messages", locale));
			mailText = mailText.replaceAll("%%WELCOME_MESSAGE%%", msgBuilder.getMessage("signup.active.message", "messages", locale));
			mailText = mailText.replaceAll("%%URL%%", url.toString());
			mailText = mailText.replaceAll("%%URL_LABEL%%", msgBuilder.getMessage("signup.active.labelUrl", "messages", locale));
			mailText = mailText.replaceAll("%%BOOKMARK%%", msgBuilder.getMessage("signup.active.bookmark", "messages", locale));
			mailText = mailText.replaceAll("%%QA%%", msgBuilder.getMessage("signup.active.qa", "messages", locale));
			mailText = mailText.replaceAll("%%GITHUB%%", msgBuilder.getMessage("signup.active.github", "messages", locale));
			mailText = mailText.replaceAll("%%DOCUMENTATION%%", msgBuilder.getMessage("signup.active.documentation", "messages", locale));

			logger.debug("Activation url is equal to [" + url.toExternalForm() + "]");
			logger.debug("Activation mail for user [" + username + "] succesfully prepared");

//			// put on hazelcast map uuid
//			IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
//					SpagoBIConstants.DISTRIBUTED_MAP_FOR_SIGNUP);
//
//			mapLocks.put(Integer.valueOf(id).toString(), uuid.toString());

			sendMail(email, subject, mailText);

			String okMsg = msgBuilder.getMessage("signup.ok.message", "messages", locale);

			// Captcha is burned and must be reloaded at client side
			request.getSession().removeAttribute("simpleCaptcha");

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
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", e);
		}
	}

	private void sendMail(String emailAddress, String subject, String emailContent) throws Exception {

		SessionFacade facade = MailSessionBuilder.newInstance().usingUserProfile().withTimeout(5000).withConnectionTimeout(5000).build();

		// create a message
		Message msg = facade.createNewMimeMessage();

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
		facade.sendMessage(msg);

	}

	private UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}
}
