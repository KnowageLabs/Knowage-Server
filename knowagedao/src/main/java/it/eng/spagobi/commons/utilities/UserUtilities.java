/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.commons.utilities;

import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_WIDGET_USE;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MAP_WIDGET_USE;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.UserFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.AccessibilityPreferences;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.dao.exception.DAORuntimeException;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.profiling.bean.SbiAccessibilityPreferences;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.UserProfileCache;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class UserUtilities {

	private static final Logger LOGGER = Logger.getLogger(UserUtilities.class);

	public static String getSchema(String ente, IEngUserProfile userProfile) {
		LOGGER.debug("Ente: " + ente);
		if (userProfile != null) {
			try {
				return (String) userProfile.getUserAttribute(ente);
			} catch (EMFInternalError e) {
				LOGGER.error("User profile is NULL!!!!");
			}
		} else {
			LOGGER.warn("User profile is NULL!!!!");
		}
		return null;
	}

	public static IEngUserProfile getUserProfile(HttpServletRequest req) throws Exception {
		LOGGER.debug("IN");
		try {
			SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
			String userId = userProxy.readUserIdentifier(req);
			return UserUtilities.getUserProfile(userId);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	public static String fromUserProfile2JSON(UserProfile profile) {
		ObjectMapper mapper = new ObjectMapper();
		String toReturn;
		try {
			toReturn = mapper.writeValueAsString(profile);
		} catch (JsonProcessingException e) {
			throw new SpagoBIRuntimeException("Error while serializing profile into json object", e);
		}
		return toReturn;
	}

	public static UserProfile fromJSON2UserProfile(String json) {
		ObjectMapper mapper = new ObjectMapper();
		UserProfile profile;
		try {
			profile = mapper.readValue(json, UserProfile.class);
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Error while deserializing profile from json object", e);
		}
		return profile;
	}

	public static IEngUserProfile getUserProfile(String userId) throws Exception {

		Monitor getUserProfileMonitor = MonitorFactory.start("KnowageDAO.UserUtilities.getUserProfile");

		LOGGER.debug("IN.userId=" + userId);
		CacheInterface cache = UserProfileCache.getCache();
		// Search UserProfile in cache
		if (cache.contains(userId)) {
			UserProfile cachedUserProfile = (UserProfile) cache.get(userId);
			getUserProfileMonitor.stop();
			return cachedUserProfile;
		} else {
			if (userId == null) {
				return null;
			}

			try {
				UserProfile profile;
				if (UserProfile.isSchedulerUser(userId)) {
					LOGGER.debug("User [" + userId + "] has been recognized as a scheduler user.");
					profile = UserProfile.createSchedulerUserProfile(userId);
				} else if (UserProfile.isDataPreparationUser(userId)) {
					LOGGER.debug("User [" + userId + "] has been recognized as a data preparation user.");
					profile = UserProfile.createDataPreparationUserProfile(userId);
				} else if (PublicProfile.isPublicUser(userId)) {
					LOGGER.debug("User [" + userId + "] has been recognized as a public user.");
					String decodedUserId = JWTSsoService.jwtToken2userId(userId);
					SpagoBIUserProfile user = PublicProfile.createPublicUserProfile(decodedUserId);
					profile = new UserProfile(user);
				} else {
					ISecurityServiceSupplier supplier = createISecurityServiceSupplier();
					SpagoBIUserProfile user = supplier.createUserProfile(userId);
					if (user == null) {
						return null;
					}

					checkTenant(user);

					if (userHasNoRoles(user)) {
						setDefaultRole(user);
					}

					if (importUsersIsEnabled() && userIsNotInInternalMetadata(user)) {
						importUser(user);
					}

					user.setFunctions(readFunctionality(user));

					profile = new UserProfile(user);

				}

				if (profile != null) {
					// putting locale language and country on user attributes
					Locale defaultLocale = GeneralUtilities.getDefaultLocale();
					profile.addAttributes(SpagoBIConstants.LANGUAGE, defaultLocale.getLanguage());
					profile.addAttributes(SpagoBIConstants.COUNTRY, defaultLocale.getCountry());

					// put profile in cache
					cache.put(userId, profile);
				}
				LOGGER.debug("profile from get profile" + profile);

				return profile;

			} catch (Exception e) {
				LOGGER.error("Exception while creating user profile", e);
				throw new SecurityException("Exception while creating user profile", e);
			} finally {
				LOGGER.debug("OUT");
				getUserProfileMonitor.stop();
			}
		}

	}

	private static boolean userIsNotInInternalMetadata(SpagoBIUserProfile user) {
		SbiUser internalUser = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(user.getUserId());
		return internalUser == null;
	}

	private static void importUser(SpagoBIUserProfile user) throws Exception {
		LOGGER.debug("IN - importUser");

		SbiUser newUser = fromSpagoBIUserProfile2SbiUser(user);
		ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
		userDAO.setTenant(user.getOrganization());
		userDAO.fullSaveOrUpdateSbiUser(newUser);
	}

	protected static SbiUser fromSpagoBIUserProfile2SbiUser(SpagoBIUserProfile user) throws Exception {
		LOGGER.debug("IN - fromSpagoBIUserProfile2SbiUser");

		SbiUser newUser = new SbiUser();
		newUser.setUserId(user.getUserId());
		newUser.setFullName(user.getUserName());
		newUser.setIsSuperadmin(user.getIsSuperadmin());
		newUser.getCommonInfo().setOrganization(user.getOrganization());

		LOGGER.debug("Set userId " + newUser.getUserId() +
						" - fullname " + newUser.getFullName() +
						" - superAdmin " + newUser.getFullName() +
						" - commonInfo " + newUser.getCommonInfo());

		List<String> roleNamesList = Arrays.asList(user.getRoles());

		IRoleDAO roleDAO = DAOFactory.getRoleDAO();
		roleDAO.setTenant(user.getOrganization());

		// @formatter:off
		List<SbiExtRoles> rolesList = roleNamesList.stream()
				.map(roleName -> {
					Role role = null;
					try {
						role = roleDAO.loadByName(roleName);
					} catch (Exception e) {
						throw new SpagoBIRuntimeException("An error occurred while loading role with name [" + roleName + "]", e);
					}
					return role != null ? new SbiExtRoles(role.getId()) : null;
				})
				.filter(x -> x != null)  // to filter roles there were not found into database
				.collect(Collectors.toList());
		// @formatter:on
		LOGGER.debug("Filtered roles for user " + user.getUserName());


		newUser.setSbiExtUserRoleses(new HashSet<>(rolesList));

		Map<String, Object> map = user.getAttributes();
		String email = (String) map.get("email");
		LOGGER.debug("email " + email);
		if (email != null && !email.equals("")) {
			Set<SbiUserAttributes> attributes = new HashSet<>();
			ISbiAttributeDAO attrDao = DAOFactory.getSbiAttributeDAO();
			attrDao.setTenant(user.getOrganization());

			int idAttributeMail = attrDao.loadSbiAttributeByName("email").getAttributeId();
			LOGGER.debug("idAttributeMail " + idAttributeMail);
			addAttribute(attributes, idAttributeMail, email, user.getOrganization());
			LOGGER.debug("Attributes: " + attributes);
			newUser.setSbiUserAttributeses(attributes);

		}

		return newUser;
	}

	private static void addAttribute(Set<SbiUserAttributes> attributes, int attrId, String attrValue, String tenant) {

		if (attrValue != null) {
			SbiUserAttributes a = new SbiUserAttributes();
			a.getCommonInfo().setOrganization(tenant);
			SbiUserAttributesId id = new SbiUserAttributesId(attrId);
			a.setId(id);
			a.setAttributeValue(attrValue);
			attributes.add(a);
		}
	}

	private static boolean importUsersIsEnabled() {
		String importUserIsEnabled = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.INTERNAL_SECURITY_LOGIN_IMPORT_USER_IF_NOT_EXISTING);
		return "true".equalsIgnoreCase(importUserIsEnabled);
	}

	private static boolean userHasNoRoles(SpagoBIUserProfile user) {
		return ArrayUtils.isEmpty(user.getRoles());
	}

	/**
	 * This method sets the default role, in case it is defined
	 *
	 * @param user the user profile object to be initialized with the default role
	 */
	private static void setDefaultRole(SpagoBIUserProfile user) {
		String defaultRole = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.INTERNAL_SECURITY_USERS_DEFAULT_ROLE);
		if (ArrayUtils.isEmpty(user.getRoles()) && StringUtils.isNotEmpty(defaultRole)) {
			LOGGER.debug("User profile object has no roles, setting the default one that is [" + defaultRole + "]");
			user.setRoles(new String[] { defaultRole });
		}
	}

	public static boolean isTechnicalUser(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN) // for administrators
					|| profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV) // for developers
					|| profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_TEST) // for testers
					|| profile.isAbleToExecuteAction(CommunityFunctionalityConstants.PARAMETER_MANAGEMENT)) { // for behavioural model administrators
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean isTechDsManager(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN) // for administrators
					|| profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV)) { // for developers
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean isAdministrator(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean hasDeveloperRole(IEngUserProfile profile) {
		return hasRoleType(profile, SpagoBIConstants.ROLE_TYPE_DEV);
	}

	public static boolean hasAdministratorRole(IEngUserProfile profile) {
		return hasRoleType(profile, SpagoBIConstants.ADMIN_ROLE_TYPE);
	}

	public static boolean hasUserRole(IEngUserProfile profile) {
		return hasRoleType(profile, SpagoBIConstants.ROLE_TYPE_USER);
	}

	public static boolean hasTesterRole(IEngUserProfile profile) {
		return hasRoleType(profile, SpagoBIConstants.ROLE_TYPE_TEST);
	}

	public static boolean hasModelAdminRole(IEngUserProfile profile) {
		return hasRoleType(profile, SpagoBIConstants.ROLE_TYPE_MODEL_ADMIN);
	}

	private static boolean hasRoleType(IEngUserProfile profile, String roleType) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				String roleCode = r.getRoleTypeCD();
				if (roleCode.equalsIgnoreCase(roleType)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean isTester(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_TEST)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean haveRoleAndAuthorization(IEngUserProfile profile, String role, String[] authorization) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		ArrayList<String> auth = new ArrayList<>(Arrays.asList(authorization));
		try {
			if (((UserProfile) profile).getIsSuperadmin()) {
				return true;
			}
			boolean result = false;
			for (int i = 0; i < profile.getRoles().size(); i++) {
				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				roleDAO.setTenant(((UserProfile) profile).getOrganization());

				Role rol = roleDAO.loadByName(((ArrayList<?>) profile.getRoles()).get(i).toString());
				if (role == null || rol.getRoleTypeCD().compareTo(role) == 0) {

					// check for authorization
					if (auth == null || auth.isEmpty()) {
						result = true;
						break;
					} else {
						Boolean ok = true;
						for (String au : auth) {
							if (!profile.isAbleToExecuteAction(au)) {
								ok = false;
								break;
							}
						}

						if (ok) {
							result = ok;
							break;
						}
					}
				}
			}

			return result;

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	/**
	 * User functionality root exists.
	 *
	 * @param username the username
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean userFunctionalityRootExists(String username) throws Exception {
		boolean exists = false;
		try {
			LOGGER.debug("****  username checked: " + username);
			ILowFunctionalityDAO functdao = DAOFactory.getLowFunctionalityDAO();
			exists = functdao.checkUserRootExists(username);
		} catch (Exception e) {
			LOGGER.error("Error while checking user functionality root existence", e);
			throw new Exception("Unable to check user functionality existence", e);
		}
		return exists;
	}

	/**
	 * User functionality root exists.
	 *
	 * @param userProfile the user profile
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean userFunctionalityRootExists(UserProfile userProfile) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		boolean toReturn = false;
		String userId = (String) userProfile.getUserId();
		try {
			toReturn = userFunctionalityRootExists(userId);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot find if user functionality exists for user [" + userId + "]", e);
		}
		return toReturn;
	}

	/**
	 * Load the user personal folder as a LowFunctionality object. If the personal folder exists, it is returned; if it does not exist and create is false, null
	 * is returned, otherwise the personal folder is created and then returned.
	 *
	 * @param userProfile         UserProfile the user profile object
	 * @param createIfNotExisting Boolean that specifies if the personal folder must be created if it doesn't exist
	 * @return the personal folder as a LowFunctionality object, or null in case the personal folder does not exist and create is false
	 */
	public static LowFunctionality loadUserFunctionalityRoot(UserProfile userProfile, boolean createIfNotExisting) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		String userId = (String) userProfile.getUserId();
		if (createIfNotExisting && !userFunctionalityRootExists(userProfile)) {
			try {
				createUserFunctionalityRoot(userProfile);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot create personal functionality for user with id [" + userId + "]", e);
			}
		}
		LowFunctionality lf = null;
		try {
			lf = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath("/" + userId, false);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot load user functionality for user with id [" + userId + "]", e);
		}
		return lf;
	}

	public static boolean isPersonalFolder(LowFunctionality folder, UserProfile userProfile) {
		Assert.assertNotNull(userProfile, "User profile in input is null");
		Assert.assertNotNull(folder, "Folder in input is null");
		LowFunctionality personalFolder = loadUserFunctionalityRoot(userProfile, false);
		if (personalFolder == null) {
			String userId = (String) userProfile.getUserId();
			LOGGER.debug("Personal folder for user [" + userId + "] does not exist");
			return false;
		}
		return personalFolder.getId().equals(folder.getId());
	}

	public static boolean isAPersonalFolder(LowFunctionality folder) {
		Assert.assertNotNull(folder, "Folder in input is null");
		try {
			List<LowFunctionality> lowFunct = DAOFactory.getLowFunctionalityDAO().loadAllUserFunct();

			if (lowFunct != null) {
				for (int i = 0; i < lowFunct.size(); i++) {
					if ((lowFunct.get(i)).getId().equals(folder.getId())) {
						return true;
					}
				}
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot load user functionality", e);
		}

		return false;
	}

	/**
	 * Creates the user functionality root.
	 *
	 * @param userProfile the user profile
	 * @throws Exception the exception
	 */
	public static void createUserFunctionalityRoot(IEngUserProfile userProfile) throws Exception {
		LOGGER.debug("IN");
		try {
			String userId = (String) ((UserProfile) userProfile).getUserId();
			LOGGER.debug("userId: " + userId);
			Collection roleStrs = ((UserProfile) userProfile).getRolesForUse();
			Iterator roleIter = roleStrs.iterator();
			List roles = new ArrayList();
			LOGGER.debug("Roles's number: " + roleStrs.size());
			while (roleIter.hasNext()) {
				String rolename = (String) roleIter.next();
				LOGGER.debug("Rolename: " + rolename);
				Role role = DAOFactory.getRoleDAO().loadByName(rolename);
				if (role != null) {
					roles.add(role);
					LOGGER.debug("Add Rolename ( " + rolename + ") ");
				} else {
					LOGGER.debug("Rolename ( " + rolename + ") doesn't exist in EXT_ROLES");
				}
			}

			UserFunctionality userFunct = new UserFunctionality();
			userFunct.setCode("ufr_" + userId);
			userFunct.setDescription("User Functionality Root");
			userFunct.setName(userId);
			userFunct.setPath("/" + userId);
			ILowFunctionalityDAO functdao = DAOFactory.getLowFunctionalityDAO();
			functdao.insertUserFunctionality(userFunct);
		} catch (Exception e) {
			LOGGER.error("Error while creating user functionality root", e);
			throw new Exception("Unable to create user functionality root", e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	public static String[] readFunctionality(SpagoBIUserProfile user) {
		LOGGER.debug("IN");
		List<String> functionalities = new ArrayList<>();
		List<String> roleFunct = readFunctionalityByRole(user);
		List<String> userFunct = readFunctionalityByUser(user);
		List<String> licenseFunct = readFunctionalityByLicense(user);
		functionalities.addAll(roleFunct);
		functionalities.addAll(userFunct);
		functionalities.addAll(licenseFunct);
		String[] a = new String[] { "" };
		LOGGER.debug("OUT");
		return functionalities.toArray(a);
	}

	public static List<String> readFunctionalityByUser(SpagoBIUserProfile user) {
		Boolean isSuperAdm = user.getIsSuperadmin();
		if (isSuperAdm != null && isSuperAdm) {
			return getSuperadminFunctionalities();
		} else {
			return new ArrayList<>();
		}
	}

	private static List<String> getSuperadminFunctionalities() {
		List<String> superadminFunctionalities = new ArrayList<>();

		superadminFunctionalities.add(CommunityFunctionalityConstants.DATASOURCE_MANAGEMENT);
		superadminFunctionalities.add(CommunityFunctionalityConstants.DATASOURCE_READ);
		superadminFunctionalities.add(CommunityFunctionalityConstants.CONFIG_MANAGEMENT);
		superadminFunctionalities.add(CommunityFunctionalityConstants.EXPORTERS_CATALOGUE);
		superadminFunctionalities.add(CommunityFunctionalityConstants.DOMAIN_MANAGEMENT);
		superadminFunctionalities.add(CommunityFunctionalityConstants.LICENSE_MANAGEMENT);

		return superadminFunctionalities;
	}

	public static List<String> readFunctionalityByRole(SpagoBIUserProfile user) {
		LOGGER.debug("IN");
		try {
			String[] roles = user.getRoles();
			String organization = user.getOrganization();
			it.eng.spagobi.commons.dao.IUserFunctionalityDAO dao = DAOFactory.getUserFunctionalityDAO();
			dao.setTenant(organization);
			String[] functionalities = dao.readUserFunctionality(roles);
			LOGGER.debug("Functionalities retrieved: " + functionalities == null ? "" : functionalities.toString());

			List<String> roleFunctionalities = new ArrayList<>();
			Role virtualRole = getVirtualRole(roles, organization);

			if (virtualRole.getAbleToSaveSubobjects()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SAVE_SUBOBJECT_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSeeSubobjects()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_SUBOBJECTS_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSeeSnapshots()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_SNAPSHOTS_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToRunSnapshots()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.RUN_SNAPSHOTS_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSeeViewpoints()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_VIEWPOINTS_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSeeNotes()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_NOTES_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSendMail()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEND_MAIL_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSaveIntoPersonalFolder()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SAVE_INTO_FOLDER_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSaveRememberMe()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SAVE_REMEMBER_ME_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSeeMetadata()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_METADATA_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToSaveMetadata()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SAVE_METADATA_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToBuildQbeQuery()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.BUILD_QBE_QUERIES_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToDoMassiveExport()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.DO_MASSIVE_EXPORT_FUNCTIONALITY);
			}
			if (virtualRole.getAbleToManageUsers()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.FINAL_USERS_MANAGEMENT);
			}
			if (virtualRole.getAbleToSeeDocumentBrowser()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_DOCUMENT_BROWSER);
			}
			if (virtualRole.getAbleToSeeMyData()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_MY_DATA);
			}
			if (virtualRole.getAbleToSeeMyWorkspace()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_MY_WORKSPACE);
			}
			if (virtualRole.getAbleToSeeFavourites()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_FAVOURITES);
			}
			if (virtualRole.getAbleToSeeSubscriptions()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_SUBSCRIPTIONS);
			}
			if (virtualRole.getAbleToSeeToDoList()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.SEE_TODO_LIST);
			}
			if (virtualRole.getAbleToCreateDocuments()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.CREATE_DOCUMENT);
			}
			if (virtualRole.getAbleToEditAllKpiComm()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.KPI_COMMENT_EDIT_ALL);
			}
			if (virtualRole.getAbleToEditMyKpiComm()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.KPI_COMMENT_EDIT_MY);
			}
			if (virtualRole.getAbleToDeleteKpiComm()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.KPI_COMMENT_DELETE);
			}
			if (virtualRole.getAbleToCreateSocialAnalysis()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.CREATE_SOCIAL_ANALYSIS);
			}
			if (virtualRole.getAbleToViewSocialAnalysis()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.VIEW_SOCIAL_ANALYSIS);
			}
			if (virtualRole.getAbleToHierarchiesManagement()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT);
			}
			if (virtualRole.getAbleToEnableDatasetPersistence()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.ENABLE_DATASET_PERSISTENCE);
			}
			if (virtualRole.getAbleToEnableFederatedDataset()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.ENABLE_FEDERATED_DATASET);
			}
			if (virtualRole.getAbleToEnableRate()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.ENABLE_TO_RATE);
			}
			if (virtualRole.getAbleToEnablePrint()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.ENABLE_TO_PRINT);
			}
			if (virtualRole.getAbleToEnableCopyAndEmbed()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.ENABLE_TO_COPY_AND_EMBED);
			}
			if (virtualRole.getAbleToManageGlossaryBusiness()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.MANAGE_GLOSSARY_BUSINESS);
			}
			if (virtualRole.getAbleToManageGlossaryTechnical()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.MANAGE_GLOSSARY_TECHNICAL);
			}
			if (virtualRole.getAbleToManageKpiValue()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.MANAGE_KPI_VALUE);
			}
			if (virtualRole.getAbleToManageCalendar()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.MANAGE_CALENDAR);
			}
			if (virtualRole.getAbleToUseFunctionsCatalog()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.FUNCTIONS_CATALOG_USAGE);
			}
			if (virtualRole.getAbleToManageInternationalization()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.MANAGE_INTERNATIONALIZATION);
			}

			if (virtualRole.getAbleToCreateSelfServiceCockpit()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.CREATE_SELF_SERVICE_COCKPIT);
			}

			if (virtualRole.getAbleToCreateSelfServiceGeoreport()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.CREATE_SELF_SERVICE_GEOREPORT);
			}

			if (virtualRole.getAbleToCreateSelfServiceKpi()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.CREATE_SELF_SERVICE_KPI);
			}

			if (virtualRole.getAbleToEditPythonScripts()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.EDIT_PYTHON_SCRIPTS);
			}
			if (virtualRole.getAbleToCreateCustomChart()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.CREATE_CUSTOM_CHART);
			}

			if (virtualRole.getAbleToUseDataPreparation()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.DATA_PREPARATION);
			}

			if (virtualRole.getAbleToSeeHelpOnline()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.HELP_ON_LINE);
			}

			if (virtualRole.getAbleToUseDossier()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.DOSSIER_CREATION);
			}

			if (virtualRole.getAbleToUseDashboardThemeManagement()) {
				roleFunctionalities.add(CommunityFunctionalityConstants.DASHBOARD_THEMES_MANAGEMENT);
			}

			if (!roleFunctionalities.isEmpty()) {
				List<String> roleTypeFunctionalities = Arrays.asList(functionalities);
				roleFunctionalities.addAll(roleTypeFunctionalities);
			} else {
				Boolean isSuperAdm = user.getIsSuperadmin();

				if (!isSuperAdm) {
					for (int i = 0; i < functionalities.length; i++) {
						String f = functionalities[i];
						if (!f.equalsIgnoreCase(CommunityFunctionalityConstants.TENANT_MANAGEMENT)) {
							roleFunctionalities.add(f);
						}
					}

				} else {
					roleFunctionalities = Arrays.asList(functionalities);
				}
			}

			return roleFunctionalities;
		} catch (Exception e) {
			LOGGER.error("Exception", e);
			throw new RuntimeException("Error while loading functionalities", e);
		} finally {
			LOGGER.debug("OUT");
		}

	}

	public static List<String> readFunctionalityByLicense(SpagoBIUserProfile user) {
		LOGGER.debug("IN");
		List<String> licenseFunctionalities = new ArrayList<>();
		try {
			Class<?> licenseManager = Class.forName("it.eng.knowage.tools.servermanager.utils.LicenseManager");
			Method getInstanceMethod = licenseManager.getMethod("getInstance");
			Method readFunctionalitiesMethod = licenseManager.getMethod("readFunctionalityByLicense", SpagoBIUserProfile.class);
			Object instance = getInstanceMethod.invoke(null);
			Set<String> functionalities = (Set<String>) readFunctionalitiesMethod.invoke(instance, user);
			if (functionalities != null) {
				licenseFunctionalities.addAll(functionalities);
			}
		} catch (Exception e) {
			LOGGER.debug("Server Manager not installed or not installer correctly.", e);
			licenseFunctionalities.addAll(freeFunctionalities());
			LOGGER.debug("Add following free functionalities: " + licenseFunctionalities, e);
		}
		LOGGER.debug("OUT");
		return licenseFunctionalities;
	}

	/**
	 * Add commons functionalities availables only in the Enterprise to the Community for free.
	 *
	 * @return List of functionalities
	 */
	private static List<String> freeFunctionalities() {
		List<String> ret = new ArrayList<>();
		ret.add(DOCUMENT_WIDGET_USE);
		ret.add(MAP_WIDGET_USE);
		return ret;
	}

	public static String getUserId(HttpServletRequest req) {
		LOGGER.debug("IN");
		SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
		String userId = userProxy.readUserIdentifier(req);
		LOGGER.debug("OUT,userId:" + userId);
		return userId;
	}

	private static Role getVirtualRole(String[] roles, String organization) throws Exception {
		LOGGER.debug("IN");
		Role virtualRole = new Role("", "");

		if (roles != null) {
			for (int i = 0; i < roles.length; i++) {
				String roleName = roles[i];
				LOGGER.debug("RoleName=" + roleName);
				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				roleDAO.setTenant(organization);
				Role anotherRole = roleDAO.loadByName(roleName);
				if (anotherRole != null) {
					if (anotherRole.getAbleToEditPythonScripts()) {
						LOGGER.debug("User has role " + roleName + " that is able to edit python scripts.");
						virtualRole.setAbleToEditPythonScripts(true);
					}
					if (anotherRole.getAbleToCreateCustomChart()) {
						LOGGER.debug("User has role " + roleName + " that is able to create custom chart.");
						virtualRole.setAbleToCreateCustomChart(true);
					}
					if (anotherRole.getAbleToSaveSubobjects()) {
						LOGGER.debug("User has role " + roleName + " that is able to save subobjects.");
						virtualRole.setAbleToSaveSubobjects(true);
					}
					if (anotherRole.getAbleToSeeSubobjects()) {
						LOGGER.debug("User has role " + roleName + " that is able to see subobjects.");
						virtualRole.setAbleToSeeSubobjects(true);
					}
					if (anotherRole.getAbleToSeeViewpoints()) {
						LOGGER.debug("User has role " + roleName + " that is able to see viewpoints.");
						virtualRole.setAbleToSeeViewpoints(true);
					}
					if (anotherRole.getAbleToSeeSnapshots()) {
						LOGGER.debug("User has role " + roleName + " that is able to see snapshots.");
						virtualRole.setAbleToSeeSnapshots(true);
					}
					if (anotherRole.getAbleToRunSnapshots()) {
						LOGGER.debug("User has role " + roleName + " that is able to run snapshots.");
						virtualRole.setAbleToRunSnapshots(true);
					}
					if (anotherRole.getAbleToSeeMetadata()) {
						LOGGER.debug("User has role " + roleName + " that is able to see metadata.");
						virtualRole.setAbleToSeeMetadata(true);
					}
					if (anotherRole.getAbleToSaveMetadata()) {
						LOGGER.debug("User has role " + roleName + " that is able to save metadata.");
						virtualRole.setAbleToSaveMetadata(true);
					}
					if (anotherRole.getAbleToSendMail()) {
						LOGGER.debug("User has role " + roleName + " that is able to send mail.");
						virtualRole.setAbleToSendMail(true);
					}
					if (anotherRole.getAbleToSeeNotes()) {
						LOGGER.debug("User has role " + roleName + " that is able to see notes.");
						virtualRole.setAbleToSeeNotes(true);
					}
					if (anotherRole.getAbleToSaveRememberMe()) {
						LOGGER.debug("User has role " + roleName + " that is able to save remember me.");
						virtualRole.setAbleToSaveRememberMe(true);
					}
					if (anotherRole.getAbleToSaveIntoPersonalFolder()) {
						LOGGER.debug("User has role " + roleName + " that is able to save into personal folder.");
						virtualRole.setAbleToSaveIntoPersonalFolder(true);
					}
					if (anotherRole.getAbleToBuildQbeQuery()) {
						LOGGER.debug("User has role " + roleName + " that is able to build QBE queries.");
						virtualRole.setAbleToBuildQbeQuery(true);
					}
					if (anotherRole.getAbleToDoMassiveExport()) {
						LOGGER.debug("User has role " + roleName + " that is able to do massive export.");
						virtualRole.setAbleToDoMassiveExport(true);
					}
					if (anotherRole.getAbleToManageUsers()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage users.");
						virtualRole.setAbleToManageUsers(true);
					}
					if (anotherRole.getAbleToSeeDocumentBrowser()) {
						LOGGER.debug("User has role " + roleName + " that is able to see document browser.");
						virtualRole.setAbleToSeeDocumentBrowser(true);
					}
					if (anotherRole.getAbleToSeeMyData()) {
						LOGGER.debug("User has role " + roleName + " that is able to see MyData.");
						virtualRole.setAbleToSeeMyData(true);
					}
					if (anotherRole.getAbleToSeeMyWorkspace()) {
						LOGGER.debug("User has role " + roleName + " that is able to see MyWorkspace.");
						virtualRole.setAbleToSeeMyWorkspace(true);
					}
					if (anotherRole.getAbleToSeeFavourites()) {
						LOGGER.debug("User has role " + roleName + " that is able to see Favourites.");
						virtualRole.setAbleToSeeFavourites(true);
					}
					if (anotherRole.getAbleToSeeSubscriptions()) {
						LOGGER.debug("User has role " + roleName + " that is able to see Subscriptions.");
						virtualRole.setAbleToSeeSubscriptions(true);
					}
					if (anotherRole.getAbleToSeeToDoList()) {
						LOGGER.debug("User has role " + roleName + " that is able to see To Do List.");
						virtualRole.setAbleToSeeToDoList(true);
					}
					if (anotherRole.getAbleToCreateDocuments()) {
						LOGGER.debug("User has role " + roleName + " that is able to create documents.");
						virtualRole.setAbleToCreateDocuments(true);
					}
					if (anotherRole.getAbleToEditAllKpiComm()) {
						LOGGER.debug("User has role " + roleName + " that is able to edit all kpi comments.");
						virtualRole.setAbleToEditAllKpiComm(true);
					}
					if (anotherRole.getAbleToEditMyKpiComm()) {
						LOGGER.debug("User has role " + roleName + " that is able to edit owned kpi comments.");
						virtualRole.setAbleToEditMyKpiComm(true);
					}
					if (anotherRole.getAbleToEditAllKpiComm()) {
						LOGGER.debug("User has role " + roleName + " that is able to delete kpi comments.");
						virtualRole.setAbleToDeleteKpiComm(true);
					}
					if (anotherRole.getAbleToCreateSocialAnalysis()) {
						LOGGER.debug("User has role " + roleName + " that is able to create social analysis.");
						virtualRole.setAbleToCreateSocialAnalysis(true);
					}
					if (anotherRole.getAbleToViewSocialAnalysis()) {
						LOGGER.debug("User has role " + roleName + " that is able to view social analysis.");
						virtualRole.setAbleToViewSocialAnalysis(true);
					}
					if (anotherRole.getAbleToHierarchiesManagement()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage hierarchies");
						virtualRole.setAbleToHierarchiesManagement(true);
					}
					if (anotherRole.getAbleToEnableDatasetPersistence()) {
						LOGGER.debug("User has role " + roleName + " that is able to persist dataset.");
						virtualRole.setAbleToEnableDatasetPersistence(true);
					}
					if (anotherRole.getAbleToEnableFederatedDataset()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage federated dataset.");
						virtualRole.setAbleToEnableFederatedDataset(true);
					}
					if (anotherRole.getAbleToEnableRate()) {
						LOGGER.debug("User has role " + roleName + " that is able to enable rating.");
						virtualRole.setAbleToEnableRate(true);
					}
					if (anotherRole.getAbleToEnablePrint()) {
						LOGGER.debug("User has role " + roleName + " that is able to print documents.");
						virtualRole.setAbleToEnablePrint(true);
					}
					if (anotherRole.getAbleToEnableCopyAndEmbed()) {
						LOGGER.debug("User has role " + roleName + " that is able to copy or embed link.");
						virtualRole.setAbleToEnableCopyAndEmbed(true);
					}
					if (anotherRole.getAbleToManageGlossaryBusiness()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage glossary business.");
						virtualRole.setAbleToManageGlossaryBusiness(true);
					}
					if (anotherRole.getAbleToManageGlossaryTechnical()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage glossary technical.");
						virtualRole.setAbleToManageGlossaryTechnical(true);
					}
					if (anotherRole.getAbleToManageKpiValue()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage kpi value.");
						virtualRole.setAbleToManageKpiValue(true);
					}
					if (anotherRole.getAbleToManageCalendar()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage Calendar.");
						virtualRole.setAbleToManageCalendar(true);
					}
					if (anotherRole.getAbleToUseFunctionsCatalog()) {
						LOGGER.debug("User has role " + roleName + " that is able to use functions catalog.");
						virtualRole.setAbleToUseFunctionsCatalog(true);
					}
					if (anotherRole.getAbleToManageInternationalization()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage Internationalization.");
						virtualRole.setAbleToManageInternationalization(true);
					}
					if (anotherRole.getAbleToCreateSelfServiceCockpit()) {
						LOGGER.debug("User has role " + roleName + " that is able to create self service cockpit.");
						virtualRole.setAbleToCreateSelfServiceCockpit(true);
					}
					if (anotherRole.getAbleToCreateSelfServiceGeoreport()) {
						LOGGER.debug("User has role " + roleName + " that is able to create self service geographic report.");
						virtualRole.setAbleToCreateSelfServiceGeoreport(true);
					}
					if (anotherRole.getAbleToCreateSelfServiceKpi()) {
						LOGGER.debug("User has role " + roleName + " that is able to create self service kpi.");
						virtualRole.setAbleToCreateSelfServiceKpi(true);
					}
					if (anotherRole.getAbleToUseDataPreparation()) {
						LOGGER.debug("User has role " + roleName + " that is able to use data preparation.");
						virtualRole.setAbleToUseDataPreparation(true);
					}
					if (anotherRole.getAbleToUseDossier()) {
						LOGGER.debug("User has role " + roleName + " that is able to create dossier.");
						virtualRole.setAbleToUseDossier(true);
					}
					if (anotherRole.getAbleToSeeHelpOnline()) {
						LOGGER.debug("User has role " + roleName + " that is able to see help online.");
						virtualRole.setAbleToSeeHelpOnline(true);
					}
					if (anotherRole.getAbleToUseDashboardThemeManagement()) {
						LOGGER.debug("User has role " + roleName + " that is able to manage dashboard themes.");
						virtualRole.setAbleToUseDashboardThemeManagement(true);
					}
				}
			}
		}
		LOGGER.debug("OUT");
		return virtualRole;
	}

	private static void checkTenant(SpagoBIUserProfile profile) {
		if (profile.getOrganization() == null) {
			LOGGER.warn("User profile [" + profile.getUserId() + "] has no organization/tenant set!!!");
			List<SbiTenant> tenants = DAOFactory.getTenantsDAO().loadAllTenants();
			if (tenants == null || tenants.isEmpty()) {
				throw new SpagoBIRuntimeException("No tenants found on database");
			}
			if (tenants.size() > 1) {
				throw new SpagoBIRuntimeException(
						"Tenants are more than one, cannot associate input user profile [" + profile.getUserId() + "] to a single tenant!!!");
			}
			SbiTenant tenant = tenants.get(0);
			LOGGER.warn("Associating user profile [" + profile.getUserId() + "] to tenant [" + tenant.getName() + "]");
			profile.setOrganization(tenant.getName());
		}
	}

	/*
	 * Method copied from SecurityServiceSupplierFactory for DAO refactoring
	 *
	 * is this method in the right place?
	 */

	public static ISecurityServiceSupplier createISecurityServiceSupplier() {
		LOGGER.debug("IN");

		ISecurityServiceSupplier securityServiceSupplier = null;
		String engUserProfileFactoryClass = null;

		try {
			SingletonConfig configSingleton = SingletonConfig.getInstance();
			engUserProfileFactoryClass = configSingleton.getConfigValue("SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS.className");
			if (engUserProfileFactoryClass != null) {
				engUserProfileFactoryClass = engUserProfileFactoryClass.trim();
				try {
					securityServiceSupplier = (ISecurityServiceSupplier) Class.forName(engUserProfileFactoryClass).newInstance();
				} catch (Throwable t) {
					throw new DAORuntimeException("Impossible to instatiate supplier class [" + engUserProfileFactoryClass + "]", t);
				}
			} else {
				throw new DAORuntimeException(
						"Impossible read from configuartion the property [SPAGOBI.SECURITY.USER-PROFILE-FACTORY-CLASS] that contains the name of the class used as securityServiceSupplier");
			}
		} catch (DAORuntimeException e) {
			throw e;
		} catch (Throwable t) {
			throw new DAORuntimeException("Impossible to instatiate supplier class [" + engUserProfileFactoryClass + "]", t);
		}

		return securityServiceSupplier;
	}

	/**
	 * Clones the input profile object. We don't implement the SpagoBIUserProfile.clone method because SpagoBIUserProfile is created by Axis tools, and
	 * therefore, when generating the class we may lost that method.
	 *
	 * @param profile The input SpagoBIUserProfile object
	 * @return a clone of the input SpagoBIUserProfile object
	 */
	public static SpagoBIUserProfile clone(SpagoBIUserProfile profile) {
		// @formatter:off
		SpagoBIUserProfile clone = new SpagoBIUserProfile(
			profile.getAttributes() != null ? new HashMap(profile.getAttributes()) : null,
			profile.getFunctions() != null ? profile.getFunctions().clone() : null,
			profile.getIsSuperadmin(),
			profile.getOrganization(),
			profile.getRoles() != null ? profile.getRoles().clone() : null,
			profile.getUniqueIdentifier(),
			profile.getUserId(),
			profile.getUserName()
		);
		// @formatter:on
		return clone;
	}

	public static boolean isEngineEnabled(IEngUserProfile userProfile, String valueCd) {
		boolean toReturn = false;

		try {
			IEngineDAO engineDao = DAOFactory.getEngineDAO();
			engineDao.setUserProfile(userProfile);
			List<Engine> nonPagedEngines = engineDao.loadAllEnginesByTenant();
			for (int i = 0, l = nonPagedEngines.size(); i < l; i++) {
				Engine elem = nonPagedEngines.get(i);
				IDomainDAO domainDAO = DAOFactory.getDomainDAO();
				Domain domainType = domainDAO.loadDomainById(elem.getBiobjTypeId());
				if (domainType.getValueCd().equalsIgnoreCase(valueCd)) {
					toReturn = true;
					break;
				}
			}
		} catch (Throwable t) {
			LOGGER.error("Impossible to load engines from database ", t);
			throw new SpagoBIEngineRuntimeException("Impossible get engine availability");
		}

		return toReturn;

	}

	public static List<String> getCurrentRoleNames(IEngUserProfile profile) throws EMFInternalError {
		LOGGER.debug("IN");
		List<String> roleNames = (List<String>) ((UserProfile) profile).getRolesForUse();
		LOGGER.debug("OUT");
		return roleNames;
	}

	public static Set<Domain> getDataSetCategoriesByUser(IEngUserProfile profile) {
		LOGGER.debug("IN");
		IRoleDAO rolesDao = null;
		Set<Domain> categories = new HashSet<>();
		try {
			// to get Roles Names check first if a default role is set, otherwise get all
			List<String> roleNames = getCurrentRoleNames(profile);

			if (!roleNames.isEmpty()) {
				rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(profile);
				ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
				List<Domain> array = categoryDao.getCategoriesForDataset().stream().map(Domain::fromCategory).collect(toList());
				for (String roleName : roleNames) {
					Role role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					for (RoleMetaModelCategory r : ds) {
						for (Domain dom : array) {
							if (r.getCategoryId().equals(dom.getValueId())) {
								categories.add(dom);
							}
						}

					}
				}
			}
			LOGGER.debug("OUT");
			return categories;
		} catch (Exception e) {
			LOGGER.error("Impossible to get role dataset categories for user [" + profile + "]", e);
			throw new SpagoBIRuntimeException("Impossible to get role dataset categories for user [" + profile + "]", e);
		}
	}

	public static Set<Domain> getBusinessModelsCategoriesByUser(IEngUserProfile profile) {
		LOGGER.debug("IN");
		IRoleDAO rolesDao = null;
		Set<Domain> toReturn = new HashSet<>();
		try {
			// to get Roles Names check first if a default role is set, otherwise get all
			List<String> roleNames = getCurrentRoleNames(profile);

			if (!roleNames.isEmpty()) {
				rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(profile);
				ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
				List<Domain> allCategories = categoryDao.getCategoriesForBusinessModel().stream().map(Domain::fromCategory).collect(toList());
				for (String roleName : roleNames) {
					Role role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> roles = rolesDao.getMetaModelCategoriesForRole(role.getId());
					for (RoleMetaModelCategory r : roles) {
						for (Domain cat : allCategories) {
							if (r.getCategoryId().equals(cat.getValueId())) {
								toReturn.add(cat);
							}
						}

					}
				}
			}
			LOGGER.debug("OUT");
			return toReturn;
		} catch (Exception e) {
			LOGGER.error("Impossible to get role dataset categories for user [" + profile + "]", e);
			throw new SpagoBIRuntimeException("Impossible to get role dataset categories for user [" + profile + "]", e);
		}
	}

	public static AccessibilityPreferences readAccessibilityPreferencesByUser(IEngUserProfile user) {
		AccessibilityPreferences preferences = null;

		if (user != null) {
			String userId = (String) ((UserProfile) user).getUserId();

			try {
				it.eng.spagobi.profiling.dao.ISbiAccessibilityPreferencesDAO dao = DAOFactory.getSiAccessibilityPreferencesDAO();
				SbiAccessibilityPreferences ap = dao.readUserAccessibilityPreferences(userId);
				if (ap != null) {
					preferences = new AccessibilityPreferences();
					preferences.setId(ap.getId());
					preferences.setUser((String) ((UserProfile) user).getUserId());
					preferences.setEnableUio(ap.isEnableUio());
					preferences.setEnableRobobraille(ap.isEnableRobobraille());
					preferences.setEnableGraphSonification(ap.isEnableGraphSonification());
					preferences.setEnableVoice(ap.isEnableVoice());
					preferences.setPreferences(ap.getPreferences());
				}
			} catch (EMFUserError e) {
				LOGGER.error("Impossible to get preferences for user [" + user + "]", e);
			}
		}

		return preferences;

	}

	public static List<RoleMetaModelCategory> getUserCategories(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		List<RoleMetaModelCategory> categories = new ArrayList<>();
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				if (r.getRoleMetaModelCategories() != null) {
					categories.addAll(r.getRoleMetaModelCategories());
				}

			}
			return categories;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static ArrayList<Role> getAdministratorRoles(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");

		ArrayList<Role> listRoles = new ArrayList<>();

		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				String roleCode = r.getRoleTypeCD();
				if (roleCode.equalsIgnoreCase(SpagoBIConstants.ADMIN_ROLE_TYPE) || roleCode.equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_MODEL_ADMIN)) {
					listRoles.add(r);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
		return listRoles;
	}

	public static ArrayList<String> getAdministratorRolesNames(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");

		ArrayList<String> listRoles = new ArrayList<>();

		LOGGER.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				String roleCode = r.getRoleTypeCD();
				if (roleCode.equalsIgnoreCase(SpagoBIConstants.ADMIN_ROLE_TYPE) || roleCode.equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_MODEL_ADMIN)) {
					listRoles.add(r.getName());
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
		return listRoles;
	}

	private UserUtilities() {
	}

}