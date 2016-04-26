
var app = angular.module("linkDatasetModule",["ngMaterial","angular_list","sbiModule"])
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("linkDatasetCTRL",linkDatasetFunction);
linkDatasetFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging"];
function linkDatasetFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging){
	
	//VARIABLES
	
	
	$scope.translate = sbiModule_translate;
	$scope.showme = true;
	$scope.sourceList = [];
	$scope.tablesList = [];
	$scope.selectedTables = [];
	$scope.forAdding = [];
	$scope.forDeletion = [];
	
	
	
	$scope.removeFromSelected = [ 			 		               	
	 		 		               	{
	 		 		               		label: sbiModule_translate.load("sbi.federationdefinition.delete"),
	 		 		               		icon:"fa fa-trash-o",
	 		 		               		backgroundColor:'red',
	 		 		               		action : function(item) {
	 		 		               				$scope.remove(item);
	 		 		               				console.log($scope.forDeletion);
	 		 		               			}
	 		 		               	}
	 		 		             ];
	
	 
	//FUNCTIONS	
		 
	angular.element(document).ready(function () { // on page load function
				$scope.getTablesByDatasetID(datasetId);
				$scope.getSources();
		    });
	

	
	$scope.getSources = function(){ // service that gets predefined list GET		
		sbiModule_restServices.promiseGet("2.0/metaSourceResource", "")
		.then(function(response) {
			console.log(response.data);
			$scope.sourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
	}
	
	$scope.getTablesBySourceID = function(id){	
		sbiModule_restServices.promiseGet("2.0/metaSourceResource/"+id+"/metatables", "")
		.then(function(response) {
			console.log(response.data);
			$scope.tablesList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
	}
	
	$scope.moveToSelected = function(item){	
		
		var index = $scope.tablesList.indexOf(item);
		if($scope.selectedTables.indexOf(item)===-1){
			$scope.selectedTables.push(item);
			$scope.forAdding.push(item);
			
		}
	}
	$scope.arrayContains = function(array,property,item) {
		for (var i = 0; i < array.length; i++) {
			if(array[i][property] == item[property]){
				return true;
			}
		}
		return false;
	}
	
$scope.remove = function(item){	
		
	var index = $scope.selectedTables.indexOf(item);
		if($scope.selectedTables.indexOf(item)>-1){
			$scope.selectedTables.splice(index,1);
			if(!$scope.arrayContains($scope.forDeletion,'tableId',item)){
				$scope.forDeletion.push(item);
			}
			
			
		} 
	}

$scope.getTablesByDatasetID = function(id){	
	sbiModule_restServices.promiseGet("2.0/metaDsRelationResource/dataset/"+id, "")
	.then(function(response) {
		
		$scope.selectedTables = response.data;
		console.log($scope.selectedTables);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('aaaaaaaaa', 'Error');
		
	});	
}

$scope.deleteRelations = function(dsId){
	var path = "2.0/metaDsRelationResource/"+dsId;
	console.log(path);
	sbiModule_restServices.promiseDelete("2.0/metaDsRelationResource/"+dsId,"",angular.toJson($scope.forDeletion))
	.then(function(response) {
		
		$scope.getTablesByDatasetID(dsId);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('aaaaaaaaa', 'Error');
		
	});	
}

$scope.insertRelations = function(dsId){	
	sbiModule_restServices.promisePost("2.0/metaDsRelationResource/"+dsId, "",$scope.forDeletion)
	.then(function(response) {
		
		$scope.getTablesByDatasetID(dsId);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('aaaaaaaaa', 'Error');
		
	});	
}

$scope.saveRelation = function(dsId){
	$scope.deleteRelations(dsId);
	
	
	
}

};


