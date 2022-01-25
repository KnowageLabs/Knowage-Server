import { find, traverseDF, contains, replace, getParent } from './treeService'
import { swapOperands, insertAfter, remove, getNextOperand, getFirstLevelOperands, getSibilng } from './operandUtilService'
import { getOperator } from './operatorUtilService'
import { operator } from './filterTreeFactoryService'
import { createGroupGroupUtilService, getLastOperand, getChildExpression, areInSameGroup, getGroupGroupUtilService, getGroupOperandsGroupUtilsService, } from './groupUtilService'

const assert = require('assert')
const defaultOperator = operator('AND')

export function swap(filterTree, operand1, operand2) {

    swapOperands(filterTree, find(filterTree, operand1), find(filterTree, operand2));
}

export function move(filterTree, operand1, operand2) {
    var operand2Copy = { ...operand2 }
    insertAfter(filterTree, operand1, getOperandOrDefaultOperator(filterTree, find(filterTree, operand1)), find(filterTree, operand2Copy))

    if (contains(filterTree, operand1)) {
        remove(filterTree, operand1);
    } else {
        var temp = find(filterTree, operand2Copy)
        traverseDF(filterTree, function (node) {
            if (assert.deepEqual(operand1, node) && getNextOperand(filterTree, temp) !== node) {
                remove(filterTree, node);
            }
        });


        // angular.copy(temp, operand2)
        operand2 = { ...temp }
    }



}

export function getOperandOrDefaultOperator(filterTree, operand1) {
    var operator = getOperator(filterTree, operand1);
    if (!operator) {
        return defaultOperator;
    }

    return operator;
}


export function group(filterTree, operands) {
    //			if(isSameGroup(filterTree,operands)){
    var group = createGroup(filterTree, operands);
    adjoinOperands(filterTree, operands);
    insertGroup(filterTree, group, operands);
    removeSelected(filterTree, operands, group);
}

export function removeSelected(filterTree, operands, group) {

    for (var i = 0; i < operands.length - 1; i++) {
        traverseDF(filterTree, function (node) {
            if (assert.deepEqual(operands[i], node) && !contains(find(filterTree, group), node)) {
                remove(filterTree, node);
            }
        });
    }
}

export function insertGroup(filterTree, group, operands) {
    var operandsCopy = [...operands]

    replaceElement(filterTree, group, operands[operands.length - 1])

    for (var i = 0; i < operandsCopy.length; i++) {
        operands[i] = find(filterTree, operandsCopy[i]);
    }


}

export function createGroup(filterTree, operands) {
    return createGroupGroupUtilService(filterTree, operands);
}


export function adjoinOperands(filterTree, operands) {
    var operandsCopy = [...operands]
    for (var i = 1; i < operands.length; i++) {
        move(filterTree, find(filterTree, operands[i]), find(filterTree, operands[i - 1]));

    }

    for (var i = 0; i < operandsCopy.length; i++) {
        operands[i] = find(filterTree, operandsCopy[i]);
    }



}



export function ungroup(filterTree, group) {

    var groupCopy = { ...group }

    while (getLastOperand(groupCopy)) {
        move(filterTree, getLastOperand(groupCopy), groupCopy)

    }

    remove(filterTree, find(filterTree, groupCopy))


}


export function replaceElement(filterTree, source, destination) {

    replace(filterTree, source, destination)
}

export function getGroupExpression(group) {
    return getChildExpression(group);
}

export function getLastGroupOperand(group) {
    return getLastOperand(group);
}

export function isSameGroup(filterTree, operands) {
    return areInSameGroup(filterTree, operands)
}

export function getGroup(tree, operand) {
    return getGroupGroupUtilService(tree, operand);
}

export function getGroupOperands(group) {
    return getGroupOperandsGroupUtilsService(group)
}

export function getGroupSibling(filterTree, group) {

    return getSibilng(filterTree, group);

}

export function getGroupSiblingExpressionOperator(filterTree, group) {

    return getParent(filterTree, getGroupSibling(filterTree, group))
}

export function getFirstLevelOperandsAdvancedFilterService(filterTree) {
    return getFirstLevelOperands(filterTree);
}
