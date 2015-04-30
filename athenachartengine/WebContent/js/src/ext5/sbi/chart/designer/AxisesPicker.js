Ext.define('Sbi.chart.designer.AxisesPicker', {
	extend: 'Ext.grid.Panel',

    multiSelect: true,
    
    requires: [
        'Ext.grid.*',
        'Sbi.chart.designer.AxisesContainerStore'
    ],
        
    config:{
        flex: 1,
		margin: '5 0 5 0',
		minHeight: 150
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
    
    enableColumnHide:false,
    
    margin: '0 5 0 0'
    
});