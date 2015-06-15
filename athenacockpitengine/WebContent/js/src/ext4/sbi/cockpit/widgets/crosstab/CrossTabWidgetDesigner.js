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

Ext.ns("Sbi.cockpit.widgets.crosstab");

Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'crosstablWidgetDesigner'
	};

	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.crosstab && Sbi.settings.cockpit.widgets.crosstab.crosstabWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.crosstab.crosstabWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c); // this operation should overwrite this.crosstabTemplate content, that is the definition of the crosstab

	this.init();

	c = {
		//items: [this.crosstabDefinitionPanel]
			items: [Ext.create('Ext.tab.Panel', {
			    width: 400,
			    height: 400,
			    tabPosition: 'right',
			    items: [ this.crosstabDefinitionPanel, this.fontConfigurationPanel]
			})]
		,title: LN('sbi.crosstab.crosstabdefinitionpanel.title')
		,border: false
	};

	Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.constructor.call(this, c);

	this.columnsContainerPanel.on(	'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
	this.rowsContainerPanel.on(		'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);

	this.columnsContainerPanel.on(
		'beforerender' ,
		function (thePanel, attribute) {
			
			var state = {};
			
			if(Sbi.isValorized(config)) {
				
				state.rows = config.rows;
				state.columns = config.columns;
				state.measures = config.measures;

			}
			
			this.setFontStateBeforeRender(this, state);
			
			this.setDesignerState(state);
		},
		this
	);
};

Ext.extend(Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {

	
	//field to select widget font type
	fontTypeCombo: null
	//field to select widget font size
	, fontSizeCombo: null

	, tdLevelFontSizeCombo: null

	, tdLevelFontColorText: null

	, tdLevelFontWeightCombo: null

	, tdLevelFontDecorationCombo: null

	, tdMemberFontSizeCombo: null

	, tdMemberFontColorText: null

	, tdMemberFontWeightCombo: null

	, tdMemberFontDecorationCombo: null
	
	, tdDataFontSizeCombo: null

	, tdDataFontColorText: null

	, tdDataFontWeightCombo: null

	, tdDataFontDecorationCombo: null
	
	//panel to show font size options
	, fontConfigurationPanel: null,
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
    // -----------------------------------------------------------------------------------------------------------------

	crosstabTemplate: {}
	, isStatic: false
	, crosstabDefinitionPanel: null
	, columnsContainerPanel: null
	, rowsContainerPanel: null
	, measuresContainerPanel: null
	, ddGroup: null // must be provided with the constructor input object

	, getCrosstabDefinition: function() {
		var crosstabDef = {};
		crosstabDef.rows = this.rowsContainerPanel.getContainedAttributes();
		crosstabDef.columns = this.columnsContainerPanel.getContainedAttributes();
		crosstabDef.measures = this.measuresContainerPanel.getContainedMeasures();
		crosstabDef.config = this.measuresContainerPanel.getCrosstabConfig();
		crosstabDef.config.type = 'pivot';
		return crosstabDef;
	}

	, getDesignerState: function() {
		Sbi.trace("[CrossTabWidgetDesigner.getDesignerState]: IN");
		Sbi.trace("[CrossTabWidgetDesigner.getDesignerState]: " + Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.getDesignerState);

		var state = Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.getDesignerState(this);

		state.rows = this.rowsContainerPanel.getContainedAttributes();
		state.columns = this.columnsContainerPanel.getContainedAttributes();
		state.measures = this.measuresContainerPanel.getContainedMeasures();
		state.config = this.measuresContainerPanel.getCrosstabConfig();
		state.config.type = 'pivot';
		
		this.getFontState(state);

		Sbi.trace("[CrossTabWidgetDesigner.getDesignerState]: OUT");

		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[CrossTabWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.crosstab.CrossTabWidgetDesigner.superclass.setDesignerState(this, state);

		if (state !== undefined && state !== null) {
			if (state.rows) this.rowsContainerPanel.setAttributes(state.rows);
			if (state.columns) this.columnsContainerPanel.setAttributes(state.columns);
			if (state.measures) this.measuresContainerPanel.setMeasures(state.measures);
			this.setFontState(state);
		}


		Sbi.trace("[CrossTabWidgetDesigner.setDesignerState]: OUT");
	}

	/* check cross tab validity: rules are
	 * - at least one measure
	 * - at least one attribute in rows or in columns
	 * - if there is segment attribute it must be used
	 * - if ther is mandatory measure it must be used
	 */
	, validate: function(validFields){
		var valErr='';

		valErr+= ''+this.columnsContainerPanel.validate(validFields);
		valErr+= ''+this.rowsContainerPanel.validate(validFields);
		valErr+= ''+this.measuresContainerPanel.validate(validFields);

		if(valErr != ''){
			valErr = valErr.substring(0, valErr.length - 1)
			return LN("sbi.worksheet.designer.validation.invalidFields")+valErr;
		}

		var crossTabDef = this.getCrosstabDefinition();

		// at least one measure
		if(crossTabDef.measures.length<1){
			return LN('sbi.crosstab.crossTabValidation.noMeasure');

		}

		// at least one row or one column
		else if(crossTabDef.columns.length<1 && crossTabDef.rows.length<1){
			return LN('sbi.crosstab.crossTabValidation.noAttribute');
		}

		// if there is mandatoryField it must have been inserted
		if(this.measuresContainerPanel.hasMandatoryMeasure === true){
			var isMandatory = this.isThereMandatoryMeasure(crossTabDef);
			if(isMandatory === false){
				return LN('sbi.crosstab.crossTabValidation.noMandatoryMeasure');
			}

		}

		// if there is segmentAttribute it must have been inserted in columns or rows
		if(this.rowsContainerPanel.hasSegmentAttribute === true || this.columnsContainerPanel.hasSegmentAttribute === true){
			var isSegment = this.isThereSegmentAttribute(crossTabDef);
			if(isSegment === false){
				return LN('sbi.crosstab.crossTabValidation.noSegmentAttribute');
			}
		}
		return null;
	}

	, isThereSegmentAttribute: function(crossTabDef){
		var isThereSegment = false;
		for (var i = 0; i < crossTabDef.rows.length && isThereSegment === false; i++) {
			var row = crossTabDef.rows[i];
			if(row.nature === 'segment_attribute'){
				isThereSegment = true;
			}
		}

		for (var i = 0; i < crossTabDef.columns.length && isThereSegment === false; i++) {
			var row = crossTabDef.columns[i];
			if(row.nature === 'segment_attribute'){
				isThereSegment = true;
			}
		}
		return isThereSegment;
	}

	, isThereMandatoryMeasure: function(crossTabDef){
		var isThereMandatory = false;
		for (var i = 0; i < crossTabDef.measures.length && isThereMandatory === false; i++) {
			var measure = crossTabDef.measures[i];
			if(measure.nature === 'mandatory_measure'){
				isThereMandatory = true;
			}
		}
		return isThereMandatory;
	}

	, checkIfAttributeIsAlreadyPresent: function(aPanel, attribute) {
		var attributeId = attribute.id;
		var alreadyPresent = this.containsAttribute(attributeId);
		if (alreadyPresent) {
			Ext.Msg.show({
				   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
				   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.attributealreadypresent'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
			return false;
		}
		return true;
	}

	, containsAttribute: function (attributeId) {
		var storeRows = this.rowsContainerPanel.store;
		var storeColumns = this.columnsContainerPanel.store;
		if (storeRows.findExact('id', attributeId) !== -1) {
			return true;
		}
		if (storeColumns.findExact('id', attributeId) !== -1) {
			return true;
		}
		return false;
	}


	// -----------------------------------------------------------------------------------------------------------------
    // init methods
    // -----------------------------------------------------------------------------------------------------------------
	, init: function() {

		this.columnsContainerPanel = new Sbi.cockpit.widgets.crosstab.AttributesContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.columns')
			, width: 400
			, initialData: this.crosstabTemplate.columns
			, ddGroup: this.ddGroup
		});
		// propagate events
		this.columnsContainerPanel.on(
			'attributeDblClick' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeDblClick", this, attribute);
			},
			this
		);
		this.columnsContainerPanel.on(
			'attributeRemoved' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeRemoved", this, attribute);
			},
			this
		);

		this.rowsContainerPanel = new Sbi.cockpit.widgets.crosstab.AttributesContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.rows')
			, width: 200
			, initialData: this.crosstabTemplate.rows
			, ddGroup: this.ddGroup
		});
		// propagate events
		this.rowsContainerPanel.on(
			'attributeDblClick' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeDblClick", this, attribute);
			},
			this
		);
		this.rowsContainerPanel.on(
			'attributeRemoved' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeRemoved", this, attribute);
			},
			this
		);

		this.measuresContainerPanel = new Sbi.cockpit.widgets.crosstab.MeasuresContainerPanel({
			title: LN('sbi.crosstab.crosstabdefinitionpanel.measures')
			, width: 400
			, initialData: this.crosstabTemplate.measures
			, crosstabConfig: this.crosstabTemplate.config
			, ddGroup: this.ddGroup
			, isStatic: this.isStatic
		});

		this.crosstabDefinitionPanel = new Ext.Panel({
	        title: 'Crosstab Designer',
			baseCls:'x-plain'
			, padding: '10 10 10 30'
			, layout: {
				type: 'table',
				columns:2
			}
			// applied to child components
			, defaults: {height: 150}
			, items:[
		      {
		    	  border: false
		      }
		         , this.columnsContainerPanel
		         , this.rowsContainerPanel
		         , this.measuresContainerPanel
		      ]
		});
		
