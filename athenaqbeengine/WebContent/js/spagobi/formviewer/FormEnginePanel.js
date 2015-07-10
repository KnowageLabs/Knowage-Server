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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.formviewer");

Sbi.formviewer.FormEnginePanel = function(formEngineConfig) {
	
	var defaultSettings = {
		//title: LN('sbi.qbe.queryeditor.title'),
	};
	
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.queryBuilderPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.queryBuilderPanel);
	}
	
	var c = Ext.apply(defaultSettings, formEngineConfig.config || {});
	
	Ext.apply(this, c);
	
	/*
	this.services = this.services || new Array();	
	this.services['saveQuery'] = this.services['saveQuery'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_QUERY_ACTION'
		, baseParams: new Object()
	});
	
	this.addEvents('execute');
	*/
	
	this.initFormViewerPage(formEngineConfig.template, c.formViewerPageConfig || {}, formEngineConfig.formValues);
	this.initResultsPage( Ext.apply( c.resultsPageConfig || {}, {template : formEngineConfig.template} ));
	this.initWorksheetPage(formEngineConfig.worksheet || {});
	this.activePageNumber =0;
	c = Ext.apply(c, {
		closable: false
		, border: false
		, activeItem: 0
		, hideMode: !Ext.isIE ? 'nosize' : 'display'
		, layout: 'card'
		, items: [this.formViewerPage, this.resultsPage, this.worksheetPage]
	});
	
	
	// constructor
    Sbi.formviewer.FormEnginePanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formviewer.FormEnginePanel, Ext.Panel, {
    
    services: null
    , formViewerPage: null
    , resultsPage: null
    , activePageNumber: null
   
   
    // -- public methods ----------------------------------------------------------------------------------
    
    
    
    // -- private methods ----------------------------------------------------------------------------------
    
    , initFormViewerPage: function(template, config, formValues) {
		this.formViewerPage = new Sbi.formviewer.FormViewerPage(template, config, formValues);
		this.formViewerPage.on('submit', this.moveToResultsPage, this);
		this.formViewerPage.on('crosstabrequired', this.moveToWorksheetPage, this);
		this.formViewerPage.on('activate', this.getSaveWorksheetButtonEnabler(false), this);
	}

	, initResultsPage: function(config) {
		this.resultsPage = new Sbi.formviewer.ResultsPage(config);
		this.resultsPage.on('edit', this.moveToFormPage, this);
		this.resultsPage.on('activate', this.getSaveWorksheetButtonEnabler(true, 'dataset'), this);
	}
	
	, initWorksheetPage: function(config) {
		this.worksheetPage = new Sbi.formviewer.WorksheetPage(config);
		this.worksheetPage.on('edit', this.moveToFormPage, this);
		this.worksheetPage.on('contentexported', function(){sendMessage({}, 'contentexported');}, this);
		this.worksheetPage.on('activate', this.getSaveWorksheetButtonEnabler(true, 'worksheet'), this);
	}
	
	// it is actually used to save worksheet and form as dataset!
	, getSaveWorksheetButtonEnabler : function (enabled, target) {
		var toReturn = function () {
			if (typeof sendMessage == 'function') { // check if function is existing (when building a Smart Filter document it does not)
				sendMessage({button: "saveworksheet", property:"visibility", value:"" + enabled + "", target:"" + target + ""}, "managebutton");
			}
		};
		return toReturn;
	}
	
    , moveToWorksheetPage: function(formState) {
    	this.getLayout().setActiveItem( 2 );
    	this.worksheetPage.setFormState(formState);
    	this.worksheetPage.updateWorksheetEngine();
    	this.activePageNumber = 2;
	}
	
    , moveToResultsPage: function(formState) {
    	this.getLayout().setActiveItem( 1 );
    	this.resultsPage.setFormState(formState);
    	this.resultsPage.loadResults(formState.groupingVariables);
    	this.activePageNumber = 1;
	}
    
    , moveToFormPage: function() {
    	this.getLayout().setActiveItem( 0 );
    	this.activePageNumber = 0;
	}
    
    , validate : function () {
    	return this.worksheetPage.worksheetDesignerPanel.validate(this.getWorksheetTemplateAsString, this.worksheetPage.worksheetDesignerPanel.showValidationErrors, this );	
    }
    
    , getWorksheetTemplateAsString : function () {
	    if (this.worksheetPage !== null) {

			var worksheetDefinition = this.worksheetPage.getWorksheetDefinition();
			var formState = this.formViewerPage.getFormState();
			

			var template = Ext.util.JSON.encode({
				'OBJECT_WK_DEFINITION' : worksheetDefinition,
				'OBJECT_FORM_VALUES' : formState
			});
			return template;
		} else {
			alert('Warning: worksheetDesignerPanel not defined!!');
			return null;
		}
	}
    
    , getFormState : function () {
    	return this.formViewerPage.getFormState();
    }
    
    , exportContent: function(mimeType){
    	if(this.isWorksheetPageActive()){
	    	if( this.worksheetPage!=undefined && this.worksheetPage!=null){
	    		this.worksheetPage.exportContent(mimeType);
	    	}
		}else{
			sendMessage({}, 'worksheetexporttaberror');
		}
    }

    , isWorksheetPageActive: function(){
    	if( this.activePageNumber == 2 && this.worksheetPage!=undefined && this.worksheetPage!=null){
    		return this.worksheetPage.tabs.getActiveTab().id=='WorkSheetPreviewPage';
    	}
    	return false;
    }
	
});