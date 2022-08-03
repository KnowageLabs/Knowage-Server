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
package it.eng.spagobi.commons.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.safehaus.uuid.UUIDGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRolesId;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasourceId;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductTypeId;
import it.eng.spagobi.commons.metadata.SbiOrganizationTheme;
import it.eng.spagobi.commons.metadata.SbiOrganizationThemeId;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
@SuppressWarnings("all")
public class TenantsDAOHibImpl extends AbstractHibernateDAO implements ITenantsDAO {

	static private Logger logger = Logger.getLogger(TenantsDAOHibImpl.class);

	public static final String ADMIN_USER_ID_SUFFIX = "_admin";
	public static final String ADMIN_USER_NAME_SUFFIX = " ADMIN";

	@Override
	public SbiTenant loadTenantByName(String name) throws EMFUserError {
		logger.debug("IN");
		SbiTenant tenant = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiTenant.class);
			criteria.add(labelCriterrion);
			tenant = (SbiTenant) criteria.uniqueResult();
			if (tenant != null)
				Hibernate.initialize(tenant.getSbiOrganizationThemes());
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the tenant with name " + name, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return tenant;
	}

	@Override
	public SbiTenant loadTenantById(Integer id) throws EMFUserError {
		logger.debug("IN");
		SbiTenant tenant = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("id", id);
			Criteria criteria = tmpSession.createCriteria(SbiTenant.class);
			criteria.add(labelCriterrion);
			tenant = (SbiTenant) criteria.uniqueResult();

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading the tenant with id " + id, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return tenant;
	}

	@Override
	public List<SbiTenant> loadAllTenants() {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiTenant";
			Query query = aSession.createQuery(q);
			ArrayList<SbiTenant> result = (ArrayList<SbiTenant>) query.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error getting tenants", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List<SbiOrganizationDatasource> loadSelectedDS(String tenant) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("from SbiOrganizationDatasource ds where ds.sbiOrganizations.name = :tenantName");
			hibQuery.setString("tenantName", tenant);
			ArrayList<SbiOrganizationDatasource> result = (ArrayList<SbiOrganizationDatasource>) hibQuery.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error getting Tenant Data Sources", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List<SbiOrganizationProductType> loadSelectedProductTypes(String tenant) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("from SbiOrganizationProductType p where p.sbiOrganizations.name = :tenantName");
			hibQuery.setString("tenantName", tenant);
			ArrayList<SbiOrganizationProductType> result = (ArrayList<SbiOrganizationProductType>) hibQuery.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error getting Tenant Product Types", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List<Integer> loadSelectedProductTypesIds(String tenant) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession
					.createQuery("select p.sbiProductType.productTypeId from SbiOrganizationProductType p where p.sbiOrganizations.name = :tenantName");
			hibQuery.setString("tenantName", tenant);
			ArrayList<Integer> result = (ArrayList<Integer>) hibQuery.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error getting Tenant Product Types", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public Set loadThemesByTenantName(String name) throws EMFUserError {
		logger.debug("IN");
		SbiTenant tenant = null;
		Session tmpSession = null;
		Transaction tx = null;
		Set themes = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("name", name);
			Criteria criteria = tmpSession.createCriteria(SbiTenant.class);
			criteria.add(labelCriterrion);
			tenant = (SbiTenant) criteria.uniqueResult();

			tx.commit();

			if (tenant != null)
				themes = tenant.getSbiOrganizationThemes();
		} catch (HibernateException he) {
			logger.error("Error while loading the tenant with name " + name, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		logger.debug("OUT");
		return themes;
	}

	@Override
	public void insertTenant(SbiTenant aTenant) throws EMFUserError {
		logger.debug("insertTenant IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			this.disableTenantFilter(aSession);

			updateSbiCommonInfo4Insert(aTenant);
			Integer idTenant = (Integer) aSession.save(aTenant);
			aSession.flush();

			aTenant.setId(idTenant);

			SbiCommonInfo sbiCommoInfo = new SbiCommonInfo();
			sbiCommoInfo.setOrganization(aTenant.getName());

			Set<SbiOrganizationDatasource> ds = aTenant.getSbiOrganizationDatasources();
			for (SbiOrganizationDatasource sbiOrganizationDatasource : ds) {
				SbiDataSource sbiDs = sbiOrganizationDatasource.getSbiDataSource();
				sbiOrganizationDatasource.setId(new SbiOrganizationDatasourceId(sbiDs.getDsId(), idTenant));
				sbiOrganizationDatasource.setCommonInfo(sbiCommoInfo);
				updateSbiCommonInfo4Insert(sbiOrganizationDatasource);
				aSession.save(sbiOrganizationDatasource);
				aSession.flush();
			}

			// Set<SbiOrganizationEngine> engines = aTenant.getSbiOrganizationEngines();
			// for (SbiOrganizationEngine sbiOrganizationEngine : engines) {
			// SbiEngines sbiEngine = sbiOrganizationEngine.getSbiEngines();
			// sbiOrganizationEngine.setId(new SbiOrganizationEngineId(sbiEngine.getEngineId(), idTenant));
			// sbiOrganizationEngine.setCommonInfo(sbiCommoInfo);
			// updateSbiCommonInfo4Insert(sbiOrganizationEngine);
			// aSession.save(sbiOrganizationEngine);
			// aSession.flush();
			// }

			Set<SbiOrganizationProductType> productType = aTenant.getSbiOrganizationProductType();
			for (SbiOrganizationProductType sbiOrganizationProductType : productType) {
				SbiProductType sbiProductType = sbiOrganizationProductType.getSbiProductType();
				sbiOrganizationProductType.setId(new SbiOrganizationProductTypeId(sbiProductType.getProductTypeId(), idTenant));
				sbiOrganizationProductType.setCommonInfo(sbiCommoInfo);
				updateSbiCommonInfo4Insert(sbiOrganizationProductType);
				aSession.save(sbiOrganizationProductType);
				aSession.flush();
			}

			Set<SbiOrganizationTheme> themes = aTenant.getSbiOrganizationThemes();
			for (SbiOrganizationTheme theme : themes) {
				SbiOrganizationTheme sbiOrganizationTheme = new SbiOrganizationTheme();
				sbiOrganizationTheme.setId(theme.getId());
				sbiOrganizationTheme.setThemeName(theme.getThemeName());
				sbiOrganizationTheme.setConfig(theme.getConfig());
				sbiOrganizationTheme.setActive(theme.isActive());
				sbiOrganizationTheme.setCommonInfo(sbiCommoInfo);
				updateSbiCommonInfo4Insert(sbiOrganizationTheme);
				aSession.save(sbiOrganizationTheme);
				aSession.flush();
			}
			tx.commit();

			// initAlarmForTenant(aTenant);

		} catch (HibernateException he) {
			logger.error("Error while inserting the tenant with id " + ((aTenant == null) ? "" : String.valueOf(aTenant.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("insertTenant OUT");
			}
		}
	}

	/*
	 * Method copied from it.eng.spagobi.tools.scheduler.init.AlarmQuartzInitializer for removing dependecy
	 */
	public void initAlarmForTenant(SbiTenant tenant) {
		try {
			logger.debug("IN");
			boolean alreadyInDB = false;
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.setTenant(tenant.getName());
			alreadyInDB = schedulerDAO.jobExists("AlarmInspectorJob", "AlarmInspectorJob");
			if (!alreadyInDB) {

				// CREATE JOB DETAIL
				Job jobDetail = new Job();
				jobDetail.setName("AlarmInspectorJob");
				jobDetail.setGroupName("AlarmInspectorJob");
				jobDetail.setDescription("AlarmInspectorJob");
				jobDetail.setDurable(true);
				jobDetail.setVolatile(false);
				jobDetail.setRequestsRecovery(true);

				schedulerDAO.insertJob(jobDetail);

				Calendar startDate = new java.util.GregorianCalendar(2012, Calendar.JANUARY, 01);
				startDate.set(Calendar.AM_PM, Calendar.AM);
				startDate.set(Calendar.HOUR, 00);
				startDate.set(Calendar.MINUTE, 00);
				startDate.set(Calendar.SECOND, 0);
				startDate.set(Calendar.MILLISECOND, 0);

				String nameTrig = "schedule_uuid_" + UUIDGenerator.getInstance().generateTimeBasedUUID().toString();

				CronExpression cronExpression = new CronExpression("minute{numRepetition=5}");

				Trigger simpleTrigger = new Trigger();
				simpleTrigger.setName(nameTrig);
				simpleTrigger.setStartTime(startDate.getTime());
				simpleTrigger.setJob(jobDetail);
				simpleTrigger.setCronExpression(cronExpression);
				simpleTrigger.setRunImmediately(false);

				schedulerDAO.insertTrigger(simpleTrigger);

				logger.debug("Added job with name AlarmInspectorJob");
			}
			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error while initializing scheduler ", e);
		}
	}

	@Override
	public SbiUser initializeAdminUser(SbiTenant aTenant) {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiUser toReturn = null;

		Assert.assertNotNull(aTenant, "Tenant in input is null");

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String userId = getAdminUserId(aTenant);
			ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
			SbiUser existingUser = userDAO.loadSbiUserByUserId(userId);
			if (existingUser != null) {
				logger.error("Cannot initialize admin user for tenant " + aTenant.getName() + ": user [" + userId + "] already existing");
				throw new SpagoBIRuntimeException("Cannot initialize admin user for tenant " + aTenant.getName() + ": user [" + userId + "] already existing");
			}

			Role adminRole = createAdminRoleForTenant(aTenant.getName());
			logger.debug("Storing user [" + userId + "] into database ...");
			SbiUser tenantAdmin = new SbiUser();
			tenantAdmin.setUserId(userId);
			tenantAdmin.setFullName(aTenant.getName() + ADMIN_USER_NAME_SUFFIX);
			String pwd = Password.encriptPassword(userId);
			tenantAdmin.setPassword(pwd);
			tenantAdmin.setIsSuperadmin(false);
			tenantAdmin.getCommonInfo().setOrganization(aTenant.getName());
			Integer newId = userDAO.saveSbiUser(tenantAdmin);
			setRole(adminRole, tenantAdmin);
			logger.debug("User [" + userId + "] sucesfully stored into database with id [" + newId + "]");
			toReturn = userDAO.loadSbiUserById(newId);
		} catch (Exception e) {
			logger.error("Error while trying to initialize admin for tenant " + aTenant.getName() + ": " + e.getMessage(), e);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error while trying to initialize admin for tenant " + aTenant.getName(), e);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
			logger.debug("OUT");
		}

		return toReturn;
	}

	private String getAdminUserId(SbiTenant aTenant) {
		Assert.assertNotNull(aTenant, "Tenant in input is null");
		logger.debug("IN: tenant = [" + aTenant.getName() + "]");
		String userId = aTenant.getName().toLowerCase() + ADMIN_USER_ID_SUFFIX;
		logger.debug("OUT: user id = [" + userId + "]");
		return userId;
	}

	private Role createAdminRoleForTenant(String tenant) {
		logger.debug("IN: tenant is " + tenant);
		Role aRole = new Role();
		try {
			RoleDAOHibImpl roleDAO = new RoleDAOHibImpl();
			roleDAO.setTenant(tenant);
			String roleName = "/" + tenant.toLowerCase() + "/admin";
			logger.debug("Role name is [" + roleName + "]");
			Role existingRole = roleDAO.loadByName(roleName);
			if (existingRole != null) {
				logger.debug("Role [" + roleName + "] already exists");
				return existingRole;
			}
			aRole.setName(roleName);
			aRole.setDescription(roleName);
			aRole.setOrganization(tenant);
			Domain domain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("ROLE_TYPE", "ADMIN");
			aRole.setRoleTypeCD("ADMIN");
			aRole.setRoleTypeID(domain.getValueId());
			setDefaultAuthorizationsForAdmin(aRole);
			// roleDAO.insertRole(aRole);
			roleDAO.insertRoleComplete(aRole);
			Role toReturn = roleDAO.loadByName(roleName);
			return toReturn;
		} catch (Exception e) {
			logger.error("An unexpected error occurred while creating admin role for tenant " + tenant, e);
			throw new SpagoBIRuntimeException("An unexpected error occurred while creating admin role for tenant " + tenant, e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void setDefaultAuthorizationsForAdmin(Role aRole) {
		aRole.setAbleToDeleteKpiComm(true);
		aRole.setAbleToEditAllKpiComm(true);
		aRole.setAbleToEditMyKpiComm(true);
		aRole.setIsAbleToBuildQbeQuery(true);
		aRole.setIsAbleToCreateDocuments(true);
		aRole.setIsAbleToCreateSocialAnalysis(true);
		aRole.setIsAbleToDoMassiveExport(true);
		aRole.setIsAbleToEnableDatasetPersistence(true);
		aRole.setIsAbleToEnableFederatedDataset(true);
		aRole.setIsAbleToEnableRate(true);
		aRole.setIsAbleToEnablePrint(true);
		aRole.setIsAbleToEnableCopyAndEmbed(true);
		aRole.setIsAbleToHierarchiesManagement(true);
		aRole.setIsAbleToManageUsers(true);
		aRole.setIsAbleToSaveIntoPersonalFolder(true);
		aRole.setIsAbleToSaveMetadata(true);
		aRole.setIsAbleToSaveRememberMe(true);
		aRole.setIsAbleToEditPythonScripts(true);
		aRole.setIsAbleToCreateCustomChart(true);
		aRole.setIsAbleToSaveSubobjects(true);
		aRole.setIsAbleToSeeDocumentBrowser(true);
		aRole.setIsAbleToSeeFavourites(true);
		aRole.setIsAbleToSeeMetadata(true);
		aRole.setIsAbleToSeeMyData(true);
		aRole.setIsAbleToSeeMyWorkspace(true);
		aRole.setIsAbleToSeeNotes(true);
		aRole.setIsAbleToSeeSnapshots(true);
		aRole.setIsAbleToRunSnapshots(true);
		aRole.setIsAbleToSeeSubobjects(true);
		aRole.setIsAbleToSeeSubscriptions(true);
		aRole.setIsAbleToSeeToDoList(true);
		aRole.setIsAbleToSeeViewpoints(true);
		aRole.setIsAbleToSendMail(true);
		aRole.setIsAbleToViewSocialAnalysis(true);
	}

	private void setRole(Role role, SbiUser tenantAdmin) {
		logger.debug("IN");
		SbiExtUserRoles sbiExtUserRole = new SbiExtUserRoles();
		SbiExtUserRolesId id = new SbiExtUserRolesId();
		try {
			ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
			Integer extRoleId = role.getId();
			id.setExtRoleId(extRoleId); // role Id
			id.setId(tenantAdmin.getId()); // user Id
			sbiExtUserRole.setSbiUser(tenantAdmin);
			sbiExtUserRole.setId(id);
			sbiExtUserRole.getCommonInfo().setOrganization(role.getOrganization());
			userDAO.updateSbiUserRoles(sbiExtUserRole);
			// RoleDAOHibImpl roleDAO = new RoleDAOHibImpl();
			// userDAO.updateSbiUserRoles(sbiExtUserRole);
		} catch (Exception e) {
			logger.error("An unexpected error occurred while associating role [" + role.getName() + "] to user with id " + tenantAdmin, e);
			throw new SpagoBIRuntimeException("An unexpected error occurred while associating role [" + role.getName() + "] to user with id " + tenantAdmin, e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void modifyTenant(SbiTenant aTenant) throws EMFUserError, Exception {
		logger.debug("modifyTenant IN");
		final Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();

			// load tenant by id
			// check if name is the same, otherwise throw an exception
			SbiTenant tenant = loadTenantById(aTenant.getId());
			if (!tenant.getName().equalsIgnoreCase(aTenant.getName())) {
				throw new SpagoBIRuntimeException("It's not allowed to modify the name of an existing Tenant.");
			}

			updateSbiCommonInfo4Update(aTenant);
			aSession.update(aTenant);
			aSession.flush();

			this.disableTenantFilter(aSession);

			SbiCommonInfo sbiCommoInfo = new SbiCommonInfo();
			sbiCommoInfo.setOrganization(aTenant.getName());

			UserProfile profile = (UserProfile) getUserProfile();

			Set<SbiOrganizationDatasource> ds = aTenant.getSbiOrganizationDatasources();
			ArrayList<Integer> datasourceToBeAss = new ArrayList<Integer>();
			ArrayList<Integer> datasourceToBeInsert = new ArrayList<Integer>();
			// get a list of datasource ids
			Iterator itds = ds.iterator();
			while (itds.hasNext()) {
				SbiOrganizationDatasource dsI = (SbiOrganizationDatasource) itds.next();
				datasourceToBeAss.add(dsI.getSbiDataSource().getDsId());
				datasourceToBeInsert.add(dsI.getSbiDataSource().getDsId());
			}
			Query hibQuery = aSession.createQuery(
					"from SbiOrganizationDatasource ds where ds.sbiOrganizations.id = :idTenant and (ds.sbiDataSource.commonInfo.userIn = :userId or length(ds.sbiDataSource.jndi) > 0)");
			hibQuery.setInteger("idTenant", aTenant.getId());
			hibQuery.setString("userId", profile.getUserId().toString());
			ArrayList<SbiOrganizationDatasource> existingDsAssociated = (ArrayList<SbiOrganizationDatasource>) hibQuery.list();

			boolean deletedSomedsOrgAss = false;
			List<Integer> idsDeleted = new ArrayList<Integer>();
			if (existingDsAssociated != null) {
				Iterator it = existingDsAssociated.iterator();
				while (it.hasNext()) {
					SbiOrganizationDatasource assDS = (SbiOrganizationDatasource) it.next();

					// check whether the ds has to be associated:
					if (datasourceToBeAss.contains(assDS.getSbiDataSource().getDsId())) {
						// already existing --> do nothing but delete it from the list of associations
						datasourceToBeInsert.remove(new Integer(assDS.getSbiDataSource().getDsId()));
					} else {
						// not existing --> must be deleted
						Query docsQ = aSession.createQuery("from SbiObjects o where o.dataSource.dsId = :idDS and o.commonInfo.organization = :tenant");
						docsQ.setInteger("idDS", assDS.getSbiDataSource().getDsId());
						docsQ.setString("tenant", aTenant.getName());
						ArrayList<Object> docs = (ArrayList<Object>) docsQ.list();
						if (docs != null && !docs.isEmpty()) {
							tx.rollback();
							throw new Exception("datasource:" + assDS.getSbiDataSource().getLabel());

						} else {

							// check no model associated
							Query modelQ = aSession.createQuery("from SbiMetaModel m where m.dataSource.dsId = :idDS and m.commonInfo.organization = :tenant");
							modelQ.setInteger("idDS", assDS.getSbiDataSource().getDsId());
							modelQ.setString("tenant", aTenant.getName());
							ArrayList<Object> models = (ArrayList<Object>) modelQ.list();
							if (models != null && !models.isEmpty()) {
								tx.rollback();
								throw new Exception("datasource:" + assDS.getSbiDataSource().getLabel());

							} else {
								idsDeleted.add(assDS.getSbiDataSource().getDsId());
								aSession.delete(assDS);
								aSession.flush();
								deletedSomedsOrgAss = true;
							}
						}

					}

				}

				// check if a datasource among the one whose association was deleted remained without any tenant and delete it
				if (deletedSomedsOrgAss) {
					deleteUnusedDataSource(aSession, idsDeleted);
				}

			}
			// insert filtered datasources ds list
			for (Integer idDsToAss : datasourceToBeInsert) {
				SbiOrganizationDatasource sbiOrganizationDatasource = new SbiOrganizationDatasource();
				sbiOrganizationDatasource.setId(new SbiOrganizationDatasourceId(idDsToAss, aTenant.getId()));
				sbiOrganizationDatasource.setCommonInfo(sbiCommoInfo);
				updateSbiCommonInfo4Insert(sbiOrganizationDatasource);
				aSession.save(sbiOrganizationDatasource);
				aSession.flush();
			}

			// Product Type Association Management
			// first delete product types relationship with this tenant then insert the new ones

			Set<SbiOrganizationProductType> productTypes = aTenant.getSbiOrganizationProductType();
			ArrayList<Integer> productTypesToBeAss = new ArrayList<Integer>();
			ArrayList<Integer> productTypesToBeInsert = new ArrayList<Integer>();
			// get a list of product types ids
			Iterator itproduct = productTypes.iterator();
			while (itproduct.hasNext()) {
				SbiOrganizationProductType aOrganizationProductType = (SbiOrganizationProductType) itproduct.next();
				productTypesToBeAss.add(aOrganizationProductType.getSbiProductType().getProductTypeId());
				productTypesToBeInsert.add(aOrganizationProductType.getSbiProductType().getProductTypeId());
			}

			hibQuery = aSession.createQuery("from SbiOrganizationProductType p where p.sbiOrganizations.id = :idTenant");
			hibQuery.setInteger("idTenant", aTenant.getId());
			ArrayList<SbiOrganizationProductType> existingProductTypeAssociated = (ArrayList<SbiOrganizationProductType>) hibQuery.list();
			if (existingProductTypeAssociated != null) {
				Iterator it = existingProductTypeAssociated.iterator();
				while (it.hasNext()) {
					SbiOrganizationProductType assProductType = (SbiOrganizationProductType) it.next();

					if (productTypesToBeAss.contains(assProductType.getSbiProductType().getProductTypeId())) {
						// already existing --> do nothing but delete it from the list of associations
						productTypesToBeInsert.remove(assProductType.getSbiProductType().getProductTypeId());
					} else {
						// not existing --> remove the association on the db
						aSession.delete(assProductType);
						aSession.flush();
					}
				}
			}
			// insert filtered product types list
			for (Integer idProductTypeToAss : productTypesToBeInsert) {
				SbiOrganizationProductType sbiOrganizationProductType = new SbiOrganizationProductType();
				sbiOrganizationProductType.setId(new SbiOrganizationProductTypeId(idProductTypeToAss, aTenant.getId()));
				sbiOrganizationProductType.setCommonInfo(sbiCommoInfo);
				updateSbiCommonInfo4Insert(sbiOrganizationProductType);
				aSession.save(sbiOrganizationProductType);
				aSession.flush();
			}

			// check associations between roles and authorizations to remove (possibly)
			checkAuthorizationsRoles(aTenant, existingProductTypeAssociated, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the tenant with id " + ((aTenant == null) ? "" : String.valueOf(aTenant.getId())), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("modifyTenant OUT");
			}
		}
	}

	/**
	 * @param aTenant
	 * @param aSession
	 * @param sbiCommoInfo
	 */
	protected String handleThemes(SbiTenant aTenant) {

		Transaction tx = null;
		final Session aSession = getSession();
		String newUuid = null;
		try {
			tx = aSession.beginTransaction();
			SbiCommonInfo sbiCommoInfo = new SbiCommonInfo();
			sbiCommoInfo.setOrganization(aTenant.getName());
			Query hibQuery = aSession.createQuery("from SbiOrganizationTheme p where p.id.organizationId = :idTenant");
			hibQuery.setInteger("idTenant", aTenant.getId());

			ArrayList<SbiOrganizationTheme> existingSbiOrganizationThemes = (ArrayList<SbiOrganizationTheme>) hibQuery.list();

			existingSbiOrganizationThemes.stream().filter(x -> x.getId().getUuid() != null).forEach(x -> {

				Optional s = aTenant.getSbiOrganizationThemes().stream().filter(y -> x.getId().getUuid().equals(((SbiOrganizationTheme) y).getId().getUuid()))
						.findFirst();

				Query q = aSession.createQuery("from SbiOrganizationTheme p where p.id.organizationId = :idTenant and p.id.uuid = :uuid");
				q.setInteger("idTenant", x.getId().getOrganizationId());
				q.setString("uuid", x.getId().getUuid());
				SbiOrganizationTheme sbiOrganizationTheme = (SbiOrganizationTheme) q.uniqueResult();

				if (s.isPresent()) {
					SbiOrganizationTheme newSbiOrganizationTheme = new SbiOrganizationTheme();
					newSbiOrganizationTheme.setId(((SbiOrganizationTheme) s.get()).getId());
					newSbiOrganizationTheme.setThemeName(((SbiOrganizationTheme) s.get()).getThemeName());

					newSbiOrganizationTheme.setConfig(((SbiOrganizationTheme) s.get()).getConfig());

					newSbiOrganizationTheme.setActive(((SbiOrganizationTheme) s.get()).isActive());

					aSession.delete(sbiOrganizationTheme);
					aSession.flush();

					newSbiOrganizationTheme.setCommonInfo(sbiCommoInfo);
					updateSbiCommonInfo4Insert(newSbiOrganizationTheme);

					aSession.save(newSbiOrganizationTheme);

				} else {
					aSession.delete(sbiOrganizationTheme);

				}
				aSession.flush();

			});

			newUuid = UUID.randomUUID().toString();

			String uuidForLambda = newUuid;
			// newThemes
			aTenant.getSbiOrganizationThemes().stream().filter(y -> ((SbiOrganizationTheme) y).getId().getUuid() == null).forEach(x -> {

				SbiOrganizationTheme newSbiOrganizationTheme = (SbiOrganizationTheme) x;
				newSbiOrganizationTheme.getId().setUuid(uuidForLambda);
				newSbiOrganizationTheme.setCommonInfo(sbiCommoInfo);
				updateSbiCommonInfo4Insert(newSbiOrganizationTheme);

				aSession.save(newSbiOrganizationTheme);
				aSession.flush();

			});
			tx.commit();
		} catch (HibernateException he) {
			String message = "Error while updating themes for the tenant " + aTenant.getName();
			logger.error(message, he);

			if (tx != null)
				tx.rollback();

			throw new SpagoBIRuntimeException(message);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("modifyTenant OUT");
			}
		}
		return newUuid;
	}

	/**
	 * Remove not valid association between authorizations and roles after changing product types related to a tenant
	 *
	 * @param aTenant
	 * @param aSession
	 */
	private void checkAuthorizationsRoles(SbiTenant aTenant, ArrayList<SbiOrganizationProductType> existingProductTypeAssociated, Session aSession) {
		// 1) Get the product types id currently configured for the tenant

		Set<SbiOrganizationProductType> productTypes = aTenant.getSbiOrganizationProductType();
		ArrayList<Integer> productTypesIds = new ArrayList<Integer>();
		// get a list of product types ids
		Iterator itproduct = productTypes.iterator();
		while (itproduct.hasNext()) {
			SbiOrganizationProductType aOrganizationProductType = (SbiOrganizationProductType) itproduct.next();
			productTypesIds.add(aOrganizationProductType.getSbiProductType().getProductTypeId());
		}

		// 2) Get ids of the authorizations related to this product types
		String hql = "select f.id from SbiAuthorizations f where f.productType.productTypeId IN (:PRODUCT_TYPES)";

		Query hqlQueryAut = aSession.createQuery(hql);
		hqlQueryAut.setParameterList("PRODUCT_TYPES", productTypesIds);
		List<Integer> authorizations = hqlQueryAut.list();

		// 3) Check if, for this tenant, there are associations between roles and authorizations
		// for product types not longer associated to the tenant
		hql = "select autrole from SbiAuthorizationsRoles autrole where autrole.commonInfo.organization = :TENANT and autrole.id.authorizationId NOT IN (:AUTHORIZATIONS)";
		Query hqlQueryAutRole = aSession.createQuery(hql);
		hqlQueryAutRole.setString("TENANT", aTenant.getName());
		hqlQueryAutRole.setParameterList("AUTHORIZATIONS", authorizations);
		List<SbiAuthorizationsRoles> associations = new ArrayList<SbiAuthorizationsRoles>();
		associations = hqlQueryAutRole.list();

		// 4) Get current existing association between roles and authorizations (before changing products)
		ArrayList<Integer> oldProductTypesIds = new ArrayList<Integer>();
		// get possible authorizations of current (old) products types
		for (SbiOrganizationProductType existingProductType : existingProductTypeAssociated) {
			oldProductTypesIds.add(existingProductType.getSbiProductType().getProductTypeId());
		}

		List<Integer> oldAuthorizations;
		if (oldProductTypesIds.size() > 0) {
			String oldHql = "select f.id from SbiAuthorizations f where f.productType.productTypeId IN (:PRODUCT_TYPES)";
			Query oldHqlQueryAut = aSession.createQuery(oldHql);
			oldHqlQueryAut.setParameterList("PRODUCT_TYPES", oldProductTypesIds);
			oldAuthorizations = oldHqlQueryAut.list();
		} else {
			oldAuthorizations = new ArrayList<Integer>(0);
		}

		// search current associations roles-authorizations of current (old) product types
		List<SbiAuthorizationsRoles> oldAssociations;
		if (oldAuthorizations.size() > 0) {
			String oldHql = "select autrole from SbiAuthorizationsRoles autrole where autrole.commonInfo.organization = :TENANT and autrole.id.authorizationId IN (:AUTHORIZATIONS)";
			Query oldHqlQueryAutRole = aSession.createQuery(oldHql);
			oldHqlQueryAutRole.setString("TENANT", aTenant.getName());
			oldHqlQueryAutRole.setParameterList("AUTHORIZATIONS", oldAuthorizations);
			oldAssociations = oldHqlQueryAutRole.list();
		} else {
			oldAssociations = new ArrayList<SbiAuthorizationsRoles>(0);
		}

		// save the old authorizations names and associated roles to be applied again with new products (if not already set)
		Map<String, Set<SbiExtRoles>> oldAuthMap = new HashMap<String, Set<SbiExtRoles>>();
		for (SbiAuthorizationsRoles oldAssociation : oldAssociations) {
			String anAuthName = oldAssociation.getSbiAuthorizations().getName();
			if (oldAuthMap.containsKey(anAuthName)) {
				// authorization name previously found in the map, just update the roles set
				SbiExtRoles role = oldAssociation.getSbiExtRoles();
				Set<SbiExtRoles> roles = oldAuthMap.get(anAuthName);
				// update the roles set
				roles.add(role);
				oldAuthMap.put(anAuthName, roles);
			} else {
				// new authorization found
				SbiExtRoles role = oldAssociation.getSbiExtRoles();
				Set<SbiExtRoles> roles = new HashSet<SbiExtRoles>();
				roles.add(role);
				oldAuthMap.put(anAuthName, roles);
			}
		}

		// 5) Remove not valid associations with new products
		Iterator it = associations.iterator();
		while (it.hasNext()) {
			SbiAuthorizationsRoles anAssociation = (SbiAuthorizationsRoles) it.next();
			aSession.delete(anAssociation);
			aSession.flush();
		}

		// 6) Restore old associations also valid for new products (if not already set)
		restorePreviousAuthorizationsRoles(aSession, oldAuthMap, aTenant, productTypesIds);

	}

	/**
	 * Restore authorizations valid for specific roles after changing the product types associated with specific tenant
	 *
	 * @param aSession
	 * @param oldAuthMap      a map that contains authorizations names as keys and roles as values
	 * @param aTenant         the specific tenant
	 * @param productTypesIds the new products types associated with the tenant
	 */
	private void restorePreviousAuthorizationsRoles(Session aSession, Map<String, Set<SbiExtRoles>> oldAuthMap, SbiTenant aTenant,
			ArrayList<Integer> productTypesIds) {
		// iterate the map for each authorization and enable it on the same roles if this is possible also with the new product types
		for (Map.Entry<String, Set<SbiExtRoles>> entry : oldAuthMap.entrySet()) {
			String oldAuthName = entry.getKey();
			// search if this old association is also valid for new product type set
			String hqlRestoreAut = "select f from SbiAuthorizations f where  f.name = :AUTHORIZATION_NAME AND f.productType.productTypeId IN (:PRODUCT_TYPES) ";
			Query hqlQueryRestoreAut = aSession.createQuery(hqlRestoreAut);
			hqlQueryRestoreAut.setString("AUTHORIZATION_NAME", oldAuthName);
			hqlQueryRestoreAut.setParameterList("PRODUCT_TYPES", productTypesIds);
			List<SbiAuthorizations> autsToRestore = hqlQueryRestoreAut.list();

			if (!autsToRestore.isEmpty()) {
				// old auth is also valid for new products, so restore the associations with the same roles
				for (SbiAuthorizations autToRestore : autsToRestore) {
					Set<SbiExtRoles> roles = entry.getValue();
					for (SbiExtRoles role : roles) {

						// Check if the association Authorization-Role is already present
						String checkAutHql = "select autrole from SbiAuthorizationsRoles autrole where autrole.commonInfo.organization = :TENANT and autrole.id.authorizationId = :AUTHORIZATION AND autrole.id.roleId = :ROLE";
						Query checkAutHqlQuery = aSession.createQuery(checkAutHql);
						checkAutHqlQuery.setString("TENANT", aTenant.getName());
						checkAutHqlQuery.setInteger("AUTHORIZATION", autToRestore.getId());
						checkAutHqlQuery.setInteger("ROLE", role.getExtRoleId());

						List<SbiAuthorizationsRoles> checkAssociations = new ArrayList<SbiAuthorizationsRoles>();
						checkAssociations = checkAutHqlQuery.list();

						if (checkAssociations.isEmpty()) {
							// the association is not already present on the db so we can insert it
							SbiAuthorizationsRoles sbiAuthorizationsRoles = new SbiAuthorizationsRoles();
							SbiAuthorizationsRolesId sbiAuthorizationsRolesId = new SbiAuthorizationsRolesId(autToRestore.getId(), role.getExtRoleId());

							sbiAuthorizationsRoles.setId(sbiAuthorizationsRolesId);
							sbiAuthorizationsRoles.setSbiAuthorizations(autToRestore);
							sbiAuthorizationsRoles.setSbiExtRoles(role);
							// force the correct tenant (otherwise the super admin tenant's was used)
							sbiAuthorizationsRoles.setOrganization(aTenant.getName());
							sbiAuthorizationsRoles.getCommonInfo().setOrganization(aTenant.getName());

							updateSbiCommonInfo4Update(sbiAuthorizationsRoles);
							// Save on db
							aSession.save(sbiAuthorizationsRoles);
							aSession.flush();
						}

					}
				}
			}

		}
	}

	@Override
	public void deleteTenant(SbiTenant aTenant) throws EMFUserError {

		logger.debug("deleteTenant IN");
		logger.debug("tentant is equal to [" + aTenant.getName() + "]");
		Session aSession = null;
		Connection jdbcConnection = null;
		InputStream is = null;
		try {
			aSession = getSession();
			jdbcConnection = HibernateSessionManager.getConnection(aSession);
			jdbcConnection.setAutoCommit(false);
			Thread curThread = Thread.currentThread();
			ClassLoader classLoad = curThread.getContextClassLoader();
			is = classLoad.getResourceAsStream("it/eng/spagobi/commons/dao/deleteTenant.sql");
			String str = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if (is != null) {
				while ((str = reader.readLine()) != null) {
					if (!str.isEmpty() && !str.trim().startsWith("--")) {
						PreparedStatement statement = jdbcConnection.prepareStatement(str);
						logger.debug("\n" + str + "\n");
						statement.setString(1, aTenant.getName());
						statement.execute();
						statement.close();
					}
				}
			}
			StringEscapeUtils seu = new StringEscapeUtils();

			str = "DELETE FROM QRTZ_CRON_TRIGGERS WHERE trigger_name IN (SELECT DISTINCT t.trigger_name " + "FROM QRTZ_TRIGGERS t WHERE t.JOB_GROUP LIKE '"
					+ seu.escapeSql(aTenant.getName()) + "/%') "
					+ "AND trigger_group IN (SELECT DISTINCT t.trigger_group FROM QRTZ_TRIGGERS t WHERE t.JOB_GROUP " + "LIKE '"
					+ seu.escapeSql(aTenant.getName()) + "/%')";
			PreparedStatement statement = jdbcConnection.prepareStatement(str);
			statement.execute();
			statement.close();

			str = "DELETE FROM QRTZ_TRIGGERS WHERE JOB_GROUP LIKE '" + seu.escapeSql(aTenant.getName()) + "/%'";
			statement = jdbcConnection.prepareStatement(str);
			statement.execute();
			statement.close();

			str = "DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_GROUP LIKE '" + seu.escapeSql(aTenant.getName()) + "/%'";
			statement = jdbcConnection.prepareStatement(str);
			statement.execute();
			statement.close();

			jdbcConnection.commit();

		} catch (Exception e) {
			logger.error("Error while deleting the tenant with id " + ((aTenant == null) ? "" : String.valueOf(aTenant.getId())), e);

			// Added
			e.printStackTrace();

			if (jdbcConnection != null) {
				try {
					jdbcConnection.rollback();
				} catch (SQLException ex) {
					logger.error("Error while deleting the tenant with id " + ((aTenant == null) ? "" : String.valueOf(aTenant.getId())), ex);
				}
			}

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Error closing stream", e);
				}
			try {
				if (jdbcConnection != null && !jdbcConnection.isClosed()) {
					jdbcConnection.close();
				}
			} catch (SQLException ex) {
				logger.error("Error while deleting the tenant with id " + ((aTenant == null) ? "" : String.valueOf(aTenant.getId())), ex);
			}
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("deleteTenant OUT");
			}
		}
	}

	/**
	 * When modifying a tenant if a datasource remains with no tenant delete it
	 *
	 * @param aSession
	 * @param ids:     id of <tenant,dataource> modified
	 * @throws EMFUserError
	 */

	public void deleteUnusedDataSource(Session aSession, List<Integer> ids) throws EMFUserError {
		logger.debug("IN");
		UserProfile profile = (UserProfile) this.getUserProfile();
		Assert.assertNotNull(profile, "User profile object is null; it must be provided");

		Query hibQuery = aSession.createQuery("from SbiDataSource ds where ds.commonInfo.userIn = :userId or (ds.jndi != '' and ds.jndi is not null)");
		hibQuery.setString("userId", profile.getUserId().toString());
		ArrayList<SbiDataSource> datasourceList = (ArrayList<SbiDataSource>) hibQuery.list();
		for (Iterator iterator = datasourceList.iterator(); iterator.hasNext();) {
			SbiDataSource sbiDataSource = (SbiDataSource) iterator.next();
			Integer dsId = sbiDataSource.getDsId();
			// check only datasource whose link to tenant has been modified
			if (ids.contains(dsId)) {
				Query hibQuery2 = aSession.createQuery("from SbiOrganizationDatasource ds where ds.id.datasourceId = :dsId");
				hibQuery2.setInteger("dsId", dsId);
				ArrayList<SbiOrganizationDatasource> dsOrganizations = (ArrayList<SbiOrganizationDatasource>) hibQuery2.list();
				if (dsOrganizations.isEmpty()) {
					logger.debug("delete datasource " + sbiDataSource.getLabel());
					aSession.delete(sbiDataSource);
					aSession.flush();
				}
			}
		}
		logger.debug("OUT");
	}

	@Override
	public String updateThemes(IEngUserProfile profile, String uuid, String themeName, ObjectNode newThemeConfig, boolean isActive) throws EMFUserError {
		ITenantsDAO tenantDao = DAOFactory.getTenantsDAO();
		tenantDao.setUserProfile(profile);
		Tenant tenantManager = TenantManager.getTenant();
		String tenantName = tenantManager.getName();
		SbiTenant tenant = tenantDao.loadTenantByName(tenantName);

		ObjectMapper mapper = new ObjectMapper();
		String newThemeConfigStr = null;

		if (!newThemeConfig.isEmpty(null)) {
			try {
				newThemeConfigStr = mapper.writeValueAsString(newThemeConfig);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				throw new SpagoBIRuntimeException("Error during theme config conversion", e1);
			}
		}

		if (uuid == null) {
			if (isActive) {
				tenant.getSbiOrganizationThemes().stream().forEach(x -> ((SbiOrganizationTheme) x).setActive(false));
			}

			SbiOrganizationThemeId id = new SbiOrganizationThemeId();
			id.setOrganizationId(tenant.getId());

			SbiOrganizationTheme newTheme = new SbiOrganizationTheme(themeName, newThemeConfigStr, isActive);
			newTheme.setId(id);
			updateSbiCommonInfo4Update(newTheme);
			tenant.getSbiOrganizationThemes().add(newTheme);

		} else {
			if (isActive) {
				tenant.getSbiOrganizationThemes().stream().forEach(x -> ((SbiOrganizationTheme) x).setActive(false));
			}

			String tmpNewThemeConfigStr = newThemeConfigStr;
			tenant.getSbiOrganizationThemes().stream().filter(x -> ((SbiOrganizationTheme) x).getId().getUuid().equals(uuid)).forEach(x -> {
				SbiOrganizationTheme sbiOrganizationTheme = (SbiOrganizationTheme) x;
				sbiOrganizationTheme.setConfig(tmpNewThemeConfigStr);
				sbiOrganizationTheme.setActive(isActive);
				sbiOrganizationTheme.setThemeName(themeName);
				updateSbiCommonInfo4Update(sbiOrganizationTheme);
			});
		}

		String newId = handleThemes(tenant);

		return newId;
	}

	@Override
	public void deleteTheme(IEngUserProfile profile, String themeId) throws EMFUserError {
		ITenantsDAO tenantDao = DAOFactory.getTenantsDAO();
		tenantDao.setUserProfile(profile);
		Tenant tenantManager = TenantManager.getTenant();
		String tenantName = tenantManager.getName();
		SbiTenant tenant = tenantDao.loadTenantByName(tenantName);
		Set<SbiOrganizationTheme> newSbiOrganizationThemes = new HashSet<>();

		tenant.getSbiOrganizationThemes().stream().forEach(x -> {

			SbiOrganizationTheme sbiOrganizationTheme = (SbiOrganizationTheme) x;

			if (sbiOrganizationTheme.getId().getUuid().equals(themeId))
				newSbiOrganizationThemes.add(sbiOrganizationTheme);
		});

		tenant.setSbiOrganizationThemes(newSbiOrganizationThemes);
		handleThemes(tenant);

	}

}