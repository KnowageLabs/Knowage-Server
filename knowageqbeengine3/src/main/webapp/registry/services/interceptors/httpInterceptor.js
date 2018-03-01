(function () {

	 angular.module('BlankApp')
    .factory('httpInterceptor', function($q,$injector) {

    	return{
    	 'responseError': function(rejection) {
    		 var sbiMsg = $injector.get('sbiModule_messaging');
    		 var msg = sbiMsg.showErrorMessage(rejection.data.errors[0].message,'Failure!!!' );
    	      	return $q.reject(msg);
    	     }
    	}
    	return $q.reject(msg);
       	   });

})();