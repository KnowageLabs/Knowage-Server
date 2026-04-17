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
package it.eng.knowage.security.ldap.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.security.ldap.exceptions.LdapConnectionException;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.security.EncryptionPBEWithMD5AndDES;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Loads and exposes the Enhanced LDAP provider configuration.
 *
 * <p>Configuration is loaded in priority order:
 * <ol>
 *   <li>Table {@code SBI_CONFIG} via {@code DAOFactory}, using keys prefixed with {@code KNOWAGE_LDAP.}</li>
 *   <li>Properties file pointed to by the system property {@code enhanced.ldap.config}</li>
 * </ol>
 *
 * <p>The admin password supports transparent encryption: if the stored value starts with
 * {@code ENC(} and ends with {@code )}, it is decrypted using {@link EncryptionPBEWithMD5AndDES}.
 */
public class EnhancedLdapConfig {

	private static final Logger LOGGER = LogManager.getLogger(EnhancedLdapConfig.class);

	public static final String SYSTEM_PROPERTY = "enhanced.ldap.config";
	private static final String SBI_CONFIG_PREFIX = "KNOWAGE_LDAP.";

	// Config keys (without prefix)
	public static final String KEY_HOST                   = "HOST";
	public static final String KEY_PORT                   = "PORT";
	public static final String KEY_USE_SSL                = "USE_SSL";
	public static final String KEY_BASE_DN                = "BASE_DN";
	public static final String KEY_ADMIN_USER             = "ADMIN_USER";
	public static final String KEY_ADMIN_PSW              = "ADMIN_PSW";
	public static final String KEY_USER_SEARCH_PATH       = "USER_SEARCH_PATH";
	public static final String KEY_USER_OBJECT_CLASS      = "USER_OBJECT_CLASS";
	public static final String KEY_USER_ID_ATTRIBUTE      = "USER_ID_ATTRIBUTE";
	public static final String KEY_USER_SEARCH_FILTER     = "USER_SEARCH_FILTER";
	public static final String KEY_USER_DISPLAYNAME_ATTR  = "USER_DISPLAYNAME_ATTR";
	public static final String KEY_ROLES_SOURCE           = "ROLES_SOURCE";
	public static final String KEY_USER_MEMBEROF_ATTRIBUTE= "USER_MEMBEROF_ATTRIBUTE";
	public static final String KEY_GROUP_SEARCH_PATH      = "GROUP_SEARCH_PATH";
	public static final String KEY_GROUP_OBJECT_CLASS     = "GROUP_OBJECT_CLASS";
	public static final String KEY_GROUP_ID_ATTRIBUTE     = "GROUP_ID_ATTRIBUTE";
	public static final String KEY_ACCESS_GROUP_FILTER    = "ACCESS_GROUP_FILTER";
	public static final String KEY_AUTO_CREATE_USER       = "AUTO_CREATE_USER";
	public static final String KEY_DEFAULT_ROLE           = "DEFAULT_ROLE";
	public static final String KEY_DEFAULT_TENANT         = "DEFAULT_TENANT";
	public static final String KEY_FALLBACK_TO_INTERNAL   = "FALLBACK_TO_INTERNAL";
	public static final String KEY_LDAP_REFERRAL          = "LDAP_REFERRAL";

	// Loaded values
	private String host;
	private int port;
	private boolean useSsl;
	private String baseDn;
	private String adminUser;
	private String adminPassword;
	private String userSearchPath;
	private String userObjectClass;
	private String userIdAttribute;
	private String userSearchFilter;
	private String userDisplayNameAttr;
	private String rolesSource;
	private String userMemberOfAttribute;
	private String groupSearchPath;
	private String groupObjectClass;
	private String groupIdAttribute;
	private String accessGroupFilter;
	private boolean autoCreateUser;
	private String defaultRole;
	private String defaultTenant;
	private boolean fallbackToInternal;
	private String ldapReferral;

	public EnhancedLdapConfig() {
		Properties props = loadProperties();
		parseProperties(props);
	}

	/** Test-only factory — bypasses DB and file loading. */
	public static EnhancedLdapConfig fromProperties(Properties props) {
		EnhancedLdapConfig cfg = new EnhancedLdapConfig(props);
		return cfg;
	}

	private EnhancedLdapConfig(Properties props) {
		parseProperties(props);
	}

	// -------------------------------------------------------------------------
	// Loading
	// -------------------------------------------------------------------------

	private Properties loadProperties() {
		Properties props = tryLoadFromDatabase();
		if (props != null && !props.isEmpty()) {
			LOGGER.debug("Enhanced LDAP config loaded from SBI_CONFIG");
			return props;
		}
		LOGGER.debug("SBI_CONFIG not available or empty, falling back to properties file");
		return loadFromFile();
	}

