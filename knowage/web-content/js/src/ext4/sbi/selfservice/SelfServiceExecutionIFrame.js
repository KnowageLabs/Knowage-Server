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
		Sbi.debug('[SelfServiceExecutionIFrame.saveHandler]: got window');
		
		if (theWindow.qbe != null) {
			this.saveQbe();
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
			// we have to save a new dataset
			var qbeWindow = this.iframe.getWin();
			var qbePanel = qbeWindow.qbe;
			var anActiveTab = qbePanel.tabs.getActiveTab();
			var activeTabId = anActiveTab.getId();
			this.openQbeSaveDataSetWizard();
	}
	
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