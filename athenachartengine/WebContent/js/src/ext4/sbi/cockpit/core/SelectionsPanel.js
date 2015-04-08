/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.cockpit.core.SelectionsPanel', {
	extend: 'Ext.Panel'
	, layout:'fit'
	, border: false
	, config: {
		  gridConfig: {}
		, grid: null
		, gridHeader: null
		, gridColumns: null
		, store: null
		, widgetManager: null
		, showByAssociation: true
		, showGridHeader: true
	}

	, constructor : function(config) {
		Sbi.trace("[SelectionsPanel.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.initEvents();
		this.callParent(arguments);

		this.widgetManager.on('selectionChange', this.onSelectionChange,this);

		Sbi.trace("[SelectionsPanel.constructor]: OUT");
	}


	, initComponent: function() {

        Ext.apply(this, {
            items: [this.grid]
        });

        this.callParent();
    }

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getFieldValues: function(values){
		var toReturn = "";
		var comma = "";
		for (var i=0; i< values.length; i++){
			toReturn += comma + values[i];
			if (comma == "") comma = ", ";
		}
		return toReturn;
	}

	, refreshStore: function() {
		Sbi.trace("[SelectionsPanel.refreshStore]: IN");

		var data = null;

		if(this.showByAssociation === true) {
			data = this.initStoreDataByAssociation();
		} else {
			data = this.initStoreDataByWidget();
		}

		this.store.loadData(data);

		Sbi.trace("[SelectionsPanel.refreshStore]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[SelectionsPanel.init]: IN");
		this.initStore();
		this.initGrid();
		Sbi.trace("[SelectionsPanel.init]: OUT");
	}

	, initStore: function() {
		Sbi.trace("[SelectionsPanel.initStore]: IN");

		if(this.showByAssociation === true) {
			var data = this.initStoreDataByAssociation();

			this.store = new Ext.data.ArrayStore({
				fields: ['widget', 'association', 'values']
				, data: data
			});

		} else {
			var data = this.initStoreDataByWidget();
			this.store = new Ext.data.ArrayStore({
				fields: ['widget', 'field', 'values']
				, groupField: 'widget'
				, data: data
			});
		}

		Sbi.trace("[SelectionsPanel.initStore]: OUT");
	}

	, initStoreDataByAssociation: function() {
		Sbi.trace("[SelectionsPanel.initStoreDataByAssociation]: IN");

		var initialData = [];
		
//		if (this.widgetManager){
//			var selections = this.widgetManager.getSelectionsByAssociations();
//
//			for(var association in selections) {
//				var el = ['_association_', association, selections[association].join()];
//				initialData.push(el);
//			}
//		}

		// Until we will change selection model we use an hybrid approach. We show selection on association
		// plus selection on fields for chart widget

		if (this.widgetManager) {
			var selections = this.widgetManager.getSelections() || [];

			for (var widgetId in selections){
				Sbi.trace("[SelectionsPanel.initStoreDataByAssociation]: processing selection on widget [" + widgetId + "]");
				var widget = this.widgetManager.getWidget(widgetId);
				if(widget && widget.fieldsSelectionEnabled === true) {
					Sbi.trace("[SelectionsPanel.initStoreDataByAssociation]: field selections are enabled on widget [" + widgetId + "]");
					for (field in selections[widgetId]){
						if(selections[widgetId][field].values.length == 0) continue;
						var el = [widgetId, field, selections[widgetId][field].values.join()];
						initialData.push(el);
					}
				}
			}
		}

		Sbi.trace("[SelectionsPanel.initStoreDataByAssociation]: OUT");

		return initialData;
	}

	, initStoreDataByWidget: function() {
		Sbi.trace("[SelectionsPanel.initStoreDataByWidget]: IN");

		var initialData = [];

		if (this.widgetManager){
			var selections = this.widgetManager.getSelections() || [];

			for (widget in selections){
				var values = [];
				for (field in selections[widget]){
					if (!Ext.isFunction(selections[widget])){
						values = this.getFieldValues(selections[widget][field].values);
						var el = [widget,  field, values];
						initialData.push(el);
					}
				}
			}
		}

		Sbi.trace("[SelectionsPanel.initStoreDataByWidget]: OUT");

		return initialData;
	}

	, initGrid: function() {

	   	this.initGridHeader();
	   	if(this.showGridHeader === true) {
	   		this.initGridColumns();
	   	}


	   	var groupingFeature = Ext.create('Ext.grid.feature.Grouping',{
            groupHeaderTpl: 'Widget: {name} ({rows.length} '+ LN('sbi.cockpit.core.selections.list.items')+')'
        });
        var features = (this.showByAssociation === true)? undefined: [groupingFeature];

		var c = this.gridConfig;
	    this.grid = Ext.create('Ext.grid.Panel', Ext.apply(c || {}, {
	    	store: this.store,
		    features: features,
		    columns: this.gridColumns,
		    viewConfig: {
		    	stripeRows: true
		    },
		    header: Sbi.isValorized(this.gridHeader) ? this.gridHeader : false
		}));
	}

	, initGridColumns: function() {


	   	this.gridColumns = [];

	   	if(this.showByAssociation === true) {
	   		this.gridColumns.push({
	   			header: LN('sbi.cockpit.core.selections.list.columnAssociation')
	           	, width: 10
	           	, sortable: true
	           	, dataIndex: 'association'
	           	, flex: 1
	           });
	   	} else {
	   		this.gridColumns.push({
	   			header: LN('sbi.cockpit.core.selections.list.columnWidget')
	   			, width: 10
	   			, sortable: true
	   			, dataIndex: 'widget'
	   			, flex: 1
	        });
	  		this.gridColumns.push({
	  			header: LN('sbi.cockpit.core.selections.list.columnField')
	            , width: 70
	            , sortable: true
	            , dataIndex: 'field'
	            , flex: 1
	  		});
	    }

	    this.gridColumns.push({
	    	header: LN('sbi.cockpit.core.selections.list.columnValues')
           	, width: 70
           	, sortable: true
           	, dataIndex: 'values'
           	, flex: 1
	    });

	    this.gridColumns.push({
	    	xtype: 'actioncolumn',
	        width: 30,
	        items: [{
	           	iconCls: 'selectionDel',
	            tooltip: 'Delete',
	            handler: this.onPerformUnselect,
	            scope: this
	        }]
	    });
	}
	, initGridHeader: function() {
		Sbi.trace("[SelectionPanel.initGridHeader]: IN");

		this.gridHeader = {
			xtype: 'header',
	        titlePosition: 0,
	        items: [{
	        	xtype: 'button',
	            text: LN('sbi.selection.selectionpanel.btn.clearselections'),
	            tooltip: LN('sbi.selection.selectionpanel.btn.clearselections'),
	            handler: this.onPerformUnselectAll,
	            scope: this
	        }]
		};

		Sbi.trace("[SelectionPanel.initGridHeader]: OUT");
	}

	, onSelectionChange: function() {
		this.refreshStore();
	}

	, onCancelSingle: function(grid, rowIndex, colIndex) {
		this.fireEvent("cancelSingle",grid, rowIndex, colIndex);
	}

	, onPerformUnselect: function(grid, rowIndex, colIndex) {
		var record = this.grid.getStore().getAt(rowIndex);
		var widgetId = record.get('widget');
		var fieldHeader = record.get('association');
		if(widgetId != "_association_") {
			this.widgetManager.clearFieldSelections(widgetId, fieldHeader);
			this.fireEvent("performunselect");
		} else {
			this.widgetManager.clearAssociationSelections(fieldHeader);
		}

	}

	, onPerformUnselectAll: function(){
		this.widgetManager.clearSelections();
		this.fireEvent("performunselectall");
	}

	, initEvents: function() {
		this.addEvents(
			/**
			* @event performunselect
			* Fires when user ask to remove selections on specified field or association
			* @param {AssociationEditorWizard} this
			*/
			'performunselect'
			/**
			* @event performunselectall
			* Fires when user ask to remove all selections
			* @param {AssociationEditorWizard} this
			*/
			, 'performunselectall'
		);
	}
});
