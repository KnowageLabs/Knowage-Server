Ext.define('Sbi.chart.designer.AxisesPicker', {
	extend: 'Ext.grid.Panel',

    requires: [
        'Ext.grid.*',
        'Sbi.chart.designer.AxisesContainerStore',
    ],
        
    config:{
		region: 'south',
		margin: '5 0 5 0',
		minHeight: 200,
    },
	
    columns: [
        {
        	text: 'Nome asse', 
            flex:  1,
            dataIndex: 'axisName'
        }
    ],

    enableDragDrop: true,
    margin: '0 5 0 0',
});