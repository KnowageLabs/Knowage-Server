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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCatalogFunctionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NotInFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.AbstractDataBase;
import it.eng.spagobi.utilities.database.IDataBase;

public abstract class AbstractSelectQueryVisitor extends AbstractFilterVisitor implements ISelectQueryVisitor {

	protected static final String ALIAS_PREFIX = "AS";

	protected static final String DATE_TIME_FORMAT_SQL_STANDARD = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "YYYY").replace("MM", "MM")
			.replace("dd", "DD").replace("HH", "HH24").replace("mm", "MI").replace("ss", "SS");

	protected boolean buildPreparedStatement;
	protected boolean useNameAsAlias;
	protected IDataBase database;
	protected String aliasPrefix;
	protected String aliasDelimiter;
	protected List<Object> queryParameters;

	public AbstractSelectQueryVisitor(IDataBase database) {
		this.buildPreparedStatement = false;
		this.useNameAsAlias = false;
		this.database = database;
		this.aliasDelimiter = database.getAliasDelimiter();
		this.aliasPrefix = ALIAS_PREFIX;
		this.queryParameters = new ArrayList<>();
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

	@Override
	public void visit(UnaryFilter item) {
		if (item.getOperand() == null) {
			if (SimpleFilterOperator.EQUALS_TO.equals(item.getOperator())) {
				visit(new NullaryFilter(item.getProjection(), SimpleFilterOperator.IS_NULL));
			} else if (SimpleFilterOperator.DIFFERENT_FROM.equals(item.getOperator())) {
				visit(new NullaryFilter(item.getProjection(), SimpleFilterOperator.IS_NOT_NULL));
			} else {
				throw new IllegalArgumentException("Invalid use of operator " + item.getOperator() + " with NULL");
			}
		} else {
			append(item.getProjection(), false);
			queryBuilder.append(" ");
			queryBuilder.append(item.getOperator());
			queryBuilder.append(" ");
			append(item.getOperand());
		}
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
					queryBuilder.append(((String) operand).replaceAll("'", "''"));
					queryBuilder.append("'");
				} else {
					queryBuilder.append(operand);
				}
			}
		}
	}

	@Override
	protected String getFormattedTimestamp(Timestamp timestamp) {
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
	protected String getFormattedDate(Date date) {
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
	public void visit(NotInFilter item) {
		if (item.getOperands().isEmpty()) {
			visit(new UnsatisfiedFilter());
		} else {
			if (hasNullOperand(item) || !database.getDatabaseDialect().isSingleColumnInOperatorSupported()
					|| (item.getProjections().size() > 1 && !database.getDatabaseDialect().isMultiColumnInOperatorSupported())) {
				queryBuilder.append("(");
				visit(transformToAndOrFilters(item));
				queryBuilder.append(")");
			} else {
				append(item);
			}
		}
	}

	private boolean hasNullOperand(NotInFilter item) {
		boolean hasNullOperand = false;
		if (item != null) {
			for (Object operand : item.getOperands()) {
				if (operand == null) {
					hasNullOperand = true;
					break;
				}
			}
		}
		return hasNullOperand;
	}

	protected void append(NotInFilter item) {
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
	public void visit(InFilter item) {
		if (item.getOperands().isEmpty()) {
			visit(new UnsatisfiedFilter());
		} else {
			if (hasNullOperand(item) || !database.getDatabaseDialect().isSingleColumnInOperatorSupported()
					|| (item.getProjections().size() > 1 && !database.getDatabaseDialect().isMultiColumnInOperatorSupported())) {
				queryBuilder.append("(");
				visit(transformToAndOrFilters(item));
				queryBuilder.append(")");
			} else {
				append(item);
			}
		}
	}

	private boolean hasNullOperand(InFilter item) {
		boolean hasNullOperand = false;
		if (item != null) {
			for (Object operand : item.getOperands()) {
				if (operand == null) {
					hasNullOperand = true;
					break;
				}
			}
		}
		return hasNullOperand;
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
		String value = item.isPattern() ? item.getValue() : "%" + item.getValue() + "%";
		if (buildPreparedStatement) {
			queryBuilder.append(" ?");
			queryParameters.add(value);
		} else {
			queryBuilder.append(" '");
			queryBuilder.append(value);
			queryBuilder.append("'");
		}
	}

	/**
	 * @param projection
	 * @param useAlias   enables the output of an alias. Column name is used as alias if related flag is true.
	 */
	protected void append(Projection projection, boolean useAlias) {
		String aliasDelimiter = database.getAliasDelimiter();
		IAggregationFunction aggregationFunction = projection.getAggregationFunction();

		String name = projection.getName();
		/*
		 * TODO: Remove isCalculatedColumn or change logic
		 */
		String columnName = isCalculatedColumn(name) ? name.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, aliasDelimiter)
				: aliasDelimiter + name + aliasDelimiter;

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

	// TODO: improve auto columns detection using ANTLR VISITOR
	/**
	 * @param DataStoreCalculatedField
	 * @param useAlias                 enables the output of an alias. Column name is used as alias if related flag is true.
	 */
	protected void append(DataStoreCalculatedField projection, boolean useAlias) {
		String aliasDelimiter = database.getAliasDelimiter();
		IAggregationFunction aggregationFunction = projection.getAggregationFunction();

		String name = projection.getFormula();
		String columnName = name.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, aliasDelimiter);

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

	public boolean isCalculatedColumn(String columnName) {
		return columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER);
	}

	protected void append(Sorting item) {
		String aliasDelimiter = database.getAliasDelimiter();
		AbstractSelectionField projs = item.getProjection();

		if (!projs.getClass().equals(DataStoreCalculatedField.class)) {
			Projection projection = (Projection) projs;
			IAggregationFunction aggregationFunction = projection.getAggregationFunction();

			String name = projection.getName();
			String columnName = isCalculatedColumn(name) ? name.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, aliasDelimiter)
					: aliasDelimiter + name + aliasDelimiter;

			if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
				queryBuilder.append(columnName);
			} else {
				queryBuilder.append(aggregationFunction.apply(columnName));
			}

			queryBuilder.append(item.isAscending() ? " ASC" : " DESC");
		} else {
			DataStoreCalculatedField projection = (DataStoreCalculatedField) projs;
			IAggregationFunction aggregationFunction = projection.getAggregationFunction();

			String name = projection.getAlias();
			String columnName = isCalculatedColumn(name) ? name.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, aliasDelimiter)
					: aliasDelimiter + name + aliasDelimiter;

			if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
				queryBuilder.append(columnName);
			} else {
				queryBuilder.append(aggregationFunction.apply(columnName));
			}

			queryBuilder.append(item.isAscending() ? " ASC" : " DESC");
		}
	}

	@Override
	public void visit(UnsatisfiedFilter item) {
		queryBuilder.append(" 0=1");
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
		boolean selectAll = query.hasSelectAll();
		boolean selectCount = query.hasSelectCount();
		Assert.assertTrue(!(selectAll && selectCount), "Invalid projections definition");

		String tableName = query.getTableName();
		Assert.assertTrue(StringUtilities.isNotEmpty(tableName), "Missing table definition");

		Assert.assertTrue(query.hasLimit() || !query.hasOffset(), "Invalid offset definition (missing limit)");
	}

	protected void buildSelect(SelectQuery query) {
		queryBuilder.append("SELECT ");

		appendSelectDistinct(query);

		if (query.hasSelectAll() || query.getProjections().isEmpty()) {
			queryBuilder.append("* ");
		} else if (query.hasSelectCount()) {
			queryBuilder.append("COUNT(*) ");
		} else {
			List<AbstractSelectionField> projectionsAbs = query.getProjections();
			List<Projection> projections = new ArrayList<Projection>();
			List<DataStoreCalculatedField> projectionsCalcFields = new ArrayList<DataStoreCalculatedField>();
			List<DataStoreCatalogFunctionField> projectionsCataolgFunctionsFields = new ArrayList<DataStoreCatalogFunctionField>();
			for (AbstractSelectionField abstractSelectionField : projectionsAbs) {
				if (!abstractSelectionField.getClass().equals(DataStoreCalculatedField.class)
						&& !abstractSelectionField.getClass().equals(DataStoreCatalogFunctionField.class)) {
					Projection proj = (Projection) abstractSelectionField;
					projections.add(proj);
				} else if (abstractSelectionField.getClass().equals(DataStoreCatalogFunctionField.class)) {
					DataStoreCatalogFunctionField projCatalogFunc = (DataStoreCatalogFunctionField) abstractSelectionField;
					projectionsCataolgFunctionsFields.add(projCatalogFunc);
				} else {
					DataStoreCalculatedField projCalc = (DataStoreCalculatedField) abstractSelectionField;
					projectionsCalcFields.add(projCalc);
				}
			}
			if (projections == null || projections.isEmpty()) {
				return;
			}

			append(projections.get(0), true);
			for (int i = 1; i < projections.size(); i++) {
				queryBuilder.append(", ");
				append(projections.get(i), true);
			}

			// added another management for calculated fields

			for (int i = 0; i < projectionsCalcFields.size(); i++) {
				queryBuilder.append(", ");
				append(projectionsCalcFields.get(i), true);
			}

			List<AbstractSelectionField> groups = query.getGroups();
			List<Sorting> sortings = query.getSortings();
			if (groups != null && !groups.isEmpty() && sortings != null && !sortings.isEmpty()) {
				for (Sorting sorting : sortings) {
					AbstractSelectionField projs = sorting.getProjection();
					boolean projectionAlreadyDefined = false;
					if (!projs.getClass().equals(DataStoreCalculatedField.class)) {
						Projection projection = (Projection) projs;

						for (Projection p : projections) {
							if (p.getDataset().equals(projection.getDataset()) && p.getName().equals(projection.getName())) {
								projectionAlreadyDefined = true;
								break;
							}
						}
						if (!projectionAlreadyDefined) {
							IAggregationFunction aggregationFunction = projection.getAggregationFunction();
							if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
								queryBuilder.append(",");
								append(projection, false);
							}
						}

					} else {
						DataStoreCalculatedField projection = (DataStoreCalculatedField) projs;
						for (DataStoreCalculatedField p : projectionsCalcFields) {
							if (p.getDataset().equals(projection.getDataset()) && p.getName().equals(projection.getName())) {
								projectionAlreadyDefined = true;
								break;
							}
						}
						if (!projectionAlreadyDefined) {
							IAggregationFunction aggregationFunction = projection.getAggregationFunction();
							if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
								queryBuilder.append(",");
								append(projection, false);
							}
						}

					}
				}
			}
		}
	}

	protected void appendSelectDistinct(SelectQuery query) {

		/*
		 * WORKAROUND - KNOWAGE-5361 : exclude DISTINCT clause for all the dataset that contains a CLOB.
		 */
		boolean containsCLOB = false;
		if (query.getDataSet().getDsMetadata() != null && query.getDataSet().getDsMetadata().contains("CLOB"))
			containsCLOB = true;

		if (query.isSelectDistinct() && query.getGroups().isEmpty() && !query.hasAggregationFunction() && !containsCLOB) {
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
		List<AbstractSelectionField> groups = query.getGroups().stream().filter(g -> (g instanceof DataStoreCatalogFunctionField) == false)
				.collect(Collectors.toList());
		if (groups == null || groups.isEmpty()) {
			return;
		}

		queryBuilder.append(" GROUP BY ");

		if (groups.get(0) instanceof Projection) {
			append((Projection) groups.get(0), false);
		} else {
			append((DataStoreCalculatedField) groups.get(0), false);
		}

		for (int i = 1; i < groups.size(); i++) {
			queryBuilder.append(",");

			if (groups.get(i) instanceof Projection) {
				append((Projection) groups.get(i), false);
			} else {
				append((DataStoreCalculatedField) groups.get(i), false);
			}
		}

		List<Sorting> sortings = query.getSortings();
		if (sortings != null && !sortings.isEmpty()) {
			for (Sorting sorting : sortings) {
				AbstractSelectionField projs = sorting.getProjection();

				if (!projs.getClass().equals(DataStoreCalculatedField.class)) {
					Projection projection = (Projection) projs;
					boolean projectionAlreadyDefined = false;
					for (AbstractSelectionField g : groups) {
						if (g instanceof Projection) {
							Projection proj = (Projection) g;
							if (proj.getDataset().equals(projection.getDataset()) && proj.getName().equals(projection.getName())) {
								projectionAlreadyDefined = true;
								break;
							}
						} else {
							DataStoreCalculatedField calc = (DataStoreCalculatedField) g;
							if (calc.getDataset().equals(projection.getDataset()) && calc.getName().equals(projection.getName())) {
								projectionAlreadyDefined = true;
								break;
							}
						}
					}
					if (!projectionAlreadyDefined) {
						IAggregationFunction aggregationFunction = projection.getAggregationFunction();
						if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
							queryBuilder.append(",");
							append(projection, false);
						}
					}

				} else {

					DataStoreCalculatedField projection = (DataStoreCalculatedField) projs;
					boolean projectionAlreadyDefined = false;
					for (AbstractSelectionField g : groups) {
						if (g instanceof Projection) {
							Projection proj = (Projection) g;
							if (proj.getDataset().equals(projection.getDataset()) && proj.getName().equals(projection.getName())) {
								projectionAlreadyDefined = true;
								break;
							}
						} else {
							DataStoreCalculatedField calc = (DataStoreCalculatedField) g;
							if (calc.getDataset().equals(projection.getDataset()) && calc.getName().equals(projection.getName())) {
								projectionAlreadyDefined = true;
								break;
							}
						}
					}
					if (!projectionAlreadyDefined) {
						IAggregationFunction aggregationFunction = projection.getAggregationFunction();
						if (aggregationFunction == null || AggregationFunctions.NONE_FUNCTION.equals(aggregationFunction)) {
							// queryBuilder.append(",");
							// append(projection, false);
						}
					}

				}

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
		return getPreparedStatementData(selectQuery, true);
	}

	/**
	 * @return descriptor for a real prepared statement if param buildPreparedStatement is true, otherwise a descriptor for a regular statement with all values
	 *         already resolved
	 */
	protected PreparedStatementData getPreparedStatementData(SelectQuery selectQuery, boolean buildPreparedStatement) {
		this.buildPreparedStatement = buildPreparedStatement;
		visit(selectQuery);
		return new PreparedStatementData(queryBuilder.toString(), queryParameters);
	}

}
