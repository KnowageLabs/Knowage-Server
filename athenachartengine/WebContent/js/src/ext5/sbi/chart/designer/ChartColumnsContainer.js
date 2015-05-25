Ext.define('Sbi.chart.designer.ChartColumnsContainer', {
    extend: 'Ext.grid.Panel',
	alternateClassName: ['ChartColumnsContainer'],
	requires: [
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesContainerModel'
    ],
	statics: {
        idseed: 1
	},
	
    config:{
		minHeight: 150,
		flex: 1,
		axisData: {}
		
    },
    model: Sbi.chart.designer.AxisesContainerModel, 
    
    columns: [
        {
        	text: 'Default column name', 
            dataIndex: 'serieColumn',
            sortable: true,
            flex: 1
        }
    ],
    hideHeaders: true,
    enableColumnHide:false,
    margin: '0 0 0 0'	
});