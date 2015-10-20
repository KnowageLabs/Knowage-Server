Ext.define('Sbi.chart.designer.ChartAxisesContainer', {
	extend: 'Ext.panel.Panel',
	requires: [
		'Ext.layout.container.HBox',
		'Sbi.chart.designer.ChartColumnsContainerManager',
		'Sbi.chart.designer.ChartUtils',
	],
	alternateClassName: ['ChartAxisesContainer'],
	
	/**
	 * We need to disable the border in order to hide the
	 * horizontal line that appears on the place of additional
	 * Y-axis panels (when we add ones by clicking on the plus 
	 * button on the left Y-axis panel) when we close them.
	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	border: false,
	
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
			
			// (danilo.ristovski@mht.net)
			var config = 
			{
				"idAxisesContainer":panel.id,
				"id": '', 
				"panelWhereAddSeries":null, 
				"isDestructible":true, 
				"dragGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure,
				"dropGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure
			};
			
			var newPanel = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(config);
			
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