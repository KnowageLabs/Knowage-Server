/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.strategy;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.DataBaseException;

class CachedEvaluationStrategy extends AbstractEvaluationStrategy {

	private static final Logger logger = Logger.getLogger(CachedEvaluationStrategy.class);

	private ICache cache;
	private UserProfile profile;

	public CachedEvaluationStrategy(UserProfile userProfile, IDataSet dataSet, ICache cache) {
		super(dataSet);
		this.profile = userProfile;
		this.cache = cache;
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		Monitor totalCacheTiming = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:totalCache");
		IDataStore dataStore;
		try {
			dataStore = cache.get(profile, dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
			if (dataSet.isRealtime())
				unsetNgsiConsumer();

			if (dataStore == null) {
				dataStore = manageDatasetNotInCache(projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
			} else {
				if (dataSet.isRealtime())
					subscribeNGSI();
			}

			dataStore.adjustMetadata(dataSet.getMetadata());
			dataSet.decode(dataStore);
		} catch (DataBaseException e) {
			throw new RuntimeException(e);
		} finally {
			totalCacheTiming.stop();
		}
		return dataStore;
	}

	@Override
	protected IDataStore executeSummaryRow(List<AbstractSelectionField> summaryRowProjections, IMetaData metaData, Filter filter, int maxRowCount) {
		throw new UnsupportedOperationException("Summary row is already included in the datastore from the execution, so this method should not be called");
	}

	@Override
	protected boolean isSummaryRowIncluded() {
		return true;
	}

	protected IDataStore manageDatasetNotInCache(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups,
			List<Sorting> sortings, List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes)
			throws DataBaseException {
		Monitor timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:putInCache");
		DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI();
		datasetManagementAPI.putDataSetInCache(dataSet, cache, getEvaluationStrategy(), indexes);
		timing.stop();

		if (dataSet.getDataStore() != null && dataSet.getDataStore().getMetaData().getFieldCount() == 0) {
			// update only datasource's metadata from dataset if for some strange cause it hasn't got fields
			// WTF???
			logger.debug("Update datastore's metadata with dataset's metadata when no data is found...");
			return new DataStore(dataSet.getMetadata());
		}

		timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:getFromCache");
		IDataStore dataStore = cache.get(profile, dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount,
				indexes);
		timing.stop();
		if (dataStore == null) {
			throw new CacheException("Impossible to get data of " + dataSet.getLabel() + " from cache");
		}

		// if result was not cached put refresh date as now
		dataStore.setCacheDate(getDate());
		return dataStore;
	}

	private void unsetNgsiConsumer() {
		RESTDataSet restDataSet = dataSet.getImplementation(RESTDataSet.class);
		restDataSet.setRealtimeNgsiConsumer(false);
	}

	private void subscribeNGSI() {
		RESTDataSet restDataSet = dataSet.getImplementation(RESTDataSet.class);
		restDataSet.subscribeNGSI();
	}

	protected DatasetEvaluationStrategyType getEvaluationStrategy() {
		return DatasetEvaluationStrategyType.CACHED;
	}

	@Override
	protected IDataStore executeTotalsFunctions(IDataSet dataSet, Set<String> totalFunctionsProjections, Filter filter, int maxRowCount, Set<String> indexes) {
		try {

			/*
			 * We should retrieve values from cache DB
			 */
			FlatDataSet flatDataSet = new FlatDataSet();
			flatDataSet.setDataSource(cache.getDataSource());

			CacheItem cacheItem = cache.getMetadata().getCacheItem(dataSet.getSignature());
			if (cacheItem == null) { // probably dataset was not cached before
				DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI();
				datasetManagementAPI.putDataSetInCache(dataSet, cache, getEvaluationStrategy(), indexes);
				cacheItem = cache.getMetadata().getCacheItem(dataSet.getSignature());
			}
			flatDataSet.setTableName(cacheItem.getTable());
			flatDataSet.setMetadata(dataSet.getMetadata());

			String[] totalFunctionsProjectionsString = new String[totalFunctionsProjections.size()];
			totalFunctionsProjections.toArray(totalFunctionsProjectionsString);
			String totalFunctionsQuery = new SelectQuery(dataSet).select(totalFunctionsProjectionsString).from(flatDataSet.getFlatTableName()).where(filter)
					.toSql(flatDataSet.getDataSource());
			logger.info("Total functions query [ " + totalFunctionsQuery + " ]");
			return flatDataSet.getDataSource().executeStatement(totalFunctionsQuery, -1, -1, maxRowCount, false);
		} catch (DataBaseException e) {
			throw new RuntimeException(e);
		}
	}

}
