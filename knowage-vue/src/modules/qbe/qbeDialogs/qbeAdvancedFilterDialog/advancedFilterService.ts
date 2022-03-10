const treeService = require('./treeService')
const operandUtilService = require('./operandUtilService')
const operatorUtilService = require('./operatorUtilService')
const filterTreeFactoryService = require('./filterTreeFactoryService')
const groupUtilService = require('./groupUtilService')

const deepEqual = require('deep-equal')
const defaultOperator = filterTreeFactoryService.operator('AND')
const deepcopy = require('deepcopy');

export function swap(filterTree, operand1, operand2) {
    operandUtilService.swapOperands(filterTree, treeService.find(filterTree, operand1), treeService.find(filterTree, operand2));
    treeService.setFilterTree(filterTree)
}

export function move(filterTree, operand1, operand2) {
    const tempNode = treeService.find(filterTree, operand1)
    const operand2Copy = deepcopy(operand2)
    operandUtilService.insertAfter(filterTree, tempNode, getOperandOrDefaultOperator(filterTree, treeService.find(filterTree, tempNode)), treeService.find(filterTree, operand2Copy))

    if (treeService.contains(filterTree, tempNode)) {
        operandUtilService.remove(filterTree, tempNode);
    } else {
        const temp = treeService.find(filterTree, operand2Copy)
        treeService.traverseDF(filterTree, function (node) {
            if (deepEqual(operand1, node) && operandUtilService.getNextOperand(filterTree, temp) !== node) {
                operandUtilService.remove(filterTree, node);
            }
        });

        operand2.childNodes = temp.childNodes
        operand2.value = temp.value
        operand2.type = temp.type

        if (!temp.details) delete operand2.details
        else operand2.details = temp.details
    }

    treeService.setFilterTree(filterTree)

}

export function getOperandOrDefaultOperator(filterTree, operand1) {
    const operator = operatorUtilService.getOperator(filterTree, operand1);
    if (!operator) {
        return defaultOperator;
    }

    return operator;
}


export function group(filterTree, operands) {
    const group = createGroup(filterTree, operands);
    adjoinOperands(filterTree, operands);
    insertGroup(filterTree, group, operands);
    removeSelected(filterTree, operands, group);

    treeService.setFilterTree(filterTree)
}

export function removeSelected(filterTree, operands, group) {
    for (let i = 0; i < operands.length - 1; i++) {
        treeService.traverseDF(filterTree, function (node) {
            if (deepEqual(operands[i], node) && !treeService.contains(treeService.find(filterTree, group), node)) {
                operandUtilService.remove(filterTree, node);
            }
        });
    }
}

export function insertGroup(filterTree, group, operands) {
    const operandsCopy = deepcopy(operands)

    replaceElement(filterTree, group, operands[operands.length - 1])

    for (let i = 0; i < operandsCopy.length; i++) {
        operands[i] = treeService.find(filterTree, operandsCopy[i]);
    }
}

export function createGroup(filterTree, operands) {
    return groupUtilService.createGroup(filterTree, operands);
}


export function adjoinOperands(filterTree, operands) {
    const operandsCopy = deepcopy(operands)

    for (let i = 1; i < operands.length; i++) {
        move(filterTree, treeService.find(filterTree, operands[i]), treeService.find(filterTree, operands[i - 1]));

    }

    for (let i = 0; i < operandsCopy.length; i++) {
        operands[i] = treeService.find(filterTree, operandsCopy[i]);
    }



}



export function ungroup(filterTree, group) {
    const groupCopy = deepcopy(group)

    while (groupUtilService.getLastOperand(groupCopy)) {
        move(filterTree, groupUtilService.getLastOperand(groupCopy), groupCopy)

    }

    operandUtilService.remove(filterTree, treeService.find(filterTree, groupCopy))
    treeService.setFilterTree(filterTree)

}


export function replaceElement(filterTree, source, destination) {
    treeService.replace(filterTree, source, destination)
}

export function getGroupExpression(group) {
    return groupUtilService.getChildExpression(group);
}

export function getLastGroupOperand(group) {
    return groupUtilService.getLastOperand(group);
}

export function isSameGroup(filterTree, operands) {
    return groupUtilService.areInSameGroup(filterTree, operands)
}

export function getGroup(tree, operand) {
    return groupUtilService.getGroup(tree, operand);
}

export function getGroupOperands(group) {
    return groupUtilService.getGroupOperands(group)
}

export function getGroupSibling(filterTree, group) {
    return groupUtilService.getSibilng(filterTree, group);

}

export function getGroupSiblingExpressionOperator(filterTree, group) {
    return treeService.getParent(filterTree, getGroupSibling(filterTree, group))
}

export function getFirstLevelOperandsAdvancedFilterService(filterTree) {
    return operandUtilService.getFirstLevelOperands(filterTree);
}
