Ext.define('Sbi.chart.designer.ChartAxisesContainer', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Ext.layout.container.HBox',
		'Sbi.chart.designer.ChartColumnsContainerManager',
		'Sbi.chart.designer.ChartUtils',
	],
	alternateClassName: ['ChartAxisesContainer'],
	
	xtype: 'layout-horizontal-box',
	layout: {
		type: 'hbox',
		pack: 'start',
		align: 'stretch'
	},
	defaults: {
	},
	
	statics: {
		addToAxisesContainer: function (panel) {
			var newPanel = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(
					panel.id , '' , null, true, 
					Sbi.chart.designer.ChartUtils.ddGroupMeasure, 
					Sbi.chart.designer.ChartUtils.ddGroupMeasure);
			if(newPanel != null) {
				panel.add(newPanel);
			}
		}
	},
	
	config: {
		otherPanel: null,
	},
	items: [],
	
});