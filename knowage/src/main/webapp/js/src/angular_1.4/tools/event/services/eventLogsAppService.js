(function(){

	var app = angular.module("eventModule");

	app.service("eventService", function (sbiModule_restServices,$httpParamSerializer){

		this.getAllEvents = function (filter){

			return sbiModule_restServices.promiseGet("2.0/events", "", $httpParamSerializer(filter));

		}

	});

}());