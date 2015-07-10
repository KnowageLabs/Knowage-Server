/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Container of the smart filters catalogue.
 * If the user clicks on a smart filter an execution of the associated engine will be started
 * 
 *  @author
 *  Alessandro Portosa (alessandro.portosa@eng.it)
 *  
 */
 
  
Ext.define('Sbi.tools.document.SmartFilterBrowser', {
	extend: 'Ext.Panel'
	
	, config: {
		modelName : "Sbi.tools.document.SmartFilterModel",
		autoScroll:true
    }


	, viewPanel: null
	
	, constructor : function(config) {
		this.initConfig(config);
		this.initStore();
		this.initViewPanel();
		this.items=[this.viewPanel];
		this.callParent(arguments);
		this.addEvents(
		        /**
		         * @event event1
		         * Execute the document by clicking in the smart filter
				 * @param {Object} docType 'WORKSHEET'
				 * @param {Object} inputType 'SMART_FILTER'
				 * @param {Object} record the record that contains all the information of the smart filter
		         */
		        'executeDocument'
				);
	}
	
	,
	initStore : function(baseParams) {
		var model = Ext.ModelMgr.getModel(this.getModelName());

		this.filteredProperties = [ "name" ];
		
		Sbi.debug('DataViewPanel bulding the store...');

		this.storeConfig = Ext.apply({
			model : this.getModelName(),
			filteredProperties : [ "name" ]
		}, {});

		// creates and returns the store
		Sbi.debug('DataViewPanel store built.');

		this.store = Ext.create('Sbi.widgets.store.InMemoryFilteredStore', this.storeConfig);				
		this.store.load();
		
	}
	
	,
	initViewPanel: function() {
		var config = {};
		config.store = this.store;
		this.viewPanel = Ext.create('Sbi.tools.document.SmartFilterView', config);
		this.viewPanel.on('executeDocument',function(docType, inputType,  record){
			this.fireEvent('executeDocument',docType, inputType,  record);
		},this);
		
	}

	
	

    
	
});