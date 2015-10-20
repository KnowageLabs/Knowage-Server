Ext.define('Sbi.chart.designer.ChartCategoriesContainer', {
    extend: 'Ext.grid.Panel',
	requires: [
        'Ext.grid',
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesContainerModel',
        'Sbi.chart.designer.ChartUtils'
    ],
    axisData: Sbi.chart.designer.ChartUtils.createEmptyAxisData(),
    config:{
    	/**
    	 * Minimum height of the bottom X-axis (category) panel.
    	 * 
    	 * @modifiedBy: danristo (danilo.ristovski@mht.net)
    	 */
		minHeight: 100
    },
    model: Sbi.chart.designer.AxisesContainerModel,  
    columns: [
        {
        	text: LN('sbi.chartengine.categorycontainer.categoryname'), 
            dataIndex: 'categoryColumn',
            sortable: true,
            autoSizeColumn: true
        }
    ],
    enableDragDrop: true,
    enableColumnHide:false,
    margin: '0 0 0 0'	
});