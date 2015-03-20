/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.execution");

Sbi.execution.InfoPage = function(config, doc) {
	
	// init properties...
	var defaultSettings = {
		// set default values here
		bodyStyle: {padding: "50px"} 
		, tablecellpadding: "10"
	};

	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.infopage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.infopage);
	}
	this.document= doc;
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	// init events...
	this.addEvents();
	this.initServices();
	this.init();
	
	// constructor
    Sbi.execution.InfoPage.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.execution.InfoPage
 * @extends Ext.Panel
 * 
 * It shows information related to executed document
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.execution.InfoPage, Ext.Panel, {
    
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
	, init: function() {
		

	/*
	 * document: {
	 * 	"id":5,
	 * 	"label":"DOC0004",
	 * 	"name":"Combo Simple",
	 *  "description":"Combo simple test",
	 *  "typeCode":"REPORT",
	 *  "typeId":6,
	 *  "encrypt":0,
	 *  "visible":1,
	 *  "profiledVisibility":"",
	 *  "engine":"Jasper Report Engine",
	 *  "engineid":10,
	 *  "datasource":1,
	 *  "uuid":"e1340fb5-6958-11e2-92cc-f9e75bf56606",
	 *  "relname":"",
	 *  "stateCode":"REL",
	 *  "stateId":41,
	 *  "functionalities":[13,15],
	 *  "creationDate":"2013-01-28 15:42:03.086",
	 *  "creationUser":"biadmin",
	 *  "refreshSeconds":0,
	 *  "actions":[{"name":"showmetadata","description":"Show Metadata"}],"exporters":["PDF","XLS","RTF","JPG"],"decorators":{"isSavable":true}}
						 */
		var tpl = new Ext.XTemplate(
				
			    '<table class="document-table-info">',
			    '<tr>',
			    ' <td rowspan="6" class="document-table-info-icon">',
			    	'<div class="group-view">',
			    		'<div class="document-item-icon">',
			    			'<img src="' + Ext.BLANK_IMAGE_URL + '" class="{typeCode}-icon"></img></td>',
			    		'</div>',
			    	'</div>',
			    ' </td>',
			    ' <td class="document-table-info-title">' + LN('sbi.generic.label') + ':</td>',
			    ' <td class="document-table-info-content">{label}</td>',
			    '</tr>',
			    '<tr>',
			    ' <td class="document-table-info-title">' + LN('sbi.generic.name') + ':</td>',
			    ' <td class="document-table-info-content">{name}</td>',
			    '</tr>',
			    '<tr>',
			    ' <td class="document-table-info-title">' + LN('sbi.generic.descr') + ':</td>',
			    ' <td class="document-table-info-content">{description}</td>',
			    '</tr>',
			    '<tr>',
			    ' <td class="document-table-info-title">' + LN('sbi.generic.type') + ':</td>',
			    ' <td class="document-table-info-content">{typeCode}</td>',
			    '</tr>',
			    '<tr>',
			    ' <td class="document-table-info-title">' + LN('sbi.generic.creationdate') + ':</td>',
			    ' <td class="document-table-info-content">{creationDate}</td>',
			    '</tr>',
			    '<tr>',
			    ' <td class="document-table-info-title">' + LN('sbi.generic.author') + ':</td>',
			    ' <td class="document-table-info-content">{creationUser}</td>',
			    '</tr>',
			    '</table>'
			);
		//tpl.overwrite(panel.body, data);
		this.html = tpl.applyTemplate(this.document);
		
		Sbi.trace('[InfoPage.init]: document: ' + Sbi.toSource(this.document));
		Sbi.trace('[InfoPage.init]: executionInstance: ' + Sbi.toSource(this.executionInstance));
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // synchronization methods
	// -----------------------------------------------------------------------------------------------------------------
	// This methods change properly the interface according to the specific execution instance passed in
	, synchronize: function( executionInstance ) {
		
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
     * @param {Sbi.execution.InfoPage} this
     * @param {Ext.Toolbar} ...
     */
	//'eventone'
	/**
     * @event eventtwo
     * Fired before ...
     * @param {Sbi.execution.InfoPage} this
     * @param {Object} ...
     */
	//'eventtwo'
	//);	
});