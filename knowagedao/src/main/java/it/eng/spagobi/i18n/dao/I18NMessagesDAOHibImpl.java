/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.i18n.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.i18n.metadata.SbiI18NMessageBody;
import it.eng.spagobi.i18n.metadata.SbiI18NMessages;

public class I18NMessagesDAOHibImpl extends AbstractHibernateDAO implements I18NMessagesDAO {

	static private Logger logger = Logger.getLogger(I18NMessagesDAOHibImpl.class);

	@Override
	public String getI18NMessages(Locale locale, String code) throws EMFUserError {
		logger.debug("IN. code=" + code);
		String toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		if (locale == null) {
			logger.warn("No I18n conversion because locale passed as parameter is null");
			return code;
		}

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String qDom = "from SbiDomains dom where dom.valueCd = :valueCd AND dom.domainCd = 'LANG'";
			Query queryDom = aSession.createQuery(qDom);
			String localeId = locale.getISO3Language().toUpperCase();
			logger.debug("localeId=" + localeId);
			queryDom.setString("valueCd", localeId);
			Object objDom = queryDom.uniqueResult();
			if (objDom == null) {
				logger.error("Could not find domain for locale " + locale.getISO3Language());
				return code;
			}
			Integer domId = ((SbiDomains) objDom).getValueId();

			String q = "from SbiI18NMessages att where att.id.languageCd = :languageCd AND att.id.label = :label";
			Query query = aSession.createQuery(q);

			query.setInteger("languageCd", domId);
			query.setString("label", code);

			Object obj = query.uniqueResult();
			if (obj != null) {
				SbiI18NMessages SbiI18NMessages = (SbiI18NMessages) obj;
				toReturn = SbiI18NMessages.getMessage();
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
		logger.debug("OUT.toReturn=" + toReturn);
		return toReturn;
	}

	@Override
	public Map<String, String> getAllI18NMessages(Locale locale) throws EMFUserError {
		logger.debug("IN");

		Map<String, String> toReturn = new HashMap<String, String>();

		Session aSession = null;
		Transaction tx = null;

		if (locale == null) {
			logger.error("No I18n conversion because locale passed as parameter is null");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String qDom = "from SbiDomains dom where dom.valueCd = :valueCd AND dom.domainCd = 'LANG'";
			Query queryDom = aSession.createQuery(qDom);

			String localeId = null;

			try {
				localeId = locale.toLanguageTag();
			} catch (Exception e) {
				logger.warn("No iso code found for locale, set manually");
			}

			logger.debug("localeId=" + localeId);
			queryDom.setString("valueCd", localeId);
			Object objDom = queryDom.uniqueResult();
			if (objDom == null) {
				logger.error("Could not find domain for locale " + locale.getISO3Language());
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}

			Integer domId = ((SbiDomains) objDom).getValueId();

			String q = "from SbiI18NMessages att where att.id.languageCd = :languageCd";
			Query query = aSession.createQuery(q);

			query.setInteger("languageCd", domId);

			List objList = query.list();
			if (objList != null && objList.size() > 0) {
				for (Iterator iterator = objList.iterator(); iterator.hasNext();) {
					SbiI18NMessages i18NMess = (SbiI18NMessages) iterator.next();
					toReturn.put(i18NMess.getLabel(), i18NMess.getMessage());
				}
			}

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
		logger.debug("OUT.toReturn=" + toReturn);
		return toReturn;

	}

	@Override
	public List<SbiI18NMessages> getI18NMessages(String languageName) {
		logger.debug("IN");

		List<SbiI18NMessages> toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Integer domainId = getSbiDomainId(languageName, aSession);
			String tenant = getTenant();
			String hql = "from SbiI18NMessages m where m.languageCd = :languageCd and m.commonInfo.organization = :organization";
			Query query = aSession.createQuery(hql);
			query.setInteger("languageCd", domainId);
			query.setString("organization", tenant);

			toReturn = query.list();

		} catch (HibernateException he) {
			logger.error(he.getMessage(), he);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException();
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT.toReturn=" + toReturn);
		return toReturn;

	}

	@Override
	public SbiI18NMessages getSbiI18NMessageById(Integer id) {
		logger.debug("IN");
		Session session = null;
		SbiI18NMessages toReturn = null;
		try {
			session = getSession();
			String hql = "from SbiI18NMessages m where m.id = :id";
			Query query = session.createQuery(hql);
			query.setInteger("id", id);
			toReturn = (SbiI18NMessages) query.uniqueResult();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException();
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	public void insertI18NMessage(SbiI18NMessageBody message) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		SbiI18NMessages toInsert = new SbiI18NMessages();
		try {
			session = getSession();
			tx = session.beginTransaction();

			Integer domainId = getSbiDomainId(message.getLanguage(), session);

			toInsert.setLanguageCd(domainId);
			toInsert.setLabel(message.getLabel());
			toInsert.setMessage(message.getMessage());

			updateSbiCommonInfo4Insert(toInsert);
			session.save(toInsert);
			tx.commit();
			session.flush();
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException();
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public void updateI18NMessage(SbiI18NMessages message) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		SbiI18NMessages toModify = new SbiI18NMessages();
		try {
			session = getSession();
			tx = session.beginTransaction();

			toModify.setId(message.getId());
			toModify.setLanguageCd(message.getLanguageCd());
			toModify.setLabel(message.getLabel());
			toModify.setMessage(message.getMessage());

			updateSbiCommonInfo4Update(toModify);
			session.update(toModify);
			tx.commit();
			session.flush();
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException();
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public void updateNonDefaultI18NMessagesLabel(SbiI18NMessages oldMessage, SbiI18NMessages newMessage) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			String tenant = getTenant();
			List<SbiI18NMessages> messages = getSbiI18NMessagesByLabel(oldMessage, tenant, session);
			Iterator<SbiI18NMessages> it = messages.iterator();
			while (it.hasNext()) {
				SbiI18NMessages toModify = it.next();
				toModify.setLabel(newMessage.getLabel());
				updateSbiCommonInfo4Update(toModify);
				session.update(toModify);
				session.flush();
			}
			tx.commit();
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException();
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public void deleteI18NMessage(Integer id) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		SbiI18NMessages toDelete = new SbiI18NMessages();
		try {
			session = getSession();
			tx = session.beginTransaction();

			String hql = "from SbiI18NMessages mess where mess.id = :id";
			Query query = session.createQuery(hql);
			query.setInteger("id", id);
			toDelete = (SbiI18NMessages) query.uniqueResult();

			session.delete(toDelete);
			tx.commit();
			session.flush();
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException();
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		logger.debug("OUT");
	}

	@Override
	public void deleteNonDefaultI18NMessages(SbiI18NMessages message) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			String tenant = getTenant();
			List<SbiI18NMessages> nonDefaultMessages = getSbiI18NMessagesByLabel(message, tenant, session);
			Iterator<SbiI18NMessages> it = nonDefaultMessages.iterator();
			while (it.hasNext()) {
				SbiI18NMessages toDelete = it.next();
				session.delete(toDelete);
				session.flush();
			}
			tx.commit();
		} catch (HibernateException e) {
			logException(e);
			if (tx != null)
				tx.rollback();
			throw new RuntimeException();
		} finally {
			if (session != null) {
				if (session.isOpen())
					session.close();
			}
		}
		logger.debug("OUT");
	}

	private Integer getSbiDomainId(String langName, Session curSession) {
		logger.debug("IN");
		Integer domainId = null;
		SbiDomains domain = null;
		String DOMAIN_CD = "LANG";
		try {
			String hql = "from SbiDomains d where d.domainCd = :domainCd and d.valueCd = :valueCd";
			Query query = curSession.createQuery(hql);
			query.setString("domainCd", DOMAIN_CD);
			query.setString("valueCd", langName);
			domain = (SbiDomains) query.uniqueResult();
			domainId = domain.getValueId();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException();
		}
		logger.debug("OUT");
		return domainId;
	}

	private List<SbiI18NMessages> getSbiI18NMessagesByLabel(SbiI18NMessages message, String tenant, Session curSession) {
		logger.debug("IN");
		List<SbiI18NMessages> toReturn = new ArrayList<SbiI18NMessages>();
		try {
			String hql = "from SbiI18NMessages m where m.label = :label and m.commonInfo.organization = :organization and m.languageCd != :languageCd";
			Query query = curSession.createQuery(hql);
			query.setString("label", message.getLabel());
			query.setString("organization", tenant);
			query.setInteger("languageCd", message.getLanguageCd());
			toReturn = query.list();
		} catch (HibernateException e) {
			logException(e);
			throw new RuntimeException();
		}
		logger.debug("OUT");
		return toReturn;
	}

}
