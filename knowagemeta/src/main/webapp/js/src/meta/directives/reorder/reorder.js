angular.module('metaManager').directive('reorder', function(sbiModule_config ) {
	  return {
		  scope:{
			  isFirst:"=",
			  isLast:'=',
			  upFunc:"&",
			  downFunc:"&"
		  },

		  	restrict:'E',
		    templateUrl:sbiModule_config.contextName + '/js/src/meta/directives/reorder/reorder.html',

		  };
		});