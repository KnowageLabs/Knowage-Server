Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelLegendElement", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartParallelLegendElement",
		
		columnWidth: 1,
		height: 130,
		
		title: LN("sbi.chartengine.configuration.parallel.legendElementPanel.title"),
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
			
			var legendElementFontStyle = Ext.create	
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: '{configModel.parallelLegendElementFontWeight}',
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
        	
        	this.add(legendElementFontStyle);
        	        	
        	var legendElementFontSize = Ext.create
 	        (
     			'Sbi.chart.designer.FontDimCombo',
     			
     			{
     				bind: '{configModel.parallelLegendElementFontSize}',
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
 	        
 	        this.add(legendElementFontSize);
	        
 	        var legendElementFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.parallelLegendElementFontFamily}',
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
 	        
 	       this.add(legendElementFontFamily);
		}
});