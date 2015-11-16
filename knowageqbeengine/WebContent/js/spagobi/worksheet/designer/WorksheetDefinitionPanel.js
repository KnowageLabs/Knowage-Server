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

Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.WorksheetDefinitionPanel = function(config) {	

	var defaultSettings = {
		// default settings
		layout : 'fit'
		, autoScroll : true
		, border : false
	};
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.worksheetDefinitionPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.worksheetDefinitionPanel);
	}
 
	this.services = new Array();
	var params = {};
	this.services['setWorkSheetState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SET_WORKSHEET_DEFINITION_ACTION'
			, baseParams: params
	});
	this.services['getWorkSheetState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_WORKSHEET_PREVIEW_ACTION'
			, baseParams: params
	});
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	this.init(config);
	
	c = Ext.apply(c, {
	    items: [this.worksheetPanel]
	});

	// constructor
	Sbi.worksheet.designer.WorksheetDefinitionPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.designer.WorksheetDefinitionPanel, Ext.Panel, {

	worksheetDesignerPanel : null
	, worksheetPreviewPanel : null
//	, formState : null  // for SmartFilter engine: TODO move this variable elsewhere
	, worksheetPanel : null
	
	,
	init : function (config) {
		
		this.worksheetDesignerPanel = new Sbi.worksheet.designer.WorksheetDesignerPanel(Ext.apply({
			engineAlreadyInitialized : true
		}, {
			worksheetTemplate : config
		}));
		this.worksheetPreviewPanel = new Sbi.worksheet.runtime.WorkSheetPreviewPage({id : 'WorkSheetPreviewPage'}); // was ({closable: false});
		
		this.worksheetPreviewPanel.on('activate', function() {
			//validate
			this.worksheetDesignerPanel.validate(
					function(){
						this.setWorksheetState(this.refreshWorksheetPreview, Sbi.exception.ExceptionHandler.handleFailure, this);
					}
					, this.worksheetDesignerPanel.showValidationErrors
					, this
			);
			
		}, this);
		
		this.worksheetPanel = new Sbi.worksheet.designer.WorksheetPanel({
			title : null
			, worksheetDesignerPanel : this.worksheetDesignerPanel
			, worksheetPreviewPanel : this.worksheetPreviewPanel
		});
				
	}

	,
	setWorksheetState : function (successFn, failureFn, scope) {
		var worksheetDefinition = this.worksheetDesignerPanel.getWorksheetDefinition();
		this.addSheetAdditionalData(worksheetDefinition,this.worksheetDesignerPanel.worksheetTemplate);
		var params = {
			'worksheetdefinition':  Ext.encode(worksheetDefinition)
		};
		
//		params.formstate = Ext.util.JSON.encode(this.getFormState());
		
		Ext.Ajax.request({
		    url: this.services['setWorkSheetState'],
		    success: successFn,
		    failure: failureFn,
		    scope: scope,
		    params: params
		});   
	}
	
	,
	getWorksheetDefinition : function () {
		return this.worksheetDesignerPanel.getWorksheetDefinition();   
	}
	
	, validate : function () {
		return 	this.worksheetDesignerPanel.validate(this.getWorksheetTemplateAsString, this.worksheetDesignerPanel.showValidationErrors, this );	
	}

	, getWorksheetTemplateAsString : function () {
		var worksheetDefinition = null;
		if (this.worksheetDesignerPanel.rendered === true) {
			// get the current worksheet designer state
			worksheetDefinition = this.worksheetDesignerPanel.getWorksheetDefinition();
		} else {
			// get the initial worksheet template
			worksheetDefinition = this.worksheetDesignerPanel.worksheetTemplate;
		}
		this.addAdditionalData(worksheetDefinition);
		var template = Ext.util.JSON.encode({
			'OBJECT_WK_DEFINITION' : worksheetDefinition
		});
		return template;
	}
	,
	refreshWorksheetPreview : function () {
		this.worksheetPreviewPanel.getFrame().setSrc(this.services['getWorkSheetState']);
	}

	, addAdditionalData : function(sheetTemplate){

		if(this.worksheetPreviewPanel.rendered === true &&
		   this.worksheetPreviewPanel.getFrame().getWindow() &&
		   this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel){
			var additionalData = this.worksheetPreviewPanel.getFrame().getWindow().workSheetPanel.getAdditionalData();
			if(additionalData!=undefined && additionalData!=null){
				var sheets = sheetTemplate.sheets;
				for(var i=0; i<sheets.length; i++){
					if(additionalData[i]!=undefined && additionalData[i]!=null && additionalData[i].data!=undefined && additionalData[i].data!=null && !Ext.isEmpty(additionalData[i].data) && sheets[i].content.crosstabDefinition!=undefined && sheets[i].content.crosstabDefinition!=null){
						if (additionalData[i].data.crosstabDefinition) {
							var crosstabDefinition = additionalData[i].data.crosstabDefinition;
							if (crosstabDefinition.calculatedFields) {
								sheets[i].content.crosstabDefinition.calculatedFields = crosstabDefinition.calculatedFields;
							}
							if (crosstabDefinition.additionalData) {
								sheets[i].content.crosstabDefinition.additionalData = crosstabDefinition.additionalData;
							}
						}
					}
				}	
			}
		}else{
			this.addSheetAdditionalData(sheetTemplate,this.worksheetDesignerPanel.worksheetTemplate);
		}
	}

	, addSheetAdditionalData: function(designerTemplate, documentTemplate){
		if(designerTemplate!=undefined && designerTemplate!=null && documentTemplate!=undefined && documentTemplate!=null){
			var designerTemplateSheets = designerTemplate.sheets;
			var documentTemplateSheets = documentTemplate.sheets;
			if(documentTemplateSheets!=undefined && documentTemplateSheets!=null && designerTemplateSheets!=undefined && designerTemplateSheets!=null){
				for(var i=0; i<designerTemplateSheets.length; i++){
					for(var y=0; y<documentTemplateSheets.length; y++){
						if(designerTemplateSheets[i].name == documentTemplateSheets[y].name){
							if(designerTemplateSheets[i].content.crosstabDefinition!=undefined && designerTemplateSheets[i].content.crosstabDefinition!=null && documentTemplateSheets[y].content.crosstabDefinition!=undefined && documentTemplateSheets[y].content.crosstabDefinition!=null){
								designerTemplateSheets[i].content.crosstabDefinition.calculatedFields =documentTemplateSheets[y].content.crosstabDefinition.calculatedFields;
								designerTemplateSheets[i].content.crosstabDefinition.additionalData =documentTemplateSheets[y].content.crosstabDefinition.additionalData;
							}
							break;
						}
					}	
				}	
			}
		}
	}
	
	, exportContent : function(mimeType, metadata, parameters) {
		if (this.isWorksheetPageActive()) {
			var frame = this.worksheetPreviewPanel.getFrame();
			var theWindow = frame.getWindow();
			var workSheetPanel = theWindow.workSheetPanel;
			workSheetPanel.on('contentexported', this.sendMessageToParentFrame, this, {single : true});	
//			workSheetPanel.un('contentexported', this.sendMessageToParentFrame, this);	
//			workSheetPanel.on('contentexported', this.sendMessageToParentFrame, this);	
			workSheetPanel.exportContent(mimeType, true, metadata, parameters);
		} else {
			sendMessage({}, 'worksheetexporttaberror');
		}
	}

	, sendMessageToParentFrame: function(){
		sendMessage({}, 'contentexported');
	}
	
	, isWorksheetPageActive: function(){
		return this.worksheetPanel.isWorksheetPageActive();
	}

});