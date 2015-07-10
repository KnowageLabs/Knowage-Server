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
 * updateToolsForActiveTab(activeSheet): update the tools: take the configuration of the activeSheet
 * and update the tools
 * 
 * 
 * Public Events
 * 
 * toolschange(change): the value of the tools is changed.. change a map with the change value.
 * for example {layout: layout-header}
 * 
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.DesignToolsPanel = function(config) { 

	var defaultSettings = {
		border: false
	};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.designTools) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.designTools);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);
	
	this.services = this.services || new Array();	
	this.services['getQueryFields'] = this.services['getQueryFields'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_WORKSHEET_FIELDS_ACTION'
		, baseParams: new Object()
	});
	
	this.addEvents("attributeDblClick", "fieldRightClick", "validateInvalidFieldsAfterLoad");

	
	this.initPanels();
	
	c = {
        layout: {
        	//type:'border'
        	type:'accordion'
        	//type:'vbox',
            //align:'stretch'
        },
        items:[ this.designToolsPallettePanel, this.designToolsFieldsPanel, this.designToolsLayoutPanel]
	};
	
	Sbi.worksheet.designer.DesignToolsPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.worksheet.designer.DesignToolsPanel, Ext.Panel, {
	designToolsFieldsPanel: null,
	designToolsPallettePanel: null,
	designToolsLayoutPanel: null,
	globalFilters: null,
	fieldsOptions: null, // JSON object that contains options for attributes (code/description visualization) and measures (scale factor)

	initPanels: function() {

		this.designToolsPallettePanel = new Sbi.worksheet.designer.DesignToolsPallettePanel({region : 'north'});
		
		
		this.designToolsFieldsPanel = new Sbi.formbuilder.QueryFieldsPanel({
			displayRefreshButton : false,
			border: false,
	        gridConfig: {
				ddGroup: 'worksheetDesignerDDGroup'
	        	, type: 'queryFieldsPanel'
	        },
			region : 'center',
			split: true,
			height : 120,
			services : this.services
		});
		this.designToolsFieldsPanel.store.on('load', this.fieldsLoadedHandler, this);
		this.designToolsFieldsPanel.store.on('beforeload', this.getGlobalFilters, this); // forces a calculation of global filters
		this.designToolsFieldsPanel.grid.on('rowdblclick', this.fieldDblClickHandler, this);
		this.designToolsFieldsPanel.grid.on('rowcontextmenu', this.fieldRightClickHandler, this);
		

		this.designToolsLayoutPanel = new Sbi.worksheet.designer.DesignToolsLayoutPanel({region : 'south', height : 130 , split: true});

		//		this.designToolsFieldsPanel.flex = 1;
		//		this.designToolsPallettePanel.flex = 1;
		//		this.designToolsLayoutPanel.flex = 1;
		this.designToolsLayoutPanel.on('layoutchange', function(sheetLayout){
			var change = {
				'sheetLayout' : sheetLayout
			};
			this.fireEvent('toolschange',change);
		}, this);
	}

	, fieldDblClickHandler : function (grid, rowIndex, event) {
		var record = grid.store.getAt(rowIndex);
		if (record.data.nature == 'attribute' || record.data.nature == 'segment_attribute') {
	     	this.fireEvent("attributeDblClick", this, record.data);
		}
	}
	
	, fieldsLoadedHandler : function (store, records, options) {
		smartFilterMetadata = Sbi.commons.JSON.decode(store.getMetadata());
		store.each(this.initOptions, this);
		store.each(this.initAttributeValues, this);
	}

	, initAttributeValues : function (record) {
		var globalFilter = this.getGlobalFilterForRecord(record);
		if (globalFilter != null) {
			// global filter was found
			record.data.values = globalFilter.values;
		} else {
			// global filter was not found
			record.data.values = '[]';
		}
	}
	
	, initOptions : function (record) {
		var recordWithOptions = this.getOptionsForRecord(record);
		if (recordWithOptions != undefined && recordWithOptions != null) {
			// options were found
			record.data.options = recordWithOptions.options;
		} else {
			// options were not found
			record.data.options = {};
		}
	}
	
	, getGlobalFilterForRecord : function (record) {
		var toReturn = null;
		for (var i = 0; i < this.globalFilters.length; i++) {
			var aGlobalFilter = this.globalFilters[i];
			if (record.data.alias == aGlobalFilter.alias) {
				toReturn = aGlobalFilter;
				break;
			}
		}
		return toReturn;
	}
	
	, getOptionsForRecord : function (record) {
		var toReturn = null;
		for (var i = 0; i < this.fieldsOptions.length; i++) {
			var aFieldOptions = this.fieldsOptions[i];
			if (record.data.alias == aFieldOptions.alias) {
				toReturn = aFieldOptions;
				break;
			}
		}
		return toReturn;
	}
	
	//Update the tools info for the active sheet
	, updateToolsForActiveTab: function(activeSheet){
		if ( activeSheet.sheetLayout !== null ) {
			this.designToolsLayoutPanel.setLayoutValue(activeSheet.sheetLayout);
		}
	}
	
	, refresh: function(){
		this.designToolsFieldsPanel.refresh();
		this.designToolsFieldsPanel.on('validateInvalidFieldsAfterLoad', 
				function(){
					this.fireEvent("validateInvalidFieldsAfterLoad", this); 	
		}, this);
		
	}
	
    , getFields : function () {
    	return this.designToolsFieldsPanel.getFields();
    }
    , getDesignToolsFieldsPanel : function () {
    	return this.designToolsFieldsPanel;
    }    
	, getGlobalFilters : function () {
		var fields = this.getFields();
		if (fields.length == 0) {
			// fields were not loaded
			return this.globalFilters;
		}
		// fields were already loaded and initialized by the fieldsLoadedHandler function
		this.globalFilters = [];
		for (var i = 0; i < fields.length; i++) {
			var aField = fields[i];
			if (aField.values != '[]') {
				this.globalFilters.push(Ext.apply({}, aField)); // we clone the field in order to solve https://spagobi.eng.it/jira/browse/SPAGOBI-1530
			}
		}
		return this.globalFilters;
	}
	
	, getFieldsOptions : function () {
		var fields = this.getFields();
		if (fields.length == 0) {
			// fields were not loaded
			return this.fieldsOptions;
		}
		// fields were already loaded and initialized by the fieldsLoadedHandler function
		this.fieldsOptions = [];
		for (var i = 0; i < fields.length; i++) {
			var aField = fields[i];
			if ( !Sbi.qbe.commons.Utils.isEmpty(aField.options) ) { // stands for if aField.options != {}
				this.fieldsOptions.push(aField);
			}
		}
		return this.fieldsOptions;
	}
    
	, setGlobalFilters : function (globalFilters) {
		this.globalFilters = globalFilters;
	}
	
	, setFieldsOptions : function (fieldsOptions) {
		this.fieldsOptions = fieldsOptions;
	}
	
	, getGlobalFilterForAttribute : function (attribute) {
		var toReturn = null;
		var globalFilters = this.getGlobalFilters();
		for (var i = 0; i < globalFilters.length; i++) {
			var aGlobalFilter = globalFilters[i];
			if (attribute.alias == aGlobalFilter.alias) {
				toReturn = aGlobalFilter;
				break;
			}
		}
		return toReturn;
	}
	
	, fieldRightClickHandler : function ( grid, rowIndex, e ) {
		var record = grid.store.getAt(rowIndex);
		this.fireEvent("fieldRightClick", this, record.data, e);
	}

	
});
