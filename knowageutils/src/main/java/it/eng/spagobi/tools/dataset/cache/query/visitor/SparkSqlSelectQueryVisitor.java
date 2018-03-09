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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.eng.spagobi.tools.dataset.cache.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.cache.query.SelectQuery;
import it.eng.spagobi.tools.dataset.cache.query.item.Sorting;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.utilities.database.IDataBase;

public class SparkSqlSelectQueryVisitor extends AbstractSelectQueryVisitor {

	public SparkSqlSelectQueryVisitor(IDataBase database) {
		super(database);
		this.aliasPrefix = "";
	}

	@Override
	public String getFormattedTimestamp(Timestamp timestamp) {
		return "'" + timestamp.toString() + "'";
	}

	@Override
	public String getFormattedDate(Date date) {
		return "'" + new SimpleDateFormat(CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT).format(date) + "'";
	}

	/**
	 * @return a fake prepared statement (prepared statements are not supported by JDBC driver)
	 */
	@Override
	public PreparedStatementData getPreparedStatementData(SelectQuery selectQuery) {
		return getPreparedStatementData(selectQuery, false);
	}

	@Override
	protected void visit(SelectQuery selectQuery) {
		validate(selectQuery);

		buildRowNumberInlineViewPrefix(selectQuery);
		buildSelect(selectQuery);
		buildRowNumber(selectQuery);
		buildFrom(selectQuery);
		buildWhere(selectQuery);
		buildGroupBy(selectQuery);
		buildHaving(selectQuery);
		buildRowNumberInlineViewPostfix(selectQuery);
		buildOrderBy(selectQuery);
		buildLimit(selectQuery);
		buildOffset(selectQuery);
	}

	private void buildRowNumberInlineViewPrefix(SelectQuery query) {
		long offset = query.getOffset();
		if (offset >= 0) {
			queryBuilder.append("select * from (");
		}
	}

	private void buildRowNumberInlineViewPostfix(SelectQuery query) {
		long offset = query.getOffset();
		if (offset >= 0) {
			long limit = query.getLimit();
			queryBuilder.append(") t WHERE row___number > ");
			queryBuilder.append(offset);
			queryBuilder.append(" AND row___number <= ");
			queryBuilder.append(offset + limit);
		}

	}

	private void buildRowNumber(SelectQuery query) {
		queryBuilder.append(", row_number() over (");

		List<Sorting> sortings = query.getSortings();
		if (sortings == null || sortings.isEmpty()) {
			queryBuilder.append(" ORDER BY ");
			append(new Sorting(query.getProjections().get(0), true));
		} else {
			buildOrderBy(query);
		}

		queryBuilder.append(") row___number");
	}

	@Override
	protected void buildLimit(SelectQuery query) {
		long offset = query.getOffset();
		if (offset == 0) {
			super.buildLimit(query);
		}
	}

	/**
	 * In order to support OFFSET, do nothing here
	 */
	@Override
	protected void buildOffset(SelectQuery query) {
	}

}
