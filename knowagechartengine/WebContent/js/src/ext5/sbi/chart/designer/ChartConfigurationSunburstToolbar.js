/**
 * Customization panel for the SUNBURST chart type (Step 2). * 
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
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
		 */
		columnWidth: 1,
		height: 265,
		
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
    		  	/* Combobox for POSITION of the TOOLBAR (top, bottom) */
	         	{
	         		xtype : 'combo',
	         		queryMode : 'local',
	         		triggerAction : 'all',
	         		forceSelection : true,
	         		width: Sbi.settings.chart.configurationStep.widthOfFields,
	         		padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
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
			);
        	
        	this.add(toolbarPosition);        	
        	
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
 	        	 	        	
 	        	this.add(toolbarSpacing);
 	        	this.add(toolbarTail);
 	        	
 	        	/**
 	        	 * KNOWAGE-702 issue: The toolbar height should be removed since we are not
 	        	 * using this static value for the height of elements inside the breadcrumb
 	        	 * (toolbar) anymore, rather following the height of each element's word.
 	        	 * 
 	        	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 	        	 */
// 	        	var toolbarHeight=Ext.create(
// 	        			{
//	           	         		xtype: 'numberfield',
//	           	         		bind : '{configModel.toolbarHeight}',	
//	           	         		id: "sunburstToolbarHeight",
//	           	         		fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.height") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
//	           	         		//maxWidth: '120',
//	           	         		maxValue: '100',
//	           	         		minValue: '10',
//	           	         		width:Sbi.settings.chart.configurationStep.widthOfFields,
//	           	         	    padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields, 
//	           	         		emptyText: LN("sbi.chartengine.configuration.sunburstTooltipHeight.emptyText"),
//	           	         		
//	           	         		listeners: 
//	           	         		{        				    	
//	           	         			change: function(thisEl, newValue)
//	           	         			{			
//	           	         				if (newValue || parseInt(newValue)==0)
//	           	         				{
//	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.height') + ":"); 
//	           	         				}	
//	           	         				else
//	           	         				{
//	           	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.height') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
//	           	         				}
//	           	         			}	
//	           	         		}
//	       	         		}	
// 	        	);
 	        	
 	        	/**
 	        	 * KNOWAGE-701 issue: The toolbar width should be removed since we are not
 	        	 * using this static value for the width of elements inside the breadcrumb
 	        	 * (toolbar) anymore, rather following the length of each element's word.
 	        	 * 
 	        	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 	        	 */
// 	        	var toolbarWidth=Ext.create({
//               		 xtype: 'numberfield',
//                		 bind : '{configModel.toolbarWidth}',	
//                		 id: "sunburstToolbarWidth",
//                		 fieldLabel: LN("sbi.chartengine.configuration.sunburst.toolbar.width") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
//                		// maxWidth: '120',
//                		 maxValue: '200',
//                		 minValue: '10',
//                     width:Sbi.settings.chart.configurationStep.widthOfFields,
//                     padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields, 
//                     emptyText: LN("sbi.chartengine.configuration.sunburstTooltipWidth.emptyText"),
//                		 
//                		 /**       				     
//               		  * @author: danristo (danilo.ristovski@mht.net)
//               		  */
//    	         		listeners: 
//    	         		{        				    	
//    	         			change: function(thisEl, newValue)
//    	         			{			
//    	         				if (newValue || parseInt(newValue)==0)
//    	         				{
//    	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.width') + ":"); 
//    	         				}	
//    	         				else
//    	         				{
//    	         					this.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.width') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":"); 
//    	         				}
//    	         			}	
//    	         		}
//                	});
 	        	 	        	
// 	        	this.add(toolbarHeight);
// 	        	this.add(toolbarWidth);
 	        	
 	        	//         	}
 	        
 	        /* Color picker drop-down matrix (table) */
// 	        	this.colorPicker = Ext.create
// 	        	(
// 	        			'Sbi.chart.designer.ColorPickerContainer',
// 	        			
// 	        			{
// 	        				viewModel: this.viewModel,
// 	        				isColorMandatory: true,
// 	        				customLabel: LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor'),
// 	        				fieldBind: '{configModel.toolbarPercFontColor}',
// 	        				initiator: "sunburstPercentageColor",
// 	        				//bodyPadding:10
// 	        				width: Sbi.settings.chart.configurationStep.widthOfFields,
// 	        				padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
// 	        			}
// 	        	);	
 	        
        	/**
        	 * Set the field label as the one when the color value is missing (plain (original) text
	         * with flag for mandatory fields) on the very beginning. This is needed for backup style
	         * and all those that do not contain any color value for the color element (in their
	         * structure). There will be no setting of color value inside the ColorPicker when there
	         * is no value for that particular color.
	         * 
	         * The 'isColorMandatory' serves as a flag for ColorPicker so it can know of this file
	         * should fire an appropriate event while listening for changing of the value inside of 
	         * the color picker text field.
	         * 
	         * The 'initiator' is needed for forwarding the information about the color text field
	         * for which the appropriate event is fired (because many of them can fire when e.g. 
	         * changing/applying the style).
	         * 
	         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	         */
	        this.colorPicker = Ext.create('Sbi.chart.designer.components.ColorPicker',{
	        	viewModel: this.viewModel,
	        	id: "toolbarPercFontColorPicker",
	        	fieldBind: '{configModel.toolbarPercFontColor}',
	        	bind: '{configModel.toolbarPercFontColor}',
				fieldLabel: LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor')
							+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":",
				emptyText: LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor.emptyText'),
				width: Sbi.settings.chart.configurationStep.widthOfFields,
				padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
				initiator: "sunburstPercentageColor",
				isColorMandatory: true,
			});
	        
	        /**
			 * If the style defines the value of the color for this color field the field's 
			 * label will be updated with the text that does not include the flag for the 
			 * mandatory field (the symbol that tells the user that the value for the
			 * mandatory color field is missing - not specified).
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
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
        					globalScope.colorPicker
        						.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor')+":");
    					}
        				else
    					{
        					globalScope.colorPicker
    							.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor')
    												+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					}
    				}        			
    			}
    		);
	        
	        /**
	         * When the color value is not valid, fired this event from the ColorPicker, so the text
	         * of the color field is going to be as the one when the value is missing (with the flag
	         * for mandatory field).
	         * 
	         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	         */
	        this.colorPicker.on
	        (
	        	"colorNotValid",
	        	
	        	function(actualColorField)
	        	{	        		
	        		if (actualColorField == "sunburstPercentageColor")
    				{
    					globalScope.colorPicker
							.labelEl.update(LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor')
												+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					
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