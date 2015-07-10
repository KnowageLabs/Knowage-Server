/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.persist;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.database.DataBase;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Functions that manage the persistence of the dataset
 *
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */

public class PersistedTableManager {

	private String dialect = new String();
	private String tableName = new String();
	private boolean rowCountColumIncluded = false;
	private Map<String, Integer> columnSize = new HashMap<String, Integer>();

	private IEngUserProfile profile = null;

	private static transient Logger logger = Logger.getLogger(PersistedTableManager.class);
	public static final String DIALECT_MYSQL = "MySQL";
	public static final String DIALECT_POSTGRES = "PostgreSQL";
	public static final String DIALECT_ORACLE = "OracleDialect";
	public static final String DIALECT_HSQL = "HSQL";
	public static final String DIALECT_HSQL_PRED = "Predefined hibernate dialect";
	public static final String DIALECT_ORACLE9i10g = "Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "SQLServer";
	public static final String DIALECT_DB2 = "DB2";
	public static final String DIALECT_INGRES = "Ingres";
	public static final String DIALECT_TERADATA = "Teradata";
	public static final String DIALECT_VOLTDB = "VoltDBDialect";

	static final String Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public PersistedTableManager() {

	}

	public PersistedTableManager(IEngUserProfile profile) {
		this.profile = profile;
	}

	public void persistDataSet(IDataSet dataset) throws Exception {
		String tableName = dataset.getTableNameForReading();
		// changed

		// get data source for writing not only getDataSource
		IDataSource dsPersist = dataset.getDataSourceForWriting();
		if (dsPersist == null) {
			logger.error("No data source for writing found: check the datasource associated to dataset " + dataset.getLabel()
					+ " is read and write or there is one and only one datasource marked as write default");
			throw new SpagoBIServiceException("", "sbi.ds.noDataSourceForWriting");
		}
		persistDataSet(dataset, dsPersist, tableName);
	}

	public void persistDataSet(IDataSet dataset, IDataSource dsPersist, String tableName) throws Exception {
		logger.debug("IN");

		// get persisted table name
		this.setTableName(tableName);
		logger.debug("Persisted table name is [" + getTableName() + "]");
		// set dialect of db
		this.setDialect(dsPersist.getHibDialectClass());
		logger.debug("DataSource target dialect is [" + getDialect() + "]");
		// for the first version not all target dialect are enable
		if (getDialect().contains(DIALECT_SQLSERVER) || getDialect().contains(DIALECT_DB2) || getDialect().contains(DIALECT_INGRES)
				|| getDialect().contains(DIALECT_TERADATA)) {
			logger.error("Persistence management not implemented for dialect " + getDialect() + ".");
			throw new SpagoBIRuntimeException("Persistence management not implemented for dialect " + getDialect() + ".");
		}
		String signature = dataset.getSignature();
		logger.debug("Dataset signature : " + signature);
		if (signature != null && signature.equals(TemporaryTableManager.getLastDataSetSignature(tableName))) {
			// signature matches: no need to create a Persistent Table
			logger.debug("Signature matches: no need to create a Persistent Table");
			return;
		}

		dataset.setPersisted(false);
		dataset.loadData();
		IDataStore datastore = dataset.getDataStore();
		persistDataset(datastore, dsPersist);
	}

	public void persistDataset(IDataSet dataSet, IDataStore datastore, IDataSource datasource, String tableName) throws Exception {
		this.setTableName(tableName);
		this.setDialect(datasource.getHibDialectClass());

		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		if (dataSet instanceof FileDataSet || dataSet instanceof CkanDataSet) {
			datastore = normalizeFileDataSet(dataSet, datastore);
		}

		logger.debug("DataSource target dialect is [" + getDialect() + "]");
		// for the first version not all target dialect are enable
		if (getDialect().contains(DIALECT_DB2) || getDialect().contains(DIALECT_INGRES) || getDialect().contains(DIALECT_TERADATA)) {
			logger.debug("Persistence management isn't implemented for " + getDialect() + ".");
			throw new SpagoBIServiceException("", "sbi.ds.dsCannotPersistDialect");
		}
		persistDataset(datastore, datasource);
	}

