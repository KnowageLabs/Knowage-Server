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

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it) v0.0.1
 *
 */
(function() {
angular.module('cockpitModule')

.directive('cockpitChartWidget',function(cockpitModule_widgetServices,$mdDialog,buildParametersForExecution,$compile){
	return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/chartWidget/templates/chartWidgetTemplate.html',
		   controller: cockpitChartWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	// init the widget
                    	element.ready(function () {
                    		scope.initWidget();
                    	});
                    }
                };
		   }
	}
})
.factory('buildParametersForExecution',function(sbiModule_user, sbiModule_config){
	var formAction = function(service){
		return {url: '/' + sbiModule_config.chartEngineContextName
		+ "/api/1.0/chart/pages/" + service // changed by Dragan was
											// /api/1.0/pages/
		+ "&SBI_LANGUAGE=" + sbiModule_config.curr_language
		+ "&SBI_COUNTRY=" + sbiModule_config.curr_country
		+ "&user_id=" + sbiModule_user.userId
		,testUrl: '/' + sbiModule_config.chartEngineContextName
		+ "/api/1.0/chart/pages/executeTest"};// changed by Dragan was
												// /api/1.0/pages/executeTest
	}
	var formEditAction =  formAction('edit_cockpit');
	var formExecAction =  formAction('execute_cockpit');

	var ret = function(widgetData, newData, editMode){
		var ret = {};
		if(editMode){
			ret.formAction = formEditAction;
		}else{
			ret.formAction = formExecAction;
		}
		if(widgetData && widgetData.chartTemplate && widgetData.chartTemplate.CHART){
			widgetData.chartTemplate.CHART.outcomingEventsEnabled = true;
		}
		if(newData){
			widgetData.jsonData = newData;
		}
		ret.formParameters = [{name: 'widgetData', value: {"widgetData": widgetData} }];
		return ret;
	};
	return {
		edit: function(widgetData, newData){
			return ret(widgetData, newData, true);
		},
		execute: function(widgetData, newData){
			return ret(widgetData, newData);
		}
	}
});

