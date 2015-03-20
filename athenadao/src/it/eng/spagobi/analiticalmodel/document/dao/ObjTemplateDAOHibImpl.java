/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 21-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.analiticalmodel.document.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjTemplates;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;

public class ObjTemplateDAOHibImpl extends AbstractHibernateDAO implements IObjTemplateDAO {

	static private Logger logger = Logger.getLogger(ObjTemplateDAOHibImpl.class);

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO#loadBIObjectTemplate(java.lang.Integer)
	 */
	public ObjTemplate loadBIObjectTemplate(Integer tempId) throws EMFInternalError {
		ObjTemplate objTemp = new ObjTemplate();
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjTemplates hibObjTemp = (SbiObjTemplates)aSession.load(SbiObjTemplates.class,  tempId);
			objTemp = toObjTemplate(hibObjTemp);
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return objTemp;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO#getBIObjectActiveTemplate(java.lang.Integer)
	 */
	public ObjTemplate getBIObjectActiveTemplate(Integer biobjId) throws EMFInternalError {
		ObjTemplate objTemp = new ObjTemplate();
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjTemplates sot where sot.active=true and sot.sbiObject.biobjId="+biobjId;
			String hql = "from SbiObjTemplates sot where sot.active=true and sot.sbiObject.biobjId=?";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			SbiObjTemplates hibObjTemp = (SbiObjTemplates)query.uniqueResult();
			if(hibObjTemp==null) {
				objTemp = null;
			} else {
				objTemp = toObjTemplate(hibObjTemp);
			}
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return objTemp;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO#getBIObjectActiveTemplate(java.lang.Integer)
	 */
	public ObjTemplate getBIObjectActiveTemplateByLabel(String label) throws EMFInternalError {



		ObjTemplate objTemp = new ObjTemplate();
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hqlObj = "from SbiObjects sot where sot.label=?";
			Query queryObj = aSession.createQuery(hqlObj);
			queryObj.setString(0, label);
			SbiObjects biobj = (SbiObjects)queryObj.uniqueResult();
			Integer biobjId = biobj.getBiobjId();

			//String hql = "from SbiObjTemplates sot where sot.active=true and sot.sbiObject.biobjId="+biobjId;
			String hql = "from SbiObjTemplates sot where sot.active=true and sot.sbiObject.biobjId=?";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			SbiObjTemplates hibObjTemp = (SbiObjTemplates)query.uniqueResult();
			if(hibObjTemp==null) {
				objTemp = null;
			} else {
				objTemp = toObjTemplate(hibObjTemp);
			}
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return objTemp;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO#getBIObjectTemplateList(java.lang.Integer)
	 */
	public List getBIObjectTemplateList(Integer biobjId) throws EMFInternalError {
		List templates = new ArrayList();
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjTemplates sot where sot.sbiObject.biobjId="+biobjId+" order by sot.prog desc";
			String hql = "from SbiObjTemplates sot where sot.sbiObject.biobjId=? order by sot.prog desc";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				templates.add(toObjTemplate((SbiObjTemplates)it.next()));
			}
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return templates;
	}



	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO#getNextProgForTemplate(java.lang.Integer)
	 */
	public Integer getNextProgForTemplate(Integer biobjId) throws EMFInternalError {
		Integer maxProg = null;
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "select max(sot.prog) as maxprog from SbiObjTemplates sot where sot.sbiObject.biobjId="+biobjId;
			String hql = "select max(sot.prog) as maxprog from SbiObjTemplates sot where sot.sbiObject.biobjId=?";
			Query query = aSession.createQuery(hql);

			query.setInteger(0, biobjId.intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				maxProg = (Integer)it.next();
			}
			if(maxProg==null){
				maxProg = new Integer(1);
			} else {
				maxProg = new Integer(maxProg.intValue() + 1);
			}
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}
		return maxProg;
	}



	/**
	 * To obj template.
	 * 
	 * @param hibObjTemp the hib obj temp
	 * 
	 * @return the obj template
	 */
	public ObjTemplate toObjTemplate(SbiObjTemplates hibObjTemp){
		ObjTemplate objTemp = new ObjTemplate();
		objTemp.setActive(hibObjTemp.getActive());
		objTemp.setBinId(hibObjTemp.getSbiBinContents().getId());
		objTemp.setBiobjId(hibObjTemp.getSbiObject().getBiobjId());
		objTemp.setCreationDate(hibObjTemp.getCreationDate());
		objTemp.setId(hibObjTemp.getObjTempId());
		objTemp.setName(hibObjTemp.getName());
		objTemp.setProg(hibObjTemp.getProg());
		// metadata
		objTemp.setDimension(hibObjTemp.getDimension());
		objTemp.setCreationUser(hibObjTemp.getCreationUser());
		return objTemp;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO#deleteBIObjectTemplate(java.lang.Integer)
	 */
	public void deleteBIObjectTemplate(Integer tempId) throws EMFInternalError {
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiObjTemplates hibObjTemp = (SbiObjTemplates)aSession.load(SbiObjTemplates.class,  tempId);
			SbiBinContents hibBinCont = hibObjTemp.getSbiBinContents();
			aSession.delete(hibBinCont);
			aSession.delete(hibObjTemp);
			tx.commit();
		}catch(HibernateException he){
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		}finally{
			if (aSession!=null){
				if (aSession.isOpen()) aSession.close();
			}
		}		
	}


	public void insertBIObjectTemplate(ObjTemplate objTemplate)
	throws EMFUserError, EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;		
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// store the binary content
			SbiBinContents hibBinContent = new SbiBinContents();
			byte[] bytes = objTemplate.getContent();
			hibBinContent.setContent(bytes);
			updateSbiCommonInfo4Insert(hibBinContent);
			Integer idBin = (Integer) aSession.save(hibBinContent);
			// recover the saved binary hibernate object
			hibBinContent = (SbiBinContents) aSession.load(SbiBinContents.class, idBin);
			// set to not active the current active template
			String hql = "update SbiObjTemplates sot set sot.active = false where sot.active = true and sot.sbiObject.biobjId=?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, objTemplate.getBiobjId().intValue());
			try{
				logger.debug ("Updates the current template of object " + objTemplate.getBiobjId() + " with active = false.");
				query.executeUpdate();
			} catch (Exception e) {
				logger.error("Exception",e);
			}
			// get the next prog for the new template
			Integer maxProg = null;
			Integer nextProg = null;

			hql = "select max(sot.prog) as maxprog from SbiObjTemplates sot where sot.sbiObject.biobjId=?";
			query = aSession.createQuery(hql);

			query.setInteger(0, objTemplate.getBiobjId().intValue());
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				maxProg = (Integer)it.next();				
			}
			logger.debug("maxProg readed from SbiObjTemplates with biobjId " + objTemplate.getBiobjId() + " is : " + maxProg);
			if (maxProg == null) {
				nextProg = new Integer(1);
			} else {
				nextProg = new Integer(maxProg.intValue() + 1);
			}
			logger.debug("nextProg used is: " + nextProg);
			
