package it.eng.spagobi.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ProfiledLdapSecurityServiceSupplier extends LdapSecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(ProfiledLdapSecurityServiceSupplier.class);

	private static final String SEARCH_USER_BEFORE_PSW = "SEARCH_USER_BEFORE_PSW";
	private static final String SEARCH_USER_BEFORE_USER = "SEARCH_USER_BEFORE_USER";
	private static final String SEARCH_USER_BEFORE = "SEARCH_USER_BEFORE";
	private static final String SEARCH_USER_BEFORE_FILTER = "SEARCH_USER_BEFORE_FILTER";

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
				/* NEW */
				String filename = System.getProperty(LDAP_AUTHENTICATION_CONFIG);
				Assert.assertNotNull(filename,
						"System property " + LDAP_AUTHENTICATION_CONFIG + " has not been configured. Please add it while starting your JVM.");
				Properties properties = new Properties();

				try {
					properties.load(new FileInputStream(filename));
				} catch (FileNotFoundException e) {
					logger.error("File properties [" + filename + "] not found");
				} catch (IOException e) {
					logger.error("Impossible to read properties file [" + filename + "]");
				}

				Boolean searchUserBefore = new Boolean(properties.getProperty(SEARCH_USER_BEFORE));
				if (searchUserBefore) {

					String postfix = properties.getProperty("DN_POSTFIX");
					postfix = postfix.startsWith(",") ? postfix.substring(1) : postfix;

					String usernameForLDAPAuthBefore = new String(properties.getProperty(SEARCH_USER_BEFORE_USER));

					boolean validUsername = usernameForLDAPAuthBefore != null && !usernameForLDAPAuthBefore.isEmpty();

					String username = null;
					String password = null;
					if (validUsername) {
						username = String.format("%s%s,%s", properties.getProperty("DN_PREFIX"), usernameForLDAPAuthBefore, postfix);

						String passwordForLDAPAuthBefore = new String(properties.getProperty(SEARCH_USER_BEFORE_PSW));
						password = passwordForLDAPAuthBefore;
						logger.debug("Found credentials in properties file for authentication before looking for attribute.");
					}

					InitialDirContext ctx;
					String ldapCompleteNameAttribute = null;
					try {
						ctx = bind(username, password);

						String[] searchAttrs = { "nsRole", "uid", "objectClass", "givenName", "gn", "sn", "cn" };
						SearchControls ctrls = new SearchControls();
						ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
						ctrls.setCountLimit(0);
						ctrls.setTimeLimit(0);
						ctrls.setReturningAttributes(searchAttrs);

						String filter = String.format(properties.getProperty(SEARCH_USER_BEFORE_FILTER), userId);

						try {
							NamingEnumeration<SearchResult> answer = ctx.search(postfix, filter, ctrls);

							while (answer.hasMore()) {
								SearchResult ldapResult = answer.next();
								// distinguish name
								ldapCompleteNameAttribute = ldapResult.getNameInNamespace();
								break;
							}
							ctx.close();
						} catch (NamingException e) {
							logger.error("Resource with given filters not found", e);
							return null;
						}
					} catch (FileNotFoundException e) {
						logger.error("File properties [" + filename + "] not found");
					} catch (IOException e) {
						logger.error("Impossible to read properties file [" + filename + "]");
					}

					return checkAuthentication(userId, ldapCompleteNameAttribute, psw);
				} else {
					return super.checkAuthentication(userId, psw);
				}
			}
		}

	}

	public SpagoBIUserProfile checkAuthentication(String userId, String ldapCompleteNameAttribute, String psw) {
		SpagoBIUserProfile profile = null;
		try {
			if (userId != null) {
				SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
				if (user != null) {
					boolean bound = bind(ldapCompleteNameAttribute, psw) != null;
					if (!bound) {
						logger.error("Impossible to bind user " + userId + ". Username or password not valid.");
						return null;
					}

					profile = new SpagoBIUserProfile();

					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
					Date expiresAt = calendar.getTime();

					String jwtToken = JWTSsoService.userId2jwtToken(userId, expiresAt);

					profile.setUniqueIdentifier(jwtToken);
					profile.setUserId(user.getUserId());
					profile.setUserName(user.getFullName());
					profile.setOrganization(user.getCommonInfo().getOrganization());
					profile.setIsSuperadmin(user.getIsSuperadmin());
				} else {
					logger.error("UserName " + userId + " not found into database. Returning [null] as user profile");
				}

			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user profile object from database", e);
		}
		return profile;
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