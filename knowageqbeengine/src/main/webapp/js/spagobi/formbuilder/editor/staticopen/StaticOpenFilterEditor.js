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

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.StaticOpenFilterEditor = function(config) {
	
	var defaultSettings = {
		layout: 'form' // form layout required: input field labels are displayed only with this layout
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticOpenFilterEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticOpenFilterEditor);
	}
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
	
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
	this.services = new Array();
	this.services['getFilterValuesService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FILTER_VALUES_ACTION'
		, baseParams: params
	});
	
	this.init();
	
	// constructor
	Ext.apply(c, {
		header: false
		, items: [this.filter]
	});
    Sbi.formbuilder.StaticOpenFilterEditor.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.StaticOpenFilterEditor, Ext.Panel,  {
    
	filter: null
	, store: null
	
	// filter conf
	, text: null
	, singleSelection: null
	, maxSelectedNumber: null
	, operator: null
	, query: null
	, field: null
	, orderField: null
	, orderType: null
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	, setContents: function(c) {
		alert('setContents');
	}
	
	, getContents: function() {
		var c = {};
		
		c.text = this.text || undefined;
		c.singleSelection = this.singleSelection || undefined;
		c.maxSelectedNumber = this.maxSelectedNumber || undefined;
		c.operator = this.operator || undefined;
		c.field = this.field || undefined;
		c.orderBy = this.orderBy || undefined;
		c.orderType = this.orderType || undefined;
		c.queryRootEntity = this.queryRootEntity || undefined;
		
		c.queryType = this.queryType || undefined;
		c.lookupQuery = this.lookupQuery || undefined;
		
		return c;
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, init: function() {
		var baseConfig = {
	       fieldLabel: this.text
		   , name : this.id
		   , width: this.fieldWidth
		   , allowBlank: true
		};
		
		this.initStore();
		
		var maxSelectionNumber = 1;
		if (this.singleSelection === undefined || this.singleSelection === null || this.singleSelection === true) {
			maxSelectionNumber = 1;
		} else {
			maxSelectionNumber = this.maxSelectedNumber;
		}
		
		
		this.filter = new Ext.ux.form.SuperBoxSelect(Ext.apply(baseConfig, {
			editable: true			    
		    , forceSelection: false
		    , store: this.store
		    , displayField: 'column_1'
		    , valueField: 'column_1'
		    , emptyText: ''
		    , typeAhead: false
		    , triggerAction: 'all'
		    , selectOnFocus: true
		    , autoLoad: false
		    , maxSelection: maxSelectionNumber
		    , width: 200
		    , maxHeight: 250
		}));
		
		/*
		this.filter = new Ext.Panel({
			html: 'prova'
		});
		*/
	}
		
	, initStore: function() {
		var queryType = this.queryType;
		var lookupQuery = this.lookupQuery;
		var entityId = this.field;
		var orderField = this.orderBy;
		var orderType = this.orderType;
		var queryRootEntity = this.queryRootEntity;
		
		
		this.store = new Ext.data.JsonStore({
			url: this.services['getFilterValuesService']
			, remoteSort: true
		});
		var baseParams = {
				'QUERY_TYPE': queryType, 
				'LOOKUP_QUERY': lookupQuery, 
				'ENTITY_ID': entityId, 
				'ORDER_ENTITY': orderField, 
				'ORDER_TYPE': orderType, 
				'QUERY_ROOT_ENTITY': queryRootEntity
		};
		this.store.baseParams = baseParams;
		
		this.store.on('loadexception', function(store, options, response, e) {
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
		});
	}
	
});