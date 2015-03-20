/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.dao;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.Map;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IDossierPartsTempDAO extends ISpagoBIDao{
	
	/**
	 * Store image.
	 * 
	 * @param dossierId the dossier id
	 * @param image the image
	 * @param docLogicalName the doc logical name
	 * @param pageNum the page num
	 * @param workflowProcessId the workflow process id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void storeImage(Integer dossierId, byte[] image, String docLogicalName, int pageNum, Long workflowProcessId) throws EMFInternalError;
	
	/**
	 * Gets the images of dossier part.
	 * 
	 * @param dossierId the dossier id
	 * @param pageNum the page num
	 * @param workflowProcessId the workflow process id
	 * 
	 * @return the images of dossier part
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public Map getImagesOfDossierPart(Integer dossierId, int pageNum, Long workflowProcessId) throws EMFInternalError;
	
	/**
	 * Gets the notes of dossier part.
	 * 
	 * @param dossierId the dossier id
	 * @param pageNum the page num
	 * @param workflowProcessId the workflow process id
	 * 
	 * @return the notes of dossier part
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public byte[] getNotesOfDossierPart(Integer dossierId, int pageNum, Long workflowProcessId) throws EMFInternalError;
	
	/**
	 * Store note.
	 * 
	 * @param dossierId the dossier id
	 * @param pageNum the page num
	 * @param noteContent the note content
	 * @param workflowProcessId the workflow process id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void storeNote(Integer dossierId, int pageNum, byte[] noteContent, Long workflowProcessId) throws EMFInternalError;
	
	/**
	 * Erases the dossier temporary parts for the process specified at input.
	 * 
	 * @param dossierId The id of the dossier
	 * @param workflowProcessId The id of the process
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void cleanDossierParts(Integer dossierId, Long workflowProcessId) throws EMFInternalError;
	
	/**
	 * Erases the dossier temporary parts for all the processes that involve the dossier specified at input.
	 * 
	 * @param dossierId The dossier id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void eraseDossierParts(Integer dossierId) throws EMFInternalError;

}
