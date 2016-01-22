Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelLegendTitle", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartParallelLegendTitle",
		
		columnWidth: 1,
		height: 170,
		
		title: LN("sbi.chartengine.configuration.parallel.legendTitlePanel.title"),
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
		},*/
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			var globalScope = this;
			
			this.stylePopupLegendTitle = null;
			this.stylePopupLegendElement = null;
			
			var item = [
						{
							xtype : 'fieldcontainer',

							/**
							 * Take the default layout for fields in the
							 * main panel. It is applied also in other
							 * fields in this file.
							 * 
							 * @author Danilo Ristovski (danristo,
							 *         danilo.ristovski@mht.net)
							 */
							layout : Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,

							defaults : {
								/**
								 * Old implementation (margin) and the
								 * new one (padding). It is applied also
								 * in other fields in this file.
								 * 
								 * @author Danilo Ristovski (danristo,
								 *         danilo.ristovski@mht.net)
								 */
								//margin : Sbi.settings.chart.configurationStep.marginOfTopFieldset
							},
							width: Sbi.settings.chart.configurationStep.widthOfFields,
		        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
							items : [
									{
										xtype : 'textfield',
										width : Sbi.settings.chart.configurationStep.widthOfFields,
										emptyText : LN("sbi.chartengine.configuration.title.emptyText"),
										bind : '{configModel.legendTitle}',
										fieldLabel : LN('sbi.chartengine.configuration.title')
									} ]
						}
		
		]
			
			this.add(item);
			 var legentTitleFontStyle = Ext.create	
				(	
					'Sbi.chart.designer.FontStyleCombo',
					
					{
						bind: '{configModel.parallelLegendTitleFontWeight}',
						fieldLabel: LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
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
					}	
				);
	        	
	        	this.add(legentTitleFontStyle);
	        	
	        	var legentTitleFontSize = Ext.create
	 	        (
	     			'Sbi.chart.designer.FontDimCombo',
	     			
	     			{
	     				bind : '{configModel.parallelLegendTitleFontSize}',
	     				fieldLabel: LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	     				width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
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
	     			}
	 			);
	 	        
	 	        this.add(legentTitleFontSize);
		        
	 	        var legentTitleFontFamily = Ext.create
				(	
					'Sbi.chart.designer.FontCombo',
					
					{
						bind: '{configModel.parallelLegendTitleFontFamily}',
						fieldLabel: LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
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
					}	
				);
	 	        
	 	       this.add(legentTitleFontFamily);
		}
});