	/**
	 * Tries to load all {@code KNOWAGE_LDAP.*} entries from the {@code SBI_CONFIG} table.
	 * Returns null (not empty) if the DAO is unavailable (e.g. outside container context).
	 */
	private Properties tryLoadFromDatabase() {
		try {
			List<Config> configs = DAOFactory.getSbiConfigDAO().loadConfigParametersByProperties(SBI_CONFIG_PREFIX);
			if (configs == null || configs.isEmpty()) {
				return null;
			}
			Properties props = new Properties();
			for (Config cfg : configs) {
				String label = cfg.getLabel();
				String value = cfg.getValueCheck();
				if (label != null && label.startsWith(SBI_CONFIG_PREFIX)) {
					String key = label.substring(SBI_CONFIG_PREFIX.length());
					if (value != null) {
						props.setProperty(key, value);
					}
				}
			}
			return props.isEmpty() ? null : props;
		} catch (Exception e) {
			LOGGER.debug("Could not load LDAP config from SBI_CONFIG: {}", e.getMessage());
			return null;
		}
	}

	private Properties loadFromFile() {
		String path = System.getProperty(SYSTEM_PROPERTY);
		if (path == null || path.isEmpty()) {
			throw new LdapConnectionException(
				"Enhanced LDAP config not found: SBI_CONFIG has no KNOWAGE_LDAP.* entries and "
				+ "system property '" + SYSTEM_PROPERTY + "' is not set.");
		}
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(path)) {
			props.load(fis);
			LOGGER.debug("Enhanced LDAP config loaded from file: {}", path);
			return props;
		} catch (IOException e) {
			throw new SpagoBIRuntimeException(
				"Cannot read Enhanced LDAP config file: " + path, e);
		}
	}

	// -------------------------------------------------------------------------
	// Parsing
	// -------------------------------------------------------------------------

	private void parseProperties(Properties p) {
		host                = require(p, KEY_HOST);
		port                = parseInt(p, KEY_PORT, 389);
		useSsl              = parseBoolean(p, KEY_USE_SSL, false);
		baseDn              = require(p, KEY_BASE_DN);
		adminUser           = require(p, KEY_ADMIN_USER);
		adminPassword       = decryptIfNeeded(require(p, KEY_ADMIN_PSW));
		userSearchPath      = p.getProperty(KEY_USER_SEARCH_PATH, "");
		userObjectClass     = p.getProperty(KEY_USER_OBJECT_CLASS, "person");
		userIdAttribute     = p.getProperty(KEY_USER_ID_ATTRIBUTE, "sAMAccountName");
		userSearchFilter    = p.getProperty(KEY_USER_SEARCH_FILTER, "");
		userDisplayNameAttr = p.getProperty(KEY_USER_DISPLAYNAME_ATTR, "displayName");
		rolesSource         = p.getProperty(KEY_ROLES_SOURCE, "LDAP");
		userMemberOfAttribute = p.getProperty(KEY_USER_MEMBEROF_ATTRIBUTE, "memberOf");
		groupSearchPath     = p.getProperty(KEY_GROUP_SEARCH_PATH, "");
		groupObjectClass    = p.getProperty(KEY_GROUP_OBJECT_CLASS, "group");
		groupIdAttribute    = p.getProperty(KEY_GROUP_ID_ATTRIBUTE, "cn");
		accessGroupFilter   = p.getProperty(KEY_ACCESS_GROUP_FILTER, "");
		autoCreateUser      = parseBoolean(p, KEY_AUTO_CREATE_USER, true);
		defaultRole         = p.getProperty(KEY_DEFAULT_ROLE, "");
		defaultTenant       = p.getProperty(KEY_DEFAULT_TENANT, "DEFAULT_TENANT");
		fallbackToInternal  = parseBoolean(p, KEY_FALLBACK_TO_INTERNAL, true);
		ldapReferral        = parseReferral(p);
	}

	private String require(Properties p, String key) {
		String value = p.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			throw new LdapConnectionException(
				"Required LDAP configuration parameter missing: " + SBI_CONFIG_PREFIX + key);
		}
		return value.trim();
	}

	private int parseInt(Properties p, String key, int defaultValue) {
		String value = p.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid integer value for LDAP config key '{}': '{}', using default {}",
				key, value, defaultValue);
			return defaultValue;
		}
	}

	private boolean parseBoolean(Properties p, String key, boolean defaultValue) {
		String value = p.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value.trim());
	}

	private String parseReferral(Properties p) {
		String value = p.getProperty(KEY_LDAP_REFERRAL, "ignore").trim().toLowerCase();
		if (!value.equals("ignore") && !value.equals("follow") && !value.equals("throw")) {
			LOGGER.warn("Invalid LDAP_REFERRAL value '{}', defaulting to 'ignore'", value);
			return "ignore";
		}
		return value;
	}

	/**
	 * Decrypts a password if it is wrapped in ENC(...).
	 * Otherwise returns the value as-is (plain text).
	 */
	private String decryptIfNeeded(String value) {
		if (value.startsWith("ENC(") && value.endsWith(")")) {
			String encrypted = value.substring(4, value.length() - 1);
			try {
				return EncryptionPBEWithMD5AndDES.getInstance().decrypt(encrypted);
			} catch (Exception e) {
				throw new LdapConnectionException(
					"Failed to decrypt LDAP admin password. "
					+ "Ensure the system property 'symmetric_encryption_key' is set.", e);
			}
		}
		return value;
	}

	// -------------------------------------------------------------------------
	// Validation
	// -------------------------------------------------------------------------

	/**
	 * Validates that all required parameters are present and coherent.
	 * Throws {@link LdapConnectionException} if validation fails.
	 */
	public void validate() {
		// host and baseDn are already checked by require() at construction time
		if (port <= 0 || port > 65535) {
			throw new LdapConnectionException("Invalid LDAP port: " + port);
		}
		LOGGER.debug("Enhanced LDAP config validation passed");
	}

	// -------------------------------------------------------------------------
	// Derived helpers
	// -------------------------------------------------------------------------

	/** Returns the LDAP provider URL, e.g. {@code ldap://host:389}. */
	public String getLdapUrl() {
		String scheme = useSsl ? "ldaps" : "ldap";
		return scheme + "://" + host + ":" + port;
	}

	/**
	 * Returns the effective user search base DN.
	 * If {@code USER_SEARCH_PATH} is set, uses it as-is.
	 * Otherwise falls back to {@code BASE_DN}.
	 */
	public String getEffectiveUserSearchBase() {
		if (userSearchPath != null && !userSearchPath.isEmpty()) {
			return userSearchPath;
		}
		return baseDn;
	}

	/**
	 * Returns the effective group search base DN.
	 * If {@code GROUP_SEARCH_PATH} is set, uses it as-is.
	 * Otherwise falls back to {@code BASE_DN}.
	 */
	public String getEffectiveGroupSearchBase() {
		if (groupSearchPath != null && !groupSearchPath.isEmpty()) {
			return groupSearchPath;
		}
		return baseDn;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	public String getHost() { return host; }
	public int getPort() { return port; }
	public boolean isUseSsl() { return useSsl; }
	public String getBaseDn() { return baseDn; }
	public String getAdminUser() { return adminUser; }
	public String getAdminPassword() { return adminPassword; }
	public String getUserSearchPath() { return userSearchPath; }
	public String getUserObjectClass() { return userObjectClass; }
	public String getUserIdAttribute() { return userIdAttribute; }
	public String getUserSearchFilter() { return userSearchFilter; }
	public String getUserDisplayNameAttr() { return userDisplayNameAttr; }
	public String getRolesSource() { return rolesSource; }
	public String getUserMemberOfAttribute() { return userMemberOfAttribute; }
	public String getGroupSearchPath() { return groupSearchPath; }
	public String getGroupObjectClass() { return groupObjectClass; }
	public String getGroupIdAttribute() { return groupIdAttribute; }
	public String getAccessGroupFilter() { return accessGroupFilter; }
	public boolean isAutoCreateUser() { return autoCreateUser; }
	public String getDefaultRole() { return defaultRole; }
	public String getDefaultTenant() { return defaultTenant; }
	public boolean isFallbackToInternal() { return fallbackToInternal; }
	public String getLdapReferral() { return ldapReferral; }

	@Override
	public String toString() {
		return "EnhancedLdapConfig{"
			+ "url='" + getLdapUrl() + "'"
			+ ", baseDn='" + baseDn + "'"
			+ ", adminUser='" + adminUser + "'"
			+ ", adminPassword='[MASKED]'"
			+ ", userIdAttribute='" + userIdAttribute + "'"
			+ ", userSearchPath='" + userSearchPath + "'"
			+ ", accessGroupFilter='" + accessGroupFilter + "'"
			+ ", autoCreateUser=" + autoCreateUser
			+ ", fallbackToInternal=" + fallbackToInternal
			+ "}";
	}

}
