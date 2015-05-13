Ext.define('Sbi.chart.designer.ChartTypeSelector', {
	extend: 'Ext.grid.Panel',
    title: 'Chart Type Selector',
    margin: '0 0 5 0',
	enableColumnHide:true,
	hideHeaders: true,
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
			var clickHandler = record.get('handler');
			clickHandler();
		}
	}
});


