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
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.utilities.MockDataSet;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;

@RunWith(JMockit.class)
public class PersistedEvaluationStrategyTest {

    @Test
    public void shouldUsePersistedTableName() {
        final String TABLE_NAME = "PersistedTable";
        MockDataSet dataSet = new MockDataSet();
        dataSet.setPersistTableName(TABLE_NAME);
        PersistedEvaluationStrategy strategy = (PersistedEvaluationStrategy) DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.PERSISTED, dataSet, new UserProfile());
        Assert.assertThat(strategy.getTableName(), is(TABLE_NAME));
    }

    @Test
    public void shouldUseDatasourceForWriting() {
        final String DATASOURCE_LABEL = "DatasourceForWriting";
        MockDataSet dataSet = new MockDataSet();
        IDataSource dataSource = DataSourceFactory.getDataSource();
        dataSource.setLabel(DATASOURCE_LABEL);
        dataSet.setDataSourceForWriting(dataSource);
        PersistedEvaluationStrategy strategy = (PersistedEvaluationStrategy) DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.PERSISTED, dataSet, new UserProfile());
        Assert.assertThat(strategy.getDataSource().getLabel(), is(DATASOURCE_LABEL));
    }

    @Test
    public void shouldUsePreviousFireTimeToSetDataStoreDate() {

        final Date now = new Date();

        new MockUp<PersistedEvaluationStrategy>() {
            @Mock private Trigger loadTrigger() {
                Trigger trigger = new Trigger();
                trigger.setPreviousFireTime(now);
                return trigger;
            }
        };

        PersistedEvaluationStrategy strategy = (PersistedEvaluationStrategy) DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.PERSISTED, new MockDataSet(), new UserProfile());
        Assert.assertThat(strategy.getDate(), is(now));
    }

    @Test
    public void shouldUseDatasetDateInToSetDataStoreDate() {

        final Date now = new Date();

        new MockUp<PersistedEvaluationStrategy>() {
            @Mock private Trigger loadTrigger() {
                return new Trigger();
            }
        };

        MockDataSet dataSet = new MockDataSet();
        dataSet.setDateIn(now);

        PersistedEvaluationStrategy strategy = (PersistedEvaluationStrategy) DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.PERSISTED, dataSet, new UserProfile());
        Assert.assertThat(strategy.getDate(), is(now));
    }
}