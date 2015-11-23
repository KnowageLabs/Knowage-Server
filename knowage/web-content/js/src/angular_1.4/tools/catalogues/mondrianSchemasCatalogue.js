/**
 * 
 */

var app = angular.module('mondrianSchemasCatalogueModule',['ngMaterial','angular_list','angular_table','sbiModule','angular_2_col']);

app.controller('mondrianSchemasCatalogueController',["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",mondrianSchemasCatalogueFunction]);

function mondrianSchemasCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.translate = sbiModule_translate;
	$scope.showMe = false;
	$scope.selectedMondrianSchema={};
	$scope.itemList = [{NAME:"dragan",DESCRIPTION:"neki"}];
	$scope.fileList = [];
	$scope.counter = 1;
	
	$scope.catalogueSpeedOptions =  [{
	        
		label:sbiModule_translate.load("sbi.generic.delete"),
	    icon:'fa fa-minus',
	    backgroundColor:'red',
	    color:'white',
	    action:function(item){
	    $scope.deleteMondrianSchema(item);
	    }
	 }];
	
	
	$scope.saveMondrianCatalogue = function(){
		
		console.log("saving Schema...");
		
		
		$scope.itemList.push($scope.selectedMondrianSchema);
		for(var i=0;i<$scope.itemList.length;i++){
			
			if($scope.itemList[i].$$hashKey === $scope.selectedMondrianSchema.$$hashKey)
			$scope.itemList[i] = $scope.selectedMondrianSchema;
		}
		console.log("saved!!!");
		
		
	};
	
	$scope.deleteMondrianSchema = function(){
		
		console.log("deleting Schema...");
	};
	
	
	
	$scope.createDragan = function(){
		$scope.showMe = true;
		$scope.selectedMondrianSchema= {};
		
	}
	
	$scope.cancel = function(){
		$scope.showMe = false;
	}
	
	$scope.myAssociatedClickFunction = function(item){
		$scope.showMe = true;
		$scope.selectedMondrianSchema= item;
		console.log(item);
		
	}
	
	$scope.update = function(){
		
		
		
	}
	
	
};