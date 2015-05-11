Ext.define('Sbi.chart.designer.ColorPickerContainer', {
	extend : 'Ext.container.Container',
	name: 'backgroundColor',
	layout : 'hbox',
	margin: '5 0',
	items : [ ],
	config:{
		customLabel : '',
		fieldBind: '',
	},
	constructor : function(config) {
		this.callParent(config);
		Ext.apply(this.config,config);
		var picker = Ext.create('Sbi.chart.designer.ColorPicker');
		var field = Ext.create('Ext.form.Field',{
			readOnly : true,
			fieldLabel : this.config.customLabel ? this.config.customLabel : 'Colore',
			bind: this.config.fieldBind
		});
		this.add(field);
		this.add(picker);
	}
});