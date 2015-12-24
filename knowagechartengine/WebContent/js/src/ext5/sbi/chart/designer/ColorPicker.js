Ext.define('Sbi.chart.designer.ColorPicker',{
	extend : 'Ext.button.Button',	
	
	//padding : '1 0 5 0',	// Danilo
	height: 20,				// Danilo (button should be the same height of the color field)
	margin : '0 0 0 -20',	// Danilo
	
	constructor : function(config) {
		this.callParent(config);
		
		var vm = config.viewModel;
		var fb = config.fieldBind;

		var globalScope = this;
		
		var menu = Ext.create('Ext.menu.ColorPicker', {
			listeners : {
				select : function(picker,selColor) {
					var style = 'background-image:none;background-color: #'+ selColor;											
					this.findParentByType('container').down('field').setFieldStyle(style);

					var bindValue = fb.replace(/\{\w+\.(\w+)\}/, '$1');
					
					vm.data.configModel.data[bindValue] = selColor;
					
					/**
					 * Important when this component is mandatory for the chart.
					 * Firing an event will inform us that color is picked and we
					 * don't need the flag that warns the user.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					globalScope.fireEvent("colorPicked");
				}
			},
		});
		this.setMenu(menu);
    }
});