/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *  @author
 *  Rossato Luca (luca.rossato@eng.it)
 */
 
  
Ext.define('Sbi.tools.multitenant.MultitenantListDetailPanel', {
	extend: 'Sbi.widgets.compositepannel.ListDetailPanel'

	,config: {
		stripeRows: true,
		modelName: "Sbi.tools.multitenant.MultitenantModel"
	}

	, initServices: function(baseParams){
		this.services["saveTenant"]= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'multitenant/save'
				, baseParams: baseParams
		});
	}

	, constructor: function(config) {
		this.services = [];
		this.initServices();
		this.detailPanel =  Ext.create('Sbi.tools.multitenant.MultitenantDetailPanel',{});
		this.columns = [{dataIndex:"MULTITENANT_ID", header:LN('sbi.multitenant.id')}, {dataIndex:"MULTITENANT_NAME", header:LN('sbi.generic.name')}];
		this.fields = ["MULTITENANT_ID","MULTITENANT_NAME", "MULTITENANT_THEME"];
		this.detailPanel.on("save",this.onFormSave,this);
		this.filteredProperties = ["MULTITENANT_NAME"];
		this.buttonToolbarConfig = {
				newButton: true
		};
		
		this.buttonColumnsConfig ={
			deletebutton:true
		};
	
		this.callParent(arguments);
	}
	
	, onDeleteRow: function(record){
		
		var thisPanel = this;

		var deleteRecord = function(buttonId, text, config){
			
			var record = config.record;
			
			if(buttonId == 'yes') {			
				
				var recordToDelete = Ext.create("Sbi.tools.multitenant.MultitenantModel",record.data);
				
				if (!this.loadMask) {    		
		    		this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "  Wait...  "});
		    	}		   
		    	this.loadMask.show();
		    					
				recordToDelete.destroy({
					success : function(object, response, options) {
					
						if (this.loadMask && this.loadMask != null) {	
				    		this.loadMask.hide();
				    	}
						if(response !== undefined && response.response !== undefined && response.response.responseText !== undefined && response.response.statusText=="OK") {
							response = response.response ;
							if(response.responseText!=null && response.responseText!=undefined){
								if(response.responseText.indexOf("errors")>=0){
									Sbi.exception.ExceptionHandler.handleFailure(response);
								}else{
									Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.multitenant.deleted'));
									thisPanel.grid.store.remove(record);
									thisPanel.grid.store.commitChanges();
								}
							}
						} else {
							Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
						}
					},
					failure: function(object, response, options){
						if (this.loadMask && this.loadMask != null) {	
				    		this.loadMask.hide();
				    	}
						Sbi.exception.ExceptionHandler.handleFailure
					},
					scope: this
				});
			} 
		};
		
		Ext.Msg.show({
			   title: LN('sbi.multitenant.delete.title'),   
			   msg: LN('sbi.multitenant.delete.msg'),
			   buttons: Ext.Msg.YESNO,
			   icon: Ext.MessageBox.QUESTION,
			   modal: true,
			   fn: deleteRecord, 
			   record: record
			});

	}
    , handleFailure : function(response, options) {
    	
    	var errorEngines = "engine";
    	var errorDS = "datasource";
    	var errorProductType = "producttype";

    	
    	var errMessage = ''
    	if(response !== undefined) {
    		if (response.responseText !== undefined) {
    			try{
    				var content = Ext.util.JSON.decode( response.responseText );
    			}catch(e){
    				var content =Ext.JSON.decode( response.responseText );
    			}
    			
    			if (content.errors !== undefined  && content.errors.length > 0) {
    				if (content.errors[0].message === 'session-expired') {
    					// session expired
    		        	Sbi.exception.ExceptionHandler.redirectToLoginUrl();
    		        	return;
    				} else if (content.errors[0].message === 'not-enabled-to-call-service') {
    					Sbi.exception.ExceptionHandler.showErrorMessage(LN('not-enabled-to-call-service'), 'Service Error')
    				}  else if (content.message === 'validation-error') {
    					for (var count = 0; count < content.errors.length; count++) {
    						var anError = content.errors[count];
    						if (anError.message !== undefined && anError.message !== '') {
		        				errMessage += anError.message;
		        			}
		        			if (count < content.errors.length - 1) {
		        				errMessage += '<br/>';
		        			}
    					}
    				}else {
    					for (var count = 0; count < content.errors.length; count++) {
    						var anError = content.errors[count];
    						if (anError.message !== undefined && anError.message !== '' && (anError.message.indexOf(errorEngines)>=0 || anError.message.indexOf(errorDS)>=0 || anError.message.indexOf(errorProductType)>=0 )) {
		        				//errMessage += LN(anError.message);
    							errMessage += LN('multitenant.error.association')+anError.message;
		        			} else if (anError.localizedMessage !== undefined && anError.localizedMessage !== '') {
		        				errMessage += anError.localizedMessage;
		        			} else if (anError.message !== undefined && anError.message !== '') {
		        				errMessage += anError.message;
		        			}
		        			if (count < content.errors.length - 1) {
		        				errMessage += '<br/>';
		        			}
    					}
    				}
    			}
    		} else {
    			errMessage = LN('sbi.generic.genericError');
    		}
    	}
    	
    	Sbi.exception.ExceptionHandler.showErrorMessage(errMessage, LN('sbi.generic.serviceError'));
   	
    }
	, onFormSave: function(record){
		
		Ext.Ajax.request({
	        url: this.services['saveTenant'],
	        params: Ext.encode(record),
	        method: 'POST',
	        success: function(response, options) {

			if(response !== undefined && response.responseText != undefined && response.responseText != null && response.statusText=="OK") {
				if(response.responseText.indexOf("errors")>=0){
					this.handleFailure(response);
				}else{

					var respoceJSON = Ext.decode(response.responseText);
					if(respoceJSON.MULTITENANT_ID){
						record.MULTITENANT_ID = respoceJSON.MULTITENANT_ID;
					}
					if(respoceJSON.SAVE_TYPE && respoceJSON.SAVE_TYPE == 'INSERT'){
						Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.multitenant.saved') + ' ' + respoceJSON.NEW_USER);
					}else{
						Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.generic.resultMsg'));
					}
					this.detailPanel.setActiveTab(0);
					
					var selectedRow = this.grid.getSelectionModel().getSelection();
				
					selectedRow[0].data = Ext.apply(selectedRow[0].data, record);	
					selectedRow[0].raw = Ext.apply(selectedRow[0].raw, record);	
					selectedRow[0].data.MULTITENANT_ID = record.MULTITENANT_ID;
					selectedRow[0].commit();

					this.grid.store.sync();
					this.grid.store.commitChanges() ;
					this.grid.store.loadData( selectedRow[0], true ) ;
					
					this.grid.getView().refresh();
					
					this.detailPanel.tenantId.setValue(record.MULTITENANT_ID);
					
					//this.grid.getSelectionModel().select(selectedRow[0]);
					//this.grid.getView().select(this.grid.store.data.length -1);
					//this.grid.store.on('refresh',this.grid.getView().select(this.grid.getSelectionModel().select(selectedRow[0])));
					//this.grid.store.on('refresh', this.grid.getView().select(this.grid.store.data.length -1));
				}
			} else {
				Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			}
		},
		failure: Sbi.exception.ExceptionHandler.handleFailure	      		
		,scope: this
		});
	}

	, onGridSelect: function(selectionrowmodel, record, index, eOpts){
		this.detailPanel.setValues(record.data);
		this.detailPanel.enginesStore.getProxy().extraParams  = {'TENANT': record.data.MULTITENANT_NAME, DOMAIN_TYPE:"DIALECT_HIB"};
		this.detailPanel.enginesStore.load();
		this.detailPanel.dsStore.getProxy().extraParams  = {'TENANT': record.data.MULTITENANT_NAME, DOMAIN_TYPE:"DIALECT_HIB"};
		this.detailPanel.dsStore.load();
		this.detailPanel.productTypesStore.getProxy().extraParams  = {'TENANT': record.data.MULTITENANT_NAME, DOMAIN_TYPE:"DIALECT_HIB"};
		this.detailPanel.productTypesStore.load();
		this.detailPanel.show();
	}

});
