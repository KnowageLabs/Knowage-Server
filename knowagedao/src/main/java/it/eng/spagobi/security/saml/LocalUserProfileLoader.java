package it.eng.spagobi.security.saml;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

public final class LocalUserProfileLoader {
	private static final Set<String> JWT_CLAIMS = Set.of("iss", "sub", "aud", "exp", "nbf", "iat", "jti",
			SsoServiceInterface.USER_ID);

	private LocalUserProfileLoader() {
	}

	public static SpagoBIUserProfile load(String userId) {
		return load(userId, DAOFactory.getSbiUserDAO());
	}

	static SpagoBIUserProfile load(String userId, ISbiUserDAO dao) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing local SAML account");
		}
		// User IDs are unique across tenants; the account determines the authorization tenant.
		SbiUser user = dao.loadSbiUserByUserId(userId);
		if (user == null || !userId.equals(user.getUserId()) || user.getCommonInfo() == null
				|| Boolean.TRUE.equals(user.getFlgPwdBlocked())) {
			throw new IllegalArgumentException("Local SAML account not found or blocked");
		}
		String tenant = user.getCommonInfo().getOrganization();
		if (tenant == null || tenant.trim().isEmpty()) {
			throw new IllegalArgumentException("Local SAML account has no tenant");
		}
		dao.setTenant(tenant);
		ArrayList<SbiExtRoles> assigned = dao.loadSbiUserRolesById(user.getId());
		Set<String> roles = new LinkedHashSet<>();
		if (assigned != null) {
			for (SbiExtRoles role : assigned) {
				if (role == null || role.getName() == null || role.getName().trim().isEmpty()
						|| role.getCommonInfo() == null || !tenant.equals(role.getCommonInfo().getOrganization())) {
					throw new IllegalArgumentException("Invalid local SAML role");
				}
				roles.add(role.getName());
			}
		}
		if (roles.isEmpty()) {
			throw new IllegalArgumentException("Local SAML account has no assigned roles");
		}
		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		profile.setUserId(user.getUserId());
		profile.setUserName(user.getFullName() == null || user.getFullName().trim().isEmpty() ? user.getUserId() : user.getFullName());
		profile.setOrganization(tenant);
		profile.setRoles(roles.toArray(new String[0]));
		profile.setIsSuperadmin(Boolean.TRUE.equals(user.getIsSuperadmin()));
		ArrayList<SbiUserAttributes> attributes = dao.loadSbiUserAttributesById(user.getId());
		if (attributes != null) {
			for (SbiUserAttributes attribute : attributes) {
				String name = attribute.getSbiAttribute().getAttributeName();
				if (name != null && !name.startsWith("kn_") && !JWT_CLAIMS.contains(name) && attribute.getAttributeValue() != null) {
					profile.getAttributes().put(name, attribute.getAttributeValue());
				}
			}
		}
		return profile;
	}
}
