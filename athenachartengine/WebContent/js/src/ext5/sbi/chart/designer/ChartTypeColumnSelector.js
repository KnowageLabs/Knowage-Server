Ext.define('Sbi.chart.designer.ChartTypeColumnSelector', {
    extend: 'Ext.panel.Panel',
    xtype: 'layout-border',
    requires: [
        'Ext.layout.container.Border'
    ],

    config: {
    	region: 'west',
    	
    	chartTypeSelector: {
        	region: 'north',
        	margin: '0 0 5 0',
        	minHeight: 200,
        	html: '<p>Chart type selector</p>'
        },
        
    	columnsPicker: {
            region: 'center',
            margin: '0 0 5 0',
            minHeight: 200,
            html: '<p>Columns picker</p>'
        },
        
		categoriesPicker: {
            region: 'south',
            margin: '0 0 5 0',
            minHeight: 200,
            html: '<p>Axises picker</p>'
        }
    },
    
    collapsible: true,
    scrollable: true,
//    overflowY: 'auto',
//    overflowX: 'auto',
	maxWidth: 220,
	minWidth: 220,
	width: 220,
    bodyBorder: true,
    defaults: {
        split: true,
    },
    constructor: function(config) {
        this.callParent(config);

        this.add(config.chartTypeSelector ? config.chartTypeSelector : this.chartTypeSelector);
        this.add(config.columnsPicker ? config.columnsPicker : this.columnsPicker);
        this.add(config.categoriesPicker ? config.categoriesPicker : this.categoriesPicker);
    },
    items: [],
});