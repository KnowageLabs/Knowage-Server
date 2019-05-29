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
	angular.module('advancedFiltersApp').service('advancedFiltersService',function($injector,$parse){

		var treeService = $injector.get('treeService');
		var operatorUtilService = $injector.get('operatorUtilService');
		var filterTreeFactoryService = $injector.get('filterTreeFactoryService');
		var groupUtilService = $injector.get('groupUtilService');
		var operandUtilService = $injector.get('operandUtilService');

		var swap = function(filterTree,operand1,operand2){

			operandUtilService.swapOperands(filterTree,treeService.find(filterTree,operand1),treeService.find(filterTree,operand2));
		}

		var move = function(filterTree,operand1,operand2){
			var operand2Copy = angular.copy(operand2)
			operandUtilService.insertAfter(filterTree,operand1,getOperandOrDefaultOperator(filterTree,treeService.find(filterTree,operand1)),treeService.find(filterTree,operand2Copy))

			if(treeService.contains(filterTree,operand1)){
				operandUtilService.remove(filterTree,operand1);
			}else{
				var temp =  treeService.find(filterTree,operand2Copy)
				treeService.traverseDF(filterTree,function(node){
					if(angular.equals(operand1,node)&&operandUtilService.getNextOperand(filterTree,temp)!==node){
						operandUtilService.remove(filterTree,node);
					}
				});


				angular.copy(temp,operand2)
			}



		}

		var getOperandOrDefaultOperator = function(filterTree,operand1){
			var operator = operatorUtilService.getOperator(filterTree,operand1);
			if(!operator){
				return operatorUtilService.defaultOperator;
			}

			return operator;
		}


		var group = function(filterTree,operands){
//			if(isSameGroup(filterTree,operands)){
				var group = createGroup(filterTree,operands);
				adjoinOperands(filterTree,operands);
				insertGroup(filterTree,group,operands);
				removeSelected(filterTree,operands,group);





//			}
		}

		var removeSelected = function(filterTree,operands,group){




			for(var i = 0;i<operands.length-1;i++){
				treeService.traverseDF(filterTree,function(node){
					if(angular.equals(operands[i],node)&&!treeService.contains(treeService.find(filterTree,group),node)){
						operandUtilService.remove(filterTree,node);
					}
				});
			}
		}

		var insertGroup = function(filterTree,group,operands){
			var operandsCopy = angular.copy(operands)

			replaceElement(filterTree,group,operands[operands.length-1])

			for(var i = 0;i<operandsCopy.length;i++){
				operands[i] = treeService.find(filterTree,operandsCopy[i]);
			}


		}

		var createGroup = function(filterTree,operands){
			return groupUtilService.createGroup(filterTree,operands);
		}

		var adjoinOperands = function(filterTree,operands){
			var operandsCopy = angular.copy(operands)
			for(var i = 1;i<operands.length;i++){
				move(filterTree,treeService.find(filterTree,operands[i]),treeService.find(filterTree,operands[i-1]));

			}

			for(var i = 0;i<operandsCopy.length;i++){
				operands[i] = treeService.find(filterTree,operandsCopy[i]);
			}



		}


		var ungroup = function(filterTree,group){

			var groupCopy = angular.copy(group)

			while(groupUtilService.getLastOperand(groupCopy)){
				move(filterTree,groupUtilService.getLastOperand(groupCopy),groupCopy)

			}

			operandUtilService.remove(filterTree,treeService.find(filterTree,groupCopy))


		}

		var replaceElement = function(filterTree,source,destination){

			treeService.replace(filterTree,source,destination)
		}

		var getGroupExpression = function(group){
			return groupUtilService.getChildExpression(group);
		}

		var getLastGroupOperand = function(group){
			return groupUtilService.getLastOperand(group);
		}

		var isSameGroup = function(filterTree,operands){
			return groupUtilService.areInSameGroup(filterTree,operands)
		}

		var getGroup = function(tree,operand){
			return groupUtilService.getGroup(tree,operand);
		}

		var getGroupOperands = function(group){
			return groupUtilService.getGroupOperands(group)
		}

		var getGroupSibling = function(filterTree,group){

			return operandUtilService.getSibilng(filterTree,group);

		}

		var getGroupSiblingExpressionOperator = function(filterTree,group){

			return treeService.getParent(filterTree,getGroupSibling(filterTree,group))
		}

		var getFirstLevelOperands = function(filterTree){
			return operandUtilService.getFirstLevelOperands(filterTree);
		}





		return{
			move : move,
			swap : swap,
			group : group,
			ungroup : ungroup,
			adjoinOperands : adjoinOperands,
			insertGroup : insertGroup,
			isSameGroup:isSameGroup,
			getGroup:getGroup,
			getGroupOperands:getGroupOperands,
			getFirstLevelOperands:getFirstLevelOperands
		}

	})
})()

