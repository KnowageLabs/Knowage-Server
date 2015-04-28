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
		minHeight: 300
    },
	
    model: Sbi.chart.designer.AxisesContainerModel,
    
    columns: [
        {
        	text: 'Nome asse',
            dataIndex: 'axisName',
            sortable: false,
            flex: 1
        }
    ],
    enableDragDrop: true,
    margin: '0 0 0 0'
});
