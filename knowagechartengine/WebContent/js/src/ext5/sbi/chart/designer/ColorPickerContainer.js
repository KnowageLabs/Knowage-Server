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
		
		var globalScope = this;
		
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
		
		/**
		 * Important when this component is mandatory for the chart.
		 * This event will inform us that color is picked and we
		 * don't need the flag that warns the user.
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		picker.on
		(
			"colorPicked", 
			
			function() 
			{
				if (globalScope.config.isColorMandatory)
				{					
					field.labelEl.update(globalScope.config.label + ":"); 
				}					
			}
		);	
		
		this.add(field);
		this.add(picker);
	}
});