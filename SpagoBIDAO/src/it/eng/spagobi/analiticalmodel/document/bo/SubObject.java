/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.bo;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

public class SubObject implements Serializable {
	
	static private Logger logger = Logger.getLogger(SubObject.class);
	
	private Integer id = null;
	private Integer biobjId = null;
	private String name = null;
	private Boolean isPublic = new Boolean(false);
	private String owner = null;
	private String description = null;
	private Date lastChangeDate;
	private Date creationDate;
	private byte[] content;
	private Integer binaryContentId=null;
	
	/**
	 * Gets the binary content id.
	 * 
	 * @return the binary content id
	 */
	public Integer getBinaryContentId() {
	    return binaryContentId;
	}
	
	/**
	 * Sets the binary content id.
	 * 
	 * @param binaryContentId the new binary content id
	 */
	public void setBinaryContentId(Integer binaryContentId) {
	    this.binaryContentId = binaryContentId;
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
	 * Tries to load binary content from database for this SubObject instance, given its binary content identifier,
	 * if content field is null.
	 * 
	 * @return The binary content of this instance; if it is null, it tries to load it from database if binary content identifier
	 * is available
	 * 
	 * @throws EMFUserError if some errors while reading from db occurs
	 * @throws EMFInternalError if some errors while reading from db occurs
	 */
	public byte[] getContent() throws EMFUserError, EMFInternalError {
		if (content == null) {
			if (binaryContentId != null) {
				// reads from database
				try {
					content = DAOFactory.getBinContentDAO().getBinContent(binaryContentId);
				} catch (EMFUserError e) {
					logger.error("Error while recovering content of subobject with id = [" + id + "], " +
							"binary content id = [" + binaryContentId + "], " +
							"name = [" + name + "] of biobject with id = [" + biobjId + "]" + e);
					throw e;
				} catch (EMFInternalError e) {
					logger.error("Error while recovering content of subobject with id = [" + id + "], " +
							"binary content id = [" + binaryContentId + "], " +
							"name = [" + name + "] of biobject with id = [" + biobjId + "]" + e);
					throw e;
				}
			} else {
				logger.warn("Both content field of this istance and binary identifier are null. Cannot load content from database.");
			}
		}
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
	
}
