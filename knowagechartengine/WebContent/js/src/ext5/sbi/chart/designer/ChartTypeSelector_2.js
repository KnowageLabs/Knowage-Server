/**
 * The new color picker in the form of combo box.
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */

var chartTypesStore = Ext.create 
( 
	"Ext.data.Store", 
	
	{
		fields: ["style", "styleAbbr"],
				
		data: 
		[
		 	{
		 		style: LN("sbi.chartengine.designer.charttype.bar"), 
		 		styleAbbr: "bar",
		 		icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/barchart/img/barchart_icon_new_1.png'
 			},	
		 	
 			{
 				style: LN("sbi.chartengine.designer.charttype.line"), 
 				styleAbbr: "line",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/linechart/img/linechart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.pie"), 
				styleAbbr: "pie",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_icon_new_1.png',
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.radar"), 
				styleAbbr: "radar",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/radarchart/img/radarchart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.scatter"), 
				styleAbbr: "scatter",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/scatterchart/img/scatterchart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.treemap"), 
				styleAbbr: "treemap",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/treemapchart/img/treemapchart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.heatmap"), 
				styleAbbr: "heatmap",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/heatmapchart/img/heatmapchart_icon_new_1.png'
			},	
			
			{
				style: LN("sbi.chartengine.designer.charttype.gauge"), 
				styleAbbr: "gauge",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/gaugechart/img/gaugechart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.wordcloud"), 
				styleAbbr: "wordcloud",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/wordcloudchart/img/wordcloudchart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.parallel"),
				styleAbbr: "parallel",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/parallelchart/img/parallelchart_icon_new_1.png'
			},	
		 	
 			{
				style: LN("sbi.chartengine.designer.charttype.sunburst"),
				styleAbbr: "sunburst",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/sunburstchart/img/sunburstchart_icon_new_1.png'
			},
			
			{
				style: LN("sbi.chartengine.designer.charttype.chord"),
				styleAbbr: "chord",
				icon: Sbi.chart.designer.Designer.relativePathReturn + '/js/src/ext4/sbi/cockpit/widgets/extjs/chordchart/img/chordchart_icon_new_1.png'
			}
		]
	}
);

