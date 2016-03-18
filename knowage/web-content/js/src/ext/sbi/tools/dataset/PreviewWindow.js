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