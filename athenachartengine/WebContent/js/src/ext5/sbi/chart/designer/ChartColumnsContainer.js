Ext.define('Sbi.chart.designer.ChartColumnsContainer', {
    extend: 'Ext.grid.Panel',

	requires: [
        'Ext.grid',
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesContainerModel'
    ],
        
    config:{
		minHeight: 300
    },
    
    model: Sbi.chart.designer.AxisesContainerModel,
	    
    columns: [
        {
        	text: 'Nome colonna', 
            dataIndex: 'axisName'
        }
    ],

    enableDragDrop: true,
    
    margin: '0 5 0 0'	
});