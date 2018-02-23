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
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class ImpalaDataBase extends AbstractDataBase {

	private static transient Logger logger = Logger.getLogger(ImpalaDataBase.class);

	private static final String ALIAS_DELIMITER = "`";

	private static int MAX_CHARSET_RATIO = 4; // utf8mb4
	private static int MAX_VARCHAR_BYTE_VALUE = 65535;
	private static int MAX_VARCHAR_VALUE = MAX_VARCHAR_BYTE_VALUE / MAX_CHARSET_RATIO;

	public ImpalaDataBase(IDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public String getDataBaseType(Class javaType) {
		String toReturn = null;
		String javaTypeName = javaType.toString();
		if (javaTypeName.contains("java.lang.String") && getVarcharLength() <= MAX_VARCHAR_VALUE) {
			toReturn = " VARCHAR (" + getVarcharLength() + ")";
		} else if (javaTypeName.contains("java.lang.Byte")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Short")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Integer")) {
			toReturn = " INTEGER ";
		} else if (javaTypeName.contains("java.lang.Long")) {
			toReturn = " BIGINT ";
		} else if (javaTypeName.contains("java.lang.BigDecimal") || javaTypeName.contains("java.math.BigDecimal")) {
			toReturn = " DOUBLE ";
		} else if (javaTypeName.contains("java.lang.Double")) {
			toReturn = " DOUBLE ";
		} else if (javaTypeName.contains("java.lang.Float")) {
			toReturn = " FLOAT ";
		} else if (javaTypeName.contains("java.lang.Boolean")) {
			toReturn = " BOOLEAN ";
		} else if (javaTypeName.contains("java.sql.Date") || javaTypeName.contains("java.util.Date") || javaTypeName.toLowerCase().contains("timestamp")
				|| javaTypeName.contains("java.sql.Time")) {
			toReturn = " TIMESTAMP ";
		} else if (javaTypeName.contains("[B") || javaTypeName.contains("BLOB")) {
			throw new SpagoBIRuntimeException("Binary large objects such as BLOB, RAW BINARY, and VARBINARY do not currently have an equivalent in Impala.");
		} else if ((javaTypeName.contains("java.lang.String") && getVarcharLength() > MAX_VARCHAR_VALUE) || javaTypeName.contains("[C")
				|| javaTypeName.contains("CLOB") || javaTypeName.contains("JSON") || javaTypeName.contains("Map") || javaTypeName.contains("List")) {
			toReturn = " STRING ";
		} else {
			toReturn = " STRING ";
			logger.error("Cannot map java type [" + javaTypeName + "] to a valid database type. Set STRING by default ");
		}

		return toReturn;
	}

	@Override
	public String getAliasDelimiter() {
		return ALIAS_DELIMITER;
	}

	@Override
	public String getUsedMemorySizeQuery(String schema, String tableNamePrefix) {
		throw new UnsupportedOperationException(
				"Cannot find this information in Impala using standard query. Need to call @Statistics(Memory) stored procedure.");
	}

}
