/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.security.ldap.connector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.security.ldap.config.EnhancedLdapConfig;
import it.eng.knowage.security.ldap.exceptions.LdapAuthenticationException;
import it.eng.knowage.security.ldap.exceptions.LdapConnectionException;
import it.eng.knowage.security.ldap.exceptions.LdapSearchException;

/**
 * Low-level JNDI connector for LDAP/Active Directory.
 *
 * <p>Each public method is stateless: contexts are created and closed within the method,
 * except {@link #bindAsServiceAccount()} which returns a context that the caller must close.
 *
 * <p>This class intentionally uses only {@code javax.naming} — no external LDAP libraries.
 */
public class EnhancedLdapConnector {

	private static final Logger LOGGER = LogManager.getLogger(EnhancedLdapConnector.class);

	private static final String JNDI_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String CONNECT_TIMEOUT_MS = "5000";

	private final EnhancedLdapConfig config;

	public EnhancedLdapConnector(EnhancedLdapConfig config) {
		this.config = config;
	}

	// -------------------------------------------------------------------------
	// Service account bind
	// -------------------------------------------------------------------------

	/**
	 * Binds to the LDAP server using the service account DN and password exactly as
	 * configured — no prefix/postfix manipulation.
	 *
	 * <p>The caller is responsible for closing the returned context.
	 *
	 * @return an open {@link InitialDirContext} bound as the service account
	 * @throws LdapConnectionException if the bind fails
	 */
	public InitialDirContext bindAsServiceAccount() {
		LOGGER.debug("Binding service account: {}", config.getAdminUser());
		try {
			InitialDirContext ctx = createContext(config.getAdminUser(), config.getAdminPassword());
			LOGGER.debug("Service account bind successful");
			return ctx;
		} catch (AuthenticationException e) {
			throw new LdapConnectionException(
				"Service account bind failed for DN '" + config.getAdminUser() + "': " + e.getMessage(), e);
		} catch (NamingException e) {
			throw new LdapConnectionException(
				"Cannot connect to LDAP server at " + config.getLdapUrl() + ": " + e.getMessage(), e);
		}
	}

	// -------------------------------------------------------------------------
	// User search
	// -------------------------------------------------------------------------

	/**
	 * Searches for a user entry using SUBTREE_SCOPE from {@code USER_SEARCH_PATH} (or {@code BASE_DN}).
	 *
	 * <p>If multiple entries match, a WARNING is logged and the first result is returned
	 * (prefer warning over exception for multi-valued results).
	 *
	 * @param serviceCtx an open context bound as the service account
	 * @param userId     the login identifier (value of USER_ID_ATTRIBUTE)
	 * @return the matching {@link SearchResult}, or {@code null} if not found
	 * @throws LdapSearchException if the search operation itself fails
	 */
	public SearchResult searchUser(DirContext serviceCtx, String userId) {
		String searchBase = config.getEffectiveUserSearchBase();
		String filter = buildUserSearchFilter(userId);
		LOGGER.debug("Searching user '{}' in '{}' with filter '{}'", userId, searchBase, filter);

		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setCountLimit(0);
		controls.setTimeLimit(0);
		// Explicitly request all standard attributes ("*") plus memberOf which is a
		// linked/operational attribute in Active Directory and may not be returned by default.
		controls.setReturningAttributes(new String[]{"*", config.getUserMemberOfAttribute()});

		NamingEnumeration<SearchResult> results = null;
		try {
			results = serviceCtx.search(searchBase, filter, controls);

			if (!results.hasMore()) {
				LOGGER.debug("User '{}' not found in LDAP", userId);
				return null;
			}

			SearchResult firstResult = results.next();
			LOGGER.debug("User '{}' found: {}", userId, firstResult.getNameInNamespace());

			if (results.hasMore()) {
				LOGGER.warn("Multiple LDAP entries found for userId '{}'. Using first result: {}",
					userId, firstResult.getNameInNamespace());
			}

			return firstResult;

		} catch (NamingException e) {
			throw new LdapSearchException(
				"LDAP search failed for user '" + userId + "' (base='" + searchBase + "', filter='" + filter + "')", e);
		} finally {
			closeQuietly(results);
		}
	}

