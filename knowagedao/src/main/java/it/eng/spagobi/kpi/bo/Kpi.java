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
package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Kpi implements Serializable {

	private static final long serialVersionUID = -3696035077361936505L;
	/**
	 *
	 */
	private Integer id;
	private Integer version;
	private String name;
	private String author;
	private Date dateCreation;

	/**
	 * If 'true' this is the last version of this Kpi
	 */
	private boolean active;

	/**
	 * If enableVersioning = true will not be allowed to disable it
	 */
	private boolean enableVersioning;

	/**
	 * A json object
	 */
	private String definition;
	/**
	 * A json object
	 */
	private String cardinality;
	/**
	 * A json array
	 */
	private String placeholder;
	/**
	 * domainCd = KPI_KPI_CATEGORY
	 */
	private Domain category;
	private Threshold threshold;

	public Kpi() {
	}

	public Kpi(Integer id, Integer version) {
		this.id = id;
		this.version = version;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition
	 *            the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return the cardinality
	 */
	public String getCardinality() {
		return cardinality;
	}

	/**
	 * @param cardinality
	 *            the cardinality to set
	 */
	public void setCardinality(String cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * @param placeholder
	 *            the placeholder to set
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	/**
	 * @return the category
	 */
	public Domain getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(Domain category) {
		this.category = category;
	}

	/**
	 * @return the threshold
	 */
	public Threshold getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(Threshold threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the dateCreation
	 */
	public Date getDateCreation() {
		return dateCreation;
	}

	/**
	 * @param dateCreation
	 *            the dateCreation to set
	 */

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	/**
	 * @return the enableVersioning
	 */
	public boolean isEnableVersioning() {
		return enableVersioning;
	}

	/**
	 * @param enableVersioning
	 *            the enableVersioning to set
	 */
	public void setEnableVersioning(boolean enableVersioning) {
		this.enableVersioning = enableVersioning;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Kpi other = (Kpi) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	public void writeObject(ObjectOutputStream aOutputStream) {
		  throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}


}
