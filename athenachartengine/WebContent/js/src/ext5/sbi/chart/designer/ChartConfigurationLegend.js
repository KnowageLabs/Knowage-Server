var positionData = [ {
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
} ];

var stylePanelLegend = Ext.create('Sbi.chart.designer.StylePopup', {
    title:'Stile Legenda',
});

Ext.define('Sbi.chart.designer.ChartConfigurationLegend', {
    id: 'chartLegend',
	extend : 'Ext.panel.Panel',
	columnWidth: 0.7,
	title : 'Legenda',

	bodyPadding : 10,
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
                name : 'pos',
                displayField : 'name',
                valueField : 'value',
                store : {
                    fields : ['name', 'value'],
                    data : positionData
                }
            } , {
                xtype : 'combo',
                queryMode : 'local',
                value : 'h',
                triggerAction : 'all',
                forceSelection : true,
                editable : false,
                fieldLabel : 'Layout',
                name : 'layout',
                displayField : 'name',
                valueField : 'value',
                store : {
                    fields : ['name', 'value'],
                    data : layoutData
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
                name:'floating', 
                id: 'floating',
                labelSeparator: '',
                fieldLabel: 'Floating'
            },{
                xtype : 'textfield',
                name : 'x',
                fieldLabel : 'X',
                maxWidth:'50'
            },{
                xtype : 'textfield',
                name : 'y',
                fieldLabel : 'Y',
                maxWidth:'50'
            },{
                xtype : 'button',
                text: 'St',
                handler: function(){
                    stylePanelLegend.show()
                }
            }]
        }
     ]
    
});