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

public class SDKDocument implements java.io.Serializable {
	private Integer dataSetId;

	private Integer dataSourceId;

	private String description;

	private Integer engineId;

	private Integer id;

	private String label;

	private String name;

	private String state;

	private String type;

	private String lockedByUser;

	public SDKDocument() {
	}

	public SDKDocument(Integer dataSetId, Integer dataSourceId, String description, Integer engineId, Integer id,
			String label, String name, String state, String type, String lockedByUser) {
		this.dataSetId = dataSetId;
		this.dataSourceId = dataSourceId;
		this.description = description;
		this.engineId = engineId;
		this.id = id;
		this.label = label;
		this.name = name;
		this.state = state;
		this.type = type;
		this.lockedByUser = lockedByUser;
	}

	/**
	 * Gets the dataSetId value for this SDKDocument.
	 *
	 * @return dataSetId
	 */
	public Integer getDataSetId() {
		return dataSetId;
	}

	/**
	 * Sets the dataSetId value for this SDKDocument.
	 *
	 * @param dataSetId
	 */
	public void setDataSetId(Integer dataSetId) {
		this.dataSetId = dataSetId;
	}

	/**
	 * Gets the dataSourceId value for this SDKDocument.
	 *
	 * @return dataSourceId
	 */
	public Integer getDataSourceId() {
		return dataSourceId;
	}

	/**
	 * Sets the dataSourceId value for this SDKDocument.
	 *
	 * @param dataSourceId
	 */
	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	/**
	 * Gets the description value for this SDKDocument.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SDKDocument.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the engineId value for this SDKDocument.
	 *
	 * @return engineId
	 */
	public Integer getEngineId() {
		return engineId;
	}

	/**
	 * Sets the engineId value for this SDKDocument.
	 *
	 * @param engineId
	 */
	public void setEngineId(Integer engineId) {
		this.engineId = engineId;
	}

	/**
	 * Gets the id value for this SDKDocument.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKDocument.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the label value for this SDKDocument.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SDKDocument.
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the name value for this SDKDocument.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKDocument.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the state value for this SDKDocument.
	 *
	 * @return state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state value for this SDKDocument.
	 *
	 * @param state
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Gets the type value for this SDKDocument.
	 *
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SDKDocument.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the lockedByUser value for this SDKDocument.
	 *
	 * @return lockedByUser
	 */
	public String getLockedByUser() {
		return lockedByUser;
	}

	/**
	 * Sets lockedByUser value for this SDKDocument.
	 *
	 * @param lockedByUser
	 */
	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKDocument))
			return false;
		SDKDocument other = (SDKDocument) obj;
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
				&& ((this.dataSetId == null && other.getDataSetId() == null)
						|| (this.dataSetId != null && this.dataSetId.equals(other.getDataSetId())))
				&& ((this.dataSourceId == null && other.getDataSourceId() == null)
						|| (this.dataSourceId != null && this.dataSourceId.equals(other.getDataSourceId())))
				&& ((this.description == null && other.getDescription() == null)
						|| (this.description != null && this.description.equals(other.getDescription())))
				&& ((this.engineId == null && other.getEngineId() == null)
						|| (this.engineId != null && this.engineId.equals(other.getEngineId())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.state == null && other.getState() == null)
						|| (this.state != null && this.state.equals(other.getState())))
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
		if (getDataSetId() != null) {
			_hashCode += getDataSetId().hashCode();
		}
		if (getDataSourceId() != null) {
			_hashCode += getDataSourceId().hashCode();
		}
		if (getDescription() != null) {
			_hashCode += getDescription().hashCode();
		}
		if (getEngineId() != null) {
			_hashCode += getEngineId().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getState() != null) {
			_hashCode += getState().hashCode();
		}
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
