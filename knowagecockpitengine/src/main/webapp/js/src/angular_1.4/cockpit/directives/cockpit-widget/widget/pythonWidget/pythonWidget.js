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
 * @author Marco Balestri <marco.balestri@eng.it>
 */

(function () {
	angular
		.module('cockpitModule')
		.config(function($locationProvider) {
			$locationProvider.html5Mode(true);
		})
		.directive('cockpitPythonWidget', function () {
			return {
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/pythonWidget/templates/pythonWidgetTemplate.html',
				controller: cockpitPythonWidgetControllerFunction,
				compile: function (tElement, tAttrs, transclude) {
					return {
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							element.ready(function () {
							scope.initWidget();
							});
						}
					};
				}
			}
		})

		.directive('bindHtmlCompile', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                scope.$watch(function () {
                    return scope.$eval(attrs.bindHtmlCompile);
                }, function (value) {
                    element.html(value && value.toString());
                    var compileScope = scope;
                    if (attrs.bindHtmlScope) {
                        compileScope = scope.$eval(attrs.bindHtmlScope);
                    }
                    $compile(element.contents())(compileScope);
                });
            }
        };
    }])

	function cockpitPythonWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdPanel,
			$q,
			$timeout,
			$http,
			$sce,
			$location,
			sbiModule_translate,
			sbiModule_restServices,
			sbiModule_config,
			cockpitModule_properties,
			cockpitModule_analyticalDrivers,
			cockpitModule_generalServices,
			cockpitModule_datasetServices,
			cockpitModule_widgetSelection,
			cockpitModule_template,
			sbiModule_user) {

		$scope.getTemplateUrl = function (template) {
	  		return cockpitModule_generalServices.getTemplateUrl('pythonWidget', template);
	  	}

		$scope.refresh = function (element, width, height, datasetRecords, nature) {
			$scope.showWidgetSpinner();
			if(nature == 'init') {
				$timeout(function () {
					$scope.widgetIsInit = true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				}, 2000);
			}
			$scope.documentId = cockpitModule_properties.DOCUMENT_ID;
			if ($scope.ngModel.pythonAddress == undefined) {
				$scope.pythonOutput = 'Configure python address';
			} else {
				$scope.sendData();
			}
		}

		$scope.reinit = function() {
			$scope.refreshWidget();
		}

		$scope.crossNavigation = function () {
			$scope.doSelection(null, null, null, null, null, null, $scope.ngModel.dataset.dsId, null);
		}

		$scope.createIframe = function () {
			// get <div> associated to this bokeh application
			var element = angular.element(document.querySelector('#w' + $scope.ngModel.id + ' #bokeh'));
			// remove old iframe if it exists
			var oldElem = document.getElementById("bokeh_" + $scope.ngModel.id)
			if (oldElem) oldElem.remove();
			// create a new iframe and append it to the <div>
			var iframe = document.createElement('iframe');
			iframe.height="100%";
			iframe.width="100%";
			iframe.id = "bokeh_" + $scope.ngModel.id;
			iframe.classList.add("layout-fill");
			element.append(iframe);
			// write content inside the iframe
			document.getElementById(iframe.id).contentWindow.document.open();
			document.getElementById(iframe.id).contentWindow.document.write($scope.pythonOutput);
			document.getElementById(iframe.id).contentWindow.document.close();
		}

		$scope.buildAggregations = function (columnSelectedOfDataset, dataset_label) {
			aggregations = {"measures": [], "categories": [], "dataset": dataset_label};
			for (i=0; i<columnSelectedOfDataset.length; i++) {
				x = columnSelectedOfDataset[i];
				if (x.fieldType == "MEASURE") {
					item = {"id": x.name, "alias": x.alias, "columnName": x.name, "orderType": "", "funct": x.aggregationSelected, "orderColumn": x.name}
					if (x.isCalculated) item.formula = x.formula
					aggregations.measures.push(item)
				}
				else if (x.fieldType == "ATTRIBUTE") {
					item = {"id": x.name, "alias": x.alias, "columnName": x.name, "orderType": "", "funct": "NONE"}
					aggregations.categories.push(item)
				}
			}
			return aggregations;
		}

		$scope.sendData = function () {
			if (cockpitModule_properties.EDIT_MODE == true) {
				$scope.sendDataEditMode();
	    	}
	    	else {
	    		$scope.sendDataViewMode();
	    	}
		}

		$scope.setPythonParameters = function () {
			//get user_id from parameters and use it for authentication in python
			$scope.encodedUserId = sbiModule_user.userUniqueIdentifier;
	        $scope.drivers = cockpitModule_analyticalDrivers;
			//if there is a dataset selected save its label
			if ($scope.ngModel.dataset != undefined && !angular.equals({}, $scope.ngModel.dataset)) {
				$scope.dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
				$scope.selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters($scope.ngModel, $scope.dataset);
				$scope.dataset_label = $scope.dataset.label;
				$scope.aggregations = $scope.buildAggregations($scope.ngModel.content.columnSelectedOfDataset, $scope.dataset_label);
				$scope.parameters = cockpitModule_datasetServices.getDatasetParameters($scope.ngModel.dataset.dsId);
				// if parameter has only one value, it must not be enclosed in array
				for (var parKey in $scope.parameters) {
					var parValues = $scope.parameters[parKey];
					if (Array.isArray(parValues) && parValues.length == 1) {
						$scope.parameters[parKey] = $scope.parameters[parKey][0];
					}
				}
			}
			else { //no dataset selected
				$scope.selections = "";
				$scope.aggregations = "";
				$scope.parameters = "";
			}
		}

		$scope.sendDataEditMode = function () { //send code and data to python and retrieve result as img or html/js
			$scope.setPythonParameters();
		    $http({
		        url: $scope.ngModel.pythonAddress + "/widget/edit/" + $scope.ngModel.pythonOutputType,
		        method: "POST",
		        headers: {'Content-Type': 'application/json',
		        		  'Knowage-Authorization': $scope.encodedUserId},

		        data: { 'dataset': $scope.dataset_label,
		        		'script' : $scope.ngModel.pythonCode,
		        		'output_variable' : $scope.ngModel.pythonOutput,
		        		'widget_id' :  $scope.ngModel.id,
		        		'document_id' :  $scope.documentId,
		        		"drivers" : $scope.drivers,
		        		'datastore_request': JSON.stringify({"aggregations": $scope.aggregations, 'parameters': $scope.parameters,'selections': $scope.selections})}
		    })
		    .then(function(response) { //success
		            $scope.pythonOutput = $sce.trustAsHtml(response.data);
		            if ($scope.ngModel.pythonOutputType != 'img') {
						$scope.createIframe();
					}
		            $scope.hideWidgetSpinner();
		    },
		    function(response) { //failed
		    	$scope.pythonOutput = 'Error: ' + $sce.trustAsHtml(response.data);
		    	if ($scope.ngModel.pythonOutputType != 'img') {
					$scope.createIframe();
				}
		    	$scope.hideWidgetSpinner();
		    });

		}

		$scope.sendDataViewMode = function () { //send code and data to python and retrieve result as img or html/js
			$scope.setPythonParameters();
		    $http({
		        url: $scope.ngModel.pythonAddress + "/widget/view/" + $scope.ngModel.pythonOutputType,
		        method: "POST",
		        headers: {'Content-Type': 'application/json',
		        		  'Knowage-Authorization': $scope.encodedUserId},

		        data: { 'dataset': $scope.dataset_label,
		        		'output_variable' : $scope.ngModel.pythonOutput,
		        		'widget_id' :  $scope.ngModel.id,
		        		'document_id' :  $scope.documentId,
		        		"drivers" : $scope.drivers,
		        		'datastore_request': JSON.stringify({"aggregations": $scope.aggregations, 'parameters': $scope.parameters, 'selections': $scope.selections})}
		    })
		    .then(function(response) { //success
		            $scope.pythonOutput = $sce.trustAsHtml(response.data);
		            if ($scope.ngModel.pythonOutputType != 'img') {
						$scope.createIframe();
					}
		            $scope.hideWidgetSpinner();
		    },
		    function(response) { //failed
		    	$scope.pythonOutput = 'Python Error';
		    	if ($scope.ngModel.pythonOutputType != 'img') {
					$scope.createIframe();
				}
		    	$scope.hideWidgetSpinner();
		    });

		}

		$scope.init = function (element, width, height) {
			$scope.showWidgetSpinner();
			$scope.refresh(element, width, height, null, 'init');
		}

		$scope.getOptions = function () {
			var obj = {};
			obj["page"] = 0;
			obj["itemPerPage"] = 10;
			obj["type"] = 'python';
			return obj;
		}

		$scope.editWidget = function (index) {
			var finishEdit=$q.defer();
			var config = {
				attachTo:  angular.element(document.body),
				controller: pythonWidgetEditControllerFunction,
				disableParentScroll: true,
				templateUrl: $scope.getTemplateUrl('pythonWidgetEditPropertyTemplate'),
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: false,
				locals: {finishEdit: finishEdit, model: $scope.ngModel},
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}

	}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("python", {'initialDimension': {'width':5, 'height':5}, 'updatable':true, 'clickable':true});

})();
