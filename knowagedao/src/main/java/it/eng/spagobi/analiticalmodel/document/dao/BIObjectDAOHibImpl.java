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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.bo.Viewpoint;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjFunc;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjFuncId;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjTemplates;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.util.DocumentCompositionUtil;
import it.eng.spagobi.analiticalmodel.document.util.EscapedLikeRestrictions;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFunctions;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.BIObjectParameterDAOHibImpl;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.engines.config.dao.EngineDAOHibImpl;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter.TYPE;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDs;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;

/**
 * Defines the Hibernate implementations for all DAO methods, for a BI Object.
 */
public class BIObjectDAOHibImpl extends AbstractHibernateDAO implements IBIObjectDAO {

	private static final Logger LOGGER = LogManager.getLogger(BIObjectDAOHibImpl.class);

	public static final String COLUMN_LABEL = "LABEL";
	public static final String COLUMN_NAME = "NAME";
	public static final String COLUMN_ENGINE = "ENGINE";
	public static final String COLUMN_STATE = "STATE";
	public static final String COLUMN_TYPE = "TYPE";
	public static final String COLUMN_DATE = "CREATION_DATE";
	public static final String SCOPE_NODE = "node";

	public static final String START_WITH = "START_WITH";
	public static final String END_WITH = "END_WITH";
	public static final String NOT_EQUALS_TO = "NOT_EQUALS_TO";
	public static final String EQUALS_TO = "EQUALS_TO";
	public static final String CONTAINS = "CONTAINS";
	public static final String LESS_THAN = "LESS_THAN";
	public static final String EQUALS_OR_GREATER_THAN = "EQUALS_OR_GREATER_THAN";
	public static final String GREATER_THAN = "GREATER_THAN";
	public static final String EQUALS_OR_LESS_THAN = "EQUALS_OR_LESS_THAN";
	public static final String NOT_ENDS_WITH = "NOT_ENDS_WITH";
	public static final String NOT_CONTAINS = "NOT_CONTAINS";
	public static final String IS_NULL = "IS_NULL";
	public static final String NOT_NULL = "NOT_NULL";

	private Map<TYPE, Domain> defaultParameterMap = null;

	/**
	 * Load bi object for execution by id and role.
	 *
	 * @param id   the id
	 * @param role the role
	 * @return the BI object
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadBIObjectForExecutionByIdAndRole(java.lang.Integer, java.lang.String)
	 */
	@Override
	public BIObject loadBIObjectForExecutionByIdAndRole(Integer id, String role) throws EMFUserError {
		LOGGER.debug("Loading biObject with id {} and role {}", id, role);
		Session aSession = null;
		Transaction tx = null;
		BIObject biObject = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			biObject = loadBIObjectForDetail(id);
			// String hql = "from SbiObjPar s where s.sbiObject.biobjId = " +
			// biObject.getId() + " order by s.priority asc";
			String hql = "from SbiObjPar s where s.sbiObject.biobjId = ? order by s.priority asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, biObject.getId().intValue());
			List<SbiObjPar> hibObjectPars = hqlQuery.list();
			SbiObjPar hibObjPar = null;
			Iterator<SbiObjPar> it = hibObjectPars.iterator();
			BIObjectParameter tmpBIObjectParameter = null;
			BIObjectParameterDAOHibImpl aBIObjectParameterDAOHibImpl = new BIObjectParameterDAOHibImpl();
			IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
			List<BIObjectParameter> biObjectParameters = new ArrayList<>();
			Parameter aParameter = null;
			int count = 1;
			while (it.hasNext()) {
				hibObjPar = it.next();
				tmpBIObjectParameter = aBIObjectParameterDAOHibImpl.toBIObjectParameter(hibObjPar);

				// *****************************************************************
				// **************** START PRIORITY RECALCULATION
				// *******************
				// *****************************************************************
				Integer priority = tmpBIObjectParameter.getPriority();
				if (priority == null || priority.intValue() != count) {
					LOGGER.warn(
							"The priorities of the biparameters for the document with id = {} are not sorted. Priority recalculation starts.",
							biObject.getId());
					aBIObjectParameterDAOHibImpl.recalculateBiParametersPriority(biObject.getId(), aSession);
					tmpBIObjectParameter.setPriority(count);
				}
				count++;
				// *****************************************************************
				// **************** END PRIORITY RECALCULATION
				// *******************
				// *****************************************************************

				aParameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(tmpBIObjectParameter.getParID(),
						role, false);
				tmpBIObjectParameter.setParID(aParameter.getId());
				tmpBIObjectParameter.setParameter(aParameter);
				biObjectParameters.add(tmpBIObjectParameter);
			}
			biObject.setDrivers(biObjectParameters);
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object for execution by id {} and role {}", id, role, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return biObject;
	}

	/**
	 * Load bi object by id.
	 *
	 * @param biObjectID the bi object id
	 * @return the BI object
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadBIObjectById(java.lang.Integer)
	 */
	@Override
	public BIObject loadBIObjectById(Integer biObjectID) throws EMFUserError {
		LOGGER.debug("Loading biObject with id {}", biObjectID);
		BIObject toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjects hibBIObject = (SbiObjects) aSession.get(SbiObjects.class, biObjectID);
			if (hibBIObject != null) {
				toReturn = toBIObject(hibBIObject, aSession);
			} else {
				LOGGER.warn("Unable to load document whose id is equal to [{}]", biObjectID);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object by id {}", biObjectID, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	/**
	 * Load bi object for detail.
	 *
	 * @param id the id
	 * @return the BI object
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadBIObjectForDetail(java.lang.Integer)
	 */
	@Override
	public BIObject loadBIObjectForDetail(Integer id) throws EMFUserError {
		Monitor monitor = MonitorFactory.start(
				"it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl.loadBIObjectForDetail(Integer id)");
		LOGGER.debug("Loading biObject with id {}", id);
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = " from SbiObjects where biobjId = ?";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, id.intValue());
			SbiObjects hibObject = (SbiObjects) hqlQuery.uniqueResult();
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				LOGGER.warn("Unable to load document whose id is equal to [{}]", id);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object for detail by id {}", id, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		monitor.stop();
		return biObject;
	}

	/**
	 * Load bi object by label.
	 *
	 * @param label the label
	 * @return the BI object
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadBIObjectByLabel(java.lang.String)
	 */
	@Override
	public BIObject loadBIObjectByLabel(String label) throws EMFUserError {
		LOGGER.debug("Loading biObject with label {}", label);
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("label", label);
			Criteria criteria = aSession.createCriteria(SbiObjects.class);
			criteria.add(labelCriterrion);
			SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				LOGGER.warn("Unable to load document whose label is equal to [{}]", label);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object by label {}", label, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return biObject;
	}

	/**
	 * Load bi object by label.
	 *
	 * @param label the label
	 * @return the BI object
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadBIObjectByLabel(java.lang.String)
	 */
	@Override
	public BIObject loadBIObjectByName(String name) throws EMFUserError {
		LOGGER.debug("Loading biObject with name {}", name);
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("name", name);
			Criteria criteria = aSession.createCriteria(SbiObjects.class);
			criteria.add(labelCriterrion);
			SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				LOGGER.warn("Unable to load document whose name is equal to [{}]", name);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object by name {}", name, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return biObject;
	}

	/**
	 * Load bi object for tree.
	 *
	 * @param id the id
	 * @return the BI object
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadBIObjectForTree(java.lang.Integer)
	 */
	@Override
	public BIObject loadBIObjectForTree(Integer id) throws EMFUserError {
		LOGGER.debug("Loading biObject with id {}", id);
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			LOGGER.debug("Hibernate session obtained: {}", aSession);
			tx = aSession.beginTransaction();
			LOGGER.debug("Hibernate transaction started");
			Criterion domainCdCriterrion = Restrictions.eq("biobjId", id);
			Criteria criteria = aSession.createCriteria(SbiObjects.class);
			criteria.add(domainCdCriterrion);
			LOGGER.debug("Hibernate criteria filled: {}", criteria);
			SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();
			LOGGER.debug("Hibernate object retrived: {}", hibObject);
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				LOGGER.warn("Unable to load document whose id is equal to [{}]", id);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object for tree by id {}", id, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("End loading biObject with id: {}", id);
		}
		return biObject;
	}

	/**
	 * Modify bi object.
	 *
	 * @param obj the obj
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#modifyBIObject(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
	 */
	@Override
	public void modifyBIObject(BIObject obj) throws EMFUserError {
		internalModify(obj, null, false);
	}

	/**
	 * Modify bi object.
	 *
	 * @param obj        the obj
	 * @param loadParsDC boolean for management Document Composition params
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#modifyBIObject(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
	 */
	@Override
	public void modifyBIObject(BIObject obj, boolean loadParsDC) throws EMFUserError {
		internalModify(obj, null, loadParsDC);
	}

	/**
	 * Modify bi object.
	 *
	 * @param obj     the obj
	 * @param objTemp the obj temp
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#modifyBIObjectWithoutVersioning(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
	 */
	@Override
	public void modifyBIObject(BIObject obj, ObjTemplate objTemp) throws EMFUserError {
		internalModify(obj, objTemp, false);
	}

	/**
	 * Modify bi object (specially provided for custom-made output category parameters for the SUNBURST chart).
	 *
	 * @param obj     the obj
	 * @param objTemp the obj temp
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#modifyBIObjectWithoutVersioning(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@Override
	public void modifyBIObject(BIObject obj, ObjTemplate objTemp, List categories) throws EMFUserError {
		internalModify(obj, objTemp, false, categories);
	}

	/**
	 * Modify bi object (for special chart types, that need exclusion of some of default output parameters). Example: WORDCLOUD, PARALLEL and CHORD chart types.
	 *
	 * @param obj     the obj
	 * @param objTemp the obj temp
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#modifyBIObjectWithoutVersioning(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@Override
	public void modifyBIObject(BIObject obj, ObjTemplate objTemp, String specificChartTypes) throws EMFUserError {
		internalModify(obj, objTemp, false, specificChartTypes);
	}

	/**
	 * Modify bi object.
	 *
	 * @param obj        the obj
	 * @param objTemp    the obj temp
	 * @param loadParsDC boolean for management Document Composition params
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#modifyBIObjectWithoutVersioning(it.eng.spagobi.analiticalmodel.document.bo.BIObject)
	 */
	@Override
	public void modifyBIObject(BIObject obj, ObjTemplate objTemp, boolean loadParsDC) throws EMFUserError {
		internalModify(obj, objTemp, loadParsDC);

	}

	/**
	 * Updates the biobject data into database.
	 *
	 * @param biObject The BI Object as input
	 * @param objTemp  The BIObject template
	 * @throws EMFUserError If any exception occurred
	 */
	private void internalModify(BIObject biObject, ObjTemplate objTemp, boolean loadParsDC) throws EMFUserError {
		LOGGER.debug("Modifying biObject {} with template {}", biObject, objTemp);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, biObject.getId());

