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
package it.eng.spagobi.commons.bo;

import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.ALERT_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.ARTIFACT_CATALOGUE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.CKAN_FUNCTIONALITY;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.CONTSTRAINT_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.CONTSTRAINT_VIEW;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.CREATE_CHART_FUNCTIONALITY;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.CREATE_COCKPIT_FUNCTIONALITY;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.CREATE_DATASETS_AS_FINAL_USER;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DATASET_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DATASOURCE_BIG_DATA;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DATASOURCE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DATASOURCE_READ;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DATA_PREPARATION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DISTRIBUTIONLIST_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DISTRIBUTIONLIST_USER;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_ADMINISTRATION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_DELETE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_DETAIL_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_TEST;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_USER;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_METADATA_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MOVE_DOWN_STATE;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_MOVE_UP_STATE;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOCUMENT_STATE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.DOMAIN_WRITE;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.EVENTS_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.EXECUTE_CROSS_NAVIGATION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.FEDERATION_DEFINITION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.FUNCTIONALITIES_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.FUNCTIONS_CATALOG_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.GEO_LAYERS_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.GIS_WEB_DESIGNER;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.HOTLINK_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.IMAGES_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.KPI_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.KPI_SCHEDULATION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.LOVS_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.LOVS_VIEW;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MANAGE_ANALYTICAL_WIDGET;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MANAGE_CHART_WIDGET;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MANAGE_CROSS_NAVIGATION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MANAGE_MULTISHEET_COCKPIT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MANAGE_STATIC_WIDGET;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MAPCATALOGUE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MENU_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.META_MODELS_CATALOGUE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.META_MODEL_LIFECYCLE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.MODIFY_REFRESH;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.NOTIFY_CONTEXT_BROKER_ACTION;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.PARAMETER_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.PARAMETER_VIEW;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.PROFILE_ATTRIBUTES_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.PROFILE_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.READ_ENGINES_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.READ_ROLES;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.REGISTRY_DATA_ENTRY;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.SELF_SERVICE_DATASET_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.SELF_SERVICE_META_MODEL_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.SHARED_DEVELOPMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.SYNCRONIZE_ROLES_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.USER_SAVE_DOCUMENT_FUNCTIONALITY;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.VIEW_MY_FOLDER_ADMIN;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.WORKLIST_MANAGEMENT;
import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.WORKSPACE_MANAGEMENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.security.AuthorizationsBusinessMapper;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class contain the information about the user
 */
public class UserProfile implements IEngUserProfile {

	private static transient Logger logger = Logger.getLogger(UserProfile.class);

	private enum PREDEFINED_PROFILE_ATTRIBUTES {
		USER_ID("user_id"), USER_ROLES("user_roles"), TENANT_ID("TENANT_ID");

		private String name;

