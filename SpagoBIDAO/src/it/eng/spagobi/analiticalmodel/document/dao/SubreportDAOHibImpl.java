/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubreports;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubreportsId;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Gioia
 *
 */
public class SubreportDAOHibImpl 
extends AbstractHibernateDAO 
implements ISubreportDAO {

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO#loadSubreportsByMasterRptId(java.lang.Integer)
	 */
	public List loadSubreportsByMasterRptId(Integer master_rpt_id) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			//hql = " from SbiSubreports as subreport " + 
	        // "where subreport.id.masterReport.biobjId = " + master_rpt_id.toString();
			
			hql = " from SbiSubreports as subreport " + 
	         "where subreport.id.masterReport.biobjId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0,  master_rpt_id.intValue());
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toSubreport((SbiSubreports) it.next()));
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
		return realResult;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO#loadSubreportsBySubRptId(java.lang.Integer)
	 */
	public List loadSubreportsBySubRptId(Integer sub_rpt_id) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		List realResult = new ArrayList();
		String hql = null;
		Query hqlQuery = null;
		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			//hql = " from SbiSubreports as subreport " + 
	        // "where subreport.id.subReport.biobjId = " + sub_rpt_id.toString();
			
			hql = " from SbiSubreports as subreport " + 
	         "where subreport.id.subReport.biobjId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, sub_rpt_id.intValue());
			List hibList = hqlQuery.list();
			
			Iterator it = hibList.iterator();

			while (it.hasNext()) {
				realResult.add(toSubreport((SbiSubreports) it.next()));
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
		return realResult;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO#insertSubreport(it.eng.spagobi.commons.bo.Subreport)
	 */
	public void insertSubreport(Subreport aSubreport) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		aSession = getSession();
		tx = aSession.beginTransaction();
		SbiSubreportsId hibSubreportid = new SbiSubreportsId();
		SbiObjects masterReport = (SbiObjects) aSession.load(SbiObjects.class, aSubreport.getMaster_rpt_id());
		SbiObjects subReport = (SbiObjects) aSession.load(SbiObjects.class, aSubreport.getSub_rpt_id());
		hibSubreportid.setMasterReport(masterReport);
		hibSubreportid.setSubReport(subReport);
		SbiSubreports hibSubreport = new SbiSubreports(hibSubreportid);
		updateSbiCommonInfo4Insert(hibSubreport);
		aSession.save(hibSubreport);	
		tx.commit();
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO#eraseSubreportByMasterRptId(java.lang.Integer)
	 */
	public void eraseSubreportByMasterRptId(Integer master_rpt_id) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		String hql = null;
		Query hqlQuery = null;
		List subreports = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			//hql = " from SbiSubreports as subreport " + 
	        // "where subreport.id.masterReport.biobjId = " + master_rpt_id.toString();
			
			hql = " from SbiSubreports as subreport " + 
	         "where subreport.id.masterReport.biobjId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0,master_rpt_id.intValue());
			subreports = hqlQuery.list();
			
			Iterator it = subreports.iterator();
			while (it.hasNext()) {
				aSession.delete((SbiSubreports) it.next());
			}			
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		}	finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO#eraseSubreportBySubRptId(java.lang.Integer)
	 */
	public void eraseSubreportBySubRptId(Integer sub_rpt_id) throws EMFUserError {
		Session aSession = null;
		Transaction tx = null;
		String hql = null;
		Query hqlQuery = null;
		List subreports = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			//hql = " from SbiSubreports as subreport " + 
	        // "where subreport.id.subReport.biobjId = " + sub_rpt_id.toString();
			
			hql = " from SbiSubreports as subreport " + 
	         "where subreport.id.subReport.biobjId = ?" ;
			
			hqlQuery = aSession.createQuery(hql);
			hqlQuery.setInteger(0, sub_rpt_id.intValue());
			subreports = hqlQuery.list();
			
			Iterator it = subreports.iterator();
			while (it.hasNext()) {
				aSession.delete((SbiSubreports) it.next());
			}			
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception ex) {
			logException(ex);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100); 
		}	finally {
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}	
	}
	
	/**
	 * From the hibernate subreports at input, gives
	 * the corrispondent <code>Subreports</code> object.
	 * 
	 * @param hibSubreport the hib subreport
	 * 
	 * @return The corrispondent <code>Parameter</code> object
	 */
	public Subreport toSubreport(SbiSubreports hibSubreport){
		Subreport aSubreport = new Subreport();
		aSubreport.setMaster_rpt_id(hibSubreport.getId().getMasterReport().getBiobjId());
		aSubreport.setSub_rpt_id(hibSubreport.getId().getSubReport().getBiobjId());		
		return aSubreport;
	}

}
