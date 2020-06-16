package it.eng.spagobi.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.assertion.Assert;

public class ProfiledLdapSecurityServiceSupplier extends LdapSecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(ProfiledLdapSecurityServiceSupplier.class);

	private static final String SEARCH_USER_BEFORE_PSW = "SEARCH_USER_BEFORE_PSW";
	private static final String SEARCH_USER_BEFORE_USER = "SEARCH_USER_BEFORE_USER";
	private static final String SEARCH_USER_BEFORE = "SEARCH_USER_BEFORE";
	private static final String ATTRIBUTE_USED_TO_LOGIN = "ATTRIBUTE_USED_TO_LOGIN";
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

					Hashtable<String, String> env = new Hashtable<String, String>(11);
					env.put(Context.INITIAL_CONTEXT_FACTORY, properties.getProperty("INITIAL_CONTEXT_FACTORY"));
					env.put(Context.PROVIDER_URL, properties.getProperty("PROVIDER_URL"));

					env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty("SECURITY_AUTHENTICATION"));

					env.put("javax.security.sasl.qop", "auth-conf");
					env.put("javax.security.sasl.strength", "high");

					String postfix = properties.getProperty("DN_POSTFIX");
					postfix = postfix.startsWith(",") ? postfix.substring(1) : postfix;

					String usernameForLDAPAuthBefore = new String(properties.getProperty(SEARCH_USER_BEFORE_USER));
					String passwordForLDAPAuthBefore = new String(properties.getProperty(SEARCH_USER_BEFORE_PSW));

					boolean validUsername = usernameForLDAPAuthBefore != null && !usernameForLDAPAuthBefore.isEmpty();

					if (validUsername) {
						env.put(Context.SECURITY_PRINCIPAL, String.format("%s%s,%s", properties.getProperty("DN_PREFIX"), usernameForLDAPAuthBefore, postfix));
						env.put(Context.SECURITY_CREDENTIALS, passwordForLDAPAuthBefore);
						logger.debug("Found credentials in properties file for authentication before looking for attribute.");
					}

					String[] searchAttrs = { "nsRole", "uid", "objectClass", "givenName", "gn", "sn", "cn" };
					SearchControls ctrls = new SearchControls();
					ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
					ctrls.setCountLimit(0);
					ctrls.setTimeLimit(0);
					ctrls.setReturningAttributes(searchAttrs);

					InitialDirContext ctx = null;
					try {
						ctx = new InitialDirContext(env);
						logger.debug("Authentication successfull.");
					} catch (Exception e) {
						logger.error("Authentication NOT successfull. Reason: ", e);
						return null;
					}

					String filter = String.format(properties.getProperty(SEARCH_USER_BEFORE_FILTER), userId);

					// Search for user's email given matching user ID.
					String ldapCompleteNameAttribute = null;

					try {
						NamingEnumeration<SearchResult> answer = ctx.search(postfix, filter, ctrls);

						while (answer.hasMore()) {
							SearchResult ldapResult = answer.next();
							Attributes attrs = ldapResult.getAttributes();
							Attribute completeNameAttribute = attrs.get(properties.getProperty(ATTRIBUTE_USED_TO_LOGIN));
							String[] strAttribute = completeNameAttribute.toString().split(",");
							boolean found = false;
							for (String string : strAttribute) {
								if (string.contains(" - ")) {
									ldapCompleteNameAttribute = string.trim();
									found = true;
									break;
								}
							}
							if (found)
								break;
						}
						ctx.close();
					} catch (NamingException e) {
						logger.error("Not found resource with given filters", e);
						return null;
					}

					return super.checkAuthentication(ldapCompleteNameAttribute, psw);
				} else {
					return super.checkAuthentication(userId, psw);
				}
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