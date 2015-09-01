Ext.define
(
	'Sbi.chart.designer.StylePopupToolbar',
	
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
			bindToolbarPosition: null,
        	bindToolbarSpacing: null,
        	bindToolbarTail: null,
        	bindToolbarHeight: null,
        	bindToolbarWidth: null,
        	bindToolbarOpacMouseOver: null,
        	bindToolbarPercFontColor: null
		},
	
		items : [],
		
		constructor: function(config) 
		{
	        this.callParent(config);
	        this.viewModel = config.viewModel;
	        this.title = config.title && config.title != null ? config.title: this.title;
	       
	        Ext.apply(this.config,config);
        
	        if(this.config.bindToolbarPosition) 
	        {
	        	var toolbarPosition = Ext.create
	        	(
	        			/* Horizontal line with just one combo - POSITION (top, bottom) */
	                 	{ 
	                 		xtype : 'fieldcontainer',
	                 		layout : 'hbox',
	                 		
	                 		defaults : 
	                 		{
	                 			labelWidth: '100%',
	                 			// (top, right, bottom, left)
	                 			margin: '0 20 10 0'
	                 		},
	    	                    	 
	    		        	 items: 
	    	        		 [
	    	        		  	/* Combobox for POSITION of the TOOLBAR (top, bottom) */
	            	         	{
	            	         		xtype : 'combo',
	            	         		queryMode : 'local',
	            	         		value : 'bottom',
	            	         		triggerAction : 'all',
	            	         		forceSelection : true,
	            	         		editable : false,
	            	         		fieldLabel : LN('sbi.chartengine.configuration.position'),
	            	         		bind : '{configModel.toolbarPosition}',
	            	         		displayField : 'name',
	            	         		valueField : 'value',
	                        		 
	                        		 store: 
	                        		 {
	                        			 fields : ['name', 'value'],
	                        			 
	                        			 data : 
	                    				 [ 
	                        				   {
	                        					   name : LN('sbi.chartengine.configuration.position.b'),
	                        					   value : 'bottom'
	                        				   }, 
	                        				   
	                        				   {
	                        					   name : LN('sbi.chartengine.configuration.position.t'),
	                        					   value : 'top'
	                        				   }
	                    				   ]
	                        		 }
	            	         	}
	        	         	]		
	                     }
    			);
	        	
	        	this.add(toolbarPosition);
	        }
	        
	        if (this.config.bindToolbarSpacing && this.config.bindToolbarTail)
        	{
	        	var toolbarSpacingAndTail = Ext.create
	        	(
        			/* Horizontal line with two number fields - SPACING and TAIL */
                    {            
	                   	 xtype : 'fieldcontainer',
	                   	 layout : 'hbox',
	                   	 
	                   	 defaults : 
	                   	 {
	                   		 labelWidth : '100%',
	                   		 margin:'0 30 0 0'
	                   	 },
	   	                    	 
	                   	 items: 
	               		 [		                    	         
	           	         	{
	           	         		xtype: 'numberfield',
	           	         		bind : '{configModel.toolbarSpacing}',		
	           	         		fieldLabel: LN("sbi.chartengine.configuration.toolbar.spacing"),	
	       	         			maxWidth: '120',
	       	         			maxValue: '50',
	       	         			minValue: '1',
	       	         			value: "1"
	       	         		},
	       	         		
	       	         		{
	   	                		 xtype: 'numberfield',
	   	                		 bind : '{configModel.toolbarTail}',	
	   	                		 fieldLabel: LN("sbi.chartengine.configuration.toolbar.tail"),	
	   	                		 maxWidth: '120',
	   	                		 maxValue: '100',
	   	                		 minValue: '10',
	   	                		 value: "10"
	   	                	}
	   	         		]		                     
                    }
	        	);
	        	
	        	this.add(toolbarSpacingAndTail);
        	}
	        
	        if (this.config.bindToolbarHeight && this.config.bindToolbarWidth)
        	{
	        	var toolbarHeightAndWidth = Ext.create
	        	(
        			/* Horizontal line with two number fields - HEIGHT and WIDTH */
                    {            
	                   	 xtype : 'fieldcontainer',
	                   	 layout : 'hbox',
	                   	 
	                   	 defaults : 
	                   	 {
	                   		 labelWidth : '100%',
	                   		 margin:'5 30 0 0'
	                   	 },
	   	                    	 
	                   	 items: 
	               		 [		                    	         
	           	         	{
	           	         		xtype: 'numberfield',
	           	         		bind : '{configModel.toolbarHeight}',		
	           	         		fieldLabel: LN("sbi.chartengine.configuration.toolbar.height"),	
	           	         		maxWidth: '120',
	           	         		maxValue: '100',
	           	         		minValue: '10',
	           	         		value: "10"
	       	         		},
	       	         		
	       	         		{
	   	                		 xtype: 'numberfield',
	   	                		 bind : '{configModel.toolbarWidth}',	
	   	                		 fieldLabel: LN("sbi.chartengine.configuration.toolbar.width"),	
	   	                		 maxWidth: '120',
	   	                		 maxValue: '200',
	   	                		 minValue: '10',
	   	                		 value: "10"
	   	                	}
	   	         		]		                     
                    }	
	        	);
	        	
	        	this.add(toolbarHeightAndWidth);
        	}
	        
	        var toolbarFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.toolbarFontFamily}'
				}	
			);
	        
	        if (this.config.bindToolbarPercFontColor)
        	{
	        	 /* Color picker drop-down matrix (table) */
		        var colorPicker = Ext.create
		        (
	        		'Sbi.chart.designer.ColorPickerContainer',
	        		
	        		{
	        			viewModel: this.viewModel,
	        			customLabel: LN("sbi.chartengine.configuration.toolbar.percentageColor"), 
	        			fieldBind: '{configModel.toolbarPercFontColor}',	
	        		}
	    		);		        
		        
		        this.add(colorPicker);
        	}
	        
	        this.add(toolbarFontFamily);
	        
	        var toolbarFontStyle = Ext.create
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: '{configModel.toolbarFontWeight}'
				}	
			);
	        
	        this.add(toolbarFontStyle);
	        
	        var toolbarFontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.toolbarFontSize}'
    			}
			);
	        
	        this.add(toolbarFontSize);
    }
});