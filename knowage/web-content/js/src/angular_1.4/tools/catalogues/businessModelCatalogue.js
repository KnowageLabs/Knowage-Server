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
	$scope.businessModelHistoryAll=[
	                             {mid:0,CREATOR:"biadmin",CREATION_DATE:"21.12.1968"},
	                             {mid:0,CREATOR:"aiadmin",CREATION_DATE:"09.07.1968"},
	                             {mid:1,CREATOR:"biadmin",CREATION_DATE:"31.12.1995"},
	                             {mid:0,CREATOR:"biadmin",CREATION_DATE:"20.09.2001"}
	                             ];
	$scope.businessModelHistory=[];
	
	$scope.createArsenije = function(){
		$scope.selectedBusinessModel = {};
		$scope.showMe = true;
		$scope.businessModelHistory=[];
	}
	
	$scope.saveBusinessModel = function(){
		console.log("aj em in sejv biznis model");
		
		if(typeof $scope.selectedBusinessModel.id === "undefined")
			$scope.selectedBusinessModel.id = $scope.businessModelList.length;
		
		$scope.businessModelList.push($scope.selectedBusinessModel);
		$scope.selectedBusinessModel = {};
		
	}
	
	$scope.cancel = function(){
		$scope.showMe = false;
		$scope.selectedBusinessModel = {};
		$scope.businessModelHistory=[];
	}
	
	$scope.unlockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.LOCKED = false;
		console.log($scope.selectedBusinessModel);
	}
	
	$scope.lockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.LOCKED = true;
		console.log($scope.selectedBusinessModel);
	}
	
	$scope.testClickFunction = function(item){
		$scope.businessModelHistory=[];
		console.log(item.id);
		
		$scope.showMe = true;
		$scope.selectedBusinessModel = item;
		for(var i = 0 ; i < $scope.businessModelHistoryAll.length;i++){
			if($scope.businessModelHistoryAll[i].mid == item.id)				
				$scope.businessModelHistory.push($scope.businessModelHistoryAll[i]);

		}
	}
	
	$scope.deleteItem=function(){
		  console.log("delete");
		  //test
		  $scope.businessModelList=[];
		 }
	
	$scope.downloadFile = function(item){
		console.log(item);
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
	 
	 $scope.bmSpeedMenu2= [
	                       {
		                       label:'download',
		                       icon:'fa fa-download',
		                       color:'green',
		                       action:function(item){
		                    	   $scope.downloadFile(item);
		                       }
	                       }
	                       ];
};