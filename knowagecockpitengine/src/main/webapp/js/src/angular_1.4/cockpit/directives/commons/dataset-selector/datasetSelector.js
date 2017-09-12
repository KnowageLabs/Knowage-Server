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
(function(){

angular.module('cockpitModule').directive('datasetSelector',function($compile){
	   return{
		   templateUrl: baseScriptPath+ '/directives/commons/dataset-selector/templates/datasetSelector.html',
		   transclude: true,
		   replace: true,		  
		   scope:{
			   ngModel:"=",
			   onChange:"&",
		   },
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	
                    }
                };
		   	},
		    controller: datasetSelectorControllerFunction,
		   
	   }
});

function datasetSelectorControllerFunction($scope,cockpitModule_datasetServices,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();
	$scope.addNewDataset=function(){
		 cockpitModule_datasetServices.addDataset(undefined,$scope.availableDatasets,false,true)
		 .then(function(data){
			 $scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();
			 $scope.ngModel=data.id.dsId;
			 $scope.onChange({dsId:data.id.dsId});
		 });
	}
};

})();