
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

	this.getTargetTypes = [
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.manual"),value:"manual"},
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.field"),value:"valueOfField"},
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.anotherEntity"),value:"anotherEntity"},

	];

	this.getHavingTargetTypes = [
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.manual"),value:""},
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.anotherField"),value:"anotherEntity"}
		 // {name:sbiModule_translate.load("kn.qbe.filters.target.types.subquery"),value:"subquery"},
	];

	this.aggFunctions = [ "NONE", "SUM", "MIN", "MAX", "AVG", "COUNT", "COUNT_DISTINCT" ];

	this.getSpecialOperators = [
		{name:sbiModule_translate.load("kn.qbe.filters.operators.equals.to"),value:"EQUALS TO"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.greater.than"),value:"GREATER THAN"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.greater.than"),value:"EQUALS OR GREATER THAN"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.less.than"),value:"LESS THAN"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.less.than"),value:"EQUALS OR LESS THAN"}
	];

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
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.null"),value:"NOT NULL"}
		         ];
	this.getSpatialOperators = [
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.contains"),value:"SPATIAL_CONTAINS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.covered.by"),value:"SPATIAL_COVERED_BY"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.covers"),value:"SPATIAL_COVERS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.disjoint"),value:"SPATIAL_DISJOINT"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.equals.to"),value:"SPATIAL_EQUALS_TO"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.filter"),value:"SPATIAL_FILTER"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.intersects"),value:"SPATIAL_INTERSECTS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.overlaps"),value:"SPATIAL_OVERLAPS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.touches"),value:"SPATIAL_TOUCHES"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.inside"),value:"SPATIAL_INSIDE"}];

	this.booleanConnectors= ["AND","OR"];

	this.deleteFilter = function(filters,filter,expression,advancedFilters){

		console.log(filter);
		this.deleteFilterByProperty('filterId',filter.filterId,filters,expression,advancedFilters)
	}

	this.deleteFilterByProperty = function(propertyName,propertyValue,filters,expression,advancedFilters){
		for (var i = 0; i < filters.length; i++) {
			if(filters[i][propertyName]!=undefined && filters[i][propertyName]==propertyValue) {

				filters.splice(i, 1);
				this.generateExpressions (filters, expression, advancedFilters);
				i--;
			}

		}
	}

	class Node{
		constructor(type){
			this.type = type;
		    this.childNodes = []
		}
	}

	class Operand extends Node{
		constructor(type,value){
			super(type)
		    this.value = value;
		}
	}

	this.generateExpressions = function (filters){
		var obj ={};

		for(var i = 0;i<filters.length ;i++){
			if(filters.length===1) return new Operand("NODE_CONST","$F{"+filters[i].filterId+"}");
		    obj = new Operand("NODE_OP",filters[i].booleanConnector);
		    var temp = obj;
		    for(var i = 0;i<filters.length ;i++){
		    	for(;i<filters.length-2 ;i++){
		    		temp.childNodes.push(new Operand("NODE_CONST","$F{"+filters[i].filterId+"}"))
		            temp.childNodes.push(new Operand("NODE_OP",filters[i].booleanConnector))
		            temp = temp.childNodes[1]
		    	}
		    	temp.childNodes.push(new Operand("NODE_CONST","$F{"+filters[i].filterId+"}"))
		    }
		}
		return obj;

	}

});


