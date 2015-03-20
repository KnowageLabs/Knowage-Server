/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.HashSet;
import java.util.Set;


/**
 * @author zerbetto (davide.zerbetto@eng.it)
 */

public class SbiDossierPartsTemp  extends SbiHibernateModel {


    // Fields    

     private Integer partId;
     private SbiObjects sbiObject;
     private Integer pageId;
     private Long workflowProcessId;
     private Set sbiDossierBinaryContentsTemps = new HashSet(0);


    // Constructors

    /**
     * default constructor.
     */
    public SbiDossierPartsTemp() {
    }

	/**
	 * minimal constructor.
	 * 
	 * @param partId the part id
	 */
    public SbiDossierPartsTemp(Integer partId) {
        this.partId = partId;
    }
    
    /**
     * full constructor.
     * 
     * @param partId the part id
     * @param sbiObject the sbi object
     * @param workflowProcessId the workflow process id
     * @param sbiDossierBinaryContentsTemps the sbi dossier binary contents temps
     */
    public SbiDossierPartsTemp(Integer partId, SbiObjects sbiObject, Long workflowProcessId, Set sbiDossierBinaryContentsTemps) {
        this.partId = partId;
        this.sbiObject = sbiObject;
        this.workflowProcessId = workflowProcessId;
        this.sbiDossierBinaryContentsTemps = sbiDossierBinaryContentsTemps;
    }
    

   
    // Property accessors

    /**
     * Gets the part id.
     * 
     * @return the part id
     */
    public Integer getPartId() {
        return this.partId;
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
     * Gets the sbi object.
     * 
     * @return the sbi object
     */
    public SbiObjects getSbiObject() {
        return this.sbiObject;
    }
    
    /**
     * Sets the sbi object.
     * 
     * @param sbiObject the new sbi object
     */
    public void setSbiObject(SbiObjects sbiObject) {
        this.sbiObject = sbiObject;
    }

    /**
     * Gets the workflow process id.
     * 
     * @return the workflow process id
     */
    public Long getWorkflowProcessId() {
        return this.workflowProcessId;
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
     * Gets the sbi dossier binary contents temps.
     * 
     * @return the sbi dossier binary contents temps
     */
    public Set getSbiDossierBinaryContentsTemps() {
        return this.sbiDossierBinaryContentsTemps;
    }
    
    /**
     * Sets the sbi dossier binary contents temps.
     * 
     * @param sbiDossierBinaryContentsTemps the new sbi dossier binary contents temps
     */
    public void setSbiDossierBinaryContentsTemps(Set sbiDossierBinaryContentsTemps) {
        this.sbiDossierBinaryContentsTemps = sbiDossierBinaryContentsTemps;
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
