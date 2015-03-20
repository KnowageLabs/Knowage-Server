
/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.database.temporarytable;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

public class TemporaryTable {

	private String tableName;
	private IDataSource dataSource;
	
	public TemporaryTable( String tableName, IDataSource dataSource ) {
		this.tableName = tableName;
		this.dataSource = dataSource;
	}
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public IDataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
}
