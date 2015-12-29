/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
