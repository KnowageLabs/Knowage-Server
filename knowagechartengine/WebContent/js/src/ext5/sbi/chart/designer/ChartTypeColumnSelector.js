Ext.define('Sbi.chart.designer.ChartTypeColumnSelector', {
    extend: 'Ext.panel.Panel',
//    xtype: 'layout-border',
//    requires: [
//        'Ext.layout.container.Border'
//    ],
   // id: "idchartss", // danristo
    config: {
    	region: 'west',
    	
    	chartTypeSelector: {
        	region: 'north',
        	margin: '0 0 5 0',
        	minHeight: 200,
        	html: '<p>Chart type selector</p>'
        },
        
        stylePickerCombo: {
            region: 'center',
            margin: '0 0 5 0',
            minHeight: 200,
//            html: '<p>Columns picker</p>'
        },
        
    	columnsPicker: {
            region: 'center',
            margin: '0 0 5 0',
            minHeight: 200,
            html: '<p>Columns picker</p>'
        },        
        
		categoriesPicker: {
            region: 'south',
            margin: '0 0 5 0',
            minHeight: 200,
            html: '<p>Axises picker</p>'
        }
    },
    
    width: 235,
    
//    collapsible: false,
    
    /**
     * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
//    scrollable: true,
    overflowY: "auto",
    
   // border: true,
	resizable:true, 
	
//    bodyBorder: false,
//    defaults: {
//        split: false,
//    },
    
    constructor: function(config) {
        this.callParent(config);
        
        this.add(config.styleLabel ? config.styleLabel : this.styleLabel);
        this.add(config.stylePickerCombo ? config.stylePickerCombo : this.stylePickerCombo);
        this.add(config.styleLabel2 ? config.styleLabel2 : this.styleLabel2);
        this.add(config.chartTypeSelector ? config.chartTypeSelector : this.chartTypeSelector);        
        this.add(config.columnsPicker ? config.columnsPicker : this.columnsPicker);
        this.add(config.categoriesPicker ? config.categoriesPicker : this.categoriesPicker);
    },
    items: [],
    
    listeners:
	{
    	updateWidth: function(categoriesPicker,seriesPicker)
    	{
    		console.log("USPEO 2");
    		console.log(categoriesPicker);
    		console.log(seriesPicker);
    	}
	}
});