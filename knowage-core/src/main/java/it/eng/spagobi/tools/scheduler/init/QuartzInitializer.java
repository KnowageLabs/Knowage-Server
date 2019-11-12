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
import java.util.Properties;

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

	private static final String JDBC_HSQLDB = "jdbc:hsqldb";
	private static final String JDBC_INGRES = "jdbc:ingres";
	private static final String JDBC_ORACLE = "jdbc:oracle";
	private static final String JDBC_POSTGRESQL = "jdbc:postgresql";
	private static final String JDBC_MYSQL = "jdbc:mysql";
	private static final String JDBC_SQLSERVER = "jdbc:sqlserver";

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

			String jndiUrl = properties.getProperty("org.quartz.dataSource.quartz.jndiURL");

			if (jndiUrl == null) {
				throw new IllegalStateException("No value for property org.quartz.dataSource.quartz.jndiURL");
			}

			Connection connection = null;
			String driverDelegateClass = null;
			try {
				InitialContext ctx = new InitialContext();
				DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/knowage");
				connection = ds.getConnection();
				DatabaseMetaData metaData = connection.getMetaData();
				String url = metaData.getURL();

				if (url.startsWith(JDBC_MYSQL)) {
					driverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
				} else if (url.startsWith(JDBC_SQLSERVER)) {
					driverDelegateClass = "org.quartz.impl.jdbcjobstore.MSSQLDelegate";
				} else if (url.startsWith(JDBC_POSTGRESQL)) {
					driverDelegateClass = "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
				} else if (url.startsWith(JDBC_ORACLE)) {
					// TODO : Oracle in WEBLOGIC org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate
					driverDelegateClass = "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate";
				} else if (url.startsWith(JDBC_INGRES)) {
					driverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
				} else if (url.startsWith(JDBC_HSQLDB)) {
					driverDelegateClass = "org.quartz.impl.jdbcjobstore.HSQLDBDelegate";
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

			if (driverDelegateClass == null) {
				throw new IllegalStateException("Cannot determine driver delegate class. See previous log.");
			}

			logger.info("Quartz will be initialized with the delegate: " + driverDelegateClass);
			properties.put("org.quartz.jobStore.driverDelegateClass", driverDelegateClass);

			stdSchedFact.initialize(properties);
			Scheduler sched = stdSchedFact.getScheduler();
			sched.start();
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "init", "Error while initializing scheduler " + e);
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
