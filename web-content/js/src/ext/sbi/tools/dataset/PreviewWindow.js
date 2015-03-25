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
Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.PreviewWindow = function(config) {
	
	var baseParams = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', MESSAGE_DET: "DATASET_TEST"};
	
	var defaultSettings = {
		title: LN("sbi.tools.dataset.previewwindow.title")
		, closeAction: 'hide'		
		, plain: true
		, width: 800
		, height: 500
	};
	 
	if(Sbi.settings && Sbi.settings.tools && Sbi.settings.tools.dataset && Sbi.settings.tools.dataset.previewWindow) {
	   defaultSettings = Ext.apply(defaultSettings, Sbi.settings.tools.dataset.previewWindow);
	}
	 
	var c = Ext.apply(defaultSettings, config || {});
	 
	Ext.apply(this, c);
	
	this.services = this.services || new Array();	
	this.services['loadDataStore'] = this.services['loadDataStore'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MANAGE_DATASETS_ACTION'
		, baseParams: this.baseParams || baseParams
	});
	
	this.init();
	
	c = Ext.apply(c, {
		layout:'fit'
		, items: [this.dataStore]
	});

    // constructor
    Sbi.tools.dataset.PreviewWindow.superclass.constructor.call(this, c);
    
	this.addEvents();
    
};

Ext.extend(Sbi.tools.dataset.PreviewWindow, Ext.Window, {
	
	services	:	null
	, dataStore	:	null
	, dataStoreConfig : null
	
	,
	init : function() {
		this.dataStoreConfig = {
			header: false,
			frame: false, 
		    border: false,
		    displayInfo: true,
		    pageSize: 25,		    
		    sortable: true,
		    services: {
		    	loadDataStore : this.services['loadDataStore']
		    }
		};
		this.dataStore = new Sbi.widgets.DataStorePanel(this.dataStoreConfig);
	}
	
	,
	load : function(parameters) {
		this.dataStore.execQuery(parameters);
	}
	
});