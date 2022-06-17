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
package it.eng.spagobi.profiling.dao;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import it.eng.qbe.statement.hibernate.HQLStatement;
import it.eng.qbe.statement.hibernate.HQLStatement.IConditionalOperator;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.dao.QueryFilter;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.dao.QueryStaticFilter;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiEs;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.dao.filters.FinalUsersFilter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiUserDAOHibImpl extends AbstractHibernateDAO implements ISbiUserDAO {

	static private Logger logger = Logger.getLogger(SbiUserDAOHibImpl.class);

	static private enum AvailableFiltersOnUsersList {
		userId, fullName
	};

	/**
	 * Load SbiUser by id.
	 *
	 * @param id the identifier /** Load SbiUser by id.
	 * @param id the bi object id
	 * @return the BI object
	 * @throws SpagoBIDAOException
	 */
	@Override
	public SbiUser loadSbiUserById(Integer id) {
		logger.debug("IN");
		SbiUser toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = (SbiUser) aSession.load(SbiUser.class, id);

			Hibernate.initialize(toReturn);
			Hibernate.initialize(toReturn.getSbiExtUserRoleses());
			Hibernate.initialize(toReturn.getSbiUserAttributeses());
			for (SbiUserAttributes current : toReturn.getSbiUserAttributeses()) {
				Hibernate.initialize(current.getSbiAttribute());
			}

			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while loading user by id " + id, he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<UserBO> loadUsers(QueryFilters filters, String dateFilter) {
		logger.debug("IN");
		List<UserBO> results = new ArrayList<UserBO>();
		Session aSession = null;
		Transaction tx = null;

		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hibernateQuery = this.getQueryForUsersList(aSession, filters);
			List users = hibernateQuery.list();

			for (Iterator iterator = users.iterator(); iterator.hasNext();) {
				SbiUser sbiUser = (SbiUser) iterator.next();
				if (dateFilter != null) {
					if (sbiUser.getCommonInfo().getTimeUp() != null) {
						if (sbiUser.getCommonInfo().getTimeUp().getTime() > new Date(dateFilter).getTime()
								|| sbiUser.getCommonInfo().getTimeIn().getTime() > new Date(dateFilter).getTime()) {
							results.add(toUserBO(sbiUser));
						}
					} else if (sbiUser.getCommonInfo().getTimeIn().getTime() > new Date(dateFilter).getTime()) {
						results.add(toUserBO(sbiUser));
					}
				} else {
					results.add(toUserBO(sbiUser));
				}

			}

			return results;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while loading users ", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List<UserBO> loadUsers(QueryFilters filters) {
		return loadUsers(filters, null);
	}

	/**
	 * Reset failed login attemtpts counter.
	 *
	 * @author Marco Libanori
	 */
	@Override
	public void resetFailedLoginAttempts(String userId) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {

			if (isUserIdAlreadyInUse(userId) != null) {

				aSession = getSession();
				tx = aSession.beginTransaction();

				SbiUser user = getSbiUserByUserId(userId);

				user.setFailedLoginAttempts(0);

				aSession.update(user);

				tx.commit();
			}

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while reading failed login attempts counter for user " + userId, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/**
	 * Insert SbiUser
	 *
	 * @param user
	 * @throws EMFUserError
	 * @throws SpagoBIDAOException
	 */
	@Override
	public Integer saveSbiUser(SbiUser user) {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			checkUserId(user.getUserId(), user.getId());

			Integer id = (Integer) aSession.save(user);

			emitUserCreated(aSession, user);

			tx.commit();
			return id;

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while inserting user " + user, he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/**
	 * Update SbiUser
	 *
	 * @param user
	 * @throws EMFUserError
	 * @throws SpagoBIDAOException
	 */
	@Override
	public void updateSbiUser(SbiUser user, Integer userID) {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSession.update(user);

			emitUserUpdated(aSession, user);

			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while update user [" + user + "] with id " + userID, he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void updateSbiUserAttributes(SbiUserAttributes attribute) {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSession.saveOrUpdate(attribute);

			emitUserAttributeUpdated(aSession, attribute);

			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while update user attribute " + attribute, he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void updateSbiUserRoles(SbiExtUserRoles role) {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSession.saveOrUpdate(role);

			emitUserRoleUpdated(aSession, role);

			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while updating user with role " + role, he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public SbiUser loadSbiUserByUserId(String userId) {
		logger.debug("IN");
		try {
			SbiUser user = getSbiUserByUserId(userId);
			return user;
		} catch (HibernateException he) {
			throw new SpagoBIDAOException("Error while loading user by id " + userId, he);
		} finally {
			logger.debug("OUT");
		}

	}

	@Override
	public ArrayList<SbiUserAttributes> loadSbiUserAttributesById(Integer id) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "select us.sbiUserAttributeses from SbiUser us where us.id = :id";

			Query query = aSession.createQuery(q);
			query.setInteger("id", id);

			ArrayList<SbiUserAttributes> result = (ArrayList<SbiUserAttributes>) query.list();

			Hibernate.initialize(result);
			for (SbiUserAttributes current : result) {
				Hibernate.initialize(current.getSbiAttribute());
			}
			return result;

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while loading user attribute with id " + id, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public ArrayList<SbiExtRoles> loadSbiUserRolesById(Integer id) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "select us.sbiExtUserRoleses from SbiUser us where us.id = :id";
			Query query = aSession.createQuery(q);
			query.setInteger("id", id);

			ArrayList<SbiExtRoles> result = (ArrayList<SbiExtRoles>) query.list();
			return result;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while loading user role with id " + id, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public ArrayList<SbiUser> loadSbiUsers() {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiUser ";
			Query query = aSession.createQuery(q);

			ArrayList<SbiUser> result = (ArrayList<SbiUser>) query.list();
			return result;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while loading users", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public Integer fullSaveOrUpdateSbiUser(SbiUser user) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			id = user.getId();

			checkUserId(user.getUserId(), id);

			SbiUser currentSessionUser = null;

			if (id != 0) {
				SbiUser userToUpdate = (SbiUser) aSession.load(SbiUser.class, id);
				if (user.getPassword() != null && user.getPassword().length() > 0) {
					userToUpdate.setPassword(user.getPassword());
				}
				userToUpdate.setFullName(user.getFullName());
				userToUpdate.setUserId(user.getUserId());
				userToUpdate.setId(id);
				userToUpdate.setFailedLoginAttempts(user.getFailedLoginAttempts());
				userToUpdate.setDefaultRoleId(user.getDefaultRoleId());
				updateSbiCommonInfo4Update(userToUpdate);
				aSession.save(userToUpdate);

				emitUserUpdated(aSession, userToUpdate);

				currentSessionUser = userToUpdate;
			} else {
				SbiUser newUser = new SbiUser();
				newUser.setUserId(user.getUserId());
				newUser.setFullName(user.getFullName());
				newUser.setPassword(user.getPassword());
				newUser.setDefaultRoleId(user.getDefaultRoleId());
				newUser.getCommonInfo().setOrganization(user.getCommonInfo().getOrganization());
				if (user.getCommonInfo().getUserIn() != null)
					newUser.getCommonInfo().setUserIn(user.getCommonInfo().getUserIn());
				newUser.setFlgPwdBlocked(user.getFlgPwdBlocked());
				updateSbiCommonInfo4Insert(newUser);
				newUser.setIsSuperadmin(Boolean.FALSE);
				id = (Integer) aSession.save(newUser);

				emitUserCreated(aSession, newUser);

				currentSessionUser = newUser;
			}

			// sets roles
			// remove existing roles
			String qr = " from SbiExtUserRoles x where x.id.id = :id ";
			Query queryR = aSession.createQuery(qr);
			queryR.setInteger("id", id);
			List<SbiExtUserRoles> userRoles = queryR.list();

			Set<String> actualRoles = new HashSet<String>();
			Set<String> addedRoles = new HashSet<String>();
			Set<String> deletedRoles = null;

			if (userRoles != null && !userRoles.isEmpty()) {
				for (SbiExtUserRoles extUserRoles : userRoles) {
					actualRoles.add(extUserRoles.getId().getExtRoleId().toString());
				}
			}

			for (SbiExtRoles sbiExtRoles : user.getSbiExtUserRoleses()) {
				addedRoles.add(sbiExtRoles.getExtRoleId().toString());
			}

			deletedRoles = new HashSet<String>(actualRoles);

			deletedRoles.removeAll(addedRoles);
			addedRoles.removeAll(actualRoles);

			if (userRoles != null && !userRoles.isEmpty()) {
				Iterator rolesIt = userRoles.iterator();
				while (rolesIt.hasNext()) {
					SbiExtUserRoles temp = (SbiExtUserRoles) rolesIt.next();

					if (!deletedRoles.contains(temp.getId().getExtRoleId().toString())) {
						continue;
					}

					rolesIt.remove();
					aSession.delete(temp);

					emitUserRoleDeleted(aSession, temp);

					aSession.flush();
				}
			}
			// add new roles
			Iterator<SbiExtRoles> rolesIt = user.getSbiExtUserRoleses().iterator();
			while (rolesIt.hasNext()) {
				SbiExtRoles aRole = rolesIt.next();

				if (!addedRoles.contains(aRole.getExtRoleId().toString())) {
					continue;
				}

				SbiExtUserRoles sbiExtUserRole = new SbiExtUserRoles();
				SbiExtUserRolesId extUserRoleId = new SbiExtUserRolesId();

				extUserRoleId.setExtRoleId(aRole.getExtRoleId());// role Id
				extUserRoleId.setId(currentSessionUser.getId());// user ID

				sbiExtUserRole.setId(extUserRoleId);
				sbiExtUserRole.setSbiUser(currentSessionUser);

				sbiExtUserRole.setSbiUser(currentSessionUser);
				sbiExtUserRole.getId().setId(currentSessionUser.getId());
				sbiExtUserRole.getCommonInfo().setOrganization(aRole.getCommonInfo().getOrganization());
				updateSbiCommonInfo4Insert(sbiExtUserRole);
				aSession.saveOrUpdate(sbiExtUserRole);

				emitUserRoleAdded(aSession, sbiExtUserRole);

				aSession.flush();
			}
			// set attributes
			// remove existing attributes
			qr = " from SbiUserAttributes x where x.id.id = :id ";
			queryR = aSession.createQuery(qr);
			queryR.setInteger("id", id);
			List<SbiUserAttributes> userAttributes = queryR.list();
			Set<SbiUserAttributes> newAttributes = user.getSbiUserAttributeses();

			Set<Integer> actualAttrs = new HashSet<Integer>();
			Set<Integer> addedAttrs = new HashSet<Integer>();
			Set<Integer> deletedAttrs = null;
			Set<Integer> alreadyPresentAttrs = null;

			if (userAttributes != null && !userAttributes.isEmpty()) {
				for (SbiUserAttributes sbiUserAttributes : userAttributes) {
					actualAttrs.add(sbiUserAttributes.getId().getAttributeId());
				}
			}

			for (SbiUserAttributes sbiUserAttributes : user.getSbiUserAttributeses()) {
				addedAttrs.add(sbiUserAttributes.getId().getAttributeId());
			}

			deletedAttrs = new HashSet<Integer>(actualAttrs);
			alreadyPresentAttrs = new HashSet<Integer>(actualAttrs);

			alreadyPresentAttrs.retainAll(addedAttrs);
			deletedAttrs.removeAll(addedAttrs);
			addedAttrs.removeAll(actualAttrs);

			// remove deleted attributes
			if (userAttributes != null && !userAttributes.isEmpty()) {
				Iterator<SbiUserAttributes> attrsIt = userAttributes.iterator();
				while (attrsIt.hasNext()) {
					SbiUserAttributes temp = attrsIt.next();
					int attributeId = temp.getId().getAttributeId();

					if (deletedAttrs.contains(attributeId)) {
						attrsIt.remove();
						aSession.delete(temp);

						emitUserAttributeDeleted(aSession, temp);

						aSession.flush();
					}

				}
			}
			// update attributes
			if (userAttributes != null && !userAttributes.isEmpty()) {
				Iterator<SbiUserAttributes> attrsIt = userAttributes.iterator();
				while (attrsIt.hasNext()) {
					SbiUserAttributes temp = attrsIt.next();
					int attributeId = temp.getId().getAttributeId();

					if (alreadyPresentAttrs.contains(attributeId)) {
						if (newAttributes != null && !newAttributes.isEmpty()) {

							SbiUserAttributes currUserAttr = null;
							for (SbiUserAttributes sbiUserAttributes : newAttributes) {

								if (sbiUserAttributes.getId().getAttributeId() == attributeId) {
									currUserAttr = sbiUserAttributes;
									break;
								}
							}

							if (currUserAttr != null && !currUserAttr.getAttributeValue().equals(temp.getAttributeValue())) {
								temp.setAttributeValue(currUserAttr.getAttributeValue());
								updateSbiCommonInfo4Update(temp);
								aSession.saveOrUpdate(temp);

								emitUserAttributeUpdated(aSession, temp);

								aSession.flush();
							}
						}
					}
				}
			}
			// add new attributes and updates old ones
			if (newAttributes != null && !newAttributes.isEmpty()) {
				Iterator<SbiUserAttributes> attrsIt = newAttributes.iterator();
				while (attrsIt.hasNext()) {
					SbiUserAttributes attribute = attrsIt.next();
					int attributeId = attribute.getId().getAttributeId();

					if (addedAttrs.contains(attributeId)) {
						attribute.getId().setId(id);
						updateSbiCommonInfo4Insert(attribute);
						aSession.saveOrUpdate(attribute);

						SbiAttribute loadedSbiAttribute = DAOFactory.getSbiAttributeDAO().loadSbiAttributeById(attributeId);
						attribute.setSbiAttribute(loadedSbiAttribute);
						attribute.setSbiUser(currentSessionUser);

						emitUserAttributeAdded(aSession, attribute);

						aSession.flush();
					}

				}
			}

			tx.commit();
		} catch (Exception he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while saving user " + user, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return id;

	}

	/**
	 * Get value of failed login attemtpts counter from DB.
	 *
	 * @author Marco Libanori
	 */
	@Override
	public int getFailedLoginAttempts(String userId) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {

			Integer result = 0;

			if (isUserIdAlreadyInUse(userId) != null) {

				aSession = getSession();
				tx = aSession.beginTransaction();

				SbiUser user = getSbiUserByUserId(userId);

				result = user.getFailedLoginAttempts();

				tx.commit();
			}

			return result;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while reading failed login attempts counter for user " + userId, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/**
	 * Increment failed login attemtpts counter.
	 *
	 * @author Marco Libanori
	 */
	@Override
	public void incrementFailedLoginAttempts(String userId) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {

			if (isUserIdAlreadyInUse(userId) != null) {

				aSession = getSession();
				tx = aSession.beginTransaction();

				SbiUser user = getSbiUserByUserId(userId);

				user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

				aSession.update(user);

				tx.commit();
			}

		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while reading failed login attempts counter for user " + userId, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/**
	 * Check if the user identifier in input is valid (for insertion or modification) for the user with the input integer id. In case of user insertion, id
	 * should be null.
	 *
	 * @param userId The user identifier to check
	 * @param id     The id of the user to which the user identifier should be validated
	 * @throws SpagoBIDAOException
	 */
	@Override
	public void checkUserId(String userId, Integer id) {
		// if id == 0 means you are in insert case check user name is not
		// already used
		logger.debug("Check if user identifier " + userId + " is already present ...");
		Integer existingId = this.isUserIdAlreadyInUse(userId);
		if (id != null) {
			// case of user modification
			if (existingId != null && !id.equals(existingId)) {
				throw new SpagoBIDAOException("User identifier is already present : [" + userId + "]");
			}
		} else {
			// case of user insertion
			if (existingId != null) {
				throw new SpagoBIDAOException("User identifier is already present : [" + userId + "]");
			}
		}
		logger.debug("User identifier " + userId + " is valid.");
	}

	@Override
	public ArrayList<UserBO> loadUsers() {
		logger.debug("IN");
		ArrayList<UserBO> users = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria crit = aSession.createCriteria(SbiUser.class);

			ArrayList<SbiUser> result = (ArrayList<SbiUser>) crit.list();
			if (result != null && !result.isEmpty()) {
				users = new ArrayList<UserBO>();
				for (int i = 0; i < result.size(); i++) {
					users.add(toUserBO(result.get(i)));
				}
			}

			return users;
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while loading users", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/**
	 * From the Hibernate SbiUser at input, gives the corrispondent BI object (UserBO).
	 *
	 * @param sbiUser The Hibernate SbiUser
	 * @return the corrispondent output <code>UserBO</code>
	 * @throws EMFUserError
	 */
	public UserBO toUserBO(SbiUser sbiUser) {
		logger.debug("IN");
		// create empty UserBO
		UserBO userBO = new UserBO();
		userBO.setId(sbiUser.getId());
		userBO.setDtLastAccess(sbiUser.getDtLastAccess());
		userBO.setDtPwdBegin(sbiUser.getDtPwdBegin());
		userBO.setDtPwdEnd(sbiUser.getDtPwdEnd());
		userBO.setFlgPwdBlocked(sbiUser.getFlgPwdBlocked());
		userBO.setFullName(sbiUser.getFullName());
		userBO.setPassword(sbiUser.getPassword());
		userBO.setUserId(sbiUser.getUserId());
		userBO.setIsSuperadmin(sbiUser.getIsSuperadmin());
		userBO.setDefaultRoleId(sbiUser.getDefaultRoleId());

		IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
		configsDao.setUserProfile(getUserProfile());

		Integer maxFailedLoginAttempts = null;
		try {
			maxFailedLoginAttempts = Integer.valueOf(SingletonConfig.getInstance().getConfigValue("internal.security.login.maxFailedLoginAttempts"));
		} catch (NumberFormatException e) {
			throw new SpagoBIRuntimeException("Error while retrieving maxFailedLoginAttempts for user ", e);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while retrieving maxFailedLoginAttempts for user ", e);
		}
		userBO.setFailedLoginAttempts(sbiUser.getFailedLoginAttempts());
		userBO.setBlockedByFailedLoginAttempts(sbiUser.getFailedLoginAttempts() >= maxFailedLoginAttempts);

		List<Integer> userRoles = new ArrayList<>();
		Set roles = sbiUser.getSbiExtUserRoleses();
		for (Iterator it = roles.iterator(); it.hasNext();) {
			SbiExtRoles role = (SbiExtRoles) it.next();
			Integer roleId = role.getExtRoleId();
			userRoles.add(roleId);
		}
		userBO.setSbiExtUserRoleses(userRoles);

		HashMap<Integer, HashMap<String, String>> userAttributes = new HashMap<Integer, HashMap<String, String>>();
		Set<SbiUserAttributes> attributes = sbiUser.getSbiUserAttributeses();

		for (Iterator<SbiUserAttributes> it = attributes.iterator(); it.hasNext();) {
			SbiUserAttributes attr = it.next();
			Integer attrId = attr.getSbiAttribute().getAttributeId();
			HashMap<String, String> nameValueAttr = new HashMap<String, String>();

			nameValueAttr.put(attr.getSbiAttribute().getAttributeName(), attr.getAttributeValue());
			userAttributes.put(attrId, nameValueAttr);
		}
		userBO.setSbiUserAttributeses(userAttributes);

		logger.debug("OUT");
		return userBO;
	}

	@Override
	public Integer isUserIdAlreadyInUse(String userId) {
		logger.debug("IN");
		try {
			SbiUser user = getSbiUserByUserId(userId);
			if (user != null) {
				return Integer.valueOf(user.getId());
			}
		} catch (HibernateException he) {
			throw new SpagoBIRuntimeException("Error while checking if user identifier is already in use", he);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Get the SbiUser object with the input user identifier. The search method is CASE INSENSITIVE!!!
	 *
	 * @param userId The user identifier
	 * @return the SbiUser object with the input user identifier
	 */
	protected SbiUser getSbiUserByUserId(String userId) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			// WE MUST UNSET THE TENANT FILTER, SINCE USER ID MUST BE UNIQUE
			// ACCROSS ALL TENANTS
			this.disableTenantFilter(aSession);
			tx = aSession.beginTransaction();
			LogMF.debug(logger, "IN : user id = [{0}]", userId);
			// case insensitive search!!!!
			Criteria criteria = aSession.createCriteria(SbiUser.class);

			/*
			 * Here we use ignoreCase() because some sort of problem with
			 * collations coming from SpagoBI.
			 *
			 * See also other occurrences of this comment.
			 *
			 * TODO : Fix ignoreCase()!
			 */
			criteria.add(Restrictions.eq("userId", userId).ignoreCase());
			SbiUser user = (SbiUser) criteria.uniqueResult();

			if (user != null) {
				Hibernate.initialize(user);
				Hibernate.initialize(user.getSbiExtUserRoleses());
				Hibernate.initialize(user.getSbiUserAttributeses());
				for (SbiUserAttributes current : user.getSbiUserAttributeses()) {
					Hibernate.initialize(current.getSbiAttribute());
				}
			}
			LogMF.debug(logger, "OUT : returning [{0}]", user);
			return user;
		} finally {
			if (tx != null) {
				tx.rollback();
			}
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

	}

	@Override
	public PagedList<UserBO> loadUsersPagedList(QueryFilters filters, Integer offset, Integer fetchSize) {
		logger.debug("IN");
		PagedList<UserBO> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer total;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			hibernateQuery = this.getQueryForUsersList(aSession, filters);
			List users = hibernateQuery.list();

			total = users.size();
			int indexStart = offset < 0 ? 0 : Math.min(offset, total - 1); // if totale = 0 --> indexStart = -1
			int indexEnd = (fetchSize > 0) ? Math.min(indexStart + fetchSize - 1, total - 1) : total - 1; // if totale =
																											// 0 -->
																											// indexEnd
																											// = -1

			List<UserBO> results = new ArrayList<UserBO>();
			if (total > 0) {
				for (int c = indexStart; c <= indexEnd; c++) {
					SbiUser hibuser = (SbiUser) users.get(c);
					results.add(toUserBO(hibuser));
				}
			}

			toReturn = new PagedList<UserBO>();
			toReturn.setStart(indexStart + 1);
			toReturn.setTotal(total);
			toReturn.setResults(results);

		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			throw new SpagoBIDAOException("Error while loading the list of users", he);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	private Query getQueryForUsersList(Session aSession, QueryFilters filters) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" from SbiUser ");
		if (!filters.isEmpty()) {
			buffer.append(" where ");
			Iterator<QueryFilter> iterator = filters.iterator();
			while (iterator.hasNext()) {
				String hqlFilter = this.toHQL(iterator.next());
				buffer.append(hqlFilter);
				if (iterator.hasNext()) {
					buffer.append(" and ");
				}
			}
		}
		buffer.append(" order by userId ");
		String hql = buffer.toString();
		LogMF.debug(logger, "HQL for users list : [{0}]", hql);
		Query hqlQuery = aSession.createQuery(hql);
		return hqlQuery;
	}

	private String toHQL(QueryFilter filter) {
		Assert.assertNotNull(filter, "Filter is null");
		if (filter instanceof QueryStaticFilter) {
			QueryStaticFilter staticFilter = (QueryStaticFilter) filter;
			boolean ignoreCase = staticFilter.isIgnoreCase();
			IConditionalOperator conditionalOperator = (IConditionalOperator) HQLStatement.conditionalOperators.get(staticFilter.getOperator());
			String actualFieldName = AvailableFiltersOnUsersList.valueOf(staticFilter.getField()).toString();
			String leftHandValue = ignoreCase ? "upper(" + actualFieldName + ")" : actualFieldName;
			String value = staticFilter.getValue() != null ? staticFilter.getValue().toString() : "";
			String escapedValue = StringEscapeUtils.escapeSql(value);
			String[] rightHandValues = new String[] { "'" + (ignoreCase ? escapedValue.toUpperCase() : escapedValue) + "'" };
			return conditionalOperator.apply(leftHandValue, rightHandValues);
		} else if (filter instanceof FinalUsersFilter) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(" id in (");
			buffer.append(" select ur.id.id ");
			buffer.append("	from ");
			buffer.append("		SbiExtUserRoles ur, SbiExtRoles r ");
			buffer.append("	where ");
			buffer.append("		ur.id.extRoleId = r.extRoleId ");
			buffer.append("	group by ur.id.id ");
			buffer.append("	having sum(case when r.roleType.valueCd = 'USER' then 0 else 1 end) = 0) ");
			buffer.append(") ");
			return buffer.toString();
		} else {
			throw new SpagoBIRuntimeException("Cannot handle filter of type [" + filter.getClass().getName() + "]");
		}
	}

	@Override
	public boolean thereIsAnyUsers() {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();

			disableTenantFilter(aSession);
			tx = aSession.beginTransaction();

			Number count = (Number) aSession.createCriteria(SbiUser.class)
				.setProjection(Projections.rowCount())
				.uniqueResult();

			return count.longValue() > 0;

		} finally {
			if (tx != null) {
				tx.rollback();
			}
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
				}
			}
			logger.debug("OUT");
		}

	}

	@Override
	public void deleteSbiUserById(Integer id) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String q = " from SbiUserAttributes x where x.id.id = :id ";
			Query query = aSession.createQuery(q);
			query.setInteger("id", id);

			ArrayList<SbiUserAttributes> userAttributes = (ArrayList<SbiUserAttributes>) query.list();

			// deletes attributes associations
			if (userAttributes != null) {
				Iterator attrsIt = userAttributes.iterator();
				while (attrsIt.hasNext()) {
					SbiUserAttributes temp = (SbiUserAttributes) attrsIt.next();
					attrsIt.remove();

					aSession.delete(temp);
					aSession.flush();
				}
			}

			String qr = " from SbiExtUserRoles x where x.id.id = :id ";
			Query queryR = aSession.createQuery(qr);
			queryR.setInteger("id", id);

			ArrayList<SbiExtUserRoles> userRoles = (ArrayList<SbiExtUserRoles>) queryR.list();
			if (userRoles != null) {
				Iterator rolesIt = userRoles.iterator();
				while (rolesIt.hasNext()) {
					SbiExtUserRoles temp = (SbiExtUserRoles) rolesIt.next();
					rolesIt.remove();
					aSession.delete(temp);
					aSession.flush();
				}
			}
			SbiUser userToDelete = (SbiUser) aSession.load(SbiUser.class, id);

			aSession.delete(userToDelete);

			emitUserDeleted(aSession, userToDelete);

			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while deleting user with id " + id, he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void deleteSbiUserAttributeById(Integer id, Integer attributeId) {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiUserAttributesId pk = new SbiUserAttributesId(id, attributeId);
			SbiUserAttributes attribute = (SbiUserAttributes) aSession.load(SbiUserAttributes.class, pk);
			aSession.delete(attribute);

			emitUserAttributeDeleted(aSession, attribute);

			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDAOException("Error while deleting attribute " + attributeId + " of user with id " + id);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

	private void emitUserAttributeDeleted(Session aSession, SbiUserAttributes attribute) {

		JsonObject data = createCommonDataForEvent()
				.add("id", attribute.getId().getId())
				.add("attributeId", attribute.getSbiAttribute().getAttributeId())
				.add("name", attribute.getSbiAttribute().getAttributeName())
				.add("value", attribute.getAttributeValue())
				.build();

		SbiEs event = createUserEvent(attribute.getSbiUser())
				.withEvent("UserAttributeDeleted")
				.withData(data)
				.build();

			aSession.save(event);

	}

	private void emitUserAttributeAdded(Session aSession, SbiUserAttributes attribute) {

		JsonObject data = createCommonDataForEvent()
				.add("id", attribute.getId().getId())
				.add("attributeId", attribute.getId().getAttributeId())
				.add("name", attribute.getSbiAttribute().getAttributeName())
				.add("value", attribute.getAttributeValue())
				.build();

		SbiEs event = createUserEvent(attribute.getSbiUser())
				.withEvent("UserAttributeAdded")
				.withData(data)
				.build();

			aSession.save(event);

	}

	private void emitUserDeleted(Session aSession, SbiUser user) {

		Set<SbiExtRoles> sbiExtUserRoleses = user.getSbiExtUserRoleses();

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		sbiExtUserRoleses.stream()
			.map(e -> Json.createObjectBuilder()
					.add("extRoleId", e.getExtRoleId())
					.add("code", e.getCode())
					.add("name", e.getName())
					.build())
			.forEach(arrayBuilder::add);

		JsonObject data = createCommonDataForEvent()
				.add("id", user.getId())
				.add("userId", user.getUserId())
				.add("roles", arrayBuilder)
				.build();

		SbiEs event = createUserEvent(user)
			.withEvent("UserDeleted")
			.withData(data)
			.build();

		aSession.save(event);
	}

	private void emitUserCreated(Session aSession, SbiUser user) {

		Set<SbiExtRoles> sbiExtUserRoleses = user.getSbiExtUserRoleses();

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		sbiExtUserRoleses.stream()
			.map(e -> Json.createObjectBuilder()
					.add("extRoleId", e.getExtRoleId())
					.add("code", e.getCode())
					.add("name", e.getName())
					.build())
			.forEach(arrayBuilder::add);

		JsonObject data = createCommonDataForEvent()
				.add("id", user.getId())
				.add("userId", user.getUserId())
				.add("roles", arrayBuilder)
				.build();

		SbiEs event = createUserEvent(user)
			.withEvent("UserCreated")
			.withData(data)
			.build();

		aSession.save(event);
	}

	private void emitUserUpdated(Session aSession, SbiUser user) {

		Set<SbiExtRoles> sbiExtUserRoleses = user.getSbiExtUserRoleses();

		JsonArrayBuilder rolesArrayBuilder = Json.createArrayBuilder();

		sbiExtUserRoleses.stream()
			.map(e -> {
				JsonObjectBuilder builder = Json.createObjectBuilder();
				builder.add("extRoleId", e.getExtRoleId());

				if (isNull(e.getCode())) {
					builder.addNull("code");
				} else {
					builder.add("code", e.getCode());
				}
				builder.add("name", e.getName());

				JsonObject built = builder.build();

				return built;
			})
			.forEach(rolesArrayBuilder::add);

		JsonObject data = createCommonDataForEvent()
				.add("id", user.getId())
				.add("userId", user.getUserId())
				.add("roles", rolesArrayBuilder)
				.build();

		SbiEs event = createUserEvent(user)
			.withEvent("UserUpdated")
			.withData(data)
			.build();

		aSession.save(event);
	}

	private void emitUserRoleUpdated(Session aSession, SbiExtUserRoles role) {

		JsonObject data = createCommonDataForEvent()
				.add("extRoleId", role.getId().getExtRoleId())
				.add("id", role.getId().getId())
				.build();

		SbiEs event = createUserEvent(role.getSbiUser())
			.withEvent("UserRoleUpdated")
			.withData(data)
			.build();

		aSession.save(event);
	}

	private void emitUserRoleDeleted(Session aSession, SbiExtUserRoles role) {

		JsonObject data = createCommonDataForEvent()
				.add("extRoleId", role.getId().getExtRoleId())
				.add("id", role.getId().getId())
				.build();

		SbiEs event = createUserEvent(role.getSbiUser())
			.withEvent("UserRoleDeleted")
			.withData(data)
			.build();

		aSession.save(event);
	}

	private void emitUserRoleAdded(Session aSession, SbiExtUserRoles role) {

		JsonObject data = createCommonDataForEvent()
				.add("extRoleId", role.getId().getExtRoleId())
				.add("id", role.getId().getId())
				.build();

		SbiEs event = createUserEvent(role.getSbiUser())
			.withEvent("UserRoleAdded")
			.withData(data)
			.build();

		aSession.save(event);
	}

	private void emitUserAttributeUpdated(Session aSession, SbiUserAttributes attribute) {

		JsonObject data = createCommonDataForEvent()
				.add("id", attribute.getId().getId())
				.add("attributeId", attribute.getSbiAttribute().getAttributeId())
				.add("name", attribute.getSbiAttribute().getAttributeName())
				.add("value", attribute.getAttributeValue())
				.build();

		SbiEs event = createUserEvent(attribute.getSbiUser())
				.withEvent("UserAttributeUpdated")
				.withData(data)
				.build();

		aSession.save(event);

	}

	private SbiEs.Builder createUserEvent(SbiUser user) {
		return createUserEvent(user.getId());
	}

	private SbiEs.Builder createUserEvent(int userId) {
		return SbiEs.Builder.newBuilder()
			.withType("User")
			.withId(userId);
	}
}
