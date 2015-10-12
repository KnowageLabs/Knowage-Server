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
	title: LN('sbi.chartengine.axisstylepopup.title'),
    layout: 'border',
    bodyPadding: 5,
	floating: true,
    draggable: true,
    closable : true,
    closeAction: 'destroy',
    width: 500,
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
		this.callParent(config);
		
		var plotbandsStore = Ext.create('Sbi.chart.designer.PlotbandsStore', {});
		
		this.axisData = config.axisData;
		this.allAxisData = config.allAxisData;
		
		var isYAxis = (config.isYAxis != undefined)? config.isYAxis: false;
		this.isYAxis = isYAxis;
		
		this.axisFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: 'Axis',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115
			},
			layout: 'anchor',
			items : []
		});
		
		this.titleFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			id: 'titleFieldSetForAxis',	// (danristo :: danilo.ristovski@mht.net) 
			title: 'Title',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115
			},
			layout: 'anchor',
			items : []
		});		
		
		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE")
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
						labelWidth : 115
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
						labelWidth : 115
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
						labelWidth : 115
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
						labelWidth : 115
					},
					
					layout: 'anchor',
					items : []
				}
			);
			
			this.axisPlotbandsParamsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
//					collapsible: true,
//					collapsed: true,	// collapsed by default (when window pops up) 
					id: 'axisPlotbandsParamsFieldSet',	
					title: LN('sbi.chartengine.axisstylepopup.plotbandParams.title'),	
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : 115
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
				defaults: {anchor: '100%',
					labelAlign : 'left',
					labelWidth : 115
				},
				layout: 'anchor',
				items : []
			});
			
			this.minorgridFieldSet = Ext.create('Ext.form.FieldSet', {
				collapsible: true,
				collapsed : true,
				id: "minorGridFieldSetYAxis", // (danristo :: danilo.ristovski@mht.net) 
				title: LN('sbi.chartengine.axisstylepopup.minorgrid'),
				defaults: {anchor: '100%',
					labelAlign : 'left',
					labelWidth : 115
				},
				layout: 'anchor',
				items : []
			});
		}
		
		var styleRotate = this.axisData.styleRotate;
		this.styleRotateNumberField = Ext.create('Ext.form.field.Number', {
			fieldLabel: LN('sbi.chartengine.axisstylepopup.rotate'),
			selectOnFocus: true,
			value: styleRotate ? '' + styleRotate : '',
			maxValue: 180,
			minValue: -180,
		});
		this.axisFieldSet.add(this.styleRotateNumberField);
		
		var styleAlign = this.axisData.styleAlign;
		if(isYAxis) {
			this.styleAlignComboBox = Ext.create('Sbi.chart.designer.FontVerticalAlignCombo', {
				value: (styleAlign && styleAlign.trim() != '') ? styleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.align'),
			});
		} else {
			this.styleAlignComboBox = Ext.create('Sbi.chart.designer.FontAlignCombo', {
				value: (styleAlign && styleAlign.trim() != '') ? styleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.align'),
			});
		}
		this.axisFieldSet.add(this.styleAlignComboBox);
		
		var axisStyleColor = this.axisData.styleColor;
		this.styleColor = {
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                Ext.create('Ext.form.field.Base', {
                	id: 'styleColorField',
					fieldStyle : (axisStyleColor && axisStyleColor.trim() != '') ? 
						'background-image: none; background-color: ' + axisStyleColor.trim() : '',
                    fieldLabel : LN('sbi.chartengine.axisstylepopup.color'),
					labelWidth : 115,
                    readOnly : true,
					flex: 15,
				
					getStyle: function() {
						return this.getFieldStyle( );
					}
                }), {
                    xtype : 'button',
					layout : 'hbox',
                    menu : Ext.create('Ext.menu.ColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-image: none; background-color: #' + selColor;
                                Ext.getCmp('styleColorField').setFieldStyle(style);
                            }
                        }
                    }),
					flex: 1                
                }
			],
			getColor: function(){
				var styleColor = this.items[0].getStyle();
				var indexOfSharp = styleColor.indexOf('#');
				styleColor = styleColor.substring(indexOfSharp);
				
				return styleColor;
			}
		};
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
		 * The 'opposite' parameter is enabled only when we have the SCATTER chart type
		 * (danristo :: danilo.ristovski@mht.net) 
		 */		
		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "SCATTER")
		{
			this.styleOpposite = Ext.create
	    	(
				{
			        xtype: 'checkboxfield',
			        id: 'oppositeAxis',
			        value: this.axisData.styleOpposite,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.opposite"), 
			    }	
	    	);
			
			this.axisFieldSet.add(this.styleOpposite);
		}		
		
		if(isYAxis) {
			var majorgridInterval = '' + this.axisData.majorgridInterval;
			this.majorgridIntervalNumberField = Ext.create('Ext.form.field.Number', {
				fieldLabel: LN('sbi.chartengine.axisstylepopup.interval'),
				selectOnFocus: true,
				value: (majorgridInterval && majorgridInterval.trim() != '') ? majorgridInterval.trim() : '',
				minValue: 0,
			});
			this.majorgridFieldSet.add(this.majorgridIntervalNumberField);
			
			var majorgridStyleTypeline = this.axisData.majorgridStyleTypeline;
			this.majorgridStyleTypelineComboBox = Ext.create('Sbi.chart.designer.TypeLineCombo', {
				value: (majorgridStyleTypeline && majorgridStyleTypeline.trim() != '') ? majorgridStyleTypeline.trim() : '',
				fieldLabel : LN('sbi.chartengine.axisstylepopup.typeline'),
			});
			this.majorgridFieldSet.add(this.majorgridStyleTypelineComboBox);
			
			var majorgridStyleColor = this.axisData.majorgridStyleColor;
			this.majorgridStyleColor = {
				xtype : 'fieldcontainer',
	            layout : 'hbox',
	            items: [
	                Ext.create('Ext.form.field.Base', {
	                	id: 'majorgridStyleColorColorField',
						fieldStyle : (majorgridStyleColor && majorgridStyleColor.trim() != '') ? 
							'background-image: none; background-color: ' + majorgridStyleColor.trim() : '',
	                    fieldLabel : LN('sbi.chartengine.axisstylepopup.color'),
						labelWidth : 115,
	                    readOnly : true,
						flex: 15,
					
						getStyle: function() {
							return this.getFieldStyle( );
						}
	                }), {
	                    xtype : 'button',
						layout : 'hbox',
	                    menu : Ext.create('Ext.menu.ColorPicker',{
	                        listeners : {
	                            select : function(picker, selColor) {
	                                var style = 'background-image: none; background-color: #' + selColor;
	                                Ext.getCmp('majorgridStyleColorColorField').setFieldStyle(style);
	                            }
	                        }
	                    }),
						flex: 1                
	                }
				],
				getColor: function(){
					var styleColor = this.items[0].getStyle();
					var indexOfSharp = styleColor.indexOf('#');
					styleColor = styleColor.substring(indexOfSharp);
					
					return styleColor;
				}
			};
			this.majorgridFieldSet.add(this.majorgridStyleColor);
			
			var minorgridInterval = '' + this.axisData.minorgridInterval;
			this.minorgridIntervalNumberField = Ext.create('Ext.form.field.Number', {
				fieldLabel: LN('sbi.chartengine.axisstylepopup.interval'),
				selectOnFocus: true,
				value: (minorgridInterval && minorgridInterval.trim() != '') ? minorgridInterval.trim() : '',
				minValue: 0,
			});
			this.minorgridFieldSet.add(this.minorgridIntervalNumberField);
			
			var minorgridStyleTypeline = this.axisData.minorgridStyleTypeline;
			this.minorgridStyleTypelineComboBox = Ext.create('Sbi.chart.designer.TypeLineCombo', {
				value: (minorgridStyleTypeline && minorgridStyleTypeline.trim() != '') ? minorgridStyleTypeline.trim() : '',
				fieldLabel : LN('sbi.chartengine.axisstylepopup.typeline'),
			});
			this.minorgridFieldSet.add(this.minorgridStyleTypelineComboBox);
			
			var minorgridStyleColor = this.axisData.minorgridStyleColor;
			this.minorgridStyleColor = {
				xtype : 'fieldcontainer',
	            layout : 'hbox',
	            items: [
	                Ext.create('Ext.form.field.Base', {
	                	id: 'minorgridStyleColorColorField',
						fieldStyle : (minorgridStyleColor && minorgridStyleColor.trim() != '') ? 
							'background-image: none; background-color: ' + minorgridStyleColor.trim() : '',
	                    fieldLabel : LN('sbi.chartengine.axisstylepopup.color'),
						labelWidth : 115,
	                    readOnly : true,
						flex: 15,
					
						getStyle: function() {
							return this.getFieldStyle( );
						}
	                }), {
	                    xtype : 'button',
						layout : 'hbox',
	                    menu : Ext.create('Ext.menu.ColorPicker',{
	                        listeners : {
	                            select : function(picker, selColor) {
	                                var style = 'background-image: none; background-color: #' + selColor;
	                                Ext.getCmp('minorgridStyleColorColorField').setFieldStyle(style);
	                            }
	                        }
	                    }),
						flex: 1                
	                }
				],
				getColor: function(){
					var styleColor = this.items[0].getStyle();
					var indexOfSharp = styleColor.indexOf('#');
					styleColor = styleColor.substring(indexOfSharp);
					
					return styleColor;
				}
			};
			this.minorgridFieldSet.add(this.minorgridStyleColor);
		}
				
		var titleStyleAlign = this.axisData.titleStyleAlign;
		if(isYAxis) {
			this.titleStyleAlignComboBox = Ext.create('Sbi.chart.designer.FontVerticalAlignCombo', {
				value: (titleStyleAlign && titleStyleAlign.trim() != '') ? titleStyleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.align'),
			});
		} else {
			this.titleStyleAlignComboBox = Ext.create('Sbi.chart.designer.FontAlignCombo', {
				value: (titleStyleAlign && titleStyleAlign.trim() != '') ? titleStyleAlign.trim() : '',
						fieldLabel : LN('sbi.chartengine.axisstylepopup.align'),
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
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.minValueYAxis") 
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
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.maxValueYAxis") 
			    }	
	    	);
			
			/**
			 * LINE COLOR
			 * 		Description: Color of the speedometer line
			 */
			var lineColor = this.axisData.lineColor;
			
			this.lineColor = 
			{
				xtype : 'fieldcontainer',
	            layout : 'hbox',
	            
	            items: 
            	[
	                Ext.create
	                (
                		'Ext.form.field.Base', 
                		
                		{
		                	id: 'yAxisLineColor',
							fieldStyle : (lineColor && lineColor.trim() != '') ? 
								'background-image: none; background-color: ' + lineColor.trim() : '',
		                    fieldLabel : LN("sbi.chartengine.axisstylepopup.additionalParams.lineColor"),
							labelWidth : 115,
		                    readOnly : true,
							flex: 15,
						
							getStyle: function() 
							{
								return this.getFieldStyle();
							}
		                }
            		), 
            		
            		{
	                    xtype : 'button',
						layout : 'hbox',
						
	                    menu : Ext.create
	                    (
                			'Ext.menu.ColorPicker',
                			
                			{	                        
                				listeners : 
                				{
		                            select : function(picker, selColor) 
		                            {
		                                var style = 'background-image: none; background-color: #' + selColor;
		                                Ext.getCmp('yAxisLineColor').setFieldStyle(style);
		                            }
                				}	
                			}
            			),
						
            			flex: 1                
	                }
				],
				
				getColor: function()
				{
					var styleColor = this.items[0].getStyle();
					var indexOfSharp = styleColor.indexOf('#');
					styleColor = styleColor.substring(indexOfSharp);
					
					return styleColor;
				}
			};
			
			/**
			 * OFFSET
			 * 
			 * 		Description: 	
			 * 			Distance (in pixels) between the speedometer border and the axes (or �speed� line). 
			 * 			If positive, the line will be outside the speedometer. If negative the axes will be 
			 * 			closer to the center
			 */
			this.offsetBorderFromYAxis = Ext.create
	    	(
				{
			        xtype: 'numberfield',
			        id: 'offsetBorderFromYAxis',
			        value: this.axisData.offset,
			        maxValue: 60,
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.offset") 
			    }	
	    	);
			
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.lineWidth") 
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
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.additionalParams.endOnTick")
			    }	
	    	);
			
			this.axisAdditionalParamsFieldSet.add(this.minValueYAxis);
			this.axisAdditionalParamsFieldSet.add(this.maxValueYAxis);
			this.axisAdditionalParamsFieldSet.add(this.lineColor);
			this.axisAdditionalParamsFieldSet.add(this.offsetBorderFromYAxis);
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
			var tickColor = this.axisData.tickColor;
			
			this.tickColor = 
			{
				xtype : 'fieldcontainer',
	            layout : 'hbox',
	            
	            items: 
            	[
	                Ext.create
	                (
                		'Ext.form.field.Base', 
                		
                		{
		                	id: 'tickColorYAxis',
							fieldStyle : (tickColor && tickColor.trim() != '') ? 
								'background-image: none; background-color: ' + tickColor.trim() : '',
		                    fieldLabel : LN("sbi.chartengine.axisstylepopup.mainTickParams.tickColor"),
							labelWidth : 115,
		                    readOnly : true,
							flex: 15,
						
							getStyle: function() 
							{
								return this.getFieldStyle();
							}
		                }
            		), 
            		
            		{
	                    xtype : 'button',
						layout : 'hbox',
						
	                    menu : Ext.create
	                    (
                			'Ext.menu.ColorPicker',
                			
                			{	                        
                				listeners : 
                				{
		                            select : function(picker, selColor) 
		                            {
		                                var style = 'background-image: none; background-color: #' + selColor;
		                                Ext.getCmp('tickColorYAxis').setFieldStyle(style);
		                            }
                				}	
                			}
            			),
						
            			flex: 1                
	                }
				],
				
				getColor: function()
				{
					var styleColor = this.items[0].getStyle();
					var indexOfSharp = styleColor.indexOf('#');
					styleColor = styleColor.substring(indexOfSharp);
					
					return styleColor;
				}
			};
			
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.mainTickParams.tickPixelInterval")
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.mainTickParams.tickWidth") 
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.mainTickParams.tickLength") 
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
			var minorTickColor = this.axisData.minorTickColor;
			
			this.minorTickColor = 
			{
				xtype : 'fieldcontainer',
	            layout : 'hbox',
	            
	            items: 
            	[
	                Ext.create
	                (
                		'Ext.form.field.Base', 
                		
                		{
		                	id: 'minorTickColor',
							fieldStyle : (minorTickColor && minorTickColor.trim() != '') ? 
								'background-image: none; background-color: ' + minorTickColor.trim() : '',
		                    fieldLabel : LN("sbi.chartengine.axisstylepopup.minorTickParams.tickColor"),
							labelWidth : 115,
		                    readOnly : true,
							flex: 15,
						
							getStyle: function() 
							{
								return this.getFieldStyle();
							}
		                }
            		), 
            		
            		{
	                    xtype : 'button',
						layout : 'hbox',
						
	                    menu : Ext.create
	                    (
                			'Ext.menu.ColorPicker',
                			
                			{	                        
                				listeners : 
                				{
		                            select : function(picker, selColor) 
		                            {
		                                var style = 'background-image: none; background-color: #' + selColor;
		                                Ext.getCmp('minorTickColor').setFieldStyle(style);
		                            }
                				}	
                			}
            			),
						
            			flex: 1                
	                }
				],
				
				getColor: function()
				{
					var styleColor = this.items[0].getStyle();
					var indexOfSharp = styleColor.indexOf('#');
					styleColor = styleColor.substring(indexOfSharp);
					
					return styleColor;
				}
			};
			
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickInterval") 
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickWidth")
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
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.minorTickParams.minorTickLength")
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
			        minValue: 0,
			        value: this.axisData.distance,
			        labelSeparator: '',			        
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.labelParams.distanceLabelFromYAxis") 
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
			        minValue: 0,
			        maxValue: 359,
			        value: this.axisData.rotation,
			        labelSeparator: '',			        
			        fieldLabel: LN("sbi.chartengine.axisstylepopup.labelParams.rotationOfLabelYAxis") 
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
//				console.log(this.axisData);
//				console.log(plotbandsStore);
//				
//				console.log(this.axisData.from);
//				console.log(this.axisData.to);
//				console.log(this.axisData.color);
				
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
			
			
			this.colorPickerContainer = Ext.create('Sbi.chart.designer.ColorPickerContainer',{
	    		viewModel: null,
	    		customLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
	       		fieldBind: '{configModel.backgroundColor}',
	       	});
					
			var axisStylePopupScope = this;
			
			this.plotsContainer = Ext.create
			(
				"Sbi.chart.designer.ChartColumnsContainer", 
				
				{				
					//minHeight: 300,
					//height: 300,
					flex: 1,
									
					id: "plotsContainer",
					store: plotbandsStore, // IMPLEMENT YOUR STORE
					
					title:
					{
						hidden: true //???
					},
					
					tools:
					[						
						// PLUS BUTTON
						{
						    type:'plus',
//						    tooltip: "AAA",
						    
						    flex: 1,							
							
							// IMPLEMENT YOUR HANDLER
						    handler: function(event, toolEl, panelHeader) 
						    {									    							    	
						    	var r = Ext.create('Sbi.chart.designer.PlotbandsModel', {
				                    from: 0,
				                    to: 0,
				                    color: ''
				                });
						    	
//						    	console.log(plotbandsStore.data.length);
						    	
//						    	var currentNumberOfItemsInArray = plotbandsStore.data.length;
						    	
//						    	if (currentNumberOfItemsInArray === 0)
//						    	{
//						    		var r = Ext.create('Sbi.chart.designer.PlotbandsModel', {
//					                    from: 0,
//					                    to: 0,
//					                    color: ''
//					                });
//						    	}
						    	
						    	plotbandsStore.insert(plotbandsStore.data.length, r);
						    }							
						}						
					],
					
					hideHeaders: false, // ????
					
					//  USE YOUR COLUMNS
					columns: 
					{
						items: 
						[
							 {
								dataIndex: 'from',
								flex: 1,
								layout: 'fit',
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
								text: LN("sbi.chartengine.axisstylepopup.plotbandParams.columnColor"),
								
								listeners:  
								{
									click: 
									{
										fn: function(ad, gg, index)
										{ 
											console.log('click el'); 
											
											Ext.create
											(
												'Ext.window.Window', 
												{
												    title: 'Choose the color for the plot',	// TODO: LN()
												    height: 110,
												    width: 200,
												    resizable: false,
												    ownerCt : this, 	// bring the color picker in front
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
												        		fn: function(a, b, cc)
													        	{
													        		console.log("Color picker window");
													        		
													        		var plotbandsParamsFieldSet = axisStylePopupScope.getComponent("axisPlotbandsParamsFieldSet");
													        		var plotsContainer = plotbandsParamsFieldSet.getComponent("plotsContainer");
													        		
													        		plotsContainer.store.data.items[index].data.color = "#" + b;
													        		
													        		plotsContainer.store.commitChanges();
													        		plotsContainer.reconfigure();
													        		
													        		this.ownerCt.close();
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
										icon: '/athena/themes/sbi_default/img/delete.gif',
		//								tooltip: LN('sbi.chartengine.columnscontainer.tooltip.removecolumn'),											
										
										handler: function(grid, rowIndex, colIndex) 
										{
											
											var store = grid.getStore();
											var item = store.getAt(rowIndex);
											
//											console.log(item);
											
											var from = item.get('from');
											var to = item.get('to');
											
											Ext.Msg.show({
				            					title : '',
				            					
//				            					message : Sbi.locale.sobstituteParams(
//				      								LN('sbi.chartengine.designer.removeplotParamsremoveplot'), 
//				      								[from, to]),
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
//				            							console.log(store);
//				            							console.log(rowIndex);
//				            							console.log(store.getAt(rowIndex));
				            							
				            							var rec = store.removeAt(rowIndex);
//				            							plotbandsStore.reload();
//				            							console.log(store);
				            							//grid.reconfigure(plotbandsStore);
				            						}
				            					}
				            				});
										}
									}
							 	]
							}				            
			            ]
					},
					
					selModel: 
					{
						selType: 'cellmodel'
					},
					
					plugins: 
					[
					 	{
					 		ptype: 'cellediting',
					 		clicksToEdit: 1
					 	}
				 	]
				}
			);
			
			this.axisPlotbandsParamsFieldSet.add(this.plotsContainer);
			
			/**
			 * ********************************************************
			 * Axis - plotbands parameters (END)
			 * ********************************************************
			 */
		}
		
		var titleStyleColor = this.axisData.titleStyleColor;
		this.titleStyleColor = {
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                Ext.create('Ext.form.field.Base', {
                	id: 'titleStyleColorColorField',
					fieldStyle : (titleStyleColor && titleStyleColor.trim() != '') ? 
						'background-image: none; background-color: ' + titleStyleColor.trim() : '',
                    fieldLabel : LN('sbi.chartengine.axisstylepopup.color'),
					labelWidth : 115,
                    readOnly : true,
					flex: 15,
				
					getStyle: function() {
						return this.getFieldStyle( );
					}
                }), {
                    xtype : 'button',
					layout : 'hbox',
                    menu : Ext.create('Ext.menu.ColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-image: none; background-color: #' + selColor;
                                Ext.getCmp('titleStyleColorColorField').setFieldStyle(style);
                            }
                        }
                    }),
					flex: 1                
                }
			],
			getColor: function(){
				var styleColor = this.items[0].getStyle();
				var indexOfSharp = styleColor.indexOf('#');
				styleColor = styleColor.substring(indexOfSharp);
				
				return styleColor;
			}
		};
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
		this.axisData.styleRotate = styleRotate;

		var styleAlign = this.styleAlignComboBox.getValue();
		this.axisData.styleAlign = styleAlign;

		var axisStyleColor = this.styleColor.getColor();
		this.axisData.styleColor = axisStyleColor;

		var styleFont = this.styleFontComboBox.getValue();
		this.axisData.styleFont = styleFont;

		var styleFontWeigh = this.styleFontWeighComboBox.getValue();
		this.axisData.styleFontWeigh = styleFontWeigh;

		var styleFontSize = this.styleFontSizeComboBox.getValue();
		this.axisData.styleFontSize = styleFontSize;
		
		if(this.styleOpposite) {
			var styleOpposite = this.styleOpposite.getValue();
			this.axisData.styleOpposite = styleOpposite;
		}

		if(isYAxis) {
			var majorgridInterval = this.majorgridIntervalNumberField.getValue();
			this.axisData.majorgridInterval = majorgridInterval;
	
			var majorgridStyleTypeline = this.majorgridStyleTypelineComboBox.getValue();
			this.axisData.majorgridStyleTypeline = majorgridStyleTypeline;
	
			var majorgridStyleColor = this.majorgridStyleColor.getColor();
			this.axisData.majorgridStyleColor = majorgridStyleColor;
	
			var minorgridInterval = this.minorgridIntervalNumberField.getValue();
			this.axisData.minorgridInterval = minorgridInterval;
	
			var minorgridStyleTypeline = this.minorgridStyleTypelineComboBox.getValue();
			this.axisData.minorgridStyleTypeline = minorgridStyleTypeline;
	
			var minorgridStyleColor = this.minorgridStyleColor.getColor();
			this.axisData.minorgridStyleColor = minorgridStyleColor;
		}
		
		// ** START **
		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE")
		{
			var minValueYAxis = this.minValueYAxis.getValue();
			this.axisData.min = minValueYAxis;
			
			var maxValueYAxis = this.maxValueYAxis.getValue();
			this.axisData.max = maxValueYAxis;	
			
			var lineColorYAxis = this.lineColor.getColor();
			this.axisData.lineColor = lineColorYAxis;
			
			var offsetBorderFromYAxis = this.offsetBorderFromYAxis.getValue();
			console.log(offsetBorderFromYAxis);
			if (offsetBorderFromYAxis <= 60)
				this.axisData.offset = offsetBorderFromYAxis;
			else
				this.axisData.offset = 60;
			
			var lineWidthYAxis = this.lineWidthYAxis.getValue();
			this.axisData.lineWidth = lineWidthYAxis;
			
			var endOnTick = this.endOnTick.getValue();
			this.axisData.endOnTickGauge = endOnTick;
			
			var tickPosition = this.tickPosition.getValue();
			this.axisData.tickPosition = tickPosition;
			
			var tickColor = this.tickColor.getColor();
			this.axisData.tickColor = tickColor;
			
			var tickPixelInterval = this.tickPixelInterval.getValue();
			this.axisData.tickPixelInterval = tickPixelInterval;
			
			var tickWidth = this.tickWidth.getValue();
			this.axisData.tickWidth = tickWidth;
			
			var tickLength = this.tickLength.getValue();
			this.axisData.tickLength = tickLength;
			
			var minorTickPosition = this.minorTickPosition.getValue();
			this.axisData.minorTickPosition = minorTickPosition;
			
			var minorTickColor = this.minorTickColor.getColor();
			this.axisData.minorTickColor = minorTickColor;
			
			var minorTickInterval = this.minorTickInterval.getValue();
			this.axisData.minorTickInterval = minorTickInterval;
			
			var minorTickWidth = this.minorTickWidth.getValue();
			this.axisData.minorTickWidth = minorTickWidth;
			
			var minorTickLength = this.minorTickLength.getValue();
			this.axisData.minorTickLength = minorTickLength;
			
			/**
			 * LABELS sub-tag of the AXIS tag
			 */
			var distanceLabelFromYAxis = this.distanceLabelFromYAxis.getValue();
			this.axisData.distance = distanceLabelFromYAxis;
			
			var rotationOfLabelYAxis = this.rotationOfLabelYAxis.getValue();
			this.axisData.rotation = rotationOfLabelYAxis;
			
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
				
				var idValueOfPlotArray = new Array();
				var fromValueForPlotbandsArray  = new Array();
				var toValueForPlotbandsArray  = new Array();
				var colorPlotbandArray  = new Array();
				
				for (var i=0; i<numberOfPlots; i++)
				{
					var plotData = plotbandsStoreTemp.data.items[i].data;
					
					idValueOfPlotArray.push(plotData.idPlot);
					fromValueForPlotbandsArray.push(plotData.from);
					toValueForPlotbandsArray.push(plotData.to);
					colorPlotbandArray.push(plotData.color);
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
			
			this.axisData.from = fromValueForPlotbands;
			this.axisData.to = toValueForPlotbands;
			this.axisData.color = colorPlotband;
			
//			console.log(this.axisData);
			
			// ** END **
		}		
		
		// var titleText = this.titleTextTextField.getValue();
		// this.axisData.titleText = titleText;

		var titleStyleAlign = this.titleStyleAlignComboBox.getValue();
		this.axisData.titleStyleAlign = titleStyleAlign;

		var titleStyleColor = this.titleStyleColor.getColor();
		this.axisData.titleStyleColor = titleStyleColor;

		var titleStyleFont = this.titleStyleFontComboBox.getValue();
		this.axisData.titleStyleFont = titleStyleFont;

		var titleStyleFontWeigh = this.titleStyleFontWeighComboBox.getValue();
		this.axisData.titleStyleFontWeigh = titleStyleFontWeigh;

		var titleStyleFontSize = this.titleStyleFontSizeComboBox.getValue();
		this.axisData.titleStyleFontSize = titleStyleFontSize;
		
		this.destroy();
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