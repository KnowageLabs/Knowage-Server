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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseDet;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.dao.es.NoEventEmitting;
import it.eng.spagobi.commons.dao.es.RoleEventsEmittingCommand;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRolesId;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.mapcatalogue.metadata.SbiGeoLayersRoles;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Defines the Hibernate implementations for all DAO methods, for a Role.
 *
 * @author zoppello
 */
public class RoleDAOHibImpl extends AbstractHibernateDAO implements IRoleDAO {

	private static final Logger LOGGER = Logger.getLogger(RoleDAOHibImpl.class);

	public static final String DEFAULT_CACHE_SUFFIX = "_ROLE_CACHE";

	public static CacheManager cacheManager = null;

	private RoleEventsEmittingCommand eventEmittingCommand = new NoEventEmitting();

	/**
	 * Load by id.
	 *
	 * @param roleID the role id
	 * @return the role
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadByID(java.lang.Integer)
	 */
	@Override
	public Role loadByID(Integer roleID) throws EMFUserError {
		Monitor m = MonitorFactory.start("knowage_loadByID");
		Role toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		toReturn = getFromCache(String.valueOf(roleID));
		if (toReturn == null) {
			LOGGER.debug("Not found a Role [ " + roleID + " ] into the cache");
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();

				SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleID);

				toReturn = toRole(hibRole);
				putIntoCache(String.valueOf(toReturn.getId()), toReturn);
				tx.commit();
			} catch (HibernateException he) {
				logException(he);

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
		m.stop();
		return toReturn;
	}

