Ext.define('Sbi.chart.designer.ChartColumnsContainerManager', {
	requires: [
        'Sbi.chart.designer.ChartColumnsContainer',
        'Sbi.chart.designer.ChartUtils'
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
		
		allAxisData: {},
		
		resetContainers: function() {
			var yAxisPool = this.yAxisPool;
			var storePool = this.storePool;
			
			while(yAxisPool.length > 0) {
				var yAxis = yAxisPool[0];
				yAxis.destroy();
			}
			
			this.instanceCounter = 0;
			this.storePool = [];
			this.yAxisPool = [];
		},
		
		/**
		 * Added for PLOTBANDS tag of the GAUGE chart XML template
		 * (danristo :: danilo.ristovski@mht.net) 
		 */
		setPlotbandsStore: function(jsonTemplate)
		{
			this.allAxisData = jsonTemplate;
		},

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

		createChartColumnsContainer: function(config) {
		
			// (danilo.ristovski@mht.net)
			var idAxisesContainer = config.idAxisesContainer;
			var id = config.id;
			var panelWhereAddSeries = config.panelWhereAddSeries;
			var isDestructible = config.isDestructible;
			var dragGroup = config.dragGroup;
			var dropGroup = config.dropGroup;
			var axis = config.axis;
			var axisTitleTextboxHidden = config.axisTitleTextboxHidden ? config.axisTitleTextboxHidden : false;
			var gearHidden = config.gearHidden ? config.gearHidden : false;
			var plusHidden = config.plusHidden ? config.plusHidden : false;
			
			if( ChartColumnsContainerManager.instanceCounter == ChartColumnsContainerManager.COUNTER_LIMIT) {
				Ext.log('Maximum number of ChartColumnsContainer instances reached');
				return null;
			}
	    	
			ChartColumnsContainerManager.instanceIdFeed++;
	    	var idChartColumnsContainer = (id && id != '')? id: 'Axis_' + ChartColumnsContainerManager.instanceIdFeed;
	    		    	
	    	ChartColumnsContainerManager.instanceCounter++;
	    	
	    	var axisAlias = (axis && axis != null)? axis.alias: idChartColumnsContainer;
			var chartColumnsContainerStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
				idAxisesContainer: idChartColumnsContainer,
				autoDestroy : true,
				axisAlias: axisAlias
			});
			
			Ext.Array.push(ChartColumnsContainerManager.storePool, chartColumnsContainerStore);
			
			var titleText = (axis && axis != null && axis.TITLE && axis.TITLE.text &&  axis.TITLE.text != null) ? axis.TITLE.text : '';
			
			var axisData = (axis && axis != null)? 
					Sbi.chart.designer.ChartUtils.convertJsonAxisObjToAxisData(axis) : 
						Sbi.chart.designer.ChartUtils.createEmptyAxisData();
					
			var chartColumnsContainer = Ext.create("Sbi.chart.designer.ChartColumnsContainer", {
				id: idChartColumnsContainer,
				idAxisesContainer: idAxisesContainer,
				axisData: axisData,
				emptyText : LN('sbi.chartengine.designer.emptytext.dragdropmeasures'),	
				
				store: chartColumnsContainerStore,
				
				controller: Ext.create('Ext.app.ViewController', {
			        onTitleChange: function (barTextField, textValue) {
			        	this.view.axisData.titleText = textValue;
			        }
			    }),
			    listeners: {
			    	updateAxisTitleValue: function(textValue) {
			        	this.axisData.titleText = textValue;
			        	
			        	var textfieldAxisTitleId = this.id + '_TitleTextfield';
			    		var textfieldAxisTitle = Ext.getCmp(textfieldAxisTitleId);
			    		textfieldAxisTitle.setValue(textValue);
			    	}
			    },
			    setAxisData: function(axisData) {
  					this.axisData = axisData;
  					this.fireEvent('updateAxisTitleValue', axisData.titleText);
  				},
  				getAxisData: function() {
  					return this.axisData;
  				},
				minHeight: 300,
				height: 300,
				flex: 1,
				viewConfig: {
					plugins: {
						ptype: 'gridviewdragdrop',
						dragGroup: dragGroup,
						dropGroup: dropGroup
					},
					listeners: {
						beforeDrop: function(node, data, dropRec, dropPosition) {	
							
							/**
							 * Prevent user from defining multiple serie items; if this part is 
							 * not provided, error appears
							 * @author: danristo (danilo.ristovski@mht.net)
							 */
							var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
							var enableAddAndSum = chartType != 'SUNBURST' && chartType != 'WORDCLOUD' && 
													chartType != 'TREEMAP' && chartType != 'HEATMAP';													
							
							/**
							 * Benedetto's code
							 * @commentBy: danristo (danilo.ristovski@mht.net)
							 */
							//var chosenTheme = Sbi.chart.designer.Designer.styleName;
							//var styledThemeBaseTemplate = Sbi.chart.designer.Designer.getConfigurationForStyle(chosenTheme)[chartType.toLowerCase()];
//							
//							console.log('styledThemeBaseTemplate -> ', styledThemeBaseTemplate);
//							
//							var applySeries = true;
//							
//							var thisTemplate = Sbi.chart.designer.Designer.exportAsJson();
//							var tempNewMergedTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
//									thisTemplate, 
//									styledThemeBaseTemplate,
//									{
//										applySeries: applySeries
//									});
//							
//							console.log('tempNewMergedTemplate -> ', tempNewMergedTemplate);
							
							/**
  	  						 * Prevent taking more than one serie from the container when we have
  	  						 * one of these chart types.
  	  						 * @author: danristo (danilo.ristovski@mht.net)
  	  						 */
  	  						if (data.records.length > 1 && (chartType == 'SUNBURST' || chartType == 'WORDCLOUD' || 
									chartType == 'TREEMAP' || chartType == 'HEATMAP')) {
  	  							return false;
  							} 
							
							if (enableAddAndSum || (!enableAddAndSum && this.store.data.length == 0)) {
								// *_* The original code
								if(data.view.id != this.id) {
									
									data.records[0] = data.records[0].copy('droppedSerie_' + ChartColumnsContainer.idseed++);
									if( !data.records[0].get('serieGroupingFunction')) {
										data.records[0].set('serieGroupingFunction', 'SUM');
									}
									if( !data.records[0].get('axisName')) {
										var serieColumn = data.records[0].get('serieColumn', 'SUM');
										data.records[0].set('axisName', serieColumn);
										
										// (danilo.ristovski@mht.net)								
										if(Ext.getCmp("chartParallelLimit").hidden == false && 
												Ext.getCmp("chartParallelLimit") != undefined && 
													Ext.getCmp("chartParallelLimit") != null)
										{
											Ext.getCmp("chartParallelLimit").addItem(data.records[0]);
										}										
									}
									
									if( !data.records[0].get('serieGroupingFunction')) {
										data.records[0].set('serieGroupingFunction', 'SUM');
									}
								}	
								
//								if (chartType == "GAUGE") 
//								{
//									console.log(data.records[0]);
//								}
								
							} else  {
								return false;
							}
							
//							if (chartType == 'GAUGE') {
////								this.ownerCt.ownerCt.fireEvent("newSerieItem", data.records[0]);
//							
//								var chartLeftAxisesContainer = Ext.getCmp('chartLeftAxisesContainer');
//								chartLeftAxisesContainer.fireEvent("newSerieItem", data.records[0]);
//							}
						}
					}
				},
				
				title: {
					hidden: true 
				}, 
				tools:[
				       Ext.create('Ext.form.TextField', {
				    	   
				    	id: idChartColumnsContainer + '_TitleTextfield',
				    	
				    	/**
				    	 * True for the SUNBURST, WORDCLOUD, TREEMAP and PARALLEL charts
				    	 * @author: danristo (danilo.ristovski@mht.net)
				    	 */ 
				    	hidden: axisTitleTextboxHidden,	
				    	
						flex: 10,
						allowBlank:  true,
						selectOnFocus: true,
						emptyText: LN('sbi.chartengine.designer.emptytext.axistitle'),
						value: titleText,
						listeners: {
				            change: 'onTitleChange'
				        }
					}),
					
					// STYLE POPUP
					{
					    type:'gear',
					    tooltip: LN('sbi.chartengine.columnscontainer.tooltip.setaxisstyle'),
					    id: "stylePopupLeftAxis_"+idChartColumnsContainer, // (danilo.ristovski@mht.net)
					    // True for the SUNBURST, WORDCLOUD, TREEMAP and PARALLEL charts (danilo.ristovski@mht.net)
					    hidden: gearHidden,	
					    
					    flex: 1,
					    handler: function(event, toolEl, panelHeader) {
					    	var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
					    	if(chartType.toUpperCase() != 'PIE') {
					    		var thisChartColumnsContainer = panelHeader.ownerCt;
					    		
					    		var axisStylePopup = Ext.create('Sbi.chart.designer.AxisStylePopup', {
					    			axisData: thisChartColumnsContainer.getAxisData(),
					    			allAxisData: ChartColumnsContainerManager.allAxisData,
					    			isYAxis: true
					    		});
					    		
					    		/**
					    		 * For the HEATMAP chart type hide these two fieldsets inside the
					    		 * Axis style configuration popup window since we don't need them.
					    		 * 
					    		 * @author: danristo (danilo.ristovski@mht.net)
					    		 */
						    	if (chartType.toUpperCase() == 'HEATMAP')
					    		{
						    		axisStylePopup.getComponent('majorGridFieldSetYAxis').hide();
						    		axisStylePopup.getComponent('minorGridFieldSetYAxis').hide();
					    		}							    	
						    	
					    		axisStylePopup.show();
					    	}
					    	
						}
					},
					
					// PLUS BUTTON
					{
					    type:'plus',
					    tooltip: LN('sbi.chartengine.columnscontainer.tooltip.addaxis'),
					    id: "plusLeftAxis_"+idChartColumnsContainer, // (added by: danilo.ristovski@mht.net)
					    // *_* True for the SUNBURST, WORDCLOUD, TREEMAP and PARALLEL charts
					    hidden: plusHidden || (panelWhereAddSeries == null),
					    
					    flex: 1,
					    handler: function(event, toolEl, panelHeader) {
					    	
					    	
					    	var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
					    						    	
					    	if(chartType.toUpperCase() != 'PIE') {
					    		if (!panelWhereAddSeries.isVisible()) {
					    			panelWhereAddSeries.setVisible(true);
					    		}
					    	
					    		ChartAxisesContainer.addToAxisesContainer(panelWhereAddSeries);
					    	}
					    	
					    }
						
						// *_* Old code included in: (hidden: plusHidden || (panelWhereAddSeries == null))
					    //hidden: (panelWhereAddSeries == null)
					}
					
				],
				
				closable : isDestructible,
				closeAction : 'destroy',
				beforeDestroy: function(el, eOpts){
					ChartColumnsContainerManager.instanceCounter--;
					Ext.Array.remove(ChartColumnsContainerManager.storePool, chartColumnsContainerStore);
					Ext.Array.remove(ChartColumnsContainerManager.yAxisPool, this);
				},
				
				hideHeaders: true,
				columns: {
					items: [{
						dataIndex: 'serieColumn',
						flex: 12,
						layout: 'fit',
						sortable: false,
					}, {
		                dataIndex: 'serieGroupingFunction',
		                flex: 8,
						layout: 'fit',
						sortable: false,
						editor: {
							xtype: 'combobox',
							editable: false,
							displayField: 'label',
							valueField: 'value',
							store: [
		                        ['AVG','AVG'],
		                        ['COUNT','COUNT'],
		                        ['MAX','MAX'],
		                        ['MIN','MIN'],
		                        ['SUM','SUM']
		                    ],
							fields: ['value', 'label']
						}
		            },
					{
						menuDisabled: true,
						sortable: false,
						flex: 1,
  						align : 'center',
						xtype: 'actioncolumn',
						id: "actionColumnLeftAxis_"+idChartColumnsContainer,
						items: [{
							icon: '/athena/themes/sbi_default/img/createTemplate.jpg',
							
							tooltip: LN('sbi.chartengine.columnscontainer.tooltip.style'),
							handler: function(grid, rowIndex, colIndex) {
								var store = grid.getStore();
								
								ChartColumnsContainerManager.promptChangeSerieStyle(store, rowIndex);
							}
						},{
							icon: '/athena/themes/sbi_default/img/delete.gif',
							tooltip: LN('sbi.chartengine.columnscontainer.tooltip.removecolumn'),
							handler: function(grid, rowIndex, colIndex) {
								var store = grid.getStore();
								var item = store.getAt(rowIndex);
								
								var serieColumn = item.get('serieColumn');
								var serieName = item.get('axisName');
								
								Ext.Msg.show({
	            					title : '',
	            					message : Sbi.locale.sobstituteParams(
	      								LN('sbi.chartengine.designer.removeserie'), 
	      								[serieColumn, serieName]),
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
	            							
	            							// (danristo :: danilo.ristovski@mht.net) 
	            							if(Ext.getCmp("chartParallelLimit").hidden == false && 
	    											Ext.getCmp("chartParallelLimit") != undefined && 
	    												Ext.getCmp("chartParallelLimit") != null)
            								{
	            								Ext.getCmp("chartParallelLimit").removeItem(store.getAt(rowIndex));
            								}	    							
	            							
	            							var rec = store.removeAt(rowIndex);
	            							
	            							// need to force reload for showing the emptyText message
	            							if(store.getCount() == 0) {
	            								store.reload();
	            							}
	            						}
	            					}
	            				});
							}
						}]
					}
				]},
				selModel: {
					selType: 'cellmodel'
				},
				plugins: [{
					ptype: 'cellediting',
					clicksToEdit: 1
				}]
			});
			
			/** 
			 * If the chart is one of the specified types hide the "createTemplate" 
			 * icon that is attached to every record (item) inside the left (Y) axis
			 * panel (between the aggregation type and 'remove' button) and it is dedicated
			 * for specifying Series and Tooltip details. 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			if (chartType == "SUNBURST" || chartType == "WORDCLOUD" ||
					chartType == "TREEMAP" || chartType == "PARALLEL" ||
						chartType == "HEATMAP")
			{
				chartColumnsContainer.columns[2].items[0].iconCls = "x-hidden";				
			}	
			
			Ext.Array.push(ChartColumnsContainerManager.yAxisPool, chartColumnsContainer);
			
			return chartColumnsContainer;
	    }
	}
});