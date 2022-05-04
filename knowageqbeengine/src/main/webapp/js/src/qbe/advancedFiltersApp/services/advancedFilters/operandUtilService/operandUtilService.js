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
	angular.module('advancedFiltersApp').service(
			'operandUtilService',
			function($injector) {

				var treeService = $injector.get('treeService');
				var operatorUtilService = $injector.get('operatorUtilService');
				var groupUtilService = $injector.get('groupUtilService');
				var filterTreeFactoryService = $injector
						.get('filterTreeFactoryService');

				var getSibilng = function(filterTree, operand) {

					var operator = getExpressionOperator(filterTree, operand)

					if (operatorUtilService.getLeftOperand(operator) === operand) {
						return operatorUtilService.getRightOperand(operator)
					}

					return operatorUtilService.getLeftOperand(operator);
				}

				var getNextOperand = function(filterTree, operand) {

					var nextOperand;
					var operator = treeService.getParent(filterTree, operand)

					treeService.traverseDF(filterTree,
							function(node) {
								if (operatorUtilService.getOperator(filterTree,
										node) === operator) {
									nextOperand = node;
								}
							})

					return nextOperand;
				}

				var insertAfter = function(filterTree, operand, operator,
						beforeOperand) {
					var beforeOperandCopy = angular.copy(beforeOperand)
					treeService.replace(filterTree, createInsertExpression(
							filterTree, angular.copy(operand), operator, beforeOperand),
							getInsertPosition(filterTree, beforeOperand))

							return treeService.find(filterTree,beforeOperandCopy)

				}

				var createInsertExpression = function(filterTree, operand,
						operator, beforeOperand) {
					return filterTreeFactoryService.expression(angular
							.copy(beforeOperand), operator,
							getInsertExpressionRightOperator(filterTree, angular.copy(operand),
									beforeOperand))
				}

				var getInsertExpressionRightOperator = function(filterTree,
						operand, beforeOperand) {
					if (!isInSimpleExpression(filterTree, beforeOperand)) {
						return subexpression(filterTree, angular.copy(operand), getNextOperand(
								filterTree, beforeOperand))
					}

					return operand;
				}

				var getInsertPosition = function(filterTree, beforeOperand) {
					if (!isInSimpleExpression(filterTree, beforeOperand)) {
						return getExpressionOperator(filterTree, beforeOperand);
					}
					return beforeOperand;
				}

				var subexpression = function(filterTree, operand, nextOperand) {
					var leftOperand = operand;
					var operator = filterTreeFactoryService.operator(operatorUtilService.getOperator(filterTree,nextOperand).value);
					var rightOperand = nextOperand;
					if (!isInSimpleExpression(filterTree, rightOperand)) {
						rightOperand = getExpressionOperator(filterTree,nextOperand)
					}
					return filterTreeFactoryService.expression(leftOperand,operator, rightOperand)
				}

				var remove = function(filterTree, operand) {

					if(!getSibilng(filterTree, operand)){
						treeService.remove(filterTree,operand)

						return;

					}

					if (!isInSimpleExpression(filterTree, operand)) {

						var nextOperand = getNextOperand(filterTree, operand);
						
						if (nextOperand && nextOperand.value != "PAR") {
							operatorUtilService.swapOperators(filterTree,
									nextOperand, operand);
						}
					}

					treeService.replace(filterTree,
							getSibilng(filterTree, operand), getExpressionOperator(
									filterTree, operand))


				}



				var getExpressionOperator = function(filterTree, operand) {
					return treeService.getParent(filterTree, operand)
				}

				var swapOperands = function(filterTree, operand1, operand2) {
					treeService.swapNodes(operand1, operand2);
					operatorUtilService.swapOperators(filterTree, operand1,
							operand2);

				}

				var isInSimpleExpression = function(filterTree, operand) {
					return operatorUtilService.isOperatorFromSimple(
							operatorUtilService.getOperator(filterTree, operand),
							operand)
				}

				var getFirstLevelOperands = function(filterTree){
					var operands = [];
					treeService.traverseDF(filterTree,function(node){
						if(!groupUtilService.getGroup(filterTree,node)&&!operatorUtilService.isOperator(node)){
							operands.push(node)
						}
					})
					return operands;
				}

				return {
					getSibilng : getSibilng,
					insertAfter : insertAfter,
					swapOperands : swapOperands,
					getNextOperand : getNextOperand,
					remove : remove,
					getFirstLevelOperands:getFirstLevelOperands
				}

			})
})()
