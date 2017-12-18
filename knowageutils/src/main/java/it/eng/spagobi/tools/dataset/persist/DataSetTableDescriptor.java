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
package it.eng.spagobi.tools.dataset.persist;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.exceptions.DataSetNotLoadedYetException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataSetTableDescriptor implements IDataSetTableDescriptor {

	private String tableName = null;
	private Map<String, String> field2ColumnMap = null;
	private Map<String, Class> field2ClassMap = null;
	private Map<String, String> column2fieldMap = null;
	private IDataSource dataSource;
	
	public DataSetTableDescriptor() {
		this.field2ColumnMap = new HashMap<String, String>();
		this.field2ClassMap = new HashMap<String, Class>();
		this.column2fieldMap = new HashMap<String, String>();
	}
	
	public DataSetTableDescriptor(IDataSet dataSet) {
		this();
		IFieldMetaData fieldMetadata;
		String fieldName;
		
		dataSource =  dataSet.getDataSourceForReading();

		IMetaData metaData =  dataSet.getMetadata();
		
		IDataStore dataStore = null;
		try {
			dataStore = dataSet.getDataStore();
		} catch (DataSetNotLoadedYetException e) {
			dataSet.loadData();
			dataStore = dataSet.getDataStore();
		}
		
		if(dataStore!=null && dataStore.getMetaData()!=null){
			metaData =  dataStore.getMetaData();
		}

		if(metaData!=null){
			for(int i=0; i<metaData.getFieldCount(); i++){
				fieldMetadata = metaData.getFieldMeta(i);
				fieldName = fieldMetadata.getAlias();
				if(fieldName==null || fieldName.equals("")){
					fieldName = fieldMetadata.getName();
				}
				field2ColumnMap.put(fieldMetadata.getName(), fieldName);
				column2fieldMap.put(fieldName, fieldMetadata.getName());
				field2ClassMap.put(fieldMetadata.getName(), fieldMetadata.getType());
			}
		}
		tableName = dataSet.getTableNameForReading();
	}
	
	public void addField(String fieldName, String columnName, Class type) {
		this.field2ColumnMap.put(fieldName, columnName);
		this.field2ClassMap.put(fieldName, type);
		this.column2fieldMap.put(columnName, fieldName);
	}

	public String getColumnName(String fieldName) {
		return this.field2ColumnMap.get(fieldName);
	}
	
	public String getFieldName(String columnName) {
		return this.column2fieldMap.get(columnName);
	}
	
	public Class getColumnType(String fieldName) {
		return this.field2ClassMap.get(fieldName);
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public String toString() {
		return "DataSetTableDescriptor [tableName=" + tableName
				+ ", field2ColumnMap=" + field2ColumnMap + ", field2ClassMap="
				+ field2ClassMap + "]";
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor#getColumnNames()
	 */
	public Set<String> getColumnNames() {
		Set<String> columnNames = null;
		if(column2fieldMap!=null){
			columnNames = column2fieldMap.keySet();
		}
		return columnNames;
	}

}
