/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParuseId;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Defines the Hibernate implementations for all DAO methods,
 * for a ObjParuse object.  
 * 
 * @author Zerbetto
 */
public class ObjParuseDAOHibImpl extends AbstractHibernateDAO implements IObjParuseDAO {



	/**
	 * Modify obj paruse.
	 * 
	 * @param aObjParuse the a obj paruse
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#modifyObjParuse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse)
	 */
	public void modifyObjParuse(ObjParuse aObjParuse) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get the existing object
			/*String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + 
			             " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() + 
			             " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + 
			             " and s.id.filterOperation = '" + aObjParuse.getFilterOperation()+"'";*/
			String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = ? " + 
            " and s.id.sbiParuse.useId = ? "  + 
            " and s.id.sbiObjParFather.objParId = ? "  + 
            " and s.id.filterOperation = ? " ;
			
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, aObjParuse.getObjParId().intValue());
			hqlQuery.setInteger(1, aObjParuse.getParuseId().intValue());
			hqlQuery.setInteger(2, aObjParuse.getObjParFatherId().intValue());
			hqlQuery.setString(3, aObjParuse.getFilterOperation());
			
			SbiObjParuse sbiObjParuse = (SbiObjParuse)hqlQuery.uniqueResult();
			if (sbiObjParuse == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					    "modifyObjParuse", "the ObjParuse relevant to BIObjectParameter with " +
					    "id="+aObjParuse.getObjParId()+" and ParameterUse with " +
					    "id="+aObjParuse.getParuseId()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1043);
			}
			// delete the existing object
			aSession.delete(sbiObjParuse);
			// create the new object
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getObjParId());
			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, aObjParuse.getParuseId());
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getObjParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,  this.getClass().getName(), 
					                "modifyObjParuse", "the BIObjectParameter with " +
					                "id="+aObjParuse.getObjParFatherId()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1043);
			}
			SbiObjParuseId correlationId = new SbiObjParuseId();
			correlationId.setSbiObjPar(sbiObjPar);
			correlationId.setSbiParuse(sbiParuse);
			correlationId.setSbiObjParFather(sbiObjParFather);
			correlationId.setFilterOperation(aObjParuse.getFilterOperation());
			SbiObjParuse correlation = new SbiObjParuse(correlationId);
			correlation.setProg(aObjParuse.getProg());
			correlation.setFilterColumn(aObjParuse.getFilterColumn());
			correlation.setPreCondition(aObjParuse.getPreCondition());
			correlation.setPostCondition(aObjParuse.getPostCondition());
			correlation.setLogicOperator(aObjParuse.getLogicOperator());
			// save new object
			updateSbiCommonInfo4Insert(correlation);
			aSession.save(correlation);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		/*
		Criterion aCriterion = Expression.and(
				  Expression.eq("id.sbiObjPar.objParId", aObjParuse.getObjParId()), 
				  Expression.eq("id.sbiParuse.useId", aObjParuse.getParuseId()));
		Criteria aCriteria = aSession.createCriteria(SbiObjParuse.class);
		aCriteria.add(aCriterion);
		SbiObjParuse sbiObjParuse = (SbiObjParuse) aCriteria.uniqueResult();
		*/
	}


	/**
	 * Insert obj paruse.
	 * 
	 * @param aObjParuse the a obj paruse
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#insertObjParuse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse)
	 */
	public void insertObjParuse(ObjParuse aObjParuse) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getObjParId());
			SbiParuse sbiParuse = (SbiParuse) aSession.load(SbiParuse.class, aObjParuse.getParuseId());
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParuse.getObjParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,  this.getClass().getName(), 
					                "modifyObjParuse", "the BIObjectParameter with " +
					                "id="+aObjParuse.getObjParFatherId()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1043);
			}
			SbiObjParuseId correlationId = new SbiObjParuseId();
			correlationId.setSbiObjPar(sbiObjPar);
			correlationId.setSbiParuse(sbiParuse);
			correlationId.setSbiObjParFather(sbiObjParFather);
			correlationId.setFilterOperation(aObjParuse.getFilterOperation());
			SbiObjParuse correlation = new SbiObjParuse(correlationId);
			correlation.setProg(aObjParuse.getProg());
			correlation.setFilterColumn(aObjParuse.getFilterColumn());
			correlation.setPreCondition(aObjParuse.getPreCondition());
			correlation.setPostCondition(aObjParuse.getPostCondition());
			correlation.setLogicOperator(aObjParuse.getLogicOperator());
			updateSbiCommonInfo4Insert(correlation);
			aSession.save(correlation);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}


	/**
	 * Erase obj paruse.
	 * 
	 * @param aObjParuse the a obj paruse
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#eraseObjParuse(ObjParuse)
	 */
	public void eraseObjParuse(ObjParuse aObjParuse) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			eraseObjParuse(aObjParuse, aSession);
			
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		/*
		Criterion aCriterion = Expression.and(
				  Expression.eq("id.sbiObjPar.objParId", aObjParuse.getObjParId()), 
				  Expression.eq("id.sbiParuse.useId", aObjParuse.getParuseId()));
		Criteria aCriteria = aSession.createCriteria(SbiObjParuse.class);
		aCriteria.add(aCriterion);
		SbiObjParuse sbiObjParuse = (SbiObjParuse)aCriteria.uniqueResult();
		*/
	}

	
	public void eraseObjParuse(ObjParuse aObjParuse, Session aSession) throws EMFUserError {
		// get the existing object
		/*String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + 
		             " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() + 
		             " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + 
		             " and s.id.filterOperation = '" + aObjParuse.getFilterOperation() + "'";*/
		String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = ? "  + 
        " and s.id.sbiParuse.useId = ? " +  
        " and s.id.sbiObjParFather.objParId = ? "  + 
        " and s.id.filterOperation = ? ";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParuse.getObjParId().intValue());
		hqlQuery.setInteger(1, aObjParuse.getParuseId().intValue());
		hqlQuery.setInteger(2, aObjParuse.getObjParFatherId().intValue());
		hqlQuery.setString(3,  aObjParuse.getFilterOperation());
		
		SbiObjParuse sbiObjParuse = (SbiObjParuse)hqlQuery.uniqueResult();
		if (sbiObjParuse == null) {		
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
			    "eraseObjParuse", "the ObjParuse relevant to BIObjectParameter with " +
			    "id="+aObjParuse.getObjParId()+" and ParameterUse with " +
			    "id="+aObjParuse.getParuseId()+" does not exist.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1045);
		}
		aSession.delete(sbiObjParuse);
	}

	/**
	 * Load obj paruses.
	 * 
	 * @param objParId the obj par id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#loadObjParuses(Integer)
	 */
	public List loadObjParuses(Integer objParId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + objParId + " order by s.prog";	
			String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List sbiObjParuses = hqlQuery.list();
			Iterator it = sbiObjParuses.iterator();
			while (it.hasNext()){
				toReturn.add(toObjParuse((SbiObjParuse)it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}
	
	/**
	 * From the hibernate SbiObjParuse at input, gives
	 * the corrispondent <code>ObjParuse</code> object.
	 * 
	 * @param aSbiObjParuse The hybernate SbiObjParuse
	 * 
	 * @return The corrispondent <code>ObjParuse</code>
	 */
	public ObjParuse toObjParuse (SbiObjParuse aSbiObjParuse) {
		if (aSbiObjParuse == null) return null;
		ObjParuse toReturn = new ObjParuse();
		toReturn.setObjParId(aSbiObjParuse.getId().getSbiObjPar().getObjParId());
		toReturn.setParuseId(aSbiObjParuse.getId().getSbiParuse().getUseId());
		toReturn.setProg(aSbiObjParuse.getProg());
		toReturn.setObjParFatherId(aSbiObjParuse.getId().getSbiObjParFather().getObjParId());
		toReturn.setFilterColumn(aSbiObjParuse.getFilterColumn());
		toReturn.setFilterOperation(aSbiObjParuse.getId().getFilterOperation());
		toReturn.setPreCondition(aSbiObjParuse.getPreCondition());
		toReturn.setPostCondition(aSbiObjParuse.getPostCondition());
		toReturn.setLogicOperator(aSbiObjParuse.getLogicOperator());
		return toReturn;
	}

	
	/**
	 * Gets the dependencies.
	 * 
	 * @param objParFatherId the obj par father id
	 * 
	 * @return the dependencies
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#getDependencies(Integer)
	 */
	public List getDependencies(Integer objParFatherId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get all the sbiobjparuse objects which have the parameter as the father
			//String hql = "from SbiObjParuse s where s.id.sbiObjParFather=" + objParFatherId;
			String hql = "from SbiObjParuse s where s.id.sbiObjParFather=? " ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParFatherId.intValue());
			List objParuses = query.list();
			if (objParuses == null || objParuses.size() == 0) 
				return toReturn;
			// add to the list all the distinct labels of parameter which depend form the father parameter 
			Iterator it = objParuses.iterator();
			while (it.hasNext()) {
				SbiObjParuse objParuseHib = (SbiObjParuse) it.next();
				Integer objParId = objParuseHib.getId().getSbiObjPar().getObjParId();
				SbiObjPar hibObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, objParId);
				String label = hibObjPar.getLabel();
				if(!toReturn.contains(label)){
					toReturn.add(label);
				}
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}
	
	
	/**
	 * Gets the all dependencies for parameter use.
	 * 
	 * @param useId the use id
	 * 
	 * @return the all dependencies for parameter use
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#getAllDependenciesForParameterUse(java.lang.Integer)
	 */
	public List getAllDependenciesForParameterUse(Integer useId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjParuse s where s.id.sbiParuse.useId = " + useId;
			String hql = "from SbiObjParuse s where s.id.sbiParuse.useId = ?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, useId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				toReturn.add(toObjParuse((SbiObjParuse) it.next()));
			}
			tx.commit();
		} catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}
	
	
	/**
	 * Gets the document labels list with associated dependencies.
	 * 
	 * @param useId the use id
	 * 
	 * @return the document labels list with associated dependencies
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#getDocumentLabelsListWithAssociatedDependencies(java.lang.Integer)
	 */
	public List getDocumentLabelsListWithAssociatedDependencies(Integer useId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = 
					"select " +
					"	distinct(obj.label) " +
					"from " +
					"	SbiObjects obj, SbiObjParuse s " +
					"where " +
					"	obj.sbiObjPars.objParId = s.id.sbiObjPar.objParId and " +
					"	s.id.sbiParuse.useId = " + useId;
			*/
			String hql = 
				"select " +
				"	distinct(obj.label) " +
				"from " +
				"	SbiObjects obj, SbiObjPar p, SbiObjParuse s " +
				"where " +
				"	obj.biobjId = p.sbiObject.biobjId and " + 
				"	p.objParId = s.id.sbiObjPar.objParId and " +
				"	s.id.sbiParuse.useId = ?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, useId.intValue());
			List result = query.list();
			toReturn = result;
			tx.commit();
		} catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);  
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}
	
	
	/**
	 * Load obj paruse.
	 * 
	 * @param objParId the obj par id
	 * @param paruseId the paruse id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#loadObjParuse(java.lang.Integer, java.lang.Integer)
	 */
	public List loadObjParuse(Integer objParId, Integer paruseId) throws EMFUserError {
		List objparuses = new ArrayList();
		ObjParuse toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			Criterion aCriterion = Expression.and(
											  Expression.eq("id.sbiObjPar.objParId", objParId), 
											  Expression.eq("id.sbiParuse.useId", paruseId));
			Criteria aCriteria = aSession.createCriteria(SbiObjParuse.class);
			aCriteria.add(aCriterion);
			List sbiObjParuses = (List) aCriteria.list();
			*/
			/*String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId=" + objParId + 
			             " and s.id.sbiParuse.useId=" +  paruseId +
			             " order by s.prog";
			*/
			String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId=? "+ 
            " and s.id.sbiParuse.useId=? " +
            " order by s.prog";
			
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParId.intValue());
			query.setInteger(1, paruseId.intValue());
			
			List sbiObjParuses = query.list();
			if(sbiObjParuses==null) 
				return objparuses;
			Iterator itersbiOP = sbiObjParuses.iterator();
			while(itersbiOP.hasNext()) {
				SbiObjParuse sbiop = (SbiObjParuse)itersbiOP.next();
			    ObjParuse op = toObjParuse(sbiop);
			    objparuses.add(op);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return objparuses;
	}
	
	
	
	public void eraseObjParuseIfExists(ObjParuse aObjParuse, Session aSession) throws EMFUserError {
		// get the existing object
		/*String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + 
		             " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() + 
		             " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + 
		             " and s.id.filterOperation = '" + aObjParuse.getFilterOperation() + "'";*/
		String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = ? "  + 
        " and s.id.sbiParuse.useId = ? " +  
        " and s.id.sbiObjParFather.objParId = ? "  + 
        " and s.id.filterOperation = ? ";
		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParuse.getObjParId().intValue());
		hqlQuery.setInteger(1, aObjParuse.getParuseId().intValue());
		hqlQuery.setInteger(2, aObjParuse.getObjParFatherId().intValue());
		hqlQuery.setString(3,  aObjParuse.getFilterOperation());
		
		SbiObjParuse sbiObjParuse = (SbiObjParuse)hqlQuery.uniqueResult();
		if (sbiObjParuse == null) {		
		}
		else{
			aSession.delete(sbiObjParuse);			
		}
	}
	
	
	public List loadObjParusesFather(Integer objParId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + objParId + " order by s.prog";	
			String hql = "from SbiObjParuse s where s.id.sbiObjParFather.objParId = ? order by s.prog"; 
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List sbiObjParuses = hqlQuery.list();
			Iterator it = sbiObjParuses.iterator();
			while (it.hasNext()){
				toReturn.add(toObjParuse((SbiObjParuse)it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return toReturn;
	}

	
}
