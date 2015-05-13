Ext.define('Sbi.chart.designer.ColorPicker',{
	extend : 'Ext.button.Button',
	margin : '0 0 0 -15',
	padding : '1 0',
	
	constructor : function(config) {
		this.callParent(config);
		
		this.viewModel = config.viewModel;
		
		var menu = Ext.create('Ext.menu.ColorPicker',{
			value : config.bind ? config.bind : '000000',
			listeners : {
				select : function(picker,selColor) {
					var style = 'background-color: #'+ selColor 
						+ '; background-image: none;';											
					this.findParentByType('container').down('field').setFieldStyle(style);
				}
			}});
		
        this.setMenu(menu);
    }
});