Ext.define
(
	"Sbi.chart.designer.ChartConfigurationHeatmapLegendAndTooltip", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartHeatmapLegendAndTooltip",
		requires: [
		           'Sbi.chart.designer.StylePopup',
		           'Sbi.chart.designer.StylePopupLegendHeatmap'
		           ],
		  
       /**
	   	 * NOTE: 
	   	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
	   	 * Instead of using dynamic width for this panel that relies
	   	 * on the width of the width of the window of the browser, fix this
	   	 * value so it can be entirely visible to the end user. Also the
	   	 * height will be defined as the fixed value.
	   	 * 
	   	 * @author: danristo (danilo.ristovski@mht.net)
	   	 */        
       columnWidth: 1,
//        width: 200,
        height: 110,
        
		title: LN("sbi.chartengine.configuration.heatmap.panelTitle"),
		items: [],
		
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
			
			var globalScope = this;
			
			this.heatmapChartLegend = null;
			this.heatmapChartTooltip = null;
			
			this.heatmapChartLegend = Ext.create
			(
				'Sbi.chart.designer.StylePopupLegendHeatmap',
				
				{
					title: LN('sbi.chartengine.configuration.legendstyle'), 
		    	    viewModel: this.viewModel,		
		    	    bindFontAlign:'{configModel.legendAlign}',
		    	    bindSymbolHeight:'{configModel.symbolHeight}'
				}
			);
			
			this.heatmapChartTooltip = Ext.create
			(
				'Sbi.chart.designer.StylePopupTooltipHeatmap',
				
				{
		        	title: LN("sbi.chartengine.configuration.heatmap.tooltipPopupTitle"), 
		        	viewModel: this.viewModel,
		        	bindFont:'{configModel.tipFontFamily}',
		        	bindFontDim:'{configModel.tipFontSize}',
		        	bindColor:'{configModel.tipColor}', 
		        	bindFontStyle:'{configModel.tipFontWeight}', // (does not work)
//		        	isFontFamilyMandatory: true,
//		        	isFontSizeMandatory: true,
//		        	isFontColorMandatory: true
	        		
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
//							text: Sbi.chart.designer.Designer.htmlForMandatoryFields + LN("sbi.chartengine.configuration.heatmap.labelLegend"),
							html: LN("sbi.chartengine.configuration.heatmap.labelLegend") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":",
							padding: "3 0 0 0"
							//     margin: '0 0 0 10'
						},
		 			 
						{
                    		 xtype : 'button',
                    		 text: LN("sbi.chartengine.configuration.heatmap.legendButtonText"), 
                    		 margin: '5 20 5 30',
                    		 
                    		 handler: function()
                    		 {
                    			 globalScope.heatmapChartLegend.show();
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
						//	text: Sbi.chart.designer.Designer.htmlForMandatoryFields + LN("sbi.chartengine.configuration.heatmap.tooltipLegend"),
							html: LN("sbi.chartengine.configuration.heatmap.tooltipLegend") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":",
							padding: "3 0 0 0"
							//     margin: '0 0 0 10'
						},
					 
						{
				     		 xtype : 'button',				     	
				     		 text: LN("sbi.chartengine.configuration.heatmap.tooltipButtonText"),
				     		 margin: '0 0 0 30',
				     		 
				     		 handler: function()
				     		 {
				     			globalScope.heatmapChartTooltip.show();
				     		 }
				     	 }
					 ]
				 }	
			 ];
			
			this.add(item);
		}
});