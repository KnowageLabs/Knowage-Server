(function(){

	var app = angular.module("eventModule");

	app.service("eventService", function (sbiModule_restServices,$httpParamSerializer){

		this.getAllEvents = function (ev){
			
			return sbiModule_restServices.promiseGet("2.0/events", "", $httpParamSerializer(ev));

		}
		
		this.getQueryEvents = function (eventObjSerialized){
			
			return sbiModule_restServices.promiseGet("2.0/events","",$httpParamSerializer(eventObjSerialized));
		}

	});

}());