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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.formviewer");

Sbi.formviewer.ResultsPage = function(config) {	
	var defaultSettings = {
		//title: LN('sbi.qbe.queryeditor.title')
	};
		
	if(Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.resultsPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.resultsPage);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
		
	this.services = this.services || new Array();	
	this.services['getSelectedColumns'] = this.services['getSelectedColumns'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_SELECTED_COLUMNS_ACTION'
		, baseParams: new Object()
	});
		
	this.addEvents('edit');
		
	//this.initControlPanel(c.controlPanelConfig || {});
	this.initMasterDetailPanel(c.masterDetailPanelConfig || {});
	
	this.toolbar = new Ext.Toolbar({
		items: [
		    '->'
		    , {
				text: LN('sbi.formviewer.resultspage.backtoform'),
				handler: function() {this.fireEvent('edit');},
				scope: this
		    }
		  ]
	});
	
	c = Ext.apply(c, {
	    layout:'border',
	    tbar: this.toolbar,
	    style: 'padding:3px;',
	    //bodyStyle:'background:green',
	    //items: [this.controlPanel, this.masterResultsPanel, this.detailResultsPanel]
	    items: this.hasGroupingVariables() ? [this.masterResultsPanel, this.detailResultsPanel] : [this.detailResultsPanel]
	});
		
		
		
	// constructor
	Sbi.formviewer.ResultsPage.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formviewer.ResultsPage, Ext.Panel, {
    
    services: null
    , controlPanel: null
    , masterResultsPanel: null
    , detailResultsPanel: null
    , groupInputField: null
    , formState: null
   
    // -- public methods -----------------------------------------------------------------------
    
    , getFormState: function() {
		return this.formState;
	}

	, setFormState: function(formState) {
		this.formState = formState;
	}
    
    , loadResults: function(groupFields) {
    	if ( this.hasGroupingVariables() ) {
        	var values = new Array();
    		for(p in this.formState.groupingVariables) {
    			values.push(this.formState.groupingVariables[p]);
    		}
        	
        	var baseParams = {groupFields: Ext.util.JSON.encode(values), formstate: Ext.util.JSON.encode(this.formState)}
    		this.masterResultsPanel.execQuery(baseParams);
        	this.detailResultsPanel.store.removeAll();
    	} else {
	       	var baseParams = {filters: "[]", formState: Ext.util.JSON.encode(this.formState)}
			this.detailResultsPanel.execQuery(baseParams);
    	}

	}
    
    // -- private methods -----------------------------------------------------------------------
        
    , initControlPanel: function() {
		var p = {type: 'groupable'};
		store = new Ext.data.JsonStore({
			url: this.services['getSelectedColumns']
			, baseParams: p
			, root: 'fields'
		    , fields: ['name', 'alias']
		});
		
		store.on('loadexception', function(store, options, response, e) {
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
		});
		
		this.groupInputField = new Ext.ux.form.SuperBoxSelect({
	        editable: true			    
		    , forceSelection: false
		    , store: store
		    , fieldLabel: 'Group By'
		    , anchor:'100%'
		    , displayField: 'alias'
		    , valueField: 'name'
		    , emptyText: ''
		    , typeAhead: false
		    , triggerAction: 'all'
		    , selectOnFocus: true
		    , autoLoad: false
		});
		
		var submitButton = new Ext.Button({
			text: "Ricalcola",
            scope: this,
            handler: function(){this.loadResults();}

		});
		
		var backButton = new Ext.Button({
			text: "Indietro",
            scope: this,
            handler: function() {this.fireEvent('edit', this);}

		});
	
		this.controlPanel = new Ext.Panel({
			region: 'north'
			, layout: 'border'
			, frame: false
			, border: true
			, style:'padding:10px; background:white'
			, height: 70
			, items: [
			     new Ext.form.FormPanel({
			    	 region: 'center', 
			    	 frame: false, 
					 border: false,
			    	 bodyStyle:'padding:10px',
			    	 items: [this.groupInputField]
			     })
				, new Ext.form.FormPanel({
					region: 'east', 
					frame: false, 
					border: false,
					split: false, 
					bodyStyle:'padding:13 10 13 5',
					width: 85, 
					items: [submitButton]
				}), new Ext.form.FormPanel({
					region: 'west', 
					split: false, 
					frame: false, 
					border: false,
					bodyStyle:'padding:13 10 13 5',
					width: 85, 
					items: [backButton]
				})
			]
		});
	}
    
    , initMasterDetailPanel: function() {
		this.masterResultsPanel = new Sbi.formviewer.DataStorePanel({
			region: 'west',
			split: true,
			collapsible: false,
			autoScroll: true,
			frame: false, 
			border: false,
			width: 320,
			minWidth: 320,
			displayInfo: false,
			pageSize: 50,
			sortable: false,
			
			services: {
				loadDataStore: Sbi.config.serviceRegistry.getServiceUrl({
					serviceName: 'EXECUTE_MASTER_QUERY_ACTION'
					, baseParams: new Object()
				})
			}
		});
	
		this.detailResultsPanel = new Sbi.formviewer.DataStorePanel({
			region: 'center',
			frame: false, 
		    border: false,
		    displayInfo: true,
		    pageSize: 25,
		    sortable: true,
		    
		    services: {
				loadDataStore: Sbi.config.serviceRegistry.getServiceUrl({
					serviceName: 'EXECUTE_DETAIL_QUERY_ACTION'
					, baseParams: new Object()
				})
			}
		});
		
		this.masterResultsPanel.grid.on("rowdblclick", function(grid,  rowIndex, e){
	    	var row;
	       	var record = grid.getStore().getAt( rowIndex );
	       	var baseParams = grid.getStore().baseParams;
	       	var fields = Ext.util.JSON.decode(baseParams.groupFields);
	       	var filters = new Array();
	       	for(var i = 0; i < fields.length; i++) {
	       		var filter = {
	       			columnName: fields[i],
	       			value: record.get('column_'+(i+1))
	       		};
	       		filters.push(filter);
	       	}
	       	
	       	var baseParams = {filters: Ext.util.JSON.encode(filters), formState: Ext.util.JSON.encode(this.formState)}
			this.detailResultsPanel.execQuery(baseParams);
	    }, this);
	}
    
    ,
    hasGroupingVariables : function () {
    	var groupingVariables = this.template.groupingVariables;
    	if (groupingVariables.length == 0) {
    		return false;
    	}
    	return true;
    }
    
});