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

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 */

public class PersistedTableHelper {

	private static transient Logger logger = Logger.getLogger(PersistedTableHelper.class);

	public static void addField(PreparedStatement insertStatement, int fieldIndex, IField field, IFieldMetaData fieldMeta, Map<String, Integer> columnSizes) {
		Object fieldValue = field.getValue();
		String fieldMetaName = fieldMeta.getName();
		String fieldMetaTypeName = fieldMeta.getType().toString();
		FieldType fieldMetaFieldType = fieldMeta.getFieldType();
		try {
			// in case of a measure with String type, convert it into a Double
			if (fieldMetaFieldType.equals(FieldType.MEASURE) && fieldMetaTypeName.contains("String")) {
				try {
					logger.debug("Column type is string but the field is measure: converting it into a double");
					// only for primitive type is necessary to use setNull method if value is null
					if (StringUtilities.isEmpty((String) fieldValue)) {
						insertStatement.setNull(fieldIndex + 1, java.sql.Types.DOUBLE);
					} else {
						try {
							insertStatement.setDouble(fieldIndex + 1, Double.parseDouble(fieldValue.toString()));
						} catch (NumberFormatException e) {
							String toParse = fieldValue.toString();
							if (toParse != null) {
								if (toParse.indexOf(",") != toParse.lastIndexOf(",")) {
									// if the comma is a group separator
									toParse = toParse.replace(",", "");
								} else {
									// use the period instead of the comma
									toParse = toParse.replace(",", ".");
								}
							}
							insertStatement.setDouble(fieldIndex + 1, Double.parseDouble(toParse));
						}
					}
				} catch (Throwable t) {
					throw new RuntimeException("An unexpected error occured while converting to double measure field [" + fieldMetaName + "] whose value is ["
							+ fieldValue + "]", t);
				}
			} else if (fieldMetaTypeName.contains("String")) {
				Integer lenValue = (fieldValue == null) ? new Integer("0") : new Integer(fieldValue.toString().length());
				Integer prevValue = columnSizes.get(fieldMetaName) == null ? new Integer("0") : columnSizes.get(fieldMetaName);
				if (lenValue > prevValue) {
					columnSizes.remove(fieldMetaName);
					columnSizes.put(fieldMetaName, lenValue);
				}
				if (!(fieldValue instanceof String)) {
					logger.debug("An unexpected error occured while extimating field [" + fieldMetaName + "] memory size whose type is equal to ["
							+ fieldMetaTypeName + "]. Field forced to String");
					Object nonStringValue = fieldValue;
					if (nonStringValue != null) {
						insertStatement.setString(fieldIndex + 1, nonStringValue.toString());
					} else {
						insertStatement.setString(fieldIndex + 1, "");
					}
				} else {
					insertStatement.setString(fieldIndex + 1, (String) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Date")) {
				insertStatement.setDate(fieldIndex + 1, (Date) fieldValue);
			} else if (fieldMetaTypeName.contains("Timestamp")) {
				insertStatement.setTimestamp(fieldIndex + 1, (Timestamp) fieldValue);
			} else if (fieldMetaTypeName.contains("Short")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (fieldValue == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					insertStatement.setInt(fieldIndex + 1, ((Short) fieldValue).intValue());
				}
			} else if (fieldMetaTypeName.contains("Integer")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (fieldValue == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					insertStatement.setInt(fieldIndex + 1, (Integer) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Double")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (fieldValue == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.DOUBLE);
				} else {
					insertStatement.setDouble(fieldIndex + 1, (Double) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Float")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (fieldValue == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.FLOAT);
				} else {
					insertStatement.setDouble(fieldIndex + 1, (Float) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Long")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (fieldValue == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BIGINT);
				} else {
					insertStatement.setLong(fieldIndex + 1, (Long) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Boolean")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (fieldValue == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BOOLEAN);
				} else {
					insertStatement.setBoolean(fieldIndex + 1, (Boolean) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("BigDecimal")) {
				insertStatement.setBigDecimal(fieldIndex + 1, (BigDecimal) fieldValue);
			} else if (fieldMetaTypeName.contains("[B")) { // BLOB
				insertStatement.setBytes(fieldIndex + 1, (byte[]) fieldValue);
				// ByteArrayInputStream bis = new
				// ByteArrayInputStream((byte[])field.getValue());
				// toReturn.setBinaryStream(1, bis,
				// ((byte[])field.getValue()).length);
			} else if (fieldMetaTypeName.contains("[C")) { // CLOB
				insertStatement.setBytes(fieldIndex + 1, (byte[]) fieldValue);
				// toReturn.setAsciiStream(i2+1, new
				// ByteArrayInputStream((byte[])field.getValue()),
				// ((byte[])field.getValue()).length);
			} else {
				// toReturn.setString(i2+1, (String)field.getValue());
				logger.debug("Cannot setting the column " + fieldMetaName + " with type " + fieldMetaTypeName);
			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while adding to statement value [" + fieldValue + "] of field [" + fieldMetaName
					+ "] whose type is equal to [" + fieldMetaTypeName + "]", t);
		}
	}
}
