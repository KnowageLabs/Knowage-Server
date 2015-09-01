Ext.define
(
	'Sbi.chart.designer.PlotbandsStore', 
	
	{
	    extend: 'Ext.data.Store',
	    id: "plotbandsStore",
			
	    model: Sbi.chart.designer.PlotbandsModel,
	    
	    proxy: 
	    {
	        type: 'memory'
	    }
	}
);