Ext.define('Sbi.chart.designer.SerieStylePopup', {
	extend: 'Ext.form.Panel',
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
	tooltipBackgroundColor: null,
	tooltipAlignComboBox: null,
	tooltipFontsComboBox: null,
	field: null,
	field: null,
	field: null,
	field: null,
	

    // Fields will be arranged vertically, stretched to full width
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
			
		// var serieFieldSet = Ext.create('Ext.form.FieldSet', {	
		this.serieFieldSet = Ext.create('Ext.form.FieldSet', {
			// id: 'serieFieldSet',
			collapsible: true,
			// defaultType: 'textfield',
			title: 'Serie',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115
			},
			layout: 'anchor',
			items : [ ]
		});
			
		// var tooltipFieldSet = Ext.create('Ext.form.FieldSet', {	
		this.tooltipFieldSet = Ext.create('Ext.form.FieldSet', {
			// id: 'tooltipFieldSet',
			collapsible: true,
			title: 'Tooltip',
			// defaultType: 'textfield',
			defaults: {anchor: '100%',
				labelAlign : 'left',
				labelWidth : 115,
			},
			layout: 'anchor',
			items : [ ]
		});
		
		
		/* * * * * * * * * * SERIE FIELDS  * * * * * *  * * * * */
		
		
		var serieName = dataAtRow.get('axisName');
		// var serieNameTextField = {
		this.serieNameTextField = {
			xtype: 'textfield',
			// id: 'serieName',
			name: 'serieName',
			value: (serieName && serieName.trim() != '') ? serieName.trim() : 'Custom name',
			fieldLabel: 'Name',
			selectOnFocus: true,
			allowBlank: true 
		};
		this.serieFieldSet.add(this.serieNameTextField);
	
		var serieType = dataAtRow.get('serieType');
		var serieTypes = [
			{name: 'Bar chart', value:'bar'},
			{name: 'Column chart', value:'column'}, 
			{name: 'Line chart', value:'line'}, 
			{name: 'Pie chart', value:'pie'} 
		];
		// var serieTypesComboBox = Ext.create('Ext.form.ComboBox', {
		this.serieTypesComboBox = Ext.create('Ext.form.ComboBox', {
			// id: 'serieTypes',
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
		// var serieOrderComboBox = Ext.create('Ext.form.ComboBox', {
		this.serieOrderComboBox = Ext.create('Ext.form.ComboBox', {
			// id: 'serieOrder',
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
		// var serieColorPicker = {
		this.serieColorPicker = {
			// id: 'serieColor',
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'serieColorField',
                    xtype : 'field',
					fieldStyle : (serieColor && serieColor.trim() != '') ? 'background-color: ' + serieColor.trim() : '',
                    fieldLabel : 'Color',
                    labelWidth : 115,
					readOnly : true,
					flex: 15
                }, {
                    xtype : 'button',
					layout : 'hbox',
                    menu : Ext.create('Ext.menu.ColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor;
                                Ext.getCmp('serieColorField').setFieldStyle(style);
                            }
                        }
                    }),
					flex: 1                
                }
			]
		};
		this.serieFieldSet.add(this.serieColorPicker);
		
		var showValue = dataAtRow.get('serieShowValue');
		// var serieShowValue = {
		this.serieShowValue = {
			// id: 'serieShowValue',
			xtype: 'checkboxfield',
			checked: (showValue != undefined) ? showValue: true,
			labelSeparator: '',
			fieldLabel: 'Show value',
		};
		this.serieFieldSet.add(this.serieShowValue);
		
		var seriePrecision = dataAtRow.get('seriePrecision');
		// var seriePrecisionNumberField = {
		this.seriePrecisionNumberField = {
			xtype: 'numberfield',
			// id: 'seriePrecision',
			fieldLabel: 'Precision',
			selectOnFocus: true,
			value: (seriePrecision && seriePrecision.trim() != '') ? seriePrecision.trim() : '',
			maxValue: 10,
			minValue: 0
		};
		this.serieFieldSet.add(this.seriePrecisionNumberField);
		
		
		var prefixChar = dataAtRow.get('seriePrefixChar');
		// var seriePrefixCharTextField = {
		this.seriePrefixCharTextField = {
			xtype: 'textfield',
			// id: 'prefixChar',
			name: 'name',
			value: (prefixChar && prefixChar.trim() != '') ? prefixChar.trim() : '',
			fieldLabel: 'Prefix text',
			selectOnFocus: true,
			allowBlank: true 
		};
		this.serieFieldSet.add(this.seriePrefixCharTextField);
		
		var postfixChar = dataAtRow.get('seriePostfixChar'); 
		// var seriePostfixCharTextField = {
		this.seriePostfixCharTextField = {
			xtype: 'textfield',
			id: 'postfixChar',
			name: 'name',
			value: (postfixChar && postfixChar.trim() != '') ? postfixChar.trim() : '',
			fieldLabel: 'Postfix text',
			selectOnFocus: true,
			allowBlank: true 
		};
		this.serieFieldSet.add(this.seriePostfixCharTextField);
		
		
		/* * * * * * * * * * TOOTLTIP FIELDS  * * * * * *  * * * * */
		
		var templateHtml = dataAtRow.get('serieTooltipTemplateHtml');
		// var tooltipTemplateHtml = Ext.create('Ext.form.field.TextArea',{
		this.tooltipTemplateHtml = Ext.create('Ext.form.field.TextArea',{
			// id: 'templateHtml',
			grow      : true,
			name      : 'tooltipTemplateHtml',
			value: (templateHtml && templateHtml.trim() != '') ? templateHtml.trim() : '',
			fieldLabel: 'Template html',
			anchor    : '100%'
		});
		this.tooltipFieldSet.add(this.tooltipTemplateHtml);
		
		var serieTooltipColor = dataAtRow.get('serieTooltipColor');
		// var tooltipColor = {
		this.tooltipColor = {
			// id: 'tooltipColor',
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'tooltipColorField',
                    xtype : 'field',
					fieldStyle : (serieTooltipColor && serieTooltipColor.trim() != '') ? 'background-color: ' + serieTooltipColor.trim() : '',
                    fieldLabel : 'Color',
					labelWidth : 115,
                    readOnly : true,
					flex: 15
                }, {
                    xtype : 'button',
					layout : 'hbox',
                    menu : Ext.create('Ext.menu.ColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor
                                        + '; background-image: none;';
                                Ext.getCmp('tooltipColorField').setFieldStyle(style);
								
								// console.log('selected color:', '#'+selColor);
                            }
                        }
                    }),
					flex: 1                
                }
			]
		};
		this.tooltipFieldSet.add(this.tooltipColor);
		
		var serieTooltipBackgroundColor = dataAtRow.get('serieTooltipBackgroundColor');
		// var tooltipBackgroundColor = {
		this.tooltipBackgroundColor = {
			// id: 'tooltipBackgroundColor',
			xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'tooltipBackgroundColorField',
                    fieldLabel : 'Background color',
					fieldStyle : (serieTooltipBackgroundColor && serieTooltipBackgroundColor.trim() != '') ? 
						'background-color: ' + serieTooltipBackgroundColor.trim() : '',
					labelWidth : 115,
                    xtype : 'field',
                    readOnly : true,
					flex: 15
                }, {
                    xtype : 'button',
					layout : 'hbox',
                    menu : Ext.create('Ext.menu.ColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor
                                        + '; background-image: none;';
                                Ext.getCmp('tooltipBackgroundColorField').setFieldStyle(style);
								
								// console.log('selected color:', '#'+selColor);
                            }
                        }
                    }),
					flex: 1                
                }
			]
		};
		this.tooltipFieldSet.add(this.tooltipBackgroundColor);
		
		var serieTooltipAlign = dataAtRow.get('serieTooltipAlign');
		var tooltipAlign = [ {name: 'Left ', value:'left'}, {name: 'Center', value:'center'}, {name: 'Right', value:'right'}];
		// var tooltipAlignComboBox = Ext.create('Ext.form.ComboBox', {
		this.tooltipAlignComboBox = Ext.create('Ext.form.ComboBox', {
			// id: 'tooltipAlign',
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
		// var tooltipFontsComboBox = Ext.create('Ext.form.ComboBox', {
		this.tooltipFontsComboBox = Ext.create('Ext.form.ComboBox', {
			// id: 'tooltipFonts',
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
		var tooltipFontWeightStylesComboBox = Ext.create('Ext.form.ComboBox', {
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
		this.tooltipFieldSet.add(tooltipFontWeightStylesComboBox);
		
		var serieTooltipFontSize = dataAtRow.get('serieTooltipFontSize');		
		var tooltipFontSize = [[8],[9],[10],[11],[12],[14],[16],[18],[20],[22],[24],[26],[28],[36],[48],[72]];
		var tooltipFontSizeComboBox = Ext.create('Ext.form.ComboBox', {
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
		this.tooltipFieldSet.add(tooltipFontSizeComboBox);
		
		this.add(this.serieFieldSet);
		this.add(this.tooltipFieldSet);
	},
	
	/*
	beforeDestroy: function () {
		var serieFieldSetItems = this.serieFieldSet.items.items;
		
		for(index in serieFieldSetItems) {
			serieFieldSetItems[index].destroy();
		}
		
		serieFieldSet.destroy();
		
		var tooltipFieldSetItems = this.tooltipFieldSet.items.items;
		
		for(index in tooltipFieldSetItems) {
			tooltipFieldSetItems[index].destroy();
		}
		tooltipFieldSet.destroy();
    },
    */
	
    writeConfigsAndExit: function() {
		Ext.log('Dati scritti');
		// console.log('store',store);
		// console.log('rowIndex', rowIndex);
		
		
		
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