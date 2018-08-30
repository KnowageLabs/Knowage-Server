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

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.utilities.database.DataBaseException;
import org.apache.log4j.Logger;

import java.util.List;

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
    public IDataStore executeQuery(List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings, List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
        Monitor totalCacheTiming = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:totalCache");
        IDataStore dataStore;
        try {
            dataStore = cache.get(profile, dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
            if (dataSet.isRealtime()) unsetNgsiConsumer();

            if (dataStore == null) {
                manageDatasetNotInCache(projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
            } else {
                if (dataSet.isRealtime()) subscribeNGSI();
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

    protected IDataStore manageDatasetNotInCache(List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings, List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) throws DataBaseException {
        Monitor timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:putInCache");
        DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI();
        datasetManagementAPI.putDataSetInCache(dataSet, cache, getEvaluationStrategy());
        timing.stop();

        if (dataSet.getDataStore() != null && dataSet.getDataStore().getMetaData().getFieldCount() == 0) {
            // update only datasource's metadata from dataset if for some strange cause it hasn't got fields
            // WTF???
            logger.debug("Update datastore's metadata with dataset's metadata when no data is found...");
            return new DataStore(dataSet.getMetadata());
        }

        timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:getFromCache");
        IDataStore dataStore = cache.get(profile, dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
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

}
