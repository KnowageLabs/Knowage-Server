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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class OracleDataBase extends AbstractDataBase implements CacheDataBase, MetaDataBase {

	private static transient Logger logger = Logger.getLogger(OracleDataBase.class);

	private int varcharLength = 255;

	public OracleDataBase(IDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public int getVarcharLength() {
		return varcharLength;
	}

	@Override
	public void setVarcharLength(int varcharLength) {
		this.varcharLength = varcharLength;
	}

	@Override
	public String getDataBaseType(Class javaType) {

		String toReturn = null;
		String javaTypeName = javaType.toString();

		if (javaTypeName.contains("java.lang.String")) {
			toReturn = " VARCHAR (" + getVarcharLength() + " CHAR)";
		} else if (javaTypeName.contains("java.lang.Byte")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Short")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Integer")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Long")) {
			toReturn = " NUMBER ";
		} else if (javaTypeName.contains("java.math.BigDecimal")) {
			toReturn = " NUMBER ";
		} else if (javaTypeName.contains("java.lang.Double")) {
			toReturn = " FLOAT ";
		} else if (javaTypeName.contains("java.lang.Float")) {
			toReturn = " REAL ";
		} else if (javaTypeName.contains("java.lang.Boolean")) {
			toReturn = " SMALLINT ";
		} else if (javaTypeName.contains("java.sql.Date") || javaTypeName.contains("java.util.Date")) {
			toReturn = " DATE ";
		} else if (javaTypeName.toLowerCase().contains("timestamp")) {
			toReturn = " TIMESTAMP ";
		} else if (javaTypeName.contains("java.sql.Time")) {
			toReturn = " TIMESTAMP ";
		} else if (javaTypeName.contains("[B") || javaTypeName.contains("BLOB")) {
			toReturn = " BLOB ";
		} else if (javaTypeName.contains("[C") || javaTypeName.contains("CLOB") || javaTypeName.contains("Map") || javaTypeName.contains("List")) {
			toReturn = " CLOB ";
		} else {
			toReturn = " VARCHAR(4000) ";
			logger.error("Cannot map java type [" + javaTypeName + "] to a valid database type. Set VARCHAR(4000) by default ");
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

		String query = "select " + " sum(bytes) table_size_meg " + "	from " + " user_extents " + " where  " + " Segment_Type='TABLE' "
				+ " and     segment_name like '" + tableNamePrefix.toUpperCase() + "%' ";
		// " group by segment_name ";

		return query;
	}

	@Override
	public String getSchema(Connection conn) throws SQLException {
		return conn.getSchema();
	}

	@Override
	public String getCatalog(Connection conn) throws SQLException {
		return conn.getCatalog();
	}
}
