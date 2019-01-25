/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiNewsDAOImpl extends AbstractHibernateDAO implements ISbiNewsDAO {

	private static Logger logger = Logger.getLogger(SbiNewsDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<SbiNews> getAllNews() {

		logger.debug("IN");
		List<SbiNews> listOfNews = new ArrayList<SbiNews>();
		Session session = null;

		try {
			session = getSession();
			String hql = "from SbiNews";
			Query query = session.createQuery(hql);
			listOfNews = query.list();

		} catch (HibernateException e) {
			logException(e);

			throw new SpagoBIRuntimeException(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return listOfNews;
	}

	@Override
	public void deleteNew(Integer newsId) {

		logger.debug("IN");
		Transaction transaction = null;
		Session session = null;
		SbiNews newsForDelete;

		try {

			session = getSession();
			transaction = session.beginTransaction();
			String hql = "from SbiNews s where s.id = :newId";
			Query query = session.createQuery(hql);
			query.setInteger("newsId", newsId);
			newsForDelete = (SbiNews) query.uniqueResult();

			session.delete(newsForDelete);
			session.flush();
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);
			logger.error("Error in deleting news", e);
			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException("Could not delete news", e);

		} finally {

			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");

	}

	@Override
	public void createOrUpdate(SbiNews sbiNews) {

		logger.debug("IN");
		Transaction transaction = null;
		Session session = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();
			updateSbiCommonInfo4Insert(sbiNews);
			session.save(sbiNews);
			session.flush();
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);
			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException("Could not insert news", e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");

	}

}
