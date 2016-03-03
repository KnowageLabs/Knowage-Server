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
package it.eng.spagobi.engines.dossier.dao;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.engines.dossier.bo.DossierPresentation;

import java.util.List;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IDossierPresentationsDAO extends ISpagoBIDao{
	
	/**
	 * Gets the presentation version content.
	 * 
	 * @param dossierId the dossier id
	 * @param versionId the version id
	 * 
	 * @return the presentation version content
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public byte[] getPresentationVersionContent(Integer dossierId, Integer versionId) throws EMFInternalError;
	
	/**
	 * Gets the presentation versions.
	 * 
	 * @param dossierId the dossier id
	 * 
	 * @return the presentation versions
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public List getPresentationVersions(Integer dossierId) throws EMFInternalError;
	
	/**
	 * Delete presentation version.
	 * 
	 * @param dossierId the dossier id
	 * @param versionId the version id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void deletePresentationVersion(Integer dossierId, Integer versionId) throws EMFInternalError;
	
	/**
	 * Delete presentations.
	 * 
	 * @param dossierId the dossier id
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void deletePresentations(Integer dossierId) throws EMFInternalError;
	
	/**
	 * Insert presentation.
	 * 
	 * @param dossierPresentation the dossier presentation
	 * 
	 * @throws EMFUserError the EMF user error
	 * @throws EMFInternalError the EMF internal error
	 */
	public void insertPresentation(DossierPresentation dossierPresentation) throws EMFUserError, EMFInternalError;
	
	/**
	 * Update presentation.
	 * 
	 * @param dossierPresentation the dossier presentation
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public void updatePresentation(DossierPresentation dossierPresentation) throws EMFInternalError;
	
	/**
	 * Gets the current presentation.
	 * 
	 * @param dossierId the dossier id
	 * @param workflowProcessId the workflow process id
	 * 
	 * @return the current presentation
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public DossierPresentation getCurrentPresentation(Integer dossierId, Long workflowProcessId) throws EMFInternalError;
	
	/**
	 * Gets the next prog.
	 * 
	 * @param dossierId the dossier id
	 * 
	 * @return the next prog
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public Integer getNextProg(Integer dossierId) throws EMFInternalError;
	
}
