/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
 
Ext.ns("Sbi.xxx");


/**
 * Every time you create a new class add it to the following files:
 *  - importSbiJS.jspf
 *  - ant-files/SpagoBI-2.x-source/SpagoBIProject/ant/build.xml
 */
Sbi.xxx.Xxxx = function(config) {
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	// init properties...
	var defaultSettings = {
		// set default values here
	};
	
	if (Sbi.settings && Sbi.settings.xxx && Sbi.settings.xxx.xxxx) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.xxx.xxxx);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	// init events...
	this.addEvents();
	
	this.initServices();
	this.init();
	
	
	
	// constructor
    Sbi.xxx.Xxxx.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.xxx.Xxxx
 * @extends Ext.util.Observable
 * 
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.xxx.Xxxx, Ext.util.Observable, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
   
	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	/**
	 * @method 
	 * 
	 * Controls that the configuration object passed in to the class constructor contains all the compulsory properties. 
	 * If it is not the case an exception is thrown. Use it when there are properties necessary for the object
	 * construction for whom is not possible to find out a valid default value.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the config object received as input
	 */
	, validateConfigObject: function(config) {
		
	}

	/**
	 * @method 
	 * 
	 * Modify the configuration object passed in to the class constructor adding/removing properties. Use it for example to 
	 * rename a property or to filter out not necessary properties.
	 * 
	 * @param {Object} the configuration object passed in to the class constructor
	 * 
	 * @return {Object} the modified version config object received as input
	 * 
	 */
	, adjustConfigObject: function(config) {
		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - none
	 */
	, initServices: function() {
//		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
//		
//		this.services = this.services || new Array();
//		
//		this.services['exampleService'] = this.services['exampleService'] || Sbi.config.serviceRegistry.getServiceUrl({
//			serviceName: 'EXAMPLE_ACTION'
//			, baseParams: params
//		});	
	}


	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	, init: Ext.emptyFn
	
	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================
	
	//this.addEvents(
	/**
     * @event eventone
     * Fired when ...
     * @param {Sbi.xxx.Xxxx} this
     * @param {Ext.Toolbar} ...
     */
	//'eventone'
	/**
     * @event eventtwo
     * Fired before ...
     * @param {Sbi.xxx.Xxxx} this
     * @param {Object} ...
     */
	//'eventtwo'
	//);	
});