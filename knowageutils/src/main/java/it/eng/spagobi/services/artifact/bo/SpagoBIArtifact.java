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

package it.eng.spagobi.services.artifact.bo;

public class SpagoBIArtifact implements java.io.Serializable {
	private Integer contentId;

	private String description;

	private Integer id;

	private String name;

	private String type;

	public SpagoBIArtifact() {
	}

	public SpagoBIArtifact(Integer id, String name, String description, String type, Integer contentId) {
		this.contentId = contentId;
		this.description = description;
		this.id = id;
		this.name = name;
		this.type = type;
	}

	/**
	 * Gets the contentId value for this SpagoBIArtifact.
	 *
	 * @return contentId
	 */
	public Integer getContentId() {
		return contentId;
	}

	/**
	 * Sets the contentId value for this SpagoBIArtifact.
	 *
	 * @param contentId
	 */
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	/**
	 * Gets the description value for this SpagoBIArtifact.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SpagoBIArtifact.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the id value for this SpagoBIArtifact.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SpagoBIArtifact.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name value for this SpagoBIArtifact.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SpagoBIArtifact.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the type value for this SpagoBIArtifact.
	 *
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SpagoBIArtifact.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SpagoBIArtifact))
			return false;
		SpagoBIArtifact other = (SpagoBIArtifact) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.contentId == null && other.getContentId() == null)
						|| (this.contentId != null && this.contentId.equals(other.getContentId())))
				&& ((this.description == null && other.getDescription() == null)
						|| (this.description != null && this.description.equals(other.getDescription())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.type == null && other.getType() == null)
						|| (this.type != null && this.type.equals(other.getType())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	@Override
	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getContentId() != null) {
			_hashCode += getContentId().hashCode();
		}
		if (getDescription() != null) {
			_hashCode += getDescription().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
