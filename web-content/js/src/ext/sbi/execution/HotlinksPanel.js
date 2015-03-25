/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
 
Ext.ns("Sbi.execution");


/**
 * Every time you create a new class add it to the following files:
 *  - importSbiJS.jspf
 *  - ant-files/SpagoBI-2.x-source/SpagoBIProject/ant/build.xml
 */
Sbi.execution.HotlinksPanel = function(config) {
	
	this.validateConfigObject(config);
	this.adjustConfigObject(config);
	
	// init properties...
	var defaultSettings = {
		// set default values here
		layout: 'fit'
		//, bodyStyle: {'background-color':'yellow'}
		//, html: 'Hotlinks panel'
	};
	
	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.hotlinkspanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.hotlinkspanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	// init events...
	this.addEvents('select');
	
	this.initServices();
	this.init();
	
	c.items = [this.grid];
	
	// constructor
	Sbi.execution.HotlinksPanel.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.execution.HotlinksPanel
 * @extends Ext.util.Observable
 * 
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.execution.HotlinksPanel, Ext.Panel, {
    
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
		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
		
		this.services = this.services || new Array();
		
		this.services['getFavouritesService'] = this.services['getFavouritesService'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_FAVOURITES_ACTION'
			, baseParams: params
		});	
	}


	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	, init: function() {
		this.initStore();	
		this.initGrid();
	}
	
	, initStore: function() {
		this.store = new Ext.data.JsonStore({
	        root: 'results'
	        , idProperty: 'id'
	        , fields: [
	           {name: 'name'},
	           {name: 'description'},
	           {name: 'documentLabel'},
	           {name: 'documentName'},
	           {name: 'documentDescription'},
	           {name: 'documentType'}
	        ]
			, url: this.services['getFavouritesService']
			, autoLoad : true
	    });
	}
	
	, initGrid: function() {
		this.grid = new Ext.grid.GridPanel({
			store: this.store,
	        columns: [
	            {header: LN('sbi.hotlinks.name'), sortable: true, dataIndex: 'name'},
	            {header: LN('sbi.hotlinks.document'), sortable: true, dataIndex: 'documentLabel'},
	            {header: LN('sbi.hotlinks.document.name'), sortable: true, dataIndex: 'documentName'},
	            {header: LN('sbi.hotlinks.document.description'), sortable: true, dataIndex: 'documentDescription'},
	            {header: LN('sbi.hotlinks.document.type'), sortable: true, dataIndex: 'documentType'}
	        ],
			viewConfig: {
	        	forceFit: true
			},
	        stripeRows: true,
	        collapsible: false,
	        //autoExpandColumn: 'Document',
	        //height:200,
	        //width:700,
	        loadMask: false
	    });
	    
		this.grid.on(
			'rowclick', function(grid, rowIndex, e) {
				this.fireEvent('select', {
					label: this.store.getAt(rowIndex).get('documentLabel')
					, name: this.store.getAt(rowIndex).get('documentName')
					, description: this.store.getAt(rowIndex).get('documentDescription')
					, type: this.store.getAt(rowIndex).get('documentType')
				});
				//location.href = storeRememberMe.getAt(rowIndex).get('Url');
			}, this
		);
	}
	
	, synchronize: function() {
		this.store.load();
	}
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