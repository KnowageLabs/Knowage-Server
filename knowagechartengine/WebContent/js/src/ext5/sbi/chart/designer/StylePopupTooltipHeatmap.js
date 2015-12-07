/**
 * @author: Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

Ext.define
(
	'Sbi.chart.designer.StylePopupTooltipHeatmap',
	
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
			bindFontAlign: null,
    	    bindSymbolHeight: null
		},
	
		items : [],
		
		constructor: function(config) 
		{
	        this.callParent(config);
	        this.viewModel = config.viewModel;
	        this.title = config.title && config.title != null ? config.title: this.title;
	       
	        Ext.apply(this.config,config);
	        
	        var font = Ext.create('Sbi.chart.designer.FontCombo',{
				viewModel: this.viewModel,
				bind : this.config.bindFont,
				fieldLabel :  
						LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						
				listeners:
				{
 					fontFamilyPicked: function()
					{
						this.labelEl.update(LN('sbi.chartengine.configuration.font') + ":"); 
					},
			
					fontFamilyEmpty: function()
					{
						this.labelEl.update(LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
					}
				}
			});
	        
			this.add(font);

			var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
	        	viewModel: this.viewModel,
	        	bind : this.config.bindFontDim,
	        	fieldLabel :  
	        			LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	        			
    			listeners:
				{
 					fontSizePicked: function()
					{
						this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + ":"); 
					},
			
					fontSizeEmpty: function()
					{
						this.labelEl.update(LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
					}
				}
	        });
			
			this.add(dim);
	        
			/**
			 * Added for the Heatmap (danristo :: danilo.ristovski@mht.net) 
			 */
			var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
	        	viewModel: this.viewModel,
	        	bind : this.config.bindFontStyle,
	        	fieldLabel :  
	        			LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	        			
    			listeners:
				{
 					fontStylePicked: function()
					{
						this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + ":"); 
					},
			
					fontStyleEmpty: function()
					{
						this.labelEl.update(LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
					}
				}
	        });
			
			this.add(style);
			
			var color = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
				viewModel: this.viewModel,
				fieldBind : this.config.bindColor,
				bind : this.config.bindColor,
				isColorMandatory: this.config.isFontColorMandatory
			});
			
			this.add(color);
		}	        
    }
);