		PREDEFINED_PROFILE_ATTRIBUTES(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	private static String SCHEDULER_USER_NAME = "scheduler";
	private static String SCHEDULER_USER_ID_PREFIX = "scheduler - ";
	private static String DATA_PREP_USER_ID_PREFIX = "data-preparation -";
	private static String DATA_PREP_USER_NAME = "data-preparation";

	private static int SCHEDULER_JWT_TOKEN_EXPIRE_HOURS = 2; // JWT token for scheduler will expire in 2 HOURS

	private String userUniqueIdentifier = null;
	private String userId = null;
	private String userName = null;
	private Map userAttributes = null;
	private Collection roles = null;
	private Collection functionalities = null;

	private String organization = null;
	private Boolean isSuperadmin = false;

	@JsonIgnore
	private SpagoBIUserProfile spagoBIUserProfile = null;

	public UserProfile() {
	}

	public UserProfile(UserProfile other) {
		this.userUniqueIdentifier = other.userUniqueIdentifier;
		this.userId = other.userId;
		this.userName = other.userName;
		this.userAttributes = other.userAttributes;
		this.roles = other.roles;
		this.functionalities = other.functionalities;
		this.organization = other.organization;
		this.isSuperadmin = other.isSuperadmin;
		this.spagoBIUserProfile = other.spagoBIUserProfile;
	}

	/**
	 * The Constructor.
	 *
	 * @param profile SpagoBIUserProfile
	 */
	public UserProfile(SpagoBIUserProfile profile) {
		logger.debug("IN");
		this.setSpagoBIUserProfile(profile);
		this.userUniqueIdentifier = profile.getUniqueIdentifier();
		this.userName = profile.getUserName();
		this.userId = profile.getUserId();
		this.organization = profile.getOrganization();
		this.isSuperadmin = profile.getIsSuperadmin();
		roles = new ArrayList();
		if (profile.getRoles() != null) {
			int l = profile.getRoles().length;
			for (int i = 0; i < l; i++) {
				logger.debug("ROLE:" + profile.getRoles()[i]);
				roles.add(profile.getRoles()[i]);
			}

		}
		functionalities = new ArrayList();
		if (profile.getFunctions() != null) {
			int l = profile.getFunctions().length;
			for (int i = 0; i < l; i++) {
				logger.debug("USER FUNCTIONALITY:" + profile.getFunctions()[i]);
				functionalities.add(profile.getFunctions()[i]);
			}
		}

		userAttributes = profile.getAttributes();
		if (userAttributes != null) {
			logger.debug("USER ATTRIBUTES----");
			Set keis = userAttributes.keySet();
			Iterator iter = keis.iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				logger.debug(key + "=" + userAttributes.get(key));
			}
			logger.debug("USER ATTRIBUTES----");
		} else {
			userAttributes = new HashMap();
			logger.debug("NO USER ATTRIBUTES");
		}

		setPredefinedProfileAttributes();

		logger.debug("OUT");
	}

	private void setPredefinedProfileAttributes() {
		// putting user id as a predefined profile attribute:
		userAttributes.put(PREDEFINED_PROFILE_ATTRIBUTES.USER_ID.getName(), this.userId);
		// putting user roles as a predefined profile attribute:
		userAttributes.put(PREDEFINED_PROFILE_ATTRIBUTES.USER_ROLES.getName(), concatenateRolesForINClause());
		// putting tenant id as a predefined profile attribute:
		userAttributes.put(PREDEFINED_PROFILE_ATTRIBUTES.TENANT_ID.getName(), this.organization);
	}

	private String concatenateRolesForINClause() {
		return "'" + StringUtils.join(this.roles, "','") + "'";
	}

	public UserProfile(String userUniqueIdentifier, String userId, String userName, String organization) {
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.userId = userId;
		this.userName = userName;
		this.organization = organization;
		this.roles = new ArrayList();
		this.functionalities = new ArrayList();
		this.userAttributes = new HashMap();
		setPredefinedProfileAttributes();
	}

	public UserProfile(String userId, String organization) {
		this(userId, userId, userId, organization);
	}

