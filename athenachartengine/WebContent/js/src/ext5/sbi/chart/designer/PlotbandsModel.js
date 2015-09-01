/**
 * Model for the plotbands of the GAUGE chart
 * 
 * Author: danilo.ristovski@mht.net
 */

Ext.define
(
	"Sbi.chart.designer.PlotbandsModel",
	
	{
		extend: "Ext.data.Model",
		
		fields: 
		[
		 	'idPlot', 'from', 'to', 'color'
		 ]
	}	
);