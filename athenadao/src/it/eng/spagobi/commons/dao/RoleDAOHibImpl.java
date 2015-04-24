/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 22-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseDet;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRolesId;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiEventRole;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiOrganizationProductType;
import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.events.metadata.SbiEventsLog;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

/**
 * Defines the Hibernate implementations for all DAO methods, for a Role.
 *
 * @author zoppello
 */
public class RoleDAOHibImpl extends AbstractHibernateDAO implements IRoleDAO {

	private static transient Logger logger = Logger.getLogger(RoleDAOHibImpl.class);

	/**
	 * Load by id.
	 *
	 * @param roleID
	 *            the role id
	 *
	 * @return the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadByID(java.lang.Integer)
	 */
	@Override
	public Role loadByID(Integer roleID) throws EMFUserError {
		Role toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleID);

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
	 * @param roleName
	 *            the role name
	 *
	 * @return the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#loadByName(java.lang.String)
	 */
	@Override
	public Role loadByName(String roleName) throws EMFUserError {
		Role toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = loadByNameInSession(roleName, aSession);
			if (hibRole == null)
				return null;

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
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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
				realResult.add(toRole((SbiExtRoles) it.next()));
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

	/**
	 * Insert role.
	 *
	 * @param aRole
	 *            the a role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	public void insertRoleWithSession(Role aRole, Session aSession) {
		SbiExtRoles hibRole = new SbiExtRoles();

		hibRole.setCode(aRole.getCode());
		hibRole.setDescr(aRole.getDescription());

		hibRole.setName(aRole.getName());

		SbiDomains roleType = (SbiDomains) aSession.load(SbiDomains.class, aRole.getRoleTypeID());
		hibRole.setRoleType(roleType);

		hibRole.setRoleTypeCode(aRole.getRoleTypeCD());
		hibRole.getCommonInfo().setOrganization(aRole.getOrganization());
		updateSbiCommonInfo4Insert(hibRole);
		aSession.save(hibRole);
	}

	/**
	 * Erase role.
	 *
	 * @param aRole
	 *            the a role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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
			// aSession.createQuery(" from SbiEventRole ser where ser.id.role.extRoleId = "
			// + hibRole.getExtRoleId().toString());
			Query hibQuery = aSession.createQuery(" from SbiEventRole ser where ser.id.role.extRoleId = ?");
			hibQuery.setInteger(0, hibRole.getExtRoleId().intValue());
			List eventsRole = hibQuery.list();
			Iterator it = eventsRole.iterator();
			while (it.hasNext()) {
				SbiEventRole eventRole = (SbiEventRole) it.next();
				SbiEventsLog event = eventRole.getId().getEvent();
				aSession.delete(eventRole);
				aSession.flush();
				aSession.refresh(event);
				Set roles = event.getRoles();
				if (roles.isEmpty()) {
					aSession.delete(event);
				}
			}
			Set<SbiAuthorizationsRoles> authorizations = hibRole.getSbiAuthorizationsRoleses();
			Iterator itf = authorizations.iterator();
			while (itf.hasNext()) {
				SbiAuthorizationsRoles fr = (SbiAuthorizationsRoles) itf.next();

				aSession.delete(fr);
				aSession.flush();
				aSession.refresh(hibRole);

			}

			aSession.delete(hibRole);
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

	/**
	 * Modify role.
	 *
	 * @param aRole
	 *            the a role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
	 * @see it.eng.spagobi.commons.dao.IRoleDAO#modifyRole(it.eng.spagobi.commons.bo.Role)
	 */
	@Override
	public void modifyRole(Role aRole) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

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

			// 2 - Get only the authorizations of the product types of the tenant
			String hqlall = "from SbiAuthorizations aut where aut.productType.productTypeId IN (:PRODUCT_TYPES)";
			Query hqlQueryAll = aSession.createQuery(hqlall);
			hqlQueryAll.setParameterList("PRODUCT_TYPES", productTypesId);
			List<SbiAuthorizations> allFunct = hqlQueryAll.list();

			Set<SbiAuthorizationsRoles> authorizzationsNew = new HashSet();

			Iterator allFunIt = allFunct.iterator();
			while (allFunIt.hasNext()) {

				SbiAuthorizations authI = (SbiAuthorizations) allFunIt.next();

				if ((authI.getName().equals("SAVE_SUBOBJECTS") && aRole.isAbleToSaveSubobjects())
						|| (authI.getName().equals("SEE_SUBOBJECTS") && aRole.isAbleToSeeSubobjects())
						|| (authI.getName().equals("SEE_SNAPSHOTS") && aRole.isAbleToSeeSnapshots())
						|| (authI.getName().equals("SEE_VIEWPOINTS") && aRole.isAbleToSeeViewpoints())
						|| (authI.getName().equals("SEE_NOTES") && aRole.isAbleToSeeNotes())
						|| (authI.getName().equals("SEE_METADATA") && aRole.isAbleToSeeMetadata())
						|| (authI.getName().equals("SAVE_METADATA") && aRole.isAbleToSaveMetadata())
						|| (authI.getName().equals("SEND_MAIL") && aRole.isAbleToSendMail())
						|| (authI.getName().equals("SAVE_REMEMBER_ME") && aRole.isAbleToSaveRememberMe())
						|| (authI.getName().equals("SAVE_INTO_FOLDER") && aRole.isAbleToSaveIntoPersonalFolder())
						|| (authI.getName().equals("BUILD_QBE_QUERY") && aRole.isAbleToBuildQbeQuery())
						|| (authI.getName().equals("DO_MASSIVE_EXPORT") && aRole.isAbleToDoMassiveExport())
						|| (authI.getName().equals("EDIT_WORKSHEET") && aRole.isAbleToEditWorksheet())
						|| (authI.getName().equals("MANAGE_USERS") && aRole.isAbleToManageUsers())
						|| (authI.getName().equals("SEE_DOCUMENT_BROWSER") && aRole.isAbleToSeeDocumentBrowser())
						|| (authI.getName().equals("SEE_FAVOURITES") && aRole.isAbleToSeeFavourites())
						|| (authI.getName().equals("SEE_SUBSCRIPTIONS") && aRole.isAbleToSeeSubscriptions())
						|| (authI.getName().equals("SEE_MY_DATA") && aRole.isAbleToSeeMyData())
						|| (authI.getName().equals("SEE_TODO_LIST") && aRole.isAbleToSeeToDoList())
						|| (authI.getName().equals("KPI_COMMENT_EDIT_ALL") && aRole.isAbleToEditAllKpiComm())
						|| (authI.getName().equals("KPI_COMMENT_EDIT_MY") && aRole.isAbleToEditMyKpiComm())
						|| (authI.getName().equals("KPI_COMMENT_DELETE") && aRole.isAbleToDeleteKpiComm())
						|| (authI.getName().equals("CREATE_DOCUMENTS") && aRole.isAbleToCreateDocuments())
						|| (authI.getName().equals("CREATE_SOCIAL_ANALYSIS") && aRole.isAbleToCreateSocialAnalysis())
						|| (authI.getName().equals("VIEW_SOCIAL_ANALYSIS") && aRole.isAbleToViewSocialAnalysis())
						|| (authI.getName().equals("HIERARCHIES_MANAGEMENT") && aRole.isAbleToHierarchiesManagement())
						|| (authI.getName().equals("ENABLE_DATASET_PERSISTENCE") && aRole.isAbleToEnableDatasetPersistence())

				) {

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

	/**
	 * Load all free roles for insert.
	 *
	 * @param parameterID
	 *            the parameter id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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
				realResult.add(toRole((SbiExtRoles) it.next()));
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
	 * @param parUseID
	 *            the par use id
	 *
	 * @return the list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 *
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
			 * String hql = "from SbiParuseDet s " +" where s.id.sbiParuse.sbiParameters.parId = "+ sbiParuse.getSbiParameters().getParId()
			 * +" and s.id.sbiParuse.label != '" + sbiParuse.getLabel()+ "'";
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
				realResult.add(toRole((SbiExtRoles) it.next()));
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
	 * From the hibernate Role at input, gives the corrispondent <code>Role</code> object.
	 *
	 * @param hibRole
	 *            The hybernate role
	 *
	 * @return The corrispondent <code>Role</code> object
	 */
	public Role toRole(SbiExtRoles hibRole) {
		logger.debug("IN.hibRole.getName()=" + hibRole.getName());
		Role role = new Role();
		role.setCode(hibRole.getCode());
		role.setDescription(hibRole.getDescr());
		role.setId(hibRole.getExtRoleId());
		role.setName(hibRole.getName());

		Set<SbiAuthorizationsRoles> authorizations = hibRole.getSbiAuthorizationsRoleses();
		Iterator it = authorizations.iterator();
		while (it.hasNext()) {
			SbiAuthorizationsRoles fr = (SbiAuthorizationsRoles) it.next();
			SbiAuthorizations f = fr.getSbiAuthorizations();

			String name = f.getName();
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
			if (name.equals("EDIT_WORKSHEET")) {
				role.setIsAbleToEditWorksheet(true);
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
		}

		role.setRoleTypeCD(hibRole.getRoleTypeCode());
		role.setRoleTypeID(hibRole.getRoleType().getValueId());
		role.setOrganization(hibRole.getCommonInfo().getOrganization());
		logger.debug("OUT");
		return role;
	}

	/**
	 * Gets all the authorizations associated to the role.
	 *
	 * @param roleID
	 *            The role id
	 *
	 * @return The authorizations associated to the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
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
			 * String hql = "select f from SbiFunctions f, SbiFuncRole fr, SbiExtRoles r " +" where f.functId = fr.id.function.functId "
			 * +" and r.extRoleId = fr.id.role.extRoleId " +" and r.extRoleId = " + roleID;
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
	 * @param roleID
	 *            The role id
	 *
	 * @return The parameter uses associated to the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
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
			 * String hql = "select pu from SbiParuseDet pud, SbiParuse pu, SbiExtRoles r " +" where pu.useId = pud.id.sbiParuse.useId "
			 * +" and r.extRoleId = pud.id.sbiExtRoles.extRoleId " +" and r.extRoleId = " + roleID;
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

			SbiExtRoles hibRole = new SbiExtRoles();

			hibRole.setCode(role.getCode());
			hibRole.setDescr(role.getDescription());
			hibRole.setName(role.getName());
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
			// 2 - Get only the authorizations of the product types of the tenant

			String hqlall = "from SbiAuthorizations aut where aut.productType.productTypeId IN (:PRODUCT_TYPES)";
			Query hqlQueryAll = aSession.createQuery(hqlall);
			hqlQueryAll.setParameterList("PRODUCT_TYPES", productTypesId);

			List<SbiAuthorizations> allFunct = hqlQueryAll.list();

			Iterator allFunIt = allFunct.iterator();
			while (allFunIt.hasNext()) {

				SbiAuthorizations functI = (SbiAuthorizations) allFunIt.next();

				if ((functI.getName().equals("SAVE_SUBOBJECTS") && role.isAbleToSaveSubobjects())
						|| (functI.getName().equals("SEE_SUBOBJECTS") && role.isAbleToSeeSubobjects())
						|| (functI.getName().equals("SEE_SNAPSHOTS") && role.isAbleToSeeSnapshots())
						|| (functI.getName().equals("SEE_VIEWPOINTS") && role.isAbleToSeeViewpoints())
						|| (functI.getName().equals("SEE_NOTES") && role.isAbleToSeeNotes())
						|| (functI.getName().equals("SEE_METADATA") && role.isAbleToSeeMetadata())
						|| (functI.getName().equals("SAVE_METADATA") && role.isAbleToSaveMetadata())
						|| (functI.getName().equals("SEND_MAIL") && role.isAbleToSendMail())
						|| (functI.getName().equals("SAVE_REMEMBER_ME") && role.isAbleToSaveRememberMe())
						|| (functI.getName().equals("SAVE_INTO_FOLDER") && role.isAbleToSaveIntoPersonalFolder())
						|| (functI.getName().equals("BUILD_QBE_QUERY") && role.isAbleToBuildQbeQuery())
						|| (functI.getName().equals("DO_MASSIVE_EXPORT") && role.isAbleToDoMassiveExport())
						|| (functI.getName().equals("EDIT_WORKSHEET") && role.isAbleToEditWorksheet())
						|| (functI.getName().equals("MANAGE_USERS") && role.isAbleToManageUsers())
						|| (functI.getName().equals("SEE_DOCUMENT_BROWSER") && role.isAbleToSeeDocumentBrowser())
						|| (functI.getName().equals("SEE_FAVOURITES") && role.isAbleToSeeFavourites())
						|| (functI.getName().equals("SEE_SUBSCRIPTIONS") && role.isAbleToSeeSubscriptions())
						|| (functI.getName().equals("SEE_MY_DATA") && role.isAbleToSeeMyData())
						|| (functI.getName().equals("SEE_TODO_LIST") && role.isAbleToSeeToDoList())
						|| (functI.getName().equals("KPI_COMMENT_EDIT_ALL") && role.isAbleToEditAllKpiComm())
						|| (functI.getName().equals("KPI_COMMENT_EDIT_MY") && role.isAbleToEditMyKpiComm())
						|| (functI.getName().equals("KPI_COMMENT_DELETE") && role.isAbleToDeleteKpiComm())
						|| (functI.getName().equals("CREATE_DOCUMENTS") && role.isAbleToCreateDocuments())
						|| (functI.getName().equals("CREATE_SOCIAL_ANALYSIS") && role.isAbleToCreateSocialAnalysis())
						|| (functI.getName().equals("HIERARCHIES_MANAGEMENT") && role.isAbleToHierarchiesManagement())
						|| (functI.getName().equals("VIEW_SOCIAL_ANALYSIS") && role.isAbleToViewSocialAnalysis())
						|| (functI.getName().equals("ENABLE_DATASET_PERSISTENCE") && role.isAbleToEnableDatasetPersistence())) {

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
			return roleId;
		}

	}

	@Override
	public Integer countRoles() throws EMFUserError {
		logger.debug("IN");
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
			logger.error("Error while loading the list of SbiExtRoles", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}

	@Override
	public List<Role> loadPagedRolesList(Integer offset, Integer fetchSize) throws EMFUserError {
		logger.debug("IN");
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
					toReturn.add(role);
				}
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiExtRoles", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
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
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);

			SbiDomains category = (SbiDomains) aSession.load(SbiDomains.class, categoryId);

			Set<SbiDomains> metaModelCategories = hibRole.getSbiMetaModelCategories();
			if (metaModelCategories == null) {
				metaModelCategories = new HashSet<SbiDomains>();
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
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");

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
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiExtRoles hibRole = (SbiExtRoles) aSession.load(SbiExtRoles.class, roleId);

			SbiDomains category = (SbiDomains) aSession.load(SbiDomains.class, categoryId);

			Set<SbiDomains> metaModelCategories = hibRole.getSbiMetaModelCategories();
			if (metaModelCategories != null) {
				if (metaModelCategories.contains(category)) {
					metaModelCategories.remove(category);
					hibRole.setSbiMetaModelCategories(metaModelCategories);
				} else {
					logger.error("Category " + category.getValueNm() + " is not associated to the role " + hibRole.getName());
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
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");

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
			Set<SbiDomains> sbiDomains = sbiExtRole.getSbiMetaModelCategories();

			// For each category associated to the role
			for (SbiDomains sbiDomain : sbiDomains) {
				RoleMetaModelCategory category = new RoleMetaModelCategory();
				category.setCategoryId(sbiDomain.getValueId());
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
	 * Gets all the Authorizationsations present
	 *
	 *
	 * @return The authorizations
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public List loadAllAuthorizations() throws EMFUserError {
		List functs = new ArrayList();
		logger.debug("IN");
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
		logger.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the Authorizations for product Types
	 *
	 *
	 * @return The authorizations
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public List<SbiAuthorizations> loadAllAuthorizationsByProductTypes(List<Integer> productTypesIds) throws EMFUserError {
		List functs = new ArrayList();
		logger.debug("IN");
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
		logger.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the Authorizations names for product Types
	 *
	 *
	 * @return The authorizations
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public List<String> loadAllAuthorizationsNamesByProductTypes(List<Integer> productTypesIds) throws EMFUserError {
		List functs = new ArrayList();
		logger.debug("IN");
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
		logger.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the authorizations associated to the role.
	 *
	 * @param roleID
	 *            The role id
	 *
	 * @return The authorizations associated to the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public List<SbiAuthorizations> LoadAuthorizationsAssociatedToRole(Integer roleID) throws EMFUserError {
		logger.debug("IN");
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
		logger.debug("OUT");
		return functs;
	}

	/**
	 * Gets all the authorizationsRoles object (relationn objects) associated to the role.
	 *
	 * @param roleID
	 *            The role id
	 *
	 * @return The authorizations associated to the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public List<SbiAuthorizationsRoles> LoadAuthorizationsRolesAssociatedToRole(Integer roleID) throws EMFUserError {
		logger.debug("IN");
		List<SbiAuthorizationsRoles> functs = new ArrayList<SbiAuthorizationsRoles>();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "select fr from SbiAuthorizations f, SbiAuthorizationsRoles fr, SbiExtRoles r " + " where f.id = fr.SbiAuthorizations.id"
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
		logger.debug("OUT");
		return functs;
	}

	@Override
	public void eraseAuthorizationsRolesAssociatedToRole(Integer roleID, Session currSessionDB) throws EMFUserError {
		logger.debug("IN");

		try {
			String hql = "select fr from SbiAuthorizations f, SbiAuthorizationsRoles fr, SbiExtRoles r " + " where f.id = fr.sbiAuthorizations.id"
					+ " and r.extRoleId = fr.sbiExtRoles.extRoleId " + " and r.extRoleId = ?";

			Query hqlQuery = currSessionDB.createQuery(hql);
			hqlQuery.setInteger(0, roleID.intValue());
			List<SbiAuthorizationsRoles> functs = hqlQuery.list();

			for (Iterator iterator = functs.iterator(); iterator.hasNext();) {
				SbiAuthorizationsRoles SbiAuthorizationsRoles = (SbiAuthorizationsRoles) iterator.next();
				currSessionDB.delete(SbiAuthorizationsRoles);
			}

		} catch (HibernateException he) {
			logException(he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
		}
		logger.debug("OUT");
	}

	@Override
	public SbiAuthorizations insertAuthorization(String authorizationName, String productType) throws EMFUserError {
		logger.debug("IN");
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
				if (aSession.isOpen())
					aSession.close();
			}
		}

		logger.debug("OUT");
		return toInsert;
	}

	private SbiProductType findProductType(Session aSession, String label) {
		logger.debug("IN");
		String hql = "from SbiProductType e where e.label = :label";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setParameter("label", label);
		SbiProductType productType = (SbiProductType) hqlQuery.uniqueResult();
		logger.debug("OUT");
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

}
