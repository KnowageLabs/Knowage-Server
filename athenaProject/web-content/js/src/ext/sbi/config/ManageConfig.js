/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * Object name
 * 
 * ManageConfig
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
 * Authors
 * 
 * Monia Spinelli (monia.spinelli@eng.it)
 */

Ext.ns("Sbi.config");

Sbi.config.ManageConfig = function(config) {
	
	var c = Ext.apply( {
		title : 'Config',
		layout : 'fit'
	}, config || {});

	var paramsList = {
		MESSAGE_DET : "CONFIG_LIST"
	};
	var paramsSave = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "CONFIG_SAVE"
	};
	var paramsDel = {
		LIGHT_NAVIGATOR_DISABLED : 'TRUE',
		MESSAGE_DET : "CONFIG_DELETE"
	};

	this.crudServices = {};

	this.crudServices['manageListService'] = Sbi.config.serviceRegistry
			.getServiceUrl( {
				serviceName : 'CONFIG_ACTION',
				baseParams : paramsList
			});
	this.crudServices['saveItemService'] = Sbi.config.serviceRegistry
			.getServiceUrl( {
				serviceName : 'CONFIG_ACTION',
				baseParams : paramsSave
			});
	this.crudServices['deleteItemService'] = Sbi.config.serviceRegistry
			.getServiceUrl( {
				serviceName : 'CONFIG_ACTION',
				baseParams : paramsDel
			});

	this.initGrid();

	c.items = [ this.grid ];

	// constructor
	Sbi.config.ManageConfig.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.config.ManageConfig, Ext.Panel, {

	grid : null,
	columnModel : null,
	categorySelected : false,
	storeMain : null,
	gridToolbar : null,
	Record : null,
	RecordDistinct :  Ext.data.Record.create([
	                                            {name: 'category', type: 'string'}
	                                         ]),
	editor : null,
	chooseCategoryCombo : null,
	filterLabelTextField : null,
	filterNameTextField : null

	// public methods
	,
	initGrid : function() {

		this.editor = new Ext.ux.grid.RowEditor( {
			saveText : 'Update',
			listeners : {
				afteredit : {
					fn : this.saveConfig,
					scope : this
				}
			}

		});
		
		 

		this.initStore();
		this.storeMain.load({});
		
		this.store =  new Ext.data.ArrayStore({
	         fields: ['value', 'description']
	    });
		this.storeMain.on('load', this.initFilterStore, this);
		//this.store.load(data);
		this.initColumnModel();
		this.initToolbar();
		this.grid = new Ext.grid.GridPanel( {
			store : this.storeMain,
			cm : this.columnModel,
			tbar : this.gridToolbar,
			sm : new Ext.grid.RowSelectionModel( {
				singleSelect : true
			}),
			plugins : [ this.editor ],
			// width: 600,
			height : 300,
			margins : '0 5 5 5',
			viewConfig : {
				forceFit : true
			}
		});
	}

	,
	initToolbar : function() {
		
		this.chooseCategoryCombo = new Ext.form.ComboBox({
			text: 'Category',
			emptyText : LN('sbi.config.manageconfig.fields.selectCategory'),
			displayField:'description',
			valueField: 'value',
			store: this.store,
			mode: 'local',
			typeAhead: true,
			triggerAction: 'all',			         
			listeners: {
				select: function(){
					this.categorySelected = true;
					this.filterMainStore();
				},
				scope: this
			}
		});
		
		this.filterLabelTextField = new Ext.form.TextField({
			emptyText: LN('sbi.config.manageconfig.fields.searchLabel'),
			enableKeyEvents : true,
			listeners : {
				keyup : this.filterMainStore,
				scope: this
			 }
		});
		
		this.filterNameTextField = new Ext.form.TextField({
			emptyText: LN('sbi.config.manageconfig.fields.searchName'),
			enableKeyEvents : true,
			listeners : {
				keyup : this.filterMainStore,
				scope: this
			}
		});
		
		this.gridToolbar = new Ext.Toolbar( [ 
		{
			iconCls : 'icon-domain-add',
			text : LN('sbi.generic.add'),
			handler : function() {
				var record = new this.Record();
				record.set('IS_ACTIVE', 'true');
				record.set('VALUE_TYPE', 'STRING');
				this.editor.stopEditing();
				this.storeMain.insert(0, record);
				this.grid.getView().refresh();
				this.grid.getSelectionModel().selectRow(0);
				this.editor.startEditing(0);
			},
			scope : this
		}, {
			iconCls : 'icon-domain-delete',
			text : LN('sbi.generic.delete'),
			// disabled: true,
			handler : function() {
				this.editor.stopEditing();
				var s = this.grid.getSelectionModel().getSelections();
				for ( var i = 0, r; r = s[i]; i++) {
					var id = r.get('ID');
					if(id != undefined && id != null){
						var params = {
								ID: r.get('ID')
						};

						Ext.Ajax.request( {
							url : this.crudServices['deleteItemService'],
							params : params,
							// method: 'GET',
							success : function(response, options) {
							 	response = Ext.util.JSON.decode( response.responseText );
							    var index = this.storeMain.find( "ID", response.ID );
							    var record =  this.storeMain.getAt(  index ) ;
							    if(record) {
							    	this.storeMain.remove(record);
									Ext.MessageBox.show({
							            title: LN('sbi.generic.info'),
							            msg: LN('sbi.config.manageconfig.delete'),
							            modal: false,
							            buttons: Ext.MessageBox.OK,
							            width:300,
							            icon: Ext.MessageBox.INFO,
							            animEl: 'root-menu'           
							           });
								}
							},
							failure : Sbi.exception.ExceptionHandler.handleFailure,
							scope : this
						});
					}
					else{
						this.storeMain.remove(r);
					}
				}
			},
			scope : this
		}
		,'->'
		,{xtype: 'tbtext', text: LN('sbi.config.manageconfig.fields.selectCategory')}
		, this.chooseCategoryCombo
		,{xtype: 'tbtext', text: LN('sbi.config.manageconfig.fields.label'), style: {'padding-left': 20}}
		, this.filterLabelTextField
		,{xtype: 'tbtext', text: LN('sbi.config.manageconfig.fields.name'), style: {'padding-left': 20}}
		, this.filterNameTextField
		,{
			xtype: 'tbspacer', width: 30
		}
		, {
			iconCls : 'icon-clear',
			handler : this.clearFilterForm,
			scope : this,
			tooltip: LN('sbi.config.manageconfig.fields.clearFilter')
		}
		])
	}
	
	,
	clearFilterForm : function () {
		this.chooseCategoryCombo.clearValue();
		this.categorySelected = false;
		this.filterLabelTextField.setValue('');
		this.filterNameTextField.setValue('');
		this.storeMain.clearFilter(false);
	}
	
	,
	filterMainStore : function () {
		var category = this.chooseCategoryCombo.getValue();
		var label = this.filterLabelTextField.getValue().toUpperCase();
		var name = this.filterNameTextField.getValue().toUpperCase();
		var categorySelected = this.categorySelected;
		var filterFunction = function (record, recordId) {
			var recordStartLabel = record.data.LABEL.substring(0, label.length).toUpperCase();
			var recordStartName = record.data.NAME.substring(0, name.length).toUpperCase();
			var recordCategory = record.data.CATEGORY;
			return recordStartLabel == label && recordStartName == name && (!categorySelected || recordCategory == category);
		};
		this.storeMain.filterBy(filterFunction);
	}
	
	,
	initColumnModel : function() {
		this.columnModel = new Ext.grid.ColumnModel( [ {
			header : LN('sbi.config.manageconfig.fields.label'),
			dataIndex : 'LABEL',
			// width: 220,
			sortable : true,
			editor : {
				xtype : 'textfield',
				 allowBlank : false,
				 maxLength : 100,
				 maxLengthText :
				 LN('sbi.config.manageconfig.validation.maxlengthtext')
			}
		}, {
			header : LN('sbi.config.manageconfig.fields.name'),
			dataIndex : 'NAME',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 100,
				maxLengthText : LN('sbi.config.manageconfig.validation.maxlengthtext')
			}
		}, {
			header : LN('sbi.config.manageconfig.fields.description'),
			dataIndex : 'DESCRIPTION',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 500,
				maxLengthText : LN('sbi.config.manageconfig.validation.maxlengthtext')
			}
		}, {
			xtype: 'booleancolumn',
			header : LN('sbi.config.manageconfig.fields.isactive'),
			dataIndex : 'IS_ACTIVE',
			trueText: LN('sbi.general.yes'),
            falseText: LN('sbi.general.No'),
			editor : {
				xtype : 'checkbox'
			}
		}, {
			header : LN('sbi.config.manageconfig.fields.valuecheck'),
			dataIndex : 'VALUE_CHECK',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 1000,
				maxLengthText : LN('sbi.config.manageconfig.validation.maxlengthtext')
			}
		},{
			header : LN('sbi.config.manageconfig.fields.valuetype'),
			dataIndex : 'VALUE_TYPE',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 11,
				maxLengthText : LN('sbi.config.manageconfig.validation.maxlengthtext')
			}
		},{
			header : LN('sbi.config.manageconfig.fields.category'),
			dataIndex : 'CATEGORY',
			// width: 150,
			sortable : true,
			editor : {
				xtype : 'textfield',
				allowBlank : false,
				maxLength : 50,
				maxLengthText : LN('sbi.config.manageconfig.validation.maxlengthtext')
			}
		}]);
	}

	,
	initStore : function() {

		var fields = [ {
			name : 'ID'
		}, {
			name : 'LABEL'
		}, {
			name : 'NAME'
		}, {
			name : 'DESCRIPTION'
		}, {
			name : 'IS_ACTIVE'
		}, {
			name : 'VALUE_CHECK'
		}, {
			name : 'VALUE_TYPE'
		}, {
			name : 'CATEGORY'
		} ];

		this.storeMain = new Ext.data.JsonStore( {

			root : 'response',
			idProperty : 'ID',
			fields : fields,
			url : this.crudServices['manageListService']

		});
		this.Record = Ext.data.Record.create(fields);
	}

	,
	saveConfig : function(rowEditor, obj, record, rowIndex) {

		var p = Ext.apply({},record.data);
		if (record.get('ID') == undefined || record.get('ID') == null || record.get('ID') == '') {
			delete p.ID;
		}
		if (record.get('VALUE_TYPE') == undefined || record.get('VALUE_TYPE') == null || record.get('VALUE_TYPE') == '') {
			delete p.VALUE_TYPE;
		}

		Ext.Ajax.request( {
			url : this.crudServices['saveItemService'],
			params : p,
			method : 'POST',
			success : this.successSave.createDelegate(this, [record], true),
			failure : Sbi.exception.ExceptionHandler.handleFailure,
			scope : this
		});
	}
	,successSave : function(response, options, record) {
		var jsonResponse = Ext.util.JSON.decode(response.responseText);
		record.set('ID', jsonResponse.ID);
		record.commit();
		
		this.store.removeAll(false);
		this.initFilterStore();
		
		Ext.MessageBox.show({
            title: LN('sbi.generic.info'),
            msg: LN('sbi.config.manageconfig.save'),
            modal: false,
            buttons: Ext.MessageBox.OK,
            width:300,
            icon: Ext.MessageBox.INFO,
            animEl: 'root-menu'           
           });
	}	

	, initFilterStore: function() {
		 var distinctValues; 
		 var obj;
		 var record;
		 
		 distinctValues = this.storeMain.collect("CATEGORY", true, true);
		 
	     for (var i = 0, l = distinctValues.length; i < l; i++) {	
	    	 var desc = distinctValues[i] === ''? 'NULL': distinctValues[i];
	    	 obj = { description : desc, value : distinctValues[i]};
	    	 record = new this.RecordDistinct(obj);
	    	 this.store.add(record);
	   	 }
	     
	     
	}
});