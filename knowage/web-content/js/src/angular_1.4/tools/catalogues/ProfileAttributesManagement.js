/**
 * 
 */
var app=angular.module('profileAttributesManagementModule',['ngMaterial','angular_list','angular_table','sbiModule','angular_2_col']);

app.controller('profileAttributesManagementController',["sbiModule_translate","sbiModule_restServices","$scope","$mdDialog","$mdToast",profileAttributesManagementFunction]);

function profileAttributesManagementFunction(sbiModule_translate,sbiModule_restServices,$scope,$mdDialog,$mdToast){
	$scope.createProfileAttribute=function(){
		console.log("radi");
	}
	
}