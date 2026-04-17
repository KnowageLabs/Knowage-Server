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
package it.eng.knowage.security.ldap.provider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.security.ldap.config.EnhancedLdapConfig;
import it.eng.knowage.security.ldap.connector.EnhancedLdapConnector;
import it.eng.knowage.security.ldap.exceptions.LdapConnectionException;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.security.InternalSecurityServiceSupplierImpl;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

/**
 * Enhanced LDAP security provider for Knowage 9.x.
 *
 * <p>Replaces the built-in {@code LdapSecurityServiceSupplier} and
 * {@code FullLdapSecurityServiceSupplier} with a correct implementation that:
 * <ul>
 *   <li>Performs search-before-bind with SUBTREE_SCOPE (supports multi-OU Active Directory)</li>
 *   <li>Binds the service account DN without any prefix/postfix manipulation</li>
 *   <li>Optionally auto-provisions users missing from {@code SBI_USER}</li>
 *   <li>Supports optional fallback to Knowage internal authentication</li>
 * </ul>
 *
 * <p>To activate, set the Knowage configuration key
 * {@code SPAGOBI.SECURITY.PORTAL-SECURITY-CLASS.className} to the fully qualified
 * name of this class.
 *
 * @see EnhancedLdapConfig
 * @see EnhancedLdapConnector
 */
public class EnhancedLdapSecurityServiceSupplier implements ISecurityServiceSupplier {

	private static final Logger LOGGER = LogManager.getLogger(EnhancedLdapSecurityServiceSupplier.class);

	public static final int USER_JWT_TOKEN_EXPIRE_HOURS = 10;

	private final InternalSecurityServiceSupplierImpl internalProvider =
		new InternalSecurityServiceSupplierImpl();

	// -------------------------------------------------------------------------
	// ISecurityServiceSupplier
	// -------------------------------------------------------------------------

	/**
	 * Full LDAP authentication flow:
	 * <ol>
	 *   <li>Bind as service account</li>
	 *   <li>Search for the user DN (subtree)</li>
	 *   <li>Bind with the found DN + password</li>
	 *   <li>Check group membership if ACCESS_GROUP_FILTER is set</li>
	 *   <li>Provision user in Knowage DB if missing and AUTO_CREATE_USER=true</li>
	 *   <li>Build and return SpagoBIUserProfile with JWT token</li>
	 * </ol>
	 */
	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		LOGGER.debug("checkAuthentication called for userId='{}'", userId);

		EnhancedLdapConfig config;
		try {
			config = new EnhancedLdapConfig();
			config.validate();
		} catch (Exception e) {
			LOGGER.error("Failed to load Enhanced LDAP configuration", e);
			return null;
		}

		LOGGER.debug("Config loaded: {}", config);
		EnhancedLdapConnector connector = new EnhancedLdapConnector(config);

		// Step 1: Bind as service account
		InitialDirContext serviceCtx = null;
		try {
			serviceCtx = connector.bindAsServiceAccount();
		} catch (LdapConnectionException e) {
			LOGGER.error("Service account bind failed", e);
			if (config.isFallbackToInternal()) {
				LOGGER.info("Falling back to internal authentication for user '{}'", userId);
				return internalProvider.checkAuthentication(userId, psw);
			}
			return null;
		}

