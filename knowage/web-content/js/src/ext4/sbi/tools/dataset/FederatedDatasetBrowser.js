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
 * 
 * Container of the Federated Dataset catalogue.
 * If the user clicks on a Federated Dataset an execution of the associated engine will be started
 * 
 *  @author
 *  Giulio Gavardi (giulio.gavardi@eng.it)
 *  
 */
 
  
Ext.define('Sbi.tools.dataset.FederatedDatasetBrowser', {
	extend: 'Ext.Panel'
	
	, config: {
		modelName : "Sbi.tools.dataset.FederatedDatasetModel",
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
		         * Execute the qbe clicking in the model
				 * @param {Object} docType 'QBE'
				 * @param {Object} inputType 'MODEL'
				 * @param {Object} record the record that contains all the information of the metamodel
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
		this.viewPanel = Ext.create('Sbi.tools.dataset.FederatedDatasetView', config);
		this.viewPanel.on('executeDocument',function(docType, inputType,  record){
			this.fireEvent('executeDocument',docType, inputType,  record);
		},this);
		
	}

	
	

    
	
});