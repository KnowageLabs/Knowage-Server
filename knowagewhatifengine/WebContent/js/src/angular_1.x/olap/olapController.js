var olapMod = angular.module('olapManager', [ 'ngMaterial']);
olapMod.controller("olapController", [ "$scope", "$timeout", "$window","$mdDialog",
		olapFunction ]);

function olapFunction($scope, $timeout, $window,$mdDialog) {
	$scope.params = params;
	
	console.log($scope.params);
}