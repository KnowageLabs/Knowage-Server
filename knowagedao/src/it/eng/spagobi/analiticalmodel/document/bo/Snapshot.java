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
package it.eng.spagobi.analiticalmodel.document.bo;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

public class Snapshot implements Serializable {
	
	static private Logger logger = Logger.getLogger(Snapshot.class);
	
	private Integer id = null;
	private Integer biobjId = null;
	private String name = null;
	private String description = null;
	private Date dateCreation = null;
	private Integer binId = null;
	private byte[] content = null;
	private String contentType = null;
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
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
	 * Gets the date creation.
	 * 
	 * @return the date creation
	 */
	public Date getDateCreation() {
		return dateCreation;
	}
	
	/**
	 * Sets the date creation.
	 * 
	 * @param dateCreation the new date creation
	 */
	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
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
	 * Tries to load binary content from database for this Snapshot instance, given its binary content identifier,
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
			if (binId != null) {
				// reads from database
				try {
					content = DAOFactory.getBinContentDAO().getBinContent(binId);
				} catch (EMFUserError e) {
					logger.error("Error while recovering content of snapshot with id = [" + id + "], binary content id = [" + binId + "], " +
							"name = [" + name + "] of biobject with id = [" + biobjId + "]" + e);
					throw e;
				} catch (EMFInternalError e) {
					logger.error("Error while recovering content of snapshot with id = [" + id + "], binary content id = [" + binId + "], " +
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
