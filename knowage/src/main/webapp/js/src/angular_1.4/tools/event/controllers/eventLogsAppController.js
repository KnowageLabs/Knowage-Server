(function(){

var app = angular.module("eventModule").controller("eventController",["$scope","eventService","sbiModule_messaging" ,function ($scope,eventService,sbiModule_messaging){

	eventService.getAllEvents().then(function(response){
		$scope.events = response.data.results;

//		sbiModule_messaging.showInfoMessage(response.data.total, "Total Events Number")

	}, function(response){

		if(response.data.errors){
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message,"Error with message");
		} else {
			sbiModule_messaging.showErrorMessage("Error without error message","Error without message")
		}

	})

	$scope.showDetail = false;
	$scope.selectedDetail = {};

	$scope.loadDetail = function(item){
		$scope.showDetail = true;
		$scope.selectedDetail = angular.copy(item);

	}

}])

}())