Ext.define('Sbi.chart.designer.ChartConfigurationPalette', {
    alternateClassName: ['ChartConfigurationPalette'],
    id: "chartColorPallete",
    
    /**
     * Extend the JS file with the common configuration for panels that are
     * set on the Configuration tab of the Designer (formerly known as Step 2).
     * 
     * @author: danristo (danilo.ristovski@mht.net)
     */
	extend : 'Sbi.chart.designer.ChartConfigurationRoot',
		
	/**
	 * NOTE: 
	 * This is a temporal solution (for bugs ATHENA-154 and ATHENA-157):
	 * Instead of using dynamic width for this panel (Palette) that relies
	 * on the width of the width of the window of the browser, fix this
	 * value so it can be entirely visible to the end user. 
	 * 
	 * @author: danristo (danilo.ristovski@mht.net)
	 */
	columnWidth: 1,
	//scrollable: "y",
	
	title : LN('sbi.chartengine.configuration.palette'),
	bodyPadding : 10,
	items : [],

	paletteGrid : {},
	
	statics: {
		idSeed: 0
	},
	
	listeners:
	{		
		/**
		 * This event will be fired when chart type or chart style is changed. 
		 * It serves for re-rendering of the color palette on the Configuration 
		 * tab of the Designer.
		 * 
		 * Explanation: The height takes the current number of colors in the 
		 * color palette (defined by the style applied) (numberOfColors), to 
		 * which we add the column header (+1). Then we multiply this sum with
		 * the height of the one (single) color grid row (20) and add 65 as the
		 * offset for the grid (in respect to the panel in which it lies). This
		 * offset is defined basing on the empirical approach.
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		chartTypeChanged: function(numberOfColors)
		{
			this.height = (numberOfColors+1)*20+65;
			this.update();
		}
	},
	
	constructor: function(config) {
        this.callParent(config);

        var colorPalette = config.colorPalette;
        
        var globalScope = this;
        
        this.paletteGrid = Ext.create('Ext.grid.Panel', {
    	    store: Ext.create('Ext.data.ArrayStore', {
            	storeId: 'chartConfigurationPaletteStore',            	
            	fields: ['id', 'gradient','name','order','value']
            }),
            
            /**
             * Enables reordering of items (here, colors) in the grid.
             * 
             * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
             */
            viewConfig: 
            {
                plugins: 
                {
                    ptype: 'gridviewdragdrop'
                }
            },
           
    	    width: 180,
    	    margin:'0 10 0 0',
    	    multiSelect: true,
    	    
    	    columns: [{
    	        text     : LN('sbi.chartengine.configuration.palette.header'),
    	        flex     : 1,
    	        sortable : false,
    	        dataIndex: 'value',
    	        renderer : function(value, meta) {
    	        	meta.style = "background-color:#"+value+";";
    	        }
    	    }]
    	});
        
        // Reset
        this.paletteGrid.store.loadData({});
		// Load json colors
        this.paletteGrid.store.setData(colorPalette);
		
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
	                        		order = grid.store.data.length > 0? 
	                        				grid.store.getAt(grid.store.data.items.length-1).get('order') + 1
	                        				: 1;
	                        	}
	                        	
	                        	grid.store.add({
	                        		id: 'addedColor_' + ChartConfigurationPalette.idSeed,
	                            	gradient:'',
	                            	name: 'addedColor_' + ChartConfigurationPalette.idSeed++,
	                            	order: order,
	                            	value: selColor
	                            });	
	                        	
	                        	/**
	                    		 * When user click on the plus button, the color he picked will be added
	                    		 * to the current grid (palette). Therefore we must extend its height in
	                    		 * order to adapt to the height of the current grid (palette).
	                    		 * 
	                    		 * Explanation: The height takes the current height of the grid (palette).
	                    		 * Then we add the height of the one (single) color grid row (20).
	                    		 * 
	                    		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	                    		 */
	                        	globalScope.height = globalScope.height + 20;
	    	                	globalScope.update();
	    	                	
	    	                	/**
	                    		 * Update the chart configuration panel on the Configuration tab of the 
	                    		 * Designer (former Step 2). This panel servers as a container of the
	                    		 * main configuration and second configuration panel on this tab. This
	                    		 * is important, since the pallete panel changes its height dynamically,
	                    		 * depending on user's action (clicking on the '+' or '-' button). 
	                    		 * 
	                    		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	                    		 */
	    	                	globalScope.ownerCt.ownerCt.update();
	                        }
	                    }
	                })             
	            },{
	                xtype : 'button',
	                text: '-',
	                handler: function(){
	                    var selectedRows = grid.getSelectionModel().getSelection();
	                    if (selectedRows.length) {
	                    	grid.store.remove(selectedRows);
	                    } else {
	                        Ext.Msg.alert(LN('sbi.generic.msg'), LN('sbi.chartengine.configuration.palette.msg.remove'));
	                    }
	                    
	                    /**
                		 * When user click on the minus button, we will take care of the current 
                		 * height of the palette (the one before this change - removal of color(s))
                		 * and the number of colors (items) selected by the user for removal.  
                		 * 
                		 * Explanation: The height takes the current height of the grid (palette).
                		 * Then we take away the number of items (colors) for removing picked by the 
                		 * user. multiplied with the height of the single color grid row (20).
                		 * 
                		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
                		 */
	                    globalScope.height = globalScope.height - selectedRows.length*20;	                    
	                    globalScope.update();
	                    
	                    globalScope.ownerCt.ownerCt.update();
	                }
	            }]
	        }]
	    }
	    ];		
		
		this.add(item);
		
		/**
		 * Set the height of the color palette panel according to current
		 * number of colors in the grid (palette).
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		this.height = (colorPalette.length+1)*20+65;
		this.update();		
	}
});