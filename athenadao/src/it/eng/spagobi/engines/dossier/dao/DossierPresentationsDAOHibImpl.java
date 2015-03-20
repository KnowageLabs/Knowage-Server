/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.engines.dossier.bo.DossierPresentation;
import it.eng.spagobi.engines.dossier.metadata.SbiDossierPresentations;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
public class DossierPresentationsDAOHibImpl extends AbstractHibernateDAO implements IDossierPresentationsDAO {

	static private Logger logger = Logger.getLogger(DossierPresentationsDAOHibImpl.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#getPresentationVersionContent(java.lang.Integer, java.lang.Integer)
	 */
	public byte[] getPresentationVersionContent(Integer dossierId,
			Integer versionId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		byte[] toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=" + dossierId + " and sdp.prog=" + versionId;
			String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId= ?  and sdp.prog= ?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			query.setInteger(1, versionId.intValue());
			
			SbiDossierPresentations hibObjTemp = (SbiDossierPresentations) query.uniqueResult();
			if (hibObjTemp == null) {
				return null;
			} else {
				toReturn = hibObjTemp.getSbiBinaryContent().getContent();
				return toReturn;
			}
		} catch (HibernateException he) {
			logException(he);
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
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#getPresentationVersions(java.lang.Integer)
	 */
	public List getPresentationVersions(Integer dossierId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=" + dossierId + " and prog is not null";
			String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId= ? and prog is not null";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			List list = query.list();
			if (list == null) {
				return null;
			} else {
				List toReturn = new ArrayList();
				Iterator it = list.iterator();
				while (it.hasNext()) {
					SbiDossierPresentations presentation = (SbiDossierPresentations) it.next();
					toReturn.add(toDossierPresentation(presentation));
				}
				return toReturn;
			}
		} catch (HibernateException he) {
			logException(he);
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
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#deletePresentationVersion(java.lang.Integer, java.lang.Integer)
	 */
	public void deletePresentationVersion(Integer dossierId, Integer versionId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=" + dossierId + " and sdp.prog=" + versionId;
			String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=? and sdp.prog=?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			query.setInteger(1, versionId.intValue());
			
			SbiDossierPresentations hibObjTemp = (SbiDossierPresentations) query.uniqueResult();
			if (hibObjTemp != null) {
				SbiBinContents hibBinCont = hibObjTemp.getSbiBinaryContent();
				// deletes association first
				aSession.delete(hibObjTemp);
				// deletes binary contest at last
				aSession.delete(hibBinCont);
			} else {
				logger.warn("No presentation found with prog = " + versionId + " for document with id = " + dossierId);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
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
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#insertPresentation(it.eng.spagobi.engines.dossier.bo.DossierPresentation)
	 */
	public void insertPresentation(DossierPresentation dossierPresentation) throws EMFUserError, EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// store the binary content
			SbiBinContents hibBinContent = new SbiBinContents();
			byte[] bytes = null;
			try {
				bytes = dossierPresentation.getContent();
			} catch (EMFInternalError e) {
				logger.error("Could not retrieve content of DossierPresentation object in input.");
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			}
			hibBinContent.setContent(bytes);
			Integer idBin = (Integer)aSession.save(hibBinContent);
			// recover the saved binary hibernate object
			hibBinContent = (SbiBinContents) aSession.load(SbiBinContents.class, idBin);
			// recover the associated biobject
			SbiObjects obj = (SbiObjects) aSession.load(SbiObjects.class, dossierPresentation.getBiobjectId());
			// store the object template
			SbiDossierPresentations hibObj = new SbiDossierPresentations();
			hibObj.setWorkflowProcessId(dossierPresentation.getWorkflowProcessId());
			hibObj.setCreationDate(new Date());
			hibObj.setName(dossierPresentation.getName());
			hibObj.setProg(null);
			hibObj.setSbiBinaryContent(hibBinContent);
			hibObj.setSbiObject(obj);
			hibObj.setApproved(null);
			updateSbiCommonInfo4Insert(hibObj);
			aSession.save(hibObj);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
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
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#updatePresentation(it.eng.spagobi.engines.dossier.bo.DossierPresentation)
	 */
	public void updatePresentation(DossierPresentation dossierPresentation) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			SbiDossierPresentations hibObj = (SbiDossierPresentations) aSession.load(SbiDossierPresentations.class, 
					dossierPresentation.getId());
			hibObj.setProg(dossierPresentation.getProg());
			boolean approved = dossierPresentation.getApproved().booleanValue();
			hibObj.setApproved(approved ? new Short((short) 1) : new Short((short) 0));
			updateSbiCommonInfo4Update(hibObj);
			aSession.save(hibObj);
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
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
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#getNextProg(java.lang.Integer)
	 */
	public Integer getNextProg(Integer dossierId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "select max(sdp.prog) as maxprog from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=" 
				+ dossierId;*/
			String hql = "select max(sdp.prog) as maxprog from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			Integer maxProg = (Integer) query.uniqueResult();
			Integer nextProg = null;
			if (maxProg == null) {
				nextProg = new Integer(1);
			} else {
				nextProg = new Integer(maxProg.intValue() + 1);
			}
			return nextProg;
		} catch (HibernateException he) {
			logException(he);
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
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#getCurrentPresentation(java.lang.Integer, java.lang.Long)
	 */
	public DossierPresentation getCurrentPresentation(Integer dossierId, Long workflowProcessId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		DossierPresentation toReturn = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			/*String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=" + dossierId + " " +
					"and sdp.workflowProcessId=" + workflowProcessId;*/
			String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=? "+
				"and sdp.workflowProcessId=?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			query.setInteger(1, workflowProcessId.intValue());
			
			SbiDossierPresentations hibObjTemp = (SbiDossierPresentations) query.uniqueResult();
			if (hibObjTemp == null) {
				return null;
			} else {
				toReturn = toDossierPresentation(hibObjTemp);
				return toReturn;
			}
		} catch (HibernateException he) {
			logException(he);
			if (tx != null) tx.rollback();	
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");  
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
			logger.debug("OUT");
		}
	}
	
	
	private DossierPresentation toDossierPresentation(SbiDossierPresentations presentation) {
		DossierPresentation toReturn = new DossierPresentation();
		toReturn.setId(presentation.getPresentationId());
		toReturn.setWorkflowProcessId(presentation.getWorkflowProcessId());
		toReturn.setBinId(presentation.getSbiBinaryContent().getId());
		toReturn.setBiobjectId(presentation.getSbiObject().getBiobjId());
		Short approvedFl = presentation.getApproved();
		if (approvedFl == null) {
			toReturn.setApproved(null);
		} else {
			boolean approved = presentation.getApproved().shortValue() == 1;
			toReturn.setApproved(new Boolean(approved));
		}
		toReturn.setCreationDate(presentation.getCreationDate());
		toReturn.setName(presentation.getName());
		toReturn.setProg(presentation.getProg());
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO#deletePresentations(java.lang.Integer)
	 */
	public void deletePresentations(Integer dossierId) throws EMFInternalError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=" + dossierId;
			String hql = "from SbiDossierPresentations sdp where sdp.sbiObject.biobjId=?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, dossierId.intValue());
			List list = query.list();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				SbiDossierPresentations hibObjTemp = (SbiDossierPresentations) it.next();
				SbiBinContents hibBinCont = hibObjTemp.getSbiBinaryContent();
				// deletes association first
				aSession.delete(hibObjTemp);
				// deletes binary contest at last
				aSession.delete(hibBinCont);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
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
