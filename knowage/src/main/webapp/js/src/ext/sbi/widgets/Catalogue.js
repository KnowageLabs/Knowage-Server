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
Ext.ns("Sbi.widgets");

Sbi.widgets.Catalogue = function(config) {

	// apply defaults values
	config = Ext.apply({
	// no defaults
	}, config || {});

	this.configurationObject = Ext.apply({}, config.configurationObject || {});

	this.configurationObject.manageListService = config.mainListServices.manageListService;
	this.configurationObject.saveItemService = config.mainListServices.saveItemService;
	this.configurationObject.deleteItemService = config.mainListServices.deleteItemService;
	this.configurationObject.getCategoriesService = config.mainListServices.getCategoriesService;

	// Check if we have to use Categories
	if (config.isCategorizationEnabled != null) {
		this.configurationObject.isCategorizationEnabled = true;
	} else {
		this.configurationObject.isCategorizationEnabled = false;
	}

	this.init(config);

	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	config.fileUpload = true; // this is a multipart form!!

	var c = Ext.apply({}, config);
	
	this.addEvents('rowSelected');

	Sbi.widgets.Catalogue.superclass.constructor.call(this, c);
	if (config.isCategorizationEnabled != null) {
		this.addListener('afterrender', function() {
			var me = this;
			this.getForm().on('beforeaction', function() {
				var fieldset = me.detailPanel.getComponent('fieldset');
				var categoryVisible = fieldset.getComponent('categoryVisible');
				var categoryHidden =  fieldset.getComponent('categoryHidden');
				var value = categoryVisible.getValue();
				categoryHidden.setValue(value);
			});
		});
	}


	this.rowselModel.addListener('rowselect', function(sm, row, rec) {
		this.selectedRecord = rec;
		this.fireEvent('rowSelected');
		this.getForm().loadRecord(rec);
		if (config.isCategorizationEnabled != null) {
			var fieldset = this.detailPanel.getComponent('fieldset');
			var categoryVisible = fieldset.getComponent('categoryVisible');
			if (rec.data){
				categoryVisible.setValue(rec.data.category)
			}
		}

		this.versionsGridPanel.getStore().load({
			params : {
				id : rec.get('id')
			}
		});
		this.uploadField.reset();
	}, this);

};

