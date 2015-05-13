Ext.define('Sbi.chart.designer.ChartConfigurationLegend', {
	extend : 'Ext.panel.Panel',
	columnWidth : 0.7,
	title : 'Legenda',
	id : 'page2Legend',
	bodyPadding : 10,
	items : [],
	stylePanelLegend : {},
	
	constructor: function(config) {
        this.callParent(config);
        this.viewModel = config.viewModel;
        
        this.stylePanelLegend = Ext.create('Sbi.chart.designer.StylePopup', {
    	    title:'Stile Legenda',
    	    viewModel: this.viewModel,
    	    bindFontAlign:'{configModel.legendAlign}',
    	    bindFont:'{configModel.legendFont}',
    	    bindFontDim:'{configModel.legendDimension}',
    	    bindFontStyle:'{configModel.legendStyle}',
    	    bindColor:'{configModel.legendColor}'
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
                    		 fieldLabel : 'Posizione',
                    		 bind : '{configModel.legendPosition}',
                    		 displayField : 'name',
                    		 valueField : 'value',
                    		 store : {
                    			 fields : ['name', 'value'],
                    			 data : [ {
                    				 name : 'Sotto',
                    				 value : 'bottom'
                    			 }, {
                    				 name : 'Sopra',
                    				 value : 'top'
                    			 }, {
                    				 name : 'Destra',
                    				 value : 'right'
                    			 }, {
                    				 name : 'Sinistra',
                    				 value : 'left'
                    			 } ]
                    		 }
                    	 } , {
                    		 xtype : 'combo',
                    		 queryMode : 'local',
                    		 value : 'h',
                    		 triggerAction : 'all',
                    		 forceSelection : true,
                    		 editable : false,
                    		 fieldLabel : 'Layout',
                    		 bind : '{configModel.legendLayout}',
                    		 displayField : 'name',
                    		 valueField : 'value',
                    		 store : {
                    			 fields : [ 'name', 'value' ],
                    			 data : [ {
                    				 name : 'Verticale',
                    				 value : 'v'
                    			 }, {
                    				 name : 'Orizzontale',
                    				 value : 'h'
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
                    		 bind :'{configModel.legnendFloating}', 
                    		 id: 'floating',
                    		 labelSeparator: '',
                    		 fieldLabel: 'Floating'
                    	 },{
                    		 xtype : 'textfield',
                    		 bind : '{configModel.legendX}',
                    		 fieldLabel : 'X',
                    		 maxWidth:'50'
                    	 },{
                    		 xtype : 'textfield',
                    		 bind : '{configModel.legendY}',
                    		 fieldLabel : 'Y',
                    		 maxWidth:'50'
                    	 },{
                    		 xtype : 'button',
                    		 text: 'St',
                    		 handler: function(){
                    			 stylePanelLegend.show();
                    		 }
                    	 }]
                     }
                     ]; 

        this.add(item);
	}



});