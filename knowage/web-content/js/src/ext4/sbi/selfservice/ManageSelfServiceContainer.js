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
        tablePrefix:'',
        ckanUrls:''
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
    documentexecution: null,
    
    roleSelectionWindow: null,
    
    services : null,
    params : null,
    commonParams: null
	
	, constructor : function(config) {
		this.initConfig(config);
		
		this.layout =  'card';
		
		// INIT SERVICES
		this.services = new Array();
		this.commonParams = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
		
		this.services['getParametersForExecutionService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_PARAMETERS_FOR_EXECUTION_ACTION'
			, baseParams: this.commonParams
		});
		
		this.services['getUrlForExecutionService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_URL_FOR_EXECUTION_ACTION'
			, baseParams: this.commonParams
		});
		// END INIT SERVICES
		
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
			, isWorksheetEnabled : config.isWorksheetEnabled
			, typeDoc : config.typeDoc
			, userCanPersist: config.userCanPersist
			, tablePrefix: config.tablePrefix
			, ckanUrls: config.ckanUrls
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
			var url = this.qbeFromBMBaseUrl+"&MODEL_NAME="+modelName+
				"&isWorksheetEnabled="+Sbi.settings.mydata.isWorksheetEnabled;
			if(dataSourceLabel || dataSourceLabel!=""){
				url = url+ 
				'&DATA_SOURCE_LABEL=' + dataSourceLabel;
			}
			this.documentexecution.modelName = modelName;
			this.documentexecution.load(url);
		}
		else if(inputType == "FEDERATED_DATASET"){
			var federationId = record.data.id;
			var dataSourceLabel = record.data.data_source_label;
			var url = this.qbeFromFederationBaseUrl;
		
			if(federationId || federationId!=""){
				url = url+ 
				'&FEDERATION_ID=' + federationId;
			}
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
		if(inputType == "SMART_FILTER"){
			this.params = this.commonParams;
			var doc = record.data;
			this.params.OBJECT_ID=doc.id;
		   	this.params.OBJECT_LABEL=doc.label;
		   	var url = Sbi.config.serviceRegistry.getServiceUrl({
		   						serviceName: 'EXECUTE_DOCUMENT_ACTION'
		   						, baseParams: this.params
		   					});
		   	this.documentexecution.hideSaveButton();
		   	this.documentexecution.load(url);
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
	
	, openfederation: function(){
		this.manageSelfService.openfederation();
	}
});