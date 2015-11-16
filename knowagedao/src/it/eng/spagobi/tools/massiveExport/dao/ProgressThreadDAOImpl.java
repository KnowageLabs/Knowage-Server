/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.massiveExport.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.metadata.SbiProgressThread;
//import it.eng.spagobi.tools.massiveExport.work.MassiveExportWork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ProgressThreadDAOImpl extends AbstractHibernateDAO implements IProgressThreadDAO {
	
	public static final String PREPARED = "PREPARED";
	public static final String STARTED = "STARTED";
	public static final String DOWNLOAD = "DOWNLOAD";
	public static final String ERROR = "ERROR";

	// logger component
	private static Logger logger = Logger.getLogger(ProgressThreadDAOImpl.class);

	public ProgressThread loadProgressThreadById(Integer progressThreadId) throws EMFUserError {
		logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ?" );
			hibPT.setInteger(0, progressThreadId);
			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();
			if(sbiProgressThread!=null){
				toReturn = toProgressThread(sbiProgressThread);
			}	
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progresThreadId", he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return null;
	}





	public List<ProgressThread> loadActiveProgressThreadsByUserId(
			String userId) throws EMFUserError {
		//logger.debug("IN");
		List<ProgressThread> toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.userId = ? AND (h.status = '"+STARTED+"' OR h.status = '"+PREPARED+"')" );
			hibPT.setString(0, userId);

			List sbiProgressThreadList = hibPT.list();
			if(sbiProgressThreadList!=null){
				toReturn = new ArrayList<ProgressThread>();
				for (Iterator iterator = sbiProgressThreadList.iterator(); iterator.hasNext();) {
					SbiProgressThread sbiPT = (SbiProgressThread) iterator.next();
					ProgressThread pT = toProgressThread(sbiPT);
					toReturn.add(pT);
				}
			}

			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Threads with userId"+userId + " and status STARTED or prepared", he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return toReturn;
	}

	public List<ProgressThread> loadNotClosedProgressThreadsByUserId(
			String userId) throws EMFUserError {
		//logger.debug("IN");
		List<ProgressThread> toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.userId = ? AND h.status != 'CLOSED'" );
			hibPT.setString(0, userId);

			List sbiProgressThreadList = hibPT.list();
			if(sbiProgressThreadList!=null){
				toReturn = new ArrayList<ProgressThread>();
				for (Iterator iterator = sbiProgressThreadList.iterator(); iterator.hasNext();) {
					SbiProgressThread sbiPT = (SbiProgressThread) iterator.next();
					ProgressThread pT = toProgressThread(sbiPT);
					toReturn.add(pT);
				}
			}

			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Threads with userId"+userId + " and status NOT CLOSED", he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return toReturn;
	}







	public ProgressThread loadActiveProgressThreadByUserIdAndFuncCd(String userId, String functCd) throws EMFUserError {
		//logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.userId = ? AND h.functionCd = ? AND (h.status = '"+STARTED+"' OR h.status ='"+PREPARED+"') " );
			hibPT.setString(0, userId);
			hibPT.setString(1, functCd);

			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();
			if(sbiProgressThread!=null){
				toReturn = toProgressThread(sbiProgressThread);
			}	
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progresThreadId", he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return toReturn;
	}




	public boolean incrementProgressThread(Integer progressThreadId) throws EMFUserError{
		//logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ?" );
			hibPT.setInteger(0, progressThreadId);
			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();

			Integer partial = sbiProgressThread.getPartial();
			sbiProgressThread.setPartial(partial+1);


			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progressThreadId = "+progressThreadId, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return true;

	}

	public Integer insertProgressThread(ProgressThread progThread) throws EMFUserError {
		//logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		SbiProgressThread sbiPT = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			sbiPT = new SbiProgressThread();
			sbiPT.setFunctionCd(progThread.getFunctionCd());
			sbiPT.setUserId(progThread.getUserId());

			sbiPT.setTotal(progThread.getTotal());
			sbiPT.setPartial(0);
			sbiPT.setStatus(PREPARED);
			sbiPT.setType(progThread.getType());
			sbiPT.setRandomKey(progThread.getRandomKey());

			aSession.save(sbiPT);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while inserting the progress thread with user id " + progThread.getUserId() + " and on functionality "+progThread.getFunctionCd(), he);

			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return sbiPT.getProgressThreadId();
	}




	public ProgressThread toProgressThread(SbiProgressThread sbiPT){
		//logger.debug("IN");
		ProgressThread toReturn = new ProgressThread();

		toReturn.setUserId(sbiPT.getUserId());
		toReturn.setFunctionCd(sbiPT.getFunctionCd());
		toReturn.setStatus(sbiPT.getStatus());
		toReturn.setType(sbiPT.getType());
		toReturn.setProgressThreadId(sbiPT.getProgressThreadId());
		toReturn.setRandomKey(sbiPT.getRandomKey());

		toReturn.setTotal(sbiPT.getTotal());
		toReturn.setPartial(sbiPT.getPartial());
		//logger.debug("OUT");
		return toReturn;
	}


	public void setStartedProgressThread(Integer progressThreadId) throws EMFUserError{
		//logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ? " );
			hibPT.setInteger(0, progressThreadId);
			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();
			sbiProgressThread.setStatus(STARTED);
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progressThreadId = "+progressThreadId, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");

	}
	

	public void setDownloadProgressThread(Integer progressThreadId) throws EMFUserError{
		//logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ? " );
			hibPT.setInteger(0, progressThreadId);
			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();
			sbiProgressThread.setStatus(DOWNLOAD);
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progressThreadId = "+progressThreadId, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");

	}

	public void closeProgressThread(Integer progressThreadId) throws EMFUserError{
		//logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ? " );
			hibPT.setInteger(0, progressThreadId);
			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();
			sbiProgressThread.setStatus("CLOSED");
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progressThreadId = "+progressThreadId, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");

	}





	public boolean deleteProgressThread(Integer progressThreadId) throws EMFUserError{
		//logger.debug("IN");

		boolean found = false;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ? " );
			hibPT.setInteger(0, progressThreadId);
			Object sbiProgressThreadO =hibPT.uniqueResult();
			
			if(sbiProgressThreadO  != null) {
				SbiProgressThread pT = (SbiProgressThread)sbiProgressThreadO;
				found=true;
				aSession.delete(pT);
				tx.commit();
			}

		} catch (HibernateException he) {
			logger.error("Error while deletering Progress Thread with progressThreadId = "+progressThreadId, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");
		return found;
	}






	public void setErrorProgressThread(Integer progressThreadId) throws EMFUserError{
		//logger.debug("IN");
		ProgressThread toReturn = null;

		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibPT = aSession.createQuery("from SbiProgressThread h where h.progressThreadId = ? " );
			hibPT.setInteger(0, progressThreadId);
			SbiProgressThread sbiProgressThread =(SbiProgressThread)hibPT.uniqueResult();
			sbiProgressThread.setStatus("ERROR");
			tx.commit();

		} catch (HibernateException he) {
			logger.error("Error while loading Progress Thread with progressThreadId = "+progressThreadId, he);			
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
				//logger.debug("OUT");
			}
		}
		//logger.debug("OUT");

	}




}



