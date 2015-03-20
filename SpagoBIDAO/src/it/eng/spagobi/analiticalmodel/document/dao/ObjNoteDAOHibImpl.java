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

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjNotes;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ObjNoteDAOHibImpl extends AbstractHibernateDAO implements IObjNoteDAO {

	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#getExecutionNotes(java.lang.Integer, java.lang.String)
	 */
	public ObjNote getExecutionNotes(Integer biobjId, String execIdentif) throws Exception {
		ObjNote objNote = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId + 
			//			 " and son.execReq = '"+execIdentif+"'";
			
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ?"  + 
			 " and son.execReq = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);
			
			SbiObjNotes hibObjNote = null;
			List l = query.list();
			if(l!=null && !l.isEmpty()){
				hibObjNote = (SbiObjNotes)l.get(0);
			}
			if(hibObjNote!=null) {
				objNote = toObjNote(hibObjNote);
			}
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
		return objNote;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#getExecutionNotesByOwner(java.lang.Integer, java.lang.String, java.lang.String)
	 */
	public ObjNote getExecutionNotesByOwner(Integer biobjId, String execIdentif, String owner) throws Exception {
		ObjNote objNote = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
		
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ?"  + 
			 " and son.execReq = ? and owner = ? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);
			query.setString(2, owner);
			
			SbiObjNotes hibObjNote = (SbiObjNotes)query.uniqueResult();
			if(hibObjNote!=null) {
				objNote = toObjNote(hibObjNote);
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
		return objNote;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#getListExecutionNotes(java.lang.Integer, java.lang.String)
	 */
	public List getListExecutionNotes(Integer biobjId, String execIdentif) throws Exception {
		List lstObjNote = new ArrayList();
		//ObjNote objNote = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId + 
			//			 " and son.execReq = '"+execIdentif+"'";
			
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ? and son.execReq = ?";
			
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);
			
			/*SbiObjNotes hibObjNote = (SbiObjNotes)query.uniqueResult();
			if(hibObjNote!=null) {
				objNote = toObjNote(hibObjNote);
			}*/
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()){
				lstObjNote.add(toObjNote((SbiObjNotes)it.next()));
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
		return lstObjNote;
	}

	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#saveExecutionNotes(java.lang.Integer, it.eng.spagobi.analiticalmodel.document.bo.ObjNote)
	 */
	public void saveExecutionNotes(Integer biobjId, ObjNote objNote) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			Date now = new Date();
			String execReq = objNote.getExecReq();
			SbiObjects hibBIObject = (SbiObjects) aSession.load(SbiObjects.class, biobjId);
			SbiBinContents hibBinContent = new SbiBinContents();
			hibBinContent.setContent(objNote.getContent());
			Integer idBin = (Integer)aSession.save(hibBinContent);
			// recover the saved binary hibernate object
			hibBinContent = (SbiBinContents) aSession.load(SbiBinContents.class, idBin);
			// store the object note
			SbiObjNotes hibObjNote = new SbiObjNotes();
			hibObjNote.setExecReq(execReq);
			hibObjNote.setSbiBinContents(hibBinContent);
			hibObjNote.setSbiObject(hibBIObject);
			hibObjNote.setOwner(objNote.getOwner());
			hibObjNote.setIsPublic(objNote.getIsPublic());
			hibObjNote.setCreationDate(now);
			hibObjNote.setLastChangeDate(now);
			updateSbiCommonInfo4Insert(hibObjNote);
			aSession.save(hibObjNote);
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

	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#modifyExecutionNotes(it.eng.spagobi.analiticalmodel.document.bo.ObjNote)
	 */
	public void modifyExecutionNotes(ObjNote objNote) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {			
			aSession = getSession();
			tx = aSession.beginTransaction();
			
			Date now = new Date();
			
			SbiObjNotes hibObjNote = (SbiObjNotes)aSession.load(SbiObjNotes.class, objNote.getId());
			SbiBinContents hibBinCont = hibObjNote.getSbiBinContents();
			hibBinCont.setContent(objNote.getContent());
			hibObjNote.setExecReq(objNote.getExecReq());
			hibObjNote.setIsPublic(objNote.getIsPublic());
			hibObjNote.setLastChangeDate(now);
			updateSbiCommonInfo4Update(hibObjNote);
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

	
	
	private ObjNote toObjNote(SbiObjNotes hibnotes) {
		ObjNote objNote = new ObjNote();
		objNote.setBinId(hibnotes.getSbiBinContents().getId());
		objNote.setBiobjId(hibnotes.getSbiObject().getBiobjId());
		byte[] content = hibnotes.getSbiBinContents().getContent();
		String notes = new String(content);
		objNote.setNotes((notes == null)?"":notes);
		objNote.setContent( hibnotes.getSbiBinContents().getContent());
		objNote.setExecReq(hibnotes.getExecReq());
		objNote.setId(hibnotes.getObjNoteId());
		objNote.setOwner((hibnotes.getOwner()==null)?"":hibnotes.getOwner());
		objNote.setIsPublic((hibnotes.getIsPublic()==null)?true:hibnotes.getIsPublic());
		objNote.setCreationDate(hibnotes.getCreationDate());
		objNote.setLastChangeDate(hibnotes.getLastChangeDate());
		return objNote;
	}



	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#eraseNotes(java.lang.Integer)
	 */
	public void eraseNotes(Integer biobjId) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId;
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ?" ;
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			List notes = query.list();
			Iterator notesIt = notes.iterator();
			while (notesIt.hasNext()) {
				SbiObjNotes note = (SbiObjNotes) notesIt.next();
				SbiBinContents noteBinContent = note.getSbiBinContents();
				aSession.delete(note);
				aSession.delete(noteBinContent);
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
		
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#eraseNotesByOwner(java.lang.Integer, java.lang.String)
	 */
	public void eraseNotesByOwner(Integer biobjId, String execIdentif, String owner) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			//String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId;
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ? and son.execReq = ? and son.owner = ?" ;
		
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);
			query.setString(2, owner);
			
			List notes = query.list();
			Iterator notesIt = notes.iterator();
			while (notesIt.hasNext()) {
				SbiObjNotes note = (SbiObjNotes) notesIt.next();
				SbiBinContents noteBinContent = note.getSbiBinContents();
				aSession.delete(note);
				aSession.delete(noteBinContent);
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
		
	}
	

}
