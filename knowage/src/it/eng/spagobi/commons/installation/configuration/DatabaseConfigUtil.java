package it.eng.spagobi.commons.installation.configuration;

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class DatabaseConfigUtil extends AbstractHibernateDAO {
	static private Logger logger = Logger.getLogger(DatabaseConfigUtil.class);

	public void readTables(List<String> tables, String pathToSave) throws SpagoBIRuntimeException {
		Session aSession = null;
		Connection jdbcConnection = null;

		try {
			aSession = getSession();
			jdbcConnection = HibernateSessionManager.getConnection(aSession);
			jdbcConnection.setAutoCommit(false);

			String str = null;

			for (String table : tables) {
				str = "SELECT * FROM " + table;
				PreparedStatement statement = jdbcConnection.prepareStatement(str);

				ResultSet rs = statement.executeQuery();
				ConfigManagementUtil.convertResultSetToCSV(rs, table, pathToSave);
				statement.close();
			}

		} catch (SQLException e) {

			logger.error("Error while reading content of reqested tables");
			throw new SpagoBIRuntimeException(e);
		} catch (FileNotFoundException e) {

			logger.error("Error while reading content of reqested tables");
			throw new SpagoBIRuntimeException(e);
		} finally {

			try {
				if (jdbcConnection != null && !jdbcConnection.isClosed()) {
					jdbcConnection.close();
				}
			} catch (SQLException e) {
				logger.error("Error while reading content of reqested tables");
				throw new SpagoBIRuntimeException(e);
			}

			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
	}

}
