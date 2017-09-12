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

Sbi.formbuilder.VariableEditorPanel = function(config) {
	
	var defaultSettings = {
		
		title: LN('sbi.formbuilder.variableeditorpanel.title')
		, emptyMsg: LN('sbi.formbuilder.variableeditorpanel.emptymsg')
		, filterItemName: 'grouping varaibles group'
		
		, layout: 'table'
	    , layoutConfig: {
	        columns: 100
	    }
		, enableDebugBtn: false
		, enableAddBtn: false	
		, enableClearBtn: false	
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.variableEditorPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.variableEditorPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	// constructor
    Sbi.formbuilder.VariableEditorPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.VariableEditorPanel, Sbi.formbuilder.EditorPanel, {
    
	wizard: null
	
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	, setContents: function(contents) {
		// the parent-class call this method so do not remove it.
		// It does nothings because the structure of this panel is fixed
	}

	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, init: function() {
		Sbi.formbuilder.VariableEditorPanel.superclass.init.call(this);
		var variable1GroupEditor, variable2GroupEditor;
		
		
		var bc;
		
		bc = (this.baseContents && this.baseContents.length > 0)? this.baseContents[0].admissibleFields: undefined;
		variable1GroupEditor = new Sbi.formbuilder.VariableGroupEditor({
			groupTitle: LN('sbi.formbuilder.variableeditorpanel.grouptitle') + ' 1',
			baseContents: bc
		});
		this.addFilterItem(variable1GroupEditor);
		
		bc = (this.baseContents && this.baseContents.length > 1)? this.baseContents[1].admissibleFields: undefined;
		variable2GroupEditor = new Sbi.formbuilder.VariableGroupEditor({
			groupTitle: LN('sbi.formbuilder.variableeditorpanel.grouptitle') + ' 2',
			baseContents: bc
		});
		this.addFilterItem(variable2GroupEditor);
		
	}	
	
	
	, onDebug: function() {
		
	}
	
	, getErrors: function() {
		var errors = [];
		var contents = this.getContents();
		var contents1 = contents[0].admissibleFields;
		var contents2 = contents[1].admissibleFields;
		if ( (contents1 === undefined || contents1 === null || contents1.length === 0) && // user specified variables for variable 2 but not for variable 1
				(contents2 != undefined && contents2 != null && contents2.length > 0) ) {
			errors.push(LN('sbi.formbuilder.variableeditorpanel.validationerrors.missingadmissiblefields') + ' 1');
		}
		return errors;
	}
  	
});