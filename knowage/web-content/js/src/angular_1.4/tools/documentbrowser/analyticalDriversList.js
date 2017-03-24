
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
		 
	angular.element(document).ready(function () { // on page load function
				
		$scope.getDrivers();
		    });
	

	

	$scope.getDrivers = function(){ // service that gets list of drivers @GET
		sbiModule_restServices.promiseGet("2.0", "analyticalDrivers")
		.then(function(response) {
			$scope.adList = response.data;
			console.log($scope.adList)
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});
	}
	
	$scope.selectAD = function(item){
		$scope.selectedAD = angular.copy(item);
	}
	

$scope.goBackandSave = function(){
	console.log(backUrl);
	backUrl += "&ADName="+$scope.selectedAD.label
	backUrl += "&ADId="+$scope.selectedAD.id
	console.log(backUrl);
	$scope.selectedAD = {};
	document.location.href = backUrl;
}


};


