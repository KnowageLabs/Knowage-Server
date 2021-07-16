/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
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
angular.module('chartRendererModule')

.directive('chartRenderer',function(chartInitializerRetriver,jsonChartTemplate,highchartsDrilldownHelper,sbiModule_config, sbiModule_i18n){

	return{
		restrict:'E',
		template:'<div ></div>',
		scope:{

			chartLibNamesConfig:'=',
			jsonData:'=',
			chartTemplate:'=',
			datasetLabel:'=',
			widgetData:'=',
			updateble:'=',
			drillable:'=',
			lib:'=',
			onClickSeries:'='
		},

		link:function(scope,element){


				var handleCockpitSelection = scope.onClickSeries;

				//var handleDrilldown = function (e) {};

				//var handleDrillup = function () {};

				var getChartExecutionLib = function(chartTemplate){

					var jsonTemp = angular.fromJson(chartTemplate);
					if(jsonTemp!=undefined){
						if(!jsonTemp.hasOwnProperty("CHART")){
							jsonTemp = {"CHART":jsonTemp};
						}
						var chartType = jsonTemp.CHART.type.toLowerCase();
						return scope.chartLibNamesConfig[chartType];
					}

				}

				scope.chartConf;
				scope.chartTemplate;
				scope.chartInitializer;
				scope.chartSonifyService;

				scope.renderChart = function(chartConf, jsonData,selectionsAndParams){
					var script = sbiModule_config.curr_script;
					var locale = sbiModule_config.curr_language + "-";
					if(script && script!=""){
						locale += script + "-";
					}
					locale += sbiModule_config.curr_country;
					if(scope.chartConf){
						scope.chartInitializer.initChartLibrary(element[0],	'drillup', sbiModule_config.dec, sbiModule_config.thous);
						if(scope.chartTemplate.CHART.COLORPALETTE.COLORCopy){
							scope.chartTemplate.CHART.COLORPALETTE.COLOR = angular.copy(scope.chartTemplate.CHART.COLORPALETTE.COLORCopy)
							delete scope.chartTemplate.CHART.COLORPALETTE.COLORCopy
						}

						if(scope.chartTemplate.CHART.groupSeriesCateg && scope.chartConf.series && scope.chartConf.series.length > 0){
							scope.chartConf.colorsCopy = angular.copy(scope.chartConf.colors);
							if(scope.colorMap){
								if(Object.keys(scope.colorMap).length<=scope.chartConf.series.length){
									scope.colorMap = {};
									for (var i = 0; i < scope.chartConf.series.length; i++) {
										if(scope.chartConf.colors[i]){
											scope.colorMap[scope.chartConf.series[i].name] = scope.chartConf.colors[i];
										} else {
											//scope.chartConf.series.length%10-1
											scope.colorMap[scope.chartConf.series[i].name] = scope.chartConf.colors[i%chartConf.colors.length]
										}
									}
								}
								scope.chartConf.colors = angular.copy(scope.chartConf.colorsCopy);
								for (var i = 0; i < scope.chartConf.series.length; i++) {
									if(scope.colorMap.hasOwnProperty(scope.chartConf.series[i].name)) {
										scope.chartConf.colors[i]=scope.colorMap[chartConf.series[i].name];
									}
								}
							} else {
								scope.colorMap = {};
								for (var i = 0; i < scope.chartConf.series.length; i++) {
									if(scope.chartConf.colors[i]){
										scope.colorMap[scope.chartConf.series[i].name] = scope.chartConf.colors[i];
									} else {
										scope.colorMap[scope.chartConf.series[i].name] = scope.chartConf.colors[i%chartConf.colors.length]
									}
								}
							}

						}

						var renderObject = {};
						renderObject.chartConf = scope.chartConf;
						renderObject.element = element[0];
						renderObject.handleCockpitSelection = handleCockpitSelection;
						renderObject.locale = locale;
						renderObject.widgetData = scope.widgetData;
						renderObject.chartTemplate = scope.chartTemplate.CHART;
						renderObject.chartSonifyService = scope.chartSonifyService;
						if(selectionsAndParams){
							renderObject.selectionsAndParams = selectionsAndParams;
						}

						if(chartConf.plotOptions && chartConf.series && chartConf.series[0] && chartConf.series[0].data && chartConf.series[0].data.length > chartConf.plotOptions.series.turboThreshold){
							chartConf.lang.noData = "Your dataset is returning too much data"
						}
						scope.chartInitializer.renderChart(renderObject, jsonData);
					}
				}

				scope.loadChart = function(chartTemplate,datesetLabel,jsonData,isRealtime,nature,dataAndChartConf,selectionsAndParams){
						if(scope.widgetData){
							if(isRealtime && nature){
								jsonChartTemplate.readChartTemplateForCockpit(chartTemplate,false,jsonData)
								.then(function(data){
									scope.chartConf = eval("(" + data + ")");
									scope.renderChart(scope.chartConf, jsonData,selectionsAndParams);
								})
							}
							else {
								if(!scope.initializer) scope.init({ data:jsonData,isRealtime:isRealtime,chartConf:dataAndChartConf.chartConf,selectionsAndParams:selectionsAndParams },true);
								scope.chartConf = eval("(" + dataAndChartConf.chartConf + ")");
								scope.renderChart(scope.chartConf, jsonData,selectionsAndParams);
							}

						}else{
							if(chartTemplate.CHART.type == "SCATTER" || chartTemplate.CHART.type == "BUBBLE" || chartTemplate.CHART.type == "BAR" || chartTemplate.CHART.type == "LINE"){
						    	  for (var i = 0; i < chartTemplate.CHART.VALUES.SERIE.length; i++) {
						    		  for (var j = 0; j < chartTemplate.CHART.AXES_LIST.AXIS.length; j++) {
											if(chartTemplate.CHART.VALUES.SERIE[i].axis == chartTemplate.CHART.AXES_LIST.AXIS[j].alias && chartTemplate.CHART.AXES_LIST.AXIS[j].LABELS){
												chartTemplate.CHART.VALUES.SERIE[i].scaleFactor = chartTemplate.CHART.AXES_LIST.AXIS[j].LABELS.scaleFactor
											}
							    	  }
						    	  }
							}
							jsonChartTemplate.readChartTemplate(chartTemplate,false,datesetLabel,jsonData)
							.then(function(data){
								scope.chartConf = eval("(" + data + ")");
								scope.renderChart(scope.chartConf, jsonData);
							});
						}
				}

				scope.updateChart = function(widgetData,data){
					var updateWidgetData = angular.copy(widgetData);
					updateWidgetData.jsonData = data;
					scope.chartInitializer.updateData(updateWidgetData);

				}

			scope.$on('refresh',function(event,data,isRealtime,changedChartType,chartConf,selectionsAndParams, shouldUpdate){
				if(scope.updateble){
					var dataForSending = isRealtime ? data : eval("(" + data.jsonData + ")");
					if(scope.chartInitializer != undefined && scope.chartInitializer.updateData && shouldUpdate!=undefined && !shouldUpdate){
						scope.updateChart(scope.widgetData,dataForSending);
					}else{
						var transformedData = dataForSending;
						if(isRealtime){
							if(scope.chartInitializer && scope.chartInitializer.transformeData){
								transformedData = scope.chartInitializer.transformeData(scope.widgetData,dataForSending);
							}
						}
						scope.loadChart(scope.chartTemplate,scope.datasetLabel,transformedData,isRealtime, true,chartConf,selectionsAndParams);
					}
				}
			})

			scope.$on('changeChartType',function(event,data){

				if(!data.isOriginal){
					var ChartUpdateService = chartInitializerRetriver.getChartInitializer("ChartUpdateService");
					scope.chartTemplate =  ChartUpdateService.getTemplate( scope.chartTemplate);
				}

				scope.$emit('changedChartType',scope.chartTemplate);
			})

			scope.$on('init',function(event,data, isRealtime,changedChartType,chartConf,selectionsAndParams){
				var initObject = { data:data,isRealtime:isRealtime,chartConf:chartConf,selectionsAndParams:selectionsAndParams }
				scope.init(initObject)
			})
				
			scope.init = function(initObject, stopChartLoad){

				var lib = getChartExecutionLib(scope.chartTemplate);
				if(lib){
					scope.noLib = false;
					scope.chartInitializer = chartInitializerRetriver.getChartInitializer(lib);
					scope.chartSonifyService = chartInitializerRetriver.getChartInitializer("chartSonifyService");
					if(!stopChartLoad) scope.loadChart(scope.chartTemplate,scope.datasetLabel,initObject.data,initObject.isRealtime,false,initObject.chartConf,initObject.selectionsAndParams);

				}else{
					element[0].innerHTML = "no library implementation";
				}

			}

			scope.$on('filters',function(event,data,isRealtime,changedChartType,chartConf,selectionsAndParams){

				if(!scope.chartInitializer){
				var initObject = { data:data,isRealtime:isRealtime,chartConf:chartConf,selectionsAndParams:selectionsAndParams }
				scope.init(initObject)
					}else{
				scope.loadChart(scope.chartTemplate,scope.datasetLabel,data,isRealtime,true,chartConf,selectionsAndParams);
				}

			})

			scope.$on('selections',function(event,data,isRealtime,changedChartType,chartConf,selectionsAndParams){


				scope.loadChart(scope.chartTemplate,scope.datasetLabel,data,isRealtime, true,chartConf,selectionsAndParams);

			})

			scope.$on('resize',function(event,data,isRealtime,changedChartType,chartConf,selectionsAndParams){


				scope.renderChart(scope.chartConf,data,selectionsAndParams);

			})

			scope.$on('fullExpand',function(event,data,isRealtime,changedChartType,chartConf,selectionsAndParams){

				if((scope.chartConf.chart.type == "bar" || scope.chartConf.chart.type == "column" ||
				   scope.chartConf.chart.type == "line" || scope.chartConf.chart.type == "radar" ||
				   scope.chartConf.chart.type == "scatter")  && scope.chartConf.series.length > 0){
					for( var i=0 ; i < scope.chartConf.series.length ; i++ ){

						if(scope.chartConf.series[i].data[0].dataLabels){
							scope.chartConf.series[i].selected = scope.chartConf.series[i].data[0].dataLabels.enabled;
						}

					}
				}

				scope.renderChart(scope.chartConf,data,selectionsAndParams);


			})

			scope.$on('gridster-resized',function(event,data,isRealtime,changedChartType,chartConf,selectionsAndParams){


				scope.renderChart(scope.chartConf,data,selectionsAndParams);

			})

			scope.$on('drillClick',function(event,data){

				scope.chartInitializer.chart.drillable = data.drillable;
				scope.chartInitializer.chart.cliccable = data.cliccable;
			})

			if(!scope.widgetData){
				var lib = getChartExecutionLib(scope.chartTemplate);
				if(lib){

					// when sbiModule_i18n is initialized (i.e. i18n messages are loaded), the chart initialization can start
					sbiModule_i18n.loadI18nMap().then(function() {
						scope.chartInitializer = chartInitializerRetriver.getChartInitializer(lib);
						scope.loadChart(scope.chartTemplate,scope.datasetLabel,undefined);
					});

				}else{
					element[0].innerHTML = "no library implementation";
				}

			};

			scope.$on('playSonify',function(event,data){

				scope.chartSonifyService.playSonify()

			})
			scope.$on('pauseSonify',function(event,data){

				scope.chartSonifyService.pauseSonify()

			})
			scope.$on('rewindSonify',function(event,data){

				scope.chartSonifyService.rewindSonify()

			});
			scope.$on('cancelSonify',function(event,data){

				scope.chartSonifyService.cancelSonify()
			});


		}
	}

})
