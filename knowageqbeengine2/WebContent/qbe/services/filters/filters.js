
var filters = angular.module('filters',['sbiModule']);

filters.service('filters_service',function(sbiModule_action){
	this.getFieldsValue= function(entityID){
		var queryParam = {};
		var formParam = {};
		var headers = [];
		var configParams = {
    		headers: {
    			'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'
			},

		};
		queryParam.ENTITY_ID = entityID;
		formParam.start = 0;
		formParam.limit = 20;

		var response = sbiModule_action.promisePost('GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION',queryParam,"",configParams);


		return response;
	};
	this.getOperators = [
		         {name:"equals to",value:"EQUALS TO"},
		         {name:"not equals to",value:"NOT EQUALS TO"},
		         {name:"greater then",value:"GREATER THAN"},
		         {name:"equals or greater then",value:"EQUALS OR GREATER THAN"},
		         {name:"less than",value:"LESS THAN"},
		         {name:"equals or less than",value:"EQUALS OR LESS THAN"},
		         {name:"starts with",value:"STARTS WITH"},
		         {name:"not starts with",value:"NOT STARTS WITH"},
		         {name:"ends with",value:"ENDS WITH"},
		         {name:"not ends with",value:"NOT ENDS WITH"},
		         {name:"contains",value:"CONTAINS"},
		         {name:"not contains",value:"NOT CONTAINS"},
		         {name:"between",value:"BETWEEN"},
		         {name:"not between",value:"NOT BETWEEN"},
		         {name:"in",value:"IN"},
		         {name:"not in",value:"NOT IN"},
		         {name:"is null",value:"IS NULL"},
		         {name:"not null",value:"NOT NULL"}];

	this.getBooleanConnectors= [
	    		         {name:"AND",value:"AND"},
	    		         {name:"OR",value:"OR"}];

});


