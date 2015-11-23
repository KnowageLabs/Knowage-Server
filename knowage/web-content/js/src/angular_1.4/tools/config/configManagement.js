
var app = angular.module('configManagementApp',  ['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'ng-context-menu',
                                                      'sbiModule', 'angular_list']);

app.controller('Controller', ['sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog', manageConfigFucntion ])


function manageConfigFucntion(sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog) {
	
	var path = "2.0/configs";
	$scope.translate=sbiModule_translate;
	$scope.data=[]
	$scope.itemSelected= {};
	$scope.filterCategory=[];
	$scope.hashCategory={};
	
	var calculatedHeight = document.body.clientHeight - (25 + document.body.getElementsByTagName("md-toolbar")[0].clientHeight + document.body.getElementsByClassName("buttonsContainer")[0].clientHeight);
	$scope.gridHeight = calculatedHeight +"px";
	
	var rowDefault = {
		id : "",
		label : "Label",
		name : "Name",
		valueCheck : "value check",
		valueTypeId : 407,
		category : "",
		active : "false"
	};

	sbiModule_restServices.get(path, "", null).success(function(data) {
		//creating map containing filters categories
		for (i=0;i<data.length;i++){
			if ($scope.hashCategory[data[i].category] === undefined){
				$scope.hashCategory[data[i].category]=data[i].category;
			}
			data[i].valueTypeId = data[i].valueTypeId == 407 ? 'NUM' : 'STRING';
		}
		//use the map to create filterCategory array for Grid filter
		for (var k in $scope.hashCategory){
			$scope.filterCategory.push({"value" :k , "label": k});
		}
		$scope.data = data;
	});
	
	//search function for data array
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].id == myElement.id) {
				return i;
			}
		}
		return -1;
	};

	// insert row
	$scope.addRow = function() {
		$mdDialog.show({
			controller: $scope.dialogController ,
			templateUrl: '/knowage/js/src/angular_1.4/tools/config/templates/configDialogForm.html',
			parent: angular.element(document.body),
			locals : {
				translate : $scope.translate,
				itemToEdit : undefined,
				filterCategory : $scope.filterCategory
			},
			preserveScope : true,
			clickOutsideToClose:false
		})
		.then(function(newRow) {
			//form is correct, updating grid and db
			headers = {
					'Content-Type': 'application/json'
			};
			newRow.valueTypeId = newRow.valueTypeId == 'NUM' ? 407 : 408; 
			sbiModule_restServices
			.post(path,"",angular.toJson(newRow),headers)
			.then(function successCallback(response) {
				if (response.status == 201){
					var arrayLocation = response.headers('Location').split('/');
					newRow.id = arrayLocation[arrayLocation.length -1];
					newRow.valueTypeId = newRow.valueTypeId == 407 ? 'NUM' : 'STRING';
					$scope.data.splice(0, 0, newRow);
				}
				else {
					newRow.id = "";
				}
			},
			function errorCallback(response) {
				newRow.id = "";
			});	
		}, function() {
			//form was cancelled, nothing to do 
		});
	};
	
	$scope.editRow = function() {
		var rowSelected = $scope.itemSelected;
		rowSelected.valueTypeId = rowSelected.valueTypeId == 'NUM' ? 407 : 408;
		if (rowSelected.id !== undefined) {
			var idx = $scope.indexOf($scope.data, rowSelected);
			$mdDialog.show({
				controller: $scope.dialogController ,
				templateUrl: '/knowage/js/src/angular_1.4/tools/config/templates/configDialogForm.html',
				parent: angular.element(document.body),
				locals : {
					translate : $scope.translate,
					itemToEdit : rowSelected,
					filterCategory : $scope.filterCategory
				},
				preserveScope : true,
				clickOutsideToClose:false
			}).then( function(editRow){
				headers = {
						'Content-Type': 'application/json'
				};
				editRow.valueTypeId = editRow.valueTypeId == 'NUM' ? 407 : 408;
				sbiModule_restServices
				.put(path,editRow.id, angular.toJson(editRow), headers)
				.success(function(data){
					editRow.valueTypeId = editRow.valueTypeId ==  407 ? 'NUM' : 'STRING';
					$scope.data[idx]=editRow;
				});
			},function(){
				//nothing to do, the request was cancelled
			});
		}
	};
	
	
	$scope.deleteRow = function() {
		var rowsSelected = $scope.itemSelected;
		if (rowsSelected.id !== undefined) {
				var idx = $scope.indexOf($scope.data, rowsSelected);
				if (idx>=0){
					$oldValue = $scope.data.splice(idx, 1);
					sbiModule_restServices.delete(path, rowsSelected.id);
				}
		}
	};

	$scope.dialogController =function ($scope, $mdDialog, translate, itemToEdit, filterCategory) {
		$scope.filterCategory = filterCategory; 
		$scope.translate = translate; 
		 $scope.config = itemToEdit !== undefined ? itemToEdit : undefined;
		 $scope.cancel = function() {
		    $mdDialog.cancel();
		  };
		 $scope.saveConfig = function(config) {
			config.id = config.id !== undefined ? config.id : "";
			var newRow = JSON.parse(JSON.stringify(config));
		    $mdDialog.hide(newRow);
		  };
	}
};
