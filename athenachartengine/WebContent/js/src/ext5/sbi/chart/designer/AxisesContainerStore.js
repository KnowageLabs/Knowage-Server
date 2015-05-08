Ext.define('Sbi.chart.designer.AxisesContainerStore', {
    extend: 'Ext.data.Store',
	
	config: {
		idAxisesContainer: 'idAxisesContainer',
		axisAlias: ''
	},

	
    model: Sbi.chart.designer.AxisesContainerModel,
    
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
        }
    }
});