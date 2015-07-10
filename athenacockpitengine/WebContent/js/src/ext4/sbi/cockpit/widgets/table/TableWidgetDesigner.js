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
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.ns("Sbi.cockpit.widgets.table");

Sbi.cockpit.widgets.table.TableWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'tableWidgetDesigner',
		title: LN('sbi.cockpit.widgets.table.tableWidgetDesigner.title')
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.table && Sbi.settings.cockpit.widgets.table.tableWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.table.tableWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	this.addEvents("attributeDblClick", "attributeRemoved");

	this.tableDesigner = new Sbi.cockpit.widgets.table.QueryFieldsCardPanel({
		ddGroup: this.ddGroup,
		title: 'Table Designer',
		wcId: this.wcId || this.ddGroup.substring(0, this.ddGroup.indexOf("__"))
			
	});
	
	this.initTableOptionsTab();
	this.initFontOptionsTab();
	

	// propagate events
	this.tableDesigner.on(
		'attributeDblClick' ,
		function (thePanel, attribute) {
			//this.fireEvent("attributeDblClick", this, attribute);
			this.attributeDblClickHandler(attribute, thePanel);
		},
		this
	);
	this.tableDesigner.on(
		'attributeRemoved' ,
		function (thePanel, attribute) {
			this.fireEvent("attributeRemoved", this, attribute);
		},
		this
	);

	this.tableDesigner.on(
		'beforerender' ,
		function (thePanel, attribute) {
			
			var state = {};
			
			if(Sbi.isValorized(this.visibleselectfields)) {
				
				state.visibleselectfields = this.visibleselectfields
			}
			
			state.maxRowsNumber = this.maxRowsNumber;
			state.hideGrid = this.hideGrid;
			state.lineSize = this.lineSize;
			state.gridColor = this.gridColor;
			state.alternateRowsColors = this.alternateRowsColors;
			state.alternateRowsColorsFirst = this.alternateRowsColorsFirst;
			state.alternateRowsColorsSecond = this.alternateRowsColorsSecond;
			state.summaryRow = this.summaryRow;
			state.summaryRowBackgroundColor = this.summaryRowBackgroundColor;
			state.summaryRowFormula = this.summaryRowFormula;
			
			this.setFontStateBeforeRender(this, state);
				
			this.setDesignerState(state);
		},
		this
	);

	
	var tabPanel = Ext.create('Ext.tab.Panel', {
		        	tabPosition: 'right'
		        	, border: false
		        	, margin: 0
		        	, padding: 0
		        	, bodyStyle: 'width: 100%; height: 100%'
		        	, items:[this.tableDesigner, this.tableConfigurationPanel, this.fontConfigurationPanel]
		        	//, html: "tableDesigner"
		        });

	c = {
		layout: 'fit',
		height: 400,
		items: [ tabPanel ]
	};

	Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.cockpit.widgets.table.TableWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	tableDesigner: null
	
	//field to select widget font type
	, fontTypeCombo: null
	//field to select widget font size
	, fontSizeCombo: null
	//field to select header font size
	, headerFontSizeCombo: null
	//field to select header font color
	, headerFontColorText: null
	//field to select header font weight
	, headerFontWeightCombo: null
	//field to select header font decoration
	, headerFontDecorationCombo: null
	//field to select rows font size
	, rowsFontSizeCombo: null
	//field to select rows font color
	, rowsFontColorText: null
	//field to select rows font weight
	, rowsFontWeightCombo: null
	//field to select rows font decoration
	, rowsFontDecorationCombo: null
	//panel to show font size options
	, fontConfigurationPanel: null
	
	
	//field to set the max rows limit
	, maxRowsNumberField: null
	//checkbox for grid hiding
	, hideGridCheckBox: null
	//panel for table options
	, tableConfigurationPanel: null
	//field to set the line size
	, lineSizeField: null
	//field to set the grid color
	, gridColorField: null
	
	//fields to set the grid alternate rows colors
	, alternateRowsColorsContainer: null
	, alternateRowsColorsChBox: null
	, alternateRowsColorsFirstColorField: null
	, alternateRowsColorsSecondColorField: null
	
	, summaryRowChBox: null
	, summaryRowBackgroundColorField: null
	, summaryRowFormulaField: null
	
	
	// =================================================================================================================
	// METHODS
	// =================================================================================================================



	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, initFontOptionsTab: function(){
		
		var fontSizeStore =  Ext.create('Sbi.fonts.stores.FontSizeStore',{});
		
		var fontFamilyStore = Ext.create('Sbi.fonts.stores.FontFamilyStore', {});
		
		var fontDecorationStore = Ext.create('Sbi.fonts.stores.FontDecorationStore', {});

		var rowsFontDecorationStore = Ext.create('Sbi.fonts.stores.FontDecorationStore', {});
		
		var fontWeightStore = Ext.create('Sbi.fonts.stores.FontWeightStore', {});
		
		var hexColorReg = new RegExp("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
		
		/* table font general options */
		
		this.fontTypeCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontType'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontFamilyStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontType',
			labelWidth:		110,
			width:			245

		});
		
		this.fontSizeCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontSizeStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontSize',
			labelWidth:		120,
			width:			170

		});
		
		var tableGeneralFontOptions = {
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.tableFontGeneralOpts')
	        , margin: 			10
	    	, items: 			[this.fontTypeCombo, this.fontSizeCombo]	
			, width:			600
		};
		
		
		/* table font header options */
		
		this.headerFontSizeCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'headerFontSize',
			labelWidth:		130,
			width:			180,
		});
		 
		this.headerFontColorText = Ext.create('Ext.ux.FontColorField', { 
			msgTarget: 	'qtip', 
			fallback: 		true,
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontColor'),
			afterLabelTextTpl : '<span class="help" data-qtip="'
	            	+ LN('sbi.cockpit.designer.fontConf.fontColor.info')
	            	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
			name: 			'headerFontColor',
			allowBlank: 	true,
			labelWidth:	140,
			width:			255,
		});
		 
		this.headerFontWeightCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontWeight'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontWeightStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'headerFontWeight',
			labelWidth:		130,
			width:			245
		});
		 
		this.headerFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontDecoration'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontDecorationStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'headerFontDecoration',
			labelWidth:		140,
			width:			255
		});
		
		var tableHeaderFontOptions = {
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.tableHeaderFontOptions')
	    	, margin: 			10
	    	, items: 			[this.headerFontSizeCombo, this.headerFontColorText, this.headerFontWeightCombo, this.headerFontDecorationCombo]	
			, width:			600
		};
		
		 
		 /* table font rows options */
		 
		this.rowsFontSizeCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			lazyRender:		true,
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'rowsFontSize',
			labelWidth:		130,
			width:			180
		});
		 
		this.rowsFontColorText = Ext.create('Ext.ux.FontColorField', { 
			msgTarget: 		'qtip', 
			fallback: 		true,
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontColor'),
			afterLabelTextTpl : '<span class="help" data-qtip="'
	            	+ LN('sbi.cockpit.designer.fontConf.fontColor.info')
	            	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
			name: 			'rowsFontColor',
			allowBlank: 	true,
			labelWidth:		140,
			width:			255,
			 
		});
		 
		this.rowsFontWeightCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontWeight'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontWeightStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'rowsFontWeight',
			labelWidth:		130,
			width:			245
		});
		 
		this.rowsFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontDecoration'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			rowsFontDecorationStore, 
			valueField: 	'name',
			displayField: 	'description',
			name:			'rowsFontDecoration',
			labelWidth:		140,
			width:			255
		});
		 
		var tableRowsFontOptions = {
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.tableRowsFontOptions')
	    	, margin: 			10
	    	, items: 			[this.rowsFontSizeCombo, this.rowsFontColorText, this.rowsFontWeightCombo, this.rowsFontDecorationCombo]	
			, width:			600
		};
		 
		this.fontConfigurationPanel = {
			xtype: 				'panel'
			, layout: {
			    type: 'table',
			    columns: 1
			}
	        , title: 			LN('sbi.cockpit.designer.fontConf.fontOptions')
	    	, items: 			[tableGeneralFontOptions, tableHeaderFontOptions, tableRowsFontOptions]	
		};
	}

	, initTableOptionsTab: function(){
		
		var tableOptionsLabelWidths = 140;
		var tableOptionsWidths = 300;
		var tableOptionsMargins = '5 0 0 5';
//		var tableOptionsMargins = 0;
		
		var tableOptionsFieldSetMargins = '5 0 0 5';
		var tableOptionsFieldSetPadding = '0 5 5 0';
		
		
		this.maxRowsFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: 	false,
			margin: 		tableOptionsFieldSetMargins,
			padding: 		tableOptionsFieldSetPadding,
			layout: 		'anchor',
			items : 		[]
		});
		
		this.maxRowsNumberField = Ext.create('Ext.form.field.Number',{
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.maxrowsnumber'),
			name: 				'maxRowsNumber',
	        allowBlank: 		true,
	        minValue: 0,
			labelWidth:		tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:			tableOptionsMargins
		});
		this.maxRowsFieldSet.add(this.maxRowsNumberField);
		
		
		this.gridFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: 	false,
			margin: 		tableOptionsFieldSetMargins,
			padding: 		tableOptionsFieldSetPadding,
			layout: 		'anchor',
			items : 		[]
		});
		
		this.hideGridCheckBox = Ext.create('Ext.form.field.Checkbox',{
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.hidegridlabel'),
			name: 				'hideGridCheckBox',
			allowBlank: 		true,
			labelWidth:			tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins,
		});
		this.gridFieldSet.add(this.hideGridCheckBox);
		
		this.lineSizeField = Ext.create('Ext.form.field.Number',{
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.linesize'),
			name: 				'lineSizeNumber',
			allowBlank: 		true,
			minValue: 0,
			labelWidth:			tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins, 
			tip: 				'This is a tip lineSizeField',
		});
		this.gridFieldSet.add(this.lineSizeField);
		
		this.gridColorField = Ext.create('Ext.ux.FontColorField', { 
			name: 				'gridColor',
			msgTarget: 			'qtip', 
			fallback: 			true,
			allowBlank: 		true,
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.gridcolor'),
			labelWidth:			tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins
		});
		this.gridFieldSet.add(this.gridColorField);
		
		var lineSizeField = this.lineSizeField,
			gridColorField = this.gridColorField;
		
		this.hideGridCheckBox.on('change', function(chBox){
			lineSizeField.setDisabled(chBox.getValue());
			gridColorField.setDisabled(chBox.getValue());
		});
		
		
		this.alternateRowsColorsChBox = Ext.create('Ext.form.field.Checkbox', {
			name: 				'alternateRowsColorsChBox',
            flex: 1
		});
		
		this.alternateRowsColorsFirstColorField = Ext.create('Ext.ux.FontColorField', { 
			name: 				'alternateRowsColorsFirstColorField',
			msgTarget: 			'qtip', 
			fallback: 			true,
			allowBlank: 		true,
			margin:				'0 0 0 5',
            flex: 3
		});
		
		this.alternateRowsColorsSecondColorField = Ext.create('Ext.ux.FontColorField', { 
			name: 				'alternateRowsColorsSecondColorField',
			msgTarget: 			'qtip', 
			fallback: 			true,
			allowBlank: 		true,
			margin:				'0 0 0 5',
            flex: 3
		});

		var alternateRowsColorsFirstColorField = this.alternateRowsColorsFirstColorField,
			alternateRowsColorsSecondColorField = this.alternateRowsColorsSecondColorField;
		
		this.alternateRowsColorsChBox.on('change', function(chBox){
			alternateRowsColorsFirstColorField.setDisabled(!chBox.getValue());
			alternateRowsColorsSecondColorField.setDisabled(!chBox.getValue());
		});
		
		this.alternateRowsFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: 	false,
			margin: 		tableOptionsFieldSetMargins,
			padding: 		tableOptionsFieldSetPadding,
			layout: 		'anchor',
			items : 		[]
		});
		
		this.alternateRowsColorsContainer = Ext.create('Ext.form.FieldContainer', {
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.gridrowscolors'),
			labelWidth:			tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins,
			layout: 			'hbox',
			items: [
			        this.alternateRowsColorsChBox,
			        this.alternateRowsColorsFirstColorField,
			        this.alternateRowsColorsSecondColorField,
			        ]
			
		});
		this.alternateRowsFieldSet.add(this.alternateRowsColorsContainer);
		
		this.summaryRowFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: 	false,
			margin: 		tableOptionsFieldSetMargins,
			padding: 		tableOptionsFieldSetPadding,
			layout: 		'anchor',
			items : 		[]
		});
		
		this.summaryRowChBox = Ext.create('Ext.form.field.Checkbox', {
			name: 				'summaryRowChBox',
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.summaryrow'),
			labelWidth:			tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins
		});
		this.summaryRowFieldSet.add(this.summaryRowChBox);
		
		this.summaryRowBackgroundColorField = Ext.create('Ext.ux.FontColorField', { 
			name: 				'summaryRowBackgroundColorField',
			fieldLabel: 		LN('sbi.qbe.selectgridpanel.backgroundcolor.label'),
			labelWidth:			tableOptionsLabelWidths,
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins,
			msgTarget: 			'qtip', 
			fallback: 			true,
			allowBlank: 		true
		});
		this.summaryRowFieldSet.add(this.summaryRowBackgroundColorField);
		
		this.summaryRowFormulaField = Ext.create('Ext.form.field.TextArea', {
			name: 				'summaryRowFormulaField',
			fieldLabel: 		LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.summaryrowformula'),
			labelWidth:			tableOptionsLabelWidths,
			labelAlign: 		'top',
			width:				tableOptionsWidths,
			margin:				tableOptionsMargins,
			allowBlank: 		true
		});
		this.summaryRowFieldSet.add(this.summaryRowFormulaField);
		
		var summaryRowBackgroundColorField = this.summaryRowBackgroundColorField,
			summaryRowFormulaField = this.summaryRowFormulaField;
		
		this.summaryRowChBox.on('change', function(chBox){
			summaryRowBackgroundColorField.setDisabled(!chBox.getValue());
			summaryRowFormulaField.setDisabled(!chBox.getValue());
		});
		
		this.tableConfigurationPanel = 
		{
			xtype:		'panel'
			, layout: 	{ type: 'table', columns: 1 }
	        , title: 	LN('sbi.cockpit.widgets.table.tabledesignerpanel.tableoptions.title')
	    	, items: 	[
	    	         	 this.maxRowsFieldSet,
	    	         	 this.gridFieldSet,
	    	         	 this.alternateRowsFieldSet,
	    	         	 this.summaryRowFieldSet
	    	         	 ]	
		};
		
	}

	, getDesignerState: function(running) {
		Sbi.trace("[TableWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.getDesignerState(this);
		state.wtype = 'table';
		if(this.tableDesigner.rendered === true) {
			state.visibleselectfields = this.tableDesigner.tableDesigner.getContainedValues();
		} else {
			state.visibleselectfields =  this.visibleselectfields;
		}
		
		/* START Table options */
		state.maxRowsNumber = this.maxRowsNumberField.getValue();
		state.hideGrid = this.hideGridCheckBox.getValue();
		state.lineSize = this.lineSizeField.getValue();
		state.gridColor = this.gridColorField.getValue();
		state.alternateRowsColors = this.alternateRowsColorsChBox.getValue();
		state.alternateRowsColorsFirst = this.alternateRowsColorsFirstColorField.getValue();
		state.alternateRowsColorsSecond = this.alternateRowsColorsSecondColorField.getValue();
		state.summaryRow = this.summaryRowChBox.getValue();
		state.summaryRowBackgroundColor = this.summaryRowBackgroundColorField.getValue();
		state.summaryRowFormula = this.summaryRowFormulaField.getValue();
		/* END Table options */
		
		this.getFontState(state);		

		// if all measures are aggregate set category and series: category are attributes, seriesare measure with aggregation function
		var atLeastOneAggregate = false;
		var areAllMeasureAggregate = true;
		var measureNumber = 0;

		for (var i = 0; i < state.visibleselectfields.length; i++) {
			var  field = state.visibleselectfields[i];
			if(field.nature == 'measure'){
				measureNumber++;
				if(field.funct != null && field.funct != 'NaN' && field.funct != '' ){
					atLeastOneAggregate = true;
				}
				if(field.funct == null || field.funct == 'NaN' || field.funct == ''){
					areAllMeasureAggregate = false;
				}
			}
		}

		if(running != undefined && running === true){
			if(atLeastOneAggregate == true && areAllMeasureAggregate==false){
				Sbi.exception.ExceptionHandler.showWarningMessage(LN("sbi.cockpit.TableWidgetDesigner.notAllMeasureAggregated"), "Warning");
				throw new Error(LN("sbi.cockpit.TableWidgetDesigner.notAllMeasureAggregated"));
			}
		}

		var toAggregate = false;
		if(measureNumber > 0 && areAllMeasureAggregate == true){
			toAggregate = true;
			state.category = new Array();
			state.series = new Array();

			// calculate category and series
			for (var i = 0; i < state.visibleselectfields.length; i++) {
				var  field = state.visibleselectfields[i];
				if(field.nature == 'attribute' || field.nature == 'segment_attribute'){
					state.category.push(field);
				}
				else if(field.nature == 'measure'){
					state.series.push(field);
				}
			}
		}


		Sbi.trace("[TableWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[TableWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.setDesignerState(this, state);
		
		if (state.maxRowsNumber) this.maxRowsNumberField.setValue(state.maxRowsNumber);
		if (state.hideGrid != undefined) this.hideGridCheckBox.setValue(state.hideGrid);
		if (state.lineSize) this.lineSizeField.setValue(state.lineSize);
		if (state.gridColor) this.gridColorField.setValue(state.gridColor);
		if (state.alternateRowsColors != undefined) {
			this.alternateRowsColorsChBox.setValue(state.alternateRowsColors);
			this.alternateRowsColorsFirstColorField.setDisabled(!state.alternateRowsColors);
			this.alternateRowsColorsSecondColorField.setDisabled(!state.alternateRowsColors);
		}
		if (state.alternateRowsColorsFirst) this.alternateRowsColorsFirstColorField.setValue(state.alternateRowsColorsFirst);
		if (state.alternateRowsColorsSecond) this.alternateRowsColorsSecondColorField.setValue(state.alternateRowsColorsSecond);
		if (state.summaryRow != undefined) {
			this.summaryRowChBox.setValue(state.summaryRow);
			this.summaryRowBackgroundColorField.setDisabled(!state.summaryRow);
			this.summaryRowFormulaField.setDisabled(!state.summaryRow);
		}
		if (state.summaryRowBackgroundColor) this.summaryRowBackgroundColorField.setValue(state.summaryRowBackgroundColor);
		if (state.summaryRowFormula) this.summaryRowFormulaField.setValue(state.summaryRowFormula);
	
		this.setFontState(state);
		
		if(state.visibleselectfields!=undefined && state.visibleselectfields!=null){
			Sbi.trace("[TableWidgetDesigner.setDesignerState]: there are [" + state.visibleselectfields.length + "] fields slected");
			this.tableDesigner.tableDesigner.setValues(state.visibleselectfields);
		} else {
			Sbi.trace("[TableWidgetDesigner.setDesignerState]: no fields selected");
		}
		
		Sbi.trace("[TableWidgetDesigner.setDesignerState]: OUT");
	}

	/* tab validity: rules are
	 * - at least one measure or attribute is in
	 */
	, validate: function(validFields){

		var valErr = Sbi.cockpit.widgets.table.TableWidgetDesigner.superclass.validate(this, validFields);
		if(valErr!= ''){
			return varErr;
		}

		valErr = ''+this.tableDesigner.validate(validFields);

		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1);
			return LN("sbi.cockpit.widgets.table.validation.invalidFields")+valErr;
		}

		var vals = this.tableDesigner.tableDesigner.getContainedValues();
		if (vals && vals.length> 0) {return;} // OK
		else {
				return LN("sbi.designertable.tableValidation.noElement");
		}
	}

	, containsAttribute: function (attributeId) {
		return this.tableDesigner.containsAttribute(attributeId);
	}

	, attributeDblClickHandler : function (thePanel, attribute, theSheet) {

	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, setFontStateBeforeRender: function(thePanel, state){
		Sbi.trace("[TableWidgetDesigner.setFontStateBeforeRender]: IN");
		
		var tableFonts = this.findTableFont()
		
		if(tableFonts !== undefined && tableFonts !== null){
			
			if(thePanel.fontType === undefined || thePanel.fontType === null){
				state.fontType = tableFonts.fontType;
			}else{
				state.fontType = thePanel.fontType;
			}
			
			if(thePanel.fontSize === undefined || thePanel.fontSize === null){
				state.fontSize = tableFonts.fontSize;
			}else{
				state.fontSize = thePanel.fontSize;
			}

			
			//header font
			if(thePanel.headerFontSize === undefined || thePanel.headerFontSize === null){
				state.headerFontSize = tableFonts.headerFontSize;
			}else{
				state.headerFontSize = thePanel.headerFontSize;
			}
			
			if(thePanel.headerFontColor === undefined || thePanel.headerFontColor === null){
				state.headerFontColor = tableFonts.headerFontColor;
			}else{
				state.headerFontColor = thePanel.headerFontColor;
			}
			
			if(thePanel.headerFontWeight === undefined || thePanel.headerFontWeight === null){
				state.headerFontWeight = tableFonts.headerFontWeight;
			}else{
				state.headerFontWeight = thePanel.headerFontWeight;
			}
			
			if(thePanel.headerFontDecoration === undefined || thePanel.headerFontDecoration === null){
				state.headerFontDecoration = tableFonts.headerFontDecoration;
			}else{
				state.headerFontDecoration = thePanel.headerFontDecoration;
			}
			
			//rows font
			if(thePanel.rowsFontSize === undefined || thePanel.rowsFontSize === null){
				state.rowsFontSize = tableFonts.rowsFontSize;
			}else{
				state.rowsFontSize = thePanel.rowsFontSize;
			}
			
			if(thePanel.rowsFontColor === undefined || thePanel.rowsFontColor === null){
				state.rowsFontColor = tableFonts.rowsFontColor;
			}else{
				state.rowsFontColor = thePanel.rowsFontColor;
			}
			
			if(thePanel.rowsFontWeight === undefined || thePanel.rowsFontWeight === null){
				state.rowsFontWeight = tableFonts.rowsFontWeight;
			}else{
				state.rowsFontWeight = thePanel.rowsFontWeight;
			}
			
			if(thePanel.rowsFontDecoration === undefined || thePanel.rowsFontDecoration === null){
				state.rowsFontDecoration = tableFonts.rowsFontDecoration;
			}else{
				state.rowsFontDecoration = thePanel.rowsFontDecoration;
			}			
			
		}else{
			
			state.fontType = thePanel.fontType;
			state.fontSize = thePanel.fontSize;
			
			state.headerFontSize = thePanel.headerFontSize;
			state.headerFontColor = thePanel.headerFontColor;
			state.headerFontWeight = thePanel.headerFontWeight;
			state.headerFontDecoration = thePanel.headerFontDecoration;
			
			state.rowsFontSize = thePanel.rowsFontSize;
			state.rowsFontColor = thePanel.rowsFontColor;
			state.rowsFontWeight = thePanel.rowsFontWeight;
			state.rowsFontDecoration = thePanel.rowsFontDecoration;
			
		}
		
		Sbi.trace("[TableWidgetDesigner.setFontStateBeforeRender]: OUT");		
	}
	
	, setFontState: function(state){
		Sbi.trace("[TableWidgetDesigner.setFontState]: IN");
		
		if (state.fontType) this.fontTypeCombo.setValue(state.fontType);
		if (state.fontSize) this.fontSizeCombo.setValue(state.fontSize);
		//header font
		if (state.headerFontSize) this.headerFontSizeCombo.setValue(state.headerFontSize);
		if (state.headerFontColor) this.headerFontColorText.setValue(state.headerFontColor);
		if (state.headerFontWeight) this.headerFontWeightCombo.setValue(state.headerFontWeight);
		if (state.headerFontDecoration) this.headerFontDecorationCombo.setValue(state.headerFontDecoration);
		//rows font		
		if (state.rowsFontSize) this.rowsFontSizeCombo.setValue(state.rowsFontSize);
		if (state.rowsFontColor) this.rowsFontColorText.setValue(state.rowsFontColor);
		if (state.rowsFontWeight) this.rowsFontWeightCombo.setValue(state.rowsFontWeight);
		if (state.rowsFontDecoration) this.rowsFontDecorationCombo.setValue(state.rowsFontDecoration);		
		
		Sbi.trace("[TableWidgetDesigner.setFontState]: OUT");		
	}
	
	, findTableFont: function(){
		Sbi.trace("[TableWidgetDesigner.findTableFont]: IN");
		
		var tableFonts = Sbi.storeManager.getFont("tableFonts");
		
		return tableFonts		
		
		Sbi.trace("[TabletWidgetDesigner.findTableFont]: OUT");		
	}
	
	, getFontState: function(state){
		Sbi.trace("[TableWidgetDesigner.getFontState]: IN");
		
		//blank values are permitted, so we need to check the objects before call .getValue()
		if(this.fontTypeCombo !== null)
		{	
			state.fontType = this.fontTypeCombo.getValue();
		}
		if(this.fontSizeCombo !== null)
		{	
			state.fontSize = this.fontSizeCombo.getValue();
		}

		//header font
		if(this.headerFontSizeCombo !== null) {	
			state.headerFontSize = this.headerFontSizeCombo.getValue();
		}
		
		if(this.headerFontColorText !== null) {	
			state.headerFontColor = this.headerFontColorText.getValue();
		}
		
		if(this.headerFontWeightCombo !== null) {	
			state.headerFontWeight = this.headerFontWeightCombo.getValue();
		}
		
		if(this.headerFontDecorationCombo !== null) {	
			state.headerFontDecoration = this.headerFontDecorationCombo.getValue();
		}
		
		//rows font
		if(this.rowsFontSizeCombo !== null) {
			state.rowsFontSize = this.rowsFontSizeCombo.getValue();
		}
		
		if(this.rowsFontColorText !== null) {	
			state.rowsFontColor = this.rowsFontColorText.getValue();
		}
		
		if(this.rowsFontWeightCombo !== null) {	
			state.rowsFontWeight = this.rowsFontWeightCombo.getValue();
		}
		
		if(this.rowsFontDecorationCombo !== null) {	
			state.rowsFontDecoration = this.rowsFontDecorationCombo.getValue();
		}
				
		Sbi.trace("[TableWidgetDesigner.getFontState]: OUT");		
	}
});
