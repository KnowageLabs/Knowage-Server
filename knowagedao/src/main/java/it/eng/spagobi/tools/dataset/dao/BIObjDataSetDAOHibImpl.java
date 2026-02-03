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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiObjDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * Defines the Hibernate implementations for all DAO methods, for a BI Object Dataset.
 *
 * @author Gavardi
 */
public class BIObjDataSetDAOHibImpl extends AbstractHibernateDAO implements IBIObjDataSetDAO {
	private static Logger logger = Logger.getLogger(BIObjDataSetDAOHibImpl.class);

	@Override
	public void updateObjectNotDetailDatasets(BIObject biObj, ArrayList<String> dsLabels, Session currSession)
			throws EMFUserError {
		logger.debug("IN");

		logger.debug("update notDetail associations for biObj " + biObj.getId());

		Map<String, IDataSet> datasetsToInsert = new HashMap<>();
		ArrayList<Integer> idsToInsert = new ArrayList<>();

		// Load Dataset to insert
		for (Iterator<String> iterator = dsLabels.iterator(); iterator.hasNext();) {
			String label = iterator.next();
			if (!datasetsToInsert.keySet().contains(label)) {
				IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
				if (dataSet == null) {
					logger.debug("Dataset with label " + label + " indicated in template of object " + biObj.getLabel()
							+ " is not found");
				} else {
					datasetsToInsert.put(label, dataSet);
					idsToInsert.add(dataSet.getId());
					logger.debug("To Associate object " + biObj.getLabel() + " with dataset " + label);
				}
			}
		}

		// delete previous relationships and insert new one, only not detail dataset
		logger.debug("Delete previous dataset associations if not present in current");
		ArrayList<Integer> idsAlreadyPresent = new ArrayList<>();

		// load all associations, also detial one to avoid re-insert the same association again
		ArrayList<BIObjDataSet> previousAllAssociatedDatasets = getBiObjDataSets(biObj.getId(), currSession);
		for (Iterator<BIObjDataSet> iterator = previousAllAssociatedDatasets.iterator(); iterator.hasNext();) {
			BIObjDataSet biObjDataSet = iterator.next();
			if (!idsToInsert.contains(biObjDataSet.getDataSetId())) {
				// erase only if it is not detail
				if (!biObjDataSet.getIsDetail()) {
					logger.debug("Delete association with dataset with id " + biObjDataSet.getDataSetId());
					eraseBIObjDataSet(biObjDataSet, currSession);
				}
			} else {
				idsAlreadyPresent.add(biObjDataSet.getDataSetId());
			}

		}

		logger.debug("Insert new dataset associations");
		for (Iterator<String> iterator = datasetsToInsert.keySet().iterator(); iterator.hasNext();) {
			String dsLabel = iterator.next();
			IDataSet dataSet = datasetsToInsert.get(dsLabel);

			// don't insert if it is already present (also as detail)
			if (!idsAlreadyPresent.contains(dataSet.getId())) {
				logger.debug("Insert association with dataset " + dataSet.getLabel());
				insertBiObjDataSet(biObj.getId(), dataSet.getId(), false, currSession);
			} else {
				logger.debug("Association with dataset " + dataSet.getLabel() + " already present and not deleted");
			}

		}

		logger.debug("OUT");

	}

	@Override
	public ArrayList<BIObjDataSet> getBiObjDataSets(Integer biObjId) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObjDataSet> toReturn = new ArrayList<>();

		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");

