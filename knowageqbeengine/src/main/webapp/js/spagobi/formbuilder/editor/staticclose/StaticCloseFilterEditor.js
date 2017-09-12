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

Sbi.formbuilder.StaticCloseFilterEditor = function(config) {
	
	var defaultSettings = {
		//style: 'border:1px solid #ccc !important;'
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticCloseFilterEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticCloseFilterEditor);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	this.baseConfig = config;
	
	Ext.apply(this, c);
	
	// constructor
    Sbi.formbuilder.StaticCloseFilterEditor.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.StaticCloseFilterEditor, Sbi.formbuilder.InlineEditor, {
    
	text: null
	, expression: null
	, filters: null
	, baseConfig: null
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	, setContents: function(c) {
		if(this.text !== c.text) {
			this.filter.setBoxLabel(c.text);
			//alert('filter name is changed!');
		}
		this.text = c.text;
		this.filters = c.filters;
		this.expression = c.expression;
	}
	
	, getContents: function() {
		var c = {};
		c.text = this.text;
		c.filters = this.filters;
		c.expression = this.expression;
		return c;
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, init: function() {
		var filterConf = {
			width: 148
			, hideLabel: true
			, boxLabel: this.text
	        , name: 'options'
	        , inputValue: 'option'
	        //, style: 'background: red'
	        //, bodyStyle: 'background: red'
		};
		
		if(this.singleSelection === true) {
			this.filter = new Ext.form.Radio(filterConf);
		} else {
			this.filter = new Ext.form.Checkbox(filterConf);
		}
	}
});