/**
 * Customization panel for the SUNBURST chart type (Step 2).
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	"Sbi.chart.designer.ChartConfigurationSunburstToolbar", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartToolbar",
		
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
		height: 350,
		
		title: LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
		bodyPadding: 10,
		items: [],		
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
	/*	layout: 
		{
		    type: 'vbox',
		    //align: 'center'
		},*/
		/*defaults : 
		 {	
			margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		            
		},*/
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			var globalScope = this;
			
			var toolbarPosition = Ext.create
        	(
    			/* Horizontal line with just one combo - POSITION (top, bottom) */
             	{ 
             		xtype : 'fieldcontainer',
             		//layout : 'hbox',
             		width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
             		defaults : 
             		{
             			//labelWidth: '100%',
             			// (top, right, bottom, left)
             			//margin: '0 20 10 0'
             		},
	                    	 
		        	 items: 
	        		 [
	        		  	/* Combobox for POSITION of the TOOLBAR (top, bottom) */
        	         	{
        	         		xtype : 'combo',
        	         		queryMode : 'local',
        	         		triggerAction : 'all',
        	         		forceSelection : true,
        	         		width:'280',
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
        	         		},
        	         		
        	         		/**       				     
        				     * @author: danristo (danilo.ristovski@mht.net)
        				     */
        				    listeners: 
        				    {        				    	
        				    	change: function(thisEl, newValue)
        				    	{			
        				    		if (newValue)
        				    		{
        				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.position') + ":"); 
        				    		}	
        				    		else
    				    			{
        				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.position') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
    				    			}
        				    	}
        				    }
        	         	}
    	         	]		
                 }
			);
        	
        	this.add(toolbarPosition);
        	
//        	if (this.config.bindToolbarSpacing && this.config.bindToolbarTail)
//         	{
 	    
//        	this.toolbarSpacingAndTail = Ext.create
// 	        	(
//         			/* Horizontal line with two number fields - SPACING and TAIL */
//                     {            
// 	                   	 xtype : 'fieldcontainer',   
// 	                   	 items: 
// 	               		 [		                    	         
// 	           	         	{
// 	           	         		xtype: 'numberfield',
// 	           	         		bind : '{configModel.toolbarSpacing}',	
// 	           	         		id: "sunburstToolbarSpacing",
// 	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.spacing") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
// 	       	         			//maxWidth: '120',
// 	       	         			maxValue: '50',
// 	       	         			minValue: '1',
// 	       	         		    width: Sbi.settings.chart.configurationStep.widthOfFields,
// 	       			    padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,  
// 	       	         			emptyText: LN("sbi.chartengine.configuration.sunburstTooltipSpacing.emptyText"),
// 	       	         			
// 	       	         			/**       				     
// 	        				     * @author: danristo (danilo.ristovski@mht.net)
// 	        				     */
// 	        				    listeners: 
// 	        				    {        				    	
// 	        				    	change: function(thisEl, newValue)
// 	        				    	{			
// 	        				    		if (newValue || parseInt(newValue)==0)
// 	        				    		{
// 	        				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.spacing') + ":"); 
// 	        				    		}	
// 	        				    		else
// 	    				    			{
// 	        				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.spacing') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
// 	    				    			}
// 	        				    	}
// 	        				    }
// 	       	         		},
// 	       	         		
// 	       	         		{
// 	   	                		 xtype: 'numberfield',
// 	   	                		 bind : '{configModel.toolbarTail}',
// 	   	                		 id: "sunburstToolbarTail",
// 	   	                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.tail") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
// 	   	                		 width: Sbi.settings.chart.configurationStep.widthOfFields,
// 	   	    			         padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,  
// 	   	                		// maxWidth: '120',
// 	   	                		 maxValue: '100',
// 	   	                		 minValue: '10',
// 	   	                		 emptyText: LN("sbi.chartengine.configuration.sunburstTooltipTail.emptyText"),
// 	   	                		 
// 	   	                		 /**       				     
// 	   	                		  * @author: danristo (danilo.ristovski@mht.net)
// 	   	                		  */
// 	   	                		 listeners: 
// 	   	                		 {        				    	
// 	   	                			 change: function(thisEl, newValue)
// 	   	                			 {			
// 	   	                				 if (newValue || parseInt(newValue)==0)
// 	   	                				 {
// 	   	                					 this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.tail') + ":"); 
// 	   	                				 }	
// 	   	                				 else
// 	   	                				 {
// 	   	                					 this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.tail') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
// 	   	                				 }
// 	   	                			 }	
// 	   	                		 }
// 	   	                	}
// 	   	         		]		                     
//                     }
// 	        	);
 	        	
 	        	var toolbarSpacing=Ext.create({            
                   		                    	         
	           	         	
	           	         		xtype: 'numberfield',
	           	         		bind : '{configModel.toolbarSpacing}',	
	           	         		id: "sunburstToolbarSpacing",
	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.spacing") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	       	         			//maxWidth: '120',
	       	         			maxValue: '50',
	       	         			minValue: '1',
	       	         		    width: Sbi.settings.chart.configurationStep.widthOfFields,
	       			    padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,  
	       	         			emptyText: LN("sbi.chartengine.configuration.sunburstTooltipSpacing.emptyText"),
	       	         			
	       	         			/**       				     
	        				     * @author: danristo (danilo.ristovski@mht.net)
	        				     */
	        				    listeners: 
	        				    {        				    	
	        				    	change: function(thisEl, newValue)
	        				    	{			
	        				    		if (newValue || parseInt(newValue)==0)
	        				    		{
	        				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.spacing') + ":"); 
	        				    		}	
	        				    		else
	    				    			{
	        				    			this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.spacing') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
	    				    			}
	        				    	}
	        				    }
	       	         		});
 	        	
 	        	var toolbarTail=Ext.create({
 	        		
   
		 xtype: 'numberfield',
		 bind : '{configModel.toolbarTail}',
		 id: "sunburstToolbarTail",
		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.tail") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
		 width: Sbi.settings.chart.configurationStep.widthOfFields,
        padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,  
		// maxWidth: '120',
		 maxValue: '100',
		 minValue: '10',
		 emptyText: LN("sbi.chartengine.configuration.sunburstTooltipTail.emptyText"),
		 
		 /**       				     
		  * @author: danristo (danilo.ristovski@mht.net)
		  */
		 listeners: 
		 {        				    	
			 change: function(thisEl, newValue)
			 {			
				 if (newValue || parseInt(newValue)==0)
				 {
					 this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.tail') + ":"); 
				 }	
				 else
				 {
					 this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.tail') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
				 }
			 }	
		 }
	
 	        		
 	        	});
 	        	
 	        //	this.add(this.toolbarSpacingAndTail);
 	        	
 	        	this.add(toolbarSpacing);
 	        	this.add(toolbarTail);