//		this.tabPanel = Ext.create('Ext.tab.Panel', {
//	    	tabPosition: 'right'
//	    	, border: false
//	    	, margin: 0
//	    	, padding: 0
//	    	//, bodyStyle: 'width: 100%; height: 100%'
//	    	//, items:[this.crosstabDefinitionPanel]
//	    	//, html: "tableDesigner"
//	    });
		
		var fontSizeStore =  Ext.create('Sbi.fonts.stores.FontSizeStore',{});
		
		var fontFamilyStore = Ext.create('Sbi.fonts.stores.FontFamilyStore', {});
		
		var fontDecorationStore = Ext.create('Sbi.fonts.stores.FontDecorationStore', {});
		
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
		
		var tableGeneralFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.crosstabFontGeneralOpts')
	        , margin: 			10
	    	, items: 			[this.fontTypeCombo, this.fontSizeCombo]	
			, width:			600
		};
		
		
		/* crosstab font headers options */		
		 
		 this.tdLevelFontSizeCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdLevelFontSize',
				labelWidth:		130,
				width:			180
			});
		 
		 this.tdLevelFontColorText = Ext.create('Ext.form.field.Text',{
			 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
			 name: 				'tdLevelFontColor',
	         allowBlank: 		true,
	         regex: 			hexColorReg,
	         regextText: 		'Not a valid HEX color',
	    	 enforceMaxLength: 	true,
	 		 maxLength: 		7,
	 		 msgTarget: 		'side',
			 labelWidth:		140,
			 width:				250,
			 afterLabelTextTpl : '<span class="help" data-qtip="'
				+ LN('sbi.cockpit.designer.fontConf.fontColorTip')
	         	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
		 });
		 
		 this.tdLevelFontWeightCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdLevelFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 this.tdLevelFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdLevelFontDecoration',
				labelWidth:		140,
				width:			255

			});
		 
		 var tdLevelFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.crosstabHeadersFontOpts')
	    	, margin: 			10
	    	, items: 			[this.tdLevelFontSizeCombo, this.tdLevelFontColorText, this.tdLevelFontWeightCombo, this.tdLevelFontDecorationCombo]	
			, width:			600
		};
		
		 
		 /* measures font options */
		 
		 this.tdMemberFontSizeCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdMemberFontSize',
				labelWidth:		130,
				width:			180,
			});
		 
		
		 this.tdMemberFontColorText = Ext.create('Ext.form.field.Text',{
				 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
				 name: 				'tdMemberFontColor',
		         allowBlank: 		true,
		         regex: 			hexColorReg,
		         regextText: 		'Not a valid HEX color',
		    	 enforceMaxLength: 	true,
		 		 maxLength: 		7,
		 		 msgTarget: 		'side',
	 			labelWidth:			140,
				width:				250,
				afterLabelTextTpl : '<span class="help" data-qtip="'
					+ LN('sbi.cockpit.designer.fontConf.fontColorTip')
	            	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
		 });
		 
		 this.tdMemberFontWeightCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdMemberFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 this.tdMemberFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdMemberFontDecoration',
				labelWidth:		140,
				width:			255

			});
		
		 
		var tdMemberFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.measuresHeadersFontOpts')
	    	, margin: 			10
	    	, items: 			[this.tdMemberFontSizeCombo, this.tdMemberFontColorText, this.tdMemberFontWeightCombo, this.tdMemberFontDecorationCombo]	
			, width:			600
		};
		
		 
		/* data font options */		
		 
		 this.tdDataFontSizeCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdDataFontSize',
				labelWidth:		130,
				width:			180
			});
		 
		 this.tdDataFontColorText = Ext.create('Ext.form.field.Text',{
			 fieldLabel: 		LN('sbi.cockpit.designer.fontConf.fontColor'),
			 name: 				'tdDataFontColor',
	         allowBlank: 		true,
	         regex: 			hexColorReg,
	         regextText: 		'Not a valid HEX color',
	    	 enforceMaxLength: 	true,
	 		 maxLength: 		7,
	 		 msgTarget: 		'side',
			 labelWidth:		140,
			 width:				250,
			 afterLabelTextTpl : '<span class="help" data-qtip="'
				+ LN('sbi.cockpit.designer.fontConf.fontColorTip')
	         	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
		 });
		 
		 this.tdDataFontWeightCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdDataFontWeight',
				labelWidth:		130,
				width:			245

			});
		 
		 this.tdDataFontDecorationCombo = Ext.create('Ext.form.ComboBox',{
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
				name:			'tdDataFontDecoration',
				labelWidth:		140,
				width:			255

			});
		 
		var tdDataFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.measuresFontOpts')
	    	, margin: 			10
	    	, items: 			[this.tdDataFontSizeCombo, this.tdDataFontColorText, this.tdDataFontWeightCombo, this.tdDataFontDecorationCombo]	
			, width:			600
		}; 
		
		
		
		this.fontConfigurationPanel = new Ext.Panel({
			title: 			LN('sbi.cockpit.designer.fontConf.fontOptions')
			//baseCls:'x-plain'
			, layout: {
				type: 'table',
				columns:1
			}
			// applied to child components
			//, defaults: {height: 150}
			, items: [tableGeneralFontOptions, tdLevelFontOptions, tdMemberFontOptions, tdDataFontOptions]	
		});
		
		

	}

	, onAfterLayout: function() {
		Sbi.trace("[CrossTabWidgetDesigner.onAfterLayout][" + this.getId() + "]: IN");
		var selections = this.getWidgetManager().getWidgetSelections(this.getId());

		// TODO: reselect rows in a selective way
		this.fireSelectionEvent = true;
		Sbi.trace("[CrossTabWidgetDesigner.onAfterLayout][" + this.getId() + "]: OUT");
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, setFontStateBeforeRender: function(thePanel, state){
		Sbi.trace("[CrosstabWidgetDesigner.setFontStateBeforeRender]: IN");
		
		var crosstabFonts = this.findCrosstabFont()
		
		if(crosstabFonts !== undefined && crosstabFonts !== null){
			
			if(thePanel.fontType === undefined || thePanel.fontType === null){
				state.fontType = crosstabFonts.fontType;
			}else{
				state.fontType = thePanel.fontType;
			}
			
			if(thePanel.fontSize === undefined || thePanel.fontSize === null){
				state.fontSize = crosstabFonts.fontSize;
			}else{
				state.fontSize = thePanel.fontSize;
			}
			
			// td level
			if(thePanel.tdLevelFontSize === undefined || thePanel.tdLevelFontSize === null){
				state.tdLevelFontSize = crosstabFonts.tdLevelFontSize;
			}else{
				state.tdLevelFontSize = thePanel.tdLevelFontSize;
			}
			
			if(thePanel.tdLevelFontColor === undefined || thePanel.tdLevelFontColor === null){
				state.tdLevelFontColor = crosstabFonts.tdLevelFontColor;
			}else{
				state.tdLevelFontColor = thePanel.tdLevelFontColor;
			}
			
			if(thePanel.tdLevelFontWeight === undefined || thePanel.tdLevelFontWeight === null){
				state.tdLevelFontWeight = crosstabFonts.tdLevelFontWeight;
			}else{
				state.tdLevelFontWeight = thePanel.tdLevelFontWeight;
			}
			
			if(thePanel.tdLevelFontDecoration === undefined || thePanel.tdLevelFontDecoration === null){
				state.tdLevelFontDecoration = crosstabFonts.tdLevelFontDecoration;
			}else{
				state.tdLevelFontDecoration = thePanel.tdLevelFontDecoration;
			}
			
			//td member
			if(thePanel.tdMemberFontSize === undefined || thePanel.tdMemberFontSize === null){
				state.tdMemberFontSize = crosstabFonts.tdMemberFontSize;
			}else{
				state.tdMemberFontSize = thePanel.tdMemberFontSize;
			}
			
			if(thePanel.tdMemberFontColor === undefined || thePanel.tdMemberFontColor === null){
				state.tdMemberFontColor = crosstabFonts.tdMemberFontColor;
			}else{
				state.tdMemberFontColor = thePanel.tdMemberFontColor;
			}
			
			if(thePanel.tdMemberFontWeight === undefined || thePanel.tdMemberFontWeight === null){
				state.tdMemberFontWeight = crosstabFonts.tdMemberFontWeight;
			}else{
				state.tdMemberFontWeight = thePanel.tdMemberFontWeight;
			}
			
			if(thePanel.tdMemberFontDecoration === undefined || thePanel.tdMemberFontDecoration === null){
				state.tdMemberFontDecoration = crosstabFonts.tdMemberFontDecoration;
			}else{
				state.tdMemberFontDecoration = thePanel.tdMemberFontDecoration;
			}
			
			// td data
			if(thePanel.tdDataFontSize === undefined || thePanel.tdDataFontSize === null){
				state.tdDataFontSize = crosstabFonts.tdDataFontSize;
			}else{
				state.tdDataFontSize = thePanel.tdDataFontSize;
			}
			
			if(thePanel.tdDataFontColor === undefined || thePanel.tdDataFontColor === null){
				state.tdDataFontColor = crosstabFonts.tdDataFontColor;
			}else{
				state.tdDataFontColor = thePanel.tdDataFontColor;
			}
			
			if(thePanel.tdDataFontWeight === undefined || thePanel.tdDataFontWeight === null){
				state.tdDataFontWeight = crosstabFonts.tdDataFontWeight;
			}else{
				state.tdDataFontWeight = thePanel.tdDataFontWeight;
			}
			
			if(thePanel.tdDataFontDecoration === undefined || thePanel.tdDataFontDecoration === null){
				state.tdDataFontDecoration = crosstabFonts.tdDataFontDecoration;
			}else{
				state.tdDataFontDecoration = thePanel.tdDataFontDecoration;
			}
			
		}else{
			
			state.fontType = thePanel.fontType;
			state.fontSize = thePanel.fontSize;
			
			state.tdLevelFontSize = thePanel.tdLevelFontSize,
			state.tdLevelFontColor = thePanel.tdLevelFontColor,
			state.tdLevelFontWeight = thePanel.tdLevelFontWeight,
			state.tdLevelFontDecoration = thePanel.tdLevelFontDecoration,
			state.tdMemberFontSize = thePanel.tdMemberFontSize,
			state.tdMemberFontColor = thePanel.tdMemberFontColor,
			state.tdMemberFontWeight = thePanel.tdMemberFontWeight,
			state.tdMemberFontDecoration = thePanel.tdMemberFontDecoration,
			state.tdDataFontSize = thePanel.tdDataFontSize,
			state.tdDataFontColor = thePanel.tdDataFontColor,
			state.tdDataFontWeight = thePanel.tdDataFontWeight,
			state.tdDataFontDecoration = thePanel.tdDataFontDecoration
			
		}
		
		Sbi.trace("[CrosstabWidgetDesigner.setFontStateBeforeRender]: OUT");		
	}
	
	, setFontState: function(state){
		Sbi.trace("[CrosstabWidgetDesigner.setFontState]: IN");
		
		if (state.fontType) this.fontTypeCombo.setValue(state.fontType);
		if (state.fontSize) this.fontSizeCombo.setValue(state.fontSize);
		//crosstab headers font
		if (state.tdLevelFontSize) this.tdLevelFontSizeCombo.setValue(state.tdLevelFontSize);
		if (state.tdLevelFontColor) this.tdLevelFontColorText.setValue(state.tdLevelFontColor);
		if (state.tdLevelFontWeight) this.tdLevelFontWeightCombo.setValue(state.tdLevelFontWeight);
		if (state.tdLevelFontDecoration) this.tdLevelFontDecorationCombo.setValue(state.tdLevelFontDecoration);
		//measures headers font		
		if (state.tdMemberFontSize) this.tdMemberFontSizeCombo.setValue(state.tdMemberFontSize);
		if (state.tdMemberFontColor) this.tdMemberFontColorText.setValue(state.tdMemberFontColor);
		if (state.tdMemberFontWeight) this.tdMemberFontWeightCombo.setValue(state.tdMemberFontWeight);
		if (state.tdMemberFontDecoration) this.tdMemberFontDecorationCombo.setValue(state.tdMemberFontDecoration);
		//data font		
		if (state.tdDataFontSize) this.tdDataFontSizeCombo.setValue(state.tdDataFontSize);
		if (state.tdDataFontColor) this.tdDataFontColorText.setValue(state.tdDataFontColor);
		if (state.tdDataFontWeight) this.tdDataFontWeightCombo.setValue(state.tdDataFontWeight);
		if (state.tdDataFontDecoration) this.tdDataFontDecorationCombo.setValue(state.tdDataFontDecoration);		
		
		Sbi.trace("[CrosstabWidgetDesigner.setFontState]: OUT");		
	}
	
	, findCrosstabFont: function(){
		Sbi.trace("[CrosstabWidgetDesigner.findCrosstabFont]: IN");
		
		var crosstabFonts = Sbi.storeManager.getFont("crosstabFonts");
//		var fonts = Sbi.storeManager.getFonts();
//		
//		var tabIndex = -1;
//		
//		for(var i = 0; i < fonts.length; i++) {
//			if(Sbi.isValorized(fonts[i]) && fonts[i].tabId === "crosstabFonts") {
//				tabIndex = i;
//				break;
//			}
//		}
//		
//		if(tabIndex >= 0){
//			crosstabFonts = fonts[tabIndex]
//		}
		
		return crosstabFonts		
		
		Sbi.trace("[CrosstabtWidgetDesigner.findCrosstabFont]: OUT");		
	}
	
	, getFontState: function(state){
		Sbi.trace("[CrosstabWidgetDesigner.getFontState]: IN");
		
		//blank values are permitted, so we need to check the objects before call .getValue()
		if(this.fontTypeCombo !== null)
		{	
			state.fontType = this.fontTypeCombo.getValue();
		}
		
		if(this.fontSizeCombo !== null)
		{	
			state.fontSize = this.fontSizeCombo.getValue();
		}
		
		//crosstab headers font
		if(this.tdLevelFontSizeCombo !== null)
		{	
			state.tdLevelFontSize = this.tdLevelFontSizeCombo.getValue();
		}
		if(this.tdLevelFontColorText !== null)
		{	
			state.tdLevelFontColor = this.tdLevelFontColorText.getValue();
		}		
		if(this.tdLevelFontWeightCombo !== null)
		{	
			state.tdLevelFontWeight = this.tdLevelFontWeightCombo.getValue();
		}
		if(this.tdLevelFontDecorationCombo !== null)
		{	
			state.tdLevelFontDecoration = this.tdLevelFontDecorationCombo.getValue();
		}
		
		//measures headers font
		if(this.tdMemberFontSizeCombo !== null)
		{
			state.tdMemberFontSize = this.tdMemberFontSizeCombo.getValue();
		}
		if(this.tdMemberFontColorText !== null)
		{	
			state.tdMemberFontColor = this.tdMemberFontColorText.getValue();
		}	
		if(this.tdMemberFontWeightCombo !== null)
		{	
			state.tdMemberFontWeight = this.tdMemberFontWeightCombo.getValue();
		}
		if(this.tdMemberFontDecorationCombo !== null)
		{	
			state.tdMemberFontDecoration = this.tdMemberFontDecorationCombo.getValue();
		}
		
		//data font
		if(this.tdDataFontSizeCombo !== null)
		{
			state.tdDataFontSize = this.tdDataFontSizeCombo.getValue();
		}
		if(this.tdDataFontColorText !== null)
		{	
			state.tdDataFontColor = this.tdDataFontColorText.getValue();
		}	
		if(this.tdDataFontWeightCombo !== null)
		{	
			state.tdDataFontWeight = this.tdDataFontWeightCombo.getValue();
		}
		if(this.tdDataFontDecorationCombo !== null)
		{	
			state.tdDataFontDecoration = this.tdDataFontDecorationCombo.getValue();
		}
		
		
		Sbi.trace("[CrosstabWidgetDesigner.getFontState]: OUT");		
	}
	
});
