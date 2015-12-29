/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
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
