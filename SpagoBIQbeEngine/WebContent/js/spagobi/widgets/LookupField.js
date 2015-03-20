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

Ext.ns("Sbi.widgets");

Sbi.widgets.LookupField = function(config) {
	
	var defaultSettings = {
		enableFiltering: true
	};
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.store.on('metachange', function( store, meta ) {
		this.updateMeta( meta );
	}, this);
	this.store.on('load', function( store, records, options  ) {
		this.applySelection();		
	}, this);
	

	this.store.baseParams  = c.params;
	this.params = c.params;
	this.initWin();
	
	c = Ext.apply(c, {
		triggerClass: 'x-form-search-trigger'
		, enableKeyEvents: true
		,  width: 150
	});   
	
	// constructor
	Sbi.widgets.LookupField.superclass.constructor.call(this, c);
	
	
	this.on("render", function(field) {
		field.trigger.on("click", function(e) {
			this.onLookUp(); 
		}, this);
	}, this);
	
	this.on("render", function(field) {
		field.el.on("keyup", function(e) {
			var key = e.getKey();
			if (key != e.TAB) {
				this.xdirty = true;
			}
		}, this);
	}, this);
	
	
};

/**
 * @class Sbi.widgets.LookupField
 * @extends Ext.form.TriggerField
 * 
 * LookupField
 */
Ext.extend(Sbi.widgets.LookupField, Ext.form.TriggerField, {
    
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
    , xdirty: false
    
    , singleSelect: true
    
    , paging: true
    , start: 0 
    , limit: 20
    
	// SUB-COMPONENTS MEMBERS
	, store: null
	, sm: null
	, cm: null
    , grid: null
    , win: null
    
       
   
    // ----------------------------------------------------------------------------------------
    // public methods
	// ----------------------------------------------------------------------------------------
    
    
    , getValue : function(){
		this.clean();
		var v = [];
		this.xvalue = this.xvalue || {};
		for(p in this.xvalue) {
			v.push(p);
		}
			
		if(this.singleSelect === true) {
			v = (v.length > 0)? v[0] : '';
		}
		
		return v;
	}

	/**
	 * v: 
	 *  - object -> multivalue with values/descriptions
	 *  - array -> multivalue with only values
	 *  - string -> single value
	 */
	, setValue : function(v){	 
		//alert('v ' + v);
		if(v === undefined) {
			this.xvalue = {};
			Sbi.widgets.LookupField.superclass.setValue.call(this, '');
			return;
		}
		//alert('v.toSource() ' + v.toSource());
		if(typeof v === 'object') {
			this.xvalue = {};
			
			if(v instanceof Array) {
				var t = {};
				for(var i = 0; i < v.length; i++) {
					t[ v[i] ] = v[i];
				}
				v = t;
			}
			
			Ext.apply(this.xvalue, v);
			
			//alert('this.xdirty ' + this.xdirty);
			//alert('step 1 this.getValue().toSource() ' + this.getValue().toSource());
			
			var displayText = '';
			for(p in this.xvalue) {
				displayText += this.xvalue[p] + ';';
			}	
		
			if(this.singleSelect === true) {
				displayText = displayText.substr(0, displayText.length-1);
			}
			Sbi.widgets.LookupField.superclass.setValue.call(this, displayText);
			//alert('this.xdirty ' + this.xdirty);
			//alert('step 2 this.getValue().toSource() ' + this.getValue().toSource());
			
		} else {
			this.xvalue = {};
			this.xvalue[v] = v;
			Sbi.widgets.LookupField.superclass.setValue.call(this, v);
		}
	}
	
	, getDescription: function() {
		return Sbi.widgets.LookupField.superclass.getValue.call(this);
	}
	
	, setDescription: function(d) {
		Sbi.widgets.LookupField.superclass.setValue.call(this, d);
	}
	
    
    // private methods
    , initWin: function() {
		var cm;
		if(this.cm === null) {
			cm = new Ext.grid.ColumnModel([
			   new Ext.grid.RowNumberer(),
		       {
		       	  header: "Data",
		          dataIndex: 'data',
		          width: 75
		       }
		    ]);
		} else {
			cm = this.cm;
		}
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
		if(this.enableFiltering) {
			filteringToolbar = new Sbi.widgets.FilteringToolbar({store: this.store});
		}
		
		this.sm = new Ext.grid.CheckboxSelectionModel( {singleSelect: this.singleSelect } );
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
   	        	forceFit:true
   	        	, enableRowBody:true
   	        	, showPreview:true
   	     	}
			
			, tbar: filteringToolbar
	        , bbar: pagingBar
		});
		
		this.grid.on('rowdblclick', this.onOk, this);
		
		this.win = new Ext.Window({
			title: LN('sbi.lookup.Select') ,   
            layout      : 'fit',
            width       : 580,
            height      : 300,
            closeAction :'hide',
            plain       : true,
            items       : [this.grid]
		});
	}
    
    , updateMeta: function(meta) {
    	if(this.grid){			
			this.valueField = meta.valueField;
			this.displayField = meta.displayField;
			this.descriptionField = meta.descriptionField;
			
			meta.fields[0] = new Ext.grid.RowNumberer();
			meta.fields[ meta.fields.length - 1 ] = this.sm;
			this.grid.getColumnModel().setConfig(meta.fields);
		} else {
		   alert('ERROR: store meta changed before grid instatiation')
		}
	}
    
    , resetSelection: function() {
    	this.xselection = Ext.apply({}, this.xvalue);    
   	}
    
    , onSelect: function(sm, rowIndex, record) {
    	if(this.singleSelect === true){
    		this.xselection = {}
    	}
    	this.xselection[ record.data[this.valueField] ] = record.data[this.displayField];
    }
    
    , onDeselect: function(sm, rowIndex, record) {
    	if( this.xselection[ record.data[this.valueField]] ) {
    		delete this.xselection[ record.data[this.valueField]];
    	}    	
    }
    
    , applySelection: function() {
    	//this.resetSelection();
    	
    	if(this.grid) {    		    		
			var selectedRecs = [];
			this.grid.getStore().each(function(rec){
		        if(this.xselection[ rec.data[this.valueField]] !== undefined){
		        	selectedRecs.push(rec);
		        }
		    }, this);
		    this.sm.selectRecords(selectedRecs);
		 }		
    }
	
    , clean: function() {
    	if(this.xdirty === true) {
	    	var text = Sbi.widgets.LookupField.superclass.getValue.call(this);
	    	var values = text.split(';');
	    	this.xvalue = {};
	    	if(text.trim() === '') return;
	    	var ub = (this.singleSelect === true)? 1: values.length;
	    	for(var i = 0; i < ub; i++) {
	    		this.xvalue[ '' + values[i] ] = values[i];
	    	}
	    	this.xdirty = false;
    	}
    }
    
	, onLookUp: function() {
		this.clean();
		this.resetSelection();
		
		this.win.show(this);
		var p = Ext.apply({}, this.params, {
			start: this.start
			, limit: this.limit
		});
		this.store.load({params: p});
	}
	
	, onOk: function() {
		var oldVal = this.getValue();
		this.setValue(this.xselection);
		var newVal = this.getValue();
		this.win.hide();		
		this.fireEvent('change', this, newVal, oldVal);
	}
	
	, onCancel: function() {
		this.win.hide();
	}
});