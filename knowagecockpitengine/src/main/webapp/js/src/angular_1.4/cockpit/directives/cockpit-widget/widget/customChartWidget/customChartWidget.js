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
		cockpitModule_properties,
		sbiModule_user,
		sbiModule_translate,
		datastore
		){
	$scope.datastore = datastore;
	$scope.translate = sbiModule_translate;
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
		$scope.showWidgetSpinner();
		$scope.refreshWidget(null, 'init');
	}
	datastore = angular.copy(datastore)
	datastore.clickManager = function(column,value){
		$scope.doSelection(column,value);
	}

	$scope.refresh = function(element,width,height, datasetRecords,nature){
		datastore.variables = cockpitModule_properties.VARIABLES;
		datastore.profile = sbiModule_user.profileAttributes;
		$scope.jsError = false;
		$scope.showWidgetSpinner();
		var thisElement = angular.element( document.querySelector( '#w'+$scope.ngModel.id+' .htmlRenderer' ) )[0];
		thisElement.innerHTML = '';
		thisElement.innerHTML = "<style>"+ $sce.trustAsCss($scope.ngModel.css.code) + "</style>";
		thisElement.innerHTML += $sce.trustAsHtml($scope.ngModel.html.code);
		// execute JS code
		var scrajs = thisElement.getElementsByTagName('kn-import');
		if(scrajs.length > 0){
			var toLoad = scrajs.length;
			var loaded = 0;
			var updateLoaded = function(){
				loaded++;
			}
			for(var k = 0; k < scrajs.length; k++) {
		        var scr = document.createElement("script");
		        scr.type = "text/javascript";
		        scr.src = scrajs[k].attributes[0].textContent;
				scr.addEventListener("load", updateLoaded);
		        thisElement.appendChild(scr);          
		    }
		}	 
	    
		function setJs(){
			try {
				var tempJS = $sce.trustAs($sce.JS, $scope.ngModel.js.code).$$unwrapTrustedValue();
				if(!tempJS.match(/(\$scope|\$destroy|datastore\.setData)/g)) eval(tempJS);
				else {
					$scope.jsError = $scope.translate.load('kn.cockpit.custom.code.unsafe');
				}
				$scope.hideWidgetSpinner();
				if(nature == 'init'){
					$timeout(function(){
						$scope.widgetIsInit=true;
						cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
					},500);
				}
			}catch(e){
				$scope.hideWidgetSpinner();
				$scope.jsError = e;
			}
		}
		
		function jsLoadSemaphore(){
			if(loaded == toLoad){
				setJs();
			}else {
				$timeout(function(){
					jsLoadSemaphore();
				},1000)
			}
		}
	
		if(datasetRecords || nature == 'fullExpand'){
			if(datasetRecords) datastore.setData(datasetRecords);
			if($scope.ngModel.js) {
				if(toLoad){
					jsLoadSemaphore();
				}else setJs();
			}
		}
	}

	$scope.reinit = function(){
		$scope.showWidgetSpinner();
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
