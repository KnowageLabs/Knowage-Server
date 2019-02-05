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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.dao.RoleDAOHibImpl;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.tools.news.bo.News;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiNewsDAOImpl extends AbstractHibernateDAO implements ISbiNewsDAO {

	private static Logger logger = Logger.getLogger(SbiNewsDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List getAllNews() {

		logger.debug("IN");
		List listOfNews = new ArrayList<>();
		Session session = null;

		try {
			session = getSession();
			String hql = " from SbiNews s";
			Query query = session.createQuery(hql);

			List hibList = query.list();
			Iterator iterator = hibList.iterator();
			while (iterator.hasNext()) {
				SbiNews hibNews = (SbiNews) iterator.next();
				if (hibNews != null) {
					News news = toBasicNews(hibNews);
					listOfNews.add(news);
				}
			}

		} catch (HibernateException e) {
			logException(e);

			throw new SpagoBIRuntimeException("Cannot return all news", e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return listOfNews;
	}

	@Override
	public News getNewsById(Integer id) {
		SbiNews sbiNews = null;
		logger.debug("IN");
		News newsToReturn = null;
		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "from SbiNews s WHERE s.id = :id";
			Query query = session.createQuery(hql);
			query.setInteger("id", id);
			sbiNews = (SbiNews) query.uniqueResult();

			if (sbiNews == null)
				return null;

			newsToReturn = toAdvancedNews(sbiNews);
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);

			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException(e.getMessage());

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return newsToReturn;
	}

	@Override
	public News toBasicNews(SbiNews hibNews) {

		News news = new News();
		news.setId(hibNews.getId());
		news.setTitle(hibNews.getName());
		news.setDescription(hibNews.getDescription());

		return news;
	}

	private News toAdvancedNews(SbiNews hibNews) {

		logger.debug("IN");

		News news = new News();
		news.setId(hibNews.getId());
		news.setTitle(hibNews.getName());
		news.setDescription(hibNews.getDescription());

		news.setType(hibNews.getCategoryId());
		news.setHtml(hibNews.getNews());
		news.setExpirationDate(hibNews.getExpirationDate());

		try {
			Set listOfRoles = new HashSet();
			Set<SbiExtRoles> setOfRoles = hibNews.getSbiNewsRoles();
			Iterator<SbiExtRoles> iterator = setOfRoles.iterator();
			while (iterator.hasNext()) {
				SbiExtRoles sbiExtrole = iterator.next();
				RoleDAOHibImpl roleDAO = new RoleDAOHibImpl();
				Role role = roleDAO.toBasicRole(sbiExtrole);
				listOfRoles.add(role);
			}

			news.setRoles(listOfRoles);

		} catch (Exception e) {
			logException(e);
			logger.error("Error while getting role for news", e);
			throw new SpagoBIRuntimeException(e.getMessage());
		}

		logger.debug("OUT");
		return news;
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
			query.setInteger("newId", newsId);
			newsForDelete = (SbiNews) query.uniqueResult();

			session.delete(newsForDelete);
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);
			logger.error("Error in deleting news", e);
			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException(e.getMessage());

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
			throw new SpagoBIRuntimeException(e.getMessage());

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");

	}

	@Override
	public News saveNews(News aNews) {

		Session session = null;
		Transaction transaction = null;

		IRoleDAO rolesDao = DAOFactory.getRoleDAO();
		try {
			session = getSession();
			transaction = session.beginTransaction();

			SbiNews hibNews = new SbiNews();
			hibNews.setName(aNews.getTitle());
			hibNews.setDescription(aNews.getDescription());
			hibNews.setNews(aNews.getHtml());
			hibNews.setPriority(null);
			hibNews.setExpirationDate(aNews.getExpirationDate());
			hibNews.setCategoryId(aNews.getType());
			Set roles = aNews.getRoles();
			Set extRoles = new HashSet<>();
			Iterator<Role> iterator = roles.iterator();

			while (iterator.hasNext()) {
				Role businessRole = iterator.next();
				SbiExtRoles sbiExtRole;

				try {
					sbiExtRole = rolesDao.loadSbiExtRoleById(businessRole.getId());

				} catch (Exception e) {

					throw new SpagoBIRuntimeException(e.getMessage());
				}
				extRoles.add(sbiExtRole);
			}
			hibNews.setSbiNewsRoles(extRoles);

			if (aNews.getId() != null) {
				hibNews.setId(aNews.getId());
			}

			updateSbiCommonInfo4Insert(hibNews);
			session.saveOrUpdate(hibNews);
			aNews.setId(hibNews.getId());
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);

			if (transaction != null)
				transaction.rollback();
			throw new SpagoBIRuntimeException(e.getMessage());

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		return aNews;

	}

}
