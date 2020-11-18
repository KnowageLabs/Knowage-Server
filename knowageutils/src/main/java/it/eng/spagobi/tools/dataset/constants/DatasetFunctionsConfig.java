package it.eng.spagobi.tools.dataset.constants;

import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.BIGQUERY;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.CASSANDRA;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.DB2;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.HIVE;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.HIVE2;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.IMPALA;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.METAMODEL;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.MONGO;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.MYSQL;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.MYSQL_INNODB;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.ORACLE;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.ORACLE_9I10G;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.ORACLE_SPATIAL;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.ORIENT;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.POSTGRESQL;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.REDSHIFT;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.SPARKSQL;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.SQLSERVER;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.TERADATA;
import static it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect.VERTICA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class DatasetFunctionsConfig {

	private static final String AVAILABLE_FUNCTIONS = "availableFunctions";
	private static final String NULLIF_FUNCTION = "nullifFunction";
	private static final String NULLIF = "NULLIF";
	HashMap<String, HashMap<String, List<String>>> functionsConfigurationMap = new HashMap<String, HashMap<String, List<String>>>();

	public DatasetFunctionsConfig() {
		List<String> availableFunctions = new ArrayList<String>();
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> list = new ArrayList<String>();

		Stream.of(HIVE, CASSANDRA, ORIENT, METAMODEL).forEach(e -> functionsConfigurationMap.put(e.getValue(), map));

		list.add(NULLIF);
		map.put(NULLIF_FUNCTION, list);
		availableFunctions.add(NULLIF);
		map.put(AVAILABLE_FUNCTIONS, availableFunctions);

		Stream.of(HIVE2, MONGO, DB2, IMPALA, MYSQL, MYSQL_INNODB, ORACLE_9I10G, ORACLE, POSTGRESQL, SPARKSQL, SQLSERVER, ORACLE_SPATIAL, TERADATA, VERTICA,
				REDSHIFT, BIGQUERY).forEach(e -> functionsConfigurationMap.put(e.getValue(), map));
	}

	public List<String> getAvailableFunctions(String dialect) {
		return functionsConfigurationMap.get(dialect).get(AVAILABLE_FUNCTIONS);
	}

	public List<String> getNullifFunction(String dialect) {
		return functionsConfigurationMap.get(dialect).get(NULLIF_FUNCTION);
	}
}