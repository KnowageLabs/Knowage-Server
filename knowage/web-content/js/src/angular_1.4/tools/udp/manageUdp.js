
var app = angular.module('udpManagementApp', ['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'ng-context-menu',
                                              'sbiModule', 'angular_list']);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
	'blue-grey');
});

app.controller('Controller', ['sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog', manageUdpFucntion ])


function manageUdpFucntion(sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog) {
	
	var path = "2.0/userdataproperties";
	
	$scope.translate=sbiModule_translate;
	$scope.data=[]
	$scope.itemSelected = {};
	
	var rowDefault = {
			"id" : "",
			"label" : "",
			"name" : "",
			"type" : "",
			"family" : "",
			"description" : "",
			"multivalue" : ""
		};
	
	sbiModule_restServices.get(path, "", null).success(function(data) {
		if (data.length !== undefined && data.length > 0){
			$scope.data = data;
		}
		else{
			$scope.data = [];
		}
	});
	
	$scope.copyRowInForm = function(item,cell,listId) {
		$scope.property = angular.fromJson(angular.toJson(item));
	}
	
	$scope.resetForm = function(){
		$scope.property= undefined;
		$scope.propertyForm.$setUntouched();
		$scope.propertyForm.$setPristine()
	}
	
	$scope.saveProperty = function() {
		//if id not set, add new row, else update old row
		if ($scope.property.id === undefined){
			$newRow = JSON.parse(JSON.stringify($scope.property));
			$newRow.multivalue = $scope.property.multivalue === undefined ? "false" : $scope.property.multivalue;
			$newRow.description = $scope.property.description === undefined ? "" : $scope.property.description;
			$newRow.id = "";
			sbiModule_restServices
			.post(path,"",angular.toJson($newRow))
			.then(function successCallback(response) {
					if (response.status == 201){
						var arrayLocation = response.headers('Location').split('/');
						$newRow.id = arrayLocation[arrayLocation.length -1];
						$scope.data.splice(0, 0, $newRow);
					}
				},function errorCallback(response) {
					console.log("Http Post error");
					});
		} else {
			var toUpdate = JSON.parse(JSON.stringify($scope.property));
			sbiModule_restServices
			.put(path,toUpdate.id,angular.toJson(toUpdate))
			.success( function(data){
				var idx = $scope.indexOf($scope.data,toUpdate);
				//update value in the grid
				for (key in toUpdate){
					$scope.data[idx][key] = toUpdate[key];
				}
			});
		}
		$scope.resetForm();
	};
	
	//search function for data array
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].id == myElement.id) {
				return i;
			}
		}
		return -1;
	};

	// delete selected rows and update DB
	$scope.deleteRow = function() {
		var rowSelected = $scope.itemSelected;
		if (rowSelected.id !== undefined) {
				var idx = $scope.indexOf($scope.data, rowSelected);
				if (idx>=0){
					$oldValue = $scope.data.splice(idx, 1);
					sbiModule_restServices.delete(path, rowSelected.id);
				}
		}
		$scope.resetForm();
	};

};

