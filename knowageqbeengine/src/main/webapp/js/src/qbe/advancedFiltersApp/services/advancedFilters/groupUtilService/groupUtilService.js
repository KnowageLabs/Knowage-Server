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
	angular.module('advancedFiltersApp').service('groupUtilService',function($injector){
		
		var treeService = $injector.get('treeService');
		var operatorUtilService = $injector.get('operatorUtilService');
		var filterTreeFactoryService = $injector.get('filterTreeFactoryService');
		
		var createGroupChildExpression = function(filterTree,operands){

			var childExpression ;
			var currentNode;

			for(var i = 1;i<operands.length; i++){
				
				var leftOperand 	= angular.copy(getLeftOperand(operands,i));
				var operator 		= angular.copy(getOperator(filterTree,operands[i]));
				var rightOperand 	= angular.copy(getRightOperand(filterTree,operands,i));
				
				var expression = filterTreeFactoryService.expression(leftOperand,operator,rightOperand);
				
				if(!childExpression){
					childExpression = expression;
					currentNode = childExpression;
				}else{
					angular.copy(expression,currentNode);
				}
				currentNode = currentNode.childNodes[1];
				
				
			}
			
			return childExpression;
		}
		
		var createGroup = function(filterTree,operands){
			var childExpression = createGroupChildExpression(filterTree,operands)
			return filterTreeFactoryService.group(angular.copy(childExpression));
		}
		
		var getLastOperand = function(group){
			var lastOperand;
			treeService.traverseDF(group,function(node){
				if(!operatorUtilService.isOperator(node)
						&&node!==group
						&&getGroup(group,node)===group
						) lastOperand = node;
			})
			
			return lastOperand;
		}
		
		
		var areInSameGroup = function(filterTree,operands){
			
			var firstOperandGroup = getGroup(filterTree,operands[0])
			
			for(var i = 1;i<operands.length;i++){
				if(firstOperandGroup !== getGroup(filterTree,operands[i])){
					return false;
				}
			}
			
			return true;
		}
		
		var getGroup = function(filterTree,operand){
			var group;
			
			group = treeService.getParent(filterTree,operand)
			while(!isGroup(group)){
				try{
				 group = treeService.getParent(filterTree,group)
			 	}catch(err){
			 		group = undefined;
			 		break;
			 	}
				
			}

			
			return group;
		}
		
		var getGroupOperands = function(group){
			var operands= [];
			if(!group)return;
			treeService.traverseDF(group,function(node){
				if(node!==group
						&&!operatorUtilService.isOperator(node)
						&&getGroup(group,node)===group){
					operands.push(node)
				}
			})
			return operands;
		}
		
		var getChildExpression = function(group){
			return group.childNodes[0];
		}
		var hasSubGroup = function(group){
			var hasGroup = false;
			for(var i=0;i<group.childNodes.length;i++){
				
				treeService.traverseDF(group.childNodes[i],function(node){
					if(isGroup(node)) hasGroup = true;
					
				})
			}
			
			return hasGroup;
		}
		
		var isGroup = function(node){
			return node && node.value === 'PAR';
		}
		
		var getLeftOperand = function(operands,index){
			return getPrevious(operands,index); 
		}
		
		var getPrevious = function(operands,index){
			return operands[index-1];
		}
		var getNext = function(operands,index){
			return operands[index+1];
		}
		
		var getOperator = function(filterTree,operand){
			var operator = operatorUtilService.getOperator(filterTree,operand);
			if(!operator){
				return operatorUtilService.defaultOperator;
			}
			return operator;
		}
		
		var getRightOperand = function(filterTree,operands,index){
			var rightOperand;
			if(getNext(operands,index)){
				rightOperand = operatorUtilService.getOperator(filterTree,getNext(operands,index))
				if(!rightOperand){
					rightOperand = operatorUtilService.defaultOperator;
				}
				if(!operatorUtilService.isOperatorFromSimple(filterTree,rightOperand)){
					rightOperand = filterTreeFactoryService.operator(rightOperand.value)
				}
				
				
			}else{
				
				rightOperand = operands[index]
				
			}
			
			return rightOperand;
		}

		return{
			createGroup : createGroup,
			createGroupChildExpression : createGroupChildExpression,
			getRightOperand : getRightOperand,
			getLastOperand : getLastOperand,
			getGroup : getGroup,
			getGroupOperands:getGroupOperands,
			areInSameGroup : areInSameGroup,
			getChildExpression : getChildExpression
		}
	})
})()
