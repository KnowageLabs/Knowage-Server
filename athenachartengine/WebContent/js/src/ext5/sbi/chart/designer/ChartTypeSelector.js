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
			if(((thisChartType == 'bar' || thisChartType == 'line') && selectedType.toLowerCase() == 'pie')
					|| (thisChartType == 'pie' && selectedType.toLowerCase() != 'pie')) {
				
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
							
							//Reset Series and Categories
							var chartBottomCategoriesContainer = Ext.getCmp('chartBottomCategoriesContainer');
							chartBottomCategoriesContainer.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData());
							
							var categoriesStore = Sbi.chart.designer.Designer.categoriesStore;
							categoriesStore.removeAll();
							
							var serieStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
							
							for(i in serieStorePool) {
								serieStorePool[i].removeAll();
							}
							
							var chartRightAxisesContainer = Ext.getCmp('chartRightAxisesContainer');
							chartRightAxisesContainer.removeAll();

							var chartLeftAxisesContainer = Ext.getCmp('chartLeftAxisesContainer');
							var leftColumnsContainerId = chartLeftAxisesContainer.items.keys[0];
							var leftColumnsContainer = Ext.getCmp(leftColumnsContainerId);
							
							leftColumnsContainer.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData());
//							leftColumnsContainer.tools[0].setValue('');
//							leftColumnsContainer.tools[0].reset();
							
							
							//Select the new chart type
							chartTypeSelector.setChartType(selectedType);
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