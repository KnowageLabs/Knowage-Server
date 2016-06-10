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
		}

		return model;
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
		List<String> tables = Arrays.asList(TestConstants.tables);
		PhysicalModel physicalModel = physicalModelInitializer.initialize(TestConstants.datasourceId, tables);

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

		List<String> selectedTables = Arrays.asList(TestConstants.selectedTables);

		PhysicalModel physicalModel = physicalModelInitializer.initialize(TestConstants.datasourceId, selectedTables);

		List<PhysicalTable> physicalTableToIncludeInBusinessModel = new ArrayList<PhysicalTable>();
		for (int i = 0; i < selectedTables.size(); i++) {
			physicalTableToIncludeInBusinessModel.add(physicalModel.getTable(selectedTables.get(i)));
		}
		PhysicalTableFilter physicalTableFilter = new PhysicalTableFilter(physicalTableToIncludeInBusinessModel);
		BusinessModelInitializer businessModelInitializer = new BusinessModelInitializer();
		BusinessModel businessModel = businessModelInitializer.initialize("BUSINESS_" + name, physicalTableFilter, physicalModel);

		return model;
	}

}
