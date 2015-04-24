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
        	margin: '5 0 5 0',
        	minHeight: 200,
        	html: '<p>Chart type selector</p>'
        },
    	axisesPicker: {
            region: 'south',
            margin: '5 0 5 0',
            minHeight: 200,
            html: '<p>Axises picker</p>'
        }
    },

	maxWidth: 250,
	minWidth: 100,
	width: 125,

    bodyBorder: true,

    defaults: {
        collapsible: false,
        split: true,
        bodyPadding: 10
    },

    constructor: function(config) {
        this.callParent(config);

        this.add(config.chartTypeSelector ? config.chartTypeSelector : this.chartTypeSelector);
        this.add(config.axisesPicker ? config.axisesPicker : this.axisesPicker);
    },


    items: [],
});