package it.eng.spagobi.commons.services;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.map.IMap;

import it.eng.knowage.monitor.IKnowageMonitor;
import it.eng.knowage.monitor.KnowageMonitorFactory;
import it.eng.knowage.privacymanager.LoginEventBuilder;
import it.eng.knowage.privacymanager.PrivacyManagerClient;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.SessionUserProfileBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.bo.UserProfileUtility;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.TOTPService;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.InternalSecurityServiceSupplierImpl;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.security.utils.EncryptionPBEWithMD5AndDESManager;
import it.eng.spagobi.services.common.GenericJWTSsoService;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.common.JWTSsoServiceAlgorithmFactory;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.oauth2.Oauth2SsoService;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.notifier.fiware.OAuth2Utils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

@Path("/login")
public class LoginResource extends AbstractSpagoBIResource {

	static Logger logger = Logger.getLogger(LoginResource.class);
	private static final JWTSsoServiceAlgorithmFactory ALGORITHM_FACTORY = JWTSsoServiceAlgorithmFactory.getInstance();
	private static final String PROP_NODE = "changepwd.";

	/** The format date to manage the data validation. */
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	@POST
	@Path("/")
	@PublicService
	public Response login(@Context HttpServletRequest req, Map<String, String> payload) throws Exception {
		// Initialize context
		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.login.authentication");

		try {
			// Validate payload
			String userId = payload.get("userID");
			String pwd = payload.get("password");
			if (StringUtils.isBlank(userId) || StringUtils.isBlank(pwd)) {
				logger.error("Missing credentials in login request");
				return buildUnauthorizedResponse("Missing credentials");
			}

			logger.debug("Login attempt for userID=" + userId);

			// Authenticate user
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();
			SpagoBIUserProfile userProfile = supplier.checkAuthentication(userId, pwd);

			if (userProfile == null) {
				logger.error("Authentication failed for userID=" + userId);
				AuditLogUtilities.updateAudit(req, null, "SPAGOBI.Login", null, "KO");
				monitor.stop(new SpagoBIRuntimeException("Incorrect credentials"));
				return buildUnauthorizedResponse("Incorrect credentials");
			}

			// Load user entity
			SbiUser user = userDao.loadSbiUserByUserId(userId);
			if (user == null) {
				logger.error("User entity not found for userID=" + userId);
				return buildUnauthorizedResponse("User not found");
			}

			// Check if password is blocked
			if (Boolean.TRUE.equals(user.getFlgPwdBlocked())) {
				logger.error("Password blocked for userID=" + userId);
				AuditLogUtilities.updateAudit(req, null, "SPAGOBI.Login", null, "KO");
				monitor.stop(new SpagoBIRuntimeException("Password blocked"));
				return buildUnauthorizedResponse("Password blocked");
			}

			// Handle MFA if required
			if (checkCodeMfa(req, user)) {
				return handleMfaRequired(userId, user);
			}

			monitor.stop();

			// Check admin privileges
			boolean isAdminUser = isUserAdmin(user, userDao);

			// Validate and update password if not admin
			if (!isAdminUser) {
				Response passwordCheckResponse = validateAndUpdatePassword(user, userDao);
				if (passwordCheckResponse != null) {
					return passwordCheckResponse;
				}
			}

			// Build user profile and session
			String jwtToken = userProfile.getUniqueIdentifier();
			IEngUserProfile profile = loadUserProfile(jwtToken, req);

			if (profile == null) {
				logger.error("Failed to load user profile for userID=" + userId);
				AuditLogUtilities.updateAudit(req, null, "SPAGOBI.Login", null, "ERR");
				return buildUnauthorizedResponse("User profile not created");
			}

			// Store profile in session
			storeProfileInSession((UserProfile) profile, req);
			recordLoginEvent((UserProfile) profile);

			// Complete login with tenant context
			return completeLogin(req, profile);

		} catch (SecurityException se) {
			logger.error("Security exception during login", se);
			monitor.stop(se);
			throw se;
		} catch (Exception e) {
			logger.error("Unexpected error during login", e);
			monitor.stop(e);
			return buildErrorResponse("Login failed", e);
		}
	}

	/**
	 * Verifies MFA code and completes login process
	 * Accepts MFA token and TOTP code for user authentication with multi-factor authentication
	 * Supports first-time MFA setup with secret provisioning
	 *
	 * @param req The HTTP servlet request
	 * @param payload Request payload containing tokenMfa (JWT MFA token), code (TOTP code), and optional secret
	 * @return Response with login token on success, or error response on failure
	 * @throws Exception if verification or login process fails
	 */
	@POST
	@Path("/verifyMfa")
	@PublicService
	public Response verifyMfaCode(@Context HttpServletRequest req, Map<String, String> payload) throws Exception {

		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.login.mfa.verification");

		try {
			// Estraggo i parametri dal payload
			String tokenMfa = payload.get("tokenMfa");
			String code = payload.get("code");
			String secret = payload.get("secret"); // Optional: only for first time setup

			if (StringUtils.isBlank(tokenMfa)) {
				logger.error("MFA token is missing");
				monitor.stop(new SpagoBIRuntimeException("MFA token is missing"));
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "MFA token is required")).build();
			}

