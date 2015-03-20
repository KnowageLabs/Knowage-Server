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
  * - name (mail)
  */


Ext.ns("Sbi.widgets");

Sbi.widgets.FilteringToolbar = function(config) {	
	Sbi.widgets.FilteringToolbar.superclass.constructor.call(this, config);
	this.state = this.EDITING;
	// qbe
	if(config.params){
	this.params = config.params;
	}
	
};

Ext.extend(Sbi.widgets.FilteringToolbar, Ext.Toolbar, {
    
	state: null
	, EDITING: 'editing'
	, FILTERING: 'filtering'
	, UNFILTERING: 'unfiltering'

	, columnNameStore: null
	, columnNameCombo: null
	
	, typeStore: null
	, typeCombo: null
	
	, filterStore: null
	, filterCombo: null
	
	, inputField: null
	, valueType: null	
	
	, initComponent : function(){
		Sbi.widgets.FilteringToolbar.superclass.initComponent.call(this);
		this.bind(this.store);
	}
	
	, onRender : function(ct, position) {

		Sbi.widgets.FilteringToolbar.superclass.onRender.call(this, ct, position);
	    
		this.addText(LN('sbi.lookup.ValueOfColumn'));	
		this.addSpacer();
		
		
	    this.columnNameStore = new Ext.data.SimpleStore({
	        fields: ['value', 'label'],
	        data : []
	    });	 	    
	    this.columnNameCombo = new Ext.form.ComboBox({
	        store: this.columnNameStore,
	        width: 100,
	        displayField:'label',
	        valueField:'value',
	        typeAhead: true,
	        triggerAction: 'all',
	        emptyText:'...',
	        selectOnFocus:true,
	        mode: 'local'
	    });	    
	    this.addField( this.columnNameCombo );	    
	    this.addSpacer();
	    	    
	    this.addText(LN('sbi.lookup.asA'));
	    this.addSpacer();
	    
	    this.typeStore = new Ext.data.SimpleStore({
	        fields: ['value', 'label'],
	        data : [
	                ['string', 'string']
	                , ['number', 'number']
	                , ['date', 'date']
	        ]
	    });	    
	    this.typeCombo = new Ext.form.ComboBox({
	        store: this.typeStore,
	        width: 65,
	        displayField:'label',
	        valueField:'value',
	        typeAhead: true,
	        triggerAction: 'all',
	        emptyText:'...',
	        selectOnFocus:true,
	        mode: 'local'
	    });
	    this.addField( this.typeCombo );
	    this.addSpacer();
	    	    
	    this.filterStore = new Ext.data.SimpleStore({
	        fields: ['value', 'label'],
	        data : [
	                ['CONTAINS', 'contains']
	                , ['STARTS WITH', 'starts with']
	                , ['ENDS WITH', 'ends with']
	                , ['EQUALS TO', '=']
	                , ['LESS THAN', '<']
	                , ['EQUALS OR LESS THAN', '<=']
	                , ['GREATER THAN', '>']
	 	            , ['EQUALS OR GREATER THAN', '>=']
	        ]
	    });	    
	    this.filterCombo = new Ext.form.ComboBox({
	        store: this.filterStore,
	        width: 80,
	        displayField:'label',
	        valueField:'value',
	        typeAhead: true,
	        triggerAction: 'all',
	        emptyText:'...',
	        selectOnFocus:true,
	        mode: 'local'
	    });
	    this.addField( this.filterCombo );   
	    this.addSpacer();
	    
	    this.inputField = new Ext.form.TextField({width: 70});
	    this.addField( this.inputField );
	    this.addSeparator();
	    
	    this.doButton = this.addButton({
	        tooltip: 'apply filter'
	        , iconCls: 'icon-execute'
	        , disabled: false
	        , handler: this.onClick.createDelegate(this)
	    });
	    
	    this.unfilterButton = this.addButton({
	        tooltip: 'remove filter'
	        , iconCls: 'icon-remove'
	        , disabled: false
	        , handler: this.onUnfilter.createDelegate(this)
	    });
	    
	    this.addFill();	    
	}

	, getFilterBarState: function(asObject) {
		var filterBarState = {};
		
		filterBarState.columnFilter = this.columnNameCombo.getValue();
		filterBarState.typeValueFilter = this.typeCombo.getValue();
		filterBarState.typeFilter = this.filterCombo.getValue();
		filterBarState.valueFilter = this.inputField.getValue();
		
		if(asObject !== undefined && asObject === false) {
			filterBarState = Ext.util.JSON.encode(filterBarState);
		}	
		return filterBarState;
	}
	
	, resetFilterBarState: function() {		
		this.columnNameCombo.reset();		
		this.typeCombo.reset();		
		this.filterCombo.reset();		
		this.inputField.reset();
	}
	
	, disableEditing: function() {		
		this.columnNameCombo.disable();	
		this.typeCombo.disable();	
		this.filterCombo.disable();		
		this.inputField.disable();
	}
	
	, enableEditing: function() {		
		this.columnNameCombo.enable();	
		this.typeCombo.enable();	
		this.filterCombo.enable();		
		this.inputField.enable();
	}

	, onClick: function() {
		if(this.state === this.EDITING) {
			//alert('applyFilter');
			this.unfilterButton.enable();
			this.disableEditing();
			
			this.state = this.FILTERING;		
			this.doLoad();					
		} else if(this.state === this.FILTERING) {
			//alert('editFilter');
			this.enableEditing();
			this.state = this.EDITING;
		}
		
		 
		 //alert(this.store.baseParams.toSource());		
	}
	
	, onUnfilter: function() {
		this.unfilterButton.disable();
		this.resetFilterBarState();
		this.enableEditing();
		
		this.state = this.UNFILTERING;		
		this.doLoad();
	}
	
	, doLoad: function() {
		var p = Ext.apply({}, this.getFilterBarState(false));
		if(this.params){
		p = Ext.apply(p, this.params);
		}
		this.store.load({params: p});	
	}
	
	, boforeLoad: function(store, o) {
    	
		if(this.state === this.FILTERING) {
			var p = this.getFilterBarState(false);
			o.params.FILTERS = p;
		} else if(this.state === this.UNFILTERING) {
			if(o.params.FILTERS !== undefined) 
				alert('ERROR: While UNFILTERING filters parameter cannot be valorized');
		} 
		
		//alert('FILTERING_TOOLBAR\n' +  o.params.toSource());
		
		return true;
	}
	
	, onLoad: function() {
		if(this.state === this.UNFILTERING) {
			this.state = this.EDITING;
		}
	}
	
	, onMetaChange: function(store, meta) {
		this.columnNameStore.removeAll();
		
		for(var i = 0; i < meta.fields.length; i ++) {
			if(meta.fields[i].name) {
				var r = new  this.columnNameStore.recordType({
					'value': meta.fields[i].name, 
					'label': meta.fields[i].header || meta.fields[i].name
				});
				this.columnNameStore.add([r]);				
				this.typeStore.loadData([[meta.fields[i].type, meta.fields[i].type]],false);
				this.typeCombo.setValue(meta.fields[i].type);
				this.columnNameCombo.setValue(meta.fields[i].name);
				if(meta.fields[i].type=='int' || meta.fields[i].type=='date'){
					this.filterStore.loadData([
	              	  ['EQUALS TO', '=']
	                , ['LESS THAN', '<']
	                , ['EQUALS OR LESS THAN', '<=']
	                , ['GREATER THAN', '>']
	 	            , ['EQUALS OR GREATER THAN', '>=']
	        		],false);
				}
			}
		}
	}
	
	, bind: function(store) {
		store = Ext.StoreMgr.lookup(store);
		store.on('metachange', this.onMetaChange , this);	    
	    store.on('beforeload', this.boforeLoad , this);
	    store.on("load", this.onLoad, this);
        //store.on("loadexception", this.onLoadError, this);
	    this.store = store;
	}
	
	, unbind : function(store){
        store = Ext.StoreMgr.lookup(store);
        store.un('metachange', this.onMetaChange , this);	    
	    store.un('beforeload', this.boforeLoad , this);
        store.un("load", this.onLoad, this);
        //store.un("loadexception", this.onLoadError, this);
        this.store = undefined;
    }
	
	
});