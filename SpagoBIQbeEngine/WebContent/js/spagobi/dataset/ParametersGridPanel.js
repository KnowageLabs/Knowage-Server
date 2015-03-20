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
 * Authors - Chiara Chiarelli
 */
Ext.ns("Sbi.dataset");

Sbi.dataset.ParametersGridPanel = function(config) {

	var defaultSettings = {
			title : LN('sbi.dataset.parametersgridpanel.title')
	};
	 
	if(Sbi.settings && Sbi.settings.dataset && Sbi.settings.dataset.parametersgridpanel) {
	   defaultSettings = Ext.apply(defaultSettings, Sbi.settings.dataset.parametersgridpanel);
	}
	 
	var c = Ext.apply(defaultSettings, config || {});
	 
	Ext.apply(this, c);
	
	this.init();
	
	var c = Ext.apply({}, config, this.gridConfig);

	// constructor
	Sbi.dataset.ParametersGridPanel.superclass.constructor.call(this, c);

	this.on('beforeedit', function(e) {
		var t = Ext.apply({}, e);
		var col = t.column;
		this.currentRowRecordEdited = t.row;
	}, this);

	this.on('afteredit', function(e) {
		var col = e.column;
		var row = e.row;
	}, this);

};

/**
 * @class Sbi.dataset.ParametersGridPanel
 * @extends Ext.grid.EditorGridPanel
 * 
 * ParametersGridPanel
 */
Ext.extend(Sbi.dataset.ParametersGridPanel, Ext.grid.EditorGridPanel, {

	currentRowRecordEdited : null
	, store : null
	, userColumns : null
	, type: 'parametersgrid'

	,
	loadItems : function(pars) {
		this.store.loadData(pars);
	}

	,
	onAdd : function(btn, ev) {
		var emptyRecToAdd = new Ext.data.Record({
			id : 0,
			type : 'String'
		});
		this.store.insert(0, emptyRecToAdd);
	}

	,
	onDelete : function() {
		var rec = this.getSelectionModel().getSelected();
		this.store.remove(rec);
		this.store.commitChanges();
	}

	,
	getParameters : function() {
		var arrayPars = new Array();
		var storePars = this.getStore();
		var length = storePars.getCount();
		for ( var i = 0; i < length; i++) {
			var item = storePars.getAt(i);
			var data = item.data;
			arrayPars.push(data);
		}
		return arrayPars;
	}
	
	,
	init : function() {
		this.typesStore = new Ext.data.SimpleStore({
			fields : [ 'type' ],
			data : [ [ 'String' ], [ 'Number' ], [ 'Raw' ], [ 'Generic' ] ],
			autoLoad : false
		});

		// Let's pretend we rendered our grid-columns with meta-data from our ORM framework.
		//these are grid values for range type threshold value
		this.userColumns = [ {
			header : LN('sbi.dataset.parametersgridpanel.columns.name'),
			width : 110,
			id : 'name',
			sortable : true,
			dataIndex : 'name',
			editor : new Ext.form.TextField({
				maxLength : 20,
				allowBlank : false,
				validationEvent : true
			})
		}, {
			header : LN('sbi.dataset.parametersgridpanel.columns.type'),
			width : 90,
			id : 'type',
			sortable : true,
			dataIndex : 'type',
			editor : new Ext.form.ComboBox({
				name : 'type',
				store : this.typesStore,
				displayField : 'type', // what the user sees in the popup
				valueField : 'type', // what is passed to the 'change' event
				typeAhead : true,
				forceSelection : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				editable : false,
				allowBlank : false,
				validationEvent : true
			})
		} ];

		var cm = new Ext.grid.ColumnModel({
			columns : this.userColumns
		});

		this.store = new Ext.data.JsonStore({
			//id : 'id',
			fields : [ 'name', 'type' ],
			idIndex : 0,
			data : {}
		});

		var tb = new Ext.Toolbar({
			buttonAlign : 'left',
			items : [ new Ext.Toolbar.Button({
				text : LN('sbi.dataset.parametersgridpanel.buttons.add'),
				iconCls : 'add',
				handler : this.onAdd,
				width : 30,
				scope : this
			}), '-', new Ext.Toolbar.Button({
				text : LN('sbi.dataset.parametersgridpanel.buttons.remove'),
				iconCls : 'remove',
				handler : this.onDelete,
				width : 30,
				scope : this
			}), '-' ]
		});

		var sm = new Ext.grid.RowSelectionModel({
			singleSelect : true
		});
		
		this.gridConfig = {
			xtype : 'grid',
			store : this.store,
			cm : cm,
			sm : sm,
			frame : true,
			clicksToEdit : 2,
			tbar : tb,
			viewConfig: {
	        	forceFit: true
			},
	        enableDragDrop: true,
   	        ddGroup: 'gridDDGroup'
		};
	}

});
