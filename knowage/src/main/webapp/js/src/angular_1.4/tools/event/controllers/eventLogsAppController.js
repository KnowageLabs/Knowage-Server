(function(){

var app = angular.module("eventModule").controller("eventController",["$scope","eventService","sbiModule_messaging", function ($scope,eventService,sbiModule_messaging){

	$scope.eventSelectModel = ["SCHEDULER", "ETL", "COMMONJ", "DATA_MINING"]
	$scope.eventSearch = function (startDate,endDate,type){

		getQueryEvents();

	}

	$scope.pageChangedFun = function(itemsPerPage, currentPageNumber) {
		
		var ev = {
			offset: $scope.offset,
			fetchsize: itemsPerPage
		}
		
		$scope.fetchsize = itemsPerPage;
		
		if(currentPageNumber > 1) {
			$scope.offset = currentPageNumber * $scope.fetchsize;
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

			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, "Error");

		});

	}

}])

}())