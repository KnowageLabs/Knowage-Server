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
package it.eng.spagobi.tools.dataset.common.metadata;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class FieldMetadata implements IFieldMetaData, Cloneable {

	private String name;
	private String alias;
	private Class type;
	private Map properties;
	private FieldType fieldType;
	private boolean multiValue;
	private int precision;
	private int scale;

	private boolean personal;
	private boolean decrypt;
	private boolean subjectId;

	private String description;

	public FieldMetadata() {
		this.properties = new HashMap();
		fieldType = FieldType.ATTRIBUTE;
	}

	public FieldMetadata(String name, Class type) {
		setName(name);
		setType(type);
		this.properties = new HashMap();
		fieldType = FieldType.ATTRIBUTE;
	}

	private String getId() {
		String id = null;
		String aggregationFunction = (String) properties.get("aggregationFunction");

		id = getName();
		if (getAlias() != null) {
			id = getAlias();
		}
		if (aggregationFunction != null && !"NONE".equalsIgnoreCase(aggregationFunction)) {
			id = aggregationFunction + "(" + id + ")";
		}

		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAlias() {
		if (alias == null) {
			return name;
		}
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public Class getType() {
		return type;
	}

	@Override
	public void setType(Class type) {
		this.type = type;
	}

	@Override
	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public void setProperty(String propertyName, Object propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	@Override
	public Map getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Map properties) {
		this.properties = properties;
	}

	@Override
	public FieldType getFieldType() {
		return fieldType;
	}

	@Override
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldMetadata other = (FieldMetadata) obj;
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}

		return true;
	}

	@Override
	public void deleteProperty(String propertyName) {
		Map properties = getProperties();
		properties.remove(propertyName);
	}

	@Override
	public final FieldMetadata clone() throws CloneNotSupportedException {
		FieldMetadata clone = (FieldMetadata) super.clone();
		clone.setProperties((Map) ((HashMap) properties).clone());
		return clone;
	}

	@Override
	public boolean isMultiValue() {
		return multiValue;
	}

	@Override
	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	@Override
	public void setPrecision(int precision) {
		this.precision = precision;

	}

	@Override
	public void setScale(int scale) {
		this.scale = scale;

	}

	@Override
	public int getPrecision() {
		// TODO Auto-generated method stub
		return precision;
	}

	@Override
	public int getScale() {
		// TODO Auto-generated method stub
		return scale;
	}

	/**
	 * @return the personal
	 */
	@Override
	public boolean isPersonal() {
		return personal;
	}

	/**
	 * @param personal the personal to set
	 */
	@Override
	public void setPersonal(boolean personal) {
		this.personal = personal;
	}

//	/**
//	 * @return the masked
//	 */
//	@Override
//	public boolean isMasked() {
//		return masked;
//	}
//
//	/**
//	 * @param masked the masked to set
//	 */
//	@Override
//	public void setMasked(boolean masked) {
//		this.masked = masked;
//	}

	/**
	 * @return the decrypt
	 */
	@Override
	public boolean isDecrypt() {
		return decrypt;
	}

	/**
	 * @param decrypt the decrypt to set
	 */
	@Override
	public void setDecrypt(boolean decrypt) {
		this.decrypt = decrypt;
	}

	/**
	 * @return the subjectId
	 */
	@Override
	public boolean isSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId the subjectId to set
	 */
	@Override
	public void setSubjectId(boolean subjectId) {
		this.subjectId = subjectId;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

}
