
const advancedFilterService = require('./advancedFilterService')
const treeService = require('./treeService')
const deepEqual = require('deep-equal')

const selected = [] as any

export function getSelected() {
    return selected;
}

export function add(operand) {
    selected.push(operand)
}

export function contains(operand) {
    for (let i = 0; i < selected.length; i++) {
        if (deepEqual(selected[i], operand)) {
            return true;
        }
    }

    return false;
}

export function remove(operand) {
    for (let i = 0; i < selected.length; i++) {
        if (deepEqual(selected[i], operand)) {
            selected.splice(i, 1)
        }
    }
}

export function addOrRemove(operand) {
    if (contains(operand)) {
        remove(operand)
    } else {
        add(operand)
    }
}

export function unSelectAll() {
    selected.length = 0;
}

export function isSingleGroupSelected() {
    return selected.length === 1 && selected[0].value === 'PAR'
}

export function isSelectable(operand) {
    return isEmpty() || (!isEmpty() && isSameGroupAsSelected(operand)) && !allOtherGroupMembersAreSelected(operand) && !allOtherSameLevelMembersAreSelected(operand)
}

export function isEmpty() {
    return selected.length === 0
}

export function isSameGroupAsSelected(operand) {
    return advancedFilterService.isSameGroup(treeService.getFilterTree(), [selected[0], operand])
}

export function getGroupOperands(groupOperand) {
    return advancedFilterService.getGroupOperands(advancedFilterService.getGroup(treeService.getFilterTree(), groupOperand))
}

export function getGroupOperandsCount(groupOperand) {
    if (getGroupOperands(groupOperand) && Array.isArray(getGroupOperands(groupOperand))) {
        return getGroupOperands(groupOperand).length;
    }
    return 0;
}

export function allOtherGroupMembersAreSelected(operand) {
    return getGroupOperandsCount(operand) - getSelectedCount() === 1 && !contains(operand)
}

export function allOtherSameLevelMembersAreSelected(operand) {
    return getFirstLevelOperandsCount() - getSelectedCount() === 1 && !contains(operand) && isFirstLevelOperand(operand)
}

export function getFirstLevelOperands() {
    return advancedFilterService.getFirstLevelOperandsAdvancedFilterService(treeService.getFilterTree());
}

export function isFirstLevelOperand(operand) {
    for (let i = 0; i < getFirstLevelOperands().length; i++) {
        if (deepEqual(getFirstLevelOperands()[i], operand)) {
            return true;
        }
    }

    return false;
}



export function getFirstLevelOperandsCount() {
    if (getFirstLevelOperands() && Array.isArray(getFirstLevelOperands())) {
        return getFirstLevelOperands().length;
    }
    return 0;
}

export function getSelectedCount() {
    return selected.length;
}

export function isMovable(operand) {
    return (isFirstLevelOperand(operand) && getFirstLevelOperandsCount() > 2) || getGroupOperandsCount(operand) > 2
}
