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
		label: LN('sbi.chartengine.configuration.color'),
		initiator: null
	},
	constructor : function(config) {
		this.callParent(config);
		Ext.apply(this.config,config);
		
		var globalScope = this;
		
		this.viewModel = config.viewModel;
		
		var label = this.config.customLabel ? this.config.customLabel : this.config.label;
		
		var picker = Ext.create('Sbi.chart.designer.ColorPicker',{
			viewModel : this.viewModel,
			fieldBind: this.config.fieldBind
		});
		
		var field = Ext.create('Ext.form.Field',{
			readOnly : true,
			//fieldLabel : this.config.customLabel ? this.config.customLabel : LN('sbi.chartengine.configuration.color'),
			fieldLabel: this.config.isColorMandatory ? 
					label + Sbi.settings.chart.configurationStep.htmlForMandatoryFields : 
						label,
			bind: {
				fieldStyle : 'background-image: none; background-color: ' + this.config.fieldBind,
			},
			
			width:275,
			
			listeners:
			{				
				render: function()
				{					
					globalScope.fireEvent("colorRendered", globalScope.config.initiator);
				}
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
					field.labelEl.update(label + ":"); 
				}					
			}
		);	
		
		this.add(field);
		this.add(picker);
	}
});