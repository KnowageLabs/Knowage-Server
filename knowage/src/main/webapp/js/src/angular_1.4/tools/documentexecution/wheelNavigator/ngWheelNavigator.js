/* Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. */

(function() {	
	
	angular.module('documentExecutionModule').directive('wheelNavigator', function($window) {
		
		function link(scope, element, attrs) {
			
			scope.baseUrl 	= "http://161.27.39.83:8080/knowage/servlet/AdapterHTTP?";
			scope.params	= "ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&IS_SOURCE_DOCUMENT=true&OBJECT_LABEL=";
			
			//Items list for the navigator. ID is the index of the element to be shown and the link is the page to open on click
			scope.pieItems = [
			                {id:0,link: scope.baseUrl+scope.params+"VIEW_OBJECTS&OBJECT_NAME=VIEW_OBJECTS"},
			                {id:1,link: scope.baseUrl+scope.params+"VIEW_CONTRACT&OBJECT_NAME=VIEW_CONTRACT"},
			                {id:2,link: scope.baseUrl+scope.params+"VIEW_EVENT&OBJECT_NAME=VIEW_EVENT"},
			                {id:3,link: scope.baseUrl+scope.params+"VIEW_TICKETS&OBJECT_NAME=VIEW_TICKETS"},
			                {id:4,link: scope.baseUrl+scope.params+"VIEW_BACKUPS&OBJECT_NAME=VIEW_BACKUPS"},
			                {id:5,link: scope.baseUrl+scope.params+"VIEW_AVAILABILITY&OBJECT_NAME=VIEW_AVAILABILITY"},
			                {id:6,link: scope.baseUrl+scope.params+"VIEW_BACKLOGS&OBJECT_NAME=VIEW_BACKLOGS"}
			                ];
			
			//Pie menu constructor from http://pmg.softwaretailoring.net/. To change the number of element or tooltip those properties must be changed
			var piemenu = new wheelnav('piemenu');		
				piemenu.clockwise = false;
				piemenu.wheelRadius = piemenu.wheelRadius * 0.83;
				piemenu.navItemsContinuous = true;
				piemenu.sliceAngle = 25.714285714285715;
				piemenu.createWheel();
				piemenu.setTooltips(['Summary','Contracts','Events','Tickets','Backups','Availability','Backlog']);
				piemenu.navItems[0].selected = false;
				
			//Assigning to the click the link in the itemlist
			angular.forEach(scope.pieItems, function(value, key) {
				piemenu.navItems[value.id].navigateFunction = function () {
					$window.location.href = value.link;
				}
			});
			//refreshing the wheel to get the links after the init
	        piemenu.refreshWheel();
		}
	
	  return {
	  	restrict: 'E',
	  	scope: {
	  		navigatorStyle : "="
	  	},
	    templateUrl: _CURRENTCONTEXTURL + '/wheelNavigator/ngWheelNavigatorTemplate.html',
	  	link: link
	    }
	});
})();