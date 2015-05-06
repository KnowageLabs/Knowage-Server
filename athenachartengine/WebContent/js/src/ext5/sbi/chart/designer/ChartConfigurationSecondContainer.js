Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'layout-column',
    border:false,
    requires: [
        'Ext.layout.container.Column'
    ],
    
    layout: 'column',
    defaults:{
        height: 200,
    },
    item: [ ],
    
    constructor: function(config) {
        this.callParent(config);
        var legend = Ext.create('Sbi.chart.designer.ChartConfigurationLegend');
		var palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette');
        
        this.add(legend);
		this.add(palette);
    }
    
});