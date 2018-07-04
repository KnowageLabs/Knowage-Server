(function(){

var app = angular.module("eventModule").controller("eventController",["$scope","eventService","sbiModule_messaging", function ($scope,eventService,sbiModule_messaging){

	$scope.eventSelectModel = ["SCHEDULER", "ETL", "COMMONJ", "DATA_MINING"]
	$scope.pageChangedFun = function(itemsPerPage, currentPageNumber) {

		var ev = {
			offset: $scope.offset,
			fetchsize: itemsPerPage
		}

		$scope.fetchsize = itemsPerPage;

		$scope.offset = 0;

		if(currentPageNumber > 1) {
			$scope.offset = (currentPageNumber -1) * $scope.fetchsize;
		}

		eventService.getAllEvents(ev).then(function(response) {

			$scope.events = response.data.results;
			$scope.totalItemCountt = response.data.total;

		}, function(response) {

			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, "Error");

		});

	}

	$scope.showDetail = false;
	$scope.selectedDetail = {};

	$scope.loadDetail = function(item){
		$scope.showDetail = true;
		$scope.selectedDetail = angular.copy(item);

	}

	$scope.searchEvents = function (){

		var startDateFormat = moment($scope.startDate).format("YYYY-MM-DD HH:mm:ss");
		var endDateFormat = moment($scope.endDate).format("YYYY-MM-DD HH:mm:ss");
		var filter = {

			startDate:startDateFormat,
			endDate:endDateFormat,
			type:$scope.type
		}
		eventService.getAllEvents(filter)
		.then(function(response){
			$scope.events=response.data.results;
		},	  function(response){

			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, "Error");

		});

	}

}])

}())