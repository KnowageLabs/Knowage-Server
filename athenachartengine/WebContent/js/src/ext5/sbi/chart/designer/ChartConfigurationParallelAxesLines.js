Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelAxesLines", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartParallelAxesLines",
		columnWidth: 0.3,
		title: LN("sbi.chartengine.configuration.parallel.axesLines.title"), 
		bodyPadding: 10,
		items: [],
		height: 170,
	
	    fieldDefaults: 
	    {
	        anchor: '100%'
		},
		
		layout: 
		{
		    type: 'vbox'
		},
		
		constructor: function(config) 
		{
			this.callParent(config);
			this.viewModel = config.viewModel;
			
			this.storeForSeriesBeforeDrop = Ext.data.StoreManager.lookup('storeForSeriesBeforeDrop');
					
			/* Color picker drop-down matrix (table) */
	        var colorPickerAxisColor = Ext.create
	        (
        		'Sbi.chart.designer.ColorPickerContainer',
        		
        		{
        			viewModel: this.viewModel,
        			customLabel: LN("sbi.chartengine.configuration.parallel.axesLines.axisColor"), 
        			fieldBind: '{configModel.axisColor}'
        		}
    		);
	        
	        /* Color picker drop-down matrix (table) */
	        var colorPickerBrushColor= Ext.create
	        (
        		'Sbi.chart.designer.ColorPickerContainer',
        		
        		{
        			viewModel: this.viewModel,
        			customLabel: LN("sbi.chartengine.configuration.parallel.axesLines.brushColor"), 
        			fieldBind: '{configModel.brushColor}',	
        		}
    		);
			
			var axisColNamePadd = 
			[
				{
					 xtype: 'numberfield',
					 bind : '{configModel.axisColNamePadd}',	
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd"),	
					 width: "200",
					 value: "15",
					 maxValue: '30',
					 minValue: '0'
				}
			 ];
			
			var brushWidth = 
			[
				{
					 xtype: 'numberfield',
					 bind : '{configModel.brushWidth}',	
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth"),	
					 width: "200",
					 value: "20",
					 maxValue: '100',
					 minValue: '5'
				}
			 ];
						
			this.add(colorPickerAxisColor);
			this.add(axisColNamePadd);
			this.add(colorPickerBrushColor);
			this.add(brushWidth);
		}
});