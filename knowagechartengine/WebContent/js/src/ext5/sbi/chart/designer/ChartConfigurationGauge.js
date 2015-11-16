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
		height : 100,

		fieldDefaults : {
			anchor : '100%'
		},

		layout : 
		{
			type : 'vbox'
		},

		constructor : function(config) {
			this.callParent(config);
			this.viewModel = config.viewModel;

			var item = 
			[				
				{
					xtype : 'numberfield',
					bind : '{configModel.startAnglePane}',
					id: "gaugeStartAnglePane",
					fieldLabel : LN("sbi.chartengine.configuration.gauge.startAnglePane"),
					width : "200",
					//value : "0",
					maxValue : '360',
					minValue : '-270'
				},

				{
					xtype : 'numberfield',
					bind : '{configModel.endAnglePane}',
					id: "gaugeEndAnglePane",
					fieldLabel : LN("sbi.chartengine.configuration.gauge.endAnglePane"),
					width : "200",
					//value : "90",
					maxValue : '360',
					minValue : '-270'
				}
			];

			this.add(item);
		}
	}
);