			if (StringUtils.isBlank(code)) {
				logger.error("MFA code is missing");
				monitor.stop(new SpagoBIRuntimeException("MFA code is missing"));
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "MFA code is required")).build();
			}

			// Verifica il token JWT MFA
			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(JWTSsoService.KNOWAGE_ISSUER + "_MFA").build();

			DecodedJWT decodedJWT;
			try {
				decodedJWT = verifier.verify(tokenMfa);
			} catch (Exception e) {
				logger.error("Invalid or expired MFA token", e);
				monitor.stop(e);
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid or expired MFA token")).build();
			}

			// Estraggo l'userId dal token
			String userId = decodedJWT.getClaim("userId").asString();

			if (StringUtils.isBlank(userId)) {
				logger.error("UserId not found in MFA token");
				monitor.stop(new SpagoBIRuntimeException("UserId not found in MFA token"));
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid MFA token")).build();
			}

			// Carico l'utente dal database
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			SbiUser user = userDao.loadSbiUserByUserId(userId);

			if (user == null) {
				logger.error("User not found: " + userId);
				monitor.stop(new SpagoBIRuntimeException("User not found"));
				return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "User not found")).build();
			}

			// Determino il secret da usare per la verifica
			String secretToVerify;
			if (StringUtils.isNotBlank(secret)) {
				// Primo setup: usa il secret fornito
				secretToVerify = secret;
			} else if (user.getOtpSecret() != null) {
				// Utente già configurato: usa il secret dal DB
				secretToVerify = EncryptionPBEWithMD5AndDESManager.decrypt(user.getOtpSecret());
			} else {
				logger.error("No secret available for verification");
				monitor.stop(new SpagoBIRuntimeException("No secret available"));
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Secret is required for first time setup")).build();
			}

			// Verifica il codice MFA
			boolean isValidCode = TOTPService.verifyCode(secretToVerify, code);

			if (!isValidCode) {
				logger.error("Invalid MFA code for user: " + userId);
				monitor.stop(new SpagoBIRuntimeException("Invalid MFA code"));
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid MFA code")).build();
			}

			// Se è il primo setup, salvo il secret nel database
			if (StringUtils.isNotBlank(secret) && user.getOtpSecret() == null) {
				user.setOtpSecret(EncryptionPBEWithMD5AndDESManager.encrypt(secret));
				userDao.updateSbiUser(user, user.getId());
				logger.info("MFA secret saved for user: " + userId);
			}

			// MFA verificata con successo: procedo con il login completo
			logger.debug("MFA verification successful for user: " + userId);
			MessageBuilder msgBuilder = new MessageBuilder();
			Locale locale = msgBuilder.getLocale(req);

			SpagoBIUserProfile spagoBIUserProfile = new SpagoBIUserProfile();
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, InternalSecurityServiceSupplierImpl.USER_JWT_TOKEN_EXPIRE_HOURS);
			Date expiresAt = calendar.getTime();
			spagoBIUserProfile.setUserId(user.getUserId());
			spagoBIUserProfile.setUserName(user.getFullName());
			spagoBIUserProfile.setOrganization(user.getCommonInfo().getOrganization());
			spagoBIUserProfile.setIsSuperadmin(user.getIsSuperadmin());

			List<String> roleNames = user.getSbiExtUserRoleses().stream().map(SbiExtRoles::getName).collect(Collectors.toCollection(ArrayList<String>::new));
			spagoBIUserProfile.setRoles(roleNames.toArray(String[]::new));

			String jwtToken = JWTSsoService.userId2jwtToken(userId, expiresAt);
			spagoBIUserProfile.setUniqueIdentifier(jwtToken);

			IEngUserProfile profile = loadUserProfile(jwtToken, req);

			if (profile == null) {
				logger.error("User profile not found for: " + userId);
				monitor.stop(new SpagoBIRuntimeException("User profile not found"));
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "User profile not found")).build();
			}

			storeProfileInSession((UserProfile) profile, req);
			recordLoginEvent((UserProfile) profile);

			Response response = completeLogin(req, profile);
			monitor.stop();
			return response;

		} catch (Exception e) {
			logger.error("Error during MFA verification", e);
			monitor.stop(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Internal server error during MFA verification")).build();
		}
	}

	/**
	 * OAuth2 login endpoint Accepts OAuth2 access token and performs authentication
	 */
	@GET
	@POST
	@Path("/oauth2")
	@PublicService
	public Response loginOAuth2(@Context HttpServletRequest req) throws Exception {

		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.login.oauth2.authentication");

		try {
			return performOAuth2Login(req, monitor);
		} catch (Exception e) {
			logger.error("Unexpected error during OAuth2 login", e);
			monitor.stop(e);
			return buildErrorResponse("OAuth2 login failed", e);
		}
	}

	/**
	 * OIDC login endpoint Accepts OIDC ID token and performs authentication with Keycloak
	 */
	@POST
	@Path("/oidc/authorization_code")
	@PublicService
	public Response loginOIDC(@Context HttpServletRequest req, Map<String, String> payload) throws Exception {
		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.login.oidc.authentication");

		try {

			final String code = payload.get("code");
			final String codeVerifier = payload.get("code_verifier");
			if (StringUtils.isBlank(code)) {
				return buildUnauthorizedResponse("Missing authorization code");
			}

			OAuth2Config oauth2Config = OAuth2Config.getInstance();
			TokenResponse token = exchangeCodeForToken(oauth2Config, code, codeVerifier);

			if (token == null || StringUtils.isBlank(token.access_token)) {
				monitor.stop(new SpagoBIRuntimeException("Failed to obtain access token from OAuth2 provider"));
				return buildUnauthorizedResponse("Failed to obtain access token from OAuth2 provider");
			}
			req.setAttribute(Oauth2SsoService.ACCESS_TOKEN, token.access_token);
			// req.setAttribute(Oauth2SsoService.ID_TOKEN, token.id_token);

			return performOAuth2Login(req, monitor);

		} catch (Exception e) {
			logger.error("Unexpected error during OIDC login", e);
			monitor.stop(e);
			return buildErrorResponse("OIDC login failed", e);
		}

	}

	/**
	 * OIDC Nonce generation endpoint
	 */
	@GET
	@Path("/oidc/nonce")
	@PublicService
	public Response generateNonce() {
		String nonce = OAuth2Utils.createNonce();
		return Response.ok(Map.of("nonce", nonce)).build();
	}

	/**
	 * OIDC ID token validation endpoint Accepts OIDC ID token and performs validation
	 */
	@POST
	@Path("/oidc/implicit")
	@PublicService
	public Response validateOIDCIdToken(@Context HttpServletRequest req, Map<String, String> payload) {
		IKnowageMonitor monitor = KnowageMonitorFactory.getInstance().start("knowage.oidc.validate.idtoken");

		try {
			logger.debug("Starting ID token validation");

			// Extract parameters from payload
			String idToken = payload.get("id_token");
			String expectedNonce = payload.get("nonce");

			if (StringUtils.isBlank(idToken)) {
				logger.error("Missing id_token in payload");
				monitor.stop(new SpagoBIRuntimeException("Missing id_token"));
				return buildUnauthorizedResponse("Missing id_token");
			}

			if (StringUtils.isBlank(expectedNonce)) {
				logger.error("Missing nonce in payload");
				monitor.stop(new SpagoBIRuntimeException("Missing nonce"));
				return buildUnauthorizedResponse("Missing nonce");
			}

			logger.debug("ID token validation starting with expected nonce: " + expectedNonce);

			// Decode JWT
			DecodedJWT decodedJWT = JWT.decode(idToken);
			logger.debug("JWT token properly decoded");

			// Verify token signature with JWKS
			validateIdTokenSignature(decodedJWT, idToken);

			// Verify issuer
			String configuredIssuer = OAuth2Config.getInstance().getJwtTokenIssuer();
			if (!decodedJWT.getIssuer().equals(configuredIssuer)) {
				logger.error("JWT token issuer [" + decodedJWT.getIssuer() + "] does not match the configured one [" + configuredIssuer + "]");
				monitor.stop(new SpagoBIRuntimeException("JWT issuer mismatch"));
				return buildUnauthorizedResponse("JWT issuer does not match");
			}
			logger.debug("JWT issuer verified");

			// Verify audience (must match client_id)
			String configuredClientId = OAuth2Config.getInstance().getClientId();
			if (decodedJWT.getAudience() == null || decodedJWT.getAudience().isEmpty() ||
				!decodedJWT.getAudience().get(0).equals(configuredClientId)) {
				String audience = decodedJWT.getAudience() != null && !decodedJWT.getAudience().isEmpty() ? decodedJWT.getAudience().get(0) : "null";
				logger.error("JWT token aud [" + audience + "] does not match the client id [" + configuredClientId + "]");
				monitor.stop(new SpagoBIRuntimeException("JWT audience mismatch"));
				return buildUnauthorizedResponse("JWT audience does not match");
			}
			logger.debug("JWT audience verified");

			// Verify nonce (provided by client)
			com.auth0.jwt.interfaces.Claim nonceClaim = decodedJWT.getClaim("nonce");
			if (nonceClaim.isNull() || !nonceClaim.asString().equals(expectedNonce)) {
				logger.error("JWT token nonce [" + nonceClaim.asString() + "] does not match the expected nonce [" + expectedNonce + "]");
				monitor.stop(new SpagoBIRuntimeException("JWT nonce mismatch"));
				return buildUnauthorizedResponse("JWT nonce does not match");
			}
			logger.debug("JWT nonce verified");

			logger.info("ID token validated successfully");
			monitor.stop();

			req.setAttribute(Oauth2SsoService.ID_TOKEN, idToken);
			return performOAuth2Login(req, monitor);

		} catch (Exception e) {
			logger.error("Error during ID token validation", e);
			monitor.stop(e);
			return buildErrorResponse("ID token validation failed", e);
		}
	}

	@POST
	@Path("/refreshToken")
	@PublicService
	public Response refreshToken(@Context HttpServletRequest req, Map<String, String> payload) {

		try {
			String refreshToken = payload.get("refreshToken");
			if (StringUtils.isBlank(refreshToken)) {
				return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Missing refreshToken")).build();
			}

			logger.debug("Refreshing access token using refreshToken");

			// 1) Get the Hazelcast distributed map
			IMap<String, String> tokenMap = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_REFRESH_TOKEN);

			// 2) Check if refresh token exists (revoked or never issued?)
			String userIdFromCache = tokenMap.get(refreshToken);
			if (userIdFromCache == null) {
				logger.warn("Refresh token not found or revoked");
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid or expired refreshToken")).build();
			}

			// 3) Verify JWT refresh token signature
			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			DecodedJWT decoded;

			try {
				decoded = JWT.require(algorithm).withIssuer(JWTSsoService.KNOWAGE_ISSUER).build().verify(refreshToken);
			} catch (Exception e) {
				logger.error("Invalid refresh token signature", e);
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid refreshToken")).build();
			}

			// 4) Extract data from refresh token
			String userIdFromJWT = decoded.getClaim("user_id").asString();
			Date expiresAt = decoded.getExpiresAt();
			String deviceFromJWT = decoded.getClaim("device").asString();

			// 5) Verify user ID consistency
			if (!userIdFromJWT.equals(userIdFromCache)) {
				logger.error("Refresh token mismatch: JWT user != Hazelcast user");
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Invalid refreshToken")).build();
			}

			// 6) Check token expiration
			if (expiresAt.before(new Date())) {
				logger.warn("Expired refresh token");
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Expired refreshToken")).build();
			}

			// 7) (Optional but recommended) Verify DEVICE / USER-AGENT
			String currentUA = req.getHeader("User-Agent");
			if (deviceFromJWT != null && !deviceFromJWT.equals(currentUA)) {
				logger.error("Refresh token used from different device");
				return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "RefreshToken used from different device")).build();
			}

			// 8) At this point the refresh token is valid → generate new access token
			String newAccessToken = JWTSsoService.userId2jwtToken(userIdFromJWT);

			// 9) (Optional) Generate a new refresh token and revoke the previous one
			// - Remove old token
			tokenMap.remove(refreshToken);
			// - Generate and save new token
			String newRefreshToken = storeTokenInHazelcast(userIdFromJWT, req);

			logger.info("Successfully refreshed access token for user: " + userIdFromJWT);

			return Response.ok(Map.of("token", newAccessToken, "refresh_token", newRefreshToken)).build();

		} catch (Exception e) {
			logger.error("Error during refresh token process", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Internal server error")).build();
		}
	}


	/**
	 * Generates a new refresh token and stores it in Hazelcast distributed cache
	 * The refresh token includes JWT ID, user ID, issue date, expiration time, and device information
	 * Tokens are stored with TTL matching the configured refresh expiry period
	 *
	 * @param userId The unique identifier of the user
	 * @param req The HTTP servlet request (used to extract User-Agent header)
	 * @return The generated refresh token as a JWT string, or null if generation fails
	 */
	private String storeTokenInHazelcast(String userId, HttpServletRequest req) {
		try {
			logger.debug("Generating and storing refresh token for user: " + userId);

			long refreshExpiryHours = 10L;


			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			String jti = UUID.randomUUID().toString();
			Instant now = Instant.now();
			Instant exp = now.plus(refreshExpiryHours, ChronoUnit.HOURS);

			String userAgent = (req != null && req.getHeader("User-Agent") != null) ? req.getHeader("User-Agent") : null;

			String refreshToken = JWT.create().withIssuer(JWTSsoService.KNOWAGE_ISSUER)
					.withJWTId(jti).withClaim("user_id", userId).withIssuedAt(Date.from(now)).withExpiresAt(Date.from(exp))
					.withClaim("device", userAgent).sign(algorithm);

			IMap<String, String> tokenMap = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_REFRESH_TOKEN);

			tokenMap.put(refreshToken, userId, refreshExpiryHours, TimeUnit.HOURS);

			logger.info(
					"Refresh token generated and stored in Hazelcast map : " + SpagoBIConstants.DISTRIBUTED_MAP_FOR_REFRESH_TOKEN + " for user : " + userId);

			return refreshToken;

		} catch (Exception e) {
			logger.error("Error generating/storing refresh token in Hazelcast for user " + userId, e);
			return null;
		}
	}

	/**
	 * Performs OAuth2 authentication logic
	 */
	private Response performOAuth2Login(HttpServletRequest req, IKnowageMonitor monitor) throws Exception {
		// Get userId using SSO service
		String userId = getUserIdWithSSO(req);

		if (StringUtils.isBlank(userId)) {
			logger.error("OAuth2 authentication failed");
			AuditLogUtilities.updateAudit(req, null, "SPAGOBI.Login.OAuth2", null, "KO");
			monitor.stop(new SpagoBIRuntimeException("OAuth2 authentication failed"));
			return buildUnauthorizedResponse("OAuth2 authentication failed", req);
		}

		logger.debug("OAuth2 user authenticated: userID=" + userId);

		// Create new user profile
		IEngUserProfile profile = GeneralUtilities.createNewUserProfile(userId);

		if (profile == null) {
			logger.error("User has no profile defined : " + userId);
			AuditLogUtilities.updateAudit(req, null, "SPAGOBI.Login.OAuth2", null, "ERR");
			monitor.stop(new SpagoBIRuntimeException("User has no profile defined"));
			return buildUnauthorizedResponse("User has no profile defined", req);
		}

		// Align with standard login: use default role profile when available
		profile = SessionUserProfileBuilder.getDefaultUserProfile((UserProfile) profile);

		monitor.stop();

		// Store profile in session
		storeProfileInSession((UserProfile) profile, req);
		recordLoginEvent((UserProfile) profile);

		// Complete login with tenant context
		return completeLoginForKnowageFE(req, profile);
	}

	/**
	 * Exchanges OAuth2 authorization code for access token
	 * Sends token request to OAuth2 provider endpoint and handles PKCE code verifier if provided
	 * Includes client authentication for CONFIDENTIAL clients
	 *
	 * @param cfg OAuth2 configuration containing client details and provider URLs
	 * @param code Authorization code received from OAuth2 provider
	 * @param codeVerifier PKCE code verifier (optional, for public clients)
	 * @return TokenResponse containing access token, ID token, refresh token, and expiration info
	 * @throws Exception if token exchange fails or provider is unreachable
	 */
	private TokenResponse exchangeCodeForToken(OAuth2Config cfg, String code, String codeVerifier) throws Exception {

		Client client = ClientBuilder.newBuilder().connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
				.readTimeout(10, java.util.concurrent.TimeUnit.SECONDS).build();

		Form form = new Form();
		form.param("grant_type", "authorization_code");
		form.param("client_id", cfg.getClientId());
		form.param("redirect_uri", cfg.getRedirectUrl());
		form.param("code", code);

		// Se il client è CONFIDENTIAL, aggiungi client_secret
		if (StringUtils.isNotBlank(cfg.getClientSecret())) {
			form.param("client_secret", cfg.getClientSecret());
		}

		// PKCE
		if (StringUtils.isNotBlank(codeVerifier)) {
			form.param("code_verifier", codeVerifier);
		}

		Response resp = null;
		try {
			resp = client.target(cfg.getAccessTokenUrl()) // es: http://localhost:7071/realms/knowage/protocol/openid-connect/token
					.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

			String raw = resp.readEntity(String.class);

			if (resp.getStatus() != 200) {
				logger.error("Keycloak token endpoint error. HTTP " + resp.getStatus() + " body=" + raw);
				return null;
			}

			ObjectMapper om = new ObjectMapper();
			return om.readValue(raw, TokenResponse.class);

		} finally {
			if (resp != null) {
				resp.close();
			}
			client.close();
		}
	}

	public static class TokenResponse {
		public String access_token;
		public String id_token;
		public String refresh_token;
		public String token_type;
		public Long expires_in;
		public Long refresh_expires_in;
		public String scope;

		@Override
		public String toString() {
			return "TokenResponse{" + "access_token='" + (access_token != null ? "***" : null) + '\'' + ", id_token='" + (id_token != null ? "***" : null)
					+ '\'' + ", refresh_token='" + (refresh_token != null ? "***" : null) + '\'' + ", token_type='" + token_type + '\'' + ", expires_in="
					+ expires_in + ", refresh_expires_in=" + refresh_expires_in + ", scope='" + scope + '\'' + '}';
		}
	}

	/**
	 * Checks if Multi-Factor Authentication (MFA) is required for the user
	 * MFA is skipped for LDAP-based security implementations
	 * MFA requirement is determined by tenant configuration
	 *
	 * @param req The HTTP servlet request
	 * @param user The SbiUser entity to check
	 * @return true if MFA is required and enabled for the user's tenant, false otherwise
	 * @throws Exception if tenant or configuration lookup fails
	 */
	private boolean checkCodeMfa(HttpServletRequest req, SbiUser user) throws Exception {

		String securityServiceSupplier = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
		// If securityServiceSupplier is Ldap compliant, skip MFA
		if (StringUtils.containsIgnoreCase(securityServiceSupplier, "LdapSecurityService")) {
			return false;
		}

		ITenantsDAO tenantsDAO = DAOFactory.getTenantsDAO();
		SbiTenant sbiTenant = tenantsDAO.loadTenantByName(user.getCommonInfo().getOrganization());
		Boolean isActiviMfa = sbiTenant.getIsMfa();

		// If MFA is not required, allow access
		if (isActiviMfa == null || Boolean.FALSE.equals(isActiviMfa)) {
			return false;
		}

		return true;
	}

	/**
	 * Stores user profile in HTTP session and enriches it with PM (Portal Manager) information
	 * Enrichment includes additional user context and profile attributes from request
	 *
	 * @param userProfile The UserProfile object to store in session
	 * @param req The HTTP servlet request
	 */
	private void storeProfileInSession(UserProfile userProfile, HttpServletRequest req) {
		logger.debug("IN");
		// PM-int
		UserProfileUtility.enrichProfile(userProfile, req, req.getSession());

		req.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, userProfile);

		logger.debug("OUT");
	}


	/**
	 * Checks if user password needs to be changed based on security policies
	 * Validates password expiration, age, disuse period, and other configured controls
	 * Returns true if password change is mandatory, false if password is active
	 *
	 * @param user The SbiUser entity to validate
	 * @return true if password change is required, false otherwise
	 * @throws Exception if configuration lookup fails
	 */
	private boolean checkPwd(SbiUser user) throws Exception {
		logger.debug("IN");
		boolean toReturn = false;
		if (user == null) {
			return toReturn;
		}

		if (encriptedBefore72(user)) {
			logger.info("Old encrypting method. Change password required.");
			return true;
		}

		Date currentDate = new Date();

		// gets the active controls to applicate:
		IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
		List lstConfigChecks = configDao.loadConfigParametersByProperties(PROP_NODE);
		logger.debug("checks found on db: " + lstConfigChecks.size());

		for (int i = 0; i < lstConfigChecks.size(); i++) {
			Config check = (Config) lstConfigChecks.get(i);
			if ((SpagoBIConstants.CHANGEPWD_CHANGE_FIRST).equals(check.getLabel()) && new Boolean(check.getValueCheck()) == true
					&& user.getDtLastAccess() == null) {
				// if dtLastAccess isn't enhanced it represents the first login, so is necessary change the pwd
				logger.info("The pwd needs to activate!");
				toReturn = true;
				break;
			}

			if ((SpagoBIConstants.CHANGEPWD_EXPIRED_TIME).equals(check.getLabel()) && user.getDtPwdEnd() != null
					&& currentDate.compareTo(user.getDtPwdEnd()) >= 0) {
				// check if the pwd is expiring, in this case it's locked.
				logger.info("The pwd is expiring... it should be changed");
				toReturn = true;
				break;
			}
			if ((SpagoBIConstants.CHANGEPWD_DISACTIVE_TIME).equals(check.getLabel())) {
				// defines the end date for uselessness
				Date tmpEndForUnused = null;
				if (user.getDtLastAccess() != null) {
					SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
					Calendar cal = Calendar.getInstance();
					cal.set(user.getDtLastAccess().getYear() + 1900, user.getDtLastAccess().getMonth(), user.getDtLastAccess().getDate());
					cal.add(Calendar.MONTH, 6);
					try {
						tmpEndForUnused = StringUtilities.stringToDate(sdf.format(cal.getTime()), DATE_FORMAT);
						logger.debug("End Date For Unused: " + tmpEndForUnused);
					} catch (Exception e) {
						logger.error("The control pwd goes on error: " + e);
					}
				}
				if (tmpEndForUnused != null && currentDate.compareTo(tmpEndForUnused) >= 0) {
					// check if the pwd is unused by 6 months, in this case it's locked.
					logger.info("The pwd is unused more than 6 months! It's locked!!");
					toReturn = true;
					break;
				}
			}

		} // for

		// general controls: check if the account is already blocked, otherwise update dtLastAccess field
		if (user.getFlgPwdBlocked() != null && user.getFlgPwdBlocked()) {
			// if flgPwdBlocked is true the user cannot goes on
			logger.info("The pwd needs to activate!");
			toReturn = true;
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Checks if user password was encrypted using the old SHA_SECRETPHRASE method (before version 7.2)
	 * Passwords encrypted with this legacy method require immediate change
	 *
	 * @param user The SbiUser entity to check
	 * @return true if password uses legacy encryption format, false otherwise
	 */
	private boolean encriptedBefore72(SbiUser user) {
		return user.getPassword().startsWith(Password.PREFIX_SHA_SECRETPHRASE_ENCRIPTING);
	}

	/**
	 * Builds an unauthorized response with error message
	 */
	private Response buildUnauthorizedResponse(String errorMessage) {
		return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("error", errorMessage)).build();
	}

	/**
	 * Builds an unauthorized response with error message and optional redirect
	 * If JWT_SERVICE_LOGIN_URL is configured, redirects to that URL; otherwise returns JSON error
	 */
	private Response buildUnauthorizedResponse(String errorMessage, HttpServletRequest req) {
		logger.debug("IN buildUnauthorizedResponse with request");

		// Try to get JWT_SERVICE_LOGIN_URL from configuration or environment
		String loginUrl = System.getProperty("JWT_SERVICE_LOGIN_URL", System.getenv("JWT_SERVICE_LOGIN_URL"));

		if (StringUtils.isNotBlank(loginUrl)) {
			logger.debug("JWT_SERVICE_LOGIN_URL is configured: " + loginUrl + ". Performing redirect.");
			try {
				URI redirectUri = new URI(loginUrl);
				return Response.seeOther(redirectUri).build();
			} catch (Exception e) {
				logger.error("Error creating redirect URI from JWT_SERVICE_LOGIN_URL: " + loginUrl, e);
				// Fall back to standard unauthorized response
				return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("error", errorMessage)).build();
			}
		} else {
			logger.debug("JWT_SERVICE_LOGIN_URL is not configured. Returning standard unauthorized response.");
			return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("error", errorMessage)).build();
		}
	}

	/**
	 * Builds an error response for unexpected errors
	 */
	private Response buildErrorResponse(String errorMessage, Exception e) {
		logger.error(errorMessage, e);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("error", errorMessage)).build();
	}

	/**
	 * Checks if user has admin role
	 */
	private boolean isUserAdmin(SbiUser user, ISbiUserDAO userDao) throws Exception {
		String strAdminPattern = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ROLE-TYPE-PATTERNS.ADMIN-PATTERN");
		if (user == null || StringUtils.isBlank(strAdminPattern)) {
			return false;
		}

		List lstRoles = userDao.loadSbiUserRolesById(user.getId());
		for (Object roleObj : lstRoles) {
			SbiExtRoles extRole = (SbiExtRoles) roleObj;
			Role role = DAOFactory.getRoleDAO().loadByID(extRole.getExtRoleId());
			if (role != null && role.getName().equals(strAdminPattern)) {
				logger.debug("User is administrator. Checks on the password are not applied!");
				return true;
			}
		}
		return false;
	}

	/**
	 * Validates and updates user password if needed
	 */
	private Response validateAndUpdatePassword(SbiUser user, ISbiUserDAO userDao) throws Exception {
		logger.debug("Validation password starting...");
		boolean goToChangePwd = checkPwd(user);

		if (goToChangePwd) {
			if (user.getPassword().startsWith(Password.PREFIX_SHA_SECRETPHRASE_ENCRIPTING)) {
				logger.info("Old encrypting method. Change password required.");
				return Response.status(Response.Status.FORBIDDEN)
						.entity(Map.of("error", "Password expired", "requiresPasswordChange", true, "reason", "Old encryption method"))
						.build();
			}
			return Response.status(Response.Status.FORBIDDEN)
					.entity(Map.of("error", "Password expired", "requiresPasswordChange", true))
					.build();
		}

		logger.info("The pwd is active!");
		// Update last access date on db with current date
		try {
			user.setDtLastAccess(new Date());
			userDao.updateSbiUser(user, user.getId());
		} catch (Exception e) {
			logger.error("Non-fatal error while updating user's dtLastAccess", e);
		}

		return null;
	}

	/**
	 * Handles MFA requirements when needed
	 */
	private Response handleMfaRequired(String userId, SbiUser user) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, 5);
		Date expiresAt = calendar.getTime();

		Builder tokenBuilder = JWT.create()
				.withIssuer(JWTSsoService.KNOWAGE_ISSUER + "_MFA")
				.withClaim("userId", user.getUserId())
				.withExpiresAt(expiresAt);
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		String jwtToken = tokenBuilder.sign(algorithm);

		if (user.getOtpSecret() == null) {
			String secret = TOTPService.generateSecret();
			String qrCodeUrl = TOTPService.getQRBarcodeURL(userId, JWTSsoService.KNOWAGE_ISSUER, secret);
			return Response.ok(Map.of("secret", secret, "qrCodeUrl", qrCodeUrl, "tokenMfa", jwtToken)).build();
		}

		return Response.ok(Map.of("tokenMfa", jwtToken)).build();
	}

	/**
	 * Loads user profile from JWT token
	 */
	private IEngUserProfile loadUserProfile(String jwtToken, HttpServletRequest req) throws Exception {
		logger.debug("START - Getting user profile");

		IEngUserProfile profile = UserUtilities.getUserProfile(jwtToken);
		if (profile == null) {
			logger.error("User not created");
			AuditLogUtilities.updateAudit(req, profile, "SPAGOBI.Login", null, "ERR");
			return null;
		}

		logger.debug("END - Getting user profile");

		// In case user has a default role, we get his default user profile object
		return SessionUserProfileBuilder.getDefaultUserProfile((UserProfile) profile);
	}

	/**
	 * Records login event for privacy management
	 */
	private void recordLoginEvent(UserProfile userProfile) {
		LoginEventBuilder eventBuilder = new LoginEventBuilder();
		eventBuilder.appendSession("knowage", userProfile.getSourceIpAddress(), userProfile.getSessionId(),
								   userProfile.getSessionStart(), userProfile.getUserId().toString());
		eventBuilder.appendUserAgent(userProfile.getOs(), userProfile.getSourceIpAddress(),
									  userProfile.getSourceSocketEnabled(), userProfile.getUserAgent());
		PrivacyManagerClient.getInstance().sendMessage(eventBuilder.getDTO());
	}

	/**
	 * Completes the login process with tenant context and audit logging
	 */
	private Response completeLogin(HttpServletRequest req, IEngUserProfile profile) throws Exception {
		UserProfile userProfile = (UserProfile) profile;
		Tenant tenant = new Tenant(userProfile.getOrganization());
		TenantManager.setTenant(tenant);

		try {
			// Start writing log in the DB
			Session aSession = null;
			try {
				aSession = HibernateSessionManager.getCurrentSession();
				AuditLogUtilities.updateAudit(req, profile, "SPAGOBI.Login", null, "OK");
			} catch (HibernateException he) {
				logger.error("Error writing audit log", he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null && aSession.isOpen()) {
					aSession.close();
				}
			}
			return Response.ok(Map.of("token", profile.getUserUniqueIdentifier())).build();

		} finally {
			TenantManager.unset();
		}
	}

	private Response completeLoginForKnowageFE(HttpServletRequest req, IEngUserProfile profile) throws Exception {
		UserProfile userProfile = (UserProfile) profile;

		Tenant tenant = new Tenant(userProfile.getOrganization());
		TenantManager.setTenant(tenant);

		try {
			Session aSession = null;
			try {
				aSession = HibernateSessionManager.getCurrentSession();
				AuditLogUtilities.updateAudit(req, profile, "SPAGOBI.Login", null, "OK");
			} catch (HibernateException he) {
				logger.error("Error writing audit log", he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null && aSession.isOpen()) {
					aSession.close();
				}
			}


			URI redirectUri = URI
					.create(System.getProperty("JWT_KNOWAGE_VUE", System.getenv("JWT_KNOWAGE_VUE")) + "login?authToken=" + profile.getUserUniqueIdentifier());

			return Response.seeOther(redirectUri).build();

		} finally {
			TenantManager.unset();
		}
	}



	/**
	 * Finds the user identifier from http request or from SSO system (by the http request in input).
	 * Use the SsoServiceInterface for read the userId in all cases,
	 * if SSO is disabled use FakeSsoService. Check spagobi_sso.xml
	 *
	 * @param request The http request
	 * @return the current user unique identified
	 */
	private String getUserIdWithSSO(HttpServletRequest request) {
		logger.debug("IN");
		String userId = null;
		try {

			String jwtLabel = System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL"));
			SingletonConfig config = SingletonConfig.getInstance();
			Object ssoActiveValue = config.getConfigValue("SPAGOBI_SSO.ACTIVE");
			boolean ssoActive = Boolean.parseBoolean(String.valueOf(ssoActiveValue));
			if (ssoActive) {
				SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
				userId = userProxy.readUserIdentifier(request);
			} else if (jwtLabel != null && request.getParameter(jwtLabel) != null) {
				GenericJWTSsoService genericJWTSsoService = new GenericJWTSsoService();
				userId = genericJWTSsoService.readUserIdentifier(request);
			}

		} catch (Exception e) {
			logger.error("Authentication failed", e);
		} finally {
			logger.debug("OUT");
		}
		return userId;
	}

	/**
	 * Validates ID token signature using JWKS endpoint
	 */
	private void validateIdTokenSignature(DecodedJWT decodedJWT, String idToken) throws Exception {
		try {
			com.auth0.jwk.JwkProvider provider = new com.auth0.jwk.JwkProviderBuilder(
				new java.net.URL(OAuth2Config.getInstance().getJWKSUrl())
			).build();

			com.auth0.jwk.Jwk jwk = provider.get(decodedJWT.getKeyId());
			Algorithm algorithm = Algorithm.RSA256((java.security.interfaces.RSAPublicKey) jwk.getPublicKey(), null);
			JWT.require(algorithm).build().verify(idToken);

			logger.debug("ID token signature verified successfully");
		} catch (Exception e) {
			logger.error("Error verifying ID token signature", e);
			throw new SpagoBIRuntimeException("ID token signature verification failed", e);
		}
	}




}