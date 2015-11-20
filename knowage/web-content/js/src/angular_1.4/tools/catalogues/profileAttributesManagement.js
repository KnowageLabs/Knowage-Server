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
	
	$scope.attributeList=[
	                   { 
	                	 ID:'1',
	                	 NAME:'name',
	                	 DESCRIPTION:'name'
	                   },
	                   {     
	                	     ID:'2',
		                	 NAME:'email',
		                	 DESCRIPTION:'email'
		               },
		               {
		                	 ID:'3',
		            	     NAME:'birth',
		                	 DESCRIPTION:'birth'
		               },
		                 {   
		            	     ID:'4',
		                	 NAME:'name',
		                	 DESCRIPTION:'name'
		                   },
		                   {
			                	 ID:'5',
		                	     NAME:'email',
			                	 DESCRIPTION:'email'
			               }
	                   ];
	
	$scope.createProfileAttribute=function(){
		$scope.showMe=true;
	}
	$scope.saveProfileAttribute=function(){
		console.log("IN Save profile attribute");
		console.log($scope.selectedAttribute);
	}
	
	$scope.loadAttribute=function(item){
		console.log(item);
		$scope.selectedAttribute=angular.copy(item);
		$scope.showMe=true;
	}
	
	$scope.cancel=function(){
		$scope.selectedAttribute={};
		$scope.showMe=false;
	}
	
	$scope.deleteItem=function(item,event){
		console.log("delete");
		console.log(item);
		console.log(event);
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