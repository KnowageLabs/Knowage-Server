/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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

    
	
});