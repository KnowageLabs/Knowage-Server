var app = angular.module('hierManager', ['ngMaterial','ng-context-menu','angular_table','sbiModule','document_tree','ui.tree', 'angularUtils.directives.dirPagination']);

app.factory('glDimension',function(){
	 var dimensione = {};
	 return dimensione;
});

app.controller('hierCtrl', ['sbiModule_translate',"$scope",funzione ]);

function funzione(sbiModule_translate, $scope){ 	
	sbiModule_translate.addMessageFile("messages");
	$scope.translate = sbiModule_translate;
	
}
