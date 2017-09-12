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
 
  

/**
 * Object name 
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 *  [list]
 * 
 * 
 * Public Events
 * 
 *  [list]
 * 
 * Authors
 * 
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.behavioural.lov.TestLovResultPanel', {
    extend: 'Sbi.widgets.grid.DynamicGridPanel'

	, constructor: function(config) {
		
		var defautlConf = { pagingConfig:{}, storeConfig:{ pageSize: 10}	};
		this.title =  "LOV result preview";
		this.filterConfig={};
		this.border = false;
		this.region = 'south';
		defautlConf = Ext.apply( defautlConf,config ||{} );
		Ext.apply(this,defautlConf);
		
		Sbi.debug('TestLovPanel costructor IN');
		
		defautlConf.serviceUrl=   Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'LIST_TEST_LOV_ACTION'
			,baseParams: {LIGHT_NAVIGATOR_DISABLED: 'TRUE'} 
    	});
    	
    	this.callParent([defautlConf]);
    	this.store.on('load',function(store, records, success){
			if(!success){				
				 Sbi.exception.ExceptionHandler.showErrorMessage(LN("sbi.behavioural.lov.errorLoading"));
			}
			else{
				this.fireEvent('storeLoad')
			}
			},
    		this);

    	
    	Sbi.debug('TestLovPanel costructor OUT');
    }
    
	
});


