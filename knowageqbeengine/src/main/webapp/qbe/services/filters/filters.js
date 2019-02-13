
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

	this.getBooleanConnectors= ["AND","OR"];

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

	this.generateExpressions = function (filters, expression, advancedFilters){

		advancedFilters.length = 0;

		for (var i = 0; i < filters.length; i++) {
			var advancedFilter = {
					type:"item",
					id: filters[i].filterId.substring(6),
					columns:[[]],
					name: filters[i].filterId,
					connector: filters[i].booleanConnector,
					color: filters[i].color,
					entity: filters[i].entity,
					leftValue: filters[i].leftOperandAlias,
					operator: filters[i].operator,
					rightValue: filters[i].rightOperandDescription
			};
			advancedFilters.push(advancedFilter);
		}

	 // if filters are empty set expression to empty object
		if(advancedFilters.length==0){
			angular.copy({},expression);
		} else {
			var nodeConstArray = [];
			for (var i = 0; i < advancedFilters.length; i++) {
				var nodeConstObj = {};
				nodeConstObj.value = '$F{' + advancedFilters[i].name + '}';
				nodeConstObj.type = "NODE_CONST";
				nodeConstObj.childNodes = [];
				nodeConstArray.push(nodeConstObj);
			}
			if (advancedFilters.length==1){
				angular.copy(nodeConstArray[0],expression);
			} else if (advancedFilters.length>1) {
				var nop = {};
				nop.value = "";
				nop.type = "NODE_OP";
				nop.childNodes = [];
				var nopForInsert = {};
				for (var i = advancedFilters.length-1; i >= 0 ; i--) {
					if (i-1==-1 || advancedFilters[i].connector!=advancedFilters[i-1].connector) {
						nop.value = advancedFilters[i].connector;
						nop.childNodes.push(nodeConstArray[i]);
						if(nopForInsert.value){
							nop.childNodes.push(nopForInsert);
						}
						nopForInsert = angular.copy(nop);
						nop.value = "";
						nop.type = "NODE_OP";
						nop.childNodes.length = 0;
					} else {
						nop.childNodes.push(nodeConstArray[i]);
					}
				}
				angular.copy(nopForInsert,expression);
			}
		}

	}

});


