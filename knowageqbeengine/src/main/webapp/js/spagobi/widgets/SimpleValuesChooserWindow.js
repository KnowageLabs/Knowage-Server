/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  
 

/**
  * Sbi.widgets.SimpleValuesChooserWindow 
  * 
  * A window with an internal grid with a list of values. The user can choose one or more values.
  * There are no filters or paging toolbars, just a simple list.
  * It can be used for lookup fields when a simple selection of values is required with no list pagination.
  * The constructor input object must have the following properties:
  * - url: the url to load the store with the list of values
  * - columnName: the name of the column of the store that must be displayed
  * - columnHeader: the header of the column to be displayed
  * 
  * Public Properties
  * 
  * [list]
  * 
  * Public Methods
  * 
  * - getSelectedValues: returns the values selected by the user
  * 
  * - select: select a list of values in the list
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.SimpleValuesChooserWindow = function(config) {
	
	var defaultSettings = {
		singleSelect : false
	};
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
		title		: LN('sbi.lookup.Select'),   
        layout      : 'fit',
        width       : 500,
        height      : 300,
        plain       : true,
        items       : [this.grid]
 	});
	
	// constructor
	Sbi.widgets.SimpleValuesChooserWindow.superclass.constructor.call(this, c);
	
	this.addEvents('load');

};

Ext.extend(Sbi.widgets.SimpleValuesChooserWindow, Ext.Window, {

	sm 				: null
	, cm 			: null
	, singleSelect 	: null
	, url 			: null // this must be in the constructor input object
	, columnHeader 	: null // this must be in the constructor input object
	, columnName 	: null // this must be in the constructor input object
	, store			: null // the store with the values to display
	
	,
	init : function () {
 		this.sm = new Ext.grid.CheckboxSelectionModel( {singleSelect: this.singleSelect } );
 		
     	this.cm = new Ext.grid.ColumnModel([
		   new Ext.grid.RowNumberer(),
	       {
	       	  header: this.columnHeader,
	          dataIndex: this.columnName,
	          width: 75
	       },
	       this.sm
	    ]);
 		
		this.store = new Ext.data.JsonStore({
			url: this.url
		});
		this.store.on('loadexception', function(store, options, response, e) {
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
		});
		this.store.on('load', function (store, records, options) {
			this.fireEvent('load', this, records, options);
		}, this);
     	
 		this.grid = new Ext.grid.GridPanel({
 			store 			: this.store
	     	, cm 			: this.cm
	     	, sm 			: this.sm
	     	, frame 		: false
	     	, border 		: false  
	     	, collapsible 	: false
	     	, loadMask		: true
	     	, viewConfig: {
	        	forceFit : true
	        	, enableRowBody : true
	        	, showPreview : true
	     	}
 		});
 		
	}

	,
	getSelectedValues : function () {
		var selectedRecords = this.sm.getSelections();
		var toReturn = [];
		for (var i = 0; i < selectedRecords.length; i++) {
			var aRecord = selectedRecords[i];
			toReturn.push(aRecord.data[this.columnName]);
		}
		return toReturn;
	}
	
	,
	select : function (values) {
		var records = [];
		this.store.each(function (aRecord) {
			if (values.indexOf(aRecord.data[this.columnName]) != -1) {
				records.push(aRecord);
			}
		}, this);
		this.sm.selectRecords(records, false);
	}
	
	,
	load : function (obj) {
		this.store.load(obj);
	}
	
});