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
	};

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
		super();
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
	 * To be used by SpagoBI core ONLY. The user unique identifier in the output object is a JWT token expiring SCHEDULER_JWT_TOKEN_EXPIRE_HOURS hours
	 * containing a claim SsoServiceInterface.USER_ID: the value of this claim matches this syntax: SCHEDULER_USER_ID_PREFIX + tenant name.
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

			List<String> businessProcessNames = AuthorizationsBusinessMapper.getInstance().mapActionToBusinessProcess(actionName);
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
		String functionality = AuthorizationsBusinessMapper.getInstance().mapPageModuleToBusinessProcess(pageName, moduleName);
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
			throw new SpagoBIRuntimeException("User unique identifier [" + userUniqueIdentifier + "] is not a scheduler user id");
		}
		String userId = JWTSsoService.jwtToken2userId(userUniqueIdentifier);
		logger.debug("IN: user id = " + userId);
		String organization = userId.substring(SCHEDULER_USER_ID_PREFIX.length());
		logger.debug("Organization : " + organization);
		UserProfile toReturn = new UserProfile(userUniqueIdentifier, SCHEDULER_USER_NAME, SCHEDULER_USER_NAME, organization);
		toReturn.setRoles(new ArrayList());
		toReturn.setAttributes(new HashMap());
		toReturn.setFunctionalities(getSchedulerUserFunctionalities());
		logger.debug("OUT");
		return toReturn;
	}

	public static UserProfile createDataPreparationUserProfile(String userUniqueIdentifier) {
		logger.debug("IN: userUniqueIdentifier = " + userUniqueIdentifier);
		if (!isDataPreparationUser(userUniqueIdentifier)) {
			throw new SpagoBIRuntimeException("User unique identifier [" + userUniqueIdentifier + "] is not a data preparation user id");
		}
		String userId = JWTSsoService.jwtToken2userId(userUniqueIdentifier);
		logger.debug("IN: user id = " + userId);
		String organization = userId.substring(DATA_PREP_USER_ID_PREFIX.length());
		logger.debug("Organization : " + organization);
		UserProfile toReturn = new UserProfile(userUniqueIdentifier, DATA_PREP_USER_NAME, DATA_PREP_USER_NAME, organization);
		toReturn.setRoles(new ArrayList());
		toReturn.setAttributes(new HashMap());
		toReturn.setFunctionalities(getDataPreparationUserFunctionalities());
		logger.debug("OUT");
		return toReturn;
	}

	private static Collection getSchedulerUserFunctionalities() {
		String[] functionalities = { "AlertManagement", "AnalyticalWidget", "ArtifactCatalogueManagement", "ChartWidget", "CkanIntegrationFunctionality",
				"ConstraintManagement", "ConstraintView", "CreateChartFunctionality", "CreateCockpitFunctionality", "CreateDatasetsAsFinalUser",
				"DatasetManagement", "DataSourceBigData", "DataSourceManagement", "DataSourceRead", "DistributionListManagement", "DistributionListUser",
				"DocumentAdministration", "DocumentAdminManagement", "DocumentDeleteManagement", "DocumentDetailManagement", "DocumentDevManagement",
				"DocumentManagement", "DocumentMetadataManagement", "DocumentMoveDownState", "DocumentMoveUpState", "DocumentStateManagement",
				"DocumentTestManagement", "DocumentUserManagement", "DomainWrite", "EventsManagement", "ExecuteCrossNavigation", "FederationDefinition",
				"FunctionalitiesManagement", "FunctionsCatalogManagement", "GeoLayersManagement", "GisWebDesigner", "HotLinkManagement", "ImagesManagement",
				"KpiManagement", "KpiSchedulation", "LovsManagement", "LovsView", "ManageCrossNavigation", "MapCatalogueManagement", "MenuManagement",
				"MetaModelLifecycleManagement", "MetaModelsCatalogueManagement", "ModifyRefresh", "MultisheetCockpit", "NotifyContextBrokerAction",
				"ParameterManagement", "ParameterView", "ProfileAttributeManagement", "ProfileManagement", "ReadEnginesManagement", "RegistryDataEntry",
				"SelfServiceDatasetManagement", "SelfServiceMetaModelManagement", "SharedDevelopment", "StaticWidget", "SyncronizeRolesManagement",
				"UserSaveDocumentFunctionality", "ViewMyFolderAdmin", "WorklistManagement", "WorkspaceManagement", "ReadRoles" };
		return Arrays.asList(functionalities);
	}

	private static Collection getDataPreparationUserFunctionalities() {
		String[] functionalities = { "DataPreparation" };
		return Arrays.asList(functionalities);
	}

	@Override
	public String toString() {
		return "UserProfile [userUniqueIdentifier=" + userUniqueIdentifier + ", userId=" + userId + ", userName=" + userName + ", userAttributes="
				+ userAttributes + ", roles=" + roles + ", organization=" + organization + ", isSuperadmin=" + isSuperadmin + "]";
	}

}