package it.eng.spagobi.functions.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.functions.metadata.SbiObjFunction;

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

	BIObjFunction toBIObjFunction(SbiObjFunction hibObjFunction) {
		return null;
	}
}
