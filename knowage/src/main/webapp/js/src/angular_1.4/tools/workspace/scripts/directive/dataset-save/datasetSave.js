/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	
	angular.module('datasetSaveModule')
		   .directive('datasetSave', function(sbiModule_config){
				return {
					templateUrl : sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/workspace/scripts/directive/dataset-save/dataset-save.html',
					controller : saveDataset,
					scope: {
						model: '=',
						close: '&',
						saveDataSet: '&'
					},
					link : function(scope, elem, attrs) {

					}

				};
		   });
	
	function saveDataset($scope, $rootScope, datasetSave_service, sbiModule_messaging, sbiModule_translate, sbiModule_user, datasetScheduler_service) {
		
		$scope.translate = sbiModule_translate;
		
		$scope.metaDataColumns = [
			 {
	    	      name:"name",
	    	      label: sbiModule_translate.load('sbi.ds.field.name'),
	    	      hideTooltip:true
    	     },
    	     {
	    	      name:"fieldType",
	    	      label: sbiModule_translate.load('sbi.generic.type'),
	    	      hideTooltip:true,
	    	      type:"input",
	    	      input: {
	    	    	  type:"select",
	    	    	  options: [
	    	  			{name: "ATTRIBUTE", value:"ATTRIBUTE"},
	    				{name: "MEASURE", value: "MEASURE"},
	    			]
	    	      }
    	     },
			
		];
		
		$scope.userLogged = {
			isTechnical: sbiModule_user.isTechnicalUser,
			isAbleToSchedulate: sbiModule_user.functionalities.indexOf("SchedulingDatasetManagement")>-1
		};
		
		$scope.isInEditingMode = function () {
			return $scope.model.hasOwnProperty('id');
		}
		
		$scope.formDirty = false;
		
		$scope.setFormDirty = function() {
			$scope.formDirty = true;
		}
		
		var setScopeForFinalUser = function() {
			if ($scope.isInEditingMode() == false && $scope.userLogged.isTechnical == 'false'
					&& (!$scope.model.hasOwnProperty('scopeId') && !$scope.model.hasOwnProperty('scopeCd'))) {
				for (var i = 0; i < $scope.scopeList.length; i++) {
					if ($scope.scopeList[i].VALUE_CD == "USER") {
						$scope.model.scopeId = $scope.scopeList[i].VALUE_ID;
						$scope.model.scopeCd = $scope.scopeList[i].VALUE_CD;
						break;
					}
				}
			}
		}
				
		$scope.setScopeId = function(scopeCd) {
			for (var i = 0; i < $scope.scopeList.length; i++) {
				if ($scope.scopeList[i].VALUE_CD == scopeCd) {
					$scope.model.scopeId = $scope.scopeList[i].VALUE_ID;
				}
			}
		}
	
		
		var serializeDatasetMeta = function(datasetMeta) {
			var newDsMeta = [];
			for (var i = 0; i < datasetMeta.length; i++) {
				var oldMetaObj = datasetMeta[i];
				if (oldMetaObj.pname == 'fieldAlias' || oldMetaObj.pname == 'fieldType') {
					var newMetaObj = {name: oldMetaObj.pvalue, fieldType: oldMetaObj.pvalue};
					newDsMeta.push(newMetaObj);
				}
			}
			return newDsMeta;
		}
		
		datasetSave_service.getDomainTypeCategory().then(function(response) {
			$scope.categoryList = response.data;
		}, function(response) {
			var message = "";
			if (response.status == 500) {
				message = response.data.RemoteException.message;
			} else {
				message = response.data.errors[0].message;
			}
			sbiModule_messaging.showErrorMessage(message, 'Error');
		});

		datasetSave_service.getDomainTypeScope().then(function(response) {
			$scope.scopeList = response.data;
			setScopeForFinalUser();
		}, function(response) {
			var message = "";
			if (response.status == 500) {
				message = response.data.RemoteException.message;
			} else {
				message = response.data.errors[0].message;
			}
			sbiModule_messaging.showErrorMessage(message, 'Error');
		});
		
		$scope.changeDatasetScope = function() {
			if (($scope.model.scopeCd.toUpperCase()=="ENTERPRISE"
					|| $scope.model.scopeCd.toUpperCase()=="TECHNICAL")) {
				$scope.isCategoryRequired = true;
			}
			else {
				$scope.isCategoryRequired = false;
			}
		}
		
		
	}
})();