import { createExpression, group, operator } from './filterTreeFactoryService'
import { traverseDF, getParent } from './treeService'
import { isOperator, getOperator, isOperatorFromSimple } from './operatorUtilService'

const defaultOperator = operator('AND')

export function createGroupChildExpression(filterTree, operands) {

    var childExpression;
    var currentNode;

    for (var i = 1; i < operands.length; i++) {

        var leftOperand = { ...getLeftOperand(operands, i) };
        var operator = { ...(getOperator(filterTree, operands[i])) };
        var rightOperand = { ...(getRightOperand(filterTree, operands, i)) };

        var expression = createExpression(leftOperand, operator, rightOperand);

        if (!childExpression) {
            childExpression = expression;
            currentNode = childExpression;
        } else {
            // angular.copy(expression,currentNode);
            currentNode = { ...expression }
        }
        currentNode = currentNode.childNodes[1];


    }

    return childExpression;
}

export function createGroupGroupUtilService(filterTree, operands) {
    var childExpression = createGroupChildExpression(filterTree, operands)
    return group({ ...childExpression });
}

export function getLastOperand(group) {
    var lastOperand;
    traverseDF(group, function (node) {
        if (!isOperator(node)
            && node !== group
            && getGroupGroupUtilService(group, node) === group
        ) lastOperand = node;
    })

    return lastOperand;
}


export function areInSameGroup(filterTree, operands) {

    var firstOperandGroup = getGroupGroupUtilService(filterTree, operands[0])

    for (var i = 1; i < operands.length; i++) {
        if (firstOperandGroup !== getGroupGroupUtilService(filterTree, operands[i])) {
            return false;
        }
    }

    return true;
}

export function getGroupGroupUtilService(filterTree, operand) {
    var group;

    group = getParent(filterTree, operand)
    while (!isGroup(group)) {
        try {
            group = getParent(filterTree, group)
        } catch (err) {
            group = undefined;
            break;
        }

    }


    return group;
}

export function getGroupOperandsGroupUtilsService(group) {
    var operands = [] as any[];
    if (!group) return;
    traverseDF(group, function (node) {
        if (node !== group
            && !isOperator(node)
            && getGroupGroupUtilService(group, node) === group) {
            operands.push(node)
        }
    })
    return operands;
}

export function getChildExpression(group) {
    return group.childNodes[0];
}

export function hasSubGroup(group) {
    var hasGroup = false;
    for (var i = 0; i < group.childNodes.length; i++) {

        traverseDF(group.childNodes[i], function (node) {
            if (isGroup(node)) hasGroup = true;

        })
    }

    return hasGroup;
}

export function isGroup(node) {
    return node && node.value === 'PAR';
}

export function getLeftOperand(operands, index) {
    return getPrevious(operands, index);
}

export function getPrevious(operands, index) {
    return operands[index - 1];
}
export function getNext(operands, index) {
    return operands[index + 1];
}

export function getOperatorGroupUtilService(filterTree, operand) {
    var operator = getOperator(filterTree, operand);
    if (!operator) {
        return defaultOperator;
    }
    return operator;
}

export function getRightOperand(filterTree, operands, index) {
    var rightOperand;
    if (getNext(operands, index)) {
        rightOperand = getOperator(filterTree, getNext(operands, index))
        if (!rightOperand) {
            rightOperand = defaultOperator;
        }
        if (!isOperatorFromSimple(filterTree, rightOperand)) {
            rightOperand = operator(rightOperand.value)
        }


    } else {

        rightOperand = operands[index]

    }

    return rightOperand;
}
