var app = angular.module('hierManager', ['ngMaterial','ng-context-menu','angular_list','angular_table','sbiModule','document_tree','ui.tree', 'angularUtils.directives.dirPagination']);

app.config(function($mdDateLocaleProvider) {
	 $mdDateLocaleProvider.parseDate = function(date) {
		 if (typeof date == "string"){
			 var tmp = date.split('/');
			 return new Date(tmp[2],tmp[1],tmp[0]);
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