	private String buildUserSearchFilter(String userId) {
		String customFilter = config.getUserSearchFilter();
		if (customFilter != null && !customFilter.isEmpty()) {
			return customFilter.replace("{0}", escapeLdapFilter(userId));
		}
		return "(&(objectClass=" + config.getUserObjectClass()
			+ ")(" + config.getUserIdAttribute() + "=" + escapeLdapFilter(userId) + "))";
	}

	// -------------------------------------------------------------------------
	// User authentication (bind with user DN + password)
	// -------------------------------------------------------------------------

	/**
	 * Attempts to bind to the LDAP server with the given DN and password.
	 *
	 * @param userDN   the full distinguished name found by {@link #searchUser}
	 * @param password the user's password
	 * @return {@code true} if the bind succeeds, {@code false} if credentials are invalid
	 * @throws LdapConnectionException if the server is unreachable
	 */
	public boolean authenticateUser(String userDN, String password) {
		LOGGER.debug("Authenticating user with DN: {}", userDN);
		InitialDirContext ctx = null;
		try {
			ctx = createContext(userDN, password);
			LOGGER.debug("Authentication successful for DN: {}", userDN);
			return true;
		} catch (AuthenticationException e) {
			LOGGER.debug("Authentication failed (invalid credentials) for DN: {}", userDN);
			return false;
		} catch (NamingException e) {
			throw new LdapConnectionException(
				"LDAP server error during user bind for DN '" + userDN + "': " + e.getMessage(), e);
		} finally {
			closeQuietly(ctx);
		}
	}

	// -------------------------------------------------------------------------
	// Groups
	// -------------------------------------------------------------------------

	/**
	 * Extracts the user's groups from their LDAP attributes (via the {@code memberOf} attribute
	 * or the configured {@code USER_MEMBEROF_ATTRIBUTE}).
	 *
	 * <p>Each group DN is parsed to extract the CN value. If {@code ACCESS_GROUP_FILTER} is
	 * configured, only group names matching the regex pattern are returned.
	 *
	 * @param userAttributes the attributes from the user's {@link SearchResult}
	 * @return list of group name strings (CN part of the group DN), possibly empty
	 */
	public List<String> getUserGroups(Attributes userAttributes) {
		List<String> groups = new ArrayList<>();
		String memberOfAttr = config.getUserMemberOfAttribute();
		String groupFilter  = config.getAccessGroupFilter();

		LOGGER.debug("Reading '{}' attribute to get user groups", memberOfAttr);

		try {
			Attribute memberOf = userAttributes.get(memberOfAttr);
			if (memberOf == null) {
				LOGGER.debug("Attribute '{}' not present on user entry", memberOfAttr);
				return groups;
			}

			NamingEnumeration<?> values = memberOf.getAll();
			while (values.hasMore()) {
				Object raw = values.next();
				String groupDn = (raw instanceof byte[])
					? new String((byte[]) raw, StandardCharsets.UTF_8)
					: (String) raw;
				String groupName = extractCnFromDn(groupDn);
				if (groupName == null) {
					LOGGER.debug("Could not extract CN from group DN: {}", groupDn);
					continue;
				}

				if (groupFilter != null && !groupFilter.isEmpty()) {
					if (!groupName.matches(groupFilter)) {
						LOGGER.debug("Group '{}' excluded by ACCESS_GROUP_FILTER '{}'", groupName, groupFilter);
						continue;
					}
				}

				LOGGER.debug("Adding group: {}", groupName);
				groups.add(groupName);
			}

		} catch (NamingException e) {
			LOGGER.warn("Error reading memberOf attribute: {}", e.getMessage());
		}

		LOGGER.debug("User belongs to {} group(s): {}", groups.size(), groups);
		return groups;
	}

