Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
//    extend: 'Ext.tab.Panel',	// version 2
	extend: 'Ext.panel.Panel',	// version 1
    xtype: 'layout-column',		// version 1	
    requires: [
               'Sbi.chart.designer.ChartConfigurationHeatmapLegendAndTooltip',
               'Sbi.chart.designer.ChartConfigurationLegend',
               'Sbi.chart.designer.ChartConfigurationPalette',
               'Sbi.chart.designer.ChartConfigurationParallelAxesLines',
               'Sbi.chart.designer.ChartConfigurationParallelLimit',
               'Sbi.chart.designer.ChartConfigurationParallelTooltip',
               'Sbi.chart.designer.ChartConfigurationScatterConfiguration',
               'Sbi.chart.designer.ChartConfigurationSunburstToolbarAndTip',
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
    overflowX: "auto",
	overflowY: "auto",
    
    constructor: function(config) {
        this.callParent(config);
        
        this.viewModel = config.viewModel;
        
        var legend = Ext.create('Sbi.chart.designer.ChartConfigurationLegend',{
        	margin: config.margin,
			viewModel: this.viewModel
		});
		var palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette',{
			margin: config.margin,
			colorPalette: this.viewModel.data.configModel.data.colorPalette
		});
		
		/**
		 * This panel is needed for the SUNBURST chart
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var toolbarAndTip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationSunburstToolbarAndTip",
			
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
		
		var parallelChartLegend = Ext.create
		(
				"Sbi.chart.designer.ChartConfigurationParallelLegend",
				
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
		var heatmapChartLegendAndTooltip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationHeatmapLegendAndTooltip",
			
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
		this.add(heatmapChartLegendAndTooltip);
		this.add(toolbarAndTip);	
		this.add(wordCloudParameters);
		this.add(parallelChartLimit);	
		this.add(parallelChartAxesLines);
		this.add(parallelChartTooltip);
		this.add(parallelChartLegend);
		this.add(scatterConfiguration);
		this.add(gaugePaneParameters);
		
		if (ChartUtils.enableLegend())
		{
			this.getComponent("chartLegend").show();
		}
		else 
		{
			this.getComponent("chartLegend").hide();
		}
		
		if (ChartUtils.enablePalette())
		{
			this.getComponent("chartColorPallete").show();
		}
		else 
		{
			this.getComponent("chartColorPallete").hide();
		}	
		
		if (ChartUtils.enableToolbarAndTip())
		{
			this.getComponent("chartToolbarAndTip").show();
		}
		else 
		{
			this.getComponent("chartToolbarAndTip").hide();
		}
		
		if (ChartUtils.enableWordcloudPanel())
		{
			this.getComponent("wordcloudConfiguration").show();
		}
		else 
		{
			this.getComponent("wordcloudConfiguration").hide();
		}
		
		if (ChartUtils.enableParallelPanel())
		{
			this.getComponent("chartParallelLimit").show();
			this.getComponent("chartParallelAxesLines").show();
			this.getComponent("chartParallelTooltip").show();
			this.getComponent("chartParallelLegend").show();			
		}
		else 
		{
			this.getComponent("chartParallelLimit").hide();
			this.getComponent("chartParallelAxesLines").hide();
			this.getComponent("chartParallelTooltip").hide();
			this.getComponent("chartParallelLegend").hide();
		}
		
		if (ChartUtils.enableHeatmapLegendAndTooltip())
		{
			this.getComponent("chartHeatmapLegendAndTooltip").show();
		}
		else
		{
			this.getComponent("chartHeatmapLegendAndTooltip").hide();
		}
		
		if (ChartUtils.enableScatterElements())
		{
			this.getComponent("chartScatterConfiguration").show();
		}
		else
		{
			this.getComponent("chartScatterConfiguration").hide();
		}
		
		if (ChartUtils.enableGaugePane())
		{
			this.getComponent("gaugePaneConfiguration").show();
		}
		else
		{
			this.getComponent("gaugePaneConfiguration").hide();
		}
    }
    
});