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

package it.eng.spagobi.tools.dataset.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.AssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class QBEAssociativeDatasetContainer extends AssociativeDatasetContainer {

	public QBEAssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		super(dataSet, null, dataSet.getDataSource(), parameters);
	}

	@Override
	public String getTableName() {
		String tableName = null;

		Assert.assertTrue(dataSet instanceof AbstractJDBCDataset || dataSet instanceof QbeDataSet,
				"Dataset need to be query or qbe dataset. Your dataset is: " + dataSet.getClass());

		String subQueryAlias = null;
		String aliasDelimiter = null;
		try {
			subQueryAlias = DataBaseFactory.getDataBase(dataSet.getDataSource()).getSubQueryAlias();
			aliasDelimiter = DataBaseFactory.getDataBase(dataSet.getDataSource()).getAliasDelimiter();
		} catch (DataBaseException e) {
			throw new SpagoBIRuntimeException("Error during the initializing of the AssociativeLogicManager", e);
		}

		QbeDataSet qbeDataSet = (QbeDataSet) dataSet;
		tableName = qbeDataSet.getStatement().getSqlQueryString();
		IMetaData metadata = qbeDataSet.getMetadata();
		for (int i = 0; i < metadata.getFieldCount(); i++) {
			IFieldMetaData fieldMeta = metadata.getFieldMeta(i);
			String alias = fieldMeta.getAlias();
			int col = tableName.indexOf(" as col_");
			int com = tableName.indexOf("_,") > -1 ? tableName.indexOf("_,") : tableName.indexOf("_ ");
			tableName = tableName.replace(tableName.substring(col, com + 1), " as " + aliasDelimiter + alias + aliasDelimiter);
		}
		tableName = "(\n" + tableName + "\n) " + subQueryAlias;
		return tableName;
	}

	@Override
	public IDataSource getDataSource() {
		return dataSet.getDataSource();
	}

	@Override
	public String encapsulateColumnName(String columnName) {
		return AbstractJDBCDataset.encapsulateColumnName(columnName, getDataSource());
	}

	@Override
	public PreparedStatementData buildPreparedStatementData(List<String> columnNames) throws DataBaseException {
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
