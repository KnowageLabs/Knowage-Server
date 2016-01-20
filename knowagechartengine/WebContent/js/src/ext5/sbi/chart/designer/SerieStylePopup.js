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
	serieShowAbsValue: null,
	serieShowPercentage:null,

	seriePrecisionNumberField: null,
	seriePrefixCharTextField: null,
	seriePostfixCharTextField: null,
	
	/**
	 * This item is going to be removed since the serie tooltip HTML template
	 * is handled by the velocity model of the appropriate chart type (this is
	 * done staticly, "under the hood").
	 * 
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
//	tooltipTemplateHtml: null,
	
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
		var LABEL_WIDTH = 115;
		
		this.callParent(config);		
		
		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
		var chartLibrary = Sbi.chart.designer.Designer.chartLibNamesConfig[chartType.toLowerCase()];
		
		store = config.store,
		
		rowIndex = config.rowIndex;
		var dataAtRow = store.getAt(rowIndex);	
			
		this.serieFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: LN('sbi.chartengine.designer.series'),
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : LABEL_WIDTH
			},
			layout: 'anchor',
			items : []
		});
			
		this.tooltipFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: LN('sbi.chartengine.designer.tooltip'),
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : LABEL_WIDTH,
			},
			hidden : ChartUtils.isSerieTooltipConfigurationDisabled(),
			layout: 'anchor',
			items : []
		});
		
		/**
		 * Additional elements and functionalities inside the Serie style popup window
		 * for the GAUGE chart type.
		 * (danilo.ristovski@mht.net)
		 */
		if (chartType == "GAUGE") {
			/**
			 * DIAL field set for the GAUGE chart type
			 */
			this.dialFieldSet = Ext.create('Ext.form.FieldSet', {
				collapsible: true,
				title: LN("sbi.chartengine.configuration.serieStyleConf.gauge.dial.fieldSetTitle"),	
				
				defaults: {
					anchor: '100%',
					labelAlign : 'left',
					labelWidth : LABEL_WIDTH,
				},
				
				layout: 'anchor',
				items : []
			});
			
			/**
			 * DATA_LABELS field set for the GAUGE chart type
			 */
			this.dataLabelsFieldSet = Ext.create('Ext.form.FieldSet', {
				collapsible: true,
				title: LN("sbi.chartengine.configuration.serieStyleConf.gauge.dataLabels.fieldSetTitle"),	
				
				defaults: {
					anchor: '100%',
					labelAlign : 'left',
					labelWidth : LABEL_WIDTH,
				},
				
				layout: 'anchor',
				items : []
			});
		
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
						labelWidth : LABEL_WIDTH,
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
			        fieldLabel: LN("sbi.chartengine.configuration.serieStyleConf.gauge.dataLabels.yPosition"),
			        emptyText: LN("sbi.chartengine.structure.serieStyleConfig.dataLabels.yPosition.emptyText")
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
						labelWidth : LABEL_WIDTH,
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
			
//			this.formatDataLabels = Ext.create
//	    	(
//				{
//			        xtype: 'textfield',
//			        id: 'formatDataLabels',
//			        value: dataAtRow.get('formatDataLabels'),
//			        fieldLabel: LN("sbi.chartengine.configuration.serieStyleConf.gauge.dataLabels.format"),
//			        emptyText: LN("sbi.chartengine.structure.serieStyleConfig.dataLabels.format.emptyText")
//			    }	
//	    	);
			
			this.dataLabelsFieldSet.add(this.yPositionDataLabels);
			this.dataLabelsFieldSet.add(this.colorDataLabels);
//			this.dataLabelsFieldSet.add(this.formatDataLabels);
		}
		
		/* * * * * * * * * * SERIE FIELDS  * * * * * *  * * * * */
		var serieName = dataAtRow.get('axisName');
		this.serieNameTextField = Ext.create('Ext.form.field.Text', {
			name: 'serieName',
			value: (serieName && serieName.trim() != '') ? serieName.trim() : '',
			fieldLabel: LN('sbi.generic.name'),
			selectOnFocus: true,
			allowBlank: true,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.name.emptyText")
		});
		this.serieFieldSet.add(this.serieNameTextField);
	    if(chartType=="WORDCLOUD"){
	    	this.serieNameTextField.hide();
	    }
		
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
			editable: false,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.type.emptyText")
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
		if ((chartType == "CHORD"
			|| chartType == "GAUGE" 
				|| chartType == "PIE" 
					|| chartType == "RADAR" 
						|| chartType == "SCATTER"
							|| chartType=="WORDCLOUD")
				||(chartLibrary == 'chartJs')){
		
			this.serieTypesComboBox.hide();
		}	
		
		this.serieFieldSet.add(this.serieTypesComboBox);		
		
		var serieOrder = dataAtRow.get('serieOrderType');
		this.serieOrderComboBox = Ext.create('Sbi.chart.designer.SeriesOrderCombo', {
			value: (serieOrder && serieOrder.trim() != '') ? serieOrder.trim() : '',
		});
		this.serieFieldSet.add(this.serieOrderComboBox);
				
		if(chartType=="WORDCLOUD"){
			this.serieOrderComboBox.hide();
		}
		
		var serieColor = dataAtRow.get('serieColor');
		this.serieColorPicker = {
			xtype : 'fieldcontainer',
			layout : 'hbox',
			
			/**
			 * ID of the field set that contains color picker is added. It is
			 * useful when the popup is showed for a PIE chart, since this one
			 * does not need the specification for the serie items color on the
			 * chart (color of its segment on the chart) because the chart is
			 * using colors from the color pallete on the Step 2 of the Designer
			 * when creating the XML template and rendering atfrewards.
			 *  @author: danristo (danilo.ristovski@mht.net)
			 */
			id: "serieColorFieldSet",
			
			items: [
				Ext.create('Ext.form.field.Base', {
					id : 'serieColorField',
					fieldStyle : (serieColor && serieColor.trim() != '') ? 
						'background-image: none; background-color: ' + serieColor.trim() : '',
					fieldLabel : LN('sbi.chartengine.designer.color'),
					labelWidth : LABEL_WIDTH,
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
		
		/**
		 * This parameters does not play any role when chart is of type PIE
		 * because series (pie segments) are going to take colors that are 
		 * specified inside the color palette of the Designer. This is 
		 * parameter useful for e.g. BAR and LINE chart types.
		 * @author: danristo (danilo.ristovski@mht.net)  
		 */
		// TODO: I think there are more chart types whose serie popup should be refined !!!		
		if (chartType == "PIE" || chartType=="WORDCLOUD")
		{			
			this.serieFieldSet.getComponent("serieColorFieldSet").hide();
		}		

		var showValue = dataAtRow.get('serieShowValue');
		this.serieShowValue = Ext.create('Ext.form.field.Checkbox',{
			checked: (showValue != undefined) ? showValue: true,
			labelSeparator: '',
			hidden: (chartLibrary == 'chartJs' || chartType == 'PIE' || chartType == 'WORDCLOUD'),
			fieldLabel: LN('sbi.chartengine.designer.showvalue'),
		});		

		var showAbsValue = dataAtRow.get('serieShowAbsValue');
		this.serieShowAbsValue = Ext.create('Ext.form.field.Checkbox',{
			checked: (showAbsValue != undefined) ? showAbsValue: true,
			labelSeparator: '',
			hidden: (chartLibrary == 'chartJs' || chartType != 'PIE'),
			fieldLabel: LN('sbi.chartengine.designer.showAbsValue'),
		});		
        
		var showPercentage = dataAtRow.get('serieShowPercentage');
		this.serieShowPercentage = Ext.create('Ext.form.field.Checkbox',{
			checked: (showPercentage != undefined) ? showPercentage: true,
			labelSeparator: '',
			hidden: (chartType != 'PIE' || chartType == "WORDCLOUD"),
			fieldLabel: LN('sbi.chartengine.designer.showPercentage'),
		});		

		
		this.serieFieldSet.add(this.serieShowValue);
		this.serieFieldSet.add(this.serieShowAbsValue);
		this.serieFieldSet.add(this.serieShowPercentage);
		
//		if(chartType=="PIE"){
//			this.serieShowAbsValue.show();
//			this.serieShowPercentage.show();
//			this.serieShowValue.hide();
//		}else if(chartType=="WORDCLOUD"){
//			this.serieShowAbsValue.hide();
//			this.serieShowPercentage.hide();
//			this.serieShowValue.hide();
//		}else{
//			this.serieShowAbsValue.hide();
//			this.serieShowPercentage.hide();
//			this.serieShowValue.show();
//		}
		
		var seriePrecision = dataAtRow.get('seriePrecision');
		this.seriePrecisionNumberField = Ext.create('Ext.form.field.Number', {
			id: "seriePrecisionNumberField",
			fieldLabel: LN('sbi.chartengine.designer.precision'),
			selectOnFocus: true,
			value: seriePrecision ? seriePrecision : '',
			maxValue: 10,
			minValue: 0,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.precision.emptyText")
		});
//		this.serieFieldSet.add(this.seriePrecisionNumberField);
		
		var prefixChar = dataAtRow.get('seriePrefixChar');
		this.seriePrefixCharTextField = Ext.create('Ext.form.field.Text', {
			id: "seriePrefixCharTextField",
			name: 'name',
			value: (prefixChar && prefixChar.trim() != '') ? prefixChar.trim() : '',
			hidden: (chartLibrary == 'chartJs'),
			fieldLabel: LN('sbi.chartengine.designer.prefixtext'),
			selectOnFocus: true,
			allowBlank: true,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.prefixText.emptyText")
		});
//		this.serieFieldSet.add(this.seriePrefixCharTextField);
		
		var postfixChar = dataAtRow.get('seriePostfixChar'); 
		this.seriePostfixCharTextField = Ext.create('Ext.form.field.Text', {
			id: "seriePostfixCharTextField",
			name: 'name',
			value: (postfixChar && postfixChar.trim() != '') ? postfixChar.trim() : '',
			hidden: (chartLibrary == 'chartJs'),
			fieldLabel: LN('sbi.chartengine.designer.postfixtext'),
			selectOnFocus: true,
			allowBlank: true,
			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.postfixText.emptyText")
		});
//		this.serieFieldSet.add(this.seriePostfixCharTextField);
		
		// Format: Danilo
//		var postfixChar = dataAtRow.get('seriePostfixChar');
		
		/**
		 * The value (string) of format modality of displaying of the
		 * serie item. This string is inserted by the user into the 
		 * text field on the popup (under the "Serie" fieldset.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var serieFormat = dataAtRow.get('serieFormat');
		
		this.serieFormatTextField = Ext.create
		(
			'Ext.form.field.Text', 
			{
				id: "serieFormatTextField",
				name: 'name',
				value: (serieFormat && serieFormat.trim() != '') ? serieFormat.trim() : '',
				fieldLabel: "Format",	// TODO: LN()
				selectOnFocus: true,
				allowBlank: true,
				emptyText: "Type the format"	// TODO: LN()
			}
		);
		
		var globalScope = this;
		
		// TODO: Srediti ovo - treba uraditi isto kao i za serieFormat
		/**
		 * Should be a string value that will give us info about the criteria 
		 * that is used for formatting the serie value.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var serieFormatOrPrecision = dataAtRow.get('serieFormatOrPrecision');
		
		/**
		 * The combo that will let user pick between two possible modalities
		 * of displaying the serie item: format or precision.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		this.serieFormatOrPrecision = Ext.create('Ext.form.ComboBox', {
//			xtype: "combobox",
			fieldLabel: "Precision or format", // TODO: LN(),
			
			store: {
				fields: ["name","value"],
				
				data: [{
			 		name: "Precision", // TODO: LN(),
			 		value: "precision"
			 	}, {
			 		name: "Format", // TODO: LN(),
			 		value: "format"
			 	}]
			},
			
			value: (serieFormatOrPrecision) ? serieFormatOrPrecision : "format",
			valueField: 'value',
			displayField: 'name',
			editable: false,
			emptyText: "Choose between precision and format",	// TODO: LN()
			
			listeners: {
				change: function(combo,value,b) {	
					globalScope.seriePrecisionNumberField.setValue("");
					globalScope.seriePrefixCharTextField.setValue("");
					globalScope.seriePostfixCharTextField.setValue("");
					globalScope.serieFormatTextField.setValue("");
					
					globalScope.serieFieldSet.add(globalScope.seriePrecisionNumberField);
					globalScope.serieFieldSet.add(globalScope.seriePrefixCharTextField);
					globalScope.serieFieldSet.add(globalScope.seriePostfixCharTextField);						
					globalScope.serieFieldSet.add(globalScope.serieFormatTextField);
					
					if (value.toLowerCase() == "precision") {
						Ext.getCmp("serieFormatTextField").hide();
						Ext.getCmp("seriePrecisionNumberField").show();
						Ext.getCmp("seriePrefixCharTextField").show();							
						Ext.getCmp("seriePostfixCharTextField").show();
					}
					
					if (value.toLowerCase() == "format") {
						Ext.getCmp("seriePrecisionNumberField").hide();
						Ext.getCmp("seriePrefixCharTextField").hide();							
						Ext.getCmp("seriePostfixCharTextField").hide();
						Ext.getCmp("serieFormatTextField").show();
					}	
				},
				
				/**
				 * This will run when the popup is opened.
				 */
				render: function(combo) {
					var value = combo.getValue();
					
					globalScope.serieFieldSet.add(globalScope.seriePrecisionNumberField);
					globalScope.serieFieldSet.add(globalScope.seriePrefixCharTextField);
					globalScope.serieFieldSet.add(globalScope.seriePostfixCharTextField);
					
					globalScope.serieFieldSet.add(globalScope.serieFormatTextField);
					
					if (value.toLowerCase() == "precision") {
						Ext.getCmp("serieFormatTextField").hide();
					}
					
					if (value.toLowerCase() == "format") {
						Ext.getCmp("seriePrecisionNumberField").hide();
						Ext.getCmp("seriePrefixCharTextField").hide();							
						Ext.getCmp("seriePostfixCharTextField").hide();
					}	
				}
			}
		});
		
		this.serieFieldSet.add(this.serieFormatOrPrecision);
		
		if(chartLibrary == 'chartJs') {
			this.serieFormatOrPrecision.setValue("precision");
			this.serieFormatOrPrecision.setDisabled(true);
			
			globalScope.seriePrefixCharTextField.setValue("");
			globalScope.seriePostfixCharTextField.setValue("");
			
			globalScope.seriePrefixCharTextField.hide();
			globalScope.seriePostfixCharTextField.hide();
		}
		
//		if (chartType == "GAUGE")
//		{
//			this.serieFieldSet.add(this.precisionOrFormat);
//		}
				
		/**
		 * This item is going to be removed since the serie tooltip HTML template
		 * is handled by the velocity model of the appropriate chart type (this is
		 * done staticly, "under the hood").
		 * 
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		/* * * * * * * * * * TOOTLTIP FIELDS  * * * * * *  * * * * */
//		var templateHtml = dataAtRow.get('serieTooltipTemplateHtml');
//		this.tooltipTemplateHtml = Ext.create('Ext.form.field.TextArea',{
//			grow      : true,
//			name      : 'tooltipTemplateHtml',
//			value: (templateHtml && templateHtml.trim() != '') ? templateHtml.trim() : '',
//			fieldLabel: LN('sbi.chartengine.designer.templatehtml'),
//			anchor    : '100%',
//			emptyText: LN("sbi.chartengine.structure.serieStyleConfig.tooltip.templateHtml.emptyText")
//		});
//		this.tooltipFieldSet.add(this.tooltipTemplateHtml);
		
		var serieTooltipColor = dataAtRow.get('serieTooltipColor');
		this.tooltipColor = {
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                Ext.create('Ext.form.field.Base', {
                    id : 'tooltipColorField',
					fieldStyle : (serieTooltipColor && serieTooltipColor.trim() != '') ? 
						'background-image: none; background-color: ' + serieTooltipColor.trim() : '',
                    fieldLabel : LN('sbi.chartengine.designer.tooltip.color'),
					labelWidth : LABEL_WIDTH,
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
					labelWidth : LABEL_WIDTH,
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
			fieldLabel : LN('sbi.chartengine.designer.tooltip.align'),
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
		
//		if(chartType=="WORDCLOUD"){
//		  this.tooltipFieldSet.hide();	
//		}
		
		if (chartType == "GAUGE")
		{
			this.add(this.dialFieldSet);
			this.add(this.dataLabelsFieldSet);
		}
	},
	
    writeConfigsAndExit: function() {
    	
    	/**
    	 * If there is some not valid entry, display the message in order
    	 * to inform the user about that.
    	 * 
    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	 */
    	var errorMessages = "";
    	
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
				
		var showAbsValue = this.serieShowAbsValue.getValue();
		dataAtRow.set('serieShowAbsValue', showAbsValue);
		
		var showPercentage = this.serieShowPercentage.getValue();
		dataAtRow.set('serieShowPercentage', showPercentage);
				
		/**
		 * A property that contains information about the modality of
		 * displaying of a particular serie item. It can be a value of
		 * "format" or "precision".
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */		
		var serieFormatOrPrecision = this.serieFormatOrPrecision.getValue();
		dataAtRow.set('serieFormatOrPrecision', serieFormatOrPrecision);
		
		if (serieFormatOrPrecision.toLowerCase() == "precision")
		{		
			var seriePrecision = this.seriePrecisionNumberField.getValue();
			
			/**
			 * Validation for value of precision if this modality is picked.
			 */
			if (seriePrecision < 0)
			{
				errorMessages += "The serie precision value cannot be less then <b>0</b>";	// TODO: LN()
			}
			else
			{
				dataAtRow.set('seriePrecision', seriePrecision);
				
				var prefixChar = this.seriePrefixCharTextField.getValue();
				dataAtRow.set('seriePrefixChar', prefixChar);
				
				var postfixChar = this.seriePostfixCharTextField.getValue();
				dataAtRow.set('seriePostfixChar', postfixChar);
			}
		}
		else if (serieFormatOrPrecision.toLowerCase() == "format")
		{
			/**
			 * Three possible way to construct an expression that should display
			 * a serie item in certain way (how user defined through it).
			 */
			var onlyYString = "y";
			var yString = "y:";
			var pointYString = "point.y:";
			
			var serieFormat = this.serieFormatTextField.getValue();
			
			/**
			 * We can have exactly one expression for the formatting of serie, so
			 * if there is more than one, show the popup with the warning. 
			 */
			var numOfExprInFormatPointYString = (serieFormat.match(new RegExp("{"+pointYString, "g")) || []).length;
			var numOfExprInFormatYString = (serieFormat.match(new RegExp("{"+yString, "g")) || []).length;
			var numOfExprInFormatOnlyYString = (serieFormat.match(new RegExp("{"+onlyYString+"}", "g")) || []).length;
					
			var totalNumberOfExpressions = numOfExprInFormatPointYString+numOfExprInFormatYString+numOfExprInFormatOnlyYString;
			
			/**
			 * Validation that takes care that there is exactly one expression that
			 * serves for formatting of the particular serie item.
			 */
			if (numOfExprInFormatPointYString > 1 || numOfExprInFormatYString > 1 || numOfExprInFormatOnlyYString > 1 || 
					totalNumberOfExpressions >= 2)
			{
				errorMessages += "Maximum one expression that formats the value of the serie (<b>y</b>, <b>y:</b> or <b>point.y:</b>) can be specified. Please try again or Cancel.";	// TODO: LN()
			}
			else
			{
				dataAtRow.set('serieFormat', serieFormat);
			}			
		}
		
//		var templateHtml = this.tooltipTemplateHtml.getValue();
//		dataAtRow.set('serieTooltipTemplateHtml', templateHtml);
		
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
			
//			var formatDataLabels = this.formatDataLabels.getValue();
//			dataAtRow.set('formatDataLabels', formatDataLabels);
		}		
		
		if (errorMessages)
		{
			Ext.Msg.show
			(
				{
					title : LN("sbi.chartengine.validation.structure.massageWarning.headerTitle"),
					message : errorMessages,
					icon : Ext.Msg.WARNING,
					closable : true,
					buttons : Ext.Msg.OK
				}
			);
		}			
		else
		{
			this.destroy();
		}
		
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