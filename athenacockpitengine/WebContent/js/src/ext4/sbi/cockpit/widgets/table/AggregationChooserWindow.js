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
 * Authors - Giulio Gavardi
 */
Ext.ns("Sbi.worksheet.designer");

//Sbi.cockpit.widgets.table.AggregationChooserWindow = function(defAlias, defAggregation, defNature) {
Sbi.cockpit.widgets.table.AggregationChooserWindow = function(defFormState) {
	
	var c = {
		title: LN('sbi.cockpit.widgets.table.tabledesignerpanel.configure')//LN('sbi.cockpit.aggregationwindow.title')
		, minWidth: 500
		, minHeight: 200
		, resizable : false
		, nameFieldVisible: true
		, descriptionFieldVisible: true
		, hasBuddy: false
//		, fieldAlias: defAlias
//		, funct: defAggregation
//		, fieldNature: defNature
		, fieldAlias: defFormState.alias
		, funct: defFormState.funct
		, fieldNature: defFormState.nature
		, type : defFormState.type
		, typeSecondary: defFormState.typeSecondary
		, decimals: defFormState.decimals
		, scale: defFormState.scale
		, backgroundColor: defFormState.backgroundColor
		, columnWidth: defFormState.columnWidth
		, fontSize: defFormState.fontSize
		, fontWeight: defFormState.fontWeight
		, fontColor: defFormState.fontColor
		, fontDecoration: defFormState.fontDecoration
	};

	Ext.apply(this, c);

	this.initFormPanel(c);

	// constructor
	Sbi.cockpit.widgets.table.AggregationChooserWindow.superclass.constructor.call(this, {
		layout: 'fit',
		width: this.width,
		height: this.height,
		closeAction:'destroy',
		plain: true,
		title: this.title,
		items: [this.formPanel]
    });

	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}

	this.addEvents('aggregationSave');

};

