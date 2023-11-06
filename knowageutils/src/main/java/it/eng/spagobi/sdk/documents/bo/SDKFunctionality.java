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

package it.eng.spagobi.sdk.documents.bo;

public class SDKFunctionality implements java.io.Serializable {
	private String code;

	private it.eng.spagobi.sdk.documents.bo.SDKDocument[] containedDocuments;

	private it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] containedFunctionalities;

	private String description;

	private Integer id;

	private String name;

	private Integer parentId;

	private String path;

	private Integer prog;

	public SDKFunctionality() {
	}

	public SDKFunctionality(String code, it.eng.spagobi.sdk.documents.bo.SDKDocument[] containedDocuments,
			it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] containedFunctionalities, String description, Integer id,
			String name, Integer parentId, String path, Integer prog) {
		this.code = code;
		this.containedDocuments = containedDocuments;
		this.containedFunctionalities = containedFunctionalities;
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.path = path;
		this.prog = prog;
	}

	/**
	 * Gets the code value for this SDKFunctionality.
	 *
	 * @return code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code value for this SDKFunctionality.
	 *
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the containedDocuments value for this SDKFunctionality.
	 *
	 * @return containedDocuments
	 */
	public it.eng.spagobi.sdk.documents.bo.SDKDocument[] getContainedDocuments() {
		return containedDocuments;
	}

	/**
	 * Sets the containedDocuments value for this SDKFunctionality.
	 *
	 * @param containedDocuments
	 */
	public void setContainedDocuments(it.eng.spagobi.sdk.documents.bo.SDKDocument[] containedDocuments) {
		this.containedDocuments = containedDocuments;
	}

	/**
	 * Gets the containedFunctionalities value for this SDKFunctionality.
	 *
	 * @return containedFunctionalities
	 */
	public it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] getContainedFunctionalities() {
		return containedFunctionalities;
	}

	/**
	 * Sets the containedFunctionalities value for this SDKFunctionality.
	 *
	 * @param containedFunctionalities
	 */
	public void setContainedFunctionalities(
			it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] containedFunctionalities) {
		this.containedFunctionalities = containedFunctionalities;
	}

	/**
	 * Gets the description value for this SDKFunctionality.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SDKFunctionality.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the id value for this SDKFunctionality.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKFunctionality.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name value for this SDKFunctionality.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKFunctionality.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the parentId value for this SDKFunctionality.
	 *
	 * @return parentId
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * Sets the parentId value for this SDKFunctionality.
	 *
	 * @param parentId
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	/**
	 * Gets the path value for this SDKFunctionality.
	 *
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path value for this SDKFunctionality.
	 *
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets the prog value for this SDKFunctionality.
	 *
	 * @return prog
	 */
	public Integer getProg() {
		return prog;
	}

	/**
	 * Sets the prog value for this SDKFunctionality.
	 *
	 * @param prog
	 */
	public void setProg(Integer prog) {
		this.prog = prog;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKFunctionality))
			return false;
		SDKFunctionality other = (SDKFunctionality) obj;
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
				&& ((this.code == null && other.getCode() == null)
						|| (this.code != null && this.code.equals(other.getCode())))
				&& ((this.containedDocuments == null && other.getContainedDocuments() == null)
						|| (this.containedDocuments != null
								&& java.util.Arrays.equals(this.containedDocuments, other.getContainedDocuments())))
				&& ((this.containedFunctionalities == null && other.getContainedFunctionalities() == null)
						|| (this.containedFunctionalities != null && java.util.Arrays
								.equals(this.containedFunctionalities, other.getContainedFunctionalities())))
				&& ((this.description == null && other.getDescription() == null)
						|| (this.description != null && this.description.equals(other.getDescription())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.parentId == null && other.getParentId() == null)
						|| (this.parentId != null && this.parentId.equals(other.getParentId())))
				&& ((this.path == null && other.getPath() == null)
						|| (this.path != null && this.path.equals(other.getPath())))
				&& ((this.prog == null && other.getProg() == null)
						|| (this.prog != null && this.prog.equals(other.getProg())));
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
		if (getCode() != null) {
			_hashCode += getCode().hashCode();
		}
		if (getContainedDocuments() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getContainedDocuments()); i++) {
				Object obj = java.lang.reflect.Array.get(getContainedDocuments(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getContainedFunctionalities() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getContainedFunctionalities()); i++) {
				Object obj = java.lang.reflect.Array.get(getContainedFunctionalities(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
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
		if (getParentId() != null) {
			_hashCode += getParentId().hashCode();
		}
		if (getPath() != null) {
			_hashCode += getPath().hashCode();
		}
		if (getProg() != null) {
			_hashCode += getProg().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
