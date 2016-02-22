Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
	
//    extend: 'Ext.tab.Panel',	// version 2
	extend: 'Ext.panel.Panel',	// version 1    
	xtype: 'layout-column',		// version 1	
    
    requires: 
	[
	//               'Sbi.chart.designer.ChartConfigurationHeatmapLegendAndTooltip',
		'Sbi.chart.designer.ChartConfigurationHeatmapLegend',
		'Sbi.chart.designer.ChartConfigurationHeatmapTooltip',
		
		'Sbi.chart.designer.ChartConfigurationLegend',
		'Sbi.chart.designer.ChartConfigurationPalette',
		
		'Sbi.chart.designer.ChartConfigurationParallelAxesLines',
		'Sbi.chart.designer.ChartConfigurationParallelLimit',
		'Sbi.chart.designer.ChartConfigurationParallelTooltip',
		'Sbi.chart.designer.ChartConfigurationParallelLegendTitle',
		'Sbi.chart.designer.ChartConfigurationParallelLegendElement',
		
		
		'Sbi.chart.designer.ChartConfigurationScatterConfiguration',
		
	//               'Sbi.chart.designer.ChartConfigurationSunburstToolbarAndTip',		
		'Sbi.chart.designer.ChartConfigurationSunburstToolbar',
		'Sbi.chart.designer.ChartConfigurationSunburstTip',
		
		'Sbi.chart.designer.ChartConfigurationWordcloud'
   ],
           
    border:false,
    
    layout: 'column',	// version 1
