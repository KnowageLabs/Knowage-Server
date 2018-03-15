/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
 */

package it.eng.spagobi.tools.dataset.cache.query.visitor;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.Sorting;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.utilities.database.IDataBase;

public class HsqlDbSelectQueryVisitor extends AbstractSelectQueryVisitor {

	public HsqlDbSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	@Override
	protected void append(Sorting item) {
		Projection projection = item.getProjection();
		IAggregationFunction aggregationFunction = projection.getAggregationFunction();

		String name = projection.getName();
		String alias = projection.getAlias();
		String delimitedName = StringUtilities.isEmpty(alias) ? aliasDelimiter + name + aliasDelimiter : aliasDelimiter + alias + aliasDelimiter;
		if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
			queryBuilder.append(delimitedName);
		} else {
			queryBuilder.append(aggregationFunction.apply(delimitedName));
		}

		queryBuilder.append(item.isAscending() ? " ASC" : " DESC");
	}
}
