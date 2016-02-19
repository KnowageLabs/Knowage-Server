/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

Ext.define
(
	"Sbi.chart.designer.ChartConfigurationWordcloud",

	{
		extend : 'Sbi.chart.designer.ChartConfigurationRoot',
		
		id : "wordcloudConfiguration",
		
		columnWidth: 1,
		height: 295,
		
		title : LN("sbi.chartengine.configuration.wordcloud.configPanelTitle"),
		bodyPadding : 10,
		items : [],					

		requires : [ 'Sbi.chart.designer.StylePopupTip',
				'Sbi.chart.designer.StylePopupToolbar' ],

		fieldDefaults : {
			anchor : '100%'
		},

//		layout : {
//			type : 'vbox'
//		},

		constructor : function(config) {
			
			this.callParent(config);
			this.viewModel = config.viewModel;

			var globalScope = this;
			
			var item = [

					{
						xtype : 'combo',
						queryMode : 'local',
						forceSelection : true,
						editable : false,
						width: Sbi.settings.chart.configurationStep.widthOfFields,
	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
						bind : '{configModel.sizeCriteria}',
						displayField : 'name',
						valueField : 'value',					
						emptyText: LN("sbi.chartengine.configuration.wordcloudSizeCriteria.emptyText"),
						
						store : 
						{
							fields : [ 'name', 'value' ],
							
							data : 
							[
								{
									name : LN('sbi.chartengine.configuration.wordcloud.sizeCriteria.serie'),
									value : 'serie'
								},
								{
									name : LN('sbi.chartengine.configuration.wordcloud.sizeCriteria.occurrences'),
									value : 'occurrences'
								}
							]
						},
						
						listeners:
						{
							change: function(a,currentValue)
							{
								if (currentValue)
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + ":");
								}
								else
								{
									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.sizeCriteria") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
								}
							}
						}
					},
					
					
	                 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
								xtype : 'numberfield',
								bind : '{configModel.maxWords}',
								id: "wordcloudMaxWords",
								allowBlank: true,
								fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxWords") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
								width: Sbi.settings.chart.configurationStep.widthOfFields,
			        			padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields,
								maxValue : '300',
								minValue : '10',
								emptyText: LN("sbi.chartengine.configuration.wordcloudMaxNumWords.emptyText"),
								
								listeners:
								{
									change: function(a,currentValue)
									{
										if (currentValue || parseInt(currentValue)==0)
										{
											this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxWords") + ":");
										}
										else
										{
											this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxWords") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
										}
									}
								}
    	         			}
    	         		]
                	 },
                	 
                	 {
                		 xtype : 'fieldcontainer',
                	 
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
	//                		 labelWidth : '100%',
	                		 //margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
		                	 {                		 
		 						xtype : 'combo',
		 						queryMode : 'local',
		 						forceSelection : true,
		 						editable : false,
		 						width: Sbi.settings.chart.configurationStep.widthOfFields,
			        			padding: Sbi.settings.chart.configurationStep.paddingOfInnerFields,
		 						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.wordLayout") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
		 						bind : '{configModel.wordLayout}',
		 						displayField : 'name',
		 						valueField : 'value',					
		 						emptyText: LN("sbi.chartengine.configuration.wordcloudWordLayout.emptyText"),
		 						
		 						store : 
		 						{
		 							fields : [ 'name', 'value' ],
		 							
		 							data : 
		 							[
		 								{
		 									name : LN('sbi.chartengine.configuration.wordcloud.wordLayout.horizontal'),
		 									value : 'horizontal'
		 								},
		 								{
		 									name : LN('sbi.chartengine.configuration.wordcloud.wordLayout.vertical'),
		 									value : 'vertical'
		 								},
		 								{
		 									name : LN('sbi.chartengine.configuration.wordcloud.wordLayout.horizontalAndVerticaal'),
		 									value : 'horizontalAndVertical'
		 								},
		 								{
		 									name : LN('sbi.chartengine.configuration.wordcloud.wordLayout.randomAngle'),
		 									value : 'custom'
		 								}
		 							]
		 						},
		 						
		 						listeners:
		 						{
		 							change: function(a,currentValue,previousValue,c)
		 							{		 								
		 								if (currentValue)
		 								{
		 									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.wordLayout") + ":");		 									
		 									
		 									switch(currentValue){			 									
		 										case 'horizontal':
			 										Ext.getCmp("wordcloudMaxAngle").setValue(0);
			 										Ext.getCmp("wordcloudMinAngle").setValue(0);
			 										
			 										/**
			 										 * Hide field containers that contain the two fields that 
			 										 * we need to hide.
			 										 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 										 */
			 										Ext.getCmp("wordcloudMaxAngle").ownerCt.hide();
			 										Ext.getCmp("wordcloudMinAngle").ownerCt.hide();
			 										
			 										/**
			 										 * Decrease the height of the panel since we are hiding
			 										 * two fields which height is 20px each and also take away 
			 										 * some additional space that is taken by the padding between 
			 										 * them and the one that was set for making the distance 
			 										 * after the last field - 15px for each field. This will be 
			 										 * performed only if the previous value was 'custom' (the one
			 										 * that introduced these two fields that we need to hide). 
			 										 * 
			 										 * NOTE: The same goes for next two cases ('vertical',
			 										 * 'horizontalAndVertical').
			 										 * 
			 										 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 										 */
			 										if (previousValue == "custom")
		 											{
			 											globalScope.height = globalScope.height-2*20-2*15;
				 										globalScope.update();
				 										globalScope.ownerCt.update();
		 											}
			 									   break;
			 									case 'vartical':
			 										Ext.getCmp("wordcloudMaxAngle").setValue(90);
			 										Ext.getCmp("wordcloudMinAngle").setValue(90);
			 										
			 										/**
			 										 * Hide field containers that contain the two fields that 
			 										 * we need to hide.
			 										 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 										 */
			 										Ext.getCmp("wordcloudMaxAngle").ownerCt.hide();
			 										Ext.getCmp("wordcloudMinAngle").ownerCt.hide();
			 										
			 										if (previousValue == "custom")
		 											{
			 											globalScope.height = globalScope.height-2*20-2*15;
				 										globalScope.update();
				 										globalScope.ownerCt.update();
		 											}
			 									   break;   
			 									case 'horizontalAndVertical':
			 										Ext.getCmp("wordcloudMaxAngle").setValue(90);
			 										Ext.getCmp("wordcloudMinAngle").setValue(0);
			 										
			 										/**
			 										 * Hide field containers that contain the two fields that 
			 										 * we need to hide.
			 										 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 										 */
			 										Ext.getCmp("wordcloudMaxAngle").ownerCt.hide();
			 										Ext.getCmp("wordcloudMinAngle").ownerCt.hide();
			 										
			 										if (previousValue == "custom")
		 											{
			 											globalScope.height = globalScope.height-2*20-2*15;
				 										globalScope.update();
				 										globalScope.ownerCt.update();
		 											}
			 									   break;
			 									case 'custom':
			 										Ext.getCmp("wordcloudMaxAngle").setValue('');
			 										Ext.getCmp("wordcloudMinAngle").setValue('');
			 										
			 										/**
			 										 * Show field containers that contain the two fields
			 										 * that we need to show to the user. Before that, show
			 										 * these two fields, since they were hidden previously.
			 										 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 										 */
			 										Ext.getCmp("wordcloudMaxAngle").show();
			 										Ext.getCmp("wordcloudMinAngle").show();
			 										Ext.getCmp("wordcloudMaxAngle").ownerCt.show();
			 										Ext.getCmp("wordcloudMinAngle").ownerCt.show();
			 										
			 										/**
			 										 * Increase the height of the panel since we are introducing
			 										 * (showing) two new fields which height is 20px each and
			 										 * provide some additional space that is taken by the padding 
			 										 * between them and the one that is expected after the last
			 										 * GUI item (distance from the bottom edge) - 15px for each.
			 										 * 
			 										 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 										 */
			 										globalScope.height = globalScope.height+2*20+2*15;
			 										globalScope.update();
			 										globalScope.ownerCt.update();
			 									   break;		 									   
			 									}
		 								}
		 								else
		 								{
		 									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.wordLayout") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
		 								}
		 							}
		 						}
		                	 }
	                	 ]
                	 },
                	 
	                 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
							{
								xtype : 'numberfield',
								bind : '{configModel.maxAngle}',
								id: "wordcloudMaxAngle",
								fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
								width: Sbi.settings.chart.configurationStep.widthOfFields,
								padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
								maxValue : '360',
								minValue : '0',
								emptyText: LN("sbi.chartengine.configuration.wordcloudMaxWordAngle.emptyText"),
								
								listeners:
								{
									change: function(a,currentValue)
									{
										if (currentValue || parseInt(currentValue)==0)
										{
											this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxAngle") + ":");
										}
										else
										{
											this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
										}
									}
								}
							},
    	         		]
                	 },
					
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
	    	         			xtype : 'numberfield',
	    						bind : '{configModel.minAngle}',
	    						id: "wordcloudMinAngle",
	    						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.minAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	    						width: Sbi.settings.chart.configurationStep.widthOfFields,
	    	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
	    						maxValue : '360',
	    						minValue : '0',
	    						emptyText: LN("sbi.chartengine.configuration.wordcloudMinWordAngle.emptyText"),
	    						
	    						listeners:
	    						{
	    							change: function(a,currentValue)
	    							{
	    								if (currentValue || parseInt(currentValue)==0)
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.minAngle") + ":");
	    								}
	    								else
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.minAngle") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
	    								}
	    							}
	    						}
    	         			}
    	         		]
                	 },
                	 
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
	    	         			xtype : 'numberfield',
	    						bind : '{configModel.maxFontSize}',
	    						id: "wordcloudMaxFontSize",
	    						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	    						width: Sbi.settings.chart.configurationStep.widthOfFields,
	    	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
	    						maxValue : '500',
	    						minValue : '30',
	    						emptyText: LN("sbi.chartengine.configuration.wordcloudMaxFontSize.emptyText"),
	    						
	    						listeners:
	    						{
	    							change: function(a,currentValue)
	    							{								
	    								if (currentValue || parseInt(currentValue)==0)
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + ":");
	    								}
	    								else
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.maxFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
	    								}
	    							}
	    						}
    	         			}
    	         		]
                	 },
                	 
                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
	    	         			xtype : 'numberfield',
	    						bind : '{configModel.minFontSize}',
	    						id: "wordcloudMinFontSize",
	    						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.minFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	    						width: Sbi.settings.chart.configurationStep.widthOfFields,
	    	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
	    						maxValue : '200',
	    						minValue : '5',
	    						emptyText: LN("sbi.chartengine.configuration.wordcloudMinFontSize.emptyText"),
	    						
	    						listeners:
	    						{
	    							change: function(a,currentValue)
	    							{								
	    								if (currentValue || parseInt(currentValue)==0)
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.minFontSize") + ":");
	    								}
	    								else
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.minFontSize") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
	    								}
	    							}
	    						}
    	         			}
    	         		]
                	 },

                	 {            
	                	 xtype : 'fieldcontainer',
	                	 layout : 'hbox',
	                	 
	                	 defaults : 
	                	 {
//	                		 labelWidth : '100%',
	                		 margin:'0 30 0 0'
	                	 },
		                    	 
	                	 items: 
	            		 [	
	    	         		{
	    	         			xtype : 'numberfield',
	    						bind : '{configModel.wordPadding}',
	    						id: "wordcloudWordPadding",
	    						fieldLabel : LN("sbi.chartengine.configuration.wordcloud.wordPadding") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields,
	    						width: Sbi.settings.chart.configurationStep.widthOfFields,
	    	        			padding:Sbi.settings.chart.configurationStep.paddingOfTopFields,
	    						maxValue : '20',
	    						minValue : '2',
	    						emptyText: LN("sbi.chartengine.configuration.wordcloudWordPadd.emptyText"),
	    						
	    						listeners:
	    						{
	    							change: function(a,currentValue)
	    							{
	    								if (currentValue || parseInt(currentValue)==0)
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.wordPadding") + ":");
	    								}
	    								else
	    								{
	    									this.labelEl.update(LN("sbi.chartengine.configuration.wordcloud.wordPadding") + Sbi.settings.chart.configurationStep.htmlForMandatoryFields + ":");
	    								}
	    							}
	    						}
    	         			}
    	         		]
                	 }];

			this.add(item);
			Ext.getCmp("wordcloudMaxAngle").hide();
			Ext.getCmp("wordcloudMinAngle").hide();
		}
	}
);