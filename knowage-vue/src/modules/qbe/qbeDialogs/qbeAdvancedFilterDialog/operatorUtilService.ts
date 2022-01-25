
import { traverseDF, swapNodePropertyValues } from './treeService'
import { operator } from './filterTreeFactoryService'

export function getOperator(filterTree, operand) {
    console.log('operatorUtilService - getOperator() - filterTree, ', filterTree, ', operand ', operand)
    if (!filterTree) throw new Error('filterTree cannot be undefined.');
    if (!operand) throw new Error('operand cannot be undefined.');
    var operator;

    traverseDF(filterTree, function (node) {
        if (isOperator(node) && isOperatorFrom(node, operand)) {
            operator = node;
        }
    })

    return operator;
}

export function swapOperators(filterTree, operand1, operand2) {
    console.log('operatorUtilService - swapOperators() - filterTree, ', filterTree, ', operand1 ', operand1, ', operand2 ', operand2)
    var operator1 = getOperator(filterTree, operand1);
    var operator2 = getOperator(filterTree, operand2);

    if (!operator1) {
        if (operator2) {
            operator1 = operator(operator2.value)
        } else {
            operator1 = operator("AND");
        }


    }
    if (!operator2) {
        if (operator1) {
            operator2 = operator(operator1.value)
        } else {
            operator2 = operator("AND");
        }

    }

    swapNodePropertyValues(operator1, operator2, ["type", "value"])
}

export function isOperator(node) {
    console.log('operatorUtilService - isOperator() - node, ', node)
    return isANDOperator(node) || isOROperator(node);
}

export function isOperatorFrom(operator, operand) {
    console.log('operatorUtilService - isOperatorFrom() - operator, ', operator, ', operand ', operand)
    return isOperatorFromSimple(operator, operand) || isOperatorFromComplex(operator, operand);
}

export function isOperatorFromComplex(operator, operand) {
    console.log('operatorUtilService - isOperatorFromComplex() - operator, ', operator, ', operand ', operand)
    return !isSimpeExpressionOperator(operator) && getLeftOperand(getRightOperand(operator)) === operand;
}

export function isOperatorFromSimple(operator, operand) {
    console.log('operatorUtilService - isOperatorFromSimple() - operator, ', operator, ', operand ', operand)
    return isSimpeExpressionOperator(operator) && isRightOperand(operator, operand);
}

export function isRightOperand(operator, operand) {
    console.log('operatorUtilService - isRightOperand() - operator, ', operator, ', operand ', operand)
    return getRightOperand(operator) === operand;
}


export function isSimpeExpressionOperator(operator) {
    console.log('operatorUtilService - isSimpeExpressionOperator() - operator, ', operator)
    return !isOperator(getLeftOperand(operator)) && !isOperator(getRightOperand(operator));
}

export function getLeftOperand(operator) {
    console.log('operatorUtilService - getLeftOperand() - operator, ', operator)
    if (isOperator(operator)) {
        return operator.childNodes[0];
    }
}


export function getRightOperand(operator) {
    console.log('operatorUtilService - getRightOperand() - operator, ', operator)
    if (isOperator(operator)) {
        return operator.childNodes[1];
    }
}

export function hasChildren(node) {
    console.log('operatorUtilService - hasChildren() - node, ', node)
    return node.childNodes && node.childNodes.length > 0;
}


export function isANDOperator(node) {
    console.log('operatorUtilService - isANDOperator() - node, ', node)
    return node && node.value && node.value == 'AND';
}

export function isOROperator(node) {
    console.log('operatorUtilService - isOROperator() - node, ', node)
    return node && node.value && node.value == 'OR';
}

export function isConst(node) {
    console.log('operatorUtilService - isConst() - node, ', node)
    return node && node.type && node.type == 'NODE_CONST';
}

export function isPar(node) {
    console.log('operatorUtilService - isPar() - node, ', node)
    return node && node.value == 'PAR';
}
