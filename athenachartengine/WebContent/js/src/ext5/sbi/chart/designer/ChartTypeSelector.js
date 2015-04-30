Ext.define('Sbi.chart.designer.ChartTypeSelector', {
	extend: 'Ext.panel.Panel',
    requires: [
        'Ext.layout.container.VBox',
    ],
	xtype: 'layout-vertical-box',
	layout: {
		type: 'vbox',
		pack: 'start',
		align: 'stretch'
	},
	title: 'Chart Type Selector',
	
	margin: '0 0 5 0',
	width: '100%',
	defaults: {
		frame: true,
		margin: '5 0 0 0',
	},
});