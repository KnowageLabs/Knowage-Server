/**
 * 
 */

var app = angular.module('businessModelCatalogueModule',['ngMaterial', 'angular_list', 'angular_table','sbiModule', 'angular_2_col','file_upload']);

app.controller('businessModelCatalogueController',["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast","multipartForm", "sbiModule_download",businessModelCatalogueFunction]);

function businessModelCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,multipartForm,sbiModule_download){
	
	//variables
	///////////////////////////////////////////////////////////
	$scope.isDirty = false;
	$scope.showMe = false;				//boolean
	$scope.versionLoadingShow;
	$scope.bmLoadingShow;
	$scope.isNew;
	
	$scope.translate = sbiModule_translate;
	$scope.businessModelList=[];		//All Business Models list
	$scope.listOfDatasources = [];		//Dropdown
	$scope.listOfCategories=[];			//Dropdown
	$scope.bmVersions=[];				//All versions of BM list
	$scope.selectedBusinessModels=[];	//Selected Business Models table multiselect
	$scope.selectedVersions=[];			//Selected BM Versions table multiselect
	$scope.selectedBusinessModel = {}; //Selected model for editing or new model data
	$scope.bmVersionsRadio;
	$scope.bmVersionsActive;
	$scope.fileObj ={};

	angular.element(document).ready(function () {
        $scope.getData();
    });

	
	//methods
	//////////////////////////////////////////////////////////
	   
	 $scope.getData = function(){
		 $scope.getBusinessModels();
		 $scope.getDataSources();
		 $scope.getCategories();
	 }

	
	$scope.createBusinessModel = function(){
		$scope.selectedBusinessModel = {};
		$scope.bmVersions=[];
		$scope.isNew = true;
		$scope.showMe = true;
	}
	
	
	
	$scope.cancel = function(){
		$scope.showMe = false;
		$scope.isDirty = false;
		$scope.selectedBusinessModel = {};
		$scope.bmVersions=[];
	}
	
	$scope.businessModelLock = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = !$scope.selectedBusinessModel.modelLocked;
		if($scope.selectedBusinessModel.modelLocked){
			$scope.selectedBusinessModel.modelLocker = valueUser;
		}
		else{
			$scope.selectedBusinessModel.modelLocker = "";
		}
		$scope.checkChange();
	}
	
	$scope.lockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = true;
		console.log($scope.selectedBusinessModel);
	}
	
	$scope.leftTableClick = function(item){
		$scope.isNew = false;
		if($scope.isDirty){
		    $mdDialog.show($scope.confirm).then(function(){
		    	$scope.isDirty=false;   
		    	$scope.selectedBusinessModel=angular.copy(item);
		    	$scope.showMe=true;
		    },
		     function(){		       
		    	  $scope.showMe = true;
		      });
		      
		     }else{		    
		    	 $scope.selectedBusinessModel=angular.copy(item);
		    	 console.log("selecetd bm:");
		    	 console.log($scope.selectedBusinessModel);
		    	 $scope.getVersions(item.id);
		    	 $scope.showMe=true;
		     }

	}
	
	$scope.downloadFile = function(item,ev){

					window.location = "http://localhost:8080/knowage/restful-services/2.0/businessmodels/"+$scope.selectedBusinessModel.id+"/versions/"+item.id+"/file";

	}
	
	 $scope.bmSpeedMenu= [
		                      {
		                    	  label:'delete',
		                    	  icon:'fa fa-trash-o fa-lg',
		                    	  color:'#153E7E',
		                    	  action:function(item,event){
		                    		  $scope.deleteItem(item,event);
		                    	  }
		                      	}
		                     ];
	 
	 $scope.bmSpeedMenu2= [
	                       {
		                       label:'download',
		                       icon:'fa fa-download',
		                       color:'#3CB371',
		                       action:function(item,event){
		                    	   $scope.downloadFile(item,event);
		                       }
	                       },
	                       
	                       {
		                    	  label:'delete',
		                    	  icon:'fa fa-trash',
		                    	  color:'#DC143C',
		                    	  action:function(item,event){
		                    		  $scope.deleteItemVersion(item,event);
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
			  					$scope.bmLoadingShow = true;
			  					$scope.businessModelList = [];
			  					
			  					setTimeout(function(){
				  					for(var i = 0; i < data.length; i++){
				  						$scope.businessModelList.push(data[i]);
				  						
				  						$scope.bmLoadingShow = false;
				  						$scope.$apply();
						  			}
			  					},1000);	
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
		  	.get("2.0/businessmodels/"+id+"/versions","")
		  	.success(
		  			function(data, status, headers, config) {
		  				console.log(data);
		  				if (data.hasOwnProperty("errors")) {
		  					console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
		  				} else {
		  					$scope.versionLoadingShow = true;
		  					$scope.bmVersions = [];
		  					
		  					setTimeout(function(){
		  						$scope.bmVersions = data;
			  					activeFlagStyle();
			  					millisToDate($scope.bmVersions);
			  					
			  					$scope.versionLoadingShow = false;
			  					$scope.$apply();
		  					 },600);	  					
		  					
		  				}
		  				
		  			})
		  		
		  		.error(function(data, status, headers, config) {
		  			console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
		  		});
		 
		 //$scope.versionLoadingShow = false;
		 
	 }
	 
	 $scope.saveBusinessModelFile = function(){
			multipartForm.post("2.0/businessmodels/"+$scope.selectedBusinessModel.id+"/versions",$scope.fileObj).success(
					
					function(data,status,headers,config){
						if(data.hasOwnProperty("errors")){
							
							console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
							
						}else{
							
							console.log("[UPLOAD]: SUCCESS!");
							$scope.bmVersions = $scope.getVersions($scope.selectedBusinessModel.id);
							console.log($scope.bmVersions);
							document.getElementById("businessModelFile").value = "";
							$scope.isDirty = false;
							$scope.fileObj.fileName = "";
							$scope.fileObj = {};
							
						}
					}).error(function(data, status, headers, config) {
								console.log("[UPLOAD]: FAIL!"+status);
							});
	 }
	 //calling service for saving BM @POST and @PUT
	 $scope.saveBusinessModel = function(){
		 	if($scope.selectedBusinessModel.modelLocked === undefined)
		 		$scope.selectedBusinessModel.modelLocked = false;
			if(typeof $scope.selectedBusinessModel.id === "undefined"){
				console.log("Novi se cuva");

				sbiModule_restServices
					.post("2.0/businessmodels","",$scope.selectedBusinessModel)
					.success(
							function(data, status, headers, config){
								$scope.selectedBusinessModel.id = data.id;
								$scope.businessModelList.push(data);
								$scope.selectedVersions=[];
								$scope.isDirty = false;							
								
								if($scope.fileObj.fileName !== undefined)
									$scope.saveBusinessModelFile();
								
								$scope.showActionOK("New Business Model saved successfully");
							}
							
						);
			}
				
			else{
				console.log("Cuva se postojeci:id="+$scope.selectedBusinessModel.id);
				sbiModule_restServices
					.put("2.0/businessmodels", $scope.selectedBusinessModel.id, $scope.selectedBusinessModel)
					.success(
							function(data,status,headers,config){
								if($scope.fileObj.fileName !== undefined)
									$scope.saveBusinessModelFile();
								
								if($scope.bmVersionsActive != null){
									sbiModule_restServices
									.put("2.0/businessmodels/" + $scope.selectedBusinessModel.id+"/versions/"+ $scope.bmVersionsActive,"")
									.success(
											function(){
												console.log("Uspjesno sacuvana promjena verzije ");
											}
										
										);
									}
								
								$scope.businessModelList=[];
								$scope.getBusinessModels();
								$scope.isDirty = false;
								$scope.selectedBusinessModel.modelLocker = data.modelLocker;
								$scope.showActionOK("Business Model edited successfully");
								
							}
							
						);
				
				
			}	

		}
	 	
	 	//calling service method DELETE/{bmId} deleting single item
		$scope.deleteItem=function(item,event){
			  console.log(item.id);
			  var id = item.id;
				sbiModule_restServices
					.delete("2.0/businessmodels",id)
					.success(
							function(){
								removeFromBMs(id,"left");
								if($scope.selectedBusinessModel.id == id){
									$scope.selectedBusinessModel={};
								}
								$scope.selectedBusinessModels=[];
								$scope.showActionOK("Business Model deleted successfully");
							});
		}
		
		//calling service method DELETE/{bmId}/versions/{vId} deleting single version of selected model
		$scope.deleteItemVersion=function(item,event){
			
			var bmId = $scope.selectedBusinessModel.id;
			var id = item.id;
			sbiModule_restServices
			.delete("2.0/businessmodels/"+bmId+"/versions/"+id,"")
			.success(
					function(){
						removeFromBMs(id,"right");
						$scope.getVersions(bmId);
						$scope.showActionOK("Business Model Version deleted successfully");
			});

		}
		
	 	//calling service for deleting BM @DELETE
		$scope.deleteBusinessModels = function(){
			var size = $scope.selectedBusinessModels.length;
			if(size > 0 && size == 1){
				var id = $scope.selectedBusinessModels[0].id;
				sbiModule_restServices
					.delete("2.0/businessmodels",id)
					.success(
							function(){
								removeFromBMs(id,"left");
								if($scope.selectedBusinessModel.id == id){
									$scope.selectedBusinessModel={};
									
								}
								$scope.selectedBusinessModels=[];
								$scope.showActionOK("Business Model deleted successfully");
							});
			}
			
			else{
				sbiModule_restServices
				.delete("2.0/businessmodels/deletemany",makeDeletePath($scope.selectedBusinessModels))
				.success(
						function(){
							for(var i=0; i<size; i++){
								removeFromBMs($scope.selectedBusinessModels[i].id,"left");
								
								if($scope.selectedBusinessModel.id == id){
									$scope.selectedBusinessModel={};
									
								}
							}
							
							$scope.selectedBusinessModels=[];
							$scope.showActionOK("Business Models deleted successfully");
						});
			}
			
		}
		
		//calling service for deleting BM Versions @DELETE
		$scope.deleteBusinessModelVersions = function(){
			var size = $scope.bmVersions.length;
			var bmId = $scope.selectedBusinessModel.id;
			if(size > 0 && size == 1){
				var id = $scope.bmVersions[0].id;
				sbiModule_restServices
					.delete("2.0/businessmodels/"+bmId+"/versions",id)
					.success(
							function(){
								removeFromBMs(id,"right");
								$scope.bmVersions=[];
								$scope.showActionOK("Business Model Version deleted successfully");
					});
			}
			else{
				sbiModule_restServices
				.delete("2.0/businessmodels/"+bmId+"/deleteManyVersions",makeDeletePath($scope.bmVersions))
				.success(
						function(){
							for(var i=0; i<size; i++){
								removeFromBMs($scope.bmVersions[i].id,"left");
							}
							$scope.bmVersions=[];
							$scope.showActionOK("Business Model Version deleted successfully");
							
				});
			}
		}
	 
		//my util functions
		//////////////////////////////////////////////////
	 
		//make path
		
		makeDeletePath = function(selectedItems){
			 var s="?";
			 
			 for(var i=0; i<selectedItems.length;i++){
				 s+="id="+selectedItems[i].id+"&";
			 }
			 
			 return s;
		} 	
			
		//list updating
		
		removeFromBMs = function(id,table){

			 if(table === "left")
				 var array = $scope.businessModelList;
			 else
				 var array = $scope.bmVersions;
			 
			 for(var i=0;i<array.length;i++){
				 if(array[i].id == id)
					 array.splice(i,1);	 
			 }
			 
			 if(table === "left")
				 $scope.businessModelList = array;
			 else{
				 $scope.bmVersions = array;
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
		
		//check if is name dirty 
		$scope.checkChange = function(){
			if($scope.selectedBusinessModel.name === undefined || $scope.selectedBusinessModel.name === ""){
					$scope.isDirty = false;
			}
			else{
				$scope.isDirty = true;
			}

		}
		
		//get item by id
		getItemById = function(id){
			for(var i = 0; i < $scope.businessModelList.length ; i++){
				if($scope.businessModelList[i].id == id)
					return $scope.businessModelList[i];
			}
		}
		
		//comparing bms
		compareBusinessModels = function(bm1,bm2){
			for(var i in bm1){
				if(bm1[i] !== bm2[i])
					return false;
			}
				return true;
		}
		
		 $scope.confirm = $mdDialog
	      .confirm()
	      .title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
	      .content(
	              sbiModule_translate
	              .load("sbi.catalogues.generic.modify.msg"))
	              .ariaLabel('toast').ok(
	                      sbiModule_translate.load("sbi.general.continue")).cancel(
	                              sbiModule_translate.load("sbi.general.cancel"));
		
		 activeFlagStyle = function(){
				 for(var i=0; i<$scope.bmVersions.length;i++){
					 $scope.bmVersions[i]["ACTION"] = '<md-radio-button value="'+$scope.bmVersions[i].id+'"></md-radio-button>';
					 if($scope.bmVersions[i].active){
						 $scope.bmVersionsRadio = $scope.bmVersions[i].id;
						 $scope.bmVersionsActive = $scope.bmVersions[i].id;
					 }
				 }
		 }
		 
		 $scope.clickRightTable = function(item){
			 $scope.bmVersionsActive = item.id;
		 }

};



app.directive('fileModel',['$parse',function($parse){
		
		return {
			restrict:'A',
			link: function(scope,element,attrs){
				var model = $parse(attrs.fileModel);
				var modelSetter = model.assign;
				
				element.bind('change',function(){
					scope.$apply(function(){
						modelSetter(scope,element[0].files[0]);
						
					})
				})
			}
		}
		
		
	}]);


app.service('multipartForm',['$http',function($http){
		
		this.post = function(uploadUrl,data){
			
			var formData = new FormData();
			
			formData.append("file",data.file);

			return	$http.post(uploadUrl,formData,{
					transformRequest:angular.identity,
					headers:{'Content-Type': undefined}
				})
		}
		
	}]);
