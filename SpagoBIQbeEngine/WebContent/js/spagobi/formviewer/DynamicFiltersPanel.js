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

Sbi.formviewer.DynamicFiltersPanel = function(dynamicFilters, config) {
	
	var defaultSettings = {
		// set default values here
		//title: LN('sbi.formviewer.dynamicfilterspanel.title'),
		autoScroll: true
		, frame: true
		, autoHeight: true
		, style:'padding:10px'
	};
	if (Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.dynamicFiltersPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.dynamicFiltersPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});

	this.init(dynamicFilters);
	
	Ext.apply(c, {
        items: this.items
	});
	
	// constructor
    Sbi.formviewer.DynamicFiltersPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.formviewer.DynamicFiltersPanel, Ext.Panel, {
    
	services: null
	, dynamicFilters: new Array()
	   
	// private methods
	   
	, init: function(dynamicFiltersConfig) {
		this.items = [];
		for(var i = 0; i < dynamicFiltersConfig.length; i++) {
			var aDynamicFilter = new Sbi.formviewer.DynamicFilter(dynamicFiltersConfig[i]);
			this.items.push(aDynamicFilter);
			this.dynamicFilters.push(aDynamicFilter);
		}
	}

	// public methods
	
	, getFormState: function() {
		var state = {};
		for(var i = 0; i < this.dynamicFilters.length; i++) {
			var aDynamicFilter = this.dynamicFilters[i];
			var aDynamicFilterState = aDynamicFilter.getFormState();
			state[aDynamicFilter.id] = aDynamicFilterState;
		}
		return state;
	}

	, setFormState: function(fields) {
		for(var j in fields){
			for(var i = 0; i < this.dynamicFilters.length; i++) {
				var aDynamicFilter = this.dynamicFilters[i];
				if(aDynamicFilter.id == j){
					aDynamicFilter.setFormState(fields[j]);
				}
			}
		}
	}
	
	, getErrors: function() {
		var errors = new Array();
		for(var i = 0; i < this.dynamicFilters.length; i++) {
			var aDynamicFilter = this.dynamicFilters[i];
			if (!aDynamicFilter.isValid()) {
				var validationErrors = aDynamicFilter.getValidationErrors();
				errors = errors.concat(validationErrors);
			}
		}
		return errors;
	}
	
});