			toReturn = getBiObjDataSets(biObjId, session);

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("Error while getting datasets associated with object" + biObjId, t);
		} finally {
			closeSessionIfOpen(session);
		}
		logger.debug("OUT");
		return toReturn;

	}

	@Override
	public ArrayList<BIObjDataSet> getBiObjDataSets(Integer biObjId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObjDataSet> toReturn = new ArrayList<>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = :biObjId";
		Query hqlQuery = currSession.createQuery(hql);
		hqlQuery.setParameter("biObjId", biObjId);
		List<SbiObjDataSet> hibObjectPars = hqlQuery.list();

		Iterator<SbiObjDataSet> it = hibObjectPars.iterator();

		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet(it.next());
			toReturn.add(aBIObjectDataSet);
		}

		logger.debug("OUT");
		return toReturn;
	}

	public List<BIObjDataSet> getBiObjNotDetailDataSets(Integer biObjId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObjDataSet> toReturn = new ArrayList<>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId =  :biObjId   AND (isDetail=false OR isDetail is null)";
		Query hqlQuery = currSession.createQuery(hql);
		hqlQuery.setParameter("biObjId", biObjId);
		List<SbiObjDataSet> hibObjectPars = hqlQuery.list();

		Iterator<SbiObjDataSet> it = hibObjectPars.iterator();

		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet(it.next());
			toReturn.add(aBIObjectDataSet);
		}

		logger.debug("OUT");
		return toReturn;
	}

	public void insertBiObjDataSet(Integer biObjId, Integer dsId, boolean isDetail, Session currSession) {
		logger.debug("IN");

		SbiObjDataSet toInsert = new SbiObjDataSet();
		SbiObjects sbiObject = (SbiObjects) currSession.load(SbiObjects.class, biObjId);

		toInsert.setDsId(dsId);
		toInsert.setSbiObject(sbiObject);

		toInsert.setIsDetail(isDetail);

		updateSbiCommonInfo4Insert(toInsert);

		currSession.save(toInsert);

		logger.debug("OUT");
	}

	private BIObjDataSet toBIObjDataSet(SbiObjDataSet hibObjDataSet) throws EMFUserError {
		logger.debug("IN");

		BIObjDataSet aBIObjDataSet = new BIObjDataSet();
		aBIObjDataSet.setBiObjDsId(hibObjDataSet.getBiObjDsId());

		SbiObjects hibObj = hibObjDataSet.getSbiObject();
		aBIObjDataSet.setBiObject(new BIObjectDAOHibImpl().toBIObject(hibObj, null));

		aBIObjDataSet.setDataSetId(hibObjDataSet.getDsId());

		if (hibObjDataSet.isIsDetail() == null) {
			aBIObjDataSet.setIsDetail(false);
		} else {
			aBIObjDataSet.setIsDetail(hibObjDataSet.isIsDetail());
		}

		logger.debug("OUT");
		return aBIObjDataSet;

	}

	public void eraseBIObjDataSet(BIObjDataSet aBIObjDataSet, Session currSession) {

		logger.debug("IN");
		SbiObjDataSet hibObjDataSet = (SbiObjDataSet) currSession.load(SbiObjDataSet.class,
				aBIObjDataSet.getBiObjDsId());
		currSession.delete(hibObjDataSet);
		logger.debug("deleted association with id " + aBIObjDataSet.getBiObjDsId());
		logger.debug("OUT");
	}

	@Override
	public void eraseBIObjDataSetByObjectId(Integer biObjId) throws EMFUserError {

		logger.debug("IN");

		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");

			eraseBIObjDataSetByObjectId(biObjId, session);

			transaction.commit();
		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException("Error while deleting the objDataset associated with object" + biObjId, t);
		} finally {
			closeSessionIfOpen(session);
		}
		logger.debug("OUT");
	}

	@Override
	public void eraseBIObjDataSetByObjectId(Integer biObjId, Session currSession) throws EMFUserError {

		logger.debug("IN");

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = :biObjId  ";
		Query hqlQuery = currSession.createQuery(hql);
		hqlQuery.setParameter("biObjId", biObjId);
		List<SbiObjDataSet> hibObjectPars = hqlQuery.list();

		for (Iterator<SbiObjDataSet> iterator = hibObjectPars.iterator(); iterator.hasNext();) {
			SbiObjDataSet sbiObjDataSet = iterator.next();
			currSession.delete(sbiObjDataSet);

		}
		logger.debug("OUT");
	}

	@Override
	public ArrayList<BIObject> getBIObjectsUsingDataset(Integer datasetId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObject> toReturn = new ArrayList<>();

		String hql = "from SbiObjDataSet s where s.dsId = :datasetId";
		Query hqlQuery = currSession.createQuery(hql);
		hqlQuery.setParameter("datasetId", datasetId);
		List<SbiObjDataSet> hibObjectPars = hqlQuery.list();

		Iterator<SbiObjDataSet> it = hibObjectPars.iterator();

		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet(it.next());
			BIObject obj = aBIObjectDataSet.getBiObject();
			toReturn.add(obj);
		}
		logger.debug("OUT");
		return toReturn;

	}

	@Override
	public ArrayList<BIObject> getBIObjectsUsingDataset(Integer datasetId) throws EMFUserError {
		logger.debug("IN");
		Session session = null;
		Transaction transaction = null;
		ArrayList<BIObject> biObjects = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();
			biObjects = getBIObjectsUsingDataset(datasetId, session);

		} catch (Throwable t) {
			rollbackIfActive(transaction);
			throw new SpagoBIDAOException(
					"Error while getting the objects associated with the data set with id " + datasetId, t);
		} finally {
			closeSessionIfOpen(session);
			logger.debug("OUT");
		}
		return biObjects;
	}

	@Override
	public List<SbiDataSet> getDatasetsByBIObject(Integer biObjId) throws EMFUserError {
		logger.debug("IN");

		ArrayList<SbiDataSet> toReturn = new ArrayList<>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = :biObjId";
		Query hqlQuery = getSession().createQuery(hql);
		hqlQuery.setParameter("biObjId", biObjId);
		List<SbiObjDataSet> hibSbiObjDs = hqlQuery.list();

        for (SbiObjDataSet aSbiObjDataSet : hibSbiObjDs) {
            SbiDataSet sbiDataSet = DAOFactory.getSbiDataSetDAO().loadSbiDataSetByIdAndOrganiz(aSbiObjDataSet.getDsId(), getTenant());
            toReturn.add(sbiDataSet);
        }

		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public BIObjDataSet getObjectDetailDataset(Integer objectId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		BIObjDataSet aBIObjectDataSet = null;
		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = :objectId  AND isDetail = :isDetail ";
		Query hqlQuery = currSession.createQuery(hql);
		hqlQuery.setParameter("objectId", objectId);
		hqlQuery.setParameter("isDetail", true);
		SbiObjDataSet hibDs = (SbiObjDataSet) hqlQuery.uniqueResult();

		if (hibDs != null) {
			aBIObjectDataSet = toBIObjDataSet(hibDs);
		}

		logger.debug("OUT");

		return aBIObjectDataSet;
	}

	@Override
	public ArrayList<BIObjDataSet> getObjectNotDetailDataset(Integer objectId, Session currSession)
			throws EMFUserError {
		logger.debug("IN");
		ArrayList<BIObjDataSet> toReturn = new ArrayList<>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId =  :objectId AND (isDetail =:isDetail OR isDetail is null)";
		Query hqlQuery = currSession.createQuery(hql);
		hqlQuery.setParameter("objectId", objectId);
		hqlQuery.setParameter("isDetail", false);

		List<SbiObjDataSet> hibObjectPars = hqlQuery.list();

		Iterator<SbiObjDataSet> it = hibObjectPars.iterator();

		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet(it.next());
			toReturn.add(aBIObjectDataSet);
		}

		logger.debug("OUT");

		return toReturn;
	}

	@Override
	public void updateObjectDetailDataset(Integer objectId, Integer dsId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		BIObjDataSet toDelete = getObjectDetailDataset(objectId, currSession);

		// modify only if dataset is changed
		if (toDelete != null && toDelete.getDataSetId() != null && dsId != null
				&& toDelete.getDataSetId().equals(dsId)) {
			logger.debug(
					"Keeping association between object with id " + objectId + " and detail dataset with id" + dsId);
		} else {
			logger.debug("dataset is changed for object with id " + objectId);

			if (toDelete != null) {
				logger.debug("Delet association with dataset with id " + toDelete.getDataSetId());
				// delete previous detail objDataSet
				eraseBIObjDataSet(toDelete, currSession);
			}
			if (dsId != null) {
				logger.debug("Insert association with dataset with id " + dsId);
				insertBiObjDataSet(objectId, dsId, true, currSession);
			}
		}

		logger.debug("IN");
	}

	@Override
	public void insertOrUpdateDatasetDependencies(BIObject biObject, ObjTemplate template, Session session) {

		logger.debug("IN");

		try {
			Assert.assertNotNull(session, "session cannot be null");

			byte[] documentTemplate = template.getContent();
			String driverName = biObject.getEngine().getDriverName();
			if (driverName != null && !"".equals(driverName)) {
				try {
					IEngineDriver driver = (IEngineDriver) Class.forName(driverName).newInstance();
					ArrayList<String> datasetsAssociated = driver.getDatasetAssociated(documentTemplate);
					if (datasetsAssociated != null && !datasetsAssociated.isEmpty()) {

						DAOFactory.getSbiObjDsDAO().deleteObjDsbyObjId(biObject.getId());

						for (Iterator<String> iterator = datasetsAssociated.iterator(); iterator.hasNext();) {
							String string = iterator.next();
							logger.debug(
									"Dataset associated to biObject with label " + biObject.getLabel() + ": " + string);
						}

						IBIObjDataSetDAO biObjDatasetDAO = DAOFactory.getBIObjDataSetDAO();
						biObjDatasetDAO.updateObjectNotDetailDatasets(biObject, datasetsAssociated, session);
					} else {
						logger.debug("No dataset associated to template");
					}
				} catch (Exception e) {
					logger.error("Error while inserting dataset dependencies; check template format", e);
					throw new RuntimeException("Impossible to add template [" + template.getName() + "] to document ["
							+ template.getBiobjId()
							+ "]; error while recovering dataset associations; check template format.");
				}
			}
			session.flush();
		} catch (Throwable t) {
			throw new SpagoBIDAOException(
					"Error while deleting the objDataset associated with object" + biObject.getId(), t);
		}
		logger.debug("OUT");

	}

}
