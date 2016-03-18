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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjNotes;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.metadata.SbiBinContents;

public class ObjNoteDAOHibImpl extends AbstractHibernateDAO implements IObjNoteDAO {

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#getExecutionNotes(java.lang.Integer, java.lang.String)
	 */
	@Override
	public ObjNote getExecutionNotes(Integer biobjId, String execIdentif) throws Exception {
		ObjNote objNote = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId +
			// " and son.execReq = '"+execIdentif+"'";

			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ?" + " and son.execReq = ?";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);

			SbiObjNotes hibObjNote = null;
			List l = query.list();
			if (l != null && !l.isEmpty()) {
				hibObjNote = (SbiObjNotes) l.get(0);
			}
			if (hibObjNote != null) {
				objNote = toObjNote(hibObjNote);
			}
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return objNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#getExecutionNotesByOwner(java.lang.Integer, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjNote getExecutionNotesByOwner(Integer biobjId, String execIdentif, String owner) throws Exception {
		ObjNote objNote = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ?" + " and son.execReq = ? and owner = ? ";
			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);
			query.setString(2, owner);

			SbiObjNotes hibObjNote = (SbiObjNotes) query.uniqueResult();
			if (hibObjNote != null) {
				objNote = toObjNote(hibObjNote);
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return objNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#getListExecutionNotes(java.lang.Integer, java.lang.String)
	 */
	@Override
	public List getListExecutionNotes(Integer biobjId, String execIdentif) throws Exception {
		List lstObjNote = new ArrayList();
		// ObjNote objNote = null;
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId +
			// " and son.execReq = '"+execIdentif+"'";

			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ? and son.execReq = ?";

			Query query = aSession.createQuery(hql);
			query.setInteger(0, biobjId.intValue());
			query.setString(1, execIdentif);

			/*
			 * SbiObjNotes hibObjNote = (SbiObjNotes)query.uniqueResult(); if(hibObjNote!=null) { objNote = toObjNote(hibObjNote); }
			 */
			List result = query.list();
			Iterator it = result.iterator();
			while (it.hasNext()) {
				lstObjNote.add(toObjNote((SbiObjNotes) it.next()));
			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		return lstObjNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#saveExecutionNotes(java.lang.Integer, it.eng.spagobi.analiticalmodel.document.bo.ObjNote)
	 */
	@Override
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
			Integer idBin = (Integer) aSession.save(hibBinContent);
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
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	@Override
	public void saveNote(Integer biobjId, ObjNote objNote, boolean flag) throws Exception {
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
			Integer idBin = (Integer) aSession.save(hibBinContent);
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
			if (flag) {
				hibObjNote.setObjNoteId(objNote.getId());
				aSession.update(hibObjNote);
			} else {
				aSession.save(hibObjNote);

			}
			tx.commit();
		} catch (HibernateException he) {
			logException(he);
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#modifyExecutionNotes(it.eng.spagobi.analiticalmodel.document.bo.ObjNote)
	 */
	@Override
	public void modifyExecutionNotes(ObjNote objNote) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			Date now = new Date();

			SbiObjNotes hibObjNote = (SbiObjNotes) aSession.load(SbiObjNotes.class, objNote.getId());
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
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

	private ObjNote toObjNote(SbiObjNotes hibnotes) {
		ObjNote objNote = new ObjNote();
		objNote.setBinId(hibnotes.getSbiBinContents().getId());
		objNote.setBiobjId(hibnotes.getSbiObject().getBiobjId());
		byte[] content = hibnotes.getSbiBinContents().getContent();
		String notes = new String(content);
		objNote.setNotes((notes == null) ? "" : notes);
		objNote.setContent(hibnotes.getSbiBinContents().getContent());
		objNote.setExecReq(hibnotes.getExecReq());
		objNote.setId(hibnotes.getObjNoteId());
		objNote.setOwner((hibnotes.getOwner() == null) ? "" : hibnotes.getOwner());
		objNote.setIsPublic((hibnotes.getIsPublic() == null) ? true : hibnotes.getIsPublic());
		objNote.setCreationDate(hibnotes.getCreationDate());
		objNote.setLastChangeDate(hibnotes.getLastChangeDate());
		return objNote;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#eraseNotes(java.lang.Integer)
	 */
	@Override
	public void eraseNotes(Integer biobjId) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId;
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ?";
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
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO#eraseNotesByOwner(java.lang.Integer, java.lang.String)
	 */
	@Override
	public void eraseNotesByOwner(Integer biobjId, String execIdentif, String owner) throws Exception {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();
			// String hql = "from SbiObjNotes son where son.sbiObject.biobjId = " + biobjId;
			String hql = "from SbiObjNotes son where son.sbiObject.biobjId = ? and son.execReq = ? and son.owner = ?";

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
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}

	}

}
