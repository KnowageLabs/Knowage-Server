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

import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;


public class SbiSubObjects  extends SbiHibernateModel {

    private Integer subObjId;
    private SbiObjects sbiObject;
    private SbiBinContents sbiBinContents;
    private String name;
    private String description;
    private String owner;
    private Boolean isPublic;
    private Date creationDate;
    private Date lastChangeDate;
	
    /**
     * Gets the sub obj id.
     * 
     * @return the sub obj id
     */
    public Integer getSubObjId() {
		return subObjId;
	}
	
	/**
	 * Sets the sub obj id.
	 * 
	 * @param subObjId the new sub obj id
	 */
	public void setSubObjId(Integer subObjId) {
		this.subObjId = subObjId;
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
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the owner.
	 * 
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * Sets the owner.
	 * 
	 * @param owner the new owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * Gets the checks if is public.
	 * 
	 * @return the checks if is public
	 */
	public Boolean getIsPublic() {
		return isPublic;
	}
	
	/**
	 * Sets the checks if is public.
	 * 
	 * @param isPublic the new checks if is public
	 */
	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
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
	 * Gets the last change date.
	 * 
	 * @return the last change date
	 */
	public Date getLastChangeDate() {
		return lastChangeDate;
	}
	
	/**
	 * Sets the last change date.
	 * 
	 * @param lastChangeDate the new last change date
	 */
	public void setLastChangeDate(Date lastChangeDate) {
		this.lastChangeDate = lastChangeDate;
	}
	
	
	
}