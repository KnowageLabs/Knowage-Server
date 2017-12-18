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
  * - Giulio gavardi (giulio.gavardi@eng.it)
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.FilterLookupPopupWindow = function(config) {
	

	var defaultSettings = {
			title : LN('sbi.lookup.Select'),
			layout      : 'fit',
			width       : 580,
			height      : 300,
			closeAction :'hide',
			plain       : true
	};

	if (Sbi.settings && Sbi.settings.widgets  && Sbi.settings.widgets.filterLookupPopupWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.widgets.filterLookupPopupWindow);
	}

	var codef = Ext.apply(defaultSettings, config || {});
	
	var c = Ext.apply(this, codef);

	this.addEvents('selectionmade', 'onok');	
	
	this.init();

	this.items = this.grid;

	// constructor
	Sbi.widgets.FilterLookupPopupWindow.superclass.constructor.call(this, c);

	this.store.on('metachange', function( store, meta ) {
		this.updateMeta( meta );
	}, this);

	this.store.on('load', function( store, records, options  ) {
		this.applySelection();		
	}, this);
	
	
};

Ext.extend(Sbi.widgets.FilterLookupPopupWindow, Ext.Window, {
    
	// ----------------------------------------------------------------------------------------
	// members
	// ----------------------------------------------------------------------------------------
    

	
	// STATE MEMBERS
	  valueField: null
    , displayField: null
    , descriptionField: null
    
    // oggetto (value: description, *)
    , xvalue: null
    // oggetto (value: description, *)
    , xselection: null
    // contains values for records to be enabled (i.e. that can be selected)
    , enabledRecords: null
    
    , singleSelect: true
   
	// SUB-COMPONENTS MEMBERS
	, store: null
	, sm: null
    , grid: null
    , limit: 20
      
	
    
    // private methods
    , init: function() {
     
    	var cm = new Ext.grid.ColumnModel([
		   new Ext.grid.RowNumberer(),
	       {
	       	  header: "Values",
	          dataIndex: 'Values',
	          width: 75
	          //renderer: Ext.util.Format.dateRenderer('m/d/Y H:i:s')
	       }
	    ]);

     
		var pagingBar = new Sbi.widgets.PagingToolbar({
	        pageSize: this.limit,
	        store: this.store,
	        displayInfo: true,
	        displayMsg: '', //'Displaying topics {0} - {1} of {2}',
	        emptyMsg: "No topics to display",

	        items:[
	               '->'
	               , {
	            	   text: LN('sbi.lookup.Annulla')
	            	   , listeners: {
		           			'click': {
		                  		fn: this.onCancel,
		                  		scope: this
		                	} 
	               		}
	               } , {
	            	   text: LN('sbi.lookup.Confirm')
	            	   , listeners: {
		           			'click': {
		                  		fn: this.onOk,
		                  		scope: this
		                	} 
	               		}
	               }
	        ]
	    });

		var filteringToolbar;
		if(this.params){
			filteringToolbar = new Sbi.widgets.FilteringToolbar({store: this.store, params: this.params});
		}
		else{
			filteringToolbar = new Sbi.widgets.FilteringToolbar({store: this.store});
			
		}
		
		this.sm = new Ext.grid.CheckboxSelectionModel({
			singleSelect: this.singleSelect
			, listeners: {
				beforerowselect : {
					fn : function (sm, rowIndex, keepExisting, record) {
						var recordValue = record.data[this.valueField];
						// if record is not enabled, is cannot be selected (this.enabledRecords = null means every record is enabled)
						if ( this.enabledRecords != null && this.enabledRecords.indexOf(recordValue) == -1 ) {
							return false;
						}
					}
					, scope : this
				}
			}
		});
		this.sm.on('rowselect', this.onSelect, this);
		this.sm.on('rowdeselect', this.onDeselect, this);
		
		this.grid = new Ext.grid.GridPanel({
			store: this.store
   	     	, cm: cm
   	     	, sm: this.sm
   	     	, frame: false
   	     	, border:false  
   	     	, collapsible:false
   	     	, loadMask: true
   	     	, viewConfig: {
   	        	forceFit : true
   	        	, enableRowBody : true
   	        	, showPreview : true
		        , getRowClass : this.getRowClassFunction.createDelegate(this, [], true)
   	     	}
			, tbar: filteringToolbar
			, bbar: pagingBar
		});
		
    	if(this.singleSelect === true && 
    			this.xselection == null){
    		this.xselection = {}
    	}
    	else
    		if(this.singleSelect === false && 
        			this.xselection == null){
        		this.xselection = Ext.apply({}, {});   
        	}	
    		
    	
    	// initializing the values' array, if not already initialized
    	if (this.startValues) {
    		this.xselection['Values'] = this.startValues;
    	}
    	else if (this.xselection['Values'] === undefined) {
    		this.xselection['Values'] = new Array();
    	}

	}

	, getRowClassFunction : function (record, index, rowParams, store) {
    	var recordValue = record.data[this.valueField];
    	// if record is not enabled, is cannot be selected (this.enabledRecords = null means every record is enabled)
    	if ( this.enabledRecords != null && this.enabledRecords.indexOf(recordValue) == -1 ) {
          	return "disabled-record";
    	}
    	return "";
	}
    
    , updateMeta: function(meta) {
    	if(this.grid){		
  
			this.valueField = meta.valueField;
			this.displayField = meta.displayField;
			this.descriptionField = meta.descriptionField;
			
			meta.fields[0] = new Ext.grid.RowNumberer();
			meta.fields[ meta.fields.length ] = this.sm;

			if(meta.fields[1].type && meta.fields[1].type == 'date'){
				meta.fields[1].renderer = Sbi.locale.formatters['date'];
			}
			if(meta.fields[1].type && meta.fields[1].type === 'timestamp') {
				meta.fields[1].renderer  =  Sbi.locale.formatters['timestamp'];
			}
	
			this.grid.getColumnModel().setConfig(meta.fields);		
			
		} else {
		   alert('ERROR: store meta changed before grid instatiation')
		}		
	}
    
    , resetSelection: function(valuesToLoad) {
    	this.xselection = Ext.apply({}, this.xvalue);   
    	if (valuesToLoad && valuesToLoad !== undefined){
    		this.xselection['Values'] = valuesToLoad;    
    	}
   	}
    
    , onSelect: function(sm, rowIndex, record) {
    	
//    	if(this.singleSelect === true){
//    		this.xselection = {}
//    	}
    	
    	// initializing the values' array, if not already initialized
//    	if (this.xselection['Values'] === undefined) {
//    		this.xselection['Values'] = [];
//    	}

    	var valueToAdd = record.data[this.valueField];
    	if(this.grid.getColumnModel().getColumnById('1').type == 'date'){
    		valueToAdd = valueToAdd.format('d/m/Y H:i:s');
    	}
    	else{
        	valueToAdd+='';
   		
    	}
    	
    	// it the new value is not contained into the values' array, it is added
    	if (this.xselection['Values'].indexOf(valueToAdd) === -1) {
    		this.xselection['Values'].push(valueToAdd);
    	}
    }
    
    , onDeselect: function(sm, rowIndex, record) {
    	if (this.xselection['Values'] && this.xselection['Values'].length > 0) {
    		var valueToRemove = record.data[this.valueField];
    		if (this.grid.getColumnModel().getColumnById('1').type == 'date') {
    			valueToRemove = valueToRemove.format('d/m/Y H:i:s');
    		}
    		else{
    			valueToRemove+='';
    		}
    		this.xselection['Values'].remove(valueToRemove);
    	}    	
    }
    
    , applySelection: function() {
    	if (this.grid && this.xselection['Values']) {
			var selectedRecs = [];
			this.grid.getStore().each(function(rec) {
				var valueToLookFor = rec.data[this.valueField];
				
	    		if (valueToLookFor!= null && valueToLookFor!="" && this.grid.getColumnModel().getColumnById('1').type == 'date') {
	    			valueToLookFor = valueToLookFor.format('d/m/Y H:i:s');
	    		}
	    		else{
	    			valueToLookFor+='';	
	    		}
				if (this.xselection['Values'].indexOf(valueToLookFor) !== -1){
		        	selectedRecs.push(rec);	        	
		        }
		    }, this);
			this.sm.selectRecords(selectedRecs);		    
		 }		
    }
	
	, onOk: function() {
		this.fireEvent('selectionmade', this, this.xselection);	
		this.hide();	
	}
	
	, onCancel: function() {
		this.hide();
	}
	, getSelection: function(){
		return this.xselection['Values'];	
	}
	, setSelection: function(arraySel){
		this.xselection['Values'] = arraySel;  
	}
	
});