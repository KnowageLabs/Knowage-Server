/**
 * 
 */
var app=angular.module('profileAttributesManagementModule',['ngMaterial','angular_list','angular_table','sbiModule','angular_2_col']);

app.controller('profileAttributesManagementController',["sbiModule_translate","sbiModule_restServices","$scope","$mdDialog","$mdToast","$timeout",profileAttributesManagementFunction]);

function profileAttributesManagementFunction(sbiModule_translate,sbiModule_restServices,$scope,$mdDialog,$mdToast,$timeout){
	
	$scope.translate=sbiModule_translate;
	$scope.showMe=false;
	$scope.dirtyForm=false;
	$scope.tempObj=[];
	$scope.selectedAttribute={};
	$scope.attributeList=[];
	$scope.tempObj = [];
	
	angular.element(document).ready(function () {
        $scope.getProfileAttributes();
    });
	
	
	$scope.setDirty=function(){
		$scope.dirtyForm=true;
	}
	
	$scope.getProfileAttributes= function(){
		
		sbiModule_restServices.get("2.0/attributes", '').success(
				function(data, status, headers, config) {
				       
						$scope.attributeList = data;
						
				}).error(function(data, status, headers, config) {
					
				})
		
	}
	
	

	
	
	$scope.createProfileAttribute=function(){
		
		if($scope.dirtyForm){
			$mdDialog.show($scope.confirm).then(function(){
				
     		    $scope.dirtyForm=false;
     		   $scope.selectedAttribute={};
				$scope.showMe=true;
     		    
			},function(){
				
				$scope.showMe=true;
			});
			
		}else{
	
			$scope.showMe=true;
			$scope.selectedAttribute={};
		}
		
	}
	
	$scope.saveProfileAttribute=function(){

		if($scope.selectedAttribute.hasOwnProperty("attributeId")){ // put, update existing
			sbiModule_restServices.put('2.0/attributes',$scope.selectedAttribute.attributeId,$scope.selectedAttribute).success(
				function(data, status, headers, config){
					
					for(i=0;i<$scope.attributeList.length;i++){
						if($scope.attributeList[i].attributeId===$scope.selectedAttribute.attributeId){
							$scope.attributeList[i].attributeName=$scope.selectedAttribute.attributeName;
							$scope.attributeList[i].attributeDescription=$scope.selectedAttribute.attributeDescription;
						}
					}
					$scope.dirtyForm=false;
					$scope.showActionOK();
				}).error(function(data,status,headers,config){
					
				})
			                        
			
		}else{// post create new
			sbiModule_restServices.post('2.0/attributes','',$scope.selectedAttribute).success(
					function(data, status, headers, config){
				        $scope.attributeList.push(data);  
				        $scope.dirtyForm=false;
						$scope.showActionOK();
						
					}).error(function(data,status,headers,config){
						
					})
		}
		
	}
	
	$scope.showActionOK = function() {
		var toast = $mdToast.simple()
		.content(sbiModule_translate.load("sbi.attributes.ok.msg"))
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
		
		if($scope.dirtyForm){
			$mdDialog.show($scope.confirm).then(function(){
				
     		    $scope.dirtyForm=false;
				$scope.selectedAttribute=angular.copy(item);
				$scope.showMe=true;
     		    
			},function(){
				
				$scope.showMe=true;
			});
			
		}else{
	
		$scope.selectedAttribute=angular.copy(item);
		$scope.showMe=true;
		}
	}
	
	$scope.cancel=function(){
		$scope.selectedAttribute={};
		$scope.showMe=false;
		$scope.dirtyForm=false;
	}
	
	$scope.deleteItem=function(item){
		
		sbiModule_restServices.delete('2.0/attributes',item.attributeId).success(
				function(data, status, headers, config){
					
					$scope.showActionOK();
					$scope.attributeList=[];
					$timeout(function(){								
						$scope.getProfileAttributes();
					}, 1000);
					$scope.selectedAttribute={};
					$scope.showMe=false;
				}).error(function(data,status,headers,config){
					
				})
	}
		
	$scope.deleteAttributes=function(){
		
		params="?";
		for(i=0;i<$scope.tempObj.length;i++){
			params+="id="+$scope.tempObj[i].attributeId+"&";
		}
		
		sbiModule_restServices.delete('2.0/attributes',params).success(
				function(data, status, headers, config){
					
					$scope.tempObj=[];
					$scope.attributeList=[];
					$timeout(function(){								
						$scope.getProfileAttributes();
					}, 1000);
					$scope.selectedAttribute={};
					$scope.showMe=false;
					$scope.showActionOK();
					
				}).error(function(data,status,headers,config){
					
				})
		
	}	
		
	
	$scope.paMenu=[
	               {
	            	  label:'delete',
	            	  action:function(item,event){
	            		  console.log(item);
	            	  }
	               }
	               ];
	
	$scope.paSpeedMenu= [
	                     {
	                    	label:'delete',
	                    	icon:'fa fa-trash-o fa-lg',
	                    	color:'#153E7E',
	                    	action:function(item,event){
	                    		
	                    		$scope.deleteItem(item);
	                    	}
	                     }
	                    ];
	
	 $scope.confirm = $mdDialog
	.confirm()
	.title(sbiModule_translate.load("sbi.layer.modify.progress"))
	.content(
			sbiModule_translate
			.load("sbi.layer.modify.progress.message.modify"))
			.ariaLabel('Lucky day').ok(
					sbiModule_translate.load("sbi.general.continue")).cancel(
							sbiModule_translate.load("sbi.general.cancel"));
	
	
}