	private IDataStore normalizeFileDataSet(IDataSet dataSet, IDataStore datastore) {
		if (dataSet instanceof FileDataSet || dataSet instanceof CkanDataSet) {
			// Change dataStore fields type according to the metadata specified
			// on the DataSet metadata
			// because FileDataSet has all dataStore field set as String by
			// default
			IMetaData dataStoreMetaData = datastore.getMetaData();
			IMetaData dataSetMetaData = dataSet.getMetadata();

			int filedNo = dataStoreMetaData.getFieldCount();
			for (int i = 0, l = filedNo; i < l; i++) {
				// Apply DataSet FieldType to DataStore FieldType
				IFieldMetaData dataStoreFieldMetaData = dataStoreMetaData.getFieldMeta(i);
				dataStoreFieldMetaData.setFieldType(dataSetMetaData.getFieldMeta(i).getFieldType());

			}

			// Change Object Type of field records according to metadata's field
			// type
			for (int i = 0; i < Integer.parseInt(String.valueOf(datastore.getRecordsCount())); i++) {
				IRecord rec = datastore.getRecordAt(i);
				for (int j = 0; j < rec.getFields().size(); j++) {
					IFieldMetaData fmd = dataStoreMetaData.getFieldMeta(j);
					IField field = rec.getFieldAt(j);
					// change content type
					if (fmd.getType().toString().contains("Integer")) {
						try {
							Integer intValue = Integer.valueOf((String) field.getValue());
							field.setValue(intValue);
						} catch (Throwable t) {
							logger.error("Error trying to convert value [" + field.getValue() + "] into an Integer value. Considering it as null...");
							field.setValue(null);
						}
					} else if (fmd.getType().toString().contains("Double")) {
						try {
							Double doubleValue = Double.valueOf((String) field.getValue());
							field.setValue(doubleValue);
						} catch (Throwable t) {
							logger.error("Error trying to convert value [" + field.getValue() + "] into a Double value. Considering it as null...");
							field.setValue(null);
						}
					}

				}
			}
		}
		return datastore;
	}

	public void persistDataset(IDataStore datastore, IDataSource datasource) throws Exception {
		logger.debug("IN");
		Connection connection = null;
		String dialect = datasource.getHibDialectClass();
		try {
			connection = getConnection(datasource);

			// VoltDB does not allow explicit commit/rollback actions.
			// It uses an internal transaction committing mechanism.
			// More tests to see the consistency has to be done on this.
			if (!dialect.contains("VoltDB")) {
				connection.setAutoCommit(false);
			}
			// Steps #1: define prepared statement (and max column size for
			// strings type)
			PreparedStatement statement = defineStatements(datastore, datasource, connection);
			// Steps #2: define create table statement
			String createStmtQuery = getCreateTableQuery(datastore, datasource);
			dropTableIfExists(datasource);
			// Step #3: execute create table statament
			executeStatement(createStmtQuery, datasource);
			// Step #4: execute batch with insert statements
			statement.executeBatch();
			statement.close();
			if (!dialect.contains("VoltDB")) {
				connection.commit();
			}
			logger.debug("Insertion of records on persistable table executed successfully!");
		} catch (Exception e) {
			logger.error("Error persisting the dataset into table", e);
			if (connection != null && !dialect.contains("VoltDB")) {
				connection.rollback();
			}
			throw new SpagoBIEngineRuntimeException("Error persisting the dataset into table", e);
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			logger.debug("OUT");
		}
		logger.debug("OUT");
	}

