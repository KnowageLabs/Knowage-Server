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
			
			var store = this.store;
			var selectionModel = this.getSelectionModel();
			
			var chartTypeSelector = this;

			var thisChartType = this.chartType.toLowerCase();
			
			var globalThis = this;
			
			var compatybilityLookup = 
			{
				bar: ['line'],
				line: ['bar'],
				pie: [],
				sunburst: [],
				wordcloud: [],
				treemap: [],
				parallel: []
			};
			
			var compatibleTypes = false;
			
			// foreach
			for(i in compatybilityLookup[selectedType.toLowerCase()]) 
			{
				var compatibleChart = compatybilityLookup[selectedType.toLowerCase()][i];
				compatibleTypes = compatibleTypes || compatibleChart == thisChartType;
			}
			
			if(!compatibleTypes) 
			{		
		
//			if(((thisChartType == 'bar' || thisChartType == 'line') && selectedType.toLowerCase() == 'pie')
//					|| (thisChartType == 'pie' && selectedType.toLowerCase() != 'pie')) {
				
				Ext.Msg.show({
					title : '',
					message : LN('sbi.chartengine.designer.charttype.changetype'),
					icon : Ext.Msg.QUESTION,
					closable : false,
					buttons : Ext.Msg.OKCANCEL,
					buttonText : 
					{
						ok : LN('sbi.chartengine.generic.ok'),
						cancel : LN('sbi.generic.cancel')
					},
					fn : function(buttonValue, inputText, showConfig){
						if (buttonValue == 'ok') {
							
							Sbi.chart.designer.Designer.cleanAxesSeriesAndCategories();
							
							//Select the new chart type
							chartTypeSelector.setChartType(selectedType);
							
							globalThis.fireEvent("newrowclick");
						} 
						else if (buttonValue == 'cancel') {
							for(var i = 0; i < store.data.length; i++) {
								var row = store.getAt(i);
								
								if(thisChartType.toLowerCase() === row.get('type').toLowerCase()) {
									selectionModel.select(i);
									break;
								}
							}
						}
					}
				});
			}
			else {
				chartTypeSelector.setChartType(selectedType);
				globalThis.fireEvent("newrowclick");
			}
		}
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