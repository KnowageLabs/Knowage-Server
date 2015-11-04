Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelAxesLines", 
	
	{
		extend: 'Ext.panel.Panel',
		id: "chartParallelAxesLines",
		
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
//		columnWidth: 0.3,
		width: 250,
		height: 170,
		
		title: LN("sbi.chartengine.configuration.parallel.axesLines.title"), 
		bodyPadding: 10,
		items: [],
	
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
					
			this.colorPickerAxisColor = null;
			this.colorPickerBrushColor = null;
			this.axisColNamePadd = null;
			this.brushWidth = null;
			
			/* Color picker drop-down matrix (table) */
	        this.colorPickerAxisColor = Ext.create
	        (
        		'Sbi.chart.designer.ColorPickerContainer',
        		
        		{
        			viewModel: this.viewModel,
        			isColorMandatory: true, 
        			label: LN("sbi.chartengine.configuration.parallel.axesLines.axisColor"),
        			fieldBind: '{configModel.axisColor}'
        		}
    		);
	        
	        /* Color picker drop-down matrix (table) */
	        this.colorPickerBrushColor= Ext.create
	        (
        		'Sbi.chart.designer.ColorPickerContainer',
        		
        		{
        			viewModel: this.viewModel,
        			isColorMandatory: true, 
        			label: LN("sbi.chartengine.configuration.parallel.axesLines.brushColor"),
        			fieldBind: '{configModel.brushColor}',	
        		}
    		);
			
			this.axisColNamePadd = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.axisColNamePadd}',	
					 id: "parallelAxisColNamePadd",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "15",
					 maxValue: '30',
					 minValue: '0',
					 
					 listeners:
					 {
						 change: function(thisEl, newValue, oldValue)
						 {			
							 if (newValue || parseInt(newValue)==0)
							 {
								 this.labelEl.update(LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd")+":"); 
							 }								 
							 else 
							 {
								 this.labelEl.update
								 	(LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
							 }								 								 				 
						 }
					 }
				}
			 );
			 
			this.brushWidth= Ext.create
	        (
				{
					 xtype: 'numberfield',
					 bind : '{configModel.brushWidth}',	
					 id: "parallelBrushWidth",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: "200",
//					 value: "20",
					 maxValue: '100',
					 minValue: '5',
					 
					 listeners:
					 {
						 change: function(thisEl, newValue, oldValue)
						 {							 
							 if (newValue || parseInt(newValue)==0)
							 {
								 this.labelEl.update(LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth")+":"); 
							 }								 
							 else
							 {
								 this.labelEl.update
								 	(LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
							 }								 								 				 
						 }
					 }
				}
			);
						
			this.add(this.colorPickerAxisColor);
			this.add(this.axisColNamePadd);
			this.add(this.colorPickerBrushColor);
			this.add(this.brushWidth);
		}
});