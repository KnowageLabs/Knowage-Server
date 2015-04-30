Ext.define('Sbi.chart.designer.ChartCategoriesContainer', {
    extend: 'Ext.grid.Panel',
	requires: [
        'Ext.grid',
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesContainerModel'
    ],
    config:{
		minHeight: 200
    },
    model: Sbi.chart.designer.AxisesContainerModel,  
    columns: [
        {
        	text: 'Nome categoria', 
            dataIndex: 'axisName',
            sortable: true,
            autoSizeColumn: true
        }
    ],
    enableDragDrop: true,
    enableColumnHide:false,
    margin: '0 5 0 0'	
});