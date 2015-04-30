Ext.define('ConfColorPicker', {
    extend: 'Ext.menu.ColorPicker',
	value : '000000'
});

var layoutData = [{
					name : 'Verticale',
					value : 'v'
				}, {
					name : 'Orizzontale',
					value : 'h'
				}];

var fontArray = [
    ['Arial'],
    ['Times New Roman'],
    ['Tahoma'],
    ['Verdana']
];

var fontStore = Ext.create('Ext.data.ArrayStore', {
    fields: [
        {name: 'name'}
    ],
    data: fontArray
});

var fontCombo = Ext.define('FontCombo',{
    extend:'Ext.form.ComboBox', 
    store: fontStore,
    displayField: 'name',
    fieldLabel : 'Carattere',
    tdCls: '',
    listeners: {
        change: function(sender, newValue, oldValue, opts) {
            this.inputEl.setStyle('font-family', newValue);
        }
    }
});


var dimArray = [[8],[9],[10],[11],[12],[14],[16],[18],[20],[22],[24],[26],[28],[36],[48],[72]];

var dimStore = Ext.create('Ext.data.ArrayStore', {
    fields: [
        {name: 'name'}
    ],
    data: dimArray
});

Ext.define('DimCombo', {
    extend:'Ext.form.ComboBox',
    store: dimStore,
    displayField: 'name',
    valueField: 'name',
    fieldLabel : 'Dimensione'
});


var alignArray = [ {
					name : 'Destra',
					value : 'dx'
				}, {
					name : 'Sinistra',
					value : 'sx'
				}, {
					name : 'Centro',
					value : 'cx'
				} ];

Ext.define('AllineamentoCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: alignArray
    },
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Allineamento',
    queryMode : 'local',
});

var fontStyleArray = [ {
					name : 'Bold',
					value : 'b'
				}, {
					name : 'Normal',
					value : 'n'
				}, {
					name : 'Italico',
					value : 'i'
				}, {
					name : 'Sottolineato',
					value : 's'
				}];

Ext.define('FontStyleCombo', {
    extend:'Ext.form.ComboBox',
    store: {
        fields: [ 'name','value' ],
        data: fontStyleArray
    },
    displayField: 'name',
    valueField: 'value',
    fieldLabel : 'Stile',
    queryMode : 'local',
});

Ext.define('StylePanel', {
    extend : 'Ext.form.Panel',
    width: 300,
    height: 180,
    floating: true,
    draggable: true,
    closable : true,
    closeAction: 'hide',
    bodyPadding: 10,
});

var stylePanelS = Ext.create('StylePanel', {
    title:'Stile Sottotitolo',
    items : [
        Ext.create('AllineamentoCombo'),        
        {
            xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'colorFieldSt',
                    xtype : 'field',
                    readOnly : true,
                    name : 'firstName',
                    fieldLabel : 'Colore',
                }, {
                    xtype : 'button',
                    menu : Ext.create('ConfColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor
                                        + '; background-image: none;';
                                Ext.getCmp('colorFieldSt').setFieldStyle(style);
                            }
                        }
                    }),
                    margin : '0 0 0 -15',
					padding : '1 0'                   
                }]
		},
    	Ext.create('FontCombo'),
        Ext.create('DimCombo'),
        Ext.create('FontStyleCombo'),
    ]
    
	
});
var stylePanelT = Ext.create('StylePanel',{
    title:'Stile Titolo',
    items : [
        Ext.create('AllineamentoCombo'),
        
        {
            xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'colorFieldT',
                    xtype : 'field',
                    readOnly : true,
                    name : 'firstName',
                    fieldLabel : 'Colore',
                }, {
                    xtype : 'button',
                    menu : Ext.create('ConfColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor
                                        + '; background-image: none;';
                                Ext.getCmp('colorFieldT').setFieldStyle(style);
                            }
                        }
                    }),
                    margin : '0 0 0 -15',
					padding : '1 0'                   
                }]
		},
        
        Ext.create('FontCombo'),
        Ext.create('DimCombo'),
        Ext.create('FontStyleCombo'),
        
	]
});


var stylePanelND = Ext.create('StylePanel',{
    title:'Stile "No Data"',
    items : [
        Ext.create('AllineamentoCombo'),
        
        {
            xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'colorFieldND',
                    xtype : 'field',
                    readOnly : true,
                    name : 'firstName',
                    fieldLabel : 'Colore',
                }, {
                    xtype : 'button',
                    menu : Ext.create('ConfColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor
                                        + '; background-image: none;';
                                Ext.getCmp('colorFieldND').setFieldStyle(style);
                            }
                        }
                    }),
                    margin : '0 0 0 -15',
					padding : '1 0'                   
                }]
		},
        
        Ext.create('FontCombo'),
        Ext.create('DimCombo'),
        Ext.create('FontStyleCombo'),
        
	]
});

