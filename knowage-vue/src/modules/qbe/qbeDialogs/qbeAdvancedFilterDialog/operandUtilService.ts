const operatorUtilService = require('./operatorUtilService')
const treeService = require('./treeService')
const filterTreeFactoryService = require('./filterTreeFactoryService')
const groupUtilService = require('./groupUtilService')
const deepEqual = require('deep-equal')

export function getSibilng(filterTree, operand) {
    console.log("operandUtilService - getSibling() - filterTree ", filterTree, ', operand ', operand)

    const operator = getExpressionOperator(filterTree, operand)

    if (operatorUtilService.getLeftOperand(operator) === operand) {
        return operatorUtilService.getRightOperand(operator)
    }

    return operatorUtilService.getLeftOperand(operator);
}

export function getNextOperand(filterTree, operand) {
    console.log("operandUtilService - getNextOperand() - filterTree ", filterTree, ', operand ', operand)

    let nextOperand;
    const operator = treeService.getParent(filterTree, operand)

    treeService.traverseDF(filterTree,
        function (node) {
            if (deepEqual(operatorUtilService.getOperator(filterTree, node), operator)) {
                nextOperand = node;
            }
        })

    return nextOperand;
}

export function insertAfter(filterTree, operand, operator, beforeOperand) {
    console.log("operandUtilService - insertAfter() - filterTree ", filterTree, ', operand ', operand, ', operator ', operator, ', beforeOperand ', beforeOperand)
    var beforeOperandCopy = JSON.parse(JSON.stringify(beforeOperand))
    treeService.replace(
        filterTree,
        createInsertExpression(filterTree, JSON.parse(JSON.stringify(operand)), operator, beforeOperand),
        getInsertPosition(filterTree, beforeOperand)
    )

    return treeService.find(filterTree, beforeOperandCopy)

}

export function createInsertExpression(filterTree, operand, operator, beforeOperand) {
    console.log("operandUtilService - createInsertExpression() - filterTree ", filterTree, ', operand ', operand, ', operator ', operator, ', beforeOperand ', beforeOperand)
    return filterTreeFactoryService.expression(
        JSON.stringify(beforeOperand),
        operator,
        getInsertExpressionRightOperator(filterTree, JSON.parse(JSON.stringify(operand)), beforeOperand)
    )
}

export function getInsertExpressionRightOperator(filterTree,
    operand, beforeOperand) {
    console.log("operandUtilService - getInsertExpressionRightOperator() - filterTree ", filterTree, ', operand ', operand, ', beforeOperand ', beforeOperand)
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return subexpression(
            filterTree,
            JSON.parse(JSON.stringify(operand)),
            getNextOperand(filterTree, beforeOperand)
        )
    }

    return operand;
}

export function getInsertPosition(filterTree, beforeOperand) {
    console.log("operandUtilService - getInsertPosition() - filterTree ", filterTree, ', beforeOperand ', beforeOperand)
    if (!isInSimpleExpression(filterTree, beforeOperand)) {
        return getExpressionOperator(filterTree, beforeOperand);
    }
    return beforeOperand;
}

export function subexpression(filterTree, operand, nextOperand) {
    console.log("operandUtilService - subexpression() - filterTree ", filterTree, ', operand ', operand, ', nextOperand ', nextOperand)
    const leftOperand = operand;
    const tempOperator = filterTreeFactoryService.operator(operatorUtilService.getOperator(filterTree, nextOperand).value);
    let rightOperand = nextOperand;
    if (!isInSimpleExpression(filterTree, rightOperand)) {
        rightOperand = getExpressionOperator(filterTree, nextOperand)
    }
    return filterTreeFactoryService.expression(leftOperand, tempOperator, rightOperand)
}

export function remove(filterTree, operand) {
    console.log("operandUtilService - remove() - filterTree ", filterTree, ', operand ', operand)
    if (!getSibilng(filterTree, operand)) {
        treeService.remove(filterTree, operand)
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
    console.log("operandUtilService - getExpressionOperator() - filterTree ", filterTree, ', operand ', operand)
    return treeService.getParent(filterTree, operand)
}

export function swapOperands(filterTree, operand1, operand2) {
    console.log("operandUtilService - swapOperands() - filterTree ", filterTree, ', operand1 ', operand1, ', operand2 ', operand2)
    treeService.swapNodes(operand1, operand2);
    operatorUtilService.swapOperators(filterTree, operand1, operand2);
}

export function isInSimpleExpression(filterTree, operand) {
    console.log("operandUtilService - swapOperands() - filterTree ", filterTree, ', operand ', operand)
    return operatorUtilService.isOperatorFromSimple(
        operatorUtilService.getOperator(filterTree, operand),
        operand)
}

export function getFirstLevelOperands(filterTree) {
    console.log("operandUtilService - getFirstLevelOperands() - filterTree ", filterTree)
    const operands = [] as any[];
    treeService.traverseDF(filterTree, function (node) {
        if (!groupUtilService.getGroup(filterTree, node) && !operatorUtilService.isOperator(node)) {
            operands.push(node)
        }
    })
    return operands;
}