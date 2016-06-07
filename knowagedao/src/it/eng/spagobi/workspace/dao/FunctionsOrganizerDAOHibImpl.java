package it.eng.spagobi.workspace.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.workspace.bo.FunctionsOrganizer;
import it.eng.spagobi.workspace.metadata.SbiFunctionsOrganizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class FunctionsOrganizerDAOHibImpl extends AbstractHibernateDAO implements IFunctionsOrganizerDAO {

	private static transient Logger logger = Logger.getLogger(FunctionsOrganizerDAOHibImpl.class);

	@Override
	public List loadFolderByUser() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List listOfFolders = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			IEngUserProfile user = getUserProfile();
			String userId = user.getUserUniqueIdentifier().toString();
			tx = aSession.beginTransaction();

			Criteria criteria = getSession().createCriteria(SbiFunctionsOrganizer.class);
			Criterion rest1 = Restrictions.eq("code", userId);
			Criterion rest2 = Restrictions.eq("commonInfo.userIn", userId);
			criteria.add(Restrictions.or(rest1, rest2));
			listOfFolders = criteria.list();
			Iterator it = listOfFolders.iterator();

			while (it.hasNext()) {
				realResult.add(toFunctionsOrganizer((SbiFunctionsOrganizer) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);
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
	public SbiFunctionsOrganizer createFolder(SbiFunctionsOrganizer folder) throws EMFUserError {
		logger.debug("IN");
		IEngUserProfile user = getUserProfile();
		String userId = user.getUserUniqueIdentifier().toString();
		Session aSession = null;
		Transaction tx = null;
		SbiFunctionsOrganizer hibFunct = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			hibFunct = new SbiFunctionsOrganizer();
			hibFunct.setCode(folder.getCode());
			hibFunct.setDescr(folder.getDescr());
			hibFunct.setName(folder.getName());
			hibFunct.setPath(folder.getPath());
			hibFunct.setParentFunct(folder.getParentFunct());
			hibFunct.setProg(folder.getProg());

			updateSbiCommonInfo4Insert(hibFunct);
			aSession.save(hibFunct);

			tx.commit();
		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
					logger.debug("The [insertLowFunctionality] occurs. LowFunctionality cache will be cleaned.");

				}
				logger.debug("OUT");
			}
		}
		return hibFunct;
	}

	@Override
	public void deleteFolder(Integer folderId) throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			deleteOrganizerFolderById(folderId, aSession);
			tx.commit();
		} catch (Exception he) {
			logException(he);
			logger.error("Error in loading folder linked to user", he);
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

	public void deleteOrganizerFolderById(Integer id, Session aSession) throws Exception {
		logger.debug("IN");
		SbiFunctionsOrganizer folderToDelete = (SbiFunctionsOrganizer) aSession.load(SbiFunctionsOrganizer.class, id);
		aSession.delete(folderToDelete);
	}

	public static FunctionsOrganizer toFunctionsOrganizer(SbiFunctionsOrganizer sfo) {

		FunctionsOrganizer fo = new FunctionsOrganizer();
		fo.setFunctId(sfo.getFunctId());
		fo.setCode(sfo.getCode());
		fo.setDescr(sfo.getDescr());
		fo.setName(sfo.getName());
		fo.setParentFunct(sfo.getParentFunct());
		fo.setPath(sfo.getPath());
		fo.setProg(sfo.getProg());
		fo.setUserIn(sfo.getCommonInfo().getUserIn());
		fo.setTimeIn(sfo.getCommonInfo().getTimeIn());

		return fo;
	}

}
