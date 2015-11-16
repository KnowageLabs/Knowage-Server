Ext.define('Sbi.chart.designer.SerieColorPicker',{
	extend: 'Ext.form.FieldContainer',
	layout : 'hbox',
	alternateClassName: ['SerieColorPicker'],
	config: {
		colorValue : '',
		fieldLabel : LN('sbi.chartengine.configuration.color'),
		labelWidth : 115,
	},
	statics: {
		fieldIdSeed: 0,
	},
	constructor: function(config){
		this.callParent(config);
		this.fieldLabel = config.fieldLabel;
		this.labelWidth = config.labelWidth;
		
		var idField = 'serieColorField_' + SerieColorPicker.fieldIdSeed++;
		
		var formField = Ext.create('Ext.form.field.Base', {
			id : idField,
			fieldStyle : (config.colorValue && config.colorValue.trim() != '') ? 
				'background-image: none; background-color: ' + config.colorValue.trim() : '',
			flex: 15,
			readOnly : true,
			
			getStyle: function() {
				return this.getFieldStyle( );
			}
		});
		this.add(formField);
		
		var colorPicker = {
			xtype : 'button',
			layout : 'hbox',
			menu : Ext.create('Ext.menu.ColorPicker',{
				listeners : {
					select : function(picker, selColor) {
						var style = 'background-image: none;background-color: #' + selColor;
						
						Ext.getCmp(idField).setFieldStyle(style);
					}
				}
			}),
			flex: 1                
		};

		this.add(colorPicker);
	},
	items: [],
	getColor: function(){
		var styleColor = this.items[0].getStyle();
		var indexOfSharp = styleColor.indexOf('#');
		
		return styleColor.substring(indexOfSharp);
	}
});