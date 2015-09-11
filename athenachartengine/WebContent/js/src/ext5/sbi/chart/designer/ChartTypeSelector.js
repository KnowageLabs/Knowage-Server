Ext.define('Sbi.chart.designer.ChartTypeSelector', {
	extend: 'Ext.grid.Panel',

	margin: '0 15 15 0',
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
			
			console.log(Sbi.chart.designer.ChartColumnsContainerManager.storePool);
			console.log(Sbi.chart.designer.ChartColumnsContainerManager.instanceCounter);
			console.log(Sbi.chart.designer.ChartColumnsContainerManager.instanceIdFeed);
			console.log(Sbi.chart.designer.ChartColumnsContainerManager.allAxisData);
			console.log("*** E ***");
			/**
			 * The chart type that is actually picked (clicked).
			 * (comment by: danilo.ristovski@mht.net)
			 */
			var selectedType = record.get('type');			
			
			/**
			 * The store that contains all chart types that are provided (offered) by the Designer
			 * and that we can pick (select) from the chart type selector container on the left.
			 * (comment by: danilo.ristovski@mht.net)
			 */
			var store = this.store;
			
			var selectionModel = this.getSelectionModel();
			
			/**
			 * Scope of the chart type selector.
			 * (comment by: danilo.ristovski@mht.net)
			 */
			var chartTypeSelectorScope = this;
			
			/**
			 * The chart type that has been already chosen (defined) - the one we had just before
			 * we picked a new one from the chart type selector.
			 * (comment by: danilo.ristovski@mht.net)
			 */
			var thisChartType = this.chartType.toLowerCase();
			
			/**
			 * Lookup for checking the compatibility of the chart types when we are determining
			 * should all the data that exists in the current chart within the X and Y panels
			 * be removed (cleared). 
			 * (danilo.ristovski@mht.net)
			 */
			var compatibilityAddDataLookup = 
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
			};			
					
			/**
			 * If newly clicked (selected) chart type ('selectedType') in ChartTypeSelector is 
			 * of the same type as the chart that we have in Designer (the one that has already
			 * been chosen or defined by the loading of the already existing chart document, 
			 * 'thisChartType'), do not take any action. 
			 * (danilo.ristovski@mht.net)
			 */
			if (selectedType.toLowerCase() != thisChartType)
			{						
				/**
				 * With this foreach-loop check if the previous and the newly chosen chart type 
				 * are compatible (in a manner of their quantity and quality criteria for the
				 * serie and category items). If not compatible, 'compatibleTypes' variable is
				 * going to be 'false'.
				 * (danilo.ristovski@mht.net)
				 */
				var compatibleTypes = false;
				
				for(i in compatibilityAddDataLookup[selectedType.toLowerCase()]) 
				{
					var compatibleChart = compatibilityAddDataLookup[selectedType.toLowerCase()][i];
					compatibleTypes = compatibleTypes || compatibleChart == thisChartType;
				}
				
				var plusLeftAxis = Ext.getCmp("plusLeftAxis_"+Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id);
					
					
				if(!compatibleTypes) 
				{	
					
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
						
						fn : function(buttonValue, inputText, showConfig)
						{
							if (buttonValue == 'ok') 
							{
								Sbi.chart.designer.Designer.cleanAxesSeriesAndCategories();
							

								
								/////////////////////////////////////
								var chartColumnsContainer = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
								var numberOfYAxis = chartColumnsContainer.length;
								
								console.log(numberOfYAxis);
								console.log(chartColumnsContainer);
								console.log(Ext.getCmp("chartLeftAxisesContainer").items.length);
								console.log(Ext.getCmp("chartLeftAxisesContainer").items.items[0]);
								
//								console.log("-- Y axis pool: --");
//								console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool);
								console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id);
								console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[1]);
								
								/* ---------- BOTTOM (X) AXIS PANEL ---------- */
								// Show the gear tool on the toolbar of the bottom (X) axis panel
								var stylePopupBottomPanel = Ext.getCmp("stylePopupBottomPanel");
								stylePopupBottomPanel.show();
								
								
								// Show the textfield dedicated for the title of the bottom (X) axis
								var textfieldAxisTitle = Ext.getCmp("textfieldAxisTitle");
								textfieldAxisTitle.show();
//								console.log(chartColumnsContainer);
								/* ---------- LEFT (Y) AXIS PANEL ---------- */
								// Show the gear tool on the toolbar of the left (Y) axis panel
								var stylePopupLeftAxis = Ext.getCmp("stylePopupLeftAxis_"+Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id);
								console.log( Ext.getCmp("stylePopupLeftAxis_Axis_3"));
								stylePopupLeftAxis.show();
								
								// Show the plus tool on the toolbar of the left (Y) axis panel			
								plusLeftAxis.show();
//								console.log(Ext.getCmp("plusLeftAxis_"+Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id));
								
								// Show the textfield dedicated for the title of the left (Y) axis
								var titleTextfield = Ext.getCmp(chartColumnsContainer[0].id + "_TitleTextfield");
								titleTextfield.show();
								
								// Show the serie&tooltip icon for SERIE records inside the left (Y) panel
								var actionColumnLeftAxis = Ext.getCmp("actionColumnLeftAxis_"+Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id);
								actionColumnLeftAxis.items[0].iconCls = "";	
								
								
								//////////////////////////////////
								
								
								//chartTypeSelectorScope.fireEvent("cleanJson", selectedType);
								
								//Select the new chart type
								chartTypeSelectorScope.setChartType(selectedType);		
																
								/** 
								 * Hide axis title textbox and gear tool for both left (Y)
								 * axis panel and bottom (X) axis panel and plus tool of the left
								 * (Y) panel when new row is clicked. Hide also serie&tooltip icon 
								 * for SERIE records inside the left (Y) panel.
								 * (danilo.ristovski@mht.net)
								 */
								if (selectedType.toLowerCase()=="sunburst" || selectedType.toLowerCase()=="wordcloud" || 
										selectedType.toLowerCase()=="treemap" || selectedType.toLowerCase()=="parallel" ||
											selectedType.toLowerCase()=="heatmap" || selectedType.toLowerCase()=="chord") {
									
									var chartColumnsContainerNew = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
									var numberOfYAxis = chartColumnsContainerNew.length;
									
									console.log(numberOfYAxis);
									console.log(chartColumnsContainerNew);
									
//									if (numberOfYAxis > 1) {
//										for (var i=0; i<numberOfYAxis; i++) {
//											chartColumnsContainerNew[i+1].close();
//										}
//									} else {									
//										console.log(Ext.getCmp("chartLeftAxisesContainer").items.items[0].header.items.items[1]);
//										Ext.getCmp("chartLeftAxisesContainer").items.items[0].header.items.items[1].hide();
//									}
									
									console.log(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool);
									
									/* ---------- BOTTOM (X) AXIS PANEL ---------- */
									// Hide the gear tool on the toolbar of the bottom (X) axis panel							
									if (selectedType.toLowerCase()!="heatmap") {
//										Ext.getCmp("stylePopupBottomPanel").hide();
										stylePopupBottomPanel.hide();
									}										
									
									// Hide the textfield dedicated for the title of the bottom (X) axis
//									Ext.getCmp("textfieldAxisTitle").hide();
									textfieldAxisTitle.hide();
									
									/* ---------- LEFT (Y) AXIS PANEL ---------- */									
									if (selectedType.toLowerCase()!="heatmap") {
										// Hide the gear tool on the toolbar of the left (Y) axis panel
//										console.log(Ext.getCmp("plusLeftAxis_"+Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id));
										stylePopupLeftAxis.hide();
//										Ext.getCmp("stylePopupLeftAxis_" + Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id).hide();										
									}											
									
									// Hide the plus tool on the toolbar of the left (Y) axis panel
//									Ext.getCmp("plusLeftAxis_" + Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id).hide();
									plusLeftAxis.hide();
									
									// Hide the textfield dedicated for the title of the left (Y) axis
//									Ext.getCmp(Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id + "_TitleTextfield").hide();
									titleTextfield.hide();
									
									// Hide the serie&tooltip icon for SERIE records inside the left (Y) panel
//									Ext.getCmp("actionColumnLeftAxis_" + Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id).items[0].iconCls = "x-hidden";
									actionColumnLeftAxis.items[0].iconCls = "x-hidden";
									
									
								} else if (selectedType.toLowerCase()=="radar" || selectedType.toLowerCase()=="scatter") {
									/**
									 * If we change chart type from one that is not compatible with the RADAR or SCATTER
									 * to the one of those two, hide plus buttons because we cannot have more than one
									 * Y-axis panel for these types.
									 */
									// Hide the plus tool on the toolbar of the left (Y) axis panel
									plusLeftAxis.hide();
//									Ext.getCmp("plusLeftAxis_" + Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id).hide();
									
								}
								/* END */
								
								chartTypeSelectorScope.fireEvent("newrowclick");
								
							}  else if (buttonValue == 'cancel') {
								
								for(var i = 0; i < store.data.length; i++) {
									var row = store.getAt(i);
									
									if(thisChartType === row.get('type').toLowerCase()) {
										selectionModel.select(i);
										break;
									}
								}
							}
						}
					});
				} else {
					
					/**
					 * If we come to RADAR chart from some chart type that is compatible with it 
					 * (e.g. BAR and LINE), keep the data, but remove all other Y-axis panels that
					 * were potentially defined earlier for those compatible chart types and hide
					 * the plus tool placed on the left Y-axis panels header. For RADAR chart we
					 * can have only one Y-axis.
					 */
					if (selectedType.toLowerCase() == "radar" || selectedType.toLowerCase() == "scatter") {
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
						
						// Hide the plus tool on the toolbar of the left (Y) axis panel
						plusLeftAxis.hide();
//						Ext.getCmp("plusLeftAxis_"+Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[0].id).hide();
						
						/**
						 * We need confirmation from user for removing all the items (categories) from the
						 * bottom X-axis panel when moving from the BAR/LINE to RADAR/SCATTER chart type.
						 * Removing those items inside the X-axis panel is necessary because we can have
						 * multiple categories for BAR/LINE chart type, whilst we can have ONLY ONE CATEGORY
						 * for RADAR/SCATTER chart type.
						 */					
						if ((thisChartType == "bar" || thisChartType == "line") && 
								Ext.getCmp("chartBottomCategoriesContainer").store.data.length > 1) {
							
							Ext.Msg.show ({
								title : '',
								message : LN("sbi.chartengine.designer.charttype.changetypeCategories"), 
								icon : Ext.Msg.QUESTION,
								closable : false,
								buttons : Ext.Msg.OKCANCEL,
								
								buttonText : {
									ok : LN('sbi.chartengine.generic.ok'),
									cancel : LN('sbi.generic.cancel')
								},
							
								fn : function(buttonValue, inputText, showConfig) {
									if (buttonValue == 'ok') {										
										/** 
										 * Set active type chart as the one that we chosen now (in other words, set 
										 * the chart type as 'radar'. 
										 */ 
										chartTypeSelectorScope.setChartType(selectedType);
										
										/**
										 * Clean the X-axis bottom panel for RADAR and SCATTER chart types
										 */
										Sbi.chart.designer.Designer.cleanCategoriesAxis();										
									} else if (buttonValue == 'cancel') {
										for(var i = 0; i < store.data.length; i++) {
											var row = store.getAt(i);
											
											if(thisChartType === row.get('type').toLowerCase()) {
												selectionModel.select(i);
												break;
											}
										}
									}
								}	
							});
						} else {
							/** 
							 * Set active type chart as the one that we chosen now (in other words, set 
							 * the chart type as 'radar'. 
							 */ 
							chartTypeSelectorScope.setChartType(selectedType);
						}
						
						chartTypeSelectorScope.fireEvent("newrowclick");
						
					} else {
						chartTypeSelectorScope.setChartType(selectedType);
						chartTypeSelectorScope.fireEvent("newrowclick");
					}
				}	
				
				/**
				 * When we change the chart type, reset the 'Series type' parameter on the all Y-axises
				 * that exist on the page.
				 * (added: 23th July 2015)
				 * (danilo.ristovski@mht.net)
				 */
//				var yAxisPool = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
//				console.log("PPP");
//				for (var i=0; i<yAxisPool.length; i++) {
//					var leftYAxis = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[i];
//					var leftYAxisStoreData = leftYAxis.store.data;
//					
//					if(leftYAxisStoreData.length > 0) {
//						for (var j=0; j<leftYAxisStoreData.length; j++) {
//							leftYAxisStoreData.items[j].data.serieType = undefined;
//						}
//					}	
//				}							
			}
			
		}
	},
	
	setChartType: function(type) {
		
		this.chartType = type;
		console.log("*** F ***");
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
			if(chartOrientationCombo != undefined && !chartOrientationCombo.isDisabled()) {
				chartOrientationCombo.disable();
			}
			if(chartRightAxisesContainer != undefined && !chartRightAxisesContainer.isDisabled()) {
				chartRightAxisesContainer.disable();
			}
		} else {
			if(chartOrientationCombo != undefined && chartOrientationCombo.isDisabled()) {
				chartOrientationCombo.enable();
			}
			if(chartRightAxisesContainer != undefined && chartRightAxisesContainer.isDisabled()) {
				chartRightAxisesContainer.enable();
			}
		}
	},

	getChartType: function() {
		return this.chartType;
	}
});