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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.formviewer");

Sbi.formviewer.GroupingVariablesPanel = function(groupingVariables, config) {
	
	var defaultSettings = {
		// set default values here
		title: LN('sbi.formviewer.groupingvariablespanel.title')
		, border: false
		, frame: true
		, autoScroll: true
		, autoWidth: true
		, autoHeight: true
		, layout: 'column'
    	, layoutConfig: {
	        columns: groupingVariables.length
	    }
		, style:'padding:10px'
	};
	if (Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.groupingVariablesPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.groupingVariablesPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});

	this.baseConfig = c;
	
	this.init(groupingVariables);
	
	Ext.apply(c, {
		items: this.items
	});
	
	// constructor
    Sbi.formviewer.GroupingVariablesPanel.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.formviewer.GroupingVariablesPanel, Ext.form.FormPanel, {
    
	services: null
	, items: null
	, combos: null
	   
	// private methods
	   
	, init: function(groupingVariables) {
		this.items = [];
		this.combos = new Array();
		for (var i = 0; i < groupingVariables.length; i++) {
			var combo = this.createFieldCombo( groupingVariables[i], i );
			this.combos.push( combo );
			var aPanel = new Ext.Panel({
				items: [combo]
				, layout: 'form' // form layout required: input field labels are displayed only with this layout
				, width: 300
			});
			this.items.push( aPanel );
		}
	}

	, createFieldCombo: function(groupingVariable, pos) {
		
		var store = new Ext.data.JsonStore({
			data: groupingVariable.admissibleFields,
		    fields: ['field', 'text']
		});
		
		var combo = new Ext.form.ComboBox({
			name: groupingVariable.id
            , editable: false
            , fieldLabel: LN('sbi.formviewer.groupingvariablespanel.variable-' + (pos + 1))
		    , forceSelection: true
		    , store: store
		    , mode : 'local'
		    , triggerAction: 'all'
		    , displayField: 'text'
		    , valueField: 'field'
		    , emptyText: ''
		});
		combo.setValue(groupingVariable.admissibleFields[0].field);

		return combo;
	}
	   
	// public methods
	
	, getFormState: function() {
		var state = {};
		for (var i = 0; i < this.combos.length; i++) {
			var aCombo = this.combos[i];
			state[aCombo.name] = aCombo.getValue();
		}
		return state;
	}
		
	, setFormState: function(values) {
		for(var j in values){
			for (var i = 0; i < this.combos.length; i++) {
				var aCombo = this.combos[i];
				if(j == aCombo.name){
					aCombo.setValue(values[j]);
					break;
				}
			}
		}
	}
  	
});