function cockpitChartWidgetControllerFunction(
		$scope,cockpitModule_widgetSelection,
		cockpitModule_datasetServices,
		cockpitModule_generalServices,
		cockpitModule_widgetConfigurator,
		cockpitModule_generalOptions,
		$q,
		$mdPanel,
		sbiModule_restServices,
		$httpParamSerializerJQLike,
		sbiModule_config,
		buildParametersForExecution,
		$mdToast,
		sbiModule_messaging,
		sbiModule_translate,
		sbiModule_user,
		$filter,
		$timeout,
		cockpitModule_widgetServices,
		cockpitModule_properties,
		cockpitModule_template,
		sbiModule_util,
		$mdDialog){
	$scope.property={style:{}};
	$scope.selectedTab = {'tab' : 0};
	$scope.cockpitModule_widgetSelection = cockpitModule_widgetSelection;
	$scope.cockpitModule_properties = cockpitModule_properties;
	// variable that contains last data of realtime dataset
	$scope.realTimeDatasetData;
	// variable that contains last data of realtime dataset not filtered by
	// selections
	$scope.realTimeDatasetDataNotFiltered;
	$scope.isIE = window.document.documentMode;
	$scope.model = $scope.ngModel;
	$scope.local = {};

    if($scope.model.dataset != undefined && $scope.model.dataset.dsId !=-1 && !$scope.model.content.columnSelectedOfDatasetAggregations ){
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.local);
			$scope.model.content.columnSelectedOfDatasetAggregations  = [];
			for(var i=0;i<$scope.local.metadata.fieldsMeta.length;i++){
				var obj = $scope.local.metadata.fieldsMeta[i];
				$scope.model.content.columnSelectedOfDatasetAggregations.push(obj);
			}
			$scope.safeApply();
	  }
	 if($scope.model.content.columnSelectedOfDatasetAggregations) {
		 $scope.model.content.columnSelectedOfDatasetAggregations = cockpitModule_widgetServices.checkForUpdatedDataset($scope.model.content.columnSelectedOfDatasetAggregations,$scope.model.dataset.dsId);
		 for(var c in $scope.model.content.columnSelectedOfDatasetAggregations){
			if(!$scope.model.content.columnSelectedOfDatasetAggregations[c].aliasToShow) $scope.model.content.columnSelectedOfDatasetAggregations[c].aliasToShow = $scope.model.content.columnSelectedOfDatasetAggregations[c].alias;
			if($scope.model.content.columnSelectedOfDatasetAggregations[c].fieldType == 'MEASURE' && !$scope.model.content.columnSelectedOfDatasetAggregations[c].aggregationSelected) $scope.model.content.columnSelectedOfDatasetAggregations[c].aggregationSelected = 'SUM';
			if($scope.model.content.columnSelectedOfDatasetAggregations[c].fieldType == 'ATTRIBUTE') $scope.model.content.columnSelectedOfDatasetAggregations[c].isAttribute = true;
			if($scope.model.content.columnSelectedOfDatasetAggregations[c].fieldType == 'MEASURE' && !$scope.model.content.columnSelectedOfDatasetAggregations[c].funcSummary) $scope.model.content.columnSelectedOfDatasetAggregations[c].funcSummary = $scope.model.content.columnSelectedOfDatasetAggregations[c].aggregationSelected;
		 }

		for(var z in $scope.model.content.columnSelectedOfDataset){
			for(var c in $scope.model.content.columnSelectedOfDatasetAggregations){
				if (($scope.model.content.columnSelectedOfDatasetAggregations[c].name == $scope.model.content.columnSelectedOfDataset[z].name) && $scope.model.content.columnSelectedOfDataset[z].aggregationSelected != $scope.model.content.columnSelectedOfDatasetAggregations[c].aggregationSelected) {
					 $scope.model.content.columnSelectedOfDatasetAggregations[c].aggregationSelected = $scope.model.content.columnSelectedOfDataset[z].aggregationSelected;
				}
			}
		}
	 }


	if($scope.ngModel.cross==undefined){
		$scope.ngModel.cross={};
	};

	$scope.enterpriseEdition = (sbiModule_user.functionalities.indexOf("EnableButtons")>-1)? true:false;
	$scope.chartType =  $scope.ngModel.content.chartTemplate ? $scope.ngModel.content.chartTemplate.CHART.type.toLowerCase() : "bar";
	$scope.d3Charts = ["wordcloud","parallel","sunburst","chord"]
	$scope.init=function(element,width,height){
		if($scope.ngModel.content.chartTemplate.CHART.type == "SCATTER" || $scope.ngModel.content.chartTemplate.CHART.type == "BAR" || $scope.ngModel.content.chartTemplate.CHART.type == "LINE" || $scope.ngModel.content.chartTemplate.CHART.type == "BUBBLE"){
	    	  for (var i = 0; i < $scope.ngModel.content.chartTemplate.CHART.VALUES.SERIE.length; i++) {
	    		  for (var j = 0; j < $scope.ngModel.content.chartTemplate.CHART.AXES_LIST.AXIS.length; j++) {
						if($scope.ngModel.content.chartTemplate.CHART.VALUES.SERIE[i].axis == $scope.ngModel.content.chartTemplate.CHART.AXES_LIST.AXIS[j].alias && $scope.ngModel.content.chartTemplate.CHART.AXES_LIST.AXIS[j].labels){
							$scope.ngModel.content.chartTemplate.CHART.VALUES.SERIE[i].scaleFactor = $scope.ngModel.content.chartTemplate.CHART.AXES_LIST.AXIS[j].labels.scaleFactor
						}
		    	  }
	    	  }
		}
		if($scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLORCopy){
			delete $scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLORCopy
		}
		$scope.refreshWidget({type:"chart",chartInit:true},'init');
	};
	$scope.chartLibNamesConfig = chartLibNamesConfig;

	$scope.user = sbiModule_user;

	$scope.IEChartToFix = function(){
		return $scope.isIE && $scope.enterpriseEdition && $scope.d3Charts.indexOf($scope.chartType)==-1;
	}

	$scope.$on('changeChart', function (event, data) {
		setAggregationsOnChartEngine($scope.ngModel.content,sbiModule_util)
		$scope.$broadcast("changeChartType",data);
	});

	$scope.$on('changedChartType', function (event, data){
		$scope.ngModel.content.chartTemplate.CHART = data.CHART
		$scope.refreshWidget(undefined,'init', true);
	});
	$scope.refresh=function(element,width,height,data,nature, undefined, changedChartType,dataAndChartConf){
		if ($scope.ngModel.dataset){
			var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			var aggregations = cockpitModule_widgetSelection.getAggregation($scope.ngModel,dataset);
			$scope.ngModel.dataset.label = $scope.ngModel.dataset.dsLabel;
			var filtersParams = cockpitModule_datasetServices.getWidgetSelectionsAndFilters($scope.ngModel,$scope.ngModel.dataset, false);

			var filtersParams = cockpitModule_datasetServices.getWidgetSelectionsAndFilters($scope.ngModel,$scope.ngModel.dataset, false);

			var params = cockpitModule_datasetServices.getDatasetParameters($scope.ngModel.dataset.dsId);
			var objForDrill = {};
			objForDrill.aggregations = aggregations;
			objForDrill.selections = filtersParams;
			objForDrill.parameters = params;
			objForDrill.par = "?offset=-1&size=-1";
			if(!dataset.useCache){
				objForDrill.par+="&nearRealtime=true";
			}
			var limitRows;
			if($scope.ngModel.limitRows){
				limitRows = $scope.ngModel.limitRows;
			}else if($scope.ngModel.content && $scope.ngModel.content.limitRows){
				limitRows = $scope.ngModel.content.limitRows;
			}
			if(limitRows != undefined && limitRows.enable && limitRows.rows > 0){
				objForDrill.par += "&limit=" + limitRows.rows;
			}
			objForDrill.par += "&widgetName=" + encodeURIComponent($scope.ngModel.content.name);
			var saving = undefined
			if(cockpitModule_generalServices.isNearRealTime() ){
				 saving = false
			}
			if(cockpitModule_generalServices.isSavingDataConfiguration() ){
				 saving = true
			}
			if (dataset.isRealtime == true && dataset.useCache == true){
				// Refresh for Realtime datasets
				var dataToPass = data;
				// apply filters for realtime dataset
				if (nature == 'init' || nature == 'refresh'){
					dataToPass = $scope.realtimeDataManagement(data, nature);
					$scope.realTimeDatasetDataNotFiltered = angular.copy(data);
					if ($scope.realtimeSelections && $scope.realtimeSelections.length > 0){
						$scope.applyRealtimeSelections($scope.realtimeSelections,$scope)
					}
				} else {
					dataToPass = $scope.realtimeDataManagement($scope.realTimeDatasetData, nature);
				}
				$scope.$broadcast(nature,dataToPass,(dataset.isRealtime && dataset.useCache),changedChartType,dataAndChartConf,objForDrill,saving);
				cockpitModule_generalServices.savingDataConfiguration(false)
				cockpitModule_generalServices.setNearRealTime(false)

			} else {
				// Refresh for Not realtime datasets
				$timeout(function (){
					$scope.$broadcast(nature,data, false, changedChartType,dataAndChartConf,objForDrill, saving );
					cockpitModule_generalServices.savingDataConfiguration(false)
					cockpitModule_generalServices.setNearRealTime(false)
				},400)

			}
			if(nature == 'init'){
				$timeout(function(){
					$scope.widgetIsInit=true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				},500);
			}
		}

	};

	$scope.playSonify = function(){
		$scope.$broadcast("playSonify");
	};

	$scope.pauseSonify = function(){
		$scope.$broadcast("pauseSonify");
	}

	$scope.rewindSonify = function(){
		$scope.$broadcast("rewindSonify");
	}

	$scope.cancelSonify = function(){
		$scope.$broadcast("cancelSonify");
	}
	$scope.realtimeSelections = cockpitModule_widgetServices.realtimeSelections;
	/**
	 * Set a watcher on a variable that can contains the associative selections
	 * for realtime dataset
	 */
	var realtimeSelectionsWatcher = $scope.$watchCollection('realtimeSelections',function(newValue,oldValue,scope){
		if (scope.ngModel.dataset){
			var dataset = cockpitModule_datasetServices.getDatasetById(scope.ngModel.dataset.dsId);
			if (dataset.isRealtime == true && dataset.useCache == true){
				if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1 ){
	                cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
	            }
				scope.applyRealtimeSelections(newValue,scope);
			}
		}
	});

	// Check if there are associative selections and apply that to the data
	$scope.applyRealtimeSelections = function(newValue,scope){
		if (newValue.length == 0){
			// the selections are empty
			if ($scope.realTimeDatasetDataNotFiltered) {
				var originalData = angular.copy($scope.realTimeDatasetDataNotFiltered);
				originalData = $scope.realtimeDataManagement(originalData, 'selections')
				// adapt metadata
				if (originalData){
					var metadataFields = originalData.metaData.fields;
					scope.adaptMetadata(metadataFields);
				}

				scope.$broadcast('selections',originalData,true);
			}

		} else if (scope.ngModel && scope.ngModel.dataset && scope.ngModel.dataset.dsId){
			var widgetDatasetId = scope.ngModel.dataset.dsId;
			var widgetDataset = cockpitModule_datasetServices.getDatasetById(widgetDatasetId)

			for (var i=0; i< newValue.length; i++){
				// search if there are selection on the widget's dataset
				if (newValue[i].datasetId == widgetDatasetId){
					var selections = newValue[i].selections;
					// get filter on our dataset
					if (selections[widgetDataset.label]){
						var selectionsOfDataset = selections[widgetDataset.label];
						for (var columnName in selectionsOfDataset) {
							  if (selectionsOfDataset.hasOwnProperty(columnName)) {
								  var selectionsValues = selectionsOfDataset[columnName]
								  for (var z=0 ; z < selectionsValues.length ; z++){
									  var filterValue = selectionsValues[z];
									  // clean the value from the parenthesis
										// ( )

									  filterValue = filterValue.replace(/[()]/g, '');
									  // clean the value from the parenthesis
										// ''
									  filterValue = filterValue.replace(/['']/g, '');
									  var filterValues = []
									  filterValues.push(filterValue);

									  if(scope.realTimeDatasetData){
										  if(scope.realTimeDatasetData.jsonData){
											  scope.realTimeDatasetData = JSON.parse(scope.realTimeDatasetData.jsonData);
										  }

										  // apply the filter function
										  var columnObject = scope.getColumnObjectFromName(scope.ngModel.content.columnSelectedOfDataset,columnName);
										  if (!columnObject){
												columnObject = scope.getColumnObjectFromName(widgetDataset.metadata.fieldsMeta,columnName);
										  }
										  // use the alias to match the
											// filtercolumn name
										  var filterColumnname = columnObject.alias;
										  var columnType = columnObject.fieldType;
										  scope.realTimeDatasetData.rows = scope.filterRows(scope.realTimeDatasetData,columnObject,filterValues,columnType);
										  scope.realTimeDatasetData.results = scope.realTimeDatasetData.rows.length;

										  // adapt the metadata to be sent to
											// the backend
										  var metadataFields = scope.realTimeDatasetData.metaData.fields;
										  scope.adaptMetadata(metadataFields);
										  // send broadcast for selections
											// with data filtered by selections
										  scope.$broadcast('selections',scope.realTimeDatasetData,true);
									  }
								  }
							  }
							}
					}
				}
			}
		}
	}

	/**
	 * Change the header name of the metadata's fields to use the format used by
	 * the chart backend this is necessary because after a realtime update the
	 * data has the original header name from the dataset meanwhile while
	 * loading data from backend of chart the header have the name+grouping
	 * faction (Ex: TEMPERATURE_SUM instead of TEMPERATURE)
	 *
	 */
	$scope.adaptMetadata = function (metadataFields){
		for (var x=0; x < metadataFields.length; x++){
			  if (metadataFields[x].header){
				  var colObj = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,metadataFields[x].header);
				  // set the header to use the alias (ex: temperature_SUM
					// instead of just temperature)
				  if (colObj){
					  metadataFields[x].header = colObj.alias
				  }
			  }
		  }
	}

	/**
	 * Filter or order data from realtime dataset
	 */
	$scope.realtimeDataManagement = function(data, nature){
		if ($scope.ngModel.dataset){
			var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			// Do something only if the dataset is realtime, otherwise just pass
			// the data
			if (dataset.isRealtime == true && dataset.useCache == true){
				// create a deep copy of the data, otherwise filtering on data
				// will be spread to all the widgets
				$scope.realTimeDatasetData = angular.copy(data);

				// *** CLIENT SIDE FILTERING ***
				if ($scope.ngModel.content && $scope.ngModel.content.filters){
					var filters = $scope.ngModel.content.filters;
					for (var i=0; i < filters.length ; i++){
						// check if a filter is specified
						if (filters[i].filterVals.length > 0 ){

							var columnObject = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,filters[i].colName);
							// var filterColumnname = columnObject.alias;
							var filterValues =  filters[i].filterVals;
							var columnType = columnObject.fieldType;
							$scope.realTimeDatasetData.rows = $scope.filterRows($scope.realTimeDatasetData,columnObject,filterValues,columnType);

						}
					}
				}
				// *** CLIENT SIDE SORTING ***
				if ($scope.ngModel.content && $scope.ngModel.content.chartTemplate && $scope.ngModel.content.chartTemplate.CHART && $scope.ngModel.content.chartTemplate.CHART.VALUES && $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY && $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.orderColumn){
					$scope.sortRows($scope.realTimeDatasetData);
				}

				return $scope.realTimeDatasetData;
			}
		}
		return data;

	}
	/**
	 * Client side rows sorting of realtime dataset
	 */
	$scope.sortRows = function (data){
		var columns = $scope.ngModel.content.columnSelectedOfDataset;
		var sortingField = $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.orderColumn;
		if (sortingField.length > 0 ){
			// search for the corresponding alias of the sortingColumn
			for (i = 0; i < columns.length; i++){
				if (columns[i].name === $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.orderColumn){
					sortingField = columns[i].alias;
					break;
				}
			}
			var reverse = false;
			if ($scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.orderType === 'desc'){
				reverse = true;
			}

			// search dataindex corresponding to the sortingField
			var dataIndex;
			if (data.metaData.fields){
				var fields = data.metaData.fields;
				for (var i=0; i< fields.length ; i++){
					// use alias or original name to catch correct field
					if (fields[i].header && (fields[i].header == sortingField ) ){
						// get corresponding dataIndex
						dataIndex = fields[i].dataIndex;
						break;
					}
				}
			}

			if (dataIndex) {
				data.rows = $filter('orderBy')(data.rows, dataIndex, reverse);
			}
		}
	}

	/**
	 * Returns the column object that satisfy the original name (not
	 * aliasToShow) passed as argument
	 */
	$scope.getColumnObjectFromName = function(columnSelectedOfDataset, originalName){
		for (i = 0; i < columnSelectedOfDataset.length; i++){
			if (columnSelectedOfDataset[i].name === originalName){
				return columnSelectedOfDataset[i];
			}
		}
	}

	/**
	 * Return only the objects matching the filter data: object with data and
	 * metadata columnObject: specific object of a column values: array of
	 * admissible values columnType: type (Measure/Attribute) of the column
	 */
	$scope.filterRows = function (data, columnObject, values, columnType ){
		var toReturn = [];
		var dataIndex;
		// search dataIndex
		if (data && data.metaData && data.metaData.fields){
			var fields = data.metaData.fields;
			for (var i=0; i< fields.length ; i++){
				// use alias or original name to catch correct field (because
				// after a realtime update the header use the original name)
				if (fields[i].header && (fields[i].header == columnObject.alias || fields[i].header == columnObject.name) ){
					// get corresponding dataIndex
					dataIndex = fields[i].dataIndex;
					break;
				}
			}
		}
		if (data && data.rows){
			var rows = data.rows;
		}


		if (dataIndex != null){
			for (var i=0; i < rows.length ; i++){
				if (rows[i][dataIndex]){
					for (var y=0; y < values.length ; y++){
						// handle Attribute as String and Measure as number
						if (columnType == 'ATTRIBUTE' || columnType == 'SPATIAL_ATTRIBUTE'){
							if (rows[i][dataIndex] == values[y]){
								toReturn.push(rows[i]);
							}
						} else if (columnType == 'MEASURE'){
							var columnValue = Number(rows[i][dataIndex]);
							var filterValue = Number(values[y]);
							if (columnValue == filterValue){
								toReturn.push(rows[i]);
							}
						}

					}
				}
			}
		}


		return toReturn;
	}

	$scope.editWidget=function(index){
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: function($scope,sbiModule_translate,model,mdPanelRef,doRefresh,sbiModule_user,cockpitModule_generalOptions,cockpitModule_datasetServices,sbiModule_util){
					  $scope.translate=sbiModule_translate;
					  $scope.confSpinner=false;
					  $scope.somethingChanged=false;
					  $scope.localStyle=angular.copy(model.style);
					  $scope.localModel = angular.copy(model.content);
					  $scope.localModel.cross= angular.copy(model.content.cross);
					  $scope.user = sbiModule_user;
					  $scope.localDataset = {};
					  $scope.typesMap = cockpitModule_generalOptions.typesMap;

					  $scope.getDatasetAdditionalInfo = function(dsId){
					        for(var k in cockpitModule_template.configuration.datasets){
					        	if(cockpitModule_template.configuration.datasets[k].dsId == dsId) {
					        		$scope.tempDataset = cockpitModule_template.configuration.datasets[k];
					        		break;
					        	}
					        }
					        sbiModule_restServices.restToRootProject();
					        sbiModule_restServices.promiseGet('2.0/datasets', 'availableFunctions/' + dsId, "useCache=" + $scope.tempDataset.useCache).then(function(response){
					        	$scope.datasetAdditionalInfos = response.data;
					        }, function(response) {
					        	if(response.data && response.data.errors && response.data.errors[0]) $scope.showAction(response.data.errors[0].message);
					        	else $scope.showAction($scope.translate.load('sbi.generic.error'));
					        });
						}

						if($scope.localModel.datasetId) $scope.getDatasetAdditionalInfo($scope.localModel.datasetId);

					  $scope.handleEvent=function(event, arg1){
						  if(event=='init'){
							  if($scope.localModel.datasetId != undefined){
								  $scope.datasetChanged = true;
								  $scope.confChecked = true;
							  }
						  }else if(event=='closeConfiguration'){
							  checkConfiguration();
						  }else if(event=='openStyle'){
							  $scope.somethingChanged = true;
						  }else if(event=='openCross'){
							  $scope.somethingChanged = true;
						  }else if(event=='openFilters'){
							  $scope.somethingChanged = true;
						  }else if(event=='save'){
							  $scope.somethingChanged = true;
							  if(checkChartSettings()==undefined){
								  return
							  }
							  else if(!checkChartSettings()){
								  	if($scope.localModel.chartTemplate.type.toUpperCase()=="SCATTER"){
										showAction($scope.translate.load('sbi.cockpit.select.no.aggregation.for.all.series'));
									}
								  	if ($scope.localModel.chartTemplate.type.toUpperCase()=="BAR" || $scope.localModel.chartTemplate.type.toUpperCase()=="LINE" ) {
										showAction($scope.translate.load('sbi.chartengine.validation.addserie.arearange.parLowHigh'));
									}


							  }
							  else{
								  $scope.attachCategoriesToTemplateInIframe();
						          if($scope.localModel.chartTemplate.CHART.hasOwnProperty('CHART')){
						        	  var temp = $scope.localModel.chartTemplate.CHART.CHART;
						        	  delete $scope.localModel.chartTemplate.CHART;
						        	  $scope.localModel.chartTemplate.CHART = temp;
						          }
						          removeUnnecessarySeries();
								  saveConfiguration();
							  }
						  }else if(event=='datasetChanged'){
							  $scope.somethingChanged = true;
							  changeDatasetFunction(arg1);
							  $scope.datasetChanged = true;
							  $scope.confChecked = false;
						  }else if(event=='openConfiguration'){
							  $scope.somethingChanged = true;
							  $scope.confChecked = false;
							  if($scope.datasetChanged){
								  $scope.datasetChanged = false;
							  }
							  $scope.$broadcast('updateMeasuresWithCF');
						  }
					  }
			    	  var changeDatasetFunction=function(dsId){
			    		    if($scope.localModel.datasetId){
			    		         $scope.localModel.datasetId=null
			    		    }
			    		    $scope.getDatasetAdditionalInfo(dsId);
			    		  var ds = cockpitModule_datasetServices.getDatasetById(dsId);
			    		  if(ds){
			    			  if(ds.id.dsId != $scope.localModel.datasetId && ds.id.dsLabel != $scope.localModel.datasetLabel){
			    				  // Clearing chart configurations
			    				  delete $scope.localModel.aggregations;
			    				  delete $scope.localModel.chartTemplate;
			    				  delete $scope.localModel.columnSelectedOfDataset;
			    			  }
			    			  $scope.localModel.datasetLabel = ds.label;
			    			  $scope.localModel.dataset = {id: {dsId : ds.id.dsId}};
			    			  if(!$scope.localModel.datasetId){
                                  $timeout(function(){
                                      $scope.localModel.datasetId = ds.id.dsId;
                                      $scope.localModel.dataset.dsId = $scope.localModel.datasetId;
                                  }, 100);
                              }
			    			  $scope.localModel.columnSelectedOfDatasetAggregations  = [];
		    					for(var i=0;i<ds.metadata.fieldsMeta.length;i++){
		    						var obj = ds.metadata.fieldsMeta[i];
		    						$scope.localModel.columnSelectedOfDatasetAggregations.push(obj);
		    					}
			    		  }

						  for(var c in $scope.localModel.columnSelectedOfDatasetAggregations){
							  if(!$scope.localModel.columnSelectedOfDatasetAggregations[c].aliasToShow) $scope.localModel.columnSelectedOfDatasetAggregations[c].aliasToShow = $scope.localModel.columnSelectedOfDatasetAggregations[c].alias;
							  if($scope.localModel.columnSelectedOfDatasetAggregations[c].fieldType == 'MEASURE' && !$scope.localModel.columnSelectedOfDatasetAggregations[c].aggregationSelected) $scope.localModel.columnSelectedOfDatasetAggregations[c].aggregationSelected = 'SUM';
							  if($scope.localModel.columnSelectedOfDatasetAggregations[c].fieldType == 'ATTRIBUTE') $scope.localModel.columnSelectedOfDatasetAggregations[c].isAttribute = true;
							  if($scope.localModel.columnSelectedOfDatasetAggregations[c].fieldType == 'MEASURE'  && !$scope.localModel.columnSelectedOfDatasetAggregations[c].funcSummary) $scope.localModel.columnSelectedOfDatasetAggregations[c].funcSummary = $scope.localModel.columnSelectedOfDatasetAggregations[c].aggregationSelected;
						  }

						  if ($scope.localModel.content) {
							  for(var z in $scope.localModel.content.columnSelectedOfDataset){
								  for(var c in $scope.localModel.columnSelectedOfDatasetAggregations){
									  if (($scope.localModel.content.columnSelectedOfDatasetAggregations[c].name == $scope.model.content.columnSelectedOfDataset[z].name) && $scope.localModel.content.columnSelectedOfDataset[z].aggregationSelected != $scope.localModel.content.columnSelectedOfDatasetAggregations[c].aggregationSelected) {
										   $scope.localModel.content.columnSelectedOfDatasetAggregations[c].aggregationSelected = $scope.localModel.content.columnSelectedOfDataset[z].aggregationSelected;
									  }
								  }
							  }
						  }
			    	  }


			    	  if ($scope.localModel.datasetId != undefined) {
			    		  $scope.localModel.dataset = cockpitModule_datasetServices.getDatasetById($scope.localModel.datasetId);
			    		  $scope.localModel.dataset.dsId = $scope.localModel.datasetId;
			    	  }

			    		if($scope.localModel.dataset){
			    			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.localModel.datasetId), $scope.localDataset);
			    		} else{
			    			// angular.copy([],
							// $scope.localModel.dataset.metadata.fieldsMeta);
			    		}

			    	  var checkConfiguration=function(){

			    		  $scope.confChecked = true;

	    				  setAggregationsOnChartEngine($scope.localModel,sbiModule_util);
	    				  return true;
			    	  }


