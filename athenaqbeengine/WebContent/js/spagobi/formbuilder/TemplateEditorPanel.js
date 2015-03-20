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

Sbi.formbuilder.TemplateEditorPanel = function(config) {
	
	var defaultSettings = {
		title: LN('sbi.formbuilder.templateeditorpanel.title')
	};
		
	if(Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.filtersTemplatePanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.filtersTemplatePanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
		
	/*
	this.services = this.services || new Array();	
	this.services['doThat'] = this.services['doThat'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DO_THAT_ACTION'
		, baseParams: new Object()
	});
	
	this.addEvents('customEvents');
	*/
		
	c = Ext.apply(c, {
		title: this.title,
		border: true,
		//bodyStyle:'padding:10px',
      	layout: 'fit',      	
      	items: [{
    	    html: 'destroy me please'
    	}]
	});

	// constructor
	Sbi.formbuilder.TemplateEditorPanel.superclass.constructor.call(this, c);
	
	this.on('afterlayout', this.init, this);
};

Ext.extend(Sbi.formbuilder.TemplateEditorPanel, Ext.Panel, {
    
	services: null
    , staticCloseFilterEditorPanel: null
    , staticOpenFilterEditorPanel: null
    , dynamicFiltersEditorPanel: null
    , groupingVariablesEditorPanel: null
   
    
    // public methods
    
    , init: function() {
		//alert('In');
		
		this.removeListener('afterlayout', this.init, this);
				
		this.remove(0);
		this.doLayout(true);
		
		var bc;
		
		bc = (this.template !== undefined)? this.template.staticClosedFilters : undefined;
		this.staticCloseFilterEditorPanel = new Sbi.formbuilder.StaticCloseFilterEditorPanel({
			style:'padding:10px',
			baseContents: bc
		});
		this.add(this.staticCloseFilterEditorPanel);
		
		bc = (this.template !== undefined) ? this.template.staticOpenFilters : undefined
		this.staticOpenFilterEditorPanel = new Sbi.formbuilder.StaticOpenFilterEditorPanel({
			style:'padding:10px',
			baseContents: bc
		});
		this.add(this.staticOpenFilterEditorPanel);
		
		bc = (this.template !== undefined) ? this.template.dynamicFilters : undefined
		this.dynamicEditorPanel = new Sbi.formbuilder.DynamicFilterEditorPanel({
			style:'padding:10px',
			baseContents: bc
		});
		this.add(this.dynamicEditorPanel);
		
		bc = (this.template !== undefined) ? this.template.groupingVariables : undefined
		this.groupingVariablesPanel = new Sbi.formbuilder.VariableEditorPanel({
			style:'padding:10px',
			baseContents: bc
		});
		this.add(this.groupingVariablesPanel);
		
		this.doLayout(true);
	}

	, getContents: function() {
		var state = {};
		
		if (this.staticCloseFilterEditorPanel) {
			state.staticClosedFilters = this.staticCloseFilterEditorPanel.getContents();
		}
		if (this.staticOpenFilterEditorPanel) {
			state.staticOpenFilters = this.staticOpenFilterEditorPanel.getContents();
		}
		if (this.dynamicEditorPanel) {
			state.dynamicFilters = this.dynamicEditorPanel.getContents();
		}
		if (this.groupingVariablesPanel) {
			state.groupingVariables = this.groupingVariablesPanel.getContents();
		}
		
		return state;
	}
    
});