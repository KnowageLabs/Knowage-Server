/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.writeback4j.sql;

import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.writeback4j.IMemberCoordinates;
import it.eng.spagobi.writeback4j.ISchemaRetriver;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class AnalysisExporter extends AbstractSqlSchemaManager {

	private static transient Logger logger = Logger.getLogger(AnalysisExporter.class);
	private final PivotModel model;

	public AnalysisExporter(PivotModel model, ISchemaRetriver retriver) {
		this.retriver = retriver;
		this.model = model;
	}

	/**
	 * Export the output table in CSV
	 * 
	 * @param connection
	 *            the connection to the output table
	 * @param version
	 *            the version to export
	 * @param fieldSeparator
	 *            the separator of fields
	 * @param lineSeparator
	 *            the separator between lines
	 * @return
	 * @throws Exception
	 */
	public byte[] exportCSV(Connection connection, Integer version, String fieldSeparator, String lineSeparator) throws Exception {

		byte[] toReturn = null;

		logger.debug("IN");
		ResultSet resultSet = executeExportDataQuery(connection, version);

		logger.debug("Initializing the output stream");
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(fos);

		try {
			logger.debug("Starts to navigate the result set");
			int ncols = resultSet.getMetaData().getColumnCount();
			for (int j = 1; j < (ncols + 1); j++) {
				out.append(resultSet.getMetaData().getColumnName(j));
				if (j < ncols) {
					out.append(fieldSeparator);
				} else {
					out.append(lineSeparator);
				}
			}

			while (resultSet.next()) {

				for (int k = 1; k < (ncols + 1); k++) {

					out.append(resultSet.getString(k));

					if (k < ncols) {
						out.append(fieldSeparator);
					} else {
						out.append(lineSeparator);
					}
				}

			}

			out.flush();

			logger.debug("Finished to navigate the result set");
			toReturn = fos.toByteArray();
			logger.debug("OUT");
		} catch (Exception e) {
			out.close();
		}

		return toReturn;
	}

	/**
	 * Export the output table in an external table
	 * 
	 * @param connection
	 *            the connection to the output table
	 * @param version
	 *            the version to export
	 * @param writeDataSource
	 *            the datasource for the new table
	 * @param tableName
	 *            the new table name
	 * @throws Exception
	 */
	public void exportTable(Connection connection, IDataSource readDataSource, IDataSource writeDataSource, Integer version, String tableName) throws Exception {
		logger.debug("IN");
		String sqlStatement = buildExportDataQuery(connection, version);
		logger.debug("Sql statement built. Peristing the table");
		JDBCDataSet dataset = new JDBCDataSet();
		dataset.setDataSource(readDataSource);
		dataset.setQuery(sqlStatement);

		PersistedTableManager persister = new PersistedTableManager();
		persister.persistDataSet(dataset, writeDataSource, tableName);
		logger.debug("OUT");
	}

	/**
	 * Execute the export query
	 * 
	 * @param connection
	 *            the connection to the output table
	 * @param version
	 *            the version to export
	 * @return
	 * @throws Exception
	 */
	private ResultSet executeExportDataQuery(Connection connection, Integer version) throws Exception {
		logger.debug("IN");
		String queryString = buildExportDataQuery(connection, version);
		logger.debug("Sql statement built.");
		SqlQueryStatement exportStatement = new SqlQueryStatement(queryString);
		ResultSet resultset = exportStatement.getValues(connection);
		logger.debug("OUT");
		return resultset;
	}

	/**
	 * Builds the export query
	 * 
	 * @param connection
	 *            the connection to the output table
	 * @param version
	 *            the version to export
	 * @return
	 * @throws Exception
	 */
	private String buildExportDataQuery(Connection connection, Integer version) throws Exception {
		logger.debug("IN");

		// get the coordinates of a cell (used only to get the involved
		// dimensions)
		SpagoBICellWrapper cellWrapper = (SpagoBICellWrapper) model.getCellSet().getCell(0);
		List<IMemberCoordinates> memberCordinates = new ArrayList<IMemberCoordinates>();
		Member[] members = cellWrapper.getMembers();

		// gets the measures and the coordinates of the dimension members
		for (int i = 0; i < members.length; i++) {
			Member aMember = members[i];
			if (!(aMember.getDimension().getDimensionType().equals(Type.MEASURE))) {
				memberCordinates.add(retriver.getMemberCordinates(aMember));
			}
		}

		logger.debug("The coordinates are " + memberCordinates);
		// List of where conditions
		Map<TableEntry, String> whereConditions = new HashMap<TableEntry, String>();

		// List of joins
		Set<EquiJoin> joinConditions = new HashSet<EquiJoin>();

		// List of form
		Set<String> fromTables = new HashSet<String>();

		StringBuffer degenerateDimensionConditions = new StringBuffer();

		Map<String, String> table2Alias = new HashMap<String, String>();
		getTableAlias(table2Alias, getCubeAlias());

		// for each dimension get the clauses
		for (Iterator<IMemberCoordinates> iterator = memberCordinates.iterator(); iterator.hasNext();) {
			IMemberCoordinates aIMemberCordinates = iterator.next();

			logger.debug("Exploring the coordinate " + aIMemberCordinates.toString());
			if (aIMemberCordinates.getTableName() == null) {// degenerate
															// dimension
				// create a where in the cube for each level of the degenerate
				// dimension
				Map<TableEntry, String> where = buildWhereConditions(aIMemberCordinates, null, version);
				Map<String, String> cubeTable2Alias = new HashMap<String, String>();
				cubeTable2Alias.put(null, getCubeAlias());
				addWhereCondition(degenerateDimensionConditions, where, cubeTable2Alias, null);
			} else {
				// join condition between the dimension and the cube
				addJoinConditions(fromTables, joinConditions, aIMemberCordinates, true);
				// join condition for the tables inside the dimension
				addInnerDimensionJoinConditions(fromTables, joinConditions, aIMemberCordinates);
			}

			if (aIMemberCordinates.getDimensionName().equalsIgnoreCase(WhatIfConstants.VERSION_DIMENSION_NAME)) {
				Map<TableEntry, String> where = buildWhereConditions(aIMemberCordinates, null, version);
				addWhereCondition(degenerateDimensionConditions, where, table2Alias, version);
			}
		}

		logger.debug("Starting to build the query");
		StringBuffer query = new StringBuffer();
		buildQueryForExport(memberCordinates, whereConditions, joinConditions, fromTables, query, table2Alias);
		query.append(" and ");
		query.append(degenerateDimensionConditions);
		String queryString = query.toString();

		logger.debug("OUT");
		return queryString;
	}

	private void buildQueryForExport(List<IMemberCoordinates> memberCordinates, Map<TableEntry, String> whereConditions, Set<EquiJoin> joinConditions, Set<String> fromTables,
			StringBuffer query, Map<String, String> table2Alias) {
		logger.debug("IN");

		StringBuffer from = new StringBuffer();
		StringBuffer where = new StringBuffer();
		query = query.append("select ");

		buildSelectClauseForExport(memberCordinates, table2Alias, query);

		// adding in the from clause the cube
		fromTables.add(retriver.getEditCubeTableName());
		table2Alias.put(retriver.getEditCubeTableName(), getCubeAlias());

		addWhereCondition(where, joinConditions, table2Alias);
		addWhereCondition(where, whereConditions, table2Alias, null);
		addFromConditions(from, fromTables, table2Alias);

		query.append(" from ");
		query.append(from);
		query.append(" where ");
		query.append(where);
		logger.debug("OUT");
	}

	/**
	 * Build the select clause. Get a select statement for each levels of all
	 * dimensions
	 * 
	 * @param memberCordinates
	 * @param table2Alias
	 * @param query
	 */
	private void buildSelectClauseForExport(List<IMemberCoordinates> memberCordinates, Map<String, String> table2Alias, StringBuffer query) {
		logger.debug("IN");
		List<String> selects = new ArrayList<String>();

		// for each dimension get the columns of each level
		for (Iterator<IMemberCoordinates> iterator = memberCordinates.iterator(); iterator.hasNext();) {
			IMemberCoordinates aIMemberCordinates = iterator.next();
			if (aIMemberCordinates.getTableName() != null) {// not degenerate
															// dimension
				List<TableEntry> levels = aIMemberCordinates.getLevels();
				for (Iterator<TableEntry> tableIterator = levels.iterator(); tableIterator.hasNext();) {
					TableEntry entry = tableIterator.next();
					String cluse = (entry.toString(table2Alias, this));
					if (!selects.contains(cluse)) {
						selects.add(cluse);
					}
				}
			}
		}

		// get the columns of the measures
		List<String> measuresColumns = retriver.getMeasuresColumn();
		for (Iterator<String> iterator = measuresColumns.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			String cluse = (getCubeAlias() + "." + string);
			if (!selects.contains(cluse)) {
				selects.add(cluse);
			}
		}

		for (Iterator<String> iterator = selects.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			query.append(string);
			query.append(" ,");
		}

		query.deleteCharAt(query.length() - 1);
		logger.debug("OUT");
	}

}
