Ext.define
(
	"Sbi.chart.designer.ChartConfigurationParallelAxesLines", 
	
	{
		extend: 'Sbi.chart.designer.ChartConfigurationRoot',
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
		columnWidth: 1,
//		height: 170, //why this fixed height?? it cuts off the inner components
		
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
			
			var globalScope = this;
			
			/* Color picker drop-down matrix (table) */
//			this.colorPickerAxisColor = Ext.create
//			(
//					'Sbi.chart.designer.ColorPickerContainer',
//					
//					{
//						viewModel: this.viewModel,
//						isColorMandatory: true, 
//						width: Sbi.settings.chart.configurationStep.widthOfFields,
//						padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
//						customLabel: LN("sbi.chartengine.configuration.parallel.axesLines.axisColor"),
//						fieldBind: '{configModel.axisColor}',
//						initiator: "colorPickerAxisColor"
//					}
//			);
			
			/**
	         * Set the field label as the one when the color value is missing (plain (original) text
	         * with flag for mandatory fields) on the very beginning. This is needed for backup style
	         * and all those that do not contain any color value for the color element (in their
	         * structure). There will be no setting of color value inside the ColorPicker when there
	         * is no value for that particular color.
	         * 
	         * The 'isColorMandatory' serves as a flag for ColorPicker so it can know of this file
	         * should fire an appropriate event while listening for changing of the value inside of 
	         * the color picker text field.
	         * 
	         * The 'initiator' is needed for forwarding the information about the color text field
	         * for which the appropriate event is fired (because many of them can fire when e.g. 
	         * changing/applying the style).
	         * 
	         * NOTE: The same goes for other color picker in this file (colorPickerBrushColor).
	         * 
	         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	         */
	        this.colorPickerAxisColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
	        	viewModel: this.viewModel,
	        	id: "axisColorColorPicker",
	        	fieldBind: '{configModel.axisColor}',
	        	bind: '{configModel.axisColor}',
				fieldLabel: LN('sbi.chartengine.configuration.parallel.axesLines.axisColor')
							+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":",
				emptyText: LN('sbi.chartengine.configuration.parallel.axesLines.axisColor.emptyText'),
				width: Sbi.settings.chart.configurationStep.widthOfFields,
				padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
				isColorMandatory: true,
				initiator: "colorPickerAxisColor"
			});
	        
	        /**
			 * If the style defines the value of the color for this color field the field's 
			 * label will be updated with the text that does not include the flag for the 
			 * mandatory field (the symbol that tells the user that the value for the
			 * mandatory color field is missing - not specified).
			 * 
			 * NOTE: The same goes for other color picker in this file (colorPickerBrushColor).
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
	        this.colorPickerAxisColor.on
	        (
        		"colorRendered", 
        		
        		function(actualColorField)
        		{        
        			if (actualColorField == "colorPickerAxisColor")
    				{
        				var axisColor = globalScope.viewModel.data.configModel.data.axisColor;
        				
        				if (axisColor && axisColor!="" && axisColor!="transparent")
    					{
        					globalScope.colorPickerAxisColor
        						.labelEl.update( LN("sbi.chartengine.configuration.parallel.axesLines.axisColor")+":");
    					}
        				else
    					{
        					globalScope.colorPickerAxisColor
    							.labelEl.update( LN("sbi.chartengine.configuration.parallel.axesLines.axisColor")
    												+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					}
    				}        			
    			}
    		);
	        
	        /**
	         * When the color value is not valid, fired this event from the ColorPicker, so the text
	         * of the color field is going to be as the one when the value is missing (with the flag
	         * for mandatory field).
	         * 
	         * NOTE: The same goes for other color picker in this file (colorPickerBrushColor).
	         * 
	         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	         */
	        this.colorPickerAxisColor.on
	        (
	        	"colorNotValid",
	        	
	        	function(actualColorField)
	        	{	        	
	        		if (actualColorField == "colorPickerAxisColor")
	        		{
	        			globalScope.colorPickerAxisColor
							.labelEl.update( LN("sbi.chartengine.configuration.parallel.axesLines.axisColor")
											+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");	        			
	        		}
	        	}
	        );
	        
	        /* Color picker drop-down matrix (table) */
//	        this.colorPickerBrushColor= Ext.create
//	        (
//	        		'Sbi.chart.designer.ColorPickerContainer',
//	        		
//	        		{
//	        			viewModel: this.viewModel,
//	        			isColorMandatory: true, 
//	        			width: Sbi.settings.chart.configurationStep.widthOfFields,
//	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
//	        			customLabel: LN("sbi.chartengine.configuration.parallel.axesLines.brushColor"),
//	        			fieldBind: '{configModel.brushColor}',	
//	        			initiator: "colorPickerBrushColor",
//	        			
//	        		}
//	        );
	        
	        this.colorPickerBrushColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{
	        	viewModel: this.viewModel,
	        	id: "brushColorColorPicker",
	        	fieldBind: '{configModel.brushColor}',
	        	bind: '{configModel.brushColor}',
	        	fieldLabel: LN('sbi.chartengine.configuration.parallel.axesLines.brushColor')
							+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":",
				emptyText: LN('sbi.chartengine.configuration.parallel.axesLines.brushColor.emptyText'),
				width: Sbi.settings.chart.configurationStep.widthOfFields,
				padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
				isColorMandatory: true,
				initiator: "colorPickerBrushColor",
			});
	        
	        this.colorPickerBrushColor.on
	        (
        		"colorRendered", 
        		
        		function(actualColorField)
        		{      
        			if (actualColorField == "colorPickerBrushColor")
    				{
        				var brushColor = globalScope.viewModel.data.configModel.data.brushColor;
        				
        				if (brushColor && brushColor!="" && brushColor!="transparent")
    					{
        					globalScope.colorPickerBrushColor
        						.labelEl.update( LN("sbi.chartengine.configuration.parallel.axesLines.brushColor")+":");
    					}
        				else
    					{
        					globalScope.colorPickerBrushColor
    							.labelEl.update( LN("sbi.chartengine.configuration.parallel.axesLines.brushColor")
    												+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
    					}
    				}        			
    			}
    		);
			
	        this.colorPickerBrushColor.on
	        (
	        	"colorNotValid",
	        	
	        	function(actualColorField)
	        	{	        	
	        		if (actualColorField == "colorPickerBrushColor")
	        		{
	        			globalScope.colorPickerBrushColor
							.labelEl.update( LN("sbi.chartengine.configuration.parallel.axesLines.brushColor")
											+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");	        			
	        		}
	        	}
	        );
	        
			this.axisColNamePadd = Ext.create
			(
				{
					 xtype: 'numberfield',
					 bind : '{configModel.axisColNamePadd}',	
					 id: "parallelAxisColNamePadd",
					 fieldLabel: LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,	
					 width: Sbi.settings.chart.configurationStep.widthOfFields,
	        		 padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
					 maxValue: '30',
					 minValue: '0',
					 emptyText: LN("sbi.chartengine.configuration.parallelAxesLinesAxisColNamePadd.emptyText"),
					 
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
					 width: Sbi.settings.chart.configurationStep.widthOfFields,
	        		 padding:Sbi.settings.chart.configurationStep.paddingOfInnerFields,
//					 value: "20",
					 maxValue: '100',
					 minValue: '5',
					 emptyText: LN("sbi.chartengine.configuration.parallelAxesLinesBrushWidth.emptyText"),
					 
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