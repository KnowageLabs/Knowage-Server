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
package it.eng.spagobi.analiticalmodel.document.metadata;

import java.util.Date;

import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;


public class SbiObjNotes  extends SbiHibernateModel {


	private Integer objNoteId;
     private SbiObjects sbiObject;
     private SbiBinContents sbiBinContents;
     private String execReq;
     private String owner;
     private Boolean isPublic;
     private Date creationDate;
     private Date lastChangeDate;
     
	/**
	 * Gets the obj note id.
	 * 
	 * @return the obj note id
	 */
	public Integer getObjNoteId() {
		return objNoteId;
	}
	
	/**
	 * Sets the obj note id.
	 * 
	 * @param objNoteId the new obj note id
	 */
	public void setObjNoteId(Integer objNoteId) {
		this.objNoteId = objNoteId;
	}
	
	/**
	 * Gets the sbi object.
	 * 
	 * @return the sbi object
	 */
	public SbiObjects getSbiObject() {
		return sbiObject;
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
	 * Gets the sbi bin contents.
	 * 
	 * @return the sbi bin contents
	 */
	public SbiBinContents getSbiBinContents() {
		return sbiBinContents;
	}
	
	/**
	 * Sets the sbi bin contents.
	 * 
	 * @param sbiBinContents the new sbi bin contents
	 */
	public void setSbiBinContents(SbiBinContents sbiBinContents) {
		this.sbiBinContents = sbiBinContents;
	}

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
	
	
}