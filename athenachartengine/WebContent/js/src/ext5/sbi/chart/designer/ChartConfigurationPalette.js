Ext.define('Sbi.chart.designer.ChartConfigurationPalette', {
	extend : 'Ext.panel.Panel',
	columnWidth: 0.3,
	title : 'Palette Colori',
	bodyPadding : 10,
	items : [],
	
	config : {
		paletteGrid : Ext.create('Ext.grid.Panel', {
		    store: Ext.create('Ext.data.ArrayStore', { fields: ['value']} ),
		    width: 180,
		    margin:'0 10 0 0',
		    multiSelect: true,
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
		)
	},
	
	constructor: function(config) {
        this.callParent(config);
        var grid = this.paletteGrid;
        
		var item = [{
	        xtype : 'fieldcontainer',
	        layout : 'hbox',
	        items: [
	           	grid,	
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
	                            grid.store.add({value:selColor});
	                        }
	                    }
	                }),                 
	            },{
	                xtype : 'button',
	                text: '-',
	                handler: function(){
	                    var selectedRows = grid.getSelectionModel().getSelection();
	                    if (selectedRows.length) {
	                    	grid.store.remove(selectedRows);
	                    } else {
	                        Ext.Msg.alert('Messaggio', 'Selezionare almeno un colore da eliminare');
	                    }
	                }
	            }]
	        }]
	    }
	    ];
		
		this.add(item);
	},
	
    
    
});