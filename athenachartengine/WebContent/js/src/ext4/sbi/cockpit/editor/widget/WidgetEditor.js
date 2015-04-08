/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditor = function(config) {

	Sbi.trace("[WidgetEditor.constructor]: IN");

	var defaultSettings = {
		//title: LN('Sbi.cockpit.editor.widget.WidgetEditor.title')
		engineAlreadyInitialized : false
		, border : false
		, layout: 'border'
		, autoScroll: true
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditor', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.addEvents('afterworksheetinitialized');

	this.init(config);

	c = Ext.apply(c, {
			layout: 'border',
			autoScroll: false,
			items: [
		        {
		        	region: 'west',
		        	width: 280,
		        	collapseMode:'mini',
		        	autoScroll: false,
		        	split: true,
		        	layout: 'fit',
		        	//items: [{html: "this.controlPanel"}]
		        	items: [this.controlPanel]
		        },
		        {
		        	region: 'center',
		        	split: true,
		        	collapseMode:'mini',
		        	autoScroll: true,
		        	layout: 'fit',
		        	//items: [{html: "this.mainPanel"}]
		        	items: [this.mainPanel]
		        }
			]
	});


	Sbi.cockpit.editor.widget.WidgetEditor.superclass.constructor.call(this, c);

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

	Sbi.trace("[WidgetEditor.constructor]: OUT");

};

/**
 * @class Sbi.cockpit.editor.widget.WidgetEditor
 * @extends Ext.Panel
 *
 * WorksheetDesignerPanel
 */
Ext.extend(Sbi.cockpit.editor.widget.WidgetEditor, Ext.Panel, {

	services: null

	, controlPanel: null
	, sheetsContainerPanel: null
	, contextMenu: null

	, worksheetTemplate: {}  // the initial worksheet template; to be passed as a property of the constructor's input object!!!
	, engineAlreadyInitialized : null
	, mainPanel: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	, initializeEngineInstance : function (config) {
//		Ext.Ajax.request({
//			url: this.services['executeWorksheetStartAction'],
//			params: {},
//			success: config.onSuccessHandler,
//			scope: config.scope,
//			failure: Sbi.exception.ExceptionHandler.handleFailure
//		});
	}

	, init : function (config) {
		this.initPanels(config);
		//this.initContextMenu();
		//this.setGlobalFilters(this.worksheetTemplate.globalFilters || []);
		//this.setFieldsOptions(this.worksheetTemplate.fieldsOptions || {});
	}

	, initPanels: function(config){
		this.initDesignToolPanel(config);
		this.initSheetsContainerPanel(config);
	}

	, initDesignToolPanel: function(config) {
		this.controlPanel = new Sbi.cockpit.editor.widget.WidgetEditorControlPanel(config);

//		this.controlPanel = new Sbi.worksheet.designer.DesignToolsPanel({
//		});
//		this.controlPanel.on('toolschange',function(change){
//			this.mainPanel.updateActiveSheet(change);
//		},this);
//		this.controlPanel.on(
//				'attributeDblClick',
//				this.attributeDblClickHandler,
//				this
//			);
//		this.controlPanel.on(
//				'fieldRightClick',
//				this.fieldRightClickHandler,
//				this
//			);
	}

	, initSheetsContainerPanel: function(config) {
		this.mainPanel = new Sbi.cockpit.editor.widget.WidgetEditorMainPanel();

		this.mainPanel.on(
				'attributeDblClick',
				this.attributeDblClickHandler,
				this
		);

//		this.mainPanel = new Sbi.worksheet.designer.SheetsContainerPanel(Ext.apply(this.mainPanelCfg  || {}, {
//			sheets : this.worksheetTemplate.sheets || []  ,
//			smartFilter: config.smartFilter || false
//		}));
//		this.mainPanel.on(
//			'attributeDblClick',
//			this.attributeDblClickHandler,
//			this
//		);
//
//		this.mainPanel.on('sheetchange',function(activeSheet){
//			this.controlPanel.updateToolsForActiveTab(activeSheet);
//		},this);
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
	   	};
	   	this.contextMenu.getField = function () {
	   		return this.contextField;
	   	};
	}

	, attributeDblClickHandler : function (thePanel, attribute, theSheet) {

		Ext.Msg.alert('Message', 'attributeDblClickHandler');

//		var worksheetDefinition = this.getWorksheetDefinition();
//		var params = {
//			worksheetDefinition : Ext.encode(worksheetDefinition)
//		};
//		var startValues = null;
//		var enabledRecords = null; // records selectable, if null means every record
//		if (theSheet) {
//			// double-click event on a sheet
//			params.sheetName = theSheet.getName();
//			attribute = theSheet.getFilterOnDomainValues(attribute);
//			startValues = Ext.decode(attribute.values);
//			var globalFilter = this.getGlobalFilterForAttribute(attribute);
//			if (attribute.values == '[]') {
//				// if there are no filters on sheet, consider the global filters
//				if (globalFilter !== null) {
//					startValues = Ext.decode(globalFilter.values);
//				}
//			}
//			enabledRecords = globalFilter != null ? Ext.decode(globalFilter.values) : null; // records selectable are those in global filter
//		} else {
//			// double click on top-left fields panel (global filters)
//			startValues = Ext.decode(attribute.values);
//			enabledRecords = null;
//		}
//		var c = {
//     		attribute : attribute
//     		, startValues : startValues
//     		, enabledRecords : enabledRecords
//     		, params : params
//	    };
//     	var chooserWindow = new Sbi.worksheet.designer.AttributeValuesChooserWindow(c);
//     	chooserWindow.on('selectionmade', function(theWindow, sSelection) {
//     		var selection = Ext.encode(theWindow.getSelection());
//     		attribute.values = selection;
//     	}, this);
	}

	, getWorksheetDefinition: function () {

		Ext.Msg.alert('Message', 'attributeDblClickHandler');
		return null;

//		var	worksheetDefinition = this.mainPanel.getSheetsState();
//		worksheetDefinition.globalFilters = this.getGlobalFilters();
//		worksheetDefinition.fieldsOptions = this.getFieldsOptions();
//		worksheetDefinition.version = Sbi.config.worksheetVersion;
//		return worksheetDefinition;
	}

	// return an array of validationError object, if no error returns an empty array
	, validate: function (successHandler, failureHandler, scope) {

		Ext.Msg.alert('Message', 'validate');
		return null;

//		var validFields = this.controlPanel.designToolsFieldsPanel.getFields();
//
//		var errorArray = this.mainPanel.validate(validFields);
//
//		if(errorArray && errorArray.length>0){
//			if (failureHandler != undefined){
//				return failureHandler.call(scope || this, errorArray);
//			}
//				else return null;
//			}
//		else {
//			if (successHandler != undefined){
//				return successHandler.call(scope || this);
//			}
//				else return null;
//		}
	}

	, getGlobalFilters : function () {
		Ext.Msg.alert('Message', 'getGlobalFilters');
		return null;
		//return this.controlPanel.getGlobalFilters();
	}

	, getFieldsOptions : function () {
		Ext.Msg.alert('Message', 'getGlobalFilters');
		return null;
		//return this.controlPanel.getFieldsOptions();
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
    	Ext.Msg.alert('Message', 'getGlobalFilterForAttribute');
		return null;
    	//return this.controlPanel.getGlobalFilterForAttribute(attribute);
    }

    , getOptionsForField : function (field) {
    	Ext.Msg.alert('Message', 'getOptionsForField');
		return null;
    	//return this.controlPanel.getOptionsForField(field);
    }

	, setGlobalFilters : function (globalFilters) {
		Ext.Msg.alert('Message', 'setGlobalFilters');
		//this.controlPanel.setGlobalFilters(globalFilters);
	}

	, setFieldsOptions : function (fieldsOptions) {
		Ext.Msg.alert('Message', 'setFieldsOptions');
		//this.controlPanel.setFieldsOptions(fieldsOptions);
	}

	, fieldRightClickHandler : function (thePanel, field, e) {
		Ext.Msg.alert('Message', 'fieldRightClickHandler');

//		e.stopEvent();
//		if (
//				(field.nature == 'attribute' || field.nature == 'segment_attribute')
//				&& // field is an attribute and there are options for attributes
//				(Sbi.worksheet.config.options.attributes && Sbi.worksheet.config.options.attributes.length > 0)
//				||
//				(field.nature == 'measure' || field.nature == 'mandatory_measure')
//				&& // field is a measure and there are options for measures
//				(Sbi.worksheet.config.options.measures && Sbi.worksheet.config.options.measures.length > 0)
//			) {
//			this.contextMenu.setField(field);
//			this.contextMenu.showAt(e.getXY());
//		}
	}

	, showOptions : function (item) {
		Ext.Msg.alert('Message', 'showOptions');

//		var field = item.parentMenu.getField();
//		var optionsToDisplay = null;
//		if (field.nature == 'attribute' || field.nature == 'segment_attribute') {
//			optionsToDisplay = Sbi.worksheet.config.options.attributes;
//		} else {
//			optionsToDisplay = Sbi.worksheet.config.options.measures;
//		}
//
//     	var optionsWindow = new Sbi.worksheet.config.OptionsWindow({
//     		options : optionsToDisplay
//     	});
//     	optionsWindow.on('render', function(theWindow) {
//     		var state = field.options;
//     		theWindow.setFormState(state);
//     	}, this);
//     	optionsWindow.on('apply', function(theWindow, formState) {
//     		field.options = formState;
//     	}, this);
//     	optionsWindow.show();
	}
	, fake : function () {

	}
});
