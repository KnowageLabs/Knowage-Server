/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 21-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.tools.dataset.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiObjDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Defines the Hibernate implementations for all DAO methods, for a BI Object Dataset.
 * 
 * @author Gavardi
 */
public class BIObjDataSetDAOHibImpl extends AbstractHibernateDAO implements IBIObjDataSetDAO {
	static private Logger logger = Logger.getLogger(BIObjDataSetDAOHibImpl.class);

	@Override
	public void updateObjectNotDetailDatasets(BIObject biObj, ArrayList<String> dsLabels, Session currSession) throws EMFUserError {
		logger.debug("IN");

		logger.debug("update notDetail associations for biObj " + biObj.getId());

		Map<String, IDataSet> datasetsToInsert = new HashMap<String, IDataSet>();
		ArrayList<Integer> idsToInsert = new ArrayList<Integer>();

		// Load Dataset to insert
		for (Iterator iterator = dsLabels.iterator(); iterator.hasNext();) {
			String label = (String) iterator.next();
			if (!datasetsToInsert.keySet().contains(label)) {
				IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
				if (dataSet == null) {
					logger.debug("Dataset with label " + label + " indicated in template of object " + biObj.getLabel() + " is not found");
				} else {
					datasetsToInsert.put(label, dataSet);
					idsToInsert.add(dataSet.getId());
					logger.debug("To Associate object " + biObj.getLabel() + " with dataset " + label);
				}
			}
		}

		// delete previous relationships and insert new one, only not detail dataset
		logger.debug("Delete previous dataset associations if not present in current");
		ArrayList<Integer> idsAlreadyPresent = new ArrayList<Integer>();

		// load all associations, also detial one to avoid re-insert the same association again
		ArrayList<BIObjDataSet> previousAllAssociatedDatasets = getBiObjDataSets(biObj.getId(), currSession);
		for (Iterator iterator = previousAllAssociatedDatasets.iterator(); iterator.hasNext();) {
			BIObjDataSet biObjDataSet = (BIObjDataSet) iterator.next();
			if (!idsToInsert.contains(biObjDataSet.getDataSetId())) {
				// erase only if it is not detail
				if (biObjDataSet.getIsDetail() == false) {
					logger.debug("Delete association with dataset with id " + biObjDataSet.getDataSetId());
					eraseBIObjDataSet(biObjDataSet, currSession);
				}
			} else {
				idsAlreadyPresent.add(biObjDataSet.getDataSetId());
			}

		}

		logger.debug("Insert new dataset associations");
		for (Iterator iterator = datasetsToInsert.keySet().iterator(); iterator.hasNext();) {
			String dsLabel = (String) iterator.next();
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

	// public void updateObjectDatasets(BIObject biObj, ArrayList<String> dsLabels, Session currSession) throws EMFUserError {
	// logger.debug("IN");
	// logger.debug("update associations for biObj " + biObj.getId());
	//
	// Map<String, Boolean> labelsToInsert = new HashMap<String, Boolean>();
	// String innerDs = biObj.getDataSetLabel();
	// if (innerDs != null)
	// labelsToInsert.put(innerDs, Boolean.TRUE);
	//
	// for (Iterator iterator = dsLabels.iterator(); iterator.hasNext();) {
	// String string = (String) iterator.next();
	// if (!labelsToInsert.containsKey(string)) {
	// labelsToInsert.put(string, Boolean.FALSE);
	// }
	// }
	//
	// // delete previous relationships and insert new one
	// logger.debug("Delete previous dataset associations");
	// ArrayList<BIObjDataSet> associatedDatasets = getBiObjDataSets(biObj.getId(), currSession);
	// for (Iterator iterator = associatedDatasets.iterator(); iterator.hasNext();) {
	// BIObjDataSet biObjDataSet = (BIObjDataSet) iterator.next();
	// eraseBIObjDataSet(biObjDataSet, currSession);
	// }
	//
	// logger.debug("Insert new dataset associations");
	// for (Iterator iterator = labelsToInsert.keySet().iterator(); iterator.hasNext();) {
	// String dsLabel = (String) iterator.next();
	// IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel);
	// Boolean isDetail = labelsToInsert.get(dsLabel);
	// insertBiObjDataSet(biObj.getId(), dataSet.getId(), isDetail, currSession);
	// }
	//
	// logger.debug("OUT");
	//
	// }

	@Override
	public ArrayList<BIObjDataSet> getBiObjDataSets(Integer biObjId) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObjDataSet> toReturn = new ArrayList<BIObjDataSet>();

		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");

			toReturn = getBiObjDataSets(biObjId, session);

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("Error while getting datasets associated with object" + biObjId, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		logger.debug("OUT");
		return toReturn;

	}

	@Override
	public ArrayList<BIObjDataSet> getBiObjDataSets(Integer biObjId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObjDataSet> toReturn = new ArrayList<BIObjDataSet>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = " + biObjId + "";
		Query hqlQuery = currSession.createQuery(hql);
		List hibObjectPars = hqlQuery.list();

		Iterator it = hibObjectPars.iterator();
		int count = 1;
		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet((SbiObjDataSet) it.next());
			toReturn.add(aBIObjectDataSet);
		}

		logger.debug("OUT");
		return toReturn;
	}

	public ArrayList<BIObjDataSet> getBiObjNotDetailDataSets(Integer biObjId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObjDataSet> toReturn = new ArrayList<BIObjDataSet>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = " + biObjId + " AND (isDetail=false OR isDetail is null)";
		Query hqlQuery = currSession.createQuery(hql);
		List hibObjectPars = hqlQuery.list();

		Iterator it = hibObjectPars.iterator();
		int count = 1;
		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet((SbiObjDataSet) it.next());
			toReturn.add(aBIObjectDataSet);
		}

		logger.debug("OUT");
		return toReturn;
	}

