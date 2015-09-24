Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelTooltip", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartParallelTooltip",
		columnWidth: 0.5,
		title: LN("sbi.chartengine.configuration.parallel.tooltip.title"), 
		bodyPadding: 10,
		items: [],
		height: 220,
	
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
			
			this.storeForSeriesBeforeDrop = Ext.data.StoreManager.lookup('storeForSeriesBeforeDrop');
			
			var tooltipFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.parallelTooltipFontFamily}',
					viewModel: this.viewModel
				}	
			);
	        
			var tooltipFontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.parallelTooltipFontSize}'
    			}
			);
			
			var tooltipWidth = 
			[			 
				{            
					 xtype : 'fieldcontainer',
					 layout : 'hbox',
					 
					 defaults : 
					 {
				//		 labelWidth : '100%',
						 margin:'0 30 0 0'
					 },
				       	 
					 items: 
					 [				 
						{
							 xtype: 'numberfield',
							 bind : '{configModel.parallelTooltipMinWidth}',	
							 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinWidth"),	
							 width: "200",
							 value: "20",
							 maxValue: '100',
							 minValue: '10'
						},
						
						{
							 xtype: 'numberfield',
							 bind : '{configModel.parallelTooltipMaxWidth}',	
							 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxWidth"),
							 width: "200",
							 value: "300",
							 maxValue: '500',
							 minValue: '50'
						}
					]
				}
			 ];
			
			var tooltipHeight = 
			[			 
				{            
					 xtype : 'fieldcontainer',
					 layout : 'hbox',
					 
					 defaults : 
					 {
				//		 labelWidth : '100%',
						 margin:'0 30 0 0'
					 },
				       	 
					 items: 
					 [				 
						{
							 xtype: 'numberfield',
							 bind : '{configModel.parallelTooltipMinHeight}',	
							 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinHeight"),	
							 width: "200",
							 value: "20",
							 maxValue: '50',
							 minValue: '5'
						},
						
						{
							 xtype: 'numberfield',
							 bind : '{configModel.parallelTooltipMaxHeight}',
							 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxHeight"),	
							 width: "200",
							 value: "300",
							 maxValue: '500',
							 minValue: '50'
						}
					]
				}
			 ];
			
			var tooltipPadding = 
			[
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipPadding}',	
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipPadding"),	
					 width: "200",
					 value: "2",
					 maxValue: '20',
					 minValue: '0'
				}
			 ];
			
			var tooltipBorder = 
				[			 
					{            
						 xtype : 'fieldcontainer',
						 layout : 'hbox',
						 
						 defaults : 
						 {
					//		 labelWidth : '100%',
							 margin:'0 30 0 0'
						 },
					       	 
						 items: 
						 [				 
							{
								 xtype: 'numberfield',
								 bind : '{configModel.parallelTooltipBorder}',	
								 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder"),	
								 width: "200",
								 value: "0",
								 maxValue: '10',
								 minValue: '0'
							},
							
							{
								 xtype: 'numberfield',
								 bind : '{configModel.parallelTooltipBorderRadius}',	
								 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius"),	
								 width: "200",
								 value: "5",
								 maxValue: '20',
								 minValue: '0'
							}
						]
					}
				 ];
						
			this.add(tooltipFontFamily);
			this.add(tooltipFontSize);
			this.add(tooltipWidth);
			this.add(tooltipHeight);
			this.add(tooltipPadding);
			this.add(tooltipBorder);
		}
});