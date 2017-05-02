
var app = angular.module("analyticalDriversListModule",["ngMaterial","angular_table","sbiModule"])
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("analyticalDriversListCTRL",analyticalDriversListFunction);
analyticalDriversListFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging"];
function analyticalDriversListFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging){
	
	//VARIABLES
	
	
	$scope.translate = sbiModule_translate;
	$scope.adList = [];
	$scope.selectedAD = {};
	//FUNCTIONS	
		 

	$scope.getDrivers = function(){ // service that gets list of drivers @GET
		sbiModule_restServices.promiseGet("2.0", "analyticalDrivers")
		.then(function(response) {
			$scope.adList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});
	}
	$scope.getDrivers();
	
	$scope.selectAD = function(item){
		$scope.selectedAD = angular.copy(item);
	}
	

$scope.goBackandSave = function(){
	saveUrl += "&ADName="+$scope.selectedAD.label
	saveUrl += "&ADId="+$scope.selectedAD.id
	$scope.selectedAD = {};
	document.location.href = saveUrl;
}

$scope.close = function(){
	
	$scope.selectedAD = {};
	document.location.href = closeUrl;
}


};


