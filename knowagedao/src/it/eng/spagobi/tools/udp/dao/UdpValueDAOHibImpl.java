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
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.tools.udp.bo.UdpValue;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

/**
 *
 * @see it.eng.spagobi.tools.udp.bo.SbiUdp
 * @author Antonella Giachino
 */
public class UdpValueDAOHibImpl extends AbstractHibernateDAO implements IUdpValueDAO {

	private static final Logger logger = Logger.getLogger(UdpValueDAOHibImpl.class);

	@Override
	public Integer insert(SbiUdpValue propValue) {
		logger.debug("IN");
		Session session = null;
		Transaction tx = null;
		Integer id = null;
		try {
			session = getSession();
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(propValue);
			id = (Integer) session.save(propValue);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
			logger.debug("OUT");
		}
		return id;
	}

	@Override
	public void insert(Session session, SbiUdpValue propValue) {
		updateSbiCommonInfo4Insert(propValue);
		session.save(propValue);
	}

	@Override
	public void update(SbiUdpValue propValue) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Update(propValue);
			session.update(propValue);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		logger.debug("OUT");

	}

	@Override
	public void update(Session session, SbiUdpValue propValue) {
		logger.debug("IN");
		updateSbiCommonInfo4Update(propValue);
		session.update(propValue);
		logger.debug("OUT");
	}

	@Override
	public void delete(SbiUdpValue propValue) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(propValue);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		logger.debug("OUT");
	}

	@Override
	public void delete(Session session, SbiUdpValue item) {
		logger.debug("IN");
		session.delete(item);
		logger.debug("OUT");
	}

	@Override
	public void delete(Integer id) {
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(session.load(SbiUdpValue.class, id));
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		logger.debug("OUT");
	}

	@Override
	public void delete(Session session, Integer id) {
		session.delete(session.load(SbiUdpValue.class, id));
	}

	@Override
	@SuppressWarnings("unchecked")
	public SbiUdpValue findById(Integer id) {
		logger.debug("IN");
		SbiUdpValue propValue = null;
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			propValue = (SbiUdpValue) session.get(SbiUdpValue.class, id);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		logger.debug("OUT");
		return propValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List findByReferenceId(Integer kpiId, String family) {
		logger.debug("IN");
		Session aSession = getSession();
		Transaction tx = null;
		List<UdpValue> toReturn = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			String hql = "from SbiUdpValue s " + "	where s.referenceId = ? AND " + "         lower(s.family) = lower('" + family + "') AND "
					+ "         s.endTs is NULL " + " order by s.label asc";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, kpiId);
			List toConvert = hqlQuery.list();
			for (Iterator iterator = toConvert.iterator(); iterator.hasNext();) {
				SbiUdpValue sbiUdpValue = (SbiUdpValue) iterator.next();
				UdpValue udpValue = toUdpValue(sbiUdpValue);
				if (toReturn == null)
					toReturn = new ArrayList<UdpValue>();
				toReturn.add(udpValue);
			}

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			aSession.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load a UdpValue by Id
	 */

	@Override
	public UdpValue loadById(Integer id) {
		logger.debug("IN");
		Session session = getSession();
		UdpValue udpValue = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SbiUdpValue prop = (SbiUdpValue) session.get(SbiUdpValue.class, id);
			tx.commit();
			udpValue = toUdpValue(prop);

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		logger.debug("OUT");
		return udpValue;
	}

	/**
	 * Load a UdpValue by refrence Id, udpId, family
	 */

	@Override
	public UdpValue loadByReferenceIdAndUdpId(Integer referenceId, Integer udpId, String family) {
		logger.debug("IN");
		UdpValue toReturn = null;
		Session tmpSession = getSession();
		Transaction tx = null;
		try {
			tx = tmpSession.beginTransaction();
			String hql = "from SbiUdpValue s " + "	where s.referenceId = ? AND " + "         s.sbiUdp.udpId = ? AND " + "         lower(s.family) = lower('"
					+ family + "') AND " + "         s.endTs is NULL " + " order by s.label asc";
			Query hqlQuery = tmpSession.createQuery(hql);
			hqlQuery.setInteger(0, referenceId);
			hqlQuery.setInteger(1, udpId);

			SbiUdpValue hibValueUDP = (SbiUdpValue) hqlQuery.uniqueResult();
			if (hibValueUDP == null)
				return null;
			toReturn = toUdpValue(hibValueUDP);

			// tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			tmpSession.close();
		}
		logger.debug("OUT");
		return toReturn;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SbiUdpValue> findAll() {
		logger.debug("IN");
		Session session = getSession();
		List<SbiUdpValue> list = null;
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			list = session.createQuery("from SbiUdpValue").list();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;

		} finally {
			session.close();
		}
		logger.debug("OUT");
		return list;

	}

	public UdpValue toUdpValue(SbiUdpValue sbiUdpValue) {
		logger.debug("IN");
		UdpValue toReturn = new UdpValue();

		toReturn.setUdpValueId(sbiUdpValue.getUdpValueId());
		toReturn.setUdpId(sbiUdpValue.getSbiUdp().getUdpId());
		toReturn.setReferenceId(sbiUdpValue.getReferenceId());
		toReturn.setLabel(sbiUdpValue.getSbiUdp().getLabel()); // denormilized
		toReturn.setName(sbiUdpValue.getSbiUdp().getName()); // denormilized

		try {
			IDomainDAO aDomainDAO = DAOFactory.getDomainDAO();
			Domain familyDomain = aDomainDAO.loadDomainById(sbiUdpValue.getSbiUdp().getFamilyId());
			toReturn.setFamily(familyDomain.getValueCd()); // denormilized
		} catch (Exception he) {
			logger.error(he);
		}

		Integer typeId = sbiUdpValue.getSbiUdp().getTypeId();
		if (typeId != null) {
			try {
				IDomainDAO aDomainDAO = DAOFactory.getDomainDAO();
				Domain typeDomain = aDomainDAO.loadDomainById(typeId);
				toReturn.setTypeLabel(typeDomain.getValueCd()); // denormilized
			} catch (Exception he) {
				logger.error(he);
			}
		}

		toReturn.setValue(sbiUdpValue.getValue());
		toReturn.setProg(sbiUdpValue.getProg());
		toReturn.setBeginTs(sbiUdpValue.getBeginTs());
		toReturn.setEndTs(sbiUdpValue.getEndTs());

		logger.debug("OUT");
		return toReturn;
	}

}
