const treeService = require('./treeService')
const operandUtilService = require('./operandUtilService')
const operatorUtilService = require('./operatorUtilService')
const filterTreeFactoryService = require('./filterTreeFactoryService')
const groupUtilService = require('./groupUtilService')

const deepEqual = require('deep-equal')
const defaultOperator = filterTreeFactoryService.operator('AND')
const deepcopy = require('deepcopy');

export function swap(filterTree, operand1, operand2) {
    console.log("advancedFilterService - swap() - filterTree ", filterTree, ', operand1 ', operand1, ', operand2 ', operand2)
    operandUtilService.swapOperands(filterTree, treeService.find(filterTree, operand1), treeService.find(filterTree, operand2));
    treeService.setFilterTree(filterTree)
}

export function move(filterTree, operand1, operand2) {
    console.log("advancedFilterService - move() - filterTree ", filterTree, ', operand1 ', operand1, ', operand2 ', operand2)
    var operand2Copy = deepcopy(operand2)
    operandUtilService.insertAfter(filterTree, operand1, getOperandOrDefaultOperator(filterTree, treeService.find(filterTree, operand1)), treeService.find(filterTree, operand2Copy))

    if (treeService.contains(filterTree, operand1)) {
        operandUtilService.remove(filterTree, operand1);
    } else {
        var temp = treeService.find(filterTree, operand2Copy)
        treeService.traverseDF(filterTree, function (node) {
            if (deepEqual(operand1, node) && operandUtilService.getNextOperand(filterTree, temp) !== node) {
                operandUtilService.remove(filterTree, node);
            }
        });


        // angular.copy(temp, operand2)
        // operand2 = deepcopy(temp)
        operand2.childNodes = temp.childNodes
        operand2.value = temp.value
        operand2.type = temp.type

        if (!temp.details) delete operand2.details
        else operand2.details = temp.details
    }

    treeService.setFilterTree(filterTree)

}

export function getOperandOrDefaultOperator(filterTree, operand1) {
    console.log("advancedFilterService - getOperandOrDefaultOperator() - filterTree ", filterTree, ', operand1 ', operand1)
    var operator = operatorUtilService.getOperator(filterTree, operand1);
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
    console.log("FOR BREAKPOIN", filterTree);
    treeService.setFilterTree(filterTree)
}

export function removeSelected(filterTree, operands, group) {
    console.log("advancedFilterService - removeSelected() - filterTree ", filterTree, ', operands ', operands, ', group ', group)
    for (var i = 0; i < operands.length - 1; i++) {
        treeService.traverseDF(filterTree, function (node) {
            console.log(" aaa - test 1 ", deepEqual(operands[i], node))
            console.log(" aaa - test 2 ", !treeService.contains(treeService.find(filterTree, group), node))
            if (deepEqual(operands[i], node) && !treeService.contains(treeService.find(filterTree, group), node)) {
                console.log(">>>>>>>>>>>>>>>>>>> ENTERED: tree - ", filterTree)
                operandUtilService.remove(filterTree, node);
            }
        });
    }
}

export function insertGroup(filterTree, group, operands) {
    console.log("advancedFilterService - insertGroup() - filterTree ", filterTree, ', group ', group, ', operands ', operands)
    var operandsCopy = deepcopy(operands)

    replaceElement(filterTree, group, operands[operands.length - 1])

    for (var i = 0; i < operandsCopy.length; i++) {
        operands[i] = treeService.find(filterTree, operandsCopy[i]);
    }

    console.log(" insertGroup - new tree: ", filterTree);
    console.log(" operands ", operands)
}

export function createGroup(filterTree, operands) {
    console.log("advancedFilterService - createGroup() - filterTree ", filterTree, ', operands ', operands)
    return groupUtilService.createGroup(filterTree, operands);
}


export function adjoinOperands(filterTree, operands) {
    console.log("advancedFilterService - adjoinOperands() - filterTree ", filterTree, ', operands ', operands)
    var operandsCopy = deepcopy(operands)
    // console.log(' aaa - Operands copy', operandsCopy)
    for (var i = 1; i < operands.length; i++) {
        move(filterTree, treeService.find(filterTree, operands[i]), treeService.find(filterTree, operands[i - 1]));

    }

    for (var i = 0; i < operandsCopy.length; i++) {
        operands[i] = treeService.find(filterTree, operandsCopy[i]);
    }



}



export function ungroup(filterTree, group) {
    console.log("advancedFilterService - ungroup() - filterTree ", filterTree, ', group ', group)
    var groupCopy = deepcopy(group)

    while (groupUtilService.getLastOperand(groupCopy)) {
        move(filterTree, groupUtilService.getLastOperand(groupCopy), groupCopy)

    }

    operandUtilService.remove(filterTree, treeService.find(filterTree, groupCopy))
    treeService.setFilterTree(filterTree)

}


export function replaceElement(filterTree, source, destination) {
    console.log("advancedFilterService - replaceElement() - filterTree ", filterTree, ', source ', source, ', destination ', destination)
    treeService.replace(filterTree, source, destination)
}

export function getGroupExpression(group) {
    console.log("advancedFilterService - getGroupExpression() - group ", group)
    return groupUtilService.getChildExpression(group);
}

export function getLastGroupOperand(group) {
    console.log("advancedFilterService - getLastGroupOperand() - group ", group)
    return groupUtilService.getLastOperand(group);
}

export function isSameGroup(filterTree, operands) {
    console.log("advancedFilterService - isSameGroup() - filterTree ", filterTree, ', operands ', operands)
    return groupUtilService.areInSameGroup(filterTree, operands)
}

export function getGroup(tree, operand) {
    console.log("advancedFilterService - getGroup() - tree ", tree, ', operand ', operand)
    return groupUtilService.getGroup(tree, operand);
}

export function getGroupOperands(group) {
    console.log("advancedFilterService - getGroupOperands() - group ", group)
    return groupUtilService.getGroupOperands(group)
}

export function getGroupSibling(filterTree, group) {
    console.log("advancedFilterService - getGroupSibling() - filterTree ", filterTree, ', group ', group)
    return groupUtilService.getSibilng(filterTree, group);

}

export function getGroupSiblingExpressionOperator(filterTree, group) {
    console.log("advancedFilterService - getGroupSiblingExpressionOperator() - filterTree ", filterTree, ', group ', group)
    return treeService.getParent(filterTree, getGroupSibling(filterTree, group))
}

export function getFirstLevelOperandsAdvancedFilterService(filterTree) {
    console.log("advancedFilterService - getFirstLevelOperandsAdvancedFilterService() - filterTree ", filterTree)
    return operandUtilService.getFirstLevelOperands(filterTree);
}
