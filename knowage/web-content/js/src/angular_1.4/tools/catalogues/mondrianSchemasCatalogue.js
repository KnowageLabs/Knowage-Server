/**
 * 
 */

var app = angular.module('mondrianSchemasCatalogueModule',['ngMaterial','angular_list','angular_table','sbiModule','angular_2_col']);

app.controller('mondrianSchemasCatalogueController',["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",mondrianSchemasCatalogueFunction]);

function mondrianSchemasCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	$scope.createDragan = function(){
		console.log("hi dragan");
	}
};