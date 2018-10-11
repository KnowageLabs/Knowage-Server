/**
 * Knowage, Open Source Business Intelligence suite Copyright (C) 2016
 * Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * Knowage is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath
			.lastIndexOf('/') + 1);

	angular.module('save', [ 'ngMaterial' ]).directive('save', function() {
		return {
			templateUrl : currentScriptPath + 'save.html',
			controller : save,

			scope : {
				ngModel : '='
			},
			link : function(scope, elem, attrs) {

			}

		};
	});

	function save($scope, $rootScope, save_service, $mdDialog,
			sbiModule_translate, sbiModule_config, $mdPanel, $q) {
		console.log($scope)
		$scope.savingQbeDataSet = {
			description : "",
			endDateField : "",
			isPersisted : false,
			isScheduled : false,
			label : "",
			name : "",
			persistTable : "",
			scopeCd : "",
			scopeId : "",
			startDateField : "",

		};
		$scope.translate = sbiModule_translate;

		$scope.saveQbeDataSet = function() {
			console.log($scope.savingQbeDataSet)
			$scope.ngModel.bodySend.qbeJSONQuery = {};
			$scope.ngModel.bodySend.qbeJSONQuery.catalogue = {};
			$scope.ngModel.bodySend.qbeJSONQuery.catalogue.queries = $scope.ngModel.bodySend.catalogue;
			$scope.ngModel.bodySend.qbeJSONQuery.version = 7;
			$scope.ngModel.bodySend.currentQueryId = $scope.ngModel.bodySend.qbeJSONQuery.catalogue.queries[0].id;
			$scope.ngModel.bodySend.qbeDataSource = "";
			$scope.ngModel.bodySend.label = $scope.savingQbeDataSet.label;
			$scope.ngModel.bodySend.name = $scope.savingQbeDataSet.name;
			$scope.ngModel.bodySend.description = $scope.savingQbeDataSet.description;
			$scope.ngModel.bodySend.isPersisted = $scope.savingQbeDataSet.isPersisted;
			$scope.ngModel.bodySend.isFlatDataset = false;
			$scope.ngModel.bodySend.isScheduled = $scope.savingQbeDataSet.isScheduled;
			$scope.ngModel.bodySend.persistTable = $scope.savingQbeDataSet.persistTable;
			$scope.ngModel.bodySend.endDateField = $scope.savingQbeDataSet.endDateField;
			$scope.ngModel.bodySend.startDateField = $scope.savingQbeDataSet.startDateField;
			$scope.ngModel.bodySend.scopeId = $scope.scopeList[0].VALUE_ID;
			$scope.ngModel.bodySend.scopeCd = $scope.scopeList[0].VALUE_CD;
			$scope.ngModel.bodySend.categoryId = $scope.categoryList[0].VALUE_ID;
			$scope.ngModel.bodySend.categoryCd = $scope.categoryList[0].VALUE_CD;
			$scope.ngModel.bodySend.sourceDatasetLabel = "";

			save_service.saveQbeDataSet($scope.ngModel.bodySend);
			$scope.ngModel.mdPanelRef.close();

		}
		var checkForScopeId = function(scopeCd) {
			for (var i = 0; i < $scope.scopeList.length; i++) {
				if ($scope.scopeList[i].VALUE_CD == scopeCd) {
					return $scope.scopeList[i].VALUE_ID;
				}

			}
		}

		save_service.getDomainTypeCategory().then(function(response) {
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

		save_service.getDomainTypeScope().then(function(response) {
			$scope.scopeList = response.data;
		}, function(response) {
			var message = "";
			if (response.status == 500) {
				message = response.data.RemoteException.message;
			} else {
				message = response.data.errors[0].message;
			}
			sbiModule_messaging.showErrorMessage(message, 'Error');
		});

		$scope.closeSaving = function() {
			$scope.ngModel.mdPanelRef.close();
		}
	}
})();