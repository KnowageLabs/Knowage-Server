Ext.define('Sbi.chart.designer.ChartConfigurationLegend', {
	extend : 'Sbi.chart.designer.ChartConfigurationRoot',
	
	/**
	 * NOTE: 
	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
	 * Instead of using dynamic width for this panel (Legend) that relies
	 * on the width of the width of the window of the browser, fix this
	 * value so it can be entirely visible to the end user. Also the
	 * height will be defined as the fixed value.
	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
//	columnWidth : 0.7,
	columnWidth: 1,
	//width: "100%", 
	height: 120,
	
	id: "chartLegend",
	title : LN('sbi.chartengine.configuration.legend'),
	bodyPadding : 10,
	items : [],
	stylePanelLegend : {},
	bind:{
		disabled:'{!configModel.showLegend}'
	},
	constructor: function(config) {
        this.callParent(config);
        this.viewModel = config.viewModel;
        
        this.stylePanelLegend = Ext.create('Sbi.chart.designer.StylePopup', {
    	    title:LN('sbi.chartengine.configuration.legendstyle'),
    	    viewModel: this.viewModel,
    	    bindFontAlign:'{configModel.legendAlign}',
    	    bindFont:'{configModel.legendFont}',
    	    bindFontDim:'{configModel.legendDimension}',
    	    bindFontStyle:'{configModel.legendStyle}',
    	    bindColor:'{configModel.legendColor}',
    	    bindBorderWidth:'{configModel.legendBorderWidth}',
    	   	bindBackgroundColor:'{configModel.legendBackgroundColor}'
    	});
        
        var stylePanelLegend = this.stylePanelLegend;
        
        var item = [
                     {
                    	 xtype : 'fieldcontainer',
                    	 layout : 'hbox',
                    	 defaults : {
                    		 labelWidth : '100%',
                    		 margin:'0 20 10 0'
                    	 },
                    	 items: [{
                    		 xtype : 'combo',
                    		 queryMode : 'local',
                    		 value : 'bottom',
                    		 triggerAction : 'all',
                    		 forceSelection : true,
                    		 editable : false,
                    		 fieldLabel : LN('sbi.chartengine.configuration.position'),
                    		 bind : '{configModel.legendPosition}',
                    		 displayField : 'name',
                    		 valueField : 'value',
                    		 emptyText: LN("sbi.chartengine.configuration.legend.position.emptyText"),
                    		 store : {
                    			 fields : ['name', 'value'],
                    			 data : [ {
                    				 name : LN('sbi.chartengine.configuration.position.b'),
                    				 value : 'bottom'
                    			 }, {
                    				 name : LN('sbi.chartengine.configuration.position.t'),
                    				 value : 'top'
                    			 }, {
                    				 name : LN('sbi.chartengine.configuration.position.m'),
                    				 value : 'middle'
                    			 } ]
                    		 }
                    	 } , {
                    		 xtype : 'combo',
                    		 queryMode : 'local',
                    		 value : 'horizontal',
                    		 triggerAction : 'all',
                    		 forceSelection : true,
                    		 editable : false,
                    		 fieldLabel : LN('sbi.chartengine.configuration.layout'),
                    		 bind : '{configModel.legendLayout}',
                    		 displayField : 'name',
                    		 valueField : 'value',
                    		 emptyText: LN("sbi.chartengine.configuration.legend.layout.emptyText"),
                    		 store : {
                    			 fields : [ 'name', 'value' ],
                    			 data : [ {
                    				 name : LN('sbi.chartengine.configuration.orientation.v'),
                    				 value : 'vertical'
                    			 }, {
                    				 name : LN('sbi.chartengine.configuration.orientation.h'),
                    				 value : 'horizontal'
                    			 } ]
                    		 }
                    	 }]

                     },  {            
                    	 xtype : 'fieldcontainer',
                    	 layout : 'hbox',
                    	 defaults : {
                    		 labelWidth : '100%',
                    		 margin:'0 30 0 0'
                    	 },
                    	 items: [{
                    		 xtype: 'checkboxfield',
                    		 bind :'{configModel.legendFloating}', 
                    		 id: 'floating',
                    		 labelSeparator: '',
                    		 fieldLabel: LN('sbi.chartengine.configuration.floating')
                    	 },{
                    		 xtype : 'numberfield',
                    		 bind : '{configModel.legendX}',
                    		 fieldLabel : LN('sbi.chartengine.configuration.x'),
                    		 maxWidth: 280,
                    		 emptyText: LN("sbi.chartengine.configuration.legend.xOffset.emptyText")
                    	 },{
                    		 xtype : 'numberfield',
                    		 bind : '{configModel.legendY}',
                    		 fieldLabel : LN('sbi.chartengine.configuration.y'),
                    		 maxWidth: 280,
                    		 emptyText: LN("sbi.chartengine.configuration.legend.yOffset.emptyText")
                    	 },{
                    		 xtype : 'button',
                    		 text: LN('sbi.chartengine.configuration.stylebutton'),
                    		 handler: function(){
                    			 stylePanelLegend.show();
                    		 }
                    	 }]
                     }
                     ]; 

        this.add(item);
	}

});