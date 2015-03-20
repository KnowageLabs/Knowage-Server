/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

import org.apache.log4j.Logger;

public class JDBCDatasetFactory {
	private static transient Logger logger = Logger
			.getLogger(JDBCDatasetFactory.class);

	public static IDataSet getJDBCDataSet(IDataSource dataSource) {
		IDataSet dataSet = null;

		if (dataSource == null) {

			throw new IllegalArgumentException(
					"datasource parameter cannot be null");
		}
		String dialect = dataSource.getHibDialectClass();
		if (dialect.contains("hbase")) {
			dataSet = new JDBCHBaseDataSet();
		} else if (dialect.contains("hive")) {
			dataSet = new JDBCHiveDataSet();
		}else if (dialect.contains("orient")) {
			dataSet = new JDBCOrientDbDataSet();
		} else {
			dataSet = new JDBCDataSet();
		}

		return dataSet;
	}
}
