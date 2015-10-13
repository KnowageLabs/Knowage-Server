Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelTooltip", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartParallelTooltip",
		
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
//		columnWidth: 0.5,
		width: 500,
		height: 220,
		
		title: LN("sbi.chartengine.configuration.parallel.tooltip.title"), 
		bodyPadding: 10,
		items: [],
			
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