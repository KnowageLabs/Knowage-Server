/**
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	"Sbi.chart.designer.ChartConfigurationHeatmapTooltip", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartHeatmapTooltip",
		
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
		height: 170,
		
		title: LN("sbi.chartengine.configuration.heatmap.tooltipPanel.title"),
		bodyPadding: 10,	
		
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		/*layout: 
		{
		    type: 'vbox',
		    //align: 'center'
		},
		*/
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			var globalScope = this;
			
			var font = Ext.create('Sbi.chart.designer.FontCombo',{
					viewModel: this.viewModel,
					bind : '{configModel.tipFontFamily}',
					fieldLabel :  
							LN('sbi.chartengine.configuration.font'),
					padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,		
					listeners:
					{
	 					fontFamilyPicked: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.font') + ":"); 
						},
				
						fontFamilyEmpty: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.font') + ":");
						}
					}
				});
		        
				this.add(font);
	        	
				var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
		        	viewModel: this.viewModel,
		        	bind : '{configModel.tipFontSize}',
		        	fieldLabel :  
		        			LN('sbi.chartengine.configuration.fontsize'),
		        	padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,		
	    			listeners:
					{
	 					fontSizePicked: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + ":"); 
						},
				
						fontSizeEmpty: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') +  ":");
						}
					}
		        });
				
				this.add(dim);
				
				/**
				 * Added for the Heatmap (danristo :: danilo.ristovski@mht.net) 
				 */
				var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
		        	viewModel: this.viewModel,
		        	bind : '{configModel.tipFontWeight}',
		        	fieldLabel :  
		        			LN('sbi.chartengine.configuration.fontstyle'),
		        	padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,		
	    			listeners:
					{
	 					fontStylePicked: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + ":"); 
						},
				
						fontStyleEmpty: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + ":");
						}
					}
		        });
				
				this.add(style);
				
				this.colorPicker = Ext.create
				(
					'Sbi.chart.designer.ColorPickerContainer',
					{    		
						viewModel: this.viewModel,
						fieldBind : '{configModel.tipColor}', 
						customLabel: LN("sbi.chartengine.configuration.color"),
						padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
						isColorMandatory: true,
						initiator: "heatmapTooltipColor",
						
					}
				);
				
				this.colorPicker.on
		        (
	        		"colorRendered", 
	        		
	        		function(actualColorField)
	        		{         	       			
	        			if (actualColorField == "heatmapTooltipColor")
	    				{
	        				var fontColor = globalScope.viewModel.data.configModel.data.tipColor;
	        				        				
	        				if (fontColor && fontColor!="" && fontColor!="transparent")
	    					{
	        					globalScope.colorPicker.items.items[0]
	        						.labelEl.update(LN('sbi.chartengine.configuration.color')+":");
	    					}
	        				else
	    					{
	        					globalScope.colorPicker.items.items[0]
	    							.labelEl.update(LN('sbi.chartengine.configuration.color')
	    												+  ":");
	    					}
	    				}        			
	    			}
	    		);
				
				this.add(this.colorPicker);
				
			/*	var item={
						type:'fieldcontainer',
						
						items:[
						       this.font,
						       this.style,
						       this.dim,
						       this.colorPicker
						       ]
				}*/
				
				//this.add(item);
				
		}
});