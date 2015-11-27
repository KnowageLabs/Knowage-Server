/**
 * 
 */
var app=angular.module('profileAttributesManagementModule',['ngMaterial','angular_list','angular_table','sbiModule','angular_2_col']);

app.controller('profileAttributesManagementController',["sbiModule_translate","sbiModule_restServices","$scope","$mdDialog","$mdToast",profileAttributesManagementFunction]);

function profileAttributesManagementFunction(sbiModule_translate,sbiModule_restServices,$scope,$mdDialog,$mdToast){
	
	$scope.translate=sbiModule_translate;
	$scope.showMe=false;
	$scope.selectedAttribute={};
	$scope.attributeList=[];
	
	
	$scope.getProfileAttributes= function(){
		console.log("Get Attributes");
		sbiModule_restServices.get("2.0/attributes", '').success(
				function(data, status, headers, config) {
				       
					
						$scope.attributeList = data;
						console.log($scope.attributeList);
						
						for(i=0;i<$scope.attributeList.length;i++){
							//$scope.attributeList[i].action="<md-button ng-click='deleteItem(row,column,event)'><md-icon md-font-icon='fa fa-trash' aria-label='deleteItem'></md-icon></md-button>";
						}
						
					
				}).error(function(data, status, headers, config) {
					console.log(status);

				})
		
	}
	
	$scope.getProfileAttributes();
	
	$scope.createProfileAttribute=function(){
		$scope.showMe=true;
		$scope.selectedAttribute={};
	}
	$scope.saveProfileAttribute=function(){
		console.log("IN Save profile attribute");
		console.log($scope.selectedAttribute);
		if($scope.selectedAttribute.hasOwnProperty("attributeId")){ // put, update existing
			sbiModule_restServices.put('2.0/attributes',$scope.selectedAttribute.attributeId,$scope.selectedAttribute).success(
				function(data, status, headers, config){
					console.log(data,status,headers,config);
					for(i=0;i<$scope.attributeList.length;i++){
						if($scope.attributeList[i].attributeId===$scope.selectedAttribute.attributeId){
							$scope.attributeList[i].attributeName=$scope.selectedAttribute.attributeName;
							$scope.attributeList[i].attributeDescription=$scope.selectedAttribute.attributeDescription;
						}
					}
					
					$scope.showActionOK();
				}).error(function(data,status,headers,config){
					console.log(status);
				})
			                        
			
		}else{// post create new
			sbiModule_restServices.post('2.0/attributes','',$scope.selectedAttribute).success(
					function(data, status, headers, config){
				        $scope.attributeList.push(data);  
						$scope.showActionOK();
						
					}).error(function(data,status,headers,config){
						console.log(status);
					})
		}
		
	}
	
	$scope.showActionOK = function() {
		var toast = $mdToast.simple()
		.content('Attribute saved corretly')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};
	
	$scope.loadAttribute=function(item){
		console.log(item);
		$scope.selectedAttribute=angular.copy(item);
		$scope.showMe=true;
	}
	
	$scope.cancel=function(){
		$scope.selectedAttribute={};
		$scope.showMe=false;
	}
	
	$scope.deleteItem=function(){
		console.log("delete");
		console.log()
		sbiModule_restServices.delete('2.0/attributes',$scope.selectedAttribute.attributeId).success(
				function(data, status, headers, config){
					console.log(data,status,headers,config);
					$scope.showActionOK();
					$scope.attributeList=[];
					$scope.getProfileAttributes();
				}).error(function(data,status,headers,config){
					console.log(status);
				})
	}
		
		
		
	
	$scope.paMenu=[
	               {
	            	  label:'delete',
	            	  action:function(item,event){
	            		  $scope.deleteItem(item,event);
	            	  }
	               }
	               ];
	
	$scope.paSpeedMenu= [
	                     {
	                    	label:'delete',
	                    	icon:'fa fa-minus',
	                    	backgroundColor:'red',
	                    	color:'white',
	                    	action:function(item,event){
	                    		$scope.deleteItem(item,event);
	                    	}
	                     }
	                    ];
}