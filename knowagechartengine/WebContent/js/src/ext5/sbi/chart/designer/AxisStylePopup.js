Ext.define('Sbi.chart.designer.AxisStylePopup', {
	extend: 'Ext.form.Panel',
	require: [
  	    'Sbi.chart.designer.FontCombo',
  	    'Sbi.chart.designer.FontStyleCombo',
  	    'Sbi.chart.designer.FontDimCombo',
  	    'Sbi.chart.designer.FontAlignCombo',
  	    'Sbi.chart.designer.FontVerticalAlignCombo',
  	    'Sbi.chart.designer.TypeLineCombo'
  	],
	id: 'axisStylePopup',
	title: LN('sbi.chartengine.axisstylepopup.popup.title'),
    layout: 'border',
    bodyPadding: 5,
	floating: true,
    draggable: true,
    closable : true,
    closeAction: 'destroy',
    
    /**
     * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
//    width: 500,
    
    /**
     * Providing these possibilities for the Axis style configuration popup:
     * (1) 	Resizing of the popup window for both axes (vertically and horizontally)
     * (2) 	The height is static and when initially rendered it has the value defined
     * 		in the parameter specified here (afterwards, user can resize it). But in both
     * 		situations, the height of the popup will remain constants, so the expansion of
     * 		fieldsets within it will just enable the inner vertical scrollbar.
     * (3) 	The width of the initial popup (until user potentially change it) is reduced.
     * 
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    width: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.width,
    height: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.height,
    resizable: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.resizable,
    overflowY: Sbi.settings.chart.structureStep.axisAndSerieStyleConfigPopup.overflowY,
    
    modal: true,
	config: {
		axisData: {},
		isYAxis: false
	},
	axisData: {},
	isYAxis: false,
	
	/* * * * * * * Internal components * * * * * * * */
	axisFieldSet: null,
	titleFieldSet: null,
	majorgridFieldSet: null,
	minorgridFieldSet: null,
	
	aliasTextField: null,
	styleRotateNumberField: null,
	styleAlignComboBox: null,
	styleColor: null,
	styleFontComboBox: null,
	styleFontWeighComboBox: null,
	styleFontSizeComboBox: null,
	
	majorgridIntervalNumberField: null,
	majorgridStyleTypelineComboBox: null,
	majorgridStyleColor: null,
	minorgridIntervalNumberField: null,
	minorgridStyleTypelineComboBox: null,
	minorgridStyleColor: null,

	titleTextTextField: null,
	titleStyleAlignComboBox: null,
	titleStyleColor: null,
	titleStyleFontComboBox: null,
	titleStyleFontWeighComboBox: null,
	titleStyleFontSizeComboBox: null,
	/* * * * * * * END Internal components * * * * * * * */
	layout: 'anchor',
    defaults: {
        anchor: '100%',
    },
    defaultType: 'textfield',
    
    constructor: function(config) {
    	
    	var LABEL_WIDTH = 115;
    	
		this.callParent(config);
		
		// Esc key pressing closes the modal
		this.keyMap = new Ext.util.KeyMap(Ext.getBody(), [{
			key: Ext.EventObject.ESC,
			defaultEventAction: 'preventDefault',
			scope: this,
			fn: function() {
				this.destroy()
			}
		}]);
		
		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
		
		var plotbandsStore = Ext.create('Sbi.chart.designer.PlotbandsStore', {});
		
		this.axisData = config.axisData;
		this.allAxisData = config.allAxisData;
		
		var isYAxis = (config.isYAxis != undefined)? config.isYAxis: false;
		this.isYAxis = isYAxis;
		
		/**
	     * https://production.eng.it/jira/browse/KNOWAGE-491
	     * When we are creating a chart from the Cockpit Engine, we need to use
	     * a different height, because inside the Wizard, there is less space
	     * 
	     * @author Giorgio Federici (giofeder, giorgio.federici@eng.it)
	     */
		
		if(Sbi.chart.designer.ChartUtils.isCockpitEngine){
			this.setHeight(Sbi.settings.chart.structureStep.cockpitAxisAndSerieStyleConfigPopup.height);
		}
		
		this.axisFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: LN("sbi.chartengine.axisstylepopup.axis"),	// danristo
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : LABEL_WIDTH
			},
			layout: 'anchor',
			items : []
		});
		
		this.titleFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			
			/**
			 * Only for those two D3 chart types we need axis style configuration popup,
			 * but without the Title fieldset.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net) 
			 */
			hidden: (chartType == "CHORD" || chartType == "PARALLEL"),
			
			id: 'titleFieldSetForAxis',	// (danristo :: danilo.ristovski@mht.net) 
			title: LN("sbi.chartengine.axisstylepopup.title"),	// danristo
			defaults: {
				anchor: '100%',
				labelAlign : 'left',
				labelWidth : LABEL_WIDTH
			},
			layout: 'anchor',
			items : []
		});		
		
		if (chartType == "GAUGE")
		{
			this.axisAdditionalParamsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					collapsed: true,	// collapsed by default (when window pops up)
					id: 'axisAdditionalParamsFieldSet',	
					title: LN('sbi.chartengine.axisstylepopup.additionalParams.title'),
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : LABEL_WIDTH
					},
					
					layout: 'anchor',
					items : []
				}
			);
			
			this.axisTickParamsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					collapsed: true,	// collapsed by default (when window pops up) 
					id: 'axisTickParamsFieldSet',	
					title: LN('sbi.chartengine.axisstylepopup.mainTickParams.title'),	
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : LABEL_WIDTH
					},
					
					layout: 'anchor',
					items : []
				}
			);
			
			this.axisMinorTickParamsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					collapsed: true,	// collapsed by default (when window pops up) 
					id: 'axisMinorTickParamsFieldSet',	
					title: LN('sbi.chartengine.axisstylepopup.minorTickParams.title'),
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : LABEL_WIDTH
					},
					
					layout: 'anchor',
					items : []
				}
			);
			
			this.axisLabelsParamsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					collapsed: true,	// collapsed by default (when window pops up) 
					id: 'axisLabelsParamsFieldSet',	
					title: LN('sbi.chartengine.axisstylepopup.labelParams.title'),
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : LABEL_WIDTH
					},
					
					layout: 'anchor',
					items : []
				}
			);
			
			this.axisPlotbandsParamsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					collapsed: true,	// collapsed by default (when window pops up) 
					id: 'axisPlotbandsParamsFieldSet',	
					title: LN('sbi.chartengine.axisstylepopup.plotbandParams.title'),	
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : LABEL_WIDTH
					},
					
					layout: 'anchor',
					items : []
				}
			);
		}
		
		if(isYAxis) {
			this.majorgridFieldSet = Ext.create('Ext.form.FieldSet', {
				collapsible: true,
				collapsed : true,
				id: "majorGridFieldSetYAxis",	// (danristo :: danilo.ristovski@mht.net) 
				title: LN('sbi.chartengine.axisstylepopup.majorgrid'),
				defaults: {
					anchor: '100%',
					labelAlign : 'left',
					labelWidth : LABEL_WIDTH
				},
				layout: 'anchor',
				items : []
			});
			
			this.minorgridFieldSet = Ext.create('Ext.form.FieldSet', {
				collapsible: true,
				collapsed : true,
				id: "minorGridFieldSetYAxis", // (danristo :: danilo.ristovski@mht.net) 
				title: LN('sbi.chartengine.axisstylepopup.minorgrid'),
				defaults: {
					anchor: '100%',
					labelAlign : 'left',
					labelWidth : LABEL_WIDTH
				},
				layout: 'anchor',
				items : []
			});
		}
		
		var styleRotate = this.axisData.styleRotate;
		this.styleRotateNumberField = Ext.create('Ext.form.field.Number', {
			fieldLabel: LN('sbi.chartengine.axisstylepopup.rotate'),
			emptyText: LN("sbi.chartengine.structure.axisStyleConfig.axis.labelRotate.emptyText"),
			selectOnFocus: true,
			value: styleRotate ? '' + styleRotate : '',
			maxValue: 180,
			minValue: -180,
		});
		
		/**
		 * For those two chart types we do not need this parameter (feature).
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		if (chartType != "CHORD" && chartType != "PARALLEL")
		{
			this.axisFieldSet.add(this.styleRotateNumberField);
		}
		
		var styleAlign = this.axisData.styleAlign;
		if(isYAxis) {
			this.styleAlignComboBox = Ext.create('Sbi.chart.designer.FontVerticalAlignCombo', {
				value: (styleAlign && styleAlign.trim() != '') ? styleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.axis.align'),
			});
		} else {
			this.styleAlignComboBox = Ext.create('Sbi.chart.designer.FontAlignCombo', {
				value: (styleAlign && styleAlign.trim() != '') ? styleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.axis.align'),
			});
		}
		
		/**
		 * For those two chart types we do not need this parameter (feature).
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		if (chartType != "CHORD" && chartType != "PARALLEL")
		{
			this.axisFieldSet.add(this.styleAlignComboBox);
		}
		
		var axisStyleColor = (this.axisData.styleColor || '').replace('#', '');		
		this.styleColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
			fieldLabel : LN('sbi.chartengine.axisstylepopup.color'),
			emptyText: LN('sbi.chartengine.configuration.axiscolor.emptyText'),
			labelWidth : LABEL_WIDTH,
			value: axisStyleColor
		});
		this.axisFieldSet.add(this.styleColor);
		
		var styleFont = this.axisData.styleFont;
		
		this.styleFontComboBox = Ext.create('Sbi.chart.designer.FontCombo', {
			value: (styleFont && styleFont.trim() != '') ? styleFont.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.font'),
		});
		this.axisFieldSet.add(this.styleFontComboBox);

		var styleFontWeigh = this.axisData.styleFontWeigh;
		this.styleFontWeighComboBox = Ext.create('Sbi.chart.designer.FontStyleCombo', {
			value: (styleFontWeigh && styleFontWeigh.trim() != '') ? styleFontWeigh.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontweight'),
		});
		this.axisFieldSet.add(this.styleFontWeighComboBox);
		
		var styleFontSize = this.axisData.styleFontSize;
		this.styleFontSizeComboBox = Ext.create('Sbi.chart.designer.FontDimCombo', {
			value: (styleFontSize && styleFontSize.trim() != '') ? styleFontSize.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontsize'),
		});
		this.axisFieldSet.add(this.styleFontSizeComboBox);
		
		/**
		 * NOTE: The Opposite option is disabled completely from every chart (also for the SCATTER
		 * chart) since the behavior of the parameter in combination with other axis parameters was 
		 * providing more troubles than bringing advantages.
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
//		/**
//		 * The 'opposite' parameter is enabled only when we have the SCATTER chart type 
//		 * and for the Y-axis exclusively.
//		 * 
//		 * @author: danristo (danilo.ristovski@mht.net)
//		 */	
//		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "SCATTER"
//			 && this.axisData.axisType.toLowerCase() != "category")
//		{
//			this.styleOpposite = Ext.create
//	    	(
//				{
//			        xtype: 'checkboxfield',
//			        id: 'oppositeAxis',
//			        value: this.axisData.styleOpposite,
//			        labelSeparator: '',
//			        fieldLabel: LN("sbi.chartengine.axisstylepopup.opposite")+":", 
//			    }	
//	    	);
//			
//			this.axisFieldSet.add(this.styleOpposite);
//		}		
		
		if(isYAxis) {
			var majorgridInterval = '' + this.axisData.majorgridInterval;
			this.majorgridIntervalNumberField = Ext.create('Ext.form.field.Number', {
				fieldLabel: LN('sbi.chartengine.axisstylepopup.interval'),
				selectOnFocus: true,
				value: (majorgridInterval && majorgridInterval.trim() != '') ? majorgridInterval.trim() : '',
				minValue: 0,
				emptyText: LN("sbi.chartengine.structure.axisStyleConfig.grid.lineInterval.emptyText")
			});
			this.majorgridFieldSet.add(this.majorgridIntervalNumberField);
			
			var majorgridStyleTypeline = this.axisData.majorgridStyleTypeline;
			this.majorgridStyleTypelineComboBox = Ext.create('Sbi.chart.designer.TypeLineCombo', {
				value: (majorgridStyleTypeline && majorgridStyleTypeline.trim() != '') ? majorgridStyleTypeline.trim() : '',
				fieldLabel : LN('sbi.chartengine.axisstylepopup.typeline'),
			});
			this.majorgridFieldSet.add(this.majorgridStyleTypelineComboBox);

			var majorgridStyleColor = (this.axisData.majorgridStyleColor || '').replace('#', '');
			this.majorgridStyleColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
				fieldLabel : LN('sbi.chartengine.axisstylepopup.majorminorgrid.color'),
				emptyText: LN('sbi.chartengine.configuration.axismajorgridcolor.emptyText'),
				labelWidth : LABEL_WIDTH,
				value: majorgridStyleColor
			});
			this.majorgridFieldSet.add(this.majorgridStyleColor);
			
			var minorgridInterval = '' + this.axisData.minorgridInterval;
			this.minorgridIntervalNumberField = Ext.create('Ext.form.field.Number', {
				fieldLabel: LN('sbi.chartengine.axisstylepopup.interval'),
				selectOnFocus: true,
				value: (minorgridInterval && minorgridInterval.trim() != '') ? minorgridInterval.trim() : '',
				minValue: 0,
				emptyText: LN("sbi.chartengine.structure.axisStyleConfig.grid.lineInterval.emptyText")
			});
			this.minorgridFieldSet.add(this.minorgridIntervalNumberField);
			
			var minorgridStyleTypeline = this.axisData.minorgridStyleTypeline;
			this.minorgridStyleTypelineComboBox = Ext.create('Sbi.chart.designer.TypeLineCombo', {
				value: (minorgridStyleTypeline && minorgridStyleTypeline.trim() != '') ? minorgridStyleTypeline.trim() : '',
				fieldLabel : LN('sbi.chartengine.axisstylepopup.typeline'),
			});
			this.minorgridFieldSet.add(this.minorgridStyleTypelineComboBox);

			var minorgridStyleColor = (this.axisData.minorgridStyleColor || '').replace('#', '');
			this.minorgridStyleColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
				fieldLabel : LN('sbi.chartengine.axisstylepopup.majorminorgrid.color'),
				emptyText: LN('sbi.chartengine.configuration.axisminorgridcolor.emptyText'),
				labelWidth : LABEL_WIDTH,
				value: minorgridStyleColor
			});
			this.minorgridFieldSet.add(this.minorgridStyleColor);
		}
				
		var titleStyleAlign = this.axisData.titleStyleAlign;
		if(isYAxis) {
			this.titleStyleAlignComboBox = Ext.create('Sbi.chart.designer.FontVerticalAlignCombo', {
				value: (titleStyleAlign && titleStyleAlign.trim() != '') ? titleStyleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.title.align'),
			});
		} else {
			this.titleStyleAlignComboBox = Ext.create('Sbi.chart.designer.FontAlignCombo', {
				value: (titleStyleAlign && titleStyleAlign.trim() != '') ? titleStyleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.title.align'),
			});
		}
		this.titleFieldSet.add(this.titleStyleAlignComboBox);
		
		/**
		 * Only for GAUGE chart type: add new field set group that will contain graphical elements for additional 
		 * parameters for Y axis of the chart. This field set will be collapsible.
		 * 
		 * Parameters: 
		 * 		min, max, lineColor, offset, lineWidth, endOnTick
		 */
		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE")
		{	
			/**
			 * ********************************************************
			 * Axis additional parameters (START)
			 * ********************************************************
			 */
			
			/**
			 * MIN			 
			 * 
			 * 		Description: 
			 * 			Axis bottom value, minimum value shown on the speedometer line
			 */
			this.minValueYAxis = Ext.create
	    	(
				{
			        xtype: 'numberfield',
			        id: 'minValueYAxis',
			        value: this.axisData.min,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.minValueYAxis")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.addParams.min.emptyText")
			    }	
	    	);
			
			/**
			 * MAX
			 * 
			 * 		Description: 
			 * 			Axis top value, maximum value shown on the speedometer line
			 */
			this.maxValueYAxis = Ext.create
	    	(
				{
			        xtype: 'numberfield',
			        id: 'maxValueYAxis',
			        value: this.axisData.max,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.maxValueYAxis")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.addParams.max.emptyText")
			    }	
	    	);
			
			/**
			 * LINE COLOR
			 * 		Description: Color of the speedometer line
			 */
			var lineColor = (this.axisData.lineColor || '').replace('#', '');
			this.lineColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
				fieldLabel : LN('sbi.chartengine.axisstylepopup.additionalParams.lineColor'),
				emptyText: LN('sbi.chartengine.configuration.speedometerlinecolor.emptyText'),
				labelWidth : LABEL_WIDTH,
				value: lineColor
			});
			
			/**
			 * OFFSET
			 * 
			 * 		Description: 	
			 * 			Distance (in pixels) between the speedometer border and the axes (or �speed� line). 
			 * 			If positive, the line will be outside the speedometer. If negative the axes will be 
			 * 			closer to the center
			 */
