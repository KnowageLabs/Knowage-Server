/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021-present Engineering Ingegneria Informatica S.p.A.
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

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Marco Balestri
 */
public class JDBCSynapseDataProxy extends JDBCDataProxy {

	private IDataSource dataSource;
	private String statement;
	private String schema;
	private int fetchSize;
	private int offset;

	private static transient Logger logger = Logger.getLogger(JDBCSynapseDataProxy.class);

	public JDBCSynapseDataProxy(int offsetParam, int fetchSizeParam) {
		this.setCalculateResultNumberOnLoad(true);
		this.offset = offsetParam;
		this.fetchSize = fetchSizeParam;
	}

	public JDBCSynapseDataProxy() {
		this.setCalculateResultNumberOnLoad(true);
	}

	public JDBCSynapseDataProxy(IDataSource dataSource, String statement, int offsetParam, int fetchSizeParam) {
		this(offsetParam, fetchSizeParam);
		setDataSource(dataSource);
		setStatement(statement);
	}

	public JDBCSynapseDataProxy(IDataSource dataSource, int offsetParam, int fetchSizeParam) {
		this(offsetParam, fetchSizeParam);
		setDataSource(dataSource);
		setStatement(statement);
	}

	public JDBCSynapseDataProxy(IDataSource dataSource) {
		setDataSource(dataSource);
	}

	@Override
	public String getSchema() {
		return schema;
	}

	@Override
	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
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
				connection = getDataSource().getConnection();
			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while creating connection", t);
			}
			String dialect = dataSource.getHibDialectClass();
			Assert.assertNotNull(dialect, "Database dialect cannot be null");
			try {

				stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

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
				logger.info("Executing query " + sqlQuery + " ...");
				resultSet = stmt.executeQuery(sqlQuery);

			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while executing statement: " + sqlQuery, t);
			}

			int resultNumber = -1;
			if (isCalculateResultNumberOnLoadEnabled()) {
				logger.debug("Calculation of result set total number is enabled");
				try {

					// try to calculate the query total result number using inline view tecnique
					resultNumber = getResultNumber(connection);
					logger.debug("Calculation of result set total number successful : resultNumber = " + resultNumber);
					// ok, no need to ask the datareader to calculate the query total result number
					dataReader.setCalculateResultNumberEnabled(false);

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
			try {
				// read data
				dataStore = dataReader.read(resultSet);
			} catch (Exception t) {
				throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
			}

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

	@Override
	protected int getResultNumber(Connection connection) {
		logger.debug("IN");
		int resultNumber = 0;
		Statement stmt = null;

		ResultSet rs = null;

		String statement = getStatement();
		// if db is SQL server the query nees to be modified in case it contains ORDER BY clause
		if (statement.toUpperCase().contains("ORDER BY")) {
			logger.debug("we are in SQL SERVER and ORDER BY case");
			statement = modifySQLServerQuery(statement);
		}

		String dialect = dataSource.getHibDialectClass();
		logger.debug("Dialect is " + dialect);

		try {
			String tableAlias = "";
			if (!dialect.toLowerCase().contains("orient")) {
				tableAlias = "temptable";
			}
			String sqlQuery = "SELECT COUNT(*) FROM (" + getOldStatement() + ") " + tableAlias;
			logger.info("Executing query " + sqlQuery + " ...");
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

	@Override
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

	@Override
	public IDataSource getDataSource() {
		return dataSource;
	}

	@Override
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
			logger.info("Executing query " + sqlQuery + " ...");
			resultSet = stmt.executeQuery(sqlQuery);
			return resultSet;
		} catch (SQLException e) {
			throw new SpagoBIRuntimeException(e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public boolean isOffsetSupported() {
		return false;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return false;
	}

	@Override
	public String getStatement() {
		return new String(this.statement);
	}

	public String getOldStatement() {
		return statement;
	}

	@Override
	public void setStatement(String statement) {
		this.statement = statement;
	}

	@Override
	public int getFetchSize() {
		return fetchSize;
	}

	@Override
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
