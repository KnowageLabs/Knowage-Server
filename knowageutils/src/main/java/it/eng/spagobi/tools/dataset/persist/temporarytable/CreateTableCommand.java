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
package it.eng.spagobi.tools.dataset.persist.temporarytable;


import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/** Utility class to crate dataset table creation command
 * 
 * @author gavardi
 *
 */

public class CreateTableCommand {


	public static transient Logger logger = Logger.getLogger(CreateTableCommand.class);

	/**
	 *  
	 */
	private String tableName;
	private String driverName;
	private List<ColumnMeta> columns;

	/**
	 *  Mapping physical column to rea names
	 */
	//Map<String, String> physicalColMapping;
	DataSetTableDescriptor dsTableDescriptor;

	int counter=0;



	public CreateTableCommand(String tableName, String driverName) {
		//super();
		this.driverName = driverName;
		this.tableName = tableName;
		dsTableDescriptor = new DataSetTableDescriptor();
		dsTableDescriptor.setTableName(tableName);
	}


	public void addColumn(IFieldMetaData fieldMeta) {
		logger.debug("IN");
		logger.debug("Adding the column with name = "+fieldMeta.getName()+" with alias = "+fieldMeta.getAlias()+" type="+fieldMeta.getType());
		if (columns == null) columns = new ArrayList<ColumnMeta>();

		Class fieldClass = fieldMeta.getType();
		String name = fieldMeta.getName();
		String alias = fieldMeta.getAlias();

		Map<String, Object> properties = fieldMeta.getProperties();

		//add column deifnition
		ColumnMeta columnMeta = new ColumnMeta(name, fieldClass, properties);
		columns.add(columnMeta);
		logger.debug("OUT");
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}



	public String createSQLQuery(){
		logger.debug("Creating the SQL query for the table "+tableName);
		
		String query ="CREATE TABLE ";
		query+=tableName+ " (";

		INativeDBTypeable dbTypeTranslator = NativeTypeTranslatorFactory.getInstance(driverName);
		
		// run al columns
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			ColumnMeta columnMeta = (ColumnMeta) iterator.next();

			// assign physical name to column
			String physicalName = "COL_"+counter;
			query+=physicalName;

			// type
			//Integer size = columnMeta.getSize();
			String typeJavaName = columnMeta.getType().getName();
			query += dbTypeTranslator.getNativeTypeString(typeJavaName, columnMeta.getProperties());

			// semicolon separator
			if(iterator.hasNext()){
				query+=", ";
			}

			// ad field description
			dsTableDescriptor.addField(columnMeta.getName(), physicalName, columnMeta.getType());
			counter++;
		}

		//query+=");";
		query+=")";

		logger.info("Query is "+query);
		logger.debug("OUT");
		return query;
	}




	public DataSetTableDescriptor getDsTableDescriptor() {
		return dsTableDescriptor;
	}






}
