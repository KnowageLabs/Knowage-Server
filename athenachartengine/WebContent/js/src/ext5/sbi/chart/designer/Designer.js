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
    	chartTypes : 
		[
		 	{
				name: LN('sbi.chartengine.designer.charttype.bar'), 
				type: 'BAR',
				iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/barchart/img/barchart_64x64_ico.png',
			}, 
			
			{	
				name: LN('sbi.chartengine.designer.charttype.line'), 
				type: 'LINE',
				iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/linechart/img/linechart_64x64_ico.png',
			}, 
			
			{
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
				name: LN('sbi.chartengine.designer.charttype.chord'), 
				type: 'CHORD',
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
			
			console.log("-- JSON template 1: --- ");
			console.log(jsonTemplate);
			
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
			 * This part is executed whenever we create the fresh (new) document (chart)
			 * and start the initialization of the chart through the Designer interface
			 * (when we open in Designer completely new chart, not the existing one).
			 * (danilo.ristovski@mht.net)
			 */
			if (!jsonTemplate.CHART) {
				console.log("*** NEW CHART!!! ***");
				newChart = true;
				jsonTemplate = baseTemplate;
			}			
			
			/**
			 * Predefined style type for any chart is RED.
			 * (danilo.ristovski@mht.net)
			 */		
			this.styleName = (jsonTemplate.CHART.styleName) ? (jsonTemplate.CHART.styleName) : "red";
			
			console.log("-- JSON template 2 (no changes if not new chart): --- ");
			console.log(jsonTemplate);
			
			/**
			 * Get the missing JSON configuration elements (properties) in order to define
			 * their default values for any type of chart (including the BAR chart).
			 * (danilo.ristovski@mht.net)
			 * (lazar.kostic@mht.net)
			 */
			var getConfigurationForStyle = function(style)
			{
				this.styleName = style;
				
				/**
				 * JSON template that keeps the predefined values for the different styles parameters. 
				 * We will return this JSON object when needed (e.g. before merging old JSON template 
				 * with the new one (that keeps the predefined style parameters), after changing the style).
				 */
				var templateToReturn = null;
				
				/**
				 * This method is called when changing (choosing) the style of the chart's default parameters 
				 * and it will return the JSON template depending on the chosen style (red, blue, ...). This
				 * switch statement servers for that purpose.
				 */
				switch(style)
				{				
					case "red":	
						console.log(jsonTemplate.CHART.AXES_LIST.AXIS[0].alias);
						console.log(jsonTemplate.CHART.AXES_LIST.AXIS[1].alias);
						templateToReturn = 
						
						{
							/**
							 * Generic parameters are common for any type of chart and their default values 
							 * are going to be set through this 'generic' property of the JSON template.
							 */
							generic: 
							{
								CHART:
								{
//									height: 400,
//									width: 1000,	
									isCockpitEngine: "false",
									orientation: "horizontal",
									style: "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#FF0000;",
									styleName: "red",
//									styleCustom: styleCustom, 
									
									TITLE:
									{
										style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;",
										text: "This is red chart"
									},
									
									SUBTITLE:
									{
										style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;",
										text: "Insert your subtitle"
									},
									
									EMPTYMESSAGE:
									{
										style: "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;",
										text: "Insert your empty message (when data for chart does not exist)"
									},
									
									LEGEND:
									{
										floating: false,
										layout: "",
										position: "",
										show: false,
										style: "align:;fontFamily:;fontSize:;fontWeight:;borderWidth:;color:;backgroundColor:;",
										x: 0,
										y: 0
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
									},
									
									// TODO: Need to adjust this tag with the mergeObjects concept of treating the multiply Y-axis panels
									AXES_LIST: 
									{
										AXIS: 
										[														 
									       {
									    	   //alias:'Y',
//									    	   alias: jsonTemplate.CHART.AXES_LIST.AXIS[0].alias,
//									    	   id: jsonTemplate.CHART.AXES_LIST.AXIS[0].alias,
									    	   type: 'Serie',
									    	  // position: "",
									    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
									    	   //id: Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id,
									    	   
									    	   MAJORGRID:
								    		   {
									    		   interval: "",
									    		   style: "typeline:;color:#D8D8D8;"
								    		   },
									       
								    		   MINORGRID:
							    			   {
								    			   interval: "", 
								    			   style: "typeline:;color:#E0E0E0;"
							    			   },
								    		   
								    		   TITLE:
							    			   {
								    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   text: "red measure axis title" 
							    			   }
							    		   },									       
							    		   
							    		   {
							    			  // alias:'X', 
//							    			   alias: jsonTemplate.CHART.AXES_LIST.AXIS[1].alias,
//									    	  // id: jsonTemplate.CHART.AXES_LIST.AXIS[1].alias,
							    			   type: 'Category',
							    			  // position: "", 
							    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   
							    			   TITLE:
						    				   {
							    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    				   text: ""
						    				   }
					    				   }
								        ]
									},
									VALUES: {
										SERIE: {
											borderColor:"#FFFFFF"
										}
									}
								}						
							},
							
							/**
							 * Specific parameters are specific for certain (particular) type of chart and 
							 * their default values are going to be set through separate properties of the 
							 * JSON template that are going to be named by the name of the chart that is
							 * considered (that we choosed for our chart (document). For example, specific 
							 * default parameters for the WORDCLOUD chart type are going to be put inside 
							 * the 'wordcloud' property of the JSON template and so on.
							 */
							
							/**
							 * Default (predefined) values for the specific parameters of the WORDCLOUD chart
							 */
							wordcloud:
							{								
								CHART:
								{		
									type: "WORDCLOUD",
									
									maxAngle: 121,
									maxFontSize: 51,
									maxWords: 51,
									minAngle: 61, 
									sizeCriteria: 'serie',
									wordPadding: 5									
								}
							},
							
							bar: 
							{
								CHART:
								{
									type: "BAR"
								}
							},
							
							line: 
							{
								CHART:
								{
									type: "LINE"
								}
							},
							
							pie: 
							{
								CHART:
								{
									type: "PIE"
								}
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the TREEMAP chart
							 */
							treemap: 
							{
								
								CHART:
								{		
									type: "TREEMAP"								
								}
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the PARALLEL chart
							 */
							parallel:
							{								
								CHART:
								{
									type: "PARALLEL",
									
									AXES_LIST:
									{									
										style:"axisColNamePadd:16;brushWidth:10;axisColor:#FF6600;brushColor:#339966;"										
									},
									
									LIMIT:
									{					
										/**
										 * 'serieFilterColumn' attribute depends on available (picked) SERIE items (values)
										 * for the chart, since it is always empty as a default value
										 * (danilo.ristovski@mht.net)
										 */
										style:"maxNumberOfLines:20;orderTopMinBottomMax:bottom;serieFilterColumn:;"										
									},
									
									PARALLEL_TOOLTIP:
									{										
										style:"fontFamily:Cambria;fontSize:18px;minWidth:10;maxWidth:50;minHeight:5;maxHeight:50;padding:1;border:1;borderRadius:1;"										
									},
									
									LEGEND:
									{										
										TITLE:
										{											
											style:"fontFamily:Arial;fontSize:9px;fontWeight:bold;"											
										},
										
										ELEMENT:
										{											
											style:"fontFamily:Cambria;fontSize:12px;fontWeight:normal;"											
										}
										
									},
									
									AXES_LIST:
									{
										style: "axisColNamePadd:15;brushWidth:12;axisColor:#FF6600;brushColor:#339966;"
									}
								}
								
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the HEATMAP chart
							 */
							heatmap:
							{								
								CHART:
								{		
									type: "HEATMAP",
									
									LEGEND:
									{		
										symbolWidth: 50,
										style: "align:center;"									
									},
									
									TOOLTIP:
									{										
										style: "fontFamily:Gungsuh;fontSize:24px;color:#003366;"										
									}									
								}								
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the RADAR chart
							 */
							radar: 
							{
								CHART:
								{		
									type: "RADAR"								
								}
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the SCATTER chart
							 */
							scatter:
							{	
								CHART:
								{	
									type: "SCATTER",	
									
									zoomType:"xy",
									
									AXES_LIST:
									{
										AXIS: 
										[														 
									       {
									    	   //alias:'Y', 
									    	   type: 'Serie',
									    	  // position: "",
									    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
									    	   //id: Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id,
									    	   
									    	   MAJORGRID:
								    		   {
									    		   interval: "",
									    		   style: "typeline:;color:#D8D8D8;"
								    		   },
									       
								    		   MINORGRID:
							    			   {
								    			   interval: "", 
								    			   style: "typeline:;color:#E0E0E0;"
							    			   },
								    		   
								    		   TITLE:
							    			   {
								    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
								    			   text: "" 
							    			   }
							    		   },									       
							    		   
							    		   {
							    			   //alias:'X', 
							    			   type: 'Category',
							    			   //position: "", 
							    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   
							    			   /**
							    			    * Specific for this chart type
							    			    */
							    			   startOnTick: "false", 
							    			   showLastLabel: "true",
							    			   endOnTick: "false",
							    			   
							    			   TITLE:
						    				   {
							    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    				   text: ""
						    				   }
					    				   }
								        ]
									}
								}								
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the GAUGE chart
							 */
							// TODO: Waiting for the AXES_LIST and VALUES decisions (multiple Y-axis and dynamic SERIE items and their number)
							gauge:
							{								
								CHART:
								{		
									type: "GAUGE",
									
									AXES_LIST:
									{
										AXIS: 
										[														 
									       	{
									       		//alias:'Y', 
									       		type: 'Serie',
									       		//position: "",
									       		style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
									       		//id: Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id,
									       		
									       		lineColor: "#FF0000", 
									       		lineWidth:1,
									       		max: 60,
								                min: 1,
								                minorTickColor: "#008000",
								                minorTickInterval: "",
								                minorTickLength: 10,
								                minorTickPosition: "inside",
								                minorTickWidth:"1",
							                	offset:"1" ,
								                tickColor:"#FF00FF" ,
							                	tickLength:"10", 
						                		tickPixelInterval:"30",
								                tickPosition:"inside" ,
							                	tickWidth:"2",
							                	
							                	LABELS:
						                		{
							                		distance: "5",
							                		rotation:"1"
						                		},									       	
									    	   
									       		MAJORGRID:
									       		{
									       			interval: "",
									       			style: "typeline:;color:#D8D8D8;"
									       		},
									       
									       		MINORGRID:
									       		{
									       			interval: "", 
									       			style: "typeline:;color:#E0E0E0;"
									       		},
								    		   
									       		TITLE:
									       		{
									       			style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
									       			text: "" 
									       		}
									       	}
									    ]
									},
									
									PANE:
									{											
										endAngle:"121",
										startAngle:"-121"
									}									
								}								
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the SUNBURST chart
							 */
							sunburst:
							{								
								CHART:
								{
									type: "SUNBURST",
									
									opacMouseOver:"10",
									
									TOOLBAR:
									{										
										style: "position:bottom;height:50;width:60;spacing:5;tail:10;percFontColor:#FF9900;fontFamily:Calibri;fontWeight:normal;fontSize:14px;"
									},
									
									TIP:
									{										
										style: "fontFamily:Cambria;fontWeight:bold;fontSize:14px;color:#FF0000;align:;width:200;",
										text: "Insert text here"										
									}									
								}								
							},
							
							/**
							 * Default (predefined) values for the specific parameters of the CHORD chart
							 */
							chord: 
							{
								CHART:
								{	
									type: "CHORD"						
								}	
							}
						};
						
						return templateToReturn;
						
						break;
						
					case "blue":
/*//						console.log("BLUE");
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
						legendY = 0;*/
						
						templateToReturn = 
							
						{
							generic: 
							{
								CHART:
								{
									height: 500,
									width: 1100,	
									isCockpitEngine: "false",
									orientation: "horizontal",
									style: "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#0000FF;",
									styleName: "blue",
//									styleCustom: styleCustom, 
									
									TITLE:
									{
										style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;",
										text: "This is blue chart"
									},
									
									SUBTITLE:
									{
										style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;",
										text: "Insert your subtitle"
									},
									
									EMPTYMESSAGE:
									{
										style: "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;",
										text: "Insert your empty message (when data for chart does not exist)"
									},
									
									LEGEND:
									{
										floating: false,
										layout: "",
										position: "",
										show: false,
										style: "align:;fontFamily:;fontSize:;fontWeight:;borderWidth:;color:;backgroundColor:;symbolWidth:;",
										x: 0,
										y: 0
									},
									
									AXES_LIST: 
									{
										AXIS: 
										[														 
									       {
									    	   //alias:'Y', 
									    	   type: 'Serie',
									    	   //position: "",
									    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
									    	   
									    	   MAJORGRID:
								    		   {
									    		   interval: "",
									    		   style: "typeline:;color:#D8D8D8;"
								    		   },
									       
								    		   MINORGRID:
							    			   {
								    			   interval: "", 
								    			   style: "typeline:;color:#E0E0E0;"
							    			   },
								    		   
								    		   TITLE:
							    			   {
								    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
								    			   text: "AAA" 
							    			   }
							    		   },									       
							    		   
							    		   {
							    			  // alias:'X', 
							    			   type: 'Category',
							    			   //position: "", 
							    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   
							    			   TITLE:
						    				   {
							    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    				   text: ""
						    				   }
					    				   }
								        ]
									},
									VALUES: {
										SERIE: {
											borderColor:"#FFFFFF"
										}
									}
								}						
							},
							
							bar: 
							{
								CHART:
								{
									type: "BAR"
								}
							},
							
							line: 
							{
								CHART:
								{
									type: "LINE"
								}
							},
							
							pie: 
							{
								CHART:
								{
									type: "PIE"
								}
							},
							
							wordcloud:{
								
								CHART:
								{
									
									maxAngle:'122',
									maxFontSize:'52',
									maxWords:'52',
									minAngle:'62', 
									sizeCriteria:'serie',
									wordPadding:'3'
									
								}
							},
							treemap:{
								
							},
							parallel:{
								CHART:
								{
									AXES_LIST:{
									
										style:"axisColNamePadd:16;brushWidth:10;axisColor:#FF6600;brushColor:#339966;"
										
									},
									LIMIT:{
									
										style:"maxNumberOfLines:20;orderTopMinBottomMax:bottom;"
										
									},
									PARALLEL_TOOLTIP:{
										
										style:"fontFamily:Cambria;fontSize:18px;minWidth:10;maxWidth:50;minHeight:5;maxHeight:50;padding:1;border:1;borderRadius:1;"
										
									},
									LEGEND:{
										
										TITLE:{
											
											style:"fontFamily:Arial;fontSize:9px;fontWeight:bold;"
											
										},
										ELEMENT:{
											
											style:"fontFamily:Cambria;fontSize:12px;fontWeight:normal;"
											
										}
										
									}								
								}
								
							},
							heatmap:{
								
								CHART:{
									
									LEGEND:{
										
										style:"align:center;symbolWidth:60;"
										
									},
									TOOLTIP:{
										
										style:"fontFamily:Gungsuh;fontSize:24px;color:#003366;"
										
									}
									
								}
								
							},
							radar:{
								
							},
							scatter:{
								
								CHART:{
								
									zoomType:"xy"
									
								}								
							},
							gauge:{
								
								CHART:{
									
									PANE:{
										endAngle:"122",
										startAngle:"-122"
									}
									
								}
								
							},
							sunburst:{
								
								CHART:{
									
									opacMouseOver:"12",
									
									TOOLBAR:{
										
										style: "position:bottom;height:50;width:60;spacing:5;tail:10;percFontColor:#FF9900;fontFamily:Calibri;fontWeight:normal;fontSize:15px;"
										
									},
									TIP:{
										
										style: "fontFamily:Cambria;fontWeight:bold;fontSize:14px;color:#FF0000;align:;width:200;",
										text: "insert text here"
										
									}
									
								}
								
							},
							chord:{
								
							}
						};
						
						return templateToReturn;
						
						break;
						
					case "green":
/*//						console.log("GREEN");
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
						legendY = 0;*/
						
						templateToReturn = 
							
						{
							generic: 
							{
								CHART:
								{
									height: 400,
									width: 1000,	
									isCockpitEngine: "false",
									orientation: "horizontal",
									style: "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#00FF00;",
									styleName: "blue",
//									styleCustom: styleCustom, 
									
									TITLE:
									{
										style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;",
										text: "This is green chart"
									},
									
									SUBTITLE:
									{
										style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;",
										text: "Insert your subtitle"
									},
									
									EMPTYMESSAGE:
									{
										style: "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;",
										text: "Insert your empty message (when data for chart does not exist)"
									},
									
									LEGEND:
									{
										floating: false,
										layout: "",
										position: "",
										show: false,
										style: "align:;fontFamily:;fontSize:;fontWeight:;borderWidth:;color:;backgroundColor:;symbolWidth:;",
										x: 0,
										y: 0
									},
									
									AXES_LIST: 
									{
										AXIS: 
										[														 
									       {
									    	   //alias:'Y', 
									    	   type: 'Serie',
									    	   //position: "",
									    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
									    	   
									    	   MAJORGRID:
								    		   {
									    		   interval: "",
									    		   style: "typeline:;color:#D8D8D8;"
								    		   },
									       
								    		   MINORGRID:
							    			   {
								    			   interval: "", 
								    			   style: "typeline:;color:#E0E0E0;"
							    			   },
								    		   
								    		   TITLE:
							    			   {
								    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
								    			   text: "AAA" 
							    			   }
							    		   },									       
							    		   
							    		   {
							    			   //alias:'X', 
							    			   type: 'Category',
							    			   //position: "", 
							    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   
							    			   TITLE:
						    				   {
							    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    				   text: ""
						    				   }
					    				   }
								        ]
									}
								}						
							},
							

							
							bar: 
							{
								CHART:
								{
									type: "BAR"
								}
							},
							
							line: 
							{
								CHART:
								{
									type: "LINE"
								}
							},
							
							pie: 
							{
								CHART:
								{
									type: "PIE"
								}
							},
							
							wordcloud:{
								
								CHART:
								{
									
									maxAngle:'123',
									maxFontSize:'53',
									maxWords:'53',
									minAngle:'63', 
									sizeCriteria:'serie',
									wordPadding:'5'
									
								}
							},
							treemap:{
								
							},
							parallel:{
								CHART:
								{
									AXES_LIST:{
									
										style:"axisColNamePadd:16;brushWidth:10;axisColor:#FF6600;brushColor:#339966;"
										
									},
									LIMIT:{
									
										style:"maxNumberOfLines:21;orderTopMinBottomMax:bottom;"
										
									},
									PARALLEL_TOOLTIP:{
										
										style:"fontFamily:Cambria;fontSize:18px;minWidth:10;maxWidth:50;minHeight:6;maxHeight:50;padding:1;border:1;borderRadius:1;"
										
									},
									LEGEND:{
										
										TITLE:{
											
											style:"fontFamily:Arial;fontSize:9px;fontWeight:bold;"
											
										},
										ELEMENT:{
											
											style:"fontFamily:Cambria;fontSize:12px;fontWeight:normal;"
											
										}
										
									}								
								}
								
							},
							heatmap:{
								
								CHART:{
									
									LEGEND:{
										
										style:"align:center;symbolWidth:55;"
										
									},
									TOOLTIP:{
										
										style:"fontFamily:Gungsuh;fontSize:26px;color:#003366;"
										
									}
									
								}
								
							},
							radar:{
								
							},
							scatter:{
								
								CHART:{
								
									zoomType:"xy"
									
								}								
							},
							gauge:{
								
								CHART:{
									
									PANE:{
										endAngle:"123",
										startAngle:"-123"
									}
									
								}
								
							},
							sunburst:{
								
								CHART:{
									
									opacMouseOver:"13",
									
									TOOLBAR:{
										
										style: "position:bottom;height:50;width:60;spacing:5;tail:10;percFontColor:#FF9900;fontFamily:Calibri;fontWeight:normal;fontSize:14px;"
										
									},
									TIP:{
										
										style: "fontFamily:Cambria;fontWeight:bold;fontSize:14px;color:#FF0000;align:;width:200;",
										text: "insert text here"
										
									}
									
								}
								
							},
							chord:{
								
							}
						};
						
						console.log("-- JSON template 3 (to return after style): --- ");
						console.log(jsonTemplate);
						
						return templateToReturn;
						
						break;					
				}			
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
				
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(baseTemplate, jsonTemplate);	
				
				console.log("-- JSON template 4 (long IF): --- ");
				console.log(jsonTemplate);
				
			}				
			
			/**
			 * Set the predefined values for the generic parameters of the newly created chart 
			 * (danilo.ristovski@mht.net)
			 */			
//			if (jsonTemplate.CHART.styleCustom)
//				console.log("YYYY");
//			else
			var applyAxes = true;
			var configApplyAxes = {applyAxes: applyAxes};
			
			if (newChart == true)
			{
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
						jsonTemplate, 
						getConfigurationForStyle(this.styleName).generic,
						configApplyAxes);
			}
			
			/**
			 * If the chart is already existing (not just created) and if it is of the 
			 * GAUGE type, set the plotband store that keeps the data about the plots
			 * that are linked to the particular chart of this type. Afterwards, when
			 * we open the Axis style configuration for this chart type we will have
			 * the grid panel for the plotbands populated with existing plots (intervals).
			 * (danilo.ristovski@mht.net)
			 */
			if (jsonTemplate.CHART.type.toUpperCase() == "GAUGE")
			{
				Sbi.chart.designer.ChartColumnsContainerManager.setPlotbandsStore(jsonTemplate);
			}				
			
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
			
			/**
			 * Listener for the 'rowclick' event that happens when we change the chart type
			 * on the left part of the Designer page (from the chart type picker). 
			 * (danilo.ristovski@mht.net)
			 */
			this.chartTypeSelector.on
			(
				"newrowclick",
				
				function()
				{			
					/**
					 * Get the main configuration panel (the one on the top of the Step 2 tab of the Designer page)
					 * and the second configuration panel (everything under the main panel).
					 */
					var mainConfigurationPanel = globalThis.stepsTabPanel.getComponent(1).getComponent(0);
					var secondConfigurationPanel = globalThis.stepsTabPanel.getComponent(1).getComponent(1);					
					
					var chartLegendCheckBox = mainConfigurationPanel.getComponent("showLegend");
					var chartOrientation = mainConfigurationPanel.getComponent("fieldContainer1").getComponent("chartOrientationCombo");
					var chartWidth = mainConfigurationPanel.getComponent("fieldContainer1").getComponent("chartWidthNumberfield");	
					
					/**
					 * The main configuration panel element (opacity on mouse over) to show
					 * on the Step 2 main configuration panel when the SUNBURST is selected.
					 */
					var opacityOnMouseOver = mainConfigurationPanel.getComponent("opacityMouseOver");
					
					/**
					 * The additional second configuration panel elements to show when the SUNBURST is selected.
					 */
					var colorPallete = secondConfigurationPanel.getComponent("chartColorPallete");
					var chartLegend = secondConfigurationPanel.getComponent("chartLegend");	
					var toolbarAndTip = secondConfigurationPanel.getComponent("chartToolbarAndTip");
					
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
					var parallelLegendPanel = secondConfigurationPanel.getComponent("chartParallelLegend");
					
					/**
					 * The second configuration panel element for hiding/showing when the SCATTER is selected.
					 */
					var scatterConfiguration = secondConfigurationPanel.getComponent("chartScatterConfiguration");
					
					/**
					 * The additional second configuration panel element to show when the HEATMAP is selected.
					 */
					var showLegendAndTooltip = secondConfigurationPanel.getComponent("chartHeatmapLegendAndTooltip");
					
					/**
					 * The additional second configuration panel element to show when the GAUGE is selected.
					 */
					var gaugePanePanel = secondConfigurationPanel.getComponent("gaugePaneConfiguration");
					
					/**
					 * Determine which is the newly chosen chart type in order to show/hide
					 * suitable GUI elements on the Step 2 (and Step 1, only for the GAUGE
					 * chart type).
					 */
					var isChartSunburst = this.chartType.toUpperCase() == 'SUNBURST';
					var isChartWordCloud = this.chartType.toUpperCase() == 'WORDCLOUD';		
					var isChartTreemap = this.chartType.toUpperCase() == 'TREEMAP';
					var isChartParallel = this.chartType.toUpperCase() == 'PARALLEL';					
					var isChartScatter = this.chartType.toUpperCase() == 'SCATTER';		
					var isChartRadar= this.chartType.toUpperCase() == 'RADAR';
					var isChartHeatmap = this.chartType.toUpperCase() == 'HEATMAP';	
					var isChartChord = this.chartType.toUpperCase() == 'CHORD';	
					var isChartGauge = this.chartType.toUpperCase() == 'GAUGE';	
					
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
					if (isChartSunburst || isChartWordCloud || isChartTreemap 
							|| isChartParallel || isChartHeatmap || isChartGauge) {
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
					if (isChartSunburst || isChartWordCloud || isChartScatter || isChartGauge) {
						colorPallete.hide();
					} else {
						colorPallete.show();
					}
					
					/**
					 * Show/hide the toolbar and tip panel on the second configuration panel 
					 * on the Step 2 tab of the Designer page.
					 */
					if (isChartSunburst) {
						toolbarAndTip.show();
					} else  {
						toolbarAndTip.hide();
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
						parallelLegendPanel.show();
					} else {
						parallelLimitPanel.hide();
						parallelAxesLinesPanel.hide();
						parallelTooltipPanel.hide();
						parallelLegendPanel.hide();
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
						showLegendAndTooltip.show();
					} else {
						showLegendAndTooltip.hide();
					}
					
					/**
					 * Show/hide pane panel of the GAUGE chart type on the second configuration 
					 * panel on the Step 2 tab of the Designer page. Show/hide the bottom X-axis
					 * panel on the Step 1 tab of the Designer page.
					 */
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
					
//					/**
//					 * Depending on the style that we choose for the document's generic
//					 * customizable parameters (Red, Green, Blue, ... style), take the
//					 * predefined JSON structure that is defined for that newly chosen 
//					 * style. This part is needed for later merging of the templates 
//					 */
//		    		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
//		    		
//		    		jsonTemplate = 
//		    		{
//	    				CHART:
//	    				{
//	    					type: chartType,
//	    					
//	    					COLORPALETTE:
//							{
//								COLOR: [
//								        {id:1 , order: 1, name: '7cb5ec', value: '7cb5ec' }, 
//								        {id:2 , order: 2, name: '434348', value: '434348' }, 
//								        {id:3 , order: 3, name: '90ed7d', value: '90ed7d' }, 
//								        {id:4 , order: 4, name: 'f7a35c', value: 'f7a35c' }, 
//								        {id:5 , order: 5, name: '8085e9', value: '8085e9' }, 
//								        {id:6 , order: 6, name: 'f15c80', value: 'f15c80' }, 
//								        {id:7 , order: 7, name: 'e4d354', value: 'e4d354' }, 
//								        {id:8 , order: 8, name: '2b908f', value: '2b908f' }, 
//								        {id:9 , order: 9, name: 'f45b5b', value: 'f45b5b' }, 
//								        {id:10, order: 10,name: '91e8e1', value: '91e8e1' }
//								        ]
//							},
//							
//							VALUES: jsonTemplate.CHART.VALUES
//    					}
//		    		};
//		    		
//					var genericConfigurationForStyle = getConfigurationForStyle("red").generic;
//
//					/**
//					 * Reset (refresh, modify) the 'styleName' field of the Designer, also
//					 */
//					globalThis.styleName = "red";
//					
//					/**
//					 * Reset the JSON template for the document (chart) after changing the 
//					 * previously selected style (changing the selected item of the combo)
//					 */
//					jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,genericConfigurationForStyle);
////					console.log(jsonTemplate);
//					if (isChartWordCloud){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").wordcloud);
//						
//					}
//					else if (isChartTreemap){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").treemap);
//						
//					}
//					else if (isChartParallel){
//						
//						jsonTemplate.CHART.LEGEND=null;
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").parallel);
//					
//					}
//					else if (isChartHeatmap){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").heatmap);
//					
//					}
//					else if (isChartRadar){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").radar);
//						
//					}
//					else if (isChartScatter){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").scatter);
//						
//					}
//					else if (isChartGauge){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").gauge);
//						
//					}
//					else if (isChartSunburst){
//						
//						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle("red").sunburst);
//						
//					}
//					
//					/**
//					 * Update (refresh) the main configuration panel (the one on the top of 
//					 * the Step 2 tab) after selecting the particular style
//					 */
//		    		Sbi.chart.designer.Designer.update(jsonTemplate);
				}
			);
			
			// TODO: Check the functionality of this listener
			/**
			 * Listener that listens when the chart type is changed between non-compatible
			 * chart types (i.e. between the BAR and the CHORD chart types). In that case
			 * the data in both Y and X axis panels will be removed (panels will be clear).
			 * (danilo.ristovski@mht.net)
			 */
			this.chartTypeSelector.on
			(
				"cleanJson",
				
				function(selectedType)
				{
					jsonTemplate.CHART.VALUES = {};
					
					var styleName = "";
					
					if (jsonTemplate.CHART.styleName)
					{
						styleName = jsonTemplate.CHART.styleName;
					}
					else
					{
						styleName = "red";
					}
//					var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
					
					console.log(styleName);
//					console.log(chartType);
					console.log(selectedType);
					console.log(jsonTemplate);
					
					jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).generic);

					if (selectedType == "WORDCLOUD"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).wordcloud);
						
					}
					else if (selectedType == "TREEMAP"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).treemap);
						
					}
					else if (selectedType == "PARALLEL"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).parallel);
					
					}
					else if (selectedType == "HEATMAP"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).heatmap);
					
					}
					else if (selectedType == "RADAR"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).radar);
						
					}
					else if (selectedType == "SCATTER"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).scatter);
						
					}
					else if (selectedType == "GAUGE"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).gauge);
						
					}
					else if (selectedType == "SUNBURST"){
						
						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).sunburst);
						
					}
					else if (selectedType == "CHORD"){

						jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(styleName).chord);
						
					}
					
					console.log(jsonTemplate);
					
					Sbi.chart.designer.Designer.update(jsonTemplate);
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
			 * (danilo.ristovski@mht.net) 
			 */
			// TODO: Explain the usage of this global variable
			this.seriesBeforeDropStore = Ext.create("Ext.data.Store",{id:"storeForSeriesBeforeDrop", fields: [{name: 'seriesColumn'}]});
			
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
			
			/** 
			 * Type of the 'this.categoriesPicker' is the grid panel
			 * (commented by: danilo.ristovski@mht.net) 
			 */
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
			 * with combo items that have predefined names (Red, Green, Blue, ...).
			 * (danilo.ristovski@mht.net)
			 */
			var styleStore = Ext.create ( "Ext.data.Store", {
					fields: ["style", "styleAbbr"],
					
				data:  [
				 	{style: LN('sbi.chartengine.designer.stylecolor.red'), styleAbbr: "red"},
				 	{style: LN('sbi.chartengine.designer.stylecolor.blue'), styleAbbr: "blue"},
				 	{style: LN('sbi.chartengine.designer.stylecolor.green'), styleAbbr: "green"}
					 ]
			});
			
			/**
			 * GUI label element that will be placed immediatelly above the style combo box
			 * (on the top of the left panel on the Designer page).
			 * (danilo.ristovski@mht.net)
			 */
			this.styleLabel = Ext.create ('Ext.form.Label', {
			        forId: 'stylePickerComboId',
		        text: LN('sbi.chartengine.designer.styleforparameters'),
			        //margin: '5 3 3 0'
			});
			
			/**
			 * Combo box for defining the style for the generic customizable parameters (properties)
			 * of the chart.
			 * (danilo.ristovski@mht.net)
			 * (lazar.kostic@mht.net)
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
					    		var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
					    		
					    		/* 4.9. Keep the VALUES tag (keep them when only changing the   */
					    		var variablesToKeep = jsonTemplate.CHART.VALUES;
					    		var colorpalleteToKeep = jsonTemplate.CHART.COLORPALLETE;
					    		var yAxisAliasToKeep = jsonTemplate.CHART.AXES_LIST.AXIS[0].alias;
					    		var xAxisAliasToKeep = jsonTemplate.CHART.AXES_LIST.AXIS[1].alias;
					    		
					    		/* 4.9. Clear the JSON template */
					    		jsonTemplate.CHART = {};
					    		jsonTemplate.CHART.AXES_LIST = { AXIS: [{}, {}]};
					    		
					    		/* 4.9. Take the VALUES in new JSON template */
					    		jsonTemplate.CHART.VALUES = variablesToKeep;
					    		jsonTemplate.CHART.AXES_LIST.AXIS[0].alias = yAxisAliasToKeep;
					    		jsonTemplate.CHART.AXES_LIST.AXIS[0].id = yAxisAliasToKeep;
					    		jsonTemplate.CHART.AXES_LIST.AXIS[1].alias = xAxisAliasToKeep;
					    		jsonTemplate.CHART.AXES_LIST.AXIS[1].id = xAxisAliasToKeep;
					    		
								var genericConfigurationForStyle = getConfigurationForStyle(k.data.styleAbbr).generic;
								
								console.log("-- Generic conf for style: --");
								console.log(genericConfigurationForStyle);
								
								console.log("-- JSON template 6 (before combo): --- ");
								console.log(jsonTemplate);
								
								/* Keep the ID of the left Y-axis panel when changing the style of the chart
								 * since the one (left Y-axis panel) is always present */
