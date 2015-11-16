Ext.define('Sbi.chart.designer.AdvancedEditor',{
	extend: 'Ext.tree.Panel',
	requires: [
       'Sbi.chart.designer.ChartUtils'
    ],
    rootVisible: false,
    hideHeaders: true,
    
    treeNodeIsCollapsing: false,
    treeNodeIsExpanding: false,
    dataChanged: false,
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
        }
    }],
    
    setChartData: function(json) {
	    var formattedJson = Sbi.chart.designer.ChartUtils.convertJsonToTreeFormat(json);

	    // Flags
	    this.treeNodeIsCollapsing = false;
	    this.treeNodeIsExpanding = false;
	    this.dataChanged = false;
	    
	    var advancedEditor = this;
	    
	    this.reconfigure(
    		Ext.create('Ext.data.TreeStore',{
		    	root: formattedJson,
		    	
		    	listeners: {
		    		update: function( store, record, operation, modifiedFieldNames, details, eOpts ) {
		    			
		    			// Workaround in case this event is invoked while a node is only expandind or collapsing
		    			if(!advancedEditor.treeNodeIsCollapsing && !advancedEditor.treeNodeIsExpanding) {
		    				advancedEditor.dataChanged = true;
		    			}
		    			
		    			advancedEditor.treeNodeIsCollapsing = false;
		    			advancedEditor.treeNodeIsExpanding = false;
		    		},
		    		nodebeforecollapse: function( node, eOpts ) {
		    			advancedEditor.treeNodeIsCollapsing = true;
		    		},
		    		nodebeforeexpand: function( node, eOpts ) {
		    			advancedEditor.treeNodeIsExpanding = true;
		    		}		    		
		    	}
		    })
	    );
    },
	getChartData: function() {
		var formattedJson = this.store.getRoot().data;
		var originalJson = Sbi.chart.designer.ChartUtils.convertTreeFormatToJson(formattedJson, true);
		
		return originalJson;
	}
});