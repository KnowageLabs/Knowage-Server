Ext.define('Sbi.chart.designer.AxisesContainerStore', {
    extend: 'Ext.data.Store',
    id: "axisesContainerStore",
	
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