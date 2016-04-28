
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
			$scope.sourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
	}
	
	$scope.getTablesBySourceID = function(id){	
		sbiModule_restServices.promiseGet("2.0/metaSourceResource/"+id+"/metatables", "")
		.then(function(response) {
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
		$scope.markDeleted(selectedTables_id);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('aaaaaaaaa', 'Error');
		
	});	
}

$scope.deleteRelations = function(dsId,item){
	sbiModule_restServices.promiseDelete("2.0/metaDsRelationResource/"+dsId,item.tableId)
	.then(function(response) {
		
		$scope.getTablesByDatasetID(dsId);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('aaaaaaaaa', 'Error');
		
	});	
}

$scope.insertRelations = function(dsId,item){	
	sbiModule_restServices.promisePost("2.0/metaDsRelationResource/"+dsId, "",angular.toJson(item))
	.then(function(response) {
		
		$scope.getTablesByDatasetID(dsId);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('aaaaaaaaa', 'Error');
		
	});	
}



$scope.saveRelation = function(dsId){
	for (var i = 0; i < $scope.forDeletion.length; i++) {
		$scope.deleteRelations(dsId,$scope.forDeletion[i]);
	}
	for (var i = 0; i < $scope.forAdding.length; i++) {
		$scope.insertRelations(dsId,$scope.forAdding[i]);
	}
	
}

$scope.goBack = function(){
	history.go(-1);
	
}

$scope.markDeleted = function(listId){
	console.log(listId);
	for (var i = 0; i < $scope.selectedTables.length; i++) {

		if($scope.selectedTables[i].deleted){
			console.log($scope.selectedTables[i]);
			$timeout(function() {
				//document.getElementById("listItemTemplate").style.color = "red";
				
		    }, 250);
			
		}
	}
}

};


