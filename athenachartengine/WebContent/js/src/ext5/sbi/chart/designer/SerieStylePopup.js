Ext.define('Sbi.chart.designer.SerieStylePopup', {
	extend: 'Ext.form.Panel',
	requires: [
	    'Sbi.chart.designer.SerieColorPicker'
    ],	
	id: 'serieStylePopup',
    title: 'Simple Form',
    layout: 'border',
    bodyPadding: 5,
	floating: true,
    draggable: true,
    closable : true,
    closeAction: 'destroy',
    width: 500,
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
	tooltipFontsComboBox: null,
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
			title: 'Serie',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115
			},
			layout: 'anchor',
			items : [ ]
		});
			
		this.tooltipFieldSet = Ext.create('Ext.form.FieldSet', {
			collapsible: true,
			title: 'Tooltip',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115,
			},
			layout: 'anchor',
			items : [ ]
		});
		
		/* * * * * * * * * * SERIE FIELDS  * * * * * *  * * * * */
		var serieName = dataAtRow.get('axisName');
		this.serieNameTextField = Ext.create('Ext.form.field.Text', {
			name: 'serieName',
			value: (serieName && serieName.trim() != '') ? serieName.trim() : 'Custom name',
			fieldLabel: 'Name',
			selectOnFocus: true,
			allowBlank: true 
		});
		this.serieFieldSet.add(this.serieNameTextField);
	
		var serieType = dataAtRow.get('serieType');
		var serieTypes = [
			{name: 'Bar chart', value:'bar'},
			{name: 'Column chart', value:'column'}, 
			{name: 'Line chart', value:'line'}, 
			{name: 'Pie chart', value:'pie'} 
		];
		
		this.serieTypesComboBox = Ext.create('Ext.form.ComboBox', {
			store: {
				store: 'array',
				fields: ['name', 'value'],
				data: serieTypes
			},
			value: (serieType && serieType.trim() != '') ? serieType.trim() : '',
			valueField: 'value',
			displayField: 'name',
			fieldLabel : 'Serie type',
			listeners: {
				change: function(sender, newValue, oldValue, opts) {
					this.inputEl.setStyle('font-family', newValue);
				}
			}
		});
		this.serieFieldSet.add(this.serieTypesComboBox);		
		
		var serieOrder = dataAtRow.get('serieOrderType');
		var serieOrders = [ {name: 'Ascending ', value:'asc'}, {name: 'Descending', value:'desc'}];
		this.serieOrderComboBox = Ext.create('Ext.form.ComboBox', {
			store: {
				store: 'array',
				fields: ['name', 'value'],
				data: serieOrders
			},
			value: (serieOrder && serieOrder.trim() != '') ? serieOrder.trim() : '',
			valueField: 'value',
			displayField: 'name',
			fieldLabel : 'Serie order type',
			listeners: {
				change: function(sender, newValue, oldValue, opts) {
					this.inputEl.setStyle('font-family', newValue);
				}
			}
		});
		this.serieFieldSet.add(this.serieOrderComboBox);
				
		var serieColor = dataAtRow.get('serieColor');
		this.serieColorPicker = Ext.create('Sbi.chart.designer.SerieColorPicker',{
			colorValue: serieColor,
			fieldLabel: 'Color'
		});
		this.serieFieldSet.add(this.serieColorPicker);
		
		var showValue = dataAtRow.get('serieShowValue');
		this.serieShowValue = Ext.create('Ext.form.field.Checkbox',{
			checked: (showValue != undefined) ? showValue: true,
			labelSeparator: '',
			fieldLabel: 'Show value',
		});
		this.serieFieldSet.add(this.serieShowValue);
		
		var seriePrecision = dataAtRow.get('seriePrecision');
		this.seriePrecisionNumberField = Ext.create('Ext.form.field.Number', {
			fieldLabel: 'Precision',
			selectOnFocus: true,
			value: (seriePrecision && seriePrecision.trim() != '') ? seriePrecision.trim() : '',
			maxValue: 10,
			minValue: 0
		});
		this.serieFieldSet.add(this.seriePrecisionNumberField);
		
		var prefixChar = dataAtRow.get('seriePrefixChar');
		this.seriePrefixCharTextField = Ext.create('Ext.form.field.Text', {
			name: 'name',
			value: (prefixChar && prefixChar.trim() != '') ? prefixChar.trim() : '',
			fieldLabel: 'Prefix text',
			selectOnFocus: true,
			allowBlank: true 
		});
		this.serieFieldSet.add(this.seriePrefixCharTextField);
		
		var postfixChar = dataAtRow.get('seriePostfixChar'); 
		this.seriePostfixCharTextField = Ext.create('Ext.form.field.Text', {
			name: 'name',
			value: (postfixChar && postfixChar.trim() != '') ? postfixChar.trim() : '',
			fieldLabel: 'Postfix text',
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
			fieldLabel: 'Template html',
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
                    fieldLabel : 'Color',
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
                    fieldLabel : 'Background color',
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
		var tooltipAlign = [ {name: 'Left ', value:'left'}, {name: 'Center', value:'center'}, {name: 'Right', value:'right'}];
		this.tooltipAlignComboBox = Ext.create('Ext.form.ComboBox', {
			store: {
				store: 'array',
				fields: ['name', 'value'],
				data: tooltipAlign
			},
			value: (serieTooltipAlign && serieTooltipAlign.trim() != '') ? serieTooltipAlign.trim() : '',
			valueField: 'value',
			displayField: 'name',
			fieldLabel : 'Align',
			listeners: {
				change: function(sender, newValue, oldValue, opts) {
					this.inputEl.setStyle('font-family', newValue);
				}
			}
		});
		this.tooltipFieldSet.add(this.tooltipAlignComboBox);
		
		var serieTooltipFont = dataAtRow.get('serieTooltipFont');
		var tooltipFonts = [['Arial'], ['Times New Roman'], ['Tahoma'], ['Verdana']];
		this.tooltipFontsComboBox = Ext.create('Ext.form.ComboBox', {
			store: {
				store: 'array',
				fields: ['name'],
				data: tooltipFonts
			},
			value: (serieTooltipFont && serieTooltipFont.trim() != '') ? serieTooltipFont.trim() : '',
			displayField: 'name',
			fieldLabel : 'Font',
			listeners: {
				change: function(sender, newValue, oldValue, opts) {
					this.inputEl.setStyle('font-family', newValue);
				}
			}
		});
		this.tooltipFieldSet.add(this.tooltipFontsComboBox);
		
		var serieTooltipFontWeight = dataAtRow.get('serieTooltipFontWeight');
		var tooltipFontWeightStyles = [
			{name : 'Bold', value : 'b'}, 
			{name : 'Italic', value : 'i'},
			{name : 'Underline', value : 'u'},
			{name : 'Strike-through', value : 's'}, 
		];
		
		this.tooltipFontWeightStylesComboBox = Ext.create('Ext.form.ComboBox', {
			id: 'tooltipFontWeightStyles',
			store: {
				store: 'array',
				fields: ['name', 'value'],
				data: tooltipFontWeightStyles
			},
			value: (serieTooltipFontWeight && serieTooltipFontWeight.trim() != '') ? serieTooltipFontWeight.trim() : '',
			valueField: 'value',
			displayField: 'name',
			fieldLabel : 'Font weight',
			listeners: {
				change: function(sender, newValue, oldValue, opts) {
					this.inputEl.setStyle('font-family', newValue);
				}
			}
		});
		this.tooltipFieldSet.add(this.tooltipFontWeightStylesComboBox);
		
		var serieTooltipFontSize = dataAtRow.get('serieTooltipFontSize');		
		var tooltipFontSize = [[8],[9],[10],[11],[12],[14],[16],[18],[20],[22],[24],[26],[28],[36],[48],[72]];
		this.tooltipFontSizeComboBox = Ext.create('Ext.form.ComboBox', {
			id: 'tooltipFontSize',
			store: {
				store: 'array',
				fields: ['name'],
				data: tooltipFontSize
			},
			value: (serieTooltipFontSize && serieTooltipFontSize.trim() != '') ? serieTooltipFontSize.trim() : '',
			displayField: 'name',
			fieldLabel : 'Font size',
			listeners: {
				change: function(sender, newValue, oldValue, opts) {
					this.inputEl.setStyle('font-family', newValue);
				}
			}
		});
		this.tooltipFieldSet.add(this.tooltipFontSizeComboBox);
		
		this.add(this.serieFieldSet);
		this.add(this.tooltipFieldSet);
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
		
		var seriePrecision = '' + this.seriePrecisionNumberField.getValue(); //Save as string 
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
		
		var serieTooltipFont = this.tooltipFontsComboBox.getValue();
		dataAtRow.set('serieTooltipFont', serieTooltipFont);
		
		var serieTooltipFontWeight = this.tooltipFontWeightStylesComboBox.getValue();
		dataAtRow.set('serieTooltipFontWeight', serieTooltipFontWeight);
		
		var serieTooltipFontSize = '' + this.tooltipFontSizeComboBox.getValue(); //Save as string 
		dataAtRow.set('serieTooltipFontSize', serieTooltipFontSize);
				
		Ext.log('Store updated');
		this.destroy();
	},
	
	items: [],
	// Cancel and Save buttons
    buttons: [{
        text: 'Cancel',
        handler: function(btn, elem2 ) {
			Ext.getCmp('serieStylePopup').destroy();
        }
    }, {
        text: 'Save',
        handler: function() {
			Ext.getCmp('serieStylePopup').writeConfigsAndExit();
        }
    }],
});