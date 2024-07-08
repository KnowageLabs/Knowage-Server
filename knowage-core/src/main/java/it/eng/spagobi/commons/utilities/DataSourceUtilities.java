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
package it.eng.spagobi.commons.utilities;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.mappers.SQLMapper;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.services.datasource.service.DataSourceSupplier;

public class DataSourceUtilities {

	private static Logger logger = Logger.getLogger(DataSourceUtilities.class);

	/**
	 * use this method in service implementation. If RequestContainer isn't correct.
	 *
	 * @param profile
	 * @param dsLabel
	 * @return
	 */
	public Connection getConnection(IEngUserProfile profile, String dsLabel) {
		Connection connection = null;
		// calls implementation for gets data source object

		DataSourceSupplier supplierDS = new DataSourceSupplier();
		SpagoBiDataSource ds = supplierDS.getDataSourceByLabel(dsLabel);
		logger.debug("Schema Attribute:" + ds.getSchemaAttribute());
		String schema = null;
		if (profile != null) {
			schema = UserUtilities.getSchema(ds.getSchemaAttribute(), profile);
			logger.debug("Schema:" + schema);
		}
		try {
			connection = ds.readConnection(schema);
		} catch (NamingException e) {
			logger.error("JNDI error", e);
		} catch (SQLException e) {
			logger.error("Cannot retrive connection", e);
		} catch (ClassNotFoundException e) {
			logger.error("Driver not found", e);
		}

		return connection;
	}

	/**
	 * Creates a ago DataConnection object starting from a sql connection.
	 *
	 * @param con Connection to the export database
	 *
	 * @return The Spago DataConnection Object
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	public DataConnection getDataConnection(Connection con) throws EMFInternalError {
		DataConnection dataCon = null;
		try {
			Class mapperClass = Class.forName("it.eng.spago.dbaccess.sql.mappers.OracleSQLMapper");
			SQLMapper sqlMapper = (SQLMapper) mapperClass.newInstance();
			dataCon = new DataConnection(con, "2.1", sqlMapper);
		} catch (Exception e) {
			logger.error("Error while getting Data Source " + e);
			throw new EMFInternalError(EMFErrorSeverity.ERROR, "cannot build spago DataConnection object");
		}
		return dataCon;
	}

}
