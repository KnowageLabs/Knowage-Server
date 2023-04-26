import * as treeService  from './treeService'
import * as operatorUtilService  from './operatorUtilService'
import * as filterTreeFactoryService  from './filterTreeFactoryService'
import * as groupUtilService  from './groupUtilService'

import deepcopy from 'deepcopy'

export function getSibilng(filterTree, operand) {
    const operator = getExpressionOperator(filterTree, operand)

    if (operatorUtilService.getLeftOperand(operator) === operand) {
        return operatorUtilService.getRightOperand(operator)
    }

    return operatorUtilService.getLeftOperand(operator);
}

export function getNextOperand(filterTree, operand) {
    let nextOperand;
    const operator = treeService.getParent(filterTree, operand)

    treeService.traverseDF(filterTree,
        function (node) {
            if (operatorUtilService.getOperator(filterTree, node) === operator) {
                nextOperand = node;
            }
        })

    return nextOperand;
}

export function insertAfter(filterTree, operand, operator, beforeOperand) {
    const beforeOperandCopy = deepcopy(beforeOperand)
    treeService.replace(
        filterTree,
        createInsertExpression(filterTree, deepcopy(operand), operator, beforeOperand),
        getInsertPosition(filterTree, beforeOperand)
    )

    return treeService.find(filterTree, beforeOperandCopy)

}

export function createInsertExpression(filterTree, operand, operator, beforeOperand) {
    return filterTreeFactoryService.expression(
        deepcopy(beforeOperand),
        operator,
        getInsertExpressionRightOperator(filterTree, deepcopy(operand), beforeOperand)
    )
}

export function getInsertExpressionRightOperator(filterTree,
    operand, beforeOperand) {
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return subexpression(
            filterTree,
            deepcopy(operand),
            getNextOperand(filterTree, beforeOperand)
        )
    }

    return operand;
}

export function getInsertPosition(filterTree, beforeOperand) {
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return getExpressionOperator(filterTree, beforeOperand);
    }
    return beforeOperand;
}

export function subexpression(filterTree, operand, nextOperand) {
    const leftOperand = operand;
    const tempOperator = filterTreeFactoryService.operator(operatorUtilService.getOperator(filterTree, nextOperand).value);
    let rightOperand = nextOperand;
    if (!isInSimpleExpression(filterTree, rightOperand)) {
        rightOperand = getExpressionOperator(filterTree, nextOperand)
    }
    return filterTreeFactoryService.expression(leftOperand, tempOperator, rightOperand)
}

export function remove(filterTree, operand) {
    if (!getSibilng(filterTree, operand)) {
        treeService.removeNode(filterTree, operand)
        return;
    }

    if (!isInSimpleExpression(filterTree, operand)) {
        const nextOperand = getNextOperand(filterTree, operand);

        if (nextOperand && nextOperand.value != "PAR") {
            operatorUtilService.swapOperators(filterTree, nextOperand, operand);
        }
    }

    treeService.replace(filterTree,
        getSibilng(filterTree, operand), getExpressionOperator(filterTree, operand))
}



export function getExpressionOperator(filterTree, operand) {
    return treeService.getParent(filterTree, operand)
}

export function swapOperands(filterTree, operand1, operand2) {
    treeService.swapNodes(operand1, operand2);
    operatorUtilService.swapOperators(filterTree, operand1, operand2);
}

export function isInSimpleExpression(filterTree, operand) {
    return operatorUtilService.isOperatorFromSimple(
        operatorUtilService.getOperator(filterTree, operand),
        operand)
}

export function getFirstLevelOperands(filterTree) {
    const operands = [] as any[];
    treeService.traverseDF(filterTree, function (node) {
        if (!groupUtilService.getGroup(filterTree, node) && !operatorUtilService.isOperator(node)) {
            operands.push(node)
        }
    })
    return operands;
}