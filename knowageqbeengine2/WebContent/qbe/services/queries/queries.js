
var queries = angular.module('queries',['sbiModule']);

queries.service('query_service',function(sbiModule_restServices,sbiModule_config, $q){
	
	var findWithAttr = function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}
	
	this.executeQuery = function(field, query, bodySend, queryModel){
		
		var deferred = $q.defer();
		
		var q="?SBI_EXECUTION_ID="+sbiModule_config.sbiExecutionID+"&currentQueryId="+query.id+"&start=0&limit=25";
		
		sbiModule_restServices.promisePost('qbequery/executeQuery',q,bodySend)
     	.then(function(response) {
     		console.log("[POST]: SUCCESS!");

     		for (var i = 0; i < query.fields.length; i++) {
     			var key = "column_"+(i+1);
     			var queryObject = {
         		    	"id":query.fields[i].id,
         		    	"name":query.fields[i].field,
         		    	"entity":query.fields[i].entity,
         		    	"color":field.color,
         		    	"data":[],
         		    	"hidden":false,
         		    	"order":i+1,
         		    	"filters": ["no filters"]
         		    }
     			for (var j = 0; j < response.data.rows.length; j++) {
     				queryObject.data.push(response.data.rows[j][key]);
				}
     			var index = findWithAttr(queryModel,'id', queryObject.id);
     			if(index!=-1){
     				queryModel[index].data = queryObject.data;
     			} else {
     				queryModel.push(queryObject); 
     			}
			}
     		
     		deferred.resolve(queryModel);

     	}, function(response) {
     		deferred.reject(response);
     	});
		
		return deferred.promise;
	}
})


/*$scope.executeQuery = function (data) {
    	q="?SBI_EXECUTION_ID="+sbiModule_config.sbiExecutionID+"&currentQueryId="+$scope.query.id+"&start=0&limit=25"

    	 sbiModule_restServices.promisePost('qbequery/executeQuery',q,$scope.bodySend)
     	.then(function(response) {
     		console.log("[POST]: SUCCESS!");

     		for (var i = 0; i < $scope.query.fields.length; i++) {
     			var key = "column_"+(i+1);
     			var queryObject = {
         		    	"id":$scope.query.fields[i].id,
         		    	"name":$scope.query.fields[i].field,
         		    	"entity":$scope.query.fields[i].entity,
         		    	"color":data.color,
         		    	"data":[],
         		    	"hidden":false,
         		    	"order":i+1,
         		    	"filters": ["no filters"]
         		    }
     			for (var j = 0; j < response.data.rows.length; j++) {
     				queryObject.data.push(response.data.rows[j][key]);
				}
     			var index = findWithAttr($scope.queryModel,'id', queryObject.id);
     			if(index!=-1){
     				$scope.queryModel.data = queryObject.data;
     			} else {
     				$scope.queryModel.push(queryObject); 
     			}
     			
			}

     	}, function(response) {
     	});
    }*/