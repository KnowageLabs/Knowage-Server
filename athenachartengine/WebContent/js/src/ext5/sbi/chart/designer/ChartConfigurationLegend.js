Ext.define('Sbi.chart.designer.ChartConfigurationLegend', {
	extend : 'Ext.panel.Panel',
	columnWidth : 0.7,
	title : 'Legenda',
	id : 'page2Legend',
	bodyPadding : 10,
	config : {
		stylePanelLegend : Ext.create('Sbi.chart.designer.StylePopup', {
		    title:'Stile Legenda',
		    bindFontAlign:'{configModel.legendAlign}',
		    bindFont:'{configModel.legendFont}',
		    bindFontDim:'{configModel.legendDimension}',
		    bindFontStyle:'{configModel.legendStyle}',
		    bindColor:'{configModel.legendColor}'
		}),
	},
    items : [
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
                bind : '{legendPosition}',
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
                bind : '{legendLayout}',
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
                bind :'{legnendFloating}', 
                id: 'floating',
                labelSeparator: '',
                fieldLabel: 'Floating'
            },{
                xtype : 'textfield',
                bind : '{legendX}',
                fieldLabel : 'X',
                maxWidth:'50'
            },{
                xtype : 'textfield',
                bind : '{legendY}',
                fieldLabel : 'Y',
                maxWidth:'50'
            },{
                xtype : 'button',
                text: 'St',
                handler: function(){
            		Ext.getCmp('page2Legend').getStylePanelLegend().show();
                }
            }]
        }
     ]
    
});