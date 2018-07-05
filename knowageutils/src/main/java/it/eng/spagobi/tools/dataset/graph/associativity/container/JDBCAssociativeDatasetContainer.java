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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.PreparedStatementData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;

public class JDBCAssociativeDatasetContainer extends AssociativeDatasetContainer {

	public JDBCAssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		super(dataSet, parameters);
	}

	@Override
	protected String getTableName() {
		QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
		String subQueryAlias = "";
		try {
			subQueryAlias = DataBaseFactory.getDataBase(dataSet.getDataSource()).getSubQueryAlias();
		} catch (DataBaseException e) {
			logger.error("Error while retrieving Database type");
		}

		return "(" + querableBehaviour.getStatement() + ") " + subQueryAlias;
	}

	@Override
	protected IDataSource getDataSource() {
		return dataSet.getDataSource();
	}

	@Override
	protected String encapsulateColumnName(String columnName) {
		return AbstractJDBCDataset.encapsulateColumnName(columnName, getDataSource());
	}

	private PreparedStatementData buildPreparedStatementData(List<String> columnNames) throws DataBaseException {
		return getSelectQuery(columnNames).getPreparedStatementData(getDataSource());
	}

	@Override
	public Set<Tuple> getTupleOfValues(List<String> columnNames) throws ClassNotFoundException, NamingException, SQLException, DataBaseException {
		PreparedStatementData data = buildPreparedStatementData(columnNames);
		String query = data.getQuery();
		List<Object> values = data.getValues();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			logger.debug("Executing query: " + query);
			connection = getDataSource().getConnection();
			stmt = connection.prepareStatement(query);
			for (int i = 0; i < values.size(); i++) {
				int parameterIndex = i + 1;
				Object value = values.get(i);
				if (java.util.Date.class.isAssignableFrom(value.getClass())) {
					java.util.Date date = (java.util.Date) value;
					stmt.setDate(parameterIndex, new java.sql.Date(date.getTime()));
				} else {
					stmt.setObject(parameterIndex, value);
				}
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

	@Override
	public boolean isNearRealtime() {
		return false;
	}
}
