(function () {
    angular.module('ResourceModule', [])
    	   .factory('resourceService', ["sbiModule_restServices", function(sbiModule_restServices) {

    		   var crud = {};
    		   crud.rest = sbiModule_restServices;

    		   crud.get = function(endPath, req_Path, item, conf){
    			 	var promise = this.rest.get(endPath, req_Path, item, conf);
    			 	return promise;
    		   };

    		   crud.post = function(endPath, req_Path, item, conf){
	   			 	var promise = this.rest.post(endPath, req_Path, item, conf);
	   			 	return promise;
   		  	   };

   		  	   crud.put = function(endPath, req_Path, item, conf){
   		  		    var promise = this.rest.put(endPath, req_Path, item, conf);
	   			 	return promise;
	   		   };

		   		crud.delete = function(endPath, req_Path, item, conf){
		   			var promise = this.rest.delete(endPath, req_Path, item, conf);
	   			 	return promise;
	   		   };

	   		   return crud;
    	       	   }]);
})();