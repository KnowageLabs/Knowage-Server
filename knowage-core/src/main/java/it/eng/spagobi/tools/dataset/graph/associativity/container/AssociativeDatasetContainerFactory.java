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

package it.eng.spagobi.tools.dataset.graph.associativity.container;

import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.utils.QBEAssociativeDatasetContainer;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.DataBaseException;

public abstract class AssociativeDatasetContainerFactory {

	static protected Logger logger = Logger.getLogger(AssociativeDatasetContainerFactory.class);

	public static IAssociativeDatasetContainer getContainer(DatasetEvaluationStrategy evaluationStrategy, IDataSet dataSet,
			Map<String, String> parametersValues, UserProfile userProfile) throws DataBaseException {
		Assert.assertNotNull(evaluationStrategy, "Dataset evaluation strategy cannot be null");

		switch (evaluationStrategy) {
		case PERSISTED:
			return new PersistedAssociativeDatasetContainer(dataSet, parametersValues);
		case FLAT:
			return new FlatAssociativeDatasetContainer(dataSet, parametersValues);
		case INLINE_VIEW:
			if (dataSet instanceof VersionedDataSet) {
				dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
			}
			if (dataSet instanceof AbstractJDBCDataset) {
				return new JDBCAssociativeDatasetContainer(dataSet, parametersValues);
			} else {
				return new QBEAssociativeDatasetContainer(dataSet, parametersValues);

			}
		case CACHED:
			IDataSource cacheDataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
			ICache cache = SpagoBICacheManager.getCache();
			String signature = dataSet.getSignature();
			CacheItem cacheItem = cache.getMetadata().getCacheItem(signature);
			cacheItem = cacheDataSetIfMissing(dataSet, cache, cacheItem, userProfile);
			return new CachedAssociativeDatasetContainer(dataSet, cacheItem.getTable(), cacheDataSource, parametersValues);
		default:
			throw new IllegalArgumentException("Dataset evaluation strategy [" + evaluationStrategy + "] not supported");
		}
	}

	private static CacheItem cacheDataSetIfMissing(IDataSet dataSet, ICache cache, CacheItem cacheItem, UserProfile userProfile) throws DataBaseException {
		if (cacheItem == null) {
			logger.debug("Unable to find dataset [" + dataSet.getLabel() + "] in cache. This can be due to changes on dataset parameters");
			new DatasetManagementAPI(userProfile).putDataSetInCache(dataSet, cache);
			cacheItem = cache.getMetadata().getCacheItem(dataSet.getSignature());
			if (cacheItem == null) {
				throw new CacheException("Unable to find dataset [" + dataSet.getLabel() + "] in cache.");
			}
		}
		return cacheItem;
	}

}
