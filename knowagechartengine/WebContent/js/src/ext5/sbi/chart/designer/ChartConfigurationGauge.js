Ext.define
(
	"Sbi.chart.designer.ChartConfigurationGauge",

	{
		extend : 'Sbi.chart.designer.ChartConfigurationRoot',
		id : "gaugePaneConfiguration",
		
		columnWidth: 1,
//		width : 245,
		title : LN("sbi.chartengine.configuration.gauge.panelTitle"),
		bodyPadding : 10,
		items : [],
		height: 100,

		fieldDefaults : {
			anchor : '100%'
		},

//		layout : 
//		{
//			type : 'vbox'
//		},

		constructor : function(config) {
			this.callParent(config);
			this.viewModel = config.viewModel;

			var item = {
			xtype: 'fieldcontainer',
			
			items:[				
				{
					xtype : 'numberfield',
					bind : '{configModel.startAnglePane}',
					id: "gaugeStartAnglePane",
					fieldLabel : LN("sbi.chartengine.configuration.gauge.startAnglePane"),
				    width: Sbi.settings.chart.configurationStep.widthOfFields,
					maxValue : '360',
					minValue : '-270',
					padding: Sbi.settings.chart.configurationStep.paddingOfTopFields,
					emptyText: LN("sbi.chartengine.configuration.gaugeStartAngle.emptyText")
				},

				{
					xtype : 'numberfield',
					bind : '{configModel.endAnglePane}',
					id: "gaugeEndAnglePane",
					fieldLabel : LN("sbi.chartengine.configuration.gauge.endAnglePane"),
					width: Sbi.settings.chart.configurationStep.widthOfFields,
					maxValue : '360',
					minValue : '-270',
					padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields,
					emptyText: LN("sbi.chartengine.configuration.gaugeEndAngle.emptyText")
				}
			]};

			this.add(item);
		}
	}
);