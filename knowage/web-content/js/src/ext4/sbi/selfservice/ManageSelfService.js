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
 * This class is the container for the self service interface 
 *    
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
  
Ext.define('Sbi.selfservice.ManageSelfService', {
	extend: 'Ext.tab.Panel',
//	extend: 'Ext.Panel',

    config: {
    	executionPanel: null,
    	datasetsServicePath : ''
    },
	

	/**
	 * @property {Panel} datasetPanelTab
	 *  Tab panel that contains the datasets
	 */
	datasetPanelTab: null,
	
	/**
	 * @property {Panel} modelstPanelTab
	 *  Tab panel that contains the models
	 *
	 * 
	 */
	modelsPanelTab: null
	,
	/**
	 * @property {Panel} smartFilterPanelTab
	 *  Tab panel that contains the smart filters
	 *
	 * 
	 */
	smartFilterPanelTab: null
	,
	constructor : function(config) {
		this.initConfig(config);
		
		this.layout = 'fit';
		
		var browserConf = {
			//title: LN("sbi.tools.dataset.datasetbrowser.title")
			  user: Sbi.user.userId
			, datasetsServicePath : config.datasetsServicePath
			, id: 'this'
			, qbeEditDatasetUrl : config.qbeEditDatasetUrl
			, isWorksheetEnabled: config.isWorksheetEnabled
			, typeDoc : config.typeDoc
			, fromMyAnalysis: config.fromMyAnalysis
			, userCanPersist: config.userCanPersist
			, tablePrefix: config.tablePrefix
			, ckanUrls: config.ckanUrls
		};
		
		if (Sbi.settings.browser.showTitle !== undefined && Sbi.settings.browser.showTitle){
			browserConf.title =  LN("sbi.tools.dataset.datasetbrowser.title");
		}
		this.items = [];
		
		
		if (Sbi.settings.mydata.showDataSetTab == true){
			this.datasetPanelTab = Ext.create('Sbi.tools.dataset.DataSetsBrowser', browserConf );
			this.items.push(this.datasetPanelTab);
		}
		if (Sbi.settings.mydata.showModelsTab == true){
			this.modelsPanelTab = Ext.create('Sbi.tools.model.MetaModelsBrowser',{title: LN("sbi.tools.model.metamodelsbrowser.title")});
			this.items.push(this.modelsPanelTab);
		}
		if (Sbi.settings.mydata.showSmartFilterTab == true){
			this.smartFilterPanelTab = Ext.create('Sbi.tools.document.SmartFilterBrowser',{title: LN("sbi.tools.document.smartfilterbrowser.title")});
			this.items.push(this.smartFilterPanelTab);
		}
		if (Sbi.settings.mydata.showFederatedDatasetTab == true){
			this.federatedDatasetTab = Ext.create('Sbi.tools.dataset.FederatedDatasetBrowser',{title: LN("sbi.tools.dataset.federateddatasetbrowser.title")});
			this.items.push(this.federatedDatasetTab);
		}		

		//this.items = [ this.datasetPanelTab,this.modelsPanelTab ];

		this.callParent(arguments);
		this.addEvents(
		        /**
		         * @event event1
		         * Execute the qbe clicking in the model/dataset
				 * @param {Object} docType engine to execute 'QBE'/'WORKSHEET'
				 * @param {Object} inputType 'DATASET'/'MODEL'
				 * @param {Object} record the record that contains all the information of the metamodel/dataset
		         */
		        'executeDocument'
				);
		if (Sbi.settings.mydata.showModelsTab == true){
			this.modelsPanelTab.on('executeDocument',function(docType, inputType, record){
				this.fireEvent('executeDocument',docType,inputType,record);
			},this);
		}
		if (Sbi.settings.mydata.showDataSetTab == true){
			this.datasetPanelTab.on('executeDocument',function(docType, inputType, record){
				this.fireEvent('executeDocument',docType,inputType,record);
			},this);
		}
		if (Sbi.settings.mydata.showSmartFilterTab == true){
			this.smartFilterPanelTab.on('executeDocument',function(docType, inputType, record){
				this.fireEvent('executeDocument',docType,inputType,record);
			},this);
		}
		if (Sbi.settings.mydata.showFederatedDatasetTab == true){
			this.federatedDatasetTab.on('executeDocument',function(docType, inputType, record){
				this.fireEvent('executeDocument',docType,inputType,record);
			},this);
		}
		
		if (Sbi.settings.mydata.showTabToolbar == false){
			this.getTabBar().setVisible(false);
		}
	}
    
    , openfederation: function(){
    	this.setActiveTab(this.federatedDatasetTab);
    }

    
	
});