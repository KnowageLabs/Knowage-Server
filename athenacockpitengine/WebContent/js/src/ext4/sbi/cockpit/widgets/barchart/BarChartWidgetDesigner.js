/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.barchart");

Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner = function(config) {

		var defaultSettings = {
			name: 'barChartWidgetDesigner'
			,title: LN('sbi.cockpit.widgets.barchart.barChartWidgetDesigner.title')
			, border: false
			, showSeriesGroupingPanel: false
		};

		if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.barchart && Sbi.settings.cockpit.widgets.barchart.barChartWidgetDesigner) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.barchart.barChartWidgetDesigner);
		}
		var c = Ext.apply(defaultSettings, config || {});
		Ext.apply(this, c);

		this.chartLib = 'ext3';
		if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.chartlib) {
			this.chartLib = Sbi.settings.cockpit.chartlib;
		}
		this.chartLib = this.chartLib.toLowerCase();

		this.addEvents("attributeDblClick", "attributeRemoved");

		this.init();

		c = {
				//items: [this.form]
				items: [Ext.create('Ext.tab.Panel', 
						{
			    	width: 400,
			    	height: 400,
			    	tabPosition: 'right',
			    	items: [ this.form, this.fontConfigurationPanel]
				})]
			};

		Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner.superclass.constructor.call(this, c);

		if(Ext.isIE){
			this.on('resize', function(a,b,c,d){try{ this.form.setWidth(b-50);}catch(r){}}, this);
		}

		this.on(
				'beforerender' ,
				function (thePanel, attribute) {
					var state = {};
					state.type = thePanel.type;
					state.orientation = thePanel.orientation;
					state.showvalues = thePanel.showvalues;
					state.showlegend = thePanel.showlegend;
					state.legendPosition = thePanel.legendPosition;
					state.category = thePanel.category;
					state.groupingVariable = thePanel.groupingVariable;
					state.series = thePanel.series;
					state.categoryAxis = thePanel.categoryAxis;
					state.seriesAxis = thePanel.seriesAxis;
					state.sortOrder = thePanel.sortOrder;

					this.setFontStateBeforeRender(thePanel, state);

					state.showSeriesName = thePanel.showSeriesName;
					state.showCategoryName = thePanel.showCategoryName;
					state.wtype = 'barchart';
					this.setDesignerState(state);
				},
				this
			);
		this.on('afterLayout', this.addToolTips, this);

		this.categoryContainerPanel.on(	'beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);

		if(this.showSeriesGroupingPanel === true) {
			this.seriesGroupingPanel.on('beforeAddAttribute', this.checkIfAttributeIsAlreadyPresent, this);
		}
};

