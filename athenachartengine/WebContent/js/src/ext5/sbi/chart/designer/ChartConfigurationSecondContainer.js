Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'layout-column',
    border:false,
    layout: 'column',
    defaults:{
        height: 200,
    },
    item: [ ],
    
    constructor: function(config) {
        this.callParent(config);
        
        this.viewModel = config.viewModel;
        
        var legend = Ext.create('Sbi.chart.designer.ChartConfigurationLegend',{
			viewModel: this.viewModel
		});
		var palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette',{
			viewModel: this.viewModel
		});
        
        this.add(legend);
		this.add(palette);
    }
    
});