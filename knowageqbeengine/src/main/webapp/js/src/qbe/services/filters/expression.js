
var filters = angular.module('expressions',[]);

filters.service('expression_service',function(){
	this.getExpressionAsObject= function(expression, filters, expressionArray){
		var str = expression;
		var filters = filters;
		var expressionObject = {
				type:"",
				value:"",
				childNodes:[]
		}
		if(filters.length==0){
			expressionObject = {};
		}
		else if(filters.length==1){
			expressionObject.type = "NODE_CONST";
    		expressionObject.value = expression;
		} else if (filters.length>1){
			expressionObject.type = "NODE_OP";
			expressionObject.value = filters[0].booleanConnector;
			var sameOperator = 0;
			for (var i = 0; i < filters.length-1; i++) {

				//exclude last filter from booleanOperator check
				if(filters[i+2]){
					if(filters[i].booleanConnector==filters[i+1].booleanConnector){
						sameOperator++;
					}
				} else {
					sameOperator++;
				}
			}
			if(sameOperator==filters.length-1){
				for (var i = 0; i < expressionArray.length; i++) {
					var childNode = {
							type:"NODE_CONST",
							value:expressionArray[i].name,
							childNodes:[]
					}
					expressionObject.childNodes.push(childNode);
				}
			} else {

				expressionObject.childNodes = buildExpressionTree(expressionArray);
			}
		}

		return expressionObject;
	};

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
				nodeConstObj.details = {}
				nodeConstObj.details.leftOperandAlias = advancedFilters[i].leftValue;
				nodeConstObj.details.operator = advancedFilters[i].operator;
				nodeConstObj.details.entity = advancedFilters[i].entity;
				nodeConstObj.details.rightOperandValue = advancedFilters[i].rightValue;
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

	};

	var buildExpressionTree = function(expressionArray){
		var childNodes = [];
		//first filter
		var childNode = {
				type:"NODE_CONST",
				value:expressionArray[0].name,
				childNodes:[]
		}

		childNodes.push(childNode);

		var currentOperator = expressionArray[0].operator;
		for (var i = 1; i < expressionArray.length; i++) {
			if(currentOperator==expressionArray[i].operator){
				var childNode = {
						type:"NODE_CONST",
						value:expressionArray[i].name,
						childNodes:[]
				}
				childNodes.push(childNode);
			} else {
				currentOperator=expressionArray[i].operator;
				var opChildNode = {
						type: "NODE_OP",
						value: currentOperator,
						childNodes: []
				}
				opChildNode.childNodes.push({type:"NODE_CONST", value:expressionArray[i].name, childNodes:[]});
				if(currentOperator==expressionArray[i+1].operator) {
					opChildNode.childNodes.push({type:"NODE_CONST", value:expressionArray[i+1].name, childNodes:[]});
				} else {
					currentOperator=expressionArray[i+1].operator;
					var opChildNode = {
							type: "NODE_OP",
							value: currentOperator,
							childNodes: []
					}

				}

				childNodes.push(opChildNode);
				break;
			}
		}
		return childNodes;
	}

	var buildChildNode = function (currentOperator, currentExpressionObject, nextExpressionObject) {

		if(currentOperator==currentExpressionObject.operator){

		} else {
			currentOperator = currentExpressionObject.operator;
		}
	}

});


