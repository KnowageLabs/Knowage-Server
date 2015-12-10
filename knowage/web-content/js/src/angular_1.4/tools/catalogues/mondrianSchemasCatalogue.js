/**
 * 
 */

var app = angular.module('mondrianSchemasCatalogueModule',['ngMaterial' , 'angular_list' , 'angular_table' , 'sbiModule' , 'angular_2_col']);

app.controller('mondrianSchemasCatalogueController',["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","$filter","multipartForm",mondrianSchemasCatalogueFunction]);

function mondrianSchemasCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,$filter,multipartForm){
	
	$scope.translate = sbiModule_translate;
	$scope.showMe = false;
	$scope.selectedMondrianSchema={};
	$scope.selectedVersion = {};
	$scope.itemList = [];
	$scope.fileList = [];
	$scope.servicePath = "2.0/mondrianSchemasResource";
	
	$scope.print = function(){
	console.log($scope.selectedMondrianSchema);
	};
	
	
	
	angular.element(document).ready(function () {
        $scope.getMondrianSchemas();
		
		
    });
	
	
	
	$scope.catalogueSpeedOptions =  [{
	        
		label:sbiModule_translate.load("sbi.generic.delete"),
	    icon:'fa fa-trash-o fa-lg',
	    color:'#153E7E',
	    action:function(item){
	    $scope.deleteMondrianSchema(item);
			
	    }
	 }];
	
	$scope.versionsSpeedOptions =  [
	                                
	  
	                                
	   {
	        
		label:sbiModule_translate.load("sbi.generic.delete"),
	    icon:'fa fa-trash-o fa-lg',
	    color:'#153E7E',
	    action:function(item){
	    $scope.deleteVersion(item);
			
	    }
	  }
	  
	  ];
	
	
	
	
	
	
	
	$scope.saveMondrianCatalogue = function(){
		/*
		console.log("saving Schema...");
		
		if(!isNaN($scope.selectedMondrianSchema.id)){
			
				console.log("updating...");
				$scope.modifyMondrianSchema();
				
				
		}else{
			
			console.log("adding new...");
			$scope.addNewMondrianSchema();
			
			
		}
		
		
		console.log("saved!!!");
		
		*/
		console.log("save function");
		multipartForm.post("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions",$scope.selectedMondrianSchema).success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
					
				}else{
					
					console.log("[UPLOAD]: SUCCESS!");
				$scope.fileList =$scope.getMondrianSchemasVersion();
					
				}
			}).error(function(data, status, headers, config) {
						console.log("[UPLOAD]: FAIL!"+status);
					});
		
	};
	
	
	
	
	$scope.createDragan = function(){
		$scope.showMe = true;
		$scope.selectedMondrianSchema = {};
		$scope.selectedVersion = {};
		$scope.fileList = [];
		
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
		
		sbiModule_restServices.get($scope.servicePath,"").success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					
					
				}else{
					
					$timeout(function(){$scope.itemList = data;}, 500);
					
				}
			}
		
		)};
	
	//GET ALL MONDRIAN SCHEMAS VERSIONS
	$scope.getMondrianSchemasVersion = function(){
		
		sbiModule_restServices.get("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions","").success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					
					
				}else{
					
					$timeout(function(){
						
						$scope.fileList = data;
						for(var i = 0; i<data.length;i++){
						
						$scope.fileList[i].creationDate = new Date(data[i].creationDate);
					
						$scope.fileList[i].actives = "<md-radio-button ng-value = "+$scope.fileList[i].id+" aria-label='label'></md-radio-button>";
						if($scope.fileList[i].active){
							$scope.selectedMondrianSchema.currentContentId = $scope.fileList[i].id;
						}
						
					} 	
						}, 500);
					
					
					
				}
			}
		
		)};
	
	//POST NEW MONDRIAN SCHEMA
	$scope.addNewMondrianSchema = function(){
		
		$scope.selectedMondrianSchema.type ="MONDRIAN_SCHEMA";
		console.log($scope.selectedMondrianSchema);
		
		sbiModule_restServices.post($scope.servicePath,"",$scope.selectedMondrianSchema).success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					
					
				}else{
					
						
				$scope.itemList =	$scope.getMondrianSchemas();
				}
			}
		
		)};
		
		
	
	
	//PUT MODIFY MONDRIAN SCHEMA
	$scope.modifyMondrianSchema = function(){
		
		sbiModule_restServices.put($scope.servicePath,$scope.selectedMondrianSchema.id,$scope.selectedMondrianSchema).success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					console.log("[PUT]: DATA HAS ERRORS PROPERTY!");
					
				}else{
					
					console.log("[PUT]: SUCCESS!");
					$scope.itemList = $scope.getMondrianSchemas();
					$scope.getMondrianSchemas();
					
				}
			}).error(function(data, status, headers, config) {
						console.log("[PUT]: FAIL!"+status);
					});
	
	}
	
	
	//DELETE MONDRIAN SCHEMA
	$scope.deleteMondrianSchema = function(item){
		
		console.log($scope.selectedMondrianSchema);
		
		sbiModule_restServices.delete($scope.servicePath,item.id).success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					console.log("[DELETE]: DATA HAS ERRORS PROPERTY!");
					
				}else{
					console.log("[DELETE]: SUCCESS!")					
					$scope.itemList = $scope.getMondrianSchemas();
	
				}
			}).error(function(data, status, headers, config) {
						console.log("[DELETE]: FAIL!"+status);
					});
		
	}
	
	//DELETE VERSION
	$scope.deleteVersion = function(item){
		
		sbiModule_restServices.delete("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions",item.id).success(
			
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					
					console.log("[DELETE]: DATA HAS ERRORS PROPERTY!");
					
				}else{
					
					console.log("[DELETE]: SUCCESS!")									
					$scope.fileList = $scope.getMondrianSchemasVersion();
								
					
				}
			}).error(function(data, status, headers, config) {
						console.log("[DELETE]: FAIL!"+status);
					});
	
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
		
		for(var key in data){
			
			if(key==="file"){
				formData.append(key,data[key]);
			}
			
				
			
			
		}
			
		
		
	return	$http.post(uploadUrl,formData,{
			transformRequest:angular.identity,
			headers:{'Content-Type': undefined}
		})
	}
	
}])
	


