/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner', {
	extend: 'Sbi.cockpit.core.WidgetDesigner'


	, config:{
		  title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.title')
		, border: false
		, ddGroup: null,
	}

	, form: null
	, items: null
	, showValuesCheck: null
	, categoryContainerPanel: null
	, seriesContainerPanel: null
	, axisDefinitionPanel: null
	, showLegendCheck: null
	, legendPositionCombo: null
	, showPercentageCheck: null
	, seriesPalette: null
	, chartLib: null
	
	//field to select widget font type
	, fontTypeCombo: null
	//field to select widget font size
	, fontSizeCombo: null
	//field to select chart legend font size
	, legendFontSizeCombo: null
	//field to select chart tooltip font size
	, tooltipLabelFontSizeCombo: null
	//panel to show font size options
	, fontConfigurationPanel: null

	, constructor : function(config) {
		Sbi.trace("[PieChartWidgetDesigner.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.callParent(arguments);
		this.initEvents();
		Sbi.trace("[PieChartWidgetDesigner.constructor]: OUT");
	}

	, initComponent: function() {

        Ext.apply(this, {
            //items: [this.form]
        	items: [Ext.create('Ext.tab.Panel', 
					{
				    	width: 400,
				    	height: 400,
				    	tabPosition: 'right',
				    	items: [ this.form, this.fontConfigurationPanel]
					})]
          , title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.title')
		  , border: false
        });

        this.callParent();
    }



	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	, initEvents: function(){
		this.on(
				'beforerender' ,
				function (thePanel, attribute) {
					var state = {};
					state.showvalues = thePanel.showvalues;
					state.showlegend = thePanel.showlegend;
					state.legendPosition = thePanel.legendPosition;
					state.showpercentage = thePanel.showpercentage;
					state.category = thePanel.category;
					state.series = thePanel.series;
					state.colors = thePanel.colors;
					this.setFontStateBeforeRender(thePanel, state);
					state.wtype = 'piechart';
					this.setDesignerState(state);
				},
				this
			);
		this.addEvents("attributeDblClick", "attributeRemoved");
	}

	, init: function () {
		this.chartLib = 'ext3'; //default

		if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.chartlib) {
			this.chartLib = Sbi.settings.cockpit.chartlib;
		}
		this.chartLib = this.chartLib.toLowerCase();

		this.initTemplate();

		this.showValuesCheck = new Ext.form.Checkbox({
			checked: 		false
			, fieldLabel: 	LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.showvalues.title')
			, labelWidth: 150
		});

		this.showLegendCheck = new Ext.form.Checkbox({
			checked: 		false
			, fieldLabel: 	LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.showlegend.title')
			, labelWidth: 150
		});

		this.legendPositionStore = new Ext.data.ArrayStore({
			fields : ['name', 'description']
			, data : [['bottom', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom')]
					, ['top', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top')]
					, ['left', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.left')]
					, ['right', LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.right')]]
		});
		this.legendPositionCombo = new Ext.form.ComboBox({
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
			store:          this.legendPositionStore,
			width:			245,
			labelWidth:		110,
			rowspan:		3
		});

		this.showPercentageCheck = new Ext.form.Checkbox({
			checked: false
			, labelWidth: 150
			, fieldLabel: LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.showpercentage.title')
		});

		this.seriesPalette = new Sbi.cockpit.widgets.chart.SeriesPalette({
			title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.categorypalette.title')
			, height: 300
			, closeAction: 'hide'
		});

		this.categoryContainerPanel = new Sbi.cockpit.widgets.chart.ChartCategoryPanel({
            width: 200
            , height: 70
            , initialData: null
            , ddGroup: this.ddGroup
            , tools: [{
            	  id: 'list'
                , type: 'collapse'
  	        	, handler: function() {
					this.seriesPalette.show();
				}
  	          	, scope: this
  	          	, qtip: LN('sbi.cockpit.widgets.piechartwidgetdesigner.categorypalette.title')
            }]
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

		this.seriesContainerPanel = new Sbi.cockpit.widgets.chart.ChartSeriesPanel({
            width: 430
            , height: 120
            , initialData: []
            , crosstabConfig: {}
            , ddGroup: this.ddGroup
            , displayColorColumn: false
		});

		this.imageContainerPanel = new Ext.Panel({
            width: 200
            , height: 120
            , html: '<div class="piechart" style="height: 100%;"></div>'
		});

	    this.axisDefinitionPanel = new Ext.Panel({
	        layout: 'table'
	        , baseCls:'x-plain'
//		    , cls: 'centered-panel' //for center the panel
	        , cls: 'x-axis-definition-table'
			, width: this.seriesContainerPanel.width+this.imageContainerPanel.width+20 //for center the panel
	        , padding: '0 10 10 10'
	        , layoutConfig: {columns : 2}
	        // applied to child components
	        //, defaults: {height: 100}
	        , items:[
	            this.seriesContainerPanel
	            , this.imageContainerPanel
	            , {
		        	border: false
		        }
		        , this.categoryContainerPanel
		    ]
	    });

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
			labelWidth:		110,
			width:			160

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

		var controlsItems = new Array();

		/*
		switch (this.chartLib) {
	        case 'ext3':
	        	break;
	        default:
	        	controlsItems.push(this.showValuesCheck);
		}
		*/

		controlsItems.push(this.legendPositionCombo);
		controlsItems.push(this.showLegendCheck);
    	controlsItems.push(this.showValuesCheck);
    	controlsItems.push(this.showPercentageCheck);
    	
    	
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
    	
		var chartFontSizeOptions = 
		{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: 5}
			, layout: 			{type: 'table', columns: 2}
	        , collapsible: 		true
	        , collapsed: 		false
	        , title: 			LN('sbi.cockpit.designer.fontConf.chartFontSizeOpts')
	    	, margin: 			10
	    	, items: 			[this.legendFontSizeCombo, this.tooltipLabelFontSizeCombo]	
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
			, items: 			[chartGeneralFontOptions, chartFontSizeOptions]	
		});
    	

		this.form = new Ext.Panel({
			title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.title')
			, border: false
			, layout: 'form'
			, padding: '1 0 5 6'
			, items: [
				{
					xtype: 'fieldset'
					, width: 660
					, fieldDefaults: { margin: '0 9 5 0'}
					, layout: {type: 'table', columns: 2, tdAttrs: { valign: 'top' }}
		            , collapsible: true
		            , collapsed: false
		            , title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.options.title')
	            	, margin: '0 0 10 0'
					, items: controlsItems
				}
//				,{
//					xtype: 'fieldset'
//					, layout: 'column'
//					, columnWidth : .9
//					, style: 'padding: 10px 0px 0px 15px;'
//					, border: false
//					, items: [this.showPercentageCheck]
//				}
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
	, getDesignerState: function() {
		Sbi.trace("[PieChartWidgetDesigner.getDesignerState]: IN");
		Sbi.trace("[PieChartWidgetDesigner.getDesignerState]: " + Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner.superclass.getDesignerState);
		var state = Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Pie Chart';
		state.wtype = 'piechart';
		state.showvalues = this.showValuesCheck.getValue();
		state.showlegend = this.showLegendCheck.getValue();
		state.legendPosition = this.legendPositionCombo.getValue();
		state.showpercentage = this.showPercentageCheck.getValue();
		state.category = this.categoryContainerPanel.getCategory();
		state.series = this.seriesContainerPanel.getContainedMeasures();
		state.colors = this.seriesPalette.getColors();
		
		this.getFontState(state);

		Sbi.trace("[PieChartWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[PieChartWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner.superclass.setDesignerState(this, state);
		if (state.showvalues) this.showValuesCheck.setValue(state.showvalues);
		if (state.showlegend) this.showLegendCheck.setValue(state.showlegend);
		if (state.legendPosition) this.legendPositionCombo.setValue(state.legendPosition);
		if (state.showpercentage) this.showPercentageCheck.setValue(state.showpercentage);
		if (state.category) this.categoryContainerPanel.setCategory(state.category);
		if (state.series) this.seriesContainerPanel.setMeasures(state.series);
		if (state.colors) this.seriesPalette.setColors(state.colors);
		
		this.setFontState(state);
		
		Sbi.trace("[PieChartWidgetDesigner.setDesignerState]: OUT");
	}

	, validate: function(validFields){
		var valErr='';
		valErr+=''+this.categoryContainerPanel.validate(validFields);
		valErr+=''+this.seriesContainerPanel.validate(validFields);

		if(valErr!= ''){
			valErr = valErr.substring(0, valErr.length - 1);
			return LN("sbi.cockpit.designer.validation.invalidFields")+valErr;
		}

		if (this.categoryContainerPanel.category== null){
			return LN("sbi.designerchart.chartValidation.noCategory");
		}
		var store = this.seriesContainerPanel.store;
		var seriesCount = store.getCount();
		if(seriesCount == 0 ){
			return LN("sbi.designerchart.chartValidation.noSeries");
		}



		return;

	}

	, containsAttribute: function (attributeId) {
		if (this.categoryContainerPanel.category == null) {
			return false;
		} else {
			return this.categoryContainerPanel.category.id == attributeId;
		}
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	
	, setFontStateBeforeRender: function(thePanel, state){
		Sbi.trace("[PieChartWidgetDesigner.setFontStateBeforeRender]: IN");
		
		var pieChartFonts = this.findPieChartFont()
		
		if(pieChartFonts !== undefined && pieChartFonts !== null){
			
			if(thePanel.fontType === undefined || thePanel.fontType === null){
				state.fontType = pieChartFonts.fontType;
			}else{
				state.fontType = thePanel.fontType;
			}
			
			if(thePanel.fontSize === undefined || thePanel.fontSize === null){
				state.fontSize = pieChartFonts.fontSize;
			}else{
				state.fontSize = thePanel.fontSize;
			}
			
			if(thePanel.legendFontSize === undefined || thePanel.legendFontSize === null){
				state.legendFontSize = pieChartFonts.legendFontSize;
			}else{
				state.legendFontSize = thePanel.legendFontSize;
			}
			
			if(thePanel.tooltipLabelFontSize === undefined || thePanel.tooltipLabelFontSize === null){
				state.tooltipLabelFontSize = pieChartFonts.tooltipLabelFontSize;
			}else{
				state.tooltipLabelFontSize = thePanel.tooltipLabelFontSize;
			}

		}else{
			state.fontType = thePanel.fontType;
			state.fontSize = thePanel.fontSize;
			state.legendFontSize = thePanel.legendFontSize;
			state.tooltipLabelFontSize = thePanel.tooltipLabelFontSize;
		}
		
		Sbi.trace("[PieChartWidgetDesigner.setFontStateBeforeRender]: OUT");		
	}
	
	, setFontState: function(state){
		Sbi.trace("[PieChartWidgetDesigner.setFontState]: IN");
		
		if (state.fontType) this.fontTypeCombo.setValue(state.fontType);
		if (state.fontSize) this.fontSizeCombo.setValue(state.fontSize);
		if (state.legendFontSize) this.legendFontSizeCombo.setValue(state.legendFontSize);
		if (state.tooltipLabelFontSize) this.tooltipLabelFontSizeCombo.setValue(state.tooltipLabelFontSize);	
		
		Sbi.trace("[PieChartWidgetDesigner.setFontState]: OUT");		
	}
	
	, findPieChartFont: function(){
		Sbi.trace("[PieChartWidgetDesigner.findPieChartFont]: IN");
		
		var pieChartFonts = Sbi.storeManager.getFont("pieChartFonts");
//		var fonts = Sbi.storeManager.getFonts();
//		
//		var tabIndex = -1;
//		
//		for(var i = 0; i < fonts.length; i++) {
//			if(Sbi.isValorized(fonts[i]) && fonts[i].tabId === "pieChartFonts") {
//				tabIndex = i;
//				break;
//			}
//		}
//		
//		if(tabIndex >= 0){
//			pieChartFonts = fonts[tabIndex]
//		}
		
		return pieChartFonts		
		
		Sbi.trace("[PieChartWidgetDesigner.findPieChartFont]: OUT");		
	}
	
	, getFontState: function(state){
		Sbi.trace("[PieChartWidgetDesigner.getFontState]: IN");
		
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
		if(this.tooltipLabelFontSizeCombo !== null)
		{
			state.tooltipLabelFontSize = this.tooltipLabelFontSizeCombo.getValue();
		}
		
		Sbi.trace("[PieChartWidgetDesigner.getFontState]: OUT");		
	}

});