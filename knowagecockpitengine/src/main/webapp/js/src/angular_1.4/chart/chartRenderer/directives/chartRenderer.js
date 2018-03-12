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

.directive('chartRenderer',function(chartInitializerRetriver,jsonChartTemplate,highchartsDrilldownHelper,sbiModule_config, sbiModule_i18n, ChartUpdateService){

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

				scope.renderChart = function(chartConf, jsonData){
					var locale = sbiModule_config.curr_language + "-" + sbiModule_config.curr_country;
					if(scope.chartConf){
						scope.chartInitializer.initChartLibrary(element[0],	'drillup', sbiModule_config.dec, sbiModule_config.thous);
						if(scope.chartTemplate.CHART.COLORPALETTE.COLORCopy){
							scope.chartTemplate.CHART.COLORPALETTE.COLOR = angular.copy(scope.chartTemplate.CHART.COLORPALETTE.COLORCopy)
							delete scope.chartTemplate.CHART.COLORPALETTE.COLORCopy
						}
						var renderObject = {};
						renderObject.chartConf = scope.chartConf;
						renderObject.element = element[0];
						renderObject.handleCockpitSelection = handleCockpitSelection;
						renderObject.locale = locale;
						renderObject.widgetData = scope.widgetData;

						scope.chartInitializer.renderChart(renderObject, jsonData);
					}
				}

				scope.loadChart = function(chartTemplate,datesetLabel,jsonData,isRealtime,nature,dataAndChartConf){
						if(scope.widgetData){
							if(isRealtime && nature){
								jsonChartTemplate.readChartTemplateForCockpit(chartTemplate,false,jsonData)
								.then(function(data){
									scope.chartConf = eval("(" + data + ")");
									scope.renderChart(scope.chartConf, jsonData);
								})
							}
							else {
								scope.chartConf = eval("(" + dataAndChartConf.chartConf + ")");
								scope.renderChart(scope.chartConf, jsonData);
							}

						}else{
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

			scope.$on('refresh',function(event,data,isRealtime,changedChartType,chartConf){
				if(scope.updateble){
					var dataForSending = isRealtime ? data : eval("(" + data.jsonData + ")");
					if(scope.chartInitializer != undefined && scope.chartInitializer.updateData){
						scope.updateChart(scope.widgetData,dataForSending);
					}else{
						var transformedData = dataForSending;
						if(isRealtime){
							if(scope.chartInitializer.transformeData){
								transformedData = scope.chartInitializer.transformeData(scope.widgetData,dataForSending);
							}
						}
						scope.loadChart(scope.chartTemplate,scope.datasetLabel,transformedData,isRealtime, true,chartConf);
					}
				}
			})

			scope.$on('changeChartType',function(){
				scope.chartTemplate =  ChartUpdateService.getTemplate( scope.chartTemplate);
				scope.$emit('changedChartType',scope.chartTemplate);
			})

			scope.$on('init',function(event,data, isRealtime,changedChartType,chartConf){

				var lib = getChartExecutionLib(scope.chartTemplate);
				if(lib){
					scope.noLib = false;
					scope.chartInitializer = chartInitializerRetriver.getChartInitializer(lib);
					/*var template = scope.chartTemplate;
					if(changedChartType){
						template = ChartUpdateService.getTemplate(template);
					}*/
					scope.loadChart(scope.chartTemplate,scope.datasetLabel,data,isRealtime,false,chartConf);

				}else{
					element[0].innerHTML = "no library implementation";
				}


			})

			scope.$on('filters',function(event,data,isRealtime,changedChartType,chartConf){


				scope.loadChart(scope.chartTemplate,scope.datasetLabel,data,isRealtime, true,chartConf);

			})

			scope.$on('selections',function(event,data,isRealtime,changedChartType,chartConf){


				scope.loadChart(scope.chartTemplate,scope.datasetLabel,data,isRealtime, true,chartConf);

			})

			scope.$on('resize',function(event,data){


				scope.renderChart(scope.chartConf);

			})

			scope.$on('fullExpand',function(event,data){


				scope.renderChart(scope.chartConf);

			})

			scope.$on('gridster-resized',function(event,data){


				scope.renderChart(scope.chartConf);

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

			}



		}
	}

})