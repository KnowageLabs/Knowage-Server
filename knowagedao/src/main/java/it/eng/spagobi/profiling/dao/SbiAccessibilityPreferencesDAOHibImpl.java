package it.eng.spagobi.profiling.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAccessibilityPreferences;
import it.eng.spagobi.profiling.bean.SbiUser;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class SbiAccessibilityPreferencesDAOHibImpl extends AbstractHibernateDAO implements ISbiAccessibilityPreferencesDAO {
	static private Logger logger = Logger.getLogger(SbiAccessibilityPreferencesDAOHibImpl.class);

	@Override
	public SbiAccessibilityPreferences readUserAccessibilityPreferences(String userId) throws EMFUserError {
		logger.debug("IN");
		SbiAccessibilityPreferences toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criteria criteria = aSession.createCriteria(SbiAccessibilityPreferences.class);

			criteria.createAlias("user", "u").add(Restrictions.eq("u.userId", userId));
			// String q = "from SbiAccessibilityPreferences ";
			// Query query = aSession.createQuery(q);
			// query.setString("userId", userId);
			if (!criteria.list().isEmpty()) {
				toReturn = (SbiAccessibilityPreferences) criteria.list().get(0);
			}
			tx.commit();
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
		logger.debug("OUT");

		return toReturn;
	}

	@Override
	public void saveOrUpdatePreferencesControls(String userId, boolean enableUIO, boolean enableRobobrailles, boolean enableVoice,
			boolean enableGraphSonification) throws EMFUserError {
		SbiAccessibilityPreferences ap = this.readUserAccessibilityPreferences(userId);
		ISbiUserDAO userDao;

		userDao = DAOFactory.getSbiUserDAO();

		if (ap != null) {

			ap.setEnableUio(enableUIO);
			ap.setEnableRobobraille(enableRobobrailles);
			ap.setEnableVoice(enableVoice);
			ap.setEnableGraphSonification(enableGraphSonification);

			this.updateAccesibilityPreferences(ap);

		} else {

			this.saveAccessibilityPreferences(userId, enableUIO, enableRobobrailles, enableVoice, enableGraphSonification, null);
		}

	}

	@Override
	public void saveOrUpdateUserPreferences(String userId, String preferences) throws EMFUserError {
		SbiAccessibilityPreferences ap = this.readUserAccessibilityPreferences(userId);
		ISbiUserDAO userDao;

		userDao = DAOFactory.getSbiUserDAO();

		if (ap != null) {

			this.updateUiSettings(userId, preferences);

		} else {

			this.saveAccessibilityPreferences(userId, true, false, false, false, preferences);
		}
	}

	@Override
	public Integer saveAccessibilityPreferences(String userId, boolean enableUIO, boolean enableRobobrailles, boolean enableVoice,
			boolean enableGraphSonification, String preferences) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query q = aSession.createQuery("FROM SbiUser as user where user.userId ='" + userId + "'");
			SbiUser user = (SbiUser) q.uniqueResult();

			SbiAccessibilityPreferences ap = new SbiAccessibilityPreferences();
			ap.setUser(user);
			ap.setEnableUio(enableUIO);
			ap.setEnableRobobraille(enableRobobrailles);
			ap.setEnableGraphSonification(enableGraphSonification);
			ap.setEnableVoice(enableVoice);
			ap.setPreferences(preferences);
			updateSbiCommonInfo4Insert(ap);
			Integer id = (Integer) aSession.save(ap);
			tx.commit();
			return id;

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

	}

	@Override
	public void updateAccesibilityPreferences(SbiAccessibilityPreferences ap) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			updateSbiCommonInfo4Update(ap);
			aSession.update(ap);
			tx.commit();

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

	}

	public void updateUiSettings(String userId, String preferences) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Query q = aSession.createQuery("FROM SbiUser as user where user.userId ='" + userId + "'");
			SbiUser user = (SbiUser) q.uniqueResult();

			Criteria criteria = aSession.createCriteria(SbiAccessibilityPreferences.class);

			criteria.createAlias("user", "u").add(Restrictions.eq("u.userId", userId));

			SbiAccessibilityPreferences ap = null;
			if (!criteria.list().isEmpty()) {
				ap = (SbiAccessibilityPreferences) criteria.list().get(0);
			}

			if (ap != null) {
				ap.setUser(user);
				ap.setPreferences(preferences);
				updateSbiCommonInfo4Update(ap);
				aSession.update(ap);
			}
			tx.commit();

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
	}

}
