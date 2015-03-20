/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * Object name
 * 
 * [description]
 * 
 * Authors - Marco Cortella (marco.cortella@eng.it)
 */
Ext.ns("Sbi.engines");

Sbi.engines.EngineManagementPanel = function(config) {

	var defaultSettings = {
		singleSelection : true
	};

	if (Sbi.settings && Sbi.settings.engines
			&& Sbi.settings.engines.engineManagementPanel) {
		defaultSettings = Ext.apply(defaultSettings,
				Sbi.settings.engines.engineManagementPanel);
	}
	config.tabPanelWidth ='50%'; // 520;
	config.gridWidth = '50%'; //470;
	
	var c = Ext.apply(defaultSettings, config || {});
	c.configurationObject = this.initConfigObject();

	// Ext.apply(this, c);

	Sbi.engines.EngineManagementPanel.superclass.constructor.call(this, c);

	this.rowselModel.addListener('rowselect', function(sm, row, rec) {
		var record = this.rowselModel.getSelected();
		this.setValues(record);
		this.activateEngineDetailFields(null, rec, row);
		this.activateDataSourceCombo(null,rec);

	}, this);

};

Ext
.extend(
		Sbi.engines.EngineManagementPanel,
		Sbi.widgets.ListDetailForm,
				{
					// ---------------------------------------------------------------------------
					// object's members
					// ---------------------------------------------------------------------------
					configurationObject : null

					// ---------------------------------------------------------------------------
					// public methods
					// ---------------------------------------------------------------------------

					// ---------------------------------------------------------------------------
					// private methods
					// ---------------------------------------------------------------------------

					,
					initConfigObject : function() {

						this.configurationObject = this.configurationObject
								|| {};

						this.initMasterGridConf();

						this.initDataStoreServicesConf();
						this.initCrudServicesConf();
						this.initButtonsConf();
						this.initTabsConf();

						return this.configurationObject;
					}
					
					// DataStore Services initalization

					 , initDataStoreServicesConf: function() {
						 this.configurationObject = this.configurationObject ||
						 {};

						 this.configurationObject.getEngineTypesServiceUrl =
							 Sbi.config.serviceRegistry.getServiceUrl({
								 serviceName : 'DOMAIN_ACTION',
								 baseParams : {
									 MESSAGE_DET : "DOMAINS_FILTER", 
									 DOMAIN_TYPE: "ENGINE_TYPE"
								 }
							 });
						 
						 this.configurationObject.getDocumentTypesServiceUrl =
							 Sbi.config.serviceRegistry.getServiceUrl({
								 serviceName : 'DOMAIN_ACTION',
								 baseParams : {
									 MESSAGE_DET : "DOMAINS_FILTER", 
									 DOMAIN_TYPE: "BIOBJ_TYPE"
								 }
							 });
						
						 this.configurationObject.getDataSourcesServiceUrl =
							 Sbi.config.serviceRegistry.getServiceUrl({
								 serviceName : 'READ_ENGINE_ACTION',
								 baseParams : {
									 MESSAGE_DET : "ENGINE_DATASOURCES"
								 }
							 });

					 }

					,
					initCrudServicesConf : function() {
						this.configurationObject = this.configurationObject
								|| {};

						this.configurationObject.manageListService = Sbi.config.serviceRegistry
								.getServiceUrl({
									serviceName : 'READ_ENGINE_ACTION',
									baseParams : {
										MESSAGE_DET : "ENGINE_LIST"
									}
								});

						this.configurationObject.saveItemService = Sbi.config.serviceRegistry
								.getServiceUrl({
									serviceName : 'MANAGE_ENGINE_ACTION',
									baseParams : {
										LIGHT_NAVIGATOR_DISABLED : 'TRUE',
										MESSAGE_DET : "ENGINE_INSERT"
									}
								});

						this.configurationObject.deleteItemService = Sbi.config.serviceRegistry
								.getServiceUrl({
									serviceName : 'MANAGE_ENGINE_ACTION',
									baseParams : {
										LIGHT_NAVIGATOR_DISABLED : 'TRUE',
										MESSAGE_DET : "ENGINE_DELETE"
									}
								});
						
						this.configurationObject.testEngineService = Sbi.config.serviceRegistry
						.getServiceUrl({
							serviceName : 'MANAGE_ENGINE_ACTION',
							baseParams : {
								LIGHT_NAVIGATOR_DISABLED : 'TRUE',
								MESSAGE_DET : "ENGINE_TEST"
							}
						});
					}

					,
					initMasterGridConf : function() {

						this.configurationObject = this.configurationObject
								|| {};

						this.configurationObject.fields = [ "id", "label",
								"name", "description", "documentType",
								"engineType", "useDataSet", "useDataSource",
								"engine_class", "url", "driver", "secondaryUrl",
								"dataSourceId" ];

						this.configurationObject.emptyRecToAdd = new Ext.data.Record(
								{
									id : null,
									name : '',
									label : '',
									description : '',
									documentType : '',
									engineType : '',
									useDataSet : '',
									useDataSource : '',
									engine_class : '',
									url : '',
									driver : '',
									secondaryUrl : '',
									dataSourceId : ''
								});

						this.configurationObject.gridColItems = [ {
							id : 'label',
							header : LN('sbi.generic.label'),
							width : 140,
							sortable : true,
							locked : false,
							dataIndex : 'label'
						}, {
							header : LN('sbi.generic.name'),
							width : 150,
							sortable : true,
							dataIndex : 'name'
						}, {
							header : LN('description'),
							width : 140,
							sortable : true,
							dataIndex : 'description'
						}

						];

						this.configurationObject.panelTitle = LN('sbi.ds.panelTitle');
						this.configurationObject.listTitle = 'Engine List';

						this.configurationObject.filter = true;
						this.configurationObject.columnName = [
								[ 'sbiDsConfig.label', LN('sbi.generic.label') ],
								[ 'sbiDsConfig.name', LN('sbi.generic.name') ] ];
						this.configurationObject.setCloneButton = true;
					}
					
					//Engine Combo listener
					,activateEngineDetailFields : function(combo, record, index) {
						//var engineSelected = record.get('engineType');
						var engineSelected = this.detailFieldEngineType.getRawValue();
						if (engineSelected != null
								&& engineSelected == 'EXT') {
							//Show External Engine properties
							
							this.detailFieldClass.setVisible(false);
							this.detailFieldClass.getEl().up('.x-form-item').setDisplayed(false);
							this.detailFieldUrl.setVisible(true);
							this.detailFieldUrl.getEl().up('.x-form-item').setDisplayed(true);
							this.detailFieldSecondaryUrl.setVisible(true);
							this.detailFieldSecondaryUrl.getEl().up('.x-form-item').setDisplayed(true);
							this.detailFieldDriverName.setVisible(true);
							this.detailFieldDriverName.getEl().up('.x-form-item').setDisplayed(true);
							
						} else {
							//Show Internal Engine properties
							this.detailFieldClass.setVisible(true);
							this.detailFieldClass.getEl().up('.x-form-item').setDisplayed(true);
							this.detailFieldUrl.setVisible(false);
							this.detailFieldUrl.getEl().up('.x-form-item').setDisplayed(false);
							this.detailFieldSecondaryUrl.setVisible(false);
							this.detailFieldSecondaryUrl.getEl().up('.x-form-item').setDisplayed(false);
							this.detailFieldDriverName.setVisible(false);
							this.detailFieldDriverName.getEl().up('.x-form-item').setDisplayed(false);

						}
					}
					
					//UseDatasource listener
					,activateDataSourceCombo : function(checkbox, checked) {
						var useDatasource;
						if (checkbox != null){
							useDatasource = checkbox.getValue();
						}
						else {
							useDatasource = this.detailFieldUseDataSource.getValue();
						}
					}
					

					,
					initButtonsConf : function() {
						this.configurationObject = this.configurationObject
								|| {};

						var tbButtonsArray = new Array();


//						this.tbTransfInfoButton = new Ext.Toolbar.Button({
//							text : LN('sbi.ds.help'),
//							iconCls : 'icon-info',
//							handler : this.transfInfo,
//							width : 30,
//							scope : this
//						});
//						tbButtonsArray.push(this.tbTransfInfoButton);
						this.configurationObject.tbButtonsArray = tbButtonsArray;
					}

					,
					initTabsConf : function() {
						this.configurationObject = this.configurationObject
								|| {};

						this.initDetailTab();
						this.configurationObject.tabItems = [ this.detailTab ];
					}

					,
					initDetailTab : function() {
				

						// Store of the combobox
						this.documentTypesStore = new Ext.data.JsonStore({
						    url: this.configurationObject.getDocumentTypesServiceUrl,
						    autoLoad : true,
						    root: 'domains',
						    fields: ['VALUE_CD','VALUE_ID']
						});

						this.engineTypesStore = new Ext.data.JsonStore({
						    url: this.configurationObject.getEngineTypesServiceUrl,
						    autoLoad : true,
						    root: 'domains',
						    fields: ['VALUE_CD','VALUE_ID']
						});
						
						this.dataSourcesStore = new Ext.data.JsonStore({
						    url: this.configurationObject.getDataSourcesServiceUrl,
						    autoLoad : true,
						    root: 'rows',
						    fields: ['DATASOURCE_LABEL','DATASOURCE_ID']
						});



						// START list of detail fields
						this.detailFieldName = new Ext.form.TextField ({
							maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.generic.name'),
							allowBlank : false,
							validationEvent : true,
							name : 'name'
						});

						this.detailFieldLabel = new Ext.form.TextField ({
							maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString2'),
							fieldLabel : LN('sbi.generic.label'),
							allowBlank : false,
							validationEvent : true,
							name : 'label'
						});

						this.detailFieldDescr = new Ext.form.TextArea ({
							//xtype : 'textarea',
							width : 350,
							height : 80,
							maxLength : 160,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : LN('sbi.generic.descr'),
							validationEvent : true,
							name : 'description'
						});

						this.detailFieldDocumentType = new Ext.form.ComboBox ({
							name : 'documentType',
							store : this.documentTypesStore,
							width : 150,
							fieldLabel : 'Document Type',
							displayField : 'VALUE_CD',
							valueField : 'VALUE_ID',
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : true,
							validationEvent : true,
							xtype : 'combo'
						});

						this.detailFieldEngineType = new Ext.form.ComboBox ({
							name : 'engineType',
							store : this.engineTypesStore,
							width : 150,
							fieldLabel : 'Engine Types',
							displayField : 'VALUE_CD',
							valueField : 'VALUE_ID',
							typeAhead : true,
							forceSelection : true,
							mode : 'local',
							triggerAction : 'all',
							selectOnFocus : true,
							editable : false,
							allowBlank : true,
							validationEvent : true
							//xtype : 'combo'
						});
						this.detailFieldEngineType.addListener('select',this.activateEngineDetailFields, this);


						this.detailFieldUseDataSet = new Ext.form.Checkbox ({
							
							fieldLabel : 'Use Data Set',
							name : 'useDataSet'

						});

						this.detailFieldUseDataSource = new Ext.form.Checkbox({
							fieldLabel : 'Use Data Source',
							name : 'useDataSource',
							listeners : {
						          check : {
						            fn : this.activateDataSourceCombo,
						            scope : this
						          }
						        }

						});
						//this.detailFieldUseDataSource.addListener('check',this.activateDataSourceCombo, this);
						

						
						this.detailFieldClass = new Ext.form.TextField ({
							//maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : 'Class',
							allowBlank : true,
							validationEvent : false,
							name : 'engine_class'
						});


						this.detailFieldUrl = new Ext.form.TextField ({
							//maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : 'Url',
							allowBlank : true,
							validationEvent : false,
							name : 'url'
						});

						this.detailFieldSecondaryUrl = new Ext.form.TextField ({
							//maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : 'Secondary Url',
							allowBlank : true,
							validationEvent : false,
							name : 'secondaryUrl'
						});

						this.detailFieldDriverName = new Ext.form.TextField ({
							//maxLength : 50,
							minLength : 1,
							width : 350,
							regexText : LN('sbi.roles.alfanumericString'),
							fieldLabel : 'Driver Name',
							allowBlank : true,
							validationEvent : false,
							name : 'driver'
						});
						
						
						this.testButton = new Ext.Button({
							text : 'Test',
							listeners : {
						          click : {
						            fn : this.testEngine,
						            scope : this
						          }
						        }
						});
						
						
						// END list of detail fields

						var c = {};

						this.detailTab = new Ext.Panel(
								{
									title : LN('sbi.generic.details'),
									itemId : 'detail',
									width : 430,
									items : {
										id : 'items-detail',
										itemId : 'items-detail',
										columnWidth : 0.4,
										xtype : 'fieldset',
										labelWidth : 90,
										defaultType : 'textfield',
										autoHeight : true,
										autoScroll : true,
										bodyStyle : Ext.isIE ? 'padding:0 0 8px 10px;'
												: 'padding:8px 10px;',
										border : false,

										style : {
											"margin-left" : "8px",
											"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
													: "-8px")
													: "8"
										},
										items : [ this.detailFieldLabel,
												this.detailFieldName,
												this.detailFieldDescr,
												this.detailFieldDocumentType,
												this.detailFieldEngineType,
												this.detailFieldUseDataSet,
												this.detailFieldUseDataSource,
												this.detailFieldClass,
												this.detailFieldUrl,
												this.detailFieldSecondaryUrl,
												this.detailFieldDriverName,
												this.testButton
										// this.manageDsVersionsPanel ,
										// this.detailFieldUserIn,this.detailFieldDateIn,this.detailFieldVersNum,this.detailFieldVersId,this.detailFieldId
										]
									}
								});
					}

					// OVERRIDING METHOD
					,
					addNewItem : function() {
						this.newRecord = new Ext.data.Record({
							name : '',
							label : '',
							description : '',
							documentType : '',
							engineType : '',
							useDataSet : 'false',
							useDataSource : 'false',
							engine_class : '',
							url : '',
							driver : '',
							secondaryUrl : '',
							dataSourceId : ''
						});
						this.setValues(this.newRecord);

						this.tabs.items.each(function(item) {
							item.doLayout();
						});

						if (this.newRecord != null
								&& this.newRecord != undefined) {
							this.mainElementsStore.add(this.newRecord);
							this.rowselModel.selectLastRow(true);
						}
						this.tabs.setActiveTab(0);
					}

					,
					cloneItem : function() {
						var values = this.getValues();
						var params = this.buildParamsToSendToServer(values);
						// var arrayPars = this.manageParsGrid.getParsArray();

						this.newRecord = this.buildNewRecordToSave(values);
						// this.newRecord.set('pars',arrayPars);
						this.setValues(this.newRecord);


						this.tabs.items.each(function(item) {
							item.doLayout();
						});
						if (this.newRecord != null
								&& this.newRecord != undefined) {
							this.mainElementsStore.add(this.newRecord);
							this.rowselModel.selectLastRow(true);
						}
						this.tabs.setActiveTab(0);
					}

					,
					buildNewRecordToSave : function(values) {
						var newRec = new Ext.data.Record({
							id : null,
							name : values['name'],
							label : values['label'],
							description : values['description'],
							documentType : values['documentType'],
							engineType : values['engineType'],
							useDataSet : values['useDataSet'],
							useDataSource : values['useDataSource'],
							engine_class : values['engine_class'],
							url : values['url'],
							driver : values['driver'],
							secondaryUrl : values['secondaryUrl'],
							dataSourceId : values['dataSourceId']
						});
						return newRec;
					}

					,
					buildParamsToSendToServer : function(values) {
						var params = {
							id : values['id'],
							name : values['name'],
							label : values['label'],
							description : values['description'],
							documentType : values['documentType'],
							engineType : values['engineType'],
							useDataSet : values['useDataSet'],
							useDataSource : values['useDataSource'],
							engine_class : values['engine_class'],
							url : values['url'],
							driver : values['driver'],
							secondaryUrl : values['secondaryUrl'],
							dataSourceId : values['dataSource']
						};
						return params;
					}

					,
					updateNewRecord : function(record, values) {
						record.set('id', values['id']);
						record.set('label', values['label']);
						record.set('name', values['name']);
						record.set('description', values['description']);
						record.set('documentType', values['documentType']);
						record.set('engineType', values['engineType']);
						record.set('useDataSet', values['useDataSet']);
						record.set('useDataSource', values['useDataSource']);
						record.set('engine_class', values['engine_class']);
						record.set('url', values['url']);
						record.set('driver', values['driver']);
						record.set('secondaryUrl', values['secondaryUrl']);
						record.set('dataSourceId', values['dataSource']);



					}

					,
					updateMainStore : function(idRec) {
						var values = this.getValues();
						var record;
						var length = this.mainElementsStore.getCount();
						for ( var i = 0; i < length; i++) {
							var tempRecord = this.mainElementsStore.getAt(i);
							if (tempRecord.data.id == idRec) {
								record = tempRecord;
							}
						}
						var params = this.buildParamsToSendToServer(values);
						// var arrayPars = this.manageParsGrid.getParsArray();
						this.updateNewRecord(record, values);
						this.mainElementsStore.commitChanges();
					}



					,
					getValues : function() {
						//var values = this.getForm().getFieldValues();
						
						//Manual setting of values
						var values = {};
						//values.id = "";
						values.id = this.detailFieldId;
						values.name = this.detailFieldName.getValue();
						values.label = this.detailFieldLabel.getValue();
						values.description = this.detailFieldDescr.getValue();
						values.documentType = this.detailFieldDocumentType.getValue();
						values.engineType = this.detailFieldEngineType.getValue();
						values.useDataSet = this.detailFieldUseDataSet.getValue();
						values.useDataSource = this.detailFieldUseDataSource.getValue();
						values.engine_class = this.detailFieldClass.getValue();						
						values.url = this.detailFieldUrl.getValue();
						values.secondaryUrl = this.detailFieldSecondaryUrl.getValue();
						values.driver = this.detailFieldDriverName.getValue();
							
						return values;

					}

					,
					setValues : function(rec) {
						//this.getForm().loadRecord(record);

						//Manual setting of values
						if (rec.get('id')){
							this.detailFieldId = rec.get('id');
						} else {
							this.detailFieldId = '';
						}
						this.detailFieldName.setValue( rec.get('name'));
						this.detailFieldLabel.setValue( rec.get('label'));
						this.detailFieldDescr.setValue( rec.get('description'));
						this.detailFieldDocumentType.setValue(rec.get('documentType'));
						this.detailFieldEngineType.setValue(rec.get('engineType'));
						this.detailFieldUseDataSet.setValue(rec.get('useDataSet'));
						this.detailFieldUseDataSource.setValue(rec.get('useDataSource'));
						this.detailFieldUrl.setValue(rec.get('url'));
						this.detailFieldClass.setValue(rec.get('engine_class'));						
						this.detailFieldSecondaryUrl.setValue(rec.get('secondaryUrl'));
						this.detailFieldDriverName.setValue(rec.get('driver'));
					}

					// OVERRIDING save method
					,
					save : function() {
						var values = this.getValues();
						var idRec = values['id'];
						if (idRec == 0 || idRec == null || idRec === '') {
							this.doSave("yes");
						} else {
//							Ext.MessageBox
//									.confirm(
//											LN('sbi.ds.recalculatemetadataconfirm.title'),
//											LN('sbi.ds.recalculatemetadataconfirm.msg'),
//											this.doSave, this);
							this.doSave("yes");
						}
					}

					,
					doSave : function(recalculateMetadata) {
						var values = this.getValues();

						var idRec = values['id'];
						var newRec;
						//var newDsVersion;
						var isNewRec = false;
						var params = this.buildParamsToSendToServer(values);
						params.recalculateMetadata = recalculateMetadata;
						// var arrayPars = this.manageParsGrid.getParsArray();
						//var customString = this.customDataGrid.getDataString();

						if (idRec == 0 || idRec == null || idRec === '') {
							this.updateNewRecord(this.newRecord, values);
							isNewRec = true;
						} else {
							var record;
							var oldType;
							var length = this.mainElementsStore.getCount();
							for ( var i = 0; i < length; i++) {
								var tempRecord = this.mainElementsStore
										.getAt(i);
								if (tempRecord.data.id == idRec) {
									record = tempRecord;
									//oldType = record.get('dsTypeCd');
								}
							}
							this.updateNewRecord(record, values);


						}



						if (idRec) {
							params.id = idRec;
						}
						//Serialize form values to JSON Object
						params.engineValues = Ext.util.JSON.encode(values);

						Ext.Ajax
								.request({
									url : this.services['saveItemService'],
									params : params,
									method : 'POST',
									success : function(response, options) {
										if (response !== undefined) {
											if (response.responseText !== undefined) {

												var content = Ext.util.JSON
														.decode(response.responseText);
												if (content.responseText !== 'Operation succeded') {
													Ext.MessageBox
															.show({
																title : LN('sbi.generic.error'),
																msg : content,
																width : 150,
																buttons : Ext.MessageBox.OK
															});
												}
												else {
													var itemId = content.id;
													var dateIn = content.dateIn;
													var userIn = content.userIn;
													var versId = content.versId;
													var versNum = content.versNum;

													if (isNewRec
															&& itemId != null
															&& itemId !== '') {

														var record;
														var length = this.mainElementsStore
																.getCount();
														for ( var i = 0; i < length; i++) {
															var tempRecord = this.mainElementsStore
																	.getAt(i);
															if (tempRecord.data.id == 0) {
																tempRecord.set(
																		'id',
																		itemId);
																tempRecord
																		.set(
																				'dateIn',
																				dateIn);
																tempRecord
																		.set(
																				'userIn',
																				userIn);
																tempRecord
																		.set(
																				'versId',
																				versId);
																tempRecord
																		.set(
																				'versNum',
																				versNum);
																tempRecord
																		.commit();
																this.detailFieldId
																		.setValue(itemId);
																this.detailFieldUserIn
																		.setValue(userIn);
																this.detailFieldDateIn
																		.setValue(dateIn);
																this.detailFieldVersNum
																		.setValue(versNum);
																this.detailFieldVersId
																		.setValue(versId);
															}
														}
													} else {
//														if (newDsVersion != null
//																&& newDsVersion != undefined) {
//															this.manageDsVersionsGrid
//																	.getStore()
//																	.addSorted(
//																			newDsVersion);
//															this.manageDsVersionsGrid
//																	.getStore()
//																	.commitChanges();
//															var values = this
//																	.getValues();
//															this
//																	.updateDsVersionsOfMainStore(values['id']);
//														}
													}
													this.mainElementsStore
															.commitChanges();
													if (isNewRec
															&& itemId != null
															&& itemId !== '') {
														this.rowselModel
																.selectLastRow(true);
													}

													Ext.MessageBox
															.show({
																title : LN('sbi.generic.result'),
																msg : LN('sbi.generic.resultMsg'),
																width : 200,
																buttons : Ext.MessageBox.OK
															});
												}

											} else {
												Sbi.exception.ExceptionHandler
														.showErrorMessage(
																LN('sbi.generic.serviceResponseEmpty'),
																LN('sbi.generic.serviceError'));
											}
										} else {
											Sbi.exception.ExceptionHandler
													.showErrorMessage(
															LN('sbi.generic.savingItemError'),
															LN('sbi.generic.serviceError'));
										}
									},
									failure : Sbi.exception.ExceptionHandler.handleFailure,
									scope : this
								});
					}
					
					,
					testEngine : function(){
						var values = this.getValues();

						var params = this.buildParamsToSendToServer(values);

						//Serialize form values to JSON Object
						params.engineValues = Ext.util.JSON.encode(values);

						Ext.Ajax.request({
							url : this.configurationObject.testEngineService,
							params : params,
							method : 'POST',
							success : function(response, options) {
								if (response !== undefined) {
									if (response.responseText !== undefined) {

										var content = Ext.util.JSON.decode(response.responseText);
										if (content.responseText !== 'Operation succeded') {
											Ext.MessageBox.show({
												title : LN('sbi.generic.error'),
												msg : content.responseText,
												width : 150,
												buttons : Ext.MessageBox.OK
											});
										}
										else {

											Ext.MessageBox.show({
												title : LN('Test'),
												msg : LN('Test successful'),
												width : 200,
												buttons : Ext.MessageBox.OK
											});
										}

									} else {
										Sbi.exception.ExceptionHandler.showErrorMessage(
												LN('sbi.generic.serviceResponseEmpty'),
												LN('sbi.generic.serviceError'));
									}
								} else {
									Sbi.exception.ExceptionHandler.showErrorMessage(
											LN('sbi.generic.savingItemError'),
											LN('sbi.generic.serviceError'));
								}
							},
							failure : Sbi.exception.ExceptionHandler.handleFailure,
							scope : this
						});
					}



					// METHOD TO BE OVERRIDDEN IN EXTENDED ELEMENT!!!!!
					,
					info : function() {
						var win_info_2;
						if (!win_info_2) {
							win_info_2 = new Ext.Window({
								id : 'win_info_2',
								autoLoad : {
									url : Sbi.config.contextName + '/themes/'
											+ Sbi.config.currTheme
											+ '/html/dsrules.html'
								},
								layout : 'fit',
								width : 620,
								height : 410,
								autoScroll : true,
								closeAction : 'close',
								buttonAlign : 'left',
								plain : true,
								title : LN('sbi.ds.help')
							});
						}
						;
						win_info_2.show();
					}





				});
