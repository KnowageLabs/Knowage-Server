package it.eng.spagobi.commons.dao;

import it.eng.spagobi.commons.metadata.SbiProductType;
import it.eng.spagobi.commons.metadata.SbiProductTypeEngine;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ProductTypeDAOHibImpl extends AbstractHibernateDAO implements IProductTypeDAO {
	static private Logger logger = Logger.getLogger(ProductTypeDAOHibImpl.class);

	@Override
	public List<SbiProductType> loadAllProductType() {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String q = "from SbiProductType";
			Query query = aSession.createQuery(q);
			ArrayList<SbiProductType> result = (ArrayList<SbiProductType>) query.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error getting product types", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public List<SbiProductTypeEngine> loadSelectedEngines(String productType) {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query hibQuery = aSession.createQuery("from SbiProductTypeEngine pe where pe.sbiProductType.label = :productTypeLabel");
			hibQuery.setString("productTypeLabel", productType);
			ArrayList<SbiProductTypeEngine> result = (ArrayList<SbiProductTypeEngine>) hibQuery.list();
			return result;
		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new SpagoBIRuntimeException("Error getting Product type Engines", he);
		} finally {
			logger.debug("OUT");
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

}
