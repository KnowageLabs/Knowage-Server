/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JDBCDataProxy extends AbstractDataProxy {

	IDataSource dataSource;
	String statement;
	String schema;

	private static transient Logger logger = Logger.getLogger(JDBCDataProxy.class);

	public JDBCDataProxy() {
		this.setCalculateResultNumberOnLoad(true);
	}

	public JDBCDataProxy(IDataSource dataSource, String statement) {
		this();
		setDataSource(dataSource);
		setStatement(statement);
	}

	public JDBCDataProxy(IDataSource dataSource) {
		this();
		setDataSource(dataSource);
		setStatement(statement);
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public IDataStore load(String statement, IDataReader dataReader) throws EMFUserError {
		if (statement != null) {
			setStatement(statement);
		}
		return load(dataReader);
	}

	@Override
	public IDataStore load(IDataReader dataReader) {

		IDataStore dataStore;
		Connection connection;
		Statement stmt;
		ResultSet resultSet;

		logger.debug("IN");

		connection = null;
		stmt = null;
		resultSet = null;

		try {

			try {
				connection = getDataSource().getConnection(getSchema());
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while creating connection", t);
			}
			String dialect = dataSource.getHibDialectClass();
			if (dialect == null) {
				dialect = dataSource.getHibDialectName();
			}
			try {
				// ATTENTION: For the most db sets the stmt as a scrollable
				// stmt, only for the compatibility with Ingres sets
				// a stmt forward only
				if (dialect.contains("Ingres")) {
					stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				} else if (dialect.contains("hbase") || SqlUtils.isHiveLikeDialect(dialect) || dialect.contains("SAP")) {
					stmt = connection.createStatement();
				} else {
					stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while creating connection steatment", t);
			}
			String sqlQuery = "";
			try {
				// get max size
				if (getMaxResults() > 0) {
					stmt.setMaxRows(getMaxResults());
				}
				sqlQuery = getStatement();
				logger.debug("Executing query " + sqlQuery + " ...");
				resultSet = stmt.executeQuery(sqlQuery);

			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while executing statement: " + sqlQuery, t);
			}

			boolean notCountingStrategyUsedSuccessfully = false;
			int resultNumber = -1;
			if (isCalculateResultNumberOnLoadEnabled()) {
				logger.debug("Calculation of result set total number is enabled");
				try {
					
					//if its an hive like db the query can be very slow so it's better to execute it just once and not use the inline view tecnique
					if(!SqlUtils.isHiveLikeDialect(dialect)){
						// try to calculate the query total result number using
						// inline view
						resultNumber = getResultNumber(connection);
						logger.debug("Calculation of result set total number successful : resultNumber = " + resultNumber);
						// ok, no need to ask the datareader to calculate the query
						// total result number
						dataReader.setCalculateResultNumberEnabled(false);
						notCountingStrategyUsedSuccessfully = true;
					}else{
						logger.debug("It's a BigData datasource so count data iterating result set till max ");
						dataReader.setCalculateResultNumberEnabled(true);
					}					
				} catch (Throwable t) {
					logger.debug("KO Calculation of result set total number using inlineview",t);
					try{
						logger.debug("Loading data using scrollable resultse tecnique");
						resultNumber = getResultNumber(resultSet);
						logger.debug("OK data loaded using scrollable resultse tecnique : resultNumber = " + resultNumber);
						dataReader.setCalculateResultNumberEnabled(false);
						notCountingStrategyUsedSuccessfully = true;
					}catch (SQLException e){
						logger.debug("KO data loaded using scrollable resultse tecnique",e );
						dataReader.setCalculateResultNumberEnabled(true);
						
					}
				}
			} else {
				logger.debug("Calculation of result set total number is NOT enabled");
				dataReader.setCalculateResultNumberEnabled(false);
			}

			dataStore = null;
			try {
				// read data
				dataStore = dataReader.read(resultSet);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
			}

			if (notCountingStrategyUsedSuccessfully) {
				dataStore.getMetaData().setProperty("resultNumber", new Integer(resultNumber));
			}

		} finally {
			try {
				releaseResources(connection, stmt, resultSet);
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to release allocated resources properly", t);
			}
		}

		return dataStore;
	}

	protected int getResultNumber(Connection connection) {
		logger.debug("IN");
		int resultNumber = 0;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sqlQuery = "SELECT COUNT(*) FROM (" + this.getStatement() + ") temptable";
			logger.debug("Executing query " + sqlQuery + " ...");
			stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(sqlQuery);
			rs.next();
			resultNumber = rs.getInt(1);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An error occurred while creating connection steatment", t);
		} finally {
			releaseResources(null, stmt, rs);
		}
		logger.debug("OUT : returning " + resultNumber);
		return resultNumber;
	}

	protected int getResultNumber(ResultSet resultSet) throws SQLException {
		logger.debug("IN");

		int rowcount = 0;
		if (resultSet.last()) {
			rowcount = resultSet.getRow();
			resultSet.beforeFirst(); // not rs.first() because the rs.next()
										// below will move on, missing the first
										// element
		}

		return rowcount;
	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	private void releaseResources(Connection connection, Statement statement, ResultSet resultSet) {

		logger.debug("IN");

		try {
			logger.debug("Relesing resources ...");
			if (resultSet != null) {
				try {
					resultSet.close();

				} catch (SQLException e) {
					throw new SpagoBIRuntimeException("Impossible to release [resultSet]", e);
				}
				logger.debug("[resultSet] released succesfully");
			}

			if (statement != null) {
				try {
					statement.close();

				} catch (SQLException e) {
					throw new SpagoBIRuntimeException("Impossible to release [statement]", e);
				}
				logger.debug("[statement] released succesfully");
			}

			if (connection != null) {
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
}
