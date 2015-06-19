/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.dao;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Abstract class that al DAO will have to extend.
 * 
 * @author Zoppello
 */
public class AbstractHibernateDAO {

	private static transient Logger logger = Logger.getLogger(AbstractHibernateDAO.class);

	private String userID = "server";
	private IEngUserProfile profile = null;
	private String tenant = null;

	public static final String TENANT_FILTER_NAME = "tenantFilter";
	private static final String TENANT_DEFAULT = "SPAGOBI";

	public void setUserID(String user) {
		userID = user;
	}

	public void setUserProfile(IEngUserProfile profile) {
		this.profile = profile;
		if (profile != null) {
			this.setUserID((String) (((UserProfile) profile).getUserId()));
		}
		logger.debug("userID = [{0}]" + this.userID);
	}

	public IEngUserProfile getUserProfile() {
		return profile;
	}

	// public Boolean isSuperadmin(){
	// Boolean isSuperadmin = false;
	// // look in the user profile
	// IEngUserProfile profile = this.getUserProfile();
	// if (profile != null) {
	// UserProfile userProfile = (UserProfile) profile;
	// isSuperadmin = userProfile.getIsSuperadmin();
	//
	// } else {
	// logger.debug("User profile object not found");
	// }
	// return isSuperadmin;
	// }

