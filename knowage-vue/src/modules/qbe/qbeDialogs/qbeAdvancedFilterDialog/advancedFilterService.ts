import { find, traverseDF, contains, replace, getParent } from './treeService'
import { swapOperands, insertAfter, remove, getNextOperand, getFirstLevelOperands, getSibilng } from './operandUtilService'
import { getOperator } from './operatorUtilService'
import { operator } from './filterTreeFactoryService'
import { createGroupGroupUtilService, getLastOperand, getChildExpression, areInSameGroup, getGroupGroupUtilService, getGroupOperandsGroupUtilsService, } from './groupUtilService'

const deepEqual = require('deep-equal')
const defaultOperator = operator('AND')

export function swap(filterTree, operand1, operand2) {
    console.log("advancedFilterService - swap() - filterTree ", filterTree, ', operand1 ', operand1, ', operand2 ', operand2)
    swapOperands(filterTree, find(filterTree, operand1), find(filterTree, operand2));
}

export function move(filterTree, operand1, operand2) {
    console.log("advancedFilterService - move() - filterTree ", filterTree, ', operand1 ', operand1, ', operand2 ', operand2)
    var operand2Copy = { ...operand2 }
    insertAfter(filterTree, operand1, getOperandOrDefaultOperator(filterTree, find(filterTree, operand1)), find(filterTree, operand2Copy))

    if (contains(filterTree, operand1)) {
        remove(filterTree, operand1);
    } else {
        var temp = find(filterTree, operand2Copy)
        traverseDF(filterTree, function (node) {
            if (deepEqual(operand1, node) && getNextOperand(filterTree, temp) !== node) {
                remove(filterTree, node);
            }
        });


        // angular.copy(temp, operand2)
        operand2 = { ...temp }
    }



}

export function getOperandOrDefaultOperator(filterTree, operand1) {
    console.log("advancedFilterService - getOperandOrDefaultOperator() - filterTree ", filterTree, ', operand1 ', operand1)
    var operator = getOperator(filterTree, operand1);
    if (!operator) {
        return defaultOperator;
    }

    return operator;
}


export function group(filterTree, operands) {
    console.log("advancedFilterService - group() - filterTree ", filterTree, ', operands ', operands)
    //			if(isSameGroup(filterTree,operands)){
    var group = createGroup(filterTree, operands);
    adjoinOperands(filterTree, operands);
    insertGroup(filterTree, group, operands);
    removeSelected(filterTree, operands, group);
}

export function removeSelected(filterTree, operands, group) {
    console.log("advancedFilterService - removeSelected() - filterTree ", filterTree, ', operands ', operands, ', group ', group)
    for (var i = 0; i < operands.length - 1; i++) {
        traverseDF(filterTree, function (node) {
            if (deepEqual(operands[i], node) && !contains(find(filterTree, group), node)) {
                remove(filterTree, node);
            }
        });
    }
}

export function insertGroup(filterTree, group, operands) {
    console.log("advancedFilterService - insertGroup() - filterTree ", filterTree, ', group ', group, ', operands ', operands)
    var operandsCopy = [...operands]

    replaceElement(filterTree, group, operands[operands.length - 1])

    for (var i = 0; i < operandsCopy.length; i++) {
        operands[i] = find(filterTree, operandsCopy[i]);
    }


}

export function createGroup(filterTree, operands) {
    console.log("advancedFilterService - createGroup() - filterTree ", filterTree, ', operands ', operands)
    return createGroupGroupUtilService(filterTree, operands);
}


export function adjoinOperands(filterTree, operands) {
    console.log("advancedFilterService - adjoinOperands() - filterTree ", filterTree, ', operands ', operands)
    var operandsCopy = [...operands]
    for (var i = 1; i < operands.length; i++) {
        move(filterTree, find(filterTree, operands[i]), find(filterTree, operands[i - 1]));

    }

    for (var i = 0; i < operandsCopy.length; i++) {
        operands[i] = find(filterTree, operandsCopy[i]);
    }



}



export function ungroup(filterTree, group) {
    console.log("advancedFilterService - ungroup() - filterTree ", filterTree, ', group ', group)
    var groupCopy = { ...group }

    while (getLastOperand(groupCopy)) {
        move(filterTree, getLastOperand(groupCopy), groupCopy)

    }

    remove(filterTree, find(filterTree, groupCopy))


}


export function replaceElement(filterTree, source, destination) {
    console.log("advancedFilterService - replaceElement() - filterTree ", filterTree, ', source ', source, ', destination ', destination)
    replace(filterTree, source, destination)
}

export function getGroupExpression(group) {
    console.log("advancedFilterService - getGroupExpression() - group ", group)
    return getChildExpression(group);
}

export function getLastGroupOperand(group) {
    console.log("advancedFilterService - getLastGroupOperand() - group ", group)
    return getLastOperand(group);
}

export function isSameGroup(filterTree, operands) {
    console.log("advancedFilterService - isSameGroup() - filterTree ", filterTree, ', operands ', operands)
    return areInSameGroup(filterTree, operands)
}

export function getGroup(tree, operand) {
    console.log("advancedFilterService - getGroup() - tree ", tree, ', operand ', operand)
    return getGroupGroupUtilService(tree, operand);
}

export function getGroupOperands(group) {
    console.log("advancedFilterService - getGroupOperands() - group ", group)
    return getGroupOperandsGroupUtilsService(group)
}

export function getGroupSibling(filterTree, group) {
    console.log("advancedFilterService - getGroupSibling() - filterTree ", filterTree, ', group ', group)
    return getSibilng(filterTree, group);

}

export function getGroupSiblingExpressionOperator(filterTree, group) {
    console.log("advancedFilterService - getGroupSiblingExpressionOperator() - filterTree ", filterTree, ', group ', group)
    return getParent(filterTree, getGroupSibling(filterTree, group))
}

export function getFirstLevelOperandsAdvancedFilterService(filterTree) {
    console.log("advancedFilterService - getFirstLevelOperandsAdvancedFilterService() - filterTree ", filterTree)
    return getFirstLevelOperands(filterTree);
}
