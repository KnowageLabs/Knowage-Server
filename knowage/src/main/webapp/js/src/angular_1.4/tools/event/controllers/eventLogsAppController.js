(function(){

var app = angular.module("eventModule").controller("eventController",["$scope","eventService","sbiModule_messaging", function ($scope,eventService,sbiModule_messaging){

	eventService.getAllEvents().then(function(response){
		$scope.events = response.data.results;

	}, function(response){

		if(response.data.errors){
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message,"Error with message");
		} else {
			sbiModule_messaging.showErrorMessage("Error without error message","Error without message")
		}

	})

	$scope.eventSelectModel = ["SCHEDULER", "ETL", "COMMONJ", "DATA_MINING"]
	$scope.eventSearch = function (startDate,endDate,type){

		getQueryEvents();

	}

	$scope.showDetail = false;
	$scope.selectedDetail = {};

	$scope.loadDetail = function(item){
		$scope.showDetail = true;
		$scope.selectedDetail = angular.copy(item);

	}

	$scope.getQEvents = function (){

		var startDateFormat = moment($scope.startDate).format("YYYY-MM-DD HH:mm:ss");
		var endDateFormat = moment($scope.endDate).format("YYYY-MM-DD HH:mm:ss");
		var eventObjSerialized = {

				startDate:startDateFormat,
				endDate:endDateFormat,
				type:$scope.type
		}
		eventService.getQueryEvents(eventObjSerialized)
		.then(function(response){
			$scope.events=response.data.results;
		},	  function(response){

			sbiModule_messaging.showErrorMessage(response.data.errors[0].message,"nani")
			sbiModule_messaging.showErrorMessage("no error property","nani")

		});

	}



}])

}())