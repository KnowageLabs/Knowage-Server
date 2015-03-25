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
  * Monica Franceschini
  */


Ext.ns("Sbi.widgets");

Sbi.widgets.FilteringToolbarLight = function(config) {	
	
	Sbi.widgets.FilteringToolbarLight.superclass.constructor.call(this, config);
	if(config.columnName){
		this.columnsToSearch = config.columnName;
	}else{
		this.columnsToSearch = [['name','Name']];
	}
	this.columnToSearchValue = config.columnValue;

	this.state = this.EDITING;
};

Ext.extend(Sbi.widgets.FilteringToolbarLight, Ext.Toolbar, {
    
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
	, columnsToSearch: null
	
	, initComponent : function(){
		Sbi.widgets.FilteringToolbarLight.superclass.initComponent.call(this);
		this.bind(this.store);
	}
	
	, onRender : function(ct, position) {
		Sbi.widgets.FilteringToolbarLight.superclass.onRender.call(this, ct, position);
	    
		//this.addText(this.columnsToSearch);	
		this.columnsStore = new Ext.data.SimpleStore({
	        fields: ['value', 'label'],
	        data : this.columnsToSearch
	    });	    
	    this.columnsFilterCombo = new Ext.form.ComboBox({
	        store: this.columnsStore,
	        width: 80,
	        displayField:'label',
	        valueField:'value',
	        typeAhead: true,
	        triggerAction: 'all',
	        emptyText:'...',
	        selectOnFocus:true,
	        mode: 'local'
	    });
	    this.addField( this.columnsFilterCombo );   
		
		this.addSpacer();

	    	    
	    this.filterStore = new Ext.data.SimpleStore({
	        fields: ['value', 'label'],
	        data : [
	                ['like', 'contains'],
	                ['=', '=']
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
	    
	    this.doButton = this.addButton({
	        tooltip: 'apply filter'
	        //, iconCls: 'icon-filter'
	        , iconCls: 'icon-filter-funnel'
	        , disabled: false
	        , handler: this.onClick.createDelegate(this)
	    });
	    
	    this.unfilterButton = this.addButton({
	        tooltip: 'remove filter'
	        //, iconCls: 'icon-unfilter'
	        , iconCls: 'icon-remove'
	        , disabled: false
	        , handler: this.onUnfilter.createDelegate(this)
	    });

	    this.addFill();	    

	}

	, getFilterBarState: function(asObject) {
		var filterBarState = {};
		
		filterBarState.columnFilter = this.columnsFilterCombo.getValue();
		filterBarState.typeValueFilter = '';
		filterBarState.typeFilter = this.filterCombo.getValue();
		filterBarState.valueFilter = this.inputField.getValue();
		
		if(asObject !== undefined && asObject === false) {
			filterBarState = Ext.util.JSON.encode(filterBarState);
		}	

		return filterBarState;
	}
	
	, resetFilterBarState: function() {	
		this.columnsFilterCombo.reset();		
		this.filterCombo.reset();		
		this.inputField.reset();
	}
	
	, disableEditing: function() {		
		this.columnsFilterCombo.disable();		
		this.filterCombo.disable();		
		this.inputField.disable();
	}
	
	, enableEditing: function() {	
		this.columnsFilterCombo.enable();	
		this.filterCombo.enable();		
		this.inputField.enable();
	}

	, onClick: function() {

		if(this.state === this.EDITING) {

			this.unfilterButton.enable();
			this.disableEditing();
			
			this.state = this.FILTERING;		
			this.doLoad();					
		} else if(this.state === this.FILTERING) {

			this.enableEditing();
			this.state = this.EDITING;
		}

	}
	
	, onUnfilter: function() {
		this.unfilterButton.disable();
		this.resetFilterBarState();
		this.enableEditing();
		
		this.state = this.UNFILTERING;		
		this.doLoad();
	}
	
	, doLoad: function() {
		var p = Ext.apply({}, this.store.baseParams);		
		this.store.load({params: p});	
	}
	
	, beforeLoad: function(store, o) {
		if(this.state === this.FILTERING) {
			var p = this.getFilterBarState(false);
			o.params.FILTERS = p;
		} else if(this.state === this.UNFILTERING) {
			if(o.params.FILTERS !== undefined) 
				alert('ERROR: While UNFILTERING filters parameter cannot be valorized');
		} 
		
		return true;
	}
	
	, onLoad: function() {
		if(this.state === this.UNFILTERING) {
			this.state = this.EDITING;
		}
	}
	
	, bind: function(store) {
		store = Ext.StoreMgr.lookup(store);	    
	    store.on('beforeload', this.beforeLoad , this);
	    store.on("load", this.onLoad, this);
	    this.store = store;
	}
	
	, unbind : function(store){
        store = Ext.StoreMgr.lookup(store);        
        store.un('beforeload', this.beforeLoad , this);
        store.un("load", this.onLoad, this);
        this.store = undefined;
    }
	
	
});