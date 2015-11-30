/**
 * 
 */
//module with one quote
//controller with double quote
var app = angular.module('businessModelCatalogueModule',['ngMaterial', 'angular_list', 'angular_table','sbiModule', 'angular_2_col']);

app.controller('businessModelCatalogueController',["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast", businessModelCatalogueFunction]);

function businessModelCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	//variables
	///////////////////////////////////////////////////////////
	$scope.showMe = false;	
	$scope.translate = sbiModule_translate;
	$scope.businessModelList=[];
	$scope.listOfDatasources = [];
	$scope.listOfCategories=[];
	$scope.bmVersions=[];
	$scope.selectedBusinessModels=[];
	$scope.selectedVersions=[];
	$scope.selectedBusinessModels1=[1,2,3];
	$scope.selectedBusinessModel = {};
	 

	
	//methods
	//////////////////////////////////////////////////////////
	   
	 $scope.getData = function(){
		 $scope.getBusinessModels();
		 $scope.getDataSources();
		 $scope.getCategories();
	 }

	
	$scope.createBusinessModel = function(){
		$scope.selectedBusinessModel = {};
		$scope.showMe = true;
		$scope.businessModelHistory=[];	
		
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
	 
	 
	 //functions that use services
	 //////////////////////////////////////////////////////////////////
	 
	 //calling service for getting Business Models @GET
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
	 
	 //calling service for getting data sources @GET
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
	 
	 //Calling service for getting Categories  @GET
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
	 
	 //Calling service for file versions @GET
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
	 
	 
	 //calling service for saving BM @POST and @PUT
	 $scope.saveBusinessModel = function(){
			console.log("aj em in sejv biznis model");
			
			if(typeof $scope.selectedBusinessModel.id === "undefined"){
				console.log("Novi se cuva");

				sbiModule_restServices
					.post("2.0/businessmodels","",$scope.selectedBusinessModel)
					.success(
							function(data, status, headers, config){
								$scope.selectedBusinessModel.id = data.id;
								$scope.businessModelList.push(data);
								$scope.selectedVersions=[];
								$scope.showActionOK("New Business Model saved successfully");
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
								$scope.showActionOK("Business Model edited successfully");
								console.log("Uspjesno sacuvan");
							}
							
						);
			}	

		}
		
	 	//calling service for deleting BM @DELETE
		$scope.deleteBusinessModels = function(){
			if($scope.selectedBusinessModels.length == 1){
				var id = $scope.selectedBusinessModels[0].id;
				sbiModule_restServices
					.delete("2.0/businessmodels",id)
					.success(
							function(){
								removeFromBMs(id);
								if($scope.selectedBusinessModel.id == id){
									$scope.selectedBusinessModel={};
									
								}
								$scope.selectedBusinessModels=[];
								$scope.showActionOK("Business Model deleted successfully");
							});
			}
			
			else{
				sbiModule_restServices
				.delete("2.0/businessmodels/deletemany",$scope.selectedBusinessModels1)
				.success(
						function(){
							console.log("yeah weee didddd");
							
						});
			}
			
		}
	 	 
	 //my util functions
	 //////////////////////////////////////////////////
	 
	 //list updating
	 removeFromBMs = function(id){
		 console.log("prije slice");
		 console.log($scope.businessModelList);
		 for(var i=0;i<$scope.businessModelList.length;i++){
			 if($scope.businessModelList[i].id == id){
				 $scope.businessModelList.splice(i,1);
				 
				 console.log("poslije slice");
				 console.log($scope.businessModelList);
				 break;
			 }
				 
		 }
	 }
	 
	 //date/time format
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
	 
	 //toast
	 $scope.showActionOK = function(msg) {
		    var toast = $mdToast.simple()
		    .content(msg)
		    .action('OK')
		    .highlightAction(false)
		    .hideDelay(3000)
		    .position('top')

		    $mdToast.show(toast).then(
		    		function(response) {
		    			if ( response == 'ok' ) {
		    			}
		    		});
		   };
		   
			 $scope.getData();
};