Ext
		.extend(
				Sbi.widgets.Catalogue,
				Sbi.widgets.ListDetailForm,
				{

					configurationObject : null,
					detailPanel : null,
					uploadField : null

					,
					init : function(config) {

						this.configurationObject.fields = [];

						var fields = this.initInputFields(config);
						for (f in fields) {
							this.configurationObject.fields.push(fields[f]);
						}
							
						var r ={
								id : 0,
								name : '',
								description : ''
							};
							
						if (config.isCategorizationEnabled != null) {
							
							 r = {
									id : 0,
									name : '',
									description : '',
									category: '',
									categoryVisible: ''
								};
								

						}
						
						if(config && config.additionalFormObjects){
							r = Ext.apply(r, config.additionalFormObjects.itemFieldsDefault);
						}
						
						this.configurationObject.emptyRecToAdd = new Ext.data.Record(r);
			
						this.configurationObject.filter = true;
						if (config.isCategorizationEnabled != null) {
							this.configurationObject.columnName = [
							                                       	['name', LN('sbi.generic.name')],
							                                       	['category', LN('sbi.ds.catType')]
							                	                   ];							
						} else {
							this.configurationObject.columnName = [
							                                       	['name', LN('sbi.generic.name')]
							                	                   ];
						}
						
						if(this.additionalFormObjects){
							for(var i=0; i<this.additionalFormObjects.itemFields.length; i++){
								this.configurationObject.columnName.push([this.additionalFormObjects.itemFields[i], itemFields.additionalFormObjects[i]]);
							}
							
						}


						this.configurationObject.gridColItems = [ {
							header : LN('sbi.generic.name'),
							width : 140,
							sortable : true,
							locked : true,
							dataIndex : 'name'
						}, {
							header : LN('sbi.generic.descr'),
							width : 180,
							sortable : true,
							dataIndex : 'description'
						} ];

						this.initDetailPanel(config);

						this.configurationObject.tabItems = [ this.detailPanel ];
					}

					,
					initInputFields : function(config) {
						var fields =  [ 'id', 'name', 'description' ]
						if (this.configurationObject.isCategorizationEnabled == true) {
							fields = [ 'id', 'name', 'description', 'category' ];
						} 
						if(config && config.additionalFormObjects){
							var positionToInject = 0;
							if(config.additionalFormObjects.position && config.additionalFormObjects.position<fields.length){
								positionToInject = config.additionalFormObjects.position;
							}
							for(var j=0; j<config.additionalFormObjects.items.length; j++){
								fields.splice(positionToInject+j, 0, config.additionalFormObjects.items[j]);
							}
							
						}
						return fields;
					}

					,
					initDetailPanel : function(config) {

						// START list of detail fields
						var idField = {
							name : 'id',
							hidden : true,
							value : 0
						};

						var nameField = {
							maxLength : 100,
							minLength : 1,
							fieldLabel : LN('sbi.generic.name'),
							allowBlank : false,
							validationEvent : true,
							name : 'name'
						};
						
						var descrField = {
							maxLength : 500,
							fieldLabel : LN('sbi.generic.descr'),
							allowBlank : true,
							name : 'description'
						};
						

					
						this.uploadField = new Ext.form.TextField({
							inputType : 'file',
							fieldLabel : LN('sbi.generic.upload'),
							allowBlank : true
						});

						this.uiItems = [];

						if (this.configurationObject.isCategorizationEnabled == true) {
							this.categoryStore = new Ext.data.JsonStore(
									{
										url : this.configurationObject.getCategoriesService,
//										autoLoad : true,
										autoLoad : false,
										root : 'domains',
										fields : [ 'VALUE_ID', 'VALUE_NM' ],
										restful : true
									});

							this.categoryStore.load();

							this.categoryCombo = new Ext.form.ComboBox({
								name : 'categoryCombo',
								itemId : 'categoryVisible',
								store : this.categoryStore,
								width : 150,
								fieldLabel : LN('Category'),
								valueField : 'VALUE_ID',
								displayField : 'VALUE_NM',								
								typeAhead : true,
								forceSelection : true,
								mode : 'local',
								triggerAction : 'all',
								selectOnFocus : true,
								editable : false,
								allowBlank : true,
								validationEvent : true,
								submitValue : false
							});
							
							

							this.categoryHidden = {
								xtype : 'hidden',
								name : 'category',
								itemId : 'categoryHidden'
							};

							this.uiItems = [  idField, nameField, descrField,
									this.categoryCombo, this.categoryHidden,
									this.uploadField ];

						} else {
							this.uiItems = [ idField, nameField, descrField,
									this.uploadField ];
						}

						if(config && config.additionalFormObjects){
							var positionToInject = 0;
							if(config.additionalFormObjects.position && config.additionalFormObjects.position<this.uiItems.length){
								positionToInject = config.additionalFormObjects.position;
							}
							for(var j=0; j<config.additionalFormObjects.items.length; j++){
								this.uiItems.splice(positionToInject+j, 0, config.additionalFormObjects.items[j]);
							}
							
						}
						
						this.versionsGridPanel = new Sbi.widgets.CatalogueVersionsGridPanel(
								{
									services : {
										'getVersionsService' : config.singleItemServices.getVersionsService,
										'deleteVersionsService' : config.singleItemServices.deleteVersionsService,
										'downloadVersionService' : config.singleItemServices.downloadVersionService
									},
									height : 200
								});

						// END list of detail fields

						this.detailPanel = new Ext.Panel(
								{
									title : LN('sbi.generic.details'),
									layout : 'fit',
									items : [
											{
												columnWidth : 0.4,
												xtype : 'fieldset',
												itemId: 'fieldset', 
												labelWidth : 110,
												defaults : {
													width : 220,
													border : false
												},
												defaultType : 'textfield',
												autoHeight : true,
												autoScroll : true,
												bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
														: 'padding:10px 15px;',
												border : false,
												style : {
													"margin-left" : "20px",
													"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-20px"
															: "-23px")
															: "20"
												}

												,
												items : this.uiItems
											},
											{
												style : {
													"margin-left" : "20px",
													"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-20px"
															: "-23px")
															: "20"
												}
												// , height : 400
												,
												items : [ this.versionsGridPanel ]
											}

									]
								});

					}

					,
					addNewItem : function() {
						Sbi.widgets.Catalogue.superclass.addNewItem.call(this);
						this.versionsGridPanel.getStore().removeAll();
						this.uploadField.reset();
					}

					,
					save : function() {
						this.validate(this.doSave, this.showValidationErrors,
								this);
					}

					,
					doSave : function() {

						// a multipart form cannot contain parameters on its
						// main URL; they must POST parameters
						// therefore we have to split save service url into base
						// url and parameters
						var completeUrl = this.services['saveItemService'];
						var baseUrl = completeUrl.substr(0, completeUrl
								.indexOf("?"));
						var queryStr = completeUrl.substr(completeUrl
								.indexOf("?") + 1);
						var params = Ext.urlDecode(queryStr);

						var activeVersionRecord = this.versionsGridPanel
								.getCurrentActiveRecord();
						if (activeVersionRecord != null) {
							params['active_content_id'] = activeVersionRecord
									.get('id');
						}

						var form = this.getForm();

						form
								.submit({
									url : baseUrl // a multipart form cannot
													// contain parameters on its
													// main URL; they must POST
													// parameters
									,
									params : params,
									waitMsg : LN('sbi.generic.wait'),
									success : this.doSaveHandler,
									failure : function(form, action) {
										Sbi.exception.ExceptionHandler
												.showErrorMessage(
														action.result.msg,
														LN('sbi.generic.serviceError'));
									},
									scope : this
								});
					}

					,
					doSaveHandler : function(form, action) {
						var success = (action.result && action.result.success) ? (action.result.success == true)
								: false;
						if (success) {
							Ext.Msg.show({
								title : LN('sbi.generic.ok'),
								msg : LN('sbi.generic.result'),
								buttons : Ext.Msg.OK,
								icon : Ext.MessageBox.INFO
							});
							this.uploadField.reset();
							this.commitChangesInList(action.result.msg);
						} else {
							var message = (action.result && action.result.msg) ? action.result.msg
									: LN('sbi.generic.error.msg');
							Sbi.exception.ExceptionHandler.showErrorMessage(
									message, LN('sbi.generic.error'));
						}
					}

					,
					getFormState : function() {
						var state = this.getForm().getFieldValues();
						if (!state['id']) {
							state['id'] = 0;
						}
						return state;
					}

					,
					doValidate : function() {
						// returns an array of errors; if no error returns an
						// empty array
						var toReturn = new Array();
						var values = this.getForm().getFieldValues();
						var name = values['name'];
						if (name.trim() == '') {
							toReturn
									.push(LN('sbi.generic.validation.missingName'));
						}
						return toReturn;
					}

					,
					commitChangesInList : function(serverResponseText) {
						var responseContent = Ext.util.JSON
								.decode(serverResponseText);
						if (this.isNewItem()) {
							var values = this.getFormState();
							var id = responseContent.id;
							values['id'] = id;
							var newRecord = new Ext.data.Record(values);
							this.mainElementsStore.add(newRecord);
							this.rowselModel.selectLastRow(true);
						} else {
							var values = this.getFormState();
							var record = this.getRecordById(values['id']);
							record.set('name', values['name']);
							record.set('description', values['description']);
							if (this.configurationObject.isCategorizationEnabled == true) {
								record.set('category', values['category']);
							}
							
	
							if(this.additionalFormObjects){
								for(var i=0; i<this.additionalFormObjects.itemFields.length; i++){
									record.set(this.additionalFormObjects.itemFields[i], values[this.additionalFormObjects.itemFields[i]]);
								}
								
							}
							
							this.mainElementsStore.commitChanges();
							this.versionsGridPanel.getStore().load({
								params : {
									id : values['id']
								}
							});
						}
					}

					,
					getRecordById : function(id) {
						var length = this.mainElementsStore.getCount();
						for ( var i = 0; i < length; i++) {
							var aRecord = this.mainElementsStore.getAt(i);
							if (aRecord.data.id == id) {
								return aRecord;
							}
						}
						return null;
					}

					,
					isNewItem : function() {
						var values = this.getFormState();
						return values['id'] == 0;
					}

					,
					validate : function(successHandler, failureHandler, scope) {
						var errorArray = this.doValidate();
						if (errorArray.length > 0) {
							return failureHandler.call(scope || this,
									errorArray);
						} else {
							return successHandler.call(scope || this);
						}
					}

					,
					showValidationErrors : function(errorsArray) {
						var errMessage = '';

						for ( var i = 0; i < errorsArray.length; i++) {
							var error = errorsArray[i];
							errMessage += error + '<br>';
						}

						Sbi.exception.ExceptionHandler.showErrorMessage(
								errMessage, LN('sbi.generic.error'));
					}

				});
