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
	$scope.listOfDatasources = [];
	$scope.listOfCategories=[];
	$scope.bmVersions=[];
	
	//for testing
	$scope.selectedBusinessModel = {};
	
	$scope.createBusinessModel = function(){
		$scope.selectedBusinessModel = {};
		$scope.showMe = true;
		$scope.businessModelHistory=[];
		
		
	}
	
	$scope.saveBusinessModel = function(){
		console.log("aj em in sejv biznis model");
		
		if(typeof $scope.selectedBusinessModel.id === "undefined"){
			console.log("Novi se cuva");

			sbiModule_restServices
				.post("2.0/businessmodels","",$scope.selectedBusinessModel)
				.success(
						function(){
							$scope.businessModelList=[];
							$scope.getBusinessModels();
							//$scope.businessModelList.push($scope.selectedBusinessModel);
							console.log("Uspjesno sacuvan");
						}
						
					);
		}
			
		else{
			console.log("Cuva se postojeci:id="+$scope.selectedBusinessModel.id);
			sbiModule_restServices
				.put("2.0/businessmodels", $scope.selectedBusinessModel.id, $scope.selectedBusinessModel)
				.success(
						function(){
							$scope.businessModelList=[];
							$scope.getBusinessModels();
							//$scope.businessModelList.push($scope.selectedBusinessModel);
							console.log("Uspjesno sacuvan");
						}
						
					);
		}	

	}
	
	$scope.cancel = function(){
		$scope.showMe = false;
		$scope.selectedBusinessModel = {};
		$scope.businessModelHistory=[];
	}
	
	$scope.unlockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = false;
		console.log($scope.selectedBusinessModel);
	}
	
	$scope.lockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = true;
		console.log($scope.selectedBusinessModel);
	}
	
	$scope.leftTableClick = function(item){
		$scope.selectedBusinessModel = angular.copy(item);
		$scope.getVersions(item.id);
		$scope.showMe = true;	
	}
	
	$scope.deleteItem=function(item){
		  console.log("delete");
		  //test
		  console.log(item.id);
		  $scope.businessModelList=[];
		 }
	
	$scope.downloadFile = function(item){
		//$scope.selectedBusinessModel = angular.copy(item);
		console.log('ovo je item koji proslijedjuje speed menu')
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
	 
	 //calling service for getting Business Models
	 $scope.getBusinessModels = function(){
			  sbiModule_restServices
			  	.get("2.0", 'businessmodels')
			  	.success(
			  			function(data, status, headers, config) {
			  				if (data.hasOwnProperty("errors")) {
			  					console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
			  				} else {
			  					for(var i = 0; i < data.length; i++){
			  						$scope.businessModelList.push(data[i]);
					  			}
			  				}
			  			})
			  		
			  		.error(function(data, status, headers, config) {
			  			console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
			  		});
	 }
	 
	 //calling service for getting data sources
	 $scope.getDataSources = function(){
		  sbiModule_restServices
			.get("datasources","")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{
						console.log("datasources:");
						console.log(data);
						$scope.listOfDatasources = data.root;					
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);	
	 }
	 
	 //Calling service for getting Categories
	 $scope.getCategories = function(){
		 sbiModule_restServices
			.get("domains","listValueDescriptionByType","DOMAIN_TYPE=BM_CATEGORY")
			.success
			(
				function(data, status, headers, config) 
				{
					if (data.hasOwnProperty("errors")) 
					{
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} 
					else 
					{
						console.log("categories:");
						console.log(data);
						$scope.listOfCategories = data;
						for(var i=0; i<data.length;i++){
							console.log($scope.listOfCategories[i].VALUE_ID);
						}
						
					}
				}
			)
			.error
			(
				function(data, status, headers, config) 
				{
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
				}
			);
	 }
	 
	 //Calling service for file versions
	 $scope.getVersions = function (id){
		 sbiModule_restServices
		  	.get("2.0/businessmodels",id)
		  	.success(
		  			function(data, status, headers, config) {
		  				if (data.hasOwnProperty("errors")) {
		  					console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
		  				} else {
		  					$scope.bmVersions = data;
		  					millisToDate($scope.bmVersions);
		  				}
		  			})
		  		
		  		.error(function(data, status, headers, config) {
		  			console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
		  		});
		 
	 }
	 
	 $scope.getData = function(){
		 $scope.getBusinessModels();
		 $scope.getDataSources();
		 $scope.getCategories();
	 }
	 
	 $scope.getData();
	 
	 millisToDate = function(data){
		 for(var i=0; i<data.length;i++){
			 var date = new Date(data[i].creationDate);
			 
			 var dd = date.getDate().toString();
			 var mm = (date.getMonth()+1).toString();
			 
			 var h = date.getHours().toString();
			 var m = date.getMinutes().toString();
			 var s = date.getSeconds().toString();
				 
			 data[i].creationDate = 
				 (dd[1]?dd:"0"+dd)+"/"
				 +(mm[1]?mm:"0"+mm)+"/"
				 +date.getFullYear()+" "
				 +(h[1]?h:"0"+h)+":"
				 +(m[1]?m:"0"+m)+":"
				 +(s[1]?s:"0"+s);
		 }
	 }
};