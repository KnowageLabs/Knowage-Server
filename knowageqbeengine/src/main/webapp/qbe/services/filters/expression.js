
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


