
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
		                			  {name:"none",value:"NONE"},
		                			  {name:"equals to",value:"EQUALS TO"},
		                			  {name:"not equals to",value:"NOT EQUALS TO"},
		                			  {name:"greater then",value:"GREATER THAN"},
		                			  {name:"equals or greater then",value:"EQUALS OR GREATER THAN"},
		                			  {name:"LESS THAN".toLowerCase(),value:"LESS THAN"},
		                			  {name:"EQUALS OR LESS THAN",value:"EQUALS OR LESS THAN"},
		                			  {name:"STARTS WITH",value:"STARTS WITH"},
		                			  {name:"NOT STARTS WITH",value:"NOT STARTS WITH"},
		                			  {name:"ENDS WITH",value:"ENDS WITH"},
		                			  {name:"NOT ENDS WITH",value:"NOT ENDS WITH"},
		                			  {name:"CONTAINS",value:"CONTAINS"},
		                			  {name:"NOT CONTAINS",value:"NOT CONTAINS"},
		                			  {name:"BETWEEN",value:"BETWEEN"},
		                			  {name:"NOT BETWEEN",value:"NOT BETWEEN"},
		                			  {name:"IN",value:"IN"},
		                			  {name:"NOT IN",value:"NOT IN"},
		                			  {name:"IS NULL",value:"IS NULL"},
		                			  {name:"not null",value:"NOT NULL"},
		                	]

});


