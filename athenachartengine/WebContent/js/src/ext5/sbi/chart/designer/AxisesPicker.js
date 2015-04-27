Ext.define('Sbi.chart.designer.AxisesPicker', {
	extend: 'Ext.grid.Panel',

    multiSelect: true,
    
    requires: [
        'Ext.grid.*',
        'Sbi.chart.designer.AxisesContainerStore'
    ],
        
    config:{
        flex: 1,
		region: 'south',
		margin: '5 0 5 0',
		minHeight: 300
    },
	
    model: Sbi.chart.designer.AxisesContainerModel,
    
    columns: [
        {
        	text: 'Nome asse',
            dataIndex: 'axisName'
        }
    ],

    enableDragDrop: true,
    
    margin: '0 5 0 0'
    
});