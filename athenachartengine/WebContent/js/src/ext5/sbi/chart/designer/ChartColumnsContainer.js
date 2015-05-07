Ext.define('Sbi.chart.designer.ChartColumnsContainer', {
    extend: 'Ext.grid.Panel',
	alternateClassName: ['ChartColumnsContainer'],
	requires: [
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesContainerModel'
    ],
	statics: {
        idseed: 1,
	},
    config:{
		minHeight: 150,
		width: '100%',
    },
    model: Sbi.chart.designer.AxisesContainerModel,  
    columns: [
        {
        	text: 'Nome colonna', 
            dataIndex: 'serieColumn',
            sortable: true,
            flex: 1
        }
    ],
    enableDragDrop: true,
    enableColumnHide:false,
    margin: '0 0 0 0'	
});