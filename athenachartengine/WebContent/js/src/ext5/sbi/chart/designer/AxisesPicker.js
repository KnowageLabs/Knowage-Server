Ext.define('Sbi.chart.designer.AxisesPicker', {
	extend: 'Ext.grid.Panel',
    multiSelect: true,
    requires: [
        'Ext.grid.*',
        'Sbi.chart.designer.AxisesContainerStore'
    ],        
    config:{
//        flex: 1,
//		margin: '5 0 5 0',
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
    margin: '5 15 5 0'    
});