	public String getTenant() {
		// if a tenant is set into the DAO object, it wins
		String tenantId = this.tenant;
		logger.debug("This DAO object instance tenant = [{0}]" + tenantId);

		if (tenantId == null) {
			logger.debug("Tenant id not find in this DAO object instance; looking for it in the user profile object ... ");
			// look in the user profile
			IEngUserProfile profile = this.getUserProfile();
			if (profile != null) {
				UserProfile userProfile = (UserProfile) profile;
				tenantId = userProfile.getOrganization();
				logger.debug("User profile tenant = [{0}]" + tenantId);
			} else {
				logger.debug("User profile object not found");
			}
		}

		if (tenantId == null) {
			logger.debug("Tenant id not find in this DAO object instance nor in the user profile object; " + "looking for it using TenantManager ... ");
			// look for tenant using TenantManager
			Tenant tenant = TenantManager.getTenant();
			if (tenant != null) {
				tenantId = tenant.getName();
				logger.debug("TenantManager returns tenant = [{0}]" + tenantId);
			} else {
				logger.debug("TenantManager did not return any Tenant");
			}
		}

		logger.debug("OUT: tenant = [{0}]" + tenantId);
		return tenantId;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	/**
	 * Gets tre current session.
	 * 
	 * @return The current session object.
	 */
	public Session getSession() {
		Session session = HibernateSessionManager.getCurrentSession();
		String tenantId = this.getTenant();
		if (tenantId != null) {
			// if tenant is set, enable tenant filter and put filter's value
			this.enableTenantFilter(session, tenantId);
		}
		return session;
	}

	protected void enableTenantFilter(Session session, String tenantId) {
		Filter filter = session.enableFilter(TENANT_FILTER_NAME);
		filter.setParameter("tenant", tenantId);
	}

	protected void disableTenantFilter(Session session) {
		Filter filter = session.getEnabledFilter(TENANT_FILTER_NAME);
		if (filter != null) {
			session.disableFilter(TENANT_FILTER_NAME);
		}
	}

	/**
	 * usefull to update some property
	 * 
	 * @param obj
	 * @return
	 */
	protected SbiHibernateModel updateSbiCommonInfo4Update(SbiHibernateModel obj) {
		obj.getCommonInfo().setTimeUp(new Date());
		obj.getCommonInfo().setSbiVersionUp(SbiCommonInfo.SBI_VERSION);
		obj.getCommonInfo().setUserUp(userID);
		String tenantId = this.getTenant();
		// sets the tenant if it is set and input object hasn't
		if (tenantId != null && obj.getCommonInfo().getOrganization() == null) {
			obj.getCommonInfo().setOrganization(tenantId);
		}
		if (obj.getCommonInfo().getOrganization() == null) {
			throw new SpagoBIRuntimeException("Organization not set!!!");
		}
		return obj;
	}

	/**
	 * usefull to update some property
	 * 
	 * @param obj
	 * @return
	 */
	protected SbiHibernateModel updateSbiCommonInfo4Update(SbiHibernateModel obj, boolean useDefaultTenant) {
		obj.getCommonInfo().setTimeUp(new Date());
		obj.getCommonInfo().setSbiVersionUp(SbiCommonInfo.SBI_VERSION);
		obj.getCommonInfo().setUserUp(userID);
		String tenantId = this.getTenant();
		// sets the tenant if it is set and input object hasn't
		if (tenantId != null && obj.getCommonInfo().getOrganization() == null) {
			obj.getCommonInfo().setOrganization(tenantId);
		}
		if (obj.getCommonInfo().getOrganization() == null) {
			if (useDefaultTenant)
				obj.getCommonInfo().setOrganization(TENANT_DEFAULT);
			else
				throw new SpagoBIRuntimeException("Organization not set!!!");
		}
		return obj;
	}

	protected SbiHibernateModel updateSbiCommonInfo4Insert(SbiHibernateModel obj) {
		obj.getCommonInfo().setTimeIn(new Date());
		obj.getCommonInfo().setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
		obj.getCommonInfo().setUserIn(userID);

		// sets the tenant if it is set and input object hasn't
		String tenantId = this.getTenant();
		if (tenantId != null && obj.getCommonInfo().getOrganization() == null) {
			obj.getCommonInfo().setOrganization(tenantId);
		}
		if (obj.getCommonInfo().getOrganization() == null) {
			throw new SpagoBIRuntimeException("Organization not set!!!");
		}
		return obj;
	}

	protected SbiHibernateModel updateSbiCommonInfo4Insert(SbiHibernateModel obj, boolean useDefaultTenant) {
		obj.getCommonInfo().setTimeIn(new Date());
		obj.getCommonInfo().setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
		obj.getCommonInfo().setUserIn(userID);
		// sets the tenant if it is set and input object hasn't
		String tenantId = this.getTenant();
		if (tenantId != null && obj.getCommonInfo().getOrganization() == null) {
			obj.getCommonInfo().setOrganization(tenantId);
		}
		if (obj.getCommonInfo().getOrganization() == null) {
			if (useDefaultTenant)
				obj.getCommonInfo().setOrganization(TENANT_DEFAULT);
			else
				throw new SpagoBIRuntimeException("Organization not set!!!");

		}
		return obj;
	}

	/**
	 * Traces the exception information of a throwable input object.
	 * 
	 * @param t
	 *            The input throwable object
	 */
	public void logException(Throwable t) {
		logger.error(t.getClass().getName() + " " + t.getMessage(), t);
	}

	public void rollbackIfActiveAndClose(Transaction tx, Session aSession) {
		if (tx != null && tx.isActive()) {
			tx.rollback();
		}
		if (aSession != null && aSession.isOpen()) {
			aSession.close();
		}
	}

	public void commitIfActiveAndClose(Transaction tx, Session aSession) {
		if (tx != null && tx.isActive()) {
			tx.commit();
		}
		if (aSession != null && aSession.isOpen()) {
			aSession.close();
		}
	}

	/**
	 * Loads an object of type "clazz" whose id is "id"
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends SbiHibernateModel> T load(Class<T> clazz, Serializable id) {
		Session session = null;
		T toReturn = null;

LogMF.debug(logger, "IN: id = [{0}]", id);

		try {
			if (id == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			Object obj = session.get(clazz, id);

			if (obj != null) {
				toReturn = (T) obj;
				session.flush();
			} else {
				throw new SpagoBIDOAException("Object not found");
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading dataset whose id is equal to [" + id + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return toReturn;
	}

	/**
	 * Persists a new object and returns its id
	 * 
	 * @param obj
	 * @return objId
	 */
	public Serializable insert(SbiHibernateModel obj) {
		Session session = null;
		Serializable objId = null;
		Transaction tx = null;
		LogMF.debug(logger, "IN: obj = [{0}]", obj);

		try {
			if (obj == null) {
				throw new IllegalArgumentException("Input parameter cannot be null");
			}
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				tx = session.beginTransaction();
				Assert.assertNotNull(tx, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			updateSbiCommonInfo4Insert(obj);

			objId = session.save(obj);
			tx.commit();
		} catch (Throwable t) {
			if (tx != null) {
				tx.rollback();
			}
			throw new SpagoBIDOAException("Error saving a new object of type [" + obj.getClass() + "] ", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return objId;
	}

	/**
	 * Updates an existing object
	 * 
	 * @param obj
	 */
	public void update(SbiHibernateModel obj) {
		Session session = null;
		Transaction tx = null;
		LogMF.debug(logger, "IN: obj = [{0}]", obj);

		try {
			if (obj == null) {
				throw new IllegalArgumentException("Input parameter cannot be null");
			}
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				tx = session.beginTransaction();
				Assert.assertNotNull(tx, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			updateSbiCommonInfo4Update(obj);
			session.update(obj);
			tx.commit();
		} catch (Throwable t) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDOAException("Error updating an object of type [" + obj.getClass() + "] ", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Erases a record from db
	 * 
	 * @param clazz
	 * @param id
	 */
	public void delete(Class<? extends SbiHibernateModel> clazz, Serializable id) {
		Session session = null;
		Transaction tx = null;
		LogMF.debug(logger, "IN: id = [{0}]", id);

		try {
			if (id == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				tx = session.beginTransaction();
				Assert.assertNotNull(tx, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Object obj = session.get(clazz, id);
			if (obj == null) {
				throw new SpagoBIDOAException("Object of type [" + clazz + "] whose id is equal to [" + id + "] was not found");
			}
			session.delete(obj);
			tx.commit();

		} catch (Throwable t) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDOAException("An unexpected error occured while deleting object of type [" + clazz + "] whose id is equal to [" + id + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}
	
	
	
	
	/**
	 * Erases a record from db
	 * 
	 * @param clazz
	 * @param id
	 */
	public void deleteWordRef( Serializable id) {
		Session session = null;
		Transaction tx = null;
		LogMF.debug(logger, "IN: id = [{0}]", id);

		try {
			if (id == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				tx = session.beginTransaction();
				Assert.assertNotNull(tx, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			
			Query q = session.createQuery("delete from SbiGlReferences where refWord.wordId=:id");
			q.setParameter("id", id);
			q.executeUpdate();
			tx.commit();

		} catch (Throwable t) {
			if (tx != null)
				tx.rollback();
			throw new SpagoBIDOAException("An unexpected error occured while deleting word references where word id is equal to [" + id + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	public <T extends SbiHibernateModel> List<T> list(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Input parameter 'clazz' cannot be null");
		}
		return list(clazz, null);
	}

	public <T extends SbiHibernateModel> List<T> list(ICriterion<T> criterion) {
		if (criterion == null) {
			throw new IllegalArgumentException("Input parameter 'criteria' cannot be null");
		}
		return list(null, criterion);
	}

	@SuppressWarnings("unchecked")
	private <T extends SbiHibernateModel> List<T> list(Class<T> clazz, ICriterion<T> criterion) {
		List<T> ret = null;
		Session session = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			Criteria criteria = null;
			if (criterion == null) {
				criteria = session.createCriteria(clazz);
			} else {
				criteria = criterion.evaluate(session);
			}
			ret = criteria.list();
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while fetching objects of type [" + clazz + "] ", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return ret;
	}

	/**
	 * Executes the passed method inside a single transaction
	 * 
	 * @param executeOnTransaction
	 * @return
	 */
	public <T> T executeOnTransaction(IExecuteOnTransaction<T> executeOnTransaction) {
		T returnObj = null;
		Session session = null;
		Transaction tx = null;
		logger.debug("IN: executeOnTransaction");

		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			tx = session.beginTransaction();
			Assert.assertNotNull(tx, "transaction cannot be null");
			returnObj = executeOnTransaction.execute(session);
			tx.commit();
		} catch (Throwable t) {
			if (tx != null) {
				tx.rollback();
			}
			throw new SpagoBIDOAException("Error executing on transaction ", t);
		} finally {
			if (session != null) {
				session.close();
			}
			logger.debug("OUT: executeOnTransaction");
		}
		return returnObj;
	}

}