	@Override
	public SbiExtRoles loadSbiExtRoleById(Integer roleId) throws EMFUserError {
		SbiExtRoles toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			toReturn = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);
			Hibernate.initialize(toReturn);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	/**
	 * Load by name.
	 *
	 * @param roleName the role name
	 * @return the role
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadByName(java.lang.String)
	 */
	@Override
	public Role loadByName(String roleName) throws EMFUserError {
		Role toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		toReturn = getFromCache(roleName);
		if (toReturn == null) {
			LOGGER.debug("Not found a Role [ " + roleName + " ] into the cache");
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();

				SbiExtRoles hibRole = loadByNameInSession(roleName, aSession);
				if (hibRole == null)
					return null;

				toReturn = toRole(hibRole);
				putIntoCache(roleName, toReturn);
				tx.commit();
			} catch (HibernateException he) {
				logException(he);

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
		return toReturn;
	}

	public SbiExtRoles loadByNameInSession(String roleName, Session aSession) {
		Criterion aCriterion = Expression.eq("name", roleName);

		Criteria aCriteria = aSession.createCriteria(SbiExtRoles.class);

		aCriteria.add(aCriterion);

		SbiExtRoles hibRole = (SbiExtRoles) aCriteria.uniqueResult();
		return hibRole;
	}

	/**
	 * Load all roles.
	 *
	 * @return the list
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadAllRoles()
	 */
	@Override
	public List loadAllRoles() throws EMFUserError {
		List realResult = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiExtRoles.class);
			finder.addOrder(Order.asc("name"));
			List hibList = finder.list();

			tx.commit();

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				Role role = toRole((SbiExtRoles) it.next());
				putIntoCache(String.valueOf(role.getId()), role);
				realResult.add(role);
			}
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return realResult;
	}

	@Override
	public List loadAllRolesFiltereByTenant() throws EMFUserError {
		List realResult = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria finder = aSession.createCriteria(SbiExtRoles.class).add(Restrictions.eq("commonInfo.organization", this.getTenant()));
			finder.addOrder(Order.asc("name"));
			List hibList = finder.list();

			tx.commit();

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				Role role = toRole((SbiExtRoles) it.next());
				putIntoCache(String.valueOf(role.getId()), role);
				realResult.add(role);
			}
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return realResult;
	}

	@Override
	public List loadRolesItem(JSONObject item) throws EMFUserError, JSONException {
		List realResult = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		List<SbiExtRoles> roles = new ArrayList<SbiExtRoles>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria c = aSession.createCriteria(SbiGeoLayersRoles.class, "glroles");
			c.add(Restrictions.eq("glroles.layer.layerId", item.getInt("layerId")));
			List hibList = c.list();
			tx.commit();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiGeoLayersRoles sbi = (SbiGeoLayersRoles) it.next();
				roles.add(sbi.getRole());
			}

			for (int i = 0; i < roles.size(); i++) {
				Role role = toRole(roles.get(i));
				putIntoCache(String.valueOf(role.getId()), role);
				realResult.add(role);
			}

			return realResult;

		} catch (HibernateException he) {
			logException(he);

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
	 * Insert role.
	 *
	 * @param aRole the a role
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#insertRole(it.eng.spagobi.commons.bo.Role)
	 */
	@Override
	public void insertRole(Role aRole) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			insertRoleWithSession(aRole, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [insertRole] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
			}
		}
	}

	public void insertRoleWithSession(Role aRole, Session aSession) {
		SbiExtRoles hibRole = new SbiExtRoles();

		hibRole.setCode(aRole.getCode());
		hibRole.setDescr(aRole.getDescription());
		hibRole.setIsPublic(aRole.getIsPublic());

		hibRole.setName(aRole.getName());

		SbiDomains roleType = (SbiDomains) aSession.load(SbiDomains.class, aRole.getRoleTypeID());
		hibRole.setRoleType(roleType);

		hibRole.setRoleTypeCode(aRole.getRoleTypeCD());
		hibRole.getCommonInfo().setOrganization(aRole.getOrganization());
		updateSbiCommonInfo4Insert(hibRole);
		aSession.save(hibRole);

		emitRoleAddedEvent(aSession, hibRole);

		LOGGER.debug("The [insertRoleWithSession] occurs. Role cache will be cleaned.");
		this.clearCache();
	}

	/**
	 * Erase role.
	 *
	 * @param aRole the a role
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#eraseRole(it.eng.spagobi.commons.bo.Role)
	 */
	@Override
	public void eraseRole(Role aRole) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, aRole.getId());
			// deletes associations with events (and events themselves, if they
			// have no more associations)
			// Query hibQuery =
			// aSession.createQuery(" from SbiEventRole ser where
			// ser.id.role.extRoleId = "
			// + hibRole.getExtRoleId().toString());
			// Query hibQuery = aSession.createQuery(" from SbiEventRole ser where ser.id.role.extRoleId = ?");
			// hibQuery.setInteger(0, hibRole.getExtRoleId().intValue());
			// List eventsRole = hibQuery.list();
			// Iterator it = eventsRole.iterator();
			// while (it.hasNext()) {
			// SbiEventRole eventRole = (SbiEventRole) it.next();
			// SbiEventsLog event = eventRole.getId().getEvent();
			// aSession.delete(eventRole);
			// aSession.flush();
			// aSession.refresh(event);
			// Set roles = event.getRoles();
			// if (roles.isEmpty()) {
			// aSession.delete(event);
			// }
			// }
			Set<SbiAuthorizationsRoles> authorizations = hibRole.getSbiAuthorizationsRoleses();
			Iterator itf = authorizations.iterator();
			while (itf.hasNext()) {
				SbiAuthorizationsRoles fr = (SbiAuthorizationsRoles) itf.next();

				aSession.delete(fr);
				aSession.flush();
				aSession.refresh(hibRole);

			}

			aSession.delete(hibRole);

			emitRoleDeletedEvent(aSession, hibRole);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [eraseRole] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
			}
		}
	}

	@Override
	public void unsetOtherPublicRole(Session aSession) {
		Criterion aCriterion = Expression.eq("isPublic", true);
		Criteria aCriteria = aSession.createCriteria(SbiExtRoles.class);
		aCriteria.add(aCriterion);
		SbiExtRoles hibRole = (SbiExtRoles) aCriteria.uniqueResult();
		if (hibRole != null) {
			hibRole.setIsPublic(false);
			aSession.update(hibRole);

			emitPublicFlagSetEvent(aSession, hibRole);
		}

	}

	/**
	 * Modify role.
	 *
	 * @param aRole the a role
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#modifyRole(it.eng.spagobi.commons.bo.Role)
	 */
	@Override
	public void modifyRole(Role aRole) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// if new role is public check there are no other public otherwise unset them
			if (aRole.getIsPublic() != null && aRole.getIsPublic() == true) {
				unsetOtherPublicRole(aSession);
			}

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, aRole.getId());

			hibRole.setCode(aRole.getCode());
			hibRole.setDescr(aRole.getDescription());
			hibRole.setName(aRole.getName());

			Set<SbiAuthorizationsRoles> authorizations = hibRole.getSbiAuthorizationsRoleses();
			Iterator it = authorizations.iterator();
			while (it.hasNext()) {
				SbiAuthorizationsRoles fr = (SbiAuthorizationsRoles) it.next();
				aSession.delete(fr);
				aSession.flush();
			}

			SbiDomains roleType = (SbiDomains) aSession.load(SbiDomains.class, aRole.getRoleTypeID());
			hibRole.setRoleType(roleType);

			if (!Objects.equals(hibRole.getIsPublic(), aRole.getIsPublic())) {
				hibRole.setIsPublic(aRole.getIsPublic());

				emitPublicFlagSetEvent(aSession, hibRole);
			}

			hibRole.setRoleTypeCode(aRole.getRoleTypeCD());
			updateSbiCommonInfo4Update(hibRole);

			aSession.update(hibRole);
			aSession.flush();

			// create new association
			// -----------------------------------------
			// 1 - get Product Types of this tenant
			String tenant = this.getTenant();
			if (tenant == null) {
				throw new SpagoBIRuntimeException("Organization not set!!!");
			}

			// Get corresponding Product Type Id for role's tenant
			Set<Integer> productTypesId = findProductTypesId(aSession, tenant);

			// 2 - Get only the authorizations of the product types of the
			// tenant
			String hqlall = "from SbiAuthorizations aut where aut.productType.productTypeId IN (:PRODUCT_TYPES)";
			Query hqlQueryAll = aSession.createQuery(hqlall);
			hqlQueryAll.setParameterList("PRODUCT_TYPES", productTypesId);
			List<SbiAuthorizations> allFunct = hqlQueryAll.list();

			Set<SbiAuthorizationsRoles> authorizzationsNew = new HashSet();

			Iterator allFunIt = allFunct.iterator();
			while (allFunIt.hasNext()) {

				SbiAuthorizations authI = (SbiAuthorizations) allFunIt.next();

				if (isAbleTo(aRole, authI)) {

					SbiAuthorizationsRoles fr = new SbiAuthorizationsRoles();
					SbiAuthorizationsRolesId id = new SbiAuthorizationsRolesId(authI.getId(), hibRole.getExtRoleId());
					id.setRoleId(hibRole.getExtRoleId());
					id.setAuthorizationId(authI.getId());

					fr.setSbiExtRoles(hibRole);
					fr.setSbiAuthorizations(authI);
					fr.setId(id);
					updateSbiCommonInfo4Update(fr);
					aSession.save(fr);
					aSession.flush();
					authorizzationsNew.add(fr);
				}

			}

			emitRoleUpdatedEvent(aSession, hibRole);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [modifyRole] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
			}
		}

	}

	private boolean isAbleTo(Role aRole, SbiAuthorizations authI) {
		return (authI.getName().equals("SAVE_SUBOBJECTS") && aRole.isAbleToSaveSubobjects())
				|| (authI.getName().equals("SEE_SUBOBJECTS") && aRole.isAbleToSeeSubobjects())
				|| (authI.getName().equals("SEE_SNAPSHOTS") && aRole.isAbleToSeeSnapshots())
				|| (authI.getName().equals("RUN_SNAPSHOTS") && aRole.isAbleToRunSnapshots())
				|| (authI.getName().equals("SEE_VIEWPOINTS") && aRole.isAbleToSeeViewpoints())
				|| (authI.getName().equals("SEE_NOTES") && aRole.isAbleToSeeNotes()) || (authI.getName().equals("SEE_METADATA") && aRole.isAbleToSeeMetadata())
				|| (authI.getName().equals("SAVE_METADATA") && aRole.isAbleToSaveMetadata())
				|| (authI.getName().equals("SEND_MAIL") && aRole.isAbleToSendMail())
				|| (authI.getName().equals("SAVE_REMEMBER_ME") && aRole.isAbleToSaveRememberMe())
				|| (authI.getName().equals("SAVE_INTO_FOLDER") && aRole.isAbleToSaveIntoPersonalFolder())
				|| (authI.getName().equals("BUILD_QBE_QUERY") && aRole.isAbleToBuildQbeQuery())
				|| (authI.getName().equals("DO_MASSIVE_EXPORT") && aRole.isAbleToDoMassiveExport())
				|| (authI.getName().equals("MANAGE_USERS") && aRole.isAbleToManageUsers())
				|| (authI.getName().equals("SEE_DOCUMENT_BROWSER") && aRole.isAbleToSeeDocumentBrowser())
				|| (authI.getName().equals("SEE_FAVOURITES") && aRole.isAbleToSeeFavourites())
				|| (authI.getName().equals("SEE_SUBSCRIPTIONS") && aRole.isAbleToSeeSubscriptions())
				|| (authI.getName().equals("SEE_MY_DATA") && aRole.isAbleToSeeMyData())
				|| (authI.getName().equals("SEE_MY_WORKSPACE") && aRole.isAbleToSeeMyWorkspace())
				|| (authI.getName().equals("SEE_TODO_LIST") && aRole.isAbleToSeeToDoList())
				|| (authI.getName().equals("KPI_COMMENT_EDIT_ALL") && aRole.isAbleToEditAllKpiComm())
				|| (authI.getName().equals("KPI_COMMENT_EDIT_MY") && aRole.isAbleToEditMyKpiComm())
				|| (authI.getName().equals("KPI_COMMENT_DELETE") && aRole.isAbleToDeleteKpiComm())
				|| (authI.getName().equals("CREATE_DOCUMENTS") && aRole.isAbleToCreateDocuments())
				|| (authI.getName().equals("CREATE_SOCIAL_ANALYSIS") && aRole.isAbleToCreateSocialAnalysis())
				|| (authI.getName().equals("VIEW_SOCIAL_ANALYSIS") && aRole.isAbleToViewSocialAnalysis())
				|| (authI.getName().equals("HIERARCHIES_MANAGEMENT") && aRole.isAbleToHierarchiesManagement())
				|| (authI.getName().equals("ENABLE_DATASET_PERSISTENCE") && aRole.isAbleToEnableDatasetPersistence())
				|| (authI.getName().equals("ENABLE_FEDERATED_DATASET") && aRole.isAbleToEnableFederatedDataset())
				|| (authI.getName().equals("ENABLE_TO_RATE") && aRole.isAbleToEnableRate())
				|| (authI.getName().equals("ENABLE_TO_PRINT") && aRole.isAbleToEnablePrint())
				|| (authI.getName().equals("ENABLE_TO_COPY_AND_EMBED") && aRole.isAbleToEnableCopyAndEmbed())
				|| (authI.getName().equals("MANAGE_GLOSSARY_BUSINESS") && aRole.isAbleToManageGlossaryBusiness())
				|| (authI.getName().equals("MANAGE_GLOSSARY_TECHNICAL") && aRole.isAbleToManageGlossaryTechnical())
				|| (authI.getName().equals("MANAGE_KPI_VALUE") && aRole.isAbleToManageKpiValue())
				|| (authI.getName().equals("MANAGE_CALENDAR") && aRole.isAbleToManageCalendar())
				|| (authI.getName().equals("FUNCTIONS_CATALOG_USAGE") && aRole.isAbleToUseFunctionsCatalog())
				|| (authI.getName().equals("MANAGE_INTERNATIONALIZATION") && aRole.isAbleToManageInternationalization())
				|| (authI.getName().equals("CREATE_SELF_SERVICE_COCKPIT") && aRole.isAbleToCreateSelfServiceCockpit())
				|| (authI.getName().equals("CREATE_SELF_SERVICE_KPI") && aRole.isAbleToCreateSelfServiceKpi())
				|| (authI.getName().equals("CREATE_SELF_SERVICE_GEOREPORT") && aRole.isAbleToCreateSelfServiceGeoreport())
				|| (authI.getName().equals("EDIT_PYTHON_SCRIPTS") && aRole.isAbleToEditPythonScripts())
				|| (authI.getName().equals("CREATE_CUSTOM_CHART") && aRole.isAbleToCreateCustomChart());
	}

	/**
	 * Load all free roles for insert.
	 *
	 * @param parameterID the parameter id
	 * @return the list
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadAllFreeRolesForInsert(java.lang.Integer)
	 */
	@Override
	public List loadAllFreeRolesForInsert(Integer parameterID) throws EMFUserError {
		List realResult = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiExtRoles ");
			List hibListAllRoles = hibQuery.list();

			/*
			 * String hql = "from SbiParuseDet s " + " where s.id.sbiParuse.sbiParameters.parId = " + parameterID;
			 */

			String hql = "from SbiParuseDet s " + " where s.id.sbiParuse.sbiParameters.parId = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, parameterID.intValue());

			List parUseDetsOfNoFreeRoles = hqlQuery.list();

			List noFreeRoles = new ArrayList();

			for (Iterator it = parUseDetsOfNoFreeRoles.iterator(); it.hasNext();) {
				noFreeRoles.add(((SbiParuseDet) it.next()).getId().getSbiExtRoles());
			}

			hibListAllRoles.removeAll(noFreeRoles);

			Iterator it = hibListAllRoles.iterator();

			while (it.hasNext()) {
				Role role = toRole((SbiExtRoles) it.next());
				putIntoCache(String.valueOf(role.getId()), role);
				realResult.add(role);
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return realResult;
	}

	/**
	 * Load all free roles for detail.
	 *
	 * @param parUseID the par use id
	 * @return the list
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadAllFreeRolesForDetail(java.lang.Integer)
	 */
	@Override
	public List loadAllFreeRolesForDetail(Integer parUseID) throws EMFUserError {
		List realResult = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiExtRoles ");
			List hibListAllRoles = hibQuery.list();

			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, parUseID);

			Set setParUsesDets = sbiParuse.getSbiParuseDets();
			for (Iterator it = setParUsesDets.iterator(); it.hasNext();) {
				SbiParuseDet det = (SbiParuseDet) it.next();
			}

			/*
			 * String hql = "from SbiParuseDet s " + " where s.id.sbiParuse.sbiParameters.parId = "+ sbiParuse.getSbiParameters().getParId() +
			 * " and s.id.sbiParuse.label != '" + sbiParuse.getLabel()+ "'";
			 */

			String hql = "from SbiParuseDet s " + " where s.id.sbiParuse.sbiParameters.parId = ? " + " and s.id.sbiParuse.label != ? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, sbiParuse.getSbiParameters().getParId().intValue());
			hqlQuery.setString(1, sbiParuse.getLabel());

			List parUseDetsOfNoFreeRoles = hqlQuery.list();

			List noFreeRoles = new ArrayList();

			for (Iterator it = parUseDetsOfNoFreeRoles.iterator(); it.hasNext();) {
				noFreeRoles.add(((SbiParuseDet) it.next()).getId().getSbiExtRoles());
			}

			hibListAllRoles.removeAll(noFreeRoles);

			Iterator it = hibListAllRoles.iterator();

			while (it.hasNext()) {
				Role role = toRole((SbiExtRoles) it.next());
				putIntoCache(String.valueOf(role.getId()), role);
				realResult.add(role);
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return realResult;
	}

	public Role toBasicRole(SbiExtRoles hibExtRole) {

		LOGGER.debug("IN");

		Role role = new Role();
		role.setId(hibExtRole.getExtRoleId());
		role.setName(hibExtRole.getName());
		role.setDescription(hibExtRole.getDescr());
		// role.setCode(hibExtRole.getCode());
		// role.setRoleTypeCD(hibExtRole.getRoleTypeCode());
		// role.setRoleTypeID(hibExtRole.getRoleType().getValueId());
		// role.setOrganization(hibExtRole.getCommonInfo().getOrganization());

		LOGGER.debug("OUT");
		return role;
	}

	/**
	 * From the hibernate Role at input, gives the corrispondent <code>Role</code> object.
	 *
	 * @param hibRole The hybernate role
	 * @return The corrispondent <code>Role</code> object
	 */
	public Role toRole(SbiExtRoles hibRole) {
		LOGGER.debug("IN.hibRole.getName()=" + hibRole.getName());
		Role role = new Role();
		role.setCode(hibRole.getCode());
		role.setDescription(hibRole.getDescr());
		role.setId(hibRole.getExtRoleId());
		role.setName(hibRole.getName());
		role.setIsPublic(hibRole.getIsPublic());

		Set<SbiAuthorizationsRoles> authorizations = hibRole.getSbiAuthorizationsRoleses();
		Iterator it = authorizations.iterator();
		while (it.hasNext()) {
			SbiAuthorizationsRoles fr = (SbiAuthorizationsRoles) it.next();
			SbiAuthorizations f = fr.getSbiAuthorizations();

			String name = f.getName();
			if (name.equals("EDIT_PYTHON_SCRIPTS")) {
				role.setIsAbleToEditPythonScripts(true);
			}
			if (name.equals("CREATE_CUSTOM_CHART")) {
				role.setIsAbleToCreateCustomChart(true);
			}
			if (name.equals("SAVE_SUBOBJECTS")) {
				role.setIsAbleToSaveSubobjects(true);
			}
			if (name.equals("SEE_SUBOBJECTS")) {
				role.setIsAbleToSeeSubobjects(true);
			}
			if (name.equals("SEE_VIEWPOINTS")) {
				role.setIsAbleToSeeViewpoints(true);
			}
			if (name.equals("SEE_SNAPSHOTS")) {
				role.setIsAbleToSeeSnapshots(true);
			}
			if (name.equals("RUN_SNAPSHOTS")) {
				role.setIsAbleToRunSnapshots(true);
			}
			if (name.equals("SEE_NOTES")) {
				role.setIsAbleToSeeNotes(true);
			}
			if (name.equals("SEND_MAIL")) {
				role.setIsAbleToSendMail(true);
			}
			if (name.equals("SAVE_INTO_FOLDER")) {
				role.setIsAbleToSaveIntoPersonalFolder(true);
			}
			if (name.equals("SAVE_REMEMBER_ME")) {
				role.setIsAbleToSaveRememberMe(true);
			}
			if (name.equals("SEE_METADATA")) {
				role.setIsAbleToSeeMetadata(true);
			}
			if (name.equals("SAVE_METADATA")) {
				role.setIsAbleToSaveMetadata(true);
			}
			if (name.equals("BUILD_QBE_QUERY")) {
				role.setIsAbleToBuildQbeQuery(true);
			}
			if (name.equals("DO_MASSIVE_EXPORT")) {
				role.setIsAbleToDoMassiveExport(true);
			}
			if (name.equals("MANAGE_USERS")) {
				role.setIsAbleToManageUsers(true);
			}
			if (name.equals("SEE_DOCUMENT_BROWSER")) {
				role.setIsAbleToSeeDocumentBrowser(true);
			}
			if (name.equals("SEE_FAVOURITES")) {
				role.setIsAbleToSeeFavourites(true);
			}
			if (name.equals("SEE_SUBSCRIPTIONS")) {
				role.setIsAbleToSeeSubscriptions(true);
			}
			if (name.equals("SEE_MY_DATA")) {
				role.setIsAbleToSeeMyData(true);
			}
			if (name.equals("SEE_MY_WORKSPACE")) {
				role.setIsAbleToSeeMyWorkspace(true);
			}
			if (name.equals("SEE_TODO_LIST")) {
				role.setIsAbleToSeeToDoList(true);
			}
			if (name.equals("CREATE_DOCUMENTS")) {
				role.setIsAbleToCreateDocuments(true);
			}
			if (name.equals("CREATE_SOCIAL_ANALYSIS")) {
				role.setIsAbleToCreateSocialAnalysis(true);
			}
			if (name.equals("VIEW_SOCIAL_ANALYSIS")) {
				role.setIsAbleToViewSocialAnalysis(true);
			}
			if (name.equals("HIERARCHIES_MANAGEMENT")) {
				role.setIsAbleToHierarchiesManagement(true);
			}
			if (name.equals("KPI_COMMENT_EDIT_ALL")) {
				role.setAbleToEditAllKpiComm(true);
			}
			if (name.equals("KPI_COMMENT_EDIT_MY")) {
				role.setAbleToEditMyKpiComm(true);
			}
			if (name.equals("KPI_COMMENT_DELETE")) {
				role.setAbleToDeleteKpiComm(true);
			}
			if (name.equals("ENABLE_DATASET_PERSISTENCE")) {
				role.setIsAbleToEnableDatasetPersistence(true);
			}
			if (name.equals("ENABLE_FEDERATED_DATASET")) {
				role.setIsAbleToEnableFederatedDataset(true);
			}
			if (name.equals("ENABLE_TO_RATE")) {
				role.setIsAbleToEnableRate(true);
			}
			if (name.equals("ENABLE_TO_PRINT")) {
				role.setIsAbleToEnablePrint(true);
			}
			if (name.equals("ENABLE_TO_COPY_AND_EMBED")) {
				role.setIsAbleToEnableCopyAndEmbed(true);
			}
			if (name.equals("MANAGE_GLOSSARY_BUSINESS")) {
				role.setAbleToManageGlossaryBusiness(true);
			}
			if (name.equals("MANAGE_GLOSSARY_TECHNICAL")) {
				role.setAbleToManageGlossaryTechnical(true);
			}
			if (name.equals("MANAGE_KPI_VALUE")) {
				role.setAbleToManageKpiValue(true);
			}
			if (name.equals("MANAGE_CALENDAR")) {
				role.setAbleToManageCalendar(true);
			}
			if (name.equals("FUNCTIONS_CATALOG_USAGE")) {
				role.setAbleToUseFunctionsCatalog(true);
			}
			if (name.equals("MANAGE_INTERNATIONALIZATION")) {
				role.setAbleToManageInternationalization(true);
			}
			if (name.equals("CREATE_SELF_SERVICE_COCKPIT")) {
				role.setAbleToCreateSelfServiceCockpit(true);
			}
			if (name.equals("CREATE_SELF_SERVICE_GEOREPORT")) {
				role.setAbleToCreateSelfServiceGeoreport(true);
			}
			if (name.equals("CREATE_SELF_SERVICE_KPI")) {
				role.setAbleToCreateSelfServiceKpi(true);
			}

		}

		role.setRoleTypeCD(hibRole.getRoleTypeCode());
		role.setRoleTypeID(hibRole.getRoleType().getValueId());
		role.setOrganization(hibRole.getCommonInfo().getOrganization());
		LOGGER.debug("OUT");
		return role;
	}

	/**
	 * Gets all the authorizations associated to the role.
	 *
	 * @param roleID The role id
	 * @return The authorizations associated to the role
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List LoadFunctionalitiesAssociated(Integer roleID) throws EMFUserError {
		List functs = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			 * String hql = "select f from SbiFunctions f, SbiFuncRole fr, SbiExtRoles r " + " where f.functId = fr.id.function.functId " +
			 * " and r.extRoleId = fr.id.role.extRoleId " +" and r.extRoleId = " + roleID;
			 */

			String hql = "select f from SbiFunctions f, SbiFuncRole fr, SbiExtRoles r " + " where f.functId = fr.id.function.functId "
					+ " and r.extRoleId = fr.id.role.extRoleId " + " and r.extRoleId = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, roleID.intValue());
			functs = hqlQuery.list();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return functs;
	}

	/**
	 * Gets all the parameter uses associated to the role.
	 *
	 * @param roleID The role id
	 * @return The parameter uses associated to the role
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List LoadParUsesAssociated(Integer roleID) throws EMFUserError {
		List uses = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			 * String hql = "select pu from SbiParuseDet pud, SbiParuse pu, SbiExtRoles r " + " where pu.useId = pud.id.sbiParuse.useId " +
			 * " and r.extRoleId = pud.id.sbiExtRoles.extRoleId " + " and r.extRoleId = " + roleID;
			 */

			String hql = "select pu from SbiParuseDet pud, SbiParuse pu, SbiExtRoles r " + " where pu.useId = pud.id.sbiParuse.useId "
					+ " and r.extRoleId = pud.id.sbiExtRoles.extRoleId " + " and r.extRoleId = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, roleID.intValue());
			uses = hqlQuery.list();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return uses;
	}

	@Override
	public Integer insertRoleComplete(Role role) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		Integer roleId = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// if new role is public check there are no other public otherwise unset them
			if (role.getIsPublic() != null && role.getIsPublic() == true) {
				unsetOtherPublicRole(aSession);
			}

			SbiExtRoles hibRole = new SbiExtRoles();

			hibRole.setCode(role.getCode());
			hibRole.setDescr(role.getDescription());
			hibRole.setName(role.getName());
			hibRole.setIsPublic(role.getIsPublic());

			SbiDomains roleType = (SbiDomains) aSession.load(SbiDomains.class, role.getRoleTypeID());
			hibRole.setRoleType(roleType);

			hibRole.setRoleTypeCode(role.getRoleTypeCD());
			HashSet<SbiAuthorizationsRoles> functs = new HashSet<SbiAuthorizationsRoles>();

			updateSbiCommonInfo4Insert(hibRole);
			roleId = (Integer) aSession.save(hibRole);
			aSession.flush();

			// abilitations
			// -----------------------------------------
			// 1 - get Product Types of this tenant
			String tenant = this.getTenant();
			if (tenant == null) {
				throw new SpagoBIRuntimeException("Organization not set!!!");
			}

			// Get corresponding Product Type Id for role's tenant
			Set<Integer> productTypesId = findProductTypesId(aSession, tenant);

			// ------------------------
			// 2 - Get only the authorizations of the product types of the
			// tenant

			String hqlall = "from SbiAuthorizations aut where aut.productType.productTypeId IN (:PRODUCT_TYPES)";
			Query hqlQueryAll = aSession.createQuery(hqlall);
			hqlQueryAll.setParameterList("PRODUCT_TYPES", productTypesId);

			List<SbiAuthorizations> allFunct = hqlQueryAll.list();

			Iterator allFunIt = allFunct.iterator();
			while (allFunIt.hasNext()) {

				SbiAuthorizations functI = (SbiAuthorizations) allFunIt.next();

				if (isAbleTo(role, functI)) {

					SbiAuthorizationsRoles fr = new SbiAuthorizationsRoles();
					SbiAuthorizationsRolesId id = new SbiAuthorizationsRolesId(functI.getId(), hibRole.getExtRoleId());
					fr.setId(id);
					updateSbiCommonInfo4Insert(fr);
					aSession.save(fr);
					functs.add(fr);
				}

			}
			aSession.flush();
			hibRole.setSbiAuthorizationsRoleses(functs);
			aSession.save(hibRole);

			emitRoleAddedEvent(aSession, hibRole);

			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("OUT");
					this.clearCache();
				}
			}
		}
		return roleId;
	}

	@Override
	public Integer countRoles() throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiExtRoles ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			LOGGER.error("Error while loading the list of SbiExtRoles", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				LOGGER.debug("OUT");
			}
		}
		return resultNumber;
	}

	@Override
	public List<Role> loadPagedRolesList(Integer offset, Integer fetchSize) throws EMFUserError {
		LOGGER.debug("IN");
		List<Role> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;

			String hql = "select count(*) from SbiExtRoles ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber.intValue()) : resultNumber.intValue();
			}

			hibernateQuery = aSession.createQuery("from SbiExtRoles order by name");

			hibernateQuery.setFirstResult(offset);
			if (fetchSize > 0)
				hibernateQuery.setMaxResults(fetchSize);

			toTransform = hibernateQuery.list();

			if (toTransform != null) {
				for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
					SbiExtRoles hibRole = (SbiExtRoles) iterator.next();
					Role role = toRole(hibRole);
					putIntoCache(String.valueOf(role.getId()), role);
					toReturn.add(role);
				}
			}

		} catch (HibernateException he) {
			LOGGER.error("Error while loading the list of SbiExtRoles", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				LOGGER.debug("OUT");
			}
		}
		return toReturn;
	}

	/**
	 * Associate a Meta Model Category to the role
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#insertRoleMetaModelCategory(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void insertRoleMetaModelCategory(Integer roleId, Integer categoryId) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);

			SbiCategory category = (SbiCategory) aSession.load(SbiCategory.class, categoryId);

			Set<SbiCategory> metaModelCategories = hibRole.getSbiMetaModelCategories();
			if (metaModelCategories == null) {
				metaModelCategories = new HashSet<>();
			}
			metaModelCategories.add(category);
			hibRole.setSbiMetaModelCategories(metaModelCategories);

			aSession.saveOrUpdate(hibRole);
			aSession.flush();

			updateSbiCommonInfo4Update(hibRole);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [insertRoleMetaModelCategory] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
				LOGGER.debug("OUT");

			}
		}

	}

	/**
	 * Remove the association between the role and the Meta Model Category
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#removeRoleMetaModelCategory(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void removeRoleMetaModelCategory(Integer roleId, Integer categoryId) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);

			SbiCategory category = (SbiCategory) aSession.load(SbiCategory.class, categoryId);

			Set<SbiCategory> metaModelCategories = hibRole.getSbiMetaModelCategories();
			if (metaModelCategories != null) {
				if (metaModelCategories.contains(category)) {
					metaModelCategories.remove(category);
					hibRole.setSbiMetaModelCategories(metaModelCategories);
				} else {
					LOGGER.debug("Category " + category.getName() + " is not associated to the role " + hibRole.getName());
				}

			}
			aSession.saveOrUpdate(hibRole);
			aSession.flush();
			updateSbiCommonInfo4Update(hibRole);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [removeRoleMetaModelCategory] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
				LOGGER.debug("OUT");

			}
		}
	}

	/**
	 * Get the Meta Model Categories associated to a role
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#getMetaModelCategoryForRole(java.lang.Integer)
	 */
	@Override
	public List<RoleMetaModelCategory> getMetaModelCategoriesForRole(Integer roleId) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List<RoleMetaModelCategory> categories = new ArrayList<RoleMetaModelCategory>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles sbiExtRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);
			Integer extRoleId = sbiExtRole.getExtRoleId();
			Set<SbiCategory> categoriesAsSet = sbiExtRole.getSbiMetaModelCategories();

			// For each category associated to the role
			for (SbiCategory currCategoryFromSet : categoriesAsSet) {
				RoleMetaModelCategory category = new RoleMetaModelCategory();
				category.setCategoryId(currCategoryFromSet.getId());
				category.setRoleId(extRoleId);
				categories.add(category);
			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return categories;
	}

	/**
	 * Get the Dataset Categories associated to a role
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#getDataSetCategoriesForRole(java.lang.Integer)
	 */
	@Override
	public List<RoleMetaModelCategory> getDataSetCategoriesForRole(String roleName) throws EMFUserError {
		Session aSession = null;

		List<RoleMetaModelCategory> categories = new ArrayList<RoleMetaModelCategory>();
		try {
			aSession = getSession();
			SbiExtRoles sbiExtRole = loadByNameInSession(roleName, aSession);
			Integer extRoleId = sbiExtRole.getExtRoleId();
			Set<SbiDomains> sbiDomains = sbiExtRole.getSbiDataSetCategories();

			// For each category associated to the role
			if (sbiDomains != null) {
				for (SbiDomains sbiDomain : sbiDomains) {
					RoleMetaModelCategory category = new RoleMetaModelCategory();
					category.setCategoryId(sbiDomain.getValueId());
					category.setRoleId(extRoleId);
					categories.add(category);
				}

			}

		} catch (HibernateException he) {
			logException(he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return categories;
	}

	/**
	 * Get the Meta Model Categories associated to a role
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#getMetaModelCategoryForRole(java.lang.Integer)
	 */
	@Override
	public List<Integer> getMetaModelCategoriesForRoles(final Collection<String> roles) throws EMFUserError {
		return executeOnTransaction(new IExecuteOnTransaction<List<Integer>>() {
			@Override
			public List<Integer> execute(Session session) throws Exception {
				Criteria c = session.createCriteria(SbiExtRoles.class);
				c.add(Restrictions.in("name", roles));
				c.createAlias("sbiMetaModelCategories", "_sbiMetaModelCategories");
				c.setProjection(Property.forName("_sbiMetaModelCategories.id"));
				return c.list();
			}
		});
	}

	/**
	 * Associate a Data Set Category to the role
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#insertRoleDataSetCategory(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void insertRoleDataSetCategory(Integer roleId, Integer categoryId) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);

			SbiDomains category = (SbiDomains) aSession.load(SbiDomains.class, categoryId);

			Set<SbiDomains> dataSetCategories = hibRole.getSbiDataSetCategories();
			if (dataSetCategories == null) {
				dataSetCategories = new HashSet<SbiDomains>();
			}
			dataSetCategories.add(category);
			hibRole.setSbiDataSetCategories(dataSetCategories);

			aSession.saveOrUpdate(hibRole);

			emitDatasetCategoryAddedEvent(aSession, hibRole);

			aSession.flush();

			updateSbiCommonInfo4Update(hibRole);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [insertRoleDataSetCategory] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
				LOGGER.debug("OUT");

			}
		}

	}

	/**
	 * Remove the association between the role and the Data Set Category
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#removeRoleDataSetCategory(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void removeRoleDataSetCategory(Integer roleId, Integer categoryId) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);

			SbiDomains category = (SbiDomains) aSession.load(SbiDomains.class, categoryId);

			Set<SbiDomains> dataSetCategories = hibRole.getSbiDataSetCategories();
			if (dataSetCategories != null) {
				if (dataSetCategories.contains(category)) {
					dataSetCategories.remove(category);

					emitDatasetCategoryRemovedEvent(aSession, hibRole);

					hibRole.setSbiDataSetCategories(dataSetCategories);
				} else {
					LOGGER.debug("Category " + category.getValueNm() + " is not associated to the role " + hibRole.getName());
				}

			}
			aSession.saveOrUpdate(hibRole);

			aSession.flush();
			updateSbiCommonInfo4Update(hibRole);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [removeRoleDataSetCategory] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
				LOGGER.debug("OUT");

			}
		}
	}

	/**
	 * Gets all the Authorizationsations present
	 *
	 * @return The authorizations
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List loadAllAuthorizations() throws EMFUserError {
		List functs = new ArrayList();
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select f from SbiAuthorizations f";

			Query hqlQuery = aSession.createQuery(hql);
			functs = hqlQuery.list();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the Authorizations for product Types
	 *
	 * @return The authorizations
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<SbiAuthorizations> loadAllAuthorizationsByProductTypes(List<Integer> productTypesIds) throws EMFUserError {
		List functs = new ArrayList();
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select f from SbiAuthorizations f where f.productType.productTypeId IN (:PRODUCT_TYPES)";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setParameterList("PRODUCT_TYPES", productTypesIds);
			functs = hqlQuery.list();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the Authorizations names for product Types
	 *
	 * @return The authorizations
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<String> loadAllAuthorizationsNamesByProductTypes(List<Integer> productTypesIds) throws EMFUserError {
		List functs = new ArrayList();
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select f.name from SbiAuthorizations f where f.productType.productTypeId IN (:PRODUCT_TYPES)";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setParameterList("PRODUCT_TYPES", productTypesIds);
			functs = hqlQuery.list();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the authorizations associated to the role.
	 *
	 * @param roleID The role id
	 * @return The authorizations associated to the role
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<SbiAuthorizations> LoadAuthorizationsAssociatedToRole(Integer roleID) throws EMFUserError {
		LOGGER.debug("IN");
		List<SbiAuthorizations> functs = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "select f from SbiAuthorizations f, SbiAuthorizationsRoles fr, SbiExtRoles r " + " where f.id = fr.sbiAuthorizations.id"
					+ " and r.extRoleId = fr.sbiExtRoles.extRoleId " + " and r.extRoleId = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, roleID.intValue());
			functs = hqlQuery.list();
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the authorizationsRoles object (relationn objects) associated to the role.
	 *
	 * @param roleID The role id
	 * @return The authorizations associated to the role
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<SbiAuthorizationsRoles> LoadAuthorizationsRolesAssociatedToRole(Integer roleID) throws EMFUserError {
		LOGGER.debug("IN");
		List<SbiAuthorizationsRoles> functs = new ArrayList<SbiAuthorizationsRoles>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "select fr from SbiAuthorizations f, SbiAuthorizationsRoles fr, SbiExtRoles r " + " where f.id = fr.sbiAuthorizations.id"
					+ " and r.extRoleId = fr.sbiExtRoles.extRoleId " + " and r.extRoleId = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, roleID.intValue());
			functs = hqlQuery.list();

			if (functs != null) {
				functs.forEach(authRole -> Hibernate.initialize(authRole.getSbiAuthorizations()));

			}

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		LOGGER.debug("OUT");
		return functs;
	}

	@Override
	public void eraseAuthorizationsRolesAssociatedToRole(Integer roleID, Session currSessionDB) throws EMFUserError {
		LOGGER.debug("IN");

		try {
			String hql = "select fr from SbiAuthorizations f, SbiAuthorizationsRoles fr, SbiExtRoles r " + " where f.id = fr.sbiAuthorizations.id"
					+ " and r.extRoleId = fr.sbiExtRoles.extRoleId " + " and r.extRoleId = ?";

			Query hqlQuery = currSessionDB.createQuery(hql);
			hqlQuery.setInteger(0, roleID.intValue());
			List<SbiAuthorizationsRoles> functs = hqlQuery.list();

			for (Iterator iterator = functs.iterator(); iterator.hasNext();) {
				SbiAuthorizationsRoles SbiAuthorizationsRoles = (SbiAuthorizationsRoles) iterator.next();
				currSessionDB.delete(SbiAuthorizationsRoles);

				SbiExtRoles sbiExtRoles = SbiAuthorizationsRoles.getSbiExtRoles();

				emitRoleUpdatedEvent(currSessionDB, sbiExtRoles);
			}

		} catch (HibernateException he) {
			logException(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			LOGGER.debug("The [eraseAuthorizationsRolesAssociatedToRole] occurs. Role cache will be cleaned.");
			this.clearCache();
		}
		LOGGER.debug("OUT");
	}

	@Override
	public SbiAuthorizations insertAuthorization(String authorizationName, String productType) throws EMFUserError {
		LOGGER.debug("IN");
		SbiAuthorizations toInsert = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiProductType sbiProductType = findProductType(aSession, productType);
			toInsert = new SbiAuthorizations();
			toInsert.setName(authorizationName);
			toInsert.setProductType(sbiProductType);
			updateSbiCommonInfo4Insert(toInsert, true);
			aSession.save(toInsert);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					LOGGER.debug("The [eraseAuthorizationsRolesAssociatedToRole] occurs. Role cache will be cleaned.");
					this.clearCache();
				}
			}
		}

		LOGGER.debug("OUT");
		return toInsert;
	}

	private SbiProductType findProductType(Session aSession, String label) {
		LOGGER.debug("IN");
		String hql = "from SbiProductType e where e.label = :label";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setParameter("label", label);
		SbiProductType productType = (SbiProductType) hqlQuery.uniqueResult();
		LOGGER.debug("OUT");
		return productType;
	}

	private Set<Integer> findProductTypesId(Session aSession, String tenant) {
		Set<Integer> productTypesId = new HashSet<Integer>();

		String hql = "from SbiOrganizationProductType opt where opt.commonInfo.organization=?";
		Query query = aSession.createQuery(hql);
		query.setParameter(0, tenant);
		List productTypes = query.list();
		Iterator iter = productTypes.iterator();
		while (iter.hasNext()) {
			SbiOrganizationProductType sbiOrganizationProductType = (SbiOrganizationProductType) iter.next();
			productTypesId.add(sbiOrganizationProductType.getSbiProductType().getProductTypeId());
		}
		return productTypesId;
	}

	private Role getFromCache(String key) {
		LOGGER.debug("IN");
		String tenantId = this.getTenant();
		Role role = null;
		if (tenantId != null) {
			// The tenant is set, so let's find it into the cache
			String cacheName = tenantId + DEFAULT_CACHE_SUFFIX;
			try {
				if (cacheManager == null) {
					cacheManager = CacheManager.create();
					LOGGER.debug("Cache for tenant " + tenantId + "does not exist yet. Nothing to get.");
					LOGGER.debug("OUT");
					return null;
				} else {
					if (!cacheManager.cacheExists(cacheName)) {
						LOGGER.debug("Cache for tenant " + tenantId + "does not exist yet. Nothing to get.");
						LOGGER.debug("OUT");
						return null;
					} else {
						Element el = cacheManager.getCache(cacheName).get(key);
						if (el != null) {
							role = (Role) el.getValue();
						}
					}
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error while getting a Role cache item with key " + key + " for tenant " + tenantId, t);
			}
		}
		LOGGER.debug("OUT");
		return role;
	}

	@Override
	public Role loadPublicRole() throws EMFUserError {
		Role toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiExtRoles extRole where extRole.isPublic=?";

			Query query = aSession.createQuery(hql);
			query.setBoolean(0, true);

			Object hibRoleO = query.uniqueResult();

			if (hibRoleO == null)
				return null;

			SbiExtRoles hibRole = (SbiExtRoles) hibRoleO;

			toReturn = toRole(hibRole);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return toReturn;
	}

	private void putIntoCache(String key, Role role) {
		LOGGER.debug("IN");
		String tenantId = this.getTenant();
		if (tenantId != null) {
			// The tenant is set, so let's find it into the cache
			String cacheName = tenantId + DEFAULT_CACHE_SUFFIX;
			try {
				if (cacheManager == null) {
					cacheManager = CacheManager.create();
				}
				if (!cacheManager.cacheExists(cacheName)) {
					LOGGER.debug("Cache for tenant " + tenantId + "does not exist. It will be create.");
					Cache cache = new Cache(cacheName, 300, true, false, 20, 20);
					cacheManager.addCache(cache);
				}
				cacheManager.getCache(cacheName).put(new Element(key, role));
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error while putting Role cache item with key " + key + " for tenant " + tenantId, t);
			}
		}
		LOGGER.debug("OUT");
	}

	private void clearCache() {
		LOGGER.debug("IN");
		String tenantId = this.getTenant();
		if (tenantId != null) {
			// The tenant is set, so let's find it into the cache
			String cacheName = tenantId + DEFAULT_CACHE_SUFFIX;
			try {
				if (cacheManager != null) {
					if (cacheManager.cacheExists(cacheName)) {
						cacheManager.getCache(cacheName).removeAll();
					}
					// else nothing to do, no cache existed for the current
					// tenant
				}
				// else nothing to do, no cache manager exists
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error during Role cache full cleaning process for tenant " + tenantId, t);
			}
		}
	}

	@Override
	public void setEventEmittingCommand(RoleEventsEmittingCommand command) {
		this.eventEmittingCommand = command;
	}

	private void emitRoleDeletedEvent(Session aSession, SbiExtRoles role) {
		eventEmittingCommand.emitRoleDeletedEvent(aSession, role);
	}

	private void emitRoleAddedEvent(Session aSession, SbiExtRoles role) {
		eventEmittingCommand.emitRoleAddedEvent(aSession, role);
	}

	private void emitDatasetCategoryRemovedEvent(Session aSession, SbiExtRoles role) {
		eventEmittingCommand.emitDatasetCategoryRemovedEvent(aSession, role);
	}

	private void emitDatasetCategoryAddedEvent(Session aSession, SbiExtRoles role) {
		eventEmittingCommand.emitDatasetCategoryAddedEvent(aSession, role);
	}

	private void emitRoleUpdatedEvent(Session aSession, SbiExtRoles role) {
		eventEmittingCommand.emitRoleUpdatedEvent(aSession, role);
	}

	private void emitPublicFlagSetEvent(Session aSession, SbiExtRoles role) {
		eventEmittingCommand.emitPublicFlagSetEvent(aSession, role);
	}

}
