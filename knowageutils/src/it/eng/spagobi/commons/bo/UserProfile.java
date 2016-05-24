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

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.security.AuthorizationsBusinessMapper;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class contain the information about the user
 */
public class UserProfile implements IEngUserProfile {

	private static transient Logger logger = Logger.getLogger(UserProfile.class);

	private static String WORKFLOW_USER_NAME = "[SYSTEM - WORKFLOW]";
	private static String WORKFLOW_USER_ID_PREFIX = "[SYSTEM - WORKFLOW] - ";
	private static String SCHEDULER_USER_NAME = "scheduler";
	private static String SCHEDULER_USER_ID_PREFIX = "scheduler - ";
	private static final String PUBLIC_FUNCTIONALITY = "publicFunctionality";

	private String userUniqueIdentifier = null;
	private String userId = null;
	private String userName = null;
	private Map userAttributes = null;
	private Collection roles = null;
	private Collection functionalities = null;
	private String defaultRole = null;
	private String organization = null;
	private Boolean isSuperadmin = null;

	@JsonIgnore
	private SpagoBIUserProfile spagoBIUserProfile = null;

	/**
	 * The Constructor.
	 *
	 * @param profile
	 *            SpagoBIUserProfile
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
		// putting tenant id on user attributes (for Spago modules' queries) :
		userAttributes.put(SpagoBIConstants.TENANT_ID, this.organization);

		logger.debug("OUT");
	}

	public UserProfile(String userUniqueIdentifier, String userId, String userName, String organization) {
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.userId = userId;
		this.userName = userName;
		this.organization = organization;
	}

	public UserProfile(String userId, String organization) {
		this.userUniqueIdentifier = userId;
		this.userId = userId;
		this.userName = userId;
		this.organization = organization;
	}

	/**
	 * To be user by SpagoBI core ONLY. The user identifier in the output object will match this syntax: WORKFLOW_USER_ID_PREFIX + tenant name
	 *
	 * @return the user profile for the workflow
	 */
	public static final UserProfile createWorkFlowUserProfile() {
		Tenant tenant = TenantManager.getTenant();
		if (tenant == null) {
			throw new SpagoBIRuntimeException("Tenant not found!!!");
		}
		String organization = tenant.getName();
		String userUniqueIdentifier = WORKFLOW_USER_ID_PREFIX + organization;
		UserProfile profile = new UserProfile(userUniqueIdentifier, WORKFLOW_USER_NAME, WORKFLOW_USER_NAME, organization);
		profile.roles = new ArrayList();
		profile.userAttributes = new HashMap();
		return profile;
	}

	/**
	 * To be user by SpagoBI core ONLY. The user identifier in the output object will match this syntax: SCHEDULER_USER_ID_PREFIX + tenant name
	 *
	 * @return the user profile for the scheduler
	 */
	public static final UserProfile createSchedulerUserProfile() {
		Tenant tenant = TenantManager.getTenant();
		if (tenant == null) {
			throw new SpagoBIRuntimeException("Tenant not found!!!");
		}
		String organization = tenant.getName();
		String userUniqueIdentifier = SCHEDULER_USER_ID_PREFIX + organization;
		UserProfile profile = new UserProfile(userUniqueIdentifier, SCHEDULER_USER_NAME, SCHEDULER_USER_NAME, organization);
		profile.roles = new ArrayList();
		profile.userAttributes = new HashMap();
		return profile;
	}

	/**
	 * Checks if is scheduler user.
	 *
	 * @param userId
	 *            String
	 *
	 * @return true, if checks if is scheduler user
	 */
	public static boolean isWorkflowUser(String userId) {
		// return WORKFLOW_USER_NAME.equals(userid);
		return userId != null && userId.startsWith(WORKFLOW_USER_ID_PREFIX);
	}

