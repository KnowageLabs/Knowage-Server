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
			
			// Commented (22.10)
			//var gaugeLabels = config.gaugeLabels;
			var axisTitleTextboxHidden = config.axisTitleTextboxHidden ? config.axisTitleTextboxHidden : false;
			var gearHidden = config.gearHidden ? config.gearHidden : false;
			var plusHidden = config.plusHidden ? config.plusHidden : false;
			
			if( ChartColumnsContainerManager.instanceCounter == ChartColumnsContainerManager.COUNTER_LIMIT) {
				Ext.log('Maximum number of ChartColumnsContainer instances reached');
				
				/**
				 * @author: danristo (danilo.ristovski@mht.net)
				 */
				Ext.Msg.show({
  					title : LN("sbi.chartengine.structure.yAxisPanel.plusButton.maximumNumberOfAxisExceeded.information"),
  					message : LN("sbi.chartengine.structure.yAxisPanel.plusButton.maximumNumberOfAxisExceeded"),
  					icon : Ext.Msg.INFO,
  					closable : false,
  					buttons : Ext.Msg.OK,
  					minWidth: 200,
  					
  					buttonText : {
  						ok : LN('sbi.chartengine.generic.ok')
  					}
					});
				
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
			    		textfieldAxisTitle.suspendEvents();
			    		textfieldAxisTitle.setValue(textValue);
			    		textfieldAxisTitle.resumeEvents(false);
			    	}
			    },
			    
			    setAxisData: function(axisData) {
  					this.axisData = axisData;
  					
  					var isAxesTitleFieldAbsentFlag = true;
  					var newlySelectedType = Sbi.chart.designer.ChartTypeSelector_2.chartType 
  						|| Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
  					newlySelectedType = newlySelectedType.toLowerCase();
  					
  					isAxesTitleFieldAbsentFlag = (
  							newlySelectedType == 'pie'
  								|| newlySelectedType == 'treemap'
  									|| newlySelectedType == 'wordcloud'
  										|| newlySelectedType == 'parallel'
  											|| newlySelectedType == 'sunburst'
  												|| newlySelectedType == 'chord'
  					);
  					
  					if(!isAxesTitleFieldAbsentFlag) {
  						this.fireEvent('updateAxisTitleValue', axisData.titleText);
  					}
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
//							console.log("aaa");
//							console.log(node);
//							console.log(data);
//							console.log(dropRec);
//							
//							//data.copy = true;
//							
//							console.log(data);
							
							/**
							 * Prevent user from defining multiple serie items; if this part is 
							 * not provided, error appears
							 * @author: danristo (danilo.ristovski@mht.net)
							 */
							var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
							var enableAddAndSum = chartType != 'SUNBURST' && chartType != 'WORDCLOUD' && 
													chartType != 'TREEMAP' && chartType != 'HEATMAP';													
														
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
									
									if (!data.records[0].get('serieGroupingFunction'))
									{
										data.records[0].set('serieGroupingFunction', 'SUM');
									}
									
									/**
									 * danristo (4) - serieColumn
									 */
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
									
									/**
									 * Style chosen by the user (the one already set in the Designer).
									 * 
									 * @commentBy: danristo (danilo.ristovski@mht.net)
									 */
									var chosenStyle = Sbi.chart.designer.Designer.styleName;
									
									/**
									 * If for the current document is defined some chart style that does not
									 * exist anymore. If user defined some chart style for the document and
									 * if delete it afterwards and then re-renders the Designer, he will still
									 * see the old (not existing) chart style, but when dropping serie items 
									 * in the Y-axis panel it does not take serie parameterization of any
									 * particular style (since the one set in the combo is not existing anymore). 
									 */				
									if (Sbi.chart.designer.Designer.getConfigurationForStyle(chosenStyle) != null)
									{
										var genericJsonForStyle =  Sbi.chart.designer.Designer.getConfigurationForStyle(chosenStyle).generic;
										var specificJsonForStyle = Sbi.chart.designer.Designer.getConfigurationForStyle(chosenStyle)[chartType.toLowerCase()];
										
										var combination = Sbi.chart.designer.ChartUtils.mergeObjects(genericJsonForStyle,specificJsonForStyle);
										
										/**
										 * danristo (1) 
										 */
										var serieTagExists = combination.CHART.VALUES && combination.CHART.VALUES.SERIE;
										var serieTooltipTagExists = serieTagExists && combination.CHART.VALUES.SERIE.TOOLTIP;
										
										var serieTagParameters = null;
										var serieTooltipTagParameters = null;
										
										/**
										 * danristo (2)
										 */
										(serieTagExists) ? (serieTagParameters = combination.CHART.VALUES.SERIE) : null;									
										(serieTooltipTagExists) ? (serieTooltipTagParameters = serieTagParameters.TOOLTIP) : null;
																					
										
//										/**
//										 * danristo (3) - serieGroupingFunction
//										 */
//										var storeForGroupingFunctions = this.grid.columns[1].editor.store;
//										
//										if (!data.records[0].get('serieGroupingFunction'))
//										{
//											for (i=0; i<storeForGroupingFunctions.length; i++)
//											{											
//												if (serieTagExists && serieTagParameters.groupingFunction)
//												{												
//													if (storeForGroupingFunctions[i][0] == serieTagParameters.groupingFunction.toUpperCase())
//													{
//														data.records[0].set('serieGroupingFunction', serieTagParameters.groupingFunction.toUpperCase());
//														break;
//													}
//												}										
//											}
//											
//											if (!data.records[0].get('serieGroupingFunction'))
//											{
//												data.records[0].set('serieGroupingFunction', 'SUM');
//											}
//										}										
										
//										/**
//										 * danristo (5)
//										 * Current serie types: "" (empty), "line", "bar", "area" (available in
//										 * SerieStylePopup.js file.
//										 */
//										if (!data.records[0].get('serieType'))
//										{										
//											if (serieTagExists && serieTagParameters.type)
//											{												
//												if (serieTagParameters.type.toLowerCase()=="" || serieTagParameters.type.toLowerCase()=="line" || 
//														serieTagParameters.type.toLowerCase()=="bar" || serieTagParameters.type.toLowerCase()=="area")
//												{
//													data.records[0].set('serieType', serieTagParameters.type.toLowerCase());
//												}
//												else
//												{
//													data.records[0].set('serieType', "");
//												}
//											}
//										}
										
//										/**
//										 * danristo (6)
//										 * Current serie order types: "" (empty), "asc", "desc" (available in
//										 * SerieStylePopup.js file.
//										 */
//										if (!data.records[0].get('serieOrderType'))
//										{										
//											if (serieTagExists && serieTagParameters.orderType)
//											{												
//												if (serieTagParameters.orderType.toLowerCase()=="" || 
//														serieTagParameters.orderType.toLowerCase()=="asc" || 
//															serieTagParameters.orderType.toLowerCase()=="desc")
//												{
//													data.records[0].set('serieOrderType', serieTagParameters.orderType.toLowerCase());
//												}
//												else
//												{
//													data.records[0].set('serieOrderType', "");
//												}
//											}
//										}
										
										/**
										 * danristo (7)
										 */
										(!data.records[0].get('serieColor')) ? data.records[0].set('serieColor', serieTagParameters.color) : null;									
										(!data.records[0].get('serieShowValue')) ? data.records[0].set('serieShowValue', serieTagParameters.showValue) : null;
										(!data.records[0].get('seriePrecision')) ? data.records[0].set('seriePrecision', serieTagParameters.precision) : null;
										(!data.records[0].get('seriePrefixChar')) ? data.records[0].set('seriePrefixChar', serieTagParameters.prefixChar) : null;
										(!data.records[0].get('seriePostfixChar')) ? data.records[0].set('seriePostfixChar', serieTagParameters.postfixChar) : null;
										
										(!data.records[0].get('serieTooltipTemplateHtml')) ? 
												data.records[0].set('serieTooltipTemplateHtml', serieTooltipTagParameters.templateHtml) : null;
										(!data.records[0].get('serieTooltipBackgroundColor')) ? 
												data.records[0].set('serieTooltipBackgroundColor', serieTooltipTagParameters.backgroundColor) : null;									
												
										var splitSerieTooltipStyle = serieTooltipTagParameters.style.split(";");
										
										for (j=0; j<splitSerieTooltipStyle.length; j++)
										{
											(splitSerieTooltipStyle[j].indexOf("color:") >= 0 && !data.records[0].get('serieTooltipColor')) ? 
													(data.records[0].set('serieTooltipColor', splitSerieTooltipStyle[j].substring("color:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("fontFamily:") >= 0 && !data.records[0].get('serieTooltipFont')) ? 
													(data.records[0].set('serieTooltipFont', splitSerieTooltipStyle[j].substring("fontFamily:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("fontWeight:") >= 0 && !data.records[0].get('serieTooltipFontWeight')) ? 
													(data.records[0].set('serieTooltipFontWeight', splitSerieTooltipStyle[j].substring("fontWeight:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("fontSize:") >= 0 && !data.records[0].get('serieTooltipFontSize')) ? 
													(data.records[0].set('serieTooltipFontSize', splitSerieTooltipStyle[j].substring("fontSize:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("align:") >= 0 && !data.records[0].get('serieTooltipAlign')) ? 
													(data.records[0].set('serieTooltipAlign', splitSerieTooltipStyle[j].substring("align:".length,splitSerieTooltipStyle[j].length))) : null;//
										}	
										
										/**
										 * danristo (8)
										 * If the chart type is GAUGE: we have additional tags in the style XML
										 */
										var serieDialTagExists = serieTagExists && combination.CHART.VALUES.SERIE.DIAL;
										var serieDataLabelsTagExists = serieTagExists && combination.CHART.VALUES.SERIE.DATA_LABELS;
										
										var serieDialTagParameters = null;
										var serieDataLabelsTagParameters = null;
										
										(serieDialTagExists) ? (serieDialTagParameters = serieTagParameters.DIAL) : null;
										(serieDataLabelsTagExists) ? (serieDataLabelsTagParameters = serieTagParameters.DATA_LABELS) : null;
										
										// DIAL properties
										(!data.records[0].get('backgroundColorDial') && serieDialTagExists) ? data.records[0].set('backgroundColorDial',serieDialTagParameters.backgroundColorDial) : null;
										
										// DATA_LABELS properties
										(!data.records[0].get('yPositionDataLabels') && serieDataLabelsTagExists) ? data.records[0].set('yPositionDataLabels',serieDataLabelsTagParameters.yPositionDataLabels) : null;
										(!data.records[0].get('formatDataLabels') && serieDataLabelsTagExists) ? data.records[0].set('formatDataLabels',serieDataLabelsTagParameters.formatDataLabels) : null;
										(!data.records[0].get('colorDataLabels') && serieDataLabelsTagExists) ? data.records[0].set('colorDataLabels',serieDataLabelsTagParameters.colorDataLabels) : null;							
									}											
								}	
								
							} else  {								
								return false;
							}
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
//							change: function(textField, newTextValue) {
//								console.log('textField-> ', textField);
//								console.log('newTextValue-> ', newTextValue);
//							}
//				            change: {
//				            	fn: function(textField, newTextValue) {
//					            	console.log('textField-> ', textField);
//					            	console.log('newTextValue-> ', newTextValue);
//					            },
////					            scope: this
//					            scope: Ext.getCmp(idChartColumnsContainer)
//				            }
				       }
					}),
					
					// STYLE POPUP
					Ext.create('Ext.panel.Tool', {
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
						    	if (chartType.toUpperCase() == 'HEATMAP' || chartType.toUpperCase() == 'GAUGE'
						    		 || chartType.toUpperCase() == 'CHORD' || chartType.toUpperCase() == 'PARALLEL')
					    		{
						    		axisStylePopup.getComponent('majorGridFieldSetYAxis').hide();
						    		axisStylePopup.getComponent('minorGridFieldSetYAxis').hide();
					    		}							    	
						    	
					    		axisStylePopup.show();
					    	}					    	
						}
					}),
					
					// PLUS BUTTON
					Ext.create('Ext.panel.Tool', {
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
					})
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
	            								
	            								/**
	            								 * Send information about removed serie item towards combo that
	            								 * holds the value for serie as filter column (Limit panel, Step 2)
	            								 * so it can remove it if it is selected prior to this remove.
	            								 * 
	            								 * @author: danristo (danilo.ristovski@mht.net)
	            								 */
	            								Ext.getCmp("chartParallelLimit").seriesColumnsOnYAxisCombo
	            									.fireEvent("serieRemoved",store.getAt(rowIndex).data.serieColumn);
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