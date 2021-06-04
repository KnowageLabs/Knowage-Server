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

package it.eng.spagobi.tools.dataset.metasql.query.visitor;

import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;

public class SelectQueryVisitorFactory {

	public static ISelectQueryVisitor getVisitor(IDataSource dataSource) throws DataBaseException {
		IDataBase database = DataBaseFactory.getDataBase(dataSource);
		if (dataSource == null) {
			return new MetaModelSelectQueryVisitor(database);
		}
		switch (database.getDatabaseDialect()) {
		case HIVE:
		case HIVE2:
			return new HiveSelectQueryVisitor(database);
		case SPARKSQL:
			return new SparkSqlSelectQueryVisitor(database);
		case IMPALA:
			return new ImpalaSelectQueryVisitor(database);
		case MYSQL:
		case MYSQL_INNODB:
			return new MySqlSelectQueryVisitor(database);
		case ORACLE:
		case ORACLE_9I10G:
		case ORACLE_SPATIAL:
		case VERTICA:
			return new OracleSelectQueryVisitor(database);
		case POSTGRESQL:
			return new PostgreSqlSelectQueryVisitor(database);
		case SQLSERVER:
			return new SqlServerSelectQueryVisitor(database);
		case ORIENT:
			return new OrientDbSelectQueryVisitor(database);
		case TERADATA:
			return new TeradataSelectQueryVisitor(database);
		case REDSHIFT:
			return new RedShiftSelectQueryVisitor(database);
		case BIGQUERY:
			return new BigQuerySelectQueryVisitor(database);
		case SYNAPSE:
			return new SqlServerSelectQueryVisitor(database);
		case SPANNER:
			return new SpannerSelectQueryVisitor(database);
		default:
			throw new IllegalArgumentException("Dialect [" + dataSource.getHibDialectClass() + "] not supported");
		}
	}
}