			// store the object template
			SbiObjTemplates hibObjTemplate = new SbiObjTemplates();
			//check if id is already defined. In positive case update template else insert a new one
			if (objTemplate.getId() != null && objTemplate.getId().compareTo(new Integer("-1")) != 0){
				logger.debug("Template yet exists with id: " + objTemplate.getId() + ". Updates it.");
				hibObjTemplate = (SbiObjTemplates)aSession.load(SbiObjTemplates.class, objTemplate.getId());
				hibObjTemplate.setActive(new Boolean(true));
			} else {
				logger.debug("Template doesn't exists. It inserts one.");
				hibObjTemplate.setActive(new Boolean(true));
				hibObjTemplate.setCreationDate(new Date());
				hibObjTemplate.setName(objTemplate.getName());
				hibObjTemplate.setProg(nextProg);
				hibObjTemplate.setSbiBinContents(hibBinContent);
				SbiObjects obj = (SbiObjects) aSession.load(SbiObjects.class, objTemplate.getBiobjId());
				hibObjTemplate.setSbiObject(obj);
				// metadata
				String user = objTemplate.getCreationUser();
				if (user == null || user.equals(""))user = obj.getCreationUser();
				hibObjTemplate.setCreationUser(user);
				hibObjTemplate.setDimension(objTemplate.getDimension());
				updateSbiCommonInfo4Insert(hibObjTemplate);
				aSession.save(hibObjTemplate);
			}
			tx.commit();
		} catch(HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new RuntimeException("Impossible to add template [" + objTemplate.getName() + "] to document [" + objTemplate .getBiobjId() + "]", he);
			//throw new EMFUserError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

}
