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
package it.eng.spagobi.tools.scheduler.init;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

public class QuartzInitializer implements InitializerIFace {

	private static final String PROPERTY_DATASOURCE_JNDI = "org.quartz.dataSource.quartz.jndiURL";
	private static final String PROPERTY_DELEGATE_CLASS = "org.quartz.jobStore.driverDelegateClass";

	private static final String JDBC_HSQLDB = "jdbc:hsqldb";
	private static final String JDBC_INGRES = "jdbc:ingres";
	private static final String JDBC_ORACLE = "jdbc:oracle";
	private static final String JDBC_POSTGRESQL = "jdbc:postgresql";
	private static final String JDBC_MYSQL = "jdbc:mysql";
	private static final String JDBC_MARIADB = "jdbc:mariadb";
	private static final String JDBC_SQLSERVER = "jdbc:sqlserver";

	private static final Map<String, String> JDBC_URL_PREFIX_2_DELEGATE_CLASS = new HashMap<String, String>();

	static {
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_MYSQL, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_MARIADB, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_SQLSERVER, "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_POSTGRESQL, "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_ORACLE, "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_INGRES, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		JDBC_URL_PREFIX_2_DELEGATE_CLASS.put(JDBC_HSQLDB, "org.quartz.impl.jdbcjobstore.HSQLDBDelegate");
	}

	public static transient Logger logger = Logger.getLogger(QuartzInitializer.class);

	private SourceBean _config = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean config) {
		StdSchedulerFactory stdSchedFact = new StdSchedulerFactory();
		Properties properties = new Properties();
		try {
			Thread currThread = Thread.currentThread();
			ClassLoader classLoader = currThread.getContextClassLoader();
			InputStream propIs = classLoader.getResourceAsStream("quartz.properties");
			properties.load(propIs);

			String figuredOutValue = null;

			if (properties.containsKey(PROPERTY_DELEGATE_CLASS)) {
				logger.info("Quartz delegate class set to " + properties.get(PROPERTY_DELEGATE_CLASS));
			} else {
				logger.warn("Property " + PROPERTY_DELEGATE_CLASS + " not set! Trying to figure out what delegate class needs to be used...");
				determineDelegateClass(properties);
			}

			stdSchedFact.initialize(properties);
			Scheduler sched = stdSchedFact.getScheduler();
			sched.start();
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "init", "Error while initializing scheduler " + e);
		}
	}

	/**
	 * Try to figure out which Delegate class to use.
	 *
	 * @param properties Actual Quartz configuration
	 */
	private void determineDelegateClass(Properties properties) {
		String figuredOutValue;
		String datasourceJndi = properties.getProperty(PROPERTY_DATASOURCE_JNDI);

		if (datasourceJndi == null) {
			throw new IllegalStateException("No value for property org.quartz.dataSource.quartz.jndiURL");
		}

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

			if (!JDBC_URL_PREFIX_2_DELEGATE_CLASS.containsKey(urlPrefix)) {
				throw new IllegalStateException("Prefix " + urlPrefix + " doesn't have a matching delegate class.");
			}

			figuredOutValue = JDBC_URL_PREFIX_2_DELEGATE_CLASS.get(urlPrefix);

			logger.info("Quartz will be initialized with the delegate class " + figuredOutValue);
			properties.put(PROPERTY_DELEGATE_CLASS, figuredOutValue);

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
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	@Override
	public SourceBean getConfig() {
		return _config;
	}

}