	/**
	 * Extracts the CN value from a group DN string.
	 * Example: {@code CN=SO_BO_ADMIN,OU=Groupes_Knowage,DC=example,DC=com} → {@code SO_BO_ADMIN}
	 */
	private String extractCnFromDn(String dn) {
		if (dn == null || dn.isEmpty()) {
			return null;
		}
		// Find first unescaped comma per RFC 4514 (backslash escapes the next char)
		int end = dn.length();
		for (int i = 0; i < dn.length(); i++) {
			char c = dn.charAt(i);
			if (c == '\\') {
				i++; // skip escaped character
			} else if (c == ',') {
				end = i;
				break;
			}
		}
		String firstRdn = dn.substring(0, end);
		int eq = firstRdn.indexOf('=');
		if (eq < 0) {
			return null;
		}
		return firstRdn.substring(eq + 1).trim();
	}

	// -------------------------------------------------------------------------
	// Profile attributes
	// -------------------------------------------------------------------------

	/**
	 * Extracts relevant profile attributes from the user's LDAP entry.
	 *
	 * @param userAttributes the attributes from the user's {@link SearchResult}
	 * @return map of attribute name → value (only attributes with non-null values)
	 */
	public Map<String, String> getUserAttributes(Attributes userAttributes) {
		Map<String, String> result = new HashMap<>();
		String[] attrNames = {
			"mail", "displayName", "givenName", "sn", "cn",
			"telephoneNumber", "department", "title", "company"
		};

		for (String attrName : attrNames) {
			String value = getSingleAttributeValue(userAttributes, attrName);
			if (value != null) {
				result.put(attrName, value);
			}
		}

		LOGGER.debug("Extracted {} LDAP profile attributes", result.size());
		return result;
	}

	/**
	 * Returns the string value of a single-valued LDAP attribute, or {@code null} if absent.
	 */
	public String getSingleAttributeValue(Attributes attrs, String attrName) {
		try {
			Attribute attr = attrs.get(attrName);
			if (attr != null && attr.size() > 0) {
				Object value = attr.get(0);
				if (value == null) return null;
				return (value instanceof byte[])
					? new String((byte[]) value, StandardCharsets.UTF_8)
					: value.toString();
			}
		} catch (NamingException e) {
			LOGGER.debug("Could not read attribute '{}': {}", attrName, e.getMessage());
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// JNDI context factory
	// -------------------------------------------------------------------------

	private InitialDirContext createContext(String principal, String credentials) throws NamingException {
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
		env.put(Context.PROVIDER_URL, config.getLdapUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, principal);
		env.put(Context.SECURITY_CREDENTIALS, credentials);
		env.put("com.sun.jndi.ldap.connect.timeout", CONNECT_TIMEOUT_MS);
		env.put(Context.REFERRAL, config.getLdapReferral());
		if (config.isUseSsl()) {
			env.put(Context.SECURITY_PROTOCOL, "ssl");
		}
		return new InitialDirContext(env);
	}

	// -------------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------------

	/**
	 * Escapes special characters in an LDAP search filter value per RFC 4515.
	 */
	private String escapeLdapFilter(String value) {
		if (value == null) {
			return "";
		}
		return value
			.replace("\\", "\\5c")
			.replace("*",  "\\2a")
			.replace("(",  "\\28")
			.replace(")",  "\\29")
			.replace("\0", "\\00");
	}

	private void closeQuietly(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				LOGGER.debug("Error closing LDAP context: {}", e.getMessage());
			}
		}
	}

	private void closeQuietly(NamingEnumeration<?> en) {
		if (en != null) {
			try {
				en.close();
			} catch (NamingException e) {
				LOGGER.debug("Error closing LDAP enumeration: {}", e.getMessage());
			}
		}
	}

}
