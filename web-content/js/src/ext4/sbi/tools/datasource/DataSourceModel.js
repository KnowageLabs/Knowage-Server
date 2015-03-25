/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.define('Sbi.tools.datasource.DataSourceModel', {
	extend: 'Ext.data.Model',

	proxy:{
		type: 'rest',
		url : Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'datasources'}),
		reader: {
			type: 'json',
			root: 'root'
		}
	},

	fields: [
	         "DATASOURCE_ID",
	         "DATASOURCE_LABEL",
	         "DESCRIPTION",
	         "DIALECT_NAME", 
	         "DIALECT_ID", 
	         "MULTISCHEMA",
	         "SCHEMA", 
	         "READ_ONLY",
	         "WRITE_DEFAULT",	
	         "TYPE",
	         "JNDI_URL",
	         "USER", 
	         "CONNECTION_URL", 
	         "PASSWORD",
	         "DRIVER",
	         "USERIN"
	         ]
});