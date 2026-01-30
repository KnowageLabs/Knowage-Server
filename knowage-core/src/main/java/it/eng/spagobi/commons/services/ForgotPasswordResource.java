package it.eng.spagobi.commons.services;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.auth0.jwt.exceptions.TokenExpiredException;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.validation.PasswordChecker;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.signup.validation.SignupJWTTokenManager;

@Path("/resetPassword")
public class ForgotPasswordResource {

	private static final Logger LOGGER = Logger.getLogger(ForgotPasswordResource.class);

	@POST
	@Path("/sendEmail")
	@PublicService
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendEmail(@Context HttpServletRequest req, Map<String, String> payload) {

		String mail = payload.get("mail");

		if (StringUtils.isBlank(mail)) {
			return Response.ok(Map.of("message", "Reset email sent successfully")).build();
		}

		ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
		ArrayList<SbiUser> lstUser = userDao.loadSbiUserFromEmail(mail);

		if (lstUser == null || lstUser.isEmpty()) {
			return Response.ok(Map.of("message", "Reset email sent successfully")).build();
		}

		if (lstUser.size() > 1) {
			LOGGER.warn("Multiple users found for email: " + mail);
			return Response.ok(Map.of("message", "Reset email sent successfully")).build();
		}

		try {
			SbiUser user = lstUser.get(0);

			String token = SignupJWTTokenManager.createJWTToken(user.getUserId());
			String version = SbiCommonInfo.getVersion().substring(0, SbiCommonInfo.getVersion().lastIndexOf("."));

			String resetUrl = buildResetPasswordUrl(req, token, version);
			String mailSubject = "Password Reset Request";
			String mailBody = buildResetPasswordEmailBody(resetUrl);

			sendMail(mail, mailSubject, mailBody);

			return Response.ok(Map.of("message", "Reset email sent successfully")).build();

		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while executing the sendEmail", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "An error occurred while sending reset email")).build();
		}
	}

	@GET
	@Path("/veifyToken")
	@PublicService
	@Produces(MediaType.APPLICATION_JSON)
	public Response veifyToken(@QueryParam("token") String token) {

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

			return Response.ok(Map.of("message", "Token validated successfully", "userId", userId)).build();

		} catch (TokenExpiredException te) {
			LOGGER.error("Expired Token [" + token + "]", te);
			return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Token expired")).build();
		} catch (Exception e) {
			LOGGER.error("Generic token validation error [" + token + "]", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Invalid token")).build();
		}
	}

	@POST
	@Path("/passwordChange")
	@PublicService
	@Produces(MediaType.APPLICATION_JSON)
	public Response passwordChange(@Context HttpServletRequest req, Map<String, String> payload) {

		String password = payload.get("password");
		String confirmPassword = payload.get("confirmPassword");
		String token = payload.get("token");

		// Validate password inputs
		Response validationError = validatePasswordInput(password, confirmPassword);
		if (validationError != null) {
			return validationError;
		}

		// Verify token and get user
		try {
			String userId = SignupJWTTokenManager.verifyJWTToken(token);
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId(userId);

			if (user == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "User not found")).build();
			}

			// Update user password and expiration date
			updateUserPassword(user, password);
			userDao.updateSbiUser(user, user.getId());

			return Response.ok(Map.of("message", "Password changed successfully")).build();

		} catch (TokenExpiredException te) {
			LOGGER.error("Expired Token [" + token + "]", te);
			return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Token expired")).build();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while changing password", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "An error occurred while changing password")).build();
		}
	}

	private Response validatePasswordInput(String password, String confirmPassword) {
		if (StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Password and confirm password are mandatory")).build();
		}

		if (!password.equals(confirmPassword)) {
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Passwords do not match")).build();
		}

		try {
			PasswordChecker.getInstance().isValid(password, password);
		} catch (Exception e) {
			LOGGER.error("Password is not valid", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Password is not valid")).build();
		}

		return null;
	}

	private void updateUserPassword(SbiUser user, String password) throws Exception {
		Date currentDate = new Date();
		user.setDtLastAccess(currentDate);
		user.setPassword(Password.hashPassword(password));
		user.setFlgPwdBlocked(false);

		// Set password expiration date if configured
		setPasswordExpirationDate(user, currentDate);
	}

	private void setPasswordExpirationDate(SbiUser user, Date beginDate) throws EMFUserError, Exception {
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
		List<Config> lstConfigChecks = configDao.loadConfigParametersByProperties(SpagoBIConstants.CHANGEPWD_EXPIRED_TIME);

		if (lstConfigChecks.isEmpty()) {
			return;
		}

		Config config = lstConfigChecks.get(0);
		if (!config.isActive()) {
			return;
		}

		try {
			String dateFormat = "yyyy-MM-dd";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Calendar cal = Calendar.getInstance();
			cal.set(beginDate.getYear() + 1900, beginDate.getMonth(), beginDate.getDate());
			cal.add(Calendar.DATE, Integer.parseInt(config.getValueCheck()));

			Date endDate = StringUtilities.stringToDate(sdf.format(cal.getTime()), dateFormat);
			LOGGER.debug("End Date for expiration calculated: " + endDate);

			user.setDtPwdBegin(beginDate);
			user.setDtPwdEnd(endDate);
		} catch (Exception e) {
			LOGGER.error("Error while calculating password expiration date: " + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 14008, Collections.emptyList(), Collections.emptyMap());
		}
	}

	private String buildResetPasswordUrl(HttpServletRequest req, String token, String version) throws Exception {
		String urlString = req.getContextPath() + "/restful-services/signup/changePasswordMail?token=" + token + "&version=" + version;
		URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), urlString);
		return url.toString();
	}

	private String buildResetPasswordEmailBody(String resetUrl) {
		StringBuilder body = new StringBuilder();
		body.append("Please click the link below to reset your password:<br><br>");
		body.append("<a href=\"").append(resetUrl).append("\">Reset Password</a><br><br>");
		body.append("If you did not request a password reset, please ignore this email.");
		return body.toString();
	}

	private void sendMail(String emailAddress, String subject, String emailContent) throws Exception {

		it.eng.knowage.mailsender.dto.MessageMailDto messageMailDto = new it.eng.knowage.mailsender.dto.MessageMailDto();
		messageMailDto.setProfileName(it.eng.knowage.mailsender.dto.ProfileNameMailEnum.USER);
		messageMailDto.setTypeMailEnum(it.eng.knowage.mailsender.dto.TypeMailEnum.CONTENT);
		messageMailDto.setSubject(subject);
		messageMailDto.setText(emailContent);
		messageMailDto.setContentType("text/html");
		messageMailDto.setRecipients(new String[] { emailAddress });

		it.eng.knowage.mailsender.factory.FactoryMailSender.getMailSender(it.eng.spagobi.commons.SingletonConfig.getInstance().getConfigValue(it.eng.knowage.mailsender.IMailSender.MAIL_SENDER)).sendMail(messageMailDto);

	}

}
