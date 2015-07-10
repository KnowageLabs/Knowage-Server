package it.eng.spagobi.tools.dataset.dao;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SbiDataSetDAOImpl extends AbstractHibernateDAO implements ISbiDataSetDAO {

	static private Logger logger = Logger.getLogger(SbiDataSetDAOImpl.class);

	@Override
	public SbiDataSet loadSbiDataSetByLabel(String label) {
		Session session;
		Transaction transaction;

		logger.debug("IN");

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
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}
			Query hibQuery = session.createQuery("from SbiDataSet h where h.active = ? and h.label = ? ");
			hibQuery.setBoolean(0, true);
			hibQuery.setString(1, label);
			SbiDataSet sbiDataSet = (SbiDataSet) hibQuery.uniqueResult();

			transaction.commit();

			return sbiDataSet;
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading dataset whose label is equal to [" + label + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	@Override
	public List<SbiDataSet> loadSbiDataSets() {
		return loadDataSets(null, null, null, null, null, null, null);
	}

	@Override
	public List<SbiDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, String category,
			String implementation) {

		Session session = getSession();

		logger.debug("IN");

		session = null;
		try {
			// open session
			session = getSession();

			// create statement
			String statement = "from SbiDataSet h where h.active = ?";
			if (owner != null) {
				if (includePublic != null && includePublic == true) {
					String ownedCondition = includeOwned ? "h.owner = ?" : "h.owner != ?";
					statement += " and (" + ownedCondition + " or h.publicDS = ?) ";
				} else {
					String ownedCondition = includeOwned ? "h.owner = ?" : "h.owner != ?";
					statement += " and " + ownedCondition + " ";
				}
			}
			if (scope != null)
				statement += " and h.publicDS = ? ";
			if (type != null)
				statement += " and h.scope.valueCd = ? ";
			if (category != null)
				statement += " and h.category.valueCd = ? ";
			if (implementation != null)
				statement += " and h.type = ? ";

			// inject parameters
			int paramIndex = 0;
			Query query = session.createQuery(statement);
			query.setBoolean(paramIndex++, true);
			if (owner != null) {
				query.setString(paramIndex++, owner);
				if (includePublic != null && includePublic == true) {
					query.setBoolean(paramIndex++, true);
				}
			}
			if (scope != null)
				query.setBoolean(paramIndex++, "PUBLIC".equalsIgnoreCase(scope));
			if (type != null)
				query.setString(paramIndex++, type);
			if (category != null)
				query.setString(paramIndex++, category);
			if (implementation != null)
				query.setString(paramIndex++, implementation);

			return executeQuery(query, session);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading dataset whose owner is equal to [" + owner + "]", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
	}

	private List<SbiDataSet> executeQuery(Query query, Session session) {
		List<SbiDataSet> sbiDataSetList;
		Transaction transaction;

		logger.debug("IN");

		transaction = null;
		try {
			transaction = beginTransaction(session);
			sbiDataSetList = query.list();

			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDOAException("An unexpected error occured while loading dataset", t);
		} finally {
			logger.debug("OUT");
		}

		return sbiDataSetList;
	}

	private Transaction beginTransaction(Session session) {
		Transaction transaction = null;
		try {
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
		}

		return transaction;
	}

}
