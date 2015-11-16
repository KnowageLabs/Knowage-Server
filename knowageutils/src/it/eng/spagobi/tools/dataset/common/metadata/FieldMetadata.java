/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.metadata;


import java.util.HashMap;
import java.util.Map;

public class FieldMetadata implements IFieldMetaData, Cloneable  {
	
	String name;
	String alias;
	Class type;
	Map properties;
	FieldType fieldType;
	
	public FieldMetadata() {
		super();
		this.properties= new HashMap();
		fieldType = FieldType.ATTRIBUTE;
	}

	public FieldMetadata(String name, Class type) {
		super();
		setName(name);
		setType(type);
		this.properties = new HashMap();
		fieldType = FieldType.ATTRIBUTE;
	}

	private String getId() {
		String id = null;
		String aggregationFunction = (String)properties.get("aggregationFunction");
		
		id = getName();
		if(getAlias()!=null){
			id = getAlias();
		}
		if(aggregationFunction != null && !"NONE".equalsIgnoreCase(aggregationFunction)) {
			id = aggregationFunction + "(" +  id + ")";
		}
		
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		if(alias==null){
			return name;
		}
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public void setProperty(String propertyName, Object propertyValue) {
		properties.put(propertyName, propertyValue);
	}

	public Map getProperties() {
		return properties;
	}
	
	public void setProperties(Map properties) {
		this.properties = properties;
	}
	
	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
	
	@Override
	public String toString() {
		return "FieldMetadata [name=" + name + ", alias=" + alias + ", type="
				+ type + ", properties=" + properties + ", fieldType="
				+ fieldType + "]";
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldMetadata other = (FieldMetadata) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		
		return true;
	}


	public void deleteProperty(String propertyName) {
		Map properties = getProperties();
		properties.remove(propertyName);
	}



	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
