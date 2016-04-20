
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
	

	 
	//FUNCTIONS	
		 
	angular.element(document).ready(function () { // on page load function
				$scope.getSources();
		    });
	

	
	$scope.getSources = function(){ // service that gets predefined list GET		
		sbiModule_restServices.promiseGet("2.0/datasources", "")
		.then(function(response) {
			console.log(response.data);
			$scope.dataSourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
	}
	

};


