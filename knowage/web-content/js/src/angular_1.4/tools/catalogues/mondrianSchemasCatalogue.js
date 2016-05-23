/**
 * 
 */

var app = angular.module('mondrianSchemasCatalogueModule',['ngMaterial' , 'angular_list' , 'angular_table' , 'sbiModule' , 'angular_2_col','file_upload','angular-list-detail']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

app.controller('mondrianSchemasCatalogueController',["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","$filter","multipartForm","sbiModule_messaging","sbiModule_download",mondrianSchemasCatalogueFunction]);

function mondrianSchemasCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,$filter,multipartForm,sbiModule_messaging,sbiModule_download){
	
	$scope.translate = sbiModule_translate;
	$scope.showMe = false;
	$scope.catalogLoadingShow = false;
	$scope.versionLoadingShow = false;
	$scope.showCatalogs = false;
	$scope.showVersions = false;
	$scope.selectedMondrianSchema={};
	$scope.selectedVersion = {};
	$scope.itemList = [];
	$scope.progressValue = 50;
	$scope.fileList =[];
	$scope.servicePath = "2.0/mondrianSchemasResource";
	$scope.file ={};
	
	

	$scope.isDisabled = function(){
		if($scope.selectedMondrianSchema.id == undefined){
			if($scope.selectedMondrianSchema.name == undefined || $scope.selectedMondrianSchema.name=="" || $scope.file.file == undefined)
				return true;
			else
				return false;
		}
		return false;
		
	}
		
		$scope.downloadFile = function(item){

			var link = "/restful-services/2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions/"+item.id+"/file";
			sbiModule_download.getLink(link);

			}
		
	
	$scope.unlockModel= function(){
		
		$scope.selectedMondrianSchema.modelLocked = false;
		sbiModule_messaging.showInfoMessage("Mondrian Schema "+$scope.selectedMondrianSchema.name+" is unlocked", 'Information!');
	}
	
	$scope.isUniqueInList = function(propertyName,obj,listOfObjs){
		
		if(obj.hasOwnProperty(propertyName)&&itemList.length){
			
			for(var item in itemList){
				
				
			}
			
		}	
	}
	
	
	
	
	angular.element(document).ready(function () {
        $scope.getMondrianSchemas();
		
		
    });
	
	$scope.catalogueSpeedOptions =  [
		
		
		{
	        
		label:sbiModule_translate.load("sbi.generic.delete"),
		icon:'fa fa-trash',
		//icon:'fa fa-trash-o fa-lg',
	    // color:'#153E7E',
	    action:function(item){
	    $scope.confirmDelete(item,event);	
	    
			
	    	}
	 	}
	
	];
	
	$scope.versionsSpeedOptions =  [
	                                
	  {
	        
		label:sbiModule_translate.load("sbi.generic.download"),
	    icon:'fa fa-download',
	   // color:'#153E7E',
	    action:function(item,event){
	    $scope.downloadFile(item,event);
			
	    }
	  }
		  ,
	                                
	   {
	        
		label:sbiModule_translate.load("sbi.generic.delete"),
	    icon:'fa fa-trash',
		//icon:'fa fa-trash-o fa-lg',
	    //color:'#153E7E',
	    action:function(item){
	    $scope.confirmDelete(item,event);	
	    
			
	    }
	  }
	  
	  ];
	
	 $scope.confirmDelete = function(item,ev) {
		    var confirm = $mdDialog.confirm()
		          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
		          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
		          .ariaLabel("confirm_delete")
		          .targetEvent(ev)
		          .ok(sbiModule_translate.load("sbi.general.continue"))
		          .cancel(sbiModule_translate.load("sbi.general.cancel"));
		    $mdDialog.show(confirm).then(function() {
		    	
		    	if (item.name) {
		    		console.log("deleting schemaa");
		    		$scope.deleteMondrianSchema(item);
		    	} else if (item.fileName) {
		    		console.log("deleting version");
		    		$scope.deleteVersion(item);
		    	}
		   
		    }, function() {
		
		    });
		  };
	
	
	$scope.saveMondrianCatalogue = function(){
		
		console.log("saving Schema...");
		
		if(!isNaN($scope.selectedMondrianSchema.id)){
				
				console.log("updating...");
				$scope.modifyMondrianSchema();
				
		}else{
			
			if($scope.selectedMondrianSchema.name===undefined){
				
					console.log("name is required");
					console.log($scope.selectedMondrianSchema.name);
					$scope.showActionOK("Name is required");
					
				}else{
					
					console.log("adding new...");
					$scope.addNewMondrianSchema();
					
				}			
		}

	};
	$scope.createMondrianSchema = function(){
		$scope.showMe = true;
		$scope.selectedMondrianSchema = {};
		$scope.selectedVersion = {};
		$scope.fileList = [];
		$scope.file ={};
		
	}
	
	$scope.cancel = function(){
		$scope.showMe = false;
	}
	
	//CLICK FUNCTION FOR CATALOQUE TABLE
	$scope.catalogueClickFunction = function(item){
		$scope.showMe = true;
		if(item!=$scope.selectedMondrianSchema){
			$scope.selectedMondrianSchema ={};
			$scope.selectedMondrianSchema = angular.copy(item);
			$scope.fileList= $scope.getMondrianSchemasVersion();
			
			
		}
		
		console.log($scope.selectedMondrianSchema);
		
	}
	
	//CLICK FUNCTION FOR VERSIONS TABLE
	$scope.versionClickFunction = function(item){
		$scope.showMe = true;
		if(item!=$scope.selectedMondrianSchema){
			
			$scope.selectedVersion = angular.copy(item);
	
		}
		
		console.log($scope.selectedVersion);
		
	}
	
	
	
	
	//REST
	
	//GET ALL MONDRIAN SCHEMAS
	$scope.getMondrianSchemas = function(){
		
		sbiModule_restServices.promiseGet($scope.servicePath,"")
		.then(function(response) {
			$scope.catalogLoadingShow =true;
			$scope.showCatalogs = false;
			$scope.itemList=[];
			
			
			setTimeout(function(){
				$scope.itemList = response.data;
				$scope.catalogLoadingShow = false;
				$scope.showCatalogs = true;
				$scope.$apply();
			},10)
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
	//GET ALL MONDRIAN SCHEMAS VERSIONS
	$scope.getMondrianSchemasVersion = function(){
		
		sbiModule_restServices.promiseGet("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions","")
		.then(function(response) {
			$scope.showVersions=false;
			$scope.versionLoadingShow = true;	
			$scope.fileList =[];
			
					
				setTimeout(function(){
						
						
						$scope.fileList=response.data;
						for(var i= 0 ; i<$scope.fileList.length;i++){	
						
							$scope.fileList[i].creationDate = new Date(response.data[i].creationDate).toLocaleString();
							$scope.fileList[i].actives = "<md-radio-button ng-value = "+$scope.fileList[i].id+" aria-label='label'></md-radio-button>";
						
							if($scope.fileList[i].active){
							
								$scope.selectedMondrianSchema.currentContentId = $scope.fileList[i].id;
								for(var j =0; j<$scope.itemList.length;j++){
									if($scope.itemList[j].id===$scope.selectedMondrianSchema.id){
									
										for(var key in $scope.selectedMondrianSchema){
											$scope.itemList[j][key] = $scope.selectedMondrianSchema[key];
										}
										
									}
								}
								
							
							}
							
						
						}
						
						$scope.versionLoadingShow = false;	
						$scope.showVersions=true;
						
						
						$scope.$apply();
					
					},500)
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		};
	
	//POST NEW MONDRIAN SCHEMA
	$scope.addNewMondrianSchema = function(){
		
		$scope.selectedMondrianSchema.type ="MONDRIAN_SCHEMA";
		console.log($scope.selectedMondrianSchema);
		
		sbiModule_restServices.promisePost($scope.servicePath,"",$scope.selectedMondrianSchema)
		.then(function(response) {
			$scope.selectedMondrianSchema = response.data;	
			$scope.itemList.unshift($scope.selectedMondrianSchema);	
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
				
			if($scope.file.file){
					console.log("uploading...");
					$scope.uploadFile();
					
				}		
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
		};
		
	//POST UPLOAD FILE
	$scope.uploadFile= function(){
		
		multipartForm.post("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions",$scope.file).success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
					
				}else{
					
					console.log("[UPLOAD]: SUCCESS!");
					sbiModule_messaging.showSuccessMessage("Mondrian schema version "+$scope.file.fileName+" successfully uploaded", 'Success!');
					$scope.getMondrianSchemasVersion();
					$scope.file={};
					
					
				}
			}).error(function(data, status, headers, config) {
						console.log("[UPLOAD]: FAIL!"+status);
					});
		
	}	
	
	
	//PUT MODIFY MONDRIAN SCHEMA
	$scope.modifyMondrianSchema = function(){
		
		sbiModule_restServices.promisePut($scope.servicePath,$scope.selectedMondrianSchema.id,$scope.selectedMondrianSchema)
		.then(function(response) {
			
			console.log("[PUT]: SUCCESS!");
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');

			for(var j =0; j<$scope.itemList.length;j++){
							if($scope.itemList[j].id===response.data.id){
								for(var key in response.data){
									$scope.itemList[j][key] = response.data[key];
								}
								$scope.selectedMondrianSchema = angular.copy($scope.itemList[j]);
							}
						}
			
			
		if($scope.file.file){
			console.log("uploading...");
			$scope.uploadFile();
			
		}
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
	}
	
	
	//DELETE MONDRIAN SCHEMA
	$scope.deleteMondrianSchema = function(item){
		
		if($scope.selectedMondrianSchema.modelLocker!=null){
			
			sbiModule_messaging.showInfoMessage("Model is locked", 'Information!');
			console.log("model is locked");
			
		}else{
			console.log($scope.selectedMondrianSchema.modelLocker);
			
			sbiModule_restServices.promiseDelete($scope.servicePath,item.id)
			.then(function(response) {
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
				$scope.getMondrianSchemas();
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
		}
	}
	
	//DELETE VERSION
	$scope.deleteVersion = function(item){
		
		if($scope.selectedMondrianSchema.modelLocker!=null){
			
			sbiModule_messaging.showInfoMessage("Model is locked", 'Information!');
			console.log("model is locked");
			
		}else{
			
			sbiModule_restServices.promiseDelete("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions",item.id)
			.then(function(response) {
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
				$scope.getMondrianSchemasVersion();
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
		}
	}
	
};

/*
app.directive('fileModel',['$parse',function($parse){
	
	return {
		restrict:'A',
		link: function(scope,element,attrs){
			
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;
			console.log(modelSetter+"model");
			
			element.bind('change',function(){
				scope.$apply(function(){
					modelSetter(scope,element[0].files[0]);
					
					
				})
			})
		}
	}
	
	
}]);*/


app.service('multipartForm',['$http',function($http){
	
	this.post = function(uploadUrl,data){
		
		var formData = new FormData();
		
		for(var key in data){
			
			
				formData.append(key,data[key]);
			}

		return $http.post(uploadUrl,formData,{
			transformRequest:angular.identity,
			headers:{'Content-Type': undefined}
		})
	}
	
}]);


	


