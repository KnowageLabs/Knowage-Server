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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.WorksheetDesignerPanel = function(config) { 

	var defaultSettings = {
		//title: LN('sbi.worksheet.designer.worksheetdesignerpanel.title')
		engineAlreadyInitialized : false
		, border : false
	};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.worksheetDesignerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.worksheetDesignerPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.services = this.services || new Array();
	this.services['executeWorksheetStartAction'] = this.services['executeWorksheetStartAction'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'START_WORKSHEET_FROM_QBE_ACTION'
		, baseParams: params
	});
	
	this.addEvents('afterworksheetinitialized');
	
	this.init(config);
	
	c = Ext.apply(c, {
			layout: 'border',
			autoScroll: true,
			items: [
		        {
		        	id: 'designToolsPanel',
		        	region: 'west',
		        	width: 275,
		        	collapseMode:'mini',
		        	autoScroll: true,
		        	split: true,
		        	layout: 'fit',
		        	items: [this.designToolsPanel]
		        },
		        {
		        	id: 'sheetsContainerPanel',	  
		        	region: 'center',
		        	split: true,
		        	collapseMode:'mini',
		        	autoScroll: true,
		        	layout: 'fit',
		        	items: [this.sheetsContainerPanel]
		        }
			]
	}); 
		

	Sbi.worksheet.designer.WorksheetDesignerPanel.superclass.constructor.call(this, c);	 		
	
	this.on('render', function () {
		if (!this.engineAlreadyInitialized) {
			this.initializeEngineInstance({
				onSuccessHandler : function(response, options) {
					this.fireEvent('afterworksheetinitialized', this);
				}
				, scope: this
			});
		} else {
			this.fireEvent('afterworksheetinitialized', this);
		}
	}, this, { single : true } );
	
};

/**
 * @class Sbi.worksheet.designer.WorksheetDesignerPanel
 * @extends Ext.Panel
 * 
 * WorksheetDesignerPanel
 */
