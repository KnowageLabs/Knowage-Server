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

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Marco Libanori
 */
public class SpannerDataBase extends AbstractDataBase implements CacheDataBase {

	private static transient Logger logger = Logger.getLogger(SpannerDataBase.class);

	private static int MAX_VARCHAR_VALUE = 10485760;

	private int varcharLength = 255;

	public SpannerDataBase(IDataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.utilities.database.IDataBase#getAliasDelimiter()
	 */
	@Override
	public String getAliasDelimiter() {
		return "";
	}

	@Override
	public String getDataBaseType(Class javaType) {
		String toReturn = null;
		String javaTypeName = javaType.toString();
		if (javaTypeName.contains("java.lang.String") && getVarcharLength() <= MAX_VARCHAR_VALUE) {
			toReturn = " STRING (" + getVarcharLength() + ")";
		} else if (javaTypeName.contains("java.lang.Byte")) {
			toReturn = " INT64 ";
		} else if (javaTypeName.contains("java.lang.Short")) {
			toReturn = " INT64 ";
		} else if (javaTypeName.contains("java.lang.Integer")) {
			toReturn = " INT64 ";
		} else if (javaTypeName.contains("java.lang.Long")) {
			toReturn = " INT64 ";
		} else if (javaTypeName.contains("java.lang.BigDecimal") || javaTypeName.contains("java.math.BigDecimal")) {
			toReturn = " FLOAT64 ";
		} else if (javaTypeName.contains("java.lang.Double")) {
			toReturn = " FLOAT64 ";
		} else if (javaTypeName.contains("java.lang.Float")) {
			toReturn = " FLOAT64 ";
		} else if (javaTypeName.contains("java.lang.Boolean")) {
			toReturn = " BOOL ";
		} else if (javaTypeName.contains("java.sql.Date") || javaTypeName.contains("java.util.Date")) {
			toReturn = " DATE ";
		} else if (javaTypeName.toLowerCase().contains("timestamp")) {
			toReturn = " TIMESTAMP ";
		} else if (javaTypeName.contains("java.sql.Time")) {
			toReturn = " TIMESTAMP ";
		} else if (javaTypeName.contains("[B") || javaTypeName.contains("BLOB")) {
			toReturn = " BYTE ";
		} else if ((javaTypeName.contains("java.lang.String") && getVarcharLength() > MAX_VARCHAR_VALUE) || javaTypeName.contains("[C")
				|| javaTypeName.contains("CLOB") || javaTypeName.contains("JSON") || javaTypeName.contains("Map") || javaTypeName.contains("List")) {
			toReturn = " BYTE ";
		} else {
			toReturn = " STRING ";
			logger.error("Cannot map java type [" + javaTypeName + "] to a valid database type. Set STRING by default ");
		}

		return toReturn;
	}

	/**
	 * Not available in Google Spanner.
	 *
	 * @return <code>null</code>
	 */
	@Override
	public String getUsedMemorySizeQuery(String schema, String tableNamePrefix) {
		return null;
	}

	@Override
	public int getVarcharLength() {
		return varcharLength;
	}

	@Override
	public void setVarcharLength(int varcharLength) {
		this.varcharLength = varcharLength;
	}

}