Ext.define('Sbi.chart.designer.ChartConfigurationGeneral', {
	extend : 'Ext.panel.Panel',

	title : 'Configurazione generica',

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
				data : layoutData
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
        
        {
            xtype : 'fieldcontainer',
            layout : 'hbox',
            items: [
                {
                    id : 'colorField',
                    xtype : 'field',
                    readOnly : true,
                    name : 'firstName',
                    fieldLabel : 'Colore sfondo',
                }, {
                    xtype : 'button',
                    menu : Ext.create('ConfColorPicker',{
                        listeners : {
                            select : function(picker, selColor) {
                                var style = 'background-color: #' + selColor
                                + '; background-image: none;';
                                Ext.getCmp('colorField').setFieldStyle(style);
                            }
                        }
                    }),
                    margin : '0 0 0 -15',
                    padding : '1 0'                   
                }]
		},

		Ext.create('FontCombo'),
       	Ext.create('DimCombo')
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
               stylePanelT.show()
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
        }, {
            xtype : 'button',
            text: 'St',
            handler: function(){
                stylePanelS.show()
            }
        }
     	]
    }, {
        xtype : 'fieldcontainer',
        layout : 'hbox',
        items : [{
            xtype : 'textfield',
            name : 'nodata',
            fieldLabel : 'Messaggio "No data"' ,
        }, {
            xtype : 'button',
            text: 'St',
            handler: function(){
                stylePanelND.show()
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


//***********************************************
// 				
//***********************************************


var positionData = [ {
					name : 'Sotto',
					value : 'bottom'
				}, {
					name : 'Sopra',
					value : 'top'
				}, {
					name : 'Destra',
					value : 'top'
				}, {
					name : 'Sinistra',
					value : 'left'
				}];



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
            }]
        }
     ]
    
});




var paletteStore = Ext.create('Ext.data.ArrayStore', {
    fields: ['value']
});

var paletteGrid = Ext.create('Ext.grid.Panel', {
    xtype: 'array-grid',
    store: paletteStore,
    width: 100,
    margin:'0 10 0 0',
    viewConfig: {
        enableTextSelection: true
    },
    columns: [{
        text     : 'Colore',
        flex     : 1,
        sortable : false,
        dataIndex: 'value',
        renderer : function(value, meta) {
        	meta.style = "background-color:#"+value+";";
            return value;
        }
    }]
    }
);


Ext.define('Sbi.chart.designer.ChartConfigurationPalette', {
	extend : 'Ext.panel.Panel',
	columnWidth: 0.3,
	title : 'Palette Colori',
	bodyPadding : 10,
    items : [{
        xtype : 'fieldcontainer',
        layout : 'hbox',
        items: [
           	paletteGrid,	
        {
            xtype : 'fieldcontainer',
        	layout : 'vbox',
            defaults: {
                arrowCls: '',
                width: 22
            },
            items: [{
                xtype : 'button',
                text: '+',
                menu : Ext.create('ConfColorPicker',{
                    listeners : {
                        select : function(picker, selColor) {
                            paletteStore.add({value:selColor});
                        }
                    }
                }),                 
            },{
                xtype : 'button',
                text: '-',              
            }]
        }]
    }
    ],
    
});


Ext.define('Sbi.chart.designer.ChartConfigurationSub', {
    extend: 'Ext.panel.Panel',
    xtype: 'layout-column',
    border:false,
    requires: [
        'Ext.layout.container.Column'
    ],
    
    layout: 'column',
    defaults:{
        height: 200,
    },
    item: [ ],
    
    constructor: function(config) {
        this.callParent(config);
        var legend = Ext.create('Sbi.chart.designer.ChartConfigurationLegend');
		var palette = Ext.create('Sbi.chart.designer.ChartConfigurationPalette');
        
        this.add(legend);
		this.add(palette);
    }
    
});

Ext.define('Sbi.chart.designer.ChartConfiguration',{
	extend: 'Ext.panel.Panel',
	border: false,
	layout: 'vbox',
	item: [ ],
	defaults:{
        width: 850,
    },
    constructor: function(config) {
        this.callParent(config);
        var general = Ext.create('Sbi.chart.designer.ChartConfigurationGeneral');
		var sub = Ext.create('Sbi.chart.designer.ChartConfigurationSub');
        
        this.add(general);
		this.add(sub);
    }	
})

