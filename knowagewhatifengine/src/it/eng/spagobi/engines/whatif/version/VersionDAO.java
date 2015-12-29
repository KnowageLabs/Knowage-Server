/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif.version;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.writeback4j.sql.SqlInsertStatement;
import it.eng.spagobi.writeback4j.sql.SqlQueryStatement;
import it.eng.spagobi.writeback4j.sql.SqlUpdateStatement;
import it.eng.spagobi.writeback4j.sql.dbdescriptor.IDbSchemaDescriptor;
import it.eng.spagobi.writeback4j.sql.dbdescriptor.JdbcTableDescriptor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Giulio Gavardi (giulio.gavardi@eng.it)
 * 
 */
public class VersionDAO {

	private final WhatIfEngineInstance instance;
	private final String editCubeTableName;
	private final IDbSchemaDescriptor descriptor;

	public static transient Logger logger = Logger.getLogger(VersionDAO.class);

	public VersionDAO(WhatIfEngineInstance instance) {
		this.instance = instance;

		editCubeTableName = instance.getWriteBackManager().getRetriver().getEditCubeTableName();
		logger.debug("Edit table name is: " + editCubeTableName);

		// defining the table descriptor
		descriptor = new JdbcTableDescriptor();
	}

	public Integer getLastVersion(Connection connection) throws SpagoBIEngineException, NumberFormatException {
		logger.debug("IN");
		logger.debug("get Last version");

		Integer lastVersion;

		try {
			String sqlQuery = "select MAX(" + getVersionColumnName() + ") as " + getVersionColumnName() + " from " + getVersionTableName();
			SqlQueryStatement queryStatement = new SqlQueryStatement(sqlQuery);
			Object o = queryStatement.getSingleValue(connection, getVersionColumnName());
			if (o != null) {
				logger.debug("Last version is " + o);

				// Oracle case
				if (o instanceof BigDecimal) {
					lastVersion = ((BigDecimal) o).intValue();
				}
				else {
					lastVersion = (Integer) o;
				}
			}
			else {
				logger.debug("No last version found, it is assumed to be 0");
				lastVersion = 0;
			}
		} catch (NumberFormatException e) {
			logger.error("Error in converting to Integer the version value: check your db settings for version column", e);
			throw e;
		} catch (SpagoBIEngineException e) {
			logger.error("Error when recovering last model version", e);
			throw e;
		}
		logger.debug("OUT");
		return lastVersion;
	}

	public String increaseActualVersion(Connection connection, Integer lastVersion, Integer newVersion, String name, String descr) throws SpagoBIEngineException {
		logger.debug("IN");

		if (descr == null) {
			name = "" + newVersion;
			descr = "" + newVersion;
		}

		String sqlInsertIntoVirtual = null;
		String insertIntoVersion = "insert into " + getVersionTableName() + " ( " + getVersionColumnName() + " , " + WhatIfConstants.WBVERSION_COLUMN_NAME + " , "
				+ WhatIfConstants.WBVERSION_COLUMN_DESCRIPTION + ") values (" + newVersion + ",'" + name + "','" + descr + "')";

		logger.debug("Data duplication");

		try {

			long dateBefore = System.currentTimeMillis();

			logger.debug("Inserting the new version in the dimension table");
			SqlInsertStatement insertStatement = new SqlInsertStatement(insertIntoVersion);
			insertStatement.executeStatement(connection);

			logger.debug("Inserting in the cube the new version");
			sqlInsertIntoVirtual = buildInserttoDuplicateDataStatment(lastVersion, newVersion);
			logger.debug("The query for the new version is " + sqlInsertIntoVirtual);
			insertStatement = new SqlInsertStatement(sqlInsertIntoVirtual);
			insertStatement.executeStatement(connection);

			long dateAfter = System.currentTimeMillis();
			logger.debug("Time to insert the new version " + (dateAfter - dateBefore));
		} catch (SpagoBIEngineException e) {
			logger.error("Error in increasing version procedure: error when duplicating data and changing version", e);
			throw e;
		}

		logger.debug("OUT");
		return sqlInsertIntoVirtual;
	}

	private String buildInserttoDuplicateDataStatment(Integer lastVersion, Integer newVersion) {
		logger.debug("IN");

		String columnsListString = "";
		String columnsListStringVersionWritten = "";

		for (Iterator<String> iterator = descriptor.getColumnNames(editCubeTableName, instance.getDataSource()).iterator(); iterator.hasNext();) {
			String s = iterator.next();

			if (s.equalsIgnoreCase(getVersionColumnName())) {
				columnsListString += " " + s + " ";
				columnsListStringVersionWritten += " " + (newVersion) + " ";

			}
			else {
				columnsListString += " " + s + " ";
				columnsListStringVersionWritten += " " + s + " ";

			}

			if (iterator.hasNext()) {
				columnsListString += ",";
				columnsListStringVersionWritten += ",";
			}
		}

		logger.debug("Columns of virtual table are: " + columnsListString);

		String statement = "";

		statement = "insert into " + editCubeTableName + " (" + columnsListString + ") "
				+ " select " + columnsListStringVersionWritten + " from " + editCubeTableName
				+ " where " + getVersionColumnName() + "=" + (lastVersion);

		logger.debug("Statement for duplicating data of last version: " + statement);

		logger.debug("OUT");
		return statement;
	}

	public List<SbiVersion> getAllVersions(Connection connection) throws SpagoBIEngineException, SQLException {
		logger.debug("IN");
		String sqlQuery = "select " + getVersionColumnName() + " as versionIdColumn, " + WhatIfConstants.WBVERSION_COLUMN_NAME + ", "
				+ WhatIfConstants.WBVERSION_COLUMN_DESCRIPTION + "  from " + getVersionTableName();
		SqlQueryStatement queryStatement = new SqlQueryStatement(sqlQuery);
		ResultSet resulSet = queryStatement.getValues(connection);
		List<SbiVersion> versions = fromResultSetToSbiVersions(resulSet);
		logger.debug("OUT");
		return versions;
	}

	public void deleteVersions(Connection connection, String versionIds) throws Exception {
		logger.debug("IN");
		logger.debug("Deleting the versions " + versionIds + " from the cube");
		String sqlQuery = "delete from " + editCubeTableName + " where " + getVersionColumnName() + " in (" + versionIds + ")";
		SqlUpdateStatement queryStatement = new SqlUpdateStatement(sqlQuery);
		queryStatement.executeStatement(connection);

		logger.debug("Deleting the versions " + versionIds + " from the version dimension");
		sqlQuery = "delete from " + getVersionTableName() + " where " + getVersionColumnName() + " in (" + versionIds + ")";
		queryStatement = new SqlUpdateStatement(sqlQuery);
		queryStatement.executeStatement(connection);

		logger.debug("Version deleted");
		logger.debug("OUT");
	}

	private String getVersionColumnName() {
		return instance.getWriteBackManager().getRetriver().getVersionColumnName();
	}

	private String getVersionTableName() {
		return instance.getWriteBackManager().getRetriver().getVersionTableName();
	}

	private List<SbiVersion> fromResultSetToSbiVersions(ResultSet resulSet) throws SQLException {
		List<SbiVersion> versions = new ArrayList<SbiVersion>();
		while (resulSet.next()) {
			Integer versionId = resulSet.getInt("versionIdColumn");
			String versionName = resulSet.getString("version_name");
			String versionDescr = resulSet.getString("version_descr");
			versions.add(new SbiVersion(versionId, versionName, versionDescr));
		}
		return versions;
	}

}
