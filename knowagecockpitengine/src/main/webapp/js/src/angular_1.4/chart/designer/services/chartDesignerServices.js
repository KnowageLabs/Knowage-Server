/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero
 *  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('ChartDesignerService', ['chartRendererModule'])
.service('ChartDesignerData',function(sbiModule_restServices, sbiModule_messaging,sbiModule_translate,sbiModule_config, $http){
	
	this.getFontSizeOptions = function(){
		var data = [
			{name:"8px",value:"8px"},
			{name:"9px",value:"9px"},
			{name:"10px",value:"10px"},
			{name:"11px",value:"11px"},
			{name:"12px",value:"12px"},
			{name:"14px",value:"14px"},
			{name:"16px",value:"16px"},
			{name:"18px",value:"18px"},
			{name:"20px",value:"20px"},
			{name:"22px",value:"22px"},
			{name:"24px",value:"24px"},
			{name:"26px",value:"26px"},
			{name:"28px",value:"28px"},
			{name:"36px",value:"36px"},
			{name:"48px",value:"48px"},
			{name:"72px",value:"72px"},
		                           ]
		return data;
	};
	
	this.getDimensionMeasureTypeOptions = function(){
		var data = [
			  {name:"px",value:"pixels"},
			  {name:"%",value:"percentage"}                          
			                             ]                        
		return data;
	};
	
	this.getOrientationTypeOptions = function(){
		var data = [
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.vertical"),value:"vertical"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.horizontal"),value:"horizontal"}                          
			                               ]                          
		return data;
	};
	
	this.getAlignTypeOptions = function(){
		var data = [
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.left"),value:"left"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.center"),value:"center"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.right"),value:"right"},
			                               ]                          
		return data;
	};
	
	this.getVerticalAlignTypeOptions = function(){
		var data = [
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top"),value:"top"},
				{name:sbiModule_translate.load("sbi.chartengine.configuration.alignment.m"),value:"middle"},
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom"),value:"bottom"},
			                               ]                          
		return data;
	};
	
	this.getParallelOrderOptions = function(){
		var data = [
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top"),value:"top"},
				{name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom"),value:"bottom"},
			                               ]                          
		return data;
	};
	
	this.getWordLayoutOptions = function(){
		var data = [
		            
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.horizontal"),value:"horizontal"},		            
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.vertical"),value:"vertical"},
				{name:sbiModule_translate.load("sbi.chartengine.configuration.wordcloud.wordLayout.horizontalAndVerticaal"),value:"horizontalAndVertical"},
				{name:sbiModule_translate.load("sbi.chartengine.configuration.wordcloud.wordLayout.randomAngle"),value:"custom"},
			                               ]                          
		return data;
	};
	
	this.getPositionTypeOptions = function(){
		var data = [
		      {name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.top"),value:"top"},
		      {name:sbiModule_translate.load("sbi.cockpit.widgets.piechartwidgetdesigner.form.legend.position.bottom"),value:"bottom"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.left"),value:"left"},
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.textalignment.right"),value:"right"},
			                               ]                          
		return data;
	};
	
	this.getFontFamilyOptions = function(){
		var data = [
			{name:"Inherit",value:"inherit"},
			{name:"Roboto",value:"Roboto"},
           	{name:"Arial",value:"Arial"},
           	{name:"Times New Roman",value:"Times New Roman"},
           	{name:"Tahoma",value:"Tahoma"},
           	{name:"Verdana",value:"Verdana"},
           	{name:"Impact",value:"Impact"},
           	{name:"Calibri",value:"Calibri"},
           	{name:"Cambria",value:"Cambria"},
           	{name:"Georgia",value:"Georgia"},
           	{name:"Gungsuh",value:"Gungsuh"},
                                      ]                        
		return data;
	};
	
	this.getFontStyleOptions = function(){
		var data = [	
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.nostyle"),value:""},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.bold"),value:"bold"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.normal"),value:"normal"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.italic"),value:"italic"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.underline"),value:"underline"},
        	                        ]                       
		return data;
	};
	
	this.getFontStyleOptionsNS = function(){
		var data = [	
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.bold"),value:"bold"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.normal"),value:"normal"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.italic"),value:"italic"},
        	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.underline"),value:"underline"},
        	                        ]                       
		return data;
	};
	
	this.getTooltipBreadcrumbValueTypeOptions = function(){
		var data = [	
		 	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.absolute"),value:"absolute"},
		 	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.percentage"),value:"percentage"},
		 	{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.combination"),value:"combination"},
		 	                        ]                           
		return data;
	};
	
	this.getChartConfigurationOptions = function(chart){
		
		var data = [
			{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.generic"),value:"generic"},
            {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.titlesubtitle"),value:"title_and_subtitle"},
            {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.nodata"),value:"no_data"}	
		];
		
		var legend = [
			  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.legendtitle"),value:"legend_title"},
              {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.legenditems"),value:"legend_items"}
		];
		
		switch (chart) {
		case 'parallel':
			var options = [
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.tooltip"),value:"tooltip"},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.limit"),value:"limit"},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.axislines"),value:"axis_lines"}
                                             ]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'sunburst':
			var options =[
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.sequence"),value:"sequence"},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.exlpanation"),value:"explanation"}
                                             ]
			Array.prototype.push.apply(data, options);
			return data;	
			break;
		case 'scatter':
			var options =[
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
                {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.ticksandlabels"),value:"ticks_and_labels"}
			]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'treemap':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"}]
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'wordcloud':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.wordsettings"),value:"word_settings"}]
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'gauge':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
						  {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.pane"),value:"pane"}]     
			Array.prototype.push.apply(data, options);
			return data;  
			break;
		case 'line':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;     
			break;
		case 'heatmap':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
							{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.tooltip"),value:"tooltip"}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'radar':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'bar':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
				{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.advancedSerieBar"),value:"advancedSerieConfBar"}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'pie':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"},
			    {name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.advancedSerieBar"),value:"advancedSerieConfBar"}]
			Array.prototype.push.apply(data, legend);
			Array.prototype.push.apply(data, options);
			return data;
			break;
		case 'chord':
			var options =[{name:sbiModule_translate.load("sbi.chartengine.designer.tab.configuration.palette"),value:"palette"}]
			Array.prototype.push.apply(data, options);
			return data;
			break;	
		default:
			break;
		}
	};
	
	this.getTemplateURLs = function(){
		var data = {
				genericDetailsURL:	sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/generic_details.html',
				titleSubtitleDetailsURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/title_and_subtitle.html',
				noDataDetailsURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/nodata.html',
				legendTitleURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/legend_title.html',
				legendItemsURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/legend_items.html',
				colorPaletteURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/color_palette.html',
				paneURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/pane.html',
				ticksURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/ticks.html',
				wordSettingsURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/word_settings.html',
				limitURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/limit.html',
				axisLinesURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/axis_lines.html',
				tooltipURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/tooltip.html',
				sequenceURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/sequence.html',
				explanationURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/explanation.html',
				advancedSerieBarURL: sbiModule_config.contextName + '/js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/advanced_serie_conf_bar.html',
			 };                   
		return data;
	};
})
/**
 * Service for the Structure tab 
 */
.service("StructureTabService", function(sbiModule_restServices,sbiModule_messaging,sbiModule_translate,sbiModule_config){
	
		var translate = sbiModule_translate;
	
	this.getBaseTemplate = function() {
		var barLine  = {  
				   "CHART":{  
					      "TITLE":{  
					         "style": {
					        	 "align":"",
					        	 "color":"",
					        	 "fontFamily":"",
					        	 "fontSize":"",
					        	 "fontWeight":""
					         },
					         "text":""
					      },
					      "VALUES":{  
					         "CATEGORY":{  
					            "name":"",
					            "column":"",
					            "orderColumn":"",
					            "orderType":"",
					            "stackedType":"",
					            "stacked":"",
					            "groupby":"",
					            "groupbyNames":""
					         },
					         "SERIE":[  
					        	 {  
					                 "axis":"",
					                 "color":"",
					                 "column":"",
					                 "groupingFunction":"SUM",
					                 "name":"",
					                 "orderType":"",
					                 "postfixChar":"",
					                 "precision":0,
					                 "prefixChar":"",
					                 "showValue":true,
					                 "showAbsValue":false,
					                 "showPercentage":false,
					                 "showCategoryValue":false,
					                 "scaleFactor":"empty",
					                 "type":"",
					                 "TOOLTIP":{  
					                    "backgroundColor":"",
					                    "borderWidth":0,
					                    "borderRadius":0,
					                    "style":{
							            	   "align":"",
							            	   "color":"",
							            	   "fontFamily":"",
							            	   "fontSize":"",
							            	   "fontWeight":""
							               }
					                 }
					              }
					         ]
					      },
					      "type":"BAR",
					      "AXES_LIST":{  
					         "AXIS":[  
					            {  
					               "id":"Y",
					               "alias":"Y",
					               "type":"Serie",
					               "position":"",
					               "min":0,
					               "max":0,
					               "style":{
					            	   "rotate":"",
					            	   "align":"",
					            	   "color":"",
					            	   "fontFamily":"",
					            	   "fontSize":"",
					            	   "fontWeight":""
					               },
					               "MAJORGRID":{  
					                  "interval":"",
					                  "style":{
					                	  "typeLine":"",
					                	  "color":""
					                  }
					               },
					               "MINORGRID":{  
					            	  "interval":"",
					                  "style":{
					                	  "typeLine":"",
					                	  "color":""
					                  }
					               },
					               "TITLE":{  
					                  "text":"",
					                  "style":{
					                	  "align":"",
					                	  "color":"",
					                	  "fontFamily":"",
					                	  "fontWeight":"",
					                	  "fontSize":""
					                  }
					               },
					               "TITLESERIE":{  
					            	   "showTitle": false,
					            	   "style":{
						                	  "align":"",
						                	  "color":"",
						                	  "fontFamily":"",
						                	  "fontWeight":"",
						                	  "fontSize":""
						                  }
						               }
					            },
					            {  
					               "id":"X",
					               "alias":"X",
					               "type":"Category",
					               "position":"",
					               "step" : "",
					               "style":{
					            	   "rotate":"",
					            	   "align":"",
					            	   "color":"",
					            	   "fontFamily":"",
					            	   "fontSize":"",
					            	   "fontWeight":""
					               },
					               "TITLE":{  
						                  "text":"",
						                  "style":{
						                	  "align":"",
						                	  "color":"",
						                	  "fontFamily":"",
						                	  "fontWeight":"",
						                	  "fontSize":""
						                  }
						               }
					            }
					         ]
					      },
					      "COLORPALETTE":{
					    	  "COLOR":[
						        	
							         ] 
					      },
					         
					      "height":100,
					      "width":100,
					      "isCockpitEngine":"",
					      "orientation":"",
					      "style":{
		                	  "backgroundColor":"#FFFFFF",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  },
		                  "styleName": "default",
					      "SUBTITLE":{  
					    	  "style":{
			                	  "align":"",
			                	  "color":"",
			                	  "fontFamily":"",
			                	  "fontWeight":"",
			                	  "fontSize":""
			                  },
					         "text":""
					      },
					      "EMPTYMESSAGE":{  
					    	  "style":{
			                	  "color":"",
			                	  "fontFamily":"",
			                	  "fontWeight":"",
			                	  "fontSize":""
			                  },
			                  "position":{
			                	  "align": "",
			                	  "verticalAlign": "middle"
			                  },
					         "text":""
					      },
					      "LEGEND":{  
					         "layout":"",
					         "position":"top",
					         "show":false,
					         "style":{
			                	  "align":"",
			                	  "color":"",
			                	  "fontFamily":"",
			                	  "fontWeight":"",
			                	  "fontSize":"",
			                	  "borderWidth":"",
			                	  "backgroundColor":""
			                  },
					         "TITLE":{  
					            "text":"",
					            "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
					         }
					      },
					      "heightDimType":"percentage",
					      "widthDimType":"percentage",
					      "borderVisible":false,
					      "seriesStacking":false,
					      "alpha":  0,
  		                  "beta": 0,
			              "depth":  0,
			              "viewDistance":  0,
			              "show3D" : false,
			              "groupCategories" : false,
			              "groupSeries" : false,
			              "groupSeriesCateg" : false,
			              "dateTime":false,
			              "dateFormat":"day"
					   }
					}
		return barLine.CHART;
	}
	
	this.getGaugeTemplate = function() {
			var guageTemp = {
					"TITLE": {
						"style":{
							"align":"",
							"color":"",
							"fontFamily":"",
							"fontWeight":"",
							"fontSize":""
						},
						"text": ""
					},
					"VALUES": {
						"SERIE": [{
							"axis": "",
							"color": "",
							"column": "",
							"groupingFunction": "SUM",
							"name": "",
							"orderType": "",
							"postfixChar": "",
							"precision": 0,
							"prefixChar": "",
							"scaleFactor": "empty",
							"showAbsValue": false,
							"showPercentage": false,
							"showValue": true,
							"type": "",
							"TOOLTIP": {
								"backgroundColor": "",
								"borderRadius": 0,
								"borderWidth": 0,
								"style": {
									"align":"",
									"color":"",
									"fontFamily":"",
									"fontWeight":"",
									"fontSize":""
								}
							},
							"DIAL": {
								"backgroundColorDial": ""
							},
							"DATA_LABELS": {
								"colorDataLabels": "",
								"yPositionDataLabels": ""
							}
						}]
					},
					"type": "GAUGE",
					"AXES_LIST": {
						"AXIS": [{
							"alias": "Y",
							"type": "Serie",
							"position": "",
							"min": "",
							"max": "",
							"lineColor": "",
							"tickPosition": "",
							"tickColor": "",
							"minorTickLength": 0,
							"lineWidth": 0,
							"endOnTickGauge": false,
							"minorTickInterval": "auto",
							"minorTickPosition": "",
							"minorTickWidth": 0,
							"minorTickColor": "",
							"tickPixelInterval": 0,
							"tickWidth": 0,
							"tickLength": 0,
							"style": {
								"rotate":"",
								"align":"",
								"color":"",
								"fontFamily":"",
								"fontWeight":"",
								"fontSize":""
									},
							"MAJORGRID": {
								"interval": "",
								"style": {
									"typeline":"",
									"color":""
								}
							},
							"MINORGRID": {
								"interval": "",
								"style": {
									"typeline":"",
									"color":""
								}
							},
							"TITLE": {
								"text": "",
								"style": {
									"align":"",
									"color":"",
									"fontFamily":"",
									"fontWeight":"",
									"fontSize":""
								}
							},
							"LABELS": {
								"distance": 0,
								"rotation": 0
							},
							"PLOTBANDS":{
								"PLOT":[]
							}
						}]
					},
					"COLORPALETTE": {
						"COLOR": []
					},
					"height": 100,
					"heightDimType": "percentage",
					"isCockpitEngine": "",
					"orientation": "",
					"seriesStacking": false,
					"style":{
						"fontFamily":"",
						"fontSize":"",
						"fontWeight":"",
						"backgroundColor":"#FFFFFF"
					},
					"styleName": "default",
					"width": 100,
					"widthDimType": "percentage",
					"SUBTITLE": {
						"style":{
							"align":"",
							"color":"",
							"fontFamily":"",
							"fontWeight":"",
							"fontSize":""
						},
						"text": ""
					},
					"EMPTYMESSAGE":{  
				    	  "style":{
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  },
		                  "position":{
		                	  "align": "",
		                	  "verticalAlign": "middle"
		                  },
				         "text":""
				      },
					"LEGEND": {
						"layout": "",
						"position": "top",
						"show": false,
						"style":{
							"align":"",
							"backgroundColor":"",
							"color":"",
							"fontFamily":"",
							"fontWeight":"",
							"fontSize":"",
							"borderWidth":0
						},
						"x": 0,// todo
						"y": 0,// todo
						"TITLE": {
							"style":{
								"align":"",
								"color":"",
								"fontFamily":"",
								"fontWeight":"",
								"fontSize":""
							},
							"text": ""
						}
					},
					"PANE": {
						"endAngle": 120,
						"startAngle": -120
					}
				}

			return guageTemp;
	}
	
	this.getHeatmapTemplate = function() {
		var heatTemp = {
				"TITLE": {
					"style":{
						"align":"",
						"color":"",
						"fontFamily":"",
						"fontWeight":"",
						"fontSize":""
					},
					"text": ""
				},
				"VALUES": {
					"CATEGORY": [{
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					}],
					"SERIE": [{
						"axis": "",
						"color": "",
						"column": "",
						"groupingFunction": "SUM",
						"name": "",
						"orderType": "",
						"postfixChar": "",
						"precision": 0,
						"prefixChar": "",
						"scaleFactor": "empty",
						"showAbsValue": false,
						"showPercentage": false,
						"showValue": true,
						"type": "",
						"TOOLTIP": {
							"backgroundColor": "",
							"borderRadius": 0,
							"borderWidth": 0,
							"style": {
								"align":"",
								"color":"",
								"fontFamily":"",
								"fontWeight":"",
								"fontSize":""
							}
						}
					}]
				},
				"type": "HEATMAP",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"min":0,
						"max":0,
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					}]
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"height": 100,
				"heightDimType": "percentage",
				"isCockpitEngine": "",
				"orientation": "horizontal",
				"seriesStacking": false,
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"styleName": "default",
				"width": 100,
				"widthDimType": "percentage",
				"SUBTITLE": {
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
					"text": ""
				},
				"EMPTYMESSAGE": {
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
	                "text": ""
				},
				"LEGEND": {
					"layout": "",
					"position": "",
					"show": false,
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":"",
	                	  "borderWidth":"",
	                	  "backgroundColor":""
	                  },
					"symbolHeight": 0,
					"x": 0,
					"y": 0,
					"TITLE": {
						"style":{
		                	  "align":"",
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  },
						"text": ""
					}
				},
				"TOOLTIP": {
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  }
				}
			}

		return heatTemp;
	}
	
	this.getParallelTemplate = function() {
		var parallelTemp = {
				"TITLE":{  
			         "style": {
			        	 "align":"",
			        	 "color":"",
			        	 "fontFamily":"",
			        	 "fontSize":"",
			        	 "fontWeight":""
			         },
			         "text":""
			      },
				"VALUES": {
					"CATEGORY": [{
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					}],
					"SERIE":[  
			        	 {  
			                 "axis":"",
			                 "color":"",
			                 "column":"",
			                 "groupingFunction":"SUM",
			                 "name":"",
			                 "orderType":"",
			                 "postfixChar":"",
			                 "precision":0,
			                 "prefixChar":"",
			                 "showValue":true,
			                 "showAbsValue":false,
			                 "showPercentage":false,
			                 "scaleFactor":"empty",
			                 "type":"",
			                 "TOOLTIP":{  
			                    "backgroundColor":"",
			                    "style":{
					            	   "rotate":"",
					            	   "align":"",
					            	   "color":"",
					            	   "fontFamily":"",
					            	   "fontSize":"",
					            	   "fontWeight":""
					               },
			                    "borderWidth":"",
			                    "borderRadius":""
			                 }
			              }
			         ]
				},
				"type": "PARALLEL",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					}],
					"style":{
						"axisColNamePadd":0,
						"brushWidth":5,
						"axisColor":"#000000",
						"brushColor":""
						
					}
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"height": 100,
				"heightDimType": "percentage",
				"isCockpitEngine": "",
				"orientation": "horizontal",
				"seriesStacking": false,
				"showTableParallel": false,
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"styleName": "default",
				"width": 100,
				"widthDimType": "percentage",
				"EMPTYMESSAGE":{  
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
			      "SUBTITLE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
				"LEGEND": {
					"TITLE": {
						"style":{
		                	  "fontFamily":"Arial",
		                	  "fontWeight":"normal",
		                	  "fontSize":"12px"
		                  },
						"text": ""
					},
					"ELEMENT": {
						"style":{
		                	  "fontFamily":"Arial",
		                	  "fontWeight":"italic",
		                	  "fontSize":"12px"
		                  }
					}
				},
				"LIMIT": {
					"groupByCategory": true,
					"style":{
						"maxNumberOfLines":5,
						"serieFilterColumn":"",
						"orderTopMinBottomMax":"top"
					}
				},
				"PARALLEL_TOOLTIP": {
					"style":{
						"fontFamily":"Arial",
						"fontSize":"12px",
						"border":0,
						"borderRadius":0,
						"fontWeight":""
					}
				}
			}

		return parallelTemp;
	}
	
	this.getRadarTemplate = function() {
		var radarTemp = {
				"TITLE":{  
			         "style": {
			        	 "align":"",
			        	 "color":"",
			        	 "fontFamily":"",
			        	 "fontSize":"",
			        	 "fontWeight":""
			         },
			         "text":""
			      },
				"VALUES": {
					"CATEGORY": {
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					},
					"SERIE": [{
						"axis": "Y",
						"color": "",
						"column": "",
						"groupingFunction": "SUM",
						"name": "",
						"orderType": "",
						"postfixChar": "",
						"precision": 0,
						"prefixChar": "",
						"showValue": "",
						"showAbsValue": false,
						"showPercentage": false,
						"scaleFactor": "empty",
						"type": "",
						"TOOLTIP":{  
		                    "backgroundColor":"",
		                    "style":{
				            	   "rotate":"",
				            	   "align":"",
				            	   "color":"",
				            	   "fontFamily":"",
				            	   "fontSize":"",
				            	   "fontWeight":""
				               },
		                    "borderWidth":"",
		                    "borderRadius":""
		                 }
					}]
				},
				"type": "RADAR",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					}]
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"height": 100,
				"isCockpitEngine": "",
				"orientation": "",
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"width": 100,
				"LEGEND": {
					"layout": "",
					"position": "",
					"show": false,
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":"",
	                	  "borderWidth":"",
	                	  "backgroundColor":""
	                  },
					"x": 0,
					"y": 0,
					"TITLE":{  
			            "text":"",
			            "style":{
		                	  "align":"",
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  }
			         }
				},
				"SUBTITLE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
			      "EMPTYMESSAGE":{  
			    	  "style":{
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
	                  "position":{
	                	  "align": "",
	                	  "verticalAlign": "middle"
	                  },
			         "text":""
			      },
				"heightDimType": "percentage",
				"widthDimType": "percentage",
				"borderVisible": false,
				"seriesStacking": false,
				"styleName": "default"
			};
		return radarTemp;
	}
	
	this.getScatterTemplate = function() {
		var scatterTemp = {
				"TITLE":{  
			         "style": {
			        	 "align":"",
			        	 "color":"",
			        	 "fontFamily":"",
			        	 "fontSize":"",
			        	 "fontWeight":""
			         },
			         "text":""
			      },
				"VALUES": {
					"CATEGORY": {
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					},
					"SERIE": [{
						"axis": "",
						"color": "",
						"column": "",
						"groupingFunction": "SUM",
						"name": "",
						"orderType": "",
						"postfixChar": "",
						"precision": 0,
						"prefixChar": "",
						"showValue": "",
						"showAbsValue": false,
						"showPercentage": false,
						"scaleFactor": "empty",
						"type": "",
						"TOOLTIP":{  
		                    "backgroundColor":"",
		                    "style":{
				            	   "rotate":"",
				            	   "align":"",
				            	   "color":"",
				            	   "fontFamily":"",
				            	   "fontSize":"",
				            	   "fontWeight":""
				               },
		                    "borderWidth":0,
		                    "borderRadius":0
		                 }
					}]
				},
				"type": "SCATTER",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"step" : "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               },
						"startOnTick": true,
						"endOnTick": true,
						"showLastLabel": true
					}]
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"borderVisible": false,
				"height": 100,
				"heightDimType": "percentage",
				"isCockpitEngine": "",
				"dateTime":false,
				"dateFormat":"day",
				"orientation": "",
				"seriesStacking": false,
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"styleName": "default",
				"width": 100,
				"widthDimType": "percentage",
				"zoomType": "",
				"LEGEND": {
					"layout": "",
					"position": "",
					"show": false,
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":"",
	                	  "borderWidth":"",
	                	  "backgroundColor":""
	                  },
					"x": 0,
					"y": 0,
					"TITLE":{  
			            "text":"",
			            "style":{
		                	  "align":"",
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  }
			         }
				},
				"SUBTITLE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
			      "EMPTYMESSAGE":{  
			    	  "style":{
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
	                  "position":{
	                	  "align": "",
	                	  "verticalAlign": ""
	                  },
			         "text":""
			      }
			};

		return scatterTemp;
	}
	
	this.getSunburstTemplate = function() {
		var sunburstTemp = {
				"TITLE":{  
			         "style": {
			        	 "align":"",
			        	 "color":"",
			        	 "fontFamily":"",
			        	 "fontSize":"",
			        	 "fontWeight":""
			         },
			         "text":""
			      },
				"VALUES": {
					"CATEGORY": [{
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					}],
					"SERIE": [{
						"axis": "",
						"color": "",
						"column": "",
						"groupingFunction": "SUM",
						"name": "",
						"orderType": "",
						"postfixChar": "",
						"precision": 0,
						"prefixChar": "",
						"showValue": true,
						"type": "",
						"showAbsValue": false,
						"showPercentage": false,
						"scaleFactor": "empty"
					}]
				},
				"type": "SUNBURST",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					}]
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"height": 100,
				"isCockpitEngine": "",
				"opacMouseOver": 0,
				"orientation": "",
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"styleName": "default",
				"width": 100,
				"LEGEND": {
					"layout": "",
					"position": "",
					"show": false,
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":"",
	                	  "borderWidth":"",
	                	  "backgroundColor":""
	                  },
					"x": 0,
					"y": 0,
					"TITLE":{  
			            "text":"",
			            "style":{
		                	  "align":"",
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  }
			         }
				},
				"TIP": {
					"style":{
						"fontFamily":"Arial",
						"fontWeight":"normal",
						"fontSize":"12px",
						"color":"#000000",
						"width":10
					},
					"text": "input explanation here"
				},
				"SUBTITLE": {
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
					"text": ""
				},
				"TOOLBAR": {
					"style":{
						"position":"top",
						"spacing":5,
						"tail":10,
						"percFontColor":"#000000",
						"fontFamily":"",
						"fontWeight":"normal",
						"fontSize":"12px",
					}
				},
				"EMPTYMESSAGE":{  
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			     },
				"heightDimType": "percentage",
				"widthDimType": "percentage",
				"seriesStacking": false,
				"percAbsolSliceValue": ""
			};

		return sunburstTemp;
	}
	
	this.getTreemapTemplate = function() {
		var treeMapTemp = {
				"TITLE":{  
			         "style": {
			        	 "align":"",
			        	 "color":"",
			        	 "fontFamily":"",
			        	 "fontSize":"",
			        	 "fontWeight":""
			         },
			         "text":""
			      },
				"VALUES": {
					"CATEGORY": [{
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					}],
					"SERIE": [{
						"axis": "",
						"color": "",
						"column": "",
						"groupingFunction": "SUM",
						"name": "",
						"orderType": "",
						"postfixChar": "",
						"precision": 0,
						"prefixChar": "",
						"showValue": "",
						"type": "",
						"TOOLTIP":{  
		                    "backgroundColor":"",
		                    "style":{
				            	   "rotate":"",
				            	   "align":"",
				            	   "color":"",
				            	   "fontFamily":"",
				            	   "fontSize":"",
				            	   "fontWeight":""
				               },
		                    "borderWidth":0,
		                    "borderRadius":0
		                 },
						"showAbsValue": false,
						"showPercentage": false,
						"scaleFactor": "empty"
					}]
				},
				"type": "TREEMAP",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					}]
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"height": 100,
				"isCockpitEngine": "",
				"orientation": "horizontal",
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"width": 100,
				"LEGEND": {
					"layout": "",
					"position": "",
					"show": false,
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":"",
	                	  "borderWidth":"",
	                	  "backgroundColor":""
	                  },
					"x": 0,
					"y": 0,
					"TITLE":{  
			            "text":"",
			            "style":{
		                	  "align":"",
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  }
			         }
				},
				"SUBTITLE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
			      "EMPTYMESSAGE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
				"heightDimType": "percentage",
				"widthDimType": "percentage",
				"seriesStacking": false,
				"styleName": "default"
			};

		return treeMapTemp;
	}
	
	this.getWordCloudTemplate = function() {
		var wordCloudTemp = {
				"TITLE":{  
			         "style": {
			        	 "align":"",
			        	 "color":"",
			        	 "fontFamily":"",
			        	 "fontSize":"",
			        	 "fontWeight":""
			         },
			         "text":""
			      },
				"VALUES": {
					"CATEGORY": [{
						"column": "",
						"groupby": "",
						"groupbyNames": "",
						"name": "",
						"orderColumn": "",
						"orderType": "",
						"stacked": "",
						"stackedType": ""
					}],
					"SERIE": [{
						"axis": "",
						"color": "",
						"column": "",
						"groupingFunction": "SUM",
						"name": "",
						"orderType": "",
						"postfixChar": "",
						"precision": 0,
						"prefixChar": "",
						"scaleFactor": "empty",
						"showAbsValue": false,
						"showPercentage": false,
						"showValue": true,
						"type": "",
						"TOOLTIP": {
							"backgroundColor": "",
							"borderRadius": 0,
							"borderWidth": 0,
							"style":{
				            	   "rotate":"",
				            	   "align":"",
				            	   "color":"",
				            	   "fontFamily":"",
				            	   "fontSize":"",
				            	   "fontWeight":""
				               }
						}
					}]
				},
				"type": "WORDCLOUD",
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"type": "Serie",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "MAJORGRID":{  
			                  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "MINORGRID":{  
			            	  "interval":"",
			                  "style":{
			                	  "typeLine":"",
			                	  "color":""
			                  }
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					},
					{
						"alias": "X",
						"type": "Category",
						"position": "",
						"style":{
			            	   "rotate":"",
			            	   "align":"",
			            	   "color":"",
			            	   "fontFamily":"",
			            	   "fontSize":"",
			            	   "fontWeight":""
			               },
			               "TITLE":{  
				                  "text":"",
				                  "style":{
				                	  "align":"",
				                	  "color":"",
				                	  "fontFamily":"",
				                	  "fontWeight":"",
				                	  "fontSize":""
				                  }
				               }
					}]
				},
				"COLORPALETTE": {
					"COLOR": []
				},
				"height": 100,
				"heightDimType": "percentage",
				"isCockpitEngine": "",
				"maxAngle": 0,
				"maxFontSize": 10,
				"maxWords": 10,
				"minAngle": 0,
				"minFontSize": 5,
				"orientation": "",
				"preventOverlap": false,
				"seriesStacking": false,
				"sizeCriteria": "serie",
				"style":{
              	  "backgroundColor":"#FFFFFF",
              	  "fontFamily":"",
              	  "fontWeight":"",
              	  "fontSize":""
                },
				"styleName": "default",
				"width": 100,
				"widthDimType": "percentage",
				"wordLayout": "horizontal",
				"wordPadding": 2,
				"SUBTITLE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
			      "EMPTYMESSAGE":{  
			    	  "style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
				"LEGEND": {
					"layout": "",
					"position": "",
					"show": false,
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":"",
	                	  "borderWidth":"",
	                	  "backgroundColor":""
	                  },
					"x": 0,
					"y": 0,
					"TITLE":{  
			            "text":"",
			            "style":{
		                	  "align":"",
		                	  "color":"",
		                	  "fontFamily":"",
		                	  "fontWeight":"",
		                	  "fontSize":""
		                  }
			         }
				}
			};

		return wordCloudTemp;
	}
	
	
	
	
	
	this.getChordTemplate = function() {
		var chordTemp = {
				"height": 100,
				"isCockpitEngine": "",
				"orientation": "horizontal",
				"style": {
					"fontFamily": "",
					"fontSize": "",
					"fontWeight": "",
					"backgroundColor": "#FFFFFF"
				},
				"styleName": "default",
				"type": "CHORD",
				"width": 100,
				"heightDimType": "percentage",
				"widthDimType": "percentage",
				"TITLE": {
					"style": {
						"align": "",
						"color": "",
						"fontFamily": "",
						"fontWeight": "",
						"fontSize": ""
					},
					"text": ""
				},
				"VALUES": {
					"CATEGORY": [
						
					],
					"SERIE": [
						{  
				               "axis":"",
				               "color":"",
				               "column":"",
				               "groupingFunction":"SUM",
				               "name":"",
				               "orderType":"",
				               "postfixChar":"",
				               "precision":0,
				               "prefixChar":"",
				               "scaleFactor":"empty",
				               "showAbsValue":false,
				               "showPercentage":false,
				               "showValue":true,
				               "type":"",
				               "TOOLTIP":{  
				                  "backgroundColor":"",
				                  "borderRadius":0,
				                  "borderWidth":0,
				                  "style": {
										"align":"",
										"color":"",
										"fontFamily":"",
										"fontWeight":"",
										"fontSize":""
									}
				               }
				            }
					]
				},
				"AXES_LIST": {
					"AXIS": [{
						"alias": "Y",
						"position": "",
						"style": {
							"rotate": "",
							"align": "",
							"color": "",
							"fontFamily": "",
							"fontWeight": "",
							"fontSize": "",
							"opposite": false
						},
						"type": "Serie",
						"TITLE": {
							"style": {
								"align": "",
								"color": "",
								"fontFamily": "",
								"fontWeight": "",
								"fontSize": ""
							},
							"text": ""
						},
						"MAJORGRID": {
							"interval": "",
							"style": {
								"typeline": "",
								"color": ""
							}
						},
						"MINORGRID": {
							"interval": "",
							"style": {
								"typeline": "",
								"color": ""
							}
						}
					},
					{
						"alias": "X",
						"position": "",
						"style": {
							"rotate": "",
							"align": "",
							"color": "",
							"fontFamily": "",
							"fontWeight": "",
							"fontSize": ""
						},
						"type": "Category",
						"TITLE": {
							"style": {
								"align": "",
								"color": "",
								"fontFamily": "",
								"fontWeight": "",
								"fontSize": ""
							},
							"text": ""
						}
					}],
					"style": {
						"axisColor": "",
						"brushColor": ""
					}
				},
				"COLORPALETTE": {
					"COLOR": [
						
					]
				},
				"LEGEND": {
					"floating": false,
					"layout": "",
					"position": "",
					"show": false,
					"style": {
						"align": "",
						"fontFamily": "",
						"fontSize": "",
						"fontWeight": "",
						"borderWidth": "",
						"color": "",
						"backgroundColor": ""
					},
					"x": 0,
					"y": 0,
					"TITLE": {
						"style": {
							"color": ""
						}
					}
				},
				"SUBTITLE": {
					"style": {
						"align": "",
						"color": "",
						"fontFamily": "",
						"fontWeight": "",
						"fontSize": ""
					},
					"text": ""
				},
				"EMPTYMESSAGE":{  
					"style":{
	                	  "align":"",
	                	  "color":"",
	                	  "fontFamily":"",
	                	  "fontWeight":"",
	                	  "fontSize":""
	                  },
			         "text":""
			      },
				"TOOLTIP": {
					"style": {
						"color": ""
					}
				},
				"TOOLBAR": {
					"style": {
						"percFontColor": ""
					}
				},
				"TIP": {
					"style": {
						"color": ""
					}
				}
			}
		return chordTemp;
	}
	
	this.getSeriesItemTypes = function() {
		
		var seriesItemTypes = 
		[
		 	{name: translate.load('sbi.chartengine.designer.charttype.notype'), value:''},
		 	{name: translate.load('sbi.chartengine.designer.charttype.bar'), value:'bar'},
		 	{name: translate.load('sbi.chartengine.designer.charttype.line'), value:'line'},
		 	{name: translate.load('sbi.chartengine.designer.charttype.area'), value:'area'},
		 	{name: translate.load('sbi.chartengine.designer.charttype.arearangelow'), value:'arearangelow'},
		    {name: translate.load('sbi.chartengine.designer.charttype.arearangehigh'), value:'arearangehigh'},
		];
		
		return seriesItemTypes;
		
	}
    
    // Data for the Series item ordering types combobox
	this.getSeriesItemOrderingTypes = function() {
		
		var seriesItemOrderingTypes = 
		[
		 	{name: translate.load('sbi.chartengine.designer.seriesorder.none'), value:''},
	        {name: translate.load('sbi.chartengine.designer.seriesorder.asc'), value:'asc'}, 
	        {name: translate.load('sbi.chartengine.designer.seriesorder.desc'), value:'desc'}
		];
		
		return seriesItemOrderingTypes;
		
	}
	
	this.getListOfDateFormats = function() {
		
		var listDateFormats = 
		[
		 	{name: translate.load('sbi.chartengine.structure.categoryStyleConfig.minus'), value:'minus'},
		 	{name: translate.load('sbi.chartengine.structure.categoryStyleConfig.slash'), value:'slash'},
		 	{name: translate.load('sbi.chartengine.structure.categoryStyleConfig.year'), value:'year'},
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.month'), value:'month'}, 
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.week'), value:'week'}, 
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.day'), value:'day'}, 
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.hour'), value:'hour'}, 
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.minute'), value:'minute'}, 
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.second'), value:'second'}, 
	        {name: translate.load('sbi.chartengine.structure.categoryStyleConfig.millisecond'), value:'millisecond'}
		];
		
		return listDateFormats;
		
	}
	
	this.getScaleFactorsFixed = function() {
		
		var scaleFactorsFixed = 
		[
	       	{name: "No selection", value: "empty"},
	       	{name: "k (thousands)", value: "k"},
	       	{name: "M (millions)", value: "M"}
        ];
		
		return scaleFactorsFixed;
		
	}
	
	this.getLineTypesOptions = function(){
		var data = [
				{name:sbiModule_translate.load("sbi.chartengine.axisstylepopup.typeline.solid"),value:"solid"},
				{name:sbiModule_translate.load("sbi.chartengine.axisstylepopup.typeline.dashed"),value:"dashed"},
				{name:sbiModule_translate.load("sbi.chartengine.axisstylepopup.typeline.dotted"),value:"dotted"},
			                               ]                          
		return data;
	};
	
	this.getGaugeTicksPosition = function(){
		var data = [
				{name:sbiModule_translate.load("sbi.chartengine.axisstylepopup.mainTickParams.tickPosition.inside"),value:"inside"},
				{name:sbiModule_translate.load("sbi.chartengine.axisstylepopup.mainTickParams.tickPosition.outside"),value:"outside"}
			                               ]                          
		return data;
	};
	
	// Returns templates for specific details for series items on the Structure tab
	this.getSeriesItemsConfDetailsTemplateURL = function(detailsForOption) {
		
		var templatesURLs = "";
		
		switch(detailsForOption) {
			case "seriesItemConfig": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/series_item_config_details.html"; break;
			case "seriesItemTooltip": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/series_item_tooltip_details.html"; break;
			case "axisConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/axis_configuration_details.html"; break;										
			case "axisTitleConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/axis_title_details.html"; break;
			case "axisMajorGridConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/axis_majorgrid_details.html"; break;
			case "axisMinorGridConfiguration": templatesURLs = sbiModule_config.contextName + 
										"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/axis_minorgrid_details.html"; break;
			case "categoriesAxisDetails": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/categories_axis_configuration_details.html"; break;
			
			case "categoriesAxisTitleDetails": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/categories_axis_title_details.html"; break;
			
			case "categoriesOrdering": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/ordering_column.html"; break;
			
			case "categoriesDateTime": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/categories_DateTime.html"; break;
			
			case "additionalParameters": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/additional_parameters.html"; break;
			
			case "mainTick": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/main_tick.html"; break;
			
			case "minorTick": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/minor_tick.html"; break;
			
			case "gaugeLabels": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/labels.html"; break;
			
			case "plotbands": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/plotbands.html"; break;
			
			case "serieTitle": templatesURLs = sbiModule_config.contextName + 
			"/js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/serie_title_details.html"; break;
			
			
		};
		
		return templatesURLs;
		
	}
	
	// Get the name of the Details panel on the Structure tab according to the options that is picked
	this.getStructureTabDetailsName = function(detailsForOption) {
		
		var detailsNameToReturn = "";
		
		switch(detailsForOption) {
			case "seriesItemConfig": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.seriesdetails.toolbar.title"); break;
			case "seriesItemTooltip": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.seriestooltipdetails.toolbar.title"); break;
			case "axisConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.configuration.toolbar.title"); break;
			case "axisTitleConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.title.toolbar.title"); break;
			case "axisMajorGridConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.majorgrid.toolbar.title"); break;
			case "axisMinorGridConfiguration": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.minorgrid.toolbar.title"); break;
			case "categoriesAxisDetails": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.configuration.toolbar.title"); break;
			case "categoriesAxisTitleDetails": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.axis.title.toolbar.title"); break;
			case "categoriesOrdering": detailsNameToReturn = translate.load("sbi.chartengine.structure.categoryStyleConfig.title"); break;
			case "categoriesDateTime": detailsNameToReturn = translate.load("sbi.chartengine.structure.categoryStyleConfig.dateTimeAndGrouping"); break;
			case "serieTitle": detailsNameToReturn = translate.load("sbi.chartengine.designer.structureTab.serie.serieTitle"); break;
			default : detailsNameToReturn = translate.load("Gauge axis additional options"); break;
		}
		
		return detailsNameToReturn;
		
	}
	
	// The types of aggregation for series items
	this.getSeriesItemAggregationTypes = function() {
		 
		var seriesItemAggregationTypes = 
		[
		 	{name:'NONE',value:'NONE'},
			{name:'AVG',value:'AVG'},
			{name:'COUNT',value:'COUNT'},
			{name:'COUNT DISTINCT',value:'COUNT_DISTINCT'},
			{name:'MAX',value:'MAX'},
			{name:'MIN',value:'MIN'},
			{name:'SUM',value:'SUM'}
		];
		 
		return seriesItemAggregationTypes;
		 
	}
	
})	

