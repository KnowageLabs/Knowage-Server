/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.tools.catalogue");

Sbi.tools.catalogue.MetaModelsCatalogue = function(config) {

	var defaultSettings = {
			configurationObject : {
				panelTitle : LN('sbi.tools.catalogue.metaModelsCatalogue')
				, listTitle : LN('sbi.tools.catalogue.metaModelsCatalogue')
			}
	};

	if (Sbi.settings && Sbi.settings.tools && Sbi.settings.tools.catalogue && Sbi.settings.tools.catalogue.metamodelscatalogue) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.tools.catalogue.metamodelscatalogue);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	var baseParams = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};

	// start services for main catalog list
	c.mainListServices = {
			'manageListService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'GET_META_MODELS_ACTION'
					, baseParams: baseParams
			})
			, 'saveItemService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'SAVE_META_MODEL_ACTION'
					, baseParams: baseParams
			})
			, 'deleteItemService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'DELETE_META_MODEL_ACTION'
					, baseParams: baseParams
			})
			, 'getCategoriesService' : Sbi.config.serviceRegistry.getRestServiceUrl({
				serviceName: 'domains/listValueDescriptionByType'
					, baseParams: {
						LIGHT_NAVIGATOR_DISABLED: 'TRUE',
						DOMAIN_TYPE:"BM_CATEGORY",
						EXT_VERSION: "3"
					}
			})	
	};
	// end services for main catalog list 

	// start services for item versions list
	c.singleItemServices = {
			'getVersionsService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'GET_META_MODEL_VERSIONS_ACTION'
					, baseParams: baseParams
			})
			, 'deleteVersionsService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'DELETE_META_MODEL_VERSIONS_ACTION'
					, baseParams: baseParams
			})
			, 'downloadVersionService' : Sbi.config.serviceRegistry.getServiceUrl({
				serviceName: 'DOWNLOAD_META_MODEL_VERSION_ACTION'
					, baseParams: baseParams
			})

	};
	// end services for item versions list

	// Set property for using a catalogue with categories
	c.isCategorizationEnabled = true;


	var dataSourceStore = new Ext.data.JsonStore(
			{
				url :  Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'datasourcespublic'}),
				autoLoad : true,
				fields : [ 'label'],
				restful : true
			});

	var dataSourcecombo = new Ext.form.ComboBox({
			fieldLabel : LN('sbi.tools.catalogue.metaModelsCatalogue.datasource'),
			store : dataSourceStore,
			width : 150,
			name: 'data_source_label',
			displayField : 'label',
			valueField : 'label',
			triggerAction : 'all'
		});

	c.additionalFormObjects={
			position: 4,
			items: [dataSourcecombo],
			itemFields:["data_source_label"],
			itemFieldsDefault:{
				data_source_label: ""
			}
	}

	Sbi.tools.catalogue.MetaModelsCatalogue.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.tools.catalogue.MetaModelsCatalogue, Sbi.widgets.Catalogue, {
	
	init : function(config){
		this.superclass().init.call(this,config);
			
		var r ={
				id : 0,
				name : '',
				description : '',
				locker: '',
				locked: ''
			};
			
		if (config.isCategorizationEnabled != null) {
			
			 r = {
					id : 0,
					name : '',
					description : '',
					category: '',
					categoryVisible: '',
					locker: '',
					locked: ''
				};
				

		}
		
		if(config && config.additionalFormObjects){
			r = Ext.apply(r, config.additionalFormObjects.itemFieldsDefault);
		}
		
		this.configurationObject.emptyRecToAdd = new Ext.data.Record(r);

	}
	
	,initDetailPanel : function(config) {
		this.superclass().initDetailPanel.call(this,config);

		this.lockedField = new Ext.form.Checkbox({
			readOnly: true,
			disabled: true,
			fieldLabel : 'Locked?',
			name : 'locked',
			dataIndex : 'locked'
		});
		
		this.lockerField = new Ext.form.DisplayField({
			maxLength : 500,
			fieldLabel : 'Locked by',
			name : 'locker',
			dataIndex : 'locker'
		});
		
		//Unlock Model Button
		
		this.unlockButton = new Ext.Button({
			text: 'Unlock Model'
			, handler: function(btn) {
				
				var recordId;
				if (this.selectedRecord.phantom){
					recordId = this.selectedRecord.data.id;
				} else {
					recordId = this.selectedRecord.id
				}
				
				this.services["unlockModel"]= Sbi.config.serviceRegistry.getRestServiceUrl({
						serviceName: '1.0/modellocker/'+recordId+'/unlock',
						baseParams: {}
				}); 
				
		        
				Ext.MessageBox.confirm(
						LN('sbi.generic.pleaseConfirm'),
						'Do you want to unlock the model?',
						function(btn, text){
							if (btn=='yes') {
								Ext.Ajax.request({
									url: this.services["unlockModel"],
									method: 'POST', 
									params: {
										"metaModelId" : this.selectedRecord.id
									},
									success : function(response, options) {
										if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
											if(response.responseText!=null && response.responseText!=undefined){
												if(response.responseText.indexOf("error.mesage.description")>=0){
													Sbi.exception.ExceptionHandler.handleFailure(response);
												}else{						
													Sbi.exception.ExceptionHandler.showInfoMessage(LN('Model unlocked'));
													//Cancel the lock on the record data
													this.selectedRecord.data.locked = false;
													this.selectedRecord.data.locker = "";
													//Reset the gui for the lock part
													this.lockedField.setValue(false);
													this.lockerField.setValue("");
													//disable the unlock button
													this.unlockButton.disable();
													//enable the lock button
													this.lockButton.enable();
												}
											}
										} else {
											Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
										}
									},
									scope: this,
									failure: Sbi.exception.ExceptionHandler.handleFailure      
								})
							}
						},
						this
					);

		    }
			, width: 30
			, scope: this
		})
		
		//Lock model button
		
		this.lockButton = new Ext.Button({
			text: 'Lock Model'
			, style: {
	            marginBottom: '10px'
	        }
			, handler: function(btn) {
				
				var recordId;
				if (this.selectedRecord.phantom){
					recordId = this.selectedRecord.data.id;
				} else {
					recordId = this.selectedRecord.id
				}
				
				this.services["lockModel"]= Sbi.config.serviceRegistry.getRestServiceUrl({
						serviceName: '1.0/modellocker/'+recordId+'/lock',
						baseParams: {}
				}); 
				
		        
				Ext.MessageBox.confirm(
						LN('sbi.generic.pleaseConfirm'),
						'Do you want to lock the model?',
						function(btn, text){
							if (btn=='yes') {
								Ext.Ajax.request({
									url: this.services["lockModel"],
									method: 'POST', 
									params: {
										"metaModelId" : this.selectedRecord.id
									},
									success : function(response, options) {
										if(response !== undefined  && response.responseText !== undefined && response.statusText=="OK") {
											if(response.responseText!=null && response.responseText!=undefined){
												if(response.responseText.indexOf("error.mesage.description")>=0){
													Sbi.exception.ExceptionHandler.handleFailure(response);
												}else{						
													Sbi.exception.ExceptionHandler.showInfoMessage(LN('Model locked'));
													var responseObject = JSON.parse(response.responseText);
													
													//Set the lock on the record data
													this.selectedRecord.data.locked = true;
													this.selectedRecord.data.locker = responseObject.locker;
													//Set the gui for the lock part
													this.lockedField.setValue(true);
													this.lockerField.setValue(responseObject.locker);
													//disable the lock button
													this.lockButton.disable();
													//enable the unlock button
													this.unlockButton.enable();
												}
											}
										} else {
											Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
										}
									},
									scope: this,
									failure: Sbi.exception.ExceptionHandler.handleFailure      
								})
							}
						},
						this
					);

		    }
			, width: 30
			, scope: this
		})
		this.lockButton.disable();
		
		var detPanelItems = this.detailPanel.items.items[0];
		var it = detPanelItems.items.items;
		it.push(this.lockedField);
		it.push(this.lockerField);
		it.push(this.lockButton);
		it.push(this.unlockButton);
		
		


		this.configurationObject.tabItems = [ this.detailPanel ];

	}
	
	,initInputFields : function(config) {
		var fields = this.superclass().initInputFields.call(this,config);
		fields.push('locked');
		fields.push('locker');

		return fields;
	}
	
	, addNewItem : function() {
		this.superclass().addNewItem.call(this);
		this.unlockButton.disable();
		this.lockButton.disable();

	}	
	
	,rowSelectedListener : function(){
		//alert("row selected!");
		if (this.selectedRecord.data.locked == false){
			this.unlockButton.disable();
			this.lockButton.enable();
		} else {
			this.unlockButton.enable();
			this.lockButton.disable();
		}
	}	

});
