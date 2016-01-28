Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelTooltip", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
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
		columnWidth: 1,
//		width: 500,
		height: 170,
		
		title: LN("sbi.chartengine.configuration.parallel.tooltip.title"), 
		bodyPadding: 10,
		items: [],
			
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
	/*	layout: 
		{
		     type: 'vbox'
		},*/
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			this.tooltipFontFamily = null;
			this.tooltipFontSize = null;
		//	this.tooltipMinWidth = null;
		//	this.tooltipMaxWidth = null;
		//	this.tooltipMinHeight = null;
		//	this.tooltipMaxHeight = null;
		//	this.tooltipPadding = null;
			this.tooltipBorder = null;
			this.tooltipBorderRadius = null;
			
			this.tooltipFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.parallelTooltipFontFamily}',
					viewModel: this.viewModel,
					fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
					width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
					listeners:
					{
						fontFamilyPicked: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.font') + ":"); 
						},
				
						fontFamilyEmpty: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
						}
					}
				}	
			);
	        
			this.tooltipFontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.parallelTooltipFontSize}',
    				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
    				width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
    				listeners:
					{
    					fontSizePicked: function()
    					{
    						this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + ":"); 
    					},
    					
    					fontSizeEmpty: function()
    					{
    						this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					}
					}
    			}
			);			
		
			this.tooltipBorder = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipBorder}',
					 id: "parallelTooltipBorder",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: Sbi.settings.chart.configurationStep.widthOfFields,
	        		 padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
//					 value: "0",
					 maxValue: '10',
					 //columnWidth: 1,
					 minValue: '0',
					//labelWidth: 100,
					// labelPad: 10,
					 emptyText: LN("sbi.chartengine.configuration.parallelTooltipBorderWidth.emptyText"),
					 
					 listeners:
					 {
						 change: function(thisEl, newValue, oldValue)
						 {							 
							 if (newValue || parseInt(newValue)==0)
							 {
								 this.labelEl.update(LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder")+":"); 
							 }								 
							 else
							 {
								 this.labelEl.update
								 	(LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
							 }								 								 				 
						 }
					 }
				}
			);
			
			this.tooltipBorderRadius = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.parallelTooltipBorderRadius}',
					 id: "parallelTooltipBorderRadius",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: Sbi.settings.chart.configurationStep.widthOfFields,
	        		 padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
//					 value: "5",
					 maxValue: '20',
					 minValue: '0',
					// labelWidth: 100,
					// labelPad: 10,
					 emptyText: LN("sbi.chartengine.configuration.parallelTooltipBorderRadius.emptyText"),
					 
					 listeners:
					 {
						 change: function(thisEl, newValue, oldValue)
						 {							 
							 if (newValue || parseInt(newValue)==0)
							 {
								 this.labelEl.update(LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius")+":"); 
							 }								 
							 else
							 {
								 this.labelEl.update
								 	(LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
							 }								 								 				 
						 }
					 }
				}
			);
			
			var tooltipBorder = 
			[			 
				{            
					 xtype : 'fieldcontainer',
					 layout: {                        
					        type: 'vbox',
					        align: 'center'
					    },
					// bodyPadding: 10,
					 fieldDefaults: 
					    {
					        anchor: '100%'
						},
					/* defaults : 
					 {
						// labelWidth : '100%',
						
					 },*/
				       	 
					 items: 
					 [				 
					  	this.tooltipBorder,
					  	this.tooltipBorderRadius
					]
				}
			 ];
						
			this.add(this.tooltipFontFamily);
			this.add(this.tooltipFontSize);
			//this.add(tooltipWidth);
			//this.add(tooltipHeight);
			//this.add(this.tooltipPadding);
			//this.add(tooltipBorder);
			this.add(this.tooltipBorder);
			this.add(this.tooltipBorderRadius);
			
		}
});