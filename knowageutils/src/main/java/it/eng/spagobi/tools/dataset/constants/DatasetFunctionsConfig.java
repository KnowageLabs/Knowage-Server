package it.eng.spagobi.tools.dataset.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;

public class DatasetFunctionsConfig {

	private static final String AVAILABLE_FUNCTIONS = "availableFunctions";
	private static final String NULLIF_FUNCTION = "nullifFunction";
	private static final String NULLIF = "NULLIF";
	HashMap<String, HashMap<String, List<String>>> functionsConfigurationMap = new HashMap<String, HashMap<String, List<String>>>();

	public DatasetFunctionsConfig() {
		List<String> availableFunctions = new ArrayList<String>();
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<String>();

		functionsConfigurationMap.put(DatabaseDialect.HIVE.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.CASSANDRA.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.ORIENT.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.METAMODEL.getValue(), map);

		list.add(NULLIF);
		map.put(NULLIF_FUNCTION, list);
		availableFunctions.add(NULLIF);
		map.put(AVAILABLE_FUNCTIONS, availableFunctions);

		functionsConfigurationMap.put(DatabaseDialect.HIVE2.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.MONGO.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.DB2.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.IMPALA.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.MYSQL.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.MYSQL_INNODB.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.ORACLE_9I10G.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.ORACLE.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.POSTGRESQL.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.SPARKSQL.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.SQLSERVER.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.ORACLE_SPATIAL.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.TERADATA.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.VERTICA.getValue(), map);
		functionsConfigurationMap.put(DatabaseDialect.REDSHIFT.getValue(), map);
	}

	public List<String> getAvailableFunctions(String dialect) {
		return functionsConfigurationMap.get(dialect).get(AVAILABLE_FUNCTIONS);
	}

	public List<String> getNullifFunction(String dialect) {
		return functionsConfigurationMap.get(dialect).get(NULLIF_FUNCTION);
	}
}