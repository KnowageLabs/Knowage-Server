/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.kpi.config.bo.KpiError;
import it.eng.spagobi.kpi.config.metadata.SbiKpiError;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelInst;
import it.eng.spagobi.tools.dataset.exceptions.DatasetException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class KpiErrorDAOImpl extends AbstractHibernateDAO implements IKpiErrorDAO{

	private static final Logger logger = Logger.getLogger(KpiErrorDAOImpl.class);


	/**	Takes as input a datasetException and converts it to a KpiError
	 * 
	 * @param exception
	 * @param modelInstanceId
	 * @param parameters
	 * @return
	 * @throws EMFUserError
	 */

	public Integer insertKpiError(	DatasetException exception, Integer modelInstanceId, String resourceName) throws EMFUserError{
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		Integer id = null;
		try {
			SbiKpiError sbiKpiError = toSbiKpiError(exception, modelInstanceId, resourceName, session);
			tx = session.beginTransaction();
			id = (Integer)session.save(sbiKpiError);
			updateSbiCommonInfo4Insert(sbiKpiError);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			if(session != null){
				session.close();
			}

			logger.debug("OUT");
		}
		return id;
	}
	
	
	


	/**
	 * @param kpiError
	 * @return
	 * @throws EMFUserError
	 */
	public Integer insertKpiError(	SbiKpiError sbiKpiError) throws EMFUserError{
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		Integer id = null;
		try {
			tx = session.beginTransaction();
			updateSbiCommonInfo4Insert(sbiKpiError);
			id = (Integer)session.save(sbiKpiError);
			
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			if(session != null){
				session.close();
			}

			logger.debug("OUT");
		}
		return id;
	}

	/**	
	 * @return
	 * @throws EMFUserError
	 */
	public List<KpiError> loadAllKpiErrors() throws EMFUserError{
		Session session = getSession();
		Transaction tx = null;
		List<KpiError> kpiErrors = null;
		try {
			tx = session.beginTransaction();

			List<SbiKpiError> list = (List<SbiKpiError>)session.createQuery("from SbiKpiError").list();

			if(list != null){

				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					SbiKpiError sbiKpiError = (SbiKpiError) iterator.next();
					if(kpiErrors == null) kpiErrors = new ArrayList<KpiError>();
					kpiErrors.add(toKpiError(sbiKpiError));					
				}

			}

			tx.commit();
			return kpiErrors;

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
	}

	/**
	 * 
	 * @return
	 * @throws EMFUserError
	 */
	public KpiError loadKpiErrorById(Integer id) throws EMFUserError {
		logger.debug("IN");
		KpiError kpiError = null;
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			SbiKpiError sbiKpiError = (SbiKpiError)session.get(SbiKpiError.class, id);
			kpiError = toKpiError(sbiKpiError);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
		return kpiError;
	}

	/**
	 * @param kpiError
	 * @return
	 * @throws EMFUserError
	 */
	public void updateKpiError(SbiKpiError sbiKpiError) throws EMFUserError{
		logger.debug("IN");
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(sbiKpiError);
			tx.commit();

		} catch (HibernateException e) {
			if( tx != null && tx.isActive() ){
				tx.rollback();
			}
			throw e;

		}finally{
			session.close();
		}
		logger.debug("OUT");
	}



	public KpiError toKpiError(SbiKpiError sbiKpiError){
		logger.debug("IN");
		KpiError toReturn=new KpiError();

		toReturn.setKpiErrorId(sbiKpiError.getKpiErrorId());
		toReturn.setUserMessage(sbiKpiError.getUserMessage());
		toReturn.setLabelModInst(sbiKpiError.getLabelModInst());
		toReturn.setParameters(sbiKpiError.getParameters());
		toReturn.setTsDate(sbiKpiError.getTsDate());
		toReturn.setFullMessage(sbiKpiError.getFullMessage());

		if(sbiKpiError.getSbiKpiModelInst() != null)
			toReturn.setKpiModelInstId(sbiKpiError.getSbiKpiModelInst().getKpiModelInst());

		logger.debug("OUT");
		return toReturn;
	}

	public SbiKpiError toSbiKpiError(DatasetException dsException, Integer modelInstanceId, String parameters, Session session){
		logger.debug("IN");

		SbiKpiError sbiKpiError = new SbiKpiError();

		sbiKpiError.setUserMessage(dsException.getUserMessage());
		sbiKpiError.setFullMessage(dsException.getFullMessage());

		SbiKpiModelInst sbiKpiModelInst = (SbiKpiModelInst)session.load(SbiKpiModelInst.class, modelInstanceId);
		sbiKpiError.setSbiKpiModelInst(sbiKpiModelInst);
		if(sbiKpiModelInst != null){
			sbiKpiError.setLabelModInst(sbiKpiModelInst.getLabel());
		}

		sbiKpiError.setTsDate(new Date());
		sbiKpiError.setParameters(parameters);

		logger.debug("OUT");
		return sbiKpiError;
	}



}
