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
package it.eng.spagobi.analiticalmodel.functionalitytree.dao;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjFunc;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.UserFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFuncRole;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFuncRoleId;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFunctions;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.RoleDAOHibImpl;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;

/**
 * Defines the Hibernate implementations for all DAO methods, for a functionality.
 */
public class LowFunctionalityDAOHibImpl extends AbstractHibernateDAO implements ILowFunctionalityDAO {
	private static transient Logger logger = Logger.getLogger(LowFunctionalityDAOHibImpl.class);

	public static final String PAGE = "PAGE";
	public static final String ROOT = "ROOT";
	public static final String DEFAULT_CACHE_SUFFIX = "_FUNCT_CACHE";

	public static CacheManager cacheManager = null;

	/* ********* start luca changes ************************************************** */

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#checkUserRootExists(java.lang.String)
	 */
	@Override
	public boolean checkUserRootExists(String userId) throws EMFUserError {
		logger.debug("IN");
		boolean exists = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion userfunctANDnullparent = Expression.and(Expression.isNull("parentFunct"), Expression.eq("functTypeCd", "USER_FUNCT"));
			Criterion filters = Expression.and(userfunctANDnullparent, Expression.like("path", "/" + userId));
			Criteria criteria = aSession.createCriteria(SbiFunctions.class);
			criteria.add(filters);
			SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
			if (hibFunct != null)
				exists = true;
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
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
		return exists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#insertUserFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo
	 * .UserFunctionality)
	 */
	@Override
	public void insertUserFunctionality(UserFunctionality userfunct) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = new SbiFunctions();

			// recover sbidomain of the user functionality
			Criterion vcdEQusfunct = Expression.eq("valueCd", "USER_FUNCT");
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(vcdEQusfunct);
			SbiDomains functTypeDomain = (SbiDomains) criteria.uniqueResult();

			hibFunct.setFunctType(functTypeDomain);
			hibFunct.setCode(userfunct.getCode());
			hibFunct.setFunctTypeCd(functTypeDomain.getValueCd());
			hibFunct.setDescr(userfunct.getDescription());
			hibFunct.setName(userfunct.getName());
			hibFunct.setPath(userfunct.getPath());

