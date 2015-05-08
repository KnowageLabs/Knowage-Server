Ext.define('Sbi.chart.designer.ChartColumnsContainerManager', {
	requires: [
        'Sbi.chart.designer.ChartColumnsContainer'
    ],

	constructor: function(config) {
	    this.initConfig(config);
	    this.callParent();
	},
	
	alternateClassName: ['ChartColumnsContainerManager'],
	
    
    statics: {
    	instanceIdFeed: 0,
    	
    	instanceCounter: 0,
    	
    	COUNTER_LIMIT: 4,
    	
		storePool: [],
		
		yAxisPool: [],

		promptChangeSerieStyle: function (store, rowIndex) {
			var previousInstance = Ext.getCmp('serieStylePopup');
			
			if(previousInstance != undefined) {
				return;
				// previousInstance.destroy();
			}
			
			var serieStylePopup = Ext.create('Sbi.chart.designer.SerieStylePopup', {
				store: store,
				rowIndex: rowIndex,
			});
			
			serieStylePopup.show();
		},

		createChartColumnsContainer: function(idAxisesContainer, id, isDestructible, dragGroup, dropGroup, axisAlias) {
			/*
			 */
			if( ChartColumnsContainerManager.instanceCounter == ChartColumnsContainerManager.COUNTER_LIMIT) {
				Ext.log('Maximum number of ChartColumnsContainer instances reached');
				return null;
			}
	    	
	    	var idChartColumnsContainer = (id && id != '')? id: 'ChartColumnsContainer_' + ChartColumnsContainerManager.instanceIdFeed;
	    	
	    	ChartColumnsContainerManager.instanceIdFeed++;
	    	
	    	ChartColumnsContainerManager.instanceCounter++;
	    	
				var chartColumnsContainerStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
					idAxisesContainer: idChartColumnsContainer,
					autoDestroy : true,
					axisAlias: axisAlias
				});
				
				Ext.Array.push(ChartColumnsContainerManager.storePool, chartColumnsContainerStore);

				var chartColumnsContainer = Ext.create("Sbi.chart.designer.ChartColumnsContainer", {
					id: idChartColumnsContainer,
					idAxisesContainer: idAxisesContainer,
					
					flex: 1,
					viewConfig: {
						plugins: {
							ptype: 'gridviewdragdrop',
							containerScroll: true,
							dragGroup: dragGroup,
							dropGroup: dropGroup
						},
						listeners: {
							beforeDrop: function(node, data, dropRec, dropPosition) {
								if(data.view.id != this.id) {
									data.records[0] = data.records[0].copy('id' + ChartColumnsContainer.idseed++);   
								} 
							}
						}
					},
					store: chartColumnsContainerStore,
					
					closable : isDestructible,
					closeAction : 'destroy',
					beforeDestroy: function(el, eOpts){
						ChartColumnsContainerManager.instanceCounter--;
						
						Ext.Array.remove(ChartColumnsContainerManager.storePool, chartColumnsContainerStore);
						Ext.Array.remove(ChartColumnsContainerManager.yAxisPool, this);

					},
					
					columns: [
						{
							// text: 'Custom name (Y)',
							text: '',
							dataIndex: 'serieColumn',
							flex: 12,
							layout: 'fit',
							sortable: false,
							items: {
			                    xtype: 'textfield',
								allowBlank: false,
			                    emptyText: 'Insert name',
								selectOnFocus: true,
								value: 'Custom name',
			                }
						}, {
			                header: '',
			                dataIndex: 'serieGroupingFunction',
			                flex: 8,
			                field: {
			                    xtype: 'combobox',
			                    typeAhead: true,
			                    triggerAction: 'all',
			                    selectOnTab: true,
			                    store: [
			                        ['AVG','AVG'],
			                        ['COUNT','COUNT'],
			                        ['MAX','MAX'],
			                        ['MIN','MIN'],
			                        ['SUM','SUM']
			                    ],
			                    lazyRender: false,
			                    listClass: 'x-combo-list-small'
			                }
			            },
						{
							menuDisabled: true,
							sortable: false,
							flex: 1,
							xtype: 'actioncolumn',
							items: [{
								icon: 'http://findicons.com/icon/download/66617/paint/24/png',
								tooltip: 'Style',
								handler: function(grid, rowIndex, colIndex) {
									var store = grid.getStore();
									
									ChartColumnsContainerManager.promptChangeSerieStyle(store, rowIndex);
														
								}
							},{
								icon: 'http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/extjs-build/examples/restful/images/delete.png',
								tooltip: 'Remove column',
								handler: function(grid, rowIndex, colIndex) {
									var store = grid.getStore();
									var rec = store.removeAt(rowIndex);
									
								}
							}]
						}
					],
					selModel: {
						selType: 'cellmodel'
					},
					plugins: [{
						ptype: 'cellediting',
						clicksToEdit: 1
					}]
				});
				
				Ext.Array.push(ChartColumnsContainerManager.yAxisPool, chartColumnsContainer);
				
				return chartColumnsContainer;
	    }
	}
});