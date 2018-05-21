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
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 *
 */
(function() {
angular.module('cockpitModule')

/*
 * This directive acts like an Ext.form.FormPanel
 * Creates an IFrame with an URL called by posting parameters
 *
 * */
.directive('postIframe',function(sbiModule_restServices){
	var i = 0;
	return {
		restrict: 'E',
	    templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/chartWidget/templates/postIframe.html',
	    scope:{
	    	   datasetLabel:'=',
	    	   datasetId:'=',
	    	   isCockpitEng:'=',
	    	   localMod:'='
	    	  },
	    link: function(scope, elem, attrs) {
	    	var genId = i++;
	    	scope.iframeName = attrs.id + "_frameName" + genId;
	    	scope.formId = attrs.id + "_formId" + genId;
	    	scope.iframeId = attrs.iframeId ? attrs.iframeId : (attrs.id + "_iframeId" + genId);
	    	scope.iframeContent = '';
	    },
	    controller: function($scope, $element, $http, sbiModule_restServices, $httpParamSerializerJQLike){
	    	$scope.iframeContent = '';
	    	$scope.updateAction = function(actionUrl){
	    		$scope.actionUrl = actionUrl + "&SBI_EXECUTION_ID=" + (new Date().getTime() + '_' + (i++));
	    	};

	    	$scope.showWidgetSpinner=function(){
	    		  $scope.confSpinner=true;
	    	  }
	    	$scope.updateParameters = function(parameters){
	    		$scope.formParameters = parameters;
	    	};

	    	var loadPageIntoIframe = function(actionUrl, parameters){
	    		var iframe = $element.find('iframe')[0];
    			if(actionUrl){
    				$scope.updateAction(actionUrl);
    			}
    			if(parameters){
    				$scope.updateParameters(parameters);
    			}
    			var formId = $element[0].id + "_formId";
    			var formAction = $scope.actionUrl;
    			var form = angular.element('<form id="'+formId+'" action="'+formAction+'" method="POST" style="display:none;"></form>');
    			for(var x=0;x<$scope.formParameters.length;x++){
    				var param = $scope.formParameters[x];
    				var paramName = param.name;
    				var paramValue = JSON.stringify(param.value);
    				paramValue = paramValue.replace(/'/g, "");
    				form.append('<input type="hidden" name="' + paramName + '" value=\'' + paramValue + '\'>');
    			}
    			var doc = iframe.contentWindow.document;
    			doc.open();
    			doc.write(form.wrap(doc.createElement('div')).parent().html());
    			doc.close();
    			doc.getElementById(formId).submit();
    			$scope.showWidgetSpinner();
	    	}
	    	$scope.updateContent = function(actionUrl, parameters, nature, width, height){
	    		if(nature == 'resize' || nature == 'gridster-resized' || nature == 'fullExpand'){
					return;
				}

				// Check if service is on line
				// When dealing with CAS, first call will force web app to do login and can give some error

				/*
					author Radmila Selakovic (radmila.selakovic@mht.net)
					Checking is it case of first rendreing or updating chart
					If it is updating chart, method that will be called is
					updateData that is implemented in highcharts414Initializer.jsp
				 */

	    		var typesOfCharts= ["HEATMAP", "TREEMAP", "CHORD", "PARALLEL","SUNBURST" ,"WORDCLOUD"];
				if(nature=="refresh" && typesOfCharts.indexOf(parameters[0].value.widgetData.chartTemplate.CHART.type) == -1){
					var iframe = $element.find('iframe')[0];
		    		var wind = iframe.contentWindow;
		    		wind.updateData(parameters[0].value.widgetData);
	    		}
	    		else{
	    			$http.get(actionUrl.testUrl).then(function(){
	    				loadPageIntoIframe(actionUrl.url, parameters);
	    			},function(){
	    				showAction("Service error");
	    			});
	    		}
			};


	    }
	};
})
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
                    	scope.postIframe = element.find('post-iframe')[0];
                    	//init the widget
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
		+ "/api/1.0/chart/pages/" + service //changed by Dragan was /api/1.0/pages/
		+ "?SBICONTEXT=" + sbiModule_config.externalBasePath
		+ "&SBI_HOST=localhost"
		+ "&SBI_LANGUAGE=" + sbiModule_config.curr_language
		+ "&SBI_COUNTRY=" + sbiModule_config.curr_country
		+ "&user_id=" + sbiModule_user.userId
		,testUrl: '/' + sbiModule_config.chartEngineContextName
		+ "/api/1.0/chart/pages/executeTest"};//changed by Dragan was /api/1.0/pages/executeTest
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
		cockpitModule_widgetConfigurator,
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
		cockpitModule_widgetServices,
		cockpitModule_properties,
		cockpitModule_template,
		$mdDialog){
	$scope.property={style:{}};
	$scope.selectedTab = {'tab' : 0};
	$scope.cockpitModule_widgetSelection = cockpitModule_widgetSelection;
	//variable that contains last data of realtime dataset
	$scope.realTimeDatasetData;
	//variable that contains last data of realtime dataset not filtered by selections
	$scope.realTimeDatasetDataNotFiltered;

	if($scope.ngModel.cross==undefined){
		$scope.ngModel.cross={};
	};

	$scope.init=function(element,width,height){
		$scope.refreshWidget({type:"chart",chartInit:true},'init');
	};
	$scope.chartLibNamesConfig = chartLibNamesConfig;

	$scope.user = sbiModule_user;

	$scope.$on('changeChart', function (event, data) {
		setAggregationsOnChartEngine($scope.ngModel.content)
		$scope.$broadcast("changeChartType");
	});

	$scope.$on('changedChartType', function (event, data){
		$scope.ngModel.content.chartTemplate.CHART = data.CHART
		$scope.refreshWidget(undefined,'init', true);
	});
	$scope.refresh=function(element,width,height,data,nature, undefined, changedChartType,dataAndChartConf){
		if ($scope.ngModel.dataset){
			var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			var aggregations = cockpitModule_widgetSelection.getAggregation($scope.ngModel,dataset);

			var filtersParams = $scope.cockpitModule_widgetSelection.getCurrentSelections(dataset.name);
			if(Object.keys(filtersParams).length == 0){
				var filtersParams = $scope.cockpitModule_widgetSelection.getCurrentFilters(dataset.name);
			}

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
			if (dataset.isRealtime == true && dataset.useCache == true){
				//Refresh for Realtime datasets
				var dataToPass = data;
				//apply filters for realtime dataset
				if (nature == 'init' || nature == 'refresh'){
					dataToPass = $scope.realtimeDataManagement(data, nature);
					$scope.realTimeDatasetDataNotFiltered = angular.copy(data);
					if ($scope.realtimeSelections && $scope.realtimeSelections.length > 0){
						$scope.applyRealtimeSelections($scope.realtimeSelections,$scope)
					}
				} else {
					dataToPass = $scope.realtimeDataManagement($scope.realTimeDatasetData, nature);
				}
				$scope.$broadcast(nature,dataToPass,(dataset.isRealtime && dataset.useCache),changedChartType,dataAndChartConf,objForDrill);

			} else {
				//Refresh for Not realtime datasets
				$scope.$broadcast(nature,data, false, changedChartType,dataAndChartConf,objForDrill);
			}
		}

	};

	$scope.realtimeSelections = cockpitModule_widgetServices.realtimeSelections;
	/**
	 * Set a watcher on a variable that can contains the associative selections for realtime dataset
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

	//Check if there are associative selections and apply that to the data
	$scope.applyRealtimeSelections = function(newValue,scope){
		if (newValue.length == 0){
			//the selections are empty
			if ($scope.realTimeDatasetDataNotFiltered) {
				var originalData = angular.copy($scope.realTimeDatasetDataNotFiltered);
				originalData = $scope.realtimeDataManagement(originalData, 'selections')
				//adapt metadata
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
				//search if there are selection on the widget's dataset
				if (newValue[i].datasetId == widgetDatasetId){
					var selections = newValue[i].selections;
					//get filter on our dataset
					if (selections[widgetDataset.label]){
						var selectionsOfDataset = selections[widgetDataset.label];
						for (var columnName in selectionsOfDataset) {
							  if (selectionsOfDataset.hasOwnProperty(columnName)) {
								  var selectionsValues = selectionsOfDataset[columnName]
								  for (var z=0 ; z < selectionsValues.length ; z++){
									  var filterValue = selectionsValues[z]
									  // clean the value from the parenthesis ( )
									  filterValue = filterValue.replace(/[()]/g, '');
									  // clean the value from the parenthesis ''
									  filterValue = filterValue.replace(/['']/g, '');
									  var filterValues = []
									  filterValues.push(filterValue);

									  if(scope.realTimeDatasetData){
										  //apply the filter function
										  var columnObject = scope.getColumnObjectFromName(scope.ngModel.content.columnSelectedOfDataset,columnName);
										  //use the alias to match the filtercolumn name
										  var filterColumnname = columnObject.alias;
										  var columnType = columnObject.fieldType;
										  scope.realTimeDatasetData.rows = scope.filterRows(scope.realTimeDatasetData,columnObject,filterValues,columnType);
										  scope.realTimeDatasetData.results = scope.realTimeDatasetData.rows.length;

										  // adapt the metadata to be sent to the backend
										  var metadataFields = scope.realTimeDatasetData.metaData.fields;
										  scope.adaptMetadata(metadataFields);

										  //send broadcast for selections with data filtered by selections
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
	 * Change the header name of the metadata's fields to use the format used by the chart backend
	 * this is necessary because after a realtime update the data has the original header name from the dataset
	 * meanwhile while loading data from backend of chart the header have the name+grouping faction
	 * (Ex: TEMPERATURE_SUM instead of TEMPERATURE)
	 *
	 */
	$scope.adaptMetadata = function (metadataFields){
		for (var x=0; x < metadataFields.length; x++){
			  if (metadataFields[x].header){
				  var colObj = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,metadataFields[x].header);
				  //set the header to use the alias (ex: temperature_SUM instead of just temperature)
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
			//Do something only if the dataset is realtime, otherwise just pass the data
			if (dataset.isRealtime == true && dataset.useCache == true){
				//create a deep copy of the data, otherwise filtering on data will be spread to all the widgets
				$scope.realTimeDatasetData = angular.copy(data);

				//*** CLIENT SIDE FILTERING ***
				if ($scope.ngModel.content && $scope.ngModel.content.filters){
					var filters = $scope.ngModel.content.filters;
					for (var i=0; i < filters.length ; i++){
						//check if a filter is specified
						if (filters[i].filterVals.length > 0 ){

							var columnObject = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,filters[i].colName);
							//var filterColumnname = columnObject.alias;
							var filterValues =  filters[i].filterVals;
							var columnType = columnObject.fieldType;
							$scope.realTimeDatasetData.rows = $scope.filterRows($scope.realTimeDatasetData,columnObject,filterValues,columnType);

						}
					}
				}
				//*** CLIENT SIDE SORTING ***
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
			//search for the corresponding alias of the sortingColumn
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

			//search dataindex corresponding to the sortingField
			var dataIndex;
			if (data.metaData.fields){
				var fields = data.metaData.fields;
				for (var i=0; i< fields.length ; i++){
					//use alias or original name to catch correct field
					if (fields[i].header && (fields[i].header == sortingField ) ){
						//get corresponding dataIndex
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
	 * Returns the column object that satisfy the original name (not aliasToShow) passed as argument
	 */
	$scope.getColumnObjectFromName = function(columnSelectedOfDataset, originalName){
		for (i = 0; i < columnSelectedOfDataset.length; i++){
			if (columnSelectedOfDataset[i].name === originalName){
				return columnSelectedOfDataset[i];
			}
		}
	}

	/**
	 * Return only the objects matching the filter
	 * data: object with data and metadata
	 * columnObject: specific object of a column
	 * values: array of admissible values
	 * columnType: type (Measure/Attribute) of the column
	 */
	$scope.filterRows = function (data, columnObject, values, columnType ){
		var toReturn = [];
		var dataIndex;
		//search dataIndex
		if (data && data.metaData && data.metaData.fields){
			var fields = data.metaData.fields;
			for (var i=0; i< fields.length ; i++){
				//use alias or original name to catch correct field (because after a realtime update the header use the original name)
				if (fields[i].header && (fields[i].header == columnObject.alias || fields[i].header == columnObject.name) ){
					//get corresponding dataIndex
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
						//handle Attribute as String and Measure as number
						if (columnType == 'ATTRIBUTE'){
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
				controller: function($scope,sbiModule_translate,model,mdPanelRef,doRefresh,sbiModule_user){
					  $scope.translate=sbiModule_translate;
					  $scope.confSpinner=false;
					  $scope.somethingChanged=false;
					  $scope.localStyle=angular.copy(model.style);
					  $scope.localModel = angular.copy(model.content);
					  $scope.localModel.cross= angular.copy(model.content.cross);
					  $scope.user = sbiModule_user;

					  $scope.model= angular.copy(model);



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
							  if(!checkChartSettings()){
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
								  showChartConfiguration();
								  $scope.datasetChanged = false;
							  }
						  }
					  }
			    	  var changeDatasetFunction=function(dsId){
			    		  var ds = cockpitModule_datasetServices.getDatasetById(dsId);
			    		  if(ds){
			    			  if(ds.id.dsId != $scope.localModel.datasetId && ds.id.dsLabel != $scope.localModel.datasetLabel){
			    				  // Clearing chart configurations
			    				  delete $scope.localModel.aggregations;
			    				  delete $scope.localModel.chartTemplate;
			    				  delete $scope.localModel.columnSelectedOfDataset;
			    			  }
			    			  $scope.localModel.datasetLabel = ds.label;
			    		  }
			    	  }
			    	  var checkConfiguration=function(){

			    		  $scope.confChecked = true;

	    				  setAggregationsOnChartEngine($scope.localModel);
	    				  return true;
			    	  }



//				  		check if right number of operands have been specified depending on operator type
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

			    		  if(!$scope.localModel.hasOwnProperty('chartTemplate')){

			    			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning.misingChartDesigner"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
			    			  return;
			    		  }
			    		  var f = true;
			    		  if(document.getElementById('chartConfigurationIframe').contentWindow.hasOwnProperty('validateForm')){
			    			  var isFormValid = document.getElementById('chartConfigurationIframe').contentWindow.validateForm();

				    		  if(!isFormValid) {
				    			  return false;
				    		  }
			    		  }

				    	  var chartTemplateFake = $scope.localModel.chartTemplate.CHART ? $scope.localModel.chartTemplate.CHART : $scope.localModel.chartTemplate;
	    				  if (chartTemplateFake.type == "SCATTER" && chartTemplateFake.VALUES.SERIE.length>1) {
	    					  var allSeries = chartTemplateFake.VALUES.SERIE;
	    						var counter = 0;
	    						for (var i = 0; i < allSeries.length; i++) {
	    							if(allSeries[i].groupingFunction=="NONE"){
	    								counter++
	    							};
	    						}
	    						if(counter<chartTemplateFake.VALUES.SERIE.length){
	    							f=false;
	    						}
	    						if (counter == 0) f = true;
	    				  }

	    				  if ((chartTemplateFake.type == "BAR" || chartTemplateFake.type == "LINE") && chartTemplateFake.VALUES.SERIE.length>0) {
	    					  var allSeries = chartTemplateFake.VALUES.SERIE;
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
			    		  var attach = document.getElementById('chartConfigurationIframe').contentWindow.attachCategories;
			    		  if (typeof attach === "function") {
			    			  document.getElementById('chartConfigurationIframe').contentWindow.attachCategories();
			    			}

			          };

			    	  var saveConfiguration=function(){

			    		  if($scope.localModel.datasetId == undefined){
			    			  // Warning: Please select a dataset
			    			  showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
			    		  }
//			    		  else if (checkFilters()==false ){
//			    			  showAction($scope.translate.load('sbi.cockpit.table.errorfilters'));
//			    		  }
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

			    	  var showChartConfiguration=function(){
				    	  var widgetData = angular.extend({"datasetLabel":$scope.localModel.datasetLabel||''},$scope.localModel);
				    	  var execPar = buildParametersForExecution.edit(widgetData);
				    	  angular.element(document.getElementById("chartConfigurationIframe")).scope()
				  		  .updateContent(execPar.formAction, execPar.formParameters, 'init');
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
		var event= item.select != undefined ? item.select : item;
		var crossParameters= createCrossParameters(item);
		var chartType = $scope.ngModel.content.chartTemplate.CHART.type;
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

		if(model.cross != undefined
				&& model.cross.enable === true
				&& model.cross.column != undefined
				&& model.cross.outputParameter != undefined
				){

			var outputParameter = {};
			outputParameter[model.cross.outputParameter] = crossParameters[model.cross.column];


			// parse output parameters if enabled
			var otherOutputParameters = [];
			var passedOutputParametersList = model.cross.outputParametersList;

			for(par in passedOutputParametersList){
				var content = passedOutputParametersList[par];

				if(content.enabled == true){

					/*if(content.dataType == 'date' && content.value != undefined && content.value != ''){

						content.value = content.value.toLocaleDateString('en-US');
						content.value+= "#MM/dd/yyyy";
					}*/

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
			/*var staticParameters = [];
			if(model.cross.staticParameters && model.cross.staticParameters != ''){
				var err=false;
				try{
					var parsedStaticPars = model.cross.staticParameters.split("&");
					for(var i=0;i<parsedStaticPars.length;i++){
						var splittedPar=parsedStaticPars[i].split("=");
						if(splittedPar[0]==undefined || splittedPar[1]==undefined){err=true;}
						else{
							var toInsert = {};
							toInsert[splittedPar[0]] = splittedPar[1];
							staticParameters.push(toInsert);
						}

					}

				}catch(e){
					err=true
					console.error(e);
				}finally{
					if(err){
						 $mdDialog.show(
							      $mdDialog.alert()
									        .clickOutsideToClose(true)
								        .title(sbiModule_translate.load("sbi.cockpit.cross.staticParameterErrorFormatTitle"))
								        .content(sbiModule_translate.load("sbi.cockpit.cross.staticParameterErrorFormatMsg"))
								        //.ariaLabel('Alert Dialog Demo')
								        .ok(sbiModule_translate.load("sbi.general.continue"))
							    );
					return;
					}
					}

			}*/


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
			//for highcharts
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
				/*if($scope.ngModel.content.chartTemplate.CHART.dateTime){
					columnValue = date_format;
				}else {
					columnValue = event.point.name;
				}*/
				columnValue = event.point.name;
			}


			var category = $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY;
			var columnName = category.name


			//var d3Types = ["WORDCLOUD", "PARALLEL", "SUNBURST"];

			//if(d3Types.indexOf(chartType)<0){
			if( Array.isArray(category)){
				columnName = category[0].name;
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
			//for d3 charts
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
    				"CATEGORY_NAME": $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY[0].name
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

function setAggregationsOnChartEngine(wconf){
	var aggregations = [];
	if(!wconf.chartTemplate.hasOwnProperty("CHART")){
		wconf.chartTemplate = {"CHART":wconf.chartTemplate};
	}
	var chartTemplate = wconf.chartTemplate;
	if(chartTemplate && chartTemplate.CHART && chartTemplate.CHART.VALUES) {

		if(chartTemplate.CHART.VALUES.SERIE) {

			var chartSeries = chartTemplate.CHART.VALUES.SERIE;

			for(var i = 0; i < chartSeries.length; i++){

				var obj = {};
				obj['name'] = chartSeries[i].column;
				obj['aggregationSelected'] = chartSeries[i].groupingFunction ? chartSeries[i].groupingFunction : 'SUM';
				obj['alias'] = chartSeries[i].name + '_' + obj.aggregationSelected;
				obj['aliasToShow'] = obj['alias'];
				obj['fieldType'] = "MEASURE";
				obj['orderType'] = chartSeries[i].orderType;
				obj['orderColumn'] = chartSeries[i].column;
				aggregations.push(obj);
			}

		}

		if(chartTemplate.CHART.VALUES.CATEGORY){

			var chartCategory= chartTemplate.CHART.VALUES.CATEGORY;
			if(chartCategory.name=="" || chartCategory.name==null || chartCategory.name==undefined){
				chartCategory.name=chartCategory.column
			}
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
				var obj = {};
				obj['name'] = chartCategory.column;
				obj['alias'] = chartCategory.name;
				obj['aliasToShow'] = chartCategory.alias;
				obj['orderType'] = chartCategory.orderType;
				obj['orderColumn'] = chartCategory.orderColumn;
				obj['fieldType'] = "ATTRIBUTE";

				aggregations.push(obj);

				if( (chartTemplate.CHART.groupCategories || chartTemplate.CHART.groupSeries || chartTemplate.CHART.groupSeriesCateg) && chartCategory.groupby!=""){
					var subs = "";
					if (chartCategory.groupby.indexOf(',') == -1) {
						subs = chartCategory.groupby
					}

					else {
						subs = angular.copy(chartCategory.groupby.substring(0, chartCategory.groupby.indexOf(',')));
					}
					var groupby = {};
					groupby['name'] = subs;
					groupby['alias'] = subs;
					groupby['aliasToShow'] = subs;
					groupby['fieldType'] = "ATTRIBUTE";
					obj['orderType'] = chartCategory.orderType;
					obj['orderColumn'] = chartCategory.orderColumn;
					aggregations.push(groupby);
				}
			};

		}
	}
	wconf.columnSelectedOfDataset = aggregations;
}

//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("chart",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();