
var filters = angular.module('filters',['sbiModule']);

filters.service('filters_service',function(sbiModule_action,sbiModule_translate){
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
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.equals.to"),value:"EQUALS TO"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.equals.to"),value:"NOT EQUALS TO"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.greater.than"),value:"GREATER THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.greater.than"),value:"EQUALS OR GREATER THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.less.than"),value:"LESS THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.less.than"),value:"EQUALS OR LESS THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.starts.with"),value:"STARTS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.starts.with"),value:"NOT STARTS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.ends.with"),value:"ENDS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.ends.with"),value:"NOT ENDS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.contains"),value:"CONTAINS"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.contains"),value:"NOT CONTAINS"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.between"),value:"BETWEEN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.between"),value:"NOT BETWEEN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.in"),value:"IN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.in"),value:"NOT IN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.is.null"),value:"IS NULL"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.null"),value:"NOT NULL"}];

	this.getBooleanConnectors= ["AND","OR"];

});


