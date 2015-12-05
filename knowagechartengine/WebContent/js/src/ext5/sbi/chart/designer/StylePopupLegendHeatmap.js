Ext.define
(
	'Sbi.chart.designer.StylePopupLegendHeatmap',
	
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
			bindFontAlign: null,
    	    bindSymbolHeight: null
		},
	
		items : [],
		
		constructor: function(config) 
		{
	        this.callParent(config);
	        this.viewModel = config.viewModel;
	        this.title = config.title && config.title != null ? config.title: this.title;
	       
	        Ext.apply(this.config,config);
	        
	        var align = Ext.create
	        (
        		'Sbi.chart.designer.VerticalAlignmentCombo',
        		{
        			viewModel: this.viewModel,
        			id: "heatmapLegendVertAlign",
        			fieldLabel: LN("sbi.chartengine.configuration.title.verticalAlignCombo") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
        			bind : this.config.bindFontAlign,
        			emptyText: LN("sbi.chartengine.configuration.heatmapLegendVertAlign.emptyText")
        		}
    		);
	        
	        var symbolHeight = Ext.create
	        (
	        	{
	        		xtype: 'numberfield',
	        		viewModel: this.viewModel,
	        		id: "heatmapLegendSymbolHeight",
           		 	bind : '{configModel.symbolHeight}',	
           		 	fieldLabel: LN("sbi.chartengine.configuration.heatmap.symbolHeight") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
           		 	width: 200,
           		 	maxValue: '800',
           		 	minValue: '50',
           		 	emptyText: LN("sbi.chartengine.configuration.heatmapSymbolHeight.emptyText")
	        	}
	        );
	        
        	this.add(align);
        	this.add(symbolHeight);
		}	        
    }
);