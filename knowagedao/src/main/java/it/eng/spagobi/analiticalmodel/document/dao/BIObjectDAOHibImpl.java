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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.json.JSONObject;
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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.engines.config.dao.EngineDAOHibImpl;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter.TYPE;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDs;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiOutputParameter;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;

/**
 * Defines the Hibernate implementations for all DAO methods, for a BI Object.
 */
public class BIObjectDAOHibImpl extends AbstractHibernateDAO implements IBIObjectDAO {

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

	private static Logger logger = Logger.getLogger(BIObjectDAOHibImpl.class);

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
		logger.debug("IN");
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
			List hibObjectPars = hqlQuery.list();
			SbiObjPar hibObjPar = null;
			Iterator it = hibObjectPars.iterator();
			BIObjectParameter tmpBIObjectParameter = null;
			BIObjectParameterDAOHibImpl aBIObjectParameterDAOHibImpl = new BIObjectParameterDAOHibImpl();
			IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
			List biObjectParameters = new ArrayList();
			Parameter aParameter = null;
			int count = 1;
			while (it.hasNext()) {
				hibObjPar = (SbiObjPar) it.next();
				tmpBIObjectParameter = aBIObjectParameterDAOHibImpl.toBIObjectParameter(hibObjPar);

				// *****************************************************************
				// **************** START PRIORITY RECALCULATION
				// *******************
				// *****************************************************************
				Integer priority = tmpBIObjectParameter.getPriority();
				if (priority == null || priority.intValue() != count) {
					logger.warn("The priorities of the biparameters for the document with id = " + biObject.getId()
							+ " are not sorted. Priority recalculation starts.");
					aBIObjectParameterDAOHibImpl.recalculateBiParametersPriority(biObject.getId(), aSession);
					tmpBIObjectParameter.setPriority(new Integer(count));
				}
				count++;
				// *****************************************************************
				// **************** END PRIORITY RECALCULATION
				// *******************
				// *****************************************************************

				aParameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(tmpBIObjectParameter.getParID(), role, false);
				tmpBIObjectParameter.setParID(aParameter.getId());
				tmpBIObjectParameter.setParameter(aParameter);
				biObjectParameters.add(tmpBIObjectParameter);
			}
			biObject.setDrivers(biObjectParameters);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Object for execution by id " + id + " and role " + role, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
					aSession.close();
			}
		}
		logger.debug("OUT");
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
		logger.debug("IN");
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
				logger.warn("Unable to load document whose id is equal to [" + biObjectID + "]");
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Object by id " + biObjectID, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
		Monitor monitor = MonitorFactory.start("it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl.loadBIObjectForDetail(Integer id)");
		logger.debug("IN");
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
				logger.warn("Unable to load document whose id is equal to [" + id + "]");
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Object for detail by id " + id, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
		logger.debug("IN");
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
				logger.warn("Unable to load document whose label is equal to [" + label + "]");
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Object by label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
		logger.debug("IN. start method with input id:" + id);
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			logger.debug("hibernate session obtained:" + aSession);
			tx = aSession.beginTransaction();
			logger.debug("hibernate transaction started");
			Criterion domainCdCriterrion = Restrictions.eq("biobjId", id);
			Criteria criteria = aSession.createCriteria(SbiObjects.class);
			criteria.add(domainCdCriterrion);
			logger.debug("hibernate criteria filled:" + criteria);
			SbiObjects hibObject = (SbiObjects) criteria.uniqueResult();
			logger.debug("hibernate object retrived:" + hibObject);
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				logger.warn("Unable to load document whose id is equal to [" + id + "]");
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Object for tree by id " + id, he);
			if (tx != null)
				tx.rollback();
			logger.error("hibernate exception", he);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
			logger.debug("OUT.end method with input id:" + id);
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
		logger.debug("IN");
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
			Set hibFunctionalities = hibBIObject.getSbiObjFuncs();
			for (Iterator it = hibFunctionalities.iterator(); it.hasNext();) {
				aSession.delete(it.next());
			}
			// functionalities storing
			Set hibObjFunc = new HashSet();
			List functionalities = biObject.getFunctionalities();
			for (Iterator it = functionalities.iterator(); it.hasNext();) {
				Integer functId = (Integer) it.next();
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

			logger.debug("Update detail dataset");
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(biObject.getId(), biObject.getDataSetId(), aSession);

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
				for (Iterator iterator = hibBIObject.getSbiOutputParameters().iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutPar = (SbiOutputParameter) iterator.next();
					// if (sbiOutPar.getIsUserDefined() != null && sbiOutPar.getIsUserDefined() == true) {
					if (sbiOutPar.getIsUserDefined() == null || sbiOutPar.getIsUserDefined() == false) {
						cleanedOutPars.add(sbiOutPar);
					}
				}
				hibBIObject.setSbiOutputParameters(cleanedOutPars);
				// hibBIObject.getSbiOutputParameters().clear();
				DAOFactory.getOutputParameterDAO().removeSystemDefinedParametersByBiobjId(hibBIObject.getBiobjId(), aSession);
				aSession.flush();
			}
			// }

			// If there are no output parameters persisted already for this document, create new ones for it. (danristo)
			// if (hibBIObject.getSbiOutputParameters() == null || hibBIObject.getSbiOutputParameters().isEmpty()) {

			List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject);

			for (Iterator iterator = op.iterator(); iterator.hasNext();) {
				SbiOutputParameter sbiOutputParameter = (SbiOutputParameter) iterator.next();
				aSession.save(sbiOutputParameter);
			}

			hibBIObject.getSbiOutputParameters().addAll(op);
			// }

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
					if (loadParsDC && (oldTemp == null || objTemp.getId() == null || objTemp.getId().compareTo(oldTemp.getId()) != 0)) {
						insertParametersDocComposition(biObject, objTemp, true);
					}

				} catch (Exception e) {
					logger.error("Error during creation of document composition parameters : ", e);
					throw new EMFUserError(EMFErrorSeverity.ERROR, e.getMessage());
				}

			}

			// insert relation with ds for data lineage
			// KNOWAGE-4819
			DAOFactory.getSbiObjDsDAO().insertRelationsFromObj(biObject);

			logger.debug("OUT");
		} catch (HibernateException he) {
			logger.error("Error modifing BI Object " + String.valueOf(biObject) + " using template " + String.valueOf(objTemp) + " with load parameters equals to " + loadParsDC, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
	}

	/**
	 * Updates the biobject data into database (specially provided for custom-made output category parameters for the SUNBURST chart).
	 *
	 * @param biObject The BI Object as input
	 * @param objTemp  The BIObject template
	 * @throws EMFUserError If any exception occurred
	 */
	private void internalModify(BIObject biObject, ObjTemplate objTemp, boolean loadParsDC, List categories) throws EMFUserError {
		logger.debug("IN");
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
				hibBIObject.setEncrypt(new Short(biObject.getEncrypt().shortValue()));
			if (biObject.getVisible() != null)
				hibBIObject.setVisible(new Short(biObject.getVisible().shortValue()));

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
			Set hibFunctionalities = hibBIObject.getSbiObjFuncs();
			for (Iterator it = hibFunctionalities.iterator(); it.hasNext();) {
				aSession.delete(it.next());
			}
			// functionalities storing
			Set hibObjFunc = new HashSet();
			List functionalities = biObject.getFunctionalities();
			for (Iterator it = functionalities.iterator(); it.hasNext();) {
				Integer functId = (Integer) it.next();
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

			logger.debug("Update detail dataset");
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(biObject.getId(), biObject.getDataSetId(), aSession);

			if (!categories.isEmpty()) {
				// hibBIObject.getSbiOutputParameters().removeAll(loadDriverSpecificOutputParameters(hibBIObject));

				// delete SbiOutputParameters
				if (hibBIObject.getSbiOutputParameters() != null && !hibBIObject.getSbiOutputParameters().isEmpty()) {
					hibBIObject.getSbiOutputParameters().clear();
					DAOFactory.getOutputParameterDAO().removeParametersByBiobjId(hibBIObject.getBiobjId(), aSession);
					aSession.flush();
				}

				// Persist the dataset
				List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject, categories);

				for (Iterator iterator = op.iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutputParameter = (SbiOutputParameter) iterator.next();
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
					if (loadParsDC && (oldTemp == null || objTemp.getId() == null || objTemp.getId().compareTo(oldTemp.getId()) != 0)) {
						insertParametersDocComposition(biObject, objTemp, true);
					}
				} catch (Exception e) {
					logger.error("Error during creation of document composition parameters : ", e);
					throw new EMFUserError(EMFErrorSeverity.ERROR, e.getMessage());
				}

			}

			logger.debug("OUT");
		} catch (HibernateException he) {
			logger.error("Error modifing BI Object " + String.valueOf(biObject) + " using template " + String.valueOf(objTemp) + " with load parameters equals to " + loadParsDC, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
	}

	/**
	 * Updates the biobject data into database (for special chart types, that need exclusion of some of default output parameters). Example: WORDCLOUD, PARALLEL
	 * and CHORD chart types.
	 *
	 * @param biObject The BI Object as input
	 * @param objTemp  The BIObject template
	 * @throws EMFUserError If any exception occurred
	 */
	private void internalModify(BIObject biObject, ObjTemplate objTemp, boolean loadParsDC, String specificChartTypes) throws EMFUserError {
		logger.debug("IN");
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
				hibBIObject.setEncrypt(new Short(biObject.getEncrypt().shortValue()));
			if (biObject.getVisible() != null)
				hibBIObject.setVisible(new Short(biObject.getVisible().shortValue()));

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
			Set hibFunctionalities = hibBIObject.getSbiObjFuncs();
			for (Iterator it = hibFunctionalities.iterator(); it.hasNext();) {
				aSession.delete(it.next());
			}
			// functionalities storing
			Set hibObjFunc = new HashSet();
			List functionalities = biObject.getFunctionalities();
			for (Iterator it = functionalities.iterator(); it.hasNext();) {
				Integer functId = (Integer) it.next();
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

			logger.debug("Update detail dataset");
			DAOFactory.getBIObjDataSetDAO().updateObjectDetailDataset(biObject.getId(), biObject.getDataSetId(), aSession);

			if (!specificChartTypes.equals("")) {

				hibBIObject.getSbiOutputParameters().clear();
				DAOFactory.getOutputParameterDAO().removeParametersByBiobjId(hibBIObject.getBiobjId(), aSession);
				aSession.flush();

				List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject, specificChartTypes);

				for (Iterator iterator = op.iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutputParameter = (SbiOutputParameter) iterator.next();
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
					if (loadParsDC && (oldTemp == null || objTemp.getId() == null || objTemp.getId().compareTo(oldTemp.getId()) != 0)) {
						insertParametersDocComposition(biObject, objTemp, true);
					}
				} catch (Exception e) {
					logger.error("Error during creation of document composition parameters : ", e);
					throw new EMFUserError(EMFErrorSeverity.ERROR, e.getMessage());
				}

			}

			logger.debug("OUT");
		} catch (HibernateException he) {
			logger.error("Error modifing BI Object " + String.valueOf(biObject) + " using template " + String.valueOf(objTemp) + " with load parameters equals to " + loadParsDC, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
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

	private Integer internalInsertBIObject(BIObject obj, ObjTemplate objTemp, boolean loadParsDC, boolean loadOP) throws EMFUserError {
		logger.debug("IN");
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
				hibBIObject.setEncrypt(new Short(obj.getEncrypt().shortValue()));
			}
			if (obj.getVisible() != null) {
				hibBIObject.setVisible(new Short(obj.getVisible().shortValue()));
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
				refreshSeconds = new Integer(0);
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
			// hibBIObject.getSbiOutputParameters().addAll(loadDriverSpecificOutputParameters(hibBIObject));
			if (loadOP) {
				List<SbiOutputParameter> op = loadDriverSpecificOutputParameters(hibBIObject);

				for (Iterator iterator = op.iterator(); iterator.hasNext();) {
					SbiOutputParameter sbiOutputParameter = (SbiOutputParameter) iterator.next();
					aSession.save(sbiOutputParameter);
				}

				hibBIObject.getSbiOutputParameters().addAll(op);
			}

			// functionalities storing
			Set hibObjFunc = new HashSet();
			List functionalities = obj.getFunctionalities();
			for (Iterator it = functionalities.iterator(); it.hasNext();) {
				Integer functId = (Integer) it.next();
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
			logger.error("Error inserting BI Object " + String.valueOf(obj) + " using template " + String.valueOf(objTemp) + " with load parameters equals to " + loadParsDC, he);
			if (tx != null)
				tx.rollback();
			throw new HibernateException(he.getLocalizedMessage(), he);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// load object
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, obj.getId());
			// erase object from functionalities
			Set hibObjFuncs = hibBIObject.getSbiObjFuncs();
			Iterator itObjFunc = hibObjFuncs.iterator();
			while (itObjFunc.hasNext()) {
				SbiObjFunc aSbiObjFunc = (SbiObjFunc) itObjFunc.next();
				if (idFunct == null || aSbiObjFunc.getId().getSbiFunctions().getFunctId().intValue() == idFunct.intValue()) {
					logger.debug("Deleting object [" + obj.getName() + "] from folder [" + aSbiObjFunc.getId().getSbiFunctions().getPath() + "]");
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

				logger.debug("The object [" + obj.getName() + "] is no more referenced by any functionality. It will be completely deleted from db.");

				// delete templates
				String hql = "from SbiObjTemplates sot where sot.sbiObject.biobjId=" + obj.getId();
				Query query = aSession.createQuery(hql);
				List templs = query.list();
				Iterator iterTempls = templs.iterator();
				while (iterTempls.hasNext()) {
					SbiObjTemplates hibObjTemp = (SbiObjTemplates) iterTempls.next();
					SbiBinContents hibBinCont = hibObjTemp.getSbiBinContents();
					aSession.delete(hibObjTemp);
					aSession.delete(hibBinCont);

				}

				// delete subobjects eventually associated
				ISubObjectDAO subobjDAO = DAOFactory.getSubObjectDAO();
				List subobjects = subobjDAO.getSubObjects(obj.getId());
				for (int i = 0; i < subobjects.size(); i++) {
					SubObject s = (SubObject) subobjects.get(i);
					// subobjDAO.deleteSubObject(s.getId());
					subobjDAO.deleteSubObjectSameConnection(s.getId(), aSession);
				}

				// delete viewpoints eventually associated
				List viewpoints = new ArrayList();
				IViewpointDAO biVPDAO = DAOFactory.getViewpointDAO();
				viewpoints = biVPDAO.loadAllViewpointsByObjID(obj.getId());
				for (int i = 0; i < viewpoints.size(); i++) {
					Viewpoint vp = (Viewpoint) viewpoints.get(i);
					biVPDAO.eraseViewpoint(vp.getVpId());
				}

				// delete snapshots eventually associated
				ISnapshotDAO snapshotsDAO = DAOFactory.getSnapshotDAO();
				List snapshots = snapshotsDAO.getSnapshots(obj.getId());
				for (int i = 0; i < snapshots.size(); i++) {
					Snapshot aSnapshots = (Snapshot) snapshots.get(i);
					snapshotsDAO.deleteSnapshot(aSnapshots.getId());
				}

				// delete notes eventually associated
				IObjNoteDAO objNoteDAO = DAOFactory.getObjNoteDAO();
				objNoteDAO.eraseNotes(obj.getId());

				// delete metadata eventually associated
				List metadata = DAOFactory.getObjMetadataDAO().loadAllObjMetadata();
				IObjMetacontentDAO objMetaContentDAO = DAOFactory.getObjMetacontentDAO();
				if (metadata != null && !metadata.isEmpty()) {
					Iterator it = metadata.iterator();
					while (it.hasNext()) {
						ObjMetadata objMetadata = (ObjMetadata) it.next();
						ObjMetacontent objMetacontent = DAOFactory.getObjMetacontentDAO().loadObjMetacontent(objMetadata.getObjMetaId(), obj.getId(), null);
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

				Set objPars = hibBIObject.getSbiObjPars();

				Iterator itObjParDep = objPars.iterator();
				BIObjectParameterDAOHibImpl objParDAO = new BIObjectParameterDAOHibImpl();
				while (itObjParDep.hasNext()) {
					SbiObjPar aSbiObjPar = (SbiObjPar) itObjParDep.next();
					BIObjectParameter aBIObjectParameter = new BIObjectParameter();
					aBIObjectParameter.setId(aSbiObjPar.getObjParId());
					objParDAO.eraseBIObjectParameterDependencies(aBIObjectParameter, aSession);
				}

				Iterator itObjPar = objPars.iterator();
				while (itObjPar.hasNext()) {
					SbiObjPar aSbiObjPar = (SbiObjPar) itObjPar.next();
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
				logger.debug("OUT");

			}
			// commit all changes
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error erasing BI Object " + String.valueOf(obj) + " and function id equals to " + idFunct, he);
			if (tx != null && tx.isActive())
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logger.error("Error erasing BI Object " + String.valueOf(obj) + " and function id equals to " + idFunct, ex);
			if (tx != null && tx.isActive())
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
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
	public List getCorrectRolesForExecution(Integer id, IEngUserProfile profile) throws EMFUserError {
		logger.debug("IN");
		List correctRoles = new ArrayList();
		try {
			logger.debug("IN");
			Session aSession = null;
			Transaction tx = null;
			UserProfile _profile = (UserProfile) profile;
			Collection roles = _profile.getRolesForUse();

			try {
				aSession = getSession();
				tx = aSession.beginTransaction();

				logger.debug("The user have [" + roles.size() + "] different roles");

				// allRolesWithPermission will store all roles with permissions on
				// folders containing the required document
				Set<String> allRolesWithPermission = new TreeSet<>();

				// first filter on roles: finds only roles with permissions on
				// folders containing the required document
				SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, id);
				String objectState = hibBIObject.getState().getValueCd();
				String permission = ObjectsAccessVerifier.getPermissionFromDocumentState(objectState);
				Set hibObjFuncs = hibBIObject.getSbiObjFuncs();
				Iterator itObjFunc = hibObjFuncs.iterator();
				while (itObjFunc.hasNext()) {
					SbiObjFunc aSbiObjFunc = (SbiObjFunc) itObjFunc.next();
					SbiFunctions aSbiFunctions = aSbiObjFunc.getId().getSbiFunctions();
					String funcTypeCd = aSbiFunctions.getFunctTypeCd();
					logger.debug("Folder type [" + funcTypeCd + "]");
					if (!funcTypeCd.equalsIgnoreCase("USER_FUNCT")) {
						logger.debug("Folder id [" + aSbiFunctions.getFunctId() + "]");
						logger.debug("Document state [" + objectState + "]");

						String rolesHql = "select distinct roles.name from " + "SbiExtRoles as roles, SbiFuncRole as funcRole "
								+ "where roles.extRoleId = funcRole.id.role.extRoleId and " + "	   funcRole.id.function.functId = " + aSbiFunctions.getFunctId()
								+ " and " + "	   funcRole.id.state.valueCd = '" + permission + "' ";
						Query rolesHqlQuery = aSession.createQuery(rolesHql);
						// get the list of roles that can see the document (in REL
						// or TEST state) in that functionality
						List rolesNames = new ArrayList();
						rolesNames = rolesHqlQuery.list();
						allRolesWithPermission.addAll(rolesNames);
					} else if(aSbiFunctions.getName().equals(_profile.getUserId())) {
						allRolesWithPermission.addAll(roles);
					}
				}

				allRolesWithPermission.retainAll(roles);

				logger.debug("There are [" + allRolesWithPermission.size() + "] roles that can execut doc [" + id + "] depending on its location");

				Set<String> userRolesWithPermission = getUserRoles(aSession, roles, allRolesWithPermission);

				logger.debug("The user have [" + userRolesWithPermission.size() + "] different roles that can execute doc [" + id + "] depending on its location");

				correctRoles = filterUsableRolesByObjectParameters(id, correctRoles, aSession, userRolesWithPermission);

				tx.rollback();
			} catch (HibernateException he) {
				logger.error("Error getting correct roles for execution for object id " + id + " and user profile " + profile, he);
				if (tx != null)
					tx.rollback();
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession != null && aSession.isOpen()) {
					aSession.close();
				}
			}
			logger.debug("OUT");
		} catch (EMFInternalError emfie) {
			logger.error("error getting role from the user profile", emfie);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
		logger.debug("OUT");
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
	public List getCorrectRolesForExecution(Integer id) throws EMFUserError {
		logger.debug("IN");
		List roles = DAOFactory.getRoleDAO().loadAllRoles();
		List nameRoles = new ArrayList();
		Iterator iterRoles = roles.iterator();
		Role role = null;
		while (iterRoles.hasNext()) {
			role = (Role) iterRoles.next();
			nameRoles.add(role.getName());
		}
		logger.debug("OUT");
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
	private List getCorrectRoles(Integer id, Collection roles) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List correctRoles = new ArrayList();

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			logger.debug("The user have [" + roles.size() + "] different roles");

			// allRolesWithPermission will store all roles with permissions on
			// folders containing the required document
			Set<String> allRolesWithPermission = new TreeSet<>();

			// first filter on roles: finds only roles with permissions on
			// folders containing the required document
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, id);
			String objectState = hibBIObject.getState().getValueCd();
			String permission = ObjectsAccessVerifier.getPermissionFromDocumentState(objectState);
			Set hibObjFuncs = hibBIObject.getSbiObjFuncs();
			Iterator itObjFunc = hibObjFuncs.iterator();
			while (itObjFunc.hasNext()) {
				SbiObjFunc aSbiObjFunc = (SbiObjFunc) itObjFunc.next();
				SbiFunctions aSbiFunctions = aSbiObjFunc.getId().getSbiFunctions();
				String funcTypeCd = aSbiFunctions.getFunctTypeCd();
				logger.debug("Folder type [" + funcTypeCd + "]");
				if (!funcTypeCd.equalsIgnoreCase("USER_FUNCT")) {
					logger.debug("Folder id [" + aSbiFunctions.getFunctId() + "]");
					logger.debug("Document state [" + objectState + "]");

					String rolesHql = "select distinct roles.name from " + "SbiExtRoles as roles, SbiFuncRole as funcRole "
							+ "where roles.extRoleId = funcRole.id.role.extRoleId and " + "	   funcRole.id.function.functId = " + aSbiFunctions.getFunctId()
							+ " and " + "	   funcRole.id.state.valueCd = '" + permission + "' ";
					Query rolesHqlQuery = aSession.createQuery(rolesHql);
					// get the list of roles that can see the document (in REL
					// or TEST state) in that functionality
					List rolesNames = new ArrayList();
					rolesNames = rolesHqlQuery.list();
					allRolesWithPermission.addAll(rolesNames);
				} else {
					List l = new ArrayList();
					l.addAll(roles);
					return l;
				}
			}

			logger.debug("There are [" + allRolesWithPermission.size() + "] roles that can execut doc [" + id + "] depending on its location");

			Set<String> userRolesWithPermission = getUserRoles(aSession, roles, allRolesWithPermission);

			logger.debug("The user have [" + userRolesWithPermission.size() + "] different roles that can execute doc [" + id + "] depending on its location");

			correctRoles = filterUsableRolesByObjectParameters(id, correctRoles, aSession, userRolesWithPermission);

			tx.rollback();
		} catch (HibernateException he) {
			logger.error("Error getting correct roles for execution for object id " + id, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
		logger.debug("IN");
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
			aBIObject.setEncrypt(new Integer(hibBIObject.getEncrypt().intValue()));
		} else
			aBIObject.setEncrypt(new Integer(0));

		// set visible flag
		if (hibBIObject.getVisible() != null) {
			aBIObject.setVisible(new Integer(hibBIObject.getVisible().intValue()));
		} else
			aBIObject.setVisible(new Integer(0));

		// set profiled visibility information
		aBIObject.setProfiledVisibility(hibBIObject.getProfiledVisibility());
		// set engine
		if (hibBIObject.getSbiEngines() != null) {
			aBIObject.setEngine(new EngineDAOHibImpl().toEngine(hibBIObject.getSbiEngines()));
		}
		// set data source
		if (hibBIObject.getDataSource() != null) {
			aBIObject.setDataSourceId(new Integer(hibBIObject.getDataSource().getDsId()));
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

		List functionlities = new ArrayList();
		boolean isPublic = false;
		if (hibBIObject.getSbiObjFuncs() != null) {
			Set hibObjFuncs = hibBIObject.getSbiObjFuncs();
			for (Iterator it = hibObjFuncs.iterator(); it.hasNext();) {
				SbiObjFunc aSbiObjFunc = (SbiObjFunc) it.next();
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

		List businessObjectParameters = new ArrayList();
		Set hibObjPars = hibBIObject.getSbiObjPars();
		if (hibObjPars != null) {
			for (Iterator it = hibObjPars.iterator(); it.hasNext();) {
				SbiObjPar aSbiObjPar = (SbiObjPar) it.next();
				BIObjectParameter par = toBIObjectParameter(aSbiObjPar);
				businessObjectParameters.add(par);
			}
			aBIObject.setDrivers(businessObjectParameters);
		}

		List businessObjectOutputParameters = new ArrayList();
		Set hibObjOutPars = hibBIObject.getSbiOutputParameters();
		if (hibObjOutPars != null) {
			for (Iterator it = hibObjOutPars.iterator(); it.hasNext();) {
				SbiOutputParameter aSbiOutPar = (SbiOutputParameter) it.next();
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
				Config defaultRegionConfig = configDAO.loadConfigParametersByLabel("SPAGOBI.DOCUMENTS.PARAMETERS_REGION_DEFAULT");
				if (defaultRegionConfig != null) {
					region = defaultRegionConfig.getValueCheck();
					logger.debug("default parameters region is " + region);
					if (region == null || region.equals("")) {
						logger.warn("default parameters region not set in configs, put default to east");
						region = "east";
					} else {
						// if default region is top or north becomes north, east
						// or right becomes right
						region = region.equalsIgnoreCase("top") || region.equalsIgnoreCase("north") ? "north" : "east";
					}

				} else {
					region = "east";
					logger.warn("default parameters region not set in configs, put default to east");
				}
			} catch (Exception e) {
				logger.error("Error during recovery of default parameters region setting: go on with default east value", e);
			}
		}

		aBIObject.setParametersRegion(region);
		aBIObject.setLockedByUser(hibBIObject.getLockedByUser());
		// put dataset
		if (session != null) {
			BIObjDataSet biObjDataSet = DAOFactory.getBIObjDataSetDAO().getObjectDetailDataset(aBIObject.getId(), session);
			if (biObjDataSet != null) {
				logger.debug("Associate dataset with id " + biObjDataSet.getDataSetId());
				aBIObject.setDataSetId(biObjDataSet.getDataSetId());
			}
		}
		logger.debug("OUT");
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
		aBIObjectParameter.setModifiable(new Integer(hiObjPar.getModFl().intValue()));
		aBIObjectParameter.setMultivalue(new Integer(hiObjPar.getMultFl().intValue()));
		aBIObjectParameter.setBiObjectID(hiObjPar.getSbiObject().getBiobjId());
		aBIObjectParameter.setParameterUrlName(hiObjPar.getParurlNm());
		aBIObjectParameter.setParID(hiObjPar.getSbiParameter().getParId());
		aBIObjectParameter.setRequired(new Integer(hiObjPar.getReqFl().intValue()));
		aBIObjectParameter.setVisible(new Integer(hiObjPar.getViewFl().intValue()));
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
	public List loadAllBIObjects() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(" from SbiObjects s order by s.label");
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject((SbiObjects) it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading all BI Objects", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	@Override
	public List loadAllBIObjectsByFolderId(final Integer folderId) throws EMFUserError {

		List realResult = executeOnTransaction(new IExecuteOnTransaction<List>() {

			@Override
			public List execute(Session session) throws Exception {
				List sbiObjects = session.createCriteria(SbiObjects.class).createAlias("sbiObjFuncs", "_sbiObjFunc").createAlias("state", "_sbiDomain")
						.add(Restrictions.eq("_sbiObjFunc.id.sbiFunctions.functId", folderId))
						.setProjection(Projections.projectionList().add(org.hibernate.criterion.Property.forName("biobjId").as("biobjId"))
								.add(org.hibernate.criterion.Property.forName("name").as("name"))
								.add(org.hibernate.criterion.Property.forName("creationUser").as("creationUser"))
								.add(org.hibernate.criterion.Property.forName("creationDate").as("creationDate"))
								.add(org.hibernate.criterion.Property.forName("objectTypeCode").as("objectTypeCode"))
								.add(org.hibernate.criterion.Property.forName("descr").as("descr"))
								.add(org.hibernate.criterion.Property.forName("stateCode").as("stateCode"))
								.add(org.hibernate.criterion.Property.forName("sbiEngines").as("sbiEngines"))
								.add(org.hibernate.criterion.Property.forName("label").as("label"))
								.add(org.hibernate.criterion.Property.forName("state").as("state"))
								.add(org.hibernate.criterion.Property.forName("visible").as("visible"))
								.add(org.hibernate.criterion.Property.forName("profiledVisibility").as("profiledVisibility"))
								.add(org.hibernate.criterion.Property.forName("previewFile").as("previewFile")))
						.setResultTransformer(Transformers.aliasToBean(SbiObjects.class)).list();

				Iterator it = sbiObjects.iterator();
				List<BIObject> realResult = new ArrayList<>();
				SbiFunctions functions = (SbiFunctions) session.load(SbiFunctions.class, folderId);
				Set<SbiObjFunc> sbiObjFuns = new HashSet<>();
				SbiObjFunc sbiObjFun = new SbiObjFunc();
				SbiObjFuncId funcId = new SbiObjFuncId();
				funcId.setSbiFunctions(functions);
				sbiObjFun.setId(funcId);
				sbiObjFuns.add(sbiObjFun);
				while (it.hasNext()) {
					SbiObjects sbiObj = (SbiObjects) it.next();
					sbiObj.setSbiObjFuncs(sbiObjFuns);
					realResult.add(toBIObject(sbiObj, session));
				}
				return realResult;

			}

		});

		return realResult;

	}

	@Override
	public List loadDocumentsBeforeDate(String data) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date selectedDate;
			try {
				selectedDate = df.parse(data);
			} catch (ParseException e) {
				logger.error("error in parsing date " + data);
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}

			Criteria criteria = aSession.createCriteria(SbiObjTemplates.class).add(Restrictions.le("creationDate", selectedDate))
					.add(Restrictions.eq("active", Boolean.FALSE)).setProjection(Property.forName("sbiObject"));
			List hibList = criteria.list();

			Iterator it = hibList.iterator();
			ArrayList<String> labelsInserted = new ArrayList<>();
			while (it.hasNext()) {
				SbiObjects obj = (SbiObjects) it.next();
				String label = obj.getLabel();
				if (!labelsInserted.contains(label)) {
					realResult.add(toBIObject(obj, aSession));
				}
				labelsInserted.add(label);
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading documents before date " + data, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadAllBIObjects (java.lang.String)
	 */
	@Override
	public List loadAllBIObjects(String filterOrder) throws EMFUserError {
		logger.debug("IN.filterOrder=" + filterOrder);
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("from SbiObjects s  order by s." + filterOrder);
			// Query hibQuery =
			// aSession.createQuery("from SbiObjects s order by ?" );
			// hibQuery.setString(0, filterOrder);
			List hibList = hibQuery.list();

			// Criteria criteria = aSession.createCriteria(SbiObjects.class);
			// criteria.setFetchMode("sbiObjFuncs.sbiEngines", FetchMode.JOIN);
			// criteria.addOrder(Order.asc(filterOrder));
			// List hibList = criteria.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject((SbiObjects) it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading all BI Objects with following filter: " + filterOrder, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO#loadAllBIObjects ()
	 */
	@Override
	public List<BIObject> loadPaginatedSearchBIObjects(Integer page, Integer item_count, Collection<CriteriaParameter> disjunctions,
			Collection<CriteriaParameter> restrictions) throws EMFUserError {
		logger.debug("IN");
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
				hibQuery.setFirstResult((page - 1) * item_count);
			}

			if (item_count != null) {
				hibQuery.setMaxResults(item_count);
			}

			List ids = hibQuery.list();
			if (!ids.isEmpty()) {
				Criteria mainC = aSession.createCriteria(SbiObjects.class);
				mainC.add(Restrictions.in("biobjId", ids));
				mainC.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

				List<SbiObjects> lso = mainC.list();
				for (SbiObjects so : lso) {
					realResult.add(toBIObject(so, aSession));
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading paginated search for BI Objects", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
	public List getBIObjectParameters(BIObject aBIObject) throws EMFUserError {
		IBIObjectParameterDAO biobjDAO = DAOFactory.getBIObjectParameterDAO();
		List biparams = biobjDAO.loadBIObjectParametersById(aBIObject.getId());
		return biparams;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO# loadAllBIObjectsFromInitialPath(java.lang.String)
	 */
	@Override
	public List loadAllBIObjectsFromInitialPath(String initialPath) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			 * Query hibQuery = aSession.createQuery( "select " + "	distinct(objects) " + "from " +
			 * "	SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions " + "where " +
			 * "	objects.biobjId = objFuncs.id.sbiObjects.biobjId " + "	and objFuncs.id.sbiFunctions.functId = functions.functId " + "	and " +
			 * "		(functions.path = '" + initialPath + "' " + "		 or functions.path like '" + initialPath + "/%' ) " + "order by " +
			 * "	objects.label");
			 */

			Query hibQuery = aSession
					.createQuery("select " + "	distinct(objects) " + "from " + "	SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions "
							+ "where " + "	objects.biobjId = objFuncs.id.sbiObjects.biobjId " + "	and objFuncs.id.sbiFunctions.functId = functions.functId "
							+ "	and " + "		(functions.path = ? " + "		 or functions.path like ?) " + "order by " + "	objects.label");

			hibQuery.setString(0, initialPath);
			hibQuery.setString(1, initialPath + "%");
			List hibList = hibQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject((SbiObjects) it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading all BI Objects from initial path " + initialPath, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO# loadAllBIObjectsFromInitialPath(java.lang.String, java.lang.String)
	 */
	@Override
	public List loadAllBIObjectsFromInitialPath(String initialPath, String filterOrder) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			 * Query hibQuery = aSession.createQuery( "select " + "	distinct(objects) " + "from " +
			 * "	SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions " + "where " +
			 * "	objects.biobjId = objFuncs.id.sbiObjects.biobjId " + "	and objFuncs.id.sbiFunctions.functId = functions.functId " + "	and " +
			 * "		(functions.path = '" + initialPath + "' " + "		 or functions.path like '" + initialPath + "/%' ) " + "order by " + "	objects." +
			 * filterOrder);
			 */
			Query hibQuery = aSession
					.createQuery("select " + "	distinct(objects) " + "from " + "	SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions "
							+ "where " + "	objects.biobjId = objFuncs.id.sbiObjects.biobjId " + "	and objFuncs.id.sbiFunctions.functId = functions.functId "
							+ "	and " + "		(functions.path = ? " + "		 or functions.path like ? " + "order by ? ");
			hibQuery.setString(0, initialPath);
			hibQuery.setString(1, initialPath + "%");
			hibQuery.setString(2, "	objects." + filterOrder);

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject((SbiObjects) it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading all BI Objects from initial path " + initialPath + " and filter " + filterOrder, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO# loadBIObjectForDetail(java.lang.String)
	 */
	@Override
	public BIObject loadBIObjectForDetail(String path) throws EMFUserError {
		logger.debug("IN");
		BIObject biObject = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = " from SbiObjects where path = '" + path + "'";
			String hql = " from SbiObjects where path = ? ";

			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setSerializable(0, path);

			SbiObjects hibObject = (SbiObjects) hqlQuery.uniqueResult();
			if (hibObject != null) {
				biObject = toBIObject(hibObject, aSession);
			} else {
				logger.warn("Unable to load document whose path is equal to [" + path + "]");
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Object for detail in path " + path, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
		}
		logger.debug("OUT");
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
	private void insertParametersDocComposition(BIObject biObject, ObjTemplate template, boolean flgDelete) throws EMFUserError {
		logger.debug("IN");
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
				List lstDocParameters = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(biObject.getId());
				for (int i = 0; i < lstDocParameters.size(); i++) {
					BIObjectParameter docParam = (BIObjectParameter) lstDocParameters.get(i);
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
				// BIObject docChild =
				// DAOFactory.getBIObjectDAO().loadBIObjectByLabel((String)lstLabeldDocs.get(i));
				BIObject docChild = loadBIObjectByLabel((String) lstLabeldDocs.get(i));

				if (docChild == null) {
					logger.error("Error while getting document child " + (String) lstLabeldDocs.get(i) + " for document composition.");
					List lstLabel = new ArrayList();
					lstLabel.add(lstLabeldDocs.get(i));
					throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", lstLabel, "component_spagobidocumentcompositionIE_messages");
				} else {
					List lstDocChildParameters = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(docChild.getId());
					for (int j = 0; j < lstDocChildParameters.size(); j++) {
						BIObjectParameter objPar = (BIObjectParameter) lstDocChildParameters.get(j);
						if (!totalParameters.contains(objPar.getLabel())) {
							SbiObjects aSbiObject = new SbiObjects();
							// aSbiObject.setBiobjId(biObject.getId());
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
							sbiObjPar.setReqFl(new Short(objPar.getRequired().shortValue()));
							sbiObjPar.setModFl(new Short(objPar.getModifiable().shortValue()));
							sbiObjPar.setViewFl(new Short(objPar.getVisible().shortValue()));
							sbiObjPar.setMultFl(new Short(objPar.getMultivalue().shortValue()));
							sbiObjPar.setProg(objPar.getProg());
							sbiObjPar.setPriority(new Integer(totalParameters.size() + 1));
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
			logger.error("Error inserting parameters in doc composition for BI Object " + biObject + ", Obj Template " + template + " and flag delete equals to " + flgDelete, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (EMFUserError eu) {
			throw eu;
		} catch (Exception e) {
			logger.error("Error while creating parameter for document composition.", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");

	}

	/**
	 * Called only for document composition (insert object modality). Puts parameters into the document composition getting these from document's children.
	 *
	 * @param biobjectId the document composition biobject id
	 * @throws EMFUserError
	 */
	private void insertParametersDocComposition(Integer biobjectId) throws EMFUserError {
		logger.debug("IN");
		// get informations about documents child
		Session aSession = null;
		Transaction tx = null;
		try {
			// gets document composition configuration
			ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobjectId);
			aSession = getSession();
			tx = aSession.beginTransaction();
			logger.debug("Template document composition in insert: " + template);
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
					logger.error("Error while getting document child " + (String) lstLabeldDocs.get(i) + " for document composition.");
					List lstLabel = new ArrayList();
					lstLabel.add(lstLabeldDocs.get(i));
					throw new EMFUserError(EMFErrorSeverity.ERROR, "1005", lstLabel, "component_spagobidocumentcompositionIE");
				} else {
					List lstDocChildParameters = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(docChild.getId());
					for (int j = 0; j < lstDocChildParameters.size(); j++) {
						BIObjectParameter objPar = (BIObjectParameter) lstDocChildParameters.get(j);
						if (!totalParameters.contains(objPar.getLabel())) {
							SbiObjects aSbiObject = new SbiObjects();
							// aSbiObject.setBiobjId(biObject.getId());
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
							sbiObjPar.setReqFl(new Short(objPar.getRequired().shortValue()));
							sbiObjPar.setModFl(new Short(objPar.getModifiable().shortValue()));
							sbiObjPar.setViewFl(new Short(objPar.getVisible().shortValue()));
							sbiObjPar.setMultFl(new Short(objPar.getMultivalue().shortValue()));
							sbiObjPar.setColSpan(objPar.getColSpan());
							sbiObjPar.setThickPerc(objPar.getThickPerc());
							sbiObjPar.setProg(objPar.getProg());
							sbiObjPar.setPriority(new Integer(totalParameters.size() + 1));
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
			logger.error("Error inserting parameters in doc composition for BI Object id " + biobjectId, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			logger.error("Error while creating parameter for document composition.", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");
	}

	@Override
	public List loadBIObjects(String type, String state, String folderPath) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			// Criteria criteria = aSession.createCriteria(SbiObjFunc.class);
			// if (type != null) {
			// Criterion typeCriterion =
			// Expression.eq("id.sbiObjects.objectTypeCode", type);
			// criteria.add(typeCriterion);
			// }
			// if (state != null) {
			// Criterion stateCriterion =
			// Expression.eq("id.sbiObjects.stateCode", state);
			// criteria.add(stateCriterion);
			// }
			// if (folderPath != null) {
			// Criterion folderPathCriterion =
			// Expression.eq("id.sbiFunctions.path", folderPath);
			// criteria.add(folderPathCriterion);
			// }
			// List hibList = criteria.list();

			StringBuilder buffer = new StringBuilder();
			if (folderPath != null) {
				buffer.append("select distinct(objects) from SbiObjects as objects, SbiObjFunc as objFuncs, SbiFunctions as functions "
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

			List hibList = query.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiObjects object = (SbiObjects) it.next();
				realResult.add(toBIObject(object, aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Objects by type " + type + ", state " + state + " and folder " + folderPath, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");
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
	public List loadBIObjects(Integer folderID, IEngUserProfile profile, boolean isPersonalFolder) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();

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
					logger.error("Error while recovering user profile", e);
				}

				if (folderID != null && roles != null && !roles.isEmpty()) {
					buffer.append("select distinct o from SbiObjects o, SbiObjFunc sof, SbiFunctions f,  SbiFuncRole fr "
							+ "where sof.id.sbiFunctions.functId = f.functId and o.biobjId = sof.id.sbiObjects.biobjId  "
							+ " and fr.id.role.extRoleId IN (select extRoleId from SbiExtRoles e  where  e.name in (:ROLES)) "
							+ " and fr.id.function.functId = f.functId " + " and f.functId = :FOLDER_ID  ");

					if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)) {
						buffer.append(" and (" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP + "' AND o.state.valueCd = '"
								+ SpagoBIConstants.DOC_STATE_DEV + "') OR" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST
								+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_TEST + "') OR " + "(fr.id.state.valueCd = '"
								+ SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE + "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_REL + "') OR "
								+ "o.stateCode = '" + SpagoBIConstants.DOC_STATE_SUSP + "'" + ") ");
					} else {
						buffer.append(" and (" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP + "' AND o.state.valueCd = '"
								+ SpagoBIConstants.DOC_STATE_DEV + "') OR" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST
								+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_TEST + "') OR " + "(fr.id.state.valueCd = '"
								+ SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE + "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_REL + "')"
								+ ") ");
					}

					if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
							&& !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
						// only visible objects (1 means true) and object
						// created by the current user
						buffer.append(" and ((o.visible = 0 and o.creationUser = '" + ((UserProfile) profile).getUserId() + "') OR (o.visible = 1)) ");
					}
					buffer.append(" order by o.name");
				} else {
					buffer.append("select objects from SbiObjects");
				}
			} else {
				if (folderID != null) {
					buffer.append("select distinct o from SbiObjects o, SbiObjFunc sof, SbiFunctions f "
							+ "where sof.id.sbiFunctions.functId = f.functId and o.biobjId = sof.id.sbiObjects.biobjId  " + " and f.functId = :FOLDER_ID  ");

					if (!profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)
							&& !profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)) {
						// only visible objects (1 means true) and object
						// created by the current user
						buffer.append(" and ((o.visible = 0 and o.creationUser = '" + ((UserProfile) profile).getUserId() + "') OR (o.visible = 1)) ");
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

			List hibList = query.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiObjects object = (SbiObjects) it.next();
				realResult.add(toBIObject(object, aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Objects for folder id " + folderID + ", suer profile " + profile +" and is personal folder equals to " + isPersonalFolder, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			logger.error("Error loading BI Objects for folder id " + folderID + ", suer profile " + profile +" and is personal folder equals to " + isPersonalFolder, e);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");
		return realResult;
	}

	@Override
	public List<BIObject> loadBIObjectsByLovId(Integer idLov) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery(
					"select distinct obj from   SbiObjects as obj " + "inner join obj.sbiObjPars as objPars " + "inner join objPars.sbiParameter as param "
							+ "inner join param.sbiParuses as paruses " + "inner join paruses.sbiLov as lov " + "where  lov.lovId = " + idLov);

			List hibList = hibQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject((SbiObjects) it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Objects by lov id " + idLov, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

			logger.debug("OUT");
		}
		return realResult;

	}

	@Override
	public List<BIObject> loadBIObjectsByParamterId(Integer idParameter) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("select distinct obj from   SbiObjects as obj " + "inner join obj.sbiObjPars as objPars "
					+ "inner join objPars.sbiParameter as param " + "where  param.parId = " + idParameter);

			List hibList = hibQuery.list();

			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				realResult.add(toBIObject((SbiObjects) it.next(), aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading BI Objects by paramter id " + idParameter, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

			logger.debug("OUT");
		}
		return realResult;
	}

	@Override
	public List<BIObject> loadAllBIObjectsBySearchKey(String searchKey, String attributes) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List result = new ArrayList();
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

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();
			Set<Integer> resultIds = new HashSet<>();
			while (it.hasNext()) {
				SbiObjects next = (SbiObjects) it.next();
				Integer id = next.getBiobjId();
				if (!resultIds.contains(id)) {
					resultIds.add(id);
					result.add(toBIObject(next, aSession));
				}
			}

			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading all BI Objects by search key " + searchKey + " and attributes " + attributes, he);
			if (tx != null) {
				tx.rollback();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}
			logger.debug("OUT");
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
	public List searchBIObjects(String valueFilter, String typeFilter, String columnFilter, String scope, Integer nodeFilter, IEngUserProfile profile)
			throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Collection roles = null;
			try {
				roles = ((UserProfile) profile).getRolesForUse();
				logger.debug("Profile roles: " + roles);

			} catch (Exception e) {
				logger.error("Error while recovering user profile", e);
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
				bufferWhere.append(" where sof.id.sbiFunctions.functId = f.functId and o.biobjId = sof.id.sbiObjects.biobjId and "
						+ " ((fr.id.role.extRoleId IN (select extRoleId from SbiExtRoles e  where  e.name in (:ROLES)) "
						+ " and fr.id.function.functId = f.functId and (" + "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP
						+ "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_DEV + "') OR" + "(fr.id.state.valueCd = '"
						+ SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST + "' AND o.state.valueCd = '" + SpagoBIConstants.DOC_STATE_TEST + "') OR "
						+ "(fr.id.state.valueCd = '" + SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE + "' AND o.state.valueCd = '"
						+ SpagoBIConstants.DOC_STATE_REL + "') " + ") ) OR  f.path like :PERSONAL_FOLDER_PREFIX )");

			}
			String operCondition = "";
			String likeStart = "";
			String likeEnd = "";
			if (valueFilter != null && !valueFilter.equals("") && typeFilter != null && !typeFilter.equals("") && columnFilter != null
					&& !columnFilter.equals("")) {
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
					 * }else if (NOT_ENDS_WITH.equalsIgnoreCase( typeFilter )) { operCondition = "NOT LIKE %:VALUE_FILTER"; } else if
					 * (NOT_CONTAINS.equalsIgnoreCase( typeFilter )) { operCondition = "NOT LIKE %:VALUE_FILTER%"; } else if (IS_NULL.equalsIgnoreCase(
					 * typeFilter )) { operCondition = "IS NULL"; } else if (NOT_NULL.equalsIgnoreCase( typeFilter )) { operCondition = "IS NOT NULL";
					 */
				} else {
					logger.error("The query Operator " + typeFilter + " is invalid.");
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

			String hql = bufferSelect.toString() + bufferFrom.toString() + bufferWhere.toString() + bufferOrder.toString();

			logger.debug("query hql: " + hql);

			Query query = aSession.createQuery(hql);

			query.setParameter("PERSONAL_FOLDER_PREFIX", "/" + ((UserProfile) profile).getUserId().toString() + "%");

			// setting query parameters
			query.setParameterList("ROLES", roles);
			logger.debug("Parameter value ROLES: " + roles);

			if (valueFilter != null) {
				if (!likeStart.equals("") || !likeEnd.equals("")) {
					query.setParameter("VALUE_FILTER", likeStart + valueFilter + likeEnd);
					logger.debug("Parameter value VALUE_FILTER: " + likeStart + valueFilter + likeEnd);
				} else {
					query.setParameter("VALUE_FILTER", valueFilter);
					logger.debug("Parameter value VALUE_FILTER: " + valueFilter);
				}
			}

			if (scope != null && scope.equals("node") && nodeFilter != null && !nodeFilter.equals("")) {
				query.setParameter("FOLDER_ID", nodeFilter);
				logger.debug("Parameter value FOLDER_ID: " + nodeFilter);
			}
			// executes query
			List hibList = query.list();
			Iterator it = hibList.iterator();
			while (it.hasNext()) {
				SbiObjects object = (SbiObjects) it.next();
				realResult.add(toBIObject(object, aSession));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error searching BI Objects by value filter " + valueFilter + ", type filter " + typeFilter + ", column filter " + columnFilter + ", scope " + scope + ", node filter " + nodeFilter + " and profile " + profile, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			logger.error("Error searching BI Objects by value filter " + valueFilter + ", type filter " + typeFilter + ", column filter " + columnFilter + ", scope " + scope + ", node filter " + nodeFilter + " and profile " + profile, e);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");
		return realResult;
	}

	@Override
	public Integer countBIObjects(String search, String user) throws EMFUserError {
		logger.debug("IN");
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
					hql += " label like '%" + search + "%'";
				}

				// if (search != null && user != null) {
				// hql += " and ";
				// }
				// if (user != null) {
				// hql += " creationUser='" + user + "'";
				// }

			}

			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of BIObjects", he);
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
	public List loadPagedObjectsList(Integer offset, Integer fetchSize) throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;

			String hql = "select count(*) from SbiObjects ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber) : resultNumber;
			}

			hibernateQuery = aSession.createQuery("from SbiObjects order by label");
			hibernateQuery.setFirstResult(offset);
			if (fetchSize > 0)
				hibernateQuery.setMaxResults(fetchSize);

			toTransform = hibernateQuery.list();

			Iterator it = toTransform.iterator();
			while (it.hasNext()) {
				SbiObjects object = (SbiObjects) it.next();
				toReturn.add(toBIObject(object, aSession));
			}
		} catch (HibernateException he) {
			logger.error("Error while loading the list of Resources", he);
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

	@Override
	public String changeLockStatus(String documentLabel, boolean isUserAdmin) throws EMFUserError {
		logger.debug("IN");
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

			if (isLocked == true && hibObject.getLockedByUser().equals(currentUser)) {
				hibObject.setLockedByUser(null);
				aSession.save(hibObject);
				tx.commit();
				toReturn = hibObject.getLockedByUser();
			} else if (isLocked == false) {
				// if its not lcked change
				hibObject.setLockedByUser(currentUser);
				aSession.save(hibObject);
				tx.commit();
				toReturn = hibObject.getLockedByUser();
			} else if (isLocked == true && !hibObject.getLockedByUser().equals(currentUser) && isUserAdmin == true) {
				hibObject.setLockedByUser(null);
				aSession.save(hibObject);
				tx.commit();
				toReturn = hibObject.getLockedByUser();
			} else {
				toReturn = null;
			}

		} catch (HibernateException he) {
			logger.error("Error changing lock status for document with label " + documentLabel + " and isUserAdmin value equals to " + isUserAdmin, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<BIObjectParameter> loadDocumentDrivers(BIObject biObject, String role) throws EMFUserError {
		logger.debug("IN");
		List toReturn = new ArrayList<BIObjectParameter>();
		Session aSession = getSession();
		Transaction tx = aSession.beginTransaction();
		try {
			String hql = "from SbiObjPar s where s.sbiObject.label = ? order by s.priority asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setString(0, biObject.getLabel());
			List hibObjectPars = hqlQuery.list();
			Iterator it = hibObjectPars.iterator();
			BIObjectParameterDAOHibImpl aBIObjectParameterDAOHibImpl = new BIObjectParameterDAOHibImpl();
			IParameterDAO aParameterDAO = DAOFactory.getParameterDAO();
			int count = 1;
			while (it.hasNext()) {
				SbiObjPar hibObjPar = (SbiObjPar) it.next();
				AbstractDriver tmpBIObjectParameter = aBIObjectParameterDAOHibImpl.toBIObjectParameter(hibObjPar);
				recalculateParameterPriority(aSession, biObject, tmpBIObjectParameter, aBIObjectParameterDAOHibImpl, count);
				Parameter aParameter = aParameterDAO.loadForExecutionByParameterIDandRoleName(tmpBIObjectParameter.getParID(), role, false);
				tmpBIObjectParameter.setParID(aParameter.getId());
				tmpBIObjectParameter.setParameter(aParameter);
				toReturn.add(tmpBIObjectParameter);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error loading document drivers for BI Object id " + biObject + " and role " + role, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null && aSession.isOpen()) {
				aSession.close();
			}

		}
		logger.debug("OUT");
		return toReturn;
	}

	private void recalculateParameterPriority(Session session, BIObject biObject, AbstractDriver parameter, BIObjectParameterDAOHibImpl dao, int count) {
		Integer priority = parameter.getPriority();
		if (priority == null || priority.intValue() != count) {
			logger.warn(
					"The priorities of the biparameters for the document with id = " + biObject.getId() + " are not sorted. Priority recalculation starts.");
			dao.recalculateBiParametersPriority(biObject.getId(), session);
			parameter.setPriority(new Integer(count));
		}
	}

	private List<BIObjectParameter> getDatasetDrivers(BIObject biObject, String role) {
		List toReturn = new ArrayList<BIObjectParameter>();
		List qbeDatasetDrivers = null;

		IBIMetaModelParameterDAO driversDao = DAOFactory.getBIMetaModelParameterDAO();
		IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
		IBIObjDataSetDAO biObjDataSetDAO = DAOFactory.getBIObjDataSetDAO();
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();

		try {
			ArrayList<BIObjDataSet> biObjDataSetList = biObjDataSetDAO.getBiObjDataSets(biObject.getId());
			Iterator itDs = biObjDataSetList.iterator();
			while (itDs.hasNext()) {
				BIObjDataSet biObjDataSet = (BIObjDataSet) itDs.next();
				Integer dsId = biObjDataSet.getDataSetId();
				IDataSet dataset = datasetDao.loadDataSetById(dsId);
				dataset = dataset instanceof VersionedDataSet ? ((VersionedDataSet) dataset).getWrappedDataset() : dataset;
				if (dataset != null && dataset.getDsType().equals("SbiQbeDataSet")) {
					JSONObject jsonConfig = new JSONObject(dataset.getConfiguration());
					String businessModelName = jsonConfig.getString("qbeDatamarts");
					MetaModel businessModel = businessModelsDAO.loadMetaModelByName(businessModelName);
					qbeDatasetDrivers = driversDao.loadBIMetaModelParameterByMetaModelId(businessModel.getId());
					// in a document i can use only one qbe dataset WITH DRIVERS
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't retrieve drivers from meta model", e);
		}

		if (qbeDatasetDrivers != null) {
			Iterator iter = qbeDatasetDrivers.iterator();
			while (iter.hasNext()) {
				BIMetaModelParameter datasetDriver = (BIMetaModelParameter) iter.next();
				BIObjectParameter documentDriver = transformDSDrivertoBIObjectParameter(datasetDriver, biObject, role);
				toReturn.add(documentDriver);
			}
		}
		return toReturn;
	}

	public BIObjectParameter transformDSDrivertoBIObjectParameter(BIMetaModelParameter datasetDriver, BIObject biObject, String role) {
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
			logger.error("Error transforming DS Driver to BI Object Parameter the driver " + String.valueOf(datasetDriver) + " of BI Object " + String.valueOf(biObject) + " and role " + String.valueOf(role), e);
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
					outputParameter.setParameterTypeId(getDefaultParameterMap().get(defaultOutputParameter.getParamType()).getValueId());
					updateSbiCommonInfo4Insert(outputParameter);
					ret.add(outputParameter);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				logger.error("Error trying to load default output parameters for engine [" + sbiEngines.getDriverNm() + "] ", e);
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
				IEngineDriver driver = (IEngineDriver) Class.forName(sbiObject.getSbiEngines().getDriverNm()).newInstance();
				List<DefaultOutputParameter> params = driver.getSpecificOutputParameters(categories);
				for (DefaultOutputParameter defaultOutputParameter : params) {
					SbiOutputParameter outputParameter = new SbiOutputParameter();
					outputParameter.setBiobjId(sbiObject.getBiobjId());
					outputParameter.setLabel(defaultOutputParameter.getParamName());
					outputParameter.setParameterTypeId(getDefaultParameterMap().get(defaultOutputParameter.getParamType()).getValueId());
					updateSbiCommonInfo4Insert(outputParameter);
					ret.add(outputParameter);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				logger.error("Error trying to load default output parameters for engine [" + sbiObject.getSbiEngines().getDriverNm() + "] ", e);
			}
		}
		return ret;
	}

	/**
	 * Method used for special chart types, that need exclusion of some of default output parameters. Example: WORDCLOUD, PARALLEL and CHORD chart types.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	private List<SbiOutputParameter> loadDriverSpecificOutputParameters(SbiObjects sbiObject, String specificChartType) {
		List<SbiOutputParameter> ret = new ArrayList<>();
		if (sbiObject.getSbiEngines() != null) {
			try {
				IEngineDriver driver = (IEngineDriver) Class.forName(sbiObject.getSbiEngines().getDriverNm()).newInstance();
				List<DefaultOutputParameter> params = driver.getSpecificOutputParameters(specificChartType);
				for (DefaultOutputParameter defaultOutputParameter : params) {
					SbiOutputParameter outputParameter = new SbiOutputParameter();
					outputParameter.setBiobjId(sbiObject.getBiobjId());
					outputParameter.setLabel(defaultOutputParameter.getParamName());
					outputParameter.setParameterTypeId(getDefaultParameterMap().get(defaultOutputParameter.getParamType()).getValueId());
					updateSbiCommonInfo4Insert(outputParameter);
					ret.add(outputParameter);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				logger.error("Error trying to load default output parameters for engine [" + sbiObject.getSbiEngines().getDriverNm() + "] ", e);
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
				logger.error("Unable to load PAR_TYPE domains", e);
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

	private Set<String> getUserRoles(Session aSession, Collection<String> userRoles, Set<String> allRolesWithPermission) {
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
		String roleTHql = "select roles.roleTypeCode from SbiExtRoles as roles where roles.name = '" + role + "'";
		Query roleHqlQuery = aSession.createQuery(roleTHql);
		return (String) roleHqlQuery.uniqueResult();
	}

	private boolean isTestRole(String roleType) {
		return SpagoBIConstants.ROLE_TYPE_TEST.equals(roleType);
	}

	private List filterUsableRolesByObjectParameters(Integer id, List correctRoles, Session aSession, Set<String> userRolesWithPermission) {
		Query hqlQuery;
		String hql;
		// find all id parameters relative to the objects
		hql = "select par.parId from " + "SbiParameters as par, SbiObjects as obj, SbiObjPar as objpar  " + "where obj.biobjId = ?  and "
				+ "      obj.biobjId = objpar.sbiObject.biobjId and " + "      par.parId = objpar.id.sbiParameter.parId ";
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
					hql = "select puseDet.id.sbiParuse.useId"
							+ " from SbiParuse as puse,"
							+ "     SbiParuseDet as puseDet,"
							+ "     SbiExtRoles as rol"
							+ " where rol.name = '" + role + "'"
							+ "  and puseDet.id.sbiExtRoles.extRoleId = rol.extRoleId"
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
					logger.debug("There is one  available modality for role [" + role + "] on parameter [" + idPar + "]");
				} else {
					logger.debug("There is no modality available for role [" + role + "] on parameter [" + idPar + "]");
				}
			}
		}
		return correctRoles;
	}

}
