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

Ext.ns("Sbi.execution");

Sbi.execution.RoleSelectionPage = function(config, doc) {
	
	// apply defaults values
	config = Ext.apply({
		// no defaults
	}, config || {});
	
	// check mandatory values
	// ...
		
	// declare exploited services
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
	this.services = new Array();
	this.services['getRolesForExecutionService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_ROLES_FOR_EXECUTION_ACTION'
		, baseParams: params
	});
	
	// add events
    this.addEvents('beforetoolbarinit', 'beforesynchronize', 'synchronize', 'synchronizeexception', 'movetonextpagerequest', 'movetoprevpagerequest', 'ready', 'movetoadminpagerequest');
             
	// init component
	this.init();
   
    
	// invoke parent constructor constructor
	var c = Ext.apply({}, config, {
		bodyStyle:'padding:16px 16px 16px 16px;'
		, border : false
		, tbar: this.toolbar
		, items :[this.roleComboBox]
		, listeners: {
		    'render': {
	        	fn: function() {
	      	 		this.loadingMask = new Sbi.decorator.LoadMask(this.body, {msg:LN('sbi.execution.roleselection.loadingmsg')}); 
	        	},
	        	scope: this
	      	}
	    } 
	});   	
	
    Sbi.execution.RoleSelectionPage.superclass.constructor.call(this, c);
};


/**
 * @class Sbi.execution.RoleSelectionPage
 * @extends Ext.FormPanel
 * ...
 */
Ext.extend(Sbi.execution.RoleSelectionPage, Ext.FormPanel, {
	
	// ---------------------------------------------------------------------------
    // object's members
	// ---------------------------------------------------------------------------
	
	services: null
	, executionInstance: null
	
	, toolbar: null
	
	, store: null
	, roleComboBox: null
	
	, loadingMask: null
	   
	// ---------------------------------------------------------------------------
    // public methods
	// ---------------------------------------------------------------------------
	
	, synchronize: function( executionInstance ) {	
	 	if(this.fireEvent('beforesynchronize', this, executionInstance, this.executionInstance) !== false){
	 		this.executionInstance = executionInstance;
	 		this.synchronizeToolbar( executionInstance );
	 		this.store.load({params: executionInstance});
	 	}
	}

	, synchronizeToolbar: function( executionInstance ){
		
		this.toolbar.items.each( function(item) {
			this.toolbar.items.remove(item);
            item.destroy();           
        }, this); 
		
		this.fireEvent('beforetoolbarinit', this, this.toolbar);
		
//		 if (Sbi.settings.browser.typeLayout == undefined || Sbi.settings.browser.typeLayout == 'tab') 
			 this.toolbar.addFill();
		
		
						// 20100505
		if (this.callFromTreeListDoc == true) {
			this.toolbar.addButton(new Ext.Toolbar.Button({
				iconCls: 'icon-back' 
				, tooltip: LN('sbi.execution.executionpage.toolbar.documentView')
				, scope: this
				, handler : function() {
					this.fireEvent('movetoadminpagerequest');
				}
			}));
		}
		
		
		this.toolbar.addButton(new Ext.Toolbar.Button({
			iconCls: 'icon-execute'
			, tooltip: LN('sbi.execution.roleselection.toolbar.next')
			, scope: this
			, handler : function() {
				this.fireEvent('movetonextpagerequest', this, this.getSelectedRole());
			}
		}));
	}

	, getSelectedRole: function() {
		return this.roleComboBox.getValue();
	}
	
	, setSelectedRole: function( role ) {
		this.roleComboBox.setValue( role );
	}
	
	//---------------------------------------------------------------------------
	// private methods
	// ---------------------------------------------------------------------------

	, init: function( config ) {
		
		this.initToolbar( config );
		 	
		this.initStore( config );
			
		this.roleComboBox = new Ext.form.ComboBox({
			tpl: '<tpl for="."><div ext:qtip="{name}: {description}" class="x-combo-list-item">{name}</div></tpl>',	
		    editable  : false,
		    fieldLabel : LN('sbi.execution.roleselection.fieldlabel'),
		    forceSelection : true,
		    mode : 'local',
		    name : 'typeFilter',
		    store : this.store,
		    displayField:'name',
		    valueField:'name',
		    emptyText: LN('sbi.execution.roleselection.emptytext'),
		    typeAhead: true,
		    triggerAction: 'all',
		    selectOnFocus:true
		});	
		
		 this.roleComboBox.on('beforeselect', function(combo, record, index) {
			 if(this.roleComboBox.isDirty() === false) {
				 this.fireEvent('ready', this, record.data.name);
			 }		    
		 }, this);
	}
	
	, initStore: function( config ) {
		this.store = new Ext.data.Store({
			proxy: new Ext.data.HttpProxy({
					url: this.services['getRolesForExecutionService']
			})
			   
		   	, reader: new Ext.data.JsonReader() 
	    });  
	    
	    this.store.on('load', function(s, records, options) {
	    	this.fireEvent('synchronize', this, s, records, options);
	    }, this);
	    
	    this.store.on('loadexception', function(store, options, response, e) {
			Sbi.exception.ExceptionHandler.handleFailure(response, options);
			this.fireEvent('synchronizeexception', this, store, options, response, e);
		}, this);	    
	}
	
	, initToolbar: function( config ) {
		this.toolbar = new Ext.Toolbar({
			items: ['']
		});
	}
});