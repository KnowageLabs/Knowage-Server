/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.federateddataset.metadata.SbiFederatedDataset;
import it.eng.spagobi.tools.dataset.federation.DatasetFederation;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SbiFederatedDatasetDAOHibImpl extends AbstractHibernateDAO implements ISbiFederatedDatasetDAO {

	static private Logger logger = Logger.getLogger(SbiFederatedDatasetDAOHibImpl.class);

	@Override
	public void saveSbiFederatedDataSet(DatasetFederation dataset) {
		LogMF.debug(logger, "IN:  model = [{0}]", dataset);

		Session session = null;
		Transaction transaction = null;

		try {
			if (dataset == null) {
				throw new IllegalArgumentException("Input parameter [dataset] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");

			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			SbiFederatedDataset hibFederatedDataset = new SbiFederatedDataset();

			hibFederatedDataset.setId_sbi_federated_data_set(dataset.getId_sbi_federated_data_set());
			hibFederatedDataset.setLabel(dataset.getLabel());
			hibFederatedDataset.setName(dataset.getName());
			hibFederatedDataset.setDescription(dataset.getDescription());
			hibFederatedDataset.setRelationships(dataset.getRelationships());
			hibFederatedDataset.setSourceDatasets(SbiFederationUtils.toSbiDataSet(dataset.getSourceDatasets()));

			updateSbiCommonInfo4Insert(hibFederatedDataset);
			session.save(hibFederatedDataset);


			transaction.commit();

			dataset.setId_sbi_federated_data_set(hibFederatedDataset.getId_sbi_federated_data_set());

		} catch (Throwable t) {
			logException(t);
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while saving model [" + dataset + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		logger.debug("OUT");

	}

	@Override
	public void getSbiFederatedDataSet(Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DatasetFederation> loadAllFederatedDataSets() throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<DatasetFederation> realResult = new ArrayList<DatasetFederation>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiFederatedDataset");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(SbiFederationUtils.toDatasetFederation((SbiFederatedDataset) it.next(), getUserProfile()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in loading all federated datasets", he);
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


	

	


}
