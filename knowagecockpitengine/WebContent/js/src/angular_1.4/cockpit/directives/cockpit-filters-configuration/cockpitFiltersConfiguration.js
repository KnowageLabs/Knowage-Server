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
			   ngModelShared: '='   
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
	
	$scope.localDS={};
	$scope.columnNames=[];
		
	$scope.updateFilters=function(dsId)
	{
		$scope.ngModelShared.filters=[]; // format [{colName : "..."  ,  filterVals : ["filterStr1" , "filterStr2", ... ]}   ,   {colName : "..."  ,  filterVals : [...] }]
		//$scope.selectedDsId=$scope.ngModelShared.dataset.dsId;	
		$scope.selectedDsId=dsId;	
		angular.copy(cockpitModule_datasetServices.getDatasetById($scope.selectedDsId), $scope.localDS);
		for(var i=0;i<$scope.localDS.metadata.fieldsMeta.length;i++)
		{
			var objToInsert={};
			objToInsert.filterVals=[];
			objToInsert.colName=$scope.localDS.metadata.fieldsMeta[i].name;
			
			$scope.ngModelShared.filters.push(objToInsert);
		}
   }
	
	
	//for chartWidget
	$scope.$watch("ngModelShared.datasetId", function(newValue, oldValue) {
		
		if(oldValue==newValue)
		{	
			if(oldValue!=undefined) //not initialization phase
			{	
				if($scope.ngModelShared.filters==undefined)	//if filters are not defined, I create them
				{
					$scope.updateFilters($scope.ngModelShared.datasetId);	
				}
			}
			else{ //initialization phase, there is no dataset
				
			}
		}
		else
		{
			$scope.updateFilters($scope.ngModelShared.datasetId);	
		}	
	});
	
	
	
	
	//for tableWidget
	
	$scope.$watch("ngModelShared.dataset.dsId", function(newValue, oldValue) {
		var filterFound=false;
		if(oldValue==newValue)
		{	
			if(oldValue!=undefined) //not initialization phase
			{	
				if($scope.ngModelShared.filters==undefined)	//if filters are not defined, I create them
				{
					$scope.updateFilters($scope.ngModelShared.dataset.dsId);	
				}
				else
				{
					$scope.localDSforFilters={};
					angular.copy(cockpitModule_datasetServices.getDatasetById($scope.ngModelShared.dataset.dsId), $scope.localDSforFilters);
					
					for(var i=0;i<$scope.localDSforFilters.metadata.fieldsMeta.length;i++)  //columns
					{	
						var obj = $scope.localDSforFilters.metadata.fieldsMeta[i];
						
						if($scope.ngModelShared.filters!=undefined){			
							filterFound=false;
							var filterToAdd={};
							for(var j=0;j<$scope.ngModelShared.filters.length;j++){        //filters
								if($scope.ngModelShared.filters[j].colName==obj.name){
									filterFound=true;
								}
							}
						}
						if(!filterFound){
							filterToAdd.colName=obj.name;
							filterToAdd.filterVals=[];
							$scope.ngModelShared.filters.push(filterToAdd);		
						}
					}
					
					
					for(var k=0;k<$scope.ngModelShared.filters.length;k++)   //filters
					{       
						filterFound=false;
						var f = $scope.ngModelShared.filters[k];
						for(var l=0;l<$scope.localDSforFilters.metadata.fieldsMeta.length;l++)  //columns
						{
							
							if($scope.localDSforFilters.metadata.fieldsMeta[l].name==f.colName)
							{
								filterFound=true;
								
							}
							
							
						}
						if(!filterFound) //if filter is not in columns
						{
							$scope.ngModelShared.filters.splice(k,1); //remove filter from filter list
						}	
						
					}
					
					
				}	
			}
			else{ //initialization phase, there is no dataset
				
			}
		}
		else
		{
			$scope.updateFilters($scope.ngModelShared.dataset.dsId); 	
		}
	});
	

	

};


})();