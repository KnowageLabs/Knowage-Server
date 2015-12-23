var app = angular.module('hierManager', ['ngMaterial','ng-context-menu','angular_table', 'angular_2_col','sbiModule','document_tree']);

app.factory('glDimension',function(){
	 var dimensione = {};
	 return dimensione;
});

app.controller('hierCtrl', ['sbiModule_translate',"$scope",funzione ]);

function funzione(sbiModule_translate, $scope){ 	
	sbiModule_translate.addMessageFile("messages");
	$scope.translate = sbiModule_translate;
}
