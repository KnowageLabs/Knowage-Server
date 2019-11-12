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
package it.eng.spagobi.commons.utilities;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import it.eng.qbe.datasource.transaction.hibernate.HibernateTransaction;
import it.eng.spagobi.commons.dao.DAOConfig;

public class HibernateSessionManager {

	private static final String DIALECT_DB2 = "org.hibernate.dialect.DB2400Dialect";
	private static final String DIALECT_HSQL = "org.hibernate.dialect.HSQLDialect";
	private static final String DIALECT_INGRES = "org.hibernate.dialect.IngresDialect";
	private static final String DIALECT_ORACLE = "org.hibernate.dialect.Oracle9Dialect";
	private static final String DIALECT_POSTGRE = "org.hibernate.dialect.PostgreSQLDialect";
	private static final String DIALECT_SQLSERVER = "org.hibernate.dialect.SQLServerDialect";
	private static final String DIALECT_MYSQL = "org.hibernate.dialect.MySQLDialect";

	private static final String JDBC_DB2 = "jdbc:db2";
	private static final String JDBC_HSQLDB = "jdbc:hsqldb";
	private static final String JDBC_INGRES = "jdbc:ingres";
	private static final String JDBC_ORACLE = "jdbc:oracle";
	private static final String JDBC_POSTGRESQL = "jdbc:postgresql";
	private static final String JDBC_MYSQL = "jdbc:mysql";
	private static final String JDBC_SQLSERVER = "jdbc:sqlserver";

	public static transient Logger logger = Logger.getLogger(HibernateSessionManager.class);

	private static SessionFactory sessionFactory;

	private static void initSessionFactory() {
		logger.info("Initializing hibernate Session Factory Described by [" + DAOConfig.getHibernateConfigurationFile() + "]");

		Connection connection = null;
		String hibernateDialect = null;
		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/knowage");
			connection = ds.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			String url = metaData.getURL();

			if (url.startsWith(JDBC_MYSQL)) {
				hibernateDialect = DIALECT_MYSQL;
			} else if (url.startsWith(JDBC_SQLSERVER)) {
				hibernateDialect = DIALECT_SQLSERVER;
			} else if (url.startsWith(JDBC_POSTGRESQL)) {
				hibernateDialect = DIALECT_POSTGRE;
			} else if (url.startsWith(JDBC_ORACLE)) {
				hibernateDialect = DIALECT_ORACLE;
			} else if (url.startsWith(JDBC_INGRES)) {
				hibernateDialect = DIALECT_INGRES;
			} else if (url.startsWith(JDBC_HSQLDB)) {
				hibernateDialect = DIALECT_HSQL;
			} else if (url.startsWith(JDBC_DB2)) {
				hibernateDialect = DIALECT_DB2;
			} else {
				throw new IllegalStateException("No Hibernate's dialect for URL: " + url);
			}

		} catch (Exception e) {
			logger.error("Error determining Hibernate's dialect", e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// Yes, it's mute!
				}
			}
		}

		if (hibernateDialect == null) {
			throw new IllegalStateException("Cannot determine Hibernate's dialect. See previous log.");
		}

		Configuration conf = new Configuration();

		logger.info("Session manager will be initialized with the dialect: " + hibernateDialect);
		conf.setProperty("hibernate.dialect", hibernateDialect);

		File hibernateConfigurationFileFile = DAOConfig.getHibernateConfigurationFileFile();
		if (hibernateConfigurationFileFile != null) {
			// for testing
			conf = conf.configure(hibernateConfigurationFileFile);
		} else {
			conf = conf.configure(DAOConfig.getHibernateConfigurationFile());
		}
		sessionFactory = conf.buildSessionFactory();
	}

	private synchronized static SessionFactory getSessionFactory() {
		if (HibernateSessionManager.sessionFactory == null) {
			initSessionFactory();
		}
		return HibernateSessionManager.sessionFactory;
	}

	/**
	 * Current session.
	 *
	 * @return the session
	 */
	public static Session getCurrentSession() {
		return getSessionFactory().openSession();
	}

	/**
	 * Retrieve current session
	 *
	 * @return
	 */
	public static Session getExistingSession() {
		return getSessionFactory().getCurrentSession();
	}

	public static Connection getConnection(Session session) {
		return HibernateTransaction.getConnection(session);
	}

}
