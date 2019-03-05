package it.eng.spagobi.tools.news.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.tools.news.metadata.SbiNewsRead;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiNewsReadDAOImpl extends AbstractHibernateDAO implements ISbiNewsReadDAO {

	private static Logger logger = Logger.getLogger(SbiNewsReadDAOImpl.class);

	@Override
	public Integer insertNewsRead(Integer id, UserProfile profile) {

		Session session = null;
		Transaction transaction = null;
		SbiNews sbiNews;

		try {

			if (getNewsReadByIdAndUser(id, String.valueOf(profile.getUserId())) != null) {
				throw new SpagoBIRuntimeException("The message is alredy read");
			}

			SbiNewsDAOImpl newsDAO = new SbiNewsDAOImpl();
			sbiNews = newsDAO.getSbiNewsById(id, profile);

			if (sbiNews.getId() != null) {

				if (UserUtilities.isTechnicalUser(profile) || newsDAO.getAvailableNews(sbiNews, profile) != null) {

					session = getSession();
					transaction = session.beginTransaction();

					SbiNewsRead newsRead = new SbiNewsRead();
					newsRead.setUser(String.valueOf(profile.getUserId()));
					newsRead.setNewsId(id);

					updateSbiCommonInfo4Insert(newsRead);
					session.save(newsRead);
					transaction.commit();

				} else {
					throw new SpagoBIRuntimeException("You are not allowed to get this news");
				}

			} else {

				throw new SpagoBIRuntimeException("An error has occured while getting news by id!");
			}

		} catch (HibernateException e) {
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException("Cannot insert", e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return id;
	}

	@Override
	public List<Integer> getReadNews(UserProfile profile) {

		logger.debug("IN");

		List<Integer> listOfReads = new ArrayList<>();

		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "select s.newsId from SbiNewsRead s WHERE s.user = :user AND s.newsId in (select news.id from SbiNews news "
					+ "where news.active = true and news.expirationDate >= current_date)";
			Query query = session.createQuery(hql);

			query.setString("user", String.valueOf(profile.getUserId()));
			listOfReads = query.list();

			transaction.commit();

		} catch (HibernateException e) {
			logException(e);

			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException("Cannot get read news for this user ", e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return listOfReads;
	}

	@Override
	public SbiNewsRead getNewsReadByIdAndUser(Integer id, String user) {

		logger.debug("IN");
		SbiNewsRead sbiNewsRead = null;
		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "from SbiNewsRead s WHERE s.newsId = :newsReadID and s.user = :user";
			Query query = session.createQuery(hql);
			query.setInteger("newsReadID", id);
			query.setString("user", user);

			sbiNewsRead = (SbiNewsRead) query.uniqueResult();
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);

			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException("Cannot get newsRead with id = " + id, e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return sbiNewsRead;

	}

}
