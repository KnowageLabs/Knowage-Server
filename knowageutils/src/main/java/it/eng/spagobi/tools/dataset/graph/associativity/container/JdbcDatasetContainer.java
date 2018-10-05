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

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

abstract class JdbcDatasetContainer extends AssociativeDatasetContainer {

	private static final Logger logger = Logger.getLogger(JdbcDatasetContainer.class);

	protected JdbcDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		super(dataSet, parameters);
	}

	protected abstract String getTableName();

	protected abstract IDataSource getDataSource();

	protected abstract String encapsulateColumnName(String columnName);

	protected SelectQuery getSelectQuery(List<String> columnNames) {
		SelectQuery selectQuery = new SelectQuery(dataSet).selectDistinct().select(columnNames.toArray(new String[0])).from(getTableName());
		if (!filters.isEmpty()) {
			selectQuery.where(new AndFilter(filters.toArray(new SimpleFilter[0])));
		}
		return selectQuery;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssociativeDatasetContainer [dataSet=");
		builder.append(dataSet);
		builder.append(", tableName=");
		builder.append(getTableName());
		builder.append(", dataSource=");
		builder.append(getDataSource());
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append(", filters=");
		builder.append(filters);
		builder.append(", groups=");
		builder.append(groups);
		builder.append(", resolved=");
		builder.append(resolved);
		builder.append("]");
		return builder.toString();
	}
}
