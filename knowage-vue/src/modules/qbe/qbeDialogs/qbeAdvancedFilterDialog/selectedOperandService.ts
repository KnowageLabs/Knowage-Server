import { getFilterTree } from './treeService'
import { isSameGroup, getGroup, getFirstLevelOperandsAdvancedFilterService } from './advancedFilterService'

const assert = require('assert')
var selected = [] as any

export function getSelected() {
    console.log("selectedOperandService - getSelected()")
    return selected;
}

export function add(operand) {
    console.log("selectedOperandService - add() - operand ", operand)
    selected.push(operand)
}

export function contains(operand) {
    console.log("selectedOperandService - contains() - operand ", operand)
    for (var i = 0; i < selected.length; i++) {
        if (assert.deepEqual(selected[i], operand)) {
            return true;
        }
    }

    return false;
}

export function remove(operand) {
    console.log("selectedOperandService - remove() - operand ", operand)
    for (var i = 0; i < selected.length; i++) {
        if (assert.deepEqual(selected[i], operand)) {
            selected.splice(i, 1)
        }
    }
}

export function addOrRemove(operand) {
    console.log("selectedOperandService - addOrRemove() - operand ", operand)
    if (contains(operand)) {
        remove(operand)
    } else {
        add(operand)
    }

    console.log(selected)
}

export function unSelectAll() {
    console.log("selectedOperandService - unSelectAll()")
    selected.length = 0;
}

export function isSingleGroupSelected() {
    console.log("selectedOperandService - isSingleGroupSelected()")
    return selected.length === 1 && selected[0].value === 'PAR'
}

export function isSelectable(operand) {
    console.log("selectedOperandService - isSelectable() - operand ", operand)
    return isEmpty() || (!isEmpty() && isSameGroupAsSelected(operand)) &&
        !allOtherGroupMembersAreSelected(operand) && !allOtherSameLevelMembersAreSelected(operand)
}

export function isEmpty() {
    console.log("selectedOperandService - isEmpty()")
    return selected.length === 0
}

export function isSameGroupAsSelected(operand) {
    console.log("selectedOperandService - isSameGroupAsSelected() - operand ", operand)
    return isSameGroup(getFilterTree, [selected[0], operand])
}

export function getGroupOperands(groupOperand) {
    console.log("selectedOperandService - getGroupOperands() - groupOperand ", groupOperand)
    return getGroupOperands(getGroup(getFilterTree, groupOperand))
}

export function getGroupOperandsCount(groupOperand) {
    console.log("selectedOperandService - getGroupOperandsCount() - groupOperand ", groupOperand)
    if (getGroupOperands(groupOperand) && Array.isArray(getGroupOperands(groupOperand))) {
        return getGroupOperands(groupOperand).length;
    }
    return 0;
}

export function allOtherGroupMembersAreSelected(operand) {
    console.log("selectedOperandService - allOtherGroupMembersAreSelected() - operand ", operand)
    return getGroupOperandsCount(operand) - getSelectedCount() === 1 && !contains(operand)
}

export function allOtherSameLevelMembersAreSelected(operand) {
    console.log("selectedOperandService - allOtherSameLevelMembersAreSelected() - operand ", operand)
    return getFirstLevelOperandsCount() - getSelectedCount() === 1 && !contains(operand) && isFirstLevelOperand(operand)
}

export function getFirstLevelOperands() {
    console.log("selectedOperandService - getFirstLevelOperands()")
    return getFirstLevelOperandsAdvancedFilterService(getFilterTree);
}

export function isFirstLevelOperand(operand) {
    console.log("selectedOperandService - isFirstLevelOperand() - operand ", operand)
    for (var i = 0; i < getFirstLevelOperands().length; i++) {
        if (assert.deepEqual(getFirstLevelOperands()[i], operand)) {
            return true;
        }
    }

    return false;
}



export function getFirstLevelOperandsCount() {
    console.log("selectedOperandService - getFirstLevelOperandsCount()")
    if (getFirstLevelOperands() && Array.isArray(getFirstLevelOperands())) {
        return getFirstLevelOperands().length;
    }
    return 0;
}

export function getSelectedCount() {
    console.log("selectedOperandService - getSelectedCount()")
    return selected.length;
}

export function isMovable(operand) {
    console.log("selectedOperandService - isMovable() - operand ", operand)
    return (isFirstLevelOperand(operand) && getFirstLevelOperandsCount() > 2) || getGroupOperandsCount(operand) > 2
}
