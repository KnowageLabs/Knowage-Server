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