	/**
	 * To be used by SpagoBI core ONLY. The user unique identifier in the output object is a JWT token expiring SCHEDULER_JWT_TOKEN_EXPIRE_HOURS hours containing a
	 * claim SsoServiceInterface.USER_ID: the value of this claim matches this syntax: SCHEDULER_USER_ID_PREFIX + tenant name.
	 *
	 * @return the user profile for the scheduler
	 */
	public static final UserProfile createSchedulerUserProfile() {
		Tenant tenant = TenantManager.getTenant();
		if (tenant == null) {
			throw new SpagoBIRuntimeException("Tenant not found!!!");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, SCHEDULER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();

		String organization = tenant.getName();
		String jwtToken = JWTSsoService.userId2jwtToken(SCHEDULER_USER_ID_PREFIX + organization, expiresAt);
		// String userUniqueIdentifier = SCHEDULER_USER_ID_PREFIX + organization;
		UserProfile profile = new UserProfile(jwtToken, SCHEDULER_USER_NAME, SCHEDULER_USER_NAME, organization);
		profile.roles = new ArrayList();
		profile.userAttributes = new HashMap();
		return profile;
	}

	/**
	 * Checks if is scheduler user.
	 *
	 * @return true, if checks if is scheduler user
	 */
	public static boolean isSchedulerUser(String jwtToken) {
		if (jwtToken == null) {
			return false;
		}
		try {
			String userId = JWTSsoService.jwtToken2userId(jwtToken);
			return userId.startsWith(SCHEDULER_USER_ID_PREFIX);
		} catch (Exception e) {
			logger.debug("Error reading jwttoken for schedulatio. Are you using a sso?", e);
			return false;
		}

	}

	/**
	 * Checks if is data preparation user.
	 *
	 * @return true, if checks if is preparation user
	 */
	public static boolean isDataPreparationUser(String jwtToken) {
		if (jwtToken == null) {
			return false;
		}
		try {
			String userId = JWTSsoService.jwtToken2userId(jwtToken);
			return userId.startsWith(DATA_PREP_USER_ID_PREFIX);
		} catch (Exception e) {
			logger.debug("Error reading jwttoken for schedulatio. Are you using a sso?", e);
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getFunctionalities()
	 */
	@Override
	public Collection getFunctionalities() throws EMFInternalError {
		return functionalities;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getFunctionalitiesByRole(java.lang.String)
	 */
	@Override
	public Collection getFunctionalitiesByRole(String arg0) throws EMFInternalError {
		return new ArrayList();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getRoles()
	 */
	@Override
	public Collection getRoles() throws EMFInternalError {
		return this.roles;
	}

	/*
	 * All roles are returned
	 */
	public Collection getRolesForUse() throws EMFInternalError {
		logger.debug("IN");
		Collection toReturn = this.roles;
		logger.debug("OUT");
		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getUserAttribute(java.lang.String)
	 */
	@Override
	public Object getUserAttribute(String attributeName) throws EMFInternalError {
		return userAttributes.get(attributeName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getUserAttributeNames()
	 */
	@JsonIgnore
	@Override
	public Collection getUserAttributeNames() {
		return userAttributes.keySet();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getUserUniqueIdentifier()
	 */
	@Override
	public Object getUserUniqueIdentifier() {
		return userUniqueIdentifier;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getUserName()
	 */
	public Object getUserName() {
		String retVal = userName;
		if (retVal == null)
			retVal = userUniqueIdentifier;
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#getUserId()
	 */
	public Object getUserId() {
		String retVal = userId;
		if (retVal == null)
			retVal = userUniqueIdentifier;
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#hasRole(java.lang.String)
	 */
	@Override
	public boolean hasRole(String roleName) throws EMFInternalError {
		return this.roles.contains(roleName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#isAbleToExecuteAction(java.lang.String)
	 */
	@Override
	public boolean isAbleToExecuteAction(String actionName) throws EMFInternalError {
		if (functionalities != null) {
			if (functionalities.contains(actionName)) {
				return true;
			}

			List<String> businessProcessNames = AuthorizationsBusinessMapper.getInstance()
					.mapActionToBusinessProcess(actionName);
			if (businessProcessNames != null) {
				for (String businessProcess : businessProcessNames) {
					if (functionalities.contains(businessProcess)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 *
	 * @see it.eng.spago.security.IEngUserProfile#isAbleToExecuteModuleInPage(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isAbleToExecuteModuleInPage(String pageName, String moduleName) throws EMFInternalError {
		String functionality = AuthorizationsBusinessMapper.getInstance().mapPageModuleToBusinessProcess(pageName,
				moduleName);
		if (functionality != null) {
			return this.functionalities.contains(functionality);
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#setApplication(java.lang.String)
	 */
	@Override
	public void setApplication(String arg0) throws EMFInternalError {
	}

	/**
	 * Sets the functionalities.
	 *
	 * @param functs the new functionalities
	 */
	public void setFunctionalities(Collection functs) {
		this.functionalities = functs;
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attrs the new attributes
	 */
	public void setAttributes(Map attrs) {
		this.userAttributes = attrs;
	}

	/**
	 * Adds an attribute.
	 */
	public void addAttributes(String key, Object value) {
		this.userAttributes.put(key, value);
	}

	/**
	 * Modify an attribute value
	 */
	public void setAttributeValue(String key, Object value) {
		this.userAttributes.remove(key);
		this.userAttributes.put(key, value);
	}

	/**
	 * Sets the roles.
	 *
	 * @param rols the new roles
	 */
	public void setRoles(Collection rols) {
		this.roles = rols;
	}

	public Map getUserAttributes() {
		return userAttributes;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public SpagoBIUserProfile getSpagoBIUserProfile() {
		return spagoBIUserProfile;
	}

	public void setSpagoBIUserProfile(SpagoBIUserProfile spagoBIUserProfile) {
		this.spagoBIUserProfile = spagoBIUserProfile;
	}

	public Boolean getIsSuperadmin() {
		return isSuperadmin;
	}

	public void setIsSuperadmin(Boolean isSuperadmin) {
		this.isSuperadmin = isSuperadmin;
	}

	/**
	 * To be used by external engines ONLY. The user unique identifier must be a JWT token expiring in SCHEDULER_JWT_TOKEN_EXPIRE_HOURS hours containing a claim
	 * SsoServiceInterface.USER_ID: the value of this claim must match this syntax: SCHEDULER_USER_ID_PREFIX + tenant name.
	 *
	 * @param userUniqueIdentifier The JWT token containing a claim SsoServiceInterface.USER_ID with value SCHEDULER_USER_ID_PREFIX + tenant name
	 * @return the user profile for the scheduler
	 */
	public static UserProfile createSchedulerUserProfile(String userUniqueIdentifier) {
		logger.debug("IN: userUniqueIdentifier = " + userUniqueIdentifier);
		if (!isSchedulerUser(userUniqueIdentifier)) {
			throw new SpagoBIRuntimeException(
					"User unique identifier [" + userUniqueIdentifier + "] is not a scheduler user id");
		}
		String userId = JWTSsoService.jwtToken2userId(userUniqueIdentifier);
		logger.debug("IN: user id = " + userId);
		String organization = userId.substring(SCHEDULER_USER_ID_PREFIX.length());
		logger.debug("Organization : " + organization);

		List<String> functionalities = getSchedulerUserFunctionalities();
		List<String> roles = new ArrayList<>();
		HashMap attributes = new HashMap();

		UserProfile toReturn = new UserProfile(userUniqueIdentifier, SCHEDULER_USER_NAME, SCHEDULER_USER_NAME,
				organization);
		toReturn.setRoles(roles);
		toReturn.setAttributes(attributes);
		toReturn.setFunctionalities(functionalities);

		setSpagoBiUserProfileIntoUserProfile(toReturn, userUniqueIdentifier, userId, organization, roles,
				functionalities);

		logger.debug("OUT");
		return toReturn;
	}

	public static UserProfile createDataPreparationUserProfile(String userUniqueIdentifier) {
		logger.debug("IN: userUniqueIdentifier = " + userUniqueIdentifier);
		if (!isDataPreparationUser(userUniqueIdentifier)) {
			throw new SpagoBIRuntimeException(
					"User unique identifier [" + userUniqueIdentifier + "] is not a data preparation user id");
		}
		String userId = JWTSsoService.jwtToken2userId(userUniqueIdentifier);
		logger.debug("IN: user id = " + userId);
		String organization = userId.substring(DATA_PREP_USER_ID_PREFIX.length());
		logger.debug("Organization : " + organization);

		List<String> functionalities = getDataPreparationUserFunctionalities();
		List<String> roles = new ArrayList<>();
		HashMap attributes = new HashMap();

		UserProfile toReturn = new UserProfile(userUniqueIdentifier, DATA_PREP_USER_NAME, DATA_PREP_USER_NAME,
				organization);
		toReturn.setRoles(roles);
		toReturn.setAttributes(attributes);
		toReturn.setFunctionalities(functionalities);

		setSpagoBiUserProfileIntoUserProfile(toReturn, userUniqueIdentifier, userId, organization, roles,
				functionalities);

		logger.debug("OUT");
		return toReturn;
	}

	private static void setSpagoBiUserProfileIntoUserProfile(UserProfile userProfile, String userUniqueIdentifier,
			String userId, String organization, List<String> roles, List<String> functionalities) {

		HashMap attributes = new HashMap();
		String[] functionalitiesAsArray = functionalities.toArray(new String[0]);
		String[] rolesAsArray = roles.toArray(new String[0]);
		boolean isSuperadmin = false;

		SpagoBIUserProfile spagoBiUserProfile = new SpagoBIUserProfile(attributes, functionalitiesAsArray, isSuperadmin,
				organization, rolesAsArray, userUniqueIdentifier, userId, userId);

		userProfile.setSpagoBIUserProfile(spagoBiUserProfile);
	}

	private static List<String> getSchedulerUserFunctionalities() {
		String[] functionalities = { ALERT_MANAGEMENT, MANAGE_ANALYTICAL_WIDGET, ARTIFACT_CATALOGUE_MANAGEMENT,
				MANAGE_CHART_WIDGET, CKAN_FUNCTIONALITY, CONTSTRAINT_MANAGEMENT, CONTSTRAINT_VIEW,
				CREATE_CHART_FUNCTIONALITY, CREATE_COCKPIT_FUNCTIONALITY, CREATE_DATASETS_AS_FINAL_USER,
				DATASET_MANAGEMENT, DATASOURCE_BIG_DATA, DATASOURCE_MANAGEMENT, DATASOURCE_READ,
				DISTRIBUTIONLIST_MANAGEMENT, DISTRIBUTIONLIST_USER, DOCUMENT_ADMINISTRATION, DOCUMENT_MANAGEMENT_ADMIN,
				DOCUMENT_DELETE_MANAGEMENT, DOCUMENT_DETAIL_MANAGEMENT, DOCUMENT_MANAGEMENT_DEV, DOCUMENT_MANAGEMENT,
				DOCUMENT_METADATA_MANAGEMENT, DOCUMENT_MOVE_DOWN_STATE, DOCUMENT_MOVE_UP_STATE,
				DOCUMENT_STATE_MANAGEMENT, DOCUMENT_MANAGEMENT_TEST, DOCUMENT_MANAGEMENT_USER, DOMAIN_WRITE,
				EVENTS_MANAGEMENT, EXECUTE_CROSS_NAVIGATION, FEDERATION_DEFINITION, FUNCTIONALITIES_MANAGEMENT,
				FUNCTIONS_CATALOG_MANAGEMENT, GEO_LAYERS_MANAGEMENT, GIS_WEB_DESIGNER, HOTLINK_MANAGEMENT,
				IMAGES_MANAGEMENT, KPI_MANAGEMENT, KPI_SCHEDULATION, LOVS_MANAGEMENT, LOVS_VIEW,
				MANAGE_CROSS_NAVIGATION, MAPCATALOGUE_MANAGEMENT, MENU_MANAGEMENT, META_MODEL_LIFECYCLE_MANAGEMENT,
				META_MODELS_CATALOGUE_MANAGEMENT, MODIFY_REFRESH, MANAGE_MULTISHEET_COCKPIT,
				NOTIFY_CONTEXT_BROKER_ACTION, PARAMETER_MANAGEMENT, PARAMETER_VIEW, PROFILE_ATTRIBUTES_MANAGEMENT,
				PROFILE_MANAGEMENT, READ_ENGINES_MANAGEMENT, REGISTRY_DATA_ENTRY, SELF_SERVICE_DATASET_MANAGEMENT,
				SELF_SERVICE_META_MODEL_MANAGEMENT, SHARED_DEVELOPMENT, MANAGE_STATIC_WIDGET,
				SYNCRONIZE_ROLES_MANAGEMENT, USER_SAVE_DOCUMENT_FUNCTIONALITY, VIEW_MY_FOLDER_ADMIN,
				WORKLIST_MANAGEMENT, WORKSPACE_MANAGEMENT, READ_ROLES };
		return Arrays.asList(functionalities);
	}

	private static List<String> getDataPreparationUserFunctionalities() {
		String[] functionalities = { DATA_PREPARATION };
		return Arrays.asList(functionalities);
	}

	@Override
	public String toString() {
		return "UserProfile [userUniqueIdentifier=" + userUniqueIdentifier + ", userId=" + userId + ", userName="
				+ userName + ", userAttributes=" + userAttributes + ", roles=" + roles + ", organization="
				+ organization + ", isSuperadmin=" + isSuperadmin + "]";
	}

}