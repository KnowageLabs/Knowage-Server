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

package it.eng.spagobi.tools.udp.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;

/**
 *
 * @see it.eng.spagobi.tools.udp.bo.SbiUdp
 * @author Antonella Giachino
 */
public class UdpDAOHibImpl extends AbstractHibernateDAO implements IUdpDAO {

	private static final Logger LOGGER = Logger.getLogger(UdpDAOHibImpl.class);
	private static final String DOMAIN_CD_UDP_FAMILY = "UDP_FAMILY";

	@Override
	public Integer insert(SbiUdp prop) {
		LOGGER.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		Integer id = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(prop);
			id = (Integer) session.save(prop);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			closeSession(session);
			LOGGER.debug("OUT");
		}
		return id;
	}

	@Override
	public void update(SbiUdp prop) {
		LOGGER.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Update(prop);
			session.update(prop);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		LOGGER.debug("OUT");
	}

	@Override
	public void delete(SbiUdp prop) {
		LOGGER.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(prop);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		LOGGER.debug("OUT");
	}

	@Override
	public void delete(Integer id) {
		LOGGER.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(session.load(SbiUdp.class, id));
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		LOGGER.debug("OUT");
	}

	@Override
	@SuppressWarnings("unchecked")
	public SbiUdp findById(Integer id) {
		LOGGER.debug("IN");
		SbiUdp prop = null;
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			prop = (SbiUdp) session.get(SbiUdp.class, id);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		LOGGER.debug("OUT");
		return prop;
	}

	@Override
	public Udp loadById(Integer id) {
		LOGGER.debug("IN");
		Session session = getSession();
		Udp udp = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SbiUdp prop = (SbiUdp) session.get(SbiUdp.class, id);
			tx.commit();
			udp = toUdp(prop);
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		LOGGER.debug("OUT");
		return udp;
	}

	/**
	 * Load a Udp by Label
	 *
	 * @throws EMFUserError
	 */

	@Override
	public Udp loadByLabel(String label) throws EMFUserError {
		LOGGER.debug("IN");
		Udp udp = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			Criterion labelCriterrion = Restrictions.eq("label", label);
			Criteria criteria = tmpSession.createCriteria(SbiUdp.class);
			criteria.add(labelCriterrion);
			SbiUdp hibUDP = (SbiUdp) criteria.uniqueResult();
			if (hibUDP == null)
				return null;
			udp = toUdp(hibUDP);

			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("Error while loading the udp with label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		LOGGER.debug("OUT");
		return udp;

	}

	/**
	 * Load a Udp by Label and Family code
	 *
	 * @throws EMFUserError
	 */

	@Override
	public Udp loadByLabelAndFamily(String label, String family) throws EMFUserError {
		LOGGER.debug("IN");
		Udp udp = null;
		Session tmpSession = null;
		Transaction tx = null;
		try {
			tmpSession = getSession();
			tx = tmpSession.beginTransaction();
			// get familyId
			String hql = "from SbiDomains s where lower(s.valueCd) = lower(?) AND s.domainCd = ?";
			Query hqlQuery = tmpSession.createQuery(hql);
			hqlQuery.setString(0, family);
			hqlQuery.setString(1, DOMAIN_CD_UDP_FAMILY);

			SbiDomains famiDom = (SbiDomains) hqlQuery.uniqueResult();
			if (famiDom == null)
				return null;

			Criterion labelCriterrion = Restrictions.eq("label", label);
			Criteria criteria2 = tmpSession.createCriteria(SbiUdp.class);
			criteria2.add(labelCriterrion);
			Criterion famCriterrion = Restrictions.eq("familyId", famiDom.getValueId());
			criteria2.add(famCriterrion);

			SbiUdp hibUDP = (SbiUdp) criteria2.uniqueResult();
			if (hibUDP == null)
				return null;
			udp = toUdp(hibUDP);

			tx.commit();
		} catch (HibernateException he) {
			LOGGER.error("Error while loading the udp with label " + label, he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (tmpSession != null) {
				if (tmpSession.isOpen())
					tmpSession.close();
			}
		}
		LOGGER.debug("OUT");
		return udp;

	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SbiUdp> findAll() {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			List<SbiUdp> list = session.createQuery("from SbiUdp").list();
			tx.commit();
			return list;

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SbiUdp> listUdpFromArray(final Object[] arr) {
		return list(session -> {
			Criteria c = session.createCriteria(SbiUdp.class);
			c.add(Restrictions.in("udpId", arr));
			return c;
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Udp> loadByFamilyAndLikeLabel(String familyCd, String lab) throws EMFUserError {
		LOGGER.debug("IN");
		Session session = getSession();
		List<Udp> toReturn = null;
		// get Domain id form KPI family
		Transaction tx = null;
		try {

			Integer domainId;
			SbiDomains domain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue(DOMAIN_CD_UDP_FAMILY, familyCd);

			if (domain == null) {
				LOGGER.error("could not find domain of type UDP_FAMILY with value code " + familyCd);
				return null;
			} else {
				domainId = domain.getValueId();
			}

			tx = session.beginTransaction();
			Query query = session.createQuery("from SbiUdp s where s.familyId = :idFamily and s.label like :labelLike");
			query.setInteger("idFamily", domainId);
			query.setString("labelLike", "%" + lab + "%");

			List<SbiUdp> list = query.list();
			if (list != null) {
				toReturn = new ArrayList<>();
				for (Iterator<SbiUdp> iterator = list.iterator(); iterator.hasNext();) {
					SbiUdp sbiUdp = iterator.next();
					Udp udp = toUdp(sbiUdp);
					toReturn.add(udp);
				}
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} catch (EMFUserError e) {
			LOGGER.error("error probably in getting asked UDP_FAMILY domain", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			closeSession(session);
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Udp> loadAllByFamily(String familyCd) throws EMFUserError {
		LOGGER.debug("IN");
		Session session = getSession();
		List<Udp> toReturn = null;
		// get Domain id form KPI family
		Transaction tx = null;
		try {

			Integer domainId;
			SbiDomains domain = DAOFactory.getDomainDAO().loadSbiDomainByCodeAndValue(DOMAIN_CD_UDP_FAMILY, familyCd);

			if (domain == null) {
				LOGGER.error("could not find domain of type UDP_FAMILY with value code " + familyCd);
				return null;
			} else {
				domainId = domain.getValueId();
			}

			tx = session.beginTransaction();
			Query query = session.createQuery("from SbiUdp s where s.familyId = :idFamily");
			query.setInteger("idFamily", domainId);

			List<SbiUdp> list = query.list();
			if (list != null) {
				toReturn = new ArrayList<>();
				for (Iterator<SbiUdp> iterator = list.iterator(); iterator.hasNext();) {
					SbiUdp sbiUdp = iterator.next();
					Udp udp = toUdp(sbiUdp);
					toReturn.add(udp);
				}
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} catch (EMFUserError e) {
			LOGGER.error("error probably in getting asked UDP_FAMILY domain", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			closeSession(session);
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	public Udp toUdp(SbiUdp sbiUdp) {
		LOGGER.debug("IN");
		Udp toReturn = new Udp();

		toReturn.setUdpId(sbiUdp.getUdpId());
		toReturn.setLabel(sbiUdp.getLabel());
		toReturn.setName(sbiUdp.getName());
		toReturn.setDescription(sbiUdp.getDescription());
		toReturn.setDataTypeId(sbiUdp.getTypeId());
		toReturn.setFamilyId(sbiUdp.getFamilyId());
		toReturn.setMultivalue(sbiUdp.isIsMultivalue());

		// get the type ValueCd
		if (sbiUdp.getTypeId() != null) {
			Domain domain;
			try {
				domain = DAOFactory.getDomainDAO().loadDomainById(sbiUdp.getTypeId());
				toReturn.setDataTypeValeCd(domain.getValueCd());
			} catch (EMFUserError e) {
				LOGGER.error("error in loading domain with Id " + sbiUdp.getTypeId(), e);
			}
		}
		LOGGER.debug("OUT");
		return toReturn;
	}

	@Override
	public Integer countUdp() throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "select count(*) from SbiUdp ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = temp.intValue();

		} catch (HibernateException he) {
			LOGGER.error("Error while loading the list of SbiUdp", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			closeSession(aSession);
			LOGGER.debug("OUT");
		}
		return resultNumber;
	}

	@Override
	public List<SbiUdp> loadPagedUdpList(Integer offset, Integer fetchSize) throws EMFUserError {
		LOGGER.debug("IN");
		List<SbiUdp> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList<>();

			String hql = "select count(*) from SbiUdp ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long) hqlQuery.uniqueResult();
			resultNumber = temp.intValue();

			offset = offset < 0 ? 0 : offset;
			if (resultNumber > 0) {
				fetchSize = (fetchSize > 0) ? Math.min(fetchSize, resultNumber) : resultNumber;
			}

			hibernateQuery = aSession.createQuery("from SbiUdp order by name");
			hibernateQuery.setFirstResult(offset);
			if (fetchSize > 0)
				hibernateQuery.setMaxResults(fetchSize);

			toReturn = hibernateQuery.list();

		} catch (HibernateException he) {
			LOGGER.error("Error while loading the list of Resources", he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			closeSession(aSession);
			LOGGER.debug("OUT");
		}
		return toReturn;
	}

}
