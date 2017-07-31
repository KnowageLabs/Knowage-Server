
var queries = angular.module('queries',['sbiModule']);

queries.service('query_service',function(sbiModule_restServices,sbiModule_config, $q){
	
	this.executeQuery = function(query, bodySend, queryModel){
		
	
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
         		    	"color":query.fields[i].color,
         		    	"data":[],
         		    	"visible":query.fields[i].visible,
         		    	"group":query.fields[i].group,
         		    	"order":i+1,
         		    	"filters": []
         		    }
     			for (var j = 0; j < response.data.rows.length; j++) {
     				var row = {
     						"value":response.data.rows[j][key],
     						"id":response.data.rows[j].id
     				}
     				queryObject.data.push(row);
				}

     				queryModel.push(queryObject); 
			}
     		
     		

     	}, function(response) {
     		//deferred.reject(response);
     	});
		
	
	}
	
	var findWithAttr = function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}
	
});