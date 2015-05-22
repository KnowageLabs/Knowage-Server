Ext.define('Sbi.chart.viewer.HighchartsDrilldownHelper', {
	
	extend: 'Ext.util.Observable',
	
	statics: {
		breadcrumb: [],
		
		drilldown: function(selectedName, selectedSerie){
			var drill = {
					selectedName: selectedName,
					selectedSerie: selectedSerie
			};
			Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb.push(drill);
		},
		
		drillup: function(){
			Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb.pop();
		}
	}
	
});