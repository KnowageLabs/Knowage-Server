var queries = angular.module('queries',['sbiModule']);

queries.service('query_service',function(sbiModule_restServices,sbiModule_config, $q, $rootScope,sbiModule_messaging, $mdDialog){
	var _this = this;
	this.smartView = true;
	this.count = 0;

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

		var promise = sbiModule_restServices.promisePost('qbequery/executeQuery',q,bodySend);

		promise.then(function(response) {
			_this.count = 0;
			queryModel.length = 0;
			console.log("[POST]: SUCCESS!");

			for (var i = 0; i < query.fields.length; i++) {

				var currField = query.fields[i];

				var queryObject = {
						"id":currField.id,
						"key": "column_" + (i+1),
						"name":currField.field,
						"alias":currField.alias,
						"entity":currField.entity,
						"color":currField.color,
						"funct":currField.funct,
						"fieldType" : currField.fieldType,
						"dataType": currField.dataType,
						"format": currField.format,
						"visible":currField.visible,
						"distinct":query.distinct,
						"group":currField.group,
						"order":i+1,
						"ordering":currField.order,
						"temporal":currField.temporal,
						"type":currField.type,
						"iconCls":currField.iconCls ? currField.iconCls : currField.fieldType,
						"longDescription":currField.longDescription,
						"filters": [],
						"havings": [],
						"inUse": currField.inUse
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

			if(query.havings.length > 0) {
				for(var i = 0; i < queryModel.length; i++) {
					for(var j = 0; j < query.havings.length; j++) {
						if(queryModel[i].id == query.havings[j].leftOperandValue) {
							queryModel[i].havings.push(query.havings[j]);
						}
					}
				}
			}

			if(isCompleteResult){
				var columns = [];
				var data = [];
				angular.copy(response.data.rows,data);
				createColumnsForPreview(columns, response.data.metaData.fields,queryModel);
				$rootScope.$broadcast('queryExecuted', {"columns":columns, "data":data, "results":response.data.results});
			}

		}, function(response) {

			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});

		return promise;
	}

	var createColumnsForPreview=function(columns, fields,queryModel){
    	for(i=1;i<fields.length;i++){
    	 var column={};
    	 column.label=fields[i].header;
    	 column.name=fields[i].name;
    	 column.dataType = queryModel[i-1].dataType;
    	 column.format = queryModel[i-1].format;
    	 column.dateFormatJava = fields[i].dateFormatJava;
    	 columns.push(column);
    	}


    }

	var refreshData = function(field,data){
			clearFieldData(field)
			var rows = getRows(field.alias,data);
			angular.copy(rows,field.data);
		}

		var clearFieldData = function(field){
			if(field){
				field.data.length = 0;
			}
		}

		var getRows = function(alias,data){

			var rows = [];
			var metaData = getMetaData(data)
			var columnName = getFieldMetaDataProperty(alias,metaData,'name')
			var values = getValues(data);

			for(var i in values){
				var row = {};
				row.id = values[i].id
				row.value = values[i][columnName]
				row.dateFormatJava = getFieldMetaDataProperty(alias,metaData,'dateFormatJava')

				rows.push(row)
			}

			return rows;

		}



		var getFieldMetaDataProperty = function(alias,metaData,propertyName){
			if(!alias || !metaData || !propertyName) return;
			var fields = getMetaDataFields(metaData)
			for(var i in fields){
				if(fields[i].header == alias){
					return fields[i][propertyName];
				}
			}
		}

		var getMetaData = function(data){
			if(data){
				return data.metaData;
			}

		}

		var getMetaDataFields = function(metaData){
			if(metaData){
				return metaData.fields;
			}
		}

		var getValues = function(data){
			if(data){
				return data.rows;
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