Ext.define
(
	"Sbi.chart.designer.ChartConfigurationHeatmapLegendAndTooltip", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartHeatmapLegendAndTooltip",
		requires: [
		           'Sbi.chart.designer.StylePopup',
		           'Sbi.chart.designer.StylePopupLegendHeatmap'
		           ],
        width: 200,
		title: LN("sbi.chartengine.configuration.heatmap.panelTitle"),
		items: [],
		height: 110,
		bodyPadding: 10,
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		layout: 
		{
		    type: 'vbox'
		},
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;			
			
			var heatmapChartLegend = Ext.create
			(
				'Sbi.chart.designer.StylePopupLegendHeatmap',
				
				{
					title:LN('sbi.chartengine.configuration.legendstyle'), 
		    	    viewModel: this.viewModel,		
		    	    bindFontAlign:'{configModel.legendAlign}',
		    	    bindSymbolHeight:'{configModel.symbolHeight}'
				}
			);
			
			var heatmapChartTooltip = Ext.create
			(
				'Sbi.chart.designer.StylePopup',
				
				{
		        	title: LN("sbi.chartengine.configuration.heatmap.tooltipPopupTitle"), 
		        	viewModel: this.viewModel,
		        	bindFont:'{configModel.tipFontFamily}',
		        	bindFontDim:'{configModel.tipFontSize}',
		        	bindColor:'{configModel.tipColor}', 
//	        		bindFontStyle:'{configModel.tipFontWeight}' // (does not work)
				}
			);	
			
			var item = 
			[
			 	{
			 		xtype : 'fieldcontainer',			 		
			 		layout : 'hbox',
			 		
			 		defaults : 
			 		{
			 			//labelWidth : '100%',
			 			// (top, right, bottom, left)
			 			margin:'5 0 5 0'
			 		},
			 		
			 		items:
		 			[
						{
							xtype: 'label',
							text: LN("sbi.chartengine.configuration.heatmap.labelLegend"),
							padding: "3 0 0 0"
							//     margin: '0 0 0 10'
						},
		 			 
						{
                    		 xtype : 'button',
                    		 text: LN("sbi.chartengine.configuration.heatmap.legendButtonText"), 
                    		 margin: '5 20 5 30',
                    		 
                    		 handler: function()
                    		 {
                    			 heatmapChartLegend.show();
                    		 }
                    	 }
		 			 ]
			 	},
               	 
				{
					xtype : 'fieldcontainer',			 		
					layout : 'hbox',
					
					defaults : 
					{
						labelWidth : '100%',
						margin: '0 0 5 0'
					},
					
					items:
					[
						{
							xtype: 'label',
							text: LN("sbi.chartengine.configuration.heatmap.tooltipLegend"),
							padding: "3 0 0 0"
							//     margin: '0 0 0 10'
						},
					 
						{
				     		 xtype : 'button',				     	
				     		 text: LN("sbi.chartengine.configuration.heatmap.tooltipButtonText"),
				     		 margin: '0 0 0 30',
				     		 
				     		 handler: function()
				     		 {
				     			heatmapChartTooltip.show();
				     		 }
				     	 }
					 ]
				 }	
			 ];
			
			this.add(item);
		}
});