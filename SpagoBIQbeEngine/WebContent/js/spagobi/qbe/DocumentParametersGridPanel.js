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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.DocumentParametersGridPanel = function(config, store) {
	var c = Ext.apply({
		// set default values here
	}, config || {});
	
	/*
	this.services = new Array();
	
	var params = {};
	
	this.services['getDocumentParameters'] = Sbi.config.remoteServiceRegistry.getServiceUrl({
		serviceName: 'GET_PARAMETERS_FOR_EXECUTION_ACTION'
		, baseParams: params
	});

	
	this.store = new Ext.data.Store({
        autoLoad:true,
        proxy: new Ext.data.ScriptTagProxy({
	        url: this.services['getDocumentParameters'],
	        method: 'GET'
	    }),
	    reader: new Ext.data.JsonReader({}, [
	            {name:'id'},
	            {name:'label'},
	            {name:'type'}
	        ])
	});
	*/
	
	this.store = store;
 	
	this.sm = new Ext.grid.RowSelectionModel({singleSelect:true});
 	
 	this.grid = new Ext.grid.GridPanel({
        store: this.store
        , border: false
        , columns: [
            {header: LN('sbi.qbe.documentparametersgridpanel.headers.label'), sortable: true, dataIndex: 'label'}
        ]
		, viewConfig: {
        	forceFit: true
        	, emptyText: LN('sbi.qbe.documentparametersgridpanel.emptytext')
		}
        , sm : this.sm
        , enableDragDrop: true
        , ddGroup: 'gridDDGroup'
        , height: 155
        //, autoScroll: true
        , layout: 'fit'
 	});
 	this.grid.type = this.type;
 	
	c = Ext.apply({}, c, {
		title: LN('sbi.qbe.documentparametersgridpanel.title')
        , border: false
        //, autoScroll: true
        , collapsible: false
        , height: 180
        , items: [this.grid]
	});
	
	// constructor
	Sbi.qbe.DocumentParametersGridPanel.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.qbe.DocumentParametersGridPanel, Ext.Panel, {
    
	services: null
	, type: 'documentparametersgrid'
	
});