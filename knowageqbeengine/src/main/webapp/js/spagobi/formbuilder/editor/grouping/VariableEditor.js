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

Sbi.formbuilder.VariableEditor = function(config) {
	
	var defaultSettings = {
		editable: false
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.variableEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.variableEditor);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	c.uniqueName = c.id || c.field || 'not defined';
	if(c.id) delete c.id;
	if(c.field) delete c.field;
	
	c.alias = c.alias || c.text || 'not defined';
	delete c.text;
	
	Ext.apply(this, c);
	
	
	// constructor
    Sbi.formbuilder.VariableEditor.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.VariableEditor, Sbi.formbuilder.InlineEditor, {
	
	uniqueName: null
	, alias: null
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	, setContents: function(c) {
	
		this.filedConf = this.filedConf || {};
		
		if(c.id || c.field) this.uniqueName = c.id || c.field;
		if(c.alias || c.text) this.alias = c.alias || c.text;
	
	}
	
	, getContents: function() {
		var c = {};
		
		c.field = this.uniqueName;
		c.text = this.alias;
		
		return c;
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, init: function() {
		this.filter = new Ext.Panel({
			html: this.alias
		});
	}
	
	
});