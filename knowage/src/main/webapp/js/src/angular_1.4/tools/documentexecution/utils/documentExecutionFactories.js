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
		var f = function(){};
		var fakeScope = {
				changeNavigationRole: f,
				closeDocument : f,
				isCloseDocumentButtonVisible: f
			};
		
		var ng = $window.parent.angular 
			|| $window.parent.parent.angular; // coming from cockpit DocumentWidget
		
		if(ng && $window.frameElement!=null) {
//			return ng.element($window.frameElement).scope().$parent.$parent;
			var scope = ng.element($window.frameElement).scope();
			if(scope && scope.$parent && scope.$parent.$parent) {
				return scope.$parent.$parent;
			} else {
				return fakeScope;
			}
		
		} else if(ng ){ // coming from cockpit DocumentWidget
//			var scope = ng.element($window.parent.parent.frameElement).scope().$parent;
			var scope = ng.element($window.parent.parent.frameElement).scope();
			
			if(scope && scope.$parent) {
				var scopeParent = scope.$parent;
				
				if (!scopeParent.changeNavigationRole) {
					scopeParent.changeNavigationRole = function(){};
				}
				return scopeParent;
			} else {
				return fakeScope
			}
			
		} else {
			return fakeScope
		}
	});
})();