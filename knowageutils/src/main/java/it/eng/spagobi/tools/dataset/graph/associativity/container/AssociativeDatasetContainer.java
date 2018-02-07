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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.SelectQuery;
import it.eng.spagobi.tools.dataset.cache.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.InFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.MultipleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.exceptions.IllegalEdgeGroupException;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.parameters.ParametersUtilities;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

abstract class AssociativeDatasetContainer implements IAssociativeDatasetContainer {

	static protected Logger logger = Logger.getLogger(AssociativeDatasetContainer.class);

	protected IDataSet dataSet;
	protected Map<String, String> parameters;
	protected final Set<SimpleFilter> filters = new HashSet<>();
	protected final Set<EdgeGroup> groups = new HashSet<>();
	protected final Set<EdgeGroup> usedGroups = new HashSet<>();

	protected boolean resolved = false;

	protected AssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		this.dataSet = dataSet;
		this.parameters = parameters;
	}

	protected abstract String getTableName();

	protected abstract IDataSource getDataSource();

	@Override
	public IDataSet getDataSet() {
		return dataSet;
	}

	@Override
	public boolean addFilter(SimpleFilter filter) {
		return filters.add(filter);
	}

	private boolean addFilter(List<String> columnNames, Set<Tuple> tuples) {
		return filters.add(buildInFilter(columnNames, tuples));
	}

	@Override
	public boolean update(EdgeGroup group, List<String> columnNames, Set<Tuple> tuples) {
		if (columnNames.isEmpty()) {
			return false;
		} else {
			usedGroups.add(group);
			if (!ParametersUtilities.containsParameter(columnNames)) {
				return addFilter(columnNames, tuples);
			} else {
				if (columnNames.size() == 1) {
					String parameter = columnNames.get(0);
					Set<String> values = new HashSet<>(tuples.size());
					for (Tuple tuple : tuples) {
						values.add(tuple.toString("", "", ""));
					}
					parameters.put(ParametersUtilities.getParameterName(parameter), StringUtils.join(values, ","));
					dataSet.setParamsMap(parameters);
					return true;
				} else {
					throw new IllegalEdgeGroupException("Columns " + columnNames
							+ " contain at least one parameter and more than one association. \nThis is a illegal state for an associative group.");
				}
			}
		}
	}

	@Override
	public Set<EdgeGroup> getGroups() {
		return groups;
	}

	@Override
	public Set<EdgeGroup> getUsedGroups() {
		return usedGroups;
	}

	@Override
	public boolean removeGroup(EdgeGroup group) {
		return groups.remove(group);
	}

	@Override
	public boolean addGroup(EdgeGroup group) {
		return groups.add(group);
	}

	@Override
	public Set<EdgeGroup> getUnresolvedGroups() {
		Set<EdgeGroup> unresolvedGroups = new HashSet<>();
		for (EdgeGroup group : groups) {
			if (!group.isResolved()) {
				unresolvedGroups.add(group);
			}
		}
		return unresolvedGroups;
	}

	protected abstract boolean isNearRealtime();

	@Override
	public boolean isResolved() {
		return resolved;
	}

	@Override
	public void resolve() {
		resolved = true;
	}

	@Override
	public void unresolve() {
		resolved = false;
	}

	@Override
	public void unresolveGroups() {
		for (EdgeGroup group : groups) {
			group.unresolve();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public abstract Set<Tuple> getTupleOfValues(List<String> columnNames) throws ClassNotFoundException, NamingException, SQLException;

	@Override
	public Set<Tuple> getTupleOfValues(String parameter) {
		return AssociativeLogicUtils.getTupleOfValues(parameters.get(ParametersUtilities.getParameterName(parameter)));
	}

	protected String buildQuery(List<String> columnNames) {
		return getSelectQuery(columnNames).toSql(getDataSource());
	}

	protected abstract String encapsulateColumnName(String columnName);

	protected SelectQuery getSelectQuery(List<String> columnNames) {
		SelectQuery selectQuery = new SelectQuery(dataSet).selectDistinct().select(columnNames.toArray(new String[0])).from(getTableName());
		if (!filters.isEmpty()) {
			selectQuery.where(new AndFilter(filters.toArray(new SimpleFilter[0])));
		}
		return selectQuery;
	}

	protected MultipleProjectionSimpleFilter buildInFilter(List<String> columnNames, Set<Tuple> tuples) {
		int columnCount = columnNames.size();
		List<Projection> projections = new ArrayList<Projection>(columnCount);
		List<IFieldMetaData> metaData = new ArrayList<IFieldMetaData>(columnCount);
		for (String columnName : columnNames) {
			projections.add(new Projection(dataSet, columnName));
			metaData.add(DataSetUtilities.getFieldMetaData(dataSet, columnName));
		}

		List<Object> values = new ArrayList<Object>();
		for (Tuple tuple : tuples) {
			values.addAll(tuple.getValues());
		}

		return new InFilter(projections, values);
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
		builder.append(", nearRealtime=");
		builder.append(isNearRealtime());
		builder.append(", resolved=");
		builder.append(resolved);
		builder.append("]");
		return builder.toString();
	}
}
