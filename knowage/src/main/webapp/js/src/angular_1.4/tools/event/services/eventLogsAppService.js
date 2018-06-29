(function(){

	var app = angular.module("eventModule");

	app.service("eventService", function (sbiModule_restServices){

		this.getAllEvents = function (){
			return sbiModule_restServices.promiseGet("2.0/events","");

		}

	});

}());