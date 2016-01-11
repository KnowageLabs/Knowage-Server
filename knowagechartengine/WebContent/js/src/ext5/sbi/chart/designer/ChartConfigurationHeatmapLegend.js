/**
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	"Sbi.chart.designer.ChartConfigurationHeatmapLegend", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
		id: "chartHeatmapLegend",
		
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
		height: 160,
		
		title: LN("sbi.chartengine.configuration.heatmap.legendPanel.title"),
		bodyPadding: 10,
		items: [],		
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		layout: 
		{
		    type: 'vbox',
		    //align: 'center'
		},
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			var globalScope = this;
			
			var align = Ext.create
	        (
        		'Sbi.chart.designer.VerticalAlignmentCombo',
        		{
        			viewModel: this.viewModel,
        			id: "heatmapLegendVertAlign",
        			fieldLabel: LN("sbi.chartengine.configuration.title.verticalAlignCombo") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
        			bind : '{configModel.legendAlign}',
        			emptyText: LN("sbi.chartengine.configuration.heatmapLegendVertAlign.emptyText"),
        			width: 280,
        			listeners:
					{
        				verticAlignPicked: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.title.verticalAlignCombo') + ":"); 
						},
				
						verticAlignEmpty: function()
						{
							this.labelEl.update(LN('sbi.chartengine.configuration.title.verticalAlignCombo') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
						}
					}
        		}
    		);
	        
	        var symbolHeight = Ext.create
	        (
	        	{
	        		xtype: 'numberfield',
	        		viewModel: this.viewModel,
	        		id: "heatmapLegendSymbolHeight",
           		 	bind : '{configModel.symbolHeight}',	
           		 	fieldLabel: LN("sbi.chartengine.configuration.heatmap.symbolHeight") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
           		 	width: 280,
           		 	maxValue: '800',
           		 	minValue: '50',
           		 	emptyText: LN("sbi.chartengine.configuration.heatmapSymbolHeight.emptyText"),
           		 	
           		 	listeners:
           		 	{
           		 		change: function(thisEl, newValue, oldValue)
           		 		{							 
           		 			if (newValue || parseInt(newValue)==0)
           		 			{
           		 				this.labelEl.update(LN("sbi.chartengine.configuration.heatmap.symbolHeight")+":"); 
           		 			}								 
           		 			else
           		 			{
           		 				this.labelEl.update
           		 				(LN("sbi.chartengine.configuration.heatmap.symbolHeight") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
           		 			}								 								 				 
           		 		}
           		 	}
	        	}
	        );
	        
	        this.stylePanelLegendTitle = Ext
			.create(
					'Sbi.chart.designer.StylePopup',
					{
						title : LN('sbi.chartengine.configuration.titlestyle'),
						viewModel : this.viewModel,
						//bindFontAlign : '{configModel.legendTitleAlign}',
						bindFont : '{configModel.legendTitleFont}',
						bindFontDim : '{configModel.legendTitleDimension}',
						bindFontStyle : '{configModel.legendTitleStyle}',
						bindColor : '{configModel.legendTitleColor}'
					});
	        var stylePanelLegendTitle=this.stylePanelLegendTitle;
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
											margin : Sbi.settings.chart.configurationStep.marginOfTopFieldset
										},

										items : [
												{
													xtype : 'textfield',
													width : Sbi.settings.chart.configurationStep.widthOfFields,
													emptyText : LN("sbi.chartengine.configuration.title.emptyText"),
													bind : '{configModel.legendTitle}',
													fieldLabel : LN('sbi.chartengine.configuration.title')
												},

												{
													xtype : 'button',

													margin : Sbi.settings.chart.configurationStep.marginOfTopFieldsetButtons,

													text : LN("sbi.chartengine.configuration.configurationButton.label"),

													handler : function() {
														stylePanelLegendTitle.show();
													}
												} ]
									}]
	        
	        this.add(item);
        	this.add(align);
        	this.add(symbolHeight);
        	
		}
});