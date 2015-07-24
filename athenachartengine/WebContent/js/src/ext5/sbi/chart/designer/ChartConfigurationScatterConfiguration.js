Ext.define
(
	"Sbi.chart.designer.ChartConfigurationScatterConfiguration", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartScatterConfiguration",
		columnWidth: 0.3,
		title: LN("sbi.chartengine.configuration.scatter.panelTitle"),
		bodyPadding: 10,
		items: [],
		height: 230,
	
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
			
	        /***
	         * Scatter zoom type combo box for the SCATTER chart type
	         */
	        var scatterZoomTypeField = Ext.create
	    	(
				"Sbi.chart.designer.ChartConfigurationScatterZoomType",
				
				{
					viewModel: this.viewModel,
	        		bind : '{configModel.scatterZoomType}'
				}
			);
	        
	        var scatterZoomTypeFieldDC = Ext.create
	        (
	    		{
	    	        xtype : 'fieldcontainer',
	    	        layout : 'hbox',
	    	        id: "zoomTypeFC",
	    	        
	    	        defaults : 
	    	        {
	    	            margin: '15 0 0 0'
	    	        },
	    	        
	    	        items : [scatterZoomTypeField]
	    		}
	        );
	        
	        var scatterStartOnTick = Ext.create
	    	(
				{
			        xtype: 'checkboxfield',
			        id: 'startOnTick',
			        bind : '{configModel.scatterStartOnTick}',
			        margin: '20 0 0 0',
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.configuration.scatter.startOnTick"),
			    }	
	    	);
	     	
	     	var scatterEndOnTick = Ext.create
	    	(
				{
			        xtype: 'checkboxfield',
			        id: 'endOnTick',
			        bind : '{configModel.scatterEndOnTick}',
			        margin: '20 0 0 0',
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.configuration.scatter.endOnTick"), 
			    }	
	    	);
	     	
	     	var scatterShowLastLabel = Ext.create
	    	(
				{
			        xtype: 'checkboxfield',
			        id: 'showLastLabel',
			        bind : '{configModel.scatterShowLastLabel}',
			        margin: '20 0 0 0',
			        labelSeparator: '',
			        fieldLabel: LN("sbi.chartengine.configuration.scatter.showLastLabel"), 
			    }	
	    	);     
	        
	        this.add(scatterZoomTypeFieldDC);
	        this.add(scatterStartOnTick); 
	        this.add(scatterEndOnTick); 
	        this.add(scatterShowLastLabel); 		
		}
});