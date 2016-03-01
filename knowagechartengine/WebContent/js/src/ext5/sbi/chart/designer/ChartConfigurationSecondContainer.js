Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
	
//    extend: 'Ext.tab.Panel',	// version 2
	extend: 'Ext.panel.Panel',	// version 1    
	xtype: 'layout-column',		// version 1	
	
    requires: 
	[
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
			
		'Sbi.chart.designer.ChartConfigurationSunburstToolbar',
		'Sbi.chart.designer.ChartConfigurationSunburstTip',
		
		'Sbi.chart.designer.ChartConfigurationWordcloud',
		'Sbi.chart.designer.ChartConfigurationWordcloudTooltip'	
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
        
		this.palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette',{
			margin: config.margin,			
			colorPalette: this.viewModel.data.configModel.data.colorPalette
		});
		
		/**
		 * This panel is needed for the SUNBURST chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		this.sunburstToolbar = Ext.create
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
		this.sunburstTip = Ext.create
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
		this.wordCloudParameters = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationWordcloud",
			
			{
				viewModel: this.viewModel
			}
		); 		
		
		var wordcloudTooltip=Ext.create
		(
				"Sbi.chart.designer.ChartConfigurationWordcloudTooltip",
				
				{
					viewModel: this.viewModel
				}
			); 	
		
		/**
		 * These three panels are needed for the PARALLEL chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		this.parallelChartLimit = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationParallelLimit",
			
			{
				viewModel: this.viewModel
			}
		);
		
		this.parallelChartAxesLines = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationParallelAxesLines",
			
			{
				viewModel: this.viewModel
			}
		);
		
		this.parallelChartTooltip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationParallelTooltip",
			
			{
				viewModel: this.viewModel
			}
		);
		
		this.parallelChartLegendTitle = Ext.create
		(
				"Sbi.chart.designer.ChartConfigurationParallelLegendTitle",
				
				{
					viewModel: this.viewModel
				}
		);
		
		this.parallelChartLegendElement = Ext.create
		(
				"Sbi.chart.designer.ChartConfigurationParallelLegendElement",
				
				{
					viewModel: this.viewModel
				}
		);
			
		// (danilo.ristovski@mht.net)
		this.scatterConfiguration = Ext.create
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
		this.heatmapChartLegend = Ext.create
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
		this.heatmapChartTooltip = Ext.create
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
		this.gaugePaneParameters = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationGauge",
			
			{
				viewModel: this.viewModel
			}
		); 
		
		this.add(legend);
		this.add(this.palette);
		
		/**
		 * HEATMAP panels for specific parameters
		 */
		this.add(this.heatmapChartLegend);
		this.add(this.heatmapChartTooltip);
		
		/**
		 * SUNBURST panels for specific parameters
		 */
		this.add(this.sunburstToolbar);	
		this.add(this.sunburstTip);
		
		/**
		 * WORDCLOUD panel for specific parameters
		 */
		this.add(this.wordCloudParameters);
		this.add(wordcloudTooltip);
		
		/**
		 * PARALLEL panels for specific parameters
		 */
		this.add(this.parallelChartLimit);	
		this.add(this.parallelChartAxesLines);
		this.add(this.parallelChartTooltip);
		this.add(this.parallelChartLegendTitle);
		this.add(this.parallelChartLegendElement);
		
		/**
		 * SCATTER panel for specific parameters
		 */
		this.add(this.scatterConfiguration);
		
		/**
		 * GAUGE panel for specific parameters
		 */
		this.add(this.gaugePaneParameters);
		
		if (ChartUtils.isLegendEnabled())
		{
//			this.getComponent("chartLegend").show();
//			console.log("SHOW LEGEND");
//			console.log(this.legend.show());
			this.legend.show();
		}
		else 
		{
//			this.getComponent("chartLegend").hide();
//			console.log("HIDE LEGEND");
//			console.log(this.palette);
			this.legend.hide();
		}
		
		if (ChartUtils.isPaletteEnabled())
		{
//			this.getComponent("chartColorPalette").show();
			this.palette.show();
		}
		else 
		{
//			this.getComponent("chartColorPalette").hide();
			this.palette.hide();
		}	
		
		if (ChartUtils.isToolbarAndTipEnabled())
		{
//			this.getComponent("chartToolbar").show();
//			this.getComponent("chartTip").show();
			this.sunburstTip.show();
			this.sunburstToolbar.show();
		}
		else 
		{
//			this.getComponent("chartToolbar").hide();
//			this.getComponent("chartTip").hide();
			
			this.sunburstTip.hide();
			this.sunburstToolbar.hide();
		}
		
		if (ChartUtils.isWordcloudPanelEnabled())
		{
//			this.getComponent("wordcloudConfiguration").show();
			this.wordCloudParameters.show();
			this.getComponent("wordcloudConfigurationTooltip").show();
		}
		else 
		{
//			this.getComponent("wordcloudConfiguration").hide();
			this.wordCloudParameters.hide();
			this.getComponent("wordcloudConfigurationTooltip").hide();
		}
		
		if (ChartUtils.isParallelPanelEnabled())
		{
//			this.getComponent("chartParallelLimit").show();
//			this.getComponent("chartParallelAxesLines").show();
//			//this.getComponent("chartParallelTooltip").show();
//			this.getComponent("chartParallelLegendTitle").show();	
//			this.getComponent("chartParallelLegendElement").show();	
			
			this.parallelChartAxesLines.show();
			this.parallelChartLegendElement.show();
			this.parallelChartLegendTitle.show();
			this.parallelChartLimit.show();
		}
		else 
		{
//			this.getComponent("chartParallelLimit").hide();
//			this.getComponent("chartParallelAxesLines").hide();
//			//this.getComponent("chartParallelTooltip").hide();
//			this.getComponent("chartParallelLegendTitle").hide();	
//			this.getComponent("chartParallelLegendElement").hide();	
			
			this.parallelChartAxesLines.hide();
			this.parallelChartLegendElement.hide();
			this.parallelChartLegendTitle.hide();
			this.parallelChartLimit.hide();
		}
		
		if(ChartUtils.isTooltipPanelEnabled()){
//			this.getComponent("chartParallelTooltip").show();
			this.parallelChartTooltip.show();
		}else{
//			this.getComponent("chartParallelTooltip").hide();
			this.parallelChartTooltip.hide();
		}
		
		if (ChartUtils.isHeatmapLegendAndTooltipEnabled())
		{
//			this.getComponent("chartHeatmapLegendAndTooltip").show();
//			this.getComponent("chartHeatmapLegend").show();
//			this.getComponent("chartHeatmapTooltip").show();
			this.heatmapChartLegend.show();
			this.heatmapChartTooltip.show();
		}
		else
		{
//			this.getComponent("chartHeatmapLegendAndTooltip").hide();
//			this.getComponent("chartHeatmapLegend").hide();
//			this.getComponent("chartHeatmapTooltip").hide();
			
			this.heatmapChartLegend.hide();
			this.heatmapChartTooltip.hide();
		}
		
		if (ChartUtils.isScatterElementsEnabled())
		{
//			this.getComponent("chartScatterConfiguration").show();
			this.scatterConfiguration.show();
		}
		else
		{
//			this.getComponent("chartScatterConfiguration").hide();
			this.scatterConfiguration.hide();
		}
		
		if (ChartUtils.isGaugePaneEnabled())
		{
//			this.getComponent("gaugePaneConfiguration").show();
			this.gaugePaneParameters.show();
		}
		else
		{
//			this.getComponent("gaugePaneConfiguration").hide();
			this.gaugePaneParameters.hide();
		}
    }
    
});