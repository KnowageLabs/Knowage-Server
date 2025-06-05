package it.eng.spagobi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.auth0.jwt.interfaces.Claim;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SusSecurityServiceSupplier implements ISecurityServiceSupplier {

	private final Logger logger = Logger.getLogger(SusSecurityServiceSupplier.class);

	private boolean isSuperAdmin = false;

	public static final String NOME = "given_name";
	public static final String COGNOME = "family_name";
	public static final String USERNAME = "preferred_username";
	public static final String CODICI = "roles";
	public static final String CURRENT_ROLE = "current-role";

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		Map<String, Claim> userClaims = JWTSsoService.getClaims(jwtToken);
		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		String tenant = System.getProperty("JWT_TENANT", System.getenv("JWT_TENANT"));

		try {
			profile.setRoles(getRoles(userClaims));
			profile.setAttributes(getProfileAttributes(userClaims, profile.getRoles()));
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

	private HashMap getProfileAttributes(Map<String, Claim> userClaims, String[] roles) {
		Map<String, String> toReturn = new HashMap<>();
		if (roles != null && roles.length > 0) {
			String role = roles[0];
			List<String> codici = userClaims.get(CODICI).asList(String.class);

			toReturn.put("roles",
					String.join(",", codici.stream().filter(s -> s.contains(role)).map(x -> x.substring(x.lastIndexOf("_") + 1)).collect(Collectors.toList())));
		}

		return (HashMap) toReturn;
	}

	private String[] getRoles(Map<String, Claim> userClaims) {
		List<String> toReturn = new ArrayList<>();

		IRoleDAO roleDao = DAOFactory.getRoleDAO();
		try {

			String roleAdmin = System.getProperty("JWT_ROLE_KNOWAGE_ADMIN", System.getenv("JWT_ROLE_KNOWAGE_ADMIN"));
			if (userClaims.get(CURRENT_ROLE) != null) {
				String role = userClaims.get(CURRENT_ROLE).asString();
				String roleParsed = role.substring(role.lastIndexOf("_") + 1);

				if (roleParsed.equals(roleAdmin)) {
					isSuperAdmin = true;
				}

				if (roleDao.loadByName(roleParsed) != null) {
					logger.debug("Adding role [" + roleParsed + "] to user profile");
					toReturn.add(roleParsed);
				}
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
