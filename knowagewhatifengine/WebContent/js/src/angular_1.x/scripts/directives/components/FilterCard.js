angular.module('filter_card',[])
	.directive('filterCard', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/filter/filterCard.html',
		      controller:filterCardController
		  };
	});

function filterCardController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
	
	$scope.showMultiHierDialog = function(ev){
		$scope.showDialog(ev,"/main/filter/multiHierarchyDialog.html");
	};

};