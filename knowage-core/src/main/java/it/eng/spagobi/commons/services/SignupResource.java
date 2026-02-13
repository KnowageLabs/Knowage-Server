package it.eng.spagobi.commons.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.google.common.io.Resources;

import it.eng.knowage.commons.security.KnowageSystemConfiguration;
import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.dto.MessageMailDto;
import it.eng.knowage.mailsender.dto.ProfileNameMailEnum;
import it.eng.knowage.mailsender.dto.TypeMailEnum;
import it.eng.knowage.mailsender.factory.FactoryMailSender;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.validation.PasswordChecker;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.signup.validation.SignupJWTTokenManager;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.filter.FishEyeImageFilter;

@Path("2.0/signup")
public class SignupResource {

	private static final Logger LOGGER = Logger.getLogger(SignupResource.class);

	@GET
	@Path("/captcha")
	@PublicService
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCaptcha() {
		try {
			int width = 200;
			int height = 75;

			ImageCaptcha imageCaptcha = new ImageCaptcha.Builder(width, height).addContent().addBackground().addFilter(new FishEyeImageFilter()).build();

			BufferedImage image = imageCaptcha.getImage();
			String content = imageCaptcha.getContent();

			String base64Image;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(image, "png", baos);
				base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
			}

			Map<String, String> response = new HashMap<>();
			response.put("image", base64Image);
			response.put("content", Password.hashPassword(content));

			return Response.ok(response).build();

		} catch (Exception e) {
			LOGGER.error("Error while generating captcha", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Error while generating captcha")).build();
		}
	}

