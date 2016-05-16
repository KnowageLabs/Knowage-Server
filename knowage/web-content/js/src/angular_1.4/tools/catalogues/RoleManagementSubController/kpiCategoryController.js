var app = angular.module("RolesManagementModule").controller("kpiCategoryController", ["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast", "$timeout","sbiModule_messaging",kpiCategoryControllerFunction]);

function kpiCategoryControllerFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, $timeout,sbiModule_messaging) {

	// VARIABLES

	$scope.translate = sbiModule_translate;


	$scope.flagCheck=false;

	$scope.selectAll = function(){
		if(!$scope.flagCheck){
			//if it was false then the user check 
			$scope.flagCheck=true;
			angular.copy([],$scope.categoriesSelected);
			for(var i=0;i<$scope.listCategories.length;i++){
				var obj = {};
				obj["VALUE_ID"] = $scope.listCategories[i].VALUE_ID
				$scope.categoriesSelected.push(obj);
			}
		}else{
			$scope.flagCheck=false;
			angular.copy([],$scope.categoriesSelected);
		} 
	};


	$scope.loadCategoriesKpi = function(){
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=KPI_KPI_CATEGORY")
		.then(function(response){ 
			angular.copy(response.data,$scope.listCategories);
			console.log(response.data);
		},function(response){
		});
	}
	$scope.loadCategoriesKpi();


	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);

		if(index != -1){
			$scope.categoriesSelected.splice(index,1);
		}else{
			var obj = {};
			obj["VALUE_ID"] = item.VALUE_ID
			$scope.categoriesSelected.push(obj);
		}

	};

	$scope.exists = function (item, list) {

		return  $scope.indexInList(item, list)>-1;

	}; 

	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.VALUE_ID==item.VALUE_ID){
				return i;
			}
		}

		return -1;
	};
}
