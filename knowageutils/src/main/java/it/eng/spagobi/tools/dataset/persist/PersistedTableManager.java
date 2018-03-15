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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
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
import it.eng.spagobi.utilities.database.CacheDataBase;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * Functions that manage the persistence of the dataset
 *
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */

public class PersistedTableManager implements IPersistedManager {

	private static final int BATCH_SIZE = 1000;

	private String dialect = new String();
	private String tableName = new String();
	private boolean rowCountColumIncluded = false;
	private Map<String, Integer> columnSize = new HashMap<String, Integer>();
	private int queryTimeout = -1;

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

	@Override
	public void persistDataSet(IDataSet dataset) throws Exception {
		String tableName = dataset.getTableNameForReading();

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
		if (getDialect().contains(DIALECT_DB2) || getDialect().contains(DIALECT_INGRES) || getDialect().contains(DIALECT_TERADATA)) {
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
		if (dataset.getDsType().toString().equalsIgnoreCase("File")) {
			ajustMetaDataFromFrontend(datastore, dataset);
		}
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
				dataStoreFieldMetaData.setType(dataSetMetaData.getFieldMeta(i).getType());

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
							Integer intValue;
							Object rawField = field.getValue();
							if (rawField instanceof BigDecimal) {
								intValue = ((BigDecimal) rawField).intValueExact();
							} else {
								intValue = Integer.valueOf((String) rawField);
							}
							field.setValue(intValue);
						} catch (Throwable t) {
							logger.error("Error trying to convert value [" + field.getValue() + "] into an Integer value. Considering it as null...");
							field.setValue(null);
						}
					} else if (fmd.getType().toString().contains("Double")) {
						try {
							Double doubleValue;
							Object rawField = field.getValue();
							if (rawField instanceof BigDecimal) {
								doubleValue = ((BigDecimal) rawField).doubleValue();
							} else {
								doubleValue = Double.valueOf((String) rawField);
							}
							field.setValue(doubleValue);

						} catch (Throwable t) {
							logger.error("Error trying to convert value [" + field.getValue() + "] into a Double value. Considering it as null...");
							field.setValue(null);
						}
					} else if (fmd.getType().toString().contains("String")) {
						try {
							String stringValue = field.getValue().toString();
							field.setValue(stringValue);
						} catch (Throwable t) {
							logger.error("Error trying to convert value [" + field.getValue() + "] into a String value. Considering it as null...");
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
			logger.debug("The datastore metadata object contains # [" + datastore.getMetaData().getFieldCount() + "] fields");
			if (datastore.getMetaData().getFieldCount() == 0) {
				logger.debug("The datastore metadata object hasn't fields. Dataset doesn't persisted!!");
				return;
			}
			connection = getConnection(datasource);

			// VoltDB does not allow explicit commit/rollback actions.
			// It uses an internal transaction committing mechanism.
			// More tests to see the consistency has to be done on this.
			if (!dialect.contains("VoltDB")) {
				connection.setAutoCommit(false);
			}
			// Steps #1: define prepared statement (and max column size for
			// strings type)
			PreparedStatement[] statements = defineStatements(datastore, datasource, connection);
			// Steps #2: set query timeout (if necessary)
			if (queryTimeout > 0) {
				for (int i = 0; i < statements.length; i++) {
					statements[i].setQueryTimeout(queryTimeout);
				}
			}
			// Steps #3,4: define create table statement
			createTable(datastore.getMetaData(), datasource);
			// Step #5: execute batch with insert statements
			for (int i = 0; i < statements.length; i++) {
				PreparedStatement statement = statements[i];
				statement.executeBatch();
				statement.close();
			}
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

	private PreparedStatement[] defineStatements(IDataStore datastore, IDataSource datasource, Connection connection) throws DataBaseException {
		int batchCount = (int) ((datastore.getRecordsCount() + BATCH_SIZE - 1) / BATCH_SIZE);
		PreparedStatement[] toReturn = new PreparedStatement[batchCount];

		IMetaData storeMeta = datastore.getMetaData();
		int fieldCount = storeMeta.getFieldCount();

		if (fieldCount == 0)
			return new PreparedStatement[0];

		String insertQuery = "insert into " + getTableName();
		String values = " values ";
		// createQuery used only for HSQL at this time
		String createQuery = "create table " + getTableName() + " (";

		insertQuery += " (";
		values += " (";
		String separator = "";

		if (this.isRowCountColumIncluded()) {
			CacheDataBase dataBase = DataBaseFactory.getCacheDataBase(datasource);
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

			for (int i = 0; i < batchCount; i++) {
				toReturn[i] = connection.prepareStatement(totalQuery);
			}

			logger.debug("Prepared statement for persist dataset as : " + totalQuery);

			for (int i = 0; i < datastore.getRecordsCount(); i++) {
				int currentBatch = i / BATCH_SIZE;
				PreparedStatement statement = toReturn[currentBatch];

				if (this.isRowCountColumIncluded()) {
					statement.setLong(1, i + 1);
				}

				IRecord record = datastore.getRecordAt(i);
				for (int j = 0; j < record.getFields().size(); j++) {
					try {
						IFieldMetaData fieldMeta = storeMeta.getFieldMeta(j);
						IField field = record.getFieldAt(j);
						Object fieldValue = field.getValue();
						String fieldMetaName = fieldMeta.getName();
						String fieldMetaTypeName = fieldMeta.getType().toString();
						boolean isfieldMetaFieldTypeMeasure = fieldMeta.getFieldType().equals(FieldType.MEASURE);
						if (this.isRowCountColumIncluded()) {
							PersistedTableHelper.addField(statement, j + 1, fieldValue, fieldMetaName, fieldMetaTypeName, isfieldMetaFieldTypeMeasure,
									getColumnSize());
						} else {
							PersistedTableHelper.addField(statement, j, fieldValue, fieldMetaName, fieldMetaTypeName, isfieldMetaFieldTypeMeasure,
									getColumnSize());
						}
					} catch (Throwable t) {
						throw new RuntimeException("An unexpecetd error occured while preparing insert statemenet for record [" + i + "]", t);
					}
				}
				statement.addBatch();
			}
		} catch (Exception e) {
			logger.error("Error persisting the dataset into table", e);
			throw new SpagoBIEngineRuntimeException("Error persisting the dataset into table", e);
		}
		return toReturn;
	}

	private void initializeStatement(PreparedStatement statement, IRecord record, IMetaData storeMeta) {
		for (int j = 0; j < record.getFields().size(); j++) {
			IFieldMetaData fieldMeta = storeMeta.getFieldMeta(j);
			IField field = record.getFieldAt(j);
			Object fieldValue = field.getValue();
			String fieldMetaName = fieldMeta.getName();
			String fieldMetaTypeName = fieldMeta.getType().toString();
			boolean isfieldMetaFieldTypeMeasure = fieldMeta.getFieldType().equals(FieldType.MEASURE);
			PersistedTableHelper.addField(statement, j, fieldValue, fieldMetaName, fieldMetaTypeName, isfieldMetaFieldTypeMeasure, getColumnSize());
		}
	}

	public PreparedStatement defineStatement(IMetaData storeMeta, IDataSource datasource, Connection connection) throws DataBaseException {
		PreparedStatement statement;

		int fieldCount = storeMeta.getFieldCount();

		if (fieldCount == 0)
			return null;

		String insertQuery = "insert into " + getTableName();
		String values = " values ";
		// createQuery used only for HSQL at this time
		String createQuery = "create table " + getTableName() + " (";

		insertQuery += " (";
		values += " (";
		String separator = "";

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

			statement = connection.prepareStatement(totalQuery);

			// set query timeout (if necessary)
			if (queryTimeout > 0) {
				statement.setQueryTimeout(queryTimeout);
			}

			logger.debug("Prepared statement for persist record as : " + totalQuery);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error persisting the dataset into table", e);
		}
		return statement;
	}

	private String getSQLColumnName(IFieldMetaData fmd) {
		String columnName = fmd.getAlias() != null ? fmd.getAlias() : fmd.getName();
		logger.debug("Column name is " + columnName);
		return columnName;
	}

	private String getDBFieldType(IDataSource dataSource, IFieldMetaData fieldMetaData) throws DataBaseException {
		CacheDataBase dataBase = DataBaseFactory.getCacheDataBase(dataSource);
		if (getColumnSize().get(fieldMetaData.getName()) != null) {
			dataBase.setVarcharLength(getColumnSize().get(fieldMetaData.getName()));
		}
		Class type = fieldMetaData.getType();
		if (fieldMetaData.getFieldType().equals(FieldType.MEASURE) && type == String.class) {
			logger.debug("Column type is string but the field is measure: converting it into a double");
			type = Double.class;
		}

		return dataBase.getDataBaseType(type);
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
				toReturn = " DOUBLE ";
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

	private String getCreateTableQuery(IMetaData md, IDataSource dataSource) throws DataBaseException {
		String toReturn = null;

		// creates the table only when metadata has fields
		if (md.getFieldCount() > 0) {
			toReturn = "create table " + tableName + " (";

			if (this.isRowCountColumIncluded()) {
				CacheDataBase dataBase = DataBaseFactory.getCacheDataBase(dataSource);
				toReturn += " " + AbstractJDBCDataset.encapsulateColumnName(PersistedTableManager.getRowCountColumnName(), dataSource) + " "
						+ dataBase.getDataBaseType(Long.class) + " , ";
			}

			logger.debug("Create table cmd : it will manage #" + md.getFieldCount() + " fields...");
			for (int i = 0, l = md.getFieldCount(); i < l; i++) {
				IFieldMetaData fmd = md.getFieldMeta(i);
				String columnName = getSQLColumnName(fmd);
				logger.debug("Adding field #" + i + " with column name [" + columnName + "]");
				toReturn += " " + AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource) + getDBFieldType(dataSource, fmd);
				toReturn += ((i < l - 1) ? " , " : "");
			}
			toReturn += " )";
		} else {
			logger.debug("Metadata fields object not found! Doesn't create temporary table.");
		}

		return toReturn;
	}

	public Connection getConnection(IDataSource datasource) {
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
					throw new RuntimeException("No attribute with name " + attributeName + " found for user " + ((UserProfile) profile).getUserId());
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
	public static String generateRandomTableName(String prefix) {
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

	private static String randomAlphabetString(int len) {
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

	public static String getRowCountColumnName() {
		return "sbicache_row_id";
	}

	public int getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public void createTable(IMetaData md, IDataSource dataSource) throws Exception {
		logger.debug("IN");
		// Steps #1: define create table statement
		String createStmtQuery = getCreateTableQuery(md, dataSource);
		if (createStmtQuery != null) {
			dropTableIfExists(dataSource);
			// Step #2: execute create table statement
			executeStatement(createStmtQuery, dataSource);
		}
		logger.debug("OUT");
	}

	public boolean insertRecord(IRecord record, IMetaData metadata, PreparedStatement statement) throws SQLException {
		statement.clearParameters();
		initializeStatement(statement, record, metadata);
		return statement.execute();

	}

	public void insertRecords(List<IRecord> records, IMetaData metadata, PreparedStatement statement) throws SQLException {
		logger.debug("IN");
		statement.clearBatch();
		for (IRecord record : records) {
			logger.debug("Setting records to be insert into statement batch");
			statement.clearParameters();
			initializeStatement(statement, record, metadata);
			statement.addBatch();
		}
		statement.executeBatch();
		logger.debug("OUT");

	}

	public void configureColumnSize(IMetaData metadata) {
		for (int i = 0; i < metadata.getFieldCount(); i++) {
			Object displaySize = metadata.getFieldMeta(i).getProperty("displaySize");
			if (displaySize != null) {
				columnSize.put(metadata.getFieldName(i), (Integer) displaySize);
			}
		}
	}

	public void ajustMetaDataFromFrontend(IDataStore datastore, IDataSet dataset) {
		IMetaData storeMeta = datastore.getMetaData();
		IMetaData dataSetMeta = dataset.getMetadata();

		for (int i = 0; i < storeMeta.getFieldCount(); i++) {
			try {
				IFieldMetaData storeFieldMeta = storeMeta.getFieldMeta(i);
				String storeFieldMetaName = storeFieldMeta.getName();
				String storeFieldMetaTypeName = storeFieldMeta.getType().toString();
				for (int j = 0; j < dataSetMeta.getFieldCount(); j++) {
					try {
						IFieldMetaData dataSetFieldMeta = dataSetMeta.getFieldMeta(j);
						String dataSetFieldMetaName = dataSetFieldMeta.getName();
						String dataSetFieldMetaTypeName = dataSetFieldMeta.getType().toString();

						if (dataSetFieldMetaName.equals(storeFieldMetaName)) {
							if (!dataSetFieldMetaTypeName.equals(storeFieldMetaTypeName)) {
								storeFieldMeta.setType(dataSetFieldMeta.getType());
								changeFieldValueType(datastore, dataSetFieldMeta, j, dataSetFieldMeta.getType());
							}
						}

					} catch (Throwable t) {
						logger.error("An unexpecetd error occured while ajusting metadata for record [" + j + "]", t);
						throw new RuntimeException("An unexpecetd error occured while ajusting metadata for record [" + j + "]", t);
					}
				}
			} catch (Throwable t) {
				logger.error("An unexpecetd error occured while ajusting metadata for record [" + i + "]", t);
				throw new RuntimeException("An unexpecetd error occured while ajusting metadata for record [" + i + "]", t);
			}
		}
	}

	public void changeFieldValueType(IDataStore datastore, IFieldMetaData dataSetFieldMeta, int index, Class c) {

		for (int i = 0; i < datastore.getRecordsCount(); i++) {
			IRecord record = datastore.getRecordAt(i);
			IField field = record.getFieldAt(index);
			Constructor<?> cons;
			try {
				cons = c.getConstructor(String.class);
			} catch (NoSuchMethodException | SecurityException e) {
				logger.error("Error while creating construnctor for dynamically instancing class type", e);
				throw new SpagoBIEngineRuntimeException("Error while creating construnctor for dynamically instancing class type. Table name:" + tableName, e);
			}
			try {
				Object value = field.getValue();
				if (value != null) {
					field.setValue(cons.newInstance(String.valueOf(value)));
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error("Error while changing field value to different type that is comming from data set wizard", e);
				throw new SpagoBIEngineRuntimeException(
						"Error while changing field value to different type that is comming from data set wizard. Table name:" + tableName, e);
			}
		}
	}
}
