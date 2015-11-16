/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;


/**
 * @author zerbetto (davide.zerbetto@eng.it)
 */

public class SbiDossierPresentations  extends SbiHibernateModel {


    // Fields    

     private Integer presentationId;
     private Long workflowProcessId;
     private SbiObjects sbiObject;
     private SbiBinContents sbiBinaryContent;
     private String name;
     private Integer prog;
     private Date creationDate;
     private Short approved;


    // Constructors

    /**
     * default constructor.
     */
    public SbiDossierPresentations() {
    }

	/**
	 * minimal constructor.
	 * 
	 * @param presentationId the presentation id
	 * @param prog the prog
	 */
    public SbiDossierPresentations(Integer presentationId, Integer prog) {
        this.presentationId = presentationId;
        this.prog = prog;
    }
    
    /**
     * full constructor.
     * 
     * @param presentationId the presentation id
     * @param workflowProcessId the workflow process id
     * @param sbiObject the sbi object
     * @param sbiBinaryContent the sbi binary content
     * @param name the name
     * @param prog the prog
     * @param creationDate the creation date
     * @param approved the approved
     */
    public SbiDossierPresentations(Integer presentationId, Long workflowProcessId, SbiObjects sbiObject, SbiBinContents sbiBinaryContent, String name, Integer prog, Date creationDate, Short approved) {
        this.presentationId = presentationId;
        this.workflowProcessId = workflowProcessId;
        this.sbiObject = sbiObject;
        this.sbiBinaryContent = sbiBinaryContent;
        this.name = name;
        this.prog = prog;
        this.creationDate = creationDate;
        this.approved = approved;
    }
    

   
    // Property accessors

    /**
     * Gets the presentation id.
     * 
     * @return the presentation id
     */
    public Integer getPresentationId() {
        return this.presentationId;
    }
    
    /**
     * Sets the presentation id.
     * 
     * @param presentationId the new presentation id
     */
    public void setPresentationId(Integer presentationId) {
        this.presentationId = presentationId;
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
     * Gets the sbi binary content.
     * 
     * @return the sbi binary content
     */
    public SbiBinContents getSbiBinaryContent() {
        return this.sbiBinaryContent;
    }
    
    /**
     * Sets the sbi binary content.
     * 
     * @param sbiBinaryContent the new sbi binary content
     */
    public void setSbiBinaryContent(SbiBinContents sbiBinaryContent) {
        this.sbiBinaryContent = sbiBinaryContent;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name.
     * 
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the prog.
     * 
     * @return the prog
     */
    public Integer getProg() {
        return this.prog;
    }
    
    /**
     * Sets the prog.
     * 
     * @param prog the new prog
     */
    public void setProg(Integer prog) {
        this.prog = prog;
    }

    /**
     * Gets the creation date.
     * 
     * @return the creation date
     */
    public Date getCreationDate() {
        return this.creationDate;
    }
    
    /**
     * Sets the creation date.
     * 
     * @param creationDate the new creation date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the approved.
     * 
     * @return the approved
     */
    public Short getApproved() {
        return this.approved;
    }
    
    /**
     * Sets the approved.
     * 
     * @param approved the new approved
     */
    public void setApproved(Short approved) {
        this.approved = approved;
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
    
}
