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

package it.eng.spagobi.tools.dataset.graph.associativity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.PreparedStatementData;
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
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.parameters.ParametersUtilities;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class AssociativeDatasetContainer {

	static protected Logger logger = Logger.getLogger(AssociativeDatasetContainer.class);

	protected final IDataSet dataSet;
	protected final String tableName;
	protected final IDataSource dataSource;
	protected Map<String, String> parameters;
	protected final Set<SimpleFilter> filters = new HashSet<>();
	protected final Set<EdgeGroup> groups = new HashSet<>();

	protected boolean nearRealtime = false;
	private boolean resolved = false;

	public AssociativeDatasetContainer(IDataSet dataSet, String tableName, IDataSource dataSource, Map<String, String> parameters) {
		this.dataSet = dataSet;
		this.tableName = tableName;
		this.dataSource = dataSource;
		this.parameters = parameters;
	}

	public IDataSet getDataSet() {
		return dataSet;
	}

	public String getTableName() {
		return tableName;
	}

	public IDataSource getDataSource() {
		return dataSource;
	}

	public Set<SimpleFilter> getFilters() {
		return filters;
	}

	public boolean addFilter(SimpleFilter filter) {
		return filters.add(filter);
	}

	public boolean addFilters(Set<SimpleFilter> filters) {
		return this.filters.addAll(filters);
	}

	public boolean addFilter(List<String> columnNames, Set<Tuple> tuples) {
		return filters.add(buildInFilter(columnNames, tuples));
	}

	public boolean update(List<String> columnNames, Set<Tuple> tuples) {
		if (columnNames.isEmpty()) {
			return false;
		} else {
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

	public Set<EdgeGroup> getGroups() {
		return groups;
	}

	public boolean removeGroup(EdgeGroup group) {
		return groups.remove(group);
	}

	public boolean addGroup(EdgeGroup group) {
		return groups.add(group);
	}

	public Set<EdgeGroup> getUnresolvedGroups() {
		Set<EdgeGroup> unresolvedGroups = new HashSet<>();
		for (EdgeGroup group : groups) {
			if (!group.isResolved()) {
				unresolvedGroups.add(group);
			}
		}
		return unresolvedGroups;
	}

	public boolean isNearRealtime() {
		return nearRealtime;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void resolve() {
		resolved = true;
	}

	public void unresolve() {
		resolved = false;
	}

	public void unresolveGroups() {
		for (EdgeGroup group : groups) {
			group.unresolve();
		}
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public Set<Tuple> getTupleOfValues(List<String> columnNames) throws ClassNotFoundException, NamingException, SQLException, DataBaseException {
		PreparedStatementData data = buildPreparedStatementData(columnNames);
		String query = data.getQuery();
		List<Object> values = data.getValues();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			logger.debug("Executing query: " + query);
			connection = dataSource.getConnection();
			stmt = connection.prepareStatement(query);
			for (int i = 0; i < values.size(); i++) {
				stmt.setObject(i + 1, values.get(i));
			}
			stmt.execute();
			rs = stmt.getResultSet();
			return AssociativeLogicUtils.getTupleOfValues(rs);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.debug(e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.debug(e);
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.debug(e);
				}
			}
		}
	}

	public Set<Tuple> getTupleOfValues(String parameter) {
		return AssociativeLogicUtils.getTupleOfValues(parameters.get(ParametersUtilities.getParameterName(parameter)));
	}

	public String buildQuery(List<String> columnNames) throws DataBaseException {
		return getSelectQuery(columnNames).toSql(dataSource);
	}

	public PreparedStatementData buildPreparedStatementData(List<String> columnNames) throws DataBaseException {
		return getSelectQuery(columnNames).getPreparedStatementData(dataSource);
	}

	private SelectQuery getSelectQuery(List<String> columnNames) {
		SelectQuery selectQuery = new SelectQuery(dataSet).selectDistinct().select(columnNames.toArray(new String[0])).from(tableName);
		if (!filters.isEmpty()) {
			selectQuery.where(new AndFilter(filters.toArray(new SimpleFilter[0])));
		}
		return selectQuery;
	}

	public String encapsulateColumnName(String columnName) {
		return AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
	}

	public MultipleProjectionSimpleFilter buildInFilter(List<String> columnNames, Set<Tuple> tuples) {
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
		builder.append(tableName);
		builder.append(", dataSource=");
		builder.append(dataSource);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append(", filters=");
		builder.append(filters);
		builder.append(", groups=");
		builder.append(groups);
		builder.append(", nearRealtime=");
		builder.append(nearRealtime);
		builder.append(", resolved=");
		builder.append(resolved);
		builder.append("]");
		return builder.toString();
	}
}
