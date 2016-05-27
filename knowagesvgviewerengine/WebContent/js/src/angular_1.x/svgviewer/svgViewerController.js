var app = angular.module('myapp', ['ngMaterial']);

app.controller('MyController', function($scope, $mdSidenav) {
  $scope.isSidenavOpen = false;
    
  $scope.openLeftMenu = function() {
    $mdSidenav('left').toggle();
  };
    
  $scope.$watch('isSidenavOpen', function(isSidenavOpen) {
	  
  });
});