'use strict';

angular.module('ngWYSIWYG', ['ngSanitize']);

//debug sanitize
angular.module('ngWYSIWYG').config(['$provide',
	//http://odetocode.com/blogs/scott/archive/2014/09/10/a-journey-with-trusted-html-in-angularjs.aspx
	function($provide) {
		$provide.decorator("$sanitize",['$delegate', '$log', function($delegate, $log) {
			return function(text, target) {
				var result = $delegate(text, target);
				//$log.info("$sanitize input: " + text);
				//$log.info("$sanitize output: " + result);
				return result;
			};
		}]);
	}
]);

angular.module('ngWYSIWYG').constant('NGP_EVENTS', {
	ELEMENT_CLICKED: 'ngp-element-clicked',
	CLICK_AWAY: 'ngp-click-away'
});
