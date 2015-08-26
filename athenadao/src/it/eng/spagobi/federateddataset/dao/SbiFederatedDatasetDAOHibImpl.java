package it.eng.spagobi.federateddataset.dao;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.federateddataset.bo.FederatedDataset;
import it.eng.spagobi.federateddataset.metadata.SbiFederatedDataset;
import it.eng.spagobi.tools.catalogue.dao.MetaModelsDAOImpl;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

public class SbiFederatedDatasetDAOHibImpl extends AbstractHibernateDAO implements ISbiFederatedDatasetDAO {
	
	static private Logger logger = Logger.getLogger(SbiFederatedDatasetDAOHibImpl.class);

	@Override
	public void saveSbiFederatedDataSet(FederatedDataset dataset) {
		LogMF.debug(logger, "IN: model = [{0}]", dataset);
		
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
				

			} catch(Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			
			SbiFederatedDataset hibFederatedDataset =  new SbiFederatedDataset();
			
			hibFederatedDataset.setId_sbi_federated_data_set(dataset.getId_sbi_federated_data_set());
			hibFederatedDataset.setLabel(dataset.getLabel());
			hibFederatedDataset.setName(dataset.getName());
			hibFederatedDataset.setDescription(dataset.getDescription());
			hibFederatedDataset.setRelationships(dataset.getRelationships());
			
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



}
