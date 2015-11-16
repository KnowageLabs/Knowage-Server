/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;


/**
 * @author zerbetto (davide.zerbetto@eng.it)
 */

public class SbiDossierBinaryContentsTemp  extends SbiHibernateModel{


    // Fields    

     private Integer binId;
     private SbiDossierPartsTemp sbiDossierPartsTemp;
     private String name;
     private byte[] binContent;
     private String type;
     private Date creationDate;


    // Constructors

    /**
     * default constructor.
     */
    public SbiDossierBinaryContentsTemp() {
    }

	/**
	 * minimal constructor.
	 * 
	 * @param binId the bin id
	 */
    public SbiDossierBinaryContentsTemp(Integer binId) {
        this.binId = binId;
    }
    
    /**
     * full constructor.
     * 
     * @param binId the bin id
     * @param sbiDossierPartsTemp the sbi dossier parts temp
     * @param name the name
     * @param binContent the bin content
     * @param type the type
     * @param creationDate the creation date
     */
    public SbiDossierBinaryContentsTemp(Integer binId, SbiDossierPartsTemp sbiDossierPartsTemp, String name, byte[] binContent, String type, Date creationDate) {
        this.binId = binId;
        this.sbiDossierPartsTemp = sbiDossierPartsTemp;
        this.name = name;
        this.binContent = binContent;
        this.type = type;
        this.creationDate = creationDate;
    }
    

   
    // Property accessors

    /**
     * Gets the bin id.
     * 
     * @return the bin id
     */
    public Integer getBinId() {
        return this.binId;
    }
    
    /**
     * Sets the bin id.
     * 
     * @param binId the new bin id
     */
    public void setBinId(Integer binId) {
        this.binId = binId;
    }

    /**
     * Gets the sbi dossier parts temp.
     * 
     * @return the sbi dossier parts temp
     */
    public SbiDossierPartsTemp getSbiDossierPartsTemp() {
        return this.sbiDossierPartsTemp;
    }
    
    /**
     * Sets the sbi dossier parts temp.
     * 
     * @param sbiDossierPartsTemp the new sbi dossier parts temp
     */
    public void setSbiDossierPartsTemp(SbiDossierPartsTemp sbiDossierPartsTemp) {
        this.sbiDossierPartsTemp = sbiDossierPartsTemp;
    }

    /**
     * Gets the bin content.
     * 
     * @return the bin content
     */
    public byte[] getBinContent() {
        return this.binContent;
    }
    
    /**
     * Sets the bin content.
     * 
     * @param binContent the new bin content
     */
    public void setBinContent(byte[] binContent) {
        this.binContent = binContent;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * Sets the type.
     * 
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
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
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
    
}
