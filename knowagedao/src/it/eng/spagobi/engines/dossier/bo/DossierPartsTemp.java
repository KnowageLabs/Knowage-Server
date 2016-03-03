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
package it.eng.spagobi.engines.dossier.bo;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class DossierPartsTemp implements Serializable{

    private Integer partId;
    private Integer dossierId;
    private Integer pageId;
    private Long workflowProcessId;
    private List dossierBinaryContentsTemps;
    
	/**
	 * Gets the part id.
	 * 
	 * @return the part id
	 */
	public Integer getPartId() {
		return partId;
	}
	
	/**
	 * Sets the part id.
	 * 
	 * @param partId the new part id
	 */
	public void setPartId(Integer partId) {
		this.partId = partId;
	}
	
	/**
	 * Gets the dossier id.
	 * 
	 * @return the dossier id
	 */
	public Integer getDossierId() {
		return dossierId;
	}
	
	/**
	 * Sets the dossier id.
	 * 
	 * @param dossierId the new dossier id
	 */
	public void setDossierId(Integer dossierId) {
		this.dossierId = dossierId;
	}
	
	/**
	 * Gets the workflow process id.
	 * 
	 * @return the workflow process id
	 */
	public Long getWorkflowProcessId() {
		return workflowProcessId;
	}
	
	/**
	 * Sets the workflow process id.
	 * 
	 * @param workflowProcessId the new workflow process id
	 */
	public void setWorkflowProcessId(Long workflowProcessId) {
		this.workflowProcessId = workflowProcessId;
	}
	
	/**
	 * Gets the dossier binary contents temps.
	 * 
	 * @return the dossier binary contents temps
	 */
	public List getDossierBinaryContentsTemps() {
		return dossierBinaryContentsTemps;
	}
	
	/**
	 * Sets the dossier binary contents temps.
	 * 
	 * @param dossierBinaryContentsTemps the new dossier binary contents temps
	 */
	public void setDossierBinaryContentsTemps(List dossierBinaryContentsTemps) {
		this.dossierBinaryContentsTemps = dossierBinaryContentsTemps;
	}
	
	/**
	 * Gets the page id.
	 * 
	 * @return the page id
	 */
	public Integer getPageId() {
		return pageId;
	}
	
	/**
	 * Sets the page id.
	 * 
	 * @param pageId the new page id
	 */
	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}
    
}
