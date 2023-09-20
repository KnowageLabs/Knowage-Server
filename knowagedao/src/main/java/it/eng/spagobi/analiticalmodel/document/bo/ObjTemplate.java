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

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;

public class ObjTemplate implements Serializable, Cloneable {

	private static Logger logger = Logger.getLogger(ObjTemplate.class);

	Integer id = null;
	Integer biobjId = null;
	Integer binId = null;
	String name = null;
	Integer prog = null;
	Date creationDate = null;
	Boolean active = null;
	@JsonIgnore
	byte[] content = null;
	String dimension = null;
	String creationUser = null;

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
	 * Gets the prog.
	 *
	 * @return the prog
	 */
	public Integer getProg() {
		return prog;
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
	 * Gets the active.
	 *
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Sets the active.
	 *
	 * @param activeP the new active
	 */
	public void setActive(Boolean activeP) {
		this.active = activeP;
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
	 * Tries to load binary content from database for this ObjTemplate instance, given its binary content identifier, if content field is null.
	 *
	 * @return The binary content of this instance; if it is null, it tries to load it from database if binary content identifier is available
	 *
	 * @throws EMFUserError     if some errors while reading from db occurs
	 * @throws EMFInternalError if some errors while reading from db occurs
	 */
	public byte[] getContent() throws HibernateException {
		if (content == null) {
			if (binId != null) {
				// reads from database
				try {
					content = DAOFactory.getBinContentDAO().getBinContent(binId);
				} catch (HibernateException e) {
					logger.error("Error while recovering content of template with id = [" + id + "], binary content id = [" + binId + "], " + "name = [" + name
							+ "] of biobject with id = [" + biobjId + "]" + e);
					throw new HibernateException(e.getLocalizedMessage(), e);
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

	/**
	 * Gets the dimension.
	 *
	 * @return the dimension
	 */
	public String getDimension() {
		return dimension;
	}

	/**
	 * Sets the dimension.
	 *
	 * @param dimension the new dimension
	 */
	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	/**
	 * Gets the creation user.
	 *
	 * @return the creation user
	 */
	public String getCreationUser() {
		return creationUser;
	}

	/**
	 * Sets the creation user.
	 *
	 * @param creationUser the new creation user
	 */
	public void setCreationUser(String creationUser) {
		this.creationUser = creationUser;
	}

	/**
	 * Clone the object.. NOTE: it does not clone the id property
	 */
	@Override
	public final ObjTemplate clone() {
		ObjTemplate clone = new ObjTemplate();
		clone.setBiobjId(biobjId);
		clone.setBinId(binId);
		clone.setName(name);
		clone.setProg(prog);
		clone.setCreationDate(creationDate);
		clone.setActive(active);
		clone.setContent(content);
		clone.setDimension(dimension);
		clone.setCreationUser(creationUser);
		return clone;
	}

}
