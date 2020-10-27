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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.CkanDataSet;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.database.CacheDataBase;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
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

	private DatabaseDialect dialect = null;
	private String tableName = new String();
	private boolean rowCountColumIncluded = false;
	private Map<String, Integer> columnSize = new HashMap<String, Integer>();
	private int queryTimeout = -1;

	private IEngUserProfile profile = null;

	private static transient Logger logger = Logger.getLogger(PersistedTableManager.class);

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

		IDataBase database = DataBaseFactory.getDataBase(dsPersist);
		if (!database.isCacheSupported()) {
			logger.error("Persistence management not implemented for dialect " + getDialect() + ".");
			throw new SpagoBIRuntimeException("Persistence management not implemented for dialect " + getDialect() + ".");
		}

		setTableName(tableName);
		logger.debug("Persisted table name is [" + getTableName() + "]");

		setDialect(database.getDatabaseDialect());
		logger.debug("DataSource target dialect is [" + getDialect() + "]");

		String signature = dataset.getSignature();
		logger.debug("Dataset signature : " + signature);
		if (signature != null && signature.equals(TemporaryTableManager.getLastDataSetSignature(tableName))) {
			// signature matches: no need to create a Persistent Table
			logger.debug("Signature matches: no need to create a Persistent Table");
			return;
		}

		dataset.setPersisted(false);
		if (dataset.isIterable()) {
			persist(dataset, dsPersist, tableName);
		} else {
			dataset.loadData();
			IDataStore datastore = dataset.getDataStore();
			if (dataset.getDsType().toString().equalsIgnoreCase("File")) {
				ajustMetaDataFromFrontend(datastore, dataset);
			}
			persistDataset(datastore, dsPersist);
		}
	}

	public void persist(IDataSet dataSet, IDataSource datasource, String tableName) throws Exception {
		logger.debug("IN");

		Monitor monitor = MonitorFactory.start("spagobi.cache.sqldb.persist.paginated");
		logger.debug("Starting iteration to transfer data");
		try (DataIterator iterator = dataSet.iterator()) {
			Connection connection = null;
			PreparedStatement statement = null;
			try {
				connection = getConnection(datasource);
				connection.setAutoCommit(false);
				statement = defineStatement(iterator.getMetaData(), datasource, connection);

				logger.debug("Setting required column sizes");
				configureColumnSize(iterator.getMetaData());

				logger.debug("Creating table to transfer data");
				createTable(iterator.getMetaData(), datasource);

				List<IRecord> records = new ArrayList<>(BATCH_SIZE);
				int recordCount = 0;
				while (iterator.hasNext()) {
					logger.debug("ResultSet iteration number " + recordCount);
					IRecord record = iterator.next();
					records.add(record);
					if (records.size() == BATCH_SIZE) {
						logger.debug("Building batch to insert " + BATCH_SIZE + " records");
						insertRecords(records, iterator.getMetaData(), statement);
						records.clear();
					}
					recordCount++;
				}
				if (!records.isEmpty()) {
					logger.debug("There are still " + records.size() + " records left that need to be copied into the cache");
					insertRecords(records, iterator.getMetaData(), statement);
					records.clear();
				}
				logger.debug("Committing inserts...");
				connection.commit();
			} catch (Exception e) {
				logger.error("Error while trasferring data from source to cache");
				if (connection != null) {
					connection.rollback();
				}
				logger.debug("Removing the empty table from cache because no data has been copied");
				dropTableIfExists(datasource, tableName);
				throw e;
			} finally {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
				monitor.stop();
				logger.debug("OUT");
			}
		}

	}

	public void persistDataset(IDataSet dataSet, IDataStore datastore, IDataSource datasource, String tableName) throws Exception {
		setTableNameAndDialect(datasource, tableName);

		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		if (dataSet instanceof FileDataSet || dataSet instanceof CkanDataSet) {
			datastore = normalizeFileDataSet(dataSet, datastore);
		}

		persistDataset(datastore, datasource);
	}

	public void updateDataset(IDataSource datasource, IDataStore datastore, String tableName) throws Exception {
		setTableNameAndDialect(datasource, tableName);

		updateDataset(datastore, datasource);
	}

	private void setTableNameAndDialect(IDataSource datasource, String tableName) throws DataBaseException {
		IDataBase database = DataBaseFactory.getDataBase(datasource);
		if (!database.isCacheSupported()) {
			logger.debug("Persistence management isn't implemented for " + getDialect() + ".");
			throw new SpagoBIServiceException("", "sbi.ds.dsCannotPersistDialect");
		}

		this.setTableName(tableName);
		this.setDialect(database.getDatabaseDialect());
		logger.debug("DataSource target dialect is [" + getDialect() + "]");
	}

	private void updateDataset(IDataStore datastore, IDataSource datasource) throws Exception {
		logger.debug("IN");

		Connection connection = null;
		try {
			logger.debug("The datastore metadata object contains # [" + datastore.getMetaData().getFieldCount() + "] fields");
			if (datastore.getMetaData().getFieldCount() == 0) {
				logger.debug("The datastore has no fields. Unable to update dataset.");
				return;
			}

			int idFieldIndex = datastore.getMetaData().getIdFieldIndex();
			if (idFieldIndex == -1) {
				logger.debug("The datastore has no ID field. Unable to update dataset.");
				return;
			}

			String idFieldAlias = datastore.getMetaData().getFieldAlias(idFieldIndex);
			connection = getConnection(datasource);

			Set<Object> sourceIds = getIds(connection, idFieldAlias);
			Set<Object> insertIds = datastore.getFieldDistinctValues(idFieldIndex);
			insertIds.removeAll(sourceIds);

			PreparedStatement[] statements = getInsertOrUpdateStatements(datastore, datasource, connection, insertIds);

			for (PreparedStatement statement : statements) {
				if (queryTimeout > 0) {
					statement.setQueryTimeout(queryTimeout);
				}
				statement.executeBatch();
				statement.close();
			}
			logger.debug("Records updated on table successfully!");
		} catch (Exception e) {
			String message = "Error updating the dataset on table";
			logger.error(message, e);
			if (connection != null) {
				connection.rollback();
			}
			throw new SpagoBIEngineRuntimeException(message, e);
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		}

		logger.debug("OUT");
	}

	private Set<Object> getIds(Connection connection, String idFieldAlias) throws SQLException {
		Set<Object> ids = new HashSet<>();

		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("SELECT ");
		selectQuery.append(idFieldAlias);
		selectQuery.append(" FROM ");
		selectQuery.append(getTableName());

		try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery.toString())) {
			if (queryTimeout > 0) {
				preparedStatement.setQueryTimeout(queryTimeout);
			}
			try (ResultSet rs = preparedStatement.executeQuery()) {
				while (rs.next()) {
					Object id = rs.getObject(idFieldAlias);
					ids.add(id);
				}
			}
		}

		return ids;
	}

	private PreparedStatement[] getInsertOrUpdateStatements(IDataStore datastore, IDataSource datasource, Connection connection, Set<Object> insertIds) {
		IMetaData storeMeta = datastore.getMetaData();
		int fieldCount = storeMeta.getFieldCount();
		int recordCount = (int) datastore.getRecordsCount();

		if (fieldCount == 0 || recordCount == 0) {
			return new PreparedStatement[0];
		}

		try {
			prefillColumnSizes(connection);
		} catch (SQLException e1) {
			logger.error("Unable to prefill column sizes");
		}

		PreparedStatement[] toReturn = new PreparedStatement[recordCount];
		StringBuilder insertSB = new StringBuilder("INSERT INTO ");
		insertSB.append(getTableName());
		insertSB.append(" (");

		StringBuilder insertValuesSB = new StringBuilder(" VALUES (");

		StringBuilder updateSB = new StringBuilder("UPDATE ");
		updateSB.append(getTableName());
		updateSB.append(" SET ");

		String insertSeparator = "";
		String updateSeparator = "";

		if (this.isRowCountColumIncluded()) {
			String rowCountColumnName = AbstractJDBCDataset.encapsulateColumnName(getRowCountColumnName(), datasource);

			insertSB.append(insertSeparator);
			insertSB.append(rowCountColumnName);

			insertValuesSB.append(insertSeparator);
			insertValuesSB.append("?");

			insertSeparator = ",";

			updateSB.append(updateSeparator);
			updateSB.append(rowCountColumnName);
			updateSB.append("=?");

			updateSeparator = ",";
		}

		int idFieldIndex = datastore.getMetaData().getIdFieldIndex();
		for (int i = 0; i < fieldCount; i++) {
			IFieldMetaData fieldMeta = storeMeta.getFieldMeta(i);
			String columnName = getSQLColumnName(fieldMeta);
			String escapedColumnName = AbstractJDBCDataset.encapsulateColumnName(columnName, datasource);

			insertSB.append(insertSeparator);
			insertSB.append(escapedColumnName);

			insertValuesSB.append(insertSeparator);
			insertValuesSB.append("?");

			insertSeparator = ",";

			if (i != idFieldIndex) {
				updateSB.append(updateSeparator);
				updateSB.append(escapedColumnName);
				updateSB.append("=?");

				updateSeparator = ",";
			}
		}

		insertSB.append(") ");

		insertValuesSB.append(") ");

		updateSB.append(" WHERE ");
		updateSB.append(AbstractJDBCDataset.encapsulateColumnName(getSQLColumnName(storeMeta.getFieldMeta(idFieldIndex)), datasource));
		updateSB.append("=?");

		String insertQuery = insertSB.toString() + insertValuesSB.toString();
		String updateQuery = updateSB.toString();

		logger.debug("INSERT statement: " + insertQuery);
		logger.debug("UPDATE statement: " + updateQuery);

		try {
			for (int i = 0; i < recordCount; i++) {
				IRecord record = datastore.getRecordAt(i);
				Object id = record.getFieldAt(idFieldIndex).getValue();
				boolean doInsert = insertIds.contains(id);

				PreparedStatement statement = connection.prepareStatement(doInsert ? insertQuery : updateQuery);
				toReturn[i] = statement;

				if (this.isRowCountColumIncluded()) {
					statement.setLong(1, i + 1L);
				}

				List<Integer> sortedIds = new ArrayList<>();
				for (int j = 0; j < record.getFields().size(); j++) {
					if (doInsert || j != idFieldIndex) {
						sortedIds.add(j);
					}
				}
				if (!doInsert) {
					sortedIds.add(idFieldIndex);
				}

				for (int j = 0; j < sortedIds.size(); j++) {
					try {
						int index = sortedIds.get(j);
						IFieldMetaData fieldMeta = storeMeta.getFieldMeta(index);
						IField field = record.getFieldAt(index);
						Object fieldValue = field.getValue();
						String alias = fieldMeta.getAlias();
						String fieldMetaName = alias != null ? alias : fieldMeta.getName();
						String fieldMetaTypeName = fieldMeta.getType().toString();
						boolean isFieldMetaFieldTypeMeasure = fieldMeta.getFieldType().equals(FieldType.MEASURE);

						Map<String, Integer> newColumnSizes = new HashMap<>();
						int fieldIndex = isRowCountColumIncluded() ? j + 1 : j;
						PersistedTableHelper.addField(statement, fieldIndex, fieldValue, fieldMetaName, fieldMetaTypeName, isFieldMetaFieldTypeMeasure,
								newColumnSizes);

						Integer oldColumnSize = columnSize.get(fieldMetaName);
						Integer newColumnSize = newColumnSizes.get(fieldMetaName);
						if (oldColumnSize != null && newColumnSize != null && newColumnSize > oldColumnSize) {
							columnSize.remove(fieldMetaName);
							columnSize.put(fieldMetaName, newColumnSize);
							try (Statement stmt = connection.createStatement()) {
								String query = "ALTER TABLE " + tableName + " MODIFY COLUMN " + fieldMetaName + " "
										+ getDBFieldTypeFromAlias(datasource, fieldMeta);
								stmt.executeUpdate(query);
							}
						}
					} catch (Exception e) {
						throw new RuntimeException("An unexpected error occurred while preparing statement for record [" + i + "]", e);
					}
				}
				statement.addBatch();
			}
		} catch (Exception e) {
			String message = "Error updating dataset into table";
			logger.error(message, e);
			throw new SpagoBIEngineRuntimeException(message, e);
		}
		return toReturn;
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
								intValue = Integer.valueOf(rawField.toString());
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
								doubleValue = Double.valueOf(rawField.toString());
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
			for (int i = 0; i < batchCount; i++) {
				toReturn[i] = connection.prepareStatement(totalQuery);
			}

			logger.debug("Prepared statement for persist dataset as : " + totalQuery);

			for (int i = 0; i < datastore.getRecordsCount(); i++) {
				int currentBatch = i / BATCH_SIZE;
				PreparedStatement statement = toReturn[currentBatch];

				if (this.isRowCountColumIncluded()) {
					statement.setLong(1, i + 1L);
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
									columnSize);
						} else {
							PersistedTableHelper.addField(statement, j, fieldValue, fieldMetaName, fieldMetaTypeName, isfieldMetaFieldTypeMeasure, columnSize);
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

	private void prefillColumnSizes(Connection connection) throws SQLException {
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + getTableName())) {
			if (queryTimeout > 0) {
				preparedStatement.setQueryTimeout(queryTimeout);
			}
			try (ResultSet rs = preparedStatement.executeQuery()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				while (rs.next()) {
					for (int i = 1; i <= rsmd.getColumnCount(); i++)
						columnSize.put(rsmd.getColumnName(i), rsmd.getColumnDisplaySize(i));
				}
			}
		}
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

	public boolean createIndexesOnTable(IDataSet dataset, IDataSource datasource, String tablename, Set<String> columns) {
		boolean result = false;
		/* INDEXES CREATION */
		if (columns != null && columns.size() > 0) {
			try {
				createIndexes(dataset, datasource, tablename, columns);
				result = true;
			} catch (Exception e) {
				logger.error(e.getStackTrace(), e);
			}
		}

		return result;
	}

	private boolean indexAlreadyOnTable(Connection conn, IDataSource datasource, String tableName, Set<String> columns, String indexName) {
		boolean result = false;

		Statement stmt = null;
		ResultSet rs3 = null;

		try {
			String query = buildGetIndexOnTable(conn, tableName, columns, indexName);
			stmt = conn.createStatement();
			int count = -1;
			if (query != null) {
				rs3 = stmt.executeQuery(query);

				while (rs3.next()) {
					count = rs3.getInt("cnt");
				}
			}

			result = count > 0;
		} catch (SQLException e) {
			logger.debug("Impossible to retrieve index for table [" + tableName + "] and columns [" + columns.iterator().next() + "]", e);
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e1) {
					logger.debug(e1);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e2) {
					logger.debug(e2);
				}
			}
		}

		return result;
	}

	public void createIndexes(IDataSet dataset, IDataSource datasource, String tableName, Set<String> columns) throws Exception {
		logger.debug("IN - Dataset label " + tableName);
		String signature = tableName;
		logger.debug("Retrieve table name for signature " + signature);

		Connection conn = getConnection(datasource);
		Statement stmt = null;

		try {
			Iterator<String> it = columns.iterator();
			while (it.hasNext()) {
				String currInd = it.next();
				Set<String> currIndSet = new HashSet<String>();
				currIndSet.add(currInd);

				if (!indexAlreadyOnTable(conn, datasource, tableName, currIndSet, "fed" + Math.abs(columns.hashCode()))) {
					String query = buildIndexStatement(conn, tableName, currIndSet);

					if (query != null) {
						stmt = conn.createStatement();
						stmt.executeUpdate(query);
					} else {
						logger.debug("Impossible to build the index statement and thus creating the index. Tablename and/or column are null or empty.");
					}
				} else {
					logger.debug("Index on table " + tableName + " (" + columns.iterator().next() + ")already present in database");
				}
			}
		} catch (SQLException e) {
			logger.debug("Impossible to build index for table [" + tableName + "] and columns [" + columns + "]", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.debug(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.debug(e);
				}
			}
		}

		logger.debug("OUT");
	}

	private String buildGetIndexOnTable(Connection conn, String tableName, Set<String> columns, String indexName) {
		logger.debug("IN - Table [" + tableName + "], Column [" + columns + "]");

		String column = columns.iterator().next();
		String statement = null;
		if (tableName != null && !tableName.isEmpty() && columns != null && !columns.isEmpty()) {
			try {
				if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("oracle")) {
					StringBuilder sb = new StringBuilder();
					sb.append("SELECT count(1) as cnt ");
					sb.append("FROM all_ind_columns ic ");
					sb.append("LEFT JOIN all_ind_expressions ie ");
					sb.append("  ON ie.index_owner  = ic.index_owner ");
					sb.append("  AND ie.index_name  = ic.index_name ");
					sb.append("  AND ie.column_position = ic.column_position ");
					sb.append("WHERE ic.table_name = '");
					sb.append(tableName.toUpperCase());
					sb.append("' ");
					sb.append("and ic.column_name = '");
					sb.append(column);
					sb.append("' ");
					statement = sb.toString();

				} else if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql")) {
					StringBuilder sb = new StringBuilder();

					sb.append("select COUNT(1) as cnt");
					sb.append(" from");
					sb.append(" pg_class t,");
					sb.append(" pg_class i,");
					sb.append(" pg_index ix,");
					sb.append(" pg_attribute a");
					sb.append(" where");
					sb.append(" t.oid = ix.indrelid");
					sb.append(" and i.oid = ix.indexrelid");
					sb.append(" and a.attrelid = t.oid");
					sb.append(" and a.attnum = ANY(ix.indkey)");
					sb.append(" and t.relkind = 'r'");
					sb.append(" and t.relname = '");
					sb.append(tableName);
					sb.append("' ");
					sb.append(" and a.attname = '");
					sb.append(column);
					sb.append("' ");

					statement = sb.toString();
				} else if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("mysql")) {
					StringBuilder sb = new StringBuilder();
					sb.append("SELECT COUNT(1) AS cnt");
					sb.append(" FROM INFORMATION_SCHEMA.STATISTICS");
					sb.append(" WHERE TABLE_NAME = '");
					sb.append(tableName);
					sb.append("' ");
					sb.append("and COLUMN_NAME = '");
					sb.append(column);
					sb.append("' ");
					statement = sb.toString();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
		}

		return statement;
	}

	private String buildIndexStatement(Connection conn, String tableName, Set<String> columns) {
		logger.debug("IN - Table [" + tableName + "], Column [" + columns + "]");

		String statement = null;
		if (tableName != null && !tableName.isEmpty() && columns != null && !columns.isEmpty()) {

			StringBuilder columnsSTring = new StringBuilder();
			for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
				String column = (String) iterator.next();
				columnsSTring = columnsSTring.append(column);
				columnsSTring = columnsSTring.append(",");
			}
			if (columnsSTring.length() > 2) {
				columnsSTring.setLength(columnsSTring.length() - 1);

				if (tableName != null && !tableName.isEmpty() && columns != null && !columns.isEmpty()) {
					try {
						if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("oracle")) {
							StringBuilder sb = new StringBuilder();
							sb.append("CREATE INDEX");
							sb.append(" ");
							sb.append("fed");
							sb.append(Math.abs(columns.hashCode()));
							sb.append(" ");
							sb.append("ON");
							sb.append(" ");
							sb.append(tableName);
							sb.append("(\"");
							sb.append(columnsSTring);
							sb.append("\")");
							statement = sb.toString();
						} else {
							StringBuilder sb = new StringBuilder();
							sb.append("CREATE INDEX");
							sb.append(" ");
							sb.append("fed");
							sb.append(Math.abs(columns.hashCode()));
							sb.append(" ");
							sb.append("ON");
							sb.append(" ");
							sb.append(tableName);
							sb.append("(");
							sb.append(columnsSTring);
							sb.append(")");
							statement = sb.toString();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		return statement;
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

	private String getDBFieldTypeFromAlias(IDataSource dataSource, IFieldMetaData fieldMetaData) throws DataBaseException {
		CacheDataBase dataBase = DataBaseFactory.getCacheDataBase(dataSource);
		Integer alias = getColumnSize().get(fieldMetaData.getAlias());
		if (alias != null) {
			dataBase.setVarcharLength(alias);
		}
		Class type = fieldMetaData.getType();
		if (fieldMetaData.getFieldType().equals(FieldType.MEASURE) && type == String.class) {
			logger.debug("Column type is string but the field is measure: converting it into a double");
			type = Double.class;
		}

		return dataBase.getDataBaseType(type);
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
				toReturn += " " + (md.getIdFieldIndex() == i ? "NOT NULL PRIMARY KEY" : "");
				toReturn += ((i < l - 1) ? " , " : "");
			}
			toReturn += " )";
		} else {
			logger.debug("Metadata fields object not found! Doesn't create temporary table.");
		}

		return toReturn;
	}

	public Connection getConnection(IDataSource datasource) {
		return datasource.getConnectionByUserProfile(profile);
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

		DatabaseDialect dialect = DatabaseDialect.get(datasource.getHibDialectClass());

		String statement = null;

		// get the list of tables names
		if (dialect.equals(DatabaseDialect.ORACLE) || dialect.equals(DatabaseDialect.ORACLE_9I10G)) {
			statement = "SELECT TABLE_NAME " + "FROM USER_TABLES " + "WHERE TABLE_NAME LIKE '" + prefix.toUpperCase() + "%'";
		} else if (dialect.equals(DatabaseDialect.SQLSERVER) || dialect.equals(DatabaseDialect.MYSQL) || dialect.equals(DatabaseDialect.MYSQL_INNODB)
				|| dialect.equals(DatabaseDialect.POSTGRESQL)) {
			statement = "SELECT TABLE_NAME " + "FROM INFORMATION_SCHEMA.TABLES " + "WHERE TABLE_NAME LIKE '" + prefix.toLowerCase() + "%'";
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
	 * @param prefix an optional prefix to use for the generated table name
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

	public DatabaseDialect getDialect() {
		return dialect;
	}

	public void setDialect(DatabaseDialect dialect) {
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
