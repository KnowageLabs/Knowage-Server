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
package it.eng.spagobi.tools.dataset.dao;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.federateddataset.dao.SbiFederationDefinitionDAOHibImpl;
import it.eng.spagobi.federateddataset.dao.SbiFederationUtils;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaDsBc;
import it.eng.spagobi.metadata.metadata.SbiMetaDsBcId;
import it.eng.spagobi.metadata.metadata.SbiMetaObjDs;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.event.DataSetEventManager;
import it.eng.spagobi.tools.dataset.exceptions.DatasetException;
import it.eng.spagobi.tools.dataset.exceptions.DatasetInUseException;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.glossary.metadata.SbiGlDataSetWlist;
import it.eng.spagobi.tools.tag.SbiDatasetTag;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * Implement CRUD operations over spagobi datsets
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataSetDAOImpl extends AbstractHibernateDAO implements IDataSetDAO {

	private static Logger logger = Logger.getLogger(DataSetDAOImpl.class);
	private static final String OWNER = "owner";
	private static final String IDS_CAT = "idsCat";
	private static final String VALUE_ID = "valueId";
	private static final String SHARED = "shared";
	private static final String ENTERPRISE = "enterprise";

	// ========================================================================================
	// READ operations (cRud)
	// ========================================================================================

	/**
	 * copy a dataset
	 *
	 * @param hibDataSet
	 * @return
	 */

	@Override
	public SbiDataSet copyDataSet(SbiDataSet hibDataSet) {

		logger.debug("IN");
		SbiDataSet hibNew = hibDataSet;

		/*
		 * SbiDataSet hibNew = null;
		 *
		 * if(hibDataSet instanceof SbiFileDataSet){ hibNew = new SbiFileDataSet(); ((SbiFileDataSet)hibNew).setFileName(((SbiFileDataSet)hibDataSet).getFileName()); }
		 *
		 * if(hibDataSet instanceof SbiQueryDataSet){ hibNew = new SbiQueryDataSet(); ((SbiQueryDataSet)hibNew).setQuery(((SbiQueryDataSet)hibDataSet).getQuery());
		 * ((SbiQueryDataSet)hibNew).setQueryScript(((SbiQueryDataSet)hibDataSet).getQueryScript());
		 * ((SbiQueryDataSet)hibNew).setQueryScriptLanguage(((SbiQueryDataSet)hibDataSet).getQueryScriptLanguage()); }
		 *
		 * if(hibDataSet instanceof SbiWSDataSet){ hibNew = new SbiWSDataSet(); ((SbiWSDataSet)hibNew ).setAdress(((SbiWSDataSet)hibDataSet).getAdress());
		 * ((SbiWSDataSet)hibNew ).setOperation(((SbiWSDataSet)hibDataSet).getOperation()); }
		 *
		 * if(hibDataSet instanceof SbiScriptDataSet){ hibNew =new SbiScriptDataSet(); ((SbiScriptDataSet) hibNew
		 * ).setScript(((SbiScriptDataSet)hibDataSet).getScript()); ((SbiScriptDataSet) hibNew ).setLanguageScript(((SbiScriptDataSet)hibDataSet).getLanguageScript());
		 *
		 * }
		 *
		 * if(hibDataSet instanceof SbiJClassDataSet){ hibNew =new SbiJClassDataSet(); ((SbiJClassDataSet) hibNew
		 * ).setJavaClassName(((SbiJClassDataSet)hibDataSet).getJavaClassName()); }
		 *
		 * if(hibDataSet instanceof SbiCustomDataSet){ hibNew =new SbiCustomDataSet(); ((SbiCustomDataSet) hibNew
		 * ).setCustomData(((SbiCustomDataSet)hibDataSet).getCustomData()); ((SbiCustomDataSet) hibNew
		 * ).setJavaClassName(((SbiCustomDataSet)hibDataSet).getJavaClassName()); }
		 *
		 * if(hibDataSet instanceof SbiQbeDataSet){ hibNew =new SbiQbeDataSet(); ((SbiQbeDataSet) hibNew ).setSqlQuery(((SbiQbeDataSet)hibDataSet).getSqlQuery());
		 * ((SbiQbeDataSet) hibNew ).setJsonQuery(((SbiQbeDataSet)hibDataSet).getJsonQuery()); ((SbiQbeDataSet) hibNew
		 * ).setDataSource(((SbiQbeDataSet)hibDataSet).getDataSource()); ((SbiQbeDataSet) hibNew ).setDatamarts(((SbiQbeDataSet)hibDataSet).getDatamarts());
		 *
		 *
		 * }
		 *
		 * hibNew.setCategory(hibDataSet.getCategory()); hibNew.setDsMetadata(hibDataSet.getDsMetadata()); hibNew.setMetaVersion(hibDataSet.getMetaVersion());
		 * hibNew.setParameters(hibDataSet.getParameters()); hibNew.setPivotColumnName(hibDataSet.getPivotColumnName());
		 * hibNew.setPivotColumnValue(hibDataSet.getPivotColumnValue()); hibNew.setPivotRowName(hibDataSet.getPivotRowName());
		 * hibNew.setTransformer(hibDataSet.getTransformer()); hibNew.setSbiVersionIn(hibDataSet.getSbiVersionIn()); hibNew.setUserIn(hibDataSet.getUserIn());
		 * hibNew.setTimeIn(hibDataSet.getTimeIn()); hibNew.setVersionNum(hibDataSet.getVersionNum()); hibNew.setDsHId(hibDataSet.getDsHId());
		 */
		logger.debug("OUT");
		return hibNew;
	}

	/**
	 * Counts number of BIObj associated.
	 *
	 * @param dsId the ds id
	 * @return Integer, number of BIObj associated
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public Integer countBIObjAssociated(Integer dsId) {
		logger.debug("IN");
		Integer resultNumber = 0;
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "select count(*) from SbiObjDataSet s where s.dsId = ? ";
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, dsId.intValue());
			resultNumber = ((Long) aQuery.uniqueResult()).intValue();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"Error while getting the objects associated with the data set with id " + dsId, e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return resultNumber;
	}

	/**
	 * Counts number of existent DataSets
	 *
	 * @return Integer, number of existent DataSets
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public Integer countDatasets() {
		logger.debug("IN");
		Session session = null;
		Transaction transaction = null;
		Long resultNumber;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			List<Domain> devCategories = new LinkedList<>();
			boolean isDev = fillDevCategories(devCategories);
			if (isDev) {
				List idsCat = createIdsCatogriesList(devCategories);
				String owner = ((UserProfile) getUserProfile()).getUserId().toString();
				Query countQuery;
				if (idsCat == null || idsCat.isEmpty()) {
					countQuery = session
							.createQuery("select count(*) from SbiDataSet sb where sb.active = ? and owner = :owner");
					countQuery.setBoolean(0, true);
					countQuery.setString(OWNER, owner);
				} else {
					countQuery = session.createQuery(
							"select count(*) from SbiDataSet sb where sb.active = ? and (sb.category.id IN (:idsCat) or owner = :owner)");
					countQuery.setBoolean(0, true);
					countQuery.setParameterList(IDS_CAT, idsCat);
					countQuery.setString(OWNER, owner);
				}

				resultNumber = (Long) countQuery.uniqueResult();
			} else {
				String hql = "select count(*) from SbiDataSet ds where ds.active = ? ";
				Query hqlQuery = session.createQuery(hql);
				hqlQuery.setBoolean(0, true);
				resultNumber = (Long) hqlQuery.uniqueResult();
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("Error while loading the list of SbiDataSet", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return resultNumber.intValue();
	}

	/**
	 * Delete all inactive versions of dataset whose id is equal to <code>datasetId</code>
	 *
	 * @param datasetId the id of the of the dataset whose incative version must be deleted
	 * @return true if the incative versions of dataset whose id is equal to <code>datasetId</code> have been succesfully deleted from database. false otherwise
	 *         (i.e. the dtaset does not have any inactive versions)
	 */
	@Override
	public boolean deleteAllInactiveDataSetVersions(Integer datasetId) {
		Session session;
		Transaction transaction;
		boolean deleted;

		logger.debug("IN");

		session = null;
		transaction = null;
		deleted = false;
		try {
			if (datasetId == null) {
				throw new IllegalArgumentException("Input parameter [datasetId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query query = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			query.setBoolean(0, false);
			query.setInteger(1, datasetId);

			List toBeDeleted = query.list();

			if (toBeDeleted != null && !toBeDeleted.isEmpty()) {
				Iterator it = toBeDeleted.iterator();
				while (it.hasNext()) {
					SbiDataSet sbiDataSet = (SbiDataSet) it.next();
					if (sbiDataSet != null && !sbiDataSet.isActive()) {
						session.delete(sbiDataSet);
					}
				}
				transaction.commit();
				deleted = true;
			}

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while deleting inactive versions of dataset "
					+ "whose id is equal to [" + datasetId + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return deleted;
	}

	/**
	 * Delete data set whose ID is equal to <code>datasetId</code> if it is not referenced by some analytical documents.
	 *
	 * @param datasetId the ID of the dataset to delete. Cannot be null.
	 * @throws SpagoBIDAOException if the dataset is referenced by at least one analytical document
	 */
	@Override
	public void deleteDataSet(Integer datasetId) {
		Session session;
		Transaction transaction;
		Integer derivedFederationId = null;
		logger.debug("IN");

		session = null;
		transaction = null;

		try {
			if (datasetId == null) {
				throw new IllegalArgumentException("Input parameter [datasetId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// check if dataset is used by document by querying SBI_OBJ_DATA_SET table
			List<BIObject> objectsAssociated = DAOFactory.getBIObjDataSetDAO().getBIObjectsUsingDataset(datasetId,
					session);
			if (!objectsAssociated.isEmpty()) {
				for (Iterator iterator = objectsAssociated.iterator(); iterator.hasNext();) {
					BIObject biObject = (BIObject) iterator.next();
					logger.debug(
							"Dataset with id " + datasetId + " is used by BiObject with label " + biObject.getLabel());
				}
			}

			// check if dataset is used by document by querying SBI_OBJ_DATA_SET table
			List<FederationDefinition> federationsAssociated = DAOFactory.getFedetatedDatasetDAO()
					.loadFederationsUsingDataset(datasetId, session);

			if (!federationsAssociated.isEmpty()) {

				// check if its a derived dataset.. In this case delete also the federation..
				for (Iterator iterator = federationsAssociated.iterator(); iterator.hasNext();) {
					FederationDefinition fedDef = (FederationDefinition) iterator.next();
					logger.debug(
							"Dataset with id " + datasetId + " is used by Federation with label " + fedDef.getLabel());
				}

			}

			boolean bLovs = hasBILovAssociated(String.valueOf(datasetId));
			if (!objectsAssociated.isEmpty() || !federationsAssociated.isEmpty() || bLovs) {
				String message = "[deleteInUseDSError]: Dataset with id [" + datasetId + "] "
						+ "cannot be erased because it is referenced by documents or federations or lovs.";
				DatasetInUseException diue = new DatasetInUseException(message);
				diue.setLov(bLovs);
				ArrayList<String> objs = new ArrayList<>();
				for (int i = 0; i < objectsAssociated.size(); i++) {
					BIObject obj = objectsAssociated.get(i);
					objs.add(obj.getLabel());
				}
				diue.setObjectsLabel(objs);
				ArrayList<String> federations = new ArrayList<>();
				for (int i = 0; i < federationsAssociated.size(); i++) {
					FederationDefinition fedDef = federationsAssociated.get(i);
					federations.add(fedDef.getLabel());
				}
				diue.setFederationsLabel(federations);

				throw diue;
			}

			// deletes all versions of the dataset specified
			Query hibernateQuery = session.createQuery("from SbiDataSet h where h.id.dsId = ? ");
			hibernateQuery.setInteger(0, datasetId);
			List<SbiDataSet> sbiDataSetList = hibernateQuery.list();
			for (SbiDataSet sbiDataSet : sbiDataSetList) {
				if (sbiDataSet != null) {
					IDataSet toReturn = DataSetFactory.toDataSet(sbiDataSet, this.getUserProfile());
					session.delete(sbiDataSet);

					FederationDefinition fd = toReturn.getDatasetFederation();
					if (fd != null && fd.isDegenerated()) {
							logger.debug("The datset is derived so there is a linked federation");
							SbiFederationDefinitionDAOHibImpl dao = (SbiFederationDefinitionDAOHibImpl) DAOFactory
									.getFedetatedDatasetDAO();
							dao.deleteFederatedDatasetById(fd.getFederation_id(), session);
							logger.debug("Deleted the linked federation with id" + derivedFederationId);
					}

					// deletes all relations with the business class
					Integer intDsId = datasetId;
					List<SbiMetaDsBc> lstBcs = DAOFactory.getSbiDsBcDAO().loadBcByDsIdAndTenant(intDsId,
							sbiDataSet.getId().getOrganization());
					for (SbiMetaDsBc dsBc : lstBcs) {
						DAOFactory.getSbiDsBcDAO().deleteDsBc(dsBc);
					}

					List<SbiMetaObjDs> listObjDs = DAOFactory.getSbiObjDsDAO().loadObjByDsId(intDsId);
					for (SbiMetaObjDs objDs : listObjDs) {
						DAOFactory.getSbiObjDsDAO().deleteObjDs(objDs);
					}

					// delete Tags associated to Dataset
					List<SbiDatasetTag> dsTags = DAOFactory.getSbiTagDao().loadDatasetTags(intDsId);
					for (SbiDatasetTag dsTag : dsTags) {
						DAOFactory.getSbiTagDao().deleteDatasetTag(dsTag);
					}

					DataSetEventManager.getINSTANCE().notifyDelete(toReturn);
				}
				// if dataset is of type FILE, delete associated file as well
				if (sbiDataSet.getType().equals(DataSetConstants.DS_FILE)) {
					deleteFileAssociatedToDataset(sbiDataSet);
				}
			}

			List<SbiGlDataSetWlist> glWlistToDelete = session.createCriteria(SbiGlDataSetWlist.class)
					.add(Restrictions.eq("id.datasetId", datasetId)).list();
			for (SbiGlDataSetWlist sbiGlDataSetWlist : glWlistToDelete) {
				session.delete(sbiGlDataSetWlist);
			}

			transaction.commit();

		}

		catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			if (e instanceof DatasetException) {
				DatasetException de = (DatasetException) e;
				throw de;
			} else {
				String msg = (e.getMessage() != null) ? e.getMessage()
						: "An unexpected error occured while deleting dataset " + "whose id is equal to [" + datasetId
								+ "]";
				throw new SpagoBIDAOException(msg, e);
			}
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Delete data set whose ID is equal to <code>datasetId</code> ALSO if is referenced by some analytical documents.
	 *
	 * @param datasetId the ID of the dataset to delete. Cannot be null.
	 */
	@Override
	public void deleteDataSetNoChecks(Integer datasetId) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;

		try {
			if (datasetId == null) {
				throw new IllegalArgumentException("Input parameter [datasetId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// deletes all versions of the dataset specified
			Query hibernateQuery = session.createQuery("from SbiDataSet h where h.id.dsId = ? ");
			hibernateQuery.setInteger(0, datasetId);
			List<SbiDataSet> sbiDataSetList = hibernateQuery.list();
			for (SbiDataSet sbiDataSet : sbiDataSetList) {
				if (sbiDataSet != null) {
					IDataSet toReturn = DataSetFactory.toDataSet(sbiDataSet, this.getUserProfile());
					session.delete(sbiDataSet);
					DataSetEventManager.getINSTANCE().notifyDelete(toReturn);
				}
			}

			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (e.getMessage() != null) ? e.getMessage()
					: "An unexpected error occured while deleting dataset " + "whose id is equal to [" + datasetId
							+ "]";
			throw new SpagoBIDAOException(msg, e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Delete the dataset version whose id is equal to <code>datasetVersionId</code> if and only if it is inactive.
	 *
	 * @param datasetVersionId the id of the version of the dataset to delete. Cannot be null.
	 * @return true if the version whose id is equal to <code>datasetVersionId</code> is deleted from database. false otherwise (the version does not exist or it
	 *         exists but it is active).
	 */
	@Override
	public boolean deleteInactiveDataSetVersion(Integer datasetVersionNum, Integer dsId) {
		Session session;
		Transaction transaction;
		boolean deleted;

		logger.debug("IN");

		session = null;
		transaction = null;
		deleted = false;

		try {

			if (datasetVersionNum == null) {
				throw new IllegalArgumentException("Input parameter [datasetVersionNum] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// SbiDataSet sbiDataSet = (SbiDataSet) session.load(SbiDataSet.class, datasetVersionId);
			Query countQuery = session
					.createQuery("from SbiDataSet ds where ds.active = ? and ds.id.versionNum = ? and ds.id.dsId = ?");
			countQuery.setBoolean(0, false);
			countQuery.setInteger(1, datasetVersionNum);
			countQuery.setInteger(2, dsId);
			SbiDataSet sbiDataSet = (SbiDataSet) countQuery.uniqueResult();
			if (sbiDataSet != null && !sbiDataSet.isActive()) {
				session.delete(sbiDataSet);
				transaction.commit();
				deleted = true;
			}

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while deleting dataset version"
					+ "whose version num is equal to [" + datasetVersionNum + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return deleted;
	}

	/**
	 * Returns the Higher Version Number of a selected DS
	 *
	 * @param dsId the a data set ID
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public Integer getHigherVersionNumForDS(Integer dsId) {
		logger.debug("IN");
		Session session = null;
		Transaction transaction = null;
		Integer toReturn = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			if (dsId != null) {
				Query hibQuery = session
						.createQuery("select max(h.id.versionNum) from SbiDataSet h where h.id.dsId = ?");
				hibQuery.setInteger(0, dsId);
				toReturn = (Integer) hibQuery.uniqueResult();
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"Error while modifing the data Set with id " + ((dsId == null) ? "" : String.valueOf(dsId)), e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Checks for bi lovs associated.
	 *
	 * @param dsId the ds id
	 * @return true, if checks for lovs associated
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.dataSet.dao.IDataSetDAO#hasBIObjAssociated(java.lang.String)
	 */
	@Override
	public boolean hasBILovAssociated(String dsId) {
		logger.debug("IN");
		boolean bool = false;

		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			Integer dsIdInt = Integer.valueOf(dsId);

			String hql = " from SbiLov s where datasetId = ?";
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, Integer.parseInt(dsId));
			List biLovAssocitedWithDs = aQuery.list();
			bool = !biLovAssocitedWithDs.isEmpty();
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("Error while getting the lovs associated with the data set with id " + dsId, e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return bool;
	}

	/**
	 * Checks for bi obj associated.
	 *
	 * @param dsId the ds id
	 * @return true, if checks for bi obj associated
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.dataSet.dao.IDataSetDAO#hasBIObjAssociated(java.lang.String)
	 */
	// @Override
	// public boolean hasBIObjAssociated(String dsId) {
	// logger.debug("IN");
	// boolean bool = false;
	//
	// Session session = null;
	// Transaction transaction = null;
	// try {
	// session = getSession();
	// transaction = session.beginTransaction();
	// Integer dsIdInt = Integer.valueOf(dsId);
	//
	// String hql = " from SbiObjects s where s.dataSet = ?";
	// Query aQuery = session.createQuery(hql);
	// aQuery.setInteger(0, dsIdInt.intValue());
	// List biObjectsAssocitedWithDs = aQuery.list();
	// if (biObjectsAssocitedWithDs.size() > 0)
	// bool = true;
	// else
	// bool = false;
	// transaction.commit();
	// } catch (Throwable t) {
	// if (transaction != null && transaction.isActive()) {
	// transaction.rollback();
	// }
	// throw new SpagoBIDOAException("Error while getting the objects associated with the data set with id " + dsId, t);
	// } finally {
	// if (session != null && session.isOpen()) {
	// session.close();
	// }
	// logger.debug("OUT");
	// }
	// return bool;
	// }

	@Override
	public boolean hasBIObjAssociated(String dsId) {
		logger.debug("IN");

		boolean toReturn = false;

		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			Integer dsIdInt = Integer.valueOf(dsId);

			List<BIObject> objectsAssociated = DAOFactory.getBIObjDataSetDAO().getBIObjectsUsingDataset(dsIdInt,
					session);

			if (objectsAssociated.isEmpty()) {
				toReturn = false;
			} else {
				toReturn = true;
			}

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"Error while getting the objects associated with the data set with id " + dsId, e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public Integer insertDataSet(IDataSet dataSet) {
		logger.debug("IN");
		Integer toReturn = insertDataSet(dataSet, null);
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Insert data set.
	 *
	 * @param dataSet the a data set
	 * @see it.eng.spagobi.tools.dataset.dao.IDataSetDAO#insertDataSet(it.eng.spagobi.tools.dataset.bo.AbstractDataSet)
	 */
	@Override
	public Integer insertDataSet(IDataSet dataSet, Session optionalDbSession) {
		Integer idToReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		boolean keepPreviousSession = (optionalDbSession != null);

		idToReturn = null;
		session = null;
		transaction = null;
		try {

			if (!keepPreviousSession) {
				try {
					session = getSession();
					Assert.assertNotNull(session, "session cannot be null");
					transaction = session.beginTransaction();
					Assert.assertNotNull(transaction, "transaction cannot be null");
				} catch (Exception e) {
					throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
				}
			} else {
				session = optionalDbSession;
			}

			SbiDomains transformer = null;
			if (dataSet.getTransformerId() != null) {
				Criterion aCriterion = Restrictions.eq(VALUE_ID, dataSet.getTransformerId());
				Criteria criteria = session.createCriteria(SbiDomains.class);
				criteria.add(aCriterion);

				transformer = (SbiDomains) criteria.uniqueResult();

				if (transformer == null) {
					throw new SpagoBIDAOException(
							"The Domain with value_id= " + dataSet.getTransformerId() + " does not exist");
				}
			}

			SbiCategory category = null;
			if (dataSet.getCategoryId() != null) {
				ICategoryDAO categoryDAO = DAOFactory.getCategoryDAO();

				category = categoryDAO.getCategory(session, dataSet.getCategoryId());

				if (category == null) {
					throw new SpagoBIDAOException(
							"The Domain with value_id= " + dataSet.getCategoryId() + " does not exist");
				}
			}
			SbiDomains scope = null;
			if (dataSet.getScopeId() != null) {
				Criterion aCriterion = Restrictions.eq(VALUE_ID, dataSet.getScopeId());
				Criteria criteria = session.createCriteria(SbiDomains.class);
				criteria.add(aCriterion);
				scope = (SbiDomains) criteria.uniqueResult();
				if (scope == null) {
					throw new SpagoBIDAOException(
							"The Domain with value_id= " + dataSet.getScopeId() + " does not exist");
				}
			} else if (dataSet.getScopeId() == null && dataSet.getScopeCd() != null) {
				Criterion aCriterion = Restrictions.eq("valueCd", dataSet.getScopeCd());
				Criterion aCriterion2 = Restrictions.eq("domainCd", "DS_SCOPE");
				Criteria criteria = session.createCriteria(SbiDomains.class);
				criteria.add(aCriterion);
				criteria.add(aCriterion2);
				scope = (SbiDomains) criteria.uniqueResult();
				if (scope == null) {
					throw new SpagoBIDAOException(
							"The Domain with value_cd= " + dataSet.getScopeCd() + " does not exist");
				}
			}
			SbiDataSetId compositeKey = getDataSetKey(session, dataSet, true);
			SbiDataSet hibDataSet = new SbiDataSet(compositeKey);

			Date currentTStamp = new Date();
			hibDataSet.setLabel(dataSet.getLabel());
			hibDataSet.setDescription(dataSet.getDescription());
			hibDataSet.setName(dataSet.getName());

			// TODO fix this!!!! the same method for dsType is used with 2 set of values: Qbe, File, .... and SbiQbeDataSet, SbiFileDataSet, ....!!!!!
			String type = dataSet.getDsType();
			if (DataSetConstants.name2Code.containsKey(type)) {
				type = DataSetConstants.name2Code.get(type);
			}
			hibDataSet.setScope(scope);
			hibDataSet.setType(type);
			updateSbiCommonInfo4Insert(hibDataSet);

			String userIn = hibDataSet.getCommonInfo().getUserIn();
			String sbiVersionIn = hibDataSet.getCommonInfo().getSbiVersionIn();
			hibDataSet.setUserIn(userIn);
			hibDataSet.setSbiVersionIn(sbiVersionIn);
			hibDataSet.getId().changeVersionNum(1);
			hibDataSet.getId().setOrganization(hibDataSet.getCommonInfo().getOrganization());
			hibDataSet.setTimeIn(currentTStamp);
			// hibDataSet.setOrganization(hibDataSet.getCommonInfo().getOrganization());
			hibDataSet.setConfiguration(dataSet.getConfiguration());
			hibDataSet.setActive(true);

			hibDataSet.setTransformer(transformer);

			hibDataSet.setPersisted(dataSet.isPersisted());
			hibDataSet.setPersistedHDFS(dataSet.isPersistedHDFS());
			hibDataSet.setPersistTableName(dataSet.getPersistTableName());

			hibDataSet.setCategory(category);
			hibDataSet.setParameters(dataSet.getParameters());
			hibDataSet.setDsMetadata(dataSet.getDsMetadata());

			// save teh federations
			if (dataSet.getDatasetFederation() != null) {
				SbiFederationDefinition federationDefinition;
				if (dataSet.getDatasetFederation().getFederation_id() < 0) {
					logger.debug("The federation is not saved.. Adding it");
					// adding the tenant to the dataset... It has been lost because the service doesn't pass the value
					dataSet.getDatasetFederation().getSourceDatasets().iterator().next().setOrganization(getTenant());

					SbiFederationDefinitionDAOHibImpl dao = (SbiFederationDefinitionDAOHibImpl) DAOFactory
							.getFedetatedDatasetDAO();
					federationDefinition = dao.saveSbiFederationDefinition(dataSet.getDatasetFederation(), false,
							session, transaction);

					dataSet.getDatasetFederation().setFederation_id(federationDefinition.getFederation_id());
					logger.debug("New federation created with id " + federationDefinition.getFederation_id());

				} else {
					federationDefinition = SbiFederationUtils.toSbiFederatedDataset(dataSet.getDatasetFederation());

				}
				hibDataSet.setFederation(federationDefinition);

			}

			if (dataSet.getOwner() == null) {
				hibDataSet.setOwner(userIn);
			} else {
				hibDataSet.setOwner(dataSet.getOwner());
			}

			session.save(hibDataSet);

			idToReturn = hibDataSet.getId().getDsId();

			if (!keepPreviousSession && transaction != null) {
				transaction.commit();
			}

			if (type.equals("SbiQbeDataSet")) {
				// insert relations between qbe dataset and bc
				// insertQbeRelations(dataSet);
				insertQbeRelations(hibDataSet);
			}

			DataSetEventManager.getINSTANCE().notifyInsert(dataSet);

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while inserting dataset", e);
		} finally {
			if (!keepPreviousSession && session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return idToReturn;
	}


	@Override
	public IDataSet loadDataSetById(Integer id) {
		IDataSet toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			if (id == null) {
				throw new IllegalArgumentException("Input parameter [id] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			hibQuery.setBoolean(0, true);
			hibQuery.setInteger(1, id);
			SbiDataSet dsActiveDetail = (SbiDataSet) hibQuery.uniqueResult();
			if (dsActiveDetail != null) {
				toReturn = DataSetFactory.toDataSet(dsActiveDetail, this.getUserProfile());
			} else {
				logger.debug("Impossible to load dataset with id [" + id + "].");
			}
			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose id is equal to [" + id + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public IDataSet loadDataSetByLabel(String label) {
		IDataSet toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			if (label == null) {
				throw new IllegalArgumentException("Input parameter [label] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}
			Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.label = ? ");
			hibQuery.setBoolean(0, true);
			hibQuery.setString(1, label);
			SbiDataSet sbiDataSet = (SbiDataSet) hibQuery.uniqueResult();
			if (sbiDataSet != null) {
				toReturn = DataSetFactory.toDataSet(sbiDataSet, this.getUserProfile());
			} else {
				logger.debug("Impossible to load dataset with label [" + label + "].");
			}

			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose label is equal to [" + label + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public IDataSet loadDataSetByName(String name) {
		logger.debug("IN");

		IDataSet toReturn = null;
		Session session = null;
		Transaction transaction = null;
		try {
			if (name == null) {
				throw new IllegalArgumentException("Input parameter [name] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}
			Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.name = ? ");
			hibQuery.setBoolean(0, true);
			hibQuery.setString(1, name);
			SbiDataSet sbiDataSet = (SbiDataSet) hibQuery.uniqueResult();
			if (sbiDataSet != null) {
				toReturn = DataSetFactory.toDataSet(sbiDataSet, this.getUserProfile());
			} else {
				logger.debug("Impossible to load dataset with name [" + name + "].");
			}

			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose name is equal to [" + name + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public List<IDataSet> loadDataSetOlderVersions(Integer dsId) {
		logger.debug("IN");
		Session session = null;
		Query query = null;

		List<IDataSet> toReturn = new ArrayList<>();
		try {
			session = getSession();
			query = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			query.setBoolean(0, false);
			query.setInteger(1, dsId);

			List<SbiDataSet> olderTemplates = query.list();

			if (olderTemplates != null && !olderTemplates.isEmpty()) {
				Iterator<SbiDataSet> it = olderTemplates.iterator();
				while (it.hasNext()) {
					SbiDataSet hibOldDataSet = it.next();
					if (hibOldDataSet != null && !hibOldDataSet.isActive()) {
						IDataSet dsD = DataSetFactory.toDataSet(hibOldDataSet);
						toReturn.add(dsD);
					}
				}
			}
		} catch (SpagoBIRuntimeException ex) {
			throw ex;
		} catch (Exception e) {
			throw new SpagoBIDAOException("An error has occured while loading dataset's older versions", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<IDataSet> loadDatasetOwnedAndShared(UserProfile user) {
		List<IDataSet> results = new ArrayList<>();

		List<IDataSet> owened = loadDataSetsOwnedByUser(user, true);
		results.addAll(owened);
		List<IDataSet> shared = loadDatasetsSharedWithUser(user, true);
		results.addAll(shared);

		return results;
	}

	/**
	 * @deprecated See {@link ISbiDataSetDAO} TODO : Delete DONE
	 */
	@Override
	@Deprecated
	public List<IDataSet> loadDataSets() {
		return loadDataSets(null, null, null, null, null, null, null, true);
	}

	/**
	 * @deprecated See {@link ISbiDataSetDAO} TODO : Delete DONE
	 */
	@Override
	@Deprecated
	public List<IDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope,
			String type, Set<Domain> categoryList, String implementation, Boolean showDerivedDatasets) {

		List<IDataSet> results;
		Session session = null;

		logger.debug("IN");

		results = new ArrayList<>();
		try {
			// open session
			session = getSession();

			// create statement
			StringBuilder statement = new StringBuilder("from SbiDataSet h where h.active = :active");
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("active", true);

			if (owner != null) {
				statement.append(" and (");
				statement.append(Boolean.TRUE.equals(includeOwned) ? "h.owner = :owner" : "h.owner != :owner");
				statement.append(") ");
				parameters.put(OWNER, owner);
			}

			if (type != null) {
				statement.append(" and h.scope.valueCd = :type ");
				parameters.put("type", type);
			}

			if (categoryList != null) {
				logger.debug("We'll take in consideration categories");
				if (!categoryList.isEmpty()) {
					logger.debug("User has one or more categories");
					if (owner != null && includeOwned) {
						logger.debug("The owner can see all it's datasets");
						// the owner of the dataset can see dataste even if category is null
					} else {
						statement.append(" and h.category.code in (:categoryList)");
						Set<String> categoryValues = new HashSet<>();
						for (Domain category : categoryList) {
							categoryValues.add(category.getValueName());
						}
						parameters.put("categoryList", categoryValues);
					}
				} else {
					logger.debug("No categories for the user so we take just it's own datasets");
					if (owner == null || !includeOwned) {
						logger.debug("Owner is not specified on the service so we should return no datasets");
						return new ArrayList<>();
					}
				}

			}

			if (implementation != null) {
				statement.append(" and h.type = :implementation ");
				parameters.put("implementation", implementation);
			}

			if (Boolean.FALSE.equals(showDerivedDatasets)) {
				statement.append(" and h.federation is null ");
			}

			// inject parameters
			Query query = session.createQuery(statement.toString());

			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Collection) {
					query.setParameterList(key, (Collection) value);
				} else {
					query.setParameter(key, value);
				}
			}

			results = executeQuery(query, session);
		} catch (Exception e) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose owner is equal to [" + owner + "]", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return results;
	}

	// ========================================================================================
	// READ operations (cRud)
	// ========================================================================================

	@Override
	public List<DataSetBasicInfo> loadDatasetsBasicInfoForLov() {
		logger.debug("IN");
		List<DataSetBasicInfo> toReturn = new ArrayList<>();
		Session session = null;

		try {
			session = getSession();
			toReturn = session.createQuery(
					"select new it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo(ds.id.dsId, ds.label, ds.name, ds.description, ds.owner, ds.scope.valueCd) "
							+ " from SbiDataSet ds where ds.active = ? "
							+ " and (ds.parameters is null or (ds.parameters is not null and ds.parameters like '%<ROWS/>%'))")
					.setBoolean(0, true).list();
		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets basic info for LOV", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}

			logger.debug("OUT");
		}

		return toReturn;
	}

	@Override
	public List<DataSetBasicInfo> loadDatasetsBasicInfoForAI(List<Integer> idsObject) {
		logger.debug("IN");

		if (idsObject.isEmpty()) {
			return Collections.emptyList();
		}

		List<DataSetBasicInfo> toReturn = new ArrayList<>();
		Session session = null;
		try {
			session = getSession();
			toReturn = session
					.createQuery("select new it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo(ds.label, ds.name, ds.description, ds.dsMetadata,ds.type) "
							+ " from SbiDataSet ds, SbiObjDataSet ds1 where ds.id.dsId = ds1.dsId and  ds.active = ? and ds1.sbiObject.id IN (:listObject)")
					.setBoolean(0, true).setParameterList("listObject", idsObject).list();
		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets basic info for LOV", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}

			logger.debug("OUT");
		}

		return toReturn;
	}

	/**
	 * @deprecated See {@link ISbiDataSetDAO} TODO : Delete DONE
	 */
	@Override
	@Deprecated
	public List<IDataSet> loadDataSetsByOwner(UserProfile user, Boolean includeOwned, Boolean includePublic,
			Boolean showDerivedDatasets) {
		return loadDataSets(user.getUserId().toString(), includeOwned, includePublic, null, null,
				UserUtilities.getDataSetCategoriesByUser(user), null, showDerivedDatasets);
	}

	/**
	 * @param scope Sent from DatasetResource <br>
	 *              Can be: "all", "owned", "enterprise" and "shared", depends on Tab from Workspace/Datasets (MyDataset, Shared, Enterprise, All)
	 */
	@Override
	public List<IDataSet> loadDatasetsByTags(UserProfile user, List<Integer> tagIds, String scope) {
		logger.debug("IN");
		List<IDataSet> toReturn = new ArrayList<>();
		Session session = null;
		Set<Domain> categoryList = null;
		String owner = null;
		String domain = null;
		String[] domains = null;
		try {
			Assert.assertNotNull(user, "UserProfile object cannot be null");

			StringBuilder statement = new StringBuilder(
					"select distinct(dst.dataSet) from SbiDatasetTag dst where dst.dataSet.active = ? ");

			if (scope.equalsIgnoreCase("owned") || scope.equalsIgnoreCase(SHARED)) {
				owner = user.getUserId().toString();
				if (owner != null) {
					if (scope.equalsIgnoreCase("owned")) {
						statement.append("and dst.dataSet.owner = :owner ");
					} else {
						statement.append("and dst.dataSet.owner != :owner ");
					}
				}
			}

			if (scope.equalsIgnoreCase(ENTERPRISE) || scope.equalsIgnoreCase(SHARED)
					|| scope.equalsIgnoreCase("all")) {
				statement.append("and dst.dataSet.scope.valueCd = :domain ");
				if (scope.equalsIgnoreCase(ENTERPRISE)) {
					domain = scope.toUpperCase();
				} else if (scope.equalsIgnoreCase(SHARED)) {
					domain = "USER";
				} else {
					domains = new String[2];
					domains[0] = "USER";
					domains[1] = "ENTERPRISE";
					statement.append(
							"and (dst.dataSet.scope.valueCd = :user or dst.dataSet.scope.valueCd = :enterprise) ");
				}

				categoryList = UserUtilities.getDataSetCategoriesByUser(user);
				if (categoryList != null && !categoryList.isEmpty()) {
					statement.append("and dst.dataSet.category.code in (:categories) ");
				}
			}

			if (!tagIds.isEmpty()) {
				statement.append("and dst.dsTagId.tagId in (:tagIds)");
			}

			session = getSession();
			Query query = session.createQuery(statement.toString());

			// Always get active versions
			query.setBoolean(0, true);

			if (owner != null) {
				query.setString(OWNER, owner);
			}

			if (domain != null) {
				query.setString("domain", domain);
			}

			if (domains != null && domains.length > 0) {
				query.setString("user", domains[0]);
				query.setString(ENTERPRISE, domains[1]);
			}

			if (categoryList != null && !categoryList.isEmpty()) {
				Iterator<Domain> it = categoryList.iterator();
				List<String> categoryValues = new ArrayList<>();
				while (it.hasNext()) {
					categoryValues.add(it.next().getValueName());
				}

				query.setParameterList("categories", categoryValues);
			}

			if (!tagIds.isEmpty()) {
				query.setParameterList("tagIds", tagIds);
			}

			toReturn = executeQuery(query, session);
		} catch (Exception e) {
			logger.error("An error has occured while filtering Enterprise Datasets by Tags", e);
			throw new SpagoBIDAOException("An unexpected error has occured while filtering Datasets by Tags", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * @deprecated TODO Delete DONE
	 */
	@Deprecated
	@Override
	public List<IDataSet> loadDataSetsOwnedByUser(UserProfile user, Boolean showDerivedDatasets) {
		return loadDataSetsByOwner(user, true, false, showDerivedDatasets);
	}

	/**
	 * @deprecated See {@link ISbiDataSetDAO} TODO : Delete DONE
	 */
	@Override
	@Deprecated
	public List<IDataSet> loadDatasetsSharedWithUser(UserProfile profile, Boolean showDerivedDataset) {
		return loadDataSets(profile.getUserId().toString(), false, false, "PUBLIC", "USER",
				UserUtilities.getDataSetCategoriesByUser(profile), null, showDerivedDataset);
	}

	/**
	 * @deprecated See {@link ISbiDataSetDAO} TODO : Delete DONE
	 */
	@Override
	@Deprecated
	public List<IDataSet> loadEnterpriseDataSets(UserProfile profile) {
		return loadDataSets(null, null, null, null, "ENTERPRISE", UserUtilities.getDataSetCategoriesByUser(profile),
				null, true);
	}

	@Override
	public List<DataSetBasicInfo> loadFederatedDataSetsByFederatoinId(Integer id) {
		logger.debug("IN");
		List<DataSetBasicInfo> results = new ArrayList<>();
		Session session = null;

		try {
			session = getSession();
			Query query = session.createQuery(
					"select distinct new it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo(ds.label, ds.name) from SbiDataSet ds where ds.federation.federation_id = :federation_id");
			query.setInteger("federation_id", id);
			results = query.list();
		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading federated datasets", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return results;
	}

	@Override
	public List<IDataSet> loadFilteredDatasetList(Integer offset, Integer fetchSize, String owner, JSONObject filters,
			JSONObject ordering, List<Integer> tagIds) {
		logger.debug("IN");
		List<IDataSet> toReturn = new ArrayList<>();
		Session session = null;
		Transaction transaction = null;
		StringBuilder sb;
		StringBuilder sbTag = new StringBuilder();
		String entityName = "";
		String valuefilter = null;
		String typeFilter = null;
		String columnFilter = null;
		List idsCat = null;
		boolean reverseOrdering = false;
		String columnOrdering = null;
		try {
			boolean isAdmin = getUserProfile()
					.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN);

			if (offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			if (!isAdmin) {
				List<Domain> devCategories = new LinkedList<>();
				fillDevCategories(devCategories);
				idsCat = createIdsCatogriesList(devCategories);
			}

			if (filters != null) {
				valuefilter = filters.getString(SpagoBIConstants.VALUE_FILTER);
				typeFilter = filters.getString(SpagoBIConstants.TYPE_FILTER);
				columnFilter = filters.getString(SpagoBIConstants.COLUMN_FILTER);
			}

			if (ordering != null) {
				reverseOrdering = ordering.optBoolean("reverseOrdering");
				columnOrdering = ordering.optString("columnOrdering");
				if (columnOrdering != null && columnOrdering.equalsIgnoreCase("dsTypeCd")) {
					columnOrdering = "type";
				}
			}

			sb = new StringBuilder("from SbiDataSet ds where ds.active = true ");
			if (!tagIds.isEmpty()) {
				sbTag = new StringBuilder(
						"select tag.dataSet.label from SbiDatasetTag tag  where tag.dsTagId.tagId in (:tagIds) group by  tag.dataSet.label");
				sb.append(" and ds.label in ( :sbTag )");
			}

			if (!isAdmin) {
				if (idsCat == null || idsCat.isEmpty()) {
					sb.append("and ds.owner = :owner ");
				} else {
					sb.append("and (ds.category.id in (:idsCat) or ds.owner = :owner) ");
				}
			}

			if (filters != null) {
				if (typeFilter.equals("=")) {
					sb.append("and ds.").append(columnFilter).append(" = :search ");
				} else if (typeFilter.equals("like")) {
					sb.append("and upper(ds.").append(columnFilter).append(") like :search ");
				}
			}

			if (ordering != null) {
				if (columnOrdering != null && !columnOrdering.isEmpty()) {
					sb.append(" order by ds.").append(columnOrdering.toLowerCase());
					if (reverseOrdering) {
						sb.append(" desc");
					}
				}
			}

			offset = offset < 0 ? 0 : offset;

			session = getSession();
			transaction = session.beginTransaction();

			Query listQuery = session.createQuery(sb.toString());

			if (idsCat != null && !idsCat.isEmpty()) {
				listQuery.setParameterList(IDS_CAT, idsCat);
			}
			if (!isAdmin) {
				listQuery.setString(OWNER, owner);
			}
			if (valuefilter != null) {
				listQuery.setString("search", "%" + valuefilter.toUpperCase() + "%");
			}
			if (!tagIds.isEmpty()) {
				listQuery.setParameterList("tagIds", tagIds);
				listQuery.setParameter("sbTag", sbTag.toString());
			}

			listQuery.setFirstResult(offset);
			if (fetchSize > 0) {
				listQuery.setMaxResults(fetchSize);
			}
			List<SbiDataSet> sbiDatasetVersions = listQuery.list();

			if (sbiDatasetVersions != null && !sbiDatasetVersions.isEmpty()) {
				for (SbiDataSet sbiDatasetVersion : sbiDatasetVersions) {
					IDataSet guiDataSet = DataSetFactory.toDataSet(sbiDatasetVersion, this.getUserProfile());
					toReturn.add(guiDataSet);
				}
			}

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error has occured while loading datasets with tags", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize) {

		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			toReturn = new ArrayList<>();

			if (offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query countQuery = session.createQuery(String.format("select count(*) %s", hsql));
			Long temp = (Long) countQuery.uniqueResult();
			Integer resultNumber = temp.intValue();

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber) : resultNumber;
			}

			Query listQuery = session.createQuery(hsql);
			listQuery.setFirstResult(offset);
			if (fetchSize > 0) {
				listQuery.setMaxResults(fetchSize);
			}
			List<SbiDataSet> sbiDatasetVersions = listQuery.list();

			if (sbiDatasetVersions != null && !sbiDatasetVersions.isEmpty()) {
				for (SbiDataSet sbiDatasetVersion : sbiDatasetVersions) {
					IDataSet guiDataSet = DataSetFactory.toDataSet(sbiDatasetVersion, this.getUserProfile());

					List<IDataSet> oldDsVersion = new ArrayList();

					if (sbiDatasetVersion.getId().getVersionNum() != null) {
						Integer dsId = sbiDatasetVersion.getId().getDsId();
						Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
						hibQuery.setBoolean(0, false);
						hibQuery.setInteger(1, dsId);

						List<SbiDataSet> olderTemplates = hibQuery.list();
						if (olderTemplates != null && !olderTemplates.isEmpty()) {
							Iterator it2 = olderTemplates.iterator();
							while (it2.hasNext()) {
								SbiDataSet hibOldDataSet = (SbiDataSet) it2.next();
								if (hibOldDataSet != null && !hibOldDataSet.isActive()) {
									IDataSet dsD = DataSetFactory.toDataSet(hibOldDataSet);
									oldDsVersion.add(dsD);
								}
							}
						}
					}
					guiDataSet.setNoActiveVersions(oldDsVersion);
					toReturn.add(guiDataSet);
				}
			}

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset versions", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public List<IDataSet> loadFilteredDatasetList(String hsql, Integer offset, Integer fetchSize, String owner) {

		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			toReturn = new ArrayList<>();
			boolean isAdmin = getUserProfile()
					.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN);

			int orderByPos = hsql.indexOf("order by");
			String orderBy = "";
			if (orderByPos > 0) {
				orderBy = hsql.substring(orderByPos);
				hsql = hsql.substring(0, orderByPos);
			}

			if (offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// if not admin filter by category and owner
			List idsCat = null;
			if (!isAdmin) {
				List<Domain> devCategories = new LinkedList<>();
				fillDevCategories(devCategories);

				idsCat = createIdsCatogriesList(devCategories);

				if (idsCat == null || idsCat.isEmpty()) {
					hsql = hsql + " and h.owner = :owner";
				} else {
					hsql = hsql + " and (h.category.id IN (:idsCat) or h.owner = :owner)";
				}
			}

			hsql = hsql + " " + orderBy;

			offset = offset < 0 ? 0 : offset;
			Query listQuery = session.createQuery(hsql);
			if (idsCat != null && idsCat.size() > 0) {
				listQuery.setParameterList(IDS_CAT, idsCat);
			}
			if (!isAdmin) {
				listQuery.setString(OWNER, owner);
			}

			listQuery.setFirstResult(offset);
			if (fetchSize > 0) {
				listQuery.setMaxResults(fetchSize);
			}
			List<SbiDataSet> sbiDatasetVersions = listQuery.list();

			addGuiDataSet(toReturn, sbiDatasetVersions);

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset versions", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public List<IDataSet> loadFilteredDatasetByTypeList(String owner, String dsType) {
		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			StringBuilder statement = new StringBuilder("from SbiDataSet h where h.active = true");
			toReturn = new ArrayList<>();
			boolean isAdmin = getUserProfile()
					.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN);
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			// if not admin filter by category and owner
			List idsCat = null;
			if (!isAdmin) {
				List<Domain> devCategories = new LinkedList<>();
				fillDevCategories(devCategories);

				idsCat = createIdsCatogriesList(devCategories);

				if (idsCat == null || idsCat.size() == 0) {
					statement.append(" and h.owner = :owner");
				} else {
					statement.append(" and (h.category.id IN (:idsCat) or h.owner = :owner)");
				}
			}
			if (dsType != null) {
				statement.append(" and h.type = :dsType");
			}

			Query listQuery = session.createQuery(statement.toString());
			if (idsCat != null && idsCat.size() > 0) {
				listQuery.setParameterList(IDS_CAT, idsCat);
			}
			if (!isAdmin) {
				listQuery.setString(OWNER, owner);
			}

			if (dsType != null) {
				listQuery.setString("dsType", dsType);
			}

			List<SbiDataSet> sbiDatasetVersions = listQuery.list();
			addGuiDataSet(toReturn, sbiDatasetVersions);
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset versions", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	private void addGuiDataSet(List<IDataSet> toReturn, List<SbiDataSet> sbiDatasetVersions) {
		if (sbiDatasetVersions != null && !sbiDatasetVersions.isEmpty()) {
			for (SbiDataSet sbiDatasetVersion : sbiDatasetVersions) {
				IDataSet guiDataSet = DataSetFactory.toDataSet(sbiDatasetVersion, this.getUserProfile());
				toReturn.add(guiDataSet);
			}
		}
	}

	@Override
	@Deprecated
	public List<IDataSet> loadFlatDatasets() {
		return loadDataSets(null, false, false, null, "TECHNICAL", null, "SbiFlatDataSet", true);
	}

	@Override
	public List<IDataSet> loadMyDataDataSets(UserProfile userProfile) {
		logger.debug("IN");
		List<IDataSet> results = new ArrayList<>();
		Session session = null;
		Set<Domain> categoryList;
		List<Integer> categoryIds = null;
		StringBuilder statement = new StringBuilder(
				"from SbiDataSet ds where ds.active = :active and (ds.owner = :owner or (");
		try {
			session = getSession();
			categoryList = UserUtilities.getDataSetCategoriesByUser(userProfile);
			if (categoryList.isEmpty()) {
				statement.append("ds.category.id is null ");
			} else {
				categoryIds = extractCategoryIds(categoryList);
				statement.append("(ds.category.id is null or ds.category.id in (:categories)) ");
			}

			statement.append(
					"and ds.scope.valueId in (select dom.valueId from SbiDomains dom where dom.valueCd in ('USER', 'ENTERPRISE') and dom.domainCd = 'DS_SCOPE')))");

			Query query = session.createQuery(statement.toString());
			query.setBoolean("active", true);
			query.setString(OWNER, userProfile.getUserId().toString());

			if (categoryIds != null && !categoryIds.isEmpty()) {
				query.setParameterList("categories", categoryIds);
			}

			results = executeQuery(query, session);
		} catch (Exception e) {
			throw new SpagoBIDAOException("An unexpected error occured while loading all datasets for final user", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return results;
	}

	@Override
	public List<IDataSet> loadNotDerivedDatasetOwnedAndShared(UserProfile user) {
		List<IDataSet> results = new ArrayList<>();

		List<IDataSet> owened = loadDataSetsOwnedByUser(user, false);
		results.addAll(owened);
		List<IDataSet> shared = loadDatasetsSharedWithUser(user, false);
		results.addAll(shared);

		return results;
	}

	@Override
	public List<IDataSet> loadNotDerivedDataSets(UserProfile user) {
		List<IDataSet> results = new ArrayList<>();

		List<IDataSet> owened = loadDataSetsOwnedByUser(user, false);
		// results.addAll(owened);
		for (IDataSet iDataSet : owened) {
			if (!results.contains(iDataSet)) {
				results.add(iDataSet);
			}
		}

		List<IDataSet> shared = loadDatasetsSharedWithUser(user, false);
		// results.addAll(shared);
		for (IDataSet iDataSet : shared) {
			if (!results.contains(iDataSet)) {
				results.add(iDataSet);
			}
		}

		List<IDataSet> enterprise = loadEnterpriseDataSets(user);
		// results.addAll(enterprise);
		for (IDataSet iDataSet : enterprise) {
			if (!results.contains(iDataSet)) {
				results.add(iDataSet);
			}
		}

		return results;
	}

	@Override
	@Deprecated
	public List<IDataSet> loadNotDerivedUserDataSets(UserProfile user) {
		return loadDataSets(user.getUserId().toString(), true, false, null, "USER",
				UserUtilities.getDataSetCategoriesByUser(user), null, false);
	}

	/**
	 * Returns List of all existent IDataSets with current active version
	 *
	 * @param offset    starting element
	 * @param fetchSize number of elements to retrieve
	 * @return List of all existent IDataSets with current active version
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize) {

		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			toReturn = new ArrayList<>();

			if (offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			List<Domain> devCategories = new LinkedList<>();
			boolean isDev = fillDevCategories(devCategories);
			Query listQuery;
			if (isDev) {
				List idsCat = createIdsCatogriesList(devCategories);
				String owner = ((UserProfile) getUserProfile()).getUserId().toString();
				Query countQuery = null;
				if (idsCat != null && !idsCat.isEmpty()) {
					countQuery = session.createQuery(
							"select count(*) from SbiDataSet sb where sb.active = ? and (sb.category.id  IN (:idsCat) or sb.owner = :owner) ");
					countQuery.setBoolean(0, true);
					countQuery.setParameterList(IDS_CAT, idsCat);
					countQuery.setString(OWNER, owner);
				} else {
					countQuery = session.createQuery(
							"select count(*) from SbiDataSet sb where sb.active = ? and  sb.owner = :owner) ");
					countQuery.setBoolean(0, true);
					countQuery.setString(OWNER, owner);
				}

				Long resultNumber = (Long) countQuery.uniqueResult();

				offset = offset < 0 ? 0 : offset;
				if (resultNumber > 0) {
					fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber.intValue())
							: resultNumber.intValue();
				}

				listQuery = session.createQuery(
						"from SbiDataSet h where h.active = ? and (h.category.id IN (:idsCat) or h.owner = :owner) order by h.name");
				listQuery.setBoolean(0, true);
				listQuery.setParameterList(IDS_CAT, idsCat);
				listQuery.setString(OWNER, owner);
				listQuery.setFirstResult(offset);
			} else {
				Query countQuery = session.createQuery("select count(*) from SbiDataSet sb where sb.active = ? ");
				countQuery.setBoolean(0, true);
				Long resultNumber = (Long) countQuery.uniqueResult();

				offset = offset < 0 ? 0 : offset;
				if (resultNumber > 0) {
					fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber.intValue())
							: resultNumber.intValue();
				}

				listQuery = session.createQuery("from SbiDataSet h where h.active = ? order by h.name ");
				listQuery.setBoolean(0, true);
				listQuery.setFirstResult(offset);
			}

			if (fetchSize > 0) {
				listQuery.setMaxResults(fetchSize);
			}

			List sbiActiveDatasetsList = listQuery.list();

			if (sbiActiveDatasetsList != null && !sbiActiveDatasetsList.isEmpty()) {
				Iterator it = sbiActiveDatasetsList.iterator();
				while (it.hasNext()) {
					SbiDataSet hibDataSet = (SbiDataSet) it.next();
					IDataSet ds = DataSetFactory.toDataSet(hibDataSet, this.getUserProfile());
					List<IDataSet> oldDsVersion = new ArrayList();

					if (hibDataSet.getId().getDsId() != null) {
						Integer dsId = hibDataSet.getId().getDsId();
						Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
						hibQuery.setBoolean(0, false);
						hibQuery.setInteger(1, dsId);

						List<SbiDataSet> olderTemplates = hibQuery.list();
						if (olderTemplates != null && !olderTemplates.isEmpty()) {
							Iterator it2 = olderTemplates.iterator();
							while (it2.hasNext()) {
								SbiDataSet hibOldDataSet = (SbiDataSet) it2.next();
								if (hibOldDataSet != null && !hibOldDataSet.isActive()) {
									VersionedDataSet dsD = (VersionedDataSet) DataSetFactory.toDataSet(hibOldDataSet,
											getUserProfile());
									oldDsVersion.add(dsD);
								}
							}
						}
					}
					ds.setNoActiveVersions(oldDsVersion);
					toReturn.add(ds);
				}
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			logger.error("An unexpected error occured while loading datasets",e);
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Returns List of all existent IDataSets with current active version for the owner
	 *
	 * @param offset    starting element
	 * @param fetchSize number of elements to retrieve
	 * @return List of all existent IDataSets with current active version
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<IDataSet> loadPagedDatasetList(Integer offset, Integer fetchSize, String owner) {

		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			toReturn = new ArrayList<>();

			if (offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query countQuery = session.createQuery("select count(*) from SbiDataSet sb where sb.active = ? ");
			countQuery.setBoolean(0, true);
			Long resultNumber = (Long) countQuery.uniqueResult();

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber.intValue()) : resultNumber.intValue();
			}

			Query listQuery = session
					.createQuery("from SbiDataSet h where h.active = ? and h.owner = ? order by h.name ");
			listQuery.setBoolean(0, true);
			listQuery.setString(1, owner);
			listQuery.setFirstResult(offset);
			if (fetchSize > 0) {
				listQuery.setMaxResults(fetchSize);
			}

			List sbiActiveDatasetsList = listQuery.list();

			if (sbiActiveDatasetsList != null && !sbiActiveDatasetsList.isEmpty()) {
				Iterator it = sbiActiveDatasetsList.iterator();
				while (it.hasNext()) {
					SbiDataSet hibDataSet = (SbiDataSet) it.next();
					IDataSet ds = DataSetFactory.toDataSet(hibDataSet, this.getUserProfile());
					List<IDataSet> oldDsVersion = new ArrayList();

					if (hibDataSet.getId().getDsId() != null) {
						Integer dsId = hibDataSet.getId().getDsId();
						Query hibQuery = session.createQuery(
								"from SbiDataSet h where h.active = ? and h.id.dsId = ?  and h.owner = ?  ");
						hibQuery.setBoolean(0, false);
						hibQuery.setInteger(1, dsId);
						hibQuery.setString(2, owner);

						List<SbiDataSet> olderTemplates = hibQuery.list();
						if (olderTemplates != null && !olderTemplates.isEmpty()) {
							Iterator it2 = olderTemplates.iterator();
							while (it2.hasNext()) {
								SbiDataSet hibOldDataSet = (SbiDataSet) it2.next();
								if (hibOldDataSet != null && !hibOldDataSet.isActive()) {
									VersionedDataSet dsD = (VersionedDataSet) DataSetFactory.toDataSet(hibOldDataSet);
									oldDsVersion.add(dsD);
								}
							}
						}
					}
					ds.setNoActiveVersions(oldDsVersion);
					toReturn.add(ds);
				}
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Returns List of all existent SbiDataSet elements (NO DETAIL, only name, label, descr...).
	 *
	 * @param offset    starting element
	 * @param fetchSize number of elements to retrieve
	 * @return List of all existent SbiDataSet
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public List<SbiDataSet> loadPagedSbiDatasetConfigList(Integer offset, Integer fetchSize) {

		List<SbiDataSet> toReturn;
		Session session;
		Transaction transaction;
		Long resultNumber;

		logger.debug("IN");

		toReturn = null;
		session = null;
		transaction = null;
		try {
			toReturn = new ArrayList<>();

			if (offset == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [0]");
				offset = 0;
			}
			if (fetchSize == null) {
				logger.warn("Input parameter [offset] is null. It will be set to [" + Integer.MAX_VALUE + "]");
				fetchSize = Integer.MAX_VALUE;
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query countQuery = session.createQuery("select count(*) from SbiDataSet ds where ds.active = ?");
			countQuery.setBoolean(0, true);
			resultNumber = (Long) countQuery.uniqueResult();

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber.intValue()) : resultNumber.intValue();
			}

			Query listQuery = session.createQuery("from SbiDataSet ds where ds.active=true order by label");
			listQuery.setFirstResult(offset);
			if (fetchSize > 0) {
				listQuery.setMaxResults(fetchSize);
			}
			toReturn = listQuery.list();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading datasets", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	// ========================================================================================
	// UPDATE operations (crUd)
	// ========================================================================================

	@Override
	public SbiDataSet loadSbiDataSetById(Integer id, Session session) {
		logger.debug("IN");

		Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
		hibQuery.setBoolean(0, true);
		hibQuery.setInteger(1, id);
		SbiDataSet dsActiveDetail = (SbiDataSet) hibQuery.uniqueResult();
		if (dsActiveDetail == null) {
			logger.debug("Impossible to load dataset with id [" + id + "].");
		}

		logger.debug("OUT");
		return dsActiveDetail;
	}

	/**
	 * @deprecated See {@link ISbiDataSetDAO} TODO : Delete
	 */
	@Override
	@Deprecated
	public List<IDataSet> loadUserDataSets(String user) {
		return loadDataSets(user, true, false, null, "USER", null, null, true);
	}

	/**
	 * Modify data set.
	 *
	 * @param aDataSet the a data set
	 * @throws EMFUserError the EMF user error
	 * @see it.eng.spagobi.tools.dataset.dao.IDataSetDAO#modifyDataSet(it.eng.spagobi.tools.dataset.bo.AbstractDataSet)
	 */
	@Override
	public void modifyDataSet(IDataSet dataSet) {
		logger.debug("IN");
		modifyDataSet(dataSet, null);
		logger.debug("OUT");
	}

	// ========================================================================================
	// DELETE operations (cruD)
	// ========================================================================================

	@Override
	public void modifyDataSet(IDataSet dataSet, Session optionalDbSession) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

		boolean keepPreviousSession = optionalDbSession != null ? true : false;

		session = null;
		transaction = null;
		try {
			if (!keepPreviousSession) {
				try {
					session = getSession();
					Assert.assertNotNull(session, "session cannot be null");
					transaction = session.beginTransaction();
					Assert.assertNotNull(transaction, "transaction cannot be null");
				} catch (Exception e) {
					throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
				}
			} else {
				session = optionalDbSession;
			}

			SbiDataSetId compositeKey = getDataSetKey(session, dataSet, false);
			SbiDataSet hibDataSet = new SbiDataSet(compositeKey);
			if (dataSet != null) {
				Integer dsId = dataSet.getId();
				hibDataSet.setActive(true);
				SbiDomains transformer = null;
				if (dataSet.getTransformerId() != null) {
					Criterion aCriterion = Restrictions.eq(VALUE_ID, dataSet.getTransformerId());
					Criteria criteria = session.createCriteria(SbiDomains.class);
					criteria.add(aCriterion);
					transformer = (SbiDomains) criteria.uniqueResult();
					if (transformer == null) {
						throw new SpagoBIDAOException(
								"The Domain with value_id= " + dataSet.getTransformerId() + " does not exist");
					}
				}

				SbiCategory category = null;
				if (dataSet.getCategoryId() != null) {
					ICategoryDAO categoryDAO = DAOFactory.getCategoryDAO();
					category = categoryDAO.getCategory(session, dataSet.getCategoryId());
					if (category == null) {
						throw new SpagoBIDAOException(
								"The Domain with value_id= " + dataSet.getCategoryId() + " does not exist");
					}
				}

				SbiDomains scope = null;
				if (dataSet.getScopeId() != null) {
					Criterion aCriterion = Restrictions.eq(VALUE_ID, dataSet.getScopeId());
					Criteria criteria = session.createCriteria(SbiDomains.class);
					criteria.add(aCriterion);
					scope = (SbiDomains) criteria.uniqueResult();
					if (scope == null) {
						throw new SpagoBIDAOException(
								"The Domain with value_id= " + dataSet.getScopeId() + " does not exist");
					}
				}
				// hibDataSet.setScope(scope);
				Date currentTStamp = new Date();
				hibDataSet.setTimeIn(currentTStamp);
				hibDataSet.setTransformer(transformer);
				hibDataSet.setCategory(category);
				hibDataSet.setParameters(dataSet.getParameters());
				hibDataSet.setDsMetadata(dataSet.getDsMetadata());

				// manage of persistence fields
				hibDataSet.setPersisted(dataSet.isPersisted());
				hibDataSet.setPersistedHDFS(dataSet.isPersistedHDFS());
				hibDataSet.setPersistTableName(dataSet.getPersistTableName());

				hibDataSet.setLabel(dataSet.getLabel());
				hibDataSet.setDescription(dataSet.getDescription());
				hibDataSet.setName(dataSet.getName());
				hibDataSet.setConfiguration(dataSet.getConfiguration());
				hibDataSet.setType(dataSet.getDsType());
				updateSbiCommonInfo4Insert(hibDataSet);

				String userIn = hibDataSet.getCommonInfo().getUserIn();
				String sbiVersionIn = hibDataSet.getCommonInfo().getSbiVersionIn();
				hibDataSet.setUserIn(userIn);
				hibDataSet.setSbiVersionIn(sbiVersionIn);
				hibDataSet.setTimeIn(currentTStamp);
				// hibDataSet.setOrganization(hibDataSet.getCommonInfo().getOrganization());

				if (dataSet.getDatasetFederation() != null) {
					SbiFederationDefinition federationDefinition = SbiFederationUtils
							.toSbiFederatedDataset(dataSet.getDatasetFederation());
					if (federationDefinition != null) {
						hibDataSet.setFederation(federationDefinition);
					}
				}

				Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
				hibQuery.setBoolean(0, true);
				hibQuery.setInteger(1, dsId);

				SbiDataSet dsActiveDetail = (SbiDataSet) hibQuery.uniqueResult();

				if (dsActiveDetail.getOwner() == null) {
					hibDataSet.setOwner(userIn);
				} else {
					hibDataSet.setOwner(dsActiveDetail.getOwner());
				}
				if (scope != null) {
					hibDataSet.setScope(scope);
				} else if (dsActiveDetail.getScope() != null) {
					hibDataSet.setScope(dsActiveDetail.getScope());
				}
				if (category != null) {
					hibDataSet.setCategory(category);
				} else if (dsActiveDetail.getCategory() != null) {
					hibDataSet.setCategory(dsActiveDetail.getCategory());
				}
				dsActiveDetail.setActive(false);
				session.update(dsActiveDetail);
				session.save(hibDataSet);

				/**
				 * When active version of Dataset is modified, update Datasources for all previous versions, so we can make sure not to brake particular Dataset if user delete
				 * Datasource that is used by some of older versions of that Dataset
				 */
				String type = dataSet.getDsType();
				boolean shouldUpdateOlderVersions = false;
				String dataSource = "";

				switch (type) {
				case DataSetConstants.DS_QUERY:
					shouldUpdateOlderVersions = true;

					break;
				case DataSetConstants.DS_QBE:
					shouldUpdateOlderVersions = true;

					break;
				case DataSetConstants.DS_FLAT:
					shouldUpdateOlderVersions = true;

					break;
				default:
					logger.debug("Dataset type is [" + type
							+ "], so no need to update older versions cause Datasource can not be ambiguous.");
				}

				if (shouldUpdateOlderVersions) {
					updateOlderVersions(dataSet);
				}

				if (!keepPreviousSession && transaction != null) {
					transaction.commit();
				}

				if (dataSet.getDsType().equals("SbiQbeDataSet")) {
					// insert relations between qbe dataset and bc
					// insertQbeRelations(dataSet);
					insertQbeRelations(hibDataSet);
				}
				DataSetEventManager.getINSTANCE().notifyChange(dataSet);
			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("Error while modifing the data Set with id "
					+ ((dataSet == null) ? "" : String.valueOf(dataSet.getId())), e);
		} finally {
			if (!keepPreviousSession && session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public String getDataSource(SbiDataSet ds) {
		String type = ds.getType();
		String dataSource = "";
		switch (type) {
		case DataSetConstants.DS_QUERY:

			dataSource = DataSetConstants.DATA_SOURCE;
			break;
		case DataSetConstants.DS_QBE:

			dataSource = DataSetConstants.QBE_DATA_SOURCE;
			break;
		case DataSetConstants.DS_FLAT:

			JSONObject jsonConf = ObjectUtils.toJSONObject(ds.getConfiguration());

			// Old flat dataset has stored the data source to a different attribute
			dataSource = jsonConf.has(DataSetConstants.DATA_SOURCE_FLAT) ? DataSetConstants.DATA_SOURCE_FLAT
					: DataSetConstants.DATA_SOURCE;
			break;
		default:
			logger.debug("Dataset type is [" + type
					+ "], so no need to update older versions cause Datasource can not be ambiguous.");
		}
		return dataSource;
	}

	/**
	 * @param dataSet
	 * @throws JSONException
	 */
	public void updateOlderVersions(IDataSet dataSet) throws JSONException {
		List<SbiDataSet> olderVersions = getDatasetOlderVersions(dataSet.getId());
		Iterator<SbiDataSet> it = olderVersions.iterator();
		String dataSourceLabel = dataSet.getDataSource().getLabel();

		while (it.hasNext()) {
			SbiDataSet ds = it.next();
			JSONObject jsonConf = ObjectUtils.toJSONObject(ds.getConfiguration());
			String dataSourceFieldName = getDataSource(ds);
			if (!dataSourceLabel.equals(jsonConf.get(dataSourceFieldName))) {
				jsonConf.put(dataSourceFieldName, dataSourceLabel);
				ds.setConfiguration(JSONUtils.escapeJsonString(jsonConf.toString()));
				updateDataset(ds, getSession());
			}
		}
	}

	/**
	 * Restore an Older Version of the dataset
	 *
	 * @param dsId      the a data set ID
	 * @param dsVersion the a data set Version
	 * @throws EMFUserError the EMF user error
	 */
	@Override
	public IDataSet restoreOlderDataSetVersion(Integer dsId, Integer dsVersion) {
		logger.debug("IN");
		Session session = null;
		Transaction transaction = null;
		IDataSet toReturn = null;
		IDataSet oldDataSet = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			if (dsId != null && dsVersion != null) {

				Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
				hibQuery.setBoolean(0, true);
				hibQuery.setInteger(1, dsId);
				SbiDataSet dsActiveDetail = (SbiDataSet) hibQuery.uniqueResult();
				oldDataSet = DataSetFactory.toDataSet(dsActiveDetail, this.getUserProfile());
				dsActiveDetail.setActive(false);

				Query hibernateQuery = session
						.createQuery("from SbiDataSet h where h.id.versionNum = ? and h.id.dsId = ?");
				hibernateQuery.setInteger(0, dsVersion);
				hibernateQuery.setInteger(1, dsId);
				SbiDataSet dsDetail = (SbiDataSet) hibernateQuery.uniqueResult();
				dsDetail.setActive(true);

				session.update(dsActiveDetail);
				session.update(dsDetail);
				transaction.commit();
				// toReturn = DataSetFactory.toGuiDataSet(dsDetail);
				toReturn = DataSetFactory.toDataSet(dsDetail);

				DataSetEventManager.getINSTANCE().notifyRestoreVersion(oldDataSet, toReturn);

			}
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"Error while modifing the data Set with id " + ((dsId == null) ? "" : String.valueOf(dsId)), e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public IDataSet toGuiGenericDataSet(IDataSet iDataSet) {
		return DataSetFactory.toGuiDataSet(iDataSet);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.dao.IDataSetDAO#updateDatasetOlderVersion(it.eng.spagobi.tools.dataset.bo.IDataSet)
	 */
	@Override
	public void updateDatasetOlderVersion(IDataSet dataSet) {
		for (SbiDataSet olderDataset : getDatasetOlderVersions(dataSet.getId())) {
			olderDataset.setLabel(dataSet.getLabel());
			olderDataset.setName(dataSet.getName());
			updateDataset(olderDataset, getSession());

		}

	}

	private Transaction beginTransaction(Session session) {
		Transaction transaction = null;
		try {
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");
		} catch (Exception e) {
			throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
		}

		return transaction;
	}

	// ========================================================================================
	// CONVERSIONS
	// ========================================================================================

	private List createIdsCatogriesList(List<Domain> devCategories) {
		List<Integer> idsCat = new LinkedList<>();
		for (Domain dom : devCategories) {
			idsCat.add(dom.getValueId());
		}
		return idsCat;
	}

	private List<IDataSet> executeQuery(Query query, Session session) {
		List<IDataSet> results;
		Transaction transaction;

		logger.debug("IN");

		results = new ArrayList<>();
		transaction = null;
		try {
			transaction = beginTransaction(session);
			List<SbiDataSet> sbiDataSetList = query.list();
			results = parseResult(sbiDataSetList);
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An unexpected error occured while loading dataset", e);
		} finally {
			logger.debug("OUT");
		}

		return results;
	}

	private List<Integer> extractCategoryIds(Set<Domain> categoryList) {
		List<Integer> toReturn = new ArrayList<>();
		Iterator<Domain> it = categoryList.iterator();
		while (it.hasNext()) {
			Domain category = it.next();
			toReturn.add(category.getValueId());
		}
		return toReturn;
	}

	private boolean fillDevCategories(List<Domain> devCategories) {
		try {
			UserProfile profile = (UserProfile) getUserProfile();
			boolean isDev = UserUtilities.hasDeveloperRole(profile) && !UserUtilities.hasAdministratorRole(profile);
			if (isDev) {
				IRoleDAO rolesDao = DAOFactory.getRoleDAO();
				rolesDao.setUserProfile(profile);
				Collection<String> roles = profile.getRolesForUse();
				Iterator<String> itRoles = roles.iterator();
				while (itRoles.hasNext()) {
					String roleName = itRoles.next();
					Role role = rolesDao.loadByName(roleName);
					List<RoleMetaModelCategory> ds = rolesDao.getMetaModelCategoriesForRole(role.getId());
					ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
					List<Domain> categories = categoryDao.getCategoriesForDataset().stream().map(Domain::fromCategory)
							.collect(toList());
					for (RoleMetaModelCategory r : ds) {
						Iterator itCategories = categories.iterator();
						while (itCategories.hasNext()) {
							Domain dom = (Domain) itCategories.next();
							if (r.getCategoryId().equals(dom.getValueId())) {
								devCategories.add(dom);
							}
						}
					}
				}
			}
			return isDev;
		} catch (EMFUserError | EMFInternalError e) {
			logger.error("Impossible to check categories for role DEV" + e);
			throw new SpagoBIRuntimeException("Impossible to check categories for role DEV" + e);
		}
	}

	private SbiDataSetId getDataSetKey(Session aSession, IDataSet dataSet, boolean isInsert) {
		SbiDataSetId toReturn = new SbiDataSetId();
		// get the next id or version num of the dataset managed
		Integer maxId = null;
		Integer nextId = null;
		String organization = null;
		String hql = null;
		Query query = null;
		if (isInsert) {
			hql = " select max(sb.id.dsId) as maxId from SbiDataSet sb ";
			toReturn.changeVersionNum(new Integer("1"));
			query = aSession.createQuery(hql);
		} else {
			hql = " select max(sb.id.versionNum) as maxId, sb.id.organization as organization from SbiDataSet sb where sb.id.dsId = ? group by organization";
			query = aSession.createQuery(hql);
			query.setInteger(0, dataSet.getId());
			toReturn.changeDsId(dataSet.getId());
		}

		List result = query.list();
		Iterator it = result.iterator();
		while (it.hasNext()) {
			Object resultObject = it.next();
			if (resultObject instanceof Integer) {
				maxId = (Integer) resultObject;
			} else {
				// composed result
				if (resultObject instanceof Object[]) {
					Object[] resultArrayObject = (Object[]) resultObject;
					maxId = (Integer) resultArrayObject[0];
					organization = (String) resultArrayObject[1];
				}

			}
		}
		logger.debug("Current max prog : " + maxId);
		if (maxId == null) {
			nextId = 1;
		} else {
			nextId = maxId.intValue() + 1;
		}

		if (isInsert) {
			logger.debug("Nextid: " + nextId);
			toReturn.changeDsId(nextId);
		} else {
			logger.debug("NextVersion: " + nextId);
			if (organization != null) {
				toReturn.setOrganization(organization);
			}
			toReturn.changeVersionNum(nextId);
		}

		return toReturn;
	}

	private List<SbiDataSet> getDatasetOlderVersions(Integer dsId) {
		logger.debug("IN");
		Session session = null;
		Query query = null;

		List<SbiDataSet> olderVersions = new ArrayList<>();
		try {
			session = getSession();
			query = session.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
			query.setBoolean(0, false);
			query.setInteger(1, dsId);
			olderVersions = query.list();
		} catch (Exception e) {
			throw new SpagoBIDAOException("An error has occured while loading dataset's older versions", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		logger.debug("OUT");
		return olderVersions;
	}

	// private void insertQbeRelations(IDataSet ds) {
	private void insertQbeRelations(SbiDataSet ds) {
		logger.debug("IN");
		try {
			// For get informations parses the template because the query object is always NULL !!
			// QbeDataSet qbeDS = (QbeDataSet) ds;
			// IDataSource qbeDSource = qbeDS.getQbeDataSourceFromStmt();
			// IDataSource qbeDSource2 = qbeDS.getQbeDataSource();
			// it.eng.qbe.query.Query q = (it.eng.qbe.query.Query) qbeDS.getQuery();
			// Set<IModelEntity> entities = q.getQueryEntities(qbeDSource);

			String config = JSONUtils.escapeJsonString(ds.getConfiguration());
			JSONObject configJSON = ObjectUtils.toJSONObject(config);

			String qbeDataSource = configJSON.getString("qbeDataSource");
			String qbeDataMart = configJSON.getString("qbeDatamarts");
			JSONObject JSONQuery = ObjectUtils.toJSONObject(configJSON.getString("qbeJSONQuery"));
			JSONObject JSONCatalogue = ObjectUtils.toJSONObject(JSONQuery.getString("catalogue"));
			JSONArray queries = ObjectUtils.toJSONArray(JSONCatalogue.getString("queries"));
			HashMap<String, Boolean> insertedMap = new HashMap<>();

			SbiMetaSource sbiMS = DAOFactory.getSbiMetaSourceDAO().loadSourceByNameAndType(qbeDataSource.toLowerCase(),
					"database");
			if (sbiMS == null) {
				logger.debug(
						"MetaSource not defined, probably the model wasn't imported. Relations between entities and dataset not inserted!");
				return;
			}
			Integer sourceId = sbiMS.getSourceId();
			// get the business class linked to the the query fields throught the meta models classes
			for (int i = 0; i < queries.length(); i++) {
				JSONObject query = (JSONObject) queries.get(i);
				JSONArray fields = query.getJSONArray("fields");
				for (int f = 0; f < fields.length(); f++) {
					JSONObject field = (JSONObject) fields.get(f);
					if (field.isNull("entity")) {
						logger.debug("Object [entity] not found. The field is calculated. Skip the field.");
						continue;
					}
					String entityName = field.getString("entity").toLowerCase();
					if (insertedMap.get(entityName) != null) {
						logger.debug("Relation with [" + entityName + "]  already inserted. Skip the field.");
						continue;
					}

					SbiMetaTable metaTable = DAOFactory.getSbiMetaTableDAO().loadTableByNameAndSource(entityName,
							sourceId);
					SbiMetaBc metaBC = null;
					if (metaTable == null) {
						metaBC = DAOFactory.getSbiMetaBCDAO().loadBcByUniqueName(qbeDataMart, entityName);
					} else {
						Integer tableId = metaTable.getTableId();
						List<SbiMetaBc> metaTableBCList = DAOFactory.getSbiTableBCDAO().loadBcByTableId(tableId);

						for (SbiMetaBc bc : metaTableBCList) {
							// get the correct bc linked to the used model
							if (bc.getSbiMetaModel().getName().equalsIgnoreCase(qbeDataMart) && !bc.isDeleted()) {
								metaBC = bc;
								break;
							}
						}

						if (metaTableBCList == null || metaTableBCList.size() == 0) {
							logger.error("The entity [" + entityName
									+ "] doesn't exist into the SbiMetaBC tale. Relation not inserted!");
							continue;
						}

					}
					if (metaBC == null || !metaBC.getSbiMetaModel().getName().equalsIgnoreCase(qbeDataMart)
							|| metaBC.isDeleted()) {
						logger.error("There isn't a business class for the entity [" + entityName + "] and the model ["
								+ qbeDataMart + "]. Relation not inserted!");
						continue;
					}

					// String uniqueName = metaBC.getUniqueName().toLowerCase();
					// String uniqueName = metaBC.getName().toLowerCase();

					// sets the new bcId
					SbiMetaDsBcId metaDsBcId = new SbiMetaDsBcId(ds.getId().getDsId(), ds.getId().getVersionNum(),ds.getId().getOrganization(), metaBC.getBcId() );
					/*metaDsBcId.setOrganization(ds.getId().getOrganization());
					metaDsBcId.setVersionNum(ds.getId().getVersionNum());
					metaDsBcId.setDsId(ds.getId().getDsId());
					metaDsBcId.setBcId(metaBC.getBcId());
                    */
					// Loads old records to delete (previous version)
					List<SbiMetaDsBc> lstDsBc = DAOFactory.getDsBcDAO().loadDsBcByKey(metaDsBcId);
					for (SbiMetaDsBc el : lstDsBc) {
						DAOFactory.getDsBcDAO().deleteDsBc(el);
					}
					// inserts the new version
					SbiMetaDsBc metaDsBc = new SbiMetaDsBc();
					metaDsBc.setId(metaDsBcId);
					DAOFactory.getDsBcDAO().insertDsBc(metaDsBc);
					insertedMap.put(entityName, true);
				}
			}
		} catch (Exception e) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while insert relations between dataset and business class", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private List<IDataSet> parseResult(List<SbiDataSet> sbiDataSetList) {
		List<IDataSet> results = null;

		results = new ArrayList<>();
		for (SbiDataSet sbiDataSet : sbiDataSetList) {
			if (sbiDataSet != null) {
				IDataSet dataSet = DataSetFactory.toDataSet(sbiDataSet, this.getUserProfile());
				if (dataSet != null) {
					results.add(dataSet);
				}
			}
		}

		return results;
	}

	private void updateDataset(SbiDataSet sbiDataSet, Session session) {
		logger.debug("IN");
		try {
			if (session == null) {
				Assert.assertNotNull(session, "Session cannot be null");
			}
			session.update(sbiDataSet);
			session.flush();
		} catch (Exception e) {
			throw new SpagoBIDAOException("An error has occured while updating dataset's older version", e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	@Override
	public List<IDataSet> loadFilteredDatasetList(int offset, int fetchSize, String owner, String sortByColumn,
			boolean reverse, List<Integer> tagIds) {

		Session session = null;
		List<IDataSet> ret = Collections.EMPTY_LIST;

		try {
			session = getSession();

			Criteria cr = session.createCriteria(SbiDataSet.class);

			if (offset != -1) {
				cr.setFirstResult(offset);
			}
			if (fetchSize != -1) {
				cr.setFetchSize(fetchSize);
			}

			if (StringUtils.isNotEmpty(owner)) {
				cr.add(Restrictions.eq(OWNER, owner));
			}

			if (StringUtils.isNotEmpty(sortByColumn)) {
				Order orderBy = null;

				if (!reverse) {
					orderBy = Order.asc(sortByColumn);
				} else {
					orderBy = Order.desc(sortByColumn);
				}
				cr.addOrder(orderBy);
			}

			if (!tagIds.isEmpty()) {
				cr.add(Restrictions.in("tag.dsTagId.tagId", tagIds));
			}

			List<SbiDataSet> listOfSbiDataset = cr.list();

			ret = listOfSbiDataset.stream().map(e -> DataSetFactory.toDataSet(e, getUserProfile())).collect(toList());

		} catch (Exception ex) {
			LogMF.error(logger,
					"Error getting list of dataset with offset {0}, limit {1}, owner {2}, sorting column {3}, reverse {4} and tags {5}",
					new Object[] { offset, fetchSize, owner, sortByColumn, reverse, tagIds });
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return ret;
	}

	@Override
	public List<IDataSet> loadDerivedDataSetByLabel(String sourceLabel) {
		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = new ArrayList<>();
		session = null;
		transaction = null;
		try {
			if (sourceLabel == null) {
				throw new IllegalArgumentException("Input parameter [label] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query hibQuery = session.createQuery(
					"from SbiDataSet h where h.active = ? and h.configuration like :sourceLabel and type = :derivedType");

			hibQuery.setBoolean(0, true);
			hibQuery.setParameter("sourceLabel", "%\"sourceDatasetLabel\":\"" + sourceLabel + "\"%");
			hibQuery.setParameter("derivedType", DataSetConstants.DS_DERIVED);
			List<SbiDataSet> sbiDataSet = hibQuery.list();
			if (sbiDataSet != null) {
				for (SbiDataSet sbiDataSetitem : sbiDataSet) {
					toReturn.add(DataSetFactory.toDataSet(sbiDataSetitem, this.getUserProfile()));
				}

			} else {
				logger.debug("Impossible to load dataset with label [" + sourceLabel + "].");
			}

			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose label is equal to [" + sourceLabel + "]",
					e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public IDataSet loadDataSetByLabelAndUserCategories(String label) {
		ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();
		IDataSet datasetToReturn = this.loadDataSetByLabel(label);
		Integer categoryId = datasetToReturn.getCategoryId();
		List<String> result;
		try {
			result = manageRolesByCategory((List<String>) this.getUserProfile().getRoles(), categoryDao, categoryId);
		} catch (EMFUserError | EMFInternalError e) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose label is equal to [" + label + "]", e);
		}
		if (result != null && !result.isEmpty()) {
			return datasetToReturn;
		}

		return null;
	}

	/**
	 * @param roles
	 * @param categoryDao
	 * @param categoryId
	 * @return
	 * @throws EMFUserError
	 */
	private List<String> manageRolesByCategory(List<String> roles, ICategoryDAO categoryDao, Integer categoryId)
			throws EMFUserError {
		List<String> correctRoles;
		if (categoryId != null) {
			List<String> rolesByCategory = getRolesByCategory(categoryDao, categoryId);
			roles.retainAll(rolesByCategory);
			correctRoles = roles;
		} else {
			correctRoles = roles.stream().collect(Collectors.toList());
		}
		return correctRoles;
	}

	private List<String> getRolesByCategory(ICategoryDAO categoryDao, Integer categoryId) throws EMFUserError {
		List<String> rolesByCategory = categoryDao.getRolesByCategory(categoryId).stream().map(SbiExtRoles::getName)
				.collect(Collectors.toList());
		return rolesByCategory;
	}

	@Override
	public Integer countCategories(Integer catId) {
		logger.debug("IN");
		Integer resultNumber = 0;
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "select count(distinct id.dsId) from SbiDataSet s where s.category.id = ? ";
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, catId.intValue());
			resultNumber = ((Long) aQuery.uniqueResult()).intValue();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("Error while getting the category with the data set with id " + catId, e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return resultNumber;
	}

	@Override
	public List<IDataSet> loadDataSetByCategoryId(Integer catId) {
		List<IDataSet> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		toReturn = new ArrayList<>();
		session = null;
		transaction = null;
		try {
			if (catId == null) {
				throw new IllegalArgumentException("Input parameter [catId] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.category.id = :catId");

			hibQuery.setBoolean(0, true);
			hibQuery.setParameter("catId", catId);
			List<SbiDataSet> sbiDataSet = hibQuery.list();
			if (sbiDataSet != null) {
				for (SbiDataSet sbiDataSetitem : sbiDataSet) {
					toReturn.add(DataSetFactory.toDataSet(sbiDataSetitem, this.getUserProfile()));
				}

			} else {
				logger.debug("Impossible to load dataset with category [" + catId + "].");
			}

			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading dataset whose category id is equal to [" + catId + "]",
					e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public List<String> loadPersistenceTableNames(Integer dsId) {
		List<String> toReturn;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Exception e) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", e);
			}

			Query hibQuery = session.createQuery("select distinct ds.persistTableName from SbiDataSet ds where ds.active = ? AND ds.id.dsId != ?");

			hibQuery.setBoolean(0, true);
			hibQuery.setInteger(1, dsId);
			toReturn = (List) hibQuery.list();
			transaction.commit();

		} catch (Exception e) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading persistence table names",
					e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	private void deleteFileAssociatedToDataset(SbiDataSet sbiDataSet) {
		try {
			JSONObject config = new JSONObject(sbiDataSet.getConfiguration());
			String fileName = config.getString("fileName");
			String fileDir = SpagoBIUtilities.getFileDatasetResourcePath();
			File toDelete = new File(fileDir + File.separatorChar + fileName);
			toDelete.delete();
		} catch (Exception e) {
			logger.error("Cannot delete file associated to dataset: " + sbiDataSet.getLabel(), e);
		}
	}
}
