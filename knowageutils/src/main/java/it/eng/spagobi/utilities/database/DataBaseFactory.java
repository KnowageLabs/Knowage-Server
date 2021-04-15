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

import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class DataBaseFactory {

	/**
	 * @param dataSource
	 * @return null if @param dataSource is null
	 * @throws DataBaseException
	 */
	public static IDataBase getDataBase(IDataSource dataSource) throws DataBaseException {
		IDataBase dataBase = null;
		if (dataSource != null) {
			String dialect = dataSource.getHibDialectClass();
			Assert.assertNotNull(dialect, "Impossible to find a database implementation for datasource [" + dataSource + "]");
			if (dialect != null) {
				DatabaseDialect sqlDialect = DatabaseDialect.get(dialect);
				Assert.assertNotNull(sqlDialect, "Impossible to find a database implementation for dialect [" + dialect + "]");
				switch (sqlDialect) {
				case HIVE:
					return new HiveDataBase(dataSource);
				case HIVE2:
					return new Hive2DataBase(dataSource);
				case SPARKSQL:
					return new SparkSqlDataBase(dataSource);
				case IMPALA:
					return new ImpalaDataBase(dataSource);
				case MYSQL:
				case MYSQL_INNODB:
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
				case TERADATA:
					return new TeradataDataBase(dataSource);
				case CASSANDRA:
					return new CassandraDataBase(dataSource);
				case DB2:
					return new DB2DataBase(dataSource);
				case MONGO:
					return new MongoDataBase(dataSource);
				case VERTICA:
					return new VerticaDataBase(dataSource);
				case REDSHIFT:
					return new RedShiftDataBase(dataSource);
				case BIGQUERY:
					return new BigQueryDataBase(dataSource);
				case SYNAPSE:
					return new SynapseDataBase(dataSource);
				default:
					throw new DataBaseException("Impossible to find a database implementation for [" + sqlDialect.toString() + "]");
				}
			}
		} else {
			return new MetaModelDataBase();
		}
		return dataBase;
	}

	/**
	 * @param dataSource
	 * @return null if @param dataSource is null
	 * @throws DataBaseException
	 */
	public static CacheDataBase getCacheDataBase(IDataSource dataSource) throws DataBaseException {
		IDataBase dataBase = getDataBase(dataSource);
		if (dataBase.isCacheSupported()) {
			return (CacheDataBase) dataBase;
		} else {
			throw new DataBaseException("The database " + dataBase.getName() + " cannot be used as cache");
		}
	}

	/**
	 * @param dataSource
	 * @return null if @param dataSource is null
	 * @throws DataBaseException
	 */
	public static MetaDataBase getMetaDataBase(IDataSource dataSource) throws DataBaseException {
		IDataBase dataBase = getDataBase(dataSource);
		if (dataBase.isMetaSupported()) {
			return (MetaDataBase) dataBase;
		} else {
			throw new DataBaseException("The database " + dataBase.getName() + " cannot be used with meta");
		}
	}
}
