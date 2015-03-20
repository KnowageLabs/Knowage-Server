/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class DossierBinaryContentsTemp implements Serializable{

    private Integer binId;
    private Integer dossierPartsTempId;
    private String name;
    private byte[] content;
    private String type;
    private Date creationDate;
    
	/**
	 * Gets the bin id.
	 * 
	 * @return the bin id
	 */
	public Integer getBinId() {
		return binId;
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
	 * Gets the dossier parts temp id.
	 * 
	 * @return the dossier parts temp id
	 */
	public Integer getDossierPartsTempId() {
		return dossierPartsTempId;
	}
	
	/**
	 * Sets the dossier parts temp id.
	 * 
	 * @param dossierPartsTempId the new dossier parts temp id
	 */
	public void setDossierPartsTempId(Integer dossierPartsTempId) {
		this.dossierPartsTempId = dossierPartsTempId;
	}
	
	/**
	 * Gets the content.
	 * 
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}
	
	/**
	 * Sets the content.
	 * 
	 * @param content the new content
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
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
		return creationDate;
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