	private PreparedStatement defineStatements(IDataStore datastore, IDataSource datasource, Connection connection) {
		PreparedStatement toReturn;

		IMetaData storeMeta = datastore.getMetaData();
		int fieldCount = storeMeta.getFieldCount();

		String insertQuery = "insert into " + getTableName();
		String values = " values ";
		// createQuery used only for HSQL at this time
		String createQuery = "create table " + getTableName() + " (";

		insertQuery += " (";
		values += " (";
		String separator = "";

		if (this.isRowCountColumIncluded()) {
			IDataBase dataBase = DataBase.getDataBase(datasource);
			insertQuery += separator + AbstractJDBCDataset.encapsulateColumnName(getRowCountColumnName(), datasource);
			createQuery += separator + AbstractJDBCDataset.encapsulateColumnName(getRowCountColumnName(), datasource) + dataBase.getDataBaseType(Long.class);
			values += separator + "?";
			separator = ",";
		}

		for (int i = 0; i < fieldCount; i++) {
			IFieldMetaData fieldMeta = storeMeta.getFieldMeta(i);
			String columnName = getSQLColumnName(fieldMeta);
			String escapedColumnName = AbstractJDBCDataset.encapsulateColumnName(columnName, datasource);

			insertQuery += separator + escapedColumnName;
			values += separator + "?";
			createQuery += separator + escapedColumnName + getDBFieldType(datasource, fieldMeta);
			separator = ",";
		}
		values += ") ";
		createQuery += ") ";
		insertQuery += ") ";

		String totalQuery = insertQuery + values;
		logger.debug("create table statement: " + createQuery);
		try {
			if (getDialect().contains(DIALECT_HSQL) || getDialect().contains(DIALECT_HSQL_PRED)) {
				// WORKAROUND for HQL : it needs the physical table for define a
				// prepareStatement.
				// So, drop and create an empty target table
				dropTableIfExists(datasource);
				// creates temporary table
				executeStatement(createQuery, datasource);
			}

			toReturn = connection.prepareStatement(totalQuery);

			logger.debug("Prepared statement for persist dataset as : " + totalQuery);

			for (int i = 0; i < datastore.getRecordsCount(); i++) {

				if (this.isRowCountColumIncluded()) {
					toReturn.setLong(1, i + 1);
				}

				IRecord record = datastore.getRecordAt(i);
				for (int j = 0; j < record.getFields().size(); j++) {
					try {
						IFieldMetaData fieldMeta = storeMeta.getFieldMeta(j);
						IField field = record.getFieldAt(j);
						if (this.isRowCountColumIncluded()) {
							addField(toReturn, j + 1, field, fieldMeta);
						} else {
							addField(toReturn, j, field, fieldMeta);
						}

					} catch (Throwable t) {
						throw new RuntimeException("An unexpecetd error occured while preparing insert statemenet for record [" + i + "]", t);
					}
				}
				toReturn.addBatch();
			}
		} catch (Exception e) {
			logger.error("Error persisting the dataset into table", e);
			throw new SpagoBIEngineRuntimeException("Error persisting the dataset into table", e);
		}
		return toReturn;
	}

