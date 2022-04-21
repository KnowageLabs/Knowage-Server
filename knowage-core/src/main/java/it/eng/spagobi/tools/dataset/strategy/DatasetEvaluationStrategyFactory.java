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

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.utilities.assertion.Assert;

public class DatasetEvaluationStrategyFactory {

	private static final Logger logger = Logger.getLogger(DatasetEvaluationStrategyFactory.class);

	private DatasetEvaluationStrategyFactory() {
	}

	/**
	 * @param strategyType
	 * @param dataSet
	 * @param userProfile
	 * @return the evaluation strategy
	 */
	public static IDatasetEvaluationStrategy get(DatasetEvaluationStrategyType strategyType, IDataSet dataSet, UserProfile userProfile) {
		Assert.assertNotNull(strategyType, "Strategy type cannot be null");
		Assert.assertNotNull(dataSet, "Dataset cannot be null");
		AbstractEvaluationStrategy ret = null;
		switch (strategyType) {

		case PERSISTED:
			ret = new PersistedEvaluationStrategy(dataSet);
			break;
		case FLAT:
			ret = new FlatEvaluationStrategy(dataSet);
			break;
		case INLINE_VIEW:
			ret = new InlineViewEvaluationStrategy(dataSet);
			break;
		case CACHED:
			Assert.assertNotNull(userProfile, "User profile cannot be null to build " + CachedEvaluationStrategy.class);
			ICache cache = null;
			try {
				cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
			} catch (Exception e) {
				throw new CacheException(e);
			}
			ret = new CachedEvaluationStrategy(userProfile, dataSet, cache);
			break;
		case REALTIME:
			Assert.assertNotNull(userProfile, "User profile cannot be null to build " + RealtimeEvaluationStrategy.class);
			ICache cacheRT = null;
			try {
				cacheRT = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
			} catch (Exception e) {
				throw new CacheException(e);
			}
			ret = new RealtimeEvaluationStrategy(userProfile, dataSet, cacheRT);
			break;
		case SOLR:
			ret = new SolrEvaluationStrategy(dataSet);
			break;
		case SOLR_FACET_PIVOT:
			ret = new SolrFacetPivotEvaluationStrategy(dataSet);
			break;
		case SOLR_SIMPLE:
			ret = new SolrSimpleEvaluationStrategy(dataSet);
			break;
		default:
			throw new IllegalArgumentException("The strategy " + strategyType + " is not valid");
		}
		return new TimeMonitoringEvaluationStrategyWrapper(ret);
	}
}
