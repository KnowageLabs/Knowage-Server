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
	    id: "sunburstToolbarPopup",
		bodyPadding : 10,
	
		config : 
		{
			bindToolbarPosition: null,
        	bindToolbarSpacing: null,
        	bindToolbarTail: null,
        	bindToolbarHeight: null,
        	bindToolbarWidth: null,
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
	            	         		fieldLabel : LN('sbi.chartengine.configuration.sunburst.toolbar.position') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	            	         		bind : '{configModel.toolbarPosition}',
	            	         		displayField : 'name',
	            	         		valueField : 'value',
	            	         		emptyText: LN("sbi.chartengine.configuration.sunburstTooltipPosition.emptyText"),
	                        		 
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
	        	this.toolbarSpacingAndTail = Ext.create
	        	(
        			/* Horizontal line with two number fields - SPACING and TAIL */
                    {            
	                   	 xtype : 'fieldcontainer',
	                   	 layout : 'hbox',
	                   	 
	                   	 defaults : 
	                   	 {
	                   		 labelWidth : '100%',
	                   		 // (top, right, bottom, left)
	                   		 margin:'0 30 0 0'
	                   	 },
	   	                    	 
	                   	 items: 
	               		 [		                    	         
	           	         	{
	           	         		xtype: 'numberfield',
	           	         		bind : '{configModel.toolbarSpacing}',	
	           	         		id: "sunburstToolbarSpacing",
	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.spacing") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	       	         			maxWidth: '120',
	       	         			maxValue: '50',
	       	         			minValue: '1',
	       	         			emptyText: LN("sbi.chartengine.configuration.sunburstTooltipSpacing.emptyText")
	       	         		},
	       	         		
	       	         		{
	   	                		 xtype: 'numberfield',
	   	                		 bind : '{configModel.toolbarTail}',
	   	                		 id: "sunburstToolbarTail",
	   	                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.tail") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	   	                		 maxWidth: '120',
	   	                		 maxValue: '100',
	   	                		 minValue: '10',
	   	                		 emptyText: LN("sbi.chartengine.configuration.sunburstTooltipTail.emptyText")
	   	                	}
	   	         		]		                     
                    }
	        	);
	        	
	        	this.add(this.toolbarSpacingAndTail);
        	}
	        
	        if (this.config.bindToolbarHeight && this.config.bindToolbarWidth)
        	{
	        	this.toolbarHeightAndWidth = Ext.create
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
	           	         		id: "sunburstToolbarHeight",
	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.height") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	           	         		maxWidth: '120',
	           	         		maxValue: '100',
	           	         		minValue: '10',
	           	         		emptyText: LN("sbi.chartengine.configuration.sunburstTooltipHeight.emptyText")
	       	         		},
	       	         		
	       	         		{
	   	                		 xtype: 'numberfield',
	   	                		 bind : '{configModel.toolbarWidth}',	
	   	                		 id: "sunburstToolbarWidth",
	   	                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.width") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	   	                		 maxWidth: '120',
	   	                		 maxValue: '200',
	   	                		 minValue: '10',
	   	                		 emptyText: LN("sbi.chartengine.configuration.sunburstTooltipWidth.emptyText")
	   	                	}
	   	         		]		                     
                    }	
	        	);
	        	
	        	this.add(this.toolbarHeightAndWidth);
        	}
	        
	        var toolbarFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.toolbarFontFamily}',
					fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
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
	        			isColorMandatory: true,
	        			label: LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor'),
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
					bind: '{configModel.toolbarFontWeight}',
					fieldLabel: LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
				}	
			);
	        
	        this.add(toolbarFontStyle);
	        
	        var toolbarFontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.toolbarFontSize}',
    				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    			}
			);
	        
	        this.add(toolbarFontSize);
    }
});