	private void addField(PreparedStatement insertStatement, int fieldIndex, IField field, IFieldMetaData fieldMeta) {
		try {
			// in case of a measure with String type, convert it into a Double
			if (fieldMeta.getFieldType().equals(FieldType.MEASURE) && fieldMeta.getType().toString().contains("String")) {
				try {
					logger.debug("Column type is string but the field is measure: converting it into a double");
					// only for primitive type is necessary to use setNull
					// method if value is null
					if (StringUtilities.isEmpty((String) field.getValue())) {
						insertStatement.setNull(fieldIndex + 1, java.sql.Types.DOUBLE);
					} else {
						try {
							insertStatement.setDouble(fieldIndex + 1, Double.parseDouble(field.getValue().toString()));
						} catch (NumberFormatException e) {
							String toParse = field.getValue().toString();
							if (toParse != null) {
								if (toParse.indexOf(",") != toParse.lastIndexOf(",")) {// if
																						// the
																						// comma
																						// is
																						// a
																						// group
																						// separator
									toParse = toParse.replace(",", "");
								} else {
									toParse = toParse.replace(",", ".");// use the .
									// instead of
									// the comma
								}

							}

							insertStatement.setDouble(fieldIndex + 1, Double.parseDouble(toParse));
						}

					}
				} catch (Throwable t) {
					throw new RuntimeException("An unexpected error occured while converting to double measure field [" + fieldMeta.getName()
							+ "] whose value is [" + field.getValue() + "]", t);
				}
			} else if (fieldMeta.getType().toString().contains("String")) {
				Integer lenValue = (field.getValue() == null) ? new Integer("0") : new Integer(field.getValue().toString().length());
				Integer prevValue = getColumnSize().get(fieldMeta.getName()) == null ? new Integer("0") : getColumnSize().get(fieldMeta.getName());
				if (lenValue > prevValue) {
					getColumnSize().remove(fieldMeta.getName());
					getColumnSize().put(fieldMeta.getName(), lenValue);
				}
				insertStatement.setString(fieldIndex + 1, (String) field.getValue());
			} else if (fieldMeta.getType().toString().contains("Date")) {
				insertStatement.setDate(fieldIndex + 1, (Date) field.getValue());
			} else if (fieldMeta.getType().toString().contains("Timestamp")) {
				insertStatement.setTimestamp(fieldIndex + 1, (Timestamp) field.getValue());
			} else if (fieldMeta.getType().toString().contains("Short")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (field.getValue() == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					insertStatement.setInt(fieldIndex + 1, ((Short) field.getValue()).intValue());
				}
			} else if (fieldMeta.getType().toString().contains("Integer")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (field.getValue() == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.INTEGER);
				} else {
					insertStatement.setInt(fieldIndex + 1, (Integer) field.getValue());
				}
			} else if (fieldMeta.getType().toString().contains("Double")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (field.getValue() == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.DOUBLE);
				} else {
					insertStatement.setDouble(fieldIndex + 1, (Double) field.getValue());
				}

			} else if (fieldMeta.getType().toString().contains("Float")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (field.getValue() == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.FLOAT);
				} else {
					insertStatement.setDouble(fieldIndex + 1, (Float) field.getValue());
				}
			} else if (fieldMeta.getType().toString().contains("Long")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (field.getValue() == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BIGINT);
				} else {
					insertStatement.setLong(fieldIndex + 1, (Long) field.getValue());
				}
			} else if (fieldMeta.getType().toString().contains("Boolean")) {
				// only for primitive type is necessary to use setNull method if
				// value is null
				if (field.getValue() == null) {
					insertStatement.setNull(fieldIndex + 1, java.sql.Types.BOOLEAN);
				} else {
					insertStatement.setBoolean(fieldIndex + 1, (Boolean) field.getValue());
				}

			} else if (fieldMeta.getType().toString().contains("BigDecimal")) {
				insertStatement.setBigDecimal(fieldIndex + 1, (BigDecimal) field.getValue());
			} else if (fieldMeta.getType().toString().contains("[B")) { // BLOB
				insertStatement.setBytes(fieldIndex + 1, (byte[]) field.getValue());
				// ByteArrayInputStream bis = new
				// ByteArrayInputStream((byte[])field.getValue());
				// toReturn.setBinaryStream(1, bis,
				// ((byte[])field.getValue()).length);
			} else if (fieldMeta.getType().toString().contains("[C")) { // CLOB
				insertStatement.setBytes(fieldIndex + 1, (byte[]) field.getValue());
				// toReturn.setAsciiStream(i2+1, new
				// ByteArrayInputStream((byte[])field.getValue()),
				// ((byte[])field.getValue()).length);
			} else {
				// toReturn.setString(i2+1, (String)field.getValue());
				logger.debug("Cannot setting the column " + fieldMeta.getName() + " with type " + fieldMeta.getType().toString());
			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while adding to statement value [" + field.getValue() + "] of field ["
					+ fieldMeta.getName() + "] whose type is equal to [" + fieldMeta.getType().toString() + "]", t);
		}
	}

	private String getSQLColumnName(IFieldMetaData fmd) {
		String columnName = fmd.getAlias() != null ? fmd.getAlias() : fmd.getName();
		logger.debug("Column name is " + columnName);
		return columnName;
	}

	private String getDBFieldType(IDataSource dataSource, IFieldMetaData fieldMetaData) {
		IDataBase dataBase = DataBase.getDataBase(dataSource);
		if (getColumnSize().get(fieldMetaData.getName()) != null) {
			dataBase.setVarcharLength(getColumnSize().get(fieldMetaData.getName()));
		}
		Class type = fieldMetaData.getType();
		if (fieldMetaData.getFieldType().equals(FieldType.MEASURE) && type == String.class) {
			logger.debug("Column type is string but the field is measure: converting it into a double");
			type = Double.class;
		}

		return dataBase.getDataBaseType(type);

		// return getDBFieldType(fieldMetaData);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	private String getDBFieldType(IFieldMetaData fieldMetaData) {
		String toReturn = "";
		String type = fieldMetaData.getType().toString();
		logger.debug("Column type input: " + type);
		if (fieldMetaData.getFieldType().equals(FieldType.MEASURE) && type.contains("java.lang.String")) {
			logger.debug("Column type is string but the field is measure: converting it into a double");
			type = "java.lang.Double";
		}

		if (type.contains("java.lang.String")) {
			String charLbl = "";
			toReturn = " VARCHAR ";
			if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " VARCHAR2 ";
				charLbl = " CHAR";
			}
			if (getColumnSize().get(fieldMetaData.getName()) == null) {
				toReturn += " (4000)"; // maxvalue for default
			} else {
				toReturn += " (" + getColumnSize().get(fieldMetaData.getName()) + charLbl + " )";
			}
		} else if (type.contains("java.lang.Short")) {
			toReturn = " INTEGER ";
		} else if (type.contains("java.lang.Integer")) {
			toReturn = " INTEGER ";
		} else if (type.contains("java.lang.Long")) {
			toReturn = " NUMERIC ";
			if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " NUMBER ";
			} else if (getDialect().contains(DIALECT_MYSQL)) {
				toReturn = " BIGINT ";
			}
		} else if (type.contains("java.lang.BigDecimal") || type.contains("java.math.BigDecimal")) {
			toReturn = " NUMERIC ";
			if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " NUMBER ";
			} else if (getDialect().contains(DIALECT_MYSQL)) {
				toReturn = " FLOAT ";
			}
		} else if (type.contains("java.lang.Double")) {
			toReturn = " DOUBLE ";
			if (getDialect().contains(DIALECT_POSTGRES) || getDialect().contains(DIALECT_SQLSERVER) || getDialect().contains(DIALECT_TERADATA)) {
				toReturn = " NUMERIC ";
			} else if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " NUMBER ";
			}
		} else if (type.contains("java.lang.Float")) {
			toReturn = " DOUBLE ";
			if (getDialect().contains(DIALECT_POSTGRES) || getDialect().contains(DIALECT_SQLSERVER) || getDialect().contains(DIALECT_TERADATA)) {
				toReturn = " NUMERIC ";
			} else if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " NUMBER ";
			}
		} else if (type.contains("java.lang.Boolean")) {
			toReturn = " BOOLEAN ";
			if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g) || getDialect().contains(DIALECT_TERADATA)
					|| getDialect().contains(DIALECT_DB2)) {
				toReturn = " SMALLINT ";
			} else if (getDialect().contains(DIALECT_SQLSERVER)) {
				toReturn = " BIT ";
			}
		} else if (type.contains("java.sql.Date")) {
			toReturn = " DATE ";
			if (getDialect().contains(DIALECT_SQLSERVER)) {
				toReturn = " DATETIME ";
			}
		} else if (type.contains("java.sql.Timestamp")) {
			toReturn = " TIMESTAMP ";
			if (getDialect().contains(DIALECT_SQLSERVER)) {
				toReturn = " DATETIME ";
			}
		} else if (type.contains("[B")) {
			toReturn = " TEXT ";
			if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " BLOB ";
			} else if (getDialect().contains(DIALECT_MYSQL)) {
				toReturn = " MEDIUMBLOB ";
			} else if (getDialect().contains(DIALECT_POSTGRES)) {
				toReturn = " BYTEA ";
			} else if (getDialect().contains(DIALECT_HSQL)) {
				toReturn = " LONGVARBINARY ";
			}
		} else if (type.contains("[C")) {
			toReturn = " TEXT ";
			if (getDialect().contains(DIALECT_ORACLE) || getDialect().contains(DIALECT_ORACLE9i10g)) {
				toReturn = " CLOB ";
			}
		} else {
			logger.debug("Cannot mapping the column type " + type);
		}
		logger.debug("Column type output: " + toReturn);
		return toReturn;
	}

	private String getCreateTableQuery(IDataStore datastore, IDataSource dataSource) {
		String toReturn = "create table " + tableName + " (";
		IMetaData md = datastore.getMetaData();

		if (this.isRowCountColumIncluded()) {
			IDataBase dataBase = DataBase.getDataBase(dataSource);
			toReturn += " " + AbstractJDBCDataset.encapsulateColumnName(this.getRowCountColumnName(), dataSource) + " " + dataBase.getDataBaseType(Long.class)
					+ " , ";
		}

		for (int i = 0, l = md.getFieldCount(); i < l; i++) {
			IFieldMetaData fmd = md.getFieldMeta(i);
			String columnName = getSQLColumnName(fmd);
			toReturn += " " + AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource) + getDBFieldType(dataSource, fmd);
			toReturn += ((i < l - 1) ? " , " : "");
		}
		toReturn += " )";

		return toReturn;
	}

	private Connection getConnection(IDataSource datasource) {
		try {
			Boolean multiSchema = datasource.getMultiSchema();
			logger.debug("Datasource is multischema: " + multiSchema);
			String schema;
			if (multiSchema == null || !multiSchema.booleanValue()) {
				schema = null;
			} else {
				String attributeName = datasource.getSchemaAttribute();
				logger.debug("Datasource multischema attribute name: " + attributeName);

				logger.debug("Looking for attribute " + attributeName + " for user " + profile + " ...");
				Object attributeValue = profile.getUserAttribute(attributeName);
				logger.debug("Attribute " + attributeName + "  is " + attributeValue);
				if (attributeValue == null) {
					throw new RuntimeException("No attribute with name " + attributeName + " found for user " + profile.getUserUniqueIdentifier());
				} else {
					schema = attributeValue.toString();
				}
			}
			return datasource.getConnection(schema);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Cannot get connection to datasource", e);
		}
	}

	private void executeStatement(String sql, IDataSource dataSource) throws Exception {
		logger.debug("IN");
		Connection connection = null;
		String dialect = dataSource.getHibDialectClass();
		try {
			// connection = dataSource.getConnection();
			connection = getConnection(dataSource);
			if (!dialect.contains("VoltDB")) {
				connection.setAutoCommit(false);
			}
			Statement stmt = connection.createStatement();
			logger.debug("Executing sql " + sql);
			stmt.execute(sql);
			if (!dialect.contains("VoltDB")) {
				connection.commit();
			}
			logger.debug("Sql " + sql + " executed successfully");
		} catch (Exception e) {
			if (connection != null && !dialect.contains("VoltDB")) {
				connection.rollback();
			}
			throw e;
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			logger.debug("OUT");
		}
	}

	private void executeBatch(List queryInsert, IDataSource datasource) throws Exception {
		logger.debug("IN");
		Connection connection = null;
		String dialect = datasource.getHibDialectClass();
		try {
			// connection = datasource.getConnection();
			connection = getConnection(datasource);
			if (!dialect.contains("VoltDB")) {
				connection.setAutoCommit(false);
			}
			Statement statement = connection.createStatement();
			for (int i = 0, l = queryInsert.size(); i < l; i++) {
				statement.addBatch(queryInsert.get(i).toString());
			}
			statement.executeBatch();
			statement.close();
			if (!dialect.contains("VoltDB")) {
				connection.commit();
			}
			logger.debug("Insertion of records on persistable table executed successfully!");
		} catch (Exception e) {
			if (connection != null && !dialect.contains("VoltDB")) {
				connection.rollback();
			}
			throw e;
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			logger.debug("OUT");
		}
	}

	private void dropTableIfExists(IDataSource datasource) {
		// drop the persisted table if one exists
		try {
			logger.debug("Signature does not match: dropping PersistedTable " + getTableName() + " if it exists...");
			TemporaryTableManager.dropTableIfExists(getTableName(), datasource);
		} catch (Exception e) {
			logger.error("Impossible to drop the temporary table with name " + getTableName(), e);
			throw new SpagoBIEngineRuntimeException("Impossible to drop the persisted table with name " + tableName, e);
		}
	}

	public void dropTableIfExists(IDataSource datasource, String tableName) {
		// drop the persisted table if one exists
		try {
			logger.debug("Dropping PersistedTable " + tableName + " if it exists...");
			TemporaryTableManager.dropTableIfExists(tableName, datasource);
		} catch (Exception e) {
			logger.error("Impossible to drop the table with name " + tableName, e);
			throw new SpagoBIEngineRuntimeException("Impossible to drop the persisted table with name " + tableName, e);
		}
	}

	public void dropTablesWithPrefix(IDataSource datasource, String prefix) {
		logger.debug("Dropping Tables with name prefix " + prefix + " if they exists...");

		String dialect = datasource.getHibDialectClass();

		String statement = null;

		// get the list of tables names
		if (dialect.contains(DIALECT_ORACLE) || dialect.contains(DIALECT_ORACLE9i10g)) {
			statement = "SELECT TABLE_NAME " + "FROM USER_TABLES " + "WHERE TABLE_NAME LIKE '" + prefix.toUpperCase() + "%'";
		} else if (dialect.contains(DIALECT_SQLSERVER) || (dialect.contains(DIALECT_MYSQL) || dialect.contains(DIALECT_POSTGRES))) {
			statement = "SELECT TABLE_NAME " + "FROM INFORMATION_SCHEMA.TABLES " + "WHERE TABLE_NAME LIKE '" + prefix.toLowerCase() + "%'";
		} else if (dialect.contains(DIALECT_HSQL) || dialect.contains(DIALECT_HSQL_PRED)) {
			statement = "SELECT TABLE_NAME " + "FROM INFORMATION_SCHEMA.SYSTEM_TABLES  " + "WHERE TABLE_TYPE = 'TABLE' AND TABLE_NAME LIKE '"
					+ prefix.toUpperCase() + "%'";
		}

		if ((statement != null) && (!statement.isEmpty())) {
			IDataStore dataStore = datasource.executeStatement(statement, 0, 0);
			int dataStoreRecordsCount = Integer.parseInt(String.valueOf(dataStore.getRecordsCount()));

			if (dataStoreRecordsCount > 0) {
				// iterate the dataStore for each table name found then delete
				// it
				for (int i = 0; i < dataStoreRecordsCount; i++) {
					IRecord rec = dataStore.getRecordAt(i);
					for (int j = 0; j < rec.getFields().size(); j++) {
						IField field = rec.getFieldAt(j);
						Object fieldValue = field.getValue();
						if (fieldValue instanceof String) {
							String tableName = (String) fieldValue;
							// delete table
							dropTableIfExists(datasource, tableName);
						}
					}
				}
			}

		}
		logger.debug("Dropped Tables with name prefix " + prefix);

	}

	/**
	 * Create a random unique name for a creating a new table
	 *
	 * @param prefix
	 *            an optional prefix to use for the generated table name
	 */
	public String generateRandomTableName(String prefix) {
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		String generatedId = uuidObj.toString();
		generatedId = generatedId.replaceAll("-", "");
		generatedId = StringUtils.convertNonAscii(generatedId);
		if ((prefix != null) && (!prefix.isEmpty())) {
			generatedId = prefix + generatedId;
		}
		if (generatedId.length() > 30) {
			generatedId = generatedId.substring(0, 30);
		}

		// If the generatedId begins with a non-letter character replace it with
		// a random letter (for ORACLE)
		if (!Character.isLetter(generatedId.charAt(0))) {
			String randomLetter = randomAlphabetString(1);
			generatedId = generatedId.substring(1);
			generatedId = randomLetter + generatedId;
		}
		generatedId = generatedId.toLowerCase();

		return generatedId;
	}

	private String randomAlphabetString(int len) {
		Random random = new Random();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(Alphabet.charAt(random.nextInt(Alphabet.length())));
		}
		return sb.toString();
	}

	// ====================================================================================
	// ACCESSOR MRTHODS
	// ====================================================================================

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, Integer> getColumnSize() {
		return this.columnSize;
	}

	public void setColumnSize(Map<String, Integer> columnSize) {
		this.columnSize = columnSize;
	}

	public IEngUserProfile getProfile() {
		return profile;
	}

	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public boolean isRowCountColumIncluded() {
		return rowCountColumIncluded;
	}

	public void setRowCountColumIncluded(boolean rowCountColumIncluded) {
		this.rowCountColumIncluded = rowCountColumIncluded;
	}

	public String getRowCountColumnName() {
		return "sbicache_row_id";
	}

}
