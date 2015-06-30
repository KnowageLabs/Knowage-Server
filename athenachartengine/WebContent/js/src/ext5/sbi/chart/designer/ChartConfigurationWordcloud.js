Ext.define
(
	"Sbi.chart.designer.ChartConfigurationWordcloud", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "wordcloudConfiguration",
		columnWidth: 0.2,
		title: LN("sbi.chartengine.configuration.wordcloud.configPanelTitle"),
		bodyPadding: 10,
		items: [],
		height: 225,
		
		requires : [
			            'Sbi.chart.designer.StylePopupTip',
			            'Sbi.chart.designer.StylePopupToolbar'],
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		layout: 
		{
		    type: 'vbox',
		    //align: 'center'
		},
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;	
									
	        var item = 
        	[        	 	
        	 	{
            		 xtype: 'numberfield',
            		 bind : '{configModel.maxWords}',	
            		 fieldLabel: LN("sbi.chartengine.configuration.wordcloud.maxWords"),
            		 width: "200",
            		 value: "10",
            		 maxValue: '100',
            		 minValue: '10'
            	},
  		  		
            	{
            		 xtype: 'numberfield',
            		 bind : '{configModel.maxAngle}',	
            		 fieldLabel: LN("sbi.chartengine.configuration.wordcloud.maxAngle"),
            		 width: "200",
            		 value: "60",
            		 maxValue: '360',
            		 minValue: '60'
            	},            	
  		  		
            	{
            		 xtype: 'numberfield',
            		 bind : '{configModel.minAngle}',	
            		 fieldLabel: LN("sbi.chartengine.configuration.wordcloud.minAngle"),
            		 width: "200",
            		 value: "0",
            		 maxValue: '270',
            		 minValue: '0'
            	},
            	
            	{
            		 xtype: 'numberfield',
            		 bind : '{configModel.maxFontSize}',	
            		 fieldLabel: LN("sbi.chartengine.configuration.wordcloud.maxFontSize"),
            		 width: "200",
            		 value: "100",
            		 maxValue: '200',
            		 minValue: '50'
            	},
            	
            	{
            		 xtype: 'numberfield',
            		 bind : '{configModel.wordPadding}',	
            		 fieldLabel: LN("sbi.chartengine.configuration.wordcloud.wordPadding"),
            		 width: "200",
            		 value: "10",
            		 maxValue: '20',
            		 minValue: '2'
            	}
            ];
	        	        
	        this.add(item);
		}
});