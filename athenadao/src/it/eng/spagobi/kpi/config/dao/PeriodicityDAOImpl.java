/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.config.bo.Periodicity;
import it.eng.spagobi.kpi.config.metadata.SbiKpiPeriodicity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

public class PeriodicityDAOImpl extends AbstractHibernateDAO implements
		IPeriodicityDAO {

	static private Logger logger = Logger.getLogger(PeriodicityDAOImpl.class);
	
	public void modifyPeriodicity(Periodicity per) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Integer perId = per.getIdKpiPeriodicity();
			String name = per.getName();
			Integer months = per.getMonths();
			Integer days = per.getDays();
			Integer hours = per.getHours();
			Integer mins = per.getMinutes();

			SbiKpiPeriodicity sbiPer = (SbiKpiPeriodicity) aSession.load(
					SbiKpiPeriodicity.class, perId);

			sbiPer.setName(name);
			sbiPer.setDays(days);
			sbiPer.setHours(hours);
			sbiPer.setMinutes(mins);
			sbiPer.setMonths(months);		
			updateSbiCommonInfo4Update(sbiPer);
			aSession.update(sbiPer);
			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.info("Impossible to modify the periodicity", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10118);

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

	public Integer insertPeriodicity(Periodicity per) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer idToReturn;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			SbiKpiPeriodicity sbiPer = new SbiKpiPeriodicity();
			
			String name = per.getName();
			Integer months = per.getMonths();
			Integer days = per.getDays();
			Integer hours = per.getHours();
			Integer mins = per.getMinutes();
			
			sbiPer.setName(name);
			sbiPer.setDays(days);
			sbiPer.setHours(hours);
			sbiPer.setMinutes(mins);
			sbiPer.setMonths(months);	
			updateSbiCommonInfo4Insert(sbiPer);
			idToReturn = (Integer) aSession.save(sbiPer);
			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.info("Impossible to insert the periodicity", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10118);

		} catch (HibernateException he) {
			logger.error("Error while inserting the periodicity ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10117);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		return idToReturn;
	}

	public Periodicity loadPeriodicityById(Integer id) throws EMFUserError {
		logger.debug("IN");
		Periodicity toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiPeriodicity hibKpiPeriodicity = (SbiKpiPeriodicity)aSession.load(SbiKpiPeriodicity.class,id);
			toReturn = new Periodicity(hibKpiPeriodicity
					.getIdKpiPeriodicity(), hibKpiPeriodicity.getName(),
					hibKpiPeriodicity.getMonths(), hibKpiPeriodicity
							.getDays(), hibKpiPeriodicity.getHours(),
					hibKpiPeriodicity.getMinutes(), hibKpiPeriodicity.getChronString());
		} catch (HibernateException he) {
			logger.error("Error while loading the Periodicity with id " + ((id == null)?"":id.toString()), he);			

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10101);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				logger.debug("OUT");
			}
		}
		return toReturn;
	}

	public List loadPeriodicityList() throws EMFUserError {
		logger.debug("IN");
		List toReturn = null;
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			toReturn = new ArrayList();
			List toTransform = null;
			toTransform = aSession.createQuery("from SbiKpiPeriodicity").list();

			for (Iterator iterator = toTransform.iterator(); iterator.hasNext();) {
				SbiKpiPeriodicity hibKpiPeriodicity = (SbiKpiPeriodicity) iterator
						.next();
				Periodicity periodicity = new Periodicity(hibKpiPeriodicity
						.getIdKpiPeriodicity(), hibKpiPeriodicity.getName(),
						hibKpiPeriodicity.getMonths(), hibKpiPeriodicity
								.getDays(), hibKpiPeriodicity.getHours(),
						hibKpiPeriodicity.getMinutes(), hibKpiPeriodicity
								.getChronString());
				toReturn.add(periodicity);
			}

		} catch (HibernateException he) {
			logger.error("Error while loading the list of Periodicity", he);

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

	public Integer getPeriodicitySeconds(Integer periodicityId)
	throws EMFUserError {

		logger.debug("IN");
		Integer toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		int seconds = 0;
		logger.debug("Getting seconds of validity for the kpi Value");

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiKpiPeriodicity hibSbiKpiPeriodicity = (SbiKpiPeriodicity) aSession
			.load(SbiKpiPeriodicity.class, periodicityId);
			if (hibSbiKpiPeriodicity.getChronString() != null) {

			} else {
				if (hibSbiKpiPeriodicity.getDays() != null) {
					logger.debug("DAYS: "
							+ hibSbiKpiPeriodicity.getDays().toString());
					// 86400 seconds in a day
					seconds += hibSbiKpiPeriodicity.getDays().intValue() * 86400;
				}
				if (hibSbiKpiPeriodicity.getHours() != null) {
					logger.debug("HOURS: "
							+ hibSbiKpiPeriodicity.getHours().toString());
					// 3600 seconds in an hour
					seconds += hibSbiKpiPeriodicity.getHours().intValue() * 3600;
				}
				if (hibSbiKpiPeriodicity.getMinutes() != null) {
					logger.debug("MINUTES: "
							+ hibSbiKpiPeriodicity.getMinutes().toString());
					// 60 seconds in a minute
					seconds += hibSbiKpiPeriodicity.getMinutes().intValue() * 60;
				}
				if (hibSbiKpiPeriodicity.getMonths() != null) {
					logger.debug("MONTHS: "
							+ hibSbiKpiPeriodicity.getMonths().toString());
					// 2592000 seconds in a month of 30 days
					seconds += hibSbiKpiPeriodicity.getMonths().intValue() * 2592000;
				}
			}
			toReturn = new Integer(seconds);
			logger.debug("Total seconds: " + toReturn.toString());

		} catch (HibernateException he) {
			logger.error("Error while loading the Periodicity with id "
					+ periodicityId, he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 10114);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
				logger.debug("OUT");
			}
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void deletePeriodicity(Integer perId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			
			SbiKpiPeriodicity sbiKpiPeriodicity = (SbiKpiPeriodicity) aSession.load(
					SbiKpiPeriodicity.class, perId);
			aSession.delete(sbiKpiPeriodicity);

			tx.commit();

		} catch (ConstraintViolationException cve) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Impossible to delete a Periodicity", cve);
			throw new EMFUserError(EMFErrorSeverity.WARNING, 10014);

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logger.error("Error while delete a Periodicity ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			aSession.close();
		}
		
	}
	
}
