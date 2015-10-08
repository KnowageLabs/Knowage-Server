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
    	
    	/**
    	 * This is a "back" (return) path for relative path implementation
    	 * (context name and context path improvement). This global variable
    	 * will be used by all JS files inside of this project (root of this
    	 * file) for purpose of dynamic path specification.
    	 * 
    	 * @author: danristo (danilo.ristovski@mht.net)
    	 */
    	realtivePathReturn: '../../..',
    	
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
		
		/**
		 * Get the missing JSON configuration elements (properties) in order to define
		 * their default values for any type of chart (including the BAR chart).
		 * (danilo.ristovski@mht.net)
		 * (lazar.kostic@mht.net)
		 */
		getConfigurationForStyle : function(style) {
//			Designer.styleName = style;
			
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
			switch(style) {				
				case "red":	

					templateToReturn = {
						/**
						 * Generic parameters are common for any type of chart and their default values 
						 * are going to be set through this 'generic' property of the JSON template.
						 */
						generic: {
							CHART: {
								height: "",
								width: "",	
								isCockpitEngine: "false",
								orientation: "horizontal",
								style: "fontFamily:Verdana;fontSize:16px;fontWeight:bold;backgroundColor:#FF0000;",
								styleName: "red",
//								styleCustom: styleCustom, 
								
								TITLE: {
									style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:normal;fontSize:26px;",
	 								text: "This is red chart"
								},
								
								SUBTITLE: {
									style: "align:center;color:#000000;fontFamily:Verdana;fontWeight:italic;fontSize:14px;",
									text: "Insert your subtitle"
								},
								
								EMPTYMESSAGE: {
									style: "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;",
									text: "Insert your empty message (when data for chart does not exist)"
								},
								
								LEGEND: {
									floating: false,
									layout: "",
									position: "",
									show: false,
									style: "align:;fontFamily:;fontSize:;fontWeight:;borderWidth:;color:;backgroundColor:;",
									x: 0,
									y: 0
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
								},
								
								// TODO: Need to adjust this tag with the mergeObjects concept of treating the multiply Y-axis panels
								AXES_LIST: {
									AXIS: [														 
								       {
								    	   type: 'Serie',
								    	  // position: "",
								    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
								    	   
								    	   MAJORGRID: {
								    		   interval: "",
								    		   style: "typeline:;color:#D8D8D8;"
							    		   },
								       
							    		   MINORGRID: {
							    			   interval: "", 
							    			   style: "typeline:;color:#E0E0E0;"
						    			   },
							    		   
							    		   TITLE: {
							    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   text: "red measure axis title" 
						    			   }
						    		   }, {
						    			   type: 'Category',
						    			  // position: "", 
						    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   
						    			   TITLE: {
						    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    				   text: ""
					    				   }
				    				   }
							        ]
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
						wordcloud: {								
							CHART: {		
								type: "WORDCLOUD",
								
								maxAngle: 121,
								maxFontSize: 51,
								maxWords: 51,
								minAngle: 61, 
								sizeCriteria: 'serie',
								wordPadding: 5									
							}
						},
						
						bar: {
							CHART: {
								type: "BAR"
							}
						},
						
						line: {
							CHART: {
								type: "LINE"
							}
						},
						
						pie: {
							CHART: {
								type: "PIE"
							}
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the TREEMAP chart
						 */
						treemap: {
							CHART: {		
								type: "TREEMAP"								
							}
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the PARALLEL chart
						 */
						parallel: {								
							CHART: {
								type: "PARALLEL",
								
								AXES_LIST: {									
									style:"axisColNamePadd:16;brushWidth:10;axisColor:#FF6600;brushColor:#339966;"										
								},
								
								LIMIT: {					
									/**
									 * 'serieFilterColumn' attribute depends on available (picked) SERIE items (values)
									 * for the chart, since it is always empty as a default value
									 * (danilo.ristovski@mht.net)
									 */
									style:"maxNumberOfLines:20;orderTopMinBottomMax:bottom;serieFilterColumn:;"										
								},
								
								PARALLEL_TOOLTIP: {										
									style:"fontFamily:Cambria;fontSize:18px;minWidth:10;maxWidth:50;minHeight:5;maxHeight:50;padding:1;border:1;borderRadius:1;"										
								},
								
								LEGEND: {										
									TITLE: {											
										style:"fontFamily:Arial;fontSize:9px;fontWeight:bold;"											
									},
									
									ELEMENT: {											
										style:"fontFamily:Cambria;fontSize:12px;fontWeight:normal;"											
									}
									
								},
								
								AXES_LIST: {
									style: "axisColNamePadd:15;brushWidth:12;axisColor:#FF6600;brushColor:#339966;"
								}
							}
							
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the HEATMAP chart
						 */
						heatmap: {								
							CHART: {		
								type: "HEATMAP",
								
								LEGEND: {		
									symbolWidth: 50,
									style: "align:center;"									
								},
								
								TOOLTIP: {										
									style: "fontFamily:Gungsuh;fontSize:24px;color:#003366;"										
								}									
							}								
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the RADAR chart
						 */
						radar: {
							CHART: {		
								type: "RADAR"								
							}
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the SCATTER chart
						 */
						scatter: {	
							CHART: {	
								type: "SCATTER",	
								
								zoomType:"xy",
								
								AXES_LIST: {
									AXIS: [														 
								       {
								    	   //alias:'Y', 
								    	   type: 'Serie',
								    	  // position: "",
								    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
								    	   //id: Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id,
								    	   
								    	   MAJORGRID: {
								    		   interval: "",
								    		   style: "typeline:;color:#D8D8D8;"
							    		   },
								       
							    		   MINORGRID: {
							    			   interval: "", 
							    			   style: "typeline:;color:#E0E0E0;"
						    			   },
							    		   
							    		   TITLE: {
							    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   text: "" 
						    			   }
						    		   }, {
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
						    			   
						    			   TITLE: {
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
						gauge: {								
							CHART: {		
								type: "GAUGE",
								
								// TODO: Need to adjust this tag with the mergeObjects concept of treating the multiply Y-axis panels
								AXES_LIST: {
									AXIS: [{
							    	   lineColor: "#FF0000", 
							    	   lineWidth: "1",
							    	   
							    	   max: "60",
							    	   min: "1", 
							    	   
							    	   minorTickColor: "#FF0000",
							    	   minorTickInterval: "",									    	   
							    	   minorTickLength: "10",
							    	   minorTickPosition: "inside",
							    	   minorTickWidth: "1",
							    	   
							    	   offset:"1",
							    	   
						               tickColor: "#FF0000",
						               tickLength: "10",
						               tickPixelInterval: "30",
						               tickPosition: "inside",
						               tickWidth: "2", 
							    	   
							    	   
							    	   type: 'Serie',
							    	  // position: "",
							    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
							    	   
							    	   MAJORGRID: {
							    		   interval: "",
							    		   style: "typeline:;color:#FF0000;"
						    		   },
							       
						    		   MINORGRID: {
						    			   interval: "", 
						    			   style: "typeline:;color:#FF0000;"
					    			   },
						    		   
						    		   TITLE: {
						    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   text: "red measure axis title" 
					    			   }
					    		   }, {
					    			   type: 'Category',
					    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
					    			   
					    			   TITLE: {
					    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
					    				   text: ""
				    				   }
			    				   }]
								},
								
								PANE: {											
									endAngle:"121",
									startAngle:"-121"
								},
								
								// 14.9 (start)
								PLOTBANDS: {
									
								},
								
								LABELS: {
									distance: "5",
									rotation: "1"
								},
								
								/**
								 * Added VALUE tag with SERIE subtag so every new serie item
								 * can have parameters with these values (according to the 
								 * current style - in this case, the RED style).
								 * @author: danristo (danilo.ristovski@mht.net)
								 */
								VALUES: {
									SERIE: {
																		       
								        animation: "true",
								        borderColor: "#FF0000",
								        color:"",
								        groupingFunction:"SUM",							        
							        	orderType:"",
							        	pointInterval:"1",
							        	postfixChar:"",
							        	precision:"",
							            prefixChar:"",
							            showInLegend: "true",
							            showValue: "true",
							            type:"",
							            
							            TOOLTIP: {
							            	backgroundColor:"",
							                style:"color:;fontFamily:;fontWeight:;fontSize:;",
							                templateHtml:""
						            	},
						            	
						            	DIAL: {
						            		backgroundColorDial:"#FF0000"
						            	},
						            	
						            	DATA_LABELS: {
						            		colorDataLabels:"#FF0000",
						            		formatDataLabels:"",
						            		yPositionDataLabels:"-40"
						            	}
									}
								}
							}								
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the SUNBURST chart
						 */
						sunburst: {								
							CHART: {
								type: "SUNBURST",
								
								opacMouseOver:"10",
								
								TOOLBAR: {										
									style: "position:bottom;height:50;width:60;spacing:5;tail:10;percFontColor:#FF9900;fontFamily:Calibri;fontWeight:normal;fontSize:14px;"
								},
								
								TIP: {										
									style: "fontFamily:Cambria;fontWeight:bold;fontSize:14px;color:#FF0000;align:;width:200;",
									text: "Insert text here"										
								}									
							}								
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the CHORD chart
						 */
						chord: {
							CHART: {	
								type: "CHORD"						
							}	
						}
					};
					
					return templateToReturn;
					
					break;
					
				case "blue":
					
					templateToReturn = {
						/**
						 * Generic parameters are common for any type of chart and their default values 
						 * are going to be set through this 'generic' property of the JSON template.
						 */
						generic: {
							CHART: {
								height: "",
								width: "",	
								isCockpitEngine: "false",
								orientation: "horizontal",
								style: "fontFamily:;fontSize:;fontWeight:;backgroundColor:;",
								styleName: "blue",
//								styleCustom: styleCustom, 
								
								TITLE: {
									style: "align:center;color:;fontFamily:;fontWeight:;fontSize:;",
	 								text: ""
								},
								
								SUBTITLE: {
									style: "align:center;color:;fontFamily:;fontWeight:;fontSize:;",
									text: ""
								},
								
								EMPTYMESSAGE: {
									style: "align:left;color:#FF0000;fontFamily:Verdana;fontWeight:normal;fontSize:10px;",
									text: "Insert your empty message (when data for chart does not exist)"
								},
								
								LEGEND: {
									floating: false,
									layout: "",
									position: "",
									show: false,
									style: "align:;fontFamily:;fontSize:;fontWeight:;borderWidth:;color:;backgroundColor:;",
									x: 0,
									y: 0
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
								},
								
								// TODO: Need to adjust this tag with the mergeObjects concept of treating the multiply Y-axis panels
								AXES_LIST: {
									AXIS: [														 
								       {
								    	   type: 'Serie',
								    	  // position: "",
								    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
								    	   
								    	   MAJORGRID: {
								    		   interval: "",
								    		   style: "typeline:;color:#D8D8D8;"
							    		   },
								       
							    		   MINORGRID: {
							    			   interval: "", 
							    			   style: "typeline:;color:#E0E0E0;"
						    			   },
							    		   
							    		   TITLE: {
							    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   text: "red measure axis title" 
						    			   }
						    		   }, {
						    			   type: 'Category',
						    			  // position: "", 
						    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   
						    			   TITLE: {
						    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    				   text: ""
					    				   }
				    				   }
							        ]
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
						wordcloud: {								
							CHART: {		
								type: "WORDCLOUD",
								
								maxAngle: 121,
								maxFontSize: 51,
								maxWords: 51,
								minAngle: 61, 
								sizeCriteria: 'serie',
								wordPadding: 5									
							}
						},
						
						bar: {
							CHART: {
								type: "BAR"
							}
						},
						
						line: {
							CHART: {
								type: "LINE"
							}
						},
						
						pie: {
							CHART: {
								type: "PIE"
							}
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the TREEMAP chart
						 */
						treemap: {
							CHART: {		
								type: "TREEMAP"								
							}
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the PARALLEL chart
						 */
						parallel: {								
							CHART: {
								type: "PARALLEL",
								
								AXES_LIST: {									
									style:"axisColNamePadd:16;brushWidth:10;axisColor:#FF6600;brushColor:#339966;"										
								},
								
								LIMIT: {					
									/**
									 * 'serieFilterColumn' attribute depends on available (picked) SERIE items (values)
									 * for the chart, since it is always empty as a default value
									 * (danilo.ristovski@mht.net)
									 */
									style:"maxNumberOfLines:20;orderTopMinBottomMax:bottom;serieFilterColumn:;"										
								},
								
								PARALLEL_TOOLTIP: {										
									style:"fontFamily:Cambria;fontSize:18px;minWidth:10;maxWidth:50;minHeight:5;maxHeight:50;padding:1;border:1;borderRadius:1;"										
								},
								
								LEGEND: {										
									TITLE: {											
										style:"fontFamily:Arial;fontSize:9px;fontWeight:bold;"											
									},
									
									ELEMENT: {											
										style:"fontFamily:Cambria;fontSize:12px;fontWeight:normal;"											
									}
									
								},
								
								AXES_LIST: {
									style: "axisColNamePadd:15;brushWidth:12;axisColor:#FF6600;brushColor:#339966;"
								}
							}
							
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the HEATMAP chart
						 */
						heatmap: {								
							CHART: {		
								type: "HEATMAP",
								
								LEGEND: {		
									symbolWidth: 50,
									style: "align:center;"									
								},
								
								TOOLTIP: {										
									style: "fontFamily:Gungsuh;fontSize:24px;color:#003366;"										
								}									
							}								
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the RADAR chart
						 */
						radar: {
							CHART: {		
								type: "RADAR"								
							}
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the SCATTER chart
						 */
						scatter: {	
							CHART: {	
								type: "SCATTER",	
								
								zoomType:"",
								
								AXES_LIST: {
									AXIS: [														 
								       {
								    	   //alias:'Y', 
								    	   type: 'Serie',
								    	  // position: "",
								    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
								    	   //id: Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id,
								    	   
								    	   MAJORGRID: {
								    		   interval: "",
								    		   style: "typeline:;color:#D8D8D8;"
							    		   },
								       
							    		   MINORGRID: {
							    			   interval: "", 
							    			   style: "typeline:;color:#E0E0E0;"
						    			   },
							    		   
							    		   TITLE: {
							    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
							    			   text: "" 
						    			   }
						    		   }, {
						    			   //alias:'X', 
						    			   type: 'Category',
						    			   //position: "", 
						    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   
						    			   /**
						    			    * Specific for this chart type
						    			    */
						    			   startOnTick: "", 
						    			   showLastLabel: "",
						    			   endOnTick: "",
						    			   
						    			   TITLE: {
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
						gauge: {								
							CHART: {		
								type: "GAUGE",
								
								// TODO: Need to adjust this tag with the mergeObjects concept of treating the multiply Y-axis panels
								AXES_LIST: {
									AXIS: [{
							    	   lineColor: "#FF0000", 
							    	   lineWidth: "1",
							    	   
							    	   max: "60",
							    	   min: "1", 
							    	   
							    	   minorTickColor: "#FF0000",
							    	   minorTickInterval: "",									    	   
							    	   minorTickLength: "10",
							    	   minorTickPosition: "inside",
							    	   minorTickWidth: "1",
							    	   
							    	   offset:"1",
							    	   
						               tickColor: "#FF0000",
						               tickLength: "10",
						               tickPixelInterval: "30",
						               tickPosition: "inside",
						               tickWidth: "2", 
							    	   
							    	   
							    	   type: 'Serie',
							    	  // position: "",
							    	   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;opposite:false;",
							    	   
							    	   MAJORGRID: {
							    		   interval: "",
							    		   style: "typeline:;color:#FF0000;"
						    		   },
							       
						    		   MINORGRID: {
						    			   interval: "", 
						    			   style: "typeline:;color:#FF0000;"
					    			   },
						    		   
						    		   TITLE: {
						    			   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
						    			   text: "red measure axis title" 
					    			   }
					    		   }, {
					    			   type: 'Category',
					    			   style: "rotate:;align:;color:;fontFamily:;fontWeight:;fontSize:;",
					    			   
					    			   TITLE: {
					    				   style: "align:;color:;fontFamily:;fontWeight:;fontSize:;",
					    				   text: ""
				    				   }
			    				   }]
								},
								
								PANE: {											
									endAngle:"121",
									startAngle:"-121"
								},
								
								// 14.9 (start)
								PLOTBANDS: {
									
								},
								
								LABELS: {
									distance: "5",
									rotation: "1"
								},
								
								/**
								 * Added VALUE tag with SERIE subtag so every new serie item
								 * can have parameters with these values (according to the 
								 * current style - in this case, the RED style).
								 * @author: danristo (danilo.ristovski@mht.net)
								 */
								VALUES: {
									SERIE: {
																		       
								        animation: "true",
								        borderColor: "#FF0000",
								        color:"",
								        groupingFunction:"SUM",							        
							        	orderType:"",
							        	pointInterval:"1",
							        	postfixChar:"",
							        	precision:"",
							            prefixChar:"",
							            showInLegend: "true",
							            showValue: "true",
							            type:"",
							            
							            TOOLTIP: {
							            	backgroundColor:"",
							                style:"color:;fontFamily:;fontWeight:;fontSize:;",
							                templateHtml:""
						            	},
						            	
						            	DIAL: {
						            		backgroundColorDial:"#FF0000"
						            	},
						            	
						            	DATA_LABELS: {
						            		colorDataLabels:"#FF0000",
						            		formatDataLabels:"",
						            		yPositionDataLabels:"-40"
						            	}
									}
								}
							}								
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the SUNBURST chart
						 */
						sunburst: {								
							CHART: {
								type: "SUNBURST",
								
								opacMouseOver:"10",
								
								TOOLBAR: {										
									style: "position:;height:;width:;spacing:;tail:10;percFontColor:#FF9900;fontFamily:;fontWeight:normal;fontSize:14px;"
								},
								
								TIP: {										
									style: "fontFamily:Cambria;fontWeight:bold;fontSize:14px;color:#FF0000;align:;width:200;",
									text: "Insert text here"										
								}									
							}								
						},
						
						/**
						 * Default (predefined) values for the specific parameters of the CHORD chart
						 */
						chord: {
							CHART: {	
								type: "CHORD"						
							}	
						}
					};	
					
					return templateToReturn;
					
					break;										
			}			
		},
				
		initialize: function(sbiExecutionId, userId, hostName, serverPort, docLabel, jsonTemplate, datasetLabel, chartLibNamesConfig, isCockpit) {

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
				newChart = true;
				jsonTemplate = baseTemplate;
			}			
			
			/**
			 * Predefined style type for any chart is RED.
			 * (danilo.ristovski@mht.net)
			 */		
			Designer.styleName = (jsonTemplate.CHART.styleName) ? (jsonTemplate.CHART.styleName) : "red";
			
			/**
			 * Merging JSON templates of specified chart types with the base JSON template
			 * (of type BAR) in order to make the union of all of the JSON elements within
			 * these two types - the base one and the current one. 			 
			 * (danristo :: danilo.ristovski@mht.net) 
			 */
			if (jsonTemplate.CHART.type.toUpperCase() == 'PIE' 
				|| jsonTemplate.CHART.type.toUpperCase() == 'SUNBURST'
					|| jsonTemplate.CHART.type.toUpperCase() == 'WORDCLOUD'
						|| jsonTemplate.CHART.type.toUpperCase() == 'TREEMAP'
							|| jsonTemplate.CHART.type.toUpperCase() == 'PARALLEL'
								|| jsonTemplate.CHART.type.toUpperCase() == 'RADAR'
									|| jsonTemplate.CHART.type.toUpperCase() == 'SCATTER'
										|| jsonTemplate.CHART.type.toUpperCase() == 'HEATMAP'
											|| jsonTemplate.CHART.type.toUpperCase() == 'CHORD'
												|| jsonTemplate.CHART.type.toUpperCase() == 'GAUGE') {
								
				/**
				 * If there is just one axis (Y-axis and no X-axis), like in the GAUGE chart and 
				 * if the AXIS property (tag) is not in the form of an array (it is just one object
				 * with some properties, put this object inside an array anyhow, so code could
				 * process it.
				 * (danristo :: danilo.ristovski@mht.net) 
				 */
				if (jsonTemplate.CHART.AXES_LIST.AXIS.length == undefined) {
					var axisTemp = jsonTemplate.CHART.AXES_LIST.AXIS;
					var axisArray = new Array();
					
					jsonTemplate.CHART.AXES_LIST.AXIS = axisArray;
					jsonTemplate.CHART.AXES_LIST.AXIS.push(axisTemp);
				}				
				
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(baseTemplate, jsonTemplate);	
				
			}				
			
			/**
			 * Set the predefined values for the generic parameters of the newly created chart 
			 * (danristo :: danilo.ristovski@mht.net) 
			 */			
			var applyAxes = true;
			var configApplyAxes = {applyAxes: applyAxes};
			
			if (newChart == true) {
				jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
						jsonTemplate, 
						Designer.getConfigurationForStyle(Designer.styleName).generic,
						configApplyAxes);
			}
			
			/**
			 * If the chart is already existing (not just created) and if it is of the 
			 * GAUGE type, set the plotband store that keeps the data about the plots
			 * that are linked to the particular chart of this type. Afterwards, when
			 * we open the Axis style configuration for this chart type we will have
			 * the grid panel for the plotbands populated with existing plots (intervals).
			 * (danristo :: danilo.ristovski@mht.net) 
			 */
			if (jsonTemplate.CHART.type.toUpperCase() == "GAUGE") {
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
			
			this.chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
			this.coreServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getCoreWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
			this.chartExportWebServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartExportWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
						
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
			        text: "Pick chart type:",
			        //margin: '5 3 3 0'
			});
			
			this.chartTypeSelector = Ext.create('Sbi.chart.designer.ChartTypeSelector_2', {
 				region: 'north',
 				//minHeight: 50		
 			});
			
			var onSelectJsonTemplate = "";
						
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
							|| isChartParallel || isChartHeatmap || isChartGauge 
								|| isChartChord || isChartPie) {
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
					if (isChartSunburst || isChartWordCloud || isChartGauge) {
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
					if (isChartGauge) {
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
				
				data: [
				 	{style: LN('sbi.chartengine.designer.stylecolor.red'), styleAbbr: "red"},
				 	{style: LN('sbi.chartengine.designer.stylecolor.blue'), styleAbbr: "blue"},
				 	//{style: LN('sbi.chartengine.designer.stylecolor.green'), styleAbbr: "green"}
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
			this.stylePickerCombo = Ext.create ( "Ext.form.ComboBox", {
//				fieldLabel: 'Choose style',
			    store: styleStore,
			    id: "stylePickerComboId",
			    queryMode: 'local',
			    displayField: 'style',
			    valueField: 'styleAbbr',
			    value: Designer.styleName,
			    editable: false,
			    padding: "5 0 10 0",
			    width: 170,
			    
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
				    		
				    		/* 4.9. Keep the VALUES tag (keep them when only changing the   */
				    		
//					    		var colorpalleteToKeep = jsonTemplate.CHART.COLORPALLETE;
//					    		var yAxisAliasToKeep = jsonTemplate.CHART.AXES_LIST.AXIS[0].alias;
//					    		var xAxisAliasToKeep = jsonTemplate.CHART.AXES_LIST.AXIS[1].alias;
				    		
				    		var valuesToKeep = jsonTemplate.CHART.VALUES;
				    		
				    		var yAxesAliasToKeep = new Array();
				    		var xAxisAliasToKeep = {};
				    		
				    		for (var i=0; i < jsonTemplate.CHART.AXES_LIST.AXIS.length; i++) {
				    			if (i < jsonTemplate.CHART.AXES_LIST.AXIS.length-1) {
				    				yAxesAliasToKeep.push(jsonTemplate.CHART.AXES_LIST.AXIS[i]);
			    				}
				    			else
			    				{
				    				xAxisAliasToKeep = jsonTemplate.CHART.AXES_LIST.AXIS[i];
			    				}
			    			}
				    		
				    		/* 4.9. Clear the JSON template */
				    		jsonTemplate.CHART = {};		// clean the JSON object (template)
				    		jsonTemplate.CHART.AXES_LIST = {};
				    		jsonTemplate.CHART.AXES_LIST.AXIS = new Array(yAxesAliasToKeep.length+1);
				    		
				    		// keep the series axes
				    		for (var i=0; i < yAxesAliasToKeep.length; i++) {
				    			jsonTemplate.CHART.AXES_LIST.AXIS[i] = yAxesAliasToKeep[i];
			    			}
				    		
				    		// keep the category axis
				    		jsonTemplate.CHART.AXES_LIST.AXIS[yAxesAliasToKeep.length] = xAxisAliasToKeep;
				    		
				    		// keep the values (items) on these axes (X and Y)
				    		jsonTemplate.CHART.VALUES = valuesToKeep;
				    		
				    		// Previously I have defined generic configuration first and then the second (specific) configuration
//								var genericConfigurationForStyle = Designer.getConfigurationForStyle(k.data.styleAbbr).generic;
														
							/* Keep the ID of the left Y-axis panel when changing the style of the chart
							 * since the one (left Y-axis panel) is always present */
//								console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id);
//								jsonTemplate.CHART.AXES_LIST.AXIS[0].id = "Axis_6";
															
							/* Clean the chart type value because it is maybe going to be changed. Case: we firstly change
							 * type of the chart and then we change the style - it is needed because of the merging function,
							 * since we have the old chart type in the old JSON template that was linked to the previous
							 * (initial) chart (document) and we want to change that value  */
//								jsonTemplate.CHART.type = "";

							/**
							 * Reset (refresh, modify) the 'styleName' field of the Designer, also
							 */
							Designer.styleName = k.data.styleAbbr;
							
							/**
							 * Reset the JSON template for the document (chart) after changing the 
							 * previously selected style (changing the selected item of the combo)
							 */
//								jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(jsonTemplate,genericConfigurationForStyle);
							
							/**
							 * Set the predefined values for specific parameters of different chart types.
							 */								
							// TODO: See the differences with the new code (Benedetto)
							
							var chartTypeToLowerCase = chartType.toLowerCase();
							
							jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
									jsonTemplate, 
									Designer.getConfigurationForStyle(k.data.styleAbbr)[chartTypeToLowerCase], 
									configApplyAxes);							
							
							jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(Designer.getConfigurationForStyle(k.data.styleAbbr).generic,jsonTemplate);
														
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
			
			var previewTools = [{ xtype: 'tbfill' }, {
	            xtype: 'image',
	            src: Sbi.chart.designer.Designer.realtivePathReturn + '/img/refresh.png',
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
	      								var src = Sbi.chart.designer.Designer.realtivePathReturn + '/img/preview-not-available.png';
	      								setPreviewImage(src);
	      							}
      							);

								
							}
							,
  							function (response) {
  								var src = Sbi.chart.designer.Designer.realtivePathReturn + '/img/preview-not-available.png';
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
			
			this.leftYAxisesPanel.on ("newSerieItem", 
				function(newSerieItem) {
					// This is not working!!! Appending the current style parameters values to the newly dropped serie item
					//console.log("!!! newSerieItem !!!");
					
					// TODO: This is only for the GAUGE (specific VALUES tag)
					var valueForSerieToAddInJson = {
						CHART: {
							VALUES: {
								SERIE: {
									DATA_LABELS: {
										colorDataLabels: "#00FF00",
										formatDataLabels: "{y}",
										yPositionDataLabels: "-40"
									},
									
									DIAL: {
										backgroundColorDial: ""
									},
									
									TOOLTIP: {
										backgroundColor: "",
										style: "color:;fontFamily:;fontWeight:;fontSize:;align:;",
										templateHtml: ""
									},
									
									//axis: "Y",
									color: "",
									column: newSerieItem.data.serieColumn,
									name: newSerieItem.data.axisName,
									groupingFunction: newSerieItem.data.serieGroupingFunction,
									orderType: "",
									postfixChar: "", 
									precision: "",
									prefixChar: "",
									showValue: true,
									type: ""
								}
							}
						}
					};
					
					jsonTemplate = Sbi.chart.designer.ChartUtils.mergeObjects(
							jsonTemplate, 
							valueForSerieToAddInJson,
							configApplyAxes);	
					
					return true;
				}
			);
			
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
  	  						if (chartType == "HEATMAP") {
	  	  						if (this.store.data.length == 0 && data.records.length == 1) {
	  	  							if (data.records[0].data.categoryDataType != "Timestamp") {	  	  								
	  	  								/**
	  	  								 * Show the message that tells user that he should firstly define
	  	  								 * (drop) the item for the categories (attributes) container that
	  	  								 * is of a DATE type (Timestamp).
	  	  								 * (danilo.ristovski@mht.net)
	  	  								 */
		  	  							Ext.Msg.show({
	  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongdatatypefirst.title"),
	  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.wrongdatatypefirst.warningmessage"),
	  		            					icon : Ext.Msg.WARNING,
	  		            					closable : false,
	  		            					buttons : Ext.Msg.OK,
	  		            					minWidth: 200,
	  		            					
	  		            					buttonText : {
	  		            						ok : LN('sbi.chartengine.generic.ok')
	  		            					}
  	  									});	
	  	  								
	  	  								return false;
  	  								}	  	  								
  	  							}
	  	  						else if (this.store.data.length == 1 && data.records.length == 1) {	  	  	
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
	  	  							 * (danilo.ristovski@mht.net)
	  	  							 */
	  	  							if (this.store.data.items[0].data.categoryDataType != "Timestamp" && 
	  	  									data.records[0].data.categoryDataType != "Timestamp") {
	  	  								Ext.Msg.show({
	  		            					title : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.timestampdataneeded.title"),	
	  		            					message : LN("sbi.chartengine.categorypanelitemsorder.heatmapchart.timestampdataneeded.warningmessage"),	
	  		            					icon : Ext.Msg.WARNING,
	  		            					closable : false,
	  		            					buttons : Ext.Msg.OK
  										});
	  	  								
	  	  								return false;
  	  								}
  	  							}
	  	  						else {
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
  	  						 * TODO: check if this works
  	  						 * (danilo.ristovski@mht.net)
  	  						 */
  	  						if (data.records.length > 1 && (chartType == "RADAR" || chartType == "SCATTER" || 
  	  								chartType == "PARALLEL" || chartType == "HEATMAP" || chartType == "CHORD")) {
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
			  	      				 * (danilo.ristovski@mht.net)
			  	      				 */			  	      				
			  	      				if(data.records[0].get('categoryColumn') == categoryItem.get('categoryColumn') 
			  	      						|| (this.store.data.length == 1 && 
			  	      								(chartType == "RADAR" || chartType == "SCATTER")) 
			  	      									|| (this.store.data.length == 2 && 
			  	      											(chartType == "PARALLEL" || 
			  	      													chartType == "HEATMAP" || 
			  	      														chartType == "CHORD"))) {
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
						    	if (chartType.toUpperCase() == 'HEATMAP') {
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
            					buttonText : {
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
						typeOfChart == "HEATMAP" || typeOfChart == "CHORD" || 
							typeOfChart == "PIE" || typeOfChart == "RADAR") {
				/**
				 * Hide the bottom (X) axis title textbox	
				 */
				Ext.getCmp("chartBottomCategoriesContainer").tools[0].hidden = true;
				
				/**
				 * Hide the gear icon on the bottom (X) axis panel	
				 */
				if (typeOfChart != "HEATMAP") {
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
			if (typeOfChart == "GAUGE") {
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
  		            src: Sbi.chart.designer.Designer.realtivePathReturn + '/img/save.png',
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
  		            					buttonText : {
  		            						ok : LN('sbi.generic.save'),
  		            						cancel : LN('sbi.generic.cancel')
  		            					},
  		            					fn : function(buttonValue, inputText, showConfig){
  		            						if (buttonValue == 'ok') {  		            							
  		            							Ext.getBody().mask(LN('sbi.chartengine.designer.savetemplate.loading'), 'x-mask-loading');
  		            							  		
  		            							  		// TODO: See what is happening here   		            							
  		            							var exportedAsOriginalJson = Sbi.chart.designer.Designer.exportAsJson(true);
  		            							  	  		            							
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
  		            src: Sbi.chart.designer.Designer.realtivePathReturn + '/img/saveAndGoBack.png',
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
											buttonText : {
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
		},			
			
		loadAxesAndSeries: function(jsonTemplate) {
			var leftYAxisesPanel = this.leftYAxisesPanel;
			var rightYAxisesPanel = this.rightYAxisesPanel;
			var bottomXAxisesPanel = this.bottomXAxisesPanel;
			
			var globalScope = this;
			Sbi.chart.designer.ChartColumnsContainerManager.resetContainers();
			
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
						var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();
						var hideAxisTitleTextbox = false;
						var hideGearTool = false;
						var hidePlusGear = false;
						
						if (chartType == "SUNBURST" || chartType == "PARALLEL" ||
								chartType == "WORDCLOUD" || chartType == "TREEMAP"
									|| chartType == "CHORD" || chartType == "PIE" || chartType == "RADAR"
										||chartType == "SCATTER" || chartType == "HEATMAP") {
							
							if (chartType != "RADAR")
							{
								hideAxisTitleTextbox = true;
								
								if (chartType != "HEATMAP")
									hideGearTool = true;
							}
							
							hidePlusGear = true;
						}
						// (danilo.ristovski@mht.net)
//						else if (chartType == "HEATMAP") {
//							
//								hideAxisTitleTextbox = true;
//						}
						// END
						
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
							"plusHidden":hidePlusGear
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
							
							if (Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase() == "GAUGE") {	
																
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
							globalScope.seriesBeforeDropStore.add(newCol);
							
							store.add(newCol);
						}
					});
				});
			}
			
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
						exported1st2ndSteps, 
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
			 * danristo (danilo.ristovski@mht.net)
			 */
			var chartViewModelData = this.cViewModel.data.configModel.data;
			
			var numberOfSerieItems = Sbi.chart.designer.ChartUtils.getSeriesDataAsOriginalJson().length;
			
			/**
			 * Validation for Step 1 if the mandatory items are not specified
			 * @commentBy danristo (danilo.ristovski@mht.net)
			 */
			if (numberOfSerieItems == 0) {
				
				if (chartType == "TREEMAP")
				{
					/**
					 * TREEMAP chart needs exactly one serie item.
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					errorMsg += "- " + LN('sbi.chartengine.validation.addserie.treemap') + '<br>';
				}
				else
				{
					errorMsg += "- " + LN('sbi.chartengine.validation.addserie') + '<br>';
				}
				
			}
			
			if (Sbi.chart.designer.ChartUtils.getCategoriesDataAsOriginalJson() == null && chartType != "GAUGE") {
				errorMsg += "- " + LN('sbi.chartengine.validation.addcategory') + '<br>';
			}	
			/**
			 * danristo (danilo.ristovski@mht.net)
			 */
			else 
			{
				var categoriesAsJson = Sbi.chart.designer.ChartUtils.getCategoriesDataAsOriginalJson();				
				
				if ((chartType == "PARALLEL" || chartType == "HEATMAP") &&
						categoriesAsJson.length != 2) {
					errorMsg += "- " + LN("sbi.chartengine.validation.exactlyTwoCategories") + '<br>'; 
				}
				else if (chartType == "TREEMAP" && categoriesAsJson.length < 2) {
					errorMsg += "- " + LN("sbi.chartengine.validation.atLeastTwoCategories") + '<br>';
				}
			}
			
			/**
			 * PIE, PARALLEL, HEATMAP chart: Instead of forcing user to specify at least one or two colors in the color pallete, 
			 * we provided  possibility for him not to specify any color while the VM will take care of 
			 * this and  dedicate predefined color as set of colors for the PIE chart.
			 * 
			 * @commentBy: danristo (danilo.ristovski@mht.net)
			 */
			
			/**
			 * Validation for Step 2 if the mandatory fields are not defined for particular chart type
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			if (chartType == "GAUGE") {								
				/**
				 * STEP 1 -> Axis style configuration popup
				 * 
				 * *** NOTE: In order to collect the data defined in the Axis style configuration
				 * popup window (all data about the axis) we need to call the getAxesDataAsOriginalJson
				 * function from the ChartUtils.js. This call will be done just for those charts that 
				 * need these data (e.g. GAUGE chart).
				 */
				var gaugeStep1YAxisPopupData = Sbi.chart.designer.ChartUtils.getAxesDataAsOriginalJson();
				
				/**
				 * Optional parameters: endOnTickGauge, lineColor, tickPosition, tickColor, minorTickPosition, minorTickColor
				 * Mandatory parameters: 
				 * 		(1) Axis additional parameters:
				 * 			-	min, max, offset, lineWidth, 
				 * 		(2) Main tick parameters:
				 * 			-	tickPixelInterval, tickWidth, tickLength,  
				 * 		(3) Minor tick parameters:
				 * 			-	minorTickPixelInterval, minorTickWidth, minorTickLength, 
				 * 		(4) Label parameters:
				 * 			-	distance, rotation
				 */
				
				/**
				 *  Axis additional parameters
				 */								
				(gaugeStep1YAxisPopupData[0].min=="" || gaugeStep1YAxisPopupData[0].min==null || gaugeStep1YAxisPopupData[0].min==undefined) ? 
						errorMsg += "- " + "<b>Min value</b> not specified [Step 1 -> Axis additional parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].max=="" || gaugeStep1YAxisPopupData[0].max==null || gaugeStep1YAxisPopupData[0].max==undefined) ? 
						errorMsg += "- " + "<b>Max value</b> not specified [Step 1 -> Axis additional parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].offset=="" || gaugeStep1YAxisPopupData[0].offset==null || gaugeStep1YAxisPopupData[0].offset==undefined) ? 
						errorMsg += "- " + "<b>Offset</b> not specified [Step 1 -> Axis additional parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].lineWidth=="" || gaugeStep1YAxisPopupData[0].lineWidth==null || gaugeStep1YAxisPopupData[0].lineWidth==undefined) ? 
						errorMsg += "- " + "<b>Line width</b> not specified [Step 1 -> Axis additional parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				
				/**
				 *  Main tick parameters
				 */							
				(gaugeStep1YAxisPopupData[0].tickPixelInterval=="" || gaugeStep1YAxisPopupData[0].tickPixelInterval==null || gaugeStep1YAxisPopupData[0].tickPixelInterval==undefined) ? 
						errorMsg += "- " + "<b>Tick pixel interval</b> not specified [Step 1 -> Main tick parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].tickWidth=="" || gaugeStep1YAxisPopupData[0].tickWidth==null || gaugeStep1YAxisPopupData[0].tickWidth==undefined) ? 
						errorMsg += "- " + "<b>Tick width</b> not specified [Step 1 -> Main tick parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].tickLength=="" || gaugeStep1YAxisPopupData[0].tickLength==null || gaugeStep1YAxisPopupData[0].tickLength==undefined) ? 
						errorMsg += "- " + "<b>Tick length</b> not specified [Step 1 -> Main tick parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				
				/**
				 *  Minor tick parameters
				 */				
				(gaugeStep1YAxisPopupData[0].minorTickInterval=="" || gaugeStep1YAxisPopupData[0].minorTickInterval==null || gaugeStep1YAxisPopupData[0].minorTickInterval==undefined) ? 
						errorMsg += "- " + "<b>Minor tick interval</b> not specified [Step 1 -> Minor tick parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].minorTickWidth=="" || gaugeStep1YAxisPopupData[0].minorTickWidth==null || gaugeStep1YAxisPopupData[0].minorTickWidth==undefined) ? 
						errorMsg += "- " + "<b>Minor tick width</b> not specified [Step 1 -> Minor tick parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].minorTickLength=="" || gaugeStep1YAxisPopupData[0].minorTickLength==null || gaugeStep1YAxisPopupData[0].minorTickLength==undefined) ? 
						errorMsg += "- " + "<b>Minor tick length</b> not specified [Step 1 -> Minor tick parameters]" + '<br>' : errorMsg; // TODO: Make LN()
				
				/**
				 * Labels parameters
				 */
				(gaugeStep1YAxisPopupData[0].distance=="" || gaugeStep1YAxisPopupData[0].distance==null || gaugeStep1YAxisPopupData[0].distance==undefined) ? 
						errorMsg += "- " + "<b>Distance</b> not specified [Step 1 -> Labels parameters]" + '<br>' : ""; // TODO: Make LN()
				(gaugeStep1YAxisPopupData[0].rotation=="" || gaugeStep1YAxisPopupData[0].rotation==null || gaugeStep1YAxisPopupData[0].rotation==undefined) ? 
						errorMsg += "- " + "<b>Rotation</b> not specified [Step 1 -> Labels parameters]" + '<br>' : ""; // TODO: Make LN()
				
				/**
				 * STEP 2 -> Pane panel
				 */				
				(chartViewModelData.startAnglePane=="" || chartViewModelData.startAnglePane==null || chartViewModelData.startAnglePane==undefined) ? 
						errorMsg += "- " + "<b>Start angle</b> not specified [Step 2 -> Pane panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				
				(chartViewModelData.endAnglePane=="" || chartViewModelData.endAnglePane==null || chartViewModelData.endAnglePane==undefined) ?
					errorMsg += "- " + "<b>End angle</b> not specified [Step 2 -> Pane panel]" + '<br>' : errorMsg;	// TODO: Make LN()
			}		
			
			else if(chartType == "PARALLEL") {
				/**
				 * STEP 2 -> Limit panel
				 */
				(chartViewModelData.serieFilterColumn=="" || chartViewModelData.serieFilterColumn==null || chartViewModelData.serieFilterColumn==undefined) ?
						errorMsg += "- " + "<b>Serie as filter column</b> not specified [Step 2 -> Limit panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.maxNumberOfLines=="" || chartViewModelData.maxNumberOfLines==null || chartViewModelData.maxNumberOfLines==undefined) ?
						errorMsg += "- " + "<b>Maximum number of records</b> not specified [Step 2 -> Limit panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.orderTopMinBottomMax=="" || chartViewModelData.orderTopMinBottomMax==null || chartViewModelData.orderTopMinBottomMax==undefined) ?
						errorMsg += "- " + "<b>Order</b> not specified [Step 2 -> Limit panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				/**
				 * STEP 2 -> Axes lines panel
				 */
				(chartViewModelData.axisColor=="" || chartViewModelData.axisColor==null || chartViewModelData.axisColor==undefined) ?
						errorMsg += "- " + "<b>Axis color</b> not specified [Step 2 -> Axes lines panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.axisColNamePadd=="" || chartViewModelData.axisColNamePadd==null || chartViewModelData.axisColNamePadd==undefined) ?
						errorMsg += "- " + "<b>Axis column name padding</b> not specified [Step 2 -> Axes lines panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.brushColor=="" || chartViewModelData.brushColor==null || chartViewModelData.brushColor==undefined) ?
						errorMsg += "- " + "<b>Brush color</b> not specified [Step 2 -> Axes lines panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.brushWidth=="" || chartViewModelData.brushWidth==null || chartViewModelData.brushWidth==undefined) ?
						errorMsg += "- " + "<b>Brush width</b> not specified [Step 2 -> Axes lines panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				/**
				 * STEP 2 -> Tooltip panel
				 */
				(chartViewModelData.parallelTooltipFontFamily=="" || chartViewModelData.parallelTooltipFontFamily==null || chartViewModelData.parallelTooltipFontFamily==undefined) ?
						errorMsg += "- " + "<b>Font</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipFontSize=="" || chartViewModelData.parallelTooltipFontSize==null || chartViewModelData.parallelTooltipFontSize==undefined) ?
						errorMsg += "- " + "<b>Font size</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipMinWidth=="" || chartViewModelData.parallelTooltipMinWidth==null || chartViewModelData.parallelTooltipMinWidth==undefined) ?
						errorMsg += "- " + "<b>Minimum width</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipMaxWidth=="" || chartViewModelData.parallelTooltipMaxWidth==null || chartViewModelData.parallelTooltipMaxWidth==undefined) ?
						errorMsg += "- " + "<b>Maximim width</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipMinHeight=="" || chartViewModelData.parallelTooltipMinHeight==null || chartViewModelData.parallelTooltipMinHeight==undefined) ?
						errorMsg += "- " + "<b>Minimum height</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipMaxHeight=="" || chartViewModelData.parallelTooltipMaxHeight==null || chartViewModelData.parallelTooltipMaxHeight==undefined) ?
						errorMsg += "- " + "<b>Maximim height</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipPadding=="" || chartViewModelData.parallelTooltipPadding==null || chartViewModelData.parallelTooltipPadding==undefined) ?
						errorMsg += "- " + "<b>Text padding</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipBorder=="" || chartViewModelData.parallelTooltipBorder==null || chartViewModelData.parallelTooltipBorder==undefined) ?
						errorMsg += "- " + "<b>Border</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelTooltipBorderRadius=="" || chartViewModelData.parallelTooltipBorderRadius==null || chartViewModelData.parallelTooltipBorderRadius==undefined) ?
						errorMsg += "- " + "<b>Border radius</b> not specified [Step 2 -> Tooltip panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				/**
				 * STEP 2 -> Legend panel (Title and Element button)
				 */
				(chartViewModelData.parallelLegendTitleFontFamily=="" || chartViewModelData.parallelLegendTitleFontFamily==null || chartViewModelData.parallelLegendTitleFontFamily==undefined) ?
						errorMsg += "- " + "<b>Font</b> not specified [Step 2 -> Legend panel -> Title button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelLegendTitleFontSize=="" || chartViewModelData.parallelLegendTitleFontSize==null || chartViewModelData.parallelLegendTitleFontSize==undefined) ?
						errorMsg += "- " + "<b>Font size</b> not specified [Step 2 -> Legend panel -> Title button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelLegendTitleFontWeight=="" || chartViewModelData.parallelLegendTitleFontWeight==null || chartViewModelData.parallelLegendTitleFontWeight==undefined) ?
						errorMsg += "- " + "<b>Font style</b> not specified [Step 2 -> Legend panel -> Title button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelLegendElementFontFamily=="" || chartViewModelData.parallelLegendElementFontFamily==null || chartViewModelData.parallelLegendElementFontFamily==undefined) ?
						errorMsg += "- " + "<b>Font</b> not specified [Step 2 -> Legend panel -> Title button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelLegendElementFontSize=="" || chartViewModelData.parallelLegendElementFontSize==null || chartViewModelData.parallelLegendElementFontSize==undefined) ?
						errorMsg += "- " + "<b>Font size</b> not specified [Step 2 -> Legend panel -> Title button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.parallelLegendElementFontWeight=="" || chartViewModelData.parallelLegendElementFontWeight==null || chartViewModelData.parallelLegendElementFontWeight==undefined) ?
						errorMsg += "- " + "<b>Font style</b> not specified [Step 2 -> Legend panel -> Title button]" + '<br>' : errorMsg;	// TODO: Make LN()
				
				/**
				 * STEP 2 -> Palette panel
				 */
//				var itemsIncolorPalette = Ext.getCmp("chartColorPallete").paletteGrid.getStore().data.length;
//				
//				(itemsIncolorPalette < 2) ? 
//						errorMsg += "- " + "Color palette needs at least 2 colors [Step 2 -> Palette panel]" + '<br>' : errorMsg;	// TODO: Make LN()
			}
			
			else if (chartType == "SUNBURST") {
				/**
				 * STEP 2 -> Toolbar and tip configuration (Toolbar style button)
				 */
				(chartViewModelData.toolbarPosition=="" || chartViewModelData.toolbarPosition==null || chartViewModelData.toolbarPosition==undefined) ?
						errorMsg += "- " + "<b>Position</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarSpacing=="" || chartViewModelData.toolbarSpacing==null || chartViewModelData.toolbarSpacing==undefined) ?
						errorMsg += "- " + "<b>Spacing</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarTail=="" || chartViewModelData.toolbarTail==null || chartViewModelData.toolbarTail==undefined) ?
						errorMsg += "- " + "<b>Tail</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarHeight=="" || chartViewModelData.toolbarHeight==null || chartViewModelData.toolbarHeight==undefined) ?
						errorMsg += "- " + "<b>Height</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarWidth=="" || chartViewModelData.toolbarWidth==null || chartViewModelData.toolbarWidth==undefined) ?
						errorMsg += "- " + "<b>Width</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarPercFontColor=="" || chartViewModelData.toolbarPercFontColor==null || chartViewModelData.toolbarPercFontColor==undefined) ?
						errorMsg += "- " + "<b>Percentage color</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarFontFamily=="" || chartViewModelData.toolbarFontFamily==null || chartViewModelData.toolbarFontFamily==undefined) ?
						errorMsg += "- " + "<b>Font</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarFontWeight=="" || chartViewModelData.toolbarFontWeight==null || chartViewModelData.toolbarFontWeight==undefined) ?
						errorMsg += "- " + "<b>Font style</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.toolbarFontSize=="" || chartViewModelData.toolbarFontSize==null || chartViewModelData.toolbarFontSize==undefined) ?
						errorMsg += "- " + "<b>Font size</b> not specified [Step 2 -> Toolbar and tip panel -> Toolbar button]" + '<br>' : errorMsg;	// TODO: Make LN()
				/**
				 * STEP 2 -> Toolbar and tip configuration (Tip style button)
				 */
				(chartViewModelData.tipFontWeight=="" || chartViewModelData.tipFontWeight==null || chartViewModelData.tipFontWeight==undefined) ?
						errorMsg += "- " + "<b>Font style</b> not specified [Step 2 -> Toolbar and tip panel -> Tip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipColor=="" || chartViewModelData.tipColor==null || chartViewModelData.tipColor==undefined) ?
						errorMsg += "- " + "<b>Font color</b> not specified [Step 2 -> Toolbar and tip panel -> Tip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipFontSize=="" || chartViewModelData.tipFontSize==null || chartViewModelData.tipFontSize==undefined) ?
						errorMsg += "- " + "<b>Font size</b> not specified [Step 2 -> Toolbar and tip panel -> Tip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipFontFamily=="" || chartViewModelData.tipFontFamily==null || chartViewModelData.tipFontFamily==undefined) ?
						errorMsg += "- " + "<b>Font</b> not specified [Step 2 -> Toolbar and tip panel -> Tip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipWidth=="" || chartViewModelData.tipWidth==null || chartViewModelData.tipWidth==undefined) ?
						errorMsg += "- " + "<b>Width</b> not specified [Step 2 -> Toolbar and tip panel -> Tip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipText=="" || chartViewModelData.tipText==null || chartViewModelData.tipText==undefined) ?
						errorMsg += "- " + "<b>Text</b> not specified [Step 2 -> Toolbar and tip panel -> Tip button]" + '<br>' : errorMsg;	// TODO: Make LN()				
			}
			
			else if (chartType == "WORDCLOUD") {
				(chartViewModelData.sizeCriteria=="" || chartViewModelData.sizeCriteria==null || chartViewModelData.sizeCriteria==undefined) ?
						errorMsg += "- " + "<b>Size criteria</b> not specified [Step 2 -> Wordcloud panel]" + '<br>' : errorMsg;	// TODO: Make LN()				
				(chartViewModelData.maxWords=="" || chartViewModelData.maxWords==null || chartViewModelData.maxWords==undefined) ?
						errorMsg += "- " + "<b>Maximum number of words</b> not specified [Step 2 -> Wordcloud panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.maxAngle=="" || chartViewModelData.maxAngle==null || chartViewModelData.maxAngle==undefined) ?
						errorMsg += "- " + "<b>Maximum word angle</b> not specified [Step 2 -> Wordcloud panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.minAngle=="" || chartViewModelData.minAngle==null || chartViewModelData.minAngle==undefined) ?
						errorMsg += "- " + "<b>Minimum word angle</b> not specified [Step 2 -> Wordcloud panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.maxFontSize=="" || chartViewModelData.maxFontSize==null || chartViewModelData.maxFontSize==undefined) ?
						errorMsg += "- " + "<b>Maximum font size</b> not specified [Step 2 -> Wordcloud panel]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.wordPadding=="" || chartViewModelData.wordPadding==null || chartViewModelData.wordPadding==undefined) ?
						errorMsg += "- " + "<b>Word padding</b> not specified [Step 2 -> Wordcloud panel]" + '<br>' : errorMsg;	// TODO: Make LN()
			}
			
			else if (chartType == "HEATMAP") {	
				
				(chartViewModelData.legendAlign=="" || chartViewModelData.legendAlign==null || chartViewModelData.legendAlign==undefined) ?
						errorMsg += "- " + "<b>Alignment</b> not specified [Step 2 -> Legend and tooltip panel -> Legend button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.symbolWidth=="" || chartViewModelData.symbolWidth==null || chartViewModelData.symbolWidth==undefined) ?
						errorMsg += "- " + "<b>Symbol width</b> not specified [Step 2 -> Legend and tooltip panel -> Legend button]" + '<br>' : errorMsg;	// TODO: Make LN()
				
				(chartViewModelData.tipFontFamily=="" || chartViewModelData.tipFontFamily==null || chartViewModelData.tipFontFamily==undefined) ?
						errorMsg += "- " + "<b>Font</b> not specified [Step 2 -> Legend and tooltip panel -> Tooltip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipFontSize=="" || chartViewModelData.tipFontSize==null || chartViewModelData.tipFontSize==undefined) ?
						errorMsg += "- " + "<b>Font size</b> not specified [Step 2 -> Legend and tooltip panel -> Tooltip button]" + '<br>' : errorMsg;	// TODO: Make LN()
				(chartViewModelData.tipColor=="" || chartViewModelData.tipColor==null || chartViewModelData.tipColor==undefined) ?
						errorMsg += "- " + "<b>Color</b> not specified [Step 2 -> Legend and tooltip panel -> Tooltip button]" + '<br>' : errorMsg;	// TODO: Make LN()
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
		 * Called inside the ChartTypeSelector. Removes everything from the X-axis panel 
		 * if we move from BAR or LINE to SCATTER or RADAR, because for the last to we 
		 * can have ONLY ONE CATEGORY, while we can have more than one for the first pair 
		 * (BAR/LINE).
		 * (danilo.ristovski@mht.net)
		 */ 
		cleanCategoriesAxis: function() {
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
		
		/*ADDING SOME TEXT FOR SVN MERGING DEMO*/
		
    }
});