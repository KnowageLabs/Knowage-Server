/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.bo;

import java.io.Serializable;
import java.util.Date;

public class ObjNote implements Serializable {

	Integer id = null;
	Integer biobjId = null;
	Integer binId = null;
	String execReq = null;
	Boolean isPublic = new Boolean(false);
	Boolean isDeletable = new Boolean(false);
	String owner = null;
	Date lastChangeDate;
	Date creationDate;
    byte[] content = null;
    String notes = null;
    
    
	/**
	 * Gets the exec req.
	 * 
	 * @return the exec req
	 */
	public String getExecReq() {
		return execReq;
	}
	
	/**
	 * Sets the exec req.
	 * 
	 * @param execReq the new exec req
	 */
	public void setExecReq(String execReq) {
		this.execReq = execReq;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Gets the biobj id.
	 * 
	 * @return the biobj id
	 */
	public Integer getBiobjId() {
		return biobjId;
	}
	
	/**
	 * Sets the biobj id.
	 * 
	 * @param biobjId the new biobj id
	 */
	public void setBiobjId(Integer biobjId) {
		this.biobjId = biobjId;
	}
	
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
	 * @return the isPublic
	 */
	public Boolean getIsPublic() {
		return isPublic;
	}

	/**
	 * @param isPublic the isPublic to set
	 */
	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the lastChangeDate
	 */
	public Date getLastChangeDate() {
		return lastChangeDate;
	}

	/**
	 * @param lastChangeDate the lastChangeDate to set
	 */
	public void setLastChangeDate(Date lastChangeDate) {
		this.lastChangeDate = lastChangeDate;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the isDeletable
	 */
	public Boolean getIsDeletable() {
		return isDeletable;
	}

	/**
	 * @param isDeletable the isDeletable to set
	 */
	public void setIsDeletable(Boolean isDeletable) {
		this.isDeletable = isDeletable;
	}
	
	
}
