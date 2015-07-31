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
			
			var selectedType = record.get('type');			
			
			var store = this.store;
			var selectionModel = this.getSelectionModel();
			
			var chartTypeSelector = this;

			var thisChartType = this.chartType.toLowerCase();
			
			var globalThis = this;		
			
			/**
			 * Lookup for checking the compatibility of the chart types when we are determining
			 * should all the data that exists in the current chart within the X and Y panels
			 * be removed (cleared). (danilo.ristovski@mht.net)
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
				heatmap:	[]
			};			
					
			/**
			 * If newly clicked (selected) chart type in ChartTypeSelector is of the same
			 * type as the chart that we have in Designer, do not take any action. 
			 * (danilo.ristovski@mht.net)
			 */
			if (selectedType.toLowerCase() != thisChartType)
			{			
				var compatibleTypes = false;
				
				// foreach
				for(i in compatibilityAddDataLookup[selectedType.toLowerCase()]) 
				{
					var compatibleChart = compatibilityAddDataLookup[selectedType.toLowerCase()][i];
					compatibleTypes = compatibleTypes || compatibleChart == thisChartType;
				}
				
				var chartColumnsContainer = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
				var numberOfYAxis = chartColumnsContainer.length;
				var leftAxisTitleTextboxId = 0;
				
				/* ---------- BOTTOM (X) AXIS PANEL ---------- */
				// Show the gear tool on the toolbar of the bottom (X) axis panel
				Ext.getCmp("stylePopupBottomPanel").show();
				
				// Show the textfield dedicated for the title of the bottom (X) axis
				Ext.getCmp("textfieldAxisTitle").show();
				
				/* ---------- LEFT (Y) AXIS PANEL ---------- */
				// Show the gear tool on the toolbar of the left (Y) axis panel
				Ext.getCmp("stylePopupLeftAxis_"+chartColumnsContainer[0].id).show();
				
				// Show the plus tool on the toolbar of the left (Y) axis panel						
				Ext.getCmp("plusLeftAxis_"+chartColumnsContainer[0].id).show();
				
				// Show the textfield dedicated for the title of the left (Y) axis
				Ext.getCmp(chartColumnsContainer[0].id + "_TitleTextfield").show();
				
				// Show the serie&tooltip icon for SERIE records inside the left (Y) panel
				Ext.getCmp("actionColumnLeftAxis_"+chartColumnsContainer[0].id).items[0].iconCls = "";				
					
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
								
								//Select the new chart type
								chartTypeSelector.setChartType(selectedType);		
																
								/* START: Hide axis title textbox and gear tool for both left (Y)
								 * axis panel and bottom (X) axis panel and plus tool of the left
								 * (Y) panel when new row is clicked. (danilo.ristovski@mht.net) */
								if (selectedType.toLowerCase()=="sunburst" || selectedType.toLowerCase()=="wordcloud" || 
										selectedType.toLowerCase()=="treemap" || selectedType.toLowerCase()=="parallel" ||
											selectedType.toLowerCase()=="heatmap")
								{								
									var chartColumnsContainerNew = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
									var numberOfYAxis = chartColumnsContainerNew.length;
									
									if (numberOfYAxis > 1)
									{
										for (var i=0; i<numberOfYAxis; i++)
										{
											chartColumnsContainerNew[i+1].close();
										}
									}
									else
									{									
										Ext.getCmp("chartLeftAxisesContainer").items.items[0].header.items.items[1].hide();
									}
									
									/* ---------- BOTTOM (X) AXIS PANEL ---------- */
									// Hide the gear tool on the toolbar of the bottom (X) axis panel							
									if (selectedType.toLowerCase()!="heatmap")
									{
										Ext.getCmp("stylePopupBottomPanel").hide();
									}										
									
									// Hide the textfield dedicated for the title of the bottom (X) axis
									Ext.getCmp("textfieldAxisTitle").hide();
									
									/* ---------- LEFT (Y) AXIS PANEL ---------- */									
									if (selectedType.toLowerCase()!="heatmap")
									{
										// Hide the gear tool on the toolbar of the left (Y) axis panel
										Ext.getCmp("stylePopupLeftAxis_" + chartColumnsContainer[0].id).hide();										
									}											
									
									// Hide the plus tool on the toolbar of the left (Y) axis panel
									Ext.getCmp("plusLeftAxis_" + chartColumnsContainer[0].id).hide();
									
									// Hide the textfield dedicated for the title of the left (Y) axis
									Ext.getCmp(chartColumnsContainer[0].id + "_TitleTextfield").hide();
									
									// Hide the serie&tooltip icon for SERIE records inside the left (Y) panel
									Ext.getCmp("actionColumnLeftAxis_" + chartColumnsContainer[0].id).items[0].iconCls = "x-hidden";
								}
								else if (selectedType.toLowerCase()=="radar" || selectedType.toLowerCase()=="scatter")
								{
									/**
									 * If we change chart type from one that is not compatible with the RADAR or SCATTER
									 * to the one of those two, hide plus buttons because we cannot have more than one
									 * Y-axis panel for these types.
									 */
									// Hide the plus tool on the toolbar of the left (Y) axis panel
									Ext.getCmp("plusLeftAxis_" + chartColumnsContainer[0].id).hide();
									
								}
								/* END */
								
								globalThis.fireEvent("newrowclick");
							} 
							else if (buttonValue == 'cancel') 
							{
								for(var i = 0; i < store.data.length; i++) 
								{
									var row = store.getAt(i);
									
									if(thisChartType === row.get('type').toLowerCase()) 
									{
										selectionModel.select(i);
										break;
									}
								}
							}
						}
					});
				}				
				
				else 
				{
					/**
					 * If we come to RADAR chart from some chart type that is compatible with it 
					 * (e.g. BAR and LINE), keep the data, but remove all other Y-axis panels that
					 * were potentially defined earlier for those compatible chart types and hide
					 * the plus tool placed on the left Y-axis panels header. For RADAR chart we
					 * can have only one Y-axis.
					 */
					if (selectedType.toLowerCase() == "radar" || selectedType.toLowerCase() == "scatter")
					{
						/**
						 * If there are some Y-axis panels created before on the Designer (other 
						 * that the default (the left) one, remove them.
						 */
						
						var chartColumnsContainerNew = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
						var numberOfYAxis = chartColumnsContainerNew.length;
						
						if (numberOfYAxis > 1)
						{						
							for (var i=1; i<numberOfYAxis; i++)
							{
								Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[1].close();	
							}
						}					
						
						// Hide the plus tool on the toolbar of the left (Y) axis panel
						Ext.getCmp("plusLeftAxis_"+chartColumnsContainer[0].id).hide();
						
						/**
						 * We need confirmation from user for removing all the items (categories) from the
						 * bottom X-axis panel when moving from the BAR/LINE to RADAR/SCATTER chart type.
						 * Removing those items inside the X-axis panel is necessary because we can have
						 * multiple categories for BAR/LINE chart type, whilst we can have ONLY ONE CATEGORY
						 * for RADAR/SCATTER chart type.
						 */					
						if ((thisChartType == "bar" || thisChartType == "line") && 
								Ext.getCmp("chartBottomCategoriesContainer").store.data.length > 1)
						{								
							Ext.Msg.show
							(
								{
									title : '',
									message : LN("sbi.chartengine.designer.charttype.changetypeCategories"), 
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
											/** 
											 * Set active type chart as the one that we chosen now (in other words, set 
											 * the chart type as 'radar'. 
											 */ 
											chartTypeSelector.setChartType(selectedType);
											
											/**
											 * Clean the X-axis bottom panel for RADAR and SCATTER chart types
											 */
											Sbi.chart.designer.Designer.cleanCategoriesAxis();										
										}
										else if (buttonValue == 'cancel') 
										{
											for(var i = 0; i < store.data.length; i++) 
											{
												var row = store.getAt(i);
												
												if(thisChartType === row.get('type').toLowerCase()) 
												{
													selectionModel.select(i);
													break;
												}
											}
										}
									}	
								}
							);
						}
						
						else
						{
							/** 
							 * Set active type chart as the one that we chosen now (in other words, set 
							 * the chart type as 'radar'. 
							 */ 
							chartTypeSelector.setChartType(selectedType);
						}
						
						globalThis.fireEvent("newrowclick");
					}
					
					else 
					{
						chartTypeSelector.setChartType(selectedType);
						globalThis.fireEvent("newrowclick");
					}
				}	
				
				/**
				 * When we change the chart type, reset the 'Series type' parameter on the all Y-axises
				 * that exist on the page.
				 * (added: 23th July 2015)
				 * (danilo.ristovski@mht.net)
				 */
				var yAxisPool = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool;
				
				for (var i=0; i<yAxisPool.length; i++)
				{
					var leftYAxis = Sbi.chart.designer.ChartColumnsContainerManager.yAxisPool[i];
					var leftYAxisStoreData = leftYAxis.store.data;
					
					if(leftYAxisStoreData.length > 0)
					{
						for (var j=0; j<leftYAxisStoreData.length; j++)
						{
							leftYAxisStoreData.items[j].data.serieType = undefined;
						}
					}	
				}							
			}
			
		}
	},
	
	setChartType: function(type) {
		
		this.chartType = type;
				
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
			if(chartOrientationCombo != undefined) {
				chartOrientationCombo.disable();
			}
			if(chartRightAxisesContainer != undefined) {
				chartRightAxisesContainer.disable();
			}
		} else {
			if(chartOrientationCombo != undefined) {
				chartOrientationCombo.enable();
			}
			if(chartRightAxisesContainer != undefined) {
				chartRightAxisesContainer.enable();
			}
		}
	},

	getChartType: function() {
		return this.chartType;
	}
});