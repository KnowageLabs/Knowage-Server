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
package it.eng.spagobi.tools.dataset.persist.temporarytable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.persist.DataSetTableDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DatasetTemporaryTableUtils {

	private static transient Logger logger = Logger.getLogger(DatasetTemporaryTableUtils.class);

	/**
	 * Creates a table with columns got from metadata. PAY ATTENTION TO THE FACT THAT THE INPUT CONNECTION WON'T BE CLOSED!!!!!
	 *
	 * @param conn
	 *            The JDBC connection to be used
	 * @param meta
	 *            The metadata of the dataset to be persisted on the database
	 * @param tableName
	 *            The name of the table to be created
	 * @param list
	 *            The list of the fields of the dataset to be included on table
	 * @return A DataSetTableDescriptor that contains the association between table's columns and dataset's fields.
	 */
	public static DataSetTableDescriptor createTemporaryTable(Connection conn, IMetaData meta, String tableName, List<String> selectedFields) {
		logger.debug("IN");

		DataSetTableDescriptor dstd = null;
		Statement st = null;
		String sqlQuery = null;

		if (selectedFields == null) {
			selectedFields = new ArrayList<String>();
		}

		try {
			CreateTableCommand createTableCommand = new CreateTableCommand(tableName, conn.getMetaData().getDriverName());

			// run through all columns in order to build the SQL columndefinition
			int count = meta.getFieldCount();
			logger.debug("The table tableName has " + count + " columns ");
			for (int i = 0; i < count; i++) {
				IFieldMetaData fieldMeta = meta.getFieldMeta(i);
				String fieldName = fieldMeta.getName();
				if (selectedFields.isEmpty() || selectedFields.contains(fieldName)) {
					createTableCommand.addColumn(fieldMeta);
				}
			}

			// after built columns create SQL Query
			sqlQuery = createTableCommand.createSQLQuery();

			// execute
			logger.debug("Executing the query " + sqlQuery + "...");
			st = conn.createStatement();
			st.execute(sqlQuery);
			logger.debug("Query executed");
			dstd = createTableCommand.getDsTableDescriptor();
			LogMF.debug(logger, "The query descriptor is {0}", dstd);

		} catch (SQLException e) {
			logger.error("Error in excuting statement " + sqlQuery, e);
			throw new SpagoBIRuntimeException("Error creating temporary table", e);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException e) {
				logger.error("could not free resources ", e);
			}
		}
		logger.debug("OUT");
		return dstd;
	}
}
