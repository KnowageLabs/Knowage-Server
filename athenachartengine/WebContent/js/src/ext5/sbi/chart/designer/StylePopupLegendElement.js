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
					bind: this.config.bindLegendElementFontWeight
				}	
			);
        	
        	this.add(legendElementFontStyle);
        	        	
        	var legendElementFontSize = Ext.create
 	        (
     			'Sbi.chart.designer.FontDimCombo',
     			
     			{
     				bind: this.config.bindLegendElementFontSize
     			}
 			);
 	        
 	        this.add(legendElementFontSize);
	        
 	        var legendElementFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: this.config.bindLegendElementFontFamily
				}	
			);
 	        
 	       this.add(legendElementFontFamily);
	        
		}
});