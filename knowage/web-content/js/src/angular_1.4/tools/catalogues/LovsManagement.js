var app = angular.module
(
	'lovsManagementModule',
	
	[
	 	'ngMaterial',
	 	'angular_list',
	 	'angular_table',	 	
	 	'sbiModule',
	 	'angular_2_col'
	 ]
);

app.controller
(
	'lovsManagementController',
	
	[
	 	"sbiModule_translate",
	 	"sbiModule_restServices",
	 	"$scope",
	 	"$mdDialog",
	 	"$mdToast",
	 	lovsManagementFunction
	 ]
);

function lovsManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast)
{
	$scope.createLov = function()
	{
		console.log("Kliknuo na plus!");
	}
};