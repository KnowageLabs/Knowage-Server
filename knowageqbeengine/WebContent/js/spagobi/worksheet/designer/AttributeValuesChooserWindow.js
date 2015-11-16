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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.AttributeValuesChooserWindow = function(config) {
	
	if (!(config && config.attribute)) {
		throw "The input object must contain the attribute property";
	}
	
	var defaultSettings = {
		singleSelect : false
	};
	
	var c = Ext.apply(defaultSettings, config || {});
	
	// The following instruction creates mainly:
	//this.attribute = c.attribute : the json object representing the attribute: it must be in the constructor input object
	//this.params = c.params : the json object with the parameters for store loading: it must be in the constructor input object
	//this.startValues = c.startValues : the initial values to be selected
	Ext.apply(this, c);
	
	var service_params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
	
	this.services = this.services || new Array();
	
	this.services['getValues'] = this.services['getValues'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_VALUES_FOR_CROSSTAB_ATTRIBUTES_ACTION'
		, baseParams: service_params
	});
	
	this.store = new Ext.data.JsonStore({
		url: this.services['getValues']  + '&ALIAS=' + this.attribute.alias + '&ENTITY_ID=' + this.attribute.id
	});
	c.store = this.store;

	this.store.on('loadexception', function(store, options, response, e) {
		Sbi.exception.ExceptionHandler.handleFailure(response, options);
	});

	// parameters for store loading
	var params = this.params
	
	var p = Ext.apply({}, params, {
		start: this.start
		, limit: this.limit
	});
	
	c.params = p;
	
//	// add selection values
//	if (this.attribute.values) {
//		c.startValues = this.attribute.values; 	
//	}
	
	// constructor
	Sbi.worksheet.designer.AttributeValuesChooserWindow.superclass.constructor.call(this, c);

//	// set first selection
//	this.setSelection(Ext.decode(this.attribute.values));
 	
	this.show(this);

	this.store.load({params: p});
};

Ext.extend(Sbi.worksheet.designer.AttributeValuesChooserWindow, Sbi.widgets.FilterLookupPopupWindow, {

    start: 0 
    , limit: 20
	, attribute 	: null // the json object representing the attribute: it must be in the constructor input object
	, params : null // the json object with the parameters for store loading: it must be in the constructor input object
	
});