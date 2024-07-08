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

import static it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.VIEW_MY_FOLDER_ADMIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

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
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Defines the Hibernate implementations for all DAO methods, for a functionality.
 */
public class LowFunctionalityDAOHibImpl extends AbstractHibernateDAO implements ILowFunctionalityDAO {

	private static final Logger LOGGER = Logger.getLogger(LowFunctionalityDAOHibImpl.class);

	private static final String FUNCT_TYPE_COMMUNITY = "COMMUNITY_FUNCT";
	private static final String FUNCT_TYPE_LOW = "LOW_FUNCT";
	private static final String FUNCT_TYPE_USER = "USER_FUNCT";
	private static final int MAX_PARAMIN_SIZE = 1000;

	public static final String PAGE = "PAGE";
	public static final String ROOT = "ROOT";
	public static final String DEFAULT_CACHE_SUFFIX = "_FUNCT_CACHE";

	public static CacheManager cacheManager = null;

	/*
	 * ********* start luca changes **************************************************
	 */

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO #checkUserRootExists(java.lang.String)
	 */
	@Override
	public boolean checkUserRootExists(String userId) throws EMFUserError {
		LOGGER.debug("IN");
		boolean exists = false;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion userfunctANDnullparent = Restrictions.and(Restrictions.isNull("parentFunct"), Restrictions.eq("functTypeCd", FUNCT_TYPE_USER));
			Criterion filters = Restrictions.and(userfunctANDnullparent, Restrictions.like("path", "/" + userId));
			Criteria criteria = aSession.createCriteria(SbiFunctions.class);
			criteria.add(filters);
			SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
			if (hibFunct != null)
				exists = true;
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return exists;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO #
	 * insertUserFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree. bo .UserFunctionality)
	 */
	@Override
	public void insertUserFunctionality(UserFunctionality userfunct) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = new SbiFunctions();

			// recover sbidomain of the user functionality
			Criterion vcdEQusfunct = Restrictions.eq("valueCd", FUNCT_TYPE_USER);
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
				// if it is not the root controls if the parent functionality
				// exists
				Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion parentCriterion = Restrictions.eq("functId", parentId);
				parentCriteria.add(parentCriterion);
				hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
				if (hibParentFunct == null) {
					LOGGER.error("The parent Functionality with id = " + parentId + " does not exist.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
				}
			}
			// if it is the root the parent functionality is null
			hibFunct.setParentFunct(hibParentFunct);

			// manages prog column that determines the folders order
			if (hibParentFunct == null)
				hibFunct.setProg(1);
			else {
				// loads sub functionalities
				// Query hibQuery =
				// aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = "
				// + parentId);
				Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = ?");
				hibQuery.setInteger(0, parentId.intValue());
				Integer maxProg = (Integer) hibQuery.uniqueResult();
				if (maxProg != null)
					hibFunct.setProg(maxProg.intValue() + 1);
				else
					hibFunct.setProg(1);
			}

			updateSbiCommonInfo4Insert(hibFunct, true);

			aSession.save(hibFunct);

			// save functionality roles

