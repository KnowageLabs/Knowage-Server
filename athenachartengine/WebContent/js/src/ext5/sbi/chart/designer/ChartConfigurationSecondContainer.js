Ext.define('Sbi.chart.designer.ChartConfigurationSecondContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'layout-column',
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
		
		var toolbarAndTip = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationToolbarAndTip",
			{
				viewModel: this.viewModel
			}
		); 
		
		var wordCloudParameters = Ext.create
		(
			"Sbi.chart.designer.ChartConfigurationWordcloud",
			{
				viewModel: this.viewModel
			}
		); 
		
		this.add(toolbarAndTip);
		this.add(palette);
		this.add(legend);
		this.add(wordCloudParameters);
		
		if (ChartUtils.enableToolbarAndTip())
		{
			this.getComponent("chartToolbarAndTip").show();
		}
		else 
		{
			this.getComponent("chartToolbarAndTip").hide();
		}
		
		if (ChartUtils.enablePalette())
		{
			this.getComponent("chartColorPallete").show();
		}
		else 
		{
			this.getComponent("chartColorPallete").hide();
		}
		
		if (ChartUtils.enableLegend())
		{
			this.getComponent("chartLegend").show();
		}
		else 
		{
			this.getComponent("chartLegend").hide();
		}
		
		if (ChartUtils.enableWordcloudPanel())
		{
			this.getComponent("wordcloudConfiguration").show();
		}
		else 
		{
			this.getComponent("wordcloudConfiguration").hide();
		}
    }
    
});