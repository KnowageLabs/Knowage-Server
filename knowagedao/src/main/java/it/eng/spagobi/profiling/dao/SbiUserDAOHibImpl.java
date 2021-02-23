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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import it.eng.qbe.statement.hibernate.HQLStatement;
import it.eng.qbe.statement.hibernate.HQLStatement.IConditionalOperator;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.dao.QueryFilter;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.dao.QueryStaticFilter;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesHistory;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesHistory;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bean.SbiUserHistory;
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
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public SbiUser loadSbiUserById(Integer id) throws EMFUserError {
		logger.debug("IN");
		SbiUser toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = (SbiUser) aSession.load(SbiUser.class, id);
			// String q = "from SbiUser us where us.id = :id";
			// Query query = aSession.createQuery(q);
			// query.setInteger("id", id);
			// toReturn = (SbiUser)query.uniqueResult();
			Hibernate.initialize(toReturn);
			Hibernate.initialize(toReturn.getSbiExtUserRoleses());
			Hibernate.initialize(toReturn.getSbiUserAttributeses());
			for (SbiUserAttributes current : toReturn.getSbiUserAttributeses()) {
				Hibernate.initialize(current.getSbiAttribute());
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
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
	public List<UserBO> loadUsers(QueryFilters filters, String dateFilter) throws EMFUserError {
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
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List<UserBO> loadUsers(QueryFilters filters) throws EMFUserError {
		return loadUsers(filters, null);
	}

	/**
	 * Insert SbiUser
	 *
	 * @param user
	 * @throws EMFUserError
	 */
	@Override
	public Integer saveSbiUser(SbiUser user) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			this.checkUserId(user.getUserId(), user.getId());

			Integer id = (Integer) aSession.save(user);
			tx.commit();
			return id;

		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
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
	 */
	@Override
	public void updateSbiUser(SbiUser user, Integer userID) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		Date opDate = new Date();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSession.update(user);
			logUpdate(aSession, user, opDate);
			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void updateSbiUserAttributes(SbiUserAttributes attribute) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSession.saveOrUpdate(attribute);
			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void updateSbiUserRoles(SbiExtUserRoles role) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			aSession.saveOrUpdate(role);
			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public SbiUser loadSbiUserByUserId(String userId) throws EMFUserError {
		logger.debug("IN");
		try {
			SbiUser user = getSbiUserByUserId(userId);
			return user;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
		}

	}

	@Override
	public ArrayList<SbiUserAttributes> loadSbiUserAttributesById(Integer id) throws EMFUserError {
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
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<SbiExtRoles> loadSbiUserRolesById(Integer id) throws EMFUserError {
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
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public ArrayList<SbiUser> loadSbiUsers() throws EMFUserError {
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
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public void deleteSbiUserByIdLogically(Integer id) throws EMFUserError {
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

			// deletes attributes associations logically
			if (userAttributes != null) {
				Iterator attrsIt = userAttributes.iterator();
				while (attrsIt.hasNext()) {
					SbiUserAttributes temp = (SbiUserAttributes) attrsIt.next();
					updateSbiCommonInfo4Update(temp);
					updateSbiCommonInfo4Delete(temp);
					aSession.save(temp);
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
					updateSbiCommonInfo4Update(temp);
					updateSbiCommonInfo4Delete(temp);
					aSession.save(temp);
					aSession.flush();
				}
			}
			SbiUser userToDelete = (SbiUser) aSession.load(SbiUser.class, id);
			updateSbiCommonInfo4Update(userToDelete);
			updateSbiCommonInfo4Delete(userToDelete);
			aSession.save(userToDelete);
			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void deleteSbiUserById(Integer id) throws EMFUserError {
		deleteSbiUserByIdLogically(id);
		deleteSbiUserByIdPhisically(id);
	}

	public void deleteSbiUserByIdPhisically(Integer id) throws EMFUserError {
		logger.debug("IN");

		Date opDate = new Date();

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
					logDeletion(aSession, temp, opDate);
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
					logDeletion(aSession, temp, opDate);
					aSession.flush();
				}
			}
			SbiUser userToDelete = (SbiUser) aSession.load(SbiUser.class, id);

			aSession.delete(userToDelete);
			logDeletion(aSession, userToDelete, opDate);
			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public void deleteRolesAndAttributesLogically(SbiUser user) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Integer id = user.getId();

			this.checkUserId(user.getUserId(), id);

			// remove existing roles logically
			String qr = " from SbiExtUserRoles x where x.id.id = :id ";
			Query queryR = aSession.createQuery(qr);
			queryR.setInteger("id", id);
			List<SbiExtUserRoles> userRoles = queryR.list();
			if (userRoles != null && !userRoles.isEmpty()) {
				Iterator rolesIt = userRoles.iterator();
				while (rolesIt.hasNext()) {
					SbiExtUserRoles temp = (SbiExtUserRoles) rolesIt.next();
					updateSbiCommonInfo4Update(temp);
					updateSbiCommonInfo4Delete(temp);
					aSession.save(temp);
					aSession.flush();
				}
			}

			// remove existing attributes logically
			qr = " from SbiUserAttributes x where x.id.id = :id ";
			queryR = aSession.createQuery(qr);
			queryR.setInteger("id", id);
			List<SbiUserAttributes> userAttributes = queryR.list();
			if (userAttributes != null && !userAttributes.isEmpty()) {
				Iterator<SbiUserAttributes> attrsIt = userAttributes.iterator();
				while (attrsIt.hasNext()) {
					SbiUserAttributes temp = attrsIt.next();
					updateSbiCommonInfo4Update(temp);
					updateSbiCommonInfo4Delete(temp);
					aSession.save(temp);
					aSession.flush();
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

	@Override
	public Integer fullSaveOrUpdateSbiUser(SbiUser user) throws EMFUserError {
		Integer id = user.getId();
		if (id != null) {
			// update scenario
			deleteRolesAndAttributesLogically(user);
		}
		return fullSaveOrUpdateSbiUserPhisically(user);
	}

	public Integer fullSaveOrUpdateSbiUserPhisically(SbiUser user) throws EMFUserError {
		logger.debug("IN");

		Date opDate = new Date();

		Session aSession = null;
		Transaction tx = null;
		Integer id = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			id = user.getId();

			this.checkUserId(user.getUserId(), id);

			SbiUser currentSessionUser = null;

			if (id != 0) {
				SbiUser userToUpdate = (SbiUser) aSession.load(SbiUser.class, id);
				boolean isChanged = !String.valueOf(userToUpdate.getPassword()).equals(String.valueOf(user.getPassword()))
						|| !String.valueOf(userToUpdate.getFullName()).equals(String.valueOf(user.getFullName()));

				if (user.getPassword() != null && user.getPassword().length() > 0) {
					userToUpdate.setPassword(user.getPassword());
				}
				userToUpdate.setFullName(user.getFullName());
				userToUpdate.setUserId(user.getUserId());
				userToUpdate.setId(id);
				updateSbiCommonInfo4Update(userToUpdate);
				aSession.save(userToUpdate);
				if (isChanged) {
					logUpdate(aSession, userToUpdate, opDate);
				}
				currentSessionUser = userToUpdate;
			} else {
				SbiUser newUser = new SbiUser();
				newUser.setUserId(user.getUserId());
				newUser.setFullName(user.getFullName());
				newUser.setPassword(user.getPassword());
				newUser.getCommonInfo().setOrganization(user.getCommonInfo().getOrganization());
				if (user.getCommonInfo().getUserIn() != null)
					newUser.getCommonInfo().setUserIn(user.getCommonInfo().getUserIn());
				newUser.setFlgPwdBlocked(user.getFlgPwdBlocked());
				updateSbiCommonInfo4Insert(newUser);
				newUser.setIsSuperadmin(Boolean.FALSE);
				id = (Integer) aSession.save(newUser);
				logCreation(aSession, newUser, opDate);
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
					logDeletion(aSession, temp, opDate);
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
				logCreation(aSession, sbiExtUserRole, opDate);
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
						logDeletion(aSession, temp, opDate);
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
								logUpdate(aSession, temp, opDate);
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
						logCreation(aSession, attribute, opDate);
						aSession.flush();
					}

				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
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
	 * Check if the user identifier in input is valid (for insertion or modification) for the user with the input integer id. In case of user insertion, id
	 * should be null.
	 *
	 * @param userId The user identifier to check
	 * @param id     The id of the user to which the user identifier should be validated
	 * @throws a EMFUserError with severity EMFErrorSeverity.ERROR and code 400 in case the user id is already in use
	 */
	@Override
	public void checkUserId(String userId, Integer id) throws EMFUserError {
		// if id == 0 means you are in insert case check user name is not
		// already used
		logger.debug("Check if user identifier " + userId + " is already present ...");
		Integer existingId = this.isUserIdAlreadyInUse(userId);
		if (id != null) {
			// case of user modification
			if (existingId != null && !id.equals(existingId)) {
				logger.error("User identifier is already present : [" + userId + "]");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "400");
			}
		} else {
			// case of user insertion
			if (existingId != null) {
				logger.error("User identifier is already present : [" + userId + "]");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "400");
			}
		}
		logger.debug("User identifier " + userId + " is valid.");
	}

	// public UserBO loadUserById(Integer id) throws EMFUserError {
	// // TODO Auto-generated method stub
	// return null;
	// }
	@Override
	public ArrayList<UserBO> loadUsers() throws EMFUserError {
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
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
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
	public UserBO toUserBO(SbiUser sbiUser) throws EMFUserError {
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

		List userRoles = new ArrayList();
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

	// public Integer countUsers() throws EMFUserError {
	// logger.debug("IN");
	// Session aSession = null;
	// Transaction tx = null;
	// Long resultNumber;
	//
	// try {
	// aSession = getSession();
	// tx = aSession.beginTransaction();
	//
	// String hql = "select count(*) from SbiUser ";
	// Query hqlQuery = aSession.createQuery(hql);
	// resultNumber = (Long)hqlQuery.uniqueResult();
	//
	// } catch (HibernateException he) {
	// logger.error("Error while loading the list of SbiUser", he);
	// if (tx != null)
	// tx.rollback();
	// throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
	//
	// } finally {
	// if (aSession != null) {
	// if (aSession.isOpen())
	// aSession.close();
	// logger.debug("OUT");
	// }
	// }
	// return new Integer(resultNumber.intValue());
	// }

	// public List<UserBO> loadPagedUsersList(Integer offset, Integer fetchSize)
	// throws EMFUserError {
	// logger.debug("IN");
	// List<UserBO> toReturn = null;
	// Session aSession = null;
	// Transaction tx = null;
	// Integer resultNumber;
	// Query hibernateQuery;
	//
	// try {
	// aSession = getSession();
	// tx = aSession.beginTransaction();
	//
	// List toTransform = null;
	// String hql = "select count(*) from SbiUser ";
	// Query hqlQuery = aSession.createQuery(hql);
	// resultNumber = new Integer(((Long)hqlQuery.uniqueResult()).intValue());
	//
	// offset = offset < 0 ? 0 : offset;
	// if(resultNumber > 0) {
	// fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber):
	// resultNumber;
	// }
	//
	// hibernateQuery = aSession.createQuery("from SbiUser su where su.id in ("
	// +
	// " select ur.id.id " +
	// " from " +
	// " SbiExtUserRoles ur, SbiExtRoles r " +
	// " where " +
	// " ur.id.extRoleId = r.extRoleId " +
	// " group by ur.id.id " +
	// " having sum(case when r.roleType.valueCd = 'USER' then 0 else 1 end) =
	// 0) " +
	// ") order by userId");
	// hibernateQuery.setFirstResult(offset);
	// if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);
	//
	// toTransform = hibernateQuery.list();
	//
	// if(toTransform!=null && !toTransform.isEmpty()){
	// toReturn = new ArrayList<UserBO>();
	// Iterator it = toTransform.iterator();
	// while(it.hasNext()){
	// SbiUser sbiUser = (SbiUser)it.next();
	// UserBO us = toUserBO(sbiUser);
	// toReturn.add(us);
	// }
	// }
	//
	// } catch (HibernateException he) {
	// logger.error("Error while loading the list of SbiAlarm", he);
	// if (tx != null)
	// tx.rollback();
	// throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
	//
	// } finally {
	// if (aSession != null) {
	// if (aSession.isOpen())
	// aSession.close();
	// logger.debug("OUT");
	// }
	// }
	// return toReturn;
	// }

	@Override
	public Integer isUserIdAlreadyInUse(String userId) {
		logger.debug("IN");
		try {
			SbiUser user = getSbiUserByUserId(userId);
			if (user != null) {
				return Integer.valueOf(user.getId());
			}
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
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
			criteria.add(Restrictions.eq("userId", userId).ignoreCase());
			SbiUser user = (SbiUser) criteria.uniqueResult();
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
	public PagedList<UserBO> loadUsersPagedList(QueryFilters filters, Integer offset, Integer fetchSize) throws EMFUserError {
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
			int indexStart = offset < 0 ? 0 : Math.min(offset, total - 1); // if
																			// total
																			// =
																			// 0
																			// -->
																			// indexStart
																			// =
																			// -1
			int indexEnd = (fetchSize > 0) ? Math.min(indexStart + fetchSize - 1, total - 1) // if
																								// total
																								// =
																								// 0
																								// -->
																								// indexEnd
																								// =
																								// -1
					: total - 1;

			List<UserBO> results = new ArrayList<UserBO>();
			if (total > 0) {
				for (int c = indexStart; c <= indexEnd; c++) {
					SbiUser hibuser = (SbiUser) users.get(c);
					results.add(toUserBO(hibuser));
				}
			}

			toReturn = new PagedList<UserBO>();
			toReturn.setStart(indexStart + 1);
			toReturn.setEnd(indexEnd + 1);
			toReturn.setTotal(total);
			toReturn.setResults(results);

		} catch (HibernateException he) {
			logger.error("Error while loading the list of users", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

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
	public void deleteSbiUserAttributeById(Integer id, Integer attributeId) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiUserAttributesId pk = new SbiUserAttributesId(id, attributeId);
			SbiUserAttributes attribute = (SbiUserAttributes) aSession.load(SbiUserAttributes.class, pk);
			aSession.delete(attribute);
			aSession.flush();
			tx.commit();
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

	private void logCreation(Session aSession, SbiUser bean, Date opDate) {
		SbiUserHistory currentEvent = new SbiUserHistory(bean);
		updateSbiCommonInfo4Insert(currentEvent);
		currentEvent.setTimeStart(opDate);
		currentEvent.setTimeEnd(null);
		aSession.save(currentEvent);
	}

	private void logCreation(Session aSession, SbiExtUserRoles bean, Date opDate) {
		SbiExtUserRolesHistory currentEvent = new SbiExtUserRolesHistory(bean);
		resetCommonInfo(currentEvent);
		updateSbiCommonInfo4Insert(currentEvent);
		currentEvent.setTimeStart(opDate);
		currentEvent.setTimeEnd(null);
		aSession.save(currentEvent);
	}

	private void logCreation(Session aSession, SbiUserAttributes bean, Date opDate) {
		SbiUserAttributesHistory currentEvent = new SbiUserAttributesHistory(bean);
		resetCommonInfo(currentEvent);
		updateSbiCommonInfo4Insert(currentEvent);
		currentEvent.setTimeStart(opDate);
		currentEvent.setTimeEnd(null);
		aSession.save(currentEvent);
	}

	private void logUpdate(Session aSession, SbiUser bean, Date opDate) {
		SbiUserHistory lastEvent = getLastEventForSbiUser(aSession, bean);

		if (lastEvent != null) {
			lastEvent.setTimeEnd(opDate);
			aSession.save(lastEvent);

			SbiUserHistory currentEvent = new SbiUserHistory(bean);
			resetCommonInfo(currentEvent);
			updateSbiCommonInfo4Update(currentEvent);
			currentEvent.setTimeStart(opDate);
			currentEvent.setTimeEnd(null);
			aSession.save(currentEvent);
		}
	}

	private void logUpdate(Session aSession, SbiUserAttributes bean, Date opDate) {
		SbiUserAttributesHistory lastEvent = getLastEventForSbiUserAttributes(aSession, bean);

		if (lastEvent != null) {
			lastEvent.setTimeEnd(opDate);
			aSession.save(lastEvent);

			SbiUserAttributesHistory currentEvent = new SbiUserAttributesHistory(bean);
			resetCommonInfo(currentEvent);
			updateSbiCommonInfo4Update(currentEvent);
			currentEvent.setTimeStart(opDate);
			currentEvent.setTimeEnd(null);
			aSession.save(currentEvent);
		}
	}

	private void logDeletion(Session aSession, SbiUser bean, Date opDate) {
		SbiUserHistory lastEvent = getLastEventForSbiUser(aSession, bean);

		if (lastEvent != null) {
			lastEvent.setTimeEnd(opDate);
			aSession.save(lastEvent);

			SbiUserHistory newEvent = new SbiUserHistory(bean);
			resetCommonInfo(newEvent);
			updateSbiCommonInfo4Delete(newEvent);
			newEvent.setTimeStart(opDate);
			newEvent.setTimeEnd(opDate);
			aSession.save(newEvent);
		}
	}

	private void logDeletion(Session aSession, SbiExtUserRoles bean, Date opDate) {
		SbiExtUserRolesHistory lastEvent = getLastEventForSbiExtUserRoles(aSession, bean);

		if (lastEvent != null) {
			lastEvent.setTimeEnd(opDate);
			aSession.save(lastEvent);

			SbiExtUserRolesHistory newEvent = new SbiExtUserRolesHistory(bean);
			resetCommonInfo(newEvent);
			updateSbiCommonInfo4Delete(newEvent);
			newEvent.setTimeStart(opDate);
			newEvent.setTimeEnd(opDate);
			aSession.save(newEvent);
		}
	}

	private void logDeletion(Session aSession, SbiUserAttributes bean, Date opDate) {
		SbiUserAttributesHistory lastEvent = getLastEventForSbiUserAttributes(aSession, bean);

		if (lastEvent != null) {
			lastEvent.setTimeEnd(opDate);
			aSession.save(lastEvent);

			SbiUserAttributesHistory newEvent = new SbiUserAttributesHistory(bean);
			resetCommonInfo(newEvent);
			updateSbiCommonInfo4Delete(newEvent);
			newEvent.setTimeStart(opDate);
			newEvent.setTimeEnd(opDate);
			aSession.save(newEvent);
		}
	}

	private SbiUserHistory getLastEventForSbiUser(Session aSession, SbiUser bean) {
		Criteria criteria = aSession.createCriteria(SbiUserHistory.class);
		criteria.add(Restrictions.eq("id", bean.getId()));
		criteria.add(Restrictions.isNull("timeEnd"));
		SbiUserHistory lastEvent = (SbiUserHistory) criteria.uniqueResult();
		return lastEvent;
	}

	private SbiExtUserRolesHistory getLastEventForSbiExtUserRoles(Session aSession, SbiExtUserRoles bean) {
		Criteria criteria = aSession.createCriteria(SbiExtUserRolesHistory.class);
		criteria.add(Restrictions.eq("id", bean.getId().getId()));
		criteria.add(Restrictions.eq("extRoleId", bean.getId().getExtRoleId()));
		criteria.add(Restrictions.isNull("timeEnd"));
		SbiExtUserRolesHistory lastEvent = (SbiExtUserRolesHistory) criteria.uniqueResult();
		return lastEvent;
	}

	private SbiUserAttributesHistory getLastEventForSbiUserAttributes(Session aSession, SbiUserAttributes bean) {
		Criteria criteria = aSession.createCriteria(SbiUserAttributesHistory.class);
		criteria.add(Restrictions.eq("id", bean.getId().getId()));
		criteria.add(Restrictions.eq("attributeId", bean.getId().getAttributeId()));
		criteria.add(Restrictions.isNull("timeEnd"));
		SbiUserAttributesHistory lastEvent = (SbiUserAttributesHistory) criteria.uniqueResult();
		return lastEvent;
	}

	/**
	 * Only for historical tables.
	 *
	 * You cannot reset USER_IN and TIME_IN on the original tables.
	 */
	private void resetCommonInfo(SbiHibernateModel bean) {
		SbiCommonInfo commonInfo = bean.getCommonInfo();

		commonInfo.setSbiVersionDe(null);
		commonInfo.setSbiVersionIn(null);
		commonInfo.setSbiVersionUp(null);

		commonInfo.setTimeDe(null);
		commonInfo.setTimeIn(null);
		commonInfo.setTimeUp(null);

		commonInfo.setUserDe(null);
		commonInfo.setUserIn(null);
		commonInfo.setUserUp(null);
	}
}