Ext.extend(Sbi.cockpit.widgets.table.AggregationChooserWindow, Ext.Window, {

    start: 0
    , limit: 20
	, attribute 	: null // the json object representing the attribute: it must be in the constructor input object
	, params : null // the json object with the parameters for store loading: it must be in the constructor input object
	
	, statics : {
		elementTypes : {
			TEXT: 'TEXT',
			NUMBER: 'NUMBER',
			CURRENCY: 'CURRENCY',
			PERCENTAGE: 'PERCENTAGE',
			DATE: 'DATE',
		}
		
		, scales : {
			NONE: '-',
			K: 'K',
			M: 'M',
			G: 'G'
		}
	}
	
	//private methods
	, initFormPanel: function(config) {

		var items = [];
		var LABEL_WIDTH = 150;
		var FIELD_WIDTH = 350;

		this.aliasTextField = Ext.create('Ext.form.Text', {
			 name: 			'fieldAlias',
			 fieldLabel: 	LN('sbi.qbe.selectgridpanel.headers.alias'),
			 allowBlank: 	true,
			 labelWidth:	LABEL_WIDTH,
			 width:			FIELD_WIDTH
		});
		 
		//if an alias is already defined, set it
    	if(config.fieldAlias != undefined && config.fieldAlias != null && config.fieldAlias != ""){
    		this.aliasTextField.setValue(config.fieldAlias);
    	}
			 
    	items.push(this.aliasTextField);
		
    	if(this.fieldNature == 'measure'){
			 
	    	var aggregationComboBoxData = [
                ['NONE',LN('sbi.qbe.selectgridpanel.aggfunc.name.none'), LN('sbi.qbe.selectgridpanel.aggfunc.name.none')],
	    		['SUM',LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.name.sum')],
	    		['AVG',LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.name.avg')],
	    		['MAX',LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.name.max')],
	    		['MIN',LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.name.min')],
	    		['COUNT',LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.name.count')],
	    		['COUNT DISTINCT',LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct')],
	    	];

	    	var aggregationComboBoxStore = Ext.create('Ext.data.SimpleStore', {
	    		fields: ['value', 'field', 'description'],
	    		data : aggregationComboBoxData
	    	});

	    	this.aggregationField = Ext.create('Ext.form.ComboBox', {
	    	   	tpl: '<tpl for="."><div ext:qtip="{field}: {description}" class="x-combo-list-item">{field}</div></tpl>',
	    	   	editable  : false,
	    	   	fieldLabel : LN('sbi.cockpit.aggregationwindow.selectAggregation'),
	    	   	forceSelection : true,
	    	   	mode : 'local',
	    	   	name : 'aggregation',
	    	   	store : aggregationComboBoxStore,
	    	   	displayField:'field',
	    	    valueField:'value',
	    	    emptyText:LN('sbi.cockpit.aggregationwindow.selectAggregation'),
	    	    typeAhead: true,
	    	    triggerAction: 'all',
	    	    selectOnFocus:true,
				labelWidth:	LABEL_WIDTH,
				width:		FIELD_WIDTH
	    	});
	    	
			if(config.funct == null ||config.funct == ''){
				this.aggregationField.select('NONE');
			} else if((config.funct == undefined) || (typeof config.funct == typeof NaN)){
				this.aggregationField.select('SUM');
			} else {
				this.aggregationField.select(config.funct);
			}
	    	
    		items.push(this.aggregationField);
    	}
    	
    	var elementTypes = Sbi.cockpit.widgets.table.AggregationChooserWindow.elementTypes;
    	var typeComboBoxData = [
            [ elementTypes.TEXT, LN('sbi.qbe.selectgridpanel.type.name.text'), LN('sbi.qbe.selectgridpanel.type.name.text')],
            [ elementTypes.NUMBER, LN('sbi.qbe.selectgridpanel.type.name.number'), LN('sbi.qbe.selectgridpanel.type.name.number')],
			[ elementTypes.CURRENCY, LN('sbi.qbe.selectgridpanel.type.name.currency'), LN('sbi.qbe.selectgridpanel.type.name.currency')],
			[ elementTypes.PERCENTAGE, LN('sbi.qbe.selectgridpanel.type.name.percentage'), LN('sbi.qbe.selectgridpanel.type.name.percentage')],
			[ elementTypes.DATE, LN('sbi.qbe.selectgridpanel.type.name.date'), LN('sbi.qbe.selectgridpanel.type.name.date')],
		];
    	
    	var typeComboBoxStore = new Ext.data.SimpleStore({
    		fields: ['value', 'field', 'description'],
    		data : typeComboBoxData
    	});
    	               	    	
    	var typeContainerItems = [];
    	
    	this.typeComboBox = Ext.create('Ext.form.ComboBox', {
    		tpl: '<tpl for="."><div ext:qtip="{field}: {description}" class="x-combo-list-item">{field}</div></tpl>',
    		name: 			'typeComboBox',
    		store: 			typeComboBoxStore,
    		displayField: 	'field',
    		valueField: 	'value',
    		editable : 		false,
			allowBlank: 	true,
			flex:			1,
		});
    	typeContainerItems.push(this.typeComboBox);
		
    	this.typeSecondaryField = null;
    	
    	this.decimalsField = Ext.create('Ext.form.field.Number', {
    		name: 			'decimalsField',
    		fieldLabel: 	LN('sbi.qbe.selectgridpanel.decimals.label'),
    		allowBlank: 	true,
    		minValue: 		0,
    		labelWidth:		LABEL_WIDTH,
    		width:			FIELD_WIDTH
    	});
    	
    	var scales = Sbi.cockpit.widgets.table.AggregationChooserWindow.scales;
    	var scaleComboBoxData = [
    	                         scales.NONE,
    	                         scales.K,
    	                         scales.M,
    	                         scales.G
    	                         ];
    	
    	var scaleComboBoxStore = new Ext.data.SimpleStore({
    		fields: ['value'],
    		data : scaleComboBoxData
    	});
    	
    	this.scaleField = Ext.create('Ext.form.ComboBox', {
    		name: 			'scaleField',
    		store: 			scaleComboBoxStore,
    		fieldLabel: 	LN('sbi.qbe.selectgridpanel.scale.label'),
    		displayField: 	'value',
    		valueField: 	'value',
    		value:			scales.NONE,
    		editable : 		false,
    		allowBlank: 	true,
    		labelWidth:		LABEL_WIDTH,
    		width:			FIELD_WIDTH
    	});
    	

	   	this.decimalsScaleContainer = Ext.create('Ext.form.FieldContainer', {
			labelWidth:		LABEL_WIDTH,
			width:			FIELD_WIDTH,
			layout: 		'vbox',
			items: 			[
			       			 	this.decimalsField,
			       			 	this.scaleField
			       			 ]
		});
	   	if(config.type != undefined && config.type != null && config.type != ""){
	   		this.typeComboBox.select(config.type);
	   		
	   		this.typeSecondaryField = this.createTypeSecondaryField(config.type);
	   		typeContainerItems.push(this.typeSecondaryField);
	   		
	   		if(this.typeSecondaryField != null) {
	   			if(config.typeSecondary != undefined 
	   					&& config.typeSecondary != null 
	   					&& config.typeSecondary != ""){
	   				this.typeSecondaryField.setValue(config.typeSecondary);
	   			}
	   		}
	   		
	   		if(config.type == elementTypes.NUMBER || config.type == elementTypes.CURRENCY) {
	   			this.decimalsScaleContainer.setVisible(true);
	   			
	   			if(config.decimals != undefined 
	   					&& config.decimals != null) {
	   				this.decimalsField.setValue(config.decimals);
	   			}
	   			
	   			if(config.scale != undefined 
	   					&& config.scale != null 
	   					&& config.scale != "") {
	   				this.scaleField.setValue(config.scale);
	   			}
	   		} else {
	   			this.decimalsScaleContainer.setVisible(false);
	   		}
	   	} else {
	   		this.typeComboBox.select(elementTypes.TEXT);
	   		this.decimalsScaleContainer.setVisible(false);
	   	}
    	
	   	this.typeContainer = Ext.create('Ext.form.FieldContainer', {
			fieldLabel: 	LN('sbi.qbe.selectgridpanel.type.label'),
			labelWidth:		LABEL_WIDTH,
			width:			FIELD_WIDTH,
			layout: 		'hbox',
			items: 			typeContainerItems
		});
	   	
	   	this.typeComboBox.on('change', function(comboBox) {
	   		var selectedValue = comboBox.getValue();
	   		
	   		if(this.typeSecondaryField != null) {
	   			this.typeSecondaryField.hide();
	   			this.typeSecondaryField.destroy();
	   		}
	   		
	   		this.typeSecondaryField = this.createTypeSecondaryField(selectedValue);
	   		
	   		if(this.typeSecondaryField != null) {
	   			this.typeContainer.add(this.typeSecondaryField);
	   		}
	   		
	   		if(selectedValue == elementTypes.NUMBER || selectedValue == elementTypes.CURRENCY) {
	   			this.decimalsScaleContainer.setVisible(true);
	   			
	   			this.decimalsField.setValue('');
	   			this.scaleField.setValue(scales.NONE);
	   		} else {
	   			this.decimalsScaleContainer.setVisible(false);
	   		}
	   		
	   	}, this);

	   	this.backgroundColorField = Ext.create('Ext.ux.FontColorField', { 
			name: 				'backgroundColorField',
			msgTarget: 			'qtip', 
			fallback: 			true,
			allowBlank: 		true,
			fieldLabel: 		LN('sbi.qbe.selectgridpanel.backgroundcolor.label'),
			labelWidth:			LABEL_WIDTH,
			width:				FIELD_WIDTH
		});
	   	
	   	if(config.backgroundColor != undefined && config.backgroundColor != null && config.backgroundColor != ""){
    		this.backgroundColorField.setValue(config.backgroundColor);
    	}

	   	this.columnWidthField = Ext.create('Ext.form.field.Number', {
			 name: 			'columnWidthField',
			 fieldLabel: 	LN('sbi.qbe.selectgridpanel.columnwidth.label'),
			 allowBlank: 	true,
			 minValue: 		0,
			 labelWidth:	LABEL_WIDTH,
			 width:			FIELD_WIDTH
		});
	   	
	   	if(config.columnWidth != undefined 
				&& config.columnWidth != null){
			this.columnWidthField.setValue(config.columnWidth);
		}
	   	
	   	var fontSizeStore =  Ext.create('Sbi.fonts.stores.FontSizeStore',{});
	   	this.fontSizeCombo = Ext.create('Ext.form.ComboBox',{
	   		name:			'fontSizeCombo',
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.widgetFontSize'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: false,
			maskRe: 		/\d/i,
			editable:       true,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontSizeStore, 
			valueField: 	'name',
			displayField: 	'description',
			labelWidth:		LABEL_WIDTH,
			width:			FIELD_WIDTH
		});

	   	if(config.fontSize != undefined 
				&& config.fontSize != null){
			this.fontSizeCombo.select(config.fontSize);
		}
	   	
	   	var fontWeightStore = Ext.create('Sbi.fonts.stores.FontWeightStore', {});
	   	this.fontWeightCombo = Ext.create('Ext.form.ComboBox',{
	   		name:			'fontWeight',
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontWeight'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			value:			'normal',
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontWeightStore, 
			valueField: 	'name',
			displayField: 	'description',
			labelWidth:		LABEL_WIDTH,
			width:			FIELD_WIDTH
   		});

	   	if(config.fontWeight != undefined 
				&& config.fontWeight != null
				&& config.fontWeight != ''){
			this.fontWeightCombo.select(config.fontWeight);
		} else {
			this.fontWeightCombo.select('normal');
		}
	   	
	   	this.fontColorField = Ext.create('Ext.ux.FontColorField', { 
	   		name: 			'fontColor',
	   		msgTarget: 		'qtip', 
	   		fallback: 		true,
	   		fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontColor'),
	   		afterLabelTextTpl : '<span class="help" data-qtip="'
	   				+ LN('sbi.cockpit.designer.fontConf.fontColor.info')
	   				+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
			allowBlank: 	true,
			labelWidth:		LABEL_WIDTH,
			width:			FIELD_WIDTH
	   	});
	   	
	   	if(config.fontColor != undefined 
	   			&& config.fontColor != null
	   			&& config.fontColor != ''){
	   		this.fontColorField.setValue(config.fontColor);
	   	}
	   	
	   	var fontDecorationStore = Ext.create('Sbi.fonts.stores.FontDecorationStore', {});
	   	this.fontDecorationCombo = Ext.create('Ext.form.ComboBox',{
	   		name:			'fontDecoration',
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.fontDecoration'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			fontDecorationStore, 
			value: 			'none',
			valueField: 	'name',
			displayField: 	'description',
			labelWidth:		LABEL_WIDTH,
			width:			FIELD_WIDTH
	   	});

	   	if(config.fontDecoration != undefined 
				&& config.fontDecoration != null
				&& config.fontDecoration != ''){
			this.fontDecorationCombo.select(config.fontDecoration);
		}
	   	
	   	items.push(this.typeContainer);
	   	items.push(this.decimalsScaleContainer);
	   	items.push(this.backgroundColorField);
	   	items.push(this.columnWidthField);
	   	items.push(this.fontSizeCombo);
	   	items.push(this.fontWeightCombo);
	   	items.push(this.fontColorField);
	   	items.push(this.fontDecorationCombo);

    	this.formPanel = Ext.create('Ext.form.FormPanel',{
    		frame:true,
    	    bodyStyle:'padding:5px 5px 0',
    	    buttonAlign : 'center',
    	    items: items,
    	    buttons: [{
    			text: LN('sbi.generic.actions.save'),
    		    handler: function(){
    	    		this.fireEvent('aggregationSave', this, this.getFormState());
    	    		//this.hide();
                	this.close();
                	this.destroy();
    		    }
            	, scope: this
    	    },{
    		    text: LN('sbi.qbe.messagewin.cancel'),
    		    handler: function(){
    		    	//this.hide();
                	this.close();
                	this.destroy();
            	}
            	, scope: this
    		}]
    	});
	}

	,createTypeSecondaryField : function(type) {
		var field = null;
		var elementTypes = Sbi.cockpit.widgets.table.AggregationChooserWindow.elementTypes;
		
		if(type == elementTypes.CURRENCY) {
			field = Ext.create('Ext.form.Text', {
				 name: 'typeSecondaryField',
				 allowBlank: 	true,
				 flex:			1,
			});
		} else if(type == elementTypes.DATE) {
			var dateFormats = [
                ['d/m/Y', 'dd/mm/YYYY'],
                ['m/d/Y', 'mm/dd/YYYY'],
                ['Y/m/d', 'YYYY/mm/dd']
    		];
			
			field = Ext.create('Ext.form.ComboBox',{
				tpl: '<tpl for="."><div ext:qtip="{field}" class="x-combo-list-item">{value}</div></tpl>',
				name: 'typeSecondaryField',
				allowBlank: 	true,
				editable : 		false,
				flex:			1,
				displayField: 	'field',
				valueField: 	'value',
				value:		'd/m/Y',
				store : new Ext.data.SimpleStore({
		    		fields: ['value', 'field'],
		    		data : dateFormats
		    	}),
			});
		}

		return field;
	}

	// public methods
	,getFormState : function() {
		var formState = {};
		formState.fieldAlias= this.aliasTextField.getValue();
		if(this.fieldNature == 'measure'){
			// non dovrebbe essere "formState.funct" ???
			formState.aggregation= this.aggregationField.getValue();
		}
		formState.type= this.typeComboBox.getValue();
		
		if (this.typeSecondaryField != undefined && this.typeSecondaryField != null) {
			formState.typeSecondary= this.typeSecondaryField.getValue();
		}
		
		if(this.decimalsScaleContainer.isVisible()) {
			formState.decimals = this.decimalsField.getValue();
			formState.scale = this.scaleField.getValue();
		} else {
			formState.decimals = null;
			formState.scale = null;
		}
		
		formState.backgroundColor = this.backgroundColorField.getValue();
		formState.columnWidth = this.columnWidthField.getValue();
		formState.fontSize = this.fontSizeCombo.getValue();
		formState.fontWeight = this.fontWeightCombo.getValue();
		formState.fontColor = this.fontColorField.getValue();
		formState.fontDecoration = this.fontDecorationCombo.getValue();
		
		return formState;
	}
});