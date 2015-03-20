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

Ext.ns("Sbi.formviewer");

Sbi.formbuilder.VariableGroupEditor = function(config) {
	

	var defaultSettings = {	
		groupTitle: LN('sbi.formbuilder.variablegroupeditor.grouptitle')
		, width: 300
        , height: 150
        , autoWidth: false    	
        , emptyMsg: LN('sbi.formbuilder.variablegroupeditor.emptymsg')
        , ddGroup    : 'formbuilderDDGroup'
        , droppable: {
			onFieldDrop: this.addField
		} 
		
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.variableGroupEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.variableGroupEditor);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	Ext.apply(c, {
		filterTitle: this.groupTitle
		, filterFrame: true
		, header: false
	});
	
	// constructor
	Sbi.formbuilder.VariableGroupEditor.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.VariableGroupEditor, Sbi.formbuilder.EditorPanel, {
    

	wizard: null
	
	, groupTitle: null
	
	//--------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
	, setContents: function(contents) {
		for(var i = 0, l = contents.length; i < l; i++) {
			this.addField(contents[i]);
		}		
	}
	
	, getContents: function() {
		var c = {};
		
		c.admissibleFields = Sbi.formbuilder.VariableGroupEditor.superclass.getContents.call(this)
		
		return c;
	}
	
	, addField: function(fieldConf) {
		
		var field = new Sbi.formbuilder.VariableEditor(fieldConf);				
		this.addFilterItem(field);
		
		field.on('actionrequest', function(action, field) {
			if(action === 'edit') {
				this.editFilter(field);
			} else if(action === 'delete') {
				this.deleteField(field);
			}
		}, this);
	}
	
	, deleteField: function(f) {
		f.destroy();
		//this.remove(f, true);
	}
	
	, editFilter: function(f) {
		alert('Error: "editFilter" unimlpemented');
		//this.onFilterWizardShow(f)
	}
	

	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	/*
	, onFilterWizardShow: function(targetFilter) {
		if(this.wizard === null) {
			this.wizard = new Sbi.formbuilder.StaticCloseFilterWizard();
			this.wizard.on('apply', function(win, target, state) {
				if(target === null) {
					this.addFilter(state);
				} else {
					target.setContents(state);
				}
				
			}, this);
		}
		
		this.wizard.setTarget(targetFilter || null);		
		this.wizard.show();
	}
	*/
	
});