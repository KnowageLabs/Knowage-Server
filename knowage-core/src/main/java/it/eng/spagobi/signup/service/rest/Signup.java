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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.google.common.io.Resources;

import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.dto.MessageMailDto;
import it.eng.knowage.mailsender.dto.ProfileNameMailEnum;
import it.eng.knowage.mailsender.dto.TypeMailEnum;
import it.eng.knowage.mailsender.factory.FactoryMailSender;
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
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.signup.service.rest.dto.SignupDTO;
import it.eng.spagobi.signup.validation.SignupJWTTokenManager;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;

@Path("/signup")
public class Signup {

	private static final Logger LOGGER = Logger.getLogger(Signup.class);
	private static final String DEFAULT_PASSWORD = "Password";
	private static final String DEFAULT_TENANT = "DEFAULT_TENANT";

	@Context
	private HttpServletResponse servletResponse;

	@Context
	private HttpServletRequest request;

	@GET
	@Path("/prepareUpdate")
	public View prepareUpdate(@Context HttpServletRequest req) {
		LOGGER.debug("IN");
		try {
			UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId((String) profile.getUserId());
			Map<String, Object> data = profile.getUserAttributes();
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
			LOGGER.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
		}
		try {
			String currTheme = ThemesManager.getDefaultTheme();
			LOGGER.debug("currTheme: " + currTheme);

			String url = "/themes/" + currTheme + "/jsp/signup/modify.jsp";
			LOGGER.debug("url for modify: " + url);

			List<SbiCommunity> communities = DAOFactory.getCommunityDAO().loadAllSbiCommunities();
			req.setAttribute("communities", communities);

			String strActiveSignup = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
			boolean activeSignup = strActiveSignup.equalsIgnoreCase("true");
			req.setAttribute("activeSignup", activeSignup);

			return new View(url);
		} catch (Throwable t) {
			LOGGER.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
		}
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public String delete(@Context HttpServletRequest req) {
		LOGGER.debug("IN");
		try {
			UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId((String) profile.getUserId());

			userDao.deleteSbiUserById(user.getId());

		} catch (Throwable t) {
			LOGGER.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
		}
		LOGGER.debug("OUT");
		return new JSONObject().toString();

	}

	private void updAttribute(ISbiUserDAO userDao, ISbiAttributeDAO dao, String attributeValue, String userId, int id, SbiAttribute attribute)
			throws EMFUserError {
		LOGGER.debug("IN");
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
					userAttribute.getCommonInfo().setOrganization(DEFAULT_TENANT);
					userAttribute.getCommonInfo().setTimeIn(new Date(System.currentTimeMillis()));
					userAttribute.getCommonInfo().setUserIn(userId);
					userAttribute.getCommonInfo().setSbiVersionIn(SbiCommonInfo.getVersion());

					SbiUserAttributesId pk = new SbiUserAttributesId();
					if (attributeId != null) {
						//pk.setAttributeId(attributeId);
						pk = new SbiUserAttributesId(attributeId);
					}
					pk.changeId(id);
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
			LOGGER.error("Error while deleting user", err);
			throw new SpagoBIServiceException("Error while deleting user", err);
		}
		LOGGER.debug("OUT");
	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public String update(@Valid SignupDTO signupDTO) {
		LOGGER.debug("IN");

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(request);

		String name = signupDTO.getName() != null ? signupDTO.getName() : "";
		String surname = signupDTO.getSurname() != null ? signupDTO.getSurname() : "";
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
				LOGGER.error("Password is not valid", e);
				String message = msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale);
				if (e instanceof EMFUserError) {
					throw new SpagoBIServiceException(message, ((EMFUserError) e).getDescription());
				} else {
					throw new SpagoBIServiceException(message, e);
				}
			}

			int userId = user.getId();

			if(!name.isEmpty() && !surname.isEmpty()) {				
				user.setFullName(name + " " + surname);
			}
			
			if (password != null && !password.equals(DEFAULT_PASSWORD)) {
				user.setPassword(Password.hashPassword(password));
			}

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
			
