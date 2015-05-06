Ext.define('Sbi.chart.designer.ChartConfiguration',{
	extend: 'Ext.panel.Panel',
	border: false,
	layout: 'vbox',
	item: [ ],
	defaults:{
        width: 850,
    },
    constructor: function(config) {
    	this.title = config.title && config.title != null ? config.title: this.title;
        this.callParent(config);
        var main = Ext.create('Sbi.chart.designer.ChartConfigurationMainContainer');
		var second = Ext.create('Sbi.chart.designer.ChartConfigurationSecondContainer');
        
        this.add(main);
		this.add(second);
    }
})