
/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 

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
	
	Ext.apply(this, config);
	
	this.store = config.store;
	if(config.cm){
	    this.cm = config.cm;
    }
	this.store.on('metachange', function( store, meta ) {
		this.updateMeta( meta );
	}, this);
	this.store.on('load', function( store, records, options  ) {
		this.applySelection();		
	}, this);
		
	if(config.drawFilterToolbar !== undefined && config.drawFilterToolbar !== null && config.drawFilterToolbar==false){
		this.drawFilterToolbar = false;
	}else{
		this.drawFilterToolbar = true;
	}
	this.store.baseParams  = config.params;
	this.params = config.params;
	this.initWin();
	
	var c = Ext.apply({}, config, {
		triggerClass: 'x-form-search-trigger'
		, enableKeyEvents: true
		,  width: 150
		//, 	readOnly: true
	});   
	
	// constructor
	Sbi.widgets.LookupField.superclass.constructor.call(this, c);
	
	
	this.on("render", function(field) {
		field.trigger.on("click", function(e) {
			if(!this.disabled) {
				this.onLookUp(); 
			}
		}, this);
	}, this);
	
	this.on("render", function(field) {
		field.el.on("keyup", function(e) {
			this.xdirty = true;
		}, this);
	}, this);
	
	this.addEvents('select');	
	
	
};

Ext.extend(Sbi.widgets.LookupField, Ext.form.TriggerField, {
    
	// ----------------------------------------------------------------------------------------
	// members
	// ----------------------------------------------------------------------------------------
    
	// STATE MEMBERS
	  valueField: null
    , displayField: null
    , descriptionField: null
    
    , drawFilterToolbar: null
    
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

	, getValues : function(){
		var v = this.getValue();
		if(this.singleSelect === true) v = [v];
		return v;
	}

	/**
	 * v: 
	 *  - object -> multivalue with values/descriptions
	 *  - array -> multivalue with only values
	 *  - string -> single value
	 */
	, setValue : function(v){	 
		
		var vt = v;
		if(typeof v === 'object') {
			this.xvalue = {};
			
			if(v instanceof Array) {
				var t = {};
				for(var i = 0; i < v.length; i++) {
					if(v[i] !== undefined) {
						t[ v[i] ] = v[i];
					}
				}
				v = t;
			}
			
			Ext.apply(this.xvalue, v);
			var displayText = '';
			for(p in this.xvalue) {
				displayText += this.xvalue[p] + ';';
			}	
			if(this.singleSelect === true) {
				displayText = displayText.substr(0, displayText.length-1);
			}
			Sbi.widgets.LookupField.superclass.setValue.call(this, displayText);
		} else {
			this.xvalue = {};
			if(v !== undefined) {
				this.xvalue[v] = v;
			}
			Sbi.widgets.LookupField.superclass.setValue.call(this, v);
		}
		
		//alert(this.name + '.setValue(' + vt + ') = ' + this.xvalue.toSource());
		
		this.fireEvent('select', this, v);
	}
	
	
    
    // private methods
    , initWin: function() {
    	if(!this.cm){
		this.cm = new Ext.grid.ColumnModel([
		   new Ext.grid.RowNumberer(),
	       {
	       	  header: "Data",
	          dataIndex: 'data',
	          width: 75
	       }
	    ]);
    	}
    	var pagingBar = null;
    	if(!this.drawFilterToolbar){
    		pagingBar = new Ext.PagingToolbar({
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
    	}else{
    		pagingBar = new Sbi.widgets.PagingToolbar({
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
    	}
		
		if(this.drawFilterToolbar){
			this.filteringToolbar = new Sbi.widgets.FilteringToolbar({store: this.store});
		}
		var checkboxSelectionModelConf = {singleSelect: this.singleSelect };
		if( this.singleSelect){
			checkboxSelectionModelConf.header = '';
		}
		
		this.sm = new Ext.grid.CheckboxSelectionModel( checkboxSelectionModelConf );
		this.sm.on('rowselect', this.onSelect, this);
		this.sm.on('rowdeselect', this.onDeselect, this);
		
		if(this.drawFilterToolbar){
			this.grid = new Ext.grid.GridPanel({
				store: this.store
	   	     	, cm: this.cm
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
				, tbar: this.filteringToolbar
		        , bbar: pagingBar
			});
		}else{
			this.grid = new Ext.grid.GridPanel({
				store: this.store
	   	     	, cm: this.cm
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
		        , bbar: pagingBar
			});
		}
		
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
    	this.sm.clearSelections(true);
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
    	if(!this.drawFilterToolbar){
    		this.resetSelection();
    	}
    	
    	if(this.grid) {    		    		
			var selectedRecs = [];
			this.grid.getStore().each(function(rec){
		        if(this.xselection[ rec.data[this.valueField]] !== undefined){
		        	selectedRecs.push(rec);
		        }
		    }, this);
			if(selectedRecs.length>0){
				this.sm.selectRecords(selectedRecs);
			}
		 }		
    }
	
    , clean: function() {
    	if(this.xdirty) {
    		
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
		this.setValue(this.xselection);
		this.win.hide();		
	}
	
	, onCancel: function() {
		this.win.hide();
	}
});