Ext.extend(Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	form: null
	, items: null
	, typeRadioGroup: null
	, orientationCombo: null
	, showValuesCheck: null
	, imageTemplate: null
	, categoryContainerPanel: null
	, seriesGroupingPanel: null
	, seriesContainerPanel: null
	, axisDefinitionPanel: null
	, showLegendCheck: null
	, radioGroupIds: null
	, chartLib: null
	, categoryAxis: null
	, seriesAxis: null
	, sortOrder: null
	, legendPositionCombo: null
	
	//field to select widget font type
	, fontTypeCombo: null
	//field to select widget font size
	, fontSizeCombo: null
	//field to select chart legend font size
	, legendFontSizeCombo: null
	//field to select chart axes titles font size 
	, axisTitleFontSizeCombo: null
	//field to select chart tooltip font size
	, tooltipLabelFontSizeCombo: null
	//field to select chart axes labels font size 
	, axisLabelsFontSizeCombo: null
	//panel to show font size options
	, fontConfigurationPanel: null
	
	, showSeriesNameCheck: null
    , showCategoryNameCheck: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================



	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function () {
		this.initTemplate();

		this.radioGroupIds = [Ext.id(), Ext.id(), Ext.id()]; // generate random id

		this.typeRadioGroup = new Ext.form.RadioGroup({
			hideLabel: 	true,
			columns: 	3,
			width:		500,
			margin: 	'0 0 0 0',
			items: [
		        {name: 'type', height: 80, width: 80, id: this.radioGroupIds[0], ctCls:'side-by-side-barchart-vertical', inputValue: 'side-by-side-barchart', checked: true},
		        {name: 'type', height: 80, width: 80, id: this.radioGroupIds[1], ctCls:'stacked-barchart-vertical', inputValue: 'stacked-barchart'},
		        {name: 'type', height: 80, width: 80, id: this.radioGroupIds[2], ctCls:'percent-stacked-barchart-vertical', inputValue: 'percent-stacked-barchart'}
			]
		});
		this.typeRadioGroup.on('change', this.changeBarChartImage, this);

		this.orientationComboStore = new Ext.data.ArrayStore({
			fields : ['name', 'description']
			, data : [['vertical', LN('sbi.worksheet.designer.barchartdesignerpanel.form.orientation.vertical')]
					, ['horizontal', LN('sbi.worksheet.designer.barchartdesignerpanel.form.orientation.horizontal')]]
		});
		this.orientationCombo = new Ext.form.ComboBox({
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	false,
			fieldLabel:     LN('sbi.worksheet.designer.barchartdesignerpanel.form.orientation.title'),
			name:           'orientation',
			displayField:   'description',
			valueField:     'name',
			value:			'vertical',
			labelWidth:		110,
			store:          this.orientationComboStore,
			width:			235
		});
		this.orientationCombo.on('change', this.changeBarChartImage, this);


		this.showValuesCheck = new Ext.form.Checkbox({
			name: 'showvalues'
			, labelWidth: 120
			, checked: false
			, fieldLabel: LN('sbi.worksheet.designer.barchartdesignerpanel.form.showvalues.title')
		});

		this.showLegendCheck = new Ext.form.Checkbox({
			name: 'showlegend'
			, labelWidth: 135
			, checked: false
			, fieldLabel: LN('sbi.worksheet.designer.barchartdesignerpanel.form.showlegend.title')
		});

		this.legendPositionStore = new Ext.data.ArrayStore({
			fields : ['name', 'description']
			, data : [['bottom', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom')]
					, ['top', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top')]
					, ['left', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.left')]
					, ['right', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.right')]]
		});
		this.legendPositionCombo = new Ext.form.ComboBox({
			width:			235,
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	false,
			fieldLabel:      LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.title'),
			name:           'position',
			displayField:   'description',
			valueField:     'name',
			value:			'bottom',
			labelWidth:		110,
			store:          this.legendPositionStore
		});

		this.categoryAxisText = new Ext.form.Text({
			 name: 'categoryAxis',
			 fieldLabel: 	LN('sbi.worksheet.designer.barchartdesignerpanel.form.categoryaxis.title'),
			 allowBlank: 	true,
			 labelWidth:	120,
			 width:			235,
		});

		this.seriesAxisText = new Ext.form.Text({
			 name: 'seriesAxis',
			 fieldLabel: 	LN('sbi.worksheet.designer.barchartdesignerpanel.form.seriesaxis.title'),
			 allowBlank: 	true,
			 labelWidth:	120,
			 width:			235
		});

		this.sortOrderComboStore = new Ext.data.ArrayStore({
			fields : ['name', 'description']
			, data : [['ASC', LN('sbi.worksheet.designer.barchartdesignerpanel.form.sortorder.ascending')]
					, ['DESC', LN('sbi.worksheet.designer.barchartdesignerpanel.form.sortorder.descending')]]
		});
		this.sortOrderCombo = new Ext.form.ComboBox({
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	false,
			fieldLabel:     LN('sbi.worksheet.designer.barchartdesignerpanel.form.sortorder.title'),
			name:           'sortOrder',
			displayField:   'description',
			valueField:     'name',
			value:			'ASC',
			store:          this.sortOrderComboStore,
			labelWidth:		110,
			width:			235
		});

		this.categoryContainerPanel = new Sbi.cockpit.widgets.chart.ChartCategoryPanel({
            width: 200
            , height: 70
            , initialData: null
            , ddGroup: this.ddGroup
		});
		// propagate events
		this.categoryContainerPanel.on(
			'attributeDblClick' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeDblClick", this, attribute);
			},
			this
		);
		this.categoryContainerPanel.on(
			'attributeRemoved' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeRemoved", this, attribute);
			},
			this
		);

		if(this.showSeriesGroupingPanel === true) {
			this.seriesGroupingPanel = new Sbi.cockpit.widgets.chart.SeriesGroupingPanel({
	            width: 430
	            , height: 70
	            , initialData: null
	            , ddGroup: this.ddGroup
			});
			// propagate events
			this.seriesGroupingPanel.on(
				'attributeDblClick' ,
				function (thePanel, attribute) {
					this.fireEvent("attributeDblClick", this, attribute);
				},
				this
			);
			this.seriesGroupingPanel.on(
				'attributeRemoved' ,
				function (thePanel, attribute) {
					this.fireEvent("attributeRemoved", this, attribute);
				},
				this
			);
		} else {
			this.seriesGroupingPanel = new Ext.Panel({border:false, frame: false});
		}


		this.seriesContainerPanel = new Sbi.cockpit.widgets.chart.ChartSeriesPanel({
            width: 460
            , height: 100
            , initialData: []
            , crosstabConfig: {}
            , ddGroup: this.ddGroup
            , parent: 'barchart'
		});


		this.imageContainerPanel = new Ext.Panel({
            width: 200
            , height: 100
            , html: this.imageTemplate.apply(['side-by-side-barchart', 'vertical'])
		});

	    this.axisDefinitionPanel = new Ext.Panel({
	        baseCls:'x-plain'
//		    , cls: 'centered-panel' //for center the panel
	        , cls: 'x-axis-definition-table'	
			, width: '100%'
	        //, padding: '0 10 10 10'
	        , layout: {type: 'table', columns : 2}
	    	//, items: axisDefinitionPanelItems
	        , items:[
	              this.seriesContainerPanel
	            , this.imageContainerPanel
	            , this.seriesGroupingPanel
		        , this.categoryContainerPanel

		    ]
	    });
	    
	    this.showSeriesNameCheck = new Ext.form.Checkbox({
			name: 'showSeriesName'
			, labelWidth: 135
			, checked: false
			, fieldLabel: LN('sbi.worksheet.designer.barchartdesignerpanel.form.showSeriesName.title')
		});
	    
	    this.showCategoryNameCheck = new Ext.form.Checkbox({
			name: 'showCategoryName'
			, labelWidth: 135
			, checked: false
			, fieldLabel: LN('sbi.worksheet.designer.barchartdesignerpanel.form.showCategoryName.title')
		});
	    

	    
	    

		var controlsItems = new Array();

		controlsItems.push(this.orientationCombo);
		controlsItems.push(this.seriesAxisText);
		controlsItems.push(this.showSeriesNameCheck);
		controlsItems.push(this.sortOrderCombo);
		controlsItems.push(this.categoryAxisText);
		controlsItems.push(this.showCategoryNameCheck);
		controlsItems.push(this.legendPositionCombo);
		controlsItems.push(this.showValuesCheck);
		controlsItems.push(this.showLegendCheck);
    	
    	/*
		switch (this.chartLib) {
	        case 'ext3':
	        	break;
	        default:
	        	controlsItems.push(this.showValuesCheck);
		}*/
		
		var fontSizeStore =  Ext.create('Sbi.fonts.stores.FontSizeStore',{});
		
		var fontFamilyStore = Ext.create('Sbi.fonts.stores.FontFamilyStore', {});

		
		    this.fontTypeCombo = new Ext.form.ComboBox({
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
	    
	    this.fontSizeCombo = new Ext.form.ComboBox({
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
	    
	    var chartGeneralFontOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.chartGeneralFontOpts')
	    	, margin: 			10
	    	, items: 			[this.fontTypeCombo, this.fontSizeCombo]	
			, width:			600
		};
		
		/* Font size options configuration */    	
		
		this.legendFontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.legendFontSize'),
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
			name:			'legendFontSize',
			labelWidth:		60,
			width:			110
		});
		
		this.axisTitleFontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.axisTitleFontSize'),
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
			name:			'axisTitleFontSize',
			labelWidth:		100,
			width:			150
		});
		
		this.tooltipLabelFontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.tooltipLabelFontSize'),
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
			name:			'tooltipLabelFontSize',
			labelWidth:		60,
			width:			110
		});
		
		this.axisLabelsFontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.cockpit.designer.fontConf.axisLabelsFontSize'),
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
			name:			'axisLabelsFontSize',
			labelWidth:		100,
			width:			150
		});
		
		
		var chartFontSizeOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.chartFontSizeOpts')
	    	, margin: 			10
	    	, items: 			[this.legendFontSizeCombo, this.axisTitleFontSizeCombo, this.tooltipLabelFontSizeCombo, this.axisLabelsFontSizeCombo]	
			, width:			600
		}; 
		
		/*
		switch (this.chartLib) {
	        case 'ext3':
	        	break;
	        default:
	        	controlsItems.push(this.showValuesCheck);
		}*/
			
		this.fontConfigurationPanel = new Ext.Panel({
			title: 			LN('sbi.cockpit.designer.fontConf.fontOptions')
			, layout: {
				type: 'table',
				columns:1
			}
			, items: 			[chartGeneralFontOptions, chartFontSizeOptions]	
		});    	


		this.form = new Ext.form.FormPanel({
			title: LN('sbi.cockpit.widgets.barchart.barChartWidgetDesigner.title')
			, border: false
			, layout: 'anchor'
			, items: [
			    {
			    	
			    	padding: '1 0 5 6'
			    	, border: false
			    	, items: [
		    	          {
							  xtype: 'fieldset'
							, width: 660
							, fieldDefaults: { margin: '0 9 5 0'}
		    	          	, padding: '0 0 0 5'
							, layout: {type: 'table', columns: 3, tdAttrs: { valign: 'top' } }
				            , collapsible: true
				            , collapsed: false
				            , title: LN('sbi.worksheet.designer.barchartdesignerpanel.form.options.title')
			            	, margin: '0 0 10 0'
//								, title: LN('sbi.worksheet.designer.barchartdesignerpanel.form.fieldsets.options')
							, columnWidth : .275
//								, border: false
							, items: controlsItems
						},
		  			    {
							xtype: 'fieldset'
							, margin: '0 0 0 0'
							, layout: {type: 'table', columns: 1, tdAttrs: { valign: 'top' } }
//							, title: LN('sbi.worksheet.designer.barchartdesignerpanel.form.fieldsets.type')
							//, columnWidth : .430
							, border: 	false
							, items: 	[this.typeRadioGroup]
							, height: 75
						}
						
//						{
//							xtype: 'fieldset'
//								, layout: 'hbox'
//								, collapsible: true
//					            , collapsed: true
//					            , margin: '5 0 5 0'
//					            , title: 'Axis Options'
//								, columnWidth : .275
//								, border: false
//								, items: [this.categoryAxisText,this.seriesAxisText,this.sortOrderCombo]
//							}
			    	]
			    }
				, this.axisDefinitionPanel
			]
		});







	}

	, initTemplate: function () {
	    this.imageTemplate = new Ext.Template('<div class="{0}-{1}-preview" style="height: 100%;"></div>');
	    this.imageTemplate.compile();
	}


	//-----------------------------------------------------------------------------------------------------------------
	//public methods
	//-----------------------------------------------------------------------------------------------------------------
	, addToolTips: function(){
		this.removeListener('afterLayout', this.addToolTips, this);

		var sharedConf = {
			anchor : 'top'
			, width : 200
			, trackMouse : true
		};

		new Ext.ToolTip(Ext.apply({
			target: this.radioGroupIds[0] + '-bodyEl',
			html: LN('sbi.worksheet.designer.barchartdesignerpanel.form.type.tooltip.side-by-side')
		}, sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: this.radioGroupIds[1] + '-bodyEl',
			html: LN('sbi.worksheet.designer.barchartdesignerpanel.form.type.tooltip.stacked')
		}, sharedConf));
		new Ext.ToolTip(Ext.apply({
			target: this.radioGroupIds[2] + '-bodyEl',
			html: LN('sbi.worksheet.designer.barchartdesignerpanel.form.type.tooltip.percent-stacked')
		}, sharedConf));
	}


	, changeBarChartImage: function() {
		var type = this.typeRadioGroup.getValue().type;
		var orientation = this.orientationCombo.getValue();
		var newHtml = this.imageTemplate.apply([type, orientation]);
		this.imageContainerPanel.update(newHtml);
	}

	, getDesignerState: function() {
		Sbi.trace("[BarChartWidgetDesigner.getDesignerState]: IN");
		Sbi.trace("[BarChartWidgetDesigner.getDesignerState]: " + Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner.superclass.getDesignerState);
//		var state = {};
		var state = Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Bar Chart';
		state.wtype = 'barchart';
		state.type = this.typeRadioGroup.getValue().type;
		state.orientation = this.orientationCombo.getValue();
		state.showvalues = this.showValuesCheck.getValue();
		state.showlegend = this.showLegendCheck.getValue();
		state.legendPosition = this.legendPositionCombo.getValue();
		state.categoryAxis = this.categoryAxisText.getValue();
		state.sortOrder = this.sortOrderCombo.getValue();
		
		this.getFontState(state);
		
		state.showSeriesName = this.showSeriesNameCheck.getValue();
		state.showCategoryName = this.showCategoryNameCheck.getValue();
		state.seriesAxis = this.seriesAxisText.getValue();
		state.category = this.categoryContainerPanel.getCategory();
		if(this.showSeriesGroupingPanel === true) {
			state.groupingVariable = this.seriesGroupingPanel.getSeriesGroupingAttribute();
		}
		state.series = this.seriesContainerPanel.getContainedMeasures();

		Sbi.trace("[BarChartWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[BarChartWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner.superclass.setDesignerState(this, state);
		if (state.type) this.typeRadioGroup.setValue({type: state.type});
		if (state.orientation) this.orientationCombo.setValue(state.orientation);
		if (state.showvalues) this.showValuesCheck.setValue(state.showvalues);
		if (state.showlegend) this.showLegendCheck.setValue(state.showlegend);
		if (state.legendPosition) this.legendPositionCombo.setValue(state.legendPosition);
		if (state.categoryAxis) this.categoryAxisText.setValue(state.categoryAxis);
		if (state.seriesAxis) this.seriesAxisText.setValue(state.seriesAxis);
		if (state.sortOrder) this.sortOrderCombo.setValue(state.sortOrder);
		
		this.setFontState(state);

		if (state.showSeriesName) this.showSeriesNameCheck.setValue(state.showSeriesName);
		if (state.showCategoryName) this.showCategoryNameCheck.setValue(state.showCategoryName);
		if (state.category) this.categoryContainerPanel.setCategory(state.category);
		if (state.groupingVariable && this.showSeriesGroupingPanel === true) this.seriesGroupingPanel.setSeriesGroupingAttribute(state.groupingVariable);
		if (state.series) this.seriesContainerPanel.setMeasures(state.series);
//		state.wtype = 'barchart';
		Sbi.trace("[BarChartWidgetDesigner.setDesignerState]: OUT");
	}


	, validate: function(validFields){
		Sbi.trace("[BarChartWidgetDesigner.validate]: IN");
		var valErr='';

		valErr+=''+this.categoryContainerPanel.validate(validFields);
		valErr+=''+this.seriesContainerPanel.validate(validFields);
		if(this.showSeriesGroupingPanel === true) {
			valErr+=''+this.seriesGroupingPanel.validate(validFields);
		}

		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1)
			return LN("sbi.worksheet.designer.validation.invalidFields")+valErr;
		}

		if (this.categoryContainerPanel.category == null){
			return LN("sbi.designerchart.chartValidation.noCategory");
		}
		var store = this.seriesContainerPanel.store;
		var seriesCount = store.getCount();
		if (seriesCount == 0) {
			return LN("sbi.designerchart.chartValidation.noSeries");
		}

		return;

	}

	, containsAttribute: function (attributeId) {
		Sbi.trace("[BarChartWidgetDesigner.containsAttribute]: IN");
		var category = this.categoryContainerPanel.getCategory();
		if (category != null && category.id == attributeId) {
			return true;
		}
		if(this.showSeriesGroupingPanel === true) {
			var groupingVariable = this.seriesGroupingPanel.getSeriesGroupingAttribute();
			if (groupingVariable != null && groupingVariable.id == attributeId) {
				return true;
			}
		}
		return false;
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
	
	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, setFontStateBeforeRender: function(thePanel, state){
		Sbi.trace("[BarChartWidgetDesigner.setFontStateBeforeRender]: IN");
		
		var barChartFonts = this.findBarChartFont()
		
		if(barChartFonts !== undefined && barChartFonts !== null){
			
			if(thePanel.fontType === undefined || thePanel.fontType === null){
				state.fontType = barChartFonts.fontType;
			}else{
				state.fontType = thePanel.fontType;
			}
			
			if(thePanel.fontSize === undefined || thePanel.fontSize === null){
				state.fontSize = barChartFonts.fontSize;
			}else{
				state.fontSize = thePanel.fontSize;
			}
			
			if(thePanel.legendFontSize === undefined || thePanel.legendFontSize === null){
				state.legendFontSize = barChartFonts.legendFontSize;
			}else{
				state.legendFontSize = thePanel.legendFontSize;
			}
			
			if(thePanel.axisTitleFontSize === undefined || thePanel.axisTitleFontSize === null){
				state.axisTitleFontSize = barChartFonts.axisTitleFontSize;
			}else{
				state.axisTitleFontSize = thePanel.axisTitleFontSize;
			}
			
			if(thePanel.tooltipLabelFontSize === undefined || thePanel.tooltipLabelFontSize === null){
				state.tooltipLabelFontSize = barChartFonts.tooltipLabelFontSize;
			}else{
				state.tooltipLabelFontSize = thePanel.tooltipLabelFontSize;
			}
			
			if(thePanel.axisLabelsFontSize === undefined || thePanel.axisLabelsFontSize === null){
				state.axisLabelsFontSize = barChartFonts.axisLabelsFontSize;
			}else{
				state.axisLabelsFontSize = thePanel.axisLabelsFontSize;
			}
		}else{
			state.fontType = thePanel.fontType;
			state.fontSize = thePanel.fontSize;
			state.legendFontSize = thePanel.legendFontSize;
			state.axisTitleFontSize = thePanel.axisTitleFontSize;
			state.tooltipLabelFontSize = thePanel.tooltipLabelFontSize;
			state.axisLabelsFontSize = thePanel.axisLabelsFontSize;
		}
		
		Sbi.trace("[BarChartWidgetDesigner.setFontStateBeforeRender]: OUT");		
	}
	
	, setFontState: function(state){
		Sbi.trace("[BarChartWidgetDesigner.setFontState]: IN");
		
		if (state.fontType) this.fontTypeCombo.setValue(state.fontType);
		if (state.fontSize) this.fontSizeCombo.setValue(state.fontSize);
		if (state.legendFontSize) this.legendFontSizeCombo.setValue(state.legendFontSize);
		if (state.axisTitleFontSize) this.axisTitleFontSizeCombo.setValue(state.axisTitleFontSize);
		if (state.tooltipLabelFontSize) this.tooltipLabelFontSizeCombo.setValue(state.tooltipLabelFontSize);
		if (state.axisLabelsFontSize) this.axisLabelsFontSizeCombo.setValue(state.axisLabelsFontSize);		
		
		Sbi.trace("[BarChartWidgetDesigner.setFontState]: OUT");		
	}
	
	, findBarChartFont: function(){
		Sbi.trace("[BarChartWidgetDesigner.findBarChartFont]: IN");
		
		var barChartFonts = Sbi.storeManager.getFont("barChartFonts");
//		var fonts = Sbi.storeManager.getFonts();
//		
//		var tabIndex = -1;
//		
//		for(var i = 0; i < fonts.length; i++) {
//			if(Sbi.isValorized(fonts[i]) && fonts[i].tabId === "barChartFonts") {
//				tabIndex = i;
//				break;
//			}
//		}
//		
//		if(tabIndex >= 0){
//			barChartFonts = fonts[tabIndex]
//		}
		
		return barChartFonts		
		
		Sbi.trace("[BarChartWidgetDesigner.findBarChartFont]: OUT");		
	}
	
	, getFontState: function(state){
		Sbi.trace("[BarChartWidgetDesigner.getFontState]: IN");
		
		//blank values are permitted, so we need to check the objects before call .getValue()
		if(this.fontTypeCombo !== null)
		{	
			state.fontType = this.fontTypeCombo.getValue();
		}
		if(this.fontSizeCombo !== null)
		{	
			state.fontSize = this.fontSizeCombo.getValue();
		}
		if(this.legendFontSizeCombo !== null)
		{
			state.legendFontSize = this.legendFontSizeCombo.getValue();
		}
		if(this.axisTitleFontSizeCombo !== null)
		{
			state.axisTitleFontSize = this.axisTitleFontSizeCombo.getValue();
		}
		if(this.tooltipLabelFontSizeCombo !== null)
		{
			state.tooltipLabelFontSize = this.tooltipLabelFontSizeCombo.getValue();
		}
		if(this.axisLabelsFontSizeCombo !== null)
		{
			state.axisLabelsFontSize = this.axisLabelsFontSizeCombo.getValue();
		}
		
		Sbi.trace("[BarChartWidgetDesigner.getFontState]: OUT");		
	}
});