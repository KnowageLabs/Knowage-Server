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
 * @authors Alessandro Piovani (alessandro.piovani@eng.it)
 * v0.0.1
 * 
 */
(function() {

angular.module('cockpitModule')
.directive('cockpitFiltersConfiguration',function(){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-filters-configuration/templates/cockpitFiltersConfiguration.html',
		   controller: cockpitFiltersControllerFunction,
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

function cockpitFiltersControllerFunction($scope,cockpitModule_widgetServices,
		cockpitModule_properties,cockpitModule_template,$mdDialog,sbiModule_translate,sbiModule_restServices,
		cockpitModule_gridsterOptions,$mdPanel,cockpitModule_widgetConfigurator,$mdToast,
		cockpitModule_generalServices,cockpitModule_widgetSelection,cockpitModule_datasetServices,$rootScope){
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.cockpitModule_widgetServices=cockpitModule_widgetServices;
	$scope.cockpitModule_datasetServices=cockpitModule_datasetServices;
	
	$scope.openGeneralConfigurationDialog=function(){
		cockpitModule_generalServices.openGeneralConfiguration();
	}
	
	$scope.openDataConfigurationDialog=function(){
		cockpitModule_generalServices.openDataConfiguration();
	}
	
	$scope.fabSpeed = {
			isOpen : false
	}
	
	$scope.showFilters=false;
	$scope.filtersToReturn=[]; // format [{colName : "..."  ,  filterVals : ["filterStr1" , "filterStr2", ... ]}   ,   {colName : "..."  ,  filterVals : [...] }]
	
	$scope.localDS={};
	$scope.columnNames=[];
	
	
	if($scope.$parent.model.filters==undefined)
	{	
		if($scope.$parent.model.dataset!=undefined && $scope.$parent.model.dataset.dsId!=undefined)
		{
			$scope.showFilters=true;
			$scope.selectedDsId=$scope.$parent.model.dataset.dsId;	
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.selectedDsId), $scope.localDS);
			for(var i=0;i<$scope.localDS.metadata.fieldsMeta.length;i++)
			{
				var objToInsert={};
				objToInsert.filterVals=[];
				objToInsert.colName=$scope.localDS.metadata.fieldsMeta[i].name;
				
				$scope.filtersToReturn.push(objToInsert);
				$scope.columnNames.push($scope.localDS.metadata.fieldsMeta[i].name);
			}
			$scope.$parent.model.filters=$scope.filtersToReturn;	

		}

	}
	else
	{
		$scope.filtersToReturn=$scope.$parent.model.filters;
	}
};


})();