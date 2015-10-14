/**
 * Customization panel for the SUNBURST chart type (Step 2).
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	"Sbi.chart.designer.ChartConfigurationSunburstToolbarAndTip", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartToolbarAndTip",
		
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
		//columnWidth: 0.2,
		width: 200, // fixed value: current solution for the ATHENA-154 bug
		height: 110,
		
		title: LN("sbi.chartengine.configuration.toolbarAndTip.title"),
		bodyPadding: 10,
		items: [],		
		
		requires : [
			            'Sbi.chart.designer.StylePopupTip',
			            'Sbi.chart.designer.StylePopupToolbar'
			            ],
	
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
			
			this.stylePopupToolbar = null;
			this.stylePopupTip = null;
			
			this.stylePopupToolbar = Ext.create
			(
				'Sbi.chart.designer.StylePopupToolbar',
				
				{
					title: LN("sbi.chartengine.configuration.toolbarAndTip.toolbarPopup.title"), 
		        	viewModel: this.viewModel,
		        	bindToolbarPosition: '{configModel.toolbarPosition}',
		        	bindToolbarSpacing: '{configModel.toolbarSpacing}',
		        	bindToolbarTail: '{configModel.toolbarTail}',
		        	bindToolbarHeight: '{configModel.toolbarHeight}',
		        	bindToolbarWidth: '{configModel.toolbarWidth}',
		        	bindToolbarOpacMouseOver: '{configModel.toolbarOpacMouseOver}',
		        	bindToolbarPercFontColor: '{configModel.toolbarPercFontColor}'
		        }
			);		
			
			this.stylePopupTip = Ext.create
			(
				'Sbi.chart.designer.StylePopupTip',
				
				{
					title: LN("sbi.chartengine.configuration.toolbarAndTip.tipPopup.title"),
		        	viewModel: this.viewModel,
		        	bindTipText: '{configModel.tipText}',
		        	bindTipFontWeight: '{configModel.tipFontWeight}',
		        	bindTipAlign: '{configModel.tipAlign}',
		        	bindTipColor: '{configModel.tipColor}',
		        	bindTipFontSize: '{configModel.tipFontSize}',
		        	bindTipFontFamily: '{configModel.tipFontFamily}',
		        	bindTipWidth: '{configModel.tipWidth}',
		        	bindTipPosition: '{configModel.tipPosition}'
		        }
			);
			
	        var item = 
        	[        	 	
        	 	{
        	 		xtype : 'fieldcontainer',
        			layout : 'hbox',
        			
        			defaults : 
        			{
        				// (top, right, bottom, left)
        				margin: '5 0 5 0'
        			},
        			
        			items:
    				[
    				 	{
    				 		xtype: 'label',
				 	        text: LN("sbi.chartengine.configuration.toolbarAndTip.toolbarLabelText"),
				 	        padding: "3 0 0 0"
//				 	        margin: '0 0 0 10'
    				 	},
    				 	
    				 	{
    				 		xtype : 'button',
    	    	            text: LN("sbi.chartengine.configuration.toolbarAndTip.toolbarConfigButtonText"),	
    	    	            margin: '5 20 5 30',
    	    	            
    	    	            handler: function()
    	    	            {
    	    	            	globalScope.stylePopupToolbar.show();
    	    	            }
    				 	}
    				 ]
    				
    			},
    			
    			{
        	 		xtype : 'fieldcontainer',
        			layout : 'hbox',
        			
        			defaults : 
        			{
        				// (top, right, bottom, left)
        				margin: '0 0 5 0'
        			},
        			
        			items:
    				[
    				 	{
							xtype: 'label',
							text: LN("sbi.chartengine.configuration.toolbarAndTip.tipLabelText"),
					     	padding: "3 0 0 0"
						},
						
    				 	{
		    				xtype : 'button',
		    	            text: LN("sbi.chartengine.configuration.toolbarAndTip.tipConfigButtonText"),   	
		    	            margin: '0 0 5 53',
		    	            
		    	            handler: function()
		    	            {
		    	            	globalScope.stylePopupTip.show();
		    	            }
    				 	}
    				 ]
    				
    			}
             ];
	        
	        this.add(item);
		}
});