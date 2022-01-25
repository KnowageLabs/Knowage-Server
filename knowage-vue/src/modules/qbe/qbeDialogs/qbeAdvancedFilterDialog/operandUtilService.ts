import { getLeftOperand, getRightOperand, getOperator, swapOperators, isOperatorFromSimple, isOperator } from './operatorUtilService'
import { getParent, traverseDF, replace, find, removeNode, swapNodes } from './treeService'
import { createExpression } from './filterTreeFactoryService'
import { getGroupGroupUtilService } from './groupUtilService'

export function getSibilng(filterTree, operand) {

    var operator = getExpressionOperator(filterTree, operand)

    if (getLeftOperand(operator) === operand) {
        return getRightOperand(operator)
    }

    return getLeftOperand(operator);
}

export function getNextOperand(filterTree, operand) {

    var nextOperand;
    var operator = getParent(filterTree, operand)

    traverseDF(filterTree,
        function (node) {
            if (getOperator(filterTree,
                node) === operator) {
                nextOperand = node;
            }
        })

    return nextOperand;
}

export function insertAfter(filterTree, operand, operator,
    beforeOperand) {
    var beforeOperandCopy = { ...beforeOperand }
    replace(filterTree, createInsertExpression(
        filterTree, { ...operand }, operator, beforeOperand),
        getInsertPosition(filterTree, beforeOperand))

    return find(filterTree, beforeOperandCopy)

}

export function createInsertExpression(filterTree, operand,
    operator, beforeOperand) {
    return createExpression({ ...beforeOperand }, operator,
        getInsertExpressionRightOperator(filterTree, { ...operand },
            beforeOperand))
}

export function getInsertExpressionRightOperator(filterTree,
    operand, beforeOperand) {
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return subexpression(filterTree, { ...operand }, getNextOperand(
            filterTree, beforeOperand))
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
    var leftOperand = operand;
    var operator = operator(getOperator(filterTree, nextOperand).value);
    var rightOperand = nextOperand;
    if (!isInSimpleExpression(filterTree, rightOperand)) {
        rightOperand = getExpressionOperator(filterTree, nextOperand)
    }
    return createExpression(leftOperand, operator, rightOperand)
}

export function remove(filterTree, operand) {

    if (!getSibilng(filterTree, operand)) {
        removeNode(filterTree, operand)

        return;

    }

    if (!isInSimpleExpression(filterTree, operand)) {

        var nextOperand = getNextOperand(filterTree, operand);

        if (nextOperand && nextOperand.value != "PAR") {
            swapOperators(filterTree,
                nextOperand, operand);
        }
    }

    replace(filterTree,
        getSibilng(filterTree, operand), getExpressionOperator(
            filterTree, operand))


}



export function getExpressionOperator(filterTree, operand) {
    return getParent(filterTree, operand)
}

export function swapOperands(filterTree, operand1, operand2) {
    swapNodes(operand1, operand2);
    swapOperators(filterTree, operand1,
        operand2);

}

export function isInSimpleExpression(filterTree, operand) {
    return isOperatorFromSimple(
        getOperator(filterTree, operand),
        operand)
}

export function getFirstLevelOperands(filterTree) {
    var operands = [] as any[];
    traverseDF(filterTree, function (node) {
        if (!getGroupGroupUtilService(filterTree, node) && !isOperator(node)) {
            operands.push(node)
        }
    })
    return operands;
}