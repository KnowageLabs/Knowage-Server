Ext.define('Sbi.chart.designer.Designer', {
    extend: 'Ext.Base',
    alternateClassName: ['Designer'],
	requires: [
        'Sbi.chart.rest.WebServiceManagerFactory',
        'Sbi.chart.designer.ChartUtils',
        'Sbi.chart.designer.AxisesContainerStore',
        'Sbi.chart.designer.AxisesPicker',
        'Sbi.chart.designer.ChartTypeColumnSelector',
        'Sbi.chart.designer.ChartCategoriesContainer',
        'Sbi.chart.designer.AxisStylePopup',
        'Sbi.chart.designer.ChartStructure',
        'Sbi.chart.designer.ChartConfigurationModel',
        'Sbi.chart.designer.ChartConfiguration',
        'Sbi.chart.designer.CrossNavigationPanel',
        'Sbi.chart.designer.AdvancedEditor'
    ],

    statics: {
    	noStylePickedStyle: null,
    	tabChangeChecksFlag: true,
    	jsonTemplateStyleExists: false,
    	backupStyleSet: false,
		jsonTemplate: null,
		chartLibNamesConfig: null,
		
		jsonTemplateHistory: [],
		jsonTemplateHistoryIterator: null,
	
    	chartServiceManager: null,
    	coreServiceManager: null,
    	chartExportWebServiceManager: null,
    	docLabel: null,
    	
    	/**
    	 * This is a "back" (return) path for relative path implementation
    	 * (context name and context path improvement). This global variable
    	 * will be used by all JS files inside of this project (root of this
    	 * file) for purpose of dynamic path specification.
    	 * 
    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	 */
    	relativePathReturn: '../../..',
    	
    	chartTypeChanged: null,
    	
		// Left designer panel 
		chartTypeColumnSelector: null,
		
		selectedChartType: '',		
				
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
		
		styleType: '',
		
		chartTypeStoreLoaded: false,
		
		/**
		 * The function that is called on creating completely new chart (new document)
		 * for getting the default style. The default chart style has 'isDefault' 
		 * parameter set to 'true'.
		 * 
		 * @author Ana Tomic (atomic, ana.tomic@mht.net)
		 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		getDefaultStyle: function(){
			var styles=JSON.parse(Sbi.chart.designer.Styles);

			var retTemplate=null;

			for(i=0;i<styles.length;i++){
				if(styles[i].STYLE.isDefault===true){
					
					/**
				    * TEMPLATE tag is now positioned inside of the main, STYLE tag.
				    * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				    */
					retTemplate=styles[i].STYLE.TEMPLATE;
				}
			}

			return retTemplate;
		},
		
		/**
		 * Get the missing JSON configuration elements (properties) in order to define
		 * their default values for any type of chart (including the BAR chart).
		 * 
		 * @author Ana Tomic (atomic, ana.tomic@mht.net)
		 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		getConfigurationForStyle : function(style,changeChartType) {
		      
			   var styles=JSON.parse(Sbi.chart.designer.Styles);
			        
			   /**
			 	* JSON template that keeps the predefined values
			 	* for the different styles parameters. We will
			 	* return this JSON object when needed (e.g. before
			 	* merging old JSON template with the new one (that
			 	* keeps the predefined style parameters), after
			 	* changing the style).
			    */
			   var retTemplate=null;
			   			   
			   for(i=0;i<styles.length;i++){
				   
				   if(styles[i].STYLE.name === style){	
					   /**
					    * TEMPLATE tag is now positioned inside of the main, STYLE tag.
					    * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					    */
					   retTemplate=styles[i].STYLE.TEMPLATE;
					   break;					   
				   } 
			   } 			   
			   
			/**
			 * if no style with given name exists return the
			 * default one
			 */
			if (retTemplate == null) {
				
				for (i = 0; i < styles.length; i++) {
					
					if (styles[i].STYLE.name === "sfnas") {
						
						if(styles.length > 1 && style != "" && !changeChartType && !Sbi.chart.designer.Designer.backupStyleSet){
							Sbi.exception.ExceptionHandler
							.showInfoMessage(
									LN("sbi.chartengine.designer.styleRemoved")

							);
						}
						
						Ext.getCmp("stylePickerComboId").setValue("");
						Sbi.chart.designer.Designer.backupStyleSet = true;
						
						/**
					    * TEMPLATE tag is now positioned inside of the main, STYLE tag.
					    * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					    */
						retTemplate = styles[i].STYLE.TEMPLATE;
						break;
					}
				}

			}
			   return retTemplate;						
		},
				
		initialize: function(sbiExecutionId, 
				userId, 
				hostName, 
				serverPort, 
				docLabel, 
				jsonTemplate, 
				datasetLabel, 
				chartLibNamesConfig, 
				isCockpit, 
				thisContextName, 
				mainContextName, 
				exporterContextName) {
			
			Sbi.chart.designer.ChartUtils.setCockpitEngine(isCockpit);	
			
			/**
			 * Base JSON template that we will use when the new chart (document) is created
			 */
			var baseTemplate = {
					CHART: {
						type: 'BAR',
						AXES_LIST: {
							AXIS: [
							       {alias:'Y', type: 'Serie'},
							       {alias:'X', type: 'Category'}
							       ]
						},
						VALUES: {
							SERIE: []
						},
						COLORPALETTE: {
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
			 * 
			 * @author Ana Tomic (atomic, ana.tomic@mht.net)
			 */
			var globalThis = this;
						
			var applyAxes = true;
			var applySeries = true;
			
			var configApplyAxes = {
				applyAxes: applyAxes,
				applySeries: applySeries,
			};
			
			/**
			 * If we are creating completely new chart (new document) immediately on loading
			 * the Designer page apply the default style.
			 * 
			 * @author Ana Tomic (atomic, ana.tomic@mht.net)
			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (!jsonTemplate.CHART) 
			{				
			    var defaultStyleTemplate = this.getDefaultStyle();
			    
			    /**
			     * If this style JSON template is marked as default in the XML document that holds its 
			     * data.
			     * 
			     * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			     */
			    if(defaultStyleTemplate)
			    {
			    	/**
			    	 * Remove unwanted properties from the JSON template that we are about to apply to
			    	 * the current chart document structure (e.g. if there is some "text" property in
			    	 * the XML style file, remove it from the JSON template). All unwanted properties
			    	 * are set inside of the static "unwantedStyleProps" object inside of the 
			    	 * ChartUtils.js file.
			    	 * 
			    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			    	 */
			    	var defaultStyleTemplateGeneric = 
			    			Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle(defaultStyleTemplate.generic);
			    	
			    	/**
			    	 * I think we don't need applying the current style to axis style configuration, neither to
			    	 * serie style configuration since we are opening newly created (not yet completely (fully)
			    	 * specified) chart document that does not have any serie and for which we are going to have 
			    	 * only one Y-axis panel.
			    	 * 
			    	 * Applying of current style to every single serie item that we are
			    	 * going to drop in the initially empty Y-axis panel is going to be done on "drop" event.
			    	 * 
			    	 * Applying of current style to Y-axis configuration (axis style configuration) is going to
			    	 * be done by this merging.
			    	 */
			    	jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(baseTemplate, defaultStyleTemplateGeneric, {applyAxes: true, applySeries: true});
			    }
			    else
			    {
			    	jsonTemplate = baseTemplate;
			    }
			    
			    newChart = true; 
			}		
					
			Designer.styleName = (jsonTemplate.CHART.styleName) ? (jsonTemplate.CHART.styleName) : "";
			
			/**
			 * Merging JSON templates of specified chart types with the base JSON template
			 * (of type BAR) in order to make the union of all of the JSON elements within
			 * these two types - the base one and the current one. 			 
			 * (danristo :: danilo.ristovski@mht.net) 
			 */
			var chartTypeUpperCase = jsonTemplate.CHART.type.toUpperCase();
			
			if (chartTypeUpperCase == 'PIE' 
				|| chartTypeUpperCase == 'SUNBURST'
					|| chartTypeUpperCase == 'WORDCLOUD'
						|| chartTypeUpperCase == 'TREEMAP'
							|| chartTypeUpperCase == 'PARALLEL'
								|| chartTypeUpperCase == 'RADAR'
									|| chartTypeUpperCase == 'SCATTER'
										|| chartTypeUpperCase == 'HEATMAP'
											|| chartTypeUpperCase == 'CHORD'
												|| chartTypeUpperCase == 'GAUGE') {
								
				/**
				 * If there is just one axis (Y-axis and no X-axis), like in the GAUGE chart and 
				 * if the AXIS property (tag) is not in the form of an array (it is just one object
				 * with some properties, put this object inside an array anyhow, so code could
				 * process it.
				 * 
				 * (danristo :: danilo.ristovski@mht.net) 
				 */
				if (jsonTemplate.CHART.AXES_LIST.AXIS.length == undefined) {
					
					var axisTemp = jsonTemplate.CHART.AXES_LIST.AXIS;
					var axisArray = new Array();
					
					jsonTemplate.CHART.AXES_LIST.AXIS = axisArray;
					jsonTemplate.CHART.AXES_LIST.AXIS.push(axisTemp);
				}							
				
				/**
				 * I think that requiring for "applyAxes" and "applySeries" is not necessary.
				 * 
				 * "applyAxes" will not make any difference since we will in case that is enabled (true)
				 * go through all already existing axes and apply them once again (redundant work). It will
				 * simply overlap the base template. If enable, the GAUGE chart snaps - it cannot be opened.
				 * 
				 * *** NOTE: We can enable it if we expect that in future the base template is going to be specified 
				 * as initial two Y-axes chart document.
				 * 
				 * "applySeries" will (if true) remove all items from serie panel in Designer.
				 * 
				 * !! We are not applying styles here !!
				 */
				
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(baseTemplate, jsonTemplate, {applyAxes: false, applySeries: false});	
				
			}
			
			Sbi.chart.designer.ChartColumnsContainerManager.initInstanceIdFeed( jsonTemplate.CHART.AXES_LIST.AXIS );				
			
			/**
			 * If the chart is already existing (not just created) and if it is of the 
			 * GAUGE type, set the plotband store that keeps the data about the plots
			 * that are linked to the particular chart of this type. Afterwards, when
			 * we open the Axis style configuration for this chart type we will have
			 * the grid panel for the plotbands populated with existing plots (intervals).
			 * (danristo :: danilo.ristovski@mht.net) 
			 */
			if (chartTypeUpperCase == "GAUGE") {
				Sbi.chart.designer.ChartColumnsContainerManager.setPlotbandsStore(jsonTemplate);
			}				
			
			this.docLabel = docLabel;
			this.jsonTemplate = jsonTemplate;
			
			this.jsonTemplateHistory.push(jsonTemplate);
			this.jsonTemplateHistoryIterator = 0;
			
			/**
			 * List of names of the libraries that we use for rendering the charts. 
			 * (comment by: danristo :: danilo.ristovski@mht.net) 
			 */
			this.chartLibNamesConfig = chartLibNamesConfig;
			
			this.mainContextName = mainContextName;
			this.chartEngineContextName = thisContextName;
			
			this.chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager('http', hostName, serverPort, thisContextName, sbiExecutionId, userId);
			this.coreServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getCoreWebServiceManager('http', hostName, serverPort, mainContextName, sbiExecutionId, userId);
			this.chartExportWebServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartExportWebServiceManager('http', hostName, serverPort, exporterContextName, sbiExecutionId, userId);
						
			this.hostName = hostName; 
			this.serverPort = serverPort;
									
			this.chartType = jsonTemplate.CHART.type;
			
			/**
			 * GUI label element that will be placed immediatelly above the style combo box
			 * (on the top of the left panel on the Designer page).
			 * (danilo.ristovski@mht.net)
			 */
			this.styleLabel2 = Ext.create ('Ext.form.Label', {
			        forId: 'chartTypeCombobox',
			        text: LN("sbi.chartengine.designer.chartTypePicker"),
			        //margin: '5 3 3 0'
			});
			
			/**
			 * Create initial (empty) chart type store, so we can call the 
			 * service for data about all chart types that are available 
			 * inside the Designer.
			 * 
			 * Fields: 
			 * 		chartType: 	The full name of the chart type that is going
			 * 					to be displayed in the combo box (the name of 
			 * 					the chart with the first letter in capitals).
			 * 
			 * 		chartTypeAbbr:	
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var chartTypesStore = Ext.create 
			( 
				"Ext.data.Store", 
				
				{
					fields: ["chartType", "chartTypeAbbr", "iconChartType"],
							
					data: []
				}
			);
			
			/**
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			Ext.Ajax.request
			(
				{
				   method : 'GET',
				   url : 'types',
				   
				   success: function(response) 
				   {
				        var obj = Ext.decode(response.responseText);
				        
				        /**
				         * Take all available chart types and add them to the chart type combo's 
				         * store in ascending alphabetical order.
				         * 
				         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				         */
				        chartTypesFromService = obj.types.sort();
				        
				        for (i=0; i<chartTypesFromService.length; i++)
			        	{				        	
				        	/**
			        		 * Capitalize only the first letter for displaying the charts type name.
			        		 */
			        		var chartTypeDisplay = chartTypesFromService[i].charAt(0).toUpperCase() + chartTypesFromService[i].slice(1);
			        		
			        		chartTypesStore.add
			        		(
		        				{
		        					"chartType": 		chartTypeDisplay,
		        					"chartTypeAbbr": 	chartTypesFromService[i].toLowerCase(),
		        					"iconChartType": 	Sbi.chart.designer.Designer.relativePathReturn + 
					        								'/img/designer/chart/types/' + 
					        									chartTypesFromService[i].toLowerCase() +
					        										'.png'
								}
	        				);			        		
			        	}
				        
				        globalThis.chartTypeSelector.fireEvent("chartTypesReady");
				        
				    },
				    
				    failure: function(response, opts) 
				    {
				    	console.log('server-side failure with status code ' + response.status);
				    }
				});
			
			/**
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			this.chartTypeSelector = Ext.create('Sbi.chart.designer.ChartTypeSelector_2', 
			{ 				
				region: 'north',
 				
				listeners:
 				{
					chartTypesReady: function()
					{
						this.setStore(chartTypesStore);
						this.fireEvent("setInitialIcon");						
					},
					
					/**
					 * Listen for the moment in which the item is rendered (this one)
					 * so to be able to customize the combo properly (icon of the chart
					 * type + name of the chart type). This will happen on the load of 
					 * the document inside of the Designer - combo box for chart type 
					 * is set to it's initial state (the icon of chart type of the 
					 * current document and it's name).
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
 					afterrender: function(chartTypeOfDocument)
 					{
 						var iconPath = "";
 						
 						for (i=0; i<this.store.data.length; i++)
						{
 							if (this.store.data.items[i].data.chartTypeAbbr.toLowerCase() == chartTypeOfDocument.value.toLowerCase())
							{
 								iconPath = this.store.data.items[i].data.icon;
 								break;
							}
						}
 					
	 					this.inputEl.setStyle
	 		            (
 		            		{
 				                "height": "35px",
 		            			'background-image': 	'url('+iconPath+')',
 				                'background-repeat': 	'no-repeat',
 				                'background-position': 	'left 2px center',
 				                'padding-left': 		'40px', 
 				                'background-size': 		"30px 30px"	            
 		            		}
 		        		);	
 					}
 				}	
 			});
			
			var onSelectJsonTemplate = "";					
			
			this.chartTypeSelector.on
			(
				"chartTypeChanged", 
				
				function() 
				{
					Sbi.chart.designer.Designer.chartTypeChanged = true;
					
					/**
					 * When the chart type is changed, remove the content from the Preview panel
					 * so it will not be confused with the old image that was potentially rendered
					 * within this panel in the Designer for some previous chart configuration and
					 * potentially another chart type.
					 * 
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					globalThis.previewPanel.removeAll();
				}
			);

			/**
			 * Listener for the 'rowclick' event that happens when we change the chart type
			 * on the left part of the Designer page (from the chart type picker). 
			 * (danristo :: danilo.ristovski@mht.net) 
			 */			
			this.chartTypeSelector.on
			(
				"resetStep2",
				
				function() 
				{	
					/**
					 * Get the main configuration panel (the one on the top of the Step 2 tab of the Designer page)
					 * and the second configuration panel (everything under the main panel).
					 */					
					var mainConfigurationPanel = globalThis.stepsTabPanel.getComponent(1).getComponent(0);
					var secondConfigurationPanel = globalThis.stepsTabPanel.getComponent(1).getComponent(1);	
					
					var chartLegendCheckBox = mainConfigurationPanel.getComponent("showLegend");
//					var chartOrientation = mainConfigurationPanel.getComponent("fieldContainer1").getComponent("chartOrientationCombo");
//					var chartWidth = mainConfigurationPanel.getComponent("fieldContainer1").getComponent("chartWidthNumberfield");	
					var chartOrientation = mainConfigurationPanel.getComponent("chartOrientationCombo");
					var chartWidth = mainConfigurationPanel.getComponent("chartWidthNumberfield");	
					
					/**
					 * The main configuration panel element (opacity on mouse over) to show
					 * on the Step 2 main configuration panel when the SUNBURST is selected.
					 */
					var opacityOnMouseOver = mainConfigurationPanel.getComponent("opacityMouseOver");
					
					/**
					 * "Show table" checkbox for the PARALLEL chart serves as a indicator of
					 * whether the PARALLEL table should be shown when rendering the chart.
					 * This checkbox is positioned in the Generic configuration panel on the
					 * Cofiguration tab of the Designer.
					 */
					var showTableParallel = mainConfigurationPanel.getComponent("showTableParallel");
					
					/**
					 * The additional second configuration panel elements to show when the SUNBURST is selected.
					 */
					var colorPalette = secondConfigurationPanel.getComponent("chartColorPalette");
					var chartLegend = secondConfigurationPanel.getComponent("chartLegend");	
//					var toolbarAndTip = secondConfigurationPanel.getComponent("chartToolbarAndTip");
					var sunburstToolbar = secondConfigurationPanel.getComponent("chartToolbar");
					var sunburstTip = secondConfigurationPanel.getComponent("chartTip");

					/**
					 * The additional second configuration panel element to show when the WORDCLOUD is selected.
					 */
					var wordCloudPanel = secondConfigurationPanel.getComponent("wordcloudConfiguration");
					
					/**
					 * The additional second configuration panel elements to show when the PARALLEL is selected.
					 */
					var parallelLimitPanel = secondConfigurationPanel.getComponent("chartParallelLimit");
					var parallelAxesLinesPanel = secondConfigurationPanel.getComponent("chartParallelAxesLines");					
					var parallelTooltipPanel = secondConfigurationPanel.getComponent("chartParallelTooltip");
					var parallelLegendTitlePanel = secondConfigurationPanel.getComponent("chartParallelLegendTitle");
					var parallelLegendElementPanel = secondConfigurationPanel.getComponent("chartParallelLegendElement");
					
					/**
					 * The second configuration panel element for hiding/showing when the SCATTER is selected.
					 */
					var scatterConfiguration = secondConfigurationPanel.getComponent("chartScatterConfiguration");
					
					/**
					 * The additional second configuration panel element to show when the HEATMAP is selected.
					 */
//					var showLegendAndTooltip = secondConfigurationPanel.getComponent("chartHeatmapLegendAndTooltip");
					var showHeatmapLegend = secondConfigurationPanel.getComponent("chartHeatmapLegend");
					var showHeatmapTooltip = secondConfigurationPanel.getComponent("chartHeatmapTooltip");
					
					/**
					 * The additional second configuration panel element to show when the GAUGE is selected.
					 */
					var gaugePanePanel = secondConfigurationPanel.getComponent("gaugePaneConfiguration");
									
					/**
					 * Determine which is the newly chosen chart type in order to show/hide
					 * suitable GUI elements on the Step 2 (and Step 1, only for the GAUGE
					 * chart type).
					 */
					var currentChartType = globalThis.chartTypeSelector.getValue().toUpperCase();
					
					var isChartSunburst = currentChartType == 'SUNBURST';
					var isChartWordCloud = currentChartType == 'WORDCLOUD';	
					var isChartPie = currentChartType == 'PIE';	
					var isChartTreemap = currentChartType == 'TREEMAP';
					var isChartParallel = currentChartType == 'PARALLEL';					
					var isChartScatter = currentChartType == 'SCATTER';		
					var isChartRadar= currentChartType == 'RADAR';
					var isChartHeatmap = currentChartType == 'HEATMAP';	
					var isChartChord = currentChartType == 'CHORD';	
					var isChartGauge = currentChartType == 'GAUGE';	
					
					var chartLibrary = globalThis.chartLibNamesConfig[currentChartType.toLowerCase()];
					
					/**
					 * Show/hide the legend check box (show/hide the legend) on the 
					 * main configuration panel on the Step 2 tab of the Designer page.
					 */
					if (isChartSunburst || isChartWordCloud  || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge 
								|| isChartChord) {	
						chartLegendCheckBox.hide();
					} else {
						chartLegendCheckBox.show();
					}
					
					/**
					 * Show/hide the legend panel on the second configuration panel on the 
					 * Step 2 tab of the Designer page.
					 */
					if (isChartSunburst || isChartWordCloud || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge
								|| isChartChord) {
						chartLegend.hide();
					} else {
						chartLegend.show();
					}
					
					/**
					 * Show/hide the orientation combo box on the main configuration panel
					 * on the Step 2 tab of the Designer page.
					 */
					if ((isChartSunburst || isChartWordCloud || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge 
								|| isChartChord || isChartPie || isChartRadar 
									|| isChartScatter)
							|| chartLibrary == 'chartJs'){
						chartOrientation.hide();
					} else {
						chartOrientation.show();
					}
					
					/**
					 * Show/hide combo box for the width of the chart on the main configuration 
					 * panel on the Step 2 tab of the Designer page.
					 */
					if (isChartSunburst) {
						chartWidth.hide();
					} else {
						chartWidth.show();
					}
					
					/**
					 * Show/hide the number field that servers for specifying a value for the 
					 * opacity on mouse over on the main configuration panel on the Step 2 tab 
					 * of the Designer page.
					 */
					if (isChartSunburst) {
						opacityOnMouseOver.show();
					} else {
						opacityOnMouseOver.hide();
					}
					
					/**
					 * Show/hide the color pallete on the second configuration panel on the 
					 * Step 2 tab of the Designer page.
					 */					
//					if (isChartWordCloud || isChartGauge) {	
					if (isChartWordCloud) {						
						colorPalette.hide();
					} else {						
						colorPalette.show();
					}
					
					/**
					 * Show/hide the toolbar and tip panel on the second configuration panel 
					 * on the Step 2 tab of the Designer page.
					 */
					if (isChartSunburst) {
						sunburstToolbar.show();
						sunburstTip.show();
					} else  {
						sunburstToolbar.hide();
						sunburstTip.hide();
					}
					
					/**
					 * Show/hide the customization panel for the WORDCLOUD chart parameters on the 
					 * second configuration panel on the Step 2 tab of the Designer page.
					 */
					if (isChartWordCloud) {
						wordCloudPanel.show();
					} else {
						wordCloudPanel.hide();
					}
					
					/**
					 * Show/hide panels dedicated to the PARALLEL chart type on the second 
					 * configuration panel on the Step 2 tab  of the Designer page.
					 */
					if (isChartParallel) {
						parallelLimitPanel.show();
						parallelAxesLinesPanel.show();
						parallelTooltipPanel.show();
						parallelLegendTitlePanel.show();
						parallelLegendElementPanel.show();
						showTableParallel.show();
					} else {
						parallelLimitPanel.hide();
						parallelAxesLinesPanel.hide();
						parallelTooltipPanel.hide();
						parallelLegendTitlePanel.hide();
						parallelLegendElementPanel.hide();
						showTableParallel.hide();
					}
					/**
					 * CHORD chart uses parallel tooltip panel
					 */
					if(isChartChord){
						parallelTooltipPanel.show();
					}
					
					/**
					 * Show/hide the SCATTER configuration panel on the second 
					 * configuration panel on the Step 2 tab of the Designer page.
					 */
					if (isChartScatter) {
						scatterConfiguration.show();
					} else {
						scatterConfiguration.hide();
					}
					
					/**
					 * Show/hide the HEATMAP's legend and tooltip panel on the second
					 * configuration panel on the Step 2 tab of the Designer page.
					 */
					if (isChartHeatmap) {
						showHeatmapLegend.show();
						showHeatmapTooltip.show();
					} else {
						showHeatmapLegend.hide();
						showHeatmapTooltip.hide();
					}
					
					/**
					 * Show/hide pane panel of the GAUGE chart type on the second configuration 
					 * panel on the Step 2 tab of the Designer page. Show/hide the bottom X-axis
					 * panel on the Step 1 tab of the Designer page.
					 */
					if (isChartGauge) {
						globalThis.bottomXAxisesPanel.hide();
						gaugePanePanel.show();
					}
					else
					{
						globalThis.bottomXAxisesPanel.show();
						gaugePanePanel.hide();
					}

					/**
					 * This is JSON template that we take form the Advance editor (previously, Step 3)
					 * so we can be up-to-date with current structure of the document inside the Designer.
					 */
					var jsonTemplateAdvancedEditor = Sbi.chart.designer.Designer.exportAsJson();	
									
					/**
			    	 * Remove unwanted properties from the JSON template that we are about to apply to
			    	 * the current chart document structure via "removeUnwantedPropsFromJsonStyle" 
			    	 * static function.
			    	 * 
			    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			    	 */
					
					/**
					 * If there is not a single item in the Y-axis panel then skip the VALUES tag
					 * from the XML file of the selected style since we do not have any serie on
					 * which we should apply the style and further more, this is the way in which 
					 * we are skipping unwanted appending (adding) to the template of the document
					 * something that is not even there - a serie item. If this is not done for this
					 * case the ghost (empty, fake) serie item will appear in the Y-axis after changing
					 */
					var yAxisListIsEmpty = (Sbi.chart.designer.ChartUtils.getSeriesDataAsOriginalJson().length == 0) ? true : false;
										
					/**
					 * Since we are dealing with the newly created chart which does not posses any 
					 * serie item, in order to skip mergin the OBJECT (not an empty array!) in the 
					 * SERIE tag of the source parameter with the target that does not have it 
					 * (SERIE tag), we will remove the VALUES tag that contains mentioned one so we 
					 * skip appearance of the ghost serie item after mergin. Parameter that tells 
					 * the method that is called that we should remove this tag is "yAxisListIsEmpty".
					 */
					var configurationForStyleGeneric = 	
						
						Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle
						(
							Designer.getConfigurationForStyle(Designer.styleName,true).generic,
							yAxisListIsEmpty
						);	
					
					var configurationForStyleSpecific = 
						
						Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle
						(
							Designer.getConfigurationForStyle(Designer.styleName,true)[currentChartType.toLowerCase()],
							yAxisListIsEmpty
						);	
					
					var localJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects
					(
						jsonTemplateAdvancedEditor,
						configurationForStyleGeneric,
						configApplyAxes
					);
					
					localJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects
					(
						localJsonTemplate, 
						configurationForStyleSpecific,
						configApplyAxes
					);							
					
					jsonTemplate = localJsonTemplate;				
					
					/**
					 * When user selects (changes) chart type we should check if there are 
					 * labels for color fields/pickers to update (show/hide) flags for 
					 * mandatory fields.
					 */
					removeFlagsForMandatoryFields(currentChartType,jsonTemplate);
										
					/**
					 * When changing the chart style, the one is going to be applied to the
					 * current (new) template of the document. Hence, we need to send information
					 * about the current number of color in the color palette (customizable by style 
					 * files) towards the color palette panel (palette (color grid) container) in 
					 * order to update its layout on the Configuration tab (prev. Step 2).
					 * 
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					var numberOfColors = (jsonTemplate.CHART.COLORPALETTE.COLOR) ? jsonTemplate.CHART.COLORPALETTE.COLOR.length : 0;					
					
					//Ext.getCmp("chartColorPallete").fireEvent("chartTypeChanged",numberOfColors);
					
					Ext.getCmp("chartColorPalette").height = (numberOfColors+1)*20+65;
					Ext.getCmp("chartColorPalette").update();
					
					/**
					 * Update (refresh) the main configuration panel (the one on the top of 
					 * the Step 2 tab) after selecting the particular style.
					 */
		    		Sbi.chart.designer.Designer.update(jsonTemplate);	
				}
			);
						
			/**
			 * Set the initial chart type (the chart type of the document that we load into the Designer).
			 */
			this.chartTypeSelector.setChartType(this.chartType);			
			
			// Store that contains the data about SERIE column(s) for the chart (danilo.ristovski@mht.net)
			this.columnsPickerStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
 				data: [],
 				id: "axisesContainerStore",
 				sorters: [{
 					property: 'serieColumn',
 					direction: 'ASC'
 				}],
 				
 				listeners: {
 					/**
 					 * When we get the necessary data populate this store 
 					 * (commented by: danilo.ristovski@mht.net) 
 					 */
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
 						
 						/**
 	 					 * Set the 'data' attribute of the store
 	 					 * (commented by: danilo.ristovski@mht.net) 
 	 					 */
 		  				this.setData(theData);
 		  			}
 				}
 			});
			
			/** 
			 * This store serves for keeping data about series existing inside 
			 * of the Y-axis panel at the moment, so the PARALLEL chart 'Limit'
			 * panel's combo box for "Serie as filter column" on the Configuration 
			 * tab (previously Step 2) can be up-to-date about available serie
			 * items. This store is used inside of the ChartConfigurationParallelLimit
			 * file (panel). ID of the panel: "chartParallelLimit".
			 *   
			 * @author: danristo (danilo.ristovski@mht.net)
			 */			
			this.seriesBeforeDropStore = Ext.create("Ext.data.Store", {
				id:"storeForSeriesBeforeDrop", 
				
				fields: [{
					name: 'seriesColumn'
				}]
			});
			
			var columnsPickerStore = this.columnsPickerStore;
			
			/** 
			 * Store that contains the data about CATEGORY column(s) for the chart
			 * (commented by: danilo.ristovski@mht.net) 
			 */
			this.categoriesPickerStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
 				data: [],
 				
 				sorters: [{
 					property: 'categoryColumn',
 					direction: 'ASC'
 				}],
 				
 				listeners: {
 					dataReady: function(jsonData) {
 		  				var jsonDataObj = Ext.JSON.decode(jsonData);
 						var theData = [];
 						
 		  				Ext.each(jsonDataObj.results, function(field, index) { 		   		  					
 		  					if(field != 'recNo' && field.nature == 'attribute') {
 		  						theData.push({
 		  							categoryColumn : field.alias,
 		  							/**
 		  							 * ('categoryDataType' added by: danilo.ristovski@mht.net) 
 		  							 */
 		  							categoryDataType: field.colType 
 		  						});
 		  					}
 		  				});
 		  				
 		  				this.setData(theData);
 		  			}
 				}
 			});
			
			var categoriesPickerStore = this.categoriesPickerStore;
			
			/** 
			 * Fires aforementioned 'dataReady' event that is catch in axis creation (attributes and measure)
			 * (commented by: danilo.ristovski@mht.net) 
			 */
  			this.chartServiceManager.run('loadDatasetFields', {}, [datasetLabel], function (response) {
  				columnsPickerStore.fireEvent('dataReady', response.responseText);
  				categoriesPickerStore.fireEvent('dataReady', response.responseText);
			});

			this.columnsPicker = Ext.create('Sbi.chart.designer.AxisesPicker', {
  				region: 'center',
  				store: columnsPickerStore,
  				
  				/**
  				 * Hide non-collapsible header so the collapsible one defined 
  				 * inside of the AxisesPicker class (file) can be rendered 
  				 * only.
  				 * 
  				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  				 */
  				hideHeaders: true, 
  				title: LN('sbi.chartengine.designer.measureslist'),
  				
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
			
			/** 
			 * Type of the 'this.categoriesPicker' is the grid panel.
			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			this.categoriesPicker = Ext.create('Sbi.chart.designer.AxisesPicker', {
  				region: 'south',
  				flex: 1,
  				//margin: '0 0 5 0',
  				store: categoriesPickerStore,

  				/**
  				 * Hide non-collapsible header so the collapsible one defined 
  				 * inside of the AxisesPicker class (file) can be rendered 
  				 * only.  				 
  				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  				 */
  				hideHeaders: true,  				
  				title: LN('sbi.chartengine.designer.attributeslist'),
  				
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
			
			var allStyleNames = function() {
				var allStyles=[];
				var styles=JSON.parse(Sbi.chart.designer.Styles);			    

				/**
				 * For the problem of inability of loading all styles available on
				 * the server inside of the Cockpit engine we will introduce this
				 * checking of all styles available. Since we get value of NULL
				 * when the Designer is rendered from the Cockpit engine, we will
				 * provide a fake styles array of zero length, just in order to 
				 * skip the error that appears when trying to get the 'length' of
				 * object which value is NULL. Outcome: Designer page can be loaded
				 * from the Cockpit engine, but with the empty chart styles combo.
				 * 
				 * @author: danristo (danilo.ristovski@mht.net)
				 */
				if (!(styles && styles!=null && styles.length>0)) {
					styles = [];
				}

				for(i=0; i < styles.length; i++) {
					style = {
						styleAbbr: styles[i].STYLE.name, 
						style: styles[i].STYLE.name
					};
					
					if (jsonTemplate.CHART.styleName === styles[i].STYLE.name)
					{
						Sbi.chart.designer.Designer.jsonTemplateStyleExists = true;
					}
					
					if (styles[i].STYLE.name !== "sfnas") {						
					allStyles.push(style);
				}
				}

				return allStyles;
			};
			
			var allStyles = allStyleNames();
			
			var styleStore = Ext.create("Ext.data.Store", {
				fields : [ "styleAbbr", "style" ],

				data : allStyles
			});					
			
			/**
			 * Manage text of labels for color elements of PARALLEL chart on the Step 2.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var removeFlagsForMandatoryFields = function(chartType,jsonTemplate)
			{				
				if (chartType=="PARALLEL")
				{
					var axesListStyle = Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.AXES_LIST.style);
							
					var axisColorLabel = "";
					var brushColorLabel = "";
					
					if (axesListStyle.axisColor && axesListStyle.axisColor!="" && axesListStyle.axisColor!="transparent")
					{
						axisColorLabel = LN("sbi.chartengine.configuration.parallel.axesLines.axisColor") + ":";
					}
					else
					{
						axisColorLabel = LN("sbi.chartengine.configuration.parallel.axesLines.axisColor") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":";						
					}	
					
//					if (Ext.getCmp("chartParallelAxesLines").colorPickerAxisColor.items.items[0].labelEl)
//						Ext.getCmp("chartParallelAxesLines").colorPickerAxisColor.items.items[0].labelEl.update(axisColorLabel);	
//					else if (Ext.getCmp("chartParallelAxesLines").colorPickerAxisColor.items.items[0].fieldLabel)
//						Ext.getCmp("chartParallelAxesLines").colorPickerAxisColor.items.items[0].fieldLabel = axisColorLabel;
					if(Ext.getCmp("chartParallelAxesLines") && Ext.getCmp("chartParallelAxesLines").colorPickerAxisColor) {
						Ext.getCmp("chartParallelAxesLines").colorPickerAxisColor.setFieldLabel(axisColorLabel);	
					}
						
					if (axesListStyle.brushColor && axesListStyle.brushColor != "" && axesListStyle.brushColor != "transparent") {
						brushColorLabel = LN("sbi.chartengine.configuration.parallel.axesLines.brushColor") + ":";						
					} else {
						brushColorLabel = LN("sbi.chartengine.configuration.parallel.axesLines.brushColor") 
							+ Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":";
//						Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor.items.items[0].fieldLabel = brushColorLabel;
					}
					
//					if (Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor.items.items[0].labelEl)
//						Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor.items.items[0].labelEl.update(brushColorLabel);
//					else if (Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor.items.items[0].fieldLabel)
//						Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor.items.items[0].fieldLabel = brushColorLabel;
					if(Ext.getCmp("chartParallelAxesLines") && Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor) {
						Ext.getCmp("chartParallelAxesLines").colorPickerBrushColor.setFieldLabel(brushColorLabel);	
					}
				}
				
				else if (chartType=="SUNBURST")
				{					
					var sunburstToolbarStyle = Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.TOOLBAR.style);
					
					var percFontColorLabel = "";
					
					if (sunburstToolbarStyle.percFontColor && sunburstToolbarStyle.percFontColor!="" && sunburstToolbarStyle.percFontColor!="transparent")
					{
						percFontColorLabel = LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor') + ":";
					}
					else
					{
						percFontColorLabel = LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":";
					}
					
//					if (Ext.getCmp("chartToolbar").colorPicker.items.items[0].labelEl)
//						Ext.getCmp("chartToolbar").colorPicker.items.items[0].labelEl.update(percFontColorLabel);	
//					else if (Ext.getCmp("chartToolbar").colorPicker.items.items[0].fieldLabel)
//						Ext.getCmp("chartToolbar").colorPicker.items.items[0].fieldLabel = percFontColorLabel;
					if(Ext.getCmp("chartToolbar") && Ext.getCmp("chartToolbar").colorPicker) {
						Ext.getCmp("chartToolbar").colorPicker.setFieldLabel(percFontColorLabel);	
					}
					
					var sunburstTipStyle = Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.TIP.style);
					
					var tipFontColorLabel = "";
					
					if (sunburstTipStyle.color && sunburstTipStyle.color!="" && sunburstTipStyle.color!="transparent")
					{
						tipFontColorLabel = LN('sbi.chartengine.configuration.sunburst.tip.fontColor') + ":";
					}
					else
					{
						tipFontColorLabel = LN('sbi.chartengine.configuration.sunburst.tip.fontColor') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":";
					}
					
//					if (Ext.getCmp("chartTip").colorPicker.items.items[0].labelEl)
//						Ext.getCmp("chartTip").colorPicker.items.items[0].labelEl.update(tipFontColorLabel);	
//					else if (Ext.getCmp("chartTip").colorPicker.items.items[0].fieldLabel)
//						Ext.getCmp("chartTip").colorPicker.items.items[0].fieldLabel = tipFontColorLabel;
					if(Ext.getCmp("chartTip") && Ext.getCmp("chartTip").colorPicker) {
						Ext.getCmp("chartTip").colorPicker.setFieldLabel(tipFontColorLabel);	
					}
				}
				
				else if (chartType=="HEATMAP")
				{
					var heatmapTooltipStyle = Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.TOOLTIP.style);
					
					var fontColorHeatmapTooltipLabel = "";
					
					if (heatmapTooltipStyle.color && heatmapTooltipStyle.color!="" && heatmapTooltipStyle.color!="transparent")
					{
						fontColorHeatmapTooltipLabel = LN('sbi.chartengine.configuration.color') + ":";
					}
					else
					{
						fontColorHeatmapTooltipLabel = LN('sbi.chartengine.configuration.color') + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":";
					}
					
//					if (Ext.getCmp("chartHeatmapTooltip").colorPicker.items.items[0].labelEl)
//						Ext.getCmp("chartHeatmapTooltip").colorPicker.items.items[0].labelEl.update(fontColorHeatmapTooltipLabel);	
//					else if (Ext.getCmp("chartHeatmapTooltip").colorPicker.items.items[0].fieldLabel)
//						Ext.getCmp("chartHeatmapTooltip").colorPicker.items.items[0].fieldLabel = fontColorHeatmapTooltipLabel;
					if(Ext.getCmp("chartHeatmapTooltip") && Ext.getCmp("chartHeatmapTooltip").colorPicker) {
						Ext.getCmp("chartHeatmapTooltip").colorPicker.setFieldLabel(fontColorHeatmapTooltipLabel);	
					}
				}
			};
			
			/**
			 * GUI label element that will be placed immediatelly above the style combo box
			 * (on the top of the left panel on the Designer page).
			 * (danilo.ristovski@mht.net)
			 */
			this.styleLabel = Ext.create ('Ext.form.Label', {
		        forId: 'stylePickerComboId',
		        text: LN('sbi.chartengine.designer.styleforparameters'),
//			        margin: '5 3 3 0'
			});
			
			/**
			 * Combo box for defining the style for the generic customizable parameters (properties)
			 * of the chart.
			 * (danilo.ristovski@mht.net)
			 * (lazar.kostic@mht.net)
			 */		
			this.stylePickerCombo = Ext.create ("Ext.form.ComboBox", {
			    store: styleStore,
			    id: "stylePickerComboId",
			    queryMode: 'local',
			    displayField: 'style',
			    valueField: 'styleAbbr',
			    value:  jsonTemplate.CHART.styleName,
			    editable: false,
			    // top,right,bottom,left
			    padding: "5 0 5 0",
			    width: Sbi.settings.chart.leftDesignerContainer.widthPercentageOfItem,
			    
			    listConfig: {
			    	listeners: {
				    	itemclick: function(combo,k) {
				    						    		
				    		/**
							 * Depending on the style that we choose for the document's generic
							 * customizable parameters (Red, Green, Blue, ... style), take the
							 * predefined JSON structure that is defined for that newly chosen 
							 * style. This part is needed for later merging of the templates 
							 */
				    		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();	
			    		

							/**
							 * Reset (refresh, modify) the 'styleName' field of the Designer, also
							 */
							Designer.styleName = k.data.styleAbbr;
							
							/**
							 * Reset the JSON template for the document (chart) after changing the 
							 * previously selected style (changing the selected item of the combo)
							 */
							
							/**
							 * Set the predefined values for specific parameters of different chart types.
							 */								
							
							var chartTypeToLowerCase = chartType.toLowerCase();
							
							/**
							 * This is JSON template that we take form the Advance editor (previously, Step 3)
							 * so we can be up-to-date with current structure of the document inside the Designer.
							 */
							var jsonTemplateAdvancedEditor = Sbi.chart.designer.Designer.exportAsJson();	
							
							/**
							 * ------------------
							 * For GAUGE chart:
							 * ------------------
							 * Remove PLOTBANDS tag from current JSON template (jsonTemplateAdvancedEditor) in
							 * order to provide possibility for removal of it when newly picked chart style does
							 * not have this tag defined in its  XML template. 
							 * 
							 * Example: (1) pick style with PLOTBANDS; (2) pick style without them; (3) when 
							 * merging specific part of the XML template that does not have PLOTABANDS, with
							 * the one that has, we will keep them in the final XML template. Hence, the best
							 * way is to provide a workaround - removal of it.
							 * 
							 * @author: danristo (danilo.ristovski@mht.net)
							 */ 
//							if (chartType == "GAUGE")
//							{
//								var numberOfYAxis = jsonTemplateAdvancedEditor.CHART.AXES_LIST.AXIS.length;
//								
//								for (i=0; i<numberOfYAxis; i++)
//								{
//									if (jsonTemplateAdvancedEditor.CHART.AXES_LIST.AXIS[i].PLOTBANDS)
//									{
//										jsonTemplateAdvancedEditor.CHART.AXES_LIST.AXIS[i].PLOTBANDS = undefined;
//									}
//								}									
//							}
							
							/**
					    	 * Remove unwanted properties from the JSON template that we are about to apply to
					    	 * the current chart document structure via "removeUnwantedPropsFromJsonStyle" 
					    	 * static function.
					    	 * 
					    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					    	 */
							
							var yAxisListIsEmpty = (Sbi.chart.designer.ChartUtils.getSeriesDataAsOriginalJson().length == 0) ? true : false;
							
							var configurationForStyleGeneric = 	
								
								Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle
								(
									Designer.getConfigurationForStyle(k.data.styleAbbr).generic,
									yAxisListIsEmpty
								);
							
							var configurationForStyleSpecific = 	
								
								Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle
								(
									Designer.getConfigurationForStyle(k.data.styleAbbr)[chartTypeToLowerCase],
									yAxisListIsEmpty
								);
							
//							var configurationForStyleGeneric = Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle(Designer.getConfigurationForStyle(k.data.styleAbbr).generic);	
//							var configurationForStyleSpecific = Sbi.chart.designer.ChartUtils.removeUnwantedPropsFromJsonStyle(Designer.getConfigurationForStyle(k.data.styleAbbr)[chartTypeToLowerCase]);	
							
							var localJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects
							(
								jsonTemplateAdvancedEditor,
								configurationForStyleGeneric,
								configApplyAxes
							);
							
							localJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects
							(
								localJsonTemplate, 
								configurationForStyleSpecific, 
								configApplyAxes
							);							
							
							jsonTemplate = localJsonTemplate;
							
							/**
				    		 * If current chart is of type PARALLEL, when changing the chart's style
				    		 * it is needed for combobox (Limit panel on the Step 2 of the Designer) 
				    		 * that containes serie items for filtering to be cleaned from previous 
				    		 * content in order to contain up-to-date serie items available inside of 
				    		 * the Y-axis panel on the Step 1.
				    		 */
				    		if (chartType == "PARALLEL" || chartType == "SUNBURST" || chartType == "HEATMAP")
			    			{
				    			if (chartType == "PARALLEL")
				    			{
				    				Ext.getCmp("chartParallelLimit").seriesColumnsOnYAxisCombo.getStore().removeAll();
				    			}
				    			
				    			removeFlagsForMandatoryFields(chartType,jsonTemplate);
			    			}
							
				    		/**
							 * When changing the chart type, the current style is going to be applied to the
							 * current (new) template of the document. Hence, we need to send information
							 * about the current number of color in the color palette (customizable by style 
							 * files) towards the color palette panel (palette (color grid) container) in 
							 * order to update its layout on the Configuration tab (prev. Step 2).
							 * 
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */	
							var numberOfColors = jsonTemplate.CHART.COLORPALETTE ? jsonTemplate.CHART.COLORPALETTE.COLOR.length : 0;
							Ext.getCmp("chartColorPalette").fireEvent("chartTypeChanged", numberOfColors);
							Ext.getCmp("chartColorPalette").height = (numberOfColors+1)*20+65;
														
							Ext.getCmp("chartColorPalette").update();
							
							/**
							 * Update (refresh) the main configuration panel (the one on the top of 
							 * the Step 2 tab) after selecting the particular style.
							 */
				    		Sbi.chart.designer.Designer.update(jsonTemplate);				    		
				    	}
			    	}
			    }
			});
			
			this.chartTypeColumnSelector = Ext.create('Sbi.chart.designer.ChartTypeColumnSelector', {
				styleLabel2: this.styleLabel2,
				chartTypeSelector: this.chartTypeSelector,
  				columnsPicker: this.columnsPicker,
  				stylePickerCombo: this.stylePickerCombo,
  				styleLabel: this.styleLabel,
  				categoriesPicker: this.categoriesPicker,
  				region: 'west'
  			});			

			/**
			 * When the left container (panel) on the Designer page is resized, fire events
			 * towards attribute/measure container panels so they can update their with.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			this.chartTypeColumnSelector.on
			(
				"resize",
				
				function()
				{
					globalThis.columnsPicker.fireEvent("updateWidth");
					globalThis.categoriesPicker.fireEvent("updateWidth");
				}
			);
			
			var chartExportWebServiceManager = this.chartExportWebServiceManager;
			var chartServiceManager = this.chartServiceManager;
			
			// Creating step 1 panel
			/**
  			 * The height of the Preview panel in the Designer page is constant (fixed) and
  			 * it is of the same heigh of the height of the Y-axis panel(s).
  			 * 
  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  			 */
  			this.previewPanel = Ext.create('Ext.panel.Panel', {
  				id: 'previewPanel',
  				
  				/**
  				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  				 */
  				//minHeight: 330,
  				height: Sbi.settings.chart.structureStep.heightYAxisAndPreviewPanels,
  				
  				title: LN('sbi.chartengine.preview'),
  				
  				/**
  				 * Old value was "center".
  				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  				 */
  				titleAlign: 'left',
  				
  				tools:[],
  				layout : {
  				    type  : 'hbox',
  				    pack  : 'center'
  				}
  				
  			});
  			
  			/**
  			 * The 'srcImg' will be the global variable that will serve as a the container
  			 * of the URL to the image that will be shown inside the Preview panel. We will
  			 * you this inside the handler (listener) of the 'resize' event of the Preview
  			 * panel.
  			 *  
  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  			 */			
			var srcImg = null;
  			
			/**
			 * Handles the resizing of the Preview panel that lies within the Designer page.
			 * 
			 * @author Daniele Davì
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
  			this.previewPanel.on
  			(
				"resize", 
				
				function()
				{
					/**
					 * Take the calculation of the resizing if the image is already rendered.
					 */
					if (srcImg && srcImg!=null)
					{
//						console.log(widthChartJson);
//						console.log(heightChartJson);
//						
//						console.log(heightDimType);
//						console.log(widthDimType);
//						
//						var heightChartJsonTemp = null;
//						var widthChartJsonTemp = null;
//							  							
//						if (heightDimType == "percentage")
//						{  								
//							if (!heightChartJson || heightChartJson == "")
//							{
//								heightChartJsonTemp = window.innerHeight;
//							}
//							else
//							{
//								heightChartJsonTemp = window.innerHeight*(Number(heightChartJson)/100);
//							}
//						}
//						else
//						{
//							heightChartJsonTemp = (!heightChartJson || heightChartJson=="") ? window.innerHeight : heightChartJson;
//						}
//							
//						if (widthDimType == "percentage")
//						{
//							if (!widthChartJson || widthChartJson == "")
//							{
//								widthChartJsonTemp = window.innerWidth;
//							}
//							else
//							{
//								widthChartJsonTemp = window.innerWidth*(Number(widthChartJson)/100);
//							}
//						}
//						else
//						{
//							widthChartJsonTemp = (!widthChartJson || widthChartJson=="") ? window.innerWidth : widthChartJson;
//						}
//						
//						var ratioChartJson = widthChartJsonTemp/heightChartJsonTemp;
//						
//						var heightPrevPan = globalThis.previewPanel.getHeight();
//						var widthPrevPan = globalThis.previewPanel.getWidth();
//						
//						var widthImg = 0;
//						var heightImg = 0;
//						
//						widthImg = (widthChartJsonTemp <= widthPrevPan) ? widthChartJsonTemp : widthPrevPan;
//						heightImg = widthImg/ratioChartJson;
//						
//						var ratioImg = widthImg/heightImg;
//						
//						console.log(ratioImg);
//						
//						heightImg = (heightImg <= heightPrevPan) ? heightImg : heightPrevPan;
//						widthImg = heightImg*ratioImg;
//						console.log(Ext.getCmp("AAADDD"));
//						/**
//						 * Call the method that sets the rendered image inside the Preview panel.
//						 */
//  						setPreviewImage(srcImg,heightImg,widthImg);
//  						console.log("USAO resize");
//  						console.log(previewPanel);
//  						Ext.getCmp("AAADDD").getEl().fireEvent('click');
						
						var sbiJson = Sbi.chart.designer.Designer.exportAsJson(true); 
						
							/**
				  			 * Code that serves for the calculation of the size of the image that is going to
				  			 * be rendered and displayed inside the Preview panel of the Designer. Calculation
				  			 * is based on the current dimensions of the Preview panel and on the dimensions of
				  			 * the chart (document, template) that is set by the user (even if the dimensions
				  			 * (chart's height and width) are not set by the user - their values will be taken
				  			 * from respective dimensions of the entire window). 
				  			 * 
				  			 * @author Daniele Davì
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				  			 */
							heightChartJson = sbiJson.CHART.height;
							widthChartJson = sbiJson.CHART.width;
							
							heightDimType = sbiJson.CHART.heightDimType;
							widthDimType = sbiJson.CHART.widthDimType;
							  							
							if (heightDimType == "percentage")
						{  								
								if (!heightChartJson || heightChartJson == "")
								{
									sbiJson.CHART.height = window.innerHeight;
//									heightChartJson = window.innerHeight;
								}
								else
							{
									sbiJson.CHART.height = window.innerHeight*(Number(heightChartJson)/100);
//									heightChartJson = window.innerHeight*(Number(heightChartJson)/100);
							}
						}
							else
						{
								sbiJson.CHART.height = (!heightChartJson || heightChartJson=="") ? window.innerHeight : heightChartJson;
//								heightChartJson = (!heightChartJson || heightChartJson=="") ? window.innerHeight : heightChartJson;
						}
							
							if (widthDimType == "percentage")
						{
								if (!widthChartJson || widthChartJson == "")
								{
									sbiJson.CHART.width = window.innerWidth;
//									widthChartJson = window.innerWidth;
								}
								else
							{
									sbiJson.CHART.width = window.innerWidth*(Number(widthChartJson)/100);
//									widthChartJson = window.innerWidth*(Number(widthChartJson)/100);
							}
						}
							else
						{
								sbiJson.CHART.width = (!widthChartJson || widthChartJson=="") ? window.innerWidth : widthChartJson;
//								widthChartJson = (!widthChartJson || widthChartJson=="") ? window.innerWidth : widthChartJson;
						}  							
							
							var ratioChartJson = sbiJson.CHART.width/sbiJson.CHART.height;
							
							var heightPrevPan = globalThis.previewPanel.getHeight();
							var widthPrevPan = globalThis.previewPanel.getWidth();
							
							/**
            			 * Set the "Loading preview..." image at the beginning of export,
            			 * so the user can know that the request for the image is sent.
            			 * 
            			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
            			 */
            			setPreviewImage(Sbi.chart.designer.Designer.relativePathReturn + '/img/loading_preview.png',heightPrevPan,widthPrevPan);
							
							var widthImg = 0;
							var heightImg = 0;
							
							widthImg = (sbiJson.CHART.width <= widthPrevPan) ? sbiJson.CHART.width : widthPrevPan;
							heightImg = widthImg/ratioChartJson;
							
							var ratioImg = widthImg/heightImg;
							
							console.log(ratioImg);
							
							heightImg = (heightImg <= heightPrevPan) ? heightImg : heightPrevPan;
							widthImg = heightImg*ratioImg;
							
							/**
							 * We added new property to the 'parameters', the 'exportWebApp'.
							 * This property is boolean value that will tell the VM that we
							 * are coming from the Highcharts Export web application and that
							 * it (the VM) should reconfigure its code according to that fact.
							 * E.g. some properties that VM initialy has should not be provided
							 * when previewing the chart in the Designer (in the Preview panel).
							 * 
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							var parameters = 
							{
							jsonTemplate: Ext.JSON.encode(sbiJson),
							exportWebApp: true	
						};
							
						chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
							var chartConf = response.responseText;								
							
							/**
							 * If chart types of documents (charts) that user wants to render in the
							 * Preview panel (using the Highcharts export engine) is TREEMAP or the 
							 * HEATMAP we need to prepare data that we receive from the VM of that
							 * chart type by sending them to the 'treemap.js'.
							 * 
							 * @author Daniele Davì
							 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
							 */
							
							/**
							 * Conversion of JSON text (string) into the JSON object that we need for
							 * the function that prepares all the data needed for the TREEMAP or
							 * HEATMAP rendering-exporting engine.
							 * 
							 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
							 */
							var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
							
							if (chartType == 'TREEMAP' || chartType == 'HEATMAP')
							{
								var jsonChartConf = Ext.JSON.decode(chartConf);
								
								if(chartType == 'TREEMAP' && typeof(prepareChartConfForTreemap) == "function") {
									chartConf = Ext.JSON.encode(prepareChartConfForTreemap(jsonChartConf));
								}
								else if(chartType == 'HEATMAP' && typeof(prepareChartConfForHeatmap) == "function") {
									chartConf = Ext.JSON.encode(prepareChartConfForHeatmap(jsonChartConf));
								}
							}								
							
							
							/**
				  			 * The height and width of the chart are set inside the 'chartConf'
				  			 * parameter.
				  			 * 
				  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				  			 */
							var parameters = {
  									options: chartConf,
  									content:'options',
  									type:'image/png',
  									//height: 298,
  									//width: previewPanel.getWidth(),
  									scale: undefined,
  									constr:'Chart',
  									callback: undefined,
  									async: 'true'
  							};
							
  							chartExportWebServiceManager.run('exportPng', parameters, [], 
									function (response) {
      								var src = '/highcharts-export-web/'+response.responseText;
      								
      								/**
      					  			 * 
      					  			 * 
      					  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
      					  			 */
      								srcImg = src;
      								setPreviewImage(src,heightImg,widthImg);	      								
      							},
      							function (response) {
      									      								
      								var src = Sbi.chart.designer.Designer.relativePathReturn + '/img/preview-not-available.png';
      								setPreviewImage(src);	
      								
      								if (response.status == 0)
  									{
      									Sbi.exception.ExceptionHandler.showErrorMessage
	    								(
	    									LN("sbi.chartengine.preview.error.wrongData.bodyText"),
	    									LN("sbi.chartengine.preview.error.title")
	    								);
  									}
      								
      							}
  							);
						}
						,
							function (response) {
							
								var src = Sbi.chart.designer.Designer.relativePathReturn + '/img/preview-not-available.png';
								setPreviewImage(src);
								
								Sbi.exception.ExceptionHandler.showErrorMessage
							(
								LN("sbi.chartengine.preview.error.missingData.bodyText"),
								LN("sbi.chartengine.preview.error.title")
							);
								
							});
					}
				}
  			);
  			
  			var previewPanel = this.previewPanel;
			
			var hostName = this.hostName; 
			var serverPort = this.serverPort;
  			
			function setPreviewImage(src,heightImg,widthImg) {
				previewPanel.removeAll();
				
				/**
	  			 * If the size of the image is equal to the size of the height of the 
	  			 * Preview panel, reduce its height for 32, because this is how much
	  			 * the Preview panel's header takes. This way, the picture will be 
	  			 * shown completely.
	  			 * 
	  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	  			 */				
				if (heightImg+32 >= globalThis.previewPanel.getHeight())
				{
					heightImg -= 32;
					widthImg -= 32;
				}
				
				var previewImg = Ext.create('Ext.Img', {
				    src: src,
				    shrinkWrap:true,
				    
				    /**
		  			 * Set the height and width of the image in a way that is calculated
		  			 * for dimensions of the Preview panel and the dimensions of the chart
		  			 * that are set by the user (fixed or no (empty) values for height/
		  			 * width of the chart (template)). 
		  			 * 
		  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  			 */
				    width: widthImg,
				    height: heightImg,
				    
				    style: {
				        'display': 'block'
				    }
				});
				
				previewPanel.add(previewImg);
			}; 
			
			/**
  			 * The height and width of the chart (document, template). These values are
  			 * specified by the user and are taken in real time (when clicking on the 
  			 * button that serves for rendering the image inside the Preview panel), even
  			 * when those values are not set. In the latter case, the chart will be of
  			 * dimensions of the current dimensions of the window.
  			 * 
  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  			 */
			var heightChartJson = null;
			var widthChartJson = null;
			
			var heightDimType = null;
			var widthDimType = null;
			
			var previewTools = [{ xtype: 'tbfill' }, {
				
				/**
				 * Old value for xtype was "image".
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */ 
				xtype: 'tool',	
	            type: 'refresh',	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	            padding: "3 0 0 0", // @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	            height: 22,
	            
	            /**
				 * These two parameters were not commented.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */ 
	            // src: Sbi.chart.designer.Designer.relativePathReturn + '/img/refresh.png',
	            //cls: 'tool-icon',
	            listeners: {
	            	click: {
	            		element: 'el',
	            		fn: function(){			
	            			
  							var sbiJson = Sbi.chart.designer.Designer.exportAsJson(true); 
							
  							/**
  				  			 * Code that serves for the calculation of the size of the image that is going to
  				  			 * be rendered and displayed inside the Preview panel of the Designer. Calculation
  				  			 * is based on the current dimensions of the Preview panel and on the dimensions of
  				  			 * the chart (document, template) that is set by the user (even if the dimensions
  				  			 * (chart's height and width) are not set by the user - their values will be taken
  				  			 * from respective dimensions of the entire window). 
  				  			 * 
  				  			 * @author Daniele Davì
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  				  			 */
  							heightChartJson = sbiJson.CHART.height;
  							widthChartJson = sbiJson.CHART.width;
  							
  							heightDimType = sbiJson.CHART.heightDimType;
  							widthDimType = sbiJson.CHART.widthDimType;
  							  							
  							if (heightDimType == "percentage")
							{  								
  								if (!heightChartJson || heightChartJson == "")
  								{
  									sbiJson.CHART.height = window.innerHeight;
//  									heightChartJson = window.innerHeight;
  								}
  								else
								{
  									sbiJson.CHART.height = window.innerHeight*(Number(heightChartJson)/100);
//  									heightChartJson = window.innerHeight*(Number(heightChartJson)/100);
								}
							}
  							else
							{
  								sbiJson.CHART.height = (!heightChartJson || heightChartJson=="") ? window.innerHeight : heightChartJson;
//  								heightChartJson = (!heightChartJson || heightChartJson=="") ? window.innerHeight : heightChartJson;
							}
  							
  							if (widthDimType == "percentage")
							{
  								if (!widthChartJson || widthChartJson == "")
  								{
  									sbiJson.CHART.width = window.innerWidth;
//  									widthChartJson = window.innerWidth;
  								}
  								else
								{
  									sbiJson.CHART.width = window.innerWidth*(Number(widthChartJson)/100);
//  									widthChartJson = window.innerWidth*(Number(widthChartJson)/100);
								}
							}
  							else
							{
  								sbiJson.CHART.width = (!widthChartJson || widthChartJson=="") ? window.innerWidth : widthChartJson;
//  								widthChartJson = (!widthChartJson || widthChartJson=="") ? window.innerWidth : widthChartJson;
							}  							
  							
  							var ratioChartJson = sbiJson.CHART.width/sbiJson.CHART.height;
  							
  							var heightPrevPan = globalThis.previewPanel.getHeight();
  							var widthPrevPan = globalThis.previewPanel.getWidth();
  							
  							/**
	            			 * Set the "Loading preview..." image at the beginning of export,
	            			 * so the user can know that the request for the image is sent.
	            			 * 
	            			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	            			 */
	            			setPreviewImage(Sbi.chart.designer.Designer.relativePathReturn + '/img/loading_preview.png',heightPrevPan,widthPrevPan);
  							
  							var widthImg = 0;
  							var heightImg = 0;
  							
  							widthImg = (sbiJson.CHART.width <= widthPrevPan) ? sbiJson.CHART.width : widthPrevPan;
  							heightImg = widthImg/ratioChartJson;
  							
  							var ratioImg = widthImg/heightImg;
  							  							
  							heightImg = (heightImg <= heightPrevPan) ? heightImg : heightPrevPan;
  							widthImg = heightImg*ratioImg;
  							
  							/**
  							 * We added new property to the 'parameters', the 'exportWebApp'.
  							 * This property is boolean value that will tell the VM that we
  							 * are coming from the Highcharts Export web application and that
  							 * it (the VM) should reconfigure its code according to that fact.
  							 * E.g. some properties that VM initialy has should not be provided
  							 * when previewing the chart in the Designer (in the Preview panel).
  							 * 
  							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  							 */
  							var parameters = 
  							{
								jsonTemplate: Ext.JSON.encode(sbiJson),
								exportWebApp: true
							};
  							
  							/**
  							 * https://production.eng.it/jira/browse/KNOWAGE-581 
  							 * in CockpitEngine, the dataset for the preview could be null,
  							 * so we need to add the label as a parameter. So, server side
  							 * we can get the dataset from the label
  							 * 
  							 * @author Giorgio Federici (giorgio.federici@eng.it)
  							 */
  							if(isCockpit){
  								parameters.datasetLabel = datasetLabel;
  							}
  							
							chartServiceManager.run('jsonChartTemplate', parameters, [], function (response) {
								var chartConf = response.responseText;								
								
								/**
								 * If chart types of documents (charts) that user wants to render in the
								 * Preview panel (using the Highcharts export engine) is TREEMAP or the 
								 * HEATMAP we need to prepare data that we receive from the VM of that
								 * chart type by sending them to the 'treemap.js'.
								 * 
								 * @author Daniele Davì
								 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
								 */
								
								/**
								 * Conversion of JSON text (string) into the JSON object that we need for
								 * the function that prepares all the data needed for the TREEMAP or
								 * HEATMAP rendering-exporting engine.
								 * 
								 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
								 */
								var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
								
								if (chartType == 'TREEMAP' || chartType == 'HEATMAP')
								{
									var jsonChartConf = Ext.JSON.decode(chartConf);
									
									if(chartType == 'TREEMAP' && typeof(prepareChartConfForTreemap) == "function") {
										chartConf = Ext.JSON.encode(prepareChartConfForTreemap(jsonChartConf));
									}
									else if(chartType == 'HEATMAP' && typeof(prepareChartConfForHeatmap) == "function") {
										chartConf = Ext.JSON.encode(prepareChartConfForHeatmap(jsonChartConf));
									}
								}								
								
								
								/**
					  			 * The height and width of the chart are set inside the 'chartConf'
					  			 * parameter.
					  			 * 
					  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					  			 */
								var parameters = {
      									options: chartConf,
      									content:'options',
      									type:'image/png',
      									//height: 298,
      									//width: previewPanel.getWidth(),
      									scale: undefined,
      									constr:'Chart',
      									callback: undefined,
      									async: 'true'
      							};
								
      							chartExportWebServiceManager.run('exportPng', parameters, [], 
  									function (response) {
	      								var src = '/highcharts-export-web/'+response.responseText;
	      								
	      								/**
	      					  			 * 
	      					  			 * 
	      					  			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	      					  			 */
	      								srcImg = src;
	      								setPreviewImage(src,heightImg,widthImg);	      								
	      							},
	      							function (response) {
	      									      								
	      								var src = Sbi.chart.designer.Designer.relativePathReturn + '/img/preview-not-available.png';
	      								setPreviewImage(src);	
	      								
	      								if (response.status == 0)
      									{
	      									Sbi.exception.ExceptionHandler.showErrorMessage
		    								(
		    									LN("sbi.chartengine.preview.error.wrongData.bodyText"),
		    									LN("sbi.chartengine.preview.error.title")
		    								);
      									}
	      								
	      							}
      							);
							}
							,
  							function (response) {
								
  								var src = Sbi.chart.designer.Designer.relativePathReturn + '/img/preview-not-available.png';
  								setPreviewImage(src);
  								
  								Sbi.exception.ExceptionHandler.showErrorMessage
								(
									LN("sbi.chartengine.preview.error.missingData.bodyText"),
									LN("sbi.chartengine.preview.error.title")
								);
  								
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
			
			/** 
			 * The height of the single category item dropped in the X-axis
			 * (bottom) panel on the Designer. This number is get by the 
			 * empirical approach, i.e. inspecting of the element.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */			
			var heightOfSingleItem = 20;
			
			/**
			 * Managing the dynamic empty message in the X-axes panel.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */			
			var currentChartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			var emptyTextForAttributes = LN('sbi.chartengine.designer.emptytext.dragdropattributes.' + currentChartType.toLowerCase());
			
			if(!emptyTextForAttributes || emptyTextForAttributes.length==0)
			{
				emptyTextForAttributes = LN('sbi.chartengine.designer.emptytext.dragdropattributes');
			}
			
			/**
			 * How many items is the edge when the bottom of the X-axis
			 * panel should reserve enough space for one more item.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var numberOfMaxItems = null;
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			this.bottomXAxisesPanel = Ext.create("Sbi.chart.designer.ChartCategoriesContainer", {
  				id: 'chartBottomCategoriesContainer',
  				viewConfig: {	
  					plugins: {
  						ptype: 'gridviewdragdrop',
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroupAttribute,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroupAttribute
  					},
  					listeners: {  	
  						
  						/**
  						 * Listen for dropping event of the category item into the
  						 * Categories container - the bottom axis panel. Take care
  						 * that there is enough empty space in the bottom panel for
  						 * dropping new item.
  						 * 
  						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  						 */
  						drop: function(node, data, dropRec, dropPosition)
  						{						
  							var numCategItemsInContainer = this.store.data.length;
  							var containersInitHeight = this.ownerCt.minHeight;  
  							
  							// Old implementation: var gridInitHeight = this.getHeight();
  							var gridInitHeight = this.ownerCt.minHeight - this.ownerCt.header.getHeight();
  							
  							/**
  							 * How many items is the edge when the bottom of the X-axis
  							 * panel should reserve enough space for one more item. This 
  							 * will initialize on the very beginning of the dropping
  							 * items into the X-axis (bottom) panel.
  							 */
  							if (numberOfMaxItems == null)
							{
  								numberOfMaxItems = Math.round(gridInitHeight/heightOfSingleItem);
							}
  							
  							if (numCategItemsInContainer >= numberOfMaxItems)
							{
  								this.ownerCt.setHeight(containersInitHeight + (numCategItemsInContainer-numberOfMaxItems+1)*heightOfSingleItem);
							}
  						},  						
  						
  						/**
						 * When the categories container (bottom X-axis panel) is populated
						 * with all categories that chart posses, this event will be fired
						 * and in this place catched in order to configure bottom panel's 
						 * height (the height will be set according to the number of items
						 * in the container). Since, we should basically fire the "drop"
						 * event again since we need the exact same logic set there.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
  						categoriesLoaded: function()
  						{  							
  							this.fireEvent("drop");
  						},
  						
  						/**
						 * When the grid's view is ready, set the tooltip item in it so
						 * it can listen for mouse over ('beforeshow' event) in order to
						 * display the tooltip with appropriate content (the name of the
						 * serie that mouse hovers in the Y-axis panel).
						 * 
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
				        viewready: function (grid) {
				        	
				        	var view = grid;
				            
				            // record the current cellIndex
				            grid.mon(view, {
				                
				            	uievent: function (type, view, cell, recordIndex, cellIndex, e) {
				                	grid.cellIndex = cellIndex;
				                    grid.recordIndex = recordIndex;
				                }
				            
				            });
			
				            grid.tip = Ext.create('Ext.tip.ToolTip', {
				               
				        	   	target: view.el,
				                delegate: '.x-grid-cell',
				                trackMouse: true,
				                renderTo: Ext.getBody(),
				                
				                listeners: {
				                	
				                    beforeshow: function updateTipBody(tip) {
				                    	
//						                        if (!Ext.isEmpty(grid.cellIndex) && grid.cellIndex !== -1) {
			                            	var header = grid.headerCt.getGridColumns()[0];
				                            var val = grid.getStore().getAt(grid.recordIndex).get(header.dataIndex);
//						                            var isDateColumn = header.xtype == 'datecolumn';
//						                            tip.update(isDateColumn ? Ext.util.Format.date(val, header.format) : val);
				                            var stringTip = '<b>Category:</b></br>' + val;
				                            tip.update(stringTip);
//						                        }
				                            
				                    }
				                }
				            });
				        },
  						
  	  					beforeDrop: function(node, data, dropRec, dropPosition) {   	  						
  	  						
  	  						/**
							 * Prevent drag&drop more than one attribute (category) per time.
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							if (data.records.length == 1)
							{  	  						
	  	  						var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase(); 
	  	  							  						
	  	  						/**
	  	  						 * Taking care of the order of the categories (based on their type) for the 
	  	  						 * HEATMAP chart type.
	  	  						 * 
	  	  						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	  	  						 */  	  						
	  	  						if (chartType == "HEATMAP") {
		  	  						if (this.store.data.length == 0 && data.records.length == 1) {
		  	  								/**
		  	  							 * first category doesn't have to be strictly of  type DATE(Timpestamp)
		  	  							 * validation removed
		  	  							 * @author Ana Tomic (ana.tomic@mht.net)
		  	  								 */
//		  	  							if (data.records[0].data.categoryDataType != "Timestamp") {	  	  								
//		  	  								/**
//		  	  								 * Show the message that tells user that he should firstly define
//		  	  								 * (drop) the item for the categories (attributes) container that
//		  	  								 * is of a DATE type (Timestamp).
//		  	  								 */
//			  	  							Ext.Msg.show({
//		  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongdatatypefirst.title"),
//		  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongdatatypefirst.warningmessage"),
//		  		            					icon : Ext.Msg.WARNING,
//		  		            					closable : false,
//		  		            					buttons : Ext.Msg.OK,
//		  		            					minWidth: 200,
//		  		            					
//		  		            					buttonText : {
//		  		            						ok : LN('sbi.chartengine.generic.ok')
//		  		            					}
//	  	  									});	
//		  	  								
//		  	  								return false;
//	  	  								}	  	  								
	  	  							}
		  	  						else if (this.store.data.length == 1 && data.records.length == 1) {	  	  	
		  	  							/**
		  	  							 * if the one of the categories is DATE(Timestamp) has to be selected as the first one
		  	  							 * validation is used
		  	  							 */
		  	  							if (dropPosition == "after" && data.records[0].data.categoryDataType == "Timestamp" ||
		  	  								dropPosition == "before" && data.records[0].data.categoryDataType != "Timestamp") {
			  	  							Ext.Msg.show ({
		  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongorderafterbefore.title"),	
		  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongorderafterbefore.warningmessage"),	
		  		            					icon : Ext.Msg.WARNING,
		  		            					closable : false,
		  		            					buttons : Ext.Msg.OK
	  										});
		  	  								
			  	  							return false;
	  	  								}	 
		  	  							
		  	  							/**
		  	  							 * If we already have one item in the CATEGORY (X-axis) container 
		  	  							 * and we want to add the second (the last one) item, we should
		  	  							 * check if that item inside the container is of type that is not
		  	  							 * the DATE (Timestamp). In that case user MUST drop the item that
		  	  							 * is of DATE (Timestamp) type.
		  	  							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  	  							 * 
		  	  							 * this validation is not used because heatmap doesn't need strictly date
		  	  							 * @author Ana Tomic (ana.tomic@mht.net)
		  	  							 */
//		  	  							if (this.store.data.items[0].data.categoryDataType != "Timestamp" && 
//		  	  									data.records[0].data.categoryDataType != "Timestamp") {
//		  	  								Ext.Msg.show({
//		  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.timestampdataneeded.title"),	
//		  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.timestampdataneeded.warningmessage"),	
//		  		            					icon : Ext.Msg.WARNING,
//		  		            					closable : false,
//		  		            					buttons : Ext.Msg.OK
//	  										});
//		  	  								
//		  	  								return false;
//	  	  								}
	  	  							}
		  	  						else {
		  	  							/**
		  	    						 * Preventing rearranging categories if the chart type is the HEATMAP
		  	    						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		  	    						 */
		  	  							return false;
	  	  							}
	  							}  	  
	  	  						
	  	  						/**
	  	  						 * Prevent taking more than one category from the container when we have
	  	  						 * one of these chart types.
	  	  						 *   	  						
	  	  						 * @author: danristo (danilo.ristovski@mht.net)
	  	  						 */
	  	  						if (data.records.length > 1 && (chartType == "RADAR" || chartType == "SCATTER" || 
	  	  								chartType == "PARALLEL" || chartType == "HEATMAP" || chartType == "CHORD" || chartType == "PIE")) {
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
				  	      				 * (3) 	PARALLEL and  chart: MUST have exactly 2 categories. For the HEATMAP
				  	      				 * 		chart we MUST HAVE exactly two categories among which we must have a
				  	      				 * 		time data type (timestamp) as the first one, while the other one can
				  	      				 * 		be of any data type.
				  	      				 * 
				  	      				 * (4) 	CHORD chart:	We must have exactly two categories. Those categories
				  	      				 * 						MUST HAVE the same set of values that are later going
				  	      				 * 						to be arranged into the regular matrix form.
				  	      				 * 
				  	      				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				  	      				 */			  	      				
				  	      				if(data.records[0].get('categoryColumn') == categoryItem.get('categoryColumn') 
				  	      						|| (this.store.data.length == 1 && 
				  	      								(chartType == "RADAR" || chartType == "SCATTER" || chartType == "PIE")) 
				  	      									|| (this.store.data.length == 2 && 
				  	      											(chartType == "PARALLEL" || 
				  	      													chartType == "HEATMAP" || 
				  	      														chartType == "CHORD"))) {
				  	      					return false;
				  	      				}
				  	      			}
		  	  					}
	  						}
							else
				        	{
					        	return false;
				        	}
  	  					}				        
  					}
  				},
  				
  				emptyText: emptyTextForAttributes,	
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
						hidden: Sbi.chart.designer.ChartUtils.isBottomAxisTextFieldDisabled(), // *_*
						allowBlank:  true,
			            emptyText: LN('sbi.chartengine.designer.emptytext.axistitle'),
						selectOnFocus: true,
						listeners: {
				            change: 'onTitleChange',
				        }
					},					

					/**
					 * Provide a button that will let user remove all category items from the 
					 * X-axis panel.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					Ext.create
					(
						"Ext.panel.Tool",
						
						{
							type: "deleteAllItemsFromAxisPanel",
							padding: "3 0 0 0",
							height: 22,
							hidden: false,
							
							handler: function()
							{
								Ext.Msg.show
								(
									{
										title : LN("sbi.chartengine.designer.removeAllCategories.title"),										
										message : LN("sbi.chartengine.designer.removeAllCategories.msg"),	
											
										icon : Ext.Msg.QUESTION,
										closable : false,
										buttons : Ext.Msg.OKCANCEL,
										
										buttonText : 
										{
											ok : LN('sbi.chartengine.generic.ok'),
											cancel : LN('sbi.generic.cancel')
										},
										
										fn : function(buttonValue, inputText, showConfig)
										{
											if (buttonValue == 'ok') 
											{
												Sbi.chart.designer.Designer.cleanCategoriesAxis();

												var bottomXAxisPanel = globalThis.bottomXAxisesPanel;
												
												/**
												 * When removing all items from the category container (the bottom
												 * X-axis panel), reset the height of it to the initial one - the 
												 * minimum height that panel can take.
												 * 
												 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
												 */
												bottomXAxisPanel.setHeight(bottomXAxisPanel.minHeight);
											}
										}
									}
								);
							}
						}
					),
					
					// STYLE POPUP
					{
					    type:'gear',
					    padding: "3 0 0 0",// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						height: 22,		// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					    tooltip: LN('sbi.chartengine.designer.tooltip.setaxisstyle'),
					    id: "stylePopupBottomPanel", // @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					    hidden: Sbi.chart.designer.ChartUtils.isBottomAxisStyleButtonDisabled(), // *_*
					    //flex: 1,
					    handler: function(event, toolEl, panelHeader) {
					    	var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
					    	if(chartType.toUpperCase() != 'PIE') {
						    	var thisChartColumnsContainer = panelHeader.ownerCt;
						    	
						    	var axisStylePopup = Ext.create('Sbi.chart.designer.AxisStylePopup', {
						    		axisData: thisChartColumnsContainer.getAxisData()
								});
						    	
						    	axisStylePopup.show();						    	
					    	}					    		
						}
					},
					
					/**
					 * Bottom X-axis panel's header Help button for providing necessary information 
					 * about category items that should be specified for particular chart type (e.g.
					 * the number of categories that current chart type needs, type of data that 
					 * attributes (categories) dropped represent etc.).
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					{
						type: "help",
						padding: "3 0 0 0",
						height: 22,
					    tooltip: LN('sbi.chartengine.designer.tooltip.setaxisstyle'),
					    hidden: false,
					    
					    handler: function()
					    {
					    	var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toLowerCase();
					    	
							Sbi.exception.ExceptionHandler.showInfoMessage
							(
								LN("sbi.chartengine.designer.emptytext.dragdropattributes." + chartType), 
								
								Sbi.locale.sobstituteParams
								(
      								LN('sbi.chartengine.designer.infoaboutcategories'), 
      								[chartType.charAt(0).toUpperCase() + chartType.slice(1)]
  								)
							);
					    }
					}
				],
			    
				hideHeaders: true,
  				columns: [
  				          {
					text: LN('sbi.chartengine.designer.columnname'), 
					dataIndex: 'categoryColumn',
					sortable: false,
					flex: 10
				}, 
				{
					text: LN('sbi.chartengine.designer.columnalias'), 
					dataIndex: 'axisName',
					hidden: true,	// Not used, so hidden (Danilo Ristovski)
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
						icon: '/' + globalThis.mainContextName + '/themes/sbi_default/img/delete.gif',
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
            					buttonText : {
            						ok : LN('sbi.chartengine.generic.ok'),
            						cancel : LN('sbi.generic.cancel')
            					},
            					fn : function(buttonValue, inputText, showConfig){
            						if (buttonValue == 'ok') {
            							var rec = store.removeAt(rowIndex);
            							
            							/**
            							 * When removing category item from the bottom axis panel
            							 * take care that height of the container panel follows the
            							 * current number of items in it (dynamic behaviour - expansion
            							 * and contraction of the panel).
            							 * 
            							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
            							 */            							
            							var numCategItemsInContainer = grid.store.data.length;
              							var heightOfGrid = grid.getHeight();
              							var heightOfAllCategoryItems = numCategItemsInContainer*heightOfSingleItem;
              							
              							if(heightOfGrid - heightOfAllCategoryItems >= heightOfSingleItem)
          								{
              								grid.ownerCt.setHeight(grid.ownerCt.getHeight()-heightOfSingleItem);
          								}
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
			
			/**
			 * Hiding the bottom (X) axis title textbox and gear tool
			 * if the already existing (saved) chart (document) is one of the 
			 * specified chart types.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var typeOfChart = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			/**
			 * The order of GUI elements on the header of the bottom panel:
			 * 		[0] - Text field for axis title
			 * 		[1] - "Remove all items" icon (recycle bin)
			 * 		[2] - Gear button - axes style configuration popup for categories
			 */
			
			if (typeOfChart == "SUNBURST" || typeOfChart == "WORDCLOUD" || 
					typeOfChart == "TREEMAP" || typeOfChart == "PARALLEL" ||
						 typeOfChart == "CHORD" || typeOfChart == "PIE") {
				
				/**
				 * Hide the bottom (X) axis title textbox.
				 */
				Ext.getCmp("chartBottomCategoriesContainer").tools[0].hidden = true;
				
				/**
				 * Hide the gear icon on the bottom (X) axis panel.
				 */
				if (typeOfChart != "CHORD" && typeOfChart != "PARALLEL")
				{
					Ext.getCmp("chartBottomCategoriesContainer").tools[2].hidden = true;	
				}
			}
			
			this.chartStructure = Ext.create('Sbi.chart.designer.ChartStructure', {
  				title: LN('sbi.chartengine.designer.stepChartStructure'),
  				leftYAxisesPanel: this.leftYAxisesPanel,
  				previewPanel: this.previewPanel,
  				rightYAxisesPanel: this.rightYAxisesPanel,
  				bottomXAxisesPanel: this.bottomXAxisesPanel
  			});
				
			/**
			 * If the chart type is GAUGE, hide the bottom X-axis panel on the
			 * Step 1 of the Deisgner.
			 * (danilo.ristovski@mht.net)
			 */
			if (typeOfChart == "GAUGE") {
				this.bottomXAxisesPanel.hide();
			}
			else
			{
				this.bottomXAxisesPanel.show();
			}
			
			// Creating Configuration step panel
			this.cModel = 
				Sbi.chart.designer.ChartUtils.createChartConfigurationModelFromJson(jsonTemplate);
					
			this.cViewModel = Ext.create('Ext.app.ViewModel',{
  				data: {
  					configModel: this.cModel
				}
  			});
						
			this.chartConfiguration = Ext.create('Sbi.chart.designer.ChartConfiguration', {
  				title: LN('sbi.chartengine.designer.stepChartConfiguration'),
  				viewModel: this.cViewModel
  			});
			
			// Creating Cross navigation step panel
			this.crossNavigationPanel = Ext.create('Sbi.chart.designer.CrossNavigationPanel', {
				id: 'crossNavigation',
				
				contextName: thisContextName,
				mainContextName: mainContextName,
				userId: userId, 
				hostName: hostName,
				sbiExecutionId: sbiExecutionId,
			       
				title: LN('sbi.chartengine.designer.stepCrossNavigation'),
			});
			// crossNavigation should be disabled when in Cockpit 
			if(isCockpit){
				this.crossNavigationPanel.hide();
			}
						
			// Creating Advanced Editor step
			this.advancedEditor = Ext.create('Sbi.chart.designer.AdvancedEditor', {
  				id: 'advancedEditor',
  				title: LN('sbi.chartengine.designer.stepAdvancedEditor')
  			});
			
			// tabs integration
			var coreServiceManager = this.coreServiceManager;
			
			this.stepsTabPanel = Ext.create('Ext.tab.Panel', {
  				bodyBorder: false,
  				width: '100%',
  				layout: "fit",
  				region: 'center',
				title: {hidden: true },
				previousTabId: '',
  				tools:[{
  					xtype: 'tbfill' 
  				}, {
  		            xtype: 'image',
  		            src: Sbi.chart.designer.Designer.relativePathReturn + '/img/save.png',
  		            cls: 'tool-icon',
  		            hidden: isCockpit,
  		            listeners: {
  		            	
  		            	click: {
  		            		element: 'el',
  		            		fn: function(){
  		            			
  		            			/**
  		            			 * TODO: Check if this part affects somehow the functioning of the application.
  		            			 * 
  		            			 * NOTE: Commented by Benedetto Milazzo and Danilo Ristovski: problem when saving chart
  		            			 * (document) while inside the Advanced editor tab
  		            			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  		            			 * 
  		            			 * NOTE: Uncommented since saving in the Advanced editor did not take
  		            			 * changes that we provided to e.g. mandatory fields while we were on
  		            			 * this tab. E.g. if we remove value of some mandatory field in the
  		            			 * Advanced tab and then try to save the document, the validation will
  		            			 * not be done appropriately - we will be able to save document even 
  		            			 * without a value of the mandatory field.
  		            			 * @date 20.01.2016.  		            			  
  		            			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  		            			 */
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
  		            					buttonText : {
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
  	  		            							coreServiceManager.run('saveChartTemplate', parameters, [], function (response) {});
  	  		            							Ext.getBody().unmask();
  	  		            							
  	  		            							/**
  	  		            							 * Change (update) the JSON template (jsonTemplate) of the current file
  	  		            							 * when saving since we need it for some other potential modifications 
  	  		            							 * of the chart (document). This is useful in the case when user saves
  	  		            							 * the document and then stays inside of the same document (without
  	  		            							 * reloading the Designer) and then modifies it somewhat more afterwards.
  	  		            							 * Logically, we do not need this feature when clicking on "Save and back". 
  	  		            							 * 
  	  		            							 * @author: danristo (danilo.ristovski@mht.net)
  	  		            							 */
  	  		            							jsonTemplate =  Sbi.chart.designer.Designer.exportAsJson();
//  	  		            							Sbi.chart.designer.Designer.chartTypeChanged = false;
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
  		            src: Sbi.chart.designer.Designer.relativePathReturn + '/img/saveAndGoBack.png',
  		            cls: 'tool-icon',
  		            hidden: isCockpit,
  		            listeners: {
  		            	click: {
  		            		element: 'el',
  		            		fn: function(){
  		            			
  		            			var activeTab = Sbi.chart.designer.Designer.stepsTabPanel.getActiveTab();
  		            			if (activeTab.getId() == 'advancedEditor') {
  		            				var json = activeTab.getChartData(); // original code
  		            				Sbi.chart.designer.Designer.update(json);
  		            			}
  		            			var errorMessages = Sbi.chart.designer.Designer.validateTemplate();

  		            			if (errorMessages == false) {
  		            				Ext.Msg.show({
  		            					title : LN('sbi.chartengine.designer.savetemplate.title'),
  		            					message : LN('sbi.chartengine.designer.savetemplateAndGoBack.msg'),
  		            					icon : Ext.Msg.QUESTION,
  		            					closable : false,
  		            					buttons : Ext.Msg.OKCANCEL,
  		            					buttonText : {
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
  		            							
//  		            							Sbi.chart.designer.Designer.chartTypeChanged = false; // danristo (19.10)
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
					
					/**
					 * Detect the browser and according to this information, if it is the
					 * Mozilla Firefox, provide a tab listener for 'focus' event on it for
					 * every tab that is present in the panel. This event will fire whenever
					 * user clicks once on particular tab and it will consequesntly set the
					 * tab as the active one.
					 * 
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					render: function () 
					{
						if (Ext.browser.is('Firefox')) 
						{
							this.items.each
							(
								function (itm, idx) 
								{
									itm.tab.on
									(
										'focus',
										
										function (tab) 
										{
											var tabpanel = tab.up('tabpanel');
											tabpanel.setActiveTab(idx);
										}
									);
								}
							);
						}
					},
					
				    tabchange: function(tabPanel, tab){
				    	
				    	if(tab.getId() == 'advancedEditor') { 
				    		Sbi.chart.designer.Designer.chartTypeColumnSelector.disable();
				    		var json = Sbi.chart.designer.Designer.exportAsJson();
							tab.setChartData(json);
							
						} else if(tabPanel.previousTabId == 'advancedEditor') { 
							Sbi.chart.designer.Designer.chartTypeColumnSelector.enable();
//						
							var advancedEditor = Ext.getCmp('advancedEditor');
							if(advancedEditor.dataChanged == true) {
//								
								var newJson = advancedEditor.getChartData();
								var oldJson = Sbi.chart.designer.Designer.exportAsJson();
//								
								var tabChangeChecksMsgs = Sbi.chart.designer.Designer.tabChangeChecksMessages(oldJson, newJson);
								if(Sbi.chart.designer.Designer.tabChangeChecksFlag && tabChangeChecksMsgs) {
										Ext.Msg.show({
											title : LN('sbi.chartengine.designer.tabchange.title'),
											message : tabChangeChecksMsgs,
											icon : Ext.Msg.WARNING,
											closable : false,
											buttons : Ext.Msg.OK,
											buttonText : {
												ok : LN('sbi.chartengine.generic.ok'),
											}
										});
//									
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
  					this.crossNavigationPanel,
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
  					bodyPadding: 10,
  					
  					/**
  					 * Disable resizing of the left part of the Designer's main page.
  					 * 
  					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
  					 */
  					splitterResize: false
  				},
  				items: [
  					this.chartTypeColumnSelector,
  					this.stepsTabPanel,
  				]
  			});
			
			//Handle resize event for making the designer responsive
			Ext.on('resize', function(w, h){
				this.chartStructure.updateLayout();
				this.chartConfiguration.updateLayout();
				this.crossNavigationPanel.updateLayout();
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
  			
			/* START LOADING CROSS NAVIGATION DATA>>>>>>>>>>>>>>>>>>>> */
			this.crossNavigationPanel.setCrossNavigationData(jsonTemplate.CHART.DRILL);
			/* END LOADING CROSS NAVIGATION DATA <<<<<<<<<<<<<<<<<<<< */
						
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
			
			var category = jsonTemplate.CHART.VALUES.CATEGORY;
			
			/**
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			/**
			 * MANAGE MULTIPLE CATEGORIES: if the chart type is on of following.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "SUNBURST" || chartType.toUpperCase() == "WORDCLOUD" || 
					chartType.toUpperCase() == "TREEMAP" || chartType.toUpperCase() == "PARALLEL" || 
						chartType.toUpperCase() == "HEATMAP" || chartType.toUpperCase() == "CHORD") {	
				if (category.length == undefined || category.length == null) {
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
			/**
			 * If the chart type is PIE, BAR, LINE, SCATTER or RADAR.
			 * (danilo.ristovski@mht.net)
			 */
			else {
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
			
			/**
			 * When all categories are loaded into the categories container
			 * (bottom X-axis panel), fire an event that will be detected by
			 * the view of the grid panel (that container).
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			Ext.getCmp("chartBottomCategoriesContainer").getView().fireEvent("categoriesLoaded",categoriesStore.data.length);
		},			
			
		loadAxesAndSeries: function(jsonTemplate) {
			var leftYAxisesPanel = this.leftYAxisesPanel;
			var rightYAxisesPanel = this.rightYAxisesPanel;
			var bottomXAxisesPanel = this.bottomXAxisesPanel;

			var globalScope = this;
			Sbi.chart.designer.ChartColumnsContainerManager.resetContainers();
			
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			var theStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
						
			var yCount = 1;
			
			Ext.Array.each(jsonTemplate.CHART.AXES_LIST.AXIS, function(axis, index){
			
				if(axis && axis.type.toUpperCase() == "SERIE"){

					var isDestructible = (yCount > 1);
					var panelWhereAddSeries = (yCount == 1) ? rightYAxisesPanel : null;
					// pie workaround "!axis.position"
					if(!axis.position || axis.position.toLowerCase() == 'left') {											
						
						/**
						 * Hiding the left (Y) axis title textbox, gear and plus tools
						 * if the already existing (saved) chart (document) is one of 
						 * the specified chart types.
						 * (danilo.ristovski@mht.net)
						 */
						var hideAxisTitleTextbox = false;
						var hideGearTool = false;
						var hidePlusGear = false;
						
						if ( Sbi.chart.designer.ChartUtils.isChartColumnsContainerPlusGearDisabled() ) {
							
							if (chartType != "RADAR" 
								&& chartType != "HEATMAP" 
									&& chartType != "SCATTER" 
										&& chartType!="GAUGE") {										
							
								hideAxisTitleTextbox = true;
								
								if (chartType != "CHORD" && chartType != "PARALLEL")
								{
									hideGearTool = true;
								}								
							}
							
							hidePlusGear = true;
						}
						
						// (danilo.ristovski@mht.net)
						var config = {
							"idAxisesContainer":leftYAxisesPanel.id , 
							"id": '', 
							"panelWhereAddSeries":panelWhereAddSeries, 
							"isDestructible":isDestructible, 
							"dragGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure,
							"dropGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure, 
							"axis":axis, 
							"axisTitleTextboxHidden":hideAxisTitleTextbox, 
							"gearHidden":hideGearTool, 
							"plusHidden":hidePlusGear,
							
							/**
							 * The left Y-axis panel should posses the Info button (the one
							 * with the  question mark), since we should provide information
							 * about the structure of the data needed for this axis, i.e. 
							 * information about series.
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							"infoHidden": false	
						};
						
						var newColumn = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(config);
						leftYAxisesPanel.add(newColumn);

					} else {
						// (danilo.ristovski@mht.net)
						var config = {
							"idAxisesContainer":rightYAxisesPanel.id, 
							"id": '', 
							"panelWhereAddSeries":panelWhereAddSeries, 
							"isDestructible":isDestructible, 
							"dragGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure,
							"dropGroup":Sbi.chart.designer.ChartUtils.ddGroupMeasure, 
							"axis":axis,
							
							/**
							 * The right Y-axis panels should not posses the Info button, since 
							 * we already provided information about the structure of series that
							 * particular chart type is expecting on the main (left) Y-axis panel.
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							"infoHidden": true
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
							
							/**
							 * This variable is introduces since the majority of chart types has experienced 
							 * change of name of the property that represents the font family: from 'font' to
							 * 'fontFamily'. 
							 * 
							 * @author: danristo (danilo.ristovski@mht.net)
							 */
							var tooltipFontFamily = "";
							
							if (jsonTooltipStyle.fontFamily)
							{
								tooltipFontFamily = jsonTooltipStyle.fontFamily;
							}
							else if (jsonTooltipStyle.font)
							{
								tooltipFontFamily = jsonTooltipStyle.font;
							}
							
							if (chartType == "GAUGE") {									
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
									serieShowAbsValue: serie.showAbsValue,
									serieShowPercentage: serie.showPercentage,
									seriePrecision: serie.precision + '',
									seriePrefixChar: serie.prefixChar,
									seriePostfixChar: serie.postfixChar,
									serieFormat: serie.serieFormat,
									serieFormatOrPrecision: serie.serieFormatOrPrecision,
									
									/**
									 * This item is going to be removed since the serie tooltip HTML template
									 * is handled by the velocity model of the appropriate chart type (this is
									 * done staticly, "under the hood").
									 * 
									 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
									 */
//									serieTooltipTemplateHtml: tooltip.templateHtml,
									
									serieTooltipBackgroundColor: tooltip.backgroundColor,
									serieTooltipAlign: jsonTooltipStyle.align,
									serieTooltipColor: jsonTooltipStyle.color,
									serieTooltipFont: tooltipFontFamily,
									serieTooltipFontWeight: jsonTooltipStyle.fontWeight,
									serieTooltipFontSize: jsonTooltipStyle.fontSize
								});								
							} 
							else {
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
									serieShowAbsValue: serie.showAbsValue,
									serieShowPercentage: serie.showPercentage,
									seriePrecision: serie.precision + '',
									seriePrefixChar: serie.prefixChar,
									seriePostfixChar: serie.postfixChar,
									serieFormat: serie.serieFormat,
									serieFormatOrPrecision: serie.serieFormatOrPrecision,
									
//									serieTooltipTemplateHtml: tooltip.templateHtml,
									serieTooltipBackgroundColor: tooltip.backgroundColor,
									serieTooltipAlign: jsonTooltipStyle.align,
									serieTooltipColor: jsonTooltipStyle.color,
									serieTooltipFont: tooltipFontFamily,
									serieTooltipFontWeight: jsonTooltipStyle.fontWeight,
									serieTooltipFontSize: jsonTooltipStyle.fontSize
								});
							}
							
							// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							if(chartType == "PARALLEL") {
								globalScope.seriesBeforeDropStore.add(newCol);
							}
							
							store.add(newCol);
						}
					});
				});
			}
			
			//Forcing reset of each store if it is empty in order to show the empty message 
			Ext.Array.each(theStorePool, function(store, index){
				if(store.getCount() == 0) {
					store.reload();
				}
			});
			
			// danristo : listen when axes load (final)
			globalScope.chartTypeSelector.fireEvent("axesSet");	
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
			
			//Workaround for color picker resets
//			if(this.cModel.get('backgroundColor') && this.cModel.get('backgroundColor').trim() == '') {
//				this.cModel.set('backgroundColor', 'transparent');
//			};
//			if(this.cModel.get('legendColor') && this.cModel.get('legendColor').trim() == '') {
//				this.cModel.set('legendColor', 'transparent');
//			};
//			
//			if(this.cModel.get('legendBackgroundColor') && this.cModel.get('legendBackgroundColor').trim() == '') {
//				this.cModel.set('legendBackgroundColor', 'transparent');
//			};
//			
//			if(this.cModel.get('legendTitleColor') && this.cModel.get('legendTitleColor').trim() == '') {
//				this.cModel.set('legendTitleColor', 'transparent');
//			};
			
			this.cViewModel.setData({
				configModel: this.cModel
			});
			
			this.chartConfiguration.setData({
  				viewModel: this.cViewModel._data.configModel.data
  			});
		
			this.chartConfiguration.update({
  				viewModel: this.cViewModel._data.configModel.data
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
			
			// resulted json from 1st, 2nd and 3rd designer steps (without properties catalogue)
			var exportedDesignerSteps = Sbi.chart.designer.ChartUtils.exportAsJson(this.cModel);
									
			// default properties catalogue by used chart library, depending on selected chart type 
    		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
    		chartType = chartType.toLowerCase();
			var library = this.chartLibNamesConfig[chartType];
			var catalogue = propertiesCatalogue[library] || {};
						
			// default properties catalogue by used chart library, depending on selected chart type 
			var oldJsonChartType = Sbi.chart.designer.Designer.jsonTemplate.CHART.type;
			oldJsonChartType = oldJsonChartType.toLowerCase();
			var oldLibrary = this.chartLibNamesConfig[oldJsonChartType];
			
			// last json template in memory
			var lastJsonTemplate = Sbi.chart.designer.Designer.jsonTemplate;
			
			// last json in memory with applied properties catalogue
			var appliedPropertiesOnOldJson = Sbi.chart.designer.ChartUtils.mergeObjects(catalogue, lastJsonTemplate);
			
			// comparison and merge generated json template with the old one
			var removeNotFoundItemsFlag = true;
			var overwrittenJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
					appliedPropertiesOnOldJson, 
					exportedDesignerSteps, 
					{
						removeNotFoundItemsFlag: removeNotFoundItemsFlag
					}
				);

			// add default catalogue properties in case there are new elements generated by designer
			var newJsonTemplate = (library === oldLibrary)?
				Sbi.chart.designer.ChartUtils.mergeObjects(
						catalogue, 
						overwrittenJsonTemplate, 
						{
							removeNotFoundItemsFlag: removeNotFoundItemsFlag
						})
				: Sbi.chart.designer.ChartUtils.mergeObjects(
						catalogue, 
						exportedDesignerSteps, 
						{
							removeNotFoundItemsFlag: removeNotFoundItemsFlag
						}
					);
			
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
		 * @extendedBy: danristo (danilo.ristovski@mht.net)
		 * */		
		validateTemplate: function() {
			var errorMsg = '';			
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			/**
			 * We will use chart model (data from the ChartUtils.js 'cModel')
			 * that holds all the parameters that user specified on the 
			 * Designer page for particular chart type. Through the cModel
			 * (viewModel) we are going to have up-to-date data (parameters)
			 * about the chart that we are creating.
			 * 
			 * @commentBy danristo (danilo.ristovski@mht.net)
			 */
			var chartViewModelData = this.cViewModel.data.configModel.data;
			
			var numberOfSerieItems = Sbi.chart.designer.ChartUtils.getSeriesDataAsOriginalJson().length;
			
			/**
			 * The maximum number of series that the PIE chart can contain.
			 * 
			 * @author danristo (danilo.ristovski@mht.net)
			 */
			var maxNumOfSeriesForPieChart = 4; 
			
			/**
			 * Validation for Step 1 if the mandatory items are not specified.
			 * 
			 * @commentBy danristo (danilo.ristovski@mht.net)
			 */
			if (numberOfSerieItems == 0) {
				
				if (chartType == "TREEMAP" || chartType == "WORDCLOUD" || chartType == "CHORD")
				{
					/**
					 * TREEMAP, WORDCLOUD and CHORD charts need exactly one serie item.
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					errorMsg += "- " + LN('sbi.chartengine.validation.addserie.exactlyOne') + '<br>';
				}
				else if (chartType == "PARALLEL")
				{
					/**
					 * PARALLEL chart needs at least two serie items.
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					errorMsg += "- " + LN('sbi.chartengine.validation.addserie.atLeastTwo') + '<br>';
				}
				else
				{
					errorMsg += "- " + LN('sbi.chartengine.validation.addserie') + '<br>';
				}
				
			}
			else if (numberOfSerieItems < 2)
			{				
				if (chartType == "PARALLEL")
				{
					/**
					 * PARALLEL chart needs at least two serie items.
					 * @author danristo (danilo.ristovski@mht.net)
					 */
					errorMsg += "- " + LN('sbi.chartengine.validation.addserie.atLeastTwo') + '<br>';
				}
			}
			
			/**
			 * Check if there are no categories selected for the chart. For the SUNBURST chart
			 * this one can be an empty array when there are no category items picked. If the
			 * chart type is GAUGE we are not supposed to have any category item at all.
			 * 
			 * @modifiedBy danristo (danilo.ristovski@mht.net)
			 */
			var categoriesPicked = Sbi.chart.designer.ChartUtils.getCategoriesDataAsOriginalJson();
			
			if ((categoriesPicked==null || (Array.isArray(categoriesPicked))) && chartType != "GAUGE") {				
				
				var categoriesAsJson = Sbi.chart.designer.ChartUtils.getCategoriesDataAsOriginalJson();				
				
				if (chartType == "RADAR" || chartType == "SCATTER")
				{
					if (categoriesAsJson.length != 1)
					{
						errorMsg += "- " + LN("sbi.chartengine.validation.exactlyOneCategory") + '<br>'; 
					}
				}
				else if (chartType == "HEATMAP" || chartType == "CHORD")
				{
					if (categoriesAsJson.length != 2)
					{
						errorMsg += "- " + LN("sbi.chartengine.validation.exactlyTwoCategories") + '<br>'; 
					}
				}
				else if (chartType == "TREEMAP" || chartType=="SUNBURST")
				{
					if (categoriesAsJson.length < 2)
					{
						errorMsg += "- " + LN("sbi.chartengine.validation.atLeastTwoCategories") + '<br>';
					}
				}
				else if (categoriesPicked==null || categoriesPicked.length==0)
				{
					errorMsg += "- " + LN('sbi.chartengine.validation.addcategory') + '<br>';
				}				
			}			
				
			var mainConfigurationPanel = this.stepsTabPanel.getComponent(1).getComponent(0);
			
			/**
			 * ********************************************************************
			 * Validate chart's height in the Generic configuration panel on Step 2
			 * ********************************************************************
			 */		
			
			var heightField = mainConfigurationPanel.getComponent("heightFieldset").getComponent("chartHeightNumberfield");
			//var heightField = mainConfigurationPanel.getComponent("chartHeightNumberfield");
			var heightFieldValue = heightField.value;
			var heightViewModelValue = this.cViewModel.data.configModel.data.height;
						
			if ((heightFieldValue || parseInt(heightFieldValue)==0) 
					&& heightFieldValue!="" && heightFieldValue!=null)
			{
				if (heightFieldValue < heightField.minValue)
				{					
					errorMsg += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.configuration.minValue"),
						
						[
							LN("sbi.chartengine.configuration.height"),
							heightField.minValue,
							LN('sbi.chartengine.configuration')
						]
					);
				}
			}
			else
			{				
				if (heightViewModelValue!=null && heightViewModelValue!="" 
						&& heightViewModelValue < heightField.minValue)
				{
					errorMsg += Sbi.locale.sobstituteParams
					(
						LN("sbi.chartengine.validation.configuration.minValue"),
						
						[
							LN("sbi.chartengine.configuration.height"),
							heightField.minValue,
							LN('sbi.chartengine.configuration')
						]
					);
				}
			}
						
			
			/**
			 * ********************************************************************
			 * Validate chart's width in the Generic configuration panel on Step 2
			 * ********************************************************************
			 */
			var widthField = mainConfigurationPanel.getComponent("widthFieldset").getComponent("chartWidthNumberfield");
//			var widthField = mainConfigurationPanel.getComponent("chartWidthNumberfield");
			
			if (!widthField.hidden)
			{								
				var widthFieldValue = widthField.value;
				var widthViewModelValue = this.cViewModel.data.configModel.data.width;
							
				if ((widthFieldValue || parseInt(widthFieldValue)==0) 
						&& widthFieldValue!="" && widthFieldValue!=null)
				{
					if (widthFieldValue < widthField.minValue)
					{					
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.width"),
								widthField.minValue,
								LN('sbi.chartengine.configuration')
							]
						);
					}
				}
				else
				{				
					if (widthViewModelValue!=null && widthViewModelValue!="" 
							&& widthViewModelValue < widthField.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.width"),
								widthField.minValue,
								LN('sbi.chartengine.configuration')
							]
						);
					}
				}
			}			

			/**
			 * ************************************************************
			 * Validate chart's opacity on mouse over (Sunburst) in the 
			 * Generic configuration panel on Step 2
			 * ************************************************************
			 */
			if (!mainConfigurationPanel.getComponent("opacityMouseOver").hidden)
			{				
				var opacityOnMouseOverField = mainConfigurationPanel.getComponent("opacityMouseOver").items.items[0];			
				var opacityOnMouseOverValue = opacityOnMouseOverField.value;
				var opacityOnMouseOverViewModel = this.cViewModel.data.configModel.data.opacMouseOver;
																
				if ((opacityOnMouseOverValue || parseInt(opacityOnMouseOverValue)==0) 
						&& opacityOnMouseOverValue!="" && opacityOnMouseOverValue!=null)
				{
					if (parseInt(opacityOnMouseOverValue) < opacityOnMouseOverField.minValue)
					{					
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.sunburst.opacityMouseOver"),
								opacityOnMouseOverField.minValue,
								LN('sbi.chartengine.configuration')
							]
						);
					}
					else if (parseInt(opacityOnMouseOverValue) > opacityOnMouseOverField.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.sunburst.opacityMouseOver"),
								opacityOnMouseOverField.maxValue,
								LN('sbi.chartengine.configuration')
							]
						);
					}
				}
				else
				{		
					if (opacityOnMouseOverViewModel!=null && opacityOnMouseOverViewModel!="" 
							&& opacityOnMouseOverViewModel < opacityOnMouseOverField.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.sunburst.opacityMouseOver"),
								opacityOnMouseOverField.minValue,
								LN('sbi.chartengine.configuration')
							]
						);
					}
					else if (opacityOnMouseOverViewModel!=null && opacityOnMouseOverViewModel!="" 
						 		&& opacityOnMouseOverViewModel > opacityOnMouseOverField.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.sunburst.opacityMouseOver"),
								opacityOnMouseOverField.maxValue,
								LN('sbi.chartengine.configuration')
							]
						);
					}
				}
			}			
			
			/**
			 * ********************************************************************
			 * Validate border width for the Legend panel on Step 2
			 * ********************************************************************
			 */
			if (!Ext.getCmp("chartLegend").hidden)
			{
				var borderWidthLegendField = Ext.getCmp("borderWidthLegend");
				var borderWidthLegendValue = borderWidthLegendField.value;
				var borderWidthViewModelValue = this.cViewModel.data.configModel.data.legendBorderWidth;
				
				if ((borderWidthLegendValue || parseInt(borderWidthLegendValue)==0) 
						&& borderWidthLegendValue!="" && borderWidthLegendValue!=null)
				{
					if (borderWidthLegendValue < borderWidthLegendField.minValue)
					{					
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValueExtended"),
							
							[
								LN("sbi.chartengine.configuration.borderwidth"),
								borderWidthLegendField.minValue,
								LN('sbi.chartengine.configuration.legend'),
								LN('sbi.chartengine.configuration.stylebutton') + " button"
							]
						);
					}
				}
				else
				{
					if (borderWidthViewModelValue!=null && borderWidthViewModelValue!="" 
							&& borderWidthViewModelValue < borderWidthLegendField.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValueExtended"),
							
							[
								LN("sbi.chartengine.configuration.borderwidth"),
								borderWidthLegendField.minValue,
								LN('sbi.chartengine.configuration.legend'),
								LN('sbi.chartengine.configuration.stylebutton') + " button"
							]
						);
					}
				}
			}						
			
			/**
			 * only numerical values (fields) from panels 
			 */
			var checkParamValuesForCharts = 
			{
				heatmap:
				{
					legend:
					{
						symbolHeight:
						{
							minValue: Ext.getCmp("heatmapLegendSymbolHeight").minValue,
							maxValue: Ext.getCmp("heatmapLegendSymbolHeight").maxValue
						}
					}					
				},
				
				parallel:
				{
					limit:
					{
						maxNumbOfRec:
						{
							minValue: Ext.getCmp("parallelLimitMaxNumbOfRec").minValue,
							maxValue: Ext.getCmp("parallelLimitMaxNumbOfRec").maxValue
						}						
					},
					
					axesLines:
					{
						axisColNamePadd:
						{
							minValue: Ext.getCmp("parallelAxisColNamePadd").minValue,
							maxValue: Ext.getCmp("parallelAxisColNamePadd").maxValue
						},
						
						brushWidth:
						{
							minValue: Ext.getCmp("parallelBrushWidth").minValue,
							maxValue: Ext.getCmp("parallelBrushWidth").maxValue
						}
					},
					
					tooltip:
					{
						/*minWidth:
						{
							minValue: Ext.getCmp("parallelTooltipMinWidth").minValue,
							maxValue: Ext.getCmp("parallelTooltipMinWidth").maxValue
						},
						
						maxWidth:
						{
							minValue: Ext.getCmp("parallelTooltipMaxWidth").minValue,
							maxValue: Ext.getCmp("parallelTooltipMaxWidth").maxValue
						},
						
						minHeight:
						{
							minValue: Ext.getCmp("parallelTooltipMinHeight").minValue,
							maxValue: Ext.getCmp("parallelTooltipMinHeight").maxValue
						},
						
						maxHeight:
						{
							minValue: Ext.getCmp("parallelTooltipMaxHeight").minValue,
							maxValue: Ext.getCmp("parallelTooltipMaxHeight").maxValue
						},
						
						textPadding:
						{
							minValue: Ext.getCmp("parallelTooltipPadding").minValue,
							maxValue: Ext.getCmp("parallelTooltipPadding").maxValue
						},
						*/
						borderWidth:
						{
							minValue: Ext.getCmp("parallelTooltipBorder").minValue,
							maxValue: Ext.getCmp("parallelTooltipBorder").maxValue
						},
						
						borderRadius:
						{
							minValue: Ext.getCmp("parallelTooltipBorderRadius").minValue,
							maxValue: Ext.getCmp("parallelTooltipBorderRadius").maxValue
						}
					}
				},
				
				chord:{
					tooltip:
					{
						borderWidth:
						{
							minValue: Ext.getCmp("parallelTooltipBorder").minValue,
							maxValue: Ext.getCmp("parallelTooltipBorder").maxValue
						},
						
						borderRadius:
						{
							minValue: Ext.getCmp("parallelTooltipBorderRadius").minValue,
							maxValue: Ext.getCmp("parallelTooltipBorderRadius").maxValue
						}
					}
				},
				
				sunburst:
				{
					toolbar:
					{
						spacing:
						{
							minValue: Ext.getCmp("sunburstToolbarSpacing").minValue,
							maxValue: Ext.getCmp("sunburstToolbarSpacing").maxValue
						},
						
						tail:
						{
							minValue: Ext.getCmp("sunburstToolbarTail").minValue,
							maxValue: Ext.getCmp("sunburstToolbarTail").maxValue
						},
						
						height:
						{
							minValue: Ext.getCmp("sunburstToolbarHeight").minValue,
							maxValue: Ext.getCmp("sunburstToolbarHeight").maxValue
						},
						
						width:
						{
							minValue: Ext.getCmp("sunburstToolbarWidth").minValue,
							maxValue: Ext.getCmp("sunburstToolbarWidth").maxValue
						}
					},
					
					tip:
					{
						width:
						{
							minValue: Ext.getCmp("sunburstTipWidth").minValue,
							maxValue: Ext.getCmp("sunburstTipWidth").maxValue
						}
					}
				},
				
				wordcloud:
				{
					maxNumOfWords:
					{
						minValue: Ext.getCmp("wordcloudMaxWords").minValue,
						maxValue: Ext.getCmp("wordcloudMaxWords").maxValue
					},
					
					maxWordAngle:
					{
						minValue: Ext.getCmp("wordcloudMaxAngle").minValue,
						maxValue: Ext.getCmp("wordcloudMaxAngle").maxValue
					},
					
					minWordAngle:
					{
						minValue: Ext.getCmp("wordcloudMinAngle").minValue,
						maxValue: Ext.getCmp("wordcloudMinAngle").maxValue
					},
					
					maxFontSize:
					{
						minValue: Ext.getCmp("wordcloudMaxFontSize").minValue,
						maxValue: Ext.getCmp("wordcloudMaxFontSize").maxValue
					},
					
					minFontSize:
					{
						minValue: Ext.getCmp("wordcloudMinFontSize").minValue,
						maxValue: Ext.getCmp("wordcloudMinFontSize").maxValue
					},
					
					wordPadding:
					{
						minValue: Ext.getCmp("wordcloudWordPadding").minValue,
						maxValue: Ext.getCmp("wordcloudWordPadding").maxValue
					}
				},
				
				gauge:
				{
					startAnglePane:
					{
						minValue: Ext.getCmp("gaugeStartAnglePane").minValue,
						maxValue: Ext.getCmp("gaugeStartAnglePane").maxValue
					},
					
					endAnglePane:
					{
						minValue: Ext.getCmp("gaugeEndAnglePane").minValue,
						maxValue: Ext.getCmp("gaugeEndAnglePane").maxValue
					}
				}
			};
			
			/**
			 * PIE, PARALLEL, HEATMAP chart: Instead of forcing user to specify at least one or two colors in the color pallete, 
			 * we provided  possibility for him not to specify any color while the VM will take care of this and dedicate predefined 
			 * color as set of colors for the PIE chart.
			 * 
			 * @commentBy: danristo (danilo.ristovski@mht.net)
			 */
			
			/**
			 * Validation for Step 2 if the mandatory fields are not defined for particular chart type.
			 * 
			 * Two ways of getting data about parameters: 
			 * 		(1) through the corresponding GUI element
			 * 		(2) through the corresponding property inside of the cModel
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			if (chartType == "GAUGE") {								
				
				/**
				 * STEP 2 -> Pane panel
				 */	
				var gaugeStartAnglePaneGUI = Ext.getCmp("gaugeStartAnglePane").value;
				var gaugeEndAnglePaneGUI = Ext.getCmp("gaugeEndAnglePane").value;
				
				var gaugeStartAnglePaneCModel = chartViewModelData.startAnglePane;
				var gaugeEndAnglePaneCModel = chartViewModelData.endAnglePane;
				
				if (gaugeStartAnglePaneGUI != null)
				{					
					if (gaugeStartAnglePaneGUI < checkParamValuesForCharts.gauge.startAnglePane.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.gauge.startAnglePane"),
								checkParamValuesForCharts.gauge.startAnglePane.minValue,
								LN('sbi.chartengine.configuration.gauge.panelTitle')
							]
						);
					}
					else if (gaugeStartAnglePaneGUI > checkParamValuesForCharts.gauge.startAnglePane.maxValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.gauge.startAnglePane"),
								checkParamValuesForCharts.gauge.startAnglePane.maxValue,
								LN('sbi.chartengine.configuration.gauge.panelTitle')
							]
						);
					}
				}
				
				if (gaugeEndAnglePaneGUI != null) 
				{					
					if (gaugeEndAnglePaneGUI < checkParamValuesForCharts.gauge.endAnglePane.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.gauge.endAnglePane"),
								checkParamValuesForCharts.gauge.endAnglePane.minValue,
								LN('sbi.chartengine.configuration.gauge.panelTitle')
							]
						);
					}
					else if (gaugeEndAnglePaneGUI > checkParamValuesForCharts.gauge.endAnglePane.maxValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.gauge.endAnglePane"),
								checkParamValuesForCharts.gauge.endAnglePane.maxValue,
								LN('sbi.chartengine.configuration.gauge.panelTitle')
							]
						);
					}
				}
				
				if ((gaugeEndAnglePaneGUI!=null && gaugeStartAnglePaneGUI!=null) || (gaugeEndAnglePaneCModel!="" && gaugeStartAnglePaneCModel!=""))
				{
					if ((gaugeEndAnglePaneGUI < gaugeStartAnglePaneGUI) || (gaugeEndAnglePaneCModel < gaugeStartAnglePaneCModel))
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.gauge.pane.startAngleBiggerThanEndAngle"),
							
							[
								LN("sbi.chartengine.configuration.gauge.startAnglePane"),
								LN("sbi.chartengine.configuration.gauge.endAnglePane"),
								LN('sbi.chartengine.configuration.gauge.panelTitle')
							]
						);	
					}
				}
			}		
			
			else if(chartType == "PARALLEL") {
								
				var parallelLimit = Ext.getCmp("chartParallelLimit");
				var parallelAxesLines = Ext.getCmp("chartParallelAxesLines");
				var parallelTooltip = Ext.getCmp("chartParallelTooltip");
				
				/* =================================================================== */
				/* PARALLEL fields (parameters) values from the GUI - NUMERICAL VALUES */
				/* =================================================================== */
				// == Limit panel ==
				var parallelLimitMaxNumOfRecGUI = parallelLimit.maxNumberOfLines.value;
				// == Axes lines panel ==
				var parallelAxesLinesAxisColNamePaddGUI = parallelAxesLines.axisColNamePadd.value;
				var parallelAxesLinesBrushWidthGUI = parallelAxesLines.brushWidth.value;
				// == Tooltip panel ==
//				var parallelTooltipMinWidthGUI = parallelTooltip.tooltipMinWidth.value;
//				var parallelTooltipMaxWidthGUI = parallelTooltip.tooltipMaxWidth.value;
//				var parallelTooltipMinHeighGUI = parallelTooltip.tooltipMinHeight.value;
//				var parallelTooltipMaxHeightGUI = parallelTooltip.tooltipMaxHeight.value;
//				var parallelTooltipPaddingGUI = parallelTooltip.tooltipPadding.value;
				var parallelTooltipBorderGUI = parallelTooltip.tooltipBorder.value;
				var parallelTooltipBorderRadiusGUI = parallelTooltip.tooltipBorderRadius.value;
						
				/* ================================================================================= */
				/* PARALLEL fields (parameters) values from the chart model	- NON-NUMERICAL VALUES	 */
				/* ================================================================================= */
				// == Limit panel ==
				var parallelLimitMaxNumOfLinesCModel = chartViewModelData.maxNumberOfLines;
				var parallelLimitSerieAsFiltColCModel = chartViewModelData.serieFilterColumn;
				var parallelLimitOrderTopMinBottMaxCModel = chartViewModelData.orderTopMinBottomMax;
				// == Axes lines panel ==
				var parallelAxesLinesAxisColorCModel = chartViewModelData.axisColor;
				var parallelAxesLinesAxisColNamePaddCModel = chartViewModelData.axisColNamePadd;
				var parallelAxesLinesBrushColorCModel = chartViewModelData.brushColor;
				var parallelAxesLinesBrushWidthCModel = chartViewModelData.brushWidth;
				// == Tooltip panel ==
				var parallelTooltipFontFamilyCModel = chartViewModelData.parallelTooltipFontFamily;
				var parallelTooltipFontSizeCModel = chartViewModelData.parallelTooltipFontSize;
				//var parallelTooltipMinWidthCModel = chartViewModelData.parallelTooltipMinWidth;
				//var parallelTooltipMaxWidthCModel = chartViewModelData.parallelTooltipMaxWidth;
				//var parallelTooltipMinHeightCModel = chartViewModelData.parallelTooltipMinHeight;
				//var parallelTooltipMaxHeightCModel = chartViewModelData.parallelTooltipMaxHeight;
				//var parallelTooltipPaddingCModel = chartViewModelData.parallelTooltipPadding;
				var parallelTooltipBorderCModel = chartViewModelData.parallelTooltipBorder;
				var parallelTooltipBorderRadiusCModel = chartViewModelData.parallelTooltipBorderRadius;
				// == Legend title configuration panel ==
				var parallelLegendTitleFontFamilyCModel = chartViewModelData.parallelLegendTitleFontFamily;
				var parallelLegendTitleFontSizeCModel = chartViewModelData.parallelLegendTitleFontSize;
				var parallelLegendTitleFontStyleCModel = chartViewModelData.parallelLegendTitleFontWeight;				
				//  == Legend element configuration panel ==
				var parallelLegendElementFontFamilyCModel = chartViewModelData.parallelLegendElementFontFamily;
				var parallelLegendElementFontSizeCModel = chartViewModelData.parallelLegendElementFontSize;
				var parallelLegendElementFontStyleCModel = chartViewModelData.parallelLegendElementFontWeight;
				
				/**
				 * STEP 2 -> Limit panel
				 */
				(parallelLimitSerieAsFiltColCModel=="" || parallelLimitSerieAsFiltColCModel==null || parallelLimitSerieAsFiltColCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.limit.serieFilterColumn"),
								LN("sbi.chartengine.configuration.parallel.limit.title")
							]
						) : errorMsg;
				
				if (parallelLimitMaxNumOfRecGUI == null)
				{
					if (parallelLimitMaxNumOfLinesCModel == null || parallelLimitMaxNumOfLinesCModel=="")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.limit.maxNumberOfLines"),
								LN("sbi.chartengine.configuration.parallel.limit.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelLimitMaxNumOfRecGUI < checkParamValuesForCharts.parallel.limit.maxNumbOfRec.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.parallel.limit.maxNumberOfLines"),
								checkParamValuesForCharts.parallel.limit.maxNumbOfRec.minValue,
								LN("sbi.chartengine.configuration.parallel.limit.title")
							]
						);
					}
					else if (parallelLimitMaxNumOfRecGUI > checkParamValuesForCharts.parallel.limit.maxNumbOfRec.maxValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.parallel.limit.maxNumberOfLines"),
								checkParamValuesForCharts.parallel.limit.maxNumbOfRec.maxValue,
								LN("sbi.chartengine.configuration.parallel.limit.title")
							]
						);
					}
				}	
				
				(parallelLimitOrderTopMinBottMaxCModel=="" || parallelLimitOrderTopMinBottMaxCModel==null || parallelLimitOrderTopMinBottMaxCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.limit.orderTopMinBottomMax"),
								LN("sbi.chartengine.configuration.parallel.limit.title")
							]
						) : errorMsg;
				
				/**
				 * STEP 2 -> Axes lines panel
				 */
				(chartViewModelData.axisColor=="transparent" || chartViewModelData.axisColor=="" || chartViewModelData.axisColor==null || chartViewModelData.axisColor==undefined || chartViewModelData.axisColor=="transparent") ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.axisColor"),
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						) : errorMsg;						
				
				if (parallelAxesLinesAxisColNamePaddGUI == null)
				{
					if (parallelAxesLinesAxisColNamePaddCModel == null || parallelAxesLinesAxisColNamePaddCModel=="")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd"),
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						);	
					}						
				}
				else 
				{					
					if (parallelAxesLinesAxisColNamePaddGUI < checkParamValuesForCharts.parallel.axesLines.axisColNamePadd.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd"),
								checkParamValuesForCharts.parallel.axesLines.axisColNamePadd.minValue,
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						);
					}
					else if (parallelAxesLinesAxisColNamePaddGUI > checkParamValuesForCharts.parallel.axesLines.axisColNamePadd.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.axisColNamePadd"),
								checkParamValuesForCharts.parallel.axesLines.axisColNamePadd.maxValue,
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						);
					}
				}
				
				(chartViewModelData.brushColor=="transparent" || chartViewModelData.brushColor=="" || chartViewModelData.brushColor==null || chartViewModelData.brushColor==undefined || chartViewModelData.brushColor=="transparent") ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.brushColor"),
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						) : errorMsg;
				
				if (parallelAxesLinesBrushWidthGUI == null)
				{
					if (parallelAxesLinesBrushWidthCModel == null || parallelAxesLinesBrushWidthCModel == "")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth"),
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelAxesLinesBrushWidthGUI < checkParamValuesForCharts.parallel.axesLines.brushWidth.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth"),
								checkParamValuesForCharts.parallel.axesLines.brushWidth.minValue,
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						);
					}
					else if (parallelAxesLinesBrushWidthGUI > checkParamValuesForCharts.parallel.axesLines.brushWidth.maxValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN("sbi.chartengine.configuration.parallel.axesLines.brushWidth"),
								checkParamValuesForCharts.parallel.axesLines.brushWidth.maxValue,
								LN("sbi.chartengine.configuration.parallel.axesLines.title")
							]
						);
					}
				}
				
				/**
				 * STEP 2 -> Tooltip panel
				 */
				(chartViewModelData.parallelTooltipFontFamily=="" || chartViewModelData.parallelTooltipFontFamily==null || chartViewModelData.parallelTooltipFontFamily==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.font'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						) : errorMsg;
								
				(chartViewModelData.parallelTooltipFontSize=="" || chartViewModelData.parallelTooltipFontSize==null || chartViewModelData.parallelTooltipFontSize==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontsize'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						) : errorMsg;
				
				/*/if (parallelTooltipMinWidthGUI == null)
				{
					if (parallelTooltipMinWidthCModel == null || parallelTooltipMinWidthCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinWidth'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					/*if (parallelTooltipMinWidthGUI < checkParamValuesForCharts.parallel.tooltip.minWidth.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinWidth'),
								checkParamValuesForCharts.parallel.tooltip.minWidth.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipMinWidthGUI > checkParamValuesForCharts.parallel.tooltip.minWidth.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinWidth'),
								checkParamValuesForCharts.parallel.tooltip.minWidth.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					} */
				//} 
				
				/*if (parallelTooltipMaxWidthGUI == null)
				{
					if (parallelTooltipMaxWidthCModel == null || parallelTooltipMaxWidthCModel=="")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxWidth'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipMaxWidthGUI < checkParamValuesForCharts.parallel.tooltip.maxWidth.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxWidth'),
								checkParamValuesForCharts.parallel.tooltip.maxWidth.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipMaxWidthGUI > checkParamValuesForCharts.parallel.tooltip.maxWidth.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxWidth'),
								checkParamValuesForCharts.parallel.tooltip.maxWidth.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				} */
				
				/*if (parallelTooltipMinHeighGUI == null)
				{
					if (parallelTooltipMinHeightCModel == null || parallelTooltipMinHeightCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinHeight'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipMinHeighGUI < checkParamValuesForCharts.parallel.tooltip.minHeight.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinHeight'),
								checkParamValuesForCharts.parallel.tooltip.minHeight.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipMinHeighGUI > checkParamValuesForCharts.parallel.tooltip.minHeight.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMinHeight'),
								checkParamValuesForCharts.parallel.tooltip.minHeight.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}*/
				
				/*if (parallelTooltipMaxHeightGUI == null)
				{
					if (parallelTooltipMaxHeightCModel == null || parallelTooltipMaxHeightCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxHeight'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipMaxHeightGUI < checkParamValuesForCharts.parallel.tooltip.maxHeight.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxHeight'),
								checkParamValuesForCharts.parallel.tooltip.maxHeight.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipMaxHeightGUI > checkParamValuesForCharts.parallel.tooltip.maxHeight.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipMaxHeight'),
								checkParamValuesForCharts.parallel.tooltip.maxHeight.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}*/
				
				/*if (parallelTooltipPaddingGUI == null)
				{
					if (parallelTooltipPaddingCModel == null || parallelTooltipPaddingCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipPadding'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipPaddingGUI < checkParamValuesForCharts.parallel.tooltip.textPadding.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipPadding'),
								checkParamValuesForCharts.parallel.tooltip.textPadding.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipPaddingGUI > checkParamValuesForCharts.parallel.tooltip.textPadding.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipPadding'),
								checkParamValuesForCharts.parallel.tooltip.textPadding.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}*/
					
				if (parallelTooltipBorderGUI == null)
				{
					if (parallelTooltipBorderCModel == null || parallelTooltipBorderCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipBorderGUI < checkParamValuesForCharts.parallel.tooltip.borderWidth.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder'),
								checkParamValuesForCharts.parallel.tooltip.borderWidth.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipBorderGUI > checkParamValuesForCharts.parallel.tooltip.borderWidth.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder'),
								checkParamValuesForCharts.parallel.tooltip.borderWidth.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}
				
				if (parallelTooltipBorderRadiusGUI == null)
				{
					if (parallelTooltipBorderRadiusCModel == null || parallelTooltipBorderRadiusCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipBorderRadiusGUI < checkParamValuesForCharts.parallel.tooltip.borderRadius.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius'),
								checkParamValuesForCharts.parallel.tooltip.borderRadius.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipBorderRadiusGUI > checkParamValuesForCharts.parallel.tooltip.borderRadius.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius'),
								checkParamValuesForCharts.parallel.tooltip.borderRadius.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}
				
				/**
				 * STEP 2 -> Legend panel (Title and Element button)
				 */
				
				(parallelLegendTitleFontFamilyCModel=="" || parallelLegendTitleFontFamilyCModel==null || parallelLegendTitleFontFamilyCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.font'),
								LN("sbi.chartengine.configuration.parallel.legendTitlePanel.title")
							]
						) : errorMsg;
							
				(parallelLegendTitleFontSizeCModel=="" || parallelLegendTitleFontSizeCModel==null || parallelLegendTitleFontSizeCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontsize'),
								LN("sbi.chartengine.configuration.parallel.legendTitlePanel.title")
							]
						) : errorMsg;	
				
				(parallelLegendTitleFontStyleCModel=="" || parallelLegendTitleFontStyleCModel==null || parallelLegendTitleFontStyleCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontstyle'),
								LN("sbi.chartengine.configuration.parallel.legendTitlePanel.title")
							]
						) : errorMsg;							
			
				(parallelLegendElementFontFamilyCModel=="" || parallelLegendElementFontFamilyCModel==null || parallelLegendElementFontFamilyCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.font'),
								LN("sbi.chartengine.configuration.parallel.legendElementPanel.title")
							]
						) : errorMsg;
				
				(parallelLegendElementFontSizeCModel=="" || parallelLegendElementFontSizeCModel==null || parallelLegendElementFontSizeCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontsize'),
								LN("sbi.chartengine.configuration.parallel.legendElementPanel.title")
							]
						) : errorMsg;				
				
				(parallelLegendElementFontStyleCModel=="" || parallelLegendElementFontStyleCModel==null || parallelLegendElementFontStyleCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontstyle'),
								LN("sbi.chartengine.configuration.parallel.legendElementPanel.title")
							]
						) : errorMsg;			
							
							
				/**
				 * STEP 2 -> Palette panel
				 */
