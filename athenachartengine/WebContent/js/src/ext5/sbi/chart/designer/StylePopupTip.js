Ext.define
(
	'Sbi.chart.designer.StylePopupTip',
	
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
			bindTipFontWeight: null,
			bindTipColor: null,
			bindTipFontSize: null,
			bindTipFontFamily: null,
			bindTipWidth: null,
			bindTipPosition: null,
			bindTipText: null
		},
	
		items : [],
		
		constructor: function(config) 
		{
	        this.callParent(config);
	        this.viewModel = config.viewModel;
	        this.title = config.title && config.title != null ? config.title: this.title;
	       
	        Ext.apply(this.config,config);
        
	        var fontStyle = Ext.create
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: '{configModel.tipFontWeight}'
				}	
			);
	        
	        this.add(fontStyle);
			
			/* Color picker drop-down matrix (table) */
	        var colorPicker = Ext.create
	        (
        		'Sbi.chart.designer.ColorPickerContainer',
        		
        		{
        			viewModel: this.viewModel,
        			customLabel: LN("sbi.chartengine.configuration.sunburst.tip.fontColor"), 
        			fieldBind: '{configModel.tipColor}',	
        		}
    		);
	        
	        this.add(colorPicker);
	        
	        var fontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.tipFontSize}'
    			}
			);
	        
	        this.add(fontSize);
	        
	        var tipFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.tipFontFamily}'
				}	
			);
	        
	        this.add(tipFontFamily);
	        
	        var tipWidth = Ext.create
	        (
        		/* Horizontal line with one number field -  WIDTH */
                 {            
                	 xtype : 'fieldcontainer',
                	 layout : 'hbox',
                	 
                	 defaults : 
                	 {
//                		 labelWidth : '100%',
                		 margin:'0 30 0 0'
                	 },
	                    	 
                	 items: 
            		 [	
    	         		{
	                		 xtype: 'numberfield',
	                		 bind : '{configModel.tipWidth}',	
	                		 id: "sunburstTipWidth",
	                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.tip.width"),	
	                		 width: "200",
//	                		 value: "10",
	                		 maxValue: '200',
	                		 minValue: '10'
	                	}
	         		]		                     
                 }	
	        );
	        
	       this.add(tipWidth);
	        
	       var tipPosition = Ext.create
	       (
    		   {
                	 xtype : 'fieldcontainer',
                	 layout : 'hbox',
                	 
                	 defaults : 
                	 {
                		 //labelWidth : '100%',
                		 margin:'0 30 0 0'
                	 },
                	 
                	 items:
            		 [            		  	
            		  	{
            		  		xtype: "textarea",
            		  		// (top, right, bottom, left)
            		  		padding: "0 0 20 50",
            		  		grow: true,
            		        name: 'tipText',
            		        fieldLabel: LN("sbi.chartengine.configuration.sunburst.tip.text"),
            		        anchor: '100%',
            		  		bind: '{configModel.tipText}',
            		  		emptyText: "... empty text ..."
            		  	}
        		  	]
 	         		
 	         	}
	       );
	       
	       this.add(tipPosition);
    }
});