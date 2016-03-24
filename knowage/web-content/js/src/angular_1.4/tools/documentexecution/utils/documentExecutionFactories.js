(function() {
	var documentExecutionApp = angular.module('documentExecutionModule');
	
	documentExecutionApp.factory('documentExecuteFactories', function($mdToast) {
		var obj = {
			EmptyViewpoint : {
				NAME : "",
				DESCRIPTION: "",
				SCOPE : "",
				OBJECT_LABEL : "",
				ROLE :"",
				VIEWPOINT : JSON.parse("{}")
			},
			
			
			
		};
		return obj;
	});

	documentExecutionApp.factory('$documentNavigationScope', function($window) {
		  return $window.parent.angular.element($window.frameElement).scope().$parent;
	});
})();