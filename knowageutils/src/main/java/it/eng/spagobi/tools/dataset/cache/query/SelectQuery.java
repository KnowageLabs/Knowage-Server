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

package it.eng.spagobi.tools.dataset.cache.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.item.Filter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.Sorting;
import it.eng.spagobi.tools.dataset.cache.query.visitor.ISelectQueryVisitor;
import it.eng.spagobi.tools.dataset.cache.query.visitor.SelectQueryVisitorFactory;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;

public class SelectQuery {
	private IDataSet dataSet;

	private boolean selectAll;
	private boolean selectCount;
	private boolean selectDistinct;
	private List<Projection> projections;

	private String schema;
	private String tableName;

	private Filter where;

	private List<Projection> groups;

	private Filter having;

	private List<Sorting> sortings;

	private long limit;
	private long offset;

	public SelectQuery(IDataSet dataSet) {
		this.dataSet = dataSet;

		this.projections = new ArrayList<>();
		this.groups = new ArrayList<>();
		this.sortings = new ArrayList<>();

		limit = -1;
		offset = -1;
	}

	public IDataSet getDataSet() {
		return dataSet;
	}

	public boolean hasSelectAll() {
		return selectAll;
	}

	public boolean hasSelectCount() {
		return selectCount;
	}

	public boolean isSelectDistinct() {
		return selectDistinct;
	}

	public List<Projection> getProjections() {
		return projections;
	}

	public String getSchema() {
		return schema;
	}

	public String getTableName() {
		return tableName;
	}

	public Filter getWhere() {
		return where;
	}

	public List<Projection> getGroups() {
		return groups;
	}

	public Filter getHaving() {
		return having;
	}

	public List<Sorting> getSortings() {
		return sortings;
	}

	public long getLimit() {
		return limit;
	}

	public boolean hasLimit() {
		return limit > -1;
	}

	public long getOffset() {
		return offset;
	}

	public boolean hasOffset() {
		return offset > -1;
	}

	public SelectQuery selectAll() {
		this.selectAll = true;
		return this;
	}

	public SelectQuery selectCount() {
		this.selectCount = true;
		return this;
	}

	public SelectQuery selectDistinct() {
		this.selectDistinct = true;
		return this;
	}

	public SelectQuery select(String... columnNames) {
		for (String columnName : columnNames) {
			this.projections.add(new Projection(dataSet, columnName));
		}
		return this;
	}

	public SelectQuery select(IAggregationFunction aggregationFunction, String columnAliasOrName) {
		return select(aggregationFunction, new Projection(dataSet, columnAliasOrName));
	}

	public SelectQuery select(IAggregationFunction aggregationFunction, Projection projection) {
		return select(projection);
	}

	public SelectQuery select(Projection... projections) {
		return select(Arrays.asList(projections));
	}

	public SelectQuery select(Collection<Projection> projections) {
		if (projections != null) {
			this.projections.addAll(projections);
		}
		return this;
	}

	public SelectQuery setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public SelectQuery from(String tableName) {
		return from(null, tableName);
	}

	public SelectQuery from(String schema, String tableName) {
		this.schema = schema;
		this.tableName = tableName;
		return this;
	}

	public SelectQuery where(Filter filter) {
		this.where = filter;
		return this;
	}

	public SelectQuery groupBy(Projection... projections) {
		return groupBy(Arrays.asList(projections));
	}

	public SelectQuery groupBy(Collection<Projection> projections) {
		if (projections != null) {
			this.groups.addAll(projections);
		}
		return this;
	}

	public SelectQuery having(Filter filter) {
		this.having = filter;
		return this;
	}

	public SelectQuery orderByAsc(Projection projection) {
		return orderBy(projection, true);
	}

	public SelectQuery orderByDesc(Projection projection) {
		return orderBy(projection, false);
	}

	private SelectQuery orderBy(Projection projection, boolean isAscending) {
		return orderBy(new Sorting(projection, isAscending));
	}

	public SelectQuery orderBy(Sorting... sortings) {
		return orderBy(Arrays.asList(sortings));
	}

	public SelectQuery orderBy(Collection<Sorting> sortings) {
		if (sortings != null) {
			this.sortings.addAll(sortings);
		}
		return this;
	}

	public SelectQuery limit(long limit) {
		this.limit = limit;
		return this;
	}

	public SelectQuery offset(long offset) {
		this.offset = offset;
		return this;
	}

	public String toSql(IDataSource dataSource) throws DataBaseException {
		ISelectQueryVisitor visitor = SelectQueryVisitorFactory.getVisitor(dataSource);
		return visitor.toSql(this);
	}

	public PreparedStatementData getPreparedStatementData(IDataSource dataSource) throws DataBaseException {
		ISelectQueryVisitor visitor = SelectQueryVisitorFactory.getVisitor(dataSource);
		return visitor.getPreparedStatementData(this);
	}

	public boolean hasAggregationFunction() {
		boolean hasAggregationFunction = false;
		for (Projection projection : projections) {
			IAggregationFunction aggregationFunction = projection.getAggregationFunction();
			if (aggregationFunction != null && !AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
				hasAggregationFunction = true;
				break;
			}
		}
		return hasAggregationFunction;
	}
}
