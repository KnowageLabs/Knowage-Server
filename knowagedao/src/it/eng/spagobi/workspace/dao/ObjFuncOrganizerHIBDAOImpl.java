package it.eng.spagobi.workspace.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.workspace.bo.DocumentOrganizer;
import it.eng.spagobi.workspace.metadata.SbiObjFuncOrganizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class ObjFuncOrganizerHIBDAOImpl extends AbstractHibernateDAO implements IObjFuncOrganizerDAO {

	private static transient Logger logger = Logger.getLogger(ObjFuncOrganizerHIBDAOImpl.class);

	@Override
	public List loadDocumentsByFolder(Integer folderId) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List listOfDocuments = null;
		List toReturn = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria criteria = aSession.createCriteria(SbiObjFuncOrganizer.class);
			// Criterion folderIdCriterion =
			// Expression.eq("id.sbiFunctionsOrganizer.funcId", folderId);

			criteria.add(Restrictions.eq("id.sbiFunctionsOrganizer.functId", folderId));
			listOfDocuments = criteria.list();
			Iterator it = listOfDocuments.iterator();

			while (it.hasNext()) {
				SbiObjFuncOrganizer hibObj = (SbiObjFuncOrganizer) it.next();
				toReturn.add(toDocumentOrganizer(hibObj));
			}
			return toReturn;
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			try {
				throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");
			} catch (EMFInternalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	@Override
	public SbiObjFuncOrganizer addDocumentToOrganizer(Integer documentId) throws EMFUserError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeDocumentFromOrganizer(SbiObjFuncOrganizer documentToRemove) throws EMFUserError {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveDocumentToDifferentFolder(SbiObjFuncOrganizer documentToMove) throws EMFUserError {
		// TODO Auto-generated method stub

	}

	private DocumentOrganizer toDocumentOrganizer(SbiObjFuncOrganizer hibObj) {
		DocumentOrganizer toReturn = new DocumentOrganizer();

		SbiObjects sbiObj = hibObj.getId().getSbiObjects();
		toReturn.setBiObjId(sbiObj.getBiobjId());
		toReturn.setDocumentLabel(sbiObj.getLabel());
		toReturn.setDocumentName(sbiObj.getName());
		toReturn.setDocumentDescription(sbiObj.getDescr());
		toReturn.setDocumentType(sbiObj.getObjectTypeCode());
		toReturn.setFunctId(hibObj.getId().getSbiFunctionsOrganizer().getFunctId());

		return toReturn;
	}

}
