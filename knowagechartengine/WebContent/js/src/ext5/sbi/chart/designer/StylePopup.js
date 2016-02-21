Ext.define('Sbi.chart.designer.StylePopup',{
	extend : 'Ext.form.Panel',
	floating : true,
	draggable : true,
	closable : true,
	closeAction : 'hide',
    modal: true,
	bodyPadding : 10,
	config : {
		bindFontAlign:null,
		bindColor:null,
		bindFont:null,
		bindFontDim:null,
		bindFontStyle:null,
		bindBorderWidth:null,
		bindBackgroundColor:null,
		isFontAlignMandatory: false,
		isFontSizeMandatory: false,
		isFontFamilyMandatory: false,
		isFontStyleMandatory: false,
		isBorderWidthMandatory: false,
		isFontColorMandatory: false,
		isBackgroundColorMandatory: false,
		
		paddingFontElements: null,
		layoutFontElements: null
	},
	
	items : [],
	
	constructor: function(config) {
        this.callParent(config);
        this.viewModel = config.viewModel;
        this.title = config.title && config.title != null ? config.title: this.title;
        Ext.apply(this.config,config);
        
        /**
         * TODO: Check if this is ok
         * 
         * Danilo
         */
        this.colorPickerElement = null;
        this.bckgColorPickerElement = null;
        
        var globalScope = this;
        
        if(this.config.bindFontAlign) {
        	var align = Ext.create('Sbi.chart.designer.FontAlignCombo',{
        		viewModel: this.viewModel,
        		bind : this.config.bindFontAlign,
        		
        		padding: this.config.paddingFontElements ? this.config.paddingFontElements : null,
				layout: this.config.layoutFontElements ? this.config.layoutFontElements : null, 
						
        		/**
    			 * If this is mandatory for certain chart type, this flag ('isFontAlignMandatory' 
    			 * boolean should be raised up. According to that we will determine if the label 
    			 * for this GUI element should be marked with some kind of flag that will point
    			 * to the mandatory field that is not specified.
    			 * 
    			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    			 */
        		fieldLabel : (globalScope.config.isFontAlignMandatory) ?  
        				LN('sbi.chartengine.configuration.title.alignment') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
        				: LN('sbi.chartengine.configuration.title.alignment'),
        				
        		
        	});
        	this.add(align);
        }
        
		var font = Ext.create('Sbi.chart.designer.FontCombo',{
			viewModel: this.viewModel,
			bind : this.config.bindFont,
			
			padding: this.config.paddingFontElements ? this.config.paddingFontElements : null,
			layout: this.config.layoutFontElements ? this.config.layoutFontElements : null, 
					
			/**
			 * If this is mandatory for certain chart type, this flag ('isFontFamilyMandatory' 
			 * boolean should be raised up. According to that we will determine if the label 
			 * for this GUI element should be marked with some kind of flag that will point
			 * to the mandatory field that is not specified.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			fieldLabel : (globalScope.config.isFontFamilyMandatory) ?  
					LN('sbi.chartengine.configuration.font') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: LN('sbi.chartengine.configuration.font'),
		});
		this.add(font);

		var dim = Ext.create('Sbi.chart.designer.FontDimCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontDim,
        	
        	padding: this.config.paddingFontElements ? this.config.paddingFontElements : null,
			layout: this.config.layoutFontElements ? this.config.layoutFontElements : null, 
        	
        	/**
			 * If this is mandatory for certain chart type, this flag ('isFontSizeMandatory' 
			 * boolean should be raised up. According to that we will determine if the label 
			 * for this GUI element should be marked with some kind of flag that will point
			 * to the mandatory field that is not specified.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
        	fieldLabel : (globalScope.config.isFontSizeMandatory) ?  
        			LN('sbi.chartengine.configuration.fontsize') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: LN('sbi.chartengine.configuration.fontsize')
        });
		this.add(dim);
        
		/**
		 * Added for the Heatmap (danristo :: danilo.ristovski@mht.net) 
		 */
		if(this.config.bindFontStyle) {
		var style = Ext.create('Sbi.chart.designer.FontStyleCombo',{
        	viewModel: this.viewModel,
        	bind : this.config.bindFontStyle,
        	
        	padding: this.config.paddingFontElements ? this.config.paddingFontElements : null,
			layout: this.config.layoutFontElements ? this.config.layoutFontElements : null, 
        	
        	/**
			 * If this is mandatory for certain chart type, this flag ('isFontStyleMandatory' 
			 * boolean should be raised up. According to that we will determine if the label 
			 * for this GUI element should be marked with some kind of flag that will point
			 * to the mandatory field that is not specified.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
        	fieldLabel : (globalScope.config.isFontStyleMandatory) ?  
        			LN('sbi.chartengine.configuration.fontstyle') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
    				: LN('sbi.chartengine.configuration.fontstyle')
        });
		this.add(style);
		}
		
		if(this.config.bindBorderWidth){
			var borderWidth = Ext.create('Ext.form.field.Number',{
				fieldLabel : LN('sbi.chartengine.configuration.borderwidth'),
				viewModel: this.viewModel,
				minValue: 0,				// added by: Danilo Ristovski (for the validation)
				id: "borderWidthLegend",	// added by: Danilo Ristovski (for the validation)
				bind:  this.config.bindBorderWidth,
				emptyText: LN("sbi.chartengine.configuration.legend.borderWith.emptyText"),
				
				padding: this.config.paddingFontElements ? this.config.paddingFontElements : null,
				layout: this.config.layoutFontElements ? this.config.layoutFontElements : null, 
				width: Sbi.settings.chart.configurationStep.widthOfFields,
						
				/**
				 * If this is mandatory for certain chart type, this flag ('isBorderWidthMandatory' 
				 * boolean should be raised up. According to that we will determine if the label 
				 * for this GUI element should be marked with some kind of flag that will point
				 * to the mandatory field that is not specified.
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
	        	fieldLabel : (globalScope.config.isBorderWidthMandatory) ?  
	        			LN('sbi.chartengine.configuration.borderwidth') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
	    				: LN('sbi.chartengine.configuration.borderwidth')
			})
			this.add(borderWidth);
		};

//		var color = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
		var color = Ext.create('Sbi.chart.designer.components.ColorPicker',{    		
			viewModel: this.viewModel,
			fieldBind : this.config.bindColor,
			bind : this.config.bindColor,
			fieldLabel : LN('sbi.chartengine.configuration.color'),
			emptyText: LN('sbi.chartengine.configuration.fontColor.emptyText'),
			
			width: Sbi.settings.chart.configurationStep.widthOfFields,
			padding: Sbi.settings.chart.configurationStep.paddingOfTopFields,
			isColorMandatory: this.config.isFontColorMandatory
		});
		this.add(color);
		
		this.colorPickerElement = color;       
				
		if(this.config.bindBackgroundColor){
//			var bkgrColor = Ext.create('Sbi.chart.designer.ColorPickerContainer',{    		
			var bkgrColor = Ext.create('Sbi.chart.designer.components.ColorPicker',{    		
				viewModel: this.viewModel,
				fieldBind : this.config.bindBackgroundColor,
				bind : this.config.bindBackgroundColor,
				emptyText: LN('sbi.chartengine.configuration.backgroundColor.emptyText'),
				
				padding: Sbi.settings.chart.configurationStep.paddingOfBottomFields,
				width: Sbi.settings.chart.configurationStep.widthOfFields,
				
				/**
				 * If this is mandatory for certain chart type, this flag ('isBackgroundColorMandatory' 
				 * boolean should be raised up. According to that we will determine if the label 
				 * for this GUI element should be marked with some kind of flag that will point
				 * to the mandatory field that is not specified.
				 *  
				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				//customLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
//				customLabel : (globalScope.config.isBackgroundColorMandatory) ?  
//						LN('sbi.chartengine.configuration.backgroundcolor') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
//						: LN('sbi.chartengine.configuration.backgroundcolor')
				fieldLabel : (globalScope.config.isBackgroundColorMandatory) ?  
	        			LN('sbi.chartengine.configuration.backgroundcolor') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields
	    				: LN('sbi.chartengine.configuration.backgroundcolor')
			});
			this.add(bkgrColor);
			
			this.bckgColorPickerElement = bkgrColor;
		}
    },
    
    /**
     * Listener that should take care of validation of the popup form that
     * appears when clicking on the "Configure" button inside the Legend
     * panel on the Configuration tab (previously Step 2).
     * 
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    listeners: {
    	close: function() {
    		
    		var errorMsg = "";
    		var globalScope = this;
    		
    		if (this.getComponent("borderWidthLegend")) {    			
    			/**
        		 * Validate the border with parameter for the Legend panel 
        		 * (inside of the popup that appears after clicking on the
        		 * Configure button).
        		 */
        		var errorMsg = "";
        		
        		
        		var borderWidthLegendField = this.getComponent("borderWidthLegend");
        		var borderWidthLegendValue = borderWidthLegendField.value;
        		    		
        		if ((borderWidthLegendValue || parseInt(borderWidthLegendValue)==0) && borderWidthLegendValue!=null)
    			{
    				if (borderWidthLegendValue < borderWidthLegendField.minValue)
    				{					
    					errorMsg += Sbi.locale.sobstituteParams
    					(
    						LN("sbi.chartengine.validation.configuration.minValueExtended"),
    						
    						[
    							LN("sbi.chartengine.configuration.borderwidth"),
    							borderWidthLegendField.minValue,
    							LN('sbi.chartengine.configuration.legend'),
    							LN('sbi.chartengine.configuration.stylebutton') + " button"
    						]
    					);
    				}
    			}
        		
//        		if (errorMsg != "") {
//        			var msg = Ext.Msg.show({						
//    					title : LN('sbi.chartengine.validation.errormessage'),
//    					message : errorMsg,
//    					icon : Ext.Msg.WARNING,
//    					closable : false,
//    					buttons : Ext.Msg.OK,
//    					
//    					fn : function()
//    					{
//    						/**
//    						 * Show again the initial popup (with the form)
//    						 * so the user can correct inappropriate value
//    						 * for the border width.
//    						 */
//    	                    globalScope.show();
//    	                }
//					});
//    			}       			
			} 
    		
    		/**
    		 * TODO: Check if this is ok
    		 * 
    		 * Validation for StylePopup color pickers.
    		 * 
    		 * Danilo
    		 */    		
    		var colorPicker = Sbi.chart.designer.components.ColorPicker;
    		
    		if (this.bckgColorPickerElement && this.bckgColorPickerElement!=null)
			{
    			var bckgColor = this.bckgColorPickerElement.getValue();
    			var bckgColor = (bckgColor.indexOf("#")==0) ? bckgColor.replace('#', '') : bckgColor;
    			
    			if (!colorPicker.validateValue(bckgColor))
    			{
    				errorMsg += Sbi.locale.sobstituteParams
    				(
    					LN("sbi.chartengine.validation.configuration.genericConf.title.colorElementNotValid"),
    					
    					[
    						LN("sbi.chartengine.configuration.backgroundcolor")
    					]
    				);
    			}
			}
    		
    		if (this.colorPickerElement && this.colorPickerElement!=null)
			{
    			var color = this.colorPickerElement.getValue();
    			var color = (color.indexOf("#")==0) ? color.replace('#', '') : color;
    			
    			if (!colorPicker.validateValue(color))
    			{
    				errorMsg += Sbi.locale.sobstituteParams
    				(
    					LN("sbi.chartengine.validation.configuration.genericConf.title.colorElementNotValid"),
    					
    					[
    					 	this.colorPickerElement.fieldLabel
    					]
    				);
    			}
			}    		

    		if (errorMsg != "") 
    		{
    			Ext.Msg.show
    			(
					{						
						title : LN('sbi.chartengine.validation.errormessage'),
						message : errorMsg,
						icon : Ext.Msg.WARNING,
						closable : false,
						buttons : Ext.Msg.OK,
						
						fn : function()
						{
							/**
							 * Show again the initial popup (with the form)
							 * so the user can correct inappropriate value
							 * for the border width.
							 */
							globalScope.show();
		                }
					}
				);
			}  
    	}
	}
});