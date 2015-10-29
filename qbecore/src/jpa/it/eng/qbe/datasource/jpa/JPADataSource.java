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
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.builder.IModelStructureBuilder;
import it.eng.qbe.model.structure.builder.jpa.JPAModelStructureBuilder;
import it.eng.qbe.query.Filter;
import it.eng.qbe.query.filters.ProfileAttributesModelAccessModality;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.sql.SqlUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

	private UserProfile userProfile = null;

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
	 * @see it.eng.qbe.datasource.IHibernateDataSource#getSessionFactory(java.lang .String)
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

	public IModelStructure getModelStructure(UserProfile profile) {
		userProfile = profile;
		System.out.println("getModelStructure(UserProfile profile) userprofile: " + (userProfile != null ? userProfile.getUserName() : " NULL") + "dataMartModelStructure: " + dataMartModelStructure);
		IModelStructureBuilder structureBuilder;
		if (dataMartModelStructure == null) {
			structureBuilder = new JPAModelStructureBuilder(this);
			dataMartModelStructure = structureBuilder.build();

			Map<String, List<String>> fieldsFilteredByRole = getFieldsFilteredByRole();

			List<Filter> filtersOnProfileAttributes = getFiltersOnProfileAttributes();
			if (!filtersOnProfileAttributes.isEmpty() || !fieldsFilteredByRole.isEmpty()) {
				logger.debug("One or more profile attributes filters were found therefore profile attributes model access modality will be activated.");
				this.setDataMartModelAccessModality(new ProfileAttributesModelAccessModality(filtersOnProfileAttributes, fieldsFilteredByRole, profile));
			}
		}
		return dataMartModelStructure;
	}

	public IModelStructure getModelStructure() {

		System.out.println("getModelStructure() userprofile: " + (userProfile != null ? userProfile.getUserName() : " NULL"));

		return getModelStructure(userProfile);
	}

	private List<Filter> getFiltersOnProfileAttributes() {
		List<Filter> toReturn = new ArrayList<Filter>();
		Iterator<String> it = dataMartModelStructure.getModelNames().iterator();
		while (it.hasNext()) {
			List<IModelEntity> list = dataMartModelStructure.getRootEntities(it.next());
			List<Filter> filters = getFiltersOnProfileAttributes(list);
			toReturn.addAll(filters);
		}
		return toReturn;
	}

	private List<Filter> getFiltersOnProfileAttributes(List<IModelEntity> list) {
		List<Filter> toReturn = new ArrayList<Filter>();
		Iterator<IModelEntity> it = list.iterator();
		while (it.hasNext()) {
			IModelEntity entity = it.next();
			List<IModelField> allFields = entity.getAllFields();
			Iterator<IModelField> fieldsIt = allFields.iterator();
			while (fieldsIt.hasNext()) {
				IModelField field = fieldsIt.next();
				String attributeName = (String) field.getProperty("attribute");
				if (attributeName != null && !attributeName.trim().equals("")) {
					logger.debug("Found profile attribute filter on field " + field.getUniqueName() + ": profile attribute is " + attributeName);
					// Filter filter = new Filter(entity.getUniqueName(), "F{" + field.getName() + "} = {" + attributeName + "}");
					List<String> values = new ArrayList<String>();
					// we put just the profile attribute name, it's value will be evaluated later, when evaluating the filters
					values.add(attributeName);
					Filter filter = new Filter(field, values);
					toReturn.add(filter);
				}
			}
		}
		return toReturn;
	}
	private Map<String, List<String>> getFieldsFilteredByRole() {
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Iterator<String> it = dataMartModelStructure.getModelNames().iterator();
		while (it.hasNext()) {
			List<IModelEntity> list = dataMartModelStructure.getRootEntities(it.next());
			Map<String, List<String>> roles = getFieldsFilteredByRole(list);
			toReturn.putAll(roles);
		}
		return toReturn;
	}

	private Map<String, List<String>> getFieldsFilteredByRole(List<IModelEntity> list) {
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Iterator<IModelEntity> it = list.iterator();
		while (it.hasNext()) {
			IModelEntity entity = it.next();
			List<IModelField> allFields = entity.getAllFields();
			Iterator<IModelField> fieldsIt = allFields.iterator();
			while (fieldsIt.hasNext()) {
				IModelField field = fieldsIt.next();
				String roles = (String) field.getProperty("excludedRoles");
				if (roles != null && !roles.trim().equals("")) {
					toReturn.put(field.getUniqueName(), Arrays.asList(roles.split(";")));
				}
			}
		}
		return toReturn;
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
