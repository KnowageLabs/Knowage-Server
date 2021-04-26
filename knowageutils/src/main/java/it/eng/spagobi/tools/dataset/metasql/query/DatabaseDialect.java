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

package it.eng.spagobi.tools.dataset.metasql.query;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonFormat;

import it.eng.spagobi.utilities.assertion.Assert;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DatabaseDialect {

	// @formatter:off
	HIVE("Apache Hive v1", "hive", true, false, false, true),
	HIVE2("Apache Hive v2", "org.hibernate.dialect.Hive2Dialect", true, false, false, true),
	MONGO("MongoDB", "MongoDialect", false, false, false, false),
	CASSANDRA("Apache Cassandra", "org.hibernate.dialect.cassandra", true, false, false,false),
	DB2("IBM DB2", "org.hibernate.dialect.DB2400Dialect", true, true, false, false),
	IMPALA("Apache Impala","org.hibernate.dialect.impala", true, true, false,true),
	MYSQL("MySQL/MariaDB", "org.hibernate.dialect.MySQLDialect", true, true, true, true),
	MYSQL_INNODB("MySQL/MariaDB (INNODB)", "org.hibernate.dialect.MySQLInnoDBDialect", true, true, true, true),
	ORACLE_9I10G("Oracle 9i/10g","org.hibernate.dialect.Oracle9Dialect", true, true, true, true),
	ORACLE("Oracle", "org.hibernate.dialect.OracleDialect", true, true, true, true),
	POSTGRESQL("PostgreSQL","org.hibernate.dialect.PostgreSQLDialect", true, true, true, true),
	SPARKSQL("Apache Spark SQL", "org.hibernate.dialect.sparksql", true, false, false, true),
	SQLSERVER("Microsoft SQL Server", "org.hibernate.dialect.SQLServerDialect", true, true, false,	true),
	ORACLE_SPATIAL("Oracle Database Spatial", "org.hibernatespatial.oracle.CustomOracleSpatialDialect",	true, true, true, true),
	ORIENT("OrientDB", "orient", true, true, false, true),
	TERADATA("Teradata","org.hibernate.dialect.TeradataDialect", true, true, false, true),
	VERTICA("Vertica",	"org.hibernate.dialect.VerticaDialect", true, true, true, true),
	METAMODEL("MetaModelDialect", "metamodel", true, false, false, true),
	REDSHIFT("Amazon RedShift","org.hibernate.dialect.RedShiftDialect", true, true, true, true),
	BIGQUERY("Google BigQuery","org.hibernate.dialect.BigQueryDialect", true, true, true, true),
	SYNAPSE("Azure Synapse","org.hibernate.dialect.SynapseDialect", true, true, true, true);
	// @formatter:on

	private final static HashMap<String, DatabaseDialect> dialects = new HashMap<>(DatabaseDialect.values().length);

	static {
		for (DatabaseDialect dialect : DatabaseDialect.values()) {
			dialects.put(dialect.value, dialect);
		}
	}

	private final String name;
	private final String value;
	private final boolean isSqlLike;
	private final boolean singleColumnInOperatorSupported;
	private final boolean multiColumnInOperatorSupported;
	private final boolean inLineViewSupported;

	private DatabaseDialect(String name, String value, boolean isSqlLike, boolean isSingleColumnInOperatorSupported, boolean isMultiColumnInOperatorSupported,
			boolean inLineViewSupported) {
		Assert.assertTrue(isSingleColumnInOperatorSupported || !isMultiColumnInOperatorSupported,
				"Dialect can't support multi-column IN operator if it doesn't support single-column IN operator");

		this.name = name;
		this.value = value;
		this.isSqlLike = isSqlLike;
		this.singleColumnInOperatorSupported = isSingleColumnInOperatorSupported;
		this.multiColumnInOperatorSupported = isMultiColumnInOperatorSupported;
		this.inLineViewSupported = inLineViewSupported;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public boolean isSqlLike() {
		return isSqlLike;
	}

	public boolean isSingleColumnInOperatorSupported() {
		return singleColumnInOperatorSupported;
	}

	public boolean isMultiColumnInOperatorSupported() {
		return multiColumnInOperatorSupported;
	}

	public boolean isInLineViewSupported() {
		return inLineViewSupported;
	}

	public static DatabaseDialect get(String value) {
		return dialects.get(value);
	}

}
