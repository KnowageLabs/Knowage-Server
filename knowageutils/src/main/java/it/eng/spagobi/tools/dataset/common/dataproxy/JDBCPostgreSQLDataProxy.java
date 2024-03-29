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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JDBCPostgreSQLDataProxy extends JDBCDataProxy {

	private static transient Logger logger = Logger.getLogger(JDBCPostgreSQLDataProxy.class);

	private String statement;

	@Override
	public String getStatement() {
		return statement;
	}

	@Override
	public void setStatement(String statement) {
		this.statement = statement;
	}

	public JDBCPostgreSQLDataProxy(int offsetParam, int fetchSizeParam) {
		this.setCalculateResultNumberOnLoad(true);
		this.offset = offsetParam;
		this.fetchSize = fetchSizeParam;
	}

	public JDBCPostgreSQLDataProxy() {
		this.setCalculateResultNumberOnLoad(true);
	}

	public JDBCPostgreSQLDataProxy(IDataSource dataSource, String statement, int offsetParam, int fetchSizeParam) {
		this(offsetParam, fetchSizeParam);
		setDataSource(dataSource);
		setStatement(statement);
	}

	public JDBCPostgreSQLDataProxy(IDataSource dataSource, int offsetParam, int fetchSizeParam) {
		this(offsetParam, fetchSizeParam);
		setDataSource(dataSource);
		setStatement(statement);
	}

	public JDBCPostgreSQLDataProxy(IDataSource dataSource) {
		setDataSource(dataSource);
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
		PreparedStatement stmt;
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
			
				
			
			String sqlQuery = "";
			try {
				sqlQuery = getFinalStatement();
				stmt = connection.prepareStatement(sqlQuery);
				// get max size
				if (getMaxResults() > 0) {
					stmt.setMaxRows(getMaxResults());
				}
				
				logger.info("Executing query " + sqlQuery + " ...");
				resultSet = stmt.executeQuery();
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
					logger.error("KO Calculation of result set total number using inlineview", t);
					throw new SpagoBIRuntimeException("An error occurred while getting total result number: " + sqlQuery, t);
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
		PreparedStatement stmt = null;

		ResultSet rs = null;

		try {
			String tableAlias = "temptable";
			String sqlQuery = "SELECT COUNT(*) FROM (" + getStatement() + ") " + tableAlias;
			logger.info("Executing query " + sqlQuery + " ...");
			stmt = connection.prepareStatement(sqlQuery,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery();
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

	@Override
	public ResultSet getData(IDataReader dataReader, Object... resources) {
		logger.debug("IN");
		Statement stmt = (Statement) resources[0];
		ResultSet resultSet = null;
		try {
			if (getMaxResults() > 0) {
				stmt.setMaxRows(getMaxResults());
			}
			String sqlQuery = getFinalStatement();
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
		return true;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return true;
	}

	private String getFinalStatement() {

		if (fetchSize == -1) {
			if (!this.statement.isEmpty()) {
				this.statement = removeLastSemicolon(this.statement);
				return this.statement;
			}
		}

		StringBuilder newStatement = new StringBuilder();
		if (!this.statement.isEmpty()) {
			this.statement = removeLastSemicolon(this.statement);

			newStatement.append("SELECT * FROM (").append(this.statement).append(") t");

			if (offset > 0) {
				newStatement.append(" OFFSET " + offset);
			}

			if (fetchSize > 0) {
				newStatement.append(" FETCH NEXT " + fetchSize + " ROWS ONLY");
			}

		}

		return newStatement.toString();
	}

}
