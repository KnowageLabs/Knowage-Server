var app = angular.module('templateManagement', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'sbiModule', 'angular_2_col']);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
	'blue-grey');
});

app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	//main Controller
	 $scope.dayFormat = "d";

	    // To select a single date, make sure the ngModel is not an array.
	    $scope.selectedDate = null;

	    // If you want multi-date select, initialize it as an array.
	    $scope.selectedDate = [];

	    $scope.firstDayOfWeek = 0; // First day of the week, 0 for Sunday, 1 for Monday, etc.
	    $scope.setDirection = function(direction) {
	      $scope.direction = direction;
	      $scope.dayFormat = direction === "vertical" ? "EEEE, MMMM d" : "d";
	    };

	    $scope.dayClick = function(date) {
	      $scope.msg = "You clicked " + $filter("date")(date, "MMM d, y h:mm:ss a Z");
	    };

	    $scope.prevMonth = function(data) {
	      $scope.msg = "You clicked (prev) month " + data.month + ", " + data.year;
	    };

	    $scope.nextMonth = function(data) {
	      $scope.msg = "You clicked (next) month " + data.month + ", " + data.year;
	    };

	    $scope.tooltips = true;
	    $scope.setDayContent = function(date) {

	        // You would inject any HTML you wanted for
	        // that particular date here.
	        return "<p></p>";

	        // You could also use an $http function directly.
	        return $http.get("/some/external/api");

	        // You could also use a promise.
	        var deferred = $q.defer();
	        $timeout(function() {
	            deferred.resolve("<p></p>");
	        }, 1000);
	        return deferred.promise;

	    };

}