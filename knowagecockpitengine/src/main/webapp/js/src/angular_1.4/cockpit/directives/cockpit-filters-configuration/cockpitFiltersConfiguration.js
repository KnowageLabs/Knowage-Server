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
		$scope.translate=sbiModule_translate;

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

		$scope.operatorsTypeString = ['=','!=','IN','like','is null','is not null','min','max'];
		$scope.operatorsTypeNumber = ['=','<','>','<=','>=','IN','is null','is not null','!=','min','max','range'];
		$scope.operatorsTypeDate = ['=','IN','is null','is not null','min','max','range'];

		$scope.updateFilters=function(dsId){
			$scope.ngModelShared.filters=[]; // format [{colName : "..."  ,  filterVals : ["filterStr1" , "filterStr2", ... ]}   ,   {colName : "..."  ,  filterVals : [...] }]
			//$scope.selectedDsId=$scope.ngModelShared.dataset.dsId;
			$scope.selectedDsId=dsId;
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.selectedDsId), $scope.localDS);
			for(var i=0;i<$scope.localDS.metadata.fieldsMeta.length;i++)
			{
				var objToInsert={};
				objToInsert.filterVals=[];
				//objToInsert.filterOperator="=";
				objToInsert.filterOperator="";

				objToInsert.colName = $scope.localDS.metadata.fieldsMeta[i].name;
				objToInsert.type = $scope.localDS.metadata.fieldsMeta[i].type;

				$scope.ngModelShared.filters.push(objToInsert);
			}

			$scope.cleanQbeColumns();
		}

		/**
		 *  Update filters in chart Case
		 */

		$scope.refreshChartFilters=function(dsId){

			if(!$scope.ngModelShared.filters){
				$scope.ngModelShared.filters=[];
			}

			 // format [{colName : "..."  ,  filterVals : ["filterStr1" , "filterStr2", ... ]}   ,   {colName : "..."  ,  filterVals : [...] }]
			//$scope.selectedDsId=$scope.ngModelShared.dataset.dsId;
			$scope.selectedDsId=dsId;
			angular.copy(cockpitModule_datasetServices.getDatasetById($scope.selectedDsId), $scope.localDS);

			// if there are already filters must delete old ones and create new ones
			var metadataArray= new Array();
			for(var i=0;i<$scope.localDS.metadata.fieldsMeta.length;i++){
				metadataArray.push($scope.localDS.metadata.fieldsMeta[i].name);
			}
			var filtersArray= new Array();
			var filtersToRemoveIndexArray= new Array();

			for(var i=0;i<$scope.ngModelShared.filters.length;i++){
				var colName = $scope.ngModelShared.filters[i].colName;
				if(!metadataArray.includes(colName)){
					filtersToRemoveIndexArray.push(i);
				}
				else{
					filtersArray.push(colName);
				}
			}

			// delete no more present filters
			for(var i=filtersToRemoveIndexArray.length-1;i>=0;i--){
				$scope.ngModelShared.filters.splice(filtersToRemoveIndexArray[i], 1);
			}

			//remove all

			for(var i=0;i<$scope.localDS.metadata.fieldsMeta.length;i++)
			{
				// before inserting new filter check it is not already present
				var objToInsert={};
				objToInsert.colName=$scope.localDS.metadata.fieldsMeta[i].name;
				if(!filtersArray.includes(objToInsert.colName)){

					objToInsert.filterVals=[];
					objToInsert.filterOperator="";
					objToInsert.type=$scope.localDS.metadata.fieldsMeta[i].type;

					$scope.ngModelShared.filters.push(objToInsert);
				}
			}
		}

		$scope.eraseFilter=function(filterName){
			var filterFound = false;
			for(var j=0;j<$scope.ngModelShared.filters.length && !filterFound;j++){
				if($scope.ngModelShared.filters[j].colName==filterName){
					$scope.ngModelShared.filters[j].filterOperator = "";
					$scope.ngModelShared.filters[j].filterVals = [];
					filterFound=true;

				}
			}
		}

		//for chartWidget
		$scope.$watch("ngModelShared.datasetId", function(newValue, oldValue) {

			if(oldValue==newValue)
			{
				if(oldValue!=undefined) //not initialization phase
				{
					//if($scope.ngModelShared.filters==undefined)	//if filters are not defined, I create them
					//{
						$scope.refreshChartFilters($scope.ngModelShared.datasetId);
					//}

					$scope.cleanQbeColumns();
				}else{
					//initialization phase, there is no dataset
				}
			}else{
				$scope.updateFilters($scope.ngModelShared.datasetId);
			}
		});

		//for tableWidget
		$scope.$watch("ngModelShared.dataset.dsId", function(newValue, oldValue) {
			var filterFound=false;
			if(oldValue==newValue){
				if(oldValue!=undefined){ //not initialization phase
					if($scope.ngModelShared.filters==undefined){ //if filters are not defined, I create them
						$scope.updateFilters($scope.ngModelShared.dataset.dsId);
					}else{
						$scope.localDSforFilters={};
						angular.copy(cockpitModule_datasetServices.getDatasetById($scope.ngModelShared.dataset.dsId), $scope.localDSforFilters);

						for(var i=0;i<$scope.localDSforFilters.metadata.fieldsMeta.length;i++){ //columns
							var obj = $scope.localDSforFilters.metadata.fieldsMeta[i];

							if($scope.ngModelShared.filters!=undefined){
								filterFound=false;
								var filterToAdd={};
								for(var j=0;j<$scope.ngModelShared.filters.length;j++){ //filters
									if($scope.ngModelShared.filters[j].colName==obj.name){
										filterFound=true;
										break;
									}
								}
							}

							if(!filterFound){
								filterToAdd.colName=obj.name;
								filterToAdd.type=obj.type;
								filterToAdd.filterVals=[];
								$scope.ngModelShared.filters.push(filterToAdd);
							}
						}

						var arrayIndexToDelete = new Array();
						for(var k=0;k<$scope.ngModelShared.filters.length;k++){ //filters
							filterFound=false;
							var f = $scope.ngModelShared.filters[k];
							for(var l=0;l<$scope.localDSforFilters.metadata.fieldsMeta.length;l++){  //columns
								if($scope.localDSforFilters.metadata.fieldsMeta[l].name==f.colName){
									filterFound=true;
									break;
								}
							}
							if(!filterFound){ //if filter is not in columns
								arrayIndexToDelete.push(k);
							}
						}

						for(var k=arrayIndexToDelete.length-1;k>=0;k--){ //filters
							var index = arrayIndexToDelete[k];
							$scope.ngModelShared.filters.splice(index,1); //remove filter from filter list
						}
					}

					$scope.cleanQbeColumns();
				}else{
					//initialization phase, there is no dataset
				}
			}else{
				$scope.updateFilters($scope.ngModelShared.dataset.dsId);
			}
		});

		// clean column name in case of QBE dataset
		$scope.cleanQbeColumns=function(){
			for(var i=0; i<$scope.ngModelShared.filters.length; i++){
				var columnName = $scope.ngModelShared.filters[i].colName;
				var colonIndex = columnName.indexOf(":");
				if(colonIndex > -1){
					columnName = columnName.substr(colonIndex + 1);
				}
				$scope.ngModelShared.filters[i].columnName = columnName;
			}
		}

		if($scope.ngModelShared.limitRows == undefined){
			$scope.ngModelShared.limitRows = {enable: false, rows: 10};
		}
	};
})();