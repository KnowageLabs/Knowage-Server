Ext.define
(
	'Sbi.chart.designer.StylePopupLegendElement',
	
	{	
		extend : 'Ext.form.Panel',
		floating : true,
		draggable : true,
		closable : true,
		closeAction : 'hide',
	    modal: true,
		bodyPadding : 10,
	
		config : 
		{			
			bindLegendElementFontWeight: null,
			bindLegendElementFontSize : null,
			bindLegendElementFontFamily : null
		},
	
		items : [],
		
		constructor: function(config) 
		{
	        this.callParent(config);
	        this.viewModel = config.viewModel;
	        this.title = config.title && config.title != null ? config.title: this.title;
	       
	        Ext.apply(this.config,config);
        
	        var legendElementFontStyle = Ext.create	
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: this.config.bindLegendElementFontWeight,
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
     				bind: this.config.bindLegendElementFontSize,
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
					bind: this.config.bindLegendElementFontFamily,
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