Ext.define('Sbi.chart.designer.ChartConfigurationMainContainer', {
	extend : 'Sbi.chart.designer.ChartConfigurationRoot',
	requires : [
	            'Sbi.chart.designer.ChartOrientationCombo',
	            'Sbi.chart.designer.ColorPickerContainer',
	            'Sbi.chart.designer.FontCombo',
	            'Sbi.chart.designer.FontDimCombo',
	            'Sbi.chart.designer.FontStyleCombo',
	            'Sbi.chart.designer.StylePopup'
	            
	            ],
	title : LN('sbi.chartengine.configuration'),
	bodyPadding : 10,
	
	//overflowX: "auto",

	fieldDefaults: {
        anchor: '100%'
	},
	
	//**********************//
	
//	height: null,
//	width: null,
	margin: "0 0 5 0",	// overrides the margin set in the ChartConfigurationRoot.js
	
	/**
	 * NOTE: 
	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
	 * Allow vertical and horizontal scroll bar appearance for the main
	 * (Generic) configuration panel on the Step 2 when its item are not 
	 * visible anymore due to resizing of the window of the browser.
	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
//	overflowX: "auto",
//	overflowY: "auto",
	
	chartOrientation : null,
	
	font : Ext.create('Sbi.chart.designer.FontCombo',{
		bind : '{configModel.font}',
		fieldLabel : LN("sbi.chartengine.configuration.fontFamily"),
		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields	// Danilo Ristovski
	}),
	
   	fontSize : Ext.create('Sbi.chart.designer.FontDimCombo',{
   		bind : '{configModel.fontDimension}',
   		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields	// Danilo Ristovski
   	}),
   	
   	fontStyle : Ext.create('Sbi.chart.designer.FontStyleCombo',{
   		bind : '{configModel.fontWeight}',
   		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields	// Danilo Ristovski
   	}),
   	
   	
   	colorPickerContainer : {},
   	
	stylePanelSubtitle : {},
	stylePanelTitle : {},
	stylePanelNoData : {},
	
	//**********************//
	
	constructor: function(config) {
		
        this.callParent(config);
        this.viewModel = config.viewModel;
       
        var globalThis = this;
        
        this.height = {
    		xtype : 'numberfield',
    		id: "chartHeightNumberfield", 	// added by: Danilo Ristovski (for the validation)
    		minValue: 0,					// added by: Danilo Ristovski (for the validation)
//    		width: 280,
    		width: Sbi.settings.chart.configurationStep.widthOfFields,			// Danilo Ristovski
    		padding: Sbi.settings.chart.configurationStep.paddingOfTopFields,	// Danilo Ristovski
    		emptyText: LN("sbi.chartengine.configuration.height.emptyText"),    		
    		bind : '{configModel.height}',
    		fieldLabel : LN('sbi.chartengine.configuration.height')
    	};
        
        this.width = {
    		xtype : 'numberfield',
    		id: "chartWidthNumberfield",
    		minValue: 0,					// added by: Danilo Ristovski (for the validation)
//    		width: 280,	// Old width (Danilo Ristovski)
    		width: Sbi.settings.chart.configurationStep.widthOfFields,			// Danilo Ristovski
    		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields,	// Danilo Ristovski
    		emptyText: LN("sbi.chartengine.configuration.width.emptyText"),    		
    		bind : '{configModel.width}',
    		fieldLabel : LN('sbi.chartengine.configuration.width'),
    		hidden: ChartUtils.disableChartWidth()
    	};
        
        this.chartOrientation = Ext.create('Sbi.chart.designer.ChartOrientationCombo',{
    		id: 'chartOrientationCombo',
    		bind : '{configModel.orientation}',    		
    		hidden: ChartUtils.disableChartOrientation(),
    		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields	// Danilo Ristovski
    	});
        
        var font = this.font;
        var fontSize = this.fontSize;
        var fontStyle = this.fontStyle;
        
        this.colorPickerContainer = Ext.create('Sbi.chart.designer.ColorPickerContainer',{
    		viewModel: this.viewModel,
    		customLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
       		fieldBind: '{configModel.backgroundColor}',
       		//margin: Sbi.settings.chart.configurationStep.paddingOfInnerFields	// Danilo Ristovski
       	});
        
        var colorPickerContainer = this.colorPickerContainer;       
        
        this.stylePanelTitle = Ext.create('Sbi.chart.designer.StylePopup',{
        	title: LN('sbi.chartengine.configuration.titlestyle'),
        	viewModel: this.viewModel,
        	bindFontAlign:'{configModel.titleAlign}',
        	bindFont:'{configModel.titleFont}',
        	bindFontDim:'{configModel.titleDimension}',
        	bindFontStyle:'{configModel.titleStyle}',
        	bindColor:'{configModel.titleColor}'
        });	
        
        this.stylePanelSubtitle = Ext.create('Sbi.chart.designer.StylePopup', {
    	    title: LN('sbi.chartengine.configuration.subtitlestyle'),
    	    viewModel: this.viewModel,
    	    bindFontAlign:'{configModel.subtitleAlign}',
    	    bindFont:'{configModel.subtitleFont}',
    	    bindFontDim:'{configModel.subtitleDimension}',
    	    bindFontStyle:'{configModel.subtitleStyle}',
    	    bindColor:'{configModel.subtitleColor}'
    	});
        
    	this.stylePanelNoData = Ext.create('Sbi.chart.designer.StylePopup',{
    	    title: LN('sbi.chartengine.configuration.nodatastyle'),
    	    viewModel: this.viewModel,
    	    bindFontAlign:'{configModel.nodataAlign}',
    	    bindFont:'{configModel.nodataFont}',
    	    bindFontDim:'{configModel.nodataDimension}',
    	    bindFontStyle:'{configModel.nodataStyle}',
    	    bindColor:'{configModel.nodataColor}'
    	});
        
        var stylePanelSubtitle = this.stylePanelSubtitle;
        var stylePanelTitle = this.stylePanelTitle;
        var stylePanelNoData = this.stylePanelNoData;
        
        var item = 
        	[ 
//             {
//				xtype : 'fieldcontainer',
//				layout : 'hbox',
//				id: "fieldContainer1",
//				defaults : {
//					margin: '10 20 10 0'
//				},
//				items : [
//				    this.height, 
//				    this.width,
//				    this.chartOrientation
//				    ]
//             }, 

				// Danilo Ristovski 
				this.height, 
			    this.width,
			    this.chartOrientation,
		
//		{
//			xtype : 'fieldcontainer',
//			layout : 'hbox',
//			defaults : {
//	            margin: '0 20 10 0'
//			},
//			items : [ 
//				font,
//				fontSize,
//				fontStyle
//			]
//		}, 
			    
			    // Danilo Ristovski 
			    font,
				fontSize,
				fontStyle,
				
				colorPickerContainer,
			
		{
			xtype : 'fieldcontainer',
			
			/**
			 * Take the default layout for fields in the main panel. It is applied
			 * also in other fields in this file.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
			
			defaults : 
			{				
				/**
				 * Old implementation (margin) and the new one (padding). It is applied
				 * also in other fields in this file.
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
//	            margin: '10 0 10 0'
				margin: Sbi.settings.chart.configurationStep.marginOfTopFieldset					
			},
			
			items : [ {
	            xtype : 'textfield',
//	            width: 280,
	            width: Sbi.settings.chart.configurationStep.widthOfFields,
	            emptyText : LN("sbi.chartengine.configuration.title.emptyText"),
	            bind : '{configModel.title}',
	            fieldLabel : LN('sbi.chartengine.configuration.title')
	        },{
				xtype : 'button',
				
				// top, right, bottom, left
//	            margin: "10 0 0 10",
				margin: Sbi.settings.chart.configurationStep.marginOfTopFieldsetButtons,					
				
	            text: LN("sbi.chartengine.configuration.configurationButton.label"),
	            handler: function(){
	            	stylePanelTitle.show();
	            }
			}]
	    }, {
	        xtype : 'fieldcontainer',
	        layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
	        
	        defaults : 
	        {	   
				// top, right, bottom, left
//	            margin: '0 0 10 0'
				margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		            
	        },
	        
	        items : [ {
	            xtype : 'textfield',
//	            width: 280,
	            width: Sbi.settings.chart.configurationStep.widthOfFields,
	            emptyText: LN("sbi.chartengine.configuration.subtitle.emptyText"),
	            bind : '{configModel.subtitle}',
	            fieldLabel : LN('sbi.chartengine.configuration.subtitle'),
	            maxWidth:'500'
	        }, {
	            xtype : 'button',
	            
				// top, right, bottom, left
//	            margin: "0 0 0 10",
				margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldsetButtons,	
				
	            text: LN("sbi.chartengine.configuration.configurationButton.label"),
	            handler: function(){
	            	stylePanelSubtitle.show();
	            }
	        }]
	    }, {
	        xtype : 'fieldcontainer',
	        
	        defaults : 
	        {	   
				// top, right, bottom, left
//	            margin: '0 0 10 0'
				margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		            
	        },
	        
	        /**
	         * Align items in the hbox vertically in the center of the hbox.
	         * 
	         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	         */
	        layout:  Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
    		
	        items : [{
	        	id: 'nodata',
	            xtype : 'textfield',
//	            width: 280,
	            width: Sbi.settings.chart.configurationStep.widthOfFields,
	            emptyText: LN("sbi.chartengine.configuration.noData.emptyText"),
	            bind : '{configModel.nodata}',
	            fieldLabel : LN('sbi.chartengine.configuration.nodata'),
	            //labelWidth : '100%'
	        },{
	            xtype : 'button',
	            
				// top, right, bottom, left
//	            margin: "0 0 0 10",
				margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldsetButtons,
				
	            text: LN("sbi.chartengine.configuration.configurationButton.label"),
	            handler: function(){
	            	stylePanelNoData.show();
	            }
	        }]
	    }];
        
        this.add(item);        
 
        /***
         * Toolbar opacity mouse over number field that is needed for the
         * SUNBURST chart type (danilo.ristovski@mht.net)
         */
    	var toolbarOpacMouseOver = Ext.create
    	(
			{
	 			/* Horizontal line with one number field - OPACITY ON MOUSE OVER */                        
				xtype : 'fieldcontainer',
				id: "opacityMouseOver",
	
				defaults : 
				{
					//labelWidth : '100%',	// Commented by Danilo Ristovski
					
					// top, right, bottom, left
	//	            margin:'5 30 0 0'
					margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		
					layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
				},
	
				items: 
				[
					 {
						xtype: 'numberfield',
		//				width: 280,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
						emptyText: LN("sbi.chartengine.configuration.opacityOnMouseOver.emptyText"),
						bind: '{configModel.opacMouseOver}',		
						fieldLabel: LN("sbi.chartengine.configuration.sunburst.opacityMouseOver"),
						maxValue: '100', 	// opacity: 100%
						minValue: '1',		// opacity: 1%
					}
				 ]		                     
			}
		);
 	    
 	    this.add(toolbarOpacMouseOver);
     	
     	if (!ChartUtils.enableOpacityMouseOver()) {
     		this.getComponent("opacityMouseOver").hide();
 		}     	
     	
    	var showLegend = Ext.create({
			xtype: 'checkboxfield',
			id: 'showLegend',
			bind : '{configModel.showLegend}',
			hidden: ChartUtils.disableShowLegendCheck(),	// danristo (danilo.ristovski@mht.net)
				
			// top, right, bottom, left
//	        margin: '20 0 0 0',	
			margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		
			layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
			
			labelSeparator: '',
			fieldLabel: LN('sbi.chartengine.configuration.showlegend'),
		});
        	    
    	this.add(showLegend);    	    	
	},
	
	items : []
});