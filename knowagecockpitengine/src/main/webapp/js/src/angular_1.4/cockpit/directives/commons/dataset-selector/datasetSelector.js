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
(function(){

	angular.module('cockpitModule').directive('datasetSelector',function($compile){
		return{
			templateUrl: baseScriptPath+ '/directives/commons/dataset-selector/templates/datasetSelector.html',
			transclude: true,
			replace: true,
			scope:{
				ngModel:"=",
				extended:"=?",
				datasetSettings:"=?",
				onChange:"&",
				isDisabled:"=?",
				noParameters: "=?",
				datasetTypeAvailable:"=?",
				datasetTypeExclusion:"=?"
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

	function datasetSelectorControllerFunction($scope,cockpitModule_datasetServices,sbiModule_translate,sbiModule_restServices,cockpitModule_generalOptions){
		$scope.translate=sbiModule_translate;
		if(!$scope.datasetSettings) $scope.datasetSettings = {};
		$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();

		$scope.addNewDataset=function(){
			cockpitModule_datasetServices.addDataset(undefined,$scope.availableDatasets,false,true,$scope.datasetTypeAvailable || undefined,$scope.datasetTypeExclusion || undefined,$scope.noParameters || false )
			.then(function(data){
				$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();
				$scope.ngModel=data.id.dsId;
				$scope.onChange({dsId:data.id.dsId});
				$scope.getMetaData($scope.ngModel);
			},function(error){
				console.log(error);
			});
		}
		$scope.cancelDataset=function(){
			delete $scope.ngModel;
		}
		$scope.getMetaData = function(id){
			if(id){
				$scope.loadingMetadata = true;
				$scope.dataset = {};
				var params = cockpitModule_datasetServices.getDatasetParameters(id);
				for(var p in params){
					if(params[p].length == 1){
						params[p] = params[p][0];
					}
				}
				for(var d in $scope.availableDatasets){
					if($scope.availableDatasets[d].id.dsId == id){
						$scope.dataset = $scope.availableDatasets[d];
						$scope.datasetSettings.sortingColumn = $scope.datasetSettings.sortingColumn || $scope.dataset.metadata.fieldsMeta[0].name;
						$scope.datasetSettings.sortingOrder = $scope.datasetSettings.sortingOrder || 'ASC';
					}
				};
				$scope.loadingMetadata = false;
			}else {
				$scope.dataset = {};
			}
		}

		$scope.availableDatasetsFilter = function(ds){
			return $scope.isDatasetAvailable(ds);
		}


		$scope.isDatasetAvailable = function(ds){
			if($scope.datasetTypeExclusion){
				var excluded = false;
				for(var e in $scope.datasetTypeExclusion){
					if($scope.datasetTypeExclusion[e].type == ds.type){
						if($scope.datasetTypeExclusion[e].configuration){
							if(ds.configuration[$scope.datasetTypeExclusion[e].configuration.property] == $scope.datasetTypeExclusion[e].configuration.value) return false;
						} else {
							return false;
						}

					}
				}
				return true;
			}
			if($scope.datasetTypeAvailable){
				for(var a in $scope.datasetTypeAvailable){
					if($scope.datasetTypeAvailable[a].type == ds.type){
						if($scope.datasetTypeAvailable[a].configuration){
							if(ds.configuration[$scope.datasetTypeAvailable[a].configuration.property] == $scope.datasetTypeAvailable[a].configuration.value) return true;
							return false;
						}
						return true;
					} return false;
				}
			}
			return true;
		}

		if($scope.ngModel) $scope.getMetaData($scope.ngModel);

		$scope.orderColumn = function(col){
			if(col.name == $scope.datasetSettings.sortingColumn) {
				$scope.datasetSettings.sortingOrder = $scope.datasetSettings.sortingOrder == 'ASC' ? 'DESC' : 'ASC';
			}
			else{
				$scope.datasetSettings.sortingColumn = col.name;
				$scope.datasetSettings.sortingOrder = 'ASC';
			}
		}

		var metaDataWatcher = $scope.$watch('ngModel',function(newValue,oldValue){
			if($scope.extended && newValue!=oldValue){
				delete $scope.datasetSettings.sortingColumn;
				delete $scope.datasetSettings.sortingOrder;
				$scope.getMetaData(newValue);
			}
		})

		$scope.getFieldType = function(typeValue){
			for(var o in cockpitModule_generalOptions.fieldsTypes){
				if(cockpitModule_generalOptions.fieldsTypes[o].value == typeValue){
					return cockpitModule_generalOptions.fieldsTypes[o].label;
				}
			}
			return typeValue;
		}

	};

})();