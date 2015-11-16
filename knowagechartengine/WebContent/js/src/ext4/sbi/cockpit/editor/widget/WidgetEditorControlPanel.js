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
 * Authors - Antonella Giachino (antonella.giachino@eng.it)
 */

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditorControlPanel = function(config) {

	var defaultSettings = {
		border: false,
		width: 275
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorControlPanel', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.services = this.services || new Array();
	var baseParams = {};

	if (c.dataset) {
		baseParams.dataset = c.dataset;
	}

	this.addEvents("attributeDblClick", "fieldRightClick", "validateInvalidFieldsAfterLoad");


	this.initPanels();

	c = {
        layout: {
        	type:'accordion'
        },
        items:[ this.designerPalettePanel, this.fieldsPalettePanel]
//        items:[ this.designerPalettePanel, this.fieldsPalettePanel, this.stylePalettePanel]
	};

	Sbi.cockpit.editor.widget.WidgetEditorControlPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorControlPanel, Ext.Panel, {
	fieldsPalettePanel: null,
	designerPalettePanel: null,
//	stylePalettePanel: null,
	globalFilters: null,
	fieldsOptions: null, // JSON object that contains options for attributes (code/description visualization) and measures (scale factor)

	initPanels: function() {

		this.designerPalettePanel = new Sbi.cockpit.editor.widget.WidgetEditorDesignerPalette({});

		this.fieldsPalettePanel = new Sbi.cockpit.editor.widget.WidgetEditorFieldPalette({
			border: false,
	        gridConfig: {
	        },
			region : 'center',
			split: true,
			height : 120,
			width: 275,
			dataset: this.dataset,
			services : this.services
		});
		this.fieldsPalettePanel.store.on('load', this.fieldsLoadedHandler, this);
		this.fieldsPalettePanel.store.on('beforeload', this.getGlobalFilters, this); // forces a calculation of global filters
		this.fieldsPalettePanel.grid.on('rowdblclick', this.fieldDblClickHandler, this);
		this.fieldsPalettePanel.grid.on('rowcontextmenu', this.fieldRightClickHandler, this);


//		this.stylePalettePanel = new Sbi.worksheet.designer.stylePalettePanel({region : 'south', height : 130 , split: true});
//
//		this.stylePalettePanel.on('layoutchange', function(sheetLayout){
//			var change = {
//				'sheetLayout' : sheetLayout
//			};
//			this.fireEvent('toolschange',change);
//		}, this);

//		this.stylePalettePanel  = new Ext.Panel({html: "Layout Panel"});
	}

	, fieldDblClickHandler : function (grid, rowIndex, event) {
		var record = grid.store.getAt(rowIndex);
		if (record.data.nature == 'attribute' || record.data.nature == 'segment_attribute') {
	     	this.fireEvent("attributeDblClick", this, record.data);
		}
	}

	, fieldsLoadedHandler : function (store, records, options) {
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
		if (this.globalFilters){
			for (var i = 0; i < this.globalFilters.length; i++) {
				var aGlobalFilter = this.globalFilters[i];
				if (record.data.alias == aGlobalFilter.alias) {
					toReturn = aGlobalFilter;
					break;
				}
			}
		}
		return toReturn;
	}

	, getOptionsForRecord : function (record) {
		var toReturn = null;
		if ( this.fieldsOptions != null) {
			for (var i = 0; i < this.fieldsOptions.length; i++) {
				var aFieldOptions = this.fieldsOptions[i];
				if (record.data.alias == aFieldOptions.alias) {
					toReturn = aFieldOptions;
					break;
				}
			}
		}
		return toReturn;
	}

	//Update the tools info for the active sheet
//	, updateToolsForActiveTab: function(activeSheet){
//		if ( activeSheet.sheetLayout !== null ) {
//			this.stylePalettePanel.setLayoutValue(activeSheet.sheetLayout);
//		}
//	}

	, refresh: function(){
		this.fieldsPalettePanel.refresh();
		this.fieldsPalettePanel.on('validateInvalidFieldsAfterLoad',
				function(){
					this.fireEvent("validateInvalidFieldsAfterLoad", this);
		}, this);

	}

    , getFields : function () {
    	return this.fieldsPalettePanel.getFields();
    }
    , getFieldsPalettePanel : function () {
    	return this.fieldsPalettePanel;
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
				this.globalFilters.push(aField);
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

	, updateValues: function(values){
		Sbi.trace("[WidgetEditorControlPanel.updateValues]: IN");
		if(values && values.selectedDatasetLabel) {
			this.fieldsPalettePanel.refreshFieldsList(values.selectedDatasetLabel);
		}
		Sbi.trace("[WidgetEditorControlPanel.updateValues]: OUT");
	}


});
