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
package it.eng.spagobi.tools.dataset.utils;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public final class OracleClobHashSupport {

	private static final Logger LOGGER = Logger.getLogger(OracleClobHashSupport.class);

	public static final String HASH_COLUMN_PREFIX = "KNH_";
	public static final int HASH_COLUMN_LENGTH = 64;
	public static final String HASH_SOURCE_FIELD_PROPERTY = "oracleClobHashSourceField";

	private OracleClobHashSupport() {
	}

	public static boolean shouldMaterializeHashColumns(IDataSet dataSet) {
		IDataSet targetDataSet = unwrap(dataSet);
		return targetDataSet != null && isOracleDataSource(targetDataSet.getDataSource())
				&& hasClobFields(targetDataSet);
	}

	public static boolean isOracleDataSource(IDataSource dataSource) {
		return dataSource != null && (containsOracle(dataSource.getHibDialectClass())
				|| containsOracle(dataSource.getDialectName())
				|| containsOracle(dataSource.getDriver())
				|| containsOracle(dataSource.getUrlConnection()));
	}

	public static boolean hasClobFields(IDataSet dataSet) {
		if (dataSet == null) {
			return false;
		}
		if (hasClobFields(dataSet.getMetadata())) {
			return true;
		}
		String dsMetadata = dataSet.getDsMetadata();
		return dsMetadata != null && dsMetadata.toUpperCase(Locale.ROOT).contains("CLOB");
	}

	public static boolean hasClobFields(IMetaData metadata) {
		return !getClobFields(metadata).isEmpty();
	}

	public static List<IFieldMetaData> getClobFields(IMetaData metadata) {
		if (metadata == null) {
			return Collections.emptyList();
		}

		List<IFieldMetaData> toReturn = new ArrayList<>();
		for (int i = 0; i < metadata.getFieldCount(); i++) {
			IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);
			if (!isHashField(fieldMetaData) && isClobType(fieldMetaData.getType())) {
				toReturn.add(fieldMetaData);
			}
		}
		return toReturn;
	}

	public static boolean isHashField(IFieldMetaData fieldMetaData) {
		return fieldMetaData != null && fieldMetaData.getProperty(HASH_SOURCE_FIELD_PROPERTY) != null;
	}

	public static boolean isClobType(Class type) {
		return type != null && (Clob.class.isAssignableFrom(type)
				|| type.getName().toLowerCase(Locale.ROOT).contains("clob"));
	}

	public static void enrichDataSetMetadata(IDataSet dataSet) {
		if (dataSet == null || dataSet.getMetadata() == null) {
			return;
		}

		IMetaData metadata = dataSet.getMetadata();
		for (IFieldMetaData clobField : getClobFields(metadata)) {
			String hashColumnName = getHashColumnName(clobField);
			if (metadata.getFieldIndex(hashColumnName) < 0) {
				metadata.addFiedMeta(createHashField(clobField));
			}
		}
	}

	public static FieldMetadata createHashField(IFieldMetaData sourceField) {
		FieldMetadata hashField = new FieldMetadata();
		String hashColumnName = getHashColumnName(sourceField);
		hashField.setName(hashColumnName);
		hashField.setAlias(hashColumnName);
		hashField.setType(String.class);
		hashField.setFieldType(sourceField.getFieldType());
		hashField.setProperty(HASH_SOURCE_FIELD_PROPERTY, sourceField.getName());
		hashField.setProperty("visible", Boolean.FALSE);
		return hashField;
	}

	public static String getHashColumnName(IFieldMetaData sourceField) {
		return getHashColumnName(sourceField.getName());
	}

	public static String getHashColumnName(String sourceFieldName) {
		return HASH_COLUMN_PREFIX + Helper.sha256(sourceFieldName).substring(0, 24).toUpperCase(Locale.ROOT);
	}

	public static String getMaterializedColumnName(IFieldMetaData fieldMetaData) {
		String alias = fieldMetaData.getAlias();
		return StringUtils.isNotEmpty(alias) ? alias : fieldMetaData.getName();
	}

	public static String hashValue(Object value) {
		String stringValue = readValueAsString(value);
		return stringValue == null ? null : Helper.sha256(stringValue);
	}

	public static String readValueAsString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof Clob) {
			try (Reader reader = ((Clob) value).getCharacterStream()) {
				StringBuilder builder = new StringBuilder();
				char[] buffer = new char[4096];
				int read;
				while ((read = reader.read(buffer)) != -1) {
					builder.append(buffer, 0, read);
				}
				return builder.toString();
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Error while converting CLOB value into String", e);
			}
		}
		return String.valueOf(value);
	}

	public static boolean hasAllHashColumns(IDataSource dataSource, UserProfile userProfile, String tableName,
			IMetaData metadata) {
		List<IFieldMetaData> clobFields = getClobFields(metadata);
		if (dataSource == null || StringUtils.isBlank(tableName) || clobFields.isEmpty()) {
			return false;
		}

		Set<String> tableColumns = getTableColumns(dataSource, userProfile, tableName);
		for (IFieldMetaData clobField : clobFields) {
			if (!tableColumns.contains(getHashColumnName(clobField).toUpperCase(Locale.ROOT))) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasAllHashColumns(IDataSource dataSource, String tableName, IMetaData metadata) {
		return hasAllHashColumns(dataSource, null, tableName, metadata);
	}

	private static Set<String> getTableColumns(IDataSource dataSource, UserProfile userProfile, String tableName) {
		Set<String> columns = new HashSet<>();
		try (Connection connection = getConnection(dataSource, userProfile);
				PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE 1 = 0");
				ResultSet resultSet = statement.executeQuery()) {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				columns.add(resultSetMetaData.getColumnName(i).toUpperCase(Locale.ROOT));
			}
		} catch (Exception e) {
			LOGGER.debug("Unable to inspect materialized table " + tableName, e);
			return Collections.emptySet();
		}
		return columns;
	}

	private static Connection getConnection(IDataSource dataSource, UserProfile userProfile) throws Exception {
		if (userProfile != null) {
			Connection connection = dataSource.getConnectionByUserProfile(userProfile);
			if (connection != null) {
				return connection;
			}
		}
		return dataSource.getConnection();
	}

	private static IDataSet unwrap(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			return ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		return dataSet;
	}

	private static boolean containsOracle(String value) {
		return value != null && value.toLowerCase(Locale.ROOT).contains("oracle");
	}
}
