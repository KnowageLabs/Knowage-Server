package it.eng.spagobi.functions.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.BIObjectDAOHibImpl;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.functions.metadata.SbiObjFunction;
import it.eng.spagobi.utilities.assertion.Assert;

public class BIObjFunctionDAOHibImpl extends AbstractHibernateDAO implements IBIObjFunctionDAO {

	static private Logger logger = Logger.getLogger(BIObjFunctionDAOHibImpl.class);

	@Override
	public ArrayList<BIObject> getBIObjectsUsingFunction(Integer functionId, Session currSession) throws EMFUserError {
		logger.debug("IN");

		ArrayList<BIObject> toReturn = new ArrayList<BIObject>();

		String hql = "from SbiObjFunction s where s.functionId = " + functionId;
		Query hqlQuery = currSession.createQuery(hql);
		List hibObjectPars = hqlQuery.list();

		Iterator it = hibObjectPars.iterator();
		int count = 1;
		while (it.hasNext()) {
			BIObjFunction aBIObjFunction = toBIObjFunction((SbiObjFunction) it.next());
			BIObject obj = aBIObjFunction.getBiObject();
			toReturn.add(obj);
		}
		logger.debug("OUT");
		return toReturn;
	}

	BIObjFunction toBIObjFunction(SbiObjFunction hibObjFunction) throws EMFUserError {
		logger.debug("IN");

		BIObjFunction aBIObjFunction = new BIObjFunction();
		aBIObjFunction.setBiObjFunctionId(hibObjFunction.getBiObjFunctionId());

		SbiObjects hibObj = hibObjFunction.getSbiObject();
		aBIObjFunction.setBiObject(new BIObjectDAOHibImpl().toBIObject(hibObj, null));

		aBIObjFunction.setFunctionId(hibObjFunction.getFunctionId());

		logger.debug("OUT");
		return aBIObjFunction;
	}

	@Override
	public void eraseBIObjFunctionByObjectId(Integer biObjId) throws EMFUserError {

		logger.debug("IN");

		Session session = null;
		Transaction transaction = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");

			eraseBIObjFunctionByObjectId(biObjId, session);

			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("Error while deleting the objDataset associated with object" + biObjId, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public void eraseBIObjFunctionByObjectId(Integer biObjId, Session currSession) throws EMFUserError {

		logger.debug("IN");

		String hql = "from SbiObjFunction s where s.sbiObject.biobjId = " + biObjId + "";
		Query hqlQuery = currSession.createQuery(hql);
		List hibObjectPars = hqlQuery.list();

		for (Iterator iterator = hibObjectPars.iterator(); iterator.hasNext();) {
			SbiObjFunction sbiObjFunction = (SbiObjFunction) iterator.next();
			currSession.delete(sbiObjFunction);

		}
		logger.debug("OUT");
	}
}
