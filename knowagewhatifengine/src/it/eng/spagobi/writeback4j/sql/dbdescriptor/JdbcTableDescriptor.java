/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 *  @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
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
