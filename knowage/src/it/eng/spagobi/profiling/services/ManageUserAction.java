/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.profiling.services;

import it.eng.qbe.query.CriteriaConstants;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.validation.SpagoBIValidationImpl;
import it.eng.spagobi.community.bo.CommunityManager;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.dao.QueryStaticFilter;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.profiling.dao.filters.FinalUsersFilter;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 * @author Alessandro Pegoraro (alessandro.pegoraro@eng.it)
 */
public class ManageUserAction extends AbstractSpagoBIAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8920524215721282986L;
	// logger component
	private static Logger logger = Logger.getLogger(ManageUserAction.class);
	private final String MESSAGE_DET = "MESSAGE_DET";
	// type of service
	private final String USERS_LIST = "USERS_LIST";
	private final String USER_INSERT = "USER_INSERT";
	private final String USER_DELETE = "USER_DELETE";

	// USER detail
	private final String ID = "id";
	private final String USER_ID = "userId";
	private final String FULL_NAME = "fullName";
	private final String PASSWORD = "pwd";

	private final String ROLES = "userRoles";
	private final String ATTRIBUTES = "userAttributes";

	public static String START = "start";
	public static String LIMIT = "limit";
	public static Integer START_DEFAULT = 0;
	public static Integer LIMIT_DEFAULT = 15;
	public static String FILTERS = "FILTERS";

	@Override
	public void doService() {
		logger.debug("IN");
		ISbiUserDAO userDao;
		try {

			userDao = DAOFactory.getSbiUserDAO();
			userDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e) {
			logger.error("Error occurred while initializating DAO", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Error occurred while initializating DAO", e);
		}

		String serviceType = this.getAttributeAsString(MESSAGE_DET);
		logger.debug("Service type " + serviceType);
		if (serviceType != null && serviceType.equalsIgnoreCase(USERS_LIST)) {
			getUsersList(userDao);
		} else if (serviceType != null && serviceType.equalsIgnoreCase(USER_INSERT)) {
			saveUser(userDao);
		} else if (serviceType != null && serviceType.equalsIgnoreCase(USER_DELETE)) {
			deleteUser(userDao);
		} else if (serviceType == null) {
			setAttributesAndRolesInResponse();
		}
		logger.debug("OUT");

	}

	protected void getUsersList(ISbiUserDAO userDao) {

		try {
			Integer start = this.getStart();
			logger.debug("Start : " + start);
			Integer limit = this.getLimit();
			logger.debug("Limit : " + limit);

			QueryFilters filters = this.getQueryFilter();
			PagedList<UserBO> usersPagedList = userDao.loadUsersPagedList(filters, start, limit);
			logger.debug("Loaded users list");

			JSONObject usersResponseJSON = createJSONResponseUsers(usersPagedList);
			writeBackToClient(new JSONSuccess(usersResponseJSON));
		} catch (Throwable e) {
			logger.error("Exception occurred while retrieving users", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while retrieving users", e);
		}
	}

	private Integer getStart() {
		Integer start = getAttributeAsInteger(START);
		if (start == null) {
			start = START_DEFAULT;
		}
		return start;
	}

	private Integer getLimit() {
		Integer limit = getAttributeAsInteger(LIMIT);
		if (limit == null) {
			limit = LIMIT_DEFAULT;
		}
		return limit;
	}

	private QueryFilters getQueryFilter() throws Exception {
		QueryFilters toReturn = new QueryFilters();
		// static filter by list's toolbar
		QueryStaticFilter filter = this.getStaticFilter();
		if (filter != null) {
			toReturn.add(filter);
		}
		IEngUserProfile profile = this.getUserProfile();
		if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT)) {
			// administrator: he can see every user
		} else {
			// user with FINAL_USERS_MANAGEMENT (users with neither
			// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked by the
			// business_map.xml therefore they cannot execute this action)
			toReturn.add(new FinalUsersFilter());
		}
		return toReturn;
	}

	protected void saveUser(ISbiUserDAO userDao) {
		UserProfile profile = (UserProfile) this.getUserProfile();
		boolean insertModality = true;
		HashMap<String, String> logParam = new HashMap();
		try {
			Integer id = getAttributeAsInteger(ID);
			if (id != null && id > 0) {
				insertModality = false;
				// modifying an existing user.
				// We must load user to check if user belongs to the right
				// tenant,
				// since Hibernate 3.6 puts tenant filter on select, not on
				// delete
				SbiUser user = userDao.loadSbiUserById(id);
				if (user != null) {
					this.checkIfCurrentUserIsAbleToSaveOrModifyUser(user);
				} else {
					logParam.put("USER ID", id.toString());
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.MODIFY", logParam, "KO");
					throw new SpagoBIServiceException(SERVICE_NAME, "User with id = " + user + " does not exists or he belongs to another tenant");
				}
			}

			String userId = getAttributeAsString(USER_ID);
			String fullName = getAttributeAsString(FULL_NAME);
			String password = getAttributeAsString(PASSWORD);
			logParam.put("FULLNAME", fullName);

			if (userId == null) {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS." + ((insertModality) ? "ADD" : "MODIFY"), logParam, "KO");
				logger.error("User name missing");
				throw new SpagoBIServiceException(SERVICE_NAME, "User name missing");
			}

			SbiUser user = new SbiUser();
			if (id != null) {
				user.setId(id);
			}
			user.setUserId(userId);
			user.setFullName(fullName);
			if (password != null && password.length() > 0) {
				try {
					user.setPassword(Password.encriptPassword(password));
				} catch (Exception e) {
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS." + ((insertModality) ? "ADD" : "MODIFY"), logParam, "KO");
					logger.error("Impossible to encrypt Password", e);
					throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to encrypt Password", e);
				}
			}

			try {
				deserializeAttributesJSONArray(user);
				deserializeRolesJSONArray(user);

			} catch (JSONException e) {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS" + ((insertModality) ? "ADD" : "MODIFY"), logParam, "ERR");
				throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while deserializing attributes and roles", e);
			}

			this.checkIfCurrentUserIsAbleToSaveOrModifyUser(user);

			// check if user id is valid: in case it is not, an exception will
			// be thrown
			checkUserId(userId, id);

			try {
				id = userDao.fullSaveOrUpdateSbiUser(user);

				CommunityManager cm = new CommunityManager();
				String commName = getCommunityAttr(user);
				if (commName != null && !commName.equals("")) {
					SbiCommunity community = DAOFactory.getCommunityDAO().loadSbiCommunityByName(commName);
					cm.saveCommunity(community, commName, userId, getHttpRequest());
				}

				logger.debug("User updated or Inserted");
			} catch (Throwable t) {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS." + ((insertModality) ? "ADD" : "MODIFY"), logParam, "KO");
				logger.error("Exception occurred while saving user", t);
				throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while saving user", t);
			}

			try {
				JSONObject attributesResponseSuccessJSON = new JSONObject();
				attributesResponseSuccessJSON.put("success", true);
				attributesResponseSuccessJSON.put("responseText", "Operation succeded");
				attributesResponseSuccessJSON.put("id", id);
				writeBackToClient(new JSONSuccess(attributesResponseSuccessJSON));
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS." + ((insertModality) ? "ADD" : "MODIFY"), logParam, "OK");
			} catch (Exception e) {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS." + ((insertModality) ? "ADD" : "MODIFY"), logParam, "KO");
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			}

		} catch (SpagoBIServiceException e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS." + ((insertModality) ? "ADD" : "MODIFY"), logParam, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e;
		} catch (Throwable e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS" + ((insertModality) ? "ADD" : "MODIFY"), logParam, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.error("Exception occurred while saving user", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while saving user", e);
		}
	}

	private String getCommunityAttr(SbiUser user) throws Exception {
		String communityName = "";
		JSONArray attributesJSON = getAttributeAsJSONArray(ATTRIBUTES);
		for (int i = 0; i < attributesJSON.length(); i++) {
			JSONObject obj = (JSONObject) attributesJSON.get(i);
			Integer key = obj.getInt("id");
			String value = obj.getString("value");

			String name = obj.getString("name");

			if (name.equalsIgnoreCase("community")) {
				communityName = obj.getString("value");
			}

		}
		return communityName;

	}

	protected void checkIfCurrentUserIsAbleToSaveOrModifyUser(SbiUser user) throws EMFUserError, EMFInternalError {
		UserProfile profile = (UserProfile) this.getUserProfile();
		if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT)) {
			// administrator: he can modify every user
		} else {
			// user with FINAL_USERS_MANAGEMENT (users with neither
			// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked by the
			// business_map.xml therefore they cannot execute this action)
			if (!this.isFinalUser(user)) {
				logger.error("User [" + profile.getUserId() + "] cannot save or modify user [" + user.getUserId() + "] since the latter is not a final user");
				throw new SpagoBIServiceException(SERVICE_NAME, "Cannot save or modify user");
			}
		}
	}

	protected void deleteUser(ISbiUserDAO userDao) {
		Integer id = getAttributeAsInteger(ID);
		UserProfile profile = (UserProfile) this.getUserProfile();
		try {
			// we must load user to check if user belongs to the right tenant,
			// since Hibernate 3.6 does not put tenant filter on delete
			SbiUser user = userDao.loadSbiUserById(id);
			HashMap<String, String> logParam = new HashMap();
			logParam.put("FULLNAME", user.getFullName());

			if (user != null) {
				if (user.getUserId().equals(profile.getUserId())) {
					AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.DELETE", logParam, "KO");
					// user deleting himself!
					throw new SpagoBIServiceException(SERVICE_NAME, "You cannot delete yourself!");
				}
				if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT)) {
					// administrator: he can delete every user
				} else {
					// user with FINAL_USERS_MANAGEMENT (users with neither
					// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked
					// by the
					// business_map.xml therefore they cannot execute this
					// action)
					// He can delete only final users
					if (!this.isFinalUser(user)) {
						AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.DELETE", logParam, "KO");
						logger.error("User [" + profile.getUserId() + "] cannot delete user [" + user.getUserId() + "]  since it is not a final user");
						throw new SpagoBIServiceException(SERVICE_NAME, "Cannot delete user");
					}
				}
				CommunityManager cm = new CommunityManager();
				cm.mngUserCommunityAfterDelete(user);
				logger.debug("User-community membership deleted");

				userDao.deleteSbiUserById(id);
				logger.debug("User deleted");

				writeBackToClient(new JSONAcknowledge("Operation succeded"));
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.DELETE", logParam, "OK");
			} else {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.DELETE", logParam, "KO");
				throw new SpagoBIServiceException(SERVICE_NAME, "User with id = " + id + " does not exists or it belongs to another tenant");
			}
		} catch (SpagoBIServiceException e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.DELETE", null, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e;
		} catch (Throwable e) {
			try {
				AuditLogUtilities.updateAudit(getHttpRequest(), profile, "PROF_USERS.DELETE", null, "ERR");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.error("Exception occurred while deleting user", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "Exception occurred while deleting user", e);
		}
	}

	private boolean isFinalUser(SbiUser user) throws EMFUserError {
		boolean toReturn = true;
		Set<SbiExtRoles> roles = user.getSbiExtUserRoleses();
		Iterator<SbiExtRoles> it = roles.iterator();
		while (it.hasNext()) {
			SbiExtRoles role = it.next();
			Integer roleId = role.getExtRoleId();
			Role roleBO = DAOFactory.getRoleDAO().loadByID(roleId);
			if (!roleBO.getRoleTypeCD().equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_USER)) {
				toReturn = false;
				break;
			}
		}
		return toReturn;
	}

	protected void setAttributesAndRolesInResponse() {
		try {
			List<SbiAttribute> attributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			List<Role> allRoles = DAOFactory.getRoleDAO().loadAllRoles();
			List<Role> roles = null;
			IEngUserProfile profile = this.getUserProfile();
			if (profile.isAbleToExecuteAction(SpagoBIConstants.PROFILE_MANAGEMENT)) {
				// administrator: he can see every role
				roles = allRoles;
			} else {
				// user with FINAL_USERS_MANAGEMENT (users with neither
				// FINAL_USERS_MANAGEMENT nor PROFILE_MANAGEMENT are blocked by
				// the
				// business_map.xml therefore they cannot execute this action)
				roles = this.filterRolesListForFinalUser(allRoles);
			}
			getSessionContainer().setAttribute("attributesList", attributes);
			getSessionContainer().setAttribute("rolesList", roles);
		} catch (Exception e) {
			logger.error("An error occurred when retrieving roles list", e);
			throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred when retrieving roles list", e);
		}
	}

	private List<Role> filterRolesListForFinalUser(List<Role> allRoles) {
		List<Role> toReturn = new ArrayList<Role>();
		for (Role role : allRoles) {
			if (role.getRoleTypeCD().equalsIgnoreCase(SpagoBIConstants.ROLE_TYPE_USER)) {
				toReturn.add(role);
			}
		}
		return toReturn;
	}

	protected void checkUserId(String userId, Integer id) {
		logger.debug("Validating user id " + userId + " ...");
		try {
			DAOFactory.getSbiUserDAO().checkUserId(userId, id);
		} catch (EMFUserError e) {
			if (e.getErrorCode().equals("400")) {
				throw new SpagoBIServiceException(SERVICE_NAME, "User id " + userId + " already in use");
			} else {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error checking if user identifier is valid", e);
			}
		}
		logger.debug("User id " + userId + " is valid");
	}

	private JSONObject createJSONResponseUsers(PagedList<UserBO> usersPagedList) throws JSONException, SerializationException {
		Locale locale = getLocale();
		JSONArray usersJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(usersPagedList.getResults(), locale);
		JSONObject results = new JSONObject();
		results.put("total", usersPagedList.getTotal());
		results.put("title", "Users");
		results.put("rows", usersJSON);
		return results;
	}

	private void deserializeRolesJSONArray(SbiUser user) throws Exception {
		Set<SbiExtRoles> roles = new HashSet<SbiExtRoles>();
		JSONArray rolesJSON = getAttributeAsJSONArray(ROLES);
		if (rolesJSON == null || rolesJSON.length() == 0) {
			throw new SpagoBIServiceException(SERVICE_NAME, "User roles were not specified: select at least one");
		}
		for (int i = 0; i < rolesJSON.length(); i++) {
			JSONObject obj = (JSONObject) rolesJSON.get(i);
			Integer extRoleId = obj.getInt("id");
			SbiExtRoles aRole = DAOFactory.getRoleDAO().loadSbiExtRoleById(extRoleId);
			roles.add(aRole);
		}
		user.setSbiExtUserRoleses(roles);
	}

	private void deserializeAttributesJSONArray(SbiUser user) throws Exception {
		Set<SbiUserAttributes> attributes = new HashSet<SbiUserAttributes>();
		JSONArray attributesJSON = getAttributeAsJSONArray(ATTRIBUTES);
		for (int i = 0; i < attributesJSON.length(); i++) {
			JSONObject obj = (JSONObject) attributesJSON.get(i);
			Integer key = obj.getInt("id");
			String value = obj.getString("value");
			// if (!value.equals("")) {
			SbiUserAttributes attribute = new SbiUserAttributes();
			attribute.setAttributeValue(value);
			SbiUserAttributesId attributeId = new SbiUserAttributesId();
			attributeId.setId(user.getId());
			attributeId.setAttributeId(key);
			attribute.setId(attributeId);
			SbiAttribute sbiAttribute = DAOFactory.getSbiAttributeDAO().loadSbiAttributeById(key);

			attribute.setSbiAttribute(sbiAttribute);
			attributes.add(attribute);
			// }
		}
		user.setSbiUserAttributeses(attributes);
	}

	private QueryStaticFilter getStaticFilter() throws JSONException {
		logger.debug("IN");
		QueryStaticFilter toReturn = null;
		JSONObject filtersJSON = null;
		if (this.requestContainsAttribute(FILTERS)) {
			filtersJSON = getAttributeAsJSONObject(FILTERS);
			String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			if (typeFilter.equalsIgnoreCase("like")) {
				typeFilter = CriteriaConstants.CONTAINS;
			} else {
				typeFilter = CriteriaConstants.EQUALS_TO;
			}
			String columnFilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
			toReturn = new QueryStaticFilter(columnFilter, valuefilter, typeFilter, true);
			logger.debug("Applying filter on users: " + toReturn.toString());
		}
		logger.debug("OUT");
		return toReturn;
	}

	public static void main(String[] args) {
		try {
			SpagoBIValidationImpl.validateField("userId", "userId", "RISORSE\\AOPE036", "REGEXP", "^[a-zA-Z0-9_\\\\\\x2D]*$", null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
