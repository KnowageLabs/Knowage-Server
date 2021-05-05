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

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;

abstract class AbstractJdbcEvaluationStrategy extends AbstractEvaluationStrategy {

	private static final Logger logger = Logger.getLogger(AbstractJdbcEvaluationStrategy.class);

	public AbstractJdbcEvaluationStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		logger.debug("IN");

		IDataStore pagedDataStore;

		try {
			SelectQuery selectQuery = new SelectQuery(dataSet).selectDistinct().select(projections).from(getTableName()).where(filter).groupBy(groups)
					.orderBy(sortings);
			pagedDataStore = getDataSource().executeStatement(selectQuery, offset, fetchSize, maxRowCount, true);
			pagedDataStore.setCacheDate(getDate());
		} catch (DataBaseException e) {
			throw new RuntimeException(e);
		} finally {
			logger.debug("OUT");
		}
		return pagedDataStore;
	}

	@Override
	protected IDataStore executeSummaryRow(List<AbstractSelectionField> summaryRowProjections, IMetaData metaData, Filter filter, int maxRowCount) {
		try {
			String summaryRowQuery = new SelectQuery(dataSet).selectDistinct().select(summaryRowProjections).from(getTableName()).where(filter)
					.toSql(getDataSource());
			logger.info("Summary row query [ " + summaryRowQuery + " ]");
			// summary row query result is 1, no need to calculate total results number, so calculateTotalResultsNumber is set to false
			return getDataSource().executeStatement(summaryRowQuery, -1, -1, maxRowCount, false);
		} catch (DataBaseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected IDataStore executeTotalsFunctions(IDataSet dataSet, Set<String> totalFunctionsProjections, Filter filter, int maxRowCount, Set<String> indexes) {
		try {
			String[] totalFunctionsProjectionsString = new String[totalFunctionsProjections.size()];
			totalFunctionsProjections.toArray(totalFunctionsProjectionsString);
			String totalFunctionsQuery = new SelectQuery(dataSet).select(totalFunctionsProjectionsString).from(getTableName()).where(filter)
					.toSql(getDataSource());
			logger.info("Total functions query [ " + totalFunctionsQuery + " ]");
			return getDataSource().executeStatement(totalFunctionsQuery, -1, -1, maxRowCount, false);
		} catch (DataBaseException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract String getTableName() throws DataBaseException;

	protected abstract IDataSource getDataSource();
}
