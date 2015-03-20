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

Sbi.tools.catalogue.MondrianSchemasCatalogue = function(config) {
	
	var defaultSettings = {
		configurationObject : {
			panelTitle : LN('sbi.tools.catalogue.mondrianSchemasCatalogue')
			, listTitle : LN('sbi.tools.catalogue.mondrianSchemasCatalogue')
		}
	};
	  
	if (Sbi.settings && Sbi.settings.tools && Sbi.settings.tools.catalogue && Sbi.settings.tools.catalogue.mondrianschemascatalogue) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.tools.catalogue.mondrianschemascatalogue);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	  
	Ext.apply(this, c);
	
	var baseParams = {
		LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		, type : 'MONDRIAN_SCHEMA'
	};
	
	// start services for main catalog list
	c.mainListServices = {
		'manageListService' : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_ARTIFACTS_ACTION'
				, baseParams: baseParams
		})
		, 'saveItemService' : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_ARTIFACT_ACTION'
				, baseParams: baseParams
		})
		, 'deleteItemService' : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'DELETE_ARTIFACT_ACTION'
				, baseParams: baseParams
		})
	};
	// end services for main catalog list 
	
	// start services for item versions list
	c.singleItemServices = {
		'getVersionsService' : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_ARTIFACT_VERSIONS_ACTION'
			, baseParams: baseParams
		})
		, 'deleteVersionsService' : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'DELETE_ARTIFACT_VERSIONS_ACTION'
			, baseParams: baseParams
		})
		, 'downloadVersionService' : Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'DOWNLOAD_ARTIFACT_VERSION_ACTION'
			, baseParams: baseParams
		})
	};
	// end services for item versions list


	Sbi.tools.catalogue.MondrianSchemasCatalogue.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.tools.catalogue.MondrianSchemasCatalogue, Sbi.widgets.Catalogue, {
	
	
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
		
		this.unlockButton = new Ext.Button({
			text: 'Unlock Model'
			, handler: function(btn) {
				
				this.services["unlockModel"]= Sbi.config.serviceRegistry.getRestServiceUrl({
						serviceName: '1.0/locker/'+this.selectedRecord.id+'/unlock',
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
										"artifactId" : this.selectedRecord.id
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
		
		var detPanelItems = this.detailPanel.items.items[0];
		var it = detPanelItems.items.items;
		it.push(this.lockedField);
		it.push(this.lockerField);
		it.push(this.unlockButton);


		this.configurationObject.tabItems = [ this.detailPanel ];

	}
	
	,initInputFields : function(config) {
		var fields = this.superclass().initInputFields.call(this,config);
		fields.push('locked');
		fields.push('locker');

		return fields;
	}
	
	,rowSelectedListener : function(){
		//alert("row selected!");
		if (this.selectedRecord.data.locked == false){
			this.unlockButton.disable();
		} else {
			this.unlockButton.enable();
		}
	}
	
	
});
