/*
ùKnowage, Open Source Business Intelligence suite
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
	angular.module('cockpitModule')
	.directive('cockpitCustomchartWidget',function(){
		return{
			templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/customChartWidget/templates/customChartWidgetTemplate.html',
			controller: cockpitCustomChartControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
						},
					post: function postLink(scope, element, attrs, ctrl, transclud) {
						element.ready(function () {
							scope.initWidget();
						});
					}
				};
			}
		}
	})

function cockpitCustomChartControllerFunction(
		$scope,
		$mdDialog,
		$mdToast,
		$timeout,
		$mdPanel,
		$q,
		$sce,
		$filter,
		cockpitModule_generalServices,
		datastore
		){
	$scope.getTemplateUrl = function(template){
		return cockpitModule_generalServices.getTemplateUrl('customChartWidget',template);
	}
	
	if(!$scope.ngModel.style) $scope.ngModel.style = {};
	
	if(!$scope.ngModel.js) {
		$scope.ngModel.css = {};
		$scope.ngModel.html = {};
		$scope.ngModel.js = {};	
	}
	
	$scope.init=function(element,width,height){
		$scope.refreshWidget(null, 'init');
	}

	$scope.refresh = function(element,width,height, datasetRecords,nature){
		$timeout(function(){
			try {
				if($scope.ngModel.js) eval($scope.ngModel.js.code);
			} catch(e){
				console.log(e);
			}
		},1000)
	}
	
	$scope.reinit = function(){
		$scope.refreshWidget();
	}
	
	
	$scope.editWidget=function(index){
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: customChartWidgetEditControllerFunction,
				disableParentScroll: true,
				templateUrl: $scope.getTemplateUrl('customChartWidgetEditPropertyTemplate'),
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: false,
				locals: {finishEdit:finishEdit,model:$scope.ngModel},
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	}


}

	/**
	 * register the widget in the cockpitModule_widgetConfigurator factory
	 */
	addWidgetFunctionality("customchart",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':true});

})();
