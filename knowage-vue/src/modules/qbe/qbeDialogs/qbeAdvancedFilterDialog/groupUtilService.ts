const filterTreeFactoryService = require('./filterTreeFactoryService')
const treeService = require('./treeService')
const operatorUtilService = require('./operatorUtilService')
const deepEqual = require('deep-equal')
const deepcopy = require('deepcopy');

const defaultOperator = filterTreeFactoryService.operator('AND')

export function createGroupChildExpression(filterTree, operands) {
    console.log("groupUtilService - createGroupChildExpression() - filterTree ", filterTree, ', operands ', operands)
    var childExpression;
    var currentNode;

    for (var i = 1; i < operands.length; i++) {

        var leftOperand = deepcopy(getLeftOperand(operands, i));
        var operator = deepcopy(getOperator(filterTree, operands[i]));
        var rightOperand = deepcopy(getRightOperand(filterTree, operands, i));

        var expression = filterTreeFactoryService.expression(leftOperand, operator, rightOperand);

        if (!childExpression) {
            childExpression = expression;
            currentNode = childExpression;
        } else {
            // angular.copy(expression,currentNode);
            currentNode = deepcopy(expression)
        }
        currentNode = currentNode.childNodes[1];


    }

    return childExpression;
}

export function createGroup(filterTree, operands) {
    console.log("groupUtilService - createGroupGroupUtilService() - filterTree ", filterTree, ', operands ', operands)
    const childExpression = createGroupChildExpression(filterTree, operands)
    return filterTreeFactoryService.group(deepcopy(childExpression));
}

export function getLastOperand(group) {
    console.log("groupUtilService - getLastOperand() - group ", group)
    let lastOperand;
    treeService.traverseDF(group, function (node) {
        if (!operatorUtilService.isOperator(node) && node !== group && deepEqual(getGroup(group, node), group)) lastOperand = node;
    })

    return lastOperand;
}


export function areInSameGroup(filterTree, operands) {
    console.log("groupUtilService - areInSameGroup() - filterTree ", filterTree, ', operands ', operands)
    var firstOperandGroup = getGroup(filterTree, operands[0])

    for (var i = 1; i < operands.length; i++) {
        if (firstOperandGroup !== getGroup(filterTree, operands[i])) {
            return false;
        }
    }

    return true;
}

export function getGroup(filterTree, operand) {
    console.log("groupUtilService - getGroupGroupUtilService() - filterTree ", filterTree, ', operand ', operand)
    var group;

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
    console.log("groupUtilService - getGroupOperandsGroupUtilsService() - group ", group)
    var operands = [] as any[];
    if (!group) return;
    treeService.traverseDF(group, function (node) {
        if (node !== group && !operatorUtilService.isOperator(node) && getGroup(group, node) === group) {
            operands.push(node)
        }
    })
    return operands;
}

export function getChildExpression(group) {
    console.log("groupUtilService - getChildExpression() - group ", group)
    return group.childNodes[0];
}

export function hasSubGroup(group) {
    console.log("groupUtilService - hasSubGroup() - group ", group)
    var hasGroup = false;
    for (var i = 0; i < group.childNodes.length; i++) {

        treeService.traverseDF(group.childNodes[i], function (node) {
            if (isGroup(node)) hasGroup = true;

        })
    }

    return hasGroup;
}

export function isGroup(node) {
    console.log("groupUtilService - isGroup() - node ", node)
    return node && node.value === 'PAR';
}

export function getLeftOperand(operands, index) {
    console.log("groupUtilService - getLeftOperand() - operands ", operands, ', index ', index)
    return getPrevious(operands, index);
}

export function getPrevious(operands, index) {
    console.log("groupUtilService - getPrevious() - operands ", operands, ', index ', index)
    return operands[index - 1];
}
export function getNext(operands, index) {
    console.log("groupUtilService - getNext() - operands ", operands, ', index ', index)
    return operands[index + 1];
}

export function getOperator(filterTree, operand) {
    console.log("groupUtilService - getOperatorGroupUtilService() - filterTree ", filterTree, ', operand ', operand)
    var operator = operatorUtilService.getOperator(filterTree, operand);
    if (!operator) {
        return defaultOperator;
    }
    return operator;
}

export function getRightOperand(filterTree, operands, index) {
    console.log("groupUtilService - getRightOperand() - filterTree ", filterTree, ', operands ', operands, ', index ', index)
    var rightOperand;
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
