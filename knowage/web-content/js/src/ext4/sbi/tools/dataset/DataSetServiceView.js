

/**
 * MANDATORY CONFIG: datasetLabel
 */

Ext.define('Sbi.tools.dataset.DataSetServiceView', {
	extend: 'Sbi.widgets.grid.DynamicGridPanel'

		, config:{
			pageSize: 10,
			remoteSort: true
			
		}

		, constructor: function(config) {
			this.initConfig(config);
			var defaultConf = { 
					pagingConfig:{},
					storeConfig:{ 
						pageSize: this.pageSize, 
						dataRoot : 'rows',
						remoteSort: true
					},
					filterConfig:{
							filterAutoType: true,
							columnComboWidth: 200
					}
				
			};
			this.layout= "fit";
			this.region= "center";
			this.loadMask = true;
			
			Ext.apply(this,defaultConf);
			
		
			Sbi.debug('ValidateDatasetGrid costructor IN');
		
			var serviceName = 'selfservicedataset/values/'+ config.datasetLabel;
			
			defaultConf.serviceUrl =  Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: serviceName
					,baseParams: config.params
			});
		

			
			this.callParent([defaultConf]);
			this.addEvents('validationErrorFound');	
			this.store.on("beforeload",function(store,options){
				if(options && options.sorters){
					for(var i=0; i<options.sorters.length;i++){
						/* we should change the sort properties. This because the SpagoBI dataset serializer set as id of the column a fake name 
						 * (something like column_1) and so the options.sorters[i].property contains this fake property name. 
						 * To order the dataset we need the correct column name.
						 * 
						 * When we change options.sorters[i].property we also mark the object as managedBySpagoBI. If the store is reloaded we don't touch 
						 * the options.sorters[i].property. 
						 * Suppose we sort by column with header A. After the execution of this method options.sorters[i].property contains A. Now suppose we
						 * take the next page. The datastore reload data but options.sorters is not changed so options.sorters[i].property contains A. If we 
						 * execute the code var sorterColumnHeader = store.fields.map[options.sorters[i].property].header; we have an error because the keys of 
						 * store.fields.map are of type column_1 but options.sorters[i].property now contains an header
						 * */
						if(!options.sorters[i].managedBySpagoBI){
							var sorterColumnHeader = store.fields.map[options.sorters[i].property].header;
							options.sorters[i].property = sorterColumnHeader;
							options.sorters[i].managedBySpagoBI = true;
						}
					}
				}

				
			},this)
		
			this.store.on('load',function(){this.fireEvent('storeLoad')},this);
			this.store.on('load',function(store, records, success){
				if(!success){
					 Sbi.exception.ExceptionHandler.showErrorMessage("Error loading data");
				}
			},this);
			this.store.on('reconfigure',function( panel, store, columns, eOpts ){

				},this);
			Sbi.debug('ValidateDatasetGrid costructor OUT');
		}


});