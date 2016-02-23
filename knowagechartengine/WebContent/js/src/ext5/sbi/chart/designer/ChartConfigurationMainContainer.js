Ext.define('Sbi.chart.designer.ChartConfigurationMainContainer', {
	
	extend : 'Sbi.chart.designer.ChartConfigurationRoot',
	
	requires : [
	            'Sbi.chart.designer.components.ColorPicker',
	            'Sbi.chart.designer.ChartOrientationCombo',
	            'Sbi.chart.designer.ColorPickerContainer',
	            'Sbi.chart.designer.FontCombo',
	            'Sbi.chart.designer.FontDimCombo',
	            'Sbi.chart.designer.FontStyleCombo',
	            'Sbi.chart.designer.StylePopup'
	            ],
	            
	title : LN('sbi.chartengine.configuration'),
	bodyPadding : 10,

	fieldDefaults: 
	{
        anchor: '100%'
	},
		
//	height: null,
//	width: null,
	
	/**
	 * Overrides the margin set in the ChartConfigurationRoot.js.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	margin: "0 10 5 0",	
	
//	overflowX: "auto",
//	overflowY: "auto",
	
	chartOrientation : null,
	
	font : Ext.create('Sbi.chart.designer.FontCombo',{
		bind : '{configModel.font}',
		fieldLabel : LN("sbi.chartengine.configuration.fontFamily"),
		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields,	// Danilo Ristovski
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
	
	constructor: function(config) {
		
        this.callParent(config);
        this.viewModel = config.viewModel;
       
        var globalThis = this;        
       
        this.height = {
    		xtype : 'numberfield',
    		id: "chartHeightNumberfield", 	// added by: Danilo Ristovski (for the validation)
    		minValue: 0,					// added by: Danilo Ristovski (for the validation)
    		width: Sbi.settings.chart.configurationStep.widthOfFields,			// Danilo Ristovski
    		padding: Sbi.settings.chart.configurationStep.paddingOfTopFields,	// Danilo Ristovski
    		emptyText: LN("sbi.chartengine.configuration.height.emptyText"),    		
    		bind : '{configModel.height}',
    		fieldLabel : LN('sbi.chartengine.configuration.height'),
    		hidden: ChartUtils.isChartHeightDisabled()  // added by: Giorgio Federici (https://production.eng.it/jira/browse/KNOWAGE-548)
    	};
        
        /**
         * TODO: Insert comments
         * Danilo
         */
        this.heightDimTypePicker = 
        {
        	xtype: "combo",
        	
        	store: 
        	{
                fields: [ 'typeValue', 'typeAbbr' ],                
                data: [ {"typeValue": "px", "typeAbbr":'pixels'}, {"typeValue": "%","typeAbbr":'percentage'} ]
            },
            
            displayField: 'typeValue',
            valueField: 'typeAbbr',
            value: Sbi.settings.chart.configurationStep.defaultDimensionType,
            bind : '{configModel.heightDimType}',
            fieldLabel : "",          
            editable : false,            
            margin: Sbi.settings.chart.configurationStep.marginOfTopFieldsetButtons,          
            width: 40,
        };
        
        this.width = {
    		xtype : 'numberfield',
    		id: "chartWidthNumberfield",
    		minValue: 0,					// added by: Danilo Ristovski (for the validation)
    		width: Sbi.settings.chart.configurationStep.widthOfFields,			// Danilo Ristovski
    		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields,	// Danilo Ristovski
    		emptyText: LN("sbi.chartengine.configuration.width.emptyText"),    		
    		bind : '{configModel.width}',
    		fieldLabel : LN('sbi.chartengine.configuration.width'),
    		hidden: ChartUtils.isChartWidthDisabled() // modifiedby: Giorgio Federici (https://production.eng.it/jira/browse/KNOWAGE-548)
    	};
        
        /**
         * TODO: Insert comments
         * Danilo
         */
        this.widthDimTypePicker = 
        {
        	xtype: "combo",
        	
        	store: {
                fields: [ 'typeValue', 'typeAbbr' ],
                
                data: [ {"typeValue": "px", "typeAbbr":'pixels'}, {"typeValue": "%","typeAbbr":'percentage'} ]
            },
            
            displayField : 'typeValue',
            valueField : 'typeAbbr',
            value: Sbi.settings.chart.configurationStep.defaultDimensionType,
            bind : '{configModel.widthDimType}',
            fieldLabel : "",          
            editable : false,            
            margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldsetButtons,         
            width: 40,
        };
        
        this.chartOrientation = Ext.create('Sbi.chart.designer.ChartOrientationCombo',{
    		id: 'chartOrientationCombo',
    		bind : '{configModel.orientation}',    		
    		hidden: ChartUtils.isChartOrientationDisabled(),
    		padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields	// Danilo Ristovski
    	});
        
        var font = this.font;
        var fontSize = this.fontSize;
        var fontStyle = this.fontStyle;
        
//        this.colorPickerContainer = Ext.create('Sbi.chart.designer.ColorPickerContainer',{
        this.colorPickerContainer = Ext.create('Sbi.chart.designer.components.ColorPicker',{
        	fieldLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
        	emptyText: LN('sbi.chartengine.configuration.backgroundColor.emptyText'),
    		bind : '{configModel.backgroundColor}',
    		viewModel: this.viewModel,
       		fieldBind: '{configModel.backgroundColor}',
       		emptyText: LN('sbi.chartengine.configuration.backgroundColor.emptyText'),
       		width: Sbi.settings.chart.configurationStep.widthOfFields,
       		padding: Sbi.settings.chart.configurationStep.paddingOfTopFields	// Danilo Ristovski
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
        
        this.heightFieldset = Ext.create
        (
    		'Ext.form.FieldSet', 
    		{
				id: "heightFieldset", // (danristo :: danilo.ristovski@mht.net) 
				//title: LN('sbi.chartengine.axisstylepopup.minorgrid'),
//				defaults: {
//					anchor: '100%',
//					labelAlign : 'left'
//				},
//				layout: 'hbox',
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
					margin : Sbi.settings.chart.configurationStep.marginOfInnerFieldset
				},
				padding: "0 0 0 0",	// Danilo Ristovski
	    		border: "hidden",
				items : [this.height,this.heightDimTypePicker]
    		}
		);
        
        this.widthFieldset = Ext.create
        (
    		'Ext.form.FieldSet', 
    		{
				id: "widthFieldset", // (danristo :: danilo.ristovski@mht.net) 
				//title: LN('sbi.chartengine.axisstylepopup.minorgrid'),
//				defaults: {
//					anchor: '100%',
//					labelAlign : 'left'
//				},
//				layout: 'hbox',
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
					margin : Sbi.settings.chart.configurationStep.marginOfInnerFieldset
				},
	    		padding: "0 0 0 0",	// Danilo Ristovski
	    		border: "hidden",
				items : [this.width,this.widthDimTypePicker]
    		}
		);
        
        var item = 
    	[ 
			// Danilo Ristovski 
			this.heightFieldset, 
			this.widthFieldset,
		    this.chartOrientation,

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
					margin: Sbi.settings.chart.configurationStep.marginOfTopFieldset					
				},
				
				items : 
				[ 
				 	{
			            xtype : 'textfield',
			            width: Sbi.settings.chart.configurationStep.widthOfFields,
			            emptyText : LN("sbi.chartengine.configuration.title.emptyText"),
			            bind : '{configModel.title}',
			            fieldLabel : LN('sbi.chartengine.configuration.title')
				 	},
				 	
				 	{
						xtype : 'button',
						
						margin: Sbi.settings.chart.configurationStep.marginOfTopFieldsetButtons,					
						
			            text: LN("sbi.chartengine.configuration.configurationButton.label"),
			            
			            handler: function()
			            {
			            	stylePanelTitle.show();
			            }
				 	}
			 	]
			}, 
			
			{
		        xtype : 'fieldcontainer',
		        
		        layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
		        
		        defaults : 
		        {	   
					margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,					
		        },
		        
		        items : 
	        	[ 
	        	  	{
	        	  		xtype : 'textfield',
			            width: Sbi.settings.chart.configurationStep.widthOfFields,
			            emptyText: LN("sbi.chartengine.configuration.subtitle.emptyText"),
			            bind : '{configModel.subtitle}',
			            fieldLabel : LN('sbi.chartengine.configuration.subtitle'),
			            maxWidth:'500'
			        }, 
			        
			        {
			            xtype : 'button',
			            
						margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldsetButtons,	
						
			            text: LN("sbi.chartengine.configuration.configurationButton.label"),
			            
			            handler: function()
			            {
			            	stylePanelSubtitle.show();
			            }
			        }
		        ]
			}, 
			
			{
		        xtype : 'fieldcontainer',
		        
		        layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
		        
		        defaults : 
		        {	   
					margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,						
		        },
	    		
		        items : 
	        	[
	        	 	{
			        	id: 'nodata',
			            xtype : 'textfield',
			            width: Sbi.settings.chart.configurationStep.widthOfFields,
			            emptyText: LN("sbi.chartengine.configuration.noData.emptyText"),
			            bind : '{configModel.nodata}',
			            fieldLabel : LN('sbi.chartengine.configuration.nodata'),
	        	 	},
	        	 	
	        	 	{
			            xtype : 'button',
			            
						margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldsetButtons,
						
			            text: LN("sbi.chartengine.configuration.configurationButton.label"),
			            handler: function()
			            
			            {
			            	stylePanelNoData.show();
			            }
	        	 	}
        	 	]
			}
		];
        
        this.add(item);        
 
        /***
         * Toolbar opacity mouse over number field that is needed for the
         * SUNBURST chart type.  
         *        
         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
         */
    	var toolbarOpacMouseOver = Ext.create
    	(
			{
	 			/* Horizontal line with one number field - OPACITY ON MOUSE OVER */                        
				xtype : 'fieldcontainer',
				id: "opacityMouseOver",
	
				defaults : 
				{
					margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		
					layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
				},
	
				items: 
				[
					 {
						xtype: 'numberfield',
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
     	
     	if (!ChartUtils.isOpacityMouseOverEnabled()) {
     		this.getComponent("opacityMouseOver").hide();
 		}     	
     	
    	var showLegend = Ext.create({
			xtype: 'checkboxfield',
			id: 'showLegend',
			bind : '{configModel.showLegend}',
			hidden: ChartUtils.isShowLegendDisabled(),	// danristo (danilo.ristovski@mht.net)
				
			margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		
			layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
			
			labelSeparator: '',
			fieldLabel: LN('sbi.chartengine.configuration.showlegend'),
			
			/**
			 * TODO: add comments
			 * Danilo
			 */
			listeners:
			{
				change: function(a,b)
				{
					Ext.getCmp("chartLegend").fireEvent("showLegendClicked",b);
				}
			}
		});
        	
    	this.add(showLegend);   
    	
    	/**
    	 * The checkbox field for showing/hiding the PARALLEL table.
    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	 */
    	var checkboxShowTableParallel = Ext.create
    	(
			{
				xtype: 'checkboxfield',
				id: 'showTableParallel',
				bind : '{configModel.showTableParallel}',
				hidden: !ChartUtils.isParallelPanelEnabled(),	
					
				margin: Sbi.settings.chart.configurationStep.marginOfInnerFieldset,		
				layout: Sbi.settings.chart.configurationStep.layoutFieldsInMainPanel,
				
				labelSeparator: '',
				fieldLabel: LN("sbi.chartengine.configuration.parallel.showTable"),
			}	
    	);
    	
    	this.add(checkboxShowTableParallel);  
    	
    	if (!ChartUtils.isParallelPanelEnabled())
		{
    		this.getComponent("showTableParallel").hide();
		}
	},
	
	items : []
});