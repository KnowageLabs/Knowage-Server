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

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.dao.RoleDAOHibImpl;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.news.bo.AdvancedNews;
import it.eng.spagobi.tools.news.bo.BasicNews;
import it.eng.spagobi.tools.news.metadata.SbiNews;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SbiNewsDAOImpl extends AbstractHibernateDAO implements ISbiNewsDAO {

	private static Logger logger = Logger.getLogger(SbiNewsDAOImpl.class);

	@Override
	public List<SbiNews> getAllSbiNews() {

		logger.debug("IN");
		Session session = null;
		List<SbiNews> hibList = null;

		try {
			session = getSession();
			String hql = " from SbiNews s";
			Query query = session.createQuery(hql);

			hibList = query.list();

		} catch (HibernateException e) {
			logException(e);

			throw new SpagoBIRuntimeException("Cannot load all news", e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return hibList;
	}

	@Override
	public List<BasicNews> getAllNews() {

		logger.debug("IN");
		List<BasicNews> listOfNews = new ArrayList<>();

		List<SbiNews> hibList = getAllSbiNews();
		Iterator<SbiNews> iterator = hibList.iterator();
		while (iterator.hasNext()) {
			SbiNews hibNews = iterator.next();
			if (hibNews != null) {
				BasicNews news = new BasicNews(hibNews.getId(), hibNews.getName(), hibNews.getDescription(), hibNews.getCategoryId());
				listOfNews.add(news);
			}
		}

		logger.debug("OUT");
		return listOfNews;
	}

	@Override
	public AdvancedNews getNewsById(Integer id, UserProfile profile) {

		logger.debug("IN");

		AdvancedNews newsToReturn = null;

		try {
			newsToReturn = toAdvancedNews(getSbiNewsById(id, profile));

		} catch (HibernateException e) {
			logException(e);
			throw new SpagoBIRuntimeException("Cannot get news with id " + id, e);

		}

		logger.debug("OUT");
		return newsToReturn;
	}

	public SbiNews getSbiNewsById(Integer id, UserProfile profile) {

		logger.debug("IN");

		SbiNews sbiNews = null;

		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();

			String hql = "from SbiNews s WHERE s.id = :id";
			Query query = session.createQuery(hql);
			query.setInteger("id", id);
			sbiNews = (SbiNews) query.uniqueResult();
			transaction.commit();

		} catch (HibernateException e) {
			logException(e);

			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException("Cannot get news with id " + id, e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		if (UserUtilities.isTechnicalUser(profile) || getAvailableNews(sbiNews, profile) != null) {
			logger.debug("OUT");
			return sbiNews;

		} else {
			throw new SpagoBIRuntimeException("You are not allowed to get this news");
		}
	}

	private AdvancedNews toAdvancedNews(SbiNews hibNews) {

		logger.debug("IN");

		AdvancedNews news = new AdvancedNews();
		news.setId(hibNews.getId());
		news.setTitle(hibNews.getName());
		news.setDescription(hibNews.getDescription());

		news.setType(hibNews.getCategoryId());
		news.setHtml(hibNews.getNews());
		news.setActive(hibNews.isActive());
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
			throw new SpagoBIRuntimeException("Cannot get roles", e);
		}

		logger.debug("OUT");
		return news;
	}

	@Override
	public void deleteNews(Integer newsId, UserProfile profile) {

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

			if (UserUtilities.isTechnicalUser(profile) || getAvailableNews(newsForDelete, profile) != null) {

				session.delete(newsForDelete);

			} else {
				throw new SpagoBIRuntimeException("You are not allowed to get this news");
			}

			transaction.commit();

		} catch (HibernateException e) {
			logException(e);
			logger.error("Error in deleting news", e);
			if (transaction != null)
				transaction.rollback();

			throw new SpagoBIRuntimeException("Error occured while deleting news", e);

		} finally {

			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");

	}

	@Override
	public AdvancedNews saveNews(AdvancedNews aNews) {

		Session session = null;
		Transaction transaction = null;

		IRoleDAO rolesDao = DAOFactory.getRoleDAO();
		try {

			logger.debug("IN");

			session = getSession();
			transaction = session.beginTransaction();

			SbiNews hibNews = new SbiNews();
			hibNews.setName(aNews.getTitle());
			hibNews.setDescription(aNews.getDescription());
			hibNews.setNews(aNews.getHtml());
			hibNews.setExpirationDate(aNews.getExpirationDate());
			hibNews.setActive(aNews.getActive());
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

					throw new SpagoBIRuntimeException("Error occured while saving news", e);
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
			throw new SpagoBIRuntimeException("Cannot save news", e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return aNews;

	}

	@Override
	public List<BasicNews> getAllNews(UserProfile profile) {

		logger.debug("IN");
		Set<BasicNews> setOfNews = new HashSet<>();
		Session session = null;

		try {
			List listOfRoles = (List) profile.getRoles();
			session = getSession();
			String hql = " from SbiNews s where s.active=true and s.expirationDate >= current_date";
			Query query = session.createQuery(hql);

			List hibList = query.list();
			Iterator iterator = hibList.iterator();
			while (iterator.hasNext()) {
				SbiNews hibNews = (SbiNews) iterator.next();
				hibNews = getAvailableNews(hibNews, profile);
				if (hibNews != null) {
					BasicNews basicNews = new BasicNews(hibNews.getId(), hibNews.getName(), hibNews.getDescription(), hibNews.getCategoryId());
					setOfNews.add(basicNews);
				}
			}

		} catch (HibernateException e) {
			logException(e);
			throw new SpagoBIRuntimeException("Cannot return all news", e);

		} catch (Exception e) {
			logException(e);
			throw new SpagoBIRuntimeException("Error occured while getting news", e);

		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

		logger.debug("OUT");
		return new ArrayList<BasicNews>(setOfNews);
	}

	public SbiNews getAvailableNews(SbiNews hibNews, IEngUserProfile profile) {

		List listOfRoles;
		SbiNews sbiNews = null;
		try {
			listOfRoles = (List) profile.getRoles();
			if (hibNews != null) {
				Set<SbiExtRoles> setOfExtRoles = hibNews.getSbiNewsRoles();
				Iterator<SbiExtRoles> roleIterator = setOfExtRoles.iterator();
				while (roleIterator.hasNext()) {
					if (listOfRoles.contains(roleIterator.next().getName())) {
						sbiNews = hibNews;
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot get available news", e);
		}

		return sbiNews;

	}

}
