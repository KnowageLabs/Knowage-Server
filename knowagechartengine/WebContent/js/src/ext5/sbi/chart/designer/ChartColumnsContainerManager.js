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
    	COLUMNS_CONTAINER_ID_PREFIX : 'Axis_',
    	
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
		 * Sets the lowest number value of the "instanceIdFeed" in order to
		 * avoid container id conflicts.
		 */
		initInstanceIdFeed: function(axes) {
			//the check is on each axis
			for( var i = 0; i < axes.length; i++) {
				var axis = axes[i];
				
				//gets the axis id or alias if "id" property doesn't exist
				var tempIdOrAlias = axis.id || axis.alias;
				
				if(Sbi.chart.designer.ChartUtils.stringStartsWith(
						tempIdOrAlias, ChartColumnsContainerManager.COLUMNS_CONTAINER_ID_PREFIX)) {
					
					var extractedNumberValueAsString = tempIdOrAlias.substring(
							ChartColumnsContainerManager.COLUMNS_CONTAINER_ID_PREFIX.length );
					
					var extractedNumberValue = Number.parseInt(extractedNumberValueAsString);
					if( !isNaN(extractedNumberValue) && 
							(extractedNumberValue > ChartColumnsContainerManager.instanceIdFeed) ) {
						ChartColumnsContainerManager.instanceIdFeed = extractedNumberValue;
					}
				}
			}
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
			var idChartColumnsContainer = (id && id != '')? 
	    			id: ChartColumnsContainerManager.COLUMNS_CONTAINER_ID_PREFIX + ChartColumnsContainerManager.instanceIdFeed;
	    		    	
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
					
			var emptyTextForMeasures = LN('sbi.chartengine.designer.emptytext.dragdropmeasures');
			var currentChartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
//			if (currentChartType == "HEATMAP")
//			{
//				emptyTextForMeasures = "AAA";
//			}
					
			var chartColumnsContainer = Ext.create("Sbi.chart.designer.ChartColumnsContainer", {
				id: idChartColumnsContainer,
				idAxisesContainer: idAxisesContainer,
				axisData: axisData,
				emptyText: emptyTextForMeasures,	
				
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
			        	
			        	/** @comment benedetto.milazzo@eng.it */
			        	// The "chartColumnsContainer" panel has defined a ViewController controller that acts
			        	// also with "listener" functionalities. In this case the method "onTitleChange" is associated
			        	// to the "change" event captured by the "textfieldAxisTitle" inside this said panel.
			        	// The method setValue() of the "textfieldAxisTitle" fires also the "change" event, and,
			        	// as it is defined, tries to call the "onTitleChange" associated to it, but, for some weird
			        	// reasons in this case it is not captured by the ViewController component, and hence it
			        	// throws an error (ATHENA-231). The workaround consists in the event suspension and reactivation
			        	// in order to bypass this behavior.
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
							
							/**
							 * Prevent user from defining multiple serie items; if this part is 
							 * not provided, error appears
							 * @author: danristo (danilo.ristovski@mht.net)
							 */
							var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
							var enableAddAndSum = 
								(chartType != 'SUNBURST' 
									&& chartType != 'WORDCLOUD' 
										&& chartType != 'TREEMAP' 
											&& chartType != 'HEATMAP' 
												&& chartType != "CHORD");													
														
							/**
  	  						 * Prevent taking more than one serie from the container when we have
  	  						 * one of these chart types.
  	  						 * @author: danristo (danilo.ristovski@mht.net)
  	  						 */
  	  						if (data.records.length > 1 && (chartType == 'SUNBURST' || chartType == 'WORDCLOUD' || 
									chartType == 'TREEMAP' || chartType == 'HEATMAP' || chartType == "CHORD")) {
  	  							return false;
  							} 
  	  					
							if (enableAddAndSum || (!enableAddAndSum && this.store.data.length == 0)) {
								// *_* The original code
								if(data.view.id != this.id) {
									data.records[0] = data.records[0].copy('droppedSerie_' + ChartColumnsContainer.idseed++);
									var newRecordToDrop = data.records[0];
									
									if (!newRecordToDrop.get('serieGroupingFunction')) {
										newRecordToDrop.set('serieGroupingFunction', 'SUM');
									}
									
									/**
									 * danristo (4) - serieColumn
									 */
									if( !newRecordToDrop.get('axisName')) {
										var serieColumn = newRecordToDrop.get('serieColumn');
										
										newRecordToDrop.set('axisName', serieColumn);
										
										// (danilo.ristovski@mht.net)	
										var chartParallelLimit = Ext.getCmp("chartParallelLimit");
										if(chartParallelLimit && 
												chartParallelLimit != null &&
												chartParallelLimit.hidden == false ) {
											chartParallelLimit.addItem(newRecordToDrop);
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
									var configurationForStyle = Sbi.chart.designer.Designer.getConfigurationForStyle(chosenStyle);
									if (configurationForStyle != null) {
										var genericJsonForStyle =  configurationForStyle.generic;
										var specificJsonForStyle = configurationForStyle[chartType.toLowerCase()];
										
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
																					
										/**
										 * danristo (7)
										 */
										(!newRecordToDrop.get('serieColor')) ? newRecordToDrop.set('serieColor', serieTagParameters.color) : null;									
										(!newRecordToDrop.get('serieShowValue')) ? newRecordToDrop.set('serieShowValue', serieTagParameters.showValue) : null;
										(!newRecordToDrop.get('seriePrecision')) ? newRecordToDrop.set('seriePrecision', serieTagParameters.precision) : null;
										(!newRecordToDrop.get('seriePrefixChar')) ? newRecordToDrop.set('seriePrefixChar', serieTagParameters.prefixChar) : null;
										(!newRecordToDrop.get('seriePostfixChar')) ? newRecordToDrop.set('seriePostfixChar', serieTagParameters.postfixChar) : null;
										
										(!newRecordToDrop.get('serieTooltipTemplateHtml')) ? 
												newRecordToDrop.set('serieTooltipTemplateHtml', serieTooltipTagParameters.templateHtml) : null;
										(!newRecordToDrop.get('serieTooltipBackgroundColor')) ? 
												newRecordToDrop.set('serieTooltipBackgroundColor', serieTooltipTagParameters.backgroundColor) : null;									
												
										var splitSerieTooltipStyle = serieTooltipTagParameters.style.split(";");
										
										for (j=0; j<splitSerieTooltipStyle.length; j++)
										{
											(splitSerieTooltipStyle[j].indexOf("color:") >= 0 && !newRecordToDrop.get('serieTooltipColor')) ? 
													(newRecordToDrop.set('serieTooltipColor', splitSerieTooltipStyle[j].substring("color:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("fontFamily:") >= 0 && !newRecordToDrop.get('serieTooltipFont')) ? 
													(newRecordToDrop.set('serieTooltipFont', splitSerieTooltipStyle[j].substring("fontFamily:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("fontWeight:") >= 0 && !newRecordToDrop.get('serieTooltipFontWeight')) ? 
													(newRecordToDrop.set('serieTooltipFontWeight', splitSerieTooltipStyle[j].substring("fontWeight:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("fontSize:") >= 0 && !newRecordToDrop.get('serieTooltipFontSize')) ? 
													(newRecordToDrop.set('serieTooltipFontSize', splitSerieTooltipStyle[j].substring("fontSize:".length,splitSerieTooltipStyle[j].length))) : null;
													
											(splitSerieTooltipStyle[j].indexOf("align:") >= 0 && !newRecordToDrop.get('serieTooltipAlign')) ? 
													(newRecordToDrop.set('serieTooltipAlign', splitSerieTooltipStyle[j].substring("align:".length,splitSerieTooltipStyle[j].length))) : null;//
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
										(!newRecordToDrop.get('backgroundColorDial') && serieDialTagExists) ? newRecordToDrop.set('backgroundColorDial',serieDialTagParameters.backgroundColorDial) : null;
										
										// DATA_LABELS properties
										(!newRecordToDrop.get('yPositionDataLabels') && serieDataLabelsTagExists) ? newRecordToDrop.set('yPositionDataLabels',serieDataLabelsTagParameters.yPositionDataLabels) : null;
										(!newRecordToDrop.get('formatDataLabels') && serieDataLabelsTagExists) ? newRecordToDrop.set('formatDataLabels',serieDataLabelsTagParameters.formatDataLabels) : null;
										(!newRecordToDrop.get('colorDataLabels') && serieDataLabelsTagExists) ? newRecordToDrop.set('colorDataLabels',serieDataLabelsTagParameters.colorDataLabels) : null;							
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
						}
					}),
					
					/**
					 * Provide a button that will let user remove all serie items from the 
					 * appropriate Y-axis panel (according to its ID).
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					Ext.create
					(
						"Ext.panel.Tool",
						
						{
							type: 'deleteAllItemsFromAxisPanel',
							
							padding: "3 0 0 0",
							height: 22,
							
							handler: function(a,b,c)
							{
								var indexOfHeader = c.id.indexOf("_header");
								var axisId = c.id.substring(0,indexOfHeader);
								Sbi.chart.designer.Designer.cleanSerieAxis(axisId);

								/**
								 * The combo box for the "Serie as filter column" on the
								 * Configuration tab's Limit panel.
								 * 
								 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
								 */
								var seriesColumnsOnYAxisCombo = Ext.getCmp("seriesColumnsOnYAxisCombo");
																
								if(seriesColumnsOnYAxisCombo && seriesColumnsOnYAxisCombo != null && seriesColumnsOnYAxisCombo.hidden == false) 
								{	
									/**
									 * We need to clean the store that is assigned to the combo box
									 * that holds all series that can be chosen for the serie item 
									 * that will serve as a filter. 
									 * 
									 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
									seriesColumnsOnYAxisCombo.getStore().removeAll();
									
									/**
									 * Take current JSON structure, since we need to modify it
									 * because of the removing of all of series, including the one
									 * that belongs to the "serieFilterColumn" attribute in the 
									 * 'style' property of the 'LIMIT' property of the JSON. needs
									 * to be cleared (its value should be removed).
									 * 
									 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
									
									/**
									 * The name of the attribute in the 'style' property of the CHART.LIMIT
									 * property that keeps data about the serie that serves as the filter
									 * column.  
									 */
									var propOfSerAsFiltCol = "serieFilterColumn:";
									/**
									 * The current JSON structure of the document. We need to change it so
									 * we can have an actual situation - no serie as filter column item.
									 */
									var jsonOfCurrentDocStructure = Sbi.chart.designer.Designer.exportAsJson();
									/**
									 * Style property of the current JSON structure of the document (needed
									 * for extracting the 'serieFilterColumn' attribute.
									 */
									var styleOfTheJson = Sbi.chart.designer.Designer.exportAsJson().CHART.LIMIT.style;
									
									/**
									 * Take the part of the 'style' string that keeps the old (not actual) 
									 * value for this ('serieFilterColumn') parameter, so we can have a 
									 * substring that contains it.
									 */
									var styleBehindTheAttribute = styleOfTheJson.substring(styleOfTheJson.indexOf(propOfSerAsFiltCol) + propOfSerAsFiltCol.length,
											styleOfTheJson.length);
									
									/**
									 * Find the index of the semicolon sign that is the edge if the odl value
									 * for the parameter that was actual for the opened (old) document and
									 * set of series that we previously had (before this removement of all
									 * series).
									 */
									var indexOfEndOfSerieAsFiltCol = styleBehindTheAttribute.indexOf(";");
									
									/**
									 * Part of the 'style' string that contains everything that precedes the 
									 * value of this property. 
									 */
									var precedingStyle = styleOfTheJson.substring(0,styleOfTheJson.indexOf(propOfSerAsFiltCol) + propOfSerAsFiltCol.length);
									/**
									 * Part of the 'style' string that contains everything that follows the 
									 * value of this property. 
									 */
									var appendixStyle = styleBehindTheAttribute.substring(indexOfEndOfSerieAsFiltCol,styleBehindTheAttribute.length);
											
									/**
									 * Concatanate the preceding and appending part of the 'style', but now
									 * without the old value - just an empty value.
									 */
									var finalString = precedingStyle + "" + appendixStyle;
									
									/**
									 * Set this new 'style' string to appropriate JSON element (the one from
									 * which we started this action). 
									 */
									jsonOfCurrentDocStructure.CHART.LIMIT.style = finalString;
									
									/**
									 * Update the Designer so the change can be applied to the panel.
									 */
									Sbi.chart.designer.Designer.update(jsonOfCurrentDocStructure);	
								}
							}
						}
					),
					
					// STYLE POPUP
					Ext.create('Ext.panel.Tool', {
					    type:'gear',
					    tooltip: LN('sbi.chartengine.columnscontainer.tooltip.setaxisstyle'),
					    id: "stylePopupLeftAxis_"+idChartColumnsContainer, // (danilo.ristovski@mht.net)
					    // True for the SUNBURST, WORDCLOUD, TREEMAP and PARALLEL charts (danilo.ristovski@mht.net)
					    hidden: gearHidden,
					    padding: "3 0 0 0",// TODO: danristo (10.11)
					    height: 22,// TODO: danristo (10.11)
					    
					    //flex: 1, //TODO: danristo (was not commented)
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
					    		// TODO: see if some other chart type satisfies this statement
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
					    
					    padding: "3 0 0 0",	
					    height: 22,	
					    
					    tooltip: LN('sbi.chartengine.columnscontainer.tooltip.addaxis'),
					    
					    id: "plusLeftAxis_" + idChartColumnsContainer, // (added by: danilo.ristovski@mht.net)
					   
					    /** 
					     * True for the SUNBURST, WORDCLOUD, TREEMAP and PARALLEL charts
					     * 
					     * @author: danristo (danilo.ristovski@mht.net)
					     */
					    hidden: plusHidden || (panelWhereAddSeries == null),
					    
					    /// flex: 1, //TODO: danristo (was not commented)
					    handler: function(event, toolEl, panelHeader) {
					    						    	
					    	var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
					    	
//					    	/**
//					    	 * JSON template of current document (current structure of
//					    	 * the document).
//					    	 * 
//					    	 * @author: danristo (danilo.ristovski@mht.net)
//					    	 */
//					    	var tempFinal = Sbi.chart.designer.Designer.exportAsJson();
					    	
					    	if(chartType.toUpperCase() != 'PIE') {
					    		if (!panelWhereAddSeries.isVisible()) {
					    			panelWhereAddSeries.setVisible(true);
					    		}
					    	
					    		ChartAxisesContainer.addToAxisesContainer(panelWhereAddSeries);
					    	}
					    						    	
					    	/**
					    	 * We will do a merging of current JSON template (current document
					    	 * structure) with the XML template of the chosen style for our
					    	 * chart type, so to take just the newly created AXIS tag for the
					    	 * newly added Y-axis panel. Afterwards we will take this one in
					    	 * order to append it to the already existing JSON template, so to
					    	 * skip applying current chart style to all axes (we need to apply
					    	 * it only to the one we are adding).
					    	 * 
					    	 * @author: danristo (danilo.ristovski@mht.net)
					    	 */
					    	configApplyAxesStyles = 
					    	{
								applyAxes: true,
								applySeries: true,
							};	
					    	
					    	var configurationForStyleGeneric = 	
								
								Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle
								(
									Designer.getConfigurationForStyle(Designer.styleName).generic,
									false
								);	
					    	
					    	var configurationForStyleSpecific = 	
								
								Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle
								(
									Designer.getConfigurationForStyle(Designer.styleName)[chartType.toLowerCase()],
									false
								);						    	
					    	
					    	/**
					    	 * When we are adding new Y-axis to the chart document we should apply 
					    	 * current style only to that one (new axis). Other axes should be 
					    	 * excluded from this process, in order to avoid the reset of already 
					    	 * saved (defined) parameters for already existing axes.  
					    	 * 
					    	 * TODO: Check with Benedetto if this is OK !!! 
					    	 */
					    	
					    	/**
					    	 * Current JSON structure of the chart document.
					    	 */
					    	var currentJson = Sbi.chart.designer.Designer.exportAsJson();
					    	/**
					    	 * All axes of the current chart document (including the newly added one).
					    	 */
					    	var currentJsonAxes = currentJson.CHART.AXES_LIST.AXIS;
					    	/**
					    	 * Take just the one that we added (since we have some number of Y-axes
					    	 * that is followed by single X-axis panel, we can take the next-to-last
					    	 * (second from behind) axis) in order to get its alias and ID.
					    	 */
					    	var currentJsonAddedAxis = currentJsonAxes[currentJsonAxes.length-2];
					    						 
					    	/**
					    	 * Forward alias and ID of the newly added Y-axis panel to the mergeObjects()
					    	 * method in order to skip applying styles to all Y-axes. The current style 
					    	 * should be applied just to the one that is added. Otherwise, we will reset
					    	 * parameters for axis style configuration and serie style configuration of
					    	 * already existing axes that will lead to canceling of any change made before
					    	 * adding new Y-axis. Via alias and ID of the newly added Y-axis we will 
					    	 * distinguish old Y-axis from the new one when applying style.
					    	 */
					    	var localJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects
					    	(
				    			currentJson,
				    			configurationForStyleGeneric, 
				    			configApplyAxesStyles,
				    			{alias: currentJsonAddedAxis.alias, id: currentJsonAddedAxis.id}
			    			);
					    	
							localJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects
							(
								localJsonTemplate, 
								configurationForStyleSpecific, 
								configApplyAxesStyles,
				    			{alias: currentJsonAddedAxis.alias, id: currentJsonAddedAxis.id}
							);
					
//							localJsonTemplateAxisTag = localJsonTemplate.CHART.AXES_LIST.AXIS;

//							/**
//							 * Take just the newly added Y-axis panel configuration and append it
//							 * to the current JSON template. This way we will apply current style 
//							 * only to that newly created Y-axis panel, instead of resetting the
//							 * axis configuration of already existing Y-axis panels.
//							 * 
//							 * @author: danristo (danilo.ristovski@mht.net)
//							 */
//							tempFinal.CHART.AXES_LIST.AXIS.push(localJsonTemplateAxisTag[localJsonTemplateAxisTag.length-1]);
							
							Sbi.chart.designer.Designer.update(localJsonTemplate);
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
				columns: [
					Ext.create('Ext.grid.column.Column', {
						dataIndex: 'serieColumn',
						flex: 12,
						layout: 'fit',
						sortable: false,
					}), 
					Ext.create('Ext.grid.column.Column', {
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
					}),
					Ext.create('Ext.grid.column.Action', {
						menuDisabled: true,
						sortable: false,
						flex: 1,
						align : 'center',
						//xtype: 'actioncolumn',
						id: "actionColumnLeftAxis_"+idChartColumnsContainer,
						items: [{
							icon: '/' + Sbi.chart.designer.Designer.mainContextName + '/themes/sbi_default/img/createTemplate.jpg',
							
							tooltip: LN('sbi.chartengine.columnscontainer.tooltip.style'),
							handler: function(grid, rowIndex, colIndex) {
								var store = grid.getStore();
								
								ChartColumnsContainerManager.promptChangeSerieStyle(store, rowIndex);
							}
						},{
							icon: '/' + Sbi.chart.designer.Designer.mainContextName + '/themes/sbi_default/img/delete.gif',
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
											var chartParallelLimit = Ext.getCmp("chartParallelLimit");
											
											if(chartParallelLimit && 
													chartParallelLimit != null &&
													chartParallelLimit.hidden == false ) {
												chartParallelLimit.removeItem(store.getAt(rowIndex));
												
												/**
												 * Send information about removed serie item towards combo that
												 * holds the value for serie as filter column (Limit panel, Step 2)
												 * so it can remove it if it is selected prior to this remove.
												 * 
												 * @author: danristo (danilo.ristovski@mht.net)
												 */
												chartParallelLimit.seriesColumnsOnYAxisCombo
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
					})
				],
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