			Integer parentId = userfunct.getParentId();
			SbiFunctions hibParentFunct = null;
			if (parentId != null) {
				// if it is not the root controls if the parent functionality exists
				Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion parentCriterion = Expression.eq("functId", parentId);
				parentCriteria.add(parentCriterion);
				hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
				if (hibParentFunct == null) {
					logger.error("The parent Functionality with id = " + parentId + " does not exist.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
				}
			}
			// if it is the root the parent functionality is null
			hibFunct.setParentFunct(hibParentFunct);

			// manages prog column that determines the folders order
			if (hibParentFunct == null)
				hibFunct.setProg(new Integer(1));
			else {
				// loads sub functionalities
				// Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = " + parentId);
				Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = ?");
				hibQuery.setInteger(0, parentId.intValue());
				Integer maxProg = (Integer) hibQuery.uniqueResult();
				if (maxProg != null)
					hibFunct.setProg(new Integer(maxProg.intValue() + 1));
				else
					hibFunct.setProg(new Integer(1));
			}

			updateSbiCommonInfo4Insert(hibFunct, true);

			aSession.save(hibFunct);

			// save functionality roles

			/*
			 * TODO does it make sens to assign execution permissions on personal folder??? Set functRoleToSave = new HashSet(); criteria =
			 * aSession.createCriteria(SbiDomains.class); Criterion relstatecriterion = Expression.eq("valueCd", "REL"); criteria.add(relstatecriterion);
			 * SbiDomains relStateDomain = (SbiDomains)criteria.uniqueResult(); Criterion nameEqrolenameCri = null; Role[] roles = userfunct.getExecRoles();
			 * if(roles!=null){ for(int i=0; i<roles.length; i++) { Role role = roles[i]; if (role!=null) { logger.debug("Role Name="+role.getName());
			 * nameEqrolenameCri = Expression.eq("name", role.getName()); } else logger.debug("Role IS NULL");
			 * 
			 * criteria = aSession.createCriteria(SbiExtRoles.class); criteria.add(nameEqrolenameCri); SbiExtRoles hibRole =
			 * (SbiExtRoles)criteria.uniqueResult(); SbiFuncRoleId sbifuncroleid = new SbiFuncRoleId(); sbifuncroleid.setFunction(hibFunct);
			 * sbifuncroleid.setState(relStateDomain); sbifuncroleid.setRole(hibRole); SbiFuncRole sbifuncrole = new SbiFuncRole();
			 * sbifuncrole.setId(sbifuncroleid); sbifuncrole.setStateCd(relStateDomain.getValueCd()); aSession.save(sbifuncrole);
			 * functRoleToSave.add(sbifuncrole); } } hibFunct.setSbiFuncRoles(functRoleToSave);
			 */

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
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

	/* ********* end luca changes ************************************************** */

	/**
	 * Load low functionality by id.
	 * 
	 * @param functionalityID
	 *            the functionality id
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the low functionality
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByID(java.lang.Integer)
	 */
	@Override
	public LowFunctionality loadLowFunctionalityByID(Integer functionalityID, boolean recoverBIObjects) throws EMFUserError {
		logger.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(String.valueOf(functionalityID));
		if (funct == null) {
			logger.debug("Not found a LowFunctionality [ " + functionalityID + " ] into the cache");
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				SbiFunctions hibFunct = (SbiFunctions) aSession.get(SbiFunctions.class, functionalityID);
				if (hibFunct != null) {
					funct = toLowFunctionality(hibFunct, recoverBIObjects);
				}
				tx.commit();
			} catch (HibernateException he) {
				logger.error("HibernateException", he);
				if (tx != null)
					tx.rollback();
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null) {
					if (aSession.isOpen())
						aSession.close();
				}
			}
			putIntoCache(String.valueOf(functionalityID), funct);
		}
		logger.debug("OUT");
		return funct;
	}

	/**
	 * Load low functionality by code.
	 * 
	 * @param label
	 *            the functionality code
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the low functionality
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByLabel(java.lang.String)
	 */
	@Override
	public LowFunctionality loadLowFunctionalityByCode(String code, boolean recoverBIObjects) throws EMFUserError {
		logger.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(code);
		if (funct == null) {
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				Criterion labelCriterrion = Expression.eq("code", code);
				Criteria criteria = aSession.createCriteria(SbiFunctions.class);
				criteria.add(labelCriterrion);
				SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
				if (hibFunct != null) {
					funct = toLowFunctionality(hibFunct, recoverBIObjects);
				} else
					return null;
				tx.commit();
			} catch (HibernateException he) {
				logger.error("HibernateException", he);
				if (tx != null)
					tx.rollback();
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null) {
					if (aSession.isOpen())
						aSession.close();
				}
			}
			putIntoCache(code, funct);
		}
		logger.debug("OUT");
		return funct;
	}

	/**
	 * Load low functionality list by id List
	 * 
	 * @param functionalityIDs
	 *            the functionality id List
	 * 
	 * @return the low functionalities List
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByID(java.lang.Integer)
	 */
	@Override
	public List loadLowFunctionalityList(List functionalityIDs) throws EMFUserError {
		logger.debug("IN");
		List lowFunctList = new ArrayList();
		List filteredFunctionalityIDs = new ArrayList();
		if (functionalityIDs != null && !functionalityIDs.isEmpty()) {
			logger.debug("SIZE=" + functionalityIDs.size());
			Iterator iter = functionalityIDs.iterator();
			while (iter.hasNext()) {
				Object id = iter.next();
				Integer intId = (Integer) iter.next();
				logger.debug("Function ID=" + intId.toString());
				LowFunctionality funct = getFromCache(intId.toString());
				if (funct != null) {
					logger.debug("Function ID=" + intId.toString() + "found from cache");
					lowFunctList.add(funct);
				} else {
					logger.debug("Function ID=" + intId.toString() + "not found from cache.");
					filteredFunctionalityIDs.add(id);
				}
			}
		}
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Criteria criteria = aSession.createCriteria(SbiFunctions.class);
			// criteria.setFetchMode("sbiFuncRoles", FetchMode.JOIN);
			Criterion domainCdCriterrion = Expression.in("functId", functionalityIDs);
			criteria.add(domainCdCriterrion);
			List temp = criteria.list();
			// Query query=aSession.createQuery("from SbiFunctions f inner join f.sbiFuncRoles where s.functId in ("+functionalityIDs.get(0)+")");
			// List temp = query.list();
			if (!temp.isEmpty()) {
				Iterator it = temp.iterator();
				while (it.hasNext()) {
					SbiFunctions func = (SbiFunctions) it.next();
					LowFunctionality lowFunctionality = toLowFunctionality(func, false);
					putIntoCache(String.valueOf(lowFunctionality.getId()), lowFunctionality);
					lowFunctList.add(lowFunctionality);
					logger.debug("ADD funcionality:" + lowFunctionality.getName());
				}
			}
			tx.commit();

		} catch (HibernateException he) {
			logger.error("HibernateException", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT.Size=" + lowFunctList.size());
		return lowFunctList;
	}

	/**
	 * Load root low functionality.
	 * 
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the low functionality
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadRootLowFunctionality(boolean)
	 */
	@Override
	public LowFunctionality loadRootLowFunctionality(boolean recoverBIObjects) throws EMFUserError {
		logger.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(ROOT);
		if (funct == null) {
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				/* ********* start luca changes *************** */
				// Criterion filters = Expression.isNull("parentFunct");
				Criterion filters = Expression.and(Expression.isNull("parentFunct"), Expression.eq("functTypeCd", "LOW_FUNCT"));
				/* ************ end luca changes ************** */
				Criteria criteria = aSession.createCriteria(SbiFunctions.class);
				criteria.add(filters);
				SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
				if (hibFunct == null)
					return null;
				funct = toLowFunctionality(hibFunct, recoverBIObjects);
				tx.commit();
			} catch (HibernateException he) {
				logger.error("HibernateException", he);
				if (tx != null)
					tx.rollback();
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null) {
					if (aSession.isOpen())
						aSession.close();
				}
			}
			putIntoCache(ROOT, funct);
		}
		logger.debug("OUT");
		return funct;
	}

	/**
	 * Load low functionality by path.
	 * 
	 * @param functionalityPath
	 *            the functionality path
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the low functionality
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByPath(java.lang.String)
	 */
	@Override
	public LowFunctionality loadLowFunctionalityByPath(String functionalityPath, boolean recoverBIObjects) throws EMFUserError {
		logger.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(functionalityPath);
		if (funct == null) {
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				Criterion domainCdCriterrion = Expression.eq("path", functionalityPath);
				Criteria criteria = aSession.createCriteria(SbiFunctions.class);
				criteria.add(domainCdCriterrion);
				SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
				if (hibFunct == null)
					return null;
				funct = toLowFunctionality(hibFunct, recoverBIObjects);
				tx.commit();
			} catch (HibernateException he) {
				logger.error("HibernateException", he);
				if (tx != null)
					tx.rollback();
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null) {
					if (aSession.isOpen())
						aSession.close();
				}
			}
			putIntoCache(functionalityPath, funct);
		}
		logger.debug("OUT");
		return funct;
	}

	/**
	 * Modify low functionality.
	 * 
	 * @param aLowFunctionality
	 *            the a low functionality
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#modifyLowFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality)
	 */
	@Override
	public void modifyLowFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, aLowFunctionality.getId());
			// delete all roles functionality

			updateSbiCommonInfo4Update(hibFunct);

			Set oldRoles = hibFunct.getSbiFuncRoles();
			Iterator iterOldRoles = oldRoles.iterator();
			while (iterOldRoles.hasNext()) {
				SbiFuncRole role = (SbiFuncRole) iterOldRoles.next();
				aSession.delete(role);
			}
			// save roles functionality
			Set functRoleToSave = new HashSet();
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP));
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST));
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE));
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE));
			// set new roles into sbiFunctions
			hibFunct.setSbiFuncRoles(functRoleToSave);
			// set new data
			hibFunct.setDescr(aLowFunctionality.getDescription());
			Criterion domainCdCriterrion = Expression.eq("valueCd", aLowFunctionality.getCodType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);
			SbiDomains functTypeDomain = (SbiDomains) criteria.uniqueResult();
			if (functTypeDomain == null) {
				logger.error("The Domain with value_cd=" + aLowFunctionality.getCodType() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1037);
			}

			hibFunct.setFunctType(functTypeDomain);
			hibFunct.setFunctTypeCd(aLowFunctionality.getCodType());
			hibFunct.setName(aLowFunctionality.getName());

			Integer parentId = aLowFunctionality.getParentId();
			Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
			Criterion parentCriterion = Expression.eq("functId", parentId);
			parentCriteria.add(parentCriterion);
			SbiFunctions hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
			if (hibParentFunct == null) {
				logger.error("The parent Functionality with id = " + parentId + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1037);
			}
			hibFunct.setParentFunct(hibParentFunct);

			// manages code and path
			String previousCode = hibFunct.getCode();
			String previousPath = hibFunct.getPath();
			String newCode = aLowFunctionality.getCode();
			String newPath = aLowFunctionality.getPath();
			if (!previousCode.equals(newCode) || !previousPath.equals(newPath)) {
				// the code or the path was changed, so the path of the current folder and of its child folders
				// must be changed

				// the condition !previousPath.equals(newPath) was added for the following reason:
				// till SpagoBI 1.9.3 a folder may have a path different from parentPath + "/" + code,
				// with this condition those cases are considered and corrected.

				// changes the code and path of the current folder
				hibFunct.setCode(newCode);
				hibFunct.setPath(newPath);

				// loads sub folders and changes their path
				Criteria subFoldersCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion subFoldersCriterion = Expression.like("path", previousPath + "/", MatchMode.START);
				subFoldersCriteria.add(subFoldersCriterion);
				List hibList = subFoldersCriteria.list();
				Iterator it = hibList.iterator();
				while (it.hasNext()) {
					SbiFunctions aSbiFunctions = (SbiFunctions) it.next();
					String oldPath = aSbiFunctions.getPath();
					String unchanged = oldPath.substring(previousPath.length());
					aSbiFunctions.setPath(newPath + unchanged);
				}
			}

			// commit all changes
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [modifyLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
					this.clearCache();
				}
			}
		}
		logger.debug("OUT");
	}

	/**
	 * Saves all roles for a functionality, using session and permission information. The permission for a functionality can be DEVELOPMENT, TEST, EXECUTION AND
	 * CREATE and each permission has its own roles.
	 * 
	 * @param aSession
	 *            The current session object
	 * @param hibFunct
	 *            The functionality hibernate object
	 * @param aLowFunctionality
	 *            The Low Functionality object
	 * @param permission
	 *            The string defining the permission
	 * @return A collection object containing all roles
	 * @throws EMFUserError
	 * 
	 */
	private Set saveRolesFunctionality(Session aSession, SbiFunctions hibFunct, LowFunctionality aLowFunctionality, String permission) throws EMFUserError {
		Set functRoleToSave = new HashSet();
		Criterion domainCdCriterrion = null;
		Criteria criteria = null;
		criteria = aSession.createCriteria(SbiDomains.class);
		domainCdCriterrion = Expression.and(Expression.eq("valueCd", permission), Expression.eq("domainCd", SpagoBIConstants.PERMISSION_ON_FOLDER));
		criteria.add(domainCdCriterrion);
		SbiDomains permissionDomain = (SbiDomains) criteria.uniqueResult();
		if (permissionDomain == null) {
			logger.error("The Domain with value_cd=" + permission + " and domain_cd=" + SpagoBIConstants.PERMISSION_ON_FOLDER + " does not exist.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1039);
		}
		Role[] roles = null;
		if (permission.equalsIgnoreCase(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)) {
			roles = aLowFunctionality.getDevRoles();
		} else if (permission.equalsIgnoreCase(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
			roles = aLowFunctionality.getTestRoles();
		} else if (permission.equalsIgnoreCase(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
			roles = aLowFunctionality.getExecRoles();
		} else if (permission.equalsIgnoreCase(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
			roles = aLowFunctionality.getCreateRoles();
		}
		for (int i = 0; i < roles.length; i++) {
			Role role = roles[i];
			domainCdCriterrion = Expression.eq("name", role.getName());
			criteria = aSession.createCriteria(SbiExtRoles.class);
			criteria.add(domainCdCriterrion);
			SbiExtRoles hibRole = (SbiExtRoles) criteria.uniqueResult();
			SbiFuncRoleId sbifuncroleid = new SbiFuncRoleId();
			sbifuncroleid.setFunction(hibFunct);
			sbifuncroleid.setState(permissionDomain);
			sbifuncroleid.setRole(hibRole);
			SbiFuncRole sbifuncrole = new SbiFuncRole();
			sbifuncrole.setId(sbifuncroleid);
			sbifuncrole.setStateCd(permissionDomain.getValueCd());

			updateSbiCommonInfo4Update(sbifuncrole, true);

			aSession.save(sbifuncrole);
			functRoleToSave.add(sbifuncrole);
		}
		logger.debug("The [saveRolesFunctionality] occurs. LowFunctionality cache will be cleaned.");
		this.clearCache();
		return functRoleToSave;
	}

	/**
	 * Insert low functionality.
	 * 
	 * @param aLowFunctionality
	 *            the a low functionality
	 * @param profile
	 *            the profile
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#insertLowFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality,
	 *      it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public void insertLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = new SbiFunctions();
			hibFunct.setCode(aLowFunctionality.getCode());
			hibFunct.setDescr(aLowFunctionality.getDescription());
			Criterion domainCdCriterrion = Expression.eq("valueCd", aLowFunctionality.getCodType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);
			SbiDomains functTypeDomain = (SbiDomains) criteria.uniqueResult();
			if (functTypeDomain == null) {
				logger.error("The Domain with value_cd=" + aLowFunctionality.getCodType() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
			}
			hibFunct.setFunctType(functTypeDomain);
			hibFunct.setFunctTypeCd(aLowFunctionality.getCodType());
			hibFunct.setName(aLowFunctionality.getName());
			hibFunct.setPath(aLowFunctionality.getPath());

			Integer parentId = aLowFunctionality.getParentId();
			SbiFunctions hibParentFunct = null;
			if (parentId != null) {
				// if it is not the root controls if the parent functionality exists
				Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion parentCriterion = Expression.eq("functId", parentId);
				parentCriteria.add(parentCriterion);
				hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
				if (hibParentFunct == null) {
					logger.error("The parent Functionality with id = " + parentId + " does not exist.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
				}
			}
			// if it is the root the parent functionality is null
			hibFunct.setParentFunct(hibParentFunct);

			// manages prog column that determines the folders order
			if (hibParentFunct == null)
				hibFunct.setProg(new Integer(1));
			else {
				// loads sub functionalities
				// Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = " + parentId);
				Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = ?");
				hibQuery.setInteger(0, parentId.intValue());
				Integer maxProg = (Integer) hibQuery.uniqueResult();
				if (maxProg != null)
					hibFunct.setProg(new Integer(maxProg.intValue() + 1));
				else
					hibFunct.setProg(new Integer(1));
			}

			updateSbiCommonInfo4Insert(hibFunct);
			aSession.save(hibFunct);

			// save roles functionality
			Set functRoleToSave = new HashSet();
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP));
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST));
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE));
			functRoleToSave.addAll(saveRolesFunctionality(aSession, hibFunct, aLowFunctionality, SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE));
			// set new roles into sbiFunctions
			hibFunct.setSbiFuncRoles(functRoleToSave);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [insertLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
					this.clearCache();
				}
				logger.debug("OUT");
			}
		}

	}

	/**
	 * Erase low functionality.
	 * 
	 * @param aLowFunctionality
	 *            the a low functionality
	 * @param profile
	 *            the profile
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#eraseLowFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality,
	 *      it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public void eraseLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			if (hasChild(aLowFunctionality.getId())) {
				HashMap params = new HashMap();
				params.put(PAGE, "BIObjectsPage");
				// params.put(SpagoBIConstants.ACTOR, SpagoBIConstants.ADMIN_ACTOR);
				params.put(SpagoBIConstants.OPERATION, SpagoBIConstants.FUNCTIONALITIES_OPERATION);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1000, new Vector(), params);
			}
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, aLowFunctionality.getId());
			Set oldRoles = hibFunct.getSbiFuncRoles();
			Iterator iterOldRoles = oldRoles.iterator();
			while (iterOldRoles.hasNext()) {
				SbiFuncRole role = (SbiFuncRole) iterOldRoles.next();
				aSession.delete(role);
			}

			// update prog column in other functions
			// String hqlUpdateProg = "update SbiFunctions s set s.prog = (s.prog - 1) where s.prog > "
			// + hibFunct.getProg() + " and s.parentFunct.functId = " + hibFunct.getParentFunct().getFunctId();
			if (hibFunct.getParentFunct() != null) {
				String hqlUpdateProg = "update SbiFunctions s set s.prog = (s.prog - 1) where s.prog > ? " + " and s.parentFunct.functId = ?";
				Query query = aSession.createQuery(hqlUpdateProg);
				query.setInteger(0, hibFunct.getProg().intValue());
				query.setInteger(1, hibFunct.getParentFunct().getFunctId().intValue());
				query.executeUpdate();
			}

			aSession.delete(hibFunct);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} catch (EMFUserError emfue) {
			if (tx != null)
				tx.rollback();
			throw emfue;
		} catch (Exception e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null)
				if (aSession != null) {
					if (aSession.isOpen()) {
						aSession.close();
						logger.debug("The [eraseLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
						this.clearCache();
					}
					logger.debug("OUT");
				}
		}
	}

	/**
	 * From the Hibernate Low Functionality object at input, gives the corrispondent <code>LowFunctionality</code> object.
	 * 
	 * @param hibFunct
	 *            The Hibernate Low Functionality object
	 * @param recoverBIObjects
	 *            If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 * 
	 * @return the corrispondent output <code>LowFunctionality</code>
	 */
	public LowFunctionality toLowFunctionality(SbiFunctions hibFunct, boolean recoverBIObjects) {
		return toLowFunctionality(hibFunct, recoverBIObjects, null);
	}

	/**
	 * From the Hibernate Low Functionality object at input, gives the corrispondent <code>LowFunctionality</code> object.
	 * 
	 * @param hibFunct
	 *            The Hibernate Low Functionality object
	 * @param recoverBIObjects
	 *            If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 * 
	 * @return the corrispondent output <code>LowFunctionality</code>
	 */
	public LowFunctionality toLowFunctionality(SbiFunctions hibFunct, boolean recoverBIObjects, List<String> allowedDocTypes) {
		logger.debug("IN");
		LowFunctionality lowFunct = new LowFunctionality();
		lowFunct.setId(hibFunct.getFunctId());
		logger.debug("ID=" + hibFunct.getFunctId().toString());
		lowFunct.setCode(hibFunct.getCode());
		lowFunct.setCodType(hibFunct.getFunctTypeCd());
		lowFunct.setDescription(hibFunct.getDescr());
		lowFunct.setName(hibFunct.getName());
		logger.debug("NAME=" + hibFunct.getName());
		lowFunct.setPath(hibFunct.getPath());
		lowFunct.setProg(hibFunct.getProg());
		SbiFunctions parentFuntionality = hibFunct.getParentFunct();
		if (parentFuntionality != null)
			// if it is not the root find the id of the parent functionality
			lowFunct.setParentId(parentFuntionality.getFunctId());
		else
			// if it is the root set the parent id to null
			lowFunct.setParentId(null);

		List devRolesList = new ArrayList();
		List testRolesList = new ArrayList();
		List execRolesList = new ArrayList();
		List createRolesList = new ArrayList();

		Set roles = hibFunct.getSbiFuncRoles();
		if (roles != null) {
			logger.debug("getSbiFuncRoles() size=" + roles.size());
			Iterator iterRoles = roles.iterator();
			while (iterRoles.hasNext()) {
				SbiFuncRole hibfuncrole = (SbiFuncRole) iterRoles.next();
				SbiExtRoles hibRole = hibfuncrole.getId().getRole();
				SbiDomains hibPermission = hibfuncrole.getId().getState();
				logger.debug("hibfuncrole.getId().getRole().getName()=" + hibRole.getName());
				RoleDAOHibImpl roleDAO = new RoleDAOHibImpl();
				Role role = roleDAO.toRole(hibRole);

				String state = hibPermission.getValueCd();
				if (state.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP)) {
					devRolesList.add(role);
				} else if (state.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST)) {
					testRolesList.add(role);
				} else if (state.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE)) {
					execRolesList.add(role);
				} else if (state.equals(SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE)) {
					createRolesList.add(role);
				}
			}
		}

		Role[] execRoles = new Role[execRolesList.size()];
		Role[] devRoles = new Role[devRolesList.size()];
		Role[] testRoles = new Role[testRolesList.size()];
		Role[] createRoles = new Role[createRolesList.size()];

		for (int i = 0; i < execRolesList.size(); i++)
			execRoles[i] = (Role) execRolesList.get(i);
		for (int i = 0; i < testRolesList.size(); i++)
			testRoles[i] = (Role) testRolesList.get(i);
		for (int i = 0; i < devRolesList.size(); i++)
			devRoles[i] = (Role) devRolesList.get(i);
		for (int i = 0; i < createRolesList.size(); i++)
			createRoles[i] = (Role) createRolesList.get(i);

		lowFunct.setDevRoles(devRoles);
		lowFunct.setTestRoles(testRoles);
		lowFunct.setExecRoles(execRoles);
		lowFunct.setCreateRoles(createRoles);

		List biObjects = new ArrayList();
		if (recoverBIObjects) {

			BIObjectDAOHibImpl objDAO = null;
			try {
				objDAO = (BIObjectDAOHibImpl) DAOFactory.getBIObjectDAO();

				Set hibObjFuncs = hibFunct.getSbiObjFuncs();
				for (Iterator it = hibObjFuncs.iterator(); it.hasNext();) {
					SbiObjFunc hibObjFunc = (SbiObjFunc) it.next();
					if (checkObjType(hibObjFunc.getId().getSbiObjects(), allowedDocTypes)) {
						BIObject object = objDAO.toBIObject(hibObjFunc.getId().getSbiObjects(), null);
						biObjects.add(object);
					}
				}
			} catch (EMFUserError e) {
				logger.error("Error", e);
			}
		}
		lowFunct.setBiObjects(biObjects);
		logger.debug("OUT");

		return lowFunct;
	}

	private boolean checkObjType(SbiObjects sbiObjects, List<String> allowedDocTypes) {
		if (allowedDocTypes != null && !allowedDocTypes.isEmpty()) {
			boolean notFound = true;
		}
		return true;
	}

	/**
	 * Exist by code.
	 * 
	 * @param code
	 *            the code
	 * 
	 * @return the integer
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#existByCode(java.lang.String)
	 */
	@Override
	public Integer existByCode(String code) throws EMFUserError {
		Integer id = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion domainCdCriterrion = Expression.eq("code", code);
			Criteria criteria = aSession.createCriteria(SbiFunctions.class);
			criteria.add(domainCdCriterrion);
			SbiFunctions func = (SbiFunctions) criteria.uniqueResult();
			if (func != null) {
				id = func.getFunctId();
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return id;
	}

	/**
	 * Load all low functionalities.
	 * 
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List loadAllLowFunctionalities(boolean recoverBIObjects) throws EMFUserError {
		return loadAllLowFunctionalities(recoverBIObjects, null);
	}

	/**
	 * Load all low functionalities.
	 * 
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List loadAllLowFunctionalities(boolean recoverBIObjects, List<String> allowedDocTypes) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String username = null;
			IEngUserProfile profile = null;
			try {
				RequestContainer reqCont = RequestContainer.getRequestContainer();
				if (reqCont != null) {
					SessionContainer sessCont = reqCont.getSessionContainer();
					SessionContainer permCont = sessCont.getPermanentContainer();
					profile = (IEngUserProfile) permCont.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
					username = (String) ((UserProfile) profile).getUserId();
				}

			} catch (Exception e) {
				logger.error("Error while recovering user profile", e);
			}
			// user has admin functionality "ViewMyFolderAdmin" he can view all Low_func and all user func
			// else he can view only his personal userFunc

			// giovanniluca.ulivo@eng.it change
			if (profile == null) {
				// try to get the profile store in dao
				profile = this.getUserProfile();
				if (profile != null) {
					username = (String) ((UserProfile) profile).getUserId();
				}
			}
			// end change

			Query hibQuery = null;
			try {
				if (profile != null && profile.isAbleToExecuteAction("ViewMyFolderAdmin")) {
					hibQuery = aSession
							.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' or s.functTypeCd = 'USER_FUNCT' order by s.parentFunct.functId, s.prog");
				} else if (username == null) {
					hibQuery = aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' order by s.parentFunct.functId, s.prog");
				} else {
					// hibQuery =
					// aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' or s.path like '/"+username+"' order by s.parentFunct.functId, s.prog");
					hibQuery = aSession
							.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' or s.path like ? order by s.parentFunct.functId, s.prog");
					hibQuery.setString(0, "/" + username);
				}
			} catch (EMFInternalError e) {
				logger.error("EMFInternalError while access to DBMS", e);
			}

			/* ********* end luca changes ***************** */

			List hibList = hibQuery.list();

			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				LowFunctionality funct = toLowFunctionality((SbiFunctions) it.next(), recoverBIObjects, allowedDocTypes);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
		return realResult;
	}

	/**
	 * Load sub low functionalities.
	 * 
	 * @param initialPath
	 *            the initial path
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadSubLowFunctionalities(java.lang.String, boolean)
	 */
	@Override
	public List loadSubLowFunctionalities(String initialPath, boolean recoverBIObjects) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// loads folder corresponding to initial path
			Criterion domainCdCriterrion = Expression.eq("path", initialPath);
			Criteria criteria = aSession.createCriteria(SbiFunctions.class);
			criteria.add(domainCdCriterrion);
			SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
			if (hibFunct == null) {
				return null;
			}
			LowFunctionality funct = toLowFunctionality(hibFunct, recoverBIObjects);
			putIntoCache(String.valueOf(funct.getId()), funct);
			realResult.add(funct);

			// loads sub functionalities

			/* ********* start luca changes *************** */
			// Query hibQuery = aSession.createQuery(" from SbiFunctions s where s.path like '" + initialPath + "/%' order by s.parentFunct.functId, s.prog");
			Query hibQuery = aSession.createQuery(" from SbiFunctions s where s.path like ? order by s.parentFunct.functId, s.prog");
			// Query hibQuery = aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' and s.path like '" + initialPath +
			// "/%' order by s.parentFunct.functId, s.prog");
			/* ********* end luca changes ***************** */
			hibQuery.setString(0, initialPath + "/%");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				funct = toLowFunctionality((SbiFunctions) it.next(), recoverBIObjects);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
		return realResult;
	}

	/**
	 * Checks for child.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return true, if checks for child
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#hasChild(java.lang.String)
	 */
	@Override
	public boolean hasChild(Integer id) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// controls if there are sub folders
			Criterion parentChildCriterion = Expression.eq("parentFunct.functId", id);
			Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
			parentCriteria.add(parentChildCriterion);
			List childFunctions = parentCriteria.list();
			if (childFunctions != null && childFunctions.size() > 0)
				return true;

			// controls if there are objects inside
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, id);
			Set hibObjfunctions = hibFunct.getSbiObjFuncs();
			if (hibObjfunctions != null && hibObjfunctions.size() > 0)
				return true;
			// Criterion objectChildCriterion = Expression.eq("sbiFunction.functId", id);
			// Criteria objectCriteria = aSession.createCriteria(SbiObjFunc.class);
			// objectCriteria.add(objectChildCriterion);
			// List childObjects = objectCriteria.list();
			// if (childObjects != null && childObjects.size() > 0) return true;

			tx.commit();

			return false;
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
	 * Deletes a set of inconsistent roles reference from the database, in order to keep functionalities tree permissions consistence.
	 * 
	 * @param rolesSet
	 *            the set containing the roles to erase
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public void deleteInconsistentRoles(Set rolesSet) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		String hql = null;
		Query hqlQuery = null;
		List functions = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Iterator i = rolesSet.iterator();
			while (i.hasNext()) {
				ArrayList rolesArray = (ArrayList) i.next();
				Integer functId = (Integer) rolesArray.get(0);
				Integer roleId = (Integer) rolesArray.get(1);
				String permission = (String) rolesArray.get(2);
				SbiFunctions sbiFunct = new SbiFunctions();
				sbiFunct.setFunctId(functId);

				// hql = " from SbiFuncRole as funcRole where funcRole.id.function = '" + sbiFunct.getFunctId() +
				// "' AND  funcRole.id.role = '"+ roleId +"' AND funcRole.stateCd ='"+stateCD+"'";
				hql = " from SbiFuncRole as funcRole where funcRole.id.function = ? " + " AND  funcRole.id.role = ?  AND funcRole.stateCd = ?";

				hqlQuery = aSession.createQuery(hql);
				hqlQuery.setInteger(0, sbiFunct.getFunctId().intValue());
				hqlQuery.setInteger(1, roleId.intValue());
				hqlQuery.setString(2, permission);
				functions = hqlQuery.list();

				Iterator it = functions.iterator();
				while (it.hasNext()) {
					aSession.delete(it.next());
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [deleteInconsistentRoles] occurs. LowFunctionality cache will be cleaned.");
					this.clearCache();
				}
				logger.debug("OUT");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadChildFunctionalities(java.lang.Integer, boolean)
	 */
	@Override
	public List loadChildFunctionalities(Integer parentId, boolean recoverBIObjects) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// loads sub functionalities
			Query hibQuery = aSession.createQuery(" from SbiFunctions s where s.parentFunct.functId = ?");
			hibQuery.setInteger(0, parentId.intValue());
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				LowFunctionality funct = toLowFunctionality((SbiFunctions) it.next(), recoverBIObjects);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#moveDownLowFunctionality(java.lang.Integer)
	 */
	@Override
	public void moveDownLowFunctionality(Integer functionalityID) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, functionalityID);

			Integer oldProg = hibFunct.getProg();
			Integer newProg = new Integer(oldProg.intValue() + 1);

			// String upperFolderHql = "from SbiFunctions s where s.prog = " + newProg.toString() +
			// " and s.parentFunct.functId = " + hibFunct.getParentFunct().getFunctId().toString();
			String upperFolderHql = "from SbiFunctions s where s.prog = ? " + " and s.parentFunct.functId = ?";
			Query query = aSession.createQuery(upperFolderHql);
			query.setInteger(0, newProg.intValue());
			query.setInteger(1, hibFunct.getParentFunct().getFunctId().intValue());
			SbiFunctions hibUpperFunct = (SbiFunctions) query.uniqueResult();
			if (hibUpperFunct == null) {
				logger.error("The function with prog [" + newProg + "] does not exist.");
				return;
			}

			hibFunct.setProg(newProg);
			hibUpperFunct.setProg(oldProg);

			updateSbiCommonInfo4Update(hibFunct);
			updateSbiCommonInfo4Update(hibUpperFunct);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [moveDownLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
					this.clearCache();
				}
				logger.debug("OUT");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#moveUpLowFunctionality(java.lang.Integer)
	 */
	@Override
	public void moveUpLowFunctionality(Integer functionalityID) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, functionalityID);
			Integer oldProg = hibFunct.getProg();
			Integer newProg = new Integer(oldProg.intValue() - 1);

			// String upperFolderHql = "from SbiFunctions s where s.prog = " + newProg.toString() +
			// " and s.parentFunct.functId = " + hibFunct.getParentFunct().getFunctId().toString();
			String upperFolderHql = "from SbiFunctions s where s.prog = ? " + " and s.parentFunct.functId = ? ";
			Query query = aSession.createQuery(upperFolderHql);
			query.setInteger(0, newProg.intValue());
			query.setInteger(1, hibFunct.getParentFunct().getFunctId().intValue());
			SbiFunctions hibUpperFunct = (SbiFunctions) query.uniqueResult();
			if (hibUpperFunct == null) {
				logger.error("The function with prog [" + newProg + "] does not exist.");
				return;
			}

			hibFunct.setProg(newProg);
			hibUpperFunct.setProg(oldProg);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [moveUpLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
					this.clearCache();
				}
				logger.debug("OUT");
			}
		}
	}

	@Override
	public List loadAllUserFunct() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List sbifunct = new ArrayList();
		List userfunct = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query query = aSession.createQuery(" from SbiFunctions where functTypeCd  = 'USER_FUNCT'");
			sbifunct = query.list();

			for (Iterator iterator = sbifunct.iterator(); iterator.hasNext();) {
				LowFunctionality funct = toLowFunctionality((SbiFunctions) iterator.next(), false);
				putIntoCache(String.valueOf(funct.getId()), funct);
				userfunct.add(funct);
			}

		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return userfunct;
	}

	/**
	 * Load all functionalities associated the user roles.
	 * 
	 * @param onlyFirstLevel
	 *            limits functionalities to first level
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List loadUserFunctionalities(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			String username = null;
			Collection roles = null;
			try {
				// RequestContainer reqCont = RequestContainer.getRequestContainer();
				if (profile != null) {
					username = (String) ((UserProfile) profile).getUserId();
					roles = ((UserProfile) profile).getRolesForUse();

				}
			} catch (Exception e) {
				logger.error("Error while recovering user profile", e);
			}
			boolean onlyFirstLevel = (parentId == null) ? true : false;

			Query hibQuery = null;

			// getting correct root parent id (if the function must return only functionality of first level)
			Integer tmpParentId = null;
			List lstParentId = null;
			if (onlyFirstLevel) {
				hibQuery = aSession.createQuery(" from SbiFunctions s where s.parentFunct.functId is null and s.functTypeCd  = 'LOW_FUNCT'");
				// tmpParentId = (Integer)hibQuery.uniqueResult();
				lstParentId = hibQuery.list();
				tmpParentId = (lstParentId == null || lstParentId.size() == 0) ? new Integer("-1") : ((SbiFunctions) lstParentId.get(0)).getFunctId();
			} else
				tmpParentId = parentId;

			// getting functionalities
			if (username == null || roles == null) {
				hibQuery = aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' order by s.parentFunct.functId, s.prog");
			} else if (onlyFirstLevel) {
				String queryStr;
				if (UserUtilities.isAdministrator(profile)) {
					queryStr = " from SbiFunctions s where ((s.functTypeCd = 'LOW_FUNCT' and s.parentFunct.functId = ?) or s.functTypeCd = 'USER_FUNCT' or s.functTypeCd = 'COMMUNITY_FUNCT' )  order by s.parentFunct.functId, s.prog";
					hibQuery = aSession.createQuery(queryStr);
					hibQuery.setInteger(0, tmpParentId.intValue());
				} else {
					queryStr = " from SbiFunctions s where ((s.functTypeCd = 'LOW_FUNCT' and s.parentFunct.functId = ?) or s.path like ? )  order by s.parentFunct.functId, s.prog";
					hibQuery = aSession.createQuery(queryStr);
					hibQuery.setInteger(0, tmpParentId.intValue());
					hibQuery.setString(1, "/" + username);
				}

			} else {
				String queryStr;
				if (UserUtilities.isAdministrator(profile)) {
					queryStr = " from SbiFunctions s where s.parentFunct.functId = ?  order by s.parentFunct.functId, s.prog";
				} else {
					queryStr = " from SbiFunctions s where (s.functTypeCd = 'LOW_FUNCT' and s.parentFunct.functId = ?  )  order by s.parentFunct.functId, s.prog";
				}

				hibQuery = aSession.createQuery(queryStr);
				hibQuery.setInteger(0, tmpParentId.intValue());
			}
			List hibList = hibQuery.list();

			// getting correct ext_role_id
			String hql = " from SbiExtRoles as extRole where extRole.name in (:roles)  ";
			hibQuery = aSession.createQuery(hql);

			int originalSize = roles.size();
			int MAX_PARAMIN_SIZE = 1000;
			List rolesIds = null;
			if (originalSize >= MAX_PARAMIN_SIZE) {
				int start = 0;
				List tmpRoles = new ArrayList(roles);
				do {
					List subList = tmpRoles.subList(start, Math.min(start + MAX_PARAMIN_SIZE, originalSize));

					hibQuery.setParameterList("roles", subList);
					List rolesIdsTmp = hibQuery.list();

					if (rolesIds == null) {
						rolesIds = rolesIdsTmp;
					} else {
						rolesIds.addAll(rolesIdsTmp);
					}

					start += MAX_PARAMIN_SIZE;
				} while (start < originalSize);
			} else {
				hibQuery.setParameterList("roles", roles);
				rolesIds = hibQuery.list();
			}

			Iterator it = hibList.iterator();
			// maintains functionalities that have the same user's role
			while (it.hasNext()) {
				SbiFunctions tmpFunc = (SbiFunctions) it.next();
				if ((UserUtilities.isAdministrator(profile) && (tmpFunc.getFunctTypeCd().equalsIgnoreCase("USER_FUNCT") || tmpFunc.getFunctTypeCd()
						.equalsIgnoreCase("LOW_FUNCT"))) || tmpFunc.getFunctTypeCd().equalsIgnoreCase("COMMUNITY_FUNCT")) {
					LowFunctionality funct = toLowFunctionality(tmpFunc, recoverBIObjects);
					putIntoCache(String.valueOf(funct.getId()), funct);
					realResult.add(funct);
				} else {
					Object[] tmpRole = tmpFunc.getSbiFuncRoles().toArray();
					for (int j = 0; j < rolesIds.size(); j++) {
						Integer principalRole = ((SbiExtRoles) rolesIds.get(j)).getExtRoleId();
						for (int i = 0; i < tmpRole.length; i++) {
							SbiFuncRole role = (SbiFuncRole) tmpRole[i];
							Integer localRoleId = role.getId().getRole().getExtRoleId();
							if (localRoleId != null && localRoleId.compareTo(principalRole) == 0) {
								if (!existFunction(realResult, tmpFunc)) {
									LowFunctionality funct = toLowFunctionality(tmpFunc, recoverBIObjects);
									putIntoCache(String.valueOf(funct.getId()), funct);
									realResult.add(funct);
									break;
								}
							}
						}
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
		return realResult;
	}

	/**
	 * Load all functionalities associated the user roles.
	 * 
	 * @param onlyFirstLevel
	 *            limits functionalities to first level
	 * @param recoverBIObjects
	 *            the recover bi objects
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List loadUserFunctionalitiesFiltered(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile, String permission) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String username = (String) ((UserProfile) profile).getUserId();
			Collection roles;
			try {
				roles = ((UserProfile) profile).getRolesForUse();
			} catch (EMFInternalError e) {
				throw new SpagoBIRuntimeException("Error while retrieving user roles", e);
			}
			boolean isFirstLevel = (parentId == null) ? true : false;

			Query hibQuery = null;

			// getting correct root parent id (if the function must return only functionality of first level)
			Integer tmpParentId = null;
			List lstParentId = null;
			if (isFirstLevel) {
				hibQuery = aSession.createQuery(" from SbiFunctions s where s.parentFunct.functId is null and s.functTypeCd  = 'LOW_FUNCT'");
				lstParentId = hibQuery.list();
				tmpParentId = (lstParentId == null || lstParentId.size() == 0) ? new Integer("-1") : ((SbiFunctions) lstParentId.get(0)).getFunctId();
			} else {
				tmpParentId = parentId;
			}

			// getting functionalities
			if (isFirstLevel) {
				hibQuery = aSession.createQuery("select distinct sfr.id.function from SbiFuncRole sfr where "
						+ "sfr.id.function.functTypeCd = 'LOW_FUNCT' and sfr.id.function.parentFunct.functId = ?  "
						+ "and sfr.stateCd = ? and sfr.id.role.name in (:roles) ");
				// CANNOT order by in SQL query: see https://spagobi.eng.it/jira/browse/SPAGOBI-942
				// + "order by sfr.id.function.parentFunct.functId, sfr.id.function.prog");
				hibQuery.setInteger(0, tmpParentId.intValue());
				hibQuery.setString(1, permission);
				hibQuery.setParameterList("roles", roles);
				// only for administrator are getted personal folders (since SpagoBI 5)
				if (UserUtilities.isAdministrator(profile)) {
					Query hibQueryPersonalFolder = aSession.createQuery("select f from SbiFunctions f where f.path like ? ");
					hibQueryPersonalFolder.setString(0, "/" + username);
					List hibListPersF = hibQueryPersonalFolder.list();
					Iterator it = hibListPersF.iterator();
					while (it.hasNext()) {
						SbiFunctions tmpFunc = (SbiFunctions) it.next();
						LowFunctionality funct = toLowFunctionality(tmpFunc, recoverBIObjects);
						putIntoCache(String.valueOf(funct.getId()), funct);
						realResult.add(funct);
					}
				}
			} else {
				hibQuery = aSession.createQuery("select distinct sfr.id.function from SbiFuncRole sfr where "
						+ "sfr.id.function.functTypeCd = 'LOW_FUNCT' and sfr.id.function.parentFunct.functId = ? "
						+ "and sfr.stateCd = ? and sfr.id.role.name in (:roles) ");
				// CANNOT order by in SQL query: see https://spagobi.eng.it/jira/browse/SPAGOBI-942
				// + "order by sfr.id.function.parentFunct.functId, sfr.id.function.prog");
				hibQuery.setInteger(0, tmpParentId.intValue());
				hibQuery.setString(1, permission);
				hibQuery.setParameterList("roles", roles);
			}
			List<SbiFunctions> hibList = hibQuery.list();

			// MUST order using a comparator, see https://spagobi.eng.it/jira/browse/SPAGOBI-942
			Collections.sort(hibList, new Comparator<SbiFunctions>() {

				@Override
				public int compare(SbiFunctions funct1, SbiFunctions funct2) {
					SbiFunctions parent1 = funct1.getParentFunct();
					SbiFunctions parent2 = funct2.getParentFunct();

					if (parent1 == null) {
						return 1;
					}
					if (parent2 == null) {
						return -1;
					}
					Integer parentId1 = parent1.getFunctId();
					Integer parentId2 = parent2.getFunctId();

					if (parentId1 > parentId2)
						return 1;
					else if (parentId1 < parentId2)
						return -1;
					else {
						Integer progId1 = funct1.getProg();
						Integer progId2 = funct2.getProg();

						if (progId1 > progId2)
							return 1;
						else if (progId1 < progId2)
							return -1;
						else
							return 0;

					}
				}
			});

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiFunctions tmpFunc = (SbiFunctions) it.next();

				LowFunctionality funct = toLowFunctionality(tmpFunc, recoverBIObjects);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
		return realResult;
	}

	private boolean existFunction(List lstFunctions, SbiFunctions newFunct) {
		boolean res = false;
		for (int i = 0; i < lstFunctions.size(); i++) {
			LowFunctionality tmpFunct = (LowFunctionality) lstFunctions.get(i);
			if (tmpFunct.getCode().equalsIgnoreCase(newFunct.getCode())) {
				res = true;
				break;
			}
		}
		return res;
	}

	/**
	 * Load all fathers functionalities.
	 * 
	 * @param functId
	 *            the identifier of functionality child
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(Integer)
	 */
	@Override
	public List loadParentFunctionalities(Integer functId, Integer rootFolderID) throws EMFUserError {
		logger.debug("IN");

		LowFunctionality funct = null;
		Integer tmpFunctId = null;
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			tmpFunctId = functId;

			if (rootFolderID != null) {
				while (tmpFunctId.intValue() != rootFolderID.intValue()) {
					SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, tmpFunctId);
					tmpFunctId = (hibFunct.getParentFunct() != null) ? hibFunct.getParentFunct().getFunctId() : null;
					funct = toLowFunctionality(hibFunct, false);
					putIntoCache(String.valueOf(funct.getId()), funct);
					realResult.add(funct);
				}

				SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, rootFolderID);
				funct = toLowFunctionality(hibFunct, false);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			} else {
				while (tmpFunctId != null) {
					SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, tmpFunctId);
					tmpFunctId = (hibFunct.getParentFunct() != null) ? hibFunct.getParentFunct().getFunctId() : null;
					funct = toLowFunctionality(hibFunct, false);
					putIntoCache(String.valueOf(funct.getId()), funct);
					realResult.add(funct);
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

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
		return realResult;
	}

	@Override
	public Integer insertCommunityFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer result = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = new SbiFunctions();

			// recover sbidomain of the user functionality
			Criterion vcdEQusfunct = Expression.eq("valueCd", "COMMUNITY_FUNCT");
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(vcdEQusfunct);
			SbiDomains functTypeDomain = (SbiDomains) criteria.uniqueResult();

			hibFunct.setFunctType(functTypeDomain);
			hibFunct.setCode(aLowFunctionality.getCode());
			hibFunct.setFunctTypeCd(functTypeDomain.getValueCd());
			hibFunct.setDescr(aLowFunctionality.getDescription());
			hibFunct.setName(aLowFunctionality.getName());
			hibFunct.setPath(aLowFunctionality.getPath());

			Integer parentId = aLowFunctionality.getParentId();
			SbiFunctions hibParentFunct = null;
			if (parentId != null) {
				// if it is not the root controls if the parent functionality exists
				Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion parentCriterion = Expression.eq("functId", parentId);
				parentCriteria.add(parentCriterion);
				hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
				if (hibParentFunct == null) {
					logger.error("The parent Functionality with id = " + parentId + " does not exist.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
				}
			}
			// if it is the root the parent functionality is null
			hibFunct.setParentFunct(hibParentFunct);

			// manages prog column that determines the folders order
			if (hibParentFunct == null)
				hibFunct.setProg(new Integer(1));
			else {
				// loads sub functionalities

				Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = ? and s.functTypeCd = ?");
				hibQuery.setInteger(0, parentId.intValue());
				hibQuery.setString(1, "COMMUNITY_FUNCT");
				Integer maxProg = (Integer) hibQuery.uniqueResult();
				if (maxProg != null)
					hibFunct.setProg(new Integer(maxProg.intValue() + 1));
				else
					hibFunct.setProg(new Integer(1));
			}

			updateSbiCommonInfo4Insert(hibFunct, true);

			result = (Integer) aSession.save(hibFunct);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [insertCommunityFunctionality] occurs. LowFunctionality cache will be cleaned.");
					this.clearCache();
				}
				logger.debug("OUT");
			}
		}
		return result;
	}

	private LowFunctionality getFromCache(String key) {
		logger.debug("IN");
		String tenantId = this.getTenant();
		LowFunctionality funct = null;
		if (tenantId != null) {
			// The tenant is set, so let's find it into the cache
			String cacheName = tenantId + DEFAULT_CACHE_SUFFIX;
			try {
				if (cacheManager == null) {
					cacheManager = CacheManager.create();
					logger.debug("Cache for tenant " + tenantId + "does not exist yet. Nothing to get.");
					logger.debug("OUT");
					return null;
				} else {
					if (!cacheManager.cacheExists(cacheName)) {
						logger.debug("Cache for tenant " + tenantId + "does not exist yet. Nothing to get.");
						logger.debug("OUT");
						return null;
					} else {
						Element el = cacheManager.getCache(cacheName).get(key);
						if (el != null) {
							funct = (LowFunctionality) el.getValue();
						}
					}
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error while getting a LowFunctionality cache item with key " + key + " for tenant " + tenantId, t);
			}
		}
		logger.debug("OUT");
		return funct;
	}

	private void putIntoCache(String key, LowFunctionality funct) {
		logger.debug("IN");
		String tenantId = this.getTenant();
		if (tenantId != null) {
			// The tenant is set, so let's find it into the cache
			String cacheName = tenantId + DEFAULT_CACHE_SUFFIX;
			try {
				if (cacheManager == null) {
					cacheManager = CacheManager.create();
				}
				if (!cacheManager.cacheExists(cacheName)) {
					logger.debug("Cache for tenant " + tenantId + "does not exist. It will be create.");
					Cache cache = new Cache(cacheName, 300, true, false, 20, 20);
					cacheManager.addCache(cache);
				}
				cacheManager.getCache(cacheName).put(new Element(key, funct));
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error while putting LowFunctionality cache item with key " + key + " for tenant " + tenantId, t);
			}
		}
		logger.debug("OUT");
	}

	private void clearCache() {
		logger.debug("IN");
		String tenantId = this.getTenant();
		if (tenantId != null) {
			// The tenant is set, so let's find it into the cache
			String cacheName = tenantId + DEFAULT_CACHE_SUFFIX;
			try {
				if (cacheManager != null) {
					if (cacheManager.cacheExists(cacheName)) {
						cacheManager.getCache(cacheName).removeAll();
					}
					// else nothing to do, no cache existed for the current tenant
				}
				// else nothing to do, no cache manager exists
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error during LowFunctionality cache full cleaning process for tenant " + tenantId, t);
			}
		}
	}
}
