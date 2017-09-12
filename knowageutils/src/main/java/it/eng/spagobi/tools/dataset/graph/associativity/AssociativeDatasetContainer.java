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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.sql.SqlUtils;

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
	protected final Set<String> filters = new HashSet<>();
	protected final Set<EdgeGroup> groups = new HashSet<>();

	protected boolean nearRealtime = false;
	private boolean resolved = false;

	private final int SQL_IN_CLAUSE_LIMIT = 999;

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

	public Set<String> getFilters() {
		return filters;
	}

	public boolean addFilter(String filter) {
		return filters.add(filter);
	}

	public boolean addFilters(Set<String> filters) {
		return this.filters.addAll(filters);
	}

	public boolean addFilter(String columnNames, Set<String> filterValues) {
		return filters.add(buildFilter(columnNames, filterValues));
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

	public Set<String> getTupleOfValues(String query) throws ClassNotFoundException, NamingException, SQLException {
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			logger.debug("Executing query: " + query);
			connection = dataSource.getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
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

	public String buildQuery(String columnNames) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ");
		sb.append(columnNames);
		sb.append(" FROM ");
		sb.append(tableName);
		if (!filters.isEmpty()) {
			sb.append(" WHERE ");
			sb.append(StringUtils.join(filters, " AND "));
		}
		return sb.toString();
	}

	public String encapsulateColumnName(String columnName) {
		return AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
	}

	public String buildFilter(String columnNames, Set<String> filterValues) {
		if (SqlUtils.hasSqlServerDialect(dataSource)) {
			return buildAndOrFilter(columnNames, filterValues);
		} else {
			return buildInFilter(columnNames, filterValues);
		}
	}

	public String buildInFilter(String columnNames, Set<String> filterValues) {
		String inClauseColumns;
		String inClauseValues;
		if (filterValues.size() > SQL_IN_CLAUSE_LIMIT) {
			inClauseColumns = "1," + columnNames;
			inClauseValues = AssociativeLogicUtils.getUnlimitedInClauseValues(filterValues);
		} else {
			inClauseColumns = columnNames;
			inClauseValues = StringUtils.join(filterValues, ",");
		}
		return "(" + inClauseColumns + ") IN (" + inClauseValues + ")";
	}

	public String buildAndOrFilter(String columnNames, Set<String> filterValues) {
		StringBuilder sb = new StringBuilder();
		String or = "";
		String[] distinctColumns = columnNames.split(",");

		for (String andOrValues : filterValues) {
			String and = "";
			String[] distinctValues = andOrValues.substring(1, andOrValues.length() - 1).split(",");

			sb.append(or);
			sb.append("(");

			for (int i = 0; i < distinctValues.length; i++) {
				String column = distinctColumns[i];
				String value = distinctValues[i];

				sb.append(and);
				sb.append(column);
				sb.append("=");
				sb.append(value);

				and = " AND ";
			}

			sb.append(")");

			or = " OR ";
		}
		return sb.toString();
	}
}
