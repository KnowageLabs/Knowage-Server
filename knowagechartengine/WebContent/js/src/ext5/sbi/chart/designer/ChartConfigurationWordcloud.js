/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

Ext.define
(
	"Sbi.chart.designer.ChartConfigurationWordcloud",

	{
		extend : 'Sbi.chart.designer.ChartConfigurationRoot',
		
		id : "wordcloudConfiguration",
		
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
		height : 320,
		
		title : LN("sbi.chartengine.configuration.wordcloud.configPanelTitle"),
		bodyPadding : 10,
		items : [],					

		requires : [ 'Sbi.chart.designer.StylePopupTip',
				'Sbi.chart.designer.StylePopupToolbar' ],

		fieldDefaults : {
			anchor : '100%'
		},

		layout : {
			type : 'vbox'
		},

		constructor : function(config) {
			this.callParent(config);
			this.viewModel = config.viewModel;

			var item = [

					{
						xtype : 'combo',
						queryMode : 'local',
						forceSelection : true,
						editable : false,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						bind : '{configModel.sizeCriteria}',
						displayField : 'name',
						valueField : 'value',					
						emptyText: LN("sbi.chartengine.configuration.wordcloudSizeCriteria.emptyText"),
						
						store : 
						{
							fields : [ 'name', 'value' ],
							
							data : 
							[
								{
									name : LN('sbi.chartengine.configuration.wordcloud.sizeCriteria.serie'),
									value : 'serie'
								},
								{
									name : LN('sbi.chartengine.configuration.wordcloud.sizeCriteria.occurrences'),
									value : 'occurrences'
								}
							]
						},
						
						listeners:
						{
							change: function(a,currentValue)
							{
								if (currentValue)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					},
					
					{
						xtype : 'numberfield',
						bind : '{configModel.maxWords}',
						id: "wordcloudMaxWords",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxWords") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
						maxValue : '300',
						minValue : '10',
						emptyText: LN("sbi.chartengine.configuration.wordcloudMaxNumWords.emptyText"),
						
						listeners:
						{
							change: function(a,currentValue)
							{
								if (currentValue || parseInt(currentValue)==0)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxWords") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxWords") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.maxAngle}',
						id: "wordcloudMaxAngle",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
						maxValue : '360',
						minValue : '0',
						emptyText: LN("sbi.chartengine.configuration.wordcloudMaxWordAngle.emptyText"),
						
						listeners:
						{
							change: function(a,currentValue)
							{
								if (currentValue || parseInt(currentValue)==0)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxAngle") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.minAngle}',
						id: "wordcloudMinAngle",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.minAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
						maxValue : '360',
						minValue : '0',
						emptyText: LN("sbi.chartengine.configuration.wordcloudMinWordAngle.emptyText"),
						
						listeners:
						{
							change: function(a,currentValue)
							{
								if (currentValue || parseInt(currentValue)==0)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.minAngle") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.minAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.maxFontSize}',
						id: "wordcloudMaxFontSize",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
						maxValue : '500',
						minValue : '30',
						emptyText: LN("sbi.chartengine.configuration.wordcloudMaxFontSize.emptyText"),
						
						listeners:
						{
							change: function(a,currentValue)
							{								
								if (currentValue || parseInt(currentValue)==0)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.wordPadding}',
						id: "wordcloudWordPadding",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.wordPadding") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
						maxValue : '20',
						minValue : '2',
						emptyText: LN("sbi.chartengine.configuration.wordcloudWordPadd.emptyText"),
						
						listeners:
						{
							change: function(a,currentValue)
							{
								if (currentValue || parseInt(currentValue)==0)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.wordPadding") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.wordPadding") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					} ];

			this.add(item);
		}
	}
);