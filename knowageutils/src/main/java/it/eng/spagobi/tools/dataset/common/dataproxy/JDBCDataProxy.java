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
package it.eng.spagobi.tools.dataset.common.dataproxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.sql.SqlUtils;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JDBCDataProxy extends AbstractDataProxy {

	private IDataSource dataSource;
	private String statement;
	private String schema;

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

			Monitor timeToGetConnection = MonitorFactory.start("Knowage.JDBCDataProxy.gettingJDBCConnection");
			logger.debug("Retrieving JDBC connection...");
			try {
				connection = getDataSource().getConnection();
			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while creating connection", t);
			} finally {
				timeToGetConnection.stop();
			}
			logger.debug("Got JDBC connection.");

			String dialect = dataSource.getHibDialectClass();
			DatabaseDialect databaseDialect = DatabaseDialect.get(dialect);
			Assert.assertNotNull(dialect, "Database dialect cannot be null");
			try {
				// ATTENTION: For the most db sets the stmt as a scrollable
				// stmt, only for the compatibility with Ingres sets
				// a stmt forward only
				if (databaseDialect.equals(DatabaseDialect.HIVE) || databaseDialect.equals(DatabaseDialect.HIVE2)
						|| databaseDialect.equals(DatabaseDialect.CASSANDRA) || databaseDialect.equals(DatabaseDialect.IMPALA)
						|| databaseDialect.equals(DatabaseDialect.ORIENT) || databaseDialect.equals(DatabaseDialect.SPARKSQL)
						|| databaseDialect.equals(DatabaseDialect.VERTICA) || databaseDialect.equals(DatabaseDialect.REDSHIFT)
						|| databaseDialect.equals(DatabaseDialect.BIGQUERY) || databaseDialect.equals(DatabaseDialect.SYNAPSE)) {
					stmt = connection.createStatement();
				} else {
					stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				}
			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while creating connection steatment", t);
			}

			String sqlQuery = "";
			try {
				// get max size
				if (getMaxResults() > 0) {
					stmt.setMaxRows(getMaxResults());
				}
				sqlQuery = getStatement();
				LogMF.info(logger, "Executing query:\n{0}", sqlQuery);
				Monitor timeToExecuteStatement = MonitorFactory.start("Knowage.JDBCDataProxy.executeStatement:" + sqlQuery);
				try {
					resultSet = stmt.executeQuery(sqlQuery);
				} finally {
					timeToExecuteStatement.stop();
				}
				LogMF.debug(logger, "Query has been executed:\n{0}", sqlQuery);
			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while executing statement: " + sqlQuery, t);
			}

			int resultNumber = -1;
			if (isCalculateResultNumberOnLoadEnabled()) {
				logger.debug("Calculation of result set total number is enabled");
				try {
					// if its an hive like db the query can be very slow so it's better to execute it just once and not use the inline view tecnique
					if (SqlUtils.isHiveLikeDialect(dialect)) {
						logger.debug("It's a BigData datasource so count data iterating result set till max");
						dataReader.setCalculateResultNumberEnabled(true);
					} else if (dataReader.isPaginationSupported() && !dataReader.isPaginationRequested()) {
						// we need to load entire resultset, therefore there is no need to use the inline view tecnique
						logger.debug("Offset = 0, fetch size = -1: the entire resultset will be loaded, no need to use the inline view tecnique");
						dataReader.setCalculateResultNumberEnabled(true);
					} else {
						// try to calculate the query total result number using inline view tecnique
						resultNumber = getResultNumber(connection);
						logger.debug("Calculation of result set total number successful : resultNumber = " + resultNumber);
						// ok, no need to ask the datareader to calculate the query total result number
						dataReader.setCalculateResultNumberEnabled(false);
					}
				} catch (Exception t) {
					logger.debug("KO Calculation of result set total number using inlineview", t);
					try {
						logger.debug("Loading data using scrollable resultset tecnique");
						resultNumber = getResultNumber(resultSet);
						logger.debug("OK data loaded using scrollable resultset tecnique : resultNumber = " + resultNumber);
						dataReader.setCalculateResultNumberEnabled(false);
					} catch (SQLException e) {
						logger.debug("KO data loaded using scrollable resultset tecnique", e);
						dataReader.setCalculateResultNumberEnabled(true);
					}
				}
			} else {
				logger.debug("Calculation of result set total number is NOT enabled");
				dataReader.setCalculateResultNumberEnabled(false);
			}

			dataStore = null;
			Monitor timeToGetDataStore = MonitorFactory.start("Knowage.JDBCDataProxy.getDataStore:" + sqlQuery);
			LogMF.debug(logger, "Getting datastore for SQL query:\n{0}", sqlQuery);
			try {
				// read data
				dataStore = dataReader.read(resultSet);
			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
			} finally {
				timeToGetDataStore.stop();
			}
			LogMF.debug(logger, "Got datastore for SQL query:\n{0}", sqlQuery);

			if (resultNumber > -1) { // it means that resultNumber was successfully calculated by this data proxy
				int limitedResultNumber = getMaxResults() > 0 && resultNumber > getMaxResults() ? getMaxResults() : resultNumber;
				dataStore.getMetaData().setProperty("resultNumber", new Integer(limitedResultNumber));
			}

		} finally {
			try {
				releaseResources(connection, stmt, resultSet);
			} catch (Exception t) {
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

		String statement = this.getStatement();
		// if db is SQL server the query nees to be modified in case it contains ORDER BY clause

		String dialect = dataSource.getHibDialectClass();
		logger.debug("Dialect is " + dialect);

		if (dialect.toUpperCase().contains("SQLSERVER") && statement.toUpperCase().contains("ORDER BY")) {
			logger.debug("we are in SQL SERVER and ORDER BY case");
			statement = modifySQLServerQuery(statement);
		}
		try {
			String tableAlias = "";
			if (!dialect.toLowerCase().contains("orient")) {
				tableAlias = "temptable";
			}
			String sqlQuery = "SELECT COUNT(*) FROM (" + statement + ") " + tableAlias;
			stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			LogMF.info(logger, "Executing count statement, SQL query:\n{0}", sqlQuery);
			Monitor timeToExecuteStatement = MonitorFactory.start("Knowage.JDBCDataProxy.executeCountStatement:" + sqlQuery);
			try {
				rs = stmt.executeQuery(sqlQuery);
			} finally {
				timeToExecuteStatement.stop();
			}
			LogMF.debug(logger, "Executed count statement, SQL query:\n{0}", sqlQuery);
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

	private String modifySQLServerQuery(String statement) {
		logger.debug("IN");
		int selectIndex = statement.toUpperCase().indexOf("SELECT");
		String noSelect = statement.substring(selectIndex + 6);
		logger.debug("No Select Query " + noSelect);
		// remove spaces
		noSelect = noSelect.trim();
		logger.debug("No Select trimmed query " + noSelect);

		int distinctIndex = noSelect.toUpperCase().indexOf("DISTINCT ");
		boolean distinct = false;
		if (distinctIndex == 0) {
			logger.debug("Remove distinct clause");
			distinct = true;
			noSelect = noSelect.substring(distinctIndex + 8);
			noSelect = noSelect.trim();
			logger.debug("No dstinct trimmetd query " + noSelect);
		}

		// remove also distinct if present
		String prefix = "";
		if (distinct) {
			prefix = "select distinct TOP(100) PERCENT ";
		} else {
			prefix = "select TOP(100) PERCENT ";

		}
		statement = prefix + noSelect;
		logger.debug("Statement for SQL SERVER " + statement);
		return statement;
	}

	protected int getResultNumber(ResultSet resultSet) throws SQLException {
		logger.debug("IN");
		LogMF.debug(logger, "Moving into last record for statement:\n{0}", resultSet.getStatement().toString());
		int rowcount = 0;
		if (resultSet.last()) {
			rowcount = resultSet.getRow();
			resultSet.beforeFirst(); // not rs.first() because the rs.next()
										// below will move on, missing the first
										// element
		}
		LogMF.debug(logger, "Moved into last record for statement:\n{0}", resultSet.getStatement().toString());
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

	@Override
	public ResultSet getData(IDataReader dataReader, Object... resources) {
		logger.debug("IN");
		Statement stmt = (Statement) resources[0];
		ResultSet resultSet = null;
		try {
			if (getMaxResults() > 0) {
				stmt.setMaxRows(getMaxResults());
			}
			String sqlQuery = getStatement();
			LogMF.info(logger, "Executing query:\n{0}", sqlQuery);
			Monitor timeToExecuteStatement = MonitorFactory.start("Knowage.JDBCDataProxy.executeStatement:" + sqlQuery);
			try {
				resultSet = stmt.executeQuery(sqlQuery);
			} finally {
				timeToExecuteStatement.stop();
			}
			LogMF.debug(logger, "Executed query:\n{0}", sqlQuery);
			return resultSet;
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			logger.debug("OUT");
		}
	}

	protected final String removeLastSemicolon(String query) {
		Assert.assertNotNull(query, "The query parameter cannot be null");

		// This replaceAll(), in fact, replace only the last: see the $ in the regex.
		return query.replaceAll(";\\s*$", "");
	}
}
