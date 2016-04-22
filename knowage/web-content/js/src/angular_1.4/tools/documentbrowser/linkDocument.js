
var app = angular.module("linkDocumentModule",["ngMaterial","angular_list","sbiModule"])
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("linkDocumentCTRL",linkDocumentFunction);
linkDocumentFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging"];
function linkDocumentFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging){
	
	$scope.translate = sbiModule_translate;
	$scope.showme = true;
	$scope.sourceList = [];
	$scope.tablesList = [];
	$scope.selectedTables = [];
	
	
	$scope.removeFromSelected = [ 			 		               	
	 		 		               	{
	 		 		               		label: sbiModule_translate.load("sbi.federationdefinition.delete"),
	 		 		               		icon:"fa fa-trash-o",
	 		 		               		backgroundColor:'red',
	 		 		               		action : function(item) {
	 		 		               				$scope.remove(item);
	 		 		               			}
	 		 		               	}
	 		 		             ];
	
	 
	//FUNCTIONS	
		 
	angular.element(document).ready(function () { // on page load function
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
			
		}
	}
	
$scope.remove = function(item){	
		
	var index = $scope.selectedTables.indexOf(item);
		if($scope.selectedTables.indexOf(item)>-1){
			$scope.selectedTables.splice(index,1);
			
		} 
	}
	

};


