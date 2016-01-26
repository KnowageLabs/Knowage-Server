var app = angular.module('hierManager', ['ngMaterial','angular_list','angular_table','sbiModule','document_tree','ui.tree', 'angularUtils.directives.dirPagination']);

app.config(function($mdDateLocaleProvider) {
	 $mdDateLocaleProvider.parseDate = function(date) {
		 if (typeof date == "string"){
			 var re = /(\d{1,2})\/(\d{1,2})\/(\d{4})/; //RegExp for a date
			 var result = date.match(re);
			 var dateCorrect = false;
			 if (result && result.length == 4){
				 var day = result[1];
				 var mm = result[2];
				 var yy = result[3]
				 if (mm>=1 && mm<=12 && day>=1 && day <= 31){
					 if ( (mm == 11 || mm == 4 || mm == 6 || mm == 9) && day<=30){
						 dateCorrect = true;	
					 }else if ( mm == 2 && ( ((yy % 4) == 0 && day<=29) || ((yy % 4) != 0 && day<=28))){
						 dateCorrect = true;
					 }else if (mm == 1 || mm == 3 || mm == 5  || mm == 7 || mm == 8 || mm == 10 || mm == 12){
						 dateCorrect = true;
					 }
				 }
			 }
			 return dateCorrect == true ? new Date(result[3],result[2]-1,result[1]) : null; //Date has month starting from 0, so result[2]-1 is the correct month
		 }else{
		    return date;
		 }
	};
});

app.controller('hierCtrl', ['sbiModule_translate',"$scope",funzione]);

function funzione(sbiModule_translate, $scope){ 	
	sbiModule_translate.addMessageFile("messages");
	$scope.translate = sbiModule_translate;
	$scope.technicalLoaded = false;
	$scope.backupLoaded = false;
	
	$scope.loadTechnical = function(){
		$scope.technicalLoaded = true;
	}
	$scope.loadBackup = function(){
		$scope.backupLoaded = true;
	}

}
