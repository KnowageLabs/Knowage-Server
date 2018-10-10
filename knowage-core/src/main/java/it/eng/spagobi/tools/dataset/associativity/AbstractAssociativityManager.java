/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.associativity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jgrapht.graph.Pseudograph;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.AssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.NearRealtimeAssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.Selection;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicResult;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.sql.SqlUtils;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public abstract class AbstractAssociativityManager implements IAssociativityManager {

	private static Logger logger = Logger.getLogger(AbstractAssociativityManager.class);

	protected IDataSource cacheDataSource;
	protected ICache cache;
	protected Map<String, Map<String, String>> datasetToAssociations;
	protected Pseudograph<String, LabeledEdge<String>> graph;
	protected Map<String, AssociativeDatasetContainer> associativeDatasetContainers = new HashMap<>();
	protected Set<String> documentsAndExcludedDatasets;
	protected List<Selection> selections;

	protected AssociativeLogicResult result = new AssociativeLogicResult();

	protected UserProfile userProfile;

	protected abstract void initProcess();

	protected abstract void calculateDatasets(String dataset, EdgeGroup fromEdgeGroup, String filter) throws Exception;

	@Override
	public AssociativeLogicResult process() throws Exception {
		if (cacheDataSource == null) {
			throw new SpagoBIException("Unable to get cache datasource, the value of [dataSource] is [null]");
		}
		if (cache == null) {
			throw new SpagoBIException("Unable to get cache, the value of [cache] is [null]");
		}

		// (1) generate the starting set of values for each associations
		initProcess();

		// (2) user click on widget -> selection!
		for (Selection selection : selections) {
			if (!documentsAndExcludedDatasets.contains(selection.getDataset())) {
				calculateDatasets(selection.getDataset(), null, selection.getFilter());
			}
		}
		return result;
	}

	protected String getColumnNames(String associationNamesString, String datasetName) {
		String[] associationNames = associationNamesString.split(",");
		List<String> columnNames = new ArrayList<>();
		for (String associationName : associationNames) {
			Map<String, String> associationToColumns = datasetToAssociations.get(datasetName);
			if (associationToColumns != null) {
				String columnName = associationToColumns.get(associationName);
				if (columnName != null) {
					columnName = associativeDatasetContainers.get(datasetName).encapsulateColumnName(columnName);
					columnNames.add(columnName);
				}
			}
		}
		return StringUtils.join(columnNames.iterator(), ",");
	}

	protected Set<String> getTupleOfValues(String dataSet, String query) throws ClassNotFoundException, NamingException, SQLException {
		return associativeDatasetContainers.get(dataSet).getTupleOfValues(query);
	}

	protected void init(Config config, UserProfile userProfile) throws EMFUserError, SpagoBIException {
		this.userProfile = userProfile;
		initGraph(config);
		initDocuments(config);
		initCache();
		initDatasets(config);
	}

	private void initDocuments(Config config) {
		this.documentsAndExcludedDatasets = config.getDocuments();
	}

	private void initDatasets(Config config) throws EMFUserError, SpagoBIException {
		datasetToAssociations = config.getDatasetToAssociations();
		selections = config.getSelections();

		IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
		if (userProfile != null) {
			dataSetDao.setUserProfile(userProfile);
		}

		for (String v1 : graph.vertexSet()) {
			if (!documentsAndExcludedDatasets.contains(v1)) {
				// the vertex is the dataset label
				IDataSet dataSet = dataSetDao.loadDataSetByLabel(v1);
				Assert.assertNotNull(dataSet, "Unable to get metadata for dataset [" + v1 + "]");

				if (dataSet.isRealtime()) {
					documentsAndExcludedDatasets.add(v1);
				} else {
					Map<String, String> parametersValues = config.getDatasetParameters().get(v1);
					dataSet.setParamsMap(parametersValues);

					AssociativeDatasetContainer container;

					if (dataSet.isPersisted()) {
						container = new AssociativeDatasetContainer(dataSet, dataSet.getPersistTableName(), dataSet.getDataSourceForWriting(),
								parametersValues);
					} else if (dataSet.isFlatDataset()) {
						container = new AssociativeDatasetContainer(dataSet, dataSet.getFlatTableName(), dataSet.getDataSource(), parametersValues);
					} else if (config.getNearRealtimeDatasets().contains(v1) && DatasetManagementAPI.isJDBCDataSet(dataSet)
							&& !SqlUtils.isBigDataDialect(dataSet.getDataSource().getHibDialectName())) {
						QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
						String tableName = "(" + querableBehaviour.getStatement() + ") T";
						container = new AssociativeDatasetContainer(dataSet, tableName, dataSet.getDataSource(), parametersValues);
					} else if (config.getNearRealtimeDatasets().contains(v1)) {
						dataSet.loadData();
						container = new NearRealtimeAssociativeDatasetContainer(dataSet, dataSet.getDataStore(), parametersValues);
					} else {
						String signature = dataSet.getSignature();
						CacheItem cacheItem = cache.getMetadata().getCacheItem(signature);
						if (cacheItem == null) {
							logger.debug("Unable to find dataset [" + v1 + "] in cache. This can be due to changes on dataset parameters");
							cache.put(dataSet);
							cacheItem = cache.getMetadata().getCacheItem(signature);
							if (cacheItem == null) {
								throw new SpagoBIException("Unable to find dataset [" + v1 + "] in cache.");
							}
						}
						container = new AssociativeDatasetContainer(dataSet, cacheItem.getTable(), cacheDataSource, parametersValues);
					}
					associativeDatasetContainers.put(v1, container);
				}
			}
		}

	}

	private void initGraph(Config config) {
		graph = config.getGraph();
	}

	private void initCache() {
		cacheDataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
		cache = SpagoBICacheManager.getCache();
	}
}
