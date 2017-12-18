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

import it.eng.qbe.datasource.transaction.hibernate.HibernateTransaction;
import it.eng.spagobi.commons.dao.DAOConfig;

import java.io.File;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionManager {

	public static transient Logger logger = Logger.getLogger(HibernateSessionManager.class);

	private static SessionFactory sessionFactory;

	// static {
	// try {
	// String fileCfg = "hibernate.cfg.xml";
	// fileCfg = fileCfg.trim();
	// logger.info( "Initializing hibernate Session Factory Described by [" + fileCfg +"]");
	// Configuration conf = new Configuration();
	// conf = conf.configure(fileCfg);
	// sessionFactory = conf.buildSessionFactory();
	//
	// } catch (Throwable ex) {
	// // Make sure you log the exception, as it might be swallowed
	// logger.error("Initial SessionFactory creation failed.", ex);
	// throw new ExceptionInInitializerError(ex);
	// }
	// }

	private synchronized static void initSessionFactory() {
		logger.info("Initializing hibernate Session Factory Described by [" + DAOConfig.getHibernateConfigurationFile() + "]");
		Configuration conf = new Configuration();
		File hibernateConfigurationFileFile = DAOConfig.getHibernateConfigurationFileFile();
		if (hibernateConfigurationFileFile != null) {
			// for testing
			conf = conf.configure(hibernateConfigurationFileFile);
		} else {
			conf = conf.configure(DAOConfig.getHibernateConfigurationFile());
		}
		sessionFactory = conf.buildSessionFactory();
	}

	private static SessionFactory getSessionFactory() {
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
