package it.eng.spagobi.utilities.database;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

public class DatabaseUtilities {

	private static transient Logger logger = Logger.getLogger(DatabaseUtilities.class);

	public static BigDecimal getUsedMemorySize(CacheDataBase database, String schema, String tableNamePrefix) throws DataBaseException {
		logger.trace("IN");
		BigDecimal userMemory = null;

		String query = database.getUsedMemorySizeQuery(schema, tableNamePrefix);
		if (query == null) {
			throw new DataBaseException("Impossible to build the query to get used memory size for the target database");
		}

		IDataStore dataStore = ((IDataBase) database).getDataSource().executeStatement(query, 0, 0, false);
		if (dataStore.getRecordsCount() == 0) {
			throw new DataBaseException("The execution of the query used to get used memory size returned no result [" + query + "]");
		}

		IRecord record = dataStore.getRecordAt(0);
		for (int i = 0, l = record.getFields().size(); i < l; i++) {
			IField field = record.getFieldAt(i);
			if (field.getValue() instanceof Long) {
				userMemory = BigDecimal.valueOf((Long) field.getValue());
			} else if (field.getValue() instanceof Integer) {
				Integer num = (Integer) field.getValue();
				userMemory = new BigDecimal(num);
			} else {
				userMemory = (BigDecimal) field.getValue();
			}
		}

		if (userMemory == null) {
			userMemory = new BigDecimal(0);
		}
		logger.trace("OUT");
		return userMemory;
	}

	// public static String getDBVendorNameFromDialect(DataSource dataSource) throws DataBaseException {
	//
	// IDataBase db = DataBaseFactory.getDataBase(dataSource);
	// String dbType = db.getName();
	//
	// // String databaseName = null;
	// // String upperCaseDialect = dialect.toUpperCase();
	// // if (upperCaseDialect.contains("MYSQL")) {
	// // databaseName = "mysql";
	// // } else if (upperCaseDialect.contains("ORACLE")) {
	// // databaseName = "oracle";
	// // } else if (upperCaseDialect.contains("POSTGRES")) {
	// // databaseName = "postgres";
	// // } else if (upperCaseDialect.contains("SQLSERVER")) {
	// // databaseName = "sqlserver";
	// // }
	// return dbType;
	// }
}
