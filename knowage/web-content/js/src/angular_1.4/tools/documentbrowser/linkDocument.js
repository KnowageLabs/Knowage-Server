
var app = angular.module("linkDocumentModule",["ngMaterial","angular_list","sbiModule"])
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("linkDocumentCTRL",linkDocumentFunction);
linkDocumentFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging"];
function linkDocumentFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging){
	
	//VARIABLES
	
	
	$scope.translate = sbiModule_translate;
	$scope.showme = true;
	$scope.sourceList = [];
	$scope.tablesList = [];
	$scope.selectedTables = [];
	$scope.savedTables = [];
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
				$scope.getTablesByDocumentID(documentID);
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
			if(!$scope.arrayContains($scope.selectedTables,'tableId',item)){
				$scope.selectedTables.push(item);
				$scope.forAdding.push(item);
			}
			
		}
		console.log($scope.selectedTables);
		console.log($scope.forAdding);
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
		var index1 = $scope.forAdding.indexOf(item);
		if($scope.forAdding.indexOf(item)>-1){
			console.log("aaaa");
			$scope.forAdding.splice(index1,1);
		}
		
			if($scope.selectedTables.indexOf(item)>-1){
				$scope.selectedTables.splice(index,1);
				
				if(!$scope.arrayContains($scope.forDeletion,'tableId',item) && $scope.arrayContains($scope.savedTables,'tableId',item) ){
					
					$scope.forDeletion.push(item);
					
				}
				
			}
			
			
			console.log("selektovani" + $scope.selectedTables);
			console.log("za brisanje" + $scope.forDeletion);
		}

$scope.getTablesByDocumentID = function(id){	
	sbiModule_restServices.promiseGet("2.0/metaDocumetRelationResource/document/"+id, "")
	.then(function(response) {
		
		$scope.selectedTables = response.data;
		$scope.savedTables = angular.copy(response.data);
		$scope.markDeleted(selectedTables_id);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('error getting saved', 'Error');
		
	});	
}

$scope.deleteRelations = function(docId,item){
	sbiModule_restServices.promiseDelete("2.0/metaDocumetRelationResource/"+docId,item.tableId)
	.then(function(response) {	
	}, function(response) {
		sbiModule_messaging.showErrorMessage('error deleting', 'Error');
		
	});	
}

$scope.insertRelations = function(docId,item){	
	sbiModule_restServices.promisePost("2.0/metaDocumetRelationResource/"+docId, "",angular.toJson(item))
	.then(function(response) {
	}, function(response) {
		sbiModule_messaging.showErrorMessage('error inserting', 'Error');
		
	});	
}

$scope.checkSave = function(){
	if($scope.forDeletion.length == 0 && $scope.forAdding.length == 0){
		return true;
	}else{
		return false;
	}
	
}



$scope.saveRelation = function(docId){
	
	if($scope.forDeletion.length > 0){
	console.log("deleting")	
	for (var i = 0; i < $scope.forDeletion.length; i++) {
		$scope.deleteRelations(docId,$scope.forDeletion[i]);
	}
}
	if($scope.forAdding.length > 0){
	console.log("adding");	
	for (var i = 0; i < $scope.forAdding.length; i++) {
		$scope.insertRelations(docId,$scope.forAdding[i]);
	}
	}
	sbiModule_messaging.showSuccessMessage('Successfully saved', 'Success!');
	
	$scope.forDeletion = [];
	$scope.forAdding = [];
	
	$timeout(function() {
		$scope.goBack();
    }, 2000);
	
//	$timeout(function(){
//		
//		$scope.getTablesByDocumentID(docId);
//	}, 1000);
	
	//$scope.goBack();
	
}

$scope.goBack = function(){
	history.go(-1);
	
}

$scope.markDeleted = function(listId){
	for (var i = 0; i < $scope.selectedTables.length; i++) {
		console.log(listId);
		if($scope.selectedTables[i].deleted){
			console.log($scope.selectedTables[i]);
			$timeout(function() {
				
				
				//document.getElementsByClassName("angularListRowItem").style.backgroundColor = "red";
				//document.getElementById('listItemTemplate').style.color  = "red";
			
		    }, 250);
			
		}
	}
}


};


