package it.eng.spagobi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.auth0.jwt.interfaces.Claim;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class CotSecurityServiceSupplier implements ISecurityServiceSupplier {

	private final Logger logger = Logger.getLogger(CotSecurityServiceSupplier.class);

	private boolean isSuperAdmin = false;

	public static final String NOME = "name";
	public static final String COGNOME = "surname";
	public static final String USERNAME = "username";
	public static final String USER_ID = "user_id";
	public static final String ORGANITATION = "organization_code";
	public static final String SITE_COTE = "site_code";
	public static final String ROLES = "role_code";

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		Map<String, Claim> userClaims = JWTSsoService.getClaims(jwtToken);
		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		String tenant = System.getProperty("JWT_TENANT", System.getenv("JWT_TENANT"));

		try {
			profile.setRoles(getRoles(userClaims));
			profile.setAttributes(getProfileAttributes(userClaims));
			profile.setUniqueIdentifier(jwtToken);
			profile.setUserId(userClaims.get(USERNAME).asString());
			profile.setUserName(userClaims.get(NOME).asString() + " " + userClaims.get(COGNOME).asString());
			profile.setOrganization(tenant);
			profile.setIsSuperadmin(isSuperAdmin);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while building profile for user [" + userClaims.get(USERNAME).asString() + "]", e);
		}
		logger.debug("Profile object created for user [" + userClaims.get(USERNAME).asString() + "]");
		return profile;
	}

	private HashMap getProfileAttributes(Map<String, Claim> userClaims) {
		Map<String, String> toReturn = new HashMap<>();
		toReturn.put("azienda", userClaims.get(ORGANITATION).asString());
		toReturn.put("cot", userClaims.get(SITE_COTE).asString());
		return (HashMap) toReturn;
	}

	private String[] getRoles(Map<String, Claim> userClaims) {
		List<String> toReturn = new ArrayList<>();

		IRoleDAO roleDao = DAOFactory.getRoleDAO();
		try {

			String roleAdmin = System.getProperty("JWT_ROLE_KNOWAGE_ADMIN", System.getenv("JWT_ROLE_KNOWAGE_ADMIN"));
			String role = userClaims.get(ROLES).asString();

			if (role.equals(roleAdmin)) {
				isSuperAdmin = true;
			}

			if (roleDao.loadByName(role) != null) {
				logger.debug("Adding role [" + role + "] to user profile");
				toReturn.add(role);
			}
		} catch (Exception e) {
			logger.error("User [" + userClaims.get(USERNAME).asString() + "] will have no roles because of an exception.", e);
			return null;
		}
		return toReturn.toArray(new String[0]);
	}

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationToken(String token) {
		logger.debug("IN - token for createUserProfile " + token);
		return this.createUserProfile(token);
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean checkAuthorization(String userId, String function) {
		throw new UnsupportedOperationException();
	}

}
