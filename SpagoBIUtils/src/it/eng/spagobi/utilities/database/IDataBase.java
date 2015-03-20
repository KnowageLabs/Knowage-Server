/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.utilities.database;

import java.math.BigDecimal;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IDataBase {
	/**
	 * @param javaType The java type to map to a database' type
	 * 
	 * @return The database type to use to map the specified java type
	 */
	String getDataBaseType(Class javaType);
	
	/**
	 * 
	 * @return the length used for mapped varchar database type. getDataBaseType(String.class) = DBTYPE(X) where X is
	 * equal to getVarcharLength().
	 */
	int getVarcharLength();

	/**
	 * 
	 * @param varcharLength the length used for varchart database type
	 */
	void setVarcharLength(int varcharLength);
	
	/**
	 * 
	 * @return the alias delimiter
	 */
	String getAliasDelimiter();
	
	
	BigDecimal getUsedMemorySize(String schema, String tablePrefix);
}
