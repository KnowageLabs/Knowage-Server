/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasourceId;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductTypeId;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.kpi.alarm.service.AlarmInspectorJob;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiExtUserRolesId;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.security.Password;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.safehaus.uuid.UUIDGenerator;

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
			tx.commit();

			initAlarmForTenant(aTenant);

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
				jobDetail.setJobClass(AlarmInspectorJob.class);

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
			setRole(adminRole, newId);
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
		aRole.setIsAbleToEditWorksheet(true);
		aRole.setIsAbleToEnableDatasetPersistence(true);
		aRole.setIsAbleToHierarchiesManagement(true);
		aRole.setIsAbleToManageUsers(true);
		aRole.setIsAbleToSaveIntoPersonalFolder(true);
		aRole.setIsAbleToSaveMetadata(true);
		aRole.setIsAbleToSaveRememberMe(true);
		aRole.setIsAbleToSaveSubobjects(true);
		aRole.setIsAbleToSeeDocumentBrowser(true);
		aRole.setIsAbleToSeeFavourites(true);
		aRole.setIsAbleToSeeMetadata(true);
		aRole.setIsAbleToSeeMyData(true);
		aRole.setIsAbleToSeeNotes(true);
		aRole.setIsAbleToSeeSnapshots(true);
		aRole.setIsAbleToSeeSubobjects(true);
		aRole.setIsAbleToSeeSubscriptions(true);
		aRole.setIsAbleToSeeToDoList(true);
		aRole.setIsAbleToSeeViewpoints(true);
		aRole.setIsAbleToSendMail(true);
		aRole.setIsAbleToViewSocialAnalysis(true);
	}

	private void setRole(Role role, int userIdInt) {
		logger.debug("IN");
		SbiExtUserRoles sbiExtUserRole = new SbiExtUserRoles();
		SbiExtUserRolesId id = new SbiExtUserRolesId();
		try {
			ISbiUserDAO userDAO = DAOFactory.getSbiUserDAO();
			Integer extRoleId = role.getId();
			id.setExtRoleId(extRoleId); // role Id
			id.setId(userIdInt); // user Id
			sbiExtUserRole.setId(id);
			userDAO.updateSbiUserRoles(sbiExtUserRole);
			// RoleDAOHibImpl roleDAO = new RoleDAOHibImpl();
			// userDAO.updateSbiUserRoles(sbiExtUserRole);
		} catch (Exception e) {
			logger.error("An unexpected error occurred while associating role [" + role.getName() + "] to user with id " + userIdInt, e);
			throw new SpagoBIRuntimeException("An unexpected error occurred while associating role [" + role.getName() + "] to user with id " + userIdInt, e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void modifyTenant(SbiTenant aTenant) throws EMFUserError, Exception {
		logger.debug("modifyTenant IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// carica il tenant tramite ID
			// verifica che il nome sia uguale altrimenti eccezione
			SbiTenant tenant = loadTenantById(aTenant.getId());
			if (!tenant.getName().equalsIgnoreCase(aTenant.getName()))
				new SpagoBIRuntimeException("It's not allowed to modify the name of an existing Tenant.");

			updateSbiCommonInfo4Update(aTenant);
			aSession.update(aTenant);
			aSession.flush();

			this.disableTenantFilter(aSession);

			SbiCommonInfo sbiCommoInfo = new SbiCommonInfo();
			sbiCommoInfo.setOrganization(aTenant.getName());

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
			Query hibQuery = aSession.createQuery("from SbiOrganizationDatasource ds where ds.sbiOrganizations.id = :idTenant");
			hibQuery.setInteger("idTenant", aTenant.getId());
			ArrayList<SbiOrganizationDatasource> existingDsAssociated = (ArrayList<SbiOrganizationDatasource>) hibQuery.list();
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

								aSession.delete(assDS);
								aSession.flush();
							}
						}

					}

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

			// // cancello tutte le Engine associate al tenant
			//
			// Set<SbiOrganizationEngine> engines = aTenant.getSbiOrganizationEngines();
			// ArrayList<Integer> enginesToBeAss = new ArrayList<Integer>();
			// ArrayList<Integer> enginesToBeInsert = new ArrayList<Integer>();
			// // get a list of engines ids
			// Iterator iteng = engines.iterator();
			// while (iteng.hasNext()) {
			// SbiOrganizationEngine enI = (SbiOrganizationEngine) iteng.next();
			// enginesToBeAss.add(enI.getSbiEngines().getEngineId());
			// enginesToBeInsert.add(enI.getSbiEngines().getEngineId());
			// }
			//
			// hibQuery = aSession.createQuery("from SbiOrganizationEngine en where en.sbiOrganizations.id = :idTenant");
			// hibQuery.setInteger("idTenant", aTenant.getId());
			// ArrayList<SbiOrganizationEngine> existingEnginesAssociated = (ArrayList<SbiOrganizationEngine>) hibQuery.list();
			// if (existingEnginesAssociated != null) {
			// Iterator it = existingEnginesAssociated.iterator();
			// while (it.hasNext()) {
			// SbiOrganizationEngine assEng = (SbiOrganizationEngine) it.next();
			//
			// if (enginesToBeAss.contains(assEng.getSbiEngines().getEngineId())) {
			// // already existing --> do nothing but delete it from the list of associations
			// enginesToBeInsert.remove(assEng.getSbiEngines().getEngineId());
			// } else {
			//
			// Query docsQ = aSession.createQuery("from SbiObjects o where o.sbiEngines.engineId = :idEngine and o.commonInfo.organization = :tenant");
			// docsQ.setInteger("idEngine", assEng.getSbiEngines().getEngineId());
			// docsQ.setString("tenant", aTenant.getName());
			// ArrayList<Object> docs = (ArrayList<Object>) docsQ.list();
			// if (docs != null && !docs.isEmpty()) {
			// tx.rollback();
			// throw new Exception("engine:" + assEng.getSbiEngines().getName());
			//
			// } else {
			// aSession.delete(assEng);
			// aSession.flush();
			// }
			// }
			// }
			// }
			// // insert filtered engines list
			// for (Integer idEngToAss : enginesToBeInsert) {
			// SbiOrganizationEngine sbiOrganizationEngine = new SbiOrganizationEngine();
			// sbiOrganizationEngine.setId(new SbiOrganizationEngineId(idEngToAss, aTenant.getId()));
			// sbiOrganizationEngine.setCommonInfo(sbiCommoInfo);
			// updateSbiCommonInfo4Insert(sbiOrganizationEngine);
			// aSession.save(sbiOrganizationEngine);
			// aSession.flush();
			// }

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
			checkAuthorizationsRoles(aTenant, aSession);

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
	 * Remove not valid association between authorizations and roles after changing product types related to a tenant
	 *
	 * @param aTenant
	 * @param aSession
	 */
	private void checkAuthorizationsRoles(SbiTenant aTenant, Session aSession) {
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

		// 4) Remove not valid associations
		Iterator it = associations.iterator();
		while (it.hasNext()) {
			SbiAuthorizationsRoles anAssociation = (SbiAuthorizationsRoles) it.next();
			aSession.delete(anAssociation);
			aSession.flush();
		}

	}

	@Override
	public void deleteTenant(SbiTenant aTenant) throws EMFUserError {

		logger.debug("deleteTenant IN");
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
					if (!str.trim().startsWith("--")) {
						PreparedStatement statement = jdbcConnection.prepareStatement(str);
						// System.out.println("\n"+str+"\n");
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
					logger.error(e);
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

}