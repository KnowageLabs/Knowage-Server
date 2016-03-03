/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

Ext.ns("Sbi.xxx");

//@see https://github.com/senchalabs/jsduck/wiki/Guide

/**
 * Every time you create a new class add it to the following files:
 *  - importSbiJS.jspf
 *  - ant-files/SpagoBI-2.x-source/SpagoBIProject/ant/build.xml
 */
Sbi.xxx.Xxxx = function(config) {

	this.adjustConfigObject(config);
	this.validateConfigObject(config);


	// init properties...
	var defaultSettings = {
		// set default values here
	};

	var settings = Sbi.getObjectSettings('Sbi.xxx.Xxxx', defaultSettings);

	var c = Ext.apply(settings, config || {});
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
//		this.services['exampleService'] = this.services['exampleService'] || Sbi.config.serviceReg.getServiceUrl('loadDataSetStore', {
//			pathParams: {datasetLabel: this.getStoreId()}
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