/**
 * 
 */
//module with one quote
//controller with double quote
var app = angular.module('businessModelCatalogueModule',['ngMaterial', 'angular_list', 'angular_table','sbiModule', 'angular_2_col']);

app.controller('businessModelCatalogueController',["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast", businessModelCatalogueFunction]);

function businessModelCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.showMe = false;
	$scope.translate = sbiModule_translate;
	$scope.businessModelList=[];
	
	//for testing
	$scope.selectedBusinessModel = {};
	$scope.businessModelList=[{id:0,NAME:"Ime1",DESCRIPTION:"Description1",LOCKED:true},{id:1,NAME:"Ime2",DESCRIPTION:"Description2",LOCKED:false}];
	
	$scope.createArsenije = function(){
		$scope.selectedBusinessModel = {};
		$scope.showMe = true;
	}
	
	$scope.saveBusinessModel = function(){
		console.log("aj em in sejv biznis model");
		$scope.selectedBusinessModel.id = $scope.businessModelList.length - 1;
		$scope.businessModelList.push($scope.selectedBusinessModel);
		$scope.selectedBusinessModel = {};
	}
	
	$scope.cancel = function(){
		$scope.showMe = false;
		$scope.selectedBusinessModel = {};
	}
	
	$scope.testClickFunction = function(item){
		console.log(item.id);
		
		$scope.showMe = true;
		$scope.selectedBusinessModel = item;
	}
	
	$scope.deleteItem=function(){
		  console.log("delete");
		  //test
		  $scope.businessModelList=[];
		 }
	
	 $scope.bmSpeedMenu= [
		                      {
		                      label:'delete',
		                      icon:'fa fa-minus',
		                      backgroundColor:'red',
		                      color:'white',
		                      action:function(item){
		                       $scope.deleteItem(item);
		                      }
		                      }
		                     ];
};