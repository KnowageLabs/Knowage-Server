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

package it.eng.spagobi.tools.dataset.cache.query;

import java.util.HashMap;

import it.eng.spagobi.utilities.assertion.Assert;

public enum SqlDialect {

	HBASE("hbase", false, false, false), HIVE("hive", false, false, true), MONGO("MongoDialect", false, false, false), CASSANDRA(
			"org.hibernate.dialect.cassandra", false, false,
			false), DB2("org.hibernate.dialect.DB2400Dialect", true, false, true), DRILL("org.hibernate.dialect.drill", true, false, false), HSQL(
					"org.hibernate.dialect.HSQLDialect", true, false,
					true), IMPALA("org.hibernate.dialect.impala", true, false, true), INGRES("org.hibernate.dialect.IngresDialect", true, false, false), MYSQL(
							"org.hibernate.dialect.MySQLInnoDBDialect", true, true,
							true), NEO4J("org.hibernate.dialect.neo4j", false, false, false), ORACLE_9I10G("org.hibernate.dialect.Oracle9Dialect", true, true,
									true), ORACLE("org.hibernate.dialect.OracleDialect", true, true, true), POSTGRESQL(
											"org.hibernate.dialect.PostgreSQLDialect", true, true, true), SPARKSQL("org.hibernate.dialect.sparksql", false,
													false, true), SQLSERVER("org.hibernate.dialect.SQLServerDialect", true, false, true), ORACLE_SPATIAL(
															"org.hibernatespatial.oracle.CustomOracleSpatialDialect", true, true,
															true), ORIENT("orient", true, false, true), TERADATA("org.hibernate.dialect.TeradataDialect", true,
																	false, true), VOLTDB("VoltDBDialect", true, true,
																			true), METAMODEL("MetaModelDialect", false, false, true);

	private final static HashMap<String, SqlDialect> dialects = new HashMap<>(SqlDialect.values().length);

	static {
		for (SqlDialect dialect : SqlDialect.values()) {
			dialects.put(dialect.value, dialect);
		}
	}

	private final String value;
	private final boolean singleColumnInOperatorSupported;
	private final boolean multiColumnInOperatorSupported;
	private final boolean inLineViewSupported;

	private SqlDialect(String value, boolean isSingleColumnInOperatorSupported, boolean isMultiColumnInOperatorSupported, boolean inLineViewSupported) {
		Assert.assertTrue(isSingleColumnInOperatorSupported || !isMultiColumnInOperatorSupported,
				"Dialect can't support multi-column IN operator if it doesn't support single-column IN operator");

		this.value = value;
		this.singleColumnInOperatorSupported = isSingleColumnInOperatorSupported;
		this.multiColumnInOperatorSupported = isMultiColumnInOperatorSupported;
		this.inLineViewSupported = inLineViewSupported;
	}

	public String getValue() {
		return value;
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

	public static SqlDialect get(String value) {
		return dialects.get(value);
	}

}
