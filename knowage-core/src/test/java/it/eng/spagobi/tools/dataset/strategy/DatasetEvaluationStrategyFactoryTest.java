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

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.ICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.MockDataSet;
import it.eng.spagobi.utilities.assertion.NullReferenceException;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;

@RunWith(JMockit.class)
public class DatasetEvaluationStrategyFactoryTest {

    {
        new MockUp<CacheFactory>() {

            @Mock
            ICache getCache(ICacheConfiguration cacheConfiguration) {
                return new SQLDBCache(null, null);
            }
        };

        new MockUp<SpagoBICacheConfiguration>() {

            @Mock
            IDataSource getCacheDataSource() {
                return DataSourceFactory.getDataSource();
            }

            @Mock
            String getTableNamePrefix() {
                return "prefix";
            }

            @Mock
            BigDecimal getCacheSpaceAvailable() {
                return new BigDecimal(0);
            }

            @Mock
            Integer getCachePercentageToClean() {
                return 0;
            }

            @Mock
            Integer getCacheDsLastAccessTtl() {
                return 0;
            }

            @Mock
            String getCacheSchedulingFullClean() {
                return "";
            }

            @Mock
            String getCacheDatabaseSchema() {
                return "";
            }

            @Mock
            Integer getCachePercentageToStore() {
                return 0;
            }

            @Mock
            List<Properties> getDimensionTypes() {
                return new ArrayList<>(0);
            }
        };
    }

    @Injectable
    IDataSet dataSet = new MockDataSet();

    @Test
    public void shouldGetPersistedStrategy() {
        IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.PERSISTED, dataSet, new UserProfile());
        Assert.assertThat(strategy, instanceOf(PersistedEvaluationStrategy.class));
    }

    @Test
    public void shouldGetFlatStrategy() {
        IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.FLAT, dataSet, new UserProfile());
        Assert.assertThat(strategy, instanceOf(FlatEvaluationStrategy.class));
    }

    @Test
    public void shouldGetInlineViewStrategy() {
        IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.INLINE_VIEW, dataSet, new UserProfile());
        Assert.assertThat(strategy, instanceOf(InlineViewEvaluationStrategy.class));
    }

    @Test
    public void shouldGetCachedStrategy() {
        IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.CACHED, dataSet, new UserProfile());
        Assert.assertThat(strategy, instanceOf(CachedEvaluationStrategy.class));
    }

    @Test
    public void shouldGetRealtimeStrategy() {
        IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.REALTIME, dataSet, new UserProfile());
        Assert.assertThat(strategy, instanceOf(RealtimeEvaluationStrategy.class));
    }

    @Test
    public void shouldGetSolrStrategy() {
        IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.SOLR, dataSet, new UserProfile());
        Assert.assertThat(strategy, instanceOf(SolrEvaluationStrategy.class));
    }

    @Test(expected = NullReferenceException.class)
    public void shouldNotAllowNullableDatasetAsArgument() {
        DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.REALTIME, null, new UserProfile());
    }

    @Test(expected = NullReferenceException.class)
    public void shouldNotAllowNullableStrategyTypeAsArgument() {
        DatasetEvaluationStrategyFactory.get(null, dataSet, new UserProfile());
    }

    @Test(expected = NullReferenceException.class)
    public void shouldNotAllowBothNullableStrategyTypeAndDatasetAsArguments() {
        DatasetEvaluationStrategyFactory.get(null, null, new UserProfile());
    }

    @Test(expected = NullReferenceException.class)
    public void shouldNotAllowNullableUserProfileAsArgumentForRealtimeStrategy() {
        DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.REALTIME, dataSet, null);
    }

    @Test(expected = NullReferenceException.class)
    public void shouldNotAllowNullableUserProfileAsArgumentForCacheStrategy() {
        DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.CACHED, dataSet, null);
    }
}