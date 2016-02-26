/**
 * @author Ana Tomic (ana.tomic@mht.net)
 */

Ext.define
(
	"Sbi.chart.designer.ChartConfigurationWordcloudTooltip",

	{
		extend : 'Sbi.chart.designer.ChartConfigurationRoot',
		
		id : "wordcloudConfigurationTooltip",
		
		columnWidth: 1,
		height:335,
		
		title : LN("sbi.chartengine.configuration.wordcloud.tooltip.configPanelTitle"),
		bodyPadding : 10,
		items : [],					

		requires : [ 'Sbi.chart.designer.StylePopupTip',
				'Sbi.chart.designer.StylePopupToolbar' ],

		fieldDefaults : {
			anchor : '100%'
		},

//		layout : {
//			type : 'vbox'
//		},

		constructor : function(config) {
			this.callParent(config);
			this.viewModel = config.viewModel;
            
			var globalScope = this;
			
			var colorPickerBackground = Ext.create('Sbi.chart.designer.components.ColorPicker',{
	        	fieldLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
	        	emptyText: LN('sbi.chartengine.configuration.backgroundColor.emptyText'),
	    		bind : '{configModel.wordcloudTooltipBackgroundColor}',
	    		viewModel: this.viewModel,
	       		fieldBind: '{configModel.wordcloudTooltipBackgroundColor}',
	       		emptyText: LN('sbi.chartengine.configuration.backgroundColor.emptyText'),
	       		width: Sbi.settings.chart.configurationStep.widthOfFields,
	       		padding: Sbi.settings.chart.configurationStep.paddingOfTopFields	
	       	});
			
			var colorPickerFont = Ext.create('Sbi.chart.designer.components.ColorPicker',{
	        	fieldLabel : LN('sbi.chartengine.designer.tooltip.color'),
	        	emptyText: LN('sbi.chartengine.configuration.fontColor.emptyText'),
	    		bind : '{configModel.wordcloudTooltipFontColor}',
	    		viewModel: this.viewModel,
	       		fieldBind: '{configModel.wordcloudTooltipFontColor}',
	       		emptyText: LN('sbi.chartengine.configuration.fontColor.emptyText'),
	       		width: Sbi.settings.chart.configurationStep.widthOfFields,
	       		padding: Sbi.settings.chart.configurationStep.paddingOfTopFields	
	       	});
			
			var textAlign=Ext.create('Sbi.chart.designer.FontAlignCombo', {
	
						fieldLabel : LN('sbi.chartengine.designer.tooltip.align'),
						//emptyText: LN('sbi.chartengine.configuration.fontColor.emptyText'),
			    		bind : '{configModel.wordcloudTooltipAlign}',
			    		viewModel: this.viewModel,
			    		width: Sbi.settings.chart.configurationStep.widthOfFields,
			       		padding: Sbi.settings.chart.configurationStep.paddingOfTopFields
					});
			
			var tooltipFontStyle = Ext.create	
			(	
				'Sbi.chart.designer.FontStyleCombo',
				
				{
					bind: '{configModel.wordcloudTooltipFontStyle}',
					fieldLabel: LN('sbi.chartengine.configuration.fontstyle'),
					width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
				
				}	
			);
			
			var tooltipFontSize = Ext.create
 	        (
     			'Sbi.chart.designer.FontDimCombo',
     			
     			{
     				bind: '{configModel.wordcloudTooltipFontSize}',
     				fieldLabel: LN('sbi.chartengine.configuration.fontsize') ,
     				width: Sbi.settings.chart.configurationStep.widthOfFields,
        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
     			}
 			);
			
			  var tooltipFontFamily = Ext.create
				(	
					'Sbi.chart.designer.FontCombo',
					
					{
						bind: '{configModel.wordcloudTooltipFontFamily}',
						fieldLabel: LN('sbi.chartengine.configuration.font'),
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
					
					}	
				);
 	        
			var item = [

					
					
					
	                 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
								xtype : 'numberfield',
								bind : '{configModel.wordcloudTooltipPrecision}',
								id: "wordcloudTooltipPrecision",
								allowBlank: true,
								fieldLabel : LN("sbi.chartengine.designer.precision"),
								width: Sbi.settings.chart.configurationStep.widthOfFields,
			        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
								maxValue : '10',
								minValue : '0',
								emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.precision.emptyText"),
								
    	         			}
    	         		]
                	 },
                	
                	 
                	
                	 
	                 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
							{
								xtype : 'textfield',
								bind : '{configModel.wordcloudTooltipPrefix}',
								id: "wordcloudTooltipPrefix",
								fieldLabel : LN("sbi.chartengine.designer.prefixtext") ,
								width: Sbi.settings.chart.configurationStep.widthOfFields,
								padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
								emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.prefixText.emptyText"),
							},
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
							{
								xtype : 'textfield',
								bind : '{configModel.wordcloudTooltipPostfix}',
								id: "wordcloudTooltipPostfix",
								fieldLabel : LN("sbi.chartengine.designer.postfixtext") ,
								width: Sbi.settings.chart.configurationStep.widthOfFields,
								padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
								emptyText: LN("sbi.chartengine.structure.serieStyleConfig.serie.postfixText.emptyText"),
							},
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	            		  colorPickerFont
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	            		  colorPickerBackground
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
                          textAlign
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
                            tooltipFontFamily
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
                           tooltipFontStyle
                	 ] 
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
                           tooltipFontSize
                	 ] 
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
								xtype : 'numberfield',
								bind : '{configModel.wordcloudTooltipBorderWidth}',
								id: "wordcloudTooltipBorderWidth",
								allowBlank: true,
								fieldLabel : LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder"),
								width: Sbi.settings.chart.configurationStep.widthOfFields,
			        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
								maxValue : '10',
								minValue : '0',
								emptyText: LN("sbi.chartengine.configuration.parallelTooltipBorderWidth.emptyText"),
								
    	         			},
    	         			
    	         		]
                	 },
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
								xtype : 'numberfield',
								bind : '{configModel.wordcloudTooltipBorderRadius}',
								id: "wordcloudTooltipBorderRadius",
								allowBlank: true,
								fieldLabel : LN("sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius"),
								width: Sbi.settings.chart.configurationStep.widthOfFields,
			        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
								maxValue : '10',
								minValue : '0',
								emptyText: LN("sbi.chartengine.configuration.parallelTooltipBorderRadius.emptyText"),
								
    	         			},
    	         			
    	         		]
                	 },
                	 
					];

			this.add(item);	
		}
	}
);