//         	}
 	        
// 	        if (this.config.bindToolbarHeight && this.config.bindToolbarWidth)
//         	{
// 	        	this.toolbarHeightAndWidth = Ext.create
// 	        	(
//         			/* Horizontal line with two number fields - HEIGHT and WIDTH */
//                     {            
// 	                   	 xtype : 'fieldcontainer',
// 	                   //	layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
// 	   				     defaults:{
// 	   				    	margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset
// 	   				     },
// 	                    fieldDefaults: 
// 	           	      {
// 	           	        anchor: '100%'
// 	           		  },   
// 	           		
// 	                   	 items: 
// 	               		 [		                    	         
// 	           	         	{
// 	           	         		xtype: 'numberfield',
// 	           	         		bind : '{configModel.toolbarHeight}',	
// 	           	         		id: "sunburstToolbarHeight",
// 	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.height") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
// 	           	         		//maxWidth: '120',
// 	           	         		maxValue: '100',
// 	           	         		minValue: '10',
// 	           	         		width:Sbi.settings.chart.configurationStep.widthOfFields,
// 	           	         	    padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields, 
// 	           	         		emptyText: LN("sbi.chartengine.configuration.sunburstTooltipHeight.emptyText"),
// 	           	         		
// 	           	         		/**       				     
//	   	                		  * @author: danristo (danilo.ristovski@mht.net)
//	   	                		  */
// 	           	         		listeners: 
// 	           	         		{        				    	
// 	           	         			change: function(thisEl, newValue)
// 	           	         			{			
// 	           	         				if (newValue || parseInt(newValue)==0)
// 	           	         				{
// 	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.height') + ":"); 
// 	           	         				}	
// 	           	         				else
// 	           	         				{
// 	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.height') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
// 	           	         				}
// 	           	         			}	
// 	           	         		}
// 	       	         		},
// 	       	         		
// 	       	         		{
// 	   	                		 xtype: 'numberfield',
// 	   	                		 bind : '{configModel.toolbarWidth}',	
// 	   	                		 id: "sunburstToolbarWidth",
// 	   	                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.width") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
// 	   	                		// maxWidth: '120',
// 	   	                		 maxValue: '200',
// 	   	                		 minValue: '10',
//                                 width:Sbi.settings.chart.configurationStep.widthOfFields,
//                                 padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields, 
//                                 emptyText: LN("sbi.chartengine.configuration.sunburstTooltipWidth.emptyText"),
// 	   	                		 
// 	   	                		 /**       				     
//	   	                		  * @author: danristo (danilo.ristovski@mht.net)
//	   	                		  */
// 	           	         		listeners: 
// 	           	         		{        				    	
// 	           	         			change: function(thisEl, newValue)
// 	           	         			{			
// 	           	         				if (newValue || parseInt(newValue)==0)
// 	           	         				{
// 	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.width') + ":"); 
// 	           	         				}	
// 	           	         				else
// 	           	         				{
// 	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.width') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
// 	           	         				}
// 	           	         			}	
// 	           	         		}
// 	   	                	}
// 	   	         		]		                     
//                     }	
// 	        	);
 	        	
 	        	
 	        	var toolbarHeight=Ext.create(
 	        			{
	           	         		xtype: 'numberfield',
	           	         		bind : '{configModel.toolbarHeight}',	
	           	         		id: "sunburstToolbarHeight",
	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.height") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
	           	         		//maxWidth: '120',
	           	         		maxValue: '100',
	           	         		minValue: '10',
	           	         		width:Sbi.settings.chart.configurationStep.widthOfFields,
	           	         	    padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields, 
	           	         		emptyText: LN("sbi.chartengine.configuration.sunburstTooltipHeight.emptyText"),
	           	         		
	           	         		/**       				     
   	                		  * @author: danristo (danilo.ristovski@mht.net)
   	                		  */
	           	         		listeners: 
	           	         		{        				    	
	           	         			change: function(thisEl, newValue)
	           	         			{			
	           	         				if (newValue || parseInt(newValue)==0)
	           	         				{
	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.height') + ":"); 
	           	         				}	
	           	         				else
	           	         				{
	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.height') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
	           	         				}
	           	         			}	
	           	         		}
	       	         		}	
 	        	);
 	        	
 	        	var toolbarWidth=Ext.create({
               		 xtype: 'numberfield',
                		 bind : '{configModel.toolbarWidth}',	
                		 id: "sunburstToolbarWidth",
                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.width") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
                		// maxWidth: '120',
                		 maxValue: '200',
                		 minValue: '10',
                     width:Sbi.settings.chart.configurationStep.widthOfFields,
                     padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields, 
                     emptyText: LN("sbi.chartengine.configuration.sunburstTooltipWidth.emptyText"),
                		 
                		 /**       				     
               		  * @author: danristo (danilo.ristovski@mht.net)
               		  */
    	         		listeners: 
    	         		{        				    	
    	         			change: function(thisEl, newValue)
    	         			{			
    	         				if (newValue || parseInt(newValue)==0)
    	         				{
    	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.width') + ":"); 
    	         				}	
    	         				else
    	         				{
    	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.width') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
    	         				}
    	         			}	
    	         		}
                	});
 	        	
 	        	
 	        	//this.add(this.toolbarHeightAndWidth);
 	        	
 	        	this.add(toolbarHeight);
 	        	this.add(toolbarWidth);
 	        	
 	        	//         	}
 	        
 	        /* Color picker drop-down matrix (table) */
	        this.colorPicker = Ext.create
	        (
        		'Sbi.chart.designer.ColorPickerContainer',
        		
        		{
        			viewModel: this.viewModel,
        			isColorMandatory: true,
        			customLabel: LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor'),
        			fieldBind: '{configModel.toolbarPercFontColor}',
        			initiator: "sunburstPercentageColor",
        			//bodyPadding:10
        			width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
        		}
    		);		 
	        
	        this.colorPicker.on
	        (
        		"colorRendered", 
        		
        		function(actualColorField)
        		{        		
        			if (actualColorField == "sunburstPercentageColor")
    				{
        				var percentageColor = globalScope.viewModel.data.configModel.data.toolbarPercFontColor;
        				
        				if (percentageColor && percentageColor!="" && percentageColor!="transparent")
    					{
        					globalScope.colorPicker.items.items[0]
        						.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor')+":");
    					}
        				else
    					{
        					globalScope.colorPicker.items.items[0]
    							.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor')
    												+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					}
    				}        			
    			}
    		);
 		        
	        this.add(this.colorPicker);
 	        
	        var toolbarFontFamily = Ext.create
 			(	
 				'Sbi.chart.designer.FontCombo',
 				
 				{
 					bind: '{configModel.toolbarFontFamily}',
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
 	        
 	       this.add(toolbarFontFamily);
	        
 	        var toolbarFontStyle = Ext.create
 			(	
 				'Sbi.chart.designer.FontStyleCombo',
 				
 				{
 					bind: '{configModel.toolbarFontWeight}',
 					fieldLabel: LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
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
 	        
 	        this.add(toolbarFontStyle);
 	        
 	        var toolbarFontSize = Ext.create
 	        (
     			'Sbi.chart.designer.FontDimCombo',
     			
     			{
     				bind : '{configModel.toolbarFontSize}',
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
 	        
 	        this.add(toolbarFontSize);
		}
});