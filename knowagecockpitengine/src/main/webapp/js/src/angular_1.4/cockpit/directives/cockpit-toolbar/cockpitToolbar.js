/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

(function() {
var cockpitToolbarPath = '/angular_1.4/cockpit';
angular.module('cockpitModule')
.directive('cockpitToolbar',function(sbiModule_config){
	return{
		templateUrl: sbiModule_config.dynamicResourcesEnginePath+ cockpitToolbarPath + '/directives/cockpit-toolbar/templates/cockpitToolbar.html',
		controller: cockpitToolbarControllerFunction,
		scope: {
				config: '='
			},
		compile: function (tElement, tAttrs, transclude) {
			return {
				pre: function preLink(scope, element, attrs, ctrl, transclud) {
				},
				post: function postLink(scope, element, attrs, ctrl, transclud) {
				}
			};
		}
	}
});

function cockpitToolbarControllerFunction($scope,$timeout,$q,$location,windowCommunicationService,cockpitModule_datasetServices,cockpitModule_analyticalDrivers,cockpitModule_widgetServices,cockpitModule_templateServices,cockpitModule_properties,cockpitModule_template,$mdDialog,sbiModule_translate,sbiModule_restServices,sbiModule_messaging,sbiModule_download,sbiModule_user,sbiModule_cockpitDocument,sbiModule_config,cockpitModule_gridsterOptions,$mdPanel,cockpitModule_widgetConfigurator,$mdToast,cockpitModule_generalServices,cockpitModule_widgetSelection,$rootScope){
	$scope.translate = sbiModule_translate;
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.cockpitModule_widgetServices=cockpitModule_widgetServices;

	if ($location.search()['scheduledexport'])
		$scope.isScheduledExcelExport = true;
	else
		$scope.isScheduledExcelExport = false;

	$scope.openGeneralConfigurationDialog=function(){
		cockpitModule_generalServices.openGeneralConfiguration();
	}

	$scope.openDataConfigurationDialog=function(){
		cockpitModule_generalServices.openDataConfiguration();
	}

	$scope.fabSpeed = {
			isOpen : false
	}


	$scope.saveCockpit=function(event){
		var haveSel=false;
		for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
			if(Object.keys(cockpitModule_template.configuration.aggregations[i].selection).length>0){
				haveSel=true;
				break;
			}
		}
		if(Object.keys(cockpitModule_template.configuration.filters).length>0){
			haveSel=true;
		}
		if(haveSel){
			var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load('sbi.cockpit.widgets.save.keepselections'))
			.textContent('')
			.ariaLabel('save cockpit')
			.ok(sbiModule_translate.load('sbi.qbe.messagewin.yes'))
			.cancel(sbiModule_translate.load('sbi.qbe.messagewin.no'));
			$mdDialog.show(confirm).then(function() {
				cockpitModule_generalServices.saveCockpit(event);
			}, function() {
				for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
					cockpitModule_template.configuration.aggregations[i].selection = {};
				}
				cockpitModule_template.configuration.filters={};
				cockpitModule_generalServices.saveCockpit(event);
			});
		}else{
			cockpitModule_generalServices.saveCockpit(event);
		}
	};

	$scope.cleanCache = function(){
		cockpitModule_generalServices.cleanCache();
	}

	$scope.openSelections = function(){
		$mdDialog.show({
			  templateUrl: sbiModule_config.dynamicResourcesEnginePath + cockpitToolbarPath + '/directives/cockpit-toolbar/templates/selectionsList.html',
			  parent: angular.element(document.body),
			  clickOutsideToClose:true,
			  escapeToClose :true,
			  preserveScope: true,
			  fullscreen: true,
			  controller: cockpitSelectionControllerFunction

		});
	}

	$scope.addWidget=function(){
		$mdDialog.show({
			  templateUrl: sbiModule_config.dynamicResourcesEnginePath+ cockpitToolbarPath + '/directives/cockpit-toolbar/templates/addWidget.html',
			  parent: angular.element(document.body),
			  clickOutsideToClose:true,
			  escapeToClose :true,
			  preserveScope: true,
			  fullscreen: true,
			  controller: function($scope,sbiModule_translate,cockpitModule_template,cockpitModule_properties,cockpitModule_widgetServices){
				$scope.widgetSpinner = true;
				  $scope.translate=sbiModule_translate;
				  $scope.addWidget=function(type){
					  var tmpWidget={
							  id:(new Date()).getTime(),
							  sizeX	:6,
							  sizeY:6,
							  //content:{name:"new "+type+" Widget"},
							  type:type,
							  isNew : true,
							  updateble : true,
							  cliccable : true
					  }
					  tmpWidget.content = {name:'widget_'+ type + '_' + tmpWidget.id};
					  if(cockpitModule_widgetConfigurator[type].initialDimension !=undefined){
						  if(cockpitModule_widgetConfigurator[type].initialDimension.width != undefined){
							  tmpWidget.sizeX = cockpitModule_widgetConfigurator[type].initialDimension.width;

						  }
						  if(cockpitModule_widgetConfigurator[type].initialDimension.height != undefined){
							  tmpWidget.sizeY = cockpitModule_widgetConfigurator[type].initialDimension.height;

						  }
					  }
					  cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,tmpWidget);
					  $mdDialog.hide();

				  };

				  $scope.showWidgetType = function(w){
					  if(w.datasetRequirement){
						  var datasetList = cockpitModule_datasetServices.getAvaiableDatasets();
						  for(var k in datasetList){
							  if(datasetList[k].type == w.datasetRequirement) return true;
						  }
						  return false
					  }else return true;
				  }

				$scope.widgetType=[];

				$scope.loadWidgetTypes = function() {
					sbiModule_restServices.get("1.0/engine", 'widget')
						.then(function(response) {
							var data = response.data;
							for (var i in data) {
								var currData = data[i];
								currData.description = $scope.translate.load(currData.descKey);
							}
							$scope.widgetType = data;
							$scope.widgetSpinner = false;
						},function(error) {
							$mdToast.show($mdToast.simple().content(error.data.ERROR).position('top').action(
							'OK').highlightAction(false).hideDelay(5000));
							$scope.widgetSpinner = false;
						});


				}

				  $scope.saveConfiguration=function(){
					  $mdDialog.hide();
				  }
				  $scope.cancelConfiguration=function(){
					  $mdDialog.cancel();
				  }

				$scope.loadWidgetTypes();
			  }
			})
	};

	$scope.closeNewCockpit=function(){
		cockpitModule_generalServices.closeNewCockpit();
	}
	$scope.isFromNewCockpit= cockpitModule_generalServices.isFromNewCockpit();

	var handler = {};
	handler.handleMessage = function(message){
		if(message == 'pdfExport') $scope.exportPdf().then(function(){},
				function(error){
					$mdDialog.show(
					  $mdDialog.alert()
						.parent(angular.element(document.body))
						.clickOutsideToClose(true)
						.title('Error during export')
						.textContent(error)
						.ok('Close')
					);
		});
		if(message == 'xlsExport' || message == 'xlsxExport') {
			$scope.exportExcel(message).then(function(){},
					function(error){
						$mdDialog.show(
						  $mdDialog.alert()
							.parent(angular.element(document.body))
							.clickOutsideToClose(true)
							.title('Error during export')
							.textContent(error)
							.ok('Close')
						);
					});
		}
	}

	windowCommunicationService.addMessageHandler(handler);

	$scope.exportExcel = function(message){

		$scope.excelType = message == 'xlsExport' ? 'xls' : 'xlsx';

		return $q(function(resolve, reject) {

			$mdDialog.show({
				controller: function($scope,cockpitModule_properties,cockpitModule_template, sbiModule_translate){
					$scope.translate = sbiModule_translate;
					$scope.cockpitModule_properties = cockpitModule_properties;
					$scope.cockpitModule_template = cockpitModule_template;
				 },
				 templateUrl: sbiModule_config.dynamicResourcesEnginePath+ cockpitToolbarPath+ '/directives/cockpit-toolbar/templates/exportExcelDialogTemplate.html',
				 parent: angular.element(document.body),
				 hasBackdrop: false,
				 clickOutsideToClose:false
			})

			var abortTimeout;
			function resetTimeout(){
				if(abortTimeout) clearTimeout(abortTimeout);
				abortTimeout = setTimeout(function(){
					$mdDialog.hide();
					$scope.$destroy();
	 				reject($scope.translate.load('kn.error.export.timeout'));
				},30000);
			}

			var cockpitWidgets = cockpitModule_templateServices.getAllCockpitWidgets();
			var requestUrl = {
					user_id: sbiModule_user.userUniqueIdentifier,
					outputType: $scope.excelType,
					document: sbiModule_cockpitDocument.docId,
					widget: [],
					DOCUMENT_LABEL: sbiModule_cockpitDocument.docLabel,
					SBI_COUNTRY: sbiModule_config.curr_country,
					SBI_LANGUAGE: sbiModule_config.curr_language,
					COCKPIT_SELECTIONS: [],
					COCKPIT_VARIABLES: []
			}

			var drivers = formatDrivers(cockpitModule_analyticalDrivers);
			for(i=0; i<cockpitWidgets.length; i++) {
				var widget = cockpitWidgets[i];
				requestUrl.widget[i] = widget;
				if (!angular.equals(cockpitModule_properties.VARIABLES,{})) {
					for (var k in widget.content.columnSelectedOfDataset) {
						if(Array.isArray(widget.content.columnSelectedOfDataset[k].variables) && widget.content.columnSelectedOfDataset[k].variables.length) {
							if (widget.type == "table" && widget.content.columnSelectedOfDataset[k].variables[0].action == 'header') {
								for (var j in cockpitModule_properties.VARIABLES) {
									if (j == widget.content.columnSelectedOfDataset[k].variables[0].variable){
										widget.content.columnSelectedOfDataset[k].aliasToShow = cockpitModule_properties.VARIABLES[j];
									}
								}
							}

						}
					}
				}

				requestUrl.COCKPIT_SELECTIONS[i] = {};
				if (widget.type == "map") {
					requestUrl.COCKPIT_SELECTIONS[i] = [];
					var allDsId = [];
					if(widget.datasetId) allDsId = widget.datasetId;
					else allDsId.push(widget.dataset.dsId);
					for (var k=0; k<allDsId.length; k++) {
						var dsId = allDsId[k];
						var dataset = cockpitModule_datasetServices.getDatasetById(dsId);
						var aggregation;
						if (widget.settings) {
							aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset, widget.settings.sortingColumn,widget.settings.sortingOrder);
						}
						else {
							aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset)
						}
						var loadDomainValues = widget.type == "selector" ? true : false;
						var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters(widget, dataset, loadDomainValues);
						var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
						var parametersString = cockpitModule_datasetServices.getParametersAsString(parameters);
						var paramsToSend = angular.fromJson(parametersString);
						requestUrl.COCKPIT_SELECTIONS[i][k] = {};
						requestUrl.COCKPIT_SELECTIONS[i][k].aggregations = aggregation;
						requestUrl.COCKPIT_SELECTIONS[i][k].parameters = paramsToSend;
						requestUrl.COCKPIT_SELECTIONS[i][k].drivers = drivers;
						requestUrl.COCKPIT_SELECTIONS[i][k].selections = selections;
					}
					requestUrl.COCKPIT_VARIABLES[i] = cockpitModule_properties.VARIABLES;
				}
				else if (widget.dataset && Object.keys(widget.dataset).length != 0) {
					var dsId = widget.dataset.dsId
					var dataset = cockpitModule_datasetServices.getDatasetById(dsId);
					var aggregation;
					if (widget.settings) {
						aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset, widget.settings.sortingColumn,widget.settings.sortingOrder);
					}
					else {
						aggregation = cockpitModule_widgetSelection.getAggregation(widget, dataset)
					}
					var loadDomainValues = widget.type == "selector" ? true : false;
					var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters(widget, dataset, loadDomainValues);
					var parameters = cockpitModule_datasetServices.getDatasetParameters(dsId);
					var parametersString = cockpitModule_datasetServices.getParametersAsString(parameters);
					var paramsToSend = angular.fromJson(parametersString);
					requestUrl.COCKPIT_SELECTIONS[i].aggregations = aggregation;
					requestUrl.COCKPIT_SELECTIONS[i].parameters = paramsToSend;
					requestUrl.COCKPIT_SELECTIONS[i].drivers = drivers;
					requestUrl.COCKPIT_SELECTIONS[i].selections = selections;
					requestUrl.COCKPIT_VARIABLES[i] = cockpitModule_properties.VARIABLES;
				}
			}

			var config = {"responseType": "arraybuffer"};
			var exportingToast = sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting"), 'Success!', 0);
			var documentLabel = requestUrl.DOCUMENT_LABEL;
			sbiModule_restServices.promisePost('1.0/cockpit/export', 'excel', requestUrl, config)
			.then(function(response){
				$mdDialog.hide();
				var mimeType = response.headers("Content-type");
				var fileName = 'exported_widget';
				if (documentLabel != undefined) {
					fileName = documentLabel;
				}
				$mdToast.hide(exportingToast);
				sbiModule_download.getBlob(response.data, fileName, mimeType, $scope.excelType);
			}, function(error){
				$mdDialog.hide();
				$mdToast.cancel(exportingToast);
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.cockpit.widgets.exporting.error"), 'Error');
			});
		})
	}

	var formatDrivers = function (analyticalDrivers) {
		var toReturn = {};
		for (var key in analyticalDrivers) {
			if (key.endsWith("_description")) continue;
			var key_description = key + "_description";
			toReturn[key] = [{'value': analyticalDrivers[key], 'description': analyticalDrivers[key_description]}];
			var foo = 0;
		}
		return toReturn;
	}

	$scope.exportPdf = function(){

		return $q(function(resolve, reject) {
			var isIE11 = window.document.documentMode;

			$mdDialog.show({
				controller: function($scope,cockpitModule_properties,cockpitModule_template, sbiModule_translate){
					$scope.translate = sbiModule_translate;
					$scope.cockpitModule_properties = cockpitModule_properties;
					$scope.cockpitModule_template = cockpitModule_template;
				 },
				 templateUrl: sbiModule_config.dynamicResourcesEnginePath+ cockpitToolbarPath+ '/directives/cockpit-toolbar/templates/exportPdfDialogTemplate.html',
				 parent: angular.element(document.body),
				 hasBackdrop: false,
				 clickOutsideToClose:false
				 })

			cockpitModule_properties.LOADING_SCREENSHOT = true;

			var abortTimeout;
			function resetTimeout(){
				if(abortTimeout) clearTimeout(abortTimeout);
				abortTimeout = setTimeout(function(){
					$mdDialog.hide();
					$scope.$destroy();
	 				cockpitModule_properties.LOADING_SCREENSHOT = false;
	 				reject($scope.translate.load('kn.error.export.timeout'));
				},30000);
			}


				 function closeOrContinue(sheet){
					if(sheet.index + 1 == cockpitModule_template.sheets.length) {
		 				doc.save(cockpitModule_properties.DOCUMENT_LABEL+'.pdf');
		 				$mdDialog.hide();
		 				cockpitModule_properties.LOADING_SCREENSHOT = false;
		 				resolve();
		 			}
		 			else {
		 				document.querySelector(".sheetPageButton-"+(sheet.index+1)).parentNode.click();
		 				for(var y in cockpitModule_template.sheets){
		 					if(cockpitModule_template.sheets[y].index == sheet.index + 1){
		 						$timeout(function(){
			 						getScreenshot(cockpitModule_template.sheets[y]);
		 						},300);
		 						break;
		 					}
		 				}

		 			}
					var divsToClean = document.getElementsByClassName('show-for-canvas');
 					for(var k in divsToClean){
 						if(divsToClean[k].classList && divsToClean[k].classList.contains('show-for-canvas')) divsToClean[k].classList.remove('show-for-canvas');
 					}
				}

				 function getScreenshot(sheet){
				 $scope.sheetsWidgets = cockpitModule_properties.INITIALIZED_WIDGETS;
				 $scope.enterpriseEdition = (sbiModule_user.functionalities.indexOf("EnableButtons")>-1)? true:false;
				 var d3Charts = ["wordcloud","parallel","sunburst","chord"];

				 	function replaceWithCanvg(widget){
				 		var element = document.querySelector('#w'+widget.id+' .placedWidget');
				 		var xml = new XMLSerializer().serializeToString(angular.element(element).find('svg')[0]);
						xml = xml.replace(/xmlns=\"http:\/\/www\.w3\.org\/2000\/svg\"/, '');
						document.getElementById('canvas_'+widget.id).classList.add('show-for-canvas');
						canvg(document.getElementById('canvas_'+widget.id), xml);
				 	}

				 	function replaceIframe(widget){
				 		if(widget.type == 'python' && (widget.pythonOutputType == 'html' || widget.pythonOutputType == 'bokeh')) {
							var element = document.querySelector('#w'+widget.id+' iframe').contentWindow.document.getElementsByTagName('body')[0];
						}else{
				 			var element = document.querySelector('#w'+widget.id+' iframe').contentWindow.document.getElementsByTagName("iframe")[0].contentWindow.document.getElementsByTagName("iframe")[0].contentWindow.document.getElementsByTagName('body')[0];
				 		}
				 		if(element.className && element.className == 'kn-svgviewer') {
				 			element = element.querySelector('iframe');
					 		var xml = new XMLSerializer().serializeToString(angular.element(element)[0].contentWindow.document.getElementsByTagName('svg')[0]);
							xml = xml.replace(/xmlns=\"http:\/\/www\.w3\.org\/2000\/svg\"/, '');
							document.getElementById('canvas_'+widget.id).classList.add('show-for-canvas');
							canvg(document.getElementById('canvas_'+widget.id), xml);
				 		}else{
					 		html2canvas(element,{
					 			allowTaint: true,
					 			useCORS: true,
					 			foreignObjectRendering: true,
					 			//FF trick or will throw error
					 			async:true,
					 			width: element.clientWidth,
					 			height: element.scrollHeight
					 		}).then(function(canvas){
					 			document.querySelector('#canvas_'+widget.id).classList.add('show-for-canvas');
					 			document.querySelector('#canvas_'+widget.id).innerHTML = '';
					 			document.querySelector('#canvas_'+widget.id).appendChild(canvas);
					 		},function(error){
					 			reject(error);
					 		})
				 		}
				 	}

				 	function getPage(sheet){
				 		try{
					 		var heightToUse;
					 		var exportSheetBar = false;
					 		var element = document.getElementById('kn-cockpit');
					 		var gridsterElement = document.querySelector('#gridsterSheet-'+sheet.index+' #gridsterContainer');

					 		if(element.scrollHeight < gridsterElement.scrollHeight){
					 			element = gridsterElement;
					 			heightToUse = gridsterElement.scrollHeight + 32;
					 			exportSheetBar = true;
					 		}
					 		else heightToUse = element.scrollHeight;

				 			if(sheet.index != 0) doc.addPage([element.clientWidth,heightToUse],heightToUse>element.clientWidth? 'p':'l');
				 			$timeout(function(){
				 				html2canvas(element,{
						 			allowTaint: true,
						 			useCORS: true,
						 			//foreignObjectRendering: true,
						 			width: element.clientWidth,
						 			height: element.scrollHeight,
						 			scale : 1.5
						 		}).then(function(canvas) {
						 			doc.addImage(canvas, 'PNG', 0, 0, element.clientWidth/2.835, element.scrollHeight/2.835);
						 			if(exportSheetBar){
						 				html2canvas(document.querySelector('#sheetTabs md-tabs-wrapper'),{width: element.clientWidth,height: 32}).then(function(sheetCanvas){
						 					doc.addImage(sheetCanvas, 'PNG', 0, element.scrollHeight/2.835, element.clientWidth/2.835, 11.287);
						 					closeOrContinue(sheet);
						 				})
						 			}else{
						 				closeOrContinue(sheet);
						 			}
						 		},function(error){
						 			reject(error);
						 		});
				 			},300);
				 		}catch(error){
				 			reject(error);
				 		}
				 	}

					 	if($scope.sheetsWidgets.length == $scope.cockpitModule_widgetServices.getAllWidgets().length){
					 		if(isIE11) { //if is IE11 and using higcharts
						 			for(var w in sheet.widgets){
					 					if(sheet.widgets[w].type == 'chart' && $scope.enterpriseEdition){
					 						var chartType =  sheet.widgets[w].content.chartTemplate ?sheet.widgets[w].content.chartTemplate.CHART.type.toLowerCase() : "bar";
					 						if(d3Charts.indexOf(chartType)==-1){
					 							replaceWithCanvg(sheet.widgets[w]);
					 						}
					 					}
				 					}
				 				}
					 		for(var w in sheet.widgets){
					 			if(sheet.widgets[w].type == 'python' && sheet.widgets[w].pythonOutputType != 'img'){
					 				replaceIframe(sheet.widgets[w]);
					 			}
					 		}
					 		$timeout(function(){
					 			getPage(sheet);
					 		},isIE11 ? 1000 : 100)
					 	}else{
					 		$scope.sheetWatcher = $scope.$watchCollection('sheetsWidgets',function(newValue,oldValue){
					 			resetTimeout();
					 				var tempIds = [];
						 			for(var w in sheet.widgets){
						 				if(newValue.indexOf(sheet.widgets[w].id) != -1) tempIds.push(sheet.widgets[w].id);
						 			}
						 			if(tempIds.length == sheet.widgets.length){
						 				for(var w in sheet.widgets){
								 			if(sheet.widgets[w].type == 'document'){
								 				replaceIframe(sheet.widgets[w]);
								 			}
								 			if(isIE11){
								 				if(sheet.widgets[w].type == 'chart' && $scope.enterpriseEdition){
							 						var chartType =  sheet.widgets[w].content.chartTemplate ?sheet.widgets[w].content.chartTemplate.CHART.type.toLowerCase() : "bar";
							 						if(d3Charts.indexOf(chartType)==-1){
							 							replaceWithCanvg(sheet.widgets[w]);
							 						}
							 					}
								 			}
								 		}
						 				$timeout(function(){
						 					getPage(sheet);
						 					$scope.sheetWatcher();
						 				},isIE11 ? 3000 : 1000)
						 			}
					 		})
					 	}
			 		}

					for(var s in cockpitModule_template.sheets){
						if(cockpitModule_template.sheets[s].index == 0) {
							if(cockpitModule_properties.CURRENT_SHEET != 0) document.querySelector(".sheetPageButton-0").parentNode.click();

							var tempElement = document.getElementById('kn-cockpit');
					 		var gridsterElement = document.querySelector('#gridsterSheet-0 #gridsterContainer');
					 		var sheetBarHeight = cockpitModule_template.sheets.length == 1 ? 0 : 32;
					 		if(tempElement.scrollHeight < gridsterElement.scrollHeight) var heightToUse = gridsterElement.scrollHeight + sheetBarHeight;
							var doc = new jsPDF({
								orientation: (heightToUse || tempElement.scrollHeight) > tempElement.clientWidth ? 'p' : 'l',
								unit: 'mm',
								format: [tempElement.clientWidth, heightToUse || tempElement.scrollHeight]
							});

							getScreenshot(cockpitModule_template.sheets[s]);
							break;
						}
					}
		})
	}



	$scope.captureScreenShot = function(ev){

		function getSheetFromCurrent(current){
			for(var i in cockpitModule_template.sheets){
				if(cockpitModule_template.sheets[i].index == current) return i;
			}
		}

		$scope.loadingScreenshot = true;
		//var element = document.querySelector('#gridsterSheet-'+cockpitModule_properties.CURRENT_SHEET+' #gridsterContainer');
		var element = document.getElementById('kn-cockpit');
		html2canvas(element,{
			allowTaint: true,
 			useCORS: true,
			width: element.clientWidth,
			height: element.clientHeight
			}
		).then(function(canvas) {
			canvas.toBlob(function(blob) {
				saveAs(blob, cockpitModule_template.sheets[getSheetFromCurrent(cockpitModule_properties.CURRENT_SHEET)].label+'.png');
				$scope.loadingScreenshot = false;
			},function(error){$scope.loadingScreenshot = false;});
		});
	};
};



function cockpitSelectionControllerFunction($scope,cockpitModule_template,cockpitModule_datasetServices,$mdDialog,sbiModule_translate,$q,sbiModule_messaging,cockpitModule_documentServices,cockpitModule_widgetSelection,cockpitModule_properties){
	$scope.selection = [];
	$scope.translate = sbiModule_translate;
	$scope.tmpSelection = [];

	angular.copy(cockpitModule_template.configuration.aggregations,$scope.tmpSelection);
	$scope.tmpFilters = {};
	angular.copy(cockpitModule_template.configuration.filters,$scope.tmpFilters);

	$scope.filterForInitialSelection=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_SELECTIONS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_SELECTIONS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}
	$scope.filterForInitialFilter=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_FILTERS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_FILTERS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}

	if($scope.tmpSelection.length >0){
		for(var i=0;i<$scope.tmpSelection.length;i++){
			var selection = $scope.tmpSelection[i].selection;
			for(var key in selection){
				var string = key.split(".");

				var obj = {
						ds : string[0],
						columnName : string[1],
						value : selection[key],
						aggregated:true
				};
				if(!$scope.filterForInitialSelection(obj)){
					$scope.selection.push(obj);
				}
			}
		}
	}

	for(var ds in $scope.tmpFilters){
		var currentDs = cockpitModule_datasetServices.getDatasetByLabel(ds).metadata.fieldsMeta;
		for(var col in $scope.tmpFilters[ds]){
			var aliasColumnName;
			for(var a in cockpitModule_properties.aliases){
				if(cockpitModule_properties.aliases[a].column == col){
					aliasColumnName = cockpitModule_properties.aliases[a].alias;
				}
			}
			var tmpObj={
					ds :ds,
					columnName : aliasColumnName,
					column	: col,
					value : $scope.tmpFilters[ds][col],
					aggregated:false
			}

			if(!$scope.filterForInitialFilter(tmpObj)){
				$scope.selection.push(tmpObj);
			}
		}
	}

	$scope.selectionsGrid = {
		angularCompileRows: true,
		domLayout :'autoHeight',
		enableColResize: false,
		enableFilter: false,
		enableSorting: false,
		pagination: false,
		onGridSizeChanged: resizeColumns,
		columnDefs : [{headerName: $scope.translate.load('sbi.cockpit.dataset'), field:'ds'},
			{headerName: $scope.translate.load('sbi.cockpit.dataset.columnname'), field:'columnName'},
			{headerName: $scope.translate.load('sbi.cockpit.core.selections.list.columnValues'), field:'value'},
			{headerName:"",cellRenderer: buttonRenderer,"field":"id","cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"flex-end","border":"none"},width: 50,suppressSizeToFit:true, tooltip: false}],
		defaultColDef: {
			suppressMovable: true,
			suppressSorting:true,
			suppressFilter:true,
			tooltip: function (params) {
				return params.value;
			},
		},
		rowData: $scope.selection
	};

	function resizeColumns(){
		$scope.selectionsGrid.api.sizeColumnsToFit();
	}

	function buttonRenderer(params){
		return 	'<md-button class="md-icon-button" ng-click="deleteSelection(\''+params.rowIndex+'\')"><md-icon md-font-icon="fa fa-trash"></md-icon></md-button>';
	}

	$scope.deleteSelection=function(rowIndex){
		var item = $scope.selection[rowIndex];
		if(item.aggregated){
			var key = item.ds + "." + item.columnName;

			for(var i=0;i<$scope.tmpSelection.length;i++){
				if($scope.tmpSelection[i].datasets.indexOf(item.ds) !=-1){
					var selection  = $scope.tmpSelection[i].selection;
					delete selection[key];
				}
			}

			var index=$scope.selection.indexOf(item);
			$scope.selection.splice(index,1);
		}else{
			delete $scope.tmpFilters[item.ds][item.column];
			if(Object.keys($scope.tmpFilters[item.ds]).length==0){
				delete $scope.tmpFilters[item.ds];
			}
			var index=$scope.selection.indexOf(item);
			$scope.selection.splice(index,1);
		}
		$scope.selectionsGrid.api.setRowData($scope.selection)

	}

	$scope.clearAllSelection = function(){
		while($scope.selection.length!=0){
			$scope.deleteSelection(0);
		}
	}

	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

	$scope.saveConfiguration = function(){
		cockpitModule_widgetSelection.updateSelections($scope.tmpSelection, $scope.tmpFilters);
		$mdDialog.cancel();
	}

}
})();