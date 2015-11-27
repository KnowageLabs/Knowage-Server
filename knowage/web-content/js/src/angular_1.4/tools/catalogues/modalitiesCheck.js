var app=angular.module("ModalitiesCheckModule",["ngMaterial","angular_list","angular_table","sbiModule","angular_2_col"]);
app.controller("ModalitiesCheckController",["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",ModalitiesCheckFunction]);
function ModalitiesCheckFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.showme = false;
	$scope.additionalField= false;
	$scope.translate=sbiModule_translate;
	$scope.SelectedConstraint={};
	$scope.predefined =[];
	$scope.label="";
	$scope.counter =0;
	$scope.ItemList=[];
	                     
	
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
			$scope.label=sbiModule_translate.load("sbi.modalities.check.details.date");	
			break;
		case "Regexp":
			$scope.label=sbiModule_translate.load("sbi.modalities.check.details.regexp");	
			break;
		case "Max Length":
			$scope.label=sbiModule_translate.load("sbi.modalities.check.details.max");	
			break;
		case "Range":
			$scope.label=sbiModule_translate.load("sbi.modalities.check.details.rangeMin");
			break;
		case "Decimal":
			$scope.label=sbiModule_translate.load("sbi.modalities.check.details.decimal");
			break;
		case "Min Length":
			$scope.label=sbiModule_translate.load("sbi.modalities.check.details.min");	
			break;	
		default:
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
		$scope.SelectedConstraint={};
		$scope.showme=true;
		
		//$scope.button_flag = true;
		
	
	}
	$scope.saveConstraints= function(){
		
		console.log("saving Constraint...");
		  console.log($scope.SelectedConstraint.ID);
		  if(!isNaN($scope.SelectedConstraint.ID)){
		   console.log("updating...");
		    for(var i=0;i<$scope.TestItemList.length;i++){
		     console.log($scope.TestItemList[i].NAME);
		    if($scope.TestItemList[i].ID === $scope.SelectedConstraint.ID){
		     
		     $scope.TestItemList[i] =angular.copy($scope.SelectedConstraint);
		     }
		    }
		  }else{
		   console.log("adding new...");
		   $scope.SelectedConstraint.ID=$scope.counter;
		   $scope.TestItemList.push($scope.SelectedConstraint);
		   $scope.counter++;
		   
		  }
		  
		  
		  console.log("saved!!!");
		
	
	}
	$scope.cancel = function() {
		$scope.showme = false;
		$scope.SelectedConstraint={};
		
	}
	$scope.getPredefined = function(){
		sbiModule_restServices.get("2.0", 'modalities').success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						$scope.predefined = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				})	
	}
	$scope.getPredefined();
	
	$scope.getCustom = function(){
		sbiModule_restServices.get("2.0", 'detailmodalities').success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						$scope.ItemList = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				})	
	}
	$scope.getCustom();			
};
