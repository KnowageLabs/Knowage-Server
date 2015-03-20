/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.kpi.alarm.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.alarm.bo.Alarm;
import it.eng.spagobi.kpi.alarm.bo.AlarmContact;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent;
import it.eng.spagobi.kpi.config.bo.KpiInstance;
import it.eng.spagobi.kpi.config.bo.KpiValue;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 
 * @see it.eng.spagobi.kpi.alarm.metadata.SbiAlarm
 * @author Enrico Cesaretti
 */
public class SbiAlarmDAOHibImpl extends AbstractHibernateDAO implements ISbiAlarmDAO{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SbiAlarmDAOHibImpl.class);


	public void insert(SbiAlarm item) {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(item);
			session.save(item);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}

	
	public void isAlarmingValue(KpiValue value) throws EMFUserError {
		logger.debug("IN");

		Integer kpiInstID = value.getKpiInstanceId();
		String val = value.getValue();
		Double kpiVal = null;
		if (val != null) {
			kpiVal = new Double(val);
		}
		if (kpiVal != null) {
			KpiInstance kInst = null;
			Session aSession = null;
			Transaction tx = null;

			try {
				aSession = getSession();
				tx = aSession.beginTransaction();
				SbiKpiInstance hibSbiKpiInstance = (SbiKpiInstance) aSession
				.load(SbiKpiInstance.class, kpiInstID);
				Set alarms = hibSbiKpiInstance.getSbiAlarms();
				if (!alarms.isEmpty()) {

					Iterator itAl = alarms.iterator();
					while (itAl.hasNext()) {
						boolean isAlarming = false;
						SbiAlarm alarm = (SbiAlarm) itAl.next();
						SbiThresholdValue threshold = alarm
						.getSbiThresholdValue();
						String type = threshold.getSbiThreshold()
						.getThresholdType().getValueCd();
						double min;
						double max;
						String thresholdValue = "";
						logger.debug("Threshold Type: " + type);

						if (type.equals("RANGE")) {

							min = threshold.getMinValue();
							max = threshold.getMaxValue();
							logger.debug("Threshold Min: " + min);
							logger.debug("Threshold Max: " + max);

							// if the value is in the interval, then there
							// should be
							// an alarm
							if (kpiVal.doubleValue() >= min
									&& kpiVal.doubleValue() <= max) {
								isAlarming = true;
								thresholdValue = "Min:" + min + "-Max:" + max;
								logger.debug("The value "
										+ kpiVal.doubleValue()
										+ " is in the RANGE " + thresholdValue
										+ " and so an Alarm will be scheduled");
							}

						} else if (type.equals("MINIMUM")) {

							min = threshold.getMinValue();
							logger.debug("Threshold Min: " + min);
							// if the value is smaller than the min value
							if (kpiVal.doubleValue() <= min) {
								isAlarming = true;
								thresholdValue = "Min:" + min;
								logger.debug("The value "
										+ kpiVal.doubleValue()
										+ " is lower than " + thresholdValue
										+ " and so an Alarm will be scheduled");
							}

						} else if (type.equals("MAXIMUM")) {

							max = threshold.getMaxValue();
							logger.debug("Threshold Max: " + max);
							// if the value is higher than the max value
							if (kpiVal.doubleValue() >= max) {
								isAlarming = true;
								thresholdValue = "Max:" + max;
								logger.debug("The value "
										+ kpiVal.doubleValue()
										+ " is higher than " + thresholdValue
										+ " and so an Alarm will be scheduled");
							}
						}

						if (isAlarming) {
							SbiAlarmEvent alarmEv = new SbiAlarmEvent();
							String kpiName = hibSbiKpiInstance.getSbiKpi()
							.getName();
							logger.debug("Kpi Name: " + kpiName);
							String resources = null;
							if (value.getR() != null) {
								resources = value.getR().getName();
								logger.debug("Resources: " + resources);
							}

							alarmEv.setKpiName(kpiName);
							alarmEv.setKpiValue(val);
							alarmEv.setActive(true);
							alarmEv.setEventTs(value.getBeginDate());
							alarmEv.setResources(resources);
							alarmEv.setSbiAlarms(alarm);
							alarmEv.setThresholdValue(thresholdValue);
							alarmEv.setKpiDescription(value.getValueDescr());
							if (value.getR() != null) alarmEv.setResourcesId(value.getR().getId());
							alarmEv.setKpiInstanceId(value.getKpiInstanceId());
							ISbiAlarmEventDAO dao=DAOFactory.getAlarmEventDAO();
							dao.setUserProfile(getUserProfile());
							dao.insert(alarmEv);
							logger
							.debug("A new alarm has been inserted in the Alarm Event Table");
						}

					}

				}

			} catch (HibernateException he) {
				logger
				.error(
						"Error while verifying if the KpiValue is alarming for its thresholds ",
						he);

				if (tx != null)
					tx.rollback();

				throw new EMFUserError(EMFErrorSeverity.ERROR, 10105);

			} finally {
				if (aSession != null) {
					if (aSession.isOpen())
						aSession.close();
					logger.debug("OUT");
				}
			}
		}
		logger.debug("OUT");
	}
	
