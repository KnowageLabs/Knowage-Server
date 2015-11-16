/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
