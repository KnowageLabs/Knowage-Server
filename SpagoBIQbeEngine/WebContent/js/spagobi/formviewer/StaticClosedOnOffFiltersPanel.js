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

Sbi.formviewer.StaticClosedOnOffFiltersPanel = function(aStaticClosedOnOffFiltersGroup, config) {
	
	var defaultSettings = {
		// set default values here
		frame: true
		, autoScroll: true
		//, autoWidth: true
		, autoHeight: true
        //, width: aStaticClosedOnOffFiltersGroup.width || 300
        //, height: aStaticClosedOnOffFiltersGroup.height || 150
	};
	if (Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.staticClosedOnOffFiltersPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.staticClosedOnOffFiltersPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	this.init(aStaticClosedOnOffFiltersGroup);
	
	Ext.apply(c, {
		id: aStaticClosedOnOffFiltersGroup.id
		, items: this.items
	});
	
	// constructor
    Sbi.formviewer.StaticClosedOnOffFiltersPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.formviewer.StaticClosedOnOffFiltersPanel, Ext.form.FormPanel, {
    
	items: null
	
	// private methods
	
	, init: function(aStaticClosedOnOffFiltersGroup) {
		
		var title = aStaticClosedOnOffFiltersGroup.title;
		// if a title is specified, a fieldset is created
		if (title !== undefined && title !== null && title.trim() !== '') {
			
			this.items = {
		            xtype: 'fieldset',
		            title: aStaticClosedOnOffFiltersGroup.title,
		            name: aStaticClosedOnOffFiltersGroup.id,
		            autoHeight: true,
		            autoWidth: true,
		            defaultType: 'checkbox',
		            items: []
		    }
			
			for (var i = 0; i < aStaticClosedOnOffFiltersGroup.options.length; i++) {
				// create items
				var anOption = aStaticClosedOnOffFiltersGroup.options[i];
				this.items.items.push({
					hideLabel: true,
	                boxLabel: anOption.text,
	                name: anOption.id
				});
			}
			
		} else {
			
			this.items = [];
			
			for (var i = 0; i < aStaticClosedOnOffFiltersGroup.options.length; i++) {
				// create items
				var anOption = aStaticClosedOnOffFiltersGroup.options[i];
				this.items.push({
					xtype: 'checkbox',
					hideLabel: true,
		            boxLabel: anOption.text,
		            name: anOption.id
				});
			}
		}
		
	}
	
	// public methods
	
	, getFormState: function() {
		var state = this.getForm().getValues();
		return state;
	}
  	
	, setFormState: function(values) {
		this.getForm().setValues(values);
	}
	
	
});