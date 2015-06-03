Ext.define('Sbi.chart.designer.ChartConfigurationMainContainer', {
	extend : 'Ext.panel.Panel',
	requires : [
	            'Sbi.chart.designer.StylePopup',
	            'Sbi.chart.designer.FontStyleCombo'],
	title : LN('sbi.chartengine.configuration'),
	bodyPadding : 10,
	//**********************//
	
	height: {
		xtype : 'numberfield',
		bind : '{configModel.height}',
		fieldLabel : LN('sbi.chartengine.configuration.height'),
	},
	width: {
		xtype : 'numberfield',
		bind : '{configModel.width}',
		fieldLabel : LN('sbi.chartengine.configuration.width'),
	},
	
	chartOrientation : Ext.create('Sbi.chart.designer.ChartOrientationCombo',{
		id: 'chartOrientationCombo',
		bind : '{configModel.orientation}'
	}),
	
	font : Ext.create('Sbi.chart.designer.FontCombo',{
		bind : '{configModel.font}'
	}),
	
   	fontSize : Ext.create('Sbi.chart.designer.FontDimCombo',{
   		bind : '{configModel.fontDimension}'
   	}),
   	
   	fontStyle : Ext.create('Sbi.chart.designer.FontStyleCombo',{
   		bind : '{configModel.fontWeight}'
   	}),
   	
   	
   	colorPickerContainer : {},
   	
	stylePanelSubtitle : {},
	stylePanelTitle : {},
	stylePanelNoData : {},
	
	//**********************//
	
	
	
	constructor: function(config) {
        this.callParent(config);
        this.viewModel = config.viewModel;
        var height = this.height;
        var width = this.width;
        var chartOrientation = this.chartOrientation;
        var font = this.font;
        var fontSize = this.fontSize;
        var fontStyle = this.fontStyle;
        
        this.colorPickerContainer = Ext.create('Sbi.chart.designer.ColorPickerContainer',{
    		viewModel: this.viewModel,
    		customLabel : LN('sbi.chartengine.configuration.backgroundcolor'),
       		fieldBind: '{configModel.backgroundColor}'
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
        
        var item = [ {
			xtype : 'fieldcontainer',
			layout : 'hbox',
			defaults : {
				labelWidth : '100%',
				margin: '10 20 10 0'
			},
			items : [
			    height, 
			    width,
			    chartOrientation
			    ]
		}, {
			xtype : 'fieldcontainer',
			layout : 'hbox',
			defaults : {
				labelWidth : '100%',
	            margin: '0 20 10 0'
			},
			items : [ 
				colorPickerContainer,
				font,
				fontSize,
				fontStyle
			]
		}, {
			xtype : 'fieldcontainer',
			layout : 'hbox',
			defaults : {
	            margin: '0 0 10 0'
			},
			items : [ {
	            xtype : 'textfield',
	            bind : '{configModel.title}',
	            fieldLabel : LN('sbi.chartengine.configuration.title'),
	        },{
				xtype : 'button',
	            text: 'St',
	            handler: function(){
	            	stylePanelTitle.show();
	            }
			}    
			]
	    }, {
	        xtype : 'fieldcontainer',
	        layout : 'hbox',
	        defaults : {
	            margin: '0 0 10 0'
	        },
	        items : [ {
	            xtype : 'textfield',
	            bind : '{configModel.subtitle}',
	            fieldLabel : LN('sbi.chartengine.configuration.subtitle'),
	            maxWidth:'500',
	        }, {
	            xtype : 'button',
	            text: 'St',
	            handler: function(){
	            	stylePanelSubtitle.show();
	            }
	        }
	     	]
	    }, {
	        xtype : 'fieldcontainer',
	        layout : 'hbox',
	        items : [{
	        	id: 'nodata',
	            xtype : 'textfield',
	            bind : '{configModel.nodata}',
	            fieldLabel : LN('sbi.chartengine.configuration.nodata') ,
	            labelWidth : '100%',
	        },{
	            xtype : 'button',
	            text: 'St',
	            handler: function(){
	            	stylePanelNoData.show();
	            }
	        }
	     	]
	    },{
	        xtype: 'checkboxfield',
	        id: 'showLegend',
	        bind : '{configModel.showLegend}',
	        margin: '20 0 0 0',
	        labelSeparator: '',
	        fieldLabel: LN('sbi.chartengine.configuration.showlegend'),
	    }
		];
	        
	    this.add(item);
        
	},
	
	items : [],
});