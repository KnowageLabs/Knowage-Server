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
 
  
Ext.define('Sbi.selfservice.ManageSelfServiceContainer', {
	extend: 'Ext.panel.Panel',

	congig:{
    	worksheetEngineBaseUrl : '',
    	qbeFromBMBaseUrl : '',
    	qbeFromDataSetBaseUrl : '',
        georeportEngineBaseUrl :  '',
        user : '',
        datasetsServicePath: '',
        qbeEditDatasetUrl : '',
        typeDoc: '',
        userCanPersist: '',
        tablePrefix:''
	},

	/**
	 * @property {Panel} manageSelfService
	 *  Tab panel that contains the datasets and the model views
	 */
    manageSelfService: null,
	
	/**
	 * @property {Panel} documentexecution
	 *  Tab panel that contains the execution of the engine
	 */
    documentexecution: null
	
	, constructor : function(config) {
		this.initConfig(config);
		
		this.layout =  'card';
		
		this.fromMyAnalysis = config.fromMyAnalysis;
		this.fromDocBrowser = config.fromDocBrowser;
		this.contextName = config.contextName;
		
		this.documentexecution = Ext.create('Sbi.selfservice.SelfServiceExecutionIFrame',{
			fromMyAnalysis: this.fromMyAnalysis,
			fromDocBrowser: this.fromDocBrowser,
			contextName: this.contextName
		}); 
		this.manageSelfService = Ext.create('Sbi.selfservice.ManageSelfService', {
			selfServiceContainer : this
			, datasetsServicePath : config.datasetsServicePath
			, qbeEditDatasetUrl : config.qbeEditDatasetUrl
			, typeDoc : config.typeDoc
			, userCanPersist: config.userCanPersist
			, tablePrefix: config.tablePrefix			
		}); 
		
					
		this.items = [ this.manageSelfService, this.documentexecution]
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
		this.manageSelfService.on('executeDocument', this.executeDocument ,this);

	}

	, executeDocument: function(docType,inputType, record){
		if( docType == 'QBE' ) {
			this.executeQbe(inputType, record);
		} else if ( docType == 'WORKSHEET' ) {
			this.executeWorksheet(inputType, record);
		} else if( docType == 'GEOREPORT' ) {
			this.executeGeoreport(inputType, record);
		} else {
			alert('Impossible to execute document of type [' + docType + ']');
		}
		
		this.getLayout().setActiveItem(1);	
	}
	
	, executeQbe: function(inputType, record){
		if(inputType == "MODEL"){
			var modelName = record.data.name;
			var dataSourceLabel = record.data.data_source_label;
			var url = this.qbeFromBMBaseUrl+"&MODEL_NAME="+modelName;
			if(dataSourceLabel || dataSourceLabel!=""){
				url = url+ 
				'&DATA_SOURCE_LABEL=' + dataSourceLabel;
			}
			this.documentexecution.modelName = modelName;
			this.documentexecution.load(url);
		}
		if(inputType == "DATASET"){
			var datasetLabel = record.data.label;
			var dataSourceLabel = record.data.dataSource;
			var url =  this.qbeFromDataSetBaseUrl+ '&dataset_label=' + datasetLabel;
			this.documentexecution.load(url);
			this.documentexecution.datasetLabel = datasetLabel;
		}
	}
	
	, executeWorksheet: function(inputType, record){
		if(inputType == "DATASET"){
			var datasetLabel = record.data.label;
			var dataSourceLabel = record.data.dataSource;
			var url = this.worksheetEngineBaseUrl+ '&dataset_label=' + datasetLabel;
			this.documentexecution.load(url);
			this.documentexecution.datasetLabel = datasetLabel;
			
		}
	}
	
	, executeGeoreport: function(inputType, record){
		if(inputType == "DATASET"){
			var datasetLabel = record.data.label;
			var dataSourceLabel = record.data.dataSource;
			
			var url =  this.georeportEngineBaseUrl+ '&dataset_label=' + datasetLabel ;
			if(dataSourceLabel || dataSourceLabel!=""){
				url = url+ '&datasource_label=' + dataSourceLabel;
			}
			this.documentexecution.load(url);
			this.documentexecution.datasetLabel = datasetLabel;
		}
	}
	
   
	
});