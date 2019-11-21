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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.persist.PersistedTableHelper;
import it.eng.spagobi.tools.dataset.utils.InlineViewUtility;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class InlineViewAssociativeDatasetContainer extends JdbcDatasetContainer {

	private static final Logger logger = Logger.getLogger(InlineViewAssociativeDatasetContainer.class);

	public InlineViewAssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		super(dataSet, parameters);
	}

	@Override
	protected String getTableName() {
		try {
			return InlineViewUtility.getTableName(dataSet);
		} catch (DataBaseException e) {
			logger.error("Error occured while getting table name", e);
			throw new SpagoBIRuntimeException("Error occured while getting table name", e);
		}
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
				PersistedTableHelper.addField(stmt, i, value, "", value.getClass().getName(), false, new HashMap<String, Integer>());
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
}
