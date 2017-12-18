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
 * @authors Giulio Gavardi (Giulio.Gavardi@eng.it)
 * v0.0.1
 * 
 */
(function() {
angular.module('cockpitModule').directive('cockpitCrossConfigurator',function($compile){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-cross-configurator/template/cockpitCrossConfigurator.html',
		   controller: cockpitCrossConfiguratorControllerFunction,
		   transclude: true,
		   scope: true,

		   	compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	 
                    	scope.ngModel = scope.$parent.$eval(attrs.ngModel);
                    	
                    	if(scope.ngModel==undefined){
                    		scope.$parent.$eval(attrs.ngModel+"={}");
                    		scope.ngModel = scope.$parent.$eval(attrs.ngModel);
                    	} 
                    	
                    	if(attrs.widget!=undefined){
                    		scope.isWidget=true;
                    		scope.initModel();
                    	}else{
                    		scope.isWidget=false;
                    	}
                    
                    	 transclude(scope, function (clone, scope) {
                             angular.element(element[0].querySelector("md-content")).prepend(clone);
                         });
                    
                    }
                };
		   	}
	   }
});

angular.module('cockpitModule').directive('cockpitCrossCustomWidgetConfigurator',function($compile){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-cross-configurator/template/cockpitCrossCustomWidgetConfigurator.html',
		   transclude: true,
		   replace: true,
		   controller: function(){},
		   controllerAs : "cscwc_controller",
		   scope: true,
		   compile: function (tElement, tAttrs, transclude) {
             return {
                 pre: function preLink(scope, element, attrs,ctrl, transclud) {
                 },
                 post: function postLink(scope, element, attrs,ctrl, transclud) {
                	 
                	ctrl.labelWidget = scope.$parent.$eval(attrs.label);
                	ctrl.layoutType = attrs.layout;
                	 if(ctrl.layoutType==undefined){
                		 ctrl.layoutType="row";
                	 }
                	 transclude(scope, function (clone, scope) {
                         angular.element(element[0].querySelector("md-card>md-card-content")).append(clone);
                     });
                 }
             };
		   	}
	   }
});

function cockpitCrossConfiguratorControllerFunction($scope,sbiModule_translate,cockpitModule_template,
		cockpitModule_generalOptions,cockpitModule_datasetServices, cockpitModule_properties, cockpitModule_documentServices, cockpitModule_crossServices){
	$scope.translate=sbiModule_translate;
	$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.angular=angular;
	$scope.cockpitCross={};
	$scope.localDataset = {};
	$scope.crossNavigations = cockpitModule_crossServices.getCrossList();
	$scope.chartProperties=[];
	
	$scope.crossTable = $scope.model != undefined && $scope.model.type === 'table';
	
	$scope.crossChart = $scope.localModel != undefined && $scope.localModel.wtype === 'chart'; 
	
	if($scope.crossChart){
		var chart = $scope.localModel.chartTemplate.CHART;
		if(!chart){
			chart = $scope.localModel.chartTemplate;
		}
		$scope.chartProperties=cockpitModule_crossServices.getChartParameters(chart.type, chart);
	}else{
	   if($scope.model.dataset!=undefined && $scope.model.dataset.dsId != undefined){
		   angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
	   }else{
		   $scope.model.dataset= {};
		   //angular.copy([], $scope.model.dataset.metadata.fieldsMeta); 
	   }
	}
	angular.copy(cockpitModule_template.configuration.cross,$scope.cockpitCross);
	
	$scope.initModel=function(){
		angular.copy(angular.merge({},$scope.cockpitCross,$scope.ngModel),$scope.ngModel)
	}
	
	$scope.resetCross=function(){
		$scope.ngModel.cross=$scope.cockpitCross;
		angular.copy($scope.cockpitCross,$scope.ngModel.cross);
	}
}

})();