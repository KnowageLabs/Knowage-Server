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
		
		this.axisData = config.axisData;
		
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
			id: 'titleFieldSetForAxis',	// (danilo.ristovski@mht.net)
			title: 'Title',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115
			},
			layout: 'anchor',
			items : []
		});
		
		if(isYAxis) {
			this.majorgridFieldSet = Ext.create('Ext.form.FieldSet', {
				collapsible: true,
				collapsed : true,
				id: "majorGridFieldSetYAxis",	// (danilo.ristovski@mht.net)
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
				id: "minorGridFieldSetYAxis", // (danilo.ristovski@mht.net)
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
		 * (danilo.ristovski@mht.net)
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
		
		var styleOpposite = this.styleOpposite.getValue();
		this.axisData.styleOpposite = styleOpposite;

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