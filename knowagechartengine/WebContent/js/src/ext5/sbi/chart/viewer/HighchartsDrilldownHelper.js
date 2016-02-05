Ext.define('Sbi.chart.viewer.HighchartsDrilldownHelper', {
	
	extend: 'Ext.util.Observable',
	
	statics: {
		breadcrumb: [],
		
		drilldown: function(selectedName, selectedSerie){
			var drill = {
					selectedName: selectedName,
					selectedSerie: selectedSerie
			};
			
//			console.log(drill);
//			console.log(Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb.indexOf(drill));
			
			Sbi.info("IN: HighchartsDrilldownHelper (handling the clicking on the value label of the charts item)");
			
			/**
			 * The workaround solution for drilling down when clicking on the value of the chart
			 * (i.e. on the label that is linked to single bar in the BAR chart or on the label
			 * that is linked to single point inside the LINE chart), when having multiple layers.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var i = Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb.length;
			var indicator = null;
		   
			while (i--) 
		    {
		       if (JSON.stringify(Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb[i]) === JSON.stringify(drill)) 
		       {
		    	   
		    	   indicator = true;
		       }
		    }
			
//			console.log(indicator);

		    if (indicator != true)
		    	indicator = false;
			
		    if (!indicator)
			Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb.push(drill);
		},
		
		drillup: function(){
//			console.log(Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb);
			Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb.pop();
		}
	}
	
});