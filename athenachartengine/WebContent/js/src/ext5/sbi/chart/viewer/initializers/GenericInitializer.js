Ext.define("Sbi.chart.viewer.initializers.GenericInitializer", {
	initChartLibrary: function(panelId) {
		Ext.log({level: 'error'}, 'You must define your library specific initChartLibrary method!');
	},	
	renderChart: function(chartConf) {
		Ext.log({level: 'error'}, 'You must define your library specific initChartLibrary method!');
	}
});