Ext.define
(
	'Sbi.chart.designer.ChartTypeSelector_2', 
	
	{
		extend: "Ext.form.ComboBox",
		id: "chartTypeCombobox",
		margin: '0 15 15 0',
		store: chartTypesStore,
		queryMode: 'local',
	    displayField: 'style',
	    valueField: 'styleAbbr',
	    //value: "bar",
	    editable: false,
	    padding: "5 0 0 0",
	    width: 200,
	    height: 40,
	    
	    /**
	     * Show icon that represents the chart type along with it's name.
	     */
	    tpl: [
	          	'<tpl for=".">',
	          		'<div class="x-boundlist-item">',
          				'<img src="{icon}" width="30px" style="display:inline-block; vertical-align:middle; line-height:20px;"/>',
          				'&nbsp;&nbsp;&nbsp;',
          				'<p style="display:inline-block; vertical-align:middle; line-height:20px;">{style}</p>',
	          		'</div>',
	          	'</tpl>'
	          ],
	   	    
	    statics:
    	{
	    	/**
			 * Gives us information if the Designer is completely loaded (all necessary data are available). 
			 * If it is, we can access its data (e.g. yAxisPool) and we will not have an error in the code.
			 */
	    	dataLoaded: false,
	    	chartType: null,
	    	
	    	/**
			 * Lookup for checking the compatibility of the chart types when we are determining
			 * should all the data that exists in the current chart within the X and Y panels
			 * be removed (cleared). 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			compatibilityAddDataLookup:
			{
				bar: 		['line','radar','scatter'],
				line: 		['bar','radar','scatter'],
				pie: 		[],
				sunburst: 	[],
				wordcloud: 	[],
				treemap: 	[],
				parallel: 	[],
				radar: 		['bar','line','scatter'],
				scatter:	['bar','line','radar'],
				heatmap:	[],
				chord: 		[],
				gauge:		[]
			}
    	},
	    
	    getChartType: function() {
			return this.getValue().toUpperCase();
		},
		
		setChartType: function(newChartType) {
			this.setValue(newChartType.toLowerCase());
		},
		
		getChartTypesIcons: function()
		{			
			var arrayIcons = {};
			
			for (i=0; i<chartTypesStore.data.length; i++)
			{
				arrayIcons[chartTypesStore.data.items[i].data.styleAbbr] = chartTypesStore.data.items[i].data.icon;
			}
			
			return arrayIcons;
		},
		
		resetStep1: function()
		{			
			/* ------------------------------------------- */
			/* ---------- BOTTOM (X) AXIS PANEL ---------- */
			/* ------------------------------------------- */
			
			// Show the gear tool on the toolbar of the bottom (X) axis panel
			this.stylePopupBottomPanel = Ext.getCmp("stylePopupBottomPanel");
			
			// Show the textfield dedicated for the title of the bottom (X) axis
			this.textfieldAxisTitle = Ext.getCmp("textfieldAxisTitle");
			
			/* ----------------------------------------- */
			/* ---------- LEFT (Y) AXIS PANEL ---------- */
			/* ----------------------------------------- */
			
			var leftContainerId = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id;
			
			// Show the gear tool on the toolbar of the left (Y) axis panel
			this.stylePopupLeftAxis = Ext.getCmp("stylePopupLeftAxis_" + leftContainerId);
			
			// Show the textfield dedicated for the title of the left (Y) axis
			this.titleTextfield = Ext.getCmp(leftContainerId + "_TitleTextfield");
			
			// Show the plus tool on the toolbar of the left (Y) axis panel		
			this.plusLeftAxis = Ext.getCmp("plusLeftAxis_" + leftContainerId);
			
			// Show the serie&tooltip icon for SERIE records inside the left (Y) panel
			this.actionColumnLeftAxis = Ext.getCmp("actionColumnLeftAxis_" + leftContainerId);
				
			this.stylePopupBottomPanel.show();		
			this.textfieldAxisTitle.show();			
			this.stylePopupLeftAxis.show();						
			this.plusLeftAxis.show();				
			this.titleTextfield.show();	
			
			//console.log(leftContainerId);
			
			this.actionColumnLeftAxis.items[0].iconCls = "";
		},
		
		/**
		 * @param dataJustLoaded - If the data for the chart is just loaded
		 */
		customizeStep1AndStep2: function(newlySelectedType,previousChartType)
		{			
			var globalScope = this;	
						
			var iconsPath = this.getChartTypesIcons();
			
			/**
			 * Remove the icon in the chart type combo box when changing between two types
			 * since there is an answer that user should provide and on which it will be
			 * determined if the current chart type should be changed.
			 */
			this.inputEl.setStyle
            (
        		{
	                "height": "35px",
        			'background-image': 	'url('+iconsPath[previousChartType.toLowerCase()]+')',
	                'background-repeat': 	'no-repeat',
	                'background-position': 	'left 2px center',
	                'padding-left': 		'40px', 
	                'background-size': 		"30px 30px",
	                'display': 'none'
        		}
    		);							
							
			/**
			 * Lookup for checking the compatibility of the chart types when we are determining
			 * should all the data that exists in the current chart within the X and Y panels
			 * be removed (cleared). 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			var compatibilityAddDataLookup = Sbi.chart.designer.ChartTypeSelector_2.compatibilityAddDataLookup;
			
			/**
			 * With this foreach-loop check if the previous and the newly chosen chart type 
			 * are compatible (in a manner of their quantity and quality criteria for the
			 * serie and category items). If not compatible, 'compatibleTypes' variable is
			 * going to be 'false'.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			var compatibleTypes = false;
			
			for(i in compatibilityAddDataLookup[newlySelectedType]) 
			{
				var compatibleChart = compatibilityAddDataLookup[newlySelectedType][i];
				compatibleTypes = compatibleTypes || compatibleChart == previousChartType;
			}
			
			/**
			 * If previous and current chart types are not compatible.
			 */
			if(!compatibleTypes) 
			{							
//				Ext.Msg.show
//				(
//					{
//						title : '',
//						message : LN('sbi.chartengine.designer.charttype.changetype.lossOfSeriesAndCategories'),
//						icon : Ext.Msg.QUESTION,
//						closable : false,
//						buttons : Ext.Msg.OKCANCEL,
//					
//						buttonText : 
//						{
//							ok : LN('sbi.chartengine.generic.ok'),
//							cancel : LN('sbi.generic.cancel')
//						},
//					
//						fn : function(buttonValue, inputText, showConfig)
//						{							
//							if (buttonValue == 'ok') 
//							{								
								Sbi.chart.designer.ChartTypeSelector_2.chartType = newlySelectedType.toLowerCase();
								//console.log("PPP");
								//console.log(Sbi.chart.designer.Designer.exportAsJson());
								
								/**
								 * Cleaning of axis panels since previous and current chart types are not compatible.
								 */
								Sbi.chart.designer.Designer.cleanAxesSeriesAndCategories();
								
								//console.log(Sbi.chart.designer.Designer.exportAsJson());
								
								/* TODO: Commented because we are already firing this event when handling the 
								 * "resetStep2" in Designer with input param. that is missing here */
								//globalScope.fireEvent("chartTypeChanged");	
								
								/**
								 * Since we approved changing of the chart type, we need to reset the GUI elements on
								 * Step 1 and Step 2 of the Designer. 
								 */
								globalScope.resetStep1();
								//console.log(Sbi.chart.designer.Designer.exportAsJson());
								/**
								 * Inform the Designer that it should take care of GUI elements on the Step 2 of the
								 * Designer. It should hide excess GUI elements on the Step 2 and show those necessary
								 * for the current chart type.
								 */
								globalScope.fireEvent("resetStep2");								
								
								/** 
								 * Hide axis title textbox and gear tool for both left (Y)
								 * axis panel and bottom (X) axis panel and plus tool of the left
								 * (Y) panel when new row is clicked. Hide also serie&tooltip icon 
								 * for SERIE records inside the left (Y) panel. Hidding of the serie 
								 * style configuration popup icon (serie&tooltip) will be excluded in
								 * the case of PIE chart.
								 * @author: danristo (danilo.ristovski@mht.net)
								 */
								if (newlySelectedType.toLowerCase()=="sunburst" || newlySelectedType.toLowerCase()=="wordcloud" || 
										newlySelectedType.toLowerCase()=="treemap" || newlySelectedType.toLowerCase()=="parallel" ||
										newlySelectedType.toLowerCase()=="heatmap" || newlySelectedType.toLowerCase()=="chord" || 
											newlySelectedType.toLowerCase()=="pie") {
									
									var chartColumnsContainerNew = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
									var numberOfYAxis = chartColumnsContainerNew.length;
									
									if (numberOfYAxis > 1) 
									{
										for (var i=0; i<numberOfYAxis; i++) 
										{
											chartColumnsContainerNew[i+1].close();
										}
									} 									
														
									if (newlySelectedType.toLowerCase()!="heatmap") 
									{		
										if (newlySelectedType.toLowerCase()!="chord" && newlySelectedType.toLowerCase()!="parallel")
										{
											/* ---------- BOTTOM (X) AXIS PANEL ---------- */
											// Hide the gear tool on the toolbar of the bottom (X) axis panel		
											globalScope.stylePopupBottomPanel.hide();
											
											// Hide the gear tool on the toolbar of the left (Y) axis panel
											globalScope.stylePopupLeftAxis.hide();	
										}
										
										/* ---------- LEFT (Y) AXIS PANEL ---------- */	
										// Hide the textfield dedicated for the title of the bottom (X) axis
										globalScope.textfieldAxisTitle.hide();
										
										// Hide the textfield dedicated for the title of the left (Y) axis
										globalScope.titleTextfield.hide();									
									}											
									
									// Hide the plus tool on the toolbar of the left (Y) axis panel
									globalScope.plusLeftAxis.hide();
									
									/**
									 * For PIE chart we will need serie style configuration popup
									 * in order to define how the serie items should be displayed.
									 * Foe example: with what color are serie (bars, lines, ...)
									 * items going to be presented, what is the tooltip going to 
									 * look like, etc. This is not common for other chart types in
									 * this if-statement.
									 * @author: danristo (danilo.ristovski@mht.net)
									 */
									if (newlySelectedType.toLowerCase()!="pie" && newlySelectedType.toLowerCase()!="radar")
									{
										globalScope.actionColumnLeftAxis.items[0].iconCls = "x-hidden";
									}
								} 
								
								else if(newlySelectedType.toLowerCase()=="radar" || newlySelectedType.toLowerCase()=="scatter")
								{
									// Hide the plus tool on the toolbar of the left (Y) axis panel
									globalScope.plusLeftAxis.hide();
								}
								//return true;
//							} 									
//							else if (buttonValue == 'cancel') 
//							{
//								
//								Sbi.chart.designer.ChartTypeSelector_2.chartType = previousChartType.toLowerCase();
//								// danristo
//								// Suspend "change" event that will definitely happen when changing the active combo item (previous chart type)
//								globalScope.suspendEvents(false);
//								
//								// Set previous chart type
//								globalScope.setValue(previousChartType);
//																
//								// Resume events
//								globalScope.resumeEvents();	
//								
//								globalScope.fireEvent("cancel");
//							}
//						}
//				});
			}	
			/**
			 * If previous and current chart types are compatible.
			 */
			else 
			{						
//				/**
//				 * If the new chart type is GAUGE clean the X-axis panel, since we do not have categories 
//				 * for this chart type, but keep all series items and all X-axis panels (as many as there
//				 * are specified).
//				 * 
//				 * @author: danristo (danilo.ristovski@mht.net)
//				 */
//				if (newlySelectedType.toLowerCase() == "gauge")
//				{
//					Ext.Msg.show
//					(
//						{
//							title : '',
//							message : LN("sbi.chartengine.designer.charttype.changetype.lossOfCategories"), 
//							icon : Ext.Msg.QUESTION,
//							closable : false,
//							buttons : Ext.Msg.OKCANCEL,
//							
//							buttonText : 
//							{
//								ok : LN('sbi.chartengine.generic.ok'),
//								cancel : LN('sbi.generic.cancel')
//							},
//						
//							fn : function(buttonValue, inputText, showConfig) 
//							{
//								if (buttonValue == 'ok') 
//								{
//									Sbi.chart.designer.Designer.cleanCategoriesAxis();	
//									
//									Sbi.chart.designer.ChartTypeSelector_2.chartType = newlySelectedType.toLowerCase();
//									
//									globalScope.fireEvent("resetStep2");
//								}
//								else
//								{
//									globalScope.suspendEvents(false);
//									
//									// Set previous chart type
//									globalScope.setValue(previousChartType);
//									
//									Sbi.chart.designer.ChartTypeSelector_2.chartType = previousChartType.toLowerCase();
//									
//									// Resume events
//									globalScope.resumeEvents();
//									
//									globalScope.fireEvent("cancel");
//								}
//							}
//						}
//					);
//				}
				
				/**
				 * If we come to RADAR chart from some chart type that is compatible with it 
				 * (e.g. BAR and LINE), keep the data, but remove all other Y-axis panels that
				 * were potentially defined earlier for those compatible chart types and hide
				 * the plus tool placed on the left Y-axis panels header. For RADAR chart we
				 * can have only one Y-axis.
				 * 
				 * @author: danristo (danilo.ristovski@mht.net)
				 */				
				
				if (newlySelectedType.toLowerCase() == "radar" || newlySelectedType.toLowerCase() == "scatter") {								
					
					/**
					 * We need confirmation from user for removing all the items (categories) from the
					 * bottom X-axis panel when moving from the BAR/LINE to RADAR/SCATTER chart type.
					 * Removing those items inside the X-axis panel is necessary because we can have
					 * multiple categories for BAR/LINE chart type, whilst we can have ONLY ONE CATEGORY
					 * for RADAR/SCATTER chart type.
					 * @author: danristo (danilo.ristovski@mht.net)
					 */					
					if ((previousChartType == "bar" || previousChartType == "line") && 
							Ext.getCmp("chartBottomCategoriesContainer").store.data.length > 1) {
						
						Ext.Msg.show ({
							title : '',
							message : LN("sbi.chartengine.designer.charttype.changetype.lossOfCategories"), 
							icon : Ext.Msg.QUESTION,
							closable : false,
							buttons : Ext.Msg.OKCANCEL,
							
							buttonText : {
								ok : LN('sbi.chartengine.generic.ok'),
								cancel : LN('sbi.generic.cancel')
							},
						
							fn : function(buttonValue, inputText, showConfig) {
								if (buttonValue == 'ok') {										
																		
									// Hide the plus tool on the toolbar of the left (Y) axis panel
									(globalScope.plusLeftAxis!=undefined) ? globalScope.plusLeftAxis.hide() : null;
									
									/**
									 * If there are some Y-axis panels created before on the Designer (other 
									 * that the default (the left) one, remove them.
									 */					
									var chartColumnsContainerNew = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
									var numberOfYAxis = chartColumnsContainerNew.length;
									
									if (numberOfYAxis > 1) {						
										for (var i=1; i<numberOfYAxis; i++) {
											Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[1].close();	
										}
									}
									
									Sbi.chart.designer.ChartTypeSelector_2.chartType = newlySelectedType.toLowerCase();
									
									/** 
									 * Set active type chart as the one that we chosen now (in other words, set 
									 * the chart type as 'radar'. 
									 */ 
									globalScope.fireEvent("resetStep2");
									
									/**
									 * Clean the X-axis bottom panel for RADAR and SCATTER chart types
									 */
									Sbi.chart.designer.Designer.cleanCategoriesAxis();	
																		
								} else if (buttonValue == 'cancel') {																
									
									globalScope.suspendEvents(false);
									
									// Set previous chart type
									globalScope.setValue(previousChartType);
									
									Sbi.chart.designer.ChartTypeSelector_2.chartType = previousChartType.toLowerCase();
									
									// Resume events
									globalScope.resumeEvents();
									
									globalScope.fireEvent("cancel");
								}
							}	
						});
					} 
					/**
					 * If previous chart type was not RADAR or SCATTER
					 */
					else 
					{
						globalScope.resetStep1();
						// Hide the plus tool on the toolbar of the left (Y) axis panel
						(globalScope.plusLeftAxis!=undefined) ? globalScope.plusLeftAxis.hide() : null;
						
						/**
						 * If there are some Y-axis panels created before on the Designer (other 
						 * that the default (the left) one, remove them.
						 */					
						var chartColumnsContainerNew = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
						var numberOfYAxis = chartColumnsContainerNew.length;
						
						if (numberOfYAxis > 1) {						
							for (var i=1; i<numberOfYAxis; i++) {
								Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[1].close();	
							}
						}	
						
						Sbi.chart.designer.ChartTypeSelector_2.chartType = newlySelectedType.toLowerCase();
						
						globalScope.fireEvent("resetStep2");
					}
				}
				
				/**
				 * If newly selected chart type is not RADAR, GAUGE nor SCATTER (currently: BAR or LINE)
				 */
				else 
				{
					//console.log("BAR/LINE");
					globalScope.resetStep1();
					Sbi.chart.designer.ChartTypeSelector_2.chartType = newlySelectedType.toLowerCase();
					globalScope.fireEvent("resetStep2");
				}
			}	
		},
				
		listeners:
		{
			cancel: function()
			{
				var chartType = Sbi.chart.designer.ChartTypeSelector_2.chartType;
				
				var iconsPath = this.getChartTypesIcons();				
				
				/**
				 * Set the icon next to the text (the name of the selected
				 * chart type) in the combo box.
				 */
				this.inputEl.setStyle
	            (
            		{
            			'height': "35px",
            			'background-image': 	'url('+iconsPath[chartType.toLowerCase()]+')',
		                'background-repeat': 	'no-repeat',
		                'background-position': 	'left 2px center',
		                'padding-left': 		'40px', 
		                'background-size': 		"30px 30px",
		                'display': 'inline' 
            		}
        		);	
			},
			
			resetStep2: function()
			{
				var chartType = Sbi.chart.designer.ChartTypeSelector_2.chartType;
								
				var iconsPath = this.getChartTypesIcons();				
				
				/**
				 * Set the icon next to the text (the name of the selected
				 * chart type) in the combo box.
				 */
				this.inputEl.setStyle
	            (
            		{
		                'height': "35px",
            			'background-image': 	'url('+iconsPath[chartType.toLowerCase()]+')',
		                'background-repeat': 	'no-repeat',
		                'background-position': 	'left 2px center',
		                'padding-left': 		'40px', 
		                'background-size': 		"30px 30px",
		                'display': 'inline' 
            		}
        		);				
			},
				
			change: function(comboBox,currentOrNewChartType,previousChartType)
			{	
				/**
				 * Scope of the chart type selector.
				 * @commentBy: danristo (danilo.ristovski@mht.net)
				 */
				var globalScope = this;
				
				if (previousChartType!=null)
				{
					/**
					 * Remove the icon in the chart type combo box when changing between two types
					 * since there is an answer that user should provide and on which it will be
					 * determined if the current chart type should be changed.
					 */
					this.inputEl.setStyle
		            (
		        		{
			                "height": "35px",
		        			'background-image': 	'',
			                'background-repeat': 	'no-repeat',
			                'background-position': 	'left 2px center',
			                'padding-left': 		'40px', 
			                'background-size': 		"30px 30px",
			                'display': 'none'
		        		}
		    		);
					
					var compatibilityAddDataLookup = Sbi.chart.designer.ChartTypeSelector_2.compatibilityAddDataLookup;
					
					/**
					 * With this foreach-loop check if the previous and the newly chosen chart type 
					 * are compatible (in a manner of their quantity and quality criteria for the
					 * serie and category items). If not compatible, 'compatibleTypes' variable is
					 * going to be 'false'.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					var compatibleTypes = false;
					
					for(i in compatibilityAddDataLookup[currentOrNewChartType]) 
					{
						var compatibleChart = compatibilityAddDataLookup[currentOrNewChartType][i];
						compatibleTypes = compatibleTypes || compatibleChart == previousChartType;
					}
					
					var messageTxt = (compatibleTypes) ? 
							LN("sbi.chartengine.designer.charttype.changetype.lossOfAxesSerStyleConf") : 
								LN("sbi.chartengine.designer.charttype.changetype.lossOfAxesSerStyleConfAndSeriesCategor");										
					
					/**
					 * Warn user that he is about to lose all axis style ceonfiguration and
					 * serie style configuration customization changes that he potentially
					 * made.
					 */
					Ext.Msg.show
					(
						{
							title: LN("sbi.chartengine.designer.charttype.changetype.lossOfAxesAndSeriesStyleConfigCustomization.title"),
							
							message: messageTxt,
										
							icon: Ext.Msg.QUESTION,
							closable: false,
							buttons: Ext.Msg.OKCANCEL,
							
							buttonText: 
							{
								ok: LN('sbi.chartengine.generic.ok'),
								cancel: LN('sbi.generic.cancel')
							},
							
							fn: function(buttonValue, inputText, showConfig)
							{
								if (buttonValue == 'ok') 
								{									
									/**
									 * If currentOrNewChartType is not null - if user did not click-down on the
									 * chart type and then move mouse on some other chart type and make a click-up
									 * we can proceed with change of the chart type. Otherwise (without this ckeck)
									 * the error will appear.
									 */
									if (currentOrNewChartType!=null)
									{
										/**
										 * When Designer renders for the first time (when opening the chart in it for the first
										 * time) second input parameters of this function (event) will be equal to the actual chart
										 * type (document). The third input parameter will be null. 
										 * 
										 * When we change the chart type inside the Designer the second parameter will contain newly
										 * selected chart type, while the third parameter will contain value of the chart type that 
										 * was previously selected.
										 * 
										 * @commentBy: danristo (danilo.ristovski@mht.net)
										 */				
										
										/**
										 * The chart type that is actually picked (clicked). Newly selected chart type.
										 * @commentBy: danristo (danilo.ristovski@mht.net)
										 */
										var newlySelectedType = currentOrNewChartType.toLowerCase();	
										
//										/**
//										 * If the Designer is loaded for the first time, i.e. document is just opened. This will
//										 * help us to determine if we should call customization function for Designer's Step 1
//										 * and Step 2. If it is just loaded, we do not need customization.
//										 */
//										var designerJustLoaded = false;
//										(previousChartType != null) ? designerJustLoaded = false : designerJustLoaded = true;
										
//										/**
//										 * The chart type that has been already chosen (defined) - the one we had just before
//										 * we picked a new one from the chart type selector.
//										 * @commentBy: danristo (danilo.ristovski@mht.net)
//										 */
//										var previousChartType = (previousChartType != null) ? previousChartType.toLowerCase() : newlySelectedType;
										
										/**
										 * If newly clicked (selected) chart type ('selectedType') in ChartTypeSelector is 
										 * of the same type as the chart that we have in Designer (the one that has already
										 * been chosen or defined by the loading of the already existing chart document, 
										 * 'previousChartType'), do not take any action.
										 *  
										 * @author: danristo (danilo.ristovski@mht.net)
										 */				
										if (newlySelectedType != previousChartType)
										{						
											globalScope.customizeStep1AndStep2(newlySelectedType,previousChartType);										
										}
										/**
										 * Previous and current chart type are the same: (1) the same chart type is chosen twice or
										 * (2) we are just loading the Designer page for the first time
										 */
										else
										{
											globalScope.on
											(
												"axesSet",function() 
												{
													Sbi.chart.designer.ChartTypeSelector_2.dataLoaded = true;	
													
						//							if(Sbi.chart.designer.ChartTypeSelector_2.dataLoaded)
						//							{	
						//								//globalScope.resetStep1();
						//								//globalScope.customizeStep1AndStep2(newlySelectedType,previousChartType);
						//							}
												}
											);					
										}			
									}
								}
								else
								{
									/**
									 * If user does not want to change the current chart type and the axes/serie style configuration
									 * customization made until the moment, we will cancel its change and keep current data.
									 */
									Sbi.chart.designer.ChartTypeSelector_2.chartType = previousChartType.toLowerCase();
									
									globalScope.suspendEvents(false);
									
									// Set previous chart type
									globalScope.setValue(Sbi.chart.designer.ChartTypeSelector_2.chartType);
																		
									// Resume events
									globalScope.resumeEvents();	
									
									globalScope.fireEvent("cancel");
								}
							}
						}
					);
					
				}				
			}
		}
	}
);