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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.apache.log4j.Logger;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class SqlUpdateStatement {

	private final String sqlStatement;

	public static transient Logger logger = Logger.getLogger(SqlUpdateStatement.class);

	public SqlUpdateStatement(String sqlStatement) {
		super();
		this.sqlStatement = sqlStatement;
	}

	public void executeStatement(Connection connection) throws Exception {
		logger.debug("Executing update query: " + sqlStatement);

		try {
			PreparedStatement statement = connection.prepareStatement(sqlStatement);
			statement.executeUpdate();

		} catch (Exception e) {
			logger.error("Error executing the query " + sqlStatement, e);
			throw e;
		}
		logger.debug("Query executed");
	}

}
