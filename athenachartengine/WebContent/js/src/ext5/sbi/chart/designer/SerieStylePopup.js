Ext.define('Sbi.chart.designer.SerieStylePopup', {
	extend: 'Ext.form.Panel',
	require: [
	    'Sbi.chart.designer.FontCombo',
	    'Sbi.chart.designer.FontStyleCombo',
	    'Sbi.chart.designer.FontDimCombo',
	    'Sbi.chart.designer.FontAlignCombo'
	],
	
	id: 'serieStylePopup',
    title: LN('sbi.chartengine.designer.seriesstyleconf'),
    layout: 'border',
    bodyPadding: 5,
	floating: true,
    draggable: true,
    closable : true,
    closeAction: 'destroy',
    width: 500,
    modal: true,
	config: {
		store: '',
		rowIndex: ''
	},
	
	/* * * * * * * Internal components * * * * * * * */
	serieFieldSet: null,
	tooltipFieldSet: null,
	
	serieNameTextField: null,
	serieTypesComboBox: null,
	serieOrderComboBox: null,
	serieColorPicker: null,
	serieShowValue: null,
	seriePrecisionNumberField: null,
	seriePrefixCharTextField: null,
	seriePostfixCharTextField: null,
	
	tooltipTemplateHtml: null,
	tooltipColor: null,
	tooltipBackgroundColor: null,
	tooltipAlignComboBox: null,
	tooltipFontComboBox: null,
	tooltipFontWeightStylesComboBox: null,
	tooltipFontSizeComboBox: null,
	/* * * * * * * END Internal components * * * * * * * */

	layout: 'anchor',
    defaults: {
        anchor: '100%',
    },

    // The fields
    defaultType: 'textfield',
	constructor: function(config) {
		this.callParent(config);		
		
		store = config.store,
		
		rowIndex = config.rowIndex;
		var dataAtRow = store.getAt(rowIndex);	
			
		this.serieFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: LN('sbi.chartengine.designer.series'),
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115
			},
			layout: 'anchor',
			items : []
		});
			
		this.tooltipFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: LN('sbi.chartengine.designer.tooltip'),
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115,
			},
			layout: 'anchor',
			items : []
		});
		
		/**
		 * Name of the current chart type.
		 * (danilo.ristovski@mht.net)
		 */		
		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
		
		/**
		 * Additional elements and functionalities inside the Serie style popup window
		 * for the GAUGE chart type.
		 * (danilo.ristovski@mht.net)
		 */
		if (chartType == "GAUGE")
		{
			/**
			 * DIAL field set for the GAUGE chart type
			 */
			this.dialFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					// TODO: uncomment
//					collapsed: true,
					title: "Dial",	// TODO: LN()
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : 115,
					},
					
					layout: 'anchor',
					items : []
				}
			);
			
			/**
			 * DATA_LABELS field set for the GAUGE chart type
			 */
			this.dataLabelsFieldSet = Ext.create
			(
				'Ext.form.FieldSet', 
				
				{
					collapsible: true,
					// TODO: uncomment
//					collapsed: true,
					title: "Data labels",	// TODO: LN()
					
					defaults: 
					{
						anchor: '100%',
						labelAlign : 'left',
						labelWidth : 115,
					},
					
					layout: 'anchor',
					items : []
				}
			);
		
			/**
			 * Variables (GUI elements) for the DIAL and DATA LABELS of the GAUGE chart
			 */
			
			/**
			 * DIAL: 
			 * 		backgroundColor
			 * 			- color of the indicator
			 */
			var backgroundColorDial = dataAtRow.get('backgroundColorDial');
			
			this.backgroundColorDial = {
				xtype : 'fieldcontainer',
				layout : 'hbox',
				items: [
					Ext.create('Ext.form.field.Base', {
						id : 'backgroundColorDial',
						fieldStyle : (backgroundColorDial && backgroundColorDial.trim() != '') ? 
							'background-image: none; background-color: ' + backgroundColorDial.trim() : '',
						fieldLabel : LN('sbi.chartengine.designer.color'),
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
									var style = 'background-image: none;background-color: #' + selColor;
									
									Ext.getCmp('backgroundColorDial').setFieldStyle(style);
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
		
			this.dialFieldSet.add(this.backgroundColorDial);
			
			/**
			 * DATA LABELS
			 * 		y 
			 * 			- Y position of the panel
			 */	
			
			this.yPositionDataLabels = Ext.create
	    	(
				{
			        xtype: 'numberfield',
			        id: 'yPositionDataLabels',
			        value: dataAtRow.get('yPositionDataLabels'),
			        fieldLabel: "Y position" // TODO: LN()
			    }	
	    	);
			
			/**
			 * DATA LABELS: 
			 * 		color
			 * 			- panel background color
			 */
			var colorDataLabels = dataAtRow.get('colorDataLabels');
			
			this.colorDataLabels = {
				xtype : 'fieldcontainer',
				layout : 'hbox',
				items: [
					Ext.create('Ext.form.field.Base', {
						id : 'colorDataLabels',
						fieldStyle : (colorDataLabels && colorDataLabels.trim() != '') ? 
							'background-image: none; background-color: ' + colorDataLabels.trim() : '',
						fieldLabel : LN('sbi.chartengine.designer.color'),
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
									var style = 'background-image: none;background-color: #' + selColor;
									
									Ext.getCmp('colorDataLabels').setFieldStyle(style);
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
			
			this.formatDataLabels = Ext.create
	    	(
				{
			        xtype: 'textfield',
			        id: 'formatDataLabels',
			        value: dataAtRow.get('formatDataLabels'),
			        fieldLabel: "Format" // TODO: LN()
			    }	
	    	);
			
			this.dataLabelsFieldSet.add(this.yPositionDataLabels);
			this.dataLabelsFieldSet.add(this.colorDataLabels);
			this.dataLabelsFieldSet.add(this.formatDataLabels);
		}
		
		/* * * * * * * * * * SERIE FIELDS  * * * * * *  * * * * */
		var serieName = dataAtRow.get('axisName');
		this.serieNameTextField = Ext.create('Ext.form.field.Text', {
			name: 'serieName',
			value: (serieName && serieName.trim() != '') ? serieName.trim() : '',
			fieldLabel: LN('sbi.generic.name'),
			selectOnFocus: true,
			allowBlank: true 
		});
		this.serieFieldSet.add(this.serieNameTextField);
	
		var serieType = null;
		var serieTypes = null;
		
		if(chartType.toUpperCase() == 'PIE') {
			serieType = dataAtRow.get('serieType') && dataAtRow.get('serieType').toUpperCase() == 'PIE'? dataAtRow.get('serieType') : '';
			serieTypes = [
				{name: LN('sbi.chartengine.designer.charttype.notype'), value:''},
				{name: LN('sbi.chartengine.designer.charttype.pie'), value:'pie'} 
			];
		} else {
			serieType = dataAtRow.get('serieType') && dataAtRow.get('serieType').toUpperCase() != 'PIE'? dataAtRow.get('serieType') : '';
			serieTypes = [
				{name: LN('sbi.chartengine.designer.charttype.notype'), value:''},
				{name: LN('sbi.chartengine.designer.charttype.bar'), value:'bar'},
				{name: LN('sbi.chartengine.designer.charttype.line'), value:'line'},
				{name: LN('sbi.chartengine.designer.charttype.area'), value:'area'},
            ];
		}
		
		this.serieTypesComboBox = Ext.create('Ext.form.ComboBox', {
			store: {
				store: 'array',
				fields: ['name', 'value'],
				data: serieTypes
			},
			value: (serieType && serieType.trim() != '') ? serieType.trim() : '',
			valueField: 'value',
			displayField: 'name',
			fieldLabel : LN('sbi.chartengine.designer.seriestype'),
		});				
		
		/**
		 * Disable combo box for the series type of the serie item (that we picked
		 * and put inside the Y-axis (serie) panel) for these chart types (since we
		 * do not need them). Even there are numerous chart types that do not need this
		 * option (this combo box), but we will just specify those following chart types 
		 * since for those we have the option of changing the serie configuration for 
		 * separate serie items inside.
		 * Y-axis panel(s)
		 * (danilo.ristovski@mht.net)
		 */
		if (chartType == "CHORD" || chartType == "GAUGE" || chartType == "PIE" 
				|| chartType == "RADAR" || chartType == "SCATTER")
		{
			this.serieTypesComboBox.disable();
		}	
		
		this.serieFieldSet.add(this.serieTypesComboBox);		
		
		var serieOrder = dataAtRow.get('serieOrderType');
		this.serieOrderComboBox = Ext.create('Sbi.chart.designer.SeriesOrderCombo', {
			value: (serieOrder && serieOrder.trim() != '') ? serieOrder.trim() : '',
		});
		this.serieFieldSet.add(this.serieOrderComboBox);
				
		var serieColor = dataAtRow.get('serieColor');
		this.serieColorPicker = {
			xtype : 'fieldcontainer',
			layout : 'hbox',
			items: [
				Ext.create('Ext.form.field.Base', {
					id : 'serieColorField',
					fieldStyle : (serieColor && serieColor.trim() != '') ? 
						'background-image: none; background-color: ' + serieColor.trim() : '',
					fieldLabel : LN('sbi.chartengine.designer.color'),
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
								var style = 'background-image: none;background-color: #' + selColor;
								
								Ext.getCmp('serieColorField').setFieldStyle(style);
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
		this.serieFieldSet.add(this.serieColorPicker);
		
		var showValue = dataAtRow.get('serieShowValue');
		this.serieShowValue = Ext.create('Ext.form.field.Checkbox',{
			checked: (showValue != undefined) ? showValue: true,
			labelSeparator: '',
			fieldLabel: LN('sbi.chartengine.designer.showvalue'),
		});
		this.serieFieldSet.add(this.serieShowValue);
		
		var seriePrecision = dataAtRow.get('seriePrecision');
		this.seriePrecisionNumberField = Ext.create('Ext.form.field.Number', {
			fieldLabel: LN('sbi.chartengine.designer.precision'),
			selectOnFocus: true,
			value: seriePrecision ? seriePrecision : '',
			maxValue: 10,
			minValue: 0
		});
		this.serieFieldSet.add(this.seriePrecisionNumberField);
		
		var prefixChar = dataAtRow.get('seriePrefixChar');
		this.seriePrefixCharTextField = Ext.create('Ext.form.field.Text', {
			name: 'name',
			value: (prefixChar && prefixChar.trim() != '') ? prefixChar.trim() : '',
			fieldLabel: LN('sbi.chartengine.designer.prefixtext'),
			selectOnFocus: true,
			allowBlank: true 
		});
		this.serieFieldSet.add(this.seriePrefixCharTextField);
		
		var postfixChar = dataAtRow.get('seriePostfixChar'); 
		this.seriePostfixCharTextField = Ext.create('Ext.form.field.Text', {
			name: 'name',
			value: (postfixChar && postfixChar.trim() != '') ? postfixChar.trim() : '',
			fieldLabel: LN('sbi.chartengine.designer.postfixtext'),
			selectOnFocus: true,
			allowBlank: true 
		});
		this.serieFieldSet.add(this.seriePostfixCharTextField);
				
		/* * * * * * * * * * TOOTLTIP FIELDS  * * * * * *  * * * * */
		var templateHtml = dataAtRow.get('serieTooltipTemplateHtml');
		this.tooltipTemplateHtml = Ext.create('Ext.form.field.TextArea',{
			grow      : true,
			name      : 'tooltipTemplateHtml',
			value: (templateHtml && templateHtml.trim() != '') ? templateHtml.trim() : '',
			fieldLabel: LN('sbi.chartengine.designer.templatehtml'),
			anchor    : '100%'
		});
		this.tooltipFieldSet.add(this.tooltipTemplateHtml);
		
		var serieTooltipColor = dataAtRow.get('serieTooltipColor');
		this.tooltipColor = {
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                Ext.create('Ext.form.field.Base', {
                    id : 'tooltipColorField',
					fieldStyle : (serieTooltipColor && serieTooltipColor.trim() != '') ? 
						'background-image: none; background-color: ' + serieTooltipColor.trim() : '',
                    fieldLabel : LN('sbi.chartengine.designer.color'),
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
                                Ext.getCmp('tooltipColorField').setFieldStyle(style);
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
		this.tooltipFieldSet.add(this.tooltipColor);
		
		var serieTooltipBackgroundColor = dataAtRow.get('serieTooltipBackgroundColor');
		this.tooltipBackgroundColor = {
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                Ext.create('Ext.form.field.Base', {
                    id : 'tooltipBackgroundColorField',
                    fieldLabel : LN('sbi.chartengine.designer.backgroundcolor'),
					fieldStyle : (serieTooltipBackgroundColor && serieTooltipBackgroundColor.trim() != '') ? 
						'background-image: none; background-color: ' + serieTooltipBackgroundColor.trim() : '',
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
                                Ext.getCmp('tooltipBackgroundColorField').setFieldStyle(style);
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
		this.tooltipFieldSet.add(this.tooltipBackgroundColor);
		
		var serieTooltipAlign = dataAtRow.get('serieTooltipAlign');
		this.tooltipAlignComboBox = Ext.create('Sbi.chart.designer.FontAlignCombo', {
			value: (serieTooltipAlign && serieTooltipAlign.trim() != '') ? serieTooltipAlign.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.align'),
		});
		this.tooltipFieldSet.add(this.tooltipAlignComboBox);
		
		var serieTooltipFont = dataAtRow.get('serieTooltipFont');
		this.tooltipFontComboBox = Ext.create('Sbi.chart.designer.FontCombo', {
			value: (serieTooltipFont && serieTooltipFont.trim() != '') ? serieTooltipFont.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.font'),
		});
		this.tooltipFieldSet.add(this.tooltipFontComboBox);
		
		var serieTooltipFontWeight = dataAtRow.get('serieTooltipFontWeight');
		this.tooltipFontWeightStylesComboBox = Ext.create('Sbi.chart.designer.FontStyleCombo', {
			value: (serieTooltipFontWeight && serieTooltipFontWeight.trim() != '') ? serieTooltipFontWeight.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontweight'),
		});
		this.tooltipFieldSet.add(this.tooltipFontWeightStylesComboBox);
		
		var serieTooltipFontSize = dataAtRow.get('serieTooltipFontSize');		
		this.tooltipFontSizeComboBox = Ext.create('Sbi.chart.designer.FontDimCombo', {
			value: (serieTooltipFontSize && serieTooltipFontSize.trim() != '') ? serieTooltipFontSize.trim() : '',
			fieldLabel : LN('sbi.chartengine.axisstylepopup.fontsize'),
		});
		this.tooltipFieldSet.add(this.tooltipFontSizeComboBox);
		
		this.add(this.serieFieldSet);
		this.add(this.tooltipFieldSet);
		
		if (chartType == "GAUGE")
		{
			this.add(this.dialFieldSet);
			this.add(this.dataLabelsFieldSet);
		}
	},
	
    writeConfigsAndExit: function() {
		var dataAtRow = store.getAt(rowIndex);
		var serieName = this.serieNameTextField.getValue();
		dataAtRow.set('axisName', serieName);
		
		var serieType = this.serieTypesComboBox.getValue();
		dataAtRow.set('serieType', serieType);
		
		var serieOrder = this.serieOrderComboBox.getValue();
		dataAtRow.set('serieOrderType', serieOrder);
		
		var serieColor = this.serieColorPicker.getColor();
		dataAtRow.set('serieColor', serieColor);
		
		var showValue = this.serieShowValue.getValue();
		dataAtRow.set('serieShowValue', showValue);
		
		var seriePrecision = this.seriePrecisionNumberField.getValue();
		dataAtRow.set('seriePrecision', seriePrecision);
		
		var prefixChar = this.seriePrefixCharTextField.getValue();
		dataAtRow.set('seriePrefixChar', prefixChar);
		
		var postfixChar = this.seriePostfixCharTextField.getValue();
		dataAtRow.set('seriePostfixChar', postfixChar);
		
		var templateHtml = this.tooltipTemplateHtml.getValue();
		dataAtRow.set('serieTooltipTemplateHtml', templateHtml);
		
		var serieTooltipColor = this.tooltipColor.getColor();
		dataAtRow.set('serieTooltipColor', serieTooltipColor);
		
		var serieTooltipBackgroundColor = this.tooltipBackgroundColor.getColor();
		dataAtRow.set('serieTooltipBackgroundColor', serieTooltipBackgroundColor);
		
		var serieTooltipAlign = this.tooltipAlignComboBox.getValue();
		dataAtRow.set('serieTooltipAlign', serieTooltipAlign);
		
		var serieTooltipFont = this.tooltipFontComboBox.getValue();
		dataAtRow.set('serieTooltipFont', serieTooltipFont);
		
		var serieTooltipFontWeight = this.tooltipFontWeightStylesComboBox.getValue();
		dataAtRow.set('serieTooltipFontWeight', serieTooltipFontWeight);
		
		var serieTooltipFontSize = '' + this.tooltipFontSizeComboBox.getValue(); //Save as string 
		dataAtRow.set('serieTooltipFontSize', serieTooltipFontSize);

		/**
		 * When close the popup take care also of elements that it had when appeared
		 * that are related to charts of type GAUGE.
		 * (danilo.ristovski@mht.net)
		 */
		if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE")
		{
			/**
			 * DIAL
			 */
			var backgroundColorDial = this.backgroundColorDial.getColor();
			dataAtRow.set('backgroundColorDial', backgroundColorDial);
			
			/**
			 * DATA LABELS
			 */		
			var yPositionDataLabels = this.yPositionDataLabels.getValue();
			dataAtRow.set('yPositionDataLabels', yPositionDataLabels);
			
			var colorDataLabels = this.colorDataLabels.getColor();
			dataAtRow.set('colorDataLabels', colorDataLabels);
			
			var formatDataLabels = this.formatDataLabels.getValue();
			dataAtRow.set('formatDataLabels', formatDataLabels);
		}		
		
		this.destroy();
	},
	
	items: [],
	// Cancel and Save buttons
    buttons: [{
        text: LN('sbi.generic.cancel'),
        handler: function(btn, elem2 ) {
			Ext.getCmp('serieStylePopup').destroy();
        }
    }, {
        text: LN('sbi.generic.save'),
        handler: function() {
			Ext.getCmp('serieStylePopup').writeConfigsAndExit();
        }
    }],
});