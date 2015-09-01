Ext.define('Sbi.chart.designer.Designer', {
    extend: 'Ext.Base',
    alternateClassName: ['Designer'],
	requires: [
        'Sbi.chart.rest.WebServiceManagerFactory',
        'Sbi.chart.designer.ChartUtils',
        'Sbi.chart.designer.ChartTypeSelector',
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesPicker',
        'Sbi.chart.designer.ChartTypeColumnSelector',
        'Sbi.chart.designer.ChartCategoriesContainer',
        'Sbi.chart.designer.AxisStylePopup',
        'Sbi.chart.designer.ChartStructure',
        'Sbi.chart.designer.ChartConfigurationModel',
        'Sbi.chart.designer.ChartConfiguration',
        'Sbi.chart.designer.AdvancedEditor',
    ],

    statics: {
    	tabChangeChecksFlag: true,
    	
		jsonTemplate: null,
		chartLibNamesConfig: null,
		
		jsonTemplateHistory: [],
		jsonTemplateHistoryIterator: null,
	
    	chartServiceManager: null,
    	coreServiceManager: null,
    	chartExportWebServiceManager: null,
    	docLabel: null,
    	
		// Left designer panel 
		chartTypeColumnSelector: null,
		
		selectedChartType: '',
		
		// chart types
    	chartTypes : [{
			name: LN('sbi.chartengine.designer.charttype.bar'), 
			type: 'BAR',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/barchart/img/barchart_64x64_ico.png',
		}, {	
			name: LN('sbi.chartengine.designer.charttype.line'), 
			type: 'LINE',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/linechart/img/linechart_64x64_ico.png',
		}, {
			name: LN('sbi.chartengine.designer.charttype.pie'), 
			type: 'PIE',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		}, 
		
		{
			name: LN('sbi.chartengine.designer.charttype.sunburst'), 
			type: 'SUNBURST',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		}, 
		
		{
			name: LN('sbi.chartengine.designer.charttype.wordcloud'), 
			type: 'WORDCLOUD',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		},
		
		{
			name: LN('sbi.chartengine.designer.charttype.treemap'), 
			type: 'TREEMAP',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		},
		
		{
			name: LN('sbi.chartengine.designer.charttype.parallel'), 
			type: 'PARALLEL',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		},
		
		{
			name: LN('sbi.chartengine.designer.charttype.radar'), 
			type: 'RADAR',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		},
		
		{
			name: LN('sbi.chartengine.designer.charttype.scatter'), 
			type: 'SCATTER',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		},
		
		{
			name: LN('sbi.chartengine.designer.charttype.heatmap'), 
			type: 'HEATMAP',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		},
		
		{
			name: LN('sbi.chartengine.designer.charttype.gauge'), 
			type: 'GAUGE',
			iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
		}
		
		],
		
		chartTypeStore: null,
		chartTypeSelector: null,
		
		// columns and categories pickers
		columnsPickerStore: null,
		categoriesPickerStore: null,
		columnsPicker: null,
		categoriesPicker: null,
		
		/* * * * * * START STEP 1 * * * * * */
		// main central preview panel
		previewPanel: null,
		
		// right designer vertical axes container
		rightYAxisesPanel: null,
		// left designer vertical axes container
		leftYAxisesPanel: null,
		
		// bottom designer horizontal axis
		categoriesStore: null,
		bottomXAxisesPanel: null,
		
		chartStructure: null,
		/* * * * * * END STEP 1 * * * * * */
		
		/* * * * * * START STEP 2 * * * * * */
		// data bundle for step 2 storing
		cModel: null,
		cViewModel: null,
		chartConfiguration: null,
		/* * * * * * END STEP 2 * * * * * */
		
		/* * * * * * START STEP 3 * * * * * */
		advancedEditor: null,
		/* * * * * * END STEP 3 * * * * * */
		
		// step panel
		stepsTabPanel: null,
		
		// designer main panel
		designerMainPanel: null,
		
		hostName : '', 
		serverPort: '',
				
		initialize: function(sbiExecutionId, userId, hostName, serverPort, docLabel, jsonTemplate, datasetLabel, chartLibNamesConfig, isCockpit) {

			Sbi.chart.designer.ChartUtils.setCockpitEngine(isCockpit);			
			
//			console.log(jsonTemplate);
			
			var baseTemplate = {
					CHART: {
						type: 'BAR',
						AXES_LIST: {
							AXIS: [
							       {alias:'Y', type: 'Serie'},
							       {alias:'X', type: 'Category'}
							       ]
						},
						VALUES:
						{
							SERIE: []
						},
						COLORPALETTE:
						{
							COLOR: [
							        {id:1 , order: 1, name: '7cb5ec', value: '7cb5ec' }, 
							        {id:2 , order: 2, name: '434348', value: '434348' }, 
							        {id:3 , order: 3, name: '90ed7d', value: '90ed7d' }, 
							        {id:4 , order: 4, name: 'f7a35c', value: 'f7a35c' }, 
							        {id:5 , order: 5, name: '8085e9', value: '8085e9' }, 
							        {id:6 , order: 6, name: 'f15c80', value: 'f15c80' }, 
							        {id:7 , order: 7, name: 'e4d354', value: 'e4d354' }, 
							        {id:8 , order: 8, name: '2b908f', value: '2b908f' }, 
							        {id:9 , order: 9, name: 'f45b5b', value: 'f45b5b' }, 
							        {id:10, order: 10,name: '91e8e1', value: '91e8e1' }
							        ]
						}
					}
			};	
			
			var newChart = false;
			
			/**
			 * Global scope (scope of the Designer). 
			 * (danilo.ristovski@mht.net)
			 */
			var globalThis = this;
			
			/**
			 * This part is executed whenever we create the fresh document (chart)
			 * and start the initialization of the chart through the Designer interface
			 * (when we open in Designer completely new chart, not the existing one).
			 * (danilo.ristovski@mht.net)
			 */
			if (!jsonTemplate.CHART) {
//				console.log("NO CHART");
				newChart = true;
				jsonTemplate = baseTemplate;
			}			
			
			/**
			 * Predefined style type is RED
			 * (danilo.ristovski@mht.net)
			 */		
//			console.log(jsonTemplate.CHART.styleName);
			this.styleName = (jsonTemplate.CHART.styleName) ? (jsonTemplate.CHART.styleName) : "red";
//			this.styleCustom = (jsonTemplate.CHART.styleCustom) ? (jsonTemplate.CHART.styleCustom) : false;
			
			/**
			 * Get the missing JSON configuration elements (properties) in order to define
			 * their default values for any type of chart (including the BAR chart)
			 * (danilo.ristovski@mht.net)
			 */
			var getConfigurationForStyle = function(style)
			{
				this.styleName = style;
				
				// CHART
				var chartHeight = null;
				var chartWidth = null;
				var chartIsCockpitEngine = null;
				var chartOrientation = null;
				var chartStyle = null;
				
				// TITLE
				var titleStyle = null;
				var titleText = null;
				
				// SUBTITLE
				var subtitleStyle = null;
				var subtitleText = null; 
				
				// EMPTY MESSAGE
				var emptyMessageStyle = null;
				var emptyMessageText = null;
				
				// LEGEND
				var legendFloating = null;
				var legendLayout = null;
				var legendPosition = null;
				var legendShow = null;
				var legendStyle = null;
				var legendX = null;
				var legendY = null;				
				
				switch(style)
				{				
					case "red":
//						console.log("RED");
						chartStyleName = "red";
//						styleCustom = false;
						chartHeight = 400;
						chartWidth = 1000;
						chartIsCockpitEngine = "false";
						chartOrientation = "horizontal";
						chartStyle = "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#FF0000;";						
						titleStyle = "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;";
						titleText = "Insert your title";						
						subtitleStyle = "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;";
						subtitleText = "Insert your subtitle";						
						emptyMessageStyle = "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;";
						emptyMessageText = "Insert your empty message (when no data for chart exists)";
						legendFloating = false;
						legendLayout = "";
						legendPosition = "";
						legendShow = false;
						legendStyle = "align:;fontFamily:;fontSize:;fontWeight:;borderWidth:;color:;backgroundColor:;symbolWidth:;";
						legendX = 0;
						legendY = 0;	
						break;
						
					case "blue":
//						console.log("BLUE");
						chartStyleName = "blue";
//						styleCustom = false;
						chartHeight = 600;
						chartWidth = 1200;
						chartIsCockpitEngine = "false";
						chartOrientation = "horizontal";
						chartStyle = "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#0000FF;";						
						titleStyle = "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;";
						titleText = "Insert your title";						
						subtitleStyle = "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;";
						subtitleText = "Insert your subtitle";						
						emptyMessageStyle = "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;";
						emptyMessageText = "Insert your empty message (when no data for chart exists)";
						legendFloating = false;
						legendLayout = "horizontal";
						legendPosition = "middle";
						legendShow = false;
						legendStyle = "align:center;fontFamily:Verdana;fontSize:10px;fontWeight:normal;borderWidth:0;color:#000000;backgroundColor:#FFFFFF;symbolWidth:0;";
						legendX = 0;
						legendY = 0;
						break;
						
					case "green":
//						console.log("GREEN");
						chartStyleName = "green";
//						styleCustom = false;
						chartHeight = 800;
						chartWidth = 1400;
						chartIsCockpitEngine = "false";
						chartOrientation = "horizontal";
						chartStyle = "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#00FF00;";						
						titleStyle = "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;";
						titleText = "Insert your title";						
						subtitleStyle = "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;";
						subtitleText = "Insert your subtitle";						
						emptyMessageStyle = "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;";
						emptyMessageText = "Insert your empty message (when no data for chart exists)";
						legendFloating = false;
						legendLayout = "horizontal";
						legendPosition = "middle";
						legendShow = false;
						legendStyle = "align:center;fontFamily:Verdana;fontSize:10px;fontWeight:normal;borderWidth:0;color:#000000;backgroundColor:#FFFFFF;symbolWidth:0;";
						legendX = 0;
						legendY = 0;
						break;					
				}
				
				/**
				 * The missing generic style configuration elements in a form of
				 * the JSON
				 *(danilo.ristovski@mht.net)
				 */
				var configurationToReturn = 
				
				{
					generic: 
					{
						CHART:
						{
							height: chartHeight,
							width: chartWidth,	
							isCockpitEngine: chartIsCockpitEngine,
							orientation: chartOrientation,
							style: chartStyle,
							styleName: chartStyleName,
//							styleCustom: styleCustom, 
							
							TITLE:
							{
								style: titleStyle,
								text: titleText
							},
							
							SUBTITLE:
							{
								style: subtitleStyle,
								text: subtitleText
							},
							
							EMPTYMESSAGE:
							{
								style: emptyMessageStyle,
								text: emptyMessageText
							},
							
							LEGEND:
							{
								floating: legendFloating,
								layout: legendLayout,
								position: legendPosition,
								show: legendShow,
								style: legendStyle,
								x: legendX,
								y: legendY
							}
						}						
					}
				};
								
				return configurationToReturn;
			
			};			
			
			/**
			 * Merging JSON templates of specified chart types with the base JSON template
			 * (of type BAR) in order to make the union of all of the JSON elements within
			 * these two types - the base one and the current one. 			 
			 * (danilo.ristovski@mht.net)
			 */
			if (jsonTemplate.CHART.type.toUpperCase() == 'PIE' 
				|| jsonTemplate.CHART.type.toUpperCase() == 'SUNBURST'
					|| jsonTemplate.CHART.type.toUpperCase() == 'WORDCLOUD'
						|| jsonTemplate.CHART.type.toUpperCase() == 'TREEMAP'
							|| jsonTemplate.CHART.type.toUpperCase() == 'PARALLEL'
								|| jsonTemplate.CHART.type.toUpperCase() == 'RADAR'
									|| jsonTemplate.CHART.type.toUpperCase() == 'SCATTER'
										|| jsonTemplate.CHART.type.toUpperCase() == 'HEATMAP'
											|| jsonTemplate.CHART.type.toUpperCase() == 'GAUGE') {
//				console.log(baseTemplate);
//				console.log(jsonTemplate);
				
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(baseTemplate, jsonTemplate);				
			}				
			
			/**
			 * 
			 */			
//			if (jsonTemplate.CHART.styleCustom)
//				console.log("YYYY");
//			else
			if (newChart == true)
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate, getConfigurationForStyle(this.styleName).generic);
			
			Sbi.chart.designer.ChartColumnsContainerManager.setPlotbandsStore(jsonTemplate);
			
			this.docLabel = docLabel;
			this.jsonTemplate = jsonTemplate;
			
			this.jsonTemplateHistory.push(jsonTemplate);
			this.jsonTemplateHistoryIterator = 0;
			
			/**
			 * List of names of the libraries that we use for rendering the charts. 
			 * (comment by: danilo.ristovski@mht.net)
			 */
			this.chartLibNamesConfig = chartLibNamesConfig;	
			
			this.chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
			this.coreServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getCoreWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
			this.chartExportWebServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartExportWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
						
			this.hostName = hostName; 
			this.serverPort = serverPort;
			
			/**
			 * Chart types that we specified at the beginning of the Designer and
			 * that are available through the Chart Type Selector (needed for creating
			 * of the top left panel on the Designer page. 
			 * (comment by: danilo.ristovski@mht.net)
			 */
			var chartTypes = this.chartTypes;
			
			/**
			 * Populating store with those chart types ('fields' define the structure 
			 * of every single chartType).
			 * (comment by: danilo.ristovski@mht.net)
			 */
			var chartTypeStore = Ext.create('Ext.data.Store', {
				fields: [
					{name: 'name', type: 'string'},
					{name: 'type', type: 'string'},
					{name: 'iconUrl', type: 'string'},
				],
	 			data: chartTypes
		    });
			
			this.chartTypeStore = chartTypeStore;			
			
			/**
			 * One of the main roles of this JS class (file) is listening to the 
			 * event of changing the chart types inside of it (the selector), i.e.
			 * clicking on the row of the Selector.
			 * (comment by: danilo.ristovski@mht.net)
			 */
			this.chartTypeSelector = Ext.create('Sbi.chart.designer.ChartTypeSelector', {
 				region: 'north',
 				minHeight: 50,
 				store: chartTypeStore
 			});
			
			var onSelectJsonTemplate = "";
			
			// *_* Listens when chart type is changed
			this.chartTypeSelector.on
			(
				"newrowclick",
				
				function()
				{				
					var mainConfigurationPanel = globalThis.stepsTabPanel.getComponent(1).getComponent(0);
					var secondConfigurationPanel = globalThis.stepsTabPanel.getComponent(1).getComponent(1);
					
					/* Main configuration panel elements for hiding/showing when the SUNBURST is selected */
					var chartLegendCheckBox = mainConfigurationPanel.getComponent("showLegend");
					var chartOrientation = mainConfigurationPanel.getComponent("fieldContainer1").getComponent("chartOrientationCombo");
					var chartWidth = mainConfigurationPanel.getComponent("fieldContainer1").getComponent("chartWidthNumberfield");
					var opacityOnMouseOver = mainConfigurationPanel.getComponent("opacityMouseOver");
					
					/* Second configuration panel elements for hiding/showing when the SUNBURST is selected */
					var colorPallete = secondConfigurationPanel.getComponent("chartColorPallete");
					var chartLegend = secondConfigurationPanel.getComponent("chartLegend");	
					var toolbarAndTip = secondConfigurationPanel.getComponent("chartToolbarAndTip");
					
					/* Second configuration panel elements for hiding/showing when the WORDCLOUD is selected */
					var wordCloudPanel = secondConfigurationPanel.getComponent("wordcloudConfiguration");
					
					/* Second configuration panel elements for hiding/showing when the PARALLEL is selected */
					var parallelLimitPanel = secondConfigurationPanel.getComponent("chartParallelLimit");
					var parallelAxesLinesPanel = secondConfigurationPanel.getComponent("chartParallelAxesLines");					
					var parallelTooltipPanel = secondConfigurationPanel.getComponent("chartParallelTooltip");
					var parallelLegendPanel = secondConfigurationPanel.getComponent("chartParallelLegend");
					
					/* Second configuration panel elements for hiding/showing when the SCATTER is selected */
					var scatterConfiguration = secondConfigurationPanel.getComponent("chartScatterConfiguration");
					
					/**
					 * Second configuration panel elements for hiding/showing when the HEATMAP is selected
					 * (danilo.ristovski@mht.net)
					 */
					var showLegendAndTooltip = secondConfigurationPanel.getComponent("chartHeatmapLegendAndTooltip");
					
					var gaugePanePanel = secondConfigurationPanel.getComponent("gaugePaneConfiguration");
					
					var isChartSunburst = this.chartType.toUpperCase() == 'SUNBURST';
					var isChartWordCloud = this.chartType.toUpperCase() == 'WORDCLOUD';		
					var isChartTreemap = this.chartType.toUpperCase() == 'TREEMAP';
					var isChartParallel = this.chartType.toUpperCase() == 'PARALLEL';					
					var isChartScatter = this.chartType.toUpperCase() == 'SCATTER';						
					var isChartHeatmap = this.chartType.toUpperCase() == 'HEATMAP';	
					var isChartGauge = this.chartType.toUpperCase() == 'GAUGE';	
					
					if (isChartSunburst || isChartWordCloud  || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge) {						
						chartLegendCheckBox.hide();
					} else {
						chartLegendCheckBox.show();
					}
					
					if (isChartSunburst || isChartWordCloud || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge) {
						chartLegend.hide();
					} else {
						chartLegend.show();
					}
					
					if (isChartSunburst || isChartWordCloud || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge) {
						chartOrientation.hide();
					} else {
						chartOrientation.show();
					}
					
					if (isChartSunburst) {
						chartWidth.hide();
					} else {
						chartWidth.show();
					}
					
					if (isChartSunburst) {
						opacityOnMouseOver.show();
					} else {
						opacityOnMouseOver.hide();
					}
					
					if (isChartSunburst || isChartWordCloud || isChartScatter || isChartGauge) {
						colorPallete.hide();
					} else {
						colorPallete.show();
					}
					
					if (isChartSunburst) {
						toolbarAndTip.show();
					} else  {
						toolbarAndTip.hide();
					}
					
					if (isChartWordCloud) {
						wordCloudPanel.show();
					} else {
						wordCloudPanel.hide();
					}
					
					if (isChartParallel) {
						parallelLimitPanel.show();
						parallelAxesLinesPanel.show();
						parallelTooltipPanel.show();
						parallelLegendPanel.show();
					} else {
						parallelLimitPanel.hide();
						parallelAxesLinesPanel.hide();
						parallelTooltipPanel.hide();
						parallelLegendPanel.hide();
					}
					
					if (isChartScatter) {
						scatterConfiguration.show();
					} else {
						scatterConfiguration.hide();
					}
					
					if (isChartHeatmap) {
						showLegendAndTooltip.show();
					} else {
						showLegendAndTooltip.hide();
					}
					
					if (isChartGauge)
					{
						globalThis.bottomXAxisesPanel.hide();
						gaugePanePanel.show();
					}
					else
					{
						globalThis.bottomXAxisesPanel.show();
						gaugePanePanel.hide();
					}
				}
			);
			
			var selectedChartType = jsonTemplate.CHART.type.toUpperCase();
			
			this.chartTypeSelector.setChartType(selectedChartType);			
			
			// Store that contains the data about SERIE column(s) for the chart (danilo.ristovski@mht.net)
			this.columnsPickerStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
 				data: [],
 				id: "axisesContainerStore",
 				sorters: [{
 					property: 'serieColumn',
 					direction: 'ASC'
 				}],
 				
 				listeners: 
 				{
 					// *_* When we get the necessary data populate this store
 					dataReady: function(jsonData) {
 						
 						var jsonDataObj = Ext.JSON.decode(jsonData);
 						var theData = [];
 		  				
 						Ext.each(jsonDataObj.results, function(field, index){		  					
 		  					
 		  					if(field != 'recNo' && field.nature == 'measure'){		  						
 		  						
 		  						theData.push({
 		  							serieColumn : field.alias,
 		  							serieDataType: field.colType // (danilo.ristovski@mht.net)
 		  						});
 		  					}
 		  				});
 						
 		  				this.setData(theData);	// *_* Set the 'data' attribute of the store
 		  			}
 				}
 			});
			
			// *_*
			this.seriesBeforeDropStore = Ext.create("Ext.data.Store",{id:"storeForSeriesBeforeDrop", fields: [{name: 'seriesColumn'}]});
			
			var columnsPickerStore = this.columnsPickerStore;
			
			// *_* Store that contains the data about CATEGORY column(s) for the chart
			this.categoriesPickerStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
 				data: [],
 				
 				sorters: [{
 					property: 'categoryColumn',
 					direction: 'ASC'
 				}],
 				
 				listeners: 
 				{
 					dataReady: function(jsonData) 
 					{
 		  				var jsonDataObj = Ext.JSON.decode(jsonData);
 						var theData = [];
 						
 		  				Ext.each(jsonDataObj.results, function(field, index)
  						{ 		   		  					
 		  					if(field != 'recNo' && field.nature == 'attribute')
 		  					{
 		  						theData.push({
 		  							categoryColumn : field.alias,
 		  							categoryDataType: field.colType // (danilo.ristovski@mht.net)
 		  						});
 		  					}
 		  				});
 		  				
 		  				this.setData(theData);
 		  			}
 				}
 			});
			
			var categoriesPickerStore = this.categoriesPickerStore;
			
			// loading measures and attributes
			// *_* Fires aforementioned 'dataReady' event that is catch in axis creation (attributes and measure)
  			this.chartServiceManager.run('loadDatasetFields', {}, [datasetLabel], function (response) {
  				columnsPickerStore.fireEvent('dataReady', response.responseText);
  				categoriesPickerStore.fireEvent('dataReady', response.responseText);
			});

			this.columnsPicker = Ext.create('Sbi.chart.designer.AxisesPicker', {
  				region: 'center',
//  				flex:  1,
//  				margin: '0 15 5 0',
  				store: columnsPickerStore,
  				viewConfig: {
  					copy: true,
  					plugins: {
  						ptype: 'gridviewdragdrop',
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroupMeasure,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroupMeasure,
  						dragText: LN('sbi.chartengine.designer.tooltip.drop.series'),
  						enableDrop: false
  					},
  					listeners: {
  						drop: function(node, data, dropRec, dropPosition) {  							
  							var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('serieColumn') : ' on empty view';
  						}
  					}
  				},
  				columns: [
  					{
  						text: LN('sbi.chartengine.designer.measureslist'), 
  						dataIndex: 'serieColumn',
  						sortable: false,
  						flex: 1
  					}
  				]
  			});
			
			// *_* Type: GRID PANEL
			this.categoriesPicker = Ext.create('Sbi.chart.designer.AxisesPicker', {
  				region: 'south',
//  				flex: 1,
//  				margin: '0 0 5 0',
  				store: categoriesPickerStore, 
  				viewConfig: {
  					copy: true,
  					plugins: {
  						ptype: 'gridviewdragdrop',
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroupAttribute,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroupAttribute,
  						dragText: LN('sbi.chartengine.designer.tooltip.drop.categories'),
  						enableDrop: false
  					},
  					listeners: {
  						drop: function(node, data, dropRec, dropPosition) {
  							var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('categoryColumn') : ' on empty view';
  						}
  					}
  				},
  				columns: [
  					{
  						text: LN('sbi.chartengine.designer.attributeslist'), 
  						dataIndex: 'categoryColumn',
  						sortable: false,
  						flex: 1
  					}
  				]
  			});
			
			/**
			 * Static store for styles for the generic parameters of the document (chart)
			 * with combo items that have predefined names (Red, Green, Blue, ...)
			 * (danilo.ristovski@mht.net)
			 */
			var styleStore = Ext.create
			(
				"Ext.data.Store", 
				
				{
					fields: ["style", "styleAbbr"],
					
					data: 
					[
					 	{"style":"Red", "styleAbbr":"red"},
					 	{"style":"Blue", "styleAbbr":"blue"},
					 	{"style":"Green", "styleAbbr":"green"}
					 ]
				}
			);
			
			this.styleLabel = Ext.create
			(
				{
					xtype: 'label',
			        forId: 'stylePickerComboId',
			        text: 'Style for parameters',	// TODO: LN()
			        //margin: '5 3 3 0'
				}
			);
			
			/**
			 * Combo box for defining the style for the generic customizable parameters (properties)
			 * of the chart 
			 * (danilo.ristovski@mht.net)
			 */
			this.stylePickerCombo = Ext.create
			(
				"Ext.form.ComboBox", 
				
				{
//					fieldLabel: 'Choose style',
				    store: styleStore,
				    id: "stylePickerComboId",
				    queryMode: 'local',
				    displayField: 'style',
				    valueField: 'styleAbbr',
				    value: globalThis.styleName,
				    editable: false,
				    padding: "5 0 10 0",
				    width: 170,
				    
				    listConfig:
				    	{listeners: 
				    	{
					    	itemclick: function(combo,k)
					    	{
								/**
								 * Depending on the style that we choose for the document's generic
								 * customizable parameters (Red, Green, Blue, ... style), take the
								 * predefined JSON structure that is defined for that newly chosen 
								 * style. This part is needed for later merging of the templates 
								 */
//					    		jsonTemplate.CHART.styleCustom = false;
//					    		globalThis.styleCustom = false;
//					    		console.log(k);
								var genericConfigurationForStyle = getConfigurationForStyle(k.data.styleAbbr).generic;
//								console.log(genericConfigurationForStyle);
								/**
								 * Reset (refresh, modify) the 'styleName' field of the Designer, also
								 */
								globalThis.styleName = k.data.styleAbbr;
								
								/**
								 * Reset the JSON template for the document (chart) after changing the 
								 * previously selected style (changing the selected item of the combo)
								 */
//								console.log(jsonTemplate);
								jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,genericConfigurationForStyle);
//					    		console.log(jsonTemplate);
								/**
								 * Update (refresh) the main configuration panel (the one on the top of 
								 * the Step 2 tab) after selecting the particular style
								 */
					    		Sbi.chart.designer.Designer.update(jsonTemplate);
					    	}
				    	}}
				}
			);
			
			this.chartTypeColumnSelector = Ext.create('Sbi.chart.designer.ChartTypeColumnSelector', {
  				chartTypeSelector: this.chartTypeSelector,
  				columnsPicker: this.columnsPicker,
  				stylePickerCombo: this.stylePickerCombo,
  				styleLabel: this.styleLabel,
  				categoriesPicker: this.categoriesPicker,
  				region: 'west'
  			});			

			var chartExportWebServiceManager = this.chartExportWebServiceManager;
			var chartServiceManager = this.chartServiceManager;
			
			// Creating step 1 panel
  			this.previewPanel = Ext.create('Ext.panel.Panel', {
  				id: 'previewPanel',
  				minHeight: 300,
  				title: LN('sbi.chartengine.preview'),
  				titleAlign: 'center',
  				tools:[]
  				
  			});
  			
  			var previewPanel = this.previewPanel;
			
			var hostName = this.hostName; 
			var serverPort = this.serverPort;
  			
			function setPreviewImage(src) {
				previewPanel.removeAll();
				var previewImg = Ext.create('Ext.Img', {
				    src: src,
					listeners: {
			            render: function() {
			                this.mon(this.getEl(), 'load', function(e) {
			                	previewPanel.setHeight(this.getHeight()+20);
			                });
			            }
			        }
				});
				previewPanel.add(previewImg);
			}; 
			
  			var previewTools = 
  				[{ xtype: 'tbfill' }, {
  		            xtype: 'image',
  		            src: '/athenachartengine/img/refresh.png',
  		            cls: 'tool-icon',
  		            listeners: {
  		            	click: {
  		            		element: 'el',
  		            		fn: function(){
  		            			
      							var sbiJson = Sbi.chart.designer.Designer.exportAsJson(true);

      							var parameters = {
  									jsonTemplate: Ext.JSON.encode(sbiJson)
  								};
      							
  								chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
									var chartConf = response.responseText;
	
									var parameters = {
	      									options: chartConf,
	      									content:'options',
	      									type:'image/png',
	      									width: previewPanel.getWidth(),
	      									scale: undefined,
	      									constr:'Chart',
	      									callback: undefined,
	      									async: 'true'
	      							};
									
	      							chartExportWebServiceManager.run('exportPng', parameters, [], 
      									function (response) {
		      								var src = '/highcharts-export-web/'+response.responseText;
		      								setPreviewImage(src);
		      							},
		      							function (response) {
		      								var src = '/athenachartengine/img/preview-not-available.png';
		      								setPreviewImage(src);
		      							}
	      							);

  									
  								}
  								,
      							function (response) {
      								var src = '/athenachartengine/img/preview-not-available.png';
      								setPreviewImage(src);
      							});
      							
  		            		}
  		            	}
  		            },
	            	afterrender: function(c) {
	            		Ext.create('Ext.tip.ToolTip', {
	            			target: c.getEl(),
	            			html: LN('sbi.chartengine.refreshpreview')
	            		});
	            	}
  				}];
  			
  			this.previewPanel.tools = previewTools;
  			
			this.rightYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartRightAxisesContainer',
  				hidden : true
  			});
			
			this.leftYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartLeftAxisesContainer',
  				alias: 'Asse Y',
  				otherPanel: this.rightYAxisesPanel
  			});		
			
			this.categoriesStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
  				storeId: 'categoriesStore'
			});
			
			this.bottomXAxisesPanel = Ext.create("Sbi.chart.designer.ChartCategoriesContainer", {
  				id: 'chartBottomCategoriesContainer',
  				viewConfig: {
  					plugins: {
  						ptype: 'gridviewdragdrop',
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroupAttribute,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroupAttribute
  					},
  					listeners: {  						
  						
  	  					beforeDrop: function(node, data, dropRec, dropPosition) {   	  						
  	  						
  	  						var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
  	  						
  	  						/**
  	  						 * Taking care of the order of the categories (based on their type) for the 
  	  						 * HEATMAP chart type
  	  						 * (danilo.ristovski@mht.net)
  	  						 */  	  						
  	  						if (chartType == "HEATMAP")
  							{
	  	  						if (this.store.data.length == 0 && data.records.length == 1)
  	  							{
	  	  							if (data.records[0].data.categoryDataType != "Timestamp")
  	  								{	  	  								
	  	  								/**
	  	  								 * Show the message that tells user that he should firstly define
	  	  								 * (drop) the item for the categories (attributes) container that
	  	  								 * is of a DATE type (Timestamp)
	  	  								 * (danilo.ristovski@mht.net)
	  	  								 */
		  	  							Ext.Msg.show
		  	  							(
	  	  									{
		  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongdatatypefirst.title"),
		  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongdatatypefirst.warningmessage"),
		  		            					icon : Ext.Msg.WARNING,
		  		            					closable : false,
		  		            					buttons : Ext.Msg.OK,
		  		            					minWidth: 200,
		  		            					
		  		            					buttonText : 
		  		            					{
		  		            						ok : LN('sbi.chartengine.generic.ok')
		  		            					}
	  	  									}
  	  									);	
	  	  								
	  	  								return false;
  	  								}	  	  								
  	  							}
	  	  						else if (this.store.data.length == 1 && data.records.length == 1)
  	  							{	  	  	
	  	  							if (dropPosition == "after" && data.records[0].data.categoryDataType == "Timestamp" ||
	  	  								dropPosition == "before" && data.records[0].data.categoryDataType != "Timestamp")
  	  								{
		  	  							Ext.Msg.show
		  	  							(
	  	  									{
		  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongorderafterbefore.title"),	
		  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongorderafterbefore.warningmessage"),	
		  		            					icon : Ext.Msg.WARNING,
		  		            					closable : false,
		  		            					buttons : Ext.Msg.OK
  	  										}
  	  									);
	  	  								
		  	  							return false;
  	  								}	 
	  	  							
	  	  							/**
	  	  							 * If we already have one item in the CATEGORY (X-axis) container 
	  	  							 * and we want to add the second (the last one) item, we should
	  	  							 * check if that item inside the container is of type that is not
	  	  							 * the DATE (Timestamp). In that case user MUST drop the item that
	  	  							 * is of DATE (Timestamp) type.
	  	  							 * (danilo.ristovski@mht.net)
	  	  							 */
	  	  							if (this.store.data.items[0].data.categoryDataType != "Timestamp" && 
	  	  									data.records[0].data.categoryDataType != "Timestamp")
  	  								{
	  	  								Ext.Msg.show
	  	  								(	
  	  										{
		  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.timestampdataneeded.title"),	
		  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.timestampdataneeded.warningmessage"),	
		  		            					icon : Ext.Msg.WARNING,
		  		            					closable : false,
		  		            					buttons : Ext.Msg.OK
	  										}
	  									);
	  	  								
	  	  								return false;
  	  								}
  	  							}
	  	  						else
  	  							{
	  	  							/**
	  	    						 * Preventing rearranging categories if the chart type is the HEATMAP
	  	    						 * (danilo.ristovski@mht.net)
	  	    						 */
	  	  							return false;
  	  							}
  							}  	  
  	  						
  	  						/**
  	  						 * Prevent taking more than one category from the container when we have
  	  						 * one of these chart types.
  	  						 * (danilo.ristovski@mht.net)
  	  						 */
  	  						if (data.records.length > 1 && (chartType == "RADAR" || chartType == "SCATTER" || 
  	  								chartType == "PARALLEL" || chartType == "HEATMAP"))
  							{
  	  							return false;
  							}  	  						
  	  						
  	  						if(dropRec && data.view.id != this.id) { // if the dropping item comes from another container
	  	  						var thisStore = dropRec.store;
		  	  					var storeCategoriesLength = thisStore.data.items.length;		  	  					
		  	  					
		  	  					for(var rowIndex = 0; rowIndex < storeCategoriesLength; rowIndex++) {
			  	      				var categoryItem = thisStore.getAt(rowIndex);
			  	      				 		  	      				
			  	      				/**
			  	      				 * (0)	Any chart: 		If we already have category that we are trying to 
			  	      				 * 						drop in this panel for any - prevent dropping.
			  	      				 * 
			  	      				 * (1) 	RADAR chart: 	If we already have one category column inside the 
			  	      				 * 						X-axis panel (bottom panel, category container). It
			  	      				 * 						must have exactly one category inside the X-axis panel.
			  	      				 * 
			  	      				 * (2) 	SCATTER chart: 	also MUST have only one category
			  	      				 * 
			  	      				 * (3) 	PARALLEL, TREEMAP (earlier) and HEATMAP chart: MUST have exactly 2 categories
			  	      				 * 
			  	      				 * (danilo.ristovski@mht.net)
			  	      				 */			  	      				
			  	      				if(data.records[0].get('categoryColumn') == categoryItem.get('categoryColumn') 
			  	      						|| (this.store.data.length == 1 && 
			  	      								(chartType == "RADAR" || chartType == "SCATTER")) 
			  	      									|| (this.store.data.length == 2 && 
			  	      											(chartType == "PARALLEL" || 
			  	      													chartType == "HEATMAP"))) {
			  	      					return false;
			  	      				}
			  	      			}
	  	  					}
  						},
  					}
  				},
  				
  				store: this.categoriesStore,
  				
  				axisData: Sbi.chart.designer.ChartUtils.createEmptyAxisData(true),
  				
				plugins: [{
					ptype:	 'cellediting',
					clicksToEdit: 1
				}],

				controller: Ext.create('Ext.app.ViewController', {
			        onTitleChange: function (barTextField, textValue) {
			        	this.view.axisData.titleText = textValue;
			        }
			    }),
			    listeners: {
			    	updateAxisTitleValue: function(textValue) {
			    		this.axisData.titleText = textValue;
			    		var textfieldAxisTitle = Ext.getCmp('textfieldAxisTitle');
			    		textfieldAxisTitle.setValue(textValue);
			    	}
			    },

				title: {
					hidden: true 
				}, 
				tools:[
				    
				    // TEXT AREA
				    {
				    	xtype: 'textfield',
				    	id: 'textfieldAxisTitle',
						flex: 10,
						hidden: false, // *_*
						allowBlank:  true,
			            emptyText: LN('sbi.chartengine.designer.emptytext.axistitle'),
						selectOnFocus: true,
						listeners: {
				            change: 'onTitleChange',
				        }
					},
					
					// STYLE POPUP
					{
					    type:'gear',
					    tooltip: LN('sbi.chartengine.designer.tooltip.setaxisstyle'),
					    id: "stylePopupBottomPanel", // (danilo.ristovski@mht.net)
					    hidden: false, // *_*
					    flex: 1,
					    handler: function(event, toolEl, panelHeader) {
					    	var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
					    	if(chartType.toUpperCase() != 'PIE') {
						    	var thisChartColumnsContainer = panelHeader.ownerCt;
						    	
						    	var axisStylePopup = Ext.create('Sbi.chart.designer.AxisStylePopup', {
						    		axisData: thisChartColumnsContainer.getAxisData()
								});
						    	
//						    	console.log(axisStylePopup);
								
						    	/**
					    		 * (danilo.ristovski@mht.net)
					    		 */
						    	if (chartType.toUpperCase() == 'HEATMAP')
					    		{
						    		axisStylePopup.getComponent('titleFieldSetForAxis').hide();
					    		}	
						    	
						    	axisStylePopup.show();						    	
					    	}
					    	
					    		
						}
					}					
				],
			    
				hideHeaders: true,
  				columns: [{
					text: LN('sbi.chartengine.designer.columnname'), 
					dataIndex: 'categoryColumn',
					sortable: false,
					flex: 10
				}, {
					text: LN('sbi.chartengine.designer.columnalias'), 
					dataIndex: 'axisName',
					sortable: false,
					flex: 10,
					editor: {
						xtype: 'textfield',
						selectOnFocus: true,
					}
					
				}, {
					menuDisabled: true,
					sortable: false,
					xtype: 'actioncolumn',
					align : 'center',
					flex: 1,
					items: [{
						icon: '/athena/themes/sbi_default/img/delete.gif',
						tooltip: LN('sbi.generic.remove'),
						handler: function(grid, rowIndex, colIndex) {
							var store = grid.getStore();
							var item = store.getAt(rowIndex);
							
							var categoryColumn = item.get('categoryColumn');
							
							Ext.Msg.show({
            					title : '',
            					message : Sbi.locale.sobstituteParams(
      								LN('sbi.chartengine.designer.removecategory'), 
      								[categoryColumn]),
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
            							var rec = store.removeAt(rowIndex);
            						}
            					}
            				});
						}
					}]
				}],
  				
  				setAxisData: function(axisData) {
  					this.axisData = axisData;
  					this.fireEvent('updateAxisTitleValue', axisData.titleText);
  				},
  				getAxisData: function() {
  					return this.axisData;
  				}
  				
  			});
			
			
			
			/* START: Hiding the bottom (X) axis title textbox and gear tool
			 *  if the already existing (saved) chart (document) is one of the 
			 *  specified chart types.
			 * (danilo.ristovski@mht.net)
			 *  */
			var typeOfChart = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			if (typeOfChart == "SUNBURST" || typeOfChart == "WORDCLOUD" || 
					typeOfChart == "TREEMAP" || typeOfChart == "PARALLEL" ||
						typeOfChart == "HEATMAP")
			{
				// Hide the bottom (X) axis title textbox				
				Ext.getCmp("chartBottomCategoriesContainer").tools[0].hidden = true;
				
				// Hide the gear icon on the bottom (X) axis panel
				if (typeOfChart != "HEATMAP")
				{
					Ext.getCmp("chartBottomCategoriesContainer").tools[1].hidden = true;
				}
			}
			// END
		
			this.chartStructure = Ext.create('Sbi.chart.designer.ChartStructure', {
  				title: LN('sbi.chartengine.designer.step1'),
  				leftYAxisesPanel: this.leftYAxisesPanel,
  				previewPanel: this.previewPanel,
  				rightYAxisesPanel: this.rightYAxisesPanel,
  				bottomXAxisesPanel: this.bottomXAxisesPanel
  			});
					
			if (typeOfChart == "GAUGE")
			{
				this.bottomXAxisesPanel.hide();
			}
			else
			{
				this.bottomXAxisesPanel.show();
			}
			
			// Creating step 2 panel
			this.cModel = 
				Sbi.chart.designer.ChartUtils.createChartConfigurationModelFromJson(jsonTemplate);
					
			this.cViewModel = Ext.create('Ext.app.ViewModel',{
  				data: {
  					configModel: this.cModel
				}
  			});
						
			this.chartConfiguration = Ext.create('Sbi.chart.designer.ChartConfiguration', {
  				title: LN('sbi.chartengine.designer.step2'),
  				viewModel: this.cViewModel
  			});
			
			// Creating step 3 panel
			this.advancedEditor = Ext.create('Sbi.chart.designer.AdvancedEditor', {
  				id: 'advancedEditor',
  				title: LN('sbi.chartengine.designer.step3')
  			});
			
			// tabs integration
			var coreServiceManager = this.coreServiceManager;
			
			this.stepsTabPanel = Ext.create('Ext.tab.Panel', {
  				bodyBorder: false,
  				width: '100%',
  				region: 'center',
				title: {hidden: true },
				previousTabId: '',
  				tools:[{ xtype: 'tbfill' }, {
  		            xtype: 'image',
  		            src: '/athenachartengine/img/save.png',
  		            cls: 'tool-icon',
  		            listeners: {
  		            	click: {
  		            		element: 'el',
  		            		fn: function(){
  		            			
  		            			var activeTab = Sbi.chart.designer.Designer.stepsTabPanel.getActiveTab();
  		            			if (activeTab.getId() == 'advancedEditor') {  		            				
  		            				var json = activeTab.getChartData();  		            				
  		            				Sbi.chart.designer.Designer.update(json);
  		            			}
  		            			var errorMessages = Sbi.chart.designer.Designer.validateTemplate();

  		            			if (errorMessages == false) {
  		            				Ext.Msg.show({
  		            					title : LN('sbi.chartengine.designer.savetemplate.title'),
  		            					message : LN('sbi.chartengine.designer.savetemplate.msg'),
  		            					icon : Ext.Msg.QUESTION,
  		            					closable : false,
  		            					buttons : Ext.Msg.OKCANCEL,
  		            					buttonText : 
  		            					{
  		            						ok : LN('sbi.generic.save'),
  		            						cancel : LN('sbi.generic.cancel')
  		            					},
  		            					fn : function(buttonValue, inputText, showConfig){
  		            						if (buttonValue == 'ok') {  		            							
  		            							Ext.getBody().mask(LN('sbi.chartengine.designer.savetemplate.loading'), 'x-mask-loading');
  		            							  		 
  		            							
  		            							
  		            							var exportedAsOriginalJson = Sbi.chart.designer.Designer.exportAsJson(true);
  		            							jsonTemplate = exportedAsOriginalJson;
//  		            							console.log(exportedAsOriginalJson);
  		            							
//  		            							exportedAsOriginalJson.CHART.styleCustom = globalThis.styleCustom;
  		            							
//  		            							console.log(globalThis.styleCustom);
  		            							
//  		            							console.log(jsonTemplate.CHART.AXES_LIST);
//  		            							console.log(exportedAsOriginalJson.CHART.AXES_LIST);
//  		            							if (Object.is(jsonTemplate.CHART.AXES_LIST,exportedAsOriginalJson.CHART.AXES_LIST))
//  		            								console.log("ISTO");
//  		            							else
//  		            								console.log("RAZLICITO");
////  		            							globalThis.styleCustom
  		            							
  		            							
  		            							
  		            							if(isCockpit){
  		            								var chartEngineWidgetDesigner = window.parent.cockpitPanel.widgetContainer.widgetEditorWizard.editorMainPanel.widgetEditorPage.widgetEditorPanel.mainPanel.customConfPanel.designer;
  		            								
  		            								chartEngineWidgetDesigner.chartTemplate = exportedAsOriginalJson;
  		            								chartEngineWidgetDesigner.setAggregationsOnChartEngine();
  		            								Ext.getBody().unmask();
  		            							}else{
  		            								var parameters = {
  	  		            									jsonTemplate: Ext.JSON.encode(exportedAsOriginalJson),
  	  		            									docLabel: docLabel
  	  		            							};
  	  		            							coreServiceManager.run('saveChartTemplate', parameters, [], function (response) {});
  	  		            							Ext.getBody().unmask();
  		            							}
  		            								
  		            						}
  		            					}
  		            				});
  		            			} else {
  		            				Ext.Msg.show({
  		            					title : LN('sbi.chartengine.validation.errormessage'),
  		            					message : errorMessages,
  		            					icon : Ext.Msg.WARNING,
  		            					closable : false,
  		            					buttons : Ext.Msg.OK
  		            				});
  		            			}
  		            		}
  		            	},
  		            	afterrender: function(c) {
  		            		Ext.create('Ext.tip.ToolTip', {
  		            			target: c.getEl(),
  		            			html: LN('sbi.generic.save')
  		            		});
  		            	}
  		            }
  		        }, 
  		        {
  		            xtype: 'image',
  		            src: '/athenachartengine/img/saveAndGoBack.png',
  		            cls: 'tool-icon',
  		            hidden: isCockpit,
  		            listeners: {
  		            	click: {
  		            		element: 'el',
  		            		fn: function(){
  		            			var activeTab = Sbi.chart.designer.Designer.stepsTabPanel.getActiveTab();
  		            			if (activeTab.getId() == 'advancedEditor') {
  		            				var json = activeTab.getChartData();
  		            				Sbi.chart.designer.Designer.update(json);
  		            			}
  		            			var errorMessages = Sbi.chart.designer.Designer.validateTemplate();

  		            			if (errorMessages == false) {
  		            				Ext.Msg.show({
  		            					title : LN('sbi.chartengine.designer.savetemplate.title'),
  		            					message : LN('sbi.chartengine.designer.savetemplate.msg'),
  		            					icon : Ext.Msg.QUESTION,
  		            					closable : false,
  		            					buttons : Ext.Msg.OKCANCEL,
  		            					buttonText : 
  		            					{
  		            						ok : LN('sbi.generic.save'),
  		            						cancel : LN('sbi.generic.cancel')
  		            					},
  		            					fn : function(buttonValue, inputText, showConfig){
  		            						if (buttonValue == 'ok') {
  		            							Ext.getBody().mask(LN('sbi.chartengine.designer.savetemplate.loading'), 'x-mask-loading');
  		            							var exportedAsOriginalJson = Sbi.chart.designer.Designer.exportAsJson(true);
  		            							var parameters = {
  		            									jsonTemplate: Ext.JSON.encode(exportedAsOriginalJson),
  		            									docLabel: docLabel
  		            							};
  		            							coreServiceManager.run('saveChartTemplate', parameters, [], function (response) {
  		            								var context = coreServiceManager.initialConfig.serviceConfig.context;
  		            								parent.location.href = context+'/servlet/AdapterHTTP?PAGE=DetailBIObjectPage&MESSAGEDET=DETAIL_SELECT&OBJECT_ID=9&LIGHT_NAVIGATOR_BACK_TO=1';

  		            							});
  		            						}
  		            					}
  		            				});
  		            			} else {
  		            				Ext.Msg.show({
  		            					title : LN('sbi.chartengine.validation.errormessage'),
  		            					message : errorMessages,
  		            					icon : Ext.Msg.WARNING,
  		            					closable : false,
  		            					buttons : Ext.Msg.OK
  		            				});
  		            			}
  		            		}
  		            	},
  		            	afterrender: function(c) {
  		            		Ext.create('Ext.tip.ToolTip', {
  		            			target: c.getEl(),
  		            			html: LN('sbi.generic.saveAndGoBack')
  		            		});
  		            	}
  		            }
  		        }],
				listeners: {
				    tabchange: function(tabPanel, tab){
				    	
				    	if(tab.getId() == 'advancedEditor') {
				    		Sbi.chart.designer.Designer.chartTypeColumnSelector.disable();
				    		
				    		var json = Sbi.chart.designer.Designer.exportAsJson();
				    		
							tab.setChartData(json);
							
						} else if(tabPanel.previousTabId == 'advancedEditor') {
							Sbi.chart.designer.Designer.chartTypeColumnSelector.enable();
							
							var advancedEditor = Ext.getCmp('advancedEditor');
							if(advancedEditor.dataChanged == true) {
								var newJson = advancedEditor.getChartData();
								var oldJson = Sbi.chart.designer.Designer.exportAsJson();
								
								var tabChangeChecksMsgs = Sbi.chart.designer.Designer.tabChangeChecksMessages(oldJson, newJson);
								if(Sbi.chart.designer.Designer.tabChangeChecksFlag && tabChangeChecksMsgs) {
										Ext.Msg.show({
											title : LN('sbi.chartengine.designer.tabchange.title'),
											message : tabChangeChecksMsgs,
											icon : Ext.Msg.WARNING,
											closable : false,
											buttons : Ext.Msg.OK,
											buttonText : 
											{
												ok : LN('sbi.chartengine.generic.ok'),
											}
										});
								
									tabPanel.setActiveTab('advancedEditor');
									return false;
			    				} else {
			    					Sbi.chart.designer.Designer.update(newJson);
			    				}
							}
						}
						tabPanel.previousTabId = tab.getId();
					}
				},  
  				items: [
  					this.chartStructure,
  					this.chartConfiguration,
  					this.advancedEditor,
  				]
  			});
			
			// Creating designer main panel
			this.designerMainPanel = Ext.create('Ext.panel.Panel', {
				id: 'designerMainPanel',
  				renderTo: Ext.getBody(),
  				xtype: 'layout-border',
  				requires: [
  					'Ext.layout.container.Border'
  				],
  				layout: 'border',
  				height: '100%',                            
  				bodyBorder: false,
  				defaults: {
  					collapsible: false,
  					split: true,
  					bodyPadding: 10
  				},
  				items: [
  					this.chartTypeColumnSelector,
  					this.stepsTabPanel,
  				],
  				
//  				listeners:
//				{
//  					aaa: function(text)
//  					{  					
//  						//if (globalThis.styleCustom == true)
//  						globalThis.chartTypeColumnSelector.fireEvent("ppp",text);
////  						console.log(globalThis.styleCustom);
//  						console.log("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
////  						globalThis.styleCustom = true;
//  						
//  					}
//				}
  			});
			
			//Handle resize event for making the designer responsive
			Ext.on('resize', function(w, h){
				this.chartStructure.updateLayout();
				this.chartConfiguration.updateLayout();
				this.advancedEditor.updateLayout();
				this.designerMainPanel.updateLayout();
			}, this);
			
  			/*  LOADING CONFIGURATION FROM TEMPLATE >>>>>>>>>>>>>>>>>>>> */
  			/* START LOADING Y AXES, X AXIS AND SERIES >>>>>>>>>>>>>>>>>>>> */
  			this.loadAxesAndSeries(jsonTemplate);
  			/* END LOADING Y AXES, X AXIS AND SERIES <<<<<<<<<<<<<<<<<<<< */
  			
			/* START LOADING CATEGORIES >>>>>>>>>>>>>>>>>>>> */
			this.loadCategories(jsonTemplate);
			
			/* END LOADING CATEGORIES <<<<<<<<<<<<<<<<<<<< */
  			
  			/*  LOADED CONFIGURATION FROM TEMPLATE <<<<<<<<<<<<<<<<<<<< */
		},
		
		loadCategories: function(jsonTemplate) {
			var categoriesStore = this.categoriesStore;
			// Reset categoriesStore
			categoriesStore.loadData({});
			var chartType = jsonTemplate.CHART.type;
			
			if(!(jsonTemplate.CHART.VALUES && jsonTemplate.CHART.VALUES.CATEGORY)) {
				return;
			}
			
			// NEWCHARTS: MANAGE MULTIPLE CATEGORIES AS A LIST
			var category = jsonTemplate.CHART.VALUES.CATEGORY;
			
			// (danilo.ristovski@mht.net)
			if (chartType.toUpperCase() == "SUNBURST" || chartType.toUpperCase() == "WORDCLOUD" || 
					chartType.toUpperCase() == "TREEMAP" || chartType.toUpperCase() == "PARALLEL" || 
						chartType.toUpperCase() == "HEATMAP")
			{	
				if (category.length == undefined || category.length == null)
				{
					var mainCategory = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
						axisName: category.name ? category.name: category.column,
						axisType: 'ATTRIBUTE', 
						//categoryDataType: 
						categoryColumn: category.column
					});
					
					categoriesStore.add(mainCategory);
				}
				else
				{
					for (var i=0; i<category.length; i++) {	
						var mainCategory = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
							axisName: category[i].name ? category[i].name: category[i].column,
							axisType: 'ATTRIBUTE', 
							
							categoryColumn: category[i].column
						});
						
						categoriesStore.add(mainCategory);
					}
				}
							
			}
			else	// If chart type is PIE, BAR or LINE
			{
				var mainCategory = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
					axisName: category.name != undefined ? category.name: category.column,
					axisType: 'ATTRIBUTE', 
					
					categoryColumn: category.column,
					categoryGroupby: category.groupby,
					categoryStacked: category.stacked,
					categoryStackedType: category.stackedType, 
					categoryOrderColumn: category.orderColumn, 
					categoryOrderType: category.orderType
				});
						
				categoriesStore.add(mainCategory);
				
				var groupBy = category.groupby;
				var groupByNames = category.groupbyNames;

				if(groupBy) {
					var gbyCategories = groupBy.split(',');
					var gbyNames = groupByNames ? groupByNames.split(',') : groupBy.split(',');

					Ext.Array.each(gbyCategories, function(gbyCategory, index) {
						var newCat = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
							axisName: gbyNames[index],
							axisType: 'ATTRIBUTE', 

							categoryColumn: gbyCategory,
							categoryStacked: ''
						});
						categoriesStore.add(newCat);
					});
				}
			}			
		},			
			
		loadAxesAndSeries: function(jsonTemplate) {
			var leftYAxisesPanel = this.leftYAxisesPanel;
			var rightYAxisesPanel = this.rightYAxisesPanel;
			var bottomXAxisesPanel = this.bottomXAxisesPanel;
			var globThis = this;
			Sbi.chart.designer.ChartColumnsContainerManager.resetContainers();

			var theStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
			var yCount = 1;
			
			Ext.Array.each(jsonTemplate.CHART.AXES_LIST.AXIS, function(axis, index){
				
				if(axis.type.toUpperCase() == "SERIE"){

					var isDestructible = (yCount > 1);
					var panelWhereAddSeries = (yCount == 1) ? rightYAxisesPanel : null;
					// pie workaround "!axis.position"
					if(!axis.position || axis.position.toLowerCase() == 'left') {											
						
						// *_* 					
						/* START: Hiding the left (Y) axis title textbox, gear and plus tools
						 *  if the already existing (saved) chart (document) is one of the specified chart types.
						 *  */
						var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
						var hideAxisTitleTextbox = false;
						var hideGearTool = false;
						var hidePlusGear = false;
						
						if (chartType == "SUNBURST" || chartType == "PARALLEL" ||
								chartType == "WORDCLOUD" || chartType == "TREEMAP")
						{
							hideAxisTitleTextbox = true;
							hideGearTool = true;
							hidePlusGear = true;
						}
						// (danilo.ristovski@mht.net)
						else if (chartType == "RADAR" || chartType == "SCATTER" || chartType == "HEATMAP")	
						{
							hidePlusGear = true;
							
							if (chartType == "HEATMAP")
							{
								hideAxisTitleTextbox = true;
							}
						}
						// END
						
						// (danilo.ristovski@mht.net)
						var config = 
						{
							"idAxisesContainer":leftYAxisesPanel.id , 
							"id": '', 
							"panelWhereAddSeries":panelWhereAddSeries, 
							"isDestructible":isDestructible, 
							"dragGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure,
							"dropGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure, 
							"axis":axis, 
							"axisTitleTextboxHidden":hideAxisTitleTextbox, 
							"gearHidden":hideGearTool, 
							"plusHidden":hidePlusGear
						};
						
						var newColumn = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(config);
						leftYAxisesPanel.add(newColumn);

					} else {
						
						// (danilo.ristovski@mht.net)
						var config = 
						{
							"idAxisesContainer":rightYAxisesPanel.id, 
							"id": '', 
							"panelWhereAddSeries":panelWhereAddSeries, 
							"isDestructible":isDestructible, 
							"dragGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure,
							"dropGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure, 
							"axis":axis
						};
						
						var newColumn = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(config);
						rightYAxisesPanel.add(newColumn);
						rightYAxisesPanel.show();
					}
					yCount++;

				} else if(axis.type.toUpperCase() == "CATEGORY"){
					var axisData = (axis && axis != null)? 
							Sbi.chart.designer.ChartUtils.convertJsonAxisObjToAxisData(axis) : 
								Sbi.chart.designer.ChartUtils.createEmptyAxisData(true);
					
					bottomXAxisesPanel.setAxisData(axisData);
				}
			});
			
			if(jsonTemplate.CHART.VALUES && jsonTemplate.CHART.VALUES.SERIE) {
				Ext.Array.each(jsonTemplate.CHART.VALUES.SERIE, function(serie, index){
					var axisAlias = serie.axis ? serie.axis : 'Y';
					Ext.Array.each(theStorePool, function(store, index){
						
						if(store.axisAlias === axisAlias) {
							var tooltip = serie.TOOLTIP ? serie.TOOLTIP : {};
							var tooltipStyle = serie.TOOLTIP ? serie.TOOLTIP.style : '';
							var jsonTooltipStyle = Sbi.chart.designer.ChartUtils.jsonizeStyle(tooltipStyle);
							
							if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE")
							{								
								var newCol = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
									id: (serie.id && serie.id != '')? serie.id : 'serie' + ChartColumnsContainer.idseed++,
											axisName: serie.name && serie.name != '' ? serie.name : serie.column,
											axisType: 'MEASURE',
																
											backgroundColorDial: serie.DIAL.backgroundColorDial,
											
											yPositionDataLabels: serie.DATA_LABELS.yPositionDataLabels,
											colorDataLabels: serie.DATA_LABELS.colorDataLabels,
											formatDataLabels: serie.DATA_LABELS.formatDataLabels,
											
											serieAxis: store.axisAlias,
											serieGroupingFunction: serie.groupingFunction != ''? serie.groupingFunction : 'SUM',
											serieType: serie.type,
											serieOrderType: serie.orderType,
											serieColumn: serie.column,
											serieColor: serie.color,
											serieShowValue: serie.showValue,
											seriePrecision: serie.precision + '',
											seriePrefixChar: serie.prefixChar,
											seriePostfixChar: serie.postfixChar,
											
											serieTooltipTemplateHtml: tooltip.templateHtml,
											serieTooltipBackgroundColor: tooltip.backgroundColor,
											serieTooltipAlign: jsonTooltipStyle.align,
											serieTooltipColor: jsonTooltipStyle.color,
											serieTooltipFont: jsonTooltipStyle.font,
											serieTooltipFontWeight: jsonTooltipStyle.fontWeight,
											serieTooltipFontSize: jsonTooltipStyle.fontSize
								});								
							}
							else
							{
								var newCol = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
									id: (serie.id && serie.id != '')? serie.id : 'serie' + ChartColumnsContainer.idseed++,
											axisName: serie.name && serie.name != '' ? serie.name : serie.column,
											axisType: 'MEASURE',
																					
											serieAxis: store.axisAlias,
											serieGroupingFunction: serie.groupingFunction != ''? serie.groupingFunction : 'SUM',
											serieType: serie.type,
											serieOrderType: serie.orderType,
											serieColumn: serie.column,
											serieColor: serie.color,
											serieShowValue: serie.showValue,
											seriePrecision: serie.precision + '',
											seriePrefixChar: serie.prefixChar,
											seriePostfixChar: serie.postfixChar,
											
											serieTooltipTemplateHtml: tooltip.templateHtml,
											serieTooltipBackgroundColor: tooltip.backgroundColor,
											serieTooltipAlign: jsonTooltipStyle.align,
											serieTooltipColor: jsonTooltipStyle.color,
											serieTooltipFont: jsonTooltipStyle.font,
											serieTooltipFontWeight: jsonTooltipStyle.fontWeight,
											serieTooltipFontSize: jsonTooltipStyle.fontSize
								});
							}
							
							
							// *_* 
							globThis.seriesBeforeDropStore.add(newCol);
							
							store.add(newCol);
						}
					});
				});
			}
		},
		
		update: function(jsonTemplate) {
			this.jsonTemplate = jsonTemplate;
			
			var selectedChartType = jsonTemplate.CHART.type.toUpperCase();
			this.chartTypeSelector.setChartType(selectedChartType);
	
			this.jsonTemplateHistory.push(jsonTemplate);
			var jsonTemplateHistoryLen = this.jsonTemplateHistory.length;
			this.jsonTemplateHistoryIterator = jsonTemplateHistoryLen - 1;

			this.updateStep1Data(jsonTemplate);
			this.updateStep2Data(jsonTemplate);
		},
		
		updateStep1Data: function(jsonTemplate) {
			// Updating step 1 data
			this.loadCategories(jsonTemplate);
			this.loadAxesAndSeries(jsonTemplate);

		}, 
		
		updateStep2Data: function(jsonTemplate) {
			// Updating step 2 data
			this.cModel.drop();
			this.cModel.erase();
			
			this.cModel = 
				Sbi.chart.designer.ChartUtils.createChartConfigurationModelFromJson(jsonTemplate);

			this.cViewModel.setData({
				configModel: this.cModel
			});

			this.chartConfiguration.setData({
  				viewModel: this.cViewModel
  			});
			
			//updating color Palette
			var paletteStore = Ext.data.StoreManager.lookup('chartConfigurationPaletteStore');
			// Reset
			paletteStore.loadData({});
			// Load json colors
			paletteStore.setData(this.cModel.get('colorPalette'));
		}, 
		
		exportAsJson: function(finalJson) {
			finalJson = finalJson || false;
			
			// resulted json from 1st and 2nd designer steps (without properties catalogue)
			var exported1st2ndSteps = Sbi.chart.designer.ChartUtils.exportAsJson(this.cModel);
						
			// default properties catalogue by used chart library, depending on selected chart type 
    		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
    		chartType = chartType.toLowerCase();
			var library = this.chartLibNamesConfig[chartType];
			var catalogue = propertiesCatalogue[library];
			
			// default properties catalogue by used chart library, depending on selected chart type 
			var oldJsonChartType = Sbi.chart.designer.Designer.jsonTemplate.CHART.type;
			oldJsonChartType = oldJsonChartType.toLowerCase();
			var oldLibrary = this.chartLibNamesConfig[oldJsonChartType];
			
			// last json template in memory
			var lastJsonTemplate = Sbi.chart.designer.Designer.jsonTemplate;
			
			// last json in memory with applied properties catalogue
			var appliedPropertiesOnOldJson = Sbi.chart.designer.ChartUtils.mergeObjects(catalogue, lastJsonTemplate);
			
			// comparison and merge generated json template with the old one
			var overwrittenJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(appliedPropertiesOnOldJson, exported1st2ndSteps, true);
			
			// add default catalogue properties in case there are new elements generated by designer
			var newJsonTemplate = (library === oldLibrary)?
				Sbi.chart.designer.ChartUtils.mergeObjects(catalogue, overwrittenJsonTemplate, true)
				: Sbi.chart.designer.ChartUtils.mergeObjects(catalogue, exported1st2ndSteps, true);
				
			if(finalJson == true) {
				return Sbi.chart.designer.Designer.removeIdAttribute(newJsonTemplate);
			} else {
				return newJsonTemplate;
			}
			
		},
		removeIdAttribute: function(templateJson) {
			if(templateJson.CHART){
				if(templateJson.CHART.AXES_LIST 
						&& templateJson.CHART.AXES_LIST.AXIS 
						&& templateJson.CHART.AXES_LIST.AXIS.length) {
					
					var axes = templateJson.CHART.AXES_LIST.AXIS;
					for(i in axes) {
						var axis = axes[i];
						delete axis.id;
					}
				}
				if(templateJson.CHART.VALUES
						&& templateJson.CHART.VALUES.SERIE
						&& templateJson.CHART.VALUES.SERIE.length) {
					
					var series = templateJson.CHART.VALUES.SERIE;
					for(i in series) {
						var serie = series[i];
						delete serie.id;
					}
				}
				if(templateJson.CHART.COLORPALETTE
						&& templateJson.CHART.COLORPALETTE.COLOR
						&& templateJson.CHART.COLORPALETTE.COLOR.length) {
					
					var colors = templateJson.CHART.COLORPALETTE.COLOR;
					for(i in colors) {
						var color = colors[i];
						delete color.id;
					}
				}
			}
			return templateJson;
		},
		
		/**
		 * Returns a list of validation errors as string format
		 * */		
		validateTemplate: function() {
			var errorMsg = '';			
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			if (Sbi.chart.designer.ChartUtils.getSeriesDataAsOriginalJson().length == 0) {
				errorMsg += "- " + LN('sbi.chartengine.validation.addserie') + '<br>';
			}
			
			if (Sbi.chart.designer.ChartUtils.getCategoriesDataAsOriginalJson() == null && chartType != "GAUGE") {
				errorMsg += "- " + LN('sbi.chartengine.validation.addcategory') + '<br>';
			}						
			else // (danilo.ristovski@mht.net)
			{
				var categoriesAsJson = Sbi.chart.designer.ChartUtils.getCategoriesDataAsOriginalJson();				
				
				if ((chartType == "PARALLEL" || chartType == "HEATMAP") &&
						categoriesAsJson.length != 2)
				{
					errorMsg += "- " + LN("sbi.chartengine.validation.exactlyTwoCategories") + '<br>'; 
				}
				else if (chartType == "TREEMAP" && categoriesAsJson.length < 2)
				{
					errorMsg += "- " + LN("sbi.chartengine.validation.atLeastTwoCategories") + '<br>';
				}
			}

			var selectedChartType = this.chartTypeSelector.getChartType().toLowerCase();
			var serieStores = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
    		for(var storeIndex in serieStores) {
    			var store = serieStores[storeIndex];
    			var axisAlias = store.axisAlias;
    			
    			for(var rowIndex in store.data.items) {
    				var serieAsMap = store.getAt(rowIndex);
    				
    				var serieColumn = serieAsMap.get('serieColumn') != undefined? serieAsMap.get('serieColumn'): '';
    				var serieName = serieAsMap.get('axisName') != undefined? serieAsMap.get('axisName'): '';
    				var serieType = serieAsMap.get('serieType') != undefined? serieAsMap.get('serieType').toLowerCase(): '';
    				
    				if((selectedChartType == 'pie' && (serieType != '' && serieType != 'pie')) || 
    					((selectedChartType == 'bar' || selectedChartType == 'line') && (serieType == 'pie'))) {
    					
						errorMsg += "- " 
							+ Sbi.locale.sobstituteParams(
								LN('sbi.chartengine.validation.wrongserietype'), 
								[selectedChartType, serieType, serieColumn, serieName]) 
							+ '<br>';
    				}
    			}
    		}
			
			return errorMsg != ''? errorMsg : false;
		},
		
		cleanAxesSeriesAndCategories: function() {
			//Reset Series and Categories
			this.bottomXAxisesPanel.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData(true));
			
			this.categoriesStore.removeAll();
			
			var serieStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
			
			for(i in serieStorePool) {
				serieStorePool[i].removeAll();
			}
			
			this.rightYAxisesPanel.removeAll();

			var leftColumnsContainerId = this.leftYAxisesPanel.items.keys[0];
			var leftColumnsContainer = Ext.getCmp(leftColumnsContainerId);
			
			leftColumnsContainer.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData(false, true));
			
			//Since it remained only one serieStore let's update its data for it complies with the new leftColumnsContainer data
			serieStorePool[0].axisAlias = leftColumnsContainer.axisData.alias;
			serieStorePool[0].idAxisesContainer = leftColumnsContainer.axisData.id;
		},
		
		/**
		 * TODO: Added 16.07 - Called inside the ChartTypeSelector. Removes everything from the
		 * X-axis panel if we move from BAR or LINE to SCATTER or RADAR, because for the last to we can have ONLY ONE
		 * CATEGORY, while we can have more than one for the first pair (BAR/LINE).
		 * (danilo.ristovski@mht.net)
		 */ 
		cleanCategoriesAxis: function()
		{
			this.bottomXAxisesPanel.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData(true));			
			this.categoriesStore.removeAll();
		},
		
		tabChangeChecksMessages: function(oldJson, newJson) {
			var result = '';
			
			var oldJsonType = oldJson.CHART.type.toLowerCase();
			var newJsonType = newJson.CHART.type.toLowerCase();
			if((oldJsonType == 'pie' && newJsonType != 'pie') || 
					((oldJsonType == 'bar' || oldJsonType == 'line') && (newJsonType != 'bar' && newJsonType != 'line'))) {
				
				result += '- ' + Sbi.locale.sobstituteParams(
						LN('sbi.chartengine.designer.tabchange.changetypeerror'),[oldJsonType, newJsonType]);
			}
			
			return result == ''? false : result;
		}
		
    }
});