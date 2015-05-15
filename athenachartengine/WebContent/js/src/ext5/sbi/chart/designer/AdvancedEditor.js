Ext.define('Sbi.chart.designer.AdvancedEditor',{
	extend: 'Ext.tree.Panel',
	requires: [
       'Sbi.chart.designer.ChartUtils'
    ],
//    title: 'Simple Tree',
    rootVisible: false,
    hideHeaders: true,
    plugins: [
        Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        })
    ],
    selType: 'cellmodel',
    columns: [{
        xtype: 'treecolumn',
        dataIndex: 'key',
        flex: 1
    },{
        dataIndex: 'value',
        align: 'left',
        flex: 4,
        getEditor: function(record) {
            var fieldType = 'displayfield';
            if(record.get('type')) {
                if(record.get('type') == 'string') fieldType = 'textfield';
                if(record.get('type') == 'number') fieldType = 'numberfield';
                if(record.get('type') == 'boolean') fieldType = 'checkboxfield';
            }
            return Ext.create('Ext.grid.CellEditor', {
                field: {
                    xtype: fieldType,
                    selectOnFocus: true
                }
            });
        },
    }],
    setChartData: function(json) {
	    var formattedJson = Sbi.chart.designer.ChartUtils.convertJsonToTreeFormat(json);
	    
	    this.reconfigure(
    		Ext.create('Ext.data.TreeStore',{
		    	root: formattedJson
		    })
	    );
    },
	getChartData: function() {
		var formattedJson = this.store.getRoot().data;
		var originalJson = Sbi.chart.designer.ChartUtils.convertTreeFormatToJson(formattedJson, true);
		
		console.log('originalJson: ', originalJson);
	}
});