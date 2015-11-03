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
		height: 230,
		
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
			
			this.tooltipFontFamily = null;
			this.tooltipFontSize = null;
			this.tooltipMinWidth = null;
			this.tooltipMaxWidth = null;
			this.tooltipMinHeight = null;
			this.tooltipMaxHeight = null;
			this.tooltipPadding = null;
			this.tooltipBorder = null;
			this.tooltipBorderRadius = null;
			
			this.tooltipFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.parallelTooltipFontFamily}',
					viewModel: this.viewModel,
					fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
				}	
			);
	        
			this.tooltipFontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.parallelTooltipFontSize}',
    				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    			}
			);
			
			this.tooltipMinWidth = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipMinWidth}',
					 id: "parallelTooltipMinWidth",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinWidth") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "20",
					 maxValue: '100',
					 minValue: '10'
				}
			);
			
			this.tooltipMaxWidth = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipMaxWidth}',
					 id: "parallelTooltipMaxWidth",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxWidth") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
					 width: "200",
//					 value: "300",
					 maxValue: '500',
					 minValue: '50'
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
					  	this.tooltipMinWidth,
					  	this.tooltipMaxWidth						
					 ]
				}
			 ];
			
			this.tooltipMinHeight = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipMinHeight}',	
					 id: "parallelTooltipMinHeight",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinHeight") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "20",
					 maxValue: '50',
					 minValue: '5'
				}
			);
			
			this.tooltipMaxHeight = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipMaxHeight}',
					 id: "parallelTooltipMaxHeight",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxHeight") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "300",
					 maxValue: '500',
					 minValue: '50'
				}
			);
			
			
			
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
						this.tooltipMinHeight,
						this.tooltipMaxHeight
					]
				}
			 ];
			
			this.tooltipPadding = Ext.create
			( 			
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipPadding}',	
					 id: "parallelTooltipPadding",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipPadding") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "2",
					 maxValue: '20',
					 minValue: '0'
				}
			 );
			
			this.tooltipBorder = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipBorder}',
					 id: "parallelTooltipBorder",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "0",
					 maxValue: '10',
					 minValue: '0'
				}
			);
			
			this.tooltipBorderRadius = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipBorderRadius}',
					 id: "parallelTooltipBorderRadius",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "5",
					 maxValue: '20',
					 minValue: '0'
				}
			);
			
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
					  	this.tooltipBorder,
					  	this.tooltipBorderRadius
					]
				}
			 ];
						
			this.add(this.tooltipFontFamily);
			this.add(this.tooltipFontSize);
			this.add(tooltipWidth);
			this.add(tooltipHeight);
			this.add(this.tooltipPadding);
			this.add(tooltipBorder);
		}
});