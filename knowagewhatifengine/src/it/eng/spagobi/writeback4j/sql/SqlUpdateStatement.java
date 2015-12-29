/**
 * 
 */
package it.eng.spagobi.writeback4j.sql;

import java.sql.Connection;
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
			Statement statement = connection.createStatement();
			statement.executeUpdate(sqlStatement);

		} catch (Exception e) {
			logger.error("Error executing the query " + sqlStatement, e);
			throw e;
		}
		logger.debug("Query executed");
	}

}
