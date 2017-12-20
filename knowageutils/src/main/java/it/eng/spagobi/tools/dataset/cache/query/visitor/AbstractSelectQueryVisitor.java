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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.cache.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.cache.query.SelectQuery;
import it.eng.spagobi.tools.dataset.cache.query.SqlDialect;
import it.eng.spagobi.tools.dataset.cache.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.CompoundFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Filter;
import it.eng.spagobi.tools.dataset.cache.query.item.InFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.OrFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.cache.query.item.Sorting;
import it.eng.spagobi.tools.dataset.cache.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.AbstractDataBase;

public abstract class AbstractSelectQueryVisitor implements ISelectQueryVisitor {

	protected static final String DATE_TIME_FORMAT_SQL_STANDARD = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "YYYY").replace("MM", "MM")
			.replace("dd", "DD").replace("HH", "HH24").replace("mm", "MI").replace("ss", "SS");

	protected boolean buildPreparedStatement;
	protected String aliasDelimiter;
	protected SqlDialect dialect;
	protected StringBuilder queryBuilder;
	protected List<Object> queryParameters;

	public AbstractSelectQueryVisitor() {
		buildPreparedStatement = false;
		this.aliasDelimiter = AbstractDataBase.STANDARD_ALIAS_DELIMITER;
		this.queryBuilder = new StringBuilder();
		this.queryParameters = new ArrayList<Object>();
	}

	protected void visit(Filter filter) {
		if (filter instanceof CompoundFilter) {
			visit((CompoundFilter) filter);
		} else if (filter instanceof SimpleFilter) {
			visit((SimpleFilter) filter);
		} else {
			throw new IllegalArgumentException("No visit(" + filter.getClass().getCanonicalName() + ") method available");
		}
	}

	@Override
	public void visit(AndFilter item) {
		visit((CompoundFilter) item);
	}

	@Override
	public void visit(OrFilter item) {
		visit((CompoundFilter) item);
	}

	@Override
	public void visit(BetweenFilter item) {
		append(item.getProjection(), false);
		queryBuilder.append(" BETWEEN ");
		append(item.getBeginValue());
		queryBuilder.append(" AND ");
		append(item.getEndValue());
	}

	@Override
	public void visit(NullaryFilter item) {
		append(item.getProjection(), false);
		queryBuilder.append(" ");
		queryBuilder.append(item.getOperator());
	}

	protected void visit(CompoundFilter item) {
		String spacedOp = " " + item.getCompositionOperator().toString() + " ";
		List<Filter> filters = item.getFilters();

		boolean isCompoundFilter = filters.get(0) instanceof CompoundFilter;
		queryBuilder.append(isCompoundFilter ? "(" : "");
		visit(filters.get(0));
		queryBuilder.append(isCompoundFilter ? ")" : "");

		for (int i = 1; i < filters.size(); i++) {
			queryBuilder.append(spacedOp);
			isCompoundFilter = filters.get(i) instanceof CompoundFilter;
			queryBuilder.append(isCompoundFilter ? "(" : "");
			visit(filters.get(i));
			queryBuilder.append(isCompoundFilter ? ")" : "");
		}
	}

	@Override
	public void visit(UnaryFilter item) {
		append(item.getProjection(), false);
		queryBuilder.append(" ");
		queryBuilder.append(item.getOperator());
		queryBuilder.append(" ");
		append(item.getOperand());
	}

	protected void append(Object operand) {
		if (operand == null) {
			queryBuilder.append("NULL");
		} else {
			if (buildPreparedStatement) {
				queryBuilder.append("?");
				queryParameters.add(operand);
			} else {
				if (Timestamp.class.isAssignableFrom(operand.getClass())) {
					queryBuilder.append(getFormattedTimestamp((Timestamp) operand));
				} else if (Date.class.isAssignableFrom(operand.getClass())) {
					queryBuilder.append(getFormattedDate((Date) operand));
				} else if (String.class.isAssignableFrom(operand.getClass())) {
					queryBuilder.append("'");
					queryBuilder.append(operand);
					queryBuilder.append("'");
				} else {
					queryBuilder.append(operand);
				}
			}
		}
	}

	@Override
	public String getFormattedTimestamp(Timestamp timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_TIMESTAMP_FORMAT);

		StringBuilder sb = new StringBuilder();
		sb.append("TO_DATE('");
		sb.append(dateFormat.format(timestamp));
		sb.append("','");
		sb.append(DATE_TIME_FORMAT_SQL_STANDARD);
		sb.append("')");

		return sb.toString();
	}

	@Override
	public String getFormattedDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT);

		StringBuilder sb = new StringBuilder();
		sb.append("TO_DATE('");
		sb.append(dateFormat.format(date));
		sb.append("','");
		sb.append(DATE_TIME_FORMAT_SQL_STANDARD);
		sb.append("')");

		return sb.toString();
	}

	@Override
	public void visit(InFilter item) {
		if (!dialect.isSingleColumnInOperatorSupported() || (item.getProjections().size() > 1 && !dialect.isMultiColumnInOperatorSupported())) {
			queryBuilder.append("(");
			visit(transformToAndOrFilters(item));
			queryBuilder.append(")");
		} else {
			append(item);
		}
	}

	protected Filter transformToAndOrFilters(InFilter item) {
		List<Projection> projections = item.getProjections();
		List<Object> operands = item.getOperands();

		int columnCount = projections.size();
		int tupleCount = operands.size() / columnCount;

		AndFilter[] andFilters = new AndFilter[tupleCount];
		for (int tupleIndex = 0; tupleIndex < tupleCount; tupleIndex++) {
			UnaryFilter[] equalFilters = new UnaryFilter[columnCount];
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				equalFilters[columnIndex] = new UnaryFilter(projections.get(columnIndex), SimpleFilterOperator.EQUALS_TO,
						operands.get(columnIndex + tupleIndex * columnCount));
			}

			andFilters[tupleIndex] = new AndFilter(equalFilters);
		}
		return new OrFilter(andFilters);
	}

	protected void append(InFilter item) {
		List<Projection> projections = item.getProjections();
		String openBracket = projections.size() > 1 ? "(" : "";
		String closeBracket = projections.size() > 1 ? ")" : "";

		queryBuilder.append(openBracket);

		append(projections.get(0), false);
		for (int i = 1; i < projections.size(); i++) {
			queryBuilder.append(",");
			append(projections.get(i), false);
		}

		queryBuilder.append(closeBracket);

		queryBuilder.append(" ");
		queryBuilder.append(item.getOperator());
		queryBuilder.append(" (");

		List<Object> operands = item.getOperands();
		for (int i = 0; i < operands.size(); i++) {
			if (i % projections.size() == 0) { // 1st item of tuple of values
				if (i >= projections.size()) { // starting from 2nd tuple of values
					queryBuilder.append(",");
				}
				queryBuilder.append(openBracket);
			}
			if (i % projections.size() != 0) {
				queryBuilder.append(",");
			}
			append(operands.get(i));
			if (i % projections.size() == projections.size() - 1) { // last item of tuple of values
				queryBuilder.append(closeBracket);
			}
		}

		queryBuilder.append(")");
	}

	@Override
	public void visit(LikeFilter item) {
		append(item.getProjection(), false);
		queryBuilder.append(" ");
		queryBuilder.append(item.getOperator());
		if (buildPreparedStatement) {
			queryBuilder.append(" ?");
			queryParameters.add(item.getPattern());
		} else {
			queryBuilder.append(" '");
			queryBuilder.append(item.getPattern());
			queryBuilder.append("'");
		}
	}

	protected void append(Projection item, boolean useAlias) {
		IAggregationFunction aggregationFunction = item.getAggregationFunction();

		String name = item.getName();
		String delimitedName = name.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)
				? name.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, aliasDelimiter)
				: aliasDelimiter + name + aliasDelimiter;
		boolean isValidAggregationFunction = aggregationFunction != null && !aggregationFunction.getName().equals(AggregationFunctions.NONE);
		if (!isValidAggregationFunction) {
			queryBuilder.append(delimitedName);
		} else {
			queryBuilder.append(aggregationFunction.apply(delimitedName));
		}

		String alias = item.getAlias();
		if (useAlias) {
			if (StringUtilities.isNotEmpty(alias) && !alias.equals(name)) {
				queryBuilder.append(" as ");
				queryBuilder.append(aliasDelimiter);
				queryBuilder.append(alias);
				queryBuilder.append(aliasDelimiter);
			} else if (isValidAggregationFunction) {
				queryBuilder.append(" as ");
				queryBuilder.append(aliasDelimiter);
				queryBuilder.append(name);
				queryBuilder.append(aliasDelimiter);
			}
		}
	}

	protected void append(Sorting item) {
		Projection projection = item.getProjection();
		IAggregationFunction aggregationFunction = projection.getAggregationFunction();

		String name = aliasDelimiter + projection.getName() + aliasDelimiter;
		if (aggregationFunction == null) {
			queryBuilder.append(name);
		} else {
			queryBuilder.append(aggregationFunction.apply(name));
		}

		queryBuilder.append(item.isAscending() ? " ASC" : " DESC");
	}

	@Override
	public void visit(UnsatisfiedFilter item) {
		queryBuilder.append(" 0=1");
	}

	public void visit(SimpleFilter item) {
		if (item instanceof BetweenFilter) {
			visit((BetweenFilter) item);
		} else if (item instanceof InFilter) {
			visit((InFilter) item);
		} else if (item instanceof LikeFilter) {
			visit((LikeFilter) item);
		} else if (item instanceof NullaryFilter) {
			visit((NullaryFilter) item);
		} else if (item instanceof UnaryFilter) {
			visit((UnaryFilter) item);
		} else if (item instanceof UnsatisfiedFilter) {
			visit((UnsatisfiedFilter) item);
		} else {
			throw new IllegalArgumentException("No visit(" + item.getClass().getCanonicalName() + ") method available");
		}
	}

	protected void visit(SelectQuery selectQuery) {
		validate(selectQuery);

		buildSelect(selectQuery);
		buildFrom(selectQuery);
		buildWhere(selectQuery);
		buildGroupBy(selectQuery);
		buildHaving(selectQuery);
		buildOrderBy(selectQuery);
		buildLimit(selectQuery);
		buildOffset(selectQuery);
	}

	protected void validate(SelectQuery query) {
		List<Projection> projections = query.getProjections();
		boolean selectAll = query.hasSelectAll();
		boolean selectCount = query.hasSelectCount();
		Assert.assertTrue(!(selectAll && selectCount), "Invalid projections definition");

		String tableName = query.getTableName();
		Assert.assertTrue(StringUtilities.isNotEmpty(tableName), "Missing table definition");
	}

	protected void buildSelect(SelectQuery query) {
		queryBuilder.append("SELECT ");

		appendSelectDistinct(query);

		if (query.hasSelectAll()) {
			queryBuilder.append("* ");
		} else if (query.hasSelectCount()) {
			queryBuilder.append("COUNT(*) ");
		} else {
			List<Projection> projections = query.getProjections();
			if (projections == null || projections.isEmpty()) {
				return;
			}

			append(projections.get(0), true);
			for (int i = 1; i < projections.size(); i++) {
				queryBuilder.append(",");
				append(projections.get(i), true);
			}

			List<Projection> groups = query.getGroups();
			List<Sorting> sortings = query.getSortings();
			if (groups != null && !groups.isEmpty() && sortings != null && !sortings.isEmpty()) {
				for (Sorting sorting : sortings) {
					queryBuilder.append(",");
					append(sorting.getProjection(), false);
				}
			}
		}
	}

	protected void appendSelectDistinct(SelectQuery query) {
		if (query.isSelectDistinct()) {
			queryBuilder.append("DISTINCT ");
		}
	}

	protected void buildFrom(SelectQuery query) {
		queryBuilder.append(" FROM ");

		String schema = query.getSchema();
		if (StringUtilities.isNotEmpty(schema)) {
			queryBuilder.append(schema);
			queryBuilder.append(".");
		}

		queryBuilder.append(query.getTableName());
	}

	protected void buildWhere(SelectQuery query) {
		Filter filter = query.getWhere();
		if (filter == null) {
			return;
		}

		queryBuilder.append(" WHERE ");
		visit(filter);
	}

	protected void buildGroupBy(SelectQuery query) {
		List<Projection> groups = query.getGroups();
		if (groups == null || groups.isEmpty()) {
			return;
		}

		queryBuilder.append(" GROUP BY ");
		append(groups.get(0), false);
		for (int i = 1; i < groups.size(); i++) {
			queryBuilder.append(",");
			append(groups.get(i), false);
		}

		List<Sorting> sortings = query.getSortings();
		if (sortings != null && !sortings.isEmpty()) {
			for (Sorting sorting : sortings) {
				queryBuilder.append(",");
				append(sorting.getProjection(), false);
			}
		}
	}

	protected void buildHaving(SelectQuery query) {
		Filter filter = query.getHaving();
		if (filter == null) {
			return;
		}

		queryBuilder.append(" HAVING ");
		visit(filter);
	}

	protected void buildOrderBy(SelectQuery query) {
		List<Sorting> sortings = query.getSortings();
		if (sortings == null || sortings.isEmpty()) {
			return;
		}

		queryBuilder.append(" ORDER BY ");
		append(sortings.get(0));
		for (int i = 1; i < sortings.size(); i++) {
			queryBuilder.append(",");
			append(sortings.get(i));
		}
	}

	protected void buildLimit(SelectQuery query) {
		long limit = query.getLimit();
		if (limit >= 0) {
			queryBuilder.append(" LIMIT ");
			queryBuilder.append(limit);
		}
	}

	protected void buildOffset(SelectQuery query) {
		long offset = query.getOffset();
		if (offset >= 0) {
			queryBuilder.append(" OFFSET ");
			queryBuilder.append(offset);
		}
	}

	@Override
	public String toSql(SelectQuery selectQuery) {
		visit(selectQuery);
		return queryBuilder.toString();
	}

	@Override
	public PreparedStatementData getPreparedStatementData(SelectQuery selectQuery) {
		buildPreparedStatement = true;
		visit(selectQuery);
		return new PreparedStatementData(queryBuilder.toString(), queryParameters);
	}

}
