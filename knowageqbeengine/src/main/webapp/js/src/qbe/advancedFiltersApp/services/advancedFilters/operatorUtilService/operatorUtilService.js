/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function(){
	angular.module('advancedFiltersApp').service('operatorUtilService',function($injector){
		
		var filterTreeFactoryService = $injector.get('filterTreeFactoryService');
		var treeService = $injector.get('treeService');
		
		
		
		var getOperator = function(filterTree,operand){
			if(!filterTree) throw new Error('filterTree cannot be undefined.'); 
			if(!operand) throw new Error('operand cannot be undefined.'); 
			var operator;
			
			treeService.traverseDF(filterTree,function(node){
				if(isOperator(node) && isOperatorFrom(node,operand)){
					operator = node;
				}
			})
			
			return operator;
		}
		
		var swapOperators = function(filterTree,operand1,operand2){
			
			var operator1 = getOperator(filterTree,operand1);
			var operator2 = getOperator(filterTree,operand2);
			
			if(!operator1) {
				if(operator2){
					operator1 = filterTreeFactoryService.operator(operator2.value)
				}else{
					operator1 = filterTreeFactoryService.operator("AND");
				}
				
				
			}
			if(!operator2) {
				if(operator1){
					operator2 = filterTreeFactoryService.operator(operator1.value)
				}else{
					operator2 = filterTreeFactoryService.operator("AND");
				}
				
			}
			
			treeService.swapNodePropertyValues(operator1,operator2,["type","value"])
		}
		var isOperator = function(node){
			return isANDOperator(node) || isOROperator(node);
		}
		
		var isOperatorFrom = function(operator,operand){
			return isOperatorFromSimple(operator,operand) || isOperatorFromComplex(operator,operand);
		}
		
		var isOperatorFromComplex = function(operator,operand){
			return !isSimpeExpressionOperator(operator) && getLeftOperand(getRightOperand(operator)) === operand;
		}
		
		var isOperatorFromSimple = function(operator,operand){
			return isSimpeExpressionOperator(operator) && isRightOperand(operator,operand);
		}
		
		var isRightOperand = function(operator,operand){
			return getRightOperand(operator) === operand;
		}
		
		
		var isSimpeExpressionOperator = function(operator){
			return !isOperator(getLeftOperand(operator)) &&  !isOperator(getRightOperand(operator));
		}
		
		var getLeftOperand = function(operator){
			if(isOperator(operator)){
				return operator.childNodes[0];
			}
		}
		
		
		var getRightOperand = function(operator){
			if(isOperator(operator)){
				return operator.childNodes[1];
			}
		}
		
		var hasChildren = function(node){
			return node.childNodes && node.childNodes.length > 0;
		}
		
		
		var isANDOperator = function(node){
			return node && node.value && node.value == 'AND';
		}
		
		var isOROperator = function(node){
			return node && node.value && node.value == 'OR';
		}
		
		var isConst = function(node){
			return node && node.type && node.type == 'NODE_CONST';
		}
		
		var isPar = function(node){
			return node && node.value == 'PAR';
		}
		
		return{
			isOperator : isOperator,
			isOperatorFrom : isOperatorFrom,
			isOperatorFromSimple : isOperatorFromSimple,
			getLeftOperand : getLeftOperand,
			getRightOperand : getRightOperand,
			getOperator : getOperator,
			swapOperators : swapOperators,
			defaultOperator : filterTreeFactoryService.operator("AND")
		}
		
		
	})
})()