//								console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id);
//								jsonTemplate.CHART.AXES_LIST.AXIS[0].id = "Axis_6";
																
								/* Clean the chart type value because it is maybe going to be changed. Case: we firstly change
								 * type of the chart and then we change the style - it is needed because of the merging function,
								 * since we have the old chart type in the old JSON template that was linked to the previous
								 * (initial) chart (document) and we want to change that value  */
//								jsonTemplate.CHART.type = "";
console.log(chartType);
//console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[]);
								/**
								 * Reset (refresh, modify) the 'styleName' field of the Designer, also
								 */
								globalThis.styleName = k.data.styleAbbr;
								
								/**
								 * Reset the JSON template for the document (chart) after changing the 
								 * previously selected style (changing the selected item of the combo)
								 */
								jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,genericConfigurationForStyle);
								
								console.log("-- JSON template 7 (after combo): --- ");
								console.log(jsonTemplate);
								
								/**
								 * Set the predefined values for specific parameters of different chart types.
								 */
								console.log(chartType);
								
								// TODO: See the differences with the new code (Benedetto)
								if (chartType == "BAR")
								{
									jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(k.data.styleAbbr).bar);
									console.log(getConfigurationForStyle(k.data.styleAbbr).bar);
									console.log(jsonTemplate);
								}
								else if (chartType == "LINE")
								{
									jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(k.data.styleAbbr).line);
								}
								else if (chartType == "PIE")
								{
									jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(k.data.styleAbbr).pie);
								}
								else if (chartType == "WORDCLOUD"){
								jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).wordcloud,
										configApplyAxes);
								}
								else if (chartType == "TREEMAP"){
									
									jsonTemplate.CHART.COLORPALLETE = colorpalleteToKeep;
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).treemap,
										configApplyAxes);									
								}
								else if (chartType == "PARALLEL"){
									
									jsonTemplate.CHART.LEGEND = {};
									jsonTemplate.CHART.COLORPALLETE = colorpalleteToKeep;
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).parallel,
										configApplyAxes);								
								}
								else if (chartType == "HEATMAP"){
									
									jsonTemplate.CHART.LEGEND = {};
									jsonTemplate.CHART.COLORPALLETE = colorpalleteToKeep;
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).heatmap,
										configApplyAxes);								
								}
								else if (chartType == "RADAR"){
									
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).radar,
										configApplyAxes);									
								}
								else if (chartType == "SCATTER"){
									
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).scatter,
										configApplyAxes);									
								}
								else if (chartType == "GAUGE"){
									
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).gauge,
										configApplyAxes);									
								}
								else if (chartType == "SUNBURST"){
									
jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
										jsonTemplate, 
										getConfigurationForStyle(k.data.styleAbbr).sunburst,
										configApplyAxes);									
								}
								else if (chartType == "CHORD"){
	
									jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,getConfigurationForStyle(k.data.styleAbbr).chord,
										configApplyAxes);
									
								}
								
								console.log("-- JSON template 8 (after applying combo): --- ");
								console.log(jsonTemplate);
								
								/**
								 * Update (refresh) the main configuration panel (the one on the top of 
								 * the Step 2 tab) after selecting the particular style.
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
  	  						 * HEATMAP chart type.
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
	  	  								 * is of a DATE type (Timestamp).
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
								
						    	/**
					    		 * Hide the Title collapsable field container when we open the
					    		 * Axis style configuration popup for the HEATMAP chart. We do
					    		 * not need the one.
					    		 * (danilo.ristovski@mht.net)
					    		 */
						    	// TODO: Extend this if-statement also with other chart types that don't need the element
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
			
			/**
			 * Hiding the bottom (X) axis title textbox and gear tool
			 * if the already existing (saved) chart (document) is one of the 
			 * specified chart types.
			 * (danilo.ristovski@mht.net)
			 */
			var typeOfChart = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
			
			if (typeOfChart == "SUNBURST" || typeOfChart == "WORDCLOUD" || 
					typeOfChart == "TREEMAP" || typeOfChart == "PARALLEL" ||
						typeOfChart == "HEATMAP" || typeOfChart == "CHORD")
			{
				/**
				 * Hide the bottom (X) axis title textbox	
				 */
				Ext.getCmp("chartBottomCategoriesContainer").tools[0].hidden = true;
				
				/**
				 * Hide the gear icon on the bottom (X) axis panel	
				 */
				if (typeOfChart != "HEATMAP")
				{
					Ext.getCmp("chartBottomCategoriesContainer").tools[1].hidden = true;
				}
			}
		
			this.chartStructure = Ext.create('Sbi.chart.designer.ChartStructure', {
  				title: LN('sbi.chartengine.designer.step1'),
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
  		            							  		
  		            							  		// TODO: See what is happening here   		            							
  		            							var exportedAsOriginalJson = Sbi.chart.designer.Designer.exportAsJson(true);
  		            							
  		            							console.log("-- Exported as original JSON: --- ");
  		            							console.log(exportedAsOriginalJson);
  		            							
//  		            							console.log(Ext.JSON.encode(exportedAsOriginalJson));
  		            							
  		            							//jsonTemplate = exportedAsOriginalJson;
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
  		            							
  		            								var parameters = {
  	  		            									jsonTemplate: Ext.JSON.encode(exportedAsOriginalJson),
  	  		            									docLabel: docLabel
  	  		            							};
  	  		            							coreServiceManager.run('saveChartTemplate', parameters, [], function (response) {});
  	  		            							Ext.getBody().unmask();
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
  				]
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
			console.log("*** G ***");
			if(!(jsonTemplate.CHART.VALUES && jsonTemplate.CHART.VALUES.CATEGORY)) {
				return;
			}
			
			var category = jsonTemplate.CHART.VALUES.CATEGORY;
			
			/**
			 * 
			 * (danilo.ristovski@mht.net)
			 */
			/**
			 * MANAGE MULTIPLE CATEGORIES: if the chart type is on of following.
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "SUNBURST" || chartType.toUpperCase() == "WORDCLOUD" || 
					chartType.toUpperCase() == "TREEMAP" || chartType.toUpperCase() == "PARALLEL" || 
						chartType.toUpperCase() == "HEATMAP" || chartType.toUpperCase() == "CHORD")
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
			/**
			 * If the chart type is PIE, BAR, LINE, SCATTER or RADAR.
			 * (danilo.ristovski@mht.net)
			 */
			else	
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
			console.log(leftYAxisesPanel);
			console.log(rightYAxisesPanel);
			console.log(bottomXAxisesPanel);
			var globThis = this;
			Sbi.chart.designer.ChartColumnsContainerManager.resetContainers();
			console.log("*** A ***");
			var theStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
			console.log(theStorePool);
			var yCount = 1;
			
			Ext.Array.each(jsonTemplate.CHART.AXES_LIST.AXIS, function(axis, index){
				
				if(axis.type.toUpperCase() == "SERIE"){

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
						var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
						var hideAxisTitleTextbox = false;
						var hideGearTool = false;
						var hidePlusGear = false;
						
						if (chartType == "SUNBURST" || chartType == "PARALLEL" ||
								chartType == "WORDCLOUD" || chartType == "TREEMAP"
									|| chartType == "CHORD")
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
			console.log("*** H ***");
			console.log(jsonTemplate);
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
			console.log("*** I ***");
		}, 
		
		updateStep2Data: function(jsonTemplate) {
			// Updating step 2 data
			this.cModel.drop();
			this.cModel.erase();
			console.log("*** J ***");
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
			var removeNotFoundItemsFlag = true;
			var overwrittenJsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
					appliedPropertiesOnOldJson, 
					exported1st2ndSteps, 
					{
						removeNotFoundItemsFlag: removeNotFoundItemsFlag
					});
			
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
						exported1st2ndSteps, 
						{
							removeNotFoundItemsFlag: removeNotFoundItemsFlag
						});
				
			if(finalJson == true) {
				return Sbi.chart.designer.Designer.removeIdAttribute(newJsonTemplate);
			} else {
				return newJsonTemplate;
			}
			
		},
		removeIdAttribute: function(templateJson) {
			console.log("*** K ***");
			
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
		 * (extended by: danilo.ristovski@mht.net)
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
			/**
			 * (danilo.ristovski@mht.net)
			 */
			else 
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
			console.log("*** L ***");
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
		 * Called inside the ChartTypeSelector. Removes everything from the X-axis panel 
		 * if we move from BAR or LINE to SCATTER or RADAR, because for the last to we 
		 * can have ONLY ONE CATEGORY, while we can have more than one for the first pair 
		 * (BAR/LINE).
		 * (danilo.ristovski@mht.net)
		 */ 
		cleanCategoriesAxis: function()
		{
			this.bottomXAxisesPanel.setAxisData(Sbi.chart.designer.ChartUtils.createEmptyAxisData(true));			
			this.categoriesStore.removeAll();
			console.log("*** M ***");
		},
		
		tabChangeChecksMessages: function(oldJson, newJson) {
			var result = '';
			console.log("*** tab change ***");
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