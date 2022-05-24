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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 */

public class PersistedTableHelper {

	private static transient Logger logger = Logger.getLogger(PersistedTableHelper.class);

	public static void addField(PreparedStatement insertStatement, int fieldIndex, Object fieldValue, String fieldMetaName, String fieldMetaTypeName,
			boolean isfieldMetaFieldTypeMeasure, Map<String, Integer> columnSizes) {
		try {
			if (fieldValue == null) {
				insertStatement.setObject(fieldIndex + 1, null);
			} else if (isfieldMetaFieldTypeMeasure && fieldMetaTypeName.contains("String")) {
				// in case of a measure with String type, convert it into a Double
				try {
					logger.debug("Column type is string but the field is measure: converting it into a double");
					// only for primitive type is necessary to use setNull method if value is null
					String fieldValueString = (String) fieldValue;
					if (fieldValueString == null || "".equals(fieldValueString.trim())) {
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
					throw new RuntimeException(
							"An unexpected error occured while converting to double measure field [" + fieldMetaName + "] whose value is [" + fieldValue + "]",
							t);
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
					insertStatement.setString(fieldIndex + 1, fieldValue.toString());
				} else {
					insertStatement.setString(fieldIndex + 1, (String) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Date")) {
				if (fieldValue instanceof java.sql.Date) {
					insertStatement.setDate(fieldIndex + 1, (Date) fieldValue);
				} else if (fieldValue instanceof java.util.Date) {
					// JDK 8 version
					/*
					 * Instant instant = date.toInstant(); ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()); LocalDate localDate =
					 * zdt.toLocalDate();
					 */
					DateTime dateTime = new DateTime(fieldValue, DateTimeZone.getDefault());
					java.sql.Date sqlDate = new java.sql.Date(dateTime.getMillis());
					insertStatement.setDate(fieldIndex + 1, sqlDate);
				} else {
					java.util.Date date = new java.util.Date(fieldValue.toString());
					// JDK 8 version
					/*
					 * Instant instant = date.toInstant(); ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()); LocalDate localDate =
					 * zdt.toLocalDate();
					 */
					DateTime dateTime = new DateTime(date, DateTimeZone.getDefault());
					java.sql.Date sqlDate = new java.sql.Date(dateTime.getMillis());
					insertStatement.setDate(fieldIndex + 1, sqlDate);
				}
			} else if (fieldMetaTypeName.toLowerCase().contains("timestamp")) {
				Timestamp timestamp = Timestamp.valueOf(fieldValue.toString());
				DateTime dateTime = new DateTime(timestamp, DateTimeZone.getDefault());
				insertStatement.setTimestamp(fieldIndex + 1, new Timestamp(dateTime.getMillis()));
			} else if (fieldMetaTypeName.contains("Time")) {
				insertStatement.setTime(fieldIndex + 1, (Time) fieldValue);
			} else if (fieldMetaTypeName.contains("Byte")) {
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					// insertStatement.setByte(fieldIndex + 1, (Byte) fieldValue);
					insertStatement.setByte(fieldIndex + 1, Byte.parseByte(fieldValue.toString()));
				}
			} else if (fieldMetaTypeName.contains("Short")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					if (fieldValue instanceof Integer) {
						insertStatement.setInt(fieldIndex + 1, (Integer) fieldValue);
					} else if (fieldValue instanceof Short) {
						insertStatement.setShort(fieldIndex + 1, (Short) fieldValue);
					} else {
						logger.debug("Cannot setting the column " + fieldMetaName + " with type " + fieldMetaTypeName);
					}
				}
			} else if (fieldMetaTypeName.contains("BigInteger")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BIGINT);
				} else {
					insertStatement.setLong(fieldIndex + 1, Long.valueOf(fieldValue.toString()));
				}
			} else if (fieldMetaTypeName.contains("Integer")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					insertStatement.setInt(fieldIndex + 1, (Integer) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("Double")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.DOUBLE);
				} else {

					try {
						insertStatement.setDouble(fieldIndex + 1, (Double) fieldValue);
					} catch (ClassCastException e) {
						logger.debug("the type of field [" + fieldValue + "] is not a double, I'm going to transform it in double ");
						;
						insertStatement.setDouble(fieldIndex + 1, new Double(fieldValue + ""));
					}
				}
			} else if (fieldMetaTypeName.contains("Float")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.FLOAT);
				} else {
					insertStatement.setDouble(fieldIndex + 1, ((Number) fieldValue).floatValue());
				}
			} else if (fieldMetaTypeName.contains("Long")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BIGINT);
				} else {
					insertStatement.setLong(fieldIndex + 1, ((Number) fieldValue).longValue());
				}
			} else if (fieldMetaTypeName.contains("Boolean")) {
				// only for primitive type is necessary to use setNull method if value is null
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BOOLEAN);
				} else {
					insertStatement.setBoolean(fieldIndex + 1, (Boolean) fieldValue);
				}
			} else if (fieldMetaTypeName.contains("BigDecimal")) {
				if (fieldValue == null || fieldValue.toString().isEmpty()) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.DECIMAL);
				} else if (fieldValue instanceof BigDecimal) {
					insertStatement.setBigDecimal(fieldIndex + 1, (BigDecimal) fieldValue);
				} else {
					insertStatement.setBigDecimal(fieldIndex + 1, BigDecimal.valueOf(((Number) fieldValue).longValue()));
				}
			} else if (fieldMetaTypeName.contains("[B")) { // BLOB
				insertStatement.setBytes(fieldIndex + 1, (byte[]) fieldValue);
			} else if (fieldMetaTypeName.contains("BLOB")) {
				if (fieldValue.getClass().toString().contains("oracle.sql.BLOB")) {
					insertStatement.setBytes(fieldIndex + 1, ((oracle.sql.BLOB) fieldValue).getBytes());
				} else {
					logger.debug("Cannot setting the column " + fieldMetaName + " with type " + fieldMetaTypeName);
				}
			} else if (fieldMetaTypeName.contains("[C")) { // CLOB
				insertStatement.setBytes(fieldIndex + 1, (byte[]) fieldValue);
			} else if (fieldMetaTypeName.contains("CLOB")) {
				if (fieldValue.getClass().toString().contains("oracle.sql.CLOB")) {
					StringBuilder sb = new StringBuilder();
					oracle.sql.CLOB clob = (oracle.sql.CLOB) fieldValue;
					long length = clob.length();
					long index = 1;
					while (length > Integer.MAX_VALUE) {
						sb.append(clob.getSubString(index, Integer.MAX_VALUE));
						index += Integer.MAX_VALUE;
						length -= Integer.MAX_VALUE;
					}
					if (length > 0) {
						sb.append(clob.getSubString(index, (int) length));
					}
					insertStatement.setString(fieldIndex + 1, sb.toString());
				} else {
					logger.debug("Cannot setting the column " + fieldMetaName + " with type " + fieldMetaTypeName);
				}
			} else if (fieldMetaTypeName.contains("JSONArray") || fieldMetaTypeName.contains("JSONObject") || fieldMetaTypeName.contains("Map")
					|| fieldMetaTypeName.contains("List")) { // JSONObject and JSONArray
				insertStatement.setString(fieldIndex + 1, fieldValue.toString());
			} else {
				logger.error("Cannot setting the column " + fieldMetaName + " with type " + fieldMetaTypeName);
				insertStatement.setObject(fieldIndex + 1, fieldValue.toString());
			}
		} catch (Throwable t) {
			if (fieldValue == null) {
				logger.error("FieldValue is null", t);
			} else {
				logger.error("FieldValue [" + fieldValue + "] is instance of class [" + fieldValue.getClass().getName() + "]", t);
			}
			throw new RuntimeException("An unexpected error occured while adding to statement value [" + fieldValue + "] of field [" + fieldMetaName
					+ "] whose type is equal to [" + fieldMetaTypeName + "]", t);
		}
	}
}
