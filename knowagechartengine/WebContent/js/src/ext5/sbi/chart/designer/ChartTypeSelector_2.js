/**
 * The new color picker in the form of combo box. 
 * 
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
Ext.ns("Sbi.chart.designer");

Ext.define
(
	'Sbi.chart.designer.ChartTypeSelector_2', 
	
	{
		extend: "Ext.form.ComboBox",
		id: "chartTypeCombobox",
		margin: '0 20 10 0',
		queryMode: 'local',
	    displayField: 'chartType',
	    valueField: 'chartTypeAbbr',
	    editable: false,
	    padding: "5 0 0 0",
	    width: Sbi.settings.chart.leftDesignerContainer.widthPercentageOfItem,
	    height: 40,
	    
	    /**
	     * Show icon that represents the chart type along with it's name.
	     */
	    tpl:
    	[
          	'<tpl for=".">',
          		'<div class="x-boundlist-item">',
      				'<img src="{iconChartType}" width="30px" style="display:inline-block; vertical-align:middle; line-height:20px;"/>',
      				'&nbsp;&nbsp;&nbsp;',
      				'<p style="display:inline-block; vertical-align:middle; line-height:20px;">{chartType}</p>',
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
			
			for (i=0; i < this.getStore().data.length; i++)
			{
				arrayIcons[this.getStore().data.items[i].data.chartTypeAbbr] = this.getStore().data.items[i].data.iconChartType;
			}
			
			return arrayIcons;
		},
		
		/**
		 * Function that serves for reconfiguration of the Structure tab of the 
		 * Designer when changing chart types (hide not necessary and not needed
		 * elements on this tab and show that are).
		 */
		reconfigureStructureTab: function()
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
			
			if(Sbi.chart.designer.ChartUtils.isBottomAxisStyleButtonDisabled()) {
				this.stylePopupBottomPanel.hide();
			} else {
				this.stylePopupBottomPanel.show();		
			}
				
			if(Sbi.chart.designer.ChartUtils.isBottomAxisTextFieldDisabled()) {
				this.textfieldAxisTitle.hide();
			} else {
				this.textfieldAxisTitle.show();			
			}
			this.stylePopupLeftAxis.show();						
			this.plusLeftAxis.show();				
			this.titleTextfield.show();	
			
			this.actionColumnLeftAxis.items[0].iconCls = "";
			
			/**
			 * Hide the tool that provides to the user the opportunity to set the ordering by
			 * category and its ordering type. For some chart types, this option should be 
			 * not provided, so we need to hide this tool in that situation.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			var categoryStylePopupTool = Ext.getCmp("idCategoryStylePopupTool");
			
			if (Sbi.chart.designer.ChartUtils.isCategoryStylePopupDisabled() && !categoryStylePopupTool.hidden)
			{
				categoryStylePopupTool.hide();
			}
			else if (categoryStylePopupTool.hidden)
			{
				categoryStylePopupTool.show();
			}
		},
		
		/**
		 * Function that handles the changing of the chart type from the Designer's 
		 * Structure and Configuration tab perspective (hiding and showing particular
		 * elements (GUI components) within them).
		 */
		reconfigureStructureAndConfigurationTab: function(newlySelectedType,previousChartType) {			
			
			var globalScope = this;	
		
			var iconsPath = this.getChartTypesIcons();
			
			/**
			 * Designer's 'chartLibNamesConfig' input parameter is a container of all available
			 * chart types to which their libraries name is associated.
			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */	
			var chartLibrary = Sbi.chart.designer.Designer.chartLibNamesConfig[newlySelectedType.toLowerCase()];

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
			 * Call the Designer's static function in order to update the empty text
			 * in the series/categories container (bottom X panel and Y axis panels) 
			 * in the Designer, depending on the chart type of the document.
			 */
			Sbi.chart.designer.Designer.emptyTextHandler(newlySelectedType.toLowerCase());
			
			/**
			 * Fire an event that the chart type is generally changed. This is a general information 
			 * for parts of code that depend on this activity - changing of the chart type of the 
			 * current document. E.g. this is useful for removing the picture from the Preview panel 
			 * inside the Designer page when the chart type is changed (we should not keep the preview 
			 * of some old (previous) chart type and chart configuration.
			 */
			globalScope.fireEvent("chartTypeChanged");	
										
			Sbi.chart.designer.ChartTypeSelector_2.chartType = newlySelectedType.toLowerCase();

//			/**
//			 * Cleaning of axis panels since previous and current chart types are not compatible.
//			 */
			Sbi.chart.designer.Designer.cleanAxesSeriesAndCategories();
//											
//			/**
//			 * Since we approved changing of the chart type, we need to reconfigure the GUI elements on
//			 * Structure tab of the Designer (as well as those on the Step 2 of the Designer). 
//			 */
			globalScope.reconfigureStructureTab();
//			
//			/**
//			 * Inform the Designer that it should take care of GUI elements on the Configuration tab of 
//			 * the Designer. It should hide excess GUI elements on this tab and show just those that are
//			 * necessary for the current chart type. This event is also caught inside the Designer.
//			 */			

			globalScope.fireEvent("reconfigureConfigurationTab");
			
			var bottomAxisPanel = Ext.getCmp("chartBottomCategoriesContainer");

			/**
			 * If the newly selected chart type is PARALLEL we should remove all potential
			 * content from the store that keeps all series available for the "Serie as 
			 * filter column". This is important for the case in which we open the PARALLEL
			 * chart, change the chart type and afterwards return back to the PARALLEL chart.
			 * If this is not done, we are keeping the store that was actual for the previous
			 * PARALLEL chart document.
			 */
			if (newlySelectedType.toLowerCase() == "parallel")
			{
				/**
				 * The combo box for the "Serie as filter column" on the
				 * Configuration tab's Limit panel.
				 */
				var seriesColumnsOnYAxisCombo = Ext.getCmp("seriesColumnsOnYAxisCombo");

				if(seriesColumnsOnYAxisCombo && seriesColumnsOnYAxisCombo != null && seriesColumnsOnYAxisCombo.hidden == false) 
				{
					seriesColumnsOnYAxisCombo.getStore().removeAll();
				}
			}
			
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
					if ((newlySelectedType.toLowerCase()!="chord" && newlySelectedType.toLowerCase()!="parallel")
							|| chartLibrary == 'chartJs') {

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
					globalScope.titleTextfield.hide();	//console.log("===");								
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
		},
				
		listeners:
		{
			/**
			 * When the chart type combo box receives an answer from the service
			 * that provides information about all available chart types inside
			 * the Designer (this data is contained inside the "engine_config.xml"
			 * we are ready to generate the store. Afterwards this event is fired
			 * from Designer.js so the item inside the combo box will correspond
			 * with its structure to the documents chart type and its icon.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			setInitialIcon: function()
			{
				var chartType = Sbi.chart.designer.Designer.chartType;
				
				Sbi.info("Chart type: " + chartType);
				
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
			
			reconfigureConfigurationTab: function()
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
			
			/**
			 * Handler for changing of the chart type od the document.
			 */
			change: function(comboBox,currentOrNewChartType,previousChartType)
			{	
				/**
				 * Scope of the chart type selector.
				 */
				var globalScope = this;	
				
				/**
				 * When changing the chart type, check if the newly picked one is existing (if the 
				 * code treat is as a part of application - it there is a specific customization
				 * inside the picked style sheet (XML template) for this document). If the one does
				 * not exist, this variable will be undefined. 
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				var chartSpecificConfigCheck = Designer.getConfigurationForStyle(Designer.styleName,true)[currentOrNewChartType.toLowerCase()];
						
				/**
				 * Check if the event is fired because of the change of the chart type and not
				 * because of the initial rendering the Designer (because that is also a moment
				 * in which this event is fired by the ExtJS combo component). If the previous
				 * chart type is not null (we are actually changing chart type from the current
				 * one to the new one), start handling chart type change.
				 */
				if (previousChartType!=null)
				{
					if (chartSpecificConfigCheck)
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
						
						var messageTxt = "";
					
						/**
						 * When changing chart types: check if the style of the current document template exists 
						 * on the server (an XML file). If does not, check also if the backup style (the one that
						 * should be applied when the old chart style does not exist anymore) is already applied.
						 * If it is not, show appropriate notification about loosing current configuration of the
						 * file and about applying the backup style. 
						 */
						if (!Sbi.chart.designer.Designer.jsonTemplateStyleExists && !Sbi.chart.designer.Designer.backupStyleSet)
						{
							messageTxt = LN("sbi.chartengine.designer.charttype.changetype.lossOfAxesSerStyleConfAndSeriesCategorAndStyle")
						}
						else
						{						
							messageTxt = LN("sbi.chartengine.designer.charttype.changetype.lossOfAxesSerStyleConfAndSeriesCategor");	
						}													

						/**
						 * Warn user that he is about to lose all axis style configuration and
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
										 * If 'currentOrNewChartType' is not null - if user did not click-down on the
										 * chart type and then move mouse on some other chart type and make a click-up
										 * we can proceed with change of the chart type. Otherwise (without this check)
										 * the error will appear.
										 */
										if (currentOrNewChartType!=null)
										{
											/**
											 * When Designer renders for the first time (when opening the chart in it for the first
											 * time) the second input parameter ('currentOrNewChartType') of this function (event) 
											 * will be equal to the actual chart type (document). The third input parameter 
											 * ('previousChartType') will be null. 
											 * 
											 * When we change the chart type inside the Designer the second parameter will contain newly
											 * selected chart type, while the third parameter will contain value of the chart type that 
											 * was previously selected.
											 */				

											/**
											 * The chart type that is actually picked (clicked). Newly selected chart type.
											 */
											var newlySelectedType = currentOrNewChartType.toLowerCase();	
//											
											/**
											 * If newly clicked (selected) chart type ('selectedType') in ChartTypeSelector is 
											 * of the same type as the chart that we have in Designer (the one that has already
											 * been chosen or defined by the loading of the already existing chart document, 
											 * 'previousChartType'), do not take any action.
											 */				
											if (newlySelectedType != previousChartType)
											{	
												globalScope.reconfigureStructureAndConfigurationTab(newlySelectedType,previousChartType);
											}			
										}
									}
									else
									{
										/**
										 * If user does not want to change the current chart type and the axes/serie style configuration
										 * customization made until the moment, we will cancel its change and keep the current data.
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
					else
					{
						/**
						 * If the specific customization inside the selected (or backup) XML style sheet 
						 * (template) is not defined (not handled by the application, not existing), return 
						 * the chart type to the previous one, since we do not handle newly picked one. This
						 * way we skip error that could happen otherwise when merging JSON templates of the
						 * document.
						 * 
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						Sbi.exception.ExceptionHandler.showErrorMessage
						(
							Sbi.locale.sobstituteParams
							(
								LN("sbi.chartengine.designer.notExistingChartType.msg"),
								
								[
								 	currentOrNewChartType[0].toUpperCase()+currentOrNewChartType.substring(1,currentOrNewChartType.length)
								]
							),
								
							LN('sbi.chartengine.designer.notExistingChartType.title')
						);
						
						/**
						 * If user does not want to change the current chart type and the axes/serie style configuration
						 * customization made until the moment, we will cancel its change and keep the current data.
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
		}
	}
);