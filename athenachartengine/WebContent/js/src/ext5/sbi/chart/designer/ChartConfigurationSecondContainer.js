Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'layout-column',
    requires: [
               'Sbi.chart.designer.ChartConfigurationHeatmapLegendAndTooltip',
               'Sbi.chart.designer.ChartConfigurationLegend',
               'Sbi.chart.designer.ChartConfigurationPalette',
               'Sbi.chart.designer.ChartConfigurationParallelAxesLines',
               'Sbi.chart.designer.ChartConfigurationParallelLimit',
               'Sbi.chart.designer.ChartConfigurationParallelTooltip',
               'Sbi.chart.designer.ChartConfigurationScatterConfiguration',
               'Sbi.chart.designer.ChartConfigurationToolbarAndTip',
               'Sbi.chart.designer.ChartConfigurationWordcloud',
           ],
    border:false,
    layout: 'column',
    defaults:{
        height: 200,
    },
    item: [ ],  
    
    constructor: function(config) {
        this.callParent(config);
        
        this.viewModel = config.viewModel;
        
        var legend = Ext.create('Sbi.chart.designer.ChartConfigurationLegend',{
			viewModel: this.viewModel
		});
		var palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette',{
			colorPalette: this.viewModel.data.configModel.data.colorPalette
		});
		
		/**
		 * This panel is needed for the SUNBURST chart
		 * (danilo.ristovski@mht.net)
		 */
		var toolbarAndTip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationToolbarAndTip",
			
			{
				viewModel: this.viewModel
			}
		); 
		
		/**
		 * This panel is needed for the WORDCLOUD chart
		 * (danilo.ristovski@mht.net)
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
		 * (danilo.ristovski@mht.net)
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
		
		this.add(legend);
		this.add(palette);
		this.add(heatmapChartLegendAndTooltip);
		this.add(toolbarAndTip);	
		this.add(wordCloudParameters);
		this.add(parallelChartLimit);	
		this.add(parallelChartAxesLines);
		this.add(parallelChartTooltip);
		this.add(scatterConfiguration);
		
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
		}
		else 
		{
			this.getComponent("chartParallelLimit").hide();
			this.getComponent("chartParallelAxesLines").hide();
			this.getComponent("chartParallelTooltip").hide();
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
    }
    
});