//			this.offsetBorderFromYAxis = Ext.create
//	    	(
//				{
//			        xtype: 'numberfield',
//			        id: 'offsetBorderFromYAxis',
//			        value: this.axisData.offset,
//			        maxValue: 60,
//			        labelSeparator: '',
//			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.offset") 
//			    }	
//	    	);
			
			/**
			 * LINE WIDTH
			 * 
			 * 		Description: 	
			 * 			The width of the axis (speedometer) line
			 */
			this.lineWidthYAxis = Ext.create
	    	(
				{
			        xtype: 'numberfield',
			        id: 'lineWidthYAxis',
			        value: this.axisData.lineWidth,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.lineWidth")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.addParams.lineWidth.emptyText")
			    }	
	    	);
			
			/**
			 * END ON TICK
			 * 
			 * 		Description: 	
			 * 			If true the axes will end with a tick. Default: true
			 */
			this.endOnTick = Ext.create
	    	(
				{
			        xtype: 'checkboxfield',
			        id: 'endOnTickYAxis',
			        value: this.axisData.endOnTickGauge,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.endOnTick")+":"
			    }	
	    	);
			
			this.axisAdditionalParamsFieldSet.add(this.minValueYAxis);
			this.axisAdditionalParamsFieldSet.add(this.maxValueYAxis);
			this.axisAdditionalParamsFieldSet.add(this.lineColor);
			//this.axisAdditionalParamsFieldSet.add(this.offsetBorderFromYAxis);
			this.axisAdditionalParamsFieldSet.add(this.lineWidthYAxis);
			this.axisAdditionalParamsFieldSet.add(this.endOnTick);
			
			/**
			 * ********************************************************
			 * Axis additional parameters (END)
			 * ********************************************************
			 */
			
			// ----------------------------------------------------------------------
			
			/**
			 * ********************************************************
			 * Axis - main tick parameters (START)
			 * ********************************************************
			 */

			/**
			 * TICK POSITION
			 * 
			 * 		Description: 	
			 * 			The position of the hyphens (ticks) of the main line: outside (default) or inside
			 */
			this.tickPosition = Ext.create
			(				
				{					
					xtype : 'combo',
           		 	queryMode : 'local',
           		 	value : this.axisData.tickPosition,
           		 	triggerAction : 'all',
           		 	forceSelection : true,
           		 	editable : false,
           		 	fieldLabel : LN("sbi.chartengine.axisstylepopup.mainTickParams.tickPosition"),
           		 	displayField : 'name',
           		 	valueField : 'value',
           		 	emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.position.emptyText"),
           		
           		 	store : 
           		 	{
	           			 fields : ['name', 'value'],
	           			
	           			 data : 
           				 [ 
           				  	{
           				  		name : LN("sbi.chartengine.axisstylepopup.mainTickParams.tickPosition.inside"),
           				  		value : 'inside'
           				  	}, 
           				   
           				  	{
           				  		name : LN("sbi.chartengine.axisstylepopup.mainTickParams.tickPosition.outside"),
           				  		value : 'outside'
           				  	}
           				]
           		 	}
					
				}
			);
			
			/**
			 * TICK COLOR
			 * 
			 * 		Description: 	
			 * 			Axis main tick color
			 */
			var tickColor = (this.axisData.tickColor || '').replace('#', '');
			this.tickColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
				fieldLabel : LN('sbi.chartengine.axisstylepopup.mainTickParams.tickColor'),
				emptyText: LN('sbi.chartengine.configuration.tickcolor.emptyText'),
				labelWidth : LABEL_WIDTH,
				value: tickColor
			});
			
			/**
			 * TICK PIXEL INTERVAL
			 * 
			 * 		Description: 	
			 * 			Size of the gap between two (main) ticks using the measure unit of the axis
			 */
			this.tickPixelInterval = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'tickPixelInterval',
			        value: this.axisData.tickPixelInterval,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.mainTickParams.tickPixelInterval")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.pixelInterval.emptyText")
			    }
			);
			
			/**
			 * TICK WIDTH
			 * 
			 * 		Description: 	
			 * 			Axis main tick width
			 */
			this.tickWidth = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'tickWidth',
			        value: this.axisData.tickWidth,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.mainTickParams.tickWidth")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.width.emptyText")
			    }
			);
			
			/**
			 * TICK LENGTH
			 * 
			 * 		Description: 	
			 * 			Axis main tick length
			 */
			this.tickLength = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'tickLength',
			        value: this.axisData.tickLength,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.mainTickParams.tickLength")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.length.emptyText")
			    }
			);
			
			this.axisTickParamsFieldSet.add(this.tickPosition);
			this.axisTickParamsFieldSet.add(this.tickColor);
			this.axisTickParamsFieldSet.add(this.tickPixelInterval);
			this.axisTickParamsFieldSet.add(this.tickWidth);
			this.axisTickParamsFieldSet.add(this.tickLength);
			
			/**
			 * ********************************************************
			 * Axis - main tick parameters (END)
			 * ********************************************************
			 */
			
			// ----------------------------------------------------------------------
			
			/**
			 * ********************************************************
			 * Axis - minor tick parameters (START)
			 * ********************************************************
			 */
			
			/**
			 * MINOR TICK POSITION
			 * 
			 * 		Description: 	
			 * 			Whether to display minor tick inside or outside the line: outside (default) or inside
			 */
			this.minorTickPosition = Ext.create
			(				
				{					
					xtype : 'combo',
           		 	queryMode : 'local',
           		 	value : this.axisData.minorTickPosition,
           		 	triggerAction : 'all',
           		 	forceSelection : true,
           		 	editable : false,
           		 	fieldLabel : LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickPosition"),
           		 	displayField : 'name',
           		 	valueField : 'value',
           		 	emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.position.emptyText"),
           		
           		 	store : 
           		 	{
	           			 fields : ['name', 'value'],
	           			
	           			 data : 
           				 [ 
           				  	{
           				  		name : LN("sbi.chartengine.axisstylepopup.minorTickParams.tickPosition.inside"),	
           				  		value : 'inside'
           				  	}, 
           				   
           				  	{
           				  		name : LN("sbi.chartengine.axisstylepopup.minorTickParams.tickPosition.outside"),	
           				  		value : 'outside'
           				  	}
           				]
           		 	}
					
				}
			);
			
			/**
			 * MINOR TICK COLOR
			 * 
			 * 		Description: 	
			 * 			Axis minor tick color
			 */
			var minorTickColor = (this.axisData.minorTickColor || '').replace('#', '');
			this.minorTickColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
				fieldLabel : LN('sbi.chartengine.axisstylepopup.minorTickParams.tickColor'),
				emptyText: LN('sbi.chartengine.configuration.tickcolor.emptyText'),
				labelWidth : LABEL_WIDTH,
				value: minorTickColor
			});
			
			/**
			 * MINOR TICK INTERVAL
			 * 
			 * 		Description: 	
			 * 			Size of the gap between two minor ticks using the measure unit of the axis. when "auto", 
			 * 			it will be calculated as 1/5 of the tickPixelInterval
			 */
			this.minorTickInterval = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'minorTickInterval',
			        value: this.axisData.minorTickInterval,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickInterval")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.pixelInterval.emptyText")
			    }
			);
			
			/**
			 * MINOR TICK WIDTH
			 * 
			 * 		Description: 	
			 * 			Axis main tick width
			 */
			this.minorTickWidth = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'minorTickWidth',
			        value: this.axisData.minorTickWidth,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickWidth")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.width.emptyText")
			    }
			);
			
			/**
			 * MINOR TICK LENGTH
			 * 
			 * 		Description: 	
			 * 			Axis minor tick length
			 */
			this.minorTickLength = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'minorTickLength',
			        value: this.axisData.minorTickLength,
			        minValue: 0,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickLength")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.tick.length.emptyText")
			    }
			);
			
			this.axisMinorTickParamsFieldSet.add(this.minorTickPosition);
			this.axisMinorTickParamsFieldSet.add(this.minorTickColor);
			this.axisMinorTickParamsFieldSet.add(this.minorTickInterval);
			this.axisMinorTickParamsFieldSet.add(this.minorTickWidth);
			this.axisMinorTickParamsFieldSet.add(this.minorTickLength);
			
			
			/**
			 * ********************************************************
			 * Axis - tick parameters (END)
			 * ********************************************************
			 */
			
			// ----------------------------------------------------------------------
			
			/**
			 * ********************************************************
			 * Axis - labels parameters (START)
			 * ********************************************************
			 */
			
			/**
			 * DISTANCE
			 * 
			 * 		Description: 	
			 * 			Distance of the label from the axis. Default: 15
			 */			
			this.distanceLabelFromYAxis = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'distanceLabelFromYAxis',
			        minValue: -60,
			        value: this.axisData.distance,
			        labelSeparator: '',			        
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.labelParams.distanceLabelFromYAxis")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.labels.distance.emptyText")
			    }
			);
			
			/**
			 * ROTATION
			 * 
			 * 		Description: 	
			 * 			Label rotation express in degrees 0-360. Default: 0
			 */
			this.rotationOfLabelYAxis = Ext.create
			(
				{
			        xtype: 'numberfield',
			        id: 'rotationOfLabelYAxis',
//			        minValue: 0,
//			        maxValue: 359,
			        value: this.axisData.rotation,
			        labelSeparator: '',			        
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.labelParams.rotationOfLabelYAxis")+":",
			        emptyText: LN("sbi.chartengine.structure.axisStyleConfig.labels.rotation.emptyText")
			    }
			);
			
			this.axisLabelsParamsFieldSet.add(this.distanceLabelFromYAxis);
			this.axisLabelsParamsFieldSet.add(this.rotationOfLabelYAxis);
			
			/**
			 * ********************************************************
			 * Axis - labels parameters (END)
			 * ********************************************************
			 */
			
			// ----------------------------------------------------------------------
			
			if (this.axisData.from)
			{				
				if (this.axisData.from.length)
				{
					var numPlots = this.axisData.from.length;
					
					if (numPlots > 1)
					{						
						for (var i=0; i<numPlots; i++)
						{						
							var t = Ext.create('Sbi.chart.designer.PlotbandsModel', {
			                    from: this.axisData.from[i],
			                    to: this.axisData.to[i],
			                    color:this.axisData.color[i]
			                });						
								
							plotbandsStore.insert(i,t);
						}
					}
					else if (!(this.axisData.from[0] == "" && this.axisData.to[0] == "" && this.axisData.color[0] == ""))
					{						
						var t = Ext.create('Sbi.chart.designer.PlotbandsModel', {
		                    from: this.axisData.from[0],
		                    to: this.axisData.to[0],
		                    color:this.axisData.color[0]
		                });						
							
						plotbandsStore.insert(0,t);
					}
				}
			}
					
			var axisStylePopupScope = this;
					
			this.plotsContainer = Ext.create
			(
				"Sbi.chart.designer.ChartColumnsContainer", 
				
				{				
					//minHeight: 300,
					//height: 300,
					flex: 1,
									
					id: "plotsContainer",
					store: plotbandsStore, 
					
					title: LN("sbi.chartengine.configuration.gauge.axisStylePopup.plotbands.title"),
									
					tools:
					[		
					 	//CLEAR BUTTON
					 	Ext.create
					 	(
				 			"Ext.panel.Tool",
				 			
				 			{
				 				type: "deleteAllItemsFromAxisPanel",
				 				
				 				handler: function()
				 				{
				 					this.ownerCt.ownerCt.getStore().removeAll();
				 				}
				 			}
			 			),
						
			 			// PLUS BUTTON
			 			Ext.create
					 	(
				 			"Ext.panel.Tool",
				 			
				 			{
				 				type: "plus",
				 				
				 				handler: function()
				 				{
				 					var r = Ext.create('Sbi.chart.designer.PlotbandsModel', {
					                    from: 0,
					                    to: 0,
					                    color: ''
					                });
							    	
							    	plotbandsStore.insert(plotbandsStore.data.length, r);
				 				}
				 			}
			 			)						
					],
					
					hideHeaders: false, 
					
					columns: 
					{
						items: 
						[
							 {
								dataIndex: 'from',
								flex: 1,
								layout: 'fit',
								align : 'center',
								sortable: false,
								text: LN("sbi.chartengine.axisstylepopup.plotbandParams.columnFrom"),
								
								editor: 
								{
									xtype: 'numberfield'
								}
							 }, 
						
							{
				                dataIndex: 'to',
				                flex: 1,
								layout: 'fit',
								align : 'center',
								sortable: false,
								text: LN("sbi.chartengine.axisstylepopup.plotbandParams.columnTo"),
								
								editor: 
								{
									xtype: 'numberfield'
								}
				            },
				            
				            {
				            	dataIndex: 'color',	
				            	
				                flex: 1,
								layout: 'fit',
								sortable: false,
								align : 'center',
								text: LN("sbi.chartengine.axisstylepopup.plotbandParams.columnColor"),
									
								/**
								 * What happens inside of this grid cell (textfield for color value)
								 * when we render (open) Axis style configuration popup. Property
								 * "style" of this element will set CSS 'background-color' property
								 * value to the value of color that user choose for the particular 
								 * cell when he saved it last time. This way we will take the string 
								 * value of chosen color and assign it to the background color of the
								 * cell element.
								 */
								renderer: function(colorPicked,thisField)
								{
									thisField.style = "background-color: " + colorPicked;
								},
								
								listeners:  
								{									
									click: 
									{
										fn: function(a,b,index)
										{ 													
											Ext.create
											(
												'Ext.window.Window', 
												{
												    title: LN("sbi.chartengine.configuration.gauge.axisStyle.plotbandsParameters.pickColor"),
												    height: 110,
												    width: 200,
												    resizable: false,
												    ownerCt: this, 	// bring the color picker in front
												    modal: true,		// prevent user from clicking outside this 
												    layout: 'fit',
												    
												    items: 
												    { 												        
												    	xtype: 'colorpicker',
												        value: axisStylePopupScope.getComponent("axisPlotbandsParamsFieldSet")
												        			.getComponent("plotsContainer").store.data.items[index].data.color,
												    	
												        listeners: 
											        	{
												        	select: 
											        		{
												        		fn: function(a,b)
													        	{													        		
													        		var plotbandsParamsFieldSet = axisStylePopupScope.getComponent("axisPlotbandsParamsFieldSet");
													        		var plotsContainer = plotbandsParamsFieldSet.getComponent("plotsContainer");
													        		
													        		plotsContainer.store.data.items[index].data.color = "#" + b;
													        		
													        		this.ownerCt.close();
													        		
													        		/**
													        		 * After changing/setting the color value for the field,
													        		 * update the grid which contains it so we can see changes.
													        		 */
													        		plotsContainer.getView().refresh();
													        	}
											        		}
											        	}
												    }
												}
											).show();
										}
									}
								},
								
								editor: 
								{
									xtype: 'textfield',
									readOnly: true
								}							              
			                },			                
			                
							{
								menuDisabled: true,
								sortable: false,
								flex: 1,
		  						align : 'center',
								xtype: 'actioncolumn',
								
								items: 
								[
								 	{
										icon: '/' + Sbi.chart.designer.Designer.mainContextName + '/themes/sbi_default/img/delete.gif',										
										
										handler: function(grid, rowIndex, colIndex) 
										{
											
											var store = grid.getStore();
											var item = store.getAt(rowIndex);
																						
											var from = item.get('from');
											var to = item.get('to');
											
											Ext.Msg.show({
				            					title : '',
				            					
				            					message : LN('sbi.chartengine.designer.removeplot'),
				      								
				            					icon : Ext.Msg.QUESTION,
				            					closable : false,
				            					buttons : Ext.Msg.OKCANCEL,
				            					
				            					buttonText : 
				            					{
				            						ok : LN('sbi.chartengine.generic.ok'),
				            						cancel : LN('sbi.generic.cancel')
				            					},
				            					
				            					fn : function(buttonValue, inputText, showConfig)
				            					{
				            						if (buttonValue == 'ok') 
				            						{						            							
				            							var rec = store.removeAt(rowIndex);
				            						}
				            					}
				            				});
										}
									}
							 	]
							}				            
			            ]
					},
					
					selModel: {
						selType: 'cellmodel'
					},
					
					plugins: [{
				 		ptype: 'cellediting',
				 		clicksToEdit: 1
				 	}]
				}
			);
			
			this.axisPlotbandsParamsFieldSet.add(this.plotsContainer);
			
			/**
			 * ********************************************************
			 * Axis - plotbands parameters (END)
			 * ********************************************************
			 */
		}
		
		var titleStyleColor = (this.axisData.titleStyleColor || '').replace('#', '');
		this.titleStyleColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
			fieldLabel : LN('sbi.chartengine.axisstylepopup.color'),
			emptyText: LN('sbi.chartengine.configuration.axistitlecolor.emptyText'),
			labelWidth : LABEL_WIDTH,
			value: titleStyleColor
		});
		this.titleFieldSet.add(this.titleStyleColor);

		var titleStyleFont = this.axisData.titleStyleFont;
		this.titleStyleFontComboBox = Ext.create('Sbi.chart.designer.FontCombo', {
			value: (titleStyleFont && titleStyleFont.trim() != '') ? titleStyleFont.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.font'),
		});
		this.titleFieldSet.add(this.titleStyleFontComboBox);
		
		var titleStyleFontWeigh = this.axisData.titleStyleFontWeigh;
		this.titleStyleFontWeighComboBox = Ext.create('Sbi.chart.designer.FontStyleCombo', {
			value: (titleStyleFontWeigh && titleStyleFontWeigh.trim() != '') ? titleStyleFontWeigh.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontweight'),
		});
		this.titleFieldSet.add(this.titleStyleFontWeighComboBox);
		
		var titleStyleFontSize = this.axisData.titleStyleFontSize;
		this.titleStyleFontSizeComboBox = Ext.create('Sbi.chart.designer.FontDimCombo', {
			value: (titleStyleFontSize && titleStyleFontSize.trim() != '') ? titleStyleFontSize.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontsize'),
		});
		this.titleFieldSet.add(this.titleStyleFontSizeComboBox);

		
		this.add(this.axisFieldSet);
		this.add(this.titleFieldSet);
		
		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE")
		{
			this.add(this.axisAdditionalParamsFieldSet);
			this.add(this.axisTickParamsFieldSet);
			this.add(this.axisMinorTickParamsFieldSet);
			this.add(this.axisLabelsParamsFieldSet);
			this.add(this.axisPlotbandsParamsFieldSet);	
		}		
		
		if(isYAxis) {
			this.add(this.majorgridFieldSet);
			this.add(this.minorgridFieldSet);
		}
    },
    writeConfigsAndExit: function() {
    	var isYAxis = this.isYAxis;
    	
		var styleRotate = this.styleRotateNumberField.getValue();
//		this.axisData.styleRotate = styleRotate;

		var styleAlign = this.styleAlignComboBox.getValue();
//		this.axisData.styleAlign = styleAlign;		
		
		var axisStyleColor = this.styleColor.getColor();
//		this.axisData.styleColor = axisStyleColor;

		var styleFont = this.styleFontComboBox.getValue();
//		this.axisData.styleFont = styleFont;

		var styleFontWeigh = this.styleFontWeighComboBox.getValue();
//		this.axisData.styleFontWeigh = styleFontWeigh;

		var styleFontSize = this.styleFontSizeComboBox.getValue();
//		this.axisData.styleFontSize = styleFontSize;
		
		if(this.styleOpposite) {
			var styleOpposite = this.styleOpposite.getValue();
//			this.axisData.styleOpposite = styleOpposite;
		}

		if(isYAxis) {
			var majorgridInterval = this.majorgridIntervalNumberField.getValue();
//			this.axisData.majorgridInterval = majorgridInterval;
	
			var majorgridStyleTypeline = this.majorgridStyleTypelineComboBox.getValue();
//			this.axisData.majorgridStyleTypeline = majorgridStyleTypeline;
	
			var majorgridStyleColor = this.majorgridStyleColor.getColor();
//			this.axisData.majorgridStyleColor = majorgridStyleColor;
	
			var minorgridInterval = this.minorgridIntervalNumberField.getValue();
//			this.axisData.minorgridInterval = minorgridInterval;
	
			var minorgridStyleTypeline = this.minorgridStyleTypelineComboBox.getValue();
//			this.axisData.minorgridStyleTypeline = minorgridStyleTypeline;
	
			var minorgridStyleColor = this.minorgridStyleColor.getColor();
//			this.axisData.minorgridStyleColor = minorgridStyleColor;
		}				
		
		// var titleText = this.titleTextTextField.getValue();
		// this.axisData.titleText = titleText;

		var titleStyleAlign = this.titleStyleAlignComboBox.getValue();
//		this.axisData.titleStyleAlign = titleStyleAlign;

		var titleStyleColor = this.titleStyleColor.getColor();
//		this.axisData.titleStyleColor = titleStyleColor;

		var titleStyleFont = this.titleStyleFontComboBox.getValue();
//		this.axisData.titleStyleFont = titleStyleFont;

		var titleStyleFontWeigh = this.titleStyleFontWeighComboBox.getValue();
//		this.axisData.titleStyleFontWeigh = titleStyleFontWeigh;

		var titleStyleFontSize = this.titleStyleFontSizeComboBox.getValue();
//		this.axisData.titleStyleFontSize = titleStyleFontSize;
		
		/**
		 * TODO: Check if this is ok
		 * 
		 * Danilo
		 */
		var errorMessages = "";
		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
		
		var colorPicker = Sbi.chart.designer.components.ColorPicker;
		
		if (axisStyleColor && axisStyleColor!=null)
		{
			var axisStyleColorTemp = (axisStyleColor.indexOf("#")==0) ? axisStyleColor.replace('#', '') : axisStyleColor;
			
			if (!colorPicker.validateValue(axisStyleColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.axisstylepopup.color"),
					 	LN("sbi.chartengine.axisstylepopup.axis")						
					]
				);
			}
		}
		
		if (titleStyleColor && titleStyleColor!=null)
		{
			var titleStyleColorTemp = (titleStyleColor.indexOf("#")==0) ? titleStyleColor.replace('#', '') : titleStyleColor;
			
			if (!colorPicker.validateValue(titleStyleColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.axisstylepopup.color"),
					 	LN("sbi.chartengine.axisstylepopup.title")						
					]
				);
			}
		}
		
		if (majorgridStyleColor && majorgridStyleColor!=null)
		{
			var majorgridStyleColorTemp = (majorgridStyleColor.indexOf("#")==0) ? majorgridStyleColor.replace('#', '') : majorgridStyleColor;
			
			if (!colorPicker.validateValue(majorgridStyleColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.axisstylepopup.color"),
					 	LN("sbi.chartengine.axisstylepopup.majorgrid")						
					]
				);
			}
		}
		
		if (minorgridStyleColor && minorgridStyleColor!=null)
		{
			var minorgridStyleColorTemp = (minorgridStyleColor.indexOf("#")==0) ? minorgridStyleColor.replace('#', '') : minorgridStyleColor;
			
			if (!colorPicker.validateValue(minorgridStyleColorTemp))
			{
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
					
					[
					 	LN("sbi.chartengine.axisstylepopup.color"),
					 	LN("sbi.chartengine.axisstylepopup.minorgrid")
					]
				);
			}
		}
		
		/**
		 * Validation for the GAUGE chart's values of the axis style configuration 
		 * parameters.
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		
		// ** START **
		if (chartType == "GAUGE")
		{
			var minValueYAxis = this.minValueYAxis.getValue();
			var maxValueYAxis = this.maxValueYAxis.getValue();			
			var lineColorYAxis = this.lineColor.getColor();			
			//var offsetBorderFromYAxis = this.offsetBorderFromYAxis.getValue();
			
			var lineWidthYAxis = this.lineWidthYAxis.getValue();
			var endOnTick = this.endOnTick.getValue();
			var tickPosition = this.tickPosition.getValue();
			var tickColor = this.tickColor.getColor();
			var tickPixelInterval = this.tickPixelInterval.getValue();			

			var tickWidth = this.tickWidth.getValue();
			var tickLength = this.tickLength.getValue();
			var minorTickPosition = this.minorTickPosition.getValue();
			var minorTickColor = this.minorTickColor.getColor();
			var minorTickInterval = this.minorTickInterval.getValue();
			var minorTickWidth = this.minorTickWidth.getValue();
			var minorTickLength = this.minorTickLength.getValue();
			var distanceLabelFromYAxis = this.distanceLabelFromYAxis.getValue();
			var rotationOfLabelYAxis = this.rotationOfLabelYAxis.getValue();
			
			/* **************************************************
			 * **************** VALIDATION **********************
			 * **************************************************/
			
			/**
			 * The indicator that min is bigger than max, so the plotbands cannot be examined. 
			 */
			var minBiggerThanMax = false;
			var minDefinedMaxNotDefined = false;
			var maxDefinedMinNotDefined = false;
			
			/**
			 * Maximum value as the ending point of the GAUGE chart axis must be 
			 * bigger than the minimum value as the starting point of the chart.
			 */			
			if (maxValueYAxis!=null) 
			{
				if (minValueYAxis!=null)
				{		
					/**
					 * Min and max are not mandatory, but if defined they must satisfy 
					 * criterie by which minimum value for the chart cannot be bigger
					 * than the maximum value.
					 */
					if (minValueYAxis >= maxValueYAxis)
					{
						errorMessages += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.structure.gauge.additionalParameters.maxLessThanMin"),
							
							[
								LN("sbi.chartengine.axisstylepopup.additionalParams.maxValueYAxis"),
								LN("sbi.chartengine.axisstylepopup.additionalParams.minValueYAxis"),
								LN('sbi.chartengine.axisstylepopup.additionalParams.title')
							]
						);
					
						minBiggerThanMax = true;	
					}					
				}	
				else
				{
					/**
					 * Since the Ext JS library let us not specifying the minimum value
					 * for the chart, we can leave it empty. Library treat this as value
					 * of 0 (zero).
					 */
					maxDefinedMinNotDefined = true;
				}
			}	
			else
			{	
				/**
				 * Since the nature of the library, user cannot set just minimum value for the chart.
				 */
				if (minValueYAxis!=null)
				{
					errorMessages += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.structure.gauge.additionalParameters.maxAndMinDefine"),
						
						[
							LN("sbi.chartengine.axisstylepopup.additionalParams.maxValueYAxis"),
							LN("sbi.chartengine.axisstylepopup.additionalParams.minValueYAxis"),
							LN('sbi.chartengine.axisstylepopup.additionalParams.title')
						]
					);
				
					minDefinedMaxNotDefined = true;
				}
			}
			
			/**
			 * If values for these parameters are provided by the user they must be valid (correct).
			 */
			
			/**
			 * TODO: Check if this is ok
			 * 
			 * Danilo
			 */
			if (lineColorYAxis && lineColorYAxis!=null)
			{
				var lineColorYAxisTemp = (lineColorYAxis.indexOf("#")==0) ? lineColorYAxis.replace('#', '') : lineColorYAxis;
				
				if (!colorPicker.validateValue(lineColorYAxisTemp))
				{
					errorMessages += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
						
						[
						 	LN("sbi.chartengine.axisstylepopup.additionalParams.lineColor"),
						 	LN("sbi.chartengine.axisstylepopup.additionalParams.title")							
						]
					);
				}
			}
			
			!(lineWidthYAxis!=null && lineWidthYAxis < this.lineWidthYAxis.minValue) ? lineWidthYAxis :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.additionalParams.lineWidth"),
						this.lineWidthYAxis.minValue,
						LN('sbi.chartengine.axisstylepopup.additionalParams.title')
					]
				);
			
			/**
			 * TODO: Check if this is ok
			 * 
			 * Danilo
			 */
			if (tickColor && tickColor!=null)
			{
				var tickColorTemp = (tickColor.indexOf("#")==0) ? tickColor.replace('#', '') : tickColor;
				
				if (!colorPicker.validateValue(tickColorTemp))
				{
					errorMessages += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
						
						[
							LN("sbi.chartengine.axisstylepopup.mainTickParams.tickColor"),
						 	LN("sbi.chartengine.axisstylepopup.mainTickParams.title")
						]
					);
				}
			}
			
			!(tickPixelInterval!=null && tickPixelInterval < this.tickPixelInterval.minValue) ? tickPixelInterval :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.mainTickParams.tickPixelInterval"),
						this.tickPixelInterval.minValue,
						LN('sbi.chartengine.axisstylepopup.mainTickParams.title')
					]
				);
			
			!(tickWidth!=null && tickWidth < this.tickWidth.minValue) ? tickWidth :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.mainTickParams.tickWidth"),
						this.tickWidth.minValue,
						LN('sbi.chartengine.axisstylepopup.mainTickParams.title')
					]
				);
			
			!(tickLength!=null && tickLength < this.tickLength.minValue) ? tickLength :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.mainTickParams.tickLength"),
						this.tickLength.minValue,
						LN('sbi.chartengine.axisstylepopup.mainTickParams.title')
					]
				);
			
			/**
			 * TODO: Check if this is ok
			 * 
			 * Danilo
			 */
			if (minorTickColor && minorTickColor!=null)
			{
				var minorTickColorTemp = (minorTickColor.indexOf("#")==0) ? minorTickColor.replace('#', '') : tickColor;
				
				if (!colorPicker.validateValue(minorTickColorTemp))
				{
					errorMessages += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.structure.axisStyleConfPopupOpened.colorValuesInvalid"),
						
						[

							LN("sbi.chartengine.axisstylepopup.minorTickParams.tickColor"),
						 	LN("sbi.chartengine.axisstylepopup.minorTickParams.title")
						]
					);
				}
			}
			
			!(minorTickInterval!=null && minorTickInterval < this.minorTickInterval.minValue) ? minorTickInterval :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickInterval"),
						this.minorTickInterval.minValue,
						LN('sbi.chartengine.axisstylepopup.minorTickParams.title')
					]
				);
			
			!(minorTickWidth!=null && minorTickWidth < this.minorTickWidth.minValue) ? minorTickWidth :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickWidth"),
						this.minorTickWidth.minValue,
						LN('sbi.chartengine.axisstylepopup.minorTickParams.title')
					]
				);
			
			!(minorTickLength!=null && minorTickLength < this.minorTickLength.minValue) ? minorTickLength :
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickLength"),
						this.minorTickLength.minValue,
						LN('sbi.chartengine.axisstylepopup.minorTickParams.title')
					]
				);
			
			!(distanceLabelFromYAxis!=null && distanceLabelFromYAxis < this.distanceLabelFromYAxis.minValue) ? distanceLabelFromYAxis : 
				errorMessages += Sbi.locale.sobstituteParams
				(
					LN("sbi.chartengine.validation.structure.gauge.paramsMinValue"),
					
					[
						LN("sbi.chartengine.axisstylepopup.labelParams.distanceLabelFromYAxis"),
						this.distanceLabelFromYAxis.minValue,
						LN('sbi.chartengine.axisstylepopup.labelParams.title')
					]
				);
									
			/**
			 * PLOTBANDS sub-tag of the AXIS tag
			 */
			var plotbandsStoreTemp = this.plotsContainer.getStore();
			var numberOfPlots = plotbandsStoreTemp.data.length;
						
			var fromValueForPlotbands;
			var toValueForPlotbands;
			var colorPlotband;
			
			if (numberOfPlots > 0)
			{
				var plotbandsData = plotbandsStoreTemp.data.items[0].data;
				
				var fromValueForPlotbandsArray  = new Array();
				var toValueForPlotbandsArray  = new Array();
				var colorPlotbandArray  = new Array();
				
				/**
				 * In case user did not define max value (maxValueYAxis == null), he cannot 
				 * set any plot (the 'else' block).
				 */
				if (maxValueYAxis!=null)
				{			
					if (maxDefinedMinNotDefined)
					{
						/**
						 * Since the Ext JS library let us not specifying the minimum value
						 * for the chart, we can leave it empty. Library treat this as value
						 * of 0 (zero). This way we are setting this parameter to the value
						 * of 0 (because plots need information about the minimum value when
						 * validating the parameters).
						 */
						minValueYAxis = 0;
					}
					
					for (var i=0; i<numberOfPlots; i++)
					{
						var plotData = plotbandsStoreTemp.data.items[i].data;
						
						fromValueForPlotbandsArray.push(plotData.from);
						toValueForPlotbandsArray.push(plotData.to);
						colorPlotbandArray.push(plotData.color);
						
						if (!minBiggerThanMax && !minDefinedMaxNotDefined)
						{		
							(plotData.from < minValueYAxis) ? 
								errorMessages += Sbi.locale.sobstituteParams
								(
									LN("sbi.chartengine.validation.structure.gauge.plotbandsParameters.fromLessThanMinValue"),
									
									[
										LN("sbi.chartengine.axisstylepopup.plotbandParams.columnFrom"),
										i+1,
										minValueYAxis,
										LN('sbi.chartengine.axisstylepopup.plotbandParams.title')
									]
								) : errorMessages;		
												
							(plotData.from > maxValueYAxis) ? 
								errorMessages += Sbi.locale.sobstituteParams
								(
									LN("sbi.chartengine.validation.structure.gauge.plotbandsParameters.fromMoreThanMaxValue"),
									
									[
										LN("sbi.chartengine.axisstylepopup.plotbandParams.columnFrom"),
										i+1,
										maxValueYAxis,
										LN('sbi.chartengine.axisstylepopup.plotbandParams.title')
									]
								) : errorMessages;
								
							(plotData.to < minValueYAxis) ? 
								errorMessages += Sbi.locale.sobstituteParams
								(
									LN("sbi.chartengine.validation.structure.gauge.plotbandsParameters.toLessThanMinValue"),
									
									[
										LN("sbi.chartengine.axisstylepopup.plotbandParams.columnTo"),
										i+1,
										minValueYAxis,
										LN('sbi.chartengine.axisstylepopup.plotbandParams.title')
									]
								) : errorMessages;							
						
										
							(plotData.to > maxValueYAxis) ? 
								errorMessages += Sbi.locale.sobstituteParams
								(
									LN("sbi.chartengine.validation.structure.gauge.plotbandsParameters.toMoreThanMaxValue"),
									
									[
										LN("sbi.chartengine.axisstylepopup.plotbandParams.columnTo"),
										i+1,
										maxValueYAxis,
										LN('sbi.chartengine.axisstylepopup.plotbandParams.title')
									]
								) : errorMessages;
						}					
					}
				}
				else
				{					
					errorMessages += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.structure.gauge.plotbandsParameters.maxNotDefined"),
						
						[
							LN("sbi.chartengine.axisstylepopup.additionalParams.maxValueYAxis"),
							LN('sbi.chartengine.axisstylepopup.additionalParams.title'),
							LN('sbi.chartengine.axisstylepopup.plotbandParams.title')
						]
					);
				}			
	
				fromValueForPlotbands = fromValueForPlotbandsArray;
				toValueForPlotbands = toValueForPlotbandsArray;	
				colorPlotband = colorPlotbandArray;					
			}
			else
			{				
				fromValueForPlotbands = "";
				toValueForPlotbands = "";			
				colorPlotband = "";	
			}
			
			/**
			 * If there are no error messages after validation, destroy (close)
			 * the popup. Otherwise, show new popup that will inform user about 
			 * errors (in a meanwhile, the initial one (axis style configuration
			 * popup will stay active (opened) in the background.
			 */
			if (errorMessages=="")
			{
				this.axisData.min = minValueYAxis;		
				this.axisData.max = maxValueYAxis;			
				
				this.axisData.lineColor = lineColorYAxis;				
				//this.axisData.offset = offsetBorderFromYAxis;				
				this.axisData.lineWidth = lineWidthYAxis;			
				this.axisData.endOnTickGauge = endOnTick;		
			
				this.axisData.tickPosition = tickPosition;				
				this.axisData.tickColor = tickColor;				
				this.axisData.tickPixelInterval = tickPixelInterval;				
				this.axisData.tickWidth = tickWidth;				
				this.axisData.tickLength = tickLength;				
				
				this.axisData.minorTickPosition = minorTickPosition;				
				this.axisData.minorTickColor = minorTickColor;				
				this.axisData.minorTickInterval = minorTickInterval;				
				this.axisData.minorTickWidth = minorTickWidth;				
				this.axisData.minorTickLength = minorTickLength;
				
				/**
				 * LABELS sub-tag of the AXIS tag
				 */;
				this.axisData.distance = distanceLabelFromYAxis;
				this.axisData.rotation = rotationOfLabelYAxis;
				
				this.axisData.from = fromValueForPlotbands;
				this.axisData.to = toValueForPlotbands;
				this.axisData.color = colorPlotband;
			}							
			// ** END **
		}
		
		if (errorMessages!="")
		{
			Ext.Msg.show
			(
				{
					title:	Sbi.locale.sobstituteParams
							(
									LN("sbi.chartengine.validation.structure.axisAndSeriesStyleConfigPopup.messageWarning.headerTitle"),
									
									[
										LN("sbi.chartengine.axisstylepopup.popup.title")
									]
							),
							
					message : errorMessages,
					icon : Ext.Msg.WARNING,
					closable : true,
					buttons : Ext.Msg.OK
				}
			);
		}
		
		/**
		 * In the case of the GAUGE chart only if there are no error
		 * messages we can destroy the axis style configuration popup
		 * window.
		 * 
		 * @modifiedBy: danristo (danilo.ristovski@mht.net)
		 */		
		if (errorMessages=="")
		{
			this.axisData.styleRotate = styleRotate;
			this.axisData.styleAlign = styleAlign;
			this.axisData.styleColor = axisStyleColor;
			this.axisData.styleFont = styleFont;
			this.axisData.styleFontWeigh = styleFontWeigh;
			
			this.axisData.styleFontSize = styleFontSize;
			
			if(this.styleOpposite) 
			{
				this.axisData.styleOpposite = styleOpposite;
			}
			
			if (isYAxis)
			{
				this.axisData.majorgridInterval = majorgridInterval;
				this.axisData.majorgridStyleTypeline = majorgridStyleTypeline;
				this.axisData.majorgridStyleColor = majorgridStyleColor;
				this.axisData.minorgridInterval = minorgridInterval;
				this.axisData.minorgridStyleTypeline = minorgridStyleTypeline;
				this.axisData.minorgridStyleColor = minorgridStyleColor;

			}
			
			this.axisData.titleStyleAlign = titleStyleAlign;
			this.axisData.titleStyleColor = titleStyleColor;
			this.axisData.titleStyleFont = titleStyleFont;
			this.axisData.titleStyleFontWeigh = titleStyleFontWeigh;
			this.axisData.titleStyleFontSize = titleStyleFontSize;
			
			this.destroy();
		}
    },

	items: [],
	// Cancel and Save buttons
    buttons: [{
        text: LN('sbi.generic.cancel'),
        handler: function(btn, elem2 ) {
			Ext.getCmp('axisStylePopup').destroy();
        }
    }, {
        text: LN('sbi.generic.save'),
        handler: function() {
			Ext.getCmp('axisStylePopup').writeConfigsAndExit();
        }
    }],
});