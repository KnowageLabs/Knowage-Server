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
package it.eng.spagobi.utilities.logging;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.ErrorCode;

import it.eng.spagobi.commons.utilities.StringUtilities;

public class Log4jJNDIAppender extends JDBCAppender {

	private String jndi;

	public String getJndi() {
		return jndi;
	}

	public void setJndi(String jndi) {
		this.jndi = jndi;
	}

	@Override
	protected Connection getConnection() throws SQLException {
		if (StringUtilities.isEmpty(jndi)) {
			return super.getConnection();
		} else {
			try {
				return getJNDIConnection();
			} catch (NamingException e) {
				errorHandler.error("Cannot get connection from JNDI name [" + jndi + "]", e, ErrorCode.GENERIC_FAILURE);
				throw new RuntimeException("Cannot get connection from JNDI name [" + jndi + "]", e);
			}
		}
	}

	private Connection getJNDIConnection() throws NamingException, SQLException {
		Connection connection = null;
		Context ctx = new InitialContext();
		DataSource ds = (DataSource) ctx.lookup(jndi);
		connection = ds.getConnection();
		return connection;
	}

	@Override
	protected void closeConnection(Connection connection) {
		try {
			if (connection != null && !connection.isClosed())
				connection.close();
		} catch (SQLException e) {
			errorHandler.error("An error happened while closing connection", e, ErrorCode.CLOSE_FAILURE);
		}
	}

	protected void closeStatement(Statement stmt) {
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			errorHandler.error("An error happened while closing statement", e, ErrorCode.CLOSE_FAILURE);
		}
	}

	@Override
	protected void execute(String sql) throws SQLException {

		Connection con = null;
		Statement stmt = null;

		try {
			con = getConnection();
			stmt = con.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			errorHandler.error("An error happened while executing sql [" + sql + "]", e, ErrorCode.GENERIC_FAILURE);
			throw new RuntimeException("An error happened while executing sql [" + sql + "]", e);
		} finally {
			closeStatement(stmt);
			closeConnection(con);
		}
	}

}
