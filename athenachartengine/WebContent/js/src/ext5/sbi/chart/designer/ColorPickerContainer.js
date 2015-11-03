Ext.define('Sbi.chart.designer.ColorPickerContainer', {
	extend : 'Ext.container.Container',
	name: 'backgroundColor',
	layout : 'hbox',
	margin: '5 0',
	items : [ ],
	config:{
		customLabel : null,
		fieldBind: null,
		isColorMandatory: false,
		label: LN('sbi.chartengine.configuration.color')
	},
	constructor : function(config) {
		this.callParent(config);
		Ext.apply(this.config,config);
		
		this.viewModel = config.viewModel;
		
		var picker = Ext.create('Sbi.chart.designer.ColorPicker',{
			viewModel : this.viewModel,
			fieldBind: this.config.fieldBind
		});
		
		var field = Ext.create('Ext.form.Field',{
			readOnly : true,
			//fieldLabel : this.config.customLabel ? this.config.customLabel : LN('sbi.chartengine.configuration.color'),
			fieldLabel: this.config.isColorMandatory ?  
					this.config.label + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: this.config.label,
			bind: {
				fieldStyle : 'background-image: none; background-color: '+this.config.fieldBind,
			}
		});
		
		this.add(field);
		this.add(picker);
	}
});