			updateSbiCommonInfo4Update(hibBIObject);

			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, biObject.getEngine().getId());
			hibBIObject.setSbiEngines(hibEngine);
			SbiDataSource dSource = null;
			if (biObject.getDataSourceId() != null) {
				dSource = (SbiDataSource) aSession.load(SbiDataSource.class, biObject.getDataSourceId());
			}
			hibBIObject.setDataSource(dSource);

			// SbiDataSet dSet = null;
			// if (biObject.getDataSetId() != null) {
			// Query hibQuery =
			// aSession.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			// hibQuery.setBoolean(0, true);
			// hibQuery.setInteger(1, biObject.getDataSetId());
			// dSet = (SbiDataSet) hibQuery.uniqueResult();
			// }
			// // hibBIObject.setDataSet(dSet);
			// hibBIObject.setDataSet((dSet == null) ? null :
			// dSet.getId().getDsId());

			hibBIObject.setDescr(biObject.getDescription());
			hibBIObject.setLabel(biObject.getLabel());
			hibBIObject.setName(biObject.getName());

			if (biObject.getEncrypt() != null)
				hibBIObject.setEncrypt(biObject.getEncrypt().shortValue());
			if (biObject.getVisible() != null)
				hibBIObject.setVisible(biObject.getVisible().shortValue());

			hibBIObject.setProfiledVisibility(biObject.getProfiledVisibility());
			hibBIObject.setRelName(biObject.getRelName());
			SbiDomains hibState = (SbiDomains) aSession.load(SbiDomains.class, biObject.getStateID());
			hibBIObject.setState(hibState);
			hibBIObject.setStateCode(biObject.getStateCode());
			SbiDomains hibObjectType = (SbiDomains) aSession.load(SbiDomains.class, biObject.getBiObjectTypeID());
			hibBIObject.setObjectType(hibObjectType);
			hibBIObject.setObjectTypeCode(biObject.getBiObjectTypeCode());

			hibBIObject.setRefreshSeconds(biObject.getRefreshSeconds());
			hibBIObject.setParametersRegion(biObject.getParametersRegion());
			hibBIObject.setLockedByUser(biObject.getLockedByUser());
			hibBIObject.setPreviewFile(biObject.getPreviewFile());

			// functionalities erasing
			Set<SbiObjFunc> hibFunctionalities = hibBIObject.getSbiObjFuncs();
			for (Iterator<SbiObjFunc> it = hibFunctionalities.iterator(); it.hasNext();) {
				aSession.delete(it.next());
			}
			// functionalities storing
			Set<SbiObjFunc> hibObjFunc = new HashSet<>();
			List<Integer> functionalities = biObject.getFunctionalities();
			for (Iterator<Integer> it = functionalities.iterator(); it.hasNext();) {
				Integer functId = it.next();
				SbiFunctions aSbiFunctions = (SbiFunctions) aSession.load(SbiFunctions.class, functId);
				SbiObjFuncId aSbiObjFuncId = new SbiObjFuncId();
				aSbiObjFuncId.setSbiFunctions(aSbiFunctions);
				aSbiObjFuncId.setSbiObjects(hibBIObject);
				SbiObjFunc aSbiObjFunc = new SbiObjFunc(aSbiObjFuncId);
				updateSbiCommonInfo4Update(aSbiObjFunc);
				aSession.save(aSbiObjFunc);
				hibObjFunc.add(aSbiObjFunc);
			}
			hibBIObject.setSbiObjFuncs(hibObjFunc);

			LOGGER.debug("Update dataset detail with id {} for biObject {}", biObject.getDataSetId(), biObject.getId());
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(biObject.getId(), biObject.getDataSetId(),
					aSession);

			// Previous implementation: commented by danristo
			// if (engineHasChanged) {
			// If Engine is changed, we have to load its specific output
			// parameters and save them
			// hibBIObject.getSbiOutputParameters().clear();
			// hibBIObject.getSbiOutputParameters().addAll(loadDriverSpecificOutputParameters(hibBIObject));
			// }

			// If the document previously had the output parameters, remove not user defined one them in order to refresh them. (danristo)
			// if (!hibBIObject.getSbiOutputParameters().isEmpty() || hibBIObject.getSbiOutputParameters() != null) {
			// delete SbiOutputParameters
			if (hibBIObject.getSbiOutputParameters() != null && !hibBIObject.getSbiOutputParameters().isEmpty()) {
				// delete only default system parameters
				Set<SbiOutputParameter> cleanedOutPars = new HashSet<>();
				for (Iterator<SbiOutputParameter> iterator = hibBIObject.getSbiOutputParameters().iterator(); iterator
						.hasNext();) {
					SbiOutputParameter sbiOutPar = iterator.next();
					if (sbiOutPar.getIsUserDefined() == null || !sbiOutPar.getIsUserDefined()) {
						cleanedOutPars.add(sbiOutPar);
					}
				}
				hibBIObject.setSbiOutputParameters(cleanedOutPars);
				DAOFactory.getOutputParameterDAO().removeSystemDefinedParametersByBiobjId(hibBIObject.getBiobjId(),
						aSession);
				aSession.flush();
			}

			List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject);

			for (Iterator<SbiOutputParameter> iterator = op.iterator(); iterator.hasNext();) {
				SbiOutputParameter sbiOutputParameter = iterator.next();
				aSession.save(sbiOutputParameter);
			}

			hibBIObject.getSbiOutputParameters().addAll(op);

			tx.commit();

			// update biobject template info
			if (objTemp != null) {
				try {
					ObjTemplate oldTemp = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biObject.getId());
					// set the biobject id into ObjTemplate (it should not be
					// necessary, but to avoid errors ...)
					objTemp.setBiobjId(biObject.getId());
					// insert or update new template
					IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
					dao.setUserProfile(this.getUserProfile());
					dao.insertBIObjectTemplate(objTemp, biObject);
					// if the input document is a document composition and
					// template is changed deletes existing parameters
					// and creates all new parameters automatically
					// (the parameters are recovered from all documents that
					// compose general document)
					if (loadParsDC && (oldTemp == null || objTemp.getId() == null
							|| objTemp.getId().compareTo(oldTemp.getId()) != 0)) {
						insertParametersDocComposition(biObject, objTemp, true);
					}

				} catch (Exception e) {
					LOGGER.error("Error during creation of document composition parameters for document id {}",
							biObject.getId(), e);
					throw new EMFUserError(EMFErrorSeverity.ERROR, e.getMessage());
				}

			}

			// insert relation with ds for data lineage
			// KNOWAGE-4819
			DAOFactory.getSbiObjDsDAO().insertRelationsFromObj(biObject);

			LOGGER.debug("OUT");
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error modifing BI Object {} using template {} with load parameters equals to {}", biObject,
					objTemp, loadParsDC, he);
			if (he.getCause().getMessage().contains("constraint")) {
				throw new SpagoBIDAOException("Document with label " + biObject.getLabel() + " already exists", he);
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Updates the biobject data into database (specially provided for custom-made output category parameters for the SUNBURST chart).
	 *
	 * @param biObject The BI Object as input
	 * @param objTemp  The BIObject template
	 * @throws EMFUserError If any exception occurred
	 */
	private void internalModify(BIObject biObject, ObjTemplate objTemp, boolean loadParsDC, List categories)
			throws EMFUserError {
		LOGGER.debug("Modifying biObject {} with template {} and categories {}", biObject, objTemp, categories);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, biObject.getId());

			updateSbiCommonInfo4Update(hibBIObject);

			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, biObject.getEngine().getId());
			hibBIObject.setSbiEngines(hibEngine);
			SbiDataSource dSource = null;
			if (biObject.getDataSourceId() != null) {
				dSource = (SbiDataSource) aSession.load(SbiDataSource.class, biObject.getDataSourceId());
			}
			hibBIObject.setDataSource(dSource);

			// SbiDataSet dSet = null;
			// if (biObject.getDataSetId() != null) {
			// Query hibQuery =
			// aSession.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			// hibQuery.setBoolean(0, true);
			// hibQuery.setInteger(1, biObject.getDataSetId());
			// dSet = (SbiDataSet) hibQuery.uniqueResult();
			// }
			// // hibBIObject.setDataSet(dSet);
			// hibBIObject.setDataSet((dSet == null) ? null :
			// dSet.getId().getDsId());

			hibBIObject.setDescr(biObject.getDescription());
			hibBIObject.setLabel(biObject.getLabel());
			hibBIObject.setName(biObject.getName());

			if (biObject.getEncrypt() != null)
				hibBIObject.setEncrypt(biObject.getEncrypt().shortValue());
			if (biObject.getVisible() != null)
				hibBIObject.setVisible(biObject.getVisible().shortValue());

			hibBIObject.setProfiledVisibility(biObject.getProfiledVisibility());
			hibBIObject.setRelName(biObject.getRelName());
			SbiDomains hibState = (SbiDomains) aSession.load(SbiDomains.class, biObject.getStateID());
			hibBIObject.setState(hibState);
			hibBIObject.setStateCode(biObject.getStateCode());
			SbiDomains hibObjectType = (SbiDomains) aSession.load(SbiDomains.class, biObject.getBiObjectTypeID());
			hibBIObject.setObjectType(hibObjectType);
			hibBIObject.setObjectTypeCode(biObject.getBiObjectTypeCode());

			hibBIObject.setRefreshSeconds(biObject.getRefreshSeconds());
			hibBIObject.setParametersRegion(biObject.getParametersRegion());
			hibBIObject.setLockedByUser(biObject.getLockedByUser());
			hibBIObject.setPreviewFile(biObject.getPreviewFile());

			// functionalities erasing
			Set<SbiObjFunc> hibFunctionalities = hibBIObject.getSbiObjFuncs();
			for (Iterator<SbiObjFunc> it = hibFunctionalities.iterator(); it.hasNext();) {
				aSession.delete(it.next());
			}
			// functionalities storing
			Set<SbiObjFunc> hibObjFunc = new HashSet<>();
			List<Integer> functionalities = biObject.getFunctionalities();
			for (Iterator<Integer> it = functionalities.iterator(); it.hasNext();) {
				Integer functId = it.next();
				SbiFunctions aSbiFunctions = (SbiFunctions) aSession.load(SbiFunctions.class, functId);
				SbiObjFuncId aSbiObjFuncId = new SbiObjFuncId();
				aSbiObjFuncId.setSbiFunctions(aSbiFunctions);
				aSbiObjFuncId.setSbiObjects(hibBIObject);
				SbiObjFunc aSbiObjFunc = new SbiObjFunc(aSbiObjFuncId);
				updateSbiCommonInfo4Update(aSbiObjFunc);
				aSession.save(aSbiObjFunc);
				hibObjFunc.add(aSbiObjFunc);
			}
			hibBIObject.setSbiObjFuncs(hibObjFunc);

			LOGGER.debug("Update dataset detail with id {} for biObject {}", biObject.getDataSetId(), biObject.getId());
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(biObject.getId(), biObject.getDataSetId(),
					aSession);

			if (!categories.isEmpty()) {

				// delete SbiOutputParameters
				if (hibBIObject.getSbiOutputParameters() != null && !hibBIObject.getSbiOutputParameters().isEmpty()) {
					hibBIObject.getSbiOutputParameters().clear();
					DAOFactory.getOutputParameterDAO().removeParametersByBiobjId(hibBIObject.getBiobjId(), aSession);
					aSession.flush();
				}

				// Persist the dataset
				List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject, categories);

				for (Iterator<SbiOutputParameter> iterator = op.iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutputParameter = iterator.next();
					aSession.save(sbiOutputParameter);
				}

				hibBIObject.getSbiOutputParameters().addAll(op);

			}

			tx.commit();

			// update biobject template info
			if (objTemp != null) {
				try {
					ObjTemplate oldTemp = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biObject.getId());
					// set the biobject id into ObjTemplate (it should not be
					// necessary, but to avoid errors ...)
					objTemp.setBiobjId(biObject.getId());
					// insert or update new template
					IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
					dao.setUserProfile(this.getUserProfile());
					dao.insertBIObjectTemplate(objTemp, biObject);
					// if the input document is a document composition and
					// template is changed deletes existing parameters
					// and creates all new parameters automatically
					// (the parameters are recovered from all documents that
					// compose general document)
					if (loadParsDC && (oldTemp == null || objTemp.getId() == null
							|| objTemp.getId().compareTo(oldTemp.getId()) != 0)) {
						insertParametersDocComposition(biObject, objTemp, true);
					}
				} catch (Exception e) {
					LOGGER.error("Error during creation of document composition parameters : ", e);
					throw new EMFUserError(EMFErrorSeverity.ERROR, e.getMessage());
				}

			}

			LOGGER.debug("OUT");
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error modifing BI Object {} using template {} with load parameters equals to {}", biObject,
					objTemp, loadParsDC, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Updates the biobject data into database (for special chart types, that need exclusion of some of default output parameters). Example: WORDCLOUD, PARALLEL and
	 * CHORD chart types.
	 *
	 * @param biObject The BI Object as input
	 * @param objTemp  The BIObject template
	 * @throws EMFUserError If any exception occurred
	 */
	private void internalModify(BIObject biObject, ObjTemplate objTemp, boolean loadParsDC, String specificChartTypes)
			throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, biObject.getId());

			updateSbiCommonInfo4Update(hibBIObject);

			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, biObject.getEngine().getId());
			hibBIObject.setSbiEngines(hibEngine);
			SbiDataSource dSource = null;
			if (biObject.getDataSourceId() != null) {
				dSource = (SbiDataSource) aSession.load(SbiDataSource.class, biObject.getDataSourceId());
			}
			hibBIObject.setDataSource(dSource);

			// SbiDataSet dSet = null;
			// if (biObject.getDataSetId() != null) {
			// Query hibQuery =
			// aSession.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			// hibQuery.setBoolean(0, true);
			// hibQuery.setInteger(1, biObject.getDataSetId());
			// dSet = (SbiDataSet) hibQuery.uniqueResult();
			// }
			// // hibBIObject.setDataSet(dSet);
			// hibBIObject.setDataSet((dSet == null) ? null :
			// dSet.getId().getDsId());

			hibBIObject.setDescr(biObject.getDescription());
			hibBIObject.setLabel(biObject.getLabel());
			hibBIObject.setName(biObject.getName());

			if (biObject.getEncrypt() != null)
				hibBIObject.setEncrypt(biObject.getEncrypt().shortValue());
			if (biObject.getVisible() != null)
				hibBIObject.setVisible(biObject.getVisible().shortValue());

			hibBIObject.setProfiledVisibility(biObject.getProfiledVisibility());
			hibBIObject.setRelName(biObject.getRelName());
			SbiDomains hibState = (SbiDomains) aSession.load(SbiDomains.class, biObject.getStateID());
			hibBIObject.setState(hibState);
			hibBIObject.setStateCode(biObject.getStateCode());
			SbiDomains hibObjectType = (SbiDomains) aSession.load(SbiDomains.class, biObject.getBiObjectTypeID());
			hibBIObject.setObjectType(hibObjectType);
			hibBIObject.setObjectTypeCode(biObject.getBiObjectTypeCode());

			hibBIObject.setRefreshSeconds(biObject.getRefreshSeconds());
			hibBIObject.setParametersRegion(biObject.getParametersRegion());
			hibBIObject.setLockedByUser(biObject.getLockedByUser());
			hibBIObject.setPreviewFile(biObject.getPreviewFile());

			// functionalities erasing
			Set<SbiObjFunc> hibFunctionalities = hibBIObject.getSbiObjFuncs();
			for (Iterator<SbiObjFunc> it = hibFunctionalities.iterator(); it.hasNext();) {
				aSession.delete(it.next());
			}
			// functionalities storing
			Set<SbiObjFunc> hibObjFunc = new HashSet<>();
			List<Integer> functionalities = biObject.getFunctionalities();
			for (Iterator<Integer> it = functionalities.iterator(); it.hasNext();) {
				Integer functId = it.next();
				SbiFunctions aSbiFunctions = (SbiFunctions) aSession.load(SbiFunctions.class, functId);
				SbiObjFuncId aSbiObjFuncId = new SbiObjFuncId();
				aSbiObjFuncId.setSbiFunctions(aSbiFunctions);
				aSbiObjFuncId.setSbiObjects(hibBIObject);
				SbiObjFunc aSbiObjFunc = new SbiObjFunc(aSbiObjFuncId);
				updateSbiCommonInfo4Update(aSbiObjFunc);
				aSession.save(aSbiObjFunc);
				hibObjFunc.add(aSbiObjFunc);
			}
			hibBIObject.setSbiObjFuncs(hibObjFunc);

			LOGGER.debug("Update dataset detail with id {} for biObject {}", biObject.getDataSetId(), biObject.getId());
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(biObject.getId(), biObject.getDataSetId(),
					aSession);

			if (!specificChartTypes.equals("")) {

				hibBIObject.getSbiOutputParameters().clear();
				DAOFactory.getOutputParameterDAO().removeParametersByBiobjId(hibBIObject.getBiobjId(), aSession);
				aSession.flush();

				List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject, specificChartTypes);

				for (Iterator<SbiOutputParameter> iterator = op.iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutputParameter = iterator.next();
					aSession.save(sbiOutputParameter);
				}

				hibBIObject.getSbiOutputParameters().addAll(op);

			}

			tx.commit();

			// update biobject template info
			if (objTemp != null) {
				try {
					ObjTemplate oldTemp = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biObject.getId());
					// set the biobject id into ObjTemplate (it should not be
					// necessary, but to avoid errors ...)
					objTemp.setBiobjId(biObject.getId());
					// insert or update new template
					IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
					dao.setUserProfile(this.getUserProfile());
					dao.insertBIObjectTemplate(objTemp, biObject);
					// if the input document is a document composition and
					// template is changed deletes existing parameters
					// and creates all new parameters automatically
					// (the parameters are recovered from all documents that
					// compose general document)
					if (loadParsDC && (oldTemp == null || objTemp.getId() == null
							|| objTemp.getId().compareTo(oldTemp.getId()) != 0)) {
						insertParametersDocComposition(biObject, objTemp, true);
					}
				} catch (Exception e) {
					LOGGER.error("Error during creation of document composition parameters : ", e);
					throw new EMFUserError(EMFErrorSeverity.ERROR, e.getMessage());
				}

			}

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error modifing BI Object {} using template {} with load parameters equals to {}", biObject,
					objTemp, loadParsDC, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Implements the query to insert a BIObject and its template. All information needed is stored into the input <code>BIObject</code> and
	 * <code>ObjTemplate</code> objects.
	 *
	 * @param obj     The object containing all insert information
	 * @param objTemp The template of the biobject
	 * @throws EMFUserError If an Exception occurred
	 */
	@Override
	public void insertBIObject(BIObject obj, ObjTemplate objTemp, boolean loadParsDC) throws EMFUserError {
		internalInsertBIObject(obj, objTemp, loadParsDC, true);
	}

	/**
	 * Implements the query to insert a BIObject. All information needed is stored into the input <code>BIObject</code> object.
	 *
	 * @param obj The object containing all insert information
	 * @throws EMFUserError If an Exception occurred
	 */
	@Override
	public Integer insertBIObject(BIObject obj) throws EMFUserError {
		return internalInsertBIObject(obj, null, false, true);
	}

	/**
	 * Implements the query to insert a BIObject. All information needed is stored into the input <code>BIObject</code> object.
	 *
	 * @param obj        The object containing all insert information
	 * @param loadParsDC boolean for management Document Composition params
	 * @throws EMFUserError If an Exception occurred
	 */
	@Override
	public void insertBIObject(BIObject obj, boolean loadParsDC) throws EMFUserError {
		internalInsertBIObject(obj, null, loadParsDC, true);
	}

	/**
	 * Implements the query to insert a BIObject. All information needed is stored into the input <code>BIObject</code> object.
	 *
	 * @param obj        The object containing all insert information
	 * @param loadParsDC boolean for management Document Composition params
	 * @throws EMFUserError If an Exception occurred
	 */
	@Override
	public Integer insertBIObject(BIObject obj, ObjTemplate objTemp) throws EMFUserError {
		return internalInsertBIObject(obj, objTemp, false, true);
	}

	/**
	 * Implements the query to insert a BIObject. All information needed is stored into the input <code>BIObject</code> object.
	 *
	 * @param obj        The object containing all insert information
	 * @param loadParsDC boolean for management Document Composition params
	 * @throws EMFUserError If an Exception occurred
	 */
	@Override
	public Integer insertBIObjectForClone(BIObject obj, ObjTemplate objTemp) throws EMFUserError {
		return internalInsertBIObject(obj, objTemp, false, false); // clone doesn't add default output parameter for each engine
	}

	private Integer internalInsertBIObject(BIObject obj, ObjTemplate objTemp, boolean loadParsDC, boolean loadOP)
			throws EMFUserError {
		LOGGER.debug("Inserting biObject {} with template {}", obj, objTemp);
		Session aSession = null;
		Transaction tx = null;
		Integer idToReturn = null;
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjects hibBIObject = new SbiObjects();
			// add the common info

			SbiEngines hibEngine = (SbiEngines) aSession.load(SbiEngines.class, obj.getEngine().getId());
			hibBIObject.setSbiEngines(hibEngine);
			hibBIObject.setDescr(obj.getDescription());

			hibBIObject.setLabel(obj.getLabel());
			hibBIObject.setName(obj.getName());
			if (obj.getEncrypt() != null) {
				hibBIObject.setEncrypt(obj.getEncrypt().shortValue());
			}
			if (obj.getVisible() != null) {
				hibBIObject.setVisible(obj.getVisible().shortValue());
			}
			hibBIObject.setProfiledVisibility(obj.getProfiledVisibility());
			hibBIObject.setRelName(obj.getRelName());

			SbiDomains hibState = (SbiDomains) aSession.load(SbiDomains.class, obj.getStateID());
			hibBIObject.setState(hibState);
			hibBIObject.setStateCode(obj.getStateCode());
			SbiDomains hibObjectType = (SbiDomains) aSession.load(SbiDomains.class, obj.getBiObjectTypeID());
			hibBIObject.setObjectType(hibObjectType);
			hibBIObject.setObjectTypeCode(obj.getBiObjectTypeCode());
			SbiDataSource dSource = null;
			if (obj.getDataSourceId() != null) {
				dSource = (SbiDataSource) aSession.load(SbiDataSource.class, obj.getDataSourceId());
			}
			hibBIObject.setDataSource(dSource);

			// if (obj.getDataSetId() != null) {
			// Query hibQuery =
			// aSession.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			// hibQuery.setBoolean(0, true);
			// hibQuery.setInteger(1, obj.getDataSetId());
			// dSet = (SbiDataSet) hibQuery.uniqueResult();
			// // dSet = (SbiDataSet) aSession.load(SbiDataSet.class,
			// obj.getDataSetId());
			// }
			// hibBIObject.setDataSet(dSet);
			// hibBIObject.setDataSet((dSet == null) ? null :
			// dSet.getId().getDsId());

			hibBIObject.setLockedByUser(obj.getLockedByUser());
			Integer refreshSeconds = obj.getRefreshSeconds();
			if (refreshSeconds == null)
				refreshSeconds = 0;
			hibBIObject.setRefreshSeconds(refreshSeconds);

			// parameters region
			hibBIObject.setParametersRegion(obj.getParametersRegion());

			// uuid generation
			UUIDGenerator uuidGenerator = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGenerator.generateTimeBasedUUID();
			String uuid = uuidObj.toString();
			hibBIObject.setUuid(uuid);
			if (obj.getPreviewFile() != null && !"".equals(obj.getPreviewFile())) {
				hibBIObject.setPreviewFile(obj.getPreviewFile());
			}
			hibBIObject.setCreationDate(new Date());
			hibBIObject.setCreationUser(obj.getCreationUser());

			updateSbiCommonInfo4Insert(hibBIObject);

			// save biobject
			Integer id = (Integer) aSession.save(hibBIObject);

			idToReturn = id;
			// recover the saved hibernate object
			hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, id);

			// Saving output parameters
			if (loadOP) {
				List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject);

				for (Iterator<SbiOutputParameter> iterator = op.iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutputParameter = iterator.next();
					aSession.save(sbiOutputParameter);
				}

				hibBIObject.getSbiOutputParameters().addAll(op);
			}

			// functionalities storing
			Set<SbiObjFunc> hibObjFunc = new HashSet<>();
			List<Integer> functionalities = obj.getFunctionalities();
			for (Iterator<Integer> it = functionalities.iterator(); it.hasNext();) {
				Integer functId = it.next();
				SbiFunctions aSbiFunctions = (SbiFunctions) aSession.load(SbiFunctions.class, functId);
				SbiObjFuncId aSbiObjFuncId = new SbiObjFuncId();
				aSbiObjFuncId.setSbiFunctions(aSbiFunctions);
				aSbiObjFuncId.setSbiObjects(hibBIObject);
				SbiObjFunc aSbiObjFunc = new SbiObjFunc(aSbiObjFuncId);
				updateSbiCommonInfo4Insert(aSbiObjFunc);
				aSession.save(aSbiObjFunc);
				hibObjFunc.add(aSbiObjFunc);
			}
			hibBIObject.setSbiObjFuncs(hibObjFunc);

			// update detail dataset relationship
			LOGGER.debug("Update dataset detail with id {} for biObject {}", obj.getDataSetId(), id);
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(id, obj.getDataSetId(), aSession);

			// we must close transaction before saving ObjTemplate,
			// since ObjTemplateDAO opens a new transaction and it would fail in
			// Ingres
			tx.commit();
			obj.setId(id);

			if (objTemp != null) {
				objTemp.setBiobjId(id);

				IObjTemplateDAO dao = DAOFactory.getObjTemplateDAO();
				dao.setUserProfile(this.getUserProfile());
				dao.insertBIObjectTemplate(objTemp, obj);
			}

			// insert relation with ds for data lineage
			DAOFactory.getSbiObjDsDAO().insertRelationsFromObj(obj);

			// if the document is a document composition creates all parameters
			// automatically
			// (the parameters are recovered from all documents that compose
			// general document)
			if (loadParsDC) {
				insertParametersDocComposition(id);
			}
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error inserting BI Object {} using template {} with load parameters equals to {}", obj,
					objTemp, loadParsDC, he);
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return idToReturn;
	}

	/**
	 * Erase bi object.
	 *
	 * @param obj     the obj
	 * @param idFunct the id funct
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#eraseBIObject(it.eng.spagobi.analiticalmodel.document.bo.BIObject, java.lang.Integer)
	 */
	@Override
	public void eraseBIObject(BIObject obj, Integer idFunct) throws EMFUserError {
		LOGGER.debug("Erasing biObject {}", obj);
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// load object
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, obj.getId());
			// erase object from functionalities
			Set<SbiObjFunc> hibObjFuncs = hibBIObject.getSbiObjFuncs();
			Iterator<SbiObjFunc> itObjFunc = hibObjFuncs.iterator();
			while (itObjFunc.hasNext()) {
				SbiObjFunc aSbiObjFunc = itObjFunc.next();
				if (idFunct == null
						|| aSbiObjFunc.getId().getSbiFunctions().getFunctId().intValue() == idFunct.intValue()) {
					LOGGER.debug("Deleting object [{}] from folder [{}]", obj.getName(),
							aSbiObjFunc.getId().getSbiFunctions().getPath());
					aSession.delete(aSbiObjFunc);
				}
			}

			aSession.flush();
			// reload object
			aSession.refresh(hibBIObject);

			// if the object is no more referenced in any folder, erases it from
			// sbi_obejcts table
			hibObjFuncs = hibBIObject.getSbiObjFuncs();
			if (hibObjFuncs == null || hibObjFuncs.isEmpty()) {

				LOGGER.debug(
						"The object [{}] is no more referenced by any functionality. It will be completely deleted from db.",
						obj.getName());

				// delete templates
				String hql = "from SbiObjTemplates sot where sot.sbiObject.biobjId=:biobjId";
				Query query = aSession.createQuery(hql);
				query.setParameter("biobjId", obj.getId());
				List<SbiObjTemplates> templs = query.list();
				Iterator<SbiObjTemplates> iterTempls = templs.iterator();
				while (iterTempls.hasNext()) {
					SbiObjTemplates hibObjTemp = iterTempls.next();
					SbiBinContents hibBinCont = hibObjTemp.getSbiBinContents();
					aSession.delete(hibObjTemp);
					aSession.delete(hibBinCont);

				}

				// delete subobjects eventually associated
				ISubObjectDAO subobjDAO = DAOFactory.getSubObjectDAO();
				List<SubObject> subobjects = subobjDAO.getSubObjects(obj.getId());
				for (int i = 0; i < subobjects.size(); i++) {
					SubObject s = subobjects.get(i);
					subobjDAO.deleteSubObjectSameConnection(s.getId(), aSession);
				}

				// delete viewpoints eventually associated
				IViewpointDAO biVPDAO = DAOFactory.getViewpointDAO();
				List<Viewpoint> viewpoints = biVPDAO.loadAllViewpointsByObjID(obj.getId());
				for (int i = 0; i < viewpoints.size(); i++) {
					Viewpoint vp = viewpoints.get(i);
					biVPDAO.eraseViewpoint(vp.getVpId());
				}

				// delete snapshots eventually associated
				ISnapshotDAO snapshotsDAO = DAOFactory.getSnapshotDAO();
				List<Snapshot> snapshots = snapshotsDAO.getSnapshots(obj.getId());
				for (int i = 0; i < snapshots.size(); i++) {
					Snapshot aSnapshots = snapshots.get(i);
					snapshotsDAO.deleteSnapshot(aSnapshots.getId());
				}

				// delete notes eventually associated
				IObjNoteDAO objNoteDAO = DAOFactory.getObjNoteDAO();
				objNoteDAO.eraseNotes(obj.getId());

				// delete metadata eventually associated
				List<ObjMetadata> metadata = DAOFactory.getObjMetadataDAO().loadAllObjMetadata();
				IObjMetacontentDAO objMetaContentDAO = DAOFactory.getObjMetacontentDAO();
				if (metadata != null && !metadata.isEmpty()) {
					Iterator<ObjMetadata> it = metadata.iterator();
					while (it.hasNext()) {
						ObjMetadata objMetadata = it.next();
						ObjMetacontent objMetacontent = DAOFactory.getObjMetacontentDAO()
								.loadObjMetacontent(objMetadata.getObjMetaId(), obj.getId(), null);
						if (objMetacontent != null) {
							objMetaContentDAO.eraseObjMetadata(objMetacontent);
						}
					}
				}

				// delete ObjDataset eventually associatyed
				DAOFactory.getBIObjDataSetDAO().eraseBIObjDataSetByObjectId(obj.getId());
				// delete ObjFunction eventually associated
				DAOFactory.getBIObjFunctionDAO().eraseBIObjFunctionByObjectId(obj.getId());

				// delete parameters associated
				// before deleting parameters associated is needed to delete all
				// dependencies,
				// otherwise in case there could be error if is firstly deleted
				// a parameter from wich some else is dependant
				// (thought priority parameter is not costraining dependencies
				// definition)

				// delete CrossNavigation
				DAOFactory.getCrossNavigationDAO().deleteByDocument(obj, aSession);
				aSession.flush();

				Set<SbiObjPar> objPars = hibBIObject.getSbiObjPars();

				Iterator<SbiObjPar> itObjParDep = objPars.iterator();
				BIObjectParameterDAOHibImpl objParDAO = new BIObjectParameterDAOHibImpl();
				while (itObjParDep.hasNext()) {
					SbiObjPar aSbiObjPar = itObjParDep.next();
					BIObjectParameter aBIObjectParameter = new BIObjectParameter();
					aBIObjectParameter.setId(aSbiObjPar.getObjParId());
					objParDAO.eraseBIObjectParameterDependencies(aBIObjectParameter, aSession);
				}

				Iterator<SbiObjPar> itObjPar = objPars.iterator();
				while (itObjPar.hasNext()) {
					SbiObjPar aSbiObjPar = itObjPar.next();
					BIObjectParameter aBIObjectParameter = new BIObjectParameter();
					aBIObjectParameter.setId(aSbiObjPar.getObjParId());

					objParDAO.eraseBIObjectParameter(aBIObjectParameter, aSession, false);
				}

				// update subreports table
				ISubreportDAO subrptdao = DAOFactory.getSubreportDAO();
				subrptdao.eraseSubreportByMasterRptId(obj.getId());
				subrptdao.eraseSubreportBySubRptId(obj.getId());

				// delete relation between document and dataset if it exists
				List<SbiMetaObjDs> lstRelObjDs = DAOFactory.getSbiObjDsDAO().loadDsByObjId(obj.getId());
				for (SbiMetaObjDs r : lstRelObjDs) {
					DAOFactory.getSbiObjDsDAO().deleteObjDs(r);
				}

				// delete SbiOutputParameters
				if (hibBIObject.getSbiOutputParameters() != null && !hibBIObject.getSbiOutputParameters().isEmpty()) {
					DAOFactory.getOutputParameterDAO().removeParametersByBiobjId(hibBIObject.getBiobjId(), aSession);
					aSession.flush();
				}

				// delete relation between document and word (ie SbiGlDocWlist)
				DAOFactory.getGlossaryDAO().deleteDocWlistByBiobjId(obj.getId(), aSession);

				// delete object
				aSession.delete(hibBIObject);

			}
			// commit all changes
			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("Error erasing BI Object {} and function id equals to {}", obj, idFunct, he);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			LOGGER.error("Error erasing BI Object {} and function id equals to {}", obj, idFunct, ex);
			rollbackIfActive(tx);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Gets the correct roles for execution.
	 *
	 * @param id      the id
	 * @param profile the profile
	 * @return the correct roles for execution
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#getCorrectRolesForExecution(java.lang.Integer, it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	public List<String> getCorrectRolesForExecution(Integer id, IEngUserProfile profile) throws EMFUserError {
		LOGGER.debug("Getting correct roles for execution on biObject with id {} and profile {}", id, profile);
		List<String> correctRoles = new ArrayList<>();
		try {
			Session aSession = null;
			Transaction tx = null;
			UserProfile profileAsUserProfile = (UserProfile) profile;
			Collection roles = profileAsUserProfile.getRolesForUse();

			try {
				aSession = getSession();
				tx = aSession.beginTransaction();

				LOGGER.debug("The user have [{}] different roles", roles.size());

				// allRolesWithPermission will store all roles with permissions on
				// folders containing the required document
				Set<String> allRolesWithPermission = new TreeSet<>();

				// first filter on roles: finds only roles with permissions on
				// folders containing the required document
				SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, id);
				String objectState = hibBIObject.getState().getValueCd();
				String permission = ObjectsAccessVerifier.getPermissionFromDocumentState(objectState);
				Set<SbiObjFunc> hibObjFuncs = hibBIObject.getSbiObjFuncs();
				Iterator<SbiObjFunc> itObjFunc = hibObjFuncs.iterator();
				while (itObjFunc.hasNext()) {
					SbiObjFunc aSbiObjFunc = itObjFunc.next();
					SbiFunctions aSbiFunctions = aSbiObjFunc.getId().getSbiFunctions();
					String funcTypeCd = aSbiFunctions.getFunctTypeCd();
					LOGGER.debug("Folder type [{}]", funcTypeCd);
					if (!funcTypeCd.equalsIgnoreCase("USER_FUNCT")) {
						LOGGER.debug("Folder id [{}]", aSbiFunctions.getFunctId());
						LOGGER.debug("Document state [{}]", objectState);

						String rolesHql = "select distinct roles.name from "
								+ "SbiExtRoles as roles, SbiFuncRole as funcRole "
								+ "where roles.extRoleId = funcRole.id.role.extRoleId and "
								+ "	   funcRole.id.function.functId = :functId " + " and "
								+ "	   funcRole.id.state.valueCd = :permission";
						Query rolesHqlQuery = aSession.createQuery(rolesHql);
						rolesHqlQuery.setParameter("functId", aSbiFunctions.getFunctId());
						rolesHqlQuery.setParameter("permission", permission);
						// get the list of roles that can see the document (in REL
						// or TEST state) in that functionality
						List rolesNames = new ArrayList();
						rolesNames = rolesHqlQuery.list();
						allRolesWithPermission.addAll(rolesNames);
					} else if (aSbiFunctions.getName().equals(profileAsUserProfile.getUserId())) {
						allRolesWithPermission.addAll(roles);
					}
				}

				allRolesWithPermission.retainAll(roles);

				LOGGER.debug("There are [{}] roles that can execut doc [{}] depending on its location",
						allRolesWithPermission.size(), id);

				Set<String> userRolesWithPermission = getUserRoles(aSession, roles, allRolesWithPermission);

				LOGGER.debug("The user have [{}] different roles that can execute doc [{}] depending on its location",
						userRolesWithPermission.size(), id);

				correctRoles = filterUsableRolesByObjectParameters(id, correctRoles, aSession, userRolesWithPermission);

				tx.rollback();
			} catch (HibernateException he) {
				rollbackIfActive(tx);
				LOGGER.error("Error getting correct roles for execution for object id {} and user profile {}", id,
						profile, he);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				closeSessionIfOpen(aSession);
			}
		} catch (EMFInternalError emfie) {
			LOGGER.error("Error getting role from the user profile", emfie);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		return correctRoles;
	}

	/**
	 * Gets the correct roles for execution.
	 *
	 * @param id the id
	 * @return the correct roles for execution
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#getCorrectRolesForExecution(java.lang.Integer)
	 */
	@Override
	public List<String> getCorrectRolesForExecution(Integer id) throws EMFUserError {
		LOGGER.debug("Getting correct roles for execution on biObject with id {}", id);
		List<Role> roles = DAOFactory.getRoleDAO().loadAllRoles();
		List<String> nameRoles = new ArrayList();
		Iterator<Role> iterRoles = roles.iterator();
		Role role = null;
		while (iterRoles.hasNext()) {
			role = iterRoles.next();
			nameRoles.add(role.getName());
		}
		return getCorrectRoles(id, nameRoles);
	}

	/**
	 * Gets a list of correct role according to the report at input, identified by its id
	 *
	 * @param id    The Integer representing report's id
	 * @param roles The collection of all roles
	 * @return The correct roles list
	 * @throws EMFUserError if any exception occurred
	 */
	private List<String> getCorrectRoles(Integer id, Collection roles) throws EMFUserError {
		LOGGER.debug("Getting correct roles on biObject with id {} giving following roles {}", id, roles);
		Session aSession = null;
		Transaction tx = null;
		List<String> correctRoles = new ArrayList<>();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			LOGGER.debug("The user have [{}] different roles", roles.size());

			// allRolesWithPermission will store all roles with permissions on
			// folders containing the required document
			Set<String> allRolesWithPermission = new TreeSet<>();

			// first filter on roles: finds only roles with permissions on
			// folders containing the required document
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, id);
			String objectState = hibBIObject.getState().getValueCd();
			String permission = ObjectsAccessVerifier.getPermissionFromDocumentState(objectState);
			Set<SbiObjFunc> hibObjFuncs = hibBIObject.getSbiObjFuncs();
			Iterator<SbiObjFunc> itObjFunc = hibObjFuncs.iterator();
			while (itObjFunc.hasNext()) {
				SbiObjFunc aSbiObjFunc = itObjFunc.next();
				SbiFunctions aSbiFunctions = aSbiObjFunc.getId().getSbiFunctions();
				String funcTypeCd = aSbiFunctions.getFunctTypeCd();
				LOGGER.debug("Folder type [{}]", funcTypeCd);
				if (!funcTypeCd.equalsIgnoreCase("USER_FUNCT")) {
					LOGGER.debug("Folder id [{}]", aSbiFunctions.getFunctId());
					LOGGER.debug("Document state [{}]", objectState);

					String rolesHql = "select distinct roles.name from "
							+ "SbiExtRoles as roles, SbiFuncRole as funcRole "
							+ "where roles.extRoleId = funcRole.id.role.extRoleId and "
							+ "	   funcRole.id.function.functId =  :functId " + " and "
							+ "	   funcRole.id.state.valueCd = :permission ";
					Query rolesHqlQuery = aSession.createQuery(rolesHql);
					rolesHqlQuery.setParameter("functId", aSbiFunctions.getFunctId());
					rolesHqlQuery.setParameter("permission", permission);
					// get the list of roles that can see the document (in REL
					// or TEST state) in that functionality
					List<String> rolesNames = new ArrayList();
					rolesNames = rolesHqlQuery.list();
					allRolesWithPermission.addAll(rolesNames);
				} else {
					List<String> l = new ArrayList<>();
					l.addAll(roles);
					return l;
				}
			}

			LOGGER.debug("There are [{}] roles that can execut doc [{}] depending on its location",
					allRolesWithPermission.size(), id);

			Set<String> userRolesWithPermission = getUserRoles(aSession, roles, allRolesWithPermission);

			LOGGER.debug("The user have [{}] different roles that can execute doc [{}] depending on its location",
					userRolesWithPermission.size(), id);

			correctRoles = filterUsableRolesByObjectParameters(id, correctRoles, aSession, userRolesWithPermission);

			tx.rollback();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error getting correct roles for execution for object id {}", id, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return correctRoles;
	}

	/**
	 * From the Hibernate BI object at input, gives the corrispondent BI object.
	 *
	 * @param hibBIObject The Hibernate BI object
	 * @return the corrispondent output <code>BIObject</code>
	 */
	@Override
	public BIObject toBIObject(SbiObjects hibBIObject, Session session) throws EMFUserError {
		LOGGER.debug("Converting {} to BIObject", hibBIObject);
		// create empty biobject
		BIObject aBIObject = new BIObject();
		// set type (type code and id)
		if (hibBIObject.getObjectTypeCode() != null) {
			aBIObject.setBiObjectTypeCode(hibBIObject.getObjectTypeCode());
		}
		if (hibBIObject.getObjectType() != null) {
			aBIObject.setBiObjectTypeID(hibBIObject.getObjectType().getValueId());
		}
		// set description
		String descr = hibBIObject.getDescr();
		if (descr == null)
			descr = "";
		aBIObject.setDescription(descr);
		// set encrypt flag
		if (hibBIObject.getEncrypt() != null) {
			aBIObject.setEncrypt(hibBIObject.getEncrypt().intValue());
		} else
			aBIObject.setEncrypt(0);

		// set visible flag
		if (hibBIObject.getVisible() != null) {
			aBIObject.setVisible(hibBIObject.getVisible().intValue());
		} else
			aBIObject.setVisible(0);

		// set profiled visibility information
		aBIObject.setProfiledVisibility(hibBIObject.getProfiledVisibility());
		// set engine
		if (hibBIObject.getSbiEngines() != null) {
			aBIObject.setEngine(new EngineDAOHibImpl().toEngine(hibBIObject.getSbiEngines()));
		}
		// set data source
		if (hibBIObject.getDataSource() != null) {
			aBIObject.setDataSourceId(hibBIObject.getDataSource().getDsId());
		}
		// if (hibBIObject.getDataSet() != null) {
		// // aBIObject.setDataSetId(new
		// Integer(hibBIObject.getDataSet().getId().getDsId()));
		// aBIObject.setDataSetId(new Integer(hibBIObject.getDataSet()));
		// }

		// set id
		aBIObject.setId(hibBIObject.getBiobjId());
		aBIObject.setLabel(hibBIObject.getLabel());
		aBIObject.setName(hibBIObject.getName());
		if (hibBIObject.getCommonInfo() != null) {
			aBIObject.setTenant(hibBIObject.getCommonInfo().getOrganization());
		}

		// set path
		aBIObject.setPath(hibBIObject.getPath());
		aBIObject.setUuid(hibBIObject.getUuid());
		aBIObject.setRelName(hibBIObject.getRelName());
		aBIObject.setStateCode(hibBIObject.getStateCode());
		if (hibBIObject.getState() != null) {
			aBIObject.setStateID(hibBIObject.getState().getValueId());
			aBIObject.setStateCodeStr(hibBIObject.getState().getValueNm());
		}

		List<Integer> functionlities = new ArrayList<>();
		boolean isPublic = false;
		if (hibBIObject.getSbiObjFuncs() != null) {
			Set<SbiObjFunc> hibObjFuncs = hibBIObject.getSbiObjFuncs();
			for (Iterator<SbiObjFunc> it = hibObjFuncs.iterator(); it.hasNext();) {
				SbiObjFunc aSbiObjFunc = it.next();
				Integer functionalityId = aSbiObjFunc.getId().getSbiFunctions().getFunctId();
				functionlities.add(functionalityId);
				if (!isPublic) { // optimization: this ensure that the following
								 // code is executed only once in the for
								 // cycle (during the second execution of the
					// cycle we already know that the document is public)
					String folderType = aSbiObjFunc.getId().getSbiFunctions().getFunctTypeCd();
					// if document belongs to another folder or the folder is
					// not a personal folder, that means it is shared
					if (it.hasNext() || folderType.equalsIgnoreCase("LOW_FUNCT")) {
						isPublic = true;
					}
				}
			}
		}

		aBIObject.setFunctionalities(functionlities);
		aBIObject.setPublicDoc(isPublic);

		List<BIObjectParameter> businessObjectParameters = new ArrayList<>();
		Set<SbiObjPar> hibObjPars = hibBIObject.getSbiObjPars();
		if (hibObjPars != null) {
			for (Iterator<SbiObjPar> it = hibObjPars.iterator(); it.hasNext();) {
				SbiObjPar aSbiObjPar = it.next();
				BIObjectParameter par = toBIObjectParameter(aSbiObjPar);
				businessObjectParameters.add(par);
			}
			aBIObject.setDrivers(businessObjectParameters);
		}

		List<OutputParameter> businessObjectOutputParameters = new ArrayList<>();
		Set<SbiOutputParameter> hibObjOutPars = hibBIObject.getSbiOutputParameters();
		if (hibObjOutPars != null) {
			for (Iterator<SbiOutputParameter> it = hibObjOutPars.iterator(); it.hasNext();) {
				SbiOutputParameter aSbiOutPar = it.next();
				OutputParameter opar = toOutputParameter(aSbiOutPar);
				businessObjectOutputParameters.add(opar);
			}
			aBIObject.setOutputParameters(businessObjectOutputParameters);
		}

		aBIObject.setCreationDate(hibBIObject.getCreationDate());
		aBIObject.setCreationUser(hibBIObject.getCreationUser());

		aBIObject.setRefreshSeconds(hibBIObject.getRefreshSeconds());
		aBIObject.setPreviewFile(hibBIObject.getPreviewFile());

		String region = hibBIObject.getParametersRegion();
		if (region == null) {
			try {
				IConfigDAO configDAO = DAOFactory.getSbiConfigDAO();
				Config defaultRegionConfig = configDAO
						.loadConfigParametersByLabel("SPAGOBI.DOCUMENTS.PARAMETERS_REGION_DEFAULT");
				if (defaultRegionConfig != null) {
					region = defaultRegionConfig.getValueCheck();
					LOGGER.debug("Default parameters region is {}", region);
					if (region == null || region.equals("")) {
						LOGGER.warn("Default parameters region not set in configs, put default to east");
						region = "east";
					} else {
						// if default region is top or north becomes north, east
						// or right becomes right
						region = region.equalsIgnoreCase("top") || region.equalsIgnoreCase("north") ? "north" : "east";
					}

				} else {
					region = "east";
					LOGGER.warn("Default parameters region not set in configs, put default to east");
				}
			} catch (Exception e) {
				LOGGER.error(
						"Error during recovery of default parameters region setting: go on with default east value", e);
			}
		}

		aBIObject.setParametersRegion(region);
		aBIObject.setLockedByUser(hibBIObject.getLockedByUser());
		// put dataset
		if (session != null) {
			BIObjDataSet biObjDataSet = DAOFactory.getBIObjDataSetDAO().getObjectDetailDataset(aBIObject.getId(),
					session);
			if (biObjDataSet != null) {
				LOGGER.debug("Associate dataset with id {}", biObjDataSet.getDataSetId());
				aBIObject.setDataSetId(biObjDataSet.getDataSetId());
			}
		}
		return aBIObject;
	}

	/**
	 * From the hibernate BI object parameter at input, gives the corrispondent <code>BIObjectParameter</code> object.
	 *
	 * @param hiObjPar The hybernate BI object parameter
	 * @return The corrispondent <code>BIObjectParameter</code>
	 */
	public BIObjectParameter toBIObjectParameter(SbiObjPar hiObjPar) {
		BIObjectParameter aBIObjectParameter = new BIObjectParameter();
		aBIObjectParameter.setId(hiObjPar.getObjParId());
		aBIObjectParameter.setLabel(hiObjPar.getLabel());
		aBIObjectParameter.setModifiable(hiObjPar.getModFl().intValue());
		aBIObjectParameter.setMultivalue(hiObjPar.getMultFl().intValue());
		aBIObjectParameter.setBiObjectID(hiObjPar.getSbiObject().getBiobjId());
		aBIObjectParameter.setParameterUrlName(hiObjPar.getParurlNm());
		aBIObjectParameter.setParID(hiObjPar.getSbiParameter().getParId());
		aBIObjectParameter.setRequired(hiObjPar.getReqFl().intValue());
		aBIObjectParameter.setVisible(hiObjPar.getViewFl().intValue());
		aBIObjectParameter.setPriority(hiObjPar.getPriority());
		aBIObjectParameter.setProg(hiObjPar.getProg());
		Parameter parameter = new Parameter();
		parameter.setId(hiObjPar.getSbiParameter().getParId());
		parameter.setType(hiObjPar.getSbiParameter().getParameterTypeCode());
		aBIObjectParameter.setParameter(parameter);
		return aBIObjectParameter;
	}

	public OutputParameter toOutputParameter(SbiOutputParameter hiObjPar) {
		OutputParameter outp = new OutputParameter();
		outp.setId(hiObjPar.getId());
		outp.setName(hiObjPar.getLabel());
		outp.setType(from(hiObjPar.getParameterType()));
		outp.setFormatCode(hiObjPar.getFormatCode());
		outp.setFormatValue(hiObjPar.getFormatValue());
		outp.setBiObjectId(hiObjPar.getBiobjId());
		outp.setIsUserDefined(hiObjPar.getIsUserDefined());

		return outp;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadAllBIObjects ()
	 */
	@Override
	public List<BIObject> loadAllBIObjects() throws EMFUserError {
		LOGGER.debug("Loading all biObjects");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiObjects s order by s.label");
			List<SbiObjects> hibList = hibQuery.list();
			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject(it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading all BI Objects", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public List<BIObject> loadAllBIObjectsByFolderId(final Integer folderId) throws EMFUserError {
		LOGGER.debug("Loading all biObjects in folder with id {}", folderId);

		List<BIObject> realResult = executeOnTransaction(new IExecuteOnTransaction<List>() {

			@Override
			public List execute(Session session) throws Exception {
				List<SbiObjects> sbiObjects = session.createCriteria(SbiObjects.class)
						.createAlias("sbiObjFuncs", "_sbiObjFunc").createAlias("state", "_sbiDomain")
						.add(Restrictions.eq("_sbiObjFunc.id.sbiFunctions.functId", folderId))
						.setProjection(Projections.projectionList().add(Property.forName("biobjId").as("biobjId"))
								.add(Property.forName("name").as("name"))
								.add(Property.forName("creationUser").as("creationUser"))
								.add(Property.forName("creationDate").as("creationDate"))
								.add(Property.forName("objectTypeCode").as("objectTypeCode"))
								.add(Property.forName("descr").as("descr"))
								.add(Property.forName("stateCode").as("stateCode"))
								.add(Property.forName("sbiEngines").as("sbiEngines"))
								.add(Property.forName("label").as("label")).add(Property.forName("state").as("state"))
								.add(Property.forName("visible").as("visible"))
								.add(Property.forName("profiledVisibility").as("profiledVisibility"))
								.add(Property.forName("previewFile").as("previewFile")))
						.setResultTransformer(Transformers.aliasToBean(SbiObjects.class)).list();

				Iterator<SbiObjects> it = sbiObjects.iterator();
				List<BIObject> realResult = new ArrayList<>();
				SbiFunctions functions = (SbiFunctions) session.load(SbiFunctions.class, folderId);
				Set<SbiObjFunc> sbiObjFuns = new HashSet<>();
				SbiObjFunc sbiObjFun = new SbiObjFunc();
				SbiObjFuncId funcId = new SbiObjFuncId();
				funcId.setSbiFunctions(functions);
				sbiObjFun.setId(funcId);
				sbiObjFuns.add(sbiObjFun);
				while (it.hasNext()) {
					SbiObjects sbiObj = it.next();
					sbiObj.setSbiObjFuncs(sbiObjFuns);
					realResult.add(toBIObject(sbiObj, session));
				}
				return realResult;

			}

		});

		return realResult;

	}

	@Override
	public List<BIObject> loadDocumentsBeforeDate(String data) throws EMFUserError {
		LOGGER.debug("Loading all biObjects before date {}", data);
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date selectedDate;
			try {
				selectedDate = df.parse(data);
			} catch (ParseException e) {
				LOGGER.error("Error in parsing date {}", data);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}

			Criteria criteria = aSession.createCriteria(SbiObjTemplates.class)
					.add(Restrictions.le("creationDate", selectedDate)).add(Restrictions.eq("active", Boolean.FALSE))
					.setProjection(Property.forName("sbiObject"));
			List<SbiObjects> hibList = criteria.list();

			Iterator<SbiObjects> it = hibList.iterator();
			ArrayList<String> labelsInserted = new ArrayList<>();
			while (it.hasNext()) {
				SbiObjects obj = it.next();
				String label = obj.getLabel();
				if (!labelsInserted.contains(label)) {
					realResult.add(toBIObject(obj, aSession));
				}
				labelsInserted.add(label);
			}

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading documents before date {}", data, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadAllBIObjects (java.lang.String)
	 */
	@Override
	public List<BIObject> loadAllBIObjects(String filterOrder) throws EMFUserError {
		LOGGER.debug("Loading all biObjects matching filter {}", filterOrder);
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("from SbiObjects s  order by :filterOrder");
			hibQuery.setParameter("filterOrder", "s." + filterOrder);
			List<SbiObjects> hibList = hibQuery.list();

			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject(it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading all BI Objects with following filter: {}", filterOrder, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadAllBIObjects ()
	 */
	@Override
	public List<BIObject> loadPaginatedSearchBIObjects(Integer page, Integer itemPerPage,
			Collection<CriteriaParameter> disjunctions, Collection<CriteriaParameter> restrictions)
			throws EMFUserError {
		LOGGER.debug("Loading paginated biObjects for page {}, item count {}", page, itemPerPage);
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria hibQuery = aSession.createCriteria(SbiObjects.class);
			if (disjunctions != null && !disjunctions.isEmpty()) {
				Disjunction disjunction = Restrictions.disjunction();
				for (CriteriaParameter cp : disjunctions) {
					disjunction.add(cp.toHibernateCriterion());
				}
				hibQuery.add(disjunction);
			}
			if (restrictions != null) {
				for (CriteriaParameter cp : restrictions) {
					hibQuery.add(cp.toHibernateCriterion());
				}
			}
			hibQuery.setProjection(Property.forName("biobjId"));
			hibQuery.addOrder(Order.asc("label"));

			if (page != null && page > 1) {
				hibQuery.setFirstResult((page - 1) * itemPerPage);
			}

			if (itemPerPage != null) {
				hibQuery.setMaxResults(itemPerPage);
			}

			List ids = hibQuery.list();
			if (!ids.isEmpty()) {
				Criteria mainC = aSession.createCriteria(SbiObjects.class);
				mainC.add(Restrictions.in("biobjId", ids));
				mainC.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

				List<SbiObjects> lso = mainC.list();
				for (SbiObjects so : lso) {
					realResult.add(toBIObject(so, aSession));
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading paginated search for BI Objects", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/**
	 * Gets the biparameters associated with to a biobject.
	 *
	 * @param aBIObject BIObject the biobject to analize
	 * @return List, list of the biparameters associated with the biobject
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<BIObjectParameter> getBIObjectParameters(BIObject aBIObject) throws EMFUserError {
		IBIObjectParameterDAO biobjDAO = DAOFactory.getBIObjectParameterDAO();
		return biobjDAO.loadBIObjectParametersById(aBIObject.getId());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO# loadAllBIObjectsFromInitialPath(java.lang.String)
	 */
	@Override
	public List<BIObject> loadAllBIObjectsFromInitialPath(String initialPath) throws EMFUserError {
		LOGGER.debug("Loading all biObjects from initial path {}", initialPath);
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(
					"select distinct(objects) from SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions where objects.biobjId = objFuncs.id.sbiObjects.biobjId and objFuncs.id.sbiFunctions.functId = functions.functId and (functions.path = ? or functions.path like ?) order by objects.label");

			hibQuery.setString(0, initialPath);
			hibQuery.setString(1, initialPath + "%");
			List<SbiObjects> hibList = hibQuery.list();

			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject(it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading all BI Objects from initial path {}", initialPath, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO# loadAllBIObjectsFromInitialPath(java.lang.String, java.lang.String)
	 */
	@Override
	public List<BIObject> loadAllBIObjectsFromInitialPath(String initialPath, String filterOrder) throws EMFUserError {
		LOGGER.debug("Loading all biObjects from initial path {} and filter order {}", initialPath, filterOrder);
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(
					"select distinct(objects) from SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions where objects.biobjId = objFuncs.id.sbiObjects.biobjId and objFuncs.id.sbiFunctions.functId = functions.functId and (functions.path = ? or functions.path like ? order by ?");
			hibQuery.setString(0, initialPath);
			hibQuery.setString(1, initialPath + "%");
			hibQuery.setString(2, "objects." + filterOrder);

			List<SbiObjects> hibList = hibQuery.list();
			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject(it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading all BI Objects from initial path {} and filter {}", initialPath, filterOrder,
					he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO# loadBIObjectForDetail(java.lang.String)
	 */
	@Override
	public BIObject loadBIObjectForDetail(String path) throws EMFUserError {
		LOGGER.debug("Loading biObject for detail from path {}", path);
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiObjects where path = ?";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setSerializable(0, path);

			SbiObjects hibObject = (SbiObjects) hqlQuery.uniqueResult();
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				LOGGER.warn("Unable to load document whose path is equal to [{}]", path);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Object for detail in path {}", path, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return biObject;
	}

	/**
	 * Called only for document composition (update object modality). Puts parameters into the document composition getting these from document's children.
	 *
	 * @param aSession  the hibernate session
	 * @param biObject  the BI object of document composition
	 * @param template  the BI last active template
	 * @param flgDelete the flag that suggest if is necessary to delete parameters before the insertion
	 * @throws EMFUserError
	 */
	private void insertParametersDocComposition(BIObject biObject, ObjTemplate template, boolean flgDelete)
			throws EMFUserError {
		LOGGER.debug("Inserting parameter doc composition for biObject with id {} with template {}", biObject,
				template);
		Session aSession = null;
		Transaction tx = null;
		// get informations about documents child
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// gets document composition configuration
			if (template == null)
				return;
			byte[] contentBytes = template.getContent();
			String contentStr = new String(contentBytes);
			SourceBean content = SourceBean.fromXMLString(contentStr);
			DocumentCompositionUtil docConf = new DocumentCompositionUtil(content);
			List lstLabeldDocs = docConf.getSbiObjLabelsArray();
			List totalParameters = new ArrayList();

			// if flag flgDelete is true delete all parameters associated to
			// document composition
			if (flgDelete) {
				List<BIObjectParameter> lstDocParameters = DAOFactory.getBIObjectParameterDAO()
						.loadBIObjectParametersById(biObject.getId());
				for (int i = 0; i < lstDocParameters.size(); i++) {
					BIObjectParameter docParam = lstDocParameters.get(i);
					SbiObjects aSbiObject = new SbiObjects();
					aSbiObject.setBiobjId(biObject.getId());

					SbiParameters aSbiParameter = new SbiParameters();
					aSbiParameter.setParId(docParam.getParameter().getId());

					SbiObjPar hibObjPar = new SbiObjPar();
					hibObjPar.setObjParId(docParam.getId());
					hibObjPar.setLabel(docParam.getLabel());

					hibObjPar.setSbiObject(aSbiObject);
					hibObjPar.setSbiParameter(aSbiParameter);

					aSession.delete(hibObjPar);
				}
			}

			// for every document child gets parameters and inserts these into
			// new document composition object
			for (int i = 0; i < lstLabeldDocs.size(); i++) {
				BIObject docChild = loadBIObjectByLabel((String) lstLabeldDocs.get(i));

				if (docChild == null) {
					LOGGER.error("Error while getting document child {} for document composition.",
							lstLabeldDocs.get(i));
					List lstLabel = new ArrayList();
					lstLabel.add(lstLabeldDocs.get(i));
					throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", lstLabel,
							"component_spagobidocumentcompositionIE_messages");
				} else {
					List<BIObjectParameter> lstDocChildParameters = DAOFactory.getBIObjectParameterDAO()
							.loadBIObjectParametersById(docChild.getId());
					for (int j = 0; j < lstDocChildParameters.size(); j++) {
						BIObjectParameter objPar = lstDocChildParameters.get(j);
						if (!totalParameters.contains(objPar.getLabel())) {
							SbiObjects aSbiObject = new SbiObjects();
							Integer objId = biObject.getId();
							if (objId == null || objId.compareTo(new Integer("0")) == 0)
								objId = biObject.getId();
							aSbiObject.setBiobjId(objId);

							SbiParameters aSbiParameter = new SbiParameters();
							aSbiParameter.setParId(objPar.getParID());
							SbiObjPar sbiObjPar = new SbiObjPar();
							sbiObjPar.setSbiObject(aSbiObject);
							sbiObjPar.setSbiParameter(aSbiParameter);
							sbiObjPar.setObjParId(new Integer("-1"));
							sbiObjPar.setLabel(objPar.getLabel());
							sbiObjPar.setParurlNm(objPar.getParameterUrlName());
							sbiObjPar.setReqFl(objPar.getRequired().shortValue());
							sbiObjPar.setModFl(objPar.getModifiable().shortValue());
							sbiObjPar.setViewFl(objPar.getVisible().shortValue());
							sbiObjPar.setMultFl(objPar.getMultivalue().shortValue());
							sbiObjPar.setProg(objPar.getProg());
							sbiObjPar.setPriority(totalParameters.size() + 1);
							sbiObjPar.setColSpan(objPar.getColSpan());
							sbiObjPar.setThickPerc(objPar.getThickPerc());
							updateSbiCommonInfo4Insert(sbiObjPar);
							aSession.save(sbiObjPar);
							totalParameters.add(objPar.getLabel());
						}
					}
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error(
					"Error inserting parameters in doc composition for BI Object {}, Obj Template {} and flag delete equals to {}",
					biObject, template, flgDelete, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (EMFUserError eu) {
			throw eu;
		} catch (Exception e) {
			LOGGER.error("Error while creating parameter for document composition.", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	/**
	 * Called only for document composition (insert object modality). Puts parameters into the document composition getting these from document's children.
	 *
	 * @param biobjectId the document composition biobject id
	 * @throws EMFUserError
	 */
	private void insertParametersDocComposition(Integer biobjectId) throws EMFUserError {
		LOGGER.debug("Inserting parameter doc composition for biObject with id {}", biobjectId);
		// get informations about documents child
		Session aSession = null;
		Transaction tx = null;
		try {
			// gets document composition configuration
			ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobjectId);
			aSession = getSession();
			tx = aSession.beginTransaction();
			LOGGER.debug("Template document composition in insert: {}", template);
			if (template == null)
				return;
			byte[] contentBytes = template.getContent();
			String contentStr = new String(contentBytes);
			SourceBean content = SourceBean.fromXMLString(contentStr);
			DocumentCompositionUtil docConf = new DocumentCompositionUtil(content);
			List lstLabeldDocs = docConf.getSbiObjLabelsArray();
			List totalParameters = new ArrayList();

			// for every document child gets parameters and inserts these into
			// new document composition object
			for (int i = 0; i < lstLabeldDocs.size(); i++) {
				BIObject docChild = loadBIObjectByLabel((String) lstLabeldDocs.get(i));
				if (docChild == null) {
					LOGGER.error("Error while getting document child {} for document composition.",
							lstLabeldDocs.get(i));
					List lstLabel = new ArrayList();
					lstLabel.add(lstLabeldDocs.get(i));
					throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", lstLabel,
							"component_spagobidocumentcompositionIE");
				} else {
					List<BIObjectParameter> lstDocChildParameters = DAOFactory.getBIObjectParameterDAO()
							.loadBIObjectParametersById(docChild.getId());
					for (int j = 0; j < lstDocChildParameters.size(); j++) {
						BIObjectParameter objPar = lstDocChildParameters.get(j);
						if (!totalParameters.contains(objPar.getLabel())) {
							SbiObjects aSbiObject = new SbiObjects();
							Integer objId = biobjectId;
							if (objId == null || objId.compareTo(new Integer("0")) == 0)
								objId = biobjectId;
							aSbiObject.setBiobjId(objId);

							SbiParameters aSbiParameter = new SbiParameters();
							aSbiParameter.setParId(objPar.getParID());
							SbiObjPar sbiObjPar = new SbiObjPar();
							sbiObjPar.setSbiObject(aSbiObject);
							sbiObjPar.setSbiParameter(aSbiParameter);
							sbiObjPar.setObjParId(new Integer("-1"));
							sbiObjPar.setLabel(objPar.getLabel());
							sbiObjPar.setParurlNm(objPar.getParameterUrlName());
							sbiObjPar.setReqFl(objPar.getRequired().shortValue());
							sbiObjPar.setModFl(objPar.getModifiable().shortValue());
							sbiObjPar.setViewFl(objPar.getVisible().shortValue());
							sbiObjPar.setMultFl(objPar.getMultivalue().shortValue());
							sbiObjPar.setColSpan(objPar.getColSpan());
							sbiObjPar.setThickPerc(objPar.getThickPerc());
							sbiObjPar.setProg(objPar.getProg());
							sbiObjPar.setPriority(totalParameters.size() + 1);
							updateSbiCommonInfo4Insert(sbiObjPar);
							aSession.save(sbiObjPar);
							totalParameters.add(objPar.getLabel());
						}
					}
				}
			}
			tx.commit();
		} catch (EMFUserError e) {
			throw e;
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error inserting parameters in doc composition for BI Object id {}", biobjectId, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			LOGGER.error("Error while creating parameter for document composition.", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
	}

	@Override
	public List<BIObject> loadBIObjects(String type, String state, String folderPath) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			StringBuilder buffer = new StringBuilder();
			if (folderPath != null) {
				buffer.append(
						"select distinct(objects) from SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions "
								+ "where objects.biobjId = objFuncs.id.sbiObjects.biobjId and objFuncs.id.sbiFunctions.functId = functions.functId "
								+ "and functions.path = :PATH and ");
			} else {
				buffer.append("select objects from SbiObjects as objects where ");
			}
			if (state != null) {
				buffer.append(" objects.stateCode = :STATE and ");
			}
			if (type != null) {
				buffer.append(" objects.objectTypeCode = :TYPE");
			}
			String hql = buffer.toString();
			if (hql.endsWith(" and ")) {
				hql = hql.substring(0, hql.length() - " and ".length());
			}
			if (hql.endsWith(" where ")) {
				hql = hql.substring(0, hql.length() - " where ".length());
			}

			Query query = aSession.createQuery(hql);
			if (folderPath != null) {
				query.setParameter("PATH", folderPath);
			}
			if (state != null) {
				query.setParameter("STATE", state);
			}
			if (type != null) {
				query.setParameter("TYPE", type);
			}

			List<SbiObjects> hibList = query.list();
			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				SbiObjects object = it.next();
				realResult.add(toBIObject(object, aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Objects by type {}, state {} and folder {}", type, state, folderPath, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	/**
	 * Loads visible objects of the user roles
	 *
	 * @param folderID
	 * @param profile  the profile of the user
	 * @return
	 * @throws EMFUserError
	 */
	@Override
	public List<BIObject> loadBIObjects(Integer folderID, IEngUserProfile profile, boolean isPersonalFolder)
			throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			StringBuilder buffer = new StringBuilder();
			Collection roles = null;

			if (!isPersonalFolder) {

				try {
					if (profile != null)
						roles = ((UserProfile) profile).getRolesForUse();
				} catch (Exception e) {
					LOGGER.error("Error while recovering user profile", e);
				}

				if (folderID != null && roles != null && !roles.isEmpty()) {
					buffer.append(
							"select distinct o from SbiObjects o, SbiObjFunc sof, SbiFunctions f,  SbiFuncRole fr "
									+ "where sof.id.sbiFunctions.functId = f.functId and o.biobjId = sof.id.sbiObjects.biobjId  "
									+ " and fr.id.role.extRoleId IN (select extRoleId from SbiExtRoles e  where  e.name in (:ROLES)) "
									+ " and fr.id.function.functId = f.functId " + " and f.functId = :FOLDER_ID  ");

					if (profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
						buffer.append(
								" and (" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP
										+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_DEV + "') OR"
										+ "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST
										+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_TEST + "') OR "
										+ "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE
										+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_REL + "') OR "
										+ "o.stateCode = '" + SpagoBIConstants.DOC_STATE_SUSP + "'" + ") ");
					} else {
						buffer.append(
								" and (" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP
										+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_DEV + "') OR"
										+ "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST
										+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_TEST + "') OR "
										+ "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE
										+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_REL + "')" + ") ");
					}

					if (!profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)
							&& !profile
									.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV)) {
						// only visible objects (1 means true) and object
						// created by the current user
						buffer.append(" and ((o.visible = 0 and o.creationUser = '"
								+ ((UserProfile) profile).getUserId() + "') OR (o.visible = 1)) ");
					}
					buffer.append(" order by o.name");
				} else {
					buffer.append("select objects from SbiObjects");
				}
			} else {
				if (folderID != null) {
					buffer.append("select distinct o from SbiObjects o, SbiObjFunc sof, SbiFunctions f "
							+ "where sof.id.sbiFunctions.functId = f.functId and o.biobjId = sof.id.sbiObjects.biobjId  "
							+ " and f.functId = :FOLDER_ID  ");

					if (!profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)
							&& !profile
									.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV)) {
						// only visible objects (1 means true) and object
						// created by the current user
						buffer.append(" and ((o.visible = 0 and o.creationUser = '"
								+ ((UserProfile) profile).getUserId() + "') OR (o.visible = 1)) ");
					}
					buffer.append(" order by o.name");
				}
			}

			String hql = buffer.toString();
			Query query = aSession.createQuery(hql);

			if (!isPersonalFolder) {
				if (folderID != null && roles != null && !roles.isEmpty()) {
					query.setInteger("FOLDER_ID", folderID.intValue());
					query.setParameterList("ROLES", roles);
				}
			} else {
				if (folderID != null) {
					query.setInteger("FOLDER_ID", folderID.intValue());
				}
			}

			List<SbiObjects> hibList = query.list();
			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				SbiObjects object = it.next();
				realResult.add(toBIObject(object, aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error(
					"Error loading BI Objects for folder id {}, suer profile {} and is personal folder equals to {}",
					folderID, profile, isPersonalFolder, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			rollbackIfActive(tx);
			LOGGER.error(
					"Error loading BI Objects for folder id {}, suer profile {} and is personal folder equals to {}",
					folderID, profile, isPersonalFolder, e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public List<BIObject> loadBIObjectsByLovId(Integer idLov) throws EMFUserError {

		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(
					"select distinct obj from   SbiObjects as obj " + "inner join obj.sbiObjPars as objPars "
							+ "inner join objPars.sbiParameter as param " + "inner join param.sbiParuses as paruses "
							+ "inner join paruses.sbiLov as lov " + "where  lov.lovId = :idLov");
			hibQuery.setParameter("idLov", idLov);
			List<SbiObjects> hibList = hibQuery.list();

			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject(it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Objects by lov id {}", idLov, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
		return realResult;

	}

	@Override
	public List<BIObject> loadBIObjectsByParamterId(Integer idParameter) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(
					"select distinct obj from   SbiObjects as obj " + "inner join obj.sbiObjPars as objPars "
							+ "inner join objPars.sbiParameter as param " + "where  param.parId = :idParameter");
			hibQuery.setParameter("idParameter", idParameter);
			List<SbiObjects> hibList = hibQuery.list();

			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject(it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading BI Objects by paramter id {}", idParameter, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
		return realResult;
	}

	@Override
	public List<BIObject> loadAllBIObjectsBySearchKey(String searchKey, String attributes) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> result = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria hibQuery = aSession.createCriteria(SbiObjects.class);

			Criterion labelCriterion = EscapedLikeRestrictions.ilikeEscaped("label", searchKey, MatchMode.ANYWHERE);
			Criterion nameCriterion = EscapedLikeRestrictions.ilikeEscaped("name", searchKey, MatchMode.ANYWHERE);
			Criterion descrCriterion = EscapedLikeRestrictions.ilikeEscaped("descr", searchKey, MatchMode.ANYWHERE);

			if ("LABEL".equalsIgnoreCase(attributes)) {
				hibQuery.add(labelCriterion);
			} else if ("NAME".equalsIgnoreCase(attributes)) {
				hibQuery.add(nameCriterion);
			} else if ("DESCRIPTION".equalsIgnoreCase(attributes)) {
				hibQuery.add(descrCriterion);
			} else {
				Disjunction disjunction = Restrictions.disjunction();
				disjunction.add(labelCriterion);
				disjunction.add(nameCriterion);
				disjunction.add(descrCriterion);
				hibQuery.add(disjunction);
			}

			List<SbiObjects> hibList = hibQuery.list();
			Iterator<SbiObjects> it = hibList.iterator();
			Set<Integer> resultIds = new HashSet<>();
			while (it.hasNext()) {
				SbiObjects next = it.next();
				Integer id = next.getBiobjId();
				if (!resultIds.contains(id)) {
					resultIds.add(id);
					result.add(toBIObject(next, aSession));
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading all BI Objects by search key {} and attributes {}", searchKey, attributes, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
			LOGGER.debug("OUT");
		}
		return result;
	}

	/**
	 * Search objects with the features specified
	 *
	 * @param valueFilter  the value of the filter for the research
	 * @param typeFilter   the type of the filter (the operator: equals, starts...)
	 * @param columnFilter the column on which the filter is applied
	 * @param nodeFilter   the node (folder id) on which the filter is applied
	 * @param profile      the profile of the user
	 * @return
	 * @throws EMFUserError
	 */
	@Override
	public List<BIObject> searchBIObjects(String valueFilter, String typeFilter, String columnFilter, String scope,
			Integer nodeFilter, IEngUserProfile profile) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<BIObject> realResult = new ArrayList<>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Collection roles = null;
			try {
				roles = ((UserProfile) profile).getRolesForUse();
				LOGGER.debug("Profile roles: {}", roles);

			} catch (Exception e) {
				LOGGER.error("Error while recovering user profile", e);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1084);
			}

			StringBuilder bufferSelect = new StringBuilder();
			StringBuilder bufferFrom = new StringBuilder();
			StringBuilder bufferWhere = new StringBuilder();
			StringBuilder bufferOrder = new StringBuilder();

			// definition of the the search query
			if (roles != null && !roles.isEmpty()) {
				bufferSelect.append(" select distinct o ");
				bufferFrom.append(" from SbiObjects as o, SbiObjFunc as sof, SbiFunctions as f,  SbiFuncRole as fr ");
				bufferWhere.append(
						" where sof.id.sbiFunctions.functId = f.functId and o.biobjId = sof.id.sbiObjects.biobjId and "
								+ " ((fr.id.role.extRoleId IN (select extRoleId from SbiExtRoles e  where  e.name in (:ROLES)) "
								+ " and fr.id.function.functId = f.functId and (" + "(fr.id.state.valueCd = '"
								+ SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP + "' AND o.state.valueCd = '"
								+ SpagoBIConstants.DOC_STATE_DEV + "') OR" + "(fr.id.state.valueCd = '"
								+ SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST + "' AND o.state.valueCd = '"
								+ SpagoBIConstants.DOC_STATE_TEST + "') OR " + "(fr.id.state.valueCd = '"
								+ SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE + "' AND o.state.valueCd = '"
								+ SpagoBIConstants.DOC_STATE_REL + "') "
								+ ") ) OR  f.path like :PERSONAL_FOLDER_PREFIX )");

			}
			String operCondition = "";
			String likeStart = "";
			String likeEnd = "";
			if (valueFilter != null && !valueFilter.equals("") && typeFilter != null && !typeFilter.equals("")
					&& columnFilter != null && !columnFilter.equals("")) {
				// defines correct logical operator
				if (typeFilter.equalsIgnoreCase(START_WITH)) {
					operCondition = " like :VALUE_FILTER";
					likeStart = "";
					likeEnd = "%";
				} else if (typeFilter.equalsIgnoreCase(END_WITH)) {
					operCondition = " like :VALUE_FILTER";
					likeStart = "%";
					likeEnd = "";
				} else if (EQUALS_TO.equalsIgnoreCase(typeFilter)) {
					operCondition = " = :VALUE_FILTER";
				} else if (NOT_EQUALS_TO.equalsIgnoreCase(typeFilter)) {
					operCondition = " != :VALUE_FILTER";
				} else if (GREATER_THAN.equalsIgnoreCase(typeFilter)) {
					operCondition = " > :VALUE_FILTER";
				} else if (LESS_THAN.equalsIgnoreCase(typeFilter)) {
					operCondition = " < :VALUE_FILTER";
				} else if (CONTAINS.equalsIgnoreCase(typeFilter)) {
					operCondition = " like :VALUE_FILTER";
					likeStart = "%";
					likeEnd = "%";
				} else if (EQUALS_OR_LESS_THAN.equalsIgnoreCase(typeFilter)) {
					operCondition = " <= :VALUE_FILTER";
				} else if (EQUALS_OR_GREATER_THAN.equalsIgnoreCase(typeFilter)) {
					operCondition = " >= :VALUE_FILTER";
					/*
					 * }else if (NOT_ENDS_WITH.equalsIgnoreCase( typeFilter )) { operCondition = "NOT LIKE %:VALUE_FILTER"; } else if (NOT_CONTAINS.equalsIgnoreCase( typeFilter ))
					 * { operCondition = "NOT LIKE %:VALUE_FILTER%"; } else if (IS_NULL.equalsIgnoreCase( typeFilter )) { operCondition = "IS NULL"; } else if
					 * (NOT_NULL.equalsIgnoreCase( typeFilter )) { operCondition = "IS NOT NULL";
					 */
				} else {
					LOGGER.error("The query Operator {} is invalid.", typeFilter);
					throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
				}

				if (columnFilter.equalsIgnoreCase(COLUMN_LABEL)) {
					bufferWhere.append(" and o.label " + operCondition);
				}
				if (columnFilter.equalsIgnoreCase(COLUMN_NAME)) {
					bufferWhere.append(" and o.name " + operCondition);
				}
				if (columnFilter.equalsIgnoreCase(COLUMN_ENGINE)) {
					bufferFrom.append(", SbiEngines e ");
					bufferWhere.append(" and e.engineId = o.sbiEngines and e.name " + operCondition);
				}

				if (columnFilter.equalsIgnoreCase(COLUMN_STATE)) {
					bufferFrom.append(", SbiDomains d ");
					bufferWhere.append(" and d.valueId = o.state and d.valueCd " + operCondition);
				}

				if (columnFilter.equalsIgnoreCase(COLUMN_TYPE)) {
					bufferFrom.append(", SbiDomains d ");
					bufferWhere.append(" and d.valueId = o.objectType and d.valueCd " + operCondition);
				}

				if (columnFilter != null && columnFilter.equalsIgnoreCase(COLUMN_DATE)) {
					bufferWhere.append(" and convert(o.creationDate, DATE) " + operCondition);
				}

			}

			if (scope != null && scope.equals(SCOPE_NODE) && nodeFilter != null && !nodeFilter.equals("")) {
				bufferWhere.append(" and (f.functId = :FOLDER_ID or f.parentFunct = :FOLDER_ID) ");
			}

			bufferOrder.append(" order by o.name");

			String hql = bufferSelect.toString().concat(bufferFrom.toString()).concat(bufferWhere.toString())
					.concat(bufferOrder.toString());

			LOGGER.debug("Query hql: {}", hql);

			Query query = aSession.createQuery(hql);

			query.setParameter("PERSONAL_FOLDER_PREFIX", "/" + ((UserProfile) profile).getUserId().toString() + "%");

			// setting query parameters
			query.setParameterList("ROLES", roles);
			LOGGER.debug("Parameter value ROLES: {}", roles);

			if (valueFilter != null) {
				if (!likeStart.equals("") || !likeEnd.equals("")) {
					query.setParameter("VALUE_FILTER", likeStart + valueFilter + likeEnd);
					LOGGER.debug("Parameter value VALUE_FILTER: {}{}{}", likeStart, valueFilter, likeEnd);
				} else {
					query.setParameter("VALUE_FILTER", valueFilter);
					LOGGER.debug("Parameter value VALUE_FILTER: {}", valueFilter);
				}
			}

			if (scope != null && scope.equals("node") && nodeFilter != null && !nodeFilter.equals("")) {
				query.setParameter("FOLDER_ID", nodeFilter);
				LOGGER.debug("Parameter value FOLDER_ID: {}", nodeFilter);
			}
			// executes query
			List<SbiObjects> hibList = query.list();
			Iterator<SbiObjects> it = hibList.iterator();
			while (it.hasNext()) {
				SbiObjects object = it.next();
				realResult.add(toBIObject(object, aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error(
					"Error searching BI Objects by value filter {}, type filter {}, column filter {}, scope {}, node filter {} and profile {}",
					valueFilter, typeFilter, columnFilter, scope, nodeFilter, profile, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			rollbackIfActive(tx);
			LOGGER.error(
					"Error searching BI Objects by value filter {}, type filter {}, column filter {}, scope {}, node filter {} and profile {}",
					valueFilter, typeFilter, columnFilter, scope, nodeFilter, profile, e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return realResult;
	}

	@Override
	public Integer countBIObjects(String search, String user) throws EMFUserError {
		LOGGER.debug("Counting biObjects matching search {} and user {}", search, user);
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiObjects ";

			if (search != null || user != null) {
				hql += " where ";

				if (search != null) {
					hql += " label like :search ";
				}

			}

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setParameter("search", "%" + search + "%");
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = temp.intValue();

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error while loading the list of BIObjects", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return resultNumber;
	}

	@Override
	public List<BIObject> loadPagedObjectsList(Integer offset, Integer fetchSize) throws EMFUserError {
		LOGGER.debug("IN");
		List<BIObject> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList<>();
			List<SbiObjects> toTransform = null;

			String hql = "select count(*) from SbiObjects ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = temp.intValue();

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber) : resultNumber;
			}

			hibernateQuery = aSession.createQuery("from SbiObjects order by label");
			hibernateQuery.setFirstResult(offset);
			if (fetchSize > 0)
				hibernateQuery.setMaxResults(fetchSize);

			toTransform = hibernateQuery.list();

			Iterator<SbiObjects> it = toTransform.iterator();
			while (it.hasNext()) {
				SbiObjects object = it.next();
				toReturn.add(toBIObject(object, aSession));
			}
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error while loading the list of Resources", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	@Override
	public String changeLockStatus(String documentLabel, boolean isUserAdmin) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		String toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("label", documentLabel);
			Criteria criteria = aSession.createCriteria(SbiObjects.class);
			criteria.add(labelCriterrion);

			SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();
			if (hibObject == null)
				return null;

			String currentUser = (String) ((UserProfile) getUserProfile()).getUserId();

			boolean isLocked = false;
			if (hibObject.getLockedByUser() != null && !hibObject.getLockedByUser().equals(""))
				isLocked = true;

			if (isLocked && hibObject.getLockedByUser().equals(currentUser)) {
				hibObject.setLockedByUser(null);
				aSession.save(hibObject);
				tx.commit();
				toReturn = hibObject.getLockedByUser();
			} else if (!isLocked) {
				// if its not lcked change
				hibObject.setLockedByUser(currentUser);
				aSession.save(hibObject);
				tx.commit();
				toReturn = hibObject.getLockedByUser();
			} else if (isLocked && !hibObject.getLockedByUser().equals(currentUser) && isUserAdmin) {
				hibObject.setLockedByUser(null);
				aSession.save(hibObject);
				tx.commit();
				toReturn = hibObject.getLockedByUser();
			} else {
				toReturn = null;
			}

		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error changing lock status for document with label {} and isUserAdmin value equals to {}",
					documentLabel, isUserAdmin, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		return toReturn;
	}

	@Override
	public List<BIObjectParameter> loadDocumentDrivers(BIObject biObject, String role) throws EMFUserError {
		LOGGER.debug("IN");
		List<BIObjectParameter> toReturn = new ArrayList<>();
		Session aSession = getSession();
		Transaction tx = aSession.beginTransaction();
		try {
			String hql = "from SbiObjPar s where s.sbiObject.label = ? order by s.priority asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, biObject.getLabel());
			List<SbiObjPar> hibObjectPars = hqlQuery.list();
			Iterator<SbiObjPar> it = hibObjectPars.iterator();
			BIObjectParameterDAOHibImpl aBIObjectParameterDAOHibImpl = new BIObjectParameterDAOHibImpl();
			IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
			int count = 1;
			while (it.hasNext()) {
				SbiObjPar hibObjPar = it.next();
				BIObjectParameter tmpBIObjectParameter = aBIObjectParameterDAOHibImpl.toBIObjectParameter(hibObjPar);
				recalculateParameterPriority(aSession, biObject, tmpBIObjectParameter, aBIObjectParameterDAOHibImpl,
						count);
				Parameter aParameter = aParameterDAO
						.loadForExecutionByParameterIDandRoleName(tmpBIObjectParameter.getParID(), role, false);
				tmpBIObjectParameter.setParID(aParameter.getId());
				tmpBIObjectParameter.setParameter(aParameter);
				toReturn.add(tmpBIObjectParameter);
			}
			tx.commit();
		} catch (HibernateException he) {
			rollbackIfActive(tx);
			LOGGER.error("Error loading document drivers for BI Object id {} and role {}", biObject, role, he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			closeSessionIfOpen(aSession);
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	private void recalculateParameterPriority(Session session, BIObject biObject, AbstractDriver parameter,
			BIObjectParameterDAOHibImpl dao, int count) {
		Integer priority = parameter.getPriority();
		if (priority == null || priority.intValue() != count) {
			LOGGER.warn(
					"The priorities of the biparameters for the document with id = {} are not sorted. Priority recalculation starts.",
					biObject.getId());
			dao.recalculateBiParametersPriority(biObject.getId(), session);
			parameter.setPriority(count);
		}
	}

	public BIObjectParameter transformDSDrivertoBIObjectParameter(BIMetaModelParameter datasetDriver, BIObject biObject,
			String role) {
		BIObjectParameter docDriver = new BIObjectParameter();
		IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
		docDriver.setId(biObject.getId());
		docDriver.setLabel(datasetDriver.getLabel());
		docDriver.setModifiable(datasetDriver.getModifiable());
		docDriver.setMultivalue(datasetDriver.getMultivalue());
		docDriver.setBiObjectID(datasetDriver.getBiMetaModelID());
		docDriver.setParameterUrlName(datasetDriver.getParameterUrlName());
		docDriver.setParID(datasetDriver.getParID());
		docDriver.setRequired(datasetDriver.getRequired());
		docDriver.setVisible(datasetDriver.getVisible());
		docDriver.setPriority(datasetDriver.getPriority());
		docDriver.setProg(datasetDriver.getProg());
		docDriver.setColSpan(datasetDriver.getColSpan());
		docDriver.setThickPerc(datasetDriver.getThickPerc());

		Parameter parameter = new Parameter();
		try {
			parameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(docDriver.getParID(), role, false);
		} catch (EMFUserError e) {
			LOGGER.error(
					"Error transforming DS Driver to BI Object Parameter the driver {} of BI Object {} and role {}",
					datasetDriver, biObject, role, e);
		}
		parameter.setId(datasetDriver.getParameter().getId());
		parameter.setType(datasetDriver.getParameter().getType());
		docDriver.setParameter(parameter);
		return docDriver;
	}

	private List<SbiOutputParameter> loadDriverSpecificOutputParameters(SbiObjects sbiObject) {
		List<SbiOutputParameter> ret = new ArrayList<>();
		SbiEngines sbiEngines = sbiObject.getSbiEngines();
		if (sbiEngines != null && sbiEngines.getDriverNm() != null && !sbiEngines.getDriverNm().isEmpty()) {
			try {
				IEngineDriver driver = (IEngineDriver) Class.forName(sbiEngines.getDriverNm()).newInstance();
				List<DefaultOutputParameter> params = driver.getDefaultOutputParameters();
				for (DefaultOutputParameter defaultOutputParameter : params) {
					SbiOutputParameter outputParameter = new SbiOutputParameter();
					outputParameter.setBiobjId(sbiObject.getBiobjId());
					outputParameter.setLabel(defaultOutputParameter.getParamName());
					outputParameter.setParameterTypeId(
							getDefaultParameterMap().get(defaultOutputParameter.getParamType()).getValueId());
					updateSbiCommonInfo4Insert(outputParameter);
					ret.add(outputParameter);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				LOGGER.error("Error trying to load default output parameters for engine [{}] ",
						sbiEngines.getDriverNm(), e);
			}
		}
		return ret;
	}

	/**
	 * Specially provided method for custom-made output category parameters for the SUNBURST chart.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	private List<SbiOutputParameter> loadDriverSpecificOutputParameters(SbiObjects sbiObject, List categories) {
		List<SbiOutputParameter> ret = new ArrayList<>();
		if (sbiObject.getSbiEngines() != null) {
			try {
				IEngineDriver driver = (IEngineDriver) Class.forName(sbiObject.getSbiEngines().getDriverNm())
						.newInstance();
				List<DefaultOutputParameter> params = driver.getSpecificOutputParameters(categories);
				for (DefaultOutputParameter defaultOutputParameter : params) {
					SbiOutputParameter outputParameter = new SbiOutputParameter();
					outputParameter.setBiobjId(sbiObject.getBiobjId());
					outputParameter.setLabel(defaultOutputParameter.getParamName());
					outputParameter.setParameterTypeId(
							getDefaultParameterMap().get(defaultOutputParameter.getParamType()).getValueId());
					updateSbiCommonInfo4Insert(outputParameter);
					ret.add(outputParameter);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				LOGGER.error("Error trying to load default output parameters for engine [{}] ",
						sbiObject.getSbiEngines().getDriverNm(), e);
			}
		}
		return ret;
	}

	/**
	 * Method used for special chart types, that need exclusion of some of default output parameters. Example: WORDCLOUD, PARALLEL and CHORD chart types.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	private List<SbiOutputParameter> loadDriverSpecificOutputParameters(SbiObjects sbiObject,
			String specificChartType) {
		List<SbiOutputParameter> ret = new ArrayList<>();
		if (sbiObject.getSbiEngines() != null) {
			try {
				IEngineDriver driver = (IEngineDriver) Class.forName(sbiObject.getSbiEngines().getDriverNm())
						.newInstance();
				List<DefaultOutputParameter> params = driver.getSpecificOutputParameters(specificChartType);
				for (DefaultOutputParameter defaultOutputParameter : params) {
					SbiOutputParameter outputParameter = new SbiOutputParameter();
					outputParameter.setBiobjId(sbiObject.getBiobjId());
					outputParameter.setLabel(defaultOutputParameter.getParamName());
					outputParameter.setParameterTypeId(
							getDefaultParameterMap().get(defaultOutputParameter.getParamType()).getValueId());
					updateSbiCommonInfo4Insert(outputParameter);
					ret.add(outputParameter);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				LOGGER.error("Error trying to load default output parameters for engine [{}] ",
						sbiObject.getSbiEngines().getDriverNm(), e);
			}
		}
		return ret;
	}

	private Map<TYPE, Domain> getDefaultParameterMap() {
		if (defaultParameterMap == null) {
			defaultParameterMap = new HashMap();
			try {
				IDomainDAO domainDao = DAOFactory.getDomainDAO();
				defaultParameterMap.put(TYPE.String, domainDao.loadDomainByCodeAndValue("PAR_TYPE", "STRING"));
				defaultParameterMap.put(TYPE.Number, domainDao.loadDomainByCodeAndValue("PAR_TYPE", "NUM"));
				defaultParameterMap.put(TYPE.Date, domainDao.loadDomainByCodeAndValue("PAR_TYPE", "DATE"));
			} catch (EMFUserError e) {
				LOGGER.error("Unable to load PAR_TYPE domains", e);
			}
		}
		return defaultParameterMap;
	}

	private Domain from(SbiDomains sbiType) {
		Domain type = new Domain();
		type.setDomainCode(sbiType.getDomainCd());
		type.setDomainName(sbiType.getDomainNm());
		type.setValueCd(sbiType.getValueCd());
		type.setValueDescription(sbiType.getValueDs());
		type.setValueName(sbiType.getValueNm());
		type.setValueId(sbiType.getValueId());
		return type;
	}

	private Set<String> getUserRoles(Session aSession, Collection<String> userRoles,
			Set<String> allRolesWithPermission) {
		// userRolesWithPermission will store the filtered roles with
		// permissions on folders containing the required document
		Set<String> userRolesWithPermission = new TreeSet<>();
		for (String role : userRoles) {
			// if the role is a user role and can see the document (in REL
			// or TEST state),
			// it is a correct role
			// TESTER management: add all available folders execution roles
			String roleType = getRoleType(aSession, role);
			if (isTestRole(roleType)) {
				userRolesWithPermission = allRolesWithPermission;
				break;
			} else if (allRolesWithPermission.contains(role)) {
				userRolesWithPermission.add(role);
			}
		}
		return userRolesWithPermission;
	}

	private String getRoleType(Session aSession, String role) {
		String roleTHql = "select roles.roleTypeCode from SbiExtRoles as roles where roles.name = :role ";
		Query roleHqlQuery = aSession.createQuery(roleTHql);
		roleHqlQuery.setParameter("role", role);
		return (String) roleHqlQuery.uniqueResult();
	}

	private boolean isTestRole(String roleType) {
		return SpagoBIConstants.ROLE_TYPE_TEST.equals(roleType);
	}

	private List<String> filterUsableRolesByObjectParameters(Integer id, List<String> correctRoles, Session aSession,
			Set<String> userRolesWithPermission) {
		Query hqlQuery;
		String hql;
		// find all id parameters relative to the objects
		hql = "select par.parId from " + "SbiParameters as par, SbiObjects as obj, SbiObjPar as objpar  "
				+ "where obj.biobjId = ?  and " + "      obj.biobjId = objpar.sbiObject.biobjId and "
				+ "      par.parId = objpar.id.sbiParameter.parId ";
		hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, id.intValue());
		List idParameters = hqlQuery.list();

		if (idParameters.isEmpty()) {
			// if the object has not parameter associates all the roles that
			// have the execution or
			// test permissions on the containing folders are correct roles
			// in the same manner.
			correctRoles = new ArrayList<>(userRolesWithPermission);
		} else {
			// second filter on roles: finds only roles with correct modalities
			// of the parameters of the required document
			Iterator iterParam = null;
			String idPar = null;
			List parUses = null;
			// for each role of the user
			for (String role : userRolesWithPermission) {
				boolean correct = true;
				iterParam = idParameters.iterator();
				// for each parameter get the number of the modality for the
				// current role
				while (iterParam.hasNext()) {
					idPar = iterParam.next().toString();
					hql = "select puseDet.id.sbiParuse.useId" + " from SbiParuse as puse,"
							+ "     SbiParuseDet as puseDet," + "     SbiExtRoles as rol" + " where rol.name = '" + role
							+ "'" + "  and puseDet.id.sbiExtRoles.extRoleId = rol.extRoleId"
							+ "  and puse.sbiParameters.parId = " + idPar
							+ "  and puseDet.id.sbiParuse.useId = puse.useId";
					hqlQuery = aSession.createQuery(hql);
					parUses = hqlQuery.list();
					// if the modality for the current role and the current
					// parameter are more
					// or less than one the role can't execute the report and so
					// it isn't
					// correct
					if (parUses.size() != 1) {
						correct = false;
					}
				}
				if (correct) {
					correctRoles.add(role);
					LOGGER.debug("There is one available modality for role [{}] on parameter [{}]", role, idPar);
				} else {
					LOGGER.debug("There is no modality available for role [{}] on parameter [{}]", role, idPar);
				}
			}
		}
		return correctRoles;
	}

}
