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
package it.eng.knowage.initializer;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.meta.initializer.BusinessModelInitializer;
import it.eng.knowage.meta.initializer.PhysicalModelInitializer;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.filter.PhysicalTableFilter;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.qbe.datasource.ConnectionDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class TestModelFactory {

	public static final String MODEL_NAME = "MODEL_TEST";
	public static final String FILTERED_MODEL_NAME = "FILTERED_MODEL_TEST";
	public static final String CONNECTION_NAME = "Test connection";
	public static final String DATABASE_NAME = "Test Database";

	public static Model createModel(TestConstants.DatabaseType dbType) {
		Model model = null;
		switch (dbType) {
		case MYSQL:
			model = createModelOnMySql();
			break;
		case POSTGRES:
			model = createModelOnPostgres();
			break;
		case ORACLE:
			model = createModelOnOracle();
			break;
		}

		return model;
	}

	public static Model createFilteredModel(TestConstants.DatabaseType dbType) {
		return createFilteredModel(dbType, FILTERED_MODEL_NAME);
	}

	public static Model createFilteredModel(TestConstants.DatabaseType dbType, String modelName) {
		Model model = null;
		switch (dbType) {
		case MYSQL:
			model = createFilteredModelOnMySql(modelName);
			break;
		case POSTGRES:
			model = createFilteredModelOnPostgres(modelName);
			break;
		case ORACLE:
			model = createFilteredModelOnOracle(modelName);
			break;
		}

		return model;
	}

	public static ConnectionDescriptor getConnectionDescriptor(TestConstants.DatabaseType dbType) {
		ConnectionDescriptor connectionDescriptor = null;
		switch (dbType) {
		case MYSQL:
			connectionDescriptor = getConnectionDescriptorOnMySql();
			break;
		case POSTGRES:
			connectionDescriptor = getConnectionDescriptorOnPostgres();
			break;
		case ORACLE:
			connectionDescriptor = getConnectionDescriptorOnOracle();
			break;
		}
		return connectionDescriptor;
	}

	// =======================================================
	// MYSQL
	// =======================================================
	private static Model createModelOnMySql() {
		Model model;

		model = ModelFactory.eINSTANCE.createModel();
		model.setName(MODEL_NAME);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();

		physicalModelInitializer.setRootModel(model);
		PhysicalModel physicalModel = physicalModelInitializer.initialize("PHYSICAL" + MODEL_NAME,
				TestConnectionFactory.createConnection(TestConstants.DatabaseType.MYSQL), CONNECTION_NAME, TestConstants.MYSQL_DRIVER, TestConstants.MYSQL_URL,
				TestConstants.MYSQL_USER, TestConstants.MYSQL_PWD, DATABASE_NAME, TestConstants.MYSQL_DEFAULT_CATALOGUE, TestConstants.MYSQL_DEFAULT_SCHEMA);

		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + MODEL_NAME, physicalModel);

		return model;
	}

	public static Model createFilteredModelOnMySql(String modelName) {
		Model model;

		String name = modelName != null ? modelName : FILTERED_MODEL_NAME;

		model = ModelFactory.eINSTANCE.createModel();
		model.setName(name);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();

		physicalModelInitializer.setRootModel(model);

		List<String> selectedTables = Arrays.asList(TestConstants.MYSQL_FILTERED_TABLES_FOR_PMODEL);

		PhysicalModel physicalModel = physicalModelInitializer.initialize("PHYSICAL" + name,
				TestConnectionFactory.createConnection(TestConstants.DatabaseType.MYSQL), CONNECTION_NAME, TestConstants.MYSQL_DRIVER, TestConstants.MYSQL_URL,
				TestConstants.MYSQL_USER, TestConstants.MYSQL_PWD, DATABASE_NAME, TestConnectionFactory.getDefaultCatalogue(TestConstants.DatabaseType.MYSQL),
				TestConnectionFactory.getDefaultSchema(TestConstants.DatabaseType.MYSQL), selectedTables);

		List<PhysicalTable> physicalTableToIncludeInBusinessModel = new ArrayList<PhysicalTable>();
		for (int i = 0; i < TestConstants.MYSQL_FILTERED_TABLES_FOR_BMODEL.length; i++) {
			physicalTableToIncludeInBusinessModel.add(physicalModel.getTable(TestConstants.MYSQL_FILTERED_TABLES_FOR_BMODEL[i]));
		}
		PhysicalTableFilter physicalTableFilter = new PhysicalTableFilter(physicalTableToIncludeInBusinessModel);
		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + name, physicalTableFilter, physicalModel);

		return model;
	}

	public static ConnectionDescriptor getConnectionDescriptorOnMySql() {
		ConnectionDescriptor connectionDescriptor = new ConnectionDescriptor();
		connectionDescriptor.setName("Test Model");
		connectionDescriptor.setDialect(TestConstants.MYSQL_DEFAULT_DIALECT);
		connectionDescriptor.setDriverClass(TestConstants.MYSQL_DRIVER);
		connectionDescriptor.setUrl(TestConstants.MYSQL_URL);
		connectionDescriptor.setUsername(TestConstants.MYSQL_USER);
		connectionDescriptor.setPassword(TestConstants.MYSQL_PWD);
		return connectionDescriptor;
	}

	// =======================================================
	// POSTGRES
	// =======================================================
	private static Model createModelOnPostgres() {
		Model model;

		model = ModelFactory.eINSTANCE.createModel();
		model.setName(MODEL_NAME);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();

		physicalModelInitializer.setRootModel(model);
		PhysicalModel physicalModel = physicalModelInitializer.initialize("PHYSICAL" + MODEL_NAME,
				TestConnectionFactory.createConnection(TestConstants.DatabaseType.POSTGRES), CONNECTION_NAME, TestConstants.POSTGRES_DRIVER,
				TestConstants.POSTGRES_URL, TestConstants.POSTGRES_USER, TestConstants.POSTGRES_PWD, DATABASE_NAME, TestConstants.POSTGRES_DEFAULT_CATALOG,
				TestConstants.POSTGRES_DEFAULT_SCHEMA);

		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + MODEL_NAME, physicalModel);

		return model;
	}

	public static Model createFilteredModelOnPostgres(String modelName) {
		Model model;

		String name = modelName != null ? modelName : FILTERED_MODEL_NAME;

		model = ModelFactory.eINSTANCE.createModel();
		model.setName(name);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();

		physicalModelInitializer.setRootModel(model);

		List<String> selectedTables = Arrays.asList(TestConstants.POSTGRES_FILTERED_TABLES_FOR_PMODEL);

		PhysicalModel physicalModel = physicalModelInitializer.initialize("PHYSICAL" + MODEL_NAME,
				TestConnectionFactory.createConnection(TestConstants.DatabaseType.POSTGRES), CONNECTION_NAME, TestConstants.POSTGRES_DRIVER,
				TestConstants.POSTGRES_URL, TestConstants.POSTGRES_USER, TestConstants.POSTGRES_PWD, DATABASE_NAME, TestConstants.POSTGRES_DEFAULT_CATALOG,
				TestConstants.POSTGRES_DEFAULT_SCHEMA, selectedTables);

		List<PhysicalTable> physicalTableToIncludeInBusinessModel = new ArrayList<PhysicalTable>();
		for (int i = 0; i < TestConstants.POSTGRES_FILTERED_TABLES_FOR_BMODEL.length; i++) {
			physicalTableToIncludeInBusinessModel.add(physicalModel.getTable(TestConstants.POSTGRES_FILTERED_TABLES_FOR_BMODEL[i]));
		}
		PhysicalTableFilter physicalTableFilter = new PhysicalTableFilter(physicalTableToIncludeInBusinessModel);
		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + name, physicalTableFilter, physicalModel);

		return model;
	}

	public static ConnectionDescriptor getConnectionDescriptorOnPostgres() {
		ConnectionDescriptor connectionDescriptor = new ConnectionDescriptor();
		connectionDescriptor.setName("Test Model");
		connectionDescriptor.setDialect(TestConstants.POSTGRES_DEFAULT_DIALECT);
		connectionDescriptor.setDriverClass(TestConstants.POSTGRES_DRIVER);
		connectionDescriptor.setUrl(TestConstants.POSTGRES_URL);
		connectionDescriptor.setUsername(TestConstants.POSTGRES_USER);
		connectionDescriptor.setPassword(TestConstants.POSTGRES_PWD);
		return connectionDescriptor;
	}

	// =======================================================
	// ORACLE
	// =======================================================
	private static Model createModelOnOracle() {
		Model model;

		model = ModelFactory.eINSTANCE.createModel();
		model.setName(MODEL_NAME);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();

		physicalModelInitializer.setRootModel(model);
		PhysicalModel physicalModel = physicalModelInitializer.initialize("PHYSICAL" + MODEL_NAME,
				TestConnectionFactory.createConnection(TestConstants.DatabaseType.ORACLE), CONNECTION_NAME, TestConstants.ORACLE_DRIVER,
				TestConstants.ORACLE_URL, TestConstants.ORACLE_USER, TestConstants.ORACLE_PWD, DATABASE_NAME, TestConstants.ORACLE_DEFAULT_CATALOGUE,
				TestConstants.ORACLE_DEFAULT_SCHEMA);

		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + MODEL_NAME, physicalModel);

		return model;
	}

	public static Model createFilteredModelOnOracle(String modelName) {
		Model model;

		String name = modelName != null ? modelName : FILTERED_MODEL_NAME;

		model = ModelFactory.eINSTANCE.createModel();
		model.setName(name);

		PhysicalModelInitializer physicalModelInitializer = new PhysicalModelInitializer();

		physicalModelInitializer.setRootModel(model);

		List<String> selectedTables = Arrays.asList(TestConstants.ORACLE_FILTERED_TABLES_FOR_PMODEL);

		PhysicalModel physicalModel = physicalModelInitializer.initialize("PHYSICAL" + MODEL_NAME,
				TestConnectionFactory.createConnection(TestConstants.DatabaseType.ORACLE), CONNECTION_NAME, TestConstants.ORACLE_DRIVER,
				TestConstants.ORACLE_URL, TestConstants.ORACLE_USER, TestConstants.ORACLE_PWD, DATABASE_NAME, TestConstants.ORACLE_DEFAULT_CATALOGUE,
				TestConstants.ORACLE_DEFAULT_SCHEMA, selectedTables);

		List<PhysicalTable> physicalTableToIncludeInBusinessModel = new ArrayList<PhysicalTable>();
		for (int i = 0; i < TestConstants.ORACLE_FILTERED_TABLES_FOR_BMODEL.length; i++) {
			physicalTableToIncludeInBusinessModel.add(physicalModel.getTable(TestConstants.ORACLE_FILTERED_TABLES_FOR_BMODEL[i]));
		}
		PhysicalTableFilter physicalTableFilter = new PhysicalTableFilter(physicalTableToIncludeInBusinessModel);
		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + name, physicalTableFilter, physicalModel);

		return model;
	}

	public static ConnectionDescriptor getConnectionDescriptorOnOracle() {
		ConnectionDescriptor connectionDescriptor = new ConnectionDescriptor();
		connectionDescriptor.setName("Test Model");
		connectionDescriptor.setDialect(TestConstants.ORACLE_DEFAULT_DIALECT);
		connectionDescriptor.setDriverClass(TestConstants.ORACLE_DRIVER);
		connectionDescriptor.setUrl(TestConstants.ORACLE_URL);
		connectionDescriptor.setUsername(TestConstants.ORACLE_USER);
		connectionDescriptor.setPassword(TestConstants.ORACLE_PWD);
		return connectionDescriptor;
	}
}
