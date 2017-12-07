
var queries = angular.module('queries',['sbiModule']);

queries.service('query_service',function(sbiModule_restServices,sbiModule_config, $q, $rootScope,sbiModule_messaging, $mdDialog){

	this.smartView = true;

	this.setSmartView = function (value) {
		this.smartView = value;
	}

	this.executeQuery = function(query, bodySend, queryModel, isCompleteResult, start, itemsPerPage){

		if(start==undefined){
			start = 0;
		}
		if(itemsPerPage==undefined){
			itemsPerPage = 25;
		}

		if(itemsPerPage==0) return;

		var q="?SBI_EXECUTION_ID="+sbiModule_config.sbiExecutionID+"&currentQueryId="+query.id+"&start="+start+"&limit="+itemsPerPage;

		sbiModule_restServices.promisePost('qbequery/executeQuery',q,bodySend)
     	.then(function(response) {
     		queryModel.length = 0;
     		console.log("[POST]: SUCCESS!");

     		for (var i = 0; i < query.fields.length; i++) {
     			var key = "column_"+(i+1);
     			var queryObject = {
         		    	"id":query.fields[i].id,
         		    	"name":query.fields[i].field,
         		    	"entity":query.fields[i].entity,
         		    	"color":query.fields[i].color,
         		    	"data":[],
         		    	"funct":query.fields[i].funct,
         		    	"visible":query.fields[i].visible,
         		    	"distinct":query.distinct,
         		    	"group":query.fields[i].group,
         		    	"order":i+1,
         		    	"temporal":query.fields[i].temporal,
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

     		if(query.filters.length > 0) {
     			for(var i = 0; i < queryModel.length; i++) {
     				for(var j = 0; j < query.filters.length; j++) {
     					if(queryModel[i].id == query.filters[j].leftOperandValue) {
     						queryModel[i].filters.push(query.filters[j]);
     					}
     				}
     			}
     		}

     		if(isCompleteResult){
     			var columns = [];
     			var data = [];
     			angular.copy(response.data.rows,data);
     			createColumnsForPreview(columns, response.data.metaData.fields);
     			$rootScope.$broadcast('queryExecuted', {"columns":columns, "data":data, "results":response.data.results});
     		}

     	}, function(response) {

     		sbiModule_messaging.showErrorMessage("Server response: "+response.status, 'Error');
     	});


	}

	var createColumnsForPreview=function(columns, fields){
    	for(i=1;i<fields.length;i++){
    	 var column={};
    	 column.label=fields[i].header;
    	 column.name=fields[i].name;
    	 columns.push(column);
    	}


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