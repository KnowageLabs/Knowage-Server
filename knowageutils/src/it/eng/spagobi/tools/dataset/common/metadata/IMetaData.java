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

import java.util.List;
import java.util.Map;

/**
 * @authors Angelo Bernabei (angelo.bernabei@eng.it)       
 *          Andrea Gioia (andrea.gioia@eng.it)
 *          Davide Zerbetto (davide.zerbetto@eng.it)
 */
public interface IMetaData {
	
	/**
	 * @return Returns the index of identifier field if any. -1 otherwise. 
	 */
	int getIdFieldIndex();
	
	void setIdField(int fieldIndex);
	
	/**
	 * @return Returns the number of fields in this DataStore object. 
	 */
	int getFieldCount();
	
	/**
	 * Get the designated field's index. 
	 * 
	 * @param columnIndex the first column is 0, the second is 1, ... 
	 * 
	 * @return column index 
	 * 
	 * @deprecated use getFieldIndex(IFieldMetaData fieldMeta) instead. This method is ambiguous because
	 * field name is not unique among fields contained in a result set. The same field can be used more then
	 * one time in the select statement of the same query. This is a problem when different aggregation functions
	 * are applied on the different occurrences of the same fields (see SPAGOBI-757)
	 */
	int getFieldIndex(String fieldName);
	
	int getFieldIndex(IFieldMetaData fieldMeta);
	
	/**
	 * Get the designated column's name. 
	 * 
	 * @param columnIndex the first column is 0, the second is 1, ... 
	 * 
	 * @return column name 
	 */
	String getFieldName(int fieldIndex);

	/**
	 * Get the designated column's alias. 
	 * 
	 * @param columnIndex the first column is 0, the second is 1, ... 
	 * 
	 * @return column alias, if alias is null return the name
	 */
	
	String getFieldAlias(int fieldIndex);

	
	/**
	 * Retrieves the designated column's Class type
	 * 
	 * @param columnIndex
	 * 
	 * @return Java class
	 */
	Class getFieldType(int fieldIndex);
	
	void addFiedMeta(IFieldMetaData fieldMetaData);
	
	IFieldMetaData getFieldMeta(int fieldIndex);
	
	List findFieldMeta(String propertyName, Object propertyValue);
	
	Object getProperty(String propertyName);

	void setProperty(String propertyName, Object propertyValue);
	
	Map<String, Object> getProperties();

	void deleteFieldMetaDataAt(int pivotFieldIndex); 
	
	void changeFieldAlias(int fieldIndex, String newAlias);
	
}
