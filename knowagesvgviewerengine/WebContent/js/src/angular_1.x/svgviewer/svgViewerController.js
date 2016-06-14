var app = angular.module('svgViewerApp', ['ngMaterial']);

app.controller('SvgViewerController', function($scope, $mdSidenav,$http) {
  $scope.isSidenavOpen = false;
    
  $scope.openSideNav = function() {
    $mdSidenav('svgSideNav').toggle();
  };
    
  $scope.$watch('isSidenavOpen', function(isSidenavOpen) {
	  
  });
  
  $http.get('http://localhost:8080/knowagesvgviewerengine/api/1.0/svgviewer/getMeasures').
	  success(function(data) {
	      $scope.measures = data;
	  });
});