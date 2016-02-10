/**
 * Customization panel for the SUNBURST chart type (Step 2).
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	"Sbi.chart.designer.ChartConfigurationSunburstTip", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartTip",
		
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
		columnWidth: 1,
		height: 320,
		
		title: LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
		bodyPadding: 10,
		items: [],		
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		/*layout: 
		{
		    type: 'vbox',
		    //align: 'center'
		},
		defaults : 
		 {	
			margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		            
		 },*/
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			var globalScope = this;
			
			var fontStyle = Ext.create
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: '{configModel.tipFontWeight}',
					fieldLabel: LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
					width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
					
					/**       				     
				     * @author: danristo (danilo.ristovski@mht.net)
				     */
				    listeners: 
				    {        				    	
				    	change: function(thisEl, newValue)
				    	{			
				    		if (newValue)
				    		{
				    			this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + ":"); 
				    		}	
				    		else
			    			{
				    			this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
			    			}
				    	}
				    }
				}	
			);
	        
	        this.add(fontStyle);
			
			/* Color picker drop-down matrix (table) */
//	        this.colorPicker = Ext.create
//	        (
//	        		'Sbi.chart.designer.ColorPickerContainer',
//	        		
//	        		{
//	        			viewModel: this.viewModel,
//	        			width: Sbi.settings.chart.configurationStep.widthOfFields,
//	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
//	        			fieldBind: '{configModel.tipColor}',
//	        			bodyPadding:10,
//	        			isColorMandatory: true,
//	        			customLabel: LN("sbi.chartengine.configuration.sunburst.tip.fontColor"),
//	        			initiator: "sunburstTooltipColor"
//	        		}
//	        );
	        this.colorPicker = Ext.create('Sbi.chart.designer.components.ColorPicker',{
	        	viewModel: this.viewModel,
	        	fieldBind: '{configModel.tipColor}',
	        	bind: '{configModel.tipColor}',
				fieldLabel : LN('sbi.chartengine.configuration.sunburst.tip.fontColor'),
				emptyText: LN('sbi.chartengine.configuration.axistitlecolor.emptyText'),
				width: Sbi.settings.chart.configurationStep.widthOfFields,
				adding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
				isColorMandatory: true,
//	        			initiator: "sunburstTooltipColor"
			});
	        
	        this.colorPicker.on
	        (
        		"colorRendered", 
        		
        		function(actualColorField)
        		{            			
        			if (actualColorField == "sunburstTooltipColor")
    				{
        				var fontColor = globalScope.viewModel.data.configModel.data.tipColor;
        				        				
        				if (fontColor && fontColor!="" && fontColor!="transparent")
    					{
        					globalScope.colorPicker.items.items[0]
        						.labelEl.update(LN('sbi.chartengine.configuration.sunburst.tip.fontColor')+":");
    					}
        				else
    					{
        					globalScope.colorPicker.items.items[0]
    							.labelEl.update(LN('sbi.chartengine.configuration.sunburst.tip.fontColor')
    												+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					}
    				}        			
    			}
    		);
	        
	        this.add(this.colorPicker);
	        
	        var fontSize = Ext.create
	        (
    			'Sbi.chart.designer.FontDimCombo',
    			
    			{
    				bind : '{configModel.tipFontSize}',
    				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
    				width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
    				
    				/**       				     
				     * @author: danristo (danilo.ristovski@mht.net)
				     */
				    listeners: 
				    {        				    	
				    	change: function(thisEl, newValue)
				    	{			
				    		if (newValue)
				    		{
				    			this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + ":"); 
				    		}	
				    		else
			    			{
				    			this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
			    			}
				    	}
				    }
    			}
			);
	        
	        this.add(fontSize);
	        
	        var tipFontFamily = Ext.create
			(	
				'Sbi.chart.designer.FontCombo',
				
				{
					bind: '{configModel.tipFontFamily}',
					fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
					width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
					
					/**       				     
				     * @author: danristo (danilo.ristovski@mht.net)
				     */
				    listeners: 
				    {        				    	
				    	change: function(thisEl, newValue)
				    	{			
				    		if (newValue)
				    		{
				    			this.labelEl.update(LN('sbi.chartengine.configuration.font') + ":"); 
				    		}	
				    		else
			    			{
				    			this.labelEl.update(LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
			    			}
				    	}
				    }
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
	                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.tip.width") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	                		 width: 280,
	                		 maxValue: '200',
	                		 minValue: '10',
	                		 emptyText: LN("sbi.chartengine.configuration.sunburstTipWidth.emptyText"),
	                		 
	                		 /**       				     
	     				     * @author: danristo (danilo.ristovski@mht.net)
	     				     */
	     				    listeners: 
	     				    {        				    	
	     				    	change: function(thisEl, newValue)
	     				    	{			
	     				    		if (newValue || parseInt(newValue))
	     				    		{
	     				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.tip.width') + ":"); 
	     				    		}	
	     				    		else
	     			    			{
	     				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.tip.width') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
	     			    			}
	     				    	}
	     				    }
	                	}
	         		]		                     
                 }	
	        );
	        
	       this.add(tipWidth);
	        
	       var tipPosition = Ext.create
	       (
    		   {
                	 xtype : 'fieldcontainer',
                	 //layout : 'hbox',
                	 
                	 defaults : 
                	 {
                		 //labelWidth : '100%',
                		// margin:'0 30 0 0'
                	 },
                	 
                	 items:
            		 [            		  	
            		  	{
            		  		xtype: "textarea",
            		  		// (top, right, bottom, left)
            		  		//padding: "0 0 5 20",
            		  		grow: true,
            		        name: 'tipText',
            		        fieldLabel: LN("sbi.chartengine.configuration.sunburst.tip.text") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
            		        //anchor: '100%',
            		        width: Sbi.settings.chart.configurationStep.widthOfFields,
                			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
            		        height: 120,
            		  		bind: '{configModel.tipText}',
            		  		emptyText: LN("sbi.chartengine.configuration.sunburstTipText.emptyText"),
            		  		
            		  		/**       				     
            		  		 * @author: danristo (danilo.ristovski@mht.net)
            		  		 */
	     				    listeners: 
	     				    {        				    	
	     				    	change: function(thisEl, newValue)
	     				    	{			
	     				    		if (newValue)
	     				    		{
	     				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.tip.text') + ":"); 
	     				    		}	
	     				    		else
	     			    			{
	     				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.tip.text') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
	     			    			}
	     				    	}
	     				    }
            		  	}
        		  	]
 	         		
 	         	}
	       );
	       
	       this.add(tipPosition);
		}
});