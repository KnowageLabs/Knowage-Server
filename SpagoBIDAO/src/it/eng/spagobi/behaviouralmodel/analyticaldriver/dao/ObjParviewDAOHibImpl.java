/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParviewId;
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
 * for a ObjParview object.  
 * 
 * @author gavardi
 */
public class ObjParviewDAOHibImpl extends AbstractHibernateDAO implements IObjParviewDAO {



	/**
	 * Modify obj parview.
	 * 
	 * @param aObjParview the a obj parview
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#modifyObjParview(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview)
	 */
	public void modifyObjParview(ObjParview aObjParview) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get the existing object
			String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId = ? " + 
            " and s.id.sbiObjParFather.objParId = ? "  + 
            " and s.id.operation = ? " +
            " and s.id.compareValue = ? ";
			
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, aObjParview.getObjParId().intValue());
			hqlQuery.setInteger(1, aObjParview.getObjParFatherId().intValue());
			hqlQuery.setString(2, aObjParview.getOperation());
			hqlQuery.setString(3, aObjParview.getCompareValue());
			
			SbiObjParview sbiObjParview = (SbiObjParview)hqlQuery.uniqueResult();
			if (sbiObjParview == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					    "modifyObjParview", "the ObjParview relevant to BIObjectParameter with " +
					    "id="+aObjParview.getObjParId()+"  does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1043);
			}
			// delete the existing object
			aSession.delete(sbiObjParview);
			// create the new object
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getObjParId());
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getObjParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,  this.getClass().getName(), 
					                "modifyObjParview", "the BIObjectParameter with " +
					                " does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1043);
			}
			SbiObjParviewId viewId = new SbiObjParviewId();
			viewId.setSbiObjPar(sbiObjPar);
			viewId.setSbiObjParFather(sbiObjParFather);
			viewId.setOperation(aObjParview.getOperation());
			SbiObjParview view = new SbiObjParview(viewId);
			view.setProg(aObjParview.getProg());
			view.setViewLabel(aObjParview.getViewLabel());

			// save new object
			updateSbiCommonInfo4Insert(view);
			aSession.save(view);
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
	 * Insert obj parview.
	 * 
	 * @param aObjParview the a obj parview
	 * 
	 * @throws EMFviewrError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO#insertObjParuse(it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse)
	 */
	public void insertObjParview(ObjParview aObjParview) throws EMFUserError {

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjPar sbiObjPar = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getObjParId());
			SbiObjPar sbiObjParFather = (SbiObjPar) aSession.load(SbiObjPar.class, aObjParview.getObjParFatherId());
			if (sbiObjParFather == null) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,  this.getClass().getName(), 
					                "modifyObjParview", "the BIObjectParameter with " +
					                "id="+aObjParview.getObjParFatherId()+" does not exist.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 1043);
			}
			SbiObjParviewId viewId = new SbiObjParviewId();
			viewId.setSbiObjPar(sbiObjPar);
			viewId.setSbiObjParFather(sbiObjParFather);
			viewId.setOperation(aObjParview.getOperation());
			viewId.setCompareValue(aObjParview.getCompareValue());
			SbiObjParview view = new SbiObjParview(viewId);
			view.setProg(aObjParview.getProg());
			view.setViewLabel(aObjParview.getViewLabel());
			updateSbiCommonInfo4Insert(view);
			aSession.save(view);
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
	 * Erase obj parview.
	 * 
	 * @param aObjParview the a obj parview
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#eraseObjParview(ObjParview)
	 */
	public void eraseObjParview(ObjParview aObjParview) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			eraseObjParview(aObjParview, aSession);
			
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

	
	public void eraseObjParview(ObjParview aObjParview, Session aSession) throws EMFUserError {
		// get the existing object
		/*String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + 
		             " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() + 
		             " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + 
		             " and s.id.filterOperation = '" + aObjParuse.getFilterOperation() + "'";*/
		String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId = ? "  + 
        " and s.id.sbiObjParFather.objParId = ? "  + 
        " and s.id.operation = ? " +
        " and s.id.compareValue = ? ";

		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParview.getObjParId().intValue());
		hqlQuery.setInteger(1, aObjParview.getObjParFatherId().intValue());
		hqlQuery.setString(2,  aObjParview.getOperation());
		hqlQuery.setString(3,  aObjParview.getCompareValue());
		
		SbiObjParview sbiObjParview = (SbiObjParview)hqlQuery.uniqueResult();
		if (sbiObjParview == null) {		
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
			    "eraseObjParview", "the ObjParview relevant to BIObjectParameter with " +
			    "id="+aObjParview.getObjParId()+" does not exist.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, 1045);
		}
		aSession.delete(sbiObjParview);
	}

	/**
	 * Load obj parviews.
	 * 
	 * @param objParId the obj par id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#loadObjParviews(Integer)
	 */
	public List<ObjParview> loadObjParviews(Integer objParId) throws EMFUserError {
		List<ObjParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + objParId + " order by s.prog";	
			String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List sbiObjParviews = hqlQuery.list();
			Iterator it = sbiObjParviews.iterator();
			while (it.hasNext()){
				toReturn.add(toObjParview((SbiObjParview)it.next()));
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
	 * From the hibernate SbiObjParview at input, gives
	 * the corrispondent <code>ObjParview</code> object.
	 * 
	 * @param aSbiObjParview The hybernate SbiObjParview
	 * 
	 * @return The corrispondent <code>ObjParview</code>
	 */
	public ObjParview toObjParview (SbiObjParview aSbiObjParview) {
		if (aSbiObjParview == null) return null;
		ObjParview toReturn = new ObjParview();
		toReturn.setObjParId(aSbiObjParview.getId().getSbiObjPar().getObjParId());
		toReturn.setObjParFatherId(aSbiObjParview.getId().getSbiObjParFather().getObjParId());
		toReturn.setOperation(aSbiObjParview.getId().getOperation());
		toReturn.setCompareValue(aSbiObjParview.getId().getCompareValue());
		toReturn.setProg(aSbiObjParview.getProg());
		toReturn.setViewLabel(aSbiObjParview.getViewLabel());

		return toReturn;
	}

	
	/**
	 * Gets the dependencies.
	 * 
	 * @param objParFatherId the obj par father id
	 * 
	 * @return the dependencies
	 * 
	 * @throws EMFviewrError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#getDependencies(Integer)
	 */
	public List getDependencies(Integer objParFatherId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// get all the sbiobjparview objects which have the parameter as the father
			//String hql = "from SbiObjParview s where s.id.sbiObjParFather=" + objParFatherId;
			String hql = "from SbiObjParview s where s.id.sbiObjParFather=? " ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParFatherId.intValue());
			List objParviews = query.list();
			if (objParviews == null || objParviews.size() == 0) 
				return toReturn;
			// add to the list all the distinct labels of parameter which depend form the father parameter 
			Iterator it = objParviews.iterator();
			while (it.hasNext()) {
				SbiObjParview objParviewHib = (SbiObjParview) it.next();
				Integer objParId = objParviewHib.getId().getSbiObjPar().getObjParId();
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
	 * Gets the all dependencies for parameter view.
	 * 
	 * @param viewId the view id
	 * 
	 * @return the all dependencies for parameter view
	 * 
	 * @throws EMFviewrError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#getAllDependenciesForParameterview(java.lang.Integer)
	 */
	public List getAllDependenciesForParameterview(Integer viewId) throws EMFUserError {
		List toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjParview s where s.id.sbiParview.viewId = " + viewId;
			String hql = "from SbiObjParview s where s.id.sbiParview.viewId = ?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, viewId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				toReturn.add(toObjParview((SbiObjParview) it.next()));
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
	 * @param viewId the view id
	 * 
	 * @return the document labels list with associated dependencies
	 * 
	 * @throws EMFviewrError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#getDocumentLabelsListWithAssociatedDependencies(java.lang.Integer)
	 */
	public List getDocumentLabelsListWithAssociatedDependencies(Integer viewId) throws EMFUserError {
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
					"	SbiObjects obj, SbiObjParview s " +
					"where " +
					"	obj.sbiObjPars.objParId = s.id.sbiObjPar.objParId and " +
					"	s.id.sbiParview.viewId = " + viewId;
			*/
			String hql = 
				"select " +
				"	distinct(obj.label) " +
				"from " +
				"	SbiObjects obj, SbiObjPar p, SbiObjParview s " +
				"where " +
				"	obj.biobjId = p.sbiObject.biobjId and " + 
				"	p.objParId = s.id.sbiObjPar.objParId and ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, viewId.intValue());
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
	 * Load obj parview.
	 * 
	 * @param objParId the obj par id
	 * @param parviewId the parview id
	 * 
	 * @return the list
	 * 
	 * @throws EMFviewrError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#loadObjParview(java.lang.Integer, java.lang.Integer)
	 */
	public List loadObjParview(Integer objParId, Integer parviewId) throws EMFUserError {
		List objparviews = new ArrayList();
		ObjParview toReturn = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*
			Criterion aCriterion = Expression.and(
											  Expression.eq("id.sbiObjPar.objParId", objParId), 
											  Expression.eq("id.sbiParview.viewId", parviewId));
			Criteria aCriteria = aSession.createCriteria(SbiObjParview.class);
			aCriteria.add(aCriterion);
			List sbiObjParviews = (List) aCriteria.list();
			*/
			/*String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId=" + objParId + 
			             " and s.id.sbiParview.viewId=" +  parviewId +
			             " order by s.prog";
			*/
			String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId=? "+ 
            " and s.id.sbiParview.viewId=? " +
            " order by s.prog";
			
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objParId.intValue());
			query.setInteger(1, parviewId.intValue());
			
			List sbiObjParviews = query.list();
			if(sbiObjParviews==null) 
				return objparviews;
			Iterator itersbiOP = sbiObjParviews.iterator();
			while(itersbiOP.hasNext()) {
				SbiObjParview sbiop = (SbiObjParview)itersbiOP.next();
			    ObjParview op = toObjParview(sbiop);
			    objparviews.add(op);
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
		return objparviews;
	}
	
	
	
	/**
	 * Load obj parviews with father relationship
	 * 
	 * @param objParId the obj par id
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO#loadObjParviews(Integer)
	 */
	public List<ObjParview> loadObjParviewsFather(Integer objParId) throws EMFUserError {
		List<ObjParview> toReturn = new ArrayList();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + objParId + " order by s.prog";	
			String hql = "from SbiObjParview s where s.id.sbiObjParFather = ? order by s.prog";
			Query hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, objParId.intValue());
			List sbiObjParviews = hqlQuery.list();
			Iterator it = sbiObjParviews.iterator();
			while (it.hasNext()){
				toReturn.add(toObjParview((SbiObjParview)it.next()));
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
	
	
	
	public void eraseObjParviewIfExists(ObjParview aObjParview, Session aSession) throws EMFUserError {
		// get the existing object
		/*String hql = "from SbiObjParuse s where s.id.sbiObjPar.objParId = " + aObjParuse.getObjParId() + 
		             " and s.id.sbiParuse.useId = " + aObjParuse.getParuseId() + 
		             " and s.id.sbiObjParFather.objParId = " + aObjParuse.getObjParFatherId() + 
		             " and s.id.filterOperation = '" + aObjParuse.getFilterOperation() + "'";*/
		String hql = "from SbiObjParview s where s.id.sbiObjPar.objParId = ? "  + 
		" and s.id.sbiObjParFather.objParId = ? "  + 
		" and s.id.operation = ? " +
		" and s.id.compareValue = ? ";

		Query hqlQuery = aSession.createQuery(hql);
		hqlQuery.setInteger(0, aObjParview.getObjParId().intValue());
		hqlQuery.setInteger(1, aObjParview.getObjParFatherId().intValue());
		hqlQuery.setString(2,  aObjParview.getOperation());
		hqlQuery.setString(3,  aObjParview.getCompareValue());

		SbiObjParview sbiObjParview = (SbiObjParview)hqlQuery.uniqueResult();
		if (sbiObjParview == null) {		
		}
		else{		aSession.delete(sbiObjParview);
		}
	}
	
	
	

	
}
