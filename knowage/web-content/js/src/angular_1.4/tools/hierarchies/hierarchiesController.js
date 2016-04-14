var app = angular.module('hierManager', ['ngMaterial','angular_list','angular_table','sbiModule','document_tree','ui.tree', 'angularUtils.directives.dirPagination']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
	   $mdThemingProvider.theme('knowage')
	   $mdThemingProvider.setDefaultTheme('knowage');
	}]);

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