	@POST
	@Path("/create")
	@PublicService
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@Context HttpServletRequest req, Map<String, String> payload) {
		LOGGER.debug("IN");

		try {
			// Check if signup is enabled
			String strActiveSignup = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
			boolean activeSignup = "true".equalsIgnoreCase(strActiveSignup);
			if (!activeSignup) {
				LOGGER.error("Attempt to register with signup not active");
				return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "Signup is not enabled")).build();
			}

			// Extract parameters
			String username = payload.get("username");
			String password = payload.get("password");
			String confirmPassword = payload.get("confirmPassword");
			String name = payload.get("name");
			String surname = payload.get("surname");
			String email = payload.get("email");
			String captcha = payload.get("captcha");
			String content = payload.get("content");

			// Validate inputs
			Response validationError = validateSignupInput(username, password, confirmPassword, email, captcha, content);
			if (validationError != null) {
				return validationError;
			}

			// Get default tenant
			String defaultTenant = getDefaultTenant();

			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			Integer existingUserId = userDao.isUserIdAlreadyInUse(username);
			boolean userRegistrationFromExpiredToken = false;

			// Check if user already exists
			if (existingUserId != null) {
				SbiUser sbiUser = userDao.loadSbiUserById(existingUserId);
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

				userRegistrationFromExpiredToken = matchingEmailAddress && sbiUser.getDtLastAccess() == null;

				if (!userRegistrationFromExpiredToken) {
					LOGGER.error("Username already in use");
					return Response.status(Response.Status.CONFLICT).entity(Map.of("error", "Username already in use")).build();
				}
			}

			// Create user
			SbiUser user = new SbiUser();
			user.setUserId(username);
			user.setPassword(Password.hashPassword(password));
			user.setFullName(name + " " + surname);
			user.getCommonInfo().setOrganization(defaultTenant);
			user.getCommonInfo().setUserIn(username);
			user.setFlgPwdBlocked(true);

			// Set tenant and role
			Set<SbiExtRoles> roles = new HashSet<>();
			SbiExtRoles r = new SbiExtRoles();
			String defaultRole = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_ROLE_ON_SIGNUP");

			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			roleDAO.setTenant(defaultTenant);
			Role signupRole = roleDAO.loadByName(defaultRole);

			if (signupRole == null) {
				LOGGER.error("Invalid role " + defaultRole + " for the new user");
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Invalid default role configuration")).build();
			}

			r.changeExtRoleId(signupRole.getId());
			r.getCommonInfo().setOrganization(defaultTenant);
			roles.add(r);
			user.setSbiExtUserRoleses(roles);

			// Set user attributes
			Set<SbiUserAttributes> attributes = new HashSet<>();
			ISbiAttributeDAO attrDao = DAOFactory.getSbiAttributeDAO();
			attrDao.setTenant(defaultTenant);

			addAttribute(attributes, attrDao.loadSbiAttributeByName("email").getAttributeId(), email, defaultTenant);

			user.setSbiUserAttributeses(attributes);
			if (userRegistrationFromExpiredToken) {
				user.changeId(existingUserId);
			}

			int id = userDao.fullSaveOrUpdateSbiUser(user);
			LOGGER.debug("User [" + username + "] successfully created with id [" + id + "]");

			// Send activation email
			try {
				sendActivationEmail(req, user, username);
			} catch (Exception e) {
				LOGGER.error("Cannot send activation email to [" + email + "]", e);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Error while sending activation email")).build();
			}

			LOGGER.debug("OUT");
			return Response.ok(Map.of("message", "User registered successfully. Please check your email for activation")).build();

		} catch (Exception e) {
			LOGGER.error("Error during user creation", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "An error occurred during registration")).build();
		}
	}

	@POST
	@Path("/active")
	@PublicService
	@Produces(MediaType.APPLICATION_JSON)
	public Response active(@Context HttpServletRequest req, Map<String, String> payload) {
		LOGGER.debug("IN");
		String token = payload.get("token");

		if (StringUtils.isBlank(token)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Token is mandatory")).build();
		}

		try {
			String userId = SignupJWTTokenManager.verifyJWTToken(token);

			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId(userId);

			if (user == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "User not found")).build();
			}

			if (!user.getFlgPwdBlocked()) {
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "User already active")).build();
			}

			user.setFlgPwdBlocked(false);
			userDao.updateSbiUser(user, null);

			LOGGER.debug("OUT");
			return Response.ok(Map.of("message", "User activated successfully")).build();

		} catch (TokenExpiredException te) {
			LOGGER.error("Expired Token [" + token + "]", te);
			return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Token expired", "expired", true)).build();
		} catch (Exception e) {
			LOGGER.error("Generic token validation error [" + token + "]", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Invalid token")).build();
		}
	}

	private String getDefaultTenant() {
		String defaultTenant = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_TENANT_ON_SIGNUP");
		if (defaultTenant == null) {
			LOGGER.debug("DEFAULT_TENANT_ON_SIGNUP not configured, using default value");
			defaultTenant = "DEFAULT_TENANT";
		}
		LOGGER.debug("Default tenant set to: " + defaultTenant);
		return defaultTenant;
	}

	private Response validateSignupInput(String username, String password, String confirmPassword, String email, String captcha, String content) {
		if (StringUtils.isBlank(username)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Username is mandatory")).build();
		}

		if (StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Password and confirm password are mandatory")).build();
		}

		if (!password.equals(confirmPassword)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Passwords do not match")).build();
		}

		try {
			PasswordChecker.getInstance().isValid(password, password);
		} catch (EMFUserError e) {
			LOGGER.error("Password is not valid", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Password is not valid")).build();
		} catch (Exception e) {
			LOGGER.error("Password is not valid", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Password is not valid")).build();
		}

		if (StringUtils.isBlank(email)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Email is mandatory")).build();
		}

		if (StringUtils.isBlank(captcha)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Captcha is required")).build();
		}

		if (StringUtils.isBlank(content)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Captcha content is missing")).build();
		}

		if (!content.equals(Password.hashPassword(captcha))) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Invalid captcha")).build();
		}

		return null;
	}

	private void sendActivationEmail(HttpServletRequest req, SbiUser user, String username) throws Exception {
		String host = req.getServerName();
		int port = req.getServerPort();

		String mailText = Resources.toString(getClass().getResource("/templates/confirmationMailTemplate.html"), StandardCharsets.UTF_8);
		String subject = "Activation Request";

		String token = SignupJWTTokenManager.createJWTToken(user.getUserId());
		String version = SbiCommonInfo.getVersion().substring(0, SbiCommonInfo.getVersion().lastIndexOf("."));

		String urlString = KnowageSystemConfiguration.getKnowageVueContext() + "/login?registrationToken=" + token + "&version=" + version;
		URL url = new URL(req.getScheme(), host, port, urlString);

		// Replace placeholders in template
		mailText = mailText.replace("%%WELCOME%%", "Welcome to our platform");
		mailText = mailText.replace("%%USERNAME%%", username);
		mailText = mailText.replace("%%THANKS_MESSAGE%%", "Thank you for registering");
		mailText = mailText.replace("%%WELCOME_MESSAGE%%", "Please activate your account by clicking the link below");
		mailText = mailText.replace("%%URL%%", url.toString());
		mailText = mailText.replace("%%URL_LABEL%%", "Activate Account");
		mailText = mailText.replace("%%BOOKMARK%%", "Bookmark our website");
		mailText = mailText.replace("%%QA%%", "Visit our Q&A section");
		mailText = mailText.replace("%%GITHUB%%", "Visit our GitHub");
		mailText = mailText.replace("%%DOCUMENTATION%%", "Read the documentation");

		LOGGER.debug("Activation url is equal to [" + url.toExternalForm() + "]");
		LOGGER.debug("Activation mail for user [" + username + "] successfully prepared");

		sendMail(user.getSbiUserAttributeses().iterator().next().getAttributeValue(), subject, mailText);
	}

	private void addAttribute(Set<SbiUserAttributes> attributes, int attrId, String attrValue, String defaultTenant) {
		if (attrValue != null && !attrValue.isEmpty()) {
			SbiUserAttributes a = new SbiUserAttributes();
			a.getCommonInfo().setOrganization(defaultTenant);
			SbiUserAttributesId id = new SbiUserAttributesId(attrId);
			a.setId(id);
			a.setAttributeValue(attrValue);
			attributes.add(a);
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

}