		try {
			// Step 2: Search for the user
			SearchResult userEntry = connector.searchUser(serviceCtx, userId);
			if (userEntry == null) {
				LOGGER.warn("User '{}' not found in LDAP directory", userId);
				if (config.isFallbackToInternal()) {
					LOGGER.info("User '{}' not in LDAP, trying internal auth", userId);
					return internalProvider.checkAuthentication(userId, psw);
				}
				return null;
			}

			String userDN = userEntry.getNameInNamespace();
			Attributes userAttributes = userEntry.getAttributes();
			LOGGER.debug("User '{}' found with DN: {}", userId, userDN);

			// Step 3: Authenticate the user (bind with found DN + password)
			boolean authenticated = connector.authenticateUser(userDN, psw);
			if (!authenticated) {
				LOGGER.warn("Authentication failed for user '{}' (invalid credentials)", userId);
				if (config.isFallbackToInternal()) {
					LOGGER.info("LDAP auth failed, trying internal auth for user '{}'", userId);
					return internalProvider.checkAuthentication(userId, psw);
				}
				return null;
			}

			LOGGER.debug("LDAP authentication successful for user '{}'", userId);

			// Step 4: Load groups from LDAP
			List<String> ldapGroups = connector.getUserGroups(userAttributes);
			LOGGER.debug("User '{}' belongs to LDAP groups: {}", userId, ldapGroups);

			// Step 4b: If ACCESS_GROUP_FILTER is set, verify at least one matching group
			String groupFilter = config.getAccessGroupFilter();
			if (groupFilter != null && !groupFilter.isEmpty()) {
				if (ldapGroups.isEmpty()) {
					LOGGER.warn("User '{}' has no groups matching ACCESS_GROUP_FILTER '{}' — access denied",
						userId, groupFilter);
					return null;
				}
				LOGGER.debug("User '{}' has {} authorized group(s)", userId, ldapGroups.size());
			}

			// Step 5: Load or provision the Knowage DB user
			SbiUser sbiUser = loadOrProvisionUser(userId, userAttributes, ldapGroups, config, connector);
			if (sbiUser == null) {
				LOGGER.warn("User '{}' not found in Knowage DB and AUTO_CREATE_USER=false", userId);
				return null;
			}

			// Step 6: Build the user profile
			SpagoBIUserProfile profile = buildProfile(sbiUser, userId, ldapGroups, config);
			LOGGER.debug("Profile built successfully for user '{}'", userId);
			return profile;

		} catch (Exception e) {
			LOGGER.error("Unexpected error during LDAP authentication for user '{}'", userId, e);
			return null;
		} finally {
			closeQuietly(serviceCtx);
		}
	}

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		return internalProvider.createUserProfile(jwtToken);
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationToken(String token) {
		return internalProvider.createUserProfile(token);
	}

	@Override
	@Deprecated
	public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
		throw new UnsupportedOperationException(
			"checkAuthenticationWithToken is not supported by EnhancedLdapSecurityServiceSupplier");
	}

	@Override
	@Deprecated
	public boolean checkAuthorization(String userId, String function) {
		throw new UnsupportedOperationException(
			"checkAuthorization is not supported by EnhancedLdapSecurityServiceSupplier");
	}

	// -------------------------------------------------------------------------
	// User provisioning
	// -------------------------------------------------------------------------

	/**
	 * Loads the Knowage {@link SbiUser} for the given userId.
	 * If the user does not exist and {@code AUTO_CREATE_USER=true}, creates it.
	 *
	 * @return the SbiUser, or null if not found and auto-create is disabled
	 */
	private SbiUser loadOrProvisionUser(String userId, Attributes ldapAttributes,
			List<String> ldapGroups, EnhancedLdapConfig config, EnhancedLdapConnector connector) {

		SbiUser user = null;
		try {
			user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
		} catch (Exception e) {
			LOGGER.error("Error loading user '{}' from Knowage DB", userId, e);
			return null;
		}

		if (user != null) {
			LOGGER.debug("User '{}' found in Knowage DB (id={})", userId, user.getId());
			return user;
		}

		// User not in DB
		if (!config.isAutoCreateUser()) {
			return null;
		}

		LOGGER.info("User '{}' not in Knowage DB, auto-provisioning...", userId);
		return provisionUser(userId, ldapAttributes, ldapGroups, config, connector);
	}

	/**
	 * Creates a new Knowage user from LDAP data.
	 */
	private SbiUser provisionUser(String userId, Attributes ldapAttributes,
			List<String> ldapGroups, EnhancedLdapConfig config, EnhancedLdapConnector connector) {

		try {
			// Resolve full name from LDAP displayName (or fallback to userId)
			String displayName = connector.getSingleAttributeValue(ldapAttributes, config.getUserDisplayNameAttr());
			if (displayName == null || displayName.isEmpty()) {
				displayName = connector.getSingleAttributeValue(ldapAttributes, "cn");
			}
			if (displayName == null || displayName.isEmpty()) {
				displayName = userId;
			}

			// Build the SbiUser object
			SbiUser newUser = new SbiUser();
			newUser.setUserId(userId);
			newUser.setFullName(displayName);
			newUser.setPassword(""); // No password: authentication is always via LDAP
			newUser.setIsSuperadmin(false);
			newUser.getCommonInfo().setOrganization(config.getDefaultTenant());

			Integer newId = DAOFactory.getSbiUserDAO().saveSbiUser(newUser);
			newUser = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
			LOGGER.info("Created Knowage user '{}' (id={}, tenant={})",
				userId, newId, config.getDefaultTenant());

			// Assign default role if configured
			assignDefaultRole(newUser, config);

			// Map LDAP attributes to SBI_USER_ATTRIBUTES
			mapLdapAttributesToProfile(newUser, ldapAttributes, connector);

			return newUser;

		} catch (Exception e) {
			LOGGER.error("Failed to auto-provision user '{}' in Knowage DB", userId, e);
			return null;
		}
	}

	/**
	 * Assigns the configured default role to the newly created user.
	 */
	private void assignDefaultRole(SbiUser user, EnhancedLdapConfig config) {
		String defaultRoleName = config.getDefaultRole();
		if (defaultRoleName == null || defaultRoleName.isEmpty()) {
			LOGGER.debug("No DEFAULT_ROLE configured, skipping role assignment for user '{}'", user.getUserId());
			return;
		}

		try {
			Role role = DAOFactory.getRoleDAO().loadByName(defaultRoleName);
			if (role == null) {
				LOGGER.warn("Default role '{}' not found in Knowage DB, skipping role assignment", defaultRoleName);
				return;
			}

			SbiExtUserRolesId roleId = new SbiExtUserRolesId(user.getId(), role.getId());
			SbiExtUserRoles userRole = new SbiExtUserRoles(roleId, user);
			userRole.getCommonInfo().setOrganization(config.getDefaultTenant());
			DAOFactory.getSbiUserDAO().updateSbiUserRoles(userRole);
			LOGGER.info("Assigned default role '{}' to user '{}'", defaultRoleName, user.getUserId());

		} catch (Exception e) {
			LOGGER.warn("Could not assign default role '{}' to user '{}': {}",
				defaultRoleName, user.getUserId(), e.getMessage());
		}
	}

	/**
	 * Maps LDAP attribute values to SBI_USER_ATTRIBUTES for attributes defined in SBI_ATTRIBUTE.
	 * Only attributes whose names exist in the SBI_ATTRIBUTE table are mapped.
	 */
	private void mapLdapAttributesToProfile(SbiUser user, Attributes ldapAttributes,
			EnhancedLdapConnector connector) {
		try {
			List<SbiAttribute> profileAttributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			if (profileAttributes == null || profileAttributes.isEmpty()) {
				return;
			}

			for (SbiAttribute attr : profileAttributes) {
				String attrName = attr.getAttributeName();
				String ldapValue = connector.getSingleAttributeValue(ldapAttributes, attrName);
				if (ldapValue == null) {
					continue;
				}

				SbiUserAttributesId attrId = new SbiUserAttributesId(user.getId(), attr.getAttributeId());
				SbiUserAttributes userAttr = new SbiUserAttributes(attrId, user, attr, ldapValue);
				userAttr.getCommonInfo().setOrganization(user.getCommonInfo().getOrganization());
				DAOFactory.getSbiUserDAO().updateSbiUserAttributes(userAttr);
				LOGGER.debug("Mapped LDAP attribute '{}' = '{}' for user '{}'",
					attrName, ldapValue, user.getUserId());
			}

		} catch (Exception e) {
			LOGGER.warn("Could not map LDAP attributes for user '{}': {}", user.getUserId(), e.getMessage());
		}
	}

	// -------------------------------------------------------------------------
	// Profile building
	// -------------------------------------------------------------------------

	/**
	 * Builds a {@link SpagoBIUserProfile} with a 10-hour JWT token.
	 *
	 * <p>Roles are resolved as follows:
	 * <ul>
	 *   <li>If {@code ROLES_SOURCE=LDAP}: use the filtered LDAP group names (intersected with Knowage roles)</li>
	 *   <li>If {@code ROLES_SOURCE=KNOWAGE}: load roles from the DB (delegates to internal logic)</li>
	 * </ul>
	 */
	private SpagoBIUserProfile buildProfile(SbiUser sbiUser, String userId,
			List<String> ldapGroups, EnhancedLdapConfig config) {

		SpagoBIUserProfile profile = new SpagoBIUserProfile();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();
		String jwtToken = JWTSsoService.userId2jwtToken(userId, expiresAt);

		profile.setUniqueIdentifier(jwtToken);
		profile.setUserId(sbiUser.getUserId());
		profile.setUserName(sbiUser.getFullName());
		profile.setOrganization(sbiUser.getCommonInfo().getOrganization());
		profile.setIsSuperadmin(sbiUser.getIsSuperadmin());

		// Roles
		String[] roles = resolveRoles(sbiUser, ldapGroups, config);
		profile.setRoles(roles);

		// Attributes from DB (includes any previously mapped LDAP attributes)
		try {
			Map<String, Object> attributes = profile.getAttributes();
			List<it.eng.spagobi.profiling.bean.SbiUserAttributes> dbAttrs =
				DAOFactory.getSbiUserDAO().loadSbiUserAttributesById(sbiUser.getId());
			if (dbAttrs != null) {
				for (it.eng.spagobi.profiling.bean.SbiUserAttributes attr : dbAttrs) {
					if (attr.getAttributeValue() != null) {
						attributes.put(attr.getSbiAttribute().getAttributeName(), attr.getAttributeValue());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Could not load profile attributes for user '{}': {}", userId, e.getMessage());
		}

		return profile;
	}

	/**
	 * Resolves the final set of roles for the profile.
	 *
	 * <p>When {@code ROLES_SOURCE=LDAP}: for each LDAP group that matches a Knowage role by name,
	 * ensures the association exists in {@code SBI_EXT_USER_ROLES} (idempotent upsert), then
	 * returns the list of matched role names.
	 * <p>When {@code ROLES_SOURCE=KNOWAGE}: loads roles directly from the DB without any sync.
	 */
	private String[] resolveRoles(SbiUser sbiUser, List<String> ldapGroups, EnhancedLdapConfig config) {
		if ("KNOWAGE".equalsIgnoreCase(config.getRolesSource())) {
			return loadRolesFromDb(sbiUser);
		}

		// Load current DB roles: roleId → roleName
		java.util.Map<Integer, String> dbRoleMap = new java.util.HashMap<>();
		try {
			List<it.eng.spagobi.commons.metadata.SbiExtRoles> current =
				DAOFactory.getSbiUserDAO().loadSbiUserRolesById(sbiUser.getId());
			if (current != null) {
				for (it.eng.spagobi.commons.metadata.SbiExtRoles r : current) {
					dbRoleMap.put(r.getExtRoleId(), r.getName());
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Could not load existing roles for user '{}': {}", sbiUser.getUserId(), e.getMessage());
		}

		// For each LDAP group: find matching Knowage role, add if missing
		List<String> resolvedRoles = new ArrayList<>();
		java.util.Set<Integer> ldapRoleIds = new java.util.HashSet<>();
		for (String groupName : ldapGroups) {
			try {
				Role role = DAOFactory.getRoleDAO().loadByName(groupName);
				if (role == null) {
					LOGGER.debug("LDAP group '{}' has no matching Knowage role, skipped", groupName);
					continue;
				}

				resolvedRoles.add(groupName);
				ldapRoleIds.add(role.getId());

				if (!dbRoleMap.containsKey(role.getId())) {
					SbiExtUserRolesId roleAssocId = new SbiExtUserRolesId(sbiUser.getId(), role.getId());
					SbiExtUserRoles roleAssoc = new SbiExtUserRoles(roleAssocId, sbiUser);
					roleAssoc.getCommonInfo().setOrganization(sbiUser.getCommonInfo().getOrganization());
					DAOFactory.getSbiUserDAO().updateSbiUserRoles(roleAssoc);
					LOGGER.info("Added role '{}' for user '{}'", groupName, sbiUser.getUserId());
				}
			} catch (Exception e) {
				LOGGER.warn("Could not sync role '{}' for user '{}': {}",
					groupName, sbiUser.getUserId(), e.getMessage());
			}
		}

		// Revoke roles that are in DB but no longer in LDAP groups
		for (java.util.Map.Entry<Integer, String> entry : dbRoleMap.entrySet()) {
			if (!ldapRoleIds.contains(entry.getKey())) {
				try {
					DAOFactory.getSbiUserDAO().deleteSbiUserRoleById(sbiUser.getId(), entry.getKey());
					LOGGER.info("Revoked role '{}' from user '{}' (no longer in LDAP groups)",
						entry.getValue(), sbiUser.getUserId());
				} catch (Exception e) {
					LOGGER.warn("Could not revoke role '{}' from user '{}': {}",
						entry.getValue(), sbiUser.getUserId(), e.getMessage());
				}
			}
		}

		return resolvedRoles.toArray(new String[0]);
	}

	private String[] loadRolesFromDb(SbiUser sbiUser) {
		try {
			List<it.eng.spagobi.commons.metadata.SbiExtRoles> dbRoles =
				DAOFactory.getSbiUserDAO().loadSbiUserRolesById(sbiUser.getId());
			List<String> roleNames = new ArrayList<>();
			for (it.eng.spagobi.commons.metadata.SbiExtRoles r : dbRoles) {
				roleNames.add(r.getName());
			}
			return roleNames.toArray(new String[0]);
		} catch (Exception e) {
			LOGGER.warn("Could not load roles from DB for user '{}': {}", sbiUser.getUserId(), e.getMessage());
			return new String[0];
		}
	}

	// -------------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------------

	private void closeQuietly(InitialDirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				LOGGER.debug("Error closing LDAP service context: {}", e.getMessage());
			}
		}
	}

}
