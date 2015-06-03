Ext.define('Sbi.chart.designer.ChartTypeSelector', {
	extend: 'Ext.grid.Panel',
    margin: '0 0 5 0',
    title: {
		hidden: true 
	},
	enableColumnHide:true,
	hideHeaders: true,
	
	chartType: '', // 'BAR', 'LINE' or 'PIE'
	
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

			this.setChartType(selectedType);
		},
	},
	
	setChartType: function(type) {
		this.chartType = type;
		
		// selection on the table
		for(var i = 0; i < this.store.data.length; i++) {
			var row = this.store.getAt(i);
			
			if(type === row.get('type')) {
				this.getSelectionModel().select(i);
				break;
			}
		}
		
		var chartOrientationCombo = Ext.getCmp('chartOrientationCombo');
		var chartRightAxisesContainer = Ext.getCmp('chartRightAxisesContainer');
		if(this.chartType.toUpperCase() == 'PIE') {
			if(chartOrientationCombo != undefined) {
				chartOrientationCombo.disable();
			}
			if(chartRightAxisesContainer != undefined) {
				chartRightAxisesContainer.disable();
			}
		} else {
			if(chartOrientationCombo != undefined) {
				chartOrientationCombo.enable();
			}
			if(chartRightAxisesContainer != undefined) {
				chartRightAxisesContainer.enable();
			}
		}
	},

	getChartType: function() {
		return this.chartType;
	}
});