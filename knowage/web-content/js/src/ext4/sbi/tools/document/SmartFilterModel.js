/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  

Ext.define('Sbi.tools.document.SmartFilterModel', {
    extend: 'Ext.data.Model',
    proxy: {
        type: 'rest',
        url : Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: '1.0/documents'
		}),
		extraParams: {
            inputType: 'SMART_FILTER'
        },
        reader: {
            type: 'json',
            root: 'root'
        }
    },
    fields: ['creationDate',
             'creationUser',
    		'dataSetLabel',
    		'dataSourceLabel',
    		'description',
    		'docVersion',
    		'engine',
    		'functionalities',
    		'id',
    		'label',
    		'name',
    		'objMetaDataAndContents',
    		'parametersRegion',
    		'previewFile',
    		'stateCode',
    		'tenant',
    		'typeCode',
    		'visible']
});