/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
   
Ext.define('Sbi.tools.datasource.DataSourceListDetailPanel', {
	extend: 'Sbi.widgets.compositepannel.ListDetailPanel'

	,config: {
		stripeRows: true,
		modelName: "Sbi.tools.datasource.DataSourceModel"
	}

	, constructor: function(config) {
		
		var isSuperadmin= config.isSuperadmin;
		
		this.services =[];
		this.initServices();
		this.detailPanel =  Ext.create('Sbi.tools.datasource.DataSourceDetailPanel',{services: this.services, isSuperadmin: isSuperadmin });
		this.columns = [{dataIndex:"DATASOURCE_LABEL", header:LN('sbi.generic.label')}, {dataIndex:"DESCRIPTION", header:LN('sbi.generic.descr')}];
		this.fields = ["DATASOURCE_ID","DATASOURCE_LABEL","DESCRIPTION","DRIVER","DIALECT_ID","DIALECT_CLASS","DIALECT_NAME","JNDI_URL","USER","PASSWORD","SCHEMA","MULTISCHEMA","READ_ONLY","WRITE_DEFAULT","CONNECTION_URL"];
		this.detailPanel.on("save",this.checkCanSave,this);
		this.detailPanel.on("test",this.onFormTest,this);
		this.filteredProperties = ["DATASOURCE_LABEL","DESCRIPTION"];
		this.buttonToolbarConfig = {
				newButton: true
		};
		this.buttonColumnsConfig ={
				deletebutton:true
		};
	

		this.callParent(arguments);
	}
	
	, initServices: function(baseParams){
		
		this.services["test"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'datasources/test'
				, baseParams: baseParams
		});
		this.services["getDialects"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'domains/listValueDescriptionByType'
				, baseParams: baseParams
		});
	}
	
	, onDeleteRow: function(record){
		
		if (Sbi.user.isSuperAdmin != 'true' && record.data.USERIN != Sbi.user.userId) {
			Ext.Msg.alert('Delete', LN('sbi.datasource.delete.forbidden'));
			return;
		}
		
		var recordToDelete = Ext.create("Sbi.tools.datasource.DataSourceModel",record.data);
		recordToDelete.destroy({
			success : function(object, response, options) {
				if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
					response = response.response ;
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.datasource.deleted'));
							this.grid.store.remove(record);
							this.grid.store.commitChanges();
						}
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			},
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}
	
	
	, checkCanSave: function(record){
					// if write default is true and there is already a write default ask for modification
		var currentPanel = this;
		var already = this.isThereWriteDefault();
		if(
				(record.WRITE_DEFAULT == true || record.WRITE_DEFAULT == 'true' || record.WRITE_DEFAULT == 'on')
				&&
				already != undefined
		)
		{		
					 Ext.Msg.confirm(LN("sbi.datasource.validation.error"),already+LN("sbi.datasource.validation.writeDefault"),
							 			function(btn, text){
						 					if (btn == 'yes'){ 
						 						currentPanel.onFormSave(record);
						 							}
						 					else{
						 						record.WRITE_DEFAULT=false;
						 						currentPanel.onFormSave(record);						 						
						 					}
						 				});
				
		}
		else{ 
			// case current not to set write default or there is no already one
			this.onFormSave(record);
		}		
	}	
	
	, onFormSave: function(record){
		 var recordToSave = Ext.create("Sbi.tools.datasource.DataSourceModel",record);
		 recordToSave.save({
			success : function(object, response, options) {
	
				if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
					response = response.response ;
					if(response.responseText!=null && response.responseText!=undefined){
						if(response.responseText.indexOf("error.mesage.description")>=0){
							Sbi.exception.ExceptionHandler.handleFailure(response);
						}else{
							var respoceJSON = Ext.decode(response.responseText);
							if(respoceJSON.DATASOURCE_ID){
								record.DATASOURCE_ID = respoceJSON.DATASOURCE_ID;
							}
							Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.datasource.saved'));
							var selectedRow = this.grid.getSelectionModel().getSelection();
							
							
							selectedRow[0].data = Ext.apply(selectedRow[0].data, record);	
							selectedRow[0].raw = Ext.apply(selectedRow[0].raw, record);	
							selectedRow[0].data.DATASOURCE_ID = record.DATASOURCE_ID;

							// if it has been set write deafult must change the previous write default
							if(object.data.WRITE_DEFAULT == true){
								var currentLabel = object.data.DATASOURCE_LABEL;
						        var store = this.grid.store;
						        for (var i = 0; i < this.grid.store.count(); i++) {
						        	var element = this.grid.store.getAt(i);
						        	// set all to write default = false except the current one
						        	if(element.data.DATASOURCE_LABEL != object.data.DATASOURCE_LABEL){
						        		element.data.WRITE_DEFAULT = false;
						        	}
						        }
							}
							selectedRow[0].commit();

							this.grid.store.sync();
							this.grid.store.commitChanges() ;
							this.grid.store.loadData( selectedRow[0], true ) ;
							
							this.grid.getView().refresh();
							
							this.detailPanel.dataSourceId.setValue(record.DATASOURCE_ID);
							

							
						}
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			},
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure  
		});
		
		
	}

	, onFormTest: function(record){
		var r = record;
		try{			
			if ((this.grid.selModel.selected !== undefined) && (record.DATASOURCE_LABEL === undefined || record.DATASOURCE_LABEL == null)){
				var r = this.grid.selModel.selected.items[0].raw;
			}
		}catch(error){			
		}

		Ext.Ajax.request({
			url: this.services["test"],
			params: r,
			success : function(response, options) {
				if(response !== undefined && response.statusText !== undefined) {
					var responceText = Ext.decode(response.responseText);
					if(responceText.error){
						Sbi.exception.ExceptionHandler.showErrorMessage(responceText.error, 'Service Error');
					}else if(responceText.error==""){
						Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.datasource.test.failed'), 'Service Error');
					}else{
						Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.datasource.tested'));
					}
				} else {
					Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
				}
			},
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}
	
	, onGridSelect: function(selectionrowmodel, record, index, eOpts){
		this.detailPanel.show();
		this.detailPanel.setFormState(record.data);
	}
	
	, isThereWriteDefault: function(){
        var store = this.grid.store;
        for (var i = 0; i < this.grid.store.count(); i++) {
        			var element = this.grid.store.getAt(i);
                    var isWriteDefault = element.data.WRITE_DEFAULT;
                    if(isWriteDefault == true || isWriteDefault == 'true' || isWriteDefault == 'on') return element.data.DATASOURCE_LABEL;
        }
        return;
	}
});
