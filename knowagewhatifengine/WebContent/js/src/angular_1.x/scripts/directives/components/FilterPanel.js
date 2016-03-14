angular.module('filter_panel',[])
.directive('filterPanel',function(){
	return{
		restrict: "E",
		replace: 'true',
		templateUrl: '/knowagewhatifengine/html/template/main/filter/filterPanel.html',
		controller: filterPanelController
	}
});

function filterPanelController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {

};

