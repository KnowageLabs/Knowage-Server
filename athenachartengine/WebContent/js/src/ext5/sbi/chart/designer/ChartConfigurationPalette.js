Ext.define('Sbi.chart.designer.ChartConfigurationPalette', {
	extend : 'Ext.panel.Panel',
	columnWidth: 0.3,
	title : 'Palette Colori',
	bodyPadding : 10,
	items : [],

	paletteGrid : {},
	
	constructor: function(config) {
        this.callParent(config);
        
        this.paletteGrid = Ext.create('Ext.grid.Panel', {
    	    store: Ext.create('Ext.data.ArrayStore', { 
            	storeId: 'chartConfigurationPaletteStore',
            	
            	fields: ['gradient','name','order','value'], 
            	
            	data: config.colorPalette
            }),
    	    
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
    	});
        
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

	                        	var order = 0;
	                        	if(grid.store.data && grid.store.data.items) {
	                        		order = grid.store.getAt(grid.store.data.items.length-1).get('order') + 1;
	                        	}
	                        	
	                        	console.log(order);
	                        	
	                        	grid.store.add({
	                            	gradient:'',
	                            	name:'',
	                            	order: order,
	                            	value:selColor
	                            });
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