Ext.extend(Sbi.worksheet.designer.WorksheetDesignerPanel, Ext.Panel, {
	services: null,
	designToolsPanel: null,
	sheetsContainerPanel: null,
	worksheetTemplate: {},   // the initial worksheet template; to be passed as a property of the constructor's input object!!!
	contextMenu: null,
	engineAlreadyInitialized : null

	,
	initializeEngineInstance : function (config) {
		Ext.Ajax.request({
			url: this.services['executeWorksheetStartAction'],
			params: {},
			success: config.onSuccessHandler,
			scope: config.scope,
			failure: Sbi.exception.ExceptionHandler.handleFailure
		});
	}
	
	,
	init : function (config) {
		this.initPanels(config);
		this.initContextMenu();
		this.setGlobalFilters(this.worksheetTemplate.globalFilters || []);
		this.setFieldsOptions(this.worksheetTemplate.fieldsOptions || {});
	},
	
	initPanels: function(config){
		this.designToolsPanel = new Sbi.worksheet.designer.DesignToolsPanel({
		});
		this.designToolsPanel.on('toolschange',function(change){
			this.sheetsContainerPanel.updateActiveSheet(change);
		},this);
		this.designToolsPanel.on(
				'attributeDblClick', 
				this.attributeDblClickHandler, 
				this
			);
		this.designToolsPanel.on(
				'fieldRightClick', 
				this.fieldRightClickHandler, 
				this
			);

		this.sheetsContainerPanel = new Sbi.worksheet.designer.SheetsContainerPanel(Ext.apply(this.sheetsContainerPanelCfg  || {}, {
			sheets : this.worksheetTemplate.sheets || []  ,
			smartFilter: config.smartFilter || false
		}));
		this.sheetsContainerPanel.on(
			'attributeDblClick', 
			this.attributeDblClickHandler,
			this
		);
		
		this.sheetsContainerPanel.on('sheetchange',function(activeSheet){
			this.designToolsPanel.updateToolsForActiveTab(activeSheet);
		},this);
	}
	
	, initContextMenu : function () {
		var items = [{
			text : LN('sbi.config.optionswindow.title')
			, scope : this
			, handler : this.showOptions
		}];
	   	this.contextMenu = 
			new Ext.menu.Menu({
				items: items
		});
	   	this.contextMenu.setField = function (field) {
	   		this.contextField = field;
	   	}
	   	this.contextMenu.getField = function () {
	   		return this.contextField;
	   	}
	}
	
	, attributeDblClickHandler : function (thePanel, attribute, theSheet) {
		var worksheetDefinition = this.getWorksheetDefinition();
		var params = {
			worksheetDefinition : Ext.encode(worksheetDefinition)
		};
		var startValues = null;
		var enabledRecords = null; // records selectable, if null means every record
		if (theSheet) {
			// double-click event on a sheet
			params.sheetName = theSheet.getName();
			attribute = theSheet.getFilterOnDomainValues(attribute);
			startValues = Ext.decode(attribute.values);
			var globalFilter = this.getGlobalFilterForAttribute(attribute);
			if (attribute.values == '[]') {
				// if there are no filters on sheet, consider the global filters
				if (globalFilter !== null) {
					startValues = Ext.decode(globalFilter.values);
				}
			}
			enabledRecords = globalFilter != null ? Ext.decode(globalFilter.values) : null; // records selectable are those in global filter
		} else {
			// double click on top-left fields panel (global filters)
			startValues = Ext.decode(attribute.values);
			enabledRecords = null;
		}
		var c = {
     		attribute : attribute
     		, startValues : startValues
     		, enabledRecords : enabledRecords
     		, params : params
	    };
     	var chooserWindow = new Sbi.worksheet.designer.AttributeValuesChooserWindow(c);
     	chooserWindow.on('selectionmade', function(theWindow, sSelection) {
     		var selection = Ext.encode(theWindow.getSelection());
     		attribute.values = selection;
     	}, this);
	}
	
	, getWorksheetDefinition: function () {
		var	worksheetDefinition = this.sheetsContainerPanel.getSheetsState();
		worksheetDefinition.globalFilters = this.getGlobalFilters();
		worksheetDefinition.fieldsOptions = this.getFieldsOptions();
		worksheetDefinition.version = Sbi.config.worksheetVersion;
		return worksheetDefinition;
	}
	
	, validate: function (successHandler, failureHandler, scope) {
		// return an array of validationError object, if no error returns an empty array
		//var invalidFields = this.designToolsPanel.designToolsFieldsPanel.getInvalidFieldsArray();
		var validFields = this.designToolsPanel.designToolsFieldsPanel.getFields();
		
		var errorArray = this.sheetsContainerPanel.validate(validFields);

		if(errorArray && errorArray.length>0){
			if (failureHandler != undefined){
				return failureHandler.call(scope || this, errorArray);
			}
				else return null;
			}
		else {
			if (successHandler != undefined){
				return successHandler.call(scope || this);	
			}
				else return null;
		}
	}
	
	, getGlobalFilters : function () {
		return this.designToolsPanel.getGlobalFilters();
	}
	
	, getFieldsOptions : function () {
		return this.designToolsPanel.getFieldsOptions();
	}
	
    , showValidationErrors : function(errorsArray) {
    	errMessage = '';
    	
    	for(var i = 0; i < errorsArray.length; i++) {
    		var error = errorsArray[i];
    		var sheet = error.sheet;
    		var message = error.message;
    		errMessage += error.sheet + ': ' + error.message + '<br>';
    	}
    	
    	Sbi.exception.ExceptionHandler.showErrorMessage(errMessage, LN('sbi.crosstab.crossTabValidation.title'));
   	
    }
	
    , getGlobalFilterForAttribute : function (attribute) {
    	return this.designToolsPanel.getGlobalFilterForAttribute(attribute);
    }
    
    , getOptionsForField : function (field) {
    	return this.designToolsPanel.getOptionsForField(field);
    }
    
	, setGlobalFilters : function (globalFilters) {
		this.designToolsPanel.setGlobalFilters(globalFilters);
	}
	
	, setFieldsOptions : function (fieldsOptions) {
		this.designToolsPanel.setFieldsOptions(fieldsOptions);
	}
	
	, fieldRightClickHandler : function (thePanel, field, e) {
		e.stopEvent();
		if (
				(field.nature == 'attribute' || field.nature == 'segment_attribute')
				&& // field is an attribute and there are options for attributes
				(Sbi.worksheet.config.options.attributes && Sbi.worksheet.config.options.attributes.length > 0)
				||
				(field.nature == 'measure' || field.nature == 'mandatory_measure')
				&& // field is a measure and there are options for measures
				(Sbi.worksheet.config.options.measures && Sbi.worksheet.config.options.measures.length > 0)
			) {
			this.contextMenu.setField(field);
			this.contextMenu.showAt(e.getXY());
		}
	}
	
	, showOptions : function (item) {
		var field = item.parentMenu.getField();
		var optionsToDisplay = null;
		if (field.nature == 'attribute' || field.nature == 'segment_attribute') {
			optionsToDisplay = Sbi.worksheet.config.options.attributes;
		} else {
			optionsToDisplay = Sbi.worksheet.config.options.measures;
		}
		
     	var optionsWindow = new Sbi.worksheet.config.OptionsWindow({
     		options : optionsToDisplay
     	});
     	optionsWindow.on('render', function(theWindow) {
     		var state = field.options;
     		theWindow.setFormState(state);
     	}, this);
     	optionsWindow.on('apply', function(theWindow, formState) {
     		field.options = formState;
     	}, this);
     	optionsWindow.show();
	}
	, fake : function () {

	}
	
});
