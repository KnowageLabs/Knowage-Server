Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelLegendTitle", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartParallelLegendTitle",
		
		columnWidth: 1,
		height: 130,
		
		title: "Legend title configuration",	// TODO: LN()
		bodyPadding: 10,
		items: [],
	
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
			
			var globalScope = this;
			
			this.stylePopupLegendTitle = null;
			this.stylePopupLegendElement = null;
			
			 var legentTitleFontStyle = Ext.create	
				(	
					'Sbi.chart.designer.FontStyleCombo',
					
					{
						bind: '{configModel.parallelLegendTitleFontWeight}',
						fieldLabel: LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						
						listeners:
						{
							fontStylePicked: function()
							{
								this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + ":"); 
							},
					
							fontStyleEmpty: function()
							{
								this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
							}
						}
					}	
				);
	        	
	        	this.add(legentTitleFontStyle);
	        	
	        	var legentTitleFontSize = Ext.create
	 	        (
	     			'Sbi.chart.designer.FontDimCombo',
	     			
	     			{
	     				bind : '{configModel.parallelLegendTitleFontSize}',
	     				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						
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
	 	        
	 	        this.add(legentTitleFontSize);
		        
	 	        var legentTitleFontFamily = Ext.create
				(	
					'Sbi.chart.designer.FontCombo',
					
					{
						bind: '{configModel.parallelLegendTitleFontFamily}',
						fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						
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
	 	        
	 	       this.add(legentTitleFontFamily);
		}
});