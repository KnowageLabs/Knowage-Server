Ext.define
(
	"Sbi.chart.designer.ChartConfigurationScatterConfiguration", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartScatterConfiguration",
		requires: [
		           'Sbi.chart.designer.ChartConfigurationScatterZoomType'
		           ],
				
       /**
       	 * NOTE: 
       	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
       	 * Instead of using dynamic width for this panel (Scatter configuration) 
       	 * that relies on the width of the width of the window of the browser, 
       	 * fix this value so it can be entirely visible to the end user. Also the
       	 * height will be defined as the fixed value.
       	 * 
       	 * @author: danristo (danilo.ristovski@mht.net)
       	 */
       columnWidth: 1,     		          
//        width: 290,
       height: 230,
        
	    title: LN("sbi.chartengine.configuration.scatter.panelTitle"),
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