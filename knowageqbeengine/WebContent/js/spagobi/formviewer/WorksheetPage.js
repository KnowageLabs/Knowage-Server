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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.formviewer");

Sbi.formviewer.WorksheetPage = function(config) {	
	var defaultSettings = {
		//title: LN('sbi.worksheet.title')
		layout: 'fit'
		, autoScroll: true
		, border : false
	};
		
	if(Sbi.settings && Sbi.settings.formviewer && Sbi.settings.formviewer.worksheetPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formviewer.worksheetPage);
	}
		
	this.services={};
	this.services['getWorkSheetState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_WORKSHEET_PREVIEW_ACTION'
		, baseParams: params
	});
	this.services['setWorkSheetState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SET_WORKSHEET_DEFINITION_ACTION'
		, baseParams: params
	});
	this.services['executeWorksheetStartAction'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'START_WORKSHEET_FROM_QBE_ACTION'
			, baseParams: params
	});
	this.services['updateWoksheetDataSet'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'UPDATE_DATA_SET_WITH_SMART_FILTER_VALUES'
			, baseParams: params
	});
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init(c);
	
	c = Ext.apply(c, {
	    //style: 'padding:3px;',
	    items: [this.worksheetPanel]
	});
		
	// constructor
	Sbi.formviewer.WorksheetPage.superclass.constructor.call(this, c);
	
	this.addEvents('edit', 'contentexported');
};

Ext.extend(Sbi.formviewer.WorksheetPage, Ext.Panel, {

	formState: null
	, worksheetDesignerPanel : null
	, worksheetPreviewPanel : null
	, worksheetPanel : null
	
    // -- public methods -----------------------------------------------------------------------

    , getFormState: function() {
		return this.formState;
	}

	, setFormState: function(formState) {
		this.formState = formState;
	}
    
    // -- private methods -----------------------------------------------------------------------

	, init: function (c) {
		
		this.worksheetDesignerPanel = new Sbi.worksheet.designer.WorksheetDesignerPanel(Ext.apply(c||{},{smartFilter: true}));
		
		this.worksheetPreviewPanel = new Sbi.worksheet.runtime.WorkSheetPreviewPage({id : 'WorkSheetPreviewPage', closable: false});
			
		this.worksheetPreviewPanel.on('activate', function() {
			//validate
			
			this.worksheetDesignerPanel.validate(
					function(){
						this.setWorksheetState(this.refreshWorksheetPreview, Sbi.exception.ExceptionHandler.handleFailure, this);
					}
					, this.worksheetDesignerPanel.showValidationErrors
					, this);
			
			//			var errorArray = this.worksheetDesignerPanel.validate();
			//			if(errorArray && errorArray.length>0){
				//				this.worksheetDesignerPanel.showValidationErrors(errorArray);
				//			return;
				//	}
			//	else {
				// valid case
				//		this.setWorksheetState(this.refreshWorksheetPreview, Sbi.exception.ExceptionHandler.handleFailure, this);
		//	}
		}, this);
		
		this.worksheetPanel = new Sbi.worksheet.designer.WorksheetPanel({
			id : 'WorksheetPanel'
			, title : ''
			, worksheetDesignerPanel : this.worksheetDesignerPanel
			, worksheetPreviewPanel : this.worksheetPreviewPanel
			, extraButtons : [
 			    '-'
 			    , {
 					text: LN('sbi.formviewer.resultspage.backtoform'),
 					handler: function() {this.fireEvent('edit');},
 					scope: this
 			    }
 			 ]
		});

	}

	/**
	 * Update the worksheet engine instance with
	 * the form values
	 * If the worksheet has not been already started
	 * we start it...
	 * 
	 */
	, updateWorksheetEngine : function(){
		this.worksheetPanel.setActiveItem(0);
		this.worksheetPanel.prevButton.toggle(true, true);
		var worksheetEngineInitialized = this.engineInitialized;
		if (worksheetEngineInitialized === undefined || worksheetEngineInitialized == false) {
			if (!this.notFirstTimeDesignPanelOpened) {
				this.notFirstTimeDesignPanelOpened = true;
				Ext.Ajax.request({
					url: this.services['executeWorksheetStartAction'],
					params: {},
					scope: this,
					success: this.updateWorksheetDataSet,
					failure: Sbi.exception.ExceptionHandler.handleFailure
				});
			} else {
				this.updateWorksheetDataSet();
			}
		} else {
			this.updateWorksheetDataSet();
		}
	}
	
	/**
	 * Update the data set inside the worksheet
	 */
	, updateWorksheetDataSet: function(){
		var params = {
				'formstate':   Ext.util.JSON.encode(this.getFormState())
		};
		Ext.Ajax.request({
			url: this.services['updateWoksheetDataSet'],
			params: params,
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure
		});
	}
	
	, setWorksheetState : function (successFn, failureFn, scope) {
		var worksheetDefinition = this.worksheetDesignerPanel.getWorksheetDefinition();
		var params = {
				'worksheetdefinition':  Ext.encode(worksheetDefinition)
		};
		
		params.formstate = Ext.util.JSON.encode(this.getFormState());
		
		Ext.Ajax.request({
		    url: this.services['setWorkSheetState'],
		    success: successFn,
		    failure: failureFn,
		    scope: scope,
		    params: params
		});   
	}
	
	, getWorksheetDefinition : function () {
		return this.worksheetDesignerPanel.getWorksheetDefinition();   
	}
	
	, validate : function () {
		return this.worksheetDesignerPanel.validate();
	}
  	
  	, addFormStateParameter: function(crosstabPreviewPanel, requestParameters) {
  		requestParameters.formstate = Ext.util.JSON.encode(this.getFormState());
  	}
  	
	, refreshWorksheetPreview : function () {
		this.worksheetPreviewPanel.getFrame().setSrc(this.services['getWorkSheetState']);
	}
	
    , exportContent: function(mimeType){
    	if( this.worksheetPreviewPanel!=undefined && this.worksheetPreviewPanel!=null && 
    		this.worksheetPreviewPanel.getFrame()!=undefined && this.worksheetPreviewPanel.getFrame()!=null &&
    		this.worksheetPreviewPanel.getFrame().getWindow()!=undefined && this.worksheetPreviewPanel.getFrame().getWindow()!=null &&
    		this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel!=undefined && this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel!=null){
    			this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.un('contentexported', this.sendMessageToParentFrame,this);	
    			this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.on('contentexported', this.sendMessageToParentFrame,this);	
    			this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.exportContent(mimeType, true);
    	}
    }
	
    , sendMessageToParentFrame: function(){
    	this.fireEvent('contentexported');
    }

});