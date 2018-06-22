(function(){

var app = angular.module("eventModule").controller("eventController",["$scope","eventService" ,function ($scope,eventService){

	$scope.test = "nesto";
	$scope.events = eventService.getAllEvents();

}])









}
())