			/*
			 * TODO does it make sens to assign execution permissions on personal folder??? Set functRoleToSave = new HashSet(); criteria =
			 * aSession.createCriteria(SbiDomains.class); Criterion relstatecriterion = Restrictions.eq("valueCd", "REL"); criteria.add(relstatecriterion);
			 * SbiDomains relStateDomain = (SbiDomains)criteria.uniqueResult(); Criterion nameEqrolenameCri = null; Role[] roles = userfunct.getExecRoles();
			 * if(roles!=null){ for(int i=0; i<roles.length; i++) { Role role = roles[i]; if (role!=null) { logger.debug("Role Name="+role.getName());
			 * nameEqrolenameCri = Restrictions.eq("name", role.getName()); } else logger.debug("Role IS NULL");
			 *
			 * criteria = aSession.createCriteria(SbiExtRoles.class); criteria.add(nameEqrolenameCri); SbiExtRoles hibRole =
			 * (SbiExtRoles)criteria.uniqueResult(); SbiFuncRoleId sbifuncroleid = new SbiFuncRoleId(); sbifuncroleid.setFunction(hibFunct);
			 * sbifuncroleid.setState(relStateDomain); sbifuncroleid.setRole(hibRole); SbiFuncRole sbifuncrole = new SbiFuncRole();
			 * sbifuncrole.setId(sbifuncroleid); sbifuncrole.setStateCd(relStateDomain.getValueCd()); aSession.save(sbifuncrole);
			 * functRoleToSave.add(sbifuncrole); } } hibFunct.setSbiFuncRoles(functRoleToSave);
			 */

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}

	}

	/*
	 * ********* end luca changes **************************************************
	 */

	/**
	 * Load low functionality by id.
	 *
	 * @param functionalityID  the functionality id
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the low functionality
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByID(java.lang.Integer)
	 */
	@Override
	public LowFunctionality loadLowFunctionalityByID(Integer functionalityID, boolean recoverBIObjects) throws EMFUserError {
		LOGGER.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(String.valueOf(functionalityID));
		if (funct == null) {
			LOGGER.debug("Not found a LowFunctionality [ " + functionalityID + " ] into the cache");
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
				rollbackIfActive(tx);
				LOGGER.error("HibernateException", he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				closeSessionIfOpen(aSession);
			}
			putIntoCache(String.valueOf(functionalityID), funct);
		}
		LOGGER.debug("OUT");
		return funct;
	}

	/**
	 * Load low functionality by code.
	 *
	 * @param label            the functionality code
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the low functionality
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByLabel(java.lang.String)
	 */
	@Override
	public LowFunctionality loadLowFunctionalityByCode(String code, boolean recoverBIObjects) throws EMFUserError {
		LOGGER.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(code);
		if (funct == null) {
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				Criterion labelCriterrion = Restrictions.eq("code", code);
				Criteria criteria = aSession.createCriteria(SbiFunctions.class);
				criteria.add(labelCriterrion);
				SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
				if (hibFunct != null) {
					funct = toLowFunctionality(hibFunct, recoverBIObjects);
				} else
					return null;
				tx.commit();
			} catch (HibernateException he) {
				rollbackIfActive(tx);
				LOGGER.error("HibernateException", he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				closeSessionIfOpen(aSession);
			}
			putIntoCache(code, funct);
		}
		LOGGER.debug("OUT");
		return funct;
	}

	/**
	 * Load low functionality list by id List
	 *
	 * @param functionalityIDs the functionality id List
	 *
	 * @return the low functionalities List
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByID(java.lang.Integer)
	 */
	@Override
	public List<LowFunctionality> loadLowFunctionalityList(List<Integer> functionalityIDs) throws EMFUserError {
		LOGGER.debug("IN");
		List<LowFunctionality> lowFunctList = new ArrayList<>();
		List filteredFunctionalityIDs = new ArrayList();
		if (functionalityIDs != null && !functionalityIDs.isEmpty()) {
			LOGGER.debug("SIZE=" + functionalityIDs.size());
			Iterator<Integer> iter = functionalityIDs.iterator();
			while (iter.hasNext()) {
				Object id = iter.next();
				Integer intId = iter.next();
				LOGGER.debug("Function ID=" + intId.toString());
				LowFunctionality funct = getFromCache(intId.toString());
				if (funct != null) {
					LOGGER.debug("Function ID=" + intId.toString() + "found from cache");
					lowFunctList.add(funct);
				} else {
					LOGGER.debug("Function ID=" + intId.toString() + "not found from cache.");
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
			Criterion domainCdCriterrion = Restrictions.in("functId", functionalityIDs);
			criteria.add(domainCdCriterrion);
			List<SbiFunctions> temp = criteria.list();
			// Query
			// query=aSession.createQuery("from SbiFunctions f inner join f.sbiFuncRoles where s.functId in ("+functionalityIDs.get(0)+")");
			// List temp = query.list();
			if (!temp.isEmpty()) {
				Iterator<SbiFunctions> it = temp.iterator();
				while (it.hasNext()) {
					SbiFunctions func = it.next();
					LowFunctionality lowFunctionality = toLowFunctionality(func, false);
					putIntoCache(String.valueOf(lowFunctionality.getId()), lowFunctionality);
					lowFunctList.add(lowFunctionality);
					LOGGER.debug("ADD funcionality:" + lowFunctionality.getName());
				}
			}
			tx.commit();

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT.Size=" + lowFunctList.size());
		return lowFunctList;
	}

	/**
	 * Load root low functionality.
	 *
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the low functionality
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadRootLowFunctionality(boolean)
	 */
	@Override
	public LowFunctionality loadRootLowFunctionality(boolean recoverBIObjects) throws EMFUserError {
		LOGGER.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(ROOT);
		if (funct == null) {
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				/* ********* start luca changes *************** */
				// Criterion filters = Restrictions.isNull("parentFunct");
				Criterion filters = Restrictions.and(Restrictions.isNull("parentFunct"), Restrictions.eq("functTypeCd", FUNCT_TYPE_LOW));
				/* ************ end luca changes ************** */
				Criteria criteria = aSession.createCriteria(SbiFunctions.class);
				criteria.add(filters);
				SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
				if (hibFunct == null)
					return null;
				funct = toLowFunctionality(hibFunct, recoverBIObjects);
				tx.commit();
			} catch (HibernateException he) {
				rollbackIfActive(tx);
				LOGGER.error("HibernateException", he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				closeSessionIfOpen(aSession);
			}
			putIntoCache(ROOT, funct);
		}
		LOGGER.debug("OUT");
		return funct;
	}

	/**
	 * Load low functionality by path.
	 *
	 * @param functionalityPath the functionality path
	 * @param recoverBIObjects  the recover bi objects
	 *
	 * @return the low functionality
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByPath(java.lang.String)
	 */
	@Override
	public LowFunctionality loadLowFunctionalityByPath(String functionalityPath, boolean recoverBIObjects) throws EMFUserError {
		LOGGER.debug("IN");
		LowFunctionality funct = null;
		funct = getFromCache(functionalityPath);
		if (funct == null) {
			Session aSession = null;
			Transaction tx = null;
			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				Criterion domainCdCriterrion = Restrictions.eq("path", functionalityPath);
				Criteria criteria = aSession.createCriteria(SbiFunctions.class);
				criteria.add(domainCdCriterrion);
				SbiFunctions hibFunct = (SbiFunctions) criteria.uniqueResult();
				if (hibFunct == null)
					return null;
				funct = toLowFunctionality(hibFunct, recoverBIObjects);
				tx.commit();
			} catch (HibernateException he) {
				rollbackIfActive(tx);
				LOGGER.error("HibernateException", he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				closeSessionIfOpen(aSession);
			}
			putIntoCache(functionalityPath, funct);
		}
		LOGGER.debug("OUT");
		return funct;
	}

	/**
	 * Modify low functionality.
	 *
	 * @param aLowFunctionality the a low functionality
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#modifyLowFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality)
	 */
	@Override
	public void modifyLowFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, aLowFunctionality.getId());
			// delete all roles functionality

			updateSbiCommonInfo4Update(hibFunct);

			Set<SbiFuncRole> oldRoles = hibFunct.getSbiFuncRoles();
			Iterator<SbiFuncRole> iterOldRoles = oldRoles.iterator();
			while (iterOldRoles.hasNext()) {
				SbiFuncRole role = iterOldRoles.next();
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
			Criterion domainCdCriterrion = Restrictions.eq("valueCd", aLowFunctionality.getCodType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);
			SbiDomains functTypeDomain = (SbiDomains) criteria.uniqueResult();
			if (functTypeDomain == null) {
				LOGGER.error("The Domain with value_cd=" + aLowFunctionality.getCodType() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1037);
			}

			hibFunct.setFunctType(functTypeDomain);
			hibFunct.setFunctTypeCd(aLowFunctionality.getCodType());
			hibFunct.setName(aLowFunctionality.getName());

			Integer parentId = aLowFunctionality.getParentId();
			Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
			Criterion parentCriterion = Restrictions.eq("functId", parentId);
			parentCriteria.add(parentCriterion);
			SbiFunctions hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
			if (hibParentFunct == null) {
				LOGGER.error("The parent Functionality with id = " + parentId + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1037);
			}
			hibFunct.setParentFunct(hibParentFunct);

			// manages code and path
			String previousCode = hibFunct.getCode();
			String previousPath = hibFunct.getPath();
			String newCode = aLowFunctionality.getCode();
			String newPath = aLowFunctionality.getPath();
			if (!previousCode.equals(newCode) || !previousPath.equals(newPath)) {
				// the code or the path was changed, so the path of the current
				// folder and of its child folders
				// must be changed

				// the condition !previousPath.equals(newPath) was added for the
				// following reason:
				// till SpagoBI 1.9.3 a folder may have a path different from
				// parentPath + "/" + code,
				// with this condition those cases are considered and corrected.

				// changes the code and path of the current folder
				hibFunct.setCode(newCode);
				hibFunct.setPath(newPath);

				// loads sub folders and changes their path
				Criteria subFoldersCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion subFoldersCriterion = Restrictions.like("path", previousPath + "/", MatchMode.START);
				subFoldersCriteria.add(subFoldersCriterion);
				List<SbiFunctions> hibList = subFoldersCriteria.list();
				Iterator<SbiFunctions> it = hibList.iterator();
				while (it.hasNext()) {
					SbiFunctions aSbiFunctions = it.next();
					String oldPath = aSbiFunctions.getPath();
					String unchanged = oldPath.substring(previousPath.length());
					aSbiFunctions.setPath(newPath + unchanged);
				}
			}

			// commit all changes
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [modifyLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
		}
		LOGGER.debug("OUT");
	}

	/**
	 * Saves all roles for a functionality, using session and permission information. The permission for a functionality can be DEVELOPMENT, TEST, EXECUTION AND
	 * CREATE and each permission has its own roles.
	 *
	 * @param aSession          The current session object
	 * @param hibFunct          The functionality hibernate object
	 * @param aLowFunctionality The Low Functionality object
	 * @param permission        The string defining the permission
	 * @return A collection object containing all roles
	 * @throws EMFUserError
	 *
	 */
	private Set saveRolesFunctionality(Session aSession, SbiFunctions hibFunct, LowFunctionality aLowFunctionality, String permission) throws EMFUserError {
		Set functRoleToSave = new HashSet();
		Criterion domainCdCriterrion = null;
		Criteria criteria = null;
		criteria = aSession.createCriteria(SbiDomains.class);
		domainCdCriterrion = Restrictions.and(Restrictions.eq("valueCd", permission), Restrictions.eq("domainCd", SpagoBIConstants.PERMISSION_ON_FOLDER));
		criteria.add(domainCdCriterrion);
		SbiDomains permissionDomain = (SbiDomains) criteria.uniqueResult();
		if (permissionDomain == null) {
			LOGGER.error("The Domain with value_cd=" + permission + " and domain_cd=" + SpagoBIConstants.PERMISSION_ON_FOLDER + " does not exist.");
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
			domainCdCriterrion = Restrictions.eq("name", role.getName());
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
		LOGGER.debug("The [saveRolesFunctionality] occurs. LowFunctionality cache will be cleaned.");
		this.clearCache();
		return functRoleToSave;
	}

	/**
	 * Insert low functionality.
	 *
	 * @param aLowFunctionality the a low functionality
	 * @param profile           the profile
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#insertLowFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality,
	 *      it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public LowFunctionality insertLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = new SbiFunctions();
			hibFunct.setCode(aLowFunctionality.getCode());
			hibFunct.setDescr(aLowFunctionality.getDescription());
			Criterion domainCdCriterrion = Restrictions.eq("valueCd", aLowFunctionality.getCodType());
			Criteria criteria = aSession.createCriteria(SbiDomains.class);
			criteria.add(domainCdCriterrion);
			SbiDomains functTypeDomain = (SbiDomains) criteria.uniqueResult();
			if (functTypeDomain == null) {
				LOGGER.error("The Domain with value_cd=" + aLowFunctionality.getCodType() + " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
			}
			hibFunct.setFunctType(functTypeDomain);
			hibFunct.setFunctTypeCd(aLowFunctionality.getCodType());
			hibFunct.setName(aLowFunctionality.getName());
			hibFunct.setPath(aLowFunctionality.getPath());

			Integer parentId = aLowFunctionality.getParentId();
			SbiFunctions hibParentFunct = null;
			if (parentId != null) {
				// if it is not the root controls if the parent functionality
				// exists
				Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion parentCriterion = Restrictions.eq("functId", parentId);
				parentCriteria.add(parentCriterion);
				hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
				if (hibParentFunct == null) {
					LOGGER.error("The parent Functionality with id = " + parentId + " does not exist.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
				}
			}
			// if it is the root the parent functionality is null
			hibFunct.setParentFunct(hibParentFunct);

			// manages prog column that determines the folders order
			if (hibParentFunct == null)
				hibFunct.setProg(1);
			else {
				// loads sub functionalities
				// Query hibQuery =
				// aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = "
				// + parentId);
				Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = ?");
				hibQuery.setInteger(0, parentId.intValue());
				Integer maxProg = (Integer) hibQuery.uniqueResult();
				if (maxProg != null)
					hibFunct.setProg(maxProg.intValue() + 1);
				else
					hibFunct.setProg(1);
			}

			updateSbiCommonInfo4Insert(hibFunct);
			aSession.save(hibFunct);
			aLowFunctionality.setProg(hibFunct.getProg());
			aLowFunctionality.setId(hibFunct.getFunctId());
			if (hibFunct.getFunctType().equals(FUNCT_TYPE_USER)) {
				if (hibFunct.getParentFunct() != null) {
					aLowFunctionality.setParentId(hibFunct.getParentFunct().getFunctId());
				}
			}
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
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [insertLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
			LOGGER.debug("OUT");
		}
		return aLowFunctionality;

	}

	/**
	 * Erase low functionality.
	 *
	 * @param aLowFunctionality the a low functionality
	 * @param profile           the profile
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#eraseLowFunctionality(it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality,
	 *      it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public void eraseLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			if (hasChild(aLowFunctionality.getId())) {
				HashMap params = new HashMap();
				params.put(PAGE, "BIObjectsPage");
				// params.put(SpagoBIConstants.ACTOR,
				// SpagoBIConstants.ADMIN_ACTOR);
				params.put(SpagoBIConstants.OPERATION, SpagoBIConstants.FUNCTIONALITIES_OPERATION);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1000, new Vector(), params);
			}
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, aLowFunctionality.getId());
			Set<SbiFuncRole> oldRoles = hibFunct.getSbiFuncRoles();
			Iterator<SbiFuncRole> iterOldRoles = oldRoles.iterator();
			while (iterOldRoles.hasNext()) {
				SbiFuncRole role = iterOldRoles.next();
				aSession.delete(role);
			}

			// update prog column in other functions
			// String hqlUpdateProg =
			// "update SbiFunctions s set s.prog = (s.prog - 1) where s.prog > "
			// + hibFunct.getProg() + " and s.parentFunct.functId = " +
			// hibFunct.getParentFunct().getFunctId();
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
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (EMFUserError emfue) {
			rollbackIfActive(tx);
			throw emfue;
		} catch (Exception e) {
			logException(e);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [eraseLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
			LOGGER.debug("OUT");
		}
	}

	/**
	 * From the Hibernate Low Functionality object at input, gives the corrispondent <code>LowFunctionality</code> object.
	 *
	 * @param hibFunct         The Hibernate Low Functionality object
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the corrispondent output <code>LowFunctionality</code>
	 */
	public LowFunctionality toLowFunctionality(SbiFunctions hibFunct, boolean recoverBIObjects) {
		return toLowFunctionality(hibFunct, recoverBIObjects, null);
	}

	/**
	 * From the Hibernate Low Functionality object at input, gives the corrispondent <code>LowFunctionality</code> object.
	 *
	 * @param hibFunct         The Hibernate Low Functionality object
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the corrispondent output <code>LowFunctionality</code>
	 *
	 *
	 */

	public LowFunctionality toLowFunctionality(SbiFunctions hibFunct, boolean recoverBIObjects, List<String> allowedDocTypes) {
		return toLowFunctionality(hibFunct, recoverBIObjects, allowedDocTypes, null, null);
	}

	public LowFunctionality toLowFunctionality(SbiFunctions hibFunct, boolean recoverBIObjects, List<String> allowedDocTypes, String date, String status) {
		LOGGER.debug("IN");
		LowFunctionality lowFunct = new LowFunctionality();
		lowFunct.setId(hibFunct.getFunctId());
		LOGGER.debug("ID=" + hibFunct.getFunctId().toString());
		lowFunct.setCode(hibFunct.getCode());
		lowFunct.setCodType(hibFunct.getFunctTypeCd());
		lowFunct.setDescription(hibFunct.getDescr());
		lowFunct.setName(hibFunct.getName());
		LOGGER.debug("NAME=" + hibFunct.getName());
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

		Set<SbiFuncRole> roles = hibFunct.getSbiFuncRoles();
		if (roles != null) {
			LOGGER.debug("getSbiFuncRoles() size=" + roles.size());
			Iterator<SbiFuncRole> iterRoles = roles.iterator();
			while (iterRoles.hasNext()) {
				SbiFuncRole hibfuncrole = iterRoles.next();
				SbiExtRoles hibRole = hibfuncrole.getId().getRole();
				SbiDomains hibPermission = hibfuncrole.getId().getState();
				LOGGER.debug("hibfuncrole.getId().getRole().getName()=" + hibRole.getName());
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

			String[] filteringStatus = null;
			List<String> filteringStatusList = null;
			if (status != null) {
				filteringStatus = status.split(",", -1);
				filteringStatusList = Arrays.asList(filteringStatus);
			}

			BIObjectDAOHibImpl objDAO = null;
			try {
				objDAO = (BIObjectDAOHibImpl) DAOFactory.getBIObjectDAO();

				Set hibObjFuncs = hibFunct.getSbiObjFuncs();
				for (Iterator it = hibObjFuncs.iterator(); it.hasNext();) {
					SbiObjFunc hibObjFunc = (SbiObjFunc) it.next();
					if (checkObjType(hibObjFunc.getId().getSbiObjects(), allowedDocTypes)) {
						BIObject object = objDAO.toBIObject(hibObjFunc.getId().getSbiObjects(), null);

						if (date != null) {
							SbiObjects sbiObj = hibObjFunc.getId().getSbiObjects();
							if (status != null && !filteringStatusList.contains(sbiObj.getStateCode()))
								continue;

							if (sbiObj.getCommonInfo().getTimeUp() != null) {
								if (sbiObj.getCommonInfo().getTimeIn().getTime() >= new Date(date).getTime()
										|| sbiObj.getCommonInfo().getTimeUp().getTime() > new Date(date).getTime()) {
									biObjects.add(object);
								}
							} else {
								if (sbiObj.getCommonInfo().getTimeIn().getTime() >= new Date(date).getTime()) {
									biObjects.add(object);
								}
							}

						} else if (status != null) {
							SbiObjects sbiObj = hibObjFunc.getId().getSbiObjects();
							if (!filteringStatusList.contains(sbiObj.getStateCode()))
								continue;
							biObjects.add(object);
						} else {
							biObjects.add(object);
						}

					}
				}
			} catch (EMFUserError e) {
				LOGGER.error("Error", e);
			}
		}
		lowFunct.setBiObjects(biObjects);
		LOGGER.debug("OUT");

		return lowFunct;
	}

	private boolean checkObjType(SbiObjects sbiObjects, List<String> allowedDocTypes) {
		if (allowedDocTypes != null && !allowedDocTypes.isEmpty()) {
			boolean notFound = true;
		}
		// TODO : Really?
		return true;
	}

	/**
	 * Exist by code.
	 *
	 * @param code the code
	 *
	 * @return the integer
	 *
	 * @throws EMFUserError the EMF user error
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
			Criterion domainCdCriterrion = Restrictions.eq("code", code);
			Criteria criteria = aSession.createCriteria(SbiFunctions.class);
			criteria.add(domainCdCriterrion);
			SbiFunctions func = (SbiFunctions) criteria.uniqueResult();
			if (func != null) {
				id = func.getFunctId();
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return id;
	}

	/**
	 * Load all low functionalities.
	 *
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List<LowFunctionality> loadAllLowFunctionalities(boolean recoverBIObjects) throws EMFUserError {
		return loadAllLowFunctionalities(recoverBIObjects, null);
	}

	@Override
	public List<LowFunctionality> loadAllLowFunctionalities(String dateFilter) throws EMFUserError {
		return loadAllLowFunctionalities(true, null, dateFilter, null);
	}

	@Override
	public List<LowFunctionality> loadAllLowFunctionalities(boolean recoverBIObjects, List<String> allowedDocTypes) throws EMFUserError {
		return loadAllLowFunctionalities(recoverBIObjects, allowedDocTypes, null, null);

	}

	/**
	 * Load all low functionalities.
	 *
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List<LowFunctionality> loadAllLowFunctionalities(boolean recoverBIObjects, List<String> allowedDocTypes, String date, String status)
			throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<LowFunctionality> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String username = null;
			IEngUserProfile profile = UserProfileManager.getProfile();

			// giovanniluca.ulivo@eng.it change
			if (profile == null) {
				// try to get the profile store in dao
				profile = this.getUserProfile();
			}
			// end change

			if (profile != null) {
				username = (String) ((UserProfile) profile).getUserId();
			}

			Query hibQuery = null;

			// user has admin functionality it.eng.spagobi.commons.constants.CommunityFunctionalityConstants.VIEW_MY_FOLDER_ADMIN he can view all
			// Low_func and all user func
			// else he can view only his personal userFunc
			try {
				if (profile != null && profile.isAbleToExecuteAction(VIEW_MY_FOLDER_ADMIN)) {
					hibQuery = aSession.createQuery(
							" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' or s.functTypeCd = 'USER_FUNCT' order by s.parentFunct.functId, s.prog");
				} else if (username == null) {
					hibQuery = aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' order by s.parentFunct.functId, s.prog");
				} else {
					// hibQuery =
					// aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' or s.path like '/"+username+"'
					// order by
					// s.parentFunct.functId, s.prog");
					hibQuery = aSession
							.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' or s.path like ? order by s.parentFunct.functId, s.prog");
					hibQuery.setString(0, "/" + username);
				}
			} catch (EMFInternalError e) {
				LOGGER.error("EMFInternalError while access to DBMS", e);
			}

			/* ********* end luca changes ***************** */

			List<SbiFunctions> hibList = hibQuery.list();

			Iterator<SbiFunctions> it = hibList.iterator();

			while (it.hasNext()) {
				LowFunctionality funct = toLowFunctionality(it.next(), recoverBIObjects, allowedDocTypes, date, status);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public List<LowFunctionality> loadFunctionalitiesForSharing(Integer docId) {
		LOGGER.debug("IN");
		Session session = null;
		List<LowFunctionality> toReturn = new ArrayList<>();
		List<SbiFunctions> hibList = new ArrayList<>();
		IEngUserProfile profile;

		try {
			StringBuilder statement = new StringBuilder("from SbiFunctions f ");
			profile = getUserProfile();
			String username = (String) ((UserProfile) profile).getUserId();
			boolean isFinalUser = false;

			if (profile != null && profile.isAbleToExecuteAction(VIEW_MY_FOLDER_ADMIN)) {
//				statement.append("where (f.functTypeCd = 'LOW_FUNCT' or f.functTypeCd = 'USER_FUNCT')");

//				no personal folder should be present
				statement.append("where (f.functTypeCd = 'LOW_FUNCT')");
			} else if (username == null) {
				statement.append("where f.functTypeCd = 'LOW_FUNCT'");
			} else {
				isFinalUser = true;
				statement.append("where (f.functTypeCd = 'LOW_FUNCT' or f.path = :path)");
			}

			statement.append(" and f.functId not in (select distinct obf.id.sbiFunctions.functId from SbiObjFunc obf where obf.id.sbiObjects.biobjId = :docId)")
					.append(" order by f.parentFunct.functId, f.prog");
			session = getSession();
			Query query = session.createQuery(statement.toString());

			if (username != null && isFinalUser) {
				query.setString("path", "/" + username);
			}
			query.setInteger("docId", docId);
			hibList = query.list();
			Iterator<SbiFunctions> it = hibList.iterator();
			while (it.hasNext()) {
				LowFunctionality funct = toLowFunctionality(it.next(), false, null, null, null);
				toReturn.add(funct);
			}
		} catch (Exception e) {
			LOGGER.error("Cannot load functionalities for sharing", e);
			throw new SpagoBIDAOException("Cannot load functionalities for sharing", e);
		} finally {
			closeSessionIfOpen(session);
		}

		LOGGER.debug("OUT");
		return toReturn;
	}

	/**
	 * Load sub low functionalities.
	 *
	 * @param initialPath      the initial path
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadSubLowFunctionalities(java.lang.String, boolean)
	 */
	@Override
	public List loadSubLowFunctionalities(String initialPath, boolean recoverBIObjects) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<LowFunctionality> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// loads folder corresponding to initial path
			Criterion domainCdCriterrion = Restrictions.eq("path", initialPath);
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
			// Query hibQuery =
			// aSession.createQuery(" from SbiFunctions s where s.path like '" +
			// initialPath + "/%' order by s.parentFunct.functId, s.prog");
			Query hibQuery = aSession.createQuery(" from SbiFunctions s where s.path like ? order by s.parentFunct.functId, s.prog");
			// Query hibQuery =
			// aSession.createQuery(" from SbiFunctions s where s.functTypeCd = 'LOW_FUNCT' and s.path like '"
			// + initialPath +
			// "/%' order by s.parentFunct.functId, s.prog");
			/* ********* end luca changes ***************** */
			hibQuery.setString(0, initialPath + "/%");
			List<SbiFunctions> hibList = hibQuery.list();
			Iterator<SbiFunctions> it = hibList.iterator();
			while (it.hasNext()) {
				funct = toLowFunctionality(it.next(), recoverBIObjects);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/**
	 * Checks for child.
	 *
	 * @param id the id
	 *
	 * @return true, if checks for child
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#hasChild(java.lang.String)
	 */
	@Override
	public boolean hasChild(Integer id) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// controls if there are sub folders
			Criterion parentChildCriterion = Restrictions.eq("parentFunct.functId", id);
			Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
			parentCriteria.add(parentChildCriterion);
			List childFunctions = parentCriteria.list();
			if (childFunctions != null && !childFunctions.isEmpty())
				return true;

			// controls if there are objects inside
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, id);
			Set hibObjfunctions = hibFunct.getSbiObjFuncs();
			if (hibObjfunctions != null && !hibObjfunctions.isEmpty())
				return true;
			// Criterion objectChildCriterion =
			// Restrictions.eq("sbiFunction.functId", id);
			// Criteria objectCriteria =
			// aSession.createCriteria(SbiObjFunc.class);
			// objectCriteria.add(objectChildCriterion);
			// List childObjects = objectCriteria.list();
			// if (childObjects != null && childObjects.size() > 0) return true;

			tx.commit();

			return false;
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
	}

	/**
	 * Deletes a set of inconsistent roles reference from the database, in order to keep functionalities tree permissions consistence.
	 *
	 * @param rolesSet the set containing the roles to erase
	 *
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public void deleteInconsistentRoles(Set rolesSet) throws EMFUserError {
		LOGGER.debug("IN");
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

				// hql =
				// " from SbiFuncRole as funcRole where funcRole.id.function = '"
				// + sbiFunct.getFunctId() +
				// "' AND funcRole.id.role = '"+ roleId
				// +"' AND funcRole.stateCd ='"+stateCD+"'";
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
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			rollbackIfActive(tx);
			logException(ex);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [deleteInconsistentRoles] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
			LOGGER.debug("OUT");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO #loadChildFunctionalities(java.lang.Integer, boolean)
	 */
	@Override
	public List<LowFunctionality> loadChildFunctionalities(Integer parentId, boolean recoverBIObjects) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<LowFunctionality> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// loads sub functionalities
			Query hibQuery = aSession.createQuery(" from SbiFunctions s where s.parentFunct.functId = ?");
			hibQuery.setInteger(0, parentId.intValue());
			List<SbiFunctions> hibList = hibQuery.list();
			Iterator<SbiFunctions> it = hibList.iterator();
			while (it.hasNext()) {
				LowFunctionality funct = toLowFunctionality(it.next(), recoverBIObjects);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO #moveDownLowFunctionality(java.lang.Integer)
	 */
	@Override
	public void moveDownLowFunctionality(Integer functionalityID) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, functionalityID);

			Integer oldProg = hibFunct.getProg();
			Integer newProg = oldProg.intValue() + 1;

			// String upperFolderHql = "from SbiFunctions s where s.prog = " +
			// newProg.toString() +
			// " and s.parentFunct.functId = " +
			// hibFunct.getParentFunct().getFunctId().toString();
			String upperFolderHql = "from SbiFunctions s where s.prog = ? " + " and s.parentFunct.functId = ?";
			Query query = aSession.createQuery(upperFolderHql);
			query.setInteger(0, newProg.intValue());
			query.setInteger(1, hibFunct.getParentFunct().getFunctId().intValue());
			SbiFunctions hibUpperFunct = (SbiFunctions) query.uniqueResult();
			if (hibUpperFunct == null) {
				LOGGER.error("The function with prog [" + newProg + "] does not exist.");
				return;
			}

			hibFunct.setProg(newProg);
			hibUpperFunct.setProg(oldProg);

			updateSbiCommonInfo4Update(hibFunct);
			updateSbiCommonInfo4Update(hibUpperFunct);

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [moveDownLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
			LOGGER.debug("OUT");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO #moveUpLowFunctionality(java.lang.Integer)
	 */
	@Override
	public void moveUpLowFunctionality(Integer functionalityID) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = (SbiFunctions) aSession.load(SbiFunctions.class, functionalityID);
			Integer oldProg = hibFunct.getProg();
			Integer newProg = oldProg.intValue() - 1;

			// String upperFolderHql = "from SbiFunctions s where s.prog = " +
			// newProg.toString() +
			// " and s.parentFunct.functId = " +
			// hibFunct.getParentFunct().getFunctId().toString();
			String upperFolderHql = "from SbiFunctions s where s.prog = ? " + " and s.parentFunct.functId = ? ";
			Query query = aSession.createQuery(upperFolderHql);
			query.setInteger(0, newProg.intValue());
			query.setInteger(1, hibFunct.getParentFunct().getFunctId().intValue());
			SbiFunctions hibUpperFunct = (SbiFunctions) query.uniqueResult();
			if (hibUpperFunct == null) {
				LOGGER.error("The function with prog [" + newProg + "] does not exist.");
				return;
			}

			hibFunct.setProg(newProg);
			hibUpperFunct.setProg(oldProg);

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [moveUpLowFunctionality] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
			LOGGER.debug("OUT");
		}
	}

	@Override
	public List loadAllUserFunct() throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		List<SbiFunctions> sbifunct = new ArrayList<>();
		List<LowFunctionality> userfunct = new ArrayList<>();
		try {
			aSession = getSession();
			Query query = aSession.createQuery("from SbiFunctions where functTypeCd  = 'USER_FUNCT'");
			sbifunct = query.list();

			for (Iterator<SbiFunctions> iterator = sbifunct.iterator(); iterator.hasNext();) {
				LowFunctionality funct = toLowFunctionality(iterator.next(), false);
				putIntoCache(String.valueOf(funct.getId()), funct);
				userfunct.add(funct);
			}

		} catch (HibernateException he) {
			LOGGER.error("HibernateException", he);

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return userfunct;
	}

	/**
	 * Load all functionalities associated the user roles.
	 *
	 * @param onlyFirstLevel   limits functionalities to first level
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List<LowFunctionality> loadUserFunctionalities(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<LowFunctionality> realResult = new ArrayList<>();
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			String username = null;
			Collection roles = null;
			try {
				// RequestContainer reqCont =
				// RequestContainer.getRequestContainer();
				if (profile != null) {
					username = (String) ((UserProfile) profile).getUserId();
					roles = ((UserProfile) profile).getRolesForUse();

				}
			} catch (Exception e) {
				LOGGER.error("Error while recovering user profile", e);
			}
			boolean onlyFirstLevel = (parentId == null);

			Query hibQuery = null;

			// getting correct root parent id (if the function must return only
			// functionality of first level)
			Integer tmpParentId = null;
			List lstParentId = null;
			if (onlyFirstLevel) {
				hibQuery = aSession.createQuery(" from SbiFunctions s where s.parentFunct.functId is null and s.functTypeCd  = 'LOW_FUNCT'");
				// tmpParentId = (Integer)hibQuery.uniqueResult();
				lstParentId = hibQuery.list();
				tmpParentId = (lstParentId == null || lstParentId.isEmpty()) ? new Integer("-1") : ((SbiFunctions) lstParentId.get(0)).getFunctId();
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
			List<SbiFunctions> hibList = hibQuery.list();

			// getting correct ext_role_id
			String hql = " from SbiExtRoles as extRole where extRole.name in (:roles)  ";
			hibQuery = aSession.createQuery(hql);

			int originalSize = roles.size();
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

			Iterator<SbiFunctions> it = hibList.iterator();
			// maintains functionalities that have the same user's role
			while (it.hasNext()) {
				SbiFunctions tmpFunc = it.next();
				if ((UserUtilities.isAdministrator(profile)
						&& (tmpFunc.getFunctTypeCd().equalsIgnoreCase(FUNCT_TYPE_USER) || tmpFunc.getFunctTypeCd().equalsIgnoreCase(FUNCT_TYPE_LOW)))
						|| tmpFunc.getFunctTypeCd().equalsIgnoreCase(FUNCT_TYPE_COMMUNITY)) {
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
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/**
	 * Load all functionalities associated the user roles.
	 *
	 * @param onlyFirstLevel   limits functionalities to first level
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	@Override
	public List<LowFunctionality> loadUserFunctionalitiesFiltered(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile, String permission)
			throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<LowFunctionality> realResult = new ArrayList<>();
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
			boolean isFirstLevel = (parentId == null);

			Query hibQuery = null;

			// getting correct root parent id (if the function must return only
			// functionality of first level)
			Integer tmpParentId = null;
			List lstParentId = null;
			if (isFirstLevel) {
				hibQuery = aSession.createQuery(" from SbiFunctions s where s.parentFunct.functId is null and s.functTypeCd  = 'LOW_FUNCT'");
				lstParentId = hibQuery.list();
				tmpParentId = (lstParentId == null || lstParentId.isEmpty()) ? new Integer("-1") : ((SbiFunctions) lstParentId.get(0)).getFunctId();
			} else {
				tmpParentId = parentId;
			}

			// getting functionalities
			if (isFirstLevel) {
				hibQuery = aSession.createQuery("select distinct sfr.id.function from SbiFuncRole sfr where "
						+ "sfr.id.function.functTypeCd = 'LOW_FUNCT' and sfr.id.function.parentFunct.functId = ?  "
						+ "and sfr.stateCd = ? and sfr.id.role.name in (:roles) ");
				// CANNOT order by in SQL query: see
				// https://spagobi.eng.it/jira/browse/SPAGOBI-942
				// +
				// "order by sfr.id.function.parentFunct.functId, sfr.id.function.prog");
				hibQuery.setInteger(0, tmpParentId.intValue());
				hibQuery.setString(1, permission);
				hibQuery.setParameterList("roles", roles);
				// only for administrator are getted personal folders (since
				// SpagoBI 5)
				if (UserUtilities.isAdministrator(profile)) {
					Query hibQueryPersonalFolder = aSession.createQuery("select f from SbiFunctions f where f.path like ? ");
					hibQueryPersonalFolder.setString(0, "/" + username);
					List<SbiFunctions> hibListPersF = hibQueryPersonalFolder.list();
					Iterator<SbiFunctions> it = hibListPersF.iterator();
					while (it.hasNext()) {
						SbiFunctions tmpFunc = it.next();
						LowFunctionality funct = toLowFunctionality(tmpFunc, recoverBIObjects);
						putIntoCache(String.valueOf(funct.getId()), funct);
						realResult.add(funct);
					}
				}
			} else {
				hibQuery = aSession.createQuery("select distinct sfr.id.function from SbiFuncRole sfr where "
						+ "sfr.id.function.functTypeCd = 'LOW_FUNCT' and sfr.id.function.parentFunct.functId = ? "
						+ "and sfr.stateCd = ? and sfr.id.role.name in (:roles) ");
				// CANNOT order by in SQL query: see
				// https://spagobi.eng.it/jira/browse/SPAGOBI-942
				// +
				// "order by sfr.id.function.parentFunct.functId, sfr.id.function.prog");
				hibQuery.setInteger(0, tmpParentId.intValue());
				hibQuery.setString(1, permission);
				hibQuery.setParameterList("roles", roles);
			}
			List<SbiFunctions> hibList = hibQuery.list();

			// MUST order using a comparator, see
			// https://spagobi.eng.it/jira/browse/SPAGOBI-942
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

			Iterator<SbiFunctions> it = hibList.iterator();
			while (it.hasNext()) {
				SbiFunctions tmpFunc = it.next();

				LowFunctionality funct = toLowFunctionality(tmpFunc, recoverBIObjects);
				putIntoCache(String.valueOf(funct.getId()), funct);
				realResult.add(funct);
			}
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	private boolean existFunction(List<LowFunctionality> lstFunctions, SbiFunctions newFunct) {
		boolean res = false;
		for (int i = 0; i < lstFunctions.size(); i++) {
			LowFunctionality tmpFunct = lstFunctions.get(i);
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
	 * @param functId the identifier of functionality child
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(Integer)
	 */
	@Override
	public List loadParentFunctionalities(Integer functId, Integer rootFolderID) throws EMFUserError {
		LOGGER.debug("IN");

		LowFunctionality funct = null;
		Integer tmpFunctId = null;
		Session aSession = null;
		Transaction tx = null;
		List<LowFunctionality> realResult = new ArrayList<>();
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
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public Integer insertCommunityFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer result = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiFunctions hibFunct = new SbiFunctions();

			// recover sbidomain of the user functionality
			Criterion vcdEQusfunct = Restrictions.eq("valueCd", FUNCT_TYPE_COMMUNITY);
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
				// if it is not the root controls if the parent functionality
				// exists
				Criteria parentCriteria = aSession.createCriteria(SbiFunctions.class);
				Criterion parentCriterion = Restrictions.eq("functId", parentId);
				parentCriteria.add(parentCriterion);
				hibParentFunct = (SbiFunctions) parentCriteria.uniqueResult();
				if (hibParentFunct == null) {
					LOGGER.error("The parent Functionality with id = " + parentId + " does not exist.");
					throw new EMFUserError(EMFErrorSeverity.ERROR, 1038);
				}
			}
			// if it is the root the parent functionality is null
			hibFunct.setParentFunct(hibParentFunct);

			// manages prog column that determines the folders order
			if (hibParentFunct == null)
				hibFunct.setProg(1);
			else {
				// loads sub functionalities

				Query hibQuery = aSession.createQuery("select max(s.prog) from SbiFunctions s where s.parentFunct.functId = ? and s.functTypeCd = ?");
				hibQuery.setInteger(0, parentId.intValue());
				hibQuery.setString(1, FUNCT_TYPE_COMMUNITY);
				Integer maxProg = (Integer) hibQuery.uniqueResult();
				if (maxProg != null)
					hibFunct.setProg(maxProg.intValue() + 1);
				else
					hibFunct.setProg(1);
			}

			updateSbiCommonInfo4Insert(hibFunct, true);

			result = (Integer) aSession.save(hibFunct);

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("HibernateException", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("The [insertCommunityFunctionality] occurs. LowFunctionality cache will be cleaned.");
			this.clearCache();
			LOGGER.debug("OUT");
		}
		return result;
	}

	private LowFunctionality getFromCache(String key) {
		LOGGER.debug("IN");
		String tenantId = this.getTenant();
		LowFunctionality funct = null;
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
							funct = (LowFunctionality) el.getValue();
						}
					}
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error while getting a LowFunctionality cache item with key " + key + " for tenant " + tenantId, t);
			}
		}
		LOGGER.debug("OUT");
		return funct;
	}

	private void putIntoCache(String key, LowFunctionality funct) {
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
				cacheManager.getCache(cacheName).put(new Element(key, funct));
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Error while putting LowFunctionality cache item with key " + key + " for tenant " + tenantId, t);
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
				throw new SpagoBIRuntimeException("Error during LowFunctionality cache full cleaning process for tenant " + tenantId, t);
			}
		}
	}
}
