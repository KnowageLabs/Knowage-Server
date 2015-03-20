/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.kpi.model.bo.ModelResources;
import it.eng.spagobi.kpi.model.bo.Resource;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelInst;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;
import it.eng.spagobi.kpi.model.metadata.SbiResources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;

public class ModelResourceDAOImpl extends AbstractHibernateDAO implements
		IModelResourceDAO {

	static private Logger logger = Logger.getLogger(ModelResourceDAOImpl.class);
	
	
	
	public Resource toResource(SbiKpiModelResources re) throws EMFUserError {

		logger.debug("IN");
		Resource toReturn = new Resource();

		IResourceDAO resDao=DAOFactory.getResourceDAO();
		
		SbiResources r = re.getSbiResources();
		toReturn = resDao.toResource(r);
		logger.debug("OUT");
		return toReturn;
	}
	
	
	
	public List loadModelResourceByModelId(Integer modelInstId)
			throws EMFUserError {

		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Query hibQuery = aSession.createQuery(" from SbiKpiModelResources where sbiKpiModelInst = " + modelInstId);
			
			List hibList = hibQuery.list();
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				ModelResources modRes=toModelResources((SbiKpiModelResources) it.next());
				realResult.add(modRes);
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while loading all model resource ", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();				
			}
		}
		logger.debug("OUT");
		return realResult;
	}

	public void addModelResource(Integer modelId, Integer resourceId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			SbiKpiModelResources aModelResources = new SbiKpiModelResources();
			
			SbiKpiModelInst aModelInst = (SbiKpiModelInst) aSession.load(
					SbiKpiModelInst.class, modelId);
			SbiResources aResource = (SbiResources) aSession.load(
					SbiResources.class, resourceId);
			aModelResources.setSbiKpiModelInst(aModelInst);
			aModelResources.setSbiResources(aResource);
			
			aSession.save(aModelResources);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			aSession.close();
		}


	}

	public boolean isSelected(Integer modelId, Integer resourceId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		boolean toReturn = false;
		try {
			tx = aSession.beginTransaction();
			List modelResourceList = getModelResource(aSession, modelId, resourceId);
			if (modelResourceList.isEmpty())
				toReturn = false;
			else
				toReturn = true;
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			aSession.close();
		}
		return toReturn;
	}

	public void removeModelResource(Integer modelId, Integer resourceId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			List modelResourceList = getModelResource(aSession, modelId, resourceId);
			for (Iterator iterator = modelResourceList.iterator(); iterator
					.hasNext();) {
				SbiKpiModelResources modelResource = (SbiKpiModelResources) iterator.next();
				aSession.delete(modelResource);
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			aSession.close();
		}
	}

	private List getModelResource(Session aSession, Integer modelId,
			Integer resourceId) {
		SbiKpiModelInst aModelInst = (SbiKpiModelInst) aSession.load(
				SbiKpiModelInst.class, modelId);
		SbiResources aResource = (SbiResources) aSession.load(
				SbiResources.class, resourceId);
		Criteria crit = aSession.createCriteria(SbiKpiModelResources.class);
		crit.add(Expression.eq("sbiKpiModelInst", aModelInst));
		crit.add(Expression.eq("sbiResources", aResource));
		return crit.list();	
	}
	
	private List getModelResource(Session aSession, Integer modelId) {
		SbiKpiModelInst aModelInst = (SbiKpiModelInst) aSession.load(
				SbiKpiModelInst.class, modelId);
		Criteria crit = aSession.createCriteria(SbiKpiModelResources.class);
		crit.add(Expression.eq("sbiKpiModelInst", aModelInst));
		return crit.list();	
	}

	public void removeAllModelResource(Integer modelId) throws EMFUserError {
		Session aSession = getSession();
		Transaction tx = null;
		try {
			tx = aSession.beginTransaction();
			List modelResourceList = getModelResource(aSession, modelId);
			for (Iterator iterator = modelResourceList.iterator(); iterator
					.hasNext();) {
				SbiKpiModelResources modelResource = (SbiKpiModelResources) iterator.next();
				aSession.delete(modelResource);
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw new EMFUserError(EMFErrorSeverity.ERROR, 101);

		} finally {
			aSession.close();
		}
	}
	
	
	private ModelResources toModelResources(SbiKpiModelResources sbiMr) {

		logger.debug("IN");
		ModelResources toReturn = new ModelResources();


		Integer modelResourcesId=sbiMr.getKpiModelResourcesId();
		
		Integer modelInstId=null;
		if(sbiMr.getSbiKpiModelInst()!=null){
			modelInstId=sbiMr.getSbiKpiModelInst().getKpiModelInst();
		}

		Integer resourceId=null;
		if(sbiMr.getSbiResources()!=null){
			resourceId=sbiMr.getSbiResources().getResourceId();
		}
		toReturn.setModelResourcesId(modelResourcesId);
		toReturn.setModelInstId(modelInstId);
		toReturn.setResourceId(resourceId);
		

		logger.debug("OUT");
		return toReturn;
	}
	
}
