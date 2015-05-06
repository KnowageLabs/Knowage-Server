var paletteStore = Ext.create('Ext.data.ArrayStore', {
    fields: ['value']
});

var paletteGrid = Ext.create('Ext.grid.Panel', {
    store: paletteStore,
    width: 100,
    margin:'0 10 0 0',
    
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
                menu : Ext.create('Ext.menu.ColorPicker',{
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