/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.engines.dossier.metadata.SbiDossierBinaryContentsTemp;
import it.eng.spagobi.engines.dossier.metadata.SbiDossierPartsTemp;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Zerbetto (davide.zerbetto@eng.it)
 * 
 */
public class DossierPartsTempDAOHibImpl extends AbstractHibernateDAO implements IDossierPartsTempDAO {

	static public final String IMAGE = "IMAGE";
	static public final String NOTE = "NOTE";
	
	static private Logger logger = Logger.getLogger(DossierPartsTempDAOHibImpl.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO#getImagesOfDossierPart(java.lang.Integer, int, java.lang.Long)
	 */
	public Map getImagesOfDossierPart(Integer dossierId, int pageNum, Long workflowProcessId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer pageId = new Integer(pageNum);
		Map toReturn = new HashMap();
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "from SbiDossierBinaryContentsTemp binTemp where binTemp.sbiDossierPartsTemp.pageId=" + pageId.toString() +
					" and binTemp.sbiDossierPartsTemp.sbiObject.biobjId=" + dossierId + 
					" and binTemp.sbiDossierPartsTemp.workflowProcessId = " + workflowProcessId + 
					" and binTemp.type='" + IMAGE + "'";*/
			String hql = "from SbiDossierBinaryContentsTemp binTemp where binTemp.sbiDossierPartsTemp.pageId=?"  +
			" and binTemp.sbiDossierPartsTemp.sbiObject.biobjId=?"  + 
			" and binTemp.sbiDossierPartsTemp.workflowProcessId = ?"+ 
			" and binTemp.type=?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0,  pageId.intValue());
			query.setInteger(1, dossierId.intValue());
			query.setInteger(2, workflowProcessId.intValue());
			query.setString(3, IMAGE);
			
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				SbiDossierBinaryContentsTemp hibObjTemp = (SbiDossierBinaryContentsTemp) it.next();
				toReturn.put(hibObjTemp.getName(), hibObjTemp.getBinContent());
			}
			return toReturn;
		} catch (HibernateException he) {
			logger.error("Error while storing image content: ", he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO#storeNote(java.lang.Integer, int, byte[], java.lang.Long)
	 */
	public void storeNote(Integer dossierId, int pageNum, byte[] noteContent, Long workflowProcessId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer pageId = new Integer(pageNum);
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId=" + dossierId + " " +
					"and partTemp.pageId=" + pageId.toString() + " and partTemp.workflowProcessId = " + workflowProcessId;*/
			String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId= ?" +
			" and partTemp.pageId= ? and partTemp.workflowProcessId = ?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			query.setInteger(1, pageId.intValue());
			query.setInteger(2, workflowProcessId.intValue());
			
			SbiDossierPartsTemp hibObjTemp = (SbiDossierPartsTemp) query.uniqueResult();
			if (hibObjTemp == null) {
				hibObjTemp = new SbiDossierPartsTemp();
				SbiObjects objHib = (SbiObjects) aSession.load(SbiObjects.class, dossierId);
				hibObjTemp.setSbiObject(objHib);
				hibObjTemp.setPageId(pageId);
				hibObjTemp.setSbiDossierBinaryContentsTemps(new HashSet());
				hibObjTemp.setWorkflowProcessId(workflowProcessId);
				updateSbiCommonInfo4Insert(hibObjTemp);
				aSession.save(hibObjTemp);
			}
			/*hql = "from SbiDossierBinaryContentsTemp binTemp where binTemp.sbiDossierPartsTemp.pageId=" + pageId.toString() +
				" and binTemp.sbiDossierPartsTemp.sbiObject.biobjId=" + dossierId + 
				" and binTemp.sbiDossierPartsTemp.workflowProcessId = " + workflowProcessId + 
				" and binTemp.type='" + NOTE + "'";*/
			
			hql = "from SbiDossierBinaryContentsTemp binTemp where binTemp.sbiDossierPartsTemp.pageId= ?"+
				" and binTemp.sbiDossierPartsTemp.sbiObject.biobjId= ?"  + 
				" and binTemp.sbiDossierPartsTemp.workflowProcessId = ?"  + 
				" and binTemp.type= ?" ;
			query = aSession.createQuery(hql);
			query.setInteger(0, pageId.intValue());
			query.setInteger(1, dossierId.intValue());
			query.setInteger(2, workflowProcessId.intValue());
			query.setString(3, NOTE);
			
			SbiDossierBinaryContentsTemp temp = (SbiDossierBinaryContentsTemp) query.uniqueResult();
			if (temp != null) {
				// updates note row
				temp.setBinContent(noteContent);
				temp.setCreationDate(new Date());
			} else {
				// creates a new note row
				temp = new SbiDossierBinaryContentsTemp();
				temp.setSbiDossierPartsTemp(hibObjTemp);
				temp.setBinContent(noteContent);
				temp.setCreationDate(new Date());
				temp.setName(NOTE);
				temp.setType(NOTE);
			}
			updateSbiCommonInfo4Insert(temp);
			aSession.save(temp);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while storing image content: ", he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO#storeImage(java.lang.Integer, byte[], java.lang.String, int, java.lang.Long)
	 */
	public void storeImage(Integer dossierId, byte[] image,
			String docLogicalName, int pageNum, Long workflowProcessId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer pageId = new Integer(pageNum);
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId=" + dossierId + " " +
			//		"and partTemp.pageId=" + pageId.toString() + " and partTemp.workflowProcessId = " + workflowProcessId;
			String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId=? " +
				" and partTemp.pageId=?  and partTemp.workflowProcessId = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			query.setInteger(1, pageId.intValue());
			query.setInteger(2, workflowProcessId.intValue());
			
			SbiDossierPartsTemp hibObjTemp = (SbiDossierPartsTemp) query.uniqueResult();
			if (hibObjTemp == null) {
				hibObjTemp = new SbiDossierPartsTemp();
				SbiObjects objHib = (SbiObjects) aSession.load(SbiObjects.class, dossierId);
				hibObjTemp.setSbiObject(objHib);
				hibObjTemp.setPageId(pageId);
				hibObjTemp.setSbiDossierBinaryContentsTemps(new HashSet());
				hibObjTemp.setWorkflowProcessId(workflowProcessId);
				aSession.save(hibObjTemp);
			}
			SbiDossierBinaryContentsTemp temp = new SbiDossierBinaryContentsTemp();
			temp.setSbiDossierPartsTemp(hibObjTemp);
			temp.setBinContent(image);
			temp.setCreationDate(new Date());
			temp.setName(docLogicalName);
			temp.setType(IMAGE);
			updateSbiCommonInfo4Insert(temp);
			aSession.save(temp);
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while storing image content: ", he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO#getNotesOfDossierPart(java.lang.Integer, int, java.lang.Long)
	 */
	public byte[] getNotesOfDossierPart(Integer dossierId, int pageNum, Long workflowProcessId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		Integer pageId = new Integer(pageNum);
		byte[] toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "from SbiDossierBinaryContentsTemp binTemp where binTemp.sbiDossierPartsTemp.pageId=" + pageId.toString() +
					" and binTemp.sbiDossierPartsTemp.sbiObject.biobjId=" + dossierId + 
					" and binTemp.sbiDossierPartsTemp.workflowProcessId = " + workflowProcessId + 
					" and binTemp.type='" + NOTE + "'";*/
			String hql = "from SbiDossierBinaryContentsTemp binTemp where binTemp.sbiDossierPartsTemp.pageId= ?"+
				" and binTemp.sbiDossierPartsTemp.sbiObject.biobjId= ?" + 
				" and binTemp.sbiDossierPartsTemp.workflowProcessId = ?" + 
				" and binTemp.type=? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, pageId.intValue());
			query.setInteger(1, dossierId.intValue());
			query.setInteger(2, workflowProcessId.intValue());
			query.setString(3, NOTE);
			
			SbiDossierBinaryContentsTemp hibObjTemp = (SbiDossierBinaryContentsTemp) query.uniqueResult();
			if (hibObjTemp != null) toReturn = hibObjTemp.getBinContent();
			return toReturn;
		} catch (HibernateException he) {
			logger.error("Error while storing image content: ", he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO#cleanDossierParts(java.lang.Integer, java.lang.Long)
	 */
	public void cleanDossierParts(Integer dossierId, Long workflowProcessId)
			throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId=" + dossierId + " " +
				" and partTemp.workflowProcessId = " + workflowProcessId;*/
			String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId= ? "  +
			" and partTemp.workflowProcessId = ? " ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			query.setInteger(1, workflowProcessId.intValue());
			
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				SbiDossierPartsTemp hibObj = (SbiDossierPartsTemp) it.next();
				aSession.delete(hibObj);
				// the temporary binary contents in table SbiDossierBinaryContentsTemp are deleted because 
				// the foreign key is defined with the ON DELETE CASCADE clause
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing dossier parts: ", he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO#eraseDossierParts(java.lang.Integer)
	 */
	public void eraseDossierParts(Integer dossierId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId=" + dossierId;
			String hql = "from SbiDossierPartsTemp partTemp where partTemp.sbiObject.biobjId=?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				SbiDossierPartsTemp hibObj = (SbiDossierPartsTemp) it.next();
				aSession.delete(hibObj);
				// the temporary binary contents in table SbiDossierBinaryContentsTemp are deleted because 
				// the foreign key is defined with the ON DELETE CASCADE clause
			}
			tx.commit();
		} catch (HibernateException he) {
			logger.error("Error while erasing dossier parts: ", he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
		
	}

}
