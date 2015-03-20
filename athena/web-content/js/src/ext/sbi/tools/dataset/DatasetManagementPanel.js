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
 * Authors - Chiara Chiarelli (chiara.chiarelli@eng.it)
 * Monica Franceschini (monica.franceshini@eng.it)
 */
//var thisPanel;

Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.DatasetManagementPanel = function(config) {

	var paramsList = {
		MESSAGE_DET : "DATASETS_LIST"
	};
	var paramsSave = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "DATASET_INSERT"
	};
	var paramsDel = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "DATASET_DELETE"
	};

	this.configurationObject = {};

	this.configurationObject.manageListService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'MANAGE_DATASETS_ACTION',
				baseParams : paramsList
			});
	this.configurationObject.saveItemService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'MANAGE_DATASETS_ACTION',
				baseParams : paramsSave
			});
	this.configurationObject.deleteItemService = Sbi.config.serviceRegistry
			.getServiceUrl({
				serviceName : 'MANAGE_DATASETS_ACTION',
				baseParams : paramsDel
			});
	
	this.configurationObject.uploadFileService = Sbi.config.serviceRegistry
	.getServiceUrl({
		serviceName : 'UPLOAD_DATASET_FILE_ACTION',
		baseParams : {LIGHT_NAVIGATOR_DISABLED: 'TRUE'}
	});
	
	this.configurationObject.getDatamartsService = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName : 'GET_META_MODELS_ACTION',
		baseParams :  {LIGHT_NAVIGATOR_DISABLED: 'TRUE'}
	});
	//this.configurationObject.getDatamartsService = Sbi.config.qbeGetDatamartsUrl;

	this.initConfigObject();
	
	
	this.configurationObject.filter = true;
	this.configurationObject.columnName = [['label', LN('sbi.generic.label')],
	                                       ['name', LN('sbi.generic.name')],
	                                       ['category.valueNm', LN('sbi.ds.catType')]
	                	                   ];
	this.configurationObject.setCloneButton = true;
	config.configurationObject = this.configurationObject;
	config.singleSelection = true;
	
	
	//added
	config.id = 'datasetForm';
	config.fileUpload = true; // this is a multipart form!!
	config.isUpload=true;
	config.method='POST';
    config.enctype='multipart/form-data';
    

    config.tabPanelWidth ='70%'; // 520;
    config.gridWidth = '30%'; //470;

	var c = Ext.apply({}, config || {}, {});

	Sbi.tools.dataset.DatasetManagementPanel.superclass.constructor.call(this, c);
	
	this.fileUploaded = false; //used to warn that a new Dataset File is uploaded
	
	this.rowselModel.addListener('rowselect', function(sm, row, rec) {
		this.activateDsTypeForm(null, rec, row);
		this.activateTransfForm(null, rec, row);
		this.activatePersistForm(null, rec.get('isPersisted'), null, null);
		this.activateDsVersionsGrid(null, rec, row);
		this.activateDsTestTab(this.datasetTestTab);
		this.manageDatasetFieldMetadataGrid.loadItems(rec.get("meta"), rec);
		this.setValues(rec);
		// destroy the qbe query builder, if existing
		if (this.qbeDataSetBuilder != null) {
			this.qbeDataSetBuilder.destroy();
			this.qbeDataSetBuilder = null;
		}
		this.manageFormsEnabling();
		this.fileUploaded = false; //reset to default

	}, this);
	
	this.tabs.addListener('tabchange', this.modifyToolbar, this);
	
	thisPanel = this;

	//invokes before each ajax request 
    Ext.Ajax.on('beforerequest', this.showMask, this);   
    // invokes after request completed 
    Ext.Ajax.on('requestcomplete', this.hideMask, this);            
    // invokes if exception occured 
    Ext.Ajax.on('requestexception', this.hideMask, this); 

	this.manageFormsDisabling();


};