//				var itemsIncolorPalette = Ext.getCmp("chartColorPalette").paletteGrid.getStore().data.length;
//				
//				(itemsIncolorPalette < 2) ? 
//						errorMsg += "- " + "Color palette needs at least 2 colors [Step 2 -> Palette panel]" + '<br>' : errorMsg;	
			}
			else if(chartType=="CHORD"){
				var parallelTooltip = Ext.getCmp("chartParallelTooltip");
				
				//=====
				var parallelTooltipBorderGUI = parallelTooltip.tooltipBorder.value;
				var parallelTooltipBorderRadiusGUI = parallelTooltip.tooltipBorderRadius.value;
				//==========
				var parallelTooltipFontFamilyCModel = chartViewModelData.parallelTooltipFontFamily;
				var parallelTooltipFontSizeCModel = chartViewModelData.parallelTooltipFontSize;
				var parallelTooltipBorderCModel = chartViewModelData.parallelTooltipBorder;
				var parallelTooltipBorderRadiusCModel = chartViewModelData.parallelTooltipBorderRadius;
				
				if (parallelTooltipBorderGUI == null)
				{
					if (parallelTooltipBorderCModel == null || parallelTooltipBorderCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipBorderGUI < checkParamValuesForCharts.parallel.tooltip.borderWidth.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder'),
								checkParamValuesForCharts.parallel.tooltip.borderWidth.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipBorderGUI > checkParamValuesForCharts.parallel.tooltip.borderWidth.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorder'),
								checkParamValuesForCharts.parallel.tooltip.borderWidth.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}
				
				if (parallelTooltipBorderRadiusGUI == null)
				{
					if (parallelTooltipBorderRadiusCModel == null || parallelTooltipBorderRadiusCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius'),
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}						
				}
				else 
				{					
					if (parallelTooltipBorderRadiusGUI < checkParamValuesForCharts.parallel.tooltip.borderRadius.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius'),
								checkParamValuesForCharts.parallel.tooltip.borderRadius.minValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
					else if (parallelTooltipBorderRadiusGUI > checkParamValuesForCharts.parallel.tooltip.borderRadius.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.parallel.tooltip.parallelTooltipBorderRadius'),
								checkParamValuesForCharts.parallel.tooltip.borderRadius.maxValue,
								LN("sbi.chartengine.configuration.parallel.tooltip.title")
							]
						);
					}
				}	
			
			
			}
			
			else if (chartType == "SUNBURST") {
				
//				var sunburstToolbar = Ext.getCmp("chartToolbarAndTip").stylePopupToolbar;
//				var sunburstTip = Ext.getCmp("chartToolbarAndTip").stylePopupTip;				
				
				var sunburstToolbar = Ext.getCmp("chartToolbar");
				var sunburstTip = Ext.getCmp("chartTip");
				
				/* =================================================================== */
				/* SUNBURST fields (parameters) values from the GUI - NUMERICAL VALUES */
				/* =================================================================== */
				// == Toolbar and tip panel : Toolbar button ==
				var sunburstToolbarSpacingGUI = Ext.getCmp("sunburstToolbarSpacing").value;
				var sunburstToolbarTailGUI = Ext.getCmp("sunburstToolbarTail").value;
				var sunburstToolbarHeightGUI = Ext.getCmp("sunburstToolbarHeight").value;
				var sunburstToolbarWidthGUI = Ext.getCmp("sunburstToolbarWidth").value;
				// == Toolbar and tip panel : Tip button ==
				var sunburstTipWidthGUI = Ext.getCmp("sunburstTipWidth").value;
				
				/* ================================================================================= */
				/* SUNBURST fields (parameters) values from the chart model	- NON-NUMERICAL VALUES	 */
				/* ================================================================================= */
				// == Toolbar and tip panel : Toolbar button ==
				var sunburstToolbarSpacingCModel = chartViewModelData.toolbarSpacing;
				var sunburstToolbarTailCModel = chartViewModelData.toolbarTail;
				var sunburstToolbarHeightCModel = chartViewModelData.toolbarHeight;
				var sunburstToolbarWidthCModel = chartViewModelData.toolbarWidth;
				// == Toolbar and tip panel : Tip button ==
				var sunburstTipFontWeightCModel = chartViewModelData.tipFontWeight;
				var sunburstTipColorCModel = chartViewModelData.tipColor;
				var sunburstTipFontSizeCModel = chartViewModelData.tipFontSize;
				var sunburstTipFontFamilyCModel = chartViewModelData.tipFontFamily;
				var sunburstTipWidthCModel = chartViewModelData.tipWidth;
				var sunburstTipTextCModel = chartViewModelData.tipText;	
				
				/**
				 * STEP 2 -> Toolbar configuration
				 */			
				
				(chartViewModelData.toolbarPosition=="" || chartViewModelData.toolbarPosition==null || chartViewModelData.toolbarPosition==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.position'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						)  : errorMsg;	
				
				if (sunburstToolbarSpacingGUI == null)
				{
					if (sunburstToolbarSpacingCModel == null || sunburstToolbarSpacingCModel=="")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.spacing'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}						
				}
				else 
				{					
					if (sunburstToolbarSpacingGUI < checkParamValuesForCharts.sunburst.toolbar.spacing.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.minValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.spacing'),
								checkParamValuesForCharts.sunburst.toolbar.spacing.minValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
					else if (sunburstToolbarSpacingGUI > checkParamValuesForCharts.sunburst.toolbar.spacing.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.maxValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.spacing'),
								checkParamValuesForCharts.sunburst.toolbar.spacing.maxValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
				}
				
				if (sunburstToolbarTailGUI == null)
				{
					if (sunburstToolbarTailCModel == null || sunburstToolbarTailCModel=="")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.tail'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}						
				}
				else 
				{					
					if (sunburstToolbarTailGUI < checkParamValuesForCharts.sunburst.toolbar.tail.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.minValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.tail'),
								checkParamValuesForCharts.sunburst.toolbar.tail.minValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
					else if (sunburstToolbarTailGUI > checkParamValuesForCharts.sunburst.toolbar.tail.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.maxValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.tail'),
								checkParamValuesForCharts.sunburst.toolbar.tail.maxValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
				}
				
				if (sunburstToolbarHeightGUI == null)
				{
					if (sunburstToolbarHeightCModel == null || sunburstToolbarHeightCModel == "")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.height'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}						
				}
				else 
				{					
					if (sunburstToolbarHeightGUI < checkParamValuesForCharts.sunburst.toolbar.height.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.minValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.height'),
								checkParamValuesForCharts.sunburst.toolbar.height.minValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
					else if (sunburstToolbarHeightGUI > checkParamValuesForCharts.sunburst.toolbar.height.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.maxValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.height'),
								checkParamValuesForCharts.sunburst.toolbar.height.maxValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
				}
				
				if (sunburstToolbarWidthGUI == null)
				{
					if (sunburstToolbarWidthCModel == null || sunburstToolbarWidthCModel == "")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.width'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}										
				}
				else 
				{					
					if (sunburstToolbarWidthGUI < checkParamValuesForCharts.sunburst.toolbar.width.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.minValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.width'),
								checkParamValuesForCharts.sunburst.toolbar.width.minValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
					else if (sunburstToolbarWidthGUI > checkParamValuesForCharts.sunburst.toolbar.width.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.maxValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.width'),
								checkParamValuesForCharts.sunburst.toolbar.width.maxValue,
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
				}
				
				(chartViewModelData.toolbarPercFontColor=="transparent" || chartViewModelData.toolbarPercFontColor=="" || chartViewModelData.toolbarPercFontColor==null || chartViewModelData.toolbarPercFontColor==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.toolbar.percentageColor'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						) : errorMsg;
							
							
							
				(chartViewModelData.toolbarFontFamily=="" || chartViewModelData.toolbarFontFamily==null || chartViewModelData.toolbarFontFamily==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.font'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						) : errorMsg;
							
				(chartViewModelData.toolbarFontWeight=="" || chartViewModelData.toolbarFontWeight==null || chartViewModelData.toolbarFontWeight==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontstyle'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//											LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						) : errorMsg;
							
				(chartViewModelData.toolbarFontSize=="" || chartViewModelData.toolbarFontSize==null || chartViewModelData.toolbarFontSize==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontsize'),
								LN("sbi.chartengine.configuration.sunburst.toolbarConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						) : errorMsg;
				
				/**
				 * STEP 2 -> Tip configuration
				 */		
							
				(chartViewModelData.tipFontWeight=="" || chartViewModelData.tipFontWeight==null || chartViewModelData.tipFontWeight==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontstyle'),
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//														LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						) : errorMsg;

				(sunburstTipColorCModel=="transparent" || sunburstTipColorCModel=="" || sunburstTipColorCModel==null || sunburstTipColorCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.tip.fontColor'),
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.tipPopup.title")
							]
						) : errorMsg;
							
				(sunburstTipFontSizeCModel=="" || sunburstTipFontSizeCModel==null || sunburstTipFontSizeCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.fontsize'),
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.tipPopup.title")
							]
						) : errorMsg;
							
				(sunburstTipFontFamilyCModel=="" || sunburstTipFontFamilyCModel==null || sunburstTipFontFamilyCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.font'),
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.tipPopup.title")
							]
						) : errorMsg;
								
				if (sunburstTipWidthGUI == null)
				{
					if (sunburstTipWidthCModel == null || sunburstTipWidthCModel=="")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.tip.width'),
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.tipPopup.title")
							]
						);
					}						
				}
				else 
				{					
					if (sunburstTipWidthGUI < checkParamValuesForCharts.sunburst.tip.width.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.minValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.tip.width'),
								checkParamValuesForCharts.sunburst.tip.width.minValue,
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
					else if (sunburstTipWidthGUI > checkParamValuesForCharts.sunburst.tip.width.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.maxValueExtended"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.tip.width'),
								checkParamValuesForCharts.sunburst.tip.width.maxValue,
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.toolbarPopup.title")
							]
						);
					}
				}
				
				(sunburstTipTextCModel=="" || sunburstTipTextCModel==null || sunburstTipTextCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.sunburst.toolbarAndTip.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.sunburst.tip.text'),
								LN("sbi.chartengine.configuration.sunburst.tipConfigurationPanel.title"),
//								LN("sbi.chartengine.configuration.sunburst.toolbarAndTip.tipPopup.title")
							]
						) : errorMsg;			
			}
			
			else if (chartType == "WORDCLOUD") {
				
				/* ==================================================================== */
				/* WORDCLOUD fields (parameters) values from the GUI - NUMERICAL VALUES */
				/* ==================================================================== */
				var wordcloudMaxWordsGUI = Ext.getCmp("wordcloudMaxWords").value;
				var wordcloudMaxAngleGUI = Ext.getCmp("wordcloudMaxAngle").value;
				var wordcloudMinAngleGUI = Ext.getCmp("wordcloudMinAngle").value;
				var wordcloudMaxFontSizeGUI = Ext.getCmp("wordcloudMaxFontSize").value;
				var wordcloudMinFontSizeGUI = Ext.getCmp("wordcloudMinFontSize").value;
				var wordcloudWordPaddingGUI = Ext.getCmp("wordcloudWordPadding").value;
			
				/* ================================================================================= */
				/* WORDCLOUD fields (parameters) values from the chart model - NON-NUMERICAL VALUES	 */
				/* ================================================================================= */
				var wordcloudSizeCriteriaCModel = chartViewModelData.sizeCriteria;
				var wordcloudMaxWordsCModel = chartViewModelData.maxWords;
				var wordcloudWordLayoutCModel = chartViewModelData.wordLayout;
				var wordcloudMaxAngleCModel = chartViewModelData.maxAngle;
				var wordcloudMinAngleCModel = chartViewModelData.minAngle;
				var wordcloudMaxFontSizeCModel = chartViewModelData.maxFontSize;
				var wordcloudMinFontSizeCModel = chartViewModelData.minFontSize;
				var wordcloudWordPaddingCModel = chartViewModelData.wordPadding;
				
				
				var wordcloudMinAngle;
				var wordcloudMaxAngle;
				var wordcloudMaxFont;
				var wordcloudMinFont;
				
				(wordcloudSizeCriteriaCModel=="" || wordcloudSizeCriteriaCModel==null || wordcloudSizeCriteriaCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.sizeCriteria'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						) : errorMsg;
				(wordcloudWordLayoutCModel=="" || wordcloudWordLayoutCModel==null ||wordcloudWordLayoutCModel==undefined) ?
						 errorMsg += Sbi.locale.sobstituteParams
						 (
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
										
							[
								LN('sbi.chartengine.configuration.wordcloud.wordLayout'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						 ) : errorMsg;			
											
				if (wordcloudMaxWordsGUI == null)
				{
					if (wordcloudMaxWordsCModel == null || wordcloudMaxWordsCModel === "")
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxWords'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}		
					else
					{ 
						if (wordcloudMaxWordsCModel < checkParamValuesForCharts.wordcloud.maxNumOfWords.minValue)
						{						
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.minValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.maxWords'),
									checkParamValuesForCharts.wordcloud.maxNumOfWords.minValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
						else if (wordcloudMaxWordsCModel > checkParamValuesForCharts.wordcloud.maxNumOfWords.maxValue)
						{						
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.maxValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.maxWords'),
									checkParamValuesForCharts.wordcloud.maxNumOfWords.maxValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
					}
				}
				else 
				{					
					if (wordcloudMaxWordsGUI < checkParamValuesForCharts.wordcloud.maxNumOfWords.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxWords'),
								checkParamValuesForCharts.wordcloud.maxNumOfWords.minValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else if (wordcloudMaxWordsGUI > checkParamValuesForCharts.wordcloud.maxNumOfWords.maxValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxWords'),
								checkParamValuesForCharts.wordcloud.maxNumOfWords.maxValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
				}
				
				if (wordcloudMaxAngleGUI == null)
				{
					if (wordcloudMaxAngleCModel == null || wordcloudMaxAngleCModel==="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxAngle'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else
					{
						if (wordcloudMaxAngleCModel < checkParamValuesForCharts.wordcloud.maxWordAngle.minValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.minValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.maxAngle'),
									checkParamValuesForCharts.wordcloud.maxWordAngle.minValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
						else if (wordcloudMaxAngleCModel > checkParamValuesForCharts.wordcloud.maxWordAngle.maxValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.maxValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.maxAngle'),
									checkParamValuesForCharts.wordcloud.maxWordAngle.maxValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
					}
					wordcloudMaxAngle=wordcloudMaxAngleCModel;
				}
				else 
				{					
					if (wordcloudMaxAngleGUI < checkParamValuesForCharts.wordcloud.maxWordAngle.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxAngle'),
								checkParamValuesForCharts.wordcloud.maxWordAngle.minValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else if (wordcloudMaxAngleGUI > checkParamValuesForCharts.wordcloud.maxWordAngle.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxAngle'),
								checkParamValuesForCharts.wordcloud.maxWordAngle.maxValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					
					wordcloudMaxAngle=wordcloudMaxAngleGUI;
				}
				
				if (wordcloudMinAngleGUI == null)
				{
					if (wordcloudMinAngleCModel == null || wordcloudMinAngleCModel==="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.minAngle'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}	
					else
					{
						if (wordcloudMinAngleCModel < checkParamValuesForCharts.wordcloud.minWordAngle.minValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.minValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.minAngle'),
									checkParamValuesForCharts.wordcloud.minWordAngle.minValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
						else if (wordcloudMinAngleCModel > checkParamValuesForCharts.wordcloud.minWordAngle.maxValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.maxValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.minAngle'),
									checkParamValuesForCharts.wordcloud.minWordAngle.maxValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
					}
					
					wordcloudMinAngle=wordcloudMinAngleCModel;
				}
				else 
				{					
					if (wordcloudMinAngleGUI < checkParamValuesForCharts.wordcloud.minWordAngle.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.minAngle'),
								checkParamValuesForCharts.wordcloud.minWordAngle.minValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else if (wordcloudMinAngleGUI > checkParamValuesForCharts.wordcloud.minWordAngle.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.minAngle'),
								checkParamValuesForCharts.wordcloud.minWordAngle.maxValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					
					wordcloudMinAngle=wordcloudMinAngleCModel;
				}
				
			    if(Number(wordcloudMinAngle) > Number(wordcloudMaxAngle))
			    {
				     errorMsg += Sbi.locale.sobstituteParams
				     (
					      LN("sbi.chartengine.validation.configuration.notGreater"),
					      
					      [
						       LN('sbi.chartengine.configuration.wordcloud.minAngle'),
						       LN('sbi.chartengine.configuration.wordcloud.maxAngle'),
						       LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
					      ]
				     );			     
			    }
				
				if (wordcloudMaxFontSizeGUI == null)
				{
					wordcloudMaxFont=wordcloudMaxFontSizeCModel;
					if (wordcloudMaxFontSizeCModel == null || wordcloudMaxFontSizeCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxFontSize'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}	
					else
					{
						if (wordcloudMaxFontSizeCModel < checkParamValuesForCharts.wordcloud.maxFontSize.minValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.minValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
									checkParamValuesForCharts.wordcloud.maxFontSize.minValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
						else if (wordcloudMaxFontSizeCModel > checkParamValuesForCharts.wordcloud.maxFontSize.maxValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.maxValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.maxFontSize'),
									checkParamValuesForCharts.wordcloud.maxFontSize.maxValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
					}
				}
				else 
				{					
					wordcloudMaxFont=wordcloudMaxFontSizeGUI;
					if (wordcloudMaxFontSizeGUI < checkParamValuesForCharts.wordcloud.maxFontSize.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxFontSize'),
								checkParamValuesForCharts.wordcloud.maxFontSize.minValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else if (wordcloudMaxFontSizeGUI > checkParamValuesForCharts.wordcloud.maxFontSize.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.maxFontSize'),
								checkParamValuesForCharts.wordcloud.maxFontSize.maxValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
				}				
				
				if (wordcloudMinFontSizeGUI == null)
				{
					wordcloudMinFont=wordcloudMinFontSizeCModel;
					if (wordcloudMinFontSizeCModel == null || wordcloudMinFontSizeCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}	
					else
					{
						if (wordcloudMinFontSizeCModel < checkParamValuesForCharts.wordcloud.minFontSize.minValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.minValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
									checkParamValuesForCharts.wordcloud.maxFontSize.minValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
						else if (wordcloudMinFontSizeCModel > checkParamValuesForCharts.wordcloud.minFontSize.maxValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.maxValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
									checkParamValuesForCharts.wordcloud.minFontSize.maxValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
					}
				}
				else 
				{	
					wordcloudMinFont=wordcloudMinFontSizeGUI;
					if (wordcloudMinFontSizeGUI < checkParamValuesForCharts.wordcloud.minFontSize.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
								checkParamValuesForCharts.wordcloud.minFontSize.minValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else if (wordcloudMinFontSizeGUI > checkParamValuesForCharts.wordcloud.minFontSize.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
								checkParamValuesForCharts.wordcloud.minFontSize.maxValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
				}
				
				 if(wordcloudMinFont > wordcloudMaxFont)
				    {
					     errorMsg += Sbi.locale.sobstituteParams
					     (
						      LN("sbi.chartengine.validation.configuration.notGreater"),
						      
						      [
							       LN('sbi.chartengine.configuration.wordcloud.minFontSize'),
							       LN('sbi.chartengine.configuration.wordcloud.maxFontSize'),
							       LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
						      ]
					     );			     
				    }
				
				
				
				if (wordcloudWordPaddingGUI == null)
				{
					if (wordcloudWordPaddingCModel == null || wordcloudWordPaddingCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.wordPadding'),
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else
					{
						if (wordcloudWordPaddingCModel < checkParamValuesForCharts.wordcloud.wordPadding.minValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.minValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.wordPadding'),
									checkParamValuesForCharts.wordcloud.wordPadding.minValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
						else if (wordcloudWordPaddingCModel > checkParamValuesForCharts.wordcloud.wordPadding.maxValue)
						{
							errorMsg += Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.validation.configuration.maxValue"),
								
								[
									LN('sbi.chartengine.configuration.wordcloud.wordPadding'),
									checkParamValuesForCharts.wordcloud.wordPadding.maxValue,
									LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
								]
							);
						}
					}
				}
				else 
				{					
					if (wordcloudWordPaddingGUI < checkParamValuesForCharts.wordcloud.wordPadding.minValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.wordPadding'),
								checkParamValuesForCharts.wordcloud.wordPadding.minValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
					else if (wordcloudWordPaddingGUI > checkParamValuesForCharts.wordcloud.wordPadding.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.wordcloud.wordPadding'),
								checkParamValuesForCharts.wordcloud.wordPadding.maxValue,
								LN("sbi.chartengine.configuration.wordcloud.configPanelTitle")
							]
						);
					}
				}
			}
			
			else if (chartType == "HEATMAP") {
//				
//				var heatmapLegend = Ext.getCmp("chartHeatmapLegendAndTooltip").heatmapChartLegend;
//				var heatmapTooltip = Ext.getCmp("chartHeatmapLegendAndTooltip").heatmapChartTooltip;
				
				var heatmapLegend = Ext.getCmp("chartHeatmapLegend");
				var heatmapTooltip = Ext.getCmp("chartHeatmapTooltip");
				
				// HEATMAP fields (parameters) values from the GUI 	
				var heatmapLegendVertAlignGUI = heatmapLegend.items.items[0].value;
				var heatmapLegendSymbolHeightGUI = heatmapLegend.items.items[1].value;
//				var heatmapTooltipFontFamilyGUI = heatmapTooltip.items.items[0].value;
//				var heatmapTooltipFontSizeGUI = heatmapTooltip.items.items[1].value;
//				var heatmapTooltipFontColorGUI = heatmapTooltip.items.items[2].value;
				
				// HEATMAP fields (parameters) values from the chart model
				var heatmapLegendVertAlignCModel = chartViewModelData.legendAlign;
				var heatmapLegendSymbolHeightCModel = chartViewModelData.symbolHeight;
				
//				var heatmapTooltipFontFamilyCModel = chartViewModelData.tipFontFamily;
//				var heatmapTooltipFontSizeCModel = chartViewModelData.tipFontSize;
//				var heatmapTooltipFontStyleCModel = chartViewModelData.tipFontWeight;
//				var heatmapTooltipFontColorCModel = chartViewModelData.tipColor;
								
				(heatmapLegendVertAlignCModel=="" || heatmapLegendVertAlignCModel==null || heatmapLegendVertAlignCModel==undefined) ?
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.title.verticalAlignCombo'),
								LN("sbi.chartengine.configuration.heatmap.legendPanel.title")
							]
						) : errorMsg;	
				
				
								
				if (heatmapLegendSymbolHeightGUI == null)
				{
					if (heatmapLegendSymbolHeightCModel == null || heatmapLegendSymbolHeightCModel=="")
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
							
							[
								LN('sbi.chartengine.configuration.heatmap.symbolHeight'),
								LN("sbi.chartengine.configuration.heatmap.legendPanel.title")
							]
						);
					}						
				}
				else 
				{
					if (heatmapLegendSymbolHeightGUI < checkParamValuesForCharts.heatmap.legend.symbolHeight.minValue)
					{						
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.minValue"),
							
							[
								LN('sbi.chartengine.configuration.heatmap.symbolHeight'),
								checkParamValuesForCharts.heatmap.legend.symbolHeight.minValue,
								LN("sbi.chartengine.configuration.heatmap.legendPanel.title")
							]
						);
					}
					else if (heatmapLegendSymbolHeightGUI > checkParamValuesForCharts.heatmap.legend.symbolHeight.maxValue)
					{
						errorMsg += Sbi.locale.sobstituteParams
						(
							LN("sbi.chartengine.validation.configuration.maxValue"),
							
							[
								LN('sbi.chartengine.configuration.heatmap.symbolHeight'),
								checkParamValuesForCharts.heatmap.legend.symbolHeight.maxValue,
								LN("sbi.chartengine.configuration.heatmap.legendPanel.title")
							]
						);
					}
				}	
				
//				(heatmapTooltipFontFamilyCModel=="" || heatmapTooltipFontFamilyCModel==null || heatmapTooltipFontFamilyCModel==undefined) ?
//						errorMsg += Sbi.locale.sobstituteParams
//						(
//							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
//							
//							[
//								LN('sbi.chartengine.configuration.font'),
//								LN("sbi.chartengine.configuration.heatmap.tooltipPanel.title")
//							]
//						) : errorMsg;
//				
//				(heatmapTooltipFontSizeCModel=="" ||  heatmapTooltipFontSizeCModel==null ||  heatmapTooltipFontSizeCModel==undefined) ?
//						errorMsg += Sbi.locale.sobstituteParams
//						(
//							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
//							
//							[
//								LN('sbi.chartengine.configuration.fontsize'),
//								LN("sbi.chartengine.configuration.heatmap.tooltipPanel.title")
//							]
//						) : errorMsg;
//				
//				(heatmapTooltipFontColorCModel=="transparent" || heatmapTooltipFontColorCModel=="" || heatmapTooltipFontColorCModel==null || heatmapTooltipFontColorCModel==undefined) ?
//						errorMsg += Sbi.locale.sobstituteParams
//						(
//							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
//							
//							[
//								LN('sbi.chartengine.configuration.color'),
//								LN("sbi.chartengine.configuration.heatmap.tooltipPanel.title")
//							]
//						) : errorMsg;
//							
//				(heatmapTooltipFontStyleCModel=="" ||  heatmapTooltipFontStyleCModel==null ||  heatmapTooltipFontStyleCModel==undefined) ? 
//						errorMsg += Sbi.locale.sobstituteParams
//						(
//							LN("sbi.chartengine.validation.configuration.parameterNotSpecified"),
//							
//							[
//								LN('sbi.chartengine.configuration.fontstyle'),
//								LN("sbi.chartengine.configuration.heatmap.tooltipPanel.title")
//							]
//						) : errorMsg;
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
		
		/**
		 * Static function that updates the empty text inside the series/categories 
		 * container in the Designer, depending on the chart type of the document.
		 * This function works for both attributes (categories) and measures (series).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		emptyTextHandler: function(chartType,dataType)
		{
			var bottomXAxisGridView = this.bottomXAxisesPanel.getView();
			var oldEmptyText = bottomXAxisGridView.emptyText;
			
			var newEmptyTextBottomPanel = LN("sbi.chartengine.designer.emptytext.dragdropattributes." + chartType.toLowerCase());
			var newEmptyTextYPanel = LN("sbi.chartengine.designer.emptytext.dragdropmeasures." + chartType.toLowerCase());
			
			/**
			 * HTML template is figured out empirically (inspecting the element).
			 * Dynamic extraction of the preciding and succeeding part of the HTML
			 * of the old message is impossible, since we do not have information 
			 * about the old chart type.
			 */
			bottomXAxisGridView.emptyText = '<div class="x-grid-empty">' + newEmptyTextBottomPanel + '</div>';
			
			bottomXAxisGridView.refresh();
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
			
			//this.rightYAxisesPanel.update();
			//this.rightYAxisesPanel.updateLayout();

			var leftColumnsContainerId = this.leftYAxisesPanel.items.keys[0];
			var leftColumnsContainer = Ext.getCmp(leftColumnsContainerId);
			
			// bened 
			var emptyAxisData = Sbi.chart.designer.ChartUtils.createEmptyAxisData(false, true);
			leftColumnsContainer.setAxisData(emptyAxisData);
			
			//Since it remained only one serieStore let's update its data for it complies with the new leftColumnsContainer data
			serieStorePool[0].axisAlias = leftColumnsContainer.axisData.alias;
			serieStorePool[0].idAxisesContainer = leftColumnsContainer.axisData.id;
		},
		
		/**
		 * Called inside the ChartTypeSelector. Removes everything from the X-axis panel 
		 * if we move from BAR or LINE to SCATTER or RADAR, because for the last to we 
		 * can have ONLY ONE CATEGORY, while we can have more than one for the first pair 
		 * (BAR/LINE).
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */ 
		cleanCategoriesAxis: function() {
			this.bottomXAxisesPanel.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData(true));			
			this.categoriesStore.removeAll();
		},
		
		/**
		 * Responsible for removing all serie items inside of the corresponding 
		 * Y-axis panel when clicking on button for deleting all serie items that
		 * is inside of the panel's header.
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		cleanSerieAxis: function(yAxisId)
		{
			var serieStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
			
			for(i in serieStorePool) 
			{				
				if (serieStorePool[i].config.idAxisesContainer == yAxisId)
					serieStorePool[i].removeAll();
			}	
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
		
		/*ADDING SOME TEXT FOR SVN MERGING DEMO*/
		
    }
});