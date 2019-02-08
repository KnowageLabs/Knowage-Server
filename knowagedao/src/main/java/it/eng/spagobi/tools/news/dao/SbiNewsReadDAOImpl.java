package it.eng.spagobi.tools.news.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.tools.news.metadata.SbiNewsRead;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiNewsReadDAOImpl extends AbstractHibernateDAO implements ISbiNewsReadDAO {

	private static Logger logger = Logger.getLogger(SbiNewsReadDAOImpl.class);

	@Override
	public Integer insert(Integer id) {

		Session session = null;
		Transaction transaction = null;
		SbiNews sbiNews;

		try {
			SbiNewsDAOImpl newsDAO = new SbiNewsDAOImpl();
			sbiNews = newsDAO.getSbiNewsById(id);

			if (sbiNews.getId() != null) {

				session = getSession();
				transaction = session.beginTransaction();

				SbiNewsRead newsRead = new SbiNewsRead();
				newsRead.setUser("biuser");
				newsRead.setNewsId(id);

				updateSbiCommonInfo4Insert(newsRead);
				session.save(newsRead);
				transaction.commit();

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
	public List<Integer> getReadNews(String user) {

		logger.debug("IN");

		List<Integer> listOfReads = new ArrayList<>();

		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "Select newsId from SbiNewsRead s WHERE s.user = :user";
			Query query = session.createQuery(hql);
			query.setString("user", user);
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

}
