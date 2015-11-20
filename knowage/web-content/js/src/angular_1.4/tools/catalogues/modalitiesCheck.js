var app=angular.module("ModalitiesCheckModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("ModalitiesCheckController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",ModalitiesCheckFunction]);
function ModalitiesCheckFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.showme = false;
	$scope.translate=sbiModule_translate;
	$scope.predefined=[];
	$scope.SelectedConstraint={};
	$scope.TestItemList=[];
	                     
	
	$scope.listType = [
	                   {value : 'Date', label :'Date' },
	                   {value : 'Regexp', label:'Regexp' },
	                   {value : 'Max Length',label:'Max Length' },
	                   {value : 'Range', label:'Range' },
	                   {value : 'Decimal', label:'Decimal' },
	                   {value : 'Min Length', label:'Min Length' }
	                   ];

	                     

	$scope.loadConstraints=function(item){
		$scope.SelectedConstraint=item;
		$scope.showme=true; 
	} 	                
	
	
	
	$scope.createConstraints =function(){
		console.log("radi");
		$scope.showme=true;
	
	}
	$scope.saveConstraints= function(){
		
		$scope.TestItemList.push($scope.SelectedConstraint);
		
	
	}
	$scope.cancel = function() {
		$scope.showme = false;
		$scope.SelectedConstraint={};
		
	}
	$scope.getPredefined = function() {
		
		
		sbiModule_restServices.get("datasources", '').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
					
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						console.log("got predefined constraints list");
						
						
						$scope.predefined = data.root;
						console.log($scope.dataSourceList);
						
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				})	
		
	} 
	$scope.getPredefined();
	
	$scope.deleteItem=function(){
		  console.log("delete");
		 }
		 $scope.mcSpeedMenu= [
		                      {
		                      label:sbiModule_translate.load("sbi.generic.delete"),
		                      icon:'fa fa-minus',
		                      backgroundColor:'red',
		                      color:'white',
		                      action:function(item){
		                      $scope.deleteItem(item);
		                      }
		                      }
		                     ];
		 
		 

};