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
package it.eng.spagobi.utilities.database;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class PostgreSQLDataBase extends AbstractDataBase {

	private static transient Logger logger = Logger.getLogger(PostgreSQLDataBase.class);

	public PostgreSQLDataBase(IDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public String getDataBaseType(Class javaType) {
		String toReturn = null;
		String javaTypeName = javaType.toString();
		if (javaTypeName.contains("java.lang.String")) {
			toReturn = " VARCHAR (" + getVarcharLength() + ")";
		} else if (javaTypeName.contains("java.lang.Byte")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Short")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Integer")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Long")) {
			toReturn = " NUMERIC ";
		} else if (javaTypeName.contains("java.lang.BigDecimal") || javaTypeName.contains("java.math.BigDecimal")) {
			toReturn = " NUMERIC ";
		} else if (javaTypeName.contains("java.lang.Double")) {
			toReturn = " NUMERIC ";
		} else if (javaTypeName.contains("java.lang.Float")) {
			toReturn = " NUMERIC ";
		} else if (javaTypeName.contains("java.lang.Boolean")) {
			toReturn = " BOOLEAN ";
		} else if (javaTypeName.contains("java.sql.Time")) {
			toReturn = " TIME ";
		} else if (javaTypeName.contains("java.sql.Date")) {
			toReturn = " DATE ";
		} else if (javaTypeName.toLowerCase().contains("timestamp")) {
			toReturn = " TIMESTAMP ";
		} else if (javaTypeName.contains("[B") || javaTypeName.contains("BLOB")) {
			toReturn = " BYTEA ";
		} else if (javaTypeName.contains("[C") || javaTypeName.contains("CLOB")) {
			toReturn = " TEXT ";
		} else {
			logger.debug("Cannot map java type [" + javaTypeName + "] to a valid database type ");
		}

		return toReturn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.utilities.database.IDataBase#getAliasDelimiter()
	 */
	@Override
	public String getAliasDelimiter() {
		return "\"";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.utilities.database.AbstractDataBase#getUsedMemorySizeQuery(java.lang.String, java.lang.String)
	 */
	@Override
	public String getUsedMemorySizeQuery(String schema, String tableNamePrefix) {
		String query = " SELECT " + " CASE count(*) " + " WHEN 0 THEN 0 "
				+ " ELSE SUM(pg_total_relation_size('\"' || table_schema || '\".\"' || table_name || '\"')) " + " END AS size "
				+ " FROM information_schema.tables " + " WHERE " + " table_name like '" + tableNamePrefix + "%' ";
		if ((schema != null) && (!schema.isEmpty())) {
			query += " AND table_schema = '" + schema + "';";
		} else {
			query += ";";
		}

		// String query = "SELECT " +
		// " sum(pg_total_relation_size('\"' || table_schema || '\".\"' || table_name || '\"')) as size " +
		// " FROM information_schema.tables " +
		// " where table_name like '"+ tableNamePrefix +"%'";
		return query;
	}
}
