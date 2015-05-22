Ext.define('Sbi.chart.designer.ChartTypeSelector', {
	extend: 'Ext.grid.Panel',
    title: 'Chart Type Selector',
    margin: '0 0 5 0',
	enableColumnHide:true,
	hideHeaders: true,
	
	chartType: '',
	
	columns: [
	    {
	    	dataIndex: 'iconUrl',
	    	flex: 1,
	    	renderer: function(value){
	            return '<img style="width:32px;height:32px;" src="' + value + '" />';
	        },
	    }, {
	        dataIndex: 'name' 
	    }
	],
	listeners : {
		rowclick: function(table, record, tr, rowIndex, e, eOpts ) {
			var selectedType = record.get('type');
			this.chartType = selectedType;
		},
	},
	
	setChartType: function(type) {
		this.chartType = type;
		
		// selection on the table
		for(i = 0; i < this.store.data.length; i++) {
			var row = this.store.getAt(i);
			
			if(type === row.get('type')) {
				this.getSelectionModel().select(i);
				break;
			}
		}
	},

	getChartType: function() {
		return this.chartType;
	}
});