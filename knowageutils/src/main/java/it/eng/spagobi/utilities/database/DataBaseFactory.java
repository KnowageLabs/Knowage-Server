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
package it.eng.spagobi.utilities.database;

import it.eng.spagobi.tools.dataset.cache.query.SqlDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class DataBaseFactory {

	public static IDataBase getDataBase(IDataSource dataSource) {
		IDataBase dataBase = null;
		if (dataSource != null) {
			String dialect = dataSource.getHibDialectClass();
			if (dialect != null) {
				switch (SqlDialect.get(dialect)) {
				case HBASE:
					return new HBaseDataBase(dataSource);
				case HIVE:
				case SPARKSQL:
					return new HiveDataBase(dataSource);
				case HSQL:
					return new HSQLDataBase(dataSource);
				case IMPALA:
					return new ImpalaDataBase(dataSource);
				case INGRES:
					return new IngresDataBase(dataSource);
				case MYSQL:
					return new MySQLDataBase(dataSource);
				case ORACLE:
					return new OracleDataBase(dataSource);
				case ORACLE_9I10G:
					return new Oracle9I10GDataBase(dataSource);
				case ORACLE_SPATIAL:
					return new OracleSpatialDataBase(dataSource);
				case POSTGRESQL:
					return new PostgreSQLDataBase(dataSource);
				case SQLSERVER:
					return new SQLServerDataBase(dataSource);
				case ORIENT:
					return new OrientDataBase(dataSource);
				case VOLTDB:
					return new VoltDBDataBase(dataSource);
				case TERADATA:
					return new TeradataDataBase(dataSource);
				case CASSANDRA:
					return new CassandraDataBase(dataSource);
				case DB2:
					return new DB2DataBase(dataSource);
				case DRILL:
				case MONGO:
				case NEO4J:
				default:
					break;
				}
			}
		} else {
			return new MetaModelDataBase(dataSource);
		}
		return dataBase;
	}
}
