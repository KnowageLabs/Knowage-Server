(function(){

var app = angular.module("eventModule").controller("eventController",["$scope","eventService" ,function ($scope,eventService){

	$scope.events = eventService.getAllEvents();
	$scope.showDetail = false;
	$scope.selectedDetail = {};

	$scope.loadDetail = function(item){
		$scope.showDetail = true;
		$scope.selectedDetail = angular.copy(item);

	}

}])





















}
())