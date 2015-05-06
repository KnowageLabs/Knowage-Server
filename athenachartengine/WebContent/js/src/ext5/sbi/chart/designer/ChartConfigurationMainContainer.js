Ext.define('Sbi.chart.designer.ChartConfigurationMainContainer', {
	extend : 'Ext.panel.Panel',
	requires : ['Sbi.chart.designer.StylePopup'],
	title : 'Configurazione generica',
	id: 'page2Container',
	config : {
		stylePanelSubtitle : Ext.create('Sbi.chart.designer.StylePopup', {
		    title:'Stile Sottotitolo',
		}),
		stylePanelTitle : Ext.create('Sbi.chart.designer.StylePopup',{
		    title:'Stile Titolo',
		}),
		stylePanelNoData : Ext.create('Sbi.chart.designer.StylePopup',{
		    title:'Stile "No Data"',
		})
	},
	
	bodyPadding : 10,
	
	items : [ {
		xtype : 'fieldcontainer',
		layout : 'hbox',
		defaults : {
			labelWidth : '100%',
			margin: '10 20 10 0'
		},
		items : [ {
			xtype : 'textfield',
			name : 'height',
			fieldLabel : 'Altezza',
		}, {
			xtype : 'textfield',
			name : 'width',
			fieldLabel : 'Larghezza',
		}, {
			xtype : 'combo',
			queryMode : 'local',
			value : 'h',
			triggerAction : 'all',
			forceSelection : true,
			editable : false,
			fieldLabel : 'Verso',
			name : 'verso',
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
		} ]
	}, {
		xtype : 'fieldcontainer',
		layout : 'hbox',
		defaults : {
			labelWidth : '100%',
            margin: '0 20 10 0'
		},
		items : [ 
			Ext.create('Sbi.chart.designer.ColorPickerContainer'),
			Ext.create('Sbi.chart.designer.FontCombo'),
	       	Ext.create('Sbi.chart.designer.FontDimCombo')
		]
	}, {
		xtype : 'fieldcontainer',
		layout : 'hbox',
		defaults : {
            margin: '0 0 10 0'
		},
		items : [ {
            xtype : 'textfield',
            name : 'title',
            fieldLabel : 'Titolo',
        }, {
			xtype : 'button',
            text: 'St',
            handler: function(){
                Ext.getCmp('page2Container').getStylePanelTitle().show();
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
            name : 'subtitle',
            fieldLabel : 'Sottotitolo',
            maxWidth:'500',
        }, {
            xtype : 'button',
            text: 'St',
            handler: function(){
                Ext.getCmp('page2Container').getStylePanelSubtitle().show();
            }
        }
     	]
    }, {
        xtype : 'fieldcontainer',
        layout : 'hbox',
        items : [{
        	id: 'nodata',
            xtype : 'textfield',
            name : 'nodata',
            fieldLabel : 'Messaggio "No data"' ,
            labelWidth : '100%',
        }, {
            xtype : 'button',
            text: 'St',
            handler: function(){
                Ext.getCmp('page2Container').getStylePanelNoData().show();
            }
        }
     	]
    },{
        xtype: 'checkboxfield',
        name:'showLegend', 
        id: 'showLegend',
        margin: '20 0 0 0',
        value: true,
        labelSeparator: '',
        fieldLabel: 'Mostra Legenda',
        handler: function(checkbox, checked) {
            Ext.getCmp('chartLegend').setDisabled(!checked);
        }
    }

	],
});