	public void insertBiObjDataSet(Integer biObjId, Integer dsId, boolean isDetail, Session currSession) throws EMFUserError {
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

	public void eraseBIObjDataSet(BIObjDataSet aBIObjDataSet, Session currSession) throws EMFUserError {

		logger.debug("IN");
		SbiObjDataSet hibObjDataSet = (SbiObjDataSet) currSession.load(SbiObjDataSet.class, aBIObjDataSet.getBiObjDsId());
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

			String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = " + biObjId + "";
			Query hqlQuery = session.createQuery(hql);
			List hibObjectPars = hqlQuery.list();

			for (Iterator iterator = hibObjectPars.iterator(); iterator.hasNext();) {
				SbiObjDataSet sbiObjDataSet = (SbiObjDataSet) iterator.next();
				session.delete(sbiObjDataSet);

			}

			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("Error while deleting the objDataset associated with object" + biObjId, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public ArrayList<BIObject> getBIObjectsUsingDataset(Integer datasetId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObject> toReturn = new ArrayList<BIObject>();

		String hql = "from SbiObjDataSet s where s.dsId = " + datasetId;
		Query hqlQuery = currSession.createQuery(hql);
		List hibObjectPars = hqlQuery.list();

		Iterator it = hibObjectPars.iterator();
		int count = 1;
		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet((SbiObjDataSet) it.next());
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
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("Error while getting the objects associated with the data set with id " + datasetId, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return biObjects;
	}

	@Override
	public BIObjDataSet getObjectDetailDataset(Integer objectId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = " + objectId + " AND isDetail =" + true;
		Query hqlQuery = currSession.createQuery(hql);
		SbiObjDataSet hibDs = (SbiObjDataSet) hqlQuery.uniqueResult();

		BIObjDataSet aBIObjectDataSet = null;
		if (hibDs != null) {
			aBIObjectDataSet = toBIObjDataSet(hibDs);
		}

		logger.debug("OUT");

		return aBIObjectDataSet;
	}

	@Override
	public ArrayList<BIObjDataSet> getObjectNotDetailDataset(Integer objectId, Session currSession) throws EMFUserError {
		logger.debug("IN");
		ArrayList<BIObjDataSet> toReturn = new ArrayList<BIObjDataSet>();

		String hql = "from SbiObjDataSet s where s.sbiObject.biobjId = " + objectId + " AND (isDetail =" + false + " OR isDetail is null)";
		Query hqlQuery = currSession.createQuery(hql);

		List hibObjectPars = hqlQuery.list();

		Iterator it = hibObjectPars.iterator();
		int count = 1;
		while (it.hasNext()) {
			BIObjDataSet aBIObjectDataSet = toBIObjDataSet((SbiObjDataSet) it.next());
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
		if (toDelete != null && toDelete.getDataSetId() != null && dsId != null && toDelete.getDataSetId().equals(dsId)) {
			logger.debug("Keeping association between object with id " + objectId + " and detail dataset with id" + dsId);
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

}
