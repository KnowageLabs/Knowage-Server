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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import it.eng.qbe.datasource.transaction.hibernate.HibernateTransaction;
import it.eng.spagobi.commons.dao.DAOConfig;

public class HibernateSessionManager {

	private static final String PROPERTY_DATASOURCE_JNDI = "hibernate.connection.datasource";
	private static final String PROPERTY_DIALECT = "hibernate.dialect";

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
	private static final String JDBC_MARIADB = "jdbc:mariadb";
	private static final String JDBC_SQLSERVER = "jdbc:sqlserver";

	public static final Map<String, String> JDBC_URL_PREFIX_2_DIALECT = new HashMap<String, String>();

	static {
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_MYSQL, DIALECT_MYSQL);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_MARIADB, DIALECT_MYSQL);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_SQLSERVER, DIALECT_SQLSERVER);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_POSTGRESQL, DIALECT_POSTGRE);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_ORACLE, DIALECT_ORACLE);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_INGRES, DIALECT_INGRES);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_HSQLDB, DIALECT_HSQL);
		JDBC_URL_PREFIX_2_DIALECT.put(JDBC_DB2, DIALECT_DB2);
	}

	public static transient Logger logger = Logger.getLogger(HibernateSessionManager.class);

	private static SessionFactory sessionFactory;

	private static void initSessionFactory() {
		logger.info("Initializing hibernate Session Factory Described by [" + DAOConfig.getHibernateConfigurationFile() + "]");

		Configuration conf = setDialectPropertyToConfiguration();

		sessionFactory = conf.buildSessionFactory();

	}

	private static Configuration setDialectPropertyToConfiguration() {
		Configuration conf = new Configuration();
		File hibernateConfigurationFileFile = DAOConfig.getHibernateConfigurationFileFile();
		if (hibernateConfigurationFileFile != null) {
			// for testing
			conf = conf.configure(hibernateConfigurationFileFile);
		} else {
			conf = conf.configure(DAOConfig.getHibernateConfigurationFile());
		}

		String figuredOutValue = conf.getProperty(PROPERTY_DIALECT);

		if (figuredOutValue != null) {
			logger.info("Hibernate configuration set dialect to " + figuredOutValue);
		} else {
			logger.warn("Property hibernate.dialect not set! Trying to figure out what dialect needs to be used...");
			determineDialect(conf);
		}
		return conf;
	}

	public static String getDialect() {
		logger.info("Initializing hibernate Session Factory Described by [" + DAOConfig.getHibernateConfigurationFile() + "]");

		Configuration conf = setDialectPropertyToConfiguration();

		return conf.getProperty(PROPERTY_DIALECT);
	}

	/**
	 * Try to figure out which Hibernate dialect to use.
	 *
	 * @param conf Actual Hibernate configuration
	 */
	private static void determineDialect(Configuration conf) {
		String datasourceJndi = conf.getProperty(PROPERTY_DATASOURCE_JNDI);

		if (datasourceJndi == null) {
			throw new IllegalStateException("The property hibernate.connection.datasource is not set in file");
		}

		String figuredOutValue = getDialect(datasourceJndi);
		logger.warn("Property hibernate.dialect set to " + figuredOutValue);
		conf.setProperty(PROPERTY_DIALECT, figuredOutValue);

	}

	public static String determineDialectFromJNDIResource(String datasourceJndi) {

		if (datasourceJndi == null) {
			throw new IllegalStateException("The property hibernate.connection.datasource is not set in file");
		}

		String figuredOutValue = getDialect(datasourceJndi);
		logger.warn("Property hibernate.dialect set to " + figuredOutValue);

		return figuredOutValue;
	}

	private static String getDialect(String datasourceJndi) {
		String figuredOutValue = null;

		Connection connection = null;
		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(datasourceJndi);
			connection = ds.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			String url = metaData.getURL();

			Pattern jdbcPattern = Pattern.compile("(jdbc:[^:]+).+");
			Matcher matcher = jdbcPattern.matcher(url);
			matcher.matches();
			String urlPrefix = matcher.group(1);

			if (!JDBC_URL_PREFIX_2_DIALECT.containsKey(urlPrefix)) {
				throw new IllegalStateException("Prefix " + urlPrefix + " doesn't have a matching dialect.");
			}

			figuredOutValue = JDBC_URL_PREFIX_2_DIALECT.get(urlPrefix);
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
		return figuredOutValue;

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
