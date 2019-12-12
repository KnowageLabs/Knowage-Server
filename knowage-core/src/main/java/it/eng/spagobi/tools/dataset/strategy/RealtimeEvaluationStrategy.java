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

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.utilities.database.DataBaseException;

class RealtimeEvaluationStrategy extends CachedEvaluationStrategy {

	private static final Logger logger = Logger.getLogger(RealtimeEvaluationStrategy.class);

	public RealtimeEvaluationStrategy(UserProfile userProfile, IDataSet dataSet, ICache cache) {
		super(userProfile, dataSet, cache);
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		try {
			return manageDatasetNotInCache(projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
		} catch (DataBaseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected DatasetEvaluationStrategyType getEvaluationStrategy() {
		return DatasetEvaluationStrategyType.REALTIME;
	}
}
