/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 *     
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */
 
  
Ext.define('Sbi.selfservice.SelfServiceExecutionIFrame', {
	extend: 'Sbi.widgets.EditorIFramePanelContainer'

		
	, modelName: null
	, datasetLabel: null
	, fromMyAnalysis: null
	, fromDocBrowser: null
	, contextName: null
	, frame: false
	, border: 0
	
	, init: function(config){
		this.callParent(arguments);
		
		
		if( Sbi.settings && Sbi.settings 
			&& Sbi.settings.mydata && Sbi.settings.mydata.toolbar 
			&& Sbi.settings.mydata && Sbi.settings.mydata.toolbar.hide === true) {
			Sbi.debug("[SelfServiceExecutionIFrame.init]: Toolbar not visible");
		} else {
			if ((config.hideToolbar == undefined) || (config.hideToolbar == false)){
				this.initToolbar(config);
			}
		}
		
		if ((config.fromMyAnalysis != undefined) && (config.fromMyAnalysis != null)){
			this.fromMyAnalysis = config.fromMyAnalysis;
		}
		
		if ((config.fromDocBrowser != undefined) && (config.fromDocBrowser != null)){
			this.fromDocBrowser = config.fromDocBrowser;
		}
		
		if ((config.contextName != undefined) && (config.contextName != null)){
			this.contextName = config.contextName;
		}
		
	}


	, initToolbar : function (config) {

		this.tbar  = Ext.create('Ext.toolbar.Toolbar');
		this.tbar.add('->');
		// passed by JSP userDocumentBrowserCreateDoc.jsp 
		if(config.hideExtraSaveButton != undefined && config.hideExtraSaveButton == true){
				// if in creation detail page in user browser do not use this save button
		} else{
			this.tbar.add({
				iconCls : 'icon-saveas' 
				, tooltip: LN('sbi.execution.executionpage.toolbar.saveas')
				, id: 'selfserviceSaveBtn'
				, scope : this
			    , handler : this.saveHandler
			});
		}
		
	}
	
	, hideSaveButton : function () {
		var btn = Ext.getCmp('selfserviceSaveBtn')
		btn.hide();	
	}
	
	, showSaveButton : function () {
		var btn = Ext.getCmp('selfserviceSaveBtn')
		btn.show();	
	}
	
	, returnToMyAnalysis : function() {
		window.location = this.contextName + '/servlet/AdapterHTTP?ACTION_NAME=CREATE_DOCUMENT_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE';
		
	}
	
	, returnToDocBrower: function(){		
		window.location = this.contextName + '/servlet/AdapterHTTP?ACTION_NAME=DOCUMENT_USER_BROWSER_START_ACTION';
	}
	
	, saveHandler : function() {
		
		var theWindow = this.iframe.getWin();
		Sbi.debug('[SelfServiceExecutionIFrame.saveWorksheet]: got window');
		
		if (theWindow.qbe != null) {
			this.saveQbe();
		} else if (theWindow.workSheetPanel != null) {
			var template = theWindow.workSheetPanel.validate();
			this.saveWorksheet(template);
		} else if (theWindow.geoReportPanel != null){
			var template = theWindow.geoReportPanel.validate();
			this.saveGeoReport(template);
		} else {
			alert("Impossible to save document of type [unknown]");
		}
		
	}
	
	, saveGeoReport : function(template) {
		
    	if (template == null) {
    		alert("Impossible to get template");
    		return;
    	}
    	
    	Sbi.debug('[SelfServiceExecutionIFrame.saveGeoReport]: ' + template);
    	
		var documentWindowsParams = {
				'OBJECT_TYPE': 'MAP',
				'OBJECT_TEMPLATE': template,
				'model_name': this.modelName,
				'typeid': 'GEOREPORT',
				'fromMyAnalysis': this.fromMyAnalysis,
				'fromDocBrowser': this.fromDocBrowser
					
		};

		if(this.datasetLabel!=null){
			documentWindowsParams.dataset_label= this.datasetLabel;
			documentWindowsParams.MESSAGE_DET= 'DOC_SAVE_FROM_DATASET';
		} else if(this.modelName!=null){
			documentWindowsParams.model_name= this.modelName;
			documentWindowsParams.MESSAGE_DET= 'DOC_SAVE_FROM_MODEL';
		}
		
		this.win_saveDoc = Ext.create("Sbi.execution.SaveDocumentWindowExt4", documentWindowsParams);
		this.win_saveDoc.on('returnToMyAnalysis', this.returnToMyAnalysis, this);
		this.win_saveDoc.on('returnToDocBrower', this.returnToDocBrower, this);
		
		this.win_saveDoc.show();
    
    }
	
	,
	saveQbe : function () {
		//try {
			// May be we have to save a new dataset or a worksheet document
			var qbeWindow = this.iframe.getWin();
			var qbePanel = qbeWindow.qbe;
			var anActiveTab = qbePanel.tabs.getActiveTab();
			var activeTabId = anActiveTab.getId();
			var isBuildingWorksheet = (activeTabId === 'WorksheetPanel');
			if (isBuildingWorksheet) {
				// save worksheet as document
				var template = qbePanel.validate();
				this.saveWorksheet(template);
			} else {
				// save query as new dataset
				
				this.openQbeSaveDataSetWizard();
				
//				var queryDefinition = this.getQbeQueryDefinition();
//				var saveDatasetWindow = Ext.create("Sbi.selfservice.SaveDatasetWindow", { queryDefinition : queryDefinition } );
//				saveDatasetWindow.on('save', function(theWindow, formState) { theWindow.close(); }, this);
//				saveDatasetWindow.show();
				
			}
		//} catch (err) {
		//	alert('Sorry, cannot perform operation.');
		//	throw err;
		//}
	}
	
	, saveWorksheet : function(template) {
	
    	if (template == null) {
    		alert("Impossible to get template");
    		return;
    	}
    	
    	var templateJSON = Ext.JSON.decode(template);
		var wkDefinition = templateJSON.OBJECT_WK_DEFINITION;
		var worksheetQuery = templateJSON.OBJECT_QUERY;
		var documentWindowsParams = {
				'OBJECT_TYPE': 'WORKSHEET',
				'template': wkDefinition,
				'OBJECT_WK_DEFINITION': wkDefinition,
				'OBJECT_QUERY': worksheetQuery,
				'model_name': this.modelName,
				'typeid': 'WORKSHEET' ,
				'fromMyAnalysis': this.fromMyAnalysis

		};

		if(this.datasetLabel!=null){
			documentWindowsParams.dataset_label= this.datasetLabel;
			documentWindowsParams.MESSAGE_DET= 'DOC_SAVE_FROM_DATASET';
		}else if(this.modelName!=null){
			documentWindowsParams.model_name= this.modelName;
			documentWindowsParams.MESSAGE_DET= 'DOC_SAVE_FROM_MODEL';
		}
		
		
		this.win_saveDoc = Ext.create("Sbi.execution.SaveDocumentWindowExt4",documentWindowsParams);
		this.win_saveDoc.on('returnToMyAnalysis', this.returnToMyAnalysis, this);

		this.win_saveDoc.show();
    
    }
	
//	,
//	getQbeQueryDefinition : function () {
//		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: IN');
//		var qbeWindow = this.iframe.getWin();
//		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: got window');
//		var qbePanel = qbeWindow.qbe;
//		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: got qbe panel object');
//		var queries = qbePanel.getQueriesCatalogue();
//		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: got queries');
//		var toReturn = {};
//		toReturn.queries = queries;
//		toReturn.sourceDatasetLabel = this.datasetLabel;
//		return toReturn;
//	}
	
	,
	openQbeSaveDataSetWizard : function () {
		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: IN');
		var qbeWindow = this.iframe.getWin();
		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: got window');
		var qbePanel = qbeWindow.qbe;
		Sbi.debug('[SelfServiceExecutionIFrame.getQbeQueryDefinition]: got qbe panel object');
		qbePanel.on('save', this.returnToMyAnalysis, this); //added
		qbePanel.openSaveDataSetWizard(this.fromMyAnalysis);
	}	
	
});