// check if right number of operands have been specified depending on operator
// type
				  		var checkFilters = function(){
				  			var filters = $scope.model.content.filters;

				  			if(filters == undefined) return true;

				  			var oneOperandOperator = ['=','!=','like','<','>','<=','>='];
				  			var twoOperandOperator = ['range'];
				  			var zeroOperandOperator = ['is null','is not null','min','max'];

				  			for(var i = 0; i < filters.length - 1; i++){
				  				var filter = filters[i];
				  				var operator = filter.filterOperator;
				  				var values = filter.filterVals;

				  				if(oneOperandOperator.indexOf(operator) > -1){
				  					if(values.length<1){
				  						return false;
				  					}
				  					else{
				  						if(values[0] == ''){
				  							return false;
				  						}
				  					}

				  				}
				  				else if(twoOperandOperator.indexOf(operator) > -1){
				  					if(values.length<2){
				  						return false;
				  					}
				  					else{
				  						if(values[0] == '' || values[1] == ''){
				  							return false;
				  						}
				  					}
				  				}
				  				else if(zeroOperandOperator.indexOf(operator) > -1){
				  					if(values.length!=0){
				  						return false;
				  					}
				  				}
				  			}
				  			return true;
				  		}





			    	  var checkChartSettings = function (){
			    		  var chartTemplate = $scope.localModel.chartTemplate.CHART ? $scope.localModel.chartTemplate.CHART : $scope.localModel.chartTemplate;

			    		  if(chartTemplate.VALUES.SERIE.length==0){
			    			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning.misingChartDesigner"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
			    			  return;
			    		  }
			    		  var f = true;

			    		  $scope.$broadcast("validateForm");
			    		  var isFormValid = cockpitModule_widgetServices.isFormValid();
			    		  if(!isFormValid) {
			    			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning.message"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
			    			  return false;
			    		  }

			    		 if(chartTemplate.type == "SCATTER" || chartTemplate.type == "BAR" || chartTemplate.type == "LINE" || chartTemplate.type == "BUBBLE"){
					    	  for (var i = 0; i < chartTemplate.VALUES.SERIE.length; i++) {
					    		  for (var j = 0; j < chartTemplate.AXES_LIST.AXIS.length; j++) {
										if(chartTemplate.VALUES.SERIE[i].axis == chartTemplate.AXES_LIST.AXIS[j].alias && chartTemplate.AXES_LIST.AXIS[j].labels){
											chartTemplate.VALUES.SERIE[i].scaleFactor = chartTemplate.AXES_LIST.AXIS[j].labels.scaleFactor
										}
						    	  }
					    	  }
				    	  }

	    				  if (chartTemplate.type == "SCATTER" && chartTemplate.VALUES.SERIE.length>1) {
	    					  var allSeries = chartTemplate.VALUES.SERIE;
	    						var counter = 0;
	    						for (var i = 0; i < allSeries.length; i++) {
	    							if(allSeries[i].groupingFunction=="NONE"){
	    								counter++
	    							};
	    						}
	    						if(counter<chartTemplate.VALUES.SERIE.length){
	    							f=false;
	    						}
	    						if (counter == 0) f = true;
	    				  }

	    				  if ((chartTemplate.type == "BAR" || chartTemplate.type == "LINE") && chartTemplate.VALUES.SERIE.length>0) {
	    					  var allSeries = chartTemplate.VALUES.SERIE;
	    						var counterlow = 0;
	    						var counterhigh = 0;
	    						for (var i = 0; i < allSeries.length; i++) {
	    							if(allSeries[i].type=="arearangelow"){
	    								counterlow++
	    							};
	    						}
	    						for (var i = 0; i < allSeries.length; i++) {
	    							if(allSeries[i].type=="arearangehigh"){
	    								counterhigh++
	    							};
	    						}
	    						if(counterlow!=counterhigh){
	    							f=false;
	    						}
	    				  }

	    				  return f;

			    	  }

			    	  $scope.attachCategoriesToTemplateInIframe = function() {
			    		  $scope.$broadcast("attachCategories");
			          };

			          var removeUnnecessarySeries = function (){
			        	  $scope.$broadcast("removeUnnecessarySeries");
			          }
			    	  var saveConfiguration=function(){

			    		  if($scope.localModel.datasetId == undefined){
			    			  // Warning: Please select a dataset
			    			  showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
			    		  }
// else if (checkFilters()==false ){
// showAction($scope.translate.load('sbi.cockpit.table.errorfilters'));
// }
			    		  else {
			    			  if(checkConfiguration()){
			    				  if($scope.somethingChanged){
			    					  $scope.localModel.wtype = "chart";
			    					  $scope.localModel.designer = "Chart Engine Designer";

			    					  angular.copy($scope.localModel, model.content);
			    					  if(model.style==undefined){
			    						  model.style={};
			    					  }
			    					  angular.copy($scope.localStyle, model.style);
			    					  model.dataset = {dsId: $scope.localModel.datasetId, dsLabel: $scope.localModel.datasetLabel};
			    				  }
			    				  mdPanelRef.close();
			    				  $scope.$destroy();
			    				  doRefresh(undefined,'init');
			    				  finishEdit.resolve();
		    				  }
			    		  }
			    	  }


			    	  $scope.cancelConfiguration=function(){
			    		  mdPanelRef.close();
			    		  $scope.$destroy();
			    		  finishEdit.reject();
			    	  }

			    	  var showAction = function(text) {
			  			var toast = $mdToast.simple()
			  			.content(text)
			  			.action('OK')
			  			.highlightAction(false)
			  			.hideDelay(3000)
			  			.position('top')

			  			$mdToast.show(toast).then(function(response) {
			  				if ( response == 'ok' ) {
			  				}
			  			});
			  		  }

			    	  $scope.hideWidgetSpinner=function(){
			    		  $scope.confSpinner=false;
			    		  safeApply();
			    	  }

			    	  $scope.finishLoadingIframe=function(){
			    		  $scope.hideWidgetSpinner();
			    		  safeApply();
			    	  }

			    	  var safeApply=function(){
		    			  if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
		    				  $scope.$apply();
		    			  }
		    		  }

			    	  $scope.handleEvent('init');

			    	  $scope.updateGrid = function (){
			    		  $scope.columnsGrid.api.setRowData($scope.localModel.columnSelectedOfDatasetAggregations);
			    		  $scope.columnsGrid.api.sizeColumnsToFit();
			    		  $scope.somethingChanged = true;
			    	  }

			  		function editableCell(params){
			  			return typeof(params.value) !== 'undefined' ? '<i class="fa fa-edit"></i> <i>'+params.value+'<md-tooltip>'+params.value+'</md-tooltip></i>' : '';
			  		}
			  		function typeCell(params){
			  			return "<i class='"+$scope.typesMap[params.value].icon+"'></i> "+$scope.typesMap[params.value].label;
			  		}
			  		function isInputEditable(params) {
			  			return typeof(params.data.name) !== 'undefined';
			  		}
			  		function isAggregationEditable(params) {
			  			return params.data.fieldType == "MEASURE"  && !params.data.isCalculated ? true : false;
			  		}
			  		function aggregationRenderer(params) {
			  			var aggregation = '<i class="fa fa-edit"></i> <i>'+params.value+'</i>';
			  			changeAggregationOnSerie(params.data.alias, params.value);
			  	        return params.data.fieldType == "MEASURE"  && !params.data.isCalculated ? aggregation : '';

			  		}
			  		function changeAggregationOnSerie(alias, aggFunc) {
			  			if($scope.localModel.chartTemplate) {
			  				var chartSeries =  $scope.localModel.chartTemplate.CHART ? $scope.localModel.chartTemplate.CHART.VALUES.SERIE : $scope.localModel.chartTemplate.VALUES.SERIE;
		  					for(var j = 0; j < chartSeries.length; j++) {
		  						if(alias == chartSeries[j].column) {
			  						chartSeries[j].groupingFunction = aggFunc;
			  					}
		  					}
			  			}
			  		}

			  		$scope.updateAliasOnSerie = function(newAlias, oldAlias) {
			  			if($scope.localModel.chartTemplate) {
			  				var chartSeries =  $scope.localModel.chartTemplate.CHART ? $scope.localModel.chartTemplate.CHART.VALUES.SERIE : $scope.localModel.chartTemplate.VALUES.SERIE;
		  					for(var j = 0; j < chartSeries.length; j++) {
		  						if(oldAlias == chartSeries[j].column) {
		  							chartSeries[j].column = newAlias;
		  							chartSeries[j].name = newAlias;
			  					}
		  					}
			  			}
			  		}

			  		function rowDragEnter(event){
			  			$scope.startingDragRow = event.overIndex;
			  		}
			  		function onRowDragEnd(event){
			  			moveInArray(model.content.columnSelectedOfDatasetAggregations, $scope.startingDragRow, event.overIndex);
			  		}

			  		function resizeColumns(){
			  			$scope.columnsGrid.api.sizeColumnsToFit();
			  		}
			  		function refreshRow(cell){
			  			if(cell.data.fieldType == 'MEASURE' && !cell.data.isAttribute && !cell.data.aggregationSelected) cell.data.aggregationSelected = 'SUM';
			  			if(cell.data.fieldType == 'MEASURE' && cell.data.isAttribute && !cell.data.aggregationSelected) cell.data.aggregationSelected = '';
			  			if(cell.data.fieldType == 'MEASURE' && !cell.data.isAttribute && cell.data.aggregationSelected) cell.data.funcSummary = cell.data.aggregationSelected == 'NONE' ? 'SUM' : cell.data.aggregationSelected;
			  			if(cell.data.fieldType == 'MEASURE' && cell.data.isAttribute && cell.data.aggregationSelected) cell.data.funcSummary = cell.data.aggregationSelected;

			  			if(cell.data.isCalculated) cell.data.alias = cell.data.aliasToShow;
			  			$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
			  		}

			  		function fieldTypeToAggregationMap(match) {
			  		  var map = {
			  		    ATTRIBUTE: ["","COUNT","COUNT_DISTINCT"],
			  		    MEASURE: ["NONE","SUM","AVG","MAX","MIN","COUNT","COUNT_DISTINCT"],
			  		  };

			  		  return map[match];
			  		}

			  		$scope.columnsDefinition = [
			  	    	{
			  	    		headerName: 'Name',
			  	    		field:'alias'
			  	    	},
			  	    	{
			  	    		headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.alias'),
			  	    		field:'aliasToShow'
			  	    	},
			  	    	{
			  	    		headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.type'),
			  	    		field: 'fieldType',
			  	    		editable: true,
			  	    		cellEditor: "agSelectCellEditor",
			  	    		cellEditorParams: {values: ['ATTRIBUTE','MEASURE']}
			  	    	},
			  	    	{
			  	    		headerName: 'Data Type',
			  	    		field: 'type',
			  	    		cellRenderer: typeCell
			  	    	},
			  	    	{
			  	    		headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.aggregation'),
			  	    		field: 'aggregationSelected',
			  	    		cellRenderer: aggregationRenderer,
			  	    		editable: isAggregationEditable,
			  	    		cellClass: 'editableCell',
			  	    		cellEditor: "agSelectCellEditor",
			  	    		cellEditorParams: function(params) {
			  	    			 var selectedFieldType = null;
			  	    			if(params.data.fieldType == 'ATTRIBUTE' || params.data.fieldType == 'MEASURE' && params.data.isAttribute) {
			  	    				 selectedFieldType = 'ATTRIBUTE';
			  	    			} else {
			  	    				 selectedFieldType = params.data.fieldType;
			  	    			}
			  	    	        var allowedAggregations = fieldTypeToAggregationMap(selectedFieldType);
			  	    	        return {
			  	    	          values: allowedAggregations,
			  	    	          formatValue: function(value) {
			  	    	            return value + ' (' + selectedFieldType + ')';
			  	    	          },
			  	    	        };
			  	    	    }
			  	    	},
			  	    	{
			  	    		headerName: "",
			  	    		cellRenderer: buttonRenderer,
			  	    		field: "valueId",
			  	    		cellStyle: {"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"flex-end"},
			  	    		width: 150,
			  	    		suppressSizeToFit: true,
			  	    		tooltip: false,
			  	    	}
			  	    ];

			  		$scope.columnsGrid = {
			  				angularCompileRows: true,
			  				domLayout:'autoHeight',
			  		        enableColResize: false,
			  		        enableFilter: false,
			  		        enableSorting: false,
			  		        onGridReady : resizeColumns,
			  		        singleClickEdit: true,
			  		        onCellEditingStopped: refreshRow,
			  		        columnDefs: $scope.columnsDefinition,
			  				rowData: $scope.localModel.columnSelectedOfDatasetAggregations
			  		}

			  		function buttonRenderer(params){
			  			if(params.data.isCalculated){

			  				return '<calculated-field ng-model="localModel"  callback-update-grid="updateGrid()" callback-update-alias="updateAliasOnSerie(newAlias, oldAlias)" selected-item="'+params.rowIndex+'" additional-info="datasetAdditionalInfos"></calculated-field>' +
			  				'<md-button class="md-icon-button" ng-click="deleteColumn(\''+params.data.alias+'\',$event)"><md-icon md-font-icon="fa fa-trash"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>';
			  			}

			  		}

			  		$scope.deleteColumn = function(rowName,event) {
			  			var chartSeries =  $scope.localModel.chartTemplate.CHART ? $scope.localModel.chartTemplate.CHART.VALUES.SERIE : $scope.localModel.chartTemplate.VALUES.SERIE;
		  				for(var i = 0; i < chartSeries.length; i++) {
			  				if(chartSeries[i].column == rowName) {
				  				sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning.deletingCFOnSerie"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
				    			return;
			  				}
		  				}
		  				for(var k in $scope.localModel.columnSelectedOfDatasetAggregations){
							if($scope.localModel.columnSelectedOfDatasetAggregations[k].alias == rowName) {
								var item = $scope.localModel.columnSelectedOfDatasetAggregations[k];
								var index=$scope.localModel.columnSelectedOfDatasetAggregations.indexOf(item);
								$scope.localModel.columnSelectedOfDatasetAggregations.splice(index,1);
							}
						}
						if($scope.localModel.settings && $scope.localModel.settings.sortingColumn && $scope.localModel.settings.sortingColumn == item.aliasToShow){
							$scope.localModel.settings.sortingColumn = null;
						}
		  			}

			  		$scope.$watchCollection('localModel.columnSelectedOfDatasetAggregations',function(newValue,oldValue){
						if($scope.columnsGrid.api && newValue){
							$scope.columnsGrid.api.setRowData(newValue);
							$scope.columnsGrid.api.sizeColumnsToFit();
						}
			  		});

			  		/*
					 *
					 */



			      },
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/chartWidget/templates/chartWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {finishEdit:finishEdit, model:$scope.ngModel, doRefresh:$scope.refreshWidget}
		};
		$mdPanel.open(config);

		return finishEdit.promise;
	}


	$scope.reloadWidgetsByChartEvent = function(item){
		if($scope.ngModel.cliccable == false) return
		var event= item.select != undefined ? item.select : item;
		var crossParameters= createCrossParameters(item);
		var chartType = $scope.ngModel.content.chartTemplate.CHART.type;
		if($scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLORCopy){
			delete $scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLORCopy
		}
		if($scope.ngModel.updateble && chartType.toLowerCase()=="pie"){
			$scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLORCopy = angular.copy($scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLOR)
			if($scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLOR[0]){
				$scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLOR[0].value = item.point.color;
			} else {
				$scope.ngModel.content.chartTemplate.CHART.COLORPALETTE.COLOR.push({"value":item.point.color})
			}
		}
		if($scope.ngModel.cliccable==false){
			console.log("widget is not cliccable")
			return;
		}

		// check if cross navigation was enable don this widget
		var model = {};
		if($scope.ngModel.cross.enable!=undefined){
			model = $scope.ngModel;
		} else {
			model = $scope.ngModel.content;
		}
		var crossEnabled = model.cross != undefined && model.cross.enable === true;

		var outputParameterExists = crossEnabled &&  (model.cross.column!=undefined &&  model.cross.outputParameter != undefined);
		var outputParametersListExists = crossEnabled && model.cross.outputParametersList != undefined;

		if( outputParameterExists || outputParametersListExists){
			var outputParameter = {};
			if (model.cross.outputParameter!= undefined) outputParameter[model.cross.outputParameter] = crossParameters[model.cross.column];


			// parse output parameters if enabled
			var otherOutputParameters = [];
			var passedOutputParametersList = model.cross.outputParametersList;

			for(par in passedOutputParametersList){
				var content = passedOutputParametersList[par];

				if(content.enabled == true){

					/*
					 * if(content.dataType == 'date' && content.value !=
					 * undefined && content.value != ''){
					 *
					 * content.value =
					 * content.value.toLocaleDateString('en-US');
					 * content.value+= "#MM/dd/yyyy"; }
					 */

					if(content.type == 'static'){
						var objToAdd = {};
						objToAdd[par] = content.value;
						otherOutputParameters.push(objToAdd);
					}
					else if(content.type == 'dynamic'){
						if(content.column){
							var valToAdd = crossParameters[content.column];
							var objToAdd = {};
							objToAdd[par] = valToAdd;
							otherOutputParameters.push(objToAdd);
						}
					}
					else if(content.type == 'selection'){
						var selectionsObj = cockpitModule_template.getSelections();
						if(selectionsObj){
							var found = false;
							for(var i = 0; i < selectionsObj.length && found == false; i++){
								if(selectionsObj[i].ds == content.dataset && selectionsObj[i].columnName == content.column){
									var val = selectionsObj[i].value;
									var objToAdd = {};
									objToAdd[par] = val;
									otherOutputParameters.push(objToAdd);
									found = true;
								}
							}
						}
					}
				}
			}







			// parse static parameters if present
			/*
			 * var staticParameters = []; if(model.cross.staticParameters &&
			 * model.cross.staticParameters != ''){ var err=false; try{ var
			 * parsedStaticPars = model.cross.staticParameters.split("&");
			 * for(var i=0;i<parsedStaticPars.length;i++){ var
			 * splittedPar=parsedStaticPars[i].split("=");
			 * if(splittedPar[0]==undefined ||
			 * splittedPar[1]==undefined){err=true;} else{ var toInsert = {};
			 * toInsert[splittedPar[0]] = splittedPar[1];
			 * staticParameters.push(toInsert); }
			 *  }
			 *
			 * }catch(e){ err=true console.error(e); }finally{ if(err){
			 * $mdDialog.show( $mdDialog.alert() .clickOutsideToClose(true)
			 * .title(sbiModule_translate.load("sbi.cockpit.cross.staticParameterErrorFormatTitle"))
			 * .content(sbiModule_translate.load("sbi.cockpit.cross.staticParameterErrorFormatMsg"))
			 * //.ariaLabel('Alert Dialog Demo')
			 * .ok(sbiModule_translate.load("sbi.general.continue")) ); return; } }
			 *  }
			 */


			// if destination document is specified don't ask
			if(model.cross.crossName != undefined){
				parent.execExternalCrossNavigation(outputParameter,{},model.cross.crossName,null,otherOutputParameters);
				return;
			}
			else{
				parent.execExternalCrossNavigation(outputParameter,{},null,null,otherOutputParameters);
				return;
			}
		}

		if(event.point){
			// for highcharts
			var date = new Date(event.point.x);
			var char =  "/" ;
			var theyear=date.getFullYear()
			var themonth=date.getMonth()+1
			var theday=date.getDate()
			var date_format = theday+char+themonth+char+theyear;

			if(chartType === 'SCATTER'){

				var columnValue  = {};
				if($scope.ngModel.content.chartTemplate.CHART.dateTime){
					columnValue = date_format;
				}else {
					columnValue = event.point.category.name;
				}
			}else{
				var columnValue  = {};
				/*
				 * if($scope.ngModel.content.chartTemplate.CHART.dateTime){
				 * columnValue = date_format; }else { columnValue =
				 * event.point.name; }
				 */
				columnValue = event.point.name;
			}


			var category = $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY;
			var columnName = category.name


			// var d3Types = ["WORDCLOUD", "PARALLEL", "SUNBURST"];

			// if(d3Types.indexOf(chartType)<0){
			if( Array.isArray(category)){
				columnName = category[(event.point.id.match(new RegExp("_", "g")) ).length-1].name;
			}else{
				columnName = category.name;
			}
			if(chartType === 'HEATMAP'){
				columnName = event.point.category;
				columnValue = event.point.name;
				$scope.doSelection(columnName,columnValue, null, null, null, true);
				columnName = event.point.group.name;
				columnValue = event.point.group.value;
				$scope.doSelection(columnName,columnValue);
			}else{

				$scope.doSelection(columnName,columnValue);
			}
		}else{
			// for d3 charts
			if( event["selectParam_cross"]){
				delete event["selectParam_cross"];
			}
			var count = Object.keys(event).length;
			for (column in event){
				if(count>1){
					$scope.doSelection(column,event[column],null,null,null,true);
				}else{
					$scope.doSelection(column,event[column]);
				}
				count--;
			}
		}



	}

	$scope.finishLoadingIframe=function(){
		$scope.hideWidgetSpinner();
	}

	function createCrossParameters(event){
		if($scope.ngModel.content.chartTemplate.CHART.dateTime){
			var date = new Date(event.point.x);
			var char =  "/" ;
			var theyear=date.getFullYear()
			var themonth=date.getMonth()+1
			var theday=date.getDate()
			var date_format = theday+char+themonth+char+theyear;
		}
		if( $scope.ngModel.content.chartTemplate.CHART.type==="HEATMAP"){
			var parameters = {
    				"SERIE_NAME": event.point.series.name,
    				"SERIE_VALUE":event.point.y,
    				"CATEGORY_VALUE":event.point.name,
    				"CATEGORY_NAME": event.point.category,
    				"GROUPING_NAME": event.point.group.name,
    				"GROUPING_VALUE": event.point.group.value
    		};

    		return parameters;

       }

       if( $scope.ngModel.content.chartTemplate.CHART.type==="PARALLEL"){
    	   var parameters = {

    				"CATEGORY_VALUE":event.selectParam_cross.categoryValue,
    				"CATEGORY_NAME": event.selectParam_cross.categoryName,
    				"GROUPING_NAME": event.selectParam_cross.groupingCategoryName,
    				"GROUPING_VALUE": event.selectParam_cross.groupingCategoryValue
    		};

    		return parameters;

       }
       if( $scope.ngModel.content.chartTemplate.CHART.type==="TREEMAP"){
    	   var parameters = {
    				"SERIE_NAME": event.point.series.name,
    				"SERIE_VALUE":event.point.value,
    				"CATEGORY_VALUE":event.point.name,
    				"CATEGORY_NAME": $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY[(event.point.id.match(new RegExp("_", "g")) ).length-1].name
    		};

    		return parameters;
       }
       if( $scope.ngModel.content.chartTemplate.CHART.type==="WORDCLOUD"){
    	   var parameters = {
    				"SERIE_NAME": event.selectParam_cross.serieName,
    				"SERIE_VALUE":event.selectParam_cross.serieValue,
    				"CATEGORY_VALUE":event.selectParam_cross.categoryValue,
    				"CATEGORY_NAME": event.selectParam_cross.categoryName,
    		};

    	   if(event.selectParam_cross.categoryId){
    		   parameters["CATEGORY_ID"]= event.selectParam_cross.categoryId;
    	   }else{
    		   parameters["CATEGORY_ID"]= event.selectParam_cross.categoryValue;
    	   }

    	   return parameters;
       }

       else if( $scope.ngModel.content.chartTemplate.CHART.type==="SUNBURST"){
        	   var parameters = event.selectParam_cross;

        		return parameters;
       }

       else if($scope.ngModel.content.chartTemplate.CHART.type==="SCATTER"){
    	   var parameters = {
    				"SERIE_NAME": event.point.series.name,
    				"SERIE_VALUE":event.point.y,
    				"CATEGORY_VALUE": $scope.ngModel.content.chartTemplate.CHART.dateTime ? date_format : event.point.category.name,
    				"CATEGORY_NAME": $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.name
    		};

    	   	return parameters;
       }
       else if($scope.ngModel.content.chartTemplate.CHART.type==="CHORD"){
    	   var parameters = {
    				"SERIE_NAME": event.SERIE_NAME,
    				"SERIE_VALUE":event.SERIE_VALUE,
    				"CATEGORY_VALUE":  event.CATEGORY_VALUE,
    				"CATEGORY_NAME": event.CATEGORY_NAME,
    			};

    	   return parameters;
       }
       var parameters = {
			"SERIE_NAME": event.point.series.name,
			"SERIE_VALUE":event.point.y,
			"CATEGORY_VALUE": $scope.ngModel.content.chartTemplate.CHART.dateTime ? date_format : event.point.name,
			"CATEGORY_NAME": $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.name
		};

		return parameters;
	}
};

