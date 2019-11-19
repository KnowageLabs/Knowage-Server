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
			sbiModule_translate,
			sbiModule_restServices,
			cockpitModule_properties,
			cockpitModule_generalServices,
			cockpitModule_datasetServices,
			cockpitModule_widgetSelection) {

		$scope.getTemplateUrl = function (template) {
	  		return cockpitModule_generalServices.getTemplateUrl('pythonWidget', template);
	  	}

		$scope.refresh = function (element, width, height, datasetRecords, nature) {
			$scope.showWidgetSpinner();
			if(nature == 'init') {
				$timeout(function () {
					$scope.widgetIsInit = true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				}, 500);
			}
			// if address of python is not set yet then set it and call sendData()
			if ($scope.pythonAddress == undefined) {
				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.promiseGet('2.0/configs/label', 'PYTHON_ADDRESS')
				.then(function(response){
					$scope.pythonAddress = response.data;
					$scope.sendData();
				}, function(error){
					//todo
				});
			}
			else { //python address already set so just call sendData()
				$scope.sendData();
			}
			$scope.hideWidgetSpinner();
		}

		$scope.reinit = function() {
			$scope.refreshWidget();
		}

		$scope.createIframe = function () {
			// get <div> associated to this bokeh application
			var element = angular.element(document.querySelector('#w' + $scope.ngModel.id + ' #bokeh'));
			// create an iframe and append it to the <div>
			var iframe = document.createElement('iframe');
			iframe.id = "bokeh_" + $scope.ngModel.id;
			iframe.classList.add("layout-fill");
			element.append(iframe);
			// write content inside the iframe
			document.getElementById(iframe.id).contentWindow.document.open();
			document.getElementById(iframe.id).contentWindow.document.write($scope.pythonOutput);
			document.getElementById(iframe.id).contentWindow.document.close();
		}

		$scope.buildAggregations = function (meta, dataset_label) {
			aggregations = {"measures": [], "categories": [], "dataset": dataset_label};
			for (i=0; i<meta.length; i++) {
				x = meta[i];
				if (x.fieldType == "MEASURE") {
					item = {"id": x.name, "alias": x.alias, "columnName": x.name, "orderType": "", "funct": "SUM", "orderColumn": x.name}
					aggregations.measures.push(item)
				}
				else if (x.fieldType == "ATTRIBUTE") {
					item = {"id": x.name, "alias": x.alias, "columnName": x.name, "orderType": "", "funct": "NONE"}
					aggregations.categories.push(item)
				}
			}
			return aggregations;
		}

		$scope.sendData = function () { //send code and data to python and retrieve result as img or html/js
			//get user_id from parameters and use it for authentication in python
			url_string = window.location.href
			var url = new URL(url_string);
			var encodedUserId = url.searchParams.get("user_id");
			//if there is a dataset selected save its label
			if ($scope.ngModel.dataset != undefined && !angular.equals({}, $scope.ngModel.dataset)) {
				var dataset = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
				var selections = cockpitModule_datasetServices.getWidgetSelectionsAndFilters($scope.ngModel, dataset);
				var dataset_label = dataset.label;
				var aggregations = $scope.buildAggregations(dataset.metadata.fieldsMeta, dataset_label);
			}
			else { //no dataset selected
				var dataset_label = "";
				var selections = "";
				var aggregations = "";
			}

		    $http({
		        url: $scope.pythonAddress.valueCheck + $scope.ngModel.pythonOutputType,
		        method: "POST",
		        headers: {'Content-Type': 'application/json',
		        		  'Authorization': encodedUserId,
		        		  'Dataset-name': dataset_label,
		        		  'parameters': [] },
		        data: { 'script' : $scope.ngModel.pythonCode,
		        		'output_variable' : $scope.ngModel.pythonOutput,
		        		'widget_id' :  $scope.ngModel.id,
		        		'datastore_request': JSON.stringify({"aggregations": aggregations, 'selections': selections})}
		    })
		    .then(function(response) { //success
		            $scope.pythonOutput = $sce.trustAsHtml(response.data);
		            if ($scope.ngModel.pythonOutputType == 'bokeh') {
						$scope.createIframe();
					}
		    },
		    function(response) { //failed
		    	$scope.pythonOutput = 'Error: ' + $sce.trustAsHtml(response.data);
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
