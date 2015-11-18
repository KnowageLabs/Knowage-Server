var app=angular.module("ModalitiesCheckModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("ModalitiesCheckController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",ModalitiesCheckFunction]);

function ModalitiesCheckFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){

	$scope.createConstraints =function(){
		console.log("radi");
		
	}
	
	 
	
	
	
};