Ext.define('Sbi.chart.designer.ChartConfigurationMainContainer', {
	extend : 'Ext.panel.Panel',
	requires : [
	            'Sbi.chart.designer.StylePopup',
	            'Sbi.chart.designer.FontStyleCombo'],
	title : 'Configurazione generica',
	id: 'page2Container',
	bodyPadding : 10,
	
	//**********************//
	
	height: {
		xtype : 'textfield',
		bind : '{configModel.height}',
		fieldLabel : 'Altezza',
	},
	width: {
		xtype : 'textfield',
		bind : '{configModel.width}',
		fieldLabel : 'Larghezza',
	},
	
	chartOrientation : Ext.create('Sbi.chart.designer.ChartOrientationCombo',{
		bind : '{configModel.orientation}'
	}),
	
	font : Ext.create('Sbi.chart.designer.FontCombo',{
		bind : '{configModel.font}'
	}),
	
   	fontSize : Ext.create('Sbi.chart.designer.FontDimCombo',{
   		bind : '{configModel.fontDimension}'
   	}),
   	
   	fontStyle : Ext.create('Sbi.chart.designer.FontStyleCombo',{
   		bind : '{configModel.fontStyle}'
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
    		customLabel : 'Colore sfondo',
       		fieldBind: '{configModel.backgroundColor}'
       	});
        
        var colorPickerContainer = this.colorPickerContainer;
        
        this.stylePanelTitle = Ext.create('Sbi.chart.designer.StylePopup',{
        	title:'Stile Titolo',
        	viewModel: this.viewModel,
        	bindFontAlign:'{configModel.titleAlign}',
        	bindFont:'{configModel.titleFont}',
        	bindFontDim:'{configModel.titleDimension}',
        	bindFontStyle:'{configModel.titleStyle}',
        	bindColor:'{configModel.titleColor}'
        });
        this.stylePanelSubtitle = Ext.create('Sbi.chart.designer.StylePopup', {
    	    title:'Stile Sottotitolo',
    	    viewModel: this.viewModel,
    	    bindFontAlign:'{configModel.subtitleAlign}',
    	    bindFont:'{configModel.subtitleFont}',
    	    bindFontDim:'{configModel.subtitleDimension}',
    	    bindFontStyle:'{configModel.subtitleStyle}',
    	    bindColor:'{configModel.subtitleColor}'
    	});
        
    	this.stylePanelNoData = Ext.create('Sbi.chart.designer.StylePopup',{
    	    title:'Stile "No Data"',
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
	            fieldLabel : 'Titolo',
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
	            fieldLabel : 'Sottotitolo',
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
	            fieldLabel : 'Messaggio "No data"' ,
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
	        name: 'showLegend', 
	        id: 'showLegend',
	        margin: '20 0 0 0',
	        value: true,
	        labelSeparator: '',
	        fieldLabel: 'Mostra Legenda',
	        handler: function(checkbox, checked) {
	            Ext.getCmp('page2Legend').setDisabled(!checked);
	        }
	    }
		];
	        
	    this.add(item);
        
	},
	
	items : [],
});