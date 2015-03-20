/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spagobi.analiticalmodel.document.bo.ObjNote;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;



public interface IObjNoteDAO extends ISpagoBIDao{

	/**
	 * Save Notes for a specific execution of the biobject.
	 * 
	 * @param biobjId id of the biobject executed
	 * @param objNote notes to save
	 * 
	 * @throws Exception the exception
	 */
	public void saveExecutionNotes(Integer biobjId, ObjNote objNote) throws Exception;
	
	
	/**
	 * Get Notes for a specific execution of the biobject.
	 * 
	 * @param biobjId id of the biobject executed
	 * @param execIdentif the exec identif
	 * 
	 * @return ObjNote notes saved
	 * 
	 * @throws Exception the exception
	 */
	public ObjNote getExecutionNotes(Integer biobjId, String execIdentif) throws Exception;
	
	/**
	 * Get Notes for a specific execution of the biobject by owner.
	 * 
	 * @param biobjId id of the biobject executed
	 * @param execIdentif the exec identif
	 * @param owner the note's owner
	 * 
	 * @return ObjNote notes saved
	 * 
	 * @throws Exception the exception
	 */
	public ObjNote getExecutionNotesByOwner(Integer biobjId, String execIdentif, String owner) throws Exception;
	
	/**
	 * Get Notes for a specific execution of the biobject.
	 * 
	 * @param biobjId id of the biobject executed
	 * @param execIdentif the exec identif
	 * 
	 * @return ObjNote notes saved
	 * 
	 * @throws Exception the exception
	 */
	public List getListExecutionNotes(Integer biobjId, String execIdentif) throws Exception;
	
	/**
	 * Modify execution notes.
	 * 
	 * @param objNote the obj note
	 * 
	 * @throws Exception the exception
	 */
	public void modifyExecutionNotes(ObjNote objNote) throws Exception;
	
	/**
	 * Deletes all notes associated to the BIObject with the id specified in input.
	 * 
	 * @param biobjId the biobj id
	 * 
	 * @throws Exception the exception
	 */
	public void eraseNotes(Integer biobjId) throws Exception;
	
	/**
	 * Deletes all notes associated to the BIObject with the id and the owner specified in input.
	 * 
	 * @param biobjId the biobj id
	 * @param owner the user owner the note
	 * 
	 * @throws Exception the exception
	 */
	public void eraseNotesByOwner(Integer biobjId, String execIdentif, String owner) throws Exception ;
	
}
