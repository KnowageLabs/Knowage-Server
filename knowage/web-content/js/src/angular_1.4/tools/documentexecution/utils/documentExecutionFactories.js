(function() {
	angular.module('documentExecutionModule')
	.factory('documentExecuteFactories', function($mdToast) {
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
})();