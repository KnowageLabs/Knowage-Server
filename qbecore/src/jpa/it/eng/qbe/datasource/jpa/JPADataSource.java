/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.datasource.ConnectionDescriptor;
import it.eng.qbe.datasource.IPersistenceManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.transaction.ITransaction;
import it.eng.qbe.datasource.transaction.jpa.JPAEclipseLinkTransaction;
import it.eng.qbe.datasource.transaction.jpa.JPAHibernateTransaction;
import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.builder.IModelStructureBuilder;
import it.eng.qbe.model.structure.builder.jpa.JPAModelStructureBuilder;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JPADataSource extends AbstractDataSource implements IJpaDataSource {

	private EntityManagerFactory factory;
	private final boolean classLoaderExtended = false;

	private static transient Logger logger = Logger.getLogger(JPADataSource.class);

	protected JPADataSource(String dataSourceName, IDataSourceConfiguration configuration) {
		logger.debug("Creating a new JPADataSource");
		setName(dataSourceName);
		dataMartModelAccessModality = new AbstractModelAccessModality();

		// validate and set configuration
		if (configuration instanceof FileDataSourceConfiguration) {
			this.configuration = configuration;
		} else if (configuration instanceof CompositeDataSourceConfiguration) {
			IDataSourceConfiguration subConf = ((CompositeDataSourceConfiguration) configuration).getSubConfigurations().get(0);
			if (subConf instanceof FileDataSourceConfiguration) {
				this.configuration = subConf;
				this.configuration.loadDataSourceProperties().putAll(configuration.loadDataSourceProperties());
			} else {
				Assert.assertUnreachable("Not suitable configuration to create a JPADataSource");
			}
		} else {
			Assert.assertUnreachable("Not suitable configuration to create a JPADataSource");
		}
		logger.debug("Created a new JPADataSource");
	}

	public FileDataSourceConfiguration getFileDataSourceConfiguration() {
		return (FileDataSourceConfiguration) configuration;
	}

	protected void initEntityManagerFactory(String name) {
		factory = Persistence.createEntityManagerFactory(name, buildEmptyConfiguration());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.eng.qbe.datasource.IHibernateDataSource#getSessionFactory(java.lang
	 * .String)
	 */
	public EntityManagerFactory getEntityManagerFactory(String dmName) {
		return getEntityManagerFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.qbe.datasource.jpa.IJPAataSource#getEntityManagerFactory()
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		if (factory == null) {
			open();
		}
		return factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.qbe.datasource.jpa.IJPAataSource#getEntityManager()
	 */
	public EntityManager getEntityManager() {
		if (factory == null) {
			open();
		}
		EntityManager entityManager = factory.createEntityManager();
		return entityManager;
	}

	public void open() {
		File jarFile = null;

		FileDataSourceConfiguration configuration = getFileDataSourceConfiguration();

		jarFile = configuration.getFile();
		if (jarFile == null) {
			return;
		}

		if (!classLoaderExtended) {
			updateCurrentClassLoader(jarFile);
		}

		initEntityManagerFactory(getConfiguration().getModelName());

	}

	public boolean isOpen() {
		return factory != null;
	}

	public void close() {
		factory = null;
	}

	@Override
	public IDataSource getToolsDataSource() {
		IDataSource dataSource = (IDataSource) configuration.loadDataSourceProperties().get("datasource");
		// FOR SPAGOBIMETA
		if (dataSource == null) {
			ConnectionDescriptor connectionDescriptor = (ConnectionDescriptor) configuration.loadDataSourceProperties().get("connection");
			if (connectionDescriptor != null) {
				dataSource = connectionDescriptor.getDataSource();
			}
		}
		return dataSource;
	}

	public IModelStructure getModelStructure() {
		IModelStructureBuilder structureBuilder;
		if (dataMartModelStructure == null) {
			structureBuilder = new JPAModelStructureBuilder(this);
			dataMartModelStructure = structureBuilder.build();
		}

		return dataMartModelStructure;
	}

	protected Map<String, Object> buildEmptyConfiguration() {
		Map<String, Object> cfg = new HashMap<String, Object>();
		String dialect = getToolsDataSource().getHibDialectClass();

		// to solve http://spagoworld.org/jira/browse/SPAGOBI-1934
		if (dialect != null && dialect.contains("SQLServerDialect")) {
			dialect = "org.hibernate.dialect.ExtendedSQLServerDialect";
		}

		// at the moment (04/2015) hibernate doesn't provide a dialect for hive or hbase with phoenix. But its similar to the postrges one
		if (SqlUtils.isHiveLikeDialect(dialect)) {
			dialect = "org.hibernate.dialect.PostgreSQLDialect";
		}
		
		if (getToolsDataSource().checkIsJndi()) {
			cfg.put("javax.persistence.nonJtaDataSource", getToolsDataSource().getJndi());
			cfg.put("hibernate.dialect", dialect);
			cfg.put("hibernate.validator.apply_to_ddl", "false");
			cfg.put("hibernate.validator.autoregister_listeners", "false");
		} else {
			cfg.put("javax.persistence.jdbc.url", getToolsDataSource().getUrlConnection());
			cfg.put("javax.persistence.jdbc.password", getToolsDataSource().getPwd());
			cfg.put("javax.persistence.jdbc.user", getToolsDataSource().getUser());
			cfg.put("javax.persistence.jdbc.driver", getToolsDataSource().getDriver());
			cfg.put("hibernate.dialect", dialect);
			cfg.put("hibernate.validator.apply_to_ddl", "false");
			cfg.put("hibernate.validator.autoregister_listeners", "false");
		}
		return cfg;
	}

	public ITransaction getTransaction() {
		if (getEntityManager() instanceof org.eclipse.persistence.jpa.JpaEntityManager) {
			return new JPAEclipseLinkTransaction(this);
		} else {
			return new JPAHibernateTransaction(this);
		}
	}

	public IPersistenceManager getPersistenceManager() {
		// TODO Auto-generated method stub
		return new JPAPersistenceManager(this);
	}

}
