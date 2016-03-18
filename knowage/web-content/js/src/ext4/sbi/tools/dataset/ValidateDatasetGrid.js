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


Ext.define('Sbi.tools.dataset.ValidateDatasetGrid', {
    extend: 'Sbi.widgets.grid.DynamicGridPanel'


  , constructor: function (config) {
		thisPanel = this;
		Ext.QuickTips.init();

		var defaultConf = { pagingConfig:{}, 
							storeConfig:{ pageSize: 10, 
										  dataRoot : 'rows' ,
										  storeType: "InMemoryFilteredStore"
//										  parentGrid: this,
//								    	  filteredProperties: {}
							}
		};

		
		this.border = false;// true;
		this.bodyStyle = 'padding:10px;';
        this.height = 300; 
        this.width = '100%';
        this.autoscroll =  true;
        this.loadMask = true;
         
        this.firedValidationErrorFound = false;

        defaultConf.params = config;
        defaultConf.usePost = true;
		Ext.apply(this,defaultConf);
		
		Sbi.debug('ValidateDatasetGrid costructor IN');
		
		defaultConf.serviceUrl =  Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'selfservicedataset/getDataStore'
			,baseParams: {}
    	});

		
		Ext.util.Format.myRenderer = function(value, metaData, record, rowIndex, colIndex){
			var validationErrors = thisPanel.store.getValidationErrors();
			if ((validationErrors != null) && (validationErrors != undefined)){
				if (!thisPanel.firedValidationErrorFound){
					this.fireEvent('validationErrorFound');
					thisPanel.firedValidationErrorFound = true;
				}
				
				//redefining rowIndex for correct pagination management
				if (thisPanel.store.page > 1){
					rowIndex += (thisPanel.store.page-1)*thisPanel.store.pageSize;
				}
				for (var i=0; i<validationErrors.length; i++) {					
					if (validationErrors[i].id == rowIndex){
						var val = validationErrors[i];
						for(j in val){
							var sub_key = j;
							var sub_val = val[j];
							var cIndex = sub_key.replace('column_','');
							if (cIndex == colIndex){
								metaData.tdCls = 'custom-error';
								metaData.tdAttr = 'data-qtip=\''+sub_val+'\'';

//								if (!thisPanel.firedValidationErrorFound){
//									this.fireEvent('validationErrorFound');
//									thisPanel.firedValidationErrorFound = true;
//								}
							}
						} 
					}
				}
			}
			return value;			
		}
		
    	this.callParent([defaultConf]);
		this.addEvents('validationErrorFound');	

    	this.store.on('load',function(){this.fireEvent('storeLoad')},this);
    	Sbi.debug('ValidateDatasetGrid costructor OUT');
    }



});
