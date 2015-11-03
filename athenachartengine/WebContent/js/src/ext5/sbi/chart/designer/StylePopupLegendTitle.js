Ext.define
(
	'Sbi.chart.designer.StylePopupLegendTitle',
	
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
			bindLegendTitleFontFamily: null,
			bindLegendTitleFontSize: null,
			bindLegendTitleFontWeight: null
		},
	
		items : [],
		
		constructor: function(config) 
		{
	        this.callParent(config);
	        this.viewModel = config.viewModel;
	        this.title = config.title && config.title != null ? config.title: this.title;
	       
	        Ext.apply(this.config,config);       
	        
	        var legentTitleFontStyle = Ext.create	
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: '{configModel.parallelLegendTitleFontWeight}',
					fieldLabel: LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
				}	
			);
        	
        	this.add(legentTitleFontStyle);
        	
        	var legentTitleFontSize = Ext.create
 	        (
     			'Sbi.chart.designer.FontDimCombo',
     			
     			{
     				bind : '{configModel.parallelLegendTitleFontSize}',
     				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
     			}
 			);
 	        
 	        this.add(legentTitleFontSize);
	        
 	        var legentTitleFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.parallelLegendTitleFontFamily}',
					fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
				}	
			);
 	        
 	       this.add(legentTitleFontFamily);
    }
});