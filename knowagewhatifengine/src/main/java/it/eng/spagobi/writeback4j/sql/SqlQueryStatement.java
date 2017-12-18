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
package it.eng.spagobi.writeback4j.sql;

import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
 * 
 */
public class SqlQueryStatement {

	private final String sqlStatement;

	public static transient Logger logger = Logger.getLogger(SqlQueryStatement.class);

	public SqlQueryStatement(String sqlStatement) {
		super();

		this.sqlStatement = sqlStatement;
	}

	public Object getSingleValue(Connection connection, String columnName) throws SpagoBIEngineException {
		Object toReturn = null;
		ResultSet rs = null;

		try {
			logger.debug("Executing query " + sqlStatement);
			Statement statement = connection.createStatement();
			rs = statement.executeQuery(sqlStatement);
			logger.debug("Query executed & getting the first value");

			if (rs.next()) {
				toReturn = rs.getObject(columnName);
			}

		} catch (Exception e) {
			logger.error("Error executing the query " + sqlStatement, e);
			throw new SpagoBIEngineException("Error executing the query " + sqlStatement, e);
		}

		return toReturn;
	}

	public ResultSet getValues(Connection connection) throws SpagoBIEngineException {
		ResultSet rs = null;

		try {
			logger.debug("Executing query " + sqlStatement);
			Statement statement = connection.createStatement();
			rs = statement.executeQuery(sqlStatement);
			logger.debug("Query executed");

		} catch (Exception e) {
			logger.error("Error executing the query " + sqlStatement, e);
			throw new SpagoBIEngineException("Error executing the query " + sqlStatement, e);
		}

		return rs;
	}

}