.service("PreviewService", function(sbiModule_restServices,chartInitializerRetriver,sbiModule_messaging,sbiModule_translate,sbiModule_config, $http, $q, $timeout){
	this.run = function(temp) {
		
		var deferred = $q.defer();

		var chartTemp = {"CHART": temp}
		if( (chartTemp.CHART.groupCategories || chartTemp.CHART.groupSeries || chartTemp.CHART.groupSeriesCateg) && chartTemp.CHART.VALUES.CATEGORY.groupby!=""){
			var arrayOfCateg = [];
			arrayOfCateg.push(chartTemp.CHART.VALUES.CATEGORY)
			 if (chartTemp.CHART.VALUES.CATEGORY.groupby.indexOf(',') == -1) { 
					subs = chartTemp.CHART.VALUES.CATEGORY.groupby ;
				}
				
				else {
					subs = angular.copy(chartTemp.CHART.VALUES.CATEGORY.groupby.substring(0, chartTemp.CHART.VALUES.CATEGORY.groupby.indexOf(',')));
				}
			var groupby = {};
			groupby['column'] = subs;
			groupby['groupby'] = "";
			groupby['name'] = subs;
			groupby['groupbyNames'] = "";
			groupby['orderColumn'] = chartTemp.CHART.VALUES.CATEGORY.orderColumn;
			groupby['orderType'] =chartTemp.CHART.VALUES.CATEGORY.orderType;;
			groupby['stackedType'] = chartTemp.CHART.VALUES.CATEGORY.stackedType;
			groupby['stacked'] =  chartTemp.CHART.VALUES.CATEGORY.stacked;
			arrayOfCateg.push(groupby);
			delete chartTemp.CHART.VALUES.CATEGORY;
			 chartTemp.CHART.VALUES.CATEGORY = arrayOfCateg;
		}
		
		var configParams = { 
    			headers: {
    				'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'
				}, 
    			transformResponse: function(obj) {	
					// Replace ASCII value of the potential single quotes from the template (e.g. from a TITLE) with the a proper escape combination.
						obj = obj.replace(new RegExp("&#39;",'g'),"\\'");
						return obj;
				}
			};
		
		
		
		sbiModule_restServices.promisePost('../api/1.0/chart/jsonChartTemplate/readChartTemplate','', 'jsonTemplate='+angular.toJson(chartTemp)+'&exportWebApp=true', configParams)
		.then(function(response) {
			
			var chartConf = eval("(" + response.data + ")");
			
			var chartType = chartConf.chart.type;
			var d3Types = ["sunburst","parallel","wordcloud","chord"];
			var highSpec = ["heatmap", "treemap"]
			var lib = "d3js244";
			if(d3Types.indexOf(chartType.toLowerCase())>=0){
				lib = "d3js244"
			} else {
				lib="highcharts414"
			}
			var encoded = {};
			var prepareDataForRequest = function (){
				encoded =  btoa(document.getElementById('forSVGPreview').innerHTML);
				
				var parameters = {
						type:'image/png',
						scale: undefined,
						constr:'Chart',
						callback: undefined,
						async: 'true'
					};
					if(lib=="d3js244"){
						parameters.options = encoded;
						parameters.content = 'html';
					} else {
						parameters.options = JSON.stringify(chartConf);
						parameters.content = 'options';
					}
					
						
					
					$http({
					    method: 'POST',
					    url: sbiHost + '/highcharts-export-web/',
					    headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'},
					    transformRequest: function(obj) {
					        var str = [];
					        for(var p in obj)
					        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
					        return str.join("&");
					    },
					    data: parameters
					}).then(function successExportPng(response) {
						deferred.resolve(response);
					},function errorExportPng(response) {
						deferred.reject(response);
					});
					
			}
			if(d3Types.indexOf(chartType.toLowerCase())>=0 || highSpec.indexOf(chartType.toLowerCase())>=0){
				var chartInitializer = chartInitializerRetriver.getChartInitializer(lib);
				if(lib=="highcharts414"){

					chartConf = chartInitializer.renderChart(chartConf,null, null, true);	
					prepareDataForRequest();	
				} else {
					document.getElementById("forSVGPreview").style.height = "500px";
					document.getElementById("forSVGPreview").style.width = "500px";
					chartInitializer.renderChart(chartConf,document.getElementById('forSVGPreview'), null, null, true);	

					document.getElementById("forSVGPreview").style.height = "0px";
					document.getElementById("forSVGPreview").style.width = "0px";
					if(chartType.toLowerCase()=='wordcloud'){
						$timeout(function(){								
							prepareDataForRequest();
							
						}, 3000);

					} else {
						prepareDataForRequest();
					}
					
				}
			} else {
				prepareDataForRequest();
			}
				
			
			
		
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
		return deferred.promise;
	}
});