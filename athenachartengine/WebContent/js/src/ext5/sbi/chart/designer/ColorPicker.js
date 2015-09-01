Ext.define('Sbi.chart.designer.ColorPicker',{
	extend : 'Ext.button.Button',
	margin : '0 0 0 -15',
	padding : '1 0',
	
	constructor : function(config) {
		this.callParent(config);
		
		var vm = config.viewModel;
		var fb = config.fieldBind;
		
		var menu = Ext.create('Ext.menu.ColorPicker', {
			listeners : {
				select : function(picker,selColor) {
					var style = 'background-image:none;background-color: #'+ selColor;											
					this.findParentByType('container').down('field').setFieldStyle(style);

					var bindValue = fb.replace(/\{\w+\.(\w+)\}/, '$1');
					
					vm.data.configModel.data[bindValue] = selColor;
					
					//Ext.getCmp("idchartss").fireEvent("ppp", "backgroundColor"); //danilo
				}
			},
		});
		this.setMenu(menu);
    }
});