	/**
	 * Checks if is scheduler user.
	 *
	 * @param userId
	 *            String
	 *
	 * @return true, if checks if is scheduler user
	 */
	public static boolean isSchedulerUser(String userId) {
		// return SCHEDULER_USER_NAME.equals(userid);
		return userId != null && userId.startsWith(SCHEDULER_USER_ID_PREFIX);
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
	 * if a role default is assigned return it, else returns all roles
	 */
	public Collection getRolesForUse() throws EMFInternalError {
		logger.debug("IN");
		Collection toReturn = null;
		logger.debug("look if default role is selected");
		if (defaultRole != null) {
			logger.debug("default role selected is " + defaultRole);
			toReturn = new ArrayList<String>();
			toReturn.add(defaultRole);
		} else {
			logger.debug("default role not selected");

			toReturn = this.roles;
		}

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
		// first check if the actionName is a functionality...
		if (functionalities != null && functionalities.contains(actionName)) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.security.IEngUserProfile#isAbleToExecuteAction(java.lang.String)
	 */
	public boolean isAbleToExecuteService(String serviceUrl) throws EMFInternalError {
		// first check if the actionName is a functionality...
		if (this.functionalities.contains(serviceUrl)) {
			return true;
		}
		String functionality = AuthorizationsBusinessMapper.getInstance().mapServiceToBusinessProcess(serviceUrl);

		if (functionality != null) {
			if (functionality.equals(PUBLIC_FUNCTIONALITY)) {
				return true;
			}
			return this.functionalities.contains(functionality);
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
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
	 * @param functs
	 *            the new functionalities
	 */
	public void setFunctionalities(Collection functs) {
		this.functionalities = functs;
	}

	/**
	 * Sets the attributes.
	 *
	 * @param attrs
	 *            the new attributes
	 */
	public void setAttributes(Map attrs) {
		this.userAttributes = attrs;
	}

	/**
	 * Adds an attribute.
	 *
	 * @param attrs
	 *            the new attributes
	 */
	public void addAttributes(String key, Object value) {
		this.userAttributes.put(key, value);
	}

	/**
	 * Modify an attribute value
	 *
	 * @param attrs
	 *            the new attributes
	 */
	public void setAttributeValue(String key, Object value) {
		this.userAttributes.remove(key);
		this.userAttributes.put(key, value);
	}

	/**
	 * Sets the roles.
	 *
	 * @param rols
	 *            the new roles
	 */
	public void setRoles(Collection rols) {
		this.roles = rols;
	}

	public String getDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(String defaultRole) {
		logger.debug("IN " + defaultRole);
		this.defaultRole = defaultRole;
		logger.debug("OUT");
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
	 * To be user by external engines ONLY. The user identifier must match this syntax: SCHEDULER_USER_ID_PREFIX + tenant name
	 *
	 * @param userUniqueIdentifier
	 *            The user identifier (SCHEDULER_USER_ID_PREFIX + tenant name)
	 * @return the user profile for the scheduler
	 */
	public static UserProfile createSchedulerUserProfile(String userUniqueIdentifier) {
		logger.debug("IN: userUniqueIdentifier = " + userUniqueIdentifier);
		if (!isSchedulerUser(userUniqueIdentifier)) {
			throw new SpagoBIRuntimeException("User unique identifier [" + userUniqueIdentifier + "] is not a scheduler user id");
		}
		String actualUserId = SCHEDULER_USER_NAME;
		String userName = SCHEDULER_USER_NAME;
		String organization = userUniqueIdentifier.substring(SCHEDULER_USER_ID_PREFIX.length());
		logger.debug("Organization : " + organization);
		UserProfile toReturn = new UserProfile(userUniqueIdentifier, actualUserId, userName, organization);
		toReturn.setRoles(new ArrayList());
		toReturn.setAttributes(new HashMap());
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * To be user by external engines ONLY. The user identifier must match this syntax: WORKFLOW_USER_ID_PREFIX + tenant name
	 *
	 * @param userUniqueIdentifier
	 *            The user identifier (WORKFLOW_USER_ID_PREFIX + tenant name)
	 * @return the user profile for the workflow
	 */
	public static UserProfile createWorkflowUserProfile(String userUniqueIdentifier) {
		logger.debug("IN: userUniqueIdentifier = " + userUniqueIdentifier);
		if (!isWorkflowUser(userUniqueIdentifier)) {
			throw new SpagoBIRuntimeException("User unique identifier [" + userUniqueIdentifier + "] is not a workflow user id");
		}
		String actualUserId = WORKFLOW_USER_NAME;
		String userName = WORKFLOW_USER_NAME;
		String organization = userUniqueIdentifier.substring(WORKFLOW_USER_ID_PREFIX.length());
		logger.debug("Organization : " + organization);
		UserProfile toReturn = new UserProfile(userUniqueIdentifier, actualUserId, userName, organization);
		toReturn.setRoles(new ArrayList());
		toReturn.setAttributes(new HashMap());
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public String toString() {
		return "UserProfile [userUniqueIdentifier=" + userUniqueIdentifier + ", userId=" + userId + ", userName=" + userName + ", userAttributes="
				+ userAttributes + ", roles=" + roles + ", defaultRole=" + defaultRole + ", organization=" + organization + ", isSuperadmin=" + isSuperadmin
				+ "]";
	}

}
