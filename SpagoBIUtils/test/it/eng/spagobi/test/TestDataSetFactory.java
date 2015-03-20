/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.test;

import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestDataSetFactory {
	public static SpagoBiDataSource createSpagoBiDataSource() {
		SpagoBiDataSource dataSourceConfig = new SpagoBiDataSource();
		dataSourceConfig.setDriver(TestCaseConstants.CONNECTION_DRIVER);
		dataSourceConfig.setHibDialectClass(TestCaseConstants.CONNECTION_DIALECT);
		dataSourceConfig.setHibDialectName(TestCaseConstants.CONNECTION_DIALECT);
		dataSourceConfig.setMultiSchema(false);
		dataSourceConfig.setUser(TestCaseConstants.CONNECTION_USER);
		dataSourceConfig.setPassword(TestCaseConstants.CONNECTION_PWD);
		dataSourceConfig.setUrl(TestCaseConstants.CONNECTION_URL);
		return dataSourceConfig;
	}
}
