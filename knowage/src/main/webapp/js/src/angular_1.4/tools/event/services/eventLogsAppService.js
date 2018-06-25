(function(){

	var app = angular.module("eventModule");

	app.service("eventService", function (){

		var allEvents = [{

                "id" : 1,

                "user" : "biuser",

                "date" : "17/04/2018",

                "type" : "type1",

                "desc" : "someText"
            },

            {

                "id" : 2,

                "user" : "biadmin",

                "date" : "17/04/2018",

                "type" : "type2",

                "desc" : "someText"
            },

            {

                "id" : 3,

                "user" : "biuser",

                "date" : "17/04/2018",

                "type" : "type3",

                "desc" : "someText"

            }
        ]

		this.getAllEvents = function() {

			return allEvents;
		}

	});

}());