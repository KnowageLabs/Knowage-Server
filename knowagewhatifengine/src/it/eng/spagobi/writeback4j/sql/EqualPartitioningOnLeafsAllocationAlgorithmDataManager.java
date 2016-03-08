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

package it.eng.spagobi.writeback4j.sql;

import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.writeback4j.ISchemaRetriver;

import java.sql.Connection;
import java.sql.SQLException;

import org.olap4j.metadata.Member;

public class EqualPartitioningOnLeafsAllocationAlgorithmDataManager extends AbstractUpdatingAlgotithmsDataManager {
	private final boolean useInClause = true;
	private final IDataSource dataSource;

	public EqualPartitioningOnLeafsAllocationAlgorithmDataManager(ISchemaRetriver retriver, IDataSource dataSource) {
		this.retriver = retriver;
		this.dataSource = dataSource;
	}

	public long getLeafs(Member[] members) throws Exception {
		String queryString = getLeafsQuery(members);
		SqlQueryStatement updateStatement = new SqlQueryStatement(queryString);
		Connection connection;
		Long result = null;
		try {
			logger.debug("Getting the connection to DB");
			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			logger.error("Error opening connection to datasource " + dataSource.getLabel());
			throw new SpagoBIRuntimeException("Error opening connection to datasource " + dataSource.getLabel(), e);
		}
		try {

			result = new Long("" + updateStatement.getSingleValue(connection, "leafsValue"));
		} catch (WhatIfPersistingTransformationException e) {
			logger.debug("Error persisting the modifications", e);
			throw e;
		} finally {
			logger.debug("Closing the connection used to persist the modifications");
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Error closing the connection to the db");

			}
			logger.debug("Closed the connection used to persist the modifications");
		}

		return result;

	}

	private String getLeafsQuery(Member[] members) throws Exception {
		String queryWhere = buildWhereClauseForCell(members, useInClause);
		return "select count(*) as leafsValue from " + retriver.getEditCubeTableName() + " as " + getCubeAlias() + " " + queryWhere;
	}

	/**
	 * Build the update statement for the measure
	 * 
	 * @param buffer
	 *            the buffer of the query
	 * @param measure
	 *            the measure to update
	 * @param value
	 *            the value to assign to the leafs
	 */
	@Override
	protected void buildUpdate(StringBuffer buffer, Member measure, Object... values) throws SpagoBIEngineException {
		String measureColumn = null;
		try {
			measureColumn = retriver.getMeasureColumn(measure);
		} catch (SpagoBIEngineException e) {
			logger.error("Error loading the column for the table measure " + measure.getName(), e);
			throw new SpagoBIEngineException("Error loading the column for the table measure " + measure.getName(), e);
		}

		buffer.append("update ");
		buffer.append(retriver.getEditCubeTableName());
		buffer.append(" " + getCubeAlias());
		buffer.append(" set " + measureColumn + " = " + values[0]);

	}

}
