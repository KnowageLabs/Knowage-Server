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
import java.sql.Statement;

import org.apache.log4j.Logger;

public class SqlInsertStatement {

	private final String sqlStatement;

	public static transient Logger logger = Logger.getLogger(SqlInsertStatement.class);

	public SqlInsertStatement(String sqlStatement) {
		super();
		this.sqlStatement = sqlStatement;
	}

	public void executeStatement(Connection connection) throws SpagoBIEngineException {
		try {

			Statement statement = connection.createStatement();
			statement.executeUpdate(sqlStatement);

		} catch (Exception e) {
			logger.error("Error executing the query " + sqlStatement, e);
			throw new SpagoBIEngineException("Error executing the query " + sqlStatement, e);
		}
	}

}
