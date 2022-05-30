import { iField, iFilter, iQBE, iQuery } from './QBE'
import { findByName, replace } from './qbeDialogs/qbeAdvancedFilterDialog/treeService'

export function onFiltersSaveCallback(filters: iFilter[], field: iField, parameters: any[], expression: any, qbe: iQBE | null, selectedQuery: iQuery, smartView: boolean, executeQBEQuery: Function) {
    if (!qbe) return

    for (let i = 0; i < filters.length; i++) {
        const tempFilter = filters[i]
        const index = selectedQuery.filters.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
        if (index !== -1) {
            selectedQuery.filters[index] = tempFilter
        } else {
            selectedQuery.filters.push(tempFilter)
        }
    }

    removeDeletedFilters(filters, field, expression, qbe, selectedQuery)

    refresh(selectedQuery.filters, expression, qbe, selectedQuery)

    if (selectedQuery.expression.childNodes?.length === 0) {
        selectedQuery.expression = {}
    }

    qbe.pars = parameters ? [...parameters] : []

    if (smartView) {
        executeQBEQuery(false)
    }
}

export function refresh(filters: iFilter[], expression: any, qbe: iQBE, selectedQuery: iQuery) {
    if (!qbe) return

    for (let filter of filters) {
        var newConst = {
            value: '$F{' + filter.filterId + '}',
            childNodes: [],
            details: {
                leftOperandAlias: filter.leftOperandAlias,
                operator: filter.operator,
                entity: filter.entity,
                rightOperandValue: filter.rightOperandValue.join(', ')
            },
            type: 'NODE_CONST'
        }
        var oldConst = findByName(expression, newConst.value)

        replace(expression, newConst, oldConst)
    }
    selectedQuery.expression = expression
}

export function removeDeletedFilters(filters: iFilter[], field: iField, expression: any, qbe: iQBE, selectedQuery: iQuery) {
    if (!qbe) return

    for (let i = selectedQuery.filters.length - 1; i >= 0; i--) {
        const tempFilter = selectedQuery.filters[i]
        if (tempFilter.leftOperandValue === field.id) {
            const index = filters.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
            if (index === -1) {
                selectedQuery.filters.splice(i, 1)
            }
        }
    }
}