//    layout: 'tab',	// version 2
    
    defaults:{
        height: 200,
    },
    item: [ ],  
    
    /**
	 * NOTE: 
	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
	 * Allow vertical and horizontal scroll bar appearance for the second
	 * configuration panel (the one that lies under the generic (main) one
	 * and that contains chart-specific sub-panels) on the Step 2 when its 
	 * item are not visible anymore due to resizing of the window of the 
	 * browser.
	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
//    	overflowX: "auto",
//		overflowY: "auto",
    
    constructor: function(config) {
        this.callParent(config);

        this.viewModel = config.viewModel;
        
        /**
         * If the "showLegend" parameter is false or undefined, the temporary parameter's value 
         * for the indication whether the Legend panel should be collapsed is true. Otherwise, 
         * take the boolean value of the parameter (no matter if it is true or false and if this 
         * value is in a form of a string or boolean).
         * 
         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
         */
        var legendPanelCollapsed = !this.viewModel.data.configModel.get("showLegend") ? true : JSON.parse(this.viewModel.data.configModel.get("showLegend"));
        
        var legend = Ext.create('Sbi.chart.designer.ChartConfigurationLegend',{
        	margin: config.margin,
        	collapsed: legendPanelCollapsed,
			viewModel: this.viewModel
		});
        this.legend = legend;
        
		var palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette',{
			margin: config.margin,			
			colorPalette: this.viewModel.data.configModel.data.colorPalette
		});
		
		/**
		 * This panel is needed for the SUNBURST chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var sunburstToolbar = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationSunburstToolbar",
			
			{
				viewModel: this.viewModel
			}
		); 
		
		/**
		 * This panel is needed for the SUNBURST chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var sunburstTip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationSunburstTip",
			
			{
				viewModel: this.viewModel
			}
		); 
		
		/**
		 * This panel is needed for the WORDCLOUD chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var wordCloudParameters = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationWordcloud",
			
			{
				viewModel: this.viewModel
			}
		); 		
		
		/**
		 * These three panels are needed for the PARALLEL chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var parallelChartLimit = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationParallelLimit",
			
			{
				viewModel: this.viewModel
			}
		);
		
		var parallelChartAxesLines = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationParallelAxesLines",
			
			{
				viewModel: this.viewModel
			}
		);
		
		var parallelChartTooltip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationParallelTooltip",
			
			{
				viewModel: this.viewModel
			}
		);
		
		var parallelChartLegendTitle = Ext.create
		(
				"Sbi.chart.designer.ChartConfigurationParallelLegendTitle",
				
				{
					viewModel: this.viewModel
				}
		);
		
		var parallelChartLegendElement = Ext.create
		(
				"Sbi.chart.designer.ChartConfigurationParallelLegendElement",
				
				{
					viewModel: this.viewModel
				}
		);
			
		// (danilo.ristovski@mht.net)
		var scatterConfiguration = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationScatterConfiguration",
			
			{
				viewModel: this.viewModel
			}	
		);
		
		
		/**
		 * These three panels are needed for the HEATMAP chart
		 * (danilo.ristovski@mht.net)
		 */
		var heatmapChartLegend = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationHeatmapLegend",
			
			{
				viewModel: this.viewModel
			}	
		);
		
		/**
		 * These three panels are needed for the HEATMAP chart
		 * (danilo.ristovski@mht.net)
		 */
		var heatmapChartTooltip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationHeatmapTooltip",
			
			{
				viewModel: this.viewModel
			}	
		);
		
		/**
		 * This panel is needed for the SCATTER chart
		 * (danilo.ristovski@mht.net)
		 */
		var gaugePaneParameters = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationGauge",
			
			{
				viewModel: this.viewModel
			}
		); 
		
		this.add(legend);
		this.add(palette);
		
		/**
		 * HEATMAP panels for specific parameters
		 */
		this.add(heatmapChartLegend);
		this.add(heatmapChartTooltip);
		
		/**
		 * SUNBURST panels for specific parameters
		 */
		this.add(sunburstToolbar);	
		this.add(sunburstTip);
		
		/**
		 * WORDCLOUD panel for specific parameters
		 */
		this.add(wordCloudParameters);
		
		/**
		 * PARALLEL panels for specific parameters
		 */
		this.add(parallelChartLimit);	
		this.add(parallelChartAxesLines);
		this.add(parallelChartTooltip);
		this.add(parallelChartLegendTitle);
		this.add(parallelChartLegendElement);
		
		/**
		 * SCATTER panel for specific parameters
		 */
		this.add(scatterConfiguration);
		
		/**
		 * GAUGE panel for specific parameters
		 */
		this.add(gaugePaneParameters);
		
		if (ChartUtils.isLegendEnabled())
		{
			this.getComponent("chartLegend").show();
		}
		else 
		{
			this.getComponent("chartLegend").hide();
		}
		
		if (ChartUtils.isPaletteEnabled())
		{
			this.getComponent("chartColorPalette").show();
		}
		else 
		{
			this.getComponent("chartColorPalette").hide();
		}	
		
		if (ChartUtils.isToolbarAndTipEnabled())
		{
//			this.getComponent("chartToolbarAndTip").show();
			this.getComponent("chartToolbar").show();
			this.getComponent("chartTip").show();
		}
		else 
		{
//			this.getComponent("chartToolbarAndTip").hide();
			this.getComponent("chartToolbar").hide();
			this.getComponent("chartTip").hide();
		}
		
		if (ChartUtils.isWordcloudPanelEnabled())
		{
			this.getComponent("wordcloudConfiguration").show();
		}
		else 
		{
			this.getComponent("wordcloudConfiguration").hide();
		}
		
		if (ChartUtils.isParallelPanelEnabled())
		{
			this.getComponent("chartParallelLimit").show();
			this.getComponent("chartParallelAxesLines").show();
			//this.getComponent("chartParallelTooltip").show();
			this.getComponent("chartParallelLegendTitle").show();	
			this.getComponent("chartParallelLegendElement").show();	
		}
		else 
		{
			this.getComponent("chartParallelLimit").hide();
			this.getComponent("chartParallelAxesLines").hide();
			//this.getComponent("chartParallelTooltip").hide();
			this.getComponent("chartParallelLegendTitle").hide();	
			this.getComponent("chartParallelLegendElement").hide();	
		}
		
		if(ChartUtils.isTooltipPanelEnabled()){
			this.getComponent("chartParallelTooltip").show();
		}else{
			this.getComponent("chartParallelTooltip").hide();
		}
		
		if (ChartUtils.isHeatmapLegendAndTooltipEnabled())
		{
//			this.getComponent("chartHeatmapLegendAndTooltip").show();
			this.getComponent("chartHeatmapLegend").show();
			this.getComponent("chartHeatmapTooltip").show();
		}
		else
		{
//			this.getComponent("chartHeatmapLegendAndTooltip").hide();
			this.getComponent("chartHeatmapLegend").hide();
			this.getComponent("chartHeatmapTooltip").hide();
		}
		
		if (ChartUtils.isScatterElementsEnabled())
		{
			this.getComponent("chartScatterConfiguration").show();
		}
		else
		{
			this.getComponent("chartScatterConfiguration").hide();
		}
		
		if (ChartUtils.isGaugePaneEnabled())
		{
			this.getComponent("gaugePaneConfiguration").show();
		}
		else
		{
			this.getComponent("gaugePaneConfiguration").hide();
		}
    }
    
});