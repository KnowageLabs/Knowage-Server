package it.eng.spagobi.security;

import java.util.List;

import org.apache.log4j.Logger;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

public class ProfiledLdapSecurityServiceSupplier extends LdapSecurityServiceSupplier {
	static private Logger logger = Logger.getLogger(ProfiledLdapSecurityServiceSupplier.class);
	private static final String ATTRIBUTE_AUTHENTICATION_MODE = "auth_mode";
	private static final String ATTRIBUTE_AUTHENTICATION_MODE_LDAP = "LDAP";
	private static final String ATTRIBUTE_AUTHENTICATION_MODE_INTERNAL = "internal";

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
		if (user == null) {
			logger.error("UserName not found into database");
			return null;
		} else {
			String authMode = getAuthMode(user);
			if (ATTRIBUTE_AUTHENTICATION_MODE_INTERNAL.equals(authMode)) {
				return new InternalSecurityServiceSupplierImpl().checkAuthentication(userId, psw);
			} else {
				return super.checkAuthentication(userId, psw);
			}
		}
	}

	private String getAuthMode(SbiUser user) {
		List<SbiUserAttributes> attributes = DAOFactory.getSbiUserDAO().loadSbiUserAttributesById(user.getId());
		String authMode = ATTRIBUTE_AUTHENTICATION_MODE_LDAP;
		for (SbiUserAttributes attribute : attributes) {
			if (ATTRIBUTE_AUTHENTICATION_MODE.equals(attribute.getSbiAttribute().getAttributeName())) {
				authMode = attribute.getAttributeValue();
				break;
			}
		}
		logger.debug("Authentication mode: " + authMode);
		return authMode;
	}

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		return new InternalSecurityServiceSupplierImpl().createUserProfile(jwtToken);
	}
}