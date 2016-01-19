Ext.define('Sbi.chart.designer.AxisesPicker', {
	extend: 'Ext.grid.Panel',
    multiSelect: true,
    
    /**
     * Providing collapsible attribute/measure (category/serie) picker.
     * 
     *  @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    collapsible: true, 
    padding: "5 20 5 0",
    
    listeners:
	{
    	/**
		 * Update the width of the measures/attributes containers
		 * according to the width of the view of the grid panel
		 * in which items are. We are taking away value the value
		 * of the right padding of the grid panel (side[index]:
		 * top[0], right[1], bottom[2], left[3]).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
    	updateWidth: function()
    	{
    		var paddingRight = this.padding.split(" ");
    		
    		var leftPanelDesignerWidthPercString = Sbi.settings.chart.leftDesignerContainer.widthPercentageOfItem;
    		var leftPanelDesignerWidthPercArray = leftPanelDesignerWidthPercString.split("%");
    		this.setWidth(this.ownerCt.getWidth()*Number(leftPanelDesignerWidthPercArray[0])/100-paddingRight[1]);
    	}
	},
    
    //layout: "fit", // danristo (for IE problem)
    
    requires: [
        'Ext.grid.*',
        'Sbi.chart.designer.AxisesContainerStore'
    ],        
    config:{
        flex: 1,
    	// top,right,bottom,left
//		margin: '0 0 10 0',
		minHeight: 50
    },	
    model: Sbi.chart.designer.AxisesContainerModel,    
    columns: [
        {
        	text: LN('sbi.chartengine.axisespicker.axisname'),
            dataIndex: 'axisName', //'serieColumn' for measures (columns), 'categoriesColumn' for attributes (categories)
            sortable: false,
            flex: 1
        }
    ],
    enableDragDrop: true,    
    enableColumnHide:false,    
    //margin: '5 15 5 0'    
});