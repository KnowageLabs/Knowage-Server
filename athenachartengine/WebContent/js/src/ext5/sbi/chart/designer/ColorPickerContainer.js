Ext.define('Sbi.chart.designer.ColorPickerContainer', {
	extend : 'Ext.container.Container',
	name: 'backgroundColor',
	layout : 'hbox',
	margin: '5 0',
	items : [ ],
	config:{
		customLabel : null,
		fieldBind: null,
	},
	constructor : function(config) {
		this.callParent(config);
		Ext.apply(this.config,config);
		
		this.viewModel = config.viewModel;
		
		var picker = Ext.create('Sbi.chart.designer.ColorPicker',{
			viewModel : this.viewModel,
			bind : config.fieldBind
		});
		var field = Ext.create('Ext.form.Field',{
			readOnly : true,
			fieldLabel : this.config.customLabel ? this.config.customLabel : 'Colore',
			bind: this.config.fieldBind
		});
		this.add(field);
		this.add(picker);
	}
});