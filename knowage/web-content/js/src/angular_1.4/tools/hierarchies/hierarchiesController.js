var app = angular.module('hierManager', ['ngMaterial','angular_list','angular_table','sbiModule','document_tree','ui.tree', 'angularUtils.directives.dirPagination']);

app.config(function($mdDateLocaleProvider) {
	 $mdDateLocaleProvider.parseDate = function(date) {
		 if (typeof date == "string"){
			 var re = /(\d{1,2})\/(\d{1,2})\/(\d{4})/; //RegExp for a date
			 var result= date.match(re);
			 if (result && result.length == 4){
				 return new Date(result[3],result[2]-1,result[1]); //Date has month starting from 0, so result[2]-1 is the correct month
			 }else{
				 return null;
			 }
		 }else{
		    return date;
		 }
	};
});

app.controller('hierCtrl', ['sbiModule_translate',"$scope",funzione ]);

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
