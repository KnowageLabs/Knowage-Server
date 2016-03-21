angular.module('sbi_side_nav',[])
.directive('sbiSideNav',function(){
	return{
		restrict: "E",
		replace: 'true',
		templateUrl: '/knowagewhatifengine/html/template/right/sideNavigation.html',
		controller: sideNavigationController
	}
});

function sideNavigationController($scope, $timeout, $window, $mdDialog, $mdSidenav, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
	
	$scope.tableIcon=true;
	$scope.chartIcon=false;
	$scope.toggleRight = buildToggler('right');

	function buildToggler(navID) {
	      return function() {
	        $mdSidenav(navID)
	          .toggle();
	      }
	    }
	
	$scope.closeSideNav = function(navId){
		$mdSidenav(navId).close();
	}
	
	$scope.changeDataRepr = function(id){
		$scope.tableIcon= id == "table" || id == "both" ?true:false;
		$scope.chartIcon= id == "chart" || id == "both"?true:false;
	}
}