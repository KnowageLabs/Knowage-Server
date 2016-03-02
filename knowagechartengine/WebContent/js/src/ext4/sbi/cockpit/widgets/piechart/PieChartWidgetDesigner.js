/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

Ext.define('Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner', {
	extend: 'Sbi.cockpit.core.WidgetDesigner'


	, config:{
		  title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.title')
		, border: false
		, ddGroup: null
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
            items: [this.form]
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
					state.fontType = thePanel.fontType;
					state.fontSize = thePanel.fontSize;
					state.legendFontSize = thePanel.legendFontSize;
					state.tooltipLabelFontSize = thePanel.tooltipLabelFontSize;
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
			labelWidth:		110
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

	    this.fontTypeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.worksheet.designer.fontConf.widgetFontType'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			new Ext.data.ArrayStore({
								fields: ['name','description'],
								data:   [["Times New Roman","Times New Roman"],["Verdana","Verdana"],["Arial","Arial"]]
							}),  
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontType',
			labelWidth:		110,
			width:			245

		});
	    
	    this.fontSizeStore = new Ext.data.ArrayStore({
			fields : ['name', 'description']
			, data : [[6,"6"],[8,"8"],[10,"10"],[12,"12"],[14,"14"],[16,"16"],[18,"18"],[22,"22"],[24,"24"],[28,"28"],[32,"32"],[36,"36"],[40,"40"]]
		});
	    
	    this.fontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.worksheet.designer.fontConf.widgetFontSize'),
			queryMode:      'local',
			triggerAction:  'all',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			typeAhead: 		true,
			lazyRender:		true,
			store: 			this.fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'fontSize',
			labelWidth:		110,
			width:			160

		});

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
    	controlsItems.push(this.fontTypeCombo);
    	controlsItems.push(this.showValuesCheck);
    	controlsItems.push(this.fontSizeCombo);
    	controlsItems.push(this.showPercentageCheck);
    	
    	
    	/* Font size options configuration */    	
    	
    	this.legendFontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.worksheet.designer.fontConf.legendFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			lazyRender:		true,
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			this.fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'legendFontSize',
			labelWidth:		60,
			width:			110
		});
		
		this.tooltipLabelFontSizeCombo = new Ext.form.ComboBox({
			fieldLabel: 	LN('sbi.worksheet.designer.fontConf.tooltipLabelFontSize'),
			typeAhead: 		true,
			triggerAction: 'all',
			lazyRender:		true,
			queryMode:      'local',
			forceSelection: true,
			editable:       false,
			allowBlank: 	true,
			store: 			this.fontSizeStore,    
			valueField: 	'name',
			displayField: 	'description',
			name:			'tooltipLabelFontSize',
			labelWidth:		60,
			width:			110
		});
    	
		this.fontConfigurationPanel = 
    	{
			xtype: 				'fieldset'
			, fieldDefaults: 	{ margin: '0 9 4 0'}
    		, layout: 			{type: 'table', columns: 2}
            , collapsible: 		true
            , collapsed: 		true
            , title: 			LN('sbi.worksheet.designer.fontConf.fontOptions')
			, items: 			[this.legendFontSizeCombo, this.tooltipLabelFontSizeCombo]
			, width:			355
    	}
    	

		this.form = new Ext.Panel({
			border: false
			, layout: 'form'
			, padding: '1 0 5 6'
			, items: [
				{
					xtype: 'fieldset'
					, width: 685
					, fieldDefaults: { margin: '0 9 5 0'}
					, layout: {type: 'table', columns: 2}
		            , collapsible: true
		            , collapsed: true
		            , title: LN('sbi.cockpit.widgets.piechartwidgetdesigner.form.options.title')
	            	, margin: '0 0 10 0'
					, items: controlsItems
				}
				, this.fontConfigurationPanel
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
		if(this.tooltipLabelFontSizeCombo)
		{
			state.tooltipLabelFontSize = this.tooltipLabelFontSizeCombo.getValue();
		}

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
		if (state.fontType) this.fontTypeCombo.setValue(state.fontType);
		if (state.fontSize) this.fontSizeCombo.setValue(state.fontSize);
		if (state.legendFontSize) this.legendFontSizeCombo.setValue(state.legendFontSize);
		if (state.tooltipLabelFontSize) this.tooltipLabelFontSizeCombo.setValue(state.tooltipLabelFontSize);
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

});