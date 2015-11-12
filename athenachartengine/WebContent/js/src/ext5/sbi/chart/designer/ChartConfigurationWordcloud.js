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
//		width : 230,
		height : 260,
		
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
						//value : 'serie', 
						forceSelection : true,
						editable : false,
						width: 200,
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						bind : '{configModel.sizeCriteria}',
						displayField : 'name',
						valueField : 'value',
						store : {
							fields : [ 'name', 'value' ],
							data : [
									{
										name : LN('sbi.chartengine.configuration.wordcloud.sizeCriteria.serie'),
										value : 'serie'
									},
									{
										name : LN('sbi.chartengine.configuration.wordcloud.sizeCriteria.occurrences'),
										value : 'occurrences'
									}]
						}
					},
					{
						xtype : 'numberfield',
						bind : '{configModel.maxWords}',
						id: "wordcloudMaxWords",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxWords") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width : "200",
						maxValue : '100',
						minValue : '10'
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.maxAngle}',
						id: "wordcloudMaxAngle",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width : "200",
						maxValue : '360',
						minValue : '60'
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.minAngle}',
						id: "wordcloudMinAngle",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.minAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width : "200",
						maxValue : '270',
						minValue : '0'
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.maxFontSize}',
						id: "wordcloudMaxFontSize",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width : "200",
						maxValue : '200',
						minValue : '50'
					},

					{
						xtype : 'numberfield',
						bind : '{configModel.wordPadding}',
						id: "wordcloudWordPadding",
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.wordPadding") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						width : "200",
						maxValue : '20',
						minValue : '2'
					} ];

			this.add(item);
		}
	}
);