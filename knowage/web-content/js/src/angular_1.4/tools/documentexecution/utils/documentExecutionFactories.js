(function() {
	var documentExecutionApp = angular.module('documentExecutionModule');
	
	documentExecutionApp.factory('documentExecuteFactories', function($mdToast,sbiModule_translate) {
		var obj = {
			EmptyViewpoint : {
				NAME : "",
				DESCRIPTION: "",
				SCOPE : "",
				OBJECT_LABEL : "",
				ROLE :"",
				VIEWPOINT : JSON.parse("{}")
			}
			
		};
		return obj;
	});

//	documentExecutionApp.factory('$crossNavigationScope', function($window) {
//		   return $window.parent.angular.element($window.frameElement).scope().$parent.$parent;
//	});
	documentExecutionApp.factory('$crossNavigationScope', function($window) {
		if($window.parent.angular && $window.frameElement!=null) {
			return $window.parent.angular.element($window.frameElement).scope().$parent.$parent;
		
		} else if($window.parent.parent.angular ){ // coming from cockpit DocumentWidget
			var scope = $window.parent.parent.angular.element($window.parent.parent.frameElement).scope().$parent
			if (!scope.changeNavigationRole) {
				scope.changeNavigationRole = function(){};
			}
			return scope;
			
		} else {
			var f = function(){};
			return {
				changeNavigationRole: f,
				isCloseDocumentButtonVisible: f
			};
		}
	});
})();