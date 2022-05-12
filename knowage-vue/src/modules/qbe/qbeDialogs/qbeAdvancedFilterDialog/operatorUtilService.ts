const treeService = require('./treeService')
const filterTreeFactoryService = require('./filterTreeFactoryService')
const deepEqual = require('deep-equal')

export function getOperator(filterTree, operand) {
    if (!filterTree) throw new Error('filterTree cannot be undefined.');
    if (!operand) throw new Error('operand cannot be undefined.');
    let operator;

    treeService.traverseDF(filterTree, function (node) {
        if (isOperator(node) && isOperatorFrom(node, operand)) {
            operator = node;
        }
    })

    return operator;
}

export function swapOperators(filterTree, operand1, operand2) {
    let operator1 = getOperator(filterTree, operand1);
    let operator2 = getOperator(filterTree, operand2);

    if (!operator1) {
        if (operator2) {
            operator1 = filterTreeFactoryService.operator(operator2.value)
        } else {
            operator1 = filterTreeFactoryService.operator("AND");
        }
    }

    if (!operator2) {
        if (operator1) {
            operator2 = filterTreeFactoryService.operator(operator1.value)
        } else {
            operator2 = filterTreeFactoryService.operator("AND");
        }

    }

    treeService.swapNodePropertyValues(operator1, operator2, ["type", "value"])
}

export function isOperator(node) {
    return isANDOperator(node) || isOROperator(node);
}

export function isOperatorFrom(operator, operand) {
    return isOperatorFromSimple(operator, operand) || isOperatorFromComplex(operator, operand);
}

export function isOperatorFromComplex(operator, operand) {
    return !isSimpeExpressionOperator(operator) && deepEqual(getLeftOperand(getRightOperand(operator)), operand);
}

export function isOperatorFromSimple(operator, operand) {
    return isSimpeExpressionOperator(operator) && isRightOperand(operator, operand);
}

export function isRightOperand(operator, operand) {
    return deepEqual(getRightOperand(operator), operand);
}


export function isSimpeExpressionOperator(operator) {
    return !isOperator(getLeftOperand(operator)) && !isOperator(getRightOperand(operator));
}

export function getLeftOperand(operator) {
    if (isOperator(operator)) {
        return operator.childNodes[0];
    }
}


export function getRightOperand(operator) {
    if (isOperator(operator)) {
        return operator.childNodes[1];
    }
}

export function hasChildren(node) {
    return node.childNodes && node.childNodes.length > 0;
}


export function isANDOperator(node) {
    return node && node.value && node.value == 'AND';
}

export function isOROperator(node) {
    return node && node.value && node.value == 'OR';
}

export function isConst(node) {
    return node && node.type && node.type == 'NODE_CONST';
}

export function isPar(node) {
    return node && node.value == 'PAR';
}
