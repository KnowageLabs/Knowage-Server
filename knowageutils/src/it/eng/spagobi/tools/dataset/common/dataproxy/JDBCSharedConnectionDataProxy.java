/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;


import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * This class is a JDBC data proxy that can be used when you have a java.sql.Connection object and you want to execute a query against 
 * that connection.
 * The connection must be not null and must be active.
 * When the query is executed, THE CONNECTION IS NOT CLOSED: it is caller class's responsibility to close the connection.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class JDBCSharedConnectionDataProxy extends AbstractDataProxy {
	
	private Connection connection;
	private String statement;
	
	private static transient Logger logger = Logger.getLogger(JDBCSharedConnectionDataProxy.class);
	
	public JDBCSharedConnectionDataProxy(Connection connection) {
		setConnection(connection);
	}
	
	public JDBCSharedConnectionDataProxy(Connection connection, String statement) {
		setConnection(connection);
		setStatement(statement);
	}
	
	/**
	 * Loads the input statement using the input reader.
	 * CONNECTION IS NOT CLOSED AFTER STATEMENT EXECUTED: it is caller class's responsibility to close the connection.
	 * 
	 * @param statement The statement to be executed
	 * @param dataReader The data reader to be used
	 * @return the data store
	 */
	public IDataStore load(String statement, IDataReader dataReader)  {
		if(statement != null) {
			setStatement(statement);
		}
		return load(dataReader);
	}
	
	/**
	 * Loads the internal statement using the input reader.
	 * CONNECTION IS NOT CLOSED AFTER STATEMENT EXECUTED: it is caller class's responsibility to close the connection.
	 * 
	 * @param dataReader The data reader to be used
	 * @return the data store
	 */
	public IDataStore load(IDataReader dataReader) {
		
		IDataStore dataStore;
		Statement stmt;
		ResultSet resultSet;
		
		logger.debug("IN");
		
		stmt = null;
		resultSet = null;
		
		try {			
			
			try {
				stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while creating connection steatment", t);
			}
			
			
	        try {
	        	//get max size 
	        	if(getMaxResults() > 0){
	        		stmt.setMaxRows(getMaxResults());
	        	}
				resultSet = stmt.executeQuery( getStatement() );
				
			} catch (Throwable t) {
				logger.error("Trovata!:",t);
				throw new SpagoBIRuntimeException("An error occurred while executing statement", t);
			}
			
			dataStore = null;
			try {
				dataStore = dataReader.read( resultSet );
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
			}
			
			if( isCalculateResultNumberOnLoadEnabled() ) {
				
			}
			
		} finally {		
			try {
				releaseResources(null, stmt, resultSet);
			} catch(Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to release allocated resources properly", t);
			}
		}
		
		return dataStore;
	}
	
	private void releaseResources(Connection connection, Statement statement, ResultSet resultSet) {
		
		logger.debug("IN");
		
		try {
			logger.debug("Relesing resources ...");
			if(resultSet != null) {
				try {
					resultSet.close();

				} catch (SQLException e) {
					throw new SpagoBIRuntimeException("Impossible to release [resultSet]", e);
				}
				logger.debug("[resultSet] released succesfully");
			}
			
			if(statement != null) {
				try {
					statement.close();
					
				} catch (SQLException e) {
					throw new SpagoBIRuntimeException("Impossible to release [statement]", e);
				}
				logger.debug("[statement] released succesfully");
			}
			
			if(connection != null) {
				try {
					if (!connection.isClosed()) {
					    connection.close();
					}
				} catch (SQLException e) {
					throw new SpagoBIRuntimeException("Impossible to release [connection]", e);
				}
				logger.debug("[connection] released succesfully");
			}		
			logger.debug("All resources have been released succesfully");
		} finally {
			logger.debug("OUT");
		}
	}


	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
}
