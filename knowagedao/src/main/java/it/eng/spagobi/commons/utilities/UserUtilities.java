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

import static it.eng.spagobi.commons.constants.SpagoBIConstants.DOCUMENT_WIDGET_USE;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
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
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.dao.exception.DAORuntimeException;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.profiling.PublicProfile;
import it.eng.spagobi.profiling.bean.SbiAccessibilityPreferences;
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

	static Logger logger = Logger.getLogger(UserUtilities.class);

	public static String getSchema(String ente, RequestContainer aRequestContainer) {

		logger.debug("Ente: " + ente);
		SessionContainer aSessionContainer = aRequestContainer.getSessionContainer();
		SessionContainer permanentSession = aSessionContainer.getPermanentContainer();

		IEngUserProfile userProfile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		if (userProfile != null) {
			try {
				return (String) userProfile.getUserAttribute(ente);
			} catch (EMFInternalError e) {
				logger.error("User profile is NULL!!!!");
			}
		} else {
			logger.warn("User profile is NULL!!!!");
		}
		return null;
	}

	public static String getSchema(String ente, IEngUserProfile userProfile) {
		logger.debug("Ente: " + ente);
		if (userProfile != null) {
			try {
				return (String) userProfile.getUserAttribute(ente);
			} catch (EMFInternalError e) {
				logger.error("User profile is NULL!!!!");
			}
		} else {
			logger.warn("User profile is NULL!!!!");
		}
		return null;
	}

	/**
	 * Gets the user profile.
	 *
	 * @return the user profile
	 * @throws Exception the exception
	 */
	public static IEngUserProfile getUserProfile() throws Exception {
		RequestContainer aRequestContainer = RequestContainer.getRequestContainer();
		SessionContainer aSessionContainer = aRequestContainer.getSessionContainer();
		SessionContainer permanentSession = aSessionContainer.getPermanentContainer();

		IEngUserProfile userProfile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		if (userProfile == null) {

			String userId = null;
			PortletRequest portletRequest = PortletUtilities.getPortletRequest();
			Principal principal = portletRequest.getUserPrincipal();
			userId = principal.getName();
			logger.debug("got userId from Principal=" + userId);

			userProfile = UserUtilities.getUserProfile(userId);

			logger.debug("User profile created. User id: " + (String) ((UserProfile) userProfile).getUserId());
			logger.debug("Attributes name of the user profile: " + userProfile.getUserAttributeNames());
			logger.debug("Functionalities of the user profile: " + userProfile.getFunctionalities());
			logger.debug("Roles of the user profile: " + userProfile.getRoles());

			permanentSession.setAttribute(IEngUserProfile.ENG_USER_PROFILE, userProfile);

			// String username = (String) userProfile.getUserUniqueIdentifier();
			String username = ((UserProfile) userProfile).getUserId().toString();
			if (!UserUtilities.userFunctionalityRootExists(username)) {
				UserUtilities.createUserFunctionalityRoot(userProfile);
			}

		}

		return userProfile;
	}

	public static IEngUserProfile getUserProfile(HttpServletRequest req) throws Exception {
		logger.debug("IN");
		try {
			SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
			String userId = userProxy.readUserIdentifier(req);
			return UserUtilities.getUserProfile(userId);
		} finally {
			logger.debug("OUT");
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

		logger.debug("IN.userId=" + userId);
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
					logger.debug("User [" + userId + "] has been recognized as a scheduler user.");
					profile = UserProfile.createSchedulerUserProfile(userId);
				} else if (UserProfile.isDataPreparationUser(userId)) {
					logger.debug("User [" + userId + "] has been recognized as a data preparation user.");
					profile = UserProfile.createDataPreparationUserProfile(userId);
				} else if (PublicProfile.isPublicUser(userId)) {
					logger.debug("User [" + userId + "] has been recognized as a public user.");
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
				logger.debug("profile from get profile" + profile);

				return profile;

			} catch (Exception e) {
				logger.error("Exception while creating user profile", e);
				throw new SecurityException("Exception while creating user profile", e);
			} finally {
				logger.debug("OUT");
				getUserProfileMonitor.stop();
			}
		}

	}

	public static boolean isTechnicalUser(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) // for
																							// administrators
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV) // for
																								// developers
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST) // for
																								// testers
					|| profile.isAbleToExecuteAction(SpagoBIConstants.PARAMETER_MANAGEMENT)) { // for
																								// behavioural
																								// model
																								// administrators
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
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN) // for
																							// administrators
					|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {// for
																									// developers
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
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean hasDeveloperRole(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				String roleCode = r.getRoleTypeCD();
				if (roleCode.equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_DEV) || roleCode.equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_TEST)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean hasAdministratorRole(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				String roleCode = r.getRoleTypeCD();
				if (roleCode.equalsIgnoreCase(SpagoBIConstants.ADMIN_ROLE_TYPE) || roleCode.equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_MODEL_ADMIN)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean hasUserRole(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			Collection<String> roles = ((UserProfile) profile).getRolesForUse();
			for (String role : roles) {
				Role r = roleDAO.loadByName(role);
				String roleCode = r.getRoleTypeCD();
				if (roleCode.equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_USER)) {
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
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
		try {
			if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user's information", e);
		}
	}

	public static boolean haveRoleAndAuthorization(IEngUserProfile profile, String Role, String[] authorization) {
		Assert.assertNotNull(profile, "Object in input is null");
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
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
				if (Role == null || rol.getRoleTypeCD().compareTo(Role) == 0) {

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
			logger.debug("****  username checked: " + username);
			ILowFunctionalityDAO functdao = DAOFactory.getLowFunctionalityDAO();
			exists = functdao.checkUserRootExists(username);
		} catch (Exception e) {
			logger.error("Error while checking user functionality root existence", e);
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
			logger.debug("Personal folder for user [" + userId + "] does not exist");
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
		logger.debug("IN");
		try {
			String userId = (String) ((UserProfile) userProfile).getUserId();
			logger.debug("userId: " + userId);
			Collection roleStrs = ((UserProfile) userProfile).getRolesForUse();
			Iterator roleIter = roleStrs.iterator();
			List roles = new ArrayList();
			logger.debug("Roles's number: " + roleStrs.size());
			while (roleIter.hasNext()) {
				String rolename = (String) roleIter.next();
				logger.debug("Rolename: " + rolename);
				Role role = DAOFactory.getRoleDAO().loadByName(rolename);
				if (role != null) {
					roles.add(role);
					logger.debug("Add Rolename ( " + rolename + ") ");
				} else
					logger.debug("Rolename ( " + rolename + ") doesn't exist in EXT_ROLES");
			}
			Role[] rolesArr = new Role[roles.size()];
			rolesArr = (Role[]) roles.toArray(rolesArr);

			UserFunctionality userFunct = new UserFunctionality();
			userFunct.setCode("ufr_" + userId);
			userFunct.setDescription("User Functionality Root");
			userFunct.setName(userId);
			userFunct.setPath("/" + userId);
			// userFunct.setExecRoles(rolesArr);
			ILowFunctionalityDAO functdao = DAOFactory.getLowFunctionalityDAO();
			functdao.insertUserFunctionality(userFunct);
		} catch (Exception e) {
			logger.error("Error while creating user functionality root", e);
			throw new Exception("Unable to create user functionality root", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public static String[] readFunctionality(SpagoBIUserProfile user) {
		logger.debug("IN");
		List<String> functionalities = new ArrayList<>();
		List<String> roleFunct = readFunctionalityByRole(user);
		List<String> userFunct = readFunctionalityByUser(user);
		List<String> licenseFunct = readFunctionalityByLicense(user);
		functionalities.addAll(roleFunct);
		functionalities.addAll(userFunct);
		functionalities.addAll(licenseFunct);
		String[] a = new String[] { "" };
		logger.debug("OUT");
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

		superadminFunctionalities.add(SpagoBIConstants.DATASOURCE_MANAGEMENT);
		superadminFunctionalities.add(SpagoBIConstants.DATASOURCE_READ);
		superadminFunctionalities.add(SpagoBIConstants.CONFIG_MANAGEMENT);
		superadminFunctionalities.add(SpagoBIConstants.EXPORTERS_CATALOGUE);
		superadminFunctionalities.add(SpagoBIConstants.DOMAIN_MANAGEMENT);
		superadminFunctionalities.add(SpagoBIConstants.LICENSE_MANAGEMENT);

		return superadminFunctionalities;
	}

	public static List<String> readFunctionalityByRole(SpagoBIUserProfile user) {
		logger.debug("IN");
		try {
			String[] roles = user.getRoles();
			String organization = user.getOrganization();
			it.eng.spagobi.commons.dao.IUserFunctionalityDAO dao = DAOFactory.getUserFunctionalityDAO();
			dao.setTenant(organization);
			String[] functionalities = dao.readUserFunctionality(roles);
			logger.debug("Functionalities retrieved: " + functionalities == null ? "" : functionalities.toString());

			List<String> roleFunctionalities = new ArrayList<>();
			Role virtualRole = getVirtualRole(roles, organization);

			if (virtualRole.isAbleToSaveSubobjects()) {
				roleFunctionalities.add(SpagoBIConstants.SAVE_SUBOBJECT_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSeeSubobjects()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_SUBOBJECTS_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSeeSnapshots()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_SNAPSHOTS_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToRunSnapshots()) {
				roleFunctionalities.add(SpagoBIConstants.RUN_SNAPSHOTS_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSeeViewpoints()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_VIEWPOINTS_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSeeNotes()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_NOTES_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSendMail()) {
				roleFunctionalities.add(SpagoBIConstants.SEND_MAIL_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSaveIntoPersonalFolder()) {
				roleFunctionalities.add(SpagoBIConstants.SAVE_INTO_FOLDER_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSaveRememberMe()) {
				roleFunctionalities.add(SpagoBIConstants.SAVE_REMEMBER_ME_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSeeMetadata()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_METADATA_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToSaveMetadata()) {
				roleFunctionalities.add(SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToBuildQbeQuery()) {
				roleFunctionalities.add(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToDoMassiveExport()) {
				roleFunctionalities.add(SpagoBIConstants.DO_MASSIVE_EXPORT_FUNCTIONALITY);
			}
			if (virtualRole.isAbleToManageUsers()) {
				roleFunctionalities.add(SpagoBIConstants.FINAL_USERS_MANAGEMENT);
			}
			if (virtualRole.isAbleToSeeDocumentBrowser()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_DOCUMENT_BROWSER);
			}
			if (virtualRole.isAbleToSeeMyData()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_MY_DATA);
			}
			if (virtualRole.isAbleToSeeMyWorkspace()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_MY_WORKSPACE);
			}
			if (virtualRole.isAbleToSeeFavourites()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_FAVOURITES);
			}
			if (virtualRole.isAbleToSeeSubscriptions()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_SUBSCRIPTIONS);
			}
			if (virtualRole.isAbleToSeeToDoList()) {
				roleFunctionalities.add(SpagoBIConstants.SEE_TODO_LIST);
			}
			if (virtualRole.isAbleToCreateDocuments()) {
				roleFunctionalities.add(SpagoBIConstants.CREATE_DOCUMENT);
			}
			if (virtualRole.isAbleToEditAllKpiComm()) {
				roleFunctionalities.add(SpagoBIConstants.KPI_COMMENT_EDIT_ALL);
			}
			if (virtualRole.isAbleToEditMyKpiComm()) {
				roleFunctionalities.add(SpagoBIConstants.KPI_COMMENT_EDIT_MY);
			}
			if (virtualRole.isAbleToDeleteKpiComm()) {
				roleFunctionalities.add(SpagoBIConstants.KPI_COMMENT_DELETE);
			}
			if (virtualRole.isAbleToCreateSocialAnalysis()) {
				roleFunctionalities.add(SpagoBIConstants.CREATE_SOCIAL_ANALYSIS);
			}
			if (virtualRole.isAbleToViewSocialAnalysis()) {
				roleFunctionalities.add(SpagoBIConstants.VIEW_SOCIAL_ANALYSIS);
			}
			if (virtualRole.isAbleToHierarchiesManagement()) {
				roleFunctionalities.add(SpagoBIConstants.HIERARCHIES_MANAGEMENT);
			}
			if (virtualRole.isAbleToEnableDatasetPersistence()) {
				roleFunctionalities.add(SpagoBIConstants.ENABLE_DATASET_PERSISTENCE);
			}
			if (virtualRole.isAbleToEnableFederatedDataset()) {
				roleFunctionalities.add(SpagoBIConstants.ENABLE_FEDERATED_DATASET);
			}
			if (virtualRole.isAbleToEnableRate()) {
				roleFunctionalities.add(SpagoBIConstants.ENABLE_TO_RATE);
			}
			if (virtualRole.isAbleToEnablePrint()) {
				roleFunctionalities.add(SpagoBIConstants.ENABLE_TO_PRINT);
			}
			if (virtualRole.isAbleToEnableCopyAndEmbed()) {
				roleFunctionalities.add(SpagoBIConstants.ENABLE_TO_COPY_AND_EMBED);
			}
			if (virtualRole.isAbleToManageGlossaryBusiness()) {
				roleFunctionalities.add(SpagoBIConstants.MANAGE_GLOSSARY_BUSINESS);
			}
			if (virtualRole.isAbleToManageGlossaryTechnical()) {
				roleFunctionalities.add(SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL);
			}
			if (virtualRole.isAbleToManageKpiValue()) {
				roleFunctionalities.add(SpagoBIConstants.MANAGE_KPI_VALUE);
			}
			if (virtualRole.isAbleToManageCalendar()) {
				roleFunctionalities.add(SpagoBIConstants.MANAGE_CALENDAR);
			}
			if (virtualRole.isAbleToUseFunctionsCatalog()) {
				roleFunctionalities.add(SpagoBIConstants.FUNCTIONS_CATALOG_USAGE);
			}
			if (virtualRole.isAbleToManageInternationalization()) {
				roleFunctionalities.add(SpagoBIConstants.MANAGE_INTERNATIONALIZATION);
			}

			if (virtualRole.isAbleToCreateSelfServiceCockpit()) {
				roleFunctionalities.add(SpagoBIConstants.CREATE_SELF_SERVICE_COCKPIT);
			}

			if (virtualRole.isAbleToCreateSelfServiceGeoreport()) {
				roleFunctionalities.add(SpagoBIConstants.CREATE_SELF_SERVICE_GEOREPORT);
			}

			if (virtualRole.isAbleToCreateSelfServiceKpi()) {
				roleFunctionalities.add(SpagoBIConstants.CREATE_SELF_SERVICE_KPI);
			}

			if (virtualRole.isAbleToEditPythonScripts()) {
				roleFunctionalities.add(SpagoBIConstants.EDIT_PYTHON_SCRIPTS);
			}
			if (virtualRole.isAbleToCreateCustomChart()) {
				roleFunctionalities.add(SpagoBIConstants.CREATE_CUSTOM_CHART);
			}
			if (!roleFunctionalities.isEmpty()) {
				List<String> roleTypeFunctionalities = Arrays.asList(functionalities);
				roleFunctionalities.addAll(roleTypeFunctionalities);
			} else {
				Boolean isSuperAdm = user.getIsSuperadmin();

				if (!isSuperAdm) {
					for (int i = 0; i < functionalities.length; i++) {
						String f = functionalities[i];
						if (!f.equalsIgnoreCase(SpagoBIConstants.TENANT_MANAGEMENT)) {
							roleFunctionalities.add(f);
						}
					}

				} else {
					roleFunctionalities = Arrays.asList(functionalities);
				}
			}

			return roleFunctionalities;
		} catch (Exception e) {
			logger.error("Exception", e);
			throw new RuntimeException("Error while loading functionalities", e);
		} finally {
			logger.debug("OUT");
		}

	}

	public static List<String> readFunctionalityByLicense(SpagoBIUserProfile user) {
		logger.debug("IN");
		List<String> licenseFunctionalities = new ArrayList<>();
		try {
			Class<?> licenseManager = Class.forName("it.eng.knowage.tools.servermanager.utils.LicenseManager");
			Method readFunctionalitiesMethod = licenseManager.getMethod("readFunctionalityByLicense", SpagoBIUserProfile.class);
			Set<String> functionalities = (Set<String>) readFunctionalitiesMethod.invoke(null, user);
			if (functionalities != null) {
				licenseFunctionalities.addAll(functionalities);
			}
		} catch (Exception e) {
			logger.debug("Server Manager not installed or not installer correctly.", e);
			licenseFunctionalities.addAll(freeFunctionalities());
			logger.debug("Add following free functionalities: " + licenseFunctionalities, e);
		}
		logger.debug("OUT");
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
		ret.add("MapWidgetUse");
		return ret;
	}

	public static String getUserId(HttpServletRequest req) {
		logger.debug("IN");
		SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
		String userId = userProxy.readUserIdentifier(req);
		logger.debug("OUT,userId:" + userId);
		return userId;
	}

	private static Role getVirtualRole(String[] roles, String organization) throws Exception {
		logger.debug("IN");
		Role virtualRole = new Role("", "");

		if (roles != null) {
			for (int i = 0; i < roles.length; i++) {
				String roleName = roles[i];
				logger.debug("RoleName=" + roleName);
				IRoleDAO roleDAO = DAOFactory.getRoleDAO();
				roleDAO.setTenant(organization);
				Role anotherRole = roleDAO.loadByName(roleName);
				if (anotherRole != null) {
					if (anotherRole.isAbleToEditPythonScripts()) {
						logger.debug("User has role " + roleName + " that is able to edit python scripts.");
						virtualRole.setIsAbleToEditPythonScripts(true);
					}
					if (anotherRole.isAbleToCreateCustomChart()) {
						logger.debug("User has role " + roleName + " that is able to create custom chart.");
						virtualRole.setIsAbleToCreateCustomChart(true);
					}
					if (anotherRole.isAbleToSaveSubobjects()) {
						logger.debug("User has role " + roleName + " that is able to save subobjects.");
						virtualRole.setIsAbleToSaveSubobjects(true);
					}
					if (anotherRole.isAbleToSeeSubobjects()) {
						logger.debug("User has role " + roleName + " that is able to see subobjects.");
						virtualRole.setIsAbleToSeeSubobjects(true);
					}
					if (anotherRole.isAbleToSeeViewpoints()) {
						logger.debug("User has role " + roleName + " that is able to see viewpoints.");
						virtualRole.setIsAbleToSeeViewpoints(true);
					}
					if (anotherRole.isAbleToSeeSnapshots()) {
						logger.debug("User has role " + roleName + " that is able to see snapshots.");
						virtualRole.setIsAbleToSeeSnapshots(true);
					}
					if (anotherRole.isAbleToRunSnapshots()) {
						logger.debug("User has role " + roleName + " that is able to run snapshots.");
						virtualRole.setIsAbleToRunSnapshots(true);
					}
					if (anotherRole.isAbleToSeeMetadata()) {
						logger.debug("User has role " + roleName + " that is able to see metadata.");
						virtualRole.setIsAbleToSeeMetadata(true);
					}
					if (anotherRole.isAbleToSaveMetadata()) {
						logger.debug("User has role " + roleName + " that is able to save metadata.");
						virtualRole.setIsAbleToSaveMetadata(true);
					}
					if (anotherRole.isAbleToSendMail()) {
						logger.debug("User has role " + roleName + " that is able to send mail.");
						virtualRole.setIsAbleToSendMail(true);
					}
					if (anotherRole.isAbleToSeeNotes()) {
						logger.debug("User has role " + roleName + " that is able to see notes.");
						virtualRole.setIsAbleToSeeNotes(true);
					}
					if (anotherRole.isAbleToSaveRememberMe()) {
						logger.debug("User has role " + roleName + " that is able to save remember me.");
						virtualRole.setIsAbleToSaveRememberMe(true);
					}
					if (anotherRole.isAbleToSaveIntoPersonalFolder()) {
						logger.debug("User has role " + roleName + " that is able to save into personal folder.");
						virtualRole.setIsAbleToSaveIntoPersonalFolder(true);
					}
					if (anotherRole.isAbleToBuildQbeQuery()) {
						logger.debug("User has role " + roleName + " that is able to build QBE queries.");
						virtualRole.setIsAbleToBuildQbeQuery(true);
					}
					if (anotherRole.isAbleToDoMassiveExport()) {
						logger.debug("User has role " + roleName + " that is able to do massive export.");
						virtualRole.setIsAbleToDoMassiveExport(true);
					}
					if (anotherRole.isAbleToManageUsers()) {
						logger.debug("User has role " + roleName + " that is able to manage users.");
						virtualRole.setIsAbleToManageUsers(true);
					}
					if (anotherRole.isAbleToSeeDocumentBrowser()) {
						logger.debug("User has role " + roleName + " that is able to see document browser.");
						virtualRole.setIsAbleToSeeDocumentBrowser(true);
					}
					if (anotherRole.isAbleToSeeMyData()) {
						logger.debug("User has role " + roleName + " that is able to see MyData.");
						virtualRole.setIsAbleToSeeMyData(true);
					}
					if (anotherRole.isAbleToSeeMyWorkspace()) {
						logger.debug("User has role " + roleName + " that is able to see MyWorkspace.");
						virtualRole.setIsAbleToSeeMyWorkspace(true);
					}
					if (anotherRole.isAbleToSeeFavourites()) {
						logger.debug("User has role " + roleName + " that is able to see Favourites.");
						virtualRole.setIsAbleToSeeFavourites(true);
					}
					if (anotherRole.isAbleToSeeSubscriptions()) {
						logger.debug("User has role " + roleName + " that is able to see Subscriptions.");
						virtualRole.setIsAbleToSeeSubscriptions(true);
					}
					if (anotherRole.isAbleToSeeToDoList()) {
						logger.debug("User has role " + roleName + " that is able to see To Do List.");
						virtualRole.setIsAbleToSeeToDoList(true);
					}
					if (anotherRole.isAbleToCreateDocuments()) {
						logger.debug("User has role " + roleName + " that is able to create documents.");
						virtualRole.setIsAbleToCreateDocuments(true);
					}
					if (anotherRole.isAbleToEditAllKpiComm()) {
						logger.debug("User has role " + roleName + " that is able to edit all kpi comments.");
						virtualRole.setAbleToEditAllKpiComm(true);
					}
					if (anotherRole.isAbleToEditMyKpiComm()) {
						logger.debug("User has role " + roleName + " that is able to edit owned kpi comments.");
						virtualRole.setAbleToEditMyKpiComm(true);
					}
					if (anotherRole.isAbleToEditAllKpiComm()) {
						logger.debug("User has role " + roleName + " that is able to delete kpi comments.");
						virtualRole.setAbleToDeleteKpiComm(true);
					}
					if (anotherRole.isAbleToCreateSocialAnalysis()) {
						logger.debug("User has role " + roleName + " that is able to create social analysis.");
						virtualRole.setIsAbleToCreateSocialAnalysis(true);
					}
					if (anotherRole.isAbleToViewSocialAnalysis()) {
						logger.debug("User has role " + roleName + " that is able to view social analysis.");
						virtualRole.setIsAbleToViewSocialAnalysis(true);
					}
					if (anotherRole.isAbleToHierarchiesManagement()) {
						logger.debug("User has role " + roleName + " that is able to manage hierarchies");
						virtualRole.setIsAbleToHierarchiesManagement(true);
					}
					if (anotherRole.isAbleToEnableDatasetPersistence()) {
						logger.debug("User has role " + roleName + " that is able to persist dataset.");
						virtualRole.setIsAbleToEnableDatasetPersistence(true);
					}
					if (anotherRole.isAbleToEnableFederatedDataset()) {
						logger.debug("User has role " + roleName + " that is able to manage federated dataset.");
						virtualRole.setIsAbleToEnableFederatedDataset(true);
					}
					if (anotherRole.isAbleToEnableRate()) {
						logger.debug("User has role " + roleName + " that is able to enable rating.");
						virtualRole.setIsAbleToEnableRate(true);
					}
					if (anotherRole.isAbleToEnablePrint()) {
						logger.debug("User has role " + roleName + " that is able to print documents.");
						virtualRole.setIsAbleToEnablePrint(true);
					}
					if (anotherRole.isAbleToEnableCopyAndEmbed()) {
						logger.debug("User has role " + roleName + " that is able to copy or embed link.");
						virtualRole.setIsAbleToEnableCopyAndEmbed(true);
					}
					if (anotherRole.isAbleToManageGlossaryBusiness()) {
						logger.debug("User has role " + roleName + " that is able to manage glossary business.");
						virtualRole.setAbleToManageGlossaryBusiness(true);
					}
					if (anotherRole.isAbleToManageGlossaryTechnical()) {
						logger.debug("User has role " + roleName + " that is able to manage glossary technical.");
						virtualRole.setAbleToManageGlossaryTechnical(true);
					}
					if (anotherRole.isAbleToManageKpiValue()) {
						logger.debug("User has role " + roleName + " that is able to manage kpi value.");
						virtualRole.setAbleToManageKpiValue(true);
					}
					if (anotherRole.isAbleToManageCalendar()) {
						logger.debug("User has role " + roleName + " that is able to manage Calendar.");
						virtualRole.setAbleToManageCalendar(true);
					}
					if (anotherRole.isAbleToUseFunctionsCatalog()) {
						logger.debug("User has role " + roleName + " that is able to use functions catalog.");
						virtualRole.setAbleToUseFunctionsCatalog(true);
					}
					if (anotherRole.isAbleToManageInternationalization()) {
						logger.debug("User has role " + roleName + " that is able to manage Internationalization.");
						virtualRole.setAbleToManageInternationalization(true);
					}
					if (anotherRole.isAbleToCreateSelfServiceCockpit()) {
						logger.debug("User has role " + roleName + " that is able to create self service cockpit.");
						virtualRole.setAbleToCreateSelfServiceCockpit(true);
					}
					if (anotherRole.isAbleToCreateSelfServiceGeoreport()) {
						logger.debug("User has role " + roleName + " that is able to create self service geographic report.");
						virtualRole.setAbleToCreateSelfServiceGeoreport(true);
					}
					if (anotherRole.isAbleToCreateSelfServiceKpi()) {
						logger.debug("User has role " + roleName + " that is able to create self service kpi.");
						virtualRole.setAbleToCreateSelfServiceKpi(true);
					}
				}
			}
		}
		logger.debug("OUT");
		return virtualRole;
	}

	private static void checkTenant(SpagoBIUserProfile profile) {
		if (profile.getOrganization() == null) {
			logger.warn("User profile [" + profile.getUserId() + "] has no organization/tenant set!!!");
			List<SbiTenant> tenants = DAOFactory.getTenantsDAO().loadAllTenants();
			if (tenants == null || tenants.size() == 0) {
				throw new SpagoBIRuntimeException("No tenants found on database");
			}
			if (tenants.size() > 1) {
				throw new SpagoBIRuntimeException(
						"Tenants are more than one, cannot associate input user profile [" + profile.getUserId() + "] to a single tenant!!!");
			}
			SbiTenant tenant = tenants.get(0);
			logger.warn("Associating user profile [" + profile.getUserId() + "] to tenant [" + tenant.getName() + "]");
			profile.setOrganization(tenant.getName());
		}
	}

	/*
	 * Method copied from SecurityServiceSupplierFactory for DAO refactoring
	 *
	 * is this method in the right place?
	 */

	public static ISecurityServiceSupplier createISecurityServiceSupplier() {
		logger.debug("IN");

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
		} catch (Throwable t) {
			if (t instanceof DAORuntimeException)
				throw (DAORuntimeException) t;
			else
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
			profile.getAttributes() != null ? (HashMap) profile.getAttributes().clone() : null,
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
			logger.error("Impossible to load engines from database ", t);
			throw new SpagoBIEngineRuntimeException("Impossible get engine availability");
		}

		return toReturn;

	}

	public static List<String> getCurrentRoleNames(IEngUserProfile profile) throws EMFInternalError {
		logger.debug("IN");
		List<String> roleNames = (List<String>) ((UserProfile) profile).getRolesForUse();
		logger.debug("OUT");
		return roleNames;
	}

	public static Set<Domain> getDataSetCategoriesByUser(IEngUserProfile profile) {
		logger.debug("IN");
		IRoleDAO rolesDao = null;
		Set<Domain> categories = new HashSet<>();
		try {
			// to get Roles Names check first if a default role is set, otherwise get all
			List<String> roleNames = getCurrentRoleNames(profile);

			if (!roleNames.isEmpty()) {
				rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(profile);
				ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
				List<Domain> array = categoryDao.getCategoriesForDataset()
					.stream()
					.map(Domain::fromCategory)
					.collect(toList());
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
			logger.debug("OUT");
			return categories;
		} catch (Exception e) {
			logger.error("Impossible to get role dataset categories for user [" + profile + "]", e);
			throw new SpagoBIRuntimeException("Impossible to get role dataset categories for user [" + profile + "]", e);
		}
	}

	public static Set<Domain> getBusinessModelsCategoriesByUser(IEngUserProfile profile) {
		logger.debug("IN");
		IRoleDAO rolesDao = null;
		Set<Domain> toReturn = new HashSet<>();
		try {
			// to get Roles Names check first if a default role is set, otherwise get all
			List<String> roleNames = getCurrentRoleNames(profile);

			if (!roleNames.isEmpty()) {
				rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(profile);
				IDomainDAO domainDao = DAOFactory.getDomainDAO();
				ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
				List<Domain> allCategories = categoryDao.getCategoriesForBusinessModel()
					.stream()
					.map(Domain::fromCategory)
					.collect(toList());
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
			logger.debug("OUT");
			return toReturn;
		} catch (Exception e) {
			logger.error("Impossible to get role dataset categories for user [" + profile + "]", e);
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
				logger.error("Impossible to get preferences for user [" + user + "]", e);
				// e.printStackTrace();
			}
			// dao.setTenant(organization);
		}

		return preferences;

	}

	public static List<RoleMetaModelCategory> getUserCategories(IEngUserProfile profile) {
		Assert.assertNotNull(profile, "Object in input is null");
		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
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

		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
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

		logger.debug("IN.user id = [" + ((UserProfile) profile).getUserId() + "]");
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

}