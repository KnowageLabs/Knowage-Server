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
package it.eng.spagobi.writeback4j.sql.dbdescriptor;

import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.Set;

import org.apache.log4j.Logger;

public class JdbcTableDescriptor implements IDbSchemaDescriptor {

	public static transient Logger logger = Logger.getLogger(JdbcTableDescriptor.class);

	public Set<String> getColumnNames(String table, IDataSource dataSource) {
		logger.debug("IN");
		logger.debug("Loading the name of the columns of the table " + table);
		IDataSetTableDescriptor tabledescriptor = null;
		try {
			tabledescriptor = TemporaryTableManager.getTableDescriptor(null, table, dataSource);
		} catch (Exception e) {
			logger.error("Error loading the names of the columns from the datasource ", e);
			throw new SpagoBIEngineRuntimeException("Error loading the names of the columns from the datasource ", e);
		}
		logger.debug("OUT");
		return tabledescriptor.getColumnNames();
	}

}
