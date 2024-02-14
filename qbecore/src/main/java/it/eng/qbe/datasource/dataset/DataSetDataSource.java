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

package it.eng.qbe.datasource.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.AbstractDataSource;
import it.eng.qbe.datasource.IPersistenceManager;
import it.eng.qbe.datasource.configuration.CompositeDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.DataSetDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.sql.ISQLDataSource;
import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.model.structure.builder.IModelStructureBuilder;
import it.eng.qbe.model.structure.builder.dataset.DataSetModelStructureBuilder;
import it.eng.qbe.statement.hive.HiveQLStatement;
import it.eng.qbe.statement.sql.SQLStatement;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.sql.SqlUtils;

public class DataSetDataSource extends AbstractDataSource implements ISQLDataSource {

	private static final Logger LOGGER = Logger.getLogger(DataSetDataSource.class);

	private List<IDataSet> datasets;
	public static final String EMPTY_MODEL_NAME = "";
	public static final String DATASETS = "DATASETS";
	public Class statementType = SQLStatement.class;

	protected DataSetDataSource(String dataSourceName, IDataSourceConfiguration configuration) {
		LOGGER.debug("Creating a new DataSetDataSource");
		setName(dataSourceName);
		dataMartModelAccessModality = new AbstractModelAccessModality();
		this.configuration = configuration;
		datasets = new ArrayList<>();

		Assert.assertNotNull(configuration.loadDataSourceProperties(), "The properties of the datasource can not be empty");

		// // validate and set configuration
		if (configuration instanceof DataSetDataSourceConfiguration) {
			datasets.add(((DataSetDataSourceConfiguration) configuration).getDataset());
		} else if (configuration instanceof CompositeDataSourceConfiguration) {
			List<IDataSourceConfiguration> subConfigurations = ((CompositeDataSourceConfiguration) configuration).getSubConfigurations();

			for (int i = 0; i < subConfigurations.size(); i++) {
				IDataSourceConfiguration subConf = ((CompositeDataSourceConfiguration) configuration).getSubConfigurations().get(i);
				if (subConf instanceof DataSetDataSourceConfiguration) {
					datasets.add(((DataSetDataSourceConfiguration) subConf).getDataset());

				} else {
					Assert.assertUnreachable("Not suitable configuration to create a JPADataSource");
				}
			}

		} else {
			Assert.assertUnreachable("Not suitable configuration to create a JPADataSource");
		}
		LOGGER.debug("Created a new JPADataSource");
		initStatementType();
	}

	public DataSetDataSourceConfiguration getDataSetDataSourceConfiguration() {
		return (DataSetDataSourceConfiguration) configuration;
	}

	public List<IDataSet> getDatasets() {
		return datasets;
	}

	@Override
	public void open() {

	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public void close() {

	}

	@Override
	public IDataSource getToolsDataSource() {
		return getDataSourceForReading();
	}

	@Override
	public IModelStructure getModelStructure() {
		IModelStructureBuilder structureBuilder;
		if (dataMartModelStructure == null) {
			structureBuilder = new DataSetModelStructureBuilder(this);
			dataMartModelStructure = structureBuilder.build();
		}

		return dataMartModelStructure;
	}

	// TO-DO
	@Override
	public IPersistenceManager getPersistenceManager() {
		return null;
	}

	public List<IDataSet> getRootEntities() {
		return datasets;
	}

	public Class getStatementType() {
		return statementType;
	}

	private void initStatementType() {
		IDataSource datasourceForReading = this.getDataSourceForReading();
		if (datasourceForReading != null) {
			if (SqlUtils.isHiveLikeDialect(datasourceForReading.getHibDialectClass())) {
				statementType = HiveQLStatement.class;
			}
		}

	}

	public IDataSource getDataSourceForReading() {
		return datasets.get(0).getDataSourceForReading();
	}
}
