(function(){

	var app = angular.module("eventModule");

	app.service("eventService", function (sbiModule_restServices,$httpParamSerializer){

		this.getAllEvents = function (){
			return sbiModule_restServices.promiseGet("2.0/events","");

		}



		this.getQueryEvents = function (eventObjSerialized){

			return sbiModule_restServices.promiseGet("2.0/events","",$httpParamSerializer(eventObjSerialized));
		}

	});

}());