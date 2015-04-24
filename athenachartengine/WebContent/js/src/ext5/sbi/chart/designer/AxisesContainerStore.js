Ext.define('Sbi.chart.designer.AxisesContainerStore', {
    extend: 'Ext.data.Store',
    fields: ['axisName'],
    
    data: [],
    
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
        }
    }
});