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
package it.eng.spagobi.tools.dataset.bo;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JDBCDatasetFactory {
	private static transient Logger logger = Logger.getLogger(JDBCDatasetFactory.class);

	public static IDataSet getJDBCDataSet(IDataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("datasource parameter cannot be null");
		}

		DatabaseDialect databaseDialect;
		try {
			databaseDialect = DataBaseFactory.getDataBase(dataSource).getDatabaseDialect();
		} catch (DataBaseException e) {
			throw new SpagoBIRuntimeException("Unable to get dataset by datasource");
		}

		IDataSet dataSet = null;
		switch (databaseDialect) {
		case CASSANDRA:
		case HIVE:
		case HIVE2:
		case SPARKSQL:
			dataSet = new JDBCHiveDataSet();
			break;
		case ORIENT:
			dataSet = new JDBCOrientDbDataSet();
			break;
		case IMPALA:
			dataSet = new JDBCImpalaDataSet();
			break;
		case VERTICA:
			dataSet = new JDBCVerticaDataSet();
			break;
		case REDSHIFT:
			dataSet = new JDBCRedShiftDataSet();
			break;
		case BIGQUERY:
			dataSet = new JDBCBigQueryDataSet();
			break;
		case SYNAPSE:
			dataSet = new JDBCSynapseDataSet();
			break;
		case SPANNER:
			dataSet = new JDBCSpannerDataSet();
			break;
		default:
			dataSet = new JDBCDataSet();
		}

		return dataSet;
	}
}
