/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.threshold.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.threshold.bo.Threshold;
import it.eng.spagobi.kpi.threshold.metadata.SbiThreshold;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;

public class ThresholdDAOImpl extends AbstractHibernateDAO implements
		IThresholdDAO {

	static private Logger logger = Logger.getLogger(ThresholdDAOImpl.class);
	static private String THRESHOLD_NAME = "name";
	static private String THRESHOLD_DESCRIPTION = "description";
	static private String THRESHOLD_CODE ="code";

	private String getThreshodProperty(String property) {
		String toReturn = null;
		if (property != null && property.equals("NAME"))
			toReturn = THRESHOLD_NAME;
		if (property != null && property.equals("DESCRIPTION"))
			toReturn = THRESHOLD_DESCRIPTION;
		if (property != null && property.equals("CODE"))
			toReturn = THRESHOLD_CODE;
		return toReturn;
	}
	
	
	public Threshold loadThresholdById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Threshold toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiThreshold hibThreshold = (SbiThreshold) aSession.load(
					SbiThreshold.class, id);
			toReturn = new Threshold();
			toReturn = toThreshold(hibThreshold);

		} catch (HibernateException he) {
			logger.error("Error while loading the Threshold with id "
					+ ((id == null) ? "" : id.toString()), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	public Threshold loadThresholdByCode(String thrCode) throws EMFUserError {
		logger.debug("IN");
		Threshold toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Criterion labelCriterrion = Expression.eq("code", thrCode);
			Criteria criteria = aSession.createCriteria(SbiThreshold.class);
			criteria.add(labelCriterrion);			
			SbiThreshold hibThreshold = (SbiThreshold)criteria.uniqueResult();
			if (hibThreshold == null) return null;
			toReturn = new Threshold();
			toReturn = toThreshold(hibThreshold);

		} catch (HibernateException he) {
			logger.error("Error while loading the Threshold with code "
					+ ((thrCode == null) ? "" : thrCode), he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	
	public List loadThresholdList(String fieldOrder, String typeOrder)
			throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;
			if (fieldOrder != null && typeOrder != null) {
				Criteria crit = aSession
				.createCriteria(SbiThreshold.class);
				if (typeOrder.toUpperCase().trim().equals("ASC"))
					crit.addOrder(Order.asc(getThreshodProperty(fieldOrder)));
				if (typeOrder.toUpperCase().trim().equals("DESC"))
					crit.addOrder(Order.desc(getThreshodProperty(fieldOrder)));
				toTransform = crit.list();
			} else {
				toTransform = aSession.createQuery("from SbiThreshold").list();
			}

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiThreshold hibThreshold = (SbiThreshold) iterator.next();
				Threshold threshold = new Threshold();
				threshold.setName(hibThreshold.getName());
				threshold.setCode(hibThreshold.getCode());
				threshold.setDescription(hibThreshold.getDescription());
				threshold.setId(hibThreshold.getThresholdId());
				threshold.setThresholdTypeId(hibThreshold.getThresholdType().getValueId());
				threshold.setThresholdTypeCode(hibThreshold.getThresholdType().getValueCd());
				toReturn.add(threshold);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Threshold", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	public void modifyThreshold(Threshold threshold) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String name = threshold.getName();
			String description = threshold.getDescription();
			String code = threshold.getCode();
			Integer thresholdTypeId = threshold.getThresholdTypeId();

			SbiThreshold sbiThreshold = (SbiThreshold) aSession.load(
					SbiThreshold.class, threshold.getId());

			SbiDomains newThresholdType = null;
			if (thresholdTypeId != null) {
				newThresholdType = (SbiDomains) aSession.load(SbiDomains.class,
						thresholdTypeId);
			}
			SbiDomains oldThrType = sbiThreshold.getThresholdType();
			if(!oldThrType.getValueCd().equalsIgnoreCase(newThresholdType.getValueCd())){
				//If the type of threshold is changed all old threshold need to be deleted
				Set set = sbiThreshold.getSbiThresholdValues();
				ArrayList thValues=new ArrayList();
				for (Iterator iterator = set.iterator(); iterator.hasNext();) {
					SbiThresholdValue sbiThValue = (SbiThresholdValue) iterator.next();
					//look up for alarms
					String hql = "from SbiAlarm a where a.sbiThresholdValue.idThresholdValue = :id";
					Query hqlQuery = aSession.createQuery(hql);
					hqlQuery.setInteger("id", sbiThValue.getIdThresholdValue());
					List <SbiAlarm> alarms = hqlQuery.list();
					if(alarms != null && !alarms.isEmpty()){
						throw new EMFUserError(EMFErrorSeverity.ERROR, 10119);
					}
					aSession.delete(sbiThValue);
					aSession.flush();
				}
			}

			sbiThreshold.setName(name);
			sbiThreshold.setDescription(description);
			sbiThreshold.setCode(code);
			sbiThreshold.setThresholdType(newThresholdType);
			updateSbiCommonInfo4Update(sbiThreshold);
			aSession.saveOrUpdate(sbiThreshold);
			
			Integer thrId = threshold.getId();

			tx.commit();

		} catch (HibernateException he) {
			logException(he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
	}

	public Integer insertThreshold(Threshold threshold) throws EMFUserError {
		logger.debug("IN");
		Integer idToReturn;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String name = threshold.getName();
			String description = threshold.getDescription();
			String code = threshold.getCode();
			Integer thresholdTypeId = threshold.getThresholdTypeId();

			SbiThreshold sbiThreshold = new SbiThreshold();

			SbiDomains thresholdType = null;
			if (thresholdTypeId != null) {
				thresholdType = (SbiDomains) aSession.load(SbiDomains.class,
						thresholdTypeId);
			}

			sbiThreshold.setName(name);
			sbiThreshold.setDescription(description);
			sbiThreshold.setCode(code);
			sbiThreshold.setThresholdType(thresholdType);
			updateSbiCommonInfo4Insert(sbiThreshold);
			idToReturn = (Integer) aSession.save(sbiThreshold);

			tx.commit();

		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");
		return idToReturn;
	}

	public boolean deleteThreshold(Integer thresholdId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			SbiThreshold aThreshold = (SbiThreshold) aSession.load(
					SbiThreshold.class, thresholdId);
			aSession.delete(aThreshold);

			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to delete a Threshold", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10016);
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Error while delete a Threshold ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);
		} finally {
			aSession.close();
		}
		return true;
	}

	public List loadThresholdList() throws EMFUserError {
		return loadThresholdList(null, null);
	}

	
	public Threshold toThreshold(SbiThreshold t) throws EMFUserError {

		logger.debug("IN");
		Threshold toReturn = new Threshold();

		Integer id=t.getThresholdId();
		String name=t.getName();
		String description=t.getDescription();
		String code=t.getCode();
		SbiDomains d=t.getThresholdType();

		toReturn.setId(id);
		toReturn.setName(name);
		toReturn.setDescription(description);
		toReturn.setCode(code);
		toReturn.setThresholdTypeId(d.getValueId());
		toReturn.setThresholdTypeCode(d.getValueCd());
		
		// get all the threshold Values
		IThresholdValueDAO thValuesDao=(IThresholdValueDAO)DAOFactory.getThresholdValueDAO();
		Set set=t.getSbiThresholdValues();
		ArrayList thValues=new ArrayList();
		for (Iterator iterator = set.iterator(); iterator.hasNext();) {
			SbiThresholdValue sbiThValue = (SbiThresholdValue) iterator.next();
			thValues.add(thValuesDao.toThresholdValue(sbiThValue));
		}
		toReturn.setThresholdValues(thValues);
		
		logger.debug("OUT");
		return toReturn;
	}


	public Integer countThresholds() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiThreshold ";
			Query hqlQuery = aSession.createQuery(hql);
			resultNumber = new Integer(((Long)hqlQuery.uniqueResult()).intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Thresholds", he);	
			if (tx != null)
				tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);
		
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return resultNumber;
	}


	public List loadPagedThresholdList(Integer offset, Integer fetchSize)
			throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;
			
			String hql = "select count(*) from SbiThreshold ";
			Query hqlQuery = aSession.createQuery(hql);
			resultNumber = new Integer(((Long)hqlQuery.uniqueResult()).intValue());
			
			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}
			
			hibernateQuery = aSession.createQuery("from SbiThreshold order by name");
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

			toTransform = hibernateQuery.list();			

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiThreshold hibThreshold = (SbiThreshold) iterator.next();
				Threshold threshold = toThreshold(hibThreshold);
				toReturn.add(threshold);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Threshold", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}


	public List loadThresholdListFiltered(String hsql, Integer offset,
			Integer fetchSize) throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;
			
			String hql = "select count(*) "+hsql;
			Query hqlQuery = aSession.createQuery(hql);
			resultNumber = new Integer(((Long)hqlQuery.uniqueResult()).intValue());
			
			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}
			
			hibernateQuery = aSession.createQuery(hsql);
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			

			toTransform = hibernateQuery.list();			

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiThreshold hibThreshold = (SbiThreshold) iterator.next();
				Threshold threshold = toThreshold(hibThreshold);
				toReturn.add(threshold);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the filtered list of Threshold", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 9104);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}
	
}