/*
	public void insert(Session session, SbiAlarm item) {
		session.save(item);
	}
	*/
	public Integer update(SbiAlarm item) {
		Session session = getSession();
		Transaction tx = null;
		Integer id = null;
		try {
			boolean save = true;
			id = item.getId();
			if(id!=null && id!=0){
				save = false;
			}
			tx = session.beginTransaction();
			updateSbiCommonInfo4Update(item);
			if(save){
				//save
				id = (Integer)session.save(item);
			}else{
				session.saveOrUpdate(item);
			}
			
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();			
		}
		return id;
	}	
/*
	public void update(Session session, SbiAlarm item) {
		session.update(item);
	}	
*/
	public void delete(SbiAlarm item) {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(item);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}

	public void delete(Session session, SbiAlarm item) {
		session.delete(item);
	}

	public void delete(Integer id) {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(session.load(SbiAlarm.class, id));
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}


	public void delete(Session session, Integer id) {
		session.delete(session.load(SbiAlarm.class, id));
	}

	@SuppressWarnings("unchecked")
	public SbiAlarm findById(Integer id) {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SbiAlarm item = (SbiAlarm)session.get(SbiAlarm.class, id);
			tx.commit();
			return item;

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<SbiAlarm> findAll() {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			List<SbiAlarm> list = (List<SbiAlarm>)session.createQuery("from SbiAlarm").list();
			Iterator it = list.iterator();
			while(it.hasNext()){
				SbiAlarm alarm = (SbiAlarm)it.next();
				Hibernate.initialize(alarm);
				Hibernate.initialize(alarm.getModality());
				Hibernate.initialize(alarm.getSbiAlarmContacts());
				Iterator it2 = alarm.getSbiAlarmContacts().iterator();
				while(it2.hasNext()){
					Hibernate.initialize(it2.next());
				}
			}

			tx.commit();
			return list;

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}	

	/** Load all the alarms referencing a KpiInstance
	 * 
	 * @param Integer kpiInstanceId
	 * @return List of Sbi Alarms
	 * @throws EMFUserError 
	 */

	public List<Alarm> loadAllByKpiInstId(Integer kpiInstanceId) throws EMFUserError {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "from SbiAlarm where sbiKpiInstance="+kpiInstanceId;
			Query hqlQuery = session.createQuery(hql);

			List<SbiAlarm> list = (List<SbiAlarm>)hqlQuery.list();

			List<Alarm> toReturn=new ArrayList<Alarm>();

			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				SbiAlarm sbiAlarm = (SbiAlarm) iterator.next();
				Alarm alarm=toAlarm(sbiAlarm);
				toReturn.add(alarm);
			}

			tx.commit();
			return toReturn;

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}	




	public Alarm toAlarm(SbiAlarm sbiAlarm) throws EMFUserError{
		Alarm toReturn=new Alarm();
		toReturn.setDescr(sbiAlarm.getDescr());
		toReturn.setId(sbiAlarm.getId());
		toReturn.setLabel(sbiAlarm.getLabel());
		toReturn.setName(sbiAlarm.getName());
		toReturn.setText(sbiAlarm.getText());		
		toReturn.setUrl(sbiAlarm.getUrl());		

		Boolean autoDis=(sbiAlarm.isAutoDisabled()!=null) ? sbiAlarm.isAutoDisabled(): null;
		if(autoDis!=null){
			toReturn.setAutoDisabled(autoDis.booleanValue());		
		}	

		Boolean single=(sbiAlarm.isSingleEvent()!=null) ? sbiAlarm.isSingleEvent(): null;
		if(single!=null){
			toReturn.setSingleEvent(single.booleanValue());		
		}	

		ISbiAlarmContactDAO alarmContactDAO=DAOFactory.getAlarmContactDAO();
		Set<AlarmContact> contactsToInsert = new HashSet<AlarmContact>(0); 
		Set<SbiAlarmContact> contacts=sbiAlarm.getSbiAlarmContacts();
		if(contacts!=null){
			for (Iterator iterator = contacts.iterator(); iterator.hasNext();) {
				SbiAlarmContact sbiAlarmContact = (SbiAlarmContact) iterator.next();
				Integer idAlarmContact=sbiAlarmContact.getId();
				AlarmContact alarmContact=alarmContactDAO.loadById(idAlarmContact);
				contactsToInsert.add(alarmContact);
			}			
		}
		toReturn.setSbiAlarmContacts(contactsToInsert);	


		if(sbiAlarm.getSbiKpiInstance()!=null){
			toReturn.setIdKpiInstance(sbiAlarm.getSbiKpiInstance().getIdKpiInstance());
		}
		if(sbiAlarm.getSbiThresholdValue()!=null){
			toReturn.setIdThresholdValue(sbiAlarm.getSbiThresholdValue().getIdThresholdValue());
		}
		if(sbiAlarm.getModality()!=null){
			toReturn.setModalityId(sbiAlarm.getModality().getValueId());
		}


		return toReturn;
	}


	public Integer countAlarms() throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiAlarm ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());

		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiAlarm", he);	
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


	public List<SbiAlarm> loadPagedAlarmsList(Integer offset, Integer fetchSize)throws EMFUserError {
		logger.debug("IN");
		List<SbiAlarm> toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		Integer resultNumber;
		Query hibernateQuery;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "select count(*) from SbiAlarm ";
			Query hqlQuery = aSession.createQuery(hql);
			Long temp = (Long)hqlQuery.uniqueResult();
			resultNumber = new Integer(temp.intValue());
			
			offset = offset < 0 ? 0 : offset;
			if(resultNumber > 0) {
				fetchSize = (fetchSize > 0)? Math.min(fetchSize, resultNumber): resultNumber;
			}
			
			hibernateQuery = aSession.createQuery("from SbiAlarm order by label");
			hibernateQuery.setFirstResult(offset);
			if(fetchSize > 0) hibernateQuery.setMaxResults(fetchSize);			
	
			toReturn = (List<SbiAlarm>)hibernateQuery.list();	
			if(toReturn!=null && !toReturn.isEmpty()){
				Iterator it = toReturn.iterator();
				while(it.hasNext()){
					SbiAlarm alarm = (SbiAlarm)it.next();
					Hibernate.initialize(alarm);
					Hibernate.initialize(alarm.getModality());
					Hibernate.initialize(alarm.getSbiAlarmContacts());
					Iterator it2 = alarm.getSbiAlarmContacts().iterator();
					while(it2.hasNext()){
						Hibernate.initialize(it2.next());
					}
				}
			}
			
		} catch (HibernateException he) {
			logger.error("Error while loading the list of SbiAlarm", he);	
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

