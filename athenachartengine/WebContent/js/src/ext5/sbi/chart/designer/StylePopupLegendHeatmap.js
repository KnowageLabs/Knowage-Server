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
    	    bindSymbolWidth: null
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
        		'Sbi.chart.designer.FontAlignCombo',
        		{
        			viewModel: this.viewModel,
        			bind : this.config.bindFontAlign
        		}
    		);
	        
	        var symbolWidth = Ext.create
	        (
	        	{
	        		xtype: 'numberfield',
	        		viewModel: this.viewModel,
           		 	bind : '{configModel.symbolWidth}',	
           		 	fieldLabel: LN("sbi.chartengine.configuration.heatmap.symbolWidth"),
           		 	width: "200",
           		 	maxValue: '1000',
           		 	minValue: '10'       		
	        	}
	        );
	        
        	this.add(align);
        	this.add(symbolWidth);
		}	        
    }
);