			if(!name.isEmpty() && !surname.isEmpty()) {				
				profile.setAttributeValue("name", name);
				profile.setAttributeValue("surname", surname);
			}
			profile.setAttributeValue("email", email);

		} catch (Throwable t) {
			LOGGER.error("An unexpected error occurred while executing the subscribe action", t);
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", t);
		}
		LOGGER.debug("OUT");
		return new JSONObject().toString();
	}

	@GET
	@Path("/prepareActive")
	@PublicService
	public View prepareActive(@Context HttpServletRequest req) {
		String currTheme = ThemesManager.getDefaultTheme();
		LOGGER.debug("currTheme: " + currTheme);

		String url = "/themes/" + currTheme + "/jsp/signup/active.jsp";
		LOGGER.debug("url for active: " + url);
		req.setAttribute("currTheme", currTheme);
		return new View(url);
	}

	@GET
	@Path("/active")
	@PublicService
	public String active(@Context HttpServletRequest request) throws JSONException {
		LOGGER.debug("IN");

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

			LOGGER.debug("OUT");
			return new JSONObject("{message: '" + msgBuilder.getMessage("signup.msg.userActivationOK", "messages", locale) + "'}").toString();
		} catch (TokenExpiredException te) {
			LOGGER.error("Expired Token [" + token + "]", te);
			return new JSONObject("{errors: '" + msgBuilder.getMessage("signup.msg.userActiveKO", "messages", locale) + "',expired:true}").toString();
		} catch (Exception e) {
			LOGGER.error("Generic token validation error [" + token + "]", e);
			return new JSONObject("{errors: '" + msgBuilder.getMessage("signup.msg.userActiveKO", "messages", locale) + "'}").toString();
		}
	}

	private JSONObject buildErrorMessage(MessageBuilder msgBuilder, Locale locale, String errorString) {
		LOGGER.debug("IN");
		JSONObject errorMsg = new JSONObject();
		JSONArray errors = new JSONArray();
		try {
			errors.put(new JSONObject("{message: '" + msgBuilder.getMessage(errorString, "messages", locale) + "'}"));
			errorMsg.put("errors", errors);
			errorMsg.put("message", "validation-error");
		} catch (JSONException e) {
			throw new SpagoBIServiceException(msgBuilder.getMessage("signup.check.error", "messages", locale), e);
		}
		LOGGER.debug("OUT");
		return errorMsg;
	}

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@PublicService
	public Response create(@Valid SignupDTO signupDTO) {
		LOGGER.debug("IN");

		MessageBuilder msgBuilder = new MessageBuilder();
		Locale locale = msgBuilder.getLocale(request);

		String name = signupDTO.getName();

		String strActiveSignup = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
		boolean activeSignup = "true".equalsIgnoreCase(strActiveSignup);
		if (!activeSignup) {
			LOGGER.error(String.format("Attempt to register with signup not active for the user [%s]", name));
			throw new SpagoBIServiceException(this.request.getPathInfo(), msgBuilder.getMessage("signup.check.error", "messages", locale));
		}

		String surname = signupDTO.getSurname();
		String username = signupDTO.getUsername();
		if (username == null || username.equals("")) {
			LOGGER.error("Username is mandatory");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.usernameMandatory");
			return Response.ok(errObj.toString()).build();
		}

		String password = signupDTO.getPassword();
		String confirmPassword = signupDTO.getConfirmPassword();
		if (password == null || password.equals("") || confirmPassword == null || !password.equals(confirmPassword)) {
			LOGGER.error("Passwortd and confirm password are different");
			JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.pwdNotEqual");
			return Response.ok(errObj.toString()).build();
		}

		try {
			PasswordChecker.getInstance().isValid(password, password);
		} catch (Exception e) {
			LOGGER.error("Password is not valid", e);
			String message = msgBuilder.getMessage("signup.check.pwdInvalid", "messages", locale);
			if (e instanceof EMFUserError) {
				throw new SpagoBIServiceException(message, ((EMFUserError) e).getDescription());
			} else {
				throw new SpagoBIServiceException(message, e);
			}
		}

		String email = signupDTO.getEmail();
		if (email == null || email.equals("")) {
			LOGGER.error("email is mandatory");
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
		boolean useCaptcha = Boolean.parseBoolean(strUseCaptcha);

		try {
			ImageCaptcha c = (ImageCaptcha) request.getSession().getAttribute("simpleCaptcha");

			if (useCaptcha && captcha == null) {
				LOGGER.error("empty captcha");
				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.captchEmpty");
				return Response.ok(errObj.toString()).build();
			} else if (useCaptcha && !c.isCorrect(captcha)) {
				LOGGER.error("Invalid captcha");
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
					LOGGER.error("Username already in use");
					JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.userInUse");
					return Response.ok(errObj.toString()).build();
				}
			}

			SbiUser user = new SbiUser();
			user.setUserId(username);
			user.setPassword(Password.hashPassword(password));
			user.setFullName(name + " " + surname);
			user.getCommonInfo().setOrganization(DEFAULT_TENANT);
			user.getCommonInfo().setUserIn(username);
			user.setFlgPwdBlocked(true);

			String defaultTenant = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_TENANT_ON_SIGNUP");
			// if config is not defined, because it is a new configuration do not throw error and put a default value
			if (defaultTenant == null) {
				defaultTenant = "DEFAULT_TENANT";
			}

			Set<SbiExtRoles> roles = new HashSet<>();
			SbiExtRoles r = new SbiExtRoles();
			String defaultRole = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_ROLE_ON_SIGNUP");

			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			roleDAO.setTenant(defaultTenant);
			Role signupRole = roleDAO.loadByName(defaultRole);
			if (signupRole == null) {
				LOGGER.error("Invalid role " + defaultRole + " for the new user. "
						+ " Check the attibute SPAGOBI.SECURITY.DEFAULT_ROLE_ON_SIGNUP configuration and set a valid role name ! ");

				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "signup.check.invalidRole");
				return Response.ok(errObj.toString()).build();
			}

			r.changeExtRoleId(signupRole.getId());
			r.getCommonInfo().setOrganization(defaultTenant);
			roles.add(r);
			user.setSbiExtUserRoleses(roles);

			Set<SbiUserAttributes> attributes = new HashSet<>();

			ISbiAttributeDAO attrDao = DAOFactory.getSbiAttributeDAO();
			attrDao.setTenant(defaultTenant);
			addAttribute(attributes, attrDao.loadSbiAttributeByName("email").getAttributeId(), email);
			if (attrDao.loadSbiAttributeByName("gender") != null) {
				addAttribute(attributes, attrDao.loadSbiAttributeByName("gender").getAttributeId(), gender);
			}
			if (attrDao.loadSbiAttributeByName("birth_date") != null) {
				addAttribute(attributes, attrDao.loadSbiAttributeByName("birth_date").getAttributeId(), birthDate);
			}
			if (attrDao.loadSbiAttributeByName("location") != null) {
				addAttribute(attributes, attrDao.loadSbiAttributeByName("location").getAttributeId(), address);
			}
			if (attrDao.loadSbiAttributeByName("community") != null) {
				addAttribute(attributes, attrDao.loadSbiAttributeByName("community").getAttributeId(), enterprise);
			}
			if (attrDao.loadSbiAttributeByName("short_bio") != null) {
				addAttribute(attributes, attrDao.loadSbiAttributeByName("short_bio").getAttributeId(), biography);
			}
			if (attrDao.loadSbiAttributeByName("language") != null) {
				addAttribute(attributes, attrDao.loadSbiAttributeByName("language").getAttributeId(), language);
			}

			user.setSbiUserAttributeses(attributes);
			if (userRegistrationFromExpiredToken) {
				user.changeId(existingUserId);
			}

			int id = userDao.fullSaveOrUpdateSbiUser(user);

			LOGGER.debug("User [" + username + "] succesfuly created with id [" + id + "]");

			if (StringUtils.isNotEmpty(enterprise)) {
				LOGGER.debug("User [" + username + "] would be part of community [" + enterprise + "]");
				SbiCommunity community = DAOFactory.getCommunityDAO().loadSbiCommunityByName(enterprise);
				CommunityManager communityManager = new CommunityManager();
				communityManager.saveCommunity(community, enterprise, user.getUserId(), request);
			}

			String host = request.getServerName();
			LOGGER.debug("Activation url host is equal to [" + host + "]");
			int port = request.getServerPort();
			LOGGER.debug("Activation url port is equal to [" + port + "]");

			// Get confirmation mail template
			String mailText = Resources.toString(getClass().getResource("/templates/confirmationMailTemplate.html"), StandardCharsets.UTF_8);

			LOGGER.debug("Preparing activation mail for user [" + username + "]");
			String subject = msgBuilder.getMessage("signup.active.subject", "messages", locale);
			LOGGER.debug("Activation mail's subject set to [" + subject + "]");

			String token = SignupJWTTokenManager.createJWTToken(user.getUserId());
			String version = SbiCommonInfo.getVersion().substring(0, SbiCommonInfo.getVersion().lastIndexOf("."));

			String urlString = request.getContextPath() + "/restful-services/signup/prepareActive?token=" + token + "&locale=" + locale + "&version=" + version;
			URL url = new URL(request.getScheme(), host, port, urlString);

			// Replacing all placeholder occurencies in template with dynamic user values
			mailText = mailText.replace("%%WELCOME%%", msgBuilder.getMessage("signup.active.welcome", "messages", locale));
			mailText = mailText.replace("%%USERNAME%%", username);
			mailText = mailText.replace("%%THANKS_MESSAGE%%", msgBuilder.getMessage("signup.active.thanks", "messages", locale));
			mailText = mailText.replace("%%WELCOME_MESSAGE%%", msgBuilder.getMessage("signup.active.message", "messages", locale));
			mailText = mailText.replace("%%URL%%", url.toString());
			mailText = mailText.replace("%%URL_LABEL%%", msgBuilder.getMessage("signup.active.labelUrl", "messages", locale));
			mailText = mailText.replace("%%BOOKMARK%%", msgBuilder.getMessage("signup.active.bookmark", "messages", locale));
			mailText = mailText.replace("%%QA%%", msgBuilder.getMessage("signup.active.qa", "messages", locale));
			mailText = mailText.replace("%%GITHUB%%", msgBuilder.getMessage("signup.active.github", "messages", locale));
			mailText = mailText.replace("%%DOCUMENTATION%%", msgBuilder.getMessage("signup.active.documentation", "messages", locale));

			LOGGER.debug("Activation url is equal to [" + url.toExternalForm() + "]");
			LOGGER.debug("Activation mail for user [" + username + "] succesfully prepared");

			try {
				sendMail(email, subject, mailText);
			} catch (Exception e) {
				LOGGER.error("Cannot send email with user [" + email + "]", e);
				JSONObject errObj = buildErrorMessage(msgBuilder, locale, "sbi.execution.send.error");
				return Response.ok(errObj.toString()).build();
			}

			String okMsg = msgBuilder.getMessage("signup.ok.message", "messages", locale);

			// Captcha is burned and must be reloaded at client side
			request.getSession().removeAttribute("simpleCaptcha");

			LOGGER.debug("OUT");
			return Response.ok(new JSONObject().put("message", okMsg).toString()).build();

		} catch (Throwable t) {
			LOGGER.error("Error during user creation", t);
			throw new SpagoBIServiceException(msgBuilder.getMessage("signup.check.error", "messages", locale), t);
		}

	}

	private void addAttribute(Set<SbiUserAttributes> attributes, int attrId, String attrValue) {

		if (attrValue != null) {
			SbiUserAttributes a = new SbiUserAttributes();
			a.getCommonInfo().setOrganization(DEFAULT_TENANT);
			SbiUserAttributesId id = new SbiUserAttributesId(attrId);
			a.setId(id);
			a.setAttributeValue(attrValue);
			attributes.add(a);
		}
	}

	@POST
	@Path("/prepare")
	@PublicService
	public View prepare(@Context HttpServletRequest req) {
		String currTheme = ThemesManager.getDefaultTheme();
		LOGGER.debug("currTheme: " + currTheme);

		String url = "/themes/" + currTheme + "/jsp/signup/signup.jsp";
		LOGGER.debug("url for signup: " + url);

		// TODO : do we need to use the request locale?
		Locale locale = Locale.getDefault();
		LOGGER.debug("locale for signup: " + locale);
		try {
			List<SbiCommunity> communities = DAOFactory.getCommunityDAO().loadAllSbiCommunities();
			req.setAttribute("communities", communities);
			req.setAttribute("currTheme", currTheme);
			req.setAttribute("locale", locale);
			return new View(url);
		} catch (Exception e) {
			throw new SpagoBIServiceException("An unexpected error occurred while executing the subscribe action", e);
		}
	}

	private void sendMail(String emailAddress, String subject, String emailContent) throws Exception {

		MessageMailDto messageMailDto = new MessageMailDto();
		messageMailDto.setProfileName(ProfileNameMailEnum.USER);
		messageMailDto.setTypeMailEnum(TypeMailEnum.CONTENT);
		messageMailDto.setSubject(subject);
		messageMailDto.setText(emailContent);
		messageMailDto.setContentType("text/html");
		messageMailDto.setRecipients(new String[] { emailAddress });

		FactoryMailSender.getMailSender(SingletonConfig.getInstance().getConfigValue(IMailSender.MAIL_SENDER)).sendMail(messageMailDto);

	}

	private UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}
}
