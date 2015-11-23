var app=angular.module("ModalitiesCheckModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("ModalitiesCheckController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",ModalitiesCheckFunction]);
function ModalitiesCheckFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.showme = false;
	$scope.additionalField= false;
	$scope.translate=sbiModule_translate;
	$scope.SelectedConstraint={};
	$scope.label="";
	
	$scope.TestItemList=[];
	                     
	
	$scope.listType = [
	                   {value : 'Date', label :'Date' },
	                   {value : 'Regexp', label:'Regexp' },
	                   {value : 'Max Length',label:'Max Length' },
	                   {value : 'Range', label:'Range' },
	                   {value : 'Decimal', label:'Decimal' },
	                   {value : 'Min Length', label:'Min Length' }
	                   ];

	$scope.FieldsCheck = function(l){
		if(l.value == "Range"){
			$scope.additionalField= true;
		}else{
			$scope.additionalField= false;
		}
		
		switch (l.value) {
		case "Date":
			$scope.label="Date Value Format";	
			break;
		case "Regexp":
			$scope.label="Regular Expression";	
			break;
		case "Max Length":
			$scope.label="Max Length Value";	
			break;
		case "Range":
			$scope.label="Lower Range Value";
			break;
		case "Decimal":
			$scope.label="Decimal Places";
			break;
		case "Min Length":
			$scope.label="Min Length Value";	
			break;	
		default:
			$scope.label="Date Value Format";
			break;
		}
		
	}                     

	$scope.loadConstraints=function(item){
		$scope.SelectedConstraint=angular.copy(item);
		$scope.showme=true;
		//$scope.button_flag = false;
	} 	                
	
	
	
	$scope.createConstraints =function(){
		console.log("radi");
		$scope.showme=true;
		//$scope.button_flag = true;
		
	
	}
	$scope.saveConstraints= function(){
		
		$scope.TestItemList.push($scope.SelectedConstraint);
		
	
	}
	$scope.cancel = function() {
		$scope.showme = false;
		$scope.SelectedConstraint={};
		
	}
	
	$scope.tableFunction=function(){
		
		
		
	for (var int = 0; int < $scope.TestItemList.length; int++) {
		
		$scope.TestItemList[int].icon =
			'<md-button class="md-icon-button" ng-click="tableFunction.deleteConstraint()"> <md-icon md-font-icon="fa fa-trash-o fa-lg" style=" margin-top: 6px ; color: #153E7E;"></md-icon> </md-button>';
			
	}
	

	}
};