Ext.define('Sbi.chart.designer.AxisesContainerStore', {
    extend: 'Ext.data.Store',
    
    model: Sbi.chart.designer.AxisesContainerModel,
    
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
        }
    }
});