function setAggregationsOnChartEngine(wconf,sbiModule_util){
	var chartsForGrouping = ["bar","line", "radar","bubble"]
	var aggregations = [];
	if(!wconf.chartTemplate.hasOwnProperty("CHART")){
		wconf.chartTemplate = {"CHART":wconf.chartTemplate};
	}
	var chartTemplate = wconf.chartTemplate;
	if(chartTemplate && chartTemplate.CHART && chartTemplate.CHART.VALUES) {

		if(chartTemplate.CHART.VALUES.SERIE) {

			var chartSeries = chartTemplate.CHART.VALUES.SERIE;

			for(var i = 0; i < chartSeries.length; i++){
				var index = sbiModule_util.findInArray(wconf.columnSelectedOfDatasetAggregations, 'alias', chartSeries[i].column);
				var obj = {};
				obj['name'] = chartSeries[i].column;
				obj['aggregationSelected'] = wconf.columnSelectedOfDatasetAggregations[index].aggregationSelected;
				obj['alias'] = chartSeries[i].name + '_' + obj.aggregationSelected;
				obj['aliasToShow'] = obj['alias'];
				obj['fieldType'] = "MEASURE";
				obj['orderType'] = chartSeries[i].orderType;
				obj['orderColumn'] = chartSeries[i].column;
				if(wconf.columnSelectedOfDatasetAggregations[index].isCalculated) {
					obj['isCalculated'] = wconf.columnSelectedOfDatasetAggregations[index].isCalculated;
					obj['formula'] = wconf.columnSelectedOfDatasetAggregations[index].formula;
					obj['datasetOrTableFlag'] = wconf.columnSelectedOfDatasetAggregations[index].datasetOrTableFlag;
				}
				aggregations.push(obj);
			}

		}

		if(chartTemplate.CHART.VALUES.CATEGORY){

			var chartCategory= chartTemplate.CHART.VALUES.CATEGORY;

			if(Array.isArray(chartCategory)){
				for(var i = 0; i < chartCategory.length; i++){

					var obj = {};
					obj['name'] = chartCategory[i].column;
					obj['alias'] = chartCategory[i].name;
					obj['aliasToShow'] = obj['alias'];
					obj['fieldType'] = "ATTRIBUTE";
					obj['orderType'] = chartCategory[i].orderType;
					obj['orderColumn'] = chartCategory[i].orderColumn;
					aggregations.push(obj);
				}
			} else {
				if(chartCategory.name=="" || chartCategory.name==null || chartCategory.name==undefined){
					chartCategory.name=chartCategory.column
				}
				var obj = {};
				obj['name'] = chartCategory.column;
				obj['alias'] = chartCategory.name;
				obj['aliasToShow'] = chartCategory.alias;
				obj['orderType'] = chartCategory.drillOrder && chartCategory.drillOrder[chartCategory.column] ? chartCategory.drillOrder[chartCategory.column].orderType : chartCategory.orderType ;
				obj['orderColumn'] =  chartCategory.drillOrder && chartCategory.drillOrder[chartCategory.column] ? chartCategory.drillOrder[chartCategory.column].orderColumn : chartCategory.orderColumn ;
				obj['fieldType'] = "ATTRIBUTE";

				aggregations.push(obj);

				if(chartTemplate.CHART.type.toLowerCase()=="bubble" || (chartsForGrouping.indexOf(chartTemplate.CHART.type.toLowerCase() )>-1) && ( chartTemplate.CHART.groupCategories || chartTemplate.CHART.groupSeries || chartTemplate.CHART.groupSeriesCateg) && chartCategory.groupby!=""){
					var subs = "";
					if (chartCategory.groupby.indexOf(',') == -1) {
						subs = chartCategory.groupby
					}

					else {
						subs = angular.copy(chartCategory.groupby.substring(0, chartCategory.groupby.indexOf(',')));
					}
					if(subs!=""){
						var groupby = {};
						groupby['name'] = subs;
						groupby['alias'] = subs;
						groupby['aliasToShow'] = subs;
						groupby['fieldType'] = "ATTRIBUTE";
						if(chartCategory.drillOrder){
							groupby['orderType'] = chartCategory.drillOrder[subs] ? chartCategory.drillOrder[subs].orderType : obj.orderType ;
							groupby['orderColumn'] = chartCategory.drillOrder[subs] ? chartCategory.drillOrder[subs].orderColumn : (chartCategory.groupby || obj.orderColumn);
						} else {
							groupby['orderType'] = chartCategory.orderType ? chartCategory.orderType : "";
							groupby['orderColumn'] = chartCategory.orderColumn ? chartCategory.orderColumn : "";
						}
						aggregations.push(groupby);
					}

				}
			};

		}
	}
	wconf.columnSelectedOfDataset = aggregations;
}

// this function register the widget in the cockpitModule_widgetConfigurator
// factory
addWidgetFunctionality("chart",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true, 'drillable' : false});

})();
