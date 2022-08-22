import * as treeService  from './treeService'
import * as filterTreeFactoryService  from './filterTreeFactoryService'
import * as operatorUtilService  from './operatorUtilService'

import deepcopy from 'deepcopy'

const defaultOperator = filterTreeFactoryService.operator('AND')

export function createGroupChildExpression(filterTree, operands) {
    let childExpression;
    let currentNode;

    for (let i = 1; i < operands.length; i++) {

        const leftOperand = deepcopy(getLeftOperand(operands, i));
        const operator = deepcopy(getOperator(filterTree, operands[i]));
        const rightOperand = deepcopy(getRightOperand(filterTree, operands, i));

        const expression = filterTreeFactoryService.expression(leftOperand, operator, rightOperand);

        if (!childExpression) {
            childExpression = expression;
            currentNode = childExpression;
        } else {
            currentNode.childNodes = expression.childNodes
            currentNode.value = expression.value
            currentNode.type = expression.type

            if (!expression.details) delete currentNode.details
            else currentNode.details = expression.details
        }
        currentNode = currentNode.childNodes[1];


    }

    return childExpression;
}

export function createGroup(filterTree, operands) {
    const childExpression = createGroupChildExpression(filterTree, operands)
    return filterTreeFactoryService.group(deepcopy(childExpression));
}

export function getLastOperand(group) {
    let lastOperand;
    treeService.traverseDF(group, function (node) {
        if (!operatorUtilService.isOperator(node) && node !== group && getGroup(group, node) === group) lastOperand = node;
    })

    return lastOperand;
}


export function areInSameGroup(filterTree, operands) {
    const firstOperandGroup = getGroup(filterTree, operands[0])

    for (let i = 1; i < operands.length; i++) {
        if (firstOperandGroup !== getGroup(filterTree, operands[i])) {
            return false;
        }
    }

    return true;
}

export function getGroup(filterTree, operand) {
    let group;

    group = treeService.getParent(filterTree, operand)
    while (!isGroup(group)) {
        try {
            group = treeService.getParent(filterTree, group)
        } catch (err) {
            group = undefined;
            break;
        }

    }


    return group;
}

export function getGroupOperands(group) {
    const operands = [] as any[];
    if (!group) return;
    treeService.traverseDF(group, function (node) {
        if (node !== group && !operatorUtilService.isOperator(node) && getGroup(group, node) === group) {
            operands.push(node)
        }
    })
    return operands;
}

export function getChildExpression(group) {
    return group.childNodes[0];
}

export function hasSubGroup(group) {
    let hasGroup = false;
    for (let i = 0; i < group.childNodes.length; i++) {

        treeService.traverseDF(group.childNodes[i], function (node) {
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

export function getOperator(filterTree, operand) {
    const operator = operatorUtilService.getOperator(filterTree, operand);
    if (!operator) {
        return defaultOperator;
    }
    return operator;
}

export function getRightOperand(filterTree, operands, index) {
    let rightOperand;
    if (getNext(operands, index)) {
        rightOperand = operatorUtilService.getOperator(filterTree, getNext(operands, index))
        if (!rightOperand) {
            rightOperand = defaultOperator;
        }
        if (!operatorUtilService.isOperatorFromSimple(filterTree, rightOperand)) {
            rightOperand = filterTreeFactoryService.operator(rightOperand.value)
        }


    } else {

        rightOperand = operands[index]

    }

    return rightOperand;
}
