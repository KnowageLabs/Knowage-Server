/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  

Ext.define('Sbi.tools.model.MetaModelModel', {
    extend: 'Ext.data.Model',
    fields: ['id', 'name', 'description','data_source_label'],
    proxy: {
        type: 'ajax',
        url : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_META_MODELS_FOR_FINAL_USER_ACTION'
		}),
        reader: {
            type: 'json',
            root: 'rows'
        }
    }
});