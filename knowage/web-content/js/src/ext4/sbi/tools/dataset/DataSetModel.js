/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 Ext.define('Sbi.tools.dataset.DataSetModel', {
    extend: 'Ext.data.Model',
    proxy:{
    	type : 'rest',
    	url : Sbi.config.serviceRegistry.getRestServiceUrl({
    		serviceName : 'selfservicedataset'
    	}),
    	reader : {
    		type : 'json',
    		root : 'root'
    	}
    },
    fields:
    	[
    	 	"id",
            "version_num",
            "active",
            "label",
            "name",
            "description",
            "catTypeVn",
            "catTypeCd",
            "catTypeId",
            "dsTypeCd",
            "configuration",
            "pars",
            "actions",
            "fileName",
            "fileType",
            "csvDelimiter",
            "csvQuote",
            "csvEncoding",
            "skipRows",
            "limitRows",
            "xslSheetNumber",
            "isPublic",
            "owner",
            "userIn",
            "dateIn",
            "dataSource",
            "meta",
        	"qbeDataSource",
        	"qbeDatamarts",
        	"qbeJSONQuery",
        	"ckanId",
        	"ckanUrl"
        ]
}); 