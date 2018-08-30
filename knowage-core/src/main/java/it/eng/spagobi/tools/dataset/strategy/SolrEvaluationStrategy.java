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

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;
import it.eng.spagobi.utilities.assertion.Assert;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.List;

class SolrEvaluationStrategy extends AbstractEvaluationStrategy {

    private static final Logger logger = Logger.getLogger(SolrEvaluationStrategy.class);

    public SolrEvaluationStrategy(IDataSet dataSet) {
        super(dataSet);
    }

    @Override
    public IDataStore executeQuery(List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings, List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
        logger.debug("IN");
        Assert.assertTrue(summaryRowProjections.isEmpty(), "Impossible to calculate summary row with strategy " + this.getClass());

        SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
        SolrQuery solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).fields(projections).sorts(sortings).filter(filter).facets(groups);
        solrDataSet.setSolrQuery(solrQuery);
        dataSet.loadData(offset, fetchSize, maxRowCount);
        IDataStore dataStore = dataSet.getDataStore();
        dataStore.setCacheDate(getDate());

        return dataStore;
    }
}