Ext.extend(Sbi.tools.dataset.DatasetManagementPanel, Sbi.widgets.ListDetailForm, {

	configurationObject : null,
	tbInfoButton: null,
	tbProfAttrsButton : null,
	tbTransfInfoButton: null,
	tbPersistInfoButton: null,
	gridForm : null,
	mainElementsStore : null,
	profileAttributesStore: null,
	trasfDetail : null,
	persistDetail : null,
	jClassDetail : null,
	customDataDetail : null,		
	scriptDetail : null,
	queryDetail : null,
	flatDetail : null,
	WSDetail : null,
	fileDetail : null,
	parsGrid : null,
	datasetTestTab : null,
	manageParsGrid : null,
	manageDsVersionsGrid : null,
	manageDatasetFieldMetadataGrid: null,
	newRecord: null,
	detailFieldId: null,
	detailFieldUserIn: null,
	detailFieldDateIn: null,
	detailFieldVersNum: null,
	detailFieldVersId: null,
	qbeDataSetBuilder: null,
	customDataGrid: null,
	detailFieldScopeId: null

	
	, manageFormsDisabling: function(){
		this.tabs.disable();
	}
	, manageFormsEnabling: function(){
		this.tabs.enable();		
	}
	,
	modifyToolbar : function(tabpanel, panel) {
		var itemId = panel.getItemId();
		if (itemId !== undefined && itemId !== null && itemId === 'type') {
			this.tbInfoButton.show();
			this.tbProfAttrsButton.show();
			this.tbTransfInfoButton.hide();
			this.tbPersistInfoButton.hide();
		} else if (itemId !== undefined && itemId !== null && itemId === 'transf') {
			this.tbTransfInfoButton.show();
			this.tbInfoButton.hide();
			this.tbProfAttrsButton.hide();
			this.tbPersistInfoButton.hide();
		} else if (itemId !== undefined && itemId !== null && itemId === 'advanced') {
			this.tbTransfInfoButton.hide();
			this.tbInfoButton.hide();
			this.tbProfAttrsButton.hide();
			this.tbPersistInfoButton.show();
		} else {
			this.tbInfoButton.hide();
			this.tbProfAttrsButton.hide();
			this.tbTransfInfoButton.hide();
			this.tbPersistInfoButton.hide();
		}
	}

	,
	activateTransfForm : function(combo, record, index) {
		var transfSelected = record.get('trasfTypeCd');
		if (transfSelected != null && transfSelected == 'PIVOT_TRANSFOMER') {
			this.trasfDetail.setVisible(true);
		} else {
			this.trasfDetail.setVisible(false);
		}
	}
 
	
					// extjs4: ,activatePersistForm : function(check, newValue,
					// oldValue, opts) {
					,
					activatePersistForm : function(check, checked) {
						// var persistSelected = newValue;
						var persistSelected = checked;
						if (persistSelected != null && persistSelected == true) {
							this.persistDetail.setVisible(true);
						} else {
							this.persistDetail.setVisible(false);
						}
					}

					,
					activateDsTestTab : function(panel) {
						if (panel) {
							var record = this.rowselModel.getSelected();
							if (record) {
								var dsParsList = this.manageParsGrid
										.getParsArray();
								this.parsGrid.fillParameters(dsParsList);
							}
						}
					},
					activateDsVersionsGrid : function(combo, record, index) {
						var dsVersionsList = record.get('dsVersions');
						this.manageDsVersionsGrid.loadItems(dsVersionsList);
					}

					,
					activateDsTypeForm : function(combo, record, index) {

						var dsTypeSelected = record.get('dsTypeCd');
						if (dsTypeSelected != null && dsTypeSelected == 'File') {
							this.fileDetail.setVisible(true);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Query') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(true);
							this.jClassDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Java Class') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(true);
							this.customDataDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Web Service') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(true);
							this.qbeQueryDetail.setVisible(false);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Script') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.scriptDetail.setVisible(true);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Qbe') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(true);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Custom') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.customDataDetail.setVisible(true);
							this.flatDetail.setVisible(false);
						} else if (dsTypeSelected != null
								&& dsTypeSelected == 'Flat') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.flatDetail.setVisible(true); 
						} else if (dsTypeSelected != null
								|| dsTypeSelected == '') {
							this.fileDetail.setVisible(false);
							this.queryDetail.setVisible(false);
							this.jClassDetail.setVisible(false);
							this.scriptDetail.setVisible(false);
							this.WSDetail.setVisible(false);
							this.qbeQueryDetail.setVisible(false);
							this.customDataDetail.setVisible(false);
							this.flatDetail.setVisible(false); 
						}

						var dsParsList = record.get('pars');
						if (dsParsList != null && dsParsList != undefined) {
							this.manageParsGrid.loadItems(dsParsList);
						} else {
							this.manageParsGrid.loadItems([]);
						}

						if (record && record.json) {
							var dsCustomList = record.json.customs;
							if (dsCustomList != null
									&& dsCustomList != undefined) {
								this.customDataGrid.loadItems(dsCustomList);
							} else {
								this.customDataGrid.loadItems([]);
							}
						}

					}

				,test : function(button, event, service) {
					var values = this.getValues();

					var requestParameters = {
						start : 0,
						limit : 25,
						dsId : values['id'],
						dsTypeCd : values['dsTypeCd'],
						fileName : values['fileName'],
						csvDelimiter : values['csvDelimiter'],
						skipRows : values['skipRows'],						
						limitRows : values['limitRows'],						
						xslSheetNumber : values['xslSheetNumber'],						
						fileType : values['fileType'],
						csvQuote : values['csvQuote'],
						skipRows : values['skipRows'],
						limitRows : values['limitRows'],
						xslSheetNumber : values['xslSheetNumber'],
						query : values['query'],
						queryScript : values['queryScript'],
						queryScriptLanguage : values['queryScriptLanguage'],
						dataSource : values['dataSource'],
						wsAddress : values['wsAddress'],
						wsOperation : values['wsOperation'],
						script : values['script'],
						scriptLanguage : values['scriptLanguage'],
						jclassName : values['jclassName'],
						customData : values['customData'],
						trasfTypeCd : values['trasfTypeCd'],
						pivotColName : values['pivotColName'],
						pivotColValue : values['pivotColValue'],
						pivotRowName : values['pivotRowName'],
						pivotIsNumRows : values['pivotIsNumRows'],
						isPersisted : values['isPersisted'],
						persistTableName : values['persistTableName'],
						flatTableName : values['flatTableName'],
						dataSourceFlat : values['dataSourceFlat'],
						qbeSQLQuery : values['qbeSQLQuery'],
						qbeJSONQuery : values['qbeJSONQuery'],
						qbeDataSource: values['qbeDataSource'],
						qbeDatamarts: values['qbeDatamarts'],
						userIn: values['userIn'],
						dateIn: values['dateIn'],
						versNum: values['versNum'],
						versId: values['versId'],
						meta: values['meta'],
						fileUploaded: thisPanel.fileUploaded,
						scopeCd: values['scopeCd']
					};
					arrayPars = this.parsGrid.getParametersValues();
					if (arrayPars) {
						requestParameters.pars = Ext.util.JSON
								.encode(arrayPars);
					}

					customString = this.customDataGrid.getDataString();
					
					if (customString) {
						requestParameters.customData = Ext.util.JSON.encode(customString);
					}
					
					if (this.previewWindow === undefined) {
						this.previewWindow = new Sbi.tools.dataset.PreviewWindow({
							modal : true
							, width: this.getWidth() - 50
							, height: this.getHeight() - 50
						});
					}
					this.previewWindow.show();
					this.previewWindow.load(requestParameters);
				}							

				,initConfigObject : function() {
					this.configurationObject.fields = [ 'id', 'name',
							'label', 'description', 'dsTypeCd', 
							'catTypeVn', 'isPublic', 'usedByNDocs', 'fileName','fileType','csvDelimiter','csvQuote','skipRows','limitRows','xslSheetNumber',
							'query', 'queryScript', 'queryScriptLanguage','dataSource', 'wsAddress',
							'wsOperation', 'script', 'scriptLanguage',
							'jclassName', 'jclassNameForCustom', 'customData', 'pars', 'trasfTypeCd',
							'pivotColName', 'pivotColValue',
							'pivotRowName', 'pivotIsNumRows', 'dsVersions',
							'isPersisted', 'persistTableName',
							'flatTableName', 'dataSourceFlat',
							'qbeSQLQuery', 'qbeJSONQuery', 'qbeDataSource',
							'qbeDatamarts',	'userIn','dateIn','versNum','versId','meta', 'scopeCd'];

					this.configurationObject.emptyRecToAdd = new Ext.data.Record(
							{	id : null,
								name : '', label : '', description : '',
								dsTypeCd : '', catTypeVn : '', isPublic:'', usedByNDocs : 0,
								csvDelimiter: '', fileType: '',csvQuote: '', skipRows: '', limitRows: '', xslSheetNumber: '',
								fileName : '', query : '', queryScript : '', queryScriptLanguage : '', dataSource : '',
								wsAddress : '', wsOperation : '', script : '',
								scriptLanguage : '', jclassName : '',jclassNameForCustom:'', customData: '', pars : [],
								trasfTypeCd : '', pivotColName : '', pivotColValue : '',								
								pivotRowName : '', pivotIsNumRows : '', 
								isPersisted:'', persistTableName:'',
								flatTableName:'', dataSourceFlat : '',
								qbeSQLQuery: '',qbeJSONQuery: '', qbeDataSource: '', qbeDatamarts: '',
								dsVersions : [], userIn:'',dateIn:'',versNum:'',versId:'',meta:[], scopeCd:''
							});

					this.configurationObject.gridColItems = [ {
						id : 'label',
						header : LN('sbi.generic.label'),
						width : 105,
						sortable : true,
						locked : false,
						dataIndex : 'label'
					}, {
						header : LN('sbi.generic.name'),
						width : 105,
						sortable : true,
						dataIndex : 'name'
					}, {
						header : LN('sbi.generic.type'),
						width : 60,
						sortable : true,
						dataIndex : 'dsTypeCd'
					}, {
						header : LN('sbi.ds.numDocs'),
						width : 55,
						sortable : true,
						dataIndex : 'usedByNDocs'
					} ];

					this.configurationObject.panelTitle = LN('sbi.ds.panelTitle');
					this.configurationObject.listTitle = LN('sbi.ds.listTitle');
					
					var tbButtonsArray = new Array();
					
					this.tbFieldsMetadataButton = new Ext.Toolbar.Button({
		 	            text: LN('sbi.ds.metadata'),
		 	            iconCls: 'icon-metadata',
		 	            handler: this.fieldsMetadata,
		 	            width: 30,
		 	            scope: this
		 	            });
					tbButtonsArray.push(this.tbFieldsMetadataButton);
					
					this.tbProfAttrsButton = new Ext.Toolbar.Button({
		 	            text: LN('sbi.ds.pars'),
		 	            iconCls: 'icon-profattr',
		 	            handler: this.profileAttrs,
		 	            width: 30,
		 	            scope: this
		 	            });
					tbButtonsArray.push(this.tbProfAttrsButton);
					
					this.tbInfoButton = new Ext.Toolbar.Button({
		 	            text: LN('sbi.ds.help'),
		 	            iconCls: 'icon-info',
		 	            handler: this.info,
		 	            width: 30,
		 	            scope: this
		 	            });
					tbButtonsArray.push(this.tbInfoButton);
					
					this.tbTransfInfoButton = new Ext.Toolbar.Button({
		 	            text: LN('sbi.ds.help'),
		 	            iconCls: 'icon-info',
		 	            handler: this.transfInfo,
		 	            width: 30,
		 	            scope: this
		 	            });
					tbButtonsArray.push(this.tbTransfInfoButton);
					
					this.tbPersistInfoButton = new Ext.Toolbar.Button({
		 	            text: LN('sbi.ds.help'),
		 	            iconCls: 'icon-info',
		 	            handler: this.persistInfo,
		 	            width: 30,
		 	            scope: this
		 	            });
					tbButtonsArray.push(this.tbPersistInfoButton);
					this.configurationObject.tbButtonsArray = tbButtonsArray;

					this.initTabItems();
				}
				
				,initTabItems : function() {				
					this.initDetailTab();
					this.initTypeTab();
					this.initTrasfTab();
					this.initAdvancedTab();
					this.initTestTab();				
				}
				
				,initDetailTab : function() {
					this.profileAttributesStore = new Ext.data.SimpleStore({
						fields : [ 'profAttrs' ],
						data : config.attrs,
						autoLoad : false
					});
					
					// Store of the combobox
					this.catTypesStore = new Ext.data.SimpleStore({
						fields : [ 'catTypeVn' ],
						data : config.catTypeVn,
						autoLoad : false
					});		
					
					this.scopeStore = new Ext.data.SimpleStore({
						fields : [ 'scopeCd' ],
						data : config.scopeCd,
						autoLoad : false
					});
                  		
                  	this.isPublicStore = new Ext.data.SimpleStore({
                  		fields: ['value', 'field', 'description'],
                  		data : [
                          		[true,'Public', 'Everybody can view this datset'],
                          		[false, 'Private', 'The saved dataset will be visible only to you']
                          	] 
                  	});    

					// START list of detail fields
					this.detailFieldId = new Ext.form.TextField({
						name : 'id',
						hidden : true
					});
					
					this.detailFieldUserIn = new Ext.form.TextField({
							name : 'userIn',
							hidden : true
						});
					
					this.detailFieldDateIn = new Ext.form.TextField({
							name : 'dateIn',
							hidden : true
					});
					
					this.detailFieldVersNum = new Ext.form.TextField({
							name : 'versNum',
							hidden : true
					});
					
					this.detailFieldVersId = new Ext.form.TextField({
							name : 'versId',
							hidden : true
					});

					var detailFieldName = {
						maxLength : 50,	minLength : 1, width : 350,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.generic.name'),
						allowBlank : false,	validationEvent : true,
						name : 'name'
					};

					var detailFieldLabel = {
						maxLength : 50, minLength : 1, width : 350,
						regexText : LN('sbi.roles.alfanumericString2'),
						fieldLabel : LN('sbi.generic.label'),
						allowBlank : false, validationEvent : true,
						name : 'label'
					};

					var detailFieldDescr = {
						xtype : 'textarea',
						width : 350, height : 80, maxLength : 160,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.generic.descr'),
						validationEvent : true,
						name : 'description'
					};

					var detailFieldCatType = {
						name : 'catTypeVn',
						store : this.catTypesStore,
						width : 350,
						fieldLabel : LN('sbi.ds.catType'),
						displayField : 'catTypeVn', 
						valueField : 'catTypeVn', 
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : true, validationEvent : true,
						xtype : 'combo'
					};
					
					var scopeField = new Ext.form.ComboBox({
						name : 'isPublic',
						store : this.isPublicStore,
						width : 350,
						fieldLabel : LN('sbi.ds.scope'),
						displayField : 'field', 
						valueField : 'value', 
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : true, validationEvent : true,
						xtype : 'combo'							    	
					});
					
					var scopeCdField = new Ext.form.ComboBox({
						name : 'scopeCd',
						store : this.scopeStore,
						width : 350,
						//fieldLabel : LN('sbi.ds.scope'),
						displayField : 'scopeCd', 
						valueField : 'scopeCd', 
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : true, validationEvent : true,
						xtype : 'combo'							    	
					});
					// END list of detail fields

					var c = {};
					this.manageDatasetFieldMetadataGrid = new Sbi.tools.ManageDatasetFieldMetadata(c);
					
					this.manageDsVersionsGrid = new Sbi.tools.ManageDatasetVersions(c);
					this.manageDsVersionsGrid.addListener('verionrestored', function(version) {
						
						var values = this.getValues();
						var newDsVersion = new Ext.data.Record(
								{	dsId: values['id'],
									dateIn : values['dateIn'],
									userIn : values['userIn'],
									versId : values['versId'],
									type : values['dsTypeCd'], 
									versNum : values['versNum']
								});
						this.manageDsVersionsGrid.getStore().addSorted(newDsVersion);
						this.manageDsVersionsGrid.getStore().commitChanges();	
						var rec = this.buildNewRecordDsVersion(version);
						this.activateDsTypeForm(null, rec, null);
						this.activateTransfForm(null, rec, null);
						this.activatePersistForm(null, rec.get('isPersisted'), null, null);
						this.activateDsTestTab(this.datasetTestTab);
						this.setValues(rec);
						this.updateMainStore(values['id']);
					}, this);
					
					this.manageDsVersionsGrid.addListener('verionsdeleted', function() {
						var values = this.getValues();
						this.updateDsVersionsOfMainStore(values['id']);
					}, this);

					this.manageDsVersionsPanel = new Ext.Panel(
							{	id : 'man-vers',
								title : LN('sbi.ds.versionPanel'),
								layout : 'fit',
								autoScroll : true,
								style : {
									"margin-top" : "20px"
								},
								border : true,
								items : [ this.manageDsVersionsGrid ],
								scope : this
							});

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
									autoHeight : true, autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 8px 10px;'
											: 'padding:8px 10px;',
									border : false,
									
									style : {
										"margin-left" : "8px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
												: "-8px")
												: "8"
									},
									items : [ 
									        detailFieldLabel, detailFieldName,
											detailFieldDescr, detailFieldCatType, scopeField, scopeCdField, this.manageDsVersionsPanel ,
											this.detailFieldUserIn,this.detailFieldDateIn,this.detailFieldVersNum,this.detailFieldVersId,this.detailFieldId
											]
								}
							});
				}
				
				
				
				
				
				
				
				,initTypeTab : function() {
					
					this.dsTypesStore = new Ext.data.SimpleStore({
						fields : [ 'dsTypeCd' ],
						data : config.dsTypes,
						autoLoad : false
					});

					this.dataSourceStore = new Ext.data.SimpleStore({
						fields : [ 'dataSource','name' ],
						data : config.dataSourceLabels,
						autoLoad : false
					});

					this.scriptLanguagesStore = new Ext.data.SimpleStore({
						fields : [ 'scriptLanguage' ,'name'],
						data : config.scriptTypes,
						autoLoad : false
					});

					// START list of Advanced fields
					var detailDsType = new Ext.form.ComboBox({
						name : 'dsTypeCd',
						store : this.dsTypesStore,
						width : 350, //160,
						fieldLabel : LN('sbi.ds.dsTypeCd'),
						displayField : 'dsTypeCd', // what the user sees in
						// the popup
						valueField : 'dsTypeCd', // what is passed to the
						// 'change' event
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : false, validationEvent : true
					});
					detailDsType.addListener('select',this.activateDsTypeForm, this);
			
					this.fileUploadFormPanel = new Sbi.tools.dataset.FileDatasetPanel(config);
					var uploadButton = this.fileUploadFormPanel.getComponent('fileUploadPanel').getComponent('fileUploadButton');
					
					uploadButton.setHandler(this.uploadFileButtonHandler, this);

					
				

					this.detailDataSource = new Ext.form.ComboBox({
						name : 'dataSource',
						store : this.dataSourceStore,
						width : 350, //180,
						fieldLabel : LN('sbi.ds.dataSource'),
						displayField : 'dataSource', // what the user
						// sees in the
						// popup
						valueField : 'dataSource', // what is passed to the
						// 'change' event
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : false, validationEvent : true
					});

					this.detailQbeDataSource = new Ext.form.ComboBox({
						name : 'qbeDataSource',
						store : this.dataSourceStore,
						width : 350,
						fieldLabel : LN('sbi.ds.dataSource'),
						displayField : 'dataSource', // what the user
						// sees in the popup
						valueField : 'dataSource', // what is passed to the
						// 'change' event
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : false, validationEvent : true
					});

					this.detailQuery = new Ext.form.TextArea({
						maxLength : 30000,
						xtype : 'textarea',
						width : '100%',// 350,
						height : 400, //140,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.query'),
						validationEvent : true,
						allowBlank : false,
						name : 'query'
					});
					
					this.detailQueryScript = new Ext.form.TextArea({
						maxLength : 30000,
						xtype : 'textarea',
						width : '100%', //350,
						height : 300, //80,
						//regexText : 'script',
						fieldLabel : 'script',
						//validationEvent : true,
						allowBlank : false,
						name : 'queryScript'
					});

					this.detailWsAddress = new Ext.form.TextField({
						maxLength : 250, minLength : 1,
						width : 350,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.wsAddress'),
						allowBlank : false, validationEvent : true,
						name : 'wsAddress'
					});

					this.detailWsOperation = new Ext.form.TextField({
						maxLength : 50, minLength : 1, width : 350,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.wsOperation'),
						allowBlank : true, validationEvent : true,
						name : 'wsOperation'
					});

					this.detailScript = new Ext.form.TextArea({
						maxLength : 30000,
						xtype : 'textarea',
						//width : this.textAreaWidth,
						height : 350, //195,
						width : '100%', //350,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.script'),
						allowBlank : false, validationEvent : true,
						name : 'script'
					});

					var openQbeWizardButton = new Ext.Button(
							{
								text : LN('sbi.ds.openQbeQizard'),
								handler : this.jsonTriggerFieldHandler,
								scope : this,
								icon : 'null' // workaround: without this,
							// the button shows other
							// icons (such as Bold, Italic, ...) in
							// background
							});
					
					this.qbeSQLQuery = new Ext.form.Hidden({
						name : 'qbeSQLQuery'
					});
					
					this.qbeJSONQuery = new Ext.form.TriggerField({
						name : 'qbeJSONQuery'
						, valueField : 'qbeJSONQuery'
						, width : 350 
						, fieldLabel : 'Qbe Query'
						, triggerClass: 'x-form-search-trigger'
						, editable: false
					});
					this.qbeJSONQuery.on("render", function(field) {
						field.trigger.on("click", function(e) {
							this.jsonTriggerFieldHandler(); 
						}, this);
					}, this);

					var datamartsStore = new Ext.data.JsonStore({
					    url: this.configurationObject.getDatamartsService,
					    root: 'rows',
					    fields: ['id', 'name', 'description']
					});
					
					this.qbeDatamarts = new Ext.form.ComboBox({
						name : 'qbeDatamarts'
			    	   	, fieldLabel: LN('sbi.tools.managedatasets.datamartcombo.label')
			    	   	, forceSelection: true
			    	   	, editable: false
			    	   	, store: datamartsStore
			    	   	, width : 350 
			    	   	, displayField: 'name'
			    	    , valueField: 'name'
			    	    , typeAhead: true
			    	    , triggerAction: 'all'
			    	    , selectOnFocus: true
			    	    , allowBlank: false
			    	});

					this.detailScriptLanguage = new Ext.form.ComboBox({
						name : 'scriptLanguage',
						store : this.scriptLanguagesStore,
						width : 350, //160,
						fieldLabel : LN('sbi.ds.scriptLanguage'),
						displayField : 'name', // what the user
						// sees in the
						// popup
						valueField : 'scriptLanguage', // what is passed to
						// the
						// 'change' event
						typeAhead : true,
						forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true,
						editable : false,
						allowBlank : false,
						validationEvent : true,
						xtype : 'combo'
					});

					this.detailJclassName = new Ext.form.TextField({
						maxLength : 100,
						minLength : 1,
						width : 350,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.jclassName'),
						allowBlank : false,
						validationEvent : true,
						name : 'jclassName'
					});
					
					this.detailJclassNameForCustom = new Ext.form.TextField({
						maxLength : 100,
						minLength : 1,
						width : 350,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.jclassName'),
						allowBlank : false,
						validationEvent : true,
						name : 'jclassNameForCustom'
					});
					
					this.customDataGrid = new Sbi.tools.dataset.CustomDataGrid();
					

					this.dsTypeDetail = new Ext.form.FieldSet(
							{
								labelWidth : 100,
								defaultType : 'textfield',
								//autoHeight : true,
								autoScroll : true,
								border : false,
								style : {
									"margin-left" : "5px",
									"margin-bottom" : "0px",
									"margin-top" : "3px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "0px"
											: "-3px")
											: "0px"
								},
								items : [ detailDsType ]
							});
					
					this.queryDetail = new Sbi.tools.dataset.QueryDatasetConfigurationPanel(config);

					this.qbeQueryDetail = new Ext.form.FieldSet(
							{
								labelWidth : 120,
								defaults : {
									//width : 280,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								border : true,
								style : {
									"margin-left" : "3px",
									"margin-top" : "0px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
											: "-5px")
											: "3px"
								},
								items : [
								        this.detailQbeDataSource,
								        this.qbeDatamarts,
										this.qbeSQLQuery,
										this.qbeJSONQuery
										]
							});

					this.jClassDetail = new Ext.form.FieldSet(
							{
								labelWidth : 100,
								defaults : {
									//width : 280,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								border : true,
								style : {
									"margin-left" : "3px",
									"margin-top" : "0px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
											: "-5px")
											: "3px"
								},
								items : [ this.detailJclassName ]
							});
					
					
					this.customDataDetail = new Ext.form.FieldSet(
							{
								labelWidth : 100,
								defaults : {
									//width : 280,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								border : true,
								style : {
									"margin-left" : "3px",
									"margin-top" : "0px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
											: "-5px")
											: "3px"
								},
								items : [ this.detailJclassNameForCustom, this.customDataGrid ]
							});

						this.fileDetail = new Ext.form.FieldSet({
							labelWidth : 80,
							defaults : {
								// width : 280,
								border : true
							},
							defaultType : 'textfield',
							autoHeight : true,
							autoScroll : true,
							border : true,
							items : [ this.fileUploadFormPanel ]
						});

					this.WSDetail = new Ext.form.FieldSet(
							{
								labelWidth : 100,
								defaults : {
									//width : 280,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								border : true,
								style : {
									"margin-left" : "3px",
									"margin-top" : "0px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
											: "-5px")
											: "3px"
								},
								items : [ this.detailWsAddress,
										this.detailWsOperation ]
							});

					this.scriptDetail = new Ext.form.FieldSet(
							{
								labelWidth : 100,
								defaults : {
								//	width : 280,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								border : true,
								style : {
									"margin-left" : "3px",
									"margin-top" : "0px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
											: "-5px")
											: "3px"
								},
								items : [ this.detailScriptLanguage,
										this.detailScript ]
							});
					
					
					var detailFlatTableName = new Ext.form.TextField({
						maxLength : 50,
						minLength : 1,
						width : 350,
						fieldLabel : LN('sbi.ds.flatTableName'),
						allowBlank : false,
						validationEvent : true,
						name : 'flatTableName'
					});
					
					var dataSourceFlat = new Ext.form.ComboBox({
						id : 'dataSourceFlat',
						name : 'dataSourceFlat',
						store : this.dataSourceStore,
						width : 350, //180,
						fieldLabel : LN('sbi.ds.dataSource'),
						displayField : 'dataSource', // what the user sees in the popup
						valueField : 'dataSource', // what is passed to the 'change' event
						typeAhead : true, forceSelection : true,
						mode : 'local',
						triggerAction : 'all',
						selectOnFocus : true, editable : false,
						allowBlank : false, validationEvent : true
					});
					
					this.flatDetail = new Ext.form.FieldSet({
							labelWidth : 100,
							defaults : {
								border : true
							},
							defaultType : 'textfield',
							autoHeight : true,
							autoScroll : true,
							border : true,
							style : {
								"margin-left" : "3px",
								"margin-top" : "0px",
								"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-3px"
										: "-5px")
										: "3px"
							},
							items : [ detailFlatTableName,
									dataSourceFlat ]
					});

					var c = {};
					this.manageParsGrid = new Sbi.tools.ManageDatasetParameters(
							c);

					this.manageParsPanel = new Ext.Panel(
							{
								id : 'man-pars',
								layout : 'fit',
								autoScroll : false,
								bodyStyle: Ext.isIE ? 'padding:0 0 3px 3px;' : 'padding:3px 3px;',
								border : true,
								height: 'auto',
								autoScroll : true,
						        boxMaxHeight: 300,
						        boxMinHeight: 100,
								items : [ this.manageParsGrid ],
								scope : this
							});

					this.typeTab = new Ext.Panel(
							{
								title : LN('sbi.generic.type'),
								itemId : 'type',
								width : 350,
								items : {
									id : 'type-detail',
									itemId : 'type-detail',
									// columnWidth: 0.4,
									xtype : 'fieldset',
									scope : this,
									labelWidth : 80,
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									border : false,
									style : {
										"margin-left" : "5px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
												: "-7px")
												: "0"
									},
									items : [ this.dsTypeDetail,
											this.jClassDetail,
											this.customDataDetail,
											this.scriptDetail,
											this.queryDetail,
											this.WSDetail, this.fileDetail,
											this.qbeQueryDetail,
											this.flatDetail,
											this.manageParsPanel ]
								}
							});
				}

				
				
				
				
				
				
				
				
				
				
				
				
				//handler for the upload file button
				,uploadFileButtonHandler: function(btn, e) {
					
					Sbi.debug("[DatasetManagementPanel.uploadFileButtonHandler]: IN");
					
			        var form = Ext.getCmp('datasetForm').getForm();
			        
			        Sbi.debug("[DatasetManagementPanel.uploadFileButtonHandler]: form is equal to [" + form + "]");
					
			        var completeUrl = thisPanel.configurationObject.uploadFileService;
					var baseUrl = completeUrl.substr(0, completeUrl
							.indexOf("?"));
					
					Sbi.debug("[DatasetManagementPanel.uploadFileButtonHandler]: base url is equal to [" + baseUrl + "]");
					
					var queryStr = completeUrl.substr(completeUrl
							.indexOf("?") + 1);
					var params = Ext.urlDecode(queryStr);

					Sbi.debug("[DatasetManagementPanel.uploadFileButtonHandler]: base url is equal to [" + Sbi.toSource(params) + "]");
					
					
					Sbi.debug("[DatasetManagementPanel.uploadFileButtonHandler]: form is valid [" + form.isValid() + "]");
					
					
					form.submit({
						clientValidation: false,
						url : baseUrl // a multipart form cannot
										// contain parameters on its
										// main URL; they must POST
										// parameters
						,
						params : params,
						waitMsg : LN('sbi.generic.wait'),
						success : function(form, action) {
							Ext.MessageBox.alert('Success!','File Uploaded to the Server');
							var fileNameUploaded = Ext.getCmp('fileUploadField').getValue();
							//get only the file name from the path (for IE)
							fileNameUploaded = fileNameUploaded.replace(/^.*[\\\/]/, '');
							Ext.getCmp('fileNameField').setValue(fileNameUploaded);							
							this.fileUploadFormPanel.activateFileTypePanel(action.result.fileExtension);
							thisPanel.fileUploaded = true;
						},
						failure : function(form, action) {
							switch (action.failureType) {
				            case Ext.form.Action.CLIENT_INVALID:
				                Ext.Msg.alert('Failure', 'Form fields may not be submitted with invalid values');
				                break;
				            case Ext.form.Action.CONNECT_FAILURE:
				                Ext.Msg.alert('Failure', 'Ajax communication failed');
				                break;
				            case Ext.form.Action.SERVER_INVALID:
				            	if(action.result.msg && action.result.msg.indexOf("NonBlockingError:")>=0){
				            		var error = Ext.util.JSON.decode(action.result.msg);
				            		if(error.error=='USED'){//the file is used from more than one dataset
				            			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.'+error.error)+error.used+" datasets",LN("sbi.ds.failedToUpload") );
				            		}else{
				            			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.'+error.error),LN("sbi.ds.failedToUpload"));
				            		}
				            		
				            	}else{
				            		Sbi.exception.ExceptionHandler.showErrorMessage(action.result.msg,'Failure');
				            	}
							}
						},
						scope : this
					});		
					
					Sbi.debug("[DatasetManagementPanel.uploadFileButtonHandler]: OUT");
				}
				
				,initTrasfTab : function() {
					this.transfTypesStore = new Ext.data.SimpleStore({
						fields : [ 'trasfTypeCd' ],
						data : config.trasfTypes,
						autoLoad : false
					});

					var detailTransfType = new Ext.form.ComboBox({
						name : 'trasfTypeCd',
						store : this.transfTypesStore,
						width : 350,//120,
						fieldLabel : LN('sbi.ds.trasfTypeCd'),
						displayField : 'trasfTypeCd', // what the user
						// sees in the
						// popup
						valueField : 'trasfTypeCd', // what is passed to the
						// 'change' event
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
					detailTransfType.addListener('select',
							this.activateTransfForm, this);

					this.trasfTypeDetail = new Ext.form.FieldSet(
							{
								labelWidth : 120,
								defaults : {
									width : 350,//260,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
										: 'padding:10px 15px;',
								border : true,
								style : {
									"margin-left" : "10px",
									"margin-top" : "10px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
											: "-13px")
											: "10px"
								},
								items : [ detailTransfType ]
							});

					var detailPivotCName = {
						maxLength : 40,
						minLength : 1,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.pivotColName'),
						allowBlank : true,
						validationEvent : true,
						name : 'pivotColName'
					};

					var detailPivotCValue = {
						maxLength : 40,
						minLength : 1,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.pivotColValue'),
						allowBlank : true,
						validationEvent : true,
						name : 'pivotColValue'
					};

					var detailPivotRName = {
						maxLength : 40,
						minLength : 1,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.pivotRowName'),
						allowBlank : true,
						validationEvent : true,
						name : 'pivotRowName'
					};

					var detailIsNumRow = new Ext.form.Checkbox({
						xtype : 'checkbox',
						itemId : 'pivotIsNumRows',
						name : 'pivotIsNumRows',
						fieldLabel : LN('sbi.ds.pivotIsNumRows')
					});

					this.trasfDetail = new Ext.form.FieldSet(
							{
								labelWidth : 150,
								defaults : {
									width : 350,
									border : true
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
										: 'padding:10px 15px;',
								border : true,
								style : {
									"margin-left" : "10px",
									"margin-top" : "10px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
											: "-13px")
											: "10px"
								},
								items : [ detailPivotCName,
										detailPivotCValue,
										detailPivotRName, detailIsNumRow ]
							});

					this.transfTab = new Ext.Panel(
							{
								title : LN('sbi.ds.transfType'),
								itemId : 'transf',
								width : 350,
								items : {
									id : 'transf-detail',
									itemId : 'transf-detail',
									xtype : 'fieldset',
									scope : this,
									labelWidth : 90,
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:0px 0px;',
									border : false,
									style : {
										"margin-left" : "10px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "0"
									},
									items : [ this.trasfTypeDetail,
											this.trasfDetail ]
								}
							});
				}
				
				, initPersistTab : function(){
					this.isPersisted = new Ext.form.Checkbox({
						xtype : 'checkbox',
						itemId : 'isPersisted',
						name : 'isPersisted',
						fieldLabel : LN('sbi.ds.isPersisted')
					});
					//extjs4: this.isPersisted.addListener('change',	this.activatePersistForm, this);
					this.isPersisted.addListener('check',	this.activatePersistForm, this);
					
					var persistTableName = new Ext.form.TextField({
						maxLength : 50, minLength : 1,
						width : 200,
						regexText : LN('sbi.roles.alfanumericString'),
						fieldLabel : LN('sbi.ds.persistTableName'),
						allowBlank : false, validationEvent : true,
						name : 'persistTableName'
					});

					var fsPersist = new Ext.form.FieldSet(
							{
								labelWidth : 150,
								defaults : {
								//	width : 200,
									border : false
								},
								defaultType : 'textfield',
								autoHeight : true,
								autoScroll : true,
								bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
										: 'padding:10px 15px;',
								border : true,
								style : {
									"margin-left" : "10px",
									"margin-top" : "10px",
									"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
											: "-13px")
											: "10px"
								},
								items : [ persistTableName ]
							});
					return fsPersist;
				}
				
			
				, initAdvancedTab : function() {
					
					this.persistDetail  = this.initPersistTab();
						
					this.persistPanel = new Ext.Panel(
							{
								title : LN('sbi.ds.persist'),
								itemId : 'persistPanel',
								width : '100%', //500,
								items : {
									id : 'persist-detail',
									itemId : 'persist-detail',
									xtype : 'fieldset',
									scope : this,
									labelWidth : 90,
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 15px;'
											: 'padding:0px 0px;',
									border : false,
									style : {
										"margin-left" : "0px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-10px"
												: "-13px")
												: "0"
									},
									items : [this.isPersisted,
									         this.persistDetail] 
								}
							});
					
					this.advancedTab = new Ext.Panel(
							{
								title : LN('sbi.ds.advancedTab'),
								itemId : 'advanced',
								width : '100%', //550,
								items : {
									id : 'advanced-detail',
									itemId : 'advanced-detail',
									xtype : 'fieldset',
									scope : this,
									labelWidth : 90,
									defaultType : 'textfield',
									autoHeight : true,
									autoScroll : true,
									bodyStyle : Ext.isIE ? 'padding:0 0 5px 10px;'
											: 'padding:0px 0px;',
									border : true,
									style : {
										"margin-left" : "15px",
										"margin-right" : Ext.isIE6 ? (Ext.isStrict ? "-5px"
												: "-8px")
												: "0"
									},
									items : [this.persistPanel] 
								}
							});
				  }
				
				, initTestTab : function() {
					this.tbTestDSButton = new Ext.Toolbar.Button({
						text : LN('sbi.ds.test'),
						width : 30,
						handler : this.test,
						iconCls : 'icon-execute',
						scope : this
					});

					this.tbTestToolbar = new Ext.Toolbar({
						buttonAlign : 'right',
						height : 25,
						scope : this,
						items : [ this.tbTestDSButton ]
					});

					var c = {tbar: this.tbTestToolbar};
					this.parsGrid = new Sbi.tools.ParametersFillGrid(c);

					this.datasetTestTab = new Ext.Panel({
						title : LN('sbi.ds.test'),
						id : 'test-pars',
						layout : 'fit',
						autoScroll : true,
						bodyStyle : Ext.isIE ? 'padding:0 0 10px 10px;'
								: 'padding:10px 10px;',
						border : true,
						items : [ this.parsGrid],
						scope : this
					});
					this.datasetTestTab.addListener('activate',
							this.activateDsTestTab, this);

					this.configurationObject.tabItems = [ this.detailTab,
							this.typeTab, this.transfTab, this.advancedTab, //this.persistTab, this.flatTab, 
							this.datasetTestTab ];
				}

				// OVERRIDING METHOD
				,
				addNewItem : function() {
					if(this.tabs.disabled){
						this.manageFormsEnabling();
					}
					this.newRecord = new Ext.data.Record(
							{	id : null,
								name : '', label : '', description : '',
								dsTypeCd : '', catTypeVn : '', isPublic:'', usedByNDocs : 0,
								csvDelimiter: '', fileType: '' ,csvQuote:'', skipRows:'', limitRows:'', xslSheetNumber:'',
								fileName : '', query : '', queryScript : '', queryScriptLanguage : '', dataSource : '',
								wsAddress : '', wsOperation : '', script : '',
								scriptLanguage : '', jclassName : '', jclassNameForCustom:'', customData : '', pars : [],
								trasfTypeCd : '', pivotColName : '', pivotColValue : '',
								pivotRowName : '', pivotIsNumRows : '',
								isPersisted:'', persistTableName:'',
								flatTableName:'', dataSourceFlat:'',
								qbeSQLQuery: '',
								qbeJSONQuery: '', qbeDataSource: '', qbeDatamarts: '',
								dsVersions : [],
								userIn: '',
								dateIn: '',
								versNum: 2,
								versId: 0,
								meta: [],
								scopeCd: ''
							});
					this.setValues(this.newRecord);
					this.manageParsGrid.loadItems([]);
					this.manageDsVersionsGrid.loadItems([]);
					this.manageDatasetFieldMetadataGrid.loadItems([]);
					
					this.tabs.items.each(function(item) {
						item.doLayout();
					});
					this.trasfDetail.setVisible(false);
					this.persistDetail.setVisible(false);
					
					if (this.newRecord != null
							&& this.newRecord != undefined) {
						this.mainElementsStore.add(this.newRecord);
						this.rowselModel.selectLastRow(true);
					}				
					this.tabs.setActiveTab(0);					
				}

				,cloneItem: function() {	
					var values = this.getValues();
					var params = this.buildParamsToSendToServer(values);
					var arrayPars = this.manageParsGrid.getParsArray();
					
					this.newRecord = this.buildNewRecordToSave(values);		
					this.newRecord.set('pars',arrayPars);
					this.setValues(this.newRecord);
					
					if (arrayPars) {
						this.manageParsGrid.loadItems(arrayPars);
					}else{
						this.manageParsGrid.loadItems([]);
					}
					this.manageDsVersionsGrid.loadItems([]);
					
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
				,buildNewRecordDsVersion: function(values){
					var actualValues = this.getValues();
					var newRec = new Ext.data.Record({
						id: actualValues['id'],
						name : actualValues['name'],
						label : actualValues['label'],
						usedByNDocs: actualValues['usedByNDocs'],
						dsVersions: [],
						pars: values['pars'],
						description : actualValues['description'],
						dsTypeCd : values['dsTypeCd'],
						catTypeVn : values['catTypeVn'],
						isPublic: values['isPublic'],
						usedByNDocs : values['usedByNDocs'],
						fileName : values['fileName'],
						csvDelimiter : values['csvDelimiter'],
						skipRows : values['skipRows'],
						limitRows : values['limitRows'],
						xslSheetNumber : values['xslSheetNumber'],
						fileType : values['fileType'],
						csvQuote : values['csvQuote'],												
						query : values['query'],
						queryScript : values['queryScript'],
						queryScriptLanguage : values['queryScriptLanguage'],
						dataSource : values['dataSource'],
						wsAddress : values['wsAddress'],
						wsOperation : values['wsOperation'],
						script : values['script'],
						scriptLanguage : values['scriptLanguage'],
						jclassName : values['jclassName'],
						customData : values['customData'],
						trasfTypeCd : values['trasfTypeCd'],
						pivotColName : values['pivotColName'],
						pivotColValue : values['pivotColValue'],
						pivotRowName : values['pivotRowName'],
						pivotIsNumRows : values['pivotIsNumRows'],
						isPersisted: values['isPersisted'],
						persistTableName: values['persistTableName'],
						dataSourceFlat: values['dataSourceFlat'],
						flatTableName: values['flatTableName'],
						qbeSQLQuery : values['qbeSQLQuery'],
						qbeJSONQuery : values['qbeJSONQuery'],
						qbeDataSource: values['qbeDataSource'],
						qbeDatamarts: values['qbeDatamarts'],
						userIn: values['userIn'],
						dateIn: values['dateIn'],
						versNum: values['versNum'],
						versId: values['versId'],
						meta: values['meta'],
						scopeCd: values['scopeCd']
					});
					return newRec;
				}
				
				,buildNewRecordToSave: function(values){
					var newRec = new Ext.data.Record({
						id: null,
						name : values['name'],
						label : '...',
						usedByNDocs: 0,
						dsVersions: [],
						description : values['description'],
						dsTypeCd : values['dsTypeCd'],
						catTypeVn : values['catTypeVn'],
						isPublic: values['isPublic'],
						usedByNDocs : values['usedByNDocs'],
						fileName : values['fileName'],
						csvDelimiter : values['csvDelimiter'],
						skipRows : values['skipRows'],
						limitRows : values['limitRows'],
						xslSheetNumber : values['xslSheetNumber'],						
						fileType : values['fileType'],
						csvQuote : values['csvQuote'],
						query : values['query'],
						queryScript : values['queryScript'],
						queryScriptLanguage : values['queryScriptLanguage'],
						dataSource : values['dataSource'],
						wsAddress : values['wsAddress'],
						wsOperation : values['wsOperation'],
						script : values['script'],
						scriptLanguage : values['scriptLanguage'],
						customData : values['customData'],
						jclassName : values['jclassName'],
						jclassNameForCustom : values['jclassNameForCustom'],
						trasfTypeCd : values['trasfTypeCd'],
						pivotColName : values['pivotColName'],
						pivotColValue : values['pivotColValue'],
						pivotRowName : values['pivotRowName'],
						pivotIsNumRows : values['pivotIsNumRows'],
						isPersisted: values['isPersisted'],
						persistTableName: values['persistTableName'],
						dataSourceFlat: values['dataSourceFlat'],
						flatTableName: values['flatTableName'],
						qbeSQLQuery : values['qbeSQLQuery'],
						qbeJSONQuery : values['qbeJSONQuery'],
						qbeDataSource: values['qbeDataSource'],
						qbeDatamarts: values['qbeDatamarts'],
						userIn: values['userIn'],
						dateIn: values['dateIn'],
						versNum: values['versNum'],
						versId: values['versId'],
						meta: values['meta'],
						scopeCd: values['scopeCd']
					});
					return newRec;
				}	
				
				,buildParamsToSendToServer: function(values){
					var params = {
							name : values['name'],
							label : values['label'],
							description : values['description'],
							dsTypeCd : values['dsTypeCd'],
							catTypeVn : values['catTypeVn'],
							isPublic: values['isPublic'],
							usedByNDocs : values['usedByNDocs'],
							fileName : values['fileName'],
							csvDelimiter : values['csvDelimiter'],
							skipRows : values['skipRows'],
							limitRows : values['limitRows'],
							xslSheetNumber : values['xslSheetNumber'],																				
							fileType : values['fileType'],
							csvQuote : values['csvQuote'],
							query : values['query'],
							queryScript : values['queryScript'],
							queryScriptLanguage : values['queryScriptLanguage'],
							dataSource : values['dataSource'],
							wsAddress : values['wsAddress'],
							wsOperation : values['wsOperation'],
							script : values['script'],
							scriptLanguage : values['scriptLanguage'],
							jclassName : values['jclassName'],
							customData : values['customData'],
							trasfTypeCd : values['trasfTypeCd'],
							pivotColName : values['pivotColName'],
							pivotColValue : values['pivotColValue'],
							pivotRowName : values['pivotRowName'],
							pivotIsNumRows : values['pivotIsNumRows'],
							isPersisted: values['isPersisted'],
							persistTableName: values['persistTableName'],
							dataSourceFlat: values['dataSourceFlat'],
							flatTableName: values['flatTableName'],
							qbeSQLQuery : values['qbeSQLQuery'],
							qbeJSONQuery : values['qbeJSONQuery'],
							qbeDataSource: values['qbeDataSource'],
							qbeDatamarts: values['qbeDatamarts'],
							userIn: values['userIn'],
							dateIn: values['dateIn'],
							versNum: values['versNum'],
							versId: values['versId'],
							meta: values['meta'],
							fileUploaded : thisPanel.fileUploaded,
							scopeCd: values['scopeCd'],
						};
					return params;
				}
				
				,updateNewRecord: function(record, values, arrayPars, meta, customString){
					record.set('label',values['label']);
					record.set('name',values['name']);
					record.set('description',values['description']);
					record.set('usedByNDocs',0);
					record.set('dsTypeCd',values['dsTypeCd']);
					record.set('catTypeVn',values['catTypeVn']);
					record.set('isPublic',values['isPublic']);
					record.set('fileName',values['fileName']);
					record.set('csvDelimiter',values['csvDelimiter']);		
					record.set('skipRows',values['skipRows']);					
					record.set('limitRows',values['limitRows']);					
					record.set('xslSheetNumber',values['xslSheetNumber']);					
					record.set('fileType',values['fileType']);			
					record.set('csvQuote',values['csvQuote']);					
					record.set('query',values['query']);
					record.set('queryScript',values['queryScript']);
					record.set('queryScriptLanguage',values['queryScriptLanguage']);
					record.set('dataSource',values['dataSource']);
					record.set('wsAddress',values['wsAddress']);
					record.set('wsOperation',values['wsOperation']);
					record.set('script',values['script']);
					record.set('scriptLanguage',values['scriptLanguage']);
					record.set('jclassName',values['jclassName']);
					record.set('jclassNameForCustom',values['jclassName']);
					record.set('customData',values['customData']);					
					record.set('trasfTypeCd',values['trasfTypeCd']);
					record.set('pivotColName',values['pivotColName']);
					record.set('pivotColValue',values['pivotColValue']);
					record.set('pivotRowName',values['pivotRowName']);
					record.set('pivotIsNumRows',values['pivotIsNumRows']);
					record.set('isPersisted', values['isPersisted']),
					record.set('persistTableName', values['persistTableName']),
					record.set('dataSourceFlat', values['dataSourceFlat']),
					record.set('flatTableName', values['flatTableName']),					
					record.set('qbeSQLQuery',values['qbeSQLQuery']);
					record.set('qbeJSONQuery',values['qbeJSONQuery']);
					record.set('qbeDataSource',values['qbeDataSource']);
					record.set('qbeDatamarts',values['qbeDatamarts']);
					record.set('userIn',values['userIn']);
					record.set('dateIn',values['dateIn']);
					record.set('versNum',values['versNum']);
					record.set('versId',values['versId']);
					record.set('scopeCd',values['scopeCd']);
					
					if (arrayPars) {
						record.set('pars',arrayPars);
					}
					if (arrayPars) {
						record.set('pars',arrayPars);
					}
					if (customString) {
						record.set('customData',customString);
					}
					if (meta) {
						record.set('meta',meta);
					}


					
				}
				
				, updateMainStore: function(idRec){
					var values = this.getValues();
					var record;
					var length = this.mainElementsStore.getCount();
					for(var i=0;i<length;i++){
			   	        var tempRecord = this.mainElementsStore.getAt(i);
			   	        if(tempRecord.data.id==idRec){
			   	        	record = tempRecord;
						}			   
			   	    }	
					var params = this.buildParamsToSendToServer(values);
					var arrayPars = this.manageParsGrid.getParsArray();
					var meta = this.manageDatasetFieldMetadataGrid.getValues()
					this.updateNewRecord(record,values,arrayPars, meta);
					this.mainElementsStore.commitChanges();
				}
				
				, updateDsVersionsOfMainStore: function(idRec){
					var arrayVersions = this.manageDsVersionsGrid.getCurrentDsVersions();
					if (arrayVersions) {
						var record;
						var length = this.mainElementsStore.getCount();
						for(var i=0;i<length;i++){
				   	        var tempRecord = this.mainElementsStore.getAt(i);
				   	        if(tempRecord.data.id==idRec){
				   	        	record = tempRecord;
							}			   
				   	    }	
						record.set('dsVersions',arrayVersions);
						this.mainElementsStore.commitChanges();			
					}
				}
				
				, getValues: function() {
					var values = this.getForm().getFieldValues();
					
					// in the refactored architecture all detail parts will be
					// implemented as external widgets. The state of each widget
					// will be read using the method getFormState. QueryDetail is
					// the first refactored part.
					values = Ext.apply(values, this.queryDetail.getFormState());
					// ----------------------------------------------------------
					
					if(this.customDataDetail.isVisible()){
						values.jclassName = values.jclassNameForCustom;
					}
					
					values.fileUploaded = thisPanel.fileUploaded;
					
					return values;
					
				}
				
				, setValues: function(record) {
					record.data.jclassNameForCustom = record.data.jclassName;
					this.getForm().loadRecord(record);
					
					// in the refactored architecture all detail parts will be
					// implemented as external widgets. The state of each widget
					// will be set using the method setFormState. QueryDetail is
					// the first refactored part.
					this.queryDetail.setFormState(record.data);
					// ----------------------------------------------------------
					this.fileUploadFormPanel.setFormState(record.data);
				}
				
				// OVERRIDING save method
				, save : function () {
					var values = this.getValues();
					var idRec = values['id'];
					if (idRec == 0 || idRec == null || idRec === '') {
						this.doSave("yes");
					} else {
						Ext.MessageBox.confirm(
							LN('sbi.ds.recalculatemetadataconfirm.title')
							, LN('sbi.ds.recalculatemetadataconfirm.msg')
							, this.doSave
							, this
						);
					}
				}
				/**
				 * Opens the loading mask 
				 */
			    , showMask : function(){  
			    	if (this.loadMask == null) {
			    		this.loadMask = new Ext.LoadMask(this, {msg: "Loading..."});    		
			    	}
			    	this.loadMask.show();
			    }

				/**
				 * Closes the loading mask
				*/
				, hideMask: function() {
			    	if (this.loadMask != null) {	
			    		this.loadMask.hide();
			    	}
				} 
				, doSave : function(recalculateMetadata) {
					var values = this.getValues();
					
					var idRec = values['id'];
					var newRec;
					var newDsVersion;
					var isNewRec = false;
					var params = this.buildParamsToSendToServer(values);
					params.dsId = idRec;
					params.recalculateMetadata = recalculateMetadata;
					var arrayPars = this.manageParsGrid.getParsArray();
					var customString = this.customDataGrid.getDataString();
					var meta = this.manageDatasetFieldMetadataGrid.getValues()
					if (idRec == 0 || idRec == null || idRec === '') {
						this.updateNewRecord(this.newRecord,values,arrayPars,meta, customString);
						isNewRec = true;
					}else{
						var record;
						var oldType;
						var length = this.mainElementsStore.getCount();
						for(var i=0;i<length;i++){
				   	        var tempRecord = this.mainElementsStore.getAt(i);
				   	        if(tempRecord.data.id==idRec){
				   	        	record = tempRecord;
				   	        	oldType = record.get('dsTypeCd');
							}			   
				   	    }	
						this.updateNewRecord(record,values,arrayPars,meta, customString);
						
						newDsVersion = new Ext.data.Record(
								{	dsId: values['id'],
									dateIn : values['dateIn'],
									userIn : values['userIn'],
									versId : values['versId'],
									type : oldType, 
									versNum : values['versNum']
								});
					}
					
					if (arrayPars) {
						params.pars = Ext.util.JSON.encode(arrayPars);
					}
					if (customString) {
						params.customData = Ext.util.JSON.encode(customString);
					}
					if (this.manageDatasetFieldMetadataGrid) {
						params.meta = Ext.util.JSON.encode(this.manageDatasetFieldMetadataGrid.getValues());
					}
					

					
					if (idRec) {
						params.id = idRec;
					}

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
											//	if (mask != undefined) mask.hide();
												Ext.MessageBox
														.show({
															title : LN('sbi.generic.error'),
															msg : content,
															width : 150,
															buttons : Ext.MessageBox.OK
														});
											} else {
												var itemId = content.id;
												var dateIn = content.dateIn;
												var userIn = content.userIn;
												var versId = content.versId;
												var versNum = content.versNum;
												var meta = content.meta;
												
												//update metadata
												var length = this.mainElementsStore.getCount();
												for(var i=0;i<length;i++){
										   	        var tempRecord = this.mainElementsStore.getAt(i);
										   	        if(tempRecord.data.id==itemId){
										   	        	tempRecord.set('meta',meta);
										   	        	
										   	        	var dsType = tempRecord.get('dsTypeCd');
										   	        	//update filename if the dataset is a FileDataset
										   	        	if ((dsType != undefined) && (dsType =='File')){
										   	        		var fileType = tempRecord.get('fileType');
										   	        		fileType = fileType.toLowerCase();
										   	        		var dsLabel = tempRecord.get('label');
										   	        		var updatedFileName = dsLabel + '.' +fileType
										   	        		tempRecord.set('fileName',updatedFileName); //update record in JsonStore
															Ext.getCmp('fileNameField').setValue(updatedFileName); //update field in GUI

										   	        	}
										   	        	
										   	        	tempRecord.commit();
										   	        	this.manageDatasetFieldMetadataGrid.loadItems(meta,tempRecord);
										   	        	break;
													}			   
										   	    }
												
												
//												var newRecord = this.mainElementsStore.getAt(this.mainElementsStore.getCount()-1);
//												newRecord.data.meta = meta;
//												newRecord.commit();
												if (isNewRec
														&& itemId != null
														&& itemId !== '') {
		
													var record;
													
													for(var i=0;i<length;i++){
											   	        var tempRecord = this.mainElementsStore.getAt(i);
											   	       
											   	        this.rowselModel.selectLastRow(true);
											   	        if(!tempRecord.data.id || tempRecord.data.id==0){
											   	        	tempRecord.set('id',itemId);
											   	        	tempRecord.set('dateIn',dateIn);
											   	        	tempRecord.set('userIn',userIn);
											   	        	tempRecord.set('versId',versId);
											   	        	tempRecord.set('versNum',versNum);
											   	        	tempRecord.set('meta',meta);
											   	        	//update filename if the dataset is a FileDataset
											   	        	var dsType = tempRecord.get('dsTypeCd');
											   	        	if ((dsType != undefined) && (dsType =='File')){
											   	        		var fileType = tempRecord.get('fileType');
											   	        		fileType = fileType.toLowerCase();
											   	        		var dsLabel = tempRecord.get('label');
											   	        		var updatedFileName = dsLabel + '.' +fileType
											   	        		tempRecord.set('fileName',updatedFileName); //update record in JsonStore
																Ext.getCmp('fileNameField').setValue(updatedFileName); //update field in GUI

											   	        	}
											   	        	tempRecord.commit();
											   	        	this.detailFieldId.setValue(itemId);
											   	        	this.detailFieldUserIn.setValue(userIn);
											   	        	this.detailFieldDateIn.setValue(dateIn);
											   	        	this.detailFieldVersNum.setValue(versNum);
											   	        	this.detailFieldVersId.setValue(versId);
														}			   
											   	    }
												}else{
													if(newDsVersion!= null && newDsVersion != undefined){
														this.manageDsVersionsGrid.getStore().addSorted(newDsVersion);
														this.manageDsVersionsGrid.getStore().commitChanges();
														var values = this.getValues();
														this.updateDsVersionsOfMainStore(values['id']);
													}
												}
												this.mainElementsStore.commitChanges();
												if (isNewRec) {
													this.rowselModel.selectLastRow();
												}
												thisPanel.fileUploaded = false //reset to default
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
				manageQbeQuery : function(theQbeDatasetBuilder, qbeQuery) {
					var jsonQuery = qbeQuery.data.jsonQuery;
					this.qbeJSONQuery.setValue(Ext.util.JSON.encode(jsonQuery));
					var sqlQuery = qbeQuery.data.sqlQuery.sql;
					this.qbeSQLQuery.setValue(sqlQuery);
					var datamarts = qbeQuery.data.datamarts;
					this.qbeDatamarts.setValue(datamarts);
					var parameters = qbeQuery.data.parameters;
					this.manageParsGrid.loadItems([]);
					this.manageParsGrid.loadItems(parameters);
				}
				
				,
				jsonTriggerFieldHandler : function() {
					//alert("jsonTriggerFieldHandler");
					var values = this.getValues();
					var datasetId = values['id'];
					var datasourceLabel = this.detailQbeDataSource.getValue();
					if (datasourceLabel == '') {
						Ext.MessageBox.show({
							title : LN('sbi.generic.error'),
							msg : LN('sbi.tools.managedatasets.errors.missingdatasource'),
							width : 150,
							buttons : Ext.MessageBox.OK
						});
						return;
					}
					if (datamart == '') {
						Ext.MessageBox.show({
							title : LN('sbi.generic.error'),
							msg : LN('sbi.tools.managedatasets.errors.missingdatamart'),
							width : 150,
							buttons : Ext.MessageBox.OK
						});
						return;
					}
					var datamart = this.qbeDatamarts.getValue();
					this.initQbeDataSetBuilder(datasourceLabel, datamart, datasetId);
					this.qbeDataSetBuilder.show();
				}
				
				, initQbeDataSetBuilder: function(datasourceLabel, datamart, datasetId) {
					if (this.qbeDataSetBuilder === null) {
						this.initNewQbeDataSetBuilder(datasourceLabel, datamart, datasetId);
						return;
					}
					if (this.mustRefreshQbeView(datasourceLabel, datamart, datasetId)) {
						this.qbeDataSetBuilder.destroy();
						this.initNewQbeDataSetBuilder(datasourceLabel, datamart, datasetId);
						return;
					}
				}
				
				, initNewQbeDataSetBuilder: function(datasourceLabel, datamart, datasetId) {
					this.qbeDataSetBuilder = new Sbi.tools.dataset.QbeDatasetBuilder({
						datasourceLabel : datasourceLabel,
						datamart : datamart,
						datasetId : datasetId,
						jsonQuery : this.qbeJSONQuery.getValue(),
						qbeParameters : this.manageParsGrid.getParsArray(),
						modal : true,
						width : this.getWidth() - 50,
						height : this.getHeight() - 50,
						listeners : {
							hide : 
								{
									fn : function(theQbeDatasetBuilder) {
											theQbeDatasetBuilder.getQbeQuery(); // asynchronous
									}
									, scope: this
								},
							gotqbequery : {
									fn: this.manageQbeQuery
									, scope: this
							}
						}
					});
				}
				
				, mustRefreshQbeView: function(datasourceLabel, datamart, datasetId) {
					if (datasourceLabel == this.qbeDataSetBuilder.getDatasourceLabel()
							&& datamart == this.qbeDataSetBuilder.getDatamart()
							&& datasetId == this.qbeDataSetBuilder.getDatasetId()) {
						return false;
					}
					return true;
				}
				
				//METHOD TO BE OVERRIDDEN IN EXTENDED ELEMENT!!!!!
				,info : function() {		
					var win_info_2;
					if(!win_info_2){
						win_info_2= new Ext.Window({
							id:'win_info_2',
							autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/dsrules.html'},             				
							layout:'fit',
							width:620,
							height:410,
							autoScroll: true,
							closeAction:'close',
							buttonAlign : 'left',
							plain: true,
							title: LN('sbi.ds.help')
						});
					};
					win_info_2.show();
			    }
				
				,transfInfo: function() {		
					var win_info_4;
					if(!win_info_4){
						win_info_4= new Ext.Window({
							id:'win_info_4',
							autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/dsTrasformationHelp.html'},             				
							layout:'fit',
							width:760,
							height:420,
							autoScroll: true,
							closeAction:'close',
							buttonAlign : 'left',
							plain: true,
							title: LN('sbi.ds.help')
						});
					};
					win_info_4.show();
			    }
				
				,persistInfo: function() {		
					var win_info_5;
					if(!win_info_5){
						win_info_5= new Ext.Window({
							id:'win_persist_5',
							autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/dsPersistenceHelp.html'},             				
							layout:'fit',
							width:760,
							height:420,
							autoScroll: true,
							closeAction:'close',
							buttonAlign : 'left',
							plain: true,
							title: LN('sbi.ds.help')
						});
					};
					win_info_5.show();
			    }
				
				,fieldsMetadata: function() {		
				
					if(!this.win_info_metadata){
						this.win_info_metadata= new Ext.Window({     				
							layout:'fit',
							width:370,
							height:350,
							closeAction:'hide',
							autoScroll: false,
							title: LN('sbi.ds.metadata'),
							items: [this.manageDatasetFieldMetadataGrid]
							,buttonAlign : 'right',
						    buttons: [{
								text: LN('sbi.general.ok'),
							    handler: function(){
							    	this.manageDatasetFieldMetadataGrid.updateRecord();
							    	this.win_info_metadata.hide();
							    }
						       	, scope: this
							}]
						});
					};
					if(this.manageDatasetFieldMetadataGrid.emptyStore){
						Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.ds.field.metadata.nosaved"));
					}else{
						this.win_info_metadata.show();
					}
					
			    }
				
				,profileAttrs: function() {		
					var win_info_3;
					if(!win_info_3){
						win_info_3= new Ext.Window({
							id:'win_info_3',          				
							layout:'fit',
							width:220,
							height:350,
							closeAction:'close',
							buttonAlign : 'left',
							autoScroll: true,
							plain: true,
							items: {  
						        xtype: 'grid',
						        border: false,
						        columns: [{header: LN('sbi.ds.pars'),width : 170}],                
						        store: this.profileAttributesStore
						    }
						});
					};
					win_info_3.show();
			    }
				
				/**
				 * Opens the loading mask 
				*/
			    , showMask : function(){
			    	this.un('afterlayout',this.showMask,this);
			    	if (this.loadMask == null) {    		
			    		this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "Loading...  "});
			    	}
			    	if (this.loadMask){
			    		this.loadMask.show();
			    	}
			    }
 
				/**
				 * Closes the loading mask
				*/
				, hideMask: function() {
			    	if (this.loadMask && this.loadMask != null) {	
			    		this.loadMask.hide();
			    	}
				} 
});
