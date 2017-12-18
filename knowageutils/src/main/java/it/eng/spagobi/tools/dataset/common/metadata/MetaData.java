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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class MetaData implements IMetaData, Cloneable {
	
	int idFieldIndex;
	
	List<IFieldMetaData> fieldsMeta;
	Map<String, Object> properties;
	
	// @deprecated this map is used only by deprecated method getFieldIndex. Once this method will be removed
	// remove also this map and all the references to it made within this class
	Map<String, Integer> name2IndexMap;
	Map<String, Integer> alias2IndexMap;

	public MetaData() {
		idFieldIndex = -1;
		name2IndexMap = new HashMap<String, Integer>();
		alias2IndexMap = new HashMap<String, Integer>();
		fieldsMeta = new ArrayList<IFieldMetaData>();
		properties = new HashMap<String, Object>();
	}

	@JsonIgnore
	public int getFieldCount() {
		return fieldsMeta.size();
	}

	@JsonIgnore
	public int getIdFieldIndex() {
		return idFieldIndex;
	}
	
	public void setIdField(int fieldIndex) {
		this.idFieldIndex = fieldIndex;
	}
	
	public int getFieldIndex(String fieldName) {
		Integer columnIndex = null;
		columnIndex = (Integer)alias2IndexMap.get(fieldName.toUpperCase());
		if(columnIndex == null || columnIndex<0){
			columnIndex = (Integer)name2IndexMap.get(fieldName.toUpperCase());
		}
		return columnIndex == null? -1: columnIndex.intValue();
	}
	
	public int getFieldIndex(IFieldMetaData fieldMeta) {
		return fieldsMeta.indexOf(fieldMeta);
	}

	public IFieldMetaData getFieldMeta(int fieldIndex) {
		IFieldMetaData fieldMeta = null;

		fieldMeta = fieldsMeta.get(fieldIndex);

		return fieldMeta;
	}
	
	public List findFieldMeta(String propertyName, Object propertyValue) {
		List results;
		Iterator it;
		
		results = new ArrayList();
		it = fieldsMeta.iterator();
		while(it.hasNext()) {
			IFieldMetaData fieldMeta = (IFieldMetaData)it.next();
			if(fieldMeta.getProperty(propertyName) != null 
					&& fieldMeta.getProperty(propertyName).equals(propertyValue)) {
				results.add(fieldMeta);
			}
		}
		
		return results;
	}
	
	public String getFieldAlias(int fieldIndex) {
		String fieldAlias = null;
		IFieldMetaData fieldMeta;
		
		fieldMeta = getFieldMeta(fieldIndex);		
		if(fieldMeta != null) {
			String alias = fieldMeta.getAlias();
			if(alias!=null && !alias.equals("")){
				fieldAlias = alias;
			}else{
				fieldAlias = fieldMeta.getName();
			}
		}
		
		return fieldAlias;
	}
	
	
	public String getFieldName(int fieldIndex) {
		String fieldName = null;
		IFieldMetaData fieldMeta;
		
		fieldMeta = getFieldMeta(fieldIndex);		
		if(fieldMeta != null) {
				fieldName = fieldMeta.getName();
		}
		return fieldName;
	}
	

	public Class getFieldType(int fieldIndex) {
		Class fieldType = null;
		IFieldMetaData fieldMeta;
		
		fieldMeta = getFieldMeta(fieldIndex);		
		if(fieldMeta != null) {
			fieldType = fieldMeta.getType();
		}
		
		return fieldType;
	}

	
	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public void setProperty(String propertyName, Object proprtyValue) {
		properties.put(propertyName, proprtyValue);
		
	}
	
	public void addFiedMeta(IFieldMetaData fieldMetaData) {
		Integer fieldIndex = new Integer(fieldsMeta.size());
		fieldsMeta.add(fieldMetaData);
		String fieldName = fieldMetaData.getName();
		if(fieldMetaData.getAlias()!=null){
			String fieldAlias = fieldMetaData.getAlias();
			alias2IndexMap.put(fieldAlias.toUpperCase(), fieldIndex);
		}
		name2IndexMap.put(fieldName.toUpperCase(), fieldIndex);
	}

	@Override
	public String toString() {
		return fieldsMeta.toString();
	}

	public void deleteFieldMetaDataAt(int pivotFieldIndex) {
		name2IndexMap.remove( getFieldMeta(pivotFieldIndex) );
		fieldsMeta.remove( pivotFieldIndex );	
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public List getFieldsMeta() {
		return fieldsMeta;
	}

	public void setFieldsMeta(FieldMetadata[] fieldsMeta) {
		this.fieldsMeta = new ArrayList<IFieldMetaData>(fieldsMeta.length);

		for (FieldMetadata fm : fieldsMeta) {
			this.fieldsMeta.add(fm);
		}
	}

	public void changeFieldAlias(int fieldIndex, String newAlias) {
		IFieldMetaData m = this.getFieldMeta(fieldIndex);
		m.setAlias(newAlias);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
//		MetaData toReturn = new MetaData();
//		toReturn.setProperties(this.getProperties());
//		
//		int fields = this.getFieldCount();
//		for (int i = 0; i < fields; i++) {
//			IFieldMetaData fieldMetadata = this.getFieldMeta(i);
//			IFieldMetaData clone = fieldMetadata.clone();
//			toReturn.addFiedMeta(clone);
//		}
		
		return super.clone();
	}


}
