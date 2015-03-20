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
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Date;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Abstract class that al DAO will have to extend.
 * 
 * @author Zoppello
 */
public class AbstractHibernateDAO {

	private static transient Logger logger = Logger
			.getLogger(AbstractHibernateDAO.class);
	
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
			this.setUserID( (String) (((UserProfile)profile).getUserId() ));
		}
		logger.debug("userID = [{0}]"+ this.userID);
	}

	public IEngUserProfile getUserProfile() {
		return profile;
	}
	
//	public Boolean isSuperadmin(){
//		Boolean isSuperadmin = false;
//		// look in the user profile
//		IEngUserProfile profile = this.getUserProfile();
//		if (profile != null) {
//			UserProfile userProfile = (UserProfile) profile;
//			isSuperadmin = userProfile.getIsSuperadmin();
//
//		} else {
//			logger.debug("User profile object not found");
//		}
//		return isSuperadmin;
//	}
	
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
				logger.debug( "User profile tenant = [{0}]" + tenantId);
			} else {
				logger.debug("User profile object not found");
			}
		}
		
		if (tenantId == null) {
			logger.debug("Tenant id not find in this DAO object instance nor in the user profile object; " +
					"looking for it using TenantManager ... ");
			// look for tenant using TenantManager
			Tenant tenant = TenantManager.getTenant();
			if (tenant != null) {
				tenantId = tenant.getName();
				logger.debug("TenantManager returns tenant = [{0}]" + tenantId);
			} else {
				logger.debug("TenantManager did not return any Tenant");
			}
		}
		
		logger.debug( "OUT: tenant = [{0}]" + tenantId);
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
}
