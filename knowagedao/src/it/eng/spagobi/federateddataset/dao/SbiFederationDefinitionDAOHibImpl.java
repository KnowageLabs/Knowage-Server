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

package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.federateddataset.metadata.SbiDataSetFederation;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SbiFederationDefinitionDAOHibImpl extends AbstractHibernateDAO implements ISbiFederationDefinitionDAO {

	static private Logger logger = Logger.getLogger(SbiFederationDefinitionDAOHibImpl.class);

	/**
	 * Saves the FederationDefinition. If already exist returns that one
	 *
	 * @param dataset
	 */
	@Override
	public int saveSbiFederationDefinitionNoDuplicated(FederationDefinition federationDefinition) {
		return saveSbiFederationDefinition(federationDefinition, false);
	}

	/**
	 * Saves the FederationDefinition. If already exist one with same label
	 * thrown an exception
	 *
	 * @param dataset
	 */
	@Override
	public int saveSbiFederationDefinition(FederationDefinition federationDefinition) {
		return saveSbiFederationDefinition(federationDefinition, true);
	}

	private int saveSbiFederationDefinition(FederationDefinition dataset, boolean duplicated) {
		LogMF.debug(logger, "IN:  model = [{0}]", dataset);

		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			int id = saveSbiFederationDefinition(dataset, duplicated, session, transaction).getFederation_id();
			transaction.commit();
			return id;
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

	}

	public SbiFederationDefinition saveSbiFederationDefinition(FederationDefinition dataset, boolean duplicated, Session session, Transaction transaction) {
		LogMF.debug(logger, "IN:  model = [{0}]", dataset);

		if (dataset == null) {
			throw new IllegalArgumentException("Input parameter [dataset] cannot be null");
		}

		try {
			Assert.assertNotNull(transaction, "transaction cannot be null");

		} catch (Throwable t) {
			throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
		}

		if (!duplicated) {
			logger.debug("Checking if the federation already exists");
			Query hibQuery = session.createQuery(" from SbiFederationDefinition fd where fd.label = ? ");
			hibQuery.setString(0, dataset.getLabel());
			SbiFederationDefinition sbiResult = (SbiFederationDefinition) hibQuery.uniqueResult();
			if (sbiResult != null) {
				logger.debug("The federation already exisists and the id is " + sbiResult.getFederation_id());
				dataset.setFederation_id(sbiResult.getFederation_id());

				return sbiResult;
			}
			logger.debug("The federation doesn't exist");
		}

		SbiFederationDefinition hibFederatedDataset = new SbiFederationDefinition();

		hibFederatedDataset.setFederation_id(dataset.getFederation_id());
		hibFederatedDataset.setLabel(dataset.getLabel());
		hibFederatedDataset.setName(dataset.getName());
		hibFederatedDataset.setDescription(dataset.getDescription());
		hibFederatedDataset.setRelationships(dataset.getRelationships());
		hibFederatedDataset.setSourceDatasets(SbiFederationUtils.toSbiDataSet(dataset.getSourceDatasets()));
		hibFederatedDataset.setDegenerated(dataset.isDegenerated());

		updateSbiCommonInfo4Insert(hibFederatedDataset);
		session.save(hibFederatedDataset);
		return hibFederatedDataset;

	}

	@Override
	public FederationDefinition loadFederationDefinition(Integer id) throws EMFUserError {

		logger.debug("IN: loading federation");
		Session aSession = null;
		Transaction tx = null;
		FederationDefinition toReturn = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiFederationDefinition fd where fd.federation_id = ? ");
			hibQuery.setInteger(0, id);
			SbiFederationDefinition sbiResult = (SbiFederationDefinition) hibQuery.uniqueResult();

			toReturn = SbiFederationUtils.toDatasetFederationWithDataset(sbiResult, loadDatasetsUsedByFederation(sbiResult.getFederation_id(), aSession));

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Loading dataset federation", he);
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
		return toReturn;

	}

	@Override
	public SbiFederationDefinition loadSbiFederationDefinition(Integer id, Session session) throws EMFUserError {

		logger.debug("IN: loading federation");

		Query hibQuery = session.createQuery(" from SbiFederationDefinition fd where fd.federation_id = ? ");
		hibQuery.setInteger(0, id);
		SbiFederationDefinition sbiResult = (SbiFederationDefinition) hibQuery.uniqueResult();

		logger.debug("OUT");
		return sbiResult;

	}

	@Override
	public List<FederationDefinition> loadAllFederatedDataSets() throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<FederationDefinition> realResult = new ArrayList<FederationDefinition>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiFederationDefinition");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(SbiFederationUtils.toDatasetFederationNoDataset((SbiFederationDefinition) it.next()));
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

	@Override
	public List<FederationDefinition> loadNotDegeneratedFederatedDataSets() throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List<FederationDefinition> realResult = new ArrayList<FederationDefinition>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiFederationDefinition sfd where sfd.degenerated=false ");

			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(SbiFederationUtils.toDatasetFederationNoDataset((SbiFederationDefinition) it.next()));
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

	@Override
	public List<FederationDefinition> loadFederationsUsingDataset(Integer dsId) throws EMFUserError {
		logger.debug("IN");

		List<FederationDefinition> toReturn = new ArrayList<FederationDefinition>();

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = loadFederationsUsingDataset(dsId, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in loading datasets linked to federation", he);
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
		return toReturn;
	}

	/**
	 * Counts number of BIObj associated.
	 *
	 * @param dsId
	 *            the ds id
	 * @return Integer, number of BIObj associated
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	@Override
	public Integer countFederationsUsingDataset(Integer dsId) {
		logger.debug("IN");
		Integer resultNumber = new Integer(0);
		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "select count(*) from SbiDataSetFederation s where s.id.dsId = ? ";
			Query aQuery = session.createQuery(hql);
			aQuery.setInteger(0, dsId.intValue());
			resultNumber = new Integer(((Long) aQuery.uniqueResult()).intValue());

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("Error while counting the federations associated with the data set with id " + dsId, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return resultNumber;

	}

	@Override
	public List<FederationDefinition> loadFederationsUsingDataset(Integer dsId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<FederationDefinition> toReturn = new ArrayList<FederationDefinition>();

		Query hibQuery = currSession.createQuery("from SbiDataSetFederation s where s.id.dsId= ?");
		hibQuery.setInteger(0, dsId);
		List hibDsFed = hibQuery.list();

		Iterator it = hibDsFed.iterator();
		while (it.hasNext()) {
			SbiDataSetFederation sbiDsFed = (SbiDataSetFederation) it.next();
			SbiFederationDefinition sbiFedDef = (SbiFederationDefinition) currSession.load(SbiFederationDefinition.class, sbiDsFed.getId().getFederationId());
			FederationDefinition fedDef = SbiFederationUtils.toDatasetFederationNoDataset(sbiFedDef);
			toReturn.add(fedDef);

		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Loads the datsets linked to a federation
	 *
	 * @param federationID
	 * @param currSession
	 * @param profile
	 * @return
	 * @throws EMFUserError
	 */
	@Override
	public Set<IDataSet> loadAllFederatedDataSets(Integer federationID) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Set<IDataSet> realResult = new HashSet<IDataSet>();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			realResult = loadDatasetsUsedByFederation(federationID, aSession);

			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			logger.error("Error in loading datasets linked to federation", he);
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

	private Set<IDataSet> loadDatasetsUsedByFederation(Integer federationID, Session currSession) throws EMFUserError {
		logger.debug("IN");

		logger.debug("Loading dataset for federation " + federationID);
		Set<IDataSet> dataSets = new HashSet<IDataSet>();
		List<Integer> datasetIds = new ArrayList<Integer>();

		logger.debug("Getting SbiDataSetFederation");
		Query hibQuery = currSession.createQuery("from SbiDataSetFederation s where s.id.federationId= ?");
		hibQuery.setInteger(0, federationID);
		List hibDsFed = hibQuery.list();

		Iterator it = hibDsFed.iterator();
		while (it.hasNext()) {
			SbiDataSetFederation sbiDsFed = (SbiDataSetFederation) it.next();
			Integer dsId = sbiDsFed.getId().getDsId();
			datasetIds.add(dsId);
		}

		logger.debug("Getting source datasets");
		Query hibQueryDs = currSession.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId in ( :datasets )");
		hibQueryDs.setBoolean(0, true);
		hibQueryDs.setParameterList("datasets", datasetIds);
		List<SbiDataSet> dsActiveDetail = hibQueryDs.list();
		if (dsActiveDetail != null) {
			dataSets = DataSetFactory.toDataSet(dsActiveDetail, this.getUserProfile());
		}

		logger.debug("Loaded " + dataSets.size() + " dataset for federation " + federationID);
		logger.debug("OUT");
		return dataSets;
	}

	public void deleteFederatedDatasetById(Integer id, Session aSession) throws Exception {
		logger.debug("IN");
		SbiFederationDefinition federationToDelete = (SbiFederationDefinition) aSession.load(SbiFederationDefinition.class, id);
		aSession.delete(federationToDelete);
	}

	@Override
	public void deleteFederatedDatasetById(Integer id) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			deleteFederatedDatasetById(id, aSession);
			tx.commit();
		} catch (Exception he) {
			logException(he);
			logger.error("Error in loading datasets linked to federation", he);
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

	@Override
	public Integer modifyFederation(FederationDefinition fds) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer idToReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiFederationDefinition hibFederation = (SbiFederationDefinition) aSession.load(SbiFederationDefinition.class, fds.getFederation_id());

			hibFederation.setLabel(fds.getLabel());
			hibFederation.setName(fds.getName());
			hibFederation.setDescription(fds.getDescription());
			hibFederation.setRelationships(fds.getRelationships());
			hibFederation.setSourceDatasets(SbiFederationUtils.toSbiDataSet(fds.getSourceDatasets()));
			hibFederation.setDegenerated(fds.isDegenerated());

			updateSbiCommonInfo4Update(hibFederation);
			tx.commit();
			logger.debug("OUT");
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return idToReturn;

	}

}
