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

package it.eng.spagobi.tools.dataset.metasql.query.visitor;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.utilities.database.AbstractDataBase;
import it.eng.spagobi.utilities.database.IDataBase;

public class PostgreSqlSelectQueryVisitor extends AbstractSelectQueryVisitor {

	public PostgreSqlSelectQueryVisitor(IDataBase database) {
		super(database);
	}

	/**
	 * @param projection
	 * @param useAlias   enables the output of an alias. Column name is used as alias if related flag is true.
	 */
	@Override
	protected void append(Projection projection, boolean useAlias) {
		String aliasDelimiter = database.getAliasDelimiter();
		IAggregationFunction aggregationFunction = projection.getAggregationFunction();

		String name = projection.getName();
		String columnName = isCalculatedColumn(name) ? name.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, aliasDelimiter) : name;

		boolean isValidAggregationFunction = aggregationFunction != null && !aggregationFunction.getName().equals(AggregationFunctions.NONE);
		if (!isValidAggregationFunction) {
			queryBuilder.append(columnName);
		} else {
			queryBuilder.append(aggregationFunction.apply(columnName));
		}

		String alias = projection.getAlias();
		if (useAlias) {
			if (StringUtilities.isNotEmpty(alias) && !alias.equals(name)) {
				queryBuilder.append(" ");
				queryBuilder.append(aliasPrefix);
				queryBuilder.append(" ");
				queryBuilder.append(aliasDelimiter);
				queryBuilder.append(alias);
				queryBuilder.append(aliasDelimiter);
			} else if (useNameAsAlias || isValidAggregationFunction) {
				queryBuilder.append(" ");
				queryBuilder.append(aliasPrefix);
				queryBuilder.append(" ");
				queryBuilder.append(aliasDelimiter);
				queryBuilder.append(name);
				queryBuilder.append(aliasDelimiter);
			}
		}
	}

}
